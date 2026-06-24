package data.core;

import exception.DataAnalyzerException;
import model.social.Comment;
import model.social.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lớp trừu tượng BaseHtmlScraper kế thừa từ BaseCollector, chuyên biệt cho việc
 * khai thác dữ liệu (Web Scraping) từ các trang tin điện tử bằng cách phân tích
 * cú pháp HTML (sử dụng thư viện Jsoup).
 *
 * Lớp này định nghĩa luồng duyệt tin tự động bao gồm phân trang, trích xuất liên kết,
 * tiền lọc, cào chi tiết bài viết và xử lý độ trễ để tránh bị máy chủ web chặn
 * (IP block).
 *
 * @author Nguyễn Thị Thanh Trúc - 202417052
 */
public abstract class BaseHtmlScraper extends BaseCollector {

    // Chuỗi User-Agent giả lập trình duyệt Chrome để tránh bị chặn bởi cơ chế bảo
    // vệ của máy chủ web
    protected static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36";

    // Bộ định dạng ngày tháng áp dụng phổ biến cho các trang báo mạng Việt Nam
    protected static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ROOT);

    // --- CÁC PHƯƠNG THỨC TRỪU TƯỢNG ĐỂ LỚP CON CẤP THÔNG TIN CSS SELECTOR ---
    // Xây dựng URL trang tìm kiếm dựa trên từ khóa đã mã hóa và số trang hiện tại
    protected abstract String buildSearchUrl(String encodedQuery, int page);

    // Trả về CSS Selector để chọn liên kết (thẻ a) của bài viết từ danh sách tìm
    // kiếm
    protected abstract String getArticleLinkSelector();

    // Trả về CSS Selector để trích xuất tiêu đề bài viết trong trang chi tiết
    protected abstract String getTitleSelector();

    // Trả về CSS Selector để trích xuất phần tóm tắt ngắn (Sapo) bài viết
    protected abstract String getSapoSelector();

    // Trả về CSS Selector để trích xuất nội dung bài viết
    protected abstract String getContentSelector();

    // Trả về CSS Selector để trích xuất mốc thời gian đăng tải bài viết
    protected abstract String getDateSelector();

    /**
     * Phương thức lọc nhanh mốc thời gian ngay tại trang danh sách tìm kiếm (nếu có
     * hiển thị).
     * Mặc định trả về null. Lớp con có thể ghi đè phương thức này để tối ưu hóa
     * hiệu năng,
     * loại bỏ sớm các bài viết quá cũ mà không cần gửi thêm yêu cầu HTTP tải trang
     * chi tiết.
     *
     * @param card Phần tử HTML chứa tóm tắt bài viết ở danh sách tìm kiếm
     * @return Date ngày đăng tải (nếu có), hoặc null nếu không hỗ trợ lọc nhanh
     */
    protected Date preFilterDate(Element card) {
        return null;
    }

    /**
     * Thực hiện thu thập các bài viết thô từ các trang báo mạng điện tử (Template
     * Method).
     * Tiến hành phân trang liên tục, lấy danh sách link bài viết và phân tích chi
     * tiết cho đến khi
     * thu thập đủ số lượng yêu cầu (maxCount) hoặc không còn bài viết mới được tìm
     * thấy.
     */
    @Override
    protected final List<Post> fetchRawPosts(String query, Date startDate, Date endDate)
            throws DataAnalyzerException {
        List<Post> collectedData = new ArrayList<>();
        Set<String> processedUrls = new HashSet<>(); // Quản lý trùng lặp URL trong đợt thu thập dữ liệu
        int totalProcessed = 0;
        int currentPage = 1;

        // Mã hóa từ khóa truy vấn sang chuẩn UTF-8 để nhúng an toàn vào URL tìm kiếm
        String encodedQuery;
        try {
            encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
        } catch (java.io.UnsupportedEncodingException e) {
            throw new exception.QueryEncodingException("Lỗi mã hoá từ khoá: " + e.getMessage(), e);
        }

        while (totalProcessed < maxCount) {
            String searchUrl = buildSearchUrl(encodedQuery, currentPage);
            log("Accessing page: " + searchUrl);

            Document searchPage;
            try {
                // Tải tài liệu HTML từ trang tìm kiếm bằng Jsoup
                searchPage = Jsoup.connect(searchUrl).userAgent(USER_AGENT).timeout(15000).get();
            } catch (IOException e) {
                log("Connection error: " + e.getMessage());
                throw new exception.NetworkConnectionException("Lỗi kết nối tới " + searchUrl + ": " + e.getMessage(),
                        e);
            }

            // Trích xuất các phần tử chứa liên kết bài viết bằng CSS Selector
            Elements articleLinks = searchPage.select(getArticleLinkSelector());
            if (articleLinks.isEmpty()) {
                log("No more articles found. Pagination ended.");
                break;
            }

            // Số bài viết mới chưa được cào trên trang hiện tại
            int newArticlesOnPage = 0;
            for (Element linkElement : articleLinks) {
                if (totalProcessed >= maxCount)
                    break;

                // Đảm bảo lấy chính xác thẻ 'a' chứa thuộc tính href
                Element actualLink = linkElement.is("a") ? linkElement : linkElement.selectFirst("a");
                if (actualLink == null)
                    continue;

                String articleUrl = actualLink.attr("abs:href"); // Lấy URL tuyệt đối dạng đầy đủ
                if (articleUrl.isEmpty() || processedUrls.contains(articleUrl))
                    continue;

                newArticlesOnPage++;

                // Bước tiền lọc (Pre-filter) ngày tháng ngoài danh sách nếu lớp con hỗ trợ ghi
                // đè
                Date cardDate = preFilterDate(linkElement);
                if (cardDate != null && !isInDateRange(cardDate, startDate, endDate)) {
                    logRejectedItem("PRE-FILTER: Date out of range: " + articleUrl);
                    continue;
                }

                processedUrls.add(articleUrl);

                // Thực hiện cào chi tiết bài viết từ liên kết cụ thể
                Post article = scrapeArticleDetail(articleUrl);

                if (article != null) {
                    Date articleDate = article.getTimestamp();
                    // Lọc chính xác mốc thời gian của bài viết sau khi đã parse từ chi tiết trang
                    if (isInDateRange(articleDate, startDate, endDate)) {
                        collectedData.add(article);
                        totalProcessed++;
                        logAcceptedItem(articleUrl, extractTitleFromLink(actualLink), totalProcessed, maxCount);
                    } else {
                        logRejectedItem("Date out of range: " + articleUrl);
                    }
                } else {
                    logRejectedItem("CONTENT OR DATE CAN'T BE SCRAPED");
                }

                // Tránh gửi yêu cầu quá dồn dập, thể hiện tính "lịch sự" của Crawler (Polite
                // Crawling)
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new exception.ProcessInterruptedException("Tiến trình bị gián đoạn", e);
                }
            }

            if (newArticlesOnPage == 0)
                break;

            currentPage++;
            // Độ trễ ngắn khi chuẩn bị chuyển sang phân trang tiếp theo
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new exception.ProcessInterruptedException("Tiến trình bị gián đoạn", e);
            }
        }
        return collectedData;
    }

    /**
     * Tải và phân tích trang bài viết chi tiết, bóc tách các trường dữ liệu cần
     * thiết để tạo thực thể Post.
     *
     * @param url Liên kết bài viết cần khai thác chi tiết
     * @return Đối tượng {@link Post} hoàn chỉnh hoặc null nếu có lỗi phát sinh
     */
    private Post scrapeArticleDetail(String url) {
        try {
            // Tải tài liệu HTML chi tiết bài viết
            Document doc = Jsoup.connect(url).userAgent(USER_AGENT).timeout(10000).get();

            Element titleEl = doc.selectFirst(getTitleSelector());
            String title = (titleEl != null) ? titleEl.text().trim() : "N/A";

            Element sapoEl = doc.selectFirst(getSapoSelector());
            String sapo = (sapoEl != null) ? sapoEl.text().trim() : "";

            String content = doc.select(getContentSelector()).text();

            // Gộp tiêu đề, sapo và nội dung thành văn bản thô đầy đủ cho bài viết
            String fullText = title + ". " + sapo + ". " + content;

            // Cơ chế tự động bóc tách PostId từ URL bài viết dựa trên định dạng (ví dụ:
            // title-slug-12345.html -> ID: 12345)
            String postId;
            try {
                postId = url.substring(url.lastIndexOf('-') + 1, url.lastIndexOf('.'));
            } catch (Exception e) {
                postId = String.valueOf(url.hashCode()); // Tránh trùng lặp khóa nếu cấu trúc URL dị biệt
            }

            Date finalDate = extractDate(doc);
            if (finalDate == null)
                return null; // Bỏ qua bài viết nếu không xác định được mốc thời gian đăng tải hợp lệ

            return new Post(postId, fullText, finalDate, 0, 0);

        } catch (Exception e) {
            log("      -> ⚠ ERROR WHEN SCRAPING: " + e.getMessage());
            return null;
        }
    }

    /**
     * Trích xuất mốc thời gian đăng tải bài viết từ văn bản HTML bằng biểu thức
     * chính quy (Regex).
     */
    private Date extractDate(Document doc) {
        Element dateEl = doc.selectFirst(getDateSelector());
        if (dateEl == null)
            return null;

        String rawText = dateEl.text().trim();
        // Regex tìm kiếm mốc ngày (dd/MM/yyyy) và giờ (HH:mm) nằm trong văn bản thô
        Matcher m = Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4}).*?(\\d{1,2}:\\d{2})").matcher(rawText);
        if (m.find()) {
            String cleanTimeStr = m.group(1) + " " + m.group(2);
            try {
                return DATE_FORMATTER.parse(cleanTimeStr);
            } catch (ParseException e) {
                // Bỏ qua nếu lỗi định dạng ngày tháng không thể parse
            }
        }
        return null;
    }

    /**
     * Mặc định các trang tin điện tử (báo mạng) không hỗ trợ cào bình luận bằng
     * HTML tĩnh dễ dàng
     * (do bình luận thường được tải động qua API hoặc plugin bên thứ ba), do đó
     * phương thức này mặc định trả về danh sách rỗng.
     */
    @Override
    protected List<Comment> fetchCommentsForPost(String postId, Date startDate, Date endDate)
            throws DataAnalyzerException {
        return Collections.emptyList();
    }

    /**
     * Hỗ trợ trích xuất nhanh tiêu đề bài viết từ thẻ liên kết (Fallback qua thuộc
     * tính title hoặc alt của ảnh nếu văn bản rỗng).
     */
    private String extractTitleFromLink(Element link) {
        String title = link.text().trim();
        if (title.isEmpty()) {
            title = link.attr("title").trim();
        }
        if (title.isEmpty()) {
            Element img = link.selectFirst("img");
            if (img != null) {
                title = img.attr("alt").trim();
            }
        }
        return title.isEmpty() ? "N/A" : title;
    }

    // --- CÁC PHƯƠNG THỨC TIỆN ÍCH GHI LOG ĐƯỢC CHUẨN HÓA ĐỊNH DẠNG ---

    protected void logAcceptedItem(String itemUrl, String title, int current, int total) {
        String pad = new String(new char[this.getClass().getSimpleName().length() + 3]).replace('\0', ' ');
        log("      -> ✓ ACCEPTED (" + current + "/" + total + "): " + itemUrl + " \n" + pad + "      -> Title: "
                + title);
    }

    protected void logRejectedItem(String reason) {
        log("      -> ✗ REJECTED: " + reason);
    }
}
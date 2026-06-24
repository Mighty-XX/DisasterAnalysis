package data.collectors;

import data.core.BaseHtmlScraper;
import org.jsoup.nodes.Element;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lớp VietNamNetCollector kế thừa từ BaseHtmlScraper, thực hiện việc cào dữ liệu chi tiết
 * từ báo điện tử VietNamNet (vietnamnet.vn).
 *
 * Đặc trưng của VietNamNet là sở hữu nhiều cấu trúc giao diện HTML khác nhau tùy theo thời kỳ đăng bài
 * và thể loại bài viết (Video, Infographic, bài viết thường). Do đó, lớp này sử dụng tập hợp các bộ chọn CSS
 * thay thế (phân tách bởi dấu phẩy) để đảm bảo tính tương thích và ổn định tối đa cho hệ thống.
 *
 * @author Nguyễn Thị Thanh Trúc - 202417052
 */
public class VietNamNetCollector extends BaseHtmlScraper {

    // Bộ định dạng ngày tháng phục vụ việc đọc nhanh mốc thời gian ngoài trang danh sách tìm kiếm
    private static final SimpleDateFormat LIST_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);

    /**
     * Xây dựng URL trang tìm kiếm của VietNamNet.
     * Hệ thống tìm kiếm của VietNamNet đếm số trang bắt đầu từ 0 (Trang 1 ứng với chỉ số p0).
     * Do đó, chỉ số trang gửi đi được tính toán bằng công thức: (page - 1).
     */
    @Override
    protected String buildSearchUrl(String encodedQuery, int page) {
        return "https://vietnamnet.vn/tim-kiem-p" + (page - 1) + "?q=" + encodedQuery;
    }

    /**
     * Định nghĩa các bộ chọn CSS thay thế (cách nhau bởi dấu phẩy) để tìm liên kết bài viết.
     * Giúp tương thích với nhiều kiểu giao diện hiển thị của VietNamNet như:
     * bài viết ngang (.horizontalPost), bài viết dọc (.verticalPost), mục tìm kiếm (.search-result-item).
     */
    @Override
    protected String getArticleLinkSelector() {
        return ".horizontalPost, .verticalPost, .search-result-item, .backItem";
    }

    /**
     * Định nghĩa các bộ chọn CSS thay thế để trích xuất tiêu đề bài viết trong trang chi tiết.
     */
    @Override
    protected String getTitleSelector() {
        return "h1.content-detail-title, h1.title, h1.post-title, h1";
    }

    /**
     * Định nghĩa các bộ chọn CSS thay thế để trích xuất đoạn tóm tắt ngắn (Sapo) của bài viết.
     */
    @Override
    protected String getSapoSelector() {
        return ".content-detail-sapo, .sapo, .bold, h2";
    }

    /**
     * Định nghĩa các bộ chọn CSS thay thế để trích xuất các đoạn văn bản nội dung bài viết.
     */
    @Override
    protected String getContentSelector() {
        return ".content-detail-text p, .maincontent p, #vnn-container-detail p, article p";
    }

    /**
     * Định nghĩa các bộ chọn CSS thay thế để định vị thẻ chứa mốc thời gian đăng tải bài viết.
     */
    @Override
    protected String getDateSelector() {
        return ".bread-crumb-detail__time, .content-detail-date, .date-time, .date, .pubDate";
    }

    /**
     * Ghi đè phương thức tiền lọc (Pre-filter) mốc thời gian trực tiếp ngoài trang danh sách tìm kiếm.
     * VietNamNet hiển thị sẵn ngày đăng ngoài danh sách tìm kiếm, giúp hệ thống lọc bỏ sớm các bài viết cũ
     * mà không cần tốn băng thông tải trang chi tiết bài viết.
     *
     * @param card Phần tử HTML chứa một khối thông tin bài viết ngoài danh sách tìm kiếm
     * @return Date ngày đăng tải (nếu lọc được), ngược lại trả về null để chuyển sang cào chi tiết ở bước sau
     */
    @Override
    protected Date preFilterDate(Element card) {
        // Tìm thẻ chứa thời gian hiển thị ngoài danh sách bằng các bộ chọn thay thế
        Element timeElement = card.selectFirst(".verticalPost__time, .time, .date, .search-result-item-date");
        if (timeElement == null)
            return null;

        String rawCardDate = timeElement.text().trim();
        // Sử dụng biểu thức chính quy (Regex) lọc ra chuỗi ngày tháng dạng dd/MM/yyyy
        Matcher m = Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})").matcher(rawCardDate);
        if (m.find()) {
            try {
                // Phân tích cú pháp ngày đăng tải từ kết quả Regex thu được
                return LIST_DATE_FORMAT.parse(m.group(1));
            } catch (Exception e) {
                // Bỏ qua lỗi định dạng để tiến hành kiểm tra kỹ hơn ở trang chi tiết
            }
        }
        return null;
    }
}
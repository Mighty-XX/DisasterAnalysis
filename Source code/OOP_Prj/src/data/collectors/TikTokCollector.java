package data.collectors;

import com.google.gson.JsonObject;
import data.core.BaseRestApiCollector;
import exception.DataAnalyzerException;
import model.social.Comment;
import model.social.Post;

import java.util.*;

/**
 * Lớp TikTokCollector kế thừa từ BaseRestApiCollector, thực hiện nhiệm vụ thu thập
 * bài viết (Video đóng vai trò là Post) từ nền tảng TikTok thông qua dịch vụ cào dữ liệu trung gian RapidAPI.
 *
 * Lớp này kế thừa khung phân trang tổng quát từ lớp cha giúp tinh giản tối đa mã nguồn,
 * đồng thời tích hợp xử lý tiêu đề bảo mật xác thực yêu cầu HTTP Headers và làm sạch nội dung văn bản.
 *
 * @author Nguyễn Thị Thanh Trúc - 202417052
 */
public class TikTokCollector extends BaseRestApiCollector {

    // Khóa kết nối xác thực tài khoản dịch vụ trên hệ thống RapidAPI
    private String rapidApiKey = "ca309c0b79msh2893ee04fc55c6dp1f1833jsn1cbb904b091c";

    // Tên miền máy chủ đích của dịch vụ cào dữ liệu TikTok Scraper trên hệ thống RapidAPI
    private static final String RAPIDAPI_HOST = "tiktok-scraper7.p.rapidapi.com";

    /**
     * Khởi tạo và thiết lập các thông số cấu hình ban đầu cho bộ thu thập dữ liệu TikTok.
     * Hỗ trợ nạp động khóa API Key trực tiếp từ giao diện người dùng nếu được cung cấp.
     *
     * @param configParams Bản đồ (Map) chứa các tham số cấu hình dưới dạng Key-Value
     */
    @Override
    public void initialize(Map<String, String> configParams) {
        super.initialize(configParams);
        if (configParams != null && configParams.containsKey("apiKey")) {
            this.rapidApiKey = configParams.get("apiKey");
        }
        log("--- TikTok Collector Initialized (Via RapidAPI Scraper) ---");
    }

    /**
     * Xây dựng URL gọi API tìm kiếm video của TikTok.
     * Sử dụng tài nguyên '/feed/search' của dịch vụ cào dữ liệu trung gian.
     *
     * @param encodedQuery Từ khóa tìm kiếm đã mã hóa chuẩn URL UTF-8
     * @param maxCount     Số lượng kết quả yêu cầu tối đa
     * @return Chuỗi liên kết URL tìm kiếm của TikTok Scraper
     */
    @Override
    protected String buildSearchUrl(String encodedQuery, int maxCount) {
        return "https://" + RAPIDAPI_HOST + "/feed/search?keywords=" + encodedQuery;
    }

    /**
     * Thực hiện luồng thu thập video thô từ TikTok API dựa trên từ khóa tìm kiếm và khoảng thời gian giới hạn.
     *
     * @param query     Từ khóa tìm kiếm dữ liệu
     * @param startDate Ngày bắt đầu khoảng thu thập
     * @param endDate   Ngày kết thúc khoảng thu thập
     * @return Danh sách các đối tượng {@link Post} thu thập được
     * @throws DataAnalyzerException Khi tiến trình bị gián đoạn hoặc xảy ra lỗi API mạng hệ thống
     */
    @Override
    protected List<Post> fetchRawPosts(String query, Date startDate, Date endDate) throws DataAnalyzerException {
        log("TikTok Search Query: " + query);
        if (rapidApiKey == null || rapidApiKey.startsWith("HÃY_DÁN_MÃ_KEY")) {
            return Collections.emptyList();
        }

        // Cấu hình các Header bắt buộc để xác thực quyền truy cập với hệ thống RapidAPI
        Map<String, String> headers = Map.of("X-RapidAPI-Key", rapidApiKey, "X-RapidAPI-Host", RAPIDAPI_HOST);
        String searchUrl = buildSearchUrl(encode(query), maxCount);

        // Khai báo bộ đếm ngoài dạng mảng 1 phần tử để truyền và tăng giá trị ổn định bên trong biểu thức Lambda
        int[] counter = {0};

        // Thực thi phân trang thông qua khung hàm collectPaginated của lớp cha
        return collectPaginated(
                "0",
                token -> searchUrl + "&count=30&cursor=" + token,
                url -> fetchJsonWithHeaders(url, headers),
                root -> getArray(getObj(root, "data"), "videos"),
                item -> {
                    String id = getStr(item, "video_id");

                    // Làm sạch tiêu đề video, lọc bỏ biểu tượng cảm xúc (Emoji) rác tránh lỗi font hiển thị
                    String title = getStr(item, "title").replaceAll("[\\p{So}\\p{Cn}]", "").trim();

                    // Quy đổi mốc thời gian đăng video (Unix Timestamp dạng giây sang mili giây)
                    Date publishedAt = new Date(getLong(item, "create_time") * 1000L);

                    // Kiểm tra điều kiện mốc thời gian đăng nằm trong khoảng yêu cầu
                    if (!isInDateRange(publishedAt, startDate, endDate)) {
                        logRejectedItem(title, publishedAt);
                        return null;
                    }

                    // Trích xuất thông tin định danh tác giả (unique_id) để tạo link xem video trực tuyến đầy đủ
                    String authorId = getObj(item, "author") != null ? getStr(getObj(item, "author"), "unique_id") : "";
                    String videoUrl = authorId.isEmpty() ? "https://www.tiktok.com/video/" + id : "https://www.tiktok.com/@" + authorId + "/video/" + id;

                    // Tăng bộ đếm cho bài viết được chấp nhận và hiển thị tiến độ (1/20, 2/20,...) ra Console kèm độ trễ mượt giao diện
                    counter[0]++;
                    logAcceptedItemWithDelay(videoUrl, title, publishedAt, counter[0], maxCount);

                    return new Post(id, title, publishedAt, getInt(item, "digg_count"), getInt(item, "comment_count"));
                },
                (root, items) -> {
                    JsonObject data = getObj(root, "data");
                    return (data != null && data.has("cursor")) ? data.get("cursor").getAsString() : null;
                },
                1500
        );
    }

    /**
     * Thu thập danh sách bình luận (Comments) cho một video TikTok chỉ định.
     * Sử dụng tài nguyên '/comment/list' của dịch vụ cào dữ liệu RapidAPI.
     *
     * @param postId    ID bài viết cần lấy bình luận
     * @param startDate Ngày bắt đầu giới hạn khoảng thời gian thu thập bình luận
     * @param endDate   Ngày kết thúc giới hạn khoảng thời gian thu thập bình luận
     * @return Danh sách các đối tượng {@link Comment} thu thập được của bài viết
     * @throws DataAnalyzerException Khi tiến trình bị gián đoạn từ phía người dùng hoặc lỗi API mạng xảy ra
     */
    @Override
    protected List<Comment> fetchCommentsForPost(String postId, Date startDate, Date endDate) throws DataAnalyzerException {
        Map<String, String> headers = Map.of("X-RapidAPI-Key", rapidApiKey, "X-RapidAPI-Host", RAPIDAPI_HOST);
        String videoUrl = "https://www.tiktok.com/video/" + postId;

        return collectPaginated(
                "0",
                token -> "https://" + RAPIDAPI_HOST + "/comment/list?url=" + encode(videoUrl) + "&count=30&cursor=" + token,
                url -> fetchJsonWithHeaders(url, headers),
                root -> getArray(getObj(root, "data"), "comments"),
                item -> {
                    long createTime = getLong(item, "create_time");
                    Date cmtDate = createTime > 0 ? new Date(createTime * 1000L) : new Date();

                    // Lọc an toàn: Chỉ chấp nhận bình luận nằm trong khoảng thời gian yêu cầu
                    if (!isInDateRange(cmtDate, startDate, endDate)) {
                        return null;
                    }

                    String content = getStr(item, "text");
                    if (content.isEmpty()) {
                        content = getStr(item, "desc");
                    }

                    String cid = getStr(item, "cid").isEmpty() ? getStr(item, "id") : getStr(item, "cid");

                    return new Comment(postId, cid, content, cmtDate, getInt(item, "digg_count"));
                },
                (root, items) -> {
                    JsonObject data = getObj(root, "data");
                    return (data != null && data.has("cursor")) ? data.get("cursor").getAsString() : null;
                },
                1500,
                15
        );
    }
}
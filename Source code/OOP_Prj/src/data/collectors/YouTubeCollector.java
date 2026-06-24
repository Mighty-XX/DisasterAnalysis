package data.collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import data.core.BaseRestApiCollector;
import exception.DataAnalyzerException;
import model.social.Comment;
import model.social.Post;

import java.util.*;

/**
 * Lớp YouTubeCollector kế thừa từ BaseRestApiCollector, thực hiện nhiệm vụ thu thập
 * bài viết (Video đóng vai trò là Post) và bình luận từ nền tảng YouTube thông qua YouTube Data API v3.
 *
 * Lớp này kế thừa khung phân trang tổng quát từ lớp cha giúp tinh giản tối đa mã nguồn,
 * kết hợp kỹ thuật tối ưu hạn ngạch gọi API bằng cuộc gọi thống kê gộp (Batch Request).
 *
 * @author Nguyễn Thị Thanh Trúc - 202417052
 */
public class YouTubeCollector extends BaseRestApiCollector {

    // Khóa xác thực API (API Key) để kết nối và gọi dịch vụ của YouTube Data API v3
    private final String apiKey = "AIzaSyBXc1t8OSG5vj_CSyWxpYKHlz6Mdi5Y8k4";

    /**
     * Xây dựng URL gọi API tìm kiếm video của YouTube.
     * Sử dụng tài nguyên 'search' của YouTube API, lọc riêng loại dữ liệu là 'video'.
     *
     * @param encodedQuery Từ khóa tìm kiếm đã mã hóa chuẩn URL UTF-8
     * @param maxCount     Số lượng kết quả tìm kiếm tối đa yêu cầu
     * @return Chuỗi liên kết URL gọi API tìm kiếm
     */
    @Override
    protected String buildSearchUrl(String encodedQuery, int maxCount) {
        return "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&q="
                + encodedQuery + "&maxResults=" + maxCount + "&key=" + apiKey;
    }

    /**
     * Thực hiện luồng thu thập video thô từ YouTube Data API dựa trên từ khóa và khoảng thời gian giới hạn.
     *
     * @param query     Từ khóa tìm kiếm dữ liệu
     * @param startDate Ngày bắt đầu khoảng thu thập
     * @param endDate   Ngày kết thúc khoảng thu thập
     * @return Danh sách các đối tượng {@link Post} thu thập được
     * @throws DataAnalyzerException Khi tiến trình bị gián đoạn hoặc xảy ra lỗi API mạng hệ thống
     */
    @Override
    protected List<Post> fetchRawPosts(String query, Date startDate, Date endDate) throws DataAnalyzerException {
        log("YouTube Search Query: " + query);

        // Xây dựng URL tìm kiếm cơ sở và ghép tham số mốc thời gian nếu có
        String baseUrl = buildSearchUrl(encode(query), maxCount)
                + (startDate != null ? "&publishedAfter=" + encode(formatIsoDate(startDate)) : "")
                + (endDate != null ? "&publishedBefore=" + encode(formatIsoDate(endDate)) : "");

        // Khai báo bộ đếm ngoài dạng mảng 1 phần tử để truyền và tăng giá trị ổn định bên trong biểu thức Lambda
        int[] counter = {0};

        // Thực thi phân trang thông qua khung hàm collectPaginated của lớp cha
        return collectPaginated(
                "",
                token -> baseUrl + (token.isEmpty() ? "" : "&pageToken=" + token),
                this::fetchJson,
                root -> getArray(root, "items"),
                item -> {
                    String videoId = getStr(getObj(item, "id"), "videoId");
                    JsonObject snippet = getObj(item, "snippet");
                    if (videoId.isEmpty() || snippet == null) {
                        return null;
                    }

                    Date publishedAt = parseIsoDate(getStr(snippet, "publishedAt"));

                    // Thu thập thống kê tương tác (lượt thích, lượt bình luận) thông qua cơ chế gọi gộp (Batch Request)
                    Map<String, JsonObject> statsMap = fetchBatchStats(Collections.singletonList(videoId));
                    JsonObject stats = statsMap.get(videoId);
                    int likes = getInt(stats, "likeCount");
                    int commentsCount = getInt(stats, "commentCount");

                    String fullContent = getStr(snippet, "title") + "\n" + getStr(snippet, "description");

                    // Tăng bộ đếm cho bài viết được chấp nhận và hiển thị tiến độ (1/20, 2/20,...) ra Console kèm độ trễ mượt giao diện
                    counter[0]++;
                    logAcceptedItemWithDelay("https://www.youtube.com/watch?v=" + videoId, getStr(snippet, "title"), publishedAt, counter[0], maxCount);

                    return new Post(videoId, fullContent, publishedAt, likes, commentsCount);
                },
                (root, items) -> getStr(root, "nextPageToken"),
                1500
        );
    }

    /**
     * Thu thập danh sách bình luận (Comments) cho một video YouTube chỉ định.
     * Sử dụng tài nguyên 'commentThreads' của YouTube API, tự động lọc theo mốc thời gian.
     *
     * @param postId    ID của video YouTube cần lấy bình luận
     * @param startDate Ngày bắt đầu giới hạn khoảng thời gian thu thập bình luận
     * @param endDate   Ngày kết thúc giới hạn khoảng thời gian thu thập bình luận
     * @return Danh sách các đối tượng {@link Comment} thu thập được của bài viết
     * @throws DataAnalyzerException Khi tiến trình bị gián đoạn từ phía người dùng hoặc lỗi API mạng xảy ra
     */
    @Override
    protected List<Comment> fetchCommentsForPost(String postId, Date startDate, Date endDate) throws DataAnalyzerException {
        String baseUrl = "https://www.googleapis.com/youtube/v3/commentThreads?part=snippet&videoId=" + postId
                + "&maxResults=50&order=relevance&key=" + apiKey;

        return collectPaginated(
                "",
                token -> baseUrl + (token.isEmpty() ? "" : "&pageToken=" + token),
                this::fetchJson,
                root -> getArray(root, "items"),
                item -> {
                    JsonObject snippet = getObj(item, "snippet");
                    JsonObject topLevelComment = getObj(snippet, "topLevelComment");
                    JsonObject snip = getObj(topLevelComment, "snippet");
                    if (snip == null) {
                        return null;
                    }

                    Date cmtDate = parseIsoDate(getStr(snip, "publishedAt"));
                    // Lọc an toàn: Chỉ chấp nhận bình luận nằm trong khoảng thời gian yêu cầu
                    if (!isInDateRange(cmtDate, startDate, endDate)) {
                        return null;
                    }

                    String content = getStr(snip, "textOriginal");
                    if (content.isEmpty()) {
                        content = getStr(snip, "textDisplay");
                    }

                    return new Comment(postId, getStr(topLevelComment, "id"), content, cmtDate, getInt(snip, "likeCount"));
                },
                (root, items) -> getStr(root, "nextPageToken"),
                500,
                15
        );
    }

    /**
     * Thực hiện cuộc gọi API gộp (Batch Request) để lấy thông tin thống kê tương tác cho danh sách ID video.
     * Giúp tiết kiệm hạn ngạch gọi mạng (Quota) của YouTube Data API.
     *
     * @param videoIds Danh sách các Video ID cần lấy thống kê
     * @return Bản đồ (Map) liên kết giữa Video ID và đối tượng JsonObject chứa số liệu thống kê tương tác
     */
    private Map<String, JsonObject> fetchBatchStats(List<String> videoIds) {
        Map<String, JsonObject> statsMap = new HashMap<>();
        if (videoIds.isEmpty()) {
            return statsMap;
        }

        // Ghép các ID video cách nhau bởi dấu phẩy để gọi API gộp một lượt
        String statsUrl = "https://www.googleapis.com/youtube/v3/videos?part=statistics&id="
                + String.join(",", videoIds) + "&key=" + apiKey;
        JsonElement statsRoot = fetchJson(statsUrl);
        if (statsRoot.isJsonObject() && statsRoot.getAsJsonObject().has("items")) {
            for (JsonElement video : statsRoot.getAsJsonObject().getAsJsonArray("items")) {
                JsonObject vObj = video.getAsJsonObject();
                statsMap.put(getStr(vObj, "id"), vObj.getAsJsonObject("statistics"));
            }
        }
        return statsMap;
    }
}
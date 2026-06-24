package data.core;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exception.DataAnalyzerException;
import org.jsoup.Jsoup;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Lớp trừu tượng BaseRestApiCollector kế thừa từ BaseCollector, chuyên biệt cho
 * việc thu thập dữ liệu (Data Collection) từ các nguồn cung cấp dịch vụ web
 * dạng REST API.
 *
 * Lớp này áp dụng mẫu thiết kế <b>Template Method Pattern</b> thông qua phương
 * thức
 * {@code collectPaginated}, định nghĩa khung quy trình phân trang và kiểm soát
 * luồng chung,
 * giúp loại bỏ mã nguồn trùng lặp ở các lớp con cụ thể (như YouTube,
 * TikTok,...).
 *
 * @author Nguyễn Thị Thanh Trúc - 202417052
 */
public abstract class BaseRestApiCollector extends BaseCollector {

    // Chuỗi User-Agent giả lập trình duyệt Chrome thực tế để tránh bị các hệ thống
    // API chặn kết nối
    protected static final String USER_AGENT_HEADER = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    // Đối tượng Gson dùng chung để bóc tách và phân tích cấu trúc dữ liệu JSON
    protected static final Gson gson = new Gson();

    // Định dạng ngày giờ chuẩn ISO 8601 thường được các hệ thống REST API lớn sử
    // dụng
    private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ROOT);

    static {
        // Cố định múi giờ UTC cho chuẩn ISO để đảm bảo tính nhất quán múi giờ khi lưu
        // trữ và phân tích
        ISO_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Định nghĩa khung quy trình phân trang tổng quát và thu thập dữ liệu tự động
     * (Pagination Template Method).
     * Phương thức này đóng vai trò điều khiển luồng lặp phân trang, xử lý lỗi hệ
     * thống và trì hoãn luồng lịch sự.
     *
     * @param <T>                Kiểu dữ liệu đối tượng cần thu thập (Post hoặc
     *                           Comment)
     * @param initialToken       Mã phân trang khởi đầu (Token hoặc Cursor)
     * @param urlBuilder         Hàm xây dựng URL cho từng trang dựa trên mã Token
     *                           hiện tại
     * @param jsonFetcher        Hàm thực hiện gửi yêu cầu HTTP để tải về tài liệu
     *                           JSON
     * @param itemsExtractor     Hàm bóc tách mảng danh sách phần tử thô từ đối
     *                           tượng JSON gốc
     * @param itemMapper         Hàm ánh xạ từ đối tượng JSON đơn lẻ sang đối tượng
     *                           dữ liệu đích
     * @param nextTokenExtractor Hàm trích xuất mã Token của trang tiếp theo từ JSON
     *                           phản hồi
     * @param delayMs            Thời gian trì hoãn giữa các yêu cầu gọi API (mili
     *                           giây)
     * @return Danh sách các đối tượng dữ liệu thu thập được đã được ánh xạ hoàn
     *         chỉnh
     * @throws DataAnalyzerException Khi tiến trình bị gián đoạn hoặc xảy ra lỗi
     *                                 API mạng hệ thống
     */
    protected <T> List<T> collectPaginated(
            String initialToken,
            Function<String, String> urlBuilder,
            Function<String, JsonElement> jsonFetcher,
            Function<JsonObject, JsonArray> itemsExtractor,
            Function<JsonObject, T> itemMapper,
            BiFunction<JsonObject, JsonArray, String> nextTokenExtractor,
            long delayMs) throws DataAnalyzerException {
        return collectPaginated(initialToken, urlBuilder, jsonFetcher, itemsExtractor, itemMapper, nextTokenExtractor,
                delayMs, this.maxCount);
    }

    /**
     * Định nghĩa khung quy trình phân trang tổng quát với giới hạn số lượng tuỳ
     * chỉnh.
     *
     * @param limitCount Giới hạn số lượng đối tượng cần thu thập
     */
    protected <T> List<T> collectPaginated(
            String initialToken,
            Function<String, String> urlBuilder,
            Function<String, JsonElement> jsonFetcher,
            Function<JsonObject, JsonArray> itemsExtractor,
            Function<JsonObject, T> itemMapper,
            BiFunction<JsonObject, JsonArray, String> nextTokenExtractor,
            long delayMs,
            int limitCount) throws DataAnalyzerException {

        List<T> results = new ArrayList<>();
        String currentToken = initialToken;
        boolean hasMore = true;

        while (results.size() < limitCount && hasMore) {
            // Kiểm tra trạng thái ngắt luồng an toàn để hỗ trợ dừng tiến trình từ giao diện
            // người dùng
            if (Thread.currentThread().isInterrupted()) {
                throw new exception.ProcessInterruptedException("Tiến trình bị gián đoạn");
            }

            // 1. Dựng URL yêu cầu cho trang hiện tại
            String url = urlBuilder.apply(currentToken);

            // 2. Gọi mạng lấy dữ liệu JSON thô
            JsonElement root = jsonFetcher.apply(url);
            if (root == null || !root.isJsonObject()) {
                break;
            }

            JsonObject rootObj = root.getAsJsonObject();

            // Nếu API phản hồi thông báo lỗi từ hệ thống (như hết hạn ngạch hoặc sai key)
            if (rootObj.has("message")) {
                throw new exception.DataStorageException("API Error: " + rootObj.get("message").getAsString());
            }

            // 3. Trích xuất mảng danh sách phần tử từ tài liệu JSON nhận được
            JsonArray items = itemsExtractor.apply(rootObj);
            if (items == null || items.size() == 0) {
                break;
            }

            // 4. Duyệt qua mảng JSON thô để ánh xạ sang đối tượng dữ liệu đích
            for (JsonElement item : items) {
                if (results.size() >= limitCount) {
                    break;
                }
                if (item.isJsonObject()) {
                    T mappedItem = itemMapper.apply(item.getAsJsonObject());
                    if (mappedItem != null) {
                        results.add(mappedItem);
                    }
                }
            }

            // 5. Xác định mã Token phân trang cho vòng lặp kế tiếp
            String nextToken = nextTokenExtractor.apply(rootObj, items);
            if (nextToken == null || nextToken.isEmpty() || nextToken.equals(currentToken)) {
                hasMore = false;
            } else {
                currentToken = nextToken;
            }

            // 6. Trì hoãn luồng lịch sự để bảo vệ hạn ngạch kết nối (Rate Limit)
            politeDelay(delayMs);
        }

        return results;
    }

    /**
     * Gửi yêu cầu HTTP GET tiêu chuẩn để nhận về tài liệu JSON từ một địa chỉ liên
     * kết API.
     *
     * @param url Địa chỉ REST API cần kết nối
     * @return Đối tượng {@link JsonElement} gốc lấy được, hoặc JsonObject rỗng nếu
     *         lỗi kết nối xảy ra
     */
    protected JsonElement fetchJson(String url) {
        try {
            String jsonResponse = Jsoup.connect(url)
                    .userAgent(USER_AGENT_HEADER)
                    .header("Accept", "application/json")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .timeout(20000)
                    .execute()
                    .body();
            return JsonParser.parseString(jsonResponse);
        } catch (Exception e) {
            log(e.getMessage());
        }
        return new JsonObject();
    }

    /**
     * Gửi yêu cầu HTTP GET đi kèm các tiêu đề yêu cầu (Request Headers) phục vụ xác
     * thực bảo mật API.
     *
     * @param url     Địa chỉ REST API cần kết nối
     * @param headers Bản đồ (Map) chứa các cặp tiêu đề Key-Value cần gửi đi kèm yêu
     *                cầu
     * @return Đối tượng {@link JsonElement} gốc lấy được, hoặc JsonObject rỗng nếu
     *         xảy ra lỗi kết nối
     */
    protected JsonElement fetchJsonWithHeaders(String url, Map<String, String> headers) {
        try {
            org.jsoup.Connection conn = Jsoup.connect(url)
                    .userAgent(USER_AGENT_HEADER)
                    .header("Accept", "application/json")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .timeout(20000);

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.header(entry.getKey(), entry.getValue());
                }
            }
            String jsonResponse = conn.execute().body();
            return JsonParser.parseString(jsonResponse);
        } catch (Exception e) {
            log("Error calling API with headers: " + url + " - " + e.getMessage());
        }
        return new JsonObject();
    }

    /**
     * Mã hóa chuỗi truy vấn sang định dạng URL UTF-8 để nhúng an toàn vào liên kết
     * REST API.
     *
     * @param value Chuỗi ký tự thô cần thực hiện mã hóa
     * @return Chuỗi ký tự đã được mã hóa UTF-8 thành công
     * @throws exception.QueryEncodingException Khi xảy ra lỗi hệ thống trong quá
     *                                          trình mã hóa ký tự
     */
    protected String encode(String value) throws exception.QueryEncodingException {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            throw new exception.QueryEncodingException("Lỗi mã hóa tham số: " + value, e);
        }
    }

    /**
     * Thực hiện tạm dừng luồng hoạt động lịch sự giữa các yêu cầu mạng để tránh quá
     * tải máy chủ đích.
     *
     * @param ms Thời gian trì hoãn hoạt động tính bằng mili giây
     * @throws exception.ProcessInterruptedException Khi tiến trình bị người dùng
     *                                               yêu cầu ngắt quãng từ giao diện
     */
    protected void politeDelay(long ms) throws exception.ProcessInterruptedException {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new exception.ProcessInterruptedException("Tiến trình bị gián đoạn", e);
        }
    }

    /**
     * Trích xuất an toàn giá trị chuỗi văn bản (String) từ đối tượng JsonObject.
     *
     * @param obj    Đối tượng JsonObject cần thực hiện bóc tách
     * @param member Tên trường thuộc tính cần lấy giá trị bên trong đối tượng
     * @return Giá trị chuỗi thu được, hoặc chuỗi rỗng nếu thuộc tính không tồn tại
     *         hoặc rỗng
     */
    protected String getStr(JsonObject obj, String member) {
        return (obj != null && obj.has(member) && !obj.get(member).isJsonNull()) ? obj.get(member).getAsString() : "";
    }

    /**
     * Trích xuất an toàn giá trị số nguyên (int) từ đối tượng JsonObject.
     *
     * @param obj    Đối tượng JsonObject cần thực hiện bóc tách
     * @param member Tên trường thuộc tính cần lấy giá trị bên trong đối tượng
     * @return Giá trị số nguyên thu được, hoặc 0 nếu thuộc tính không tồn tại hoặc
     *         lỗi định dạng số
     */
    protected int getInt(JsonObject obj, String member) {
        return (obj != null && obj.has(member) && !obj.get(member).isJsonNull()) ? obj.get(member).getAsInt() : 0;
    }

    /**
     * Trích xuất an toàn giá trị số lớn (long) từ đối tượng JsonObject.
     *
     * @param obj    Đối tượng JsonObject cần thực hiện bóc tách
     * @param member Tên trường thuộc tính cần lấy giá trị bên trong đối tượng
     * @return Giá trị số lớn thu được, hoặc 0L nếu thuộc tính khuyết hoặc rỗng
     */
    protected long getLong(JsonObject obj, String member) {
        return (obj != null && obj.has(member) && !obj.get(member).isJsonNull()) ? obj.get(member).getAsLong() : 0L;
    }

    /**
     * Trích xuất an toàn đối tượng JsonObject con nằm bên trong một đối tượng
     * JsonObject cha.
     *
     * @param parent Đối tượng JsonObject cha chứa dữ liệu
     * @param member Tên trường thuộc tính đối tượng con cần trích xuất
     * @return Đối tượng JsonObject con thu được, hoặc null nếu không tồn tại thuộc
     *         tính tương ứng
     */
    protected JsonObject getObj(JsonObject parent, String member) {
        return (parent != null && parent.has(member) && parent.get(member).isJsonObject())
                ? parent.getAsJsonObject(member)
                : null;
    }

    /**
     * Trích xuất an toàn mảng dữ liệu JsonArray con nằm bên trong một đối tượng
     * JsonObject cha.
     *
     * @param parent Đối tượng JsonObject cha chứa dữ liệu
     * @param member Tên trường thuộc tính mảng con cần trích xuất
     * @return Đối tượng JsonArray con thu được, hoặc null nếu không tồn tại mảng dữ
     *         liệu tương ứng
     */
    protected JsonArray getArray(JsonObject parent, String member) {
        return (parent != null && parent.has(member) && parent.get(member).isJsonArray())
                ? parent.getAsJsonArray(member)
                : null;
    }

    /**
     * Chuyển đổi một chuỗi văn bản ngày tháng dạng ISO 8601 thành đối tượng
     * java.util.Date trong Java.
     *
     * @param dateStr Chuỗi văn bản ngày tháng cần thực hiện chuyển đổi
     * @return Đối tượng {@link Date} tương ứng thu được, hoặc null nếu lỗi định
     *         dạng chuỗi xảy ra
     */
    protected Date parseIsoDate(String dateStr) {
        try {
            return ISO_FORMAT.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Chuyển đổi một đối tượng Ngày tháng (Date) thành dạng chuỗi chữ văn bản chuẩn
     * ISO 8601.
     *
     * @param date Đối tượng Ngày tháng cần chuyển đổi sang chuỗi chữ văn bản
     * @return Chuỗi chữ văn bản định dạng chuẩn ISO 8601, hoặc chuỗi rỗng nếu đối
     *         tượng ngày tháng rỗng
     */
    protected String formatIsoDate(Date date) {
        return date != null ? ISO_FORMAT.format(date) : "";
    }

    /**
     * Lọc bỏ hoàn toàn các mã định dạng thẻ HTML ra khỏi văn bản, chỉ giữ lại phần
     * nội dung chữ thuần túy.
     *
     * @param html Chuỗi văn bản thô ban đầu có chứa mã định dạng HTML
     * @return Chuỗi chữ văn bản sạch đã được loại bỏ mã HTML và loại bỏ khoảng
     *         trắng thừa hai đầu
     */
    protected String stripHtml(String html) {
        if (html == null)
            return "";
        return html.replaceAll("<[^>]*>", "").trim();
    }

    /**
     * Ghi nhận và hiển thị một mục dữ liệu được chấp nhận hợp lệ kèm theo độ trễ
     * mượt giao diện đồ họa.
     *
     * @param itemUrl     Đường dẫn liên kết tuyệt đối của mục dữ liệu thu thập được
     * @param title       Tiêu đề hiển thị của mục dữ liệu thu thập được
     * @param publishedAt Mốc thời gian xuất bản của mục dữ liệu
     * @param current     Chỉ số thứ tự hiện tại của tiến trình thu thập dữ liệu
     * @param total       Tổng số lượng tối đa cần thực hiện thu thập trong tiến
     *                    trình
     */
    protected void logAcceptedItemWithDelay(String itemUrl, String title, Date publishedAt, int current, int total) {
        String pad = new String(new char[this.getClass().getSimpleName().length() + 3]).replace('\0', ' ');
        String timeSuffix = (publishedAt != null) ? " (" + publishedAt + ")" : "";
        log("      -> ✓ ACCEPTED (" + current + "/" + total + "): " + itemUrl + " \n" + pad + "      -> Title: " + title
                + timeSuffix);
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Ghi nhận một mục dữ liệu bị từ chối do mốc thời gian xuất bản nằm ngoài
     * khoảng yêu cầu hệ thống.
     *
     * @param title       Tiêu đề hiển thị của mục dữ liệu bị từ chối
     * @param publishedAt Mốc thời gian xuất bản của mục dữ liệu bị từ chối
     */
    protected void logRejectedItem(String title, Date publishedAt) {
        String timeSuffix = (publishedAt != null) ? " (" + publishedAt + ")" : "";
        log("      -> ✗ REJECTED (Out of Date Range): " + title + timeSuffix);
    }

    /**
     * Phương thức trừu tượng yêu cầu các lớp con tự định nghĩa cấu trúc URL API tìm
     * kiếm.
     *
     * @param encodedQuery Từ khóa tìm kiếm đã được thực hiện mã hóa chuẩn URL UTF-8
     * @param maxCount     Số lượng kết quả thu thập tối đa yêu cầu trên mỗi trang
     *                     gọi API mạng
     * @return Chuỗi liên kết URL API hoàn chỉnh để thực hiện yêu cầu gọi mạng tìm
     *         kiếm dữ liệu thô
     */
    protected abstract String buildSearchUrl(String encodedQuery, int maxCount);
}
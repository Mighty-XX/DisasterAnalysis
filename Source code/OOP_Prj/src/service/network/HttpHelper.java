package service.network;

import exception.InvalidDataException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Lớp tiện ích xử lý các tác vụ gọi mạng (HTTP Requests) của toàn hệ thống.
 * Giúp chuẩn hóa việc gửi dữ liệu và bắt lỗi tập trung tại một nơi.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class HttpHelper {

    // Tái sử dụng một Client duy nhất cho toàn ứng dụng để tối ưu tài nguyên (Best Practice)
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Gửi một yêu cầu HTTP POST có đính kèm JSON.
     *
     * @param url     Địa chỉ API cần gọi
     * @param jsonBody Nội dung JSON cần gửi
     * @param headers Danh sách các Header tùy chỉnh (ví dụ: Authorization). Truyền null nếu không có.
     * @return Chuỗi văn bản phản hồi (Response Body) từ server.
     * @throws InvalidDataException Ném ra lỗi có ý nghĩa nếu mạng rớt hoặc server trả về lỗi.
     */
    public static String sendPost(String url, String jsonBody, Map<String, String> headers) throws InvalidDataException {
        // 1. Dựng khung cơ bản cho Request
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        // 2. Gắn thêm các Header tùy chỉnh (Nếu có)
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }

        // 3. Gửi đi và bắt lỗi tập trung
        try {
            HttpResponse<String> response = CLIENT.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

            // Nếu server trả về lỗi 4xx, 5xx
            if (response.statusCode() != 200) {
                throw new InvalidDataException("Lỗi API (HTTP " + response.statusCode() + "): " + response.body());
            }
            return response.body();

        } catch (java.io.IOException e) {
            throw new InvalidDataException("Lỗi kết nối mạng: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvalidDataException("Yêu cầu qua mạng bị gián đoạn.", e);
        }
    }
}
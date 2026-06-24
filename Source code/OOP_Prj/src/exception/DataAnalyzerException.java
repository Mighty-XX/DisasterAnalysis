package exception;

/**
 * Lớp ngoại lệ cơ sở (Base Exception) cho toàn bộ hệ thống.
 * <p>
 * Lớp này kế thừa từ {@link RuntimeException} (Unchecked Exception),
 * đóng vai trò là ngoại lệ gốc cho các lỗi nghiệp vụ riêng của dự án.
 * Mọi ngoại lệ tùy chỉnh khác (như lỗi mạng, lỗi đọc file) đều nên kế thừa từ lớp này.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class DataAnalyzerException extends RuntimeException {

    /**
     * Khởi tạo một ngoại lệ với thông điệp lỗi cụ thể.
     * <p>
     * Thường được sử dụng khi hệ thống chủ động phát hiện ra một điều kiện không hợp lệ
     * (ví dụ: dữ liệu đầu vào bị rỗng, tham số sai) và cần dừng luồng thực thi ngay lập tức
     * mà không liên quan đến lỗi từ thư viện bên thứ 3.
     * @param message Thông điệp chi tiết mô tả nguyên nhân gây ra lỗi.
     */
    public DataAnalyzerException(String message) {
        super(message);
    }

    /**
     * Khởi tạo một ngoại lệ với thông điệp lỗi và nguyên nhân gốc (Exception Chaining).
     * <p>
     * Thường được sử dụng trong các khối {@code catch} để "gói" ngoại lệ cấp thấp.
     * Việc truyền tham số {@code cause} giúp giữ lại toàn bộ lịch sử lỗi (Stack Trace) để debug sau này.
     * * @param message Thông điệp mô tả ngữ cảnh xảy ra lỗi trong ứng dụng.
     * @param cause   Ngoại lệ gốc rễ (Root cause) gây ra lỗi này.
     */
    public DataAnalyzerException(String message, Throwable cause) {
        super(message, cause);
    }
}
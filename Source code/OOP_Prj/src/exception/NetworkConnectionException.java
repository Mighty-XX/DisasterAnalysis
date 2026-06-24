package exception;

/**
 * Ngoại lệ xử lý các lỗi liên quan đến kết nối mạng.
 * <p>
 * Lớp này kế thừa từ {@link DataAnalyzerException} và được sử dụng khi hệ thống
 * gặp sự cố trong quá trình giao tiếp với các máy chủ bên ngoài
 * (ví dụ: mất mạng internet, quá thời gian chờ (timeout), hoặc máy chủ từ chối kết nối).
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class NetworkConnectionException extends DataAnalyzerException {
    public NetworkConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

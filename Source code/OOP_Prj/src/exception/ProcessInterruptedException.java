package exception;

/**
 * Ngoại lệ xử lý các lỗi liên quan đến việc tiến trình bị gián đoạn.
 * <p>
 * Lớp này kế thừa từ {@link DataAnalyzerException} và được sử dụng khi một luồng (thread)
 * đang thực thi các tác vụ mất thời gian (như thu thập hoặc làm sạch dữ liệu)
 * thì bị ép dừng hoặc ngắt quãng đột ngột.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class ProcessInterruptedException extends DataAnalyzerException {
    public ProcessInterruptedException(String message) {
        super(message);
    }

    public ProcessInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}

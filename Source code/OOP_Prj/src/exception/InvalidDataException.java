package exception;

/**
 * Ngoại lệ xử lý các lỗi liên quan đến tính hợp lệ của dữ liệu.
 * <p>
 * Lớp này kế thừa từ {@link DataAnalyzerException} và được sử dụng chuyên biệt cho các
 * trường hợp lỗi khi kiểm tra (validate) dữ liệu đầu vào. Ví dụ: ngày tháng
 * không hợp lệ, từ khóa bị trống, hoặc lỗi định dạng khi bóc tách văn bản.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class InvalidDataException extends DataAnalyzerException {
    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
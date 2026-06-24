package exception;

/**
 * Ngoại lệ xử lý các lỗi liên quan đến việc mã hóa chuỗi truy vấn (URL Encoding).
 * <p>
 * Lớp này kế thừa từ {@link DataAnalyzerException} và được sử dụng chuyên biệt cho
 * các trường hợp lỗi khi biến đổi từ khóa tìm kiếm (có dấu tiếng Việt, ký tự đặc biệt)
 * sang định dạng URL an toàn (thường là chuẩn UTF-8) để gửi yêu cầu lên các API.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class QueryEncodingException extends DataAnalyzerException {
    public QueryEncodingException(String message, Throwable cause) {
        super(message, cause);
    }
}

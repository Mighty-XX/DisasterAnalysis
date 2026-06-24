package exception;

/**
 * Ngoại lệ xử lý các lỗi liên quan đến quá trình lưu trữ và truy xuất dữ liệu.
 * <p>
 * Lớp này kế thừa từ {@link DataAnalyzerException} và được sử dụng chuyên biệt cho các
 * sự cố về I/O (Input/Output), chẳng hạn như lỗi đọc/ghi file CSV, không tìm thấy file,
 * hoặc các lỗi cấu hình cơ sở dữ liệu.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class DataStorageException extends DataAnalyzerException {
    public DataStorageException(String message) {
        super(message);
    }

    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
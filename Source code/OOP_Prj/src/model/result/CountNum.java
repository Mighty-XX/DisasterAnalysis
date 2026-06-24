package model.result;

/**
 * Lớp CountNum biểu diễn một kết quả phân tích chuyên biệt dưới dạng số lượng đếm (số nguyên).
 * Lớp này kế thừa (extends) từ AnalysisResult. Điểm đặc biệt của CountNum là nó tự động
 * cố định đơn vị đo lường (unit) là "Count" và chuyển đổi kiểu dữ liệu số thực (double)
 * của lớp cha thành số nguyên (int) để phù hợp với nghiệp vụ đếm thực tế.
 *
 * @author Nguyễn Thị Thanh Trúc - 202417052
 */
public class CountNum extends AnalysisResult {

    // Constructor
    // Hàm khởi tạo nhận nhãn tùy chỉnh và số lượng đếm
    // Sử dụng từ khóa 'super' để gọi constructor của lớp cha
    public CountNum(String label, int count) {
        super(label, count, "Count");
    }

    // Hàm khởi tạo nhãn mặc định "Total"
    public CountNum(int count) {
        super("Total", count, "Count");
    }

    /**
     * Lấy số lượng đếm dưới dạng số nguyên (int).
     * Do thuộc tính 'value' ở lớp cha lưu trữ kiểu 'double' (để phục vụ tính toán số thực chung),
     * phương thức này thực hiện ép kiểu tường minh từ double về int để trả về số lượng nguyên vẹn.
     */
    public int getCount() {
        return (int) this.getValue();
    }

    /**
     * Thiết lập hoặc cập nhật số lượng đếm mới.
     * Gọi lại phương thức setValue() của lớp cha để cập nhật thuộc tính value gốc.
     */
    public void setCount(int count) {
        this.setValue(count);
    }

    /**
     * Ghi đè phương thức toString() kế thừa từ lớp Object.
     * Trả về một chuỗi ký tự mô tả ngắn gọn và trực quan về kết quả đếm.
     * Phương thức này không chỉ hỗ trợ việc hiển thị thông tin ra màn hình/log, mà còn là công cụ hữu ích
     * giúp lập trình viên dễ dàng theo dõi trạng thái đối tượng và gỡ lỗi (debugging) trong quá trình phát triển.
     */
    @Override
    public String toString() {
        return getLabel() + ": " + getCount();
    }
}
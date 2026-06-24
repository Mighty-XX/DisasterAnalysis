package model.result;

/**
 * Lớp AnalysisResult đại diện cho kết quả chung của một tiến trình phân tích dữ liệu.
 * Lớp này dùng để đóng gói dữ liệu đầu ra của các chỉ số phân tích (như tỷ lệ, số lượng phần trăm) thành một cấu trúc chuẩn hóa
 * gồm: nhãn mô tả, giá trị định lượng và đơn vị đo lường.
 *
 * @author Nguyễn Thị Thanh Trúc - 202417052
 */
public class AnalysisResult {

    // Nhãn hoặc tiêu đề mô tả kết quả phân tích
    private String label;

    // Giá trị số của kết quả phân tích.
    // Sử dụng phạm vi truy cập 'protected' để cho phép các lớp con kế thừa từ AnalysisResult
    // có thể trực tiếp truy cập và thao tác trên biến này mà không cần thông qua getter/setter.
    protected double value;

    // Đơn vị đo lường tương ứng của kết quả (ví dụ: "%", "bài viết", "lượt")
    private String unit;

    // Constructor
    // Hàm khởi tạo không tham số
    public AnalysisResult() {
    }

    // Hàm khởi tạo đầy đủ tham số để thiết lập nhanh giá trị cho kết quả phân tích
    public AnalysisResult(String label, double value, String unit) {
        this.label = label;
        this.value = value;
        this.unit = unit;
    }

    // Getters & Setters
    // Lấy nhãn mô tả của kết quả phân tích
    public String getLabel() {
        return label;
    }

    // Thiết lập hoặc thay đổi nhãn mô tả của kết quả
    public void setLabel(String label) {
        this.label = label;
    }

    // Lấy giá trị định lượng của kết quả
    public double getValue() {
        return value;
    }

    // Thiết lập hoặc thay đổi giá trị định lượng cho kết quả
    public void setValue(double value) {
        this.value = value;
    }

    // Lấy đơn vị đo lường của kết quả
    public String getUnit() {
        return unit;
    }

    // Thiết lập hoặc thay đổi đơn vị đo lường cho kết quả
    public void setUnit(String unit) {
        this.unit = unit;
    }
}
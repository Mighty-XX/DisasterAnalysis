package model.result;

/**
 * Lớp SentimentResult đại diện cho kết quả phân tích sắc thái cảm xúc (Sentiment Analysis) của dữ liệu.
 * Lớp này kế thừa (extends) từ AnalysisResult. Nó theo dõi chi tiết số lượng phản hồi tích cực (positive)
 * và tiêu cực (negative), đồng thời tự động cập nhật và đồng bộ giá trị tổng (value) ở lớp cha.
 *
 * @author Nguyễn Thị Thanh Trúc - 202417052
 */
public class SentimentResult extends AnalysisResult {

    // Số lượng thực thể (bài viết hoặc bình luận) mang sắc thái tích cực
    private int positiveCount;

    // Số lượng thực thể (bài viết hoặc bình luận) mang sắc thái tiêu cực
    private int negativeCount;

    /**
     * Hàm khởi tạo không tham số
     * Thực hiện gọi ngược lên hàm khởi tạo mặc định của lớp cha AnalysisResult bằng từ khóa 'super()'.
     */
    public SentimentResult() {
        super();
    }

    /**
     * Hàm khởi tạo đầy đủ tham số để thiết lập kết quả phân tích sắc thái.
     * Tự động tính tổng số lượng (tích cực + tiêu cực) để làm giá trị (value) tổng thể của lớp cha
     * và đặt đơn vị đo lường mặc định là "Posts".
     *
     * @param dateLabel     Nhãn thời gian hoặc tiêu đề của đợt phân tích (ví dụ: ngày cụ thể)
     * @param positiveCount Số lượng phản hồi tích cực
     * @param negativeCount Số lượng phản hồi tiêu cực
     */
    public SentimentResult(String dateLabel, int positiveCount, int negativeCount) {
        // Gọi hàm khởi tạo của lớp cha, ép kiểu tổng số lượng sang double và đặt đơn vị là "Posts"
        super(dateLabel, (double) positiveCount + negativeCount, "Posts");
        this.positiveCount = positiveCount;
        this.negativeCount = negativeCount;
    }

    // Lấy số lượng phản hồi tích cực
    public int getPositiveCount() {
        return positiveCount;
    }

    // Cập nhật số lượng phản hồi tích cực
    // Sau khi cập nhật, tự động gọi phương thức đồng bộ để tính lại tổng số lượng bài viết ở lớp cha
    public void setPositiveCount(int positiveCount) {
        this.positiveCount = positiveCount;
        this.updateTotalValue();
    }

    // Lấy số lượng phản hồi tiêu cực
    public int getNegativeCount() {
        return negativeCount;
    }

    // Cập nhật số lượng phản hồi tiêu cực
    // Sau khi cập nhật, tự động gọi phương thức đồng bộ để tính lại tổng số lượng bài viết ở lớp cha
    public void setNegativeCount(int negativeCount) {
        this.negativeCount = negativeCount;
        this.updateTotalValue();
    }

    /**
     * Phương thức phụ trợ tính toán lại và tự động cập nhật thuộc tính 'value'.
     * Việc đặt phạm vi truy cập là 'private' thể hiện tính Đóng gói (Encapsulation), ngăn chặn các đối tượng
     * bên ngoài tự ý can thiệp vào tiến trình đồng bộ dữ liệu nội bộ của lớp này.
     */
    private void updateTotalValue() {
        this.setValue((double) this.positiveCount + this.negativeCount);
    }

    /**
     * Ghi đè phương thức toString() để hiển thị chi tiết kết quả phân tích sắc thái.
     * Trả về chuỗi ký tự định dạng cân đối, giúp hiển thị báo cáo trực quan và hỗ trợ đắc lực
     * cho việc theo dõi luồng dữ liệu cũng như gỡ lỗi (debugging) nhanh chóng khi cần kiểm tra trạng thái đối tượng.
     */
    @Override
    public String toString() {
        return String.format("Date: %s | Total: %.0f | Pos: %d | Neg: %d",
                getLabel(), getValue(), positiveCount, negativeCount);
    }
}
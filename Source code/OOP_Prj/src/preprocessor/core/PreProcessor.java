package preprocessor.core;

/**
 * Giao diện (Interface) cơ sở cho tất cả các bộ tiền xử lý văn bản.
 * <p>
 * Dựa trên mẫu thiết kế Strategy (Chiến lược), giao diện này định nghĩa một bộ khung chung
 * để làm sạch hoặc biến đổi dữ liệu chữ (text). Các lớp triển khai cụ thể
 * (như LowerCaseProcessor, StopWordsRemover...) sẽ kế thừa giao diện này
 * để thực hiện các thao tác xử lý văn bản chuyên biệt trước khi đưa vào phân tích.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public interface PreProcessor {

    /**
     * Xử lý và biến đổi chuỗi văn bản đầu vào.
     * @param content Chuỗi văn bản thô (raw) cần được xử lý (ví dụ: nội dung bài báo, bình luận).
     * @return Chuỗi văn bản mới sau khi đã được làm sạch, cắt gọt hoặc biến đổi.
     */
    String process(String content);
}
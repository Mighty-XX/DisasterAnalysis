package service.ai.core;

import exception.InvalidDataException;

/**
 * Giao diện (Interface) tiêu chuẩn cho các dịch vụ AI trong hệ thống.
 * <p>
 * Định nghĩa một bộ khung chung để ứng dụng giao tiếp với các mô hình ngôn ngữ lớn (LLM).
 * Nhờ thiết kế loose coupling này, ta có thể dễ dàng chuyển đổi hoặc cắm thêm
 * các nhà cung cấp AI khác (như Gemini, Groq, GPT,..) vào hệ thống.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public interface AIClient {

    /**
     * Gửi yêu cầu (Prompt) lên máy chủ AI và nhận về bản nhận xét/tóm tắt số liệu.
     *
     * @param promptText Câu lệnh hoàn chỉnh đã được nhào nặn từ PromptBuilder.
     * @return Chuỗi văn bản chứa lời nhận xét, đánh giá chuyên sâu do AI sinh ra.
     * @throws InvalidDataException Ném ra nếu API Key sai, rớt mạng, hoặc AI trả về lỗi.
     */
    String getSummaryFromAI(String promptText) throws InvalidDataException;

    /**
     * Truy xuất tên của mô hình AI đang được sử dụng.
     *
     * @return Tên phiên bản model AI.
     */
    String getModelName();

    /**
     * Kiểm tra trạng thái của dịch vụ AI (API Key, Mạng).
     *
     * @return {@code true} nếu AI client sẵn sàng nhận lệnh, ngược lại trả về {@code false}.
     */
    boolean isAvailable();
}
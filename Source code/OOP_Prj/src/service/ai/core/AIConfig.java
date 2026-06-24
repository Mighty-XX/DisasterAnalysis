package service.ai.core;

import service.ai.GeminiAIService;
import service.ai.GroqAIService;

/**
 * Lớp cấu hình (config) quản lý và điều phối các dịch vụ AI trong hệ thống.
 * <p>
 * Lớp này hoạt động như một "trạm trung chuyển", giúp tách biệt hoàn toàn
 * tầng giao diện (UI) khỏi các lớp triển khai AI cụ thể. Nhờ thiết kế này,
 * giao diện chỉ cần xin cấp một AIClient thông qua tên gọi mà không cần
 * phải biết cụ thể lớp đó như thế nào bên trong.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class AIConfig {

    /**
     * Khởi tạo và trả về đối tượng AIClient phù hợp dựa trên lựa chọn từ người dùng.
     *
     * @param aiName Tên dịch vụ AI được chọn từ giao diện (VD: "Groq", "Google Gemini").
     * @return Đối tượng implements AIClient tương ứng để thực hiện việc gọi API.
     */
    public static AIClient getAIClient(String aiName) {
        // Kiểm tra an toàn (tránh NullPointerException) và điều hướng sang dịch vụ Groq
        if (aiName != null && aiName.contains("Groq")) {
            return new GroqAIService();
        }

        // Kế hoạch dự phòng (Fallback): Mặc định luôn trả về Gemini
        // nếu tham số truyền vào bị null, rỗng, hoặc người dùng chọn một AI chưa được hỗ trợ.
        return new GeminiAIService();
    }

    /**
     * Trả về danh sách model AI hiện đang được hệ thống hỗ trợ.
     * <p>
     * Hàm tiện ích này được thiết kế để nạp trực tiếp danh sách vào ComboBox trên UI.
     * Điều này giúp giao diện tự động đồng bộ (Dynamic Update) mỗi khi bạn
     * lập trình thêm hoặc loại bỏ một con AI mới trong mảng này.
     *
     * @return Mảng chuỗi chứa tên hiển thị của các dịch vụ AI.
     */
    public static String[] getSupportedAIs() {
        return new String[]{
                "Google Gemini",
                "Groq" // Tên sẽ hiển thị trên Menu thả xuống của ChartView
        };
    }
}
package service.ai;

import com.google.gson.*;
import exception.InvalidDataException;
import service.ai.core.AIClient;
import service.network.HttpHelper;

import java.util.Map;

/**
 * Dịch vụ tóm tắt dữ liệu sử dụng Groq API (Siêu tốc độ).
 * <p>
 * Lớp này triển khai giao diện {@link AIClient}, sử dụng chuẩn API tương thích
 * 100% với OpenAI nhưng trỏ về máy chủ Groq để tận dụng tốc độ nội suy tức thì
 * của các mô hình Llama mã nguồn mở.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class GroqAIService implements AIClient {

    // ===== CẤU HÌNH API GROQ =====
    private static final String API_KEY = "YOUR_GROQ_API_KEY_HERE";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummaryFromAI(String promptText) throws InvalidDataException {

        // 1. Tạo JSON Request tuân thủ cấu trúc chuẩn của OpenAI
        JsonObject root = new JsonObject();
        // Cấu hình mô hình Llama 3.1 8B thế hệ mới nhất của Groq
        root.addProperty("model", "llama-3.1-8b-instant");

        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", promptText);

        messages.add(userMessage);
        root.add("messages", messages);

        // 2. NHỜ CHUYÊN GIA GIAO HÀNG (Groq yêu cầu truyền API Key qua Header)
        Map<String, String> authHeader = Map.of("Authorization", "Bearer " + API_KEY);
        String responseBody = HttpHelper.sendPost(API_URL, root.toString(), authHeader);

        // 3. Bóc tách JSON chuẩn OpenAI để lấy văn bản phản hồi
        JsonObject resJson = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray choices = resJson.getAsJsonArray("choices");

        // Kiểm tra an toàn
        if (choices == null || choices.size() == 0) {
            return "⚠ Groq AI không trả về kết quả.";
        }

        // Truy xuất: choices[0] -> message -> content
        return choices.get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString().trim();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getModelName() {
        return "Llama-3.1-8B (via Groq)";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAvailable() {
        return API_KEY != null && !API_KEY.isEmpty();
    }
}
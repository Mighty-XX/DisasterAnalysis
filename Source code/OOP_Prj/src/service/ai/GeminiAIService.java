package service.ai;

import com.google.gson.*;
import exception.InvalidDataException;
import service.ai.core.AIClient;
import service.network.HttpHelper;

/**
 * Dịch vụ tóm tắt dữ liệu sử dụng Google Gemini API.
 * <p>
 * Lớp này triển khai giao diện {@link AIClient}, đảm nhiệm việc đóng gói dữ liệu
 * theo cấu trúc JSON phân cấp đặc thù của Google và bóc tách kết quả trả về.
 * Logic kết nối mạng phức tạp đã được ủy quyền hoàn toàn cho {@link HttpHelper}.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class GeminiAIService implements AIClient {

    // ===== CẤU HÌNH API =====
    private static final String API_KEY = "YOUR_GEMINI_API_KEY_HERE";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                                          + API_KEY;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummaryFromAI(String promptText) throws InvalidDataException {

        // 1. Tạo JSON body theo cấu trúc lồng nhau (nested) đặc thù của Google
        JsonObject root = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();

        part.addProperty("text", promptText);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        root.add("contents", contents);

        // 2. NHỜ CHUYÊN GIA GIAO HÀNG (Gemini xác thực qua URL nên không cần Header phụ)
        String responseBody = HttpHelper.sendPost(API_URL, root.toString(), null);

        // 3. Bóc tách JSON kiểu Google để lấy văn bản tóm tắt
        JsonObject resJson = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray candidates = resJson.getAsJsonArray("candidates");

        // Kiểm tra an toàn trước khi truy xuất phần tử
        if (candidates == null || candidates.size() == 0) {
            return "⚠ AI không trả về kết quả.";
        }

        // Truy xuất sâu: candidates[0] -> content -> parts[0] -> text
        return candidates.get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString().trim();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getModelName() {
        return "gemini-2.5-flash";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAvailable() {
        return API_KEY != null && !API_KEY.isEmpty();
    }
}
package preprocessor.processors;

import preprocessor.core.PreProcessor;

/**
 * Bộ tiền xử lý chuyển đổi văn bản sang chữ thường (Lowercase).
 * <p>
 * Lớp này giúp đồng nhất định dạng chữ, tránh việc thuật toán đếm từ khóa bị sót
 * do phân biệt chữ hoa/chữ thường (ví dụ: "Bão" và "bão" sẽ được coi là giống nhau).
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class LowerCaseProcessor implements PreProcessor {

    /**
     * Thực thi việc chuyển đổi chuỗi sang in thường.
     * @param content Chuỗi văn bản thô gốc.
     * @return Chuỗi văn bản đã viết thường toàn bộ, hoặc nguyên bản nếu chuỗi rỗng.
     */
    @Override
    public String process(String content) {

        // Kiểm tra an toàn: Tránh lỗi nếu không có dữ liệu
        if (content == null || content.isEmpty()) {
            return content;
        }

        // Dùng hàm mặc định của Java để chuyển đổi
        return content.toLowerCase();
    }
}
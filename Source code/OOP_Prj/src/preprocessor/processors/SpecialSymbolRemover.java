package preprocessor.processors;

import preprocessor.core.PreProcessor;

/**
 * Bộ tiền xử lý loại bỏ các ký tự đặc biệt, biểu tượng cảm xúc (emoji) và rác văn bản.
 * <p>
 * Lớp này sử dụng Biểu thức chính quy (Regex) để lọc văn bản, chỉ giữ lại chữ cái
 * (bao gồm tiếng Việt có dấu), chữ số, dấu câu cơ bản và khoảng trắng.
 * Rất hữu ích để dọn dẹp dữ liệu cào từ mạng xã hội (vốn chứa nhiều emoji).
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class SpecialSymbolRemover implements PreProcessor {

    /**
     * Lọc và loại bỏ các ký tự không mong muốn khỏi chuỗi đầu vào.
     * @param content Chuỗi văn bản thô.
     * @return Chuỗi văn bản chỉ còn chữ, số, khoảng trắng và dấu câu.
     */
    @Override
    public String process(String content) {
        if (content == null) {
            return null;
        }

        // Thay thế các ký tự không hợp lệ (emoji, biểu tượng lạ) bằng DẤU CÁCH
        // để tránh tình trạng các từ bị dính chặt vào nhau sau khi xóa (vd: "cầu🙏mong" -> cầu mong")
        String cleaned = content.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", " ");

        // Chuẩn hóa dấu câu: Gộp các dấu câu lặp lại liên tiếp (vd: .... thành ., "" thành ")
        cleaned = cleaned.replaceAll("\\.{2,}", ".");
        cleaned = cleaned.replaceAll("\"{2,}", "\"");
        cleaned = cleaned.replaceAll("!{2,}", "!");
        cleaned = cleaned.replaceAll("\\?{2,}", "?");
        cleaned = cleaned.replaceAll(",{2,}", ",");

        // Gộp các khoảng trắng thừa thành 1 khoảng trắng duy nhất và cắt khoảng trắng 2 đầu
        return cleaned.replaceAll("\\s+", " ").trim();
    }
}
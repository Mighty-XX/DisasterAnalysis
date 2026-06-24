package analysis.core;

import model.result.AnalysisResult;
import model.social.Post;
import model.social.Comment;
import java.util.*;
import java.util.regex.*;

/**
 * Lớp trừu tượng (Abstract class) cung cấp bộ công cụ xử lý ngôn ngữ tự nhiên cơ bản.
 * <p>
 * Lớp này gom các logic dùng chung (như quét từ khóa, xử lý câu phủ định)
 * nhằm tránh lặp lại code (DRY - Don't Repeat Yourself) ở các module phân tích con.
 *
 * @author Vũ Lê Dũng - 202416900
 */
public abstract class BaseKeywordAnalysis<T extends AnalysisResult>
        implements AnalysisTask<T> {

    // Regex tìm các từ phủ định (không, chưa, chẳng) đứng trước từ khóa
    private static final Pattern NEGATION_PATTERN =
            Pattern.compile("(?i)(không|chưa|chẳng|đừng)\\s*$");

    @Override
    public void execute(Post p, Map<String, T> result) {
        /*
         * Tối ưu NLP: Ghép nội dung đã làm sạch (cleanContent) và nội dung gốc (rawContent).
         * Lý do: Bước lọc StopWords trước đó có thể vô tình cắt đứt các cụ từ ghép có nghĩa
         * (VD: "kịp thời" bị mất từ "thời"). Ghép 2 chuỗi giúp bù trừ và tăng độ chính xác.
         */
        String cleanPost = p.getCleanContent() != null
                ? p.getCleanContent().toLowerCase() : "";
        String rawPost = p.getRawContent() != null
                ? p.getRawContent().toLowerCase() : "";
        String contentToAnalyze = cleanPost + " " + rawPost;

        // Kích hoạt hàm xử lý do các lớp con định nghĩa
        processContent(contentToAnalyze, p.getTimestamp(), result);

        // Duyệt và xử lý đệ quy cho cả dữ liệu của các bình luận (Comment)
        if (p.getComments() != null) {
            for (Comment c : p.getComments()) {
                String cleanCmt = c.getCleanContent() != null
                        ? c.getCleanContent().toLowerCase() : "";
                String rawCmt = c.getRawContent() != null
                        ? c.getRawContent().toLowerCase() : "";
                String cmtContent = cleanCmt + " " + rawCmt;

                processContent(cmtContent, c.getTimestamp(), result);
            }
        }
    }

    /**
     * Hàm trừu tượng ép buộc các lớp kế thừa (Sub-class) phải tự định nghĩa logic
     * tính điểm và cập nhật kết quả theo đặc thù riêng của thuật toán đó.
     */
    protected abstract void processContent(String lowerContent,
                                           Date timestamp, Map<String, T> result);


    /**
     * Phương thức đánh giá đối sánh từ khóa thông minh bằng Regex.
     * Sử dụng Unicode Lookaround để đảm bảo từ khóa đứng độc lập (Word Boundary),
     * tránh nhận diện nhầm từ (VD: tìm "cô" nhưng lại khớp vào "công").
     *
     * @return 1 (Khớp hoàn toàn), -1 (bị đảo nghĩa do từ phủ định), 0 (Không tìm thấy)
     */
    protected int checkKeyword(String lowerContent, String keyword) {
        String regex = "(?Ui)(?<![\\p{L}\\p{N}])" + Pattern.quote(keyword)
                + "(?![\\p{L}\\p{N}])";
        Matcher m = Pattern.compile(regex).matcher(lowerContent);

        if (m.find()) {
            // Cắt 20 ký tự ngay trước từ khóa vừa tìm thấy để quét từ phủ định
            int start = Math.max(0, m.start() - 20);
            String beforeMatch = lowerContent.substring(start, m.start());

            if (NEGATION_PATTERN.matcher(beforeMatch).find()) {
                return -1;// Phát hiện phủ định (VD: "không an toàn")
            }
            return 1;// Khớp chuẩn
        }
        return 0;// Không khớp
    }

    /**
     * Hàm tiện ích kiểm tra nhanh xem văn bản có chứa bất kỳ từ khóa nào
     * trong một danh sách cho trước hay không.
     */
    protected boolean containsAny(String lowerContent, List<String> keywords) {
        for (String k : keywords) {
            if (checkKeyword(lowerContent, k) != 0) {
                return true;
            }
        }
        return false;
    }
}
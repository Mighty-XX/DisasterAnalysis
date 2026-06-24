package analysis.implementation;

import java.util.*;
import analysis.DictionaryConfig;
import analysis.core.BaseKeywordAnalysis;
import model.result.SentimentResult;

/**
 * Lớp phân tích Mức độ hài lòng / Cảm xúc theo từng hạng mục cứu trợ.
 * <p>
 * Phân tích độ hiệu quả của công tác cứu trợ bằng cách đếm xem trong một nhóm
 * hỗ trợ nhất định (VD: Lương thực, Y tế), người dân đang sử dụng nhiều từ ngữ
 * Tích cực (cảm ơn, an toàn) hay Tiêu cực (đói, thiếu thốn).
 *
 * @author Vũ Lê Dũng - 202416900
 */
public class SatisfactionAnalysis extends BaseKeywordAnalysis<SentimentResult> {

    private final Map<String, List<String>> itemKeywords =
            DictionaryConfig.getItemKeywords();

    @Override
    protected void processContent(String lowerContent, Date timestamp,
                                  Map<String, SentimentResult> result) {
        if (lowerContent.isEmpty()) {
            return;
        }

        // Bước 1: Xác định xem bài viết đang nói về nhu cầu cứu trợ nào
        String category = classifyCategory(lowerContent);

        if (category == null) {
            return;// Bỏ qua nếu bài viết không đề cập đến công tác cứu trợ
        }

        SentimentResult sentiment = result.getOrDefault(category,
                new SentimentResult());
        int posFound = 0;
        int negFound = 0;

        // Bước 2: Quét từ khóa Tích cực và xử lý logic Đảo nghĩa (Negation handling)
        for (String w : DictionaryConfig.POSITIVE_WORDS) {
            int match = checkKeyword(lowerContent, w);

            if (match == 1) {
                posFound++;// Khớp chuẩn (VD: "an toàn")
            } else if (match == -1) {
                negFound++;// Bị phủ định (VD: "không an toàn" -> Tính là tiêu cực)
            }
        }

        // Bước 3: Quét từ khóa Tiêu cực với logic tương tự
        for (String w : DictionaryConfig.NEGATIVE_WORDS) {
            int match = checkKeyword(lowerContent, w);

            if (match == 1) {
                negFound++;
            } else if (match == -1) {
                posFound++;// VD: "không đói" -> Tính là tích cực
            }
        }

        // Bước 4: Lưu kết quả nếu thực sự câu văn có bộc lộ cảm xúc
        if (posFound > 0 || negFound > 0) {
            sentiment.setPositiveCount(sentiment.getPositiveCount() + posFound);
            sentiment.setNegativeCount(sentiment.getNegativeCount() + negFound);
            result.put(category, sentiment);
        }
    }

    /**
     * Dò tìm hạng mục cứu trợ được đề cập trong câu văn (Dựa trên Map itemKeywords).
     */
    private String classifyCategory(String lowerContent) {
        for (Map.Entry<String, List<String>> entry : itemKeywords.entrySet()) {
            if (containsAny(lowerContent, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}
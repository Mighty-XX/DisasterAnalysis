package analysis.implementation;

import java.time.ZoneId;
import java.util.*;
import analysis.DictionaryConfig;
import analysis.core.BaseKeywordAnalysis;
import model.result.SentimentResult;

/**
 * Lớp phân tích Chuỗi thời gian (Time-series Analysis) đối với thái độ xã hội.
 * <p>
 * Nhóm tất cả các bài viết theo từng Ngày (Date). Thuật toán sẽ tính toán
 * tổng hòa cảm xúc (Positive vs Negative) của một bài viết, sau đó quyết định xem
 * bài viết đó mang xu hướng nào và cộng dồn vào thống kê của ngày tương ứng.
 * Phục vụ cho việc theo dõi biểu đồ đường (Line chart) diễn biến tâm lý mạng xã hội.
 *
 * @author Vũ Lê Dũng - 202416900
 */
public class SentimentOverTimeAnalysis
        extends BaseKeywordAnalysis<SentimentResult> {

    /**
     * Định nghĩa kiểu liệt kê (Enum) nội bộ để kiểm soát chặt chẽ 4 trạng thái cảm xúc,
     * thay vì dùng String hardcode dễ gây lỗi chính tả.
     */
    private enum SentimentLabel {
        POSITIVE, NEGATIVE, NEUTRAL, UNKNOWN
    }

    @Override
    protected void processContent(String lowerContent, Date timestamp,
                                  Map<String, SentimentResult> result) {
        if (lowerContent.isEmpty() || timestamp == null) {
            return;
        }

        // Chuyển đổi đối tượng Date sang chuỗi định dạng "yyyy-MM-dd" để làm Key phân nhóm
        // Sử dụng Java 8 Time API (toInstant, ZoneId) để xử lý múi giờ chính xác
        String dateKey = timestamp.toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate().toString();

        // Lấy nhãn cảm xúc chủ đạo của bài viết
        SentimentLabel label = classifySentiment(lowerContent);

        // Lọc nhiễu: Chỉ quan tâm đến các bài viết có xu hướng rõ ràng (Tích cực / Tiêu cực)
        if (label == SentimentLabel.UNKNOWN || label == SentimentLabel.NEUTRAL) {
            return;
        }

        SentimentResult sr = result.getOrDefault(dateKey,
                new SentimentResult());

        if (label == SentimentLabel.POSITIVE) {
            sr.setPositiveCount(sr.getPositiveCount() + 1);
        } else {
            sr.setNegativeCount(sr.getNegativeCount() + 1);
        }
        result.put(dateKey, sr);
    }

    /**
     * Tính toán xu hướng chung của bài viết bằng cách so sánh tổng lượng từ
     * tích cực và tiêu cực xuất hiện.
     */
    private SentimentLabel classifySentiment(String lowerContent) {
        int pos = 0;
        int neg = 0;

        // Quét và tính điểm cho từ khóa tích cực
        for (String w : DictionaryConfig.POSITIVE_WORDS) {
            int match = checkKeyword(lowerContent, w);

            if (match == 1) {
                pos++;
            } else if (match == -1) {
                neg++;
            }
        }

        // Quét và tính điểm cho từ khóa tiêu cực
        for (String w : DictionaryConfig.NEGATIVE_WORDS) {
            int match = checkKeyword(lowerContent, w);

            if (match == 1) {
                neg++;
            } else if (match == -1) {
                pos++;
            }
        }

        // Áp dụng thuật toán đa số thắng (Majority Voting)
        if (pos > neg) {
            return SentimentLabel.POSITIVE;
        }
        if (neg > pos) {
            return SentimentLabel.NEGATIVE;
        }

        // Nếu hòa (pos == neg), phân loại là trung lập hoặc không xác định
        return (pos == 0 && neg == 0)
                ? SentimentLabel.UNKNOWN : SentimentLabel.NEUTRAL;
    }
}
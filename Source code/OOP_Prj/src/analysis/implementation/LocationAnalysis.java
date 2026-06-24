package analysis.implementation;

import analysis.DictionaryConfig;
import analysis.core.BaseKeywordAnalysis;
import model.result.CountNum;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Lớp phân tích và trích xuất thông tin địa điểm (Hotspot Analysis).
 * <p>
 * Thuật toán này không chỉ tìm tên tỉnh thành một cách mù quáng, mà áp dụng kỹ thuật
 * lọc theo ngữ cảnh (Contextual Filtering): Một địa điểm chỉ được ghi nhận là "Điểm nóng"
 * NẾU câu văn đó có đi kèm với các từ khóa về thiệt hại hoặc tiêu cực. Điều này
 * giúp loại bỏ dữ liệu nhiễu (Ví dụ: bài đăng "Tôi đang ở Hà Nội an toàn" sẽ không bị tính).
 *
 * @author Vũ Lê Dũng - 202416900
 */
public class LocationAnalysis extends BaseKeywordAnalysis<CountNum> {

    // Nạp sẵn từ điển địa danh vào bộ nhớ để tránh khởi tạo lại nhiều lần trong vòng lặp
    private final Map<String, List<String>> locationDictionary =
            DictionaryConfig.getLocationKeywords();

    @Override
    protected void processContent(String lowerContent, Date timestamp,
                                  Map<String, CountNum> result) {
        if (lowerContent.isEmpty()) {
            return;
        }

        // Bước 1: Kiểm tra chéo xem câu văn có ngữ cảnh thảm họa/thiệt hại hay không
        boolean hasDamageContext =
                containsAny(lowerContent, DictionaryConfig.AFFECTED_PEOPLE)
                        || containsAny(lowerContent, DictionaryConfig.DAMAGED_INFRA)
                        || containsAny(lowerContent, DictionaryConfig.HOUSES_DAMAGED)
                        || containsAny(lowerContent, DictionaryConfig.NEGATIVE_WORDS);

        // Nếu không có ngữ cảnh thiệt hại -> Dừng xử lý câu này (Lọc nhiễu)
        if (!hasDamageContext) {
            return;
        }

        // Bước 2: Quét qua từ điển địa danh để định vị khu vực được nhắc đến
        for (Map.Entry<String, List<String>> entry :
                locationDictionary.entrySet()) {
            if (containsAny(lowerContent, entry.getValue())) {
                String province = entry.getKey();
                CountNum counter = result.getOrDefault(province,
                        new CountNum(0));

                counter.setCount(counter.getCount() + 1);
                result.put(province, counter);
            }
        }
    }
}
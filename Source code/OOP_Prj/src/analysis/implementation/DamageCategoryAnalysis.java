package analysis.implementation;

import java.util.*;
import analysis.DictionaryConfig;
import analysis.core.BaseKeywordAnalysis;
import model.result.CountNum;

/**
 * Lớp phân tích và thống kê phân loại thiệt hại (Damage Category).
 * <p>
 * Lớp này kế thừa bộ công cụ quét từ khóa từ {@link BaseKeywordAnalysis}
 * để duyệt qua nội dung văn bản và phân loại bài viết vào các nhóm thiệt hại cụ thể
 * (Con người, Hạ tầng, Nhà cửa, Tài sản, Kinh tế). Kết quả sẽ được sử dụng để
 * vẽ các biểu đồ phân bổ (ví dụ: Pie Chart).
 *
 * @author Vũ Lê Dũng - 202416900
 */
public class DamageCategoryAnalysis extends BaseKeywordAnalysis<CountNum> {

    /**
     * Ghi đè phương thức lõi để thực thi logic phân tích riêng của lớp này.
     * * @param lower     Nội dung văn bản đã được chuyển thành chữ thường và làm sạch.
     * @param timestamp Thời gian đăng bài (không dùng trong phân tích này nhưng bắt buộc theo Interface).
     * @param result    Bản đồ (Map) lưu trữ kết quả thống kê dạng [Tên danh mục -> Số lượng].
     */
    @Override
    protected void processContent(String lower, Date timestamp,
                                  Map<String, CountNum> result) {
        // Nhận diện xem câu văn này đang nhắc đến loại hình thiệt hại nào
        String category = classifyContent(lower);

        // Cập nhật kết quả vào bộ nhớ dùng chung
        updateMap(result, category);
    }

    /**
     * Hàm tiện ích giúp cập nhật số đếm (Counter) vào Map một cách an toàn.
     */
    private void updateMap(Map<String, CountNum> result, String category) {
        // Nếu câu văn không chứa từ khóa thiệt hại nào, bỏ qua để tối ưu hiệu suất
        if (category == null) {
            return;
        }

        // Sử dụng getOrDefault để tránh lỗi NullPointerException khi danh mục mới xuất hiện lần đầu
        CountNum b2 = result.getOrDefault(category, new CountNum(0));

        // Tăng bộ đếm lên 1 và lưu ngược lại vào Map
        b2.setCount(b2.getCount() + 1);
        result.put(category, b2);
    }

    /**
     * Thuật toán phân loại theo mức độ ưu tiên.
     * Hệ thống quét lần lượt theo thứ tự từ thiệt hại nghiêm trọng nhất (Con người)
     * xuống các thiệt hại về tài sản/kinh tế. Nếu bài viết chứa nhiều loại,
     * nó sẽ ưu tiên nhãn được phát hiện đầu tiên.
     * * @return Tên danh mục thiệt hại (String), hoặc null nếu không tìm thấy.
     */
    private String classifyContent(String lower) {
        if (lower == null || lower.trim().isEmpty()) {
            return null;
        }

        if (containsAny(lower, DictionaryConfig.AFFECTED_PEOPLE)) {
            return "Con người (People)";
        }
        if (containsAny(lower, DictionaryConfig.DAMAGED_INFRA)) {
            return "Hạ tầng (Infrastructure)";
        }
        if (containsAny(lower, DictionaryConfig.HOUSES_DAMAGED)) {
            return "Nhà cửa (Housing)";
        }
        if (containsAny(lower, DictionaryConfig.LOSS_BELONGINGS)) {
            return "Tài sản (Belongings)";
        }
        if (containsAny(lower, DictionaryConfig.DISRUPTION_PRODUCTION)) {
            return "Kinh tế (Economy)";
        }

        return null;
    }
}
package preprocessor.processors;

import preprocessor.core.PreProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * Bộ tiền xử lý chuẩn hóa tiếng Việt, dịch teencode và từ viết tắt.
 * <p>
 * Lớp này sử dụng một từ điển tĩnh (Static Map) để chuyển đổi các từ lóng
 * hoặc viết tắt thông dụng trên mạng xã hội về tiếng Việt chuẩn, giúp tăng
 * độ chính xác cho các thuật toán phân tích sau này.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class VietnameseNormalizer implements PreProcessor {

    // Danh sách từ điển teencode. Map được nạp sẵn để tiết kiệm chi phí tạo mới.
    private static final Map<String, String> TEENCODE_MAP = new HashMap<>();

    // Khối static chạy 1 lần duy nhất khi nạp Class vào bộ nhớ
    static {
        // Lưu ý: Các key đều có khoảng trắng 2 đầu để bắt đúng "từ độc lập" (Word Boundary)
        TEENCODE_MAP.put(" k ", " không ");
        TEENCODE_MAP.put(" ko ", " không ");
        TEENCODE_MAP.put(" kh ", " không ");
        TEENCODE_MAP.put(" vs ", " với ");
        TEENCODE_MAP.put(" dc ", " được ");
        TEENCODE_MAP.put(" đc ", " được ");
        TEENCODE_MAP.put(" j ", " gì ");
        TEENCODE_MAP.put(" v ", " vậy ");
        TEENCODE_MAP.put(" z ", " vậy ");
        TEENCODE_MAP.put(" mn ", " mọi người ");
        TEENCODE_MAP.put(" mng ", " mọi người ");
        TEENCODE_MAP.put(" ng ", " người ");
        TEENCODE_MAP.put(" qá ", " quá ");
        TEENCODE_MAP.put(" qs ", " quá ");
        TEENCODE_MAP.put(" lun ", " luôn ");
        TEENCODE_MAP.put(" r ", " rồi ");
        TEENCODE_MAP.put(" oy ", " rồi ");
        TEENCODE_MAP.put(" hnay ", " hôm nay ");
        TEENCODE_MAP.put(" cs ", " có ");
        TEENCODE_MAP.put(" vn ", " việt nam ");
        TEENCODE_MAP.put(" tphcm ", " thành phố hồ chí minh ");
        TEENCODE_MAP.put(" tp ", " thành phố ");
        TEENCODE_MAP.put(" ubnd ", " ủy ban nhân dân ");
        TEENCODE_MAP.put(" bv ", " bệnh viện ");
        TEENCODE_MAP.put(" pccc ", " phòng cháy chữa cháy ");
        TEENCODE_MAP.put(" ca ", " công an ");
    }

    /**
     * Thực hiện dò tìm và thay thế teencode bằng tiếng Việt chuẩn.
     */
    @Override
    public String process(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // Chú ý: Đệm thêm khoảng trắng ở đầu và cuối chuỗi để đảm bảo
        // các từ viết tắt nằm ở vị trí đầu/cuối câu vẫn khớp được với Key trong Map
        String result = " " + content + " ";

        // Duyệt qua từng cặp key-value trong từ điển để thực hiện thay thế
        for (Map.Entry<String, String> entry : TEENCODE_MAP.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        // Cắt bỏ khoảng trắng thừa đã đệm vào ở trên trước khi trả về
        return result.trim();
    }
}
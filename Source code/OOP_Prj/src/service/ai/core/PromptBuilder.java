package service.ai.core;

import model.result.*;

import java.util.Map;
import java.util.TreeMap;

/**
 * Lớp tiện ích hỗ trợ nhào nặn và định dạng dữ liệu (Prompt) riêng cho các dịch vụ AI.
 * <p>
 * Tách biệt hoàn toàn logic xử lý chuỗi và dữ liệu biểu đồ ra khỏi logic gọi API mạng.
 * Giúp mã nguồn tuân thủ low coupling và dễ dàng tái sử dụng.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class PromptBuilder {

    /**
     * Lắp ráp câu lệnh hoàn chỉnh (Prompt) để gửi cho AI.
     * <p>
     * Áp dụng kỹ thuật Prompt Engineering: Ép AI trả lời theo một khuôn mẫu cố định
     * luôn nhất quán, không bị dài dòng hay lộn xộn dù dùng bất kỳ AI nào.
     *
     * @param chartTitle Tiêu đề biểu đồ (giúp AI hiểu ngữ cảnh tổng thể)
     * @param dataText   Dữ liệu đã được định dạng sẵn (sinh ra từ hàm formatChartData)
     * @return Chuỗi Prompt hoàn chỉnh kèm theo yêu cầu (Instruction) khắt khe cho AI.
     */
    public static String buildPromptInstruction(String chartTitle, String dataText) {
        return "Hãy phân tích dữ liệu biểu đồ '" + chartTitle
                + "' dưới đây và đưa ra bản tóm tắt ngắn gọn bằng tiếng Việt.\n\n"
                + "Yêu cầu: Tóm tắt xu hướng (4 câu, mỗi câu 1 dòng rõ ràng dễ đọc), chỉ ra "
                + "điểm đáng chú ý và đưa ra 1 nhận xét khách quan (giải pháp phục hồi)."
                + "mở đầu: bằng câu \"Dưới đây là bản tóm gọn và phân tích\"\n\n" + dataText;
    }

    /**
     * Chuyển đổi dữ liệu phân tích thô (Map/Object) thành định dạng văn bản thô (Plain Text)
     * để các mô hình AI có thể đọc và hiểu được.
     *
     * @param problemType Loại bài toán đang phân tích (PROBLEM_1 đến 4).
     * @param results     Bản đồ (Map) chứa kết quả phân tích số liệu.
     * @return Chuỗi văn bản chứa số liệu đã được dàn phẳng, có cấu trúc dễ đọc cho AI.
     */
    public static String formatChartData(String problemType, Map<String, ? extends AnalysisResult> results) {
        // Dùng StringBuilder để tối ưu bộ nhớ khi cộng chuỗi liên tục trong vòng lặp lớn
        StringBuilder sb = new StringBuilder();

        switch (problemType) {
            case "PROBLEM_1":
                sb.append("=== DỮ LIỆU: Phân tích cảm xúc theo thời gian (Sentiment Over Time) ===\n");
                sb.append("Định dạng: Ngày | Số bài Tích cực | Số bài Tiêu cực\n\n");

                // Đổ dữ liệu Map vào TreeMap để các chuỗi ngày tháng tự động được sắp xếp
                // theo thứ tự thời gian tăng dần, giúp AI dễ dàng nhận ra "xu hướng thời gian".
                TreeMap<String, ? extends AnalysisResult> sorted = new TreeMap<>(results);
                int totalPos = 0, totalNeg = 0;

                for (Map.Entry<String, ? extends AnalysisResult> entry : sorted.entrySet()) { // bốc Key-Val O(1)
                    if (entry.getValue() instanceof SentimentResult) { // Downcasting (? trong Map)
                        SentimentResult sr = (SentimentResult) entry.getValue();
                        sb.append(String.format("  %s : Tích cực = %d, Tiêu cực = %d\n",
                                  entry.getKey(), sr.getPositiveCount(), sr.getNegativeCount()));
                        totalPos += sr.getPositiveCount();
                        totalNeg += sr.getNegativeCount();
                    }
                }
                sb.append(String.format("\nTỔNG CỘNG: Tích cực = %d, Tiêu cực = %d\n", totalPos, totalNeg));
                break;

            case "PROBLEM_2":
                sb.append("=== DỮ LIỆU: Phân loại thiệt hại (Damage Category) ===\n");
                sb.append("Định dạng: Loại thiệt hại | Số lượng bài viết\n\n");
                int totalDamage = 0;

                for (Map.Entry<String, ? extends AnalysisResult> entry : results.entrySet()) {
                    if (entry.getValue() instanceof CountNum) {
                        int count = ((CountNum) entry.getValue()).getCount();
                        // Chỉ đẩy cho AI những hạng mục có dữ liệu thực tế (>0)
                        // để tiết kiệm số lượng Token gửi đi (tiết kiệm chi phí).
                        if (count > 0) {
                            sb.append(String.format("  %s : %d bài\n", entry.getKey(), count));
                            totalDamage += count;
                        }
                    }
                }
                sb.append(String.format("\nTỔNG CỘNG: %d bài có đề cập thiệt hại\n", totalDamage));
                break;

            case "PROBLEM_3":
                sb.append("=== DỮ LIỆU: Phân tích mức độ hài lòng (Satisfaction Analysis) ===\n");
                sb.append("Định dạng: Danh mục | Số bài Tích cực | Số bài Tiêu cực\n\n");

                for (Map.Entry<String, ? extends AnalysisResult> entry : results.entrySet()) {
                    // Xử lý linh hoạt 2 loại kết quả trả về trong cùng một bài toán
                    if (entry.getValue() instanceof SentimentResult) {
                        SentimentResult sr = (SentimentResult) entry.getValue();
                        sb.append(String.format("  %s : Tích cực = %d, Tiêu cực = %d\n",
                                  entry.getKey(), sr.getPositiveCount(), sr.getNegativeCount()));
                    }
                }
                break;

            case "PROBLEM_4":
                sb.append("=== DỮ LIỆU: Phân tích điểm nóng thiên tai (Hotspot Locations) ===\n");
                sb.append("Định dạng: Khu vực | Số điểm nóng (số lần đề cập)\n\n");

                // Dùng Stream API để sắp xếp giảm dần theo số lượng điểm nóng.
                // Việc đưa các Tỉnh/Thành phố bị ảnh hưởng nặng nhất lên đầu bảng
                // giúp AI ngay lập tức tập trung sự chú ý vào các vùng trọng điểm.
                results.entrySet().stream()
                        .filter(e -> e.getValue() instanceof CountNum)
                        .sorted((a, b) -> Integer.compare(
                                ((CountNum) b.getValue()).getCount(),
                                ((CountNum) a.getValue()).getCount()))
                        .forEach(e -> sb.append(String.format("  %s : %d điểm nóng\n",
                                e.getKey(), ((CountNum) e.getValue()).getCount())));

                // Tính tổng nhanh bằng thao tác mapToInt trên luồng Stream
                int totalHotspot = results.values().stream()
                        .filter(v -> v instanceof CountNum)
                        .mapToInt(v -> ((CountNum) v).getCount())
                        .sum();

                sb.append(String.format("\nTỔNG CỘNG: %d điểm nóng trên %d khu vực\n", totalHotspot, results.size()));
                break;

            default:
                sb.append("Không xác định loại phân tích.\n");
        }
        return sb.toString();
    }
}
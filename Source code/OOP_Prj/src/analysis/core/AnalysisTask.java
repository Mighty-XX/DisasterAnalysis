package analysis.core;

import model.social.Post;
import model.result.AnalysisResult;

import java.util.Map;

/**
 * Giao diện (Interface) nền tảng định nghĩa bộ khung chuẩn cho mọi tác vụ phân tích.
 * <p>
 * Áp dụng tính Đa hình (Polymorphism) trong OOP, interface này đảm bảo tất cả
 * các thuật toán phân tích (cảm xúc, địa điểm, mức độ thiệt hại...) đều tuân thủ
 * một quy tắc thực thi thống nhất, giúp hệ thống dễ dàng tháo lắp hoặc thêm mới tính năng.
 *
 * @author Vũ Lê Dũng - 202416900
 * @param <T> Kiểu dữ liệu của kết quả phân tích (kế thừa từ AnalysisResult)
 */
public interface AnalysisTask<T extends AnalysisResult> {

    /**
     * Phương thức lõi thực thi logic phân tích thống kê trên một bài Post.
     * * @param data Bài Post đã qua các bước tiền xử lý (làm sạch dữ liệu).
     * @param result Cấu trúc Map lưu trữ kết quả phân tích gộp.
     * Key thường là nhóm phân loại (VD: Tỉnh thành, Loại thiệt hại).
     */
    void execute(Post data, Map<String, T> result);
}
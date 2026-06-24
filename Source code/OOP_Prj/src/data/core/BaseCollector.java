package data.core;

import exception.DataAnalyzerException;
import model.social.Post;
import model.social.Comment;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lớp trừu tượng BaseCollector triển khai giao diện IDataCollector.
 *
 * Lớp này áp dụng mẫu thiết kế <b>Template Method Pattern</b> nhằm định nghĩa một
 * khung xử lý dữ liệu cố định (pipeline) trong phương thức {@code collect}.
 * Quy trình gộp: Lấy bài viết -> Lọc/Nạp bình luận -> Kiểm tra ngắt luồng được thực hiện nhất quán,
 * trong khi chi tiết thu thập dữ liệu thô được nhường quyền triển khai cho các lớp con thông qua
 * hai phương thức trừu tượng {@code fetchRawPosts} và {@code fetchCommentsForPost}.
 *
 * @author Nguyễn Thị Thanh Trúc - 202417052
 */
public abstract class BaseCollector implements IDataCollector {

    // Bộ ghi nhật ký hoạt động (Logger) tự động nhận diện tên lớp con cụ thể đang chạy
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    // Giới hạn số lượng thực thể tối đa cần thu thập
    protected int maxCount;

    // Khởi tạo cấu hình và trích xuất tham số giới hạn số lượng thu thập (maxCount) nếu có
    @Override
    public void initialize(Map<String, String> configParams) {
        if (configParams != null && configParams.containsKey("maxCount")) {
            this.maxCount = Integer.parseInt(configParams.get("maxCount"));
        }
    }

    /**
     * Định nghĩa quy trình cốt lõi thu thập dữ liệu (Template Method).
     * Phương thức này được đánh dấu là <b>final</b> để ngăn các lớp con ghi đè,
     * đảm bảo quy trình thu thập và liên kết bài viết - bình luận luôn hoạt động đúng trình tự.
     *
     * @param keywords  Danh sách các từ khóa tìm kiếm dữ liệu
     * @param startDate Ngày bắt đầu khoảng thu thập
     * @param endDate   Ngày kết thúc khoảng thu thập
     * @return Danh sách các bài viết hoàn chỉnh đã được liên kết với các bình luận tương ứng
     * @throws DataAnalyzerException Khi xảy ra lỗi hệ thống hoặc luồng bị gián đoạn từ phía người dùng
     */
    @Override
    public final List<Post> collect(List<String> keywords, Date startDate, Date endDate) throws DataAnalyzerException {
        List<Post> allPosts = new ArrayList<>();
        try {
            // Tạo chuỗi truy vấn chung từ danh sách từ khóa tìm kiếm
            String query = String.join(" ", keywords);
            logger.info("Starting collection query: " + query);

            // 1. Gọi phương thức trừu tượng để lớp con thu thập danh sách bài viết thô
            allPosts = fetchRawPosts(query, startDate, endDate);

            if (allPosts == null) {
                return Collections.emptyList();
            }

            // 2. Duyệt qua từng bài viết để nạp các bình luận tương ứng
            for (Post p : allPosts) {
                // Kiểm tra xem luồng hiện tại có bị yêu cầu ngắt không (hỗ trợ tính năng dừng/hủy tiến trình trên giao diện)
                if (Thread.currentThread().isInterrupted()) {
                    throw new exception.ProcessInterruptedException("Tiến trình bị gián đoạn");
                }

                // Gọi phương thức trừu tượng để lấy danh sách bình luận cho bài viết hiện tại
                List<Comment> comments = fetchCommentsForPost(p.getId(), startDate, endDate);
                if (comments != null) {
                    for (Comment c : comments) {
                        // Kiểm tra an toàn: Chỉ nạp bình luận nếu thời gian đăng nằm trong khoảng yêu cầu
                        if (isInDateRange(c.getTimestamp(), startDate, endDate)) {
                            p.addComment(c);
                        }
                    }
                }
            }

        } catch (DataAnalyzerException e) {
            logger.log(Level.SEVERE, "Collector pipeline failed: " + e.getMessage());
            throw e; // Ném lại ngoại lệ hệ thống đã bắt được để các thành phần ở tầng trên xử lý
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in collection pipeline", e);
            throw new exception.DataStorageException("Lỗi hệ thống trong quá trình thu thập: " + e.getMessage(), e);
        }
        return allPosts;
    }

    /**
     * Phương thức trừu tượng yêu cầu lớp con tự triển khai kỹ thuật thu thập bài viết thô
     * (chưa kèm bình luận) dựa trên truy vấn tìm kiếm và khoảng thời gian.
     */
    protected abstract List<Post> fetchRawPosts(String query, Date startDate, Date endDate) throws DataAnalyzerException;

    /**
     * Phương thức trừu tượng yêu cầu lớp con tự triển khai kỹ thuật thu thập bình luận
     * cho một bài viết cụ thể dựa trên ID bài viết đó.
     */
    protected abstract List<Comment> fetchCommentsForPost(String postId, Date startDate, Date endDate) throws DataAnalyzerException;

    /**
     * Phương thức phụ trợ kiểm tra xem một mốc thời gian có nằm trong khoảng giới hạn hay không.
     *
     * @param date      Mốc thời gian cần kiểm tra
     * @param startDate Mốc bắt đầu (cho phép null nếu không giới hạn thời gian bắt đầu)
     * @param endDate   Mốc kết thúc (cho phép null nếu không giới hạn thời gian kết thúc)
     * @return true nếu mốc thời gian hợp lệ, ngược lại trả về false
     */
    protected boolean isInDateRange(Date date, Date startDate, Date endDate) {
        if (date == null)
            return false;
        if (startDate != null && date.before(startDate))
            return false;
        if (endDate != null && date.after(endDate))
            return false;
        return true;
    }

    // Tiện ích in nhanh thông báo debug ra Console kèm theo tên lớp triển khai cụ thể
    protected void log(String msg) {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + msg);
    }
}
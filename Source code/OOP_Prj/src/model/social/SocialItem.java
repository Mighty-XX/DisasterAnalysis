package model.social;

import java.util.Date;

/**
 * Lớp trừu tượng (Abstract Class) đại diện cho một thực thể chung trên mạng xã hội.
 * Lớp này đóng vai trò là lớp cha (Base Class) để định nghĩa các thuộc tính và
 * phương thức dùng chung cho các đối tượng cụ thể hơn như Bài viết (Post) hay Bình luận (Comment).
 * Việc sử dụng lớp trừu tượng ở đây giúp tối ưu hóa việc tái sử dụng mã nguồn và thể hiện tính kế thừa trong OOP.
 *
 * @author Nguyễn Thị Thanh Trúc - 202417052
 */
public abstract class SocialItem {

    // Mã định danh duy nhất của thực thể (ID bài viết hoặc ID bình luận)
    // Sử dụng phạm vi truy cập 'protected' để các lớp con kế thừa có thể truy cập trực tiếp
    protected String id;

    // Nội dung văn bản gốc chưa qua xử lý thu thập từ mạng xã hội
    protected String rawContent;

    // Nội dung văn bản sau khi đã qua các bước tiền xử lý, làm sạch (preprocessor)
    protected String cleanContent;

    // Thời điểm thực thể được đăng tải
    protected Date timestamp;

    // Số lượng lượt thích (likes) của thực thể này
    protected int likeCount;

    /**
     * Hàm khởi tạo (Constructor) để thiết lập các thông tin cơ bản cho một đối tượng SocialItem.
     * Do đây là lớp trừu tượng, hàm khởi tạo này sẽ được gọi thông qua từ khóa 'super' ở các lớp con.
     */
    public SocialItem(String id, String rawContent, Date timestamp, int likeCount) {
        this.id = id;
        this.rawContent = rawContent;
        this.timestamp = timestamp;
        this.likeCount = likeCount;
    }

    // Getters
    // Lấy mã định danh của thực thể
    public String getId() {
        return id;
    }

    // Lấy nội dung văn bản gốc chưa qua xử lý
    public String getRawContent() {
        return rawContent;
    }

    // Lấy thời điểm đăng tải của thực thể
    public Date getTimestamp() {
        return timestamp;
    }

    // Lấy số lượng lượt tương tác thích
    public int getLikeCount() {
        return likeCount;
    }

    // Lấy nội dung văn bản đã được xử lý/làm sạch
    public String getCleanContent() {
        return cleanContent;
    }

    /**
     * Cập nhật nội dung văn bản sau khi đã thực hiện xử lý, làm sạch dữ liệu.
     * Phương thức này cho phép thay đổi trạng thái của thuộc tính cleanContent từ bên ngoài lớp.
     */
    public void setCleanContent(String cleanContent) {
        this.cleanContent = cleanContent;
    }
}
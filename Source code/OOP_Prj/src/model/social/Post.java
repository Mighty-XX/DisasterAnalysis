package model.social;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp Post đại diện cho một bài viết trên mạng xã hội.
 * Lớp này kế thừa lớp trừu tượng SocialItem. Ngoài các thuộc tính chung kế thừa,
 * Post đóng vai trò quản lý danh sách các bình luận (Comment) thuộc về nó.
 * Đây là minh chứng cho mối quan hệ Aggregation trong lập trình hướng đối tượng,
 * biểu diễn quan hệ một-nhiều (một bài viết có thể chứa nhiều bình luận).
 *
 * @author Nguyễn Thị Thanh Trúc - 202417052
 */
public class Post extends SocialItem {

    // Số lượng bình luận được ghi nhận của bài viết (thu thập từ số liệu thống kê gốc)
    private int commentCount;

    // Danh sách lưu trữ các đối tượng bình luận (Comment) thực tế thuộc về bài viết này.
    // Sử dụng từ khóa 'final' để đảm bảo tham chiếu của danh sách không bị thay đổi sau khi khởi tạo.
    private final List<Comment> comments;

    /**
     * Hàm khởi tạo (Constructor) để tạo mới một đối tượng Post.
     *
     * @param id           Mã định danh duy nhất của bài viết
     * @param rawContent   Nội dung gốc chưa xử lý của bài viết
     * @param timestamp    Thời điểm bài viết được đăng tải
     * @param likeCount    Số lượng lượt thích ban đầu của bài viết
     * @param commentCount Số lượng bình luận ghi nhận trên hệ thống
     */
    public Post(String id, String rawContent, Date timestamp, int likeCount, int commentCount) {
        // Gọi hàm khởi tạo của lớp cha SocialItem bằng từ khóa 'super' để gán dữ liệu cho thuộc tính chung
        super(id, rawContent, timestamp, likeCount);
        this.commentCount = commentCount;
        // Khởi tạo danh sách động ArrayList để lưu giữ các bình luận sẽ được liên kết sau này
        this.comments = new ArrayList<>();
    }

    // Getters
    // Lấy tổng số lượng bình luận được ghi nhận của bài viết từ dữ liệu gốc
    public int getCommentCount() {
        return commentCount;
    }

    // Lấy danh sách chứa các đối tượng bình luận (Comment) thực tế liên kết với bài viết này
    public List<Comment> getComments() {
        return comments;
    }

    /**
     * Thêm một đối tượng bình luận (Comment) mới vào danh sách quản lý của bài viết này.
     * Đây là phương thức giúp thực hiện liên kết dữ liệu giữa bài viết và các bình luận thuộc về nó.
     *
     * @param comment Đối tượng bình luận cần thêm vào bài viết
     */
    public void addComment(Comment comment) {
        this.comments.add(comment);
    }
}
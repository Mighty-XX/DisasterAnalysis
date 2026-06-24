package model.social;

import java.util.Date;

/**
 * Lớp Comment đại diện cho một bình luận trên mạng xã hội.
 * Lớp này kế thừa lớp trừu tượng SocialItem.
 * Nhờ đó, Comment tự động thừa hưởng các thuộc tính chung
 * như id, rawContent, cleanContent, timestamp và likeCount,
 * đồng thời bổ sung thêm thuộc tính riêng để liên kết với bài viết tương ứng.
 *
 * @author Nguyễn Thị Thanh Trúc - 202417052
 */
public class Comment extends SocialItem {

    // Mã định danh của bài viết (Post) mà bình luận này thuộc về.
    // Thuộc tính này đóng vai trò như một khóa ngoại, thiết lập mối quan hệ liên kết (Association) giữa Comment và Post.
    private String postId;

    /**
     * Hàm khởi tạo (Constructor) để tạo mới một đối tượng Comment.
     *
     * @param postId     Mã bài viết chứa bình luận này
     * @param id         Mã định danh duy nhất của bình luận
     * @param rawComment Nội dung gốc chưa xử lý của bình luận
     * @param timestamp  Thời gian bình luận được tạo
     * @param likeCount  Số lượng lượt thích ban đầu của bình luận
     */
    public Comment(String postId, String id, String rawComment, Date timestamp, int likeCount) {
        // Sử dụng từ khóa 'super' để gọi hàm khởi tạo của lớp cha (SocialItem),
        // thực hiện gán các giá trị cho thuộc tính chung của một SocialItem.
        super(id, rawComment, timestamp, likeCount);
        this.postId = postId;
    }

    /**
     * Lấy mã định danh của bài viết chứa bình luận này.
     * Hỗ trợ việc truy vết hoặc lọc các bình luận theo từng bài viết cụ thể.
     */
    public String getPostId() {
        return postId;
    }

}
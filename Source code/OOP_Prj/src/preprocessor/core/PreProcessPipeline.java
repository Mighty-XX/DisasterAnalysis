package preprocessor.core;

import java.util.ArrayList;
import java.util.List;

import model.social.Comment;
import model.social.Post;

/**
 * Lớp đường ống (Pipeline) quản lý luồng tiền xử lý văn bản.
 * Áp dụng mẫu thiết kế Pipeline, cho phép truyền văn bản thô qua một chuỗi
 * các bộ lọc (PreProcessor) theo thứ tự để làm sạch trước khi đem đi phân tích.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class PreProcessPipeline {

    private final List<PreProcessor> processors;

    public PreProcessPipeline() {
        this.processors = new ArrayList<>();
    }

    /**
     * Nạp một bộ xử lý mới vào hệ thống.
     * @param processor Bộ xử lý cần nạp (vd: chuyển chữ thường, xóa icon...)
     */
    public void addProcessor(PreProcessor processor) {
        this.processors.add(processor); // Đẩy bộ xử lý vào cuối danh sách
    }

    /**
     * Thực thi chuỗi làm sạch trên một bài viết (Post).
     * @param post Bài viết cần xử lý nội dung.
     */
    public void execute(Post post) {
        String currentContent = post.getRawContent();

        // Chạy nội dung truyền qua từng bộ lọc
        for (PreProcessor processor : processors) {
            currentContent = processor.process(currentContent);
        }

        // Cập nhật lại nội dung đã sạch vào đối tượng
        post.setCleanContent(currentContent);
    }

    /**
     * Thực thi chuỗi làm sạch trên một bình luận (Comment).
     * Sử dụng nạp chồng phương thức (Overloading) để dùng chung logic.
     * @param cmt Bình luận cần xử lý nội dung.
     */
    public void execute(Comment cmt) {
        String currentContent = cmt.getRawContent();

        // Chạy qua từng bộ lọc tương tự như Post
        for (PreProcessor processor : processors) {
            currentContent = processor.process(currentContent);
        }

        cmt.setCleanContent(currentContent);
    }
}
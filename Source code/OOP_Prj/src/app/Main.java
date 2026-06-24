package app;

import javafx.application.Application;
import model.social.Post;
import ui.MainUI;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp Main đóng vai trò là điểm bắt đầu (Entry Point) của toàn bộ ứng dụng.
 * <p>
 * Nhiệm vụ chính của lớp này là khởi chạy giao diện đồ họa (GUI) JavaFX
 * và cung cấp một vùng nhớ toàn cục (Global State) để lưu trữ tạm thời
 * dữ liệu bài viết (Post) được luân chuyển giữa các module trong hệ thống.
 *
 * @author Vũ Lê Dũng - 202416900
 */
public class Main {

    /**
     * Bộ nhớ tạm (In-memory Store) lưu trữ danh sách các bài viết đã thu thập và làm sạch.
     * <p>
     * Khai báo public static (toàn cục) để các lớp Dịch vụ (như DataService, Analysis)
     * có thể truy cập, đọc và ghi dữ liệu nhanh chóng mà không cần truyền tham số
     * qua lại quá nhiều tầng.
     */
    public static List<Post> globalData = new ArrayList<>();

    /**
     * Phương thức main cốt lõi của Java, nơi trình biên dịch (JVM) tìm đến đầu tiên
     * để thực thi chương trình.
     * * @param args Các tham số cấu hình truyền vào từ giao diện dòng lệnh (Command Line).
     */
    public static void main(String[] args) {
        // Kích hoạt vòng đời (Lifecycle) của JavaFX và bàn giao luồng chính
        // cho lớp MainUI để khởi tạo các thành phần giao diện người dùng.
        Application.launch(MainUI.class, args);
    }
}
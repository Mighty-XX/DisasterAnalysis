package ui.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * Tiện ích hỗ trợ hiển thị các hộp thoại thông báo (Pop-up/Alert).
 * Lớp này đã bọc sẵn Platform.runLater để đảm bảo an toàn luồng (Thread-safe)
 * khi được gọi từ các tác vụ chạy ngầm (Background Threads).
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class AlertUtils {

    // CONSTANTS (HẰNG SỐ TIÊU ĐỀ)
    private static final String TITLE_ERROR = "Lỗi / Cảnh báo";
    private static final String TITLE_INFO = "Thông báo hệ thống";

    // CONSTRUCTOR
    /**
     * Private constructor để ngăn chặn việc khởi tạo đối tượng (new AlertUtils()).
     * Quy tắc chuẩn: Utility class chỉ chứa các hàm static thì không được phép khởi tạo.
     */
    private AlertUtils() {
        // Ném lỗi ngay lập tức nếu có ai đó cố tình dùng Reflection để khởi tạo
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // PUBLIC STATIC METHODS

    /**
     * Hiển thị hộp thoại cảnh báo lỗi (Error).
     * Dùng để thông báo khi nhập thiếu dữ liệu, chọn sai ngày, hoặc lỗi kết nối.
     * * @param msg Nội dung lỗi chi tiết cần thông báo
     */
    public static void showError(String msg) {
        Platform.runLater(() -> {
            // Sử dụng AlertType.ERROR để hiển thị icon dấu X đỏ trực quan hơn
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(TITLE_ERROR);
            alert.setHeaderText(null); // Gán null để ẩn phần header thừa thãi, giúp hộp thoại gọn gàng
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    /**
     * Hiển thị hộp thoại thông báo thông tin thành công (Information).
     * Dùng khi xuất file thành công hoặc hoàn thành một tác vụ.
     * @param msg Nội dung thông báo
     */
    public static void showInfo(String msg) {
        Platform.runLater(() -> {
            // Sử dụng AlertType.INFORMATION để hiển thị icon chữ i màu xanh
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(TITLE_INFO);
            alert.setHeaderText(null); // Gán null để giao diện đẹp và hiện đại hơn
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}
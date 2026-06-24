package ui.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ui.MainUI;
import ui.utils.ThemeManager;

/**
 * HomeView – Màn hình trang chủ của ứng dụng.
 *
 * Lớp này chịu trách nhiệm:
 * - Hiển thị tiêu đề chính và phụ đề chào mừng hệ thống.
 * - Cung cấp lối tắt chuyển hướng sang phân vùng Dashboard phân tích dữ liệu.
 * - Cung cấp tính năng đóng và thoát ứng dụng an toàn.
 * - Tích hợp nút chuyển đổi giao diện nhanh (Dark/Light Mode) ở góc màn hình.
 * - Đăng ký các thành phần giao diện với ThemeManager để quản lý giao diện tập trung.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class HomeView {

    // CONSTANTS (HẰNG SỐ CẤU HÌNH GIAO DIỆN)
    private static final int VBOX_SPACING = 18;
    private static final Insets THEME_BTN_MARGIN = new Insets(12, 14, 0, 0);

    // FIELDS
    /** Tham chiếu đến bộ điều hướng chính MainUI để quản lý chuyển đổi màn hình. */
    private final MainUI navigator;

    /** Node cấu trúc gốc (Root Node) trả về cho MainUI để thiết lập vào Scene. */
    private StackPane root;

    /** Nhãn chữ hiển thị tiêu đề chính của trang chủ. */
    private Label lblHomeTitle;

    /** Nhãn chữ hiển thị phụ đề giới thiệu mục đích hệ thống. */
    private Label lblHomeSubtitle;

    /** Nút bấm thực hiện chức năng thoát ứng dụng. */
    private Button btnHomeExit;

    /** Nút bấm thực hiện chuyển đổi nhanh chế độ giao diện sáng/tối. */
    private Button btnToggleTheme;

    // CONSTRUCTOR
    /**
     * Khởi tạo một thực thể HomeView mới và tiến hành xây dựng cây thành phần giao diện.
     *
     * @param navigator Tham chiếu đến bộ điều hướng chính MainUI dùng để chuyển đổi màn hình
     */
    public HomeView(MainUI navigator) {
        this.navigator = navigator;
        buildUI();
    }

    // PRIVATE METHODS (DỰNG GIAO DIỆN & XỬ LÝ SỰ KIỆN)
    /**
     * Thực hiện khởi tạo, định cấu hình Style CSS và liên kết sự kiện cho toàn bộ
     * các thành phần đồ họa xuất hiện trên màn hình trang chủ.
     */
    private void buildUI() {
        // Khởi tạo và định dạng tiêu đề chính của dự án
        lblHomeTitle = new Label("JAVA PROJECT BY GROUP 5");
        lblHomeTitle.getStyleClass().add("home-title");

        // Khởi tạo phụ đề giới thiệu ngữ cảnh ứng dụng Logistics
        lblHomeSubtitle = new Label("Humanitarian Logistics Data Analysis System");
        lblHomeSubtitle.getStyleClass().add("home-subtitle");

        // Khởi tạo nút phân tích dữ liệu chính (Chuyển hướng sang Dashboard)
        Button btnHomeAnalysis = new Button("📊  Data Analysis");
        btnHomeAnalysis.getStyleClass().add("home-btn-analysis");

        // Khởi tạo nút thoát ứng dụng hệ thống
        btnHomeExit = new Button("✖  Exit");
        btnHomeExit.getStyleClass().add("home-btn-exit");

        // Định nghĩa sự kiện khi người dùng nhấn chọn tác vụ "Data Analysis"
        btnHomeAnalysis.setOnAction(e -> {
            navigator.switchToDashboard();
        });

        // Định nghĩa sự kiện thoát ứng dụng an toàn
        btnHomeExit.setOnAction(e -> System.exit(0));

        // Khởi tạo và định vị nút bấm chuyển đổi giao diện đặt tại góc trên bên phải màn hình
        btnToggleTheme = new Button("☀ Light Mode");
        btnToggleTheme.getStyleClass().add("home-btn-toggle");
        btnToggleTheme.setOnAction(e -> ThemeManager.getInstance().toggleTheme());
        StackPane.setAlignment(btnToggleTheme, Pos.TOP_RIGHT);
        StackPane.setMargin(btnToggleTheme, THEME_BTN_MARGIN);

        // Đóng gói các thành phần văn bản và nút bấm điều hướng vào hộp bố cục dọc VBox
        VBox box = new VBox(VBOX_SPACING, lblHomeTitle, lblHomeSubtitle, btnHomeAnalysis, btnHomeExit);
        box.setAlignment(Pos.CENTER);

        // Thiết lập lớp nền Root và gán dải màu Gradient chuyển sắc mặc định
        root = new StackPane(box, btnToggleTheme);
        root.getStyleClass().add("home-root");

        // Đăng ký các thành phần UI này vào bộ quản lý ThemeManager để phục vụ cập nhật giao diện tự động
        ThemeManager.getInstance().registerHomeView(root, lblHomeTitle, lblHomeSubtitle, btnHomeExit, btnToggleTheme);
    }

    // PUBLIC METHODS
    /**
     * Trả về nút gốc (Root Node) của thành phần giao diện HomeView để nhúng vào cửa sổ Scene Graph chính.
     *
     * @return Đối tượng {@link Parent} đại diện cho cấu trúc Layout của trang chủ
     */
    public Parent getView() {
        return root;
    }
}
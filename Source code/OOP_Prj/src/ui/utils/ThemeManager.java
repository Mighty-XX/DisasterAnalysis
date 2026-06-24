package ui.utils;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.net.URL;

/**
 * Quản lý trạng thái và cấu hình giao diện (Dark/Light Mode) cho ứng dụng.
 * Lớp này áp dụng mẫu thiết kế Singleton để quản lý tập trung và cung cấp
 * điểm truy cập toàn cục cho các thành phần UI cần thay đổi giao diện.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class ThemeManager {

    // CONSTANTS (HẰNG SỐ CẤU HÌNH)
    private static final String LIGHT_MODE_TEXT = "\u2600 Light Mode";
    private static final String DARK_MODE_TEXT = "\ud83c\udf19 Dark Mode";
    private static final String DARK_THEME_PATH = "/resources/dark-theme.css";
    private static final String LIGHT_THEME_PATH = "/resources/light-theme.css";

    // SINGLETON INSTANCE
    private static final ThemeManager INSTANCE = new ThemeManager();

    // FIELDS
    private boolean isDarkMode = true;

    // Các thành phần giao diện của HomeView
    private StackPane homeRoot;
    private Label lblHomeTitle;
    private Label lblHomeSubtitle;
    private Button btnHomeExit;
    private Button btnHomeToggle;

    // Các thành phần giao diện của DashboardView
    private VBox dashboardRoot;
    private BorderPane dashboardHeader;
    private VBox dashboardInputBox;
    private Label lblDashTitle;
    private Label lblDashConfig;
    private Label lblDashLogs;
    private Label lblDashResults;
    private Label lblStatus;
    private Button btnDashHome;
    private TextArea txtLogs;
    private Label[] dashInputLabels;

    /**
     * Phương thức khởi tạo riêng tư để ngăn chặn việc tạo thực thể từ bên ngoài.
     */
    private ThemeManager() {
    }

    /**
     * Lấy thực thể duy nhất của ThemeManager.
     *
     * @return Thực thể duy nhất của lớp ThemeManager
     */
    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    // ĐĂNG KÝ GIAO DIỆN
    public void registerHomeView(StackPane homeRoot,
                                 Label lblHomeTitle,
                                 Label lblHomeSubtitle,
                                 Button btnHomeExit,
                                 Button btnHomeToggle) {
        this.homeRoot = homeRoot;
        this.lblHomeTitle = lblHomeTitle;
        this.lblHomeSubtitle = lblHomeSubtitle;
        this.btnHomeExit = btnHomeExit;
        this.btnHomeToggle = btnHomeToggle;
    }

    public void registerDashboardView(VBox dashboardRoot,
                                      BorderPane dashboardHeader,
                                      VBox dashboardInputBox,
                                      Label lblDashTitle,
                                      Label lblDashConfig,
                                      Label lblDashLogs,
                                      Label lblDashResults,
                                      Label lblStatus,
                                      Button btnDashHome,
                                      TextArea txtLogs,
                                      Label[] dashInputLabels) {
        this.dashboardRoot = dashboardRoot;
        this.dashboardHeader = dashboardHeader;
        this.dashboardInputBox = dashboardInputBox;
        this.lblDashTitle = lblDashTitle;
        this.lblDashConfig = lblDashConfig;
        this.lblDashLogs = lblDashLogs;
        this.lblDashResults = lblDashResults;
        this.lblStatus = lblStatus;
        this.btnDashHome = btnDashHome;
        this.txtLogs = txtLogs;
        this.dashInputLabels = dashInputLabels;
    }

    // LOGIC ĐỔI GIAO DIỆN

    public boolean isDarkMode() {
        return isDarkMode;
    }

    public void toggleTheme() {
        applyTheme(!isDarkMode);
    }

    public void applyCurrentTheme() {
        applyTheme(isDarkMode);
    }

    public void applyTheme(boolean dark) {
        this.isDarkMode = dark;
        String toggleLabel = dark ? LIGHT_MODE_TEXT : DARK_MODE_TEXT;

        // Cập nhật nhãn hiển thị cho các nút chuyển đổi theme
        if (btnHomeToggle != null) {
            btnHomeToggle.setText(toggleLabel);
        }
        if (dashboardHeader != null && dashboardHeader.getRight() instanceof Button) {
            ((Button) dashboardHeader.getRight()).setText(toggleLabel);
        }

        // Tải CSS tương ứng
        String cssPath = dark ? DARK_THEME_PATH : LIGHT_THEME_PATH;
        loadStylesheet(cssPath);
    }

    /**
     * Tải và áp dụng file CSS (Đã gộp chung logic của applyDarkTheme và applyLightTheme cũ).
     */
    private void loadStylesheet(String cssPath) {
        try {
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl == null) {
                System.err.println("Không tìm thấy file giao diện: " + cssPath);
                return;
            }

            String externalForm = cssUrl.toExternalForm();

            if (homeRoot != null) {
                homeRoot.getStylesheets().clear();
                homeRoot.getStylesheets().add(externalForm);
            }
            if (dashboardRoot != null) {
                dashboardRoot.getStylesheets().clear();
                dashboardRoot.getStylesheets().add(externalForm);
            }
        } catch (Exception e) {
            System.err.println("Lỗi tải giao diện: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
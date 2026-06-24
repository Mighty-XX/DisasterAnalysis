package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.utils.AlertUtils;
import ui.utils.ThemeManager;
import ui.views.ChartView;
import ui.views.DashboardView;
import ui.views.HomeView;

/**
 * MainUI – Điểm khởi động ứng dụng JavaFX, đóng vai trò là bộ điều phối trung tâm (Navigator).
 *
 * Lớp này chỉ chịu trách nhiệm:
 * - Khởi tạo Stage và Scene.
 * - Chuyển đổi qua lại giữa các màn hình (Routing).
 * - Làm cầu nối (Proxy) cập nhật UI cho DashboardView.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class MainUI extends Application {

    // CHUẨN HÓA: Đưa Constants lên đầu
    private static final int WINDOW_WIDTH = 1500;
    private static final int WINDOW_HEIGHT = 780;

    private Stage stageRef;
    private Scene mainScene;
    private HomeView homeView;
    private DashboardView dashboardView;

    @Override
    public void start(Stage primaryStage) {
        this.stageRef = primaryStage;
        primaryStage.setTitle("Humanitarian Logistics System - Group 5");

        this.homeView = new HomeView(this);
        this.dashboardView = new DashboardView(this, stageRef);

        ThemeManager.getInstance().applyCurrentTheme();

        this.mainScene = new Scene(homeView.getView(), WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    // ĐIỀU HƯỚNG MÀN HÌNH (ROUTING)

    public void switchToDashboard() {
        mainScene.setRoot(dashboardView.getView());
    }

    public void switchToHome() {
        mainScene.setRoot(homeView.getView());
    }

    public void showChart(String problemType) {
        ChartView chartView = new ChartView(this, problemType);
        if (chartView.getView() != null) {
            mainScene.setRoot(chartView.getView());
        }
    }

    // CẦU NỐI CẬP NHẬT GIAO DIỆN (PROXY FOR DASHBOARD)

    public void updateStatus(String msg) {
        Platform.runLater(() -> dashboardView.getLblStatus().setText(msg));
    }

    public void enableAnalysisButtons() {
        Platform.runLater(() -> {
            dashboardView.getBtnP1().setDisable(false);
            dashboardView.getBtnP2().setDisable(false);
            dashboardView.getBtnP3().setDisable(false);
            dashboardView.getBtnP4().setDisable(false);
            dashboardView.getBtnRun().setDisable(false);
            dashboardView.getBtnStop().setDisable(true);
            dashboardView.getProgressIndicator().setVisible(false);
        });
    }

    public void showExportButton(String csvPath) {
        Platform.runLater(() -> dashboardView.showExportButton(csvPath));
    }

    public void notifyNoDataOrError(String msg) {
        Platform.runLater(() -> {
            dashboardView.getProgressIndicator().setVisible(false);
            dashboardView.getBtnRun().setDisable(false);
            dashboardView.getBtnStop().setDisable(true);

            dashboardView.getBtnP1().setDisable(true);
            dashboardView.getBtnP2().setDisable(true);
            dashboardView.getBtnP3().setDisable(true);
            dashboardView.getBtnP4().setDisable(true);

            dashboardView.getLblStatus().setText(msg);

            // Đã tách ra AlertUtils riêng biệt
            AlertUtils.showError(msg);
        });
    }
}
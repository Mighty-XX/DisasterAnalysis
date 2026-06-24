package ui.views;

import app.DataService;
import app.CollectorConfig;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import ui.MainUI;
import ui.utils.*;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * DashboardView – Màn hình điều khiển trung tâm chứa form cấu hình,
 * khu vực hiển thị nhật ký (logs) và các lối tắt kích hoạt biểu đồ phân tích.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class DashboardView {

    // CONSTANTS (HẰNG SỐ CẤU HÌNH)
    private static final int INPUT_PREF_WIDTH = 190;
    private static final int LABEL_PREF_WIDTH = 100;
    private static final int CHART_BTN_WIDTH = 240;
    private static final int CHART_BTN_HEIGHT = 52;
    private static final int LOG_AREA_HEIGHT = 150;

    // FIELDS
    private final MainUI navigator;
    private final Stage stageRef;
    private VBox root;

    // Các thành phần điều khiển đồ thị và tiến trình thu thập dữ liệu
    private Button btnP1;
    private Button btnP2;
    private Button btnP3;
    private Button btnP4;
    private Button btnRun;
    private Button btnStop;
    private Button btnExport;
    private ProgressIndicator progressIndicator;
    private Label lblStatus;
    private TextArea txtLogs;

    // Các thành phần đồ họa cần đăng ký đồng bộ giao diện (Theme)
    private BorderPane dashboardHeader;
    private VBox dashboardInputBox;
    private Label lblDashTitle;
    private Label lblDashConfig;
    private Label lblDashLogs;
    private Label lblDashResults;
    private Button btnDashHome;
    private Label[] dashInputLabels;

    // Quản lý trạng thái tệp tin dữ liệu ngoại tuyến
    private File selectedCsvFile = null;
    private Label lblSelectedFile;

    // Bộ chọn mốc thời gian cấu hình dữ liệu đầu vào
    private DatePicker dpStart;
    private DatePicker dpEnd;

    // CONSTRUCTOR
    public DashboardView(MainUI navigator, Stage stageRef) {
        this.navigator = navigator;
        this.stageRef = stageRef;
        buildUI();
    }

    // PRIVATE METHODS (DỰNG GIAO DIỆN & XỬ LÝ SỰ KIỆN)
    private void buildUI() {
        buildHeader();
        buildStatusBar();
        buildInputBox();
        buildLogArea();
        buildChartButtons();
        assembleRoot();
        redirectSystemOut();

        ThemeManager.getInstance().registerDashboardView(
                root, dashboardHeader, dashboardInputBox,
                lblDashTitle, lblDashConfig, lblDashLogs, lblDashResults,
                lblStatus, btnDashHome, txtLogs, dashInputLabels);
    }

    private void buildHeader() {
        btnDashHome = new Button("⬅ Home");
        btnDashHome.getStyleClass().add("dash-btn-home");
        btnDashHome.setOnAction(e -> navigator.switchToHome());

        lblDashTitle = new Label("DATA ANALYSIS DASHBOARD");
        lblDashTitle.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        lblDashTitle.getStyleClass().add("dash-title");

        Button btnToggleDash = new Button("☀ Light Mode");
        btnToggleDash.getStyleClass().add("dash-btn-toggle");
        btnToggleDash.setOnAction(e -> ThemeManager.getInstance().toggleTheme());

        dashboardHeader = new BorderPane();
        dashboardHeader.setLeft(btnDashHome);
        dashboardHeader.setCenter(lblDashTitle);
        dashboardHeader.setRight(btnToggleDash);
        dashboardHeader.getStyleClass().add("dash-header");
        BorderPane.setAlignment(lblDashTitle, Pos.CENTER);
        BorderPane.setAlignment(btnToggleDash, Pos.CENTER_RIGHT);
    }

    private void buildInputBox() {
        dashboardInputBox = new VBox(10);
        dashboardInputBox.getStyleClass().add("dash-input-box");
        dashboardInputBox.setAlignment(Pos.CENTER_LEFT);

        // Dòng 1: Lựa chọn nguồn dữ liệu đầu vào
        ComboBox<String> cbbSource = new ComboBox<>();
        cbbSource.getItems().addAll(CollectorConfig.getAllSupportedSources());
        cbbSource.setValue("Dân Trí");
        cbbSource.setStyle("-fx-font-size: 14px; -fx-padding: 4;");
        cbbSource.setPrefWidth(INPUT_PREF_WIDTH);

        Button btnChooseFile = new Button("📂 Choose File...");
        btnChooseFile.setStyle("-fx-font-size: 16px; -fx-padding: 4; -fx-cursor: hand;");
        btnChooseFile.setPrefWidth(INPUT_PREF_WIDTH);

        lblSelectedFile = new Label("No file selected");
        lblSelectedFile.setStyle("-fx-font-style: italic; -fx-text-fill: #7f8c8d; -fx-font-size: 16px;");

        btnChooseFile.setVisible(false);
        lblSelectedFile.setVisible(false);

        Label lblSource = new Label("Source:");
        lblSource.setPrefWidth(LABEL_PREF_WIDTH);
        HBox row1 = new HBox(10, lblSource, cbbSource, btnChooseFile, lblSelectedFile);
        row1.setAlignment(Pos.CENTER_LEFT);

        // Dòng 2: Nhập nhóm từ khóa
        TextField txtKw = new TextField("bão Yagi");
        txtKw.setPromptText("Nhập từ khóa");
        txtKw.setStyle("-fx-font-size: 14px; -fx-padding: 4;");
        txtKw.setPrefWidth(INPUT_PREF_WIDTH);
        Label lblKw = new Label("Keywords:");
        lblKw.setPrefWidth(LABEL_PREF_WIDTH);
        HBox row2 = new HBox(10, lblKw, txtKw);
        row2.setAlignment(Pos.CENTER_LEFT);

        // Dòng 3: Khoảng thời gian
        dpStart = new DatePicker(LocalDate.of(2024, 9, 1));
        dpStart.setStyle("-fx-font-size: 14px; -fx-padding: 4;");
        dpStart.setPrefWidth(INPUT_PREF_WIDTH);

        dpEnd = new DatePicker(LocalDate.of(2024, 9, 29));
        dpEnd.setStyle("-fx-font-size: 14px; -fx-padding: 4;");
        dpEnd.setPrefWidth(INPUT_PREF_WIDTH);

        Label lblFrom = new Label("From:");
        lblFrom.setPrefWidth(LABEL_PREF_WIDTH);
        HBox row3 = new HBox(10, lblFrom, dpStart, new Label("  To:"), dpEnd);
        row3.setAlignment(Pos.CENTER_LEFT);

        // Dòng 4: Giới hạn số lượng
        TextField txtMaxCount = new TextField("");
        txtMaxCount.setPromptText("Số bài viết tối đa");
        txtMaxCount.setStyle("-fx-font-size: 14px; -fx-padding: 4;");
        txtMaxCount.setPrefWidth(INPUT_PREF_WIDTH);
        Label lblMax = new Label("Max Count:");
        lblMax.setPrefWidth(LABEL_PREF_WIDTH);
        HBox row4 = new HBox(10, lblMax, txtMaxCount);
        row4.setAlignment(Pos.CENTER_LEFT);

        dashInputLabels = new Label[] {
                (Label) row1.getChildren().get(0),
                (Label) row2.getChildren().get(0),
                (Label) row3.getChildren().get(0),
                (Label) row3.getChildren().get(2),
                (Label) row4.getChildren().get(0)
        };
        for (Label lbl : dashInputLabels) {
            lbl.getStyleClass().add("dash-input-label");
        }

        lblDashConfig = new Label("⚙ Configuration");
        lblDashConfig.getStyleClass().add("dash-lbl-config");
        dashboardInputBox.getChildren().addAll(lblDashConfig, row1, row2, row3, row4);

        cbbSource.setOnAction(e -> {
            boolean isOffline = cbbSource.getValue().contains("File");
            btnChooseFile.setVisible(isOffline);
            lblSelectedFile.setVisible(isOffline);
            txtKw.setDisable(isOffline);
        });

        btnChooseFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Data File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            File file = fileChooser.showOpenDialog(stageRef);
            if (file != null) {
                selectedCsvFile = file;
                lblSelectedFile.setText("File: " + file.getName());
                lblSelectedFile.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");
                if (btnExport != null) {
                    btnExport.setVisible(true);
                    btnExport.setManaged(true);
                }
            }
        });

        initRunButton(cbbSource, txtKw, txtMaxCount);
    }

    private void initRunButton(ComboBox<String> cbbSource, TextField txtKw, TextField txtMaxCount) {
        btnRun.setOnAction(e -> {
            if (dpStart.getValue().isAfter(dpEnd.getValue())) {
                AlertUtils.showError("Start date cannot be after End date");
                return;
            }

            String source = cbbSource.getValue();
            String customPath = null;

            if (source.contains("File")) {
                if (selectedCsvFile == null || !selectedCsvFile.exists()) {
                    AlertUtils.showError("Please select a CSV file first!");
                    return;
                }
                customPath = selectedCsvFile.getAbsolutePath();
            }

            Date start = Date.from(dpStart.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date end = Date.from(dpEnd.getValue().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

            int maxCount = Integer.MAX_VALUE;
            String maxCountStr = txtMaxCount.getText().trim();
            if (!maxCountStr.isEmpty()) {
                try {
                    maxCount = Integer.parseInt(maxCountStr);
                } catch (NumberFormatException ex) {
                    AlertUtils.showError("Max Count must be a valid number!");
                    return;
                }
            } else {
                if (!source.contains("File")) {
                    AlertUtils.showError("Please enter Max Count for online sources!");
                    return;
                }
            }

            txtLogs.clear();
            btnRun.setDisable(true);
            btnStop.setDisable(false);
            if (btnExport != null) {
                btnExport.setVisible(false);
                btnExport.setManaged(false);
            }
            progressIndicator.setVisible(true);
            disableChartButtons();

            DataService.processDataRequest(source, txtKw.getText(), start, end, navigator, customPath, maxCount);
        });
    }

    private void disableChartButtons() {
        btnP1.setDisable(true);
        btnP2.setDisable(true);
        btnP3.setDisable(true);
        btnP4.setDisable(true);
    }

    private void buildStatusBar() {
        btnRun = new Button("▶ Start collecting");
        btnRun.getStyleClass().add("dash-btn-run");

        btnStop = new Button("⏹ Stop");
        btnStop.getStyleClass().add("dash-btn-stop");
        btnStop.setDisable(true);
        btnStop.setOnAction(e -> {
            DataService.stopDataRequest();
            btnStop.setDisable(true);
            lblStatus.setText("Đang dừng thu thập...");
        });

        btnExport = new Button("💾 Export to Excel");
        btnExport.getStyleClass().add("dash-btn-export");
        btnExport.setVisible(false);
        btnExport.setManaged(false);
        btnExport.setOnAction(e -> {
            File inputCsv = selectedCsvFile;
            if (inputCsv == null || !inputCsv.exists()) {
                FileChooser openChooser = new FileChooser();
                openChooser.setTitle("Select CSV File to Export");
                openChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
                openChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                inputCsv = openChooser.showOpenDialog(stageRef);
            }

            if (inputCsv == null || !inputCsv.exists()) {
                AlertUtils.showError("No CSV file selected for export!");
                return;
            }

            FileChooser saveChooser = new FileChooser();
            saveChooser.setTitle("Save Excel File");
            saveChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            saveChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            saveChooser.setInitialFileName("BaoCao_" + inputCsv.getName().replace(".csv", ".xlsx"));
            File saveFile = saveChooser.showSaveDialog(stageRef);

            if (saveFile != null) {
                app.utils.ExcelExporter.exportStatisticsToExcel(inputCsv.getAbsolutePath(), saveFile.getAbsolutePath());
                AlertUtils.showInfo("Xuất file Excel thành công!\nĐã lưu tại: " + saveFile.getName());
            }
        });

        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);

        lblStatus = new Label("⚙ Configure your options above, then press Start");
        lblStatus.getStyleClass().add("dash-lbl-status");
    }

    private void buildLogArea() {
        txtLogs = new TextArea();
        txtLogs.setEditable(false);
        txtLogs.setPrefHeight(LOG_AREA_HEIGHT);
        txtLogs.getStyleClass().add("dash-txt-logs");

        lblDashLogs = new Label("📄 Logs (Tiến trình):");
        lblDashLogs.getStyleClass().add("dash-lbl-logs");
    }

    private void redirectSystemOut() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                Platform.runLater(() -> txtLogs.appendText(String.valueOf((char) b)));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                String s = new String(b, off, len);
                Platform.runLater(() -> txtLogs.appendText(s));
            }
        };
        System.setOut(new PrintStream(out, true));
    }

    private void buildChartButtons() {
        btnP1 = createChartButton("Sentiment Over Time", "PROBLEM_1");
        btnP2 = createChartButton("Damage Category", "PROBLEM_2");
        btnP3 = createChartButton("Satisfaction", "PROBLEM_3");
        btnP4 = createChartButton("Hotspot Locations", "PROBLEM_4");
    }

    private Button createChartButton(String text, String type) {
        Button btn = new Button(text);
        btn.setPrefSize(CHART_BTN_WIDTH, CHART_BTN_HEIGHT);
        btn.setDisable(true);
        btn.getStyleClass().add("dash-btn-chart");
        btn.setOnAction(e -> navigator.showChart(type));
        return btn;
    }

    private void assembleRoot() {
        HBox statusBox = new HBox(10, btnRun, btnStop, btnExport, progressIndicator, lblStatus);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        VBox logBox = new VBox(5, lblDashLogs, txtLogs);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(150,100,255,0.3);");

        HBox chartBox = new HBox(20, btnP1, btnP2, btnP3, btnP4);
        chartBox.setAlignment(Pos.CENTER);

        lblDashResults = new Label("─ RESULTS ─");
        lblDashResults.getStyleClass().add("dash-lbl-results");

        root = new VBox(16, dashboardHeader, dashboardInputBox, statusBox, logBox, sep, lblDashResults, chartBox);
        root.setPadding(new Insets(0, 20, 20, 20));
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("dash-root");
    }

    // PUBLIC METHODS (GETTERS & SETTERS)
    public Parent getView() {
        return root;
    }

    public void showExportButton(String csvPath) {
        if (csvPath != null && !csvPath.isEmpty()) {
            this.selectedCsvFile = new File(csvPath);
            if (this.btnExport != null) {
                this.btnExport.setVisible(true);
                this.btnExport.setManaged(true);
            }
        }
    }

    public Button getBtnP1() { return btnP1; }
    public Button getBtnP2() { return btnP2; }
    public Button getBtnP3() { return btnP3; }
    public Button getBtnP4() { return btnP4; }
    public Button getBtnRun() { return btnRun; }
    public Button getBtnStop() { return btnStop; }
    public ProgressIndicator getProgressIndicator() { return progressIndicator; }
    public Label getLblStatus() { return lblStatus; }
}
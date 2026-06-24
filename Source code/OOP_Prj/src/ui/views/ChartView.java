package ui.views;

import analysis.core.AnalysisTask;
import analysis.implementation.*;
import app.Main;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.result.*;
import model.social.Post;
import service.ai.core.AIConfig;
import service.ai.core.AIClient;
import service.ai.core.PromptBuilder;
import ui.MainUI;
import ui.utils.AlertUtils;
import ui.utils.ThemeManager;

import java.util.*;

/**
 * Màn hình hiển thị biểu đồ phân tích thống kê và vùng tương tác tóm tắt dữ liệu từ AI.
 * <p>
 * Lớp này chịu trách nhiệm khởi chạy các tác vụ phân tích {@link AnalysisTask} trên
 * toàn bộ kho dữ liệu toàn cục, dựng các thành phần đồ họa JavaFX tương ứng (LineChart,
 * PieChart, BarChart), điều phối luồng gọi các dịch vụ trí tuệ nhân tạo độc lập và
 * tự động đồng bộ hóa màu sắc theo hệ thống Stylesheet chung.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class ChartView {

    // CONSTANTS
    private static final int CHART_MAX_HEIGHT = 320;
    private static final int CHART_PREF_HEIGHT = 300;
    private static final int AI_PANEL_HEIGHT = 220;

    // FIELDS (GIỮ NGUYÊN TÊN GỐC)
    /** Tham chiếu đến MainUI để điều phối điều hướng quay lại Dashboard. */
    private final MainUI navigator;

    /** Loại bài toán phân tích hiện tại (ví dụ: "PROBLEM_1" đến "PROBLEM_4"). */
    private final String problemType;

    /** Layout gốc BorderPane chứa toàn bộ giao diện của ChartView. */
    private BorderPane root;

    /** Thanh lựa chọn giúp người dùng thay đổi nhà cung cấp dịch vụ AI. */
    private ComboBox<String> cbbAi;

    /** Giữ tạm nút bấm AI để thiết lập sự kiện kích hoạt sau khi TextArea được khởi tạo. */
    private Button pendingAiBtn;

    /**
     * Khởi tạo đối tượng ChartView, ngay lập tức kích hoạt luồng phân tích và dựng giao diện.
     *
     * @param navigator   Bộ điều hướng MainUI điều khiển luồng hiển thị cửa sổ.
     * @param problemType Mã chuỗi định danh loại đồ thị cần hiển thị.
     */
    public ChartView(MainUI navigator, String problemType) {
        this.navigator = navigator;
        this.problemType = problemType;
        buildUI();
    }

    /**
     * Kích hoạt tiến trình tính toán dữ liệu thô và tiến hành lắp ráp các thành phần UI.
     */
    private void buildUI() {
        List<Post> data = Main.globalData;
        if (data == null || data.isEmpty()) {
            AlertUtils.showError("No data!");
            return;
        }

        // 1. Ánh xạ thuật toán và tiêu đề dựa trên mã bài toán
        // Fix lỗi đỏ Wildcard Capture bằng cách ép kiểu an toàn
        @SuppressWarnings("unchecked")
        AnalysisTask<AnalysisResult> task = (AnalysisTask<AnalysisResult>) resolveTask();
        String title = resolveTitle();
        if (task == null) {
            return;
        }

        // 2. Thực thi phân tích tích lũy trên tập dữ liệu toàn cục
        Map<String, AnalysisResult> resMap = new HashMap<>();
        for (Post p : data) {
            task.execute(p, resMap);
        }

        // 3. Khởi tạo đối tượng biểu đồ đồ họa phù hợp
        Node chartNode = createChartNode(resMap);

        // 4. Tổ chức bố cục tổng thể cho màn hình
        buildLayout(chartNode, title, resMap);

        // 5. Đồng bộ hóa phong cách đồ họa sâu bên trong các nút thành phần của biểu đồ
        Platform.runLater(() -> applyChartStyle(chartNode));
    }

    /**
     * Khởi tạo đối tượng phân tích cụ thể dựa trên mã định danh bài toán.
     *
     * @return Thực thể kế thừa từ AnalysisTask, hoặc {@code null} nếu mã không hợp lệ.
     */
    private AnalysisTask<?> resolveTask() {
        switch (problemType) {
            case "PROBLEM_1":
                return new SentimentOverTimeAnalysis();
            case "PROBLEM_2":
                return new DamageCategoryAnalysis();
            case "PROBLEM_3":
                return new SatisfactionAnalysis();
            case "PROBLEM_4":
                return new LocationAnalysis();
            default:
                return null;
        }
    }

    /**
     * Chuyển đổi mã bài toán thành chuỗi văn bản hiển thị trên thanh tiêu đề.
     *
     * @return Chuỗi văn bản tiêu đề tương ứng.
     */
    private String resolveTitle() {
        switch (problemType) {
            case "PROBLEM_1":
                return "Sentiment Over Time";
            case "PROBLEM_2":
                return "Damage Category Analysis";
            case "PROBLEM_3":
                return "Satisfaction Analysis";
            case "PROBLEM_4":
                return "Hotspot Locations Analysis";
            default:
                return "";
        }
    }

    /**
     * Phương thức sinh ra các Node biểu đồ thích hợp dựa trên kết quả.
     *
     * @param results Bản đồ kết quả đã qua xử lý tính toán.
     * @return Node giao diện chứa thực thể biểu đồ JavaFX hoàn chỉnh.
     */
    private Node createChartNode(Map<String, ? extends AnalysisResult> results) {
        if (results.isEmpty()) {
            return new StackPane(new Label("No data available."));
        }

        switch (problemType) {
            case "PROBLEM_1":
                return buildLineChart(results);
            case "PROBLEM_2":
                return buildPieChart(results);
            case "PROBLEM_3":
                return buildBarChartSentiment(results);
            case "PROBLEM_4":
                return buildHorizontalBarChart(results);
            default:
                return new StackPane(new Label("Unknown chart type."));
        }
    }

    /**
     * Dựng biểu đồ đường (LineChart) chuyên dụng cho phân tích xu hướng cảm xúc theo thời gian.
     */
    private Node buildLineChart(Map<String, ? extends AnalysisResult> results) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Count");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setCreateSymbols(false);

        XYChart.Series<String, Number> pos = new XYChart.Series<>();
        pos.setName("Positive");
        XYChart.Series<String, Number> neg = new XYChart.Series<>();
        neg.setName("Negative");

        // Sử dụng TreeMap sắp xếp các chuỗi ngày tháng theo dòng thời gian tuyến tính
        TreeMap<String, ? extends AnalysisResult> sorted = new TreeMap<>(results);
        for (Map.Entry<String, ? extends AnalysisResult> entry : sorted.entrySet()) {
            if (entry.getValue() instanceof SentimentResult) {
                SentimentResult sr = (SentimentResult) entry.getValue();
                pos.getData().add(new XYChart.Data<>(entry.getKey(), sr.getPositiveCount()));
                neg.getData().add(new XYChart.Data<>(entry.getKey(), sr.getNegativeCount()));
            }
        }
        lineChart.getData().addAll(pos, neg);
        return lineChart;
    }

    /**
     * Dựng biểu đồ tròn (PieChart) hiển thị tỷ trọng phân loại các danh mục thiệt hại.
     */
    private Node buildPieChart(Map<String, ? extends AnalysisResult> results) {
        PieChart pieChart = new PieChart();
        for (Map.Entry<String, ? extends AnalysisResult> entry : results.entrySet()) {
            if (entry.getValue() instanceof CountNum) {
                int count = ((CountNum) entry.getValue()).getCount();
                if (count > 0) {
                    pieChart.getData().add(new PieChart.Data(entry.getKey(), count));
                }
            }
        }
        return pieChart;
    }

    /**
     * Dựng biểu đồ cột (BarChart) đối sánh đa chiều mức độ hài lòng trên từng khía cạnh.
     */
    private Node buildBarChartSentiment(Map<String, ? extends AnalysisResult> results) {
        CategoryAxis xBar = new CategoryAxis();
        xBar.setLabel("Category");
        NumberAxis yBar = new NumberAxis();
        yBar.setLabel("Count");

        BarChart<String, Number> barChart = new BarChart<>(xBar, yBar);
        XYChart.Series<String, Number> sPos = new XYChart.Series<>();
        sPos.setName("Positive");
        XYChart.Series<String, Number> sNeg = new XYChart.Series<>();
        sNeg.setName("Negative");

        for (Map.Entry<String, ? extends AnalysisResult> entry : results.entrySet()) {
            int pVal = 0, nVal = 0;
            if (entry.getValue() instanceof SentimentResult) {
                SentimentResult sr = (SentimentResult) entry.getValue();
                pVal = sr.getPositiveCount();
                nVal = sr.getNegativeCount();
            }
            if (pVal > 0 || nVal > 0) {
                sPos.getData().add(new XYChart.Data<>(entry.getKey(), pVal));
                sNeg.getData().add(new XYChart.Data<>(entry.getKey(), nVal));
            }
        }
        barChart.getData().addAll(sPos, sNeg);
        return barChart;
    }

    /**
     * Dựng biểu đồ cột ngang hiển thị danh sách xếp hạng tần suất xuất hiện các điểm nóng thiên tai.
     */
    private Node buildHorizontalBarChart(Map<String, ? extends AnalysisResult> results) {
        NumberAxis xAxisLoc = new NumberAxis();
        xAxisLoc.setLabel("Number of mentions (Emergency level)");
        CategoryAxis yAxisLoc = new CategoryAxis();
        yAxisLoc.setLabel("City");

        BarChart<Number, String> locChart = new BarChart<>(xAxisLoc, yAxisLoc);
        locChart.setLegendVisible(false);

        XYChart.Series<Number, String> series = new XYChart.Series<>();

        // Chuyển mảng và thực hiện thuật toán sắp xếp giảm dần cấu trúc điểm nóng
        List<Map.Entry<String, ? extends AnalysisResult>> sorted = new ArrayList<>(results.entrySet());
        sorted.sort((e1, e2) -> Integer.compare(
                ((CountNum) e2.getValue()).getCount(),
                ((CountNum) e1.getValue()).getCount()));

        for (Map.Entry<String, ? extends AnalysisResult> entry : sorted) {
            int count = ((CountNum) entry.getValue()).getCount();
            if (count > 0) {
                // Đẩy phần tử vào vị trí gốc 0 để đảo bảo đồ thị vẽ từ đỉnh cao nhất xuống dưới
                series.getData().add(0, new XYChart.Data<>(count, entry.getKey()));
            }
        }
        locChart.getData().add(series);
        return locChart;
    }

    // Xây dựng layout tổng thể

    /**
     * Thực hiện phân tách bố cục không gian, thiết lập StyleClass phục vụ quản lý CSS External.
     */
    private void buildLayout(Node chartNode, String title,
                             Map<String, ? extends AnalysisResult> results) {
        boolean dark = ThemeManager.getInstance().isDarkMode();
        root = new BorderPane();

        // Cơ chế Style dự phòng (Fallback) đảm bảo giao diện luôn hiển thị chính xác
        root.setStyle(dark
                ? "-fx-background-color: linear-gradient(to bottom, #0d0a1f, #130d2e, #0d0a1f);"
                : "-fx-background-color: linear-gradient(to bottom, #f4f6ff, #edf0ff, #f4f6ff);");

        root.setTop(buildHeader(title, dark));
        root.setCenter(buildChartCenter(chartNode));
        root.setBottom(buildAiPanel(title, results, dark));
    }

    /**
     * Thiết lập cấu trúc thanh Header: Điều hướng quay lui, Tiêu đề động, và cụm cấu hình AI.
     */
    private HBox buildHeader(String title, boolean dark) {
        Button btnBack = new Button("⬅ Back");
        btnBack.getStyleClass().add("btn-back");
        btnBack.setStyle("-fx-font-size: 14px; -fx-background-color: rgba(255,255,255,0.12);"
                + "-fx-text-fill: white; -fx-background-radius: 16;"
                + "-fx-border-color: rgba(255,255,255,0.25); -fx-border-radius: 16;"
                + "-fx-padding: 8 16 8 16; -fx-cursor: hand;");
        btnBack.setOnAction(e -> navigator.switchToDashboard());

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("chart-title-label");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblTitle.setStyle(dark
                ? "-fx-text-fill: white; -fx-effect: dropshadow(gaussian,rgba(120,60,255,0.5),8,0.3,0,1);"
                : "-fx-text-fill: white; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.3),6,0.2,0,1);");

        // Khởi tạo ComboBox lựa chọn máy chủ AI
        cbbAi = new ComboBox<>();
        cbbAi.getItems().addAll(AIConfig.getSupportedAIs());
        if (!cbbAi.getItems().isEmpty()) {
            cbbAi.setValue(cbbAi.getItems().get(0));
        }
        cbbAi.setStyle("-fx-font-size: 14px; -fx-background-radius: 6; -fx-cursor: hand;");

        Button btnAi = new Button("✨ AI Tóm tắt");
        btnAi.setStyle("-fx-font-size: 15px; -fx-padding: 8 20 8 20;"
                + "-fx-background-color: linear-gradient(to right,#6a11cb,#2575fc);"
                + "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 16;"
                + "-fx-cursor: hand; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.3),6,0,0,2);");

        Region spacerL = new Region();
        Region spacerR = new Region();
        HBox.setHgrow(spacerL, Priority.ALWAYS);
        HBox.setHgrow(spacerR, Priority.ALWAYS);

        // Giữ vững bố cục ngang đối xứng, tích hợp gọn gàng cấu trúc ComboBox liền kề nút kích hoạt
        HBox topBox = new HBox(10, btnBack, spacerL, lblTitle, cbbAi, btnAi, spacerR);
        topBox.getStyleClass().add("chart-header-box");
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(12, 16, 12, 16));
        topBox.setStyle(dark
                ? "-fx-background-color: linear-gradient(to right, #1a0533, #2d1260, #1a0533);"
                : "-fx-background-color: linear-gradient(to right, #6a11cb, #2575fc);");

        this.pendingAiBtn = btnAi;
        return topBox;
    }

    /**
     * Chuẩn hóa kích thước khung hình đồ thị hiển thị trung tâm.
     */
    private Node buildChartCenter(Node chartNode) {
        if (chartNode instanceof Chart) {
            ((Chart) chartNode).setMaxHeight(CHART_MAX_HEIGHT);
            ((Chart) chartNode).setPrefHeight(CHART_PREF_HEIGHT);
            chartNode.setStyle("-fx-background-color: transparent;");
        }
        return chartNode;
    }

    /**
     * Dựng phân vùng Panel chân trang phục vụ kết xuất thông tin tóm tắt chuyên sâu từ mô hình AI.
     */
    private VBox buildAiPanel(String title,
                              Map<String, ? extends AnalysisResult> results,
                              boolean dark) {
        Label lblAiTitle = new Label("📋 Phân tích từ AI:");
        lblAiTitle.getStyleClass().add("lbl-ai-panel-title");
        lblAiTitle.setStyle(dark
                ? "-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: rgba(200,170,255,0.95);"
                : "-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1e293b;");

        TextArea txtAi = new TextArea();
        txtAi.setEditable(false);
        txtAi.setWrapText(true);
        txtAi.setPrefHeight(AI_PANEL_HEIGHT);
        txtAi.setMaxHeight(Double.MAX_VALUE);
        txtAi.setPromptText("Kết quả tóm tắt của AI sẽ hiển thị ở đây...");
        txtAi.setStyle(dark
                ? "-fx-font-size: 15px; -fx-font-family: 'Segoe UI';"
                  + "-fx-control-inner-background: #0f0a1e; -fx-text-fill: #ddd8ff;"
                  + "-fx-border-color: rgba(150,100,255,0.3); -fx-border-radius: 6; -fx-background-radius: 6;"
                : "-fx-font-size: 15px; -fx-font-family: 'Segoe UI';"
                  + "-fx-control-inner-background: #ffffff; -fx-text-fill: #111827;"
                  + "-fx-border-color: #d0d8f8; -fx-border-radius: 6; -fx-background-radius: 6;");

        // Đồng bộ gắn kết sự kiện kích hoạt tổng hợp thông tin khi có tham chiếu từ TextArea
        if (pendingAiBtn != null) {
            pendingAiBtn.setOnAction(e -> triggerAiSummary(pendingAiBtn, txtAi, title, results));
        }

        VBox panel = new VBox(6, lblAiTitle, txtAi);
        panel.setPadding(new Insets(10, 14, 14, 14));
        panel.setStyle(dark
                ? "-fx-background-color: rgba(13,10,31,0.85);"
                  + "-fx-border-color: rgba(150,100,255,0.2) transparent transparent transparent;"
                : "-fx-background-color: #f8faff;"
                  + "-fx-border-color: #d0d8f8 transparent transparent transparent;");
        VBox.setVgrow(txtAi, Priority.ALWAYS);
        return panel;
    }

    /**
     * Kích hoạt tiến trình gửi nhận dữ liệu AI không đồng bộ (Asynchronous Thread) nhằm bảo vệ UI không bị đóng băng.
     */
    private void triggerAiSummary(Button btn, TextArea txtArea, String title,
                                  Map<String, ? extends AnalysisResult> results) {

        String selectedAiName = cbbAi.getValue();

        // Tạm khóa các nút điều phối tương tác để tránh việc Spam gọi lặp Request liên tục
        btn.setDisable(true);
        cbbAi.setDisable(true);
        txtArea.setText("⏳ Đang nhờ " + selectedAiName + " phân tích, vui lòng đợi...");

        new Thread(() -> {
            try {
                // Chuyển đổi cấu trúc Map số liệu thô thành dữ liệu văn bản có cấu trúc chuẩn
                String dataText = PromptBuilder.formatChartData(problemType, results);

                String prompt = PromptBuilder.buildPromptInstruction(title, dataText);

                // Trích xuất đối tượng Client xử lý cụ thể thông qua lớp cấu hình AIConfig
                AIClient aiService = AIConfig.getAIClient(selectedAiName);

                if (!aiService.isAvailable()) {
                    Platform.runLater(() -> {
                        txtArea.setText("❌ Lỗi: " + selectedAiName + " chưa được cấu hình API Key!");
                        btn.setDisable(false);
                        cbbAi.setDisable(false);
                    });
                    return;
                }

                String summary = aiService.getSummaryFromAI(prompt);

                // Đồng bộ kết quả xử lý thành công quay ngược lại luồng hiển thị giao diện JavaFX
                Platform.runLater(() -> {
                    txtArea.setText(summary);
                    btn.setDisable(false);
                    cbbAi.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    txtArea.setText("❌ Lỗi khi gọi AI: " + ex.getMessage());
                    btn.setDisable(false);
                    cbbAi.setDisable(false);
                });
            }
        }).start();
    }

    // Style nội dung chart (gọi sau khi chart vào scene)

    /**
     * Khai phá cấu trúc cây Node CSS nội bộ của JavaFX Chart để áp đặt chính xác màu nền và đường lưới.
     */
    private void applyChartStyle(Node chartNode) {
        if (!(chartNode instanceof Chart)) {
            return;
        }

        boolean dark = ThemeManager.getInstance().isDarkMode();
        String plotBg = dark ? "#1c1535" : "#ffffff";
        String legendBg = dark ? "rgba(30,20,60,0.85)" : "rgba(255,255,255,0.85)";
        String legendTxt = dark ? "rgba(220,210,255,0.95)" : "#374151";
        String axisLblTxt = dark ? "#ffffff" : "#111827";
        String tickLblTxt = dark ? "#ffffff" : "#4b5563";
        String tickMarkClr = dark ? "rgba(200,185,255,0.7)" : "rgba(100,116,139,0.7)";
        String gridLineClr = dark ? "rgba(140,100,230,0.30)" : "rgba(203,213,225,0.6)";
        String zeroLineClr = dark ? "rgba(180,130,255,0.55)" : "rgba(148,163,184,0.8)";
        String altRowFill = dark ? "rgba(80,50,180,0.07)" : "rgba(241,245,249,0.5)";

        // Điều phối màu nền khung vẽ đồ thị
        applyStyleToNode(chartNode, ".chart-plot-background", "-fx-background-color: " + plotBg + ";");
        applyStyleToNode(chartNode, ".chart-content", "-fx-background-color: transparent;");

        // Định hình màu sắc bảng chú giải (Legend)
        String legendStyle = "-fx-background-color: " + legendBg + ";"
                + "-fx-background-radius: 6; -fx-padding: 5 10 5 10;"
                + (dark ? "" : "-fx-border-color: #e2e8f0; -fx-border-radius: 6;");
        applyStyleToNode(chartNode, ".chart-legend", legendStyle);
        applyStyleToNodes(chartNode, ".chart-legend-item", "-fx-text-fill: " + legendTxt + "; -fx-font-size: 12px;");

        // Tạo bóng đổ và hiệu ứng chữ sắc nét cho nhãn lát cắt PieChart
        String pieLabelStyle = dark
                ? "-fx-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 4, 0.5, 0, 1);"
                : "-fx-fill: #111827; -fx-font-size: 13px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.9), 4, 0.5, 0, 1);";
        applyStyleToNodes(chartNode, ".chart-pie-label", pieLabelStyle);

        String pieLineStyle = dark
                ? "-fx-stroke: rgba(200,185,255,0.8); -fx-stroke-width: 1.2;"
                : "-fx-stroke: rgba(100,116,139,0.8); -fx-stroke-width: 1.2;";
        applyStyleToNodes(chartNode, ".chart-pie-label-line", pieLineStyle);

        // Tiêu đề đồ thị chính
        applyStyleToNodes(chartNode, ".chart-title", "-fx-text-fill: " + axisLblTxt + "; -fx-font-weight: bold; -fx-font-size: 14px;");

        // Định dạng nhãn trục tọa độ (Axis labels)
        applyStyleToNodes(chartNode, ".axis-label", "-fx-text-fill: " + axisLblTxt + "; -fx-font-size: 13px; -fx-font-weight: bold;");

        // Khung số định vị trên thước chia (Tick labels)
        String tickLblStyle = "-fx-fill: " + tickLblTxt + "; -fx-font-size: 12px; -fx-font-weight: bold;";
        applyStyleToNodes(chartNode, ".tick-label", tickLblStyle);
        applyStyleToNodes(chartNode, ".axis .tick-label", tickLblStyle);
        applyStyleToNodes(chartNode, ".axis", "-fx-tick-label-fill: " + tickLblTxt + "; -fx-font-size: 12px;");

        // Dấu gạch khấc chia tỷ lệ
        applyStyleToNodes(chartNode, ".axis .tick-mark", "-fx-stroke: " + tickMarkClr + "; -fx-fill: " + tickMarkClr + ";");

        // Hệ thống đường lưới ma trận (Grid lines)
        applyStyleToNodes(chartNode, ".chart-horizontal-grid-lines", "-fx-stroke: " + gridLineClr + ";");
        applyStyleToNodes(chartNode, ".chart-vertical-grid-lines", "-fx-stroke: " + gridLineClr + ";");

        // Đường mốc gốc tọa độ 0 (Zero baseline)
        applyStyleToNodes(chartNode, ".chart-horizontal-zero-line", "-fx-stroke: " + zeroLineClr + ";");
        applyStyleToNodes(chartNode, ".chart-vertical-zero-line", "-fx-stroke: " + zeroLineClr + ";");

        // Chế độ tô màu xen kẽ các hàng cột đồ thị (Alternating row fills)
        String altRowFillStyle = "-fx-fill: " + altRowFill + "; -fx-stroke: transparent;";
        applyStyleToNodes(chartNode, ".chart-alternative-row-fill", altRowFillStyle);
        applyStyleToNodes(chartNode, ".chart-alternative-column-fill", altRowFillStyle);
    }

    /** Helper method để áp dụng style cho 1 Node cụ thể */
    private void applyStyleToNode(Node parent, String selector, String style) {
        Node node = parent.lookup(selector);
        if (node != null) {
            node.setStyle(style);
        }
    }

    /** Helper method để áp dụng style cho nhiều Node cùng lúc */
    private void applyStyleToNodes(Node parent, String selector, String style) {
        for (Node node : parent.lookupAll(selector)) {
            node.setStyle(style);
        }
    }

    /**
     * Xuất ra Node gốc đã được dựng hoàn chỉnh để tầng điều phối chính nhúng trực tiếp vào Scene.
     *
     * @return Layout gốc {@link Parent} chứa giao diện tích hợp của màn hình đồ thị.
     */
    public Parent getView() {
        return root;
    }
}
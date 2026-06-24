package app;

import app.utils.CSVExporter;
import data.core.IDataCollector;
import exception.DataAnalyzerException;
import javafx.application.Platform;
import model.social.Post;
import preprocessor.core.PreProcessPipeline;
import preprocessor.processors.*;
import ui.MainUI;

import java.util.*;

/**
 * Lớp Dịch vụ lõi (Core Service) điều phối toàn bộ vòng đời của dữ liệu.
 * <p>
 * Chịu trách nhiệm khởi tạo kết nối (Collector), lấy dữ liệu, đưa qua bộ lọc làm sạch
 * (Pipeline) và lưu trữ. Lớp này sử dụng kỹ thuật Đa luồng (Multithreading) để
 * đảm bảo ứng dụng không bị treo trong quá trình tải dữ liệu từ API bên ngoài.
 *
 * @author Vũ Lê Dũng - 202416900
 */
public class DataService {

    // Lưu trữ tham chiếu tới luồng đang chạy để có thể ngắt (interrupt) khi cần
    private static Thread currentCollectionThread = null;

    /**
     * Xử lý yêu cầu thu thập dữ liệu do người dùng kích hoạt từ giao diện.
     */
    public static void processDataRequest(String source, String keywords,
                                          Date reqStart, Date reqEnd, MainUI uiController,
                                          String customFilePath, int maxCount) {

        stopDataRequest();// Đảm bảo dọn dẹp tiến trình cũ trước khi chạy mới

        // Tạo một luồng (Thread) riêng biệt xử lý tác vụ nặng (I/O, Network)
        // Việc này ngăn ngừa hiện tượng "Not Responding" trên giao diện JavaFX
        currentCollectionThread = new Thread(() -> {
            try {
                System.out.println("\n--- BẮT ĐẦU THU THẬP TỪ: "
                        + source.toUpperCase() + " ---");

                // Platform.runLater() được dùng để cập nhật dữ liệu trả ngược về UI Thread an toàn
                Platform.runLater(() -> uiController.updateStatus(
                        "⏳ Đang kết nối tới " + source + "..."));

                // 1. Áp dụng Design Pattern (Factory) để lấy đúng công cụ thu thập
                IDataCollector collector = CollectorConfig.getCollector(source,
                        customFilePath);
                collector.initialize(Collections.singletonMap("maxCount",
                        String.valueOf(maxCount)));

                // 2. Thu thập dữ liệu thô
                List<String> kwList = (keywords != null && !keywords.isBlank())
                        ? Arrays.asList(keywords.split(","))
                        : new ArrayList<>();

                List<Post> rawPosts = collector.collect(kwList,
                        reqStart, reqEnd);

                if (rawPosts == null || rawPosts.isEmpty()) {
                    Platform.runLater(() -> uiController.notifyNoDataOrError(
                            "⚠️ Không tìm thấy bài viết nào phù hợp!"));
                    return;
                }

                // 3. Tiền xử lý văn bản (Pipeline)
                Platform.runLater(() -> uiController.updateStatus(
                        "🧹 Đang làm sạch dữ liệu văn bản..."));
                System.out.println("\n--- ĐANG LÀM SẠCH DỮ LIỆU VĂN BẢN ("
                        + rawPosts.size() + " BÀI VIẾT) ---");
                PreProcessPipeline pipeline = buildStandardPipeline();

                for (Post p : rawPosts) {
                    pipeline.execute(p);
                    // Sử dụng Method Reference (::) của Java 8 để code tinh gọn hơn
                    if (p.getComments() != null) {
                        p.getComments().forEach(pipeline::execute);
                    }
                }

                // Cập nhật dữ liệu vào biến toàn cục cho các hàm Analysis dùng
                Main.globalData = rawPosts;

                // 4. Lưu CSV (Bỏ qua nếu đang chạy chế độ đọc File Offline)
                String savedFileName = "";

                if (!source.contains("File")) {
                    savedFileName = CSVExporter.export(Main.globalData,
                            source, keywords);
                }

                // 5. Hoàn tất & Cập nhật UI
                final String finalName = savedFileName;

                Platform.runLater(() -> {
                    String msg = "✅ Đã thu thập " + Main.globalData.size()
                            + " kết quả.";
                    if (!finalName.isEmpty()) {
                        msg += " → Đã lưu: " + finalName;
                        uiController.showExportButton(finalName);
                    } else if (source.contains("File") && customFilePath != null) {
                        uiController.showExportButton(customFilePath);
                    }

                    uiController.updateStatus(msg);
                    uiController.enableAnalysisButtons();
                });

            } catch (DataAnalyzerException e) {
                // Bắt các lỗi tự định nghĩa
                if (e instanceof exception.ProcessInterruptedException
                        || Thread.currentThread().isInterrupted()) {
                    Platform.runLater(() -> uiController.notifyNoDataOrError(
                            "⛔ Đã hủy thu thập dữ liệu!"));
                } else {
                    e.printStackTrace();
                    Platform.runLater(() -> uiController.notifyNoDataOrError(
                            "❌ Lỗi dữ liệu/lưu trữ: " + e.getMessage()));
                }
            } catch (Exception e) {
                // Bắt lỗi hệ thống chưa lường trước
                if (e instanceof InterruptedException
                        || Thread.currentThread().isInterrupted()) {
                    Platform.runLater(() -> uiController.notifyNoDataOrError(
                            "⛔ Đã hủy thu thập dữ liệu!"));
                } else {
                    e.printStackTrace();
                    Platform.runLater(() -> uiController.notifyNoDataOrError(
                            "❌ Lỗi hệ thống: " + e.getMessage()));
                }
            }
        });
        currentCollectionThread.start();
    }

    /**
     * Dừng ngay lập tức quá trình cào dữ liệu nếu người dùng bấm Hủy.
     */
    public static void stopDataRequest() {
        if (currentCollectionThread != null
                && currentCollectionThread.isAlive()) {
            currentCollectionThread.interrupt();
            currentCollectionThread = null;
        }
    }

    /**
     * Thiết lập chuỗi quy trình làm sạch dữ liệu (Pipeline Pattern).
     * Dữ liệu đi qua lần lượt: Xóa hoa -> Xóa ký tự đặc biệt -> Bỏ từ dừng -> Chuẩn hóa TV.
     */
    private static PreProcessPipeline buildStandardPipeline() {
        PreProcessPipeline pipeline = new PreProcessPipeline();

        pipeline.addProcessor(new LowerCaseProcessor());
        pipeline.addProcessor(new SpecialSymbolRemover());
        pipeline.addProcessor(new StopWordsRemover("resources/stopwords.txt"));
        pipeline.addProcessor(new VietnameseNormalizer());

        return pipeline;
    }
}
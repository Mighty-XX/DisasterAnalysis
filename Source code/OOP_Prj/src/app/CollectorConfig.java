package app;

import data.collectors.*;
import data.core.IDataCollector;

/**
 * Lớp cấu hình và quản lý việc khởi tạo các Data Collector.
 * @author Vũ Lê Dũng - 202416900
 */
public class CollectorConfig {

    /**
     * Phương thức khởi tạo Collector dựa trên tên nguồn được chọn từ UI.
     *
     * @param sourceName     Tên nguồn (VD: "VnExpress", "File CSV (Offline)",
     * "Google News")
     * @param customFilePath Đường dẫn file (chỉ dùng khi sourceName chứa chữ
     * "File")
     * @return IDataCollector tương ứng
     * @throws IllegalArgumentException Nếu chọn chế độ Offline nhưng quên truyền
     * đường dẫn
     */
    public static IDataCollector getCollector(String sourceName,
                                              String customFilePath) {

        // 1. Kiểm tra đầu vào (Safe check)
        if (sourceName == null) {
            System.err.println("Cảnh báo: Tên nguồn bị null. Đang dùng "
                    + "VnExpressCollector mặc định.");
            return new VnExpressCollector();
        }

        // 2. Xử lý trường hợp đặc biệt: Offline File
        if (sourceName.contains("File")) {
            if (customFilePath == null || customFilePath.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn một file CSV "
                        + "trước khi chạy chế độ Offline!");
            }
            return new FileCollector(customFilePath);
        }

        if (sourceName.contains("YouTube")) {
            return new YouTubeCollector();
        } else if (sourceName.contains("Dân Trí")) {
            return new DanTriCollector();
        } else if (sourceName.contains("VietNamNet")) {
            return new VietNamNetCollector();
        } else if (sourceName.contains("TikTok")) {
            return new TikTokCollector();
        } else if (sourceName.contains("VnExpress")) {
            return new VnExpressCollector();
        }

        // 4. Fallback: Nếu tên nguồn lạ, trả về mặc định
        System.err.println("Cảnh báo: Không nhận diện được nguồn '"
                + sourceName + "'. Đang dùng VnExpressCollector.");
        return new VnExpressCollector();
    }

    /**
     * Hàm trả về TẤT CẢ các nguồn hỗ trợ (bao gồm cả Online và Offline).
     * Dùng để nạp trực tiếp vào ComboBox trên UI.
     */
    public static String[] getAllSupportedSources() {
        return new String[] {
                "Dân Trí",
                "VietNamNet",
                "VnExpress",
                "YouTube",
                "TikTok",
                "File CSV (Offline)"
        };
    }
}
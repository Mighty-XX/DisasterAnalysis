package data.collectors;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import data.core.IDataCollector;
import exception.DataAnalyzerException;
import exception.DataStorageException;
import model.social.Post;

/**
 * Lớp FileCollector triển khai giao diện IDataCollector, thực hiện nhiệm vụ thu thập dữ liệu "ngoại tuyến" (offline)
 * bằng cách đọc và phân tích cú pháp (parsing) các tệp tin lưu trữ dữ liệu dạng CSV có sẵn trên máy tính.
 *
 * Lớp này hỗ trợ tự động nhận diện định dạng dữ liệu (cũ/mới), xử lý ký tự ẩn BOM, và tái cấu trúc lại
 * mối quan hệ phân cấp một-nhiều (Post - Comment) trong bộ nhớ ngay khi đọc tệp tin.
 *
 * @author Nguyễn Thị Thanh Trúc - 202417052
 */
public class FileCollector implements IDataCollector {

    // Đường dẫn tuyệt đối hoặc tương đối dẫn đến tệp tin CSV dữ liệu cần đọc
    private String filePath;

    /**
     * Hàm khởi tạo (Constructor) thiết lập đường dẫn tệp tin CSV đầu vào.
     *
     * @param filePath Đường dẫn tệp tin dữ liệu
     */
    public FileCollector(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Phương thức khởi tạo cấu hình (Triển khai bắt buộc từ IDataCollector).
     * Do FileCollector nhận đường dẫn trực tiếp qua constructor, phương thức này được để trống.
     */
    @Override
    public void initialize(Map<String, String> configParams) {
        // Không cần làm gì
    }

    /**
     * Đọc tệp tin CSV, phân tích cú pháp từng dòng dữ liệu và tiến hành lọc dữ liệu theo mốc thời gian.
     */
    @Override
    public List<Post> collect(List<String> keywords, Date startDate, Date endDate) throws DataAnalyzerException {
        List<Post> posts = new ArrayList<>();
        Map<String, Post> postMap = new HashMap<>(); // Bản đồ HashMap hỗ trợ tìm nhanh bài viết cha theo ID để nạp comment
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        File file = new File(this.filePath);
        if (!file.exists()) {
            System.err.println("❌ Lỗi: File không tồn tại: " + this.filePath);
            return posts;
        }

        System.out.println("-> Đang đọc file và lọc từ ngày: " + startDate + " đến " + endDate);

        // Sử dụng BufferedReader để tối ưu tốc độ đọc tệp tin lớn theo từng dòng (Line-by-Line)
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // Đọc dòng tiêu đề đầu tiên (CSV Header)

            // -------------------------------------------------------------------------
            // XỬ LÝ AN TOÀN: Bóc tách mã ẩn BOM UTF-8 (\ufeff)
            // Nếu tệp được lưu ở dạng UTF-8 with BOM, ký tự ẩn này sẽ xuất hiện ở ngay đầu dòng đầu tiên.
            // Cần cắt bỏ ký tự này để tránh gây lỗi phân tích cú pháp tiêu đề.
            // -------------------------------------------------------------------------
            if (line != null && line.startsWith("\ufeff"))
                line = line.substring(1);

            // Tự động nhận diện định dạng dữ liệu:
            // - Định dạng mới (phân cấp POST/COMMENT) bắt đầu bằng cột "Type,"
            // - Định dạng cũ (chỉ lưu danh sách POST thuần túy)
            boolean isNewFormat = line != null && line.startsWith("Type,");

            while ((line = br.readLine()) != null) {
                // Hỗ trợ kiểm tra ngắt luồng an toàn từ giao diện người dùng (Hủy/Dừng đọc tệp)
                if (Thread.currentThread().isInterrupted()) {
                    throw new exception.ProcessInterruptedException("Tiến trình đọc file bị gián đoạn");
                }

                if (isNewFormat) {
                    // Phân tích theo định dạng mới (Có phân biệt hàng POST và hàng COMMENT)
                    parseNewFormat(line, fmt, posts, postMap, startDate, endDate);
                } else {
                    // Phân tích theo định dạng cũ (Mỗi hàng là một bài viết POST thô)
                    Post p = parseLineQuickly(line, fmt);
                    if (p != null) {
                        Date pDate = p.getTimestamp();
                        // Thực hiện lọc theo khoảng thời gian yêu cầu
                        if (pDate != null) {
                            if (startDate != null && pDate.before(startDate))
                                continue;
                            if (endDate != null && pDate.after(endDate))
                                continue;
                        }
                        posts.add(p);
                        postMap.put(p.getId(), p);
                    }
                }
            }

        } catch (DataAnalyzerException e) {
            throw e;
        } catch (Exception e) {
            throw new DataStorageException("Lỗi đọc file CSV: " + this.filePath + " - " + e.getMessage(), e);
        }

        System.out.println("-> Kết quả sau lọc: " + posts.size() + " bài viết.");
        return posts;
    }

    /**
     * Phân tích một dòng dữ liệu theo định dạng chuẩn mới hỗ trợ phân cấp dữ liệu.
     * Tự động tái lập cấu trúc Aggregation: Nếu đọc được COMMENT, sẽ tìm POST cha tương ứng trong Map và nạp vào.
     */
    private void parseNewFormat(String line, SimpleDateFormat fmt, List<Post> posts, Map<String, Post> postMap,
                                Date startDate, Date endDate) {
        List<String> parts = parseCsvLine(line);
        if (parts.size() < 6)
            return; // Bảo vệ hệ thống khỏi các hàng dữ liệu bị khuyết thiếu cột

        String type = parts.get(0);
        String id = parts.get(1);
        String parentId = parts.get(2);
        String dateStr = parts.get(3);
        String content = parts.get(4);
        int likes = 0;
        int comments = 0;
        try {
            likes = Integer.parseInt(parts.get(5));
            if (parts.size() > 6 && !parts.get(6).isEmpty()) {
                comments = Integer.parseInt(parts.get(6));
            }
        } catch (NumberFormatException e) {
            // Bỏ qua lỗi định dạng số để giữ chương trình hoạt động ổn định
        }

        Date date = null;
        try {
            if (!dateStr.isEmpty() && !dateStr.equals("null")) {
                date = fmt.parse(dateStr);
            }
        } catch (Exception e) {
            // ignore
        }

        // Thực hiện lọc mốc thời gian an toàn
        if (startDate != null && date != null && date.before(startDate))
            return;
        if (endDate != null && date != null && date.after(endDate))
            return;

        if ("POST".equals(type)) {
            // Nếu là dòng bài viết, khởi tạo đối tượng Post và đưa vào danh sách quản lý
            Post p = new Post(id, content, date, likes, comments);
            posts.add(p);
            postMap.put(id, p);
        } else if ("COMMENT".equals(type)) {
            // Nếu là dòng bình luận, tìm bài viết cha tương ứng trong Map để liên kết gộp lại (Aggregation)
            model.social.Comment c = new model.social.Comment(parentId, id, content, date, likes);
            Post parent = postMap.get(parentId);
            if (parent != null) {
                parent.addComment(c); // Tái thiết lập mối liên kết Post - Comment trong bộ nhớ
            }
        }
    }

    /**
     * ---------------------------------------------------------------------------------
     * THUẬT TOÁN ĐẮT GIÁ: Máy trạng thái (State Machine) bóc tách dữ liệu CSV chuẩn RFC 4180.
     *
     * Phân tách dòng chữ CSV thành danh sách các cột dữ liệu một cách an toàn.
     * Tuyệt đối KHÔNG sử dụng phương thức 'line.split(",")' vì nếu văn bản nội dung bài viết chứa dấu phẩy
     * nằm trong dấu nháy kép (ví dụ: "Chào, tôi là Trúc"), lệnh split thông thường sẽ chia cắt nhầm cột
     * gây lỗi vỡ cấu trúc bảng. Thuật toán này theo dõi trạng thái nằm trong/ngoài dấu nháy để tách cột chính xác.
     * ---------------------------------------------------------------------------------
     */
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false; // Cờ theo dõi trạng thái nằm trong dấu nháy kép

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                // Xử lý dấu nháy kép trốn (Escaped quotes: hai dấu nháy kép liên tiếp "" đại diện cho một dấu nháy thực tế)
                if (i < line.length() - 1 && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++; // Nhảy qua dấu nháy kép thứ hai
                } else {
                    inQuotes = !inQuotes; // Đảo trạng thái cờ khi gặp dấu nháy đơn lẻ
                }
            } else if (c == ',' && !inQuotes) {
                // Nếu gặp dấu phẩy và KHÔNG nằm trong dấu nháy kép -> Đây là điểm phân tách cột thực sự
                result.add(sb.toString());
                sb.setLength(0); // Làm sạch bộ đệm chuẩn bị cho cột tiếp theo
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString()); // Nạp cột cuối cùng của dòng dữ liệu
        return result;
    }

    /**
     * Phương thức phân tích nhanh một dòng dữ liệu theo định dạng bài viết cũ bằng substring định vị dấu ngăn cách.
     */
    private Post parseLineQuickly(String line, SimpleDateFormat fmt) {
        try {
            int firstSep = line.indexOf("\",\"");
            int secondSep = line.indexOf("\",\"", firstSep + 3);
            int lastQuote = line.lastIndexOf("\",");

            if (firstSep == -1 || secondSep == -1 || lastQuote == -1)
                return null;

            String id = line.substring(1, firstSep);
            String dateStr = line.substring(firstSep + 3, secondSep);

            String content = line.substring(secondSep + 3, lastQuote);
            String statsPart = line.substring(lastQuote + 2);
            String[] stats = statsPart.split(",");

            int likes = 0;
            int comments = 0;
            if (stats.length >= 2) {
                likes = Integer.parseInt(stats[0].trim());
                comments = Integer.parseInt(stats[1].trim());
            }

            Date date = null;
            try {
                if (!dateStr.equals("null") && !dateStr.isEmpty()) {
                    date = fmt.parse(dateStr);
                }
            } catch (Exception e) {
                // ignore
            }
            return new Post(id, content, date, likes, comments);

        } catch (Exception e) {
            return null;
        }
    }
}
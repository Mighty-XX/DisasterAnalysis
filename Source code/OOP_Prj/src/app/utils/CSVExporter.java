package app.utils;

import model.social.Comment;
import model.social.Post;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Tiện ích hỗ trợ xuất dữ liệu ra file định dạng CSV.
 * <p>
 * Lớp này chịu trách nhiệm biến đổi các đối tượng Post/Comment trong bộ nhớ
 * thành file văn bản cấu trúc bảng, phục vụ cho việc lưu trữ offline hoặc
 * sử dụng bởi các công cụ Data Science khác (như Excel, Python Pandas).
 *
 * @author Vũ Lê Dũng - 202416900
 */
public class CSVExporter {

        // Định dạng thời gian chuẩn để xuất file không bị lỗi format
        private static final SimpleDateFormat CSV_DATE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        /**
         * Hàm chính khởi tạo quá trình xuất file.
         * Tự động làm sạch tên file để tránh các ký tự đặc biệt gây lỗi hệ điều hành.
         */
        public static String export(List<Post> posts, String source,
                        String keywords) {
                // Xóa dấu tiếng Việt và loại bỏ khoảng trắng thay vì chỉ lấy từ đầu tiên
                String cleanSource = removeAccents(source).replaceAll("\\s+", "");

                // Regex lọc bỏ các ký tự cấm trên Windows/Mac ra khỏi tên file
                String kwPart = (keywords != null && !keywords.trim().isEmpty())
                                ? "_" + keywords.trim()
                                                .replaceAll("[\\\\/:*?\"<>|]", "")
                                                .replaceAll("\\s+", "-")
                                                .replaceAll(",", "+")
                                                .replaceAll("-{2,}", "-")
                                : "";

                String dynamicFileName = resolveFileName(cleanSource
                                + kwPart.toLowerCase() + "_posts", ".csv");

                saveToCSV(posts, dynamicFileName);

                return dynamicFileName;
        }

        /**
         * Xóa bỏ dấu tiếng Việt khỏi chuỗi.
         */
        private static String removeAccents(String text) {
                if (text == null)
                        return "";
                String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
                return pattern.matcher(normalized).replaceAll("");
        }

        /**
         * Thuật toán cấp phát tên file không trùng lặp (Tự động tăng số thứ tự _1,
         * _2...).
         */
        private static String resolveFileName(String baseName, String extension) {
                int index = 1;
                File f;

                do {
                        f = new File(baseName + "_" + index + extension);
                        index++;
                } while (f.exists());

                return f.getName();
        }

        /**
         * Ghi luồng dữ liệu xuống ổ đĩa vật lý sử dụng mã hóa UTF-8 để hỗ trợ Tiếng
         * Việt.
         */
        private static void saveToCSV(List<Post> posts, String filename) {

                // Dùng try-with-resources để tự động đóng luồng ghi (writer) ngay cả khi có lỗi
                try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                                new FileOutputStream(filename), StandardCharsets.UTF_8))) {

                        // Ghi cờ BOM (Byte Order Mark) để MS Excel có thể đọc tiếng Việt chuẩn xác
                        writer.write('\ufeff');
                        writer.println("Type,ID,ParentID,Date,Content,Likes,Comments");

                        for (Post p : posts) {
                                String contentToSave = p.getCleanContent() != null
                                                ? p.getCleanContent()
                                                : (p.getRawContent() != null ? p.getRawContent() : "");

                                // Bọc chuỗi chứa dấu phẩy/xuống dòng bằng dấu ngoặc kép để CSV không bị lệch
                                // cột
                                contentToSave = contentToSave.replace("\"", "\"\"")
                                                .replace("\n", " ")
                                                .replace("\r", " ");

                                String dateStr = (p.getTimestamp() != null)
                                                ? CSV_DATE_FMT.format(p.getTimestamp())
                                                : "";

                                writer.printf("POST,\"%s\",\"\",\"%s\",\"%s\",%d,%d%n",
                                                p.getId(), dateStr, contentToSave,
                                                p.getLikeCount(), p.getCommentCount());

                                // Ghi đính kèm bình luận ngay dưới bài đăng gốc
                                if (p.getComments() != null) {
                                        for (Comment c : p.getComments()) {
                                                String cContentToSave = c.getCleanContent() != null
                                                                ? c.getCleanContent()
                                                                : (c.getRawContent() != null
                                                                                ? c.getRawContent()
                                                                                : "");

                                                cContentToSave = cContentToSave.replace("\"", "\"\"")
                                                                .replace("\n", " ")
                                                                .replace("\r", " ");

                                                String cDateStr = (c.getTimestamp() != null)
                                                                ? CSV_DATE_FMT.format(c.getTimestamp())
                                                                : "";

                                                writer.printf("COMMENT,\"%s\",\"%s\",\"%s\",\"%s\",%d,0%n",
                                                                c.getId(), p.getId(), cDateStr,
                                                                cContentToSave, c.getLikeCount());
                                        }
                                }
                        }
                        System.out.println("-> [SAVED]: " + filename);
                } catch (IOException e) {
                        // Ném lỗi về lớp cấp cao hơn để cảnh báo lên giao diện thay vì giấu lỗi
                        throw new exception.DataStorageException("Saving error: "
                                        + e.getMessage(), e);
                }
        }
}   
package preprocessor.processors;

import preprocessor.core.PreProcessor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bộ tiền xử lý loại bỏ từ dừng (Stop Words).
 * <p>
 * Stop words là các từ xuất hiện tần suất cao nhưng không mang lại nhiều ý nghĩa
 * cho việc phân tích ngữ nghĩa (ví dụ: "và", "là", "thì", "mà"). Lớp này sẽ tải
 * danh sách từ dừng từ file và loại bỏ chúng khỏi văn bản để tối ưu hóa việc phân tích từ khóa.
 *
 * @author Phạm Minh Hùng - 202416928
 */
public class StopWordsRemover implements PreProcessor {

    // Sử dụng HashSet để tìm kiếm từ (contains) với độ phức tạp O(1) - siêu nhanh
    private Set<String> stopWords;

    /**
     * Khởi tạo bộ lọc và nạp dữ liệu từ điển.
     * @param filePath Đường dẫn tới file txt chứa danh sách các từ dừng.
     */
    public StopWordsRemover(String filePath) {
        this.stopWords = new HashSet<>();
        loadStopWords(filePath);
    }

    /**
     * Đọc file chứa stop words và lưu vào bộ nhớ (RAM).
     */
    private void loadStopWords(String filePath) {
        // Ưu tiên load từ classpath (giúp app chạy ổn định kể cả khi đóng gói thành file .jar)
        InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
        try {
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            // Dùng try-with-resources để tự động đóng luồng đọc file
            try (BufferedReader reader = br) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Chuyển in thường từ điển để đảm bảo khớp 100% với text truyền vào
                    String word = line.trim().toLowerCase();
                    if (!word.isEmpty()) {
                        stopWords.add(word);
                    }
                }
            }
        } catch (IOException e) {
            throw new exception.DataStorageException("Không thể nhận diện \"" + filePath + "\" là một file chứa stop words: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa các từ dừng có trong nội dung văn bản.
     */
    @Override
    public String process(String content) {
        // Nếu rỗng thì trả về luôn, tránh lỗi NullPointerException
        if (content == null || content.isEmpty()) {
            return content;
        }

        // Tách chuỗi thành mảng các từ dựa trên khoảng trắng
        // \\s+ : Match 1 hoặc nhiều dấu cách (space, tab, xuống dòng)
        String[] words = content.split("\\s+");

        // Ứng dụng Java 8 Stream API để lọc song song
        return Arrays.stream(words)
                // Chỉ giữ lại những từ KHÔNG có mặt trong danh sách stopWords
                .filter(word -> !stopWords.contains(word.toLowerCase()))
                // Ghép mảng kết quả lại thành chuỗi, cách nhau bởi 1 dấu cách
                .collect(Collectors.joining(" "));
    }
}
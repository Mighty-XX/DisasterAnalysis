package data.core;

import java.util.*;

import exception.DataAnalyzerException;
import model.social.Post;

/**
 * Interface IDataCollector định nghĩa giao diện (hợp đồng hành vi) chung cho toàn bộ
 * các bộ thu thập dữ liệu (collectors) trong hệ thống.
 *
 * Thiết kế này đảm bảo tính đa hình (Polymorphism) và tính nhất quán, cho phép hệ thống
 * quản lý và gọi thực thi bất kỳ bộ thu thập dữ liệu nào (TikTok, YouTube, VnExpress,...)
 * thông qua một bộ khung chung.
 *
 * @author Nguyễn Thị Thanh Trúc - 202417052
 */
public interface IDataCollector {

    /**
     * Khởi tạo và thiết lập các thông số cấu hình ban đầu cho bộ thu thập dữ liệu.
     *
     * @param configParams Bản đồ (Map) chứa các tham số cấu hình dưới dạng Key-Value
     *                     (ví dụ: ("File", filePath), ("YouTube", ""), ..)
     */
    void initialize(Map<String, String> configParams);

    /**
     * Thực hiện tiến trình thu thập các bài viết (Post) dựa trên từ khóa và khoảng thời gian yêu cầu.
     *
     * @param keywords  Danh sách các từ khóa cần tìm kiếm và thu thập dữ liệu
     * @param startDate Ngày bắt đầu giới hạn khoảng thời gian thu thập dữ liệu
     * @param endDate   Ngày kết thúc giới hạn khoảng thời gian thu thập dữ liệu
     * @return Danh sách các đối tượng {@link Post} thu thập được
     * @throws DataAnalyzerException Ngoại lệ cơ sở của hệ thống, xảy ra khi có lỗi trong quá trình thu thập dữ liệu
     */
    List<Post> collect(List<String> keywords, Date startDate, Date endDate) throws DataAnalyzerException;

}
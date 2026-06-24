package data.collectors;

import data.core.BaseHtmlScraper;

/**
 * Lớp DanTriCollector kế thừa từ BaseHtmlScraper, thực hiện việc cào dữ liệu
 * chi tiết
 * từ báo điện tử Dân Trí (dantri.com.vn).
 *
 * Lớp này cung cấp cấu trúc đường dẫn tìm kiếm và danh sách các bộ chọn CSS dự
 * phòng
 * tương ứng với cấu trúc HTML của báo Dân Trí, giúp hệ thống cào thông tin
 * chính xác
 * cho cả giao diện bài viết mới và các bài viết theo chuẩn cũ.
 *
 * @author Nguyễn Thị Thanh Trúc - 202417052
 */
public class DanTriCollector extends BaseHtmlScraper {

    /**
     * Xây dựng URL trang tìm kiếm của báo Dân Trí.
     *
     * Dân Trí sở hữu cơ chế phân trang đặc thù:
     * - Trang đầu tiên (page == 1) có đường dẫn tĩnh, không chứa tham số phân
     * trang.
     * - Từ trang thứ hai trở đi (page >= 2), hệ thống mới bổ sung thêm tham số truy
     * vấn "?pi=" vào cuối URL.
     *
     * @param encodedQuery Từ khóa tìm kiếm đã mã hóa UTF-8
     * @param page         Số thứ tự trang kết quả cần lấy
     * @return String URL tìm kiếm hoàn chỉnh của Dân Trí
     */
    @Override
    protected String buildSearchUrl(String encodedQuery, int page) {
        // Dân Trí yêu cầu từ khóa trong URL phải là chữ thường, nếu có chữ hoa sẽ bị
        // lỗi HTTP 428
        String lowerQuery = encodedQuery.toLowerCase();
        return page == 1
                ? "https://dantri.com.vn/tim-kiem/" + lowerQuery + ".htm"
                : "https://dantri.com.vn/tim-kiem/" + lowerQuery + ".htm?pi=" + page;
    }

    /**
     * Định nghĩa các bộ chọn CSS thay thế (cách nhau bởi dấu phẩy) để tìm liên kết
     * bài viết.
     * Áp dụng để bắt được thẻ <a> tiêu đề nằm trong các cấu trúc thẻ khác nhau của
     * Dân Trí:
     * khối vật phẩm bài viết (.article-item .article-title a), hoặc thẻ h3 tiêu đề
     * (h3.article-title a).
     */
    @Override
    protected String getArticleLinkSelector() {
        return ".article-item .article-title a, h3.article-title a, article h3 a";
    }

    /**
     * Định nghĩa các bộ chọn CSS thay thế để trích xuất tiêu đề bài viết trong
     * trang chi tiết.
     * Dân Trí sử dụng class '.title-page' cho bài báo thông thường, nhưng có thể sử
     * dụng '.article-title' ở một số chuyên mục khác.
     */
    @Override
    protected String getTitleSelector() {
        return "h1.title-page, h1.article-title, h1";
    }

    /**
     * Định nghĩa các bộ chọn CSS thay thế để trích xuất đoạn tóm tắt ngắn (Sapo)
     * của bài viết.
     */
    @Override
    protected String getSapoSelector() {
        return "h2.sapo, h2.singular-sapo, h2";
    }

    /**
     * Định nghĩa các bộ chọn CSS thay thế để trích xuất toàn bộ các đoạn văn bản
     * nội dung bài viết.
     * Nội dung của Dân Trí nằm trong các thẻ 'p' thuộc khối '.singular-content',
     * hoặc thuộc tính dữ liệu '[data-slot=content]'.
     */
    @Override
    protected String getContentSelector() {
        return ".singular-content p, article p, [data-slot=content] p";
    }

    /**
     * Định nghĩa các bộ chọn CSS thay thế để định vị thẻ chứa mốc thời gian đăng
     * tải bài viết.
     * Mốc thời gian của Dân Trí nằm trong thẻ có class '.author-time', hoặc thuộc
     * khối tác giả '.author-wrap .author-time'.
     */
    @Override
    protected String getDateSelector() {
        return ".author-time, .author-wrap .author-time, time";
    }

    /**
     * Dân Trí giới hạn rate-limit rất gắt gao (dễ bị lỗi 428 Too Many Requests via
     * Varnish).
     * Ghi đè phương thức này để tăng độ trễ giữa các lượt tải trang lên 1000ms thay
     * vì 250ms mặc định.
     */
}
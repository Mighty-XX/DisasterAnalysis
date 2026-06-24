package data.collectors;

import data.core.BaseHtmlScraper;

/**
 * Lớp VnExpressCollector kế thừa từ BaseHtmlScraper, thực hiện việc cào dữ liệu chi tiết
 * từ báo điện tử VnExpress (vnexpress.net).
 *
 * Lớp này đóng vai trò cung cấp cấu hình đường dẫn tìm kiếm và "tọa độ" (CSS Selectors)
 * đặc thù của VnExpress để điền vào bộ khung xử lý đã được định nghĩa sẵn ở lớp cha BaseHtmlScraper.
 *
 * @author Nguyễn Thị Thanh Trúc - 202417052
 */
public class VnExpressCollector extends BaseHtmlScraper {

    /**
     * Xây dựng đường dẫn URL tìm kiếm bài viết của VnExpress dựa trên từ khóa và số trang.
     *
     * @param encodedQuery Từ khóa tìm kiếm đã được mã hóa UTF-8
     * @param page         Số thứ tự trang kết quả tìm kiếm cần lấy
     * @return String URL tìm kiếm hoàn chỉnh của VnExpress
     */
    @Override
    protected String buildSearchUrl(String encodedQuery, int page) {
        // VnExpress sử dụng tham số 'siteid=1000000' để chỉ định phạm vi tìm kiếm trên toàn bộ hệ thống trang con
        return "https://timkiem.vnexpress.net/?q=" + encodedQuery + "&siteid=1000000&page=" + page;
    }

    /**
     * Định nghĩa CSS Selector để lọc ra thẻ liên kết <a> của bài viết từ danh sách tìm kiếm.
     * Cấu trúc HTML của VnExpress: Mỗi khối bài viết nằm trong class '.item-news',
     * tiêu đề bài viết nằm trong thẻ 'h3' và đường link chi tiết nằm trong thẻ 'a'.
     */
    @Override
    protected String getArticleLinkSelector() {
        return ".item-news h3 a";
    }

    /**
     * Định nghĩa CSS Selector để trích xuất tiêu đề bài viết trong trang chi tiết.
     * VnExpress đặt tiêu đề chính của bài báo trong thẻ 'h1' có thuộc tính class là '.title-detail'.
     */
    @Override
    protected String getTitleSelector() {
        return "h1.title-detail";
    }

    /**
     * Định nghĩa CSS Selector để trích xuất phần tóm tắt ngắn (Sapo) của bài viết.
     * Sapo của VnExpress thường nằm trong thẻ 'p' có class '.description' thuộc khối '.sidebar-1'.
     */
    @Override
    protected String getSapoSelector() {
        return ".sidebar-1 p.description";
    }

    /**
     * Định nghĩa CSS Selector để trích xuất toàn bộ các đoạn văn bản nội dung của bài viết.
     * VnExpress đặt toàn bộ nội dung bài báo trong các thẻ 'p' nằm bên trong khối class '.fck_detail'.
     */
    @Override
    protected String getContentSelector() {
        return ".fck_detail p";
    }

    /**
     * Định nghĩa CSS Selector để trích xuất thời gian đăng tải bài viết.
     * Mốc thời gian của VnExpress được đặt trong phần tử có class là '.date'.
     */
    @Override
    protected String getDateSelector() {
        return ".date";
    }
}
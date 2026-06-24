# 🌪️ DisasterAnalysis - Hệ thống Cào và Phân tích Dữ liệu Thảm họa

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-FF0000?style=for-the-badge&logo=java&logoColor=white)
![Apache POI](https://img.shields.io/badge/Apache_POI-D22128?style=for-the-badge&logo=apache&logoColor=white)
![OOP](https://img.shields.io/badge/Architecture-OOP_%7C_Layered-blue?style=for-the-badge)

**DisasterAnalysis** là một ứng dụng Desktop được phát triển bằng Java/JavaFX, áp dụng triệt để các nguyên lý Lập trình Hướng đối tượng (OOP) và mẫu thiết kế (Design Patterns). Hệ thống hỗ trợ thu thập, tiền xử lý và phân tích chuyên sâu dữ liệu từ các nền tảng mạng xã hội (VnExpress, YouTube,...) nhằm trích xuất thông tin về tình hình thiên tai, thiệt hại và cảm xúc của cộng đồng.

---

## ✨ Tính năng nổi bật

* **🔍 Thu thập dữ liệu (Data Scraping):** Tự động cào bài viết và bình luận từ nhiều nguồn (VnExpress, YouTube) dựa trên từ khóa thiên tai.
* **🧹 Tiền xử lý văn bản (Preprocessing):** Làm sạch dữ liệu thô, loại bỏ ký tự đặc biệt và chuẩn hóa chuỗi bằng 파ipeline đa hình.
* **🧠 Phân tích chuyên sâu (Data Analysis):**
    * Phân loại cảm xúc (Tích cực / Tiêu cực / Trung lập).
    * Nhận diện danh mục thiệt hại (Con người, Hạ tầng, Tài sản, Kinh tế,...).
    * Phát hiện điểm nóng thiên tai (Hotspot Locations).
* **📊 Trực quan hóa dữ liệu (Dashboard):** Giao diện UI/UX thân thiện xây dựng bằng JavaFX, hiển thị biểu đồ thống kê trực quan.
* **📑 Xuất báo cáo (Export Report):** Kết xuất dữ liệu phân tích ra file Excel (.xlsx) định dạng chuẩn chuyên nghiệp bằng `Apache POI`.
* **🤖 Tích hợp AI:** Hỗ trợ tóm tắt dữ liệu tự động thông qua giao tiếp với các mô hình ngôn ngữ lớn (LLM).

---

## 🏗️ Kiến trúc Hệ thống (Architecture)

Dự án được thiết kế theo mô hình **Kiến trúc Phân tầng (Layered Architecture)** kết hợp với chiến lược **Đóng gói theo Tính năng (Package by Feature)**, tuân thủ nghiêm ngặt các nguyên lý SOLID:

* `model`: Chứa các thực thể cốt lõi (Domain Models) độc lập hoàn toàn.
* `data`: Phụ trách thu thập và thao tác dữ liệu đầu vào.
* `preprocessor`: Chuyên trách làm sạch văn bản thô.
* `analysis`: Đóng gói logic thuật toán phân tích và từ điển cấu hình.
* `service`: Giao tiếp mạng và kết nối API ngoại vi (AI).
* `ui`: Tầng hiển thị giao diện người dùng JavaFX.
* `exception`: Hệ thống xử lý ngoại lệ tùy chỉnh tập trung.

---

## ⚙️ Cài đặt và Khởi chạy

### Yêu cầu hệ thống (Prerequisites)
* [Java JDK 17+](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
* [Maven](https://maven.apache.org/) (Trình quản lý thư viện)
* IDE khuyên dùng: IntelliJ IDEA hoặc Eclipse.

### Các bước cài đặt
1. **Clone repository này về máy:**
   ```bash
   git clone https://github.com/Mighty-XX/DisasterAnalysis.git

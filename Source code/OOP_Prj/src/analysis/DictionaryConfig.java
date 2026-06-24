package analysis;

import java.util.*;

/**
 * Lớp cấu hình từ điển từ khóa phục vụ cho hệ thống phân tích ngữ nghĩa.
 * <p>
 * Lưu trữ danh sách các từ khóa được phân nhóm tĩnh (thiệt hại, cảm xúc, địa phương).
 * Việc tách riêng dữ liệu từ khóa khỏi logic xử lý giúp hệ thống dễ dàng mở rộng,
 * cập nhật thêm từ mới sau này mà không cần can thiệp vào code phân tích lõi.
 *
 * @author Vũ Lê Dũng - 202416900
 */

public class DictionaryConfig {
        // Nhóm từ khóa nhận diện các loại thiệt hại thực tế
        public static final List<String> AFFECTED_PEOPLE = Arrays.asList(
                "người bị thương", "mất tích", "cần cứu", "tử vong",
                "người dân gặp nạn", "thương vong", "thiệt mạng", "hy sinh",
                "đuối nước", "vùi lấp", "mắc kẹt", "cấp cứu", "nguy kịch",
                "cô lập", "trẻ em", "người già", "thai phụ", "bà bầu", "sản phụ",
                "trẻ sơ sinh", "người tàn tật", "người khuyết tật", "người bệnh",
                "lũ cuốn", "bị vùi", "nhập viện", "bỏ mạng", "mất mạng", "bị nạn",
                "kêu cứu", "chết đuối", "gia đình có con nhỏ", "người neo đơn");

        public static final List<String> DAMAGED_INFRA = Arrays.asList(
                "cầu sập", "đứt đường", "mất điện", "cột điện", "trạm y tế",
                "trạm xá", "sạt lở", "sụt lún", "vỡ đê", "hỏng đường", "chia cắt",
                "tê liệt", "cột viễn thông", "đứt cáp", "mất sóng",
                "trường học bị ngập", "hư hỏng cầu", "sập cầu", "lũ quét",
                "sạt núi", "đứt gãy giao thông", "ngập đường", "tắc đường",
                "kẹt xe", "cột điện gãy", "đổ cột điện", "cháy trạm biến áp",
                "hư cáp quang", "sạt lở đường sắt", "vỡ đập", "tràn đê",
                "ngập quốc lộ", "sập cầu treo", "hư hỏng cầu cống");

        public static final List<String> HOUSES_DAMAGED = Arrays.asList(
                "nhà sập", "tường đổ", "thiệt hại nhà", "mất nhà", "lều",
                "mái nhà", "tốc mái", "ngập nhà", "nước vào nhà", "trôi nhà",
                "sập mái", "nứt tường", "vỡ ngói", "hư hỏng chỗ ở", "sập vách",
                "cuốn trôi nhà", "ngập tới nóc", "bay mái tôn", "bung nóc",
                "nhà cấp 4 sập", "vỡ kính", "trôi trơ nền", "ngập sâu",
                "ngập lút mái", "nước dâng cao", "nhà trọ ngập", "sập tường bao",
                "bể bồn nước", "bay cửa sổ", "tàn phá nhà cửa");

        public static final List<String> LOSS_BELONGINGS = Arrays.asList(
                "mất tài sản", "mất trắng", "mất sạch", "tan hoang đồ đạc",
                "mất đồ", "hư hỏng đồ đạc", "tư trang", "đồ đạc nát bươm",
                "gió cuốn bay", "bay mất đồ", "thổi bay tài sản", "cuốn mất",
                "bay mất giấy tờ", "bay quần áo", "gió thổi bay", "bay mất ví",
                "bay đồ đạc", "gió cuốn đi", "đè nát", "đè bẹp", "vỡ nát đồ",
                "sập đè tài sản", "cây đè hỏng đồ", "đồ đạc bị đè", "tôn đè hỏng",
                "kính vỡ làm hỏng", "nát đồ", "gãy hỏng", "cây đè xe", "bẹp ô tô",
                "lật xe", "cây đè ô tô", "bay xe máy", "vỡ kính xe", "hỏng xe", "bẹp xe máy",
                "móp xe", "xe bị đè", "hỏng tivi", "hỏng tủ lạnh", "chập cháy đồ điện",
                "hư máy móc", "ướt sũng đồ điện", "hỏng máy giặt", "vỡ tivi",
                "mất điện thoại", "rơi vỡ đồ", "mất giấy tờ", "giấy tờ tùy thân", "mất bóp",
                "mất két sắt", "mất sách vở", "ướt sạch", "quần áo ướt sũng");

        public static final List<String> DISRUPTION_PRODUCTION = Arrays.asList(
                "mất mùa", "ao cá", "cây trồng", "thất thu", "ngừng sản xuất",
                "chết gà", "trôi bò", "chết lợn", "trôi heo", "ngập lúa",
                "hỏng hoa màu", "vỡ đầm tôm", "trôi lồng bè", "dập nát",
                "xưởng ngập", "kho ngập", "thiệt hại nông nghiệp", "cây gãy",
                "chết vùi", "ngập úng lúa", "chết trâu", "trôi gia súc",
                "trôi gia cầm", "bè cá", "lồng tôm", "vườn cây ăn trái",
                "sập nhà màng", "gãy đổ cây công nghiệp", "thiệt hại kinh tế",
                "chết hàng loạt", "hư hỏng máy nông nghiệp");

        // Nhóm từ khóa phân loại sắc thái cảm xúc (Sentiment)
        public static final List<String> POSITIVE_WORDS = Arrays.asList(
                "an toàn", "bình an", "ổn", "đỡ rồi", "qua khỏi", "sống sót",
                "khỏe", "bình yên", "vô sự", "thoát nạn", "an lành",
                "tai qua nạn khỏi", "may mắn", "bình phục", "kịp thời",
                "ấm lòng", "đoàn kết", "chung tay", "sẻ chia",
                "tương thân tương ái", "kiên cường", "được cứu", "nhiệt tình",
                "ủng hộ", "cảm ơn", "biết ơn", "tuyệt vời", "tốt", "trân trọng",
                "yên tâm", "nhẹ nhõm", "khả quan", "hi vọng", "hy vọng", "mừng",
                "tự hào", "hỗ trợ", "giúp đỡ", "tình người", "tử tế", "quyên góp",
                "nhường cơm sẻ áo", "cố lên", "bình tĩnh", "vượt qua", "tích cực",
                "lạc quan", "cứu trợ thành công", "đến nơi an toàn", "tiếp tế", "lan tỏa");

        public static final List<String> NEGATIVE_WORDS = Arrays.asList(
                "tử vong", "chết", "mất tích", "thi thể", "bị thương",
                "thương vong", "nguy kịch", "cấp cứu", "mắc kẹt", "vùi lấp",
                "kiệt sức", "đuối nước", "sập", "trôi", "ngập", "hỏng",
                "thiệt hại", "tàn phá", "tốc mái", "cuốn trôi", "sạt lở",
                "vỡ đê", "đổ nát", "hoang tàn", "chia cắt", "cô lập", "mất điện",
                "mất trắng", "bão", "lũ", "đói", "khát", "thiếu thốn", "kêu cứu",
                "khẩn cấp", "nguy hiểm", "cạn kiệt", "bơ vơ", "rét", "lạnh",
                "cạn lương thực", "buồn", "khủng khiếp", "hậu quả", "hối hận",
                "đau xót", "xót xa", "thương tâm", "hoảng loạn", "sợ hãi",
                "tuyệt vọng", "bàng hoàng", "khóc", "tang thương", "đau lòng",
                "kêu gào", "đói rét", "lạnh buốt", "mệt mỏi", "kiệt quệ",
                "bất lực", "kinh hoàng", "tàn khốc", "ám ảnh", "cơ cực",
                "thảm họa", "hiểm nguy", "xác xơ", "tan hoang", "khốn khổ");

        /**
         * Khởi tạo và trả về từ điển ánh xạ các nhóm Hỗ trợ (Nhu yếu phẩm, Y tế...).
         */
        public static Map<String, List<String>> getItemKeywords() {
                Map<String, List<String>> map = new HashMap<>();

                map.put("Chỗ ở (Shelter)", Arrays.asList(
                        "chỗ ở", "nhà", "lều", "bạt", "mái", "nhà trọ",
                        "mái tôn", "trú ẩn", "tạm lánh", "nhà văn hóa", "trường học",
                        "chăn", "mền", "chiếu", "mùng", "màn", "chỗ ngủ",
                        "tấm lợp", "nhà bạt", "túi ngủ", "đệm", "quần áo ấm",
                        "áo mưa", "áo khoác", "giày dép", "trung tâm sơ tán"));

                map.put("Vận chuyển (Transport)", Arrays.asList(
                        "xe", "thuyền", "cano", "cầu", "xe khách", "bán tải",
                        "xuồng", "đò", "đường", "giao thông", "cô lập", "chia cắt",
                        "phà", "trực thăng", "xe tải", "áo phao", "ủng",
                        "đường thủy", "xe múc", "xe cẩu", "thuyền máy", "ghe mo",
                        "ghe", "vỏ lãi", "bè chuối", "phao cứu sinh", "xe lội nước",
                        "đường mòn", "thuyền tôn", "đò ngang"));

                map.put("Lương thực (Food)", Arrays.asList(
                        "thức ăn", "gạo", "mì", "nước", "đói", "mì tôm", "gói mì",
                        "lương khô", "thực phẩm", "sữa", "nhu yếu phẩm", "nước sạch",
                        "cơm", "đồ hộp", "rau", "thịt", "nước suối",
                        "bánh chưng", "bánh mì", "nước lọc", "sữa bột", "sữa hộp",
                        "sữa tươi", "đồ ăn nhanh", "bếp gas mini", "cồn khô",
                        "bếp cồn", "gạo lứt", "muối vừng", "lạc rang", "cháo",
                        "nước đóng chai", "bánh quy", "thực phẩm khô", "xúc xích"));

                map.put("Y tế và Vệ sinh (Medical & Hygiene)", Arrays.asList(
                        // Đổi tên nhóm để bao hàm cả vật dụng vệ sinh rất quan trọng trong lũ lụt
                        "thuốc", "bác sĩ", "y tế", "bệnh viện", "viên lọc nước",
                        "cấp cứu", "băng gạc", "sơ cứu", "trạm xá", "dịch bệnh",
                        "nước sát khuẩn", "khẩu trang", "thuốc men", "bị thương",
                        "băng cá nhân", "cồn y tế", "thuốc cảm", "thuốc sốt",
                        "thuốc tiêu chảy", "thuốc đau bụng", "thuốc bôi muỗi",
                        "băng vệ sinh", "tã lót", "bỉm", "nước muối sinh lý",
                        "dầu gió", "vitamin", "thuốc ghẻ", "nước rửa tay", "xà phòng"));

                map.put("Tiền mặt (Cash)", Arrays.asList(
                        "tiền", "quỹ", "chuyển khoản", "mặt trận tổ quốc",
                        "quyên góp", "tài trợ", "stk", "hiện kim", "ủng hộ", "vnd",
                        "tiền mặt", "momo", "zalopay", "ngân hàng", "qr code",
                        "số tài khoản", "chuyển tiền", "gửi tiền", "đóng góp"));

                map.put("Cứu hộ (Rescue)", Arrays.asList(
                        "cứu nạn", "tìm kiếm", "cứu hộ", "mắc kẹt",
                        "mất tích", "đội cứu hộ", "chó nghiệp vụ", "flycam",
                        "dây thừng", "đèn pin siêu sáng", "cưa máy", "thợ lặn",
                        "trực thăng cứu hộ", "cứu hỏa", "quân đội", "bộ đội",
                        "dân quân", "công an", "xuồng cao su", "ròng rọc",
                        "máy xúc", "lực lượng chức năng", "người nhái"));

                map.put("Liên lạc (Communication)", Arrays.asList(
                        "điện thoại", "sóng", "mất mạng", "mất liên lạc",
                        "wifi", "sạc dự phòng", "đèn pin", "mất điện",
                        "máy phát điện", "pin", "sim", "3g", "4g", "5g", "mạng xã hội",
                        "zalo", "facebook", "bộ đàm", "đài radio", "loa phát thanh",
                        "trạm bts", "pin mặt trời", "cáp sạc", "viễn thông"));

                return map;
        }

        /**
         * Khởi tạo và trả về từ điển điểm nóng địa phương.
         * Gom nhóm các xã/huyện/di tích vào chung một Tỉnh/Thành phố đại diện
         * để dễ dàng thống kê thiệt hại theo khu vực lớn.
         */
        public static Map<String, List<String>> getLocationKeywords() {
                Map<String, List<String>> map = new HashMap<>();

                // Nhóm các tỉnh chịu thiệt hại nặng nhất
                map.put("Lào Cai", Arrays.asList(
                        "lào cai", "làng nủ", "bảo yên", "bát xát", "bắc hà", "sapa"));
                map.put("Yên Bái", Arrays.asList(
                        "yên bái", "thác bà", "trấn yên", "lục yên", "mù cang chải",
                        "văn chấn"));
                map.put("Phú Thọ", Arrays.asList(
                        "phú thọ", "cầu phong châu", "phong châu", "việt trì",
                        "hạ hòa", "tam nông"));
                map.put("Quảng Ninh", Arrays.asList(
                        "quảng ninh", "hạ long", "cẩm phả", "bãi cháy", "cô tô",
                        "uông bí", "vân đồn"));
                map.put("Hải Phòng", Arrays.asList(
                        "hải phòng", "đồ sơn", "cát bà", "kiến thụy", "thủy nguyên",
                        "bạch long vĩ"));
                map.put("Cao Bằng", Arrays.asList(
                        "cao bằng", "nguyên bình", "bảo lâm"));
                map.put("Thái Nguyên", Arrays.asList(
                        "thái nguyên", "sông cầu", "phú bình", "đồng hỷ",
                        "định hóa"));
                map.put("Tuyên Quang", Arrays.asList(
                        "tuyên quang", "sông lô", "chiêm hóa", "sơn dương"));
                map.put("Hà Nội", Arrays.asList(
                        "hà nội", "thủ đô", "chương dương", "long biên", "hồ tây",
                        "sông hồng"));
                map.put("Bắc Ninh", Arrays.asList(
                        "bắc ninh", "kinh bắc", "từ sơn", "yên phong", "quế võ",
                        "thuận thành", "gia bình", "lương tài", "tiên du",
                        "sông đuống", "Bắc Giang"));

                return map;
        }
}
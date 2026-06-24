package app.utils;

import analysis.DictionaryConfig;
import ui.utils.ExcelStyleUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ExcelExporter – Lớp tiện ích hỗ trợ đọc dữ liệu từ file CSV, tiến hành
 * phân tích thống kê chuyên sâu và xuất báo cáo trực quan ra định dạng Excel (.xlsx).
 * Lớp này hỗ trợ phân tích cảm xúc và phân loại thiệt hại để xuất biểu mẫu.
 *
 * @author Vũ Lê Dũng - 202416900
 */
public class ExcelExporter {

    // 1. CHUẨN CONVENTION: Lớp tiện ích (Utility Class) chỉ chứa các phương thức static
    // thì bắt buộc phải private constructor để ngăn chặn việc dùng từ khóa 'new' khởi tạo đối tượng.
    private ExcelExporter() {
        throw new UnsupportedOperationException("Đây là lớp tiện ích, không được khởi tạo đối tượng!");
    }

    public static void exportStatisticsToExcel(String csvFilePath, String excelOutputPath) {
        int totalPosts = 0;
        int totalComments = 0;

        int posCount = 0;
        int negCount = 0;
        int neuCount = 0;

        int affectedPeople = 0;
        int infraDamage = 0;
        int houseDamage = 0;
        int propertyLoss = 0;
        int economyDisruption = 0;
        int otherDamage = 0;

        Map<String, int[]> satisfactionStats = new HashMap<>();
        Map<String, Integer> locationStats = new HashMap<>();

        String minDate = "9999-99-99";
        String maxDate = "0000-00-00";

        // Phân tích thông tin dự án từ tên file
        String fileName = new File(csvFilePath).getName();
        String platformStr = "Đa nền tảng";
        String keywordStr = "Không xác định";
        String[] nameParts = fileName.split("_");

        if (nameParts.length >= 2) {
            platformStr = nameParts[0].substring(0, 1).toUpperCase() + nameParts[0].substring(1);
            if (nameParts.length >= 3) {
                keywordStr = nameParts[1].replace("-", " ").replace("+", ", ");
            }
        }

        String projectName = keywordStr.contains(" ")
                ? keywordStr.substring(keywordStr.lastIndexOf(" ") + 1)
                : keywordStr;

        // 1. TIẾN HÀNH ĐỌC FILE CSV VÀ PHÂN TÍCH SÂU
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (columns.length >= 7) {
                    String type = columns[0].trim();

                    // 2. CHUẨN CONVENTION: Luôn dùng ngoặc nhọn {} cho if/else dù chỉ có 1 dòng lệnh
                    if (type.equalsIgnoreCase("POST")) {
                        totalPosts++;
                    }
                    if (type.equalsIgnoreCase("COMMENT")) {
                        totalComments++;
                    }

                    String dateStr = columns[3].replace("\"", "").trim();
                    if (!dateStr.isEmpty() && dateStr.length() >= 10) {
                        if (dateStr.compareTo(minDate) < 0) {
                            minDate = dateStr;
                        }
                        if (dateStr.compareTo(maxDate) > 0) {
                            maxDate = dateStr;
                        }
                    }

                    String content = columns[4].replace("\"", "").trim().toLowerCase();

                    // Phân tích cảm xúc
                    String sentiment = classifySentiment(content);
                    if (sentiment.equals("Tích cực")) {
                        posCount++;
                    } else if (sentiment.equals("Tiêu cực")) {
                        negCount++;
                    } else {
                        neuCount++;
                    }

                    // Phân tích danh mục thiệt hại
                    String damage = classifyDamage(content);
                    switch (damage) {
                        case "Con người (People)":
                            affectedPeople++;
                            break;
                        case "Hạ tầng (Infrastructure)":
                            infraDamage++;
                            break;
                        case "Nhà cửa (Housing)":
                            houseDamage++;
                            break;
                        case "Tài sản (Belongings)":
                            propertyLoss++;
                            break;
                        case "Kinh tế (Economy)":
                            economyDisruption++;
                            break;
                        default:
                            otherDamage++;
                            break;
                    }

                    // Phân tích hài lòng (Satisfaction)
                    String category = classifySatisfactionCategory(content);
                    if (category != null) {
                        int pos = 0;
                        int neg = 0;

                        for (String w : DictionaryConfig.POSITIVE_WORDS) {
                            int checkValue = checkKeyword(content, w);
                            if (checkValue == 1) {
                                pos++;
                            } else if (checkValue == -1) {
                                neg++;
                            }
                        }

                        for (String w : DictionaryConfig.NEGATIVE_WORDS) {
                            int checkValue = checkKeyword(content, w);
                            if (checkValue == 1) {
                                neg++;
                            } else if (checkValue == -1) {
                                pos++;
                            }
                        }

                        if (pos > 0 || neg > 0) {
                            int[] stats = satisfactionStats.getOrDefault(category, new int[]{0, 0});
                            stats[0] += pos;
                            stats[1] += neg;
                            satisfactionStats.put(category, stats);
                        }
                    }

                    // Phân tích điểm nóng (Hotspot Location)
                    boolean hasDamageContext = containsAny(content, DictionaryConfig.AFFECTED_PEOPLE)
                            || containsAny(content, DictionaryConfig.DAMAGED_INFRA)
                            || containsAny(content, DictionaryConfig.HOUSES_DAMAGED)
                            || containsAny(content, DictionaryConfig.NEGATIVE_WORDS);

                    if (hasDamageContext) {
                        for (Map.Entry<String, List<String>> entry : DictionaryConfig.getLocationKeywords().entrySet()) {
                            if (containsAny(content, entry.getValue())) {
                                locationStats.put(entry.getKey(), locationStats.getOrDefault(entry.getKey(), 0) + 1);
                            }
                        }
                    }
                }
            }
            System.out.println("[INFO] Đọc và phân tích CSV chuyên sâu hoàn tất.");

        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi trong quá trình đọc dữ liệu file CSV: " + e.getMessage());
            return;
        }

        if (minDate.equals("9999-99-99")) {
            minDate = "Không xác định";
        }
        if (maxDate.equals("0000-00-00")) {
            maxDate = "Không xác định";
        }

        int totalSentiments = posCount + negCount + neuCount;
        if (totalSentiments == 0) {
            totalSentiments = 1; // Tránh chia cho 0
        }

        // 2. GHI RA EXCEL THEO ĐỊNH DẠNG BÁO CÁO MỚI
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Báo Cáo Phân Tích Chuyên Sâu");

            // --- Styles ---
            CellStyle headerStyle = ExcelStyleUtils.createHeaderStyle(workbook);
            CellStyle cellStyle = ExcelStyleUtils.createCellStyle(workbook);

            int rowIndex = 0;

            // --- THÔNG TIN DỰ ÁN ---
            Row row = sheet.createRow(rowIndex++);
            Cell cell = row.createCell(0);
            cell.setCellValue("THÔNG TIN DỰ ÁN");
            cell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, 1));

            createReportRow(sheet, rowIndex++, "Tên thảm họa", projectName, cellStyle);
            createReportRow(sheet, rowIndex++, "Từ khóa", keywordStr, cellStyle);
            createReportRow(sheet, rowIndex++, "Khoảng thời gian", minDate + " đến " + maxDate, cellStyle);
            createReportRow(sheet, rowIndex++, "Nền tảng", platformStr, cellStyle);

            // --- THỐNG KÊ DỮ LIỆU ---
            row = sheet.createRow(rowIndex++);
            Cell cellStats = row.createCell(0);
            cellStats.setCellValue("THỐNG KÊ DỮ LIỆU");
            cellStats.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, 1));

            createReportRow(sheet, rowIndex++, "Tổng số bài viết", String.valueOf(totalPosts), cellStyle);
            createReportRow(sheet, rowIndex++, "Tổng số bình luận", String.valueOf(totalComments), cellStyle);

            // --- PHÂN BỐ CẢM XÚC ---
            row = sheet.createRow(rowIndex++);
            Cell cellSentiment = row.createCell(0);
            cellSentiment.setCellValue("PHÂN BỐ CẢM XÚC");
            cellSentiment.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, 1));

            createReportRow(sheet, rowIndex++, "Tích cực",
                    String.format("%d (%.1f%%)", posCount, posCount * 100.0 / totalSentiments), cellStyle);
            createReportRow(sheet, rowIndex++, "Trung lập",
                    String.format("%d (%.1f%%)", neuCount, neuCount * 100.0 / totalSentiments), cellStyle);
            createReportRow(sheet, rowIndex++, "Tiêu cực",
                    String.format("%d (%.1f%%)", negCount, negCount * 100.0 / totalSentiments), cellStyle);

            // --- PHÂN LOẠI THIỆT HẠI ---
            row = sheet.createRow(rowIndex++);
            Cell cellDamage = row.createCell(0);
            cellDamage.setCellValue("PHÂN LOẠI THIỆT HẠI");
            cellDamage.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, 1));

            createReportRow(sheet, rowIndex++, "Con người (People)", String.valueOf(affectedPeople), cellStyle);
            createReportRow(sheet, rowIndex++, "Kinh tế (Economy)", String.valueOf(economyDisruption), cellStyle);
            createReportRow(sheet, rowIndex++, "Nhà cửa (Housing)", String.valueOf(houseDamage), cellStyle);
            createReportRow(sheet, rowIndex++, "Tài sản (Belongings)", String.valueOf(propertyLoss), cellStyle);
            createReportRow(sheet, rowIndex++, "Hạ tầng (Infrastructure)", String.valueOf(infraDamage), cellStyle);

            // --- PHÂN LOẠI HÀI LÒNG ---
            row = sheet.createRow(rowIndex++);
            Cell cellSatis = row.createCell(0);
            cellSatis.setCellValue("PHÂN LOẠI HÀI LÒNG");
            cellSatis.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, 1));

            for (Map.Entry<String, int[]> entry : satisfactionStats.entrySet()) {
                String label = entry.getKey();
                int[] stats = entry.getValue();
                createReportRow(sheet, rowIndex++, label, "Tích cực: " + stats[0] + " | Tiêu cực: " + stats[1], cellStyle);
            }
            if (satisfactionStats.isEmpty()) {
                createReportRow(sheet, rowIndex++, "Không có dữ liệu", "-", cellStyle);
            }

            // --- ĐIỂM NÓNG THIÊN TAI ---
            row = sheet.createRow(rowIndex++);
            Cell cellHotspot = row.createCell(0);
            cellHotspot.setCellValue("ĐIỂM NÓNG THIÊN TAI");
            cellHotspot.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, 1));

            // Sắp xếp điểm nóng giảm dần
            List<Map.Entry<String, Integer>> sortedLocations = new ArrayList<>(locationStats.entrySet());
            sortedLocations.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

            for (Map.Entry<String, Integer> entry : sortedLocations) {
                createReportRow(sheet, rowIndex++, entry.getKey(), String.valueOf(entry.getValue()), cellStyle);
            }
            if (sortedLocations.isEmpty()) {
                createReportRow(sheet, rowIndex++, "Không có dữ liệu", "-", cellStyle);
            }

            // Tự động giãn cột cho đẹp
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            // Giãn thêm 1 chút cho đẹp mắt hơn
            sheet.setColumnWidth(0, sheet.getColumnWidth(0) + 1500);
            sheet.setColumnWidth(1, sheet.getColumnWidth(1) + 2500);

            // Xuất ra file
            try (FileOutputStream fileOut = new FileOutputStream(excelOutputPath)) {
                workbook.write(fileOut);
            }
            System.out.println("[SUCCESS] Đã xuất báo cáo phân tích sâu thành công tại: " + excelOutputPath);

        } catch (Exception e) {
            System.err.println("[ERROR] Gặp lỗi khi tạo lập file Excel: " + e.getMessage());
        }
    }

    private static void createReportRow(Sheet sheet, int rowIndex, String label, String value, CellStyle cellStyle) {
        Row row = sheet.createRow(rowIndex);

        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);

        if (cellStyle != null) {
            labelCell.setCellStyle(cellStyle);
            valueCell.setCellStyle(cellStyle);
        }
    }

    private static String classifySentiment(String content) {
        int pos = 0;
        int neg = 0;

        for (String w : DictionaryConfig.POSITIVE_WORDS) {
            if (content.contains(w)) {
                pos++;
            }
        }

        for (String w : DictionaryConfig.NEGATIVE_WORDS) {
            if (content.contains(w)) {
                neg++;
            }
        }

        if (pos > neg) {
            return "Tích cực";
        }
        if (neg > pos) {
            return "Tiêu cực";
        }
        return "Trung lập";
    }

    private static String classifyDamage(String content) {
        if (containsAny(content, DictionaryConfig.AFFECTED_PEOPLE)) {
            return "Con người (People)";
        }
        if (containsAny(content, DictionaryConfig.DAMAGED_INFRA)) {
            return "Hạ tầng (Infrastructure)";
        }
        if (containsAny(content, DictionaryConfig.HOUSES_DAMAGED)) {
            return "Nhà cửa (Housing)";
        }
        if (containsAny(content, DictionaryConfig.LOSS_BELONGINGS)) {
            return "Tài sản (Belongings)";
        }
        if (containsAny(content, DictionaryConfig.DISRUPTION_PRODUCTION)) {
            return "Kinh tế (Economy)";
        }
        return "Khác";
    }

    private static String classifySatisfactionCategory(String content) {
        for (Map.Entry<String, List<String>> entry : DictionaryConfig.getItemKeywords().entrySet()) {
            if (containsAny(content, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static int checkKeyword(String content, String word) {
        int index = content.indexOf(word);
        if (index != -1) {
            String prefix = content.substring(Math.max(0, index - 15), index);
            if (prefix.contains("không ") || prefix.contains("chẳng ") || prefix.contains("chưa ")) {
                return -1;
            }
            return 1;
        }
        return 0;
    }

    private static boolean containsAny(String text, List<String> keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) {
                return true;
            }
        }
        return false;
    }
}
package com.webshop.app.service;

import com.webshop.app.dao.InventoryDAO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class InventoryExcelService {

    public static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final DateTimeFormatter FILE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static final String RESTOCK_TEMPLATE_SHEET = "Danh sách nhập hàng";
    private static final String RESTOCK_RESULT_SHEET = "Kết quả nhập kho";

    private static final String[] RESTOCK_TEMPLATE_HEADERS = {
            "Mã SP",
            "Tên sản phẩm",
            "Danh mục",
            "Thương hiệu",
            "Tồn hiện tại",
            "Số lượng cần nhập",
            "Ghi chú"
    };

    private static final String[] RESTOCK_RESULT_HEADERS = {
            "Mã SP",
            "Tên sản phẩm",
            "Tồn trước nhập",
            "Số lượng nhập",
            "Tồn sau nhập",
            "Trạng thái"
    };

    public String buildRestockTemplateFileName() {
        return "inventory_restock_template_"
                + LocalDateTime.now().format(FILE_TIME_FORMATTER)
                + ".xlsx";
    }

    public String buildRestockResultFileName() {
        return "inventory_restock_result_"
                + LocalDateTime.now().format(FILE_TIME_FORMATTER)
                + ".xlsx";
    }

    public boolean isValidXlsxFileName(String fileName) {
        return fileName != null
                && fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx");
    }

    public byte[] createRestockTemplateFile(List<InventoryDAO.RestockProductRow> products) {
        List<InventoryDAO.RestockProductRow> safeProducts =
                products == null ? List.of() : products;

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(RESTOCK_TEMPLATE_SHEET);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle textStyle = createTextStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            createHeaderRow(sheet, RESTOCK_TEMPLATE_HEADERS, headerStyle);

            int rowIndex = 1;

            for (InventoryDAO.RestockProductRow product : safeProducts) {
                if (product == null) {
                    continue;
                }

                Row row = sheet.createRow(rowIndex++);

                createNumberCell(row, 0, product.productId(), numberStyle);
                createTextCell(row, 1, product.title(), textStyle);
                createTextCell(row, 2, product.categoryName(), textStyle);
                createTextCell(row, 3, product.brandName(), textStyle);
                createNumberCell(row, 4, product.currentStock(), numberStyle);
                createNumberCell(row, 5, product.quantity(), numberStyle);
                createTextCell(row, 6, product.note(), textStyle);
            }

            autosizeColumns(sheet, RESTOCK_TEMPLATE_HEADERS.length);

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("InventoryExcelService.createRestockTemplateFile error", e);
        }
    }

    public List<InventoryDAO.RestockImportRow> readRestockImportRows(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("File Excel không hợp lệ.");
        }

        List<InventoryDAO.RestockImportRow> rows = new ArrayList<>();

        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            if (workbook.getNumberOfSheets() <= 0) {
                return rows;
            }

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null || sheet.getLastRowNum() < 1) {
                return rows;
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row excelRow = sheet.getRow(rowIndex);

                if (excelRow == null || isRowEmpty(excelRow)) {
                    continue;
                }

                int productId = getIntCellValue(excelRow.getCell(0));
                int quantity = getIntCellValue(excelRow.getCell(5));
                String note = getStringCellValue(excelRow.getCell(6));

                rows.add(new InventoryDAO.RestockImportRow(productId, quantity, note));
            }

            return rows;

        } catch (IOException e) {
            throw new RuntimeException("InventoryExcelService.readRestockImportRows error", e);
        }
    }

    public byte[] createRestockResultFile(List<InventoryDAO.RestockImportResultRow> results) {
        List<InventoryDAO.RestockImportResultRow> safeResults =
                results == null ? List.of() : results;

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(RESTOCK_RESULT_SHEET);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle textStyle = createTextStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            createHeaderRow(sheet, RESTOCK_RESULT_HEADERS, headerStyle);

            int rowIndex = 1;

            for (InventoryDAO.RestockImportResultRow result : safeResults) {
                if (result == null) {
                    continue;
                }

                Row row = sheet.createRow(rowIndex++);

                createNumberCell(row, 0, result.productId(), numberStyle);
                createTextCell(row, 1, result.title(), textStyle);
                createNullableNumberCell(row, 2, result.beforeStock(), numberStyle, textStyle);
                createNumberCell(row, 3, result.quantity(), numberStyle);
                createNullableNumberCell(row, 4, result.afterStock(), numberStyle, textStyle);
                createTextCell(row, 5, result.status(), textStyle);
            }

            autosizeColumns(sheet, RESTOCK_RESULT_HEADERS.length);

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("InventoryExcelService.createRestockResultFile error", e);
        }
    }

    public void writeExcelResponse(
            HttpServletResponse response,
            byte[] fileContent,
            String fileName
    ) throws IOException {

        if (response == null) {
            throw new IllegalArgumentException("Response không hợp lệ.");
        }

        byte[] safeFileContent = fileContent == null ? new byte[0] : fileContent;
        String safeFileName = normalizeFileName(fileName);

        response.reset();
        response.setContentType(EXCEL_CONTENT_TYPE);
        response.setCharacterEncoding("UTF-8");
        response.setContentLength(safeFileContent.length);

        String encodedFileName = URLEncoder.encode(safeFileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" + safeFileName + "\"; filename*=UTF-8''" + encodedFileName
        );

        response.getOutputStream().write(safeFileContent);
        response.flushBuffer();
    }

    private void createHeaderRow(Sheet sheet, String[] headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        setThinBorder(style);

        return style;
    }

    private CellStyle createTextStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        setThinBorder(style);

        return style;
    }

    private CellStyle createNumberStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setThinBorder(style);

        return style;
    }

    private void setThinBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    private void createTextCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private void createNumberCell(Row row, int columnIndex, int value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(Math.max(value, 0));
        cell.setCellStyle(style);
    }

    private void createNullableNumberCell(
            Row row,
            int columnIndex,
            Integer value,
            CellStyle numberStyle,
            CellStyle textStyle
    ) {
        Cell cell = row.createCell(columnIndex);

        if (value == null) {
            cell.setCellValue("-");
            cell.setCellStyle(textStyle);
            return;
        }

        cell.setCellValue(Math.max(value, 0));
        cell.setCellStyle(numberStyle);
    }

    private void autosizeColumns(Sheet sheet, int totalColumns) {
        for (int i = 0; i < totalColumns; i++) {
            sheet.autoSizeColumn(i);

            int currentWidth = sheet.getColumnWidth(i);
            int minWidth = 14 * 256;
            int maxWidth = 48 * 256;

            if (currentWidth < minWidth) {
                sheet.setColumnWidth(i, minWidth);
            } else if (currentWidth > maxWidth) {
                sheet.setColumnWidth(i, maxWidth);
            }
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        DataFormatter formatter = new DataFormatter();

        for (int columnIndex = 0; columnIndex <= 6; columnIndex++) {
            Cell cell = row.getCell(columnIndex);

            if (cell != null && !formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private int getIntCellValue(Cell cell) {
        if (cell == null) {
            return 0;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return Math.max((int) Math.round(cell.getNumericCellValue()), 0);
        }

        String text = getStringCellValue(cell)
                .replace(" ", "")
                .replace(",", "")
                .replace(".", "")
                .trim();

        if (text.isEmpty()) {
            return 0;
        }

        try {
            return Math.max(Integer.parseInt(text), 0);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        return new DataFormatter().formatCellValue(cell).trim();
    }

    private String normalizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "inventory_restock.xlsx";
        }

        String safeFileName = fileName.trim()
                .replace("\\", "_")
                .replace("/", "_")
                .replace("\r", "")
                .replace("\n", "");

        if (!safeFileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            safeFileName += ".xlsx";
        }

        return safeFileName;
    }
}
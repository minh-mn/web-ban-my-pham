package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.InventoryDAO;
import com.webshop.app.model.InventorySummary;
import com.webshop.app.model.User;
import com.webshop.app.service.AuditLogService;
import com.webshop.app.utils.DBConnection;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

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

@WebServlet("/admin/inventory")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 10L * 1024 * 1024,
        maxRequestSize = 20L * 1024 * 1024
)
public class AdminInventoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP_INVENTORY = "/jsp/admin/inventory/inventory.jsp";

    private static final String ACTION_ADD_STOCK = "addStock";
    private static final String ACTION_ADD_VARIANT_STOCK = "addVariantStock";
    private static final String ACTION_UPDATE_VARIANT_MIN_STOCK = "updateVariantMinStock";
    private static final String ACTION_EXPORT_RESTOCK_EXCEL = "exportRestockExcel";
    private static final String ACTION_IMPORT_RESTOCK_EXCEL = "importRestockExcel";

    private static final DateTimeFormatter FILE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String keyword = req.getParameter("keyword");
        String status = req.getParameter("status");
        String variantKeyword = req.getParameter("variantKeyword");
        String variantStatus = req.getParameter("variantStatus");

        LocalDate today = LocalDate.now();
        int selectedImportMonth = parseInt(req.getParameter("importMonth"), today.getMonthValue());
        int selectedImportYear = parseInt(req.getParameter("importYear"), today.getYear());
        String importKeyword = req.getParameter("importKeyword");

        if (selectedImportMonth < 1 || selectedImportMonth > 12) {
            selectedImportMonth = today.getMonthValue();
        }

        if (selectedImportYear < 2000) {
            selectedImportYear = today.getYear();
        }

        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);

        req.setAttribute("summary", inventoryDAO.getSummary());
        req.setAttribute("products", inventoryDAO.findInventoryProducts(keyword, status));
        req.setAttribute("lowStockAlerts", inventoryDAO.lowStockAlerts(8));
        req.setAttribute("variantKeyword", variantKeyword);
        req.setAttribute("variantStatus", variantStatus);
        req.setAttribute("variantSummary", inventoryDAO.getVariantSummary());
        req.setAttribute("variantProducts", inventoryDAO.findInventoryVariants(variantKeyword, variantStatus));
        req.setAttribute("lowStockVariantAlerts", inventoryDAO.lowStockVariantAlerts(8));
        req.setAttribute("recentActivities", inventoryDAO.recentStockActivities(12));

        req.setAttribute("last7DaysExportLabelsJson",
                toJsonStringArray(inventoryDAO.last7DaysExportLabels()));
        req.setAttribute("last7DaysExportValuesJson",
                toJsonNumberArray(inventoryDAO.last7DaysExportValues()));

        /*
         * ISSUE 128 - Lịch sử nhập hàng
         * Các method này sẽ được bổ sung trong InventoryDAO ở bước tiếp theo:
         * - getImportSummary(month, year)
         * - findImportHistory(month, year, keyword)
         * - monthlyImportStatLabels(year)
         * - monthlyImportStatValues(year)
         * - importYearOptions()
         */
        req.setAttribute("selectedImportMonth", selectedImportMonth);
        req.setAttribute("selectedImportYear", selectedImportYear);
        req.setAttribute("importKeyword", importKeyword);

        req.setAttribute("importSummary",
                inventoryDAO.getImportSummary(selectedImportMonth, selectedImportYear));

        req.setAttribute("importHistory",
                inventoryDAO.findImportHistory(selectedImportMonth, selectedImportYear, importKeyword));

        req.setAttribute("importYearOptions", inventoryDAO.importYearOptions());

        req.setAttribute("importMonthStatsLabelsJson",
                toJsonStringArray(inventoryDAO.monthlyImportStatLabels(selectedImportYear)));

        req.setAttribute("importMonthStatsValuesJson",
                toJsonNumberArray(inventoryDAO.monthlyImportStatValues(selectedImportYear)));

        req.getRequestDispatcher(JSP_INVENTORY).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");

        if (ACTION_ADD_STOCK.equalsIgnoreCase(action)) {
            handleAddStock(req, resp);
            return;
        }

        if (ACTION_ADD_VARIANT_STOCK.equalsIgnoreCase(action)) {
            handleAddVariantStock(req, resp);
            return;
        }

        if (ACTION_UPDATE_VARIANT_MIN_STOCK.equalsIgnoreCase(action)) {
            handleUpdateVariantMinStock(req, resp);
            return;
        }

        if (ACTION_EXPORT_RESTOCK_EXCEL.equalsIgnoreCase(action)) {
            handleExportRestockExcel(req, resp);
            return;
        }

        if (ACTION_IMPORT_RESTOCK_EXCEL.equalsIgnoreCase(action)) {
            handleImportRestockExcel(req, resp);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/inventory");
    }

    private void handleAddStock(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int productId = parseInt(req.getParameter("productId"), 0);
        int quantity = parseInt(req.getParameter("quantity"), 0);
        String note = req.getParameter("note");
        Integer adminUserId = getCurrentAdminUserId(req);

        try {
            inventoryDAO.addStock(productId, quantity, note, adminUserId);

            String productName = findProductNameForLog(productId);
            AuditLogService.logImport(
                    req,
                    "INVENTORY",
                    "Product",
                    productId,
                    productName,
                    "Đã nhập thêm " + quantity + " sản phẩm vào kho"
                            + (productName == null || productName.isBlank() ? " #" + productId : ": " + productName) + ".",
                    AuditLogService.changes(
                            "Sản phẩm: " + (productName == null || productName.isBlank() ? "#" + productId : productName),
                            "Số lượng nhập: " + quantity,
                            "Ghi chú: " + normalizeNote(note, "Nhập kho thủ công")
                    )
            );

            resp.sendRedirect(req.getContextPath()
                    + "/admin/inventory?success=stock_added");

        } catch (IllegalArgumentException ex) {
            redirectInventoryError(req, resp, ex.getMessage());

        } catch (RuntimeException ex) {
            throw new ServletException("AdminInventoryServlet.addStock error", ex);
        }
    }

    private void handleAddVariantStock(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int productId = parseInt(req.getParameter("productId"), 0);
        int variantId = parseInt(req.getParameter("variantId"), 0);
        int quantity = parseInt(req.getParameter("quantity"), 0);
        String note = req.getParameter("note");
        Integer adminUserId = getCurrentAdminUserId(req);

        try {
            InventoryDAO.VariantStockChange change = inventoryDAO.addVariantStock(
                    productId,
                    variantId,
                    quantity,
                    note,
                    adminUserId
            );

            AuditLogService.logImport(
                    req,
                    "INVENTORY",
                    "ProductVariant",
                    change.variantId(),
                    change.productTitle() + " - " + change.variantName(),
                    "Đã nhập thêm " + change.quantity()
                            + " sản phẩm cho biến thể " + change.variantName()
                            + " của " + change.productTitle() + ".",
                    AuditLogService.changes(
                            "SKU: " + change.sku(),
                            "Sản phẩm: " + change.productTitle(),
                            "Biến thể: " + change.variantName(),
                            "Tồn kho: " + change.beforeStock() + " -> " + change.afterStock(),
                            "Số lượng nhập: " + change.quantity(),
                            "Mức cảnh báo: " + change.minStock(),
                            "Ghi chú: " + normalizeNote(note, "Nhập kho biến thể thủ công")
                    )
            );

            resp.sendRedirect(req.getContextPath()
                    + "/admin/inventory?success=variant_stock_added#variantInventory");

        } catch (IllegalArgumentException ex) {
            redirectInventoryError(req, resp, ex.getMessage());

        } catch (RuntimeException ex) {
            throw new ServletException("AdminInventoryServlet.addVariantStock error", ex);
        }
    }

    private void handleUpdateVariantMinStock(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int productId = parseInt(req.getParameter("productId"), 0);
        int variantId = parseInt(req.getParameter("variantId"), 0);
        int minStock = parseInt(req.getParameter("minStock"), 0);

        try {
            InventoryDAO.VariantMinStockChange change = inventoryDAO.updateVariantMinStock(
                    productId,
                    variantId,
                    minStock
            );

            AuditLogService.logUpdate(
                    req,
                    "INVENTORY",
                    "ProductVariant",
                    change.variantId(),
                    change.productTitle() + " - " + change.variantName(),
                    "Đã cập nhật mức cảnh báo tồn kho cho biến thể "
                            + change.variantName() + " của " + change.productTitle() + ".",
                    AuditLogService.changes(
                            "SKU: " + change.sku(),
                            "Mức cảnh báo cũ: " + change.oldMinStock()
                    ),
                    AuditLogService.changes(
                            "SKU: " + change.sku(),
                            "Mức cảnh báo mới: " + change.newMinStock()
                    )
            );

            resp.sendRedirect(req.getContextPath()
                    + "/admin/inventory?success=variant_min_stock_updated#variantInventory");

        } catch (IllegalArgumentException ex) {
            redirectInventoryError(req, resp, ex.getMessage());

        } catch (RuntimeException ex) {
            throw new ServletException("AdminInventoryServlet.updateVariantMinStock error", ex);
        }
    }

    private void handleExportRestockExcel(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String[] selectedIds = req.getParameterValues("selectedProductIds");

        if (selectedIds == null || selectedIds.length == 0) {
            redirectInventoryError(req, resp, "Vui lòng tích chọn ít nhất một sản phẩm cần nhập thêm.");
            return;
        }

        List<RestockExportRow> exportRows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            for (String rawId : selectedIds) {
                int productId = parseInt(rawId, 0);

                if (productId <= 0) {
                    continue;
                }

                int quantity = getProductQuantityFromRequest(req, productId);
                String note = getProductNoteFromRequest(req, productId);

                if (quantity <= 0) {
                    continue;
                }

                RestockExportRow row = findProductForExport(conn, productId, quantity, note);

                if (row != null) {
                    exportRows.add(row);
                }
            }

        } catch (SQLException e) {
            throw new ServletException("AdminInventoryServlet.exportRestockExcel query error", e);
        }

        if (exportRows.isEmpty()) {
            redirectInventoryError(req, resp,
                    "Không có sản phẩm hợp lệ để xuất Excel. Vui lòng chọn sản phẩm và nhập số lượng lớn hơn 0.");
            return;
        }

        String fileName = "inventory_restock_template_"
                + LocalDateTime.now().format(FILE_TIME_FORMATTER)
                + ".xlsx";

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            buildRestockTemplateWorkbook(workbook, exportRows);
            writeWorkbookResponse(resp, workbook, fileName);

        } catch (Exception e) {
            throw new ServletException("AdminInventoryServlet.exportRestockExcel error", e);
        }
    }

    private void handleImportRestockExcel(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        boolean ajaxRequest = isAjaxRequest(req);
        Part filePart = req.getPart("restockFile");

        if (filePart == null || filePart.getSize() <= 0) {
            if (ajaxRequest) {
                writeInventoryAjaxError(resp, "Vui lòng chọn file Excel nhập hàng.");
                return;
            }

            redirectInventoryError(req, resp, "Vui lòng chọn file Excel nhập hàng.");
            return;
        }

        String submittedFileName = filePart.getSubmittedFileName();

        if (submittedFileName == null || !submittedFileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            if (ajaxRequest) {
                writeInventoryAjaxError(resp, "File nhập hàng phải có định dạng .xlsx.");
                return;
            }

            redirectInventoryError(req, resp, "File nhập hàng phải có định dạng .xlsx.");
            return;
        }

        Integer adminUserId = getCurrentAdminUserId(req);
        List<RestockImportResultRow> resultRows;

        try (XSSFWorkbook workbook = new XSSFWorkbook(filePart.getInputStream())) {
            resultRows = processRestockWorkbook(workbook, adminUserId);

            logRestockImport(req, resultRows, submittedFileName);

        } catch (Exception e) {
            if (ajaxRequest) {
                writeInventoryAjaxError(resp, "Không thể đọc file Excel. Vui lòng kiểm tra lại mẫu file nhập hàng.");
                return;
            }

            throw new ServletException("AdminInventoryServlet.importRestockExcel read error", e);
        }

        if (ajaxRequest) {
            writeRestockImportJsonResponse(req, resp, resultRows, submittedFileName);
            return;
        }

        String fileName = "inventory_restock_result_"
                + LocalDateTime.now().format(FILE_TIME_FORMATTER)
                + ".xlsx";

        try (XSSFWorkbook resultWorkbook = new XSSFWorkbook()) {
            buildRestockResultWorkbook(resultWorkbook, resultRows);
            writeWorkbookResponse(resp, resultWorkbook, fileName);

        } catch (Exception e) {
            throw new ServletException("AdminInventoryServlet.importRestockExcel result error", e);
        }
    }

    private boolean isAjaxRequest(HttpServletRequest req) {
        String requestedWith = req.getHeader("X-Requested-With");
        String accept = req.getHeader("Accept");
        String ajax = req.getParameter("ajax");

        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || "true".equalsIgnoreCase(ajax)
                || (accept != null && accept.toLowerCase(Locale.ROOT).contains("application/json"));
    }

    private void writeInventoryAjaxError(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        writeJsonResponse(resp, "{\"ok\":false,\"message\":" + jsonString(message) + "}");
    }

    private void writeRestockImportJsonResponse(
            HttpServletRequest req,
            HttpServletResponse resp,
            List<RestockImportResultRow> resultRows,
            String submittedFileName
    ) throws IOException {

        int successCount = 0;
        int errorCount = 0;
        int totalQuantity = 0;

        StringBuilder updatesJson = new StringBuilder("[");
        StringBuilder errorsJson = new StringBuilder("[");

        if (resultRows != null) {
            boolean firstUpdate = true;
            boolean firstError = true;

            for (RestockImportResultRow row : resultRows) {
                if (row == null) {
                    continue;
                }

                boolean success = "Thành công".equalsIgnoreCase(row.status());

                if (success) {
                    successCount++;
                    totalQuantity += Math.max(row.quantity(), 0);

                    if (!firstUpdate) {
                        updatesJson.append(',');
                    }

                    int afterStock = row.afterStock() == null ? 0 : Math.max(row.afterStock(), 0);

                    updatesJson.append('{')
                            .append("\"productId\":").append(Math.max(row.productId(), 0)).append(',')
                            .append("\"title\":").append(jsonString(row.title())).append(',')
                            .append("\"beforeStock\":").append(row.beforeStock() == null ? "null" : Math.max(row.beforeStock(), 0)).append(',')
                            .append("\"quantity\":").append(Math.max(row.quantity(), 0)).append(',')
                            .append("\"afterStock\":").append(afterStock).append(',')
                            .append("\"stockStatusLabel\":").append(jsonString(stockStatusLabel(afterStock))).append(',')
                            .append("\"stockStatusClass\":").append(jsonString(stockStatusClass(afterStock))).append(',')
                            .append("\"status\":").append(jsonString(row.status()))
                            .append('}');

                    firstUpdate = false;

                } else {
                    errorCount++;

                    if (!firstError) {
                        errorsJson.append(',');
                    }

                    errorsJson.append('{')
                            .append("\"productId\":").append(Math.max(row.productId(), 0)).append(',')
                            .append("\"title\":").append(jsonString(row.title())).append(',')
                            .append("\"quantity\":").append(Math.max(row.quantity(), 0)).append(',')
                            .append("\"status\":").append(jsonString(row.status()))
                            .append('}');

                    firstError = false;
                }
            }
        }

        updatesJson.append(']');
        errorsJson.append(']');

        InventorySummary summary = inventoryDAO.getSummary();
        LocalDate today = LocalDate.now();
        InventoryDAO.ImportSummary importSummary = inventoryDAO.getImportSummary(
                today.getMonthValue(),
                today.getYear()
        );

        String message;

        if (successCount > 0) {
            message = "Đã nhập kho từ Excel: " + successCount
                    + " dòng thành công, tổng +" + totalQuantity + " sản phẩm.";

            if (errorCount > 0) {
                message += " Có " + errorCount + " dòng cần kiểm tra.";
            }
        } else {
            message = "File Excel chưa có dòng nhập kho thành công. Vui lòng kiểm tra dữ liệu trong file.";
        }

        String json = "{"
                + "\"ok\":" + (successCount > 0) + ","
                + "\"message\":" + jsonString(message) + ","
                + "\"fileName\":" + jsonString(submittedFileName) + ","
                + "\"successCount\":" + successCount + ","
                + "\"errorCount\":" + errorCount + ","
                + "\"totalQuantity\":" + totalQuantity + ","
                + "\"productUpdates\":" + updatesJson + ","
                + "\"errors\":" + errorsJson + ","
                + "\"summary\":{"
                + "\"productCount\":" + summary.getProductCount() + ","
                + "\"totalStock\":" + summary.getTotalStock() + ","
                + "\"lowStockCount\":" + summary.getLowStockCount() + ","
                + "\"outOfStockCount\":" + summary.getOutOfStockCount() + ","
                + "\"normalStockCount\":" + summary.getNormalStockCount() + ","
                + "\"alertCount\":" + summary.getAlertCount()
                + "},"
                + "\"importSummary\":{"
                + "\"monthlyImportQuantity\":" + importSummary.getMonthlyImportQuantity() + ","
                + "\"monthlyImportCount\":" + importSummary.getMonthlyImportCount() + ","
                + "\"monthlyProductCount\":" + importSummary.getMonthlyProductCount() + ","
                + "\"yearlyImportQuantity\":" + importSummary.getYearlyImportQuantity() + ","
                + "\"yearlyImportCount\":" + importSummary.getYearlyImportCount() + ","
                + "\"yearlyProductCount\":" + importSummary.getYearlyProductCount()
                + "}"
                + "}";

        writeJsonResponse(resp, json);
    }

    private void writeJsonResponse(HttpServletResponse resp, String json) throws IOException {
        resp.reset();
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(json == null ? "{}" : json);
        resp.flushBuffer();
    }

    private String jsonString(String value) {
        return "\"" + escapeJson(value == null ? "" : value) + "\"";
    }

    private String stockStatusLabel(int stock) {
        if (stock <= 0) {
            return "Hết hàng";
        }

        if (stock < 10) {
            return "Sắp hết hàng";
        }

        return "Còn hàng";
    }

    private String stockStatusClass(int stock) {
        if (stock <= 0) {
            return "stock-out";
        }

        if (stock < 10) {
            return "stock-low";
        }

        return "stock-normal";
    }

    private List<RestockImportResultRow> processRestockWorkbook(
            XSSFWorkbook workbook,
            Integer adminUserId
    ) throws SQLException {

        List<RestockImportResultRow> resultRows = new ArrayList<>();

        if (workbook.getNumberOfSheets() <= 0) {
            resultRows.add(RestockImportResultRow.error(0, "", null, 0, null,
                    "Lỗi: File Excel không có sheet dữ liệu."));
            return resultRows;
        }

        Sheet sheet = workbook.getSheetAt(0);

        if (sheet == null || sheet.getLastRowNum() < 1) {
            resultRows.add(RestockImportResultRow.error(0, "", null, 0, null,
                    "Lỗi: File Excel chưa có dữ liệu nhập hàng."));
            return resultRows;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row excelRow = sheet.getRow(rowIndex);

                    if (excelRow == null || isRowEmpty(excelRow)) {
                        continue;
                    }

                    int productId = getIntCellValue(excelRow.getCell(0));
                    int quantity = getIntCellValue(excelRow.getCell(5));
                    String note = getStringCellValue(excelRow.getCell(6));

                    if (productId <= 0) {
                        resultRows.add(RestockImportResultRow.error(
                                0,
                                "",
                                null,
                                quantity,
                                null,
                                "Lỗi dòng " + (rowIndex + 1) + ": Mã sản phẩm không hợp lệ."
                        ));
                        continue;
                    }

                    if (quantity <= 0) {
                        ProductStockSnapshot product = findProductForUpdate(conn, productId);

                        resultRows.add(RestockImportResultRow.error(
                                productId,
                                product == null ? "" : product.title(),
                                product == null ? null : product.stock(),
                                quantity,
                                product == null ? null : product.stock(),
                                "Lỗi dòng " + (rowIndex + 1) + ": Số lượng nhập phải lớn hơn 0."
                        ));
                        continue;
                    }

                    ProductStockSnapshot product = findProductForUpdate(conn, productId);

                    if (product == null) {
                        resultRows.add(RestockImportResultRow.error(
                                productId,
                                "",
                                null,
                                quantity,
                                null,
                                "Lỗi dòng " + (rowIndex + 1) + ": Không tìm thấy sản phẩm."
                        ));
                        continue;
                    }

                    int beforeStock = product.stock();
                    int afterStock = beforeStock + quantity;

                    updateProductStock(conn, productId, afterStock);
                    insertInventoryMovement(
                            conn,
                            productId,
                            quantity,
                            beforeStock,
                            afterStock,
                            normalizeNote(note, "Nhập kho từ file Excel"),
                            adminUserId
                    );

                    resultRows.add(RestockImportResultRow.success(
                            productId,
                            product.title(),
                            beforeStock,
                            quantity,
                            afterStock
                    ));
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;

            } finally {
                conn.setAutoCommit(true);
            }
        }

        if (resultRows.isEmpty()) {
            resultRows.add(RestockImportResultRow.error(0, "", null, 0, null,
                    "Lỗi: File Excel không có dòng dữ liệu hợp lệ."));
        }

        return resultRows;
    }

    private RestockExportRow findProductForExport(
            Connection conn,
            int productId,
            int quantity,
            String note
    ) throws SQLException {

        String sql = """
                SELECT
                    p.id,
                    p.title,
                    COALESCE(c.name, 'Chưa phân loại') AS category_name,
                    COALESCE(b.name, 'Chưa có thương hiệu') AS brand_name,
                    p.stock
                FROM store_product p
                LEFT JOIN store_category c ON c.id = p.category_id
                LEFT JOIN store_brand b ON b.id = p.brand_id
                WHERE p.id = ?
                LIMIT 1
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return new RestockExportRow(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("category_name"),
                        rs.getString("brand_name"),
                        rs.getInt("stock"),
                        quantity,
                        note
                );
            }
        }
    }

    private ProductStockSnapshot findProductForUpdate(Connection conn, int productId) throws SQLException {
        String sql = """
                SELECT id, title, stock
                FROM store_product
                WHERE id = ?
                FOR UPDATE
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return new ProductStockSnapshot(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getInt("stock")
                );
            }
        }
    }

    private void updateProductStock(Connection conn, int productId, int afterStock) throws SQLException {
        String sql = """
                UPDATE store_product
                SET stock = ?
                WHERE id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(afterStock, 0));
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    private void insertInventoryMovement(
            Connection conn,
            int productId,
            int quantity,
            int beforeStock,
            int afterStock,
            String note,
            Integer adminUserId
    ) throws SQLException {

        String sql = """
                INSERT INTO inventory_movement
                (
                    product_id,
                    movement_type,
                    quantity,
                    before_stock,
                    after_stock,
                    reference_type,
                    reference_id,
                    note,
                    created_by
                )
                VALUES (?, 'IN', ?, ?, ?, 'EXCEL_IMPORT', NULL, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, quantity);
            ps.setInt(3, beforeStock);
            ps.setInt(4, afterStock);
            ps.setString(5, note);

            if (adminUserId == null || adminUserId <= 0) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, adminUserId);
            }

            ps.executeUpdate();
        }
    }

    private void buildRestockTemplateWorkbook(
            XSSFWorkbook workbook,
            List<RestockExportRow> rows
    ) {

        Sheet sheet = workbook.createSheet("Danh sách nhập hàng");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle textStyle = createTextStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);

        String[] headers = {
                "Mã SP",
                "Tên sản phẩm",
                "Danh mục",
                "Thương hiệu",
                "Tồn hiện tại",
                "Số lượng cần nhập",
                "Ghi chú"
        };

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIndex = 1;

        for (RestockExportRow data : rows) {
            Row row = sheet.createRow(rowIndex++);

            createNumberCell(row, 0, data.productId(), numberStyle);
            createTextCell(row, 1, data.title(), textStyle);
            createTextCell(row, 2, data.categoryName(), textStyle);
            createTextCell(row, 3, data.brandName(), textStyle);
            createNumberCell(row, 4, data.currentStock(), numberStyle);
            createNumberCell(row, 5, data.quantity(), numberStyle);
            createTextCell(row, 6, data.note(), textStyle);
        }

        autosizeColumns(sheet, headers.length);
    }

    private void buildRestockResultWorkbook(
            XSSFWorkbook workbook,
            List<RestockImportResultRow> rows
    ) {

        Sheet sheet = workbook.createSheet("Kết quả nhập kho");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle textStyle = createTextStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);

        String[] headers = {
                "Mã SP",
                "Tên sản phẩm",
                "Tồn trước nhập",
                "Số lượng nhập",
                "Tồn sau nhập",
                "Trạng thái"
        };

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIndex = 1;

        for (RestockImportResultRow data : rows) {
            Row row = sheet.createRow(rowIndex++);

            createNumberCell(row, 0, data.productId(), numberStyle);
            createTextCell(row, 1, data.title(), textStyle);
            createNullableNumberCell(row, 2, data.beforeStock(), numberStyle, textStyle);
            createNumberCell(row, 3, data.quantity(), numberStyle);
            createNullableNumberCell(row, 4, data.afterStock(), numberStyle, textStyle);
            createTextCell(row, 5, data.status(), textStyle);
        }

        autosizeColumns(sheet, headers.length);
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
        setThinBorder(style);

        return style;
    }

    private CellStyle createTextStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
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
        } else {
            cell.setCellValue(Math.max(value, 0));
            cell.setCellStyle(numberStyle);
        }
    }

    private void autosizeColumns(Sheet sheet, int totalColumns) {
        for (int i = 0; i < totalColumns; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            int minWidth = 14 * 256;
            int maxWidth = 42 * 256;

            if (currentWidth < minWidth) {
                sheet.setColumnWidth(i, minWidth);
            } else if (currentWidth > maxWidth) {
                sheet.setColumnWidth(i, maxWidth);
            }
        }
    }

    private void writeWorkbookResponse(
            HttpServletResponse resp,
            XSSFWorkbook workbook,
            String fileName
    ) throws IOException {

        resp.reset();
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setCharacterEncoding("UTF-8");

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        resp.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName);

        workbook.write(resp.getOutputStream());
        resp.flushBuffer();
    }

    private int getProductQuantityFromRequest(HttpServletRequest req, int productId) {
        String value = firstNonBlank(
                req.getParameter("importQuantities_" + productId),
                req.getParameter("importQuantities[" + productId + "]"),
                req.getParameter("quantity_" + productId),
                req.getParameter("quantity[" + productId + "]")
        );

        return parseInt(value, 0);
    }

    private String getProductNoteFromRequest(HttpServletRequest req, int productId) {
        return firstNonBlank(
                req.getParameter("importNotes_" + productId),
                req.getParameter("importNotes[" + productId + "]"),
                req.getParameter("note_" + productId),
                req.getParameter("note[" + productId + "]")
        );
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        DataFormatter formatter = new DataFormatter();

        for (int i = 0; i <= 6; i++) {
            Cell cell = row.getCell(i);

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
                .replace(",", "")
                .replace(".", "")
                .trim();

        return parseInt(text, 0);
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        return new DataFormatter().formatCellValue(cell).trim();
    }

    private String toJsonStringArray(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder("[");

        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }

            builder.append('"')
                    .append(escapeJson(values.get(i)))
                    .append('"');
        }

        builder.append(']');
        return builder.toString();
    }

    private String toJsonNumberArray(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder("[");

        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }

            Integer value = values.get(i);
            builder.append(value == null ? 0 : Math.max(value, 0));
        }

        builder.append(']');
        return builder.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private void logRestockImport(HttpServletRequest req,
                                  List<RestockImportResultRow> resultRows,
                                  String fileName) {
        if (resultRows == null || resultRows.isEmpty()) {
            return;
        }

        int successCount = 0;
        int totalQuantity = 0;
        List<String> detailLines = new ArrayList<>();

        for (RestockImportResultRow row : resultRows) {
            if (row == null || !"Thành công".equalsIgnoreCase(row.status())) {
                continue;
            }

            successCount++;
            totalQuantity += Math.max(row.quantity(), 0);

            if (detailLines.size() < 30) {
                detailLines.add("#" + row.productId()
                        + " - " + row.title()
                        + ": " + row.beforeStock()
                        + " -> " + row.afterStock()
                        + " (+" + row.quantity() + ")");
            }
        }

        if (successCount <= 0) {
            return;
        }

        AuditLogService.logImport(
                req,
                "INVENTORY",
                "ExcelImport",
                null,
                fileName,
                "Đã nhập kho từ file Excel " + fileName + ": "
                        + successCount + " dòng thành công, tổng " + totalQuantity + " sản phẩm.",
                AuditLogService.changes(
                        "File: " + fileName,
                        "Số dòng thành công: " + successCount,
                        "Tổng số lượng nhập: " + totalQuantity,
                        String.join("\n", detailLines)
                )
        );
    }

    private String findProductNameForLog(int productId) {
        if (productId <= 0) {
            return null;
        }

        String sql = "SELECT title FROM store_product WHERE id = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("title");
                }
            }

        } catch (RuntimeException | SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Integer getCurrentAdminUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);

        if (session == null) {
            return null;
        }

        Object user = session.getAttribute("user");

        if (user instanceof User currentUser && currentUser.getId() > 0) {
            return currentUser.getId();
        }

        return null;
    }

    private void redirectInventoryError(
            HttpServletRequest req,
            HttpServletResponse resp,
            String message
    ) throws IOException {

        resp.sendRedirect(req.getContextPath()
                + "/admin/inventory?error="
                + URLEncoder.encode(message == null ? "Có lỗi xảy ra." : message, StandardCharsets.UTF_8));
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }

        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }

        return "";
    }

    private String normalizeNote(String note, String defaultNote) {
        if (note == null || note.trim().isEmpty()) {
            return defaultNote;
        }

        return note.trim();
    }

    private record RestockExportRow(
            int productId,
            String title,
            String categoryName,
            String brandName,
            int currentStock,
            int quantity,
            String note
    ) {
    }

    private record ProductStockSnapshot(
            int productId,
            String title,
            int stock
    ) {
    }

    private record RestockImportResultRow(
            int productId,
            String title,
            Integer beforeStock,
            int quantity,
            Integer afterStock,
            String status
    ) {

        private static RestockImportResultRow success(
                int productId,
                String title,
                int beforeStock,
                int quantity,
                int afterStock
        ) {
            return new RestockImportResultRow(
                    productId,
                    title,
                    beforeStock,
                    quantity,
                    afterStock,
                    "Thành công"
            );
        }

        private static RestockImportResultRow error(
                int productId,
                String title,
                Integer beforeStock,
                int quantity,
                Integer afterStock,
                String status
        ) {
            return new RestockImportResultRow(
                    productId,
                    title,
                    beforeStock,
                    Math.max(quantity, 0),
                    afterStock,
                    status
            );
        }
    }
}

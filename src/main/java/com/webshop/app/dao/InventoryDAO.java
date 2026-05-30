package com.webshop.app.dao;

import com.webshop.app.model.InventoryMovementView;
import com.webshop.app.model.InventoryProductStat;
import com.webshop.app.model.InventorySummary;
import com.webshop.app.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    private static final int LOW_STOCK_THRESHOLD = InventoryProductStat.LOW_STOCK_THRESHOLD;

    private static final String VALID_EXPORTED_ORDER_CONDITION = """
            LOWER(COALESCE(o.status, '')) NOT IN ('cancelled', 'canceled')
            AND (
                COALESCE(o.stock_deducted, 0) = 1
                OR UPPER(COALESCE(o.payment_status, '')) = 'PAID'
                OR LOWER(COALESCE(o.status, '')) IN ('confirmed', 'shipping', 'completed')
            )
            """;

    public InventorySummary getSummary() {
        InventorySummary summary = new InventorySummary();

        String stockSql = """
                SELECT
                    COUNT(*) AS product_count,
                    COALESCE(SUM(stock), 0) AS total_stock,
                    COALESCE(SUM(CASE WHEN stock <= 0 THEN 1 ELSE 0 END), 0) AS out_of_stock_count,
                    COALESCE(SUM(CASE WHEN stock > 0 AND stock < ? THEN 1 ELSE 0 END), 0) AS low_stock_count,
                    COALESCE(SUM(CASE WHEN stock >= ? THEN 1 ELSE 0 END), 0) AS normal_stock_count
                FROM store_product
                WHERE is_active = 1
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(stockSql)) {

            ps.setInt(1, LOW_STOCK_THRESHOLD);
            ps.setInt(2, LOW_STOCK_THRESHOLD);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    summary.setProductCount(rs.getInt("product_count"));
                    summary.setTotalStock(rs.getInt("total_stock"));
                    summary.setOutOfStockCount(rs.getInt("out_of_stock_count"));
                    summary.setLowStockCount(rs.getInt("low_stock_count"));
                    summary.setNormalStockCount(rs.getInt("normal_stock_count"));
                }
            }

            summary.setExportedToday(sumExportedQuantity(conn, ExportPeriod.TODAY));
            summary.setExportedThisWeek(sumExportedQuantity(conn, ExportPeriod.WEEK));
            summary.setExportedThisMonth(sumExportedQuantity(conn, ExportPeriod.MONTH));
            summary.setExportedThisYear(sumExportedQuantity(conn, ExportPeriod.YEAR));

            return summary;

        } catch (SQLException e) {
            throw new RuntimeException("InventoryDAO.getSummary error", e);
        }
    }

    public List<InventoryProductStat> findInventoryProducts(String keyword, String status) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    p.id,
                    p.title,
                    COALESCE(c.name, 'Chưa phân loại') AS category_name,
                    COALESCE(b.name, 'Chưa có thương hiệu') AS brand_name,
                    p.stock,
                    p.price,
                    COALESCE(SUM(CASE WHEN DATE(o.created_at) = CURDATE() THEN oi.quantity ELSE 0 END), 0) AS exported_today,
                    COALESCE(SUM(CASE WHEN YEARWEEK(o.created_at, 1) = YEARWEEK(CURDATE(), 1) THEN oi.quantity ELSE 0 END), 0) AS exported_week,
                    COALESCE(SUM(CASE WHEN YEAR(o.created_at) = YEAR(CURDATE()) AND MONTH(o.created_at) = MONTH(CURDATE()) THEN oi.quantity ELSE 0 END), 0) AS exported_month,
                    COALESCE(SUM(CASE WHEN YEAR(o.created_at) = YEAR(CURDATE()) THEN oi.quantity ELSE 0 END), 0) AS exported_year
                FROM store_product p
                LEFT JOIN store_category c ON c.id = p.category_id
                LEFT JOIN store_brand b ON b.id = p.brand_id
                LEFT JOIN store_orderitem oi ON oi.product_id = p.id
                LEFT JOIN store_order o ON o.id = oi.order_id
                    AND
                """)
                .append(VALID_EXPORTED_ORDER_CONDITION)
                .append("""
                WHERE p.is_active = 1
                """);

        List<Object> params = new ArrayList<>();

        String safeKeyword = keyword == null ? "" : keyword.trim();

        if (!safeKeyword.isEmpty()) {
            sql.append("""
                    AND (
                        LOWER(p.title) LIKE ?
                        OR LOWER(p.slug) LIKE ?
                        OR LOWER(COALESCE(c.name, '')) LIKE ?
                        OR LOWER(COALESCE(b.name, '')) LIKE ?
                    )
                    """);

            String pattern = "%" + safeKeyword.toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        String normalizedStatus = status == null ? "" : status.trim().toLowerCase();

        switch (normalizedStatus) {
            case "out" -> sql.append("AND p.stock <= 0\n");

            case "low" -> {
                sql.append("AND p.stock > 0 AND p.stock < ?\n");
                params.add(LOW_STOCK_THRESHOLD);
            }

            case "normal" -> {
                sql.append("AND p.stock >= ?\n");
                params.add(LOW_STOCK_THRESHOLD);
            }

            default -> {
                // Không lọc trạng thái.
            }
        }

        sql.append("""
                GROUP BY p.id, p.title, c.name, b.name, p.stock, p.price
                ORDER BY
                    CASE
                        WHEN p.stock <= 0 THEN 0
                        WHEN p.stock > 0 AND p.stock < ? THEN 1
                        ELSE 2
                    END,
                    p.stock ASC,
                    exported_month DESC,
                    p.title ASC
                """);

        params.add(LOW_STOCK_THRESHOLD);

        List<InventoryProductStat> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapInventoryProductStat(rs));
                }
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException("InventoryDAO.findInventoryProducts error", e);
        }
    }

    public List<InventoryProductStat> lowStockAlerts(int limit) {
        String sql = """
                SELECT
                    p.id,
                    p.title,
                    COALESCE(c.name, 'Chưa phân loại') AS category_name,
                    COALESCE(b.name, 'Chưa có thương hiệu') AS brand_name,
                    p.stock,
                    p.price,
                    0 AS exported_today,
                    0 AS exported_week,
                    0 AS exported_month,
                    0 AS exported_year
                FROM store_product p
                LEFT JOIN store_category c ON c.id = p.category_id
                LEFT JOIN store_brand b ON b.id = p.brand_id
                WHERE p.is_active = 1
                AND p.stock < ?
                ORDER BY p.stock ASC, p.title ASC
                LIMIT ?
                """;

        List<InventoryProductStat> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, LOW_STOCK_THRESHOLD);
            ps.setInt(2, Math.max(limit, 1));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapInventoryProductStat(rs));
                }
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException("InventoryDAO.lowStockAlerts error", e);
        }
    }

    public void addStock(int productId, int quantity, String note, Integer adminUserId) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Sản phẩm không hợp lệ.");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng nhập thêm phải lớn hơn 0.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int beforeStock = lockCurrentProductStock(conn, productId);
                int afterStock = beforeStock + quantity;

                updateProductStock(conn, productId, afterStock);

                insertMovement(
                        conn,
                        productId,
                        "IN",
                        quantity,
                        beforeStock,
                        afterStock,
                        "MANUAL",
                        null,
                        normalizeNote(note, "Nhập thêm tồn kho từ trang admin"),
                        adminUserId
                );

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (IllegalArgumentException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("InventoryDAO.addStock error", e);
        }
    }

    /**
     * Issue 127:
     * Lấy danh sách sản phẩm đã tích chọn kèm số lượng cần nhập để xuất ra Excel.
     */
    public List<RestockProductRow> findProductsForRestockExcel(List<RestockRequestRow> requestRows) {
        List<RestockProductRow> result = new ArrayList<>();

        if (requestRows == null || requestRows.isEmpty()) {
            return result;
        }

        try (Connection conn = DBConnection.getConnection()) {
            for (RestockRequestRow requestRow : requestRows) {
                if (requestRow == null || requestRow.productId() <= 0 || requestRow.quantity() <= 0) {
                    continue;
                }

                RestockProductRow row = findRestockProductRow(
                        conn,
                        requestRow.productId(),
                        requestRow.quantity(),
                        requestRow.note()
                );

                if (row != null) {
                    result.add(row);
                }
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException("InventoryDAO.findProductsForRestockExcel error", e);
        }
    }

    /**
     * Issue 127:
     * Cộng tồn kho hàng loạt sau khi đọc dữ liệu từ file Excel.
     */
    public List<RestockImportResultRow> importRestockRows(
            List<RestockImportRow> importRows,
            Integer adminUserId
    ) {
        List<RestockImportResultRow> resultRows = new ArrayList<>();

        if (importRows == null || importRows.isEmpty()) {
            resultRows.add(RestockImportResultRow.error(
                    0,
                    "",
                    null,
                    0,
                    null,
                    "Lỗi: Không có dữ liệu nhập kho."
            ));
            return resultRows;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                for (int i = 0; i < importRows.size(); i++) {
                    RestockImportRow row = importRows.get(i);
                    int displayRow = i + 2;

                    if (row == null) {
                        resultRows.add(RestockImportResultRow.error(
                                0,
                                "",
                                null,
                                0,
                                null,
                                "Lỗi dòng " + displayRow + ": Dữ liệu trống."
                        ));
                        continue;
                    }

                    int productId = row.productId();
                    int quantity = row.quantity();

                    if (productId <= 0) {
                        resultRows.add(RestockImportResultRow.error(
                                productId,
                                "",
                                null,
                                quantity,
                                null,
                                "Lỗi dòng " + displayRow + ": Mã sản phẩm không hợp lệ."
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
                                "Lỗi dòng " + displayRow + ": Không tìm thấy sản phẩm."
                        ));
                        continue;
                    }

                    if (quantity <= 0) {
                        resultRows.add(RestockImportResultRow.error(
                                productId,
                                product.title(),
                                product.stock(),
                                quantity,
                                product.stock(),
                                "Lỗi dòng " + displayRow + ": Số lượng nhập phải lớn hơn 0."
                        ));
                        continue;
                    }

                    int beforeStock = product.stock();
                    int afterStock = beforeStock + quantity;

                    updateProductStock(conn, productId, afterStock);

                    insertMovement(
                            conn,
                            productId,
                            "IN",
                            quantity,
                            beforeStock,
                            afterStock,
                            "EXCEL_IMPORT",
                            null,
                            normalizeNote(row.note(), "Nhập kho từ file Excel"),
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

            if (resultRows.isEmpty()) {
                resultRows.add(RestockImportResultRow.error(
                        0,
                        "",
                        null,
                        0,
                        null,
                        "Lỗi: File Excel không có dòng dữ liệu hợp lệ."
                ));
            }

            return resultRows;

        } catch (Exception e) {
            throw new RuntimeException("InventoryDAO.importRestockRows error", e);
        }
    }

    /**
     * Issue 128:
     * Thống kê nhập hàng theo tháng và năm.
     */
    public ImportSummary getImportSummary(int month, int year) {
        ImportSummary summary = new ImportSummary();

        int safeMonth = normalizeMonth(month);
        int safeYear = normalizeYear(year);

        summary.setSelectedMonth(safeMonth);
        summary.setSelectedYear(safeYear);

        String monthSql = """
                SELECT
                    COALESCE(SUM(im.quantity), 0) AS total_quantity,
                    COUNT(*) AS import_count,
                    COUNT(DISTINCT im.product_id) AS product_count
                FROM inventory_movement im
                WHERE UPPER(COALESCE(im.movement_type, '')) = 'IN'
                AND MONTH(im.created_at) = ?
                AND YEAR(im.created_at) = ?
                """;

        String yearSql = """
                SELECT
                    COALESCE(SUM(im.quantity), 0) AS total_quantity,
                    COUNT(*) AS import_count,
                    COUNT(DISTINCT im.product_id) AS product_count
                FROM inventory_movement im
                WHERE UPPER(COALESCE(im.movement_type, '')) = 'IN'
                AND YEAR(im.created_at) = ?
                """;

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(monthSql)) {
                ps.setInt(1, safeMonth);
                ps.setInt(2, safeYear);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        summary.setMonthlyImportQuantity(rs.getInt("total_quantity"));
                        summary.setMonthlyImportCount(rs.getInt("import_count"));
                        summary.setMonthlyProductCount(rs.getInt("product_count"));
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(yearSql)) {
                ps.setInt(1, safeYear);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        summary.setYearlyImportQuantity(rs.getInt("total_quantity"));
                        summary.setYearlyImportCount(rs.getInt("import_count"));
                        summary.setYearlyProductCount(rs.getInt("product_count"));
                    }
                }
            }

            return summary;

        } catch (SQLException e) {
            throw new RuntimeException("InventoryDAO.getImportSummary error", e);
        }
    }

    /**
     * Issue 128:
     * Lịch sử nhập hàng chi tiết theo tháng/năm/từ khóa.
     */
    public List<ImportHistoryRow> findImportHistory(int month, int year, String keyword) {
        int safeMonth = normalizeMonth(month);
        int safeYear = normalizeYear(year);

        StringBuilder sql = new StringBuilder("""
                SELECT
                    im.id,
                    im.product_id,
                    COALESCE(p.title, CONCAT('Sản phẩm #', im.product_id)) AS product_title,
                    COALESCE(c.name, 'Chưa phân loại') AS category_name,
                    COALESCE(b.name, 'Chưa có thương hiệu') AS brand_name,
                    im.quantity,
                    im.before_stock,
                    im.after_stock,
                    im.reference_type,
                    im.note,
                    COALESCE(u.full_name, u.username, 'Admin') AS created_by_name,
                    im.created_at
                FROM inventory_movement im
                LEFT JOIN store_product p ON p.id = im.product_id
                LEFT JOIN store_category c ON c.id = p.category_id
                LEFT JOIN store_brand b ON b.id = p.brand_id
                LEFT JOIN users u ON u.id = im.created_by
                WHERE UPPER(COALESCE(im.movement_type, '')) = 'IN'
                AND MONTH(im.created_at) = ?
                AND YEAR(im.created_at) = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(safeMonth);
        params.add(safeYear);

        String safeKeyword = keyword == null ? "" : keyword.trim();

        if (!safeKeyword.isEmpty()) {
            sql.append("""
                    AND (
                        LOWER(COALESCE(p.title, '')) LIKE ?
                        OR LOWER(COALESCE(p.slug, '')) LIKE ?
                        OR LOWER(COALESCE(c.name, '')) LIKE ?
                        OR LOWER(COALESCE(b.name, '')) LIKE ?
                        OR LOWER(COALESCE(im.note, '')) LIKE ?
                    )
                    """);

            String pattern = "%" + safeKeyword.toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append("""
                ORDER BY im.created_at DESC, im.id DESC
                LIMIT 200
                """);

        List<ImportHistoryRow> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ImportHistoryRow row = new ImportHistoryRow();

                    row.setId(rs.getInt("id"));
                    row.setProductId(rs.getInt("product_id"));
                    row.setProductTitle(rs.getString("product_title"));
                    row.setCategoryName(rs.getString("category_name"));
                    row.setBrandName(rs.getString("brand_name"));
                    row.setQuantity(rs.getInt("quantity"));
                    row.setBeforeStock(getNullableInteger(rs, "before_stock"));
                    row.setAfterStock(getNullableInteger(rs, "after_stock"));
                    row.setReferenceType(rs.getString("reference_type"));
                    row.setNote(rs.getString("note"));
                    row.setCreatedByName(rs.getString("created_by_name"));
                    row.setCreatedAt(rs.getTimestamp("created_at"));

                    result.add(row);
                }
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException("InventoryDAO.findImportHistory error", e);
        }
    }

    /**
     * Issue 128:
     * Danh sách năm có dữ liệu nhập hàng để đổ vào combobox lọc.
     */
    public List<Integer> importYearOptions() {
        String sql = """
                SELECT DISTINCT YEAR(created_at) AS import_year
                FROM inventory_movement
                WHERE UPPER(COALESCE(movement_type, '')) = 'IN'
                AND created_at IS NOT NULL
                ORDER BY import_year DESC
                """;

        List<Integer> years = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int year = rs.getInt("import_year");

                if (year > 0 && !years.contains(year)) {
                    years.add(year);
                }
            }

            int currentYear = LocalDate.now().getYear();

            if (!years.contains(currentYear)) {
                years.add(0, currentYear);
            }

            return years;

        } catch (SQLException e) {
            throw new RuntimeException("InventoryDAO.importYearOptions error", e);
        }
    }

    /**
     * Issue 128:
     * Labels biểu đồ nhập hàng theo 12 tháng của năm.
     */
    public List<String> monthlyImportStatLabels(int year) {
        int safeYear = normalizeYear(year);
        List<String> labels = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            labels.add(String.format("%02d/%d", month, safeYear));
        }

        return labels;
    }

    /**
     * Issue 128:
     * Values biểu đồ nhập hàng theo 12 tháng của năm.
     */
    public List<Integer> monthlyImportStatValues(int year) {
        int safeYear = normalizeYear(year);

        String sql = """
                SELECT
                    MONTH(created_at) AS import_month,
                    COALESCE(SUM(quantity), 0) AS total_quantity
                FROM inventory_movement
                WHERE UPPER(COALESCE(movement_type, '')) = 'IN'
                AND YEAR(created_at) = ?
                GROUP BY MONTH(created_at)
                ORDER BY import_month ASC
                """;

        List<Integer> values = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            values.add(0);
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, safeYear);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int month = rs.getInt("import_month");
                    int quantity = rs.getInt("total_quantity");

                    if (month >= 1 && month <= 12) {
                        values.set(month - 1, Math.max(quantity, 0));
                    }
                }
            }

            return values;

        } catch (SQLException e) {
            throw new RuntimeException("InventoryDAO.monthlyImportStatValues error", e);
        }
    }

    public List<InventoryMovementView> recentStockActivities(int limit) {
        String sql = """
                SELECT *
                FROM (
                    SELECT
                        im.id AS id,
                        im.product_id AS product_id,
                        COALESCE(p.title, CONCAT('Sản phẩm #', im.product_id)) AS product_title,
                        im.movement_type AS movement_type,
                        im.quantity AS quantity,
                        im.before_stock AS before_stock,
                        im.after_stock AS after_stock,
                        im.note AS note,
                        COALESCE(u.full_name, u.username, 'Admin') AS created_by_name,
                        im.created_at AS created_at
                    FROM inventory_movement im
                    LEFT JOIN store_product p ON p.id = im.product_id
                    LEFT JOIN users u ON u.id = im.created_by

                    UNION ALL

                    SELECT
                        NULL AS id,
                        oi.product_id AS product_id,
                        COALESCE(p.title, CONCAT('Sản phẩm #', oi.product_id)) AS product_title,
                        'OUT' AS movement_type,
                        oi.quantity AS quantity,
                        NULL AS before_stock,
                        NULL AS after_stock,
                        CONCAT('Xuất kho từ đơn hàng #', o.id) AS note,
                        'Hệ thống' AS created_by_name,
                        o.created_at AS created_at
                    FROM store_orderitem oi
                    JOIN store_order o ON o.id = oi.order_id
                    LEFT JOIN store_product p ON p.id = oi.product_id
                    WHERE
                """
                + VALID_EXPORTED_ORDER_CONDITION
                + """
                ) x
                ORDER BY x.created_at DESC
                LIMIT ?
                """;

        List<InventoryMovementView> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Math.max(limit, 1));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapMovement(rs));
                }
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException("InventoryDAO.recentStockActivities error", e);
        }
    }

    public List<String> last7DaysExportLabels() {
        String sql = """
                SELECT DATE_SUB(CURDATE(), INTERVAL seq.n DAY) AS export_date
                FROM (
                    SELECT 6 AS n UNION ALL SELECT 5 UNION ALL SELECT 4 UNION ALL SELECT 3
                    UNION ALL SELECT 2 UNION ALL SELECT 1 UNION ALL SELECT 0
                ) seq
                ORDER BY export_date ASC
                """;

        List<String> labels = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                labels.add(rs.getDate("export_date").toString());
            }

            return labels;

        } catch (SQLException e) {
            throw new RuntimeException("InventoryDAO.last7DaysExportLabels error", e);
        }
    }

    public List<Integer> last7DaysExportValues() {
        String sql = """
                SELECT
                    d.export_date,
                    COALESCE(SUM(oi.quantity), 0) AS exported_qty
                FROM (
                    SELECT DATE_SUB(CURDATE(), INTERVAL seq.n DAY) AS export_date
                    FROM (
                        SELECT 6 AS n UNION ALL SELECT 5 UNION ALL SELECT 4 UNION ALL SELECT 3
                        UNION ALL SELECT 2 UNION ALL SELECT 1 UNION ALL SELECT 0
                    ) seq
                ) d
                LEFT JOIN store_order o ON DATE(o.created_at) = d.export_date
                    AND
                """
                + VALID_EXPORTED_ORDER_CONDITION
                + """
                LEFT JOIN store_orderitem oi ON oi.order_id = o.id
                GROUP BY d.export_date
                ORDER BY d.export_date ASC
                """;

        List<Integer> values = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                values.add(rs.getInt("exported_qty"));
            }

            return values;

        } catch (SQLException e) {
            throw new RuntimeException("InventoryDAO.last7DaysExportValues error", e);
        }
    }

    private int sumExportedQuantity(Connection conn, ExportPeriod period) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT COALESCE(SUM(oi.quantity), 0) AS exported_qty
                FROM store_orderitem oi
                JOIN store_order o ON o.id = oi.order_id
                WHERE
                """)
                .append(VALID_EXPORTED_ORDER_CONDITION)
                .append("\n");

        switch (period) {
            case TODAY -> sql.append("AND DATE(o.created_at) = CURDATE()\n");

            case WEEK -> sql.append("AND YEARWEEK(o.created_at, 1) = YEARWEEK(CURDATE(), 1)\n");

            case MONTH -> sql.append("""
                    AND YEAR(o.created_at) = YEAR(CURDATE())
                    AND MONTH(o.created_at) = MONTH(CURDATE())
                    """);

            case YEAR -> sql.append("AND YEAR(o.created_at) = YEAR(CURDATE())\n");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("exported_qty");
            }

            return 0;
        }
    }

    private int lockCurrentProductStock(Connection conn, int productId) throws SQLException {
        String sql = """
                SELECT stock
                FROM store_product
                WHERE id = ?
                FOR UPDATE
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Không tìm thấy sản phẩm cần nhập kho.");
                }

                return rs.getInt("stock");
            }
        }
    }

    private RestockProductRow findRestockProductRow(
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

                return new RestockProductRow(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("category_name"),
                        rs.getString("brand_name"),
                        rs.getInt("stock"),
                        quantity,
                        normalizeNote(note, "")
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

    private void insertMovement(
            Connection conn,
            int productId,
            String movementType,
            int quantity,
            Integer beforeStock,
            Integer afterStock,
            String referenceType,
            Integer referenceId,
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
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setString(2, movementType);
            ps.setInt(3, quantity);
            setNullableInteger(ps, 4, beforeStock);
            setNullableInteger(ps, 5, afterStock);
            ps.setString(6, referenceType);
            setNullableInteger(ps, 7, referenceId);
            ps.setString(8, note);
            setNullableInteger(ps, 9, adminUserId);
            ps.executeUpdate();
        }
    }

    private InventoryProductStat mapInventoryProductStat(ResultSet rs) throws SQLException {
        InventoryProductStat stat = new InventoryProductStat();

        stat.setId(rs.getInt("id"));
        stat.setTitle(rs.getString("title"));
        stat.setCategoryName(rs.getString("category_name"));
        stat.setBrandName(rs.getString("brand_name"));
        stat.setStock(rs.getInt("stock"));
        stat.setPrice(rs.getBigDecimal("price"));
        stat.setExportedToday(rs.getInt("exported_today"));
        stat.setExportedThisWeek(rs.getInt("exported_week"));
        stat.setExportedThisMonth(rs.getInt("exported_month"));
        stat.setExportedThisYear(rs.getInt("exported_year"));

        return stat;
    }

    private InventoryMovementView mapMovement(ResultSet rs) throws SQLException {
        InventoryMovementView movement = new InventoryMovementView();

        movement.setId(getNullableInteger(rs, "id"));
        movement.setProductId(rs.getInt("product_id"));
        movement.setProductTitle(rs.getString("product_title"));
        movement.setMovementType(rs.getString("movement_type"));
        movement.setQuantity(rs.getInt("quantity"));
        movement.setBeforeStock(getNullableInteger(rs, "before_stock"));
        movement.setAfterStock(getNullableInteger(rs, "after_stock"));
        movement.setNote(rs.getString("note"));
        movement.setCreatedByName(rs.getString("created_by_name"));
        movement.setCreatedAt(rs.getTimestamp("created_at"));

        return movement;
    }

    private Integer getNullableInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private void setNullableInteger(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            int index = i + 1;

            if (value instanceof Integer intValue) {
                ps.setInt(index, intValue);

            } else if (value instanceof BigDecimal decimalValue) {
                ps.setBigDecimal(index, decimalValue);

            } else {
                ps.setString(index, String.valueOf(value));
            }
        }
    }

    private String normalizeNote(String note, String defaultNote) {
        if (note == null || note.trim().isEmpty()) {
            return defaultNote;
        }

        return note.trim();
    }

    private int normalizeMonth(int month) {
        if (month < 1 || month > 12) {
            return LocalDate.now().getMonthValue();
        }

        return month;
    }

    private int normalizeYear(int year) {
        if (year < 2000) {
            return LocalDate.now().getYear();
        }

        return year;
    }

    private enum ExportPeriod {
        TODAY,
        WEEK,
        MONTH,
        YEAR
    }

    public record RestockRequestRow(
            int productId,
            int quantity,
            String note
    ) {
    }

    public record RestockProductRow(
            int productId,
            String title,
            String categoryName,
            String brandName,
            int currentStock,
            int quantity,
            String note
    ) {
    }

    public record RestockImportRow(
            int productId,
            int quantity,
            String note
    ) {
    }

    public record RestockImportResultRow(
            int productId,
            String title,
            Integer beforeStock,
            int quantity,
            Integer afterStock,
            String status
    ) {

        public static RestockImportResultRow success(
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

        public static RestockImportResultRow error(
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

    public static class ImportSummary {

        private int selectedMonth;
        private int selectedYear;

        private int monthlyImportQuantity;
        private int monthlyImportCount;
        private int monthlyProductCount;

        private int yearlyImportQuantity;
        private int yearlyImportCount;
        private int yearlyProductCount;

        public int getSelectedMonth() {
            return selectedMonth;
        }

        public void setSelectedMonth(int selectedMonth) {
            this.selectedMonth = Math.max(selectedMonth, 0);
        }

        public int getSelectedYear() {
            return selectedYear;
        }

        public void setSelectedYear(int selectedYear) {
            this.selectedYear = Math.max(selectedYear, 0);
        }

        public int getMonthlyImportQuantity() {
            return monthlyImportQuantity;
        }

        public void setMonthlyImportQuantity(int monthlyImportQuantity) {
            this.monthlyImportQuantity = Math.max(monthlyImportQuantity, 0);
        }

        public int getMonthlyImportCount() {
            return monthlyImportCount;
        }

        public void setMonthlyImportCount(int monthlyImportCount) {
            this.monthlyImportCount = Math.max(monthlyImportCount, 0);
        }

        public int getMonthlyProductCount() {
            return monthlyProductCount;
        }

        public void setMonthlyProductCount(int monthlyProductCount) {
            this.monthlyProductCount = Math.max(monthlyProductCount, 0);
        }

        public int getYearlyImportQuantity() {
            return yearlyImportQuantity;
        }

        public void setYearlyImportQuantity(int yearlyImportQuantity) {
            this.yearlyImportQuantity = Math.max(yearlyImportQuantity, 0);
        }

        public int getYearlyImportCount() {
            return yearlyImportCount;
        }

        public void setYearlyImportCount(int yearlyImportCount) {
            this.yearlyImportCount = Math.max(yearlyImportCount, 0);
        }

        public int getYearlyProductCount() {
            return yearlyProductCount;
        }

        public void setYearlyProductCount(int yearlyProductCount) {
            this.yearlyProductCount = Math.max(yearlyProductCount, 0);
        }

        public String getSelectedPeriodText() {
            return String.format("%02d/%d", selectedMonth, selectedYear);
        }
    }

    public static class ImportHistoryRow {

        private int id;
        private int productId;
        private String productTitle;
        private String categoryName;
        private String brandName;
        private int quantity;
        private Integer beforeStock;
        private Integer afterStock;
        private String referenceType;
        private String note;
        private String createdByName;
        private Timestamp createdAt;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = Math.max(id, 0);
        }

        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = Math.max(productId, 0);
        }

        public String getProductTitle() {
            return productTitle;
        }

        public String getDisplayProductTitle() {
            if (productTitle == null || productTitle.trim().isEmpty()) {
                return "Sản phẩm #" + productId;
            }

            return productTitle.trim();
        }

        public void setProductTitle(String productTitle) {
            this.productTitle = productTitle;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public String getDisplayCategoryName() {
            if (categoryName == null || categoryName.trim().isEmpty()) {
                return "Chưa phân loại";
            }

            return categoryName.trim();
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getBrandName() {
            return brandName;
        }

        public String getDisplayBrandName() {
            if (brandName == null || brandName.trim().isEmpty()) {
                return "Chưa có thương hiệu";
            }

            return brandName.trim();
        }

        public void setBrandName(String brandName) {
            this.brandName = brandName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = Math.max(quantity, 0);
        }

        public String getQuantityText() {
            return "+" + quantity;
        }

        public Integer getBeforeStock() {
            return beforeStock;
        }

        public String getBeforeStockText() {
            return beforeStock == null ? "-" : String.valueOf(Math.max(beforeStock, 0));
        }

        public void setBeforeStock(Integer beforeStock) {
            this.beforeStock = beforeStock;
        }

        public Integer getAfterStock() {
            return afterStock;
        }

        public String getAfterStockText() {
            return afterStock == null ? "-" : String.valueOf(Math.max(afterStock, 0));
        }

        public void setAfterStock(Integer afterStock) {
            this.afterStock = afterStock;
        }

        public String getStockChangeText() {
            return getBeforeStockText() + " → " + getAfterStockText();
        }

        public String getReferenceType() {
            return referenceType;
        }

        public String getReferenceTypeLabel() {
            if (referenceType == null || referenceType.trim().isEmpty()) {
                return "Không xác định";
            }

            return switch (referenceType.trim().toUpperCase()) {
                case "MANUAL" -> "Nhập thủ công";
                case "EXCEL_IMPORT" -> "Nhập từ Excel";
                case "RETURN" -> "Hoàn kho";
                default -> referenceType.trim();
            };
        }

        public String getReferenceTypeClass() {
            if (referenceType == null || referenceType.trim().isEmpty()) {
                return "import-method-other";
            }

            return switch (referenceType.trim().toUpperCase()) {
                case "MANUAL" -> "import-method-manual";
                case "EXCEL_IMPORT" -> "import-method-excel";
                case "RETURN" -> "import-method-return";
                default -> "import-method-other";
            };
        }

        public void setReferenceType(String referenceType) {
            this.referenceType = referenceType;
        }

        public String getNote() {
            return note;
        }

        public String getDisplayNote() {
            if (note == null || note.trim().isEmpty()) {
                return "Không có ghi chú";
            }

            return note.trim();
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getCreatedByName() {
            return createdByName;
        }

        public String getDisplayCreatedByName() {
            if (createdByName == null || createdByName.trim().isEmpty()) {
                return "Admin";
            }

            return createdByName.trim();
        }

        public void setCreatedByName(String createdByName) {
            this.createdByName = createdByName;
        }

        public Timestamp getCreatedAt() {
            return createdAt;
        }

        public String getFormattedCreatedAt() {
            if (createdAt == null) {
                return "";
            }

            return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(createdAt);
        }

        public void setCreatedAt(Timestamp createdAt) {
            this.createdAt = createdAt;
        }
    }

    private record ProductStockSnapshot(
            int productId,
            String title,
            int stock
    ) {
    }
}

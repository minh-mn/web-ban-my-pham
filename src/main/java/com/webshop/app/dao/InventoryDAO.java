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
import java.sql.Types;
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
     * Dùng cho issue 127:
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
     * Dùng cho issue 127:
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

    private record ProductStockSnapshot(
            int productId,
            String title,
            int stock
    ) {
    }
}

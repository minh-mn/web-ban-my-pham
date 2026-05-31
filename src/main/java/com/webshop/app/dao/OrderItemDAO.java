package com.webshop.app.dao;

import com.webshop.app.model.OrderItem;
import com.webshop.app.utils.DBConnection;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO {

    private static final String DEFAULT_PRODUCT_IMAGE = "/assets/images/default-product.jpg";

    /* ================= CREATE ================= */

    public void create(Connection conn, OrderItem item) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection must not be null");
        }

        if (item == null) {
            throw new SQLException("OrderItem must not be null");
        }

        if (item.getOrderId() <= 0) {
            throw new SQLException("OrderItem.orderId is invalid");
        }

        if (item.getProductId() <= 0) {
            throw new SQLException("OrderItem.productId is invalid");
        }

        if (item.getQuantity() <= 0) {
            throw new SQLException("OrderItem.quantity must be greater than 0");
        }

        String sql = """
                INSERT INTO store_orderitem
                (
                    order_id,
                    product_id,
                    variant_id,
                    variant_name,
                    variant_size,
                    variant_type,
                    price,
                    quantity
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProductId());

            setNullableInteger(ps, 3, item.getVariantId());
            setNullableString(ps, 4, item.getVariantName());
            setNullableString(ps, 5, item.getVariantSize());
            setNullableString(ps, 6, item.getVariantType());

            ps.setBigDecimal(7, vnd0(item.getPrice()));
            ps.setInt(8, item.getQuantity());

            ps.executeUpdate();
        }
    }

    public boolean existsByOrderId(Connection conn, int orderId) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection must not be null");
        }

        if (orderId <= 0) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM store_orderitem
                WHERE order_id = ?
                LIMIT 1
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /* ================= FIND BY ORDER ID ================= */

    public List<OrderItem> findByOrderId(int orderId) {
        try (Connection connection = DBConnection.getConnection()) {
            return findByOrderId(connection, orderId);
        } catch (SQLException e) {
            throw new RuntimeException("OrderItemDAO.findByOrderId error", e);
        }
    }

    public List<OrderItem> findByOrderId(Connection connection, int orderId) throws SQLException {
        if (connection == null) {
            throw new SQLException("Connection must not be null");
        }

        if (orderId <= 0) {
            return new ArrayList<>();
        }

        String sql = """
                SELECT
                    oi.id,
                    oi.order_id,
                    oi.product_id,
                    oi.variant_id,
                    oi.variant_name,
                    oi.variant_size,
                    oi.variant_type,
                    oi.price,
                    oi.quantity,
                    COALESCE(p.title, CONCAT('Sản phẩm #', oi.product_id)) AS product_name,
                    p.image AS image_url,
                    COALESCE(oi.price, 0) * COALESCE(oi.quantity, 0) AS item_subtotal
                FROM store_orderitem oi
                LEFT JOIN store_product p ON p.id = oi.product_id
                WHERE oi.order_id = ?
                ORDER BY oi.id ASC
                """;

        List<OrderItem> items = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        }

        return items;
    }

    public int countByOrderId(int orderId) {
        if (orderId <= 0) {
            return 0;
        }

        String sql = """
                SELECT COUNT(*)
                FROM store_orderitem
                WHERE order_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderItemDAO.countByOrderId error", e);
        }
    }

    /* ================= DELETE ================= */

    public void deleteByOrderId(Connection conn, int orderId) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection must not be null");
        }

        if (orderId <= 0) {
            return;
        }

        String sql = """
                DELETE FROM store_orderitem
                WHERE order_id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    /* ================= MAPPER ================= */

    private OrderItem mapRow(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();

        item.setId(rs.getInt("id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setProductId(rs.getInt("product_id"));

        int variantId = rs.getInt("variant_id");
        if (rs.wasNull()) {
            item.setVariantId(null);
        } else {
            item.setVariantId(variantId);
        }

        item.setVariantName(rs.getString("variant_name"));
        item.setVariantSize(rs.getString("variant_size"));
        item.setVariantType(rs.getString("variant_type"));

        item.setPrice(vnd0(rs.getBigDecimal("price")));
        item.setQuantity(rs.getInt("quantity"));

        item.setProductName(defaultIfBlank(rs.getString("product_name"), "Sản phẩm"));
        item.setImageUrl(normalizeImageUrl(rs.getString("image_url")));

        BigDecimal subtotal = vnd0(rs.getBigDecimal("item_subtotal"));
        setSubtotalIfSupported(item, subtotal);

        return item;
    }

    /* ================= HELPERS ================= */

    private static BigDecimal vnd0(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return value.trim();
    }

    private String normalizeImageUrl(String imageUrl) {
        String value = defaultIfBlank(imageUrl, DEFAULT_PRODUCT_IMAGE);

        if (value.startsWith("http://")
                || value.startsWith("https://")
                || value.startsWith("data:")) {
            return value;
        }

        if (value.startsWith("/")) {
            return value;
        }

        if (value.startsWith("uploads/") || value.startsWith("assets/")) {
            return "/" + value;
        }

        return "/uploads/product/" + value;
    }

    private void setNullableInteger(
            PreparedStatement ps,
            int index,
            Integer value
    ) throws SQLException {

        if (value == null || value <= 0) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private void setNullableString(
            PreparedStatement ps,
            int index,
            String value
    ) throws SQLException {

        if (value == null || value.trim().isEmpty()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }

    private void setSubtotalIfSupported(OrderItem item, BigDecimal subtotal) {
        if (item == null) {
            return;
        }

        try {
            Method setSubtotalMethod = item.getClass().getMethod("setSubtotal", BigDecimal.class);
            setSubtotalMethod.invoke(item, subtotal);
        } catch (Exception ignored) {
            /*
             * Một số version OrderItem có getSubtotal() tự tính price * quantity
             * và không có setter. Vì vậy không bắt buộc phải setSubtotal.
             */
        }
    }

    public List<Integer> findFrequentlyBoughtTogether(int productId) {
        List<Integer> productIds = new ArrayList<>();
        String sql = "SELECT oi2.product_id, SUM(oi1.quantity * oi2.quantity) AS score " +
                "FROM store_orderitem oi1 " +
                "JOIN store_orderitem oi2 ON oi1.order_id = oi2.order_id " +
                "WHERE oi1.product_id = ? " +
                "AND oi2.product_id != ? " +
                "GROUP BY oi2.product_id " +
                "ORDER BY score DESC " +
                "LIMIT 8";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ps.setInt(2, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productIds.add(rs.getInt("product_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productIds;
    }
}

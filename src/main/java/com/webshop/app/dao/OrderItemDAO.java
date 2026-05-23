package com.webshop.app.dao;

import com.webshop.app.model.OrderItem;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO {

    /* ================= CREATE ================= */

    public void create(Connection conn, OrderItem item) throws SQLException {
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

            ps.setBigDecimal(7, item.getPrice());
            ps.setInt(8, item.getQuantity());

            ps.executeUpdate();
        }
    }

    public boolean existsByOrderId(Connection conn, int orderId) throws SQLException {
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
                    p.title AS product_name,
                    p.image AS image_url
                FROM store_orderitem oi
                JOIN store_product p ON p.id = oi.product_id
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

        item.setPrice(rs.getBigDecimal("price"));
        item.setQuantity(rs.getInt("quantity"));

        item.setProductName(rs.getString("product_name"));
        item.setImageUrl(rs.getString("image_url"));

        return item;
    }

    /* ================= HELPERS ================= */

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
}
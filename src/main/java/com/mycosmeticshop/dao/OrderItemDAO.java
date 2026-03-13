package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.OrderItem;
import com.mycosmeticshop.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO {

    /* ================= CREATE ================= */
    public void create(Connection conn, OrderItem i) throws SQLException {

        String sql =
                "INSERT INTO store_orderitem " +
                "(order_id, product_id, price, quantity) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, i.getOrderId());
            ps.setInt(2, i.getProductId());
            ps.setBigDecimal(3, i.getPrice());
            ps.setInt(4, i.getQuantity());
            ps.executeUpdate();
        }
    }

    /**
     * ✅ IDempotent helper (dùng cho finalize VNPAY):
     * check order đã có items chưa (tránh insert lặp)
     */
    public boolean existsByOrderId(Connection conn, int orderId) throws SQLException {
        String sql = "SELECT 1 FROM store_orderitem WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /* ================= FIND BY ORDER ID ================= */
    public List<OrderItem> findByOrderId(int orderId) {

        String sql =
                "SELECT oi.id, oi.order_id, oi.product_id, oi.price, oi.quantity, " +
                "       p.title AS product_name, p.image AS image_url " +
                "FROM store_orderitem oi " +
                "JOIN store_product p ON p.id = oi.product_id " +
                "WHERE oi.order_id = ? " +
                "ORDER BY oi.id ASC";

        List<OrderItem> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem i = new OrderItem();
                    i.setId(rs.getInt("id"));
                    i.setOrderId(rs.getInt("order_id"));
                    i.setProductId(rs.getInt("product_id"));
                    i.setPrice(rs.getBigDecimal("price"));
                    i.setQuantity(rs.getInt("quantity"));

                    i.setProductName(rs.getString("product_name"));
                    i.setImageUrl(rs.getString("image_url"));

                    list.add(i);
                }
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("OrderItemDAO.findByOrderId error", e);
        }
    }
}

package com.webshop.app.dao;

import com.webshop.app.model.OrderItem;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO {

    /* ================= CREATE ================= */

    public void create(Connection conn, OrderItem item) throws SQLException {
        String sql =
                "INSERT INTO store_orderitem " +
                        "(order_id, product_id, price, quantity) " +
                        "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProductId());
            ps.setBigDecimal(3, item.getPrice());
            ps.setInt(4, item.getQuantity());
            ps.executeUpdate();
        }
    }

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

        List<OrderItem> items = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }

            return items;

        } catch (SQLException e) {
            throw new RuntimeException("OrderItemDAO.findByOrderId error", e);
        }
    }

    private OrderItem mapRow(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();

        item.setId(rs.getInt("id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setPrice(rs.getBigDecimal("price"));
        item.setQuantity(rs.getInt("quantity"));
        item.setProductName(rs.getString("product_name"));
        item.setImageUrl(rs.getString("image_url"));

        return item;
    }
}
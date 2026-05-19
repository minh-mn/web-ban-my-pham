package com.webshop.app.dao;

import com.webshop.app.model.Order;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AdminOrderDAO {

    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();

        String sql = """
                SELECT id, user_id, full_name, total, status, created_at
                FROM store_order
                ORDER BY created_at DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                orders.add(mapRowBasic(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.findAll error", e);
        }

        return orders;
    }

    public Order findById(int id) {
        String sql = """
                SELECT id, user_id, full_name, phone, address, total,
                       payment_method, payment_status, status, vnp_txn_ref, created_at
                FROM store_order
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapRowDetail(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.findById error", e);
        }
    }

    public boolean updateStatus(int id, String status) {
        String sql = """
                UPDATE store_order
                SET status = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.updateStatus error", e);
        }
    }

    private Order mapRowBasic(ResultSet resultSet) throws SQLException {
        Order order = new Order();

        order.setId(resultSet.getInt("id"));
        setNullableUserId(order, resultSet);
        order.setFullName(resultSet.getString("full_name"));
        order.setTotal(resultSet.getBigDecimal("total"));
        order.setStatus(resultSet.getString("status"));
        order.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));

        return order;
    }

    private Order mapRowDetail(ResultSet resultSet) throws SQLException {
        Order order = mapRowBasic(resultSet);

        order.setPhone(resultSet.getString("phone"));
        order.setAddress(resultSet.getString("address"));
        order.setPaymentMethod(resultSet.getString("payment_method"));
        order.setPaymentStatus(resultSet.getString("payment_status"));
        order.setVnpTxnRef(resultSet.getString("vnp_txn_ref"));

        return order;
    }

    private void setNullableUserId(Order order, ResultSet resultSet) throws SQLException {
        int userId = resultSet.getInt("user_id");

        if (resultSet.wasNull()) {
            order.setUserId(0);
        } else {
            order.setUserId(userId);
        }
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.Order;
import com.mycosmeticshop.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminOrderDAO {

    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();

        String sql =
            "SELECT id, user_id, full_name, total, status, created_at " +
            "FROM store_order " +
            "ORDER BY created_at DESC";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowBasic(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.findAll error", e);
        }

        return list;
    }

    // ===== DETAIL: lấy đầy đủ cột cho trang chi tiết =====
    public Order findById(int id) {
        String sql =
            "SELECT id, user_id, full_name, phone, address, total, " +
            "       payment_method, payment_status, status, vnp_txn_ref, created_at " +
            "FROM store_order " +
            "WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Order o = new Order();
                o.setId(rs.getInt("id"));
                o.setUserId(rs.getInt("user_id"));
                o.setFullName(rs.getString("full_name"));
                o.setPhone(rs.getString("phone"));
                o.setAddress(rs.getString("address"));
                o.setTotal(rs.getBigDecimal("total"));
                o.setPaymentMethod(rs.getString("payment_method"));
                o.setPaymentStatus(rs.getString("payment_status"));
                o.setStatus(rs.getString("status"));
                o.setVnpTxnRef(rs.getString("vnp_txn_ref"));

                Timestamp ts = rs.getTimestamp("created_at");
                o.setCreatedAt(ts == null ? null : ts.toLocalDateTime());

                return o;
            }

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.findById error", e);
        }
    }

    // ===== UPDATE STATUS =====
    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE store_order SET status = ? WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.updateStatus error", e);
        }
    }

    private Order mapRowBasic(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setUserId(rs.getInt("user_id"));
        o.setFullName(rs.getString("full_name"));
        o.setTotal(rs.getBigDecimal("total"));
        o.setStatus(rs.getString("status"));

        Timestamp ts = rs.getTimestamp("created_at");
        o.setCreatedAt(ts == null ? null : ts.toLocalDateTime());

        return o;
    }
}

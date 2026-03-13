package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.OrderDiscount;
import com.mycosmeticshop.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderDiscountDAO {

    public List<OrderDiscount> findAll() {
        String sql =
            "SELECT id, name, min_order_value, discount_percent, max_discount_amount, " +
            "       start_date, end_date, active " +
            "FROM order_discounts " +
            "ORDER BY id DESC";

        List<OrderDiscount> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("OrderDiscountDAO.findAll failed", e);
        }
    }

    public OrderDiscount findById(int id) {
        String sql =
            "SELECT id, name, min_order_value, discount_percent, max_discount_amount, " +
            "       start_date, end_date, active " +
            "FROM order_discounts " +
            "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDiscountDAO.findById failed", e);
        }
    }

    public int create(OrderDiscount d) {
        String sql =
            "INSERT INTO order_discounts " +
            " (name, min_order_value, discount_percent, max_discount_amount, start_date, end_date, active) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, safeText(d.getName()));
            ps.setBigDecimal(2, nz(d.getMinOrderValue()));
            ps.setBigDecimal(3, nz(d.getDiscountPercent()));

            if (d.getMaxDiscountAmount() == null) ps.setNull(4, Types.DECIMAL);
            else ps.setBigDecimal(4, d.getMaxDiscountAmount());

            ps.setDate(5, Date.valueOf(d.getStartDate()));
            ps.setDate(6, Date.valueOf(d.getEndDate()));
            ps.setBoolean(7, d.isActive());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDiscountDAO.create failed", e);
        }
    }

    public boolean update(OrderDiscount d) {
        String sql =
            "UPDATE order_discounts SET " +
            " name = ?, " +
            " min_order_value = ?, " +
            " discount_percent = ?, " +
            " max_discount_amount = ?, " +
            " start_date = ?, " +
            " end_date = ?, " +
            " active = ? " +
            "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, safeText(d.getName()));
            ps.setBigDecimal(2, nz(d.getMinOrderValue()));
            ps.setBigDecimal(3, nz(d.getDiscountPercent()));

            if (d.getMaxDiscountAmount() == null) ps.setNull(4, Types.DECIMAL);
            else ps.setBigDecimal(4, d.getMaxDiscountAmount());

            ps.setDate(5, Date.valueOf(d.getStartDate()));
            ps.setDate(6, Date.valueOf(d.getEndDate()));
            ps.setBoolean(7, d.isActive());
            ps.setInt(8, d.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("OrderDiscountDAO.update failed", e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM order_discounts WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("OrderDiscountDAO.delete failed", e);
        }
    }

    public OrderDiscount findActiveForToday(LocalDate today) {
        String sql =
            "SELECT TOP 1 id, name, min_order_value, discount_percent, max_discount_amount, " +
            "       start_date, end_date, active " +
            "FROM order_discounts " +
            "WHERE active = 1 " +
            "  AND start_date <= ? " +
            "  AND end_date >= ? " +
            "ORDER BY discount_percent DESC, min_order_value ASC, id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            Date d = Date.valueOf(today);
            ps.setDate(1, d);
            ps.setDate(2, d);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDiscountDAO.findActiveForToday failed", e);
        }
    }

    private OrderDiscount mapRow(ResultSet rs) throws SQLException {
        OrderDiscount d = new OrderDiscount();
        d.setId(rs.getInt("id"));
        d.setName(rs.getString("name"));
        d.setMinOrderValue(rs.getBigDecimal("min_order_value"));
        d.setDiscountPercent(rs.getBigDecimal("discount_percent"));
        d.setMaxDiscountAmount(rs.getBigDecimal("max_discount_amount"));

        Date s = rs.getDate("start_date");
        Date e = rs.getDate("end_date");
        d.setStartDate(s != null ? s.toLocalDate() : null);
        d.setEndDate(e != null ? e.toLocalDate() : null);

        d.setActive(rs.getBoolean("active"));
        return d;
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String safeText(String s) {
        return (s == null) ? "" : s.trim();
    }
}

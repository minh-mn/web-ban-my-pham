package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.Coupon;
import com.mycosmeticshop.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CouponDAO {

    /* ===================== FRONTEND ===================== */

    public Coupon findByCode(String code) {
        String sql =
            "SELECT id, code, discount_percent, max_discount_amount, max_uses, used_count, is_active, start_date, end_date " +
            "FROM store_coupon " +
            "WHERE code = ? AND is_active = 1";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, code);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findByCode error", e);
        }
    }

    public void increaseUsedCount(Connection conn, int couponId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
            "UPDATE store_coupon SET used_count = used_count + 1 WHERE id = ?")) {
            ps.setInt(1, couponId);
            ps.executeUpdate();
        }
    }

    /* ===================== ADMIN ===================== */

    public List<Coupon> findAll() {
        List<Coupon> list = new ArrayList<>();

        String sql =
            "SELECT id, code, discount_percent, max_discount_amount, max_uses, used_count, is_active, start_date, end_date " +
            "FROM store_coupon " +
            "ORDER BY id DESC";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findAll error", e);
        }

        return list;
    }

    public Coupon findById(int id) {
        String sql =
            "SELECT id, code, discount_percent, max_discount_amount, max_uses, used_count, is_active, start_date, end_date " +
            "FROM store_coupon " +
            "WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findById error", e);
        }

        return null;
    }

    public void create(Coupon cp) {
        String sql =
            "INSERT INTO store_coupon " +
            "(code, discount_percent, max_discount_amount, max_uses, used_count, is_active, start_date, end_date) " +
            "VALUES (?, ?, ?, ?, 0, ?, ?, ?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, cp.getCode());
            ps.setInt(2, cp.getDiscountPercent());

            // max_discount_amount: nullable
            if (cp.getMaxDiscountAmount() == null) ps.setNull(3, Types.DECIMAL);
            else ps.setBigDecimal(3, cp.getMaxDiscountAmount());

            // max_uses: NOT NULL (DB constraint)
            ps.setInt(4, cp.getMaxUses());

            ps.setBoolean(5, cp.isActive());

            if (cp.getStartDate() == null) ps.setNull(6, Types.DATE);
            else ps.setDate(6, Date.valueOf(cp.getStartDate()));

            if (cp.getEndDate() == null) ps.setNull(7, Types.DATE);
            else ps.setDate(7, Date.valueOf(cp.getEndDate()));

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.create error", e);
        }
    }

    public void update(Coupon cp) {
        String sql =
            "UPDATE store_coupon " +
            "SET code = ?, discount_percent = ?, max_discount_amount = ?, max_uses = ?, is_active = ?, start_date = ?, end_date = ? " +
            "WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, cp.getCode());
            ps.setInt(2, cp.getDiscountPercent());

            if (cp.getMaxDiscountAmount() == null) ps.setNull(3, Types.DECIMAL);
            else ps.setBigDecimal(3, cp.getMaxDiscountAmount());

            ps.setInt(4, cp.getMaxUses());

            ps.setBoolean(5, cp.isActive());

            if (cp.getStartDate() == null) ps.setNull(6, Types.DATE);
            else ps.setDate(6, Date.valueOf(cp.getStartDate()));

            if (cp.getEndDate() == null) ps.setNull(7, Types.DATE);
            else ps.setDate(7, Date.valueOf(cp.getEndDate()));

            ps.setInt(8, cp.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.update error", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM store_coupon WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.delete error", e);
        }
    }

    public void toggleActive(int id) {
        String sql =
            "UPDATE store_coupon " +
            "SET is_active = CASE WHEN is_active = 1 THEN 0 ELSE 1 END " +
            "WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.toggleActive error", e);
        }
    }

    /* ===================== MAPPER ===================== */

    private Coupon mapRow(ResultSet rs) throws SQLException {
        Coupon cp = new Coupon();

        cp.setId(rs.getInt("id"));
        cp.setCode(rs.getString("code"));
        cp.setDiscountPercent(rs.getInt("discount_percent"));
        cp.setMaxDiscountAmount(rs.getBigDecimal("max_discount_amount"));
        cp.setMaxUses(rs.getInt("max_uses"));
        cp.setUsedCount(rs.getInt("used_count"));
        cp.setActive(rs.getBoolean("is_active"));

        Date sd = rs.getDate("start_date");
        Date ed = rs.getDate("end_date");

        cp.setStartDate(sd == null ? null : sd.toLocalDate());
        cp.setEndDate(ed == null ? null : ed.toLocalDate());

        return cp;
    }
}

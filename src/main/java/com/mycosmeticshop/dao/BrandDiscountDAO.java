package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.BrandDiscount;
import com.mycosmeticshop.model.DiscountType;
import com.mycosmeticshop.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BrandDiscountDAO {

    /**
     * Lấy BrandDiscount hợp lệ có discount_value cao nhất
     * (tương đương Django: order_by('-discount_value').first())
     */
    public BrandDiscount findBestActiveByBrandId(int brandId) {

        String sql =
                "SELECT TOP 1 * " +
                "FROM store_branddiscount " +
                "WHERE brand_id = ? " +
                "  AND is_active = 1 " +
                "  AND start_date <= CAST(GETDATE() AS DATE) " +
                "  AND end_date   >= CAST(GETDATE() AS DATE) " +
                "ORDER BY discount_value DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, brandId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapRow(rs, false);
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.findBestActiveByBrandId error", e);
        }
    }

    // ================= ADMIN CRUD =================

    /**
     * Admin list: lấy tất cả discount.
     * Nếu muốn hiển thị tên brand trong admin list, bật joinBrand=true
     * và đảm bảo bảng brand tương ứng tồn tại (ví dụ store_brand).
     */
    public List<BrandDiscount> findAll(boolean joinBrand) {

        List<BrandDiscount> list = new ArrayList<>();

        // ⚠️ Bạn chỉnh tên bảng brand nếu khác.
        String sql = joinBrand
                ? "SELECT d.*, b.name AS brand_name " +
                  "FROM store_branddiscount d " +
                  "JOIN store_brand b ON b.id = d.brand_id " +
                  "ORDER BY d.id DESC"
                : "SELECT * FROM store_branddiscount ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs, joinBrand));
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.findAll error", e);
        }

        return list;
    }

    public BrandDiscount findById(int id) {

        String sql = "SELECT * FROM store_branddiscount WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapRow(rs, false);
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.findById error", e);
        }
    }

    public void create(BrandDiscount d) {

        String sql =
                "INSERT INTO store_branddiscount " +
                "(brand_id, discount_type, discount_value, max_discount_amount, start_date, end_date, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, d.getBrandId());
            ps.setString(2, d.getDiscountType() != null ? d.getDiscountType().name() : null);
            ps.setBigDecimal(3, d.getDiscountValue());
            ps.setBigDecimal(4, d.getMaxDiscountAmount());

            if (d.getStartDate() != null) ps.setDate(5, Date.valueOf(d.getStartDate()));
            else ps.setNull(5, Types.DATE);

            if (d.getEndDate() != null) ps.setDate(6, Date.valueOf(d.getEndDate()));
            else ps.setNull(6, Types.DATE);

            ps.setBoolean(7, d.isActive());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.create error", e);
        }
    }

    public void update(BrandDiscount d) {

        String sql =
                "UPDATE store_branddiscount SET " +
                "brand_id = ?, " +
                "discount_type = ?, " +
                "discount_value = ?, " +
                "max_discount_amount = ?, " +
                "start_date = ?, " +
                "end_date = ?, " +
                "is_active = ? " +
                "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, d.getBrandId());
            ps.setString(2, d.getDiscountType() != null ? d.getDiscountType().name() : null);
            ps.setBigDecimal(3, d.getDiscountValue());
            ps.setBigDecimal(4, d.getMaxDiscountAmount());

            if (d.getStartDate() != null) ps.setDate(5, Date.valueOf(d.getStartDate()));
            else ps.setNull(5, Types.DATE);

            if (d.getEndDate() != null) ps.setDate(6, Date.valueOf(d.getEndDate()));
            else ps.setNull(6, Types.DATE);

            ps.setBoolean(7, d.isActive());
            ps.setInt(8, d.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.update error", e);
        }
    }

    public void delete(int id) {

        String sql = "DELETE FROM store_branddiscount WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.delete error", e);
        }
    }

    public void toggleActive(int id) {

        String sql =
                "UPDATE store_branddiscount " +
                "SET is_active = CASE WHEN is_active = 1 THEN 0 ELSE 1 END " +
                "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.toggleActive error", e);
        }
    }

    // ================= MAPPING =================

    private BrandDiscount mapRow(ResultSet rs, boolean hasBrandName) throws SQLException {
        BrandDiscount d = new BrandDiscount();
        d.setId(rs.getInt("id"));
        d.setBrandId(rs.getInt("brand_id"));

        String dt = rs.getString("discount_type");
        if (dt != null && !dt.isBlank()) {
            d.setDiscountType(DiscountType.valueOf(dt));
        }

        d.setDiscountValue(rs.getBigDecimal("discount_value"));
        d.setMaxDiscountAmount(rs.getBigDecimal("max_discount_amount"));

        Date sd = rs.getDate("start_date");
        if (sd != null) d.setStartDate(sd.toLocalDate());

        Date ed = rs.getDate("end_date");
        if (ed != null) d.setEndDate(ed.toLocalDate());

        d.setActive(rs.getBoolean("is_active"));

        // Nếu bạn đã thêm brandName trong model theo bản mình chỉnh, có thể set ở đây:
        if (hasBrandName) {
            try {
                d.setBrandName(rs.getString("brand_name"));
            } catch (SQLException ignore) { }
        }

        return d;
    }
}

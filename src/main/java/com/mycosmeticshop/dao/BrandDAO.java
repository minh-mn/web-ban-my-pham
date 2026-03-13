package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.Brand;
import com.mycosmeticshop.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BrandDAO {

    /* ===================== FRONTEND / ADMIN ===================== */

    /**
     * Lấy danh sách brand + số lượng sản phẩm
     * Dùng cho:
     * - sidebar filter
     * - dropdown chọn brand
     * - admin brand discount
     */
    public List<Brand> findAllWithProductCount() {

        List<Brand> list = new ArrayList<>();

        String sql =
            "SELECT b.id, b.name, COUNT(p.id) AS product_count " +
            "FROM store_brand b " +
            "LEFT JOIN store_product p ON p.brand_id = b.id " +
            "GROUP BY b.id, b.name " +
            "ORDER BY b.name";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Brand b = new Brand();
                b.setId(rs.getInt("id"));
                b.setName(rs.getString("name"));
                b.setProductCount(rs.getInt("product_count"));
                list.add(b);
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDAO.findAllWithProductCount error", e);
        }

        return list;
    }

    /**
     * ⚠️ Vì bảng store_brand KHÔNG có cột active
     * => tất cả brand đều được coi là active
     * => method này chỉ là alias để KHÔNG phá code servlet hiện có
     */
    public List<Brand> findAllActive() {
        return findAllWithProductCount();
    }

    /**
     * Alias cho code cũ
     */
    public List<Brand> findWithProductCount() {
        return findAllWithProductCount();
    }

    /* ===================== ADMIN BASIC CRUD ===================== */

    public List<Brand> findAll() {

        List<Brand> list = new ArrayList<>();

        String sql =
            "SELECT id, name " +
            "FROM store_brand " +
            "ORDER BY id DESC";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Brand b = new Brand();
                b.setId(rs.getInt("id"));
                b.setName(rs.getString("name"));
                list.add(b);
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDAO.findAll error", e);
        }

        return list;
    }

    public Brand findById(int id) {

        String sql =
            "SELECT id, name " +
            "FROM store_brand " +
            "WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Brand b = new Brand();
                    b.setId(rs.getInt("id"));
                    b.setName(rs.getString("name"));
                    return b;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDAO.findById error", e);
        }

        return null;
    }

    public void create(String name) {

        String sql =
            "INSERT INTO store_brand (name) " +
            "VALUES (?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("BrandDAO.create error", e);
        }
    }

    public void update(int id, String name) {

        String sql =
            "UPDATE store_brand " +
            "SET name = ? " +
            "WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("BrandDAO.update error", e);
        }
    }

    /**
     * Xóa brand
     * ⚠ Nếu brand đang được product dùng → SQL Server báo lỗi FK (547)
     */
    public void delete(int id) {

        String sql =
            "DELETE FROM store_brand " +
            "WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 547) {
                throw new RuntimeException(
                    "Không thể xóa brand vì đang được sản phẩm sử dụng.", e);
            }
            throw new RuntimeException("BrandDAO.delete error", e);
        }
    }
}

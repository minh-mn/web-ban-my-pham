package com.webshop.app.dao;

import com.webshop.app.model.ProductVariant;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductVariantDAO {

    public List<ProductVariant> findActiveByProductId(int productId) {
        String sql =
                "SELECT id, product_id, size, type, extra_price, stock, active " +
                        "FROM store_product_variant " +
                        "WHERE product_id = ? AND active = 1 " +
                        "ORDER BY id ASC";

        List<ProductVariant> variants = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    variants.add(mapRow(rs));
                }
            }

            return variants;

        } catch (SQLException e) {
            throw new RuntimeException("ProductVariantDAO.findActiveByProductId error", e);
        }
    }

    public ProductVariant findActiveByIdAndProductId(int variantId, int productId) {
        String sql =
                "SELECT id, product_id, size, type, extra_price, stock, active " +
                        "FROM store_product_variant " +
                        "WHERE id = ? AND product_id = ? AND active = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, variantId);
            ps.setInt(2, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("ProductVariantDAO.findActiveByIdAndProductId error", e);
        }
    }

    private ProductVariant mapRow(ResultSet rs) throws SQLException {
        ProductVariant v = new ProductVariant();

        v.setId(rs.getInt("id"));
        v.setProductId(rs.getInt("product_id"));
        v.setSize(rs.getString("size"));
        v.setType(rs.getString("type"));
        v.setExtraPrice(rs.getBigDecimal("extra_price"));
        v.setStock(rs.getInt("stock"));
        v.setActive(rs.getBoolean("active"));

        return v;
    }

    public void deleteByProductId(int productId) {
        String sql = "DELETE FROM store_product_variant WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa biến thể cũ", e);
        }
    }

    public void insert(ProductVariant v) {
        String sql = "INSERT INTO store_product_variant (product_id, size, type, extra_price, stock, active, created_at) VALUES (?, ?, ?, ?, ?, 1, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, v.getProductId());
            ps.setString(2, v.getSize());
            ps.setString(3, v.getType());
            ps.setBigDecimal(4, v.getExtraPrice());
            ps.setInt(5, v.getStock());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi thêm biến thể mới: " + e.getMessage(), e);
        }
    }
}

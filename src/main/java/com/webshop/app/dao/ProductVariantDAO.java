package com.webshop.app.dao;

import com.webshop.app.model.ProductVariant;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProductVariantDAO {

    private static final String BASE_SELECT = """
            SELECT
                id,
                product_id,
                sku,
                size,
                color,
                type,
                extra_price,
                stock,
                min_stock,
                active
            FROM store_product_variant
            """;

    public List<ProductVariant> findActiveByProductId(int productId) {
        String sql = BASE_SELECT + """
                WHERE product_id = ? AND active = 1
                ORDER BY id ASC
                """;

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
        String sql = BASE_SELECT + """
                WHERE id = ? AND product_id = ? AND active = 1
                """;

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
        String sql = """
                INSERT INTO store_product_variant
                    (product_id, sku, size, color, type, extra_price, stock, min_stock, active, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, NOW())
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, v.getProductId());
            setNullableString(ps, 2, v.getSku());
            setNullableString(ps, 3, v.getSize());
            setNullableString(ps, 4, v.getColor());
            setNullableString(ps, 5, v.getType());
            ps.setBigDecimal(6, v.getExtraPrice());
            ps.setInt(7, v.getStock());
            ps.setInt(8, v.getMinStock());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thêm biến thể mới: " + e.getMessage(), e);
        }
    }

    private ProductVariant mapRow(ResultSet rs) throws SQLException {
        ProductVariant v = new ProductVariant();

        v.setId(rs.getInt("id"));
        v.setProductId(rs.getInt("product_id"));
        v.setSku(rs.getString("sku"));
        v.setSize(rs.getString("size"));
        v.setColor(rs.getString("color"));
        v.setType(rs.getString("type"));
        v.setExtraPrice(rs.getBigDecimal("extra_price"));
        v.setStock(rs.getInt("stock"));
        v.setMinStock(rs.getInt("min_stock"));
        v.setActive(rs.getBoolean("active"));

        return v;
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }
}

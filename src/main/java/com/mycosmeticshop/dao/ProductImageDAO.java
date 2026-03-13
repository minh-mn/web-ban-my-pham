package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.ProductImage;
import com.mycosmeticshop.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductImageDAO {

    public List<ProductImage> findByProductId(int productId) {
        String sql =
            "SELECT id, image, [order], product_id " +
            "FROM store_productimage " +
            "WHERE product_id = ? " +
            "ORDER BY [order] ASC, id ASC";

        List<ProductImage> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductImage pi = new ProductImage();
                    pi.setId(rs.getLong("id"));
                    pi.setImage(rs.getString("image"));
                    pi.setOrder(rs.getInt("order"));        // label "order" OK dù SQL dùng [order]
                    pi.setProductId(rs.getInt("product_id"));
                    list.add(pi);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("ProductImageDAO.findByProductId error", e);
        }

        return list;
    }

    public void insert(ProductImage img) {
        String sql =
            "INSERT INTO store_productimage (image, [order], product_id) " +
            "VALUES (?, ?, ?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, img.getImage());
            ps.setInt(2, img.getOrder());
            ps.setInt(3, img.getProductId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("ProductImageDAO.insert error", e);
        }
    }

    public void insert(int productId, String image, int order) {
        ProductImage img = new ProductImage();
        img.setProductId(productId);
        img.setImage(image);
        img.setOrder(order);
        insert(img);
    }

    public void deleteByProductId(int productId) {
        String sql = "DELETE FROM store_productimage WHERE product_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("ProductImageDAO.deleteByProductId error", e);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM store_productimage WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("ProductImageDAO.deleteById error", e);
        }
    }

    public void replaceAll(int productId, List<String> images) {
        // Xoá hết ảnh cũ rồi insert lại theo thứ tự mới
        deleteByProductId(productId);

        int order = 0;
        for (String imgPath : images) {
            if (imgPath == null) continue;
            String t = imgPath.trim();
            if (t.isEmpty()) continue;
            insert(productId, t, order++);
        }
    }
}

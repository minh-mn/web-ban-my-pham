package com.webshop.app.dao;

import com.webshop.app.model.ProductImage;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductImageDAO {

    public List<ProductImage> findByProductId(int productId) {
        String sql =
                "SELECT id, image, `order`, product_id " +
                        "FROM store_productimage " +
                        "WHERE product_id = ? " +
                        "ORDER BY `order` ASC, id ASC";

        List<ProductImage> list = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductImage productImage = new ProductImage();

                    productImage.setId(rs.getLong("id"));
                    productImage.setImage(rs.getString("image"));
                    productImage.setOrder(rs.getInt("order"));
                    productImage.setProductId(rs.getInt("product_id"));

                    list.add(productImage);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("ProductImageDAO.findByProductId error", e);
        }

        return list;
    }

    public void insert(ProductImage image) {
        try (Connection connection = DBConnection.getConnection()) {
            insert(connection, image);
        } catch (SQLException e) {
            throw new RuntimeException("ProductImageDAO.insert error", e);
        }
    }

    public void insert(int productId, String image, int order) {
        ProductImage productImage = new ProductImage();

        productImage.setProductId(productId);
        productImage.setImage(image);
        productImage.setOrder(order);

        insert(productImage);
    }

    public void deleteByProductId(int productId) {
        try (Connection connection = DBConnection.getConnection()) {
            deleteByProductId(connection, productId);
        } catch (SQLException e) {
            throw new RuntimeException("ProductImageDAO.deleteByProductId error", e);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM store_productimage WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("ProductImageDAO.deleteById error", e);
        }
    }

    /*
     * Thay toàn bộ ảnh gallery của sản phẩm trong transaction.
     *
     * Mục tiêu:
     * - Xóa gallery cũ.
     * - Insert gallery mới.
     * - Nếu insert lỗi thì rollback, tránh mất dữ liệu gallery giữa chừng.
     */
    public void replaceAll(int productId, List<String> images) {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                deleteByProductId(connection, productId);

                int order = 0;

                if (images != null) {
                    for (String imagePath : images) {
                        String normalizedImagePath = normalizeImagePath(imagePath);

                        if (normalizedImagePath == null) {
                            continue;
                        }

                        ProductImage productImage = new ProductImage();
                        productImage.setProductId(productId);
                        productImage.setImage(normalizedImagePath);
                        productImage.setOrder(order++);

                        insert(connection, productImage);
                    }
                }

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (Exception e) {
            throw new RuntimeException("ProductImageDAO.replaceAll error", e);
        }
    }

    private void insert(Connection connection, ProductImage image) throws SQLException {
        String sql =
                "INSERT INTO store_productimage (image, `order`, product_id) " +
                        "VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, image.getImage());
            ps.setInt(2, image.getOrder());
            ps.setInt(3, image.getProductId());

            ps.executeUpdate();
        }
    }

    private void deleteByProductId(Connection connection, int productId) throws SQLException {
        String sql = "DELETE FROM store_productimage WHERE product_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        }
    }

    private String normalizeImagePath(String imagePath) {
        if (imagePath == null) {
            return null;
        }

        String trimmedPath = imagePath.trim();

        if (trimmedPath.isEmpty()) {
            return null;
        }

        return trimmedPath;
    }
}
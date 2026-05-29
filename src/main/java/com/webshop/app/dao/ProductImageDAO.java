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
                    list.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("ProductImageDAO.findByProductId error", e);
        }

        return list;
    }

    public ProductImage findById(long id) {
        String sql =
                "SELECT id, image, `order`, product_id " +
                        "FROM store_productimage " +
                        "WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("ProductImageDAO.findById error", e);
        }
    }

    public ProductImage findByIdAndProductId(long id, int productId) {
        String sql =
                "SELECT id, image, `order`, product_id " +
                        "FROM store_productimage " +
                        "WHERE id = ? AND product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.setInt(2, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("ProductImageDAO.findByIdAndProductId error", e);
        }
    }

    public String findImageUrlByIdAndProductId(long id, int productId) {
        String sql =
                "SELECT image " +
                        "FROM store_productimage " +
                        "WHERE id = ? AND product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.setInt(2, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("image");
                }

                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("ProductImageDAO.findImageUrlByIdAndProductId error", e);
        }
    }

    public List<String> findImageUrlsByProductId(int productId) {
        String sql =
                "SELECT image " +
                        "FROM store_productimage " +
                        "WHERE product_id = ? " +
                        "ORDER BY `order` ASC, id ASC";

        List<String> urls = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    urls.add(rs.getString("image"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("ProductImageDAO.findImageUrlsByProductId error", e);
        }

        return urls;
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
        productImage.setImage(normalizeImagePath(image));
        productImage.setOrder(Math.max(order, 0));

        insert(productImage);
    }

    public int deleteByProductId(int productId) {
        try (Connection connection = DBConnection.getConnection()) {
            return deleteByProductId(connection, productId);
        } catch (SQLException e) {
            throw new RuntimeException("ProductImageDAO.deleteByProductId error", e);
        }
    }

    public boolean deleteById(long id) {
        String sql = "DELETE FROM store_productimage WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("ProductImageDAO.deleteById error", e);
        }
    }

    public boolean deleteByIdAndProductId(long id, int productId) {
        String sql =
                "DELETE FROM store_productimage " +
                        "WHERE id = ? AND product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.setInt(2, productId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("ProductImageDAO.deleteByIdAndProductId error", e);
        }
    }

    /*
     * Thay toàn bộ ảnh gallery của sản phẩm trong transaction.
     *
     * Lưu ý:
     * - Hàm này chỉ xử lý SQL.
     * - Nếu cần xóa file vật lý của gallery cũ thì servlet/service phải lấy danh sách URL cũ
     *   trước khi gọi replaceAll rồi gọi UploadConfig.deleteProductGalleryFileByUrl(...).
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

        String normalizedImage = normalizeImagePath(image == null ? null : image.getImage());

        if (normalizedImage == null) {
            throw new SQLException("Product image path is empty.");
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, normalizedImage);
            ps.setInt(2, image.getOrder());
            ps.setInt(3, image.getProductId());

            ps.executeUpdate();
        }
    }

    private int deleteByProductId(Connection connection, int productId) throws SQLException {
        String sql = "DELETE FROM store_productimage WHERE product_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, productId);

            return ps.executeUpdate();
        }
    }

    private ProductImage mapRow(ResultSet rs) throws SQLException {
        ProductImage productImage = new ProductImage();

        productImage.setId(rs.getLong("id"));
        productImage.setImage(rs.getString("image"));
        productImage.setOrder(rs.getInt("order"));
        productImage.setProductId(rs.getInt("product_id"));

        return productImage;
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
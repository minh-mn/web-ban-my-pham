package com.webshop.app.dao;

import com.webshop.app.config.UploadConfig;
import com.webshop.app.model.Brand;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BrandDAO {

    private static final int MYSQL_FOREIGN_KEY_CONSTRAINT_ERROR = 1451;
    private static final int MYSQL_DUPLICATE_ENTRY_ERROR = 1062;

    private static final String TABLE_BRAND = "store_brand";
    private static final String COLUMN_IMAGE = "image";

    /* =========================================================
       FRONTEND / ADMIN
    ========================================================= */

    public List<Brand> findAllWithProductCount() {
        List<Brand> brands = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection()) {
            boolean hasImageColumn = hasBrandImageColumn(connection);

            String sql = hasImageColumn
                    ? """
                    SELECT
                        b.id,
                        b.name,
                        b.image,
                        COUNT(DISTINCT p.id) AS product_count
                    FROM store_brand b
                    LEFT JOIN store_product p ON p.brand_id = b.id
                    GROUP BY b.id, b.name, b.image
                    ORDER BY b.name ASC
                    """
                    : """
                    SELECT
                        b.id,
                        b.name,
                        COUNT(DISTINCT p.id) AS product_count
                    FROM store_brand b
                    LEFT JOIN store_product p ON p.brand_id = b.id
                    GROUP BY b.id, b.name
                    ORDER BY b.name ASC
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    brands.add(mapRowWithProductCount(resultSet, hasImageColumn));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDAO.findAllWithProductCount error", e);
        }

        return brands;
    }

    public List<Brand> findAllActive() {
        return findAllWithProductCount();
    }

    public List<Brand> findWithProductCount() {
        return findAllWithProductCount();
    }

    /* =========================================================
       ADMIN BASIC CRUD
    ========================================================= */

    public List<Brand> findAll() {
        List<Brand> brands = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection()) {
            boolean hasImageColumn = hasBrandImageColumn(connection);

            String sql = hasImageColumn
                    ? """
                    SELECT
                        b.id,
                        b.name,
                        b.image,
                        COUNT(DISTINCT p.id) AS product_count
                    FROM store_brand b
                    LEFT JOIN store_product p ON p.brand_id = b.id
                    GROUP BY b.id, b.name, b.image
                    ORDER BY b.id DESC
                    """
                    : """
                    SELECT
                        b.id,
                        b.name,
                        COUNT(DISTINCT p.id) AS product_count
                    FROM store_brand b
                    LEFT JOIN store_product p ON p.brand_id = b.id
                    GROUP BY b.id, b.name
                    ORDER BY b.id DESC
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    brands.add(mapRowWithProductCount(resultSet, hasImageColumn));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDAO.findAll error", e);
        }

        return brands;
    }

    public Brand findById(int id) {
        validateId(id);

        try (Connection connection = DBConnection.getConnection()) {
            boolean hasImageColumn = hasBrandImageColumn(connection);

            String sql = hasImageColumn
                    ? """
                    SELECT id, name, image
                    FROM store_brand
                    WHERE id = ?
                    """
                    : """
                    SELECT id, name
                    FROM store_brand
                    WHERE id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return mapRow(resultSet, hasImageColumn);
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDAO.findById error", e);
        }

        return null;
    }

    /*
     * Tương thích code cũ:
     * brandDAO.create(name)
     */
    public void create(String name) {
        create(name, null);
    }

    /*
     * Thêm thương hiệu.
     * Nếu database có cột image thì lưu logo.
     * Nếu database chưa có cột image thì vẫn thêm được brand, không lỗi SQL.
     */
    public void create(String name, String image) {
        validateName(name);

        try (Connection connection = DBConnection.getConnection()) {
            boolean hasImageColumn = hasBrandImageColumn(connection);

            String sql = hasImageColumn
                    ? """
                    INSERT INTO store_brand (name, image)
                    VALUES (?, ?)
                    """
                    : """
                    INSERT INTO store_brand (name)
                    VALUES (?)
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, normalizeName(name));

                if (hasImageColumn) {
                    statement.setString(2, normalizeImage(image));
                }

                statement.executeUpdate();
            }

        } catch (SQLException e) {
            if (isDuplicateNameError(e)) {
                throw new IllegalArgumentException("Tên thương hiệu đã tồn tại.");
            }

            throw new RuntimeException("BrandDAO.create error", e);
        }
    }

    /*
     * Tương thích code cũ:
     * brandDAO.update(id, name)
     *
     * Khi chỉ sửa tên, giữ lại logo cũ.
     */
    public void update(int id, String name) {
        validateId(id);
        validateName(name);

        Brand currentBrand = findById(id);
        String currentImage = currentBrand != null ? currentBrand.getImage() : null;

        update(id, name, currentImage);
    }

    /*
     * Cập nhật thương hiệu.
     * Nếu database có cột image thì cập nhật cả name và logo.
     * Nếu database chưa có cột image thì chỉ cập nhật name.
     */
    public void update(int id, String name, String image) {
        validateId(id);
        validateName(name);

        try (Connection connection = DBConnection.getConnection()) {
            boolean hasImageColumn = hasBrandImageColumn(connection);

            String sql = hasImageColumn
                    ? """
                    UPDATE store_brand
                    SET name = ?,
                        image = ?
                    WHERE id = ?
                    """
                    : """
                    UPDATE store_brand
                    SET name = ?
                    WHERE id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, normalizeName(name));

                if (hasImageColumn) {
                    statement.setString(2, normalizeImage(image));
                    statement.setInt(3, id);
                } else {
                    statement.setInt(2, id);
                }

                statement.executeUpdate();
            }

        } catch (SQLException e) {
            if (isDuplicateNameError(e)) {
                throw new IllegalArgumentException("Tên thương hiệu đã tồn tại.");
            }

            throw new RuntimeException("BrandDAO.update error", e);
        }
    }

    public void delete(int id) {
        validateId(id);

        String sql = """
                DELETE FROM store_brand
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == MYSQL_FOREIGN_KEY_CONSTRAINT_ERROR) {
                throw new IllegalArgumentException(
                        "Không thể xóa thương hiệu vì đang được sản phẩm sử dụng."
                );
            }

            throw new RuntimeException("BrandDAO.delete error", e);
        }
    }

    /* =========================================================
       SCHEMA HELPERS
    ========================================================= */

    private boolean hasBrandImageColumn(Connection connection) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, TABLE_BRAND);
            statement.setString(2, COLUMN_IMAGE);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /* =========================================================
       VALIDATION / NORMALIZE
    ========================================================= */

    private void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Brand id không hợp lệ.");
        }
    }

    private void validateName(String name) {
        String normalizedName = normalizeName(name);

        if (normalizedName == null || normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Tên thương hiệu không được để trống.");
        }

        if (normalizedName.length() > 100) {
            throw new IllegalArgumentException("Tên thương hiệu không được vượt quá 100 ký tự.");
        }
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private String normalizeImage(String image) {
        if (image == null || image.trim().isEmpty()) {
            return null;
        }

        return UploadConfig.normalizeBrandImageUrl(image);
    }

    private boolean isDuplicateNameError(SQLException e) {
        return e.getErrorCode() == MYSQL_DUPLICATE_ENTRY_ERROR;
    }

    /* =========================================================
       MAPPER
    ========================================================= */

    private Brand mapRow(ResultSet resultSet, boolean hasImageColumn) throws SQLException {
        Brand brand = new Brand();

        brand.setId(resultSet.getInt("id"));
        brand.setName(resultSet.getString("name"));

        if (hasImageColumn) {
            brand.setImage(resultSet.getString("image"));
        } else {
            brand.setImage(null);
        }

        return brand;
    }

    private Brand mapRowWithProductCount(ResultSet resultSet, boolean hasImageColumn) throws SQLException {
        Brand brand = mapRow(resultSet, hasImageColumn);
        brand.setProductCount(resultSet.getInt("product_count"));

        return brand;
    }
}
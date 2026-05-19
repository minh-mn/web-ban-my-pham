package com.webshop.app.dao;

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

    /* ===================== FRONTEND / ADMIN ===================== */

    public List<Brand> findAllWithProductCount() {
        List<Brand> brands = new ArrayList<>();

        String sql = """
                SELECT b.id, b.name, COUNT(p.id) AS product_count
                FROM store_brand b
                LEFT JOIN store_product p ON p.brand_id = b.id
                GROUP BY b.id, b.name
                ORDER BY b.name
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                brands.add(mapRowWithProductCount(resultSet));
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

    /* ===================== ADMIN BASIC CRUD ===================== */

    public List<Brand> findAll() {
        List<Brand> brands = new ArrayList<>();

        String sql = """
                SELECT id, name
                FROM store_brand
                ORDER BY id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                brands.add(mapRow(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDAO.findAll error", e);
        }

        return brands;
    }

    public Brand findById(int id) {
        String sql = """
                SELECT id, name
                FROM store_brand
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDAO.findById error", e);
        }

        return null;
    }

    public void create(String name) {
        String sql = """
                INSERT INTO store_brand (name)
                VALUES (?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("BrandDAO.create error", e);
        }
    }

    public void update(int id, String name) {
        String sql = """
                UPDATE store_brand
                SET name = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setInt(2, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("BrandDAO.update error", e);
        }
    }

    public void delete(int id) {
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
                throw new RuntimeException(
                        "Không thể xóa thương hiệu vì đang được sản phẩm sử dụng.", e
                );
            }

            throw new RuntimeException("BrandDAO.delete error", e);
        }
    }

    private Brand mapRow(ResultSet resultSet) throws SQLException {
        Brand brand = new Brand();

        brand.setId(resultSet.getInt("id"));
        brand.setName(resultSet.getString("name"));

        return brand;
    }

    private Brand mapRowWithProductCount(ResultSet resultSet) throws SQLException {
        Brand brand = mapRow(resultSet);

        brand.setProductCount(resultSet.getInt("product_count"));

        return brand;
    }
}
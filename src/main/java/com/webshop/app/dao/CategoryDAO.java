package com.webshop.app.dao;

import com.webshop.app.model.Category;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryDAO {

    private static final int MYSQL_FOREIGN_KEY_CONSTRAINT_ERROR = 1451;

    /* =====================================================
       FRONTEND: CATEGORY TREE
    ===================================================== */

    public List<Category> findParents() {

        Map<Integer, Integer> countMap = countActiveProductsByCategory();

        List<Category> parents = new ArrayList<>();

        String sql = """
                SELECT id, name, slug, is_active
                FROM store_category
                WHERE parent_id IS NULL
                  AND is_active = 1
                ORDER BY name ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Category parent = mapCategory(resultSet);

                int parentId = parent.getId();

                parent.setChildren(findChildren(parentId, countMap));

                int totalProducts = 0;

                for (Category child : parent.getChildren()) {
                    totalProducts += child.getProductCount();
                }

                parent.setProductCount(totalProducts);

                parents.add(parent);
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findParents error", e);
        }

        return parents;
    }

    private List<Category> findChildren(
            int parentId,
            Map<Integer, Integer> countMap
    ) {

        List<Category> children = new ArrayList<>();

        String sql = """
                SELECT id, name, slug, is_active
                FROM store_category
                WHERE parent_id = ?
                  AND is_active = 1
                ORDER BY name ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, parentId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    Category child = mapCategory(resultSet);

                    child.setParentId(parentId);

                    child.setProductCount(
                            countMap.getOrDefault(child.getId(), 0)
                    );

                    children.add(child);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findChildren error", e);
        }

        return children;
    }

    private Map<Integer, Integer> countActiveProductsByCategory() {

        Map<Integer, Integer> productCountMap = new HashMap<>();

        String sql = """
                SELECT category_id, COUNT(*) AS cnt
                FROM store_product
                WHERE is_active = 1
                GROUP BY category_id
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                productCountMap.put(
                        resultSet.getInt("category_id"),
                        resultSet.getInt("cnt")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "CategoryDAO.countActiveProductsByCategory error",
                    e
            );
        }

        return productCountMap;
    }

    /* =====================================================
       ADMIN CRUD
    ===================================================== */

    public List<Category> findAll() {

        Map<Integer, Integer> countMap = countActiveProductsByCategory();

        List<Category> categories = new ArrayList<>();

        String sql = """
                SELECT id, name, slug, parent_id, is_active
                FROM store_category
                ORDER BY parent_id ASC, name ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Category category = mapCategory(resultSet);

                int parentId = resultSet.getInt("parent_id");

                category.setParentId(
                        resultSet.wasNull() ? null : parentId
                );

                category.setProductCount(
                        countMap.getOrDefault(category.getId(), 0)
                );

                categories.add(category);
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findAll error", e);
        }

        return categories;
    }

    public Category findById(int id) {

        String sql = """
                SELECT id, name, slug, parent_id, is_active
                FROM store_category
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                Category category = mapCategory(resultSet);

                int parentId = resultSet.getInt("parent_id");

                category.setParentId(
                        resultSet.wasNull() ? null : parentId
                );

                return category;
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findById error", e);
        }
    }

    public List<Category> findAllParents() {

        List<Category> parents = new ArrayList<>();

        String sql = """
                SELECT id, name, slug, is_active
                FROM store_category
                WHERE parent_id IS NULL
                ORDER BY name ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                parents.add(mapCategory(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findAllParents error", e);
        }

        return parents;
    }

    public void create(Category category) {

        String sql = """
                INSERT INTO store_category
                (name, slug, parent_id, is_active)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, category.getName());
            statement.setString(2, category.getSlug());

            if (category.getParentId() == null) {
                statement.setNull(3, Types.INTEGER);
            } else {
                statement.setInt(3, category.getParentId());
            }

            statement.setBoolean(4, category.isActive());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.create error", e);
        }
    }

    public void update(Category category) {

        String sql = """
                UPDATE store_category
                SET name = ?,
                    slug = ?,
                    parent_id = ?,
                    is_active = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, category.getName());
            statement.setString(2, category.getSlug());

            if (category.getParentId() == null) {
                statement.setNull(3, Types.INTEGER);
            } else {
                statement.setInt(3, category.getParentId());
            }

            statement.setBoolean(4, category.isActive());
            statement.setInt(5, category.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.update error", e);
        }
    }

    public void delete(int id) {

        String sql = """
                DELETE FROM store_category
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {

            if (e.getErrorCode() == MYSQL_FOREIGN_KEY_CONSTRAINT_ERROR) {

                throw new RuntimeException(
                        "Không thể xóa danh mục vì đang có sản phẩm hoặc danh mục con.",
                        e
                );
            }

            throw new RuntimeException("CategoryDAO.delete error", e);
        }
    }

    /* =====================================================
       SLUG CHECK
    ===================================================== */

    public boolean existsBySlug(String slug) {

        String sql = """
                SELECT 1
                FROM store_category
                WHERE slug = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, slug);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.existsBySlug error", e);
        }
    }

    public boolean existsBySlugExceptId(String slug, int id) {

        String sql = """
                SELECT 1
                FROM store_category
                WHERE slug = ?
                  AND id <> ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, slug);
            statement.setInt(2, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "CategoryDAO.existsBySlugExceptId error",
                    e
            );
        }
    }

    public List<Category> findActiveForMenu() {

        List<Category> categories = new ArrayList<>();

        String sql = """
                SELECT id, name, slug
                FROM store_category
                WHERE is_active = 1
                ORDER BY name ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Category category = new Category();

                category.setId(resultSet.getInt("id"));
                category.setName(resultSet.getString("name"));
                category.setSlug(resultSet.getString("slug"));

                categories.add(category);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "CategoryDAO.findActiveForMenu error",
                    e
            );
        }

        return categories;
    }

    /* =====================================================
       MAPPER
    ===================================================== */

    private Category mapCategory(ResultSet resultSet)
            throws SQLException {

        Category category = new Category();

        category.setId(resultSet.getInt("id"));
        category.setName(resultSet.getString("name"));
        category.setSlug(resultSet.getString("slug"));

        try {
            category.setActive(resultSet.getBoolean("is_active"));
        } catch (SQLException ignored) {
        }

        return category;
    }
}
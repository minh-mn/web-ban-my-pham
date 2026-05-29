package com.webshop.app.dao;

import com.webshop.app.model.CategoryTag;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CategoryTagDAO {

    /* =====================================================
       FIND
    ===================================================== */

    public List<CategoryTag> findAllByCategoryId(int categoryId) {
        List<CategoryTag> tags = new ArrayList<>();

        if (categoryId <= 0) {
            return tags;
        }

        String sql = """
                SELECT id, category_id, name, slug, display_order, is_active
                FROM store_category_tag
                WHERE category_id = ?
                ORDER BY display_order ASC, id ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tags.add(mapRow(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryTagDAO.findAllByCategoryId error", e);
        }

        return tags;
    }

    public List<CategoryTag> findActiveByCategoryId(int categoryId) {
        List<CategoryTag> tags = new ArrayList<>();

        if (categoryId <= 0) {
            return tags;
        }

        String sql = """
                SELECT id, category_id, name, slug, display_order, is_active
                FROM store_category_tag
                WHERE category_id = ?
                  AND is_active = 1
                ORDER BY display_order ASC, id ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tags.add(mapRow(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryTagDAO.findActiveByCategoryId error", e);
        }

        return tags;
    }

    public Map<Integer, List<CategoryTag>> findActiveByCategoryIds(List<Integer> categoryIds) {
        Map<Integer, List<CategoryTag>> result = new LinkedHashMap<>();

        List<Integer> cleanedIds = cleanCategoryIds(categoryIds);

        if (cleanedIds.isEmpty()) {
            return result;
        }

        for (Integer categoryId : cleanedIds) {
            result.put(categoryId, new ArrayList<>());
        }

        String sql =
                "SELECT id, category_id, name, slug, display_order, is_active " +
                        "FROM store_category_tag " +
                        "WHERE is_active = 1 " +
                        "AND category_id IN (" + placeholders(cleanedIds.size()) + ") " +
                        "ORDER BY category_id ASC, display_order ASC, id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            int index = 1;

            for (Integer categoryId : cleanedIds) {
                statement.setInt(index++, categoryId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    CategoryTag tag = mapRow(resultSet);
                    result.computeIfAbsent(tag.getCategoryId(), key -> new ArrayList<>()).add(tag);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryTagDAO.findActiveByCategoryIds error", e);
        }

        return result;
    }

    public Map<Integer, Integer> countActiveByCategory() {
        Map<Integer, Integer> result = new LinkedHashMap<>();

        String sql = """
                SELECT category_id, COUNT(*) AS tag_count
                FROM store_category_tag
                WHERE is_active = 1
                GROUP BY category_id
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                result.put(
                        resultSet.getInt("category_id"),
                        resultSet.getInt("tag_count")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryTagDAO.countActiveByCategory error", e);
        }

        return result;
    }

    /* =====================================================
       CREATE / UPDATE / DELETE
    ===================================================== */

    public int create(CategoryTag tag) {
        validateTag(tag);

        String sql = """
                INSERT INTO store_category_tag
                (category_id, name, slug, display_order, is_active)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindTag(statement, tag);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }

                return 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryTagDAO.create error", e);
        }
    }

    public boolean update(CategoryTag tag) {
        if (tag == null || tag.getId() <= 0) {
            throw new RuntimeException("ID thẻ danh mục không hợp lệ.");
        }

        validateTag(tag);

        String sql = """
                UPDATE store_category_tag
                SET category_id = ?,
                    name = ?,
                    slug = ?,
                    display_order = ?,
                    is_active = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            bindTag(statement, tag);
            statement.setInt(6, tag.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("CategoryTagDAO.update error", e);
        }
    }

    public boolean delete(int id) {
        if (id <= 0) {
            return false;
        }

        String sql = """
                DELETE FROM store_category_tag
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("CategoryTagDAO.delete error", e);
        }
    }

    public void deleteByCategoryId(int categoryId) {
        if (categoryId <= 0) {
            return;
        }

        String sql = """
                DELETE FROM store_category_tag
                WHERE category_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CategoryTagDAO.deleteByCategoryId error", e);
        }
    }

    /**
     * Dùng khi admin sửa danh mục:
     * - Xóa toàn bộ tag cũ của category.
     * - Thêm lại danh sách tag mới từ form.
     */
    public void replaceByCategoryId(int categoryId, List<CategoryTag> tags) {
        if (categoryId <= 0) {
            throw new RuntimeException("ID danh mục không hợp lệ khi lưu tag.");
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                deleteByCategoryId(connection, categoryId);

                List<CategoryTag> normalizedTags = normalizeTags(categoryId, tags);

                for (CategoryTag tag : normalizedTags) {
                    insertTag(connection, tag);
                }

                connection.commit();

            } catch (Exception e) {
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (Exception e) {
            throw new RuntimeException("CategoryTagDAO.replaceByCategoryId error", e);
        }
    }

    /* =====================================================
       PRIVATE SQL HELPERS
    ===================================================== */

    private void deleteByCategoryId(Connection connection, int categoryId) throws SQLException {
        String sql = """
                DELETE FROM store_category_tag
                WHERE category_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            statement.executeUpdate();
        }
    }

    private void insertTag(Connection connection, CategoryTag tag) throws SQLException {
        String sql = """
                INSERT INTO store_category_tag
                (category_id, name, slug, display_order, is_active)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindTag(statement, tag);
            statement.executeUpdate();
        }
    }

    private void bindTag(PreparedStatement statement, CategoryTag tag) throws SQLException {
        String name = cleanText(tag.getName());
        String slug = cleanText(tag.getSlug());

        if (slug == null || slug.isBlank()) {
            slug = toSlug(name);
        }

        statement.setInt(1, tag.getCategoryId());
        statement.setString(2, name);
        statement.setString(3, slug);
        statement.setInt(4, Math.max(tag.getDisplayOrder(), 0));
        statement.setBoolean(5, tag.isActive());
    }

    private CategoryTag mapRow(ResultSet resultSet) throws SQLException {
        CategoryTag tag = new CategoryTag();

        tag.setId(resultSet.getInt("id"));
        tag.setCategoryId(resultSet.getInt("category_id"));
        tag.setName(resultSet.getString("name"));
        tag.setSlug(resultSet.getString("slug"));
        tag.setDisplayOrder(resultSet.getInt("display_order"));
        tag.setActive(resultSet.getBoolean("is_active"));

        /*
         * Không bắt buộc SELECT created_at / updated_at.
         * Nếu database hoặc query có 2 cột này thì set thêm,
         * nếu không có thì bỏ qua để tránh lỗi trang trắng.
         */
        if (hasColumn(resultSet, "created_at")) {
            tag.setCreatedAt(resultSet.getTimestamp("created_at"));
        }

        if (hasColumn(resultSet, "updated_at")) {
            tag.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        }

        return tag;
    }

    private boolean hasColumn(ResultSet resultSet, String columnName) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                return true;
            }
        }

        return false;
    }

    /* =====================================================
       VALIDATION / NORMALIZE
    ===================================================== */

    private void validateTag(CategoryTag tag) {
        if (tag == null) {
            throw new RuntimeException("Dữ liệu tag danh mục không hợp lệ.");
        }

        if (tag.getCategoryId() <= 0) {
            throw new RuntimeException("Tag cần thuộc một danh mục hợp lệ.");
        }

        if (isBlank(tag.getName())) {
            throw new RuntimeException("Tên tag không được để trống.");
        }
    }

    private List<CategoryTag> normalizeTags(int categoryId, List<CategoryTag> tags) {
        List<CategoryTag> result = new ArrayList<>();

        if (tags == null || tags.isEmpty()) {
            return result;
        }

        int order = 1;

        for (CategoryTag tag : tags) {
            if (tag == null || isBlank(tag.getName())) {
                continue;
            }

            tag.setCategoryId(categoryId);

            if (tag.getDisplayOrder() <= 0) {
                tag.setDisplayOrder(order);
            }

            if (isBlank(tag.getSlug())) {
                tag.setSlug(toSlug(tag.getName()));
            } else {
                tag.setSlug(toSlug(tag.getSlug()));
            }

            result.add(tag);
            order++;
        }

        return result;
    }

    private List<Integer> cleanCategoryIds(List<Integer> categoryIds) {
        List<Integer> result = new ArrayList<>();

        if (categoryIds == null || categoryIds.isEmpty()) {
            return result;
        }

        for (Integer categoryId : categoryIds) {
            if (categoryId != null && categoryId > 0 && !result.contains(categoryId)) {
                result.add(categoryId);
            }
        }

        return result;
    }

    private String placeholders(int size) {
        if (size <= 0) {
            return "";
        }

        return String.join(",", java.util.Collections.nCopies(size, "?"));
    }

    private String cleanText(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String toSlug(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "tag";
        }

        String text = value.trim().toLowerCase(Locale.ROOT);
        text = text.replace("đ", "d").replace("Đ", "D");

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        normalized = normalized.replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceAll("^-+", "");
        normalized = normalized.replaceAll("-+$", "");

        return normalized.isBlank() ? "tag" : normalized;
    }
}
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CategoryDAO {

    private static final int MYSQL_FOREIGN_KEY_CONSTRAINT_ERROR = 1451;
    private static final int MYSQL_DUPLICATE_ENTRY_ERROR = 1062;

    /* =====================================================
       FRONTEND: CATEGORY TREE
    ===================================================== */

    /**
     * Lấy danh sách danh mục cha đang hoạt động.
     * Hàm này giữ lại tên cũ để không làm lỗi các servlet/JSP đang gọi findParents().
     */
    public List<Category> findParents() {
        return findActiveTree();
    }

    /**
     * Lấy cây danh mục active để hiển thị ngoài trang sản phẩm.
     * Có cộng tổng số sản phẩm từ danh mục con lên danh mục cha.
     */
    public List<Category> findActiveTree() {

        Map<Integer, Integer> countMap = countActiveProductsByCategory();
        List<Category> categories = new ArrayList<>();

        String sql = """
                SELECT id, name, slug, parent_id, is_active
                FROM store_category
                WHERE is_active = 1
                ORDER BY parent_id IS NOT NULL, parent_id ASC, name ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Category category = mapCategory(resultSet);

                category.setParentId(getNullableInteger(resultSet, "parent_id"));
                category.setProductCount(countMap.getOrDefault(category.getId(), 0));

                categories.add(category);
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findActiveTree error", e);
        }

        return buildCategoryTree(categories, true);
    }

    /**
     * Lấy danh mục active dạng phẳng cho menu đơn giản.
     */
    public List<Category> findActiveForMenu() {

        List<Category> categories = new ArrayList<>();

        String sql = """
                SELECT id, name, slug, parent_id, is_active
                FROM store_category
                WHERE is_active = 1
                ORDER BY parent_id IS NOT NULL, parent_id ASC, name ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Category category = mapCategory(resultSet);

                category.setParentId(getNullableInteger(resultSet, "parent_id"));

                categories.add(category);
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findActiveForMenu error", e);
        }

        return categories;
    }

    private Map<Integer, Integer> countActiveProductsByCategory() {

        Map<Integer, Integer> productCountMap = new HashMap<>();

        String sql = """
                SELECT category_id, COUNT(*) AS cnt
                FROM store_product
                WHERE is_active = 1
                  AND category_id IS NOT NULL
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
            throw new RuntimeException("CategoryDAO.countActiveProductsByCategory error", e);
        }

        return productCountMap;
    }

    /* =====================================================
       ADMIN CATEGORY LIST
    ===================================================== */

    /**
     * Lấy tất cả danh mục dạng phẳng cho admin.
     */
    public List<Category> findAll() {

        Map<Integer, Integer> countMap = countActiveProductsByCategory();
        List<Category> categories = new ArrayList<>();

        String sql = """
                SELECT id, name, slug, parent_id, is_active
                FROM store_category
                ORDER BY parent_id IS NOT NULL, parent_id ASC, name ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Category category = mapCategory(resultSet);

                category.setParentId(getNullableInteger(resultSet, "parent_id"));
                category.setProductCount(countMap.getOrDefault(category.getId(), 0));

                categories.add(category);
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findAll error", e);
        }

        return categories;
    }

    /**
     * Lấy tất cả danh mục dạng cây cho admin.
     * Dùng khi muốn hiển thị danh mục cha/con rõ ràng trong trang quản trị.
     */
    public List<Category> findAllAsTree() {
        return buildCategoryTree(findAll(), true);
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

                category.setParentId(getNullableInteger(resultSet, "parent_id"));
                category.setProductCount(countActiveProductsByCategory().getOrDefault(category.getId(), 0));

                return category;
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findById error", e);
        }
    }

    /**
     * Lấy danh mục cha để đưa vào combobox trong form admin.
     */
    public List<Category> findAllParents() {

        List<Category> parents = new ArrayList<>();

        String sql = """
                SELECT id, name, slug, parent_id, is_active
                FROM store_category
                WHERE parent_id IS NULL
                ORDER BY is_active DESC, name ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Category category = mapCategory(resultSet);

                category.setParentId(getNullableInteger(resultSet, "parent_id"));

                parents.add(category);
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findAllParents error", e);
        }

        return parents;
    }

    /**
     * Lấy danh mục con theo parent_id.
     */
    public List<Category> findChildrenByParentId(int parentId) {

        Map<Integer, Integer> countMap = countActiveProductsByCategory();
        List<Category> children = new ArrayList<>();

        String sql = """
                SELECT id, name, slug, parent_id, is_active
                FROM store_category
                WHERE parent_id = ?
                ORDER BY is_active DESC, name ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, parentId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    Category child = mapCategory(resultSet);

                    child.setParentId(getNullableInteger(resultSet, "parent_id"));
                    child.setProductCount(countMap.getOrDefault(child.getId(), 0));

                    children.add(child);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findChildrenByParentId error", e);
        }

        return children;
    }

    /* =====================================================
       ADMIN CRUD
    ===================================================== */

    public void create(Category category) {

        validateCategoryBeforeSave(category, false);

        String name = cleanText(category.getName());
        String slug = cleanText(category.getSlug());
        Integer parentId = normalizeParentId(category.getParentId());

        if (existsBySlug(slug)) {
            throw new RuntimeException("Slug danh mục đã tồn tại. Vui lòng nhập slug khác.");
        }

        if (parentId != null && !existsById(parentId)) {
            throw new RuntimeException("Danh mục cha không tồn tại.");
        }

        String sql = """
                INSERT INTO store_category
                (name, slug, parent_id, is_active)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, slug);
            setNullableInteger(statement, 3, parentId);
            statement.setBoolean(4, category.isActive());

            statement.executeUpdate();

        } catch (SQLException e) {

            if (e.getErrorCode() == MYSQL_DUPLICATE_ENTRY_ERROR) {
                throw new RuntimeException("Slug danh mục đã tồn tại. Vui lòng nhập slug khác.", e);
            }

            throw new RuntimeException("CategoryDAO.create error", e);
        }
    }

    public void update(Category category) {

        validateCategoryBeforeSave(category, true);

        String name = cleanText(category.getName());
        String slug = cleanText(category.getSlug());
        Integer parentId = normalizeParentId(category.getParentId());

        if (!existsById(category.getId())) {
            throw new RuntimeException("Danh mục cần cập nhật không tồn tại.");
        }

        if (existsBySlugExceptId(slug, category.getId())) {
            throw new RuntimeException("Slug danh mục đã tồn tại. Vui lòng nhập slug khác.");
        }

        if (parentId != null) {

            if (parentId == category.getId()) {
                throw new RuntimeException("Không thể chọn chính danh mục này làm danh mục cha.");
            }

            if (!existsById(parentId)) {
                throw new RuntimeException("Danh mục cha không tồn tại.");
            }

            if (isDescendantOf(parentId, category.getId())) {
                throw new RuntimeException("Không thể chọn danh mục con làm danh mục cha.");
            }
        }

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

            statement.setString(1, name);
            statement.setString(2, slug);
            setNullableInteger(statement, 3, parentId);
            statement.setBoolean(4, category.isActive());
            statement.setInt(5, category.getId());

            statement.executeUpdate();

        } catch (SQLException e) {

            if (e.getErrorCode() == MYSQL_DUPLICATE_ENTRY_ERROR) {
                throw new RuntimeException("Slug danh mục đã tồn tại. Vui lòng nhập slug khác.", e);
            }

            throw new RuntimeException("CategoryDAO.update error", e);
        }
    }

    public void delete(int id) {

        if (id <= 0) {
            throw new RuntimeException("ID danh mục không hợp lệ.");
        }

        if (!existsById(id)) {
            throw new RuntimeException("Danh mục cần xóa không tồn tại.");
        }

        if (hasChildren(id)) {
            throw new RuntimeException("Không thể xóa danh mục vì đang có danh mục con.");
        }

        if (hasProducts(id)) {
            throw new RuntimeException("Không thể xóa danh mục vì đang có sản phẩm thuộc danh mục này.");
        }

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
                        "Không thể xóa danh mục vì đang có sản phẩm, khuyến mãi hoặc dữ liệu khác liên kết.",
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
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanText(slug));

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
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanText(slug));
            statement.setInt(2, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.existsBySlugExceptId error", e);
        }
    }

    public boolean existsById(int id) {

        String sql = """
                SELECT 1
                FROM store_category
                WHERE id = ?
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.existsById error", e);
        }
    }

    /* =====================================================
       DELETE SAFETY CHECK
    ===================================================== */

    public boolean hasChildren(int categoryId) {

        String sql = """
                SELECT 1
                FROM store_category
                WHERE parent_id = ?
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.hasChildren error", e);
        }
    }

    public boolean hasProducts(int categoryId) {

        String sql = """
                SELECT 1
                FROM store_product
                WHERE category_id = ?
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.hasProducts error", e);
        }
    }

    /* =====================================================
       TREE HELPER
    ===================================================== */

    private List<Category> buildCategoryTree(
            List<Category> categories,
            boolean aggregateProductCount
    ) {

        Map<Integer, Category> categoryMap = new LinkedHashMap<>();
        List<Category> roots = new ArrayList<>();

        for (Category category : categories) {
            category.setChildren(new ArrayList<>());
            categoryMap.put(category.getId(), category);
        }

        for (Category category : categories) {

            Integer parentId = category.getParentId();

            if (parentId != null
                    && parentId > 0
                    && parentId != category.getId()
                    && categoryMap.containsKey(parentId)) {

                categoryMap.get(parentId).getChildren().add(category);

            } else {
                roots.add(category);
            }
        }

        if (aggregateProductCount) {
            for (Category root : roots) {
                calculateTotalProductCount(root);
            }
        }

        return roots;
    }

    private int calculateTotalProductCount(Category category) {

        int total = category.getProductCount();

        for (Category child : category.getChildren()) {
            total += calculateTotalProductCount(child);
        }

        category.setProductCount(total);

        return total;
    }

    /**
     * Kiểm tra selectedParentId có phải là con/cháu của categoryId hay không.
     * Nếu có thì không được set làm cha vì sẽ tạo vòng lặp.
     */
    private boolean isDescendantOf(int selectedParentId, int categoryId) {

        Integer currentId = selectedParentId;
        int safetyCounter = 0;

        while (currentId != null && safetyCounter < 100) {

            if (currentId == categoryId) {
                return true;
            }

            currentId = findParentIdById(currentId);
            safetyCounter++;
        }

        return false;
    }

    private Integer findParentIdById(int id) {

        String sql = """
                SELECT parent_id
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

                return getNullableInteger(resultSet, "parent_id");
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findParentIdById error", e);
        }
    }

    /* =====================================================
       VALIDATION / UTIL
    ===================================================== */

    private void validateCategoryBeforeSave(Category category, boolean updating) {

        if (category == null) {
            throw new RuntimeException("Dữ liệu danh mục không hợp lệ.");
        }

        if (updating && category.getId() <= 0) {
            throw new RuntimeException("ID danh mục không hợp lệ.");
        }

        if (isBlank(category.getName())) {
            throw new RuntimeException("Tên danh mục không được để trống.");
        }

        if (isBlank(category.getSlug())) {
            throw new RuntimeException("Slug danh mục không được để trống.");
        }
    }

    private String cleanText(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Integer normalizeParentId(Integer parentId) {

        if (parentId == null || parentId <= 0) {
            return null;
        }

        return parentId;
    }

    private Integer getNullableInteger(ResultSet resultSet, String columnName)
            throws SQLException {

        int value = resultSet.getInt(columnName);

        return resultSet.wasNull() ? null : value;
    }

    private void setNullableInteger(
            PreparedStatement statement,
            int parameterIndex,
            Integer value
    ) throws SQLException {

        if (value == null) {
            statement.setNull(parameterIndex, Types.BIGINT);
        } else {
            statement.setInt(parameterIndex, value);
        }
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
        category.setActive(resultSet.getBoolean("is_active"));

        return category;
    }
}
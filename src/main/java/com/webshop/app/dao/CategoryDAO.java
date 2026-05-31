package com.webshop.app.dao;

import com.webshop.app.model.Category;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CategoryDAO {

    private static final int MYSQL_FOREIGN_KEY_CONSTRAINT_ERROR = 1451;
    private static final int MYSQL_DUPLICATE_ENTRY_ERROR = 1062;

    private final CategoryTagDAO categoryTagDAO = new CategoryTagDAO();

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
        Map<Integer, Integer> tagCountMap = categoryTagDAO.countActiveByCategory();

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
                category.setTagCount(tagCountMap.getOrDefault(category.getId(), 0));

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

        Map<Integer, Integer> tagCountMap = categoryTagDAO.countActiveByCategory();
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
                category.setTagCount(tagCountMap.getOrDefault(category.getId(), 0));

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


    /**
     * Lấy danh mục hot cho trang chủ.
     * Luôn hiển thị đủ 13 mục theo mẫu, dùng ảnh tĩnh trong assets/images/categories
     * để không bị mất hình khi danh mục chưa có sản phẩm hoặc chưa gán image_url trong database.
     */
    public List<Category> findHotCategoriesForHome(int limit) {
        int safeLimit = limit <= 0 ? 13 : Math.min(limit, 13);
        List<Category> rawCategories = new ArrayList<>();

        String sql = """
                SELECT
                    c.id,
                    c.name,
                    c.slug,
                    c.parent_id,
                    c.is_active,
                    COUNT(p.id) AS product_count,
                    (
                        SELECT COALESCE(NULLIF(TRIM(p2.image), ''), first_img.image)
                        FROM store_product p2
                        LEFT JOIN (
                            SELECT spi.product_id, spi.image
                            FROM store_productimage spi
                            INNER JOIN (
                                SELECT product_id, MIN(id) AS min_id
                                FROM store_productimage
                                GROUP BY product_id
                            ) x ON x.product_id = spi.product_id AND x.min_id = spi.id
                        ) first_img ON first_img.product_id = p2.id
                        WHERE p2.category_id = c.id
                          AND p2.is_active = 1
                        ORDER BY p2.discount_percent DESC, p2.stock DESC, p2.id DESC
                        LIMIT 1
                    ) AS hot_image_url
                FROM store_category c
                LEFT JOIN store_product p ON p.category_id = c.id AND p.is_active = 1
                WHERE c.is_active = 1
                  AND c.parent_id IS NOT NULL
                GROUP BY c.id, c.name, c.slug, c.parent_id, c.is_active
                ORDER BY c.name ASC, c.id ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Category category = mapCategory(resultSet);
                category.setParentId(getNullableInteger(resultSet, "parent_id"));
                category.setProductCount(resultSet.getInt("product_count"));
                category.setHotImageUrl(resultSet.getString("hot_image_url"));
                rawCategories.add(category);
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findHotCategoriesForHome error", e);
        }

        List<Category> sourceCategories = new ArrayList<>();
        for (Category category : rawCategories) {
            if (!isExcludedHotCategory(category.getName()) && !isExcludedHotCategory(category.getSlug())) {
                sourceCategories.add(category);
            }
        }

        List<HotCategoryTarget> targets = buildHotCategoryTargets();
        List<Category> result = new ArrayList<>();
        Set<Integer> usedIds = new LinkedHashSet<>();

        for (HotCategoryTarget target : targets) {
            Category matched = findBestHotCategoryMatch(sourceCategories, usedIds, target);

            if (matched != null) {
                matched.setName(target.displayName);
                matched.setHighlightLabel(target.badgeLabel);

                // Ưu tiên ảnh danh mục cố định để layout giống mẫu và không bị mất hình.
                matched.setHotImageUrl(target.imageUrl);

                result.add(matched);
                usedIds.add(matched.getId());
            } else {
                result.add(createFallbackHotCategory(target));
            }

            if (result.size() >= safeLimit) {
                break;
            }
        }

        return result;
    }

    private List<HotCategoryTarget> buildHotCategoryTargets() {
        List<HotCategoryTarget> targets = new ArrayList<>();
        targets.add(new HotCategoryTarget("Son Môi", "LIP", "/assets/images/categories/son-moi.png", "son-moi", "son moi", "son duong", "duong moi", "lip"));
        targets.add(new HotCategoryTarget("Má Hồng", "MAKE", "/assets/images/categories/ma-hong.png", "ma-hong", "ma hong", "blush"));
        targets.add(new HotCategoryTarget("Phấn Mắt", "MAKE", "/assets/images/categories/phan-mat.png", "phan-mat", "phan mat", "mat makeup", "eye", "eyeshadow"));
        targets.add(new HotCategoryTarget("Phấn Nước", "MAKE", "/assets/images/categories/phan-nuoc.png", "phan-nuoc", "phan nuoc", "cushion"));
        targets.add(new HotCategoryTarget("Xịt Khóa Nền", "MAKE", "/assets/images/categories/xit-khoa-nen.png", "xit-khoa-nen", "xit khoa nen", "khoa nen", "setting spray"));
        targets.add(new HotCategoryTarget("Phụ Kiện", "ACC", "/assets/images/categories/phu-kien.png", "phu-kien", "phu kien", "accessories"));
        targets.add(new HotCategoryTarget("Tẩy Trang", "CLEAN", "/assets/images/categories/tay-trang.png", "tay-trang", "tay trang", "makeup remover", "micellar"));
        targets.add(new HotCategoryTarget("Kem Chống Nắng", "SPF", "/assets/images/categories/kem-chong-nang.png", "kem-chong-nang", "chong-nang", "kem chong nang", "sunscreen", "sun care"));
        targets.add(new HotCategoryTarget("Sữa Rửa Mặt", "FOAM", "/assets/images/categories/sua-rua-mat.png", "sua-rua-mat", "sua rua mat", "cleanser", "face wash"));
        targets.add(new HotCategoryTarget("Nước Hoa Hồng", "SKIN", "/assets/images/categories/nuoc-hoa-hong.png", "nuoc-hoa-hong", "toner-lotion", "nuoc hoa hong", "toner", "lotion"));
        targets.add(new HotCategoryTarget("Tinh Chất Dưỡng", "CARE", "/assets/images/categories/tinh-chat-duong.png", "tinh-chat-duong", "serum-essence", "tinh chat duong", "serum", "essence"));
        targets.add(new HotCategoryTarget("Kem Dưỡng", "SKIN", "/assets/images/categories/kem-duong.png", "kem-duong", "gel-duong", "kem duong", "duong am", "moisturizer", "cream"));
        targets.add(new HotCategoryTarget("Mặt Nạ", "MASK", "/assets/images/categories/mat-na.png", "mat-na", "mat na", "mask"));
        return targets;
    }

    private Category findBestHotCategoryMatch(List<Category> categories, Set<Integer> usedIds, HotCategoryTarget target) {
        Category bestCategory = null;
        int bestScore = Integer.MIN_VALUE;

        for (Category category : categories) {
            if (usedIds.contains(category.getId())) {
                continue;
            }

            int score = scoreHotCategory(category, target);
            if (score > bestScore) {
                bestScore = score;
                bestCategory = category;
            }
        }

        return bestScore > 0 ? bestCategory : null;
    }

    private int scoreHotCategory(Category category, HotCategoryTarget target) {
        String normalizedName = normalizeCategoryText(category.getName());
        String normalizedSlug = normalizeCategoryText(category.getSlug());

        int score = 0;

        for (int i = 0; i < target.keywords.length; i++) {
            String keyword = target.keywords[i];
            int weight = Math.max(40 - (i * 3), 10);

            if (normalizedSlug.equals(keyword)) {
                score += weight + 80;
            } else if (normalizedSlug.contains(keyword)) {
                score += weight + 55;
            }

            if (normalizedName.equals(keyword)) {
                score += weight + 60;
            } else if (normalizedName.contains(keyword)) {
                score += weight + 35;
            }
        }

        if (category.getProductCount() > 0) {
            score += 8;
        }
        if (category.getHotImageUrl() != null && !category.getHotImageUrl().isBlank()) {
            score += 12;
        }

        return score;
    }

    private Category createFallbackHotCategory(HotCategoryTarget target) {
        Category category = new Category();
        category.setId(0);
        category.setName(target.displayName);
        category.setSlug(buildSlug(target.displayName));
        category.setActive(true);
        category.setProductCount(0);
        category.setHotImageUrl(target.imageUrl);
        category.setHighlightLabel(target.badgeLabel);
        return category;
    }

    private boolean isExcludedHotCategory(String value) {
        String normalized = normalizeCategoryText(value);
        return normalized.contains("blind box")
                || normalized.contains("hop mu")
                || normalized.contains("hop blind")
                || normalized.contains("mystery box");
    }

    private String normalizeCategoryText(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase()
                .trim();

        normalized = normalized.replace('đ', 'd');
        normalized = normalized.replaceAll("[^a-z0-9\\s-]", " ");
        normalized = normalized.replaceAll("\\s+", " ");

        return normalized;
    }

    private String buildSlug(String value) {
        String normalized = normalizeCategoryText(value);
        return normalized.replace(' ', '-');
    }

    private static class HotCategoryTarget {
        private final String displayName;
        private final String badgeLabel;
        private final String imageUrl;
        private final String[] keywords;

        private HotCategoryTarget(String displayName, String badgeLabel, String imageUrl, String... keywords) {
            this.displayName = displayName;
            this.badgeLabel = badgeLabel;
            this.imageUrl = imageUrl;
            this.keywords = keywords;
        }
    }

    /* =====================================================
       ADMIN CATEGORY LIST
    ===================================================== */

    /**
     * Lấy tất cả danh mục dạng phẳng cho admin.
     */
    public List<Category> findAll() {

        Map<Integer, Integer> countMap = countActiveProductsByCategory();
        Map<Integer, Integer> tagCountMap = categoryTagDAO.countActiveByCategory();

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
                category.setTagCount(tagCountMap.getOrDefault(category.getId(), 0));

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
                category.setTags(categoryTagDAO.findAllByCategoryId(category.getId()));

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

        Map<Integer, Integer> tagCountMap = categoryTagDAO.countActiveByCategory();
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
                category.setTagCount(tagCountMap.getOrDefault(category.getId(), 0));

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
        Map<Integer, Integer> tagCountMap = categoryTagDAO.countActiveByCategory();

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
                    child.setTagCount(tagCountMap.getOrDefault(child.getId(), 0));

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
        createAndReturnId(category);
    }

    /**
     * Tạo danh mục và trả về ID.
     * Dùng cho AdminCategoryServlet khi cần tạo danh mục xong rồi lưu tag.
     */
    public int createAndReturnId(Category category) {

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
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, name);
            statement.setString(2, slug);
            setNullableInteger(statement, 3, parentId);
            statement.setBoolean(4, category.isActive());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    category.setId(generatedId);
                    return generatedId;
                }
            }

            return 0;

        } catch (SQLException e) {

            if (e.getErrorCode() == MYSQL_DUPLICATE_ENTRY_ERROR) {
                throw new RuntimeException("Slug danh mục đã tồn tại. Vui lòng nhập slug khác.", e);
            }

            throw new RuntimeException("CategoryDAO.createAndReturnId error", e);
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
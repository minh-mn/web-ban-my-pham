package com.webshop.app.dao;

import com.webshop.app.model.Brand;
import com.webshop.app.model.Category;
import com.webshop.app.model.Product;
import com.webshop.app.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO phục vụ riêng cho trang chủ.
 *
 * Mục tiêu:
 * - Issue 27: hiển thị nhiều nhóm sản phẩm trên trang chủ.
 * - Issue 28: hiển thị danh mục hot dựa trên số sản phẩm active.
 *
 * Tách DAO này ra riêng để không phá luồng ProductDAO/CategoryDAO hiện tại.
 */
public class HomeSectionDAO {

    private static final int DEFAULT_LIMIT = 8;

    public List<Product> findFeaturedProducts(int limit) {
        String sql = productBaseSelect() +
                "WHERE p.is_active = 1 " +
                "ORDER BY COALESCE(rv.avg_rating, 0) DESC, " +
                "COALESCE(rv.review_count, 0) DESC, " +
                "COALESCE(sd.sold_qty, 0) DESC, " +
                "p.created_at DESC " +
                "LIMIT ?";

        return queryProducts(sql, limit, "HomeSectionDAO.findFeaturedProducts error");
    }

    public List<Product> findBestSellingProducts(int limit) {
        String sql = productBaseSelect() +
                "WHERE p.is_active = 1 " +
                "ORDER BY COALESCE(sd.sold_qty, 0) DESC, " +
                "COALESCE(rv.avg_rating, 0) DESC, " +
                "p.created_at DESC " +
                "LIMIT ?";

        return queryProducts(sql, limit, "HomeSectionDAO.findBestSellingProducts error");
    }

    public List<Product> findDeepDiscountProducts(int limit) {
        String sql = productBaseSelect() +
                "WHERE p.is_active = 1 " +
                "AND p.discount_percent > 0 " +
                "ORDER BY p.discount_percent DESC, " +
                "COALESCE(sd.sold_qty, 0) DESC, " +
                "p.created_at DESC " +
                "LIMIT ?";

        return queryProducts(sql, limit, "HomeSectionDAO.findDeepDiscountProducts error");
    }

    public List<Product> findMostViewedProducts(int limit) {
        String sql = productBaseSelect() +
                "WHERE p.is_active = 1 " +
                "ORDER BY COALESCE(p.view_count, 0) DESC, " +
                "COALESCE(sd.sold_qty, 0) DESC, " +
                "p.created_at DESC " +
                "LIMIT ?";

        return queryProducts(sql, limit, "HomeSectionDAO.findMostViewedProducts error");
    }

    public List<Product> findNewProducts(int limit) {
        String sql = productBaseSelect() +
                "WHERE p.is_active = 1 " +
                "ORDER BY p.created_at DESC, p.id DESC " +
                "LIMIT ?";

        return queryProducts(sql, limit, "HomeSectionDAO.findNewProducts error");
    }

    public List<Category> findHotCategories(int limit) {
        int safeLimit = normalizeLimit(limit);
        List<Category> categories = new ArrayList<>();

        String sql = """
                SELECT
                    c.id,
                    c.name,
                    c.slug,
                    c.parent_id,
                    c.is_active,
                    COUNT(p.id) AS product_count,
                    MAX(CASE
                            WHEN p.image IS NOT NULL AND TRIM(p.image) <> ''
                            THEN p.image
                            ELSE NULL
                        END) AS hot_image
                FROM store_category c
                LEFT JOIN store_product p
                       ON p.category_id = c.id
                      AND p.is_active = 1
                WHERE c.is_active = 1
                GROUP BY c.id, c.name, c.slug, c.parent_id, c.is_active
                HAVING product_count > 0
                ORDER BY product_count DESC, c.name ASC
                LIMIT ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, safeLimit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Category category = new Category();
                    category.setId(resultSet.getInt("id"));
                    category.setName(resultSet.getString("name"));
                    category.setSlug(resultSet.getString("slug"));
                    category.setActive(resultSet.getBoolean("is_active"));

                    Object parentId = resultSet.getObject("parent_id");
                    if (parentId != null) {
                        category.setParentId(resultSet.getInt("parent_id"));
                    }

                    category.setProductCount(resultSet.getInt("product_count"));
                    category.setHotImageUrl(resultSet.getString("hot_image"));
                    category.setHighlightLabel("HOT");
                    categories.add(category);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("HomeSectionDAO.findHotCategories error", e);
        }

        return categories;
    }

    public void increaseViewCount(int productId) {
        if (productId <= 0) {
            return;
        }

        String sql = """
                UPDATE store_product
                SET view_count = COALESCE(view_count, 0) + 1
                WHERE id = ?
                  AND is_active = 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("HomeSectionDAO.increaseViewCount error", e);
        }
    }

    public int findViewCountByProductId(int productId) {
        if (productId <= 0) {
            return 0;
        }

        String sql = "SELECT COALESCE(view_count, 0) AS view_count FROM store_product WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0;
                }

                return resultSet.getInt("view_count");
            }

        } catch (SQLException e) {
            throw new RuntimeException("HomeSectionDAO.findViewCountByProductId error", e);
        }
    }

    private List<Product> queryProducts(String sql, int limit, String errorMessage) {
        int safeLimit = normalizeLimit(limit);
        List<Product> products = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, safeLimit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(mapProduct(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(errorMessage, e);
        }

        return products;
    }

    private String productBaseSelect() {
        return "SELECT " +
                "p.id, p.title, p.slug, p.description, " +
                "p.price, p.discount_percent, p.stock, p.image, p.is_active, p.created_at, " +
                "COALESCE(p.view_count, 0) AS view_count, " +
                "COALESCE(rv.avg_rating, 0) AS avg_rating, " +
                "COALESCE(rv.review_count, 0) AS review_count, " +
                "COALESCE(sd.sold_qty, 0) AS sold_qty, " +
                "c.id AS c_id, c.name AS c_name, c.slug AS c_slug, " +
                "b.id AS b_id, b.name AS b_name " +
                "FROM store_product p " +
                "LEFT JOIN store_category c ON p.category_id = c.id " +
                "LEFT JOIN store_brand b ON p.brand_id = b.id " +
                "LEFT JOIN ( " +
                "    SELECT product_id, AVG(rating) AS avg_rating, COUNT(*) AS review_count " +
                "    FROM store_review " +
                "    WHERE (status IS NULL OR status = 'APPROVED') " +
                "      AND (is_hidden IS NULL OR is_hidden = 0) " +
                "    GROUP BY product_id " +
                ") rv ON rv.product_id = p.id " +
                "LEFT JOIN ( " +
                "    SELECT oi.product_id, SUM(oi.quantity) AS sold_qty " +
                "    FROM store_orderitem oi " +
                "    JOIN store_order o ON o.id = oi.order_id " +
                "    WHERE oi.product_id IS NOT NULL " +
                "      AND (o.payment_status = 'PAID' OR LOWER(o.status) = 'completed') " +
                "    GROUP BY oi.product_id " +
                ") sd ON sd.product_id = p.id ";
    }

    private Product mapProduct(ResultSet resultSet) throws SQLException {
        Product product = new Product();

        product.setId(resultSet.getInt("id"));
        product.setTitle(resultSet.getString("title"));
        product.setSlug(resultSet.getString("slug"));
        product.setDescription(resultSet.getString("description"));
        product.setPrice(resultSet.getBigDecimal("price"));
        product.setDiscountPercent(resultSet.getInt("discount_percent"));
        product.setFinalPrice(calculateFinalPrice(product.getPrice(), product.getDiscountPercent()));
        product.setStock(resultSet.getInt("stock"));
        product.setImage(resultSet.getString("image"));
        product.setActive(resultSet.getBoolean("is_active"));
        product.setAvgRating(resultSet.getDouble("avg_rating"));
        product.setReviewCount(resultSet.getInt("review_count"));
        product.setSoldQuantity(resultSet.getInt("sold_qty"));
        product.setViewCount(resultSet.getInt("view_count"));

        if (resultSet.getObject("c_id") != null) {
            Category category = new Category();
            category.setId(resultSet.getInt("c_id"));
            category.setName(resultSet.getString("c_name"));
            category.setSlug(resultSet.getString("c_slug"));

            product.setCategory(category);
            product.setCategoryId(resultSet.getInt("c_id"));
            product.setCategoryName(resultSet.getString("c_name"));
        }

        if (resultSet.getObject("b_id") != null) {
            Brand brand = new Brand();
            brand.setId(resultSet.getInt("b_id"));
            brand.setName(resultSet.getString("b_name"));

            product.setBrand(brand);
            product.setBrandId(resultSet.getInt("b_id"));
            product.setBrandName(resultSet.getString("b_name"));
        }

        return product;
    }

    private BigDecimal calculateFinalPrice(BigDecimal price, int discountPercent) {
        if (price == null) {
            return BigDecimal.ZERO;
        }

        if (discountPercent <= 0) {
            return price;
        }

        BigDecimal rate = BigDecimal.valueOf(100L - discountPercent);
        return price.multiply(rate).divide(BigDecimal.valueOf(100));
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, 24);
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
}

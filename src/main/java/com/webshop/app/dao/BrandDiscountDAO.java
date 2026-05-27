package com.webshop.app.dao;

import com.webshop.app.model.BrandDiscount;
import com.webshop.app.model.DiscountType;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BrandDiscountDAO {

    private static final String SCOPE_ALL_BRAND_PRODUCTS = "ALL_BRAND_PRODUCTS";
    private static final String SCOPE_SELECTED_PRODUCTS = "SELECTED_PRODUCTS";

    private static final String BRAND_DISCOUNT_SELECT_COLUMNS = """
            d.id AS id,
            d.brand_id AS brand_id,
            d.apply_scope AS apply_scope,
            d.discount_type AS discount_type,
            d.discount_value AS discount_value,
            d.max_discount_amount AS max_discount_amount,
            d.start_date AS start_date,
            d.end_date AS end_date,
            d.is_active AS is_active,
            b.name AS brand_name
            """;

    /* =========================================================
       FRONTEND / PRICING
    ========================================================= */

    /**
     * Lấy BrandDiscount hợp lệ có discount_value cao nhất theo brand.
     *
     * Lưu ý:
     * Method cũ này chỉ lấy giảm giá áp dụng toàn bộ sản phẩm của thương hiệu.
     * Không lấy SELECTED_PRODUCTS để tránh áp sai giảm giá cho toàn bộ brand.
     */
    public BrandDiscount findBestActiveByBrandId(int brandId) {
        if (brandId <= 0) {
            return null;
        }

        String sql = """
                SELECT
                """ + BRAND_DISCOUNT_SELECT_COLUMNS + """
                FROM store_branddiscount d
                LEFT JOIN store_brand b ON b.id = d.brand_id
                WHERE d.brand_id = ?
                  AND d.is_active = 1
                  AND (d.start_date IS NULL OR d.start_date <= CURDATE())
                  AND (d.end_date IS NULL OR d.end_date >= CURDATE())
                  AND (
                        d.apply_scope IS NULL
                        OR d.apply_scope = ''
                        OR d.apply_scope = 'ALL_BRAND_PRODUCTS'
                  )
                ORDER BY d.discount_value DESC, d.id DESC
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, brandId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                BrandDiscount discount = mapRow(resultSet);
                attachSelectedProductIds(discount);
                return discount;
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.findBestActiveByBrandId error", e);
        }
    }

    /**
     * Lấy BrandDiscount tốt nhất cho một sản phẩm cụ thể.
     *
     * Điều kiện hợp lệ:
     * - Cùng brand.
     * - Đang active.
     * - Trong thời gian hiệu lực.
     * - apply_scope = ALL_BRAND_PRODUCTS
     *   hoặc apply_scope = SELECTED_PRODUCTS và product_id nằm trong store_branddiscount_product.
     */
    public BrandDiscount findBestActiveByBrandIdAndProductId(int brandId, int productId) {
        if (brandId <= 0 || productId <= 0) {
            return null;
        }

        String sql = """
                SELECT
                """ + BRAND_DISCOUNT_SELECT_COLUMNS + """
                FROM store_branddiscount d
                LEFT JOIN store_brand b ON b.id = d.brand_id
                WHERE d.brand_id = ?
                  AND d.is_active = 1
                  AND (d.start_date IS NULL OR d.start_date <= CURDATE())
                  AND (d.end_date IS NULL OR d.end_date >= CURDATE())
                  AND (
                        d.apply_scope IS NULL
                        OR d.apply_scope = ''
                        OR d.apply_scope = 'ALL_BRAND_PRODUCTS'
                        OR (
                            d.apply_scope = 'SELECTED_PRODUCTS'
                            AND EXISTS (
                                SELECT 1
                                FROM store_branddiscount_product bp
                                WHERE bp.brand_discount_id = d.id
                                  AND bp.product_id = ?
                            )
                        )
                  )
                ORDER BY d.discount_value DESC, d.id DESC
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, brandId);
            statement.setInt(2, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                BrandDiscount discount = mapRow(resultSet);
                attachSelectedProductIds(discount);
                return discount;
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.findBestActiveByBrandIdAndProductId error", e);
        }
    }

    public boolean isValid(BrandDiscount discount) {
        if (discount == null || !discount.isActive()) {
            return false;
        }

        LocalDate today = LocalDate.now();

        if (discount.getStartDate() != null && today.isBefore(discount.getStartDate())) {
            return false;
        }

        if (discount.getEndDate() != null && today.isAfter(discount.getEndDate())) {
            return false;
        }

        return true;
    }

    public boolean isApplicableToProduct(BrandDiscount discount, int productId, int productBrandId) {
        if (!isValid(discount) || productId <= 0 || productBrandId <= 0) {
            return false;
        }

        if (discount.getBrandId() != productBrandId) {
            return false;
        }

        String applyScope = normalizeApplyScope(discount.getApplyScope());

        if (SCOPE_ALL_BRAND_PRODUCTS.equals(applyScope)) {
            return true;
        }

        if (SCOPE_SELECTED_PRODUCTS.equals(applyScope)) {
            List<Integer> selectedProductIds = discount.getSelectedProductIds();

            if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
                return selectedProductIds.contains(productId);
            }

            return existsProductTarget(discount.getId(), productId);
        }

        return false;
    }

    public boolean existsProductTarget(int brandDiscountId, int productId) {
        if (brandDiscountId <= 0 || productId <= 0) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM store_branddiscount_product
                WHERE brand_discount_id = ?
                  AND product_id = ?
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, brandDiscountId);
            statement.setInt(2, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.existsProductTarget error", e);
        }
    }

    /* =========================================================
       ADMIN CRUD
    ========================================================= */

    public List<BrandDiscount> findAll(boolean joinBrand) {
        List<BrandDiscount> discounts = new ArrayList<>();

        String sql = joinBrand
                ? """
                SELECT
                """ + BRAND_DISCOUNT_SELECT_COLUMNS + """
                FROM store_branddiscount d
                LEFT JOIN store_brand b ON b.id = d.brand_id
                ORDER BY d.id DESC
                """
                : """
                SELECT
                    d.id AS id,
                    d.brand_id AS brand_id,
                    d.apply_scope AS apply_scope,
                    d.discount_type AS discount_type,
                    d.discount_value AS discount_value,
                    d.max_discount_amount AS max_discount_amount,
                    d.start_date AS start_date,
                    d.end_date AS end_date,
                    d.is_active AS is_active,
                    NULL AS brand_name
                FROM store_branddiscount d
                ORDER BY d.id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                discounts.add(mapRow(resultSet));
            }

            attachSelectedProductIds(discounts);

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.findAll error", e);
        }

        return discounts;
    }

    public BrandDiscount findById(int id) {
        if (id <= 0) {
            return null;
        }

        String sql = """
                SELECT
                """ + BRAND_DISCOUNT_SELECT_COLUMNS + """
                FROM store_branddiscount d
                LEFT JOIN store_brand b ON b.id = d.brand_id
                WHERE d.id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                BrandDiscount discount = mapRow(resultSet);
                attachSelectedProductIds(discount);
                return discount;
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.findById error", e);
        }
    }

    public void create(BrandDiscount discount) {
        String sql = """
                INSERT INTO store_branddiscount
                (
                    brand_id,
                    apply_scope,
                    discount_type,
                    discount_value,
                    max_discount_amount,
                    start_date,
                    end_date,
                    is_active
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bindForInsert(statement, discount);
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        discount.setId(generatedKeys.getInt(1));
                    }
                }
            }

            replaceSelectedProducts(
                    connection,
                    discount.getId(),
                    discount.getSelectedProductIds(),
                    discount.getApplyScope()
            );

            connection.commit();

        } catch (SQLException e) {
            rollbackQuietly(connection);
            throw new RuntimeException("BrandDiscountDAO.create error", e);

        } finally {
            closeQuietly(connection);
        }
    }

    public void update(BrandDiscount discount) {
        String sql = """
                UPDATE store_branddiscount
                SET brand_id = ?,
                    apply_scope = ?,
                    discount_type = ?,
                    discount_value = ?,
                    max_discount_amount = ?,
                    start_date = ?,
                    end_date = ?,
                    is_active = ?
                WHERE id = ?
                """;

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bindForUpdate(statement, discount);
                statement.executeUpdate();
            }

            replaceSelectedProducts(
                    connection,
                    discount.getId(),
                    discount.getSelectedProductIds(),
                    discount.getApplyScope()
            );

            connection.commit();

        } catch (SQLException e) {
            rollbackQuietly(connection);
            throw new RuntimeException("BrandDiscountDAO.update error", e);

        } finally {
            closeQuietly(connection);
        }
    }

    public void delete(int id) {
        if (id <= 0) {
            return;
        }

        String sql = """
                DELETE FROM store_branddiscount
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.delete error", e);
        }
    }

    public void toggleActive(int id) {
        if (id <= 0) {
            return;
        }

        String sql = """
                UPDATE store_branddiscount
                SET is_active = CASE
                    WHEN is_active = 1 THEN 0
                    ELSE 1
                END
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.toggleActive error", e);
        }
    }

    /* =========================================================
       PRODUCT TARGETING
    ========================================================= */

    public List<Integer> findSelectedProductIds(int brandDiscountId) {
        List<Integer> productIds = new ArrayList<>();

        if (brandDiscountId <= 0) {
            return productIds;
        }

        String sql = """
                SELECT product_id
                FROM store_branddiscount_product
                WHERE brand_discount_id = ?
                ORDER BY product_id ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, brandDiscountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    productIds.add(resultSet.getInt("product_id"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.findSelectedProductIds error", e);
        }

        return productIds;
    }

    public void replaceSelectedProducts(
            int brandDiscountId,
            List<Integer> productIds,
            String applyScope
    ) {
        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            replaceSelectedProducts(connection, brandDiscountId, productIds, applyScope);

            connection.commit();

        } catch (SQLException e) {
            rollbackQuietly(connection);
            throw new RuntimeException("BrandDiscountDAO.replaceSelectedProducts error", e);

        } finally {
            closeQuietly(connection);
        }
    }

    private void replaceSelectedProducts(
            Connection connection,
            int brandDiscountId,
            List<Integer> productIds,
            String applyScope
    ) throws SQLException {

        if (brandDiscountId <= 0) {
            return;
        }

        deleteSelectedProducts(connection, brandDiscountId);

        if (!SCOPE_SELECTED_PRODUCTS.equals(normalizeApplyScope(applyScope))) {
            return;
        }

        List<Integer> cleanedProductIds = normalizeProductIds(productIds);
        if (cleanedProductIds.isEmpty()) {
            return;
        }

        String sql = """
                INSERT IGNORE INTO store_branddiscount_product
                (
                    brand_discount_id,
                    product_id,
                    created_at
                )
                VALUES (?, ?, NOW())
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Integer productId : cleanedProductIds) {
                statement.setInt(1, brandDiscountId);
                statement.setInt(2, productId);
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void deleteSelectedProducts(Connection connection, int brandDiscountId)
            throws SQLException {

        String sql = """
                DELETE FROM store_branddiscount_product
                WHERE brand_discount_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, brandDiscountId);
            statement.executeUpdate();
        }
    }

    /* =========================================================
       MAPPING
    ========================================================= */

    private BrandDiscount mapRow(ResultSet resultSet) throws SQLException {
        BrandDiscount discount = new BrandDiscount();

        discount.setId(resultSet.getInt("id"));
        discount.setBrandId(resultSet.getInt("brand_id"));
        discount.setApplyScope(resultSet.getString("apply_scope"));

        String discountType = resultSet.getString("discount_type");
        if (discountType != null && !discountType.isBlank()) {
            discount.setDiscountType(normalizeDiscountType(discountType));
        }

        discount.setDiscountValue(resultSet.getBigDecimal("discount_value"));
        discount.setMaxDiscountAmount(resultSet.getBigDecimal("max_discount_amount"));

        Date startDate = resultSet.getDate("start_date");
        if (startDate != null) {
            discount.setStartDate(startDate.toLocalDate());
        }

        Date endDate = resultSet.getDate("end_date");
        if (endDate != null) {
            discount.setEndDate(endDate.toLocalDate());
        }

        discount.setActive(resultSet.getBoolean("is_active"));
        discount.setBrandName(resultSet.getString("brand_name"));

        return discount;
    }

    private void attachSelectedProductIds(BrandDiscount discount) {
        if (discount == null || discount.getId() <= 0) {
            return;
        }

        discount.setSelectedProductIds(findSelectedProductIds(discount.getId()));
    }

    private void attachSelectedProductIds(List<BrandDiscount> discounts) {
        if (discounts == null || discounts.isEmpty()) {
            return;
        }

        for (BrandDiscount discount : discounts) {
            attachSelectedProductIds(discount);
        }
    }

    /* =========================================================
       BIND
    ========================================================= */

    private void bindForInsert(PreparedStatement statement, BrandDiscount discount)
            throws SQLException {

        statement.setInt(1, discount.getBrandId());
        statement.setString(2, normalizeApplyScope(discount.getApplyScope()));
        statement.setString(3, normalizeDiscountType(discount.getDiscountType()));
        statement.setBigDecimal(4, discount.getDiscountValue());
        setNullableBigDecimal(statement, 5, discount.getMaxDiscountAmount());
        setNullableDate(statement, 6, discount.getStartDate());
        setNullableDate(statement, 7, discount.getEndDate());
        statement.setBoolean(8, discount.isActive());
    }

    private void bindForUpdate(PreparedStatement statement, BrandDiscount discount)
            throws SQLException {

        statement.setInt(1, discount.getBrandId());
        statement.setString(2, normalizeApplyScope(discount.getApplyScope()));
        statement.setString(3, normalizeDiscountType(discount.getDiscountType()));
        statement.setBigDecimal(4, discount.getDiscountValue());
        setNullableBigDecimal(statement, 5, discount.getMaxDiscountAmount());
        setNullableDate(statement, 6, discount.getStartDate());
        setNullableDate(statement, 7, discount.getEndDate());
        statement.setBoolean(8, discount.isActive());
        statement.setInt(9, discount.getId());
    }

    /* =========================================================
       SQL HELPERS
    ========================================================= */

    private void setNullableDate(PreparedStatement statement, int index, LocalDate date)
            throws SQLException {

        if (date == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setDate(index, Date.valueOf(date));
        }
    }

    private void setNullableBigDecimal(PreparedStatement statement, int index, java.math.BigDecimal value)
            throws SQLException {

        if (value == null) {
            statement.setNull(index, Types.DECIMAL);
        } else {
            statement.setBigDecimal(index, value);
        }
    }

    private void rollbackQuietly(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Ignore rollback error.
        }
    }

    private void closeQuietly(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException ignored) {
            // Ignore close error.
        }
    }

    /* =========================================================
       NORMALIZE HELPERS
    ========================================================= */

    private String normalizeApplyScope(String applyScope) {
        if (applyScope == null || applyScope.isBlank()) {
            return SCOPE_ALL_BRAND_PRODUCTS;
        }

        String normalized = applyScope.trim().toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case SCOPE_SELECTED_PRODUCTS, "PRODUCTS", "PRODUCT" -> SCOPE_SELECTED_PRODUCTS;
            default -> SCOPE_ALL_BRAND_PRODUCTS;
        };
    }

    private String normalizeDiscountType(DiscountType discountType) {
        if (discountType == null) {
            return DiscountType.PERCENT.name();
        }

        return discountType.name();
    }

    private DiscountType normalizeDiscountType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return DiscountType.PERCENT;
        }

        String normalized = rawType.trim().toUpperCase(Locale.ROOT);

        if ("AMOUNT".equals(normalized)) {
            normalized = "FIXED";
        }

        return DiscountType.valueOf(normalized);
    }

    private List<Integer> normalizeProductIds(List<Integer> productIds) {
        Set<Integer> uniqueIds = new LinkedHashSet<>();

        if (productIds == null) {
            return new ArrayList<>();
        }

        for (Integer productId : productIds) {
            if (productId != null && productId > 0) {
                uniqueIds.add(productId);
            }
        }

        return new ArrayList<>(uniqueIds);
    }
}
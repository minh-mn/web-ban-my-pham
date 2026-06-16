package com.webshop.app.dao;

import com.webshop.app.model.Coupon;
import com.webshop.app.model.DiscountType;
import com.webshop.app.utils.DBConnection;

import java.math.BigDecimal;
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

public class CouponDAO {

    private static final String DEFAULT_RANK_CODE = "MEMBER";
    private static final String DEFAULT_COUPON_TYPE = "DISCOUNT";

    private static final String APPLY_SCOPE_ALL = "ALL";
    private static final String APPLY_SCOPE_BRAND = "BRAND";
    private static final String APPLY_SCOPE_PRODUCTS = "PRODUCTS";

    private static final String COUPON_SELECT_COLUMNS = """
            c.id AS id,
            c.code AS code,
            c.discount_percent AS discount_percent,
            c.discount_type AS discount_type,
            c.discount_value AS discount_value,
            c.max_discount_amount AS max_discount_amount,
            c.max_uses AS max_uses,
            c.used_count AS used_count,
            c.is_active AS is_active,
            c.start_date AS start_date,
            c.end_date AS end_date,
            c.type AS type,
            c.description AS description,
            c.apply_scope AS apply_scope,
            c.brand_id AS brand_id,
            b.name AS brand_name,
            c.min_order_amount AS min_order_amount,
            c.min_rank_code AS min_rank_code
            """;

    /* =========================================================
       FRONTEND / CHECKOUT
    ========================================================= */

    /**
     * Tìm coupon theo code, KHÔNG lọc active/hạn dùng.
     *
     * Lý do:
     * - CheckoutServlet cần phân biệt:
     *   + Mã không tồn tại.
     *   + Mã tồn tại nhưng bị tắt.
     *   + Mã tồn tại nhưng hết hạn.
     *   + Mã tồn tại nhưng chưa đủ điều kiện đơn hàng.
     */
    public Coupon findByCode(String code) {
        if (isBlank(code)) {
            return null;
        }

        String sql = """
                SELECT
                """ + COUPON_SELECT_COLUMNS + """
                FROM store_coupon c
                LEFT JOIN store_brand b ON c.brand_id = b.id
                WHERE UPPER(c.code) = UPPER(?)
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeCode(code));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                Coupon coupon = mapRow(resultSet);
                attachSelectedProductIds(coupon);
                return coupon;
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findByCode error", e);
        }
    }

    /**
     * Tìm coupon đang active + còn hạn + còn lượt.
     * Dùng cho các luồng chỉ cần mã có thể xem/lưu, chưa xét min_order_amount.
     */
    public Coupon findUsableBaseByCode(String code) {
        if (isBlank(code)) {
            return null;
        }

        String sql = """
                SELECT
                """ + COUPON_SELECT_COLUMNS + """
                FROM store_coupon c
                LEFT JOIN store_brand b ON c.brand_id = b.id
                WHERE UPPER(c.code) = UPPER(?)
                  AND c.is_active = 1
                  AND (c.start_date IS NULL OR c.start_date <= CURDATE())
                  AND (c.end_date IS NULL OR c.end_date >= CURDATE())
                  AND (COALESCE(c.max_uses, 0) <= 0 OR COALESCE(c.used_count, 0) < c.max_uses)
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeCode(code));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                Coupon coupon = mapRow(resultSet);
                attachSelectedProductIds(coupon);
                return coupon;
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findUsableBaseByCode error", e);
        }
    }

    /**
     * Dùng cho JS kiểm tra mã nhập tay:
     * lấy tất cả mã đang active, còn hạn, còn lượt.
     * Không lọc min_order_amount để checkout có thể hiện mã chưa đủ điều kiện ở trạng thái mờ.
     */
    public List<Coupon> findAllActiveCouponsForCheckout() {
        List<Coupon> coupons = new ArrayList<>();

        String sql = """
                SELECT
                """ + COUPON_SELECT_COLUMNS + """
                FROM store_coupon c
                LEFT JOIN store_brand b ON c.brand_id = b.id
                WHERE c.is_active = 1
                  AND (c.start_date IS NULL OR c.start_date <= CURDATE())
                  AND (c.end_date IS NULL OR c.end_date >= CURDATE())
                  AND (COALESCE(c.max_uses, 0) <= 0 OR COALESCE(c.used_count, 0) < c.max_uses)
                ORDER BY
                  c.min_order_amount ASC,
                  c.discount_value DESC,
                  c.discount_percent DESC,
                  COALESCE(c.max_discount_amount, 999999999) DESC,
                  c.id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                coupons.add(mapRow(resultSet));
            }

            attachSelectedProductIds(coupons);

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findAllActiveCouponsForCheckout error", e);
        }

        return coupons;
    }

    /**
     * Lấy danh sách mã dùng được cho tổng đơn hiện tại.
     * Dùng khi chỉ muốn hiện các mã đã đủ điều kiện.
     */
    public List<Coupon> findAvailableCouponsForCheckout(BigDecimal subtotal) {
        List<Coupon> coupons = new ArrayList<>();
        BigDecimal safeSubtotal = safeMoney(subtotal);

        String sql = """
                SELECT
                """ + COUPON_SELECT_COLUMNS + """
                FROM store_coupon c
                LEFT JOIN store_brand b ON c.brand_id = b.id
                WHERE c.is_active = 1
                  AND (c.start_date IS NULL OR c.start_date <= CURDATE())
                  AND (c.end_date IS NULL OR c.end_date >= CURDATE())
                  AND (COALESCE(c.max_uses, 0) <= 0 OR COALESCE(c.used_count, 0) < c.max_uses)
                  AND (c.min_order_amount IS NULL OR c.min_order_amount <= ?)
                ORDER BY
                  c.discount_value DESC,
                  c.discount_percent DESC,
                  COALESCE(c.max_discount_amount, 999999999) DESC,
                  c.id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBigDecimal(1, safeSubtotal);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    coupons.add(mapRow(resultSet));
                }
            }

            attachSelectedProductIds(coupons);

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findAvailableCouponsForCheckout error", e);
        }

        return coupons;
    }

    /**
     * Kiểm tra coupon đủ điều kiện cơ bản: active, còn hạn, còn lượt.
     */
    public boolean isValid(Coupon coupon) {
        if (coupon == null) {
            return false;
        }

        if (!coupon.isActive()) {
            return false;
        }

        LocalDate today = LocalDate.now();

        if (coupon.getStartDate() != null && today.isBefore(coupon.getStartDate())) {
            return false;
        }

        if (coupon.getEndDate() != null && today.isAfter(coupon.getEndDate())) {
            return false;
        }

        return coupon.getMaxUses() <= 0 || coupon.getUsedCount() < coupon.getMaxUses();
    }

    /**
     * Kiểm tra coupon có dùng được cho tổng đơn hiện tại không.
     */
    public boolean isUsableForSubtotal(Coupon coupon, BigDecimal subtotal) {
        if (!isValid(coupon)) {
            return false;
        }

        BigDecimal safeSubtotal = safeMoney(subtotal);
        BigDecimal minOrderAmount = safeMoney(coupon.getMinOrderAmount());

        return safeSubtotal.compareTo(minOrderAmount) >= 0;
    }

    /**
     * Trả lý do lỗi rõ ràng cho CheckoutServlet khi user nhập mã tay.
     */
    public String getCouponInvalidReason(Coupon coupon, BigDecimal subtotal) {
        if (coupon == null) {
            return "Mã khuyến mãi không tồn tại trong hệ thống.";
        }

        if (!coupon.isActive()) {
            return "Mã khuyến mãi hiện không còn hoạt động.";
        }

        LocalDate today = LocalDate.now();

        if (coupon.getStartDate() != null && today.isBefore(coupon.getStartDate())) {
            return "Mã khuyến mãi chưa đến thời gian sử dụng.";
        }

        if (coupon.getEndDate() != null && today.isAfter(coupon.getEndDate())) {
            return "Mã khuyến mãi đã hết hạn.";
        }

        if (coupon.getMaxUses() > 0 && coupon.getUsedCount() >= coupon.getMaxUses()) {
            return "Mã khuyến mãi đã hết lượt sử dụng.";
        }

        BigDecimal safeSubtotal = safeMoney(subtotal);
        BigDecimal minOrderAmount = safeMoney(coupon.getMinOrderAmount());

        if (safeSubtotal.compareTo(minOrderAmount) < 0) {
            return "Đơn hàng chưa đạt giá trị tối thiểu để dùng mã này.";
        }

        return null;
    }

    /**
     * Kiểm tra coupon có áp dụng cho một sản phẩm cụ thể không.
     *
     * @param coupon coupon đã được load.
     * @param productId id sản phẩm.
     * @param productBrandId brand_id của sản phẩm.
     */
    public boolean isApplicableToProduct(Coupon coupon, int productId, int productBrandId) {
        if (coupon == null || productId <= 0) {
            return false;
        }

        String scope = normalizeApplyScope(coupon.getApplyScope());

        if (APPLY_SCOPE_ALL.equals(scope)) {
            return true;
        }

        if (APPLY_SCOPE_BRAND.equals(scope)) {
            Integer couponBrandId = coupon.getBrandId();
            return couponBrandId != null
                    && couponBrandId > 0
                    && productBrandId > 0
                    && couponBrandId == productBrandId;
        }

        if (APPLY_SCOPE_PRODUCTS.equals(scope)) {
            List<Integer> selectedIds = coupon.getSelectedProductIds();

            if (selectedIds != null && !selectedIds.isEmpty()) {
                return selectedIds.contains(productId);
            }

            return existsProductTarget(coupon.getId(), productId);
        }

        return true;
    }

    public boolean existsProductTarget(int couponId, int productId) {
        if (couponId <= 0 || productId <= 0) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM store_coupon_product
                WHERE coupon_id = ?
                  AND product_id = ?
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, couponId);
            statement.setInt(2, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.existsProductTarget error", e);
        }
    }

    /**
     * Tăng used_count trong transaction, có kiểm tra còn lượt + còn hạn.
     */
    public void increaseUsedCount(Connection connection, int couponId) throws SQLException {
        if (!increaseUsedCountIfAvailable(connection, couponId)) {
            throw new SQLException("Coupon is not available or usage limit has been reached. couponId=" + couponId);
        }
    }

    public boolean increaseUsedCountIfAvailable(Connection conn, long couponId) throws SQLException {
        String sql = """
                UPDATE store_coupon
                SET used_count = used_count + 1,
                    updated_at = NOW()
                WHERE id = ?
                  AND is_active = 1
                  AND (COALESCE(max_uses, 0) <= 0 OR COALESCE(used_count, 0) < max_uses)
                  AND (start_date IS NULL OR start_date <= CURDATE())
                  AND (end_date IS NULL OR end_date >= CURDATE())
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, couponId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Coupon> findActiveCouponsForHome() {
        List<Coupon> coupons = new ArrayList<>();

        // Thêm LEFT JOIN store_brand b ON c.brand_id = b.id
        String sql = """
            SELECT
            c.id, c.code, c.discount_percent, c.discount_type, c.discount_value,
            c.max_discount_amount, c.max_uses, c.used_count, c.is_active,
            c.start_date, c.end_date, c.type, c.description, c.min_order_amount,
            c.min_rank_code, c.apply_scope, c.brand_id,
            b.name AS brand_name 
            FROM store_coupon c
            LEFT JOIN store_brand b ON c.brand_id = b.id
            WHERE c.is_active = 1
              AND (c.start_date IS NULL OR c.start_date <= CURDATE())
              AND (c.end_date IS NULL OR c.end_date >= CURDATE())
              AND (COALESCE(c.max_uses, 0) <= 0 OR COALESCE(c.used_count, 0) < c.max_uses)
            ORDER BY c.discount_percent DESC, c.id DESC
            LIMIT 6
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Coupon coupon = mapRow(resultSet);
                // Gán thêm brandName vào model nếu cần
                coupon.setBrandName(resultSet.getString("brand_name"));
                coupons.add(coupon);
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findActiveCouponsForHome error", e);
        }

        return coupons;
    }

    /* =========================================================
       ADMIN
    ========================================================= */

    public List<Coupon> findAll() {
        List<Coupon> coupons = new ArrayList<>();

        String sql = """
                SELECT
                """ + COUPON_SELECT_COLUMNS + """
                FROM store_coupon c
                LEFT JOIN store_brand b ON c.brand_id = b.id
                ORDER BY c.id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                coupons.add(mapRow(resultSet));
            }

            attachSelectedProductIds(coupons);

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findAll error", e);
        }

        return coupons;
    }

    public Coupon findById(int id) {
        String sql = """
                SELECT
                """ + COUPON_SELECT_COLUMNS + """
                FROM store_coupon c
                LEFT JOIN store_brand b ON c.brand_id = b.id
                WHERE c.id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Coupon coupon = mapRow(resultSet);
                    attachSelectedProductIds(coupon);
                    return coupon;
                }

                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findById error", e);
        }
    }

    public void create(Coupon coupon) {
        String sql = """
                INSERT INTO store_coupon
                (
                    code,
                    discount_percent,
                    discount_type,
                    discount_value,
                    apply_scope,
                    brand_id,
                    start_date,
                    end_date,
                    used_count,
                    is_active,
                    max_uses,
                    max_discount_amount,
                    description,
                    min_order_amount,
                    min_rank_code,
                    created_at,
                    updated_at,
                    type
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?)
                """;

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bindCouponForInsert(statement, coupon);
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        coupon.setId(generatedKeys.getInt(1));
                    }
                }
            }

            replaceSelectedProducts(connection, coupon.getId(), coupon.getSelectedProductIds(), coupon.getApplyScope());

            connection.commit();

        } catch (SQLException e) {
            rollbackQuietly(connection);
            throw new RuntimeException("CouponDAO.create error", e);

        } finally {
            closeQuietly(connection);
        }
    }

    public void update(Coupon coupon) {
        String sql = """
                UPDATE store_coupon
                SET code = ?,
                    discount_percent = ?,
                    discount_type = ?,
                    discount_value = ?,
                    apply_scope = ?,
                    brand_id = ?,
                    start_date = ?,
                    end_date = ?,
                    used_count = ?,
                    is_active = ?,
                    max_uses = ?,
                    max_discount_amount = ?,
                    description = ?,
                    min_order_amount = ?,
                    min_rank_code = ?,
                    updated_at = NOW(),
                    type = ?
                WHERE id = ?
                """;

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bindCouponForUpdate(statement, coupon);
                statement.executeUpdate();
            }

            replaceSelectedProducts(connection, coupon.getId(), coupon.getSelectedProductIds(), coupon.getApplyScope());

            connection.commit();

        } catch (SQLException e) {
            rollbackQuietly(connection);
            throw new RuntimeException("CouponDAO.update error", e);

        } finally {
            closeQuietly(connection);
        }
    }

    public void delete(int id) {
        String sql = """
                DELETE FROM store_coupon
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.delete error", e);
        }
    }

    public void toggleActive(int id) {
        String sql = """
                UPDATE store_coupon
                SET is_active = CASE
                    WHEN is_active = 1 THEN 0
                    ELSE 1
                END,
                    updated_at = NOW()
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.toggleActive error", e);
        }
    }

    public boolean existsByCode(String code) {
        if (isBlank(code)) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM store_coupon
                WHERE UPPER(code) = UPPER(?)
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeCode(code));

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.existsByCode error", e);
        }
    }

    public boolean softDisable(long id) {
        String sql = """
                UPDATE store_coupon
                SET is_active = 0,
                    updated_at = NOW()
                WHERE id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.softDisable error", e);
        }
    }

    public int deactivateExpiredCoupons() {
        String sql = """
                UPDATE store_coupon
                SET is_active = 0,
                    updated_at = NOW()
                WHERE is_active = 1
                  AND end_date IS NOT NULL
                  AND end_date < CURDATE()
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.deactivateExpiredCoupons error", e);
        }
    }

    /* =========================================================
       PRODUCT TARGETING
    ========================================================= */

    public List<Integer> findSelectedProductIds(int couponId) {
        List<Integer> productIds = new ArrayList<>();

        if (couponId <= 0) {
            return productIds;
        }

        String sql = """
                SELECT product_id
                FROM store_coupon_product
                WHERE coupon_id = ?
                ORDER BY product_id ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, couponId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    productIds.add(resultSet.getInt("product_id"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findSelectedProductIds error", e);
        }

        return productIds;
    }

    public void replaceSelectedProducts(int couponId, List<Integer> productIds, String applyScope) {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            replaceSelectedProducts(connection, couponId, productIds, applyScope);

            connection.commit();

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.replaceSelectedProducts error", e);
        }
    }

    private void replaceSelectedProducts(
            Connection connection,
            int couponId,
            List<Integer> productIds,
            String applyScope
    ) throws SQLException {

        if (couponId <= 0) {
            return;
        }

        deleteSelectedProducts(connection, couponId);

        if (!APPLY_SCOPE_PRODUCTS.equals(normalizeApplyScope(applyScope))) {
            return;
        }

        List<Integer> cleanedProductIds = normalizeProductIds(productIds);
        if (cleanedProductIds.isEmpty()) {
            return;
        }

        String sql = """
                INSERT IGNORE INTO store_coupon_product
                (
                    coupon_id,
                    product_id,
                    created_at
                )
                VALUES (?, ?, NOW())
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Integer productId : cleanedProductIds) {
                statement.setInt(1, couponId);
                statement.setInt(2, productId);
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void deleteSelectedProducts(Connection connection, int couponId) throws SQLException {
        String sql = """
                DELETE FROM store_coupon_product
                WHERE coupon_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, couponId);
            statement.executeUpdate();
        }
    }

    /* =========================================================
       USER COUPON WALLET
    ========================================================= */

    public boolean saveVoucherToUserCollection(int userId, String couponCode) {
        if (userId <= 0 || isBlank(couponCode)) {
            return false;
        }

        String sql = """
                INSERT IGNORE INTO user_coupon (user_id, coupon_id)
                SELECT ?, id
                FROM store_coupon
                WHERE UPPER(code) = UPPER(?)
                  AND is_active = 1
                  AND (start_date IS NULL OR start_date <= CURDATE())
                  AND (end_date IS NULL OR end_date >= CURDATE())
                  AND (COALESCE(max_uses, 0) <= 0 OR COALESCE(used_count, 0) < max_uses)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, normalizeCode(couponCode));

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.saveVoucherToUserCollection error", e);
        }
    }

    public List<Coupon> findSavedCouponsByUserId(int userId) {
        List<Coupon> coupons = new ArrayList<>();

        if (userId <= 0) {
            return coupons;
        }

        String sql = """
                SELECT
                """ + COUPON_SELECT_COLUMNS + """
                FROM store_coupon c
                LEFT JOIN store_brand b ON c.brand_id = b.id
                JOIN user_coupon uc ON c.id = uc.coupon_id
                WHERE uc.user_id = ?
                  AND COALESCE(uc.is_used, 0) = 0
                  AND c.is_active = 1
                  AND (c.start_date IS NULL OR c.start_date <= CURDATE())
                  AND (c.end_date IS NULL OR c.end_date >= CURDATE())
                  AND (COALESCE(c.max_uses, 0) <= 0 OR COALESCE(c.used_count, 0) < c.max_uses)
                ORDER BY uc.saved_at DESC, c.discount_value DESC, c.discount_percent DESC, c.id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    coupons.add(mapRow(resultSet));
                }
            }

            attachSelectedProductIds(coupons);

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findSavedCouponsByUserId error", e);
        }

        return coupons;
    }

    public List<Coupon> findAllActiveCoupons() {
        List<Coupon> coupons = new ArrayList<>();

        String sql = """
                SELECT
                """ + COUPON_SELECT_COLUMNS + """
                FROM store_coupon c
                LEFT JOIN store_brand b ON c.brand_id = b.id
                WHERE c.is_active = 1
                  AND (c.start_date IS NULL OR c.start_date <= CURDATE())
                  AND (c.end_date IS NULL OR c.end_date >= CURDATE())
                  AND (COALESCE(c.max_uses, 0) <= 0 OR COALESCE(c.used_count, 0) < c.max_uses)
                ORDER BY c.discount_value DESC, c.discount_percent DESC, c.id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                coupons.add(mapRow(resultSet));
            }

            attachSelectedProductIds(coupons);

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findAllActiveCoupons error", e);
        }

        return coupons;
    }

    /* =========================================================
       MAPPER
    ========================================================= */

    private Coupon mapRow(ResultSet resultSet) throws SQLException {
        Coupon coupon = new Coupon();

        coupon.setId(resultSet.getInt("id"));
        coupon.setCode(resultSet.getString("code"));

        coupon.setDiscountPercent(resultSet.getInt("discount_percent"));
        coupon.setDiscountType(resultSet.getString("discount_type"));

        BigDecimal discountValue = resultSet.getBigDecimal("discount_value");
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            discountValue = BigDecimal.valueOf(resultSet.getInt("discount_percent"));
        }
        coupon.setDiscountValue(discountValue);

        coupon.setMaxDiscountAmount(resultSet.getBigDecimal("max_discount_amount"));
        coupon.setMaxUses(resultSet.getInt("max_uses"));
        coupon.setUsedCount(resultSet.getInt("used_count"));
        coupon.setActive(resultSet.getBoolean("is_active"));

        coupon.setType(resultSet.getString("type"));
        coupon.setDescription(resultSet.getString("description"));

        coupon.setApplyScope(resultSet.getString("apply_scope"));
        coupon.setBrandId(getNullableInteger(resultSet, "brand_id"));
        coupon.setBrandName(resultSet.getString("brand_name"));

        coupon.setMinOrderAmount(resultSet.getBigDecimal("min_order_amount"));
        coupon.setMinRankCode(resultSet.getString("min_rank_code"));

        Date startDate = resultSet.getDate("start_date");
        Date endDate = resultSet.getDate("end_date");

        coupon.setStartDate(startDate == null ? null : startDate.toLocalDate());
        coupon.setEndDate(endDate == null ? null : endDate.toLocalDate());

        return coupon;
    }

    private void attachSelectedProductIds(Coupon coupon) {
        if (coupon == null || coupon.getId() <= 0) {
            return;
        }

        coupon.setSelectedProductIds(findSelectedProductIds(coupon.getId()));
    }

    private void attachSelectedProductIds(List<Coupon> coupons) {
        if (coupons == null || coupons.isEmpty()) {
            return;
        }

        for (Coupon coupon : coupons) {
            attachSelectedProductIds(coupon);
        }
    }

    private Integer getNullableInteger(ResultSet resultSet, String columnName) throws SQLException {
        int value = resultSet.getInt(columnName);
        return resultSet.wasNull() ? null : value;
    }

    /* =========================================================
       BIND INSERT / UPDATE
    ========================================================= */

    private void bindCouponForInsert(PreparedStatement statement, Coupon coupon) throws SQLException {
        statement.setString(1, normalizeCode(coupon.getCode()));
        statement.setInt(2, normalizeDiscountPercent(coupon));
        statement.setString(3, normalizeDiscountType(coupon));
        statement.setBigDecimal(4, normalizeDiscountValue(coupon));
        statement.setString(5, normalizeApplyScope(coupon.getApplyScope()));
        setNullableInteger(statement, 6, normalizeBrandId(coupon));

        setNullableDate(statement, 7, coupon.getStartDate());
        setNullableDate(statement, 8, coupon.getEndDate());

        statement.setInt(9, 0);
        statement.setBoolean(10, coupon.isActive());

        statement.setInt(11, Math.max(coupon.getMaxUses(), 0));
        setNullableBigDecimal(statement, 12, coupon.getMaxDiscountAmount());

        statement.setString(13, normalizeNullableText(coupon.getDescription()));
        statement.setBigDecimal(14, normalizeMinOrderAmount(coupon.getMinOrderAmount()));
        statement.setString(15, normalizeRankCode(coupon.getMinRankCode()));
        statement.setString(16, normalizeCouponType(coupon.getType()));
    }

    private void bindCouponForUpdate(PreparedStatement statement, Coupon coupon) throws SQLException {
        statement.setString(1, normalizeCode(coupon.getCode()));
        statement.setInt(2, normalizeDiscountPercent(coupon));
        statement.setString(3, normalizeDiscountType(coupon));
        statement.setBigDecimal(4, normalizeDiscountValue(coupon));
        statement.setString(5, normalizeApplyScope(coupon.getApplyScope()));
        setNullableInteger(statement, 6, normalizeBrandId(coupon));

        setNullableDate(statement, 7, coupon.getStartDate());
        setNullableDate(statement, 8, coupon.getEndDate());

        statement.setInt(9, Math.max(coupon.getUsedCount(), 0));
        statement.setBoolean(10, coupon.isActive());

        statement.setInt(11, Math.max(coupon.getMaxUses(), 0));
        setNullableBigDecimal(statement, 12, coupon.getMaxDiscountAmount());

        statement.setString(13, normalizeNullableText(coupon.getDescription()));
        statement.setBigDecimal(14, normalizeMinOrderAmount(coupon.getMinOrderAmount()));
        statement.setString(15, normalizeRankCode(coupon.getMinRankCode()));
        statement.setString(16, normalizeCouponType(coupon.getType()));
        statement.setInt(17, coupon.getId());
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

    private void setNullableBigDecimal(PreparedStatement statement, int index, BigDecimal value)
            throws SQLException {

        if (value == null) {
            statement.setNull(index, Types.DECIMAL);
        } else {
            statement.setBigDecimal(index, value);
        }
    }

    private void setNullableInteger(PreparedStatement statement, int index, Integer value)
            throws SQLException {

        if (value == null || value <= 0) {
            statement.setNull(index, Types.BIGINT);
        } else {
            statement.setInt(index, value);
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

    private BigDecimal safeMoney(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return value;
    }

    private BigDecimal normalizeMinOrderAmount(BigDecimal value) {
        return safeMoney(value);
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeCouponType(String type) {
        if (type == null || type.isBlank()) {
            return DEFAULT_COUPON_TYPE;
        }

        return type.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeRankCode(String rankCode) {
        if (rankCode == null || rankCode.isBlank()) {
            return DEFAULT_RANK_CODE;
        }

        return rankCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeNullableText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        return text.trim();
    }

    private String normalizeDiscountType(Coupon coupon) {
        DiscountType discountType = coupon.getDiscountType();

        if (discountType == null) {
            return DiscountType.PERCENT.name();
        }

        return discountType.name();
    }

    private BigDecimal normalizeDiscountValue(Coupon coupon) {
        BigDecimal discountValue = coupon.getDiscountValue();

        if (discountValue != null && discountValue.compareTo(BigDecimal.ZERO) > 0) {
            return discountValue;
        }

        if (coupon.getDiscountPercent() > 0) {
            return BigDecimal.valueOf(coupon.getDiscountPercent());
        }

        return BigDecimal.ZERO;
    }

    private int normalizeDiscountPercent(Coupon coupon) {
        if (coupon.getDiscountType() == DiscountType.PERCENT) {
            BigDecimal discountValue = normalizeDiscountValue(coupon);

            if (discountValue.compareTo(BigDecimal.ZERO) > 0) {
                return discountValue.intValue();
            }

            return Math.max(coupon.getDiscountPercent(), 0);
        }

        return 0;
    }

    private String normalizeApplyScope(String applyScope) {
        if (applyScope == null || applyScope.isBlank()) {
            return APPLY_SCOPE_ALL;
        }

        String normalized = applyScope.trim().toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case APPLY_SCOPE_BRAND -> APPLY_SCOPE_BRAND;
            case APPLY_SCOPE_PRODUCTS, "SELECTED_PRODUCTS", "PRODUCT" -> APPLY_SCOPE_PRODUCTS;
            default -> APPLY_SCOPE_ALL;
        };
    }

    private Integer normalizeBrandId(Coupon coupon) {
        String applyScope = normalizeApplyScope(coupon.getApplyScope());

        if (!APPLY_SCOPE_BRAND.equals(applyScope)) {
            return null;
        }

        Integer brandId = coupon.getBrandId();
        return brandId != null && brandId > 0 ? brandId : null;
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

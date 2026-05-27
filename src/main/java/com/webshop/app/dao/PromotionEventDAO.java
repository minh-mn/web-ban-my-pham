package com.webshop.app.dao;

import com.webshop.app.model.DiscountType;
import com.webshop.app.model.PromotionEvent;
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

public class PromotionEventDAO {

	private static final String TABLE = "store_promotionevent";

	private static final String SCOPE_ALL = "ALL";
	private static final String SCOPE_CATEGORY = "CATEGORY";
	private static final String SCOPE_BRAND = "BRAND";
	private static final String SCOPE_PRODUCTS = "PRODUCTS";

	private static final String EVENT_SELECT_COLUMNS = """
            e.id AS id,
            e.name AS name,
            e.scope AS scope,
            e.discount_type AS discount_type,
            e.discount_value AS discount_value,
            e.max_discount_amount AS max_discount_amount,
            e.brand_id AS brand_id,
            e.category_id AS category_id,
            e.start_date AS start_date,
            e.end_date AS end_date,
            e.is_active AS is_active,
            b.name AS brand_name,
            c.name AS category_name
            """;

    /* =========================================================
       CREATE
    ========================================================= */

	public int create(PromotionEvent event) {
		String sql = """
                INSERT INTO store_promotionevent
                (
                    name,
                    scope,
                    discount_type,
                    discount_value,
                    max_discount_amount,
                    brand_id,
                    category_id,
                    start_date,
                    end_date,
                    is_active
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

		Connection connection = null;

		try {
			connection = DBConnection.getConnection();
			connection.setAutoCommit(false);

			int generatedId = 0;

			try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
				bindForInsert(statement, event);
				statement.executeUpdate();

				try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						generatedId = generatedKeys.getInt(1);
						event.setId(generatedId);
					}
				}
			}

			replaceSelectedProducts(
					connection,
					event.getId(),
					event.getSelectedProductIds(),
					event.getScope()
			);

			connection.commit();
			return generatedId;

		} catch (SQLException ex) {
			rollbackQuietly(connection);
			throw new RuntimeException("PromotionEventDAO.create error", ex);

		} finally {
			closeQuietly(connection);
		}
	}

    /* =========================================================
       FIND ALL
    ========================================================= */

	public List<PromotionEvent> findAll(boolean joinRef) {
		List<PromotionEvent> events = new ArrayList<>();

		String sql;

		if (joinRef) {
			sql = """
                    SELECT
                    """ + EVENT_SELECT_COLUMNS + """
                    FROM store_promotionevent e
                    LEFT JOIN store_brand b ON b.id = e.brand_id
                    LEFT JOIN store_category c ON c.id = e.category_id
                    ORDER BY e.id DESC
                    """;
		} else {
			sql = """
                    SELECT
                        e.id AS id,
                        e.name AS name,
                        e.scope AS scope,
                        e.discount_type AS discount_type,
                        e.discount_value AS discount_value,
                        e.max_discount_amount AS max_discount_amount,
                        e.brand_id AS brand_id,
                        e.category_id AS category_id,
                        e.start_date AS start_date,
                        e.end_date AS end_date,
                        e.is_active AS is_active,
                        NULL AS brand_name,
                        NULL AS category_name
                    FROM store_promotionevent e
                    ORDER BY e.id DESC
                    """;
		}

		try (Connection connection = DBConnection.getConnection();
		     PreparedStatement statement = connection.prepareStatement(sql);
		     ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {
				events.add(mapRow(resultSet));
			}

			attachSelectedProductIds(events);

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.findAll error", ex);
		}

		return events;
	}

	public List<PromotionEvent> findAll() {
		return findAll(false);
	}

    /* =========================================================
       FIND BY ID
    ========================================================= */

	public PromotionEvent findById(int id) {
		if (id <= 0) {
			return null;
		}

		String sql = """
                SELECT
                """ + EVENT_SELECT_COLUMNS + """
                FROM store_promotionevent e
                LEFT JOIN store_brand b ON b.id = e.brand_id
                LEFT JOIN store_category c ON c.id = e.category_id
                WHERE e.id = ?
                """;

		try (Connection connection = DBConnection.getConnection();
		     PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setInt(1, id);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return null;
				}

				PromotionEvent event = mapRow(resultSet);
				attachSelectedProductIds(event);
				return event;
			}

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.findById error", ex);
		}
	}

    /* =========================================================
       FRONTEND / PRICING
    ========================================================= */

	public List<PromotionEvent> findActiveEventsForProduct(int productId, int brandId, int categoryId) {
		List<PromotionEvent> events = new ArrayList<>();

		if (productId <= 0) {
			return events;
		}

		String sql = """
                SELECT
                """ + EVENT_SELECT_COLUMNS + """
                FROM store_promotionevent e
                LEFT JOIN store_brand b ON b.id = e.brand_id
                LEFT JOIN store_category c ON c.id = e.category_id
                WHERE e.is_active = 1
                  AND (e.start_date IS NULL OR e.start_date <= CURDATE())
                  AND (e.end_date IS NULL OR e.end_date >= CURDATE())
                  AND (
                        e.scope = 'ALL'
                        OR (e.scope = 'BRAND' AND e.brand_id = ?)
                        OR (e.scope = 'CATEGORY' AND e.category_id = ?)
                        OR (
                            e.scope = 'PRODUCTS'
                            AND EXISTS (
                                SELECT 1
                                FROM store_promotionevent_product ep
                                WHERE ep.promotion_event_id = e.id
                                  AND ep.product_id = ?
                            )
                        )
                  )
                ORDER BY e.discount_value DESC, e.id DESC
                """;

		try (Connection connection = DBConnection.getConnection();
		     PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setInt(1, brandId);
			statement.setInt(2, categoryId);
			statement.setInt(3, productId);

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					events.add(mapRow(resultSet));
				}
			}

			attachSelectedProductIds(events);

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.findActiveEventsForProduct error", ex);
		}

		return events;
	}

	public PromotionEvent findBestActiveForProduct(int productId, int brandId, int categoryId) {
		List<PromotionEvent> events = findActiveEventsForProduct(productId, brandId, categoryId);

		if (events.isEmpty()) {
			return null;
		}

		return events.get(0);
	}

	public boolean isValid(PromotionEvent event) {
		if (event == null || !event.isActive()) {
			return false;
		}

		LocalDate today = LocalDate.now();

		if (event.getStartDate() != null && today.isBefore(event.getStartDate())) {
			return false;
		}

		if (event.getEndDate() != null && today.isAfter(event.getEndDate())) {
			return false;
		}

		return true;
	}

	public boolean isApplicableToProduct(PromotionEvent event, int productId, int productBrandId, int productCategoryId) {
		if (!isValid(event) || productId <= 0) {
			return false;
		}

		PromotionEvent.Scope scope = event.getScope();

		if (scope == PromotionEvent.Scope.ALL) {
			return true;
		}

		if (scope == PromotionEvent.Scope.BRAND) {
			return event.getBrandId() != null
					&& event.getBrandId() > 0
					&& productBrandId > 0
					&& event.getBrandId() == productBrandId;
		}

		if (scope == PromotionEvent.Scope.CATEGORY) {
			return event.getCategoryId() != null
					&& event.getCategoryId() > 0
					&& productCategoryId > 0
					&& event.getCategoryId() == productCategoryId;
		}

		if (scope == PromotionEvent.Scope.PRODUCTS) {
			List<Integer> selectedProductIds = event.getSelectedProductIds();

			if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
				return selectedProductIds.contains(productId);
			}

			return existsProductTarget(event.getId(), productId);
		}

		return false;
	}

	public boolean existsProductTarget(int promotionEventId, int productId) {
		if (promotionEventId <= 0 || productId <= 0) {
			return false;
		}

		String sql = """
                SELECT 1
                FROM store_promotionevent_product
                WHERE promotion_event_id = ?
                  AND product_id = ?
                LIMIT 1
                """;

		try (Connection connection = DBConnection.getConnection();
		     PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setInt(1, promotionEventId);
			statement.setInt(2, productId);

			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next();
			}

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.existsProductTarget error", ex);
		}
	}

    /* =========================================================
       UPDATE
    ========================================================= */

	public void update(PromotionEvent event) {
		String sql = """
                UPDATE store_promotionevent
                SET name = ?,
                    scope = ?,
                    discount_type = ?,
                    discount_value = ?,
                    max_discount_amount = ?,
                    brand_id = ?,
                    category_id = ?,
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
				bindForUpdate(statement, event);
				statement.executeUpdate();
			}

			replaceSelectedProducts(
					connection,
					event.getId(),
					event.getSelectedProductIds(),
					event.getScope()
			);

			connection.commit();

		} catch (SQLException ex) {
			rollbackQuietly(connection);
			throw new RuntimeException("PromotionEventDAO.update error", ex);

		} finally {
			closeQuietly(connection);
		}
	}

    /* =========================================================
       TOGGLE ACTIVE
    ========================================================= */

	public void toggleActive(int id) {
		if (id <= 0) {
			return;
		}

		String sql = """
                UPDATE store_promotionevent
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

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.toggleActive error", ex);
		}
	}

    /* =========================================================
       DELETE
    ========================================================= */

	public void delete(int id) {
		if (id <= 0) {
			return;
		}

		String sql = """
                DELETE FROM store_promotionevent
                WHERE id = ?
                """;

		try (Connection connection = DBConnection.getConnection();
		     PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setInt(1, id);
			statement.executeUpdate();

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.delete error", ex);
		}
	}

    /* =========================================================
       PRODUCT TARGETING
    ========================================================= */

	public List<Integer> findSelectedProductIds(int promotionEventId) {
		List<Integer> productIds = new ArrayList<>();

		if (promotionEventId <= 0) {
			return productIds;
		}

		String sql = """
                SELECT product_id
                FROM store_promotionevent_product
                WHERE promotion_event_id = ?
                ORDER BY product_id ASC
                """;

		try (Connection connection = DBConnection.getConnection();
		     PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setInt(1, promotionEventId);

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					productIds.add(resultSet.getInt("product_id"));
				}
			}

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.findSelectedProductIds error", ex);
		}

		return productIds;
	}

	public void replaceSelectedProducts(
			int promotionEventId,
			List<Integer> productIds,
			PromotionEvent.Scope scope
	) {
		Connection connection = null;

		try {
			connection = DBConnection.getConnection();
			connection.setAutoCommit(false);

			replaceSelectedProducts(connection, promotionEventId, productIds, scope);

			connection.commit();

		} catch (SQLException ex) {
			rollbackQuietly(connection);
			throw new RuntimeException("PromotionEventDAO.replaceSelectedProducts error", ex);

		} finally {
			closeQuietly(connection);
		}
	}

	private void replaceSelectedProducts(
			Connection connection,
			int promotionEventId,
			List<Integer> productIds,
			PromotionEvent.Scope scope
	) throws SQLException {

		if (promotionEventId <= 0) {
			return;
		}

		deleteSelectedProducts(connection, promotionEventId);

		if (scope != PromotionEvent.Scope.PRODUCTS) {
			return;
		}

		List<Integer> cleanedProductIds = normalizeProductIds(productIds);
		if (cleanedProductIds.isEmpty()) {
			return;
		}

		String sql = """
                INSERT IGNORE INTO store_promotionevent_product
                (
                    promotion_event_id,
                    product_id,
                    created_at
                )
                VALUES (?, ?, NOW())
                """;

		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			for (Integer productId : cleanedProductIds) {
				statement.setInt(1, promotionEventId);
				statement.setInt(2, productId);
				statement.addBatch();
			}

			statement.executeBatch();
		}
	}

	private void deleteSelectedProducts(Connection connection, int promotionEventId) throws SQLException {
		String sql = """
                DELETE FROM store_promotionevent_product
                WHERE promotion_event_id = ?
                """;

		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, promotionEventId);
			statement.executeUpdate();
		}
	}

    /* =========================================================
       MAP
    ========================================================= */

	private PromotionEvent mapRow(ResultSet resultSet) throws SQLException {
		PromotionEvent event = new PromotionEvent();

		event.setId(resultSet.getInt("id"));
		event.setName(resultSet.getString("name"));
		event.setScope(normalizeScope(resultSet.getString("scope")));
		event.setDiscountType(normalizeDiscountType(resultSet.getString("discount_type")));
		event.setDiscountValue(resultSet.getBigDecimal("discount_value"));
		event.setMaxDiscountAmount(resultSet.getBigDecimal("max_discount_amount"));

		event.setBrandId(getNullableInteger(resultSet, "brand_id"));
		event.setCategoryId(getNullableInteger(resultSet, "category_id"));

		Date startDate = resultSet.getDate("start_date");
		Date endDate = resultSet.getDate("end_date");

		event.setStartDate(startDate == null ? null : startDate.toLocalDate());
		event.setEndDate(endDate == null ? null : endDate.toLocalDate());

		event.setActive(resultSet.getBoolean("is_active"));
		event.setBrandName(resultSet.getString("brand_name"));
		event.setCategoryName(resultSet.getString("category_name"));

		return event;
	}

	private void attachSelectedProductIds(PromotionEvent event) {
		if (event == null || event.getId() <= 0) {
			return;
		}

		event.setSelectedProductIds(findSelectedProductIds(event.getId()));
	}

	private void attachSelectedProductIds(List<PromotionEvent> events) {
		if (events == null || events.isEmpty()) {
			return;
		}

		for (PromotionEvent event : events) {
			attachSelectedProductIds(event);
		}
	}

    /* =========================================================
       BIND
    ========================================================= */

	private void bindForInsert(PreparedStatement statement, PromotionEvent event) throws SQLException {
		statement.setString(1, safeText(event.getName()));
		statement.setString(2, normalizeScope(event.getScope()).name());
		statement.setString(3, normalizeDiscountType(event.getDiscountType()).name());
		statement.setBigDecimal(4, event.getDiscountValue());
		setNullableBigDecimal(statement, 5, event.getMaxDiscountAmount());
		setNullableInteger(statement, 6, normalizeBrandId(event));
		setNullableInteger(statement, 7, normalizeCategoryId(event));
		setNullableDate(statement, 8, event.getStartDate());
		setNullableDate(statement, 9, event.getEndDate());
		statement.setBoolean(10, event.isActive());
	}

	private void bindForUpdate(PreparedStatement statement, PromotionEvent event) throws SQLException {
		statement.setString(1, safeText(event.getName()));
		statement.setString(2, normalizeScope(event.getScope()).name());
		statement.setString(3, normalizeDiscountType(event.getDiscountType()).name());
		statement.setBigDecimal(4, event.getDiscountValue());
		setNullableBigDecimal(statement, 5, event.getMaxDiscountAmount());
		setNullableInteger(statement, 6, normalizeBrandId(event));
		setNullableInteger(statement, 7, normalizeCategoryId(event));
		setNullableDate(statement, 8, event.getStartDate());
		setNullableDate(statement, 9, event.getEndDate());
		statement.setBoolean(10, event.isActive());
		statement.setInt(11, event.getId());
	}

    /* =========================================================
       SQL HELPERS
    ========================================================= */

	private void setNullableDate(PreparedStatement statement, int index, LocalDate date) throws SQLException {
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

	private void setNullableInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
		if (value == null || value <= 0) {
			statement.setNull(index, Types.BIGINT);
		} else {
			statement.setInt(index, value);
		}
	}

	private Integer getNullableInteger(ResultSet resultSet, String columnName) throws SQLException {
		int value = resultSet.getInt(columnName);
		return resultSet.wasNull() ? null : value;
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

	private PromotionEvent.Scope normalizeScope(PromotionEvent.Scope scope) {
		return scope == null ? PromotionEvent.Scope.ALL : scope;
	}

	private PromotionEvent.Scope normalizeScope(String rawScope) {
		if (rawScope == null || rawScope.isBlank()) {
			return PromotionEvent.Scope.ALL;
		}

		String normalized = rawScope.trim().toUpperCase(Locale.ROOT);

		return switch (normalized) {
			case SCOPE_CATEGORY -> PromotionEvent.Scope.CATEGORY;
			case SCOPE_BRAND -> PromotionEvent.Scope.BRAND;
			case SCOPE_PRODUCTS, "PRODUCT", "SELECTED_PRODUCTS" -> PromotionEvent.Scope.PRODUCTS;
			default -> PromotionEvent.Scope.ALL;
		};
	}

	private DiscountType normalizeDiscountType(DiscountType discountType) {
		return discountType == null ? DiscountType.PERCENT : discountType;
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

	private Integer normalizeBrandId(PromotionEvent event) {
		if (normalizeScope(event.getScope()) != PromotionEvent.Scope.BRAND) {
			return null;
		}

		Integer brandId = event.getBrandId();
		return brandId != null && brandId > 0 ? brandId : null;
	}

	private Integer normalizeCategoryId(PromotionEvent event) {
		if (normalizeScope(event.getScope()) != PromotionEvent.Scope.CATEGORY) {
			return null;
		}

		Integer categoryId = event.getCategoryId();
		return categoryId != null && categoryId > 0 ? categoryId : null;
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

	private String safeText(String value) {
		return value == null ? "" : value.trim();
	}

	public PromotionEvent findActiveEvent() {
		String sql = "SELECT * FROM " + TABLE +
				" WHERE is_active = 1 " +
				" AND CURDATE() BETWEEN start_date AND end_date " +
				" ORDER BY id DESC LIMIT 1";

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql);
		     ResultSet rs = ps.executeQuery()) {

			if (rs.next()) {
				return mapRow(rs);
			}
		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.findActiveEvent error", ex);
		}
		return null;
	}
}

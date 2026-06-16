package com.webshop.app.dao;

import com.webshop.app.model.Brand;
import com.webshop.app.model.Category;
import com.webshop.app.model.Product;
import com.webshop.app.model.PromotionEvent;
import com.webshop.app.utils.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProductDAO {

	/* =====================================================
	   HOT COLLECTION / SEMANTIC KEYWORD SEARCH
	===================================================== */

	public int countProductsByKeywordSet(
			List<String> keywords,
			List<Integer> brandIds,
			List<String> priceRanges,
			Integer minRating
	) {
		StringBuilder sql = new StringBuilder(
				"SELECT COUNT(*) FROM ( " +
						"SELECT p.id " +
						"FROM store_product p " +
						"LEFT JOIN store_review r ON p.id = r.product_id " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"WHERE p.is_active = 1 "
		);

		List<String> normalizedKeywords = normalizeCollectionKeywords(keywords);

		appendKeywordSetCondition(sql, normalizedKeywords);

		if (brandIds != null && !brandIds.isEmpty()) {
			sql.append("AND p.brand_id IN (")
					.append(placeholders(brandIds.size()))
					.append(") ");
		}

		appendPriceRanges(sql, priceRanges);

		sql.append("GROUP BY p.id ");

		if (minRating != null) {
			sql.append("HAVING COALESCE(AVG(r.rating), 0) >= ? ");
		}

		sql.append(") x");

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql.toString())) {

			int idx = 1;

			idx = bindKeywordSet(ps, normalizedKeywords, idx);
			idx = bindIntegerList(ps, brandIds, idx);

			if (minRating != null) {
				ps.setInt(idx, minRating);
			}

			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.countProductsByKeywordSet error", e);
		}
	}

	public List<Product> findProductsPagedByKeywordSet(
			List<String> keywords,
			List<Integer> brandIds,
			String sort,
			List<String> priceRanges,
			Integer minRating,
			int page,
			int pageSize
	) {
		int safePage = Math.max(1, page);
		int safeSize = Math.max(1, pageSize);
		int offset = (safePage - 1) * safeSize;

		List<Product> list = new ArrayList<>();

		StringBuilder sql = new StringBuilder(
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.created_at, " +
						"COALESCE(AVG(r.rating), 0) AS avg_rating, " +
						"COUNT(r.id) AS review_count, " +
						"c.id AS c_id, c.name AS c_name, " +
						"b.id AS b_id, b.name AS b_name " +
						"FROM store_product p " +
						"LEFT JOIN store_review r ON p.id = r.product_id " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"WHERE p.is_active = 1 "
		);

		List<String> normalizedKeywords = normalizeCollectionKeywords(keywords);

		appendKeywordSetCondition(sql, normalizedKeywords);

		if (brandIds != null && !brandIds.isEmpty()) {
			sql.append("AND p.brand_id IN (")
					.append(placeholders(brandIds.size()))
					.append(") ");
		}

		appendPriceRanges(sql, priceRanges);

		sql.append("GROUP BY p.id, p.title, p.slug, p.description, ")
				.append("p.price, p.discount_percent, p.stock, p.image, p.created_at, ")
				.append("c.id, c.name, b.id, b.name ");

		if (minRating != null) {
			sql.append("HAVING COALESCE(AVG(r.rating), 0) >= ? ");
		}

		appendSort(sql, sort);

		sql.append("LIMIT ?, ?");

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql.toString())) {

			int idx = 1;

			idx = bindKeywordSet(ps, normalizedKeywords, idx);
			idx = bindIntegerList(ps, brandIds, idx);

			if (minRating != null) {
				ps.setInt(idx++, minRating);
			}

			ps.setInt(idx++, offset);
			ps.setInt(idx, safeSize);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapRowList(rs));
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findProductsPagedByKeywordSet error", e);
		}

		return list;
	}

	private void appendKeywordSetCondition(StringBuilder sql, List<String> keywords) {
		if (keywords == null || keywords.isEmpty()) {
			return;
		}

		sql.append("AND (");

		for (int i = 0; i < keywords.size(); i++) {
			if (i > 0) {
				sql.append(" OR ");
			}

			sql.append("LOWER(CONCAT_WS(' ', ")
					.append("p.title, ")
					.append("COALESCE(p.description, ''), ")
					.append("COALESCE(p.slug, ''), ")
					.append("COALESCE(c.name, ''), ")
					.append("COALESCE(c.slug, '')")
					.append(")) LIKE ? ");
		}

		sql.append(") ");
	}

	private List<String> normalizeCollectionKeywords(List<String> keywords) {
		if (keywords == null || keywords.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> result = new ArrayList<>();

		for (String keyword : keywords) {
			if (keyword == null) {
				continue;
			}

			String value = keyword.trim().toLowerCase(Locale.ROOT);

			if (!value.isEmpty() && !result.contains(value)) {
				result.add(value);
			}
		}

		return result;
	}

	private int bindKeywordSet(
			PreparedStatement ps,
			List<String> keywords,
			int startIndex
	) throws SQLException {
		int idx = startIndex;

		if (keywords == null || keywords.isEmpty()) {
			return idx;
		}

		for (String keyword : keywords) {
			ps.setString(idx++, "%" + keyword + "%");
		}

		return idx;
	}

	private int bindIntegerList(
			PreparedStatement ps,
			List<Integer> values,
			int startIndex
	) throws SQLException {
		int idx = startIndex;

		if (values == null || values.isEmpty()) {
			return idx;
		}

		for (Integer value : values) {
			if (value != null) {
				ps.setInt(idx++, value);
			}
		}

		return idx;
	}


	public enum DeleteMode {
		HARD_DELETED,
		SOFT_DELETED,
		NOT_FOUND
	}

    /* =========================================================
       FRONTEND PRODUCT LIST
       Giữ nguyên luồng chính: /products lần đầu vẫn load tất cả sản phẩm active
    ========================================================= */

	public List<Product> findProducts(
			String keyword,
			List<Integer> categoryIds,
			List<Integer> brandIds,
			String sort,
			List<String> priceRanges,
			Integer minRating
	) {
		List<Product> list = new ArrayList<>();

		StringBuilder sql = new StringBuilder(
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.created_at, " +
						"COALESCE(AVG(r.rating), 0) AS avg_rating, " +
						"COUNT(r.id) AS review_count, " +
						"c.id AS c_id, c.name AS c_name, " +
						"b.id AS b_id, b.name AS b_name " +
						"FROM store_product p " +
						"LEFT JOIN store_review r ON p.id = r.product_id " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"WHERE p.is_active = 1 "
		);

		if (keyword != null && !keyword.isBlank()) {
			sql.append("AND (p.title LIKE ? OR p.description LIKE ?) ");
		}

		if (categoryIds != null && !categoryIds.isEmpty()) {
			sql.append("AND p.category_id IN (")
					.append(placeholders(categoryIds.size()))
					.append(") ");
		}

		if (brandIds != null && !brandIds.isEmpty()) {
			sql.append("AND p.brand_id IN (")
					.append(placeholders(brandIds.size()))
					.append(") ");
		}

		appendPriceRanges(sql, priceRanges);

		sql.append("GROUP BY p.id, p.title, p.slug, p.description, ")
				.append("p.price, p.discount_percent, p.stock, p.image, p.created_at, ")
				.append("c.id, c.name, b.id, b.name ");

		if (minRating != null) {
			sql.append("HAVING COALESCE(AVG(r.rating), 0) >= ? ");
		}

		appendSort(sql, sort);

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql.toString())) {

			bindProductFilters(ps, keyword, categoryIds, brandIds, minRating, 1);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapRowList(rs));
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findProducts error", e);
		}

		return list;
	}

	public int countProducts(
			String keyword,
			List<Integer> categoryIds,
			List<Integer> brandIds,
			List<String> priceRanges,
			Integer minRating
	) {
		StringBuilder sql = new StringBuilder(
				"SELECT COUNT(*) FROM ( " +
						"SELECT p.id " +
						"FROM store_product p " +
						"LEFT JOIN store_review r ON p.id = r.product_id " +
						"WHERE p.is_active = 1 "
		);

		if (keyword != null && !keyword.isBlank()) {
			sql.append("AND (p.title LIKE ? OR p.description LIKE ?) ");
		}

		if (categoryIds != null && !categoryIds.isEmpty()) {
			sql.append("AND p.category_id IN (")
					.append(placeholders(categoryIds.size()))
					.append(") ");
		}

		if (brandIds != null && !brandIds.isEmpty()) {
			sql.append("AND p.brand_id IN (")
					.append(placeholders(brandIds.size()))
					.append(") ");
		}

		appendPriceRanges(sql, priceRanges);

		sql.append("GROUP BY p.id ");

		if (minRating != null) {
			sql.append("HAVING COALESCE(AVG(r.rating), 0) >= ? ");
		}

		sql.append(") x");

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql.toString())) {

			bindProductFilters(ps, keyword, categoryIds, brandIds, minRating, 1);

			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.countProducts error", e);
		}
	}

	public List<Product> findProductsPaged(
			String keyword,
			List<Integer> categoryIds,
			List<Integer> brandIds,
			String sort,
			List<String> priceRanges,
			Integer minRating,
			int page,
			int pageSize
	) {
		int safePage = Math.max(1, page);
		int safeSize = Math.max(1, pageSize);
		int offset = (safePage - 1) * safeSize;

		List<Product> list = new ArrayList<>();

		StringBuilder sql = new StringBuilder(
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.created_at, " +
						"COALESCE(AVG(r.rating), 0) AS avg_rating, " +
						"COUNT(r.id) AS review_count, " +
						"c.id AS c_id, c.name AS c_name, " +
						"b.id AS b_id, b.name AS b_name " +
						"FROM store_product p " +
						"LEFT JOIN store_review r ON p.id = r.product_id " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"WHERE p.is_active = 1 "
		);

		if (keyword != null && !keyword.isBlank()) {
			sql.append("AND (p.title LIKE ? OR p.description LIKE ?) ");
		}

		if (categoryIds != null && !categoryIds.isEmpty()) {
			sql.append("AND p.category_id IN (")
					.append(placeholders(categoryIds.size()))
					.append(") ");
		}

		if (brandIds != null && !brandIds.isEmpty()) {
			sql.append("AND p.brand_id IN (")
					.append(placeholders(brandIds.size()))
					.append(") ");
		}

		appendPriceRanges(sql, priceRanges);

		sql.append("GROUP BY p.id, p.title, p.slug, p.description, ")
				.append("p.price, p.discount_percent, p.stock, p.image, p.created_at, ")
				.append("c.id, c.name, b.id, b.name ");

		if (minRating != null) {
			sql.append("HAVING COALESCE(AVG(r.rating), 0) >= ? ");
		}

		appendSort(sql, sort);

		sql.append("LIMIT ?, ?");

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql.toString())) {

			int idx = bindProductFilters(ps, keyword, categoryIds, brandIds, minRating, 1);
			ps.setInt(idx++, offset);
			ps.setInt(idx, safeSize);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapRowList(rs));
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findProductsPaged error", e);
		}

		return list;
	}

    /* =========================================================
       ADMIN PRODUCT LIST
    ========================================================= */

	public List<Product> findProductsAdmin(String keyword, Integer categoryId, Integer brandId, String sort) {
		return findProductsAdmin(keyword, categoryId, brandId, null, null, sort);
	}

	public List<Product> findProductsAdmin(
			String keyword,
			Integer categoryId,
			Integer brandId,
			String activeStatus,
			String stockStatus,
			String sort
	) {
		List<Product> list = new ArrayList<>();

		StringBuilder sql = new StringBuilder(
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.created_at, p.is_active, " +
						"COALESCE(AVG(r.rating), 0) AS avg_rating, " +
						"COUNT(r.id) AS review_count, " +
						"c.id AS c_id, c.name AS c_name, " +
						"b.id AS b_id, b.name AS b_name " +
						"FROM store_product p " +
						"LEFT JOIN store_review r ON p.id = r.product_id " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"WHERE 1 = 1 "
		);

		if (keyword != null && !keyword.isBlank()) {
			sql.append("AND (p.title LIKE ? OR p.slug LIKE ? OR p.description LIKE ?) ");
		}

		if (categoryId != null && categoryId > 0) {
			sql.append("AND p.category_id = ? ");
		}

		if (brandId != null && brandId > 0) {
			sql.append("AND p.brand_id = ? ");
		}

		String normalizedActiveStatus = activeStatus == null
				? ""
				: activeStatus.trim().toLowerCase(Locale.ROOT);

		if ("active".equals(normalizedActiveStatus) || "1".equals(normalizedActiveStatus)) {
			sql.append("AND p.is_active = 1 ");
		} else if ("hidden".equals(normalizedActiveStatus)
				|| "inactive".equals(normalizedActiveStatus)
				|| "0".equals(normalizedActiveStatus)) {
			sql.append("AND p.is_active = 0 ");
		}

		String normalizedStockStatus = stockStatus == null
				? ""
				: stockStatus.trim().toLowerCase(Locale.ROOT);

		switch (normalizedStockStatus) {
			case "in_stock":
				sql.append("AND p.stock > 10 ");
				break;

			case "low_stock":
				sql.append("AND p.stock > 0 AND p.stock <= 10 ");
				break;

			case "out_stock":
			case "out_of_stock":
				sql.append("AND p.stock <= 0 ");
				break;

			default:
				break;
		}

		sql.append("GROUP BY p.id, p.title, p.slug, p.description, ")
				.append("p.price, p.discount_percent, p.stock, p.image, p.created_at, p.is_active, ")
				.append("c.id, c.name, b.id, b.name ");

		appendSort(sql, sort);

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql.toString())) {

			int idx = 1;

			if (keyword != null && !keyword.isBlank()) {
				String like = "%" + keyword.trim() + "%";
				ps.setString(idx++, like);
				ps.setString(idx++, like);
				ps.setString(idx++, like);
			}

			if (categoryId != null && categoryId > 0) {
				ps.setInt(idx++, categoryId);
			}

			if (brandId != null && brandId > 0) {
				ps.setInt(idx++, brandId);
			}

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Product p = mapRowList(rs);
					p.setActive(rs.getBoolean("is_active"));
					list.add(p);
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findProductsAdmin error", e);
		}

		return list;
	}

	public List<Product> findAll() {
		return findProductsAdmin(null, null, null, "created_desc");
	}

    /* =========================================================
       PRODUCT DETAIL
    ========================================================= */

	public Product findById(int productId) {
		String sql =
				"SELECT p.id, p.title, p.slug, p.description, p.price, p.discount_percent, " +
						"p.stock, p.image, p.is_active, p.created_at, " +
						"c.id AS c_id, c.name AS c_name, b.id AS b_id, b.name AS b_name " +
						"FROM store_product p " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"WHERE p.id = ? AND p.is_active = 1";

		return findProductDetail(sql, productId, "ProductDAO.findById error");
	}

	public Product findByIdAdmin(int productId) {
		String sql =
				"SELECT p.id, p.title, p.slug, p.description, p.price, p.discount_percent, " +
						"p.stock, p.image, p.is_active, p.created_at, " +
						"c.id AS c_id, c.name AS c_name, b.id AS b_id, b.name AS b_name " +
						"FROM store_product p " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"WHERE p.id = ?";

		return findProductDetail(sql, productId, "ProductDAO.findByIdAdmin error");
	}

	public Product findBySlug(String slug) {
		String sql =
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.is_active, p.created_at, " +
						"COALESCE(AVG(r.rating), 0) AS avg_rating, " +
						"COUNT(r.id) AS review_count, " +
						"c.id AS c_id, c.name AS c_name, " +
						"b.id AS b_id, b.name AS b_name " +
						"FROM store_product p " +
						"LEFT JOIN store_review r ON p.id = r.product_id " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"WHERE p.slug = ? AND p.is_active = 1 " +
						"GROUP BY p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.is_active, p.created_at, " +
						"c.id, c.name, b.id, b.name";

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, slug);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}

				Product p = mapRowList(rs);
				p.setActive(rs.getBoolean("is_active"));
				return p;
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findBySlug error", e);
		}
	}

    /* =========================================================
       SEARCH / SUGGESTION
    ========================================================= */

	public List<Product> searchByKeyword(String keyword) {
		List<Product> list = new ArrayList<>();

		String sql =
				"SELECT id, title, slug, price, image, " +
						"CASE " +
						"WHEN title LIKE ? THEN 0 " +
						"WHEN title LIKE ? THEN 1 " +
						"ELSE 2 " +
						"END AS relevance " +
						"FROM store_product " +
						"WHERE is_active = 1 " +
						"AND (title LIKE ? OR description LIKE ?) " +
						"ORDER BY relevance ASC, id DESC " +
						"LIMIT 8";

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql)) {

			String keywordTrim = keyword == null ? "" : keyword.trim();
			String like = "%" + keywordTrim + "%";

			ps.setString(1, like);
			ps.setString(2, keywordTrim + "%");
			ps.setString(3, like);
			ps.setString(4, like);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapRowSuggestion(rs));
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.searchByKeyword error", e);
		}

		return list;
	}

	public List<Product> searchSuggestions(String keyword) {
		List<Product> list = new ArrayList<>();

		String sql =
				"SELECT id, title, slug, price, image, " +
						"CASE " +
						"WHEN title LIKE ? THEN 0 " +
						"WHEN title LIKE ? THEN 1 " +
						"ELSE 2 " +
						"END AS score " +
						"FROM store_product " +
						"WHERE is_active = 1 " +
						"AND (title LIKE ? OR description LIKE ?) " +
						"ORDER BY score ASC, id DESC " +
						"LIMIT 8";

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql)) {

			String keywordTrim = keyword == null ? "" : keyword.trim();
			String likeAll = "%" + keywordTrim + "%";
			String likeStart = keywordTrim + "%";

			ps.setString(1, likeStart);
			ps.setString(2, likeAll);
			ps.setString(3, likeAll);
			ps.setString(4, likeAll);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapRowSuggestion(rs));
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.searchSuggestions error", e);
		}

		return list;
	}

    /* =========================================================
       CREATE / UPDATE / DELETE
    ========================================================= */

	public int create(Product p) {
		String sql =
				"INSERT INTO store_product " +
						"(title, name, slug, description, price, discount_percent, stock, image, " +
						"is_active, category_id, brand_id, created_at) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			String title = p.getTitle();

			ps.setString(1, title);
			ps.setString(2, title);
			ps.setString(3, p.getSlug());
			ps.setString(4, p.getDescription());
			ps.setBigDecimal(5, p.getPrice());
			ps.setInt(6, p.getDiscountPercent());
			ps.setInt(7, p.getStock());
			ps.setString(8, p.getImage());
			ps.setBoolean(9, p.isActive());
			ps.setObject(10, p.getCategory() != null ? p.getCategory().getId() : null);
			ps.setObject(11, p.getBrand() != null ? p.getBrand().getId() : null);

			ps.executeUpdate();

			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getInt(1);
				}

				return 0;
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.create error", e);
		}
	}

	public boolean update(Product p) {
		String sql =
				"UPDATE store_product SET title=?, name=?, slug=?, description=?, price=?, discount_percent=?, " +
						"stock=?, image=?, is_active=?, category_id=?, brand_id=? WHERE id=?";

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql)) {

			String title = p.getTitle();

			ps.setString(1, title);
			ps.setString(2, title);
			ps.setString(3, p.getSlug());
			ps.setString(4, p.getDescription());
			ps.setBigDecimal(5, p.getPrice());
			ps.setInt(6, p.getDiscountPercent());
			ps.setInt(7, p.getStock());
			ps.setString(8, p.getImage());
			ps.setBoolean(9, p.isActive());
			ps.setObject(10, p.getCategory() != null ? p.getCategory().getId() : null);
			ps.setObject(11, p.getBrand() != null ? p.getBrand().getId() : null);
			ps.setInt(12, p.getId());

			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.update error", e);
		}
	}

	public boolean updateActiveStatus(int productId, boolean active) {
		String sql = "UPDATE store_product SET is_active = ? WHERE id = ?";

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setBoolean(1, active);
			ps.setInt(2, productId);

			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.updateActiveStatus error", e);
		}
	}

	/*
	 * Xóa sản phẩm an toàn:
	 * - Nếu sản phẩm đã có trong store_orderitem: chỉ ẩn sản phẩm bằng is_active = 0.
	 * - Nếu sản phẩm chưa có đơn hàng: xóa cứng trong transaction.
	 *
	 * Issue 123:
	 * - Khi xóa cứng sản phẩm, xóa thêm store_productmedia để tránh lỗi khóa ngoại.
	 */
	public DeleteMode deleteOrDeactivateSafely(int productId) {
		String checkProductSql = "SELECT id FROM store_product WHERE id = ?";
		String checkOrderItemSql = "SELECT 1 FROM store_orderitem WHERE product_id = ? LIMIT 1";

		String softDeleteSql = "UPDATE store_product SET is_active = 0 WHERE id = ?";

		String deleteCartItemsSql = "DELETE FROM cart_items WHERE product_id = ?";
		String deleteReviewsSql = "DELETE FROM store_review WHERE product_id = ?";
		String deleteImagesSql = "DELETE FROM store_productimage WHERE product_id = ?";

		// Issue 123: xóa media chi tiết ảnh/video của sản phẩm
		String deleteProductMediaSql = "DELETE FROM store_productmedia WHERE product_id = ?";

		String deleteProductDiscountSql = "DELETE FROM store_productdiscount WHERE product_id = ?";

		/*
		 * Các bảng target khuyến mãi mới.
		 * Xóa trước product để tránh lỗi khóa ngoại khi hard delete.
		 */
		String deleteCouponProductSql = "DELETE FROM store_coupon_product WHERE product_id = ?";
		String deleteBrandDiscountProductSql = "DELETE FROM store_branddiscount_product WHERE product_id = ?";
		String deletePromotionEventProductSql = "DELETE FROM store_promotionevent_product WHERE product_id = ?";

		String deleteProductSql = "DELETE FROM store_product WHERE id = ?";

		try (Connection conn = DBConnection.getConnection()) {
			conn.setAutoCommit(false);

			try {
				boolean productExists;

				try (PreparedStatement ps = conn.prepareStatement(checkProductSql)) {
					ps.setInt(1, productId);

					try (ResultSet rs = ps.executeQuery()) {
						productExists = rs.next();
					}
				}

				if (!productExists) {
					conn.rollback();
					return DeleteMode.NOT_FOUND;
				}

				boolean hasOrderItem;

				try (PreparedStatement ps = conn.prepareStatement(checkOrderItemSql)) {
					ps.setInt(1, productId);

					try (ResultSet rs = ps.executeQuery()) {
						hasOrderItem = rs.next();
					}
				}

				if (hasOrderItem) {
					try (PreparedStatement ps = conn.prepareStatement(softDeleteSql)) {
						ps.setInt(1, productId);
						ps.executeUpdate();
					}

					conn.commit();
					return DeleteMode.SOFT_DELETED;
				}

				executeDeleteByProductId(conn, deleteCartItemsSql, productId);
				executeDeleteByProductId(conn, deleteReviewsSql, productId);
				executeDeleteByProductId(conn, deleteImagesSql, productId);

				// Issue 123: xóa media ảnh/video trước khi xóa sản phẩm
				executeDeleteByProductId(conn, deleteProductMediaSql, productId);

				executeDeleteByProductId(conn, deleteProductDiscountSql, productId);

				executeDeleteByProductId(conn, deleteCouponProductSql, productId);
				executeDeleteByProductId(conn, deleteBrandDiscountProductSql, productId);
				executeDeleteByProductId(conn, deletePromotionEventProductSql, productId);

				try (PreparedStatement ps = conn.prepareStatement(deleteProductSql)) {
					ps.setInt(1, productId);
					ps.executeUpdate();
				}

				conn.commit();
				return DeleteMode.HARD_DELETED;

			} catch (Exception e) {
				conn.rollback();
				throw e;

			} finally {
				conn.setAutoCommit(true);
			}

		} catch (Exception e) {
			throw new RuntimeException("ProductDAO.deleteOrDeactivateSafely error", e);
		}
	}

	public boolean delete(int id) {
		String sql = "DELETE FROM store_product WHERE id = ?";

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, id);
			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			if (e.getErrorCode() == 1451) {
				throw new RuntimeException("Không thể xóa sản phẩm vì đang được tham chiếu.", e);
			}

			throw new RuntimeException("ProductDAO.delete error", e);
		}
	}

	public int deleteReviewsByProductId(int productId) {
		String sql = "DELETE FROM store_review WHERE product_id = ?";

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, productId);
			return ps.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.deleteReviewsByProductId error", e);
		}
	}

	private void executeDeleteByProductId(Connection conn, String sql, int productId) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, productId);
			ps.executeUpdate();
		}
	}

    /* =========================================================
       HOME FEATURED PRODUCTS
    ========================================================= */

	public List<Product> findHomeDiscoverProducts() {
		List<Product> list = new ArrayList<>();

		String sql =
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.created_at, " +
						"0 AS avg_rating, " +
						"0 AS review_count, " +
						"c.id AS c_id, c.name AS c_name, " +
						"b.id AS b_id, b.name AS b_name, " +
						"CASE p.slug " +
						"WHEN 'hop-qua-yeu-thuong-son-tint-li-romand-the-juicy-lasting-tint-solunar-phan-ma-hong-judydoll' THEN 1 " +
						"WHEN 'hop-qua-yeu-thuong-son-tint-bong-merzy-the-watery-dew-tint-son-kem-li-merzy' THEN 0 " +
						"WHEN 'hop-qua-yeu-thuong-son-bong-pha-le-merzy-bisous-glowy-gel-tint-son-phao-merzy' THEN 0 " +
						"WHEN 'hop-qua-yeu-thuong-son-tint-bong-foif-jelly-lasting-tint-son-tint-bong-foif-juicy-fit' THEN 18 " +
						"WHEN 'hop-qua-yeu-thuong-2-son-tint-bong-foif-juicy-fit-tint-vecci-eyeliner-tui-foif' THEN 3 " +
						"WHEN 'hop-qua-yeu-thuong-sua-rua-mat-svr-sebiaclear-gel-moussant' THEN 22 " +
						"WHEN 'hop-qua-yeu-thuong-sua-rua-mat-svr-topialyse-gel-lavant' THEN 17 " +
						"WHEN 'hop-qua-yeu-thuong-gel-rua-mat-svr-moussante-gel-biore-bnbg' THEN 30 " +
						"WHEN 'bam-mi-than-xam-vacosi-full-lash-curler-bm02' THEN 3800 " +
						"WHEN 'mat-na-mieng-bnbg-duong-am-ho-tro-duong-trang-cang-bong-da' THEN 322800 " +
						"WHEN 'chi-ke-may-innisfree-ngang-manh-lau-troi-auto-eyebrow-pencil' THEN 45500 " +
						"ELSE 0 END AS sold_qty, " +
						"CASE p.slug " +
						"WHEN 'mat-na-mieng-bnbg-duong-am-ho-tro-duong-trang-cang-bong-da' THEN 322800 " +
						"WHEN 'chi-ke-may-innisfree-ngang-manh-lau-troi-auto-eyebrow-pencil' THEN 45500 " +
						"ELSE 0 END AS view_count " +
						"FROM store_product p " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"WHERE p.is_active = 1 " +
						"AND p.slug IN (" +
						"'hop-qua-yeu-thuong-son-tint-li-romand-the-juicy-lasting-tint-solunar-phan-ma-hong-judydoll'," +
						"'hop-qua-yeu-thuong-son-tint-bong-merzy-the-watery-dew-tint-son-kem-li-merzy'," +
						"'hop-qua-yeu-thuong-son-bong-pha-le-merzy-bisous-glowy-gel-tint-son-phao-merzy'," +
						"'hop-qua-yeu-thuong-son-tint-bong-foif-jelly-lasting-tint-son-tint-bong-foif-juicy-fit'," +
						"'hop-qua-yeu-thuong-2-son-tint-bong-foif-juicy-fit-tint-vecci-eyeliner-tui-foif'," +
						"'hop-qua-yeu-thuong-sua-rua-mat-svr-sebiaclear-gel-moussant'," +
						"'hop-qua-yeu-thuong-sua-rua-mat-svr-topialyse-gel-lavant'," +
						"'hop-qua-yeu-thuong-gel-rua-mat-svr-moussante-gel-biore-bnbg'," +
						"'bam-mi-than-xam-vacosi-full-lash-curler-bm02'," +
						"'mat-na-mieng-bnbg-duong-am-ho-tro-duong-trang-cang-bong-da'," +
						"'chi-ke-may-innisfree-ngang-manh-lau-troi-auto-eyebrow-pencil'" +
						") " +
						"ORDER BY FIELD(p.slug, " +
						"'hop-qua-yeu-thuong-son-tint-li-romand-the-juicy-lasting-tint-solunar-phan-ma-hong-judydoll'," +
						"'hop-qua-yeu-thuong-son-tint-bong-merzy-the-watery-dew-tint-son-kem-li-merzy'," +
						"'hop-qua-yeu-thuong-son-bong-pha-le-merzy-bisous-glowy-gel-tint-son-phao-merzy'," +
						"'hop-qua-yeu-thuong-son-tint-bong-foif-jelly-lasting-tint-son-tint-bong-foif-juicy-fit'," +
						"'hop-qua-yeu-thuong-2-son-tint-bong-foif-juicy-fit-tint-vecci-eyeliner-tui-foif'," +
						"'hop-qua-yeu-thuong-sua-rua-mat-svr-sebiaclear-gel-moussant'," +
						"'hop-qua-yeu-thuong-sua-rua-mat-svr-topialyse-gel-lavant'," +
						"'hop-qua-yeu-thuong-gel-rua-mat-svr-moussante-gel-biore-bnbg'," +
						"'bam-mi-than-xam-vacosi-full-lash-curler-bm02'," +
						"'mat-na-mieng-bnbg-duong-am-ho-tro-duong-trang-cang-bong-da'," +
						"'chi-ke-may-innisfree-ngang-manh-lau-troi-auto-eyebrow-pencil'" +
						")";

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql);
		     ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				list.add(mapRowList(rs));
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findHomeDiscoverProducts error", e);
		}

		return list;
	}

	public List<Product> findFeaturedTop12BestSellerDeepDiscount() {
		List<Product> list = new ArrayList<>();

		String sql =
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.created_at, " +
						"COALESCE(rv.avg_rating, 0) AS avg_rating, " +
						"COALESCE(rv.review_count, 0) AS review_count, " +
						"c.id AS c_id, c.name AS c_name, " +
						"b.id AS b_id, b.name AS b_name, " +
						"COALESCE(sd.sold_qty, 0) AS sold_qty " +
						"FROM store_product p " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"LEFT JOIN ( " +
						"SELECT product_id, AVG(rating) AS avg_rating, COUNT(*) AS review_count " +
						"FROM store_review GROUP BY product_id " +
						") rv ON rv.product_id = p.id " +
						"LEFT JOIN ( " +
						"SELECT oi.product_id, SUM(oi.quantity) AS sold_qty " +
						"FROM store_orderitem oi " +
						"JOIN store_order o ON o.id = oi.order_id " +
						"WHERE o.payment_status = 'PAID' " +
						"GROUP BY oi.product_id " +
						") sd ON sd.product_id = p.id " +
						"WHERE p.is_active = 1 " +
						"ORDER BY COALESCE(sd.sold_qty, 0) DESC, p.discount_percent DESC, p.created_at DESC " +
						"LIMIT 12";

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql);
		     ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				list.add(mapRowList(rs));
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findFeaturedTop12BestSellerDeepDiscount error", e);
		}

		return list;
	}

	public List<Product> findFeaturedTop12DeepDiscount() {
		List<Product> list = new ArrayList<>();

		String sql =
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.created_at, " +
						"COALESCE(AVG(r.rating), 0) AS avg_rating, " +
						"COUNT(r.id) AS review_count, " +
						"c.id AS c_id, c.name AS c_name, " +
						"b.id AS b_id, b.name AS b_name " +
						"FROM store_product p " +
						"LEFT JOIN store_review r ON p.id = r.product_id " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"WHERE p.is_active = 1 " +
						"GROUP BY p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.created_at, " +
						"c.id, c.name, b.id, b.name " +
						"ORDER BY p.discount_percent DESC, COALESCE(AVG(r.rating), 0) DESC, COUNT(r.id) DESC, p.created_at DESC " +
						"LIMIT 12";

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql);
		     ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				list.add(mapRowList(rs));
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findFeaturedTop12DeepDiscount error", e);
		}

		return list;
	}


	public List<Product> findHomeBestSellingProducts(int limit) {
		List<Product> list = new ArrayList<>();
		int safeLimit = limit > 0 ? limit : 12;

		String sql =
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.created_at, " +
						"COALESCE(rv.avg_rating, 0) AS avg_rating, " +
						"COALESCE(rv.review_count, 0) AS review_count, " +
						"c.id AS c_id, c.name AS c_name, " +
						"b.id AS b_id, b.name AS b_name, " +
						"COALESCE(sd.sold_qty, 0) AS sold_qty " +
						"FROM store_product p " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"LEFT JOIN ( " +
						"SELECT product_id, AVG(rating) AS avg_rating, COUNT(*) AS review_count " +
						"FROM store_review GROUP BY product_id " +
						") rv ON rv.product_id = p.id " +
						"LEFT JOIN ( " +
						"SELECT oi.product_id, SUM(oi.quantity) AS sold_qty " +
						"FROM store_orderitem oi " +
						"JOIN store_order o ON o.id = oi.order_id " +
						"WHERE o.payment_status = 'PAID' " +
						"GROUP BY oi.product_id " +
						") sd ON sd.product_id = p.id " +
						"WHERE p.is_active = 1 " +
						"ORDER BY COALESCE(sd.sold_qty, 0) DESC, p.discount_percent DESC, p.created_at DESC, p.id DESC " +
						"LIMIT ?";

		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, safeLimit);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Product product = mapRowList(rs);

					/*
					 * Nếu database chưa có dữ liệu đơn hàng PAID, tạo lượt bán ảo giảm dần
					 * để section Bán chạy vẫn có số liệu hiển thị ổn định trên trang chủ.
					 */
					if (product.getSoldQuantity() <= 0) {
						product.setSoldQuantity(Math.max(1, 300 - list.size() * 17));
					}

					list.add(product);
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findHomeBestSellingProducts error", e);
		}

		return list;
	}




	public List<Product> findFeaturedProductsByBrandIds(List<Integer> brandIds, int limit) {
		List<Product> list = new ArrayList<>();

		if (brandIds == null || brandIds.isEmpty()) {
			return list;
		}

		int perBrandLimit = limit > 0 ? limit : 9;

		String sql =
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.created_at, " +
						"COALESCE(rv.avg_rating, 0) AS avg_rating, " +
						"COALESCE(rv.review_count, 0) AS review_count, " +
						"c.id AS c_id, c.name AS c_name, " +
						"b.id AS b_id, b.name AS b_name, " +
						"COALESCE(sd.sold_qty, 0) AS sold_qty " +
						"FROM store_product p " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"LEFT JOIN ( " +
						"SELECT product_id, AVG(rating) AS avg_rating, COUNT(*) AS review_count " +
						"FROM store_review GROUP BY product_id " +
						") rv ON rv.product_id = p.id " +
						"LEFT JOIN ( " +
						"SELECT oi.product_id, SUM(oi.quantity) AS sold_qty " +
						"FROM store_orderitem oi " +
						"JOIN store_order o ON o.id = oi.order_id " +
						"WHERE o.payment_status = 'PAID' " +
						"GROUP BY oi.product_id " +
						") sd ON sd.product_id = p.id " +
						"WHERE p.is_active = 1 " +
						"AND p.brand_id = ? " +
						"ORDER BY COALESCE(sd.sold_qty, 0) DESC, p.discount_percent DESC, p.created_at DESC " +
						"LIMIT ?";

		try (Connection c = DBConnection.getConnection()) {
			for (Integer brandId : brandIds) {
				if (brandId == null || brandId <= 0) {
					continue;
				}

				try (PreparedStatement ps = c.prepareStatement(sql)) {
					ps.setInt(1, brandId);
					ps.setInt(2, perBrandLimit);

					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							list.add(mapRowList(rs));
						}
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findFeaturedProductsByBrandIds error", e);
		}

		return list;
	}


    /* =========================================================
       PROMOTION PRODUCT PICKER
       Chỉ dùng cho admin/promotions, không ảnh hưởng /products
    ========================================================= */

	public List<Product> findForPromotionPicker(String keyword, Integer brandId, Integer categoryId) {
		return findForPromotionPicker(keyword, brandId, categoryId, true, new ArrayList<>());
	}

	public List<Product> findForPromotionPicker(
			String keyword,
			Integer brandId,
			Integer categoryId,
			List<Integer> selectedProductIds
	) {
		return findForPromotionPicker(keyword, brandId, categoryId, true, selectedProductIds);
	}

	public List<Product> findForPromotionPicker(
			String keyword,
			Integer brandId,
			Integer categoryId,
			boolean activeOnly,
			List<Integer> selectedProductIds
	) {
		List<Product> products = new ArrayList<>();
		Set<Integer> selectedIds = new LinkedHashSet<>();

		if (selectedProductIds != null) {
			selectedIds.addAll(selectedProductIds);
		}

		StringBuilder sql = new StringBuilder(
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.created_at, p.is_active, " +
						"0 AS avg_rating, " +
						"0 AS review_count, " +
						"c.id AS c_id, c.name AS c_name, " +
						"b.id AS b_id, b.name AS b_name " +
						"FROM store_product p " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"WHERE 1 = 1 "
		);

		if (activeOnly) {
			sql.append("AND p.is_active = 1 ");
		}

		if (keyword != null && !keyword.isBlank()) {
			sql.append("AND (p.title LIKE ? OR p.slug LIKE ? OR p.description LIKE ?) ");
		}

		if (brandId != null && brandId > 0) {
			sql.append("AND p.brand_id = ? ");
		}

		if (categoryId != null && categoryId > 0) {
			sql.append("AND p.category_id = ? ");
		}

		sql.append("ORDER BY p.title ASC, p.id DESC");

		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql.toString())) {

			int idx = 1;

			if (keyword != null && !keyword.isBlank()) {
				String like = "%" + keyword.trim() + "%";
				ps.setString(idx++, like);
				ps.setString(idx++, like);
				ps.setString(idx++, like);
			}

			if (brandId != null && brandId > 0) {
				ps.setInt(idx++, brandId);
			}

			if (categoryId != null && categoryId > 0) {
				ps.setInt(idx, categoryId);
			}

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Product product = mapRowList(rs);
					product.setActive(rs.getBoolean("is_active"));
					product.setSelectedForPromotion(selectedIds.contains(product.getId()));
					products.add(product);
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findForPromotionPicker error", e);
		}

		return products;
	}

	public List<Product> findByIds(List<Integer> productIds) {
		List<Product> products = new ArrayList<>();
		List<Integer> cleanedIds = normalizeProductIds(productIds);

		if (cleanedIds.isEmpty()) {
			return products;
		}

		String sql =
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.created_at, p.is_active, " +
						"0 AS avg_rating, " +
						"0 AS review_count, " +
						"c.id AS c_id, c.name AS c_name, " +
						"b.id AS b_id, b.name AS b_name " +
						"FROM store_product p " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"WHERE p.id IN (" + placeholders(cleanedIds.size()) + ") " +
						"ORDER BY p.title ASC, p.id DESC";

		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {

			int idx = 1;
			for (Integer productId : cleanedIds) {
				ps.setInt(idx++, productId);
			}

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Product product = mapRowList(rs);
					product.setActive(rs.getBoolean("is_active"));
					products.add(product);
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findByIds error", e);
		}

		return products;
	}

	public List<Product> findActiveByBrandId(int brandId) {
		if (brandId <= 0) {
			return new ArrayList<>();
		}

		return findForPromotionPicker(null, brandId, null, true, new ArrayList<>());
	}

	public List<Product> findActiveByCategoryId(int categoryId) {
		if (categoryId <= 0) {
			return new ArrayList<>();
		}

		return findForPromotionPicker(null, null, categoryId, true, new ArrayList<>());
	}

    /* =========================================================
       PRIVATE QUERY HELPERS
    ========================================================= */

	private Product findProductDetail(String sql, int productId, String errorMessage) {
		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, productId);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}

				return mapRowDetail(rs);
			}

		} catch (SQLException e) {
			throw new RuntimeException(errorMessage, e);
		}
	}

	private int bindProductFilters(
			PreparedStatement ps,
			String keyword,
			List<Integer> categoryIds,
			List<Integer> brandIds,
			Integer minRating,
			int idx
	) throws SQLException {

		if (keyword != null && !keyword.isBlank()) {
			String like = "%" + keyword.trim() + "%";
			ps.setString(idx++, like);
			ps.setString(idx++, like);
		}

		if (categoryIds != null && !categoryIds.isEmpty()) {
			for (Integer categoryId : categoryIds) {
				if (categoryId != null) {
					ps.setInt(idx++, categoryId);
				}
			}
		}

		if (brandIds != null && !brandIds.isEmpty()) {
			for (Integer brandId : brandIds) {
				if (brandId != null) {
					ps.setInt(idx++, brandId);
				}
			}
		}

		if (minRating != null) {
			ps.setInt(idx++, minRating);
		}

		return idx;
	}

	private void appendPriceRanges(StringBuilder sql, List<String> priceRanges) {
		if (priceRanges == null || priceRanges.isEmpty()) {
			return;
		}

		List<String> conditions = new ArrayList<>();

		for (String priceRange : priceRanges) {
			if (priceRange == null || priceRange.isBlank()) {
				continue;
			}

			switch (priceRange.trim()) {
				case "lt500":
					conditions.add("p.price < 500000");
					break;

				case "500_1000":
					conditions.add("p.price BETWEEN 500000 AND 1000000");
					break;

				case "gt1000":
					conditions.add("p.price > 1000000");
					break;

				default:
					break;
			}
		}

		if (!conditions.isEmpty()) {
			sql.append("AND (")
					.append(String.join(" OR ", conditions))
					.append(") ");
		}
	}

	private String placeholders(int size) {
		if (size <= 0) {
			return "";
		}

		return String.join(",", java.util.Collections.nCopies(size, "?"));
	}

    /* =========================================================
       MAPPERS
    ========================================================= */

	private Product mapRowList(ResultSet rs) throws SQLException {
		Product p = new Product();

		p.setId(rs.getInt("id"));
		p.setTitle(rs.getString("title"));
		p.setSlug(rs.getString("slug"));
		p.setDescription(rs.getString("description"));
		p.setPrice(rs.getBigDecimal("price"));
		p.setDiscountPercent(rs.getInt("discount_percent"));
		p.setStock(rs.getInt("stock"));
		p.setImage(rs.getString("image"));

		if (hasColumn(rs, "is_active")) {
			p.setActive(rs.getBoolean("is_active"));
		} else {
			p.setActive(true);
		}

		if (hasColumn(rs, "avg_rating")) {
			p.setAvgRating(rs.getDouble("avg_rating"));
		}

		if (hasColumn(rs, "review_count")) {
			p.setReviewCount(rs.getInt("review_count"));
		}

		if (hasColumn(rs, "sold_qty")) {
			p.setSoldQuantity(rs.getInt("sold_qty"));
		}

		if (hasColumn(rs, "view_count")) {
			p.setViewCount(rs.getInt("view_count"));
		}

		applyFinalPrice(p);

		if (rs.getObject("c_id") != null) {
			Category cat = new Category();
			cat.setId(rs.getInt("c_id"));
			cat.setName(rs.getString("c_name"));
			p.setCategory(cat);
			p.setCategoryId(rs.getInt("c_id"));
			p.setCategoryName(rs.getString("c_name"));
		}

		if (rs.getObject("b_id") != null) {
			Brand br = new Brand();
			br.setId(rs.getInt("b_id"));
			br.setName(rs.getString("b_name"));
			p.setBrand(br);
			p.setBrandId(rs.getInt("b_id"));
			p.setBrandName(rs.getString("b_name"));
		}

		return p;
	}

	private Product mapRowDetail(ResultSet rs) throws SQLException {
		Product p = new Product();

		p.setId(rs.getInt("id"));
		p.setTitle(rs.getString("title"));
		p.setSlug(rs.getString("slug"));
		p.setDescription(rs.getString("description"));
		p.setPrice(rs.getBigDecimal("price"));
		p.setDiscountPercent(rs.getInt("discount_percent"));
		p.setStock(rs.getInt("stock"));
		p.setImage(rs.getString("image"));
		p.setActive(rs.getBoolean("is_active"));

		applyFinalPrice(p);

		if (rs.getObject("c_id") != null) {
			Category cat = new Category();
			cat.setId(rs.getInt("c_id"));
			cat.setName(rs.getString("c_name"));
			p.setCategory(cat);
			p.setCategoryId(rs.getInt("c_id"));
			p.setCategoryName(rs.getString("c_name"));
		}

		if (rs.getObject("b_id") != null) {
			Brand br = new Brand();
			br.setId(rs.getInt("b_id"));
			br.setName(rs.getString("b_name"));
			p.setBrand(br);
			p.setBrandId(rs.getInt("b_id"));
			p.setBrandName(rs.getString("b_name"));
		}

		return p;
	}

	private Product mapRowSuggestion(ResultSet rs) throws SQLException {
		Product p = new Product();

		p.setId(rs.getInt("id"));
		p.setTitle(rs.getString("title"));
		p.setSlug(rs.getString("slug"));
		p.setPrice(rs.getBigDecimal("price"));
		p.setImage(rs.getString("image"));

		return p;
	}

	private void applyFinalPrice(Product p) {
		if (p == null || p.getPrice() == null) {
			return;
		}

		int discountPercent = p.getDiscountPercent();

		if (discountPercent <= 0) {
			p.setFinalPrice(p.getPrice());
			return;
		}

		BigDecimal hundred = new BigDecimal("100");
		BigDecimal rate = hundred.subtract(BigDecimal.valueOf(discountPercent));
		BigDecimal finalPrice = p.getPrice().multiply(rate).divide(hundred);

		p.setFinalPrice(finalPrice);
	}

	private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();

		for (int i = 1; i <= columnCount; i++) {
			if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
				return true;
			}
		}

		return false;
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

	public List<Product> findProductsByPromotion(PromotionEvent promotion) {
		if (promotion == null) {
			return Collections.emptyList();
		}

		PromotionEvent.Scope scope = promotion.getScope();

		if (scope == null) {
			return Collections.emptyList();
		}

		List<Integer> categoryIds = Collections.emptyList();
		List<Integer> brandIds = Collections.emptyList();
		List<String> priceRanges = Collections.emptyList();

		switch (scope) {
			case ALL:
				break;

			case CATEGORY:
				if (promotion.getCategoryId() == null || promotion.getCategoryId() <= 0) {
					return Collections.emptyList();
				}

				categoryIds = Collections.singletonList(promotion.getCategoryId());
				break;

			case BRAND:
				if (promotion.getBrandId() == null || promotion.getBrandId() <= 0) {
					return Collections.emptyList();
				}

				brandIds = Collections.singletonList(promotion.getBrandId());
				break;

			case PRODUCTS:
				return findByIds(promotion.getSelectedProductIds());

			default:
				return Collections.emptyList();
		}

		return findProductsPaged(
				null,           // keyword
				categoryIds,    // categoryIds
				brandIds,       // brandIds
				"created_desc", // sort
				priceRanges,    // priceRanges
				null,           // minRating
				1,              // page
				12              // pageSize
		);
	}

	/**
	 * Lấy danh sách sản phẩm theo danh sách ID và giữ nguyên thứ tự ID truyền vào.
	 * Dùng cho các màn hình cần hiển thị sản phẩm theo thứ tự seed/danh mục cố định.
	 */
	public List<Product> findByIdsOrdered(List<Integer> ids) {
		List<Product> products = new ArrayList<>();
		List<Integer> cleanedIds = normalizeProductIds(ids);

		if (cleanedIds.isEmpty()) {
			return products;
		}

		String inPlaceholders = placeholders(cleanedIds.size());
		String fieldPlaceholders = placeholders(cleanedIds.size());

		String sql =
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.created_at, p.is_active, " +
						"0 AS avg_rating, " +
						"0 AS review_count, " +
						"c.id AS c_id, c.name AS c_name, " +
						"b.id AS b_id, b.name AS b_name " +
						"FROM store_product p " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"WHERE p.id IN (" + inPlaceholders + ") " +
						"ORDER BY FIELD(p.id, " + fieldPlaceholders + ")";

		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {

			int idx = 1;

			for (Integer id : cleanedIds) {
				ps.setInt(idx++, id);
			}

			for (Integer id : cleanedIds) {
				ps.setInt(idx++, id);
			}

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Product product = mapRowList(rs);
					product.setActive(rs.getBoolean("is_active"));
					products.add(product);
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findByIdsOrdered error", e);
		}

		return products;
	}

	/**
	 * Lấy sản phẩm liên quan cùng danh mục.
	 */
	public List<Product> findRelatedByCategory(int categoryId, int excludeId, int limit) {
		if (categoryId <= 0 || limit <= 0) {
			return new ArrayList<>();
		}

		List<Product> products = new ArrayList<>();

		String sql =
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.created_at, p.is_active, " +
						"0 AS avg_rating, " +
						"0 AS review_count, " +
						"c.id AS c_id, c.name AS c_name, " +
						"b.id AS b_id, b.name AS b_name " +
						"FROM store_product p " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"WHERE p.category_id = ? " +
						"AND p.id <> ? " +
						"AND p.stock > 0 " +
						"AND p.is_active = 1 " +
						"ORDER BY p.created_at DESC, p.id DESC " +
						"LIMIT ?";

		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, categoryId);
			ps.setInt(2, excludeId);
			ps.setInt(3, limit);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Product product = mapRowList(rs);
					product.setActive(rs.getBoolean("is_active"));
					products.add(product);
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findRelatedByCategory error", e);
		}

		return products;
	}

	/**
	 * Lấy sản phẩm liên quan dựa theo tag tương đồng.
	 *
	 * Lưu ý:
	 * - Một số database cũ chưa có bảng store_product_tag.
	 * - Trang chi tiết sản phẩm không được lỗi 500 chỉ vì thiếu bảng tag.
	 * - Nếu thiếu bảng tag, trả về danh sách rỗng để RecommendationService dùng sản phẩm cùng danh mục.
	 */
	public List<Product> findRelatedByTag(int productId, int limit) {
		if (productId <= 0 || limit <= 0) {
			return new ArrayList<>();
		}

		if (!tableExists("store_product_tag")) {
			return new ArrayList<>();
		}

		List<Product> products = new ArrayList<>();

		String sql =
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.created_at, p.is_active, " +
						"0 AS avg_rating, " +
						"0 AS review_count, " +
						"c.id AS c_id, c.name AS c_name, " +
						"b.id AS b_id, b.name AS b_name, " +
						"COUNT(*) AS tag_score " +
						"FROM store_product p " +
						"JOIN store_product_tag pt ON p.id = pt.product_id " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"WHERE pt.tag_id IN ( " +
						"SELECT tag_id FROM store_product_tag WHERE product_id = ? " +
						") " +
						"AND p.id <> ? " +
						"AND p.stock > 0 " +
						"AND p.is_active = 1 " +
						"GROUP BY p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.created_at, p.is_active, " +
						"c.id, c.name, b.id, b.name " +
						"ORDER BY tag_score DESC, p.created_at DESC, p.id DESC " +
						"LIMIT ?";

		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, productId);
			ps.setInt(2, productId);
			ps.setInt(3, limit);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Product product = mapRowList(rs);
					product.setActive(rs.getBoolean("is_active"));
					products.add(product);
				}
			}

		} catch (SQLException e) {
			if (isMissingTableError(e, "store_product_tag")) {
				return new ArrayList<>();
			}
			throw new RuntimeException("ProductDAO.findRelatedByTag error", e);
		}

		return products;
	}

	private boolean tableExists(String tableName) {
		String sql =
				"SELECT COUNT(*) " +
						"FROM information_schema.tables " +
						"WHERE table_schema = DATABASE() " +
						"AND table_name = ?";

		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, tableName);

			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}

		} catch (SQLException e) {
			return false;
		}
	}

	private boolean isMissingTableError(SQLException e, String tableName) {
		String message = e.getMessage();
		return message != null
				&& message.toLowerCase(Locale.ROOT).contains(tableName.toLowerCase(Locale.ROOT))
				&& (message.toLowerCase(Locale.ROOT).contains("doesn't exist")
				|| message.toLowerCase(Locale.ROOT).contains("does not exist")
				|| message.toLowerCase(Locale.ROOT).contains("unknown table"));
	}

	private void appendSort(StringBuilder sql, String sort) {
		if (sort == null || sort.isBlank()) {
			sql.append("ORDER BY p.id DESC "); // Sắp xếp mặc định khi không truyền tham số
			return;
		}

		switch (sort.trim()) {
			case "price_asc":
				sql.append("ORDER BY p.price ASC ");
				break;

			case "price_desc":
				sql.append("ORDER BY p.price DESC ");
				break;

			case "stock_asc":
				sql.append("ORDER BY p.stock ASC, p.id DESC ");
				break;

			case "stock_desc":
				sql.append("ORDER BY p.stock DESC, p.id DESC ");
				break;

			case "name_asc":
				sql.append("ORDER BY p.title ASC, p.id DESC ");
				break;

			case "name_desc":
				sql.append("ORDER BY p.title DESC, p.id DESC ");
				break;

			case "rating_desc":
				sql.append("ORDER BY COALESCE(AVG(r.rating), 0) DESC ");
				break;

			case "best-selling":
			case "best_selling":
				/*
				 * Sử dụng Subquery để tính tổng số lượng bán (SUM(oi.quantity))
				 * từ các đơn hàng có trạng thái thanh toán là 'PAID' (Đã thanh toán).
				 * Sản phẩm nào bán nhiều nhất sẽ được sắp xếp lên đầu (DESC).
				 */
				sql.append("ORDER BY (SELECT COALESCE(SUM(oi.quantity), 0) FROM store_orderitem oi ")
						.append("JOIN store_order o ON o.id = oi.order_id ")
						.append("WHERE o.payment_status = 'PAID' AND oi.product_id = p.id) DESC ");
				break;

			case "updated_desc":
			case "created_desc":
			case "newest":
				/*
				 * Sắp xếp theo thời gian mới nhất.
				 * - Nếu trong Database của bạn có cột 'updated_at' (ngày cập nhật), hãy đổi thành: p.updated_at DESC
				 * - Nếu không có, sử dụng cột 'created_at' (ngày tạo) có sẵn trong câu lệnh SELECT của bạn: p.created_at DESC
				 */
				sql.append("ORDER BY p.created_at DESC ");
				break;

			default:
				sql.append("ORDER BY p.id DESC ");
				break;
		}
	}
}

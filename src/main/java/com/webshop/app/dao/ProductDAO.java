package com.webshop.app.dao;

import com.webshop.app.model.Brand;
import com.webshop.app.model.Category;
import com.webshop.app.model.Product;
import com.webshop.app.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ProductDAO {

	public enum DeleteMode {
		HARD_DELETED,
		SOFT_DELETED,
		NOT_FOUND
	}

	public List<Product> findProducts(String keyword, List<Integer> categoryIds, List<Integer> brandIds, String sort,
	                                  List<String> priceRanges, Integer minRating) {

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
			sql.append("AND p.category_id IN (").append(placeholders(categoryIds.size())).append(") ");
		}
		if (brandIds != null && !brandIds.isEmpty()) {
			sql.append("AND p.brand_id IN (").append(placeholders(brandIds.size())).append(") ");
		}

		appendPriceRange(sql, priceRanges);

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

	public int countProducts(String keyword, List<Integer> categoryIds, List<Integer> brandIds, List<String> priceRanges,
	                         Integer minRating) {

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
			sql.append("AND p.category_id IN (").append(placeholders(categoryIds.size())).append(") ");
		}
		if (brandIds != null && !brandIds.isEmpty()) {
			sql.append("AND p.brand_id IN (").append(placeholders(brandIds.size())).append(") ");
		}

		appendPriceRange(sql, priceRanges);

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

	public List<Product> findProductsPaged(String keyword, List<Integer> categoryIds, List<Integer> brandIds, String sort,
	                                       List<String> priceRanges, Integer minRating, int page, int pageSize) {

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
			sql.append("AND p.category_id IN (").append(placeholders(categoryIds.size())).append(") ");
		}
		if (brandIds != null && !brandIds.isEmpty()) {
			sql.append("AND p.brand_id IN (").append(placeholders(brandIds.size())).append(") ");
		}

		appendPriceRange(sql, priceRanges);

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

	public List<Product> findProductsAdmin(String keyword, Integer categoryId, Integer brandId, String sort) {

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
		if (categoryId != null) sql.append("AND p.category_id = ? ");
		if (brandId != null) sql.append("AND p.brand_id = ? ");

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

			if (categoryId != null) ps.setInt(idx++, categoryId);
			if (brandId != null) ps.setInt(idx, brandId);

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
				"SELECT p.id, p.title, p.slug, p.description, p.price, p.discount_percent, " +
						"p.stock, p.image, p.is_active, p.created_at, " +
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
				if (!rs.next()) return null;

				Product p = mapRowList(rs);
				p.setActive(rs.getBoolean("is_active"));
				return p;
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findBySlug error", e);
		}
	}

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

	public List<Product> findForPromotionPicker(String keyword, Integer brandId, Integer categoryId) {
		return findForPromotionPicker(keyword, brandId, categoryId, true, null);
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
		Set<Integer> selectedSet = toIdSet(selectedProductIds);

		StringBuilder sql = new StringBuilder(
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.is_active, " +
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

		sql.append("ORDER BY p.title ASC, p.id DESC ");

		try (Connection connection = DBConnection.getConnection();
		     PreparedStatement statement = connection.prepareStatement(sql.toString())) {

			int index = 1;

			if (keyword != null && !keyword.isBlank()) {
				String like = "%" + keyword.trim() + "%";
				statement.setString(index++, like);
				statement.setString(index++, like);
				statement.setString(index++, like);
			}

			if (brandId != null && brandId > 0) {
				statement.setInt(index++, brandId);
			}

			if (categoryId != null && categoryId > 0) {
				statement.setInt(index, categoryId);
			}

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					Product product = mapRowPromotionPicker(resultSet);
					product.setSelectedForPromotion(selectedSet.contains(product.getId()));
					products.add(product);
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findForPromotionPicker error", e);
		}

		return products;
	}

	public List<Product> findActiveByBrandId(int brandId) {
		return findForPromotionPicker(null, brandId, null, true, null);
	}

	public List<Product> findActiveByCategoryId(int categoryId) {
		return findForPromotionPicker(null, null, categoryId, true, null);
	}

	public List<Product> findByIds(List<Integer> productIds) {
		List<Product> products = new ArrayList<>();
		List<Integer> cleanedIds = normalizeProductIds(productIds);

		if (cleanedIds.isEmpty()) {
			return products;
		}

		String sql =
				"SELECT p.id, p.title, p.slug, p.description, " +
						"p.price, p.discount_percent, p.stock, p.image, p.is_active, " +
						"c.id AS c_id, c.name AS c_name, " +
						"b.id AS b_id, b.name AS b_name " +
						"FROM store_product p " +
						"LEFT JOIN store_category c ON p.category_id = c.id " +
						"LEFT JOIN store_brand b ON p.brand_id = b.id " +
						"WHERE p.id IN (" + placeholders(cleanedIds.size()) + ") " +
						"ORDER BY p.title ASC, p.id DESC";

		try (Connection connection = DBConnection.getConnection();
		     PreparedStatement statement = connection.prepareStatement(sql)) {

			for (int i = 0; i < cleanedIds.size(); i++) {
				statement.setInt(i + 1, cleanedIds.get(i));
			}

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					products.add(mapRowPromotionPicker(resultSet));
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findByIds error", e);
		}

		return products;
	}

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
				if (rs.next()) return rs.getInt(1);
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

	public DeleteMode deleteOrDeactivateSafely(int productId) {
		String checkProductSql = "SELECT id FROM store_product WHERE id = ?";
		String checkOrderItemSql = "SELECT 1 FROM store_orderitem WHERE product_id = ? LIMIT 1";

		String softDeleteSql = "UPDATE store_product SET is_active = 0 WHERE id = ?";

		String deleteCartItemsSql = "DELETE FROM cart_items WHERE product_id = ?";
		String deleteCouponTargetsSql = "DELETE FROM store_coupon_product WHERE product_id = ?";
		String deleteBrandDiscountTargetsSql = "DELETE FROM store_branddiscount_product WHERE product_id = ?";
		String deletePromotionEventTargetsSql = "DELETE FROM store_promotionevent_product WHERE product_id = ?";
		String deleteReviewsSql = "DELETE FROM store_review WHERE product_id = ?";
		String deleteImagesSql = "DELETE FROM store_productimage WHERE product_id = ?";
		String deleteProductDiscountSql = "DELETE FROM store_productdiscount WHERE product_id = ?";
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
				executeDeleteByProductId(conn, deleteCouponTargetsSql, productId);
				executeDeleteByProductId(conn, deleteBrandDiscountTargetsSql, productId);
				executeDeleteByProductId(conn, deletePromotionEventTargetsSql, productId);
				executeDeleteByProductId(conn, deleteReviewsSql, productId);
				executeDeleteByProductId(conn, deleteImagesSql, productId);
				executeDeleteByProductId(conn, deleteProductDiscountSql, productId);
				executeDeleteByProductId(conn, deleteProductSql, productId);

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

	private Product findProductDetail(String sql, int productId, String errorMessage) {
		try (Connection c = DBConnection.getConnection();
		     PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, productId);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) return null;
				return mapRowDetail(rs);
			}

		} catch (SQLException e) {
			throw new RuntimeException(errorMessage, e);
		}
	}

	private int bindProductFilters(PreparedStatement ps, String keyword, List<Integer> categoryIds,
	                               List<Integer> brandIds, Integer minRating, int idx) throws SQLException {

		if (keyword != null && !keyword.isBlank()) {
			String like = "%" + keyword.trim() + "%";
			ps.setString(idx++, like);
			ps.setString(idx++, like);
		}

		if (categoryIds != null && !categoryIds.isEmpty()) {
			for (Integer cid : categoryIds) {
				ps.setInt(idx++, cid);
			}
		}

		if (brandIds != null && !brandIds.isEmpty()) {
			for (Integer bid : brandIds) {
				ps.setInt(idx++, bid);
			}
		}

		if (minRating != null) ps.setInt(idx++, minRating);

		return idx;
	}

	private void appendPriceRange(StringBuilder sql, List<String> priceRanges) {
		if (priceRanges == null || priceRanges.isEmpty()) return;

		List<String> clauses = new ArrayList<>();
		for (String pr : priceRanges) {
			if ("lt500".equals(pr)) {
				clauses.add("p.price < 500000");
			} else if ("500_1000".equals(pr)) {
				clauses.add("p.price BETWEEN 500000 AND 1000000");
			} else if ("gt1000".equals(pr)) {
				clauses.add("p.price > 1000000");
			}
		}

		if (!clauses.isEmpty()) {
			sql.append("AND (").append(String.join(" OR ", clauses)).append(") ");
		}
	}

	private void appendSort(StringBuilder sql, String sort) {
		if ("price_asc".equals(sort)) {
			sql.append("ORDER BY p.price ASC ");
		} else if ("price_desc".equals(sort)) {
			sql.append("ORDER BY p.price DESC ");
		} else {
			sql.append("ORDER BY p.created_at DESC ");
		}
	}

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
		p.setAvgRating(rs.getDouble("avg_rating"));
		p.setReviewCount(rs.getInt("review_count"));

		applyFinalPrice(p);
		attachCategoryAndBrand(rs, p);

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
		attachCategoryAndBrand(rs, p);

		return p;
	}

	private Product mapRowPromotionPicker(ResultSet rs) throws SQLException {
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
		attachCategoryAndBrand(rs, p);

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

	private void attachCategoryAndBrand(ResultSet rs, Product p) throws SQLException {
		if (rs.getObject("c_id") != null) {
			int categoryId = rs.getInt("c_id");

			Category cat = new Category();
			cat.setId(categoryId);
			cat.setName(rs.getString("c_name"));

			p.setCategory(cat);
			p.setCategoryId(categoryId);
			p.setCategoryName(rs.getString("c_name"));
		}

		if (rs.getObject("b_id") != null) {
			int brandId = rs.getInt("b_id");

			Brand br = new Brand();
			br.setId(brandId);
			br.setName(rs.getString("b_name"));

			p.setBrand(br);
			p.setBrandId(brandId);
			p.setBrandName(rs.getString("b_name"));
		}
	}

	private void applyFinalPrice(Product p) {
		if (p == null || p.getPrice() == null) return;

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

	private void executeDeleteByProductId(Connection conn, String sql, int productId) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, productId);
			ps.executeUpdate();
		}
	}

	private Set<Integer> toIdSet(List<Integer> ids) {
		return new LinkedHashSet<>(normalizeProductIds(ids));
	}

	private List<Integer> normalizeProductIds(List<Integer> productIds) {
		List<Integer> cleaned = new ArrayList<>();
		Set<Integer> unique = new LinkedHashSet<>();

		if (productIds == null) {
			return cleaned;
		}

		for (Integer productId : productIds) {
			if (productId != null && productId > 0) {
				unique.add(productId);
			}
		}

		cleaned.addAll(unique);
		return cleaned;
	}

	private String placeholders(int size) {
		if (size <= 0) {
			return "";
		}

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < size; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append("?");
		}

		return builder.toString();
	}

	public List<Product> findProductsByPromotion(com.webshop.app.model.PromotionEvent event) {
		List<Product> list = new ArrayList<>();
		if (event == null) return list;

		StringBuilder sql = new StringBuilder(
				"SELECT id, title, slug, description, price, discount_percent, stock, image " +
						"FROM store_product " +
						"WHERE 1=1"
		);

		if (event.getScope() == com.webshop.app.model.PromotionEvent.Scope.CATEGORY) {
			sql.append(" AND category_id = ?");
		} else if (event.getScope() == com.webshop.app.model.PromotionEvent.Scope.BRAND) {
			sql.append(" AND brand_id = ?");
		}

		sql.append(" ORDER BY id DESC");

		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql.toString())) {

			if (event.getScope() == com.webshop.app.model.PromotionEvent.Scope.CATEGORY) {
				ps.setInt(1, event.getCategoryId());
			} else if (event.getScope() == com.webshop.app.model.PromotionEvent.Scope.BRAND) {
				ps.setInt(1, event.getBrandId());
			}

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Product p = new Product();
					p.setId(rs.getInt("id"));
					p.setTitle(rs.getString("title"));
					p.setSlug(rs.getString("slug"));
					p.setDescription(rs.getString("description"));
					p.setPrice(rs.getBigDecimal("price"));
					p.setDiscountPercent(rs.getInt("discount_percent"));
					p.setStock(rs.getInt("stock"));
					p.setImage(rs.getString("image"));

					if (p.getPrice() != null) {
						BigDecimal originalPrice = p.getPrice();
						BigDecimal finalPrice = originalPrice;

						if (event.getDiscountType() != null) {
							String typeName = event.getDiscountType().name();

							if ("PERCENT".equals(typeName)) {
								BigDecimal hundred = new BigDecimal("100");
								BigDecimal discountAmount = originalPrice.multiply(event.getDiscountValue())
										.divide(hundred, 2, java.math.RoundingMode.HALF_UP);

								if (event.getMaxDiscountAmount() != null) {
									if (discountAmount.compareTo(event.getMaxDiscountAmount()) > 0) {
										discountAmount = event.getMaxDiscountAmount();
									}
								}
								finalPrice = originalPrice.subtract(discountAmount);

							} else if ("AMOUNT".equals(typeName) || "FIXED".equals(typeName) || "CASH".equals(typeName)) {
								finalPrice = originalPrice.subtract(event.getDiscountValue());
							}
						}

						if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
							finalPrice = BigDecimal.ZERO;
						}

						p.setFinalPrice(finalPrice);
					}

					list.add(p);
				}
			}
		} catch (SQLException ex) {
			throw new RuntimeException("ProductDAO.findProductsByPromotion error", ex);
		}

		return list;
	}
}

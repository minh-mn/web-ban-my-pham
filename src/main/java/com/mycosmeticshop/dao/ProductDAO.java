package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.Brand;
import com.mycosmeticshop.model.Category;
import com.mycosmeticshop.model.Product;
import com.mycosmeticshop.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

	/*
	 * ========================================================= FIND PRODUCTS –
	 * SEARCH / FILTER / SORT (FRONTEND: chỉ active)
	 * =========================================================
	 */
	public List<Product> findProducts(String keyword, Integer categoryId, Integer brandId, String sort,
			String priceRange, Integer minRating) {

		List<Product> list = new ArrayList<>();

		StringBuilder sql = new StringBuilder("SELECT p.id, p.title, p.slug, p.description, "
				+ "       p.price, p.discount_percent, p.stock, p.image, p.created_at, "
				+ "       COALESCE(AVG(CAST(r.rating AS float)), 0) AS avg_rating, "
				+ "       COUNT(r.id) AS review_count, " + "       c.id AS c_id, c.name AS c_name, "
				+ "       b.id AS b_id, b.name AS b_name " + "FROM store_product p "
				+ "LEFT JOIN store_review r ON p.id = r.product_id "
				+ "LEFT JOIN store_category c ON p.category_id = c.id "
				+ "LEFT JOIN store_brand b ON p.brand_id = b.id " + "WHERE p.is_active = 1 ");

		if (keyword != null && !keyword.isBlank()) {
			sql.append("AND (p.title LIKE ? OR p.description LIKE ?) ");
		}
		if (categoryId != null)
			sql.append("AND p.category_id = ? ");
		if (brandId != null)
			sql.append("AND p.brand_id = ? ");

		if (priceRange != null) {
			switch (priceRange) {
			case "lt500":
				sql.append("AND p.price < 500000 ");
				break;
			case "500_1000":
				sql.append("AND p.price BETWEEN 500000 AND 1000000 ");
				break;
			case "gt1000":
				sql.append("AND p.price > 1000000 ");
				break;
			}
		}

		sql.append("GROUP BY p.id, p.title, p.slug, p.description, "
				+ "         p.price, p.discount_percent, p.stock, p.image, p.created_at, "
				+ "         c.id, c.name, b.id, b.name ");

		if (minRating != null) {
			sql.append("HAVING COALESCE(AVG(CAST(r.rating AS float)), 0) >= ? ");
		}

		// sort
		if ("price_asc".equals(sort)) {
			sql.append("ORDER BY p.price ASC ");
		} else if ("price_desc".equals(sort)) {
			sql.append("ORDER BY p.price DESC ");
		} else {
			sql.append("ORDER BY p.created_at DESC ");
		}

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {

			int idx = 1;
			if (keyword != null && !keyword.isBlank()) {
				String like = "%" + keyword.trim() + "%";
				ps.setString(idx++, like);
				ps.setString(idx++, like);
			}
			if (categoryId != null)
				ps.setInt(idx++, categoryId);
			if (brandId != null)
				ps.setInt(idx++, brandId);
			if (minRating != null)
				ps.setInt(idx++, minRating);

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

	/*
	 * ========================================================= PAGINATION SUPPORT
	 * (FRONTEND) - countProducts(): đếm tổng sản phẩm theo bộ filter -
	 * findProductsPaged(): lấy 1 trang theo page/pageSize
	 * =========================================================
	 */

	/**
	 * Đếm tổng số sản phẩm theo filter để tính totalPages.
	 */
	public int countProducts(String keyword, Integer categoryId, Integer brandId, String priceRange,
			Integer minRating) {

		// Để đếm đúng khi có join review + lọc minRating, ta group theo product_id
		// trước rồi COUNT ngoài.
		StringBuilder sql = new StringBuilder(
				"SELECT COUNT(*) AS total " + "FROM ( " + "   SELECT p.id " + "   FROM store_product p "
						+ "   LEFT JOIN store_review r ON p.id = r.product_id " + "   WHERE p.is_active = 1 ");

		if (keyword != null && !keyword.isBlank()) {
			sql.append("AND (p.title LIKE ? OR p.description LIKE ?) ");
		}
		if (categoryId != null)
			sql.append("AND p.category_id = ? ");
		if (brandId != null)
			sql.append("AND p.brand_id = ? ");

		if (priceRange != null) {
			switch (priceRange) {
			case "lt500":
				sql.append("AND p.price < 500000 ");
				break;
			case "500_1000":
				sql.append("AND p.price BETWEEN 500000 AND 1000000 ");
				break;
			case "gt1000":
				sql.append("AND p.price > 1000000 ");
				break;
			}
		}

		sql.append("GROUP BY p.id ");

		// minRating: nếu null thì không HAVING
		if (minRating != null) {
			sql.append("HAVING COALESCE(AVG(CAST(r.rating AS float)), 0) >= ? ");
		}

		sql.append(") x");

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {

			int idx = 1;

			if (keyword != null && !keyword.isBlank()) {
				String like = "%" + keyword.trim() + "%";
				ps.setString(idx++, like);
				ps.setString(idx++, like);
			}
			if (categoryId != null)
				ps.setInt(idx++, categoryId);
			if (brandId != null)
				ps.setInt(idx++, brandId);
			if (minRating != null)
				ps.setInt(idx++, minRating);

			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.countProducts error", e);
		}
	}

	/**
	 * Lấy danh sách sản phẩm theo trang (SQL Server: OFFSET/FETCH). page bắt đầu từ
	 * 1.
	 */
	public List<Product> findProductsPaged(String keyword, Integer categoryId, Integer brandId, String sort,
			String priceRange, Integer minRating, int page, int pageSize) {

		int safePage = Math.max(1, page);
		int safeSize = Math.max(1, pageSize);
		int offset = (safePage - 1) * safeSize;

		List<Product> list = new ArrayList<>();

		StringBuilder sql = new StringBuilder("SELECT p.id, p.title, p.slug, p.description, "
				+ "       p.price, p.discount_percent, p.stock, p.image, p.created_at, "
				+ "       COALESCE(AVG(CAST(r.rating AS float)), 0) AS avg_rating, "
				+ "       COUNT(r.id) AS review_count, " + "       c.id AS c_id, c.name AS c_name, "
				+ "       b.id AS b_id, b.name AS b_name " + "FROM store_product p "
				+ "LEFT JOIN store_review r ON p.id = r.product_id "
				+ "LEFT JOIN store_category c ON p.category_id = c.id "
				+ "LEFT JOIN store_brand b ON p.brand_id = b.id " + "WHERE p.is_active = 1 ");

		if (keyword != null && !keyword.isBlank()) {
			sql.append("AND (p.title LIKE ? OR p.description LIKE ?) ");
		}
		if (categoryId != null)
			sql.append("AND p.category_id = ? ");
		if (brandId != null)
			sql.append("AND p.brand_id = ? ");

		if (priceRange != null) {
			switch (priceRange) {
			case "lt500":
				sql.append("AND p.price < 500000 ");
				break;
			case "500_1000":
				sql.append("AND p.price BETWEEN 500000 AND 1000000 ");
				break;
			case "gt1000":
				sql.append("AND p.price > 1000000 ");
				break;
			}
		}

		sql.append("GROUP BY p.id, p.title, p.slug, p.description, "
				+ "         p.price, p.discount_percent, p.stock, p.image, p.created_at, "
				+ "         c.id, c.name, b.id, b.name ");

		if (minRating != null) {
			sql.append("HAVING COALESCE(AVG(CAST(r.rating AS float)), 0) >= ? ");
		}

		// sort
		if ("price_asc".equals(sort)) {
			sql.append("ORDER BY p.price ASC ");
		} else if ("price_desc".equals(sort)) {
			sql.append("ORDER BY p.price DESC ");
		} else {
			sql.append("ORDER BY p.created_at DESC ");
		}

		// pagination
		sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {

			int idx = 1;

			if (keyword != null && !keyword.isBlank()) {
				String like = "%" + keyword.trim() + "%";
				ps.setString(idx++, like);
				ps.setString(idx++, like);
			}
			if (categoryId != null)
				ps.setInt(idx++, categoryId);
			if (brandId != null)
				ps.setInt(idx++, brandId);
			if (minRating != null)
				ps.setInt(idx++, minRating);

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

	/*
	 * ========================================================= ADMIN LIST: lấy cả
	 * active + inactive =========================================================
	 */
	public List<Product> findProductsAdmin(String keyword, Integer categoryId, Integer brandId, String sort) {

		List<Product> list = new ArrayList<>();

		StringBuilder sql = new StringBuilder("SELECT p.id, p.title, p.slug, p.description, "
				+ "       p.price, p.discount_percent, p.stock, p.image, p.created_at, p.is_active, "
				+ "       COALESCE(AVG(CAST(r.rating AS float)), 0) AS avg_rating, "
				+ "       COUNT(r.id) AS review_count, " + "       c.id AS c_id, c.name AS c_name, "
				+ "       b.id AS b_id, b.name AS b_name " + "FROM store_product p "
				+ "LEFT JOIN store_review r ON p.id = r.product_id "
				+ "LEFT JOIN store_category c ON p.category_id = c.id "
				+ "LEFT JOIN store_brand b ON p.brand_id = b.id " + "WHERE 1=1 ");

		if (keyword != null && !keyword.isBlank()) {
			sql.append("AND (p.title LIKE ? OR p.slug LIKE ? OR p.description LIKE ?) ");
		}
		if (categoryId != null)
			sql.append("AND p.category_id = ? ");
		if (brandId != null)
			sql.append("AND p.brand_id = ? ");

		sql.append("GROUP BY p.id, p.title, p.slug, p.description, "
				+ "         p.price, p.discount_percent, p.stock, p.image, p.created_at, p.is_active, "
				+ "         c.id, c.name, b.id, b.name ");

		if ("price_asc".equals(sort)) {
			sql.append("ORDER BY p.price ASC ");
		} else if ("price_desc".equals(sort)) {
			sql.append("ORDER BY p.price DESC ");
		} else {
			sql.append("ORDER BY p.created_at DESC ");
		}

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {

			int idx = 1;
			if (keyword != null && !keyword.isBlank()) {
				String like = "%" + keyword.trim() + "%";
				ps.setString(idx++, like);
				ps.setString(idx++, like);
				ps.setString(idx++, like);
			}
			if (categoryId != null)
				ps.setInt(idx++, categoryId);
			if (brandId != null)
				ps.setInt(idx++, brandId);

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

	/*
	 * ========================================================= FIND BY ID
	 * (FRONTEND: chỉ active)
	 * =========================================================
	 */
	public Product findById(int productId) {

		String sql = "SELECT p.id, p.title, p.slug, p.description, p.price, p.discount_percent, p.stock, p.image, p.is_active, p.created_at, "
				+ "       c.id AS c_id, c.name AS c_name, b.id AS b_id, b.name AS b_name " + "FROM store_product p "
				+ "LEFT JOIN store_category c ON p.category_id = c.id "
				+ "LEFT JOIN store_brand b ON p.brand_id = b.id " + "WHERE p.id = ? AND p.is_active = 1";

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, productId);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;
				return mapRowDetail(rs);
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findById error", e);
		}
	}

	/*
	 * ========================================================= FIND BY ID (ADMIN:
	 * lấy cả inactive) =========================================================
	 */
	public Product findByIdAdmin(int productId) {

		String sql = "SELECT p.id, p.title, p.slug, p.description, p.price, p.discount_percent, p.stock, p.image, p.is_active, p.created_at, "
				+ "       c.id AS c_id, c.name AS c_name, b.id AS b_id, b.name AS b_name " + "FROM store_product p "
				+ "LEFT JOIN store_category c ON p.category_id = c.id "
				+ "LEFT JOIN store_brand b ON p.brand_id = b.id " + "WHERE p.id = ?";

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, productId);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;
				return mapRowDetail(rs);
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findByIdAdmin error", e);
		}
	}

	/*
	 * ========================================================= FIND BY SLUG
	 * (FRONTEND) =========================================================
	 */
	public Product findBySlug(String slug) {

		String sql = "SELECT p.id, p.title, p.slug, p.description, "
				+ "       p.price, p.discount_percent, p.stock, p.image, p.is_active, p.created_at, "
				+ "       COALESCE(AVG(CAST(r.rating AS float)), 0) AS avg_rating, "
				+ "       COUNT(r.id) AS review_count, " + "       c.id AS c_id, c.name AS c_name, "
				+ "       b.id AS b_id, b.name AS b_name " + "FROM store_product p "
				+ "LEFT JOIN store_review r ON p.id = r.product_id "
				+ "LEFT JOIN store_category c ON p.category_id = c.id "
				+ "LEFT JOIN store_brand b ON p.brand_id = b.id " + "WHERE p.slug = ? AND p.is_active = 1 "
				+ "GROUP BY p.id, p.title, p.slug, p.description, "
				+ "         p.price, p.discount_percent, p.stock, p.image, p.is_active, p.created_at, "
				+ "         c.id, c.name, b.id, b.name";

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, slug);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;

				Product p = mapRowList(rs); // có avg_rating + review_count
				p.setActive(rs.getBoolean("is_active")); // mapRowList không set active
				return p;
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.findBySlug error", e);
		}
	}

	/*
	 * ========================================================= SEARCH BY KEYWORD
	 * (AJAX / QUICK SEARCH)
	 * =========================================================
	 */
	public List<Product> searchByKeyword(String keyword) {

		String sql = "SELECT id, title, slug, description, price, stock, image, discount_percent "
				+ "FROM dbo.store_product " + "WHERE is_active = 1 AND (title LIKE ? OR slug LIKE ?) "
				+ "ORDER BY id DESC";

		List<Product> list = new ArrayList<>();

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
			ps.setString(1, like);
			ps.setString(2, like);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Product p = new Product();
					p.setId(rs.getInt("id"));
					p.setTitle(rs.getString("title"));
					p.setSlug(rs.getString("slug"));
					p.setDescription(rs.getString("description"));
					p.setPrice(rs.getBigDecimal("price"));
					p.setStock(rs.getInt("stock"));
					p.setImage(rs.getString("image"));
					p.setDiscountPercent(rs.getInt("discount_percent"));
					applyFinalPrice(p);
					list.add(p);
				}
			}

			return list;

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.searchByKeyword error", e);
		}
	}

	/*
	 * ========================================================= CREATE / UPDATE /
	 * DELETE =========================================================
	 */
	public int create(Product p) {

		String sql = "INSERT INTO store_product "
				+ "(title, name, slug, description, price, discount_percent, stock, image, is_active, category_id, brand_id, created_at) "
				+ "OUTPUT INSERTED.id " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME())";

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

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

			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.create error", e);
		}
	}

	public boolean update(Product p) {

		String sql = "UPDATE store_product SET title=?, name=?, slug=?, description=?, price=?, discount_percent=?, "
				+ "stock=?, image=?, is_active=?, category_id=?, brand_id=? WHERE id=?";

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

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

	public boolean delete(int id) {
		String sql = "DELETE FROM store_product WHERE id = ?";
		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, id);
			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			if (e.getErrorCode() == 547) {
				throw new RuntimeException("Không thể xóa sản phẩm vì đang được tham chiếu (đơn hàng/chi tiết đơn...).",
						e);
			}
			throw new RuntimeException("ProductDAO.delete error", e);
		}
	}

	public int deleteReviewsByProductId(int productId) {
		String sql = "DELETE FROM store_review WHERE product_id = ?";
		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, productId);
			return ps.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("ProductDAO.deleteReviewsByProductId error", e);
		}
	}

	/*
	 * ========================================================= HOME FEATURED: TOP
	 * 12 - BÁN CHẠY + GIẢM GIÁ SÂU (đúng schema)
	 * =========================================================
	 */
	public List<Product> findFeaturedTop12BestSellerDeepDiscount() {

		List<Product> list = new ArrayList<>();

		String sql = "SELECT TOP 12 " + "   p.id, p.title, p.slug, p.description, "
				+ "   p.price, p.discount_percent, p.stock, p.image, p.created_at, "
				+ "   COALESCE(rv.avg_rating, 0) AS avg_rating, " + "   COALESCE(rv.review_count, 0) AS review_count, "
				+ "   c.id AS c_id, c.name AS c_name, " + "   b.id AS b_id, b.name AS b_name, "
				+ "   COALESCE(sd.sold_qty, 0) AS sold_qty " + "FROM store_product p "
				+ "LEFT JOIN store_category c ON p.category_id = c.id "
				+ "LEFT JOIN store_brand b ON p.brand_id = b.id " + "LEFT JOIN ( "
				+ "   SELECT product_id, AVG(CAST(rating AS float)) AS avg_rating, COUNT(*) AS review_count "
				+ "   FROM store_review GROUP BY product_id " + ") rv ON rv.product_id = p.id " + "LEFT JOIN ( "
				+ "   SELECT oi.product_id, SUM(oi.quantity) AS sold_qty " + "   FROM store_orderitem oi "
				+ "   JOIN store_order o ON o.id = oi.order_id " + "   WHERE o.payment_status = 'PAID' "
				+ "   GROUP BY oi.product_id " + ") sd ON sd.product_id = p.id " + "WHERE p.is_active = 1 "
				+ "ORDER BY " + "   COALESCE(sd.sold_qty, 0) DESC, " + "   p.discount_percent DESC, "
				+ "   p.created_at DESC";

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

		String sql = "SELECT TOP 12 " + "   p.id, p.title, p.slug, p.description, "
				+ "   p.price, p.discount_percent, p.stock, p.image, p.created_at, "
				+ "   COALESCE(AVG(CAST(r.rating AS float)), 0) AS avg_rating, " + "   COUNT(r.id) AS review_count, "
				+ "   c.id AS c_id, c.name AS c_name, " + "   b.id AS b_id, b.name AS b_name " + "FROM store_product p "
				+ "LEFT JOIN store_review r ON p.id = r.product_id "
				+ "LEFT JOIN store_category c ON p.category_id = c.id "
				+ "LEFT JOIN store_brand b ON p.brand_id = b.id " + "WHERE p.is_active = 1 " + "GROUP BY "
				+ "   p.id, p.title, p.slug, p.description, "
				+ "   p.price, p.discount_percent, p.stock, p.image, p.created_at, " + "   c.id, c.name, b.id, b.name "
				+ "ORDER BY " + "   p.discount_percent DESC, " + "   COALESCE(AVG(CAST(r.rating AS float)), 0) DESC, "
				+ "   COUNT(r.id) DESC, " + "   p.created_at DESC";

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

	/* ===================== MAPPERS ===================== */

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

		// ✅ tính finalPrice để JSP dùng ${product.finalPrice}
		applyFinalPrice(p);

		if (rs.getObject("c_id") != null) {
			Category cat = new Category();
			cat.setId(rs.getInt("c_id"));
			cat.setName(rs.getString("c_name"));
			p.setCategory(cat);
		}

		if (rs.getObject("b_id") != null) {
			Brand br = new Brand();
			br.setId(rs.getInt("b_id"));
			br.setName(rs.getString("b_name"));
			p.setBrand(br);
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
		}

		if (rs.getObject("b_id") != null) {
			Brand br = new Brand();
			br.setId(rs.getInt("b_id"));
			br.setName(rs.getString("b_name"));
			p.setBrand(br);
		}

		return p;
	}

	private void applyFinalPrice(Product p) {
		if (p == null)
			return;
		BigDecimal price = p.getPrice();
		if (price == null)
			return;

		int dp = p.getDiscountPercent();
		if (dp <= 0) {
			p.setFinalPrice(price);
			return;
		}

		BigDecimal hundred = new BigDecimal("100");
		BigDecimal rate = hundred.subtract(BigDecimal.valueOf(dp));
		BigDecimal finalPrice = price.multiply(rate).divide(hundred);

		p.setFinalPrice(finalPrice);
	}
}

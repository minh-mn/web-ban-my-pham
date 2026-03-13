package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.DiscountType;
import com.mycosmeticshop.model.PromotionEvent;
import com.mycosmeticshop.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PromotionEventDAO {

	private static final String TABLE = "dbo.store_promotionevent";

	/* ================= CREATE ================= */
	public int create(PromotionEvent e) {

		String sql = "INSERT INTO " + TABLE + " " + "(name, scope, discount_type, discount_value, max_discount_amount, "
				+ " brand_id, category_id, start_date, end_date, is_active) " + "OUTPUT INSERTED.id "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, e.getName());
			ps.setString(2, e.getScope().name());
			ps.setString(3, e.getDiscountType().name());
			ps.setBigDecimal(4, e.getDiscountValue());

			if (e.getMaxDiscountAmount() == null)
				ps.setNull(5, Types.NUMERIC);
			else
				ps.setBigDecimal(5, e.getMaxDiscountAmount());

			// brand_id BIGINT
			if (e.getBrandId() == null)
				ps.setNull(6, Types.BIGINT);
			else
				ps.setLong(6, e.getBrandId().longValue());

			// category_id BIGINT
			if (e.getCategoryId() == null)
				ps.setNull(7, Types.BIGINT);
			else
				ps.setLong(7, e.getCategoryId().longValue());

			ps.setDate(8, Date.valueOf(e.getStartDate()));
			ps.setDate(9, Date.valueOf(e.getEndDate()));
			ps.setBoolean(10, e.isActive());

			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.create error", ex);
		}
	}

	/* ================= FIND ALL (ADMIN LIST) ================= */

	/**
	 * joinRef = true: join ra brandName/categoryName để list đẹp (nếu bạn có field
	 * view) Nếu model PromotionEvent không có brandName/categoryName thì vẫn OK
	 * (chỉ bỏ qua)
	 */
	public List<PromotionEvent> findAll(boolean joinRef) {

		List<PromotionEvent> list = new ArrayList<>();

		String sql;
		if (joinRef) {
			sql = "SELECT e.*, " + "       b.name AS brand_name, " + "       c.name AS category_name " + "FROM " + TABLE
					+ " e " + "LEFT JOIN dbo.store_brand b ON b.id = e.brand_id "
					+ "LEFT JOIN dbo.store_category c ON c.id = e.category_id " + "ORDER BY e.id DESC";
		} else {
			sql = "SELECT * FROM " + TABLE + " ORDER BY id DESC";
		}

		try (Connection c = DBConnection.getConnection();
				PreparedStatement ps = c.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				PromotionEvent e = mapRow(rs);

				// Nếu model có các field view (không bắt buộc) thì set bằng reflection-safe
				// không làm ở đây.
				// Bạn có thể tự thêm vào model: private String brandName, categoryName +
				// getter/setter
				// rồi mở các dòng dưới:
				// e.setBrandName(rs.getString("brand_name"));
				// e.setCategoryName(rs.getString("category_name"));

				list.add(e);
			}

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.findAll error", ex);
		}

		return list;
	}

	/** Giữ tương thích nếu bạn đang gọi findAll() không tham số */
	public List<PromotionEvent> findAll() {
		return findAll(false);
	}

	/* ================= FIND BY ID ================= */
	public PromotionEvent findById(int id) {

		String sql = "SELECT * FROM " + TABLE + " WHERE id = ?";

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;
				return mapRow(rs);
			}

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.findById error", ex);
		}
	}

	/* ================= UPDATE ================= */
	public void update(PromotionEvent e) {

		String sql = "UPDATE " + TABLE + " SET " + "name = ?, scope = ?, discount_type = ?, discount_value = ?, "
				+ "max_discount_amount = ?, brand_id = ?, category_id = ?, "
				+ "start_date = ?, end_date = ?, is_active = ? " + "WHERE id = ?";

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, e.getName());
			ps.setString(2, e.getScope().name());
			ps.setString(3, e.getDiscountType().name());
			ps.setBigDecimal(4, e.getDiscountValue());

			if (e.getMaxDiscountAmount() == null)
				ps.setNull(5, Types.NUMERIC);
			else
				ps.setBigDecimal(5, e.getMaxDiscountAmount());

			if (e.getBrandId() == null)
				ps.setNull(6, Types.BIGINT);
			else
				ps.setLong(6, e.getBrandId().longValue());

			if (e.getCategoryId() == null)
				ps.setNull(7, Types.BIGINT);
			else
				ps.setLong(7, e.getCategoryId().longValue());

			ps.setDate(8, Date.valueOf(e.getStartDate()));
			ps.setDate(9, Date.valueOf(e.getEndDate()));
			ps.setBoolean(10, e.isActive());
			ps.setInt(11, e.getId());

			ps.executeUpdate();

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.update error", ex);
		}
	}

	/* ================= TOGGLE ACTIVE ================= */
	public void toggleActive(int id) {

		String sql = "UPDATE " + TABLE + " " + "SET is_active = CASE WHEN is_active = 1 THEN 0 ELSE 1 END "
				+ "WHERE id = ?";

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.toggleActive error", ex);
		}
	}

	/* ================= DELETE ================= */
	public void delete(int id) {

		String sql = "DELETE FROM " + TABLE + " WHERE id = ?";

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.delete error", ex);
		}
	}

	/* ================= MAP RESULTSET ================= */
	private PromotionEvent mapRow(ResultSet rs) throws SQLException {

		PromotionEvent e = new PromotionEvent();

		e.setId(rs.getInt("id"));
		e.setName(rs.getString("name"));

		e.setScope(PromotionEvent.Scope.valueOf(rs.getString("scope")));
		e.setDiscountType(DiscountType.valueOf(rs.getString("discount_type")));
		e.setDiscountValue(rs.getBigDecimal("discount_value"));
		e.setMaxDiscountAmount(rs.getBigDecimal("max_discount_amount"));

		long brandId = rs.getLong("brand_id");
		e.setBrandId(rs.wasNull() ? null : safeToIntOrNull(brandId));

		long categoryId = rs.getLong("category_id");
		e.setCategoryId(rs.wasNull() ? null : safeToIntOrNull(categoryId));

		e.setStartDate(rs.getDate("start_date").toLocalDate());
		e.setEndDate(rs.getDate("end_date").toLocalDate());
		e.setActive(rs.getBoolean("is_active"));

		return e;
	}

	/**
	 * Vì model hiện đang dùng Integer (không phải Long), ta ép BIGINT -> int an
	 * toàn. Nếu vượt int thì trả null để tránh overflow. Khuyến nghị: đổi
	 * PromotionEvent.brandId/categoryId sang Long về lâu dài.
	 */
	private Integer safeToIntOrNull(long v) {
		if (v > Integer.MAX_VALUE || v < Integer.MIN_VALUE)
			return null;
		return (int) v;
	}
}

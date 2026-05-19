package com.webshop.app.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.webshop.app.model.DiscountType;
import com.webshop.app.model.PromotionEvent;
import com.webshop.app.utils.DBConnection;

public class PromotionEventDAO {

	private static final String TABLE = "store_promotionevent";

	/* ================= CREATE ================= */

	public int create(PromotionEvent e) {

		String sql =
				"INSERT INTO " + TABLE + " " +
						"(name, scope, discount_type, discount_value, max_discount_amount, " +
						" brand_id, category_id, start_date, end_date, is_active) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection c = DBConnection.getConnection();
			 PreparedStatement ps = c.prepareStatement(
					 sql,
					 Statement.RETURN_GENERATED_KEYS
			 )) {

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

			ps.executeUpdate();

			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}

			return 0;

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.create error", ex);
		}
	}

	/* ================= FIND ALL ================= */

	public List<PromotionEvent> findAll(boolean joinRef) {

		List<PromotionEvent> list = new ArrayList<>();

		String sql;

		if (joinRef) {

			sql =
					"SELECT e.*, " +
							"       b.name AS brand_name, " +
							"       c.name AS category_name " +
							"FROM " + TABLE + " e " +
							"LEFT JOIN store_brand b ON b.id = e.brand_id " +
							"LEFT JOIN store_category c ON c.id = e.category_id " +
							"ORDER BY e.id DESC";

		} else {

			sql =
					"SELECT * FROM " + TABLE + " " +
							"ORDER BY id DESC";
		}

		try (Connection c = DBConnection.getConnection();
			 PreparedStatement ps = c.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				list.add(mapRow(rs));
			}

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.findAll error", ex);
		}

		return list;
	}

	public List<PromotionEvent> findAll() {
		return findAll(false);
	}

	/* ================= FIND BY ID ================= */

	public PromotionEvent findById(int id) {

		String sql =
				"SELECT * FROM " + TABLE + " WHERE id = ?";

		try (Connection c = DBConnection.getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, id);

			try (ResultSet rs = ps.executeQuery()) {

				if (!rs.next()) {
					return null;
				}

				return mapRow(rs);
			}

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.findById error", ex);
		}
	}

	/* ================= UPDATE ================= */

	public void update(PromotionEvent e) {

		String sql =
				"UPDATE " + TABLE + " SET " +
						"name = ?, " +
						"scope = ?, " +
						"discount_type = ?, " +
						"discount_value = ?, " +
						"max_discount_amount = ?, " +
						"brand_id = ?, " +
						"category_id = ?, " +
						"start_date = ?, " +
						"end_date = ?, " +
						"is_active = ? " +
						"WHERE id = ?";

		try (Connection c = DBConnection.getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {

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

		String sql =
				"UPDATE " + TABLE + " " +
						"SET is_active = CASE WHEN is_active = 1 THEN 0 ELSE 1 END " +
						"WHERE id = ?";

		try (Connection c = DBConnection.getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.toggleActive error", ex);
		}
	}

	/* ================= DELETE ================= */

	public void delete(int id) {

		String sql =
				"DELETE FROM " + TABLE + " WHERE id = ?";

		try (Connection c = DBConnection.getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (SQLException ex) {
			throw new RuntimeException("PromotionEventDAO.delete error", ex);
		}
	}

	/* ================= MAP ================= */

	private PromotionEvent mapRow(ResultSet rs) throws SQLException {

		PromotionEvent e = new PromotionEvent();

		e.setId(rs.getInt("id"));
		e.setName(rs.getString("name"));

		e.setScope(
				PromotionEvent.Scope.valueOf(
						rs.getString("scope")
				)
		);

		e.setDiscountType(
				DiscountType.valueOf(
						rs.getString("discount_type")
				)
		);

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

	private Integer safeToIntOrNull(long v) {

		if (v > Integer.MAX_VALUE || v < Integer.MIN_VALUE)
			return null;

		return (int) v;
	}
}
package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.Banner;
import com.mycosmeticshop.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BannerDAO {

	// ================= USER SIDE =================
	public List<Banner> findActiveBanners() {
		List<Banner> list = new ArrayList<>();

		String sql = "SELECT id, title, image, link, is_active, [order] " + "FROM store_banner "
				+ "WHERE is_active = 1 " + "ORDER BY [order] ASC";

		try (Connection c = DBConnection.getConnection();
				PreparedStatement ps = c.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				list.add(mapRow(rs));
			}

		} catch (SQLException e) {
			throw new RuntimeException("BannerDAO.findActiveBanners error", e);
		}

		return list;
	}

	// ================= ADMIN SIDE =================

	/** Admin list: lấy tất cả banner (active + inactive) */
	public List<Banner> findAll() {
		List<Banner> list = new ArrayList<>();

		String sql = "SELECT id, title, image, link, is_active, [order] " + "FROM store_banner "
				+ "ORDER BY [order] ASC, id DESC";

		try (Connection c = DBConnection.getConnection();
				PreparedStatement ps = c.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				list.add(mapRow(rs));
			}

		} catch (SQLException e) {
			throw new RuntimeException("BannerDAO.findAll error", e);
		}

		return list;
	}

	public Banner findById(int id) {
		String sql = "SELECT id, title, image, link, is_active, [order] " + "FROM store_banner " + "WHERE id = ?";

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return mapRow(rs);
			}

		} catch (SQLException e) {
			throw new RuntimeException("BannerDAO.findById error", e);
		}

		return null;
	}

	public void create(Banner b) {
		String sql = "INSERT INTO store_banner (title, image, link, is_active, [order], created_at) "
				+ "VALUES (?, ?, ?, ?, " + "        COALESCE((SELECT MAX([order]) + 1 FROM store_banner), 1), "
				+ "        SYSDATETIME())";

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, b.getTitle());
			ps.setString(2, b.getImageUrl());
			ps.setString(3, b.getLink());
			ps.setBoolean(4, b.isActive());

			ps.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("BannerDAO.create error", e);
		}
	}

	public void update(Banner b) {
		String sql = "UPDATE store_banner " + "SET title = ?, image = ?, link = ?, is_active = ? " + "WHERE id = ?";

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, b.getTitle());
			ps.setString(2, b.getImageUrl());
			ps.setString(3, b.getLink());
			ps.setBoolean(4, b.isActive());
			ps.setInt(5, b.getId());

			ps.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("BannerDAO.update error", e);
		}
	}

	public void delete(int id) {
		String sql = "DELETE FROM store_banner WHERE id = ?";

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("BannerDAO.delete error", e);
		}
	}

	/** Toggle is_active theo trạng thái hiện tại */
	public void toggleActive(int id) {
		String sql = "UPDATE store_banner " + "SET is_active = CASE WHEN is_active = 1 THEN 0 ELSE 1 END "
				+ "WHERE id = ?";

		try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("BannerDAO.toggleActive error", e);
		}
	}

	// ================= MAPPING =================
	private Banner mapRow(ResultSet rs) throws SQLException {
		Banner b = new Banner();
		b.setId(rs.getInt("id"));
		b.setTitle(rs.getString("title"));

		// DB image -> Java imageUrl
		b.setImageUrl(rs.getString("image"));

		b.setLink(rs.getString("link"));

		// is_active + order nếu model có
		try {
			b.setActive(rs.getBoolean("is_active"));
		} catch (SQLException ignore) {
			// nếu model/DB thiếu cột thì bỏ qua
		}

		// nếu Banner model có setOrder(int)
		try {
			int orderVal = rs.getInt("order"); // với SQL Server alias "order" vẫn ok vì select [order]
			// b.setOrder(orderVal);
		} catch (SQLException ignore) {
		}

		return b;
	}
}

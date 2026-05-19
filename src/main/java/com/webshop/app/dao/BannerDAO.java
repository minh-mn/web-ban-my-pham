package com.webshop.app.dao;

import com.webshop.app.model.Banner;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BannerDAO {

	// ================= USER SIDE =================

	public List<Banner> findActiveBanners() {
		List<Banner> banners = new ArrayList<>();

		String sql = """
                SELECT id, title, image, link, is_active, `order`
                FROM store_banner
                WHERE is_active = 1
                ORDER BY `order` ASC
                """;

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql);
			 ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {
				banners.add(mapRow(resultSet));
			}

		} catch (SQLException e) {
			throw new RuntimeException("BannerDAO.findActiveBanners error", e);
		}

		return banners;
	}

	// ================= ADMIN SIDE =================

	public List<Banner> findAll() {
		List<Banner> banners = new ArrayList<>();

		String sql = """
                SELECT id, title, image, link, is_active, `order`
                FROM store_banner
                ORDER BY `order` ASC, id DESC
                """;

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql);
			 ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {
				banners.add(mapRow(resultSet));
			}

		} catch (SQLException e) {
			throw new RuntimeException("BannerDAO.findAll error", e);
		}

		return banners;
	}

	public Banner findById(int id) {
		String sql = """
                SELECT id, title, image, link, is_active, `order`
                FROM store_banner
                WHERE id = ?
                """;

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setInt(1, id);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return mapRow(resultSet);
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("BannerDAO.findById error", e);
		}

		return null;
	}

	public void create(Banner banner) {
		String sql = """
                INSERT INTO store_banner (title, image, link, is_active, `order`, created_at)
                VALUES (
                    ?, ?, ?, ?,
                    COALESCE((SELECT MAX(b.`order`) + 1 FROM store_banner b), 1),
                    NOW()
                )
                """;

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, banner.getTitle());
			statement.setString(2, banner.getImageUrl());
			statement.setString(3, banner.getLink());
			statement.setBoolean(4, banner.isActive());

			statement.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("BannerDAO.create error", e);
		}
	}

	public void update(Banner banner) {
		String sql = """
                UPDATE store_banner
                SET title = ?, image = ?, link = ?, is_active = ?
                WHERE id = ?
                """;

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, banner.getTitle());
			statement.setString(2, banner.getImageUrl());
			statement.setString(3, banner.getLink());
			statement.setBoolean(4, banner.isActive());
			statement.setInt(5, banner.getId());

			statement.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("BannerDAO.update error", e);
		}
	}

	public void delete(int id) {
		String sql = """
                DELETE FROM store_banner
                WHERE id = ?
                """;

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setInt(1, id);
			statement.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("BannerDAO.delete error", e);
		}
	}

	public void toggleActive(int id) {
		String sql = """
                UPDATE store_banner
                SET is_active = CASE WHEN is_active = 1 THEN 0 ELSE 1 END
                WHERE id = ?
                """;

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setInt(1, id);
			statement.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("BannerDAO.toggleActive error", e);
		}
	}

	private Banner mapRow(ResultSet resultSet) throws SQLException {
		Banner banner = new Banner();

		banner.setId(resultSet.getInt("id"));
		banner.setTitle(resultSet.getString("title"));
		banner.setImageUrl(resultSet.getString("image"));
		banner.setLink(resultSet.getString("link"));
		banner.setActive(resultSet.getBoolean("is_active"));

		// Nếu model Banner sau này có setOrder(int), có thể mở dòng dưới:
		// banner.setOrder(resultSet.getInt("order"));

		return banner;
	}
}
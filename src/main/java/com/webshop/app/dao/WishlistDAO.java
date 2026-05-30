package com.webshop.app.dao;

import java.sql.*;
import java.util.*;
import com.webshop.app.utils.DBConnection;

public class WishlistDAO {

    public boolean add(int userId, int productId) {
        String sql = "INSERT IGNORE INTO store_wishlist(user_id, product_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, productId);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean remove(int userId, int productId) {
        String sql = "DELETE FROM store_wishlist WHERE user_id=? AND product_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, productId);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean exists(int userId, int productId) {
        String sql = "SELECT 1 FROM store_wishlist WHERE user_id=? AND product_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, productId);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Integer> findProductIdsByUser(int userId) {
        List<Integer> ids = new ArrayList<>();

        String sql = "SELECT product_id FROM store_wishlist WHERE user_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ids.add(rs.getInt("product_id"));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ids;
    }

    public List<Integer> findUserIdsByProduct(int productId) {
        List<Integer> userIds = new ArrayList<>();
        String sql = "SELECT user_id FROM store_wishlist WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getInt("user_id"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("WishlistDAO.findUserIdsByProduct error", e);
        }
        return userIds;
    }
}

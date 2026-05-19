package com.webshop.app.dao;

import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class RememberTokenDAO {

    public Integer findUserIdByToken(String token) {
        String sql =
                "SELECT user_id " +
                        "FROM remember_tokens " +
                        "WHERE token = ? " +
                        "  AND revoked = 0 " +
                        "  AND expires_at > ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, token);
            statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("user_id");
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("RememberTokenDAO.findUserIdByToken error", e);
        }

        return null;
    }

    public void saveToken(int userId, String token, LocalDateTime expiresAt) {
        String sql =
                "INSERT INTO remember_tokens(token, user_id, expires_at, revoked, created_at) " +
                        "VALUES (?, ?, ?, 0, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, token);
            statement.setInt(2, userId);
            statement.setTimestamp(3, Timestamp.valueOf(expiresAt));
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("RememberTokenDAO.saveToken error", e);
        }
    }

    public void revokeToken(String token) {
        String sql =
                "UPDATE remember_tokens " +
                        "SET revoked = 1 " +
                        "WHERE token = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, token);
            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("RememberTokenDAO.revokeToken error", e);
        }
    }

    public int deleteExpiredTokens() {
        String sql =
                "DELETE FROM remember_tokens " +
                        "WHERE expires_at <= ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));

            return statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("RememberTokenDAO.deleteExpiredTokens error", e);
        }
    }
}
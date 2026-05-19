package com.webshop.app.dao;

import com.webshop.app.model.PasswordResetToken;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PasswordResetTokenDAO {

    public void invalidateAllActiveTokensOfUser(int userId) {
        String sql = """
                UPDATE password_reset_tokens
                SET used = 1
                WHERE user_id = ?
                  AND used = 0
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("PasswordResetTokenDAO.invalidateAllActiveTokensOfUser error", e);
        }
    }

    public void create(int userId, String token, LocalDateTime expiresAt) {
        String sql = """
                INSERT INTO password_reset_tokens
                (user_id, token, expires_at, used)
                VALUES (?, ?, ?, 0)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, token);
            statement.setTimestamp(3, Timestamp.valueOf(expiresAt));
            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("PasswordResetTokenDAO.create error", e);
        }
    }

    public PasswordResetToken findByToken(String token) {
        String sql = """
                SELECT id, user_id, token, expires_at, used
                FROM password_reset_tokens
                WHERE token = ?
                  AND used = 0
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, token);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapRow(resultSet);
            }

        } catch (Exception e) {
            throw new RuntimeException("PasswordResetTokenDAO.findByToken error", e);
        }
    }

    public boolean markUsed(int id) {
        String sql = """
                UPDATE password_reset_tokens
                SET used = 1
                WHERE id = ?
                  AND used = 0
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("PasswordResetTokenDAO.markUsed error", e);
        }
    }

    public int cleanupExpiredOrUsed() {
        String sql = """
                DELETE FROM password_reset_tokens
                WHERE used = 1
                   OR expires_at < ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            return statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("PasswordResetTokenDAO.cleanupExpiredOrUsed error", e);
        }
    }

    private PasswordResetToken mapRow(ResultSet resultSet) throws Exception {
        PasswordResetToken token = new PasswordResetToken();

        token.setId(resultSet.getInt("id"));
        token.setUserId(resultSet.getInt("user_id"));
        token.setToken(resultSet.getString("token"));

        Timestamp expiresAt = resultSet.getTimestamp("expires_at");
        token.setExpiresAt(expiresAt == null ? null : expiresAt.toLocalDateTime());

        token.setUsed(resultSet.getBoolean("used"));

        return token;
    }
}
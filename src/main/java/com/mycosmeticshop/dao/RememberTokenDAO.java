package com.mycosmeticshop.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.mycosmeticshop.utils.DBConnection;

public class RememberTokenDAO {

    /**
     * Token hợp lệ khi:
     *  - token tồn tại
     *  - revoked = 0/false
     *  - expires_at > NOW()
     */
    public Integer findUserIdByToken(String token) {
        String sql =
                "SELECT user_id " +
                "FROM remember_tokens " +
                "WHERE token = ? " +
                "  AND revoked = 0 " +
                "  AND expires_at > ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, token);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("user_id");
            }

        } catch (Exception e) {
            System.out.println("[RememberTokenDAO] findUserIdByToken error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lưu token mới (revoked mặc định = 0)
     */
    public void saveToken(int userId, String token, LocalDateTime expiresAt) {
        String sql =
                "INSERT INTO remember_tokens(token, user_id, expires_at, revoked, created_at) " +
                "VALUES (?, ?, ?, 0, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, token);
            ps.setInt(2, userId);
            ps.setTimestamp(3, Timestamp.valueOf(expiresAt));
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("[RememberTokenDAO] saveToken error: " + e.getMessage());
        }
    }

    /**
     * Revoke token (khi logout hoặc token invalid)
     */
    public void revokeToken(String token) {
        String sql = "UPDATE remember_tokens SET revoked = 1 WHERE token = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, token);
            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("[RememberTokenDAO] revokeToken error: " + e.getMessage());
        }
    }

    /**
     * (Tuỳ chọn) dọn token hết hạn để DB gọn
     */
    public int deleteExpiredTokens() {
        String sql = "DELETE FROM remember_tokens WHERE expires_at <= ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            return ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("[RememberTokenDAO] deleteExpiredTokens error: " + e.getMessage());
        }
        return 0;
    }
}

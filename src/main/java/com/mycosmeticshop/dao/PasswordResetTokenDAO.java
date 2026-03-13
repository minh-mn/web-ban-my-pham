package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.PasswordResetToken;
import com.mycosmeticshop.utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class PasswordResetTokenDAO {

    /**
     * (Tuỳ chọn - khuyến nghị) Khi tạo token mới, vô hiệu hoá token cũ của user
     * để tránh user có nhiều link reset còn hiệu lực.
     */
    public void invalidateAllActiveTokensOfUser(int userId) {
        String sql = "UPDATE password_reset_tokens SET used = 1 WHERE user_id = ? AND used = 0";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Invalidate old reset tokens failed", e);
        }
    }

    /**
     * Tạo token reset.
     * Nếu bạn dùng invalidateAllActiveTokensOfUser(userId) thì gọi trước create().
     */
    public void create(int userId, String token, LocalDateTime expiresAt) {
        String sql = "INSERT INTO password_reset_tokens(user_id, token, expires_at, used) VALUES(?,?,?,0)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, token);
            ps.setTimestamp(3, Timestamp.valueOf(expiresAt));
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Create reset token failed", e);
        }
    }

    /**
     * Tìm token theo chuỗi token.
     * Khuyến nghị: lọc used=0 để token đã dùng không thể dùng lại.
     */
    public PasswordResetToken findByToken(String token) {
        String sql = "SELECT id, user_id, token, expires_at, used " +
                     "FROM password_reset_tokens " +
                     "WHERE token = ? AND used = 0";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, token);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                PasswordResetToken t = new PasswordResetToken();
                t.setId(rs.getInt("id"));
                t.setUserId(rs.getInt("user_id"));
                t.setToken(rs.getString("token"));
                t.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
                t.setUsed(rs.getBoolean("used"));
                return t;
            }

        } catch (Exception e) {
            throw new RuntimeException("Find reset token failed", e);
        }
    }

    /**
     * Đánh dấu token đã dùng.
     * Dùng WHERE id=? AND used=0 để tránh update thừa.
     */
    public boolean markUsed(int id) {
        String sql = "UPDATE password_reset_tokens SET used = 1 WHERE id = ? AND used = 0";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Mark token used failed", e);
        }
    }

    /**
     * Cleanup token:
     * - Xoá token đã dùng
     * - Xoá token hết hạn (expires_at < now)
     *
     * Nên chạy định kỳ (hoặc gọi nhẹ mỗi khi requestReset).
     */
    public int cleanupExpiredOrUsed() {
        String sql = "DELETE FROM password_reset_tokens WHERE used = 1 OR expires_at < ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Cleanup reset tokens failed", e);
        }
    }
}

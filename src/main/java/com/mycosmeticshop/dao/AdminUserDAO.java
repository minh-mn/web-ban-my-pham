package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.User;
import com.mycosmeticshop.utils.DBConnection;
import com.mycosmeticshop.utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminUserDAO {

    private static final String T_USERS = "dbo.users";

    /* ===================== SEARCH (ADMIN LIST) ===================== */
    // active: null = không lọc, 1 = active, 0 = disabled
    public List<User> search(String q, String role, String rank, Integer active) {

        List<User> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, username, role, full_name, email, phone, active, created_at ");
        sql.append("FROM ").append(T_USERS).append(" ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (q != null && !q.isBlank()) {
            // tìm theo username/full_name/email/phone cho tiện quản trị
            sql.append(" AND (username LIKE ? OR full_name LIKE ? OR email LIKE ? OR phone LIKE ?) ");
            String like = "%" + q.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (role != null && !role.isBlank()) {
            sql.append(" AND role = ? ");
            params.add(role.trim());
        }

        if (active != null) {
            sql.append(" AND active = ? ");
            params.add(active);
        }

        sql.append(" ORDER BY id DESC");

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            bind(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.search error", e);
        }

        return list;
    }

    /* ===================== FIND BY ID (ADMIN EDIT/DETAIL) ===================== */
    public User findById(int id) {

        String sql =
            "SELECT id, username, role, full_name, email, phone, active, created_at " +
            "FROM " + T_USERS + " " +
            "WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, (long) id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.findById error", e);
        }

        return null;
    }

    /* ===================== UPDATE INFO (ADMIN) ===================== */
    public boolean updateInfoAdmin(User u) {

        String sql =
            "UPDATE " + T_USERS + " " +
            "SET full_name = ?, email = ?, phone = ?, role = ?, active = ? " +
            "WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, u.getFullName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPhone());
            ps.setString(4, u.getRole());
            ps.setInt(5, u.isActive() ? 1 : 0);
            ps.setLong(6, (long) u.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.updateInfoAdmin error", e);
        }
    }

    /* ===================== UPDATE PASSWORD (ADMIN RESET) ===================== */
    public boolean updatePasswordAdmin(int id, String newPlainPassword) {

        String sql =
            "UPDATE " + T_USERS + " " +
            "SET password = ? " +
            "WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, PasswordUtils.hash(newPlainPassword));
            ps.setLong(2, (long) id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.updatePasswordAdmin error", e);
        }
    }

    /* ===================== DELETE / DISABLE (NEW) ===================== */

    /**
     * Khuyến nghị: "xóa mềm" để tránh lỗi FK (orders, tokens, ...)
     * Đổi active = 0.
     */
    public boolean disableById(int id) {

        String sql = "UPDATE " + T_USERS + " SET active = 0 WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, (long) id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.disableById error", e);
        }
    }

    /**
     * Xóa thật user khỏi DB.
     * Chỉ dùng nếu chắc chắn không bị FK (hoặc DB đã ON DELETE CASCADE).
     */
    public boolean deleteById(int id) {

        String sql = "DELETE FROM " + T_USERS + " WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, (long) id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.deleteById error", e);
        }
    }

    /* ===================== OLD METHODS (KEEP IF YOU STILL USE) ===================== */

    public void updateRole(int id, String role) {

        String sql = "UPDATE " + T_USERS + " SET role = ? WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, role);
            ps.setLong(2, (long) id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.updateRole error", e);
        }
    }

    public void toggleLock(int id) {
        throw new UnsupportedOperationException(
            "toggleLock chưa hỗ trợ vì chưa xác nhận dbo.users có cột is_locked."
        );
    }

    /* ===================== MAPPER ===================== */

    private User mapRow(ResultSet rs) throws SQLException {

        User u = new User();

        long rawId = rs.getLong("id");
        if (rawId > Integer.MAX_VALUE || rawId < Integer.MIN_VALUE) {
            throw new RuntimeException("User id vượt quá giới hạn int: " + rawId);
        }

        u.setId((int) rawId);
        u.setUsername(rs.getString("username"));
        u.setRole(rs.getString("role"));

        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setActive(rs.getBoolean("active"));
        u.setCreatedAt(rs.getTimestamp("created_at"));

        return u;
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object v = params.get(i);
            int idx = i + 1;

            if (v == null) {
                ps.setNull(idx, Types.VARCHAR);
                continue;
            }

            if (v instanceof Integer) ps.setInt(idx, (Integer) v);
            else if (v instanceof Long) ps.setLong(idx, (Long) v);
            else ps.setString(idx, String.valueOf(v));
        }
    }
}

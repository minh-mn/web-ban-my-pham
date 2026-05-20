package com.webshop.app.dao;

import com.webshop.app.model.User;
import com.webshop.app.utils.DBConnection;
import com.webshop.app.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /* ================= INTERNAL MAPPER (users table) ================= */
    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();

        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setRole(rs.getString("role"));

        try {
            u.setFullName(rs.getString("full_name"));
        } catch (SQLException ignored) {
        }

        try {
            u.setEmail(rs.getString("email"));
        } catch (SQLException ignored) {
        }

        try {
            u.setPhone(rs.getString("phone"));
        } catch (SQLException ignored) {
        }

        try {
            u.setActive(rs.getBoolean("active"));
        } catch (SQLException ignored) {
        }

        try {
            u.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (SQLException ignored) {
        }

        return u;
    }

    /* ================= FIND BY USERNAME (users) ================= */
    public User findByUsername(String username) {
        String sql = "SELECT id, username, role, full_name, email, phone, active, created_at "
                + "FROM users WHERE username = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return mapUser(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.findByUsername error", e);
        }
    }

    /* ================= FIND BY ID (users.id) ================= */
    public User findById(int id) {
        String sql = "SELECT id, username, role, full_name, email, phone, active, created_at "
                + "FROM users WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return mapUser(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.findById error", e);
        }
    }

    /* ================= FIND ALL ================= */
    public List<User> findAll() {
        String sql = "SELECT id, username, role, full_name, email, phone, active, created_at "
                + "FROM users ORDER BY id DESC";

        List<User> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapUser(rs));
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.findAll error", e);
        }
    }

    /* ================= LOGIN ================= */
    public User login(String username, String plainPassword) {
        String sql = "SELECT id, username, password, role, full_name, email, phone, active, created_at "
                + "FROM users WHERE username = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                if (!rs.getBoolean("active")) {
                    return null;
                }

                String hashed = rs.getString("password");

                if (!PasswordUtils.verify(plainPassword, hashed)) {
                    return null;
                }

                return mapUser(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.login error", e);
        }
    }

    /* ================= CHECK PASSWORD (BY USERNAME) ================= */
    public boolean checkPasswordByUsername(String username, String plainPassword) {
        String sql = "SELECT password FROM users WHERE username = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                return PasswordUtils.verify(plainPassword, rs.getString("password"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.checkPasswordByUsername error", e);
        }
    }

    /* ================= CHECK PASSWORD (BY users.id) ================= */
    public boolean checkPassword(int userId, String plainPassword) {
        String sql = "SELECT password FROM users WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                return PasswordUtils.verify(plainPassword, rs.getString("password"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.checkPassword error", e);
        }
    }

    /* ================= UPDATE PASSWORD ================= */
    public void updatePassword(int userId, String newPlainPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, PasswordUtils.hash(newPlainPassword));
            ps.setInt(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updatePassword error", e);
        }
    }

    public boolean updatePasswordAdmin(int userId, String newPlainPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, PasswordUtils.hash(newPlainPassword));
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updatePasswordAdmin error", e);
        }
    }

    /* ================= UPDATE INFO (ADMIN) ================= */
    public boolean updateInfoAdmin(User u) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, role = ?, active = ? WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, u.getFullName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPhone());
            ps.setString(4, u.getRole());
            ps.setBoolean(5, u.isActive());
            ps.setInt(6, u.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updateInfoAdmin error", e);
        }
    }

    /* ================= REGISTER ================= */
    public void create(User u, String plainPassword) {
        String sqlUsers = "INSERT INTO users (username, password, role, full_name, email, phone, active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlUsers)) {

            ps.setString(1, u.getUsername());
            ps.setString(2, PasswordUtils.hash(plainPassword));
            ps.setString(3, u.getRole() != null ? u.getRole() : "USER");
            ps.setString(4, u.getFullName());
            ps.setString(5, u.getEmail());
            ps.setString(6, u.getPhone());
            ps.setBoolean(7, u.isActive());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.create error", e);
        }
    }

    /* ================= FIND BY EMAIL ================= */
    public User findByEmail(String email) {
        String sql = "SELECT id, username, role, full_name, email, phone, active, created_at "
                + "FROM users WHERE email = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                if (!rs.getBoolean("active")) {
                    return null;
                }

                return mapUser(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.findByEmail error", e);
        }
    }

    public boolean updateContact(int userId, String email, String phone) {
        String sql = "UPDATE users SET email = ?, phone = ? WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, phone);
            ps.setInt(3, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updateContact error", e);
        }
    }

    /* ================= SOCIAL LOGIN METHODS ================= */
    public User findBySocialId(String provider, String socialId) {
        String column = "google".equals(provider) ? "google_id" : "facebook_id";

        String sql = "SELECT id, username, role, full_name, email, phone, active, created_at "
                + "FROM users WHERE " + column + " = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, socialId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return mapUser(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.findBySocialId error", e);
        }
    }

    public void saveSocialUser(User u, String provider, String socialId) {
        String column = "google".equals(provider) ? "google_id" : "facebook_id";

        String sql = "INSERT INTO users (username, role, full_name, email, active, " + column + ") "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, u.getUsername());
            ps.setString(2, "USER");
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getEmail());
            ps.setBoolean(5, true);
            ps.setString(6, socialId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.saveSocialUser error", e);
        }
    }

    public void updateSocialId(int userId, String provider, String socialId) {
        String column = "google".equals(provider) ? "google_id" : "facebook_id";

        String sql = "UPDATE users SET " + column + " = ? WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, socialId);
            ps.setInt(2, userId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updateSocialId error", e);
        }
    }

    /*
     * Hàm insert này giữ lại để tương thích với các servlet cũ nếu còn gọi.
     * Password được hash tại đây để tránh lưu mật khẩu thô vào database.
     */
    public boolean insert(User user) {
        String sql = "INSERT INTO users (username, password, role, full_name, email, phone, active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, PasswordUtils.hash(user.getPassword()));
            ps.setString(3, user.getRole() != null ? user.getRole() : "USER");
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            ps.setBoolean(7, user.isActive());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.insert error", e);
        }
    }
}
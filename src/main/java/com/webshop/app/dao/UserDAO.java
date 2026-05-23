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

    private static final String DEFAULT_ROLE = "USER";

    /*
     * Các cột user dùng chung cho những câu SELECT.
     * Có manual_rank_code để account/checkout/admin nhận đúng rank admin gán thủ công.
     */
    private static final String USER_COLUMNS = """
            id,
            username,
            password,
            role,
            full_name,
            email,
            phone,
            active,
            created_at,
            google_id,
            facebook_id,
            birth_date,
            gender,
            manual_rank_code
            """;

    /* ================= INTERNAL MAPPER (users table) ================= */

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setRole(rs.getString("role"));

        /*
         * Bọc try-catch để an toàn nếu một vài câu SELECT cũ/rút gọn
         * không lấy đủ toàn bộ cột.
         */
        try {
            user.setPassword(rs.getString("password"));
        } catch (SQLException ignored) {
        }

        try {
            user.setFullName(rs.getString("full_name"));
        } catch (SQLException ignored) {
        }

        try {
            user.setEmail(rs.getString("email"));
        } catch (SQLException ignored) {
        }

        try {
            user.setPhone(rs.getString("phone"));
        } catch (SQLException ignored) {
        }

        try {
            user.setActive(rs.getBoolean("active"));
        } catch (SQLException ignored) {
        }

        try {
            user.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (SQLException ignored) {
        }

        try {
            user.setGoogleId(rs.getString("google_id"));
        } catch (SQLException ignored) {
        }

        try {
            user.setFacebookId(rs.getString("facebook_id"));
        } catch (SQLException ignored) {
        }

        try {
            if (rs.getDate("birth_date") != null) {
                user.setBirthDate(rs.getDate("birth_date").toString());
            }
        } catch (SQLException ignored) {
        }

        try {
            user.setGender(rs.getString("gender"));
        } catch (SQLException ignored) {
        }

        /*
         * Quan trọng cho Ưu tiên 2:
         * Admin gán manual_rank_code thì User object phải giữ được giá trị này.
         */
        try {
            user.setManualRankCode(normalizeRankCode(rs.getString("manual_rank_code")));
        } catch (SQLException ignored) {
        }

        return user;
    }

    /* ================= FIND BY USERNAME (users) ================= */

    public User findByUsername(String username) {
        String sql = """
                SELECT %s
                FROM users
                WHERE username = ?
                """.formatted(USER_COLUMNS);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapUser(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.findByUsername error", e);
        }
    }

    /* ================= FIND BY ID (users.id) ================= */

    public User findById(int id) {
        String sql = """
                SELECT %s
                FROM users
                WHERE id = ?
                """.formatted(USER_COLUMNS);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapUser(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.findById error", e);
        }
    }

    /* ================= FIND ALL ================= */

    public List<User> findAll() {
        String sql = """
                SELECT %s
                FROM users
                ORDER BY id DESC
                """.formatted(USER_COLUMNS);

        List<User> users = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.findAll error", e);
        }
    }

    /* ================= LOGIN ================= */

    public User login(String username, String plainPassword) {
        String sql = """
                SELECT %s
                FROM users
                WHERE username = ?
                """.formatted(USER_COLUMNS);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                if (!resultSet.getBoolean("active")) {
                    return null;
                }

                String hashedPassword = resultSet.getString("password");

                if (!PasswordUtils.verify(plainPassword, hashedPassword)) {
                    return null;
                }

                return mapUser(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.login error", e);
        }
    }

    /* ================= CHECK PASSWORD (BY USERNAME) ================= */

    public boolean checkPasswordByUsername(String username, String plainPassword) {
        String sql = """
                SELECT password
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }

                return PasswordUtils.verify(plainPassword, resultSet.getString("password"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.checkPasswordByUsername error", e);
        }
    }

    /* ================= CHECK PASSWORD (BY users.id) ================= */

    public boolean checkPassword(int userId, String plainPassword) {
        String sql = """
                SELECT password
                FROM users
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }

                return PasswordUtils.verify(plainPassword, resultSet.getString("password"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.checkPassword error", e);
        }
    }

    /* ================= UPDATE PASSWORD ================= */

    public void updatePassword(int userId, String newPlainPassword) {
        String sql = """
                UPDATE users
                SET password = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PasswordUtils.hash(newPlainPassword));
            statement.setInt(2, userId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updatePassword error", e);
        }
    }

    public boolean updatePasswordAdmin(int userId, String newPlainPassword) {
        String sql = """
                UPDATE users
                SET password = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PasswordUtils.hash(newPlainPassword));
            statement.setInt(2, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updatePasswordAdmin error", e);
        }
    }

    /* ================= UPDATE INFO (ADMIN) ================= */

    public boolean updateInfoAdmin(User user) {
        String sql = """
                UPDATE users
                SET full_name = ?,
                    email = ?,
                    phone = ?,
                    role = ?,
                    active = ?,
                    manual_rank_code = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setString(4, user.getRole());
            statement.setBoolean(5, user.isActive());
            statement.setString(6, normalizeNullableRankCode(user.getManualRankCode()));
            statement.setInt(7, user.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updateInfoAdmin error", e);
        }
    }

    /*
     * Dùng khi chỉ muốn cập nhật rank thủ công mà không đụng thông tin user khác.
     */
    public boolean updateManualRankCode(int userId, String manualRankCode) {
        String sql = """
                UPDATE users
                SET manual_rank_code = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeNullableRankCode(manualRankCode));
            statement.setInt(2, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updateManualRankCode error", e);
        }
    }

    /*
     * Lấy manual rank trực tiếp theo userId.
     * CheckoutService/UserRankService có thể dùng hàm này để ưu tiên rank admin gán.
     */
    public String findManualRankCodeByUserId(int userId) {
        String sql = """
                SELECT manual_rank_code
                FROM users
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return normalizeNullableRankCode(resultSet.getString("manual_rank_code"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.findManualRankCodeByUserId error", e);
        }
    }

    /* ================= REGISTER ================= */

    public void create(User user, String plainPassword) {
        String sql = """
                INSERT INTO users
                (
                    username,
                    password,
                    role,
                    full_name,
                    email,
                    phone,
                    active,
                    manual_rank_code
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, PasswordUtils.hash(plainPassword));
            statement.setString(3, user.getRole() != null ? user.getRole() : DEFAULT_ROLE);
            statement.setString(4, user.getFullName());
            statement.setString(5, user.getEmail());
            statement.setString(6, user.getPhone());
            statement.setBoolean(7, user.isActive());
            statement.setString(8, normalizeNullableRankCode(user.getManualRankCode()));

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.create error", e);
        }
    }

    /* ================= FIND BY EMAIL ================= */

    public User findByEmail(String email) {
        String sql = """
                SELECT %s
                FROM users
                WHERE email = ?
                """.formatted(USER_COLUMNS);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                if (!resultSet.getBoolean("active")) {
                    return null;
                }

                return mapUser(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.findByEmail error", e);
        }
    }

    public boolean updateContact(int userId, String email, String phone) {
        String sql = """
                UPDATE users
                SET email = ?,
                    phone = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setString(2, phone);
            statement.setInt(3, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updateContact error", e);
        }
    }

    /* ================= SOCIAL LOGIN METHODS ================= */

    public User findBySocialId(String provider, String socialId) {
        String column = "google".equalsIgnoreCase(provider) ? "google_id" : "facebook_id";

        String sql = """
                SELECT %s
                FROM users
                WHERE %s = ?
                """.formatted(USER_COLUMNS, column);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, socialId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapUser(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.findBySocialId error", e);
        }
    }

    public void saveSocialUser(User user, String provider, String socialId) {
        String column = "google".equalsIgnoreCase(provider) ? "google_id" : "facebook_id";

        String sql = """
                INSERT INTO users
                (
                    username,
                    role,
                    full_name,
                    email,
                    active,
                    %s,
                    manual_rank_code
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """.formatted(column);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, DEFAULT_ROLE);
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getEmail());
            statement.setBoolean(5, true);
            statement.setString(6, socialId);
            statement.setString(7, normalizeNullableRankCode(user.getManualRankCode()));

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.saveSocialUser error", e);
        }
    }

    public void updateSocialId(int userId, String provider, String socialId) {
        String column = "google".equalsIgnoreCase(provider) ? "google_id" : "facebook_id";

        String sql = """
                UPDATE users
                SET %s = ?
                WHERE id = ?
                """.formatted(column);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, socialId);
            statement.setInt(2, userId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updateSocialId error", e);
        }
    }

    /*
     * Hàm insert này giữ lại để tương thích với các servlet cũ nếu còn gọi.
     * Password được hash tại đây để tránh lưu mật khẩu thô vào database.
     */
    public boolean insert(User user) {
        String sql = """
                INSERT INTO users
                (
                    username,
                    password,
                    full_name,
                    email,
                    phone,
                    role,
                    active,
                    google_id,
                    facebook_id,
                    birth_date,
                    gender,
                    manual_rank_code
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, PasswordUtils.hash(user.getPassword()));
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getRole() != null ? user.getRole() : DEFAULT_ROLE);
            statement.setBoolean(7, user.isActive());

            statement.setString(8, null);
            statement.setString(9, null);

            if (user.getBirthDate() != null && !user.getBirthDate().isBlank()) {
                statement.setDate(10, java.sql.Date.valueOf(user.getBirthDate()));
            } else {
                statement.setNull(10, java.sql.Types.DATE);
            }

            statement.setString(11, user.getGender());
            statement.setString(12, normalizeNullableRankCode(user.getManualRankCode()));

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.insert error", e);
        }
    }

    /* ================= HELPERS ================= */

    private String normalizeRankCode(String rankCode) {
        if (rankCode == null || rankCode.isBlank()) {
            return null;
        }

        String normalized = rankCode.trim().toUpperCase();

        return switch (normalized) {
            case "MEMBER", "SILVER", "GOLD", "DIAMOND", "VIP" -> normalized;
            default -> null;
        };
    }

    private String normalizeNullableRankCode(String rankCode) {
        return normalizeRankCode(rankCode);
    }
}
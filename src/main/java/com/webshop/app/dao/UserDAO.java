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

    /* ================= INTERNAL MAPPER ================= */

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setRole(rs.getString("role"));

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

        try {
            user.setManualRankCode(normalizeRankCode(rs.getString("manual_rank_code")));
        } catch (SQLException ignored) {
        }

        return user;
    }

    /* ================= FIND BY USERNAME ================= */

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

    /* ================= FIND BY ID ================= */

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

    /* ================= CHECK PASSWORD ================= */

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
        if (userId <= 0 || newPlainPassword == null || newPlainPassword.isBlank()) {
            return;
        }

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

    /*
     * Giữ lại để tương thích code cũ.
     * AdminUserServlet mới đã chặn admin đổi mật khẩu tài khoản khác.
     */
    public boolean updatePasswordAdmin(int userId, String newPlainPassword) {
        if (userId <= 0 || newPlainPassword == null || newPlainPassword.isBlank()) {
            return false;
        }

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

    /* ================= UPDATE INFO ADMIN - ISSUE 130 ================= */

    /*
     * Issue 130:
     * Tên hàm giữ nguyên để không làm vỡ code cũ.
     *
     * Nhưng hàm này KHÔNG cập nhật thông tin cá nhân nữa:
     * - full_name
     * - email
     * - phone
     *
     * Admin chỉ được cập nhật các trường quản trị:
     * - role
     * - active
     * - manual_rank_code
     *
     * Nếu user yêu cầu sửa thông tin cá nhân, nên xử lý bằng luồng request riêng
     * hoặc để user tự sửa trong trang tài khoản.
     */
    public boolean updateInfoAdmin(User user) {
        if (user == null || user.getId() <= 0) {
            return false;
        }

        String sql = """
                UPDATE users
                SET role = ?,
                    active = ?,
                    manual_rank_code = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeRole(user.getRole()));
            statement.setBoolean(2, user.isActive());
            statement.setString(3, normalizeNullableRankCode(user.getManualRankCode()));
            statement.setInt(4, user.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updateInfoAdmin error", e);
        }
    }

    /*
     * Dùng khi chỉ muốn cập nhật rank thủ công mà không đụng thông tin user khác.
     * Không cập nhật rank thủ công cho tài khoản ADMIN ở tầng DAO.
     */
    public boolean updateManualRankCode(int userId, String manualRankCode) {
        if (userId <= 0) {
            return false;
        }

        String sql = """
                UPDATE users
                SET manual_rank_code = ?
                WHERE id = ?
                  AND role <> 'ADMIN'
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

    /* ================= USER SELF UPDATE ================= */

    /*
     * User tự cập nhật thông tin cá nhân.
     * Hàm này phục vụ trang tài khoản của chính user, không dùng cho admin sửa tùy ý.
     */
    public boolean updateProfileSelf(int userId,
                                     String fullName,
                                     String email,
                                     String phone,
                                     String birthDate,
                                     String gender) {
        if (userId <= 0) {
            return false;
        }

        String sql = """
                UPDATE users
                SET full_name = ?,
                    email = ?,
                    phone = ?,
                    birth_date = ?,
                    gender = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, nullify(fullName));
            statement.setString(2, nullify(email));
            statement.setString(3, nullify(phone));

            String normalizedBirthDate = nullify(birthDate);
            if (normalizedBirthDate != null) {
                statement.setDate(4, java.sql.Date.valueOf(normalizedBirthDate));
            } else {
                statement.setNull(4, java.sql.Types.DATE);
            }

            statement.setString(5, normalizeGender(gender));
            statement.setInt(6, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updateProfileSelf error", e);
        }
    }

    public boolean updateContact(int userId, String email, String phone) {
        if (userId <= 0) {
            return false;
        }

        String sql = """
                UPDATE users
                SET email = ?,
                    phone = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, nullify(email));
            statement.setString(2, nullify(phone));
            statement.setInt(3, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updateContact error", e);
        }
    }

    public boolean updateFullName(int userId, String fullName) {
        if (userId <= 0) {
            return false;
        }

        String sql = """
                UPDATE users
                SET full_name = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, nullify(fullName));
            statement.setInt(2, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updateFullName error", e);
        }
    }

    /* ================= ACCOUNT STATUS ================= */

    public boolean setActive(int userId, boolean active) {
        if (userId <= 0) {
            return false;
        }

        String sql = """
                UPDATE users
                SET active = ?
                WHERE id = ?
                  AND role <> 'ADMIN'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBoolean(1, active);
            statement.setInt(2, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.setActive error", e);
        }
    }

    public boolean disableById(int userId) {
        return setActive(userId, false);
    }

    public boolean enableById(int userId) {
        return setActive(userId, true);
    }

    /*
     * Giữ tên hàm để tương thích code cũ.
     * Không xóa cứng user, chỉ khóa mềm bằng active = 0.
     */
    public boolean deleteById(int userId) {
        return disableById(userId);
    }

    public boolean isAdminUser(int userId) {
        if (userId <= 0) {
            return false;
        }

        String sql = """
                SELECT role
                FROM users
                WHERE id = ?
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && "ADMIN".equalsIgnoreCase(resultSet.getString("role"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.isAdminUser error", e);
        }
    }

    public boolean existsById(int userId) {
        if (userId <= 0) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM users
                WHERE id = ?
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.existsById error", e);
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
            statement.setString(3, normalizeRole(user.getRole()));
            statement.setString(4, nullify(user.getFullName()));
            statement.setString(5, nullify(user.getEmail()));
            statement.setString(6, nullify(user.getPhone()));
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
            statement.setString(3, nullify(user.getFullName()));
            statement.setString(4, nullify(user.getEmail()));
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
            statement.setString(3, nullify(user.getFullName()));
            statement.setString(4, nullify(user.getEmail()));
            statement.setString(5, nullify(user.getPhone()));
            statement.setString(6, normalizeRole(user.getRole()));
            statement.setBoolean(7, user.isActive());

            statement.setString(8, null);
            statement.setString(9, null);

            if (user.getBirthDate() != null && !user.getBirthDate().isBlank()) {
                statement.setDate(10, java.sql.Date.valueOf(user.getBirthDate()));
            } else {
                statement.setNull(10, java.sql.Types.DATE);
            }

            statement.setString(11, normalizeGender(user.getGender()));
            statement.setString(12, normalizeNullableRankCode(user.getManualRankCode()));

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.insert error", e);
        }
    }

    /* ================= HELPERS ================= */

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return DEFAULT_ROLE;
        }

        String normalized = role.trim().toUpperCase();

        if (!"ADMIN".equals(normalized) && !"USER".equals(normalized)) {
            return DEFAULT_ROLE;
        }

        return normalized;
    }

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

    private String normalizeGender(String gender) {
        String value = nullify(gender);

        if (value == null) {
            return null;
        }

        value = value.toUpperCase();

        return switch (value) {
            case "MALE", "FEMALE", "OTHER", "NAM", "NU", "NỮ", "KHAC", "KHÁC" -> value;
            default -> null;
        };
    }

    private String nullify(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

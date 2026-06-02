package com.webshop.app.dao;

import com.webshop.app.model.User;
import com.webshop.app.utils.DBConnection;
import com.webshop.app.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class AdminUserDAO {

    private static final String T_USERS = "users";

    public List<User> search(String q, String role, String rank, Integer active) {
        List<User> users = new ArrayList<>();

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT ");
        sql.append("id, username, password, role, full_name, email, phone, active, ");
        sql.append("manual_rank_code, created_at ");
        sql.append("FROM ").append(T_USERS).append(" ");
        sql.append("WHERE 1 = 1 ");

        List<Object> params = new ArrayList<>();

        if (q != null && !q.isBlank()) {
            sql.append("AND (");
            sql.append("CAST(id AS CHAR) LIKE ? ");
            sql.append("OR username LIKE ? ");
            sql.append("OR full_name LIKE ? ");
            sql.append("OR email LIKE ? ");
            sql.append("OR phone LIKE ? ");
            sql.append(") ");

            String keyword = "%" + q.trim() + "%";

            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        String normalizedRole = normalizeRoleFilter(role);
        if (normalizedRole != null) {
            sql.append("AND role = ? ");
            params.add(normalizedRole);
        }

        /*
         * rank = null / blank / ALL  => không lọc rank
         * rank = AUTO                => user chưa được admin set rank thủ công
         * rank = MEMBER/GOLD/VIP...  => user được admin set rank thủ công tương ứng
         */
        if (rank != null && !rank.isBlank() && !"ALL".equalsIgnoreCase(rank)) {
            if ("AUTO".equalsIgnoreCase(rank)) {
                sql.append("AND manual_rank_code IS NULL ");
            } else {
                sql.append("AND manual_rank_code = ? ");
                params.add(rank.trim().toUpperCase());
            }
        }

        if (active != null) {
            sql.append("AND active = ? ");
            params.add(active);
        }

        sql.append("ORDER BY ");
        sql.append("CASE WHEN role = 'ADMIN' THEN 0 ELSE 1 END ASC, ");
        sql.append("id DESC");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            bind(statement, params);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapRow(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.search error", e);
        }

        return users;
    }

    public User findById(int id) {
        String sql = """
                SELECT
                    id,
                    username,
                    password,
                    role,
                    full_name,
                    email,
                    phone,
                    active,
                    manual_rank_code,
                    created_at
                FROM users
                WHERE id = ?
                LIMIT 1
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
            throw new RuntimeException("AdminUserDAO.findById error", e);
        }

        return null;
    }

    /*
     * Issue 130:
     * Tên hàm giữ nguyên để tương thích AdminUserServlet/user_form.jsp cũ.
     * Tuy nhiên DAO không cập nhật full_name, email, phone nữa.
     *
     * Admin chỉ được cập nhật các trường quản trị:
     * - role
     * - active
     * - manual_rank_code
     *
     * Thông tin cá nhân của user phải do user tự chỉnh hoặc xử lý qua luồng yêu cầu riêng.
     */
    public boolean updateInfoAdmin(User user) {
        if (user == null || user.getId() <= 0) {
            return false;
        }

        String sql = """
                UPDATE users
                SET
                    role = ?,
                    active = ?,
                    manual_rank_code = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeRole(user.getRole()));
            statement.setInt(2, user.isActive() ? 1 : 0);
            setNullableRank(statement, 3, user.getManualRankCode());
            statement.setInt(4, user.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.updateInfoAdmin error", e);
        }
    }

    /*
     * Chỉ nên gọi khi đổi mật khẩu chính tài khoản đang đăng nhập
     * hoặc sau này có luồng user yêu cầu reset mật khẩu riêng.
     */
    public boolean updatePasswordAdmin(int id, String newPlainPassword) {
        if (id <= 0 || newPlainPassword == null || newPlainPassword.isBlank()) {
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
            statement.setInt(2, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.updatePasswordAdmin error", e);
        }
    }

    /*
     * Không cho đổi rank thủ công của tài khoản ADMIN ở tầng DAO.
     */
    public boolean updateManualRank(int id, String manualRankCode) {
        if (id <= 0) {
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

            setNullableRank(statement, 1, manualRankCode);
            statement.setInt(2, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.updateManualRank error", e);
        }
    }

    /*
     * Khóa tài khoản mềm bằng active = 0.
     * Không xóa dữ liệu để bảo toàn đơn hàng, bình luận, lịch sử thanh toán.
     * Không khóa tài khoản ADMIN ở tầng DAO.
     */
    public boolean disableById(int id) {
        if (id <= 0) {
            return false;
        }

        String sql = """
                UPDATE users
                SET active = 0
                WHERE id = ?
                  AND role <> 'ADMIN'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.disableById error", e);
        }
    }

    /*
     * Giữ tên hàm để không vỡ code cũ.
     * Không DELETE cứng nữa, chỉ chuyển thành khóa tài khoản.
     */
    public boolean deleteById(int id) {
        return disableById(id);
    }

    /*
     * Cho phép đổi role USER <-> ADMIN với user thường.
     * Không cho cập nhật trực tiếp tài khoản đang là ADMIN ở tầng DAO.
     * Servlet sẽ kiểm tra thêm không cho admin tự đổi role chính mình.
     */
    public boolean updateRole(int id, String role) {
        if (id <= 0) {
            return false;
        }

        String normalizedRole = normalizeRole(role);

        String sql = """
                UPDATE users
                SET role = ?
                WHERE id = ?
                  AND role <> 'ADMIN'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizedRole);
            statement.setInt(2, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.updateRole error", e);
        }
    }

    /*
     * Bảng users đang dùng active nên toggleLock sẽ toggle active.
     * active = 1: mở tài khoản
     * active = 0: khóa tài khoản
     *
     * Không thao tác với tài khoản ADMIN ở tầng DAO.
     */
    public boolean toggleLock(int id) {
        if (id <= 0) {
            return false;
        }

        String sql = """
                UPDATE users
                SET active = CASE WHEN active = 1 THEN 0 ELSE 1 END
                WHERE id = ?
                  AND role <> 'ADMIN'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.toggleLock error", e);
        }
    }

    public boolean setActive(int id, boolean active) {
        if (id <= 0) {
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

            statement.setInt(1, active ? 1 : 0);
            statement.setInt(2, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.setActive error", e);
        }
    }

    public boolean existsById(int id) {
        if (id <= 0) {
            return false;
        }

        String sql = "SELECT 1 FROM users WHERE id = ? LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.existsById error", e);
        }
    }

    public boolean isAdminUser(int id) {
        if (id <= 0) {
            return false;
        }

        String sql = "SELECT role FROM users WHERE id = ? LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && "ADMIN".equalsIgnoreCase(resultSet.getString("role"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.isAdminUser error", e);
        }
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        User user = new User();

        user.setId(resultSet.getInt("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        user.setRole(resultSet.getString("role"));
        user.setFullName(resultSet.getString("full_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPhone(resultSet.getString("phone"));
        user.setActive(resultSet.getBoolean("active"));
        user.setManualRankCode(resultSet.getString("manual_rank_code"));
        user.setCreatedAt(resultSet.getTimestamp("created_at"));

        return user;
    }

    private void bind(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            int parameterIndex = i + 1;

            if (value == null) {
                statement.setNull(parameterIndex, Types.VARCHAR);
            } else if (value instanceof Integer) {
                statement.setInt(parameterIndex, (Integer) value);
            } else if (value instanceof Long) {
                statement.setLong(parameterIndex, (Long) value);
            } else {
                statement.setString(parameterIndex, String.valueOf(value));
            }
        }
    }

    private void setNullableRank(PreparedStatement statement,
                                 int index,
                                 String manualRankCode) throws SQLException {

        if (manualRankCode == null
                || manualRankCode.isBlank()
                || "AUTO".equalsIgnoreCase(manualRankCode)) {

            statement.setNull(index, Types.VARCHAR);
            return;
        }

        statement.setString(index, manualRankCode.trim().toUpperCase());
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }

        String normalized = role.trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_");
        return normalized.isBlank() ? "USER" : normalized;
    }

    private String normalizeRoleFilter(String role) {
        if (role == null || role.isBlank() || "ALL".equalsIgnoreCase(role)) {
            return null;
        }

        String normalized = role.trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_");
        return normalized.isBlank() ? null : normalized;
    }
}

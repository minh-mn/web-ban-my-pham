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
        sql.append("SELECT id, username, role, full_name, email, phone, active, created_at ");
        sql.append("FROM ").append(T_USERS).append(" ");
        sql.append("WHERE 1 = 1 ");

        List<Object> params = new ArrayList<>();

        if (q != null && !q.isBlank()) {
            sql.append("AND (username LIKE ? OR full_name LIKE ? OR email LIKE ? OR phone LIKE ?) ");
            String keyword = "%" + q.trim() + "%";

            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        if (role != null && !role.isBlank()) {
            sql.append("AND role = ? ");
            params.add(role.trim());
        }

        if (active != null) {
            sql.append("AND active = ? ");
            params.add(active);
        }

        sql.append("ORDER BY id DESC");

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
                SELECT id, username, role, full_name, email, phone, active, created_at
                FROM users
                WHERE id = ?
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

    public boolean updateInfoAdmin(User user) {
        String sql = """
                UPDATE users
                SET full_name = ?, email = ?, phone = ?, role = ?, active = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setString(4, user.getRole());
            statement.setInt(5, user.isActive() ? 1 : 0);
            statement.setInt(6, user.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.updateInfoAdmin error", e);
        }
    }

    public boolean updatePasswordAdmin(int id, String newPlainPassword) {
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

    public boolean disableById(int id) {
        String sql = """
                UPDATE users
                SET active = 0
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.disableById error", e);
        }
    }

    public boolean deleteById(int id) {
        String sql = """
                DELETE FROM users
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.deleteById error", e);
        }
    }

    public void updateRole(int id, String role) {
        String sql = """
                UPDATE users
                SET role = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, role);
            statement.setInt(2, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("AdminUserDAO.updateRole error", e);
        }
    }

    public void toggleLock(int id) {
        throw new UnsupportedOperationException(
                "toggleLock chưa hỗ trợ vì bảng users chưa xác nhận có cột is_locked."
        );
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        User user = new User();

        user.setId(resultSet.getInt("id"));
        user.setUsername(resultSet.getString("username"));
        user.setRole(resultSet.getString("role"));
        user.setFullName(resultSet.getString("full_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPhone(resultSet.getString("phone"));
        user.setActive(resultSet.getBoolean("active"));
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
}
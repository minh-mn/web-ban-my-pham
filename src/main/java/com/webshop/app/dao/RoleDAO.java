package com.webshop.app.dao;

import com.webshop.app.model.Permission;
import com.webshop.app.model.Role;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RoleDAO {

    public List<Role> findAll() {
        String sql = """
                SELECT id, code, name, description, active, system_role, created_at, updated_at
                FROM roles
                ORDER BY system_role DESC, active DESC, code ASC
                """;

        List<Role> roles = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Role role = mapRole(resultSet);
                role.setPermissionCodes(new ArrayList<>(findPermissionCodesByRole(role.getCode())));
                roles.add(role);
            }

            return roles;

        } catch (SQLException e) {
            throw new RuntimeException("RoleDAO.findAll error", e);
        }
    }

    public List<Role> findAllActive() {
        String sql = """
                SELECT id, code, name, description, active, system_role, created_at, updated_at
                FROM roles
                WHERE active = 1
                ORDER BY system_role DESC, code ASC
                """;

        List<Role> roles = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                roles.add(mapRole(resultSet));
            }

            return roles;

        } catch (SQLException e) {
            throw new RuntimeException("RoleDAO.findAllActive error", e);
        }
    }

    public Role findByCode(String code) {
        String normalizedCode = normalizeCode(code);
        if (normalizedCode == null) {
            return null;
        }

        String sql = """
                SELECT id, code, name, description, active, system_role, created_at, updated_at
                FROM roles
                WHERE code = ?
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizedCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                Role role = mapRole(resultSet);
                role.setPermissionCodes(new ArrayList<>(findPermissionCodesByRole(role.getCode())));
                return role;
            }

        } catch (SQLException e) {
            throw new RuntimeException("RoleDAO.findByCode error", e);
        }
    }

    public boolean existsActiveRole(String code) {
        String normalizedCode = normalizeCode(code);
        if (normalizedCode == null) {
            return false;
        }

        String sql = "SELECT 1 FROM roles WHERE code = ? AND active = 1 LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizedCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("RoleDAO.existsActiveRole error", e);
        }
    }

    public List<Permission> findAllPermissions() {
        String sql = """
                SELECT id, code, name, module, description, created_at
                FROM permissions
                ORDER BY module ASC, code ASC
                """;

        List<Permission> permissions = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                permissions.add(mapPermission(resultSet));
            }

            return permissions;

        } catch (SQLException e) {
            throw new RuntimeException("RoleDAO.findAllPermissions error", e);
        }
    }

    public Set<String> findPermissionCodesByRole(String roleCode) {
        String normalizedRoleCode = normalizeCode(roleCode);
        Set<String> permissions = new LinkedHashSet<>();

        if (normalizedRoleCode == null) {
            return permissions;
        }

        String sql = """
                SELECT p.code
                FROM role_permissions rp
                JOIN roles r ON r.id = rp.role_id
                JOIN permissions p ON p.id = rp.permission_id
                WHERE r.code = ?
                  AND r.active = 1
                ORDER BY p.module ASC, p.code ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizedRoleCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    permissions.add(resultSet.getString("code"));
                }
            }

            return permissions;

        } catch (SQLException e) {
            throw new RuntimeException("RoleDAO.findPermissionCodesByRole error", e);
        }
    }

    public boolean create(Role role) {
        if (role == null || role.getCode() == null || role.getName() == null) {
            return false;
        }

        String sql = """
                INSERT INTO roles (code, name, description, active, system_role, created_at, updated_at)
                VALUES (?, ?, ?, ?, 0, NOW(), NOW())
                """;

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, normalizeCode(role.getCode()));
                statement.setString(2, role.getName());
                setNullableString(statement, 3, role.getDescription());
                statement.setInt(4, role.isActive() ? 1 : 0);

                int rows = statement.executeUpdate();
                if (rows <= 0) {
                    connection.rollback();
                    return false;
                }

                int roleId;
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        connection.rollback();
                        return false;
                    }
                    roleId = generatedKeys.getInt(1);
                }

                replacePermissions(connection, roleId, role.getPermissionCodes());
                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("RoleDAO.create error", e);
        }
    }

    public boolean update(Role role) {
        if (role == null || role.getCode() == null || role.getName() == null) {
            return false;
        }

        Role oldRole = findByCode(role.getCode());
        if (oldRole == null) {
            return false;
        }

        String sql = """
                UPDATE roles
                SET name = ?,
                    description = ?,
                    active = ?,
                    updated_at = NOW()
                WHERE code = ?
                """;

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, role.getName());
                setNullableString(statement, 2, role.getDescription());

                boolean keepActive = oldRole.isSystemRole() || role.isActive();
                statement.setInt(3, keepActive ? 1 : 0);
                statement.setString(4, role.getCode());

                int rows = statement.executeUpdate();
                if (rows <= 0) {
                    connection.rollback();
                    return false;
                }

                replacePermissions(connection, oldRole.getId(), role.getPermissionCodes());
                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("RoleDAO.update error", e);
        }
    }

    public boolean deactivate(String code) {
        String normalizedCode = normalizeCode(code);
        if (normalizedCode == null || "ADMIN".equals(normalizedCode) || "USER".equals(normalizedCode)) {
            return false;
        }

        String sql = """
                UPDATE roles
                SET active = 0, updated_at = NOW()
                WHERE code = ?
                  AND system_role = 0
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizedCode);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("RoleDAO.deactivate error", e);
        }
    }

    private void replacePermissions(Connection connection, int roleId, List<String> permissionCodes) throws SQLException {
        try (PreparedStatement deleteStatement = connection.prepareStatement(
                "DELETE FROM role_permissions WHERE role_id = ?")) {
            deleteStatement.setInt(1, roleId);
            deleteStatement.executeUpdate();
        }

        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return;
        }

        String insertSql = """
                INSERT IGNORE INTO role_permissions (role_id, permission_id)
                SELECT ?, id
                FROM permissions
                WHERE code = ?
                """;

        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            Set<String> uniqueCodes = new LinkedHashSet<>();
            for (String permissionCode : permissionCodes) {
                String normalizedCode = normalizeCode(permissionCode);
                if (normalizedCode != null) {
                    uniqueCodes.add(normalizedCode);
                }
            }

            for (String permissionCode : uniqueCodes) {
                insertStatement.setInt(1, roleId);
                insertStatement.setString(2, permissionCode);
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();
        }
    }

    private Role mapRole(ResultSet resultSet) throws SQLException {
        Role role = new Role();
        role.setId(resultSet.getInt("id"));
        role.setCode(resultSet.getString("code"));
        role.setName(resultSet.getString("name"));
        role.setDescription(resultSet.getString("description"));
        role.setActive(resultSet.getBoolean("active"));
        role.setSystemRole(resultSet.getBoolean("system_role"));
        role.setCreatedAt(resultSet.getTimestamp("created_at"));
        role.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return role;
    }

    private Permission mapPermission(ResultSet resultSet) throws SQLException {
        Permission permission = new Permission();
        permission.setId(resultSet.getInt("id"));
        permission.setCode(resultSet.getString("code"));
        permission.setName(resultSet.getString("name"));
        permission.setModule(resultSet.getString("module"));
        permission.setDescription(resultSet.getString("description"));
        permission.setCreatedAt(resultSet.getTimestamp("created_at"));
        return permission;
    }

    private void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value.trim());
        }
    }

    private String normalizeCode(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        return normalized.isEmpty() ? null : normalized;
    }
}

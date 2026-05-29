package com.webshop.app.dao;

import com.webshop.app.model.Policy;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PolicyDAO {

    public String getFileNameBySlug(String slug) {
        String sql = """
                SELECT file_name
                FROM policies
                WHERE slug = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, slug);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("file_name");
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("PolicyDAO.getFileNameBySlug error", e);
        }

        return null;
    }

    public String getFileNameById(int id) {
        String sql = """
                SELECT file_name
                FROM policies
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("file_name");
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("PolicyDAO.getFileNameById error", e);
        }

        return null;
    }

    public Policy getPolicyBySlug(String slug) {
        String sql = """
                SELECT id, title, file_name, slug
                FROM policies
                WHERE slug = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, slug);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("PolicyDAO.getPolicyBySlug error", e);
        }

        return null;
    }

    public Policy findById(int id) {
        String sql = """
                SELECT id, title, slug, file_name
                FROM policies
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

        } catch (Exception e) {
            throw new RuntimeException("PolicyDAO.findById error", e);
        }

        return null;
    }

    public void insert(Policy policy) {
        String sql = """
                INSERT INTO policies(title, slug, file_name)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, policy.getTitle());
            statement.setString(2, policy.getSlug());
            statement.setString(3, policy.getFileName());

            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("PolicyDAO.insert error", e);
        }
    }

    public boolean deleteById(int id) {
        String sql = """
                DELETE FROM policies
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("PolicyDAO.deleteById error", e);
        }
    }

    public List<Policy> getAllPolicies() {
        List<Policy> policies = new ArrayList<>();

        String sql = """
                SELECT id, title, slug, file_name
                FROM policies
                ORDER BY id ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                policies.add(mapRow(resultSet));
            }

        } catch (Exception e) {
            throw new RuntimeException("PolicyDAO.getAllPolicies error", e);
        }

        return policies;
    }

    private Policy mapRow(ResultSet resultSet) throws SQLException {
        Policy policy = new Policy();

        policy.setId(resultSet.getInt("id"));
        policy.setTitle(resultSet.getString("title"));
        policy.setSlug(resultSet.getString("slug"));
        policy.setFileName(resultSet.getString("file_name"));

        return policy;
    }
}
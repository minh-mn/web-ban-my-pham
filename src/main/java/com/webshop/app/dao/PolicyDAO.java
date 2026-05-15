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
        String fileName = null;
        String sql = "SELECT file_name FROM policies WHERE slug = ?";

        // Sử dụng Try-with-resources để tự động đóng kết nối
        try (Connection conn = new DBConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, slug);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                fileName = rs.getString("file_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    public Policy getPolicyBySlug(String slug) {

        String sql = "SELECT id, title, file_name, slug FROM policies WHERE slug = ?";

        try (Connection conn = new DBConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, slug);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Policy p = new Policy();
                p.setId(rs.getInt("id"));
                p.setTitle(rs.getString("title"));
                p.setFileName(rs.getString("file_name"));
                p.setSlug(rs.getString("slug"));

                return p;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void insert(Policy p) {
        String sql = "INSERT INTO policies(title, slug, file_name) VALUES (?, ?, ?)";

        try (Connection conn = new DBConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getTitle());
            ps.setString(2, p.getSlug());
            ps.setString(3, p.getFileName());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Policy> getAllPolicies() {

        List<Policy> list = new ArrayList<>();

        String sql = "SELECT * FROM policies";

        try (Connection conn = new DBConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Policy p = new Policy();
                p.setId(rs.getInt("id"));
                p.setTitle(rs.getString("title"));
                p.setSlug(rs.getString("slug"));
                p.setFileName(rs.getString("file_name"));

                list.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
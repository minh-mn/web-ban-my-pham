package com.webshop.app.model;

import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Policy {
    private int id;
    private String title;
    private String slug;
    private String fileName;

    public Policy() {}

    public Policy(int id, String title, String slug, String fileName) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.fileName = fileName;
    }

    // Getter và Setter (Bắt buộc phải có để JSP đọc được)
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public void setId(int id) {}

    public Policy getPolicyBySlug(String slug) {
        String sql = "SELECT id, title, file_name, slug FROM policies WHERE slug = ?";

        try (Connection conn = DBConnection.getConnection();
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
}
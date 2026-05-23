package com.webshop.app.dao;

import com.webshop.app.model.Page;
import com.webshop.app.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PageDAO {

    public List<Page> findAll() {

        String sql = "SELECT * FROM pages ORDER BY created_at DESC";

        List<Page> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Page p = new Page();
                p.setId(rs.getInt("id"));
                p.setTitle(rs.getString("title"));
                p.setSlug(rs.getString("slug"));
                p.setType(rs.getString("type"));

                list.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void save(Page p) {

        boolean isUpdate = p.getId() > 0;

        String sql;

        if (isUpdate) {
            sql = """
                UPDATE pages
                SET title=?, slug=?, content=?, thumbnail=?, type=?, updated_at=NOW()
                WHERE id=?
            """;
        } else {
            sql = """
                INSERT INTO pages(title, slug, content, thumbnail, type)
                VALUES (?, ?, ?, ?, ?)
            """;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getTitle());
            ps.setString(2, p.getSlug());
            ps.setString(3, p.getContent());
            ps.setString(4, p.getThumbnail());
            ps.setString(5, p.getType());

            if (isUpdate) {
                ps.setInt(6, p.getId());
            }

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Page findById(int id) {

        String sql = "SELECT * FROM pages WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Page p = new Page();
                p.setId(rs.getInt("id"));
                p.setTitle(rs.getString("title"));
                p.setSlug(rs.getString("slug"));
                p.setContent(rs.getString("content"));
                p.setThumbnail(rs.getString("thumbnail"));
                p.setType(rs.getString("type"));
                return p;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Page findBySlug(String slug) {

        String sql = "SELECT * FROM pages WHERE slug=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, slug);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Page p = new Page();
                p.setTitle(rs.getString("title"));
                p.setContent(rs.getString("content"));
                return p;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Page> getByType(String type) {

        String sql = "SELECT * FROM pages WHERE type=? ORDER BY id DESC";

        List<Page> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Page p = new Page();

                p.setId(rs.getInt("id"));
                p.setTitle(rs.getString("title"));
                p.setSlug(rs.getString("slug"));
                p.setContent(rs.getString("content"));
                p.setThumbnail(rs.getString("thumbnail"));
                p.setType(rs.getString("type"));

                list.add(p);
            }

        } catch (Exception e) {
            throw new RuntimeException("PageDAO.getByType error", e);
        }

        return list;
    }

    public List<Page> getFooterPages() {

        String sql = """
        SELECT id, title, slug, type
        FROM pages
        WHERE status='published'
          AND type='policy'
        ORDER BY id DESC
    """;

        List<Page> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Page p = new Page();

                p.setId(rs.getInt("id"));
                p.setTitle(rs.getString("title"));
                p.setSlug(rs.getString("slug"));
                p.setType(rs.getString("type"));

                list.add(p);
            }

        } catch (Exception e) {
            throw new RuntimeException("PageDAO.getFooterPages error", e);
        }

        return list;
    }
}
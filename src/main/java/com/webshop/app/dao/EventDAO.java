package com.webshop.app.dao;

import com.webshop.app.model.Event;
import com.webshop.app.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    // 1. Thêm sự kiện mới (Admin)
    public boolean insertEvent(Event event) {
        String sql = "INSERT INTO store_events (title, summary, tag, image_url, event_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, event.getTitle());
            ps.setString(2, event.getSummary());
            ps.setString(3, event.getTag());
            ps.setString(4, event.getImageUrl());
            if (event.getEventDate() != null) {
                ps.setTimestamp(5, new java.sql.Timestamp(event.getEventDate().getTime()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 2. Cập nhật thông tin sự kiện (Admin)
    public boolean updateEvent(Event event) {
        String sql = "UPDATE store_events SET title = ?, summary = ?, tag = ?, image_url = ?, event_date = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, event.getTitle());
            ps.setString(2, event.getSummary());
            ps.setString(3, event.getTag());
            ps.setString(4, event.getImageUrl());
            if (event.getEventDate() != null) {
                ps.setTimestamp(5, new java.sql.Timestamp(event.getEventDate().getTime()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }
            ps.setInt(6, event.getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 3. Xóa sự kiện theo ID (Admin)
    public boolean deleteEvent(int id) {
        String sql = "DELETE FROM store_events WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 4. Lấy chi tiết một sự kiện theo ID (Dùng khi bấm nút "Sửa")
    public Event getEventById(int id) {
        String sql = "SELECT * FROM store_events WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Event e = new Event();
                    e.setId(rs.getInt("id"));
                    e.setTitle(rs.getString("title"));
                    e.setSummary(rs.getString("summary"));
                    e.setTag(rs.getString("tag"));
                    e.setImageUrl(rs.getString("image_url"));
                    e.setEventDate(rs.getTimestamp("event_date"));
                    return e;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 5. Lấy toàn bộ danh sách sự kiện (Dành cho trang quản lý Admin)
    public List<Event> getAllEvents() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM store_events ORDER BY event_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Event e = new Event();
                e.setId(rs.getInt("id"));
                e.setTitle(rs.getString("title"));
                e.setSummary(rs.getString("summary"));
                e.setTag(rs.getString("tag"));
                e.setImageUrl(rs.getString("image_url"));
                e.setEventDate(rs.getTimestamp("event_date"));
                list.add(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 6. Lấy danh sách giới hạn sự kiện mới nhất (Hiển thị ngoài Trang Chủ người dùng)
    public List<Event> getRecentEvents(int limit) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM store_events ORDER BY event_date DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Event e = new Event();
                    e.setId(rs.getInt("id"));
                    e.setTitle(rs.getString("title"));
                    e.setSummary(rs.getString("summary"));
                    e.setTag(rs.getString("tag"));
                    e.setImageUrl(rs.getString("image_url"));
                    e.setEventDate(rs.getTimestamp("event_date"));
                    list.add(e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 7. Tìm kiếm sự kiện theo từ khóa (Tìm theo tiêu đề, mô tả hoặc tag)
    public List<Event> searchEvents(String searchQuery) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM store_events WHERE title LIKE ? OR summary LIKE ? OR tag LIKE ? ORDER BY event_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Xử lý chuỗi tìm kiếm, nếu null thì mặc định chuỗi rỗng để tìm tất cả
            String keyword = "%" + (searchQuery != null ? searchQuery.trim() : "") + "%";
            ps.setString(1, keyword);
            ps.setString(2, keyword);
            ps.setString(3, keyword);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Event e = new Event();
                    e.setId(rs.getInt("id"));
                    e.setTitle(rs.getString("title"));
                    e.setSummary(rs.getString("summary"));
                    e.setTag(rs.getString("tag"));
                    e.setImageUrl(rs.getString("image_url"));
                    e.setEventDate(rs.getTimestamp("event_date"));
                    list.add(e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
package com.webshop.app.dao;

import com.webshop.app.utils.DBConnection;

import java.sql.*;
import java.util.*;

public class WebsiteSettingDAO {

    public Map<String, String> getAllSettings() {

        Map<String, String> map = new HashMap<>();

        String sql = "SELECT setting_key, setting_value FROM website_settings";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getString("setting_key"),
                        rs.getString("setting_value"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    public void saveOrUpdate(String key, String value) {

        String sql = """
            INSERT INTO website_settings(setting_key, setting_value)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE setting_value = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, value);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getMapSettings() {

        Map<String, String> map = new HashMap<>();

        String sql = "SELECT setting_key, setting_value FROM website_settings";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getString("setting_key"),
                        rs.getString("setting_value"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }
}
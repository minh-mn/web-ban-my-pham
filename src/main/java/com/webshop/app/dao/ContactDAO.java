package com.webshop.app.dao;

import com.webshop.app.model.Contact;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ContactDAO {

    public void insert(Contact c) {

        String sql = """
            INSERT INTO contact_messages
            (full_name, email, phone, subject, message)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getFullName());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getSubject());
            ps.setString(5, c.getMessage());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("ContactDAO.insert error", e);
        }
    }

    public List<Contact> findAll() {

        List<Contact> list = new ArrayList<>();

        String sql = """
        SELECT * FROM contact_messages
        ORDER BY created_at DESC
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Contact c = new Contact();
                c.setId(rs.getInt("id"));
                c.setFullName(rs.getString("full_name"));
                c.setEmail(rs.getString("email"));
                c.setPhone(rs.getString("phone"));
                c.setSubject(rs.getString("subject"));
                c.setMessage(rs.getString("message"));
                c.setCreatedAt(rs.getTimestamp("created_at"));

                list.add(c);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}
package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.Review;
import com.mycosmeticshop.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    // ================= LOAD REVIEW THEO PRODUCT =================
    public List<Review> findByProductId(int productId) {

        List<Review> list = new ArrayList<>();

        String sql =
            "SELECT r.id, r.product_id, r.rating, r.comment, r.created_at, " +
            "       r.author_id, r.has_emoji, r.sentiment, " +
            "       u.username AS author_name " +
            "FROM store_review r " +
            "JOIN auth_user u ON r.author_id = u.id " +
            "WHERE r.product_id = ? " +
            "ORDER BY r.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setId(rs.getInt("id"));
                    r.setProductId(rs.getInt("product_id"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));

                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        r.setCreatedAt(ts.toLocalDateTime());
                    }

                    r.setAuthorId(rs.getInt("author_id"));
                    r.setAuthorName(rs.getString("author_name"));
                    r.setHasEmoji(rs.getBoolean("has_emoji"));
                    r.setSentiment(rs.getInt("sentiment"));

                    list.add(r);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("ReviewDAO.findByProductId error", e);
        }

        return list;
    }

    // ================= CHECK EXISTS (product_id + author_id) =================
    private boolean existsByProductAndAuthor(Connection conn, int productId, int authorId) throws SQLException {
        String sql = "SELECT 1 FROM store_review WHERE product_id = ? AND author_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, authorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ================= VALIDATE FK: author_id tồn tại =================
    private boolean existsAuthor(Connection conn, int authorId) throws SQLException {
        String sql = "SELECT 1 FROM auth_user WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ================= UPDATE REVIEW =================
    private void update(Connection conn, Review r) throws SQLException {

        String sql =
            "UPDATE store_review " +
            "SET rating = ?, comment = ?, has_emoji = ?, sentiment = ? " +
            "WHERE product_id = ? AND author_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getRating());
            ps.setString(2, r.getComment());
            ps.setBoolean(3, r.isHasEmoji());
            ps.setInt(4, r.getSentiment());
            ps.setInt(5, r.getProductId());
            ps.setInt(6, r.getAuthorId());
            ps.executeUpdate();
        }
    }

    // ================= INSERT REVIEW =================
    private void insert(Connection conn, Review r) throws SQLException {

        String sql =
            "INSERT INTO store_review " +
            "(product_id, author_id, rating, comment, created_at, has_emoji, sentiment) " +
            "VALUES (?, ?, ?, ?, GETDATE(), ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getProductId());
            ps.setInt(2, r.getAuthorId());
            ps.setInt(3, r.getRating());
            ps.setString(4, r.getComment());
            ps.setBoolean(5, r.isHasEmoji());
            ps.setInt(6, r.getSentiment());
            ps.executeUpdate();
        }
    }

    // ================= CREATE OR UPDATE (CHỐNG DUPLICATE UNIQUE) =================
    public void createOrUpdate(Review r) {

        if (r == null) {
            throw new RuntimeException("ReviewDAO.createOrUpdate error: review is null");
        }
        if (r.getProductId() <= 0) {
            throw new RuntimeException("ReviewDAO.createOrUpdate error: productId invalid");
        }
        if (r.getAuthorId() <= 0) {
            throw new RuntimeException("ReviewDAO.createOrUpdate error: authorId invalid");
        }

        try (Connection conn = DBConnection.getConnection()) {

            // ✅ validate FK trước khi insert/update để tránh SQL 500 khó đọc
            if (!existsAuthor(conn, r.getAuthorId())) {
                throw new RuntimeException(
                    "ReviewDAO.createOrUpdate error: author_id=" + r.getAuthorId() +
                    " không tồn tại trong auth_user. (Session user đang sai id)"
                );
            }

            if (existsByProductAndAuthor(conn, r.getProductId(), r.getAuthorId())) {
                update(conn, r);
            } else {
                insert(conn, r);
            }

        } catch (SQLException e) {
            throw new RuntimeException("ReviewDAO.createOrUpdate error", e);
        }
    }

    // ================= DELETE REVIEWS BY PRODUCT =================
    public int deleteByProductId(int productId) {

        String sql = "DELETE FROM store_review WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("ReviewDAO.deleteByProductId error", e);
        }
    }
}

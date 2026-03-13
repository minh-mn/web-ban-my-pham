package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.Review;
import com.mycosmeticshop.utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminReviewDAO {

    private static final String TABLE = "dbo.store_review";

    public List<Review> search(Integer rating, Long productId, Long authorId) {

        List<Review> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, rating, comment, created_at, author_id, product_id, has_emoji, sentiment ");
        sql.append("FROM ").append(TABLE).append(" WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (rating != null) {
            sql.append(" AND rating = ? ");
            params.add(rating);
        }
        if (productId != null) {
            sql.append(" AND product_id = ? ");
            params.add(productId);
        }
        if (authorId != null) {
            sql.append(" AND author_id = ? ");
            params.add(authorId);
        }

        sql.append(" ORDER BY created_at DESC, id DESC");

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("AdminReviewDAO.search error", e);
        }

        return list;
    }

    public Review findById(long id) {

        String sql =
            "SELECT id, rating, comment, created_at, author_id, product_id, has_emoji, sentiment " +
            "FROM " + TABLE + " WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("AdminReviewDAO.findById error", e);
        }

        return null;
    }

    public void delete(long id) {

        String sql = "DELETE FROM " + TABLE + " WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("AdminReviewDAO.delete error", e);
        }
    }

    /* ===================== MAPPER ===================== */

    private Review mapRow(ResultSet rs) throws SQLException {
        Review r = new Review();

        long id = rs.getLong("id");
        long authorId = rs.getLong("author_id");
        long productId = rs.getLong("product_id");

        // Model của bạn đang dùng int -> ép kiểu an toàn
        r.setId(safeToInt(id));
        r.setAuthorId(safeToInt(authorId));
        r.setProductId(safeToInt(productId));

        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        r.setHasEmoji(rs.getBoolean("has_emoji"));
        r.setSentiment(rs.getInt("sentiment"));

        // created_at là datetimeoffset -> ưu tiên lấy OffsetDateTime
        LocalDateTime created = null;
        try {
            OffsetDateTime odt = rs.getObject("created_at", OffsetDateTime.class);
            if (odt != null) created = odt.toLocalDateTime();
        } catch (Exception ignore) {
            // fallback nếu driver không hỗ trợ
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) created = ts.toLocalDateTime();
        }
        r.setCreatedAt(created);

        // authorName không có trong store_review schema -> để null
        return r;
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object v = params.get(i);
            int idx = i + 1;
            if (v instanceof Integer) ps.setInt(idx, (Integer) v);
            else if (v instanceof Long) ps.setLong(idx, (Long) v);
            else ps.setString(idx, String.valueOf(v));
        }
    }

    private int safeToInt(long v) {
        if (v > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (v < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) v;
    }
}

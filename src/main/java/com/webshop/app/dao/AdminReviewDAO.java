package com.webshop.app.dao;

import com.webshop.app.model.Review;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminReviewDAO {

    private static final String TABLE = "store_review";

    public List<Review> search(Integer rating, Long productId, Long authorId) {
        List<Review> reviews = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, rating, comment, created_at, author_id, product_id, has_emoji, sentiment ");
        sql.append("FROM ").append(TABLE).append(" WHERE 1 = 1 ");

        List<Object> params = new ArrayList<>();

        if (rating != null) {
            sql.append("AND rating = ? ");
            params.add(rating);
        }

        if (productId != null) {
            sql.append("AND product_id = ? ");
            params.add(productId);
        }

        if (authorId != null) {
            sql.append("AND author_id = ? ");
            params.add(authorId);
        }

        sql.append("ORDER BY created_at DESC, id DESC");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            bindParams(statement, params);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reviews.add(mapRow(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("AdminReviewDAO.search error", e);
        }

        return reviews;
    }

    public Review findById(long id) {
        String sql = """
                SELECT id, rating, comment, created_at, author_id, product_id, has_emoji, sentiment
                FROM store_review
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("AdminReviewDAO.findById error", e);
        }

        return null;
    }

    public boolean delete(long id) {
        String sql = """
                DELETE FROM store_review
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminReviewDAO.delete error", e);
        }
    }

    private Review mapRow(ResultSet resultSet) throws SQLException {
        Review review = new Review();

        review.setId(safeToInt(resultSet.getLong("id")));
        review.setAuthorId(safeToInt(resultSet.getLong("author_id")));
        review.setProductId(safeToInt(resultSet.getLong("product_id")));
        review.setRating(resultSet.getInt("rating"));
        review.setComment(resultSet.getString("comment"));
        review.setHasEmoji(resultSet.getBoolean("has_emoji"));
        review.setSentiment(resultSet.getInt("sentiment"));
        review.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));

        return review;
    }

    private void bindParams(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            int parameterIndex = i + 1;

            if (value instanceof Integer) {
                statement.setInt(parameterIndex, (Integer) value);
            } else if (value instanceof Long) {
                statement.setLong(parameterIndex, (Long) value);
            } else {
                statement.setString(parameterIndex, String.valueOf(value));
            }
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private int safeToInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        return (int) value;
    }
}
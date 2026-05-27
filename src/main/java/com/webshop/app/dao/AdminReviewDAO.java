package com.webshop.app.dao;

import com.webshop.app.model.Review;
import com.webshop.app.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminReviewDAO {

    public List<Review> search(Integer rating, Long productId, Long authorId) {
        return search(rating, productId, authorId, null, null);
    }

    public List<Review> search(Integer rating, Long productId, Long authorId, String status, String mediaType) {
        List<Review> reviews = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.id, r.rating, r.comment, r.created_at, r.author_id, r.product_id, ");
        sql.append("       r.order_id, r.order_item_id, r.has_emoji, r.sentiment, r.status, r.is_hidden, ");
        sql.append("       r.admin_note, r.approved_at, r.approved_by, r.voucher_awarded, ");
        sql.append("       u.username AS author_name, p.title AS product_name, p.slug AS product_slug, p.image AS product_image, ");
        sql.append("       COALESCE(m.media_count, 0) AS media_count, ");
        sql.append("       COALESCE(m.has_image, 0) AS has_image, COALESCE(m.has_video, 0) AS has_video, ");
        sql.append("       m.image_url, m.video_url ");
        sql.append("FROM store_review r ");
        sql.append("LEFT JOIN users u ON u.id = r.author_id ");
        sql.append("LEFT JOIN store_product p ON p.id = r.product_id ");
        sql.append(mediaAggregateSql());
        sql.append("WHERE 1 = 1 ");

        List<Object> params = new ArrayList<>();

        if (rating != null) {
            sql.append("AND r.rating = ? ");
            params.add(rating);
        }

        if (productId != null) {
            sql.append("AND r.product_id = ? ");
            params.add(productId);
        }

        if (authorId != null) {
            sql.append("AND r.author_id = ? ");
            params.add(authorId);
        }

        String normalizedStatus = normalizeStatus(status);
        if (normalizedStatus != null) {
            if ("HIDDEN".equals(normalizedStatus)) {
                sql.append("AND COALESCE(r.is_hidden, 0) = 1 ");
            } else {
                sql.append("AND r.status = ? ");
                params.add(normalizedStatus);
            }
        }

        String normalizedMedia = normalizeMediaType(mediaType);
        if ("IMAGE".equals(normalizedMedia)) {
            sql.append("AND COALESCE(m.has_image, 0) = 1 ");
        } else if ("VIDEO".equals(normalizedMedia)) {
            sql.append("AND COALESCE(m.has_video, 0) = 1 ");
        } else if ("MEDIA".equals(normalizedMedia)) {
            sql.append("AND COALESCE(m.media_count, 0) > 0 ");
        }

        sql.append("ORDER BY FIELD(r.status, 'PENDING', 'REJECTED', 'APPROVED'), r.created_at DESC, r.id DESC");

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
                SELECT r.id, r.rating, r.comment, r.created_at, r.author_id, r.product_id,
                       r.order_id, r.order_item_id, r.has_emoji, r.sentiment, r.status, r.is_hidden,
                       r.admin_note, r.approved_at, r.approved_by, r.voucher_awarded,
                       u.username AS author_name,
                       p.title AS product_name,
                       p.slug AS product_slug,
                       p.image AS product_image,
                       COALESCE(m.media_count, 0) AS media_count,
                       COALESCE(m.has_image, 0) AS has_image,
                       COALESCE(m.has_video, 0) AS has_video,
                       m.image_url,
                       m.video_url
                FROM store_review r
                LEFT JOIN users u ON u.id = r.author_id
                LEFT JOIN store_product p ON p.id = r.product_id
                LEFT JOIN (
                    SELECT
                        review_id,
                        COUNT(*) AS media_count,
                        MAX(CASE WHEN media_type = 'IMAGE' THEN 1 ELSE 0 END) AS has_image,
                        MAX(CASE WHEN media_type = 'VIDEO' THEN 1 ELSE 0 END) AS has_video,
                        MAX(CASE WHEN media_type = 'IMAGE' THEN media_url ELSE NULL END) AS image_url,
                        MAX(CASE WHEN media_type = 'VIDEO' THEN media_url ELSE NULL END) AS video_url
                    FROM store_review_media
                    GROUP BY review_id
                ) m ON m.review_id = r.id
                WHERE r.id = ?
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

    public boolean approve(long id, int adminId, String adminNote) {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String sql = """
                        UPDATE store_review
                        SET status = 'APPROVED',
                            is_hidden = 0,
                            admin_note = ?,
                            approved_at = NOW(),
                            approved_by = ?
                        WHERE id = ?
                        """;

                int affected;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, normalizeToEmpty(adminNote));
                    if (adminId > 0) {
                        statement.setInt(2, adminId);
                    } else {
                        statement.setNull(2, Types.INTEGER);
                    }
                    statement.setLong(3, id);
                    affected = statement.executeUpdate();
                }

                if (affected > 0) {
                    awardReviewCouponIfNeeded(connection, id);
                }

                connection.commit();
                return affected > 0;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("AdminReviewDAO.approve error", e);
        }
    }

    public boolean reject(long id, int adminId, String adminNote) {
        String sql = """
                UPDATE store_review
                SET status = 'REJECTED',
                    is_hidden = 1,
                    admin_note = ?,
                    approved_at = NULL,
                    approved_by = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeToEmpty(adminNote));
            if (adminId > 0) {
                statement.setInt(2, adminId);
            } else {
                statement.setNull(2, Types.INTEGER);
            }
            statement.setLong(3, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminReviewDAO.reject error", e);
        }
    }

    public boolean hide(long id) {
        String sql = "UPDATE store_review SET is_hidden = 1 WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("AdminReviewDAO.hide error", e);
        }
    }

    public boolean unhide(long id) {
        String sql = "UPDATE store_review SET is_hidden = 0 WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("AdminReviewDAO.unhide error", e);
        }
    }

    public boolean delete(long id) {
        String sql = "DELETE FROM store_review WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminReviewDAO.delete error", e);
        }
    }

    private void awardReviewCouponIfNeeded(Connection connection, long reviewId) throws SQLException {
        ReviewData data = findReviewDataForReward(connection, reviewId);
        if (data == null || data.voucherAwarded || data.authorId <= 0) {
            return;
        }

        String code = buildRewardCode(reviewId, data.authorId);
        Integer couponId = findCouponIdByCode(connection, code);
        if (couponId == null) {
            couponId = createRewardCoupon(connection, code, reviewId);
        }

        saveCouponForUser(connection, data.authorId, couponId);
        markVoucherAwarded(connection, reviewId);
    }

    private ReviewData findReviewDataForReward(Connection connection, long reviewId) throws SQLException {
        String sql = "SELECT id, author_id, voucher_awarded FROM store_review WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, reviewId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ReviewData data = new ReviewData();
                data.authorId = rs.getInt("author_id");
                data.voucherAwarded = rs.getBoolean("voucher_awarded");
                return data;
            }
        }
    }

    private Integer findCouponIdByCode(Connection connection, String code) throws SQLException {
        String sql = "SELECT id FROM store_coupon WHERE code = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    private Integer createRewardCoupon(Connection connection, String code, long reviewId) throws SQLException {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(60);

        String sql = """
                INSERT INTO store_coupon
                (code, discount_percent, start_date, end_date, used_count, is_active,
                 max_uses, max_discount_amount, min_order_amount, min_rank_code, type, description,
                 created_at, updated_at)
                VALUES (?, 5, ?, ?, 0, 1, 1, 30000.00, 0.00, 'MEMBER', 'REVIEW_REWARD', ?, NOW(), NOW())
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, code);
            statement.setDate(2, java.sql.Date.valueOf(today));
            statement.setDate(3, java.sql.Date.valueOf(end));
            statement.setString(4, "Voucher cảm ơn sau khi đánh giá sản phẩm. Mã review #" + reviewId);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        Integer existing = findCouponIdByCode(connection, code);
        if (existing != null) {
            return existing;
        }

        throw new SQLException("Cannot create review reward coupon");
    }

    private void saveCouponForUser(Connection connection, int userId, int couponId) throws SQLException {
        String sql = """
                INSERT INTO user_coupon (user_id, coupon_id, saved_at, is_used)
                VALUES (?, ?, NOW(), 0)
                ON DUPLICATE KEY UPDATE saved_at = saved_at
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, couponId);
            statement.executeUpdate();
        }
    }

    private void markVoucherAwarded(Connection connection, long reviewId) throws SQLException {
        String sql = "UPDATE store_review SET voucher_awarded = 1 WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, reviewId);
            statement.executeUpdate();
        }
    }

    private Review mapRow(ResultSet resultSet) throws SQLException {
        Review review = new Review();
        review.setId(safeToInt(resultSet.getLong("id")));
        review.setAuthorId(safeToInt(resultSet.getLong("author_id")));
        review.setProductId(safeToInt(resultSet.getLong("product_id")));
        review.setOrderId(getNullableInt(resultSet, "order_id"));
        review.setOrderItemId(getNullableInt(resultSet, "order_item_id"));
        review.setRating(resultSet.getInt("rating"));
        review.setComment(resultSet.getString("comment"));
        review.setHasEmoji(resultSet.getBoolean("has_emoji"));
        review.setSentiment(resultSet.getInt("sentiment"));
        review.setStatus(resultSet.getString("status"));
        review.setHidden(resultSet.getBoolean("is_hidden"));
        review.setAdminNote(resultSet.getString("admin_note"));
        review.setApprovedAt(toLocalDateTime(resultSet.getTimestamp("approved_at")));
        review.setApprovedBy(getNullableInt(resultSet, "approved_by"));
        review.setVoucherAwarded(resultSet.getBoolean("voucher_awarded"));
        review.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        review.setAuthorName(resultSet.getString("author_name"));
        review.setProductName(resultSet.getString("product_name"));
        review.setProductSlug(resultSet.getString("product_slug"));
        review.setProductImage(resultSet.getString("product_image"));
        review.setMediaCount(resultSet.getInt("media_count"));
        review.setHasImage(resultSet.getBoolean("has_image"));
        review.setHasVideo(resultSet.getBoolean("has_video"));
        review.setImageUrl(resultSet.getString("image_url"));
        review.setVideoUrl(resultSet.getString("video_url"));
        return review;
    }

    private String mediaAggregateSql() {
        return """
                LEFT JOIN (
                    SELECT
                        review_id,
                        COUNT(*) AS media_count,
                        MAX(CASE WHEN media_type = 'IMAGE' THEN 1 ELSE 0 END) AS has_image,
                        MAX(CASE WHEN media_type = 'VIDEO' THEN 1 ELSE 0 END) AS has_video,
                        MAX(CASE WHEN media_type = 'IMAGE' THEN media_url ELSE NULL END) AS image_url,
                        MAX(CASE WHEN media_type = 'VIDEO' THEN media_url ELSE NULL END) AS video_url
                    FROM store_review_media
                    GROUP BY review_id
                ) m ON m.review_id = r.id
                """;
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

    private Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
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

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }

        String value = status.trim().toUpperCase(Locale.ROOT);
        return switch (value) {
            case "PENDING", "APPROVED", "REJECTED", "HIDDEN" -> value;
            default -> null;
        };
    }

    private String normalizeMediaType(String mediaType) {
        if (mediaType == null || mediaType.trim().isEmpty()) {
            return null;
        }
        String value = mediaType.trim().toUpperCase(Locale.ROOT);
        return switch (value) {
            case "IMAGE", "VIDEO", "MEDIA" -> value;
            default -> null;
        };
    }

    private String normalizeToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String buildRewardCode(long reviewId, int userId) {
        String raw = "RV" + reviewId + "U" + userId;
        return raw.length() > 20 ? raw.substring(0, 20) : raw;
    }

    private static class ReviewData {
        int authorId;
        boolean voucherAwarded;
    }
}

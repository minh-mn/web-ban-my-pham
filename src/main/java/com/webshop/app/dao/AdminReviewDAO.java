package com.webshop.app.dao;

import com.webshop.app.model.Review;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminReviewDAO {

    public List<Review> search(Integer rating, Long productId, Long authorId) {
        return search(rating, productId, authorId, null, null, null);
    }

    public List<Review> search(Integer rating, Long productId, Long authorId, String status, String mediaType) {
        return search(rating, productId, authorId, status, mediaType, null);
    }

    public List<Review> search(Integer rating, Long productId, Long authorId, String status, String mediaType, String keyword) {
        List<Review> reviews = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.id, r.rating, r.comment, r.created_at, COALESCE(r.author_id, r.user_id) AS reviewer_id, r.product_id, ");
        sql.append("       r.order_id, r.order_item_id, r.has_emoji, r.sentiment, r.status, r.is_hidden, ");
        sql.append("       r.admin_note, r.approved_at, r.approved_by, r.voucher_awarded, ");
        sql.append("       u.username AS author_name, u.full_name AS author_full_name, u.email AS author_email, ");
        sql.append("       u.phone AS author_phone, u.role AS author_role, u.active AS author_active, ");
        sql.append("       COALESCE(u.manual_rank_code, 'MEMBER') AS author_rank_code, ");
        sql.append("       COALESCE(rank_table.name, u.manual_rank_code, 'Thành viên') AS author_rank_name, ");
        sql.append("       u.created_at AS author_created_at, ");
        sql.append("       CAST(p.id AS CHAR) AS product_code, ");
        sql.append("       COALESCE(NULLIF(p.title, ''), p.name, CONCAT('Sản phẩm #', p.id)) AS product_name, ");
        sql.append("       p.slug AS product_slug, p.image AS product_image, ");
        sql.append(mediaSelectSql());
        sql.append("FROM store_review r ");
        sql.append("LEFT JOIN users u ON u.id = COALESCE(r.author_id, r.user_id) ");
        sql.append("LEFT JOIN store_rank rank_table ON rank_table.code = u.manual_rank_code ");
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
            sql.append("AND COALESCE(r.author_id, r.user_id) = ? ");
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
            sql.append("AND ").append(hasImageSql()).append(" ");
        } else if ("VIDEO".equals(normalizedMedia)) {
            sql.append("AND ").append(hasVideoSql()).append(" ");
        } else if ("MEDIA".equals(normalizedMedia)) {
            sql.append("AND (").append(hasImageSql()).append(" OR ").append(hasVideoSql()).append(") ");
        }

        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword != null) {
            sql.append("AND (r.comment LIKE ? ");
            sql.append("OR CAST(r.id AS CHAR) LIKE ? ");
            sql.append("OR CAST(r.product_id AS CHAR) LIKE ? ");
            sql.append("OR CAST(COALESCE(r.author_id, r.user_id) AS CHAR) LIKE ? ");
            sql.append("OR p.title LIKE ? OR p.name LIKE ? ");
            sql.append("OR u.username LIKE ? OR u.full_name LIKE ? OR u.email LIKE ? OR u.phone LIKE ?) ");

            String like = "%" + normalizedKeyword + "%";
            for (int i = 0; i < 10; i++) {
                params.add(like);
            }
        }

        sql.append("ORDER BY FIELD(r.status, 'PENDING', 'REJECTED', 'APPROVED'), ");
        sql.append("COALESCE(r.is_hidden, 0) ASC, r.created_at DESC, r.id DESC");

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
                SELECT r.id, r.rating, r.comment, r.created_at, COALESCE(r.author_id, r.user_id) AS reviewer_id, r.product_id,
                       r.order_id, r.order_item_id, r.has_emoji, r.sentiment, r.status, r.is_hidden,
                       r.admin_note, r.approved_at, r.approved_by, r.voucher_awarded,
                       u.username AS author_name,
                       u.full_name AS author_full_name,
                       u.email AS author_email,
                       u.phone AS author_phone,
                       u.role AS author_role,
                       u.active AS author_active,
                       COALESCE(u.manual_rank_code, 'MEMBER') AS author_rank_code,
                       COALESCE(rank_table.name, u.manual_rank_code, 'Thành viên') AS author_rank_name,
                       u.created_at AS author_created_at,
                       CAST(p.id AS CHAR) AS product_code,
                       COALESCE(NULLIF(p.title, ''), p.name, CONCAT('Sản phẩm #', p.id)) AS product_name,
                       p.slug AS product_slug,
                       p.image AS product_image,
                """ + mediaSelectSql() + """
                FROM store_review r
                LEFT JOIN users u ON u.id = COALESCE(r.author_id, r.user_id)
                LEFT JOIN store_rank rank_table ON rank_table.code = u.manual_rank_code
                LEFT JOIN store_product p ON p.id = r.product_id
                """ + mediaAggregateSql() + """
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
        Long couponId = findCouponIdByCode(connection, code);
        if (couponId == null) {
            couponId = createRewardCoupon(connection, code, reviewId);
        }

        saveCouponForUser(connection, data.authorId, couponId);
        markVoucherAwarded(connection, reviewId);
    }

    private ReviewData findReviewDataForReward(Connection connection, long reviewId) throws SQLException {
        String sql = "SELECT id, COALESCE(author_id, user_id) AS reviewer_id, voucher_awarded FROM store_review WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, reviewId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ReviewData data = new ReviewData();
                data.authorId = rs.getInt("reviewer_id");
                data.voucherAwarded = rs.getBoolean("voucher_awarded");
                return data;
            }
        }
    }

    private Long findCouponIdByCode(Connection connection, String code) throws SQLException {
        String sql = "SELECT id FROM store_coupon WHERE code = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    private Long createRewardCoupon(Connection connection, String code, long reviewId) throws SQLException {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(60);

        String sql = """
                INSERT INTO store_coupon
                (code, discount_percent, discount_type, discount_value, start_date, end_date, used_count, is_active,
                 max_uses, max_discount_amount, min_order_amount, min_rank_code, type, description,
                 created_at, updated_at)
                VALUES (?, 5, 'PERCENT', 5.00, ?, ?, 0, 1, 1, 30000.00, 0.00, 'MEMBER', 'REVIEW_REWARD', ?, NOW(), NOW())
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, code);
            statement.setDate(2, java.sql.Date.valueOf(today));
            statement.setDate(3, java.sql.Date.valueOf(end));
            statement.setString(4, "Voucher cảm ơn sau khi đánh giá sản phẩm. Mã review #" + reviewId);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        Long existing = findCouponIdByCode(connection, code);
        if (existing != null) {
            return existing;
        }

        throw new SQLException("Cannot create review reward coupon");
    }

    private void saveCouponForUser(Connection connection, int userId, long couponId) throws SQLException {
        String sql = """
                INSERT INTO user_coupon (user_id, coupon_id, saved_at, is_used)
                VALUES (?, ?, NOW(), 0)
                ON DUPLICATE KEY UPDATE saved_at = saved_at
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setLong(2, couponId);
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
        review.setAuthorId(safeToInt(resultSet.getLong("reviewer_id")));
        review.setProductId(safeToInt(resultSet.getLong("product_id")));
        review.setProductCode(resultSet.getString("product_code"));
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
        review.setAuthorFullName(resultSet.getString("author_full_name"));
        review.setAuthorEmail(resultSet.getString("author_email"));
        review.setAuthorPhone(resultSet.getString("author_phone"));
        review.setAuthorRole(resultSet.getString("author_role"));
        review.setAuthorActive(getNullableBoolean(resultSet, "author_active"));
        review.setAuthorRankCode(resultSet.getString("author_rank_code"));
        review.setAuthorRankName(resultSet.getString("author_rank_name"));
        review.setAuthorCreatedAt(toLocalDateTime(resultSet.getTimestamp("author_created_at")));

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

    private String mediaSelectSql() {
        return """
                       (COALESCE(m.media_count, 0)
                           + CASE WHEN NULLIF(TRIM(r.review_image), '') IS NULL THEN 0 ELSE 1 END
                           + CASE WHEN NULLIF(TRIM(r.review_video), '') IS NULL THEN 0 ELSE 1 END) AS media_count,
                       GREATEST(COALESCE(m.has_image, 0), CASE WHEN NULLIF(TRIM(r.review_image), '') IS NULL THEN 0 ELSE 1 END) AS has_image,
                       GREATEST(COALESCE(m.has_video, 0), CASE WHEN NULLIF(TRIM(r.review_video), '') IS NULL THEN 0 ELSE 1 END) AS has_video,
                       COALESCE(m.image_url, NULLIF(TRIM(r.review_image), '')) AS image_url,
                       COALESCE(m.video_url, NULLIF(TRIM(r.review_video), '')) AS video_url
                """;
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

    private String hasImageSql() {
        return "(COALESCE(m.has_image, 0) = 1 OR NULLIF(TRIM(r.review_image), '') IS NOT NULL)";
    }

    private String hasVideoSql() {
        return "(COALESCE(m.has_video, 0) = 1 OR NULLIF(TRIM(r.review_video), '') IS NOT NULL)";
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

    private Boolean getNullableBoolean(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
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

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }

        String value = keyword.trim();
        return value.length() > 120 ? value.substring(0, 120) : value;
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

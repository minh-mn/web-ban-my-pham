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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReviewDAO {

    public List<Review> findByProductId(int productId) {
        return findByProductId(productId, "newest", null, null);
    }

    public List<Review> findByProductId(int productId, String sort, Integer rating, String mediaType) {
        List<Review> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.id, r.product_id, r.order_id, r.order_item_id, r.rating, r.comment, r.created_at, ");
        sql.append("       r.author_id, r.has_emoji, r.sentiment, r.status, r.is_hidden, r.admin_note, ");
        sql.append("       r.approved_at, r.approved_by, r.voucher_awarded, ");
        sql.append("       u.username AS author_name, p.title AS product_name, p.slug AS product_slug, p.image AS product_image, ");
        sql.append("       COALESCE(m.media_count, 0) AS media_count, ");
        sql.append("       COALESCE(m.has_image, 0) AS has_image, COALESCE(m.has_video, 0) AS has_video, ");
        sql.append("       m.image_url, m.video_url ");
        sql.append("FROM store_review r ");
        sql.append("JOIN users u ON r.author_id = u.id ");
        sql.append("LEFT JOIN store_product p ON p.id = r.product_id ");
        sql.append(mediaAggregateSql());
        sql.append("WHERE r.product_id = ? ");
        sql.append("AND r.status = 'APPROVED' ");
        sql.append("AND COALESCE(r.is_hidden, 0) = 0 ");

        List<Object> params = new ArrayList<>();
        params.add(productId);

        if (rating != null && rating >= 1 && rating <= 5) {
            sql.append("AND r.rating = ? ");
            params.add(rating);
        }

        String normalizedMedia = normalizeMediaType(mediaType);
        if ("IMAGE".equals(normalizedMedia)) {
            sql.append("AND COALESCE(m.has_image, 0) = 1 ");
        } else if ("VIDEO".equals(normalizedMedia)) {
            sql.append("AND COALESCE(m.has_video, 0) = 1 ");
        } else if ("MEDIA".equals(normalizedMedia)) {
            sql.append("AND COALESCE(m.media_count, 0) > 0 ");
        }

        appendSort(sql, sort);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            bind(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("ReviewDAO.findByProductId error", e);
        }

        return list;
    }

    public Review findReviewableOrderItem(int userId, int orderId, int productId) {
        String sql = """
                SELECT
                    o.id AS order_id,
                    oi.id AS order_item_id,
                    oi.product_id,
                    COALESCE(p.title, CONCAT('Sản phẩm #', oi.product_id)) AS product_name,
                    p.slug AS product_slug,
                    p.image AS product_image
                FROM store_order o
                JOIN store_orderitem oi ON oi.order_id = o.id
                LEFT JOIN store_product p ON p.id = oi.product_id
                WHERE o.id = ?
                  AND o.user_id = ?
                  AND oi.product_id = ?
                  AND LOWER(o.status) = 'completed'
                  AND UPPER(o.payment_status) = 'PAID'
                  AND UPPER(o.shipping_status) = 'DELIVERED'
                LIMIT 1
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            ps.setInt(3, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Review review = new Review();
                review.setOrderId(rs.getInt("order_id"));
                review.setOrderItemId(rs.getInt("order_item_id"));
                review.setProductId(rs.getInt("product_id"));
                review.setProductName(rs.getString("product_name"));
                review.setProductSlug(rs.getString("product_slug"));
                review.setProductImage(rs.getString("product_image"));
                return review;
            }

        } catch (SQLException e) {
            throw new RuntimeException("ReviewDAO.findReviewableOrderItem error", e);
        }
    }

    public boolean canUserReviewProduct(int userId, int productId) {
        if (userId <= 0 || productId <= 0) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM store_order o
                JOIN store_orderitem oi ON oi.order_id = o.id
                WHERE o.user_id = ?
                  AND oi.product_id = ?
                  AND LOWER(o.status) = 'completed'
                  AND UPPER(o.payment_status) = 'PAID'
                  AND UPPER(o.shipping_status) = 'DELIVERED'
                LIMIT 1
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, productId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("ReviewDAO.canUserReviewProduct error", e);
        }
    }

    public boolean canUserReviewOrderItem(int userId, int orderId, int orderItemId, int productId) {
        if (userId <= 0 || orderId <= 0 || orderItemId <= 0 || productId <= 0) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM store_order o
                JOIN store_orderitem oi ON oi.order_id = o.id
                WHERE o.id = ?
                  AND o.user_id = ?
                  AND oi.id = ?
                  AND oi.product_id = ?
                  AND LOWER(o.status) = 'completed'
                  AND UPPER(o.payment_status) = 'PAID'
                  AND UPPER(o.shipping_status) = 'DELIVERED'
                LIMIT 1
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            ps.setInt(3, orderItemId);
            ps.setInt(4, productId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("ReviewDAO.canUserReviewOrderItem error", e);
        }
    }

    public Map<Integer, Boolean> findReviewedOrderItemMap(int userId, int orderId) {
        Map<Integer, Boolean> result = new HashMap<>();
        if (userId <= 0 || orderId <= 0) {
            return result;
        }

        String sql = """
                SELECT order_item_id
                FROM store_review
                WHERE author_id = ?
                  AND order_id = ?
                  AND order_item_id IS NOT NULL
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("order_item_id"), Boolean.TRUE);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("ReviewDAO.findReviewedOrderItemMap error", e);
        }

        return result;
    }

    public void createOrUpdate(Review review) {
        createOrUpdate(review, review == null ? null : review.getImageUrl(), review == null ? null : review.getVideoUrl());
    }

    public void createOrUpdate(Review review, String imageUrl, String videoUrl) {
        if (review == null) {
            throw new RuntimeException("ReviewDAO.createOrUpdate error: review is null");
        }
        if (review.getProductId() <= 0 || review.getAuthorId() <= 0) {
            throw new RuntimeException("ReviewDAO.createOrUpdate error: invalid productId/authorId");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!existsAuthor(conn, review.getAuthorId())) {
                    throw new RuntimeException("ReviewDAO.createOrUpdate error: author_id=" + review.getAuthorId() + " không tồn tại");
                }

                Integer existingId = findExistingReviewId(conn, review);
                int reviewId;
                if (existingId != null && existingId > 0) {
                    reviewId = existingId;
                    update(conn, review, reviewId);
                } else {
                    reviewId = insert(conn, review);
                }

                replaceMedia(conn, reviewId, imageUrl, videoUrl);

                notifyReviewCreatedSafely(
                        conn,
                        review,
                        reviewId
                );

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("ReviewDAO.createOrUpdate error", e);
        }
    }

    private Integer findExistingReviewId(Connection conn, Review review) throws SQLException {
        String sql;
        if (review.getOrderItemId() != null) {
            sql = """
                    SELECT id
                    FROM store_review
                    WHERE author_id = ?
                      AND order_item_id = ?
                    LIMIT 1
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, review.getAuthorId());
                ps.setInt(2, review.getOrderItemId());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : null;
                }
            }
        }

        sql = """
                SELECT id
                FROM store_review
                WHERE product_id = ?
                  AND author_id = ?
                ORDER BY id DESC
                LIMIT 1
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, review.getProductId());
            ps.setInt(2, review.getAuthorId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    private void update(Connection conn, Review review, int reviewId) throws SQLException {
        String sql = """
                UPDATE store_review
                SET order_id = ?,
                    order_item_id = ?,
                    rating = ?,
                    comment = ?,
                    has_emoji = ?,
                    sentiment = ?,
                    status = 'PENDING',
                    is_hidden = 0,
                    admin_note = NULL,
                    approved_at = NULL,
                    approved_by = NULL,
                    created_at = NOW()
                WHERE id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableInt(ps, 1, review.getOrderId());
            setNullableInt(ps, 2, review.getOrderItemId());
            ps.setInt(3, review.getRating());
            ps.setString(4, safeString(review.getComment()));
            ps.setBoolean(5, review.isHasEmoji());
            ps.setInt(6, review.getSentiment());
            ps.setInt(7, reviewId);
            ps.executeUpdate();
        }
    }

    private int insert(Connection conn, Review review) throws SQLException {
        String sql = """
                INSERT INTO store_review
                (product_id, author_id, order_id, order_item_id, rating, comment,
                 created_at, has_emoji, sentiment, status, is_hidden, voucher_awarded)
                VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, ?, 'PENDING', 0, 0)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, review.getProductId());
            ps.setInt(2, review.getAuthorId());
            setNullableInt(ps, 3, review.getOrderId());
            setNullableInt(ps, 4, review.getOrderItemId());
            ps.setInt(5, review.getRating());
            ps.setString(6, safeString(review.getComment()));
            ps.setBoolean(7, review.isHasEmoji());
            ps.setInt(8, review.getSentiment());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Cannot create review: generated id not returned");
    }

    private void replaceMedia(Connection conn, int reviewId, String imageUrl, String videoUrl) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM store_review_media WHERE review_id = ?")) {
            ps.setInt(1, reviewId);
            ps.executeUpdate();
        }

        insertMedia(conn, reviewId, "IMAGE", imageUrl);
        insertMedia(conn, reviewId, "VIDEO", videoUrl);
    }

    private void insertMedia(Connection conn, int reviewId, String mediaType, String mediaUrl) throws SQLException {
        String value = normalize(mediaUrl);
        if (value == null) {
            return;
        }

        String sql = """
                INSERT INTO store_review_media (review_id, media_type, media_url, created_at)
                VALUES (?, ?, ?, NOW())
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            ps.setString(2, mediaType);
            ps.setString(3, value);
            ps.executeUpdate();
        }
    }

    private boolean existsAuthor(Connection conn, int authorId) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /* ================= NOTIFICATION HELPERS - ISSUE 114 ================= */

    private void notifyReviewCreatedSafely(Connection connection,
                                           Review review,
                                           int reviewId) {
        if (connection == null
                || review == null
                || reviewId <= 0
                || review.getAuthorId() <= 0
                || review.getProductId() <= 0) {
            return;
        }

        String productName = findProductNameSafely(connection, review.getProductId());
        String title = "Có đánh giá mới";
        String message = "Khách hàng #" + review.getAuthorId()
                + " vừa gửi đánh giá " + review.getRating()
                + " sao cho sản phẩm \"" + productName + "\".";

        try {
            new NotificationDAO().createAdminNotification(
                    connection,
                    "REVIEW_CREATED",
                    title,
                    message,
                    "/admin/reviews",
                    "REVIEW",
                    (long) reviewId
            );
        } catch (SQLException e) {
            /*
             * Không để lỗi notification làm hỏng thao tác gửi đánh giá.
             */
            e.printStackTrace();
        }
    }

    /**
     * Dùng cho AdminReviewServlet nếu servlet xử lý duyệt/ẩn đánh giá
     * nhưng muốn gọi DAO để tạo thông báo cho khách.
     */
    public void notifyReviewResult(int reviewId,
                                   boolean approved,
                                   String adminNote) {
        if (reviewId <= 0) {
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {
            notifyReviewResult(connection, reviewId, approved, adminNote);
        } catch (SQLException e) {
            throw new RuntimeException("ReviewDAO.notifyReviewResult error", e);
        }
    }

    public void notifyReviewResult(Connection connection,
                                   int reviewId,
                                   boolean approved,
                                   String adminNote) throws SQLException {
        if (connection == null) {
            throw new SQLException("Connection must not be null");
        }

        ReviewNotificationTarget target = findNotificationTargetByReviewId(connection, reviewId);

        if (target == null || target.authorId() <= 0) {
            return;
        }

        String type = approved ? "REVIEW_APPROVED" : "REVIEW_REJECTED";
        String title = approved ? "Đánh giá đã được duyệt" : "Đánh giá bị từ chối";
        String message = approved
                ? "Đánh giá của bạn cho sản phẩm \"" + target.productName() + "\" đã được duyệt."
                : "Đánh giá của bạn cho sản phẩm \"" + target.productName() + "\" đã bị từ chối hoặc bị ẩn."
                        + buildAdminNoteText(adminNote);

        new NotificationDAO().createUserNotification(
                connection,
                target.authorId(),
                type,
                title,
                message,
                target.orderId() == null ? "/notifications" : "/orders/detail?id=" + target.orderId(),
                "REVIEW",
                (long) reviewId
        );
    }

    private ReviewNotificationTarget findNotificationTargetByReviewId(Connection connection,
                                                                      int reviewId) throws SQLException {
        String sql = """
                SELECT
                    r.author_id,
                    r.order_id,
                    COALESCE(p.title, CONCAT('Sản phẩm #', r.product_id)) AS product_name
                FROM store_review r
                LEFT JOIN store_product p ON p.id = r.product_id
                WHERE r.id = ?
                LIMIT 1
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reviewId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                int authorId = resultSet.getInt("author_id");
                Integer orderId = getNullableInt(resultSet, "order_id");
                String productName = defaultIfBlank(
                        resultSet.getString("product_name"),
                        "Sản phẩm"
                );

                return new ReviewNotificationTarget(authorId, orderId, productName);
            }
        }
    }

    private String findProductNameSafely(Connection connection, int productId) {
        String sql = """
                SELECT COALESCE(title, CONCAT('Sản phẩm #', id)) AS product_name
                FROM store_product
                WHERE id = ?
                LIMIT 1
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return defaultIfBlank(resultSet.getString("product_name"), "Sản phẩm");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Sản phẩm #" + productId;
    }

    private String buildAdminNoteText(String adminNote) {
        String value = normalize(adminNote);

        if (value == null) {
            return "";
        }

        return " Lý do: " + value;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        String normalized = normalize(value);
        return normalized == null ? defaultValue : normalized;
    }

    private record ReviewNotificationTarget(
            int authorId,
            Integer orderId,
            String productName
    ) {
    }


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

    private void appendSort(StringBuilder sql, String sort) {
        String value = sort == null ? "newest" : sort.trim().toLowerCase(Locale.ROOT);
        switch (value) {
            case "oldest":
                sql.append("ORDER BY r.created_at ASC, r.id ASC");
                break;
            case "highest":
                sql.append("ORDER BY r.rating DESC, r.created_at DESC, r.id DESC");
                break;
            case "lowest":
                sql.append("ORDER BY r.rating ASC, r.created_at DESC, r.id DESC");
                break;
            case "media":
                sql.append("ORDER BY COALESCE(m.media_count, 0) DESC, r.created_at DESC, r.id DESC");
                break;
            case "newest":
            default:
                sql.append("ORDER BY r.created_at DESC, r.id DESC");
                break;
        }
    }

    private String normalizeMediaType(String mediaType) {
        String value = normalize(mediaType);
        return value == null ? null : value.toUpperCase(Locale.ROOT);
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setId(rs.getInt("id"));
        r.setProductId(rs.getInt("product_id"));
        r.setAuthorId(rs.getInt("author_id"));
        r.setOrderId(getNullableInt(rs, "order_id"));
        r.setOrderItemId(getNullableInt(rs, "order_item_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        r.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        r.setHasEmoji(rs.getBoolean("has_emoji"));
        r.setSentiment(rs.getInt("sentiment"));
        r.setStatus(rs.getString("status"));
        r.setHidden(rs.getBoolean("is_hidden"));
        r.setAdminNote(rs.getString("admin_note"));
        r.setApprovedAt(toLocalDateTime(rs.getTimestamp("approved_at")));
        r.setApprovedBy(getNullableInt(rs, "approved_by"));
        r.setVoucherAwarded(rs.getBoolean("voucher_awarded"));
        r.setAuthorName(rs.getString("author_name"));
        r.setProductName(rs.getString("product_name"));
        r.setProductSlug(rs.getString("product_slug"));
        r.setProductImage(rs.getString("product_image"));
        r.setMediaCount(rs.getInt("media_count"));
        r.setHasImage(rs.getBoolean("has_image"));
        r.setHasVideo(rs.getBoolean("has_video"));
        r.setImageUrl(rs.getString("image_url"));
        r.setVideoUrl(rs.getString("video_url"));
        return r;
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            int index = i + 1;
            if (value instanceof Integer) {
                ps.setInt(index, (Integer) value);
            } else {
                ps.setString(index, String.valueOf(value));
            }
        }
    }

    private Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null || value <= 0) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}

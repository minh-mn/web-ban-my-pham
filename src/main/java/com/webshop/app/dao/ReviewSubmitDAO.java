package com.webshop.app.dao;

import com.webshop.app.model.ReviewMedia;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class ReviewSubmitDAO {

    public boolean canReview(int userId, long orderId, long productId, Long orderItemId) {
        String sql = """
                SELECT COUNT(*)
                FROM store_order o
                JOIN store_orderitem oi ON oi.order_id = o.id
                WHERE o.id = ?
                  AND o.user_id = ?
                  AND oi.product_id = ?
                  AND (? IS NULL OR oi.id = ?)
                  AND LOWER(o.status) = 'completed'
                  AND UPPER(COALESCE(o.shipping_status, 'DELIVERED')) IN ('DELIVERED', 'DONE')
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, orderId);
            statement.setInt(2, userId);
            statement.setLong(3, productId);

            if (orderItemId == null) {
                statement.setNull(4, java.sql.Types.BIGINT);
                statement.setNull(5, java.sql.Types.BIGINT);
            } else {
                statement.setLong(4, orderItemId);
                statement.setLong(5, orderItemId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("ReviewSubmitDAO.canReview error", e);
        }
    }

    public boolean hasReviewedOrderItem(int userId, long orderId, long productId, Long orderItemId) {
        String sql = """
                SELECT COUNT(*)
                FROM store_review
                WHERE author_id = ?
                  AND order_id = ?
                  AND product_id = ?
                  AND (? IS NULL OR order_item_id = ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setLong(2, orderId);
            statement.setLong(3, productId);

            if (orderItemId == null) {
                statement.setNull(4, java.sql.Types.BIGINT);
                statement.setNull(5, java.sql.Types.BIGINT);
            } else {
                statement.setLong(4, orderItemId);
                statement.setLong(5, orderItemId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("ReviewSubmitDAO.hasReviewedOrderItem error", e);
        }
    }

    public long createReview(CreateReviewRequest request) {
        String insertReviewSql = """
                INSERT INTO store_review (
                    rating,
                    comment,
                    created_at,
                    author_id,
                    product_id,
                    order_id,
                    order_item_id,
                    has_emoji,
                    sentiment,
                    status,
                    is_hidden,
                    review_image,
                    review_video,
                    is_anonymous,
                    reward_points,
                    points_awarded,
                    seller_service_rating,
                    delivery_speed_rating,
                    shipper_rating,
                    service_tags,
                    service_comment
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', 0, ?, ?, ?, ?, 0, ?, ?, ?, ?, ?)
                """;

        String insertMediaSql = """
                INSERT INTO store_review_media (
                    review_id,
                    media_type,
                    media_url,
                    original_name,
                    file_size,
                    mime_type
                ) VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection()) {
            boolean oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                long reviewId;

                try (PreparedStatement statement = connection.prepareStatement(insertReviewSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, request.rating());
                    statement.setString(2, request.comment());
                    statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    statement.setInt(4, request.authorId());
                    statement.setLong(5, request.productId());
                    statement.setLong(6, request.orderId());

                    if (request.orderItemId() == null) {
                        statement.setNull(7, java.sql.Types.BIGINT);
                    } else {
                        statement.setLong(7, request.orderItemId());
                    }

                    statement.setBoolean(8, containsEmoji(request.comment()));
                    statement.setInt(9, simpleSentiment(request.rating()));
                    statement.setString(10, request.firstImageUrl());
                    statement.setString(11, request.firstVideoUrl());
                    statement.setBoolean(12, request.anonymous());
                    statement.setInt(13, request.rewardPoints());
                    statement.setInt(14, request.sellerServiceRating());
                    statement.setInt(15, request.deliverySpeedRating());
                    statement.setInt(16, request.shipperRating());
                    statement.setString(17, request.serviceTags());
                    statement.setString(18, request.serviceComment());

                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Cannot get generated review id");
                        }
                        reviewId = keys.getLong(1);
                    }
                }

                if (request.mediaList() != null && !request.mediaList().isEmpty()) {
                    try (PreparedStatement statement = connection.prepareStatement(insertMediaSql)) {
                        for (ReviewMedia media : request.mediaList()) {
                            statement.setLong(1, reviewId);
                            statement.setString(2, media.getMediaType());
                            statement.setString(3, media.getMediaUrl());
                            statement.setString(4, media.getOriginalName());
                            statement.setLong(5, media.getFileSize());
                            statement.setString(6, media.getMimeType());
                            statement.addBatch();
                        }
                        statement.executeBatch();
                    }
                }

                connection.commit();
                connection.setAutoCommit(oldAutoCommit);
                return reviewId;
            } catch (Exception e) {
                connection.rollback();
                connection.setAutoCommit(oldAutoCommit);
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("ReviewSubmitDAO.createReview error", e);
        }
    }

    /**
     * Gọi method này trong AdminReviewServlet khi admin duyệt review.
     * Chỉ cộng xu đúng 1 lần nhờ điều kiện points_awarded = 0.
     */
    public boolean approveReviewAndAwardPoints(long reviewId, int adminId) {
        String selectSql = """
                SELECT id, author_id, order_id, reward_points, points_awarded
                FROM store_review
                WHERE id = ?
                FOR UPDATE
                """;

        String updateReviewSql = """
                UPDATE store_review
                SET status = 'APPROVED',
                    is_hidden = 0,
                    approved_at = NOW(),
                    approved_by = ?,
                    points_awarded = 1
                WHERE id = ?
                """;

        String updateUserSql = """
                UPDATE users
                SET reward_points = COALESCE(reward_points, 0) + ?
                WHERE id = ?
                """;

        String insertTransactionSql = """
                INSERT INTO user_point_transaction (
                    user_id, order_id, review_id, points, type, reason, expired_at
                ) VALUES (?, ?, ?, ?, 'EARN', ?, DATE_ADD(NOW(), INTERVAL 90 DAY))
                """;

        try (Connection connection = DBConnection.getConnection()) {
            boolean oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                int userId;
                long orderId;
                int points;
                boolean alreadyAwarded;

                try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
                    statement.setLong(1, reviewId);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            connection.rollback();
                            connection.setAutoCommit(oldAutoCommit);
                            return false;
                        }

                        userId = resultSet.getInt("author_id");
                        orderId = resultSet.getLong("order_id");
                        points = resultSet.getInt("reward_points");
                        alreadyAwarded = resultSet.getBoolean("points_awarded");
                    }
                }

                if (points <= 0) {
                    points = 200;
                }

                try (PreparedStatement statement = connection.prepareStatement(updateReviewSql)) {
                    statement.setInt(1, adminId);
                    statement.setLong(2, reviewId);
                    statement.executeUpdate();
                }

                if (!alreadyAwarded) {
                    try (PreparedStatement statement = connection.prepareStatement(updateUserSql)) {
                        statement.setInt(1, points);
                        statement.setInt(2, userId);
                        statement.executeUpdate();
                    }

                    try (PreparedStatement statement = connection.prepareStatement(insertTransactionSql)) {
                        statement.setInt(1, userId);
                        statement.setLong(2, orderId);
                        statement.setLong(3, reviewId);
                        statement.setInt(4, points);
                        statement.setString(5, "Cộng xu sau khi đánh giá sản phẩm được duyệt");
                        statement.executeUpdate();
                    }
                }

                connection.commit();
                connection.setAutoCommit(oldAutoCommit);
                return true;
            } catch (Exception e) {
                connection.rollback();
                connection.setAutoCommit(oldAutoCommit);
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("ReviewSubmitDAO.approveReviewAndAwardPoints error", e);
        }
    }

    private static boolean containsEmoji(String value) {
        if (value == null) {
            return false;
        }
        return value.codePoints().anyMatch(cp -> Character.getType(cp) == Character.SURROGATE || cp > 0xFFFF);
    }

    private static int simpleSentiment(int rating) {
        return rating >= 4 ? 1 : 0;
    }

    public record CreateReviewRequest(
            int authorId,
            long orderId,
            long productId,
            Long orderItemId,
            int rating,
            String comment,
            boolean anonymous,
            int rewardPoints,
            int sellerServiceRating,
            int deliverySpeedRating,
            int shipperRating,
            String serviceTags,
            String serviceComment,
            String firstImageUrl,
            String firstVideoUrl,
            List<ReviewMedia> mediaList
    ) {
    }
}

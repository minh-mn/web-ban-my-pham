package com.webshop.app.dao;

import com.webshop.app.model.ReviewMedia;
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
import java.util.List;

public class ReviewSubmitDAO {

    /**
     * Kiểm tra user có quyền đánh giá sản phẩm trong đơn hàng hay không.
     *
     * Bản này cố tình KHÔNG chặn cứng theo status/shipping_status nữa,
     * vì database của project đang có nhiều kiểu trạng thái khác nhau.
     *
     * Điều kiện chính:
     * - Đơn hàng thuộc đúng user.
     * - Sản phẩm thật sự nằm trong đơn hàng.
     * - Nếu có orderItemId thì orderItemId phải đúng.
     *
     * Việc chỉ hiện nút đánh giá khi đã giao thành công nên xử lý ở OrderDetailServlet/JSP.
     */
    public boolean canReview(int userId, long orderId, long productId, Long orderItemId) {
        String sql = """
                SELECT COUNT(*)
                FROM store_order o
                JOIN store_orderitem oi ON oi.order_id = o.id
                WHERE o.id = ?
                  AND o.user_id = ?
                  AND oi.product_id = ?
                  AND (? IS NULL OR oi.id = ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, orderId);
            statement.setInt(2, userId);
            statement.setLong(3, productId);

            if (orderItemId == null) {
                statement.setNull(4, Types.BIGINT);
                statement.setNull(5, Types.BIGINT);
            } else {
                statement.setLong(4, orderItemId);
                statement.setLong(5, orderItemId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("ReviewSubmitDAO.canReview error: " + e.getMessage(), e);
        }
    }

    /**
     * Chặn đánh giá trùng.
     *
     * Nếu DB có user_id thì check cả author_id và user_id.
     * Nếu DB chưa có user_id thì chỉ check author_id để tránh lỗi SQL.
     */
    public boolean hasReviewedOrderItem(int userId, long orderId, long productId, Long orderItemId) {
        try (Connection connection = DBConnection.getConnection()) {
            boolean hasUserId = columnExists(connection, "store_review", "user_id");
            boolean hasOrderId = columnExists(connection, "store_review", "order_id");
            boolean hasOrderItemId = columnExists(connection, "store_review", "order_item_id");
            boolean hasStatus = columnExists(connection, "store_review", "status");

            StringBuilder sql = new StringBuilder();
            List<SqlValue> values = new ArrayList<>();

            sql.append("SELECT COUNT(*) FROM store_review WHERE ");

            if (hasUserId) {
                sql.append("(author_id = ? OR user_id = ?)");
                values.add(new SqlValue(userId, Types.INTEGER));
                values.add(new SqlValue(userId, Types.INTEGER));
            } else {
                sql.append("author_id = ?");
                values.add(new SqlValue(userId, Types.INTEGER));
            }

            if (hasOrderId) {
                sql.append(" AND order_id = ?");
                values.add(new SqlValue(orderId, Types.BIGINT));
            }

            sql.append(" AND product_id = ?");
            values.add(new SqlValue(productId, Types.BIGINT));

            if (hasOrderItemId && orderItemId != null) {
                sql.append(" AND order_item_id = ?");
                values.add(new SqlValue(orderItemId, Types.BIGINT));
            }

            if (hasStatus) {
                sql.append(" AND status IN ('PENDING', 'APPROVED', 'REJECTED', 'HIDDEN')");
            }

            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                bindValues(statement, values);

                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() && resultSet.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("ReviewSubmitDAO.hasReviewedOrderItem error: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo review mới.
     *
     * Bản này tự kiểm tra cột nào tồn tại rồi mới insert cột đó.
     * Vì vậy nếu database của bạn chưa có user_id, reward_points, original_name, file_size...
     * thì vẫn insert được review PENDING để admin duyệt.
     */
    public long createReview(CreateReviewRequest request) {
        try (Connection connection = DBConnection.getConnection()) {
            boolean oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                Long resolvedOrderItemId = request.orderItemId();

                if (resolvedOrderItemId == null) {
                    resolvedOrderItemId = findOrderItemId(connection, request.orderId(), request.productId());
                }

                long reviewId = insertReview(connection, request, resolvedOrderItemId);

                /*
                 * Media chỉ là dữ liệu phụ.
                 * Nếu bảng media hoặc cột media thiếu, vẫn giữ review để admin thấy PENDING.
                 */
                try {
                    insertReviewMediaList(connection, reviewId, request.mediaList());
                } catch (SQLException mediaError) {
                    System.err.println("ReviewSubmitDAO.insertReviewMediaList warning: " + mediaError.getMessage());
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
            throw new RuntimeException("ReviewSubmitDAO.createReview error: " + e.getMessage(), e);
        }
    }

    private long insertReview(Connection connection, CreateReviewRequest request, Long resolvedOrderItemId) throws SQLException {
        List<String> columns = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        List<SqlValue> values = new ArrayList<>();

        addColumn(columns, placeholders, values, "rating", request.rating(), Types.INTEGER);
        addColumn(columns, placeholders, values, "comment", request.comment(), Types.LONGVARCHAR);
        addColumn(columns, placeholders, values, "created_at", Timestamp.valueOf(LocalDateTime.now()), Types.TIMESTAMP);
        addColumn(columns, placeholders, values, "author_id", request.authorId(), Types.INTEGER);

        if (columnExists(connection, "store_review", "user_id")) {
            addColumn(columns, placeholders, values, "user_id", request.authorId(), Types.INTEGER);
        }

        addColumn(columns, placeholders, values, "product_id", request.productId(), Types.BIGINT);

        if (columnExists(connection, "store_review", "order_id")) {
            addColumn(columns, placeholders, values, "order_id", request.orderId(), Types.BIGINT);
        }

        if (columnExists(connection, "store_review", "order_item_id")) {
            addColumn(columns, placeholders, values, "order_item_id", resolvedOrderItemId, Types.BIGINT);
        }

        if (columnExists(connection, "store_review", "has_emoji")) {
            addColumn(columns, placeholders, values, "has_emoji", containsEmoji(request.comment()), Types.BOOLEAN);
        }

        if (columnExists(connection, "store_review", "sentiment")) {
            addColumn(columns, placeholders, values, "sentiment", simpleSentiment(request.rating()), Types.INTEGER);
        }

        if (columnExists(connection, "store_review", "status")) {
            addColumn(columns, placeholders, values, "status", "PENDING", Types.VARCHAR);
        }

        if (columnExists(connection, "store_review", "is_hidden")) {
            addColumn(columns, placeholders, values, "is_hidden", false, Types.BOOLEAN);
        }

        if (columnExists(connection, "store_review", "review_image")) {
            addColumn(columns, placeholders, values, "review_image", emptyToNull(request.firstImageUrl()), Types.VARCHAR);
        }

        if (columnExists(connection, "store_review", "review_video")) {
            addColumn(columns, placeholders, values, "review_video", emptyToNull(request.firstVideoUrl()), Types.VARCHAR);
        }

        if (columnExists(connection, "store_review", "is_anonymous")) {
            addColumn(columns, placeholders, values, "is_anonymous", request.anonymous(), Types.BOOLEAN);
        }

        if (columnExists(connection, "store_review", "voucher_awarded")) {
            addColumn(columns, placeholders, values, "voucher_awarded", false, Types.BOOLEAN);
        }

        if (columnExists(connection, "store_review", "reward_points")) {
            addColumn(columns, placeholders, values, "reward_points", Math.max(0, request.rewardPoints()), Types.INTEGER);
        }

        if (columnExists(connection, "store_review", "points_awarded")) {
            addColumn(columns, placeholders, values, "points_awarded", false, Types.BOOLEAN);
        }

        if (columnExists(connection, "store_review", "seller_service_rating")) {
            addColumn(columns, placeholders, values, "seller_service_rating", clampRating(request.sellerServiceRating()), Types.INTEGER);
        }

        if (columnExists(connection, "store_review", "delivery_speed_rating")) {
            addColumn(columns, placeholders, values, "delivery_speed_rating", clampRating(request.deliverySpeedRating()), Types.INTEGER);
        }

        if (columnExists(connection, "store_review", "shipper_rating")) {
            addColumn(columns, placeholders, values, "shipper_rating", clampRating(request.shipperRating()), Types.INTEGER);
        }

        if (columnExists(connection, "store_review", "service_tags")) {
            addColumn(columns, placeholders, values, "service_tags", emptyToNull(request.serviceTags()), Types.VARCHAR);
        }

        if (columnExists(connection, "store_review", "service_comment")) {
            addColumn(columns, placeholders, values, "service_comment", emptyToNull(request.serviceComment()), Types.LONGVARCHAR);
        }

        String sql = "INSERT INTO store_review (" +
                String.join(", ", columns) +
                ") VALUES (" +
                String.join(", ", placeholders) +
                ")";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindValues(statement, values);

            int affectedRows = statement.executeUpdate();

            if (affectedRows <= 0) {
                throw new SQLException("Insert store_review không tạo dòng mới.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Không lấy được id đánh giá vừa tạo.");
                }

                return keys.getLong(1);
            }
        }
    }

    private void insertReviewMediaList(Connection connection, long reviewId, List<ReviewMedia> mediaList) throws SQLException {
        if (mediaList == null || mediaList.isEmpty()) {
            return;
        }

        if (!tableExists(connection, "store_review_media")) {
            return;
        }

        boolean hasOriginalName = columnExists(connection, "store_review_media", "original_name");
        boolean hasFileSize = columnExists(connection, "store_review_media", "file_size");
        boolean hasMimeType = columnExists(connection, "store_review_media", "mime_type");

        for (ReviewMedia media : mediaList) {
            List<String> columns = new ArrayList<>();
            List<String> placeholders = new ArrayList<>();
            List<SqlValue> values = new ArrayList<>();

            addColumn(columns, placeholders, values, "review_id", reviewId, Types.BIGINT);
            addColumn(columns, placeholders, values, "media_type", media.getMediaType(), Types.VARCHAR);
            addColumn(columns, placeholders, values, "media_url", media.getMediaUrl(), Types.VARCHAR);

            if (hasOriginalName) {
                addColumn(columns, placeholders, values, "original_name", emptyToNull(media.getOriginalName()), Types.VARCHAR);
            }

            if (hasFileSize) {
                addColumn(columns, placeholders, values, "file_size", media.getFileSize(), Types.BIGINT);
            }

            /*
             * Không gọi media.getMimeType() để tránh lỗi compile nếu model ReviewMedia chưa có method này.
             * Nếu cột mime_type tồn tại, để NULL vẫn an toàn nếu cột cho phép NULL.
             */
            if (hasMimeType) {
                addColumn(columns, placeholders, values, "mime_type", null, Types.VARCHAR);
            }

            String sql = "INSERT INTO store_review_media (" +
                    String.join(", ", columns) +
                    ") VALUES (" +
                    String.join(", ", placeholders) +
                    ")";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bindValues(statement, values);
                statement.executeUpdate();
            }
        }
    }

    /**
     * Admin duyệt review và cộng xu.
     * Nếu thiếu bảng/cột xu thì vẫn duyệt review, không làm chết chức năng duyệt.
     */
    public boolean approveReviewAndAwardPoints(long reviewId, int adminId) {
        try (Connection connection = DBConnection.getConnection()) {
            boolean oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                ReviewAwardData data = findReviewAwardData(connection, reviewId);

                if (data == null) {
                    connection.rollback();
                    connection.setAutoCommit(oldAutoCommit);
                    return false;
                }

                updateReviewApproved(connection, reviewId, adminId);

                if (!data.pointsAwarded() && data.rewardPoints() > 0) {
                    addUserRewardPointsIfPossible(connection, data.userId(), data.rewardPoints());
                    insertPointTransactionIfPossible(connection, data, reviewId);
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
            throw new RuntimeException("ReviewSubmitDAO.approveReviewAndAwardPoints error: " + e.getMessage(), e);
        }
    }

    private ReviewAwardData findReviewAwardData(Connection connection, long reviewId) throws SQLException {
        boolean hasUserId = columnExists(connection, "store_review", "user_id");
        boolean hasOrderId = columnExists(connection, "store_review", "order_id");
        boolean hasRewardPoints = columnExists(connection, "store_review", "reward_points");
        boolean hasPointsAwarded = columnExists(connection, "store_review", "points_awarded");

        StringBuilder sql = new StringBuilder("SELECT id, author_id");

        if (hasUserId) {
            sql.append(", user_id");
        }

        if (hasOrderId) {
            sql.append(", order_id");
        }

        if (hasRewardPoints) {
            sql.append(", reward_points");
        }

        if (hasPointsAwarded) {
            sql.append(", points_awarded");
        }

        sql.append(" FROM store_review WHERE id = ? FOR UPDATE");

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            statement.setLong(1, reviewId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                int userId = 0;

                if (hasUserId) {
                    userId = resultSet.getInt("user_id");
                    if (resultSet.wasNull() || userId <= 0) {
                        userId = resultSet.getInt("author_id");
                    }
                } else {
                    userId = resultSet.getInt("author_id");
                }

                Long orderId = null;

                if (hasOrderId) {
                    long rawOrderId = resultSet.getLong("order_id");
                    if (!resultSet.wasNull()) {
                        orderId = rawOrderId;
                    }
                }

                int rewardPoints = hasRewardPoints ? resultSet.getInt("reward_points") : 0;

                if (rewardPoints <= 0) {
                    rewardPoints = 200;
                }

                boolean pointsAwarded = hasPointsAwarded && resultSet.getBoolean("points_awarded");

                return new ReviewAwardData(userId, orderId, rewardPoints, pointsAwarded);
            }
        }
    }

    private void updateReviewApproved(Connection connection, long reviewId, int adminId) throws SQLException {
        List<String> sets = new ArrayList<>();
        List<SqlValue> values = new ArrayList<>();

        if (columnExists(connection, "store_review", "status")) {
            sets.add("status = ?");
            values.add(new SqlValue("APPROVED", Types.VARCHAR));
        }

        if (columnExists(connection, "store_review", "is_hidden")) {
            sets.add("is_hidden = ?");
            values.add(new SqlValue(false, Types.BOOLEAN));
        }

        if (columnExists(connection, "store_review", "approved_at")) {
            sets.add("approved_at = NOW()");
        }

        if (columnExists(connection, "store_review", "approved_by")) {
            sets.add("approved_by = ?");
            values.add(new SqlValue(adminId, Types.INTEGER));
        }

        if (columnExists(connection, "store_review", "points_awarded")) {
            sets.add("points_awarded = ?");
            values.add(new SqlValue(true, Types.BOOLEAN));
        }

        if (sets.isEmpty()) {
            return;
        }

        String sql = "UPDATE store_review SET " + String.join(", ", sets) + " WHERE id = ?";
        values.add(new SqlValue(reviewId, Types.BIGINT));

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindValues(statement, values);
            statement.executeUpdate();
        }
    }

    private void addUserRewardPointsIfPossible(Connection connection, int userId, int points) throws SQLException {
        if (userId <= 0 || points <= 0) {
            return;
        }

        if (!columnExists(connection, "users", "reward_points")) {
            return;
        }

        String sql = """
                UPDATE users
                SET reward_points = COALESCE(reward_points, 0) + ?
                WHERE id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, points);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    private void insertPointTransactionIfPossible(Connection connection, ReviewAwardData data, long reviewId) throws SQLException {
        if (!tableExists(connection, "user_point_transaction")) {
            return;
        }

        String sql = """
                INSERT INTO user_point_transaction (
                    user_id,
                    order_id,
                    review_id,
                    points,
                    type,
                    reason,
                    expired_at
                ) VALUES (?, ?, ?, ?, 'EARN', ?, DATE_ADD(NOW(), INTERVAL 90 DAY))
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, data.userId());

            if (data.orderId() == null) {
                statement.setNull(2, Types.BIGINT);
            } else {
                statement.setLong(2, data.orderId());
            }

            statement.setLong(3, reviewId);
            statement.setInt(4, data.rewardPoints());
            statement.setString(5, "Cộng xu sau khi đánh giá sản phẩm được duyệt");
            statement.executeUpdate();
        }
    }

    private Long findOrderItemId(Connection connection, long orderId, long productId) throws SQLException {
        String sql = """
                SELECT id
                FROM store_orderitem
                WHERE order_id = ?
                  AND product_id = ?
                ORDER BY id ASC
                LIMIT 1
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            statement.setLong(2, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("id");
                }

                return null;
            }
        }
    }

    private static void addColumn(
            List<String> columns,
            List<String> placeholders,
            List<SqlValue> values,
            String column,
            Object value,
            int sqlType
    ) {
        columns.add(column);
        placeholders.add("?");
        values.add(new SqlValue(value, sqlType));
    }

    private static void bindValues(PreparedStatement statement, List<SqlValue> values) throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            SqlValue value = values.get(i);
            int index = i + 1;

            if (value.value() == null) {
                statement.setNull(index, value.sqlType());
                continue;
            }

            switch (value.sqlType()) {
                case Types.INTEGER -> statement.setInt(index, ((Number) value.value()).intValue());
                case Types.BIGINT -> statement.setLong(index, ((Number) value.value()).longValue());
                case Types.BOOLEAN, Types.TINYINT -> statement.setBoolean(index, (Boolean) value.value());
                case Types.TIMESTAMP -> statement.setTimestamp(index, (Timestamp) value.value());
                case Types.VARCHAR, Types.LONGVARCHAR -> statement.setString(index, String.valueOf(value.value()));
                default -> statement.setObject(index, value.value());
            }
        }
    }

    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, columnName);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    private static boolean containsEmoji(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        return value.codePoints().anyMatch(cp -> Character.getType(cp) == Character.SURROGATE || cp > 0xFFFF);
    }

    private static int simpleSentiment(int rating) {
        return rating >= 4 ? 1 : 0;
    }

    private static int clampRating(int value) {
        return Math.max(1, Math.min(5, value));
    }

    private static String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private record SqlValue(Object value, int sqlType) {
    }

    private record ReviewAwardData(
            int userId,
            Long orderId,
            int rewardPoints,
            boolean pointsAwarded
    ) {
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
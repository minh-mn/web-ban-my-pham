package com.webshop.app.dao;

import com.webshop.app.model.Notification;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationDAO {

    private static final String TABLE_NAME = "notifications";

    private static final String SELECT_COLUMNS = """
            id,
            user_id,
            role_target,
            type,
            title,
            message,
            target_url,
            reference_type,
            reference_id,
            is_read,
            read_at,
            created_at,
            updated_at
            """;

    /* =========================================================
       CREATE
    ========================================================= */

    public long create(Notification notification) {
        try (Connection connection = DBConnection.getConnection()) {
            return create(connection, notification);
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.create error", e);
        }
    }

    public long create(Connection connection, Notification notification) throws SQLException {
        if (connection == null) {
            throw new SQLException("Connection must not be null");
        }

        if (notification == null) {
            throw new SQLException("Notification must not be null");
        }

        String roleTarget = normalizeRoleTarget(notification.getRoleTarget());
        Integer userId = notification.getUserId();

        /*
         * Thông báo USER phải có userId.
         * Thông báo ADMIN có thể userId = null.
         */
        if (Notification.ROLE_USER.equals(roleTarget) && (userId == null || userId <= 0)) {
            return 0;
        }

        String sql = """
                INSERT INTO notifications
                (
                    user_id,
                    role_target,
                    type,
                    title,
                    message,
                    target_url,
                    reference_type,
                    reference_id,
                    is_read,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, NOW())
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int index = 1;

            if (userId == null || userId <= 0) {
                statement.setNull(index++, Types.INTEGER);
            } else {
                statement.setInt(index++, userId);
            }

            statement.setString(index++, roleTarget);
            statement.setString(index++, normalizeType(notification.getType()));
            statement.setString(index++, trimToDefault(notification.getTitle(), "Thông báo"));
            statement.setString(index++, trimToDefault(notification.getMessage(), "Bạn có thông báo mới."));
            statement.setString(index++, trimToNull(notification.getTargetUrl()));
            statement.setString(index++, trimToNullUpper(notification.getReferenceType()));

            Long referenceId = notification.getReferenceId();
            if (referenceId == null || referenceId <= 0) {
                statement.setNull(index++, Types.BIGINT);
            } else {
                statement.setLong(index++, referenceId);
            }

            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        }

        return 0;
    }

    /*
     * Hàm tương thích code cũ.
     * Code cũ truyền orderId thì chuyển thành reference_type = ORDER.
     */
    public long create(Connection connection,
                       int userId,
                       Long orderId,
                       String type,
                       String title,
                       String message,
                       String targetUrl) throws SQLException {

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setRoleTarget(Notification.ROLE_USER);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetUrl(targetUrl);

        if (orderId != null && orderId > 0) {
            notification.setReferenceType(Notification.REF_ORDER);
            notification.setReferenceId(orderId);
        }

        return create(connection, notification);
    }

    public long createUserNotification(int userId,
                                       String type,
                                       String title,
                                       String message,
                                       String targetUrl,
                                       String referenceType,
                                       Long referenceId) {
        try (Connection connection = DBConnection.getConnection()) {
            return createUserNotification(
                    connection,
                    userId,
                    type,
                    title,
                    message,
                    targetUrl,
                    referenceType,
                    referenceId
            );
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.createUserNotification error", e);
        }
    }

    public long createUserNotification(Connection connection,
                                       int userId,
                                       String type,
                                       String title,
                                       String message,
                                       String targetUrl,
                                       String referenceType,
                                       Long referenceId) throws SQLException {

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setRoleTarget(Notification.ROLE_USER);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetUrl(targetUrl);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);

        return create(connection, notification);
    }

    public long createAdminNotification(String type,
                                        String title,
                                        String message,
                                        String targetUrl,
                                        String referenceType,
                                        Long referenceId) {
        try (Connection connection = DBConnection.getConnection()) {
            return createAdminNotification(
                    connection,
                    type,
                    title,
                    message,
                    targetUrl,
                    referenceType,
                    referenceId
            );
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.createAdminNotification error", e);
        }
    }

    public long createAdminNotification(Connection connection,
                                        String type,
                                        String title,
                                        String message,
                                        String targetUrl,
                                        String referenceType,
                                        Long referenceId) throws SQLException {

        Notification notification = new Notification();
        notification.setUserId((Integer) null);
        notification.setRoleTarget(Notification.ROLE_ADMIN);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetUrl(targetUrl);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);

        return create(connection, notification);
    }

    /* =========================================================
       ORDER NOTIFICATIONS
    ========================================================= */

    public void createOrderNotification(Connection connection,
                                        int userId,
                                        long orderId,
                                        String status) throws SQLException {

        if (userId <= 0 || orderId <= 0) {
            return;
        }

        OrderNotificationPayload payload = buildOrderPayload(orderId, status);

        if (payload == null) {
            return;
        }

        createUserNotification(
                connection,
                userId,
                payload.type(),
                payload.title(),
                payload.message(),
                "/orders/detail?id=" + orderId,
                Notification.REF_ORDER,
                orderId
        );
    }

    public long createOrderCreatedNotification(Connection connection,
                                               int userId,
                                               long orderId) throws SQLException {

        if (userId <= 0 || orderId <= 0) {
            return 0;
        }

        return createUserNotification(
                connection,
                userId,
                "ORDER_CREATED",
                "Đặt hàng thành công",
                "Đơn hàng #" + orderId + " đã được tạo thành công.",
                "/orders/detail?id=" + orderId,
                Notification.REF_ORDER,
                orderId
        );
    }

    public long createAdminOrderCreatedNotification(Connection connection,
                                                    long orderId,
                                                    int userId) throws SQLException {

        if (orderId <= 0) {
            return 0;
        }

        return createAdminNotification(
                connection,
                "ORDER_CREATED",
                "Có đơn hàng mới",
                "Khách hàng #" + userId + " vừa đặt đơn hàng #" + orderId + ".",
                "/admin/orders?action=detail&id=" + orderId,
                Notification.REF_ORDER,
                orderId
        );
    }

    public long createAdminOrderCreatedNotification(long orderId, int userId) {
        try (Connection connection = DBConnection.getConnection()) {
            return createAdminOrderCreatedNotification(connection, orderId, userId);
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.createAdminOrderCreatedNotification error", e);
        }
    }

    private OrderNotificationPayload buildOrderPayload(long orderId, String status) {
        String value = trimToDefault(status, "").toLowerCase(Locale.ROOT);

        return switch (value) {
            case "pending", "processing", "created" -> new OrderNotificationPayload(
                    "ORDER_CREATED",
                    "Đặt hàng thành công",
                    "Đơn hàng #" + orderId + " đã được tạo thành công."
            );

            case "confirmed" -> new OrderNotificationPayload(
                    "ORDER_CONFIRMED",
                    "Đơn hàng đã được xác nhận",
                    "Đơn hàng #" + orderId + " đã được admin xác nhận."
            );

            case "shipping", "delivering" -> new OrderNotificationPayload(
                    "ORDER_SHIPPING",
                    "Đơn hàng đang được giao",
                    "Đơn hàng #" + orderId + " đang trên đường tới bạn."
            );

            case "completed", "delivered" -> new OrderNotificationPayload(
                    "ORDER_DELIVERED",
                    "Đơn hàng đã giao thành công",
                    "Đơn hàng #" + orderId + " đã được giao thành công."
            );

            case "failed", "delivery_failed" -> new OrderNotificationPayload(
                    "ORDER_DELIVERY_FAILED",
                    "Giao hàng thất bại",
                    "Đơn hàng #" + orderId + " giao hàng thất bại. Shop sẽ liên hệ lại với bạn."
            );

            case "cancelled", "canceled" -> new OrderNotificationPayload(
                    "ORDER_CANCELLED",
                    "Đơn hàng đã bị hủy",
                    "Đơn hàng #" + orderId + " đã bị hủy."
            );

            default -> null;
        };
    }

    /* =========================================================
       CANCEL / RETURN / REVIEW NOTIFICATIONS
    ========================================================= */

    public long createCancelRequestNotification(Connection connection,
                                                long orderId,
                                                int userId,
                                                long cancelRequestId) throws SQLException {

        return createAdminNotification(
                connection,
                "CANCEL_REQUEST_CREATED",
                "Có yêu cầu hủy đơn",
                "Khách hàng #" + userId + " đã gửi yêu cầu hủy đơn hàng #" + orderId + ".",
                "/admin/orders?action=detail&id=" + orderId,
                Notification.REF_CANCEL_REQUEST,
                cancelRequestId
        );
    }

    public long createCancelRequestResultNotification(Connection connection,
                                                      int userId,
                                                      long orderId,
                                                      long cancelRequestId,
                                                      boolean approved) throws SQLException {

        String type = approved ? "CANCEL_REQUEST_APPROVED" : "CANCEL_REQUEST_REJECTED";
        String title = approved ? "Yêu cầu hủy đơn đã được duyệt" : "Yêu cầu hủy đơn bị từ chối";
        String message = approved
                ? "Yêu cầu hủy đơn hàng #" + orderId + " của bạn đã được duyệt."
                : "Yêu cầu hủy đơn hàng #" + orderId + " của bạn đã bị từ chối.";

        return createUserNotification(
                connection,
                userId,
                type,
                title,
                message,
                "/orders/detail?id=" + orderId,
                Notification.REF_CANCEL_REQUEST,
                cancelRequestId
        );
    }

    public long createReturnRequestNotification(Connection connection,
                                                long orderId,
                                                int userId,
                                                long returnRequestId) throws SQLException {

        return createAdminNotification(
                connection,
                "RETURN_REQUEST_CREATED",
                "Có yêu cầu hoàn hàng",
                "Khách hàng #" + userId + " đã gửi yêu cầu hoàn hàng cho đơn hàng #" + orderId + ".",
                "/admin/orders?action=detail&id=" + orderId,
                Notification.REF_RETURN_REQUEST,
                returnRequestId
        );
    }

    public long createReturnRequestResultNotification(Connection connection,
                                                      int userId,
                                                      long orderId,
                                                      long returnRequestId,
                                                      boolean approved) throws SQLException {

        String type = approved ? "RETURN_REQUEST_APPROVED" : "RETURN_REQUEST_REJECTED";
        String title = approved ? "Yêu cầu hoàn hàng đã được duyệt" : "Yêu cầu hoàn hàng bị từ chối";
        String message = approved
                ? "Yêu cầu hoàn hàng cho đơn hàng #" + orderId + " của bạn đã được duyệt."
                : "Yêu cầu hoàn hàng cho đơn hàng #" + orderId + " của bạn đã bị từ chối.";

        return createUserNotification(
                connection,
                userId,
                type,
                title,
                message,
                "/orders/detail?id=" + orderId,
                Notification.REF_RETURN_REQUEST,
                returnRequestId
        );
    }

    public long createReviewNotification(Connection connection,
                                         int userId,
                                         long reviewId,
                                         long orderId) throws SQLException {

        return createAdminNotification(
                connection,
                "REVIEW_CREATED",
                "Có đánh giá mới",
                "Khách hàng #" + userId + " vừa gửi đánh giá mới cần kiểm duyệt.",
                "/admin/reviews?action=detail&id=" + reviewId,
                Notification.REF_REVIEW,
                reviewId
        );
    }

    public long createReviewResultNotification(Connection connection,
                                               int userId,
                                               long reviewId,
                                               long orderId,
                                               boolean approved) throws SQLException {

        String type = approved ? "REVIEW_APPROVED" : "REVIEW_REJECTED";
        String title = approved ? "Đánh giá đã được duyệt" : "Đánh giá bị từ chối";
        String message = approved
                ? "Đánh giá của bạn đã được duyệt và hiển thị trên hệ thống."
                : "Đánh giá của bạn đã bị từ chối hoặc bị ẩn.";

        String targetUrl = orderId > 0
                ? "/orders/detail?id=" + orderId
                : "/notifications";

        return createUserNotification(
                connection,
                userId,
                type,
                title,
                message,
                targetUrl,
                Notification.REF_REVIEW,
                reviewId
        );
    }

    /* =========================================================
       QUERY USER / ADMIN
    ========================================================= */

    public List<Notification> findByUserId(int userId, int limit) {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM notifications
                WHERE user_id = ?
                  AND role_target = 'USER'
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """;

        List<Notification> notifications = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, normalizeLimit(limit));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(mapRow(resultSet));
                }
            }

            return notifications;
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.findByUserId error", e);
        }
    }

    public List<Notification> findLatestByUser(int userId, int limit) {
        return findByUserId(userId, limit);
    }

    public List<Notification> getNotificationsByUserId(int userId) {
        return findByUserId(userId, 10);
    }

    public List<Notification> findLatestByAdmin(int limit) {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM notifications
                WHERE role_target = 'ADMIN'
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """;

        List<Notification> notifications = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, normalizeLimit(limit));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(mapRow(resultSet));
                }
            }

            return notifications;
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.findLatestByAdmin error", e);
        }
    }

    public List<Notification> findAllByUser(int userId, int page, int pageSize) {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM notifications
                WHERE user_id = ?
                  AND role_target = 'USER'
                ORDER BY created_at DESC, id DESC
                LIMIT ? OFFSET ?
                """;

        List<Notification> notifications = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            int safePageSize = normalizeLimit(pageSize);
            int offset = Math.max(0, page - 1) * safePageSize;

            statement.setInt(1, userId);
            statement.setInt(2, safePageSize);
            statement.setInt(3, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(mapRow(resultSet));
                }
            }

            return notifications;
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.findAllByUser error", e);
        }
    }

    public List<Notification> findAllByAdmin(int page, int pageSize) {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM notifications
                WHERE role_target = 'ADMIN'
                ORDER BY created_at DESC, id DESC
                LIMIT ? OFFSET ?
                """;

        List<Notification> notifications = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            int safePageSize = normalizeLimit(pageSize);
            int offset = Math.max(0, page - 1) * safePageSize;

            statement.setInt(1, safePageSize);
            statement.setInt(2, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(mapRow(resultSet));
                }
            }

            return notifications;
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.findAllByAdmin error", e);
        }
    }

    public int countByUser(int userId) {
        String sql = """
                SELECT COUNT(*)
                FROM notifications
                WHERE user_id = ?
                  AND role_target = 'USER'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.countByUser error", e);
        }
    }

    public int countByAdmin() {
        String sql = """
                SELECT COUNT(*)
                FROM notifications
                WHERE role_target = 'ADMIN'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return resultSet.next() ? resultSet.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.countByAdmin error", e);
        }
    }

    public int countUnreadByUserId(int userId) {
        String sql = """
                SELECT COUNT(*)
                FROM notifications
                WHERE user_id = ?
                  AND role_target = 'USER'
                  AND is_read = 0
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.countUnreadByUserId error", e);
        }
    }

    public int getUnreadCount(int userId) {
        return countUnreadByUserId(userId);
    }

    public int countUnreadByAdmin() {
        String sql = """
                SELECT COUNT(*)
                FROM notifications
                WHERE role_target = 'ADMIN'
                  AND is_read = 0
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return resultSet.next() ? resultSet.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.countUnreadByAdmin error", e);
        }
    }

    /* =========================================================
       MARK READ
    ========================================================= */

    public boolean markRead(long id, int userId) {
        String sql = """
                UPDATE notifications
                SET is_read = 1,
                    read_at = NOW()
                WHERE id = ?
                  AND user_id = ?
                  AND role_target = 'USER'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            statement.setInt(2, userId);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.markRead error", e);
        }
    }

    public boolean markAsRead(long id) {
        String sql = """
                UPDATE notifications
                SET is_read = 1,
                    read_at = NOW()
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.markAsRead error", e);
        }
    }

    public boolean markAdminRead(long id) {
        String sql = """
                UPDATE notifications
                SET is_read = 1,
                    read_at = NOW()
                WHERE id = ?
                  AND role_target = 'ADMIN'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.markAdminRead error", e);
        }
    }

    public int markAllAsReadByUser(int userId) {
        String sql = """
                UPDATE notifications
                SET is_read = 1,
                    read_at = NOW()
                WHERE user_id = ?
                  AND role_target = 'USER'
                  AND is_read = 0
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.markAllAsReadByUser error", e);
        }
    }

    public int markAllAsReadByAdmin() {
        String sql = """
                UPDATE notifications
                SET is_read = 1,
                    read_at = NOW()
                WHERE role_target = 'ADMIN'
                  AND is_read = 0
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.markAllAsReadByAdmin error", e);
        }
    }

    /* =========================================================
       BROADCAST / SYSTEM
    ========================================================= */

    public boolean broadcastNotification(String type, String title, String message, String targetUrl) {
        String selectUsersSql = """
                SELECT id
                FROM users
                WHERE active = 1
                """;

        String insertSql = """
                INSERT INTO notifications
                (
                    user_id,
                    role_target,
                    type,
                    title,
                    message,
                    target_url,
                    reference_type,
                    reference_id,
                    is_read,
                    created_at
                )
                VALUES (?, 'USER', ?, ?, ?, ?, 'SYSTEM', NULL, 0, NOW())
                """;

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement selectStatement = connection.prepareStatement(selectUsersSql);
                 ResultSet resultSet = selectStatement.executeQuery();
                 PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {

                while (resultSet.next()) {
                    int userId = resultSet.getInt("id");

                    int index = 1;
                    insertStatement.setInt(index++, userId);
                    insertStatement.setString(index++, normalizeType(type));
                    insertStatement.setString(index++, trimToDefault(title, "Thông báo"));
                    insertStatement.setString(index++, trimToDefault(message, "Bạn có thông báo mới."));
                    insertStatement.setString(index++, trimToNull(targetUrl));
                    insertStatement.addBatch();
                }

                insertStatement.executeBatch();
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            rollbackQuietly(connection);
            throw new RuntimeException("NotificationDAO.broadcastNotification error", e);
        } finally {
            closeQuietly(connection);
        }
    }

    public List<Notification> getSystemBroadcastHistory() {
        String sql = """
                SELECT
                    MIN(id) AS id,
                    NULL AS user_id,
                    'USER' AS role_target,
                    type,
                    title,
                    message,
                    target_url,
                    'SYSTEM' AS reference_type,
                    NULL AS reference_id,
                    1 AS is_read,
                    NULL AS read_at,
                    MIN(created_at) AS created_at,
                    MAX(updated_at) AS updated_at
                FROM notifications
                WHERE reference_type = 'SYSTEM'
                  AND role_target = 'USER'
                GROUP BY type, title, message, target_url
                ORDER BY id DESC
                LIMIT 100
                """;

        List<Notification> notifications = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                notifications.add(mapRow(resultSet));
            }

            return notifications;
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.getSystemBroadcastHistory error", e);
        }
    }

    public void sendWishlistDiscountNotification(int productId, String productName) {
        WishlistDAO wishlistDAO = new WishlistDAO();
        List<Integer> userIds = wishlistDAO.findUserIdsByProduct(productId);

        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        String type = "WISHLIST_DISCOUNT";
        String title = "Sản phẩm yêu thích đang giảm giá";
        String message = "Sản phẩm \"" + trimToDefault(productName, "yêu thích") + "\" trong danh sách yêu thích của bạn vừa giảm giá.";
        String targetUrl = "/products/detail?id=" + productId;

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            for (int userId : userIds) {
                createUserNotification(
                        connection,
                        userId,
                        type,
                        title,
                        message,
                        targetUrl,
                        "PRODUCT",
                        (long) productId
                );
            }

            connection.commit();
        } catch (SQLException e) {
            rollbackQuietly(connection);
            throw new RuntimeException("NotificationDAO.sendWishlistDiscountNotification error", e);
        } finally {
            closeQuietly(connection);
        }
    }

    /* =========================================================
       MAP / NORMALIZE
    ========================================================= */

    private Notification mapRow(ResultSet resultSet) throws SQLException {
        Notification notification = new Notification();

        notification.setId(resultSet.getLong("id"));

        int userId = resultSet.getInt("user_id");
        if (resultSet.wasNull()) {
            notification.setUserId((Integer) null);
        } else {
            notification.setUserId(userId);
        }

        notification.setRoleTarget(resultSet.getString("role_target"));
        notification.setType(resultSet.getString("type"));
        notification.setTitle(resultSet.getString("title"));
        notification.setMessage(resultSet.getString("message"));
        notification.setTargetUrl(resultSet.getString("target_url"));
        notification.setReferenceType(resultSet.getString("reference_type"));

        long referenceId = resultSet.getLong("reference_id");
        if (!resultSet.wasNull()) {
            notification.setReferenceId(referenceId);
        }

        notification.setRead(resultSet.getBoolean("is_read"));

        Timestamp readAt = resultSet.getTimestamp("read_at");
        if (readAt != null) {
            notification.setReadAt(readAt.toLocalDateTime());
        }

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            notification.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            notification.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return notification;
    }

    private String normalizeRoleTarget(String roleTarget) {
        String value = trimToDefault(roleTarget, Notification.ROLE_USER).toUpperCase(Locale.ROOT);

        if (Notification.ROLE_ADMIN.equals(value)) {
            return Notification.ROLE_ADMIN;
        }

        return Notification.ROLE_USER;
    }

    private String normalizeType(String type) {
        String value = trimToDefault(type, "SYSTEM").toUpperCase(Locale.ROOT);

        return switch (value) {
            case "ORDER_CREATED",
                 "ORDER_CONFIRMED",
                 "ORDER_SHIPPING",
                 "ORDER_DELIVERED",
                 "ORDER_DELIVERY_FAILED",
                 "ORDER_CANCELLED",

                 "CANCEL_REQUEST_CREATED",
                 "CANCEL_REQUEST_APPROVED",
                 "CANCEL_REQUEST_REJECTED",

                 "RETURN_REQUEST_CREATED",
                 "RETURN_REQUEST_APPROVED",
                 "RETURN_REQUEST_REJECTED",

                 "REVIEW_CREATED",
                 "REVIEW_APPROVED",
                 "REVIEW_REJECTED",
                 "REVIEW_HIDDEN",

                 "WISHLIST_DISCOUNT",
                 "SYSTEM",
                 "ORDER",
                 "CANCEL",
                 "RETURN",
                 "REFUND",
                 "SHIPPING",
                 "RECEIVED",
                 "VOUCHER" -> value;

            default -> "SYSTEM";
        };
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private String trimToNullUpper(String value) {
        String trimmed = trimToNull(value);

        if (trimmed == null) {
            return null;
        }

        return trimmed.toUpperCase(Locale.ROOT);
    }

    private String trimToDefault(String value, String defaultValue) {
        String trimmed = trimToNull(value);

        return trimmed == null ? defaultValue : trimmed;
    }

    private int normalizeLimit(int limit) {
        return Math.max(1, Math.min(limit, 100));
    }

    private void rollbackQuietly(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void closeQuietly(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.setAutoCommit(true);
            connection.close();
        } catch (SQLException ignored) {
        }
    }

    private record OrderNotificationPayload(
            String type,
            String title,
            String message
    ) {
    }
}

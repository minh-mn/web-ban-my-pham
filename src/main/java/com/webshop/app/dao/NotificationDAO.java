package com.webshop.app.dao;

import com.webshop.app.model.Notification;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationDAO {

    private static final String SELECT_COLUMNS = """
            id,
            user_id,
            order_id,
            type,
            title,
            message,
            target_url,
            is_read,
            created_at,
            read_at
            """;

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
        if (notification.getUserId() <= 0) {
            return 0;
        }

        String sql = """
                INSERT INTO store_notification
                (user_id, order_id, type, title, message, target_url, is_read, created_at)
                VALUES (?, ?, ?, ?, ?, ?, 0, NOW())
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int index = 1;
            statement.setInt(index++, notification.getUserId());
            if (notification.getOrderId() == null) {
                statement.setNull(index++, java.sql.Types.BIGINT);
            } else {
                statement.setLong(index++, notification.getOrderId());
            }
            statement.setString(index++, normalizeType(notification.getType()));
            statement.setString(index++, trimToDefault(notification.getTitle(), "Thông báo"));
            statement.setString(index++, trimToDefault(notification.getMessage(), "Bạn có thông báo mới."));
            statement.setString(index++, trimToNull(notification.getTargetUrl()));
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        }

        return 0;
    }

    public long create(Connection connection,
                       int userId,
                       Long orderId,
                       String type,
                       String title,
                       String message,
                       String targetUrl) throws SQLException {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setOrderId(orderId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetUrl(targetUrl);
        return create(connection, notification);
    }

    public List<Notification> findByUserId(int userId, int limit) {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_notification
                WHERE user_id = ?
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """;

        List<Notification> notifications = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, Math.max(1, Math.min(limit, 100)));

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

    public int countUnreadByUserId(int userId) {
        String sql = """
                SELECT COUNT(*)
                FROM store_notification
                WHERE user_id = ?
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

    public boolean markRead(long id, int userId) {
        String sql = """
                UPDATE store_notification
                SET is_read = 1,
                    read_at = NOW()
                WHERE id = ?
                  AND user_id = ?
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

    private Notification mapRow(ResultSet resultSet) throws SQLException {
        Notification notification = new Notification();
        notification.setId(resultSet.getLong("id"));
        notification.setUserId(resultSet.getInt("user_id"));

        long orderId = resultSet.getLong("order_id");
        if (!resultSet.wasNull()) {
            notification.setOrderId(orderId);
        }

        notification.setType(resultSet.getString("type"));
        notification.setTitle(resultSet.getString("title"));
        notification.setMessage(resultSet.getString("message"));
        notification.setTargetUrl(resultSet.getString("target_url"));
        notification.setRead(resultSet.getBoolean("is_read"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            notification.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp readAt = resultSet.getTimestamp("read_at");
        if (readAt != null) {
            notification.setReadAt(readAt.toLocalDateTime());
        }

        return notification;
    }

    private String normalizeType(String type) {
        String value = trimToDefault(type, "SYSTEM").toUpperCase(Locale.ROOT);
        return switch (value) {
            case "ORDER", "CANCEL", "RETURN", "REFUND", "SHIPPING", "RECEIVED", "SYSTEM" -> value;
            default -> "SYSTEM";
        };
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String trimToDefault(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }
}

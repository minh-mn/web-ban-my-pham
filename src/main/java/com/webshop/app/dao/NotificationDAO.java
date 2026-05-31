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

    // 1. Hàm phát hành thông báo tới TẤT CẢ người dùng hệ thống (Dùng Batch Insert tối ưu)
    public boolean broadcastNotification(String type, String title, String message, String targetUrl) {
        String getAllUsersSql = "SELECT id FROM account";
        String insertSql = "INSERT INTO store_notification (user_id, type, title, message, target_url, is_read, created_at) VALUES (?, ?, ?, ?, ?, false, NOW())";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement getStmt = conn.prepareStatement(getAllUsersSql);
             ResultSet rs = getStmt.executeQuery();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            conn.setAutoCommit(false);

            while (rs.next()) {
                int userId = rs.getInt("id");
                insertStmt.setInt(1, userId);
                insertStmt.setString(2, type);
                insertStmt.setString(3, title);
                insertStmt.setString(4, message);
                insertStmt.setString(5, targetUrl);
                insertStmt.addBatch(); 
            }

            insertStmt.executeBatch(); 
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. Hàm đếm số thông báo CHƯA ĐỌC của 1 người dùng cụ thể
    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM store_notification WHERE user_id = ? AND is_read = false";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 3. Hàm lấy danh sách 10 thông báo MỚI NHẤT của 1 người dùng cụ thể
    public List<Notification> getNotificationsByUserId(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT id, user_id, type, title, message, target_url, is_read, created_at FROM store_notification WHERE user_id = ? ORDER BY created_at DESC LIMIT 10";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getLong("id"));
                    n.setUserId(rs.getInt("user_id"));
                    n.setType(rs.getString("type"));
                    n.setTitle(rs.getString("title"));
                    n.setMessage(rs.getString("message"));
                    n.setTargetUrl(rs.getString("target_url"));
                    n.setRead(rs.getBoolean("is_read"));
                    list.add(n);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy danh sách lịch sử các thông báo hệ thống để hiển thị trên trang danh sách Admin
     */
    public List<Notification> getSystemBroadcastHistory() {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT MIN(id) as id, type, title, message, target_url FROM store_notification WHERE order_id IS NULL GROUP BY title, message, type, target_url ORDER BY id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Notification n = new Notification();
                n.setId(resultSet.getLong("id"));
                n.setType(resultSet.getString("type"));
                n.setTitle(resultSet.getString("title"));
                n.setMessage(resultSet.getString("message"));
                n.setTargetUrl(resultSet.getString("target_url"));
                list.add(n);
            }
        } catch (SQLException e) {
            throw new RuntimeException("NotificationDAO.getSystemBroadcastHistory error", e);
        }
        return list;
    }

    /**
     * Đánh dấu một thông báo là đã đọc bằng ID
     */
    public boolean markAsRead(long id) {
        String sql = "UPDATE store_notification SET is_read = true, read_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("NotificationDAO.markAsRead error: " + e.getMessage());
            return false;
        }
    }

    public void sendWishlistDiscountNotification(int productId, String productName) {
        WishlistDAO wishlistDAO = new WishlistDAO();
        // 1. Lấy danh sách tất cả ID người dùng đang lưu sản phẩm này trong Wishlist
        List<Integer> userIds = wishlistDAO.findUserIdsByProduct(productId);

        // Nếu không có ai thích sản phẩm này thì dừng lại luôn cho nhẹ máy
        if (userIds == null || userIds.isEmpty()) return;

        // 2. Cấu hình nội dung thông báo
        String type = "VOUCHER";
        String title = "!!! Sản phẩm yêu thích của bạn ĐANG GIẢM GIÁ !!!";
        String message = "Sản phẩm \"" + productName + "\" trong danh sách yêu thích của bạn vừa hạ giá sốc. Click mua ngay kẻo lỡ!";
        String targetUrl = "/products/detail?id=" + productId; 

        // 3. Thực hiện ghi vào DB (Dùng chung 1 Connection + Transaction để tăng tối đa hiệu năng)
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            for (int userId : userIds) {
                // Gọi hàm nạp chồng (overload) có sẵn trong NotificationDAO của bạn
                this.create(conn, userId, null, type, title, message, targetUrl);
            }

            conn.commit(); // Hoàn tất lưu toàn bộ thông báo
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createOrderNotification(Connection conn, int userId, long orderId, String status) 
        throws SQLException {

        String title;
        String message;

        String targetUrl = "/orders/detail?id=" + orderId;

        switch (status.toLowerCase()) {
            case "pending":
                title = "Đặt hàng thành công";
                message = "Đơn hàng #" + orderId + " đã được tạo thành công.";
                break;
                
            case "processing":
                title = "Đơn hàng đã được xác nhận";
                message = "Đơn hàng #" + orderId + " đang được chuẩn bị.";
                break;
                
            case "shipping":
                title = "Đơn hàng đang được giao";
                message = "Đơn hàng #" + orderId + " đang trên đường tới bạn.";
                break;

            case "completed":
                title = "Đơn hàng hoàn thành";
                message = "Đơn hàng #" + orderId + " đã giao thành công.";
                break;

            case "cancelled":

            case "canceled":
                title = "Đơn hàng đã bị hủy";
                message = "Đơn hàng #" + orderId + " đã bị hủy.";
                break;

            default:
                return;
        }

        String sql = """
            INSERT INTO store_notification(
                user_id,
                order_id,
                type,
                title,
                message,
                target_url,
                is_read,
                created_at
            )
            VALUES (?, ?, 'ORDER', ?, ?, ?, 0, NOW())
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setLong(2, orderId);
            ps.setString(3, title);
            ps.setString(4, message);
            ps.setString(5, targetUrl);

            ps.executeUpdate();
        }
    }
}

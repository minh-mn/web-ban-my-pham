package com.webshop.app.service;

import com.webshop.app.dao.NotificationDAO;
import com.webshop.app.model.User;
import com.webshop.app.utils.DBConnection;
import com.webshop.app.utils.EmailUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderNotificationService {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    public void notifyCancelRequestedSafely(Connection connection, int orderId, User customer, String reason) {
        try {
            String customerName = customer == null ? "Khách hàng" : safe(customer.getFullName(), customer.getUsername());
            String title = "Yêu cầu hủy đơn hàng #" + orderId;
            String message = customerName + " đã gửi yêu cầu hủy đơn hàng #" + orderId + ". Lý do: " + safe(reason, "Không có lý do.");

            for (AdminContact admin : findAdminContacts(connection)) {
                notificationDAO.create(
                        connection,
                        admin.userId,
                        (long) orderId,
                        "CANCEL",
                        title,
                        message,
                        "/admin/cancel-requests"
                );
                sendEmailSafely(admin.email, title, buildSimpleHtml(title, message, "/admin/cancel-requests"));
            }
        } catch (Exception e) {
            System.err.println("notifyCancelRequestedSafely error: " + e.getMessage());
        }
    }

    public void notifyCancelProcessedSafely(int orderId, int userId, String status, String adminNote) {
        try (Connection connection = DBConnection.getConnection()) {
            notifyCancelProcessedSafely(connection, orderId, userId, status, adminNote);
        } catch (Exception e) {
            System.err.println("notifyCancelProcessedSafely error: " + e.getMessage());
        }
    }

    public void notifyCancelProcessedSafely(Connection connection, int orderId, int userId, String status, String adminNote) {
        try {
            boolean approved = "APPROVED".equalsIgnoreCase(status);
            String title = approved
                    ? "Yêu cầu hủy đơn #" + orderId + " đã được duyệt"
                    : "Yêu cầu hủy đơn #" + orderId + " đã bị từ chối";
            String message = approved
                    ? "Shop đã xác nhận hủy đơn hàng #" + orderId + ". " + safe(adminNote, "")
                    : "Shop chưa thể hủy đơn hàng #" + orderId + ". " + safe(adminNote, "Vui lòng liên hệ shop để được hỗ trợ.");

            notificationDAO.create(connection, userId, (long) orderId, "CANCEL", title, message, "/orders/detail?id=" + orderId);
            sendEmailSafely(findUserEmail(connection, userId), title, buildSimpleHtml(title, message, "/orders/detail?id=" + orderId));
        } catch (Exception e) {
            System.err.println("notifyCancelProcessedSafely error: " + e.getMessage());
        }
    }

    public void notifyReturnRequestedSafely(Connection connection, int orderId, User customer, String reason) {
        try {
            String customerName = customer == null ? "Khách hàng" : safe(customer.getFullName(), customer.getUsername());
            String title = "Yêu cầu hoàn hàng #" + orderId;
            String message = customerName + " đã gửi yêu cầu hoàn hàng cho đơn #" + orderId + ". Lý do: " + safe(reason, "Không có lý do.");

            for (AdminContact admin : findAdminContacts(connection)) {
                notificationDAO.create(connection, admin.userId, (long) orderId, "RETURN", title, message, "/admin/returns");
                sendEmailSafely(admin.email, title, buildSimpleHtml(title, message, "/admin/returns"));
            }
        } catch (Exception e) {
            System.err.println("notifyReturnRequestedSafely error: " + e.getMessage());
        }
    }

    public void notifyReturnProcessedSafely(int orderId, int userId, String status, String adminNote) {
        try (Connection connection = DBConnection.getConnection()) {
            String title = switch (safe(status, "REQUESTED").toUpperCase()) {
                case "APPROVED" -> "Yêu cầu hoàn hàng #" + orderId + " đã được duyệt";
                case "REJECTED" -> "Yêu cầu hoàn hàng #" + orderId + " đã bị từ chối";
                case "RETURNED" -> "Shop đã nhận hàng hoàn của đơn #" + orderId;
                case "REFUNDED" -> "Đơn #" + orderId + " đã được hoàn tiền";
                default -> "Cập nhật yêu cầu hoàn hàng #" + orderId;
            };
            String message = title + ". " + safe(adminNote, "Vui lòng xem chi tiết đơn hàng để biết thêm thông tin.");
            notificationDAO.create(connection, userId, (long) orderId, "RETURN", title, message, "/orders/detail?id=" + orderId);
            sendEmailSafely(findUserEmail(connection, userId), title, buildSimpleHtml(title, message, "/orders/detail?id=" + orderId));
        } catch (Exception e) {
            System.err.println("notifyReturnProcessedSafely error: " + e.getMessage());
        }
    }

    public void notifyReceivedConfirmedSafely(int orderId, int userId, boolean autoCompleted) {
        try (Connection connection = DBConnection.getConnection()) {
            String title = autoCompleted
                    ? "Đơn hàng #" + orderId + " đã tự động xác nhận nhận hàng"
                    : "Đã xác nhận nhận hàng cho đơn #" + orderId;
            String message = autoCompleted
                    ? "Hệ thống đã tự động đánh dấu đơn hàng là đã nhận sau 7 ngày kể từ khi giao thành công."
                    : "Cảm ơn bạn đã xác nhận đã nhận đơn hàng. Bạn có thể đánh giá sản phẩm trong chi tiết đơn hàng.";
            notificationDAO.create(connection, userId, (long) orderId, "RECEIVED", title, message, "/orders/detail?id=" + orderId);
            sendEmailSafely(findUserEmail(connection, userId), title, buildSimpleHtml(title, message, "/orders/detail?id=" + orderId));
        } catch (Exception e) {
            System.err.println("notifyReceivedConfirmedSafely error: " + e.getMessage());
        }
    }

    private List<AdminContact> findAdminContacts(Connection connection) throws SQLException {
        String sql = """
                SELECT id, email
                FROM users
                WHERE UPPER(role) = 'ADMIN'
                  AND active = 1
                """;
        List<AdminContact> admins = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                admins.add(new AdminContact(resultSet.getInt("id"), resultSet.getString("email")));
            }
        }
        return admins;
    }

    private String findUserEmail(Connection connection, int userId) throws SQLException {
        String sql = "SELECT email FROM users WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("email") : null;
            }
        }
    }

    private void sendEmailSafely(String toEmail, String subject, String html) {
        if (toEmail == null || toEmail.isBlank()) {
            return;
        }
        try {
            EmailUtil.sendHtml(toEmail, subject, html);
        } catch (Exception e) {
            System.err.println("sendEmailSafely error: " + e.getMessage());
        }
    }

    private String buildSimpleHtml(String title, String message, String targetUrl) {
        String safeTitle = escapeHtml(title);
        String safeMessage = escapeHtml(message);
        String safeUrl = escapeHtml(targetUrl == null ? "" : targetUrl);
        return """
                <div style='font-family:Arial,sans-serif;line-height:1.6;color:#1f2937'>
                    <h2 style='color:#d63384;margin:0 0 12px'>%s</h2>
                    <p style='margin:0 0 14px'>%s</p>
                    <p style='font-size:13px;color:#6b7280'>Vui lòng đăng nhập website MyCosmetic để xem chi tiết: %s</p>
                </div>
                """.formatted(safeTitle, safeMessage, safeUrl);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private record AdminContact(int userId, String email) {
    }
}

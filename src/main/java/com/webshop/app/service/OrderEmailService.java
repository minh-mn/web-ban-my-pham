package com.webshop.app.service;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.dao.OrderItemDAO;
import com.webshop.app.model.Order;
import com.webshop.app.model.OrderItem;
import com.webshop.app.utils.DBConnection;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class OrderEmailService {

    private static final Locale VI_LOCALE = new Locale("vi", "VN");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();

    public boolean sendOrderSuccessEmail(int orderId, String email) throws Exception {
        return sendOrderInvoiceEmail(orderId, email);
    }

    public boolean sendOrderSuccessEmail(String email, int orderId) throws Exception {
        return sendOrderInvoiceEmail(orderId, email);
    }

    public boolean sendOrderSuccessEmail(int orderId) throws Exception {
        return sendOrderInvoiceEmail(orderId, null);
    }

    public boolean sendOrderConfirmationEmail(int orderId, String email) throws Exception {
        return sendOrderInvoiceEmail(orderId, email);
    }

    public boolean sendOrderConfirmationEmail(String email, int orderId) throws Exception {
        return sendOrderInvoiceEmail(orderId, email);
    }

    public boolean sendOrderConfirmationEmail(int orderId) throws Exception {
        return sendOrderInvoiceEmail(orderId, null);
    }

    public boolean sendOrderInvoiceEmail(int orderId, String email) throws Exception {
        if (orderId <= 0) {
            throw new IllegalArgumentException("orderId không hợp lệ.");
        }

        if (isOrderEmailAlreadySent(orderId)) {
            return true;
        }

        Order order = orderDAO.findById(orderId);

        if (order == null) {
            markOrderEmailFailed(orderId, null, "Không tìm thấy đơn hàng.");
            throw new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId);
        }

        String toEmail = firstNonBlank(email, findUserEmailByOrder(order));

        if (isBlank(toEmail)) {
            markOrderEmailFailed(orderId, null, "Không tìm thấy email người nhận.");
            throw new IllegalArgumentException("Không tìm thấy email người nhận cho đơn hàng #" + orderId);
        }

        List<OrderItem> items = orderItemDAO.findByOrderId(orderId);

        String subject = "MyCosmetic - Xác nhận đơn hàng #" + order.getId();
        String html = buildEmailHtml(order, items);

        try {
            markOrderEmailAttempt(orderId, toEmail);
            sendHtmlMail(toEmail, subject, html);
            markOrderEmailSent(orderId, toEmail);
            return true;
        } catch (Exception e) {
            markOrderEmailFailed(orderId, toEmail, e.getMessage());
            throw e;
        }
    }

    private String buildEmailHtml(Order order, List<OrderItem> items) {
        BigDecimal subtotal = calculateSubtotal(items);
        BigDecimal discount = safeMoney(order.getCouponDiscount());
        BigDecimal shippingFee = safeMoney(order.getShippingFee());
        BigDecimal total = safeMoney(order.getTotal());

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            total = subtotal.subtract(discount).add(shippingFee);
        }

        String orderUrl = buildOrderDetailUrl(order.getId());

        StringBuilder html = new StringBuilder();

        html.append("<!doctype html><html lang='vi'><head>");
        html.append("<meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'>");
        html.append("<title>Xác nhận đơn hàng</title>");
        html.append("</head>");
        html.append("<body style='margin:0;padding:0;background:#fff7fb;font-family:Arial,Helvetica,sans-serif;color:#1f2a44;'>");
        html.append("<div style='width:100%;background:#fff7fb;padding:28px 12px;'>");
        html.append("<div style='max-width:760px;margin:0 auto;background:#fff;border-radius:24px;overflow:hidden;border:1px solid #f2bfd7;box-shadow:0 16px 38px rgba(214,51,132,.12);'>");

        html.append("<div style='background:linear-gradient(135deg,#d6004f,#ff6fa9);padding:30px 24px;text-align:center;color:#fff;'>");
        html.append("<div style='font-size:42px;line-height:1;margin-bottom:10px;'>🛍️</div>");
        html.append("<h1 style='margin:0;font-size:28px;line-height:1.3;font-weight:900;'>Xác nhận đơn hàng</h1>");
        html.append("<p style='margin:10px 0 0;font-size:15px;line-height:1.6;'>Cảm ơn bạn đã đặt hàng tại <strong>MyCosmetic</strong>.</p>");
        html.append("</div>");

        html.append("<div style='padding:28px 24px;'>");

        html.append("<h2 style='margin:0 0 14px;color:#1f2a44;font-size:21px;font-weight:900;'>Thông tin đơn hàng</h2>");
        html.append("<table role='presentation' style='width:100%;border-collapse:collapse;margin-bottom:24px;'>");
        addInfoRow(html, "Mã đơn hàng", "#" + order.getId(), true);
        addInfoRow(html, "Ngày đặt hàng", formatDate(order.getCreatedAt()), false);
        addInfoRow(html, "Trạng thái đơn hàng", formatOrderStatus(order.getStatus()), false);
        addInfoRow(html, "Thanh toán", formatPaymentMethod(order.getPaymentMethod()) + " - " + formatPaymentStatus(order.getPaymentStatus()), false);
        addInfoRow(html, "Giao hàng", formatShippingMethod(order.getShippingMethod()), false);
        addInfoRow(html, "Đơn vị vận chuyển", formatShippingProvider(order.getShippingProvider()), false);
        addInfoRow(html, "Mã vận chuyển", defaultIfBlank(order.getShippingCode(), generateShippingCode(order.getId())), true);
        html.append("</table>");

        html.append("<h2 style='margin:0 0 14px;color:#1f2a44;font-size:21px;font-weight:900;'>Thông tin người nhận</h2>");
        html.append("<div style='background:#fff8fb;border:1px solid #f2bfd7;border-radius:18px;padding:16px 18px;margin-bottom:24px;'>");
        html.append("<div style='font-weight:900;font-size:17px;color:#1f2a44;margin-bottom:8px;'>")
                .append(escape(defaultIfBlank(order.getFullName(), "Khách hàng")))
                .append("</div>");
        html.append("<div style='font-size:14px;line-height:1.7;color:#475569;'><strong>Số điện thoại:</strong> ")
                .append(escape(defaultIfBlank(order.getPhone(), "Đang cập nhật")))
                .append("</div>");
        html.append("<div style='font-size:14px;line-height:1.7;color:#475569;'><strong>Địa chỉ:</strong> ")
                .append(escape(defaultIfBlank(order.getAddress(), "Đang cập nhật")))
                .append("</div>");
        html.append("</div>");

        html.append("<h2 style='margin:0 0 14px;color:#1f2a44;font-size:21px;font-weight:900;'>Sản phẩm đã đặt</h2>");
        html.append("<table role='presentation' style='width:100%;border-collapse:collapse;border:1px solid #f2dce7;border-radius:16px;overflow:hidden;margin-bottom:24px;'>");
        html.append("<thead><tr style='background:#fff4f9;'>");
        html.append("<th style='padding:12px 10px;text-align:left;color:#1f2a44;font-size:13px;'>Sản phẩm</th>");
        html.append("<th style='padding:12px 10px;text-align:center;color:#1f2a44;font-size:13px;'>SL</th>");
        html.append("<th style='padding:12px 10px;text-align:right;color:#1f2a44;font-size:13px;'>Đơn giá</th>");
        html.append("<th style='padding:12px 10px;text-align:right;color:#1f2a44;font-size:13px;'>Thành tiền</th>");
        html.append("</tr></thead><tbody>");

        if (items == null || items.isEmpty()) {
            html.append("<tr><td colspan='4' style='padding:18px 10px;text-align:center;color:#64748b;border-bottom:1px solid #f2dce7;'>Danh sách sản phẩm đang được cập nhật.</td></tr>");
        } else {
            for (OrderItem item : items) {
                appendItemRow(html, item);
            }
        }

        html.append("</tbody></table>");

        html.append("<h2 style='margin:0 0 14px;color:#1f2a44;font-size:21px;font-weight:900;'>Tổng kết thanh toán</h2>");
        html.append("<table role='presentation' style='width:100%;border-collapse:collapse;'>");
        addSummaryRow(html, "Tạm tính", formatMoney(subtotal), false);
        addSummaryRow(html, "Giảm giá", "-" + formatMoney(discount), false);
        addSummaryRow(html, "Phí vận chuyển", formatMoney(shippingFee), false);
        addSummaryRow(html, "Tổng thanh toán", formatMoney(total), true);
        html.append("</table>");

        if (!isBlank(orderUrl)) {
            html.append("<div style='text-align:center;margin-top:28px;'>");
            html.append("<a href='").append(escape(orderUrl)).append("' ");
            html.append("style='display:inline-block;padding:12px 22px;border-radius:999px;background:#d6004f;color:#ffffff;text-decoration:none;font-weight:900;'>");
            html.append("Xem chi tiết đơn hàng</a>");
            html.append("</div>");
        }

        html.append("<div style='margin-top:28px;padding:14px 16px;border-radius:16px;background:#ecfdf5;border:1px solid #bbf7d0;color:#166534;font-size:14px;line-height:1.6;'>");
        html.append("Email này là thông báo xác nhận đơn hàng. Vui lòng giữ lại để đối chiếu khi cần hỗ trợ.");
        html.append("</div>");

        html.append("<p style='margin:22px 0 0;color:#94a3b8;font-size:12px;text-align:center;'>© MyCosmetic</p>");

        html.append("</div></div></div></body></html>");

        return html.toString();
    }

    private void addInfoRow(StringBuilder html, String label, String value, boolean highlight) {
        html.append("<tr>");
        html.append("<td style='padding:8px 0;color:#64748b;width:42%;'>").append(escape(label)).append("</td>");
        html.append("<td style='padding:8px 0;text-align:right;font-weight:")
                .append(highlight ? "900" : "700")
                .append(";color:")
                .append(highlight ? "#d6004f" : "#1f2a44")
                .append(";'>")
                .append(escape(defaultIfBlank(value, "Đang cập nhật"))).append("</td>");
        html.append("</tr>");
    }

    private void appendItemRow(StringBuilder html, OrderItem item) {
        String productName = defaultIfBlank(item.getProductName(), "Sản phẩm");
        String variant = buildVariantText(item);
        int quantity = item.getQuantity() > 0 ? item.getQuantity() : 1;
        BigDecimal price = safeMoney(item.getPrice());
        BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(quantity));

        html.append("<tr>");
        html.append("<td style='padding:12px 10px;border-bottom:1px solid #f2dce7;color:#1f2a44;font-weight:700;'>")
                .append(escape(productName));

        if (!isBlank(variant)) {
            html.append("<div style='font-size:12px;color:#64748b;font-weight:500;margin-top:4px;'>")
                    .append(escape(variant))
                    .append("</div>");
        }

        html.append("</td>");
        html.append("<td style='padding:12px 10px;border-bottom:1px solid #f2dce7;text-align:center;color:#334155;font-weight:700;'>")
                .append(quantity).append("</td>");
        html.append("<td style='padding:12px 10px;border-bottom:1px solid #f2dce7;text-align:right;color:#334155;font-weight:700;'>")
                .append(escape(formatMoney(price))).append("</td>");
        html.append("<td style='padding:12px 10px;border-bottom:1px solid #f2dce7;text-align:right;color:#d6004f;font-weight:900;'>")
                .append(escape(formatMoney(lineTotal))).append("</td>");
        html.append("</tr>");
    }

    private void addSummaryRow(StringBuilder html, String label, String value, boolean total) {
        if (total) {
            html.append("<tr>");
            html.append("<td style='padding:16px 0 0;border-top:1px solid #f2dce7;color:#1f2a44;font-size:18px;font-weight:900;'>")
                    .append(escape(label)).append("</td>");
            html.append("<td style='padding:16px 0 0;border-top:1px solid #f2dce7;text-align:right;color:#d6004f;font-size:22px;font-weight:900;'>")
                    .append(escape(value)).append("</td>");
            html.append("</tr>");
            return;
        }

        html.append("<tr>");
        html.append("<td style='padding:8px 0;color:#64748b;'>").append(escape(label)).append("</td>");
        html.append("<td style='padding:8px 0;text-align:right;font-weight:700;'>").append(escape(value)).append("</td>");
        html.append("</tr>");
    }

    private void sendHtmlMail(String toEmail, String subject, String html) throws Exception {
        Properties config = loadMailProperties();

        String host = firstNonBlank(
                System.getProperty("mycosmetic.mail.smtp.host"),
                config.getProperty("mycosmetic.mail.smtp.host"),
                config.getProperty("mail.smtp.host"),
                System.getenv("MYCOSMETIC_MAIL_SMTP_HOST"),
                System.getenv("MAIL_SMTP_HOST"),
                "smtp.gmail.com"
        );

        String port = firstNonBlank(
                System.getProperty("mycosmetic.mail.smtp.port"),
                config.getProperty("mycosmetic.mail.smtp.port"),
                config.getProperty("mail.smtp.port"),
                System.getenv("MYCOSMETIC_MAIL_SMTP_PORT"),
                System.getenv("MAIL_SMTP_PORT"),
                "587"
        );

        String username = firstNonBlank(
                System.getProperty("mycosmetic.mail.username"),
                config.getProperty("mycosmetic.mail.username"),
                config.getProperty("mail.username"),
                System.getenv("MYCOSMETIC_MAIL_USERNAME"),
                System.getenv("MAIL_USERNAME")
        );

        String password = firstNonBlank(
                System.getProperty("mycosmetic.mail.password"),
                config.getProperty("mycosmetic.mail.password"),
                config.getProperty("mail.password"),
                System.getenv("MYCOSMETIC_MAIL_PASSWORD"),
                System.getenv("MAIL_PASSWORD")
        );

        String from = firstNonBlank(
                System.getProperty("mycosmetic.mail.from"),
                config.getProperty("mycosmetic.mail.from"),
                config.getProperty("mail.from"),
                System.getenv("MYCOSMETIC_MAIL_FROM"),
                System.getenv("MAIL_FROM"),
                username
        );

        String fromName = firstNonBlank(
                System.getProperty("mycosmetic.mail.fromName"),
                config.getProperty("mycosmetic.mail.fromName"),
                config.getProperty("mail.fromName"),
                System.getenv("MYCOSMETIC_MAIL_FROM_NAME"),
                System.getenv("MAIL_FROM_NAME"),
                "MyCosmetic"
        );

        if (isBlank(username) || isBlank(password) || isBlank(from)) {
            throw new IllegalStateException("Chưa cấu hình SMTP. Cần cấu hình mycosmetic.mail.username và mycosmetic.mail.password.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", firstNonBlank(config.getProperty("mail.smtp.auth"), "true"));
        props.put("mail.smtp.starttls.enable", firstNonBlank(config.getProperty("mail.smtp.starttls.enable"), "true"));
        props.put("mail.smtp.starttls.required", firstNonBlank(config.getProperty("mail.smtp.starttls.required"), "true"));
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.connectiontimeout", firstNonBlank(config.getProperty("mail.smtp.connectiontimeout"), "12000"));
        props.put("mail.smtp.timeout", firstNonBlank(config.getProperty("mail.smtp.timeout"), "12000"));
        props.put("mail.smtp.writetimeout", firstNonBlank(config.getProperty("mail.smtp.writetimeout"), "12000"));

        Session session = Session.getInstance(
                props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                }
        );
        session.setDebug(Boolean.parseBoolean(firstNonBlank(config.getProperty("mail.debug"), "false")));

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from, fromName, StandardCharsets.UTF_8.name()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        message.setContent(html, "text/html; charset=UTF-8");

        Transport.send(message);
    }

    private Properties loadMailProperties() {
        Properties properties = new Properties();

        try (var inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("mail.properties")) {

            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties;
    }

    private boolean isOrderEmailAlreadySent(int orderId) {
        String sql = "SELECT COALESCE(order_email_sent, 0) FROM store_order WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) == 1;
            }

        } catch (SQLException e) {
            return false;
        }
    }

    private void markOrderEmailAttempt(int orderId, String toEmail) {
        String sql = "UPDATE store_order "
                + "SET order_email_attempt_count = COALESCE(order_email_attempt_count, 0) + 1, "
                + "last_order_email_attempt_at = NOW(), "
                + "order_email_to = ? "
                + "WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, trimToNull(toEmail));
            statement.setInt(2, orderId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void markOrderEmailSent(int orderId, String toEmail) {
        String sql = "UPDATE store_order "
                + "SET order_email_sent = 1, "
                + "order_email_sent_at = NOW(), "
                + "order_email_to = ?, "
                + "order_email_error = NULL "
                + "WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, trimToNull(toEmail));
            statement.setInt(2, orderId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void markOrderEmailFailed(int orderId, String toEmail, String error) {
        String sql = "UPDATE store_order "
                + "SET order_email_sent = 0, "
                + "order_email_to = COALESCE(?, order_email_to), "
                + "order_email_error = ?, "
                + "order_email_attempt_count = COALESCE(order_email_attempt_count, 0) + 1, "
                + "last_order_email_attempt_at = NOW() "
                + "WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, trimToNull(toEmail));
            statement.setString(2, limit(defaultIfBlank(error, "Gửi email thất bại."), 1000));
            statement.setInt(3, orderId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String findUserEmailByOrder(Order order) {
        if (order == null || order.getUserId() <= 0) {
            return "";
        }

        String sql = "SELECT email FROM users WHERE id = ? LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, order.getUserId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return trim(resultSet.getString("email"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "";
    }

    private BigDecimal calculateSubtotal(List<OrderItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;

        if (items == null || items.isEmpty()) {
            return subtotal;
        }

        for (OrderItem item : items) {
            if (item == null) {
                continue;
            }

            int quantity = item.getQuantity() > 0 ? item.getQuantity() : 1;
            subtotal = subtotal.add(safeMoney(item.getPrice()).multiply(BigDecimal.valueOf(quantity)));
        }

        return vnd0(subtotal);
    }

    private String buildVariantText(OrderItem item) {
        if (item == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        appendPart(builder, item.getVariantName());
        appendPart(builder, item.getVariantSize());
        appendPart(builder, item.getVariantType());

        return builder.toString();
    }

    private void appendPart(StringBuilder builder, String value) {
        if (isBlank(value)) {
            return;
        }

        if (builder.length() > 0) {
            builder.append(" - ");
        }

        builder.append(value.trim());
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Đang cập nhật";
        }

        return dateTime.format(DATE_TIME_FORMATTER);
    }

    private String formatPaymentMethod(String paymentMethod) {
        String value = trim(paymentMethod).toUpperCase(Locale.ROOT);

        return switch (value) {
            case "COD" -> "Thanh toán khi nhận hàng (COD)";
            case "VNPAY" -> "Thanh toán qua VNPAY";
            case "BANKING" -> "Chuyển khoản ngân hàng";
            default -> isBlank(paymentMethod) ? "Không xác định" : paymentMethod;
        };
    }

    private String formatPaymentStatus(String paymentStatus) {
        String value = trim(paymentStatus).toUpperCase(Locale.ROOT);

        return switch (value) {
            case "PAID" -> "Đã thanh toán";
            case "PENDING" -> "Chờ thanh toán";
            case "FAILED" -> "Thanh toán thất bại";
            case "CANCELED", "CANCELLED" -> "Đã hủy thanh toán";
            case "REFUNDED" -> "Đã hoàn tiền";
            default -> isBlank(paymentStatus) ? "Đang cập nhật" : paymentStatus;
        };
    }

    private String formatOrderStatus(String status) {
        String value = trim(status).toLowerCase(Locale.ROOT);

        return switch (value) {
            case "processing" -> "Đang xử lý";
            case "confirmed" -> "Đã xác nhận";
            case "shipping" -> "Đang giao hàng";
            case "completed" -> "Hoàn thành";
            case "cancelled", "canceled" -> "Đã hủy";
            default -> isBlank(status) ? "Đang xử lý" : status;
        };
    }

    private String formatShippingMethod(String shippingMethod) {
        String value = trim(shippingMethod).toUpperCase(Locale.ROOT);

        return switch (value) {
            case "ECONOMY" -> "Giao hàng tiết kiệm";
            case "FAST" -> "Giao hàng nhanh";
            case "EXPRESS" -> "Hỏa tốc";
            case "STANDARD" -> "Giao hàng tiêu chuẩn";
            default -> isBlank(shippingMethod) ? "Giao hàng tiêu chuẩn" : shippingMethod;
        };
    }

    private String formatShippingProvider(String shippingProvider) {
        String value = trim(shippingProvider).toUpperCase(Locale.ROOT);

        return switch (value) {
            case "INTERNAL" -> "MyCosmetic Delivery";
            case "GHTK" -> "Giao Hàng Tiết Kiệm";
            case "GHN" -> "Giao Hàng Nhanh";
            case "VIETTEL_POST" -> "Viettel Post";
            case "OTHER" -> "Đơn vị vận chuyển khác";
            default -> isBlank(shippingProvider) ? "MyCosmetic Delivery" : shippingProvider;
        };
    }

    private String generateShippingCode(int orderId) {
        if (orderId <= 0) {
            return "Đang cập nhật";
        }

        return "MC-SHIP-" + String.format("%06d", orderId);
    }

    private String buildOrderDetailUrl(int orderId) {
        Properties config = loadMailProperties();

        String baseUrl = firstNonBlank(
                System.getProperty("mycosmetic.app.baseUrl"),
                config.getProperty("mycosmetic.app.baseUrl"),
                config.getProperty("app.baseUrl"),
                System.getenv("MYCOSMETIC_APP_BASE_URL"),
                System.getenv("APP_BASE_URL")
        );

        if (isBlank(baseUrl)) {
            return "";
        }

        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl + "/account/orders/detail?id=" + orderId;
    }

    private BigDecimal safeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return vnd0(value);
    }

    private BigDecimal vnd0(BigDecimal value) {
        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private String formatMoney(BigDecimal value) {
        NumberFormat numberFormat = NumberFormat.getInstance(VI_LOCALE);
        numberFormat.setGroupingUsed(true);

        return numberFormat.format(safeMoney(value)) + "đ";
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }

        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }

        return "";
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        String trimmed = trim(value);
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

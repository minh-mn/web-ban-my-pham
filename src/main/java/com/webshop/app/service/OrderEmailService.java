package com.webshop.app.service;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.dao.OrderItemDAO;
import com.webshop.app.model.Order;
import com.webshop.app.model.OrderItem;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Service gửi email xác nhận đơn hàng / hóa đơn cho khách hàng.
 *
 * Cách hoạt động:
 * 1. Lấy Order bằng OrderDAO.
 * 2. Lấy danh sách sản phẩm bằng OrderItemDAO.
 * 3. Dựng nội dung email HTML.
 * 4. Ưu tiên gọi EmailService có sẵn trong project.
 * 5. Nếu EmailService không phù hợp, fallback sang Jakarta Mail SMTP.
 */
public class OrderEmailService {

    private static final Locale VI_LOCALE = new Locale("vi", "VN");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();

    /*
     * Các hàm này khớp với logic reflection trong CheckoutServlet/VNPayReturnServlet.
     */
    public void sendOrderSuccessEmail(int orderId, String email) throws Exception {
        sendOrderInvoiceEmail(orderId, email);
    }

    public void sendOrderSuccessEmail(String email, int orderId) throws Exception {
        sendOrderInvoiceEmail(orderId, email);
    }

    public void sendOrderSuccessEmail(int orderId) throws Exception {
        sendOrderInvoiceEmail(orderId, "");
    }

    public void sendOrderConfirmationEmail(int orderId, String email) throws Exception {
        sendOrderInvoiceEmail(orderId, email);
    }

    public void sendOrderConfirmationEmail(String email, int orderId) throws Exception {
        sendOrderInvoiceEmail(orderId, email);
    }

    public void sendOrderConfirmationEmail(int orderId) throws Exception {
        sendOrderInvoiceEmail(orderId, "");
    }

    public void sendOrderInvoiceEmail(int orderId, String email) throws Exception {
        String toEmail = trim(email);

        if (orderId <= 0 || isBlank(toEmail)) {
            return;
        }

        Order order = orderDAO.findById(orderId);

        if (order == null) {
            return;
        }

        List<OrderItem> items = orderItemDAO.findByOrderId(orderId);

        String subject = "Xác nhận đơn hàng #" + order.getId() + " - MyCosmetic Shop";
        String html = buildEmailHtml(order, items);

        boolean sent = sendByExistingEmailService(toEmail, subject, html);

        if (!sent) {
            sendByJakartaMail(toEmail, subject, html);
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

        String orderDate = formatDate(order.getCreatedAt());
        String deliveryDate = order.getDeliveredAt() != null
                ? formatDate(order.getDeliveredAt())
                : estimateDeliveryDate(order.getCreatedAt(), order.getShippingMethod());

        String shippingProvider = defaultIfBlank(order.getShippingProvider(), "MyCosmetic Delivery");
        String shippingCode = defaultIfBlank(order.getShippingCode(), generateShippingCode(order.getId()));
        String paymentMethod = formatPaymentMethod(order.getPaymentMethod());
        String shippingMethod = formatShippingMethod(order.getShippingMethod());

        StringBuilder html = new StringBuilder();

        html.append("<!doctype html>");
        html.append("<html lang='vi'><head><meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width,initial-scale=1.0'>");
        html.append("<title>Xác nhận đơn hàng</title></head>");
        html.append("<body style='margin:0;padding:0;background:#fff7fb;font-family:Arial,Helvetica,sans-serif;color:#1f2a44;'>");
        html.append("<div style='width:100%;background:#fff7fb;padding:28px 12px;'>");
        html.append("<div style='max-width:760px;margin:0 auto;background:#fff;border-radius:24px;overflow:hidden;border:1px solid #f2bfd7;box-shadow:0 16px 38px rgba(214,51,132,.12);'>");

        html.append("<div style='background:linear-gradient(135deg,#d63384,#ff85bc);padding:30px 24px;text-align:center;color:#fff;'>");
        html.append("<div style='font-size:42px;line-height:1;margin-bottom:10px;'>🎉</div>");
        html.append("<h1 style='margin:0;font-size:28px;line-height:1.3;font-weight:900;'>Đặt hàng thành công</h1>");
        html.append("<p style='margin:10px 0 0;font-size:15px;line-height:1.6;'>Cảm ơn bạn đã mua hàng tại <strong>MyCosmetic Shop</strong>.</p>");
        html.append("</div>");

        html.append("<div style='padding:28px 24px;'>");

        html.append("<h2 style='margin:0 0 14px;color:#1f2a44;font-size:21px;font-weight:900;'>Thông tin đơn hàng</h2>");
        html.append("<table role='presentation' style='width:100%;border-collapse:collapse;margin-bottom:24px;'>");
        addInfoRow(html, "Mã đơn hàng", "#" + order.getId(), true);
        addInfoRow(html, "Ngày đặt hàng", orderDate, false);
        addInfoRow(html, "Ngày nhận hàng", deliveryDate, false);
        addInfoRow(html, "Phương thức thanh toán", paymentMethod, false);
        addInfoRow(html, "Phương thức giao hàng", shippingMethod, false);
        addInfoRow(html, "Đơn vị vận chuyển", shippingProvider, false);
        addInfoRow(html, "Mã vận chuyển", shippingCode, true);
        html.append("</table>");

        html.append("<h2 style='margin:0 0 14px;color:#1f2a44;font-size:21px;font-weight:900;'>Thông tin người nhận</h2>");
        html.append("<div style='background:#fff8fb;border:1px solid #f2bfd7;border-radius:18px;padding:16px 18px;margin-bottom:24px;'>");
        html.append("<div style='font-weight:900;font-size:17px;color:#1f2a44;margin-bottom:8px;'>").append(escape(order.getFullName())).append("</div>");
        html.append("<div style='font-size:14px;line-height:1.7;color:#475569;'><strong>Số điện thoại:</strong> ").append(escape(order.getPhone())).append("</div>");
        html.append("<div style='font-size:14px;line-height:1.7;color:#475569;'><strong>Địa chỉ:</strong> ").append(escape(order.getAddress())).append("</div>");
        html.append("</div>");

        html.append("<h2 style='margin:0 0 14px;color:#1f2a44;font-size:21px;font-weight:900;'>Mặt hàng trong đơn</h2>");
        html.append("<table role='presentation' style='width:100%;border-collapse:collapse;border:1px solid #f2dce7;border-radius:16px;overflow:hidden;margin-bottom:24px;'>");
        html.append("<thead><tr style='background:#fff4f9;'>");
        html.append("<th style='padding:12px 10px;text-align:left;color:#1f2a44;font-size:13px;'>Sản phẩm</th>");
        html.append("<th style='padding:12px 10px;text-align:center;color:#1f2a44;font-size:13px;'>SL</th>");
        html.append("<th style='padding:12px 10px;text-align:right;color:#1f2a44;font-size:13px;'>Đơn giá</th>");
        html.append("<th style='padding:12px 10px;text-align:right;color:#1f2a44;font-size:13px;'>Thành tiền</th>");
        html.append("</tr></thead><tbody>");

        if (items == null || items.isEmpty()) {
            html.append("<tr><td colspan='4' style='padding:18px 10px;text-align:center;color:#64748b;border-bottom:1px solid #f2dce7;'>Danh sách mặt hàng đang được cập nhật.</td></tr>");
        } else {
            for (OrderItem item : items) {
                appendItemRow(html, item);
            }
        }

        html.append("</tbody></table>");

        html.append("<h2 style='margin:0 0 14px;color:#1f2a44;font-size:21px;font-weight:900;'>Tổng kết thanh toán</h2>");
        html.append("<table role='presentation' style='width:100%;border-collapse:collapse;'>");
        addSummaryRow(html, "Tạm tính", formatMoney(subtotal), false);
        addSummaryRow(html, "Giảm giá", formatMoney(discount), false);
        addSummaryRow(html, "Phí vận chuyển", formatMoney(shippingFee), false);
        addSummaryRow(html, "Tổng thanh toán", formatMoney(total), true);
        html.append("</table>");

        html.append("<div style='margin-top:28px;padding:14px 16px;border-radius:16px;background:#ecfdf5;border:1px solid #bbf7d0;color:#166534;font-size:14px;line-height:1.6;'>");
        html.append("Email này là xác nhận thông tin đơn hàng. Vui lòng giữ lại để đối chiếu khi cần hỗ trợ.");
        html.append("</div>");

        html.append("</div></div></div></body></html>");

        return html.toString();
    }

    private void addInfoRow(StringBuilder html, String label, String value, boolean highlight) {
        html.append("<tr>");
        html.append("<td style='padding:8px 0;color:#64748b;width:42%;'>").append(escape(label)).append("</td>");
        html.append("<td style='padding:8px 0;text-align:right;font-weight:").append(highlight ? "900" : "700")
                .append(";color:").append(highlight ? "#d63384" : "#1f2a44").append(";'>")
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
                .append(escape(productName))
                .append("<div style='font-size:12px;color:#64748b;font-weight:500;margin-top:4px;'>")
                .append(escape(defaultIfBlank(variant, "Mặc định")))
                .append("</div></td>");
        html.append("<td style='padding:12px 10px;border-bottom:1px solid #f2dce7;text-align:center;color:#334155;font-weight:700;'>")
                .append(quantity).append("</td>");
        html.append("<td style='padding:12px 10px;border-bottom:1px solid #f2dce7;text-align:right;color:#334155;font-weight:700;'>")
                .append(escape(formatMoney(price))).append("</td>");
        html.append("<td style='padding:12px 10px;border-bottom:1px solid #f2dce7;text-align:right;color:#d63384;font-weight:900;'>")
                .append(escape(formatMoney(lineTotal))).append("</td>");
        html.append("</tr>");
    }

    private void addSummaryRow(StringBuilder html, String label, String value, boolean total) {
        if (total) {
            html.append("<tr>");
            html.append("<td style='padding:16px 0 0;border-top:1px solid #f2dce7;color:#1f2a44;font-size:18px;font-weight:900;'>")
                    .append(escape(label)).append("</td>");
            html.append("<td style='padding:16px 0 0;border-top:1px solid #f2dce7;text-align:right;color:#d63384;font-size:22px;font-weight:900;'>")
                    .append(escape(value)).append("</td>");
            html.append("</tr>");
            return;
        }

        html.append("<tr>");
        html.append("<td style='padding:8px 0;color:#64748b;'>").append(escape(label)).append("</td>");
        html.append("<td style='padding:8px 0;text-align:right;font-weight:700;'>").append(escape(value)).append("</td>");
        html.append("</tr>");
    }

    private boolean sendByExistingEmailService(String toEmail, String subject, String html) {
        try {
            Class<?> emailServiceClass = Class.forName("com.webshop.app.service.EmailService");
            Object emailService = createServiceInstance(emailServiceClass);

            String[] methodNames = {"sendHtmlEmail", "sendHTML", "sendEmail", "sendMail", "send"};

            for (String methodName : methodNames) {
                if (tryInvokeEmailMethod(emailServiceClass, emailService, methodName, toEmail, subject, html)) {
                    return true;
                }
            }

            return false;
        } catch (ClassNotFoundException ignored) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Object createServiceInstance(Class<?> serviceClass) throws Exception {
        Constructor<?> constructor = serviceClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private boolean tryInvokeEmailMethod(Class<?> serviceClass,
                                         Object service,
                                         String methodName,
                                         String toEmail,
                                         String subject,
                                         String html) {

        for (Method method : serviceClass.getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }

            Class<?>[] parameterTypes = method.getParameterTypes();

            try {
                if (parameterTypes.length == 3
                        && parameterTypes[0] == String.class
                        && parameterTypes[1] == String.class
                        && parameterTypes[2] == String.class) {
                    Object target = Modifier.isStatic(method.getModifiers()) ? null : service;
                    method.invoke(target, toEmail, subject, html);
                    return true;
                }

                if (parameterTypes.length == 4
                        && parameterTypes[0] == String.class
                        && parameterTypes[1] == String.class
                        && parameterTypes[2] == String.class
                        && (parameterTypes[3] == boolean.class || parameterTypes[3] == Boolean.class)) {
                    Object target = Modifier.isStatic(method.getModifiers()) ? null : service;
                    method.invoke(target, toEmail, subject, html, true);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }
        }

        return false;
    }

    private void sendByJakartaMail(String toEmail, String subject, String html) throws Exception {
        String host = firstNonBlank(
                getConfig("mycosmetic.mail.smtp.host"),
                getConfig("mail.smtp.host"),
                getEnv("MYCOSMETIC_MAIL_SMTP_HOST"),
                getEnv("MAIL_SMTP_HOST"),
                "smtp.gmail.com"
        );

        String port = firstNonBlank(
                getConfig("mycosmetic.mail.smtp.port"),
                getConfig("mail.smtp.port"),
                getEnv("MYCOSMETIC_MAIL_SMTP_PORT"),
                getEnv("MAIL_SMTP_PORT"),
                "587"
        );

        String username = firstNonBlank(
                getConfig("mycosmetic.mail.username"),
                getConfig("mail.username"),
                getEnv("MYCOSMETIC_MAIL_USERNAME"),
                getEnv("MAIL_USERNAME")
        );

        String password = firstNonBlank(
                getConfig("mycosmetic.mail.password"),
                getConfig("mail.password"),
                getEnv("MYCOSMETIC_MAIL_PASSWORD"),
                getEnv("MAIL_PASSWORD")
        );

        String from = firstNonBlank(
                getConfig("mycosmetic.mail.from"),
                getConfig("mail.from"),
                getEnv("MYCOSMETIC_MAIL_FROM"),
                getEnv("MAIL_FROM"),
                username
        );

        String fromName = firstNonBlank(
                getConfig("mycosmetic.mail.fromName"),
                getConfig("mail.fromName"),
                getEnv("MYCOSMETIC_MAIL_FROM_NAME"),
                getEnv("MAIL_FROM_NAME"),
                "MyCosmetic Shop"
        );

        if (isBlank(username) || isBlank(password) || isBlank(from)) {
            throw new IllegalStateException(
                    "Chưa cấu hình email SMTP. Hãy cấu hình MYCOSMETIC_MAIL_USERNAME và MYCOSMETIC_MAIL_PASSWORD."
            );
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.connectiontimeout", "12000");
        props.put("mail.smtp.timeout", "12000");
        props.put("mail.smtp.writetimeout", "12000");

        Session session = Session.getInstance(
                props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                }
        );

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from, fromName, StandardCharsets.UTF_8.name()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        message.setContent(html, "text/html; charset=UTF-8");

        Transport.send(message);
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

    private String estimateDeliveryDate(LocalDateTime createdAt, String shippingMethod) {
        LocalDateTime createdTime = createdAt != null ? createdAt : LocalDateTime.now();
        String method = trim(shippingMethod).toUpperCase(Locale.ROOT);

        int daysToAdd;

        switch (method) {
            case "EXPRESS":
                daysToAdd = 1;
                break;
            case "FAST":
                daysToAdd = 2;
                break;
            default:
                daysToAdd = 4;
                break;
        }

        return createdTime.plusDays(daysToAdd).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Đang cập nhật";
        }

        return dateTime.format(DATE_TIME_FORMATTER);
    }

    private String formatPaymentMethod(String paymentMethod) {
        String value = trim(paymentMethod).toUpperCase(Locale.ROOT);

        switch (value) {
            case "COD":
                return "Thanh toán khi nhận hàng (COD)";
            case "VNPAY":
                return "Thanh toán qua VNPAY";
            case "BANKING":
                return "Chuyển khoản ngân hàng";
            default:
                return isBlank(paymentMethod) ? "Không xác định" : paymentMethod;
        }
    }

    private String formatShippingMethod(String shippingMethod) {
        String value = trim(shippingMethod).toUpperCase(Locale.ROOT);

        switch (value) {
            case "ECONOMY":
                return "Giao hàng tiết kiệm";
            case "FAST":
                return "Giao hàng nhanh";
            case "EXPRESS":
                return "Hỏa tốc";
            case "STANDARD":
                return "Giao hàng tiêu chuẩn";
            default:
                return isBlank(shippingMethod) ? "Giao hàng tiêu chuẩn" : shippingMethod;
        }
    }

    private String generateShippingCode(int orderId) {
        if (orderId <= 0) {
            return "Đang cập nhật";
        }

        return "MC-SHIP-" + String.format("%06d", orderId);
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
        NumberFormat nf = NumberFormat.getInstance(VI_LOCALE);
        nf.setGroupingUsed(true);

        return nf.format(safeMoney(value)) + " ₫";
    }

    private String getConfig(String key) {
        String value = System.getProperty(key);
        return value == null ? "" : value.trim();
    }

    private String getEnv(String key) {
        String value = System.getenv(key);
        return value == null ? "" : value.trim();
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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

package com.webshop.app.controller.OrderController;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.dao.OrderItemDAO;
import com.webshop.app.model.Order;
import com.webshop.app.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/checkout/success")
public class CheckoutSuccessServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Locale VI_LOCALE = new Locale("vi", "VN");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }

        boolean success = "true".equalsIgnoreCase(trim(req.getParameter("success")));
        String messageKey = trim(req.getParameter("message"));
        Integer orderId = parseInteger(trim(req.getParameter("orderId")));

        if (orderId == null || orderId <= 0) {
            forwardResultPage(
                    req,
                    resp,
                    false,
                    "Không tìm thấy mã đơn hàng. Vui lòng kiểm tra lại.",
                    null,
                    currentUser,
                    session
            );
            return;
        }

        Order order;
        try {
            order = orderDAO.findById(orderId);
        } catch (Exception e) {
            throw new ServletException("CheckoutSuccessServlet.findById error: orderId=" + orderId, e);
        }

        if (order == null) {
            forwardResultPage(
                    req,
                    resp,
                    false,
                    "Không tìm thấy đơn hàng. Vui lòng kiểm tra lại.",
                    null,
                    currentUser,
                    session
            );
            return;
        }

        if (!currentUser.isAdmin() && order.getUserId() != currentUser.getId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xem đơn hàng này.");
            return;
        }

        if (!success) {
            forwardResultPage(
                    req,
                    resp,
                    false,
                    buildFailMessage(messageKey),
                    order,
                    currentUser,
                    session
            );
            return;
        }

        forwardResultPage(req, resp, true, null, order, currentUser, session);
    }

    private void forwardResultPage(HttpServletRequest req,
                                   HttpServletResponse resp,
                                   boolean success,
                                   String message,
                                   Order order,
                                   User currentUser,
                                   HttpSession session)
            throws ServletException, IOException {

        prepareViewData(req, success, message, order, currentUser, session);

        req.setAttribute("pageTitle", "MyCosmetic | Kết quả thanh toán");
        req.setAttribute("pageCss", "/checkout-success.css");
        req.setAttribute("pageContent", "/jsp/checkout/checkout_success.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    private void prepareViewData(HttpServletRequest req,
                                 boolean success,
                                 String message,
                                 Order order,
                                 User currentUser,
                                 HttpSession session) {

        req.setAttribute("success", success);
        req.setAttribute("message", message);
        req.setAttribute("order", order);

        if (order == null) {
            req.setAttribute("orderItems", List.of());
            req.setAttribute("orderItemsCount", 0);
            req.setAttribute("emailSent", false);
            req.setAttribute("emailText", safeString(getUserEmail(currentUser), "Đang cập nhật"));
            return;
        }

        int orderId = extractOrderId(order);

        String receiverName = firstNonBlank(
                firstString(order, "getFullName", "getReceiverName", "getCustomerName", "getName"),
                safeString(currentUser.getFullName(), currentUser.getUsername()),
                "Khách hàng"
        );

        String receiverPhone = firstNonBlank(
                firstString(order, "getPhone", "getReceiverPhone", "getCustomerPhone", "getPhoneNumber"),
                safeString(currentUser.getPhone(), ""),
                "Đang cập nhật"
        );

        String receiverAddress = firstNonBlank(
                firstString(order, "getAddress", "getShippingAddress", "getReceiverAddress"),
                "Đang cập nhật"
        );

        String paymentMethod = safeString(firstString(order, "getPaymentMethod"), "UNKNOWN");
        String paymentMethodText = formatPaymentMethod(paymentMethod);

        String orderStatus = safeString(firstString(order, "getStatus", "getOrderStatus"), "processing");
        String orderStatusLabel = formatOrderStatus(orderStatus);

        String shippingMethod = safeString(firstString(order, "getShippingMethod", "getDeliveryMethod"), "ECONOMY");
        String shippingMethodLabel = formatShippingMethod(shippingMethod);

        Object createdAtRaw = firstValue(order, "getCreatedAt", "getCreatedDate", "getOrderDate", "getCreatedTime");
        Object deliveredAtRaw = firstValue(order, "getDeliveredAt", "getReceivedAt", "getCompletedAt");

        LocalDateTime createdAt = toLocalDateTime(createdAtRaw);
        LocalDateTime deliveredAt = toLocalDateTime(deliveredAtRaw);

        String orderDateText = formatDateTime(createdAtRaw);
        String receivedDateText = formatReceivedDate(createdAt, deliveredAt, shippingMethod);

        List<OrderLineView> orderItems = loadOrderItems(orderId);

        BigDecimal total = toBigDecimal(firstValue(order, "getTotal"));
        BigDecimal discount = toBigDecimal(firstValue(order, "getCouponDiscount", "getDiscountAmount", "getDiscount"));
        BigDecimal shippingFee = toBigDecimal(firstValue(order, "getShippingFee", "getShipFee", "getDeliveryFee"));

        if (total == null) {
            total = BigDecimal.ZERO;
        }

        if (discount == null) {
            discount = BigDecimal.ZERO;
        }

        if (shippingFee == null) {
            shippingFee = BigDecimal.ZERO;
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderLineView item : orderItems) {
            subtotal = subtotal.add(item.getLineTotal());
        }

        /*
         * Nếu order item chưa tải được, vẫn hiển thị tạm tính hợp lý thay vì dấu "-".
         * subtotal ≈ total + discount - shippingFee
         */
        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            subtotal = total.add(discount).subtract(shippingFee);
            if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
                subtotal = BigDecimal.ZERO;
            }
        }

        String emailText = safeString(getUserEmail(currentUser), "Đang cập nhật");
        boolean emailSent = getEmailSentFlag(session, orderId);

        req.setAttribute("orderId", orderId);
        req.setAttribute("orderCode", "#" + orderId);

        req.setAttribute("receiverNameText", receiverName);
        req.setAttribute("receiverPhoneText", receiverPhone);
        req.setAttribute("receiverAddressText", receiverAddress);

        /*
         * Giữ thêm các tên attribute cũ để JSP cũ hoặc fragment khác vẫn đọc được.
         */
        req.setAttribute("receiverName", receiverName);
        req.setAttribute("receiverPhone", receiverPhone);
        req.setAttribute("shippingAddress", receiverAddress);

        req.setAttribute("orderDateText", orderDateText);
        req.setAttribute("createdAtText", orderDateText);
        req.setAttribute("receivedDateText", receivedDateText);
        req.setAttribute("deliveredDateText", receivedDateText);

        req.setAttribute("paymentMethod", paymentMethod);
        req.setAttribute("paymentMethodText", paymentMethodText);
        req.setAttribute("paymentMethodLabel", paymentMethodText);

        req.setAttribute("orderStatus", orderStatus);
        req.setAttribute("orderStatusLabel", orderStatusLabel);

        req.setAttribute("shippingMethod", shippingMethod);
        req.setAttribute("shippingMethodLabel", shippingMethodLabel);

        req.setAttribute("orderItems", orderItems);
        req.setAttribute("orderItemsCount", orderItems.size());

        req.setAttribute("subtotalVnd", formatMoney(subtotal));
        req.setAttribute("discountVnd", formatMoney(discount));
        req.setAttribute("shippingFeeVnd", formatMoney(shippingFee));
        req.setAttribute("totalVnd", formatMoney(total));

        req.setAttribute("emailText", emailText);
        req.setAttribute("emailSent", emailSent);
        req.setAttribute(
                "emailNotice",
                emailSent
                        ? "Thông tin đơn hàng và hóa đơn đã được gửi về email của bạn."
                        : "Hệ thống sẽ gửi thông tin đơn hàng và hóa đơn về email của bạn sau khi xử lý xong."
        );
    }

    private List<OrderLineView> loadOrderItems(int orderId) {
        List<OrderLineView> result = new ArrayList<>();

        if (orderId <= 0) {
            return result;
        }

        try {
            List<?> rawItems = orderItemDAO.findByOrderId(orderId);

            if (rawItems == null) {
                return result;
            }

            for (Object raw : rawItems) {
                if (raw == null) {
                    continue;
                }

                String productName = firstNonBlank(
                        firstString(raw, "getProductName", "getTitle", "getName"),
                        "Sản phẩm"
                );

                String variantName = buildVariantText(raw);

                int quantity = toInt(firstValue(raw, "getQuantity"), 1);
                BigDecimal price = toBigDecimal(firstValue(raw, "getPrice", "getUnitPrice"));

                if (price == null) {
                    price = BigDecimal.ZERO;
                }

                BigDecimal lineTotal = toBigDecimal(firstValue(raw, "getSubtotal", "getTotalPrice", "getLineTotal"));

                if (lineTotal == null || lineTotal.compareTo(BigDecimal.ZERO) <= 0) {
                    lineTotal = price.multiply(BigDecimal.valueOf(quantity));
                }

                String imageUrl = safeString(firstString(raw, "getImageUrl", "getImage", "getProductImage"), "");

                result.add(new OrderLineView(
                        productName,
                        variantName,
                        quantity,
                        price.setScale(0, RoundingMode.HALF_UP),
                        lineTotal.setScale(0, RoundingMode.HALF_UP),
                        imageUrl,
                        formatMoney(price),
                        formatMoney(lineTotal)
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private String buildVariantText(Object raw) {
        String variantName = firstString(raw, "getVariantDisplayName", "getVariantName");
        String variantSize = firstString(raw, "getVariantSize");
        String variantType = firstString(raw, "getVariantType");

        List<String> parts = new ArrayList<>();

        if (!isBlank(variantName)) {
            parts.add(variantName);
        }

        if (!isBlank(variantSize)) {
            parts.add(variantSize);
        }

        if (!isBlank(variantType)) {
            parts.add(variantType);
        }

        if (parts.isEmpty()) {
            return "Mặc định";
        }

        return String.join(" - ", parts);
    }

    private boolean getEmailSentFlag(HttpSession session, int orderId) {
        if (session == null || orderId <= 0) {
            return false;
        }

        Object flag = session.getAttribute("ORDER_SUCCESS_EMAIL_SENT_" + orderId);

        if (flag instanceof Boolean) {
            return (Boolean) flag;
        }

        return false;
    }

    private String getUserEmail(User user) {
        if (user == null) {
            return "";
        }

        try {
            Object value = user.getClass().getMethod("getEmail").invoke(user);
            return value == null ? "" : String.valueOf(value).trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String buildFailMessage(String messageKey) {
        if (isBlank(messageKey)) {
            return "Giao dịch chưa hoàn tất.";
        }

        return switch (messageKey.toLowerCase(Locale.ROOT)) {
            case "checkout_failed" -> "Không thể hoàn tất thanh toán. Vui lòng thử lại.";
            case "order_not_found" -> "Không tìm thấy đơn hàng. Vui lòng kiểm tra lại.";
            case "order_create_failed" -> "Không thể tạo đơn hàng. Vui lòng thử lại.";
            case "payment_cancelled" -> "Bạn đã hủy thanh toán. Đơn hàng đã được lưu ở trạng thái chờ thanh toán, bạn có thể thanh toán lại trong lịch sử đơn hàng.";
            case "payment_failed" -> "Thanh toán thất bại. Đơn hàng đã được lưu ở trạng thái chờ thanh toán, bạn có thể bấm Thanh toán lại.";
            case "invalid_signature" -> "Chữ ký thanh toán không hợp lệ. Đơn hàng vẫn được giữ để bạn thanh toán lại.";
            case "invalid_amount" -> "Số tiền thanh toán không hợp lệ. Đơn hàng vẫn được giữ để bạn thanh toán lại.";
            case "amount_mismatch" -> "Số tiền thanh toán không khớp với đơn hàng. Đơn hàng vẫn được giữ để bạn thanh toán lại.";
            case "finalize_failed" -> "Không thể hoàn tất xác nhận thanh toán. Đơn hàng đã được lưu để bạn thanh toán lại.";
            default -> messageKey;
        };
    }

    private int extractOrderId(Order order) {
        Object idValue = firstValue(order, "getId", "getOrderId");

        if (idValue instanceof Number) {
            return ((Number) idValue).intValue();
        }

        Integer parsed = parseInteger(String.valueOf(idValue));
        return parsed != null ? parsed : 0;
    }

    private String formatPaymentMethod(String paymentMethod) {
        String value = safeString(paymentMethod, "UNKNOWN").toUpperCase(Locale.ROOT);

        return switch (value) {
            case "COD" -> "Thanh toán khi nhận hàng";
            case "VNPAY" -> "Thanh toán qua VNPAY";
            default -> "Không xác định";
        };
    }

    private String formatShippingMethod(String shippingMethod) {
        String value = safeString(shippingMethod, "ECONOMY").toUpperCase(Locale.ROOT);

        return switch (value) {
            case "FAST" -> "Giao hàng nhanh";
            case "EXPRESS" -> "Hỏa tốc";
            case "ECONOMY" -> "Giao hàng tiết kiệm";
            case "STANDARD" -> "Giao hàng tiêu chuẩn";
            default -> shippingMethod;
        };
    }

    private String formatOrderStatus(String status) {
        String value = safeString(status, "processing").toLowerCase(Locale.ROOT);

        return switch (value) {
            case "processing" -> "Đang xử lý";
            case "confirmed" -> "Đã xác nhận";
            case "shipping", "delivering" -> "Đang giao hàng";
            case "completed", "delivered" -> "Hoàn thành";
            case "cancelled", "canceled" -> "Đã hủy";
            case "pending" -> "Chờ xử lý";
            default -> status;
        };
    }

    private String formatReceivedDate(LocalDateTime createdAt,
                                      LocalDateTime deliveredAt,
                                      String shippingMethod) {

        if (deliveredAt != null) {
            return deliveredAt.format(DATE_FORMATTER);
        }

        if (createdAt == null) {
            return "Đang cập nhật";
        }

        String method = safeString(shippingMethod, "ECONOMY").toUpperCase(Locale.ROOT);

        if ("EXPRESS".equals(method)) {
            return createdAt.toLocalDate().format(DATE_FORMATTER);
        }

        if ("FAST".equals(method)) {
            LocalDate start = createdAt.toLocalDate().plusDays(1);
            LocalDate end = createdAt.toLocalDate().plusDays(3);
            return "Dự kiến " + start.format(DATE_FORMATTER) + " - " + end.format(DATE_FORMATTER);
        }

        LocalDate start = createdAt.toLocalDate().plusDays(3);
        LocalDate end = createdAt.toLocalDate().plusDays(5);
        return "Dự kiến " + start.format(DATE_FORMATTER) + " - " + end.format(DATE_FORMATTER);
    }

    private String formatDateTime(Object value) {
        LocalDateTime time = toLocalDateTime(value);

        if (time == null) {
            return "Đang cập nhật";
        }

        return time.format(DATE_TIME_FORMATTER);
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }

        try {
            if (value instanceof LocalDateTime) {
                return (LocalDateTime) value;
            }

            if (value instanceof LocalDate) {
                return ((LocalDate) value).atStartOfDay();
            }

            if (value instanceof java.sql.Timestamp) {
                return ((java.sql.Timestamp) value).toLocalDateTime();
            }

            if (value instanceof java.sql.Date) {
                return ((java.sql.Date) value).toLocalDate().atStartOfDay();
            }

            if (value instanceof Date) {
                return ((Date) value).toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
            }

            String raw = String.valueOf(value).trim();

            if (raw.isBlank()) {
                return null;
            }

            try {
                return LocalDateTime.parse(raw);
            } catch (Exception ignored) {
                return null;
            }

        } catch (Exception ignored) {
            return null;
        }
    }

    private String formatMoney(Object value) {
        BigDecimal amount = toBigDecimal(value);

        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        NumberFormat nf = NumberFormat.getInstance(VI_LOCALE);
        nf.setGroupingUsed(true);

        return nf.format(amount.setScale(0, RoundingMode.HALF_UP)) + " ₫";
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(0, RoundingMode.HALF_UP);
        }

        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue())
                    .setScale(0, RoundingMode.HALF_UP);
        }

        try {
            String raw = String.valueOf(value)
                    .replace(",", "")
                    .replace("₫", "")
                    .trim();

            if (raw.isBlank()) {
                return null;
            }

            return new BigDecimal(raw).setScale(0, RoundingMode.HALF_UP);
        } catch (Exception ignored) {
            return null;
        }
    }

    private int toInt(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private Object firstValue(Object target, String... getterNames) {
        if (target == null || getterNames == null) {
            return null;
        }

        for (String getterName : getterNames) {
            Object value = invokeGetter(target, getterName);

            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private String firstString(Object target, String... getterNames) {
        Object value = firstValue(target, getterNames);

        if (value == null) {
            return "";
        }

        return String.valueOf(value).trim();
    }

    private Object invokeGetter(Object target, String getterName) {
        if (target == null || isBlank(getterName)) {
            return null;
        }

        try {
            Method method = target.getClass().getMethod(getterName);
            return method.invoke(target);
        } catch (Exception ignored) {
            return null;
        }
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

    private Integer parseInteger(String value) {
        if (isBlank(value)) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeString(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    public static class OrderLineView {

        private final String productName;
        private final String variantName;
        private final int quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal lineTotal;
        private final String imageUrl;
        private final String unitPriceVnd;
        private final String lineTotalVnd;

        public OrderLineView(String productName,
                             String variantName,
                             int quantity,
                             BigDecimal unitPrice,
                             BigDecimal lineTotal,
                             String imageUrl,
                             String unitPriceVnd,
                             String lineTotalVnd) {
            this.productName = productName;
            this.variantName = variantName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.lineTotal = lineTotal;
            this.imageUrl = imageUrl;
            this.unitPriceVnd = unitPriceVnd;
            this.lineTotalVnd = lineTotalVnd;
        }

        public String getProductName() {
            return productName;
        }

        public String getVariantName() {
            return variantName;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public BigDecimal getLineTotal() {
            return lineTotal;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getUnitPriceVnd() {
            return unitPriceVnd;
        }

        public String getLineTotalVnd() {
            return lineTotalVnd;
        }
    }
}

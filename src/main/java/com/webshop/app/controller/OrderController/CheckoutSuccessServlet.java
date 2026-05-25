package com.webshop.app.controller.OrderController;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import com.webshop.app.dao.OrderDAO;
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

    private final OrderDAO orderDAO = new OrderDAO();

    private static final Locale VI_LOCALE = new Locale("vi", "VN");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

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
        String orderIdRaw = trim(req.getParameter("orderId"));

        Integer orderId = parseInteger(orderIdRaw);

        if (orderId == null || orderId <= 0) {
            forwardResultPage(
                    req,
                    resp,
                    false,
                    "Không tìm thấy mã đơn hàng. Vui lòng kiểm tra lại.",
                    null,
                    currentUser
            );
            return;
        }

        Order order = null;

        try {
            order = orderDAO.findById(orderId);
        } catch (Exception ex) {
            throw new ServletException("CheckoutSuccessServlet cannot load order #" + orderId, ex);
        }

        if (order == null) {
            forwardResultPage(
                    req,
                    resp,
                    false,
                    "Không tìm thấy đơn hàng. Vui lòng kiểm tra lại.",
                    null,
                    currentUser
            );
            return;
        }

        /*
         * Bảo mật:
         * User thường chỉ được xem đơn của chính mình.
         * Admin được phép xem tất cả.
         */
        if (!currentUser.isAdmin() && order.getUserId() != currentUser.getId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xem đơn hàng này.");
            return;
        }

        /*
         * Nếu URL success=true nhưng đơn không tồn tại thì fail.
         * Nếu đơn tồn tại, ưu tiên dữ liệu thật từ database thay vì tin param URL.
         */
        if (!success) {
            String failMessage = buildFailMessage(messageKey);
            forwardResultPage(req, resp, false, failMessage, order, currentUser);
            return;
        }

        forwardResultPage(req, resp, true, null, order, currentUser);
    }

    private void forwardResultPage(HttpServletRequest req,
                                   HttpServletResponse resp,
                                   boolean success,
                                   String message,
                                   Order order,
                                   User currentUser)
            throws ServletException, IOException {

        prepareOrderSuccessData(req, success, message, order, currentUser);

        req.setAttribute("pageTitle", "MyCosmetic | Kết quả thanh toán");
        req.setAttribute("pageCss", "/checkout-success.css");
        req.setAttribute("pageContent", "/jsp/checkout/checkout_success.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    private void prepareOrderSuccessData(HttpServletRequest req,
                                         boolean success,
                                         String message,
                                         Order order,
                                         User currentUser) {

        req.setAttribute("success", success);
        req.setAttribute("message", message);
        req.setAttribute("order", order);

        if (order == null) {
            req.setAttribute("paymentMethod", "UNKNOWN");
            req.setAttribute("paymentMethodLabel", "Không xác định");
            return;
        }

        int orderId = extractOrderId(order);

        String paymentMethod = safeString(order.getPaymentMethod(), "UNKNOWN");
        String paymentMethodLabel = formatPaymentMethod(paymentMethod);

        String orderStatus = safeString(
                firstString(order, "getStatus", "getOrderStatus"),
                "processing"
        );

        String orderStatusLabel = formatOrderStatus(orderStatus);

        Object totalValue = order.getTotal();
        String totalVnd = formatMoney(totalValue);

        Object subtotalValue = firstValue(order, "getSubtotal", "getSubTotal", "getTotalBeforeDiscount");
        Object discountValue = firstValue(order, "getDiscount", "getDiscountAmount");
        Object shippingFeeValue = firstValue(order, "getShippingFee", "getShipFee", "getDeliveryFee");

        String subtotalVnd = formatMoneyOrDash(subtotalValue);
        String discountVnd = formatMoneyOrZero(discountValue);
        String shippingFeeVnd = formatMoneyOrZero(shippingFeeValue);

        String shippingProvider = safeString(
                firstString(order, "getShippingProvider", "getDeliveryProvider"),
                "MyCosmetic Delivery"
        );

        String trackingCode = safeString(
                firstString(order, "getTrackingCode", "getTrackingNumber", "getShippingCode"),
                generateTrackingCode(orderId)
        );

        String shippingMethod = safeString(
                firstString(order, "getShippingMethod", "getDeliveryMethod"),
                "STANDARD"
        );

        String shippingMethodLabel = formatShippingMethod(shippingMethod);

        String receiverName = safeString(
                firstString(order, "getReceiverName", "getFullName", "getCustomerName", "getName"),
                safeString(currentUser.getFullName(), currentUser.getUsername())
        );

        String receiverPhone = safeString(
                firstString(order, "getReceiverPhone", "getPhone", "getCustomerPhone"),
                safeString(currentUser.getPhone(), "")
        );

        String shippingAddress = safeString(
                firstString(order, "getShippingAddress", "getAddress", "getReceiverAddress"),
                "Đang cập nhật"
        );

        Object createdAt = firstValue(order, "getCreatedAt", "getCreatedDate", "getOrderDate", "getCreatedTime");
        String createdAtText = formatDateTime(createdAt);

        req.setAttribute("orderId", orderId);
        req.setAttribute("orderCode", "#" + orderId);
        req.setAttribute("trackingCode", trackingCode);
        req.setAttribute("shippingProvider", shippingProvider);
        req.setAttribute("shippingMethod", shippingMethod);
        req.setAttribute("shippingMethodLabel", shippingMethodLabel);

        req.setAttribute("paymentMethod", paymentMethod);
        req.setAttribute("paymentMethodLabel", paymentMethodLabel);

        req.setAttribute("orderStatus", orderStatus);
        req.setAttribute("orderStatusLabel", orderStatusLabel);

        req.setAttribute("totalVnd", totalVnd);
        req.setAttribute("subtotalVnd", subtotalVnd);
        req.setAttribute("discountVnd", discountVnd);
        req.setAttribute("shippingFeeVnd", shippingFeeVnd);

        req.setAttribute("receiverName", receiverName);
        req.setAttribute("receiverPhone", receiverPhone);
        req.setAttribute("shippingAddress", shippingAddress);
        req.setAttribute("createdAtText", createdAtText);

        /*
         * Mục 91:
         * Servlet này chỉ hiển thị kết quả.
         * Gửi email nên xử lý ngay sau khi tạo đơn trong CheckoutServlet
         * hoặc sau khi VNPAY callback thành công.
         */
        req.setAttribute("emailNotice",
                "Thông tin đơn hàng sẽ được gửi về email nếu hệ thống email đã được cấu hình.");
    }

    private String buildFailMessage(String messageKey) {
        if (isBlank(messageKey)) {
            return "Giao dịch chưa hoàn tất.";
        }

        switch (messageKey.toLowerCase(Locale.ROOT)) {
            case "checkout_failed":
                return "Không thể hoàn tất thanh toán. Vui lòng thử lại.";
            case "order_not_found":
                return "Không tìm thấy đơn hàng. Vui lòng kiểm tra lại.";
            case "payment_cancelled":
                return "Bạn đã hủy thanh toán.";
            case "payment_failed":
                return "Thanh toán thất bại. Vui lòng thử lại.";
            default:
                return messageKey;
        }
    }

    private int extractOrderId(Order order) {
        Object idValue = firstValue(order, "getId", "getOrderId");

        if (idValue instanceof Number) {
            return ((Number) idValue).intValue();
        }

        Integer parsed = parseInteger(String.valueOf(idValue));

        return parsed != null ? parsed : 0;
    }

    private String generateTrackingCode(int orderId) {
        if (orderId <= 0) {
            return "Đang cập nhật";
        }

        return "MC-" + String.format("%06d", orderId);
    }

    private String formatPaymentMethod(String paymentMethod) {
        String value = safeString(paymentMethod, "UNKNOWN").toUpperCase(Locale.ROOT);

        switch (value) {
            case "COD":
                return "Thanh toán khi nhận hàng (COD)";
            case "VNPAY":
                return "Thanh toán qua VNPAY";
            default:
                return "Không xác định";
        }
    }

    private String formatShippingMethod(String shippingMethod) {
        String value = safeString(shippingMethod, "STANDARD").toUpperCase(Locale.ROOT);

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
                return shippingMethod;
        }
    }

    private String formatOrderStatus(String status) {
        String value = safeString(status, "processing").toLowerCase(Locale.ROOT);

        switch (value) {
            case "processing":
                return "Đang xử lý";
            case "confirmed":
                return "Đã xác nhận";
            case "shipping":
                return "Đang giao hàng";
            case "completed":
                return "Hoàn thành";
            case "cancelled":
            case "canceled":
                return "Đã hủy";
            case "pending":
                return "Chờ xử lý";
            default:
                return status;
        }
    }

    private String formatMoney(Object value) {
        BigDecimal amount = toBigDecimal(value);

        if (amount == null) {
            return "0 ₫";
        }

        NumberFormat nf = NumberFormat.getInstance(VI_LOCALE);
        nf.setGroupingUsed(true);

        return nf.format(amount) + " ₫";
    }

    private String formatMoneyOrDash(Object value) {
        BigDecimal amount = toBigDecimal(value);

        if (amount == null) {
            return "-";
        }

        return formatMoney(amount);
    }

    private String formatMoneyOrZero(Object value) {
        BigDecimal amount = toBigDecimal(value);

        if (amount == null) {
            return "0 ₫";
        }

        return formatMoney(amount);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }

        try {
            String raw = String.valueOf(value)
                    .replace(",", "")
                    .replace("₫", "")
                    .trim();

            if (raw.isBlank()) {
                return null;
            }

            return new BigDecimal(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String formatDateTime(Object value) {
        if (value == null) {
            return "Đang cập nhật";
        }

        try {
            if (value instanceof LocalDateTime) {
                return ((LocalDateTime) value).format(DATE_TIME_FORMATTER);
            }

            if (value instanceof LocalDate) {
                return ((LocalDate) value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            if (value instanceof java.sql.Timestamp) {
                LocalDateTime time = ((java.sql.Timestamp) value).toLocalDateTime();
                return time.format(DATE_TIME_FORMATTER);
            }

            if (value instanceof java.sql.Date) {
                LocalDate date = ((java.sql.Date) value).toLocalDate();
                return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            if (value instanceof Date) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy");
                return sdf.format((Date) value);
            }

            return String.valueOf(value);
        } catch (Exception ignored) {
            return String.valueOf(value);
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

    private Integer parseInteger(String value) {
        if (isBlank(value)) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
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
}
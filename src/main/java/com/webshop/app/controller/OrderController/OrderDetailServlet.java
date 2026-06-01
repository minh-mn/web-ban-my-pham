package com.webshop.app.controller.OrderController;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.webshop.app.dao.AdminOrderDAO;
import com.webshop.app.dao.AdminOrderDAO.OrderTrackingView;
import com.webshop.app.dao.CancelRequestDAO;
import com.webshop.app.dao.OrderDAO;
import com.webshop.app.dao.OrderItemDAO;
import com.webshop.app.dao.ReturnRequestDAO;
import com.webshop.app.dao.ReviewDAO;
import com.webshop.app.model.Order;
import com.webshop.app.model.OrderItem;
import com.webshop.app.model.User;
import com.webshop.app.utils.OrderStatusUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/orders/detail")
public class OrderDetailServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO itemDAO = new OrderItemDAO();
    private final AdminOrderDAO adminOrderDAO = new AdminOrderDAO();
    private final ReturnRequestDAO returnRequestDAO = new ReturnRequestDAO();
    private final CancelRequestDAO cancelRequestDAO = new CancelRequestDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int orderId = parseInt(req.getParameter("id"), -1);

        if (orderId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/orders");
            return;
        }

        orderDAO.autoCompleteUnconfirmedDeliveredOrders(7);

        Order order = orderDAO.findById(orderId);

        if (order == null) {
            resp.sendRedirect(req.getContextPath() + "/orders");
            return;
        }

        if (!user.isAdmin() && order.getUserId() != user.getId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        List<OrderItem> items = itemDAO.findByOrderId(orderId);
        List<OrderTrackingView> trackingList = loadTrackingSafely(orderId);

        String orderStatus = normalizeOrderStatus(order.getStatus());
        String shippingStatus = normalizeShippingStatus(invokeString(order, "getShippingStatus", ""));
        String paymentStatus = normalizePaymentStatus(invokeString(order, "getPaymentStatus", ""));

        order.setStatusLabel(OrderStatusUtils.toLabel(order.getStatus()));

        req.setAttribute("order", order);
        req.setAttribute("orderItems", items);
        req.setAttribute("trackingList", trackingList);
        req.setAttribute("cancelRequest", cancelRequestDAO.findByOrderId(orderId));
        req.setAttribute("returnRequest", returnRequestDAO.findByOrderId(orderId));

        /*
         * Thuộc tính cho giao diện chính của khách hàng:
         * JSP có thể dùng các biến này để hiển thị timeline:
         * Chờ xác nhận -> Đã xác nhận -> Đang giao -> Giao thành công.
         */
        req.setAttribute("orderStatus", orderStatus);
        req.setAttribute("shippingStatus", shippingStatus);
        req.setAttribute("paymentStatus", paymentStatus);
        req.setAttribute("shippingStatusLabel", shippingStatusLabel(shippingStatus));
        req.setAttribute("paymentStatusLabel", paymentStatusLabel(paymentStatus));

        req.setAttribute("stepProcessingDone", isStepProcessingDone(orderStatus));
        req.setAttribute("stepConfirmedDone", isStepConfirmedDone(orderStatus));
        req.setAttribute("stepShippingDone", isStepShippingDone(orderStatus, shippingStatus));
        req.setAttribute("stepCompletedDone", isStepCompletedDone(orderStatus, shippingStatus));
        req.setAttribute("orderCancelled", "cancelled".equals(orderStatus));
        req.setAttribute("shippingFailed", "FAILED".equals(shippingStatus));
        req.setAttribute("shippingCanceled", "CANCELED".equals(shippingStatus));

        req.setAttribute("shippingProviderLabel", firstNotBlank(
                invokeString(order, "getShippingProviderLabel", ""),
                shippingProviderLabel(invokeString(order, "getShippingProvider", ""))
        ));
        req.setAttribute("shippingMethodLabel", firstNotBlank(
                invokeString(order, "getShippingMethodLabel", ""),
                shippingMethodLabel(invokeString(order, "getShippingMethod", ""))
        ));
        req.setAttribute("shippingCode", invokeString(order, "getShippingCode", ""));
        req.setAttribute("shippedAtDate", invokeDate(order, "getShippedAtDate"));
        req.setAttribute("deliveredAtDate", invokeDate(order, "getDeliveredAtDate"));

        // Các biến riêng cho JSP để tránh lỗi EL PropertyNotFoundException
        // khi Tomcat vẫn đang giữ class Order cũ trong artifact exploded.
        boolean customerReceivedConfirmed = invokeBoolean(order, "isCustomerReceivedConfirmed", false);
        boolean delivered = invokeBoolean(order, "isDelivered", false)
                || "completed".equals(orderStatus)
                || "DELIVERED".equals(shippingStatus);
        boolean shippingCanceledByOrder = invokeBoolean(order, "isShippingCanceled", false)
                || "CANCELED".equals(shippingStatus);
        boolean receiveConfirmable = delivered && !customerReceivedConfirmed && !shippingCanceledByOrder;

        String receiveStatusLabel = invokeString(order, "getReceiveStatusLabel", null);

        if (receiveStatusLabel == null || receiveStatusLabel.isBlank()) {
            if (customerReceivedConfirmed) {
                receiveStatusLabel = "Khách hàng đã xác nhận đã nhận hàng";
            } else if (delivered) {
                receiveStatusLabel = "Chờ khách hàng xác nhận đã nhận hàng";
            } else if ("FAILED".equals(shippingStatus)) {
                receiveStatusLabel = "Giao hàng thất bại";
            } else if ("CANCELED".equals(shippingStatus)) {
                receiveStatusLabel = "Đơn hàng đã hủy";
            } else {
                receiveStatusLabel = "Chưa giao thành công";
            }
        }

        req.setAttribute("customerReceivedConfirmed", customerReceivedConfirmed);
        req.setAttribute("receiveConfirmable", receiveConfirmable);
        req.setAttribute("receiveStatusLabel", receiveStatusLabel);
        req.setAttribute("customerReceivedAtDate", invokeDate(order, "getCustomerReceivedAtDate"));
        req.setAttribute("receiveConfirmNote", invokeString(order, "getReceiveConfirmNote", null));

        req.setAttribute("reviewedOrderItemMap", reviewDAO.findReviewedOrderItemMap(user.getId(), orderId));
        req.setAttribute("success", req.getParameter("success"));
        req.setAttribute("error", req.getParameter("error"));

        req.setAttribute("pageTitle", "MyCosmetic | Chi tiết đơn hàng #" + order.getId());
        req.setAttribute("pageCss", "/order-detail.css");
        req.setAttribute("pageContent", "/jsp/order/order_detail.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    private List<OrderTrackingView> loadTrackingSafely(int orderId) {
        try {
            return adminOrderDAO.findTrackingByOrderId(orderId);
        } catch (RuntimeException e) {
            /*
             * Không để lỗi bảng tracking làm hỏng trang chi tiết đơn hàng.
             * Nếu chưa có bảng store_order_tracking, trang vẫn xem được đơn hàng.
             */
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /* =========================================================
       ORDER WORKFLOW FLAGS
    ========================================================= */

    private boolean isStepProcessingDone(String orderStatus) {
        return switch (orderStatus) {
            case "processing", "confirmed", "shipping", "completed" -> true;
            default -> false;
        };
    }

    private boolean isStepConfirmedDone(String orderStatus) {
        return switch (orderStatus) {
            case "confirmed", "shipping", "completed" -> true;
            default -> false;
        };
    }

    private boolean isStepShippingDone(String orderStatus, String shippingStatus) {
        return "shipping".equals(orderStatus)
                || "completed".equals(orderStatus)
                || "DELIVERING".equals(shippingStatus)
                || "DELIVERED".equals(shippingStatus);
    }

    private boolean isStepCompletedDone(String orderStatus, String shippingStatus) {
        return "completed".equals(orderStatus)
                || "DELIVERED".equals(shippingStatus);
    }

    /* =========================================================
       LABELS
    ========================================================= */

    private String shippingStatusLabel(String shippingStatus) {
        return switch (normalizeShippingStatus(shippingStatus)) {
            case "PENDING_PICKUP" -> "Chờ lấy hàng";
            case "DELIVERING" -> "Đang giao hàng";
            case "DELIVERED" -> "Giao thành công";
            case "FAILED" -> "Giao thất bại";
            case "CANCELED" -> "Đã hủy giao hàng";
            default -> "Chưa có trạng thái vận chuyển";
        };
    }

    private String paymentStatusLabel(String paymentStatus) {
        return switch (normalizePaymentStatus(paymentStatus)) {
            case "PAID" -> "Đã thanh toán";
            case "FAILED" -> "Thanh toán thất bại";
            case "CANCELED" -> "Đã hủy thanh toán";
            case "REFUNDED" -> "Đã hoàn tiền";
            case "PENDING" -> "Chờ thanh toán";
            default -> "Chờ thanh toán";
        };
    }

    private String shippingProviderLabel(String provider) {
        return switch (trim(provider).toUpperCase()) {
            case "INTERNAL" -> "Shop tự giao";
            case "GHTK" -> "Giao Hàng Tiết Kiệm";
            case "GHN" -> "Giao Hàng Nhanh";
            case "VIETTEL_POST" -> "Viettel Post";
            case "OTHER" -> "Đơn vị khác";
            default -> "Chưa chọn đơn vị vận chuyển";
        };
    }

    private String shippingMethodLabel(String method) {
        return switch (trim(method).toUpperCase()) {
            case "ECONOMY" -> "Tiết kiệm";
            case "FAST" -> "Nhanh";
            case "EXPRESS" -> "Hỏa tốc";
            default -> "Chưa chọn phương thức giao";
        };
    }

    /* =========================================================
       NORMALIZE / PARSE
    ========================================================= */

    private String normalizeOrderStatus(String status) {
        String value = trim(status).toLowerCase();

        if ("canceled".equals(value)) {
            return "cancelled";
        }

        return value;
    }

    private String normalizeShippingStatus(String shippingStatus) {
        String value = trim(shippingStatus).toUpperCase();

        if ("CANCELLED".equals(value)) {
            return "CANCELED";
        }

        return switch (value) {
            case "PENDING_PICKUP", "DELIVERING", "DELIVERED", "FAILED", "CANCELED" -> value;
            default -> "";
        };
    }

    private String normalizePaymentStatus(String paymentStatus) {
        String value = trim(paymentStatus).toUpperCase();

        if ("CANCELLED".equals(value)) {
            return "CANCELED";
        }

        return switch (value) {
            case "PENDING", "PAID", "FAILED", "CANCELED", "REFUNDED" -> value;
            default -> "PENDING";
        };
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(trim(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstNotBlank(String... values) {
        if (values == null) {
            return "";
        }

        for (String value : values) {
            String trimmed = trim(value);

            if (!trimmed.isBlank()) {
                return trimmed;
            }
        }

        return "";
    }

    /* =========================================================
       REFLECTION HELPERS
    ========================================================= */

    private static boolean invokeBoolean(Object target, String methodName, boolean defaultValue) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value instanceof Boolean ? (Boolean) value : defaultValue;
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private static String invokeString(Object target, String methodName, String defaultValue) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value != null ? String.valueOf(value) : defaultValue;
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private static Date invokeDate(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value instanceof Date ? (Date) value : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}

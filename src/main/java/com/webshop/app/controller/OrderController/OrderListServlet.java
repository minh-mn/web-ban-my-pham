package com.webshop.app.controller.OrderController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.model.Order;
import com.webshop.app.model.OrderStatus;
import com.webshop.app.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/orders")
public class OrderListServlet extends HttpServlet {

    private static final String FILTER_ALL = "all";
    private static final String FILTER_PROCESSING = "processing";
    private static final String FILTER_CONFIRMED = "confirmed";
    private static final String FILTER_SHIPPING = "shipping";
    private static final String FILTER_COMPLETED = "completed";
    private static final String FILTER_CANCELLED = "cancelled";
    private static final String FILTER_PAYMENT_FAILED = "payment_failed";

    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        orderDAO.autoCompleteUnconfirmedDeliveredOrders(7);

        List<Order> allOrders = user.isAdmin()
                ? orderDAO.findAll()
                : orderDAO.findByUser(user.getId());

        String filter = normalizeFilter(firstNonBlank(req.getParameter("filter"), req.getParameter("status")));
        List<Order> orders = filterOrders(allOrders, filter);

        req.setAttribute("orders", orders);
        req.setAttribute("orderFilter", filter);
        req.setAttribute("orderFilterLabel", filterLabel(filter));
        req.setAttribute("orderFilterDescription", filterDescription(filter));
        req.setAttribute("orderTotalCount", allOrders.size());
        req.setAttribute("orderFilteredCount", orders.size());

        // ✅ dòng này quyết định dropdown có ra label tiếng Việt hay không
        req.setAttribute("statusChoices", OrderStatus.choices());

        req.setAttribute("pageTitle", "MyCosmetic | Đơn hàng");
        req.setAttribute("pageCss", "/order.css");
        req.setAttribute("pageContent", "/jsp/order/order_list.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) {
            return first;
        }

        if (second != null && !second.trim().isEmpty()) {
            return second;
        }

        return null;
    }

    private static String normalizeFilter(String rawFilter) {
        if (rawFilter == null || rawFilter.trim().isEmpty()) {
            return FILTER_ALL;
        }

        String normalized = rawFilter.trim().toLowerCase(Locale.ROOT)
                .replace('-', '_');

        return switch (normalized) {
            case "all", "tat_ca", "lich_su" -> FILTER_ALL;

            case "processing", "pending", "cho_xac_nhan", "wait_confirm", "waiting_confirm" ->
                    FILTER_PROCESSING;

            case "confirmed", "cho_lay_hang", "pickup", "waiting_pickup", "pending_pickup" ->
                    FILTER_CONFIRMED;

            case "shipping", "delivering", "dang_giao" ->
                    FILTER_SHIPPING;

            case "completed", "delivered", "done", "danh_gia", "hoan_hang", "return", "review" ->
                    FILTER_COMPLETED;

            case "cancelled", "canceled", "da_huy" ->
                    FILTER_CANCELLED;

            case "payment_failed", "retry_payment", "thanh_toan_lai" ->
                    FILTER_PAYMENT_FAILED;

            default -> FILTER_ALL;
        };
    }

    private static List<Order> filterOrders(List<Order> allOrders, String filter) {
        if (allOrders == null || allOrders.isEmpty() || FILTER_ALL.equals(filter)) {
            return allOrders == null ? new ArrayList<>() : allOrders;
        }

        List<Order> result = new ArrayList<>();

        for (Order order : allOrders) {
            if (matchesFilter(order, filter)) {
                result.add(order);
            }
        }

        return result;
    }

    private static boolean matchesFilter(Order order, String filter) {
        if (order == null) {
            return false;
        }

        String orderStatus = lower(order.getStatus());
        String shippingStatus = upper(order.getShippingStatus());
        String paymentStatus = upper(order.getPaymentStatus());

        return switch (filter) {
            case FILTER_PROCESSING ->
                    "processing".equals(orderStatus) || "pending".equals(orderStatus);

            case FILTER_CONFIRMED ->
                    "confirmed".equals(orderStatus)
                            || ("PENDING_PICKUP".equals(shippingStatus) && !"processing".equals(orderStatus));

            case FILTER_SHIPPING ->
                    "shipping".equals(orderStatus)
                            || "DELIVERING".equals(shippingStatus);

            case FILTER_COMPLETED ->
                    "completed".equals(orderStatus)
                            || "DELIVERED".equals(shippingStatus);

            case FILTER_CANCELLED ->
                    "cancelled".equals(orderStatus)
                            || "canceled".equals(orderStatus)
                            || "CANCELED".equals(shippingStatus);

            case FILTER_PAYMENT_FAILED ->
                    "FAILED".equals(paymentStatus)
                            || "CANCELED".equals(paymentStatus)
                            || "CANCELLED".equals(paymentStatus);

            default -> true;
        };
    }

    private static String filterLabel(String filter) {
        return switch (filter) {
            case FILTER_PROCESSING -> "Chờ xác nhận";
            case FILTER_CONFIRMED -> "Chờ lấy hàng";
            case FILTER_SHIPPING -> "Đang giao";
            case FILTER_COMPLETED -> "Đánh giá / Hoàn hàng";
            case FILTER_CANCELLED -> "Đã hủy";
            case FILTER_PAYMENT_FAILED -> "Thanh toán lại";
            default -> "Tất cả đơn hàng";
        };
    }

    private static String filterDescription(String filter) {
        return switch (filter) {
            case FILTER_PROCESSING -> "Các đơn mới tạo, đang chờ shop xác nhận.";
            case FILTER_CONFIRMED -> "Các đơn đã được xác nhận và đang chờ shop/đơn vị vận chuyển lấy hàng.";
            case FILTER_SHIPPING -> "Các đơn đang được vận chuyển tới địa chỉ nhận hàng.";
            case FILTER_COMPLETED -> "Các đơn đã giao thành công, có thể xem chi tiết, đánh giá hoặc gửi yêu cầu hoàn hàng nếu đủ điều kiện.";
            case FILTER_CANCELLED -> "Các đơn đã bị hủy hoặc vận chuyển đã hủy.";
            case FILTER_PAYMENT_FAILED -> "Các đơn cần thanh toán lại do thanh toán thất bại hoặc đã hủy giao dịch.";
            default -> "Theo dõi trạng thái đơn hàng, vận chuyển và xem chi tiết tracking.";
        };
    }

    private static String lower(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String upper(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}

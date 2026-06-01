package com.webshop.app.controller.AdminController;

import java.io.IOException;

import com.webshop.app.dao.AdminOrderDAO;
import com.webshop.app.dao.NotificationDAO;
import com.webshop.app.dao.OrderDAO;
import com.webshop.app.model.Order;
import com.webshop.app.model.User;
import com.webshop.app.utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/order/update-status")
public class AdminOrderUpdateStatusServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final OrderDAO orderDAO = new OrderDAO();
    private final AdminOrderDAO adminOrderDAO = new AdminOrderDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private static final String CSRF_SESSION_KEY = "CSRF_TOKEN";
    private static final String CSRF_PARAM = "csrf_token";

    private static final String FLASH_SUCCESS = "admin_order_success";
    private static final String FLASH_ERROR = "admin_order_error";

    private static final String WF_CONFIRM_ORDER = "confirmOrder";
    private static final String WF_START_SHIPPING = "startShipping";
    private static final String WF_MARK_DELIVERED = "markDelivered";
    private static final String WF_MARK_FAILED = "markFailed";
    private static final String WF_CANCEL_ORDER = "cancelOrder";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User admin = getCurrentUser(session);

        if (admin == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (!admin.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return;
        }

        if (!isValidCsrf(req, session)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF invalid");
            return;
        }

        int orderId = parseInt(firstNotBlank(
                req.getParameter("orderId"),
                req.getParameter("id")
        ), -1);

        String returnUrl = safeReturnUrl(
                req.getParameter("returnUrl"),
                req.getContextPath(),
                orderId
        );

        if (orderId <= 0) {
            setFlashError(session, "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + returnUrl);
            return;
        }

        String workflowAction = resolveWorkflowAction(req);

        if (workflowAction.isBlank()) {
            setFlashError(session, "Thao tác cập nhật đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + returnUrl);
            return;
        }

        try {
            Order order = orderDAO.findById(orderId);

            if (order == null) {
                setFlashError(session, "Không tìm thấy đơn hàng #" + orderId + ".");
                resp.sendRedirect(req.getContextPath() + returnUrl);
                return;
            }

            WorkflowResult result = executeWorkflow(
                    req,
                    order,
                    admin.getId(),
                    workflowAction
            );

            if (!result.success()) {
                setFlashError(session, result.message());
                resp.sendRedirect(req.getContextPath() + returnUrl);
                return;
            }

            createNotificationSafely(order, result.notificationStatus());

            setFlashSuccess(session, result.message());
            resp.sendRedirect(req.getContextPath() + returnUrl);

        } catch (Exception e) {
            e.printStackTrace();
            setFlashError(session, "Cập nhật trạng thái đơn hàng thất bại.");
            resp.sendRedirect(req.getContextPath() + returnUrl);
        }
    }

    /* =========================================================
       WORKFLOW
    ========================================================= */

    private WorkflowResult executeWorkflow(
            HttpServletRequest req,
            Order order,
            Integer adminId,
            String workflowAction
    ) {

        return switch (workflowAction) {
            case WF_CONFIRM_ORDER -> confirmOrder(req, order, adminId);
            case WF_START_SHIPPING -> startShipping(req, order, adminId);
            case WF_MARK_DELIVERED -> markDelivered(req, order, adminId);
            case WF_MARK_FAILED -> markFailed(req, order, adminId);
            case WF_CANCEL_ORDER -> cancelOrder(req, order, adminId);
            default -> WorkflowResult.fail("Thao tác cập nhật đơn hàng không hợp lệ.");
        };
    }

    private WorkflowResult confirmOrder(
            HttpServletRequest req,
            Order order,
            Integer adminId
    ) {

        String currentStatus = normalizeStatus(order.getStatus());
        String currentShippingStatus = normalizeShippingStatus(order.getShippingStatus());

        if (!"processing".equals(currentStatus)) {
            return WorkflowResult.fail("Chỉ có thể xác nhận đơn hàng đang chờ xử lý.");
        }

        if (isFinalShippingStatus(currentShippingStatus)) {
            return WorkflowResult.fail("Không thể xác nhận đơn hàng đã kết thúc vận chuyển.");
        }

        String paymentStatus = normalizePaymentStatus(order.getPaymentStatus());
        String note = resolveTrackingNote(
                req,
                "Admin đã xác nhận đơn hàng. Đơn hàng đang chờ bàn giao cho đơn vị vận chuyển."
        );

        orderDAO.updateStatusAndPaymentStatus(
                order.getId(),
                "confirmed",
                paymentStatus
        );

        updateShippingStatusSafely(
                order.getId(),
                "PENDING_PICKUP",
                adminId,
                note
        );

        return WorkflowResult.ok(
                "Đã xác nhận đơn hàng thành công.",
                "confirmed"
        );
    }

    private WorkflowResult startShipping(
            HttpServletRequest req,
            Order order,
            Integer adminId
    ) {

        String currentStatus = normalizeStatus(order.getStatus());
        String currentShippingStatus = normalizeShippingStatus(order.getShippingStatus());

        boolean canStartFromConfirmed = "confirmed".equals(currentStatus);
        boolean canRetryAfterFailed = "shipping".equals(currentStatus)
                && "FAILED".equals(currentShippingStatus);

        if (!canStartFromConfirmed && !canRetryAfterFailed) {
            return WorkflowResult.fail(
                    "Chỉ có thể bắt đầu giao khi đơn đã xác nhận hoặc giao thất bại trước đó."
            );
        }

        String paymentStatus = normalizePaymentStatus(order.getPaymentStatus());
        String note = resolveTrackingNote(
                req,
                "Đơn hàng đã được bàn giao cho đơn vị vận chuyển và bắt đầu giao cho khách."
        );

        orderDAO.updateStatusAndPaymentStatus(
                order.getId(),
                "shipping",
                paymentStatus
        );

        updateShippingStatusSafely(
                order.getId(),
                "DELIVERING",
                adminId,
                note
        );

        return WorkflowResult.ok(
                "Đã chuyển đơn hàng sang trạng thái đang giao.",
                "shipping"
        );
    }

    private WorkflowResult markDelivered(
            HttpServletRequest req,
            Order order,
            Integer adminId
    ) {

        String currentStatus = normalizeStatus(order.getStatus());
        String currentShippingStatus = normalizeShippingStatus(order.getShippingStatus());

        if (!"shipping".equals(currentStatus)) {
            return WorkflowResult.fail("Chỉ có thể xác nhận giao thành công khi đơn hàng đang giao.");
        }

        if (!currentShippingStatus.isBlank()
                && !"DELIVERING".equals(currentShippingStatus)) {
            return WorkflowResult.fail("Chỉ có thể giao thành công khi trạng thái vận chuyển là đang giao.");
        }

        String note = resolveTrackingNote(
                req,
                "Đơn hàng đã giao thành công cho khách."
        );

        orderDAO.updateStatusAndPaymentStatus(
                order.getId(),
                "completed",
                "PAID"
        );

        updateShippingStatusSafely(
                order.getId(),
                "DELIVERED",
                adminId,
                note
        );

        return WorkflowResult.ok(
                "Đã xác nhận giao hàng thành công.",
                "completed"
        );
    }

    private WorkflowResult markFailed(
            HttpServletRequest req,
            Order order,
            Integer adminId
    ) {

        String currentStatus = normalizeStatus(order.getStatus());
        String currentShippingStatus = normalizeShippingStatus(order.getShippingStatus());

        if (!"shipping".equals(currentStatus)) {
            return WorkflowResult.fail("Chỉ có thể đánh dấu giao thất bại khi đơn hàng đang giao.");
        }

        if (!currentShippingStatus.isBlank()
                && !"DELIVERING".equals(currentShippingStatus)) {
            return WorkflowResult.fail("Chỉ có thể đánh dấu thất bại khi trạng thái vận chuyển là đang giao.");
        }

        String paymentStatus = normalizePaymentStatus(order.getPaymentStatus());
        String note = resolveTrackingNote(
                req,
                "Giao hàng thất bại. Admin cần kiểm tra để giao lại hoặc hủy đơn."
        );

        /*
         * Giao thất bại chưa phải là hủy đơn.
         * Giữ order.status = shipping để admin có thể chọn giao lại hoặc hủy sau khi xử lý.
         */
        orderDAO.updateStatusAndPaymentStatus(
                order.getId(),
                "shipping",
                paymentStatus
        );

        updateShippingStatusSafely(
                order.getId(),
                "FAILED",
                adminId,
                note
        );

        return WorkflowResult.ok(
                "Đã đánh dấu đơn hàng giao thất bại.",
                "shipping"
        );
    }

    private WorkflowResult cancelOrder(
            HttpServletRequest req,
            Order order,
            Integer adminId
    ) {

        String currentStatus = normalizeStatus(order.getStatus());
        String currentShippingStatus = normalizeShippingStatus(order.getShippingStatus());

        boolean canCancelBeforeShipping = "processing".equals(currentStatus)
                || "confirmed".equals(currentStatus);

        boolean canCancelAfterFailed = "shipping".equals(currentStatus)
                && "FAILED".equals(currentShippingStatus);

        if (!canCancelBeforeShipping && !canCancelAfterFailed) {
            return WorkflowResult.fail(
                    "Chỉ có thể hủy đơn khi đơn chưa giao hoặc đã giao thất bại."
            );
        }

        if (isFinalOrderStatus(currentStatus)) {
            return WorkflowResult.fail("Không thể hủy đơn hàng đã hoàn tất hoặc đã hủy.");
        }

        String note = resolveTrackingNote(
                req,
                "Admin đã hủy đơn hàng."
        );

        orderDAO.updateStatusAndPaymentStatus(
                order.getId(),
                "cancelled",
                "CANCELED"
        );

        updateShippingStatusSafely(
                order.getId(),
                "CANCELED",
                adminId,
                note
        );

        return WorkflowResult.ok(
                "Đã hủy đơn hàng thành công.",
                "cancelled"
        );
    }

    /* =========================================================
       LEGACY PARAM SUPPORT
    ========================================================= */

    private String resolveWorkflowAction(HttpServletRequest req) {

        String workflowAction = firstNotBlank(
                req.getParameter("workflowAction"),
                req.getParameter("orderAction")
        );

        if (!workflowAction.isBlank()) {
            return workflowAction;
        }

        /*
         * Hỗ trợ form cũ đang gửi status trực tiếp.
         * Không update tự do nữa, mà map status sang workflow hợp lệ.
         */
        String status = normalizeStatus(req.getParameter("status"));
        String shippingStatus = normalizeShippingStatus(req.getParameter("shippingStatus"));

        if (!shippingStatus.isBlank()) {
            return switch (shippingStatus) {
                case "PENDING_PICKUP" -> WF_CONFIRM_ORDER;
                case "DELIVERING" -> WF_START_SHIPPING;
                case "DELIVERED" -> WF_MARK_DELIVERED;
                case "FAILED" -> WF_MARK_FAILED;
                case "CANCELED" -> WF_CANCEL_ORDER;
                default -> "";
            };
        }

        return switch (status) {
            case "confirmed" -> WF_CONFIRM_ORDER;
            case "shipping" -> WF_START_SHIPPING;
            case "completed" -> WF_MARK_DELIVERED;
            case "cancelled", "canceled" -> WF_CANCEL_ORDER;
            default -> "";
        };
    }

    /* =========================================================
       NOTIFICATION / SHIPPING
    ========================================================= */

    private void createNotificationSafely(Order order, String notificationStatus) {

        if (order == null || notificationStatus == null || notificationStatus.isBlank()) {
            return;
        }

        try (java.sql.Connection conn = DBConnection.getConnection()) {
            notificationDAO.createOrderNotification(
                    conn,
                    order.getUserId(),
                    order.getId(),
                    notificationStatus
            );
        } catch (Exception e) {
            /*
             * Không để lỗi notification làm hỏng workflow đơn hàng.
             */
            e.printStackTrace();
        }
    }

    private void updateShippingStatusSafely(
            int orderId,
            String shippingStatus,
            Integer adminId,
            String note
    ) {

        try {
            adminOrderDAO.updateShippingStatus(
                    orderId,
                    shippingStatus,
                    adminId,
                    note
            );
        } catch (Exception e) {
            /*
             * Nếu database cũ thiếu bảng tracking/cột vận chuyển,
             * workflow chính vẫn không bị sập.
             * Nên chạy migration để cập nhật đầy đủ tracking, shipped_at, delivered_at.
             */
            e.printStackTrace();
        }
    }

    /* =========================================================
       VALIDATION / NORMALIZE
    ========================================================= */

    private boolean isFinalOrderStatus(String status) {

        return switch (normalizeStatus(status)) {
            case "completed", "cancelled" -> true;
            default -> false;
        };
    }

    private boolean isFinalShippingStatus(String shippingStatus) {

        return switch (normalizeShippingStatus(shippingStatus)) {
            case "DELIVERED", "CANCELED" -> true;
            default -> false;
        };
    }

    private String normalizeStatus(String status) {

        String value = trim(status).toLowerCase();

        if ("canceled".equals(value)) {
            return "cancelled";
        }

        return value;
    }

    private String normalizePaymentStatus(String paymentStatus) {

        String value = trim(paymentStatus).toUpperCase();

        if (value.isBlank()) {
            return "PENDING";
        }

        if ("CANCELLED".equals(value)) {
            return "CANCELED";
        }

        return switch (value) {
            case "PENDING", "PAID", "FAILED", "CANCELED", "REFUNDED" -> value;
            default -> "PENDING";
        };
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

    private String resolveTrackingNote(HttpServletRequest req, String fallback) {

        String note = firstNotBlank(
                req.getParameter("trackingNote"),
                req.getParameter("note")
        );

        if (note.isBlank()) {
            return fallback;
        }

        return note;
    }

    private boolean isValidCsrf(HttpServletRequest req, HttpSession session) {

        if (session == null) {
            return false;
        }

        String token = (String) session.getAttribute(CSRF_SESSION_KEY);
        String sent = req.getParameter(CSRF_PARAM);

        return token != null && sent != null && token.equals(sent);
    }

    private User getCurrentUser(HttpSession session) {

        if (session == null) {
            return null;
        }

        Object user = session.getAttribute("user");

        if (user == null) {
            user = session.getAttribute("authUser");
        }

        if (user instanceof User currentUser) {
            return currentUser;
        }

        return null;
    }

    private int parseInt(String value, int fallback) {

        try {
            return Integer.parseInt(trim(value));
        } catch (Exception e) {
            return fallback;
        }
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

    private String trim(String value) {

        return value == null ? "" : value.trim();
    }

    private String safeReturnUrl(
            String returnUrl,
            String contextPath,
            int orderId
    ) {

        String fallback = orderId > 0
                ? "/admin/orders?action=detail&id=" + orderId
                : "/admin/orders";

        if (returnUrl == null || returnUrl.isBlank() || "null".equalsIgnoreCase(returnUrl)) {
            return fallback;
        }

        String url = returnUrl.trim();

        if (url.startsWith("http://") || url.startsWith("https://")) {
            return fallback;
        }

        if (contextPath != null && !contextPath.isBlank() && url.startsWith(contextPath + "/")) {
            url = url.substring(contextPath.length());
        }

        if (!url.startsWith("/")) {
            return fallback;
        }

        if (!url.startsWith("/admin/orders")
                && !url.startsWith("/admin/order")) {
            return fallback;
        }

        return url;
    }

    /* =========================================================
       FLASH
    ========================================================= */

    private void setFlashSuccess(HttpSession session, String message) {

        if (session != null) {
            session.setAttribute(FLASH_SUCCESS, message);
        }
    }

    private void setFlashError(HttpSession session, String message) {

        if (session != null) {
            session.setAttribute(FLASH_ERROR, message);
        }
    }

    /* =========================================================
       DTO
    ========================================================= */

    private record WorkflowResult(
            boolean success,
            String message,
            String notificationStatus
    ) {

        static WorkflowResult ok(String message, String notificationStatus) {
            return new WorkflowResult(true, message, notificationStatus);
        }

        static WorkflowResult fail(String message) {
            return new WorkflowResult(false, message, "");
        }
    }
}

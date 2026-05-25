package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.AdminOrderDAO;
import com.webshop.app.dao.AdminOrderDAO.OrderTrackingView;
import com.webshop.app.model.Order;
import com.webshop.app.model.ShippingStatus;
import com.webshop.app.model.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/orders")
public class AdminOrderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String VIEW_ORDER_LIST = "/jsp/admin/order/order_list.jsp";
    private static final String VIEW_ORDER_DETAIL = "/jsp/admin/order/order_detail.jsp";

    private static final String FLASH_SUCCESS = "admin_order_success";
    private static final String FLASH_ERROR = "admin_order_error";

    private static final String ACTION_LIST = "list";
    private static final String ACTION_DETAIL = "detail";
    private static final String ACTION_UPDATE_STATUS = "updateStatus";
    private static final String ACTION_UPDATE_SHIPPING_STATUS = "updateShippingStatus";
    private static final String ACTION_UPDATE_SHIPPING_INFO = "updateShippingInfo";

    private final AdminOrderDAO orderDAO = new AdminOrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = normalizeAction(req.getParameter("action"));

        switch (action) {
            case ACTION_DETAIL -> showDetail(req, resp);
            case ACTION_LIST -> showList(req, resp);
            default -> showList(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = normalizeAction(req.getParameter("action"));

        switch (action) {
            case ACTION_UPDATE_STATUS -> handleUpdateStatus(req, resp);
            case ACTION_UPDATE_SHIPPING_STATUS -> handleUpdateShippingStatus(req, resp);
            case ACTION_UPDATE_SHIPPING_INFO -> handleUpdateShippingInfo(req, resp);
            default -> {
                setFlashError(req, "Thao tác quản trị đơn hàng không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/admin/orders");
            }
        }
    }

    /* =========================================================
       GET VIEWS
    ========================================================= */

    private void showList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        consumeFlash(req);

        req.setAttribute("orders", orderDAO.findAll());
        req.setAttribute("pageTitle", "Admin - Quản lý đơn hàng");
        req.setAttribute("activeMenu", "orders");

        req.getRequestDispatcher(VIEW_ORDER_LIST).forward(req, resp);
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int id = parseInt(req.getParameter("id"), -1);

        if (id <= 0) {
            setFlashError(req, "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return;
        }

        Order order = orderDAO.findById(id);

        if (order == null) {
            setFlashError(req, "Không tìm thấy đơn hàng #" + id + ".");
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return;
        }

        List<OrderTrackingView> trackingList = loadTrackingSafely(id);

        consumeFlash(req);

        req.setAttribute("order", order);
        req.setAttribute("trackingList", trackingList);
        req.setAttribute("shippingStatuses", ShippingStatus.values());
        req.setAttribute("pageTitle", "Admin - Chi tiết đơn hàng #" + id);
        req.setAttribute("activeMenu", "orders");

        req.getRequestDispatcher(VIEW_ORDER_DETAIL).forward(req, resp);
    }

    private List<OrderTrackingView> loadTrackingSafely(int orderId) {
        try {
            return orderDAO.findTrackingByOrderId(orderId);
        } catch (RuntimeException e) {
            /*
             * Không để lỗi bảng tracking làm hỏng trang chi tiết đơn hàng.
             * Nếu chưa tạo bảng store_order_tracking, trang vẫn xem được đơn hàng.
             */
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /* =========================================================
       POST HANDLERS
    ========================================================= */

    private void handleUpdateStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int id = parseInt(req.getParameter("id"), -1);
        String status = normalizeStatus(req.getParameter("status"));

        if (id <= 0) {
            setFlashError(req, "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return;
        }

        if (!isAllowedOrderStatus(status)) {
            setFlashError(req, "Trạng thái đơn hàng không hợp lệ.");
            resp.sendRedirect(detailUrl(req, id));
            return;
        }

        Order order = orderDAO.findById(id);

        if (order == null) {
            setFlashError(req, "Không tìm thấy đơn hàng #" + id + ".");
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return;
        }

        String paymentStatus = resolvePaymentStatusByOrderStatus(
                status,
                order.getPaymentStatus()
        );

        boolean updated = orderDAO.updateStatusAndPaymentStatus(id, status, paymentStatus);

        if (updated) {
            setFlashSuccess(req, "Cập nhật trạng thái đơn hàng thành công.");
        } else {
            setFlashError(req, "Không thể cập nhật trạng thái đơn hàng.");
        }

        resp.sendRedirect(detailUrl(req, id));
    }

    private void handleUpdateShippingStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int id = parseInt(req.getParameter("id"), -1);
        String shippingStatus = ShippingStatus.normalizeCode(req.getParameter("shippingStatus"));
        String note = trim(req.getParameter("trackingNote"));
        Integer adminId = getCurrentAdminId(req);

        if (id <= 0) {
            setFlashError(req, "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return;
        }

        if (!isAllowedShippingStatus(shippingStatus)) {
            setFlashError(req, "Trạng thái vận chuyển không hợp lệ.");
            resp.sendRedirect(detailUrl(req, id));
            return;
        }

        Order order = orderDAO.findById(id);

        if (order == null) {
            setFlashError(req, "Không tìm thấy đơn hàng #" + id + ".");
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return;
        }

        boolean updated = orderDAO.updateShippingStatus(
                id,
                shippingStatus,
                adminId,
                note
        );

        if (updated) {
            setFlashSuccess(req, "Cập nhật trạng thái vận chuyển thành công.");
        } else {
            setFlashError(req, "Không thể cập nhật trạng thái vận chuyển.");
        }

        resp.sendRedirect(detailUrl(req, id));
    }

    private void handleUpdateShippingInfo(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int id = parseInt(req.getParameter("id"), -1);
        String shippingProvider = normalizeShippingProvider(req.getParameter("shippingProvider"));
        String shippingCode = trim(req.getParameter("shippingCode"));
        String shippingMethod = normalizeShippingMethod(req.getParameter("shippingMethod"));
        BigDecimal shippingFee = parseVnd(req.getParameter("shippingFee"));

        if (id <= 0) {
            setFlashError(req, "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return;
        }

        Order order = orderDAO.findById(id);

        if (order == null) {
            setFlashError(req, "Không tìm thấy đơn hàng #" + id + ".");
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return;
        }

        boolean updated = orderDAO.updateShippingInfo(
                id,
                shippingProvider,
                shippingCode,
                shippingMethod,
                shippingFee
        );

        if (updated) {
            setFlashSuccess(req, "Cập nhật thông tin vận chuyển thành công.");
        } else {
            setFlashError(req, "Không thể cập nhật thông tin vận chuyển.");
        }

        resp.sendRedirect(detailUrl(req, id));
    }

    /* =========================================================
       BUSINESS RULES
    ========================================================= */

    private String resolvePaymentStatusByOrderStatus(String status, String currentPaymentStatus) {
        String normalizedStatus = normalizeStatus(status);
        String safeCurrentPaymentStatus = normalizePaymentStatus(currentPaymentStatus);

        return switch (normalizedStatus) {
            case "completed" -> "PAID";
            case "cancelled", "canceled" -> "CANCELED";
            default -> safeCurrentPaymentStatus;
        };
    }

    private boolean isAllowedOrderStatus(String status) {
        return switch (normalizeStatus(status)) {
            case "processing", "confirmed", "shipping", "completed", "cancelled" -> true;
            default -> false;
        };
    }

    private boolean isAllowedShippingStatus(String shippingStatus) {
        String normalized = ShippingStatus.normalizeCode(shippingStatus);

        return switch (normalized) {
            case "PENDING_PICKUP", "DELIVERING", "DELIVERED", "FAILED", "CANCELED" -> true;
            default -> false;
        };
    }

    /* =========================================================
       NORMALIZE / PARSE
    ========================================================= */

    private String normalizeAction(String action) {
        String value = trim(action);

        if (value.isEmpty()) {
            return ACTION_LIST;
        }

        return value;
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

        if (value.isEmpty()) {
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

    private String normalizeShippingProvider(String provider) {
        String value = trim(provider).toUpperCase();

        if (value.isEmpty()) {
            return "INTERNAL";
        }

        return switch (value) {
            case "INTERNAL", "GHTK", "GHN", "VIETTEL_POST", "OTHER" -> value;
            default -> "INTERNAL";
        };
    }

    private String normalizeShippingMethod(String method) {
        String value = trim(method).toUpperCase();

        if (value.isEmpty()) {
            return "ECONOMY";
        }

        return switch (value) {
            case "ECONOMY", "FAST", "EXPRESS" -> value;
            default -> "ECONOMY";
        };
    }

    private BigDecimal parseVnd(String raw) {
        String value = trim(raw).replace(",", "").replace(".", "");

        if (value.isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal money = new BigDecimal(value);

            if (money.compareTo(BigDecimal.ZERO) < 0) {
                return BigDecimal.ZERO;
            }

            return money.setScale(0, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(trim(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    /* =========================================================
       SESSION / FLASH
    ========================================================= */

    private Integer getCurrentAdminId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);

        if (session == null) {
            return null;
        }

        Object user = session.getAttribute("user");

        if (user == null) {
            user = session.getAttribute("authUser");
        }

        if (user instanceof User currentUser && currentUser.getId() > 0) {
            return currentUser.getId();
        }

        return null;
    }

    private void setFlashSuccess(HttpServletRequest req, String message) {
        req.getSession().setAttribute(FLASH_SUCCESS, message);
    }

    private void setFlashError(HttpServletRequest req, String message) {
        req.getSession().setAttribute(FLASH_ERROR, message);
    }

    private void consumeFlash(HttpServletRequest req) {
        HttpSession session = req.getSession();

        Object success = session.getAttribute(FLASH_SUCCESS);
        Object error = session.getAttribute(FLASH_ERROR);

        if (success != null) {
            req.setAttribute(FLASH_SUCCESS, success);
            session.removeAttribute(FLASH_SUCCESS);
        }

        if (error != null) {
            req.setAttribute(FLASH_ERROR, error);
            session.removeAttribute(FLASH_ERROR);
        }
    }

    private String detailUrl(HttpServletRequest req, int id) {
        return req.getContextPath() + "/admin/orders?action=detail&id=" + id;
    }
}

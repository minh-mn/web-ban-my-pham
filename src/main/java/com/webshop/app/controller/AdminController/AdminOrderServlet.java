package com.webshop.app.controller.AdminController;

import java.io.IOException;

import com.webshop.app.dao.AdminOrderDAO;
import com.webshop.app.model.Order;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/orders")
public class AdminOrderServlet extends HttpServlet {

    private final AdminOrderDAO orderDAO = new AdminOrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "list";
        }

        switch (action) {

            case "detail": {
                int id = parseInt(req.getParameter("id"), -1);

                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/orders");
                    return;
                }

                Order order = orderDAO.findById(id);

                if (order == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/orders");
                    return;
                }

                req.setAttribute("order", order);

                req.getRequestDispatcher("/jsp/admin/order/order_detail.jsp")
                        .forward(req, resp);
                break;
            }

            default: {
                req.setAttribute("orders", orderDAO.findAll());

                req.getRequestDispatcher("/jsp/admin/order/order_list.jsp")
                        .forward(req, resp);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");

        if ("updateStatus".equals(action)) {
            handleUpdateStatus(req, resp);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/orders");
    }

    private void handleUpdateStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int id = parseInt(req.getParameter("id"), -1);
        String status = normalizeStatus(req.getParameter("status"));

        if (id <= 0 || !isAllowedStatus(status)) {
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return;
        }

        Order order = orderDAO.findById(id);

        if (order == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return;
        }

        /*
         * Khi admin đổi trạng thái đơn hàng:
         * - completed  => đơn xem như đã thanh toán thành công, payment_status = PAID
         * - cancelled  => đơn bị hủy, payment_status = CANCELED
         * - trạng thái khác => giữ nguyên payment_status hiện tại
         *
         * Mục đích:
         * Rank user chỉ tính đơn có payment_status = PAID.
         * Nếu đơn COD đã giao thành công nhưng vẫn PENDING thì rank sẽ không cập nhật.
         */
        String paymentStatus = resolvePaymentStatusByOrderStatus(
                status,
                order.getPaymentStatus()
        );

        orderDAO.updateStatusAndPaymentStatus(id, status, paymentStatus);

        resp.sendRedirect(req.getContextPath() + "/admin/orders?action=detail&id=" + id);
    }

    private String resolvePaymentStatusByOrderStatus(String status, String currentPaymentStatus) {

        String normalizedStatus = normalizeStatus(status);
        String safeCurrentPaymentStatus = normalizePaymentStatus(currentPaymentStatus);

        return switch (normalizedStatus) {
            case "completed" -> "PAID";
            case "cancelled", "canceled" -> "CANCELED";
            default -> safeCurrentPaymentStatus;
        };
    }

    private String normalizeStatus(String status) {

        if (status == null) {
            return "";
        }

        return status.trim().toLowerCase();
    }

    private String normalizePaymentStatus(String paymentStatus) {

        if (paymentStatus == null || paymentStatus.isBlank()) {
            return "PENDING";
        }

        return paymentStatus.trim().toUpperCase();
    }

    private int parseInt(String value, int fallback) {

        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private boolean isAllowedStatus(String status) {

        if (status == null) {
            return false;
        }

        return switch (status) {
            case "processing", "confirmed", "shipping", "completed", "cancelled" -> true;
            default -> false;
        };
    }
}
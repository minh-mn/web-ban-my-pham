package com.mycosmeticshop.controller.AdminController;

import com.mycosmeticshop.dao.AdminOrderDAO;
import com.mycosmeticshop.model.Order;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/orders")
public class AdminOrderServlet extends HttpServlet {

    // DAO dùng để thao tác dữ liệu đơn hàng phía admin
    private final AdminOrderDAO orderDAO = new AdminOrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Lấy action từ URL
        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {

            // =========================
            // XEM CHI TIẾT ĐƠN HÀNG
            // =========================
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

                // Forward tới trang chi tiết đơn hàng
                req.getRequestDispatcher("/jsp/admin/order/order_detail.jsp")
                        .forward(req, resp);
                break;
            }

            // =========================
            // HIỂN THỊ DANH SÁCH ĐƠN HÀNG
            // =========================
            default: {
                req.setAttribute("orders", orderDAO.findAll());

                // Forward tới trang danh sách đơn hàng
                req.getRequestDispatcher("/jsp/admin/order/order_list.jsp")
                        .forward(req, resp);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");

        // =========================
        // CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG
        // =========================
        if ("updateStatus".equals(action)) {
            int id = parseInt(req.getParameter("id"), -1);
            String status = req.getParameter("status");

            // Chỉ update nếu id hợp lệ và status hợp lệ
            if (id > 0 && isAllowedStatus(status)) {
                orderDAO.updateStatus(id, status);
            }

            // Sau khi cập nhật quay lại trang chi tiết đơn hàng
            resp.sendRedirect(req.getContextPath() + "/admin/orders?action=detail&id=" + id);
            return;
        }

        // Nếu action không hợp lệ thì quay về danh sách
        resp.sendRedirect(req.getContextPath() + "/admin/orders");
    }

    /* ======================================================
       HELPER METHODS
       ====================================================== */

    // Parse int an toàn
    private int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return fallback;
        }
    }

    // Kiểm tra status hợp lệ để tránh client gửi dữ liệu rác
    private boolean isAllowedStatus(String status) {
        if (status == null) {
            return false;
        }

        switch (status) {
            case "processing":
            case "confirmed":
            case "shipping":
            case "completed":
            case "cancelled":
                return true;

            default:
                return false;
        }
    }
}
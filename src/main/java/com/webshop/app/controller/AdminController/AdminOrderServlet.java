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

        String action = req.getParameter("action");
        if (action == null) action = "list";

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

                // ✅ forward theo cấu trúc bạn chốt: /jsp/admin/order/...
                req.getRequestDispatcher("/jsp/admin/order/order_detail.jsp")
                   .forward(req, resp);
                break;
            }

            default: { // list
                req.setAttribute("orders", orderDAO.findAll());

                // ✅ forward theo cấu trúc bạn chốt: /jsp/admin/order/...
                req.getRequestDispatcher("/jsp/admin/order/order_list.jsp")
                   .forward(req, resp);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        if ("updateStatus".equals(action)) {
            int id = parseInt(req.getParameter("id"), -1);
            String status = req.getParameter("status");

            if (id > 0 && isAllowedStatus(status)) {
                orderDAO.updateStatus(id, status);
            }

            resp.sendRedirect(req.getContextPath() + "/admin/orders?action=detail&id=" + id);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/orders");
    }

    private int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return fallback;
        }
    }

    // Chặn status rác từ client
    private boolean isAllowedStatus(String s) {
        if (s == null) return false;
        switch (s) {
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

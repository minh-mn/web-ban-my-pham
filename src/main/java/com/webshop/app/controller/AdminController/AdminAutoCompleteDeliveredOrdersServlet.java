package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.model.User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/orders/auto-complete-received")
public class AdminAutoCompleteDeliveredOrdersServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User admin = session == null ? null : (User) session.getAttribute("user");

        if (admin == null || !admin.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        int updated = orderDAO.autoCompleteUnconfirmedDeliveredOrders(7);
        String message = "Đã tự động xác nhận " + updated + " đơn hàng đã giao quá 7 ngày.";
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/admin/orders?success=" + encoded);
    }
}

package com.webshop.app.controller.OrderController;

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

@WebServlet("/orders/cancel")
public class CancelOrderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int orderId = parseInt(request.getParameter("orderId"), -1);
        String reason = normalizeReason(request.getParameter("reason"));

        if (orderId <= 0) {
            redirectWithMessage(request, response, "/orders", "error", "Mã đơn hàng không hợp lệ.");
            return;
        }

        if (reason == null || reason.length() < 5) {
            redirectWithMessage(
                    request,
                    response,
                    "/orders/detail?id=" + orderId,
                    "error",
                    "Vui lòng nhập lý do hủy đơn ít nhất 5 ký tự."
            );
            return;
        }

        boolean cancelled = orderDAO.cancelOrderByUser(orderId, user.getId(), reason);

        if (cancelled) {
            redirectWithMessage(
                    request,
                    response,
                    "/orders/detail?id=" + orderId,
                    "success",
                    "Đơn hàng đã được hủy thành công. Nếu đơn đã thanh toán online, shop sẽ xử lý hoàn tiền theo chính sách."
            );
        } else {
            redirectWithMessage(
                    request,
                    response,
                    "/orders/detail?id=" + orderId,
                    "error",
                    "Đơn hàng này không còn đủ điều kiện hủy. Chỉ có thể hủy đơn trước khi bắt đầu giao hàng."
            );
        }
    }

    private int parseInt(String raw, int defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return null;
        }

        String value = reason.trim();
        return value.length() > 500 ? value.substring(0, 500) : value;
    }

    private void redirectWithMessage(HttpServletRequest request,
                                     HttpServletResponse response,
                                     String path,
                                     String type,
                                     String message) throws IOException {
        String separator = path.contains("?") ? "&" : "?";
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + path + separator + type + "=" + encoded);
    }
}

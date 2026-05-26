package com.webshop.app.controller.OrderController;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.dao.ReturnRequestDAO;
import com.webshop.app.model.Order;
import com.webshop.app.model.ReturnRequest;
import com.webshop.app.model.User;
import com.webshop.app.utils.DBConnection;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/orders/return")
public class ReturnOrderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final OrderDAO orderDAO = new OrderDAO();
    private final ReturnRequestDAO returnRequestDAO = new ReturnRequestDAO();

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
        String refundMethod = normalizeRefundMethod(request.getParameter("refundMethod"));

        if (orderId <= 0) {
            redirectWithMessage(request, response, "/orders", "error", "Mã đơn hàng không hợp lệ.");
            return;
        }

        if (reason == null || reason.length() < 10) {
            redirectWithMessage(
                    request,
                    response,
                    "/orders/detail?id=" + orderId,
                    "error",
                    "Vui lòng nhập lý do hoàn hàng ít nhất 10 ký tự."
            );
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Order order = orderDAO.findById(connection, orderId);

                if (order == null || order.getUserId() != user.getId()) {
                    connection.rollback();
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                if (!order.isReturnable()) {
                    connection.rollback();
                    redirectWithMessage(
                            request,
                            response,
                            "/orders/detail?id=" + orderId,
                            "error",
                            "Đơn hàng này không đủ điều kiện hoàn hàng. Chỉ hỗ trợ hoàn trong 7 ngày sau khi giao thành công và chưa có yêu cầu trước đó."
                    );
                    return;
                }

                if (returnRequestDAO.existsActiveByOrderId(connection, orderId)) {
                    connection.rollback();
                    redirectWithMessage(
                            request,
                            response,
                            "/orders/detail?id=" + orderId,
                            "error",
                            "Đơn hàng này đã có yêu cầu hoàn hàng."
                    );
                    return;
                }

                BigDecimal refundAmount = order.getTotal();

                ReturnRequest returnRequest = new ReturnRequest();
                returnRequest.setOrderId(orderId);
                returnRequest.setUserId(user.getId());
                returnRequest.setReason(reason);
                returnRequest.setStatus("REQUESTED");
                returnRequest.setRefundAmount(refundAmount);
                returnRequest.setRefundMethod(refundMethod);

                returnRequestDAO.create(connection, returnRequest);
                orderDAO.markReturnRequested(connection, orderId, refundAmount, refundMethod);

                connection.commit();

                redirectWithMessage(
                        request,
                        response,
                        "/orders/detail?id=" + orderId,
                        "success",
                        "Yêu cầu hoàn hàng đã được gửi. Shop sẽ kiểm tra và phản hồi trong thời gian sớm nhất."
                );

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new ServletException("ReturnOrderServlet error", e);
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
        return value.length() > 1000 ? value.substring(0, 1000) : value;
    }

    private String normalizeRefundMethod(String refundMethod) {
        if (refundMethod == null || refundMethod.trim().isEmpty()) {
            return "MANUAL";
        }

        String value = refundMethod.trim().toUpperCase();

        return switch (value) {
            case "VNPAY", "BANK_TRANSFER", "CASH", "STORE_CREDIT", "MANUAL" -> value;
            default -> "MANUAL";
        };
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

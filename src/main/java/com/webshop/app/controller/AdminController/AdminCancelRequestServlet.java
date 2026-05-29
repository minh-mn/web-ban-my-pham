package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.CancelRequestDAO;
import com.webshop.app.model.CancelRequest;
import com.webshop.app.model.User;
import com.webshop.app.service.OrderNotificationService;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/cancel-requests")
public class AdminCancelRequestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW_LIST = "/jsp/admin/cancel/cancel_request_list.jsp";

    private final CancelRequestDAO cancelRequestDAO = new CancelRequestDAO();
    private final OrderNotificationService notificationService = new OrderNotificationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        List<CancelRequest> requests = cancelRequestDAO.findAll();

        request.setAttribute("cancelRequests", requests);
        request.setAttribute("pageTitle", "Admin - Yêu cầu hủy đơn");
        request.setAttribute("activeMenu", "cancelRequests");
        request.setAttribute("pageCss", "/assets/css/admin/admin-return.css");
        request.setAttribute("success", request.getParameter("success"));
        request.setAttribute("error", request.getParameter("error"));

        request.getRequestDispatcher(VIEW_LIST).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User admin = session == null ? null : (User) session.getAttribute("user");

        if (admin == null || !admin.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        long id = parseLong(request.getParameter("id"), -1);
        String action = normalizeAction(request.getParameter("action"));
        String adminNote = normalizeNullable(request.getParameter("adminNote"));
        String refundMethod = normalizeRefundMethod(request.getParameter("refundMethod"));
        BigDecimal refundAmount = parseMoney(request.getParameter("refundAmount"));

        if (id <= 0) {
            redirect(request, response, "error", "Mã yêu cầu hủy không hợp lệ.");
            return;
        }

        CancelRequest current = cancelRequestDAO.findById(id);
        if (current == null) {
            redirect(request, response, "error", "Không tìm thấy yêu cầu hủy cần xử lý.");
            return;
        }

        String nextStatus = switch (action) {
            case "approve" -> "APPROVED";
            case "reject" -> "REJECTED";
            default -> null;
        };

        if (nextStatus == null) {
            redirect(request, response, "error", "Thao tác không hợp lệ.");
            return;
        }

        boolean updated = cancelRequestDAO.updateStatus(
                id,
                nextStatus,
                adminNote,
                refundAmount,
                refundMethod,
                admin.getId()
        );

        if (updated) {
            notificationService.notifyCancelProcessedSafely(
                    (int) current.getOrderId(),
                    current.getUserId(),
                    nextStatus,
                    adminNote
            );
            redirect(request, response, "success", "Cập nhật yêu cầu hủy đơn thành công.");
        } else {
            redirect(request, response, "error", "Không tìm thấy yêu cầu hủy cần cập nhật.");
        }
    }

    private long parseLong(String raw, long defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private BigDecimal parseMoney(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String value = raw.replace(".", "").replace(",", "").trim();
        try {
            BigDecimal amount = new BigDecimal(value);
            return amount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : amount;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizeAction(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "";
        }
        return raw.trim().toLowerCase();
    }

    private String normalizeRefundMethod(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String value = raw.trim().toUpperCase();
        return switch (value) {
            case "VNPAY", "BANK_TRANSFER", "CASH", "STORE_CREDIT", "MANUAL" -> value;
            default -> "MANUAL";
        };
    }

    private String normalizeNullable(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String value = raw.trim();
        return value.length() > 1000 ? value.substring(0, 1000) : value;
    }

    private void redirect(HttpServletRequest request,
                          HttpServletResponse response,
                          String type,
                          String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/admin/cancel-requests?" + type + "=" + encoded);
    }
}

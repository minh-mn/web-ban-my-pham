package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.ReturnRequestDAO;
import com.webshop.app.model.ReturnRequest;
import com.webshop.app.model.User;

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

@WebServlet("/admin/returns")
public class AdminReturnRequestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String VIEW_LIST = "/jsp/admin/return/return_list.jsp";

    private final ReturnRequestDAO returnRequestDAO = new ReturnRequestDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        List<ReturnRequest> requests = returnRequestDAO.findAll();

        request.setAttribute("returnRequests", requests);
        request.setAttribute("pageTitle", "Admin - Yêu cầu hoàn hàng");
        request.setAttribute("activeMenu", "returns");
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
            redirect(request, response, "error", "Mã yêu cầu hoàn hàng không hợp lệ.");
            return;
        }

        String nextStatus = switch (action) {
            case "approve" -> "APPROVED";
            case "reject" -> "REJECTED";
            case "returned" -> "RETURNED";
            case "refunded" -> "REFUNDED";
            default -> null;
        };

        if (nextStatus == null) {
            redirect(request, response, "error", "Thao tác không hợp lệ.");
            return;
        }

        boolean updated = returnRequestDAO.updateStatus(
                id,
                nextStatus,
                adminNote,
                refundAmount,
                refundMethod,
                admin.getId()
        );

        if (updated) {
            redirect(request, response, "success", "Cập nhật yêu cầu hoàn hàng thành công.");
        } else {
            redirect(request, response, "error", "Không tìm thấy yêu cầu hoàn hàng cần cập nhật.");
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

        String value = raw.replace(".", "")
                .replace(",", "")
                .trim();

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
        response.sendRedirect(request.getContextPath() + "/admin/returns?" + type + "=" + encoded);
    }
}

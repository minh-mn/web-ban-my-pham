package com.mycosmeticshop.controller.AdminController;

import com.mycosmeticshop.dao.OrderDAO;
import com.mycosmeticshop.model.OrderStatus;
import com.mycosmeticshop.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/admin/order/update-status")
public class AdminOrderUpdateStatusServlet extends HttpServlet {

    // DAO dùng để cập nhật trạng thái đơn hàng
    private final OrderDAO orderDAO = new OrderDAO();

    /* ======================================================
       CSRF CONFIG
       Phải đồng bộ với CsrfFilter và form JSP
       ====================================================== */
    private static final String CSRF_SESSION_KEY = "CSRF_TOKEN";
    private static final String CSRF_PARAM = "csrf_token";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        /* ======================================================
           1. KIỂM TRA ĐĂNG NHẬP + QUYỀN ADMIN
           ====================================================== */
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Chưa đăng nhập -> chuyển về login
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Không phải admin -> chặn truy cập
        if (!user.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return;
        }

        /* ======================================================
           2. KIỂM TRA CSRF
           Nếu filter đã check rồi thì check thêm ở đây vẫn an toàn
           ====================================================== */
        if (!isValidCsrf(req, session)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF invalid");
            return;
        }

        /* ======================================================
           3. ĐỌC PARAM TỪ REQUEST
           ====================================================== */
        int orderId = safeInt(req.getParameter("orderId"), -1);
        String statusRaw = req.getParameter("status");

        if (orderId <= 0 || statusRaw == null || statusRaw.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid params");
            return;
        }

        /* ======================================================
           4. KIỂM TRA STATUS HỢP LỆ
           ====================================================== */
        String normalized = statusRaw.trim();

        if (!OrderStatus.isValidKey(normalized)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid status");
            return;
        }

        /* ======================================================
           5. CẬP NHẬT DATABASE
           ====================================================== */
        try {
            orderDAO.updateStatus(orderId, normalized);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Update failed");
            return;
        }

        /* ======================================================
           6. QUAY VỀ TRANG TRƯỚC ĐÓ
           returnUrl phải được kiểm tra an toàn để tránh redirect lỗi
           ====================================================== */
        String returnUrl = req.getParameter("returnUrl");
        String target = safeReturnUrl(returnUrl, req.getContextPath());

        resp.sendRedirect(req.getContextPath() + target);
    }

    /* ======================================================
       HELPER: KIỂM TRA CSRF TOKEN
       ====================================================== */
    private boolean isValidCsrf(HttpServletRequest req, HttpSession session) {
        if (session == null) {
            return false;
        }

        String token = (String) session.getAttribute(CSRF_SESSION_KEY);
        String sent = req.getParameter(CSRF_PARAM);

        return token != null && sent != null && token.equals(sent);
    }

    /* ======================================================
       HELPER: PARSE INT AN TOÀN
       ====================================================== */
    private int safeInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    /* ======================================================
       HELPER: KIỂM TRA returnUrl AN TOÀN
       - Không cho redirect ra ngoài domain
       - Không cho giá trị null / rỗng / sai format
       - Nếu lỗi thì trả về đường dẫn mặc định
       ====================================================== */
    private String safeReturnUrl(String returnUrl, String contextPath) {
        if (returnUrl == null || returnUrl.isBlank() || "null".equalsIgnoreCase(returnUrl)) {
            return "/orders";
        }

        String url = returnUrl.trim();

        // Chặn open redirect ra ngoài
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return "/orders";
        }

        // Nếu returnUrl chứa sẵn contextPath thì cắt ra
        if (contextPath != null && !contextPath.isBlank() && url.startsWith(contextPath + "/")) {
            url = url.substring(contextPath.length());
        }

        // Chỉ chấp nhận đường dẫn nội bộ bắt đầu bằng "/"
        if (!url.startsWith("/")) {
            return "/orders";
        }

        return url;
    }
}
package com.webshop.app.controller.AuthController;

import java.io.IOException;

import com.webshop.app.service.RememberMeService;
import com.webshop.app.utils.CartUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final RememberMeService rememberMeService = new RememberMeService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);

        /*
         * Issue 132:
         * Trước khi invalidate session, lưu lại giỏ hàng hiện tại xuống database.
         *
         * Lưu ý:
         * - Chỉ lưu khi session đang có CART.
         * - Không gọi getCart() ở đây để tránh tạo cart rỗng rồi ghi đè làm mất
         *   dữ liệu cart_items cũ nếu user đăng nhập nhưng chưa mở/thao tác giỏ hàng.
         */
        if (session != null && session.getAttribute(CartUtil.CART_SESSION_KEY) != null) {
            CartUtil.saveCartForLoggedUser(session);
        }

        // =====================
        // 1) LOGOUT NEW REMEMBER ME
        // =====================
        // Cơ chế mới:
        // - revoke token trong bảng remember_tokens
        // - xóa cookie REMEMBER_ME
        rememberMeService.logoutByRequest(req, resp);

        // =====================
        // 2) CLEAR LEGACY COOKIE ONLY
        // =====================
        // Không còn dùng bảng user_tokens nữa.
        // Chỉ xóa cookie cũ REMEMBER_TOKEN nếu trình duyệt còn lưu.
        clearCookie(resp, "REMEMBER_TOKEN", req.getContextPath());

        // =====================
        // 3) INVALIDATE SESSION
        // =====================
        if (session != null) {
            session.invalidate();
        }

        resp.sendRedirect(req.getContextPath() + "/login");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        doPost(req, resp);
    }

    /**
     * Xóa cookie chắc chắn:
     * - path "/"
     * - path contextPath
     *
     * Lý do: cookie cũ có thể từng được set bằng nhiều path khác nhau.
     */
    private void clearCookie(HttpServletResponse resp, String name, String contextPath) {

        Cookie c1 = new Cookie(name, "");
        c1.setHttpOnly(true);
        c1.setMaxAge(0);
        c1.setPath("/");
        resp.addCookie(c1);

        Cookie c2 = new Cookie(name, "");
        c2.setHttpOnly(true);
        c2.setMaxAge(0);
        c2.setPath((contextPath == null || contextPath.isEmpty()) ? "/" : contextPath);
        resp.addCookie(c2);
    }
}

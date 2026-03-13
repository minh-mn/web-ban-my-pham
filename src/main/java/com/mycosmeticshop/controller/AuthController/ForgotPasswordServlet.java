package com.mycosmeticshop.controller.AuthController;

import com.mycosmeticshop.service.ForgotPasswordService;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/*
 * Servlet xử lý chức năng "Quên mật khẩu"
 * URL truy cập: /forgot-password
 *
 * Chức năng:
 * - Hiển thị form nhập email để reset mật khẩu
 * - Gửi email hướng dẫn đặt lại mật khẩu
 * - Sử dụng flash message để hiển thị thông báo 1 lần
 */
@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    // Tránh warning khi servlet được serialize
    private static final long serialVersionUID = 1L;

    // Service xử lý logic gửi email reset password
    private final ForgotPasswordService forgotPasswordService = new ForgotPasswordService();

    /*
     * Phương thức GET
     * Hiển thị trang nhập email để đặt lại mật khẩu
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập encoding UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // ===== Flash message =====
        // Flash message chỉ hiển thị 1 lần sau khi redirect
        HttpSession session = req.getSession(false);

        if (session != null) {

            // Lấy thông báo thành công
            Object msg = session.getAttribute("flash_message");

            // Lấy thông báo lỗi
            Object err = session.getAttribute("flash_error");

            // Gửi message sang request để hiển thị trên JSP
            if (msg != null) req.setAttribute("message", msg);
            if (err != null) req.setAttribute("error", err);

            // Xóa flash message sau khi đã sử dụng
            session.removeAttribute("flash_message");
            session.removeAttribute("flash_error");
        }

        // Render trang forgot password
        render(req, resp);
    }

    /*
     * Phương thức POST
     * Xử lý yêu cầu gửi email đặt lại mật khẩu
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập encoding
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Lấy email từ form
        String email = req.getParameter("email");

        // Tạo hoặc lấy session để lưu flash message
        HttpSession session = req.getSession(true);

        try {

            // Gọi service để gửi email reset password
            forgotPasswordService.requestReset(email);

            // Thông báo thành công (không tiết lộ email có tồn tại hay không)
            session.setAttribute(
                    "flash_message",
                    "Nếu email tồn tại trong hệ thống, chúng tôi đã gửi hướng dẫn đặt lại mật khẩu."
            );

        } catch (Exception e) {

            // In lỗi ra console để debug
            e.printStackTrace();

            // Thông báo lỗi cho người dùng
            session.setAttribute(
                    "flash_error",
                    "Không thể gửi email lúc này. Vui lòng thử lại sau."
            );
        }

        // Redirect về trang forgot-password
        // (PRG Pattern: Post → Redirect → Get)
        resp.sendRedirect(req.getContextPath() + "/forgot-password");
    }

    /*
     * Hàm render giao diện
     * Dùng base layout JSP để hiển thị nội dung
     */
    private void render(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập thông tin trang
        req.setAttribute("pageTitle", "MyCosmetic | Quên mật khẩu");
        req.setAttribute("pageCss", "login.css");
        req.setAttribute("pageContent", "/jsp/auth/forgot_password.jsp");

        // Forward tới layout chính
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}
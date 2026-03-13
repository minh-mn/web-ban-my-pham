package com.mycosmeticshop.controller.AuthController;

import com.mycosmeticshop.model.User;
import com.mycosmeticshop.service.ForgotPasswordService;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/*
 * Servlet xử lý chức năng đặt lại mật khẩu
 * URL truy cập: /reset-password
 *
 * Chức năng:
 * - Kiểm tra token reset password
 * - Hiển thị form nhập mật khẩu mới
 * - Xử lý cập nhật mật khẩu mới khi token hợp lệ
 */
@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Service xử lý logic quên mật khẩu và reset mật khẩu
    private final ForgotPasswordService forgotPasswordService = new ForgotPasswordService();

    /*
     * Phương thức GET
     * Hiển thị form đặt lại mật khẩu nếu token hợp lệ
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập encoding UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Lấy token từ URL
        String token = req.getParameter("token");

        // Kiểm tra token và lấy user tương ứng
        User u = forgotPasswordService.validateTokenAndGetUser(token);

        // Nếu token không hợp lệ hoặc đã hết hạn
        if (u == null) {
            /*
             * token invalid -> JSP sẽ rơi vào nhánh token rỗng / không hợp lệ
             * và hiển thị thông báo lỗi cho người dùng
             */
            req.setAttribute("error", "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            req.setAttribute("token", null);
            render(req, resp);
            return;
        }

        // Nếu token hợp lệ, truyền token sang JSP để submit lại khi POST
        req.setAttribute("token", token);
        render(req, resp);
    }

    /*
     * Phương thức POST
     * Xử lý cập nhật mật khẩu mới
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập encoding UTF-8
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Lấy dữ liệu từ form
        String token = req.getParameter("token");
        String newPass = req.getParameter("newPassword");
        String confirm = req.getParameter("confirmPassword");

        // ===== 1) VALIDATE TOKEN TRƯỚC =====
        // Tránh trường hợp người dùng gửi token rác hoặc token đã hết hạn
        User u = forgotPasswordService.validateTokenAndGetUser(token);

        if (u == null) {
            req.setAttribute("error", "Token không hợp lệ hoặc đã hết hạn. Vui lòng yêu cầu link mới.");
            req.setAttribute("token", null);
            render(req, resp);
            return;
        }

        // ===== 2) VALIDATE MẬT KHẨU MỚI =====
        String np = (newPass != null) ? newPass.trim() : "";

        // Kiểm tra độ dài tối thiểu của mật khẩu
        if (np.length() < 6) {
            req.setAttribute("error", "Mật khẩu tối thiểu 6 ký tự.");
            req.setAttribute("token", token);
            render(req, resp);
            return;
        }

        // Kiểm tra xác nhận mật khẩu
        if (confirm == null || !np.equals(confirm)) {
            req.setAttribute("error", "Xác nhận mật khẩu không khớp.");
            req.setAttribute("token", token);
            render(req, resp);
            return;
        }

        try {
            // Thực hiện đặt lại mật khẩu
            forgotPasswordService.resetPassword(token, np);

            /*
             * Redirect về trang login và báo thành công
             * Áp dụng PRG Pattern: Post -> Redirect -> Get
             */
            resp.sendRedirect(req.getContextPath() + "/login?reset=success");

        } catch (Exception e) {

            // In lỗi ra console để debug
            e.printStackTrace();

            // Thông báo lỗi cho người dùng
            req.setAttribute("error", "Không thể đặt lại mật khẩu lúc này. Vui lòng thử lại.");
            req.setAttribute("token", token);
            render(req, resp);
        }
    }

    /*
     * Hàm render giao diện reset password
     * Sử dụng base layout JSP để hiển thị nội dung
     */
    private void render(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập thông tin trang
        req.setAttribute("pageTitle", "MyCosmetic | Đặt lại mật khẩu");

        /*
         * Đồng bộ chuẩn auth:
         * nếu base.jsp load CSS theo dạng
         * contextPath + /assets/css/${pageCss}
         * thì chỉ cần truyền tên file CSS
         */
        req.setAttribute("pageCss", "login.css");

        // Chỉ định nội dung trang
        req.setAttribute("pageContent", "/jsp/auth/reset_password.jsp");

        // Forward tới layout chính
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}
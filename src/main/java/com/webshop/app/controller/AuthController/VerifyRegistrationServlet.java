package com.webshop.app.controller.AuthController;

import com.webshop.app.dao.UserDAO;
import com.webshop.app.model.User;
import com.webshop.app.utils.CartUtil;
import com.webshop.app.utils.PasswordUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@WebServlet("/verify-registration")
public class VerifyRegistrationServlet extends HttpServlet {

    // Hỗ trợ phương thức GET - Ngăn chặn triệt để lỗi 405 khi người dùng truy cập hoặc reload trang trực tiếp
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("pageTitle", "Xác thực OTP");
        req.setAttribute("pageContent", "/jsp/auth/verify-otp.jsp");
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    // Xử lý xác thực mã OTP qua phương thức POST
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String inputOtp = req.getParameter("otp_input");
        HttpSession session = req.getSession();

        String serverOtp = (String) session.getAttribute("REGISTER_OTP");
        User pendingUser = (User) session.getAttribute("pendingUser");
        String pendingProvider = (String) session.getAttribute("pendingProvider");
        String pendingSocialId = (String) session.getAttribute("pendingSocialId");

        // 1. Kiểm tra mã OTP hợp lệ
        if (serverOtp == null || !serverOtp.equals(inputOtp)) {
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Mã OTP xác thực không chính xác!\"}");
            return;
        }

        try {

            UserDAO userDAO = new UserDAO();

            User fullUser;

            if (pendingProvider != null
                    && pendingSocialId != null) {

                boolean inserted =
                        userDAO.saveSocialUser(
                                pendingUser,
                                pendingProvider,
                                pendingSocialId
                        );

                if (!inserted) {

                    resp.getWriter().write("""
            {
                "status":"error",
                "message":"Không thể lưu tài khoản mạng xã hội!"
            }
            """);

                    return;
                }

                fullUser =
                        userDAO.findBySocialId(
                                pendingProvider,
                                pendingSocialId
                        );

            } else {

                boolean inserted = userDAO.insert(pendingUser);

                if (!inserted) {

                    resp.getWriter().write("""
            {
                "status":"error",
                "message":"Không thể lưu tài khoản!"
            }
            """);

                    return;
                }

                fullUser =
                        userDAO.findByEmail(
                                pendingUser.getEmail()
                        );
            }

            if (fullUser == null) {

                resp.getWriter().write("""
        {
            "status":"error",
            "message":"Không tìm thấy tài khoản sau khi lưu!"
        }
        """);

                return;
            }

            session.setAttribute(
                    "user",
                    fullUser
            );

            try {

                CartUtil.mergeDatabaseCartIntoSession(
                        session,
                        fullUser.getId()
                );

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            session.removeAttribute("REGISTER_OTP");
            session.removeAttribute("pendingUser");
            session.removeAttribute("pendingProvider");
            session.removeAttribute("pendingSocialId");
            session.removeAttribute("OTP_TIME");

            resp.getWriter().write("""
    {
        "status":"success",
        "message":"Đăng ký thành công!",
        "redirectUrl":"/"
    }
    """);

        } catch (Exception e) {

            e.printStackTrace();

            resp.getWriter().write("""
    {
        "status":"error",
        "message":"Lỗi cơ sở dữ liệu!"
    }
    """);
        }
    }
}

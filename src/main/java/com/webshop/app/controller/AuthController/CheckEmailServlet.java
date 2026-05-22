package com.webshop.app.controller.AuthController;

import java.io.IOException;

import com.webshop.app.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.webshop.app.dao.UserDAO;

@WebServlet("/check-email")
public class CheckEmailServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập kiểu trả về là JSON
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String email = req.getParameter("email");
        boolean exists = false;

        if (email != null && !email.isBlank()) {
            // Sử dụng hàm findByEmail ĐÃ CÓ SẴN trong UserDAO của bạn
            User user = userDAO.findByEmail(email.trim());
            if (user != null) {
                exists = true; // Nếu tìm thấy user nghĩa là email đã tồn tại
            }
        }

        // Trả về kết quả JSON để Javascript nhận diện (ví dụ: {"exists": true})
        resp.getWriter().write("{\"exists\": " + exists + "}");
    }
}
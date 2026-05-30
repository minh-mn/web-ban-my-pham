package com.webshop.app.controller.NotificationController;

import com.webshop.app.dao.NotificationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("pageTitle", "Tất cả thông báo của bạn");
        request.setAttribute("pageContent", "/jsp/notifications/notifications-list.jsp");

        // Forward tới base.jsp để giữ nguyên layout tổng thể (Header, Footer, Thanh điều hướng)
        request.getRequestDispatcher("/jsp/common/base.jsp").forward(request, response);
    }
}
package com.webshop.app.controller.NotificationController;

import com.webshop.app.dao.NotificationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/notifications/read")
public class NotificationReadServlet extends HttpServlet {
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String redirectUrl = request.getParameter("redirect");

        // 1. Cập nhật trạng thái thông báo thành đã đọc trong DB
        if (idStr != null && !idStr.isEmpty()) {
            try {
                long id = Long.parseLong(idStr);
                notificationDAO.markAsRead(id);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // Kiểm tra an toàn link redirect, nếu rỗng thì về trang chủ
        if (redirectUrl == null || redirectUrl.trim().isEmpty()) {
            redirectUrl = "/";
        }

        // 2. Chuyển hướng người dùng tới trang đích thực tế
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
}

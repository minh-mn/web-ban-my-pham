package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.NotificationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/notifications")
public class AdminNotificationServlet extends HttpServlet {
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "new":
                request.getRequestDispatcher("/jsp/admin/notification/notification-form.jsp").forward(request, response);
                break;
            case "list":
            default:
                request.setAttribute("systemNotifications", notificationDAO.getSystemBroadcastHistory());
                request.getRequestDispatcher("/jsp/admin/notification/notification-list.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("sendBulk".equals(action)) {
            String title = request.getParameter("title");
            String message = request.getParameter("message");
            String type = request.getParameter("type");
            String targetUrl = request.getParameter("targetUrl");

            boolean isSent = notificationDAO.broadcastNotification(type, title, message, targetUrl);
            if (isSent) {
                response.sendRedirect(request.getContextPath() + "/admin/notifications?msg=success");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/notifications?msg=error");
            }
        }
    }
}
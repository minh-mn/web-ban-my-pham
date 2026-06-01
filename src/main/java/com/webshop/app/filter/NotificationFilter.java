package com.webshop.app.filter;

import com.webshop.app.dao.NotificationDAO;
import com.webshop.app.model.Notification;
import com.webshop.app.model.User;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebFilter("/*")
public class NotificationFilter implements Filter {

    private static final int HEADER_NOTIFICATION_LIMIT = 5;

    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (shouldSkip(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            chain.doFilter(request, response);
            return;
        }

        User currentUser = getCurrentUser(session);

        if (currentUser == null || currentUser.getId() <= 0) {
            chain.doFilter(request, response);
            return;
        }

        try {
            if (currentUser.isAdmin()) {
                loadAdminNotifications(request);
            } else {
                loadUserNotifications(request, currentUser.getId());
            }
        } catch (Exception e) {
            /*
             * Không để lỗi notification làm sập toàn bộ request.
             * Ví dụ: chưa chạy migration notifications hoặc DB tạm lỗi.
             */
            setEmptyNotificationAttributes(request, currentUser.isAdmin());
            e.printStackTrace();
        }

        chain.doFilter(request, response);
    }

    /* =========================================================
       LOAD USER / ADMIN NOTIFICATIONS
    ========================================================= */

    private void loadUserNotifications(ServletRequest request, int userId) {
        int unreadCount = notificationDAO.countUnreadByUserId(userId);
        List<Notification> notifications = notificationDAO.findLatestByUser(
                userId,
                HEADER_NOTIFICATION_LIMIT
        );

        request.setAttribute("unreadNotificationCount", unreadCount);
        request.setAttribute("latestNotifications", notifications);

        /*
         * Giữ tương thích với base.jsp/header.jsp cũ nếu đang dùng tên này.
         */
        request.setAttribute("unreadCount", unreadCount);
        request.setAttribute("notifications", notifications);
    }

    private void loadAdminNotifications(ServletRequest request) {
        int unreadCount = notificationDAO.countUnreadByAdmin();
        List<Notification> notifications = notificationDAO.findLatestByAdmin(
                HEADER_NOTIFICATION_LIMIT
        );

        request.setAttribute("adminUnreadNotificationCount", unreadCount);
        request.setAttribute("adminLatestNotifications", notifications);

        /*
         * Giữ thêm tên ngắn để JSP admin dễ dùng.
         */
        request.setAttribute("adminUnreadCount", unreadCount);
        request.setAttribute("adminNotifications", notifications);
    }

    private void setEmptyNotificationAttributes(ServletRequest request, boolean admin) {
        if (admin) {
            request.setAttribute("adminUnreadNotificationCount", 0);
            request.setAttribute("adminLatestNotifications", Collections.emptyList());
            request.setAttribute("adminUnreadCount", 0);
            request.setAttribute("adminNotifications", Collections.emptyList());
        } else {
            request.setAttribute("unreadNotificationCount", 0);
            request.setAttribute("latestNotifications", Collections.emptyList());
            request.setAttribute("unreadCount", 0);
            request.setAttribute("notifications", Collections.emptyList());
        }
    }

    /* =========================================================
       SESSION / SKIP HELPERS
    ========================================================= */

    private User getCurrentUser(HttpSession session) {
        Object user = session.getAttribute("user");

        if (user == null) {
            user = session.getAttribute("authUser");
        }

        if (user instanceof User currentUser) {
            return currentUser;
        }

        return null;
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri == null || uri.isBlank()) {
            return true;
        }

        String lowerUri = uri.toLowerCase();

        return lowerUri.contains("/assets/")
                || lowerUri.contains("/uploads/")
                || lowerUri.contains("/favicon")
                || lowerUri.endsWith(".css")
                || lowerUri.endsWith(".js")
                || lowerUri.endsWith(".png")
                || lowerUri.endsWith(".jpg")
                || lowerUri.endsWith(".jpeg")
                || lowerUri.endsWith(".gif")
                || lowerUri.endsWith(".svg")
                || lowerUri.endsWith(".webp")
                || lowerUri.endsWith(".ico")
                || lowerUri.endsWith(".woff")
                || lowerUri.endsWith(".woff2")
                || lowerUri.endsWith(".ttf")
                || lowerUri.endsWith(".map");
    }
}

package com.webshop.app.filter;

import com.webshop.app.dao.NotificationDAO;
import com.webshop.app.model.Notification;
import com.webshop.app.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebFilter("/*") // Áp dụng cho toàn bộ đường dẫn trên trang web
public class GlobalNotificationFilter implements Filter {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);

        // Bỏ qua lọc cho các file tĩnh (CSS, JS, ảnh) để tăng hiệu suất
        String uri = req.getRequestURI();
        if (uri.startsWith(req.getContextPath() + "/assets") || uri.contains(".")) {
            chain.doFilter(request, response);
            return;
        }

        User currentUser = null;
        if (session != null) {
            currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                currentUser = (User) session.getAttribute("authUser");
            }
        }

        if (currentUser != null) {
            // Lấy thông báo cho User (Khách hàng)
            if (!currentUser.isAdmin()) {
                List<Notification> latestNotifications = notificationDAO.findLatestByUser(currentUser.getId(), 5);
                int unreadCount = notificationDAO.countUnreadByUserId(currentUser.getId());

                req.setAttribute("headerNotifications", latestNotifications);
                req.setAttribute("unreadNotificationCount", unreadCount);
                req.setAttribute("unreadCount", unreadCount); // Alias
            }
            // Lấy thông báo cho Admin
            else {
                List<Notification> latestAdminNotifications = notificationDAO.findLatestByAdmin(5);
                int adminUnreadCount = notificationDAO.countUnreadByAdmin();

                // Biến này được truyền xuống base.jsp để hiện lên Icon Chuông
                req.setAttribute("headerNotifications", latestAdminNotifications);
                req.setAttribute("unreadNotificationCount", adminUnreadCount);
                req.setAttribute("unreadCount", adminUnreadCount); // Alias cho tương thích base.jsp
            }
        }

        chain.doFilter(request, response);
    }
}
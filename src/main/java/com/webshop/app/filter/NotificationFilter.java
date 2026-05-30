package com.webshop.app.filter;

import com.webshop.app.dao.NotificationDAO;
import com.webshop.app.model.Notification;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebFilter("/*") 
public class NotificationFilter implements Filter {
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();

        // Bỏ qua các file tĩnh (css, js, ảnh) và trang quản trị admin để tối ưu hiệu năng
        if (!uri.contains("/assets/") && !uri.contains("/admin/")) {
            HttpSession session = httpRequest.getServerName() != null ? httpRequest.getSession(false) : null;

            if (session != null && session.getAttribute("user") != null) {
                try {
                    // ⚠️ Đổi chữ 'Object' thành class Model User thực tế của bạn (Ví dụ: Account hoặc User)
                    Object loggedInUser = session.getAttribute("user");

                    // Lấy userId từ đối tượng đăng nhập (Sửa hàm getId() theo đúng model của bạn)
                    int userId = (Integer) loggedInUser.getClass().getMethod("getId").invoke(loggedInUser);

                    // Nạp dữ liệu tự động cho base.jsp sử dụng
                    int unreadCount = notificationDAO.getUnreadCount(userId);
                    List<Notification> notifications = notificationDAO.getNotificationsByUserId(userId);

                    request.setAttribute("unreadCount", unreadCount);
                    request.setAttribute("notifications", notifications);
                } catch (Exception e) {
                    // Log lỗi nếu cần thiết hoặc bỏ qua nếu ép kiểu sai tên hàm
                }
            }
        }
        chain.doFilter(request, response);
    }
}
package com.webshop.app.controller.NotificationController;

import com.webshop.app.dao.NotificationDAO;
import com.webshop.app.model.Notification;
import com.webshop.app.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final int MAX_PAGE_SIZE = 50;

    private static final String CSRF_SESSION_KEY = "CSRF_TOKEN";
    private static final String CSRF_PARAM = "csrf_token";

    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        User currentUser = getCurrentUser(request.getSession(false));

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (currentUser.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/admin/notifications");
            return;
        }

        String action = trim(request.getParameter("action"));

        if ("read".equalsIgnoreCase(action)) {
            markOneAsRead(request, response, currentUser);
            return;
        }

        showNotificationList(request, response, currentUser);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        User currentUser = getCurrentUser(request.getSession(false));

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (currentUser.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/admin/notifications");
            return;
        }

        if (!isValidCsrf(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF invalid");
            return;
        }

        String action = trim(request.getParameter("action"));

        if ("markAllRead".equalsIgnoreCase(action)) {
            notificationDAO.markAllAsReadByUser(currentUser.getId());
        } else if ("markRead".equalsIgnoreCase(action)) {
            long id = parseLong(request.getParameter("id"), -1);
            if (id > 0) {
                notificationDAO.markRead(id, currentUser.getId());
            }
        }

        response.sendRedirect(request.getContextPath() + "/notifications");
    }

    /* =========================================================
       VIEW
    ========================================================= */

    private void showNotificationList(HttpServletRequest request,
                                      HttpServletResponse response,
                                      User currentUser)
            throws ServletException, IOException {

        int page = parseInt(request.getParameter("page"), 1);
        int pageSize = parseInt(request.getParameter("pageSize"), DEFAULT_PAGE_SIZE);

        page = Math.max(page, 1);
        pageSize = Math.max(1, Math.min(pageSize, MAX_PAGE_SIZE));

        int totalNotifications = notificationDAO.countByUser(currentUser.getId());
        int totalPages = Math.max(1, (int) Math.ceil(totalNotifications * 1.0 / pageSize));

        if (page > totalPages) {
            page = totalPages;
        }

        List<Notification> notificationList = notificationDAO.findAllByUser(
                currentUser.getId(),
                page,
                pageSize
        );

        int unreadCount = notificationDAO.countUnreadByUserId(currentUser.getId());

        request.setAttribute("notificationList", notificationList);
        request.setAttribute("notifications", notificationList);
        request.setAttribute("unreadNotificationCount", unreadCount);
        request.setAttribute("unreadCount", unreadCount);

        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalNotifications", totalNotifications);

        request.setAttribute("pageTitle", "MyCosmetic | Thông báo");
        request.setAttribute("pageCss", "/assets/css/notification.css");
        request.setAttribute("pageContent", "/jsp/notifications/notifications-list.jsp");

        request.getRequestDispatcher("/jsp/common/base.jsp").forward(request, response);
    }

    private void markOneAsRead(HttpServletRequest request,
                               HttpServletResponse response,
                               User currentUser)
            throws IOException {

        long id = parseLong(request.getParameter("id"), -1);

        if (id > 0) {
            notificationDAO.markRead(id, currentUser.getId());
        }

        String redirect = safeReturnUrl(
                request.getParameter("returnUrl"),
                request.getContextPath()
        );

        response.sendRedirect(request.getContextPath() + redirect);
    }

    /* =========================================================
       HELPERS
    ========================================================= */

    private User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object user = session.getAttribute("user");

        if (user == null) {
            user = session.getAttribute("authUser");
        }

        if (user instanceof User currentUser) {
            return currentUser;
        }

        return null;
    }

    private boolean isValidCsrf(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return false;
        }

        Object token = session.getAttribute(CSRF_SESSION_KEY);
        String sent = request.getParameter(CSRF_PARAM);

        return token != null && sent != null && token.toString().equals(sent);
    }

    private String safeReturnUrl(String returnUrl, String contextPath) {
        String fallback = "/notifications";

        if (returnUrl == null || returnUrl.isBlank()) {
            return fallback;
        }

        String url = returnUrl.trim();

        if (url.startsWith("http://") || url.startsWith("https://")) {
            return fallback;
        }

        if (contextPath != null && !contextPath.isBlank() && url.startsWith(contextPath + "/")) {
            url = url.substring(contextPath.length());
        }

        if (!url.startsWith("/")) {
            return fallback;
        }

        return url;
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(trim(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(trim(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}

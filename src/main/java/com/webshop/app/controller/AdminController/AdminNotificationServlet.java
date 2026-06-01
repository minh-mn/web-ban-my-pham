package com.webshop.app.controller.AdminController;

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

@WebServlet("/admin/notifications")
public class AdminNotificationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private static final String FLASH_SUCCESS = "admin_notification_success";
    private static final String FLASH_ERROR = "admin_notification_error";

    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        User admin = getCurrentUser(request.getSession(false));

        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (!admin.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return;
        }

        String action = trim(request.getParameter("action"));

        switch (action) {
            case "new" -> showCreateForm(request, response);
            case "read" -> markOneAsRead(request, response);
            case "markAllRead" -> markAllAsRead(request, response);
            case "list", "" -> showList(request, response);
            default -> showList(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        User admin = getCurrentUser(request.getSession(false));

        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (!admin.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return;
        }

        String action = trim(request.getParameter("action"));

        switch (action) {
            case "sendBulk" -> sendBulkNotification(request, response);
            case "markRead" -> {
                long id = parseLong(request.getParameter("id"), -1);
                if (id > 0) {
                    notificationDAO.markAdminRead(id);
                }
                response.sendRedirect(request.getContextPath() + "/admin/notifications");
            }
            case "markAllRead" -> {
                notificationDAO.markAllAsReadByAdmin();
                setFlashSuccess(request, "Đã đánh dấu tất cả thông báo admin là đã đọc.");
                response.sendRedirect(request.getContextPath() + "/admin/notifications");
            }
            default -> response.sendRedirect(request.getContextPath() + "/admin/notifications");
        }
    }

    /* =========================================================
       VIEW
    ========================================================= */

    private void showList(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        pullFlash(request);

        int page = parseInt(request.getParameter("page"), 1);
        int pageSize = parseInt(request.getParameter("pageSize"), DEFAULT_PAGE_SIZE);

        page = Math.max(page, 1);
        pageSize = Math.max(1, Math.min(pageSize, MAX_PAGE_SIZE));

        int totalNotifications = notificationDAO.countByAdmin();
        int totalPages = Math.max(1, (int) Math.ceil(totalNotifications * 1.0 / pageSize));

        if (page > totalPages) {
            page = totalPages;
        }

        List<Notification> notificationList = notificationDAO.findAllByAdmin(page, pageSize);
        int unreadCount = notificationDAO.countUnreadByAdmin();

        /*
         * Dữ liệu chính cho trang danh sách thông báo admin.
         */
        request.setAttribute("notificationList", notificationList);
        request.setAttribute("adminNotificationList", notificationList);
        request.setAttribute("adminUnreadNotificationCount", unreadCount);
        request.setAttribute("adminUnreadCount", unreadCount);

        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalNotifications", totalNotifications);

        /*
         * Giữ lại attribute cũ để notification_list.jsp hiện tại không bị lỗi.
         */
        request.setAttribute("systemNotifications", notificationDAO.getSystemBroadcastHistory());

        request.setAttribute("pageTitle", "ADMIN | Thông báo");
        request.setAttribute("activeMenu", "notifications");
        request.setAttribute("pageCss", "/assets/css/admin/admin-notification.css");

        request.getRequestDispatcher("/jsp/admin/notification/notification_list.jsp")
                .forward(request, response);
    }

    private void showCreateForm(HttpServletRequest request,
                                HttpServletResponse response)
            throws ServletException, IOException {

        pullFlash(request);

        request.setAttribute("pageTitle", "ADMIN | Gửi thông báo");
        request.setAttribute("activeMenu", "notifications");
        request.setAttribute("pageCss", "/assets/css/admin/admin-notification.css");

        request.getRequestDispatcher("/jsp/admin/notification/notification_form.jsp")
                .forward(request, response);
    }

    /* =========================================================
       ACTIONS
    ========================================================= */

    private void sendBulkNotification(HttpServletRequest request,
                                      HttpServletResponse response)
            throws IOException {

        String title = trim(request.getParameter("title"));
        String message = trim(request.getParameter("message"));
        String type = trim(request.getParameter("type"));
        String targetUrl = trim(request.getParameter("targetUrl"));

        if (title.isBlank() || message.isBlank()) {
            setFlashError(request, "Vui lòng nhập đầy đủ tiêu đề và nội dung thông báo.");
            response.sendRedirect(request.getContextPath() + "/admin/notifications?action=new");
            return;
        }

        if (type.isBlank()) {
            type = "SYSTEM";
        }

        if (targetUrl.isBlank()) {
            targetUrl = "/notifications";
        }

        try {
            boolean sent = notificationDAO.broadcastNotification(
                    type,
                    title,
                    message,
                    targetUrl
            );

            if (sent) {
                setFlashSuccess(request, "Đã gửi thông báo hàng loạt cho khách hàng.");
                response.sendRedirect(request.getContextPath() + "/admin/notifications?msg=success");
            } else {
                setFlashError(request, "Không thể gửi thông báo hàng loạt.");
                response.sendRedirect(request.getContextPath() + "/admin/notifications?action=new&msg=error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            setFlashError(request, "Gửi thông báo thất bại. Vui lòng kiểm tra database notifications.");
            response.sendRedirect(request.getContextPath() + "/admin/notifications?action=new&msg=error");
        }
    }

    private void markOneAsRead(HttpServletRequest request,
                               HttpServletResponse response)
            throws IOException {

        long id = parseLong(request.getParameter("id"), -1);

        if (id > 0) {
            notificationDAO.markAdminRead(id);
        }

        String returnUrl = safeReturnUrl(request.getParameter("returnUrl"));
        response.sendRedirect(request.getContextPath() + returnUrl);
    }

    private void markAllAsRead(HttpServletRequest request,
                               HttpServletResponse response)
            throws IOException {

        notificationDAO.markAllAsReadByAdmin();
        setFlashSuccess(request, "Đã đánh dấu tất cả thông báo admin là đã đọc.");
        response.sendRedirect(request.getContextPath() + "/admin/notifications");
    }

    /* =========================================================
       FLASH
    ========================================================= */

    private void setFlashSuccess(HttpServletRequest request, String message) {
        HttpSession session = request.getSession();

        if (session != null) {
            session.setAttribute(FLASH_SUCCESS, message);
        }
    }

    private void setFlashError(HttpServletRequest request, String message) {
        HttpSession session = request.getSession();

        if (session != null) {
            session.setAttribute(FLASH_ERROR, message);
        }
    }

    private void pullFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        Object success = session.getAttribute(FLASH_SUCCESS);
        Object error = session.getAttribute(FLASH_ERROR);

        if (success != null) {
            request.setAttribute(FLASH_SUCCESS, success);
            session.removeAttribute(FLASH_SUCCESS);
        }

        if (error != null) {
            request.setAttribute(FLASH_ERROR, error);
            session.removeAttribute(FLASH_ERROR);
        }
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

    private String safeReturnUrl(String returnUrl) {
        String fallback = "/admin/notifications";

        if (returnUrl == null || returnUrl.isBlank()) {
            return fallback;
        }

        String url = returnUrl.trim();

        if (url.startsWith("http://") || url.startsWith("https://")) {
            return fallback;
        }

        if (!url.startsWith("/")) {
            return fallback;
        }

        if (!url.startsWith("/admin/notifications")
                && !url.startsWith("/admin/orders")
                && !url.startsWith("/admin/reviews")) {
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

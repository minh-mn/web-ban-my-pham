package com.webshop.app.controller.AccountController;

import java.io.IOException;

import com.webshop.app.dao.SearchHistoryDAO;
import com.webshop.app.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet({
        "/account/search-history/delete",
        "/search-history/delete"
})
public class SearchHistoryDeleteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final SearchHistoryDAO searchHistoryDAO = new SearchHistoryDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        handleDelete(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        /*
         * Hỗ trợ GET để tương thích nếu JSP hiện đang dùng thẻ <a>.
         * Khuyến nghị dùng form POST kèm csrf_token nếu project đang bật CsrfFilter.
         */
        handleDelete(req, resp);
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User currentUser = getCurrentUser(session);

        if (currentUser == null || currentUser.getId() <= 0) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/account");
            return;
        }

        long historyId = parseLong(firstNotBlank(
                req.getParameter("id"),
                req.getParameter("historyId"),
                req.getParameter("searchHistoryId")
        ), -1L);

        if (historyId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/account?tab=search-history&deleteInvalid=1");
            return;
        }

        boolean deleted = searchHistoryDAO.deleteByIdAndUserId(historyId, currentUser.getId());

        if (deleted) {
            resp.sendRedirect(req.getContextPath() + "/account?tab=search-history&deleteSuccess=1");
        } else {
            resp.sendRedirect(req.getContextPath() + "/account?tab=search-history&deleteFailed=1");
        }
    }

    private User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object rawUser = session.getAttribute("user");

        if (rawUser instanceof User) {
            return (User) rawUser;
        }

        return null;
    }

    private String firstNotBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return null;
    }

    private long parseLong(String raw, long def) {
        try {
            return Long.parseLong(raw);
        } catch (Exception e) {
            return def;
        }
    }
}
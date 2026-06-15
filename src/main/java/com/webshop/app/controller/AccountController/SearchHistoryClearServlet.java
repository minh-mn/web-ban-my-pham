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
        "/account/search-history/clear",
        "/search-history/clear"
})
public class SearchHistoryClearServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final SearchHistoryDAO searchHistoryDAO = new SearchHistoryDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        handleClear(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        /*
         * Hỗ trợ GET để tương thích nếu JSP hiện đang dùng thẻ <a>.
         * Khuyến nghị dùng form POST kèm csrf_token nếu project đang bật CsrfFilter.
         */
        handleClear(req, resp);
    }

    private void handleClear(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User currentUser = getCurrentUser(session);

        if (currentUser == null || currentUser.getId() <= 0) {
            String target = req.getServletPath() != null && req.getServletPath().startsWith("/search-history")
                    ? "/search-history"
                    : "/account";
            resp.sendRedirect(req.getContextPath() + "/login?redirect=" + target);
            return;
        }

        int deletedCount = searchHistoryDAO.clearByUserId(currentUser.getId());

        if (deletedCount > 0) {
            resp.sendRedirect(accountActivityUrl(req, "clearSuccess=1"));
        } else {
            resp.sendRedirect(accountActivityUrl(req, "clearEmpty=1"));
        }
    }

    private String accountActivityUrl(HttpServletRequest req, String query) {
        String servletPath = req.getServletPath();

        if ("/search-history/clear".equals(servletPath)
                || "/search-history/delete".equals(servletPath)) {
            return req.getContextPath() + "/search-history?" + query;
        }

        return req.getContextPath() + "/account?tab=activity&" + query + "#account-activity";
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
}
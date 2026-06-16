package com.webshop.app.controller.AccountController;

import com.webshop.app.dao.SearchHistoryDAO;
import com.webshop.app.model.User;
import com.webshop.app.model.UserSearchHistory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@WebServlet({"/search-history", "/account/search-history"})
public class SearchHistoryPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final SearchHistoryDAO searchHistoryDAO = new SearchHistoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        User currentUser = getCurrentUser(req.getSession(false));

        if (currentUser == null || currentUser.getId() <= 0) {
            String currentPath = req.getServletPath() == null || req.getServletPath().isBlank()
                    ? "/search-history"
                    : req.getServletPath();
            String redirectTarget = URLEncoder.encode(currentPath, StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/login?redirect=" + redirectTarget);
            return;
        }

        List<UserSearchHistory> histories = Collections.emptyList();
        int totalSearchCount = 0;
        int totalResultCount = 0;
        UserSearchHistory latestHistory = null;

        try {
            histories = searchHistoryDAO.findAllByUserId(currentUser.getId());

            for (UserSearchHistory history : histories) {
                if (history == null) {
                    continue;
                }

                totalSearchCount += Math.max(history.getSearchCount(), 0);
                totalResultCount += Math.max(history.getResultCount(), 0);

                if (latestHistory == null) {
                    latestHistory = history;
                }
            }
        } catch (RuntimeException e) {
            req.setAttribute("searchHistoryLoadError", true);
            e.printStackTrace();
        }

        req.setAttribute("searchHistories", histories);
        req.setAttribute("searchHistoryCount", histories.size());
        req.setAttribute("totalSearchCount", totalSearchCount);
        req.setAttribute("totalResultCount", totalResultCount);
        req.setAttribute("latestSearchHistory", latestHistory);

        req.setAttribute("pageTitle", "MyCosmetic | Lịch sử tìm kiếm");
        req.setAttribute("pageCss", "/assets/css/search-history.css");
        req.setAttribute("pageContent", "/jsp/account/search-history.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    private User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object rawUser = session.getAttribute("user");

        if (rawUser instanceof User) {
            return (User) rawUser;
        }

        rawUser = session.getAttribute("authUser");

        if (rawUser instanceof User) {
            return (User) rawUser;
        }

        return null;
    }
}

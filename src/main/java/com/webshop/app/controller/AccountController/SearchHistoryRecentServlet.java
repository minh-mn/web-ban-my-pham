package com.webshop.app.controller.AccountController;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webshop.app.dao.SearchHistoryDAO;
import com.webshop.app.model.User;
import com.webshop.app.model.UserSearchHistory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/search-history/recent")
public class SearchHistoryRecentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final SearchHistoryDAO searchHistoryDAO = new SearchHistoryDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);
        User currentUser = getCurrentUser(session);

        if (currentUser == null || currentUser.getId() <= 0) {
            resp.getWriter().write("[]");
            return;
        }

        try {
            List<UserSearchHistory> histories =
                    searchHistoryDAO.findRecentByUserId(currentUser.getId(), 8);

            resp.getWriter().write(objectMapper.writeValueAsString(histories));

        } catch (Exception e) {
            System.out.println("[SearchHistoryRecentServlet] load recent search history error: " + e.getMessage());
            resp.getWriter().write("[]");
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
}
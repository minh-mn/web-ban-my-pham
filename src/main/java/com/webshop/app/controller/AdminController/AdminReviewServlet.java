package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.AdminReviewDAO;
import com.webshop.app.model.Review;
import com.webshop.app.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/reviews")
public class AdminReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AdminReviewDAO reviewDAO = new AdminReviewDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "list";
        }

        switch (action) {
            case "detail" -> showDetail(req, resp);
            case "list" -> showList(req, resp);
            default -> resp.sendRedirect(req.getContextPath() + "/admin/reviews");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "delete";
        }

        long id = safeLong(req.getParameter("id"), -1);
        int adminId = currentAdminId(req);
        String adminNote = req.getParameter("adminNote");

        if (id > 0) {
            switch (action) {
                case "approve" -> reviewDAO.approve(id, adminId, adminNote);
                case "reject" -> reviewDAO.reject(id, adminId, adminNote);
                case "hide" -> reviewDAO.hide(id);
                case "unhide" -> reviewDAO.unhide(id);
                case "delete" -> reviewDAO.delete(id);
                default -> {
                    // ignore unknown action
                }
            }
        }

        String back = req.getHeader("Referer");
        if (back != null && back.contains("/admin/reviews")) {
            resp.sendRedirect(back);
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/reviews");
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer rating = nullableInt(req.getParameter("rating"));
        Long productId = nullableLong(req.getParameter("productId"));
        Long authorId = nullableLong(req.getParameter("authorId"));
        String status = safeText(req.getParameter("status"));
        String mediaType = safeText(req.getParameter("media"));

        List<Review> reviews = reviewDAO.search(rating, productId, authorId, status, mediaType);
        req.setAttribute("reviews", reviews);
        req.setAttribute("status", status);
        req.setAttribute("media", mediaType);

        req.getRequestDispatcher("/jsp/admin/review/review_list.jsp").forward(req, resp);
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        long id = safeLong(req.getParameter("id"), -1);
        if (id <= 0) {
            resp.sendRedirect(req.getContextPath() + "/admin/reviews");
            return;
        }

        Review review = reviewDAO.findById(id);
        if (review == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/reviews");
            return;
        }

        req.setAttribute("review", review);
        req.getRequestDispatcher("/jsp/admin/review/review_detail.jsp").forward(req, resp);
    }

    private int currentAdminId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        return user == null ? 0 : Math.max(user.getId(), 0);
    }

    private long safeLong(String s, long def) {
        try {
            if (s == null) {
                return def;
            }
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private Integer nullableInt(String s) {
        try {
            if (s == null) {
                return null;
            }
            String value = s.trim();
            if (value.isEmpty()) {
                return null;
            }
            int parsed = Integer.parseInt(value);
            return parsed >= 1 && parsed <= 5 ? parsed : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Long nullableLong(String s) {
        try {
            if (s == null) {
                return null;
            }
            String value = s.trim();
            if (value.isEmpty()) {
                return null;
            }
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String safeText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}

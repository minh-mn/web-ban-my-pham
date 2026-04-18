package com.webshop.app.controller.AdminController;

import java.io.IOException;
import java.util.List;

import com.webshop.app.dao.AdminReviewDAO;
import com.webshop.app.model.Review;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/reviews")
public class AdminReviewServlet extends HttpServlet {

    private final AdminReviewDAO reviewDAO = new AdminReviewDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null) action = "list";

        switch (action) {

            case "detail": {
                long id = safeLong(req.getParameter("id"), -1);
                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/reviews");
                    return;
                }

                Review r = reviewDAO.findById(id);
                if (r == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/reviews");
                    return;
                }

                req.setAttribute("review", r);
                req.getRequestDispatcher("/jsp/admin/review/review_detail.jsp").forward(req, resp);
                break;
            }

            case "list":
            default: {
                Integer rating = nullableInt(req.getParameter("rating"));
                Long productId = nullableLong(req.getParameter("productId"));
                Long authorId = nullableLong(req.getParameter("authorId"));

                List<Review> reviews = reviewDAO.search(rating, productId, authorId);
                req.setAttribute("reviews", reviews);

                req.getRequestDispatcher("/jsp/admin/review/review_list.jsp").forward(req, resp);
                break;
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null) action = "delete";

        if ("delete".equals(action)) {
            long id = safeLong(req.getParameter("id"), -1);
            if (id > 0) reviewDAO.delete(id);
        }

        String back = req.getHeader("Referer");
        if (back != null && back.contains("/admin/reviews")) resp.sendRedirect(back);
        else resp.sendRedirect(req.getContextPath() + "/admin/reviews");
    }

    /* ===================== HELPERS ===================== */

    private long safeLong(String s, long def) {
        try {
            if (s == null) return def;
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private Integer nullableInt(String s) {
        try {
            if (s == null) return null;
            String t = s.trim();
            if (t.isEmpty()) return null;
            int v = Integer.parseInt(t);
            return (v >= 1 && v <= 5) ? v : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Long nullableLong(String s) {
        try {
            if (s == null) return null;
            String t = s.trim();
            if (t.isEmpty()) return null;
            long v = Long.parseLong(t);
            return v > 0 ? v : null;
        } catch (Exception e) {
            return null;
        }
    }
}

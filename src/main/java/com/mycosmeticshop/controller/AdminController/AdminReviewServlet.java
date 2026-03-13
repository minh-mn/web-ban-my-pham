package com.mycosmeticshop.controller.AdminController;

import com.mycosmeticshop.dao.AdminReviewDAO;
import com.mycosmeticshop.model.Review;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/reviews")
public class AdminReviewServlet extends HttpServlet {

    // DAO dùng để thao tác dữ liệu review phía admin
    private final AdminReviewDAO reviewDAO = new AdminReviewDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Lấy action từ URL
        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {

            // =========================
            // XEM CHI TIẾT REVIEW
            // =========================
            case "detail": {
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
                break;
            }

            // =========================
            // HIỂN THỊ DANH SÁCH REVIEW
            // Hỗ trợ filter theo rating / productId / authorId
            // =========================
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

        // Thiết lập UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Mặc định action là delete
        String action = req.getParameter("action");
        if (action == null) {
            action = "delete";
        }

        // =========================
        // XÓA REVIEW
        // =========================
        if ("delete".equals(action)) {
            long id = safeLong(req.getParameter("id"), -1);
            if (id > 0) {
                reviewDAO.delete(id);
            }
        }

        // Quay lại trang trước nếu đúng là trang admin/reviews
        String back = req.getHeader("Referer");
        if (back != null && back.contains("/admin/reviews")) {
            resp.sendRedirect(back);
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/reviews");
        }
    }

    /* ======================================================
       HELPER METHODS
       ====================================================== */

    // Parse long an toàn
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

    // Parse Integer nullable cho rating (chỉ nhận 1..5)
    private Integer nullableInt(String s) {
        try {
            if (s == null) {
                return null;
            }

            String t = s.trim();
            if (t.isEmpty()) {
                return null;
            }

            int v = Integer.parseInt(t);
            return (v >= 1 && v <= 5) ? v : null;

        } catch (Exception e) {
            return null;
        }
    }

    // Parse Long nullable, chỉ nhận giá trị > 0
    private Long nullableLong(String s) {
        try {
            if (s == null) {
                return null;
            }

            String t = s.trim();
            if (t.isEmpty()) {
                return null;
            }

            long v = Long.parseLong(t);
            return v > 0 ? v : null;

        } catch (Exception e) {
            return null;
        }
    }
}
package com.webshop.app.controller.ReviewController;

import com.webshop.app.dao.ReviewDAO;
import com.webshop.app.model.Review;
import com.webshop.app.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "ReviewFormServlet", urlPatterns = {"/orders/review", "/orders/review/form"})
public class ReviewFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ReviewDAO reviewDAO = new ReviewDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int orderId = parseInt(request.getParameter("orderId"), -1);
        int productId = parseInt(request.getParameter("productId"), -1);

        if (orderId <= 0 || productId <= 0) {
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }

        Review reviewable;
        try {
            reviewable = reviewDAO.findReviewableOrderItem(user.getId(), orderId, productId);
        } catch (RuntimeException ex) {
            throw new ServletException("Không thể kiểm tra điều kiện đánh giá sản phẩm", ex);
        }

        if (reviewable == null) {
            response.sendRedirect(request.getContextPath()
                    + "/orders/detail?id=" + orderId
                    + "&error=review_not_allowed");
            return;
        }

        request.setAttribute("reviewable", reviewable);
        request.setAttribute("error", request.getParameter("error"));
        request.setAttribute("pageTitle", "MyCosmetic | Đánh giá sản phẩm");
        request.setAttribute("pageCss", "/order.css");
        request.setAttribute("pageContent", "/jsp/order/review_form.jsp");
        request.getRequestDispatcher("/jsp/common/base.jsp").forward(request, response);
    }

    private int parseInt(String value, int defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }
}

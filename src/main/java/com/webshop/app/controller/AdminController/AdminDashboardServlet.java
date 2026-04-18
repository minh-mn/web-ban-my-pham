package com.webshop.app.controller.AdminController;

import java.io.IOException;

import com.webshop.app.dao.AdminStatsDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final AdminStatsDAO statsDAO = new AdminStatsDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        req.setAttribute("orderCount", statsDAO.countOrders());
        req.setAttribute("totalRevenue", statsDAO.totalRevenueVnd());

        // Nếu có thêm stats thì mở ra dùng luôn (tuỳ DAO)
        // req.setAttribute("userCount", statsDAO.countUsers());
        // req.setAttribute("productCount", statsDAO.countProducts());
        // req.setAttribute("todayOrderCount", statsDAO.countOrdersToday());
        // req.setAttribute("todayRevenue", statsDAO.totalRevenueTodayVnd());

        req.getRequestDispatcher("/jsp/admin/dashboard/dashboard.jsp")
           .forward(req, resp);
    }
}

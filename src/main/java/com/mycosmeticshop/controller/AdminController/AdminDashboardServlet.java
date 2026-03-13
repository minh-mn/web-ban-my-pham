package com.mycosmeticshop.controller.AdminController;

import com.mycosmeticshop.dao.AdminStatsDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    // DAO dùng để lấy dữ liệu thống kê từ database
    private final AdminStatsDAO statsDAO = new AdminStatsDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        /* ======================================================
           LẤY DỮ LIỆU THỐNG KÊ CHO DASHBOARD
           ====================================================== */

        // Tổng số đơn hàng
        req.setAttribute("orderCount", statsDAO.countOrders());

        // Tổng doanh thu (VND)
        req.setAttribute("totalRevenue", statsDAO.totalRevenueVnd());

        /*
         Có thể mở rộng thêm các thống kê khác nếu DAO có sẵn:

         req.setAttribute("userCount", statsDAO.countUsers());
         req.setAttribute("productCount", statsDAO.countProducts());

         req.setAttribute("todayOrderCount", statsDAO.countOrdersToday());
         req.setAttribute("todayRevenue", statsDAO.totalRevenueTodayVnd());
        */

        /* ======================================================
           FORWARD TỚI JSP DASHBOARD
           ====================================================== */

        req.getRequestDispatcher("/jsp/admin/dashboard/dashboard.jsp")
                .forward(req, resp);
    }
}
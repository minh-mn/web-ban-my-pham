package com.webshop.app.controller.AdminController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webshop.app.dao.AdminStatsDAO;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final AdminStatsDAO statsDAO = new AdminStatsDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        /*
         * =========================
         * BASIC STATISTICS
         * =========================
         */
        req.setAttribute("orderCount", statsDAO.countOrders());
        req.setAttribute("allOrderCount", statsDAO.countAllOrders());
        req.setAttribute("totalRevenue", statsDAO.totalRevenueVnd());
        req.setAttribute("averageOrderValue", statsDAO.averageOrderValueVnd());

        req.setAttribute("userCount", statsDAO.countUsers());
        req.setAttribute("productCount", statsDAO.countProducts());
        req.setAttribute("pendingOrderCount", statsDAO.countPendingOrders());

        /*
         * =========================
         * TODAY STATISTICS
         * =========================
         */
        req.setAttribute("todayRevenue", statsDAO.todayRevenueVnd());
        req.setAttribute("todayOrderCount", statsDAO.todayOrdersCount());

        /*
         * =========================
         * MONTH STATISTICS
         * =========================
         */
        req.setAttribute("thisMonthRevenue", statsDAO.thisMonthRevenue());
        req.setAttribute("prevMonthRevenue", statsDAO.prevMonthRevenueCalendar());
        req.setAttribute("monthGrowthPercent", statsDAO.monthGrowthPercent());

        /*
         * =========================
         * LAST 30 DAYS STATISTICS
         * =========================
         */
        req.setAttribute("last30DaysRevenue", statsDAO.last30DaysRevenue());
        req.setAttribute("prev30DaysRevenue", statsDAO.prev30DaysRevenue());
        req.setAttribute("rollingDiffVnd", statsDAO.rollingDiffVnd());
        req.setAttribute("rollingGrowthPercent", statsDAO.rollingGrowthPercent());

        /*
         * =========================
         * CHART DATA - JSON FORMAT
         * =========================
         * Dữ liệu biểu đồ nên truyền sang JSP ở dạng JSON string.
         * Tránh lỗi JavaScript khi render List<String> trực tiếp.
         */
        req.setAttribute(
                "monthlyRevenueLabelsJson",
                objectMapper.writeValueAsString(statsDAO.chartLabels())
        );

        req.setAttribute(
                "monthlyRevenueValuesJson",
                objectMapper.writeValueAsString(statsDAO.chartValues())
        );

        req.setAttribute(
                "last12MonthLabelsJson",
                objectMapper.writeValueAsString(statsDAO.last12MonthLabels())
        );

        req.setAttribute(
                "last12MonthRevenueValuesJson",
                objectMapper.writeValueAsString(statsDAO.last12MonthRevenueValues())
        );

        req.setAttribute(
                "last7DaysLabelsJson",
                objectMapper.writeValueAsString(statsDAO.last7DaysLabels())
        );

        req.setAttribute(
                "last7DaysRevenueValuesJson",
                objectMapper.writeValueAsString(statsDAO.last7DaysRevenueValues())
        );

        req.setAttribute(
                "orderStatusLabelsJson",
                objectMapper.writeValueAsString(statsDAO.orderStatusLabels())
        );

        req.setAttribute(
                "orderStatusValuesJson",
                objectMapper.writeValueAsString(statsDAO.orderStatusValues())
        );

        /*
         * =========================
         * TABLE DATA
         * =========================
         */
        req.setAttribute("topProducts", statsDAO.topSellingProducts());
        req.setAttribute("recentOrders", statsDAO.recentOrders());

        /*
         * =========================
         * OLD ATTRIBUTE COMPATIBILITY
         * =========================
         * Giữ lại một số tên cũ nếu JSP cũ đang dùng.
         */
        req.setAttribute("chartLabelsJson", objectMapper.writeValueAsString(statsDAO.chartLabels()));
        req.setAttribute("chartValuesJson", objectMapper.writeValueAsString(statsDAO.chartValues()));

        req.setAttribute("admin_total_revenue_vnd", statsDAO.totalRevenueVnd());
        req.setAttribute("this_month_revenue", statsDAO.thisMonthRevenue());
        req.setAttribute("prev_month_revenue", statsDAO.prevMonthRevenueCalendar());

        req.getRequestDispatcher("/jsp/admin/dashboard/dashboard.jsp")
                .forward(req, resp);
    }
}
package com.webshop.app.controller.AdminController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webshop.app.dao.AdminStatsDAO;

import java.io.IOException;
import java.math.BigDecimal;

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
         * CACHE BASIC VALUES
         * =========================
         * Lưu lại các giá trị hay dùng để tránh gọi DAO nhiều lần.
         */
        int orderCount = statsDAO.countOrders();
        int allOrderCount = statsDAO.countAllOrders();

        BigDecimal totalRevenue = statsDAO.totalRevenueVnd();
        BigDecimal averageOrderValue = statsDAO.averageOrderValueVnd();

        BigDecimal thisMonthRevenue = statsDAO.thisMonthRevenue();
        BigDecimal prevMonthRevenue = statsDAO.prevMonthRevenueCalendar();

        BigDecimal last30DaysRevenue = statsDAO.last30DaysRevenue();
        BigDecimal prev30DaysRevenue = statsDAO.prev30DaysRevenue();

        BigDecimal rollingDiffVnd = statsDAO.rollingDiffVnd();

        int monthGrowthPercent = statsDAO.monthGrowthPercent();
        int rollingGrowthPercent = statsDAO.rollingGrowthPercent();

        /*
         * =========================
         * BASIC STATISTICS
         * =========================
         */
        req.setAttribute("orderCount", orderCount);
        req.setAttribute("allOrderCount", allOrderCount);
        req.setAttribute("totalRevenue", totalRevenue);
        req.setAttribute("averageOrderValue", averageOrderValue);

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
        req.setAttribute("thisMonthRevenue", thisMonthRevenue);
        req.setAttribute("prevMonthRevenue", prevMonthRevenue);
        req.setAttribute("monthGrowthPercent", monthGrowthPercent);

        /*
         * =========================
         * LAST 30 DAYS STATISTICS
         * =========================
         */
        req.setAttribute("last30DaysRevenue", last30DaysRevenue);
        req.setAttribute("prev30DaysRevenue", prev30DaysRevenue);
        req.setAttribute("rollingDiffVnd", rollingDiffVnd);
        req.setAttribute("rollingGrowthPercent", rollingGrowthPercent);

        /*
         * =========================
         * REVENUE / ORDER CHART DATA
         * =========================
         * Dữ liệu biểu đồ truyền sang JSP ở dạng JSON string.
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
         * PRODUCT ANALYTICS KPI
         * =========================
         */
        req.setAttribute("unsoldThisWeekCount", statsDAO.countUnsoldProductsThisWeek());
        req.setAttribute("unsoldThisMonthCount", statsDAO.countUnsoldProductsThisMonth());
        req.setAttribute("unsoldLast30DaysCount", statsDAO.countUnsoldProductsLast30Days());

        req.setAttribute("soldProductsThisMonthCount", statsDAO.countSoldProductsThisMonth());
        req.setAttribute("outOfStockCount", statsDAO.countOutOfStockProducts());
        req.setAttribute("lowStockCount", statsDAO.countLowStockProducts());

        /*
         * =========================
         * PRODUCT ANALYTICS TABLE DATA
         * =========================
         */
        req.setAttribute("unsoldProductsThisMonth", statsDAO.unsoldProductsThisMonth());
        req.setAttribute("lowStockProducts", statsDAO.lowStockProducts());
        req.setAttribute("slowMovingProducts", statsDAO.slowMovingProductsLast30Days());

        /*
         * =========================
         * PRODUCT ANALYTICS CHART DATA
         * =========================
         */
        req.setAttribute(
                "productPerformanceLabelsJson",
                objectMapper.writeValueAsString(statsDAO.productPerformanceLabels())
        );

        req.setAttribute(
                "productPerformanceValuesJson",
                objectMapper.writeValueAsString(statsDAO.productPerformanceValues())
        );

        req.setAttribute(
                "stockStatusLabelsJson",
                objectMapper.writeValueAsString(statsDAO.stockStatusLabels())
        );

        req.setAttribute(
                "stockStatusValuesJson",
                objectMapper.writeValueAsString(statsDAO.stockStatusValues())
        );

        req.setAttribute(
                "categorySoldLabelsJson",
                objectMapper.writeValueAsString(statsDAO.categorySoldLabels())
        );

        req.setAttribute(
                "categorySoldValuesJson",
                objectMapper.writeValueAsString(statsDAO.categorySoldValues())
        );

        req.setAttribute(
                "categoryRevenueValuesJson",
                objectMapper.writeValueAsString(statsDAO.categoryRevenueValues())
        );

        /*
         * =========================
         * OLD ATTRIBUTE COMPATIBILITY
         * =========================
         * Giữ lại tên cũ nếu một số JSP khác còn đang dùng.
         */
        req.setAttribute("chartLabelsJson", objectMapper.writeValueAsString(statsDAO.chartLabels()));
        req.setAttribute("chartValuesJson", objectMapper.writeValueAsString(statsDAO.chartValues()));

        req.setAttribute("admin_total_revenue_vnd", totalRevenue);
        req.setAttribute("admin_total_orders", orderCount);
        req.setAttribute("admin_aov_vnd", averageOrderValue);

        req.setAttribute("this_month_revenue", thisMonthRevenue);
        req.setAttribute("prev_month_revenue", prevMonthRevenue);

        req.setAttribute("revenue_diff_vnd", thisMonthRevenue.subtract(prevMonthRevenue));
        req.setAttribute("revenue_percent", monthGrowthPercent);

        req.setAttribute("admin_chart_labels", objectMapper.writeValueAsString(statsDAO.chartLabels()));
        req.setAttribute("admin_chart_values", objectMapper.writeValueAsString(statsDAO.chartValues()));

        req.setAttribute("top_products", statsDAO.topSellingProducts());

        req.getRequestDispatcher("/jsp/admin/dashboard/dashboard.jsp")
                .forward(req, resp);
    }
}
package com.webshop.app.controller.AccountController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webshop.app.dao.AdminStatsDAO;
import com.webshop.app.dao.CouponDAO;
import com.webshop.app.dao.OrderDAO;
import com.webshop.app.dao.UserCouponDAO;
import com.webshop.app.dao.UserDAO;
import com.webshop.app.model.User;
import com.webshop.app.service.UserRankService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/account")
public class AccountServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();
    private final AdminStatsDAO adminStatsDAO = new AdminStatsDAO();
    private final UserDAO userDAO = new UserDAO();

    private final UserRankService userRankService = new UserRankService();
    private final UserCouponDAO userCouponDAO = new UserCouponDAO();
    private final CouponDAO couponDAO = new CouponDAO();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        /*
         * =========================
         * RELOAD USER
         * =========================
         * Reload user từ DB để lấy email/phone/manual_rank_code mới nhất.
         * Quan trọng: UserDAO phải SELECT và map manual_rank_code.
         */
        User fresh = userDAO.findById(user.getId());

        if (fresh != null) {
            user = fresh;
            session.setAttribute("user", fresh);
        }

        req.setAttribute("user", user);
        req.setAttribute("userEmail", user.getEmail());
        req.setAttribute("userPhone", user.getPhone());

        /*
         * =========================
         * DEFAULT RANK ATTRIBUTES
         * =========================
         * Nếu bảng store_rank hoặc dữ liệu rank bị lỗi,
         * giao diện vẫn không bị crash.
         */
        setDefaultRankAttributes(req);

        /*
         * =========================
         * USER RANK
         * =========================
         * Ưu tiên manual_rank_code nếu admin đã gán.
         *
         * Cũ:
         * userRankService.buildRankAttributes(user.getId())
         *
         * Mới:
         * userRankService.buildRankAttributes(user)
         *
         * Lý do:
         * - user.getManualRankCode() được truyền vào UserRankService.
         * - Nếu manualRankCode có giá trị thì account hiển thị đúng rank admin gán.
         * - Nếu manualRankCode null/AUTO thì vẫn tự tính theo tổng chi tiêu.
         */
        String currentRankCode = normalizeRankCode(user.getManualRankCode());

        try {
            Map<String, Object> rankAttributes = userRankService.buildRankAttributes(user);
            rankAttributes.forEach(req::setAttribute);

            Object rankCodeObj = rankAttributes.get("rankCode");

            if (rankCodeObj != null && !rankCodeObj.toString().isBlank()) {
                currentRankCode = normalizeRankCode(rankCodeObj.toString());
            }

        } catch (RuntimeException e) {
            /*
             * Giữ default MEMBER để trang account vẫn chạy
             * nếu DB chưa migrate store_rank hoặc query rank lỗi.
             */
            e.printStackTrace();
        }

        if (currentRankCode == null || currentRankCode.isBlank()) {
            currentRankCode = "MEMBER";
        }

        /*
         * Dùng cho JSP nếu cần kiểm tra nhanh rank hiện tại.
         */
        req.setAttribute("currentRankCode", currentRankCode);

        /*
         * =========================
         * USER COUPONS BY RANK
         * =========================
         * Issue 110:
         * Hiển thị danh sách mã giảm giá còn hiệu lực theo hạng khách hàng hiện tại.
         *
         * currentRankCode ở đây đã xét manual rank.
         */
        try {
            req.setAttribute(
                    "availableCoupons",
                    userCouponDAO.findAvailableCouponsByRankCode(currentRankCode)
            );
        } catch (RuntimeException e) {
            /*
             * Nếu bảng store_coupon chưa có min_rank_code hoặc query lỗi,
             * JSP vẫn chạy và chỉ hiển thị danh sách rỗng.
             */
            req.setAttribute("availableCoupons", Collections.emptyList());
            e.printStackTrace();
        }

        /*
         * =========================
         * USER STATS
         * =========================
         */
        req.setAttribute("total_orders", orderDAO.countByUser(user.getId()));
        req.setAttribute("total_spent_vnd", orderDAO.totalSpentByUserVnd(user.getId()));
        req.setAttribute("latest_order", orderDAO.findLatestByUser(user.getId()));

        /*
         * Mã giảm giá user đã lưu.
         */
        req.setAttribute("savedCoupons", couponDAO.findSavedCouponsByUserId(user.getId()));

        /*
         * Chart.js cần JSON hợp lệ.
         * Nếu DAO trả về List thì convert sang JSON.
         * Nếu DAO đã trả JSON string dạng [] thì giữ nguyên.
         */
        req.setAttribute("chart_labels", toJsonArray(orderDAO.userChartLabels(user.getId())));
        req.setAttribute("chart_values", toJsonArray(orderDAO.userChartValues(user.getId())));

        /*
         * =========================
         * ADMIN STATS
         * =========================
         */
        if (user.isAdmin()) {
            setAdminStatistics(req);
        }

        /*
         * =========================
         * VIEW
         * =========================
         */
        req.setAttribute("pageTitle", "MyCosmetic | Tài khoản");
        req.setAttribute("pageCss", "order.css");
        req.setAttribute("pageContent", "/jsp/account/account.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    private void setAdminStatistics(HttpServletRequest req) throws IOException {

        /*
         * =========================
         * BASIC ADMIN STATS
         * =========================
         */
        BigDecimal totalRevenue = safeMoney(adminStatsDAO.totalRevenueVnd());
        BigDecimal averageOrderValue = safeMoney(adminStatsDAO.averageOrderValueVnd());

        req.setAttribute("admin_total_orders", adminStatsDAO.countOrders());
        req.setAttribute("admin_total_revenue_vnd", totalRevenue);
        req.setAttribute("admin_aov_vnd", averageOrderValue);

        /*
         * =========================
         * MONTH REVENUE
         * =========================
         */
        BigDecimal thisMonth = safeMoney(adminStatsDAO.thisMonthRevenue());
        BigDecimal prevMonth = safeMoney(adminStatsDAO.prevMonthRevenueCalendar());

        req.setAttribute("this_month_revenue", thisMonth);
        req.setAttribute("prev_month_revenue", prevMonth);

        BigDecimal diff = thisMonth.subtract(prevMonth).setScale(0, RoundingMode.HALF_UP);

        req.setAttribute("revenue_diff", diff);
        req.setAttribute("revenue_diff_vnd", diff);

        int percent = 0;

        if (prevMonth.compareTo(BigDecimal.ZERO) > 0) {
            percent = diff.multiply(BigDecimal.valueOf(100))
                    .divide(prevMonth, 0, RoundingMode.HALF_UP)
                    .intValue();
        }

        req.setAttribute("revenue_percent", percent);

        /*
         * =========================
         * ADMIN CHART + TOP PRODUCTS
         * =========================
         */
        req.setAttribute("admin_chart_labels", toJsonArray(adminStatsDAO.chartLabels()));
        req.setAttribute("admin_chart_values", toJsonArray(adminStatsDAO.chartValues()));
        req.setAttribute("top_products", adminStatsDAO.topSellingProducts());

        /*
         * =========================
         * QUICK PRODUCT ANALYTICS FOR ACCOUNT ADMIN VIEW
         * =========================
         */
        req.setAttribute("unsoldThisMonthCount", adminStatsDAO.countUnsoldProductsThisMonth());
        req.setAttribute("unsoldLast30DaysCount", adminStatsDAO.countUnsoldProductsLast30Days());
        req.setAttribute("outOfStockCount", adminStatsDAO.countOutOfStockProducts());
        req.setAttribute("lowStockCount", adminStatsDAO.countLowStockProducts());
    }

    private void setDefaultRankAttributes(HttpServletRequest req) {

        req.setAttribute("rankLabel", "Thành viên");
        req.setAttribute("rankCode", "MEMBER");
        req.setAttribute("currentRankCode", "MEMBER");
        req.setAttribute("rankCss", "rank-member");
        req.setAttribute("rankDiscount", 0);
        req.setAttribute("rankDiscountLabel", "Không có ưu đãi");

        req.setAttribute("rankTotalSpent", BigDecimal.ZERO);
        req.setAttribute("rankPaidOrderCount", 0);

        req.setAttribute("nextRank", null);
        req.setAttribute("nextRankLabel", null);
        req.setAttribute("nextRankMinSpent", BigDecimal.ZERO);

        req.setAttribute("amountToNextRank", BigDecimal.ZERO);
        req.setAttribute("rankProgressPercent", 0);
        req.setAttribute("maxRank", false);

        /*
         * Default cho manual rank.
         */
        req.setAttribute("manualRank", false);
        req.setAttribute("rankMode", "AUTO");
        req.setAttribute("rankModeLabel", "Rank tự động theo tổng chi tiêu");

        /*
         * Default cho Issue 110.
         */
        req.setAttribute("availableCoupons", Collections.emptyList());
    }

    private String normalizeRankCode(String rankCode) {

        if (rankCode == null || rankCode.isBlank()) {
            return "MEMBER";
        }

        String normalized = rankCode.trim().toUpperCase();

        return switch (normalized) {
            case "MEMBER", "SILVER", "GOLD", "DIAMOND", "VIP" -> normalized;
            default -> "MEMBER";
        };
    }

    private BigDecimal safeMoney(BigDecimal value) {

        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private String toJsonArray(Object value) throws IOException {

        if (value == null) {
            return "[]";
        }

        if (value instanceof String stringValue) {
            String trimmed = stringValue.trim();

            if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
                return trimmed;
            }

            if (trimmed.isEmpty()) {
                return "[]";
            }
        }

        return objectMapper.writeValueAsString(value);
    }
}
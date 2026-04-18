package com.webshop.app.controller.AccountController;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.webshop.app.dao.AdminStatsDAO;
import com.webshop.app.dao.OrderDAO;
import com.webshop.app.dao.UserDAO;
import com.webshop.app.model.User;

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // ✅ Reload user từ DB để có email/phone mới nhất
        User fresh = userDAO.findById(user.getId());
        if (fresh != null) {
            user = fresh;
            session.setAttribute("user", fresh);
        }

        req.setAttribute("user", user);

        // SAFE DEFAULTS
        req.setAttribute("rankLabel", null);
        req.setAttribute("rankCss", null);
        req.setAttribute("rankDiscount", 0);

        // ✅ set email/phone thật để JSP hiển thị
        req.setAttribute("userEmail", user.getEmail());
        req.setAttribute("userPhone", user.getPhone());

        // USER STATS
        req.setAttribute("total_orders", orderDAO.countByUser(user.getId()));
        req.setAttribute("total_spent_vnd", orderDAO.totalSpentByUserVnd(user.getId()));
        req.setAttribute("latest_order", orderDAO.findLatestByUser(user.getId()));

        req.setAttribute("chart_labels", orderDAO.userChartLabels(user.getId()));
        req.setAttribute("chart_values", orderDAO.userChartValues(user.getId()));

        // ADMIN STATS
        if (user.isAdmin()) {
            req.setAttribute("admin_total_orders", adminStatsDAO.countOrders());
            req.setAttribute("admin_total_revenue_vnd", adminStatsDAO.totalRevenueVnd());
            req.setAttribute("admin_aov_vnd", adminStatsDAO.averageOrderValueVnd());

            // ✅ THÁNG NÀY / THÁNG TRƯỚC THEO THÁNG LỊCH (KHÔNG PHẢI 30 NGÀY)
            BigDecimal thisMonth = adminStatsDAO.thisMonthRevenue();              // NEW
            BigDecimal prevMonth = adminStatsDAO.prevMonthRevenueCalendar();      // NEW

            if (thisMonth == null) thisMonth = BigDecimal.ZERO;
            if (prevMonth == null) prevMonth = BigDecimal.ZERO;

            // Nếu DAO đã vnd0 rồi thì đoạn setScale dưới đây không bắt buộc,
            // nhưng để an toàn vẫn giữ:
            thisMonth = thisMonth.setScale(0, RoundingMode.HALF_UP);
            prevMonth = prevMonth.setScale(0, RoundingMode.HALF_UP);

            req.setAttribute("this_month_revenue", thisMonth);
            req.setAttribute("prev_month_revenue", prevMonth);

            BigDecimal diff = thisMonth.subtract(prevMonth).setScale(0, RoundingMode.HALF_UP);
            req.setAttribute("revenue_diff", diff);       // dùng để đổi màu/icon
            req.setAttribute("revenue_diff_vnd", diff);   // dùng để hiển thị tiền

            int percent = 0;
            if (prevMonth.compareTo(BigDecimal.ZERO) > 0) {
                percent = diff.multiply(BigDecimal.valueOf(100))
                              .divide(prevMonth, 0, RoundingMode.HALF_UP)
                              .intValue();
            }
            req.setAttribute("revenue_percent", percent);

            // Chart + top products
            req.setAttribute("admin_chart_labels", adminStatsDAO.chartLabels());
            req.setAttribute("admin_chart_values", adminStatsDAO.chartValues());
            req.setAttribute("top_products", adminStatsDAO.topSellingProducts());
        }

        // VIEW
        req.setAttribute("pageTitle", "MyCosmetic | Tài khoản");
        req.setAttribute("pageCss", "order.css");
        req.setAttribute("pageContent", "/jsp/account/account.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}

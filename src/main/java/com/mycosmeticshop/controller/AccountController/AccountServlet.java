package com.mycosmeticshop.controller.AccountController;

import com.mycosmeticshop.dao.AdminStatsDAO;
import com.mycosmeticshop.dao.OrderDAO;
import com.mycosmeticshop.dao.UserDAO;
import com.mycosmeticshop.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@WebServlet("/account")
public class AccountServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();
    private final AdminStatsDAO adminStatsDAO = new AdminStatsDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Thiết lập mã hóa để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Lấy user từ session
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Nếu chưa đăng nhập thì chuyển về trang login
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Reload user từ DB để luôn lấy email / phone mới nhất
        User fresh = userDAO.findById(user.getId());
        if (fresh != null) {
            user = fresh;
            session.setAttribute("user", fresh);
        }

        req.setAttribute("user", user);

        // Giá trị mặc định an toàn cho phần rank
        req.setAttribute("rankLabel", null);
        req.setAttribute("rankCss", null);
        req.setAttribute("rankDiscount", 0);

        // Truyền email / phone thật sang JSP
        req.setAttribute("userEmail", user.getEmail());
        req.setAttribute("userPhone", user.getPhone());

        // Thống kê cho người dùng
        req.setAttribute("total_orders", orderDAO.countByUser(user.getId()));
        req.setAttribute("total_spent_vnd", orderDAO.totalSpentByUserVnd(user.getId()));
        req.setAttribute("latest_order", orderDAO.findLatestByUser(user.getId()));

        req.setAttribute("chart_labels", orderDAO.userChartLabels(user.getId()));
        req.setAttribute("chart_values", orderDAO.userChartValues(user.getId()));

        // Nếu là admin thì lấy thêm thống kê quản trị
        if (user.isAdmin()) {
            req.setAttribute("admin_total_orders", adminStatsDAO.countOrders());
            req.setAttribute("admin_total_revenue_vnd", adminStatsDAO.totalRevenueVnd());
            req.setAttribute("admin_aov_vnd", adminStatsDAO.averageOrderValueVnd());

            // Doanh thu tháng này / tháng trước theo tháng lịch
            BigDecimal thisMonth = adminStatsDAO.thisMonthRevenue();
            BigDecimal prevMonth = adminStatsDAO.prevMonthRevenueCalendar();

            if (thisMonth == null) {
                thisMonth = BigDecimal.ZERO;
            }
            if (prevMonth == null) {
                prevMonth = BigDecimal.ZERO;
            }

            // Làm tròn về số nguyên VND
            thisMonth = thisMonth.setScale(0, RoundingMode.HALF_UP);
            prevMonth = prevMonth.setScale(0, RoundingMode.HALF_UP);

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

            // Dữ liệu biểu đồ + top sản phẩm bán chạy
            req.setAttribute("admin_chart_labels", adminStatsDAO.chartLabels());
            req.setAttribute("admin_chart_values", adminStatsDAO.chartValues());
            req.setAttribute("top_products", adminStatsDAO.topSellingProducts());
        }

        // Thiết lập thông tin view
        req.setAttribute("pageTitle", "MyCosmetic | Tài khoản");
        req.setAttribute("pageCss", "order.css");
        req.setAttribute("pageContent", "/jsp/account/account.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}
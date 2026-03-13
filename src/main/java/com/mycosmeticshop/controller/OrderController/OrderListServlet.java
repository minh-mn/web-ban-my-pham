package com.mycosmeticshop.controller.OrderController;

import com.mycosmeticshop.dao.OrderDAO;
import com.mycosmeticshop.model.Order;
import com.mycosmeticshop.model.OrderStatus;
import com.mycosmeticshop.model.User;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/*
 * Servlet hiển thị danh sách đơn hàng
 * URL truy cập: /orders
 *
 * Chức năng:
 * - Kiểm tra người dùng đã đăng nhập hay chưa
 * - Nếu là admin thì hiển thị toàn bộ đơn hàng
 * - Nếu là user thường thì chỉ hiển thị đơn hàng của chính họ
 * - Gửi danh sách đơn hàng và các trạng thái sang JSP để hiển thị
 */
@WebServlet("/orders")
public class OrderListServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAO dùng để thao tác dữ liệu đơn hàng
    private final OrderDAO orderDAO = new OrderDAO();

    /*
     * Phương thức GET
     * Hiển thị danh sách đơn hàng
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // =====================================================
        // 1) KIỂM TRA ĐĂNG NHẬP
        // =====================================================
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Nếu chưa đăng nhập -> chuyển về trang login
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // =====================================================
        // 2) LẤY DANH SÁCH ĐƠN HÀNG
        // =====================================================
        /*
         * - Admin: xem toàn bộ đơn hàng trong hệ thống
         * - User thường: chỉ xem đơn hàng của chính mình
         */
        List<Order> orders = user.isAdmin()
                ? orderDAO.findAll()
                : orderDAO.findByUser(user.getId());

        // Gửi danh sách đơn hàng sang JSP
        req.setAttribute("orders", orders);

        // =====================================================
        // 3) GỬI DANH SÁCH TRẠNG THÁI ĐƠN HÀNG
        // =====================================================
        /*
         * Dùng cho dropdown / filter / hiển thị label trạng thái tiếng Việt
         */
        req.setAttribute("statusChoices", OrderStatus.choices());

        // =====================================================
        // 4) THIẾT LẬP THÔNG TIN TRANG
        // =====================================================
        req.setAttribute("pageTitle", "MyCosmetic | Đơn hàng");
        req.setAttribute("pageCss", "/order.css");
        req.setAttribute("pageContent", "/jsp/order/order_list.jsp");

        // =====================================================
        // 5) RENDER QUA BASE LAYOUT
        // =====================================================
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}
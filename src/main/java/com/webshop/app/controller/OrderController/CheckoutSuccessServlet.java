package com.webshop.app.controller.OrderController;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.model.Order;
import com.webshop.app.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/checkout/success")
public class CheckoutSuccessServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // ✅ chặn truy cập nếu chưa login
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // ===== Parse params =====
        boolean success = "true".equalsIgnoreCase(req.getParameter("success"));
        String method = req.getParameter("method");
        String msgKey = req.getParameter("message");
        String orderIdRaw = req.getParameter("orderId");

        if (method == null || method.isBlank()) method = "UNKNOWN";

        // ===== Load order =====
        Order order = null;
        if (orderIdRaw != null && !orderIdRaw.isBlank()) {
            try {
                int orderId = Integer.parseInt(orderIdRaw);
                order = orderDAO.findById(orderId);
            } catch (NumberFormatException ignored) {}
        }

        // ✅ Security: user thường chỉ được xem order của chính họ
        if (order != null && !currentUser.isAdmin() && order.getUserId() != currentUser.getId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return;
        }

        // ✅ Nếu có order thì ưu tiên method theo order (chống giả param)
        if (order != null && order.getPaymentMethod() != null && !order.getPaymentMethod().isBlank()) {
            method = order.getPaymentMethod();
        }

        // Nếu success=true mà không có order => fail
        if (success && order == null) {
            success = false;
            if (msgKey == null || msgKey.isBlank()) msgKey = "order_not_found";
        }

        // ===== Build message =====
        String message = null;
        if (!success) {
            if ("checkout_failed".equalsIgnoreCase(msgKey)) {
                message = "Không thể hoàn tất thanh toán. Vui lòng thử lại.";
            } else if ("order_not_found".equalsIgnoreCase(msgKey)) {
                message = "Không tìm thấy đơn hàng. Vui lòng kiểm tra lại.";
            } else if (msgKey != null && !msgKey.isBlank()) {
                message = msgKey;
            } else {
                message = "Giao dịch chưa hoàn tất.";
            }
        }

        // ===== Format money =====
        String totalVnd = null;
        if (order != null && order.getTotal() != null) {
            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
            nf.setGroupingUsed(true);
            totalVnd = nf.format(order.getTotal()) + " ₫";
        }

        // ===== DATA for JSP =====
        req.setAttribute("success", success);
        req.setAttribute("message", message);
        req.setAttribute("order", order);
        req.setAttribute("totalVnd", totalVnd);
        req.setAttribute("paymentMethod", method);

        // ===== META =====
        req.setAttribute("pageTitle", "MyCosmetic | Kết quả thanh toán");
        req.setAttribute("pageCss", "/checkout-success.css");
        req.setAttribute("pageContent", "/jsp/checkout/checkout_success.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}

package com.mycosmeticshop.controller.OrderController;

import com.mycosmeticshop.dao.OrderDAO;
import com.mycosmeticshop.model.Order;
import com.mycosmeticshop.model.User;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

/*
 * Servlet hiển thị kết quả thanh toán / đặt hàng
 * URL truy cập: /checkout/success
 *
 * Chức năng:
 * - Kiểm tra người dùng đã đăng nhập hay chưa
 * - Đọc kết quả thanh toán từ request parameter
 * - Tải thông tin đơn hàng từ database
 * - Kiểm tra quyền xem đơn hàng
 * - Gửi dữ liệu sang JSP để hiển thị kết quả thành công / thất bại
 */
@WebServlet("/checkout/success")
public class CheckoutSuccessServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAO dùng để lấy thông tin đơn hàng từ database
    private final OrderDAO orderDAO = new OrderDAO();

    /*
     * Phương thức GET
     * Hiển thị trang kết quả checkout
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập encoding UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // =====================================================
        // 1) KIỂM TRA ĐĂNG NHẬP
        // =====================================================
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        // Nếu chưa đăng nhập -> chuyển về trang login
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // =====================================================
        // 2) ĐỌC REQUEST PARAMETER
        // =====================================================
        boolean success = "true".equalsIgnoreCase(req.getParameter("success"));
        String method = req.getParameter("method");
        String msgKey = req.getParameter("message");
        String orderIdRaw = req.getParameter("orderId");

        // Nếu không có payment method thì gán mặc định
        if (method == null || method.isBlank()) {
            method = "UNKNOWN";
        }

        // =====================================================
        // 3) TẢI THÔNG TIN ĐƠN HÀNG
        // =====================================================
        Order order = null;

        if (orderIdRaw != null && !orderIdRaw.isBlank()) {
            try {
                int orderId = Integer.parseInt(orderIdRaw);
                order = orderDAO.findById(orderId);
            } catch (NumberFormatException ignored) {
                // Bỏ qua nếu orderId không hợp lệ
            }
        }

        // =====================================================
        // 4) KIỂM TRA QUYỀN XEM ĐƠN HÀNG
        // =====================================================
        /*
         * Người dùng thường chỉ được xem đơn hàng của chính họ.
         * Nếu là admin thì có thể xem được mọi đơn.
         */
        if (order != null && !currentUser.isAdmin() && order.getUserId() != currentUser.getId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return;
        }

        // =====================================================
        // 5) ƯU TIÊN PAYMENT METHOD TỪ DATABASE
        // =====================================================
        /*
         * Nếu đã tải được đơn hàng thì ưu tiên paymentMethod trong order
         * để tránh giả mạo tham số method từ URL.
         */
        if (order != null && order.getPaymentMethod() != null && !order.getPaymentMethod().isBlank()) {
            method = order.getPaymentMethod();
        }

        // =====================================================
        // 6) CHUẨN HÓA KẾT QUẢ THÀNH CÔNG / THẤT BẠI
        // =====================================================
        /*
         * Nếu success=true nhưng không tìm thấy đơn hàng
         * thì phải coi như thất bại.
         */
        if (success && order == null) {
            success = false;

            if (msgKey == null || msgKey.isBlank()) {
                msgKey = "order_not_found";
            }
        }

        // =====================================================
        // 7) TẠO THÔNG BÁO HIỂN THỊ
        // =====================================================
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

        // =====================================================
        // 8) FORMAT TIỀN TỆ
        // =====================================================
        String totalVnd = null;

        if (order != null && order.getTotal() != null) {
            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
            nf.setGroupingUsed(true);
            totalVnd = nf.format(order.getTotal()) + " ₫";
        }

        // =====================================================
        // 9) GỬI DỮ LIỆU SANG JSP
        // =====================================================
        req.setAttribute("success", success);
        req.setAttribute("message", message);
        req.setAttribute("order", order);
        req.setAttribute("totalVnd", totalVnd);
        req.setAttribute("paymentMethod", method);

        // =====================================================
        // 10) THIẾT LẬP THÔNG TIN LAYOUT
        // =====================================================
        req.setAttribute("pageTitle", "MyCosmetic | Kết quả thanh toán");
        req.setAttribute("pageCss", "/checkout-success.css");
        req.setAttribute("pageContent", "/jsp/checkout/checkout_success.jsp");

        // =====================================================
        // 11) RENDER QUA BASE LAYOUT
        // =====================================================
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}
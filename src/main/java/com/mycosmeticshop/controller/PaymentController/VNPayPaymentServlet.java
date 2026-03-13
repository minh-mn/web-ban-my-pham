package com.mycosmeticshop.controller.PaymentController;

import com.mycosmeticshop.dao.OrderDAO;
import com.mycosmeticshop.model.Order;
import com.mycosmeticshop.model.User;
import com.mycosmeticshop.utils.VNPayConfig;
import com.mycosmeticshop.utils.VNPayUtil;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/*
 * Servlet tạo URL thanh toán VNPay và chuyển hướng người dùng sang cổng thanh toán
 * URL truy cập: /vnpay/payment
 *
 * Chức năng:
 * - Kiểm tra người dùng đã đăng nhập
 * - Lấy orderId đã tạo từ session
 * - Kiểm tra snapshot cart cho flow VNPay
 * - Tải đơn hàng từ database
 * - Kiểm tra quyền sở hữu đơn hàng
 * - Tạo tham số thanh toán VNPay
 * - Ký dữ liệu và redirect sang VNPay
 */
@WebServlet("/vnpay/payment")
public class VNPayPaymentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAO dùng để thao tác dữ liệu đơn hàng
    private final OrderDAO orderDAO = new OrderDAO();

    /*
     * Kiểm tra chuỗi rỗng hoặc chỉ chứa khoảng trắng
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /*
     * Phương thức GET
     * Tạo link thanh toán VNPay và chuyển hướng người dùng
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập encoding UTF-8
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);

        // =====================================================
        // 0) KIỂM TRA ĐĂNG NHẬP
        // =====================================================
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || user.getId() <= 0) {
            // Nếu chưa đăng nhập -> chuyển về login rồi quay lại checkout
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }

        // =====================================================
        // 1) LẤY orderId TỪ SESSION
        // =====================================================
        Integer orderId = (session != null) ? (Integer) session.getAttribute("VNP_ORDER_ID") : null;

        if (orderId == null || orderId <= 0) {
            // Nếu chưa có orderId hợp lệ -> quay về cart
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        // =====================================================
        // 2) KIỂM TRA SNAPSHOT CART
        // =====================================================
        /*
         * Snapshot cart được dùng để finalize ở bước return,
         * tránh phụ thuộc vào CART chính có thể đã thay đổi.
         */
        Object vnpCartObj = session.getAttribute("VNP_CART");

        if (vnpCartObj == null) {
            // Flow thiếu snapshot -> báo lỗi và quay về trang kết quả
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=vnp_cart_missing");
            return;
        }

        // =====================================================
        // 3) TẢI ĐƠN HÀNG TỪ DATABASE
        // =====================================================
        Order o = orderDAO.findById(orderId);

        if (o == null) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=order_not_found");
            return;
        }

        // =====================================================
        // 4) KIỂM TRA QUYỀN SỞ HỮU ĐƠN HÀNG
        // =====================================================
        /*
         * Chỉ cho phép người dùng hiện tại thanh toán đơn hàng của chính họ
         */
        if (o.getUserId() != user.getId()) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=forbidden_order");
            return;
        }

        // =====================================================
        // 5) KIỂM TRA PAYMENT METHOD VÀ PAYMENT STATUS
        // =====================================================
        // Chỉ xử lý các đơn hàng thanh toán bằng VNPAY
        if (!"VNPAY".equalsIgnoreCase(o.getPaymentMethod())) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=invalid_payment_method");
            return;
        }

        // Nếu đơn đã thanh toán rồi thì không tạo lại link thanh toán
        if ("PAID".equalsIgnoreCase(o.getPaymentStatus())) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=true&orderId=" + orderId + "&method=VNPAY");
            return;
        }

        // =====================================================
        // 6) TÍNH SỐ TIỀN THANH TOÁN
        // =====================================================
        /*
         * VNPay yêu cầu amount theo đơn vị:
         * VND * 100
         */
        BigDecimal total = (o.getTotal() != null) ? o.getTotal() : BigDecimal.ZERO;

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=invalid_amount");
            return;
        }

        long amount = total.multiply(BigDecimal.valueOf(100)).longValue();

        // =====================================================
        // 7) TẠO / TÁI SỬ DỤNG txnRef
        // =====================================================
        /*
         * Nếu đơn hàng đã có txnRef thì dùng lại,
         * tránh mismatch khi người dùng refresh hoặc truy cập lại.
         */
        String txnRef = o.getVnpTxnRef();

        if (isBlank(txnRef)) {
            txnRef = "MC" + orderId + "_" + System.currentTimeMillis();
            orderDAO.setVnpTxnRef(orderId, txnRef);
        }

        // Lấy IP client để gửi sang VNPay
        String clientIp = VNPayUtil.getClientIp(req);

        // =====================================================
        // 8) TẠO DANH SÁCH THAM SỐ GỬI LÊN VNPAY
        // =====================================================
        Map<String, String> vnp = new HashMap<>();

        vnp.put("vnp_Version", VNPayConfig.VNP_VERSION);
        vnp.put("vnp_Command", VNPayConfig.VNP_COMMAND);
        vnp.put("vnp_TmnCode", VNPayConfig.VNP_TMN_CODE);

        vnp.put("vnp_Amount", String.valueOf(amount));
        vnp.put("vnp_CurrCode", "VND");

        vnp.put("vnp_TxnRef", txnRef);
        vnp.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        vnp.put("vnp_OrderType", "other");

        vnp.put("vnp_Locale", "vn");
        vnp.put("vnp_ReturnUrl", VNPayConfig.VNP_RETURN_URL);

        /*
         * Gửi SecureHashType vì nhiều môi trường sandbox cần,
         * nhưng lưu ý: KHÔNG được đưa field này vào dữ liệu ký.
         */
        vnp.put("vnp_SecureHashType", "HmacSHA512");

        /*
         * IPN chỉ bật khi có URL public thật sự
         * Ví dụ:
         * vnp.put("vnp_IpnUrl", VNPayConfig.VNP_IPN_URL);
         */

        vnp.put("vnp_IpAddr", clientIp);
        vnp.put("vnp_CreateDate", VNPayUtil.nowVnp());
        vnp.put("vnp_ExpireDate", VNPayUtil.plusMinutesVnp(VNPayConfig.EXPIRE_MINUTES));

        // =====================================================
        // 9) KÝ DỮ LIỆU
        // =====================================================
        /*
         * Không ký field vnp_SecureHashType
         */
        Map<String, String> vnpForSign = new HashMap<>(vnp);
        vnpForSign.remove("vnp_SecureHashType");

        String hashData = VNPayUtil.buildHashData(vnpForSign);
        String secureHash = VNPayUtil.hmacSHA512(VNPayConfig.VNP_HASH_SECRET, hashData);

        // =====================================================
        // 10) TẠO payUrl
        // =====================================================
        String queryString = VNPayUtil.buildQueryString(vnp);
        String payUrl = VNPayConfig.VNP_PAY_URL + "?" + queryString + "&vnp_SecureHash=" + secureHash;

        // =====================================================
        // 11) DEBUG (CHỈ NÊN BẬT KHI DEV)
        // =====================================================
        // System.out.println("===== VNPAY PAYMENT DEBUG =====");
        // System.out.println("orderId=" + orderId);
        // System.out.println("txnRef=" + txnRef);
        // System.out.println("amount=" + amount);
        // System.out.println("clientIp=" + clientIp);
        // System.out.println("payUrl=" + payUrl);

        // =====================================================
        // 12) REDIRECT SANG CỔNG THANH TOÁN VNPAY
        // =====================================================
        resp.sendRedirect(payUrl);
    }
}
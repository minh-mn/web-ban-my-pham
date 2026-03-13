package com.mycosmeticshop.controller.PaymentController;

import com.mycosmeticshop.dao.OrderDAO;
import com.mycosmeticshop.model.CartItem;
import com.mycosmeticshop.model.Order;
import com.mycosmeticshop.service.CheckoutService;
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
 * Servlet xử lý RETURN từ VNPay sau khi người dùng thanh toán xong
 * URL truy cập: /payment/vnpay-return
 *
 * Chức năng:
 * - Nhận kết quả thanh toán trả về từ VNPay
 * - Kiểm tra chữ ký bảo mật
 * - Kiểm tra đơn hàng theo txnRef
 * - Kiểm tra số tiền thanh toán
 * - Finalize đơn hàng khi thanh toán thành công
 * - Cập nhật trạng thái thất bại nếu thanh toán lỗi
 */
@WebServlet("/payment/vnpay-return")
public class VNPayReturnServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAO dùng để thao tác dữ liệu đơn hàng
    private final OrderDAO orderDAO = new OrderDAO();

    // Service dùng để finalize đơn hàng sau khi VNPay thanh toán thành công
    private final CheckoutService checkoutService = new CheckoutService();

    /*
     * Kiểm tra chuỗi rỗng hoặc chỉ chứa khoảng trắng
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /*
     * Phương thức GET
     * Xử lý dữ liệu trả về từ VNPay
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập encoding UTF-8
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // =====================================================
        // 1) THU THẬP TOÀN BỘ PARAMETER vnp_*
        // =====================================================
        Map<String, String> params = new HashMap<>();

        req.getParameterMap().forEach((k, v) -> {
            if (k != null && k.startsWith("vnp_") && v != null && v.length > 0) {
                params.put(k, v[0]);
            }
        });

        String secureHash = req.getParameter("vnp_SecureHash");
        String txnRef = req.getParameter("vnp_TxnRef");
        String respCode = req.getParameter("vnp_ResponseCode");
        String vnpAmountStr = req.getParameter("vnp_Amount");

        // =====================================================
        // 2) KIỂM TRA THAM SỐ CƠ BẢN
        // =====================================================
        if (isBlank(txnRef) || isBlank(secureHash) || isBlank(respCode) || isBlank(vnpAmountStr)) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=vnp_missing_params");
            return;
        }

        // =====================================================
        // 3) KIỂM TRA CHỮ KÝ BẢO MẬT
        // =====================================================
        /*
         * Loại bỏ các field hash trước khi verify signature.
         * Dù VNPayUtil có tự loại hay chưa, vẫn nên loại ở đây để chắc chắn.
         */
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        boolean valid = VNPayUtil.verifySignature(params, secureHash);

        // =====================================================
        // 4) TÌM ĐƠN HÀNG THEO txnRef
        // =====================================================
        Integer orderId = orderDAO.findIdByTxnRef(txnRef);

        if (orderId == null || orderId <= 0) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=order_not_found");
            return;
        }

        // Nếu chữ ký sai -> coi như thanh toán thất bại
        if (!valid) {
            orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&orderId=" + orderId + "&message=invalid_signature");
            return;
        }

        // =====================================================
        // 5) KIỂM TRA SỐ TIỀN THANH TOÁN
        // =====================================================
        BigDecimal total = orderDAO.getTotalByTxnRef(txnRef);

        if (total == null) {
            total = BigDecimal.ZERO;
        }

        /*
         * VNPay dùng amount theo đơn vị:
         * VND * 100
         */
        long dbAmount = total.multiply(BigDecimal.valueOf(100)).longValue();

        long vnpAmount;
        try {
            vnpAmount = Long.parseLong(vnpAmountStr);
        } catch (Exception e) {
            orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&orderId=" + orderId + "&message=invalid_amount");
            return;
        }

        if (dbAmount != vnpAmount) {
            orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&orderId=" + orderId + "&message=amount_mismatch");
            return;
        }

        // =====================================================
        // 6) XỬ LÝ KẾT QUẢ THÀNH CÔNG / THẤT BẠI
        // =====================================================
        if ("00".equals(respCode)) {

            // -------------------------------------------------
            // 6.1) CHỐNG XỬ LÝ LẶP
            // -------------------------------------------------
            /*
             * Nếu đơn đã ở trạng thái PAID thì không finalize lại.
             * Chỉ cleanup session và chuyển về trang thành công.
             */
            Order o = orderDAO.findById(orderId);

            if (o != null && "PAID".equalsIgnoreCase(o.getPaymentStatus())) {
                cleanupSession(req.getSession(false));
                resp.sendRedirect(req.getContextPath()
                        + "/checkout/success?success=true&orderId=" + orderId + "&method=VNPAY");
                return;
            }

            // -------------------------------------------------
            // 6.2) LẤY SNAPSHOT CART VÀ COUPON TỪ SESSION
            // -------------------------------------------------
            HttpSession session = req.getSession(false);

            @SuppressWarnings("unchecked")
            Map<Integer, CartItem> vnpCart =
                    (session != null) ? (Map<Integer, CartItem>) session.getAttribute("VNP_CART") : null;

            String couponCode =
                    (session != null) ? (String) session.getAttribute("VNP_COUPON") : null;

            /*
             * Nếu thiếu snapshot cart thì không thể finalize:
             * - không có dữ liệu item
             * - không thể trừ stock
             * - không thể cập nhật couponUsed
             */
            if (vnpCart == null || vnpCart.isEmpty()) {
                orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
                resp.sendRedirect(req.getContextPath()
                        + "/checkout/success?success=false&orderId=" + orderId + "&message=vnp_cart_missing");
                return;
            }

            try {
                // -------------------------------------------------
                // 6.3) FINALIZE ĐƠN HÀNG
                // -------------------------------------------------
                /*
                 * finalizeVnpayPaid() sẽ xử lý:
                 * - lưu item đơn hàng
                 * - trừ tồn kho
                 * - đánh dấu coupon đã dùng (nếu có)
                 * - cập nhật payment_status / order_status tương ứng
                 */
                checkoutService.finalizeVnpayPaid(orderId, vnpCart, couponCode);

                // Cleanup session sau khi finalize thành công
                cleanupSession(session);

                // Chuyển về trang kết quả thành công
                resp.sendRedirect(req.getContextPath()
                        + "/checkout/success?success=true&orderId=" + orderId + "&method=VNPAY");
                return;

            } catch (Exception e) {
                e.printStackTrace();

                /*
                 * Nếu finalize lỗi thì đánh dấu FAILED
                 * để tránh đơn hàng bị treo ở trạng thái pending mãi
                 */
                orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
                resp.sendRedirect(req.getContextPath()
                        + "/checkout/success?success=false&orderId=" + orderId + "&message=finalize_failed");
                return;
            }

        } else {
            // =================================================
            // 6.4) THANH TOÁN THẤT BẠI / BỊ HỦY
            // =================================================
            orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
            cleanupSession(req.getSession(false));

            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&orderId=" + orderId + "&method=VNPAY");
        }
    }

    /*
     * Dọn dữ liệu session sau khi kết thúc flow checkout / VNPay
     */
    private void cleanupSession(HttpSession session) {
        if (session == null) {
            return;
        }

        // Xóa giỏ hàng và coupon checkout
        session.removeAttribute("CART");
        session.removeAttribute("CHECKOUT_COUPON");
        session.removeAttribute("CHECKOUT_COUPON_DISCOUNT");

        // Xóa dữ liệu tạm của flow VNPay
        session.removeAttribute("VNP_ORDER_ID");
        session.removeAttribute("VNP_COUPON");
        session.removeAttribute("VNP_CART");
    }
}
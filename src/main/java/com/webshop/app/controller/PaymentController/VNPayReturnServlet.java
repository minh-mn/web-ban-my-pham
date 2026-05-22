package com.webshop.app.controller.PaymentController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.model.CartItem;
import com.webshop.app.model.Order;
import com.webshop.app.service.CheckoutService;
import com.webshop.app.utils.VNPayUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/payment/vnpay-return")
public class VNPayReturnServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();
    private final CheckoutService checkoutService = new CheckoutService();

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // ===== 1) Collect vnp_* params =====
        Map<String, String> params = new HashMap<>();
        req.getParameterMap().forEach((k, v) -> {
            if (k.startsWith("vnp_") && v.length > 0) {
                params.put(k, v[0]);
            }
        });

        String secureHash = req.getParameter("vnp_SecureHash");
        String txnRef = req.getParameter("vnp_TxnRef");
        String respCode = req.getParameter("vnp_ResponseCode");
        String vnpAmountStr = req.getParameter("vnp_Amount");

        // Basic check
        if (isBlank(txnRef) || isBlank(secureHash) || isBlank(respCode) || isBlank(vnpAmountStr)) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=vnp_missing_params");
            return;
        }

        // ===== 2) Verify signature =====
        // VNPayUtil.verifySignature(params, secureHash) phải đảm bảo loại vnp_SecureHash/vnp_SecureHashType
        // Nếu VNPayUtil chưa loại, mình loại ở đây để chắc chắn.
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        boolean valid = VNPayUtil.verifySignature(params, secureHash);

        // ===== 3) Find orderId by txnRef =====
        Integer orderId = orderDAO.findIdByTxnRef(txnRef);
        if (orderId == null || orderId <= 0) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=order_not_found");
            return;
        }

        if (!valid) {
            // chữ ký sai => coi như thất bại
            orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&orderId=" + orderId + "&message=invalid_signature");
            return;
        }

        // ===== 4) Verify amount =====
        BigDecimal total = orderDAO.getTotalByTxnRef(txnRef);
        if (total == null) total = BigDecimal.ZERO;

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

        // ===== 5) Success/Fail =====
        if ("00".equals(respCode)) {

            // Chống xử lý lặp: nếu đã PAID thì chỉ cleanup session và redirect success
            Order o = orderDAO.findById(orderId);
            if (o != null && "PAID".equalsIgnoreCase(o.getPaymentStatus())) {
                cleanupSession(req.getSession(false));
                resp.sendRedirect(req.getContextPath()
                        + "/checkout/success?success=true&orderId=" + orderId + "&method=VNPAY");
                return;
            }

            // FINALIZE: items + stock + couponUsed + update PAID
            HttpSession session = req.getSession(false);

            @SuppressWarnings("unchecked")
            Map<String, CartItem> vnpCart =
                    (session != null) ? (Map<String, CartItem>) session.getAttribute("VNP_CART") : null;

            String couponCode =
                    (session != null) ? (String) session.getAttribute("VNP_COUPON") : null;

            if (vnpCart == null || vnpCart.isEmpty()) {
                // thiếu snapshot => không finalize được, đánh dấu FAILED để tránh pending mãi
                orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
                resp.sendRedirect(req.getContextPath()
                        + "/checkout/success?success=false&orderId=" + orderId + "&message=vnp_cart_missing");
                return;
            }

            try {
                // finalize sẽ tự update payment_status = PAID/CONFIRMED (theo code service của bạn)
                checkoutService.finalizeVnpayPaid(orderId, vnpCart, couponCode);

                // cleanup session sau khi finalize thành công
                cleanupSession(session);

                resp.sendRedirect(req.getContextPath()
                        + "/checkout/success?success=true&orderId=" + orderId + "&method=VNPAY");
                return;

            } catch (Exception e) {
                e.printStackTrace();

                // finalize fail => để FAILED để tránh pending
                orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
                resp.sendRedirect(req.getContextPath()
                        + "/checkout/success?success=false&orderId=" + orderId + "&message=finalize_failed");
                return;
            }

        } else {
            // FAIL
            orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
            cleanupSession(req.getSession(false));
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&orderId=" + orderId + "&method=VNPAY");
        }
    }

    private void cleanupSession(HttpSession session) {
        if (session == null) return;

        // cart + checkout coupon
        session.removeAttribute("CART");
        session.removeAttribute("CHECKOUT_COUPON");
        session.removeAttribute("CHECKOUT_COUPON_DISCOUNT");

        // vnpay flow
        session.removeAttribute("VNP_ORDER_ID");
        session.removeAttribute("VNP_COUPON");
        session.removeAttribute("VNP_CART");
    }
}

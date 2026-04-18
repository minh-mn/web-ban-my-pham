package com.webshop.app.controller.PaymentController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.model.Order;
import com.webshop.app.model.User;
import com.webshop.app.utils.VNPayConfig;
import com.webshop.app.utils.VNPayUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/vnpay/payment")
public class VNPayPaymentServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);

        // 0) Require login
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null || user.getId() <= 0) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }

        // 1) Require orderId from session
        Integer orderId = (session != null) ? (Integer) session.getAttribute("VNP_ORDER_ID") : null;
        if (orderId == null || orderId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        // 2) Require snapshot cart (để return/finalize dùng, tránh CART thay đổi)
        Object vnpCartObj = session.getAttribute("VNP_CART");
        if (vnpCartObj == null) {
            // Flow bị thiếu snapshot => quay về checkout tạo lại chuẩn
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=vnp_cart_missing");
            return;
        }

        // 3) Load order
        Order o = orderDAO.findById(orderId);
        if (o == null) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=order_not_found");
            return;
        }

        // 4) Security: ensure order belongs to current user
        if (o.getUserId() != user.getId()) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=forbidden_order");
            return;
        }

        // 5) Validate method/status
        if (!"VNPAY".equalsIgnoreCase(o.getPaymentMethod())) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=invalid_payment_method");
            return;
        }

        if ("PAID".equalsIgnoreCase(o.getPaymentStatus())) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=true&orderId=" + orderId + "&method=VNPAY");
            return;
        }

        // 6) Amount (VND * 100)
        BigDecimal total = (o.getTotal() != null) ? o.getTotal() : BigDecimal.ZERO;
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=invalid_amount");
            return;
        }
        long amount = total.multiply(BigDecimal.valueOf(100)).longValue();

        // 7) TxnRef: reuse if exists (avoid mismatch on refresh)
        String txnRef = o.getVnpTxnRef();
        if (isBlank(txnRef)) {
            txnRef = "MC" + orderId + "_" + System.currentTimeMillis();
            orderDAO.setVnpTxnRef(orderId, txnRef);
        }

        String clientIp = VNPayUtil.getClientIp(req);

        // ===== Params (GỬI LÊN VNPAY) =====
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

        // GỬI SecureHashType (nhiều sandbox cần) nhưng: KHÔNG ĐƯA VÀO CHUỖI KÝ
        vnp.put("vnp_SecureHashType", "HmacSHA512");

        // ⚠️ IPN: bật khi có URL public
        // vnp.put("vnp_IpnUrl", VNPayConfig.VNP_IPN_URL);

        vnp.put("vnp_IpAddr", clientIp);
        vnp.put("vnp_CreateDate", VNPayUtil.nowVnp());
        vnp.put("vnp_ExpireDate", VNPayUtil.plusMinutesVnp(VNPayConfig.EXPIRE_MINUTES));

        // ===== SIGN (KHÔNG KÝ vnp_SecureHashType) =====
        Map<String, String> vnpForSign = new HashMap<>(vnp);
        vnpForSign.remove("vnp_SecureHashType");

        String hashData = VNPayUtil.buildHashData(vnpForSign);
        String secureHash = VNPayUtil.hmacSHA512(VNPayConfig.VNP_HASH_SECRET, hashData);

        // ===== Build payUrl =====
        String queryString = VNPayUtil.buildQueryString(vnp);
        String payUrl = VNPayConfig.VNP_PAY_URL + "?" + queryString + "&vnp_SecureHash=" + secureHash;

        // ===== DEBUG (khuyến nghị chỉ bật dev) =====
        // System.out.println("===== VNPAY PAYMENT DEBUG =====");
        // System.out.println("orderId=" + orderId);
        // System.out.println("txnRef=" + txnRef);
        // System.out.println("amount=" + amount);
        // System.out.println("clientIp=" + clientIp);
        // System.out.println("payUrl=" + payUrl);

        resp.sendRedirect(payUrl);
    }
}

package com.webshop.app.controller.PaymentController;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    private static final long serialVersionUID = 1L;

    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User user = getCurrentUser(session);

        if (user == null || user.getId() <= 0) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }

        if (!VNPayConfig.isConfigured()) {
            redirectCheckoutError(req, resp, "VNPay chưa cấu hình đủ: " + VNPayConfig.getConfigError());
            return;
        }

        Integer orderId = parseInteger(req.getParameter("orderId"));

        if ((orderId == null || orderId <= 0) && session != null) {
            Object rawOrderId = session.getAttribute("VNP_ORDER_ID");
            orderId = parseInteger(rawOrderId == null ? null : rawOrderId.toString());
        }

        if (orderId == null || orderId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        Order order = orderDAO.findById(orderId);

        if (order == null) {
            redirectCheckoutError(req, resp, "order_not_found");
            return;
        }

        if (order.getUserId() != user.getId()) {
            redirectCheckoutError(req, resp, "forbidden_order");
            return;
        }

        if (!"VNPAY".equalsIgnoreCase(order.getPaymentMethod())) {
            redirectCheckoutError(req, resp, "invalid_payment_method");
            return;
        }

        if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=true&orderId=" + orderId + "&method=VNPAY");
            return;
        }

        if (!order.isRetryPaymentAvailable()) {
            resp.sendRedirect(req.getContextPath()
                    + "/orders/detail?id=" + orderId + "&message=payment_retry_not_allowed");
            return;
        }

        BigDecimal total = order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO;

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            redirectCheckoutError(req, resp, "invalid_amount");
            return;
        }

        long vnpAmount = total
                .setScale(0, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        String txnRef = "MC" + orderId + "T" + System.currentTimeMillis();
        orderDAO.prepareVnpayPaymentAttempt(orderId, txnRef);

        if (session != null) {
            session.setAttribute("VNP_ORDER_ID", orderId);
        }

        String returnUrl = VNPayConfig.resolveReturnUrl(req);
        String ipnUrl = VNPayConfig.resolveIpnUrl(req);
        String clientIp = VNPayUtil.getClientIp(req);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", VNPayConfig.VNP_VERSION);
        vnpParams.put("vnp_Command", VNPayConfig.VNP_COMMAND);
        vnpParams.put("vnp_TmnCode", VNPayConfig.VNP_TMN_CODE);
        vnpParams.put("vnp_Amount", String.valueOf(vnpAmount));
        vnpParams.put("vnp_CurrCode", VNPayConfig.VNP_CURR_CODE);
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        vnpParams.put("vnp_OrderType", VNPayConfig.VNP_ORDER_TYPE);
        vnpParams.put("vnp_Locale", VNPayConfig.VNP_LOCALE);
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", clientIp);
        vnpParams.put("vnp_CreateDate", VNPayUtil.nowVnp());

        if (!VNPayUtil.isBlank(ipnUrl)) {
            vnpParams.put("vnp_IpnUrl", ipnUrl);
        }

        if (VNPayConfig.SEND_EXPIRE_DATE) {
            vnpParams.put("vnp_ExpireDate", VNPayUtil.plusMinutesVnp(VNPayConfig.EXPIRE_MINUTES));
        }

        String bankCode = req.getParameter("bankCode");
        if (!VNPayUtil.isBlank(bankCode)) {
            vnpParams.put("vnp_BankCode", bankCode.trim());
        }

        String hashData = VNPayUtil.buildHashData(vnpParams);
        String secureHash = VNPayUtil.hmacSHA512(VNPayConfig.VNP_HASH_SECRET, hashData);
        String queryString = VNPayUtil.buildQueryString(vnpParams);

        String paymentUrl = VNPayConfig.VNP_PAY_URL
                + "?"
                + queryString
                + "&vnp_SecureHash="
                + secureHash;

        if (VNPayConfig.DEBUG) {
            System.out.println("===== VNPAY PAYMENT DEBUG =====");
            System.out.println("orderId=" + orderId);
            System.out.println("txnRef=" + txnRef);
            System.out.println("amount=" + vnpAmount);
            System.out.println("tmnCode=" + VNPayConfig.VNP_TMN_CODE);
            System.out.println("hashSecret=" + VNPayConfig.maskedSecret());
            System.out.println("hashSecretLength=" + VNPayConfig.VNP_HASH_SECRET.length());
            System.out.println("payUrl=" + VNPayConfig.VNP_PAY_URL);
            System.out.println("returnUrl=" + returnUrl);
            System.out.println("ipnUrl=" + ipnUrl);
            System.out.println("clientIp=" + clientIp);
            System.out.println("hashData=" + hashData);
            System.out.println("secureHash=" + secureHash);
            System.out.println("paymentUrl=" + paymentUrl);
            System.out.println("================================");
        }

        resp.sendRedirect(paymentUrl);
    }

    private User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object rawUser = session.getAttribute("user");
        if (rawUser instanceof User) {
            return (User) rawUser;
        }

        return null;
    }

    private Integer parseInteger(String value) {
        if (VNPayUtil.isBlank(value)) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void redirectCheckoutError(HttpServletRequest req,
                                       HttpServletResponse resp,
                                       String message) throws IOException {
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        resp.sendRedirect(req.getContextPath()
                + "/checkout/success?success=false&message=" + encodedMessage);
    }
}

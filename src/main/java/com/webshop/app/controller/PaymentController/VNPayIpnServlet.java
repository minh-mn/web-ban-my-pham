package com.webshop.app.controller.PaymentController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.utils.VNPayUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/payment/vnpay-ipn")
public class VNPayIpnServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        // ===== 1) Collect vnp_* params =====
        Map<String, String> params = new HashMap<>();
        req.getParameterMap().forEach((k, v) -> {
            if (k != null && k.startsWith("vnp_") && v != null && v.length > 0) {
                params.put(k, v[0]);
            }
        });

        String secureHash = req.getParameter("vnp_SecureHash");
        String txnRef = req.getParameter("vnp_TxnRef");
        String respCode = req.getParameter("vnp_ResponseCode");
        String transStatus = req.getParameter("vnp_TransactionStatus");
        String amountStr = req.getParameter("vnp_Amount");

        if (isBlank(secureHash) || isBlank(txnRef) || isBlank(respCode) || isBlank(transStatus) || isBlank(amountStr)) {
            resp.getWriter().write("{\"RspCode\":\"99\",\"Message\":\"Missing params\"}");
            return;
        }

        // ===== 2) Verify signature (must exclude hash fields) =====
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        boolean valid = VNPayUtil.verifySignature(params, secureHash);
        if (!valid) {
            resp.getWriter().write("{\"RspCode\":\"97\",\"Message\":\"Invalid signature\"}");
            return;
        }

        // ===== 3) Find order by txnRef =====
        Integer orderId = orderDAO.findIdByTxnRef(txnRef);
        if (orderId == null || orderId <= 0) {
            resp.getWriter().write("{\"RspCode\":\"01\",\"Message\":\"Order not found\"}");
            return;
        }

        // ===== 4) Verify amount =====
        BigDecimal dbTotal = orderDAO.getTotalByTxnRef(txnRef);
        long dbAmount = (dbTotal != null ? dbTotal.multiply(BigDecimal.valueOf(100)).longValue() : 0L);

        long vnpAmount;
        try {
            vnpAmount = Long.parseLong(amountStr);
        } catch (Exception e) {
            resp.getWriter().write("{\"RspCode\":\"04\",\"Message\":\"Invalid amount\"}");
            return;
        }

        if (dbAmount != vnpAmount) {
            resp.getWriter().write("{\"RspCode\":\"04\",\"Message\":\"Amount mismatch\"}");
            return;
        }

        // ===== 5) Idempotent =====
        if (orderDAO.isPaidByTxnRef(txnRef)) {
            resp.getWriter().write("{\"RspCode\":\"00\",\"Message\":\"Already processed\"}");
            return;
        }

        // ===== 6) Update status =====
        // Với kiến trúc hiện tại: finalize (items/stock/coupon) đang làm ở RETURN dựa vào session snapshot.
        // IPN KHÔNG có session => IPN chỉ đánh dấu thanh toán, nhưng để status ở PENDING
        // để RETURN finalize xong mới set CONFIRMED.
        if ("00".equals(respCode) && "00".equals(transStatus)) {

            // payment paid but not finalized yet
            orderDAO.updatePaymentStatus(orderId, "PAID", "PENDING", txnRef);

            resp.getWriter().write("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
            return;
        }

        // Failed / cancelled
        orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
        resp.getWriter().write("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
    }
}

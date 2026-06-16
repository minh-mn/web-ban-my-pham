package com.webshop.app.controller.PaymentController;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.model.Order;
import com.webshop.app.service.CheckoutService;
import com.webshop.app.utils.VNPayConfig;
import com.webshop.app.utils.VNPayUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet({"/payment/vnpay-ipn", "/vnpay-ipn"})
public class VNPayIpnServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final OrderDAO orderDAO = new OrderDAO();
    private final CheckoutService checkoutService = new CheckoutService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        Map<String, String> params = collectVnpParams(req);

        String secureHash = req.getParameter("vnp_SecureHash");
        String txnRef = req.getParameter("vnp_TxnRef");
        String responseCode = req.getParameter("vnp_ResponseCode");
        String transactionStatus = req.getParameter("vnp_TransactionStatus");
        String amountStr = req.getParameter("vnp_Amount");

        if (VNPayConfig.DEBUG) {
            System.out.println("===== VNPAY IPN DEBUG =====");
            System.out.println("txnRef=" + txnRef);
            System.out.println("responseCode=" + responseCode);
            System.out.println("transactionStatus=" + transactionStatus);
            System.out.println("amount=" + amountStr);
            System.out.println("secureHash=" + secureHash);
        }

        if (VNPayUtil.isBlank(secureHash)
                || VNPayUtil.isBlank(txnRef)
                || VNPayUtil.isBlank(responseCode)
                || VNPayUtil.isBlank(transactionStatus)
                || VNPayUtil.isBlank(amountStr)) {
            writeJson(resp, "99", "Missing params");
            return;
        }

        if (!VNPayUtil.verifySignature(params, secureHash)) {
            writeJson(resp, "97", "Invalid signature");
            return;
        }

        Integer orderId = resolveOrderId(txnRef);
        if (orderId == null || orderId <= 0) {
            writeJson(resp, "01", "Order not found");
            return;
        }

        orderDAO.setVnpTxnRef(orderId, txnRef);

        if (!isAmountMatched(orderId, amountStr)) {
            writeJson(resp, "04", "Amount mismatch");
            return;
        }

        if (orderDAO.isPaidByTxnRef(txnRef)) {
            writeJson(resp, "00", "Already processed");
            return;
        }

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            try {
                checkoutService.finalizeVnpayPaid(orderId);
                writeJson(resp, "00", "Confirm Success");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                orderDAO.markVnpayAwaitingRetry(orderId, txnRef,
                        "IPN không thể hoàn tất đơn VNPay.");
                writeJson(resp, "99", "Finalize failed");
                return;
            }
        }

        orderDAO.markVnpayAwaitingRetry(orderId, txnRef,
                "VNPay IPN responseCode=" + responseCode
                        + ", transactionStatus=" + transactionStatus);
        writeJson(resp, "00", "Confirm Success");
    }

    private Map<String, String> collectVnpParams(HttpServletRequest req) {
        Map<String, String> params = new HashMap<>();

        req.getParameterMap().forEach((key, values) -> {
            if (key != null && key.startsWith("vnp_") && values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });

        return params;
    }

    private Integer resolveOrderId(String txnRef) {
        Integer orderId = orderDAO.findIdByTxnRef(txnRef);
        if (orderId != null && orderId > 0) {
            return orderId;
        }

        orderId = parseOrderIdFromTxnRef(txnRef);
        if (orderId != null && orderId > 0 && orderDAO.findById(orderId) != null) {
            return orderId;
        }

        return null;
    }

    private Integer parseOrderIdFromTxnRef(String txnRef) {
        if (VNPayUtil.isBlank(txnRef) || !txnRef.startsWith("MC")) {
            return null;
        }

        String body = txnRef.substring(2).trim();
        if (body.isEmpty()) {
            return null;
        }

        int separatorIndex = body.indexOf('T');
        String orderIdText;

        if (separatorIndex > 0) {
            orderIdText = body.substring(0, separatorIndex);
        } else if (body.length() > 13) {
            orderIdText = body.substring(0, body.length() - 13);
        } else {
            return null;
        }

        try {
            return Integer.parseInt(orderIdText);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isAmountMatched(int orderId, String amountStr) {
        Order order = orderDAO.findById(orderId);
        BigDecimal dbTotal = order != null && order.getTotal() != null
                ? order.getTotal()
                : BigDecimal.ZERO;

        long dbAmount = dbTotal
                .setScale(0, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        try {
            long vnpAmount = Long.parseLong(amountStr);
            return dbAmount == vnpAmount;
        } catch (Exception e) {
            return false;
        }
    }

    private void writeJson(HttpServletResponse resp, String code, String message) throws IOException {
        String safeCode = code == null ? "99" : code.replace("\"", "");
        String safeMessage = message == null ? "Unknown" : message.replace("\"", "'");
        resp.getWriter().write("{\"RspCode\":\"" + safeCode + "\",\"Message\":\"" + safeMessage + "\"}");
    }
}

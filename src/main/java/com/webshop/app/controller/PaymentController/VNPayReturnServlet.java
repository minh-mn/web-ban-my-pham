package com.webshop.app.controller.PaymentController;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.model.CartItem;
import com.webshop.app.model.Order;
import com.webshop.app.model.User;
import com.webshop.app.service.CheckoutService;
import com.webshop.app.utils.CartUtil;
import com.webshop.app.utils.VNPayConfig;
import com.webshop.app.utils.VNPayUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/payment/vnpay-return")
public class VNPayReturnServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final OrderDAO orderDAO = new OrderDAO();
    private final CheckoutService checkoutService = new CheckoutService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        Map<String, String> params = collectVnpParams(req);

        String secureHash = req.getParameter("vnp_SecureHash");
        String txnRef = req.getParameter("vnp_TxnRef");
        String responseCode = req.getParameter("vnp_ResponseCode");
        String transactionStatus = req.getParameter("vnp_TransactionStatus");
        String amountStr = req.getParameter("vnp_Amount");

        if (VNPayConfig.DEBUG) {
            System.out.println("===== VNPAY RETURN DEBUG =====");
            System.out.println("txnRef=" + txnRef);
            System.out.println("responseCode=" + responseCode);
            System.out.println("transactionStatus=" + transactionStatus);
            System.out.println("amount=" + amountStr);
            System.out.println("secureHash=" + secureHash);
        }

        if (VNPayUtil.isBlank(txnRef)
                || VNPayUtil.isBlank(secureHash)
                || VNPayUtil.isBlank(responseCode)
                || VNPayUtil.isBlank(amountStr)) {
            redirectFail(req, resp, null, "vnp_missing_params");
            return;
        }

        boolean validSignature = VNPayUtil.verifySignature(params, secureHash);
        Integer orderId = resolveOrderId(req, txnRef);

        if (VNPayConfig.DEBUG) {
            System.out.println("resolvedOrderId=" + orderId);
            System.out.println("validSignature=" + validSignature);
            System.out.println("===============================");
        }

        if (!validSignature) {
            if (orderId != null && orderId > 0) {
                orderDAO.markVnpayAwaitingRetry(orderId, txnRef, "VNPay return invalid signature.");
            }
            cleanupVnpayOnly(req.getSession(false));
            redirectFail(req, resp, orderId, "invalid_signature");
            return;
        }

        if (orderId == null || orderId <= 0) {
            cleanupVnpayOnly(req.getSession(false));
            redirectFail(req, resp, null, "order_not_found");
            return;
        }

        // Đồng bộ lại txnRef vào đơn nếu trước đó DB chưa lưu được.
        orderDAO.setVnpTxnRef(orderId, txnRef);

        if (!isAmountMatched(orderId, amountStr)) {
            orderDAO.markVnpayAwaitingRetry(orderId, txnRef, "VNPay amount mismatch.");
            cleanupVnpayOnly(req.getSession(false));
            redirectFail(req, resp, orderId, "amount_mismatch");
            return;
        }

        if ("00".equals(responseCode)
                && (VNPayUtil.isBlank(transactionStatus) || "00".equals(transactionStatus))) {
            handleSuccess(req, resp, orderId, txnRef);
            return;
        }

        orderDAO.markVnpayAwaitingRetry(orderId, txnRef,
                "VNPay payment failed. responseCode=" + responseCode
                        + ", transactionStatus=" + transactionStatus);
        cleanupVnpayOnly(req.getSession(false));

        redirectFail(req, resp, orderId, "vnpay_failed");
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

    private Integer resolveOrderId(HttpServletRequest req, String txnRef) {
        Integer orderId = null;

        try {
            orderId = orderDAO.findIdByTxnRef(txnRef);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (orderId != null && orderId > 0) {
            return orderId;
        }

        HttpSession session = req.getSession(false);
        orderId = getOrderIdFromSession(session);

        if (orderId != null && orderId > 0 && orderDAO.findById(orderId) != null) {
            return orderId;
        }

        orderId = parseOrderIdFromTxnRef(txnRef);

        if (orderId != null && orderId > 0 && orderDAO.findById(orderId) != null) {
            return orderId;
        }

        return null;
    }

    private Integer getOrderIdFromSession(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object rawOrderId = session.getAttribute("VNP_ORDER_ID");
        if (rawOrderId == null) {
            return null;
        }

        try {
            return Integer.parseInt(rawOrderId.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Hỗ trợ cả định dạng mới MC{orderId}T{timestamp}
     * và định dạng cũ MC{orderId}{13-digit timestamp}.
     */
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
        BigDecimal total = order != null && order.getTotal() != null
                ? order.getTotal()
                : BigDecimal.ZERO;

        long dbAmount = total
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

    private void handleSuccess(HttpServletRequest req,
                               HttpServletResponse resp,
                               int orderId,
                               String txnRef) throws IOException {

        HttpSession session = req.getSession(false);
        User currentUser = getCurrentUser(session);

        Order order = orderDAO.findById(orderId);

        if (order != null
                && "PAID".equalsIgnoreCase(order.getPaymentStatus())
                && order.isStockDeducted()) {
            cleanupPaidItems(session);
            sendOrderSuccessEmailSafely(session, currentUser, orderId);

            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=true&orderId=" + orderId + "&method=VNPAY");
            return;
        }

        Map<String, CartItem> vnpCart = getVnpCart(session);
        String couponCode = session != null ? (String) session.getAttribute("VNP_COUPON") : null;

        try {
            checkoutService.finalizeVnpayPaid(orderId, vnpCart, couponCode);
            cleanupPaidItems(session);
            sendOrderSuccessEmailSafely(session, currentUser, orderId);

            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=true&orderId=" + orderId + "&method=VNPAY");
        } catch (Exception e) {
            e.printStackTrace();
            orderDAO.markVnpayAwaitingRetry(orderId, txnRef,
                    "Không thể hoàn tất đơn sau khi VNPay trả về thành công.");
            cleanupVnpayOnly(session);

            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&orderId=" + orderId
                    + "&message=finalize_failed");
        }
    }

    private void redirectFail(HttpServletRequest req,
                              HttpServletResponse resp,
                              Integer orderId,
                              String message) throws IOException {
        StringBuilder url = new StringBuilder(req.getContextPath())
                .append("/checkout/success?success=false");

        if (orderId != null && orderId > 0) {
            url.append("&orderId=").append(orderId);
        }

        url.append("&message=").append(message == null ? "vnpay_failed" : message);
        resp.sendRedirect(url.toString());
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

    private void sendOrderSuccessEmailSafely(HttpSession session, User user, int orderId) {
        if (orderId <= 0) {
            return;
        }

        String sentKey = "ORDER_SUCCESS_EMAIL_SENT_" + orderId;
        String oldSentKey = "ORDER_EMAIL_SENT_" + orderId;
        String failedKey = "ORDER_SUCCESS_EMAIL_FAILED_" + orderId;

        if (session != null) {
            Object alreadySent = session.getAttribute(sentKey);
            Object alreadySentOld = session.getAttribute(oldSentKey);

            if (Boolean.TRUE.equals(alreadySent) || Boolean.TRUE.equals(alreadySentOld)) {
                return;
            }
        }

        String email = getUserEmail(user);
        boolean sent = false;

        try {
            Class<?> serviceClass = Class.forName("com.webshop.app.service.OrderEmailService");
            Object emailService = serviceClass.getDeclaredConstructor().newInstance();

            sent = tryInvokeEmailMethod(serviceClass, emailService, "sendOrderSuccessEmail", orderId, email)
                    || tryInvokeEmailMethod(serviceClass, emailService, "sendOrderConfirmationEmail", orderId, email);
        } catch (ClassNotFoundException ignored) {
            // Chưa có OrderEmailService thì bỏ qua, không làm hỏng luồng thanh toán.
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (session != null) {
            if (sent) {
                session.setAttribute(sentKey, true);
                session.setAttribute(oldSentKey, true);
                session.removeAttribute(failedKey);
            } else {
                session.setAttribute(failedKey, true);
            }
        }
    }

    private boolean tryInvokeEmailMethod(Class<?> serviceClass,
                                         Object service,
                                         String methodName,
                                         int orderId,
                                         String email) {
        if (!VNPayUtil.isBlank(email)) {
            try {
                serviceClass.getMethod(methodName, int.class, String.class)
                        .invoke(service, orderId, email);
                return true;
            } catch (NoSuchMethodException ignored) {
                // Thử signature tiếp theo.
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            try {
                serviceClass.getMethod(methodName, String.class, int.class)
                        .invoke(service, email, orderId);
                return true;
            } catch (NoSuchMethodException ignored) {
                // Thử signature tiếp theo.
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        try {
            serviceClass.getMethod(methodName, int.class).invoke(service, orderId);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getUserEmail(User user) {
        if (user == null) {
            return "";
        }

        try {
            Object value = user.getClass().getMethod("getEmail").invoke(user);
            return value == null ? "" : value.toString().trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, CartItem> getVnpCart(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object rawCart = session.getAttribute("VNP_CART");
        if (rawCart instanceof Map<?, ?>) {
            return (Map<String, CartItem>) rawCart;
        }

        return null;
    }

    private void cleanupPaidItems(HttpSession session) {
        if (session == null) {
            return;
        }

        Map<String, CartItem> vnpCart = getVnpCart(session);
        if (vnpCart != null && !vnpCart.isEmpty()) {
            CartUtil.removeItems(session, vnpCart.keySet());
        }

        cleanupCheckoutAndVnpaySession(session);
        CartUtil.clearSelectedCartKeys(session);
    }

    private void cleanupVnpayOnly(HttpSession session) {
        if (session == null) {
            return;
        }

        cleanupCheckoutAndVnpaySession(session);
        CartUtil.clearSelectedCartKeys(session);
    }

    private void cleanupCheckoutAndVnpaySession(HttpSession session) {
        if (session == null) {
            return;
        }

        session.removeAttribute("CHECKOUT_COUPON");
        session.removeAttribute("CHECKOUT_COUPON_DISCOUNT");
        session.removeAttribute("VNP_ORDER_ID");
        session.removeAttribute("VNP_COUPON");
        session.removeAttribute("VNP_CART");
    }
}

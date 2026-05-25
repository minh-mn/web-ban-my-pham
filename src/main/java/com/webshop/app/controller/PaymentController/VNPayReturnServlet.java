package com.webshop.app.controller.PaymentController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.model.CartItem;
import com.webshop.app.model.Order;
import com.webshop.app.model.User;
import com.webshop.app.service.CheckoutService;
import com.webshop.app.utils.CartUtil;
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

        req.getParameterMap().forEach((key, values) -> {
            if (key.startsWith("vnp_") && values.length > 0) {
                params.put(key, values[0]);
            }
        });

        String secureHash = req.getParameter("vnp_SecureHash");
        String txnRef = req.getParameter("vnp_TxnRef");
        String responseCode = req.getParameter("vnp_ResponseCode");
        String vnpAmountStr = req.getParameter("vnp_Amount");

        if (isBlank(txnRef) || isBlank(secureHash) || isBlank(responseCode) || isBlank(vnpAmountStr)) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=vnp_missing_params");
            return;
        }

        // ===== 2) Verify signature =====
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        boolean validSignature = VNPayUtil.verifySignature(params, secureHash);

        // ===== 3) Find order by txnRef =====
        Integer orderId = orderDAO.findIdByTxnRef(txnRef);

        if (orderId == null || orderId <= 0) {
            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=order_not_found");
            return;
        }

        if (!validSignature) {
            orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
            cleanupVnpayOnly(req.getSession(false));

            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&orderId=" + orderId
                    + "&message=invalid_signature");
            return;
        }

        // ===== 4) Verify amount =====
        BigDecimal total = orderDAO.getTotalByTxnRef(txnRef);

        if (total == null) {
            total = BigDecimal.ZERO;
        }

        long dbAmount = total.multiply(BigDecimal.valueOf(100)).longValue();

        long vnpAmount;

        try {
            vnpAmount = Long.parseLong(vnpAmountStr);
        } catch (Exception e) {
            orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
            cleanupVnpayOnly(req.getSession(false));

            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&orderId=" + orderId
                    + "&message=invalid_amount");
            return;
        }

        if (dbAmount != vnpAmount) {
            orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
            cleanupVnpayOnly(req.getSession(false));

            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&orderId=" + orderId
                    + "&message=amount_mismatch");
            return;
        }

        // ===== 5) Success / Fail =====
        if ("00".equals(responseCode)) {
            handleSuccess(req, resp, orderId, txnRef);
            return;
        }

        orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
        cleanupVnpayOnly(req.getSession(false));

        resp.sendRedirect(req.getContextPath()
                + "/checkout/success?success=false&orderId=" + orderId
                + "&method=VNPAY");
    }

    private void handleSuccess(
            HttpServletRequest req,
            HttpServletResponse resp,
            int orderId,
            String txnRef
    ) throws IOException {

        HttpSession session = req.getSession(false);
        User currentUser = getCurrentUser(session);

        /*
         * Nếu đơn đã PAID rồi thì không finalize lại.
         * Chỉ cleanup theo VNP_CART nếu còn snapshot.
         */
        Order order = orderDAO.findById(orderId);

        if (order != null && "PAID".equalsIgnoreCase(order.getPaymentStatus())) {
            cleanupPaidItems(session);
            sendOrderSuccessEmailSafely(session, currentUser, orderId);

            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=true&orderId=" + orderId
                    + "&method=VNPAY");
            return;
        }

        Map<String, CartItem> vnpCart = getVnpCart(session);

        String couponCode =
                (session != null) ? (String) session.getAttribute("VNP_COUPON") : null;

        if (vnpCart == null || vnpCart.isEmpty()) {
            orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
            cleanupVnpayOnly(session);

            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&orderId=" + orderId
                    + "&message=vnp_cart_missing");
            return;
        }

        try {
            /*
             * finalizeVnpayPaid:
             * - tạo order item nếu cần
             * - trừ tồn kho
             * - tăng used_count coupon nếu có
             * - cập nhật payment_status = PAID
             */
            checkoutService.finalizeVnpayPaid(orderId, vnpCart, couponCode);

            /*
             * Mục 68:
             * Chỉ xóa các sản phẩm đã thanh toán trong VNP_CART.
             * Sản phẩm chưa tích chọn vẫn giữ trong CART.
             */
            cleanupPaidItems(session);
            sendOrderSuccessEmailSafely(session, currentUser, orderId);

            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=true&orderId=" + orderId
                    + "&method=VNPAY");

        } catch (Exception e) {
            e.printStackTrace();

            orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
            cleanupVnpayOnly(session);

            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&orderId=" + orderId
                    + "&message=finalize_failed");
        }
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

    /**
     * Mục 91 - gửi email xác nhận đơn hàng sau khi VNPAY thanh toán thành công.
     * Hàm này không làm hỏng luồng thanh toán nếu gửi mail lỗi.
     */
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
            /*
             * Chưa có OrderEmailService thì bỏ qua để không làm lỗi thanh toán.
             */
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
        if (!isBlank(email)) {
            try {
                serviceClass
                        .getMethod(methodName, int.class, String.class)
                        .invoke(service, orderId, email);
                return true;
            } catch (NoSuchMethodException ignored) {
                // Thử signature tiếp theo.
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            try {
                serviceClass
                        .getMethod(methodName, String.class, int.class)
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
            serviceClass
                    .getMethod(methodName, int.class)
                    .invoke(service, orderId);
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

            if (value != null) {
                return value.toString().trim();
            }
        } catch (Exception ignored) {
            // User model chưa có getEmail thì để rỗng.
        }

        return "";
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

    /*
     * Dùng khi VNPAY thành công.
     * Chỉ xóa item đã thanh toán khỏi CART.
     */
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

    /*
     * Dùng khi VNPAY thất bại / sai chữ ký / sai tiền.
     * Không xóa CART để người dùng còn có thể thanh toán lại.
     */
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
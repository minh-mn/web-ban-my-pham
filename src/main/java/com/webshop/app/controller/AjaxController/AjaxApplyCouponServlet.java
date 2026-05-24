package com.webshop.app.controller.AjaxController;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import com.webshop.app.dao.CouponDAO;
import com.webshop.app.model.CartItem;
import com.webshop.app.model.Coupon;
import com.webshop.app.model.User;
import com.webshop.app.service.CheckoutService;
import com.webshop.app.utils.CartUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ajax/apply-coupon")
public class AjaxApplyCouponServlet extends HttpServlet {

    private static final String SESSION_CHECKOUT_COUPON = "CHECKOUT_COUPON";
    private static final String SESSION_CHECKOUT_COUPON_DISCOUNT = "CHECKOUT_COUPON_DISCOUNT";

    private final CheckoutService checkoutService = new CheckoutService();
    private final CouponDAO couponDAO = new CouponDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession();
        User loggedInUser = (User) session.getAttribute("user");

        if (loggedInUser == null) {
            writeJson(resp, false, "Vui lòng đăng nhập để thực hiện.", BigDecimal.ZERO, null);
            return;
        }

        String code = req.getParameter("code");
        String action = req.getParameter("action"); // Tham số này rất quan trọng

        Coupon coupon = couponDAO.findByCode(code);
        if (coupon == null) {
            writeJson(resp, false, "Mã không tồn tại hoặc đã hết hạn.", BigDecimal.ZERO, code);
            return;
        }

        // NẾU LÀ ACTION "save", CHÚNG TA BỎ QUA KIỂM TRA GIỎ HÀNG
        if (!"save".equals(action)) {
            Map<String, CartItem> cart = getCouponCart(session);
            BigDecimal subTotal = calculateSubtotal(cart);

            if (subTotal.compareTo(BigDecimal.ZERO) <= 0) {
                writeJson(resp, false, "Giỏ hàng đang trống.", BigDecimal.ZERO, code);
                return;
            }

            String invalidReason = couponDAO.getCouponInvalidReason(coupon, subTotal);
            if (invalidReason != null) {
                writeJson(resp, false, invalidReason, BigDecimal.ZERO, code);
                return;
            }
        }

        // Thực hiện lưu vào ví
        boolean saved = couponDAO.saveVoucherToUserCollection(loggedInUser.getId(), code);

        if (saved) {
            writeJson(resp, true, "Đã lưu vào ví thành công!", BigDecimal.ZERO, code);
        } else {
            writeJson(resp, false, "Bạn đã sở hữu mã này rồi.", BigDecimal.ZERO, code);
        }
    }

    private String getCouponCodeFromRequest(HttpServletRequest req) {
        String code = req.getParameter("code");

        if (code == null || code.trim().isEmpty()) {
            code = req.getParameter("couponCode");
        }

        return normalizeCouponCode(code);
    }

    private Map<String, CartItem> getCouponCart(HttpSession session) {
        Map<String, CartItem> selectedCart = CartUtil.getSelectedCart(session);

        if (selectedCart != null && !selectedCart.isEmpty()) {
            return selectedCart;
        }

        return CartUtil.getCart(session);
    }

    private BigDecimal calculateSubtotal(Map<String, CartItem> cart) {
        if (cart == null || cart.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return cart.values()
                .stream()
                .filter(item -> item != null && item.getSubtotal() != null)
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(0, RoundingMode.HALF_UP);
    }

    private String normalizeCouponCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return "";
        }

        return code.trim().toUpperCase();
    }

    private void clearCheckoutCoupon(HttpSession session) {
        session.removeAttribute(SESSION_CHECKOUT_COUPON);
        session.removeAttribute(SESSION_CHECKOUT_COUPON_DISCOUNT);
    }

    private void writeJson(
            HttpServletResponse resp,
            boolean success,
            String message,
            BigDecimal discount,
            String code
    ) throws IOException {

        BigDecimal safeDiscount = discount == null ? BigDecimal.ZERO : discount;

        StringBuilder json = new StringBuilder();
        json.append("{");

        /*
         * Trả cả success và ok để JSP/JS dùng key nào cũng không bị lệch.
         */
        json.append("\"success\":").append(success).append(",");
        json.append("\"ok\":").append(success).append(",");
        json.append("\"message\":\"").append(escapeJson(message)).append("\",");
        json.append("\"discount\":").append(safeDiscount.setScale(0, RoundingMode.HALF_UP).toPlainString());

        if (code != null && !code.isBlank()) {
            json.append(",\"code\":\"").append(escapeJson(code)).append("\"");
        }

        json.append("}");

        resp.getWriter().write(json.toString());
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}

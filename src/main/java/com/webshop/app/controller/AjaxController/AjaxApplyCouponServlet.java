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

    private void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User loggedInUser = (User) session.getAttribute("user");

        if (loggedInUser == null || loggedInUser.getId() <= 0) {
            clearCheckoutCoupon(session);
            writeJson(
                    resp,
                    false,
                    "Vui lòng đăng nhập trước khi sử dụng mã giảm giá.",
                    BigDecimal.ZERO,
                    null
            );
            return;
        }

        String code = getCouponCodeFromRequest(req);

        if (code.isBlank()) {
            clearCheckoutCoupon(session);
            writeJson(
                    resp,
                    false,
                    "Vui lòng nhập mã giảm giá.",
                    BigDecimal.ZERO,
                    null
            );
            return;
        }

        /*
         * BƯỚC 1: Phải kiểm tra mã có tồn tại trong database trước.
         * Nếu không có bước này, mã nhập bừa vẫn có thể bị frontend hiểu nhầm là thành công.
         */
        Coupon coupon = couponDAO.findByCode(code);

        if (coupon == null) {
            clearCheckoutCoupon(session);
            writeJson(
                    resp,
                    false,
                    "Mã khuyến mãi không tồn tại trong hệ thống.",
                    BigDecimal.ZERO,
                    code
            );
            return;
        }

        /*
         * BƯỚC 2: Lấy giỏ hàng để kiểm tra điều kiện đơn hàng.
         */
        Map<String, CartItem> cart = getCouponCart(session);
        BigDecimal subTotal = calculateSubtotal(cart);

        if (subTotal.compareTo(BigDecimal.ZERO) <= 0) {
            clearCheckoutCoupon(session);
            writeJson(
                    resp,
                    false,
                    "Giỏ hàng đang trống, không thể áp dụng mã giảm giá.",
                    BigDecimal.ZERO,
                    code
            );
            return;
        }

        /*
         * BƯỚC 3: Kiểm tra trạng thái mã:
         * - active
         * - còn hạn
         * - còn lượt
         * - đạt giá trị đơn tối thiểu
         */
        String invalidReason = couponDAO.getCouponInvalidReason(coupon, subTotal);

        if (invalidReason != null && !invalidReason.isBlank()) {
            clearCheckoutCoupon(session);
            writeJson(
                    resp,
                    false,
                    invalidReason,
                    BigDecimal.ZERO,
                    code
            );
            return;
        }

        /*
         * BƯỚC 4: Tính tiền giảm.
         */
        BigDecimal discount = checkoutService.calculateCouponDiscount(
                loggedInUser.getId(),
                code,
                subTotal
        );

        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            clearCheckoutCoupon(session);
            writeJson(
                    resp,
                    false,
                    "Mã khuyến mãi không thể áp dụng cho đơn hàng này.",
                    BigDecimal.ZERO,
                    code
            );
            return;
        }

        /*
         * BƯỚC 5: Mã hợp lệ thật sự thì mới lưu session.
         */
        couponDAO.saveVoucherToUserCollection(loggedInUser.getId(), code);

        session.setAttribute(SESSION_CHECKOUT_COUPON, code);
        session.setAttribute(SESSION_CHECKOUT_COUPON_DISCOUNT, discount);

        writeJson(
                resp,
                true,
                "Áp dụng mã giảm giá thành công.",
                discount,
                code
        );
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
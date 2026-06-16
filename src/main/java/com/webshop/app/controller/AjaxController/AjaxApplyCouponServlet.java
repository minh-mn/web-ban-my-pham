package com.webshop.app.controller.AjaxController;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

import com.webshop.app.dao.CouponDAO;
import com.webshop.app.dao.UserCouponDAO;
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

    private static final long serialVersionUID = 1L;

    private static final String ACTION_SAVE = "save";
    private static final String ACTION_APPLY = "apply";

    private static final String SESSION_CHECKOUT_COUPON = "CHECKOUT_COUPON";
    private static final String SESSION_CHECKOUT_COUPON_DISCOUNT = "CHECKOUT_COUPON_DISCOUNT";

    private final CheckoutService checkoutService = new CheckoutService();
    private final CouponDAO couponDAO = new CouponDAO();
    private final UserCouponDAO userCouponDAO = new UserCouponDAO();

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
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");

        HttpSession session = req.getSession(false);

        if (session == null) {
            writeError(resp, "Vui lòng đăng nhập để thực hiện.", null);
            return;
        }

        User loggedInUser = (User) session.getAttribute("user");

        if (loggedInUser == null || loggedInUser.getId() <= 0) {
            clearCheckoutCoupon(session);
            writeError(resp, "Vui lòng đăng nhập để thực hiện.", null);
            return;
        }

        String action = normalizeAction(req.getParameter("action"));
        String couponCode = getCouponCodeFromRequest(req);

        if (isBlank(couponCode)) {
            clearCheckoutCoupon(session);
            writeError(resp, "Vui lòng nhập mã khuyến mãi.", null);
            return;
        }

        Coupon coupon;

        try {
            coupon = couponDAO.findByCode(couponCode);
        } catch (RuntimeException e) {
            writeError(resp, "Không thể kiểm tra mã khuyến mãi lúc này.", couponCode);
            return;
        }

        if (coupon == null) {
            clearCheckoutCoupon(session);
            writeError(resp, "Mã khuyến mãi không tồn tại trong hệ thống.", couponCode);
            return;
        }

        if (ACTION_SAVE.equals(action)) {
            saveCouponToWallet(resp, loggedInUser, couponCode);
            return;
        }

        applyCouponToCheckout(resp, session, loggedInUser, coupon, couponCode);
    }

    private void applyCouponToCheckout(HttpServletResponse resp,
                                       HttpSession session,
                                       User user,
                                       Coupon coupon,
                                       String couponCode) throws IOException {

        Map<String, CartItem> cart = getCouponCart(session);
        BigDecimal subTotal = calculateSubtotal(cart);

        if (subTotal.compareTo(BigDecimal.ZERO) <= 0) {
            clearCheckoutCoupon(session);
            writeError(resp, "Giỏ hàng thanh toán đang trống.", couponCode, subTotal, BigDecimal.ZERO, subTotal);
            return;
        }

        String invalidReason = getCouponInvalidReason(coupon, subTotal);

        if (invalidReason == null && userCouponDAO.hasUserUsedCoupon(user.getId(), coupon.getId())) {
            invalidReason = "Mã ưu đãi này đã hết lượt sử dụng. Vui lòng chọn mã khác.";
        }

        if (invalidReason != null) {
            clearCheckoutCoupon(session);
            writeError(resp, invalidReason, couponCode, subTotal, BigDecimal.ZERO, subTotal);
            return;
        }

        BigDecimal discount;

        try {
            discount = checkoutService.calculateCouponDiscount(
                    user.getId(),
                    couponCode,
                    subTotal
            );
        } catch (RuntimeException e) {
            clearCheckoutCoupon(session);
            writeError(resp, "Không thể tính giảm giá cho mã này lúc này.", couponCode, subTotal, BigDecimal.ZERO, subTotal);
            return;
        }

        discount = money0(discount);

        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            clearCheckoutCoupon(session);
            writeError(
                    resp,
                    "Mã khuyến mãi không đủ điều kiện áp dụng cho đơn hàng hoặc hạng thành viên hiện tại.",
                    couponCode,
                    subTotal,
                    BigDecimal.ZERO,
                    subTotal
            );
            return;
        }

        if (discount.compareTo(subTotal) > 0) {
            discount = subTotal;
        }

        BigDecimal total = subTotal.subtract(discount);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        total = money0(total);

        session.setAttribute(SESSION_CHECKOUT_COUPON, couponCode);
        session.setAttribute(SESSION_CHECKOUT_COUPON_DISCOUNT, discount);
        session.setAttribute("coupon_success", "Áp dụng mã giảm giá thành công.");
        session.removeAttribute("coupon_error");

        writeApplySuccess(resp, couponCode, subTotal, discount, total);
    }

    private void saveCouponToWallet(HttpServletResponse resp,
                                    User user,
                                    String couponCode) throws IOException {

        try {
            boolean saved = couponDAO.saveVoucherToUserCollection(user.getId(), couponCode);

            if (saved) {
                writeSaveSuccess(resp, couponCode, "Đã lưu mã vào ví thành công.");
            } else {
                writeError(resp, "Bạn đã sở hữu mã này hoặc mã không còn đủ điều kiện lưu.", couponCode);
            }
        } catch (RuntimeException e) {
            writeError(resp, "Không thể lưu mã khuyến mãi lúc này.", couponCode);
        }
    }

    private String getCouponInvalidReason(Coupon coupon, BigDecimal subTotal) {
        if (coupon == null) {
            return "Mã khuyến mãi không tồn tại trong hệ thống.";
        }

        if (!coupon.isActive()) {
            return "Mã khuyến mãi hiện không còn hoạt động.";
        }

        LocalDate today = LocalDate.now();

        if (coupon.getStartDate() != null && coupon.getStartDate().isAfter(today)) {
            return "Mã khuyến mãi chưa đến thời gian sử dụng.";
        }

        if (coupon.getEndDate() != null && coupon.getEndDate().isBefore(today)) {
            return "Mã khuyến mãi đã hết hạn.";
        }

        if (coupon.getMaxUses() > 0 && coupon.getUsedCount() >= coupon.getMaxUses()) {
            return "Mã khuyến mãi đã hết lượt sử dụng.";
        }

        BigDecimal safeSubTotal = money0(subTotal);
        BigDecimal minOrderAmount = money0(coupon.getMinOrderAmount());

        if (safeSubTotal.compareTo(minOrderAmount) < 0) {
            return "Đơn hàng chưa đạt giá trị tối thiểu để dùng mã này.";
        }

        return null;
    }

    private String getCouponCodeFromRequest(HttpServletRequest req) {
        String code = req.getParameter("code");

        if (isBlank(code)) {
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

        BigDecimal subTotal = cart.values()
                .stream()
                .filter(item -> item != null && item.getSubtotal() != null)
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return money0(subTotal);
    }

    private BigDecimal money0(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private String normalizeAction(String action) {
        if (action == null || action.trim().isEmpty()) {
            return ACTION_APPLY;
        }

        return action.trim().toLowerCase();
    }

    private String normalizeCouponCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return "";
        }

        return code.trim().toUpperCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void clearCheckoutCoupon(HttpSession session) {
        if (session == null) {
            return;
        }

        session.removeAttribute(SESSION_CHECKOUT_COUPON);
        session.removeAttribute(SESSION_CHECKOUT_COUPON_DISCOUNT);
        session.removeAttribute("coupon_success");
    }

    private void writeApplySuccess(HttpServletResponse resp,
                                   String code,
                                   BigDecimal subtotal,
                                   BigDecimal discount,
                                   BigDecimal total) throws IOException {

        JsonBuilder json = new JsonBuilder();
        json.addBoolean("success", true);
        json.addBoolean("ok", true);
        json.addString("action", ACTION_APPLY);
        json.addString("message", "Áp dụng mã giảm giá thành công.");
        json.addString("code", code);
        json.addNumber("subtotal", subtotal);
        json.addNumber("discount", discount);
        json.addNumber("total", total);

        resp.getWriter().write(json.build());
    }

    private void writeSaveSuccess(HttpServletResponse resp,
                                  String code,
                                  String message) throws IOException {

        JsonBuilder json = new JsonBuilder();
        json.addBoolean("success", true);
        json.addBoolean("ok", true);
        json.addString("action", ACTION_SAVE);
        json.addString("message", message);
        json.addString("code", code);
        json.addNumber("discount", BigDecimal.ZERO);

        resp.getWriter().write(json.build());
    }

    private void writeError(HttpServletResponse resp,
                            String error,
                            String code) throws IOException {
        writeError(resp, error, code, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private void writeError(HttpServletResponse resp,
                            String error,
                            String code,
                            BigDecimal subtotal,
                            BigDecimal discount,
                            BigDecimal total) throws IOException {

        JsonBuilder json = new JsonBuilder();
        json.addBoolean("success", false);
        json.addBoolean("ok", false);
        json.addString("message", error);
        json.addString("error", error);

        if (!isBlank(code)) {
            json.addString("code", code);
        }

        json.addNumber("subtotal", money0(subtotal));
        json.addNumber("discount", money0(discount));
        json.addNumber("total", money0(total));

        resp.getWriter().write(json.build());
    }

    private static class JsonBuilder {

        private final StringBuilder builder = new StringBuilder("{");
        private boolean hasField = false;

        void addString(String key, String value) {
            prefix();
            builder.append('"').append(escapeJson(key)).append("\":");
            builder.append('"').append(escapeJson(value)).append('"');
        }

        void addBoolean(String key, boolean value) {
            prefix();
            builder.append('"').append(escapeJson(key)).append("\":").append(value);
        }

        void addNumber(String key, BigDecimal value) {
            BigDecimal safeValue = value == null ? BigDecimal.ZERO : value.setScale(0, RoundingMode.HALF_UP);
            prefix();
            builder.append('"').append(escapeJson(key)).append("\":").append(safeValue.toPlainString());
        }

        String build() {
            return builder.append('}').toString();
        }

        private void prefix() {
            if (hasField) {
                builder.append(',');
            }

            hasField = true;
        }

        private static String escapeJson(String value) {
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
}

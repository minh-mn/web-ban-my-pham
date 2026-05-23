package com.webshop.app.controller.AjaxController;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import com.webshop.app.dao.CouponDAO;
import com.webshop.app.model.CartItem;
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
            writeJson(
                    resp,
                    false,
                    "Vui lòng đăng nhập trước khi sử dụng mã giảm giá.",
                    BigDecimal.ZERO,
                    null
            );
            return;
        }

        String code = normalizeCouponCode(req.getParameter("code"));

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
         * Lấy giỏ hàng để tính thử discount.
         * Ưu tiên selected cart ở checkout; nếu không có thì fallback về full cart.
         */
        Map<String, CartItem> cart = getCouponCart(session);
        BigDecimal subTotal = calculateSubtotal(cart);

        /*
         * Nếu có giỏ hàng thì đây là hành động apply coupon thật sự.
         * Phải kiểm tra theo:
         * - coupon active / còn hạn / còn lượt
         * - min_order_amount
         * - min_rank_code của user hiện tại
         */
        if (subTotal.compareTo(BigDecimal.ZERO) > 0) {
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
                        "Mã giảm giá không hợp lệ, đã hết hạn, hết lượt dùng, đơn chưa đủ tối thiểu hoặc hạng thành viên chưa phù hợp.",
                        BigDecimal.ZERO,
                        code
                );
                return;
            }

            /*
             * Lưu mã vào ví user.
             * Nếu user đã lưu trước đó, INSERT IGNORE trả về false nhưng coupon vẫn hợp lệ,
             * nên vẫn cho apply vào checkout.
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
            return;
        }

        /*
         * Nếu chưa có giỏ hàng, xem như user chỉ bấm lưu voucher vào ví.
         * Không kiểm tra min_order_amount tại đây vì chưa có tổng đơn hàng.
         */
        boolean saved = couponDAO.saveVoucherToUserCollection(loggedInUser.getId(), code);

        if (saved) {
            writeJson(
                    resp,
                    true,
                    "Lưu mã giảm giá thành công.",
                    BigDecimal.ZERO,
                    code
            );
        } else {
            /*
             * INSERT IGNORE có thể trả về false khi mã đã được lưu trước đó.
             * Vẫn trả success để frontend không báo lỗi sai cho user.
             */
            writeJson(
                    resp,
                    true,
                    "Mã giảm giá đã có trong ví của bạn hoặc đã được lưu trước đó.",
                    BigDecimal.ZERO,
                    code
            );
        }
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
        json.append("\"success\":").append(success).append(",");
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
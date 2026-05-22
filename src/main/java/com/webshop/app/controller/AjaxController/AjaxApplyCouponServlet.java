package com.webshop.app.controller.AjaxController;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import com.webshop.app.model.CartItem;
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

    private final CheckoutService checkoutService = new CheckoutService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);

        if (session == null) {
            writeError(resp, "Phiên làm việc đã hết hạn. Vui lòng thử lại.");
            return;
        }

        /*
         * Mục 68:
         * Chỉ lấy các sản phẩm đã được tích chọn để tính coupon.
         */
        Map<String, CartItem> cart = CartUtil.getSelectedCart(session);

        if (cart == null || cart.isEmpty()) {
            writeError(resp, "Vui lòng chọn ít nhất một sản phẩm để áp dụng mã giảm giá.");
            return;
        }

        BigDecimal subTotal = calcSubTotal(cart);

        String code = req.getParameter("code");

        /*
         * Không nhập mã:
         * - Xóa coupon khỏi session.
         * - Trả về tổng tiền gốc.
         */
        if (isBlank(code)) {
            session.removeAttribute("CHECKOUT_COUPON");
            session.removeAttribute("CHECKOUT_COUPON_DISCOUNT");

            writeJson(resp, subTotal, BigDecimal.ZERO, subTotal);
            return;
        }

        code = code.trim();

        BigDecimal discount = checkoutService.calculateCouponDiscount(code, subTotal);

        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            session.removeAttribute("CHECKOUT_COUPON");
            session.removeAttribute("CHECKOUT_COUPON_DISCOUNT");

            writeError(resp, "Mã không hợp lệ hoặc đã hết hạn.");
            return;
        }

        discount = discount.setScale(0, RoundingMode.HALF_UP);

        BigDecimal total = subTotal.subtract(discount);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        total = total.setScale(0, RoundingMode.HALF_UP);

        /*
         * Lưu coupon vào session để CheckoutServlet dùng khi đặt hàng.
         */
        session.setAttribute("CHECKOUT_COUPON", code);
        session.setAttribute("CHECKOUT_COUPON_DISCOUNT", discount);

        writeJson(resp, subTotal, discount, total);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        doGet(req, resp);
    }

    private BigDecimal calcSubTotal(Map<String, CartItem> cart) {
        BigDecimal subTotal = BigDecimal.ZERO;

        if (cart == null || cart.isEmpty()) {
            return subTotal;
        }

        for (CartItem item : cart.values()) {
            if (item != null && item.getSubtotal() != null) {
                subTotal = subTotal.add(item.getSubtotal());
            }
        }

        return subTotal.setScale(0, RoundingMode.HALF_UP);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void writeJson(
            HttpServletResponse resp,
            BigDecimal subTotal,
            BigDecimal discount,
            BigDecimal total
    ) throws IOException {

        resp.getWriter().write(
                "{"
                        + "\"subtotal\":" + subTotal + ","
                        + "\"discount\":" + discount + ","
                        + "\"total\":" + total
                        + "}"
        );
    }

    private void writeError(HttpServletResponse resp, String message)
            throws IOException {

        resp.getWriter().write(
                "{"
                        + "\"error\":\"" + escapeJson(message) + "\""
                        + "}"
        );
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
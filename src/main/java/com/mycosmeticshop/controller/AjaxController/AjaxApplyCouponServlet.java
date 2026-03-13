package com.mycosmeticshop.controller.AjaxController;

import com.mycosmeticshop.model.CartItem;
import com.mycosmeticshop.service.CheckoutService;
import com.mycosmeticshop.utils.CartUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@WebServlet("/ajax/apply-coupon")
public class AjaxApplyCouponServlet extends HttpServlet {

    private final CheckoutService checkoutService = new CheckoutService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession();
        Map<Integer, CartItem> cart = CartUtil.getCart(session);

        // ===== 1) CHECK CART =====
        if (cart == null || cart.isEmpty()) {
            resp.getWriter().write("{\"error\":\"Giỏ hàng trống\"}");
            return;
        }

        // ===== 2) SUBTOTAL (LUÔN CÓ) =====
        BigDecimal subTotal = cart.values().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(0, RoundingMode.HALF_UP);

        String code = req.getParameter("code");

        // ===== 3) KHÔNG NHẬP MÃ → trả tổng tiền mặc định =====
        if (code == null || code.isBlank()) {
            writeJson(resp, subTotal, BigDecimal.ZERO, subTotal);
            return;
        }

        // ===== 4) TÍNH GIẢM GIÁ QUA SERVICE (CHUẨN) =====
        BigDecimal discount =
                checkoutService.calculateCouponDiscount(code.trim(), subTotal);

        if (discount == null) {
            resp.getWriter().write("{\"error\":\"Mã không hợp lệ hoặc đã hết hạn\"}");
            return;
        }

        BigDecimal total = subTotal.subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        writeJson(resp, subTotal, discount, total);
    }

    // ===== helper JSON =====
    private void writeJson(HttpServletResponse resp,
                           BigDecimal subTotal,
                           BigDecimal discount,
                           BigDecimal total) throws IOException {

        resp.getWriter().write(
                "{"
                        + "\"subtotal\":" + subTotal + ","
                        + "\"discount\":" + discount + ","
                        + "\"total\":" + total
                        + "}"
        );
    }
}

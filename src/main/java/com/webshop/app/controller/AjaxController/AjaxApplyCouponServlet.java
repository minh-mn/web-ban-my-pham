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

    private final CheckoutService checkoutService = new CheckoutService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User loggedInUser = (User) session.getAttribute("user");

        // 1. Kiểm tra đăng nhập
        if (loggedInUser == null) {
            resp.getWriter().write("{\"success\": false, \"message\": \"Vui lòng đăng nhập trước!\"}");
            return;
        }

        // 2. Kiểm tra mã hợp lệ gửi lên
        String code = req.getParameter("code");
        if (code == null || code.isBlank()) {
            resp.getWriter().write("{\"success\": false, \"message\": \"Mã giảm giá không hợp lệ!\"}");
            return;
        }

        // 3. Tiến hành gọi DAO lưu cặp (userId, couponId) vào bảng trung gian user_coupon dưới DB
        CouponDAO couponDAO = new CouponDAO();
        couponDAO.saveVoucherToUserCollection(loggedInUser.getId(), code);

        // 4. Tính toán số tiền được giảm giá dựa trên giỏ hàng hiện tại (nếu có)
        // Việc này giúp đảm bảo JavaScript ở home.jsp lấy được thuộc tính 'data.discount' mà không bị lỗi crash
        BigDecimal discount = BigDecimal.ZERO;
        Map<String, CartItem> cart = CartUtil.getCart(session);

        if (cart != null && !cart.isEmpty()) {
            BigDecimal subTotal = cart.values().stream()
                    .map(CartItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(0, RoundingMode.HALF_UP);

            BigDecimal calculatedDiscount = checkoutService.calculateCouponDiscount(code.trim(), subTotal);
            if (calculatedDiscount != null) {
                discount = calculatedDiscount;
            }
        }

        // 5. Trả về một chuỗi JSON duy nhất, hợp lệ hoàn toàn cho Frontend xử lý
        resp.getWriter().write(
                "{"
                        + "\"success\":true,"
                        + "\"message\":\"Lưu mã giảm giá thành công!\","
                        + "\"discount\":" + discount
                        + "}"
        );
    }
}

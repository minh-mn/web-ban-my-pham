package com.webshop.app.controller.OrderController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import com.webshop.app.model.CartItem;
import com.webshop.app.model.User;
import com.webshop.app.service.CheckoutService;
import com.webshop.app.utils.CartUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final CheckoutService checkoutService = new CheckoutService();

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

        return subTotal;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Lấy giỏ hàng đã được tích chọn.
     * Nếu chưa có sản phẩm được chọn thì redirect về cart.
     */
    private Map<String, CartItem> getSelectedCheckoutCart(
            HttpServletRequest req,
            HttpServletResponse resp,
            HttpSession session
    ) throws IOException {

        Map<String, CartItem> selectedCart = CartUtil.getSelectedCart(session);

        if (selectedCart == null || selectedCart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart?selectRequired=1");
            return null;
        }

        return selectedCart;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);

        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || user.getId() <= 0) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }

        /*
         * Mục 68:
         * Checkout chỉ hiển thị các sản phẩm người dùng đã tích chọn ở cart.
         */
        Map<String, CartItem> cart = getSelectedCheckoutCart(req, resp, session);

        if (cart == null) {
            return;
        }

        BigDecimal subTotal = calcSubTotal(cart);

        String appliedCoupon = (String) session.getAttribute("CHECKOUT_COUPON");

        BigDecimal couponDiscount =
                (BigDecimal) session.getAttribute("CHECKOUT_COUPON_DISCOUNT");

        if (couponDiscount == null) {
            couponDiscount = BigDecimal.ZERO;
        }

        BigDecimal total = subTotal.subtract(couponDiscount);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        req.setAttribute("cart", cart);
        req.setAttribute("subtotal", subTotal);
        req.setAttribute("subTotal", subTotal);

        req.setAttribute("couponCode", appliedCoupon);
        req.setAttribute("couponDiscount", couponDiscount);
        req.setAttribute("total", total);

        req.setAttribute("coupon_success", session.getAttribute("coupon_success"));
        req.setAttribute("coupon_error", session.getAttribute("coupon_error"));

        session.removeAttribute("coupon_success");
        session.removeAttribute("coupon_error");

        req.setAttribute("pageTitle", "MyCosmetic | Thanh toán");
        req.setAttribute("pageCss", "/checkout.css");
        req.setAttribute("pageContent", "/jsp/checkout/checkout.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();

        User user = (User) session.getAttribute("user");

        if (user == null || user.getId() <= 0) {
            session.removeAttribute("user");
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }

        /*
         * Mục 68:
         * Khi đặt hàng, chỉ lấy các sản phẩm đã tích chọn.
         */
        Map<String, CartItem> cart = getSelectedCheckoutCart(req, resp, session);

        if (cart == null) {
            return;
        }

        /*
         * Legacy apply-coupon:
         * Nếu form checkout vẫn submit action=apply-coupon thì tính theo selected cart.
         */
        String action = req.getParameter("action");

        if ("apply-coupon".equalsIgnoreCase(action)) {
            String couponCode = req.getParameter("couponCode");

            if (isBlank(couponCode)) {
                session.removeAttribute("CHECKOUT_COUPON");
                session.removeAttribute("CHECKOUT_COUPON_DISCOUNT");
                session.setAttribute("coupon_error", "Vui lòng nhập mã khuyến mãi");

                resp.sendRedirect(req.getContextPath() + "/checkout");
                return;
            }

            couponCode = couponCode.trim();

            BigDecimal subTotal = calcSubTotal(cart);
            BigDecimal discount = checkoutService.calculateCouponDiscount(couponCode, subTotal);

            if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
                session.removeAttribute("CHECKOUT_COUPON");
                session.removeAttribute("CHECKOUT_COUPON_DISCOUNT");
                session.setAttribute("coupon_error", "Mã khuyến mãi không hợp lệ hoặc đã hết hạn");
            } else {
                session.setAttribute("CHECKOUT_COUPON", couponCode);
                session.setAttribute("CHECKOUT_COUPON_DISCOUNT", discount);
                session.setAttribute("coupon_success", "Áp dụng mã thành công");
            }

            resp.sendRedirect(req.getContextPath() + "/checkout");
            return;
        }

        String fullName = req.getParameter("fullName");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");
        String paymentMethod = req.getParameter("paymentMethod");

        if (isBlank(fullName) || isBlank(phone) || isBlank(address)) {
            session.setAttribute("coupon_error", "Vui lòng nhập đầy đủ thông tin người nhận.");
            resp.sendRedirect(req.getContextPath() + "/checkout");
            return;
        }

        if (isBlank(paymentMethod)) {
            paymentMethod = "COD";
        }

        String couponCode = req.getParameter("couponCode");

        if (isBlank(couponCode)) {
            couponCode = (String) session.getAttribute("CHECKOUT_COUPON");
        }

        if (!isBlank(couponCode)) {
            couponCode = couponCode.trim();
        }

        try {
            int orderId = checkoutService.checkout(
                    user.getId(),
                    cart,
                    fullName.trim(),
                    phone.trim(),
                    address.trim(),
                    paymentMethod,
                    couponCode
            );

            if (orderId <= 0) {
                resp.sendRedirect(req.getContextPath()
                        + "/checkout/success?success=false&message=order_create_failed");
                return;
            }

            if ("COD".equalsIgnoreCase(paymentMethod)) {
                /*
                 * Quan trọng:
                 * Chỉ xóa các sản phẩm đã tích chọn và đã đặt hàng.
                 * Các sản phẩm chưa chọn vẫn giữ lại trong giỏ.
                 */
                CartUtil.removeItems(session, cart.keySet());
                CartUtil.clearSelectedCartKeys(session);

                session.removeAttribute("CHECKOUT_COUPON");
                session.removeAttribute("CHECKOUT_COUPON_DISCOUNT");

                resp.sendRedirect(req.getContextPath()
                        + "/checkout/success?success=true&orderId=" + orderId
                        + "&method=COD");
                return;
            }

            if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
                session.setAttribute("VNP_ORDER_ID", orderId);

                if (!isBlank(couponCode)) {
                    session.setAttribute("VNP_COUPON", couponCode);
                } else {
                    session.removeAttribute("VNP_COUPON");
                }

                /*
                 * Chỉ snapshot các sản phẩm đã tích chọn để VNPAY finalize.
                 */
                session.setAttribute("VNP_CART", new LinkedHashMap<>(cart));

                resp.sendRedirect(req.getContextPath() + "/vnpay/payment");
                return;
            }

            /*
             * Fallback cho các phương thức thanh toán khác nếu có.
             */
            CartUtil.removeItems(session, cart.keySet());
            CartUtil.clearSelectedCartKeys(session);

            session.removeAttribute("CHECKOUT_COUPON");
            session.removeAttribute("CHECKOUT_COUPON_DISCOUNT");

            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=true&orderId=" + orderId
                    + "&method=" + paymentMethod);

        } catch (Exception e) {
            e.printStackTrace();

            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=false&message=checkout_failed");
        }
    }
}
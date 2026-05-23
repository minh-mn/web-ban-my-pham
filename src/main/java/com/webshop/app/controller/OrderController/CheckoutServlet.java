package com.webshop.app.controller.OrderController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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

    private static final String DEFAULT_SHIPPING_METHOD = "ECONOMY";

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

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private String normalizeShippingMethod(String shippingMethod) {
        String method = trim(shippingMethod).toUpperCase();

        Set<String> validShippingMethods = Set.of("ECONOMY", "FAST", "EXPRESS");

        if (validShippingMethods.contains(method)) {
            return method;
        }

        return DEFAULT_SHIPPING_METHOD;
    }

    private BigDecimal parseShippingFee(String shippingFeeRaw) {
        if (isBlank(shippingFeeRaw)) {
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal fee = new BigDecimal(shippingFeeRaw.trim());

            if (fee.compareTo(BigDecimal.ZERO) < 0) {
                return BigDecimal.ZERO;
            }

            return fee;
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
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

    private BigDecimal getCheckoutCouponDiscount(HttpSession session) {
        BigDecimal couponDiscount =
                (BigDecimal) session.getAttribute("CHECKOUT_COUPON_DISCOUNT");

        return couponDiscount != null ? couponDiscount : BigDecimal.ZERO;
    }

    private BigDecimal calcTotal(BigDecimal subTotal, BigDecimal discount) {
        BigDecimal total = subTotal.subtract(discount);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        return total;
    }

    private void prepareCheckoutView(HttpServletRequest req,
                                     HttpSession session,
                                     Map<String, CartItem> cart) {

        BigDecimal subTotal = calcSubTotal(cart);

        String appliedCoupon = (String) session.getAttribute("CHECKOUT_COUPON");
        BigDecimal couponDiscount = getCheckoutCouponDiscount(session);
        BigDecimal total = calcTotal(subTotal, couponDiscount);

        req.setAttribute("cart", cart);
        req.setAttribute("selectedCart", cart);

        req.setAttribute("subtotal", subTotal);
        req.setAttribute("subTotal", subTotal);

        req.setAttribute("couponCode", appliedCoupon);
        req.setAttribute("couponDiscount", couponDiscount);
        req.setAttribute("discount", couponDiscount);
        req.setAttribute("total", total);

        req.setAttribute("coupon_success", session.getAttribute("coupon_success"));
        req.setAttribute("coupon_error", session.getAttribute("coupon_error"));

        session.removeAttribute("coupon_success");
        session.removeAttribute("coupon_error");

        req.setAttribute("pageTitle", "MyCosmetic | Thanh toán");
        req.setAttribute("pageCss", "/checkout.css");
        req.setAttribute("pageContent", "/jsp/checkout/checkout.jsp");
    }

    private void keepFormValues(HttpServletRequest req) {
        req.setAttribute("formFullName", trim(req.getParameter("fullName")));
        req.setAttribute("formPhone", trim(req.getParameter("phone")));
        req.setAttribute("formAddress", trim(req.getParameter("address")));

        req.setAttribute("formLocationText", trim(req.getParameter("locationText")));
        req.setAttribute("formProvince", trim(req.getParameter("province")));
        req.setAttribute("formProvinceCode", trim(req.getParameter("provinceCode")));
        req.setAttribute("formWardName", trim(req.getParameter("wardName")));
        req.setAttribute("formWardCode", trim(req.getParameter("wardCode")));
        req.setAttribute("formShippingAddress", trim(req.getParameter("shippingAddress")));

        req.setAttribute("formShippingMethod",
                normalizeShippingMethod(req.getParameter("shippingMethod")));

        req.setAttribute("formShippingFee",
                parseShippingFee(req.getParameter("shippingFee")));
    }

    private Map<String, String> validateCheckoutForm(HttpServletRequest req,
                                                     Map<String, CartItem> checkoutCart) {
        Map<String, String> errors = new HashMap<>();

        String fullName = trim(req.getParameter("fullName"));
        String phone = trim(req.getParameter("phone"));
        String address = trim(req.getParameter("address"));

        String province = trim(req.getParameter("province"));
        String provinceCode = trim(req.getParameter("provinceCode"));
        String wardName = trim(req.getParameter("wardName"));
        String wardCode = trim(req.getParameter("wardCode"));

        String paymentMethod = trim(req.getParameter("paymentMethod"));
        String shippingMethod = normalizeShippingMethod(req.getParameter("shippingMethod"));

        if (checkoutCart == null || checkoutCart.isEmpty()) {
            errors.put("general", "Không có sản phẩm nào để thanh toán.");
        }

        if (isBlank(fullName)) {
            errors.put("fullName", "Vui lòng nhập họ và tên người nhận.");
        } else if (fullName.length() < 2 || fullName.length() > 80) {
            errors.put("fullName", "Họ và tên phải từ 2 đến 80 ký tự.");
        } else if (!fullName.matches("^[\\p{L}\\s'.-]+$")) {
            errors.put("fullName", "Họ và tên chỉ nên chứa chữ cái và khoảng trắng.");
        }

        if (isBlank(phone)) {
            errors.put("phone", "Vui lòng nhập số điện thoại.");
        } else if (!phone.matches("^0(3|5|7|8|9)[0-9]{8}$")) {
            errors.put("phone", "Số điện thoại không hợp lệ. Ví dụ: 0912345678.");
        }

        if (isBlank(address)) {
            errors.put("address", "Vui lòng nhập địa chỉ giao hàng.");
        } else if (address.length() < 5 || address.length() > 160) {
            errors.put("address", "Địa chỉ phải từ 5 đến 160 ký tự.");
        }

        if (isBlank(province) || isBlank(provinceCode)) {
            errors.put("location", "Vui lòng chọn Tỉnh/TP.");
        } else if (isBlank(wardName) || isBlank(wardCode)) {
            errors.put("location", "Vui lòng chọn Phường/Xã sau khi chọn Tỉnh/TP.");
        }

        Set<String> validPaymentMethods = Set.of("COD", "VNPAY");

        if (isBlank(paymentMethod)) {
            errors.put("paymentMethod", "Vui lòng chọn phương thức thanh toán.");
        } else if (!validPaymentMethods.contains(paymentMethod)) {
            errors.put("paymentMethod", "Phương thức thanh toán không hợp lệ.");
        }

        Set<String> validShippingMethods = Set.of("ECONOMY", "FAST", "EXPRESS");

        if (!validShippingMethods.contains(shippingMethod)) {
            errors.put("shippingMethod", "Phương thức vận chuyển không hợp lệ.");
        }

        return errors;
    }

    private String buildFinalShippingAddress(HttpServletRequest req) {
        String address = trim(req.getParameter("address"));
        String wardName = trim(req.getParameter("wardName"));
        String province = trim(req.getParameter("province"));
        String shippingAddress = trim(req.getParameter("shippingAddress"));

        if (!isBlank(shippingAddress)) {
            return shippingAddress;
        }

        StringBuilder builder = new StringBuilder();

        if (!isBlank(address)) {
            builder.append(address);
        }

        if (!isBlank(wardName)) {
            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(wardName);
        }

        if (!isBlank(province)) {
            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(province);
        }

        return builder.toString();
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

        prepareCheckoutView(req, session, cart);

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

        /*
         * Mục 79:
         * Validate dữ liệu thanh toán ở backend.
         */
        Map<String, String> errors = validateCheckoutForm(req, cart);

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);

            keepFormValues(req);
            prepareCheckoutView(req, session, cart);

            req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
            return;
        }

        String fullName = trim(req.getParameter("fullName"));
        String phone = trim(req.getParameter("phone"));
        String finalAddress = buildFinalShippingAddress(req);
        String paymentMethod = trim(req.getParameter("paymentMethod"));

        String province = trim(req.getParameter("province"));

        String shippingMethod = normalizeShippingMethod(req.getParameter("shippingMethod"));
        BigDecimal submittedShippingFee = parseShippingFee(req.getParameter("shippingFee"));

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
                    fullName,
                    phone,
                    finalAddress,
                    paymentMethod,
                    couponCode,
                    shippingMethod,
                    submittedShippingFee,
                    province
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
             * Fallback an toàn: về lý thuyết không chạy tới đây vì đã validate COD/VNPAY.
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
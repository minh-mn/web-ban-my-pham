package com.webshop.app.controller.OrderController;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.webshop.app.dao.CouponDAO;
import com.webshop.app.model.CartItem;
import com.webshop.app.model.Coupon;
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

    private static final String SESSION_CHECKOUT_COUPON = "CHECKOUT_COUPON";
    private static final String SESSION_CHECKOUT_COUPON_DISCOUNT = "CHECKOUT_COUPON_DISCOUNT";

    private final CheckoutService checkoutService = new CheckoutService();
    private final CouponDAO couponDAO = new CouponDAO();

    /* =========================================================
       CART / MONEY HELPERS
    ========================================================= */

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

    private BigDecimal calcTotal(BigDecimal subTotal, BigDecimal discount) {
        BigDecimal safeSubTotal = subTotal != null ? subTotal : BigDecimal.ZERO;
        BigDecimal safeDiscount = discount != null ? discount : BigDecimal.ZERO;

        BigDecimal total = safeSubTotal.subtract(safeDiscount);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        return total.setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Tính số tiền giảm thực tế của coupon theo tổng tiền hàng hiện tại.
     *
     * Công thức:
     * discount = subtotal * discountPercent / 100
     * nếu maxDiscountAmount > 0 thì discount = min(discount, maxDiscountAmount)
     *
     * Ví dụ subtotal = 399.200:
     * - FREESHIP10: 10%, tối đa 30.000 => 30.000
     * - NEWYEAR10: 10%, tối đa 50.000 => 39.920
     * - GIAM20K: 10%, tối đa 20.000 => 20.000
     * - GIAM50K: 15%, tối đa 50.000, đơn từ 300.000 => 50.000
     *
     * Vậy GIAM50K mới là mã tốt nhất nếu đang active và đủ điều kiện.
     */
    private BigDecimal estimateCouponDiscount(Coupon coupon, BigDecimal subTotal) {
        if (coupon == null || subTotal == null || subTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal safeSubTotal = subTotal.setScale(0, RoundingMode.HALF_UP);

        BigDecimal minOrderAmount = coupon.getMinOrderAmount() != null
                ? coupon.getMinOrderAmount()
                : BigDecimal.ZERO;

        /*
         * Quan trọng:
         * Nếu đơn chưa đủ điều kiện tối thiểu thì mã này không được tính là tốt nhất.
         */
        if (safeSubTotal.compareTo(minOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }

        int percentValue = Math.max(coupon.getDiscountPercent(), 0);

        if (percentValue <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = safeSubTotal
                .multiply(BigDecimal.valueOf(percentValue))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        BigDecimal maxDiscountAmount = coupon.getMaxDiscountAmount() != null
                ? coupon.getMaxDiscountAmount()
                : BigDecimal.ZERO;

        if (maxDiscountAmount.compareTo(BigDecimal.ZERO) > 0) {
            discount = discount.min(maxDiscountAmount);
        }

        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return discount.setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal getCheckoutCouponDiscount(HttpSession session) {
        BigDecimal couponDiscount =
                (BigDecimal) session.getAttribute(SESSION_CHECKOUT_COUPON_DISCOUNT);

        return couponDiscount != null ? couponDiscount : BigDecimal.ZERO;
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

    /* =========================================================
       STRING / NORMALIZE HELPERS
    ========================================================= */

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeCouponCode(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return "";
        }

        return couponCode.trim().toUpperCase();
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

            return fee.setScale(0, RoundingMode.HALF_UP);

        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /* =========================================================
       COUPON HELPERS
    ========================================================= */

    private void clearCheckoutCoupon(HttpSession session) {
        session.removeAttribute(SESSION_CHECKOUT_COUPON);
        session.removeAttribute(SESSION_CHECKOUT_COUPON_DISCOUNT);
    }

    private boolean isCouponConditionMessage(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }

        return message.contains("Mã giảm giá")
                || message.contains("mã giảm giá")
                || message.contains("Mã khuyến mãi")
                || message.contains("mã khuyến mãi")
                || message.contains("Hạng thành viên")
                || message.contains("hạng thành viên")
                || message.contains("Đơn hàng")
                || message.contains("đơn hàng");
    }

    private boolean isCouponBaseUsable(Coupon coupon) {
        if (coupon == null) {
            return false;
        }

        if (!coupon.isActive()) {
            return false;
        }

        LocalDate today = LocalDate.now();

        if (coupon.getStartDate() != null && coupon.getStartDate().isAfter(today)) {
            return false;
        }

        if (coupon.getEndDate() != null && coupon.getEndDate().isBefore(today)) {
            return false;
        }

        return coupon.getMaxUses() <= 0 || coupon.getUsedCount() < coupon.getMaxUses();
    }

    private boolean isCouponUsableForCurrentOrder(Coupon coupon, BigDecimal subTotal) {
        if (!isCouponBaseUsable(coupon)) {
            return false;
        }

        BigDecimal safeSubTotal = subTotal != null ? subTotal : BigDecimal.ZERO;

        BigDecimal minOrderAmount = coupon.getMinOrderAmount() != null
                ? coupon.getMinOrderAmount()
                : BigDecimal.ZERO;

        return safeSubTotal.compareTo(minOrderAmount) >= 0;
    }

    /**
     * Validate riêng cho mục 72 - nhập mã giảm giá thủ công.
     *
     * Mục tiêu:
     * - Không cho áp dụng mã không tồn tại trong database.
     * - Phân biệt lỗi rõ ràng: bị tắt, chưa tới ngày dùng, hết hạn, hết lượt, chưa đủ đơn tối thiểu.
     * - Rank/min_rank_code vẫn để CheckoutService/CouponService kiểm tra bằng overload có userId.
     */
    private String validateManualCouponBaseCondition(String couponCode, BigDecimal subTotal) {
        Coupon coupon = couponDAO.findByCode(couponCode);

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

        BigDecimal safeSubTotal = subTotal != null ? subTotal : BigDecimal.ZERO;
        BigDecimal minOrderAmount = coupon.getMinOrderAmount() != null
                ? coupon.getMinOrderAmount()
                : BigDecimal.ZERO;

        if (safeSubTotal.compareTo(minOrderAmount) < 0) {
            return "Đơn hàng chưa đạt giá trị tối thiểu để dùng mã này.";
        }

        return null;
    }

    /**
     * Load coupon cho checkout.
     *
     * Logic đúng:
     * - Không hard-code mã tốt nhất.
     * - Mã "Tốt nhất" = mã dùng được và giảm thực tế nhiều tiền nhất theo đơn hiện tại.
     * - Mã dùng được nằm trên.
     * - Mã chưa đủ điều kiện nằm dưới, JSP có thể render dạng mờ.
     */
    private void loadCouponsForModal(HttpServletRequest req,
                                     int userId,
                                     BigDecimal subTotal) {

        List<Coupon> savedCoupons = new ArrayList<>();
        List<Coupon> availableCoupons = new ArrayList<>();
        List<Coupon> allCoupons = new ArrayList<>();

        Map<String, BigDecimal> couponEstimatedDiscountMap = new HashMap<>();
        Map<String, Boolean> couponUsableMap = new HashMap<>();

        BigDecimal safeSubTotal = subTotal != null ? subTotal : BigDecimal.ZERO;
        String bestCouponCode = "";

        try {
            if (userId > 0) {
                savedCoupons = couponDAO.findSavedCouponsByUserId(userId);
            }

            /*
             * Lấy tất cả mã còn active, còn hạn, còn lượt.
             * Không lọc min_order_amount để modal vẫn hiển thị mã chưa đủ điều kiện ở trạng thái mờ.
             */
            allCoupons = couponDAO.findAllActiveCouponsForCheckout();

            /*
             * Tính trước trạng thái usable và số tiền giảm thực tế cho từng mã.
             */
            for (Coupon coupon : allCoupons) {
                if (coupon == null || coupon.getCode() == null) {
                    continue;
                }

                String code = coupon.getCode();

                boolean usable = isCouponUsableForCurrentOrder(coupon, safeSubTotal);
                BigDecimal estimatedDiscount = usable
                        ? estimateCouponDiscount(coupon, safeSubTotal)
                        : BigDecimal.ZERO;

                couponUsableMap.put(code, usable);
                couponEstimatedDiscountMap.put(code, estimatedDiscount);
            }

            /*
             * Sort coupon theo đúng UX:
             * 1. Mã dùng được lên trước.
             * 2. Mã giảm thực tế nhiều tiền nhất lên đầu.
             * 3. Nếu giảm bằng nhau, ưu tiên mã yêu cầu đơn tối thiểu thấp hơn.
             * 4. Nếu vẫn bằng nhau, sort theo code để ổn định.
             */
            allCoupons.sort((a, b) -> {
                String codeA = a != null && a.getCode() != null ? a.getCode() : "";
                String codeB = b != null && b.getCode() != null ? b.getCode() : "";

                boolean usableA = couponUsableMap.getOrDefault(codeA, false);
                boolean usableB = couponUsableMap.getOrDefault(codeB, false);

                if (usableA != usableB) {
                    return usableA ? -1 : 1;
                }

                BigDecimal discountA = couponEstimatedDiscountMap.getOrDefault(codeA, BigDecimal.ZERO);
                BigDecimal discountB = couponEstimatedDiscountMap.getOrDefault(codeB, BigDecimal.ZERO);

                int compareDiscount = discountB.compareTo(discountA);

                if (compareDiscount != 0) {
                    return compareDiscount;
                }

                BigDecimal minA = a != null && a.getMinOrderAmount() != null
                        ? a.getMinOrderAmount()
                        : BigDecimal.ZERO;

                BigDecimal minB = b != null && b.getMinOrderAmount() != null
                        ? b.getMinOrderAmount()
                        : BigDecimal.ZERO;

                int compareMinOrder = minA.compareTo(minB);

                if (compareMinOrder != 0) {
                    return compareMinOrder;
                }

                return codeA.compareToIgnoreCase(codeB);
            });

            /*
             * Mã đầu tiên sau khi sort, nếu dùng được và giảm > 0, là mã tốt nhất.
             */
            for (Coupon coupon : allCoupons) {
                if (coupon == null || coupon.getCode() == null) {
                    continue;
                }

                String code = coupon.getCode();

                BigDecimal estimatedDiscount = couponEstimatedDiscountMap.getOrDefault(
                        code,
                        BigDecimal.ZERO
                );

                boolean usable = couponUsableMap.getOrDefault(code, false);

                if (usable && estimatedDiscount.compareTo(BigDecimal.ZERO) > 0) {
                    bestCouponCode = code;
                    break;
                }
            }

            /*
             * Giữ lại availableCoupons để tương thích JSP cũ nếu còn dùng.
             */
            availableCoupons = couponDAO.findAvailableCouponsForCheckout(safeSubTotal);

        } catch (RuntimeException e) {
            /*
             * Không để lỗi coupon làm hỏng toàn bộ trang checkout.
             */
            e.printStackTrace();
            req.setAttribute("couponLoadError", "Không thể tải danh sách mã khuyến mãi.");
        }

        /*
         * Biến cũ: giữ để JSP cũ không lỗi.
         */
        req.setAttribute("savedCoupons", savedCoupons);
        req.setAttribute("availableCoupons", availableCoupons);
        req.setAttribute("allCoupons", allCoupons);

        /*
         * Biến mới: JSP nên dùng couponOptions để render 1 danh sách duy nhất.
         */
        req.setAttribute("checkoutCoupons", allCoupons);
        req.setAttribute("couponOptions", allCoupons);

        /*
         * Biến hỗ trợ JSP:
         * - bestCouponCode: mã tốt nhất theo giá trị đơn hàng hiện tại.
         * - couponEstimatedDiscountMap: số tiền giảm ước lượng của từng mã.
         * - couponUsableMap: mã có đủ điều kiện theo đơn hiện tại không.
         */
        req.setAttribute("bestCouponCode", bestCouponCode);
        req.setAttribute("couponEstimatedDiscountMap", couponEstimatedDiscountMap);
        req.setAttribute("couponUsableMap", couponUsableMap);
    }

    private boolean applyCoupon(HttpServletRequest req,
                                HttpServletResponse resp,
                                HttpSession session,
                                User user,
                                Map<String, CartItem> cart) throws IOException {

        String couponCode = normalizeCouponCode(req.getParameter("couponCode"));

        if (isBlank(couponCode)) {
            clearCheckoutCoupon(session);
            session.setAttribute("coupon_error", "Vui lòng nhập mã khuyến mãi.");

            resp.sendRedirect(req.getContextPath() + "/checkout");
            return true;
        }

        BigDecimal subTotal = calcSubTotal(cart);

        /*
         * Mục 72:
         * Kiểm tra mã nhập thủ công phải tồn tại trong hệ thống trước.
         */
        String baseError = validateManualCouponBaseCondition(couponCode, subTotal);

        if (baseError != null) {
            clearCheckoutCoupon(session);
            session.setAttribute("coupon_error", baseError);

            resp.sendRedirect(req.getContextPath() + "/checkout");
            return true;
        }

        /*
         * Kiểm tra tiếp các điều kiện nghiệp vụ nâng cao:
         * - min_rank_code
         * - rule tính discount
         * - các rule trong CouponService/CheckoutService
         */
        BigDecimal discount = checkoutService.calculateCouponDiscount(
                user.getId(),
                couponCode,
                subTotal
        );

        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            clearCheckoutCoupon(session);
            session.setAttribute(
                    "coupon_error",
                    "Mã khuyến mãi không đủ điều kiện áp dụng cho đơn hàng hoặc hạng thành viên hiện tại."
            );

            resp.sendRedirect(req.getContextPath() + "/checkout");
            return true;
        }

        session.setAttribute(SESSION_CHECKOUT_COUPON, couponCode);
        session.setAttribute(SESSION_CHECKOUT_COUPON_DISCOUNT, discount);
        session.setAttribute("coupon_success", "Áp dụng mã giảm giá thành công.");

        resp.sendRedirect(req.getContextPath() + "/checkout");
        return true;
    }

    /* =========================================================
       PREPARE VIEW
    ========================================================= */

    private void prepareCheckoutView(HttpServletRequest req,
                                     HttpSession session,
                                     Map<String, CartItem> cart,
                                     int userId) {

        BigDecimal subTotal = calcSubTotal(cart);

        String appliedCoupon = (String) session.getAttribute(SESSION_CHECKOUT_COUPON);
        BigDecimal couponDiscount = BigDecimal.ZERO;

        /*
         * Re-check coupon mỗi lần mở checkout.
         * Mục tiêu:
         * - Nếu giỏ hàng thay đổi làm đơn không còn đủ min_order_amount thì tự bỏ coupon.
         * - Nếu rank user không đủ min_rank_code thì không giữ coupon cũ trong session.
         */
        if (!isBlank(appliedCoupon)) {
            appliedCoupon = normalizeCouponCode(appliedCoupon);

            String baseError = validateManualCouponBaseCondition(appliedCoupon, subTotal);

            if (baseError == null) {
                couponDiscount = checkoutService.calculateCouponDiscount(
                        userId,
                        appliedCoupon,
                        subTotal
                );
            }

            if (baseError == null && couponDiscount.compareTo(BigDecimal.ZERO) > 0) {
                session.setAttribute(SESSION_CHECKOUT_COUPON, appliedCoupon);
                session.setAttribute(SESSION_CHECKOUT_COUPON_DISCOUNT, couponDiscount);
            } else {
                clearCheckoutCoupon(session);
                appliedCoupon = null;
                couponDiscount = BigDecimal.ZERO;

                if (session.getAttribute("coupon_error") == null) {
                    session.setAttribute(
                            "coupon_error",
                            baseError != null
                                    ? baseError
                                    : "Mã khuyến mãi không còn đủ điều kiện áp dụng cho giỏ hàng hiện tại."
                    );
                }
            }
        } else {
            session.removeAttribute(SESSION_CHECKOUT_COUPON_DISCOUNT);
            couponDiscount = getCheckoutCouponDiscount(session);
        }

        BigDecimal total = calcTotal(subTotal, couponDiscount);

        loadCouponsForModal(req, userId, subTotal);

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

    /* =========================================================
       FORM HELPERS
    ========================================================= */

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

    /* =========================================================
       SERVLET METHODS
    ========================================================= */

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

        prepareCheckoutView(req, session, cart, user.getId());

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
         * Mục 72:
         * Áp dụng mã giảm giá nhập thủ công.
         */
        String action = req.getParameter("action");

        if ("apply-coupon".equalsIgnoreCase(action)) {
            if (applyCoupon(req, resp, session, user, cart)) {
                return;
            }
        }

        /*
         * Mục 79:
         * Validate dữ liệu thanh toán ở backend.
         */
        Map<String, String> errors = validateCheckoutForm(req, cart);

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);

            keepFormValues(req);
            prepareCheckoutView(req, session, cart, user.getId());

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

        /*
         * Chỉ dùng mã đã được áp dụng thành công trong session.
         * Không lấy trực tiếp req.getParameter("couponCode") khi đặt hàng,
         * để tránh user nhập đại mã rồi submit checkout.
         */
        String couponCode = normalizeCouponCode((String) session.getAttribute(SESSION_CHECKOUT_COUPON));

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

                clearCheckoutCoupon(session);

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

            clearCheckoutCoupon(session);

            resp.sendRedirect(req.getContextPath()
                    + "/checkout/success?success=true&orderId=" + orderId
                    + "&method=" + paymentMethod);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();

            String message = e.getMessage();

            if (message == null || message.isBlank()) {
                message = "Dữ liệu thanh toán không hợp lệ.";
            }

            Map<String, String> checkoutErrors = new HashMap<>();
            checkoutErrors.put("general", message);

            if (isCouponConditionMessage(message)) {
                clearCheckoutCoupon(session);
                req.setAttribute("coupon_error", message);
            }

            req.setAttribute("errors", checkoutErrors);

            keepFormValues(req);
            prepareCheckoutView(req, session, cart, user.getId());

            /*
             * Đặt lại sau prepareCheckoutView vì hàm đó có thể đọc/xóa coupon_error từ session.
             */
            if (isCouponConditionMessage(message)) {
                req.setAttribute("coupon_error", message);
            }

            req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();

            Map<String, String> checkoutErrors = new HashMap<>();
            checkoutErrors.put(
                    "general",
                    "Thanh toán thất bại. Vui lòng kiểm tra lại thông tin và thử lại."
            );

            req.setAttribute("errors", checkoutErrors);

            keepFormValues(req);
            prepareCheckoutView(req, session, cart, user.getId());

            req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
        }
    }
}
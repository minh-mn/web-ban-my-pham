package com.webshop.app.controller.OrderController;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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
    private static final String SHIPPING_ECONOMY = "ECONOMY";
    private static final String SHIPPING_FAST = "FAST";
    private static final String SHIPPING_EXPRESS = "EXPRESS";

    private static final BigDecimal FREE_SHIP_THRESHOLD = new BigDecimal("500000");
    private static final BigDecimal HCM_ECONOMY_FEE = new BigDecimal("20000");
    private static final BigDecimal HCM_FAST_FEE = new BigDecimal("35000");
    private static final BigDecimal HCM_EXPRESS_FEE = new BigDecimal("50000");
    private static final BigDecimal OTHER_ECONOMY_FEE = new BigDecimal("35000");
    private static final BigDecimal OTHER_FAST_FEE = new BigDecimal("50000");

    private static final String DEFAULT_RANK_CODE = "MEMBER";

    private static final String SESSION_CHECKOUT_COUPON = "CHECKOUT_COUPON";
    private static final String SESSION_CHECKOUT_COUPON_DISCOUNT = "CHECKOUT_COUPON_DISCOUNT";

    private final CheckoutService checkoutService = new CheckoutService();
    private final CouponDAO couponDAO = new CouponDAO();

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

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeShippingMethod(String shippingMethod) {
        String method = trim(shippingMethod).toUpperCase();

        Set<String> validShippingMethods = Set.of(
                SHIPPING_ECONOMY,
                SHIPPING_FAST,
                SHIPPING_EXPRESS
        );

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

    /**
     * Chuẩn hóa cách nhận diện TP.HCM để tính phí nội thành.
     */
    private boolean isHcmCity(String province) {
        if (province == null || province.trim().isEmpty()) {
            return false;
        }

        String value = province.trim().toLowerCase();

        return value.contains("hồ chí minh")
                || value.contains("ho chi minh")
                || value.contains("tp. hcm")
                || value.contains("tp hcm")
                || value.contains("tphcm")
                || value.contains("thành phố hồ chí minh");
    }

    private boolean isExpressSupported(String province) {
        return isHcmCity(province);
    }

    /**
     * Tính phí ship ở backend, không tin hoàn toàn vào input hidden từ trình duyệt.
     *
     * Quy tắc cuối cùng:
     * - Freeship áp dụng toàn quốc nếu tổng sau voucher >= 500.000đ.
     * - Nếu chưa đạt freeship:
     *   + TP.HCM: ECONOMY 20k, FAST 35k, EXPRESS 50k.
     *   + Ngoại tỉnh: ECONOMY 35k, FAST 50k, EXPRESS không hỗ trợ.
     * - Nếu client cố gửi EXPRESS cho ngoại tỉnh, backend không cho phí = 0 sai;
     *   hệ thống fallback về phí ECONOMY ngoại tỉnh để tránh gian lận hidden input.
     */
    private BigDecimal calculateServerShippingFee(String shippingMethod,
                                                  String province,
                                                  BigDecimal amountAfterCoupon) {
        BigDecimal safeAmountAfterCoupon = amountAfterCoupon != null
                ? amountAfterCoupon.setScale(0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        if (safeAmountAfterCoupon.compareTo(FREE_SHIP_THRESHOLD) >= 0) {
            return BigDecimal.ZERO;
        }

        String method = normalizeShippingMethod(shippingMethod);
        boolean hcm = isHcmCity(province);

        if (hcm) {
            return switch (method) {
                case SHIPPING_FAST -> HCM_FAST_FEE;
                case SHIPPING_EXPRESS -> HCM_EXPRESS_FEE;
                case SHIPPING_ECONOMY -> HCM_ECONOMY_FEE;
                default -> HCM_ECONOMY_FEE;
            };
        }

        return switch (method) {
            case SHIPPING_FAST -> OTHER_FAST_FEE;
            case SHIPPING_EXPRESS, SHIPPING_ECONOMY -> OTHER_ECONOMY_FEE;
            default -> OTHER_ECONOMY_FEE;
        };
    }

    /**
     * Lấy giỏ hàng đã được tích chọn.
     * Nếu chưa có sản phẩm được chọn thì redirect về cart.
     */
    private Map<String, CartItem> getSelectedCheckoutCart(HttpServletRequest req,
                                                          HttpServletResponse resp,
                                                          HttpSession session) throws IOException {

        Map<String, CartItem> selectedCart = CartUtil.getSelectedCart(session);

        if (selectedCart == null || selectedCart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart?selectRequired=1");
            return null;
        }

        return selectedCart;
    }

    private BigDecimal getCheckoutCouponDiscount(HttpSession session) {
        BigDecimal couponDiscount =
                (BigDecimal) session.getAttribute(SESSION_CHECKOUT_COUPON_DISCOUNT);

        return couponDiscount != null ? couponDiscount : BigDecimal.ZERO;
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

    private void clearCheckoutCoupon(HttpSession session) {
        session.removeAttribute(SESSION_CHECKOUT_COUPON);
        session.removeAttribute(SESSION_CHECKOUT_COUPON_DISCOUNT);
    }

    /**
     * Xóa đúng một sản phẩm trong trang checkout.
     *
     * Quan trọng:
     * - Đây KHÔNG phải hành động đặt hàng, nên không validate họ tên, số điện thoại, địa chỉ.
     * - Không gọi /cart/remove vì luồng đó redirect về trang giỏ hàng.
     * - Chỉ xóa đúng cartKey đang chọn, giữ các sản phẩm còn lại ở checkout.
     * - Khi tổng tiền thay đổi thì xóa mã giảm giá đang áp dụng để tránh sai điều kiện mã.
     */
    private void removeCheckoutItem(HttpServletRequest req,
                                    HttpServletResponse resp,
                                    HttpSession session,
                                    String cartKey) throws IOException {

        String key = trim(cartKey);

        if (isBlank(key)) {
            resp.sendRedirect(req.getContextPath() + "/checkout");
            return;
        }

        CartUtil.removeItems(session, Set.of(key));

        Set<String> selectedKeys = new LinkedHashSet<>(CartUtil.getSelectedCartKeys(session));
        selectedKeys.remove(key);

        if (selectedKeys.isEmpty()) {
            CartUtil.clearSelectedCartKeys(session);
        } else {
            CartUtil.setSelectedCartKeys(session, selectedKeys);
        }

        clearCheckoutCoupon(session);
        session.removeAttribute("coupon_success");
        session.removeAttribute("coupon_error");

        Map<String, CartItem> remainingCheckoutCart = CartUtil.getSelectedCart(session);

        if (remainingCheckoutCart == null || remainingCheckoutCart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart?selectRequired=1");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/checkout");
    }

    private String normalizeCouponCode(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return "";
        }

        return couponCode.trim().toUpperCase();
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

    /**
     * Tính số tiền giảm thực tế theo đơn hiện tại.
     * Đây là cơ sở để chọn mã "Tốt nhất".
     */
    private BigDecimal estimateCouponDiscount(Coupon coupon, BigDecimal subTotal) {
        if (coupon == null || subTotal == null || subTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal safeSubTotal = subTotal.setScale(0, RoundingMode.HALF_UP);
        BigDecimal minOrderAmount = coupon.getMinOrderAmount() != null
                ? coupon.getMinOrderAmount()
                : BigDecimal.ZERO;

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

    private String normalizeRankCode(String rankCode) {
        if (rankCode == null || rankCode.trim().isEmpty()) {
            return DEFAULT_RANK_CODE;
        }

        return rankCode.trim().toUpperCase();
    }

    private int rankLevel(String rankCode) {
        String code = normalizeRankCode(rankCode);

        return switch (code) {
            case "MEMBER" -> 1;
            case "SILVER" -> 2;
            case "GOLD" -> 3;
            case "DIAMOND" -> 4;
            case "VIP" -> 5;
            default -> 1;
        };
    }

    /**
     * Lấy hạng hiện tại từ session User.
     * Nếu model User chưa có getManualRankCode/getRankCode thì fallback MEMBER.
     */
    private String getUserRankCode(User user) {
        if (user == null) {
            return DEFAULT_RANK_CODE;
        }

        String[] getterNames = {"getManualRankCode", "getRankCode"};

        for (String getterName : getterNames) {
            try {
                Object rank = user.getClass().getMethod(getterName).invoke(user);

                if (rank != null && !rank.toString().trim().isEmpty()) {
                    return normalizeRankCode(rank.toString());
                }
            } catch (Exception ignored) {
                // Model User không có getter này thì thử getter tiếp theo.
            }
        }

        return DEFAULT_RANK_CODE;
    }

    private boolean isRankEligible(Coupon coupon, String userRankCode) {
        if (coupon == null) {
            return false;
        }

        String requiredRank = normalizeRankCode(coupon.getMinRankCode());
        String currentRank = normalizeRankCode(userRankCode);

        return rankLevel(currentRank) >= rankLevel(requiredRank);
    }

    /**
     * Trả về lý do mã không dùng được với user và đơn hàng hiện tại.
     * Nếu trả về null nghĩa là mã dùng được.
     */
    private String getCouponInvalidReasonForUser(User user, Coupon coupon, BigDecimal subTotal) {
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

        String userRankCode = getUserRankCode(user);

        if (!isRankEligible(coupon, userRankCode)) {
            return "Hạng thành viên hiện tại chưa phù hợp. Mã này yêu cầu hạng "
                    + normalizeRankCode(coupon.getMinRankCode())
                    + " trở lên.";
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

    private boolean isCouponUsableForUserAndOrder(User user, Coupon coupon, BigDecimal subTotal) {
        return getCouponInvalidReasonForUser(user, coupon, subTotal) == null;
    }

    /**
     * Validate riêng cho mục 72 - nhập mã giảm giá thủ công.
     *
     * Mục tiêu:
     * - Không cho áp dụng mã không tồn tại trong database.
     * - Phân biệt lỗi rõ ràng: bị tắt, chưa tới ngày dùng, hết hạn, hết lượt, chưa đủ đơn tối thiểu.
     * - Rank/min_rank_code vẫn để CheckoutService/CouponService kiểm tra bằng overload có userId.
     */
    private String validateManualCouponBaseCondition(User user, String couponCode, BigDecimal subTotal) {
        Coupon coupon = couponDAO.findByCode(couponCode);

        return getCouponInvalidReasonForUser(user, coupon, subTotal);
    }

    private void loadCouponsForModal(HttpServletRequest req,
                                     User user,
                                     BigDecimal subTotal) {

        List<Coupon> savedCoupons = new ArrayList<>();
        List<Coupon> availableCoupons = new ArrayList<>();
        List<Coupon> allCoupons = new ArrayList<>();

        Map<String, BigDecimal> couponEstimatedDiscountMap = new HashMap<>();
        Map<String, Boolean> couponUsableMap = new HashMap<>();
        Map<String, Boolean> couponRankEligibleMap = new HashMap<>();
        Map<String, String> couponDisabledReasonMap = new HashMap<>();

        BigDecimal safeSubTotal = subTotal != null ? subTotal : BigDecimal.ZERO;
        String userRankCode = getUserRankCode(user);
        String bestCouponCode = "";

        try {
            if (user != null && user.getId() > 0) {
                savedCoupons = couponDAO.findSavedCouponsByUserId(user.getId());
            }

            /*
             * allCoupons:
             * - Lấy tất cả mã để modal hiển thị được cả mã đủ điều kiện và chưa đủ điều kiện.
             * - Mã chưa đủ điều kiện sẽ bị mờ và không chọn được.
             * - Mã "Tốt nhất" chỉ xét trong nhóm user hiện tại thật sự dùng được.
             */
            List<Coupon> rawCoupons = couponDAO.findAll();

            for (Coupon coupon : rawCoupons) {
                if (!isCouponBaseUsable(coupon)) {
                    continue;
                }

                String code = coupon.getCode();

                if (code == null || code.trim().isEmpty()) {
                    continue;
                }

                String disabledReason = getCouponInvalidReasonForUser(user, coupon, safeSubTotal);
                boolean usable = disabledReason == null || disabledReason.isBlank();
                boolean rankEligible = isRankEligible(coupon, userRankCode);
                BigDecimal estimatedDiscount = usable
                        ? estimateCouponDiscount(coupon, safeSubTotal)
                        : BigDecimal.ZERO;

                allCoupons.add(coupon);
                couponUsableMap.put(code, usable);
                couponRankEligibleMap.put(code, rankEligible);
                couponDisabledReasonMap.put(code, usable ? "" : disabledReason);
                couponEstimatedDiscountMap.put(code, estimatedDiscount);

                if (usable) {
                    availableCoupons.add(coupon);
                }
            }

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

            for (Coupon coupon : allCoupons) {
                if (coupon == null || coupon.getCode() == null) {
                    continue;
                }

                String code = coupon.getCode();
                boolean usable = couponUsableMap.getOrDefault(code, false);
                BigDecimal estimatedDiscount = couponEstimatedDiscountMap.getOrDefault(code, BigDecimal.ZERO);

                if (usable && estimatedDiscount.compareTo(BigDecimal.ZERO) > 0) {
                    bestCouponCode = code;
                    break;
                }
            }

        } catch (RuntimeException e) {
            /*
             * Không để lỗi load coupon làm hỏng toàn bộ trang checkout.
             */
            e.printStackTrace();
            req.setAttribute("couponLoadError", "Không thể tải danh sách mã khuyến mãi.");
        }

        req.setAttribute("savedCoupons", savedCoupons);
        req.setAttribute("availableCoupons", availableCoupons);
        req.setAttribute("allCoupons", allCoupons);
        req.setAttribute("checkoutCoupons", allCoupons);
        req.setAttribute("couponOptions", allCoupons);

        req.setAttribute("bestCouponCode", bestCouponCode);
        req.setAttribute("currentUserRankCode", userRankCode);
        req.setAttribute("couponEstimatedDiscountMap", couponEstimatedDiscountMap);
        req.setAttribute("couponUsableMap", couponUsableMap);
        req.setAttribute("couponRankEligibleMap", couponRankEligibleMap);
        req.setAttribute("couponDisabledReasonMap", couponDisabledReasonMap);
    }

    private void prepareCheckoutView(HttpServletRequest req,
                                     HttpSession session,
                                     Map<String, CartItem> cart,
                                     User user) {

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

            String baseError = validateManualCouponBaseCondition(user, appliedCoupon, subTotal);

            if (baseError == null) {
                couponDiscount = checkoutService.calculateCouponDiscount(
                        user.getId(),
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

        loadCouponsForModal(req, user, subTotal);

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
        req.setAttribute("formLatitude", trim(req.getParameter("latitude")));
        req.setAttribute("formLongitude", trim(req.getParameter("longitude")));
        req.setAttribute("formDetectedProvince", trim(req.getParameter("detectedProvince")));
        req.setAttribute("formDetectedAddress", trim(req.getParameter("detectedAddress")));
        req.setAttribute("formMapConfirmed", trim(req.getParameter("mapConfirmed")));

        req.setAttribute("formShippingMethod",
                normalizeShippingMethod(req.getParameter("shippingMethod")));

        req.setAttribute("formShippingFee",
                parseShippingFee(req.getParameter("shippingFee")));
    }

    private String normalizeVietnameseText(String value) {
        if (value == null) {
            return "";
        }

        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD);

        normalized = normalized.replaceAll("\\p{M}", "");
        normalized = normalized.replace("đ", "d").replace("Đ", "D");

        return normalized
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s./,-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean containsAnyKeyword(String value, String... keywords) {
        String normalizedValue = normalizeVietnameseText(value);

        for (String keyword : keywords) {
            String normalizedKeyword = normalizeVietnameseText(keyword);

            if (!normalizedKeyword.isBlank() && normalizedValue.contains(normalizedKeyword)) {
                return true;
            }
        }

        return false;
    }

    private boolean isValidCoordinateValue(String value) {
        if (isBlank(value)) {
            return false;
        }

        try {
            double coordinate = Double.parseDouble(value.trim());
            return coordinate >= -180 && coordinate <= 180;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean hasVerifiedCurrentLocation(String latitude,
                                               String longitude,
                                               String detectedProvince,
                                               String selectedProvince,
                                               String mapConfirmed) {
        return "true".equalsIgnoreCase(trim(mapConfirmed))
                && isValidCoordinateValue(latitude)
                && isValidCoordinateValue(longitude)
                && !isBlank(detectedProvince)
                && isSameProvince(selectedProvince, detectedProvince);
    }

    private boolean isClearlyInvalidAddressText(String address) {
        String normalizedAddress = normalizeVietnameseText(address);
        String compactAddress = normalizedAddress.replace(" ", "");

        if (normalizedAddress.isBlank()) {
            return true;
        }

        String[] invalidValues = {
                "abc",
                "abcd",
                "abcde",
                "test",
                "testing",
                "asdf",
                "aaa",
                "aaaa",
                "dia chi",
                "dia chi nha",
                "khong biet",
                "khong co",
                "chua co",
                "tam thoi",
                "none",
                "null"
        };

        for (String invalidValue : invalidValues) {
            if (normalizedAddress.equals(invalidValue)) {
                return true;
            }
        }

        /*
         * Chặn chuỗi nhập bừa kiểu aaaaaa, 111111, //////...
         * Địa chỉ ngắn vẫn có thể hợp lệ nếu có GPS xác minh,
         * nên chỉ chặn chuỗi lặp quá dài.
         */
        if (compactAddress.length() >= 6 && compactAddress.matches("^(.)\\1{5,}$")) {
            return true;
        }

        /*
         * Chỉ toàn ký tự đặc biệt thì không phải địa chỉ.
         */
        return !normalizedAddress.matches(".*[a-z0-9].*");
    }

    private boolean isWeakManualAddress(String address) {
        String normalizedAddress = normalizeVietnameseText(address);
        String compactAddress = normalizedAddress.replace(" ", "");

        if (compactAddress.length() < 3) {
            return true;
        }

        boolean hasLetter = normalizedAddress.matches(".*[a-z].*");
        boolean hasDigit = normalizedAddress.matches(".*[0-9].*");

        /*
         * Nếu chưa xác minh GPS, chỉ nhập mỗi số nhà là quá mơ hồ.
         */
        if (!hasLetter) {
            return true;
        }

        String[] usefulKeywords = {
                "duong",
                "hem",
                "ngo",
                "so",
                "ap",
                "khu",
                "kp",
                "khu pho",
                "thon",
                "xom",
                "to",
                "block",
                "chung cu",
                "toa",
                "lau",
                "can ho",
                "xa",
                "phuong",
                "thi tran",
                "doi",
                "tan",
                "linh",
                "nguyen",
                "tran",
                "le",
                "pham",
                "hoang"
        };

        boolean hasUsefulKeyword = false;

        for (String keyword : usefulKeywords) {
            if (normalizedAddress.contains(keyword)) {
                hasUsefulKeyword = true;
                break;
            }
        }

        int wordCount = normalizedAddress.isBlank() ? 0 : normalizedAddress.split(" ").length;

        return !(hasUsefulKeyword || (hasDigit && normalizedAddress.length() >= 4) || wordCount >= 2);
    }

    private boolean isHcmAddressKeyword(String value) {
        return containsAnyKeyword(
                value,
                "tphcm",
                "tp hcm",
                "tp. hcm",
                "ho chi minh",
                "thanh pho ho chi minh",
                "sai gon",
                "thu duc",
                "linh trung",
                "linh xuan"
        );
    }

    private boolean isHcmProvince(String province) {
        return containsAnyKeyword(
                province,
                "tphcm",
                "tp hcm",
                "tp. hcm",
                "ho chi minh",
                "thanh pho ho chi minh"
        );
    }

    private boolean isSameProvince(String selectedProvince, String detectedProvince) {
        String selected = normalizeVietnameseText(selectedProvince);
        String detected = normalizeVietnameseText(detectedProvince);

        if (selected.isBlank() || detected.isBlank()) {
            return false;
        }

        if (isHcmProvince(selected) && isHcmProvince(detected)) {
            return true;
        }

        String[][] aliasGroups = {
                {"ho chi minh", "tphcm", "tp hcm", "thanh pho ho chi minh", "sai gon", "thu duc"},
                {"can tho", "tp can tho", "thanh pho can tho"},
                {"ha noi", "tp ha noi", "thanh pho ha noi"},
                {"da nang", "tp da nang", "thanh pho da nang"},
                {"ba ria", "vung tau", "ba ria vung tau"},
                {"binh duong", "thu dau mot", "di an", "thuan an"},
                {"dong nai", "bien hoa"}
        };

        for (String[] group : aliasGroups) {
            boolean selectedInGroup = false;
            boolean detectedInGroup = false;

            for (String keyword : group) {
                String normalizedKeyword = normalizeVietnameseText(keyword);

                if (selected.contains(normalizedKeyword)) {
                    selectedInGroup = true;
                }

                if (detected.contains(normalizedKeyword)) {
                    detectedInGroup = true;
                }
            }

            if (selectedInGroup && detectedInGroup) {
                return true;
            }
        }

        return selected.contains(detected) || detected.contains(selected);
    }

    private boolean isAddressProvinceConflict(String address, String province) {
        String normalizedAddress = normalizeVietnameseText(address);
        String normalizedProvince = normalizeVietnameseText(province);

        if (normalizedAddress.isBlank() || normalizedProvince.isBlank()) {
            return false;
        }

        /*
         * Trường hợp thường gặp:
         * Người dùng gõ địa chỉ thuộc TP.HCM nhưng lại chọn tỉnh/thành khác.
         */
        if (isHcmAddressKeyword(address) && !isHcmProvince(province)) {
            return true;
        }

        /*
         * Nếu người dùng ghi rõ tên tỉnh/thành trong ô địa chỉ,
         * tỉnh/thành đó phải khớp với tỉnh/thành đã chọn.
         */
        String[][] provinceGroups = {
                {"can tho", "tp can tho", "thanh pho can tho"},
                {"ho chi minh", "tphcm", "tp hcm", "sai gon", "thu duc"},
                {"ha noi", "tp ha noi", "thanh pho ha noi"},
                {"da nang", "tp da nang", "thanh pho da nang"},
                {"dong nai", "bien hoa"},
                {"binh duong", "thu dau mot", "di an", "thuan an"},
                {"long an", "tan an"},
                {"tien giang", "my tho"},
                {"ba ria", "vung tau", "ba ria vung tau"},
                {"tay ninh"},
                {"dong thap", "cao lanh", "sa dec"},
                {"vinh long"},
                {"ben tre"},
                {"an giang", "long xuyen", "chau doc"},
                {"kien giang", "rach gia", "phu quoc"},
                {"khanh hoa", "nha trang"},
                {"lam dong", "da lat"},
                {"binh thuan", "phan thiet"}
        };

        for (String[] group : provinceGroups) {
            boolean addressMentionsProvince = false;

            for (String keyword : group) {
                if (normalizedAddress.contains(normalizeVietnameseText(keyword))) {
                    addressMentionsProvince = true;
                    break;
                }
            }

            if (!addressMentionsProvince) {
                continue;
            }

            boolean selectedMatchesGroup = false;

            for (String keyword : group) {
                if (normalizedProvince.contains(normalizeVietnameseText(keyword))) {
                    selectedMatchesGroup = true;
                    break;
                }
            }

            if (!selectedMatchesGroup) {
                return true;
            }
        }

        return false;
    }

    private boolean isAddressWardConflict(String address, String wardName) {
        String normalizedAddress = normalizeVietnameseText(address);
        String normalizedWard = normalizeVietnameseText(wardName);

        if (normalizedAddress.isBlank() || normalizedWard.isBlank()) {
            return false;
        }

        /*
         * Không bắt buộc địa chỉ phải chứa tên phường/xã,
         * vì người dùng thường chỉ nhập số nhà + tên đường.
         * Chỉ kiểm tra khi ô địa chỉ có ghi rõ phường/xã.
         */
        boolean addressMentionsWardPrefix =
                normalizedAddress.contains("phuong ")
                        || normalizedAddress.contains("xa ")
                        || normalizedAddress.contains("thi tran ");

        if (!addressMentionsWardPrefix) {
            return false;
        }

        String wardShort = normalizedWard
                .replace("phuong ", "")
                .replace("xa ", "")
                .replace("thi tran ", "")
                .trim();

        if (wardShort.isBlank()) {
            return false;
        }

        return !normalizedAddress.contains(wardShort);
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
        String latitude = trim(req.getParameter("latitude"));
        String longitude = trim(req.getParameter("longitude"));
        String detectedProvince = trim(req.getParameter("detectedProvince"));
        String mapConfirmed = trim(req.getParameter("mapConfirmed"));

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

        boolean hasVerifiedCurrentLocation = hasVerifiedCurrentLocation(
                latitude,
                longitude,
                detectedProvince,
                province,
                mapConfirmed
        );

        if (isBlank(address)) {
            errors.put("address", "Vui lòng nhập địa chỉ giao hàng.");
        } else if (address.length() > 160) {
            errors.put("address", "Địa chỉ không được vượt quá 160 ký tự.");
        } else if (isClearlyInvalidAddressText(address)) {
            errors.put("address", "Địa chỉ không hợp lệ. Vui lòng nhập địa chỉ thật.");
        } else if (!hasVerifiedCurrentLocation && isWeakManualAddress(address)) {
            errors.put(
                    "address",
                    "Vui lòng nhập rõ số nhà, hẻm, tổ/khu phố/ấp/xã, tên đường hoặc chọn vị trí trên bản đồ để xác minh."
            );
        } else if (isAddressProvinceConflict(address, province)) {
            errors.put("address", "Địa chỉ cụ thể không khớp với Tỉnh/TP đã chọn.");
        } else if (isAddressWardConflict(address, wardName)) {
            errors.put("address", "Địa chỉ cụ thể có vẻ không khớp với Phường/Xã đã chọn.");
        }

        if (isBlank(province) || isBlank(provinceCode)) {
            errors.put("location", "Vui lòng chọn Tỉnh/TP.");
        } else if (isBlank(wardName) || isBlank(wardCode)) {
            errors.put("location", "Vui lòng chọn Phường/Xã sau khi chọn Tỉnh/TP.");
        } else if (!isBlank(latitude) && !isBlank(longitude) && !isBlank(detectedProvince)
                && !isSameProvince(province, detectedProvince)) {
            errors.put(
                    "location",
                    "Vị trí hiện tại không khớp với Tỉnh/TP đã chọn. Vị trí phát hiện: " + detectedProvince
            );
        }

        Set<String> validPaymentMethods = Set.of("COD", "VNPAY");

        if (isBlank(paymentMethod)) {
            errors.put("paymentMethod", "Vui lòng chọn phương thức thanh toán.");
        } else if (!validPaymentMethods.contains(paymentMethod)) {
            errors.put("paymentMethod", "Phương thức thanh toán không hợp lệ.");
        }

        Set<String> validShippingMethods = Set.of(SHIPPING_ECONOMY, SHIPPING_FAST, SHIPPING_EXPRESS);

        if (!validShippingMethods.contains(shippingMethod)) {
            errors.put("shippingMethod", "Phương thức vận chuyển không hợp lệ.");
        }

        if (SHIPPING_EXPRESS.equals(shippingMethod) && !isBlank(province) && !isExpressSupported(province)) {
            errors.put("shippingMethod", "Hỏa tốc chỉ hỗ trợ khu vực TP.HCM. Vui lòng chọn Giao hàng tiết kiệm hoặc Giao hàng nhanh.");
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
        String baseError = validateManualCouponBaseCondition(user, couponCode, subTotal);

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

    /**
     * Mục 91 - gửi thông báo đơn hàng qua email.
     *
     * Hàm này gọi bất đồng bộ và không làm thất bại đơn hàng nếu email lỗi.
     * Để hoạt động thật, project nên có một service:
     *   com.webshop.app.service.OrderEmailService
     *
     * Các tên hàm được hỗ trợ:
     * - sendOrderSuccessEmail(int orderId, String email)
     * - sendOrderSuccessEmail(String email, int orderId)
     * - sendOrderSuccessEmail(int orderId)
     * - sendOrderConfirmationEmail(int orderId, String email)
     * - sendOrderConfirmationEmail(int orderId)
     *
     * Nếu service chưa tồn tại thì hàm tự bỏ qua, project vẫn build bình thường.
     */
    private void sendOrderSuccessEmailAsync(User user, int orderId) {
        if (orderId <= 0) {
            return;
        }

        String email = getUserEmail(user);

        CompletableFuture.runAsync(() -> {
            try {
                Class<?> serviceClass = Class.forName("com.webshop.app.service.OrderEmailService");
                Object emailService = serviceClass.getDeclaredConstructor().newInstance();

                if (tryInvokeEmailMethod(serviceClass, emailService, "sendOrderSuccessEmail", orderId, email)) {
                    return;
                }

                if (tryInvokeEmailMethod(serviceClass, emailService, "sendOrderConfirmationEmail", orderId, email)) {
                    return;
                }

            } catch (ClassNotFoundException ignored) {
                /*
                 * Chưa có OrderEmailService thì bỏ qua để không làm hỏng checkout.
                 * Khi bạn thêm service gửi mail, hàm này sẽ tự gọi được nếu đúng signature.
                 */
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private boolean tryInvokeEmailMethod(Class<?> serviceClass,
                                         Object service,
                                         String methodName,
                                         int orderId,
                                         String email) {
        try {
            serviceClass
                    .getMethod(methodName, int.class, String.class)
                    .invoke(service, orderId, email);
            return true;
        } catch (NoSuchMethodException ignored) {
            // Thử signature tiếp theo.
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }

        try {
            serviceClass
                    .getMethod(methodName, String.class, int.class)
                    .invoke(service, email, orderId);
            return true;
        } catch (NoSuchMethodException ignored) {
            // Thử signature tiếp theo.
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }

        try {
            serviceClass
                    .getMethod(methodName, int.class)
                    .invoke(service, orderId);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private String getUserEmail(User user) {
        if (user == null) {
            return "";
        }

        try {
            Object value = user.getClass().getMethod("getEmail").invoke(user);

            if (value != null) {
                return value.toString().trim();
            }
        } catch (Exception ignored) {
            // User model chưa có getEmail thì để rỗng.
        }

        return "";
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

        prepareCheckoutView(req, session, cart, user);

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

        String action = trim(req.getParameter("action"));
        String removeCartKey = trim(req.getParameter("removeCartKey"));

        /*
         * Xóa sản phẩm trong checkout là thao tác chỉnh danh sách hàng hóa.
         * Không được validate họ tên, số điện thoại, địa chỉ hoặc phương thức thanh toán.
         * Vì vậy phải xử lý remove-item ngay đầu doPost, trước mọi logic đặt hàng.
         */
        if ("remove-item".equalsIgnoreCase(action) || !isBlank(removeCartKey)) {
            removeCheckoutItem(req, resp, session, removeCartKey);
            return;
        }

        /*
         * Mục 68:
         * Khi đặt hàng hoặc áp mã giảm giá, chỉ lấy các sản phẩm đã tích chọn.
         */
        Map<String, CartItem> cart = getSelectedCheckoutCart(req, resp, session);

        if (cart == null) {
            return;
        }

        /*
         * Mục 72:
         * Áp dụng mã giảm giá nhập thủ công cũng không validate thông tin giao hàng.
         */
        if ("apply-coupon".equalsIgnoreCase(action)) {
            if (applyCoupon(req, resp, session, user, cart)) {
                return;
            }
        }

        /*
         * Mục 79:
         * Chỉ validate dữ liệu thanh toán khi user thật sự bấm Đặt hàng.
         */
        Map<String, String> errors = validateCheckoutForm(req, cart);

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);

            keepFormValues(req);
            prepareCheckoutView(req, session, cart, user);

            req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
            return;
        }

        String fullName = trim(req.getParameter("fullName"));
        String phone = trim(req.getParameter("phone"));
        String finalAddress = buildFinalShippingAddress(req);
        String paymentMethod = trim(req.getParameter("paymentMethod"));

        String province = trim(req.getParameter("province"));

        String shippingMethod = normalizeShippingMethod(req.getParameter("shippingMethod"));

        /*
         * Chỉ dùng mã đã được áp dụng thành công trong session.
         * Không lấy trực tiếp req.getParameter("couponCode") khi đặt hàng,
         * để tránh user nhập đại mã rồi submit checkout.
         */
        String couponCode = normalizeCouponCode((String) session.getAttribute(SESSION_CHECKOUT_COUPON));

        BigDecimal checkoutSubTotal = calcSubTotal(cart);
        BigDecimal checkoutDiscount = BigDecimal.ZERO;

        if (!isBlank(couponCode)) {
            checkoutDiscount = checkoutService.calculateCouponDiscount(
                    user.getId(),
                    couponCode,
                    checkoutSubTotal
            );

            if (checkoutDiscount == null || checkoutDiscount.compareTo(BigDecimal.ZERO) < 0) {
                checkoutDiscount = BigDecimal.ZERO;
            }
        }

        BigDecimal amountAfterCoupon = calcTotal(checkoutSubTotal, checkoutDiscount);
        BigDecimal serverShippingFee = calculateServerShippingFee(
                shippingMethod,
                province,
                amountAfterCoupon
        );

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
                    serverShippingFee,
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

                /*
                 * Mục 91:
                 * Gửi email bất đồng bộ, lỗi mail không làm hỏng đơn hàng.
                 */
                sendOrderSuccessEmailAsync(user, orderId);

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
            sendOrderSuccessEmailAsync(user, orderId);

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
            prepareCheckoutView(req, session, cart, user);

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
            prepareCheckoutView(req, session, cart, user);

            req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
        }
    }
}

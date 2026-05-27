package com.webshop.app.model.admin;

import com.webshop.app.model.BrandDiscount;
import com.webshop.app.model.Coupon;
import com.webshop.app.model.DiscountType;
import com.webshop.app.model.OrderDiscount;
import com.webshop.app.model.PromotionEvent;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Locale;

public class AdminPromotionRow {

    public static final String TYPE_COUPON = "COUPON";
    public static final String TYPE_BRAND = "BRAND";
    public static final String TYPE_ORDER = "ORDER";
    public static final String TYPE_EVENT = "EVENT";

    private int id;
    private String type;
    private String typeLabel;
    private String title;
    private String code;
    private String scopeLabel;
    private String discountType;
    private String discountValueLabel;
    private String conditionLabel;
    private String periodLabel;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    public static AdminPromotionRow fromCoupon(Coupon coupon) {
        AdminPromotionRow row = new AdminPromotionRow();

        row.id = coupon.getId();
        row.type = TYPE_COUPON;
        row.typeLabel = "Mã giảm giá";
        row.code = coupon.getCode();
        row.title = coupon.getCode();
        row.scopeLabel = buildCouponScopeLabel(coupon);
        row.discountType = coupon.getDiscountType() == null ? "" : coupon.getDiscountType().name();
        row.discountValueLabel = discountLabel(coupon.getDiscountType(), coupon.getDiscountValue());

        row.conditionLabel = "Đơn tối thiểu: " + money(coupon.getMinOrderAmount())
                + " | Rank: " + coupon.getMinRankCode()
                + " | Lượt dùng: " + coupon.getUsedCount() + "/" + coupon.getMaxUses();

        row.periodLabel = dateRange(coupon.getStartDate(), coupon.getEndDate());
        row.startDate = coupon.getStartDate();
        row.endDate = coupon.getEndDate();
        row.active = coupon.isActive();

        return row;
    }

    public static AdminPromotionRow fromBrandDiscount(BrandDiscount discount) {
        AdminPromotionRow row = new AdminPromotionRow();

        row.id = discount.getId();
        row.type = TYPE_BRAND;
        row.typeLabel = "Giảm giá thương hiệu";

        row.title = !isEmpty(discount.getBrandName())
                ? "Thương hiệu: " + discount.getBrandName()
                : "Thương hiệu #" + discount.getBrandId();

        row.code = "";
        row.scopeLabel = buildBrandDiscountScopeLabel(discount);
        row.discountType = discount.getDiscountType() == null ? "" : discount.getDiscountType().name();
        row.discountValueLabel = discountLabel(discount.getDiscountType(), discount.getDiscountValue());

        row.conditionLabel = discount.getMaxDiscountAmount() == null
                ? "Không giới hạn giảm tối đa"
                : "Giảm tối đa: " + money(discount.getMaxDiscountAmount());

        row.periodLabel = dateRange(discount.getStartDate(), discount.getEndDate());
        row.startDate = discount.getStartDate();
        row.endDate = discount.getEndDate();
        row.active = discount.isActive();

        return row;
    }

    public static AdminPromotionRow fromOrderDiscount(OrderDiscount discount) {
        AdminPromotionRow row = new AdminPromotionRow();

        row.id = discount.getId();
        row.type = TYPE_ORDER;
        row.typeLabel = "Giảm theo đơn hàng";
        row.title = isEmpty(discount.getName())
                ? "Giảm giá theo giá trị đơn hàng"
                : discount.getName();

        row.code = "";
        row.scopeLabel = "Theo tổng giá trị đơn hàng";
        row.discountType = "PERCENT";
        row.discountValueLabel = number(discount.getDiscountPercent()) + "%";

        row.conditionLabel = "Đơn từ: " + money(discount.getMinOrderValue())
                + (discount.getMaxDiscountAmount() == null
                ? ""
                : " | Giảm tối đa: " + money(discount.getMaxDiscountAmount()));

        row.periodLabel = dateRange(discount.getStartDate(), discount.getEndDate());
        row.startDate = discount.getStartDate();
        row.endDate = discount.getEndDate();
        row.active = discount.isActive();

        return row;
    }

    public static AdminPromotionRow fromPromotionEvent(PromotionEvent event) {
        AdminPromotionRow row = new AdminPromotionRow();

        row.id = event.getId();
        row.type = TYPE_EVENT;
        row.typeLabel = "Chương trình khuyến mãi";
        row.title = event.getName();
        row.code = "";
        row.scopeLabel = buildEventScopeLabel(event);
        row.discountType = event.getDiscountType() == null ? "" : event.getDiscountType().name();
        row.discountValueLabel = discountLabel(event.getDiscountType(), event.getDiscountValue());

        row.conditionLabel = event.getMaxDiscountAmount() == null
                ? "Không giới hạn giảm tối đa"
                : "Giảm tối đa: " + money(event.getMaxDiscountAmount());

        row.periodLabel = dateRange(event.getStartDate(), event.getEndDate());
        row.startDate = event.getStartDate();
        row.endDate = event.getEndDate();
        row.active = event.isActive();

        return row;
    }

    private static String buildCouponScopeLabel(Coupon coupon) {
        String scope = coupon.getApplyScope();

        if (Coupon.SCOPE_BRAND.equals(scope)) {
            return !isEmpty(coupon.getBrandName())
                    ? "Thương hiệu: " + coupon.getBrandName()
                    : "Brand #" + coupon.getBrandId();
        }

        if (Coupon.SCOPE_PRODUCTS.equals(scope)) {
            return "Sản phẩm cụ thể";
        }

        return "Toàn bộ giỏ hàng";
    }

    private static String buildBrandDiscountScopeLabel(BrandDiscount discount) {
        if (BrandDiscount.SCOPE_SELECTED_PRODUCTS.equals(discount.getApplyScope())) {
            return !isEmpty(discount.getBrandName())
                    ? "Sản phẩm cụ thể của " + discount.getBrandName()
                    : "Sản phẩm cụ thể";
        }

        return !isEmpty(discount.getBrandName())
                ? "Tất cả sản phẩm của " + discount.getBrandName()
                : "Tất cả sản phẩm của brand #" + discount.getBrandId();
    }

    private static String buildEventScopeLabel(PromotionEvent event) {
        if (event.getScope() == null) {
            return "";
        }

        return switch (event.getScope()) {
            case ALL -> "Toàn cửa hàng";
            case BRAND -> !isEmpty(event.getBrandName())
                    ? "Thương hiệu: " + event.getBrandName()
                    : "Brand #" + event.getBrandId();
            case CATEGORY -> !isEmpty(event.getCategoryName())
                    ? "Danh mục: " + event.getCategoryName()
                    : "Category #" + event.getCategoryId();
            case PRODUCTS -> "Sản phẩm cụ thể";
        };
    }

    private static String money(BigDecimal value) {
        if (value == null) {
            return "";
        }

        DecimalFormat formatter = new DecimalFormat("#,##0.##");
        return formatter.format(value) + " ₫";
    }

    private static String number(BigDecimal value) {
        if (value == null) {
            return "";
        }

        return value.stripTrailingZeros().toPlainString();
    }

    private static String dateRange(LocalDate startDate, LocalDate endDate) {
        String start = startDate == null ? "Không giới hạn" : startDate.toString();
        String end = endDate == null ? "Không giới hạn" : endDate.toString();

        return start + " - " + end;
    }

    private static String discountLabel(DiscountType type, BigDecimal value) {
        if (type == null || value == null) {
            return "";
        }

        if (type == DiscountType.PERCENT) {
            return number(value) + "%";
        }

        return money(value);
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String searchText() {
        return (safeText(typeLabel) + " "
                + safeText(title) + " "
                + safeText(code) + " "
                + safeText(scopeLabel) + " "
                + safeText(discountValueLabel) + " "
                + safeText(conditionLabel)).toLowerCase(Locale.ROOT);
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    public LocalDate getSortDate() {
        if (startDate != null) {
            return startDate;
        }

        return LocalDate.MIN;
    }

    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    public boolean isUpcoming() {
        return startDate != null && startDate.isAfter(LocalDate.now());
    }

    public boolean isActiveNow() {
        return active && !isExpired() && !isUpcoming();
    }

    public String getStatusLabel() {
        if (!active) {
            return "INACTIVE";
        }

        if (isExpired()) {
            return "EXPIRED";
        }

        if (isUpcoming()) {
            return "UPCOMING";
        }

        return "ACTIVE";
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public String getTitle() {
        return title;
    }

    public String getCode() {
        return code;
    }

    public String getScopeLabel() {
        return scopeLabel;
    }

    public String getDiscountType() {
        return discountType;
    }

    public String getDiscountValueLabel() {
        return discountValueLabel;
    }

    public String getConditionLabel() {
        return conditionLabel;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isActive() {
        return active;
    }
}
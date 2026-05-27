package com.webshop.app.controller.AdminController;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.webshop.app.dao.BrandDAO;
import com.webshop.app.dao.BrandDiscountDAO;
import com.webshop.app.dao.CategoryDAO;
import com.webshop.app.dao.CouponDAO;
import com.webshop.app.dao.OrderDiscountDAO;
import com.webshop.app.dao.PromotionEventDAO;
import com.webshop.app.model.BrandDiscount;
import com.webshop.app.model.Coupon;
import com.webshop.app.model.DiscountType;
import com.webshop.app.model.OrderDiscount;
import com.webshop.app.model.PromotionEvent;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/promotions")
public class AdminPromotionManagementServlet extends HttpServlet {

    private static final String LIST_JSP = "/jsp/admin/promotions/promotion_list.jsp";
    private static final String FORM_JSP = "/jsp/admin/promotions/promotion_form.jsp";

    private static final String TYPE_ALL = "ALL";
    private static final String TYPE_COUPON = "COUPON";
    private static final String TYPE_BRAND = "BRAND";
    private static final String TYPE_ORDER = "ORDER";
    private static final String TYPE_EVENT = "EVENT";

    private static final String DEFAULT_COUPON_TYPE = "DISCOUNT";
    private static final String DEFAULT_RANK_CODE = "MEMBER";

    private static final Set<String> VALID_COUPON_TYPES = Set.of(
            "DISCOUNT",
            "FREESHIP",
            "PRODUCT",
            "PERCENT"
    );

    private static final Set<String> VALID_RANK_CODES = Set.of(
            "MEMBER",
            "SILVER",
            "GOLD",
            "DIAMOND",
            "VIP"
    );

    private final CouponDAO couponDAO = new CouponDAO();
    private final BrandDiscountDAO brandDiscountDAO = new BrandDiscountDAO();
    private final OrderDiscountDAO orderDiscountDAO = new OrderDiscountDAO();
    private final PromotionEventDAO promotionEventDAO = new PromotionEventDAO();
    private final BrandDAO brandDAO = new BrandDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        prepareEncoding(req, resp);
        couponDAO.deactivateExpiredCoupons();

        String action = safe(req.getParameter("action"));
        if (action.isBlank()) {
            action = "list";
        }

        String promotionType = normalizePromotionType(req.getParameter("type"));

        switch (action.toLowerCase(Locale.ROOT)) {
            case "new":
                showCreateForm(req, resp, normalizeCreateType(promotionType));
                return;

            case "edit":
                showEditForm(req, resp, promotionType, safeInt(req.getParameter("id"), -1));
                return;

            case "list":
            default:
                showList(req, resp, promotionType);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        prepareEncoding(req, resp);

        String action = safe(req.getParameter("action"));
        if (action.isBlank()) {
            action = "create";
        }

        String promotionType = normalizeCreateType(normalizePromotionType(req.getParameter("type")));

        try {
            switch (promotionType) {
                case TYPE_COUPON:
                    handleCouponPost(req, action);
                    break;

                case TYPE_BRAND:
                    handleBrandDiscountPost(req, action);
                    break;

                case TYPE_ORDER:
                    handleOrderDiscountPost(req, action);
                    break;

                case TYPE_EVENT:
                    handlePromotionEventPost(req, action);
                    break;

                default:
                    throw new IllegalArgumentException("Loại khuyến mãi không hợp lệ.");
            }

            resp.sendRedirect(req.getContextPath() + "/admin/promotions?type=" + promotionType);

        } catch (IllegalArgumentException ex) {
            restoreFormAfterError(req, resp, promotionType, action, ex.getMessage());
        }
    }

    /* =========================================================
       LIST
    ========================================================= */

    private void showList(HttpServletRequest req, HttpServletResponse resp, String promotionType)
            throws ServletException, IOException {

        String q = safe(req.getParameter("q"));
        String status = safe(req.getParameter("status")).toLowerCase(Locale.ROOT);

        List<PromotionRow> rows = loadPromotionRows(promotionType);

        if (!q.isBlank()) {
            String keyword = q.toLowerCase(Locale.ROOT);
            rows = rows.stream()
                    .filter(row -> row.searchText().contains(keyword))
                    .collect(Collectors.toList());
        }

        if (!status.isBlank()) {
            rows = rows.stream()
                    .filter(row -> matchesStatus(row, status))
                    .collect(Collectors.toList());
        }

        rows.sort(Comparator.comparing(PromotionRow::getSortDate).reversed()
                .thenComparing(PromotionRow::getId, Comparator.reverseOrder()));

        loadRefs(req);

        req.setAttribute("pageTitle", "ADMIN | Khuyến mãi & Mã giảm giá");
        req.setAttribute("activeMenu", "promotions");
        req.setAttribute("pageCss", "/assets/css/admin/admin-promotion.css");

        req.setAttribute("type", promotionType);
        req.setAttribute("promotionType", promotionType);
        req.setAttribute("q", q);
        req.setAttribute("status", status);
        req.setAttribute("promotions", rows);
        req.setAttribute("promotionRows", rows);
        req.setAttribute("stats", buildStats(rows));

        req.getRequestDispatcher(LIST_JSP).forward(req, resp);
    }

    private List<PromotionRow> loadPromotionRows(String promotionType) {
        List<PromotionRow> rows = new ArrayList<>();

        if (TYPE_ALL.equals(promotionType) || TYPE_COUPON.equals(promotionType)) {
            for (Coupon coupon : couponDAO.findAll()) {
                rows.add(PromotionRow.fromCoupon(coupon));
            }
        }

        if (TYPE_ALL.equals(promotionType) || TYPE_BRAND.equals(promotionType)) {
            for (BrandDiscount discount : brandDiscountDAO.findAll(true)) {
                rows.add(PromotionRow.fromBrandDiscount(discount));
            }
        }

        if (TYPE_ALL.equals(promotionType) || TYPE_ORDER.equals(promotionType)) {
            for (OrderDiscount discount : orderDiscountDAO.findAll()) {
                rows.add(PromotionRow.fromOrderDiscount(discount));
            }
        }

        if (TYPE_ALL.equals(promotionType) || TYPE_EVENT.equals(promotionType)) {
            for (PromotionEvent event : promotionEventDAO.findAll(true)) {
                rows.add(PromotionRow.fromPromotionEvent(event));
            }
        }

        return rows;
    }

    private boolean matchesStatus(PromotionRow row, String status) {
        return switch (status) {
            case "active" -> row.isActiveNow();
            case "inactive" -> !row.isActive();
            case "expired" -> row.isExpired();
            case "upcoming" -> row.isUpcoming();
            default -> true;
        };
    }

    private PromotionStats buildStats(List<PromotionRow> rows) {
        int active = 0;
        int inactive = 0;
        int expired = 0;
        int upcoming = 0;

        for (PromotionRow row : rows) {
            if (row.isExpired()) {
                expired++;
            } else if (row.isUpcoming()) {
                upcoming++;
            } else if (row.isActive()) {
                active++;
            } else {
                inactive++;
            }
        }

        return new PromotionStats(rows.size(), active, inactive, expired, upcoming);
    }

    /* =========================================================
       FORM
    ========================================================= */

    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp, String promotionType)
            throws ServletException, IOException {

        req.setAttribute("mode", "create");
        req.setAttribute("type", promotionType);
        req.setAttribute("promotionType", promotionType);
        req.setAttribute("pageTitle", "ADMIN | Tạo khuyến mãi");
        req.setAttribute("activeMenu", "promotions");
        req.setAttribute("pageCss", "/assets/css/admin/admin-promotion.css");

        loadRefs(req);
        attachDefaultModel(req, promotionType);

        req.getRequestDispatcher(FORM_JSP).forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp, String promotionType, int id)
            throws ServletException, IOException {

        if (id <= 0 || TYPE_ALL.equals(promotionType)) {
            resp.sendRedirect(req.getContextPath() + "/admin/promotions");
            return;
        }

        boolean found = attachExistingModel(req, promotionType, id);
        if (!found) {
            resp.sendRedirect(req.getContextPath() + "/admin/promotions?type=" + promotionType);
            return;
        }

        req.setAttribute("mode", "edit");
        req.setAttribute("type", promotionType);
        req.setAttribute("promotionType", promotionType);
        req.setAttribute("pageTitle", "ADMIN | Sửa khuyến mãi");
        req.setAttribute("activeMenu", "promotions");
        req.setAttribute("pageCss", "/assets/css/admin/admin-promotion.css");

        loadRefs(req);
        req.getRequestDispatcher(FORM_JSP).forward(req, resp);
    }

    private void restoreFormAfterError(
            HttpServletRequest req,
            HttpServletResponse resp,
            String promotionType,
            String action,
            String errorMessage
    ) throws ServletException, IOException {

        req.setAttribute("error", errorMessage);
        req.setAttribute("mode", "update".equalsIgnoreCase(action) ? "edit" : "create");
        req.setAttribute("type", promotionType);
        req.setAttribute("promotionType", promotionType);
        req.setAttribute("pageTitle", "ADMIN | Khuyến mãi");
        req.setAttribute("activeMenu", "promotions");
        req.setAttribute("pageCss", "/assets/css/admin/admin-promotion.css");

        loadRefs(req);
        attachModelFromRequest(req, promotionType);

        req.getRequestDispatcher(FORM_JSP).forward(req, resp);
    }

    private void attachDefaultModel(HttpServletRequest req, String promotionType) {
        switch (promotionType) {
            case TYPE_COUPON: {
                Coupon coupon = new Coupon();
                coupon.setActive(true);
                coupon.setType(DEFAULT_COUPON_TYPE);
                coupon.setDiscountPercent(1);
                coupon.setMaxUses(1);
                coupon.setMinOrderAmount(BigDecimal.ZERO);
                coupon.setMinRankCode(DEFAULT_RANK_CODE);
                req.setAttribute("coupon", coupon);
                break;
            }

            case TYPE_BRAND: {
                BrandDiscount discount = new BrandDiscount();
                discount.setActive(true);
                discount.setDiscountType(DiscountType.PERCENT);
                req.setAttribute("discount", discount);
                req.setAttribute("brandDiscount", discount);
                break;
            }

            case TYPE_ORDER: {
                OrderDiscount discount = new OrderDiscount();
                discount.setActive(true);
                discount.setName("Giảm giá theo giá trị đơn hàng");
                discount.setMinOrderValue(BigDecimal.ZERO);
                discount.setDiscountPercent(BigDecimal.ONE);
                req.setAttribute("discount", discount);
                req.setAttribute("orderDiscount", discount);
                break;
            }

            case TYPE_EVENT: {
                PromotionEvent event = new PromotionEvent();
                event.setActive(true);
                event.setScope(PromotionEvent.Scope.ALL);
                event.setDiscountType(DiscountType.PERCENT);
                req.setAttribute("event", event);
                break;
            }

            default:
                break;
        }
    }

    private boolean attachExistingModel(HttpServletRequest req, String promotionType, int id) {
        switch (promotionType) {
            case TYPE_COUPON: {
                Coupon coupon = couponDAO.findById(id);
                if (coupon == null) return false;
                req.setAttribute("coupon", coupon);
                return true;
            }

            case TYPE_BRAND: {
                BrandDiscount discount = brandDiscountDAO.findById(id);
                if (discount == null) return false;
                req.setAttribute("discount", discount);
                req.setAttribute("brandDiscount", discount);
                return true;
            }

            case TYPE_ORDER: {
                OrderDiscount discount = orderDiscountDAO.findById(id);
                if (discount == null) return false;
                req.setAttribute("discount", discount);
                req.setAttribute("orderDiscount", discount);
                return true;
            }

            case TYPE_EVENT: {
                PromotionEvent event = promotionEventDAO.findById(id);
                if (event == null) return false;
                req.setAttribute("event", event);
                return true;
            }

            default:
                return false;
        }
    }

    private void attachModelFromRequest(HttpServletRequest req, String promotionType) {
        try {
            switch (promotionType) {
                case TYPE_COUPON: {
                    Coupon coupon = new Coupon();
                    coupon.setId(safeInt(req.getParameter("id"), 0));
                    bindCoupon(req, coupon);
                    req.setAttribute("coupon", coupon);
                    break;
                }

                case TYPE_BRAND: {
                    BrandDiscount discount = new BrandDiscount();
                    discount.setId(safeInt(req.getParameter("id"), 0));
                    bindBrandDiscount(req, discount);
                    req.setAttribute("discount", discount);
                    req.setAttribute("brandDiscount", discount);
                    break;
                }

                case TYPE_ORDER: {
                    OrderDiscount discount = new OrderDiscount();
                    discount.setId(safeInt(req.getParameter("id"), 0));
                    bindOrderDiscount(req, discount);
                    req.setAttribute("discount", discount);
                    req.setAttribute("orderDiscount", discount);
                    break;
                }

                case TYPE_EVENT: {
                    PromotionEvent event = new PromotionEvent();
                    event.setId(safeInt(req.getParameter("id"), 0));
                    bindPromotionEvent(req, event);
                    req.setAttribute("event", event);
                    break;
                }

                default:
                    break;
            }
        } catch (Exception ignored) {
            attachDefaultModel(req, promotionType);
        }
    }

    private void loadRefs(HttpServletRequest req) {
        req.setAttribute("brands", brandDAO.findAll());
        req.setAttribute("categories", categoryDAO.findParents());
    }

    /* =========================================================
       POST HANDLERS
    ========================================================= */

    private void handleCouponPost(HttpServletRequest req, String action) {
        switch (action.toLowerCase(Locale.ROOT)) {
            case "create": {
                Coupon coupon = new Coupon();
                bindCoupon(req, coupon);
                validateCoupon(coupon, false);
                couponDAO.create(coupon);
                return;
            }

            case "update": {
                int id = requireId(req);
                Coupon coupon = couponDAO.findById(id);
                if (coupon == null) {
                    throw new IllegalArgumentException("Không tìm thấy mã giảm giá cần cập nhật.");
                }

                bindCoupon(req, coupon);
                coupon.setId(id);
                validateCoupon(coupon, true);
                couponDAO.update(coupon);
                return;
            }

            case "delete": {
                int id = requireId(req);
                couponDAO.softDisable(id);
                return;
            }

            case "toggle": {
                int id = requireId(req);
                couponDAO.toggleActive(id);
                return;
            }

            default:
                throw new IllegalArgumentException("Hành động coupon không hợp lệ.");
        }
    }

    private void handleBrandDiscountPost(HttpServletRequest req, String action) {
        switch (action.toLowerCase(Locale.ROOT)) {
            case "create": {
                BrandDiscount discount = new BrandDiscount();
                bindBrandDiscount(req, discount);
                validateBrandDiscount(discount);
                brandDiscountDAO.create(discount);
                return;
            }

            case "update": {
                int id = requireId(req);
                BrandDiscount discount = brandDiscountDAO.findById(id);
                if (discount == null) {
                    throw new IllegalArgumentException("Không tìm thấy giảm giá thương hiệu cần cập nhật.");
                }

                bindBrandDiscount(req, discount);
                discount.setId(id);
                validateBrandDiscount(discount);
                brandDiscountDAO.update(discount);
                return;
            }

            case "delete": {
                int id = requireId(req);
                brandDiscountDAO.delete(id);
                return;
            }

            case "toggle": {
                int id = requireId(req);
                brandDiscountDAO.toggleActive(id);
                return;
            }

            default:
                throw new IllegalArgumentException("Hành động giảm giá thương hiệu không hợp lệ.");
        }
    }

    private void handleOrderDiscountPost(HttpServletRequest req, String action) {
        switch (action.toLowerCase(Locale.ROOT)) {
            case "create": {
                OrderDiscount discount = new OrderDiscount();
                bindOrderDiscount(req, discount);
                validateOrderDiscount(discount);
                orderDiscountDAO.create(discount);
                return;
            }

            case "update": {
                int id = requireId(req);
                OrderDiscount discount = orderDiscountDAO.findById(id);
                if (discount == null) {
                    throw new IllegalArgumentException("Không tìm thấy giảm giá theo đơn hàng cần cập nhật.");
                }

                bindOrderDiscount(req, discount);
                discount.setId(id);
                validateOrderDiscount(discount);
                orderDiscountDAO.update(discount);
                return;
            }

            case "delete": {
                int id = requireId(req);
                orderDiscountDAO.delete(id);
                return;
            }

            case "toggle": {
                int id = requireId(req);
                OrderDiscount discount = orderDiscountDAO.findById(id);
                if (discount == null) {
                    throw new IllegalArgumentException("Không tìm thấy giảm giá theo đơn hàng cần bật/tắt.");
                }

                discount.setActive(!discount.isActive());
                orderDiscountDAO.update(discount);
                return;
            }

            default:
                throw new IllegalArgumentException("Hành động giảm giá theo đơn hàng không hợp lệ.");
        }
    }

    private void handlePromotionEventPost(HttpServletRequest req, String action) {
        switch (action.toLowerCase(Locale.ROOT)) {
            case "create": {
                PromotionEvent event = new PromotionEvent();
                bindPromotionEvent(req, event);
                validatePromotionEvent(event);
                promotionEventDAO.create(event);
                return;
            }

            case "update": {
                int id = requireId(req);
                PromotionEvent event = promotionEventDAO.findById(id);
                if (event == null) {
                    throw new IllegalArgumentException("Không tìm thấy chương trình khuyến mãi cần cập nhật.");
                }

                bindPromotionEvent(req, event);
                event.setId(id);
                validatePromotionEvent(event);
                promotionEventDAO.update(event);
                return;
            }

            case "delete": {
                int id = requireId(req);
                promotionEventDAO.delete(id);
                return;
            }

            case "toggle": {
                int id = requireId(req);
                promotionEventDAO.toggleActive(id);
                return;
            }

            default:
                throw new IllegalArgumentException("Hành động chương trình khuyến mãi không hợp lệ.");
        }
    }

    /* =========================================================
       BIND
    ========================================================= */

    private void bindCoupon(HttpServletRequest req, Coupon coupon) {
        coupon.setCode(safe(req.getParameter("code")).toUpperCase(Locale.ROOT));

        String couponType = safe(req.getParameter("couponType"));
        if (couponType.isBlank()) {
            couponType = safe(req.getParameter("type"));
        }

        couponType = couponType.toUpperCase(Locale.ROOT);
        if (couponType.isBlank() || isPromotionType(couponType)) {
            couponType = DEFAULT_COUPON_TYPE;
        }

        coupon.setType(couponType);
        coupon.setDescription(safe(req.getParameter("description")));
        coupon.setDiscountPercent(safeInt(req.getParameter("discountPercent"), 0));
        coupon.setMaxUses(safeInt(req.getParameter("maxUses"), 1));

        coupon.setMinOrderAmount(parseBigDecimal(
                req.getParameter("minOrderAmount"),
                BigDecimal.ZERO,
                "Đơn tối thiểu không hợp lệ."
        ));

        String minRankCode = safe(req.getParameter("minRankCode")).toUpperCase(Locale.ROOT);
        coupon.setMinRankCode(minRankCode.isBlank() ? DEFAULT_RANK_CODE : minRankCode);

        String maxDiscountAmount = req.getParameter("maxDiscountAmount");
        coupon.setMaxDiscountAmount(isBlank(maxDiscountAmount)
                ? null
                : parseBigDecimal(maxDiscountAmount, null, "Số tiền giảm tối đa không hợp lệ."));

        coupon.setStartDate(safeDate(req.getParameter("startDate")));
        coupon.setEndDate(safeDate(req.getParameter("endDate")));
        coupon.setActive(parseActive(req));
    }

    private void bindBrandDiscount(HttpServletRequest req, BrandDiscount discount) {
        discount.setBrandId(safeInt(req.getParameter("brandId"), -1));
        discount.setDiscountType(safeDiscountType(req.getParameter("discountType")));
        discount.setDiscountValue(parseBigDecimal(
                req.getParameter("discountValue"),
                null,
                "Giá trị giảm không hợp lệ."
        ));

        String maxDiscountAmount = req.getParameter("maxDiscountAmount");
        discount.setMaxDiscountAmount(isBlank(maxDiscountAmount)
                ? null
                : parseBigDecimal(maxDiscountAmount, null, "Số tiền giảm tối đa không hợp lệ."));

        discount.setStartDate(safeDate(req.getParameter("startDate")));
        discount.setEndDate(safeDate(req.getParameter("endDate")));
        discount.setActive(parseActive(req));
    }

    private void bindOrderDiscount(HttpServletRequest req, OrderDiscount discount) {
        String name = safe(req.getParameter("name"));
        discount.setName(name.isBlank() ? "Giảm giá theo giá trị đơn hàng" : name);

        discount.setMinOrderValue(parseBigDecimal(
                req.getParameter("minOrderValue"),
                null,
                "Giá trị đơn hàng tối thiểu không hợp lệ."
        ));

        discount.setDiscountPercent(parseBigDecimal(
                req.getParameter("discountPercent"),
                null,
                "Phần trăm giảm không hợp lệ."
        ));

        String maxDiscountAmount = req.getParameter("maxDiscountAmount");
        discount.setMaxDiscountAmount(isBlank(maxDiscountAmount)
                ? null
                : parseBigDecimal(maxDiscountAmount, null, "Số tiền giảm tối đa không hợp lệ."));

        discount.setStartDate(safeDate(req.getParameter("startDate")));
        discount.setEndDate(safeDate(req.getParameter("endDate")));
        discount.setActive(parseActive(req));
    }

    private void bindPromotionEvent(HttpServletRequest req, PromotionEvent event) {
        event.setName(safe(req.getParameter("name")));
        event.setScope(safeEnum(
                PromotionEvent.Scope.class,
                req.getParameter("scope"),
                "Phạm vi áp dụng không hợp lệ."
        ));

        event.setDiscountType(safeDiscountType(req.getParameter("discountType")));
        event.setDiscountValue(parseBigDecimal(
                req.getParameter("discountValue"),
                null,
                "Giá trị giảm không hợp lệ."
        ));

        String maxDiscountAmount = req.getParameter("maxDiscountAmount");
        event.setMaxDiscountAmount(isBlank(maxDiscountAmount)
                ? null
                : parseBigDecimal(maxDiscountAmount, null, "Số tiền giảm tối đa không hợp lệ."));

        int categoryId = safeInt(req.getParameter("categoryId"), -1);
        int brandId = safeInt(req.getParameter("brandId"), -1);

        event.setCategoryId(categoryId > 0 ? categoryId : null);
        event.setBrandId(brandId > 0 ? brandId : null);

        event.setStartDate(safeDate(req.getParameter("startDate")));
        event.setEndDate(safeDate(req.getParameter("endDate")));
        event.setActive(parseActive(req));
    }

    /* =========================================================
       VALIDATE
    ========================================================= */

    private void validateCoupon(Coupon coupon, boolean isUpdate) {
        if (coupon.getCode() == null || coupon.getCode().isBlank()) {
            throw new IllegalArgumentException("Mã coupon không được để trống.");
        }

        if (!isUpdate && couponDAO.existsByCode(coupon.getCode())) {
            throw new IllegalArgumentException("Mã coupon đã tồn tại.");
        }

        if (isUpdate && isDuplicateCodeForOtherCoupon(coupon)) {
            throw new IllegalArgumentException("Mã coupon đã được sử dụng bởi coupon khác.");
        }

        if (coupon.getType() == null || !VALID_COUPON_TYPES.contains(coupon.getType())) {
            throw new IllegalArgumentException("Loại coupon không hợp lệ.");
        }

        if (coupon.getDiscountPercent() <= 0 || coupon.getDiscountPercent() > 100) {
            throw new IllegalArgumentException("Phần trăm giảm giá phải nằm trong khoảng 1 đến 100.");
        }

        if (coupon.getMaxUses() < 1) {
            throw new IllegalArgumentException("Số lượt dùng tối đa phải lớn hơn hoặc bằng 1.");
        }

        validateNonNegative(coupon.getMinOrderAmount(), "Đơn tối thiểu không được âm.");
        validatePositiveIfPresent(coupon.getMaxDiscountAmount(), "Số tiền giảm tối đa nếu nhập phải lớn hơn 0.");

        if (!VALID_RANK_CODES.contains(coupon.getMinRankCode())) {
            throw new IllegalArgumentException("Rank tối thiểu không hợp lệ.");
        }

        validateDateRange(coupon.getStartDate(), coupon.getEndDate());
    }

    private void validateBrandDiscount(BrandDiscount discount) {
        if (discount.getBrandId() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn thương hiệu.");
        }

        validateDiscountTypeAndValue(discount.getDiscountType(), discount.getDiscountValue());
        validatePositiveIfPresent(discount.getMaxDiscountAmount(), "Số tiền giảm tối đa nếu nhập phải lớn hơn 0.");
        validateRequiredDateRange(discount.getStartDate(), discount.getEndDate());
    }

    private void validateOrderDiscount(OrderDiscount discount) {
        if (discount.getName() == null || discount.getName().isBlank()) {
            throw new IllegalArgumentException("Tên chương trình không được để trống.");
        }

        if (discount.getMinOrderValue() == null || discount.getMinOrderValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá trị đơn hàng tối thiểu không được âm.");
        }

        if (discount.getDiscountPercent() == null
                || discount.getDiscountPercent().compareTo(BigDecimal.ZERO) <= 0
                || discount.getDiscountPercent().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Phần trăm giảm theo đơn hàng phải nằm trong khoảng 1 đến 100.");
        }

        validatePositiveIfPresent(discount.getMaxDiscountAmount(), "Số tiền giảm tối đa nếu nhập phải lớn hơn 0.");
        validateRequiredDateRange(discount.getStartDate(), discount.getEndDate());
    }

    private void validatePromotionEvent(PromotionEvent event) {
        if (event.getName() == null || event.getName().isBlank()) {
            throw new IllegalArgumentException("Tên chương trình không được để trống.");
        }

        if (event.getScope() == null) {
            throw new IllegalArgumentException("Vui lòng chọn phạm vi áp dụng.");
        }

        validateDiscountTypeAndValue(event.getDiscountType(), event.getDiscountValue());
        validatePositiveIfPresent(event.getMaxDiscountAmount(), "Số tiền giảm tối đa nếu nhập phải lớn hơn 0.");
        validateRequiredDateRange(event.getStartDate(), event.getEndDate());

        if (event.getScope() == PromotionEvent.Scope.CATEGORY && event.getCategoryId() == null) {
            throw new IllegalArgumentException("Scope CATEGORY cần chọn danh mục.");
        }

        if (event.getScope() == PromotionEvent.Scope.BRAND && event.getBrandId() == null) {
            throw new IllegalArgumentException("Scope BRAND cần chọn thương hiệu.");
        }

        if (event.getScope() == PromotionEvent.Scope.ALL) {
            event.setCategoryId(null);
            event.setBrandId(null);
        }
    }

    private void validateDiscountTypeAndValue(DiscountType discountType, BigDecimal discountValue) {
        if (discountType == null) {
            throw new IllegalArgumentException("Vui lòng chọn kiểu giảm giá.");
        }

        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0.");
        }

        if (discountType == DiscountType.PERCENT && discountValue.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Giảm theo phần trăm không được lớn hơn 100.");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
        }
    }

    private void validateRequiredDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày bắt đầu và ngày kết thúc.");
        }

        validateDateRange(startDate, endDate);
    }

    private void validateNonNegative(BigDecimal value, String message) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validatePositiveIfPresent(BigDecimal value, String message) {
        if (value != null && value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean isDuplicateCodeForOtherCoupon(Coupon coupon) {
        return couponDAO.findAll()
                .stream()
                .anyMatch(existing -> existing.getId() != coupon.getId()
                        && existing.getCode() != null
                        && existing.getCode().equalsIgnoreCase(coupon.getCode()));
    }

    /* =========================================================
       SAFE HELPERS
    ========================================================= */

    private void prepareEncoding(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
    }

    private int requireId(HttpServletRequest req) {
        int id = safeInt(req.getParameter("id"), -1);

        if (id <= 0) {
            throw new IllegalArgumentException("ID không hợp lệ.");
        }

        return id;
    }

    private int safeInt(String value, int defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }

            return Integer.parseInt(value.trim());

        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private BigDecimal parseBigDecimal(String value, BigDecimal defaultValue, String errorMessage) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }

            return new BigDecimal(value.trim());

        } catch (Exception e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private LocalDate safeDate(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }

            return LocalDate.parse(value.trim());

        } catch (Exception e) {
            throw new IllegalArgumentException("Ngày không hợp lệ, định dạng đúng là yyyy-MM-dd: " + value);
        }
    }

    private boolean parseActive(HttpServletRequest req) {
        String active = req.getParameter("active");

        return "1".equals(active)
                || "true".equalsIgnoreCase(active)
                || "on".equalsIgnoreCase(active);
    }

    private DiscountType safeDiscountType(String rawValue) {
        String value = safe(rawValue).toUpperCase(Locale.ROOT);

        if (value.isBlank()) {
            return null;
        }

        /*
         * Tương thích với form cũ đang dùng AMOUNT.
         * Enum hiện tại của project dùng FIXED cho giảm theo số tiền.
         */
        if ("AMOUNT".equals(value)) {
            value = "FIXED";
        }

        try {
            return DiscountType.valueOf(value);

        } catch (Exception e) {
            throw new IllegalArgumentException("Kiểu giảm giá không hợp lệ: " + rawValue);
        }
    }

    private <E extends Enum<E>> E safeEnum(Class<E> enumType, String rawValue, String errorMessage) {
        String value = safe(rawValue).toUpperCase(Locale.ROOT);

        if (value.isBlank()) {
            return null;
        }

        try {
            return Enum.valueOf(enumType, value);

        } catch (Exception e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private String normalizePromotionType(String rawType) {
        String type = safe(rawType).toUpperCase(Locale.ROOT);

        if (type.isBlank()) {
            return TYPE_ALL;
        }

        return switch (type) {
            case "ALL" -> TYPE_ALL;
            case "COUPON", "COUPONS", "DISCOUNT_CODE", "VOUCHER" -> TYPE_COUPON;
            case "BRAND", "BRAND_DISCOUNT", "BRANDDISCOUNTS" -> TYPE_BRAND;
            case "ORDER", "ORDER_DISCOUNT", "ORDERDISCOUNTS" -> TYPE_ORDER;
            case "EVENT", "PROMOTION", "PROMOTION_EVENT", "PROMOTIONEVENTS" -> TYPE_EVENT;
            default -> TYPE_ALL;
        };
    }

    private String normalizeCreateType(String promotionType) {
        return TYPE_ALL.equals(promotionType) ? TYPE_COUPON : promotionType;
    }

    private boolean isPromotionType(String value) {
        return TYPE_ALL.equals(value)
                || TYPE_COUPON.equals(value)
                || TYPE_BRAND.equals(value)
                || TYPE_ORDER.equals(value)
                || TYPE_EVENT.equals(value);
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

    /* =========================================================
       VIEW MODELS FOR JSP
    ========================================================= */

    public static class PromotionStats {
        private final int total;
        private final int active;
        private final int inactive;
        private final int expired;
        private final int upcoming;

        public PromotionStats(int total, int active, int inactive, int expired, int upcoming) {
            this.total = total;
            this.active = active;
            this.inactive = inactive;
            this.expired = expired;
            this.upcoming = upcoming;
        }

        public int getTotal() {
            return total;
        }

        public int getActive() {
            return active;
        }

        public int getInactive() {
            return inactive;
        }

        public int getExpired() {
            return expired;
        }

        public int getUpcoming() {
            return upcoming;
        }
    }

    public static class PromotionRow {
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

        public static PromotionRow fromCoupon(Coupon coupon) {
            PromotionRow row = new PromotionRow();

            row.id = coupon.getId();
            row.type = TYPE_COUPON;
            row.typeLabel = "Mã giảm giá";
            row.code = coupon.getCode();
            row.title = coupon.getCode();
            row.scopeLabel = "Theo mã coupon";
            row.discountType = "PERCENT";
            row.discountValueLabel = coupon.getDiscountPercent() + "%";
            row.conditionLabel = "Đơn tối thiểu: " + money(coupon.getMinOrderAmount())
                    + " | Rank: " + coupon.getMinRankCode()
                    + " | Lượt dùng: " + coupon.getUsedCount() + "/" + coupon.getMaxUses();
            row.periodLabel = dateRange(coupon.getStartDate(), coupon.getEndDate());
            row.startDate = coupon.getStartDate();
            row.endDate = coupon.getEndDate();
            row.active = coupon.isActive();

            return row;
        }

        public static PromotionRow fromBrandDiscount(BrandDiscount discount) {
            PromotionRow row = new PromotionRow();

            row.id = discount.getId();
            row.type = TYPE_BRAND;
            row.typeLabel = "Giảm giá thương hiệu";
            row.title = !isEmpty(discount.getBrandName())
                    ? "Thương hiệu: " + discount.getBrandName()
                    : "Thương hiệu #" + discount.getBrandId();
            row.code = "";
            row.scopeLabel = !isEmpty(discount.getBrandName())
                    ? discount.getBrandName()
                    : "Brand #" + discount.getBrandId();
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

        public static PromotionRow fromOrderDiscount(OrderDiscount discount) {
            PromotionRow row = new PromotionRow();

            row.id = discount.getId();
            row.type = TYPE_ORDER;
            row.typeLabel = "Giảm theo đơn hàng";
            row.title = isEmpty(discount.getName()) ? "Giảm giá theo giá trị đơn hàng" : discount.getName();
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

        public static PromotionRow fromPromotionEvent(PromotionEvent event) {
            PromotionRow row = new PromotionRow();

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
            };
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
}
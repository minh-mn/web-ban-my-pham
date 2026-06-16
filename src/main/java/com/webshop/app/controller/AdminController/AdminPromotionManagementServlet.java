package com.webshop.app.controller.AdminController;

import java.io.IOException;
import java.math.BigDecimal;
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
import com.webshop.app.dao.ProductDAO;
import com.webshop.app.model.BrandDiscount;
import com.webshop.app.model.Coupon;
import com.webshop.app.model.DiscountType;
import com.webshop.app.model.OrderDiscount;
import com.webshop.app.model.PromotionEvent;
import com.webshop.app.model.Product;
import com.webshop.app.model.admin.AdminPromotionRow;
import com.webshop.app.model.admin.AdminPromotionStats;

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

    private static final String COUPON_SCOPE_ALL = "ALL";
    private static final String COUPON_SCOPE_BRAND = "BRAND";
    private static final String COUPON_SCOPE_PRODUCTS = "PRODUCTS";

    private static final String BRAND_SCOPE_ALL_BRAND_PRODUCTS = "ALL_BRAND_PRODUCTS";
    private static final String BRAND_SCOPE_SELECTED_PRODUCTS = "SELECTED_PRODUCTS";

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
    private final ProductDAO productDAO = new ProductDAO();

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

        List<AdminPromotionRow> rows = loadPromotionRows(promotionType);

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

        rows.sort(Comparator.comparing(AdminPromotionRow::getSortDate).reversed()
                .thenComparing(AdminPromotionRow::getId, Comparator.reverseOrder()));

        loadRefs(req);

        req.setAttribute("pageTitle", "ADMIN | Khuyến mãi & Mã giảm giá");
        req.setAttribute("activeMenu", "promotions");
        req.setAttribute("pageCss", "/assets/css/admin/admin-list.css");

        req.setAttribute("type", promotionType);
        req.setAttribute("promotionType", promotionType);
        req.setAttribute("q", q);
        req.setAttribute("status", status);
        req.setAttribute("promotions", rows);
        req.setAttribute("promotionRows", rows);
        req.setAttribute("stats", buildStats(rows));

        req.getRequestDispatcher(LIST_JSP).forward(req, resp);
    }

    private List<AdminPromotionRow> loadPromotionRows(String promotionType) {
        List<AdminPromotionRow> rows = new ArrayList<>();

        if (TYPE_ALL.equals(promotionType) || TYPE_COUPON.equals(promotionType)) {
            for (Coupon coupon : couponDAO.findAll()) {
                rows.add(AdminPromotionRow.fromCoupon(coupon));
            }
        }

        if (TYPE_ALL.equals(promotionType) || TYPE_BRAND.equals(promotionType)) {
            for (BrandDiscount discount : brandDiscountDAO.findAll(true)) {
                rows.add(AdminPromotionRow.fromBrandDiscount(discount));
            }
        }

        if (TYPE_ALL.equals(promotionType) || TYPE_ORDER.equals(promotionType)) {
            for (OrderDiscount discount : orderDiscountDAO.findAll()) {
                rows.add(AdminPromotionRow.fromOrderDiscount(discount));
            }
        }

        if (TYPE_ALL.equals(promotionType) || TYPE_EVENT.equals(promotionType)) {
            for (PromotionEvent event : promotionEventDAO.findAll(true)) {
                rows.add(AdminPromotionRow.fromPromotionEvent(event));
            }
        }

        return rows;
    }

    private boolean matchesStatus(AdminPromotionRow row, String status) {
        return switch (status) {
            case "active" -> row.isActiveNow();
            case "inactive" -> !row.isActive();
            case "expired" -> row.isExpired();
            case "upcoming" -> row.isUpcoming();
            default -> true;
        };
    }

    private AdminPromotionStats buildStats(List<AdminPromotionRow> rows) {
        return AdminPromotionStats.fromRows(rows);
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
        req.setAttribute("pageCss", "/assets/css/admin/admin-form.css");

        attachDefaultModel(req, promotionType);
        loadRefs(req, promotionType);

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
        req.setAttribute("pageCss", "/assets/css/admin/admin-form.css");

        loadRefs(req, promotionType);
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
        req.setAttribute("pageCss", "/assets/css/admin/admin-form.css");

        attachModelFromRequest(req, promotionType);
        loadRefs(req, promotionType);

        req.getRequestDispatcher(FORM_JSP).forward(req, resp);
    }

    private void attachDefaultModel(HttpServletRequest req, String promotionType) {
        switch (promotionType) {
            case TYPE_COUPON: {
                Coupon coupon = new Coupon();
                coupon.setActive(true);
                coupon.setType(DEFAULT_COUPON_TYPE);
                coupon.setDiscountPercent(1);
                coupon.setDiscountType(DiscountType.PERCENT);
                coupon.setDiscountValue(BigDecimal.ONE);
                coupon.setMaxUses(0);
                coupon.setMinOrderAmount(BigDecimal.ZERO);
                coupon.setMinRankCode(DEFAULT_RANK_CODE);
                coupon.setApplyScope(COUPON_SCOPE_ALL);
                req.setAttribute("coupon", coupon);
                break;
            }

            case TYPE_BRAND: {
                BrandDiscount discount = new BrandDiscount();
                discount.setActive(true);
                discount.setDiscountType(DiscountType.PERCENT);
                discount.setApplyScope(BRAND_SCOPE_ALL_BRAND_PRODUCTS);
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

    private void loadRefs(HttpServletRequest req, String promotionType) {
        req.setAttribute("brands", brandDAO.findAll());
        req.setAttribute("categories", categoryDAO.findParents());

        List<Integer> selectedProductIds = resolveSelectedProductIds(req, promotionType);

        List<Product> products = productDAO.findForPromotionPicker(
                "",
                null,
                null,
                true,
                selectedProductIds
        );

        req.setAttribute("products", products);
        req.setAttribute("selectedProductIds", selectedProductIds);
    }

    private List<Integer> resolveSelectedProductIds(HttpServletRequest req, String promotionType) {
        if (TYPE_COUPON.equals(promotionType)) {
            Object model = req.getAttribute("coupon");
            if (model instanceof Coupon coupon) {
                return coupon.getSelectedProductIds();
            }
        }

        if (TYPE_BRAND.equals(promotionType)) {
            Object model = req.getAttribute("discount");
            if (model instanceof BrandDiscount discount) {
                return discount.getSelectedProductIds();
            }
        }

        if (TYPE_EVENT.equals(promotionType)) {
            Object model = req.getAttribute("event");
            if (model instanceof PromotionEvent event) {
                return event.getSelectedProductIds();
            }
        }

        return parseSelectedProductIds(req);
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

        DiscountType discountType = safeDiscountType(req.getParameter("discountType"));
        if (discountType == null) {
            discountType = DiscountType.PERCENT;
        }
        coupon.setDiscountType(discountType);

        BigDecimal discountValue = parseBigDecimal(
                req.getParameter("discountValue"),
                null,
                "Giá trị giảm không hợp lệ."
        );

        int discountPercent = safeInt(req.getParameter("discountPercent"), 0);
        if (discountValue == null && discountPercent > 0) {
            discountValue = BigDecimal.valueOf(discountPercent);
        }

        if (discountValue == null) {
            discountValue = BigDecimal.ZERO;
        }

        coupon.setDiscountValue(discountValue);

        if (discountType == DiscountType.PERCENT) {
            coupon.setDiscountPercent(discountValue.intValue());
        } else {
            coupon.setDiscountPercent(0);
        }

        coupon.setMaxUses(safeInt(req.getParameter("maxUses"), 0));

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

        String applyScope = normalizeCouponApplyScope(req.getParameter("applyScope"));
        coupon.setApplyScope(applyScope);

        int brandId = safeInt(req.getParameter("brandId"), -1);
        coupon.setBrandId(COUPON_SCOPE_BRAND.equals(applyScope) && brandId > 0 ? brandId : 0);
        coupon.setSelectedProductIds(parseSelectedProductIds(req));

        coupon.setStartDate(safeDate(req.getParameter("startDate")));
        coupon.setEndDate(safeDate(req.getParameter("endDate")));
        coupon.setActive(parseActive(req));
    }

    private void bindBrandDiscount(HttpServletRequest req, BrandDiscount discount) {
        discount.setBrandId(safeInt(req.getParameter("brandId"), -1));

        String applyScope = normalizeBrandApplyScope(req.getParameter("applyScope"));
        discount.setApplyScope(applyScope);

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

        discount.setSelectedProductIds(parseSelectedProductIds(req));

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
        event.setSelectedProductIds(parseSelectedProductIds(req));

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

        validateDiscountTypeAndValue(coupon.getDiscountType(), coupon.getDiscountValue());

        if (coupon.getDiscountType() == DiscountType.PERCENT
                && (coupon.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0
                || coupon.getDiscountValue().compareTo(new BigDecimal("100")) > 0)) {
            throw new IllegalArgumentException("Phần trăm giảm giá phải nằm trong khoảng 1 đến 100.");
        }

        if (coupon.getMaxUses() < 0) {
            throw new IllegalArgumentException("Số lượt dùng tối đa không được âm. Nhập 0 nếu muốn không giới hạn.");
        }

        if (isUpdate
                && coupon.getMaxUses() > 0
                && coupon.getUsedCount() > coupon.getMaxUses()) {
            throw new IllegalArgumentException(
                    "Số lượt dùng tối đa không được nhỏ hơn số lượt đã sử dụng hiện tại. "
                            + "Đã dùng: " + coupon.getUsedCount()
                            + ", giới hạn mới: " + coupon.getMaxUses()
            );
        }

        validateNonNegative(coupon.getMinOrderAmount(), "Đơn tối thiểu không được âm.");
        validatePositiveIfPresent(coupon.getMaxDiscountAmount(), "Số tiền giảm tối đa nếu nhập phải lớn hơn 0.");

        if (!VALID_RANK_CODES.contains(coupon.getMinRankCode())) {
            throw new IllegalArgumentException("Rank tối thiểu không hợp lệ.");
        }

        String applyScope = normalizeCouponApplyScope(coupon.getApplyScope());

        if (COUPON_SCOPE_BRAND.equals(applyScope)
                && (coupon.getBrandId() == null || coupon.getBrandId() <= 0)) {
            throw new IllegalArgumentException("Coupon áp dụng theo thương hiệu cần chọn thương hiệu.");
        }

        if (COUPON_SCOPE_PRODUCTS.equals(applyScope)
                && coupon.getSelectedProductIds().isEmpty()) {
            throw new IllegalArgumentException("Coupon áp dụng theo sản phẩm cụ thể cần chọn ít nhất một sản phẩm.");
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

        String applyScope = normalizeBrandApplyScope(discount.getApplyScope());
        if (BRAND_SCOPE_SELECTED_PRODUCTS.equals(applyScope)
                && discount.getSelectedProductIds().isEmpty()) {
            throw new IllegalArgumentException("Giảm giá thương hiệu theo sản phẩm cụ thể cần chọn ít nhất một sản phẩm.");
        }
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

        if (event.getScope() == PromotionEvent.Scope.PRODUCTS
                && event.getSelectedProductIds().isEmpty()) {
            throw new IllegalArgumentException("Scope PRODUCTS cần chọn ít nhất một sản phẩm.");
        }

        if (event.getScope() == PromotionEvent.Scope.ALL
                || event.getScope() == PromotionEvent.Scope.PRODUCTS) {
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

        if (discountType == DiscountType.FIXED
                && discountValue.setScale(0, java.math.RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giảm theo số tiền phải lớn hơn 0.");
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

    private List<Integer> parseSelectedProductIds(HttpServletRequest req) {
        List<Integer> productIds = new ArrayList<>();

        appendProductIds(productIds, req.getParameterValues("selectedProductIds"));
        appendProductIds(productIds, req.getParameterValues("productIds"));
        appendProductIds(productIds, req.getParameterValues("selectedProducts"));

        return productIds.stream()
                .distinct()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toList());
    }

    private void appendProductIds(List<Integer> target, String[] rawValues) {
        if (rawValues == null || rawValues.length == 0) {
            return;
        }

        for (String rawValue : rawValues) {
            if (rawValue == null || rawValue.isBlank()) {
                continue;
            }

            String[] parts = rawValue.split(",");

            for (String part : parts) {
                int id = safeInt(part, -1);
                if (id > 0) {
                    target.add(id);
                }
            }
        }
    }


    private String normalizeCouponApplyScope(String rawScope) {
        String scope = safe(rawScope).toUpperCase(Locale.ROOT);

        return switch (scope) {
            case COUPON_SCOPE_BRAND -> COUPON_SCOPE_BRAND;
            case COUPON_SCOPE_PRODUCTS, "PRODUCT", "SELECTED_PRODUCTS" -> COUPON_SCOPE_PRODUCTS;
            default -> COUPON_SCOPE_ALL;
        };
    }

    private String normalizeBrandApplyScope(String rawScope) {
        String scope = safe(rawScope).toUpperCase(Locale.ROOT);

        return switch (scope) {
            case BRAND_SCOPE_SELECTED_PRODUCTS, "PRODUCTS", "PRODUCT" -> BRAND_SCOPE_SELECTED_PRODUCTS;
            default -> BRAND_SCOPE_ALL_BRAND_PRODUCTS;
        };
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

}
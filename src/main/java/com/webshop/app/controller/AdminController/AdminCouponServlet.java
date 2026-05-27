package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.CouponDAO;
import com.webshop.app.model.Coupon;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@WebServlet("/admin/coupons")
public class AdminCouponServlet extends HttpServlet {

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

    private static final String DEFAULT_COUPON_TYPE = "DISCOUNT";
    private static final String DEFAULT_RANK_CODE = "MEMBER";

    private final CouponDAO couponDAO = new CouponDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        couponDAO.deactivateExpiredCoupons();

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "list";
        }

        switch (action) {

            case "new": {
                Coupon coupon = new Coupon();
                coupon.setActive(true);
                coupon.setType(DEFAULT_COUPON_TYPE);
                coupon.setMinOrderAmount(BigDecimal.ZERO);
                coupon.setMinRankCode(DEFAULT_RANK_CODE);

                req.setAttribute("mode", "create");
                req.setAttribute("coupon", coupon);
                req.getRequestDispatcher("/jsp/admin/coupon/coupon_form.jsp").forward(req, resp);
                return;
            }

            case "edit": {
                int id = safeInt(req.getParameter("id"), -1);
                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
                }

                Coupon coupon = couponDAO.findById(id);
                if (coupon == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
                }

                req.setAttribute("mode", "edit");
                req.setAttribute("coupon", coupon);
                req.getRequestDispatcher("/jsp/admin/coupon/coupon_form.jsp").forward(req, resp);
                return;
            }

            case "list":
            default: {
                String q = safe(req.getParameter("q"));
                String status = safe(req.getParameter("status"));

                List<Coupon> coupons = couponDAO.findAll();

                if (!q.isEmpty()) {
                    String keyword = q.toUpperCase();
                    coupons = coupons.stream()
                            .filter(coupon ->
                                    coupon.getCode() != null
                                            && coupon.getCode().toUpperCase().contains(keyword)
                            )
                            .collect(Collectors.toList());
                }

                if ("active".equalsIgnoreCase(status)) {
                    coupons = coupons.stream()
                            .filter(Coupon::isActive)
                            .collect(Collectors.toList());
                } else if ("inactive".equalsIgnoreCase(status)) {
                    coupons = coupons.stream()
                            .filter(coupon -> !coupon.isActive())
                            .collect(Collectors.toList());
                }

                req.setAttribute("q", q);
                req.setAttribute("status", status);
                req.setAttribute("coupons", coupons);
                req.getRequestDispatcher("/jsp/admin/coupon/coupon_list.jsp").forward(req, resp);
                return;
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "create";
        }

        try {
            switch (action) {

                case "create": {
                    Coupon coupon = new Coupon();

                    bind(req, coupon);
                    validate(coupon, false);

                    couponDAO.create(coupon);

                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
                }

                case "update": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id <= 0) {
                        resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                        return;
                    }

                    Coupon coupon = couponDAO.findById(id);
                    if (coupon == null) {
                        resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                        return;
                    }

                    bind(req, coupon);
                    coupon.setId(id);
                    validate(coupon, true);

                    couponDAO.update(coupon);

                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
                }

                case "delete": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id > 0) {
                        couponDAO.softDisable(id);
                    }

                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
                }

                case "toggle": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id > 0) {
                        couponDAO.toggleActive(id);
                    }

                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
                }

                default: {
                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
                }
            }

        } catch (IllegalArgumentException ex) {
            Coupon coupon = new Coupon();

            if ("update".equalsIgnoreCase(action)) {
                req.setAttribute("mode", "edit");
                coupon.setId(safeInt(req.getParameter("id"), 0));
            } else {
                req.setAttribute("mode", "create");
            }

            try {
                bind(req, coupon);
            } catch (Exception ignore) {
                // Giữ lỗi validate chính để hiển thị cho admin.
            }

            req.setAttribute("error", ex.getMessage());
            req.setAttribute("coupon", coupon);
            req.getRequestDispatcher("/jsp/admin/coupon/coupon_form.jsp").forward(req, resp);
        }
    }

    /* ===================== BIND + VALIDATE ===================== */

    private void bind(HttpServletRequest req, Coupon coupon) {

        String code = safe(req.getParameter("code")).toUpperCase();
        coupon.setCode(code);

        String type = safe(req.getParameter("type")).toUpperCase();
        if (type.isEmpty()) {
            type = DEFAULT_COUPON_TYPE;
        }
        coupon.setType(type);

        coupon.setDescription(safe(req.getParameter("description")));

        BigDecimal minOrderAmount = parseBigDecimal(
                req.getParameter("minOrderAmount"),
                BigDecimal.ZERO,
                "Đơn tối thiểu không hợp lệ."
        );
        coupon.setMinOrderAmount(minOrderAmount);

        String minRankCode = safe(req.getParameter("minRankCode")).toUpperCase();
        if (minRankCode.isEmpty()) {
            minRankCode = DEFAULT_RANK_CODE;
        }
        coupon.setMinRankCode(minRankCode);

        coupon.setDiscountPercent(safeInt(req.getParameter("discountPercent"), 0));

        coupon.setMaxUses(safeInt(req.getParameter("maxUses"), 1));

        String maxDiscountAmountRaw = req.getParameter("maxDiscountAmount");
        if (maxDiscountAmountRaw == null || maxDiscountAmountRaw.trim().isEmpty()) {
            coupon.setMaxDiscountAmount(null);
        } else {
            BigDecimal maxDiscountAmount = parseBigDecimal(
                    maxDiscountAmountRaw,
                    null,
                    "Số tiền giảm tối đa không hợp lệ."
            );
            coupon.setMaxDiscountAmount(maxDiscountAmount);
        }

        coupon.setStartDate(safeDate(req.getParameter("startDate")));
        coupon.setEndDate(safeDate(req.getParameter("endDate")));

        boolean active =
                "1".equals(req.getParameter("active"))
                        || "true".equalsIgnoreCase(req.getParameter("active"))
                        || "on".equalsIgnoreCase(req.getParameter("active"));

        coupon.setActive(active);
    }

    private void validate(Coupon coupon, boolean isUpdate) {

        if (coupon.getCode() == null || coupon.getCode().isBlank()) {
            throw new IllegalArgumentException("Mã coupon không được để trống.");
        }

        if (!isUpdate && couponDAO.existsByCode(coupon.getCode())) {
            throw new IllegalArgumentException("Mã coupon đã tồn tại.");
        }

        if (isUpdate && isDuplicateCodeForOtherCoupon(coupon)) {
            throw new IllegalArgumentException("Mã coupon đã được sử dụng bởi coupon khác.");
        }

        if (coupon.getType() == null || coupon.getType().isBlank()) {
            throw new IllegalArgumentException("Loại coupon không được để trống.");
        }

        if (!VALID_COUPON_TYPES.contains(coupon.getType())) {
            throw new IllegalArgumentException("Loại coupon không hợp lệ.");
        }

        if (coupon.getDiscountPercent() <= 0 || coupon.getDiscountPercent() > 100) {
            throw new IllegalArgumentException("Phần trăm giảm giá phải nằm trong khoảng 1 đến 100.");
        }

        if (coupon.getMaxUses() < 1) {
            throw new IllegalArgumentException("Số lượt dùng tối đa phải lớn hơn hoặc bằng 1.");
        }

        if (coupon.getMaxDiscountAmount() != null
                && coupon.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số tiền giảm tối đa không được âm.");
        }

        if (coupon.getMinOrderAmount() == null) {
            coupon.setMinOrderAmount(BigDecimal.ZERO);
        }

        if (coupon.getMinOrderAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn tối thiểu không được âm.");
        }

        if (coupon.getMinRankCode() == null || coupon.getMinRankCode().isBlank()) {
            coupon.setMinRankCode(DEFAULT_RANK_CODE);
        }

        if (!VALID_RANK_CODES.contains(coupon.getMinRankCode())) {
            throw new IllegalArgumentException("Rank tối thiểu không hợp lệ.");
        }

        if (coupon.getStartDate() != null
                && coupon.getEndDate() != null
                && coupon.getEndDate().isBefore(coupon.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
        }
    }

    private boolean isDuplicateCodeForOtherCoupon(Coupon coupon) {
        return couponDAO.findAll()
                .stream()
                .anyMatch(existing ->
                        existing.getId() != coupon.getId()
                                && existing.getCode() != null
                                && existing.getCode().equalsIgnoreCase(coupon.getCode())
                );
    }

    /* ===================== HELPERS ===================== */

    private int safeInt(String value, int defaultValue) {
        try {
            if (value == null) {
                return defaultValue;
            }

            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                return defaultValue;
            }

            return Integer.parseInt(trimmed);

        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private LocalDate safeDate(String value) {
        try {
            if (value == null) {
                return null;
            }

            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                return null;
            }

            return LocalDate.parse(trimmed);

        } catch (Exception e) {
            throw new IllegalArgumentException("Ngày không hợp lệ, định dạng đúng là yyyy-MM-dd: " + value);
        }
    }

    private BigDecimal parseBigDecimal(
            String value,
            BigDecimal defaultValue,
            String errorMessage
    ) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }

            return new BigDecimal(value.trim());

        } catch (Exception e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
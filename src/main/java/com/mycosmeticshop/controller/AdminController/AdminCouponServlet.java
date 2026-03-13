package com.mycosmeticshop.controller.AdminController;

import com.mycosmeticshop.dao.CouponDAO;
import com.mycosmeticshop.model.Coupon;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/admin/coupons")
public class AdminCouponServlet extends HttpServlet {

    // DAO dùng để thao tác dữ liệu coupon trong database
    private final CouponDAO couponDAO = new CouponDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Lấy action từ URL
        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {

            // =========================
            // HIỂN THỊ FORM TẠO COUPON
            // =========================
            case "new": {
                req.setAttribute("mode", "create");
                req.getRequestDispatcher("/jsp/admin/coupon/coupon_form.jsp").forward(req, resp);
                return;
            }

            // =========================
            // HIỂN THỊ FORM SỬA COUPON
            // =========================
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

            // =========================
            // HIỂN THỊ DANH SÁCH COUPON
            // Hỗ trợ filter theo mã và trạng thái
            // =========================
            case "list":
            default: {
                String q = safe(req.getParameter("q"));
                String status = safe(req.getParameter("status")); // active / inactive / empty

                List<Coupon> coupons = couponDAO.findAll();

                // Lọc theo mã coupon
                if (!q.isEmpty()) {
                    String qUpper = q.toUpperCase();
                    coupons = coupons.stream()
                            .filter(c -> c.getCode() != null && c.getCode().toUpperCase().contains(qUpper))
                            .collect(Collectors.toList());
                }

                // Lọc theo trạng thái active / inactive
                if ("active".equalsIgnoreCase(status)) {
                    coupons = coupons.stream()
                            .filter(Coupon::isActive)
                            .collect(Collectors.toList());
                } else if ("inactive".equalsIgnoreCase(status)) {
                    coupons = coupons.stream()
                            .filter(c -> !c.isActive())
                            .collect(Collectors.toList());
                }

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

        // Lấy action từ form
        String action = req.getParameter("action");
        if (action == null) {
            action = "create";
        }

        try {
            switch (action) {

                // =========================
                // TẠO COUPON MỚI
                // =========================
                case "create": {
                    Coupon coupon = new Coupon();

                    bind(req, coupon);
                    validate(coupon, false);

                    couponDAO.create(coupon);

                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
                }

                // =========================
                // CẬP NHẬT COUPON
                // =========================
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

                // =========================
                // XÓA COUPON
                // =========================
                case "delete": {
                    int id = safeInt(req.getParameter("id"), -1);

                    if (id > 0) {
                        couponDAO.delete(id);
                    }

                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
                }

                // =========================
                // ĐỔI TRẠNG THÁI ACTIVE / INACTIVE
                // =========================
                case "toggle": {
                    int id = safeInt(req.getParameter("id"), -1);

                    if (id > 0) {
                        couponDAO.toggleActive(id);
                    }

                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
                }

                default:
                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
            }

        } catch (IllegalArgumentException ex) {

            // Nếu validate lỗi thì forward lại form và giữ dữ liệu đã nhập
            req.setAttribute("error", ex.getMessage());

            Coupon coupon = new Coupon();

            if ("update".equals(action)) {
                req.setAttribute("mode", "edit");
                coupon.setId(safeInt(req.getParameter("id"), 0));
            } else {
                req.setAttribute("mode", "create");
            }

            try {
                bind(req, coupon);
            } catch (Exception ignore) {
            }

            req.setAttribute("coupon", coupon);

            req.getRequestDispatcher("/jsp/admin/coupon/coupon_form.jsp").forward(req, resp);
        }
    }

    /* ======================================================
       BIND + VALIDATE
       Dùng để gán dữ liệu từ request vào object Coupon
       và kiểm tra dữ liệu hợp lệ trước khi lưu DB
       ====================================================== */

    private void bind(HttpServletRequest req, Coupon coupon) {

        // CODE: chuyển sang chữ in hoa để đồng bộ dữ liệu
        String code = safe(req.getParameter("code")).toUpperCase();
        coupon.setCode(code);

        // Phần trăm giảm giá
        coupon.setDiscountPercent(safeInt(req.getParameter("discountPercent"), 0));

        // Số lượt sử dụng tối đa
        int maxUses = safeInt(req.getParameter("maxUses"), 0);
        coupon.setMaxUses(maxUses);

        // Số tiền giảm tối đa (có thể null)
        String maxStr = req.getParameter("maxDiscountAmount");
        if (maxStr == null || maxStr.trim().isEmpty()) {
            coupon.setMaxDiscountAmount(null);
        } else {
            try {
                coupon.setMaxDiscountAmount(new BigDecimal(maxStr.trim()));
            } catch (Exception e) {
                throw new IllegalArgumentException("maxDiscountAmount không hợp lệ.");
            }
        }

        // Ngày bắt đầu / ngày kết thúc
        coupon.setStartDate(safeDate(req.getParameter("startDate")));
        coupon.setEndDate(safeDate(req.getParameter("endDate")));

        // Trạng thái active
        boolean active = "1".equals(req.getParameter("active"))
                || "true".equalsIgnoreCase(req.getParameter("active"));
        coupon.setActive(active);
    }

    private void validate(Coupon coupon, boolean isUpdate) {

        // Kiểm tra mã coupon
        if (coupon.getCode() == null || coupon.getCode().isBlank()) {
            throw new IllegalArgumentException("Mã coupon không được để trống.");
        }

        // Kiểm tra phần trăm giảm giá
        if (coupon.getDiscountPercent() <= 0 || coupon.getDiscountPercent() > 100) {
            throw new IllegalArgumentException("discountPercent phải trong khoảng từ 1 đến 100.");
        }

        // Kiểm tra số lượt dùng tối đa
        if (coupon.getMaxUses() < 1) {
            throw new IllegalArgumentException("maxUses phải lớn hơn hoặc bằng 1.");
        }

        // Kiểm tra maxDiscountAmount nếu có nhập
        if (coupon.getMaxDiscountAmount() != null
                && coupon.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("maxDiscountAmount nếu nhập thì phải lớn hơn 0.");
        }

        // Kiểm tra ngày bắt đầu / ngày kết thúc
        if (coupon.getStartDate() != null
                && coupon.getEndDate() != null
                && coupon.getEndDate().isBefore(coupon.getStartDate())) {
            throw new IllegalArgumentException("endDate không được trước startDate.");
        }

        // Nếu là update thì không cho maxUses nhỏ hơn usedCount hiện tại
        if (isUpdate
                && coupon.getUsedCount() > 0
                && coupon.getMaxUses() > 0
                && coupon.getUsedCount() > coupon.getMaxUses()) {
            throw new IllegalArgumentException("maxUses không được nhỏ hơn usedCount hiện tại.");
        }
    }

    /* ======================================================
       HELPERS
       Các hàm xử lý dữ liệu an toàn
       ====================================================== */

    // Parse int an toàn
    private int safeInt(String s, int def) {
        try {
            if (s == null) {
                return def;
            }
            String t = s.trim();
            if (t.isEmpty()) {
                return def;
            }
            return Integer.parseInt(t);
        } catch (Exception e) {
            return def;
        }
    }

    // Trim chuỗi an toàn
    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    // Parse LocalDate an toàn, định dạng yyyy-MM-dd
    private LocalDate safeDate(String s) {
        try {
            if (s == null) {
                return null;
            }
            String t = s.trim();
            if (t.isEmpty()) {
                return null;
            }
            return LocalDate.parse(t);
        } catch (Exception e) {
            throw new IllegalArgumentException("Ngày không hợp lệ (định dạng yyyy-MM-dd): " + s);
        }
    }
}
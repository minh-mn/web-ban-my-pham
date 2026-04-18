package com.webshop.app.controller.AdminController;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.webshop.app.dao.CouponDAO;
import com.webshop.app.model.Coupon;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/coupons")
public class AdminCouponServlet extends HttpServlet {

    private final CouponDAO couponDAO = new CouponDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null) action = "list";

        switch (action) {

            case "new": {
                req.setAttribute("mode", "create");
                req.getRequestDispatcher("/jsp/admin/coupon/coupon_form.jsp").forward(req, resp);
                return;
            }

            case "edit": {
                int id = safeInt(req.getParameter("id"), -1);
                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
                }

                Coupon cp = couponDAO.findById(id);
                if (cp == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
                }

                req.setAttribute("mode", "edit");
                req.setAttribute("coupon", cp);
                req.getRequestDispatcher("/jsp/admin/coupon/coupon_form.jsp").forward(req, resp);
                return;
            }

            case "list":
            default: {
                // Hỗ trợ filter theo UI: q + status (optional)
                String q = safe(req.getParameter("q"));
                String status = safe(req.getParameter("status")); // active/inactive/empty

                List<Coupon> coupons = couponDAO.findAll();

                if (!q.isEmpty()) {
                    String qUp = q.toUpperCase();
                    coupons = coupons.stream()
                            .filter(c -> c.getCode() != null && c.getCode().toUpperCase().contains(qUp))
                            .collect(Collectors.toList());
                }

                if ("active".equalsIgnoreCase(status)) {
                    coupons = coupons.stream().filter(Coupon::isActive).collect(Collectors.toList());
                } else if ("inactive".equalsIgnoreCase(status)) {
                    coupons = coupons.stream().filter(c -> !c.isActive()).collect(Collectors.toList());
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

        String action = req.getParameter("action");
        if (action == null) action = "create";

        try {
            switch (action) {

                case "create": {
                    Coupon cp = new Coupon();
                    bind(req, cp);
                    validate(cp, false);
                    couponDAO.create(cp);
                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
                }

                case "update": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id <= 0) {
                        resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                        return;
                    }

                    Coupon cp = couponDAO.findById(id);
                    if (cp == null) {
                        resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                        return;
                    }

                    bind(req, cp);
                    cp.setId(id);
                    validate(cp, true);
                    couponDAO.update(cp);
                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
                }

                case "delete": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id > 0) couponDAO.delete(id);
                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
                }

                case "toggle": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id > 0) couponDAO.toggleActive(id);
                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
                }

                default:
                    resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                    return;
            }

        } catch (IllegalArgumentException ex) {
            // Forward lại form + giữ dữ liệu
            req.setAttribute("error", ex.getMessage());

            Coupon cp = new Coupon();
            if ("update".equals(action)) {
                req.setAttribute("mode", "edit");
                cp.setId(safeInt(req.getParameter("id"), 0));
            } else {
                req.setAttribute("mode", "create");
            }

            try { bind(req, cp); } catch (Exception ignore) {}
            req.setAttribute("coupon", cp);

            req.getRequestDispatcher("/jsp/admin/coupon/coupon_form.jsp").forward(req, resp);
        }
    }

    /* ===================== BIND + VALIDATE ===================== */

    private void bind(HttpServletRequest req, Coupon cp) {
        // CODE
        String code = safe(req.getParameter("code")).toUpperCase();
        cp.setCode(code);

        // discount %
        cp.setDiscountPercent(safeInt(req.getParameter("discountPercent"), 0));

        // max uses (DB NOT NULL)
        int maxUses = safeInt(req.getParameter("maxUses"), 0);
        cp.setMaxUses(maxUses);

        // max discount amount (nullable)
        String maxStr = req.getParameter("maxDiscountAmount");
        if (maxStr == null || maxStr.trim().isEmpty()) {
            cp.setMaxDiscountAmount(null);
        } else {
            try {
                cp.setMaxDiscountAmount(new BigDecimal(maxStr.trim()));
            } catch (Exception e) {
                throw new IllegalArgumentException("maxDiscountAmount không hợp lệ.");
            }
        }

        // dates (LocalDate)
        cp.setStartDate(safeDate(req.getParameter("startDate")));
        cp.setEndDate(safeDate(req.getParameter("endDate")));

        // active
        boolean active = "1".equals(req.getParameter("active")) ||
                         "true".equalsIgnoreCase(req.getParameter("active"));
        cp.setActive(active);
    }

    private void validate(Coupon cp, boolean isUpdate) {
        if (cp.getCode() == null || cp.getCode().isBlank())
            throw new IllegalArgumentException("Mã coupon (code) không được để trống.");

        if (cp.getDiscountPercent() <= 0 || cp.getDiscountPercent() > 100)
            throw new IllegalArgumentException("discountPercent phải trong khoảng 1..100.");

        if (cp.getMaxUses() < 1)
            throw new IllegalArgumentException("maxUses phải >= 1.");

        if (cp.getMaxDiscountAmount() != null &&
            cp.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("maxDiscountAmount nếu nhập phải > 0.");

        if (cp.getStartDate() != null && cp.getEndDate() != null &&
            cp.getEndDate().isBefore(cp.getStartDate()))
            throw new IllegalArgumentException("endDate không được trước startDate.");

        // Nếu update, không cho maxUses < usedCount (tránh dữ liệu vô lý)
        if (isUpdate && cp.getUsedCount() > 0 && cp.getMaxUses() > 0 && cp.getUsedCount() > cp.getMaxUses())
            throw new IllegalArgumentException("maxUses không được nhỏ hơn usedCount hiện tại.");
    }

    /* ===================== HELPERS ===================== */

    private int safeInt(String s, int def) {
        try {
            if (s == null) return def;
            String t = s.trim();
            if (t.isEmpty()) return def;
            return Integer.parseInt(t);
        } catch (Exception e) {
            return def;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private LocalDate safeDate(String s) {
        try {
            if (s == null) return null;
            String t = s.trim();
            if (t.isEmpty()) return null;
            return LocalDate.parse(t); // yyyy-MM-dd
        } catch (Exception e) {
            throw new IllegalArgumentException("Ngày không hợp lệ (định dạng yyyy-MM-dd): " + s);
        }
    }
}

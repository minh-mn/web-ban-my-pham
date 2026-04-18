package com.webshop.app.controller.AdminController;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.webshop.app.dao.BrandDAO;
import com.webshop.app.dao.BrandDiscountDAO;
import com.webshop.app.model.BrandDiscount;
import com.webshop.app.model.DiscountType;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/brand-discounts")
public class AdminBrandDiscountServlet extends HttpServlet {

    private final BrandDAO brandDAO = new BrandDAO();
    private final BrandDiscountDAO brandDiscountDAO = new BrandDiscountDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null) action = "list";

        switch (action) {

            case "new": {
                req.setAttribute("brands", brandDAO.findAllActive());
                req.setAttribute("mode", "create");
                req.getRequestDispatcher("/jsp/admin/brand_discount/brand_discount_form.jsp")
                        .forward(req, resp);
                break;
            }

            case "edit": {
                int id = safeInt(req.getParameter("id"), -1);
                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/brand-discounts");
                    return;
                }

                BrandDiscount discount = brandDiscountDAO.findById(id);
                if (discount == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/brand-discounts");
                    return;
                }

                req.setAttribute("discount", discount);
                req.setAttribute("brands", brandDAO.findAllActive());
                req.setAttribute("mode", "edit");

                req.getRequestDispatcher("/jsp/admin/brand_discount/brand_discount_form.jsp")
                        .forward(req, resp);
                break;
            }

            case "list":
            default: {
                req.setAttribute("brandDiscounts", brandDiscountDAO.findAll(true));
                req.getRequestDispatcher("/jsp/admin/brand_discount/brand_discount_list.jsp")
                        .forward(req, resp);
                break;
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
                    BrandDiscount d = new BrandDiscount();
                    bind(req, d);
                    validate(d);
                    brandDiscountDAO.create(d);
                    break;
                }

                case "update": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id <= 0) break;

                    BrandDiscount d = brandDiscountDAO.findById(id);
                    if (d != null) {
                        bind(req, d);
                        d.setId(id);
                        validate(d);
                        brandDiscountDAO.update(d);
                    }
                    break;
                }

                case "delete": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id > 0) brandDiscountDAO.delete(id);
                    break;
                }

                case "toggle": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id > 0) brandDiscountDAO.toggleActive(id);
                    break;
                }

                default:
                    break;
            }

            resp.sendRedirect(req.getContextPath() + "/admin/brand-discounts");

        } catch (IllegalArgumentException ex) {
            // lỗi validate / parse -> quay lại form
            req.setAttribute("error", ex.getMessage());
            req.setAttribute("brands", brandDAO.findAllActive());

            // giữ lại dữ liệu đã nhập
            BrandDiscount d = new BrandDiscount();
            try { bind(req, d); } catch (Exception ignore) {}
            req.setAttribute("discount", d);

            String mode = "update".equals(action) ? "edit" : "create";
            req.setAttribute("mode", mode);

            req.getRequestDispatcher("/jsp/admin/brand_discount/brand_discount_form.jsp")
                    .forward(req, resp);
        }
    }

    private void bind(HttpServletRequest req, BrandDiscount d) {

        int brandId = safeInt(req.getParameter("brandId"), -1);
        DiscountType discountType = safeEnum(DiscountType.class, req.getParameter("discountType"));

        BigDecimal discountValue = safeBigDecimal(req.getParameter("discountValue"), null);

        String maxStr = req.getParameter("maxDiscountAmount");
        BigDecimal maxDiscount = (maxStr == null || maxStr.trim().isEmpty())
                ? null
                : safeBigDecimal(maxStr, null);

        LocalDate startDate = safeDate(req.getParameter("startDate"));
        LocalDate endDate = safeDate(req.getParameter("endDate"));

        boolean active = "1".equals(req.getParameter("active")) || "true".equalsIgnoreCase(req.getParameter("active"));

        d.setBrandId(brandId);
        d.setDiscountType(discountType);
        d.setDiscountValue(discountValue);
        d.setMaxDiscountAmount(maxDiscount);
        d.setStartDate(startDate);
        d.setEndDate(endDate);
        d.setActive(active);
    }

    private void validate(BrandDiscount d) {
        if (d.getBrandId() <= 0) throw new IllegalArgumentException("Vui lòng chọn thương hiệu (brand).");
        if (d.getDiscountType() == null) throw new IllegalArgumentException("Vui lòng chọn loại giảm giá (discountType).");
        if (d.getDiscountValue() == null) throw new IllegalArgumentException("Vui lòng nhập discountValue.");
        if (d.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("discountValue phải > 0.");

        if (d.getStartDate() == null || d.getEndDate() == null)
            throw new IllegalArgumentException("Vui lòng chọn startDate và endDate.");

        if (d.getEndDate().isBefore(d.getStartDate()))
            throw new IllegalArgumentException("endDate không được trước startDate.");

        if (d.getMaxDiscountAmount() != null && d.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("maxDiscountAmount nếu nhập phải > 0.");
    }

    /* ===================== SAFE PARSERS ===================== */

    private int safeInt(String s, int def) {
        try {
            if (s == null) return def;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private BigDecimal safeBigDecimal(String s, BigDecimal def) {
        try {
            if (s == null) return def;
            String t = s.trim();
            if (t.isEmpty()) return def;
            return new BigDecimal(t);
        } catch (Exception e) {
            throw new IllegalArgumentException("Giá trị số không hợp lệ: " + s);
        }
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

    private <E extends Enum<E>> E safeEnum(Class<E> enumType, String s) {
        try {
            if (s == null || s.trim().isEmpty()) return null;
            return Enum.valueOf(enumType, s.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Giá trị enum không hợp lệ: " + s);
        }
    }
}

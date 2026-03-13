package com.mycosmeticshop.controller.AdminController;

import com.mycosmeticshop.dao.BrandDAO;
import com.mycosmeticshop.dao.BrandDiscountDAO;
import com.mycosmeticshop.model.BrandDiscount;
import com.mycosmeticshop.model.DiscountType;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

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
        if (action == null) {
            action = "list";
        }

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
        if (action == null) {
            action = "create";
        }

        try {
            switch (action) {

                case "create": {
                    BrandDiscount discount = new BrandDiscount();
                    bind(req, discount);
                    validate(discount);
                    brandDiscountDAO.create(discount);
                    break;
                }

                case "update": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id <= 0) {
                        break;
                    }

                    BrandDiscount discount = brandDiscountDAO.findById(id);
                    if (discount != null) {
                        bind(req, discount);
                        discount.setId(id);
                        validate(discount);
                        brandDiscountDAO.update(discount);
                    }
                    break;
                }

                case "delete": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id > 0) {
                        brandDiscountDAO.delete(id);
                    }
                    break;
                }

                case "toggle": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id > 0) {
                        brandDiscountDAO.toggleActive(id);
                    }
                    break;
                }

                default:
                    break;
            }

            resp.sendRedirect(req.getContextPath() + "/admin/brand-discounts");

        } catch (IllegalArgumentException ex) {
            // Nếu lỗi validate hoặc parse dữ liệu thì quay lại form
            req.setAttribute("error", ex.getMessage());
            req.setAttribute("brands", brandDAO.findAllActive());

            // Giữ lại dữ liệu đã nhập để người dùng không phải nhập lại
            BrandDiscount discount = new BrandDiscount();
            try {
                bind(req, discount);
            } catch (Exception ignore) {
            }
            req.setAttribute("discount", discount);

            String mode = "update".equals(action) ? "edit" : "create";
            req.setAttribute("mode", mode);

            req.getRequestDispatcher("/jsp/admin/brand_discount/brand_discount_form.jsp")
                    .forward(req, resp);
        }
    }

    private void bind(HttpServletRequest req, BrandDiscount discount) {

        int brandId = safeInt(req.getParameter("brandId"), -1);
        DiscountType discountType = safeEnum(DiscountType.class, req.getParameter("discountType"));

        BigDecimal discountValue = safeBigDecimal(req.getParameter("discountValue"), null);

        String maxStr = req.getParameter("maxDiscountAmount");
        BigDecimal maxDiscount = (maxStr == null || maxStr.trim().isEmpty())
                ? null
                : safeBigDecimal(maxStr, null);

        LocalDate startDate = safeDate(req.getParameter("startDate"));
        LocalDate endDate = safeDate(req.getParameter("endDate"));

        boolean active = "1".equals(req.getParameter("active"))
                || "true".equalsIgnoreCase(req.getParameter("active"));

        discount.setBrandId(brandId);
        discount.setDiscountType(discountType);
        discount.setDiscountValue(discountValue);
        discount.setMaxDiscountAmount(maxDiscount);
        discount.setStartDate(startDate);
        discount.setEndDate(endDate);
        discount.setActive(active);
    }

    private void validate(BrandDiscount discount) {
        if (discount.getBrandId() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn thương hiệu.");
        }

        if (discount.getDiscountType() == null) {
            throw new IllegalArgumentException("Vui lòng chọn loại giảm giá.");
        }

        if (discount.getDiscountValue() == null) {
            throw new IllegalArgumentException("Vui lòng nhập giá trị giảm giá.");
        }

        if (discount.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị giảm giá phải lớn hơn 0.");
        }

        if (discount.getStartDate() == null || discount.getEndDate() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày bắt đầu và ngày kết thúc.");
        }

        if (discount.getEndDate().isBefore(discount.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu.");
        }

        if (discount.getMaxDiscountAmount() != null
                && discount.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giảm tối đa nếu nhập thì phải lớn hơn 0.");
        }
    }

    /* ===================== SAFE PARSERS ===================== */

    private int safeInt(String s, int def) {
        try {
            if (s == null) {
                return def;
            }
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private BigDecimal safeBigDecimal(String s, BigDecimal def) {
        try {
            if (s == null) {
                return def;
            }
            String t = s.trim();
            if (t.isEmpty()) {
                return def;
            }
            return new BigDecimal(t);
        } catch (Exception e) {
            throw new IllegalArgumentException("Giá trị số không hợp lệ: " + s);
        }
    }

    private LocalDate safeDate(String s) {
        try {
            if (s == null) {
                return null;
            }
            String t = s.trim();
            if (t.isEmpty()) {
                return null;
            }
            return LocalDate.parse(t); // yyyy-MM-dd
        } catch (Exception e) {
            throw new IllegalArgumentException("Ngày không hợp lệ (định dạng yyyy-MM-dd): " + s);
        }
    }

    private <E extends Enum<E>> E safeEnum(Class<E> enumType, String s) {
        try {
            if (s == null || s.trim().isEmpty()) {
                return null;
            }
            return Enum.valueOf(enumType, s.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Giá trị không hợp lệ: " + s);
        }
    }
}
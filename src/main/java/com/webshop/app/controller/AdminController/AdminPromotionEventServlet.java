package com.webshop.app.controller.AdminController;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.webshop.app.dao.BrandDAO;
import com.webshop.app.dao.CategoryDAO;
import com.webshop.app.dao.PromotionEventDAO;
import com.webshop.app.model.DiscountType;
import com.webshop.app.model.PromotionEvent;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/promotion-events")
public class AdminPromotionEventServlet extends HttpServlet {

    private final PromotionEventDAO eventDAO = new PromotionEventDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BrandDAO brandDAO = new BrandDAO();

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
                loadRefs(req);
                req.getRequestDispatcher("/jsp/admin/promotion_event/promotion_event_form.jsp")
                        .forward(req, resp);
                break;
            }

            case "edit": {
                int id = safeInt(req.getParameter("id"), -1);
                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/promotion-events");
                    return;
                }

                PromotionEvent ev = eventDAO.findById(id);
                if (ev == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/promotion-events");
                    return;
                }

                req.setAttribute("mode", "edit");
                req.setAttribute("event", ev);
                loadRefs(req);

                req.getRequestDispatcher("/jsp/admin/promotion_event/promotion_event_form.jsp")
                        .forward(req, resp);
                break;
            }

            case "list":
            default: {
                // ✅ joinRef = true để JSP hiển thị brandName/categoryName
                req.setAttribute("events", eventDAO.findAll(true));
                req.getRequestDispatcher("/jsp/admin/promotion_event/promotion_event_list.jsp")
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
                    PromotionEvent ev = new PromotionEvent();
                    bind(req, ev);
                    validate(ev);
                    eventDAO.create(ev);
                    break;
                }

                case "update": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id <= 0) break;

                    PromotionEvent ev = eventDAO.findById(id);
                    if (ev == null) break;

                    bind(req, ev);
                    ev.setId(id);
                    validate(ev);
                    eventDAO.update(ev);
                    break;
                }

                case "delete": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id > 0) eventDAO.delete(id);
                    break;
                }

                case "toggle": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id > 0) eventDAO.toggleActive(id);
                    break;
                }

                default:
                    break;
            }

            resp.sendRedirect(req.getContextPath() + "/admin/promotion-events");

        } catch (IllegalArgumentException ex) {

            req.setAttribute("error", ex.getMessage());
            loadRefs(req);

            PromotionEvent ev = new PromotionEvent();
            if ("update".equals(action)) {
                req.setAttribute("mode", "edit");
                ev.setId(safeInt(req.getParameter("id"), 0));
            } else {
                req.setAttribute("mode", "create");
            }

            try { bind(req, ev); } catch (Exception ignore) {}
            req.setAttribute("event", ev);

            req.getRequestDispatcher("/jsp/admin/promotion_event/promotion_event_form.jsp")
                    .forward(req, resp);
        }
    }

    /* ===================== LOAD REFS ===================== */

    private void loadRefs(HttpServletRequest req) {
        // categories là tree parent->children (CategoryDAO.findParents())
        req.setAttribute("categories", categoryDAO.findParents());
        // brands list
        req.setAttribute("brands", brandDAO.findAll());
    }

    /* ===================== BIND / VALIDATE ===================== */

    private void bind(HttpServletRequest req, PromotionEvent ev) {

        ev.setName(safe(req.getParameter("name")));

        PromotionEvent.Scope scope = safeEnum(PromotionEvent.Scope.class, req.getParameter("scope"));
        ev.setScope(scope);

        DiscountType discountType = safeEnum(DiscountType.class, req.getParameter("discountType"));
        ev.setDiscountType(discountType);

        ev.setDiscountValue(safeBigDecimal(req.getParameter("discountValue"), null));

        String maxStr = req.getParameter("maxDiscountAmount");
        ev.setMaxDiscountAmount((maxStr == null || maxStr.trim().isEmpty()) ? null : safeBigDecimal(maxStr, null));

        // categoryId / brandId
        Integer categoryId = null;
        Integer brandId = null;

        String catStr = req.getParameter("categoryId");
        if (catStr != null && !catStr.trim().isEmpty()) {
            int cid = safeInt(catStr, -1);
            categoryId = (cid > 0) ? cid : null;
        }

        String brandStr = req.getParameter("brandId");
        if (brandStr != null && !brandStr.trim().isEmpty()) {
            int bid = safeInt(brandStr, -1);
            brandId = (bid > 0) ? bid : null;
        }

        ev.setCategoryId(categoryId);
        ev.setBrandId(brandId);

        ev.setStartDate(safeDate(req.getParameter("startDate")));
        ev.setEndDate(safeDate(req.getParameter("endDate")));

        boolean active = "1".equals(req.getParameter("active")) || "true".equalsIgnoreCase(req.getParameter("active"));
        ev.setActive(active);
    }

    private void validate(PromotionEvent ev) {

        if (ev.getName() == null || ev.getName().isBlank())
            throw new IllegalArgumentException("Tên chương trình không được để trống.");

        if (ev.getScope() == null)
            throw new IllegalArgumentException("Vui lòng chọn scope.");

        if (ev.getDiscountType() == null)
            throw new IllegalArgumentException("Vui lòng chọn discountType.");

        if (ev.getDiscountValue() == null || ev.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("discountValue phải > 0.");

        if (ev.getDiscountType() == DiscountType.PERCENT &&
            ev.getDiscountValue().compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException("Giảm theo % không được > 100.");

        if (ev.getStartDate() == null || ev.getEndDate() == null)
            throw new IllegalArgumentException("Vui lòng nhập startDate và endDate.");

        if (ev.getEndDate().isBefore(ev.getStartDate()))
            throw new IllegalArgumentException("endDate không được trước startDate.");

        // scope rules
        if (ev.getScope() == PromotionEvent.Scope.CATEGORY && ev.getCategoryId() == null)
            throw new IllegalArgumentException("Scope CATEGORY cần chọn category.");

        if (ev.getScope() == PromotionEvent.Scope.BRAND && ev.getBrandId() == null)
            throw new IllegalArgumentException("Scope BRAND cần chọn brand.");

        if (ev.getScope() == PromotionEvent.Scope.ALL) {
            ev.setCategoryId(null);
            ev.setBrandId(null);
        }
    }

    /* ===================== SAFE HELPERS ===================== */

    private int safeInt(String s, int def) {
        try {
            if (s == null) return def;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
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
            return LocalDate.parse(t);
        } catch (Exception e) {
            throw new IllegalArgumentException("Ngày không hợp lệ (yyyy-MM-dd): " + s);
        }
    }

    private <E extends Enum<E>> E safeEnum(Class<E> enumType, String s) {
        try {
            if (s == null || s.trim().isEmpty()) return null;
            return Enum.valueOf(enumType, s.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Giá trị không hợp lệ: " + s);
        }
    }
}

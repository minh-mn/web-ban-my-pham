package com.mycosmeticshop.controller.AdminController;

import com.mycosmeticshop.dao.BrandDAO;
import com.mycosmeticshop.dao.CategoryDAO;
import com.mycosmeticshop.dao.PromotionEventDAO;
import com.mycosmeticshop.model.DiscountType;
import com.mycosmeticshop.model.PromotionEvent;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

@WebServlet("/admin/promotion-events")
public class AdminPromotionEventServlet extends HttpServlet {

    // DAO thao tác với bảng promotion event
    private final PromotionEventDAO eventDAO = new PromotionEventDAO();

    // DAO phục vụ dropdown category / brand
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BrandDAO brandDAO = new BrandDAO();

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
            // HIỂN THỊ FORM TẠO MỚI CHƯƠNG TRÌNH KHUYẾN MÃI
            // =========================
            case "new": {
                req.setAttribute("mode", "create");
                loadRefs(req);

                req.getRequestDispatcher("/jsp/admin/promotion_event/promotion_event_form.jsp")
                        .forward(req, resp);
                break;
            }

            // =========================
            // HIỂN THỊ FORM SỬA CHƯƠNG TRÌNH KHUYẾN MÃI
            // =========================
            case "edit": {
                int id = safeInt(req.getParameter("id"), -1);

                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/promotion-events");
                    return;
                }

                PromotionEvent event = eventDAO.findById(id);
                if (event == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/promotion-events");
                    return;
                }

                req.setAttribute("mode", "edit");
                req.setAttribute("event", event);

                loadRefs(req);

                req.getRequestDispatcher("/jsp/admin/promotion_event/promotion_event_form.jsp")
                        .forward(req, resp);
                break;
            }

            // =========================
            // HIỂN THỊ DANH SÁCH CHƯƠNG TRÌNH KHUYẾN MÃI
            // =========================
            case "list":
            default: {
                // joinRef = true để JSP hiển thị brandName / categoryName
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

        // Lấy action từ form
        String action = req.getParameter("action");
        if (action == null) {
            action = "create";
        }

        try {
            switch (action) {

                // =========================
                // TẠO MỚI CHƯƠNG TRÌNH KHUYẾN MÃI
                // =========================
                case "create": {
                    PromotionEvent event = new PromotionEvent();

                    bind(req, event);
                    validate(event);

                    eventDAO.create(event);
                    break;
                }

                // =========================
                // CẬP NHẬT CHƯƠNG TRÌNH KHUYẾN MÃI
                // =========================
                case "update": {
                    int id = safeInt(req.getParameter("id"), -1);

                    if (id <= 0) {
                        break;
                    }

                    PromotionEvent event = eventDAO.findById(id);
                    if (event == null) {
                        break;
                    }

                    bind(req, event);
                    event.setId(id);

                    validate(event);

                    eventDAO.update(event);
                    break;
                }

                // =========================
                // XÓA CHƯƠNG TRÌNH KHUYẾN MÃI
                // =========================
                case "delete": {
                    int id = safeInt(req.getParameter("id"), -1);

                    if (id > 0) {
                        eventDAO.delete(id);
                    }
                    break;
                }

                // =========================
                // ĐỔI TRẠNG THÁI ACTIVE / INACTIVE
                // =========================
                case "toggle": {
                    int id = safeInt(req.getParameter("id"), -1);

                    if (id > 0) {
                        eventDAO.toggleActive(id);
                    }
                    break;
                }

                default:
                    break;
            }

            resp.sendRedirect(req.getContextPath() + "/admin/promotion-events");

        } catch (IllegalArgumentException ex) {

            // Nếu validate lỗi thì quay lại form và giữ dữ liệu đã nhập
            req.setAttribute("error", ex.getMessage());

            loadRefs(req);

            PromotionEvent event = new PromotionEvent();

            if ("update".equals(action)) {
                req.setAttribute("mode", "edit");
                event.setId(safeInt(req.getParameter("id"), 0));
            } else {
                req.setAttribute("mode", "create");
            }

            try {
                bind(req, event);
            } catch (Exception ignore) {
            }

            req.setAttribute("event", event);

            req.getRequestDispatcher("/jsp/admin/promotion_event/promotion_event_form.jsp")
                    .forward(req, resp);
        }
    }

    /* ======================================================
       LOAD REFERENCES
       Dùng để load dữ liệu cho dropdown category / brand
       ====================================================== */
    private void loadRefs(HttpServletRequest req) {
        // categories là tree parent -> children
        req.setAttribute("categories", categoryDAO.findParents());

        // danh sách brand
        req.setAttribute("brands", brandDAO.findAll());
    }

    /* ======================================================
       BIND / VALIDATE
       ====================================================== */

    private void bind(HttpServletRequest req, PromotionEvent event) {

        // Tên chương trình khuyến mãi
        event.setName(safe(req.getParameter("name")));

        // Phạm vi áp dụng: ALL / CATEGORY / BRAND
        PromotionEvent.Scope scope = safeEnum(PromotionEvent.Scope.class, req.getParameter("scope"));
        event.setScope(scope);

        // Loại giảm giá: PERCENT / FIXED ...
        DiscountType discountType = safeEnum(DiscountType.class, req.getParameter("discountType"));
        event.setDiscountType(discountType);

        // Giá trị giảm giá
        event.setDiscountValue(safeBigDecimal(req.getParameter("discountValue"), null));

        // Mức giảm tối đa (có thể null)
        String maxStr = req.getParameter("maxDiscountAmount");
        event.setMaxDiscountAmount(
                (maxStr == null || maxStr.trim().isEmpty())
                        ? null
                        : safeBigDecimal(maxStr, null)
        );

        // categoryId / brandId
        Integer categoryId = null;
        Integer brandId = null;

        String categoryStr = req.getParameter("categoryId");
        if (categoryStr != null && !categoryStr.trim().isEmpty()) {
            int cid = safeInt(categoryStr, -1);
            categoryId = (cid > 0) ? cid : null;
        }

        String brandStr = req.getParameter("brandId");
        if (brandStr != null && !brandStr.trim().isEmpty()) {
            int bid = safeInt(brandStr, -1);
            brandId = (bid > 0) ? bid : null;
        }

        event.setCategoryId(categoryId);
        event.setBrandId(brandId);

        // Ngày bắt đầu / ngày kết thúc
        event.setStartDate(safeDate(req.getParameter("startDate")));
        event.setEndDate(safeDate(req.getParameter("endDate")));

        // Trạng thái active
        boolean active = "1".equals(req.getParameter("active"))
                || "true".equalsIgnoreCase(req.getParameter("active"));
        event.setActive(active);
    }

    private void validate(PromotionEvent event) {

        // Kiểm tra tên chương trình
        if (event.getName() == null || event.getName().isBlank()) {
            throw new IllegalArgumentException("Tên chương trình không được để trống.");
        }

        // Kiểm tra scope
        if (event.getScope() == null) {
            throw new IllegalArgumentException("Vui lòng chọn scope.");
        }

        // Kiểm tra loại giảm giá
        if (event.getDiscountType() == null) {
            throw new IllegalArgumentException("Vui lòng chọn discountType.");
        }

        // Kiểm tra giá trị giảm giá
        if (event.getDiscountValue() == null
                || event.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("discountValue phải lớn hơn 0.");
        }

        // Nếu là giảm theo % thì không được > 100
        if (event.getDiscountType() == DiscountType.PERCENT
                && event.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Giảm theo % không được lớn hơn 100.");
        }

        // Kiểm tra ngày bắt đầu / ngày kết thúc
        if (event.getStartDate() == null || event.getEndDate() == null) {
            throw new IllegalArgumentException("Vui lòng nhập startDate và endDate.");
        }

        if (event.getEndDate().isBefore(event.getStartDate())) {
            throw new IllegalArgumentException("endDate không được trước startDate.");
        }

        // Rule theo scope
        if (event.getScope() == PromotionEvent.Scope.CATEGORY && event.getCategoryId() == null) {
            throw new IllegalArgumentException("Scope CATEGORY cần chọn category.");
        }

        if (event.getScope() == PromotionEvent.Scope.BRAND && event.getBrandId() == null) {
            throw new IllegalArgumentException("Scope BRAND cần chọn brand.");
        }

        // Nếu áp dụng toàn bộ thì xóa categoryId / brandId
        if (event.getScope() == PromotionEvent.Scope.ALL) {
            event.setCategoryId(null);
            event.setBrandId(null);
        }
    }

    /* ======================================================
       SAFE HELPERS
       ====================================================== */

    // Parse int an toàn
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

    // Trim chuỗi an toàn
    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    // Parse BigDecimal an toàn
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

    // Parse LocalDate an toàn
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
            throw new IllegalArgumentException("Ngày không hợp lệ (yyyy-MM-dd): " + s);
        }
    }

    // Parse enum an toàn
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
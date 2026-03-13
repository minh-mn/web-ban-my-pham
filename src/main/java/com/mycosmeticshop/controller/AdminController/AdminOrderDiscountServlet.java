package com.mycosmeticshop.controller.AdminController;

import com.mycosmeticshop.dao.OrderDiscountDAO;
import com.mycosmeticshop.model.OrderDiscount;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

@WebServlet("/admin/order-discounts")
public class AdminOrderDiscountServlet extends HttpServlet {

    // DAO dùng để thao tác dữ liệu discount theo đơn hàng
    private final OrderDiscountDAO dao = new OrderDiscountDAO();

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
            // HIỂN THỊ FORM TẠO MỚI DISCOUNT
            // =========================
            case "new":
                req.setAttribute("mode", "create");
                req.getRequestDispatcher("/jsp/admin/order_discount/order_discount_form.jsp")
                        .forward(req, resp);
                break;

            // =========================
            // HIỂN THỊ FORM CHỈNH SỬA DISCOUNT
            // =========================
            case "edit": {
                int id = safeInt(req.getParameter("id"), -1);

                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/order-discounts");
                    return;
                }

                OrderDiscount discount = dao.findById(id);
                if (discount == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/order-discounts");
                    return;
                }

                req.setAttribute("mode", "edit");
                req.setAttribute("discount", discount);

                req.getRequestDispatcher("/jsp/admin/order_discount/order_discount_form.jsp")
                        .forward(req, resp);
                break;
            }

            // =========================
            // HIỂN THỊ DANH SÁCH DISCOUNT
            // =========================
            default:
                req.setAttribute("discounts", dao.findAll());
                req.getRequestDispatcher("/jsp/admin/order_discount/order_discount_list.jsp")
                        .forward(req, resp);
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
            if ("create".equals(action)) {
                OrderDiscount discount = buildFromRequest(req);
                validate(discount);
                dao.create(discount);

            } else if ("update".equals(action)) {
                int id = safeInt(req.getParameter("id"), -1);
                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/order-discounts");
                    return;
                }

                OrderDiscount discount = buildFromRequest(req);
                discount.setId(id);

                validate(discount);
                dao.update(discount);

            } else if ("delete".equals(action)) {
                int id = safeInt(req.getParameter("id"), -1);
                if (id > 0) {
                    dao.delete(id);
                }
            }

            resp.sendRedirect(req.getContextPath() + "/admin/order-discounts");

        } catch (IllegalArgumentException ex) {
            // Nếu validate lỗi thì quay lại form và giữ dữ liệu đã nhập
            req.setAttribute("error", ex.getMessage());

            OrderDiscount discount = new OrderDiscount();
            try {
                discount = buildFromRequest(req);
            } catch (Exception ignore) {
            }

            if ("update".equals(action)) {
                req.setAttribute("mode", "edit");
                discount.setId(safeInt(req.getParameter("id"), 0));
            } else {
                req.setAttribute("mode", "create");
            }

            req.setAttribute("discount", discount);

            req.getRequestDispatcher("/jsp/admin/order_discount/order_discount_form.jsp")
                    .forward(req, resp);
        }
    }

    /* ======================================================
       BUILD DATA TỪ REQUEST
       Gán dữ liệu từ form vào object OrderDiscount
       ====================================================== */
    private OrderDiscount buildFromRequest(HttpServletRequest req) {
        OrderDiscount discount = new OrderDiscount();

        // Giá trị đơn hàng tối thiểu để được áp dụng giảm giá
        discount.setMinOrderValue(parseBigDecimal(req.getParameter("minOrderValue"), "minOrderValue"));

        // Phần trăm giảm giá
        discount.setDiscountPercent(parseBigDecimal(req.getParameter("discountPercent"), "discountPercent"));

        // Số tiền giảm tối đa (có thể để trống)
        String max = req.getParameter("maxDiscountAmount");
        if (max == null || max.trim().isEmpty()) {
            discount.setMaxDiscountAmount(null);
        } else {
            discount.setMaxDiscountAmount(parseBigDecimal(max, "maxDiscountAmount"));
        }

        // Ngày bắt đầu / kết thúc
        discount.setStartDate(parseDate(req.getParameter("startDate"), "startDate"));
        discount.setEndDate(parseDate(req.getParameter("endDate"), "endDate"));

        // Trạng thái active
        discount.setActive("1".equals(req.getParameter("active"))
                || "on".equals(req.getParameter("active"))
                || "true".equalsIgnoreCase(req.getParameter("active")));

        return discount;
    }

    /* ======================================================
       VALIDATE
       Kiểm tra dữ liệu trước khi lưu
       ====================================================== */
    private void validate(OrderDiscount discount) {

        if (discount.getMinOrderValue() == null
                || discount.getMinOrderValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("minOrderValue phải lớn hơn 0.");
        }

        if (discount.getDiscountPercent() == null
                || discount.getDiscountPercent().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("discountPercent phải lớn hơn 0.");
        }

        if (discount.getDiscountPercent().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("discountPercent không được lớn hơn 100.");
        }

        if (discount.getMaxDiscountAmount() != null
                && discount.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("maxDiscountAmount nếu nhập thì phải lớn hơn 0.");
        }

        if (discount.getStartDate() == null || discount.getEndDate() == null) {
            throw new IllegalArgumentException("Vui lòng chọn đầy đủ startDate và endDate.");
        }

        if (discount.getEndDate().isBefore(discount.getStartDate())) {
            throw new IllegalArgumentException("endDate không được trước startDate.");
        }
    }

    /* ======================================================
       HELPER METHODS
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

    // Parse BigDecimal an toàn
    private BigDecimal parseBigDecimal(String s, String fieldName) {
        try {
            if (s == null || s.trim().isEmpty()) {
                throw new IllegalArgumentException(fieldName + " không được để trống.");
            }
            return new BigDecimal(s.trim());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ.");
        }
    }

    // Parse LocalDate an toàn
    private LocalDate parseDate(String s, String fieldName) {
        try {
            if (s == null || s.trim().isEmpty()) {
                throw new IllegalArgumentException(fieldName + " không được để trống.");
            }
            return LocalDate.parse(s.trim()); // yyyy-MM-dd
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ (định dạng yyyy-MM-dd).");
        }
    }
}
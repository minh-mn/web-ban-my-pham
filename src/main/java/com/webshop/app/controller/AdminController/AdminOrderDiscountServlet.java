package com.webshop.app.controller.AdminController;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.webshop.app.dao.OrderDiscountDAO;
import com.webshop.app.model.OrderDiscount;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/order-discounts")
public class AdminOrderDiscountServlet extends HttpServlet {

    private final OrderDiscountDAO dao = new OrderDiscountDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "new":
                req.getRequestDispatcher("/jsp/admin/order_discount/order_discount_form.jsp")
                   .forward(req, resp);
                break;

            case "edit": {
                int id = Integer.parseInt(req.getParameter("id"));
                req.setAttribute("discount", dao.findById(id));
                req.getRequestDispatcher("/jsp/admin/order_discount/order_discount_form.jsp")
                   .forward(req, resp);
                break;
            }

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

        String action = req.getParameter("action");
        if (action == null) action = "create";

        if ("create".equals(action)) {
            dao.create(buildFromRequest(req));
        } else if ("update".equals(action)) {
            OrderDiscount d = buildFromRequest(req);
            d.setId(Integer.parseInt(req.getParameter("id")));
            dao.update(d);
        } else if ("delete".equals(action)) {
            dao.delete(Integer.parseInt(req.getParameter("id")));
        }

        resp.sendRedirect(req.getContextPath() + "/admin/order-discounts");
    }

    private OrderDiscount buildFromRequest(HttpServletRequest req) {
        OrderDiscount d = new OrderDiscount();

        d.setMinOrderValue(new BigDecimal(req.getParameter("minOrderValue")));
        d.setDiscountPercent(new BigDecimal(req.getParameter("discountPercent")));

        String max = req.getParameter("maxDiscountAmount");
        if (max == null || max.trim().isEmpty()) d.setMaxDiscountAmount(null);
        else d.setMaxDiscountAmount(new BigDecimal(max));

        d.setStartDate(LocalDate.parse(req.getParameter("startDate")));
        d.setEndDate(LocalDate.parse(req.getParameter("endDate")));

        d.setActive("1".equals(req.getParameter("active")) || "on".equals(req.getParameter("active")));
        return d;
    }
}

package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.InventoryDAO;
import com.webshop.app.model.User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/inventory")
public class AdminInventoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP_INVENTORY = "/jsp/admin/inventory/inventory.jsp";

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String keyword = req.getParameter("keyword");
        String status = req.getParameter("status");

        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);

        req.setAttribute("summary", inventoryDAO.getSummary());
        req.setAttribute("products", inventoryDAO.findInventoryProducts(keyword, status));
        req.setAttribute("lowStockAlerts", inventoryDAO.lowStockAlerts(8));
        req.setAttribute("recentActivities", inventoryDAO.recentStockActivities(12));

        req.setAttribute("last7DaysExportLabelsJson",
                toJsonStringArray(inventoryDAO.last7DaysExportLabels()));
        req.setAttribute("last7DaysExportValuesJson",
                toJsonNumberArray(inventoryDAO.last7DaysExportValues()));

        req.getRequestDispatcher(JSP_INVENTORY).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");

        if (!"addStock".equalsIgnoreCase(action)) {
            resp.sendRedirect(req.getContextPath() + "/admin/inventory");
            return;
        }

        int productId = parseInt(req.getParameter("productId"), 0);
        int quantity = parseInt(req.getParameter("quantity"), 0);
        String note = req.getParameter("note");
        Integer adminUserId = getCurrentAdminUserId(req);

        try {
            inventoryDAO.addStock(productId, quantity, note, adminUserId);

            resp.sendRedirect(req.getContextPath()
                    + "/admin/inventory?success=stock_added");

        } catch (IllegalArgumentException ex) {
            resp.sendRedirect(req.getContextPath()
                    + "/admin/inventory?error="
                    + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8));

        } catch (RuntimeException ex) {
            throw new ServletException("AdminInventoryServlet.addStock error", ex);
        }
    }

    private String toJsonStringArray(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder("[");

        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }

            builder.append('"')
                    .append(escapeJson(values.get(i)))
                    .append('"');
        }

        builder.append(']');
        return builder.toString();
    }

    private String toJsonNumberArray(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder("[");

        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }

            Integer value = values.get(i);
            builder.append(value == null ? 0 : Math.max(value, 0));
        }

        builder.append(']');
        return builder.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private Integer getCurrentAdminUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);

        if (session == null) {
            return null;
        }

        Object user = session.getAttribute("user");

        if (user instanceof User currentUser && currentUser.getId() > 0) {
            return currentUser.getId();
        }

        return null;
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
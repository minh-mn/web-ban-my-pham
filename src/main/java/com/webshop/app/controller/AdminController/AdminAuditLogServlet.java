package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.AuditLogDAO;
import com.webshop.app.dao.AuditLogDAO.AuditLogFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/audit-logs")
public class AdminAuditLogServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String JSP_LIST = "/jsp/admin/audit/audit-log-list.jsp";

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        AuditLogFilter filter = buildFilter(req);
        int totalRows = auditLogDAO.count(filter);
        int totalPages = Math.max(1, (int) Math.ceil(totalRows * 1.0 / filter.getPageSize()));

        if (filter.getPage() > totalPages) {
            filter.setPage(totalPages);
        }

        req.setAttribute("auditLogs", auditLogDAO.find(filter));
        req.setAttribute("filter", filter);
        req.setAttribute("modules", auditLogDAO.findModules());
        req.setAttribute("actionTypes", auditLogDAO.findActionTypes());
        req.setAttribute("totalRows", totalRows);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("currentPage", filter.getPage());
        req.setAttribute("pageSize", filter.getPageSize());
        req.setAttribute("filterQueryString", buildFilterQueryString(filter));

        req.setAttribute("pageTitle", "ADMIN | Nhật ký hệ thống");
        req.setAttribute("activeMenu", "auditLogs");
        req.setAttribute("pageCss", "/assets/css/admin/admin-list.css");

        req.getRequestDispatcher(JSP_LIST).forward(req, resp);
    }

    private AuditLogFilter buildFilter(HttpServletRequest req) {
        AuditLogFilter filter = new AuditLogFilter();
        filter.setKeyword(trim(req.getParameter("keyword")));
        filter.setModule(emptyToNull(req.getParameter("module")));
        filter.setActionType(emptyToNull(req.getParameter("actionType")));
        filter.setActor(trim(req.getParameter("actor")));
        filter.setDateFrom(parseLocalDate(req.getParameter("dateFrom")));
        filter.setDateTo(parseLocalDate(req.getParameter("dateTo")));
        filter.setPage(parseInt(req.getParameter("page"), 1));
        filter.setPageSize(parsePageSize(req.getParameter("pageSize"), 20));
        return filter;
    }

    private int parsePageSize(String value, int fallback) {
        int parsed = parseInt(value, fallback);
        return switch (parsed) {
            case 10, 20, 50, 100 -> parsed;
            default -> fallback;
        };
    }

    private LocalDate parseLocalDate(String value) {
        String text = trim(value);
        if (text.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(text);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildFilterQueryString(AuditLogFilter filter) {
        StringBuilder query = new StringBuilder();
        appendQueryParam(query, "keyword", filter.getKeyword());
        appendQueryParam(query, "module", filter.getModule());
        appendQueryParam(query, "actionType", filter.getActionType());
        appendQueryParam(query, "actor", filter.getActor());
        appendQueryParam(query, "dateFrom", filter.getDateFrom() == null ? "" : filter.getDateFrom().toString());
        appendQueryParam(query, "dateTo", filter.getDateTo() == null ? "" : filter.getDateTo().toString());
        appendQueryParam(query, "pageSize", String.valueOf(filter.getPageSize()));
        return query.toString();
    }

    private void appendQueryParam(StringBuilder query, String name, String value) {
        String text = trim(value);
        if (text.isEmpty()) {
            return;
        }

        if (query.length() > 0) {
            query.append('&');
        }

        query.append(URLEncoder.encode(name, StandardCharsets.UTF_8));
        query.append('=');
        query.append(URLEncoder.encode(text, StandardCharsets.UTF_8));
    }

    private String emptyToNull(String value) {
        String text = trim(value);
        return text.isEmpty() ? null : text;
    }

    private int parseInt(String value, int fallback) {
        try {
            if (value == null || value.isBlank()) {
                return fallback;
            }
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}

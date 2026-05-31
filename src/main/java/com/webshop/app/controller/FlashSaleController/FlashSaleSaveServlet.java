package com.webshop.app.controller.FlashSaleController;

import com.webshop.app.dao.FlashSaleDAO;
import com.webshop.app.model.FlashSale;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/flash-sale/save")
public class FlashSaleSaveServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final FlashSaleDAO flashSaleDAO = new FlashSaleDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            String idStr = trim(req.getParameter("id"));
            String title = trim(req.getParameter("title"));
            String startTimeStr = trim(req.getParameter("startTime"));
            String endTimeStr = trim(req.getParameter("endTime"));
            String activeStr = trim(req.getParameter("active"));

            if (title.isBlank()) {
                throw new IllegalArgumentException("Tên Flash Sale không được để trống.");
            }

            LocalDateTime startTime = parseDateTimeLocal(startTimeStr);
            LocalDateTime endTime = parseDateTimeLocal(endTimeStr);

            if (!endTime.isAfter(startTime)) {
                throw new IllegalArgumentException("Thời gian kết thúc phải lớn hơn thời gian bắt đầu.");
            }

            FlashSale flashSale = new FlashSale();
            flashSale.setId(parseInt(idStr, 0));
            flashSale.setTitle(title);
            flashSale.setStartTime(Timestamp.valueOf(startTime));
            flashSale.setEndTime(Timestamp.valueOf(endTime));
            flashSale.setActive(isActiveValue(activeStr));

            if (flashSale.getId() <= 0) {
                flashSaleDAO.insert(flashSale);
            } else {
                flashSaleDAO.update(flashSale);
            }

            resp.sendRedirect(req.getContextPath() + "/admin/flash-sale?save=success");

        } catch (IllegalArgumentException e) {
            resp.sendRedirect(req.getContextPath()
                    + "/admin/flash-sale?save=error&message="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private LocalDateTime parseDateTimeLocal(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thời gian bắt đầu và kết thúc.");
        }

        return LocalDateTime.parse(value);
    }

    private boolean isActiveValue(String value) {
        return "true".equalsIgnoreCase(value)
                || "1".equals(value)
                || "on".equalsIgnoreCase(value)
                || "active".equalsIgnoreCase(value);
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

package com.webshop.app.controller.FlashSaleController;

import com.webshop.app.dao.FlashSaleDAO;
import com.webshop.app.model.FlashSale;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@WebServlet("/admin/flash-sale/save")
public class FlashSaleSaveServlet extends HttpServlet {
    private FlashSaleDAO flashSaleDAO = new FlashSaleDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Lấy dữ liệu từ form
        String idStr = req.getParameter("id");
        String title = req.getParameter("title");
        String startTimeStr = req.getParameter("startTime"); // format: yyyy-MM-ddTHH:mm
        String endTimeStr = req.getParameter("endTime");
        String activeStr = req.getParameter("active");

        FlashSale fs = new FlashSale();
        if (idStr != null && !idStr.isEmpty()) {
            fs.setId(Integer.parseInt(idStr));
        }
        fs.setTitle(title);
        fs.setStartTime(Timestamp.valueOf(LocalDateTime.parse(startTimeStr)));
        fs.setEndTime(Timestamp.valueOf(LocalDateTime.parse(endTimeStr)));
        fs.setActive(activeStr != null);

        // Lưu vào DB
        if (fs.getId() == 0) {
            flashSaleDAO.insert(fs);
        } else {
            flashSaleDAO.update(fs);
        }

        resp.sendRedirect(req.getContextPath() + "/admin/flash-sale");
    }
}
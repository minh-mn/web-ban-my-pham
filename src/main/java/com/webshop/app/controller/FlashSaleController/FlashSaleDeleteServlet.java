package com.webshop.app.controller.FlashSaleController;

import com.webshop.app.dao.FlashSaleDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/flash-sale/delete")
public class FlashSaleDeleteServlet extends HttpServlet {
    private FlashSaleDAO flashSaleDAO = new FlashSaleDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = req.getParameter("id");
        if (idStr != null) {
            flashSaleDAO.delete(Integer.parseInt(idStr));
        }
        resp.sendRedirect(req.getContextPath() + "/admin/flash-sale");
    }
}
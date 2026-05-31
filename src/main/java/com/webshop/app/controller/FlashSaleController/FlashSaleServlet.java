package com.webshop.app.controller.FlashSaleController;

import com.webshop.app.dao.FlashSaleDAO;
import com.webshop.app.dao.FlashSaleItemDAO;
import com.webshop.app.model.FlashSale;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

@WebServlet("/flash-sale")
public class FlashSaleServlet extends HttpServlet {

    private final FlashSaleDAO flashSaleDAO = new FlashSaleDAO();
    private final FlashSaleItemDAO flashSaleItemDAO = new FlashSaleItemDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FlashSale activeFlashSale = flashSaleDAO.findActiveFlashSale();

        if (activeFlashSale != null) {
            req.setAttribute("activeFlashSale", activeFlashSale);
            req.setAttribute("fsItems", flashSaleItemDAO.findByFlashSale(activeFlashSale.getId()));
        } else {
            req.setAttribute("fsItems", Collections.emptyList());
        }

        req.setAttribute("pageTitle", "Flash Deal - MyCosmetic");
        req.setAttribute("pageCss", "flash-sale.css");
        req.setAttribute("pageContent", "/jsp/product/flash-sale-full.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}

package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.FlashSaleDAO;
import com.webshop.app.dao.FlashSaleItemDAO;
import com.webshop.app.model.FlashSale;
import com.webshop.app.model.FlashSaleItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/flash-sale")
public class AdminFlashSaleServlet
        extends HttpServlet {

    private FlashSaleDAO flashSaleDAO =
            new FlashSaleDAO();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        String action =
                req.getParameter("action");

        if(action == null){

            List<FlashSale> flashSales =
                    flashSaleDAO.findAll();

            req.setAttribute(
                    "flashSales",
                    flashSales
            );

            req.getRequestDispatcher(
                    "/jsp/admin/flashsale/flashsale-list.jsp"
            ).forward(req, resp);

            return;
        }

        if(action.equals("new")){

            req.getRequestDispatcher(
                    "/jsp/admin/flashsale/flashsale-form.jsp"
            ).forward(req, resp);

            return;
        }

        if(action.equals("edit")){

            int id = Integer.parseInt(
                    req.getParameter("id")
            );

            FlashSale flashSale =
                    flashSaleDAO.findById(id);

            req.setAttribute(
                    "flashSale",
                    flashSale
            );

            req.getRequestDispatcher(
                    "/jsp/admin/flashsale/flashsale-form.jsp"
            ).forward(req, resp);
        }

        // Trong AdminFlashSaleServlet.java
        if(action.equals("edit")) {
            int id = Integer.parseInt(req.getParameter("id"));

            // 1. Lấy thông tin Flash Sale chính
            FlashSale flashSale = flashSaleDAO.findById(id);
            req.setAttribute("flashSale", flashSale);

            // 2. Lấy thêm danh sách Items (cần import FlashSaleItemDAO)
            FlashSaleItemDAO itemDAO = new FlashSaleItemDAO();
            List<FlashSaleItem> items = itemDAO.findByFlashSale(id);
            req.setAttribute("items", items); // Truyền sang JSP

            req.getRequestDispatcher("/jsp/admin/flashsale/flashsale-form.jsp").forward(req, resp);
            return;
        }
    }

}

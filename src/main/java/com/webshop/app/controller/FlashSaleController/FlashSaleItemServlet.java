package com.webshop.app.controller.FlashSaleController;

import com.webshop.app.dao.FlashSaleItemDAO;
import com.webshop.app.dao.ProductDAO;
import com.webshop.app.model.FlashSaleItem;
import com.webshop.app.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/flash-sale/items")
public class FlashSaleItemServlet extends HttpServlet {
    private final FlashSaleItemDAO flashSaleItemDAO = new FlashSaleItemDAO();
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String flashSaleId = req.getParameter("flashSaleId");

        // Load danh sách sản phẩm hiện có trong Flash Sale
        req.setAttribute("items", flashSaleItemDAO.findByFlashSale(Integer.parseInt(flashSaleId)));
        req.setAttribute("flashSaleId", flashSaleId);

        // Lấy danh sách toàn bộ sản phẩm để đổ vào dropdown chọn (nếu muốn làm UX tốt)
        req.setAttribute("products", productDAO.findAll());

        req.getRequestDispatcher("/jsp/admin/flashsale/flashsale-items.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        String flashSaleId = req.getParameter("flashSaleId");

        if ("add".equals(action)) {
            FlashSaleItem item = new FlashSaleItem();
            item.setFlashSaleId(Integer.parseInt(flashSaleId));

            Product p = new Product();
            p.setId(Integer.parseInt(req.getParameter("productId")));
            item.setProduct(p);

            item.setFlashPrice(Double.parseDouble(req.getParameter("flashPrice")));
            item.setQuantity(Integer.parseInt(req.getParameter("quantity")));
            item.setSoldQuantity(0);

            flashSaleItemDAO.insert(item);

        } else if ("delete".equals(action)) {
            int itemId = Integer.parseInt(req.getParameter("itemId"));
            flashSaleItemDAO.delete(itemId);
        }

        // Redirect để tránh lỗi F5 trùng lặp form
        resp.sendRedirect(req.getContextPath() + "/admin/flash-sale/items?flashSaleId=" + flashSaleId);
    }
}
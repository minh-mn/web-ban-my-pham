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

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_MAX_QUANTITY_PER_USER = 2;

    private final FlashSaleItemDAO flashSaleItemDAO = new FlashSaleItemDAO();
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        int flashSaleId = parseInt(req.getParameter("flashSaleId"), -1);

        if (flashSaleId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/admin/flash-sale?invalidFlashSale=1");
            return;
        }

        // Load danh sách sản phẩm hiện có trong Flash Sale.
        req.setAttribute("items", flashSaleItemDAO.findByFlashSale(flashSaleId));
        req.setAttribute("flashSaleId", flashSaleId);

        // Giá trị mặc định cho ô "Giới hạn / khách" trên form admin.
        req.setAttribute("defaultMaxQuantityPerUser", DEFAULT_MAX_QUANTITY_PER_USER);

        // Lấy danh sách toàn bộ sản phẩm để đổ vào dropdown chọn.
        req.setAttribute("products", productDAO.findAll());

        req.getRequestDispatcher("/jsp/admin/flashsale/flashsale-items.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        int flashSaleId = parseInt(req.getParameter("flashSaleId"), -1);

        if (flashSaleId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/admin/flash-sale?invalidFlashSale=1");
            return;
        }

        if ("add".equals(action)) {
            FlashSaleItem item = new FlashSaleItem();
            item.setFlashSaleId(flashSaleId);

            int productId = parseInt(req.getParameter("productId"), -1);

            if (productId <= 0) {
                resp.sendRedirect(req.getContextPath()
                        + "/admin/flash-sale/items?flashSaleId="
                        + flashSaleId
                        + "&error=invalidProduct");
                return;
            }

            Product product = new Product();
            product.setId(productId);
            item.setProduct(product);

            double flashPrice = parseDouble(req.getParameter("flashPrice"), 0);
            int quantity = parseInt(req.getParameter("quantity"), 0);

            if (flashPrice <= 0 || quantity <= 0) {
                resp.sendRedirect(req.getContextPath()
                        + "/admin/flash-sale/items?flashSaleId="
                        + flashSaleId
                        + "&error=invalidFlashItem");
                return;
            }

            /*
             * Issue 139:
             * Cho phép admin cấu hình giới hạn số lượng mỗi khách được mua
             * cho từng sản phẩm trong Flash Sale.
             *
             * Form nên gửi parameter:
             * - maxQuantityPerUser
             *
             * Nếu form cũ chưa có field này, hệ thống dùng mặc định 2.
             */
            int maxQuantityPerUser = parseInt(
                    req.getParameter("maxQuantityPerUser"),
                    DEFAULT_MAX_QUANTITY_PER_USER
            );

            if (maxQuantityPerUser <= 0) {
                maxQuantityPerUser = DEFAULT_MAX_QUANTITY_PER_USER;
            }

            item.setFlashPrice(flashPrice);
            item.setQuantity(quantity);
            item.setSoldQuantity(0);
            item.setMaxQuantityPerUser(maxQuantityPerUser);

            flashSaleItemDAO.insert(item);

            resp.sendRedirect(req.getContextPath()
                    + "/admin/flash-sale/items?flashSaleId="
                    + flashSaleId
                    + "&added=1");
            return;

        } else if ("delete".equals(action)) {
            int itemId = parseInt(req.getParameter("itemId"), -1);

            if (itemId > 0) {
                flashSaleItemDAO.delete(itemId);
            }

            resp.sendRedirect(req.getContextPath()
                    + "/admin/flash-sale/items?flashSaleId="
                    + flashSaleId
                    + "&deleted=1");
            return;
        }

        // Redirect để tránh lỗi F5 trùng lặp form.
        resp.sendRedirect(req.getContextPath() + "/admin/flash-sale/items?flashSaleId=" + flashSaleId);
    }

    private int parseInt(String raw, int defaultValue) {
        try {
            if (raw == null || raw.isBlank()) {
                return defaultValue;
            }

            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private double parseDouble(String raw, double defaultValue) {
        try {
            if (raw == null || raw.isBlank()) {
                return defaultValue;
            }

            return Double.parseDouble(raw.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}

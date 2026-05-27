package com.webshop.app.controller.PromotionController;

import com.webshop.app.dao.ProductDAO; // Giả sử bạn có DAO này
import com.webshop.app.dao.PromotionEventDAO;
import com.webshop.app.model.Product;
import com.webshop.app.model.PromotionEvent;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;

@WebServlet("/promotions")
public class PromotionServlet extends HttpServlet {

    private final PromotionEventDAO eventDAO = new PromotionEventDAO();
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Lấy chương trình khuyến mãi đang hoạt động
        PromotionEvent activePromotion = eventDAO.findActiveEvent();

        if (activePromotion != null) {
            req.setAttribute("activePromotion", activePromotion);

            // 2. Chuyển đổi LocalDate (endDate) sang Milliseconds để JS đếm ngược hoạt động
            // Mặc định cho kết thúc vào 23:59:59 của ngày endDate
            long endTimeMillis = activePromotion.getEndDate()
                    .atTime(23, 59, 59)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            req.setAttribute("endTimeMillis", endTimeMillis);

            // 3. Lấy danh sách sản phẩm theo Scope (ALL, CATEGORY, BRAND)
            // Cần viết logic này trong ProductDAO dựa vào activePromotion.getScope()
            List<Product> products = productDAO.findProductsByPromotion(activePromotion);
            req.setAttribute("promotions", products); // Chữ "promotions" này map với vòng lặp trong JSP của bạn
        }

        // 4. Set layout và forward tới JSP
        req.setAttribute("pageTitle", "Chương trình Khuyến Mãi");
        req.setAttribute("pageContent", "/jsp/product/promotions.jsp");
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}
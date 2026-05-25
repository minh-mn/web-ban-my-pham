package com.webshop.app.controller.FlashSaleController;

import com.webshop.app.dao.*;
import com.webshop.app.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/flash-sale")
public class FlashSaleServlet extends HttpServlet {
    private final FlashSaleDAO flashSaleDAO = new FlashSaleDAO();
    private final FlashSaleItemDAO flashSaleItemDAO = new FlashSaleItemDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. Lấy dữ liệu
        FlashSale activeFS = flashSaleDAO.findActiveFlashSale();
        if (activeFS != null) {
            req.setAttribute("activeFlashSale", activeFS);
            req.setAttribute("fsItems", flashSaleItemDAO.findByFlashSale(activeFS.getId()));
        }

        // 1. Đặt tiêu đề cho tab trình duyệt
        req.setAttribute("pageTitle", "MyCosmetic | Trang Flash Sale Siêu Tốc");

// 2. Kéo file CSS vào (Hãy điền tên file CSS chứa class .product-grid của bạn, ví dụ: product-list.css hoặc home.css)
        req.setAttribute("pageCss", "product-list.css");

// 3. Khai báo file nội dung chính muốn hiển thị
        req.setAttribute("pageContent", "/jsp/product/flash-sale-full.jsp");

// 4. Chuyển tiếp tới file layout tổng (base.jsp) để nó tự lắp ghép Header/Footer/CSS
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}

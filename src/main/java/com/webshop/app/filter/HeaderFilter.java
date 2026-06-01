package com.webshop.app.filter;

import com.webshop.app.dao.FlashSaleDAO;
import com.webshop.app.model.FlashSale;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter("/*") // Áp dụng cho tất cả đường dẫn
public class HeaderFilter implements Filter {

    private final FlashSaleDAO flashSaleDAO = new FlashSaleDAO();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Kiểm tra xem có Flash Sale nào đang chạy không
        FlashSale activeFlashSale = flashSaleDAO.findActiveFlashSale();

        // Đặt thuộc tính vào request để JSP có thể đọc được
        request.setAttribute("isFlashSaleActive", activeFlashSale != null);

        // Tiếp tục luồng xử lý
        chain.doFilter(request, response);
    }
}
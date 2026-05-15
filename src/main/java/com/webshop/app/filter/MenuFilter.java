package com.webshop.app.filter;

import com.webshop.app.dao.CategoryDAO;
import com.webshop.app.model.Category;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;
import java.util.List;

@WebFilter("/*") // Áp dụng cho tất cả các URL
public class MenuFilter implements Filter {
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Sử dụng hàm findParents() bạn đã viết để lấy cây danh mục
        List<Category> menuCategories = categoryDAO.findParents();
        request.setAttribute("menuCategories", menuCategories);

        chain.doFilter(request, response);
    }
}
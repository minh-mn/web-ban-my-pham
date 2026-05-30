package com.webshop.app.filter;

import com.webshop.app.dao.BrandDAO;
import com.webshop.app.dao.CategoryDAO;
import com.webshop.app.model.Brand;
import com.webshop.app.model.Category;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;
import java.util.List;

@WebFilter("/*")
public class MenuFilter implements Filter {
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BrandDAO brandDAO = new BrandDAO(); // Khai báo BrandDAO

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 1. Lấy dữ liệu Categories
        List<Category> categories = categoryDAO.findParents();
        request.setAttribute("categories", categories); // Phải khớp với ${categories} trong header.jsp

        // 2. Lấy dữ liệu Brands
        List<Brand> brands = brandDAO.findAllWithProductCount();
        request.setAttribute("brands", brands); // Phải khớp với ${brands} trong header.jsp

        chain.doFilter(request, response);
    }
}

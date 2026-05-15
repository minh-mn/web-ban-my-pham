package com.webshop.app.filter;

import com.webshop.app.dao.CategoryDAO;
import com.webshop.app.dao.PolicyDAO;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter; // Sửa import thành WebFilter

import java.io.IOException;

@WebFilter("/*") // Thay @WebServlet thành @WebFilter
public class GlobalDataFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        CategoryDAO cdao = new CategoryDAO();
        PolicyDAO pdao = new PolicyDAO();

        request.setAttribute("categoryList", cdao.findActiveForMenu());
        request.setAttribute("policyList", pdao.getAllPolicies());

        chain.doFilter(request, response);
    }
}
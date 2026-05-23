package com.webshop.app.filter;

import com.webshop.app.dao.*;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;

@WebFilter("/*")
public class GlobalDataFilter implements Filter {

    private WebsiteSettingDAO settingDAO = new WebsiteSettingDAO();
    private PageDAO pageDAO = new PageDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        // SETTINGS
        request.setAttribute(
                "settings",
                settingDAO.getAllSettings()
        );

        // FOOTER PAGES
        request.setAttribute(
                "policyList",
                pageDAO.getByType("policy")
        );

        // FOOTER QUICK LINKS
        request.setAttribute(
                "footerPages",
                pageDAO.getFooterPages()
        );

        // CATEGORY MENU
        request.setAttribute(
                "categoryList",
                categoryDAO.findActiveForMenu()
        );

        chain.doFilter(request, response);
    }
}

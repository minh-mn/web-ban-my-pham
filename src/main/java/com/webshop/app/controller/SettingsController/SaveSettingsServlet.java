package com.webshop.app.controller.SettingsController;

import com.webshop.app.dao.WebsiteSettingDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/settings/save")
public class SaveSettingsServlet extends HttpServlet {

    private WebsiteSettingDAO dao = new WebsiteSettingDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // CONTACT
        dao.saveOrUpdate("hotline", req.getParameter("hotline"));
        dao.saveOrUpdate("sales_email", req.getParameter("sales_email"));
        dao.saveOrUpdate("hr_email", req.getParameter("hr_email"));
        dao.saveOrUpdate("address", req.getParameter("address"));

        // SOCIAL
        dao.saveOrUpdate("facebook", req.getParameter("facebook"));
        dao.saveOrUpdate("instagram", req.getParameter("instagram"));

        // COMPANY INFO
        dao.saveOrUpdate("company_name", req.getParameter("company_name"));
        dao.saveOrUpdate("business_code", req.getParameter("business_code"));
        dao.saveOrUpdate("business_date", req.getParameter("business_date"));

        // OPTIONAL
        dao.saveOrUpdate("name_website", req.getParameter("name_website"));
        dao.saveOrUpdate("copyright_year", req.getParameter("copyright_year"));

        resp.sendRedirect(req.getContextPath() + "/admin/settings");
    }
}

package com.webshop.app.controller.SettingsController;

import com.webshop.app.dao.WebsiteSettingDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/admin/settings")
public class SettingsController extends HttpServlet {

    private WebsiteSettingDAO dao = new WebsiteSettingDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");

        // =========================
        // EDIT FORM
        // =========================
        if ("edit".equals(action)) {

            req.setAttribute("settings", dao.getMapSettings());

            req.getRequestDispatcher("/jsp/admin/settings/setting_form.jsp")
                    .forward(req, resp);
            return;
        }

        // =========================
        // LIST VIEW
        // =========================
        req.setAttribute("settings", dao.getAllSettings());

        req.getRequestDispatcher("/jsp/admin/settings/setting_list.jsp")
                .forward(req, resp);
    }
}
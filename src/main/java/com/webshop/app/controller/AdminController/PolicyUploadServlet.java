package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.PolicyDAO;
import com.webshop.app.model.Policy;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;

@WebServlet("/admin/policy/upload")
public class PolicyUploadServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String title = request.getParameter("title");
        String slug = request.getParameter("slug");

        Part filePart = request.getPart("file");
        String fileName = filePart.getSubmittedFileName();

        String uploadPath = getServletContext().getRealPath("/upload/policy");

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        filePart.write(uploadPath + File.separator + fileName);

        Policy p = new Policy();
        p.setTitle(title);
        p.setSlug(slug);
        p.setFileName(fileName);

        new PolicyDAO().insert(p);

        response.sendRedirect(request.getContextPath() + "/admin/policy/list");
    }
}

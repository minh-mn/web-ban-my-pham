package com.webshop.app.controller.PagesController;

import com.webshop.app.dao.PageDAO;
import com.webshop.app.model.Page;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;

@WebServlet("/admin/pages/save")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
        maxFileSize = 1024 * 1024 * 10,       // 10MB
        maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class PageSaveController extends HttpServlet {

    private PageDAO dao = new PageDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Page p = new Page();

        String id = req.getParameter("id");
        if (id != null && !id.isEmpty()) {
            p.setId(Integer.parseInt(id));
        }

        p.setTitle(req.getParameter("title"));
        p.setSlug(req.getParameter("slug"));
        p.setType(req.getParameter("type"));

        p.setContent(req.getParameter("content"));

   
        String currentThumbnail = req.getParameter("thumbnail");
        String thumbnailPath = currentThumbnail;

        Part filePart = req.getPart("thumbnailFile");
        if (filePart != null && filePart.getSize() > 0) {
            String fileName = getFileName(filePart);
            if (fileName != null && !fileName.isEmpty()) {
                String uploadPath = getServletContext().getRealPath("") + File.separator + "assets" + File.separator + "uploads";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                String newFileName = System.currentTimeMillis() + "_" + fileName;
                String fullSavePath = uploadPath + File.separator + newFileName;
                filePart.write(fullSavePath);
                thumbnailPath = req.getContextPath() + "/assets/uploads/" + newFileName;
            }
        }
        p.setThumbnail(thumbnailPath);

        dao.save(p);

        resp.sendRedirect(req.getContextPath() + "/admin/pages");
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "";
    }
}

package com.webshop.app.controller.BlogController;

import com.webshop.app.config.UploadConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/view-image")
public class ImageServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fileName = request.getParameter("fileName");

        // Lấy đường dẫn file từ cấu hình
        Path filePath = UploadConfig.resolveEventFile(fileName);

        if (Files.exists(filePath)) {
            // Thiết lập kiểu nội dung là ảnh
            String mimeType = getServletContext().getMimeType(filePath.toString());
            response.setContentType(mimeType != null ? mimeType : "image/jpeg");

            // Ghi dữ liệu file ra output stream
            Files.copy(filePath, response.getOutputStream());
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
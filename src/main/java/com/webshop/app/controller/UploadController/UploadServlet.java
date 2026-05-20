package com.webshop.app.controller.UploadController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

import com.webshop.app.config.UploadConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/uploads/*")
public class UploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        /*
         * Ví dụ:
         * /uploads/banner/abc.png
         * /uploads/product/abc.png
         * /uploads/product/gallery/abc.png
         */
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Chặn path traversal cơ bản
        if (pathInfo.contains("..") || pathInfo.contains("\\") || pathInfo.contains("//")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Bỏ dấu "/" đầu tiên
        String relative = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;

        if (relative.isBlank()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        /*
         * Chỉ cho public các thư mục upload hợp lệ.
         * Tránh việc lỡ có file khác trong BASE_DIR cũng bị public.
         */
        if (!isAllowedUploadPath(relative)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Path baseDir = UploadConfig.BASE_DIR.toAbsolutePath().normalize();
        Path filePath = baseDir.resolve(relative).toAbsolutePath().normalize();

        // Bảo vệ path traversal sau normalize
        if (!filePath.startsWith(baseDir)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mime = URLConnection.guessContentTypeFromName(filePath.getFileName().toString());

        if (mime == null) {
            mime = "application/octet-stream";
        }

        resp.setContentType(mime);
        resp.setHeader("Cache-Control", "public, max-age=86400");
        resp.setHeader("X-Content-Type-Options", "nosniff");

        try {
            resp.setContentLengthLong(Files.size(filePath));
        } catch (Exception ignored) {
        }

        try (InputStream inputStream = Files.newInputStream(filePath);
             OutputStream outputStream = resp.getOutputStream()) {

            inputStream.transferTo(outputStream);
        }
    }

    private boolean isAllowedUploadPath(String relative) {
        return relative.startsWith("banner/")
                || relative.startsWith("product/")
                || relative.startsWith("policy/");
    }
}
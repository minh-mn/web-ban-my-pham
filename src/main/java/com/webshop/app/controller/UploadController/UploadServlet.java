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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // /uploads/banner/abc.png  -> pathInfo = /banner/abc.png
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.contains("..")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // loại bỏ leading "/"
        String relative = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;

        Path filePath = UploadConfig.BASE_DIR.resolve(relative).normalize();

        // Bảo vệ path traversal
        if (!filePath.startsWith(UploadConfig.BASE_DIR)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mime = URLConnection.guessContentTypeFromName(filePath.getFileName().toString());
        if (mime == null) mime = "application/octet-stream";

        resp.setContentType(mime);
        resp.setHeader("Cache-Control", "public, max-age=86400"); // cache 1 ngày

        try (InputStream in = Files.newInputStream(filePath);
             OutputStream out = resp.getOutputStream()) {
            in.transferTo(out);
        }
    }
}

package com.webshop.app.controller.UploadController;

import com.webshop.app.config.UploadConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/uploads/*")
public class UploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /*
     * Chỉ public các loại file cần thiết.
     * Tránh lỡ public file lạ trong MyCosmeticShopUploads.
     *
     * Issue 123:
     * Bổ sung video sản phẩm: mp4, webm, mov, m4v.
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".webp",
            ".gif",
            ".svg",
            ".pdf",
            ".mp4",
            ".webm",
            ".mov",
            ".m4v"
    );

    @Override
    public void init() throws ServletException {
        super.init();

        /*
         * Đảm bảo các thư mục:
         * MyCosmeticShopUploads/banner
         * MyCosmeticShopUploads/product
         * MyCosmeticShopUploads/product/gallery
         * MyCosmeticShopUploads/product/media
         * MyCosmeticShopUploads/policy
         * MyCosmeticShopUploads/brand
         * đã tồn tại khi app chạy.
         */
        UploadConfig.ensureUploadDirectories();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        /*
         * Ví dụ URL:
         * /uploads/banner/abc.png
         * /uploads/product/abc.png
         * /uploads/product/gallery/abc.png
         * /uploads/product/media/abc.mp4
         * /uploads/policy/abc.pdf
         * /uploads/brand/abc.png
         */
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        /*
         * Chặn path traversal cơ bản trước khi resolve path.
         */
        if (hasUnsafePath(pathInfo)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        /*
         * Bỏ dấu "/" đầu tiên.
         * Ví dụ:
         * /product/a.png -> product/a.png
         * /product/media/video.mp4 -> product/media/video.mp4
         */
        String relative = pathInfo.startsWith("/")
                ? pathInfo.substring(1)
                : pathInfo;

        relative = normalizeRelativePath(relative);

        if (relative.isBlank()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        /*
         * Chỉ cho public các thư mục upload hợp lệ.
         */
        if (!isAllowedUploadPath(relative)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        /*
         * Chỉ cho phép extension ảnh/pdf/video hợp lệ.
         */
        if (!isAllowedExtension(relative)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Path baseDir = UploadConfig.BASE_DIR.toAbsolutePath().normalize();
        Path filePath = baseDir.resolve(relative).toAbsolutePath().normalize();

        /*
         * Bảo vệ path traversal sau normalize.
         */
        if (!filePath.startsWith(baseDir)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mime = URLConnection.guessContentTypeFromName(filePath.getFileName().toString());

        /*
         * Một số server/JDK có thể không đoán đúng MIME cho webm/mov/m4v,
         * nên fallback thủ công để trình duyệt phát video ổn định hơn.
         */
        if (mime == null || mime.isBlank()) {
            mime = detectMimeFromExtension(filePath.getFileName().toString());
        }

        if (mime == null || mime.isBlank()) {
            mime = "application/octet-stream";
        }

        resp.setContentType(mime);
        resp.setHeader("Cache-Control", "public, max-age=86400");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("Content-Disposition", "inline");

        try {
            resp.setContentLengthLong(Files.size(filePath));
        } catch (Exception ignored) {
            // Không bắt buộc set Content-Length.
        }

        try (InputStream inputStream = Files.newInputStream(filePath);
             OutputStream outputStream = resp.getOutputStream()) {

            inputStream.transferTo(outputStream);
        }
    }

    private boolean isAllowedUploadPath(String relative) {
        return relative.startsWith("banner/")
                || relative.startsWith("product/")
                || relative.startsWith("policy/")
                || relative.startsWith("brand/");
    }

    private boolean isAllowedExtension(String relative) {
        String value = relative.toLowerCase(Locale.ROOT);

        return ALLOWED_EXTENSIONS.stream().anyMatch(value::endsWith);
    }

    private String detectMimeFromExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }

        String value = fileName.toLowerCase(Locale.ROOT);

        if (value.endsWith(".jpg") || value.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        if (value.endsWith(".png")) {
            return "image/png";
        }

        if (value.endsWith(".webp")) {
            return "image/webp";
        }

        if (value.endsWith(".gif")) {
            return "image/gif";
        }

        if (value.endsWith(".svg")) {
            return "image/svg+xml";
        }

        if (value.endsWith(".pdf")) {
            return "application/pdf";
        }

        if (value.endsWith(".mp4") || value.endsWith(".m4v")) {
            return "video/mp4";
        }

        if (value.endsWith(".webm")) {
            return "video/webm";
        }

        if (value.endsWith(".mov")) {
            return "video/quicktime";
        }

        return null;
    }

    private boolean hasUnsafePath(String path) {
        if (path == null || path.isBlank()) {
            return true;
        }

        return path.contains("..")
                || path.contains("\\")
                || path.contains("//")
                || path.contains("%2e")
                || path.contains("%2E")
                || path.contains("%5c")
                || path.contains("%5C");
    }

    private String normalizeRelativePath(String relative) {
        if (relative == null) {
            return "";
        }

        return relative
                .trim()
                .replace("\\", "/")
                .replaceAll("/{2,}", "/");
    }
}
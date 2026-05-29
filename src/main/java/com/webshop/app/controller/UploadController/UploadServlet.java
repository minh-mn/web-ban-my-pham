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
     * Tránh public file lạ trong MyCosmeticShopUploads.
     *
     * Bao gồm:
     * - banner
     * - brand
     * - policy
     * - product
     * - product/gallery
     * - product/media
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

    private static final int BUFFER_SIZE = 8192;

    @Override
    public void init() throws ServletException {
        super.init();

        /*
         * Đảm bảo các thư mục tồn tại khi app chạy:
         * MyCosmeticShopUploads/banner
         * MyCosmeticShopUploads/product
         * MyCosmeticShopUploads/product/gallery
         * MyCosmeticShopUploads/product/media
         * MyCosmeticShopUploads/policy
         * MyCosmeticShopUploads/brand
         */
        UploadConfig.ensureUploadDirectories();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        /*
         * Ví dụ URL:
         * /uploads/banner/abc.png
         * /uploads/brand/abc.png
         * /uploads/policy/abc.pdf
         * /uploads/product/abc.png
         * /uploads/product/gallery/abc.png
         * /uploads/product/media/abc.mp4
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

        long fileLength = Files.size(filePath);

        String fileName = filePath.getFileName().toString();
        String mime = URLConnection.guessContentTypeFromName(fileName);

        if (mime == null || mime.isBlank()) {
            mime = detectMimeFromExtension(fileName);
        }

        if (mime == null || mime.isBlank()) {
            mime = "application/octet-stream";
        }

        /*
         * Header chung.
         */
        resp.setContentType(mime);
        resp.setHeader("Accept-Ranges", "bytes");
        resp.setHeader("Cache-Control", "public, max-age=86400");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("Content-Disposition", "inline");

        /*
         * Hỗ trợ Range request để video có thể phát/tua ổn định.
         */
        String rangeHeader = req.getHeader("Range");

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            servePartialContent(resp, filePath, fileLength, rangeHeader);
            return;
        }

        /*
         * Trả full file nếu không có Range.
         */
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentLengthLong(fileLength);

        try (InputStream inputStream = Files.newInputStream(filePath);
             OutputStream outputStream = resp.getOutputStream()) {

            copy(inputStream, outputStream, fileLength);
        }
    }

    private void servePartialContent(HttpServletResponse resp,
                                     Path filePath,
                                     long fileLength,
                                     String rangeHeader)
            throws IOException {

        Range range = parseRange(rangeHeader, fileLength);

        if (range == null) {
            resp.setHeader("Content-Range", "bytes */" + fileLength);
            resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return;
        }

        long contentLength = range.end - range.start + 1;

        resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        resp.setHeader("Content-Range", "bytes " + range.start + "-" + range.end + "/" + fileLength);
        resp.setContentLengthLong(contentLength);

        try (InputStream inputStream = Files.newInputStream(filePath);
             OutputStream outputStream = resp.getOutputStream()) {

            long skipped = inputStream.skip(range.start);

            while (skipped < range.start) {
                long current = inputStream.skip(range.start - skipped);

                if (current <= 0) {
                    break;
                }

                skipped += current;
            }

            copy(inputStream, outputStream, contentLength);
        }
    }

    private Range parseRange(String rangeHeader, long fileLength) {
        if (rangeHeader == null || !rangeHeader.startsWith("bytes=") || fileLength <= 0) {
            return null;
        }

        try {
            String rangeValue = rangeHeader.substring("bytes=".length()).trim();

            /*
             * Chỉ xử lý range đầu tiên nếu browser gửi nhiều range.
             */
            int commaIndex = rangeValue.indexOf(",");
            if (commaIndex >= 0) {
                rangeValue = rangeValue.substring(0, commaIndex).trim();
            }

            if (rangeValue.isBlank()) {
                return null;
            }

            long start;
            long end;

            if (rangeValue.startsWith("-")) {
                /*
                 * bytes=-500
                 * Lấy 500 bytes cuối.
                 */
                long suffixLength = Long.parseLong(rangeValue.substring(1));

                if (suffixLength <= 0) {
                    return null;
                }

                start = Math.max(0, fileLength - suffixLength);
                end = fileLength - 1;
            } else {
                /*
                 * bytes=0-999
                 * bytes=500-
                 */
                String[] parts = rangeValue.split("-", 2);

                start = Long.parseLong(parts[0]);

                if (parts.length > 1 && !parts[1].isBlank()) {
                    end = Long.parseLong(parts[1]);
                } else {
                    end = fileLength - 1;
                }
            }

            if (start < 0 || end < start || start >= fileLength) {
                return null;
            }

            end = Math.min(end, fileLength - 1);

            return new Range(start, end);

        } catch (Exception e) {
            return null;
        }
    }

    private void copy(InputStream inputStream,
                      OutputStream outputStream,
                      long length)
            throws IOException {

        byte[] buffer = new byte[BUFFER_SIZE];
        long remaining = length;

        while (remaining > 0) {
            int maxRead = (int) Math.min(buffer.length, remaining);
            int read = inputStream.read(buffer, 0, maxRead);

            if (read == -1) {
                break;
            }

            outputStream.write(buffer, 0, read);
            remaining -= read;
        }
    }

    private boolean isAllowedUploadPath(String relative) {
        return relative.startsWith("banner/")
                || relative.startsWith("brand/")
                || relative.startsWith("policy/")
                || relative.startsWith("product/");
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

        String lower = path.toLowerCase(Locale.ROOT);

        return path.contains("..")
                || path.contains("\\")
                || path.contains("//")
                || lower.contains("%2e")
                || lower.contains("%5c")
                || lower.contains("%2f");
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

    private static final class Range {
        private final long start;
        private final long end;

        private Range(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }
}
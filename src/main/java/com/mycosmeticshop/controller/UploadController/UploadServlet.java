package com.mycosmeticshop.controller.UploadController;

import com.mycosmeticshop.config.UploadConfig;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

/*
 * Servlet dùng để phục vụ file upload tĩnh từ thư mục lưu trữ trên server
 * URL truy cập: /uploads/*
 *
 * Ví dụ:
 * - /uploads/banner/abc.png
 * - /uploads/product/demo.jpg
 *
 * Chức năng:
 * - Đọc file theo path trong URL
 * - Chặn path traversal
 * - Kiểm tra file tồn tại
 * - Trả file về cho trình duyệt với đúng MIME type
 */
@WebServlet("/uploads/*")
public class UploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /*
     * Phương thức GET
     * Trả file upload về cho client
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // =====================================================
        // 1) LẤY PATH TƯƠNG ĐỐI TỪ URL
        // =====================================================
        /*
         * Ví dụ:
         * URL      : /uploads/banner/abc.png
         * pathInfo : /banner/abc.png
         */
        String pathInfo = req.getPathInfo();

        /*
         * Nếu path không hợp lệ:
         * - null
         * - "/"
         * - chứa ".." (dấu hiệu path traversal)
         * thì trả về 404
         */
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.contains("..")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Bỏ dấu "/" ở đầu để lấy path tương đối
        String relative = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;

        // =====================================================
        // 2) GHÉP VỚI THƯ MỤC GỐC UPLOAD
        // =====================================================
        Path filePath = UploadConfig.BASE_DIR.resolve(relative).normalize();

        // =====================================================
        // 3) CHẶN PATH TRAVERSAL
        // =====================================================
        /*
         * Đảm bảo file sau khi normalize vẫn nằm bên trong BASE_DIR
         * Nếu không -> truy cập trái phép
         */
        if (!filePath.startsWith(UploadConfig.BASE_DIR)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // =====================================================
        // 4) KIỂM TRA FILE TỒN TẠI
        // =====================================================
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // =====================================================
        // 5) XÁC ĐỊNH MIME TYPE
        // =====================================================
        String mime = URLConnection.guessContentTypeFromName(filePath.getFileName().toString());

        // Nếu không đoán được loại file thì dùng kiểu nhị phân mặc định
        if (mime == null) {
            mime = "application/octet-stream";
        }

        resp.setContentType(mime);

        /*
         * Cho phép trình duyệt cache file trong 1 ngày
         * giúp tải ảnh / file tĩnh nhanh hơn
         */
        resp.setHeader("Cache-Control", "public, max-age=86400");

        // =====================================================
        // 6) GỬI NỘI DUNG FILE VỀ CLIENT
        // =====================================================
        try (InputStream in = Files.newInputStream(filePath);
             OutputStream out = resp.getOutputStream()) {

            in.transferTo(out);
        }
    }
}
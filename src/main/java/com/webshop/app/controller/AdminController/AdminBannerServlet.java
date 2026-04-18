package com.webshop.app.controller.AdminController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Set;

import com.webshop.app.config.UploadConfig;
import com.webshop.app.dao.BannerDAO;
import com.webshop.app.model.Banner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/admin/banners")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 20 * 1024 * 1024
)
public class AdminBannerServlet extends HttpServlet {

    private final BannerDAO bannerDAO = new BannerDAO();

    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg", "webp", "gif");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "new":
                req.getRequestDispatcher("/jsp/admin/banner/banner_form.jsp").forward(req, resp);
                return;

            case "edit": {
                int id = safeParseInt(req.getParameter("id"), -1);
                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/banners");
                    return;
                }
                Banner banner = bannerDAO.findById(id);
                if (banner == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/banners");
                    return;
                }
                req.setAttribute("banner", banner);
                req.getRequestDispatcher("/jsp/admin/banner/banner_form.jsp").forward(req, resp);
                return;
            }

            default:
                req.setAttribute("banners", bannerDAO.findAll());
                req.getRequestDispatcher("/jsp/admin/banner/banner_list.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Nếu bạn đã có SetCharacterEncodingFilter trong web.xml thì dòng này có thể bỏ
        req.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null) action = "create";

        switch (action) {
            case "create": {
                Banner b = new Banner();
                bind(req, b, true);
                bannerDAO.create(b);
                break;
            }

            case "update": {
                int id = safeParseInt(req.getParameter("id"), -1);
                if (id <= 0) break;

                Banner b = bannerDAO.findById(id);
                if (b != null) {
                    bind(req, b, false);
                    b.setId(id);
                    bannerDAO.update(b);
                }
                break;
            }

            case "delete": {
                int id = safeParseInt(req.getParameter("id"), -1);
                if (id > 0) {
                    // Tuỳ nghiệp vụ: bạn có thể lấy banner để xoá file vật lý trước khi delete DB
                    // Banner b = bannerDAO.findById(id);
                    bannerDAO.delete(id);
                    // if (b != null) tryDeletePhysicalFile(b.getImageUrl());
                }
                break;
            }

            case "toggle": {
                int id = safeParseInt(req.getParameter("id"), -1);
                if (id > 0) bannerDAO.toggleActive(id);
                break;
            }

            default:
                // ignore
                break;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/banners");
    }

    private void bind(HttpServletRequest req, Banner b, boolean isCreate)
            throws IOException, ServletException {

        b.setTitle(trim(req.getParameter("title")));
        b.setLink(trim(req.getParameter("link")));
        b.setActive(Objects.equals(req.getParameter("active"), "1"));

        // Update: giữ ảnh cũ nếu không upload ảnh mới
        String imageUrl = isCreate ? "" : trim(req.getParameter("existingImage"));

        Part imagePart = req.getPart("imageFile");
        if (imagePart != null && imagePart.getSize() > 0) {

            // Validate MIME (nhẹ)
            String contentType = imagePart.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                throw new ServletException("File upload không hợp lệ (chỉ chấp nhận ảnh).");
            }

            // Validate extension
            String original = Paths.get(imagePart.getSubmittedFileName()).getFileName().toString();
            String ext = getExtensionLower(original);
            if (ext.isEmpty() || !ALLOWED_EXT.contains(ext)) {
                throw new ServletException("Định dạng ảnh không hỗ trợ. Chỉ chấp nhận: " + ALLOWED_EXT);
            }

            // đảm bảo folder tồn tại
            Files.createDirectories(UploadConfig.BANNER_DIR);

            String baseName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
            String safeName = System.currentTimeMillis() + "_" + baseName;

            Path dest = UploadConfig.BANNER_DIR.resolve(safeName);

            // Ghi file ổn định
            try (InputStream in = imagePart.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }

            // URL public
            imageUrl = "/uploads/banner/" + safeName;
        }

        // Create bắt buộc ảnh? tuỳ nghiệp vụ
        if (isCreate && (imageUrl == null || imageUrl.isBlank())) {
            imageUrl = "";
            // hoặc throw new ServletException("Vui lòng chọn ảnh banner.");
        }

        b.setImageUrl(imageUrl);
    }

    private static int safeParseInt(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (Exception e) { return fallback; }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String getExtensionLower(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase();
    }

    // Optional: xoá file vật lý (nếu bạn muốn khi delete)
    // private void tryDeletePhysicalFile(String imageUrl) {
    //     if (imageUrl == null || imageUrl.isBlank()) return;
    //     // imageUrl dạng "/uploads/banner/xxx.jpg" -> map về UploadConfig.BANNER_DIR/xxx.jpg
    //     String prefix = "/uploads/banner/";
    //     if (!imageUrl.startsWith(prefix)) return;
    //     String fileName = imageUrl.substring(prefix.length());
    //     try {
    //         Files.deleteIfExists(UploadConfig.BANNER_DIR.resolve(fileName));
    //     } catch (Exception ignored) {}
    // }
}

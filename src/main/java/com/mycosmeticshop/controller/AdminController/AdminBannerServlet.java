package com.mycosmeticshop.controller.AdminController;

import com.mycosmeticshop.config.UploadConfig;
import com.mycosmeticshop.dao.BannerDAO;
import com.mycosmeticshop.model.Banner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Set;

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
        if (action == null) {
            action = "list";
        }

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

        // Nếu project đã có filter set UTF-8 toàn cục thì có thể bỏ dòng này
        req.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null) {
            action = "create";
        }

        switch (action) {
            case "create": {
                Banner banner = new Banner();
                bind(req, banner, true);
                bannerDAO.create(banner);
                break;
            }

            case "update": {
                int id = safeParseInt(req.getParameter("id"), -1);
                if (id <= 0) {
                    break;
                }

                Banner banner = bannerDAO.findById(id);
                if (banner != null) {
                    bind(req, banner, false);
                    banner.setId(id);
                    bannerDAO.update(banner);
                }
                break;
            }

            case "delete": {
                int id = safeParseInt(req.getParameter("id"), -1);
                if (id > 0) {
                    // Nếu muốn xóa luôn file vật lý thì mở phần dưới ra dùng
                    // Banner banner = bannerDAO.findById(id);
                    bannerDAO.delete(id);
                    // if (banner != null) {
                    //     tryDeletePhysicalFile(banner.getImageUrl());
                    // }
                }
                break;
            }

            case "toggle": {
                int id = safeParseInt(req.getParameter("id"), -1);
                if (id > 0) {
                    bannerDAO.toggleActive(id);
                }
                break;
            }

            default:
                break;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/banners");
    }

    private void bind(HttpServletRequest req, Banner banner, boolean isCreate)
            throws IOException, ServletException {

        banner.setTitle(trim(req.getParameter("title")));
        banner.setLink(trim(req.getParameter("link")));
        banner.setActive(Objects.equals(req.getParameter("active"), "1"));

        // Khi update thì giữ ảnh cũ nếu người dùng không upload ảnh mới
        String imageUrl = isCreate ? "" : trim(req.getParameter("existingImage"));

        Part imagePart = req.getPart("imageFile");
        if (imagePart != null && imagePart.getSize() > 0) {

            // Kiểm tra MIME type cơ bản
            String contentType = imagePart.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                throw new ServletException("File upload không hợp lệ (chỉ chấp nhận ảnh).");
            }

            // Kiểm tra phần mở rộng file
            String original = Paths.get(imagePart.getSubmittedFileName()).getFileName().toString();
            String ext = getExtensionLower(original);
            if (ext.isEmpty() || !ALLOWED_EXT.contains(ext)) {
                throw new ServletException("Định dạng ảnh không hỗ trợ. Chỉ chấp nhận: " + ALLOWED_EXT);
            }

            // Đảm bảo thư mục upload tồn tại
            Files.createDirectories(UploadConfig.BANNER_DIR);

            // Làm sạch tên file để tránh ký tự lạ
            String baseName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
            String safeName = System.currentTimeMillis() + "_" + baseName;

            Path dest = UploadConfig.BANNER_DIR.resolve(safeName);

            // Ghi file vào thư mục vật lý
            try (InputStream in = imagePart.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }

            // Đường dẫn public để hiển thị ảnh trên web
            imageUrl = "/uploads/banner/" + safeName;
        }

        // Nếu là create mà chưa có ảnh thì tạm để rỗng
        // Có thể đổi thành throw exception nếu muốn bắt buộc chọn ảnh
        if (isCreate && (imageUrl == null || imageUrl.isBlank())) {
            imageUrl = "";
        }

        banner.setImageUrl(imageUrl);
    }

    private static int safeParseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String getExtensionLower(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase();
    }

    // Có thể dùng nếu muốn xóa luôn file vật lý khi xóa banner trong DB
    // private void tryDeletePhysicalFile(String imageUrl) {
    //     if (imageUrl == null || imageUrl.isBlank()) return;
    //
    //     String prefix = "/uploads/banner/";
    //     if (!imageUrl.startsWith(prefix)) return;
    //
    //     String fileName = imageUrl.substring(prefix.length());
    //     try {
    //         Files.deleteIfExists(UploadConfig.BANNER_DIR.resolve(fileName));
    //     } catch (Exception ignored) {
    //     }
    // }
}
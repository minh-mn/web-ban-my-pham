package com.webshop.app.controller.AdminController;

import com.webshop.app.config.UploadConfig;
import com.webshop.app.dao.PolicyDAO;
import com.webshop.app.model.Policy;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/policy-detail")
public class PolicyController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final PolicyDAO policyDAO = new PolicyDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        /*
         * Lấy slug từ link ở footer hoặc trang chính sách.
         * Ví dụ:
         * /policy-detail?slug=chinh-sach-doi-tra
         */
        String slug = trim(request.getParameter("slug"));

        if (slug.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        Policy policy = policyDAO.getPolicyBySlug(slug);

        if (policy == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        String fileName = trim(policy.getFileName());

        if (fileName.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        /*
         * Database nên lưu fileName, ví dụ:
         * abc123.pdf
         *
         * Controller sẽ tạo URL public:
         * /uploads/policy/abc123.pdf
         *
         * Nếu dữ liệu cũ trong DB đã là /uploads/policy/abc123.pdf
         * thì giữ nguyên để tránh lỗi ảnh/file cũ.
         */
        String fileUrl = buildPolicyFileUrl(fileName);

        request.setAttribute("fileName", fileName);
        request.setAttribute("fileUrl", fileUrl);
        request.setAttribute("policyTitle", policy.getTitle());

        /*
         * Nếu file JSP của bạn đang nằm ở:
         * src/main/webapp/WEB-INF/jsp/policy-detail.jsp
         * thì giữ dòng này.
         */
        request.getRequestDispatcher("/WEB-INF/jsp/policy-detail.jsp").forward(request, response);

        /*
         * Nếu file JSP thật sự nằm ở:
         * src/main/webapp/jsp/policy-detail.jsp
         * thì đổi dòng forward phía trên thành:
         *
         * request.getRequestDispatcher("/jsp/policy-detail.jsp").forward(request, response);
         */
    }

    private static String buildPolicyFileUrl(String fileName) {
        if (fileName == null || fileName.trim().isBlank()) {
            return "";
        }

        String value = fileName.trim().replace("\\", "/");

        if (value.startsWith("http://")
                || value.startsWith("https://")
                || value.startsWith("data:")
                || value.startsWith(UploadConfig.UPLOAD_URL_PREFIX + "/")) {
            return value;
        }

        return UploadConfig.toPolicyUrl(value);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
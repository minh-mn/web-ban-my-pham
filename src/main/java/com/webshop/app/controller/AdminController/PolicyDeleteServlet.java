package com.webshop.app.controller.AdminController;

import com.webshop.app.config.UploadConfig;
import com.webshop.app.utils.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet({
        "/admin/policy/delete",
        "/admin/policies/delete"
})
public class PolicyDeleteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {
        super.init();
        UploadConfig.ensureUploadDirectories();
    }

    /*
     * Cho phép GET để nếu JSP cũ đang dùng link xóa dạng:
     * /admin/policy/delete?id=...
     * vẫn chạy được.
     *
     * Nếu bạn muốn an toàn hơn, sau này có thể bỏ doGet
     * và chỉ cho xóa bằng POST + CSRF.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        int id = safeParseInt(req.getParameter("id"), -1);

        if (id <= 0) {
            redirectBack(req, resp, "delete=invalid");
            return;
        }

        try {
            PolicyFile policyFile = findPolicyFileById(id);

            if (policyFile == null) {
                redirectBack(req, resp, "delete=not_found");
                return;
            }

            /*
             * Xóa SQL trước.
             * Nếu SQL lỗi thì không xóa file vật lý.
             */
            boolean deletedSql = deletePolicyById(id);

            if (deletedSql) {
                /*
                 * Sau khi SQL xóa thành công mới xóa file thật.
                 */
                deletePolicyPhysicalFile(policyFile.fileName);
                redirectBack(req, resp, "delete=success");
                return;
            }

            redirectBack(req, resp, "delete=not_found");

        } catch (Exception e) {
            throw new ServletException("PolicyDeleteServlet error", e);
        }
    }

    private PolicyFile findPolicyFileById(int id) {
        String sql =
                "SELECT id, file_name " +
                        "FROM policies " +
                        "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                PolicyFile policyFile = new PolicyFile();
                policyFile.id = rs.getInt("id");
                policyFile.fileName = rs.getString("file_name");

                return policyFile;
            }

        } catch (Exception e) {
            throw new RuntimeException("PolicyDeleteServlet.findPolicyFileById error", e);
        }
    }

    private boolean deletePolicyById(int id) {
        String sql =
                "DELETE FROM policies " +
                        "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("PolicyDeleteServlet.deletePolicyById error", e);
        }
    }

    private void deletePolicyPhysicalFile(String fileNameOrUrl) {
        String value = safe(fileNameOrUrl);

        if (value.isBlank()) {
            return;
        }

        /*
         * Trường hợp DB lưu dạng:
         * /uploads/policy/abc.pdf
         */
        if (value.startsWith(UploadConfig.POLICY_URL_PREFIX)) {
            UploadConfig.deletePolicyFileByUrl(value);
            return;
        }

        /*
         * Trường hợp DB lưu thiếu dấu /:
         * uploads/policy/abc.pdf
         */
        if (value.startsWith("uploads/policy/")) {
            UploadConfig.deletePolicyFileByUrl("/" + value);
            return;
        }

        /*
         * Trường hợp DB đang lưu file_name thuần:
         * abc.pdf
         * cancel_policy.jsp
         *
         * Hàm này chỉ xóa trong MyCosmeticShopUploads/policy,
         * không xóa nhầm file trong source webapp.
         */
        UploadConfig.deletePolicyFile(value);
    }

    private void redirectBack(HttpServletRequest req,
                              HttpServletResponse resp,
                              String query)
            throws IOException {

        String contextPath = req.getContextPath();
        String referer = req.getHeader("Referer");

        if (referer != null && !referer.isBlank()) {
            String separator = referer.contains("?") ? "&" : "?";
            resp.sendRedirect(referer + separator + query);
            return;
        }

        /*
         * Fallback nếu không có Referer.
         * Nếu project của bạn đang dùng URL khác cho trang policy list,
         * chỉ cần đổi dòng này.
         */
        resp.sendRedirect(contextPath + "/admin/policies?" + query);
    }

    private int safeParseInt(String value, int fallback) {
        try {
            if (value == null || value.isBlank()) {
                return fallback;
            }

            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static final class PolicyFile {
        private int id;
        private String fileName;
    }
}
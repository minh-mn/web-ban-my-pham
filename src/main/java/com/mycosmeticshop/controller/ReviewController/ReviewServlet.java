package com.mycosmeticshop.controller.ReviewController;

import com.mycosmeticshop.dao.ReviewDAO;
import com.mycosmeticshop.model.Review;
import com.mycosmeticshop.model.User;
import com.mycosmeticshop.utils.DBConnection;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/*
 * Servlet xử lý chức năng gửi đánh giá sản phẩm
 * URL truy cập: /review
 *
 * Chức năng:
 * - Kiểm tra người dùng đã đăng nhập hay chưa
 * - Nhận dữ liệu đánh giá từ form
 * - Xác định authorId hợp lệ từ session hoặc resolve bổ sung
 * - Tạo mới hoặc cập nhật review nếu người dùng đã đánh giá trước đó
 * - Redirect về lại trang chi tiết sản phẩm
 */
@WebServlet("/review")
public class ReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAO dùng để thao tác dữ liệu review
    private final ReviewDAO reviewDAO = new ReviewDAO();

    /*
     * Phương thức POST
     * Xử lý gửi đánh giá sản phẩm
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập encoding UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // =====================================================
        // 1) KIỂM TRA ĐĂNG NHẬP
        // =====================================================
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // =====================================================
        // 2) ĐỌC DỮ LIỆU TỪ REQUEST
        // =====================================================
        String productIdRaw = req.getParameter("productId");
        String slug = req.getParameter("slug");
        String ratingRaw = req.getParameter("rating");
        String comment = req.getParameter("comment");

        int productId = parseIntOrDefault(productIdRaw, -1);
        int rating = parseIntOrDefault(ratingRaw, 5);

        // productId không hợp lệ -> quay về trang sản phẩm
        if (productId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        // Chuẩn hóa rating về khoảng 1..5
        if (rating < 1) {
            rating = 1;
        }
        if (rating > 5) {
            rating = 5;
        }

        // Chuẩn hóa comment
        if (comment == null) {
            comment = "";
        }
        comment = comment.trim();

        // =====================================================
        // 3) XÁC ĐỊNH authorId HỢP LỆ
        // =====================================================
        /*
         * FIX QUAN TRỌNG:
         * - Ưu tiên dùng user.getId() từ session hiện tại
         * - Tuyệt đối không sửa lại session user tại đây
         * - Không được set user.setId(...) vì có thể gây nhảy tài khoản / role
         */
        int authorId = user.getId();

        // Nếu id trong session hợp lệ và tồn tại trong bảng auth_user -> dùng luôn
        if (authorId > 0 && existsAuthUserId(authorId)) {
            // OK, không cần xử lý thêm
        } else {
            /*
             * Fallback:
             * thử resolve authorId bằng username / email
             * nhưng KHÔNG ghi đè lại session user
             */
            Integer resolved = resolveAuthUserIdNoSessionOverwrite(user);

            if (resolved == null || resolved <= 0) {
                /*
                 * Không resolve được -> bắt đăng nhập lại
                 * để đảm bảo FK và danh tính người đánh giá chính xác
                 */
                if (session != null) {
                    session.removeAttribute("user");
                }
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            // Chỉ dùng authorId đã resolve cho lần review này
            authorId = resolved;
        }

        // =====================================================
        // 4) TẠO ĐỐI TƯỢNG REVIEW
        // =====================================================
        Review r = new Review();
        r.setProductId(productId);
        r.setAuthorId(authorId);
        r.setRating(rating);
        r.setComment(comment);

        /*
         * Giá trị mặc định bổ sung
         * Có thể dùng cho các feature phân tích cảm xúc / emoji về sau
         */
        r.setHasEmoji(false);
        r.setSentiment(1);

        // =====================================================
        // 5) LƯU REVIEW
        // =====================================================
        /*
         * Nếu người dùng đã đánh giá sản phẩm này rồi -> update
         * Nếu chưa có -> insert mới
         */
        reviewDAO.createOrUpdate(r);

        // =====================================================
        // 6) REDIRECT VỀ ĐÚNG TRANG CHI TIẾT SẢN PHẨM
        // =====================================================
        if (slug != null && !slug.isBlank()) {
            String encodedSlug = URLEncoder.encode(slug, StandardCharsets.UTF_8.name());
            resp.sendRedirect(req.getContextPath() + "/product/" + encodedSlug);
        } else {
            resp.sendRedirect(req.getContextPath() + "/products");
        }
    }

    /*
     * Resolve authorId hợp lệ nhưng KHÔNG ghi đè session
     *
     * Thứ tự:
     * 1. Tìm theo username
     * 2. Nếu không có thì tìm theo email
     */
    private Integer resolveAuthUserIdNoSessionOverwrite(User user) {

        // 1) Thử tìm theo username
        String username = safeString(getUsernameSafe(user));
        if (!username.isEmpty()) {
            Integer found = findAuthUserIdByUsername(username);
            if (found != null && found > 0) {
                return found;
            }
        }

        // 2) Thử tìm theo email
        String email = safeString(getEmailSafe(user));
        if (!email.isEmpty()) {
            Integer found = findAuthUserIdByEmail(email);
            if (found != null && found > 0) {
                return found;
            }
        }

        return null;
    }

    /*
     * Kiểm tra id người dùng có tồn tại trong bảng auth_user hay không
     */
    private boolean existsAuthUserId(int id) {
        String sql = "SELECT 1 FROM dbo.auth_user WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            return false;
        }
    }

    /*
     * Tìm auth_user.id theo username
     */
    private Integer findAuthUserIdByUsername(String username) {
        // Nếu DB không dùng cột username thì đổi lại câu SQL cho phù hợp
        String sql = "SELECT id FROM dbo.auth_user WHERE username = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return rs.getInt(1);
            }

        } catch (Exception e) {
            return null;
        }
    }

    /*
     * Tìm auth_user.id theo email
     */
    private Integer findAuthUserIdByEmail(String email) {
        String sql = "SELECT id FROM dbo.auth_user WHERE email = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return rs.getInt(1);
            }

        } catch (Exception e) {
            return null;
        }
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /*
     * Parse String -> int
     * Nếu lỗi thì trả về giá trị mặc định
     */
    private int parseIntOrDefault(String s, int def) {
        try {
            if (s == null || s.isBlank()) {
                return def;
            }
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    /*
     * Trả về chuỗi an toàn, không null
     */
    private String safeString(String s) {
        return (s == null) ? "" : s.trim();
    }

    /*
     * Lấy username an toàn từ đối tượng User
     */
    private String getUsernameSafe(User user) {
        try {
            return user.getUsername();
        } catch (Exception e) {
            return null;
        }
    }

    /*
     * Lấy email an toàn từ đối tượng User
     */
    private String getEmailSafe(User user) {
        try {
            return user.getEmail();
        } catch (Exception e) {
            return null;
        }
    }
}
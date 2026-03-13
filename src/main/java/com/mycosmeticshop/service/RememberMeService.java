package com.mycosmeticshop.service;

import com.mycosmeticshop.dao.RememberTokenDAO;
import com.mycosmeticshop.dao.UserDAO;
import com.mycosmeticshop.model.User;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/*
 * Service xử lý chức năng "Remember Me"
 *
 * Chức năng:
 * - Tạo token ghi nhớ đăng nhập
 * - Lưu token vào database
 * - Gắn token vào cookie trên trình duyệt
 * - Xác thực người dùng bằng token
 * - Thu hồi token khi logout hoặc token không hợp lệ
 *
 * Lưu ý:
 * - Cookie chỉ lưu token ngẫu nhiên, không lưu trực tiếp thông tin user
 * - Token được đối chiếu với database để lấy user tương ứng
 */
public class RememberMeService {

    // Tên cookie dùng để lưu token remember-me
    public static final String COOKIE_NAME = "REMEMBER_ME";

    // Thời gian sống của cookie: 14 ngày
    private static final int COOKIE_AGE_SECONDS = 60 * 60 * 24 * 14;

    // Số byte ngẫu nhiên để sinh token
    private static final int TOKEN_BYTES = 32;

    // DAO thao tác với bảng remember token
    private final RememberTokenDAO rememberTokenDAO = new RememberTokenDAO();

    // DAO thao tác với dữ liệu user
    private final UserDAO userDAO = new UserDAO();

    // Bộ sinh số ngẫu nhiên an toàn
    private final SecureRandom random = new SecureRandom();

    /*
     * Xác thực người dùng bằng token
     *
     * Quy trình:
     * 1. Tìm userId theo token trong database
     * 2. Tải user theo userId
     * 3. Kiểm tra user tồn tại, id hợp lệ, trạng thái active
     *
     * @param token token lấy từ cookie
     * @return User nếu hợp lệ, ngược lại trả về null
     */
    public User authenticateByToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            Integer userId = rememberTokenDAO.findUserIdByToken(token);
            if (userId == null || userId <= 0) {
                return null;
            }

            User user = userDAO.findById(userId);

            /*
             * Guard an toàn:
             * - user phải tồn tại
             * - user.id phải đúng với userId lấy từ token
             */
            if (user == null) {
                return null;
            }

            if (user.getId() <= 0 || user.getId() != userId) {
                System.out.println(
                        "[RememberMeService] INVALID user mapping. tokenUserId="
                                + userId + ", loadedUserId=" + user.getId()
                );
                return null;
            }

            // User phải đang active
            if (!user.isActive()) {
                return null;
            }

            return user;

        } catch (Exception e) {
            System.out.println("[RememberMeService] authenticateByToken error: " + e.getMessage());
            return null;
        }
    }

    /*
     * Tạo token ngẫu nhiên và lưu vào database
     *
     * @param userId id người dùng
     * @return token vừa tạo
     */
    public String issueToken(int userId) {
        String token = generateToken();

        // Thời điểm hết hạn token = hiện tại + 14 ngày
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(COOKIE_AGE_SECONDS);

        // Lưu token vào database
        rememberTokenDAO.saveToken(userId, token, expiresAt);

        return token;
    }

    /*
     * Dùng khi đăng nhập có tick "Remember Me"
     *
     * Chức năng:
     * - tạo token
     * - lưu token vào DB
     * - set cookie theo contextPath của ứng dụng
     *
     * @param req    request hiện tại
     * @param resp   response hiện tại
     * @param userId id người dùng
     */
    public void rememberLogin(HttpServletRequest req, HttpServletResponse resp, int userId) {
        String token = issueToken(userId);
        setCookie(req, resp, token);
    }

    /*
     * Phiên bản tương thích ngược
     *
     * Nếu chưa truyền được HttpServletRequest thì vẫn dùng được,
     * nhưng cookie sẽ có path = "/"
     *
     * @param resp   response hiện tại
     * @param userId id người dùng
     */
    public void rememberLogin(HttpServletResponse resp, int userId) {
        String token = issueToken(userId);
        setCookie(resp, token);
    }

    /*
     * Logout chuẩn:
     * - thu hồi token trong database
     * - xóa cookie ở client
     *
     * @param req  request hiện tại
     * @param resp response hiện tại
     */
    public void logoutByRequest(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String token = getCookieValue(req, COOKIE_NAME);

            if (token != null && !token.isBlank()) {
                rememberTokenDAO.revokeToken(token);
            }

        } catch (Exception e) {
            System.out.println("[RememberMeService] logoutByRequest error: " + e.getMessage());
        } finally {
            clearCookie(resp, req.getContextPath());
        }
    }

    /*
     * Khi token không hợp lệ:
     * - thu hồi token trong DB
     * - xóa cookie ở client
     *
     * @param req   request hiện tại
     * @param resp  response hiện tại
     * @param token token cần thu hồi
     */
    public void invalidateToken(HttpServletRequest req, HttpServletResponse resp, String token) {
        try {
            if (token != null && !token.isBlank()) {
                rememberTokenDAO.revokeToken(token);
            }
        } catch (Exception e) {
            System.out.println("[RememberMeService] invalidateToken error: " + e.getMessage());
        } finally {
            clearCookie(resp, req.getContextPath());
        }
    }

    // =====================================================
    // COOKIE METHODS
    // =====================================================

    /*
     * Xóa cookie chắc chắn:
     * - xóa cookie với path "/"
     * - xóa cookie với path = contextPath
     *
     * Lý do:
     * cookie trước đây có thể đã được tạo với các path khác nhau
     */
    public void clearCookie(HttpServletResponse resp, String contextPath) {

        // Xóa cookie với path "/"
        Cookie c1 = new Cookie(COOKIE_NAME, "");
        c1.setPath("/");
        c1.setMaxAge(0);
        c1.setHttpOnly(true);
        resp.addCookie(c1);

        // Xóa cookie với path = contextPath
        Cookie c2 = new Cookie(
                COOKIE_NAME,
                ""
        );
        c2.setPath((contextPath == null || contextPath.isEmpty()) ? "/" : contextPath);
        c2.setMaxAge(0);
        c2.setHttpOnly(true);
        resp.addCookie(c2);
    }

    /*
     * Phiên bản tương thích ngược:
     * chỉ xóa cookie với path "/"
     */
    public void clearCookie(HttpServletResponse resp) {
        Cookie c = new Cookie(COOKIE_NAME, "");
        c.setPath("/");
        c.setMaxAge(0);
        c.setHttpOnly(true);
        resp.addCookie(c);
    }

    /*
     * Set cookie remember-me theo contextPath của ứng dụng
     *
     * @param req   request hiện tại
     * @param resp  response hiện tại
     * @param token token cần lưu vào cookie
     */
    public void setCookie(HttpServletRequest req, HttpServletResponse resp, String token) {
        String path = (req.getContextPath() == null || req.getContextPath().isEmpty())
                ? "/"
                : req.getContextPath();

        Cookie c = new Cookie(COOKIE_NAME, token);
        c.setPath(path);
        c.setMaxAge(COOKIE_AGE_SECONDS);
        c.setHttpOnly(true);

        resp.addCookie(c);
    }

    /*
     * Phiên bản tương thích ngược:
     * set cookie với path = "/"
     *
     * @param resp  response hiện tại
     * @param token token cần lưu
     */
    public void setCookie(HttpServletResponse resp, String token) {
        Cookie c = new Cookie(COOKIE_NAME, token);
        c.setPath("/");
        c.setMaxAge(COOKIE_AGE_SECONDS);
        c.setHttpOnly(true);

        resp.addCookie(c);
    }

    // =====================================================
    // INTERNAL METHODS
    // =====================================================

    /*
     * Sinh token ngẫu nhiên URL-safe
     *
     * Cách làm:
     * - sinh mảng byte ngẫu nhiên
     * - encode Base64 URL-safe
     * - bỏ padding để token gọn hơn
     */
    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    /*
     * Lấy giá trị cookie theo tên
     *
     * @param req  request hiện tại
     * @param name tên cookie
     * @return giá trị cookie hoặc null nếu không tồn tại
     */
    private String getCookieValue(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }

        return null;
    }
}
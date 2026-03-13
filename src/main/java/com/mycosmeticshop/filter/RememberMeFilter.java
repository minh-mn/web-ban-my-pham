package com.mycosmeticshop.filter;

import com.mycosmeticshop.model.User;
import com.mycosmeticshop.service.RememberMeService;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/*
 * Filter xử lý tự động đăng nhập bằng cookie Remember Me
 *
 * Chức năng:
 * - Bỏ qua static resource
 * - Bỏ qua request API / JSON nếu cần
 * - Nếu session chưa có user thì thử xác thực bằng cookie REMEMBER_ME
 * - Nếu token hợp lệ -> khôi phục session user
 * - Nếu token không hợp lệ -> xóa cookie
 */
public class RememberMeFilter implements Filter {

    // Service xử lý logic remember-me
    private RememberMeService rememberMeService;

    /*
     * Khởi tạo filter
     * Tạo instance RememberMeService để dùng trong suốt vòng đời filter
     */
    @Override
    public void init(FilterConfig filterConfig) {
        this.rememberMeService = new RememberMeService();
    }

    /*
     * Xử lý request đi qua filter
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // =====================================================
        // 1) XÁC ĐỊNH PATH HIỆN TẠI
        // =====================================================
        String ctx = req.getContextPath();
        String path = req.getRequestURI().substring(ctx.length());

        // =====================================================
        // 2) BỎ QUA STATIC RESOURCE
        // =====================================================
        /*
         * Ví dụ:
         * - /assets/css/...
         * - /assets/js/...
         * - /uploads/...
         * - /images/...
         *
         * Không cần kiểm tra remember-me với tài nguyên tĩnh
         */
        if (StaticResourceUtil.isStatic(path)) {
            chain.doFilter(request, response);
            return;
        }

        // =====================================================
        // 3) BỎ QUA REQUEST JSON / API NẾU CẦN
        // =====================================================
        String accept = req.getHeader("Accept");

        if (accept != null && accept.contains("application/json")) {
            chain.doFilter(request, response);
            return;
        }

        // =====================================================
        // 4) KIỂM TRA USER TRONG SESSION
        // =====================================================
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // =====================================================
        // 5) NẾU CHƯA CÓ SESSION USER -> THỬ XÁC THỰC BẰNG COOKIE
        // =====================================================
        if (user == null) {

            String token = getCookieValue(req, RememberMeService.COOKIE_NAME);

            if (token != null && !token.isBlank()) {
                try {
                    // Xác thực token remember-me
                    User u = rememberMeService.authenticateByToken(token);

                    if (u != null) {
                        /*
                         * Token hợp lệ:
                         * khôi phục session user
                         */
                        req.getSession(true).setAttribute("user", u);

                    } else {
                        /*
                         * Token không hợp lệ / hết hạn:
                         * xóa cookie để tránh request sau tiếp tục dùng token hỏng
                         */
                        rememberMeService.clearCookie(resp);
                    }

                } catch (Exception ex) {
                    /*
                     * Nếu lỗi hệ thống tạm thời:
                     * - chỉ log
                     * - không xóa cookie ngay
                     * vì có thể đây không phải lỗi token mà là lỗi DB / service
                     */
                    System.out.println("[RememberMeFilter] token auth error: " + ex.getMessage());
                }
            }
        }

        // =====================================================
        // 6) CHO REQUEST ĐI TIẾP
        // =====================================================
        chain.doFilter(request, response);
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
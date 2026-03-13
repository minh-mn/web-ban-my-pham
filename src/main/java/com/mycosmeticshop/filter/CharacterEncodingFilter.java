package com.mycosmeticshop.filter;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/*
 * Filter xử lý mã hóa ký tự cho request / response
 *
 * Chức năng:
 * - Thiết lập character encoding cho request
 * - Thiết lập character encoding cho response
 * - Giúp hiển thị đúng tiếng Việt trong form, query và nội dung trả về
 *
 * Lưu ý:
 * - Không set Content-Type tại đây
 * - Content-Type nên được set ở Servlet hoặc JSP tương ứng
 */
public class CharacterEncodingFilter implements Filter {

    // Encoding mặc định của ứng dụng
    private String encoding = "UTF-8";

    /*
     * Khởi tạo filter
     *
     * Nếu trong web.xml có truyền init-param "encoding"
     * thì dùng giá trị đó thay cho mặc định UTF-8.
     */
    @Override
    public void init(FilterConfig filterConfig) {
        String enc = filterConfig.getInitParameter("encoding");

        if (enc != null && !enc.isBlank()) {
            encoding = enc;
        }
    }

    /*
     * Thiết lập encoding cho request và response
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        /*
         * Chỉ set character encoding
         * Không set Content-Type ở đây để tránh ảnh hưởng
         * đến các response đặc thù như file, json, image,...
         */
        request.setCharacterEncoding(encoding);
        response.setCharacterEncoding(encoding);

        /*
         * Optional:
         * Có thể bỏ qua static resource ngay tại filter này nếu muốn.
         * Ví dụ:
         *
         * HttpServletRequest req = (HttpServletRequest) request;
         * String ctx = req.getContextPath();
         * String path = req.getRequestURI().substring(ctx.length());
         *
         * if (StaticResourceUtil.isStatic(path)) {
         *     chain.doFilter(request, response);
         *     return;
         * }
         */

        // Chuyển request/response sang filter hoặc servlet tiếp theo
        chain.doFilter(request, response);
    }
}
package com.webshop.app.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public class CharacterEncodingFilter implements Filter {

    private String encoding = "UTF-8";

    
    public void init(FilterConfig filterConfig) {
        String enc = filterConfig.getInitParameter("encoding");
        if (enc != null && !enc.isBlank()) encoding = enc;
    }

    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // ✅ KHÔNG set Content-Type ở đây
        request.setCharacterEncoding(encoding);
        response.setCharacterEncoding(encoding);

        // Optional: nếu bạn muốn bỏ qua static ngay từ đây cũng được (không bắt buộc)
        // HttpServletRequest req = (HttpServletRequest) request;
        // String ctx = req.getContextPath();
        // String path = req.getRequestURI().substring(ctx.length());
        // if (StaticResourceUtil.isStatic(path)) { chain.doFilter(request,response); return; }

        chain.doFilter(request, response);
    }

	
	public boolean test(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}

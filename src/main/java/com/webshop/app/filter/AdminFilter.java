package com.webshop.app.filter;

import com.webshop.app.model.User;
import com.webshop.app.service.PermissionService;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AdminFilter implements Filter {

    private final PermissionService permissionService = new PermissionService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Set<String> permissions = permissionService.getPermissions(user);
        boolean isSuperAdmin = permissionService.isSuperAdmin(user);

        session.setAttribute("adminPermissions", permissions);
        session.setAttribute("isSuperAdmin", isSuperAdmin);
        session.setAttribute("adminRoleCode", user.getRole());

        if (!isSuperAdmin && !permissionService.canAccessAdmin(user)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Tài khoản này chưa được cấp quyền truy cập khu vực quản trị.");
            return;
        }

        String requiredPermission = permissionService.resolveRequiredPermission(req);
        if (!isSuperAdmin && requiredPermission != null && !permissions.contains(requiredPermission)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập chức năng này: " + requiredPermission);
            return;
        }

        chain.doFilter(request, response);
    }
}

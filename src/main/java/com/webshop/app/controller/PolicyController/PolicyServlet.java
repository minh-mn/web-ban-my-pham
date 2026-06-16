package com.webshop.app.controller.PolicyController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * PolicyServlet
 * Xử lý toàn bộ trang chính sách của MyCosmetic.
 */
@WebServlet("/policy/*")
public class PolicyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();

        if (path == null || path.isBlank() || "/".equals(path)) {
            response.sendRedirect(request.getContextPath() + "/policy/cancel");
            return;
        }

        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String view;

        switch (path) {
            case "/cancel":
                view = "/jsp/policy/cancel-policy.jsp";
                break;

            case "/return":
                view = "/jsp/policy/return-policy.jsp";
                break;

            case "/privacy":
                view = "/jsp/policy/privacy-policy.jsp";
                break;

            case "/payment":
                view = "/jsp/policy/payment-policy.jsp";
                break;

            case "/terms":
                view = "/jsp/policy/terms-policy.jsp";
                break;

            case "/shopping-guide":
                view = "/jsp/policy/shopping-guide.jsp";
                break;

            case "/vnpay-guide":
                view = "/jsp/policy/vnpay-guide.jsp";
                break;

            case "/shipping":
                view = "/jsp/policy/shipping-policy.jsp";
                break;

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
        }

        request.getRequestDispatcher(view).forward(request, response);
    }
}

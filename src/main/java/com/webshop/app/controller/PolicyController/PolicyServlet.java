package com.webshop.app.controller.PolicyController;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/policy/*")
public class PolicyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        String path = pathInfo == null ? "" : pathInfo.trim().toLowerCase();

        switch (path) {
            case "/cancel", "/huy-don" -> forward(
                    request,
                    response,
                    "MyCosmetic | Chính sách hủy đơn",
                    "/jsp/policy/cancel_policy.jsp"
            );
            case "/return", "/hoan-hang" -> forward(
                    request,
                    response,
                    "MyCosmetic | Chính sách hoàn hàng",
                    "/jsp/policy/return_policy.jsp"
            );
            default -> response.sendRedirect(request.getContextPath() + "/policy/return");
        }
    }

    private void forward(HttpServletRequest request,
                         HttpServletResponse response,
                         String title,
                         String content) throws ServletException, IOException {
        request.setAttribute("pageTitle", title);
        request.setAttribute("pageCss", "/order.css");
        request.setAttribute("pageContent", content);
        request.getRequestDispatcher("/jsp/common/base.jsp").forward(request, response);
    }
}

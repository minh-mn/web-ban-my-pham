package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.PolicyDAO;
import com.webshop.app.model.Policy;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
@WebServlet("/policy-detail")
public class PolicyController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Lấy slug từ link ở Footer
        String slug = request.getParameter("slug");

        // 2. Gọi DAO để lấy thông tin chính sách từ Database
        PolicyDAO dao = new PolicyDAO();
        Policy p = dao.getPolicyBySlug(slug);

        if (p != null) {
            // 3. Gửi tên file và tiêu đề sang trang JSP
            request.setAttribute("fileName", p.getFileName());
            request.setAttribute("policyTitle", p.getTitle());

            // 4. Mở trang policy-detail.jsp
            request.getRequestDispatcher("/WEB-INF/jsp/policy-detail.jsp").forward(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/home"); // Nếu không thấy thì về trang chủ
        }
    }
}

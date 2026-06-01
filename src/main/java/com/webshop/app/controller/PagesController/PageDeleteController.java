package com.webshop.app.controller.PagesController;

import com.webshop.app.dao.PageDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/pages/delete") // Đường dẫn URL xử lý hành động xóa
public class PageDeleteController extends HttpServlet {

    private PageDAO dao = new PageDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Lấy ID của trang cần xóa gửi lên từ Form
        String idStr = req.getParameter("id");

        if (idStr != null && !idStr.isEmpty()) {
            int id = Integer.parseInt(idStr);
            // 2. Gọi DAO thực hiện xóa dữ liệu trong DB
            dao.delete(id);
        }

        // 3. Xóa xong, điều hướng quay trở lại trang danh sách quản lý
        resp.sendRedirect(req.getContextPath() + "/admin/pages");
    }
}
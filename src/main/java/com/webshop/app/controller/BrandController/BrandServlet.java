package com.webshop.app.controller.BrandController;

import java.io.IOException;
import java.util.List;

import com.webshop.app.dao.BrandDAO;
import com.webshop.app.model.Brand;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/brands")
public class BrandServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final BrandDAO brandDAO = new BrandDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Lấy danh sách thương hiệu kèm số lượng sản phẩm để hiển thị lên lưới
        List<Brand> brands = brandDAO.findAllWithProductCount();

        req.setAttribute("brands", brands);
        req.setAttribute("pageTitle", "MyCosmetic | Thương hiệu chính hãng");

        // Nhúng file giao diện thương hiệu vào baseLayout chung của dự án
        req.setAttribute("pageContent", "/jsp/brand/brand_grid.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}

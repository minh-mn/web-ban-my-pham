package com.mycosmeticshop.controller.AdminController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin")
public class AdminHomeServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

        /* ======================================================
           TẮT CACHE CHO TRANG ADMIN
           Tránh trường hợp logout rồi nhưng bấm Back vẫn vào được
           ====================================================== */

		resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		resp.setHeader("Pragma", "no-cache");
		resp.setDateHeader("Expires", 0);

        /* ======================================================
           TRANG HUB ĐIỀU HƯỚNG ADMIN
           Đây là trang trung tâm dẫn tới các chức năng:
           - Dashboard
           - Products
           - Orders
           - Users
           - Coupons
           - Categories
           ====================================================== */

		req.getRequestDispatcher("/jsp/admin/admin-center.jsp")
				.forward(req, resp);
	}
}
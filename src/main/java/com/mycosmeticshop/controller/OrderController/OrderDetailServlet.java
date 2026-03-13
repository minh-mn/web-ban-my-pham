package com.mycosmeticshop.controller.OrderController;

import com.mycosmeticshop.dao.OrderDAO;
import com.mycosmeticshop.dao.OrderItemDAO;
import com.mycosmeticshop.model.Order;
import com.mycosmeticshop.model.OrderItem;
import com.mycosmeticshop.model.User;
import com.mycosmeticshop.utils.OrderStatusUtils;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/*
 * Servlet hiển thị chi tiết đơn hàng
 * URL truy cập: /orders/detail
 *
 * Chức năng:
 * - Kiểm tra người dùng đã đăng nhập hay chưa
 * - Đọc mã đơn hàng từ request
 * - Tải thông tin đơn hàng và danh sách sản phẩm trong đơn
 * - Kiểm tra quyền xem đơn hàng
 * - Gửi dữ liệu sang JSP để hiển thị
 */
@WebServlet("/orders/detail")
public class OrderDetailServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	// DAO dùng để thao tác dữ liệu đơn hàng
	private final OrderDAO orderDAO = new OrderDAO();

	// DAO dùng để lấy danh sách item của đơn hàng
	private final OrderItemDAO itemDAO = new OrderItemDAO();

	/*
	 * Phương thức GET
	 * Hiển thị chi tiết đơn hàng
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// =====================================================
		// 1) KIỂM TRA ĐĂNG NHẬP
		// =====================================================
		HttpSession session = req.getSession(false);
		User user = (session != null) ? (User) session.getAttribute("user") : null;

		// Nếu chưa đăng nhập -> chuyển về trang login
		if (user == null) {
			resp.sendRedirect(req.getContextPath() + "/login");
			return;
		}

		// =====================================================
		// 2) LẤY orderId TỪ REQUEST
		// =====================================================
		String idRaw = req.getParameter("id");

		// Nếu không có id -> quay về trang danh sách đơn hàng
		if (idRaw == null) {
			resp.sendRedirect(req.getContextPath() + "/orders");
			return;
		}

		int orderId;
		try {
			orderId = Integer.parseInt(idRaw);
		} catch (NumberFormatException e) {
			// Nếu id không hợp lệ -> quay về trang danh sách đơn hàng
			resp.sendRedirect(req.getContextPath() + "/orders");
			return;
		}

		// =====================================================
		// 3) TẢI ĐƠN HÀNG TỪ DATABASE
		// =====================================================
		Order order = orderDAO.findById(orderId);

		// Nếu không tìm thấy đơn hàng -> quay lại trang danh sách
		if (order == null) {
			resp.sendRedirect(req.getContextPath() + "/orders");
			return;
		}

		// =====================================================
		// 4) KIỂM TRA QUYỀN XEM ĐƠN HÀNG
		// =====================================================
		/*
		 * Người dùng thường chỉ được xem đơn hàng của chính họ.
		 * Nếu là admin thì có thể xem mọi đơn hàng.
		 */
		if (!user.isAdmin() && order.getUserId() != user.getId()) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		// =====================================================
		// 5) TẢI DANH SÁCH SẢN PHẨM TRONG ĐƠN HÀNG
		// =====================================================
		List<OrderItem> items = itemDAO.findByOrderId(orderId);

		// =====================================================
		// 6) CHUẨN HÓA DỮ LIỆU HIỂN THỊ
		// =====================================================
		// Chuyển mã trạng thái đơn hàng sang nhãn dễ đọc
		order.setStatusLabel(OrderStatusUtils.toLabel(order.getStatus()));

		// Gửi dữ liệu sang JSP
		req.setAttribute("order", order);
		req.setAttribute("orderItems", items);

		// =====================================================
		// 7) THIẾT LẬP THÔNG TIN TRANG
		// =====================================================
		req.setAttribute("pageTitle", "MyCosmetic | Chi tiết đơn hàng #" + order.getId());
		req.setAttribute("pageContent", "/jsp/order/order_detail.jsp");

		// =====================================================
		// 8) RENDER QUA BASE LAYOUT
		// =====================================================
		req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
	}
}
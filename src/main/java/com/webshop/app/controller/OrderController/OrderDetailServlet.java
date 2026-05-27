package com.webshop.app.controller.OrderController;

import java.io.IOException;
import java.util.List;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.dao.OrderItemDAO;
import com.webshop.app.dao.ReturnRequestDAO;
import com.webshop.app.dao.ReviewDAO;
import com.webshop.app.model.Order;
import com.webshop.app.model.OrderItem;
import com.webshop.app.model.User;
import com.webshop.app.utils.OrderStatusUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/orders/detail")
public class OrderDetailServlet extends HttpServlet {

	private final OrderDAO orderDAO = new OrderDAO();
	private final OrderItemDAO itemDAO = new OrderItemDAO();
	private final ReturnRequestDAO returnRequestDAO = new ReturnRequestDAO();
	private final ReviewDAO reviewDAO = new ReviewDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		HttpSession session = req.getSession(false);
		User user = (session != null) ? (User) session.getAttribute("user") : null;

		if (user == null) {
			resp.sendRedirect(req.getContextPath() + "/login");
			return;
		}

		String idRaw = req.getParameter("id");
		if (idRaw == null) {
			resp.sendRedirect(req.getContextPath() + "/orders");
			return;
		}

		int orderId;
		try {
			orderId = Integer.parseInt(idRaw);
		} catch (NumberFormatException e) {
			resp.sendRedirect(req.getContextPath() + "/orders");
			return;
		}

		Order order = orderDAO.findById(orderId);
		if (order == null) {
			resp.sendRedirect(req.getContextPath() + "/orders");
			return;
		}

		if (!user.isAdmin() && order.getUserId() != user.getId()) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		List<OrderItem> items = itemDAO.findByOrderId(orderId);

		order.setStatusLabel(OrderStatusUtils.toLabel(order.getStatus()));
		req.setAttribute("order", order);
		req.setAttribute("orderItems", items);
		req.setAttribute("returnRequest", returnRequestDAO.findByOrderId(orderId));
		req.setAttribute("reviewedOrderItemMap", reviewDAO.findReviewedOrderItemMap(user.getId(), orderId));
		req.setAttribute("success", req.getParameter("success"));
		req.setAttribute("error", req.getParameter("error"));

		req.setAttribute("pageTitle", "MyCosmetic | Chi tiết đơn hàng #" + order.getId());
		req.setAttribute("pageContent", "/jsp/order/order_detail.jsp");

		req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
	}
}

package com.webshop.app.controller.OrderController;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import com.webshop.app.dao.CancelRequestDAO;
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
	private final CancelRequestDAO cancelRequestDAO = new CancelRequestDAO();
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

		orderDAO.autoCompleteUnconfirmedDeliveredOrders(7);

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
		req.setAttribute("cancelRequest", cancelRequestDAO.findByOrderId(orderId));
		req.setAttribute("returnRequest", returnRequestDAO.findByOrderId(orderId));

		// Các biến riêng cho JSP để tránh lỗi EL PropertyNotFoundException
		// khi Tomcat vẫn đang giữ class Order cũ trong artifact exploded.
		boolean customerReceivedConfirmed = invokeBoolean(order, "isCustomerReceivedConfirmed", false);
		boolean delivered = invokeBoolean(order, "isDelivered", false);
		boolean shippingCanceled = invokeBoolean(order, "isShippingCanceled", false);
		boolean receiveConfirmable = delivered && !customerReceivedConfirmed && !shippingCanceled;

		String receiveStatusLabel = invokeString(order, "getReceiveStatusLabel", null);
		if (receiveStatusLabel == null || receiveStatusLabel.isBlank()) {
			if (customerReceivedConfirmed) {
				receiveStatusLabel = "Khách hàng đã xác nhận đã nhận hàng";
			} else if (delivered) {
				receiveStatusLabel = "Chờ khách hàng xác nhận đã nhận hàng";
			} else {
				receiveStatusLabel = "Chưa giao thành công";
			}
		}

		req.setAttribute("customerReceivedConfirmed", customerReceivedConfirmed);
		req.setAttribute("receiveConfirmable", receiveConfirmable);
		req.setAttribute("receiveStatusLabel", receiveStatusLabel);
		req.setAttribute("customerReceivedAtDate", invokeDate(order, "getCustomerReceivedAtDate"));
		req.setAttribute("receiveConfirmNote", invokeString(order, "getReceiveConfirmNote", null));

		req.setAttribute("reviewedOrderItemMap", reviewDAO.findReviewedOrderItemMap(user.getId(), orderId));
		req.setAttribute("success", req.getParameter("success"));
		req.setAttribute("error", req.getParameter("error"));

		req.setAttribute("pageTitle", "MyCosmetic | Chi tiết đơn hàng #" + order.getId());
		req.setAttribute("pageContent", "/jsp/order/order_detail.jsp");

		req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
	}
	private static boolean invokeBoolean(Object target, String methodName, boolean defaultValue) {
		try {
			Method method = target.getClass().getMethod(methodName);
			Object value = method.invoke(target);
			return value instanceof Boolean ? (Boolean) value : defaultValue;
		} catch (Exception ignored) {
			return defaultValue;
		}
	}

	private static String invokeString(Object target, String methodName, String defaultValue) {
		try {
			Method method = target.getClass().getMethod(methodName);
			Object value = method.invoke(target);
			return value != null ? String.valueOf(value) : defaultValue;
		} catch (Exception ignored) {
			return defaultValue;
		}
	}

	private static Date invokeDate(Object target, String methodName) {
		try {
			Method method = target.getClass().getMethod(methodName);
			Object value = method.invoke(target);
			return value instanceof Date ? (Date) value : null;
		} catch (Exception ignored) {
			return null;
		}
	}
}

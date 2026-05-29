package com.webshop.app.controller.OrderController;

import java.io.IOException;
import java.util.List;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.model.Order;
import com.webshop.app.model.OrderStatus;
import com.webshop.app.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/orders")
public class OrderListServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        orderDAO.autoCompleteUnconfirmedDeliveredOrders(7);

        List<Order> orders = user.isAdmin()
                ? orderDAO.findAll()
                : orderDAO.findByUser(user.getId());

        req.setAttribute("orders", orders);

        // ✅ dòng này quyết định dropdown có ra label tiếng Việt hay không
        req.setAttribute("statusChoices", OrderStatus.choices());

        req.setAttribute("pageTitle", "MyCosmetic | Đơn hàng");
        req.setAttribute("pageCss", "/order.css");
        req.setAttribute("pageContent", "/jsp/order/order_list.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}

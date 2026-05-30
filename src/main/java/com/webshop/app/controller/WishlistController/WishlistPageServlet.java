package com.webshop.app.controller.WishlistController;

import com.webshop.app.dao.ProductDAO;
import com.webshop.app.dao.WishlistDAO;
import com.webshop.app.model.Product;
import com.webshop.app.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/wishlist")
public class WishlistPageServlet extends HttpServlet {

    private final WishlistDAO wishlistDAO = new WishlistDAO();
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        List<Integer> productIds = wishlistDAO.findProductIdsByUser(user.getId());

        List<Product> products = productDAO.findByIds(productIds);

        req.setAttribute("products", products);
        req.setAttribute("pageContent", "/jsp/wishlist/wishlist.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}

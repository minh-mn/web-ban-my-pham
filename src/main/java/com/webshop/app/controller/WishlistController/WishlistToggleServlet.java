package com.webshop.app.controller.WishlistController;

import java.io.IOException;

import com.webshop.app.dao.WishlistDAO;
import com.webshop.app.model.User;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/wishlist/toggle")
public class WishlistToggleServlet extends HttpServlet {

    private final WishlistDAO wishlistDAO = new WishlistDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.setStatus(401);
            resp.getWriter().write("LOGIN_REQUIRED");
            return;
        }

        int productId = Integer.parseInt(req.getParameter("productId"));

        boolean exists = wishlistDAO.exists(user.getId(), productId);

        if (exists) {
            wishlistDAO.remove(user.getId(), productId);
            resp.getWriter().write("REMOVED");
        } else {
            wishlistDAO.add(user.getId(), productId);
            resp.getWriter().write("ADDED");
        }
    }
}
package com.webshop.app.controller.AjaxController;

// ===== Java core =====
import java.io.IOException;
import java.util.List;

// ===== Project =====
import com.webshop.app.dao.ProductDAO;
import com.webshop.app.model.Product;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/ajax/search")
public class AjaxSearchServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String q = req.getParameter("q");

        resp.setContentType("application/json;charset=UTF-8");

        // Nếu chưa đủ ký tự → trả mảng rỗng
        if (q == null || q.trim().length() < 2) {
            resp.getWriter().write("[]");
            return;
        }

        List<Product> products = productDAO.findProducts(
            q,          // keyword
            null,       // category
            null,       // brand
            null,       // sort
            null,       // priceRange
            null        // minRating
        );

        // Build JSON thủ công (đủ dùng, không cần thư viện)
        StringBuilder json = new StringBuilder("[");
        for (Product p : products) {
            json.append("{")
                .append("\"id\":").append(p.getId()).append(",")
                .append("\"title\":\"")
                .append(p.getTitle().replace("\"", "\\\""))
                .append("\"")
                .append("},");
        }

        if (json.length() > 1) {
            json.setLength(json.length() - 1); // bỏ dấu ,
        }
        json.append("]");

        resp.getWriter().write(json.toString());
    }
}

package com.webshop.app.controller.ProductController;

import com.google.gson.Gson;
import com.webshop.app.dao.ProductDAO;
import com.webshop.app.model.Product;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/search-suggest")
public class SearchSuggestServlet extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String keyword = req.getParameter("q");

        if (keyword == null || keyword.trim().length() < 2) {
            resp.getWriter().write("[]");
            return;
        }

        try {
            List<Product> products = productDAO.searchByKeyword(keyword);

            List<Map<String, Object>> result = new ArrayList<>();

            for (Product p : products) {
                Map<String, Object> item = new HashMap<>();

                item.put("id", p.getId());
                item.put("title", p.getTitle());
                item.put("slug", p.getSlug());

                item.put("image", p.getImage());

                item.put("price", p.getPrice());

                result.add(item);
            }

            new Gson().toJson(result, resp.getWriter());

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"server error\"}");
        }
    }
}

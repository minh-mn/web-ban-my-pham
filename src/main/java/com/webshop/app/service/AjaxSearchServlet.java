package com.webshop.app.service;

import com.webshop.app.dao.ProductDAO;
import com.webshop.app.model.Product;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/ajax-search")
public class AjaxSearchServlet extends HttpServlet {

    private ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String q = req.getParameter("q");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (q == null || q.trim().isEmpty()) {
            resp.getWriter().write("{\"results\":[]}");
            return;
        }

        List<Product> list = productDAO.searchSuggestions(q.trim());

        StringBuilder json = new StringBuilder();
        json.append("{\"results\":[");

        for (int i = 0; i < list.size(); i++) {
            Product p = list.get(i);

            json.append("{")
                    .append("\"id\":").append(p.getId()).append(",")
                    .append("\"title\":\"").append(p.getTitle().replace("\"","")).append("\",")
                    .append("\"slug\":\"").append(p.getSlug()).append("\",")
                    .append("\"price\":").append(p.getPrice()).append(",")
                    .append("\"image\":\"").append(p.getImage()).append("\"")
                    .append("}");

            if (i < list.size() - 1) json.append(",");
        }

        json.append("]}");

        resp.getWriter().write(json.toString());
    }
}

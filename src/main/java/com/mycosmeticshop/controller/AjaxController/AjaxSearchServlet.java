package com.mycosmeticshop.controller.AjaxController;

// ===== Servlet API (Jakarta - dùng cho Tomcat 10+) =====
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// ===== Java core =====
import java.io.IOException;
import java.util.List;

// ===== Project =====
import com.mycosmeticshop.dao.ProductDAO;
import com.mycosmeticshop.model.Product;

@WebServlet("/ajax/search")
public class AjaxSearchServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAO dùng để truy vấn sản phẩm
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // Lấy keyword tìm kiếm
        String q = req.getParameter("q");

        // Trả dữ liệu JSON cho Ajax
        resp.setContentType("application/json;charset=UTF-8");

        /* ======================================================
           Nếu keyword < 2 ký tự thì không tìm kiếm
           trả về mảng rỗng
        ====================================================== */
        if (q == null || q.trim().length() < 2) {
            resp.getWriter().write("[]");
            return;
        }

        /* ======================================================
           Tìm sản phẩm theo keyword
           Các filter khác để null
        ====================================================== */
        List<Product> products = productDAO.findProducts(
                q,      // keyword
                null,   // category
                null,   // brand
                null,   // sort
                null,   // priceRange
                null    // minRating
        );

        /* ======================================================
           Build JSON thủ công (đơn giản, không cần thư viện)
        ====================================================== */
        StringBuilder json = new StringBuilder("[");

        for (Product p : products) {
            json.append("{")
                    .append("\"id\":").append(p.getId()).append(",")
                    .append("\"title\":\"")
                    .append(p.getTitle().replace("\"", "\\\""))
                    .append("\"")
                    .append("},");
        }

        // bỏ dấu phẩy cuối
        if (json.length() > 1) {
            json.setLength(json.length() - 1);
        }

        json.append("]");

        resp.getWriter().write(json.toString());
    }
}
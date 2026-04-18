package com.webshop.app.test;

import com.webshop.app.dao.ProductDAO;
import com.webshop.app.model.Product;

public class TestProductDAO {

    public static void main(String[] args) {
        ProductDAO dao = new ProductDAO();
        Product p = dao.findById(1);

        if (p != null) {
            System.out.println(p.getTitle());
        } else {
            System.out.println("Không tìm thấy sản phẩm");
        }
    }
}

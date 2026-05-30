package com.webshop.app.controller.CouponController;

import com.webshop.app.dao.CouponDAO;
import com.webshop.app.model.Coupon;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/vouchers")
public class VouchersController extends HttpServlet {

    private CouponDAO couponDAO;

    @Override
    public void init() throws ServletException {
        couponDAO = new CouponDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // 1. Lấy toàn bộ danh sách mã giảm giá đang hoạt động
        List<Coupon> allVouchers = couponDAO.findAllActiveCoupons();
        request.setAttribute("allVouchers", allVouchers);

        // 2. Kiểm tra xem User đã đăng nhập chưa để lấy danh sách Voucher đã lưu
        String savedCodes = ",";
        Object userObj = request.getSession().getAttribute("user");
        if (userObj != null) {
            int userId = 0;
            try {
                // Tự động gọi getId() hoặc getUserId() từ đối tượng Session
                userId = (Integer) userObj.getClass().getMethod("getId").invoke(userObj);
            } catch (Exception e) {
                try {
                    userId = (Integer) userObj.getClass().getMethod("getUserId").invoke(userObj);
                } catch (Exception ex) {}
            }

            // Nếu tìm thấy UserId, lấy danh sách mã đã lưu của họ
            if (userId > 0) {
                List<Coupon> savedCoupons = couponDAO.findSavedCouponsByUserId(userId);
                if (savedCoupons != null) {
                    for (Coupon c : savedCoupons) {
                        savedCodes += c.getCode() + ","; // Tạo chuỗi dạng: ,VOUCHER1,VOUCHER2,
                    }
                }
            }
        }
        // Gửi chuỗi các mã đã lưu sang trang JSP
        request.setAttribute("savedCodes", savedCodes);

        // 3. Cấu hình hiển thị qua base.jsp
        request.setAttribute("pageTitle", "Tất cả ưu đãi - MyCosmetic");
        request.setAttribute("pageCss", "voucher.css");
        request.setAttribute("pageContent", "/jsp/coupon/vouchers.jsp");

        request.getRequestDispatcher("/jsp/common/base.jsp").forward(request, response);
    }
}
package com.mycosmeticshop.controller.OrderController;

import com.mycosmeticshop.model.CartItem;
import com.mycosmeticshop.model.User;
import com.mycosmeticshop.service.CheckoutService;
import com.mycosmeticshop.utils.CartUtil;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Servlet xử lý chức năng thanh toán
 * URL truy cập: /checkout
 *
 * Chức năng:
 * - Hiển thị trang checkout
 * - Kiểm tra người dùng đã đăng nhập hay chưa
 * - Kiểm tra giỏ hàng có sản phẩm hay không
 * - Áp dụng mã khuyến mãi
 * - Tạo đơn hàng
 * - Hỗ trợ thanh toán COD và VNPAY
 */
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Service xử lý logic checkout
    private final CheckoutService checkoutService = new CheckoutService();

    /*
     * Tính tổng tiền tạm tính của giỏ hàng
     * subtotal = tổng các item.getSubtotal()
     */
    private BigDecimal calcSubTotal(Map<Integer, CartItem> cart) {
        BigDecimal sub = BigDecimal.ZERO;

        for (CartItem it : cart.values()) {
            if (it != null && it.getSubtotal() != null) {
                sub = sub.add(it.getSubtotal());
            }
        }

        return sub;
    }

    /*
     * Kiểm tra chuỗi rỗng hoặc chỉ chứa khoảng trắng
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /*
     * Phương thức GET
     * Hiển thị trang checkout
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập encoding UTF-8
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // =====================================================
        // 1) KIỂM TRA ĐĂNG NHẬP
        // =====================================================
        HttpSession session = req.getSession(false);

        User user = (session != null) ? (User) session.getAttribute("user") : null;

        /*
         * Nếu chưa đăng nhập hoặc user.id không hợp lệ
         * -> chuyển đến trang login
         * -> kèm param redirect để đăng nhập xong quay lại checkout
         */
        if (user == null || user.getId() <= 0) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }

        // =====================================================
        // 2) KIỂM TRA GIỎ HÀNG
        // =====================================================
        Map<Integer, CartItem> cart = CartUtil.getCart(session);

        if (cart == null || cart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        // =====================================================
        // 3) TÍNH TIỀN
        // =====================================================
        BigDecimal subTotal = calcSubTotal(cart);

        // Lấy thông tin coupon đang áp dụng trong session
        String appliedCoupon = (String) session.getAttribute("CHECKOUT_COUPON");
        BigDecimal couponDiscount = (BigDecimal) session.getAttribute("CHECKOUT_COUPON_DISCOUNT");

        if (couponDiscount == null) {
            couponDiscount = BigDecimal.ZERO;
        }

        // Tổng thanh toán = subtotal - giảm giá
        BigDecimal total = subTotal.subtract(couponDiscount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        // =====================================================
        // 4) GỬI DỮ LIỆU SANG JSP
        // =====================================================
        req.setAttribute("cart", cart);

        /*
         * JSP hiện tại đang dùng subtotal
         * giữ cả 2 key để tương thích ngược nếu code cũ đang dùng subTotal
         */
        req.setAttribute("subtotal", subTotal);
        req.setAttribute("subTotal", subTotal);

        req.setAttribute("couponCode", appliedCoupon);
        req.setAttribute("couponDiscount", couponDiscount);
        req.setAttribute("total", total);

        // Flash message cho coupon
        req.setAttribute("coupon_success", session.getAttribute("coupon_success"));
        req.setAttribute("coupon_error", session.getAttribute("coupon_error"));

        // Hiển thị xong thì xóa khỏi session
        session.removeAttribute("coupon_success");
        session.removeAttribute("coupon_error");

        // =====================================================
        // 5) THIẾT LẬP LAYOUT
        // =====================================================
        req.setAttribute("pageTitle", "MyCosmetic | Thanh toán");
        req.setAttribute("pageCss", "/checkout.css");
        req.setAttribute("pageContent", "/jsp/checkout/checkout.jsp");

        // =====================================================
        // 6) RENDER TRANG CHECKOUT
        // =====================================================
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    /*
     * Phương thức POST
     * Xử lý áp dụng coupon hoặc tạo đơn hàng
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        // Thiết lập encoding UTF-8
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();

        // =====================================================
        // 1) KIỂM TRA ĐĂNG NHẬP + GUARD user.id
        // =====================================================
        User user = (User) session.getAttribute("user");

        if (user == null || user.getId() <= 0) {
            // Nếu session user lỗi hoặc id không hợp lệ -> xóa session user
            session.removeAttribute("user");
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }

        // =====================================================
        // 2) KIỂM TRA GIỎ HÀNG
        // =====================================================
        Map<Integer, CartItem> cart = CartUtil.getCart(session);

        if (cart == null || cart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        // =====================================================
        // 3) XỬ LÝ APPLY COUPON (KIỂU LEGACY / NON-AJAX)
        // =====================================================
        String action = req.getParameter("action");

        if ("apply-coupon".equalsIgnoreCase(action)) {
            String couponCode = req.getParameter("couponCode");

            // Nếu chưa nhập mã coupon
            if (isBlank(couponCode)) {
                session.removeAttribute("CHECKOUT_COUPON");
                session.removeAttribute("CHECKOUT_COUPON_DISCOUNT");
                session.setAttribute("coupon_error", "Vui lòng nhập mã khuyến mãi");
                resp.sendRedirect(req.getContextPath() + "/checkout");
                return;
            }

            couponCode = couponCode.trim();
            BigDecimal subTotal = calcSubTotal(cart);

            // Tính số tiền giảm giá từ coupon
            BigDecimal discount = checkoutService.calculateCouponDiscount(couponCode, subTotal);

            if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
                session.removeAttribute("CHECKOUT_COUPON");
                session.removeAttribute("CHECKOUT_COUPON_DISCOUNT");
                session.setAttribute("coupon_error", "Mã khuyến mãi không hợp lệ hoặc đã hết hạn");
            } else {
                session.setAttribute("CHECKOUT_COUPON", couponCode);
                session.setAttribute("CHECKOUT_COUPON_DISCOUNT", discount);
                session.setAttribute("coupon_success", "Áp dụng mã thành công");
            }

            resp.sendRedirect(req.getContextPath() + "/checkout");
            return;
        }

        // =====================================================
        // 4) ĐỌC DỮ LIỆU FORM THANH TOÁN
        // =====================================================
        String fullName = req.getParameter("fullName");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");
        String paymentMethod = req.getParameter("paymentMethod");

        // Kiểm tra thông tin người nhận
        if (isBlank(fullName) || isBlank(phone) || isBlank(address)) {
            session.setAttribute("coupon_error", "Vui lòng nhập đầy đủ thông tin người nhận.");
            resp.sendRedirect(req.getContextPath() + "/checkout");
            return;
        }

        // Nếu chưa chọn phương thức thanh toán -> mặc định COD
        if (isBlank(paymentMethod)) {
            paymentMethod = "COD";
        }

        /*
         * Coupon ưu tiên lấy từ hidden input
         * nếu không có thì fallback sang session
         */
        String couponCode = req.getParameter("couponCode");
        if (isBlank(couponCode)) {
            couponCode = (String) session.getAttribute("CHECKOUT_COUPON");
        }
        if (!isBlank(couponCode)) {
            couponCode = couponCode.trim();
        }

        try {
            // =====================================================
            // 5) TẠO ĐƠN HÀNG
            // =====================================================
            int orderId = checkoutService.checkout(
                    user.getId(),   // users.id
                    cart,
                    fullName.trim(),
                    phone.trim(),
                    address.trim(),
                    paymentMethod,
                    couponCode
            );

            // Nếu tạo đơn thất bại
            if (orderId <= 0) {
                resp.sendRedirect(
                        req.getContextPath()
                                + "/checkout/success?success=false&message=order_create_failed"
                );
                return;
            }

            // =====================================================
            // 6) XỬ LÝ THANH TOÁN COD
            // =====================================================
            if ("COD".equalsIgnoreCase(paymentMethod)) {

                // Xóa dữ liệu checkout sau khi đặt hàng thành công
                session.removeAttribute("CART");
                session.removeAttribute("CHECKOUT_COUPON");
                session.removeAttribute("CHECKOUT_COUPON_DISCOUNT");

                resp.sendRedirect(
                        req.getContextPath()
                                + "/checkout/success?success=true&orderId=" + orderId
                                + "&method=COD"
                );
                return;
            }

            // =====================================================
            // 7) XỬ LÝ THANH TOÁN VNPAY
            // =====================================================
            if ("VNPAY".equalsIgnoreCase(paymentMethod)) {

                /*
                 * Lưu thông tin đơn hàng vào session
                 * để /vnpay/payment và callback sử dụng
                 */
                session.setAttribute("VNP_ORDER_ID", orderId);

                if (!isBlank(couponCode)) {
                    session.setAttribute("VNP_COUPON", couponCode);
                } else {
                    session.removeAttribute("VNP_COUPON");
                }

                /*
                 * Snapshot cart:
                 * lưu bản sao giỏ hàng tại thời điểm thanh toán
                 * để tránh phụ thuộc vào CART có thể bị thay đổi sau đó
                 */
                session.setAttribute("VNP_CART", new LinkedHashMap<>(cart));

                resp.sendRedirect(req.getContextPath() + "/vnpay/payment");
                return;
            }

            // =====================================================
            // 8) CÁC PHƯƠNG THỨC THANH TOÁN KHÁC
            // =====================================================
            resp.sendRedirect(
                    req.getContextPath()
                            + "/checkout/success?success=true&orderId=" + orderId
                            + "&method=" + paymentMethod
            );

        } catch (Exception e) {
            e.printStackTrace();

            // Nếu phát sinh lỗi trong quá trình checkout
            resp.sendRedirect(
                    req.getContextPath()
                            + "/checkout/success?success=false&message=checkout_failed"
            );
        }
    }
}
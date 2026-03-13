package com.mycosmeticshop.controller.PaymentController;

import com.mycosmeticshop.dao.OrderDAO;
import com.mycosmeticshop.utils.VNPayUtil;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/*
 * Servlet xử lý IPN (Instant Payment Notification) từ VNPay
 * URL truy cập: /payment/vnpay-ipn
 *
 * Chức năng:
 * - Nhận callback server-to-server từ VNPay
 * - Kiểm tra chữ ký bảo mật
 * - Tìm đơn hàng theo txnRef
 * - Kiểm tra số tiền thanh toán
 * - Cập nhật trạng thái thanh toán trong database
 *
 * Lưu ý:
 * - IPN là callback từ VNPay server, không phụ thuộc session người dùng
 * - Với kiến trúc hiện tại, IPN chỉ đánh dấu payment status
 * - Việc finalize đơn hàng (items / stock / coupon) vẫn xử lý ở RETURN
 */
@WebServlet("/payment/vnpay-ipn")
public class VNPayIpnServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAO dùng để thao tác dữ liệu đơn hàng
    private final OrderDAO orderDAO = new OrderDAO();

    /*
     * Kiểm tra chuỗi rỗng hoặc chỉ chứa khoảng trắng
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /*
     * Phương thức GET
     * Xử lý callback IPN từ VNPay
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // Thiết lập encoding UTF-8
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // IPN trả kết quả dạng JSON
        resp.setContentType("application/json; charset=UTF-8");

        // =====================================================
        // 1) THU THẬP TOÀN BỘ PARAMETER vnp_*
        // =====================================================
        Map<String, String> params = new HashMap<>();

        req.getParameterMap().forEach((k, v) -> {
            if (k != null && k.startsWith("vnp_") && v != null && v.length > 0) {
                params.put(k, v[0]);
            }
        });

        // Lấy các field quan trọng
        String secureHash = req.getParameter("vnp_SecureHash");
        String txnRef = req.getParameter("vnp_TxnRef");
        String respCode = req.getParameter("vnp_ResponseCode");
        String transStatus = req.getParameter("vnp_TransactionStatus");
        String amountStr = req.getParameter("vnp_Amount");

        // Kiểm tra thiếu tham số bắt buộc
        if (isBlank(secureHash) || isBlank(txnRef) || isBlank(respCode)
                || isBlank(transStatus) || isBlank(amountStr)) {

            resp.getWriter().write("{\"RspCode\":\"99\",\"Message\":\"Missing params\"}");
            return;
        }

        // =====================================================
        // 2) KIỂM TRA CHỮ KÝ BẢO MẬT
        // =====================================================
        /*
         * Khi verify signature:
         * - Không được đưa vnp_SecureHash vào dữ liệu ký
         * - Không được đưa vnp_SecureHashType vào dữ liệu ký
         */
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        boolean valid = VNPayUtil.verifySignature(params, secureHash);

        if (!valid) {
            resp.getWriter().write("{\"RspCode\":\"97\",\"Message\":\"Invalid signature\"}");
            return;
        }

        // =====================================================
        // 3) TÌM ĐƠN HÀNG THEO txnRef
        // =====================================================
        Integer orderId = orderDAO.findIdByTxnRef(txnRef);

        if (orderId == null || orderId <= 0) {
            resp.getWriter().write("{\"RspCode\":\"01\",\"Message\":\"Order not found\"}");
            return;
        }

        // =====================================================
        // 4) KIỂM TRA SỐ TIỀN THANH TOÁN
        // =====================================================
        /*
         * VNPay thường gửi amount đã nhân 100
         * nên tổng tiền trong DB cũng cần quy đổi tương ứng để so sánh.
         */
        BigDecimal dbTotal = orderDAO.getTotalByTxnRef(txnRef);
        long dbAmount = (dbTotal != null
                ? dbTotal.multiply(BigDecimal.valueOf(100)).longValue()
                : 0L);

        long vnpAmount;
        try {
            vnpAmount = Long.parseLong(amountStr);
        } catch (Exception e) {
            resp.getWriter().write("{\"RspCode\":\"04\",\"Message\":\"Invalid amount\"}");
            return;
        }

        if (dbAmount != vnpAmount) {
            resp.getWriter().write("{\"RspCode\":\"04\",\"Message\":\"Amount mismatch\"}");
            return;
        }

        // =====================================================
        // 5) IDEMPOTENT CHECK
        // =====================================================
        /*
         * Nếu đơn đã được đánh dấu thanh toán trước đó
         * thì không xử lý lại để tránh cập nhật trùng.
         */
        if (orderDAO.isPaidByTxnRef(txnRef)) {
            resp.getWriter().write("{\"RspCode\":\"00\",\"Message\":\"Already processed\"}");
            return;
        }

        // =====================================================
        // 6) CẬP NHẬT TRẠNG THÁI THANH TOÁN
        // =====================================================
        /*
         * Với kiến trúc hiện tại:
         * - RETURN mới là nơi finalize đơn hàng (items / stock / coupon)
         * - IPN không có session người dùng
         * - Vì vậy IPN chỉ xác nhận thanh toán
         *
         * Trường hợp thành công:
         * - payment status = PAID
         * - order status   = PENDING
         *
         * Sau đó RETURN finalize xong mới chuyển tiếp sang trạng thái cuối.
         */
        if ("00".equals(respCode) && "00".equals(transStatus)) {

            // Thanh toán thành công nhưng chưa finalize đơn hàng
            orderDAO.updatePaymentStatus(orderId, "PAID", "PENDING", txnRef);

            resp.getWriter().write("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
            return;
        }

        // =====================================================
        // 7) TRƯỜNG HỢP THANH TOÁN THẤT BẠI / BỊ HỦY
        // =====================================================
        orderDAO.updatePaymentStatus(orderId, "FAILED", "CANCELLED", txnRef);
        resp.getWriter().write("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
    }
}
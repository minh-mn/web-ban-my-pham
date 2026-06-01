package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.AdminOrderDAO;
import com.webshop.app.dao.AdminOrderDAO.OrderTrackingView;
import com.webshop.app.dao.AdminOrderDAO.OrderSearchFilter;
import com.webshop.app.model.Order;
import com.webshop.app.model.ShippingStatus;
import com.webshop.app.model.User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/orders")
public class AdminOrderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String VIEW_ORDER_LIST = "/jsp/admin/order/order_list.jsp";
    private static final String VIEW_ORDER_DETAIL = "/jsp/admin/order/order_detail.jsp";

    private static final String FLASH_SUCCESS = "admin_order_success";
    private static final String FLASH_ERROR = "admin_order_error";

    private static final String ACTION_LIST = "list";
    private static final String ACTION_DETAIL = "detail";

    /*
     * Action mới theo nghiệp vụ.
     * JSP admin nên gọi các action này thay vì cho admin đổi status tự do.
     */
    private static final String ACTION_CONFIRM_ORDER = "confirmOrder";
    private static final String ACTION_START_SHIPPING = "startShipping";
    private static final String ACTION_MARK_DELIVERED = "markDelivered";
    private static final String ACTION_MARK_FAILED = "markFailed";
    private static final String ACTION_CANCEL_ORDER = "cancelOrder";

    /*
     * Giữ lại action cũ để không làm lỗi các form/JSP cũ.
     * Các action cũ sẽ được chuyển sang workflow mới, không update tự do nữa.
     */
    private static final String ACTION_UPDATE_STATUS = "updateStatus";
    private static final String ACTION_UPDATE_SHIPPING_STATUS = "updateShippingStatus";
    private static final String ACTION_UPDATE_SHIPPING_INFO = "updateShippingInfo";
    private static final String ACTION_BULK_WORKFLOW = "bulkWorkflow";

    private final AdminOrderDAO orderDAO = new AdminOrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = normalizeAction(req.getParameter("action"));

        switch (action) {
            case ACTION_DETAIL -> showDetail(req, resp);
            case ACTION_LIST -> showList(req, resp);
            default -> showList(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = normalizeAction(req.getParameter("action"));

        switch (action) {
            case ACTION_CONFIRM_ORDER -> handleConfirmOrder(req, resp);
            case ACTION_START_SHIPPING -> handleStartShipping(req, resp);
            case ACTION_MARK_DELIVERED -> handleMarkDelivered(req, resp);
            case ACTION_MARK_FAILED -> handleMarkFailed(req, resp);
            case ACTION_CANCEL_ORDER -> handleCancelOrder(req, resp);

            case ACTION_UPDATE_STATUS -> handleLegacyUpdateStatus(req, resp);
            case ACTION_UPDATE_SHIPPING_STATUS -> handleLegacyUpdateShippingStatus(req, resp);
            case ACTION_UPDATE_SHIPPING_INFO -> handleUpdateShippingInfo(req, resp);
            case ACTION_BULK_WORKFLOW -> handleBulkWorkflow(req, resp);

            default -> {
                setFlashError(req, "Thao tác quản trị đơn hàng không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/admin/orders");
            }
        }
    }

    /* =========================================================
       GET VIEWS
    ========================================================= */

    private void showList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        consumeFlash(req);

        OrderSearchFilter filter = buildSearchFilter(req);
        int totalRows = orderDAO.count(filter);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalRows / filter.getPageSize()));

        if (filter.getPage() > totalPages) {
            filter.setPage(totalPages);
        }

        req.setAttribute("orders", orderDAO.search(filter));
        req.setAttribute("filter", filter);
        req.setAttribute("totalRows", totalRows);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("currentPage", filter.getPage());
        req.setAttribute("pageSize", filter.getPageSize());
        req.setAttribute("filterQueryString", buildFilterQueryString(filter));
        req.setAttribute("pageTitle", "Admin - Quản lý đơn hàng");
        req.setAttribute("activeMenu", "orders");

        req.getRequestDispatcher(VIEW_ORDER_LIST).forward(req, resp);
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int id = parseInt(req.getParameter("id"), -1);

        if (id <= 0) {
            setFlashError(req, "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return;
        }

        Order order = orderDAO.findById(id);

        if (order == null) {
            setFlashError(req, "Không tìm thấy đơn hàng #" + id + ".");
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return;
        }

        List<OrderTrackingView> trackingList = loadTrackingSafely(id);

        consumeFlash(req);

        req.setAttribute("order", order);
        req.setAttribute("trackingList", trackingList);
        req.setAttribute("shippingStatuses", ShippingStatus.values());

        /*
         * Các cờ này giúp JSP hiện đúng nút theo từng trạng thái.
         * Ví dụ:
         * - canConfirmOrder: hiện nút "Xác nhận đơn"
         * - canStartShipping: hiện nút "Bắt đầu giao"
         * - canMarkDelivered/canMarkFailed: hiện nút kết quả giao hàng
         * - canCancelOrder: hiện nút hủy đơn khi đơn chưa giao
         */
        req.setAttribute("canConfirmOrder", canConfirmOrder(order));
        req.setAttribute("canStartShipping", canStartShipping(order));
        req.setAttribute("canMarkDelivered", canMarkDelivered(order));
        req.setAttribute("canMarkFailed", canMarkFailed(order));
        req.setAttribute("canCancelOrder", canCancelOrder(order));
        req.setAttribute("canUpdateShippingInfo", canUpdateShippingInfo(order));

        req.setAttribute("pageTitle", "Admin - Chi tiết đơn hàng #" + id);
        req.setAttribute("activeMenu", "orders");

        req.getRequestDispatcher(VIEW_ORDER_DETAIL).forward(req, resp);
    }

    private List<OrderTrackingView> loadTrackingSafely(int orderId) {
        try {
            return orderDAO.findTrackingByOrderId(orderId);
        } catch (RuntimeException e) {
            /*
             * Không để lỗi bảng tracking làm hỏng trang chi tiết đơn hàng.
             * Nếu chưa tạo bảng store_order_tracking, trang vẫn xem được đơn hàng.
             */
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /* =========================================================
       WORKFLOW HANDLERS
    ========================================================= */

    private void handleConfirmOrder(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int id = parseInt(req.getParameter("id"), -1);
        Order order = findOrderOrRedirect(req, resp, id);

        if (order == null) {
            return;
        }

        if (!canConfirmOrder(order)) {
            setFlashError(req, "Chỉ có thể xác nhận đơn hàng đang chờ xử lý.");
            resp.sendRedirect(detailUrl(req, id));
            return;
        }

        String paymentStatus = normalizePaymentStatus(order.getPaymentStatus());
        String note = resolveTrackingNote(req, "Admin đã xác nhận đơn hàng và chờ bàn giao cho đơn vị vận chuyển.");
        Integer adminId = getCurrentAdminId(req);

        boolean updatedOrder = orderDAO.updateStatusAndPaymentStatus(id, "confirmed", paymentStatus);
        boolean updatedShipping = updateShippingStatusSafely(
                id,
                "PENDING_PICKUP",
                adminId,
                note
        );

        if (updatedOrder || updatedShipping) {
            setFlashSuccess(req, "Đã xác nhận đơn hàng thành công.");
        } else {
            setFlashError(req, "Không thể xác nhận đơn hàng.");
        }

        resp.sendRedirect(detailUrl(req, id));
    }

    private void handleStartShipping(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int id = parseInt(req.getParameter("id"), -1);
        Order order = findOrderOrRedirect(req, resp, id);

        if (order == null) {
            return;
        }

        if (!canStartShipping(order)) {
            setFlashError(req, "Chỉ có thể bắt đầu giao khi đơn đã được xác nhận hoặc giao thất bại trước đó.");
            resp.sendRedirect(detailUrl(req, id));
            return;
        }

        String paymentStatus = normalizePaymentStatus(order.getPaymentStatus());
        String note = resolveTrackingNote(req, "Đơn hàng đã được bàn giao và bắt đầu giao cho khách.");
        Integer adminId = getCurrentAdminId(req);

        boolean updatedOrder = orderDAO.updateStatusAndPaymentStatus(id, "shipping", paymentStatus);
        boolean updatedShipping = updateShippingStatusSafely(
                id,
                "DELIVERING",
                adminId,
                note
        );

        if (updatedOrder || updatedShipping) {
            setFlashSuccess(req, "Đã chuyển đơn hàng sang trạng thái đang giao.");
        } else {
            setFlashError(req, "Không thể bắt đầu giao đơn hàng.");
        }

        resp.sendRedirect(detailUrl(req, id));
    }

    private void handleMarkDelivered(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int id = parseInt(req.getParameter("id"), -1);
        Order order = findOrderOrRedirect(req, resp, id);

        if (order == null) {
            return;
        }

        if (!canMarkDelivered(order)) {
            setFlashError(req, "Chỉ có thể xác nhận giao thành công khi đơn đang giao.");
            resp.sendRedirect(detailUrl(req, id));
            return;
        }

        String note = resolveTrackingNote(req, "Đơn hàng đã giao thành công cho khách.");
        Integer adminId = getCurrentAdminId(req);

        /*
         * Khi giao thành công:
         * - status = completed
         * - shipping_status = DELIVERED
         * - payment_status = PAID
         *
         * Với COD, khách đã thanh toán khi nhận hàng.
         * Với thanh toán online, trạng thái PAID cũng hợp lý nếu đơn đã hoàn tất.
         */
        boolean updatedOrder = orderDAO.updateStatusAndPaymentStatus(id, "completed", "PAID");
        boolean updatedShipping = updateShippingStatusSafely(
                id,
                "DELIVERED",
                adminId,
                note
        );

        if (updatedOrder || updatedShipping) {
            setFlashSuccess(req, "Đã xác nhận giao hàng thành công.");
        } else {
            setFlashError(req, "Không thể xác nhận giao hàng thành công.");
        }

        resp.sendRedirect(detailUrl(req, id));
    }

    private void handleMarkFailed(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int id = parseInt(req.getParameter("id"), -1);
        Order order = findOrderOrRedirect(req, resp, id);

        if (order == null) {
            return;
        }

        if (!canMarkFailed(order)) {
            setFlashError(req, "Chỉ có thể đánh dấu giao thất bại khi đơn đang giao.");
            resp.sendRedirect(detailUrl(req, id));
            return;
        }

        String paymentStatus = normalizePaymentStatus(order.getPaymentStatus());
        String note = resolveTrackingNote(req, "Giao hàng thất bại. Admin cần kiểm tra và xử lý giao lại hoặc hủy đơn.");
        Integer adminId = getCurrentAdminId(req);

        /*
         * Giao thất bại chưa đồng nghĩa hủy đơn.
         * Giữ status = shipping để admin có thể giao lại hoặc xử lý tiếp.
         */
        boolean updatedOrder = orderDAO.updateStatusAndPaymentStatus(id, "shipping", paymentStatus);
        boolean updatedShipping = updateShippingStatusSafely(
                id,
                "FAILED",
                adminId,
                note
        );

        if (updatedOrder || updatedShipping) {
            setFlashSuccess(req, "Đã đánh dấu đơn hàng giao thất bại.");
        } else {
            setFlashError(req, "Không thể cập nhật giao hàng thất bại.");
        }

        resp.sendRedirect(detailUrl(req, id));
    }

    private void handleCancelOrder(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int id = parseInt(req.getParameter("id"), -1);
        Order order = findOrderOrRedirect(req, resp, id);

        if (order == null) {
            return;
        }

        if (!canCancelOrder(order)) {
            setFlashError(req, "Không thể hủy đơn đã giao thành công hoặc đang giao. Nếu giao thất bại, hãy xử lý hoàn/hủy ở bước riêng.");
            resp.sendRedirect(detailUrl(req, id));
            return;
        }

        String note = resolveTrackingNote(req, "Admin đã hủy đơn hàng.");
        Integer adminId = getCurrentAdminId(req);

        boolean updatedOrder = orderDAO.updateStatusAndPaymentStatus(id, "cancelled", "CANCELED");
        boolean updatedShipping = updateShippingStatusSafely(
                id,
                "CANCELED",
                adminId,
                note
        );

        if (updatedOrder || updatedShipping) {
            setFlashSuccess(req, "Đã hủy đơn hàng thành công.");
        } else {
            setFlashError(req, "Không thể hủy đơn hàng.");
        }

        resp.sendRedirect(detailUrl(req, id));
    }

    private void handleBulkWorkflow(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String[] rawIds = req.getParameterValues("selectedOrderIds");
        String bulkAction = normalizeBulkAction(req.getParameter("bulkAction"));
        String returnUrl = safeAdminOrdersReturnUrl(req, req.getParameter("returnUrl"));

        if (rawIds == null || rawIds.length == 0) {
            setFlashError(req, "Vui lòng chọn ít nhất một đơn hàng để xử lý hàng loạt.");
            resp.sendRedirect(returnUrl);
            return;
        }

        if (bulkAction.isEmpty()) {
            setFlashError(req, "Vui lòng chọn thao tác xử lý hàng loạt.");
            resp.sendRedirect(returnUrl);
            return;
        }

        Integer adminId = getCurrentAdminId(req);
        String note = resolveTrackingNote(req, defaultBulkNote(bulkAction));

        int successCount = 0;
        int skippedCount = 0;

        for (String rawId : rawIds) {
            int orderId = parseInt(rawId, -1);

            if (orderId <= 0) {
                skippedCount++;
                continue;
            }

            Order order = orderDAO.findById(orderId);

            if (order == null) {
                skippedCount++;
                continue;
            }

            if (applyBulkWorkflowAction(order, bulkAction, adminId, note)) {
                successCount++;
            } else {
                skippedCount++;
            }
        }

        if (successCount > 0) {
            setFlashSuccess(
                    req,
                    "Đã xử lý " + successCount + " đơn hàng."
                            + (skippedCount > 0 ? " Bỏ qua " + skippedCount + " đơn không hợp lệ hoặc sai luồng." : "")
            );
        } else {
            setFlashError(req, "Không có đơn hàng nào được xử lý. Vui lòng kiểm tra trạng thái hiện tại của các đơn đã chọn.");
        }

        resp.sendRedirect(returnUrl);
    }

    private boolean applyBulkWorkflowAction(Order order,
                                            String bulkAction,
                                            Integer adminId,
                                            String note) {
        int orderId = order.getId();

        return switch (bulkAction) {
            case ACTION_CONFIRM_ORDER -> {
                if (!canConfirmOrder(order)) {
                    yield false;
                }
                yield updateShippingStatusSafely(orderId, "PENDING_PICKUP", adminId, note);
            }
            case ACTION_START_SHIPPING -> {
                if (!canStartShipping(order)) {
                    yield false;
                }
                yield updateShippingStatusSafely(orderId, "DELIVERING", adminId, note);
            }
            case ACTION_MARK_DELIVERED -> {
                if (!canMarkDelivered(order)) {
                    yield false;
                }
                boolean updatedOrder = orderDAO.updateStatusAndPaymentStatus(orderId, "completed", "PAID");
                boolean updatedShipping = updateShippingStatusSafely(orderId, "DELIVERED", adminId, note);
                yield updatedOrder || updatedShipping;
            }
            case ACTION_MARK_FAILED -> {
                if (!canMarkFailed(order)) {
                    yield false;
                }
                yield updateShippingStatusSafely(orderId, "FAILED", adminId, note);
            }
            case ACTION_CANCEL_ORDER -> {
                if (!canCancelOrderInBulk(order)) {
                    yield false;
                }
                yield updateShippingStatusSafely(orderId, "CANCELED", adminId, note);
            }
            default -> false;
        };
    }

    private boolean canCancelOrderInBulk(Order order) {
        String status = getOrderStatus(order);
        String shippingStatus = getShippingStatus(order);

        if (isFinalOrderStatus(status) || "DELIVERED".equals(shippingStatus) || "CANCELED".equals(shippingStatus)) {
            return false;
        }

        return "processing".equals(status)
                || "confirmed".equals(status)
                || ("shipping".equals(status) && "FAILED".equals(shippingStatus));
    }

    /* =========================================================
       LEGACY HANDLERS - CHUYỂN ACTION CŨ SANG WORKFLOW MỚI
    ========================================================= */

    private void handleLegacyUpdateStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String status = normalizeStatus(req.getParameter("status"));

        switch (status) {
            case "confirmed" -> handleConfirmOrder(req, resp);
            case "shipping" -> handleStartShipping(req, resp);
            case "completed" -> handleMarkDelivered(req, resp);
            case "cancelled" -> handleCancelOrder(req, resp);
            case "processing" -> {
                int id = parseInt(req.getParameter("id"), -1);
                setFlashError(req, "Không hỗ trợ chuyển ngược đơn hàng về trạng thái chờ xử lý.");
                resp.sendRedirect(id > 0 ? detailUrl(req, id) : req.getContextPath() + "/admin/orders");
            }
            default -> {
                int id = parseInt(req.getParameter("id"), -1);
                setFlashError(req, "Trạng thái đơn hàng không hợp lệ.");
                resp.sendRedirect(id > 0 ? detailUrl(req, id) : req.getContextPath() + "/admin/orders");
            }
        }
    }

    private void handleLegacyUpdateShippingStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String shippingStatus = ShippingStatus.normalizeCode(req.getParameter("shippingStatus"));

        switch (shippingStatus) {
            case "PENDING_PICKUP" -> handleConfirmOrder(req, resp);
            case "DELIVERING" -> handleStartShipping(req, resp);
            case "DELIVERED" -> handleMarkDelivered(req, resp);
            case "FAILED" -> handleMarkFailed(req, resp);
            case "CANCELED" -> handleCancelOrder(req, resp);
            default -> {
                int id = parseInt(req.getParameter("id"), -1);
                setFlashError(req, "Trạng thái vận chuyển không hợp lệ.");
                resp.sendRedirect(id > 0 ? detailUrl(req, id) : req.getContextPath() + "/admin/orders");
            }
        }
    }

    private void handleUpdateShippingInfo(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int id = parseInt(req.getParameter("id"), -1);
        String shippingProvider = normalizeShippingProvider(req.getParameter("shippingProvider"));
        String shippingCode = trim(req.getParameter("shippingCode"));
        String shippingMethod = normalizeShippingMethod(req.getParameter("shippingMethod"));
        BigDecimal shippingFee = parseVnd(req.getParameter("shippingFee"));

        Order order = findOrderOrRedirect(req, resp, id);

        if (order == null) {
            return;
        }

        if (!canUpdateShippingInfo(order)) {
            setFlashError(req, "Không thể sửa thông tin vận chuyển khi đơn đã hoàn tất hoặc đã hủy.");
            resp.sendRedirect(detailUrl(req, id));
            return;
        }

        boolean updated = orderDAO.updateShippingInfo(
                id,
                shippingProvider,
                shippingCode,
                shippingMethod,
                shippingFee
        );

        if (updated) {
            setFlashSuccess(req, "Cập nhật thông tin vận chuyển thành công.");
        } else {
            setFlashError(req, "Không thể cập nhật thông tin vận chuyển.");
        }

        resp.sendRedirect(detailUrl(req, id));
    }

    /* =========================================================
       BUSINESS RULES
    ========================================================= */

    private boolean canConfirmOrder(Order order) {
        String status = getOrderStatus(order);
        String shippingStatus = getShippingStatus(order);

        return "processing".equals(status)
                && !isFinalOrderStatus(status)
                && !isFinalShippingStatus(shippingStatus);
    }

    private boolean canStartShipping(Order order) {
        String status = getOrderStatus(order);
        String shippingStatus = getShippingStatus(order);

        return "confirmed".equals(status)
                || ("shipping".equals(status) && "FAILED".equals(shippingStatus));
    }

    private boolean canMarkDelivered(Order order) {
        String status = getOrderStatus(order);
        String shippingStatus = getShippingStatus(order);

        return "shipping".equals(status)
                && ("DELIVERING".equals(shippingStatus) || shippingStatus.isEmpty());
    }

    private boolean canMarkFailed(Order order) {
        String status = getOrderStatus(order);
        String shippingStatus = getShippingStatus(order);

        return "shipping".equals(status)
                && ("DELIVERING".equals(shippingStatus) || shippingStatus.isEmpty());
    }

    private boolean canCancelOrder(Order order) {
        String status = getOrderStatus(order);
        String shippingStatus = getShippingStatus(order);

        if (isFinalOrderStatus(status) || isFinalShippingStatus(shippingStatus)) {
            return false;
        }

        /*
         * Không hủy trực tiếp khi đang giao.
         * Khi đang giao, admin nên chọn giao thất bại trước để lưu tracking rõ ràng.
         */
        return "processing".equals(status) || "confirmed".equals(status);
    }

    private boolean canUpdateShippingInfo(Order order) {
        String status = getOrderStatus(order);

        return !isFinalOrderStatus(status);
    }

    private boolean isFinalOrderStatus(String status) {
        return switch (normalizeStatus(status)) {
            case "completed", "cancelled" -> true;
            default -> false;
        };
    }

    private boolean isFinalShippingStatus(String shippingStatus) {
        return switch (ShippingStatus.normalizeCode(shippingStatus)) {
            case "DELIVERED", "CANCELED" -> true;
            default -> false;
        };
    }

    private String getOrderStatus(Order order) {
        if (order == null) {
            return "";
        }

        return normalizeStatus(order.getStatus());
    }

    private String getShippingStatus(Order order) {
        if (order == null) {
            return "";
        }

        return ShippingStatus.normalizeCode(order.getShippingStatus());
    }

    private boolean updateShippingStatusSafely(
            int orderId,
            String shippingStatus,
            Integer adminId,
            String note
    ) {
        try {
            return orderDAO.updateShippingStatus(
                    orderId,
                    shippingStatus,
                    adminId,
                    note
            );
        } catch (RuntimeException e) {
            /*
             * Nếu database cũ chưa có bảng/cột tracking thì vẫn không làm hỏng toàn bộ workflow.
             * Tuy nhiên nên chạy migration để tracking và shipped_at/delivered_at được cập nhật đầy đủ.
             */
            e.printStackTrace();
            return false;
        }
    }

    private Order findOrderOrRedirect(HttpServletRequest req, HttpServletResponse resp, int id)
            throws IOException {

        if (id <= 0) {
            setFlashError(req, "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return null;
        }

        Order order = orderDAO.findById(id);

        if (order == null) {
            setFlashError(req, "Không tìm thấy đơn hàng #" + id + ".");
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return null;
        }

        return order;
    }

    /* =========================================================
       FILTER / PAGINATION
    ========================================================= */

    private OrderSearchFilter buildSearchFilter(HttpServletRequest req) {
        OrderSearchFilter filter = new OrderSearchFilter();

        filter.setKeyword(trim(req.getParameter("keyword")));
        filter.setOrderStatus(emptyToNull(req.getParameter("orderStatus")));
        filter.setPaymentStatus(emptyToNull(req.getParameter("paymentStatus")));
        filter.setShippingStatus(emptyToNull(req.getParameter("shippingStatus")));
        filter.setShippingProvider(emptyToNull(req.getParameter("shippingProvider")));
        filter.setDateFrom(parseLocalDate(req.getParameter("dateFrom")));
        filter.setDateTo(parseLocalDate(req.getParameter("dateTo")));
        filter.setPage(parseInt(req.getParameter("page"), 1));
        filter.setPageSize(parsePageSize(req.getParameter("pageSize"), 20));

        return filter;
    }

    private int parsePageSize(String value, int fallback) {
        int parsed = parseInt(value, fallback);

        return switch (parsed) {
            case 10, 20, 50, 100 -> parsed;
            default -> fallback;
        };
    }

    private LocalDate parseLocalDate(String value) {
        String trimmed = trim(value);

        if (trimmed.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(trimmed);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildFilterQueryString(OrderSearchFilter filter) {
        StringBuilder query = new StringBuilder();

        appendQueryParam(query, "keyword", filter.getKeyword());
        appendQueryParam(query, "orderStatus", filter.getOrderStatus());
        appendQueryParam(query, "paymentStatus", filter.getPaymentStatus());
        appendQueryParam(query, "shippingStatus", filter.getShippingStatus());
        appendQueryParam(query, "shippingProvider", filter.getShippingProvider());
        appendQueryParam(query, "dateFrom", filter.getDateFrom() == null ? "" : filter.getDateFrom().toString());
        appendQueryParam(query, "dateTo", filter.getDateTo() == null ? "" : filter.getDateTo().toString());
        appendQueryParam(query, "pageSize", String.valueOf(filter.getPageSize()));

        return query.toString();
    }

    private void appendQueryParam(StringBuilder query, String name, String value) {
        String trimmed = trim(value);

        if (trimmed.isEmpty()) {
            return;
        }

        if (query.length() > 0) {
            query.append('&');
        }

        query.append(URLEncoder.encode(name, StandardCharsets.UTF_8));
        query.append('=');
        query.append(URLEncoder.encode(trimmed, StandardCharsets.UTF_8));
    }

    private String safeAdminOrdersReturnUrl(HttpServletRequest req, String returnUrl) {
        String fallback = req.getContextPath() + "/admin/orders";
        String value = trim(returnUrl);

        if (value.isEmpty()
                || "null".equalsIgnoreCase(value)
                || value.startsWith("http://")
                || value.startsWith("https://")) {
            return fallback;
        }

        if (!value.startsWith("/admin/orders")) {
            return fallback;
        }

        return req.getContextPath() + value;
    }

    private String normalizeBulkAction(String action) {
        String value = trim(action);

        return switch (value) {
            case ACTION_CONFIRM_ORDER,
                 ACTION_START_SHIPPING,
                 ACTION_MARK_DELIVERED,
                 ACTION_MARK_FAILED,
                 ACTION_CANCEL_ORDER -> value;
            default -> "";
        };
    }

    private String defaultBulkNote(String bulkAction) {
        return switch (bulkAction) {
            case ACTION_CONFIRM_ORDER -> "Admin đã xác nhận đơn hàng bằng thao tác hàng loạt.";
            case ACTION_START_SHIPPING -> "Admin đã chuyển đơn sang đang giao bằng thao tác hàng loạt.";
            case ACTION_MARK_DELIVERED -> "Admin đã xác nhận giao thành công bằng thao tác hàng loạt.";
            case ACTION_MARK_FAILED -> "Admin đã đánh dấu giao thất bại bằng thao tác hàng loạt.";
            case ACTION_CANCEL_ORDER -> "Admin đã hủy đơn hàng bằng thao tác hàng loạt.";
            default -> "Admin đã cập nhật đơn hàng bằng thao tác hàng loạt.";
        };
    }

    private String emptyToNull(String value) {
        String trimmed = trim(value);
        return trimmed.isEmpty() ? null : trimmed;
    }

    /* =========================================================
       NORMALIZE / PARSE
    ========================================================= */

    private String normalizeAction(String action) {
        String value = trim(action);

        if (value.isEmpty()) {
            return ACTION_LIST;
        }

        return value;
    }

    private String normalizeStatus(String status) {
        String value = trim(status).toLowerCase();

        if ("canceled".equals(value)) {
            return "cancelled";
        }

        return value;
    }

    private String normalizePaymentStatus(String paymentStatus) {
        String value = trim(paymentStatus).toUpperCase();

        if (value.isEmpty()) {
            return "PENDING";
        }

        if ("CANCELLED".equals(value)) {
            return "CANCELED";
        }

        return switch (value) {
            case "PENDING", "PAID", "FAILED", "CANCELED", "REFUNDED" -> value;
            default -> "PENDING";
        };
    }

    private String normalizeShippingProvider(String provider) {
        String value = trim(provider).toUpperCase();

        if (value.isEmpty()) {
            return "INTERNAL";
        }

        return switch (value) {
            case "INTERNAL", "GHTK", "GHN", "VIETTEL_POST", "OTHER" -> value;
            default -> "INTERNAL";
        };
    }

    private String normalizeShippingMethod(String method) {
        String value = trim(method).toUpperCase();

        if (value.isEmpty()) {
            return "ECONOMY";
        }

        return switch (value) {
            case "ECONOMY", "FAST", "EXPRESS" -> value;
            default -> "ECONOMY";
        };
    }

    private String resolveTrackingNote(HttpServletRequest req, String fallback) {
        String note = trim(req.getParameter("trackingNote"));

        if (note.isEmpty()) {
            note = trim(req.getParameter("note"));
        }

        if (note.isEmpty()) {
            return fallback;
        }

        return note;
    }

    private BigDecimal parseVnd(String raw) {
        String value = trim(raw)
                .replace(",", "")
                .replace(".", "")
                .replace(" ", "");

        if (value.isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal money = new BigDecimal(value);

            if (money.compareTo(BigDecimal.ZERO) < 0) {
                return BigDecimal.ZERO;
            }

            return money.setScale(0, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(trim(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    /* =========================================================
       SESSION / FLASH
    ========================================================= */

    private Integer getCurrentAdminId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);

        if (session == null) {
            return null;
        }

        Object user = session.getAttribute("user");

        if (user == null) {
            user = session.getAttribute("authUser");
        }

        if (user instanceof User currentUser && currentUser.getId() > 0) {
            return currentUser.getId();
        }

        return null;
    }

    private void setFlashSuccess(HttpServletRequest req, String message) {
        req.getSession().setAttribute(FLASH_SUCCESS, message);
    }

    private void setFlashError(HttpServletRequest req, String message) {
        req.getSession().setAttribute(FLASH_ERROR, message);
    }

    private void consumeFlash(HttpServletRequest req) {
        HttpSession session = req.getSession();

        Object success = session.getAttribute(FLASH_SUCCESS);
        Object error = session.getAttribute(FLASH_ERROR);

        if (success != null) {
            req.setAttribute(FLASH_SUCCESS, success);
            session.removeAttribute(FLASH_SUCCESS);
        }

        if (error != null) {
            req.setAttribute(FLASH_ERROR, error);
            session.removeAttribute(FLASH_ERROR);
        }
    }

    private String detailUrl(HttpServletRequest req, int id) {
        return req.getContextPath() + "/admin/orders?action=detail&id=" + id;
    }
}

package com.webshop.app.service;

import com.webshop.app.dao.FlashSaleItemDAO;
import com.webshop.app.model.CartItem;
import com.webshop.app.model.FlashSaleItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Issue 139:
 * Service kiểm tra giới hạn mua Flash Sale theo từng khách hàng.
 *
 * Mục tiêu:
 * - Mỗi sản phẩm Flash Sale có max_quantity_per_user.
 * - User không được thêm/tăng/cập nhật giỏ hàng vượt giới hạn này.
 * - Checkout kiểm tra lại lần cuối để tránh bypass bằng request thủ công.
 */
public class FlashSaleLimitService {

    private final FlashSaleItemDAO flashSaleItemDAO = new FlashSaleItemDAO();

    /**
     * Kết quả kiểm tra giới hạn mua Flash Sale.
     */
    public static class LimitResult {

        private final boolean limitedProduct;
        private final boolean allowed;
        private final int productId;
        private final int flashSaleId;
        private final int flashSaleItemId;
        private final int maxQuantityPerUser;
        private final int purchasedQuantity;
        private final int cartQuantity;
        private final int requestedQuantity;
        private final int remainingAllowedQuantity;
        private final String message;

        private LimitResult(
                boolean limitedProduct,
                boolean allowed,
                int productId,
                int flashSaleId,
                int flashSaleItemId,
                int maxQuantityPerUser,
                int purchasedQuantity,
                int cartQuantity,
                int requestedQuantity,
                int remainingAllowedQuantity,
                String message
        ) {
            this.limitedProduct = limitedProduct;
            this.allowed = allowed;
            this.productId = productId;
            this.flashSaleId = flashSaleId;
            this.flashSaleItemId = flashSaleItemId;
            this.maxQuantityPerUser = maxQuantityPerUser;
            this.purchasedQuantity = purchasedQuantity;
            this.cartQuantity = cartQuantity;
            this.requestedQuantity = requestedQuantity;
            this.remainingAllowedQuantity = remainingAllowedQuantity;
            this.message = message;
        }

        public static LimitResult notFlashSale(int productId) {
            return new LimitResult(
                    false,
                    true,
                    productId,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    Integer.MAX_VALUE,
                    ""
            );
        }

        public static LimitResult allowed(
                FlashSaleItem item,
                int productId,
                int purchasedQuantity,
                int cartQuantity,
                int requestedQuantity,
                int remainingAllowedQuantity
        ) {
            return new LimitResult(
                    true,
                    true,
                    productId,
                    item.getFlashSaleId(),
                    item.getId(),
                    item.getMaxQuantityPerUser(),
                    purchasedQuantity,
                    cartQuantity,
                    requestedQuantity,
                    remainingAllowedQuantity,
                    "Flash Sale: giới hạn " + item.getMaxQuantityPerUser() + " sản phẩm/khách"
            );
        }

        public static LimitResult denied(
                FlashSaleItem item,
                int productId,
                int purchasedQuantity,
                int cartQuantity,
                int requestedQuantity,
                int remainingAllowedQuantity
        ) {
            int maxQuantity = item.getMaxQuantityPerUser();

            String message;
            if (remainingAllowedQuantity <= 0) {
                message = "Bạn đã đạt giới hạn mua Flash Sale cho sản phẩm này.";
            } else {
                message = "Flash Sale chỉ cho phép mua tối đa "
                        + maxQuantity
                        + " sản phẩm/khách. Bạn còn được mua thêm "
                        + remainingAllowedQuantity
                        + " sản phẩm.";
            }

            return new LimitResult(
                    true,
                    false,
                    productId,
                    item.getFlashSaleId(),
                    item.getId(),
                    maxQuantity,
                    purchasedQuantity,
                    cartQuantity,
                    requestedQuantity,
                    Math.max(remainingAllowedQuantity, 0),
                    message
            );
        }

        public boolean isLimitedProduct() {
            return limitedProduct;
        }

        public boolean getLimitedProduct() {
            return limitedProduct;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public boolean getAllowed() {
            return allowed;
        }

        public boolean isDenied() {
            return !allowed;
        }

        public boolean getDenied() {
            return isDenied();
        }

        public int getProductId() {
            return productId;
        }

        public int getFlashSaleId() {
            return flashSaleId;
        }

        public int getFlashSaleItemId() {
            return flashSaleItemId;
        }

        public int getMaxQuantityPerUser() {
            return maxQuantityPerUser;
        }

        public int getPurchasedQuantity() {
            return purchasedQuantity;
        }

        public int getCartQuantity() {
            return cartQuantity;
        }

        public int getRequestedQuantity() {
            return requestedQuantity;
        }

        public int getRemainingAllowedQuantity() {
            return remainingAllowedQuantity == Integer.MAX_VALUE ? 0 : remainingAllowedQuantity;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Kiểm tra khi thêm thêm số lượng vào giỏ.
     *
     * Ví dụ:
     * - Trong giỏ đang có 1.
     * - User bấm thêm 1.
     * - requestedAddQuantity = 1.
     */
    public LimitResult checkCanAdd(
            int userId,
            Map<String, CartItem> cart,
            int productId,
            int requestedAddQuantity
    ) {
        if (productId <= 0 || requestedAddQuantity <= 0) {
            return LimitResult.notFlashSale(productId);
        }

        FlashSaleItem item = flashSaleItemDAO.findActiveByProductId(productId);
        return checkCanAddInternal(null, userId, cart, null, productId, requestedAddQuantity, item);
    }

    /**
     * Kiểm tra khi thêm thêm số lượng vào giỏ trong transaction.
     */
    public LimitResult checkCanAdd(
            Connection conn,
            int userId,
            Map<String, CartItem> cart,
            int productId,
            int requestedAddQuantity
    ) throws SQLException {
        if (productId <= 0 || requestedAddQuantity <= 0) {
            return LimitResult.notFlashSale(productId);
        }

        FlashSaleItem item = flashSaleItemDAO.findActiveByProductId(conn, productId);
        return checkCanAddInternal(conn, userId, cart, null, productId, requestedAddQuantity, item);
    }

    /**
     * Kiểm tra khi set trực tiếp số lượng cho một cart item.
     *
     * Dùng cho:
     * - cập nhật số lượng trong cart
     * - đổi biến thể
     * - ajax checkout quantity
     */
    public LimitResult checkCanSetQuantity(
            int userId,
            Map<String, CartItem> cart,
            String targetCartKey,
            int productId,
            int newQuantity
    ) {
        if (productId <= 0 || newQuantity <= 0) {
            return LimitResult.notFlashSale(productId);
        }

        FlashSaleItem item = flashSaleItemDAO.findActiveByProductId(productId);
        return checkCanSetQuantityInternal(null, userId, cart, targetCartKey, productId, newQuantity, item);
    }

    public LimitResult checkCanSetQuantity(
            Connection conn,
            int userId,
            Map<String, CartItem> cart,
            String targetCartKey,
            int productId,
            int newQuantity
    ) throws SQLException {
        if (productId <= 0 || newQuantity <= 0) {
            return LimitResult.notFlashSale(productId);
        }

        FlashSaleItem item = flashSaleItemDAO.findActiveByProductId(conn, productId);
        return checkCanSetQuantityInternal(conn, userId, cart, targetCartKey, productId, newQuantity, item);
    }

    /**
     * Gắn thông tin giới hạn Flash Sale vào các CartItem để JSP hiển thị/khóa nút tăng.
     */
    public void enrichCartItems(int userId, Map<String, CartItem> cart) {
        if (cart == null || cart.isEmpty()) {
            return;
        }

        for (Map.Entry<String, CartItem> entry : cart.entrySet()) {
            CartItem cartItem = entry.getValue();

            if (cartItem == null || cartItem.getProductId() <= 0) {
                continue;
            }

            FlashSaleItem flashSaleItem = flashSaleItemDAO.findActiveByProductId(cartItem.getProductId());
            enrichCartItem(userId, cart, entry.getKey(), cartItem, flashSaleItem, null);
        }
    }

    public void enrichCartItems(Connection conn, int userId, Map<String, CartItem> cart) throws SQLException {
        if (cart == null || cart.isEmpty()) {
            return;
        }

        for (Map.Entry<String, CartItem> entry : cart.entrySet()) {
            CartItem cartItem = entry.getValue();

            if (cartItem == null || cartItem.getProductId() <= 0) {
                continue;
            }

            FlashSaleItem flashSaleItem = flashSaleItemDAO.findActiveByProductId(conn, cartItem.getProductId());
            enrichCartItem(userId, cart, entry.getKey(), cartItem, flashSaleItem, conn);
        }
    }

    /**
     * Kiểm tra toàn bộ giỏ trước checkout.
     */
    public LimitResult validateCart(int userId, Map<String, CartItem> cart) {
        if (cart == null || cart.isEmpty()) {
            return LimitResult.notFlashSale(0);
        }

        for (Map.Entry<String, CartItem> entry : cart.entrySet()) {
            CartItem cartItem = entry.getValue();

            if (cartItem == null || cartItem.getQuantity() <= 0) {
                continue;
            }

            LimitResult result = checkCanSetQuantity(
                    userId,
                    cart,
                    entry.getKey(),
                    cartItem.getProductId(),
                    cartItem.getQuantity()
            );

            if (!result.isAllowed()) {
                applyLimitToCartItem(cartItem, result);
                return result;
            }

            applyLimitToCartItem(cartItem, result);
        }

        return LimitResult.notFlashSale(0);
    }

    /**
     * Kiểm tra toàn bộ giỏ trước checkout trong transaction.
     */
    public LimitResult validateCart(Connection conn, int userId, Map<String, CartItem> cart) throws SQLException {
        if (cart == null || cart.isEmpty()) {
            return LimitResult.notFlashSale(0);
        }

        for (Map.Entry<String, CartItem> entry : cart.entrySet()) {
            CartItem cartItem = entry.getValue();

            if (cartItem == null || cartItem.getQuantity() <= 0) {
                continue;
            }

            LimitResult result = checkCanSetQuantity(
                    conn,
                    userId,
                    cart,
                    entry.getKey(),
                    cartItem.getProductId(),
                    cartItem.getQuantity()
            );

            if (!result.isAllowed()) {
                applyLimitToCartItem(cartItem, result);
                return result;
            }

            applyLimitToCartItem(cartItem, result);
        }

        return LimitResult.notFlashSale(0);
    }

    /**
     * Dùng cho CheckoutService: nếu vượt giới hạn thì ném lỗi ngay.
     */
    public void validateCartOrThrow(int userId, Map<String, CartItem> cart) {
        LimitResult result = validateCart(userId, cart);

        if (!result.isAllowed()) {
            throw new IllegalStateException(result.getMessage());
        }
    }

    public void validateCartOrThrow(Connection conn, int userId, Map<String, CartItem> cart) throws SQLException {
        LimitResult result = validateCart(conn, userId, cart);

        if (!result.isAllowed()) {
            throw new IllegalStateException(result.getMessage());
        }
    }

    /**
     * Tính tổng số lượng cùng productId trong giỏ.
     * Cần tính theo productId, không theo cartKey, vì cùng sản phẩm có thể có nhiều variant.
     */
    public int countProductQuantityInCart(Map<String, CartItem> cart, int productId) {
        return countProductQuantityInCart(cart, productId, null);
    }

    public int countProductQuantityInCart(Map<String, CartItem> cart, int productId, String excludeCartKey) {
        if (cart == null || cart.isEmpty() || productId <= 0) {
            return 0;
        }

        int total = 0;

        for (Map.Entry<String, CartItem> entry : cart.entrySet()) {
            if (entry == null) {
                continue;
            }

            String cartKey = entry.getKey();
            CartItem item = entry.getValue();

            if (item == null || item.getProductId() != productId) {
                continue;
            }

            if (excludeCartKey != null && excludeCartKey.equals(cartKey)) {
                continue;
            }

            total += Math.max(item.getQuantity(), 0);
        }

        return Math.max(total, 0);
    }

    private LimitResult checkCanAddInternal(
            Connection conn,
            int userId,
            Map<String, CartItem> cart,
            String targetCartKey,
            int productId,
            int requestedAddQuantity,
            FlashSaleItem item
    ) {
        if (item == null) {
            return LimitResult.notFlashSale(productId);
        }

        int cartQuantity = countProductQuantityInCart(cart, productId, targetCartKey);
        int requestedTotalQuantity = cartQuantity + Math.max(requestedAddQuantity, 0);

        return buildResult(conn, userId, productId, item, cartQuantity, requestedTotalQuantity);
    }

    private LimitResult checkCanSetQuantityInternal(
            Connection conn,
            int userId,
            Map<String, CartItem> cart,
            String targetCartKey,
            int productId,
            int newQuantity,
            FlashSaleItem item
    ) {
        if (item == null) {
            return LimitResult.notFlashSale(productId);
        }

        int otherCartQuantity = countProductQuantityInCart(cart, productId, targetCartKey);
        int requestedTotalQuantity = otherCartQuantity + Math.max(newQuantity, 0);

        return buildResult(conn, userId, productId, item, otherCartQuantity, requestedTotalQuantity);
    }

    private LimitResult buildResult(
            Connection conn,
            int userId,
            int productId,
            FlashSaleItem item,
            int cartQuantity,
            int requestedTotalQuantity
    ) {
        int maxQuantityPerUser = item.getMaxQuantityPerUser();

        if (maxQuantityPerUser <= 0) {
            return LimitResult.allowed(item, productId, 0, cartQuantity, requestedTotalQuantity, Integer.MAX_VALUE);
        }

        int purchasedQuantity = 0;

        if (userId > 0) {
            try {
                if (conn != null) {
                    purchasedQuantity = flashSaleItemDAO.countPurchasedQuantityForUser(
                            conn,
                            userId,
                            item.getFlashSaleId(),
                            productId
                    );
                } else {
                    purchasedQuantity = flashSaleItemDAO.countPurchasedQuantityForUser(
                            userId,
                            item.getFlashSaleId(),
                            productId
                    );
                }
            } catch (SQLException e) {
                throw new RuntimeException("FlashSaleLimitService.countPurchasedQuantityForUser error", e);
            }
        }

        int remainingAllowedQuantity = Math.max(0, maxQuantityPerUser - purchasedQuantity);

        if (requestedTotalQuantity > remainingAllowedQuantity) {
            return LimitResult.denied(
                    item,
                    productId,
                    purchasedQuantity,
                    cartQuantity,
                    requestedTotalQuantity,
                    remainingAllowedQuantity
            );
        }

        return LimitResult.allowed(
                item,
                productId,
                purchasedQuantity,
                cartQuantity,
                requestedTotalQuantity,
                remainingAllowedQuantity
        );
    }

    private void enrichCartItem(
            int userId,
            Map<String, CartItem> cart,
            String targetCartKey,
            CartItem cartItem,
            FlashSaleItem flashSaleItem,
            Connection conn
    ) {
        if (cartItem == null) {
            return;
        }

        if (flashSaleItem == null) {
            clearFlashSaleLimit(cartItem);
            return;
        }

        LimitResult result = checkCanSetQuantityInternal(
                conn,
                userId,
                cart,
                targetCartKey,
                cartItem.getProductId(),
                cartItem.getQuantity(),
                flashSaleItem
        );

        applyLimitToCartItem(cartItem, result);
    }

    private void applyLimitToCartItem(CartItem cartItem, LimitResult result) {
        if (cartItem == null || result == null || !result.isLimitedProduct()) {
            if (cartItem != null) {
                clearFlashSaleLimit(cartItem);
            }
            return;
        }

        cartItem.setFlashSaleItem(true);
        cartItem.setFlashSaleId(result.getFlashSaleId());
        cartItem.setFlashSaleItemId(result.getFlashSaleItemId());
        cartItem.setMaxQuantityPerUser(result.getMaxQuantityPerUser());
        cartItem.setPurchasedFlashSaleQuantity(result.getPurchasedQuantity());
        cartItem.setRemainingFlashSaleLimit(result.getRemainingAllowedQuantity());
        cartItem.setFlashSaleLimitMessage(result.getMessage());
    }

    private void clearFlashSaleLimit(CartItem cartItem) {
        cartItem.setFlashSaleItem(false);
        cartItem.setFlashSaleId(0);
        cartItem.setFlashSaleItemId(0);
        cartItem.setMaxQuantityPerUser(0);
        cartItem.setPurchasedFlashSaleQuantity(0);
        cartItem.setRemainingFlashSaleLimit(0);
        cartItem.setFlashSaleLimitMessage(null);
    }
}

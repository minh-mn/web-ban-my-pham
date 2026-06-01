package com.webshop.app.model;

import java.math.BigDecimal;

public class CartItem {

    private int orderId;

    private int productId;
    private String title;

    private int quantity;

    /*
     * price = giá đang bán hiện tại.
     * Nếu sản phẩm có giảm giá thì price là giá sau giảm.
     */
    private BigDecimal price;

    /*
     * originalPrice = giá gốc trước giảm.
     * Dùng để hiển thị giá gốc gạch ngang trong giỏ hàng.
     */
    private BigDecimal originalPrice;

    private String imageUrl;
    private int stock;

    // ===== VARIANT FIELDS =====
    // cartKey giúp phân biệt cùng 1 sản phẩm nhưng chọn size/loại khác nhau.
    // Ví dụ: 51:2, 51:3, 51:0
    private String cartKey;
    private int variantId;
    private String variantSize;
    private String variantType;
    private String variantName;
    private BigDecimal variantExtraPrice;

    // ===== ISSUE 139: FLASH SALE PURCHASE LIMIT =====
    /*
     * Đánh dấu item này có thuộc Flash Sale đang hoạt động hay không.
     */
    private boolean flashSaleItem;

    /*
     * ID của flash sale và flash sale item để service/DAO kiểm tra giới hạn.
     */
    private int flashSaleId;
    private int flashSaleItemId;

    /*
     * Giới hạn mua tối đa cho mỗi khách trong khung Flash Sale.
     * Ví dụ: 2 nghĩa là mỗi khách chỉ được mua tối đa 2 sản phẩm này.
     */
    private int maxQuantityPerUser;

    /*
     * Số lượng user đã mua trước đó trong khung Flash Sale.
     * Dùng để hiển thị hoặc tính phần còn được mua thêm.
     */
    private int purchasedFlashSaleQuantity;

    /*
     * Số lượng tối đa user còn được giữ trong giỏ/checkout ở thời điểm kiểm tra.
     * Nếu service không set thì helper sẽ tự tính theo maxQuantityPerUser - purchasedFlashSaleQuantity.
     */
    private int remainingFlashSaleLimit = -1;

    /*
     * Message cảnh báo dùng cho JSP.
     */
    private String flashSaleLimitMessage;

    // ===== BUSINESS =====

    /**
     * Tổng tiền theo giá đang bán.
     * Nếu sản phẩm có giảm giá thì đây là tổng tiền sau giảm.
     */
    public BigDecimal getSubtotal() {
        return getSafePrice().multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Tổng tiền theo giá gốc.
     * Dùng để hiện giá gốc gạch ngang ở cột tạm tính.
     */
    public BigDecimal getOriginalSubtotal() {
        return getSafeOriginalPrice().multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Kiểm tra sản phẩm có đang được giảm giá hay không.
     */
    public boolean isDiscounted() {
        return originalPrice != null
                && price != null
                && originalPrice.compareTo(price) > 0;
    }

    public boolean getDiscounted() {
        return isDiscounted();
    }

    public boolean isHasVariant() {
        return variantId > 0;
    }

    public boolean getHasVariant() {
        return isHasVariant();
    }

    public String getVariantDisplayName() {
        if (variantName != null && !variantName.isBlank()) {
            return variantName;
        }

        boolean hasSize = variantSize != null && !variantSize.isBlank();
        boolean hasType = variantType != null && !variantType.isBlank();

        if (hasSize && hasType) {
            return variantSize + " - " + variantType;
        }

        if (hasSize) {
            return variantSize;
        }

        if (hasType) {
            return variantType;
        }

        return "Mặc định";
    }

    /*
     * Issue 139:
     * Số lượng còn được phép mua thêm theo giới hạn Flash Sale.
     */
    public int getEffectiveRemainingFlashSaleLimit() {
        if (!flashSaleItem || maxQuantityPerUser <= 0) {
            return Integer.MAX_VALUE;
        }

        if (remainingFlashSaleLimit >= 0) {
            return remainingFlashSaleLimit;
        }

        return Math.max(0, maxQuantityPerUser - purchasedFlashSaleQuantity);
    }

    public boolean isFlashSaleLimitReached() {
        return flashSaleItem
                && maxQuantityPerUser > 0
                && quantity >= getEffectiveRemainingFlashSaleLimit();
    }

    public boolean getFlashSaleLimitReached() {
        return isFlashSaleLimitReached();
    }

    public boolean isReachedFlashSaleLimit() {
        return isFlashSaleLimitReached();
    }

    public boolean getReachedFlashSaleLimit() {
        return isFlashSaleLimitReached();
    }

    public boolean isCanIncreaseQuantity() {
        if (stock > 0 && quantity >= stock) {
            return false;
        }

        if (flashSaleItem && maxQuantityPerUser > 0) {
            return quantity < getEffectiveRemainingFlashSaleLimit();
        }

        return true;
    }

    public boolean getCanIncreaseQuantity() {
        return isCanIncreaseQuantity();
    }

    public String getFlashSaleLimitLabel() {
        if (!flashSaleItem || maxQuantityPerUser <= 0) {
            return "";
        }

        return "Flash Sale: giới hạn " + maxQuantityPerUser + " sản phẩm/khách";
    }

    public String getFlashSaleLimitMessage() {
        if (flashSaleLimitMessage != null && !flashSaleLimitMessage.isBlank()) {
            return flashSaleLimitMessage;
        }

        if (!flashSaleItem || maxQuantityPerUser <= 0) {
            return "";
        }

        int remaining = getEffectiveRemainingFlashSaleLimit();

        if (remaining <= 0) {
            return "Bạn đã đạt giới hạn mua Flash Sale cho sản phẩm này.";
        }

        if (quantity >= remaining) {
            return "Bạn đã đạt giới hạn " + maxQuantityPerUser + " sản phẩm/khách trong Flash Sale.";
        }

        return getFlashSaleLimitLabel();
    }

    public boolean getHasFlashSaleLimitMessage() {
        String message = getFlashSaleLimitMessage();
        return message != null && !message.isBlank();
    }

    private BigDecimal getSafePrice() {
        return price != null ? price : BigDecimal.ZERO;
    }

    private BigDecimal getSafeOriginalPrice() {
        /*
         * Nếu chưa có originalPrice thì lấy price hiện tại.
         * Như vậy JSP gọi originalSubtotal sẽ không bị lỗi.
         */
        if (originalPrice != null) {
            return originalPrice;
        }

        return getSafePrice();
    }

    // ===== GET / SET =====

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = Math.max(orderId, 0);
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = Math.max(productId, 0);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(quantity, 0);
    }

    public BigDecimal getPrice() {
        return getSafePrice();
    }

    public void setPrice(BigDecimal price) {
        this.price = price != null ? price : BigDecimal.ZERO;
    }

    public BigDecimal getOriginalPrice() {
        return getSafeOriginalPrice();
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl == null ? null : imageUrl.trim();
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = Math.max(stock, 0);
    }

    public String getCartKey() {
        return cartKey;
    }

    public void setCartKey(String cartKey) {
        this.cartKey = cartKey == null ? null : cartKey.trim();
    }

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = Math.max(variantId, 0);
    }

    public String getVariantSize() {
        return variantSize;
    }

    public void setVariantSize(String variantSize) {
        this.variantSize = variantSize == null ? null : variantSize.trim();
    }

    public String getVariantType() {
        return variantType;
    }

    public void setVariantType(String variantType) {
        this.variantType = variantType == null ? null : variantType.trim();
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName == null ? null : variantName.trim();
    }

    public BigDecimal getVariantExtraPrice() {
        return variantExtraPrice != null ? variantExtraPrice : BigDecimal.ZERO;
    }

    public void setVariantExtraPrice(BigDecimal variantExtraPrice) {
        this.variantExtraPrice = variantExtraPrice;
    }

    // ===== ISSUE 139 GETTER / SETTER =====

    public boolean isFlashSaleItem() {
        return flashSaleItem;
    }

    public boolean getFlashSaleItem() {
        return flashSaleItem;
    }

    /*
     * Alias nếu JSP/service gọi item.flashSale.
     */
    public boolean isFlashSale() {
        return flashSaleItem;
    }

    public boolean getFlashSale() {
        return flashSaleItem;
    }

    public void setFlashSaleItem(boolean flashSaleItem) {
        this.flashSaleItem = flashSaleItem;
    }

    public void setFlashSale(boolean flashSale) {
        this.flashSaleItem = flashSale;
    }

    public int getFlashSaleId() {
        return flashSaleId;
    }

    public void setFlashSaleId(int flashSaleId) {
        this.flashSaleId = Math.max(flashSaleId, 0);
    }

    public int getFlashSaleItemId() {
        return flashSaleItemId;
    }

    public void setFlashSaleItemId(int flashSaleItemId) {
        this.flashSaleItemId = Math.max(flashSaleItemId, 0);
    }

    public int getMaxQuantityPerUser() {
        return maxQuantityPerUser;
    }

    public void setMaxQuantityPerUser(int maxQuantityPerUser) {
        this.maxQuantityPerUser = Math.max(maxQuantityPerUser, 0);
    }

    /*
     * Alias nếu code đang dùng tên flashSaleLimitPerUser.
     */
    public int getFlashSaleLimitPerUser() {
        return maxQuantityPerUser;
    }

    public void setFlashSaleLimitPerUser(int flashSaleLimitPerUser) {
        setMaxQuantityPerUser(flashSaleLimitPerUser);
    }

    public int getPurchasedFlashSaleQuantity() {
        return purchasedFlashSaleQuantity;
    }

    public void setPurchasedFlashSaleQuantity(int purchasedFlashSaleQuantity) {
        this.purchasedFlashSaleQuantity = Math.max(purchasedFlashSaleQuantity, 0);
    }

    public int getRemainingFlashSaleLimit() {
        int value = getEffectiveRemainingFlashSaleLimit();
        return value == Integer.MAX_VALUE ? 0 : value;
    }

    public void setRemainingFlashSaleLimit(int remainingFlashSaleLimit) {
        this.remainingFlashSaleLimit = Math.max(remainingFlashSaleLimit, 0);
    }

    public void setFlashSaleLimitMessage(String flashSaleLimitMessage) {
        this.flashSaleLimitMessage = flashSaleLimitMessage;
    }
}

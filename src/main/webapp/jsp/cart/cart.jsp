<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Map" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%
    /*
     * FIX GIỎ HÀNG TRỐNG SAU KHI THÊM SẢN PHẨM
     *
     * CartUtil của project đang lưu giỏ bằng session key: "CART".
     * CartViewServlet đang set request attribute: "cart".
     * Vì vậy JSP phải đọc theo thứ tự:
     * 1. requestScope.cartItems  (nếu servlet cũ có set list)
     * 2. requestScope.cart       (CartViewServlet hiện tại set Map<String, CartItem>)
     * 3. sessionScope.CART       (CartUtil.CART_SESSION_KEY)
     * 4. sessionScope.cart       (fallback cho code cũ)
     */
    Object rawCart = request.getAttribute("cartItems");

    if (rawCart == null ||
            (rawCart instanceof Collection && ((Collection<?>) rawCart).isEmpty()) ||
            (rawCart instanceof Map && ((Map<?, ?>) rawCart).isEmpty())) {
        rawCart = request.getAttribute("cart");
    }

    if (rawCart == null ||
            (rawCart instanceof Collection && ((Collection<?>) rawCart).isEmpty()) ||
            (rawCart instanceof Map && ((Map<?, ?>) rawCart).isEmpty())) {
        rawCart = session.getAttribute("CART");
    }

    if (rawCart == null ||
            (rawCart instanceof Collection && ((Collection<?>) rawCart).isEmpty()) ||
            (rawCart instanceof Map && ((Map<?, ?>) rawCart).isEmpty())) {
        rawCart = session.getAttribute("cart");
    }

    if (rawCart instanceof Map) {
        request.setAttribute("cartItemsView", ((Map<?, ?>) rawCart).values());
    } else {
        request.setAttribute("cartItemsView", rawCart);
    }
%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="cartItems" value="${requestScope.cartItemsView}" />

<main class="cart-section">
    <div class="main-container">
        <h1 class="cart-title">Giỏ hàng của bạn</h1>

        <c:if test="${not empty cartError or not empty flashSaleLimitError or param.flashLimit == '1'}">
            <section class="cart-alert cart-alert--warning">
                <strong>Thông báo giỏ hàng</strong>
                <span>
                    <c:choose>
                        <c:when test="${not empty flashSaleLimitError}">
                            <c:out value="${flashSaleLimitError}" />
                        </c:when>
                        <c:when test="${not empty cartError}">
                            <c:out value="${cartError}" />
                        </c:when>
                        <c:when test="${not empty param.message}">
                            <c:out value="${param.message}" />
                        </c:when>
                        <c:otherwise>
                            Một số sản phẩm Flash Sale đã đạt giới hạn mua của mỗi khách.
                        </c:otherwise>
                    </c:choose>
                </span>
            </section>
        </c:if>

        <c:choose>
            <c:when test="${empty cartItems}">
                <section class="cart-empty">
                    <div class="cart-empty-icon">🛒</div>
                    <h2>Giỏ hàng của bạn đang trống</h2>
                    <p>Bạn chưa có sản phẩm nào trong giỏ hàng. Hãy quay lại trang sản phẩm để tiếp tục mua sắm.</p>

                    <div class="cart-empty-actions">
                        <a href="${ctx}/products" class="btn-empty-primary">Xem sản phẩm</a>
                        <a href="${ctx}/home" class="btn-empty-secondary">Về trang chủ</a>
                    </div>
                </section>
            </c:when>

            <c:otherwise>
                <c:set var="calcSubtotal" value="0" />
                <c:set var="calcDiscount" value="0" />
                <c:set var="calcSelectedCount" value="0" />

                <section class="cart-layout">
                    <div class="cart-table-wrap">
                        <table class="cart-table">
                            <thead>
                            <tr>
                                <th class="cart-select-col">
                                    <input type="checkbox" id="cartCheckAll" class="cart-check-all" checked />
                                </th>
                                <th>Sản phẩm</th>
                                <th>Phân loại</th>
                                <th>Đơn giá</th>
                                <th>Số lượng</th>
                                <th>Tạm tính</th>
                                <th></th>
                            </tr>
                            </thead>

                            <tbody>
                            <c:forEach var="item" items="${cartItems}">
                                <c:set var="itemPrice" value="${empty item.price ? 0 : item.price}" />
                                <c:set var="itemOriginalPrice" value="${empty item.originalPrice ? itemPrice : item.originalPrice}" />
                                <c:set var="itemQuantity" value="${empty item.quantity ? 1 : item.quantity}" />
                                <c:set var="itemSubtotal" value="${itemPrice * itemQuantity}" />
                                <c:set var="itemOriginalSubtotal" value="${itemOriginalPrice * itemQuantity}" />
                                <c:set var="itemDiscount" value="${itemOriginalSubtotal > itemSubtotal ? itemOriginalSubtotal - itemSubtotal : 0}" />
                                <c:set var="itemKey" value="${empty item.cartKey ? item.productId : item.cartKey}" />
                                <c:set var="itemTitle" value="${empty item.title ? item.productName : item.title}" />
                                <c:set var="itemImage" value="${empty item.imageUrl ? item.image : item.imageUrl}" />

                                <c:url var="decreaseUrl" value="/cart/decrease">
                                    <c:param name="key" value="${itemKey}" />
                                    <c:param name="productId" value="${item.productId}" />
                                </c:url>

                                <c:url var="increaseUrl" value="/cart/increase">
                                    <c:param name="key" value="${itemKey}" />
                                    <c:param name="productId" value="${item.productId}" />
                                </c:url>

                                <c:url var="removeUrl" value="/cart/remove">
                                    <c:param name="key" value="${itemKey}" />
                                    <c:param name="productId" value="${item.productId}" />
                                </c:url>

                                <c:set var="calcSubtotal" value="${calcSubtotal + itemOriginalSubtotal}" />
                                <c:set var="calcDiscount" value="${calcDiscount + itemDiscount}" />
                                <c:set var="calcSelectedCount" value="${calcSelectedCount + 1}" />

                                <tr class="cart-row ${item.flashSaleItem ? 'cart-row--flash-sale' : ''} ${item.flashSaleLimitReached ? 'cart-row--flash-limit' : ''}"
                                    data-subtotal="${itemOriginalSubtotal}"
                                    data-discount="${itemDiscount}"
                                    data-flash-sale="${item.flashSaleItem}"
                                    data-flash-limit-reached="${item.flashSaleLimitReached}">
                                    <td class="cart-select">
                                        <input type="checkbox"
                                               class="cart-item-checkbox"
                                               name="selectedKeys"
                                               value="${itemKey}"
                                               checked />
                                    </td>

                                    <td class="cart-product">
                                        <div class="cart-product-info">
                                            <div class="cart-img-box">
                                                <c:choose>
                                                    <c:when test="${not empty itemImage}">
                                                        <c:choose>
                                                            <c:when test="${itemImage.startsWith('http') or itemImage.startsWith(ctx)}">
                                                                <img src="${itemImage}" alt="${itemTitle}" />
                                                            </c:when>
                                                            <c:when test="${itemImage.startsWith('/')}">
                                                                <img src="${ctx}${itemImage}" alt="${itemTitle}" />
                                                            </c:when>
                                                            <c:otherwise>
                                                                <img src="${ctx}/${itemImage}" alt="${itemTitle}" />
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <img src="${ctx}/assets/images/no-image.png" alt="No image" />
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>

                                            <div class="cart-product-meta">
                                                <div class="cart-product-title">
                                                    <c:out value="${itemTitle}" />
                                                </div>
                                                <div class="cart-product-id">
                                                    Mã SP: <c:out value="${item.productId}" />
                                                </div>

                                                <c:if test="${item.flashSaleItem}">
                                                    <div class="cart-flash-badge">
                                                        Flash Sale
                                                    </div>
                                                </c:if>
                                            </div>
                                        </div>
                                    </td>

                                    <td class="cart-variant">
                                        <span class="variant-text">
                                            <c:out value="${empty item.variantDisplayName ? 'Mặc định' : item.variantDisplayName}" />
                                        </span>
                                    </td>

                                    <td class="cart-price">
                                        <fmt:formatNumber value="${itemPrice}" type="number" groupingUsed="true" />đ
                                    </td>

                                    <td class="cart-quantity">
                                        <div class="cart-quantity-inner">
                                            <div class="quantity-box">
                                                <a class="qty-btn" href="${decreaseUrl}">-</a>
                                                <span class="qty-value"><c:out value="${itemQuantity}" /></span>

                                                <c:choose>
                                                    <c:when test="${item.flashSaleLimitReached or not item.canIncreaseQuantity}">
                                                        <span class="qty-btn qty-btn--disabled"
                                                              title="${empty item.flashSaleLimitMessage ? 'Không thể tăng thêm số lượng' : item.flashSaleLimitMessage}">
                                                            +
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <a class="qty-btn" href="${increaseUrl}">+</a>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>

                                            <c:if test="${not empty item.stock}">
                                                <span class="stock-note">Còn ${item.stock}</span>
                                            </c:if>

                                            <c:if test="${item.flashSaleItem}">
                                                <span class="cart-flash-limit-note ${item.flashSaleLimitReached ? 'is-reached' : ''}">
                                                    <c:choose>
                                                        <c:when test="${not empty item.flashSaleLimitMessage}">
                                                            <c:out value="${item.flashSaleLimitMessage}" />
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:out value="${item.flashSaleLimitLabel}" />
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </c:if>
                                        </div>
                                    </td>

                                    <td class="cart-subtotal">
                                        <span class="subtotal-current">
                                            <fmt:formatNumber value="${itemSubtotal}" type="number" groupingUsed="true" />đ
                                        </span>
                                        <c:if test="${itemOriginalSubtotal > itemSubtotal}">
                                            <span class="subtotal-original">
                                                <fmt:formatNumber value="${itemOriginalSubtotal}" type="number" groupingUsed="true" />đ
                                            </span>
                                        </c:if>
                                    </td>

                                    <td class="cart-remove">
                                        <a href="${removeUrl}" class="remove-btn" title="Xóa sản phẩm" aria-label="Xóa sản phẩm">
                                            <span class="remove-icon" aria-hidden="true">×</span>
                                            <span class="sr-only">Xóa sản phẩm</span>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <c:set var="summarySubtotal" value="${empty requestScope.subtotal ? calcSubtotal : requestScope.subtotal}" />
                    <c:set var="summaryDiscount" value="${empty requestScope.discountAmount ? calcDiscount : requestScope.discountAmount}" />
                    <c:set var="summaryTotal" value="${empty requestScope.totalAmount ? summarySubtotal - summaryDiscount : requestScope.totalAmount}" />
                    <c:set var="summarySelectedCount" value="${empty requestScope.selectedCount ? calcSelectedCount : requestScope.selectedCount}" />

                    <aside class="order-summary-card">
                        <h2>Thông tin đơn hàng</h2>

                        <div class="order-summary-lines">
                            <div class="order-summary-row">
                                <span>Tạm tính:</span>
                                <strong id="summarySubtotal" class="summary-value-dark">
                                    <fmt:formatNumber value="${summarySubtotal}" type="number" groupingUsed="true" />đ
                                </strong>
                            </div>

                            <div class="order-summary-row">
                                <span>Giá giảm:</span>
                                <strong id="summaryDiscount" class="summary-value-dark">
                                    <fmt:formatNumber value="${summaryDiscount}" type="number" groupingUsed="true" />đ
                                </strong>
                            </div>

                            <div class="order-summary-row order-summary-total">
                                <span>Tổng cộng:</span>
                                <strong id="summaryTotal" class="summary-value-total">
                                    <fmt:formatNumber value="${summaryTotal}" type="number" groupingUsed="true" />đ
                                </strong>
                            </div>
                        </div>

                        <p class="cart-select-note" id="cartSelectNote">
                            Đã chọn <span id="selectedCountText">${summarySelectedCount}</span> sản phẩm để thanh toán.
                        </p>

                        <div class="summary-actions">
                            <form id="checkoutForm" action="${ctx}/cart/select-checkout" method="post">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                                <div id="selectedInputs"></div>
                                <button type="submit" class="btn-checkout" id="checkoutBtn">
                                    Thanh toán ngay
                                </button>
                            </form>

                            <a href="${ctx}/products" class="btn-back-to-shop">← Tiếp tục mua hàng</a>
                        </div>
                    </aside>
                </section>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<script>
    (function () {
        const checkAll = document.getElementById('cartCheckAll');
        const itemChecks = Array.from(document.querySelectorAll('.cart-item-checkbox'));
        const subtotalEl = document.getElementById('summarySubtotal');
        const discountEl = document.getElementById('summaryDiscount');
        const totalEl = document.getElementById('summaryTotal');
        const countText = document.getElementById('selectedCountText');
        const checkoutBtn = document.getElementById('checkoutBtn');
        const selectedInputs = document.getElementById('selectedInputs');

        if (!itemChecks.length) return;

        function formatMoney(value) {
            return Math.max(0, value).toLocaleString('vi-VN') + 'đ';
        }

        function updateSummary() {
            let subtotal = 0;
            let discount = 0;
            let count = 0;

            selectedInputs.innerHTML = '';

            itemChecks.forEach(function (checkbox) {
                if (!checkbox.checked) return;

                const row = checkbox.closest('.cart-row');
                subtotal += Number(row.dataset.subtotal || 0);
                discount += Number(row.dataset.discount || 0);
                count++;

                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'selectedKeys';
                input.value = checkbox.value;
                selectedInputs.appendChild(input);
            });

            subtotalEl.textContent = formatMoney(subtotal);
            discountEl.textContent = formatMoney(discount);
            totalEl.textContent = formatMoney(subtotal - discount);
            countText.textContent = count;

            if (checkoutBtn) {
                checkoutBtn.disabled = count === 0;
                checkoutBtn.classList.toggle('disabled', count === 0);
            }

            if (checkAll) {
                checkAll.checked = count === itemChecks.length;
                checkAll.indeterminate = count > 0 && count < itemChecks.length;
            }
        }

        if (checkAll) {
            checkAll.addEventListener('change', function () {
                itemChecks.forEach(function (checkbox) {
                    checkbox.checked = checkAll.checked;
                });
                updateSummary();
            });
        }

        itemChecks.forEach(function (checkbox) {
            checkbox.addEventListener('change', updateSummary);
        });

        updateSummary();
    })();
</script>

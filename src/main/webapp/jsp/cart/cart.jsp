<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<section class="cart-section">
    <div class="container">

        <h1 class="cart-title">Giỏ hàng của bạn</h1>

        <c:if test="${param.selectRequired == '1'}">
            <div class="cart-alert">
                Vui lòng chọn ít nhất một sản phẩm để tiếp tục thanh toán.
            </div>
        </c:if>

        <c:choose>
            <%-- ================= GIỎ HÀNG RỖNG ================= --%>
            <c:when test="${empty cart}">
                <div class="cart-empty">
                    <div class="cart-empty-icon">
                        🛒
                    </div>

                    <h2>Giỏ hàng của bạn đang trống</h2>

                    <p>
                        Bạn chưa có sản phẩm nào trong giỏ hàng.
                        Hãy quay lại trang sản phẩm để tiếp tục mua sắm.
                    </p>

                    <div class="cart-empty-actions">
                        <a href="${pageContext.request.contextPath}/products"
                           class="btn-empty-primary">
                            Xem sản phẩm
                        </a>

                        <a href="${pageContext.request.contextPath}/home"
                           class="btn-empty-secondary">
                            Về trang chủ
                        </a>
                    </div>
                </div>
            </c:when>

            <%-- ================= GIỎ HÀNG CÓ SẢN PHẨM ================= --%>
            <c:otherwise>
                <form id="checkoutSelectForm"
                      method="post"
                      action="${pageContext.request.contextPath}/cart/select-checkout"></form>

                <div class="cart-layout">

                    <div class="cart-table-wrap">
                        <table class="cart-table">
                            <thead>
                            <tr>
                                <th class="cart-select-col">
                                    <input type="checkbox"
                                           id="selectAllCartItems"
                                           class="cart-check-all"
                                           checked>
                                </th>
                                <th>Sản phẩm</th>
                                <th>Biến thể</th>
                                <th>Đơn giá</th>
                                <th>Số lượng</th>
                                <th>Tạm tính</th>
                                <th></th>
                            </tr>
                            </thead>

                            <tbody>
                            <c:forEach var="entry" items="${cart}">
                                <c:set var="item" value="${entry.value}" />
                                <c:set var="cartKey" value="${entry.key}" />
                                <c:set var="options" value="${variantOptions[item.productId]}" />

                                <fmt:formatNumber var="itemSubtotalRaw"
                                                  value="${item.subtotal}"
                                                  pattern="0"
                                                  groupingUsed="false" />

                                <fmt:formatNumber var="itemOriginalSubtotalRaw"
                                                  value="${item.originalSubtotal}"
                                                  pattern="0"
                                                  groupingUsed="false" />

                                <tr>
                                        <%-- CHỌN SẢN PHẨM --%>
                                    <td class="cart-select">
                                        <input type="checkbox"
                                               class="cart-item-checkbox"
                                               form="checkoutSelectForm"
                                               name="selectedKeys"
                                               value="${cartKey}"
                                               data-subtotal="${itemSubtotalRaw}"
                                               data-original-subtotal="${itemOriginalSubtotalRaw}"
                                               checked>
                                    </td>

                                        <%-- SẢN PHẨM --%>
                                    <td class="cart-product">
                                        <div class="cart-product-info">
                                            <div class="cart-img-box">
                                                <c:choose>
                                                    <c:when test="${not empty item.imageUrl}">
                                                        <img src="${pageContext.request.contextPath}${item.imageUrl}"
                                                             alt="${fn:escapeXml(item.title)}"
                                                             onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/assets/images/default-product.jpg';">
                                                    </c:when>

                                                    <c:otherwise>
                                                        <img src="${pageContext.request.contextPath}/assets/images/default-product.jpg"
                                                             alt="default">
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>

                                            <div class="cart-product-meta">
                                                <div class="cart-product-title">
                                                    <c:out value="${item.title}" />
                                                </div>

                                                <div class="cart-product-id">
                                                    Mã SP: ${item.productId}
                                                </div>
                                            </div>
                                        </div>
                                    </td>

                                        <%-- BIẾN THỂ --%>
                                    <td class="cart-variant">
                                        <c:choose>
                                            <c:when test="${not empty options}">
                                                <form method="post"
                                                      action="${pageContext.request.contextPath}/cart/update-variant"
                                                      class="variant-update-form">

                                                    <input type="hidden" name="productId" value="${item.productId}">
                                                    <input type="hidden" name="key" value="${cartKey}">

                                                    <select name="variantId"
                                                            class="cart-variant-select"
                                                            onchange="this.form.submit()">

                                                        <c:forEach var="v" items="${options}">
                                                            <option value="${v.id}"
                                                                ${v.id == item.variantId ? 'selected' : ''}
                                                                ${v.stock <= 0 ? 'disabled' : ''}>
                                                                <c:out value="${v.displayName}" />

                                                                <c:if test="${v.extraPrice > 0}">
                                                                    - +<fmt:formatNumber value="${v.extraPrice}" type="number" groupingUsed="true" /> đ
                                                                </c:if>

                                                                - Còn ${v.stock}
                                                            </option>
                                                        </c:forEach>
                                                    </select>
                                                </form>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="variant-text">
                                                    <c:out value="${empty item.variantDisplayName ? 'Mặc định' : item.variantDisplayName}" />
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                        <%-- ĐƠN GIÁ --%>
                                    <td class="cart-price">
                                        <fmt:formatNumber value="${item.price}" type="number" groupingUsed="true" /> đ
                                    </td>

                                        <%-- SỐ LƯỢNG --%>
                                    <td class="cart-quantity">
                                        <div class="cart-quantity-inner">
                                            <div class="quantity-box">
                                                <a class="qty-btn"
                                                   href="${pageContext.request.contextPath}/cart/decrease?productId=${item.productId}&key=${cartKey}"
                                                   aria-label="Giảm số lượng">
                                                    -
                                                </a>

                                                <span class="qty-value">${item.quantity}</span>

                                                <a class="qty-btn"
                                                   href="${pageContext.request.contextPath}/cart/increase?productId=${item.productId}&key=${cartKey}"
                                                   aria-label="Tăng số lượng">
                                                    +
                                                </a>
                                            </div>

                                            <div class="stock-note">
                                                Còn ${item.stock}
                                            </div>
                                        </div>
                                    </td>

                                        <%-- TẠM TÍNH --%>
                                    <td class="cart-subtotal ${item.discounted ? 'has-discount' : ''}">
                                        <strong class="subtotal-current">
                                            <fmt:formatNumber value="${item.subtotal}" type="number" groupingUsed="true" /> đ
                                        </strong>

                                        <c:if test="${item.discounted}">
                                            <span class="subtotal-original">
                                                <fmt:formatNumber value="${item.originalSubtotal}" type="number" groupingUsed="true" /> đ
                                            </span>
                                        </c:if>
                                    </td>

                                        <%-- XÓA --%>
                                    <td class="cart-remove">
                                        <a href="${pageContext.request.contextPath}/cart/remove?productId=${item.productId}&key=${cartKey}"
                                           class="remove-btn"
                                           title="Xóa sản phẩm"
                                           aria-label="Xóa sản phẩm"
                                           onclick="return confirm('Xóa sản phẩm này khỏi giỏ hàng?');">
                                            &times;
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>

                        <%-- THÔNG TIN ĐƠN HÀNG --%>
                    <aside class="cart-summary order-summary-card">
                        <h2>Thông tin đơn hàng</h2>

                        <div class="order-summary-lines">
                            <div class="order-summary-row">
                                <span>Tạm tính:</span>
                                <strong id="selectedOriginalTotal">0đ</strong>
                            </div>

                            <div class="order-summary-row">
                                <span>Giá giảm:</span>
                                <strong id="selectedCartDiscount">0đ</strong>
                            </div>

                            <div class="order-summary-row order-summary-total">
                                <span>Tổng cộng:</span>
                                <strong id="selectedCartTotal">0đ</strong>
                            </div>
                        </div>

                        <div class="cart-select-note" id="selectedCartNote">
                            Chọn sản phẩm muốn mua rồi bấm thanh toán.
                        </div>

                        <div class="summary-actions">
                            <button type="submit"
                                    form="checkoutSelectForm"
                                    class="btn-checkout"
                                    id="selectedCheckoutBtn">
                                Thanh toán ngay
                            </button>

                            <a href="${pageContext.request.contextPath}/products" class="btn-back-to-shop">
                                ← Tiếp tục mua hàng
                            </a>
                        </div>
                    </aside>

                </div>
            </c:otherwise>
        </c:choose>

    </div>
</section>

<script>
    (function () {
        function ready(fn) {
            if (document.readyState === "loading") {
                document.addEventListener("DOMContentLoaded", fn);
            } else {
                fn();
            }
        }

        ready(function () {
            const selectAll = document.getElementById("selectAllCartItems");
            const itemCheckboxes = Array.from(document.querySelectorAll(".cart-item-checkbox"));
            const checkoutForm = document.getElementById("checkoutSelectForm");

            const originalTotalEl = document.getElementById("selectedOriginalTotal");
            const discountEl = document.getElementById("selectedCartDiscount");
            const totalEl = document.getElementById("selectedCartTotal");
            const noteEl = document.getElementById("selectedCartNote");
            const checkoutBtn = document.getElementById("selectedCheckoutBtn");

            function parseSubtotal(value) {
                if (value === null || value === undefined) {
                    return 0;
                }

                const number = Number(String(value).trim());
                return Number.isFinite(number) ? number : 0;
            }

            function formatVnd(value) {
                return new Intl.NumberFormat("en-US").format(Math.round(value)) + "đ";
            }

            function getCheckedItems() {
                return itemCheckboxes.filter(function (checkbox) {
                    return checkbox.checked;
                });
            }

            function syncSelectAll() {
                if (!selectAll) {
                    return;
                }

                const checkedCount = getCheckedItems().length;

                selectAll.indeterminate = false;
                selectAll.checked =
                    itemCheckboxes.length > 0 && checkedCount === itemCheckboxes.length;
            }

            function updateSelectedTotal() {
                const checkedItems = getCheckedItems();

                let selectedSubtotal = 0;
                let selectedOriginalTotal = 0;

                checkedItems.forEach(function (checkbox) {
                    const subtotal = parseSubtotal(checkbox.dataset.subtotal);
                    const originalSubtotal = parseSubtotal(checkbox.dataset.originalSubtotal);

                    selectedSubtotal += subtotal;
                    selectedOriginalTotal += originalSubtotal > 0 ? originalSubtotal : subtotal;
                });

                const discount = Math.max(selectedOriginalTotal - selectedSubtotal, 0);

                if (originalTotalEl) {
                    originalTotalEl.textContent = formatVnd(selectedOriginalTotal);
                }

                if (discountEl) {
                    discountEl.textContent = formatVnd(discount);
                }

                if (totalEl) {
                    totalEl.textContent = formatVnd(selectedSubtotal);
                }

                if (noteEl) {
                    if (checkedItems.length === 0) {
                        noteEl.textContent = "Vui lòng chọn ít nhất một sản phẩm để thanh toán.";
                    } else {
                        noteEl.textContent =
                            "Đã chọn " + checkedItems.length + " sản phẩm để thanh toán.";
                    }
                }

                if (checkoutBtn) {
                    const disabled = checkedItems.length === 0;
                    checkoutBtn.disabled = disabled;
                    checkoutBtn.classList.toggle("disabled", disabled);
                }

                syncSelectAll();
            }

            if (selectAll) {
                selectAll.addEventListener("change", function () {
                    itemCheckboxes.forEach(function (checkbox) {
                        checkbox.checked = selectAll.checked;
                    });

                    updateSelectedTotal();
                });
            }

            itemCheckboxes.forEach(function (checkbox) {
                checkbox.addEventListener("change", updateSelectedTotal);
            });

            if (checkoutForm) {
                checkoutForm.addEventListener("submit", function (event) {
                    if (getCheckedItems().length === 0) {
                        event.preventDefault();
                        alert("Vui lòng chọn ít nhất một sản phẩm để thanh toán.");
                    }
                });
            }

            updateSelectedTotal();
        });
    })();
</script>
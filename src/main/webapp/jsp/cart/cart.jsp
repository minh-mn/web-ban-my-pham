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
            <c:when test="${empty cart}">
                <div class="cart-empty">
                    <p>Giỏ hàng của bạn đang trống.</p>

                    <a href="${pageContext.request.contextPath}/products" class="btn-continue">
                        Tiếp tục mua sắm
                    </a>
                </div>
            </c:when>

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

                                <!-- Format subtotal thành số nguyên sạch, không có dấu phẩy/chấm -->
                                <fmt:formatNumber var="itemSubtotalRaw"
                                                  value="${item.subtotal}"
                                                  pattern="0"
                                                  groupingUsed="false" />

                                <tr>
                                    <!-- CHỌN SẢN PHẨM -->
                                    <td class="cart-select">
                                        <input type="checkbox"
                                               class="cart-item-checkbox"
                                               form="checkoutSelectForm"
                                               name="selectedKeys"
                                               value="${cartKey}"
                                               data-subtotal="${itemSubtotalRaw}"
                                               checked>
                                    </td>

                                    <!-- SẢN PHẨM -->
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

                                    <!-- BIẾN THỂ -->
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
                                                                    - +<fmt:formatNumber value="${v.extraPrice}" type="number" groupingUsed="true" /> ₫
                                                                </c:if>

                                                                - Còn ${v.stock}
                                                            </option>
                                                        </c:forEach>
                                                    </select>
                                                </form>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="variant-text">
                                                    <c:out value="${item.variantDisplayName}" />
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <!-- ĐƠN GIÁ -->
                                    <td class="cart-price">
                                        <fmt:formatNumber value="${item.price}" type="number" groupingUsed="true" /> ₫
                                    </td>

                                    <!-- SỐ LƯỢNG -->
                                    <td class="cart-quantity">
                                        <div class="cart-quantity-inner">
                                            <div class="quantity-box">
                                                <a class="qty-btn"
                                                   href="${pageContext.request.contextPath}/cart/decrease?productId=${item.productId}&key=${cartKey}"
                                                   aria-label="Giảm số lượng">
                                                    -
                                                </a>

                                                <span class="qty-value">
                                                        ${item.quantity}
                                                </span>

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

                                    <!-- TẠM TÍNH -->
                                    <td class="cart-subtotal">
                                        <fmt:formatNumber value="${item.subtotal}" type="number" groupingUsed="true" /> ₫
                                    </td>

                                    <!-- XÓA -->
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

                    <div class="cart-summary">
                        <h2>Tổng giỏ hàng</h2>

                        <div class="summary-row">
                            <span>Tạm tính</span>

                            <strong id="selectedCartTotal">
                                0 ₫
                            </strong>
                        </div>

                        <div class="cart-select-note" id="selectedCartNote">
                            Chọn sản phẩm muốn mua rồi bấm thanh toán.
                        </div>

                        <div class="summary-actions">
                            <a href="${pageContext.request.contextPath}/products" class="btn-continue">
                                Tiếp tục mua
                            </a>

                            <button type="submit"
                                    form="checkoutSelectForm"
                                    class="btn-checkout"
                                    id="selectedCheckoutBtn">
                                Thanh toán sản phẩm đã chọn
                            </button>
                        </div>
                    </div>

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
            const totalEl = document.getElementById("selectedCartTotal");
            const noteEl = document.getElementById("selectedCartNote");
            const checkoutBtn = document.getElementById("selectedCheckoutBtn");

            function parseSubtotal(value) {
                if (value === null || value === undefined) {
                    return 0;
                }

                const raw = String(value).trim();
                const number = Number(raw);

                return Number.isFinite(number) ? number : 0;
            }

            function formatVnd(value) {
                return new Intl.NumberFormat("en-US").format(Math.round(value)) + " ₫";
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

                /*
                  Không dùng indeterminate để tránh hiện dấu "-"
                  khi chỉ chọn một vài sản phẩm.
                */
                selectAll.indeterminate = false;
                selectAll.checked =
                    itemCheckboxes.length > 0 && checkedCount === itemCheckboxes.length;
            }

            function updateSelectedTotal() {
                const checkedItems = getCheckedItems();

                let selectedTotal = 0;

                checkedItems.forEach(function (checkbox) {
                    selectedTotal += parseSubtotal(checkbox.dataset.subtotal);
                });

                if (totalEl) {
                    totalEl.textContent = formatVnd(selectedTotal);
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
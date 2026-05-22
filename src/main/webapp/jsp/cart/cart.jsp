<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<section class="cart-section">
    <div class="container">

        <h1 class="cart-title">Giỏ hàng của bạn</h1>

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
                <div class="cart-layout">

                    <div class="cart-table-wrap">
                        <table class="cart-table">
                            <thead>
                            <tr>
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
                                <c:set var="item" value="${entry.value}"/>
                                <c:set var="cartKey" value="${entry.key}"/>
                                <c:set var="options" value="${variantOptions[item.productId]}"/>

                                <tr>
                                    <td class="cart-product">
                                        <div class="cart-product-info">
                                            <div class="cart-img-box">
                                                <c:choose>
                                                    <c:when test="${not empty item.imageUrl}">
                                                        <img src="${pageContext.request.contextPath}${item.imageUrl}"
                                                             alt="${fn:escapeXml(item.title)}">
                                                    </c:when>

                                                    <c:otherwise>
                                                        <img src="${pageContext.request.contextPath}/assets/images/default-product.jpg"
                                                             alt="default">
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>

                                            <div>
                                                <div class="cart-product-title">
                                                    <c:out value="${item.title}"/>
                                                </div>

                                                <div class="cart-product-id">
                                                    Mã SP: ${item.productId}
                                                </div>
                                            </div>
                                        </div>
                                    </td>

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
                                                                <c:out value="${v.displayName}"/>

                                                                <c:if test="${v.extraPrice > 0}">
                                                                    - +<fmt:formatNumber value="${v.extraPrice}" type="number" groupingUsed="true"/> ₫
                                                                </c:if>

                                                                - Còn ${v.stock}
                                                            </option>
                                                        </c:forEach>
                                                    </select>
                                                </form>
                                            </c:when>

                                            <c:otherwise>
                          <span class="variant-text">
                            <c:out value="${item.variantDisplayName}"/>
                          </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td class="cart-price">
                                        <fmt:formatNumber value="${item.price}" type="number" groupingUsed="true"/> ₫
                                    </td>

                                    <td class="cart-quantity">
                                        <div class="quantity-box">
                                            <a class="qty-btn"
                                               href="${pageContext.request.contextPath}/cart/decrease?productId=${item.productId}&key=${cartKey}">
                                                -
                                            </a>

                                            <span class="qty-value">
                                                    ${item.quantity}
                                            </span>

                                            <a class="qty-btn"
                                               href="${pageContext.request.contextPath}/cart/increase?productId=${item.productId}&key=${cartKey}">
                                                +
                                            </a>
                                        </div>

                                        <div class="stock-note">
                                            Còn ${item.stock}
                                        </div>
                                    </td>

                                    <td class="cart-subtotal">
                                        <fmt:formatNumber value="${item.subtotal}" type="number" groupingUsed="true"/> ₫
                                    </td>

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
                            <strong>
                                <fmt:formatNumber value="${total}" type="number" groupingUsed="true"/> ₫
                            </strong>
                        </div>

                        <div class="summary-actions">
                            <a href="${pageContext.request.contextPath}/products" class="btn-continue">
                                Tiếp tục mua
                            </a>

                            <a href="${pageContext.request.contextPath}/checkout" class="btn-checkout">
                                Thanh toán
                            </a>
                        </div>
                    </div>

                </div>
            </c:otherwise>
        </c:choose>

    </div>
</section>
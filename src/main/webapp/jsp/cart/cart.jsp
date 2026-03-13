<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<!-- PAGE CSS -->
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/cart.css">

<section class="section container">

    <h2 class="section-title">🛒 Giỏ hàng của bạn</h2>

    <c:choose>
        <c:when test="${not empty cart}">
            <div class="cart-wrapper">

                <table class="cart-table">
                    <thead>
                        <tr>
                            <th>Sản phẩm</th>
                            <th>Số lượng</th>
                            <th>Đơn giá</th>
                            <th>Tạm tính</th>
                            <th>Xóa</th>
                        </tr>
                    </thead>

                    <tbody>
                        <c:forEach var="entry" items="${cart}">
                            <c:set var="item" value="${entry.value}" />

                            <tr>
                                <!-- PRODUCT -->
                                <td class="cart-product">

                                    <c:choose>
                                        <c:when test="${not empty item.imageUrl}">
                                            <c:choose>
                                                <c:when test="${fn:startsWith(item.imageUrl, 'http')}">
                                                    <img src="${item.imageUrl}" class="cart-thumb"
                                                         alt="${fn:escapeXml(item.title)}">
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="${pageContext.request.contextPath}${item.imageUrl}"
                                                         class="cart-thumb"
                                                         alt="${fn:escapeXml(item.title)}">
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>

                                        <c:otherwise>
                                            <img src="${pageContext.request.contextPath}/assets/images/default-product.jpg"
                                                 class="cart-thumb"
                                                 alt="no-image">
                                        </c:otherwise>
                                    </c:choose>

                                    <span class="cart-title"><c:out value="${item.title}"/></span>
                                </td>

                                <!-- QUANTITY -->
                                <td>
                                    <div class="qty-control">
                                        <a href="${pageContext.request.contextPath}/cart/decrease?productId=${item.productId}"
                                           class="btn-qty">−</a>

                                        <span class="qty"><c:out value="${item.quantity}"/></span>

                                        <a href="${pageContext.request.contextPath}/cart/increase?productId=${item.productId}"
                                           class="btn-qty ${item.quantity >= item.stock ? 'disabled' : ''}">
                                            +
                                        </a>
                                    </div>
                                </td>

                                <!-- PRICE -->
                                <td class="cart-price">
                                    <span class="price-vnd">
                                        <fmt:formatNumber value="${item.price}"
                                                          type="number"
                                                          groupingUsed="true"
                                                          minFractionDigits="0"
                                                          maxFractionDigits="0"/>
                                    </span>
                                    <span class="currency">₫</span>
                                </td>

                                <!-- SUBTOTAL -->
                                <td class="cart-subtotal">
                                    <strong class="total-price">
                                        <fmt:formatNumber value="${item.subtotal}"
                                                          type="number"
                                                          groupingUsed="true"
                                                          minFractionDigits="0"
                                                          maxFractionDigits="0"/> ₫
                                    </strong>
                                </td>

                                <!-- REMOVE -->
                                <td>
                                    <a href="${pageContext.request.contextPath}/cart/remove?productId=${item.productId}"
                                       class="btn-remove"
                                       title="Xóa sản phẩm">
                                        ✕
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>

                <!-- ================= SUMMARY ================= -->
                <div class="cart-summary">

                    <!-- TOTAL -->
                    <div class="cart-total">
                        <span>Tổng tiền sản phẩm</span>
                        <strong class="total-price">
                            <fmt:formatNumber value="${total}"
                                              type="number"
                                              groupingUsed="true"
                                              minFractionDigits="0"
                                              maxFractionDigits="0"/> ₫
                        </strong>
                    </div>

                    <!-- PROMOTION NOTE -->
                    <div class="cart-promo-note">
                        <small>
                            💡 Khuyến mãi theo đơn hàng, mã giảm giá và ưu đãi VIP
                            sẽ được áp dụng ở bước thanh toán.
                        </small>
                    </div>

                    <!-- ACTIONS -->
                    <div class="cart-actions">
                        <a href="${pageContext.request.contextPath}/products"
                           class="btn-continue">
                            ↩ Tiếp tục mua sắm
                        </a>

                        <a href="${pageContext.request.contextPath}/checkout"
                           class="btn-checkout"
                           onclick="showLoading(this)">
                            Thanh toán
                        </a>
                    </div>
                </div>

            </div>
        </c:when>

        <c:otherwise>
            <p style="text-align:center; color:#666; font-size:16px;">
                Giỏ hàng của bạn đang trống.
            </p>
        </c:otherwise>
    </c:choose>

</section>

<script>
function showLoading(btn) {
    btn.innerHTML = "⏳ Đang xử lý...";
    btn.style.pointerEvents = "none";
}
</script>

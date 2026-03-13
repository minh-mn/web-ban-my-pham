<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/order-detail.css">

<section class="section container">

    <!-- ===== HEADER ===== -->
    <div class="order-header">
        <h2 class="section-title">
            📦 Đơn hàng #<c:out value="${order.id}"/>
        </h2>

        <span class="order-status ${order.status}">
            <c:out value="${order.statusLabel}"/>
        </span>
    </div>

    <!-- ===== ORDER INFO ===== -->
    <div class="profile-box">
        <p><strong>👤 Người nhận:</strong> <c:out value="${order.fullName}"/></p>
        <p><strong>📞 Số điện thoại:</strong> <c:out value="${order.phone}"/></p>
        <p><strong>📍 Địa chỉ:</strong> <c:out value="${order.address}"/></p>
        <p><strong>🕒 Ngày đặt:</strong> <c:out value="${order.createdAt}"/></p>

        <p>
            <strong>💳 Phương thức thanh toán:</strong>
            <c:out value="${order.paymentMethod}"/>
        </p>

        <p>
            <strong>💰 Trạng thái thanh toán:</strong>
            <c:choose>
                <c:when test="${order.paymentStatus eq 'PAID'}">
                    <span style="color: green;">Đã thanh toán</span>
                </c:when>
                <c:when test="${order.paymentStatus eq 'PENDING'}">
                    <span style="color: orange;">Chờ thanh toán</span>
                </c:when>
                <c:otherwise>
                    <span style="color: red;">Đã hủy</span>
                </c:otherwise>
            </c:choose>
        </p>

        <c:if test="${order.paymentMethod eq 'VNPAY' && not empty order.vnpTxnRef}">
            <p>
                <strong>🔐 Mã giao dịch VNPay:</strong>
                <c:out value="${order.vnpTxnRef}"/>
            </p>
        </c:if>
    </div>

    <!-- ===== ORDER ITEMS ===== -->
    <table class="cart-table">
        <thead>
        <tr>
            <th>Sản phẩm</th>
            <th>Số lượng</th>
            <th>Đơn giá</th>
            <th>Tạm tính</th>
        </tr>
        </thead>

        <tbody>
        <c:forEach var="item" items="${orderItems}">
            <tr>
                <td class="product-cell">

                    <%-- ✅ Ảnh: luôn nối contextPath nếu là path /assets/... --%>
                    <c:choose>
                        <c:when test="${not empty item.imageUrl}">
                            <c:choose>
                                <c:when test="${fn:startsWith(item.imageUrl, 'http')}">
                                    <img src="${item.imageUrl}"
                                         alt="${fn:escapeXml(item.productName)}"
                                         class="product-thumb">
                                </c:when>
                                <c:otherwise>
                                    <img src="${pageContext.request.contextPath}${item.imageUrl}"
                                         alt="${fn:escapeXml(item.productName)}"
                                         class="product-thumb">
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <img src="${pageContext.request.contextPath}/assets/images/default-product.jpg"
                                 alt="no-image"
                                 class="product-thumb">
                        </c:otherwise>
                    </c:choose>

                    <span class="product-name">
                        <c:out value="${item.productName}"/>
                    </span>
                </td>

                <td><c:out value="${item.quantity}"/></td>

                <td class="price">
                    <fmt:formatNumber value="${item.price}"
                                      type="number"
                                      groupingUsed="true"
                                      minFractionDigits="0"
                                      maxFractionDigits="0"/> ₫
                </td>

                <td class="price">
                    <fmt:formatNumber value="${item.subtotal}"
                                      type="number"
                                      groupingUsed="true"
                                      minFractionDigits="0"
                                      maxFractionDigits="0"/> ₫
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <!-- ===== TOTAL ===== -->
    <div class="order-total-box">
        <span>Tổng thanh toán</span>
        <strong>
            <fmt:formatNumber value="${order.total}"
                              type="number"
                              groupingUsed="true"
                              minFractionDigits="0"
                              maxFractionDigits="0"/> ₫
        </strong>

        <p class="order-note">
            💡 Tổng tiền đã bao gồm toàn bộ khuyến mãi
            (giảm theo sản phẩm, đơn hàng, mã khuyến mãi, ưu đãi khách hàng).
        </p>
    </div>

    <!-- ===== ACTION ===== -->
    <div class="order-actions">

        <%-- ✅ Nếu trang này chỉ GET thì KHÔNG bắt buộc CSRF.
             Nếu bạn muốn chuẩn bị cho POST (vd: Hủy đơn / Thanh toán lại), có thể để sẵn form + csrf như dưới. --%>

        <a href="${pageContext.request.contextPath}/orders"
           class="btn-primary">
            ↩ Quay lại đơn hàng
        </a>

        <%-- Ví dụ (nếu có nút Hủy đơn dạng POST) --%>
        <%--
        <c:if test="${order.status eq 'pending'}">
            <form method="post" action="${pageContext.request.contextPath}/orders/cancel" style="display:inline;">
                <jsp:include page="/jsp/common/csrf.jspf"/>
                <input type="hidden" name="id" value="${order.id}"/>
                <button type="submit" class="btn-outline">Hủy đơn</button>
            </form>
        </c:if>
        --%>

    </div>

</section>

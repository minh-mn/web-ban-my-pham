<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 26/05/2026
  Time: 6:28 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<main class="container">
    <div class="promo-header" style="text-align: center; padding: 40px 0;">
    <c:choose>
        <c:when test="${not empty activePromotion}">
        <h1 style="color: var(--pink-main); font-size: 36px; font-weight: 800;">🎁 ${activePromotion.name} 🎁</h1>
        <p style="color: #666; font-size: 16px; margin-top: 10px;">${activePromotion.description}</p>

            <div id="countdown" data-end-time="${endTimeMillis}" style="font-size: 20px; margin-top: 15px; color: #333;">
        Chương trình kết thúc sau: <span id="timer" style="font-weight: bold; color: var(--pink-main);">00 : 00 : 00</span>
        </div>
    </c:when>
    <c:otherwise>
        <h1 style="color: #666;">Hiện tại chưa có chương trình khuyến mãi nào diễn ra!</h1>
        <p>Vui lòng quay lại sau bạn nhé.</p>
    </c:otherwise>
    </c:choose>
    </div>

    <%-- Danh sách lưới sản phẩm khuyến mãi --%>
    <c:if test="${not empty promoProducts}">
    <div class="product-grid">
        <c:forEach var="product" items="${promoProducts}">
        
        <%-- Xử lý tính toán giá sau khuyến mãi động dựa vào loại hình giảm giá --%>
        <c:choose>
            <c:when test="${activePromotion.discountType == 'PERCENTAGE'}">
            <c:set var="discountLabel" value="-${activePromotion.discountValue}%" />
            <c:set var="discountedPrice" value="${product.price * (1 - activePromotion.discountValue / 100)}" />
        </c:when>
        <c:otherwise>
            <c:set var="discountLabel" value="SALE" />
            <c:set var="discountedPrice" value="${product.price - activePromotion.discountValue}" />
        </c:otherwise>
        </c:choose>

        <div class="product-card">
            <div class="badge-sale">${discountLabel}</div>
            <div class="product-img-box">
                <img src="${pageContext.request.contextPath}${product.imageUrl}" alt="${product.title}">
            </div>
            <h3 class="product-title">
                <a href="${pageContext.request.contextPath}/product?id=${product.id}" style="text-decoration: none; color: inherit;">
                ${product.title}
                </a>
            </h3>
            
            <%-- Hiển thị giá gốc (gạch chân) và giá mới sau khi giảm --%>
            <p class="product-price" style="margin-bottom: 2px;">
            <fmt:formatNumber value="${discountedPrice}" pattern="#,###" /> đ
            </p>
            <p style="text-decoration: line-through; color: #aaa; font-size: 13px; margin-bottom: 12px;">
            <fmt:formatNumber value="${product.price}" pattern="#,###" /> đ
            </p>

            <a href="${pageContext.request.contextPath}/product?id=${product.id}" class="btn-buy">Xem chi tiết</a>
        </div>
        </c:forEach>
    </div>
    </c:if>
</main>

<%-- Bộ Script đếm ngược tái sử dụng từ Flash Sale --%>
<script>
  document.addEventListener("DOMContentLoaded", function() {
    const endTimeElement = document.getElementById("countdown");
    const timerElement = document.getElementById("timer");

if (!endTimeElement || !timerElement) return;

    const endTime = parseInt(endTimeElement.getAttribute("data-end-time"));
    if (isNaN(endTime)) return;

const x = setInterval(function() {
      const now = new Date().getTime();
      const distance = endTime - now;

      if (distance < 0) {
        clearInterval(x);
        timerElement.innerHTML = "Đã kết thúc";
        return;
      }n
      const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((distance % (1000 * 60)) / 1000);

          // Định dạng hiển thị chuỗi 00:00:00
          const hStr = hours < 10 ? "0" + hours : hours;
      const mStr = minutes < 10 ? "0" + minutes : minutes;
      const sStr = seconds < 10 ? "0" + seconds : seconds;

          timerElement.innerHTML = hStr + " : " + mStr + " : " + sStr;
    }, 1000);
  });
</script>

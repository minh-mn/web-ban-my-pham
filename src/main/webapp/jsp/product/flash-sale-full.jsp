<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 25/05/2026
  Time: 9:01 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<main class="container">
  <div class="flash-sale-header" style="text-align: center; padding: 40px 0;">
    <h1 style="color: #d0021b; font-size: 40px;">⚡ FLASH SALE SIÊU TỐC ⚡</h1>

    <div id="countdown" data-end-time="${activeFlashSale.endTime.time}" style="font-size: 24px; margin-top: 20px;">
      Thời gian còn lại: <span id="timer" style="font-weight: bold;">00:00:00</span>
    </div>
  </div>

  <div class="product-grid">
    <c:forEach var="item" items="${fsItems}">
      <div class="product-card">
        <div class="badge-sale">-${item.product.discountPercent}%</div>
        <div class="product-img-box">
          <img src="${pageContext.request.contextPath}${item.product.imageUrl}" alt="${item.product.title}">
        </div>
        <h3 class="product-title">${item.product.title}</h3>

        <p class="product-price"><fmt:formatNumber value="${item.flashPrice}" /> đ</p>

        <a href="${pageContext.request.contextPath}/product?id=${item.product.id}" class="btn-buy">Mua ngay</a>
      </div>
    </c:forEach>
  </div>
</main>

<script>
  // Script đếm ngược dùng chung
  document.addEventListener("DOMContentLoaded", function() {
    const endTimeElement = document.getElementById("countdown");
    const timerElement = document.getElementById("timer");

    if (!endTimeElement || !timerElement) return; // Nếu không tìm thấy thẻ thì thoát

    const endTime = parseInt(endTimeElement.getAttribute("data-end-time"));
    if (isNaN(endTime)) return;

    const x = setInterval(function() {
      const now = new Date().getTime();
      const distance = endTime - now;

      if (distance < 0) {
        clearInterval(x);
        timerElement.innerHTML = "Đã kết thúc";
        return;
      }

      const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((distance % (1000 * 60)) / 1000);

      timerElement.innerHTML =
              (hours < 10 ? "0" + hours : hours) + " : " +
              (minutes < 10 ? "0" + minutes : minutes) + " : " +
              (seconds < 10 ? "0" + seconds : seconds);
    }, 1000);
  });
</script>
<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 26/04/2026
  Time: 7:50 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<section class="section flash-deal">
  <div class="container">
    <div class="deal-header">
      <h2 class="section-title">
        <i class="fa-solid fa-bolt"></i> FLASH DEAL
      </h2>
      <div id="countdown">
        Kết thúc sau: <span id="timer">00 : 00 : 00</span>
      </div>
    </div>
    <div class="product-grid">
      <c:forEach var="product" items="${products}" end="3">
        <div class="product-card">
          <div class="badge-sale">-${product.discountPercent}%</div>
          <div class="product-img-box">
            <img src="${pageContext.request.contextPath}${product.imageUrl}" alt="${product.title}">
          </div>
          <h3 class="product-title">${product.title}</h3>
          <div class="price">
                        <span class="sale-price">
                            <fmt:formatNumber value="${product.finalPrice}" type="number" groupingUsed="true" /> ₫
                        </span>
          </div>
          <div class="deal-progress">
            <div class="progress-bar" style="width: 75%;"></div>
            <span>Sắp cháy hàng</span>
          </div>
        </div>
      </c:forEach>
    </div>
  </div>
</section>

<script>
  // Logic Countdown Flash Deal
  function initFlashSaleTimer(hoursLimit) {
    let distance = hoursLimit * 1000 * 60 * 60; // Chuyển sang milisecond

    const x = setInterval(function() {
      distance -= 1000;

      // Tính toán Giờ, Phút, Giây
      const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((distance % (1000 * 60)) / 1000);

      const timerElement = document.getElementById("timer");
      if (timerElement) {
        timerElement.innerHTML =
                (hours < 10 ? "0" + hours : hours) + " : " +
                (minutes < 10 ? "0" + minutes : minutes) + " : " +
                (seconds < 10 ? "0" + seconds : seconds);
      }

      if (distance < 0) {
        clearInterval(x);
        if (timerElement) timerElement.innerHTML = "SỰ KIỆN KẾT THÚC";
      }
    }, 1000);
  }

  // Chạy đồng hồ 5 tiếng
  document.addEventListener("DOMContentLoaded", function() {
    initFlashSaleTimer(5);
  });
</script>
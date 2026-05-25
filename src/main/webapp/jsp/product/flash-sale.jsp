<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 26/04/2026
  Time: 7:50 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%-- Nhận tham số limit từ trang chủ truyền sang --%>
<c:set var="limit" value="${param.limit}" />

<section class="section flash-deal">
  <div class="container">
    <%-- Tiêu đề --%>
      <div class="deal-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <h2 class="section-title">
          <i class="fa-solid fa-bolt"></i> FLASH DEAL
        </h2>

        <%-- Chỉ cần kiểm tra xem activeFlashSale có dữ liệu hay không --%>
        <c:if test="${not empty activeFlashSale}">
          <div id="countdown" data-end-time="${activeFlashSale.endTime.time}">
            Kết thúc sau: <span id="timer">00 : 00 : 00</span>
          </div>
        </c:if>
      </div>

    <div class="product-grid">
      <%-- Logic: Nếu có limit thì end là 3 (4 item), không có thì end là item cuối --%>
      <c:forEach var="item" items="${fsItems}" varStatus="status"
                 end="${not empty limit ? 3 : fn:length(fsItems) - 1}">

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

    <%-- Nút Xem thêm: Chỉ hiện ở trang chủ (khi có limit) và nếu tổng sản phẩm > 4 --%>
    <c:if test="${not empty limit and fn:length(fsItems) > 4}">
      <div style="text-align: center; margin-top: 30px;">
        <a href="${pageContext.request.contextPath}/flash-sale" class="btn-view-more">
          Xem tất cả sản phẩm
        </a>
      </div>
    </c:if>
  </div>
</section>

<%-- CSS bổ sung cho nút Xem thêm --%>
<style>
  .btn-view-more {
    display: inline-block;
    padding: 12px 40px;
    background-color: #fff;
    color: #d0021b;
    border: 2px solid #d0021b;
    text-decoration: none;
    border-radius: 5px;
    font-weight: bold;
    transition: 0.3s;
  }
  .btn-view-more:hover {
    background-color: #d0021b;
    color: #fff;
  }
</style>

<script>
  function startCountdown() {
    const endTimeElement = document.getElementById("countdown");
    const timerElement = document.getElementById("timer");

    // 1. Kiểm tra sự tồn tại của các thẻ HTML
    if (!endTimeElement) {
      console.error("Lỗi: Không tìm thấy thẻ #countdown");
      return;
    }

    // 2. Lấy dữ liệu và kiểm tra xem có phải là số không
    const endTimeRaw = endTimeElement.getAttribute("data-end-time");
    console.log("Giá trị endTime từ Server:", endTimeRaw); // Kiểm tra giá trị này trong Console

    const endTime = parseInt(endTimeRaw);

    if (isNaN(endTime)) {
      console.error("Lỗi: endTime không phải là số hợp lệ. Hãy kiểm tra biến activeFlashSale trong Java!");
      if(timerElement) timerElement.innerHTML = "Lỗi dữ liệu";
      return;
    }

    const x = setInterval(function() {
      const now = new Date().getTime();
      const distance = endTime - now;

      // 3. Hiển thị kết quả (hoặc thông báo hết hạn)
      if (distance < 0) {
        clearInterval(x);
        if(timerElement) timerElement.innerHTML = "Đã kết thúc";
        return;
      }

      const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((distance % (1000 * 60)) / 1000);

      if(timerElement) {
        timerElement.innerHTML =
                (hours < 10 ? "0" + hours : hours) + " : " +
                (minutes < 10 ? "0" + minutes : minutes) + " : " +
                (seconds < 10 ? "0" + seconds : seconds);
      }
    }, 1000);
  }

  // Sử dụng DOMContentLoaded để đảm bảo script chạy khi HTML sẵn sàng
  document.addEventListener("DOMContentLoaded", startCountdown);
</script>

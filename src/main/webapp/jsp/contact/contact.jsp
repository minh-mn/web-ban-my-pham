<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 26/05/2026
  Time: 9:10 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/contact.css">

<main class="container contact-page">

  <div class="contact-hero">
    <h1>📞 LIÊN HỆ VỚI CHÚNG TÔI</h1>
    <p>MyCosmetic luôn sẵn sàng lắng nghe và hỗ trợ bạn!</p>
  </div>

  <div class="contact-layout">

    <div class="contact-info-card">
      <h3>Thông tin cửa hàng</h3>

      <div class="contact-item">
        <i class="fa-solid fa-building"></i>
        <strong>Công ty:</strong> ${settings.company_name}
      </div>

      <div class="contact-item">
        <i class="fa-solid fa-location-dot"></i>
        <strong>Địa chỉ:</strong> ${settings.address}
      </div>

      <div class="contact-item">
        <i class="fa-solid fa-phone"></i>
        <strong>Hotline:</strong> ${settings.hotline}
      </div>

      <div class="contact-item">
        <i class="fa-solid fa-envelope"></i>
        <strong>Email:</strong> ${settings.sales_email}
      </div>
    </div>

    <div class="contact-form-card">
      <h3>Gửi lời nhắn cho MyCosmetic</h3>

      <c:if test="${not empty messageSuccess}">
        <div class="contact-success">
            ${messageSuccess}
        </div>
      </c:if>

      <form action="${pageContext.request.contextPath}/lien-he"
            method="POST"
            class="contact-form">

        <input type="text"
               name="fullName"
               placeholder="Họ và tên của bạn"
               required>

        <input type="email"
               name="email"
               placeholder="Địa chỉ Email"
               required>

        <input type="text"
               name="phone"
               placeholder="Số điện thoại"
               required>

        <textarea name="message"
                  placeholder="Nội dung lời nhắn..."
                  required></textarea>

        <button type="submit" class="contact-submit">
          Gửi Lời Nhắn
        </button>

      </form>
    </div>

  </div>

</main>

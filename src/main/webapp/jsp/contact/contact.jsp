<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 26/05/2026
  Time: 9:10 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<main class="container" style="margin-top: 40px; margin-bottom: 50px;">
  <div style="text-align: center; margin-bottom: 40px;">
    <h1 style="color: var(--pink-main, #ff5fa2); font-size: 36px; font-weight: bold;">📞 LIÊN HỆ VỚI CHÚNG TÔI</h1>
    <p style="color: #666;">MyCosmetic luôn sẵn sàng lắng nghe và hỗ trợ bạn!</p>
  </div>

  <div style="display: flex; flex-wrap: wrap; gap: 40px; justify-content: space-between;">

    <div style="flex: 1; min-width: 300px; background: #fff0f6; padding: 30px; border-radius: 12px;">
      <h3 style="color: #333; margin-bottom: 20px; font-size: 20px;">Thông tin cửa hàng</h3>

      <p style="margin-bottom: 15px; color: #555; line-height: 1.6;">
        <i class="fa-solid fa-building" style="color: #ff5fa2; margin-right: 10px;"></i>
        <strong>Công ty:</strong> ${settings.company_name}  <%-- Thay bằng tên thuộc tính chứa địa chỉ trong DB của bạn (ví dụ: settings.address hoặc settings.company_address) --%>
      </p>

      <p style="margin-bottom: 15px; color: #555; line-height: 1.6;">
        <i class="fa-solid fa-location-dot" style="color: #ff5fa2; margin-right: 10px;"></i>
        <strong>Địa chỉ:</strong> ${settings.address}  <%-- Thay bằng tên thuộc tính chứa địa chỉ trong DB của bạn (ví dụ: settings.address hoặc settings.company_address) --%>
      </p>

      <p style="margin-bottom: 15px; color: #555;">
        <i class="fa-solid fa-phone" style="color: #ff5fa2; margin-right: 10px;"></i>
        <strong>Hotline:</strong> ${settings.hotline}  <%-- Thay bằng tên biến hotline của bạn (ví dụ: settings.hotline hoặc settings.phone) --%>
      </p>

      <p style="margin-bottom: 15px; color: #555;">
        <i class="fa-solid fa-envelope" style="color: #ff5fa2; margin-right: 10px;"></i>
        <strong>Email:</strong> ${settings.sales_email}      <%-- Thay bằng tên biến email trong settings --%>
      </p>
    </div>

    <div style="flex: 1.5; min-width: 300px;">
      <h3 style="color: #333; margin-bottom: 20px; font-size: 20px;">Gửi lời nhắn cho MyCosmetic</h3>

      <%-- Hiển thị thông báo thành công nếu có --%>
      <c:if test="${not empty messageSuccess}">
        <div style="background: #d4edda; color: #155724; padding: 15px; border-radius: 8px; margin-bottom: 20px;">
            ${messageSuccess}
        </div>
      </c:if>

      <form action="${pageContext.request.contextPath}/lien-he" method="POST" style="display: flex; flex-direction: column; gap: 15px;">
        <input type="text" name="fullName" placeholder="Họ và tên của bạn" required
               style="padding: 12px 15px; border: 1px solid #ddd; border-radius: 8px; width: 100%; outline: none;">

        <input type="email" name="email" placeholder="Địa chỉ Email" required
               style="padding: 12px 15px; border: 1px solid #ddd; border-radius: 8px; width: 100%; outline: none;">

        <input type="text" name="phone" placeholder="Số điện thoại" required
               style="padding: 12px 15px; border: 1px solid #ddd; border-radius: 8px; width: 100%; outline: none;">

        <textarea name="message" rows="5" placeholder="Nội dung lời nhắn..." required
                  style="padding: 12px 15px; border: 1px solid #ddd; border-radius: 8px; width: 100%; outline: none; resize: vertical;"></textarea>

        <button type="submit" class="btn-buy" style="background: #ff5fa2; color: #fff; border: none; padding: 12px 20px; border-radius: 8px; font-weight: bold; cursor: pointer; align-self: flex-start;">
          Gửi Lời Nhắn
        </button>
      </form>
    </div>

  </div>
</main>

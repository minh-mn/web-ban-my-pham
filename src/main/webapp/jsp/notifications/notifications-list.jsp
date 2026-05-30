<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 30/05/2026
  Time: 8:17 CH
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<section class="section" style="padding: 40px 0; min-height: 70vh; background: #f8f9fa;">
  <div class="container" style="max-width: 800px; margin: 0 auto; background: #fff; padding: 30px; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.05);">
    <h2 style="color: #333; margin-bottom: 25px; border-bottom: 2px solid #ff5fa2; padding-bottom: 10px; font-weight: 700;">
      🔔 TẤT CẢ THÔNG BÁO ĐÃ NHẬN
    </h2>

    <div style="display: flex; flex-direction: column; gap: 15px;">
      <c:choose>
        <c:when test="${not empty requestScope.notifications}">
          <c:forEach var="notif" items="${requestScope.notifications}">

            <%-- Đặt màu nền mặc định tùy thuộc vào trạng thái đã đọc hay chưa --%>
            <c:set var="bgColor" value="${notif.read ? '#ffffff' : '#fff9fb'}" />

            <%-- ĐÃ SỬA: Đưa thẻ <a> ra ngoài cùng toàn bộ khối thông báo để bấm vào đâu cũng được --%>
            <a href="${pageContext.request.contextPath}/notifications/read?id=${notif.id}&redirect=${notif.targetUrl}"
               style="display: flex; padding: 18px; border: 1px solid #eee; border-radius: 10px; background: ${bgColor}; gap: 15px; align-items: start; text-decoration: none; color: inherit; transition: all 0.2s ease; box-shadow: 0 2px 5px rgba(0,0,0,0.01);"
               onmouseover="this.style.background='#fff0f5'; this.style.borderColor='#ff5fa2'; this.style.transform='translateY(-1px)';"
               onmouseout="this.style.background='${bgColor}'; this.style.borderColor='#eee'; this.style.transform='none';">

              <div style="font-size: 26px; line-height: 1;">
                <c:choose>
                  <c:when test="${notif.type == 'VOUCHER'}">🎟️</c:when>
                  <c:when test="${notif.type == 'EVENT'}">📢</c:when>
                  <c:otherwise>✨</c:otherwise>
                </c:choose>
              </div>

              <div style="flex: 1;">
                <h4 style="margin: 0 0 6px 0; font-size: 15px; color: #222; font-weight: ${notif.read ? '500' : 'bold'}; line-height: 1.4;">
                  <c:out value="${notif.title}"/>
                </h4>
                <p style="margin: 0; color: #666; font-size: 13.5px; line-height: 1.5;">
                  <c:out value="${notif.message}"/>
                </p>
              </div>
            </a>

          </c:forEach>
        </c:when>
        <c:otherwise>
          <div style="text-align: center; padding: 50px 0; color: #999;">
            <p style="font-size: 14px;">Bạn chưa có thông báo nào.</p>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</section>
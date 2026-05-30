<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 30/05/2026
  Time: 7:38 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Quản lý Thông báo" scope="request"/>
<c:set var="activeMenu" value="notifications" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Hệ thống Thông báo</h1>
        <p class="admin-subtext">Danh sách các thông báo chiến dịch đã gửi hàng loạt tới người dùng.</p>
      </div>

      <a class="admin-btn admin-btn--primary"
         href="${pageContext.request.contextPath}/admin/notifications?action=new">
        + Gửi thông báo mới
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:choose>
          <c:when test="${empty systemNotifications}">
            <div style="text-align: center; padding: 40px; color: #888; font-style: italic;">
              Chưa có chiến dịch thông báo hàng loạt nào được gửi.
            </div>
          </c:when>
          <c:otherwise>
            <table class="admin-table">
              <thead>
              <tr>
                <th style="width: 80px;">Mã Lô</th>
                <th>Tiêu đề thông báo</th>
                <th>Phân loại</th>
                <th>Nội dung tóm tắt</th>
                <th>Đường dẫn chuyển hướng (URL)</th>
                <th style="width: 120px;" class="admin-actions">Hành động</th>
              </tr>
              </thead>
              <tbody>
              <c:forEach var="item" items="${systemNotifications}">
                <tr>
                  <td>#${item.id}</td>
                  <td><b>${item.title}</b></td>
                  <td>
                    <span class="admin-chip" style="background: #fff0f6; color: #ff5fa2; font-weight: bold;">
                        ${item.type}
                    </span>
                  </td>
                  <td style="max-width: 250px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                      ${item.message}
                  </td>
                  <td><code>${item.targetUrl}</code></td>
                  <td class="admin-actions">
                    <form method="post"
                          action="${pageContext.request.contextPath}/admin/notifications"
                          class="admin-inline"
                          onsubmit="return confirm('Xóa bản ghi lịch sử thông báo này?')">
                      <input type="hidden" name="action" value="delete">
                      <input type="hidden" name="id" value="${item.id}">
                      <button class="admin-btn admin-btn--danger" type="submit">Xóa</button>
                    </form>
                  </td>
                </tr>
              </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

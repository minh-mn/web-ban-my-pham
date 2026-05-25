<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 25/05/2026
  Time: 5:41 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Flash Sale" scope="request"/>
<c:set var="activeMenu" value="flashsale" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">
    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Flash Sale</h1>
        <p class="admin-subtext">Quản lý chương trình Flash Sale</p>
      </div>
      <a class="admin-btn admin-btn--primary" href="${pageContext.request.contextPath}/admin/flash-sale?action=new">
        + Thêm Flash Sale
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">
        <table class="admin-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Start</th>
            <th>End</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="f" items="${flashSales}">
            <tr>
              <td>#${f.id}</td>
              <td><b>${f.title}</b></td>
              <td>${f.startTime}</td>
              <td>${f.endTime}</td>
              <td class="admin-status-cell">
                <c:choose>
                  <c:when test="${f.active}">
                    <span class="admin-pill admin-pill--ok">Active</span>
                  </c:when>
                  <c:otherwise>
                    <span class="admin-pill admin-pill--danger">Inactive</span>
                  </c:otherwise>
                </c:choose>
              </td>
              <td class="admin-actions">
                <a class="admin-btn" href="${pageContext.request.contextPath}/admin/flash-sale?action=edit&id=${f.id}">Sửa</a>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</main>
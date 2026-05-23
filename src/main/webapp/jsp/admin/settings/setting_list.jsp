<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 23/05/2026
  Time: 2:58 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Website Settings" scope="request"/>
<c:set var="activeMenu" value="settings" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <!-- TOPBAR -->
    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Website Settings</h1>
        <p class="admin-subtext">Quản lý thông tin footer & hệ thống.</p>
      </div>

      <a class="admin-btn admin-btn--primary"
         href="${pageContext.request.contextPath}/admin/settings?action=edit">
        Chỉnh sửa
      </a>
    </div>

    <!-- TABLE -->
    <div class="admin-card">
      <div class="admin-card__body">

        <table class="admin-table">
          <thead>
          <tr>
            <th>Key</th>
            <th>Value</th>
          </tr>
          </thead>

          <tbody>
          <c:forEach var="entry" items="${settings}">
            <tr>
              <td><b>${entry.key}</b></td>
              <td><c:out value="${entry.value}"/></td>
            </tr>
          </c:forEach>
          </tbody>

        </table>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

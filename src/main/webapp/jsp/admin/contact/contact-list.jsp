<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 23/05/2026
  Time: 9:30 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Admin | Contact Messages"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <h1>Contact Messages</h1>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <table class="admin-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>Họ tên</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Subject</th>
            <th>Message</th>
            <th>Time</th>
          </tr>
          </thead>

          <tbody>
          <c:forEach var="c" items="${list}">
            <tr>
              <td>${c.id}</td>
              <td>${c.fullName}</td>
              <td>${c.email}</td>
              <td>${c.phone}</td>
              <td>${c.subject}</td>
              <td style="max-width:300px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">
                  ${c.message}
              </td>
              <td>${c.createdAt}</td>
            </tr>
          </c:forEach>
          </tbody>

        </table>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

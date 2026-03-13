<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="ADMIN | Chi tiết User" scope="request"/>
<c:set var="activeMenu" value="users" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Chi tiết User</h1>
        <p class="admin-subtext">Thông tin cơ bản (ID / Username / Role).</p>
      </div>

      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/users">Quay lại</a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:choose>
          <c:when test="${empty user}">
            <div class="admin-empty">Không tìm thấy user.</div>
          </c:when>

          <c:otherwise>

            <div class="admin-grid-2" style="max-width:720px;">

              <div class="admin-field">
                <div class="admin-label">ID</div>
                <div style="font-weight:850;">#${user.id}</div>
              </div>

              <div class="admin-field">
                <div class="admin-label">Username</div>
                <div><c:out value="${user.username}"/></div>
              </div>

              <div class="admin-field">
                <div class="admin-label">Role</div>
                <div>
                  <c:choose>
                    <c:when test="${user.role == 'ADMIN'}">
                      <span class="admin-pill admin-pill--ok">ADMIN</span>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-pill">USER</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

            </div>

            <hr class="admin-divider"/>

            <div class="admin-actions">
              <a class="admin-btn admin-btn--primary"
                 href="${pageContext.request.contextPath}/admin/users?action=edit&id=${user.id}">
                Sửa
              </a>

              <a class="admin-btn"
                 href="${pageContext.request.contextPath}/admin/users">
                Đóng
              </a>
            </div>

          </c:otherwise>
        </c:choose>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

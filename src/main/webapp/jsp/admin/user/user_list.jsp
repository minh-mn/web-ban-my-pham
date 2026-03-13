<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="ADMIN | Users" scope="request"/>
<c:set var="activeMenu" value="users" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Users</h1>
        <p class="admin-subtext">Quản lý username và role (ADMIN | USER).</p>
      </div>
    </div>

    <!-- MESSAGE / ERROR -->
    <c:if test="${param.msg == 'deleted'}">
      <div class="admin-alert admin-alert--ok" style="margin-bottom:12px;">
        Đã xóa user.
      </div>
    </c:if>

    <c:if test="${param.err == 'delete_failed'}">
      <div class="admin-alert admin-alert--danger" style="margin-bottom:12px;">
        Không thể xóa user (có thể do dữ liệu liên quan như đơn hàng / token). Thử “Disable” thay vì xóa vĩnh viễn.
      </div>
    </c:if>

    <c:if test="${param.err == 'cannot_delete_self'}">
      <div class="admin-alert admin-alert--danger" style="margin-bottom:12px;">
        Bạn không thể tự xóa tài khoản đang đăng nhập.
      </div>
    </c:if>

    <div class="admin-card">
      <div class="admin-card__body">

        <!-- TOOLBAR -->
        <div class="admin-toolbar">
          <form method="get"
                action="${pageContext.request.contextPath}/admin/users"
                class="admin-toolbar__form">

            <input class="admin-input" type="text" name="q"
                   value="${param.q}" placeholder="Tìm theo username...">

            <input class="admin-input" type="text" name="role"
                   value="${param.role}" placeholder="Role (ADMIN/USER)">

            <button class="admin-btn" type="submit">Lọc</button>

            <c:if test="${not empty param.q || not empty param.role}">
              <a class="admin-btn" href="${pageContext.request.contextPath}/admin/users">Xóa lọc</a>
            </c:if>

          </form>
        </div>

        <c:choose>
          <c:when test="${empty users}">
            <div class="admin-empty">Chưa có user.</div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
                <tr>
                  <th style="width:90px;">ID</th>
                  <th>Username</th>
                  <th style="width:170px;">Role</th>
                  <th style="width:420px;">Thao tác</th>
                </tr>
              </thead>

              <tbody>
                <c:forEach var="u" items="${users}">
                  <tr>
                    <td>#${u.id}</td>

                    <td>
                      <a href="${pageContext.request.contextPath}/admin/users?action=detail&id=${u.id}"
                         style="text-decoration:none;">
                        <span style="font-weight:850;">
                          <c:out value="${u.username}"/>
                        </span>
                      </a>
                    </td>

                    <td class="admin-status-cell">
                      <c:choose>
                        <c:when test="${u.role == 'ADMIN'}">
                          <span class="admin-pill admin-pill--ok">ADMIN</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill">USER</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td class="admin-actions">
                      <a class="admin-btn"
                         href="${pageContext.request.contextPath}/admin/users?action=edit&id=${u.id}">
                        Sửa
                      </a>

                      <!-- Đổi role nhanh -->
                      <form method="post"
                            action="${pageContext.request.contextPath}/admin/users"
                            class="admin-inline">

                        <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
                        <%@ include file="/jsp/common/csrf.jspf" %>

                        <input type="hidden" name="action" value="changeRole"/>
                        <input type="hidden" name="id" value="${u.id}"/>

                        <select class="admin-select" name="role" style="width:150px;">
                          <option value="USER"  ${u.role == 'USER' ? 'selected' : ''}>USER</option>
                          <option value="ADMIN" ${u.role == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                        </select>

                        <button class="admin-btn" type="submit">Đổi role</button>
                      </form>

                      <!-- XÓA USER -->
                      <form method="post"
                            action="${pageContext.request.contextPath}/admin/users"
                            class="admin-inline"
                            onsubmit="return confirm('Xóa user #${u.id} (${u.username})? Hành động này không thể hoàn tác.');">

                        <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
                        <%@ include file="/jsp/common/csrf.jspf" %>

                        <input type="hidden" name="action" value="delete"/>
                        <input type="hidden" name="id" value="${u.id}"/>

                        <!-- (Khuyến nghị) Không cho xóa chính mình -->
                        <c:choose>
                          <c:when test="${not empty sessionScope.user and sessionScope.user.id == u.id}">
                            <button class="admin-btn" type="button" disabled style="opacity:.55;cursor:not-allowed;">
                              Xóa
                            </button>
                          </c:when>
                          <c:otherwise>
                            <button class="admin-btn" type="submit" style="background:#e53935;border-color:#e53935;">
                              Xóa
                            </button>
                          </c:otherwise>
                        </c:choose>

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

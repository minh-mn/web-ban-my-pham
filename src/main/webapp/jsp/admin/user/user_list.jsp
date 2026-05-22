<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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
        <p class="admin-subtext">
          Quản lý tài khoản, role, trạng thái, mật khẩu và rank khách hàng.
        </p>
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
        Không thể xóa user. Có thể user đang có dữ liệu liên quan như đơn hàng hoặc token.
        Nên khóa hoặc disable tài khoản thay vì xóa vĩnh viễn.
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

            <input class="admin-input"
                   type="text"
                   name="q"
                   value="${f_q}"
                   placeholder="Tìm username, tên, email, SĐT...">

            <select class="admin-select" name="role" style="width:150px;">
              <option value="" ${empty f_role ? 'selected' : ''}>Tất cả role</option>
              <option value="USER" ${f_role == 'USER' ? 'selected' : ''}>USER</option>
              <option value="ADMIN" ${f_role == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
            </select>

            <select class="admin-select" name="rank" style="width:190px;">
              <option value="" ${empty f_rank ? 'selected' : ''}>Tất cả rank</option>
              <option value="AUTO" ${f_rank == 'AUTO' ? 'selected' : ''}>
                AUTO - Tự động
              </option>

              <c:forEach var="r" items="${ranks}">
                <option value="${r.code}" ${f_rank == r.code ? 'selected' : ''}>
                    ${r.code} - ${r.name}
                </option>
              </c:forEach>
            </select>

            <select class="admin-select" name="active" style="width:160px;">
              <option value="" ${empty f_active ? 'selected' : ''}>Tất cả trạng thái</option>
              <option value="1" ${f_active == '1' ? 'selected' : ''}>ACTIVE</option>
              <option value="0" ${f_active == '0' ? 'selected' : ''}>INACTIVE</option>
            </select>

            <button class="admin-btn" type="submit">Lọc</button>

            <c:if test="${not empty f_q || not empty f_role || not empty f_rank || not empty f_active}">
              <a class="admin-btn" href="${pageContext.request.contextPath}/admin/users">
                Xóa lọc
              </a>
            </c:if>

          </form>
        </div>

        <c:choose>
          <c:when test="${empty users}">
            <div class="admin-empty">Chưa có user phù hợp.</div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
              <tr>
                <th style="width:80px;">ID</th>
                <th>Username</th>
                <th style="width:140px;">Role</th>
                <th style="width:160px;">Rank</th>
                <th style="width:150px;">Mật khẩu</th>
                <th style="width:130px;">Trạng thái</th>
                <th style="width:680px;">Thao tác</th>
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

                    <c:if test="${not empty u.fullName}">
                      <div class="admin-muted" style="margin-top:4px;">
                        <c:out value="${u.fullName}"/>
                      </div>
                    </c:if>

                    <c:if test="${not empty u.email}">
                      <div class="admin-muted" style="margin-top:4px;">
                        <c:out value="${u.email}"/>
                      </div>
                    </c:if>
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

                  <td>
                    <c:choose>
                      <c:when test="${empty u.manualRankCode}">
                        <span class="admin-pill">AUTO</span>
                        <div class="admin-muted" style="margin-top:4px;">
                          Theo chi tiêu
                        </div>
                      </c:when>
                      <c:otherwise>
                          <span class="admin-pill admin-pill--ok">
                            <c:out value="${u.manualRankCode}"/>
                          </span>
                        <div class="admin-muted" style="margin-top:4px;">
                          Admin đặt
                        </div>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td>
                      <span class="admin-pill">
                        <c:out value="${u.passwordStatusLabel}"/>
                      </span>
                    <div class="admin-muted" style="margin-top:4px;">
                      <c:out value="${u.passwordMasked}"/>
                    </div>
                  </td>

                  <td>
                    <c:choose>
                      <c:when test="${u.active}">
                        <span class="admin-pill admin-pill--ok">ACTIVE</span>
                      </c:when>
                      <c:otherwise>
                        <span class="admin-pill admin-pill--danger">INACTIVE</span>
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

                      <%@ include file="/jsp/common/csrf.jspf" %>

                      <input type="hidden" name="action" value="changeRole"/>
                      <input type="hidden" name="id" value="${u.id}"/>

                      <select class="admin-select" name="role" style="width:120px;">
                        <option value="USER"  ${u.role == 'USER' ? 'selected' : ''}>USER</option>
                        <option value="ADMIN" ${u.role == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                      </select>

                      <button class="admin-btn" type="submit">Đổi role</button>
                    </form>

                    <!-- Đổi rank nhanh -->
                    <form method="post"
                          action="${pageContext.request.contextPath}/admin/users"
                          class="admin-inline">

                      <%@ include file="/jsp/common/csrf.jspf" %>

                      <input type="hidden" name="action" value="changeRank"/>
                      <input type="hidden" name="id" value="${u.id}"/>

                      <select class="admin-select" name="manualRankCode" style="width:150px;">
                        <option value="AUTO" ${empty u.manualRankCode ? 'selected' : ''}>
                          AUTO
                        </option>

                        <c:forEach var="r" items="${ranks}">
                          <option value="${r.code}" ${u.manualRankCode == r.code ? 'selected' : ''}>
                              ${r.code}
                          </option>
                        </c:forEach>
                      </select>

                      <button class="admin-btn" type="submit">Đổi rank</button>
                    </form>

                    <!-- XÓA USER -->
                    <form method="post"
                          action="${pageContext.request.contextPath}/admin/users"
                          class="admin-inline"
                          onsubmit="return confirm('Xóa user #${u.id} (${u.username})? Hành động này không thể hoàn tác.');">

                      <%@ include file="/jsp/common/csrf.jspf" %>

                      <input type="hidden" name="action" value="delete"/>
                      <input type="hidden" name="id" value="${u.id}"/>

                      <c:choose>
                        <c:when test="${not empty sessionScope.user and sessionScope.user.id == u.id}">
                          <button class="admin-btn"
                                  type="button"
                                  disabled
                                  style="opacity:.55;cursor:not-allowed;">
                            Xóa
                          </button>
                        </c:when>

                        <c:otherwise>
                          <button class="admin-btn"
                                  type="submit"
                                  style="background:#e53935;border-color:#e53935;">
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
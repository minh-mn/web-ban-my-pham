<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Users" scope="request"/>
<c:set var="activeMenu" value="users" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<style>
  .user-toolbar-form {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
  }

  .user-toolbar-form .admin-input {
    min-width: 260px;
  }

  .user-table-wrap {
    width: 100%;
    overflow-x: auto;
  }

  .user-table {
    min-width: 980px;
  }

  .user-main-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .user-name-link {
    color: inherit;
    text-decoration: none;
    font-weight: 850;
  }

  .user-name-link:hover {
    text-decoration: underline;
  }

  .user-sub-info {
    color: #6b7280;
    font-size: 13px;
    line-height: 1.35;
  }

  .user-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
  }

  .user-danger-btn {
    background: #e53935 !important;
    border-color: #e53935 !important;
    color: #fff !important;
  }

  .user-self-disabled {
    opacity: .55;
    cursor: not-allowed;
  }
</style>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Users</h1>
        <p class="admin-subtext">
          Quản lý tài khoản, vai trò, trạng thái và rank khách hàng.
        </p>
      </div>
    </div>

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

        <div class="admin-toolbar">
          <form method="get"
                action="${pageContext.request.contextPath}/admin/users"
                class="admin-toolbar__form user-toolbar-form">

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

              <c:forEach var="r" items="${ranks}">
                <option value="${r.code}" ${f_rank == r.code ? 'selected' : ''}>
                    ${r.code}
                </option>
              </c:forEach>
            </select>

            <select class="admin-select" name="active" style="width:170px;">
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
            <div class="user-table-wrap">
              <table class="admin-table user-table">
                <thead>
                <tr>
                  <th style="width:70px;">ID</th>
                  <th>Thông tin user</th>
                  <th style="width:120px;">Role</th>
                  <th style="width:150px;">Rank hiện tại</th>
                  <th style="width:130px;">Trạng thái</th>
                  <th style="width:220px;">Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="u" items="${users}">
                  <tr>
                    <td>#${u.id}</td>

                    <td>
                      <div class="user-main-info">
                        <a class="user-name-link"
                           href="${pageContext.request.contextPath}/admin/users?action=detail&id=${u.id}">
                          <c:out value="${u.username}"/>
                        </a>

                        <c:if test="${not empty u.fullName}">
                          <div class="user-sub-info">
                            <c:out value="${u.fullName}"/>
                          </div>
                        </c:if>

                        <c:if test="${not empty u.email}">
                          <div class="user-sub-info">
                            <c:out value="${u.email}"/>
                          </div>
                        </c:if>

                        <c:if test="${not empty u.phone}">
                          <div class="user-sub-info">
                            <c:out value="${u.phone}"/>
                          </div>
                        </c:if>
                      </div>
                    </td>

                    <td>
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
                      <span class="admin-pill admin-pill--ok">
                        <c:out value="${u.displayRankCode}"/>
                      </span>
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

                    <td>
                      <div class="user-actions">
                        <a class="admin-btn"
                           href="${pageContext.request.contextPath}/admin/users?action=edit&id=${u.id}">
                          Sửa
                        </a>

                        <a class="admin-btn"
                           href="${pageContext.request.contextPath}/admin/users?action=detail&id=${u.id}">
                          Chi tiết
                        </a>

                        <form method="post"
                              action="${pageContext.request.contextPath}/admin/users"
                              class="admin-inline"
                              onsubmit="return confirm('Xóa user #${u.id} (${u.username})? Hành động này không thể hoàn tác.');">

                          <%@ include file="/jsp/common/csrf.jspf" %>

                          <input type="hidden" name="action" value="delete"/>
                          <input type="hidden" name="id" value="${u.id}"/>

                          <c:choose>
                            <c:when test="${not empty sessionScope.user and sessionScope.user.id == u.id}">
                              <button class="admin-btn user-self-disabled"
                                      type="button"
                                      disabled>
                                Xóa
                              </button>
                            </c:when>

                            <c:otherwise>
                              <button class="admin-btn user-danger-btn"
                                      type="submit">
                                Xóa
                              </button>
                            </c:otherwise>
                          </c:choose>
                        </form>
                      </div>
                    </td>

                  </tr>
                </c:forEach>
                </tbody>
              </table>
            </div>
          </c:otherwise>
        </c:choose>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>
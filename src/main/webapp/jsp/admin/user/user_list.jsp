<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Quản lý user" scope="request"/>
<c:set var="activeMenu" value="users" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${ctx}" />

<c:set var="currentAdminId" value="0"/>
<c:choose>
  <c:when test="${not empty sessionScope.user}">
    <c:set var="currentAdminId" value="${sessionScope.user.id}"/>
  </c:when>
  <c:when test="${not empty sessionScope.authUser}">
    <c:set var="currentAdminId" value="${sessionScope.authUser.id}"/>
  </c:when>
  <c:when test="${not empty sessionScope.currentUser}">
    <c:set var="currentAdminId" value="${sessionScope.currentUser.id}"/>
  </c:when>
</c:choose>

<c:set var="totalUsers" value="0"/>
<c:set var="adminUsers" value="0"/>
<c:set var="activeUsers" value="0"/>
<c:set var="lockedUsers" value="0"/>
<c:set var="manualRankUsers" value="0"/>

<c:forEach var="u" items="${users}">
  <c:set var="totalUsers" value="${totalUsers + 1}"/>

  <c:if test="${u.role == 'ADMIN'}">
    <c:set var="adminUsers" value="${adminUsers + 1}"/>
  </c:if>

  <c:choose>
    <c:when test="${u.active}">
      <c:set var="activeUsers" value="${activeUsers + 1}"/>
    </c:when>
    <c:otherwise>
      <c:set var="lockedUsers" value="${lockedUsers + 1}"/>
    </c:otherwise>
  </c:choose>

  <c:if test="${not empty u.manualRankCode}">
    <c:set var="manualRankUsers" value="${manualRankUsers + 1}"/>
  </c:if>
</c:forEach>

<main class="admin-main">
  <div class="admin-container admin-user-page">

    <section class="admin-user-hero">
      <div class="admin-user-hero__content">
        <span class="admin-user-eyebrow">TÀI KHOẢN &amp; PHÂN QUYỀN</span>
        <h1 class="admin-h1 admin-user-title">Quản lý user</h1>
        <p class="admin-subtext admin-user-subtitle">
          Hiển thị tên tài khoản, role, rank, trạng thái và thông tin liên hệ.
          Admin chỉ được quản trị role, rank và trạng thái; không tự ý sửa thông tin cá nhân nếu user chưa yêu cầu.
        </p>
      </div>

      <div class="admin-user-hero__actions">
        <a class="admin-btn" href="${ctx}/admin/users">
          Làm mới
        </a>
      </div>
    </section>

    <c:if test="${not empty success}">
      <div class="admin-alert admin-alert--success">
        <c:out value="${success}"/>
      </div>
    </c:if>

    <c:if test="${not empty successMessage and empty success}">
      <div class="admin-alert admin-alert--success">
        <c:out value="${successMessage}"/>
      </div>
    </c:if>

    <c:if test="${not empty error}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${error}"/>
      </div>
    </c:if>

    <c:if test="${not empty errorMessage and empty error}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${errorMessage}"/>
      </div>
    </c:if>

    <c:if test="${param.msg == 'deleted'}">
      <div class="admin-alert admin-alert--success">
        Đã khóa tài khoản user. Hệ thống không xóa cứng dữ liệu để bảo toàn lịch sử đơn hàng.
      </div>
    </c:if>

    <c:if test="${param.err == 'delete_failed'}">
      <div class="admin-alert admin-alert--danger">
        Không thể xử lý tài khoản. Hãy kiểm tra quyền thao tác hoặc trạng thái user.
      </div>
    </c:if>

    <c:if test="${param.err == 'cannot_delete_self'}">
      <div class="admin-alert admin-alert--danger">
        Admin không thể tự xóa hoặc khóa tài khoản đang đăng nhập.
      </div>
    </c:if>

    <section class="admin-user-stats">
      <div class="admin-user-stat admin-user-stat--total">
        <span class="admin-user-stat__icon">👥</span>
        <span class="admin-user-stat__label">Tổng user</span>
        <strong class="admin-user-stat__value">${totalUsers}</strong>
        <span class="admin-user-stat__note">Theo bộ lọc hiện tại</span>
      </div>

      <div class="admin-user-stat admin-user-stat--admin">
        <span class="admin-user-stat__icon">🛡️</span>
        <span class="admin-user-stat__label">Admin</span>
        <strong class="admin-user-stat__value">${adminUsers}</strong>
        <span class="admin-user-stat__note">Tài khoản được bảo vệ</span>
      </div>

      <div class="admin-user-stat admin-user-stat--active">
        <span class="admin-user-stat__icon">✅</span>
        <span class="admin-user-stat__label">Đang hoạt động</span>
        <strong class="admin-user-stat__value">${activeUsers}</strong>
        <span class="admin-user-stat__note">Có thể đăng nhập</span>
      </div>

      <div class="admin-user-stat admin-user-stat--locked">
        <span class="admin-user-stat__icon">🔒</span>
        <span class="admin-user-stat__label">Đã khóa</span>
        <strong class="admin-user-stat__value">${lockedUsers}</strong>
        <span class="admin-user-stat__note">Không thể đăng nhập</span>
      </div>

      <div class="admin-user-stat admin-user-stat--rank">
        <span class="admin-user-stat__icon">🏅</span>
        <span class="admin-user-stat__label">Rank thủ công</span>
        <strong class="admin-user-stat__value">${manualRankUsers}</strong>
        <span class="admin-user-stat__note">Admin chỉ định</span>
      </div>
    </section>

    <section class="admin-card admin-user-filter-card">
      <div class="admin-card__body">
        <div class="admin-user-section-head">
          <div>
            <h2 class="admin-user-section-title">Bộ lọc user</h2>
            <p class="admin-user-section-desc">
              Tìm theo ID, username, họ tên, email, SĐT và lọc theo role, rank, trạng thái.
            </p>
          </div>

          <c:if test="${not empty f_q || not empty f_role || not empty f_rank || not empty f_active}">
            <div class="admin-user-active-filters">
              <c:if test="${not empty f_q}">
                <span class="admin-chip">Từ khóa: <strong><c:out value="${f_q}"/></strong></span>
              </c:if>
              <c:if test="${not empty f_role}">
                <span class="admin-chip">Role: <strong><c:out value="${f_role}"/></strong></span>
              </c:if>
              <c:if test="${not empty f_rank}">
                <span class="admin-chip">Rank: <strong><c:out value="${f_rank}"/></strong></span>
              </c:if>
              <c:if test="${not empty f_active}">
                <span class="admin-chip">
                  Trạng thái:
                  <strong>
                    <c:choose>
                      <c:when test="${f_active == '1'}">ACTIVE</c:when>
                      <c:otherwise>INACTIVE</c:otherwise>
                    </c:choose>
                  </strong>
                </span>
              </c:if>
            </div>
          </c:if>
        </div>

        <form method="get"
              action="${ctx}/admin/users"
              class="admin-user-filter-form">

          <label class="admin-user-filter-field admin-user-filter-field--keyword">
            <span>Tìm kiếm</span>
            <input class="admin-input"
                   type="text"
                   name="q"
                   value="${f_q}"
                   placeholder="Nhập ID, username, họ tên, email hoặc SĐT...">
          </label>

          <label class="admin-user-filter-field">
            <span>Role</span>
            <select class="admin-select" name="role">
              <option value="" ${empty f_role ? 'selected' : ''}>Tất cả role</option>
              <c:forEach var="roleItem" items="${roles}">
                <option value="${roleItem.code}" ${f_role == roleItem.code ? 'selected' : ''}>
                  ${roleItem.code} - ${roleItem.name}
                </option>
              </c:forEach>
            </select>
          </label>

          <label class="admin-user-filter-field">
            <span>Rank</span>
            <select class="admin-select" name="rank">
              <option value="" ${empty f_rank ? 'selected' : ''}>Tất cả rank</option>
              <option value="AUTO" ${f_rank == 'AUTO' ? 'selected' : ''}>AUTO</option>

              <c:forEach var="r" items="${ranks}">
                <option value="${r.code}" ${f_rank == r.code ? 'selected' : ''}>
                    ${r.code}
                </option>
              </c:forEach>
            </select>
          </label>

          <label class="admin-user-filter-field">
            <span>Trạng thái</span>
            <select class="admin-select" name="active">
              <option value="" ${empty f_active ? 'selected' : ''}>Tất cả trạng thái</option>
              <option value="1" ${f_active == '1' ? 'selected' : ''}>Đang hoạt động</option>
              <option value="0" ${f_active == '0' ? 'selected' : ''}>Đã khóa</option>
            </select>
          </label>

          <div class="admin-user-filter-actions">
            <button class="admin-btn admin-user-filter-btn" type="submit">
              Lọc
            </button>

            <a class="admin-btn admin-user-filter-btn"
               href="${ctx}/admin/users">
              Xóa lọc
            </a>
          </div>
        </form>
      </div>
    </section>

    <section class="admin-card admin-user-list-card">
      <div class="admin-card__body">

        <div class="admin-user-section-head admin-user-section-head--list">
          <div>
            <h2 class="admin-user-section-title">Danh sách user</h2>
            <p class="admin-user-section-desc">
              Đang hiển thị <strong>${totalUsers}</strong> tài khoản.
              Tài khoản ADMIN và tài khoản hiện tại được bảo vệ khỏi thao tác khóa/xóa.
            </p>
          </div>
        </div>

        <c:choose>
          <c:when test="${empty users}">
            <div class="admin-user-empty">
              <div class="admin-user-empty__icon">👤</div>
              <div>
                <h3>Chưa có user phù hợp</h3>
                <p>Thử thay đổi từ khóa hoặc bỏ bớt bộ lọc để xem thêm tài khoản.</p>
                <a class="admin-btn" href="${ctx}/admin/users">
                  Xóa bộ lọc
                </a>
              </div>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-user-table-wrap">
              <table class="admin-table admin-user-table">
                <thead>
                <tr>
                  <th class="admin-user-col-id">ID</th>
                  <th class="admin-user-col-account">Tài khoản</th>
                  <th class="admin-user-col-contact">Liên hệ</th>
                  <th class="admin-user-col-role">Role</th>
                  <th class="admin-user-col-rank">Rank</th>
                  <th class="admin-user-col-status">Trạng thái</th>
                  <th class="admin-user-col-created">Ngày tạo</th>
                  <th class="admin-user-col-actions">Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="u" items="${users}">
                  <c:set var="isSelf" value="${currentAdminId == u.id}"/>
                  <c:set var="isProtectedAdmin" value="${u.role == 'ADMIN'}"/>
                  <c:set var="canModerate" value="${not isSelf and not isProtectedAdmin}"/>

                  <tr class="${u.active ? '' : 'admin-user-row--locked'} ${isProtectedAdmin ? 'admin-user-row--admin' : ''}">
                    <td class="admin-user-id-cell">
                      <strong>#${u.id}</strong>
                    </td>

                    <td>
                      <div class="admin-user-account">
                        <div class="admin-user-avatar">
                          #${u.id}
                        </div>

                        <div class="admin-user-account__body">
                          <a class="admin-user-name"
                             href="${ctx}/admin/users?action=detail&id=${u.id}">
                            <c:out value="${u.username}"/>
                          </a>

                          <div class="admin-user-subline">
                            <c:choose>
                              <c:when test="${not empty u.fullName}">
                                <c:out value="${u.fullName}"/>
                              </c:when>
                              <c:otherwise>
                                <span class="admin-muted">Chưa cập nhật họ tên</span>
                              </c:otherwise>
                            </c:choose>
                          </div>

                          <div class="admin-user-chip-row">
                            <c:if test="${isSelf}">
                              <span class="admin-pill admin-pill--warning">Tài khoản của bạn</span>
                            </c:if>

                            <c:if test="${isProtectedAdmin}">
                              <span class="admin-pill admin-pill--danger">Được bảo vệ</span>
                            </c:if>
                          </div>
                        </div>
                      </div>
                    </td>

                    <td>
                      <div class="admin-user-contact">
                        <c:choose>
                          <c:when test="${not empty u.email}">
                            <span>Email: <strong><c:out value="${u.email}"/></strong></span>
                          </c:when>
                          <c:otherwise>
                            <span>Email: <em>Chưa cập nhật</em></span>
                          </c:otherwise>
                        </c:choose>

                        <c:choose>
                          <c:when test="${not empty u.phone}">
                            <span>SĐT: <strong><c:out value="${u.phone}"/></strong></span>
                          </c:when>
                          <c:otherwise>
                            <span>SĐT: <em>Chưa cập nhật</em></span>
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${u.role == 'ADMIN'}">
                          <span class="admin-pill admin-pill--danger">ADMIN</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill admin-pill--info"><c:out value="${u.role}"/></span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <div class="admin-user-rank">
                        <span class="admin-pill admin-pill--ok">
                          <c:out value="${u.displayRankCode}"/>
                        </span>

                        <span class="admin-user-rank__name">
                          <c:out value="${u.displayRankName}"/>
                        </span>

                        <span class="admin-user-rank__mode">
                          <c:choose>
                            <c:when test="${not empty u.manualRankCode}">
                              MANUAL
                            </c:when>
                            <c:otherwise>
                              AUTO
                            </c:otherwise>
                          </c:choose>
                        </span>
                      </div>
                    </td>

                    <td>
                      <div class="admin-user-status">
                        <c:choose>
                          <c:when test="${u.active}">
                            <span class="admin-pill admin-pill--ok">Đang hoạt động</span>
                            <span class="admin-user-status__hint">Có thể đăng nhập</span>
                          </c:when>
                          <c:otherwise>
                            <span class="admin-pill admin-pill--danger">Đã khóa</span>
                            <span class="admin-user-status__hint">Không thể đăng nhập</span>
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </td>

                    <td>
                      <div class="admin-user-created">
                        <c:choose>
                          <c:when test="${not empty u.createdAt}">
                            <fmt:formatDate value="${u.createdAt}" pattern="dd/MM/yyyy"/>
                            <span>
                              <fmt:formatDate value="${u.createdAt}" pattern="HH:mm"/>
                            </span>
                          </c:when>
                          <c:otherwise>
                            <span class="admin-muted">Không rõ</span>
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </td>

                    <td class="admin-user-action-cell">
                      <div class="admin-user-actions">

                        <a class="admin-btn admin-user-action-btn admin-user-action-btn--view"
                           href="${ctx}/admin/users?action=detail&id=${u.id}">
                          Chi tiết
                        </a>

                        <a class="admin-btn admin-user-action-btn"
                           href="${ctx}/admin/users?action=edit&id=${u.id}">
                          Sửa quyền
                        </a>

                        <form method="post"
                              action="${ctx}/admin/users"
                              class="admin-inline"
                              onsubmit="return confirm('${u.active ? 'Khóa tài khoản user này?' : 'Mở khóa tài khoản user này?'}');">
                          <%@ include file="/jsp/common/csrf.jspf" %>

                          <input type="hidden" name="action" value="toggleLock"/>
                          <input type="hidden" name="id" value="${u.id}"/>

                          <c:choose>
                            <c:when test="${canModerate}">
                              <button class="admin-btn admin-user-action-btn ${u.active ? 'admin-btn--danger' : 'admin-btn--ok'}"
                                      type="submit">
                                <c:choose>
                                  <c:when test="${u.active}">Khóa</c:when>
                                  <c:otherwise>Mở khóa</c:otherwise>
                                </c:choose>
                              </button>
                            </c:when>

                            <c:otherwise>
                              <button class="admin-btn admin-user-action-btn admin-user-action-btn--disabled"
                                      type="button"
                                      disabled
                                      title="Không thể khóa/xóa tài khoản ADMIN hoặc tài khoản đang đăng nhập">
                                Bảo vệ
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
    </section>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Chi tiết User" scope="request"/>
<c:set var="activeMenu" value="users" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="currentAdmin" value="${sessionScope.user}" />
<c:if test="${empty currentAdmin and not empty sessionScope.authUser}">
  <c:set var="currentAdmin" value="${sessionScope.authUser}" />
</c:if>
<c:if test="${empty currentAdmin and not empty sessionScope.currentUser}">
  <c:set var="currentAdmin" value="${sessionScope.currentUser}" />
</c:if>

<c:set var="isSelf" value="${not empty currentAdmin and not empty user and currentAdmin.id == user.id}" />
<c:set var="isAdminAccount" value="${not empty user and user.role == 'ADMIN'}" />
<c:set var="isProtectedAccount" value="${isSelf or isAdminAccount}" />

<main class="admin-main">
  <div class="admin-container user-detail-page user-detail-page--issue130">

    <div class="user-detail-hero">
      <div class="user-detail-hero__content">
        <span class="user-detail-eyebrow">Issue 130 · Quản lí user</span>
        <h1 class="admin-h1 user-detail-title">
          Chi tiết tài khoản
          <c:if test="${not empty user}">#${user.id}</c:if>
        </h1>
        <p class="admin-subtext user-detail-subtitle">
          Xem thông tin tài khoản, role, rank, trạng thái đăng nhập và quyền quản trị.
          Admin chỉ được quản lí quyền, trạng thái và rank; thông tin cá nhân chỉ xem.
        </p>
      </div>

      <div class="user-detail-hero__actions">
        <a class="admin-btn" href="${pageContext.request.contextPath}/admin/users">
          Quay lại danh sách
        </a>
        <c:if test="${not empty user}">
          <a class="admin-btn admin-btn--primary"
             href="${pageContext.request.contextPath}/admin/users?action=edit&id=${user.id}">
            Sửa quyền / trạng thái
          </a>
        </c:if>
      </div>
    </div>

    <c:if test="${not empty successMessage}">
      <div class="admin-alert admin-alert--success">
        <c:out value="${successMessage}" />
      </div>
    </c:if>

    <c:if test="${not empty success}">
      <div class="admin-alert admin-alert--success">
        <c:out value="${success}" />
      </div>
    </c:if>

    <c:if test="${not empty errorMessage}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${errorMessage}" />
      </div>
    </c:if>

    <c:if test="${not empty error}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${error}" />
      </div>
    </c:if>

    <c:choose>
      <c:when test="${empty user}">
        <div class="admin-card">
          <div class="admin-card__body">
            <div class="admin-empty">Không tìm thấy user.</div>
          </div>
        </div>
      </c:when>

      <c:otherwise>

        <section class="user-detail-summary">
          <div class="user-detail-summary-card user-detail-summary-card--account">
            <span class="user-detail-summary-label">Tài khoản</span>
            <strong class="user-detail-summary-value">
              <c:out value="${user.username}" />
            </strong>
            <span class="user-detail-summary-note">User ID: #${user.id}</span>
          </div>

          <div class="user-detail-summary-card">
            <span class="user-detail-summary-label">Role</span>
            <strong class="user-detail-summary-value">
              <c:choose>
                <c:when test="${user.role == 'ADMIN'}">ADMIN</c:when>
                <c:otherwise>USER</c:otherwise>
              </c:choose>
            </strong>
            <span class="user-detail-summary-note">
              <c:choose>
                <c:when test="${user.role == 'ADMIN'}">Tài khoản quản trị</c:when>
                <c:otherwise>Tài khoản khách hàng</c:otherwise>
              </c:choose>
            </span>
          </div>

          <div class="user-detail-summary-card">
            <span class="user-detail-summary-label">Trạng thái</span>
            <strong class="user-detail-summary-value">
              <c:choose>
                <c:when test="${user.active}">Đang hoạt động</c:when>
                <c:otherwise>Đã khóa</c:otherwise>
              </c:choose>
            </strong>
            <span class="user-detail-summary-note">
              <c:choose>
                <c:when test="${user.active}">Có thể đăng nhập</c:when>
                <c:otherwise>Không thể đăng nhập</c:otherwise>
              </c:choose>
            </span>
          </div>

          <div class="user-detail-summary-card">
            <span class="user-detail-summary-label">Rank</span>
            <strong class="user-detail-summary-value">
              <c:out value="${user.displayRankCode}" />
            </strong>
            <span class="user-detail-summary-note">
              <c:out value="${user.displayRankName}" />
            </span>
          </div>
        </section>

        <div class="user-detail-layout">

          <div class="user-detail-main">

            <section class="user-detail-card">
              <div class="user-detail-card__header">
                <div>
                  <h2 class="user-detail-card__title">Thông tin tài khoản</h2>
                  <p class="user-detail-card__desc">
                    Thông tin định danh và trạng thái của tài khoản trong hệ thống.
                  </p>
                </div>

                <div class="user-detail-chip-row user-detail-chip-row--right">
                  <c:choose>
                    <c:when test="${user.role == 'ADMIN'}">
                      <span class="admin-pill admin-pill--danger">ADMIN</span>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-pill admin-pill--info">USER</span>
                    </c:otherwise>
                  </c:choose>

                  <c:choose>
                    <c:when test="${user.active}">
                      <span class="admin-pill admin-pill--ok">ACTIVE</span>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-pill admin-pill--danger">LOCKED</span>
                    </c:otherwise>
                  </c:choose>

                  <c:if test="${isSelf}">
                    <span class="admin-pill admin-pill--warning">Tài khoản hiện tại</span>
                  </c:if>
                </div>
              </div>

              <div class="user-detail-card__body">
                <div class="user-detail-identity">
                  <div class="user-detail-avatar">
                    #${user.id}
                  </div>

                  <div class="user-detail-identity__body">
                    <h3 class="user-detail-name">
                      <c:choose>
                        <c:when test="${not empty user.fullName}">
                          <c:out value="${user.fullName}" />
                        </c:when>
                        <c:otherwise>
                          <c:out value="${user.username}" />
                        </c:otherwise>
                      </c:choose>
                    </h3>

                    <div class="user-detail-meta">
                      Username: <strong><c:out value="${user.username}" /></strong>
                      · ID: <strong>#${user.id}</strong>
                    </div>

                    <div class="user-detail-chip-row">
                      <span class="admin-chip">
                        Role: <strong><c:out value="${user.role}" /></strong>
                      </span>
                      <span class="admin-chip">
                        Rank: <strong><c:out value="${user.displayRankCode}" /></strong>
                      </span>
                      <span class="admin-chip">
                        Mật khẩu: <strong><c:out value="${user.passwordStatusLabel}" /></strong>
                      </span>
                    </div>
                  </div>
                </div>

                <div class="user-detail-info-grid user-detail-info-grid--3">
                  <div class="user-detail-info-item">
                    <span class="user-detail-info-label">ID</span>
                    <strong class="user-detail-info-value">#${user.id}</strong>
                  </div>

                  <div class="user-detail-info-item">
                    <span class="user-detail-info-label">Username</span>
                    <strong class="user-detail-info-value">
                      <c:out value="${user.username}" />
                    </strong>
                  </div>

                  <div class="user-detail-info-item">
                    <span class="user-detail-info-label">Ngày tạo</span>
                    <strong class="user-detail-info-value">
                      <c:choose>
                        <c:when test="${not empty user.createdAt}">
                          <fmt:formatDate value="${user.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                        </c:when>
                        <c:otherwise>
                          <span class="user-detail-muted">Không có dữ liệu</span>
                        </c:otherwise>
                      </c:choose>
                    </strong>
                  </div>
                </div>
              </div>
            </section>

            <section class="user-detail-card">
              <div class="user-detail-card__header">
                <div>
                  <h2 class="user-detail-card__title">Thông tin cá nhân</h2>
                  <p class="user-detail-card__desc">
                    Khu vực chỉ xem. Admin không được tự ý sửa nếu user chưa yêu cầu.
                  </p>
                </div>
                <span class="admin-pill admin-pill--warning">Chỉ xem</span>
              </div>

              <div class="user-detail-card__body">
                <div class="user-policy-note user-policy-note--compact">
                  <div class="user-policy-note__icon">!</div>
                  <div>
                    <strong>Quy tắc quyền hạn</strong>
                    <p>
                      Admin không chỉnh trực tiếp họ tên, email, số điện thoại, ngày sinh hoặc giới tính của user.
                      Các thay đổi thông tin cá nhân nên để user tự cập nhật hoặc xử lý qua yêu cầu hỗ trợ riêng.
                    </p>
                  </div>
                </div>

                <div class="user-detail-info-grid">
                  <div class="user-detail-info-item">
                    <span class="user-detail-info-label">Họ tên</span>
                    <strong class="user-detail-info-value">
                      <c:choose>
                        <c:when test="${not empty user.fullName}">
                          <c:out value="${user.fullName}" />
                        </c:when>
                        <c:otherwise>
                          <span class="user-detail-muted">Chưa cập nhật</span>
                        </c:otherwise>
                      </c:choose>
                    </strong>
                  </div>

                  <div class="user-detail-info-item">
                    <span class="user-detail-info-label">Email</span>
                    <strong class="user-detail-info-value">
                      <c:choose>
                        <c:when test="${not empty user.email}">
                          <c:out value="${user.email}" />
                        </c:when>
                        <c:otherwise>
                          <span class="user-detail-muted">Chưa cập nhật</span>
                        </c:otherwise>
                      </c:choose>
                    </strong>
                  </div>

                  <div class="user-detail-info-item">
                    <span class="user-detail-info-label">Số điện thoại</span>
                    <strong class="user-detail-info-value">
                      <c:choose>
                        <c:when test="${not empty user.phone}">
                          <c:out value="${user.phone}" />
                        </c:when>
                        <c:otherwise>
                          <span class="user-detail-muted">Chưa cập nhật</span>
                        </c:otherwise>
                      </c:choose>
                    </strong>
                  </div>

                  <div class="user-detail-info-item">
                    <span class="user-detail-info-label">Ngày sinh</span>
                    <strong class="user-detail-info-value">
                      <c:choose>
                        <c:when test="${not empty user.birthDate}">
                          <c:out value="${user.birthDate}" />
                        </c:when>
                        <c:otherwise>
                          <span class="user-detail-muted">Chưa cập nhật</span>
                        </c:otherwise>
                      </c:choose>
                    </strong>
                  </div>

                  <div class="user-detail-info-item">
                    <span class="user-detail-info-label">Giới tính</span>
                    <strong class="user-detail-info-value">
                      <c:choose>
                        <c:when test="${not empty user.gender}">
                          <c:out value="${user.gender}" />
                        </c:when>
                        <c:otherwise>
                          <span class="user-detail-muted">Chưa cập nhật</span>
                        </c:otherwise>
                      </c:choose>
                    </strong>
                  </div>

                  <div class="user-detail-info-item">
                    <span class="user-detail-info-label">Quyền sửa</span>
                    <strong class="user-detail-info-value user-detail-muted">
                      User tự cập nhật / yêu cầu hỗ trợ
                    </strong>
                  </div>
                </div>
              </div>
            </section>

            <section class="user-detail-card">
              <div class="user-detail-card__header">
                <div>
                  <h2 class="user-detail-card__title">Rank và đăng nhập</h2>
                  <p class="user-detail-card__desc">
                    Theo dõi rank hiện tại, rank thủ công và phương thức đăng nhập.
                  </p>
                </div>
              </div>

              <div class="user-detail-card__body">
                <div class="user-detail-info-grid">
                  <div class="user-detail-info-item">
                    <span class="user-detail-info-label">Rank hiện tại</span>
                    <strong class="user-detail-info-value">
                      <c:out value="${user.displayRankName}" />
                      (<c:out value="${user.displayRankCode}" />)
                    </strong>
                  </div>

                  <div class="user-detail-info-item">
                    <span class="user-detail-info-label">Chế độ rank</span>
                    <strong class="user-detail-info-value">
                      <c:choose>
                        <c:when test="${empty user.manualRankCode}">
                          <span class="admin-pill admin-pill--warning">AUTO</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill admin-pill--info">MANUAL</span>
                        </c:otherwise>
                      </c:choose>
                    </strong>
                  </div>

                  <div class="user-detail-info-item">
                    <span class="user-detail-info-label">Rank admin gán</span>
                    <strong class="user-detail-info-value">
                      <c:choose>
                        <c:when test="${empty user.manualRankCode}">
                          <span class="user-detail-muted">AUTO - hệ thống tự tính</span>
                        </c:when>
                        <c:otherwise>
                          <c:out value="${user.manualRankCode}" />
                        </c:otherwise>
                      </c:choose>
                    </strong>
                  </div>

                  <div class="user-detail-info-item">
                    <span class="user-detail-info-label">Mật khẩu</span>
                    <strong class="user-detail-info-value">
                      <span class="admin-pill">
                        <c:out value="${user.passwordStatusLabel}" />
                      </span>
                      <span class="user-detail-password-mask">
                        <c:out value="${user.passwordMasked}" />
                      </span>
                    </strong>
                  </div>

                  <div class="user-detail-info-item">
                    <span class="user-detail-info-label">Google Login</span>
                    <strong class="user-detail-info-value">
                      <c:choose>
                        <c:when test="${not empty user.googleId}">
                          <span class="admin-pill admin-pill--ok">Đã liên kết</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill">Chưa liên kết</span>
                        </c:otherwise>
                      </c:choose>
                    </strong>
                  </div>

                  <div class="user-detail-info-item">
                    <span class="user-detail-info-label">Facebook Login</span>
                    <strong class="user-detail-info-value">
                      <c:choose>
                        <c:when test="${not empty user.facebookId}">
                          <span class="admin-pill admin-pill--ok">Đã liên kết</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill">Chưa liên kết</span>
                        </c:otherwise>
                      </c:choose>
                    </strong>
                  </div>
                </div>
              </div>
            </section>

          </div>

          <aside class="user-detail-side">

            <section class="user-detail-card user-detail-card--side">
              <div class="user-detail-card__header">
                <div>
                  <h2 class="user-detail-card__title">Thao tác</h2>
                  <p class="user-detail-card__desc">
                    Không xóa cứng tài khoản. Chỉ khóa/mở khóa user thường.
                  </p>
                </div>
              </div>

              <div class="user-detail-card__body">
                <div class="user-detail-action-list">
                  <a class="admin-btn admin-btn--primary user-detail-action-btn"
                     href="${pageContext.request.contextPath}/admin/users?action=edit&id=${user.id}">
                    Sửa quyền / trạng thái
                  </a>

                  <c:choose>
                    <c:when test="${isProtectedAccount}">
                      <button class="admin-btn user-detail-action-btn" type="button" disabled>
                        Tài khoản được bảo vệ
                      </button>
                      <div class="user-detail-lock-note">
                        <c:choose>
                          <c:when test="${isSelf}">
                            Đây là tài khoản admin đang đăng nhập, không được tự khóa hoặc tự đổi quyền.
                          </c:when>
                          <c:otherwise>
                            Tài khoản ADMIN không được khóa/xóa trực tiếp từ giao diện quản lí user.
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </c:when>

                    <c:otherwise>
                      <form method="post"
                            action="${pageContext.request.contextPath}/admin/users"
                            class="user-detail-action-form"
                            onsubmit="return confirm('${user.active ? 'Khóa tài khoản user này?' : 'Mở khóa tài khoản user này?'}');">
                        <%@ include file="/jsp/common/csrf.jspf" %>
                        <input type="hidden" name="action" value="toggleLock" />
                        <input type="hidden" name="id" value="${user.id}" />

                        <button class="admin-btn ${user.active ? 'admin-btn--danger' : 'admin-btn--ok'} user-detail-action-btn"
                                type="submit">
                          <c:choose>
                            <c:when test="${user.active}">Khóa tài khoản</c:when>
                            <c:otherwise>Mở khóa tài khoản</c:otherwise>
                          </c:choose>
                        </button>
                      </form>

                      <div class="user-detail-lock-note">
                        Thao tác này chỉ đổi trạng thái <strong>active</strong>, không xóa dữ liệu user.
                      </div>
                    </c:otherwise>
                  </c:choose>

                  <a class="admin-btn user-detail-action-btn"
                     href="${pageContext.request.contextPath}/admin/users">
                    Đóng
                  </a>
                </div>
              </div>
            </section>

            <section class="user-detail-card user-detail-card--side">
              <div class="user-detail-card__header">
                <div>
                  <h2 class="user-detail-card__title">Tóm tắt quyền</h2>
                  <p class="user-detail-card__desc">
                    Những gì admin được và không được làm.
                  </p>
                </div>
              </div>

              <div class="user-detail-card__body">
                <div class="user-detail-rule-list">
                  <div class="user-detail-rule user-detail-rule--ok">
                    <strong>Được phép</strong>
                    <span>Sửa role, trạng thái hoạt động và rank thủ công của user thường.</span>
                  </div>

                  <div class="user-detail-rule user-detail-rule--warning">
                    <strong>Giới hạn</strong>
                    <span>Không tự khóa, tự đổi role chính tài khoản admin đang đăng nhập.</span>
                  </div>

                  <div class="user-detail-rule user-detail-rule--danger">
                    <strong>Không được phép</strong>
                    <span>Không sửa thông tin cá nhân hoặc xóa cứng tài khoản nếu user chưa yêu cầu.</span>
                  </div>
                </div>
              </div>
            </section>

            <section class="user-detail-card user-detail-card--side">
              <div class="user-detail-card__header">
                <div>
                  <h2 class="user-detail-card__title">Mốc thời gian</h2>
                  <p class="user-detail-card__desc">
                    Lịch sử cơ bản của tài khoản.
                  </p>
                </div>
              </div>

              <div class="user-detail-card__body">
                <div class="user-detail-timeline">
                  <div class="user-detail-timeline-item">
                    <span>Tạo tài khoản</span>
                    <strong>
                      <c:choose>
                        <c:when test="${not empty user.createdAt}">
                          <fmt:formatDate value="${user.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                        </c:when>
                        <c:otherwise>Không có dữ liệu</c:otherwise>
                      </c:choose>
                    </strong>
                  </div>

                  <div class="user-detail-timeline-item">
                    <span>Trạng thái hiện tại</span>
                    <strong>
                      <c:choose>
                        <c:when test="${user.active}">Đang hoạt động</c:when>
                        <c:otherwise>Đã khóa</c:otherwise>
                      </c:choose>
                    </strong>
                  </div>

                  <div class="user-detail-timeline-item">
                    <span>Rank hiện tại</span>
                    <strong>
                      <c:out value="${user.displayRankName}" />
                      (<c:out value="${user.displayRankCode}" />)
                    </strong>
                  </div>
                </div>
              </div>
            </section>

          </aside>

        </div>

      </c:otherwise>
    </c:choose>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

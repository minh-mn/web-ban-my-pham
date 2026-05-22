<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Sửa User" scope="request"/>
<c:set var="activeMenu" value="users" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<style>
  .user-edit-page {
    max-width: 1180px;
    margin: 0 auto;
  }

  .user-edit-layout {
    display: grid;
    grid-template-columns: minmax(0, 760px) 330px;
    gap: 18px;
    align-items: start;
  }

  .user-edit-left {
    display: grid;
    gap: 16px;
  }

  .user-edit-card {
    background: #fff;
    border: 1px solid #edf0f4;
    border-radius: 18px;
    padding: 18px;
    box-shadow: 0 8px 24px rgba(15, 23, 42, 0.03);
  }

  .user-edit-card__title {
    font-size: 16px;
    font-weight: 850;
    color: #111827;
    margin: 0;
  }

  .user-edit-card__desc {
    margin: 4px 0 16px;
    color: #6b7280;
    font-size: 13px;
    line-height: 1.45;
  }

  .user-form-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px 16px;
  }

  .user-form-grid .admin-field {
    margin: 0;
  }

  .user-field-full {
    grid-column: 1 / -1;
  }

  .user-form-grid .admin-input,
  .user-form-grid .admin-select {
    width: 100%;
    box-sizing: border-box;
  }

  .user-readonly {
    background: #f9fafb !important;
    color: #4b5563;
    cursor: not-allowed;
  }

  .user-rank-status {
    margin-top: 12px;
    border: 1px dashed #f3a6c8;
    background: #fff7fb;
    border-radius: 14px;
    padding: 12px;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    color: #6b7280;
    font-size: 13px;
  }

  .user-password-status {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    color: #6b7280;
    font-size: 13px;
  }

  .user-password-input-row {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 70px;
    gap: 8px;
    align-items: center;
  }

  .user-form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 18px;
    padding-top: 14px;
    border-top: 1px solid #f1f3f5;
  }

  .user-reset-note {
    margin-top: 14px;
    color: #991b1b;
    background: #fff1f2;
    border: 1px solid #fecdd3;
    border-radius: 14px;
    padding: 12px;
    font-size: 13px;
    line-height: 1.5;
  }

  .user-summary-card {
    background: #fff;
    border: 1px solid #edf0f4;
    border-radius: 18px;
    padding: 18px;
    box-shadow: 0 8px 24px rgba(15, 23, 42, 0.03);
    position: sticky;
    top: 18px;
  }

  .user-avatar {
    width: 54px;
    height: 54px;
    border-radius: 16px;
    background: #fce7f3;
    color: #be185d;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 900;
    font-size: 20px;
    margin-bottom: 12px;
  }

  .user-summary-name {
    font-size: 18px;
    font-weight: 900;
    color: #111827;
    margin-bottom: 4px;
  }

  .user-summary-email {
    color: #6b7280;
    font-size: 13px;
    word-break: break-word;
  }

  .user-summary-list {
    margin-top: 16px;
    display: grid;
    gap: 0;
  }

  .user-summary-item {
    display: grid;
    grid-template-columns: 110px minmax(0, 1fr);
    gap: 12px;
    padding: 11px 0;
    border-top: 1px solid #f1f3f5;
    align-items: center;
  }

  .user-summary-label {
    color: #6b7280;
    font-size: 13px;
  }

  .user-summary-value {
    color: #111827;
    font-size: 13px;
    font-weight: 850;
    text-align: right;
    word-break: break-word;
  }

  @media (max-width: 1180px) {
    .user-edit-layout {
      grid-template-columns: 1fr;
    }

    .user-summary-card {
      position: static;
    }
  }

  @media (max-width: 720px) {
    .user-form-grid {
      grid-template-columns: 1fr;
    }

    .user-form-actions {
      justify-content: flex-start;
      flex-wrap: wrap;
    }

    .user-password-input-row {
      grid-template-columns: 1fr;
    }

    .user-summary-item {
      grid-template-columns: 1fr;
      gap: 4px;
    }

    .user-summary-value {
      text-align: left;
    }
  }
</style>

<main class="admin-main">
  <div class="admin-container user-edit-page">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Sửa User</h1>
        <p class="admin-subtext">
          Cập nhật thông tin tài khoản, vai trò, trạng thái, rank khách hàng và reset mật khẩu.
        </p>
      </div>

      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/users">
        Quay lại
      </a>
    </div>

    <c:if test="${not empty error}">
      <div class="admin-alert admin-alert--danger" style="margin-bottom:14px;">
        <c:out value="${error}"/>
      </div>
    </c:if>

    <c:if test="${not empty success}">
      <div class="admin-alert admin-alert--success" style="margin-bottom:14px;">
        <c:out value="${success}"/>
      </div>
    </c:if>

    <div class="user-edit-layout">

      <div class="user-edit-left">

        <!-- FORM 1: UPDATE INFO -->
        <form method="post"
              action="${pageContext.request.contextPath}/admin/users"
              class="admin-form">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="updateInfo"/>
          <input type="hidden" name="id" value="${user.id}"/>

          <section class="user-edit-card">
            <h2 class="user-edit-card__title">Thông tin tài khoản</h2>
            <p class="user-edit-card__desc">
              Các thông tin cơ bản dùng để quản trị và liên hệ khách hàng.
            </p>

            <div class="user-form-grid">
              <div class="admin-field">
                <div class="admin-label">User ID</div>
                <input class="admin-input user-readonly" value="#${user.id}" disabled/>
              </div>

              <div class="admin-field">
                <div class="admin-label">Username</div>
                <input class="admin-input user-readonly" value="${user.username}" disabled/>
              </div>

              <div class="admin-field user-field-full">
                <div class="admin-label">Họ tên</div>
                <input class="admin-input"
                       name="fullName"
                       value="${user.fullName}"
                       maxlength="120"
                       placeholder="Nhập họ tên khách hàng"/>
              </div>

              <div class="admin-field">
                <div class="admin-label">Email</div>
                <input class="admin-input"
                       type="email"
                       name="email"
                       value="${user.email}"
                       maxlength="120"
                       placeholder="email@example.com"/>
              </div>

              <div class="admin-field">
                <div class="admin-label">Số điện thoại</div>
                <input class="admin-input"
                       name="phone"
                       value="${user.phone}"
                       maxlength="30"
                       placeholder="Nhập số điện thoại"/>
              </div>
            </div>
          </section>

          <section class="user-edit-card">
            <h2 class="user-edit-card__title">Phân quyền và trạng thái</h2>
            <p class="user-edit-card__desc">
              Quản lý quyền truy cập và trạng thái hoạt động của tài khoản.
            </p>

            <div class="user-form-grid">
              <div class="admin-field">
                <div class="admin-label">Role</div>
                <select class="admin-select" name="role" required>
                  <option value="USER" ${user.role == 'USER' ? 'selected' : ''}>
                    USER - Khách hàng
                  </option>
                  <option value="ADMIN" ${user.role == 'ADMIN' ? 'selected' : ''}>
                    ADMIN - Quản trị viên
                  </option>
                </select>
              </div>

              <div class="admin-field">
                <div class="admin-label">Trạng thái</div>
                <select class="admin-select" name="active" required>
                  <option value="1" ${user.active ? 'selected' : ''}>
                    Active - Cho phép đăng nhập
                  </option>
                  <option value="0" ${!user.active ? 'selected' : ''}>
                    Disabled - Vô hiệu hóa
                  </option>
                </select>
                <div class="admin-help">
                  Disabled: user không đăng nhập được.
                </div>
              </div>
            </div>
          </section>

          <section class="user-edit-card">
            <h2 class="user-edit-card__title">Rank khách hàng</h2>
            <p class="user-edit-card__desc">
              AUTO để hệ thống tự tính theo tổng chi tiêu. Chọn rank cụ thể để admin nâng hoặc hạ rank trực tiếp.
            </p>

            <div class="user-form-grid">
              <div class="admin-field user-field-full">
                <div class="admin-label">Chế độ xét rank</div>

                <select class="admin-select" name="manualRankCode">
                  <option value="AUTO" ${empty user.manualRankCode ? 'selected' : ''}>
                    AUTO - Tự động theo tổng chi tiêu
                  </option>

                  <c:forEach var="r" items="${ranks}">
                    <option value="${r.code}" ${user.manualRankCode == r.code ? 'selected' : ''}>
                        ${r.code} - ${r.name}
                    </option>
                  </c:forEach>
                </select>

                <div class="user-rank-status">
                  <span>Rank hiện tại:</span>
                  <span class="admin-pill admin-pill--ok">
                    <c:out value="${user.displayRankCode}"/>
                  </span>

                  <c:choose>
                    <c:when test="${empty user.manualRankCode}">
                      <span>Đang tự động theo tổng chi tiêu.</span>
                    </c:when>
                    <c:otherwise>
                      <span>Đang dùng rank do admin chỉ định.</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>
            </div>
          </section>

          <section class="user-edit-card">
            <h2 class="user-edit-card__title">Trạng thái mật khẩu</h2>
            <p class="user-edit-card__desc">
              Hệ thống chỉ lưu mật khẩu dạng BCrypt hash. Không thể xem lại mật khẩu gốc của user.
            </p>

            <div class="user-password-status">
              <span class="admin-pill">
                <c:out value="${user.passwordStatusLabel}"/>
              </span>

              <span>
                <c:out value="${user.passwordMasked}"/>
              </span>
            </div>

            <div class="user-form-actions">
              <a class="admin-btn" href="${pageContext.request.contextPath}/admin/users">
                Hủy
              </a>

              <button class="admin-btn admin-btn--primary" type="submit">
                Lưu thông tin
              </button>
            </div>
          </section>
        </form>

        <!-- FORM 2: CHANGE PASSWORD -->
        <form method="post"
              action="${pageContext.request.contextPath}/admin/users"
              class="admin-form">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="changePassword"/>
          <input type="hidden" name="id" value="${user.id}"/>

          <section class="user-edit-card">
            <h2 class="user-edit-card__title">Reset mật khẩu</h2>
            <p class="user-edit-card__desc">
              Admin có thể đặt lại mật khẩu mới. Mật khẩu mới sẽ được mã hóa trước khi lưu.
            </p>

            <div class="user-form-grid">
              <div class="admin-field">
                <div class="admin-label">Mật khẩu mới</div>

                <div class="user-password-input-row">
                  <input class="admin-input"
                         id="newPassword"
                         type="password"
                         name="newPassword"
                         minlength="6"
                         maxlength="72"
                         required/>

                  <button class="admin-btn"
                          type="button"
                          onclick="togglePassword('newPassword', this)">
                    Hiện
                  </button>
                </div>

                <div class="admin-help">
                  Khuyến nghị ít nhất 8 ký tự.
                </div>
              </div>

              <div class="admin-field">
                <div class="admin-label">Nhập lại mật khẩu</div>

                <div class="user-password-input-row">
                  <input class="admin-input"
                         id="confirmPassword"
                         type="password"
                         name="confirmPassword"
                         minlength="6"
                         maxlength="72"
                         required/>

                  <button class="admin-btn"
                          type="button"
                          onclick="togglePassword('confirmPassword', this)">
                    Hiện
                  </button>
                </div>
              </div>
            </div>

            <div class="user-reset-note">
              Lưu ý: Sau khi đổi mật khẩu, user cần dùng mật khẩu mới để đăng nhập.
              Không thể khôi phục mật khẩu cũ.
            </div>

            <div class="user-form-actions">
              <button class="admin-btn admin-btn--primary" type="submit">
                Đổi mật khẩu
              </button>
            </div>
          </section>
        </form>

      </div>

      <!-- SUMMARY -->
      <aside class="user-summary-card">
        <div class="user-avatar">
          U
        </div>

        <div class="user-summary-name">
          <c:choose>
            <c:when test="${not empty user.fullName}">
              <c:out value="${user.fullName}"/>
            </c:when>
            <c:otherwise>
              <c:out value="${user.username}"/>
            </c:otherwise>
          </c:choose>
        </div>

        <div class="user-summary-email">
          <c:choose>
            <c:when test="${not empty user.email}">
              <c:out value="${user.email}"/>
            </c:when>
            <c:otherwise>
              Chưa có email
            </c:otherwise>
          </c:choose>
        </div>

        <div class="user-summary-list">
          <div class="user-summary-item">
            <div class="user-summary-label">User ID</div>
            <div class="user-summary-value">#${user.id}</div>
          </div>

          <div class="user-summary-item">
            <div class="user-summary-label">Role</div>
            <div class="user-summary-value">
              <span class="admin-pill ${user.role == 'ADMIN' ? 'admin-pill--ok' : ''}">
                <c:out value="${user.role}"/>
              </span>
            </div>
          </div>

          <div class="user-summary-item">
            <div class="user-summary-label">Trạng thái</div>
            <div class="user-summary-value">
              <c:choose>
                <c:when test="${user.active}">
                  <span class="admin-pill admin-pill--ok">ACTIVE</span>
                </c:when>
                <c:otherwise>
                  <span class="admin-pill admin-pill--danger">DISABLED</span>
                </c:otherwise>
              </c:choose>
            </div>
          </div>

          <div class="user-summary-item">
            <div class="user-summary-label">Rank hiện tại</div>
            <div class="user-summary-value">
              <span class="admin-pill admin-pill--ok">
                <c:out value="${user.displayRankCode}"/>
              </span>
            </div>
          </div>

          <div class="user-summary-item">
            <div class="user-summary-label">Chế độ rank</div>
            <div class="user-summary-value">
              <c:choose>
                <c:when test="${empty user.manualRankCode}">
                  AUTO
                </c:when>
                <c:otherwise>
                  MANUAL
                </c:otherwise>
              </c:choose>
            </div>
          </div>

          <div class="user-summary-item">
            <div class="user-summary-label">Mật khẩu</div>
            <div class="user-summary-value">
              <c:out value="${user.passwordStatusLabel}"/>
            </div>
          </div>
        </div>
      </aside>

    </div>

  </div>
</main>

<script>
  function togglePassword(inputId, button) {
    const input = document.getElementById(inputId);

    if (!input) {
      return;
    }

    if (input.type === 'password') {
      input.type = 'text';
      button.textContent = 'Ẩn';
    } else {
      input.type = 'password';
      button.textContent = 'Hiện';
    }
  }
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>
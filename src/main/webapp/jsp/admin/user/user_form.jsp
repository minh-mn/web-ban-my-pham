<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Sửa User" scope="request"/>
<c:set var="activeMenu" value="users" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<c:set var="currentSessionUser" value="${sessionScope.user}"/>
<c:if test="${empty currentSessionUser}">
  <c:set var="currentSessionUser" value="${sessionScope.authUser}"/>
</c:if>
<c:if test="${empty currentSessionUser}">
  <c:set var="currentSessionUser" value="${sessionScope.currentUser}"/>
</c:if>

<c:set var="isCurrentUser" value="${not empty currentSessionUser and currentSessionUser.id == user.id}"/>
<c:set var="isProtectedAdmin" value="${user.admin}"/>
<c:set var="canEditPermission" value="${not isProtectedAdmin and not isCurrentUser}"/>
<c:set var="canChangePassword" value="${isCurrentUser}"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container user-edit-page user-edit-page--issue130">

    <div class="user-edit-hero">
      <div class="user-edit-hero__content">
        <span class="user-edit-eyebrow">Issue 130 · Quản lí user</span>
        <h1 class="admin-h1 user-edit-title">Sửa quyền tài khoản</h1>
        <p class="admin-subtext user-edit-subtitle">
          Admin chỉ được quản lí role, trạng thái hoạt động và rank thủ công. Thông tin cá nhân của user
          như họ tên, email, số điện thoại được hiển thị để đối chiếu và không được tự ý chỉnh sửa.
        </p>
      </div>

      <div class="user-edit-hero__actions">
        <a class="admin-btn" href="${pageContext.request.contextPath}/admin/users">
          Quay lại danh sách
        </a>

        <a class="admin-btn" href="${pageContext.request.contextPath}/admin/users?action=detail&id=${user.id}">
          Xem chi tiết
        </a>
      </div>
    </div>

    <c:if test="${not empty error}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${error}"/>
      </div>
    </c:if>

    <c:if test="${not empty errorMessage}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${errorMessage}"/>
      </div>
    </c:if>

    <c:if test="${not empty success}">
      <div class="admin-alert admin-alert--success">
        <c:out value="${success}"/>
      </div>
    </c:if>

    <c:if test="${not empty successMessage}">
      <div class="admin-alert admin-alert--success">
        <c:out value="${successMessage}"/>
      </div>
    </c:if>

    <div class="user-edit-layout user-edit-layout--issue130">

      <div class="user-edit-left">

        <section class="user-edit-card user-edit-account-card">
          <div class="user-edit-card__head">
            <div>
              <h2 class="user-edit-card__title">Thông tin tài khoản</h2>
              <p class="user-edit-card__desc">
                Khu vực này chỉ dùng để xem thông tin. Admin không được tự ý chỉnh sửa dữ liệu cá nhân nếu user chưa yêu cầu.
              </p>
            </div>

            <span class="admin-pill ${user.active ? 'admin-pill--ok' : 'admin-pill--danger'}">
              <c:choose>
                <c:when test="${user.active}">Đang hoạt động</c:when>
                <c:otherwise>Đã khóa</c:otherwise>
              </c:choose>
            </span>
          </div>

          <div class="user-policy-note">
            <div class="user-policy-note__icon">!</div>
            <div>
              <strong>Giới hạn quyền admin</strong>
              <p>
                Họ tên, email và số điện thoại không gửi trong form cập nhật quyền. Nếu user cần sửa,
                nên để user tự sửa trong trang tài khoản hoặc xử lý bằng luồng yêu cầu riêng.
              </p>
            </div>
          </div>

          <div class="user-form-grid user-form-grid--readonly">
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
              <input class="admin-input user-readonly"
                     value="${empty user.fullName ? 'Chưa cập nhật' : user.fullName}"
                     disabled/>
            </div>

            <div class="admin-field">
              <div class="admin-label">Email</div>
              <input class="admin-input user-readonly"
                     value="${empty user.email ? 'Chưa cập nhật' : user.email}"
                     disabled/>
            </div>

            <div class="admin-field">
              <div class="admin-label">Số điện thoại</div>
              <input class="admin-input user-readonly"
                     value="${empty user.phone ? 'Chưa cập nhật' : user.phone}"
                     disabled/>
            </div>
          </div>
        </section>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/users"
              class="admin-form user-permission-form">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="updateInfo"/>
          <input type="hidden" name="id" value="${user.id}"/>

          <c:if test="${not canEditPermission}">
            <input type="hidden" name="role" value="${user.role}"/>
            <input type="hidden" name="active" value="${user.active ? '1' : '0'}"/>
            <input type="hidden" name="manualRankCode" value="${empty user.manualRankCode ? 'AUTO' : user.manualRankCode}"/>
          </c:if>

          <section class="user-edit-card">
            <div class="user-edit-card__head">
              <div>
                <h2 class="user-edit-card__title">Phân quyền và trạng thái</h2>
                <p class="user-edit-card__desc">
                  Quản lí role, trạng thái hoạt động và rank thủ công. Tài khoản ADMIN và tài khoản hiện tại được bảo vệ.
                </p>
              </div>

              <c:choose>
                <c:when test="${isCurrentUser}">
                  <span class="admin-pill admin-pill--warning">Tài khoản hiện tại</span>
                </c:when>
                <c:when test="${isProtectedAdmin}">
                  <span class="admin-pill admin-pill--danger">ADMIN được bảo vệ</span>
                </c:when>
                <c:otherwise>
                  <span class="admin-pill admin-pill--ok">Có thể chỉnh quyền</span>
                </c:otherwise>
              </c:choose>
            </div>

            <c:if test="${not canEditPermission}">
              <div class="user-policy-note user-policy-note--warning">
                <div class="user-policy-note__icon">i</div>
                <div>
                  <strong>Không thể chỉnh quyền tài khoản này</strong>
                  <p>
                    Admin không được tự thay đổi role hoặc khóa chính mình. Tài khoản ADMIN khác cũng không được chỉnh quyền,
                    khóa hoặc xóa từ màn hình này.
                  </p>
                </div>
              </div>
            </c:if>

            <div class="user-form-grid">
              <div class="admin-field">
                <div class="admin-label">Role</div>
                <select class="admin-select" name="role" ${canEditPermission ? '' : 'disabled'} required>
                  <c:forEach var="roleItem" items="${roles}">
                    <option value="${roleItem.code}" ${user.role == roleItem.code ? 'selected' : ''}>
                      ${roleItem.code} - ${roleItem.name}
                    </option>
                  </c:forEach>
                </select>
                <div class="admin-help">
                  Role lấy từ màn hình Phân quyền role. Không cho tự đổi role chính mình hoặc chỉnh ADMIN khác.
                </div>
              </div>

              <div class="admin-field">
                <div class="admin-label">Trạng thái đăng nhập</div>
                <select class="admin-select" name="active" ${canEditPermission ? '' : 'disabled'} required>
                  <option value="1" ${user.active ? 'selected' : ''}>
                    Active - Cho phép đăng nhập
                  </option>
                  <option value="0" ${!user.active ? 'selected' : ''}>
                    Disabled - Khóa đăng nhập
                  </option>
                </select>
                <div class="admin-help">
                  Disabled nghĩa là khóa mềm, không xóa dữ liệu user.
                </div>
              </div>
            </div>
          </section>

          <section class="user-edit-card">
            <div class="user-edit-card__head">
              <div>
                <h2 class="user-edit-card__title">Rank khách hàng</h2>
                <p class="user-edit-card__desc">
                  AUTO để hệ thống tự tính theo tổng chi tiêu. Chọn rank cụ thể nếu cần chỉ định thủ công cho user thường.
                </p>
              </div>

              <span class="admin-pill ${empty user.manualRankCode ? 'admin-pill--warning' : 'admin-pill--info'}">
                <c:choose>
                  <c:when test="${empty user.manualRankCode}">AUTO</c:when>
                  <c:otherwise>MANUAL</c:otherwise>
                </c:choose>
              </span>
            </div>

            <div class="user-form-grid">
              <div class="admin-field user-field-full">
                <div class="admin-label">Chế độ xét rank</div>

                <select class="admin-select" name="manualRankCode" ${canEditPermission ? '' : 'disabled'}>
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

                  <span class="admin-pill ${empty user.manualRankCode ? 'admin-pill--warning' : 'admin-pill--info'}">
                    <c:choose>
                      <c:when test="${empty user.manualRankCode}">Tự động theo chi tiêu</c:when>
                      <c:otherwise>Admin chỉ định</c:otherwise>
                    </c:choose>
                  </span>
                </div>
              </div>
            </div>

            <div class="user-form-actions">
              <a class="admin-btn" href="${pageContext.request.contextPath}/admin/users">
                Hủy
              </a>

              <button class="admin-btn admin-btn--primary" type="submit" ${canEditPermission ? '' : 'disabled'}>
                Lưu phân quyền
              </button>
            </div>
          </section>
        </form>

        <section class="user-edit-card">
          <div class="user-edit-card__head">
            <div>
              <h2 class="user-edit-card__title">Mật khẩu và đăng nhập</h2>
              <p class="user-edit-card__desc">
                Hệ thống chỉ lưu mật khẩu dạng BCrypt hash. Không thể xem lại mật khẩu gốc của user.
              </p>
            </div>

            <span class="admin-pill ${user.hasPassword ? 'admin-pill--ok' : 'admin-pill--warning'}">
              <c:out value="${user.passwordStatusLabel}"/>
            </span>
          </div>

          <div class="user-login-status-grid">
            <div class="user-login-status-item">
              <span>Mật khẩu</span>
              <strong><c:out value="${user.passwordMasked}"/></strong>
            </div>

            <div class="user-login-status-item">
              <span>Đăng nhập xã hội</span>
              <strong>
                <c:choose>
                  <c:when test="${user.hasSocialLogin}">Có liên kết</c:when>
                  <c:otherwise>Không có</c:otherwise>
                </c:choose>
              </strong>
            </div>
          </div>

          <c:choose>
            <c:when test="${canChangePassword}">
              <form method="post"
                    action="${pageContext.request.contextPath}/admin/users"
                    class="admin-form user-password-form">

                <%@ include file="/jsp/common/csrf.jspf" %>

                <input type="hidden" name="action" value="changePassword"/>
                <input type="hidden" name="id" value="${user.id}"/>

                <div class="user-policy-note user-policy-note--success">
                  <div class="user-policy-note__icon">✓</div>
                  <div>
                    <strong>Đổi mật khẩu tài khoản hiện tại</strong>
                    <p>Chỉ được đổi mật khẩu cho chính tài khoản đang đăng nhập.</p>
                  </div>
                </div>

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
                             autocomplete="new-password"
                             required/>

                      <button class="admin-btn"
                              type="button"
                              onclick="togglePassword('newPassword', this)">
                        Hiện
                      </button>
                    </div>

                    <div class="admin-help">
                      Khuyến nghị ít nhất 8 ký tự, tối đa 72 ký tự để phù hợp BCrypt.
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
                             autocomplete="new-password"
                             required/>

                      <button class="admin-btn"
                              type="button"
                              onclick="togglePassword('confirmPassword', this)">
                        Hiện
                      </button>
                    </div>
                  </div>
                </div>

                <div class="user-form-actions">
                  <button class="admin-btn admin-btn--primary" type="submit">
                    Đổi mật khẩu
                  </button>
                </div>
              </form>
            </c:when>

            <c:otherwise>
              <div class="user-policy-note user-policy-note--warning">
                <div class="user-policy-note__icon">i</div>
                <div>
                  <strong>Không được đổi mật khẩu tài khoản khác</strong>
                  <p>
                    Nếu user quên mật khẩu, nên dùng luồng yêu cầu đặt lại mật khẩu riêng thay vì admin tự ý đổi trực tiếp.
                  </p>
                </div>
              </div>
            </c:otherwise>
          </c:choose>
        </section>

      </div>

      <aside class="user-summary-card user-summary-card--issue130">
        <div class="user-avatar user-avatar--large">
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

        <div class="user-summary-badges">
          <span class="admin-pill ${user.role == 'ADMIN' ? 'admin-pill--danger' : 'admin-pill--info'}">
            <c:out value="${user.role}"/>
          </span>

          <span class="admin-pill ${user.active ? 'admin-pill--ok' : 'admin-pill--danger'}">
            <c:choose>
              <c:when test="${user.active}">ACTIVE</c:when>
              <c:otherwise>DISABLED</c:otherwise>
            </c:choose>
          </span>
        </div>

        <div class="user-summary-list">
          <div class="user-summary-item">
            <div class="user-summary-label">User ID</div>
            <div class="user-summary-value">#${user.id}</div>
          </div>

          <div class="user-summary-item">
            <div class="user-summary-label">Username</div>
            <div class="user-summary-value">
              <c:out value="${user.username}"/>
            </div>
          </div>

          <div class="user-summary-item">
            <div class="user-summary-label">Role</div>
            <div class="user-summary-value">
              <c:out value="${user.role}"/>
            </div>
          </div>

          <div class="user-summary-item">
            <div class="user-summary-label">Trạng thái</div>
            <div class="user-summary-value">
              <c:choose>
                <c:when test="${user.active}">Đang hoạt động</c:when>
                <c:otherwise>Đã khóa</c:otherwise>
              </c:choose>
            </div>
          </div>

          <div class="user-summary-item">
            <div class="user-summary-label">Rank hiện tại</div>
            <div class="user-summary-value">
              <c:out value="${user.displayRankCode}"/>
            </div>
          </div>

          <div class="user-summary-item">
            <div class="user-summary-label">Chế độ rank</div>
            <div class="user-summary-value">
              <c:choose>
                <c:when test="${empty user.manualRankCode}">AUTO</c:when>
                <c:otherwise>MANUAL</c:otherwise>
              </c:choose>
            </div>
          </div>

          <div class="user-summary-item">
            <div class="user-summary-label">Ngày tạo</div>
            <div class="user-summary-value">
              <c:choose>
                <c:when test="${not empty user.createdAt}">
                  <fmt:formatDate value="${user.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                </c:when>
                <c:otherwise>Không rõ</c:otherwise>
              </c:choose>
            </div>
          </div>

          <div class="user-summary-item">
            <div class="user-summary-label">Bảo vệ</div>
            <div class="user-summary-value">
              <c:choose>
                <c:when test="${isCurrentUser}">Tài khoản hiện tại</c:when>
                <c:when test="${isProtectedAdmin}">ADMIN</c:when>
                <c:otherwise>Không</c:otherwise>
              </c:choose>
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

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Sửa User" scope="request"/>
<c:set var="activeMenu" value="users" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Sửa User</h1>
        <p class="admin-subtext">
          Cập nhật thông tin, role, trạng thái, rank khách hàng và reset mật khẩu.
        </p>
      </div>

      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/users">
        Quay lại
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:if test="${not empty error}">
          <div class="admin-alert admin-alert--danger">
            <c:out value="${error}"/>
          </div>
        </c:if>

        <c:if test="${not empty success}">
          <div class="admin-alert admin-alert--success">
            <c:out value="${success}"/>
          </div>
        </c:if>

        <!-- ================= FORM 1: UPDATE INFO ================= -->
        <form method="post"
              action="${pageContext.request.contextPath}/admin/users"
              class="admin-form admin-form--narrow">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="updateInfo"/>
          <input type="hidden" name="id" value="${user.id}"/>

          <div class="admin-grid-2" style="max-width:760px;">

            <div class="admin-field">
              <div class="admin-label">User ID</div>
              <input class="admin-input" value="#${user.id}" disabled/>
            </div>

            <div class="admin-field">
              <div class="admin-label">Username</div>
              <input class="admin-input" value="${user.username}" disabled/>
            </div>

            <div class="admin-field" style="grid-column: 1 / -1;">
              <div class="admin-label">Họ tên</div>
              <input class="admin-input"
                     name="fullName"
                     value="${user.fullName}"
                     maxlength="120"/>
            </div>

            <div class="admin-field">
              <div class="admin-label">Email</div>
              <input class="admin-input"
                     type="email"
                     name="email"
                     value="${user.email}"
                     maxlength="120"/>
            </div>

            <div class="admin-field">
              <div class="admin-label">Số điện thoại</div>
              <input class="admin-input"
                     name="phone"
                     value="${user.phone}"
                     maxlength="30"/>
            </div>

            <div class="admin-field">
              <div class="admin-label">Role</div>
              <select class="admin-select" name="role" required>
                <option value="USER" ${user.role == 'USER' ? 'selected' : ''}>
                  USER
                </option>
                <option value="ADMIN" ${user.role == 'ADMIN' ? 'selected' : ''}>
                  ADMIN
                </option>
              </select>
            </div>

            <div class="admin-field">
              <div class="admin-label">Trạng thái</div>
              <select class="admin-select" name="active" required>
                <option value="1" ${user.active ? 'selected' : ''}>
                  Active
                </option>
                <option value="0" ${!user.active ? 'selected' : ''}>
                  Disabled
                </option>
              </select>
              <div class="admin-help">
                Disabled: user không đăng nhập được.
              </div>
            </div>

            <!-- ================= MANUAL RANK ================= -->
            <div class="admin-field" style="grid-column: 1 / -1;">
              <div class="admin-label">Rank khách hàng</div>

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

              <div class="admin-help">
                Chọn AUTO để hệ thống tự tính rank theo tổng đơn đã thanh toán.
                Chọn rank cụ thể để admin nâng hoặc hạ rank trực tiếp cho user.
              </div>

              <div style="margin-top:8px;">
                <c:choose>
                  <c:when test="${empty user.manualRankCode}">
                    <span class="admin-pill">AUTO</span>
                    <span class="admin-muted">Rank hiện tại đang tự động theo tổng chi tiêu.</span>
                  </c:when>

                  <c:otherwise>
                    <span class="admin-pill admin-pill--ok">
                      <c:out value="${user.manualRankCode}"/>
                    </span>
                    <span class="admin-muted">Rank hiện tại do admin chỉ định.</span>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>

            <!-- ================= PASSWORD STATUS ================= -->
            <div class="admin-field" style="grid-column: 1 / -1;">
              <div class="admin-label">Trạng thái mật khẩu</div>

              <div>
                <span class="admin-pill">
                  <c:out value="${user.passwordStatusLabel}"/>
                </span>

                <span class="admin-muted" style="margin-left:8px;">
                  <c:out value="${user.passwordMasked}"/>
                </span>
              </div>

              <div class="admin-help">
                Hệ thống chỉ lưu mật khẩu dạng BCrypt hash. Không thể xem mật khẩu gốc của user.
                Admin chỉ có thể đặt lại mật khẩu mới ở form bên dưới.
              </div>
            </div>

          </div>

          <hr class="admin-divider"/>

          <div class="admin-actions">
            <button class="admin-btn admin-btn--primary" type="submit">
              Lưu thông tin
            </button>

            <a class="admin-btn" href="${pageContext.request.contextPath}/admin/users">
              Hủy
            </a>
          </div>
        </form>

        <hr class="admin-divider" style="margin:18px 0;"/>

        <!-- ================= FORM 2: CHANGE PASSWORD ================= -->
        <form method="post"
              action="${pageContext.request.contextPath}/admin/users"
              class="admin-form admin-form--narrow">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="changePassword"/>
          <input type="hidden" name="id" value="${user.id}"/>

          <div class="admin-grid-2" style="max-width:760px;">

            <div class="admin-field">
              <div class="admin-label">Mật khẩu mới</div>

              <div style="display:flex; gap:8px; align-items:center;">
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
                Khuyến nghị >= 8 ký tự. Mật khẩu sẽ được mã hóa BCrypt trước khi lưu.
              </div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Nhập lại mật khẩu</div>

              <div style="display:flex; gap:8px; align-items:center;">
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

          <hr class="admin-divider"/>

          <div class="admin-actions">
            <button class="admin-btn admin-btn--primary" type="submit">
              Đổi mật khẩu
            </button>
          </div>
        </form>

      </div>
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
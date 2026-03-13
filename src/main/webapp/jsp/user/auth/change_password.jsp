<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<section class="auth-page">
  <div class="auth-card auth-card-sm">

    <h2 class="auth-title">Đổi mật khẩu</h2>
    <p class="auth-subtitle">
      Cập nhật mật khẩu mới để bảo mật tài khoản
    </p>

    <!-- ===== ERROR MESSAGE ===== -->
    <c:if test="${not empty error}">
      <div class="auth-error-box">
        <div class="auth-error"><c:out value="${error}"/></div>
      </div>
    </c:if>

    <!-- ===== SUCCESS MESSAGE ===== -->
    <c:if test="${not empty success}">
      <div class="auth-success-box">
        <c:out value="${success}"/>
      </div>
    </c:if>

    <!-- ===== FORM ===== -->
    <form method="post"
          action="${pageContext.request.contextPath}/account/change-password"
          class="auth-form">

      <!-- ✅ CSRF TOKEN -->
      <input type="hidden"
             name="csrf_token"
             value="<c:out value='${sessionScope.CSRF_TOKEN}'/>">

      <!-- MẬT KHẨU CŨ -->
      <div class="form-group">
        <label>Mật khẩu hiện tại</label>
        <div class="input-group">
          <span class="input-icon">🔒</span>
          <input type="password"
                 name="oldPassword"
                 placeholder="Nhập mật khẩu hiện tại"
                 required>
        </div>
      </div>

      <!-- MẬT KHẨU MỚI -->
      <div class="form-group">
        <label>Mật khẩu mới</label>
        <div class="input-group">
          <span class="input-icon">🔐</span>
          <input type="password"
                 name="newPassword"
                 placeholder="Nhập mật khẩu mới"
                 minlength="6"
                 required>
        </div>
      </div>

      <!-- XÁC NHẬN -->
      <div class="form-group">
        <label>Xác nhận mật khẩu</label>
        <div class="input-group">
          <span class="input-icon">🔐</span>
          <input type="password"
                 name="confirmPassword"
                 placeholder="Nhập lại mật khẩu"
                 minlength="6"
                 required>
        </div>
      </div>

      <button type="submit" class="btn-auth">
        Lưu thay đổi
      </button>

      <div class="auth-footer">
        <a href="${pageContext.request.contextPath}/account">
          Quay lại tài khoản
        </a>
      </div>

    </form>

  </div>
</section>

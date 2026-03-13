<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="container auth-wrap">
  <div class="auth-card">

    <h2 class="auth-title">Đặt lại mật khẩu</h2>

    <c:if test="${not empty error}">
      <div class="auth-alert auth-alert--error">
        <c:out value="${error}"/>
      </div>
    </c:if>

    <c:choose>
      <c:when test="${empty token}">
        <p class="auth-desc">
          Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn. Vui lòng yêu cầu link mới.
        </p>

        <a class="auth-cta"
           href="${pageContext.request.contextPath}/forgot-password">
          Yêu cầu link mới
        </a>

        <div class="auth-footer">
          <a href="${pageContext.request.contextPath}/login">
            Quay lại đăng nhập
          </a>
        </div>
      </c:when>

      <c:otherwise>
        <p class="auth-desc">
          Vui lòng nhập mật khẩu mới. Mật khẩu tối thiểu 6 ký tự.
        </p>

        <form class="auth-form"
              method="post"
              action="${pageContext.request.contextPath}/reset-password">

          <!-- ✅ CSRF TOKEN -->
          <input type="hidden"
                 name="csrf_token"
                 value="<c:out value='${sessionScope.CSRF_TOKEN}'/>">

          <!-- TOKEN RESET -->
          <input type="hidden"
                 name="token"
                 value="<c:out value='${token}'/>"/>

          <label for="newPassword">Mật khẩu mới</label>
          <input id="newPassword"
                 class="auth-input"
                 name="newPassword"
                 type="password"
                 required
                 minlength="6"
                 autocomplete="new-password"
                 placeholder="Nhập mật khẩu mới" />

          <label for="confirmPassword">Xác nhận mật khẩu</label>
          <input id="confirmPassword"
                 class="auth-input"
                 name="confirmPassword"
                 type="password"
                 required
                 minlength="6"
                 autocomplete="new-password"
                 placeholder="Nhập lại mật khẩu" />

          <div class="auth-actions">
            <button class="auth-submit" type="submit">
              Cập nhật mật khẩu
            </button>
          </div>
        </form>

        <div class="auth-footer">
          <a href="${pageContext.request.contextPath}/login">
            Quay lại đăng nhập
          </a>
        </div>
      </c:otherwise>
    </c:choose>

  </div>
</div>

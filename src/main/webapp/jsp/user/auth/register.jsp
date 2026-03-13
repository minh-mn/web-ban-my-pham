<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<section class="auth-page">
  <div class="auth-card auth-card-sm">

    <h2 class="auth-title">Tạo tài khoản</h2>
    <p class="auth-subtitle">
      Tham gia MyCosmetic để mua sắm và theo dõi đơn hàng
    </p>

    <!-- ===== ERROR ===== -->
    <c:if test="${not empty errors}">
      <div class="auth-error-box">
        <c:forEach var="err" items="${errors}">
          <div class="auth-error"><c:out value="${err}"/></div>
        </c:forEach>
      </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/register"
          class="auth-form">

      <!-- ✅ CSRF TOKEN (BẮT BUỘC) -->
      <input type="hidden"
             name="csrf_token"
             value="<c:out value='${sessionScope.CSRF_TOKEN}'/>">

      <!-- FULL NAME -->
      <div class="form-group">
        <label>Họ tên</label>
        <div class="input-group">
          <span class="input-icon">🪪</span>
          <input type="text"
                 name="fullName"
                 placeholder="Nhập họ tên"
                 autocomplete="name"
                 required
                 value="<c:out value='${param.fullName}'/>">
        </div>
      </div>

      <!-- PHONE -->
      <div class="form-group">
        <label>Số điện thoại</label>
        <div class="input-group">
          <span class="input-icon">📱</span>
          <input type="tel"
                 name="phone"
                 placeholder="vd: 0987654321"
                 autocomplete="tel"
                 required
                 value="<c:out value='${param.phone}'/>">
        </div>
      </div>

      <!-- USERNAME -->
      <div class="form-group">
        <label>Tên đăng nhập</label>
        <div class="input-group">
          <span class="input-icon">👤</span>
          <input type="text"
                 name="username"
                 placeholder="Nhập tên đăng nhập"
                 autocomplete="username"
                 required
                 value="<c:out value='${param.username}'/>">
        </div>
      </div>

      <!-- EMAIL -->
      <div class="form-group">
        <label>Email</label>
        <div class="input-group">
          <span class="input-icon">📧</span>
          <input type="email"
                 name="email"
                 placeholder="vd: example@gmail.com"
                 autocomplete="email"
                 required
                 value="<c:out value='${param.email}'/>">
        </div>
      </div>

      <!-- PASSWORD -->
      <div class="form-group">
        <label>Mật khẩu</label>
        <div class="input-group">
          <span class="input-icon">🔒</span>
          <input type="password"
                 name="password"
                 placeholder="Tối thiểu 6 ký tự"
                 autocomplete="new-password"
                 required>
        </div>
      </div>

      <!-- PASSWORD CONFIRM -->
      <div class="form-group">
        <label>Nhập lại mật khẩu</label>
        <div class="input-group">
          <span class="input-icon">🔒</span>
          <input type="password"
                 name="confirmPassword"
                 placeholder="Nhập lại mật khẩu"
                 autocomplete="new-password"
                 required>
        </div>
      </div>

      <button type="submit" class="btn-auth">
        Đăng ký
      </button>
    </form>

    <div class="auth-footer">
      Đã có tài khoản?
      <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
    </div>

  </div>
</section>

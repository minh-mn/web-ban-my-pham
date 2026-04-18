<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/login.css">

<section class="auth-page">
	<div class="auth-card auth-card-sm">

		<h2 class="auth-title">Chào mừng trở lại</h2>
		<p class="auth-subtitle">Đăng nhập để tiếp tục mua sắm và theo dõi
			đơn hàng</p>

		<!-- ===== ERROR ===== -->
		<c:if test="${not empty error}">
			<div class="auth-error-box">
				<div class="auth-error">${error}</div>
			</div>
		</c:if>

		<form method="post" action="${pageContext.request.contextPath}/login"
			class="auth-form">

			<!-- ✅ CSRF TOKEN (BẮT BUỘC) -->
			<input type="hidden" name="csrf_token"
				value="<c:out value='${sessionScope.CSRF_TOKEN}'/>">

			<!-- USERNAME -->
			<div class="form-group">
				<labela>Tên đăng nhập</label>
				<div class="input-group">
					<span class="input-icon">👤</span> <input type="text"
						name="username" placeholder="Nhập tên đăng nhập" required>
				</div>
			</div>

			<!-- PASSWORD -->
			<div class="form-group">
				<label>Mật khẩu</label>
				<div class="input-group">
					<span class="input-icon">🔒</span> <input type="password"
						name="password" placeholder="Nhập mật khẩu" required>
				</div>

				<div class="auth-footer">
					<a href="${pageContext.request.contextPath}/forgot-password">
						Quên mật khẩu? </a>
				</div>
			</div>

			<button type="submit" class="btn-auth">Đăng nhập</button>

			<div class="auth-divider">
				<spna>Hoặc đăng nhập với</span>
			</div>

			<div class="social-login">
				<a
					href="https://accounts.google.com/o/oauth2/auth?scope=email%20profile&redirect_uri=http://localhost:8080/MyCosmeticShop/login-social?type=google&response_type=code&client_id=&approval_prompt=force"
					class="social-btn google-btn"
					style="text-decoration: none; display: flex; align-items: center; justify-content: center;">
					<span class="social-icon google-icon"></span> Google
				</a> <a
					href="https://www.facebook.com/v12.0/dialog/oauth?client_id=YOUR_FB_ID&redirect_uri=http://localhost:8080/MyCosmeticShop/login-social?type=facebook&scope=email"
					class="social-btn facebook-btn"
					style="text-decoration: none; display: flex; align-items: center; justify-content: center;">
					<span class="social-icon facebook-icon"></span> Facebook
				</a>
			</div>
		</form>

		<div class="auth-footer">
			Chưa có tài khoản? <a
				href="${pageContext.request.contextPath}/register"> Đăng ký </a>
		</div>

	</div>
</section>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:choose>
	<c:when test="${empty requestScope.__IN_BASE__ && param.render ne 'content'}">
		<c:set var="pageTitle" value="Quên mật khẩu" scope="request" />
		<c:set var="pageContent" value="/jsp/auth/forgot_password.jsp?render=content" scope="request" />
		<jsp:include page="/jsp/common/base.jsp" />
	</c:when>
	<c:otherwise>
		<div class="auth-card">
			<h2 class="auth-title">Khôi phục mật khẩu</h2>
			<p class="auth-desc">Vui lòng nhập email đăng ký. Chúng tôi sẽ gửi hướng dẫn đặt lại mật khẩu cho bạn.</p>

				<%-- Hiển thị thông báo --%>
			<c:if test="${not empty error}">
				<div class="auth-alert auth-alert--error">${error}</div>
			</c:if>
			<c:if test="${not empty message}">
				<div class="auth-alert auth-alert--success">${message}</div>
			</c:if>

			<form class="auth-form" method="post" action="${pageContext.request.contextPath}/forgot-password">
				<input type="hidden" name="csrf_token" value="<c:out value='${sessionScope.CSRF_TOKEN}'/>">

				<div class="form-group">
					<label for="email" class="auth-label">Địa chỉ Email</label>
					<input id="email" class="auth-input" name="email" type="email" required placeholder="example@email.com">
				</div>

				<button type="submit" class="auth-submit-btn">Gửi yêu cầu</button>
			</form>

			<div class="auth-footer">
				<a href="${pageContext.request.contextPath}/login" class="auth-link">Quay lại đăng nhập</a>
			</div>
		</div>
	</c:otherwise>
</c:choose>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:choose>
	<c:when
		test="${empty requestScope.__IN_BASE__ && param.render ne 'content'}">
		<c:set var="pageTitle" value="Quên mật khẩu" scope="request" />

		<!-- ❌ BỎ hideFooter để footer hiện 1 lần từ base.jsp -->
		<%-- <c:set var="hideFooter" value="true" scope="request"/> --%>

		<c:set var="pageContent"
			value="/jsp/auth/forgot_password.jsp?render=content" scope="request" />

		<jsp:include page="/jsp/common/base.jsp" />
	</c:when>

	<c:otherwise>
		<div class="container auth-wrap">
			<div class="auth-card">

				<h2 class="auth-title">Quên mật khẩu</h2>
				<p class="auth-desc">Nhập email đã đăng ký. Hệ thống sẽ gửi link
					để bạn đặt lại mật khẩu.</p>

				<c:if test="${not empty error}">
					<div class="auth-alert auth-alert--error">
						<c:out value="${error}" />
					</div>
				</c:if>

				<c:if test="${not empty message}">
					<div class="auth-alert auth-alert--success">
						<c:out value="${message}" />
					</div>
				</c:if>

				<form class="auth-form" method="post"
					action="${pageContext.request.contextPath}/forgot-password">

					<!-- ✅ CSRF TOKEN -->
					<input type="hidden" name="csrf_token"
						value="<c:out value='${sessionScope.CSRF_TOKEN}'/>"> <label
						for="email">Email</label> <input id="email" class="auth-input"
						name="email" type="email" required autocomplete="email"
						value="<c:out value='${param.email}'/>"
						placeholder="vd: example@gmail.com" />

					<div class="auth-actions">
						<button class="auth-submit" type="submit">Gửi link đặt
							lại mật khẩu</button>
					</div>
				</form>

				<div class="auth-footer">
					<a href="${pageContext.request.contextPath}/login"> Quay lại
						đăng nhập </a>
				</div>

			</div>
		</div>
	</c:otherwise>
</c:choose>

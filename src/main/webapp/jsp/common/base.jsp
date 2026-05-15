<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">

<title><c:choose>
		<c:when test="${not empty pageTitle}">
      ${pageTitle}
    </c:when>
		<c:otherwise>
      MyCosmetic Shop
    </c:otherwise>
	</c:choose></title>

<!-- GLOBAL CSS -->
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/base.css">

<!-- PAGE CSS -->
<c:if test="${not empty pageCss}">
	<link rel="stylesheet"
		href="${pageContext.request.contextPath}/assets/css/${pageCss}">
</c:if>

<script>
	window.APP_CTX = "${pageContext.request.contextPath}";
</script>

<!-- GLOBAL JS -->
<script defer src="${pageContext.request.contextPath}/assets/js/main.js"></script>
</head>

<body class="site-body">

	<!-- ================= HEADER ================= -->
	<header class="site-header">
		<div class="container header-inner">

			<a class="logo" href="${pageContext.request.contextPath}/">MyCosmetic</a>

			<div class="header-actions" style="display: flex; align-items: center; gap: 20px;">

				<div class="search-wrapper">
					<form action="${pageContext.request.contextPath}/search" method="get" class="search-form">
						<span class="search-icon">🔍</span>
						<input id="search-input" name="q" placeholder="Tìm sản phẩm..." autocomplete="off" value="${param.q}">
					</form>
					<div id="search-results" class="search-results"></div>
				</div>

				<div class="auth-links">
					<c:choose>
						<c:when test="${not empty sessionScope.user}">
							<div class="user-dropdown">
								<button class="user-btn" type="button" aria-haspopup="menu" aria-expanded="false">
									<span class="user-icon">👤</span> <span class="user-name">${sessionScope.user.username}</span>
								</button>
								<div class="user-menu">
									<a href="${pageContext.request.contextPath}/account">👤 Tài khoản</a>
									<a href="${pageContext.request.contextPath}/orders">📦 Đơn hàng</a>
									<div class="menu-divider"></div>
									<form method="post" action="${pageContext.request.contextPath}/logout">
										<input type="hidden" name="csrf_token" value="<c:out value='${sessionScope.CSRF_TOKEN}'/>">
										<button type="submit" class="logout-btn">🚪 Đăng xuất</button>
									</form>
								</div>
							</div>
						</c:when>
						<c:otherwise>
							<div class="auth-buttons">
								<a href="${pageContext.request.contextPath}/login" class="btn-login">Đăng nhập</a>
								<a href="${pageContext.request.contextPath}/register" class="btn-register">Đăng ký</a>
							</div>
						</c:otherwise>
					</c:choose>
				</div>

				<a href="${pageContext.request.contextPath}/cart" class="cart-icon" style="font-size: 24px; text-decoration: none; position: relative; margin-left: 10px;">
					🛒
					<c:if test="${not empty sessionScope.CART}">
						<c:set var="cartQty" value="0" />
						<c:forEach var="e" items="${sessionScope.CART}">
							<c:set var="cartQty" value="${cartQty + e.value.quantity}" />
						</c:forEach>
						<c:if test="${cartQty > 0}">
							<span class="cart-badge" style="position: absolute; top: -8px; right: -12px; background: var(--pink-main); color: white; border-radius: 50%; padding: 2px 6px; font-size: 11px; font-weight: bold;">${cartQty}</span>
						</c:if>
					</c:if>
				</a>

			</div>
		</div>

		<jsp:include page="/jsp/common/header.jsp" />
	</header>

	<!-- ================= MAIN ================= -->
	<main class="site-main">
		<c:choose>
			<c:when test="${not empty pageContent}">
				<!-- ✅ Flag: đánh dấu đang render trong base.jsp -->
				<c:set var="__IN_BASE__" value="true" scope="request" />

				<c:import url="${pageContent}" />
			</c:when>

			<c:otherwise>
				<div class="container"
					style="padding: 40px 0; color: #666; text-align: center;">
					Chưa có nội dung để hiển thị.</div>
			</c:otherwise>
		</c:choose>
	</main>
	
	<jsp:include page="/jsp/common/footer.jsp" />

	<script defer src="${pageContext.request.contextPath}/assets/js/main.js"></script>

</body>
</html>

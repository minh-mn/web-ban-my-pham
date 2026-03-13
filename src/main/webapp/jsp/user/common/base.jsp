<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

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

			<!-- DESKTOP NAV -->
			<nav class="main-nav">
				<a href="${pageContext.request.contextPath}/products">Sản phẩm</a> <a
					href="#">Blog</a> <a href="#">Liên hệ</a> <a
					href="${pageContext.request.contextPath}/cart" class="cart-icon">
					🛒 <c:if test="${not empty sessionScope.CART}">
						<c:set var="cartQty" value="0" />
						<c:forEach var="e" items="${sessionScope.CART}">
							<c:set var="cartQty" value="${cartQty + e.value.quantity}" />
						</c:forEach>

						<c:if test="${cartQty > 0}">
							<span class="cart-badge">${cartQty}</span>
						</c:if>
					</c:if>
				</a>
			</nav>

			<!-- ACTIONS -->
			<div class="header-actions">

				<!-- SEARCH -->
				<div class="search-wrapper">
					<form action="${pageContext.request.contextPath}/search"
						method="get" class="search-form">
						<span class="search-icon">🔍</span> <input id="search-input"
							name="q" placeholder="Tìm sản phẩm..." autocomplete="off"
							value="${param.q}">
					</form>
					<div id="search-results" class="search-results"></div>
				</div>

				<!-- AUTH -->
				<div class="auth-links">
					<c:choose>
						<c:when test="${not empty sessionScope.user}">
							<div class="user-dropdown">
								<button class="user-btn" type="button" aria-haspopup="menu"
									aria-expanded="false">
									<span class="user-icon">👤</span> <span class="user-name">${sessionScope.user.username}</span>
								</button>

								<div class="user-menu">
									<a href="${pageContext.request.contextPath}/account">👤 Tài
										khoản</a> <a href="${pageContext.request.contextPath}/orders">📦
										Đơn hàng</a>

									<div class="menu-divider"></div>

									<form method="post"
										action="${pageContext.request.contextPath}/logout">
										<!-- ✅ CSRF token -->
										<input type="hidden" name="csrf_token"
											value="<c:out value='${sessionScope.CSRF_TOKEN}'/>">
										<button type="submit" class="logout-btn">🚪 Đăng xuất</button>
									</form>
								</div>
							</div>
						</c:when>

						<c:otherwise>
							<div class="auth-buttons">
								<a href="${pageContext.request.contextPath}/login"
									class="btn-login">Đăng nhập</a> <a
									href="${pageContext.request.contextPath}/register"
									class="btn-register">Đăng ký</a>
							</div>
						</c:otherwise>
					</c:choose>
				</div>

				<button class="menu-toggle" id="menu-toggle" type="button">☰</button>
			</div>
		</div>

		<!-- MOBILE NAV -->
		<nav class="mobile-nav" id="mobile-nav">
			<a href="${pageContext.request.contextPath}/products">Sản phẩm</a> <a
				href="#">Blog</a> <a href="#">Liên hệ</a> <a
				href="${pageContext.request.contextPath}/cart">Giỏ hàng</a>

			<c:choose>
				<c:when test="${not empty sessionScope.user}">
					<a href="${pageContext.request.contextPath}/account">Tài khoản</a>
					<form method="post"
						action="${pageContext.request.contextPath}/logout">
						<!-- ✅ CSRF token -->
						<input type="hidden" name="csrf_token"
							value="<c:out value='${sessionScope.CSRF_TOKEN}'/>">
						<button type="submit">Đăng xuất</button>
					</form>
				</c:when>
				<c:otherwise>
					<div class="auth-buttons">
						<a href="${pageContext.request.contextPath}/login"
							class="btn-login">Đăng nhập</a> <a
							href="${pageContext.request.contextPath}/register"
							class="btn-register">Đăng ký</a>
					</div>
				</c:otherwise>
			</c:choose>
		</nav>
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

	<!-- ================= FOOTER (CHỈ 1 LẦN, CÓ THỂ ẨN) ================= -->
	<c:if test="${empty hideFooter}">
		<footer class="site-footer">
			<div class="container">© ${pageContext.request.serverName}
				MyCosmetic Shop — Đồ án môn học</div>
		</footer>
	</c:if>

</body>
</html>

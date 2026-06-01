<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width,initial-scale=1">

	<title>
		<c:choose>
			<c:when test="${not empty pageTitle}">
				<c:out value="${pageTitle}" />
			</c:when>
			<c:otherwise>MyCosmetic Shop</c:otherwise>
		</c:choose>
	</title>

	<!-- GLOBAL CSS -->
	<link rel="stylesheet"
	      href="${pageContext.request.contextPath}/assets/css/base.css?v=20260531">

	<!-- PAGE CSS -->
	<c:if test="${not empty pageCss}">
		<c:choose>
			<%-- Trường hợp servlet truyền: /assets/css/home.css --%>
			<c:when test="${fn:startsWith(pageCss, '/assets/')}">
				<link rel="stylesheet"
				      href="${pageContext.request.contextPath}${pageCss}?v=20260531">
			</c:when>

			<%-- Trường hợp servlet truyền: assets/css/home.css --%>
			<c:when test="${fn:startsWith(pageCss, 'assets/')}">
				<link rel="stylesheet"
				      href="${pageContext.request.contextPath}/${pageCss}?v=20260531">
			</c:when>

			<%-- Trường hợp servlet truyền: /home.css --%>
			<c:when test="${fn:startsWith(pageCss, '/')}">
				<link rel="stylesheet"
				      href="${pageContext.request.contextPath}/assets/css${pageCss}?v=20260531">
			</c:when>

			<%-- Trường hợp servlet truyền: home.css hoặc admin/admin-list.css --%>
			<c:otherwise>
				<link rel="stylesheet"
				      href="${pageContext.request.contextPath}/assets/css/${pageCss}?v=20260531">
			</c:otherwise>
		</c:choose>
	</c:if>

	<script>
		window.APP_CTX = "${pageContext.request.contextPath}";
	</script>
</head>

<body class="site-body">

<!-- ================= HEADER ================= -->
<header class="site-header">
	<div class="container header-inner">

		<a class="logo" href="${pageContext.request.contextPath}/">MyCosmetic</a>

		<div class="header-actions">

			<div class="search-wrapper search-history-wrapper">
				<form action="${pageContext.request.contextPath}/search"
				      method="get"
				      class="search-form"
				      autocomplete="off">
					<span class="search-icon">🔍</span>
					<input id="search-input"
					       name="q"
					       placeholder="Tìm sản phẩm..."
					       autocomplete="off"
					       value="${fn:escapeXml(param.q)}">
				</form>

				<div id="search-results" class="search-results"></div>

				<div id="searchHistoryDropdown" class="header-search-history" aria-label="Lịch sử tìm kiếm">
					<div class="header-search-history__head">
						<span>Lịch sử tìm kiếm</span>
						<a href="${pageContext.request.contextPath}/account?tab=search-history">Xem tất cả</a>
					</div>

					<div id="searchHistoryBody" class="header-search-history__body">
						<div class="header-search-history__empty">Đang tải lịch sử tìm kiếm...</div>
					</div>
				</div>
			</div>

			<div class="auth-links">
				<c:choose>
					<c:when test="${not empty sessionScope.user}">
						<div class="user-dropdown">
							<button class="user-btn" type="button" aria-haspopup="menu" aria-expanded="false">
								<span class="user-icon">👤</span>
								<span class="user-name">
										<c:out value="${sessionScope.user.username}" />
									</span>
							</button>

							<div class="user-menu">
								<a href="${pageContext.request.contextPath}/account">👤 Tài khoản</a>
								<a href="${pageContext.request.contextPath}/orders">📦 Đơn hàng</a>

								<div class="menu-divider"></div>

								<form method="post" action="${pageContext.request.contextPath}/logout">
									<input type="hidden"
									       name="csrf_token"
									       value="<c:out value='${sessionScope.CSRF_TOKEN}'/>">
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


			<a href="${pageContext.request.contextPath}/cart"
			   class="cart-icon"
			   aria-label="Giỏ hàng">
				<span class="cart-icon__symbol">🛒</span>

				<c:if test="${not empty sessionScope.CART}">
					<c:set var="cartQty" value="0" />

					<c:forEach var="e" items="${sessionScope.CART}">
						<c:set var="cartQty" value="${cartQty + e.value.quantity}" />
					</c:forEach>

					<c:if test="${cartQty > 0}">
						<span class="cart-badge">
							<c:out value="${cartQty > 99 ? '99+' : cartQty}" />
						</span>
					</c:if>
				</c:if>
			</a>

			<c:set var="headerUnreadCount" value="0" />
			<c:choose>
				<c:when test="${not empty requestScope.unreadNotificationCount}">
					<c:set var="headerUnreadCount" value="${requestScope.unreadNotificationCount}" />
				</c:when>
				<c:when test="${not empty requestScope.unreadCount}">
					<c:set var="headerUnreadCount" value="${requestScope.unreadCount}" />
				</c:when>
			</c:choose>

			<c:choose>
				<c:when test="${not empty requestScope.latestNotifications}">
					<c:set var="headerNotifications" value="${requestScope.latestNotifications}" />
				</c:when>
				<c:otherwise>
					<c:set var="headerNotifications" value="${requestScope.notifications}" />
				</c:otherwise>
			</c:choose>

			<div class="notification-container">
				<button id="notifBellBtn"
				        class="notification-bell"
				        type="button"
				        aria-label="Thông báo"
				        aria-haspopup="true"
				        aria-expanded="false">
					<span class="notification-bell__icon">🔔</span>

					<c:if test="${headerUnreadCount > 0}">
						<span class="notif-badge">
							<c:out value="${headerUnreadCount > 99 ? '99+' : headerUnreadCount}" />
						</span>
					</c:if>
				</button>

				<div class="notif-dropdown"
				     id="notifDropdown"
				     aria-label="Danh sách thông báo">
					<c:choose>
						<c:when test="${not empty sessionScope.user}">
							<div class="notif-dropdown__head">
								<span>Thông báo mới nhận</span>

								<c:if test="${headerUnreadCount > 0}">
									<form class="notification-mark-all-form"
									      method="post"
									      action="${pageContext.request.contextPath}/notifications">
										<input type="hidden" name="action" value="markAllRead">
										<input type="hidden"
										       name="csrf_token"
										       value="<c:out value='${sessionScope.CSRF_TOKEN}'/>">
										<button type="submit" class="notification-mark-all-btn">
											Đánh dấu đã đọc
										</button>
									</form>
								</c:if>
							</div>

							<div class="notif-dropdown__body">
								<c:choose>
									<c:when test="${not empty headerNotifications}">
										<c:forEach var="notif" items="${headerNotifications}">
											<c:url var="notifReadUrl" value="/notifications">
												<c:param name="action" value="read" />
												<c:param name="id" value="${notif.id}" />
												<c:param name="returnUrl" value="${empty notif.targetUrl ? '/notifications' : notif.targetUrl}" />
											</c:url>

											<a href="${notifReadUrl}"
											   class="notification-item ${notif.read ? 'is-read' : 'is-unread'}">
												<span class="notification-item__icon">
													<c:choose>
														<c:when test="${notif.type == 'ORDER_CREATED'}">🛒</c:when>
														<c:when test="${notif.type == 'ORDER_CONFIRMED'}">✅</c:when>
														<c:when test="${notif.type == 'ORDER_SHIPPING'}">🚚</c:when>
														<c:when test="${notif.type == 'ORDER_DELIVERED'}">📦</c:when>
														<c:when test="${notif.type == 'ORDER_DELIVERY_FAILED'}">⚠️</c:when>
														<c:when test="${notif.type == 'ORDER_CANCELLED'}">❌</c:when>
														<c:when test="${fn:startsWith(notif.type, 'CANCEL_REQUEST')}">📝</c:when>
														<c:when test="${fn:startsWith(notif.type, 'RETURN_REQUEST')}">↩️</c:when>
														<c:when test="${fn:startsWith(notif.type, 'REVIEW')}">⭐</c:when>
														<c:when test="${notif.type == 'VOUCHER'}">🎟️</c:when>
														<c:otherwise>🔔</c:otherwise>
													</c:choose>
												</span>

												<span class="notification-item__content">
													<strong><c:out value="${notif.title}" /></strong>
													<small><c:out value="${notif.message}" /></small>
												</span>

												<c:if test="${not notif.read}">
													<span class="notification-item__dot" aria-hidden="true"></span>
												</c:if>
											</a>
										</c:forEach>
									</c:when>

									<c:otherwise>
										<div class="notification-empty">
											<div class="notification-empty__icon">🔔</div>
											<p>Bạn chưa có thông báo nào mới.</p>
										</div>
									</c:otherwise>
								</c:choose>
							</div>

							<a href="${pageContext.request.contextPath}/notifications"
							   class="notification-view-all">
								Xem tất cả thông báo
							</a>
						</c:when>

						<c:otherwise>
							<div class="notification-guest">
								<div class="notification-guest__icon">🔔</div>

								<h4>Bạn có thông báo mới không?</h4>

								<p>
									Đăng nhập ngay để xem các thông báo khuyến mãi,
									voucher và theo dõi hành trình đơn hàng.
								</p>

								<a href="${pageContext.request.contextPath}/login"
								   class="notification-login-link">
									Đăng nhập ngay
								</a>
							</div>
						</c:otherwise>
					</c:choose>
				</div>
			</div>

		</div>
	</div>

	<jsp:include page="/jsp/common/header.jsp" />
</header>

<!-- ================= MAIN ================= -->
<main class="site-main">
	<c:choose>
		<c:when test="${not empty pageContent}">
			<c:set var="__IN_BASE__" value="true" scope="request" />
			<c:import url="${pageContent}" />
		</c:when>

		<c:otherwise>
			<div class="container base-empty-content">
				Chưa có nội dung để hiển thị.
			</div>
		</c:otherwise>
	</c:choose>
</main>

<jsp:include page="/jsp/common/footer.jsp" />

<!-- GLOBAL JS: chỉ load 1 lần -->
<script defer src="${pageContext.request.contextPath}/assets/js/main.js?v=20260531"></script>


<script>
	document.addEventListener("DOMContentLoaded", function () {
		const searchInput = document.getElementById("search-input");
		const searchResults = document.getElementById("search-results");
		const historyDropdown = document.getElementById("searchHistoryDropdown");
		const historyBody = document.getElementById("searchHistoryBody");

		if (!searchInput || !historyDropdown || !historyBody) {
			return;
		}

		const contextPath = window.APP_CTX || "${pageContext.request.contextPath}";
		let historyLoaded = false;
		let historyLoading = false;

		function escapeHtml(value) {
			return String(value || "")
					.replaceAll("&", "&amp;")
					.replaceAll("<", "&lt;")
					.replaceAll(">", "&gt;")
					.replaceAll('"', "&quot;")
					.replaceAll("'", "&#039;");
		}

		function showHistoryDropdown() {
			if (searchInput.value.trim().length > 0) {
				hideHistoryDropdown();
				return;
			}

			if (searchResults) {
				searchResults.classList.remove("show");
				searchResults.removeAttribute("style");
			}

			historyDropdown.classList.add("is-open");

			if (!historyLoaded && !historyLoading) {
				loadRecentSearchHistory();
			}
		}

		function hideHistoryDropdown() {
			historyDropdown.classList.remove("is-open");
		}

		function hideHistoryDropdownLater() {
			setTimeout(hideHistoryDropdown, 180);
		}

		function loadRecentSearchHistory() {
			historyLoading = true;

			fetch(contextPath + "/search-history/recent", {
				method: "GET",
				headers: {
					"Accept": "application/json"
				}
			})
					.then(function (response) {
						if (!response.ok) {
							throw new Error("Cannot load search history");
						}

						return response.json();
					})
					.then(function (histories) {
						historyLoaded = true;
						renderRecentSearchHistory(Array.isArray(histories) ? histories : []);
					})
					.catch(function () {
						historyBody.innerHTML =
								'<div class="header-search-history__empty">Không tải được lịch sử tìm kiếm.</div>';
					})
					.finally(function () {
						historyLoading = false;
					});
		}

		function renderRecentSearchHistory(histories) {
			if (!histories.length) {
				historyBody.innerHTML =
						'<div class="header-search-history__empty">Chưa có lịch sử tìm kiếm.</div>';
				return;
			}

			historyBody.innerHTML = histories.map(function (item) {
				const keyword = item.keyword || "";
				const safeKeyword = escapeHtml(keyword);
				const resultCount = item.resultCount || 0;
				const searchCount = item.searchCount || 1;
				const href = contextPath + "/search?q=" + encodeURIComponent(keyword);

				return ''
						+ '<a class="header-search-history__item" href="' + href + '" title="Tìm lại: ' + safeKeyword + '">'
						+ '  <span class="header-search-history__icon">↺</span>'
						+ '  <span class="header-search-history__content">'
						+ '    <strong>' + safeKeyword + '</strong>'
						+ '    <small>' + resultCount + ' kết quả • Đã tìm ' + searchCount + ' lần</small>'
						+ '  </span>'
						+ '</a>';
			}).join("");
		}

		searchInput.addEventListener("focus", showHistoryDropdown);
		searchInput.addEventListener("click", showHistoryDropdown);
		searchInput.addEventListener("blur", hideHistoryDropdownLater);

		searchInput.addEventListener("input", function () {
			if (searchInput.value.trim().length > 0) {
				hideHistoryDropdown();

				/*
                 * Trả quyền hiển thị lại cho dropdown gợi ý sản phẩm cũ.
                 * Không để inline style display:none chặn .search-results.show.
                 */
				if (searchResults) {
					searchResults.removeAttribute("style");
				}
			} else {
				if (searchResults) {
					searchResults.classList.remove("show");
					searchResults.removeAttribute("style");
				}
				showHistoryDropdown();
			}
		});
	});
</script>

<script>
	document.addEventListener("DOMContentLoaded", function () {
		const bellBtn = document.getElementById("notifBellBtn");
		const dropdown = document.getElementById("notifDropdown");

		if (!bellBtn || !dropdown) {
			return;
		}

		function closeNotificationDropdown() {
			dropdown.classList.remove("is-open");
			bellBtn.setAttribute("aria-expanded", "false");
		}

		bellBtn.addEventListener("click", function (event) {
			event.preventDefault();
			event.stopPropagation();

			const isOpen = dropdown.classList.toggle("is-open");
			bellBtn.setAttribute("aria-expanded", String(isOpen));
		});

		document.addEventListener("click", function (event) {
			if (!dropdown.contains(event.target) && !bellBtn.contains(event.target)) {
				closeNotificationDropdown();
			}
		});

		document.addEventListener("keydown", function (event) {
			if (event.key === "Escape") {
				closeNotificationDropdown();
			}
		});
	});
</script>

</body>
</html>

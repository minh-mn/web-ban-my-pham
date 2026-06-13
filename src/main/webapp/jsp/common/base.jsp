<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%
	if (request.getAttribute("headerNotifications") == null) {
		com.webshop.app.model.User loggedUser = (com.webshop.app.model.User) session.getAttribute("user");
		if (loggedUser == null) {
			loggedUser = (com.webshop.app.model.User) session.getAttribute("authUser");
		}
		if (loggedUser != null) {
			com.webshop.app.dao.NotificationDAO navNotifDAO = new com.webshop.app.dao.NotificationDAO();
			try {
				if (loggedUser.isAdmin()) {
					java.util.List<com.webshop.app.model.Notification> adminNotifs = navNotifDAO.findLatestByAdmin(5);
					int adminUnread = navNotifDAO.countUnreadByAdmin();
					request.setAttribute("headerNotifications", adminNotifs);
					request.setAttribute("adminUnreadCount", adminUnread);
				} else {
					java.util.List<com.webshop.app.model.Notification> userNotifs = navNotifDAO.findLatestByUser(loggedUser.getId(), 5);
					int userUnread = navNotifDAO.countUnreadByUserId(loggedUser.getId());
					request.setAttribute("headerNotifications", userNotifs);
					request.setAttribute("unreadNotificationCount", userUnread);
				}
			} catch (Exception ignored) {}
		}
	}
%>


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


	<!-- RED BUTTON THEME: load sau page CSS de dong bo mau nut -->
	<link rel="stylesheet"
	      href="${pageContext.request.contextPath}/assets/css/theme-red-buttons.css?v=20260613_6">

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

			<c:set var="unreadCount" value="${sessionScope.user.admin ? adminUnreadCount : unreadNotificationCount}" />

			<button id="notifBellBtn" class="notification-bell" type="button" aria-haspopup="true" aria-expanded="false">
				<span class="notification-bell__icon">🔔</span>

				<%-- Hiển thị số lượng thông báo chưa đọc màu đỏ nếu > 0 --%>
				<c:if test="${unreadCount > 0}">
        <span class="notif-badge" style="background: red; color: white; padding: 2px 6px; border-radius: 50%; font-size: 11px; position: absolute; top: -5px; right: -5px;">
            <c:out value="${unreadCount > 99 ? '99+' : unreadCount}" />
        </span>
				</c:if>
			</button>

			<div class="notif-dropdown" id="notifDropdown">
				<div class="notif-dropdown__header" style="padding: 10px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between;">
					<strong>Thông báo mới nhận</strong>
				</div>

				<div class="notif-dropdown__body" style="max-height: 300px; overflow-y: auto;">
					<c:choose>
						<c:when test="${not empty headerNotifications}">
							<%-- Vòng lặp duyệt danh sách thông báo --%>
							<c:forEach var="notif" items="${headerNotifications}">

								<%-- Đường dẫn chuyển hướng khi click đọc thông báo --%>
								<c:url var="notifReadUrl" value="/notifications/read">
									<c:param name="id" value="${notif.id}" />
									<c:param name="redirect" value="${empty notif.targetUrl ? '/admin/notifications' : notif.targetUrl}" />
								</c:url>

								<a href="${notifReadUrl}" class="notification-item ${notif.read ? 'is-read' : 'is-unread'}"
								   style="display: flex; padding: 10px; border-bottom: 1px solid #f9f9f9; text-decoration: none; color: #333; ${!notif.read ? 'background-color: #f0f7ff;' : ''}">

			                        <span class="notification-item__icon" style="font-size: 20px; margin-right: 10px;">
			                            <c:choose>
											<c:when test="${notif.type == 'CONTACT_CREATED'}">✉️</c:when>
											<c:when test="${notif.type == 'ORDER_CREATED'}">🛒</c:when>
											<c:otherwise>🔔</c:otherwise>
										</c:choose>
			                        </span>

									<span class="notification-item__content" style="display: flex; flex-direction: column;">
			                            <strong style="font-size: 13px; color: #111;">
			                                <c:out value="${notif.title}" />
			                            </strong>
			                            <small style="font-size: 12px; color: #666; margin-top: 2px;">
			                                <c:out value="${notif.message}" />
			                            </small>
			                        </span>

									<c:if test="${not notif.read}">
										<span class="notification-item__dot" style="width: 8px; height: 8px; background-color: #007bff; border-radius: 50%; margin-left: auto; align-self: center;"></span>
									</c:if>
								</a>
							</c:forEach>
						</c:when>

						<%-- Nếu không có thông báo nào --%>
						<c:otherwise>
							<div class="notification-empty" style="padding: 20px; text-align: center; color: #999;">
								<p>Bạn không có thông báo nào mới.</p>
							</div>
						</c:otherwise>
					</c:choose>
				</div>

				<div class="notif-dropdown__footer" style="padding: 10px; text-align: center; border-top: 1px solid #eee;">
					<a href="${pageContext.request.contextPath}${sessionScope.user.admin ? '/admin/notifications' : '/notifications'}" style="font-size: 12px; color: #ff5fa2; text-decoration: none; font-weight: bold;">
						Xem tất cả thông báo
					</a>
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

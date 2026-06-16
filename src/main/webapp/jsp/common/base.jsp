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
	      href="${pageContext.request.contextPath}/assets/css/base.css?v=20260615_header_button_stable_v8">

	<!-- PAGE CSS -->
	<c:if test="${not empty pageCss}">
		<c:choose>
			<%-- Trường hợp servlet truyền: /assets/css/home.css --%>
			<c:when test="${fn:startsWith(pageCss, '/assets/')}">
				<link rel="stylesheet"
				      href="${pageContext.request.contextPath}${pageCss}?v=20260615_header_button_stable_v8">
			</c:when>

			<%-- Trường hợp servlet truyền: assets/css/home.css --%>
			<c:when test="${fn:startsWith(pageCss, 'assets/')}">
				<link rel="stylesheet"
				      href="${pageContext.request.contextPath}/${pageCss}?v=20260615_header_button_stable_v8">
			</c:when>

			<%-- Trường hợp servlet truyền: /home.css --%>
			<c:when test="${fn:startsWith(pageCss, '/')}">
				<link rel="stylesheet"
				      href="${pageContext.request.contextPath}/assets/css${pageCss}?v=20260615_header_button_stable_v8">
			</c:when>

			<%-- Trường hợp servlet truyền: home.css hoặc admin/admin-list.css --%>
			<c:otherwise>
				<link rel="stylesheet"
				      href="${pageContext.request.contextPath}/assets/css/${pageCss}?v=20260615_header_button_stable_v8">
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

		<a class="logo logo-art" href="${pageContext.request.contextPath}/" aria-label="MyCosmetic trang chủ">
			<span class="logo-art__mark">MyCosmetic</span>
			<span class="logo-art__tagline">Be your own kind of beautiful</span>
		</a>

		<div class="header-actions">

			<div class="search-wrapper search-history-wrapper">
				<form action="${pageContext.request.contextPath}/search"
				      method="get"
				      class="search-form"
				      autocomplete="off">
					<span class="search-icon" aria-hidden="true">
						<svg viewBox="0 0 24 24" focusable="false">
							<circle cx="11" cy="11" r="6.5"></circle>
							<path d="M16 16l4 4"></path>
						</svg>
					</span>
					<input id="search-input"
					       name="q"
					       placeholder="Tìm sản phẩm..."
					       autocomplete="off"
					       value="${fn:escapeXml(param.q)}">
					<button class="search-submit-btn" type="submit" aria-label="Tìm kiếm">
						<svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
							<circle cx="11" cy="11" r="6.5"></circle>
							<path d="M16 16l4 4"></path>
						</svg>
					</button>
				</form>

				<div id="search-results" class="search-results"></div>

				<div id="searchHistoryDropdown" class="header-search-history" aria-label="Lịch sử tìm kiếm">
					<div class="header-search-history__head">
						<span>Lịch sử tìm kiếm</span>
						<a href="${pageContext.request.contextPath}/search-history">Xem tất cả</a>
					</div>

					<div id="searchHistoryBody" class="header-search-history__body">
						<div class="header-search-history__empty">Đang tải lịch sử tìm kiếm...</div>
					</div>
				</div>
			</div>

			<div class="auth-links">
				<c:choose>
					<c:when test="${not empty sessionScope.user}">
						<details class="user-dropdown" id="userDropdown">
							<summary id="userMenuBtn"
							         class="user-btn"
							         aria-haspopup="menu">
								<span class="user-icon" aria-hidden="true">
									<svg viewBox="0 0 24 24" focusable="false">
										<circle cx="12" cy="8" r="4"></circle>
										<path d="M4.5 20c1.6-4 4.2-6 7.5-6s5.9 2 7.5 6"></path>
									</svg>
								</span>

								<span class="user-name">
									<c:choose>
										<c:when test="${not empty sessionScope.user.username}">
											<c:out value="${sessionScope.user.username}" />
										</c:when>
										<c:when test="${not empty sessionScope.user.fullName}">
											<c:out value="${sessionScope.user.fullName}" />
										</c:when>
										<c:otherwise>Tài khoản</c:otherwise>
									</c:choose>
								</span>

								<span class="user-chevron" aria-hidden="true">
									<svg viewBox="0 0 24 24" focusable="false">
										<path d="M6 9l6 6 6-6"></path>
									</svg>
								</span>
							</summary>

							<div class="user-menu" id="userMenu" role="menu">
								<a href="${pageContext.request.contextPath}/account" class="user-menu-item" role="menuitem">
									<span class="user-menu-item__icon" aria-hidden="true">
										<svg viewBox="0 0 24 24" focusable="false">
											<circle cx="12" cy="8" r="4"></circle>
											<path d="M4.5 20c1.6-4 4.2-6 7.5-6s5.9 2 7.5 6"></path>
										</svg>
									</span>
									<span>Tài khoản</span>
								</a>

								<a href="${pageContext.request.contextPath}/orders" class="user-menu-item" role="menuitem">
									<span class="user-menu-item__icon" aria-hidden="true">
										<svg viewBox="0 0 24 24" focusable="false">
											<path d="M7 4.5h10l1.5 3v11a1.5 1.5 0 0 1-1.5 1.5h-10A1.5 1.5 0 0 1 5.5 18.5v-11L7 4.5z"></path>
											<path d="M9 9.5h6"></path>
											<path d="M9 13h6"></path>
										</svg>
									</span>
									<span>Đơn hàng</span>
								</a>

								<div class="menu-divider"></div>

								<form method="post" action="${pageContext.request.contextPath}/logout">
									<input type="hidden"
									       name="csrf_token"
									       value="<c:out value='${sessionScope.CSRF_TOKEN}'/>">

									<button type="submit" class="logout-btn" role="menuitem">
										<span class="user-menu-item__icon" aria-hidden="true">
											<svg viewBox="0 0 24 24" focusable="false">
												<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
												<path d="M16 17l5-5-5-5"></path>
												<path d="M21 12H9"></path>
											</svg>
										</span>
										<span>Đăng xuất</span>
									</button>
								</form>
							</div>
						</details>
					</c:when>

					<c:otherwise>
						<div class="auth-buttons">
							<a href="${pageContext.request.contextPath}/login" class="btn-login">
								<span class="mc-action-icon" aria-hidden="true">
									<svg viewBox="0 0 24 24" focusable="false"><circle cx="12" cy="8" r="4"></circle><path d="M4.5 20c1.6-4 4.2-6 7.5-6s5.9 2 7.5 6"></path></svg>
								</span>
								<span>Đăng nhập</span>
							</a>
							<a href="${pageContext.request.contextPath}/register" class="btn-register">
								<span class="mc-action-icon" aria-hidden="true">
									<svg viewBox="0 0 24 24" focusable="false"><path d="M4 20h4l10.5-10.5a2.1 2.1 0 0 0 0-3L17.5 5.5a2.1 2.1 0 0 0-3 0L4 16v4z"></path><path d="M13.5 7.5l3 3"></path></svg>
								</span>
								<span>Đăng ký</span>
							</a>
						</div>
					</c:otherwise>
				</c:choose>
			</div>


			<a href="${pageContext.request.contextPath}/cart"
			   class="cart-icon"
			   aria-label="Giỏ hàng">
				<span class="cart-icon__symbol" aria-hidden="true">
				<svg viewBox="0 0 24 24" focusable="false"><path d="M4 5h2l2.3 10.5h8.9l2-7.5H7.2"></path><circle cx="10" cy="19" r="1.6"></circle><circle cx="17" cy="19" r="1.6"></circle></svg>
			</span>

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
			<c:set var="notificationsPageUrl" value="${pageContext.request.contextPath}${sessionScope.user.admin ? '/admin/notifications' : '/notifications'}" />

			<div class="notification-container" id="notificationContainer">
				<button id="notifBellBtn" class="notification-bell" type="button" aria-haspopup="true" aria-expanded="false">
					<span class="notification-bell__icon" aria-hidden="true">
						<svg viewBox="0 0 24 24" focusable="false"><path d="M18 9a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"></path><path d="M10 21a2.2 2.2 0 0 0 4 0"></path></svg>
					</span>

					<c:if test="${unreadCount > 0}">
						<span class="notif-badge">
							<c:out value="${unreadCount > 99 ? '99+' : unreadCount}" />
						</span>
					</c:if>
				</button>

				<div class="notif-dropdown" id="notifDropdown">
					<div class="notif-dropdown__head">
						<div class="notif-dropdown__title-group">
							<strong>Thông báo mới nhận</strong>
							<span>Theo dõi cập nhật đơn hàng và hoạt động mới nhất của bạn</span>
						</div>
						<span class="notif-dropdown__counter ${unreadCount > 0 ? 'has-new' : 'is-empty'}">
							<c:choose>
								<c:when test="${unreadCount > 0}">
									<span class="notif-dropdown__counter-number">
										<c:out value="${unreadCount > 99 ? '99+' : unreadCount}" />
									</span>
									<span class="notif-dropdown__counter-text">thông báo mới</span>
								</c:when>
								<c:otherwise>
									<span class="notif-dropdown__counter-text">Không có thông báo mới</span>
								</c:otherwise>
							</c:choose>
						</span>
					</div>

					<div class="notif-dropdown__body">
						<c:choose>
							<c:when test="${not empty headerNotifications}">
								<c:forEach var="notif" items="${headerNotifications}">
									<c:url var="notifReadUrl" value="/notifications/read">
										<c:param name="id" value="${notif.id}" />
										<c:param name="redirect" value="${empty notif.targetUrl ? (sessionScope.user.admin ? '/admin/notifications' : '/notifications') : notif.targetUrl}" />
									</c:url>

									<a href="${notifReadUrl}" class="notif-card ${notif.read ? 'is-read' : 'is-unread'}">
										<span class="notif-card__icon" aria-hidden="true"><span class="notif-card__brand-mark">MC</span></span>
										<span class="notif-card__content">
											<span class="notif-card__top">
												<strong><c:out value="${notif.title}" /></strong>
												<c:if test="${not notif.read}"><span class="notif-card__badge">Mới</span></c:if>
											</span>
											<span class="notif-card__message"><c:out value="${notif.message}" /></span>
											<span class="notif-card__meta">
												<span class="notif-card__time"><c:out value="${not empty notif.createdAt ? notif.createdAt : 'Thông báo hệ thống'}" /></span>
											</span>
										</span>
										<c:if test="${not notif.read}"><span class="notif-card__dot" aria-hidden="true"></span></c:if>
									</a>
								</c:forEach>
							</c:when>
							<c:otherwise>
								<div class="notification-empty">
									<div class="notification-empty__icon">MC</div>
									<p>Bạn chưa có thông báo mới.</p>
								</div>
							</c:otherwise>
						</c:choose>
					</div>

					<a href="${notificationsPageUrl}" class="notif-dropdown__view-all">Xem tất cả thông báo</a>
				</div>
			</div></div>
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
<script defer src="${pageContext.request.contextPath}/assets/js/main.js?v=20260615_header_button_stable_v8"></script>


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

		function resetOverlayInlineStyle(element) {
			if (!element) {
				return;
			}

			/*
			 * Một số đoạn JS đóng overlay cũ có set inline style display:none/opacity:0.
			 * Nếu không xóa các style này thì lần click đầu tiên chỉ add class is-open
			 * nhưng dropdown vẫn bị ẩn, người dùng phải bấm lần thứ hai mới thấy.
			 */
			element.removeAttribute("style");
		}

		function showHistoryDropdown() {
			if (searchInput.value.trim().length > 0) {
				hideHistoryDropdown();
				return;
			}

			if (searchResults) {
				searchResults.classList.remove("show", "is-open", "active");
				resetOverlayInlineStyle(searchResults);
			}

			resetOverlayInlineStyle(historyDropdown);
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

		const searchForm = searchInput.closest("form");
		if (searchForm) {
			searchForm.addEventListener("submit", function (event) {
				if (searchInput.value.trim().length === 0) {
					event.preventDefault();
					searchInput.focus();
					showHistoryDropdown();
				}
			});
		}

		if (historyDropdown) {
			/* Giữ dropdown mở khi bấm vào các link bên trong, tránh blur input đóng sớm. */
			historyDropdown.addEventListener("mousedown", function (event) {
				event.stopPropagation();
			});
		}

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
	/* FINAL SAFETY PATCH: đóng search dropdown trước khi mở user menu, kể cả main.js còn giữ overlay cũ. */
	window.mcForceCloseSearchOverlays = function () {
		var searchInput = document.getElementById("search-input");
		var historyDropdown = document.getElementById("searchHistoryDropdown");
		var searchResults = document.getElementById("search-results");

		if (searchInput) {
			searchInput.blur();
		}

		if (historyDropdown) {
			historyDropdown.classList.remove("is-open", "show", "active");
			historyDropdown.style.display = "none";
			historyDropdown.style.pointerEvents = "none";
		}

		if (searchResults) {
			searchResults.classList.remove("show", "is-open", "active");
			searchResults.style.display = "none";
			searchResults.style.pointerEvents = "none";
		}
	};
</script>

<script>
	(function () {
		function closeSearchOverlays() {
			if (window.mcForceCloseSearchOverlays) { window.mcForceCloseSearchOverlays(); return; }
			var searchInput = document.getElementById("search-input");
			var historyDropdown = document.getElementById("searchHistoryDropdown");
			var searchResults = document.getElementById("search-results");

			if (searchInput) {
				searchInput.blur();
			}

			if (historyDropdown) {
				historyDropdown.classList.remove("is-open");
			}

			if (searchResults) {
				searchResults.classList.remove("show");
				searchResults.removeAttribute("style");
			}
		}

		window.mcCloseUserMenu = function () {
			var userDropdown = document.getElementById("userDropdown");
			var userMenuBtn = document.getElementById("userMenuBtn");
			var userMenu = document.getElementById("userMenu");

			if (!userDropdown || !userMenuBtn || !userMenu) {
				return;
			}

			userMenu.classList.remove("show");
			userDropdown.classList.remove("is-open");
			userMenuBtn.setAttribute("aria-expanded", "false");
		};

		window.mcToggleUserMenu = function (event) {
			if (event) {
				event.preventDefault();
				event.stopPropagation();
			}

			var userDropdown = document.getElementById("userDropdown");
			var userMenuBtn = document.getElementById("userMenuBtn");
			var userMenu = document.getElementById("userMenu");

			if (!userDropdown || !userMenuBtn || !userMenu) {
				return false;
			}

			closeSearchOverlays();

			var isOpen = userDropdown.classList.contains("is-open") || userMenu.classList.contains("show");

			if (isOpen) {
				window.mcCloseUserMenu();
			} else {
				userMenu.classList.add("show");
				userDropdown.classList.add("is-open");
				userMenuBtn.setAttribute("aria-expanded", "true");
			}

			return false;
		};

		document.addEventListener("DOMContentLoaded", function () {
			var userDropdown = document.getElementById("userDropdown");
			var userMenuBtn = document.getElementById("userMenuBtn");

			if (userMenuBtn) {
				userMenuBtn.addEventListener("click", window.mcToggleUserMenu);
			}

			document.addEventListener("click", function (event) {
				if (userDropdown && !userDropdown.contains(event.target)) {
					window.mcCloseUserMenu();
				}
			});

			document.addEventListener("keydown", function (event) {
				if (event.key === "Escape") {
					window.mcCloseUserMenu();
				}
			});
		});
	})();
</script>

<script>
	document.addEventListener("DOMContentLoaded", function () {
		const bellBtn = document.getElementById("notifBellBtn");
		const dropdown = document.getElementById("notifDropdown");
		const notificationContainer = document.getElementById("notificationContainer");

		if (!bellBtn || !dropdown || !notificationContainer) {
			return;
		}

		let closeTimer = null;

		function openNotificationDropdown() {
			if (closeTimer) {
				clearTimeout(closeTimer);
				closeTimer = null;
			}
			dropdown.classList.add("is-open");
			bellBtn.setAttribute("aria-expanded", "true");
		}

		function closeNotificationDropdown() {
			dropdown.classList.remove("is-open");
			bellBtn.setAttribute("aria-expanded", "false");
		}

		function scheduleClose() {
			if (closeTimer) clearTimeout(closeTimer);
			closeTimer = setTimeout(closeNotificationDropdown, 120);
		}

		notificationContainer.addEventListener("mouseenter", openNotificationDropdown);
		notificationContainer.addEventListener("mouseleave", scheduleClose);
		bellBtn.addEventListener("focus", openNotificationDropdown);
		dropdown.addEventListener("focusin", openNotificationDropdown);
		bellBtn.addEventListener("click", function (event) {
			if (window.matchMedia && window.matchMedia("(hover: none)").matches) {
				event.preventDefault();
				event.stopPropagation();
				const isOpen = dropdown.classList.toggle("is-open");
				bellBtn.setAttribute("aria-expanded", String(isOpen));
			}
		});

		document.addEventListener("click", function (event) {
			if (!notificationContainer.contains(event.target)) {
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


<script>
	document.addEventListener("DOMContentLoaded", function () {
		var btn = document.getElementById("userMenuBtn");
		if (btn) {
			btn.addEventListener("mousedown", function (event) {
				if (window.mcForceCloseSearchOverlays) {
					window.mcForceCloseSearchOverlays();
				}
			}, true);
		}
	});
</script>


<script>
	/* Final user menu fix: details/summary works natively, this only closes overlays. */
	document.addEventListener("DOMContentLoaded", function () {
		var userDropdown = document.getElementById("userDropdown");
		var searchInput = document.getElementById("search-input");
		var searchResults = document.getElementById("search-results");
		var searchHistory = document.getElementById("searchHistoryDropdown");

		function closeSearchBox() {
			if (searchInput) searchInput.blur();
			if (searchResults) {
				searchResults.classList.remove("show", "is-open", "active");
				searchResults.style.display = "none";
				searchResults.style.pointerEvents = "none";
			}
			if (searchHistory) {
				searchHistory.classList.remove("show", "is-open", "active");
				searchHistory.style.display = "none";
				searchHistory.style.pointerEvents = "none";
			}
		}

		if (userDropdown) {
			userDropdown.addEventListener("toggle", function () {
				if (userDropdown.open) closeSearchBox();
			});

			document.addEventListener("click", function (event) {
				if (!userDropdown.contains(event.target)) {
					userDropdown.removeAttribute("open");
				}
			});

			document.addEventListener("keydown", function (event) {
				if (event.key === "Escape") {
					userDropdown.removeAttribute("open");
				}
			});
		}
	});
</script>



<script>
	/* =========================================================
       FINAL V4 - USER MENU CLICK CAPTURE
       Bắt click ở capture-phase để chặn mọi JS cũ đang preventDefault.
       ========================================================= */
	(function () {
		function byId(id) {
			return document.getElementById(id);
		}

		function closeSearchOverlays() {
			var searchInput = byId("search-input");
			var searchResults = byId("search-results");
			var searchHistory = byId("searchHistoryDropdown");

			if (searchInput) {
				searchInput.blur();
			}

			if (searchResults) {
				searchResults.classList.remove("show", "is-open", "active");
				searchResults.style.display = "none";
				searchResults.style.opacity = "0";
				searchResults.style.visibility = "hidden";
				searchResults.style.pointerEvents = "none";
			}

			if (searchHistory) {
				searchHistory.classList.remove("show", "is-open", "active");
				searchHistory.style.display = "none";
				searchHistory.style.opacity = "0";
				searchHistory.style.visibility = "hidden";
				searchHistory.style.pointerEvents = "none";
			}
		}

		function closeUserMenu() {
			var userDropdown = byId("userDropdown");
			var userBtn = byId("userMenuBtn");
			var userMenu = byId("userMenu");

			if (userDropdown) {
				userDropdown.classList.remove("is-open");
				userDropdown.removeAttribute("open");
			}

			if (userMenu) {
				userMenu.classList.remove("show");
				userMenu.style.display = "none";
			}

			if (userBtn) {
				userBtn.setAttribute("aria-expanded", "false");
			}
		}

		function openUserMenu() {
			var userDropdown = byId("userDropdown");
			var userBtn = byId("userMenuBtn");
			var userMenu = byId("userMenu");

			if (!userDropdown || !userBtn || !userMenu) {
				return;
			}

			closeSearchOverlays();

			userDropdown.classList.add("is-open");
			userDropdown.setAttribute("open", "open");
			userMenu.classList.add("show");
			userMenu.style.display = "flex";
			userBtn.setAttribute("aria-expanded", "true");
		}

		function toggleUserMenu(event) {
			var userDropdown = byId("userDropdown");
			var userMenu = byId("userMenu");

			if (!userDropdown || !userMenu) {
				return;
			}

			if (event) {
				event.preventDefault();
				event.stopPropagation();
				if (typeof event.stopImmediatePropagation === "function") {
					event.stopImmediatePropagation();
				}
			}

			var isOpen = userDropdown.classList.contains("is-open")
					|| userDropdown.hasAttribute("open")
					|| userMenu.classList.contains("show")
					|| userMenu.style.display === "flex";

			if (isOpen) {
				closeUserMenu();
			} else {
				openUserMenu();
			}
		}

		// Cho phép gọi trực tiếp từ console nếu cần test: mcToggleUserMenu()
		window.mcToggleUserMenu = toggleUserMenu;
		window.mcCloseUserMenu = closeUserMenu;
		window.mcForceCloseSearchOverlays = closeSearchOverlays;

		// Mousedown chỉ đóng search overlay, không chặn click.
		document.addEventListener("pointerdown", function (event) {
			var btn = event.target.closest && event.target.closest("#userMenuBtn");
			if (btn) {
				closeSearchOverlays();
			}
		}, true);

		// Click capture chạy trước mọi listener cũ ở button/main.js.
		document.addEventListener("click", function (event) {
			var btn = event.target.closest && event.target.closest("#userMenuBtn");
			var userDropdown = byId("userDropdown");

			if (btn && userDropdown && userDropdown.contains(btn)) {
				toggleUserMenu(event);
				return false;
			}

			if (userDropdown && !userDropdown.contains(event.target)) {
				closeUserMenu();
			}
		}, true);

		document.addEventListener("keydown", function (event) {
			if (event.key === "Escape") {
				closeUserMenu();
			}
		});
	})();
</script>


<script>
	/* =========================================================
       FINAL V5 - STICKY HEADER + CLOSE SEARCH OVERLAY ON SCROLL
       ========================================================= */
	(function () {
		var ticking = false;

		function closeSearchOverlaysV5() {
			var searchResults = document.getElementById("search-results");
			var searchHistory = document.getElementById("searchHistoryDropdown");

			if (searchResults) {
				searchResults.classList.remove("show", "is-open", "active");
				searchResults.style.display = "none";
				searchResults.style.opacity = "0";
				searchResults.style.visibility = "hidden";
				searchResults.style.pointerEvents = "none";
			}

			if (searchHistory) {
				searchHistory.classList.remove("show", "is-open", "active");
				searchHistory.style.display = "none";
				searchHistory.style.opacity = "0";
				searchHistory.style.visibility = "hidden";
				searchHistory.style.pointerEvents = "none";
			}
		}

		window.addEventListener("scroll", function () {
			if (!ticking) {
				window.requestAnimationFrame(function () {
					var searchInput = document.getElementById("search-input");
					var searchHistory = document.getElementById("searchHistoryDropdown");

					/*
					 * Không đóng lịch sử tìm kiếm khi trình duyệt tự scroll nhẹ để focus input.
					 * Đây là nguyên nhân làm màn hình bị giật và lần bấm đầu không thấy dropdown.
					 */
					if (!(searchInput && document.activeElement === searchInput && searchHistory && searchHistory.classList.contains("is-open"))) {
						closeSearchOverlaysV5();
					}

					ticking = false;
				});
				ticking = true;
			}
		}, { passive: true });
	})();
</script>


<script>
	/* Tự đo đúng chiều cao header fixed để nội dung bắt đầu ngay bên dưới, không cộng thêm khoảng trắng dư. */
	(function () {
		function syncStickyHeaderHeight() {
			var header = document.querySelector('.site-header');
			if (!header) return;

			var headerHeight = Math.ceil(header.getBoundingClientRect().height || 0);
			if (headerHeight > 0) {
				document.documentElement.style.setProperty('--mc-sticky-header-height', headerHeight + 'px');
			}
		}

		document.addEventListener('DOMContentLoaded', syncStickyHeaderHeight);
		window.addEventListener('load', syncStickyHeaderHeight);
		window.addEventListener('resize', syncStickyHeaderHeight);

		if (window.ResizeObserver) {
			document.addEventListener('DOMContentLoaded', function () {
				var header = document.querySelector('.site-header');
				if (!header) return;

				var observer = new ResizeObserver(syncStickyHeaderHeight);
				observer.observe(header);
			});
		}
	})();
</script>

</body>
</html>

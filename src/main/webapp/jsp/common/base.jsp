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

		<div class="header-actions" style="display: flex; align-items: center; gap: 20px;">

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
			   style="font-size: 24px; text-decoration: none; position: relative; margin-left: 10px;">
				🛒

				<c:if test="${not empty sessionScope.CART}">
					<c:set var="cartQty" value="0" />

					<c:forEach var="e" items="${sessionScope.CART}">
						<c:set var="cartQty" value="${cartQty + e.value.quantity}" />
					</c:forEach>

					<c:if test="${cartQty > 0}">
							<span class="cart-badge"
							      style="position: absolute; top: -8px; right: -12px; background: var(--pink-main); color: white; border-radius: 50%; padding: 2px 6px; font-size: 11px; font-weight: bold;">
									${cartQty}
							</span>
					</c:if>
				</c:if>
			</a>

			<div class="notification-container"
			     style="position: relative; margin-left: 15px; display: flex; align-items: center; justify-content: center; height: 100%;">

				<button id="notifBellBtn"
				        type="button"
				        style="background: none; border: none; cursor: pointer; position: relative; padding: 5px; display: flex; align-items: center; justify-content: center; font-size: 22px; transition: transform 0.2s ease; outline: none;"
				        onmouseover="this.style.transform='scale(1.15)'"
				        onmouseout="this.style.transform='scale(1)'">
					🔔

					<c:if test="${not empty requestScope.unreadCount && requestScope.unreadCount > 0}">
							<span class="notif-badge"
							      style="position: absolute; top: -1px; right: -4px; background: #ff5fa2; color: white; border-radius: 50%; padding: 1px 5px; font-size: 10px; font-weight: bold; min-width: 16px; text-align: center; border: 2px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.2); font-family: Arial, sans-serif;">
									${requestScope.unreadCount}
							</span>
					</c:if>
				</button>

				<div class="notif-dropdown"
				     id="notifDropdown"
				     style="display: none; position: absolute; right: 0; top: 45px; width: 340px; background: white; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.15); border: 1px solid #eaeaea; z-index: 9999; overflow: hidden;">

					<c:choose>
						<c:when test="${not empty sessionScope.user}">
							<div style="padding: 14px 16px; font-weight: bold; border-bottom: 1px solid #f0f0f0; background: #fff0f6; color: #ff5fa2; display: flex; justify-content: space-between; align-items: center;">
								<span style="font-size: 14px;">Thông báo mới nhận</span>

								<c:if test="${requestScope.unreadCount > 0}">
									<a href="${pageContext.request.contextPath}/notifications?action=readAll"
									   style="font-size: 12px; color: #666; text-decoration: none; font-weight: normal;">
										Đánh dấu đã đọc
									</a>
								</c:if>
							</div>

							<div style="max-height: 380px; overflow-y: auto; scroll-behavior: smooth;">
								<c:choose>
									<c:when test="${not empty requestScope.notifications}">
										<c:forEach var="notif" items="${requestScope.notifications}">
											<c:set var="bgDefault" value="${notif.read ? 'transparent' : '#fafafa'}" />

											<a href="${pageContext.request.contextPath}/notifications/read?id=${notif.id}&redirect=${notif.targetUrl}"
											   style="display: flex; padding: 12px 16px; text-decoration: none; border-bottom: 1px solid #f9f9f9; transition: background 0.2s; gap: 12px; background: ${bgDefault};"
											   onmouseover="this.style.background='#fff5f8'"
											   onmouseout="this.style.background='${bgDefault}'">

												<div style="font-size: 20px; margin-top: 2px;">
													<c:choose>
														<c:when test="${notif.type == 'VOUCHER'}">🎟️</c:when>
														<c:when test="${notif.type == 'EVENT'}">📢</c:when>
														<c:otherwise>✨</c:otherwise>
													</c:choose>
												</div>

												<div style="flex: 1;">
													<h4 style="margin: 0 0 4px 0; color: #222; font-size: 13.5px; font-weight: ${notif.read ? '500' : 'bold'}; line-height: 1.4;">
														<c:out value="${notif.title}" />
													</h4>

													<p style="margin: 0; color: #666; font-size: 12px; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
														<c:out value="${notif.message}" />
													</p>
												</div>
											</a>
										</c:forEach>
									</c:when>

									<c:otherwise>
										<div style="padding: 40px 20px; text-align: center; color: #999;">
											<div style="font-size: 32px; margin-bottom: 10px;">🔔</div>
											<p style="margin: 0; font-size: 13px;">Bạn chưa có thông báo nào mới.</p>
										</div>
									</c:otherwise>
								</c:choose>
							</div>

							<a href="${pageContext.request.contextPath}/notifications"
							   style="display: block; padding: 12px; text-align: center; font-weight: bold; text-decoration: none; color: #ff5fa2; border-top: 1px solid #f0f0f0; background: #fffafc; font-size: 13px;">
								Xem tất cả thông báo
							</a>
						</c:when>

						<c:otherwise>
							<div style="padding: 35px 24px; text-align: center; color: #555; background: #ffffff;">
								<div style="font-size: 40px; margin-bottom: 12px;">🔔</div>

								<h4 style="margin: 0 0 8px 0; color: #222; font-size: 15px; font-weight: bold;">
									Bạn có thông báo mới không?
								</h4>

								<p style="font-size: 12.5px; color: #777; margin: 0 0 20px 0; line-height: 1.5;">
									Đăng nhập ngay để xem các thông báo sự kiện khuyến mãi, quà tặng voucher và theo dõi hành trình đơn hàng nhé!
								</p>

								<a href="${pageContext.request.contextPath}/login"
								   style="display: inline-block; padding: 10px 24px; background: #ff5fa2; color: white; text-decoration: none; border-radius: 25px; font-size: 13px; font-weight: bold; min-width: 100px; box-shadow: 0 4px 12px rgba(255,95,162,0.3); transition: all 0.2s;">
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
			<div class="container" style="padding: 40px 0; color: #666; text-align: center;">
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

		if (bellBtn && dropdown) {
			bellBtn.addEventListener("click", function (e) {
				e.stopPropagation();
				dropdown.style.display = dropdown.style.display === "none" ? "block" : "none";
			});

			document.addEventListener("click", function (e) {
				if (!dropdown.contains(e.target) && !bellBtn.contains(e.target)) {
					dropdown.style.display = "none";
				}
			});
		}
	});
</script>

</body>
</html>

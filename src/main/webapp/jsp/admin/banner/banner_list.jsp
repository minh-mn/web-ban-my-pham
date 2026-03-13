<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="ADMIN | Banner" scope="request" />
<c:set var="activeMenu" value="banners" scope="request" />
<c:set var="pageCss" value="/assets/css/admin/admin-list.css"
	scope="request" />

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

<main class="admin-main">
	<div class="admin-container">

		<div class="admin-topbar">
			<div>
				<h1 class="admin-h1">Banner</h1>
				<p class="admin-subtext">Quản lý banner trang chủ.</p>
			</div>

			<a class="admin-btn admin-btn--primary"
				href="${pageContext.request.contextPath}/admin/banners?action=new">
				+ Thêm banner </a>
		</div>

		<div class="admin-card">
			<div class="admin-card__body">

				<c:choose>
					<c:when test="${empty banners}">
						<div class="admin-empty">Chưa có banner.</div>
					</c:when>

					<c:otherwise>
						<table class="admin-table">
							<thead>
								<tr>
									<th style="width: 90px;">ID</th>
									<th style="width: 220px;">Title</th>
									<th>Image</th>
									<th>Link</th>
									<th style="width: 140px;">Trạng thái</th>
									<th style="width: 260px;">Thao tác</th>
								</tr>
							</thead>

							<tbody>
								<c:forEach var="bn" items="${banners}">
									<tr>
										<td>#${bn.id}</td>

										<td><c:out value="${bn.title}" /></td>

										<td><c:choose>
												<c:when test="${not empty bn.imageUrl}">
													<div class="admin-media">
														<img class="admin-thumb"
															src="${pageContext.request.contextPath}${bn.imageUrl}"
															alt="banner">

														<div class="admin-media__meta">
															<span class="admin-chip">Path</span>
															<div class="admin-path">
																<c:out value="${bn.imageUrl}" />
															</div>
														</div>

													</div>
												</c:when>

												<c:otherwise>
													<span class="admin-pill admin-pill--danger">NO IMAGE</span>
												</c:otherwise>
											</c:choose></td>

										<!-- Link: nếu trống -> hiển thị — -->
										<td class="admin-break"><c:choose>
												<c:when test="${not empty bn.link}">
													<c:out value="${bn.link}" />
												</c:when>
												<c:otherwise>
													<span class="admin-muted">—</span>
												</c:otherwise>
											</c:choose></td>

										<td><c:choose>
												<c:when test="${bn.active}">
													<span class="admin-pill admin-pill--ok">ACTIVE</span>
												</c:when>
												<c:otherwise>
													<span class="admin-pill">INACTIVE</span>
												</c:otherwise>
											</c:choose></td>

										<td class="admin-actions"><a class="admin-btn"
											href="${pageContext.request.contextPath}/admin/banners?action=edit&id=${bn.id}">
												Sửa </a>

											<form method="post"
												action="${pageContext.request.contextPath}/admin/banners"
												class="admin-inline">

                        <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
                        <%@ include file="/jsp/common/csrf.jspf" %>

												<input type="hidden" name="action" value="toggle">
                        <input type="hidden" name="id" value="${bn.id}">
												<button class="admin-btn" type="submit">Bật/Tắt</button>
											</form>

											<form method="post"
												action="${pageContext.request.contextPath}/admin/banners"
												class="admin-inline"
												onsubmit="return confirm('Xóa banner này?')">

                        <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
                        <%@ include file="/jsp/common/csrf.jspf" %>

												<input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${bn.id}">
												<button class="admin-btn admin-btn--danger" type="submit">Xóa</button>
											</form></td>

									</tr>
								</c:forEach>
							</tbody>
						</table>
					</c:otherwise>
				</c:choose>

			</div>
		</div>

	</div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp" />

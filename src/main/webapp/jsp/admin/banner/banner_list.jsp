<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Banner" scope="request" />
<c:set var="activeMenu" value="banners" scope="request" />
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request" />

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<main class="admin-main">
	<div class="admin-container">

		<div class="admin-topbar">
			<div>
				<h1 class="admin-h1">Quản lý Banner</h1>
				<p class="admin-subtext">
					Quản lý ảnh banner hiển thị trên trang chủ, liên kết điều hướng và trạng thái hoạt động.
				</p>
			</div>

			<a class="admin-btn admin-btn--primary" href="${ctx}/admin/banners?action=new">
				+ Thêm banner
			</a>
		</div>

		<div class="admin-card">
			<div class="admin-card__body">

				<div class="admin-row">
					<div>
						<h2 class="admin-h2">Danh sách banner</h2>
						<p class="admin-subtext">
							Tổng số:
							<strong>
								<c:choose>
									<c:when test="${empty banners}">0</c:when>
									<c:otherwise>${fn:length(banners)}</c:otherwise>
								</c:choose>
							</strong>
							banner
						</p>
					</div>

					<span class="admin-chip">Homepage Banner</span>
				</div>

				<hr class="admin-divider" />

				<c:choose>
					<c:when test="${empty banners}">
						<div class="admin-empty">
							Chưa có banner nào. Hãy thêm banner đầu tiên để hiển thị trên trang chủ.
						</div>
					</c:when>

					<c:otherwise>
						<table class="admin-table">
							<thead>
							<tr>
								<th style="width: 80px;">ID</th>
								<th style="width: 220px;">Tiêu đề</th>
								<th>Ảnh banner</th>
								<th>Liên kết</th>
								<th style="width: 140px;">Trạng thái</th>
								<th style="width: 240px;">Thao tác</th>
							</tr>
							</thead>

							<tbody>
							<c:forEach var="bn" items="${banners}">
								<tr>
									<td>
										<strong>#${bn.id}</strong>
									</td>

									<td>
										<c:choose>
											<c:when test="${not empty bn.title}">
												<strong><c:out value="${bn.title}" /></strong>
											</c:when>
											<c:otherwise>
												<span class="admin-muted">Chưa có tiêu đề</span>
											</c:otherwise>
										</c:choose>
									</td>

									<td>
										<c:choose>
											<c:when test="${not empty bn.imageUrl}">
												<div class="admin-media">
													<img
															class="admin-thumb"
															src="${ctx}${bn.imageUrl}"
															alt="Banner">

													<div class="admin-media__meta">
														<span class="admin-chip">Image path</span>
														<div class="admin-path">
															<c:out value="${bn.imageUrl}" />
														</div>
													</div>
												</div>
											</c:when>

											<c:otherwise>
												<span class="admin-pill admin-pill--danger">NO IMAGE</span>
											</c:otherwise>
										</c:choose>
									</td>

									<td class="admin-break">
										<c:choose>
											<c:when test="${not empty bn.link}">
												<c:out value="${bn.link}" />
											</c:when>
											<c:otherwise>
												<span class="admin-muted">—</span>
											</c:otherwise>
										</c:choose>
									</td>

									<td class="admin-status-cell">
										<c:choose>
											<c:when test="${bn.active}">
												<span class="admin-pill admin-pill--ok">ACTIVE</span>
											</c:when>
											<c:otherwise>
												<span class="admin-pill">INACTIVE</span>
											</c:otherwise>
										</c:choose>
									</td>

									<td>
										<div class="admin-actions">
											<a
													class="admin-btn"
													href="${ctx}/admin/banners?action=edit&id=${bn.id}">
												Sửa
											</a>

											<form
													method="post"
													action="${ctx}/admin/banners"
													class="admin-inline">

												<%@ include file="/jsp/common/csrf.jspf" %>

												<input type="hidden" name="action" value="toggle">
												<input type="hidden" name="id" value="${bn.id}">
												<button class="admin-btn" type="submit">Bật/Tắt</button>
											</form>

											<form
													method="post"
													action="${ctx}/admin/banners"
													class="admin-inline"
													onsubmit="return confirm('Bạn có chắc muốn xóa banner này không?')">

												<%@ include file="/jsp/common/csrf.jspf" %>

												<input type="hidden" name="action" value="delete">
												<input type="hidden" name="id" value="${bn.id}">
												<button class="admin-btn admin-btn--danger" type="submit">Xóa</button>
											</form>
										</div>
									</td>
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
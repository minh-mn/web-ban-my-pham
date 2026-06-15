<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Quản lý Banner" scope="request" />
<c:set var="activeMenu" value="banners" scope="request" />
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request" />

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="bannerTotal" value="${empty banners ? 0 : fn:length(banners)}" />
<c:set var="bannerActiveCount" value="0" />
<c:set var="bannerInactiveCount" value="0" />

<c:forEach var="bnStat" items="${banners}">
	<c:choose>
		<c:when test="${bnStat.active}">
			<c:set var="bannerActiveCount" value="${bannerActiveCount + 1}" />
		</c:when>
		<c:otherwise>
			<c:set var="bannerInactiveCount" value="${bannerInactiveCount + 1}" />
		</c:otherwise>
	</c:choose>
</c:forEach>

<main class="admin-main">
	<div class="admin-container admin-banner-page">

		<section class="admin-banner-hero">
			<div class="admin-banner-hero__content">
				<span class="admin-banner-eyebrow">TRANG CHỦ &amp; HIỂN THỊ</span>
				<h1 class="admin-banner-title">Quản lý banner</h1>
				<p class="admin-banner-subtitle">
					Quản lý ảnh banner hiển thị trên trang chủ, liên kết điều hướng và trạng thái hoạt động.
					Banner nên dùng đúng tỉ lệ để giao diện trang chủ không bị vỡ bố cục.
				</p>
			</div>

			<div class="admin-banner-hero__actions">
				<a class="admin-btn admin-btn--primary" href="${ctx}/admin/banners?action=new">
					+ Thêm banner
				</a>
			</div>
		</section>

		<section class="admin-banner-summary">
			<div class="admin-banner-stat admin-banner-stat--total">
				<span class="admin-banner-stat__icon">🖼️</span>
				<span class="admin-banner-stat__label">Tổng banner</span>
				<strong class="admin-banner-stat__value">
					<c:out value="${bannerTotal}" />
				</strong>
				<span class="admin-banner-stat__note">Tất cả banner trong hệ thống</span>
			</div>

			<div class="admin-banner-stat admin-banner-stat--active">
				<span class="admin-banner-stat__icon">✅</span>
				<span class="admin-banner-stat__label">Đang hiển thị</span>
				<strong class="admin-banner-stat__value">
					<c:out value="${bannerActiveCount}" />
				</strong>
				<span class="admin-banner-stat__note">Banner active trên trang chủ</span>
			</div>

			<div class="admin-banner-stat admin-banner-stat--inactive">
				<span class="admin-banner-stat__icon">⏸️</span>
				<span class="admin-banner-stat__label">Tạm ẩn</span>
				<strong class="admin-banner-stat__value">
					<c:out value="${bannerInactiveCount}" />
				</strong>
				<span class="admin-banner-stat__note">Banner chưa hiển thị</span>
			</div>

			<div class="admin-banner-stat admin-banner-stat--path">
				<span class="admin-banner-stat__icon">📁</span>
				<span class="admin-banner-stat__label">Thư mục upload</span>
				<strong class="admin-banner-stat__value admin-banner-stat__value--text">banner</strong>
				<span class="admin-banner-stat__note">/uploads/banner/</span>
			</div>
		</section>

		<section class="admin-card admin-banner-list-card">
			<div class="admin-card__body">
				<div class="admin-banner-section-head">
					<div>
						<h2 class="admin-banner-section-title">Danh sách banner</h2>
						<p class="admin-banner-section-desc">
							Kiểm tra ảnh, tiêu đề, đường dẫn điều hướng và trạng thái hiển thị của từng banner.
						</p>
					</div>

					<span class="admin-chip admin-chip--brand">
						<c:out value="${bannerTotal}" /> banner
					</span>
				</div>

				<c:choose>
					<c:when test="${empty banners}">
						<div class="admin-banner-empty">
							<div class="admin-banner-empty__icon">🖼️</div>
							<div>
								<h3>Chưa có banner nào</h3>
								<p>Hãy thêm banner đầu tiên để hiển thị nội dung nổi bật trên trang chủ.</p>
								<a class="admin-btn admin-btn--primary" href="${ctx}/admin/banners?action=new">
									+ Thêm banner
								</a>
							</div>
						</div>
					</c:when>

					<c:otherwise>
						<div class="admin-banner-table-wrap">
							<table class="admin-table admin-banner-table">
								<thead>
								<tr>
									<th class="admin-banner-col-id">ID</th>
									<th class="admin-banner-col-title">Tiêu đề</th>
									<th class="admin-banner-col-image">Ảnh banner</th>
									<th class="admin-banner-col-link">Liên kết</th>
									<th class="admin-banner-col-status">Trạng thái</th>
									<th class="admin-banner-col-actions">Thao tác</th>
								</tr>
								</thead>

								<tbody>
								<c:forEach var="bn" items="${banners}">
									<tr class="${bn.active ? 'admin-banner-row--active' : 'admin-banner-row--inactive'}">
										<td class="admin-banner-id-cell">
											<strong>#<c:out value="${bn.id}" /></strong>
										</td>

										<td>
											<div class="admin-banner-title-cell">
												<c:choose>
													<c:when test="${not empty bn.title}">
														<strong><c:out value="${bn.title}" /></strong>
													</c:when>
													<c:otherwise>
														<strong class="admin-muted">Chưa có tiêu đề</strong>
													</c:otherwise>
												</c:choose>
												<span>Banner trang chủ</span>
											</div>
										</td>

										<td>
											<c:choose>
												<c:when test="${not empty bn.imageUrl}">
													<div class="admin-banner-media">
														<img
																class="admin-banner-thumb"
																src="${ctx}${bn.imageUrl}"
																alt="${not empty bn.title ? bn.title : 'Banner'}">

														<div class="admin-banner-media__meta">
															<span class="admin-chip">Đường dẫn ảnh</span>
															<div class="admin-path admin-banner-path">
																<c:out value="${bn.imageUrl}" />
															</div>
														</div>
													</div>
												</c:when>

												<c:otherwise>
													<span class="admin-pill admin-pill--danger">Chưa có ảnh</span>
												</c:otherwise>
											</c:choose>
										</td>

										<td class="admin-banner-link-cell">
											<c:choose>
												<c:when test="${not empty bn.link}">
													<span class="admin-banner-link">
														<c:out value="${bn.link}" />
													</span>
												</c:when>
												<c:otherwise>
													<span class="admin-muted">Không gắn liên kết</span>
												</c:otherwise>
											</c:choose>
										</td>

										<td class="admin-banner-status-cell">
											<c:choose>
												<c:when test="${bn.active}">
													<span class="admin-pill admin-pill--ok">Đang hiển thị</span>
												</c:when>
												<c:otherwise>
													<span class="admin-pill admin-pill--warning">Tạm ẩn</span>
												</c:otherwise>
											</c:choose>
										</td>

										<td class="admin-banner-action-cell">
											<div class="admin-banner-actions">
												<a
														class="admin-btn admin-banner-action-btn"
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
													<button class="admin-btn admin-banner-action-btn" type="submit">
														<c:choose>
															<c:when test="${bn.active}">Ẩn</c:when>
															<c:otherwise>Hiển thị</c:otherwise>
														</c:choose>
													</button>
												</form>

												<form
														method="post"
														action="${ctx}/admin/banners"
														class="admin-inline"
														onsubmit="return confirm('Bạn có chắc muốn xóa banner này không?')">

													<%@ include file="/jsp/common/csrf.jspf" %>

													<input type="hidden" name="action" value="delete">
													<input type="hidden" name="id" value="${bn.id}">
													<button class="admin-btn admin-btn--danger admin-banner-action-btn" type="submit">
														Xóa
													</button>
												</form>
											</div>
										</td>
									</tr>
								</c:forEach>
								</tbody>
							</table>
						</div>
					</c:otherwise>
				</c:choose>
			</div>
		</section>

	</div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp" />

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Order Detail" scope="request" />
<c:set var="activeMenu" value="orders" scope="request" />
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request" />

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

<style>
	.order-detail-grid {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 18px;
	}

	.order-detail-full {
		grid-column: 1 / -1;
	}

	.order-status-row {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 10px;
		margin-top: 8px;
	}

	.admin-alert {
		margin: 0 0 16px;
		padding: 12px 14px;
		border-radius: 12px;
		font-weight: 700;
		line-height: 1.45;
	}

	.admin-alert--success {
		border: 1px solid #bbf7d0;
		background: #f0fdf4;
		color: #166534;
	}

	.admin-alert--error {
		border: 1px solid #fecaca;
		background: #fff1f2;
		color: #b91c1c;
	}

	.admin-section-card {
		margin-top: 22px;
		padding: 18px;
		border: 1px solid #edf0f5;
		border-radius: 18px;
		background: #ffffff;
	}

	.admin-section-title {
		margin: 0 0 14px;
		color: #1f2a44;
		font-size: 18px;
		font-weight: 900;
	}

	.admin-subsection-title {
		margin: 0 0 10px;
		color: #334155;
		font-size: 15px;
		font-weight: 900;
	}

	.shipping-summary {
		display: grid;
		grid-template-columns: repeat(4, minmax(0, 1fr));
		gap: 14px;
		margin-bottom: 18px;
	}

	.shipping-summary-item {
		padding: 14px;
		border: 1px solid #eef2f7;
		border-radius: 14px;
		background: #f8fafc;
	}

	.shipping-summary-item span {
		display: block;
		margin-bottom: 5px;
		color: #64748b;
		font-size: 12px;
		font-weight: 800;
		text-transform: uppercase;
		letter-spacing: 0.04em;
	}

	.shipping-summary-item strong {
		color: #1f2a44;
		font-size: 14px;
		font-weight: 900;
	}

	.tracking-steps {
		display: grid;
		grid-template-columns: repeat(4, minmax(0, 1fr));
		gap: 10px;
		margin: 16px 0 18px;
	}

	.tracking-step {
		position: relative;
		min-height: 86px;
		padding: 14px 12px;
		border: 1px solid #e5e7eb;
		border-radius: 16px;
		background: #f8fafc;
		color: #64748b;
	}

	.tracking-step::before {
		content: "";
		width: 28px;
		height: 28px;
		display: inline-flex;
		margin-bottom: 8px;
		border-radius: 50%;
		background: #e5e7eb;
	}

	.tracking-step strong {
		display: block;
		color: inherit;
		font-size: 13.5px;
		line-height: 1.35;
	}

	.tracking-step small {
		display: block;
		margin-top: 4px;
		color: inherit;
		font-size: 12px;
		line-height: 1.35;
	}

	.tracking-step.is-active {
		border-color: #93c5fd;
		background: #eff6ff;
		color: #1d4ed8;
	}

	.tracking-step.is-active::before {
		background: #3b82f6;
		box-shadow: 0 0 0 5px rgba(59, 130, 246, 0.14);
	}

	.tracking-step.is-done {
		border-color: #bbf7d0;
		background: #f0fdf4;
		color: #166534;
	}

	.tracking-step.is-done::before {
		background: #22c55e;
		box-shadow: 0 0 0 5px rgba(34, 197, 94, 0.13);
	}

	.tracking-step.is-failed {
		border-color: #fecaca;
		background: #fff1f2;
		color: #b91c1c;
	}

	.tracking-step.is-failed::before {
		background: #ef4444;
		box-shadow: 0 0 0 5px rgba(239, 68, 68, 0.13);
	}

	.tracking-history {
		display: flex;
		flex-direction: column;
		gap: 10px;
	}

	.tracking-history-item {
		display: grid;
		grid-template-columns: 170px 1fr;
		gap: 14px;
		padding: 12px 14px;
		border: 1px solid #eef2f7;
		border-radius: 14px;
		background: #ffffff;
	}

	.tracking-history-time {
		color: #64748b;
		font-size: 13px;
		font-weight: 800;
	}

	.tracking-history-content strong {
		display: block;
		margin-bottom: 4px;
		color: #1f2a44;
		font-size: 14px;
	}

	.tracking-history-content p {
		margin: 0;
		color: #475569;
		font-size: 13.5px;
		line-height: 1.45;
	}

	.admin-pill--info {
		color: #1d4ed8;
		background: #eff6ff;
		border: 1px solid #bfdbfe;
	}

	.admin-pill--warning {
		color: #b45309;
		background: #fffbeb;
		border: 1px solid #fde68a;
	}

	.admin-pill--danger {
		color: #b91c1c;
		background: #fff1f2;
		border: 1px solid #fecaca;
	}

	.admin-pill--ok {
		color: #166534;
		background: #f0fdf4;
		border: 1px solid #bbf7d0;
	}

	.admin-pill--muted {
		color: #475569;
		background: #f8fafc;
		border: 1px solid #e2e8f0;
	}

	.form-grid-2 {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 14px;
	}

	.admin-textarea {
		width: 100%;
		min-height: 86px;
		padding: 12px 14px;
		border: 1px solid #dbe3ef;
		border-radius: 12px;
		resize: vertical;
		font-family: inherit;
		font-size: 14px;
		line-height: 1.45;
	}

	.admin-textarea:focus {
		outline: none;
		border-color: #d63384;
		box-shadow: 0 0 0 4px rgba(214, 51, 132, 0.10);
	}

	@media (max-width: 900px) {
		.order-detail-grid,
		.shipping-summary,
		.tracking-steps,
		.form-grid-2 {
			grid-template-columns: 1fr;
		}

		.tracking-history-item {
			grid-template-columns: 1fr;
		}
	}
</style>

<main class="admin-main">
	<div class="admin-container">

		<div class="admin-topbar">
			<div>
				<h1 class="admin-h1">Chi tiết đơn hàng #${order.id}</h1>

				<div class="order-status-row">
					<span class="admin-muted">Trạng thái đơn:</span>

					<c:choose>
						<c:when test="${order.status == 'completed'}">
              <span class="admin-pill admin-pill--ok">
                <c:out value="${order.statusLabel}" />
              </span>
						</c:when>

						<c:when test="${order.status == 'cancelled' || order.status == 'canceled'}">
              <span class="admin-pill admin-pill--danger">
                <c:out value="${order.statusLabel}" />
              </span>
						</c:when>

						<c:when test="${order.status == 'shipping'}">
              <span class="admin-pill admin-pill--info">
                <c:out value="${order.statusLabel}" />
              </span>
						</c:when>

						<c:otherwise>
              <span class="admin-pill admin-pill--muted">
                <c:out value="${order.statusLabel}" />
              </span>
						</c:otherwise>
					</c:choose>

					<span class="admin-muted">Vận chuyển:</span>

					<c:choose>
						<c:when test="${order.delivered}">
              <span class="admin-pill admin-pill--ok">
                <c:out value="${order.shippingStatusLabel}" />
              </span>
						</c:when>

						<c:when test="${order.deliveryFailed || order.shippingCanceled}">
              <span class="admin-pill admin-pill--danger">
                <c:out value="${order.shippingStatusLabel}" />
              </span>
						</c:when>

						<c:when test="${order.delivering}">
              <span class="admin-pill admin-pill--info">
                <c:out value="${order.shippingStatusLabel}" />
              </span>
						</c:when>

						<c:otherwise>
              <span class="admin-pill admin-pill--warning">
                <c:out value="${order.shippingStatusLabel}" />
              </span>
						</c:otherwise>
					</c:choose>
				</div>
			</div>

			<a class="admin-btn"
			   href="${pageContext.request.contextPath}/admin/orders">
				Quay lại
			</a>
		</div>

		<c:if test="${not empty admin_order_success}">
			<div class="admin-alert admin-alert--success">
				<c:out value="${admin_order_success}" />
			</div>
		</c:if>

		<c:if test="${not empty admin_order_error}">
			<div class="admin-alert admin-alert--error">
				<c:out value="${admin_order_error}" />
			</div>
		</c:if>

		<div class="admin-card">
			<div class="admin-card__body">

				<!-- ================= ORDER BASIC INFO ================= -->
				<div class="admin-section-card" style="margin-top: 0;">
					<h2 class="admin-section-title">Thông tin đơn hàng</h2>

					<div class="order-detail-grid">

						<div class="admin-field">
							<div class="admin-label">Khách hàng</div>
							<div>
								<c:out value="${order.fullName}" />
							</div>
						</div>

						<div class="admin-field">
							<div class="admin-label">Số điện thoại</div>
							<div>
								<c:out value="${order.phone}" />
							</div>
						</div>

						<div class="admin-field order-detail-full">
							<div class="admin-label">Địa chỉ giao hàng</div>
							<div>
								<c:out value="${order.address}" />
							</div>
						</div>

						<div class="admin-field">
							<div class="admin-label">Tổng tiền</div>
							<div>
								<strong>
									<fmt:formatNumber value="${order.total}"
													  type="number"
													  groupingUsed="true"
													  minFractionDigits="0"
													  maxFractionDigits="0" /> ₫
								</strong>
							</div>
						</div>

						<div class="admin-field">
							<div class="admin-label">Giảm giá</div>
							<div>
								<strong>
									<fmt:formatNumber value="${order.couponDiscount}"
													  type="number"
													  groupingUsed="true"
													  minFractionDigits="0"
													  maxFractionDigits="0" /> ₫
								</strong>
							</div>
						</div>

						<div class="admin-field">
							<div class="admin-label">Ngày tạo</div>
							<div>
								<fmt:formatDate value="${order.createdAtDate}"
												pattern="dd/MM/yyyy HH:mm" />
							</div>
						</div>

						<div class="admin-field">
							<div class="admin-label">Thanh toán</div>
							<div>
								<c:out value="${order.paymentMethod}" />
								-
								<c:choose>
									<c:when test="${order.paymentStatus == 'PAID'}">
										<span class="admin-pill admin-pill--ok">PAID</span>
									</c:when>

									<c:when test="${order.paymentStatus == 'CANCELED' || order.paymentStatus == 'FAILED'}">
                    <span class="admin-pill admin-pill--danger">
                      <c:out value="${order.paymentStatus}" />
                    </span>
									</c:when>

									<c:otherwise>
                    <span class="admin-pill admin-pill--warning">
                      <c:out value="${order.paymentStatus}" />
                    </span>
									</c:otherwise>
								</c:choose>
							</div>
						</div>

						<div class="admin-field">
							<div class="admin-label">VNPAY TxnRef</div>
							<div class="admin-muted">
								<c:choose>
									<c:when test="${not empty order.vnpTxnRef}">
										<c:out value="${order.vnpTxnRef}" />
									</c:when>
									<c:otherwise>Không có</c:otherwise>
								</c:choose>
							</div>
						</div>

					</div>
				</div>

				<!-- ================= SHIPPING TRACKING ================= -->
				<div class="admin-section-card">
					<h2 class="admin-section-title">Tracking vận chuyển</h2>

					<div class="shipping-summary">
						<div class="shipping-summary-item">
							<span>Phương thức</span>
							<strong>
								<c:out value="${order.shippingMethodLabel}" />
							</strong>
						</div>

						<div class="shipping-summary-item">
							<span>Đơn vị giao</span>
							<strong>
								<c:out value="${order.shippingProviderLabel}" />
							</strong>
						</div>

						<div class="shipping-summary-item">
							<span>Mã vận đơn</span>
							<strong>
								<c:choose>
									<c:when test="${not empty order.shippingCode}">
										<c:out value="${order.shippingCode}" />
									</c:when>
									<c:otherwise>Chưa có</c:otherwise>
								</c:choose>
							</strong>
						</div>

						<div class="shipping-summary-item">
							<span>Phí ship</span>
							<strong>
								<fmt:formatNumber value="${order.shippingFee}"
												  type="number"
												  groupingUsed="true"
												  minFractionDigits="0"
												  maxFractionDigits="0" /> ₫
							</strong>
						</div>
					</div>

					<div class="tracking-steps">
						<div class="tracking-step ${order.pendingPickup ? 'is-active' : (order.delivering || order.delivered || order.deliveryFailed ? 'is-done' : '')}">
							<strong>Chờ lấy hàng</strong>
							<small>Shop chuẩn bị và bàn giao cho đơn vị vận chuyển.</small>
						</div>

						<div class="tracking-step ${order.delivering ? 'is-active' : (order.delivered || order.deliveryFailed ? 'is-done' : '')}">
							<strong>Đang giao</strong>
							<small>
								<c:choose>
									<c:when test="${not empty order.shippedAtDate}">
										Bắt đầu:
										<fmt:formatDate value="${order.shippedAtDate}"
														pattern="dd/MM/yyyy HH:mm" />
									</c:when>
									<c:otherwise>Đơn hàng đang trên đường giao đến khách.</c:otherwise>
								</c:choose>
							</small>
						</div>

						<c:choose>
							<c:when test="${order.deliveryFailed}">
								<div class="tracking-step is-failed">
									<strong>Giao thất bại</strong>
									<small>Shop cần liên hệ lại khách hàng để xử lý.</small>
								</div>
							</c:when>

							<c:otherwise>
								<div class="tracking-step ${order.delivered ? 'is-done' : ''}">
									<strong>Giao thành công</strong>
									<small>
										<c:choose>
											<c:when test="${not empty order.deliveredAtDate}">
												Hoàn tất:
												<fmt:formatDate value="${order.deliveredAtDate}"
																pattern="dd/MM/yyyy HH:mm" />
											</c:when>
											<c:otherwise>Chờ đơn hàng được giao thành công.</c:otherwise>
										</c:choose>
									</small>
								</div>
							</c:otherwise>
						</c:choose>

						<div class="tracking-step ${order.shippingCanceled ? 'is-failed' : ''}">
							<strong>Hủy vận chuyển</strong>
							<small>Chỉ dùng khi vận chuyển đã bị hủy.</small>
						</div>
					</div>

					<!-- UPDATE SHIPPING STATUS -->
					<form method="post"
						  action="${pageContext.request.contextPath}/admin/orders"
						  class="admin-form">

						<%@ include file="/jsp/common/csrf.jspf" %>

						<input type="hidden" name="action" value="updateShippingStatus" />
						<input type="hidden" name="id" value="${order.id}" />

						<div class="form-grid-2">
							<div class="admin-field">
								<div class="admin-label">Cập nhật trạng thái vận chuyển</div>

								<select name="shippingStatus" class="admin-select">
									<option value="PENDING_PICKUP"
									${order.shippingStatus == 'PENDING_PICKUP' ? 'selected' : ''}>
										Chờ lấy hàng
									</option>

									<option value="DELIVERING"
									${order.shippingStatus == 'DELIVERING' ? 'selected' : ''}>
										Đang giao
									</option>

									<option value="DELIVERED"
									${order.shippingStatus == 'DELIVERED' ? 'selected' : ''}>
										Giao thành công
									</option>

									<option value="FAILED"
									${order.shippingStatus == 'FAILED' ? 'selected' : ''}>
										Giao thất bại
									</option>

									<option value="CANCELED"
									${order.shippingStatus == 'CANCELED' ? 'selected' : ''}>
										Đã hủy vận chuyển
									</option>
								</select>

								<div class="admin-help">
									Khi chọn “Đang giao”, hệ thống tự cập nhật thời gian gửi hàng.
									Khi chọn “Giao thành công”, hệ thống tự cập nhật thời gian giao thành công.
								</div>
							</div>

							<div class="admin-field">
								<div class="admin-label">Ghi chú tracking</div>

								<textarea name="trackingNote"
										  class="admin-textarea"
										  placeholder="Ví dụ: Shipper đã lấy hàng, giao thất bại do khách không nghe máy..."></textarea>
							</div>
						</div>

						<div class="admin-actions">
							<button type="submit" class="admin-btn admin-btn--primary">
								Cập nhật vận chuyển
							</button>
						</div>
					</form>

					<hr class="admin-divider" />

					<!-- UPDATE SHIPPING INFO -->
					<h3 class="admin-subsection-title">Thông tin vận chuyển</h3>

					<form method="post"
						  action="${pageContext.request.contextPath}/admin/orders"
						  class="admin-form">

						<%@ include file="/jsp/common/csrf.jspf" %>

						<input type="hidden" name="action" value="updateShippingInfo" />
						<input type="hidden" name="id" value="${order.id}" />

						<div class="form-grid-2">
							<div class="admin-field">
								<div class="admin-label">Đơn vị vận chuyển</div>

								<select name="shippingProvider" class="admin-select">
									<option value="INTERNAL" ${order.shippingProvider == 'INTERNAL' ? 'selected' : ''}>
										Vận chuyển nội bộ
									</option>
									<option value="GHTK" ${order.shippingProvider == 'GHTK' ? 'selected' : ''}>
										Giao hàng tiết kiệm
									</option>
									<option value="GHN" ${order.shippingProvider == 'GHN' ? 'selected' : ''}>
										Giao hàng nhanh
									</option>
									<option value="VIETTEL_POST" ${order.shippingProvider == 'VIETTEL_POST' ? 'selected' : ''}>
										Viettel Post
									</option>
									<option value="OTHER" ${order.shippingProvider == 'OTHER' ? 'selected' : ''}>
										Khác
									</option>
								</select>
							</div>

							<div class="admin-field">
								<div class="admin-label">Phương thức giao hàng</div>

								<select name="shippingMethod" class="admin-select">
									<option value="ECONOMY" ${order.shippingMethod == 'ECONOMY' ? 'selected' : ''}>
										Giao hàng tiết kiệm
									</option>
									<option value="FAST" ${order.shippingMethod == 'FAST' ? 'selected' : ''}>
										Giao hàng nhanh
									</option>
									<option value="EXPRESS" ${order.shippingMethod == 'EXPRESS' ? 'selected' : ''}>
										Hỏa tốc
									</option>
								</select>
							</div>

							<div class="admin-field">
								<div class="admin-label">Mã vận đơn</div>

								<input type="text"
									   name="shippingCode"
									   class="admin-input"
									   value="${order.shippingCode}"
									   placeholder="Ví dụ: MC-SHIP-000123">
							</div>

							<div class="admin-field">
								<div class="admin-label">Phí vận chuyển</div>

								<input type="text"
									   name="shippingFee"
									   class="admin-input"
									   value="${order.shippingFee}"
									   placeholder="Ví dụ: 35000">
							</div>
						</div>

						<div class="admin-actions">
							<button type="submit" class="admin-btn admin-btn--primary">
								Lưu thông tin vận chuyển
							</button>
						</div>
					</form>

					<hr class="admin-divider" />

					<!-- TRACKING HISTORY -->
					<h3 class="admin-subsection-title">Lịch sử tracking</h3>

					<c:choose>
						<c:when test="${empty trackingList}">
							<div class="admin-empty">
								Chưa có lịch sử tracking cho đơn hàng này.
							</div>
						</c:when>

						<c:otherwise>
							<div class="tracking-history">
								<c:forEach var="tracking" items="${trackingList}">
									<div class="tracking-history-item">
										<div class="tracking-history-time">
											<c:choose>
												<c:when test="${not empty tracking.createdAt}">
													<c:out value="${tracking.createdAt}" />
												</c:when>
												<c:otherwise>Không rõ thời gian</c:otherwise>
											</c:choose>
										</div>

										<div class="tracking-history-content">
											<strong>
												<c:out value="${tracking.shippingStatusLabel}" />
											</strong>
											<p>
												<c:out value="${tracking.note}" />
											</p>
										</div>
									</div>
								</c:forEach>
							</div>
						</c:otherwise>
					</c:choose>
				</div>

				<!-- ================= ORDER ITEMS ================= -->
				<div class="admin-section-card">
					<h2 class="admin-section-title">Sản phẩm trong đơn hàng</h2>

					<c:set var="displayItems" value="${orderItems}" />

					<c:if test="${empty displayItems && not empty items}">
						<c:set var="displayItems" value="${items}" />
					</c:if>

					<c:choose>
						<c:when test="${empty displayItems}">
							<div class="admin-empty">
								Đơn hàng chưa có sản phẩm hoặc chưa load được chi tiết sản phẩm.
							</div>
						</c:when>

						<c:otherwise>
							<div style="overflow-x: auto;">
								<table class="admin-table" style="width: 100%; border-collapse: collapse;">
									<thead>
									<tr>
										<th style="width: 70px;">Ảnh</th>
										<th>Sản phẩm</th>
										<th style="width: 190px;">Biến thể</th>
										<th style="width: 130px;">Đơn giá</th>
										<th style="width: 90px;">SL</th>
										<th style="width: 150px;">Thành tiền</th>
									</tr>
									</thead>

									<tbody>
									<c:forEach var="item" items="${displayItems}">
										<tr>
											<td>
												<c:choose>
													<c:when test="${not empty item.imageUrl}">
														<img src="${pageContext.request.contextPath}${item.imageUrl}"
															 alt="${item.productName}"
															 style="width: 54px; height: 54px; object-fit: cover; border-radius: 10px;" />
													</c:when>

													<c:otherwise>
														<div style="width: 54px; height: 54px; border-radius: 10px; background: #f1f1f1; display: flex; align-items: center; justify-content: center;"
															 class="admin-muted">
															—
														</div>
													</c:otherwise>
												</c:choose>
											</td>

											<td>
												<strong>
													<c:out value="${item.productName}" />
												</strong>

												<div class="admin-muted" style="margin-top: 4px;">
													Product ID:
													<c:out value="${item.productId}" />
												</div>
											</td>

											<td>
												<c:choose>
													<c:when test="${not empty item.variantId
                                      || not empty item.variantName
                                      || not empty item.variantSize
                                      || not empty item.variantType}">
														<div>
															<span class="admin-pill">Variant</span>
														</div>

														<div style="margin-top: 6px;">
															<c:if test="${not empty item.variantName}">
																<div>
																	<strong>Tên:</strong>
																	<c:out value="${item.variantName}" />
																</div>
															</c:if>

															<c:if test="${not empty item.variantSize}">
																<div>
																	<strong>Size:</strong>
																	<c:out value="${item.variantSize}" />
																</div>
															</c:if>

															<c:if test="${not empty item.variantType}">
																<div>
																	<strong>Loại:</strong>
																	<c:out value="${item.variantType}" />
																</div>
															</c:if>

															<c:if test="${not empty item.variantId}">
																<div class="admin-muted">
																	Variant ID:
																	<c:out value="${item.variantId}" />
																</div>
															</c:if>
														</div>
													</c:when>

													<c:otherwise>
														<span class="admin-muted">Không có biến thể</span>
													</c:otherwise>
												</c:choose>
											</td>

											<td>
												<fmt:formatNumber value="${item.price}"
																  type="number"
																  groupingUsed="true"
																  minFractionDigits="0"
																  maxFractionDigits="0" />
												₫
											</td>

											<td>
												<c:out value="${item.quantity}" />
											</td>

											<td>
												<strong>
													<fmt:formatNumber value="${item.subtotal}"
																	  type="number"
																	  groupingUsed="true"
																	  minFractionDigits="0"
																	  maxFractionDigits="0" />
													₫
												</strong>
											</td>
										</tr>
									</c:forEach>
									</tbody>
								</table>
							</div>
						</c:otherwise>
					</c:choose>
				</div>

				<!-- ================= UPDATE ORDER STATUS ================= -->
				<div class="admin-section-card">
					<h2 class="admin-section-title">Cập nhật trạng thái đơn hàng</h2>

					<form method="post"
						  action="${pageContext.request.contextPath}/admin/orders"
						  class="admin-form">

						<%@ include file="/jsp/common/csrf.jspf" %>

						<input type="hidden" name="action" value="updateStatus" />
						<input type="hidden" name="id" value="${order.id}" />

						<div class="admin-field" style="max-width: 420px;">
							<div class="admin-label">Trạng thái đơn hàng</div>

							<select name="status" class="admin-select">
								<option value="processing"
								${order.status == 'processing' ? 'selected' : ''}>
									Processing - Đang xử lý
								</option>

								<option value="confirmed"
								${order.status == 'confirmed' ? 'selected' : ''}>
									Confirmed - Đã xác nhận
								</option>

								<option value="shipping"
								${order.status == 'shipping' ? 'selected' : ''}>
									Shipping - Đang giao
								</option>

								<option value="completed"
								${order.status == 'completed' ? 'selected' : ''}>
									Completed - Hoàn tất
								</option>

								<option value="cancelled"
								${order.status == 'cancelled' ? 'selected' : ''}>
									Cancelled - Đã hủy
								</option>
							</select>

							<div class="admin-help">
								Nếu chọn Completed, hệ thống tự chuyển payment_status sang PAID.
								Nếu chọn Cancelled, hệ thống tự chuyển payment_status sang CANCELED.
							</div>
						</div>

						<div class="admin-actions">
							<button type="submit" class="admin-btn admin-btn--primary">
								Lưu trạng thái đơn hàng
							</button>

							<a class="admin-btn"
							   href="${pageContext.request.contextPath}/admin/orders">
								Hủy
							</a>
						</div>
					</form>
				</div>

			</div>
		</div>

	</div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp" />

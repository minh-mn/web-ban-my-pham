<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Order Detail" scope="request" />
<c:set var="activeMenu" value="orders" scope="request" />
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request" />

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

<main class="admin-main">
	<div class="admin-container">

		<div class="admin-topbar">
			<div>
				<h1 class="admin-h1">Chi tiết đơn hàng #${order.id}</h1>

				<p class="admin-subtext">
					Trạng thái:
					<c:choose>
						<c:when test="${order.status == 'completed'}">
              <span class="admin-pill admin-pill--ok">
                <c:out value="${order.statusLabel}" />
              </span>
						</c:when>

						<c:when test="${order.status == 'cancelled'}">
              <span class="admin-pill admin-pill--danger">
                <c:out value="${order.statusLabel}" />
              </span>
						</c:when>

						<c:otherwise>
              <span class="admin-pill">
                <c:out value="${order.statusLabel}" />
              </span>
						</c:otherwise>
					</c:choose>
				</p>
			</div>

			<a class="admin-btn"
			   href="${pageContext.request.contextPath}/admin/orders">
				Quay lại
			</a>
		</div>

		<div class="admin-card">
			<div class="admin-card__body">

				<!-- INFO GRID -->
				<div class="admin-grid-2">

					<div class="admin-field">
						<div class="admin-label">Khách hàng</div>
						<div>
							<c:out value="${order.fullName}" />
						</div>
					</div>

					<div class="admin-field">
						<div class="admin-label">SĐT</div>
						<div>
							<c:out value="${order.phone}" />
						</div>
					</div>

					<div class="admin-field" style="grid-column: 1/-1;">
						<div class="admin-label">Địa chỉ</div>
						<div>
							<c:out value="${order.address}" />
						</div>
					</div>

					<div class="admin-field">
						<div class="admin-label">Tổng tiền</div>
						<div>
							<strong>
								<c:choose>
									<c:when test="${not empty order.totalVnd}">
										<fmt:formatNumber value="${order.totalVnd * 1000}"
										                  type="number"
										                  groupingUsed="true"
										                  minFractionDigits="0"
										                  maxFractionDigits="0" />
									</c:when>
									<c:otherwise>0</c:otherwise>
								</c:choose>
							</strong> ₫
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
							<c:out value="${order.paymentStatus}" />
						</div>
					</div>

					<div class="admin-field">
						<div class="admin-label">VNPAY TxnRef</div>
						<div class="admin-muted">
							<c:out value="${order.vnpTxnRef}" />
						</div>
					</div>

				</div>

				<hr class="admin-divider" />

				<!-- ORDER ITEMS -->
				<h2 class="admin-h2" style="margin: 0 0 10px;">
					Sản phẩm trong đơn hàng
				</h2>

				<%--
                  Hỗ trợ cả hai tên attribute:
                  - orderItems
                  - items

                  Nếu servlet của bạn setAttribute("orderItems", list) thì dùng orderItems.
                  Nếu servlet của bạn setAttribute("items", list) thì vẫn hiển thị được.
                --%>
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
                              <span class="admin-pill">
                                Variant
                              </span>
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

				<hr class="admin-divider" />

				<!-- UPDATE STATUS -->
				<h2 class="admin-h2" style="margin: 0 0 10px;">
					Cập nhật trạng thái
				</h2>

				<form method="post"
				      action="${pageContext.request.contextPath}/admin/orders"
				      class="admin-form">

					<%@ include file="/jsp/common/csrf.jspf" %>

					<input type="hidden" name="action" value="updateStatus" />
					<input type="hidden" name="id" value="${order.id}" />

					<div class="admin-field" style="max-width: 420px;">
						<div class="admin-label">Trạng thái</div>

						<select name="status" class="admin-select">
							<option value="processing"
							${order.status == 'processing' ? 'selected' : ''}>
								Processing
							</option>

							<option value="confirmed"
							${order.status == 'confirmed' ? 'selected' : ''}>
								Confirmed
							</option>

							<option value="shipping"
							${order.status == 'shipping' ? 'selected' : ''}>
								Shipping
							</option>

							<option value="completed"
							${order.status == 'completed' ? 'selected' : ''}>
								Completed
							</option>

							<option value="cancelled"
							${order.status == 'cancelled' ? 'selected' : ''}>
								Cancelled
							</option>
						</select>

						<div class="admin-help">
							Thay đổi trạng thái đơn hàng và lưu lại.
						</div>
					</div>

					<div class="admin-actions">
						<button type="submit" class="admin-btn admin-btn--primary">
							Lưu
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
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp" />
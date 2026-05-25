<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="isSuccess" value="${success}" />
<c:set var="statusClass" value="${not empty orderStatus ? fn:toLowerCase(orderStatus) : fn:toLowerCase(order.status)}" />
<c:set var="paymentClass" value="${not empty paymentMethod ? fn:toLowerCase(paymentMethod) : 'unknown'}" />

<section class="success-section ${isSuccess ? '' : 'is-failed'}">
	<div class="success-container">

		<!-- ================= HERO ================= -->
		<div class="success-hero">
			<div class="success-icon">
				<c:choose>
					<c:when test="${isSuccess}">
						🎉
					</c:when>
					<c:otherwise>
						⚠️
					</c:otherwise>
				</c:choose>
			</div>

			<c:choose>
				<c:when test="${isSuccess}">
					<h1 class="success-title">Thanh toán thành công!</h1>

					<p class="success-desc">
						Cảm ơn bạn đã mua hàng tại <strong>MyCosmetic Shop</strong>.
						Thông tin chi tiết đơn hàng của bạn được hiển thị bên dưới.
					</p>
				</c:when>

				<c:otherwise>
					<h1 class="success-title">Thanh toán không thành công</h1>

					<p class="success-desc">
						<c:out value="${not empty message ? message : 'Giao dịch chưa hoàn tất.'}" />
					</p>
				</c:otherwise>
			</c:choose>
		</div>

		<c:choose>
			<c:when test="${isSuccess && order != null}">

				<div class="success-layout">

					<!-- ================= LEFT COLUMN ================= -->
					<div class="success-left">

						<!-- ORDER INFORMATION -->
						<div class="success-card">
							<div class="success-card-header">
								<div>
									<h2>Thông tin đơn hàng</h2>
									<p>Thông tin xác nhận đơn hàng vừa đặt.</p>
								</div>

								<span class="success-badge ${statusClass}">
                  <c:out value="${not empty orderStatusLabel ? orderStatusLabel : order.status}" />
                </span>
							</div>

							<div class="success-info-grid">

								<div class="success-info-item">
									<span class="success-info-label">Mã đơn hàng</span>
									<span class="success-info-value highlight">
                    <c:choose>
						<c:when test="${not empty orderCode}">
							<c:out value="${orderCode}" />
						</c:when>
						<c:otherwise>
							#<c:out value="${order.id}" />
						</c:otherwise>
					</c:choose>
                  </span>
								</div>

								<div class="success-info-item">
									<span class="success-info-label">Ngày đặt hàng</span>
									<span class="success-info-value">
                    <c:out value="${not empty createdAtText ? createdAtText : 'Đang cập nhật'}" />
                  </span>
								</div>

								<div class="success-info-item">
									<span class="success-info-label">Phương thức thanh toán</span>
									<span class="success-badge ${paymentClass}">
                    <c:out value="${not empty paymentMethodLabel ? paymentMethodLabel : paymentMethod}" />
                  </span>
								</div>

								<div class="success-info-item">
									<span class="success-info-label">Trạng thái đơn hàng</span>
									<span class="success-info-value">
                    <c:out value="${not empty orderStatusLabel ? orderStatusLabel : order.status}" />
                  </span>
								</div>

							</div>
						</div>

						<!-- RECEIVER INFORMATION -->
						<div class="success-card">
							<div class="success-card-header">
								<div>
									<h2>Thông tin người nhận</h2>
									<p>Địa chỉ giao hàng đã được ghi nhận cho đơn hàng.</p>
								</div>
							</div>

							<div class="success-address-box">
								<div class="success-address-name">
									<c:out value="${not empty receiverName ? receiverName : 'Khách hàng'}" />
								</div>

								<div class="success-address-phone">
									Số điện thoại:
									<strong>
										<c:out value="${not empty receiverPhone ? receiverPhone : 'Đang cập nhật'}" />
									</strong>
								</div>

								<div class="success-address-detail">
									Địa chỉ:
									<strong>
										<c:out value="${not empty shippingAddress ? shippingAddress : 'Đang cập nhật'}" />
									</strong>
								</div>
							</div>
						</div>

						<!-- SHIPPING INFORMATION -->
						<div class="success-card">
							<div class="success-card-header">
								<div>
									<h2>Thông tin vận chuyển</h2>
									<p>Theo dõi mã vận chuyển và phương thức giao hàng.</p>
								</div>
							</div>

							<div class="success-info-grid">

								<div class="success-info-item">
									<span class="success-info-label">Đơn vị vận chuyển</span>
									<span class="success-info-value">
                    <c:out value="${not empty shippingProvider ? shippingProvider : 'MyCosmetic Delivery'}" />
                  </span>
								</div>

								<div class="success-info-item">
									<span class="success-info-label">Mã vận chuyển</span>
									<span class="success-info-value highlight">
                    <c:out value="${not empty trackingCode ? trackingCode : 'Đang cập nhật'}" />
                  </span>
								</div>

								<div class="success-info-item">
									<span class="success-info-label">Phương thức giao hàng</span>
									<span class="success-info-value">
                    <c:out value="${not empty shippingMethodLabel ? shippingMethodLabel : 'Giao hàng tiêu chuẩn'}" />
                  </span>
								</div>

								<div class="success-info-item">
									<span class="success-info-label">Phí vận chuyển</span>
									<span class="success-info-value">
                    <c:out value="${not empty shippingFeeVnd ? shippingFeeVnd : '0 ₫'}" />
                  </span>
								</div>

							</div>

							<div class="success-shipping-code">
								Mã vận chuyển của bạn:
								<strong>
									<c:out value="${not empty trackingCode ? trackingCode : 'Đang cập nhật'}" />
								</strong>
							</div>
						</div>

						<!-- PRODUCT LIST -->
						<div class="success-card">
							<div class="success-card-header">
								<div>
									<h2>Mặt hàng trong đơn</h2>
									<p>Danh sách sản phẩm đã đặt trong đơn hàng này.</p>
								</div>
							</div>

							<div class="success-products">
								<c:choose>

									<c:when test="${not empty orderItems}">
										<c:forEach var="item" items="${orderItems}">

											<div class="success-product-item">

												<div class="success-product-img">
													<c:choose>
														<c:when test="${not empty item.imageUrl}">
															<img src="${pageContext.request.contextPath}${item.imageUrl}"
																 alt="${fn:escapeXml(not empty item.productName ? item.productName : item.title)}"
																 onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/assets/images/default-product.jpg';">
														</c:when>

														<c:otherwise>
															<img src="${pageContext.request.contextPath}/assets/images/default-product.jpg"
																 alt="Sản phẩm">
														</c:otherwise>
													</c:choose>
												</div>

												<div class="success-product-info">
													<div class="success-product-name">
														<c:choose>
															<c:when test="${not empty item.productName}">
																<c:out value="${item.productName}" />
															</c:when>
															<c:when test="${not empty item.title}">
																<c:out value="${item.title}" />
															</c:when>
															<c:otherwise>
																Sản phẩm
															</c:otherwise>
														</c:choose>
													</div>

													<div class="success-product-variant">
														<c:choose>
															<c:when test="${not empty item.variantDisplayName}">
																<c:out value="${item.variantDisplayName}" />
															</c:when>
															<c:when test="${not empty item.variantName}">
																<c:out value="${item.variantName}" />
															</c:when>
															<c:otherwise>
																Mặc định
															</c:otherwise>
														</c:choose>
													</div>

													<div class="success-product-meta">
														Số lượng:
														<strong>
															<c:out value="${not empty item.quantity ? item.quantity : 1}" />
														</strong>
													</div>
												</div>

												<div class="success-product-price">
													<c:choose>
														<c:when test="${not empty item.subtotal}">
															<fmt:formatNumber value="${item.subtotal}"
																			  type="number"
																			  groupingUsed="true" />đ
														</c:when>

														<c:when test="${not empty item.totalPrice}">
															<fmt:formatNumber value="${item.totalPrice}"
																			  type="number"
																			  groupingUsed="true" />đ
														</c:when>

														<c:when test="${not empty item.price}">
															<fmt:formatNumber value="${item.price}"
																			  type="number"
																			  groupingUsed="true" />đ
														</c:when>

														<c:otherwise>
															-
														</c:otherwise>
													</c:choose>
												</div>

											</div>

										</c:forEach>
									</c:when>

									<c:otherwise>
										<div class="success-empty">
											Danh sách sản phẩm đang được cập nhật.
											Nếu cần xem chi tiết đầy đủ, vui lòng bấm
											<strong>Xem chi tiết đơn hàng</strong>.
										</div>
									</c:otherwise>

								</c:choose>
							</div>
						</div>

					</div>

					<!-- ================= RIGHT COLUMN ================= -->
					<div class="success-right">

						<!-- ORDER SUMMARY -->
						<div class="success-summary">
							<div class="success-panel-header">
								<div>
									<h2>Tổng kết thanh toán</h2>
									<p>Chi tiết giá trị đơn hàng.</p>
								</div>
							</div>

							<div class="success-summary-line">
								<span>Tạm tính</span>
								<strong>
									<c:out value="${not empty subtotalVnd ? subtotalVnd : '-'}" />
								</strong>
							</div>

							<div class="success-summary-line">
								<span>Giảm giá</span>
								<strong>
									<c:out value="${not empty discountVnd ? discountVnd : '0 ₫'}" />
								</strong>
							</div>

							<div class="success-summary-line">
								<span>Phí vận chuyển</span>
								<strong>
									<c:out value="${not empty shippingFeeVnd ? shippingFeeVnd : '0 ₫'}" />
								</strong>
							</div>

							<div class="success-summary-line total">
								<span>Tổng thanh toán</span>
								<strong>
									<c:out value="${not empty totalVnd ? totalVnd : '0 ₫'}" />
								</strong>
							</div>
						</div>

						<!-- EMAIL NOTICE -->
						<div class="success-email-notice">
							<div class="success-email-notice-icon">✉</div>

							<div>
								<strong>Thông báo đơn hàng qua email</strong>
								<br>
								<c:out value="${not empty emailNotice ? emailNotice : 'Thông tin đơn hàng và hóa đơn sẽ được gửi về email của bạn sau khi hệ thống xác nhận.'}" />
							</div>
						</div>

					</div>

				</div>

				<!-- ACTIONS -->
				<div class="success-actions">
					<a href="${pageContext.request.contextPath}/orders/detail?id=${order.id}"
					   class="success-btn">
						📄 Xem chi tiết đơn hàng
					</a>

					<a href="${pageContext.request.contextPath}/"
					   class="success-btn-secondary">
						🏠 Về trang chủ
					</a>

					<a href="${pageContext.request.contextPath}/products"
					   class="success-btn-secondary">
						🛍 Tiếp tục mua hàng
					</a>
				</div>

			</c:when>

			<c:otherwise>

				<div class="success-failed-message">
					<c:out value="${not empty message ? message : 'Không thể hoàn tất giao dịch. Vui lòng thử lại hoặc quay về giỏ hàng.'}" />
				</div>

				<div class="success-actions">
					<a href="${pageContext.request.contextPath}/cart"
					   class="success-btn-secondary">
						🔁 Quay lại giỏ hàng
					</a>

					<a href="${pageContext.request.contextPath}/"
					   class="success-btn">
						🏠 Về trang chủ
					</a>
				</div>

			</c:otherwise>
		</c:choose>

	</div>
</section>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="isSuccess" value="${success}" />
<c:set var="statusClass" value="${not empty orderStatus ? fn:toLowerCase(orderStatus) : 'processing'}" />
<c:set var="paymentClass" value="${not empty paymentMethod ? fn:toLowerCase(paymentMethod) : 'unknown'}" />

<section class="success-section ${isSuccess ? '' : 'is-failed'}">
	<div class="success-container">

		<c:if test="${isSuccess}">
			<div class="success-hero success-hero-center">
				<div class="success-icon success-icon-check" aria-hidden="true">
					<svg viewBox="0 0 52 52" focusable="false">
						<circle class="check-circle" cx="26" cy="26" r="24"></circle>
						<path class="check-mark" d="M15 27.5 L22.5 35 L38 18"></path>
					</svg>
				</div>

				<h1 class="success-title">Thanh toán thành công!</h1>

				<p class="success-desc">
					Cảm ơn bạn đã mua hàng tại <strong>MyCosmetic Shop</strong>.
					Thông tin chi tiết đơn hàng của bạn được hiển thị bên dưới.
				</p>
			</div>
		</c:if>

		<c:if test="${not isSuccess}">
			<div class="success-hero success-hero-center">
				<div class="success-icon success-icon-failed" aria-hidden="true">!</div>

				<h1 class="success-title">Thanh toán không thành công</h1>

				<p class="success-desc">
					<c:out value="${not empty message ? message : 'Giao dịch chưa hoàn tất.'}" />
				</p>
			</div>

			<div class="success-actions success-actions-center">
				<c:if test="${order != null && order.retryPaymentAvailable}">
					<a href="${pageContext.request.contextPath}/vnpay/payment?orderId=${order.id}" class="success-btn">
						Thanh toán lại
					</a>
				</c:if>

				<c:if test="${order != null}">
					<a href="${pageContext.request.contextPath}/orders/detail?id=${order.id}" class="success-btn-secondary">
						Xem chi tiết đơn hàng
					</a>
				</c:if>

				<a href="${pageContext.request.contextPath}/cart" class="success-btn-secondary">
					Quay lại giỏ hàng
				</a>

				<a href="${pageContext.request.contextPath}/" class="success-btn-secondary">
					Về trang chủ
				</a>
			</div>
		</c:if>

		<c:if test="${isSuccess && order != null}">

			<div class="success-layout success-layout-balanced">

				<div class="success-left">

					<div class="success-card">
						<div class="success-card-header">
							<div>
								<h2>Thông tin người nhận</h2>
								<p>Thông tin giao hàng đã được ghi nhận cho đơn hàng.</p>
							</div>
						</div>

						<div class="success-info-grid receiver-grid">
							<div class="success-info-item">
								<span class="success-info-label">Khách hàng</span>
								<span class="success-info-value">
                  <c:out value="${not empty receiverNameText ? receiverNameText : receiverName}" />
                </span>
							</div>

							<div class="success-info-item">
								<span class="success-info-label">Số điện thoại</span>
								<span class="success-info-value">
                  <c:out value="${not empty receiverPhoneText ? receiverPhoneText : receiverPhone}" />
                </span>
							</div>

							<div class="success-info-item success-info-item-full">
								<span class="success-info-label">Địa chỉ</span>
								<span class="success-info-value">
                  <c:out value="${not empty receiverAddressText ? receiverAddressText : shippingAddress}" />
                </span>
							</div>
						</div>
					</div>

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
                  <c:out value="${not empty orderCode ? orderCode : '#'.concat(order.id)}" />
                </span>
							</div>

							<div class="success-info-item">
								<span class="success-info-label">Ngày đặt hàng</span>
								<span class="success-info-value">
                  <c:out value="${not empty orderDateText ? orderDateText : createdAtText}" />
                </span>
							</div>

							<div class="success-info-item">
								<span class="success-info-label">Ngày nhận hàng dự kiến</span>
								<span class="success-info-value">
                  <c:out value="${not empty receivedDateText ? receivedDateText : deliveredDateText}" />
                </span>
							</div>

							<div class="success-info-item">
								<span class="success-info-label">Phương thức thanh toán</span>
								<span class="success-badge ${paymentClass}">
                  <c:out value="${not empty paymentMethodText ? paymentMethodText : paymentMethodLabel}" />
                </span>
							</div>

							<div class="success-info-item success-info-item-full">
								<span class="success-info-label">Phương thức giao hàng</span>
								<span class="success-info-value">
                  <c:out value="${not empty shippingMethodLabel ? shippingMethodLabel : 'Giao hàng tiêu chuẩn'}" />
                </span>
							</div>
						</div>

						<div class="success-products-block">
							<div class="success-subhead">
								<h3>Mặt hàng trong đơn</h3>
								<span>
                  <c:out value="${not empty orderItemsCount ? orderItemsCount : 0}" /> sản phẩm
                </span>
							</div>

							<div class="success-products">
								<c:if test="${not empty orderItems}">
									<c:forEach var="item" items="${orderItems}">
										<div class="success-product-item">
											<div class="success-product-img">
												<c:choose>
													<c:when test="${not empty item.imageUrl}">
														<img src="${pageContext.request.contextPath}${item.imageUrl}"
															 alt="${fn:escapeXml(item.productName)}"
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
													<c:out value="${item.productName}" />
												</div>

												<div class="success-product-variant">
													<c:out value="${item.variantName}" />
												</div>

												<div class="success-product-meta">
													Số lượng:
													<strong><c:out value="${item.quantity}" /></strong>
													<span>•</span>
													Đơn giá:
													<strong><c:out value="${item.unitPriceVnd}" /></strong>
												</div>
											</div>

											<div class="success-product-price">
												<c:out value="${item.lineTotalVnd}" />
											</div>
										</div>
									</c:forEach>
								</c:if>

								<c:if test="${empty orderItems}">
									<div class="success-empty">
										Chưa tải được danh sách sản phẩm. Vui lòng bấm
										<strong>Xem chi tiết đơn hàng</strong> để kiểm tra lại.
									</div>
								</c:if>
							</div>
						</div>
					</div>

				</div>

				<div class="success-right">

					<div class="success-summary">
						<div class="success-panel-header">
							<div>
								<h2>Tổng kết thanh toán</h2>
								<p>Chi tiết giá trị đơn hàng.</p>
							</div>
						</div>

						<div class="success-summary-line">
							<span>Tạm tính</span>
							<strong><c:out value="${not empty subtotalVnd ? subtotalVnd : '0 ₫'}" /></strong>
						</div>

						<div class="success-summary-line">
							<span>Giảm giá</span>
							<strong><c:out value="${not empty discountVnd ? discountVnd : '0 ₫'}" /></strong>
						</div>

						<div class="success-summary-line">
							<span>Phí vận chuyển</span>
							<strong><c:out value="${not empty shippingFeeVnd ? shippingFeeVnd : '0 ₫'}" /></strong>
						</div>

						<div class="success-summary-line total">
							<span>Tổng thanh toán</span>
							<strong><c:out value="${not empty totalVnd ? totalVnd : '0 ₫'}" /></strong>
						</div>
					</div>

				</div>

			</div>

			<div class="success-email-notice success-email-notice-bottom">
				<div class="success-email-notice-icon">✉</div>

				<div>
					<strong>Thông báo đơn hàng qua email</strong>
					<br>

					<c:choose>
						<c:when test="${emailSent}">
							Hóa đơn và thông tin đơn hàng đã được gửi về email
							<strong><c:out value="${emailText}" /></strong>.
						</c:when>
						<c:otherwise>
							Hệ thống sẽ gửi hóa đơn và thông tin đơn hàng về email
							<strong><c:out value="${not empty emailText ? emailText : 'của bạn'}" /></strong>
							sau khi xử lý xong.
						</c:otherwise>
					</c:choose>
				</div>
			</div>

			<div class="success-actions success-actions-center">
				<a href="${pageContext.request.contextPath}/orders/detail?id=${order.id}"
				   class="success-btn">
					Xem chi tiết đơn hàng
				</a>

				<a href="${pageContext.request.contextPath}/"
				   class="success-btn-secondary">
					Về trang chủ
				</a>

				<a href="${pageContext.request.contextPath}/products"
				   class="success-btn-secondary">
					Tiếp tục mua hàng
				</a>
			</div>

		</c:if>

	</div>
</section>

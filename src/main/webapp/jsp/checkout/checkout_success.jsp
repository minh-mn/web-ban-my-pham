<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="isSuccess" value="${success}" />
<c:set var="statusClass" value="${not empty orderStatus ? fn:toLowerCase(orderStatus) : 'processing'}" />
<c:set var="paymentClass" value="${not empty paymentMethod ? fn:toLowerCase(paymentMethod) : 'unknown'}" />

<section class="success-section ${isSuccess ? '' : 'is-failed'}">
	<div class="success-container">

		<c:choose>
			<c:when test="${isSuccess}">
				<div class="success-hero">
					<div class="success-hero-mark" aria-hidden="true">
						<svg viewBox="0 0 56 56" focusable="false">
							<circle class="success-hero-ring" cx="28" cy="28" r="26"></circle>
							<circle class="success-hero-fill" cx="28" cy="28" r="20"></circle>
							<path class="success-hero-check" d="M18 28.5 L25 35.5 L39 20.5"></path>
						</svg>
					</div>

					<div class="success-sparkle success-sparkle-1">✦</div>
					<div class="success-sparkle success-sparkle-2">✿</div>
					<div class="success-sparkle success-sparkle-3">✧</div>

					<h1>Thanh toán thành công!</h1>
					<p>
						Cảm ơn bạn đã mua hàng tại <strong>MyCosmetic Shop</strong>.
						Thông tin chi tiết đơn hàng của bạn được hiển thị bên dưới.
					</p>
				</div>
			</c:when>

			<c:otherwise>
				<div class="success-hero failed">
					<div class="success-hero-mark failed" aria-hidden="true">!</div>

					<h1>Thanh toán không thành công</h1>
					<p>
						<c:out value="${not empty message ? message : 'Giao dịch chưa hoàn tất. Vui lòng kiểm tra lại thông tin đơn hàng.'}" />
					</p>
				</div>

				<div class="success-actions failed-actions">
					<c:if test="${order != null && order.retryPaymentAvailable}">
						<a href="${pageContext.request.contextPath}/vnpay/payment?orderId=${order.id}"
						   class="success-action-btn primary">
							<span>↻</span>
							Thanh toán lại
						</a>
					</c:if>

					<c:if test="${order != null}">
						<a href="${pageContext.request.contextPath}/orders/detail?id=${order.id}"
						   class="success-action-btn outline">
							<span>📋</span>
							Xem chi tiết đơn hàng
						</a>
					</c:if>

					<a href="${pageContext.request.contextPath}/cart" class="success-action-btn outline">
						<span>🛒</span>
						Quay lại giỏ hàng
					</a>

					<a href="${pageContext.request.contextPath}/" class="success-action-btn outline">
						<span>⌂</span>
						Về trang chủ
					</a>
				</div>
			</c:otherwise>
		</c:choose>

		<c:if test="${isSuccess && order != null}">
			<div class="success-main-grid">

				<div class="success-left-stack">
					<div class="success-top-grid">

						<article class="success-card receiver-card">
							<div class="success-card-title">
								<span class="success-title-icon">♙</span>
								<div>
									<h2>Thông tin người nhận</h2>
									<p>Thông tin giao hàng đã được ghi nhận cho đơn hàng.</p>
								</div>
							</div>

							<div class="success-field-grid">
								<div class="success-field">
									<span>Khách hàng</span>
									<strong>
										<c:out value="${not empty receiverNameText ? receiverNameText : receiverName}" />
									</strong>
								</div>

								<div class="success-field">
									<span>Số điện thoại</span>
									<strong>
										<c:out value="${not empty receiverPhoneText ? receiverPhoneText : receiverPhone}" />
									</strong>
								</div>

								<div class="success-field field-full">
									<span>Địa chỉ</span>
									<strong>
										<c:out value="${not empty receiverAddressText ? receiverAddressText : shippingAddress}" />
									</strong>
								</div>
							</div>
						</article>

						<article class="success-card order-card">
							<div class="success-card-title">
								<span class="success-title-icon">▣</span>
								<div>
									<h2>Thông tin đơn hàng</h2>
									<p>Thông tin xác nhận đơn hàng vừa đặt.</p>
								</div>
							</div>

							<div class="success-order-list">
								<div class="success-order-row">
									<span>Mã đơn hàng</span>
									<strong class="pink">
										<c:out value="${not empty orderCode ? orderCode : '#'.concat(order.id)}" />
									</strong>
								</div>

								<div class="success-order-row">
									<span>Ngày đặt hàng</span>
									<strong>
										<c:out value="${not empty orderDateText ? orderDateText : createdAtText}" />
									</strong>
								</div>

								<div class="success-order-row">
									<span>Ngày nhận hàng dự kiến</span>
									<strong>
										<c:out value="${not empty receivedDateText ? receivedDateText : deliveredDateText}" />
									</strong>
								</div>

								<div class="success-order-row">
									<span>Phương thức thanh toán</span>
									<strong>
                                        <span class="success-chip ${paymentClass}">
                                            <c:out value="${not empty paymentMethodText ? paymentMethodText : paymentMethodLabel}" />
                                        </span>
									</strong>
								</div>

								<div class="success-order-row">
									<span>Phương thức giao hàng</span>
									<strong>
										<c:out value="${not empty shippingMethodLabel ? shippingMethodLabel : 'Giao hàng tiêu chuẩn'}" />
									</strong>
								</div>

								<div class="success-order-row">
									<span>Trạng thái đơn</span>
									<strong>
                                        <span class="success-chip ${statusClass}">
                                            <c:out value="${not empty orderStatusLabel ? orderStatusLabel : order.status}" />
                                        </span>
									</strong>
								</div>
							</div>
						</article>
					</div>

					<article class="success-card products-card">
						<div class="success-products-head">
							<div class="success-card-title compact">
								<span class="success-title-icon">▤</span>
								<div>
									<h2>Mặt hàng trong đơn</h2>
									<p>Danh sách sản phẩm đã được xác nhận.</p>
								</div>
							</div>

							<span class="success-product-count">
                                <c:out value="${not empty orderItemsCount ? orderItemsCount : 0}" /> sản phẩm
                            </span>
						</div>

						<div class="success-products-list">
							<c:choose>
								<c:when test="${not empty orderItems}">
									<c:forEach var="item" items="${orderItems}">
										<div class="success-product-row">
											<div class="success-product-thumb">
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
												<h3><c:out value="${item.productName}" /></h3>

												<div class="success-product-meta">
													<span>Phân loại: <strong><c:out value="${item.variantName}" /></strong></span>
													<span>•</span>
													<span>Số lượng: <strong><c:out value="${item.quantity}" /></strong></span>
													<span>•</span>
													<span>Đơn giá: <strong><c:out value="${item.unitPriceVnd}" /></strong></span>
												</div>
											</div>

											<div class="success-product-total">
												<c:out value="${item.lineTotalVnd}" />
											</div>
										</div>
									</c:forEach>
								</c:when>

								<c:otherwise>
									<div class="success-empty-box">
										Chưa tải được danh sách sản phẩm. Vui lòng bấm
										<strong>Xem chi tiết đơn hàng</strong> để kiểm tra lại.
									</div>
								</c:otherwise>
							</c:choose>
						</div>
					</article>
				</div>

				<aside class="success-right-stack">
					<article class="success-summary-card">
						<div class="success-card-title compact">
							<span class="success-title-icon">▦</span>
							<div>
								<h2>Tổng kết thanh toán</h2>
								<p>Chi tiết giá trị đơn hàng.</p>
							</div>
						</div>

						<div class="success-summary-lines">
							<div>
								<span>Tạm tính</span>
								<strong><c:out value="${not empty subtotalVnd ? subtotalVnd : '0 ₫'}" /></strong>
							</div>

							<div>
								<span>Giảm giá</span>
								<strong><c:out value="${not empty discountVnd ? discountVnd : '0 ₫'}" /></strong>
							</div>

							<div>
								<span>Phí vận chuyển</span>
								<strong><c:out value="${not empty shippingFeeVnd ? shippingFeeVnd : '0 ₫'}" /></strong>
							</div>

							<div class="total">
								<span>Tổng thanh toán</span>
								<strong><c:out value="${not empty totalVnd ? totalVnd : '0 ₫'}" /></strong>
							</div>
						</div>

						<div class="success-paid-note">
							<span class="success-paid-icon">✓</span>
							<div>
								<strong>Thanh toán thành công</strong>
								<p>Đơn hàng của bạn đã được hệ thống xác nhận.</p>
							</div>
						</div>
					</article>

					<article class="success-email-card">
						<span class="success-email-icon">✉</span>
						<div>
							<h2>Thông báo đơn hàng qua email</h2>

							<c:choose>
								<c:when test="${emailSent}">
									<p>
										Hóa đơn và thông tin đơn hàng đã được gửi về email
										<strong><c:out value="${emailText}" /></strong>.
									</p>
								</c:when>
								<c:otherwise>
									<p>
										Hệ thống sẽ gửi hóa đơn và thông tin đơn hàng về email
										<strong><c:out value="${not empty emailText ? emailText : 'của bạn'}" /></strong>
										sau khi xử lý xong.
									</p>
								</c:otherwise>
							</c:choose>
						</div>
					</article>
				</aside>
			</div>

			<div class="success-actions">
				<a href="${pageContext.request.contextPath}/orders/detail?id=${order.id}"
				   class="success-action-btn primary">
					<span>▣</span>
					Xem chi tiết đơn hàng
				</a>

				<a href="${pageContext.request.contextPath}/"
				   class="success-action-btn outline">
					<span>⌂</span>
					Về trang chủ
				</a>

				<a href="${pageContext.request.contextPath}/products"
				   class="success-action-btn outline accent">
					<span>▤</span>
					Tiếp tục mua hàng
				</a>
			</div>
		</c:if>
	</div>
</section>

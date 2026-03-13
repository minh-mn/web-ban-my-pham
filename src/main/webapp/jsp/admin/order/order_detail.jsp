<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:set var="pageTitle" value="ADMIN | Order Detail" scope="request" />
<c:set var="activeMenu" value="orders" scope="request" />
<c:set var="pageCss" value="/assets/css/admin/admin-form.css"
	scope="request" />

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
							<span class="admin-pill admin-pill--ok"><c:out
									value="${order.statusLabel}" /></span>
						</c:when>
						<c:when test="${order.status == 'cancelled'}">
							<span class="admin-pill admin-pill--danger"><c:out
									value="${order.statusLabel}" /></span>
						</c:when>
						<c:otherwise>
							<span class="admin-pill"><c:out
									value="${order.statusLabel}" /></span>
						</c:otherwise>
					</c:choose>
				</p>
			</div>

			<a class="admin-btn"
				href="${pageContext.request.contextPath}/admin/orders">Quay lại</a>
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
							<strong> <c:choose>
									<c:when test="${not empty order.totalVnd}">
										<fmt:formatNumber value="${order.totalVnd * 1000}" type="number"
											groupingUsed="true" minFractionDigits="0"
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

				<!-- UPDATE STATUS -->
				<h2 class="admin-h2" style="margin: 0 0 10px;">Cập nhật trạng
					thái</h2>

				<form method="post"
					action="${pageContext.request.contextPath}/admin/orders"
					class="admin-form">

          <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
          <%@ include file="/jsp/common/csrf.jspf" %>

					<input type="hidden" name="action" value="updateStatus" />
          <input type="hidden" name="id" value="${order.id}" />

					<div class="admin-field" style="max-width: 420px;">
						<div class="admin-label">Trạng thái</div>
						<select name="status" class="admin-select">
							<option value="processing"
								${order.status == 'processing' ? 'selected' : ''}>Processing</option>
							<option value="confirmed"
								${order.status == 'confirmed' ? 'selected' : ''}>Confirmed</option>
							<option value="shipping"
								${order.status == 'shipping' ? 'selected' : ''}>Shipping</option>
							<option value="completed"
								${order.status == 'completed' ? 'selected' : ''}>Completed</option>
							<option value="cancelled"
								${order.status == 'cancelled' ? 'selected' : ''}>Cancelled</option>
						</select>
						<div class="admin-help">Thay đổi trạng thái đơn hàng và lưu
							lại.</div>
					</div>

					<div class="admin-actions">
						<button type="submit" class="admin-btn admin-btn--primary">Lưu</button>
						<a class="admin-btn"
							href="${pageContext.request.contextPath}/admin/orders">Hủy</a>
					</div>

				</form>

			</div>
		</div>

	</div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp" />

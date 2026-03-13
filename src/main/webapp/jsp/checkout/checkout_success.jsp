<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<section class="section success-section">
	<div class="container success-box">

		<c:choose>
			<c:when test="${success}">
				<h2 class="success-title">🎉 Thanh toán thành công!</h2>
				<p class="success-desc">
					Cảm ơn bạn đã mua hàng tại <strong>MyCosmetic Shop</strong>.
				</p>
			</c:when>
			<c:otherwise>
				<h2 class="success-title">⚠️ Thanh toán không thành công</h2>
				<p class="success-desc">
					<c:out
						value="${message != null ? message : 'Giao dịch chưa hoàn tất.'}" />
				</p>
			</c:otherwise>
		</c:choose>

		<c:if test="${order != null}">
			<div class="success-card">
				<p>
					<strong>Mã đơn hàng:</strong> #${order.id}
				</p>

				<p>
					<strong>Tổng tiền:</strong> <span class="success-price">${totalVnd}</span>
				</p>

				<p>
					<strong>Phương thức thanh toán:</strong> ${paymentMethod}
				</p>

				<p>
					<strong>Trạng thái đơn hàng:</strong> ${order.status}
				</p>
			</div>
		</c:if>

		<div class="success-actions">
			<c:choose>
				<c:when test="${success && order != null}">
					<a
						href="${pageContext.request.contextPath}/orders/detail?id=${order.id}"
						class="success-btn"> 📄 Xem chi tiết đơn hàng </a>

					<a href="${pageContext.request.contextPath}/"
						class="success-btn secondary"> 🏠 Về trang chủ </a>
				</c:when>

				<c:otherwise>
					<a href="${pageContext.request.contextPath}/cart"
						class="success-btn secondary"> 🔁 Quay lại giỏ hàng </a>
				</c:otherwise>
			</c:choose>
		</div>

	</div>
</section>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<section class="banner-slider">
	<div class="slider-wrapper">
		<c:forEach var="banner" items="${banners}" varStatus="st">
			<div class="slide ${st.first ? 'active' : ''}">
				<img src="${pageContext.request.contextPath}${banner.imageUrl}"
					alt="${banner.title}">
				<div class="banner-overlay">
					<c:if test="${not empty banner.title}">
						<h2>${banner.title}</h2>
					</c:if>
					<c:if test="${not empty banner.link}">
						<a href="${banner.link}" class="btn-primary"> Khám phá ngay </a>
					</c:if>
				</div>
			</div>
		</c:forEach>
		<button class="nav prev">&#10094;</button>
		<button class="nav next">&#10095;</button>
		<div class="dots">
			<c:forEach items="${banners}" varStatus="st">
				<span class="dot ${st.first ? 'active' : ''}"></span>
			</c:forEach>
		</div>
	</div>
</section>

<section class="section voucher-section" style="background: #fff;">
	<div class="container">
		<div class="voucher-grid">

			<c:forEach var="voucher" items="${vouchers}">

				<div class="voucher-card
                     ${voucher.type == 'FREESHIP' ? 'free-ship' : 'discount'}">

					<!-- LEFT -->
					<div class="voucher-left">
						<div class="voucher-icon">
							<c:choose>
								<c:when test="${voucher.type == 'FREESHIP'}">
									🚚
								</c:when>
								<c:otherwise>
									🎟
								</c:otherwise>
							</c:choose>
						</div>
					</div>

					<!-- RIGHT -->
					<div class="voucher-right">

						<!-- CODE -->
						<div class="voucher-info">
							<span class="v-code">${voucher.code}</span>

							<!-- TYPE TITLE -->
							<c:choose>
								<c:when test="${voucher.type == 'FREESHIP'}">
									<h3 class="coupon-title free-ship">
										Miễn phí vận chuyển
									</h3>
								</c:when>

								<c:otherwise>
									<h3 class="coupon-title discount">
										Giảm ${voucher.discountPercent}%
									</h3>
								</c:otherwise>
							</c:choose>

							<!-- DESCRIPTION -->
							<c:if test="${not empty voucher.description}">
								<p class="coupon-desc">
										${voucher.description}
								</p>
							</c:if>

							<!-- CONDITION -->
							<c:if test="${voucher.minOrderAmount > 0}">
								<p class="coupon-condition">
									Đơn tối thiểu:
									<fmt:formatNumber value="${voucher.minOrderAmount}" type="number"/> ₫
								</p>
							</c:if>

						</div>

						<!-- ACTIONS -->
						<div class="voucher-actions">

							<!-- DETAIL BUTTON -->
							<button type="button"
							        class="btn-detail"
							        data-code="${voucher.code}"
							        data-type="${voucher.type}"
							        data-desc="${fn:escapeXml(voucher.description)}"
							        data-min="${not empty voucher.minOrderAmount ? voucher.minOrderAmount : 0}"
							        data-end="${voucher.endDate}"
									onclick="showVoucherDetailFromEl(this)">
								Xem chi tiết
							</button>

							<!-- SAVE BUTTON -->
							<button class="btn-save"
							        data-code="${voucher.code}"
							        data-loggedin="${not empty sessionScope.user}"
							        onclick="saveVoucher(this)">
								Lưu
							</button>

						</div>

					</div>

				</div>

			</c:forEach>

		</div>
	</div>
</section>

<jsp:include page="/jsp/product/hot-categories.jsp" />

<jsp:include page="/jsp/product/flash-sale.jsp" />

<jsp:include page="/jsp/common/store-events.jsp" />

<section class="section">
	<div class="container">

		<div class="section-divider"></div>
		<h2 class="section-title">Sản phẩm nổi bật</h2>

		<div class="product-grid">

			<c:forEach var="product" items="${products}">
				<div class="product-card">

					<!-- SALE BADGE -->
					<c:if test="${product.discountPercent > 0}">
						<div class="badge-sale">-${product.discountPercent}%</div>
					</c:if>

					<!-- IMAGE -->
					<div class="product-img-box">
						<c:choose>
							<c:when test="${not empty product.imageUrl}">
								<img src="${pageContext.request.contextPath}${product.imageUrl}"
									alt="${product.title}">
							</c:when>

							<c:otherwise>
								<div class="no-image">No image</div>
							</c:otherwise>
						</c:choose>
					</div>

					<!-- TITLE -->
					<h3 class="product-title">${product.title}</h3>

					<!-- RATING -->
					<div class="rating-wrap">
						<div class="rating-stars">
							<c:forEach begin="1" end="5" var="i">
								<c:choose>
									<c:when test="${i <= product.avgRating}">
										<svg width="14" height="14" viewBox="0 0 24 24" fill="#ffb400">
                                            <path
												d="M12 17.3l6.2 3.7-1.6-7
                                                     5.4-4.7-7.1-.6L12 2
                                                     9.1 8.7l-7.1.6 5.4
                                                     4.7-1.6 7z" />
                                        </svg>
									</c:when>
									<c:otherwise>
										<svg width="14" height="14" viewBox="0 0 24 24" fill="#e0e0e0">
                                            <path
												d="M12 17.3l6.2 3.7-1.6-7
                                                     5.4-4.7-7.1-.6L12 2
                                                     9.1 8.7l-7.1.6 5.4
                                                     4.7-1.6 7z" />
                                        </svg>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</div>

						<span class="rating-count"> (${product.reviewCount} đánh
							giá) </span>
					</div>

					<!-- PRICE -->
					<c:choose>
						<c:when test="${product.discountPercent > 0}">
							<p class="price">
								<span class="old-price"> <fmt:formatNumber
										value="${product.price}" type="number" groupingUsed="true" />
									₫
								</span> <span class="sale-price"> <fmt:formatNumber
										value="${product.finalPrice}" type="number"
										groupingUsed="true" /> ₫
								</span>
							</p>
						</c:when>
						<c:otherwise>
							<p class="price">
								<fmt:formatNumber value="${product.price}" type="number"
									groupingUsed="true" />
								₫
							</p>
						</c:otherwise>
					</c:choose>

					<!-- STOCK -->
					<c:choose>
						<c:when test="${product.stock == 0}">
							<div class="badge-out">Hết hàng</div>
						</c:when>
						<c:when test="${product.stock <= 5}">
							<div class="badge-low">Sắp hết</div>
						</c:when>
						<c:otherwise>
							<div class="stock-ok">Còn hàng</div>
						</c:otherwise>
					</c:choose>

					<!-- CTA -->
					<a
						href="${pageContext.request.contextPath}/product/${product.slug}"
						class="btn-outline"> Xem chi tiết </a>

				</div>
			</c:forEach>

		</div>
	</div>
</section>


<!-- ================= BRAND STORY ================= -->
<section class="section brand-story">
	<div class="container brand-story-inner">

		<h2 class="section-title">Science meets Nature</h2>

		<p class="brand-desc">Chúng tôi tin rằng chăm sóc da không chỉ là
			làm đẹp, mà còn là nuôi dưỡng làn da khỏe mạnh từ bên trong bằng
			những thành phần an toàn, minh bạch và bền vững.</p>

	</div>
</section>

<script>
document.addEventListener("DOMContentLoaded", function () {
	const slides = document.querySelectorAll(".slide");
	const dots = document.querySelectorAll(".dot");
	const prev = document.querySelector(".prev");
	const next = document.querySelector(".next");

	if (!slides.length) return;

	let index = 0;
	const interval = 5000;


	function showSlide(i) {
	slides.forEach(s => s.classList.remove("active", "prev", "next"));
	dots.forEach(d => d.classList.remove("active"));

	const prevIndex = (i - 1 + slides.length) % slides.length;
	const nextIndex = (i + 1) % slides.length;


	slides[i].classList.add("active");
	slides[prevIndex].classList.add("prev");
	slides[nextIndex].classList.add("next");

	if (dots[i]) dots[i].classList.add("active");
		index = i;
	}


	function nextSlide() {
		showSlide((index + 1) % slides.length);
	}


	function prevSlide() {
		showSlide((index - 1 + slides.length) % slides.length);
	}

	// AUTO SLIDE
	showSlide(index);

	let autoSlide = setInterval(nextSlide, interval);


	function resetAuto() {
		clearInterval(autoSlide);
		autoSlide = setInterval(nextSlide, interval);
	}


	// CLICK EVENTS

	if (next) {
		next.addEventListener("click", () => {
		nextSlide();
		resetAuto();
	});

}



	if (prev) {
		prev.addEventListener("click", () => {
		prevSlide();
		resetAuto();
	});
}


	dots.forEach((dot, i) => {
	dot.addEventListener("click", () => {

	showSlide(i);
	resetAuto();

	});
	});
	});

// Logic Countdown Flash Deal
function startFlashSale(endTimeStr) {
	const countDownDate = new Date(endTimeStr).getTime();

	const x = setInterval(function() {
		const now = new Date().getTime();
		const distance = countDownDate - now;

		// Tính toán Giờ, Phút, Giây
		const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
		const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
		const seconds = Math.floor((distance % (1000 * 60)) / 1000);

		// Hiển thị ra ID 'timer'
		document.getElementById("timer").innerHTML =
				(hours < 10 ? "0" + hours : hours) + " : " +
				(minutes < 10 ? "0" + minutes : minutes) + " : " +
				(seconds < 10 ? "0" + seconds : seconds);

		if (distance < 0) {
			clearInterval(x);
			document.getElementById("timer").innerHTML = "SỰ KIỆN KẾT THÚC";
		}
	}, 1000);
}

window.onload = function () {
    var fiveHours = 60 * 60 * 5,
        display = document.querySelector('#timer');
    startTimer(fiveHours, display);
};

function saveVoucher(btn) {

	const code = btn.getAttribute('data-code'); // 👈 FIX QUAN TRỌNG
	const isLoggedIn = btn.getAttribute('data-loggedin') === 'true';

	if (!isLoggedIn) {
		alert("Vui lòng đăng nhập để lưu mã giảm giá này!");
		window.location.href = window.APP_CTX + "/login";
		return;
	}

	btn.disabled = true;
	btn.innerText = "Đang lưu...";

	fetch(window.APP_CTX + '/ajax/apply-coupon?code=' + encodeURIComponent(code))
			.then(res => res.json())
			.then(data => {

				if (data.success) {
					btn.innerText = "Đã lưu";
					btn.classList.add("saved");
					btn.style.backgroundColor = "#ccc";

					alert(data.message + " Bạn được giảm " + data.discount + "đ.");
				} else {
					btn.disabled = false;
					btn.innerText = "Lưu";
					alert("Lỗi: " + data.message);
				}

			})
			.catch(err => {
				console.error(err);
				btn.disabled = false;
				btn.innerText = "Lưu";
				alert("Có lỗi xảy ra, vui lòng thử lại!");
			});
}

function showVoucherDetailFromEl(btn) {
	const code = btn.dataset.code || "";
	const type = btn.dataset.type || "";
	const desc = btn.dataset.desc || "";
	const minOrder = btn.dataset.min || "0";
	const endDate = btn.dataset.end || "";

	showVoucherDetail(code, type, desc, minOrder, endDate);
}

function showVoucherDetail(code, type, desc, minOrder, endDate) {

	console.log("Voucher debug:", { code, type, desc, minOrder, endDate });

	let html = "";

	// 1. LOẠI VOUCHER
	if ((type || "").toUpperCase().includes("SHIP")) {
		html += "🚚 <b>Miễn phí vận chuyển</b><br><br>";
	} else {
		html += "🎟 <b>Voucher giảm giá</b><br><br>";
	}

	// 2. MÃ VOUCHER
	html += "Mã: <b>" + code + "</b><br>";

	// 3. MÔ TẢ
	if (desc && desc.trim() !== "") {
		html += "Mô tả: " + desc + "<br>";
	} else {
		html += "Mô tả: Không có<br>";
	}

	// 4. ĐƠN TỐI THIỂU
	const min = Number(minOrder);
	if (!isNaN(min) && min > 0) {
		html += "Đơn tối thiểu: " + min.toLocaleString('vi-VN') + "₫<br>";
	} else {
		html += "Đơn tối thiểu: 0₫<br>";
	}

	// 5. HẠN SỬ DỤNG 
	if (endDate && endDate.trim() !== "") {
		// Định dạng mặc định từ DB thường là yyyy-MM-dd, ta có thể hiển thị trực tiếp hoặc format lại
		html += "Hạn sử dụng đến ngày: <b style='color: #e11d48;'>" + endDate + "</b><br>";
	} else {
		html += "Hạn sử dụng: Vô thời hạn<br>";
	}

	// CREATE MODAL (Tạo popup hộp thoại)
	const modal = document.createElement("div");
	modal.className = "voucher-modal";

	modal.innerHTML =
			'<div class="voucher-modal-box">' +
				'<div style="line-height:1.6; margin-bottom: 10px;">' +
				html +
				'</div>' +
				'<button style="margin-top:15px; padding:8px 16px; border:none; background:#ff5fa2; color:#fff; border-radius:8px; cursor:pointer;" onclick="this.closest(\'.voucher-modal\').remove()">' +
				'Đóng' +
				'</button>' +
			'</div>';

	document.body.appendChild(modal);
}
</script>

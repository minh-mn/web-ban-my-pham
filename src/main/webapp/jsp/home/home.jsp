<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<script src="${pageContext.request.contextPath}/assets/js/main.js"></script>


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

<section class="section">
	<div class="container">
		<h2 style="text-align: left; margin-bottom: 30px; font-weight: 800;">ƯU ĐÃI ĐỘC QUYỀN</h2>

		<div class="voucher-grid">
			<c:forEach var="voucher" items="${vouchers}" begin="0" end="2">
				<div class="voucher-card">
					<div class="voucher-left">
							${voucher.type == 'FREESHIP' ? '🚚' : '🎟'}
					</div>

					<div class="voucher-right">
						<div>
							<span class="v-code">${voucher.code}</span>
							<span class="v-discount">
                           <c:choose>
							   <c:when test="${voucher.type == 'FREESHIP'}">Miễn phí Ship</c:when>
							   <c:otherwise>Giảm ${voucher.discountPercent}%</c:otherwise>
						   </c:choose>
                       </span>

							<div class="v-info-text">
								<div>Đơn tối thiểu: <b><fmt:formatNumber value="${voucher.minOrderAmount}" type="number"/>đ</b></div>
								<div>Áp dụng: <b>${not empty voucher.applicableProducts ? voucher.applicableProducts : 'Tất cả sản phẩm'}</b></div>
								<div>HSD: <b>${voucher.endDate}</b></div>
							</div>
						</div>

						<div class="btn-wrapper">
							<button type="button" class="btn-detail"
							        onclick="showVoucherDetailFromEl(this)"
							        data-code="${voucher.code}"
							        data-desc="${not empty voucher.description ? voucher.description : 'Không có mô tả'}"
							        data-min="${voucher.minOrderAmount}"
							        data-end="${voucher.endDate}">
								Xem chi tiết
							</button>
							<button class="btn-save" onclick="saveVoucher(this)" data-code="${voucher.code}" data-loggedin="${not empty sessionScope.user}">
								Lưu mã
							</button>
						</div>
					</div>
				</div>
			</c:forEach>
		</div>

		<c:if test="${fn:length(vouchers) > 3}">
			<div style="text-align: center; margin-top: 30px;">
				<a href="${pageContext.request.contextPath}/vouchers" style="color: #d0021b; font-weight: 700; text-decoration: underline;">XEM TẤT CẢ ƯU ĐÃI</a>
			</div>
		</c:if>
	</div>
</section>
<jsp:include page="/jsp/product/hot-categories.jsp" />

<jsp:include page="/jsp/product/flash-sale.jsp">
	<jsp:param name="limit" value="4" />
</jsp:include>

<jsp:include page="/jsp/common/store-events.jsp">
	<jsp:param name="limit" value="3" />
</jsp:include>

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

					<form method="post" action="${pageContext.request.contextPath}/wishlist/toggle" class="wishlist-form" style="margin: 0; display: flex;">						<input type="hidden" name="productId" value="${product.id}" />

						<c:set var="inWishlist" value="${wishlistIds != null && wishlistIds.contains(product.id)}" />

						<button type="submit" class="wishlist-btn ${inWishlist ? 'active' : ''}" title="Thêm vào yêu thích"
						        style="background: #ffffff; width: 44px; border-radius: 8px; border: 1px solid var(--pink-main, #ff5fa2); font-size: 20px; cursor: pointer; color: ${inWishlist ? 'red' : '#ccc'}; transition: all 0.2s; display: flex; align-items: center; justify-content: center; padding: 0; flex-shrink: 0;">
							❤
						</button>
					</form>

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

	// 1. Hàm hiển thị thêm Voucher
	function showMoreVouchers() {
		const hiddenVouchers = document.querySelectorAll('.hidden-voucher');
		hiddenVouchers.forEach(v => v.classList.remove('hidden-voucher'));
		document.getElementById('btn-load-more-vouchers').style.display = 'none';
	}

	// 2. Hàm tạo Popup thông báo custom thay thế alert()
	function showCustomAlert(title, message, isSuccess) {
		const modal = document.createElement("div");
		modal.className = "custom-alert-modal";
		const icon = isSuccess ? "🎉" : "⚠️";
		const color = isSuccess ? "#ff5fa2" : "#e53935";

		modal.innerHTML =
				'<div class="custom-alert-box">' +
				'<div style="font-size: 40px; margin-bottom: 10px;">' + icon + '</div>' +
				'<h3 style="color:' + color + '; margin-bottom: 10px; font-size: 20px;">' + title + '</h3>' +
				'<p style="color: #555; margin-bottom: 20px; line-height: 1.5;">' + message + '</p>' +
				'<button onclick="this.closest(\'.custom-alert-modal\').remove()" ' +
				'style="background: ' + color + '; color: #fff; border: none; padding: 10px 24px; border-radius: 999px; cursor: pointer; font-weight: bold; width: 100%;">Đóng</button>' +
				'</div>';
		document.body.appendChild(modal);
	}

	// 3. Ghi đè lại hàm saveVoucher hiện tại
	function saveVoucher(btn) {
		const code = btn.getAttribute('data-code');
		const isLoggedIn = btn.getAttribute('data-loggedin') === 'true';

		if (!isLoggedIn) {
			showCustomAlert("Chưa đăng nhập", "Vui lòng đăng nhập để lưu mã!", false);
			return;
		}

		btn.innerText = "Đang lưu...";
		btn.disabled = true;

		// THÊM &action=save ĐỂ BÁO VỚI SERVER LÀ CHỈ LƯU VÀO VÍ
		fetch(window.APP_CTX + '/ajax/apply-coupon?code=' + encodeURIComponent(code) + '&action=save')
				.then(res => res.json())
				.then(data => {
					const msg = data.message ? data.message.toLowerCase() : "";

					// Nếu thành công HOẶC đã sở hữu từ trước -> Đổi trạng thái nút thành "Đã lưu"
					if (data.success || msg.includes("đã sở hữu") || msg.includes("đã lưu")) {
						btn.innerText = "Đã lưu";
						btn.classList.add("saved");
						btn.style.backgroundColor = "#ccc";
						btn.style.cursor = "not-allowed";
						showCustomAlert("Thông báo", data.success ? "Lưu mã thành công!" : "Mã này đã có trong ví của bạn.", true);
					} else {
						// Nếu lỗi khác (không phải lỗi trùng), trả lại trạng thái nút
						btn.disabled = false;
						btn.innerText = "Lưu mã";
						showCustomAlert("Lưu thất bại", data.message, false);
					}
				})
				.catch(err => {
					btn.disabled = false;
					btn.innerText = "Lưu mã";
					showCustomAlert("Lỗi", "Có lỗi kết nối, vui lòng thử lại.", false);
				});
	}

	// 4. Hàm hiển thị Chi tiết Voucher
	function showVoucherDetailFromEl(btn) {
		// Lấy dữ liệu từ các thuộc tính data-* của nút
		const code = btn.getAttribute('data-code') || "Không rõ";
		const desc = btn.getAttribute('data-desc') || "Không có mô tả cụ thể.";
		const min = btn.getAttribute('data-min');
		const end = btn.getAttribute('data-end');

		// Format số tiền (thêm dấu chấm phân cách)
		let minText = "0 ₫";
		if (min && parseInt(min) > 0) {
			minText = Number(min).toLocaleString('vi-VN') + " ₫";
		}

		// Format ngày tháng (nếu có)
		let endText = "Không giới hạn";
		if (end && end.trim() !== "") {
			endText = end;
		}

		// Tạo Popup Modal
		const modal = document.createElement("div");
		modal.className = "custom-alert-modal"; // Tận dụng lại CSS class có sẵn để tạo hiệu ứng mờ nền

		modal.innerHTML =
				'<div class="custom-alert-box" style="text-align: left;">' +
				'<div style="font-size: 32px; text-align: center; margin-bottom: 10px;">🎟️</div>' +
				'<h3 style="color: #ff5fa2; margin-bottom: 15px; font-size: 20px; text-align: center;">Chi tiết Ưu đãi</h3>' +

				'<div style="color: #444; line-height: 1.6; font-size: 14px; margin-bottom: 24px; padding: 15px; background: #fff0f6; border-radius: 12px;">' +
				'<p style="margin: 0 0 8px 0;"><strong>Mã code:</strong> <span style="color: #d0021b; font-weight: bold; font-size: 16px;">' + code + '</span></p>' +
				'<p style="margin: 0 0 8px 0;"><strong>Mô tả:</strong> ' + desc + '</p>' +
				'<p style="margin: 0 0 8px 0;"><strong>Đơn tối thiểu:</strong> ' + minText + '</p>' +
				'<p style="margin: 0;"><strong>Hạn sử dụng:</strong> ' + endText + '</p>' +
				'</div>' +

				'<button onclick="this.closest(\'.custom-alert-modal\').remove()" ' +
				'style="background: linear-gradient(135deg, #ff5fa2, #ff85bc); color: #fff; border: none; padding: 12px 24px; border-radius: 999px; cursor: pointer; font-weight: bold; width: 100%; transition: 0.2s;">' +
				'Đã hiểu' +
				'</button>' +
				'</div>';

		// Hiển thị lên màn hình
		document.body.appendChild(modal);
	}

	document.addEventListener("DOMContentLoaded", function() {
		const wishlistForms = document.querySelectorAll(".wishlist-form");

		wishlistForms.forEach(form => {
			form.addEventListener("submit", function(e) {
				e.preventDefault(); // Ngăn form tải lại trang

				const btn = this.querySelector(".wishlist-btn");
				const formData = new URLSearchParams(new FormData(this));

				// Gửi request bằng AJAX
				fetch(this.action, {
					method: "POST",
					headers: {
						"Content-Type": "application/x-www-form-urlencoded"
					},
					body: formData
				})
						.then(response => {
							// Bắt lỗi 401 từ WishlistToggleServlet
							if (response.status === 401) {
								showLoginModal();
								throw new Error("LOGIN_REQUIRED");
							}
							return response.text();
						})
						.then(data => {
							// Cập nhật giao diện nút tim dựa trên phản hồi
							if (data === "ADDED") {
								btn.style.color = "red";
								btn.classList.add("active");
							} else if (data === "REMOVED") {
								btn.style.color = "#ccc";
								btn.classList.remove("active");
							}
						})
						.catch(error => {
							if(error.message !== "LOGIN_REQUIRED"){
								console.error("Lỗi khi thêm vào wishlist:", error);
							}
						});
			});
		});
	});
</script>

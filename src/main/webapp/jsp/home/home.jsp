<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<script src="${pageContext.request.contextPath}/assets/js/main.js"></script>

<!-- =========================================================
HOME HERO / BANNER
Thiết kế tham khảo tinh thần ThegioiSkinFood:
banner rộng, section sale rõ, card sản phẩm dày thông tin.
========================================================= -->
<section class="skin-hero">
	<div class="skin-container">
		<div class="skin-hero-shell">
			<div class="skin-hero-slider">
				<c:choose>
					<c:when test="${not empty banners}">
						<c:forEach var="banner" items="${banners}" varStatus="st">
							<a class="skin-hero-slide ${st.first ? 'active' : ''}"
							   href="${not empty banner.link ? banner.link : '#'}">
								<c:choose>
									<c:when test="${not empty banner.imageUrl}">
										<img src="${ctx}${banner.imageUrl}" alt="${not empty banner.title ? banner.title : 'MyCosmeticShop banner'}">
									</c:when>
									<c:otherwise>
										<div class="skin-hero-fallback">
											<span>MYCOSMETICSHOP</span>
											<strong>Beauty Deals</strong>
										</div>
									</c:otherwise>
								</c:choose>

								<div class="skin-hero-caption">
									<span>MYCOSMETICSHOP</span>
									<strong>${not empty banner.title ? banner.title : 'Ưu đãi mỹ phẩm chính hãng'}</strong>
									<em>Mua sắm nhanh · Ưu đãi tốt · Sản phẩm nổi bật</em>
								</div>
							</a>
						</c:forEach>
					</c:when>
					<c:otherwise>
						<a class="skin-hero-slide active" href="${ctx}/products">
							<div class="skin-hero-fallback">
								<span>MYCOSMETICSHOP</span>
								<strong>Beauty Flash Deal</strong>
							</div>
							<div class="skin-hero-caption">
								<span>WELCOME</span>
								<strong>Khám phá mỹ phẩm nổi bật hôm nay</strong>
								<em>Giảm giá · Bán chạy · Danh mục hot</em>
							</div>
						</a>
					</c:otherwise>
				</c:choose>

				<button type="button" class="skin-hero-nav skin-hero-prev" aria-label="Banner trước">‹</button>
				<button type="button" class="skin-hero-nav skin-hero-next" aria-label="Banner sau">›</button>

				<div class="skin-hero-dots">
					<c:forEach items="${banners}" varStatus="st">
						<button type="button" class="skin-hero-dot ${st.first ? 'active' : ''}" aria-label="Banner ${st.index + 1}"></button>
					</c:forEach>
				</div>
			</div>

			<div class="skin-hero-side">
				<div class="skin-side-card skin-side-hot">
					<span>FLASH</span>
					<strong>Deal đang chạy</strong>
					<small>Ưu tiên các sản phẩm giảm sâu, bán tốt</small>
					<a href="${ctx}/products?sort=discount-desc">Xem ngay</a>
				</div>
				<div class="skin-side-card">
					<span>HOT CATE</span>
					<strong>Danh mục nổi bật</strong>
					<small>Lấy động từ danh mục có nhiều sản phẩm active</small>
					<a href="#hot-categories">Khám phá</a>
				</div>
			</div>
		</div>
	</div>
</section>

<!-- =========================================================
VOUCHER STRIP
========================================================= -->
<c:if test="${not empty vouchers}">
	<section class="skin-voucher-strip">
		<div class="skin-container">
			<div class="skin-voucher-shell">

				<div class="skin-strip-head skin-strip-head--voucher">
					<div>
						<span class="skin-eyebrow">MÃ ƯU ĐÃI</span>
						<h2>Săn mã nhanh</h2>
					</div>

					<div class="skin-strip-actions">
						<span class="skin-strip-note">
							<span></span>
							Ưu đãi nổi bật hôm nay
						</span>
						<a href="${ctx}/vouchers">Xem tất cả mã</a>
					</div>
				</div>

				<div class="skin-voucher-row skin-voucher-row--sync">
					<c:forEach var="voucher" items="${vouchers}" begin="0" end="3">
						<article class="skin-voucher-card skin-voucher-card--sync">
							<div class="skin-voucher-left">
								<div class="skin-voucher-mark">
									<c:choose>
										<c:when test="${voucher.type == 'FREESHIP'}">🚚</c:when>
										<c:otherwise>MC</c:otherwise>
									</c:choose>
								</div>
							</div>

							<div class="skin-voucher-divider"></div>

							<div class="skin-voucher-content">
								<div class="skin-voucher-top">
									<div class="skin-voucher-tags">
										<span class="skin-voucher-tag skin-voucher-tag-hot">HOT</span>
										<c:if test="${voucher.type == 'FREESHIP'}">
											<span class="skin-voucher-tag skin-voucher-tag-soft">FREESHIP</span>
										</c:if>
									</div>
								</div>

								<div class="skin-voucher-code-row">
									<strong>${voucher.code}</strong>

									<button type="button"
											class="skin-save-voucher"
											onclick="saveVoucher(this)"
											data-code="${voucher.code}"
											data-loggedin="${not empty sessionScope.user}">
										Lưu
									</button>
								</div>

								<div class="skin-voucher-discount">
									<c:choose>
										<c:when test="${voucher.type == 'FREESHIP'}">
											Miễn phí vận chuyển
										</c:when>
										<c:otherwise>
											Giảm <b>${voucher.discountPercent}%</b>
										</c:otherwise>
									</c:choose>
								</div>

								<div class="skin-voucher-meta">
									<span>
										Đơn từ
										<b>
											<fmt:formatNumber value="${voucher.minOrderAmount}" type="number" groupingUsed="true"/>đ
										</b>
									</span>
									<span class="dot"></span>
									<span>HSD ${voucher.endDate}</span>
								</div>
							</div>
						</article>
					</c:forEach>
				</div>

			</div>
		</div>
	</section>
</c:if>
									</div>
								</div>

								<div class="skin-voucher-code-row">
									<strong>${voucher.code}</strong>

									<button type="button"
											class="skin-save-voucher"
											onclick="saveVoucher(this)"
											data-code="${voucher.code}"
											data-loggedin="${not empty sessionScope.user}">
										Lưu
									</button>
								</div>

								<div class="skin-voucher-discount">
									<c:choose>
										<c:when test="${voucher.type == 'FREESHIP'}">
											Miễn phí vận chuyển
										</c:when>
										<c:otherwise>
											Giảm <b>${voucher.discountPercent}%</b>
										</c:otherwise>
									</c:choose>
								</div>

								<div class="skin-voucher-meta">
									<span>
										Đơn từ
										<b>
											<fmt:formatNumber value="${voucher.minOrderAmount}" type="number" groupingUsed="true"/>đ
										</b>
									</span>
									<span class="dot"></span>
									<span>HSD ${voucher.endDate}</span>
								</div>
							</div>
						</article>
					</c:forEach>
				</div>

			</div>
		</div>
	</section>
</c:if>

<!-- =========================================================
FLASH DEAL - dùng deepDiscountProducts
========================================================= -->
<c:set var="homeSectionProducts" value="${deepDiscountProducts}" scope="request"/>
<c:set var="homeSectionTitle" value="FLASH DEAL" scope="request"/>
<c:set var="homeSectionDesc" value="Deal nổi bật, giá tốt, số lượng có hạn trong hôm nay." scope="request"/>
<c:set var="homeSectionLink" value="/products?sort=discount-desc" scope="request"/>
<c:set var="homeSectionMode" value="flash" scope="request"/>
<c:set var="homeSectionShowSold" value="${true}" scope="request"/>
<c:set var="homeSectionShowViews" value="${false}" scope="request"/>
<c:set var="homeSectionShowDiscount" value="${true}" scope="request"/>
<jsp:include page="/jsp/product/home-product-section.jsp"/>

<!-- =========================================================
DANH MỤC HOT
========================================================= -->
<jsp:include page="/jsp/product/hot-categories.jsp" />

<!-- =========================================================
SỰ KIỆN HOT - kiểu banner mosaic
========================================================= -->
<section class="skin-events-section">
	<div class="skin-container">
		<div class="skin-section-top">
			<div>
				<span class="skin-eyebrow">HOT EVENT</span>
				<h2>SỰ KIỆN HOT!!!</h2>
			</div>
			<a href="${ctx}/blog">Xem tất cả</a>
		</div>

		<div class="skin-event-mosaic">
			<c:choose>
				<c:when test="${not empty recentEvents}">
					<c:forEach var="event" items="${recentEvents}" varStatus="st">
						<a class="skin-event-tile ${st.first ? 'is-large' : ''}" href="${ctx}/blog/detail?id=${event.id}">
							<c:choose>
								<c:when test="${not empty event.imageUrl}">
									<img src="${ctx}${event.imageUrl}" alt="${event.title}">
								</c:when>
								<c:otherwise>
									<div class="skin-event-fallback">
										<span>${not empty event.tag ? event.tag : 'EVENT'}</span>
										<strong>${event.title}</strong>
									</div>
								</c:otherwise>
							</c:choose>
							<div class="skin-event-info">
								<span>${not empty event.tag ? event.tag : 'SỰ KIỆN'}</span>
								<strong>${event.title}</strong>
								<small>${event.summary}</small>
							</div>
						</a>
					</c:forEach>
				</c:when>
				<c:otherwise>
					<a class="skin-event-tile is-large" href="${ctx}/products?sort=discount-desc">
						<div class="skin-event-fallback pink">
							<span>BEAUTY SALE</span>
							<strong>Ưu đãi mỹ phẩm hôm nay</strong>
						</div>
					</a>
					<a class="skin-event-tile" href="${ctx}/products">
						<div class="skin-event-fallback blue">
							<span>NEW ARRIVAL</span>
							<strong>Sản phẩm mới</strong>
						</div>
					</a>
				</c:otherwise>
			</c:choose>
		</div>
	</div>
</section>

<!-- =========================================================
PRODUCT GROUPS
========================================================= -->
<c:set var="homeSectionProducts" value="${bestSellingProducts}" scope="request"/>
<c:set var="homeSectionTitle" value="BÁN CHẠY" scope="request"/>
<c:set var="homeSectionDesc" value="Các sản phẩm được mua nhiều nhất từ dữ liệu đơn hàng." scope="request"/>
<c:set var="homeSectionLink" value="/products?sort=best-selling" scope="request"/>
<c:set var="homeSectionMode" value="default" scope="request"/>
<c:set var="homeSectionShowSold" value="${true}" scope="request"/>
<c:set var="homeSectionShowViews" value="${false}" scope="request"/>
<c:set var="homeSectionShowDiscount" value="${false}" scope="request"/>
<jsp:include page="/jsp/product/home-product-section.jsp"/>

<c:set var="homeSectionProducts" value="${featuredProducts}" scope="request"/>
<c:set var="homeSectionTitle" value="KHÁM PHÁ" scope="request"/>
<c:set var="homeSectionDesc" value="Sản phẩm nổi bật dựa trên đánh giá, lượt bán và mức độ quan tâm." scope="request"/>
<c:set var="homeSectionLink" value="/products?sort=featured" scope="request"/>
<c:set var="homeSectionMode" value="default" scope="request"/>
<c:set var="homeSectionShowSold" value="${false}" scope="request"/>
<c:set var="homeSectionShowViews" value="${false}" scope="request"/>
<c:set var="homeSectionShowDiscount" value="${false}" scope="request"/>
<jsp:include page="/jsp/product/home-product-section.jsp"/>

<c:set var="homeSectionProducts" value="${mostViewedProducts}" scope="request"/>
<c:set var="homeSectionTitle" value="LƯỢT XEM NHIỀU" scope="request"/>
<c:set var="homeSectionDesc" value="Các sản phẩm được khách hàng ghé xem nhiều trên website." scope="request"/>
<c:set var="homeSectionLink" value="/products?sort=view-desc" scope="request"/>
<c:set var="homeSectionMode" value="default" scope="request"/>
<c:set var="homeSectionShowSold" value="${false}" scope="request"/>
<c:set var="homeSectionShowViews" value="${true}" scope="request"/>
<c:set var="homeSectionShowDiscount" value="${false}" scope="request"/>
<jsp:include page="/jsp/product/home-product-section.jsp"/>

<c:set var="homeSectionProducts" value="${newProducts}" scope="request"/>
<c:set var="homeSectionTitle" value="SẢN PHẨM MỚI" scope="request"/>
<c:set var="homeSectionDesc" value="Các sản phẩm mới nhất vừa được cập nhật trong cửa hàng." scope="request"/>
<c:set var="homeSectionLink" value="/products?sort=created_desc" scope="request"/>
<c:set var="homeSectionMode" value="default" scope="request"/>
<c:set var="homeSectionShowSold" value="${false}" scope="request"/>
<c:set var="homeSectionShowViews" value="${false}" scope="request"/>
<c:set var="homeSectionShowDiscount" value="${false}" scope="request"/>
<jsp:include page="/jsp/product/home-product-section.jsp"/>

<section class="skin-brand-story">
	<div class="skin-container">
		<div class="skin-story-box">
			<span>MYCOSMETICSHOP</span>
			<h2>Chăm sóc da rõ ràng hơn, mua sắm nhanh hơn</h2>
			<p>Trang chủ được chia thành các nhóm sản phẩm rõ ràng: flash deal, bán chạy, giảm sâu, xem nhiều và sản phẩm mới. Khách hàng có thể tìm đúng nhóm sản phẩm cần mua chỉ trong vài giây.</p>
		</div>
	</div>
</section>

<button type="button" class="skin-backtop" aria-label="Lên đầu trang">↑</button>

<script>
	window.APP_CTX = "${ctx}";

	document.addEventListener("DOMContentLoaded", function () {
		initSkinHero();
		initDealCountdown();
		initBackTop();
	});

	function initSkinHero() {
		const slides = document.querySelectorAll(".skin-hero-slide");
		const dots = document.querySelectorAll(".skin-hero-dot");
		const prev = document.querySelector(".skin-hero-prev");
		const next = document.querySelector(".skin-hero-next");
		if (!slides.length) return;

		let index = 0;

		function showSlide(i) {
			slides.forEach(s => s.classList.remove("active"));
			dots.forEach(d => d.classList.remove("active"));
			slides[i].classList.add("active");
			if (dots[i]) dots[i].classList.add("active");
			index = i;
		}

		function nextSlide() {
			showSlide((index + 1) % slides.length);
		}

		function prevSlide() {
			showSlide((index - 1 + slides.length) % slides.length);
		}

		if (next) next.addEventListener("click", nextSlide);
		if (prev) prev.addEventListener("click", prevSlide);
		dots.forEach((dot, i) => dot.addEventListener("click", () => showSlide(i)));

		if (slides.length > 1) {
			setInterval(nextSlide, 5000);
		}
	}

	function initDealCountdown() {
		const boxes = document.querySelectorAll("[data-deal-countdown]");
		if (!boxes.length) return;

		function pad(num) {
			return num < 10 ? "0" + num : String(num);
		}

		function render() {
			const now = new Date();
			const end = new Date();
			end.setHours(23, 59, 59, 999);

			let distance = end.getTime() - now.getTime();
			if (distance < 0) distance = 0;

			const hours = Math.floor(distance / (1000 * 60 * 60));
			const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
			const seconds = Math.floor((distance % (1000 * 60)) / 1000);

			boxes.forEach(box => {
				box.querySelector("[data-hh]").innerText = pad(hours);
				box.querySelector("[data-mm]").innerText = pad(minutes);
				box.querySelector("[data-ss]").innerText = pad(seconds);
			});
		}

		render();
		setInterval(render, 1000);
	}

	function initBackTop() {
		const btn = document.querySelector(".skin-backtop");
		if (!btn) return;

		window.addEventListener("scroll", function () {
			btn.classList.toggle("show", window.scrollY > 400);
		});

		btn.addEventListener("click", function () {
			window.scrollTo({ top: 0, behavior: "smooth" });
		});
	}

	function showCustomAlert(title, message, isSuccess) {
		const modal = document.createElement("div");
		modal.className = "custom-alert-modal";
		const icon = isSuccess ? "🎉" : "⚠️";
		const color = isSuccess ? "#9b0012" : "#e53935";

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

	function saveVoucher(btn) {
		const code = btn.getAttribute("data-code");
		const isLoggedIn = btn.getAttribute("data-loggedin") === "true";

		if (!isLoggedIn) {
			showCustomAlert("Chưa đăng nhập", "Vui lòng đăng nhập để lưu mã!", false);
			return;
		}

		btn.innerText = "Đang lưu...";
		btn.disabled = true;

		fetch(window.APP_CTX + "/ajax/apply-coupon?code=" + encodeURIComponent(code) + "&action=save")
				.then(res => res.json())
				.then(data => {
					const msg = data.message ? data.message.toLowerCase() : "";
					if (data.success || msg.includes("đã sở hữu") || msg.includes("đã lưu")) {
						btn.innerText = "Đã lưu";
						btn.classList.add("saved");
						showCustomAlert("Thông báo", data.success ? "Lưu mã thành công!" : "Mã này đã có trong ví của bạn.", true);
					} else {
						btn.disabled = false;
						btn.innerText = "Lưu";
						showCustomAlert("Lưu thất bại", data.message || "Không thể lưu mã.", false);
					}
				})
				.catch(() => {
					btn.disabled = false;
					btn.innerText = "Lưu";
					showCustomAlert("Lỗi", "Có lỗi kết nối, vui lòng thử lại.", false);
				});
	}

	// Hàm tương thích với giao diện voucher cũ nếu còn nút Xem chi tiết trong fragment khác
	function showVoucherDetailFromEl(btn) {
		const code = btn.getAttribute("data-code") || "Không rõ";
		const desc = btn.getAttribute("data-desc") || "Không có mô tả cụ thể.";
		const min = btn.getAttribute("data-min");
		const end = btn.getAttribute("data-end");

		let minText = "0 ₫";
		if (min && parseInt(min) > 0) {
			minText = Number(min).toLocaleString("vi-VN") + " ₫";
		}

		const endText = end && end.trim() !== "" ? end : "Không giới hạn";

		const modal = document.createElement("div");
		modal.className = "custom-alert-modal";
		modal.innerHTML =
				'<div class="custom-alert-box" style="text-align: left;">' +
				'<div style="font-size: 32px; text-align: center; margin-bottom: 10px;">🎟️</div>' +
				'<h3 style="color: #9b0012; margin-bottom: 15px; font-size: 20px; text-align: center;">Chi tiết ưu đãi</h3>' +
				'<div style="color: #444; line-height: 1.6; font-size: 14px; margin-bottom: 24px; padding: 15px; background: #fff0f6; border-radius: 12px;">' +
				'<p style="margin: 0 0 8px 0;"><strong>Mã code:</strong> <span style="color: #9b0012; font-weight: bold; font-size: 16px;">' + code + '</span></p>' +
				'<p style="margin: 0 0 8px 0;"><strong>Mô tả:</strong> ' + desc + '</p>' +
				'<p style="margin: 0 0 8px 0;"><strong>Đơn tối thiểu:</strong> ' + minText + '</p>' +
				'<p style="margin: 0;"><strong>Hạn sử dụng:</strong> ' + endText + '</p>' +
				'</div>' +
				'<button onclick="this.closest(\'.custom-alert-modal\').remove()" ' +
				'style="background: linear-gradient(135deg, #8f001f, #d44d79); color: #fff; border: none; padding: 12px 24px; border-radius: 999px; cursor: pointer; font-weight: bold; width: 100%;">' +
				'Đã hiểu' +
				'</button>' +
				'</div>';

		document.body.appendChild(modal);
	}

	// Giữ lại AJAX wishlist từ file cũ để các product card cũ hoặc fragment khác vẫn hoạt động
	document.addEventListener("DOMContentLoaded", function () {
		const wishlistForms = document.querySelectorAll(".wishlist-form");
		if (!wishlistForms.length) return;

		wishlistForms.forEach(form => {
			form.addEventListener("submit", function (e) {
				e.preventDefault();

				const btn = this.querySelector(".wishlist-btn");
				const formData = new URLSearchParams(new FormData(this));

				fetch(this.action, {
					method: "POST",
					headers: { "Content-Type": "application/x-www-form-urlencoded" },
					body: formData
				})
						.then(response => {
							if (response.status === 401) {
								if (typeof showLoginModal === "function") {
									showLoginModal();
								} else {
									showCustomAlert("Chưa đăng nhập", "Vui lòng đăng nhập để thêm sản phẩm vào yêu thích.", false);
								}
								throw new Error("LOGIN_REQUIRED");
							}
							return response.text();
						})
						.then(data => {
							if (!btn) return;
							if (data === "ADDED") {
								btn.style.color = "red";
								btn.classList.add("active");
							} else if (data === "REMOVED") {
								btn.style.color = "#ccc";
								btn.classList.remove("active");
							}
						})
						.catch(error => {
							if (error.message !== "LOGIN_REQUIRED") {
								console.error("Lỗi khi thêm vào wishlist:", error);
							}
						});
			});
		});
	});

</script>

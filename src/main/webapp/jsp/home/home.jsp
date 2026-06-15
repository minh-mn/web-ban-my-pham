<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<script src="${ctx}/assets/js/main.js"></script>

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
					<a href="${ctx}/flash-sale">Xem ngay</a>
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
						<a href="${ctx}/vouchers">Xem tất cả voucher</a>
					</div>
				</div>

				<div class="skin-voucher-row skin-voucher-row--sync skin-voucher-row--full">
					<c:forEach var="voucher" items="${vouchers}" begin="0" end="1">
						<c:set var="savedNeedle" value=",${voucher.code}," />
						<c:set var="isSaved" value="${not empty savedCodes and fn:contains(savedCodes, savedNeedle)}" />
						<c:set var="isRankVoucher" value="${not empty voucher.type and voucher.type eq 'RANK'}" />
						<c:set var="isReviewVoucher" value="${not empty voucher.type and voucher.type eq 'REVIEW_REWARD'}" />
						<c:set var="scope" value="${voucher.applyScope}" />

						<article class="skin-voucher-card skin-voucher-card--sync skin-voucher-card--full">
							<div class="skin-voucher-left">
								<div class="skin-voucher-mark skin-voucher-mark--beauty">
									<span>MC</span>
									<small>BEAUTY</small>
								</div>
							</div>

							<div class="skin-voucher-divider"></div>

							<div class="skin-voucher-content">
								<div class="skin-voucher-top">
									<div class="skin-voucher-tags">
										<span class="skin-voucher-tag skin-voucher-tag-hot">HOT</span>
										<c:choose>
											<c:when test="${isRankVoucher}">
												<span class="skin-voucher-tag skin-voucher-tag-soft">Hạng thành viên</span>
											</c:when>
											<c:when test="${isReviewVoucher}">
												<span class="skin-voucher-tag skin-voucher-tag-soft">Quà đánh giá</span>
											</c:when>
											<c:when test="${voucher.type eq 'FREESHIP'}">
												<span class="skin-voucher-tag skin-voucher-tag-soft">Freeship</span>
											</c:when>
											<c:otherwise>
												<span class="skin-voucher-tag skin-voucher-tag-soft">Ưu đãi</span>
											</c:otherwise>
										</c:choose>
									</div>

									<button type="button"
									        class="skin-voucher-link-btn"
									        onclick="showVoucherDetailFromEl(this)"
									        data-code="<c:out value='${voucher.code}'/>"
									        data-desc="<c:out value='${not empty voucher.description ? voucher.description : "Không có mô tả"}'/>"
									        data-min="${voucher.minOrderAmount}"
									        data-end="${voucher.endDate}">
										Điều kiện
									</button>
								</div>

								<div class="skin-voucher-code-row">
									<strong><c:out value="${voucher.code}" /></strong>

									<c:choose>
										<c:when test="${isSaved}">
											<button type="button"
											        class="skin-save-voucher saved"
											        data-code="<c:out value='${voucher.code}'/>"
											        data-loggedin="${not empty sessionScope.user}"
											        disabled>
												Đã lưu
											</button>
										</c:when>
										<c:otherwise>
											<button type="button"
											        class="skin-save-voucher"
											        onclick="saveVoucher(this)"
											        data-code="<c:out value='${voucher.code}'/>"
											        data-loggedin="${not empty sessionScope.user}">
												Lưu mã
											</button>
										</c:otherwise>
									</c:choose>
								</div>

								<div class="skin-voucher-discount">
									<c:choose>
										<c:when test="${voucher.type eq 'FREESHIP'}">
											Miễn phí vận chuyển
										</c:when>
										<c:when test="${voucher.percentDiscount}">
											Giảm <b>${voucher.discountPercent}%</b>
										</c:when>
										<c:otherwise>
											Giảm <b><fmt:formatNumber value="${voucher.discountValue}" type="number"/>đ</b>
										</c:otherwise>
									</c:choose>
								</div>

								<div class="skin-voucher-meta-list">
									<div class="skin-voucher-meta-item">
										<span class="dot"></span>
										<span>
											Đơn hàng từ
											<b><fmt:formatNumber value="${voucher.minOrderAmount}" type="number" groupingUsed="true"/>đ</b>
										</span>
									</div>

									<c:if test="${not empty voucher.maxDiscountAmount and voucher.maxDiscountAmount > 0}">
										<div class="skin-voucher-meta-item">
											<span class="dot"></span>
											<span>
												Giảm tối đa
												<b><fmt:formatNumber value="${voucher.maxDiscountAmount}" type="number" groupingUsed="true"/>đ</b>
											</span>
										</div>
									</c:if>

									<div class="skin-voucher-meta-item">
										<span class="dot"></span>
										<span>
											Áp dụng:
											<b>
												<c:choose>
													<c:when test="${empty scope or scope eq 'ALL'}">Tất cả sản phẩm</c:when>
													<c:when test="${scope eq 'BRAND'}">Theo thương hiệu</c:when>
													<c:when test="${scope eq 'PRODUCTS'}">Sản phẩm chỉ định</c:when>
													<c:otherwise><c:out value="${scope}" /></c:otherwise>
												</c:choose>
											</b>
										</span>
									</div>

									<c:if test="${not empty voucher.minRankCode and voucher.minRankCode ne 'MEMBER'}">
										<div class="skin-voucher-meta-item">
											<span class="dot"></span>
											<span>Hạng tối thiểu: <b><c:out value="${voucher.minRankCode}" /></b></span>
										</div>
									</c:if>
								</div>

								<c:if test="${not empty voucher.description}">
									<p class="skin-voucher-desc"><c:out value="${voucher.description}" /></p>
								</c:if>

								<div class="skin-voucher-footer">
									<div class="skin-voucher-expire">
										HSD: <b><c:out value="${voucher.endDate}" /></b>
									</div>

									<button type="button"
									        class="skin-voucher-link-btn"
									        onclick="showVoucherDetailFromEl(this)"
									        data-code="<c:out value='${voucher.code}'/>"
									        data-desc="<c:out value='${not empty voucher.description ? voucher.description : "Không có mô tả"}'/>"
									        data-min="${voucher.minOrderAmount}"
									        data-end="${voucher.endDate}">
										Xem chi tiết
									</button>
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
FLASH DEAL
Chỉ hiển thị vào ngày đôi theo tháng và ngày 25 mỗi tháng.
shouldShowFlashDeal được truyền từ HomeServlet.
========================================================= -->
<c:if test="${shouldShowFlashDeal == true}">
	<c:choose>
		<c:when test="${not empty flashSaleProducts}">
			<c:set var="homeSectionProducts" value="${flashSaleProducts}" scope="request"/>
		</c:when>
		<c:otherwise>
			<c:set var="homeSectionProducts" value="${deepDiscountProducts}" scope="request"/>
		</c:otherwise>
	</c:choose>
	<c:set var="homeSectionTitle" value="FLASH DEAL" scope="request"/>
	<c:set var="homeSectionDesc" value="Deal nổi bật, giá tốt, số lượng có hạn trong hôm nay." scope="request"/>
	<c:set var="homeSectionLink" value="/flash-sale" scope="request"/>
	<c:set var="homeSectionViewAllText" value="XEM TẤT CẢ DEAL" scope="request"/>
	<c:set var="homeSectionMode" value="flash" scope="request"/>
	<c:set var="homeSectionShowSold" value="${true}" scope="request"/>
	<c:set var="homeSectionShowViews" value="${false}" scope="request"/>
	<c:set var="homeSectionShowDiscount" value="${true}" scope="request"/>
	<jsp:include page="/jsp/product/home-product-section.jsp"/>
</c:if>

<!-- =========================================================
DANH MỤC HOT
========================================================= -->
<jsp:include page="/jsp/product/hot-categories.jsp" />

<!-- =========================================================
KHÁM PHÁ
Thay thế block SỰ KIỆN HOT bằng sản phẩm khám phá theo layout danh mục hot.
========================================================= -->
<c:choose>
	<c:when test="${not empty discoverProducts}">
		<c:set var="homeSectionProducts" value="${discoverProducts}" scope="request"/>
	</c:when>
	<c:otherwise>
		<c:set var="homeSectionProducts" value="${featuredProducts}" scope="request"/>
	</c:otherwise>
</c:choose>
<c:set var="homeSectionTitle" value="KHÁM PHÁ" scope="request"/>
<c:set var="homeSectionDesc" value="" scope="request"/>
<c:set var="homeSectionLink" value="/products?sort=featured" scope="request"/>
<c:set var="homeSectionViewAllText" value="XEM TẤT CẢ" scope="request"/>
<c:set var="homeSectionMode" value="discover" scope="request"/>
<c:set var="homeSectionShowSold" value="${true}" scope="request"/>
<c:set var="homeSectionShowViews" value="${false}" scope="request"/>
<c:set var="homeSectionShowDiscount" value="${true}" scope="request"/>
<jsp:include page="/jsp/product/home-product-section.jsp"/>

<!-- =========================================================
THƯƠNG HIỆU NỔI BẬT
Layout theo mẫu: banner lớn bên trái, logo thương hiệu + sản phẩm nổi bật bên phải.
========================================================= -->
<c:set var="featuredBrandList" value="${not empty featuredHomeBrands ? featuredHomeBrands : brands}" />
<c:set var="featuredBrandProductList" value="${not empty featuredBrandProducts ? featuredBrandProducts : featuredProducts}" />

<c:if test="${not empty featuredBrandList}">
	<section class="skin-featured-brand-section" id="featured-brands">
		<style>
			.skin-featured-brand-section {
				padding: 52px 0 58px;
				background: linear-gradient(180deg, #fff 0%, #fff7fa 100%);
			}

			.skin-featured-brand-layout {
				display: grid;
				grid-template-columns: minmax(320px, 440px) minmax(0, 1fr);
				gap: 32px;
				align-items: start;
			}

			.skin-featured-brand-banner {
				position: sticky;
				top: 118px;
				display: block;
				min-height: 620px;
				overflow: hidden;
				background: linear-gradient(135deg, #ffe1eb 0%, #fff7fa 46%, #ffd0e2 100%);
				text-decoration: none;
				box-shadow: 0 18px 45px rgba(176, 18, 57, .14);
			}

			.skin-featured-brand-banner img {
				width: 100%;
				height: 100%;
				min-height: 620px;
				object-fit: cover;
				display: block;
			}

			.skin-featured-brand-banner-fallback {
				position: relative;
				min-height: 620px;
				display: flex;
				flex-direction: column;
				align-items: center;
				justify-content: center;
				padding: 46px 34px;
				overflow: hidden;
				text-align: center;
				color: #9b001c;
				background:
						radial-gradient(circle at 18% 14%, rgba(255,255,255,.92), rgba(255,255,255,0) 34%),
						radial-gradient(circle at 86% 88%, rgba(255,210,226,.9), rgba(255,210,226,0) 32%),
						linear-gradient(180deg, #fbecf2 0%, #f7dde7 100%);
				border: 1px solid rgba(176, 18, 57, .10);
			}

			.skin-featured-brand-banner-fallback::before,
			.skin-featured-brand-banner-fallback::after {
				width: 250px;
				height: 250px;
				right: -92px;
				bottom: -74px;
				background: radial-gradient(circle, rgba(255,206,222,.95) 0%, rgba(255,206,222,0) 72%);
			}

			.skin-featured-brand-banner-fallback::before {
				width: 220px;
				height: 220px;
				left: -86px;
				top: -44px;
				background: radial-gradient(circle, rgba(255,255,255,.98) 0%, rgba(255,255,255,0) 72%);
			}

			.skin-featured-brand-banner-fallback::after {
				width: 240px;
				height: 240px;
				right: -92px;
				bottom: -78px;
				background: radial-gradient(circle, rgba(255,210,226,.85) 0%, rgba(255,210,226,0) 72%);
			}

			.skin-featured-brand-banner-fallback span {
				position: relative;
				z-index: 1;
				display: inline-flex;
				align-items: center;
				justify-content: center;
				padding: 7px 14px;
				border-radius: 999px;
				background: rgba(255,255,255,.76);
				font-size: 13px;
				font-weight: 950;
				letter-spacing: .24em;
				text-transform: uppercase;
				box-shadow: 0 10px 26px rgba(176, 18, 57, .08);
			}

			.skin-featured-brand-banner-fallback strong {
				position: relative;
				z-index: 1;
				display: block;
				margin-top: 22px;
				font-size: clamp(38px, 5vw, 72px);
				font-weight: 1000;
				line-height: .92;
				letter-spacing: .015em;
				text-transform: uppercase;
				text-shadow: 0 10px 22px rgba(176, 18, 57, .08);
			}

			.skin-featured-brand-banner-fallback em {
				position: relative;
				z-index: 1;
				display: inline-flex;
				align-items: center;
				justify-content: center;
				margin-top: 24px;
				padding: 12px 22px;
				border-radius: 999px;
				background: rgba(255,255,255,.90);
				color: #b01239;
				font-size: 13px;
				font-style: normal;
				font-weight: 900;
				letter-spacing: .06em;
				text-transform: uppercase;
				box-shadow: 0 14px 30px rgba(176, 18, 57, .12);
			}

			.skin-featured-brand-main {
				min-width: 0;
			}

			.skin-featured-brand-head {
				display: flex;
				align-items: center;
				justify-content: space-between;
				gap: 18px;
				margin-bottom: 18px;
			}

			.skin-featured-brand-title {
				margin: 0;
				display: inline-block;
				width: fit-content;
				padding: 8px 0 10px;
				overflow: visible;
				color: #111;
				font-size: clamp(24px, 2.25vw, 34px);
				font-weight: 950;
				letter-spacing: .045em;
				line-height: 1.28;
				text-transform: uppercase;
				background: none;
				-webkit-text-fill-color: currentColor;
				text-shadow: none;
			}

			.skin-featured-brand-more {
				flex: 0 0 auto;
				color: #b01239;
				font-size: 17px;
				font-weight: 850;
				text-decoration: none;
			}

			.skin-featured-brand-more:hover {
				text-decoration: underline;
				text-underline-offset: 4px;
			}

			.skin-featured-brand-logo-row {
				display: grid;
				grid-template-columns: repeat(6, minmax(0, 1fr));
				align-items: center;
				min-height: 64px;
				overflow: hidden;
				border: 2px solid #b01239;
				border-radius: 999px;
				background: #fff7fa;
				box-shadow: 0 12px 28px rgba(176, 18, 57, .10);
			}

			.skin-featured-brand-logo {
				appearance: none;
				-webkit-appearance: none;
				min-width: 0;
				width: 100%;
				height: 64px;
				display: flex;
				align-items: center;
				justify-content: center;
				padding: 10px 14px;
				border: 0;
				border-right: 1px solid rgba(176, 18, 57, .12);
				background: #fff;
				color: #b01239;
				font: inherit;
				font-size: 16px;
				font-weight: 950;
				letter-spacing: .04em;
				text-align: center;
				text-decoration: none;
				text-transform: uppercase;
				cursor: pointer;
				transition: background .2s ease, color .2s ease, box-shadow .2s ease, transform .2s ease;
			}

			.skin-featured-brand-logo:last-child {
				border-right: 0;
			}

			.skin-featured-brand-logo.is-active,
			.skin-featured-brand-logo:hover {
				background: #b01239;
				color: #fff;
				box-shadow: inset 0 0 0 999px rgba(255, 255, 255, .02), 0 10px 22px rgba(176, 18, 57, .18);
			}

			.skin-featured-brand-logo:hover {
				transform: translateY(-1px);
			}

			.skin-featured-brand-logo img {
				max-width: 100%;
				max-height: 34px;
				object-fit: contain;
				display: block;
				filter: grayscale(1) contrast(1.1);
				transition: filter .22s ease, transform .22s ease;
			}

			.skin-featured-brand-logo.is-active img,
			.skin-featured-brand-logo:hover img {
				filter: grayscale(0) contrast(1) brightness(0) invert(1);
				transform: scale(1.04);
			}

			.skin-featured-brand-logo-fallback {
				display: -webkit-box;
				overflow: hidden;
				-webkit-line-clamp: 1;
				-webkit-box-orient: vertical;
			}

			.skin-featured-brand-products {
				display: grid;
				grid-template-columns: repeat(4, minmax(0, 1fr));
				margin-top: 16px;
				gap: 18px;
				background: transparent;
				align-items: start;
			}

			.skin-featured-brand-card {
				position: relative;
				min-width: 0;
				padding-bottom: 18px;
				background: #fff;
				border: 1px solid #f0d1db;
				transition: box-shadow .22s ease, transform .22s ease, border-color .22s ease;
			}

			.skin-featured-brand-card.is-hidden-by-brand {
				display: none;
			}

			.skin-featured-brand-card:hover {
				z-index: 2;
				transform: translateY(-3px);
				border-color: #efc3cf;
				box-shadow: 0 18px 36px rgba(176, 18, 57, .12);
			}

			.skin-featured-brand-image {
				position: relative;
				display: block;
				height: 190px;
				overflow: hidden;
				background: radial-gradient(circle at 50% 32%, rgba(255, 229, 238, .95), transparent 42%), #fff7fa;
			}

			.skin-featured-brand-image img {
				width: 100%;
				height: 100%;
				object-fit: cover;
				display: block;
			}

			.skin-featured-brand-section .skin-featured-brand-discount,
			.skin-featured-brand-discount {
				position: absolute;
				right: 12px;
				bottom: 12px;
				z-index: 3;
				width: 48px;
				height: 48px;
				display: inline-flex;
				align-items: center;
				justify-content: center;
				border-radius: 999px;
				background: #a8d83f !important;
				background-color: #a8d83f !important;
				color: #fff !important;
				border: 0 !important;
				font-size: 13px;
				font-weight: 950;
				box-shadow: none !important;
			}

			.skin-featured-brand-section .skin-discount-bubble {
				background: #a8d83f !important;
				background-color: #a8d83f !important;
				color: #fff !important;
				box-shadow: none !important;
			}


			.skin-featured-brand-body {
				padding: 14px 14px 0;
			}

			.skin-featured-brand-name {
				display: block;
				margin-bottom: 7px;
				color: #b01239;
				font-size: 15px;
				font-weight: 950;
				letter-spacing: .05em;
				text-transform: uppercase;
			}

			.skin-featured-brand-product-title {
				display: -webkit-box;
				min-height: 56px;
				overflow: hidden;
				color: #1d1d1d;
				font-size: 16px;
				font-weight: 650;
				line-height: 1.45;
				text-decoration: none;
				-webkit-line-clamp: 2;
				-webkit-box-orient: vertical;
			}

			.skin-featured-brand-product-title:hover {
				color: #b01239;
			}

			.skin-featured-brand-price {
				margin-top: 9px;
				display: flex;
				flex-wrap: wrap;
				align-items: baseline;
				gap: 8px;
			}

			.skin-featured-brand-price strong {
				color: #a90027;
				font-size: 16px;
				font-weight: 950;
			}

			.skin-featured-brand-price del {
				color: #8d8d8d;
				font-size: 14px;
			}

			.skin-featured-brand-sold {
				margin-top: 8px;
				color: #111;
				font-size: 14px;
				font-weight: 650;
			}

			.skin-featured-brand-actions {
				display: grid;
				grid-template-columns: 1fr 1fr;
				gap: 10px;
				margin-top: 14px;
			}

			.skin-featured-brand-view-btn,
			.skin-featured-brand-cart-btn {
				width: 100%;
				min-height: 40px;
				display: inline-flex;
				align-items: center;
				justify-content: center;
				padding: 9px 10px;
				border-radius: 999px;
				font-size: 13px;
				font-weight: 900;
				text-align: center;
				text-decoration: none;
				cursor: pointer;
				transition: background .2s ease, color .2s ease, border-color .2s ease, opacity .2s ease;
			}

			.skin-featured-brand-view-btn {
				border: 1px solid rgba(176, 18, 57, .18);
				background: #fff;
				color: #b01239;
			}

			.skin-featured-brand-view-btn:hover {
				background: #fff4f8;
				color: #9a0027;
				transform: translateY(-1px);
			}

			.skin-featured-brand-cart-form {
				margin: 0;
			}

			.skin-featured-brand-cart-btn {
				border: 0;
				background: linear-gradient(135deg, #ff4f97 0%, #d9154f 48%, #a4041b 100%);
				color: #fff;
				box-shadow: 0 10px 20px rgba(176, 18, 57, .18);
			}

			.skin-featured-brand-cart-btn:hover {
				transform: translateY(-1px);
				box-shadow: 0 14px 26px rgba(176, 18, 57, .24);
			}

			.skin-featured-brand-cart-btn:disabled {
				opacity: .45;
				cursor: not-allowed;
			}

			.skin-featured-brand-empty {
				display: none;
				margin-top: 16px;
				padding: 22px;
				border: 1px dashed #efc3cf;
				background: #fff7fa;
				color: #8d001f;
				font-weight: 850;
				text-align: center;
			}

			.skin-featured-brand-view-more {
				display: none;
				justify-content: flex-end;
				margin-top: 24px;
				width: 100%;
			}

			.skin-featured-brand-view-more.is-show {
				display: flex;
			}

			.skin-featured-brand-view-more a {
				min-width: 190px;
				min-height: 44px;
				display: inline-flex;
				align-items: center;
				justify-content: center;
				padding: 10px 24px;
				border-radius: 999px;
				background: linear-gradient(135deg, #ff4f97 0%, #d9154f 48%, #a4041b 100%);
				color: #fff;
				font-size: 16px;
				font-weight: 900;
				text-decoration: none;
				box-shadow: 0 12px 24px rgba(176, 18, 57, .20);
			}

			.skin-featured-brand-view-more a:hover {
				transform: translateY(-1px);
				box-shadow: 0 16px 28px rgba(176, 18, 57, .26);
			}

			@media (max-width: 1180px) {
				.skin-featured-brand-layout {
					grid-template-columns: 1fr;
				}

				.skin-featured-brand-banner {
					position: relative;
					top: auto;
					min-height: 320px;
				}

				.skin-featured-brand-banner img,
				.skin-featured-brand-banner-fallback {
					min-height: 320px;
				}
			}

			@media (max-width: 900px) {
				.skin-featured-brand-logo-row {
					grid-template-columns: repeat(3, minmax(0, 1fr));
					border-radius: 28px;
				}

				.skin-featured-brand-products {
					grid-template-columns: repeat(2, minmax(0, 1fr));
				}
			}

			@media (max-width: 560px) {
				.skin-featured-brand-section {
					padding: 38px 0 44px;
				}

				.skin-featured-brand-head {
					align-items: flex-start;
					flex-direction: column;
				}

				.skin-featured-brand-logo-row,
				.skin-featured-brand-products {
					grid-template-columns: 1fr;
				}

				.skin-featured-brand-actions {
					grid-template-columns: 1fr;
				}
			}
		</style>

		<div class="skin-featured-brand-layout">
			<a class="skin-featured-brand-banner" href="${ctx}/events">
				<c:choose>
					<c:when test="${not empty recentEvents && not empty recentEvents[0].imageUrl}">
						<img src="${ctx}${recentEvents[0].imageUrl}" alt="${recentEvents[0].title}">
					</c:when>
					<c:otherwise>
						<div class="skin-featured-brand-banner-fallback">
							<span>MyCosmetic</span>
							<strong>Beauty<br>Brand<br>Picks</strong>
							<em>Chọn lọc thương hiệu nổi bật</em>
						</div>
					</c:otherwise>
				</c:choose>
			</a>

			<div class="skin-featured-brand-main">
				<div class="skin-featured-brand-head">
					<h2 class="skin-featured-brand-title">THƯƠNG HIỆU NỔI BẬT</h2>
				</div>

				<div class="skin-featured-brand-logo-row" data-brand-tabs>
					<c:forEach var="brand" items="${featuredBrandList}" begin="0" end="5" varStatus="brandStatus">
						<button type="button"
						        class="skin-featured-brand-logo ${brandStatus.first ? 'is-active' : ''}"
						        data-brand-filter="${brand.id}"
						        data-brand-name="${brand.name}"
						        aria-pressed="${brandStatus.first ? 'true' : 'false'}"
						        title="${brand.name}">
							<c:choose>
								<c:when test="${not empty brand.imageUrl}">
									<c:choose>
										<c:when test="${fn:startsWith(brand.imageUrl, 'http')}">
											<img src="${brand.imageUrl}" alt="${brand.name}" loading="lazy">
										</c:when>
										<c:when test="${fn:startsWith(brand.imageUrl, '/')}">
											<img src="${ctx}${brand.imageUrl}" alt="${brand.name}" loading="lazy">
										</c:when>
										<c:otherwise>
											<img src="${ctx}/uploads/brand/${brand.imageUrl}" alt="${brand.name}" loading="lazy">
										</c:otherwise>
									</c:choose>
								</c:when>
								<c:otherwise>
									<span class="skin-featured-brand-logo-fallback">${brand.name}</span>
								</c:otherwise>
							</c:choose>
						</button>
					</c:forEach>
				</div>

				<div class="skin-featured-brand-products" data-brand-products>
					<c:forEach var="product" items="${featuredBrandProductList}">
						<c:choose>
							<c:when test="${not empty product.slug}">
								<c:set var="brandProductUrl" value="${ctx}/product/${product.slug}?id=${product.id}" />
							</c:when>
							<c:otherwise>
								<c:set var="brandProductUrl" value="${ctx}/product?id=${product.id}" />
							</c:otherwise>
						</c:choose>

						<article class="skin-featured-brand-card"
						         data-brand-product
						         data-brand-id="${product.brandId}">
							<a class="skin-featured-brand-image" href="${brandProductUrl}">
								<c:if test="${product.discountPercent > 0}">
									<span class="skin-featured-brand-discount">-${product.discountPercent}%</span>
								</c:if>

								<c:choose>
									<c:when test="${not empty product.imageUrl}">
										<c:choose>
											<c:when test="${fn:startsWith(product.imageUrl, 'http')}">
												<img src="${product.imageUrl}" alt="${product.title}" loading="lazy">
											</c:when>
											<c:when test="${fn:startsWith(product.imageUrl, '/')}">
												<img src="${ctx}${product.imageUrl}" alt="${product.title}" loading="lazy">
											</c:when>
											<c:otherwise>
												<img src="${ctx}/uploads/product/${product.imageUrl}" alt="${product.title}" loading="lazy">
											</c:otherwise>
										</c:choose>
									</c:when>
									<c:otherwise>
										<div class="skin-no-image">No image</div>
									</c:otherwise>
								</c:choose>
							</a>

							<div class="skin-featured-brand-body">
								<c:if test="${not empty product.brandName}">
									<span class="skin-featured-brand-name">${product.brandName}</span>
								</c:if>

								<a class="skin-featured-brand-product-title" href="${brandProductUrl}">
										${product.title}
								</a>

								<div class="skin-featured-brand-price">
									<c:choose>
										<c:when test="${product.discountPercent > 0}">
											<strong><fmt:formatNumber value="${product.finalPrice}" type="number" groupingUsed="true"/>đ</strong>
											<del><fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>đ</del>
										</c:when>
										<c:otherwise>
											<strong><fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>đ</strong>
										</c:otherwise>
									</c:choose>
								</div>

								<c:if test="${product.soldQuantity > 0}">
									<div class="skin-featured-brand-sold">${product.soldQuantity} đã bán</div>
								</c:if>

								<div class="skin-featured-brand-actions">
									<a class="skin-featured-brand-view-btn" href="${brandProductUrl}">
										Xem sản phẩm
									</a>

									<form method="post" action="${ctx}/cart/add" class="skin-featured-brand-cart-form">
										<input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
										<input type="hidden" name="productId" value="${product.id}">
										<input type="hidden" name="quantity" value="1">
										<input type="hidden" name="quickAdd" value="1">
										<button type="submit" class="skin-featured-brand-cart-btn" ${product.stock <= 0 ? 'disabled' : ''}>
											Thêm giỏ
										</button>
									</form>
								</div>
							</div>
						</article>
					</c:forEach>
				</div>

				<div class="skin-featured-brand-empty" data-brand-empty>
					Thương hiệu này hiện chưa có sản phẩm nổi bật.
				</div>

				<div class="skin-featured-brand-view-more" data-brand-view-more>
					<a href="${ctx}/products" data-brand-more-link>Xem thêm →</a>
				</div>
			</div>
		</div>

		<script>
			(function () {
				const SECTION_SELECTOR = ".skin-featured-brand-section";
				const BRAND_SELECTOR = "[data-brand-filter]";
				const PRODUCT_SELECTOR = "[data-brand-product]";
				const MAX_VISIBLE = 8;

				function stopNavigation(event) {
					if (!event) {
						return;
					}

					event.preventDefault();
					event.stopPropagation();

					if (typeof event.stopImmediatePropagation === "function") {
						event.stopImmediatePropagation();
					}
				}

				function setActiveBrand(section, activeBrand) {
					section.querySelectorAll(BRAND_SELECTOR).forEach(function (brand) {
						const isActive = brand === activeBrand;
						brand.classList.toggle("is-active", isActive);
						brand.setAttribute("aria-pressed", isActive ? "true" : "false");
					});
				}

				function updateProducts(section, activeBrand) {
					const brandId = activeBrand ? String(activeBrand.dataset.brandFilter || "") : "";
					const products = Array.from(section.querySelectorAll(PRODUCT_SELECTOR));
					const emptyBox = section.querySelector("[data-brand-empty]");
					const moreBox = section.querySelector("[data-brand-view-more]");
					const moreLink = section.querySelector("[data-brand-more-link]");

					let matchedCount = 0;

					products.forEach(function (product) {
						const productBrandId = String(product.dataset.brandId || "");
						const isMatch = brandId !== "" && productBrandId === brandId;

						if (!isMatch) {
							product.classList.add("is-hidden-by-brand");
							return;
						}

						matchedCount += 1;

						if (matchedCount <= MAX_VISIBLE) {
							product.classList.remove("is-hidden-by-brand");
						} else {
							product.classList.add("is-hidden-by-brand");
						}
					});

					if (emptyBox) {
						emptyBox.style.display = matchedCount === 0 ? "block" : "none";
					}

					if (moreBox && moreLink) {
						if (matchedCount > MAX_VISIBLE) {
							moreLink.href = "${ctx}/products?brand=" + encodeURIComponent(brandId);
							moreBox.classList.add("is-show");
						} else {
							moreBox.classList.remove("is-show");
						}
					}
				}

				function chooseBrand(brand, event) {
					stopNavigation(event);

					if (!brand) {
						return false;
					}

					const section = brand.closest(SECTION_SELECTOR);

					if (!section) {
						return false;
					}

					setActiveBrand(section, brand);
					updateProducts(section, brand);

					return false;
				}

				function countProductsForBrand(section, brandId) {
					const value = String(brandId || "");
					let count = 0;

					section.querySelectorAll(PRODUCT_SELECTOR).forEach(function (product) {
						if (String(product.dataset.brandId || "") === value) {
							count += 1;
						}
					});

					return count;
				}

				function initFeaturedBrandSection(section) {
					const brands = Array.from(section.querySelectorAll(BRAND_SELECTOR));

					if (!brands.length) {
						return;
					}

					brands.forEach(function (brand) {
						brand.addEventListener("click", function (event) {
							chooseBrand(brand, event);
						});

						brand.addEventListener("keydown", function (event) {
							if (event.key === "Enter" || event.key === " ") {
								chooseBrand(brand, event);
							}
						});
					});

					let firstBrandWithProducts = brands.find(function (brand) {
						return countProductsForBrand(section, brand.dataset.brandFilter) > 0;
					});

					chooseBrand(firstBrandWithProducts || brands[0], null);
				}

				document.addEventListener("DOMContentLoaded", function () {
					document.querySelectorAll(SECTION_SELECTOR).forEach(initFeaturedBrandSection);
				});
			})();
		</script>
	</section>
</c:if>

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
		initWishlistForms();
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

			if (dots[i]) {
				dots[i].classList.add("active");
			}

			index = i;
		}

		function nextSlide() {
			showSlide((index + 1) % slides.length);
		}

		function prevSlide() {
			showSlide((index - 1 + slides.length) % slides.length);
		}

		if (next) {
			next.addEventListener("click", nextSlide);
		}

		if (prev) {
			prev.addEventListener("click", prevSlide);
		}

		dots.forEach((dot, i) => {
			dot.addEventListener("click", () => showSlide(i));
		});

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

			if (distance < 0) {
				distance = 0;
			}

			const hours = Math.floor(distance / (1000 * 60 * 60));
			const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
			const seconds = Math.floor((distance % (1000 * 60)) / 1000);

			boxes.forEach(box => {
				const hh = box.querySelector("[data-hh]");
				const mm = box.querySelector("[data-mm]");
				const ss = box.querySelector("[data-ss]");

				if (hh) hh.innerText = pad(hours);
				if (mm) mm.innerText = pad(minutes);
				if (ss) ss.innerText = pad(seconds);
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
			window.scrollTo({
				top: 0,
				behavior: "smooth"
			});
		});
	}

	function showCustomAlert(title, message, isSuccess) {
		const modal = document.createElement("div");
		modal.className = "custom-alert-modal";

		const color = isSuccess ? "#9b0012" : "#e53935";

		const box = document.createElement("div");
		box.className = "custom-alert-box";

		const iconEl = document.createElement("div");
		iconEl.style.fontSize = "40px";
		iconEl.style.marginBottom = "10px";
		iconEl.textContent = isSuccess ? "🎉" : "⚠️";

		const titleEl = document.createElement("h3");
		titleEl.style.color = color;
		titleEl.style.marginBottom = "10px";
		titleEl.style.fontSize = "20px";
		titleEl.textContent = title;

		const messageEl = document.createElement("p");
		messageEl.style.color = "#555";
		messageEl.style.marginBottom = "20px";
		messageEl.style.lineHeight = "1.5";
		messageEl.textContent = message;

		const closeBtn = document.createElement("button");
		closeBtn.type = "button";
		closeBtn.textContent = "Đóng";
		closeBtn.style.background = color;
		closeBtn.style.color = "#fff";
		closeBtn.style.border = "none";
		closeBtn.style.padding = "10px 24px";
		closeBtn.style.borderRadius = "999px";
		closeBtn.style.cursor = "pointer";
		closeBtn.style.fontWeight = "bold";
		closeBtn.style.width = "100%";
		closeBtn.addEventListener("click", function () {
			modal.remove();
		});

		box.appendChild(iconEl);
		box.appendChild(titleEl);
		box.appendChild(messageEl);
		box.appendChild(closeBtn);
		modal.appendChild(box);

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
						showCustomAlert(
								"Thông báo",
								data.success ? "Lưu mã thành công!" : "Mã này đã có trong ví của bạn.",
								true
						);
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

	function showVoucherDetailFromEl(btn) {
		const code = btn.getAttribute("data-code") || "Không rõ";
		const desc = btn.getAttribute("data-desc") || "Không có mô tả cụ thể.";
		const min = btn.getAttribute("data-min");
		const end = btn.getAttribute("data-end");

		let minText = "0 ₫";

		if (min && parseInt(min, 10) > 0) {
			minText = Number(min).toLocaleString("vi-VN") + " ₫";
		}

		const endText = end && end.trim() !== "" ? end : "Không giới hạn";

		const modal = document.createElement("div");
		modal.className = "custom-alert-modal";

		const box = document.createElement("div");
		box.className = "custom-alert-box";
		box.style.textAlign = "left";

		const icon = document.createElement("div");
		icon.style.fontSize = "32px";
		icon.style.textAlign = "center";
		icon.style.marginBottom = "10px";
		icon.textContent = "🎟️";

		const title = document.createElement("h3");
		title.style.color = "#9b0012";
		title.style.marginBottom = "15px";
		title.style.fontSize = "20px";
		title.style.textAlign = "center";
		title.textContent = "Chi tiết ưu đãi";

		const detail = document.createElement("div");
		detail.style.color = "#444";
		detail.style.lineHeight = "1.6";
		detail.style.fontSize = "14px";
		detail.style.marginBottom = "24px";
		detail.style.padding = "15px";
		detail.style.background = "#fff0f6";
		detail.style.borderRadius = "12px";

		const rows = [
			["Mã code:", code],
			["Mô tả:", desc],
			["Đơn tối thiểu:", minText],
			["Hạn sử dụng:", endText]
		];

		rows.forEach(function (row) {
			const p = document.createElement("p");
			p.style.margin = "0 0 8px 0";

			const label = document.createElement("strong");
			label.textContent = row[0] + " ";

			const value = document.createElement("span");
			value.textContent = row[1];

			if (row[0] === "Mã code:") {
				value.style.color = "#9b0012";
				value.style.fontWeight = "bold";
				value.style.fontSize = "16px";
			}

			p.appendChild(label);
			p.appendChild(value);
			detail.appendChild(p);
		});

		const closeBtn = document.createElement("button");
		closeBtn.type = "button";
		closeBtn.textContent = "Đã hiểu";
		closeBtn.style.background = "linear-gradient(135deg, #8f001f, #d44d79)";
		closeBtn.style.color = "#fff";
		closeBtn.style.border = "none";
		closeBtn.style.padding = "12px 24px";
		closeBtn.style.borderRadius = "999px";
		closeBtn.style.cursor = "pointer";
		closeBtn.style.fontWeight = "bold";
		closeBtn.style.width = "100%";
		closeBtn.addEventListener("click", function () {
			modal.remove();
		});

		box.appendChild(icon);
		box.appendChild(title);
		box.appendChild(detail);
		box.appendChild(closeBtn);
		modal.appendChild(box);

		document.body.appendChild(modal);
	}

	function initWishlistForms() {
		const wishlistForms = document.querySelectorAll(".wishlist-form");

		if (!wishlistForms.length) return;

		wishlistForms.forEach(form => {
			form.addEventListener("submit", function (e) {
				e.preventDefault();

				const btn = this.querySelector(".wishlist-btn");
				const formData = new URLSearchParams(new FormData(this));

				fetch(this.action, {
					method: "POST",
					headers: {
						"Content-Type": "application/x-www-form-urlencoded"
					},
					body: formData
				})
						.then(response => {
							if (response.status === 401) {
								if (typeof showLoginModal === "function") {
									showLoginModal();
								} else {
									showCustomAlert(
											"Chưa đăng nhập",
											"Vui lòng đăng nhập để thêm sản phẩm vào yêu thích.",
											false
									);
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
	}
</script>

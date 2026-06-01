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

<!-- =========================================================
FLASH DEAL
Ưu tiên flashSaleProducts từ flash_sale_items. Nếu chưa có Flash Sale đang chạy,
fallback sang deepDiscountProducts để block không bị mất khỏi trang chủ.
========================================================= -->
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
Các thương hiệu chọn được và lọc đúng sản phẩm ngay trong section.
========================================================= -->
<c:if test="${not empty brands}">
	<c:choose>
		<c:when test="${not empty featuredHomeBrands}">
			<c:set var="featuredBrandList" value="${featuredHomeBrands}" />
		</c:when>
		<c:otherwise>
			<c:set var="featuredBrandList" value="${brands}" />
		</c:otherwise>
	</c:choose>

	<c:choose>
		<c:when test="${not empty featuredBrandProducts}">
			<c:set var="brandSectionProducts" value="${featuredBrandProducts}" />
		</c:when>
		<c:otherwise>
			<c:set var="brandSectionProducts" value="${featuredProducts}" />
		</c:otherwise>
	</c:choose>

	<section class="skin-featured-brand-section" id="featured-brands" data-featured-brand-section>
		<style>
			.skin-featured-brand-section {
				padding: 52px 0 58px;
				background: #fff;
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
				background: linear-gradient(135deg, #ffe1eb 0%, #fff7fa 48%, #ffd4e3 100%);
				text-decoration: none;
				box-shadow: 0 18px 45px rgba(157, 0, 45, .10);
			}

			.skin-featured-brand-banner img {
				width: 100%;
				height: 100%;
				min-height: 620px;
				object-fit: cover;
				display: block;
			}

			.skin-featured-brand-banner-fallback {
				min-height: 620px;
				display: flex;
				flex-direction: column;
				align-items: center;
				justify-content: center;
				padding: 38px;
				text-align: center;
				color: #9b001c;
			}

			.skin-featured-brand-banner-fallback span {
				font-size: 13px;
				font-weight: 950;
				letter-spacing: .22em;
				text-transform: uppercase;
			}

			.skin-featured-brand-banner-fallback strong {
				display: block;
				margin-top: 12px;
				font-size: clamp(32px, 4vw, 58px);
				font-weight: 1000;
				line-height: 1.05;
				text-transform: uppercase;
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
				color: #161616;
				font-size: clamp(24px, 2.4vw, 38px);
				font-weight: 950;
				letter-spacing: .075em;
				line-height: 1.2;
				text-transform: uppercase;
			}

			.skin-featured-brand-more {
				flex: 0 0 auto;
				color: #8d001f;
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
				min-height: 76px;
				overflow: hidden;
				border: 3px solid #8d001f;
				border-radius: 999px;
				background: #fff;
				box-shadow: 0 12px 28px rgba(141, 0, 31, .08);
			}

			.skin-featured-brand-logo {
				min-width: 0;
				height: 76px;
				display: flex;
				align-items: center;
				justify-content: center;
				padding: 12px 16px;
				border: 0;
				border-right: 1px solid rgba(141, 0, 31, .10);
				background: #fff;
				color: #161616;
				font-family: inherit;
				font-size: 20px;
				font-weight: 950;
				letter-spacing: .04em;
				text-align: center;
				text-decoration: none;
				text-transform: uppercase;
				cursor: pointer;
				transition: background .2s ease, color .2s ease, box-shadow .2s ease;
			}

			.skin-featured-brand-logo:last-child {
				border-right: 0;
			}

			.skin-featured-brand-logo img {
				max-width: 100%;
				max-height: 46px;
				object-fit: contain;
				display: block;
				filter: grayscale(1) contrast(1.1);
				transition: filter .22s ease, transform .22s ease;
			}
			.skin-featured-brand-logo:hover {
				background: #fff4f7;
				color: #9b001c;
				box-shadow: inset 0 -4px 0 #9b001c;
			}

			.skin-featured-brand-logo:hover img {
				filter: grayscale(0) contrast(1);
				transform: scale(1.06);
			}

			.skin-featured-brand-logo.is-active {
				position: relative;
				background: #9b001c;
				color: #fff;
				box-shadow: inset 0 0 0 2px #9b001c, 0 12px 24px rgba(155, 0, 28, .18);
			}

			.skin-featured-brand-logo.is-active::after {
				content: "";
				position: absolute;
				left: 50%;
				bottom: -9px;
				width: 18px;
				height: 18px;
				background: #9b001c;
				transform: translateX(-50%) rotate(45deg);
			}

			.skin-featured-brand-logo.is-active img {
				filter: brightness(0) invert(1);
				transform: scale(1.06);
			}

			.skin-featured-brand-logo-fallback {
				display: -webkit-box;
				overflow: hidden;
				-webkit-line-clamp: 1;
				-webkit-box-orient: vertical;
			}

			.skin-featured-brand-filter-note {
				min-height: 26px;
				margin-top: 12px;
				color: #6b7280;
				font-size: 14px;
				font-weight: 650;
			}

			.skin-featured-brand-filter-note b {
				color: #9b001c;
				font-weight: 900;
			}

			.skin-featured-brand-products {
				display: grid;
				grid-template-columns: repeat(4, minmax(0, 1fr));
				margin-top: 16px;
				border-top: 1px solid #ececec;
				border-left: 1px solid #ececec;
			}

			.skin-featured-brand-products.is-filtering {
				animation: skinBrandFilterFade .18s ease;
			}

			@keyframes skinBrandFilterFade {
				from {
					opacity: .55;
					transform: translateY(4px);
				}

				to {
					opacity: 1;
					transform: translateY(0);
				}
			}

			.skin-featured-brand-card {
				position: relative;
				min-width: 0;
				padding-bottom: 18px;
				background: #fff;
				border-right: 1px solid #ececec;
				border-bottom: 1px solid #ececec;
				transition: box-shadow .22s ease, transform .22s ease;
			}

			.skin-featured-brand-card.is-hidden {
				display: none;
			}

			.skin-featured-brand-card:hover {
				z-index: 2;
				transform: translateY(-3px);
				box-shadow: 0 18px 36px rgba(15, 23, 42, .10);
			}

			.skin-featured-brand-image {
				position: relative;
				display: block;
				height: 190px;
				overflow: hidden;
				background: #f7fbff;
			}

			.skin-featured-brand-image img {
				width: 100%;
				height: 100%;
				object-fit: cover;
				display: block;
			}

			.skin-featured-brand-discount {
				position: absolute;
				right: 12px;
				bottom: 12px;
				width: 48px;
				height: 48px;
				display: inline-flex;
				align-items: center;
				justify-content: center;
				border-radius: 999px;
				background: #a8d83f;
				color: #fff;
				font-size: 13px;
				font-weight: 950;
			}

			.skin-featured-brand-body {
				padding: 14px 14px 0;
			}

			.skin-featured-brand-name {
				display: block;
				margin-bottom: 7px;
				color: #171717;
				font-size: 15px;
				font-weight: 950;
				letter-spacing: .05em;
				text-decoration: none;
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
				color: #9b001c;
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
				font-size: 18px;
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

			.skin-featured-brand-empty {
				display: none;
				margin-top: 16px;
				padding: 24px;
				border: 1px dashed #e5b3c1;
				border-radius: 18px;
				background: #fff8fb;
				color: #8d001f;
				font-size: 15px;
				font-weight: 750;
				text-align: center;
			}

			.skin-featured-brand-empty.is-show {
				display: block;
			}

			.skin-featured-brand-view-more {
				display: flex;
				justify-content: center;
				margin-top: 24px;
			}

			.skin-featured-brand-view-more a {
				color: #8d001f;
				font-size: 18px;
				font-weight: 850;
				text-decoration: none;
			}

			.skin-featured-brand-view-more a:hover {
				text-decoration: underline;
				text-underline-offset: 4px;
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

				.skin-featured-brand-logo {
					border-bottom: 1px solid rgba(141, 0, 31, .10);
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
							<strong>Beauty Brand Picks</strong>
						</div>
					</c:otherwise>
				</c:choose>
			</a>

			<div class="skin-featured-brand-main">
				<div class="skin-featured-brand-head">
					<h2 class="skin-featured-brand-title">THƯƠNG HIỆU NỔI BẬT</h2>
					<a class="skin-featured-brand-more" href="${ctx}/brands">Xem thêm →</a>
				</div>

				<div class="skin-featured-brand-logo-row" role="tablist" aria-label="Lọc sản phẩm theo thương hiệu">
					<c:forEach var="brand" items="${featuredBrandList}" begin="0" end="5">
						<div class="skin-featured-brand-logo"
							 data-brand-filter="${brand.id}"
							 data-brand-name="${brand.name}"
							 role="tab"
							 tabindex="0"
							 onclick="return window.myCosmeticFilterFeaturedBrand ? window.myCosmeticFilterFeaturedBrand(this, event) : false;"
							 onkeydown="if(event.key === 'Enter' || event.key === ' '){return window.myCosmeticFilterFeaturedBrand ? window.myCosmeticFilterFeaturedBrand(this, event) : false;}"
							 aria-selected="false"
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
						</div>
					</c:forEach>
				</div>

				<div class="skin-featured-brand-filter-note" data-brand-note></div>

				<div class="skin-featured-brand-products" data-brand-products>
					<c:forEach var="product" items="${brandSectionProducts}">
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
								 data-brand-id="${product.brandId}"
								 data-brand-name="${product.brandName}">
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
							</div>
						</article>
					</c:forEach>
				</div>

				<div class="skin-featured-brand-empty" data-brand-empty>
					Chưa có sản phẩm phù hợp cho thương hiệu này trong danh sách nổi bật.
				</div>

				<div class="skin-featured-brand-view-more">
					<a href="${ctx}/brands">Xem thêm →</a>
				</div>
			</div>
		</div>

		<script>
			(function () {
				function getSection(control) {
					return control ? control.closest("[data-featured-brand-section]") : null;
				}

				function getBrandProductCount(section, brandId) {
					const cards = Array.from(section.querySelectorAll("[data-brand-product]"));

					return cards.filter(function (card) {
						return String(card.dataset.brandId || "") === String(brandId || "");
					}).length;
				}

				window.myCosmeticFilterFeaturedBrand = function (control, event) {
					if (event) {
						event.preventDefault();
						event.stopPropagation();
						if (typeof event.stopImmediatePropagation === "function") {
							event.stopImmediatePropagation();
						}
					}

					const section = getSection(control);

					if (!section) {
						return false;
					}

					const buttons = Array.from(section.querySelectorAll("[data-brand-filter]"));
					const cards = Array.from(section.querySelectorAll("[data-brand-product]"));
					const note = section.querySelector("[data-brand-note]");
					const empty = section.querySelector("[data-brand-empty]");
					const productGrid = section.querySelector("[data-brand-products]");
					const brandId = String(control.dataset.brandFilter || "");
					const brandName = control.dataset.brandName || "thương hiệu";
					let visibleCount = 0;

					buttons.forEach(function (button) {
						const isActive = button === control;
						button.classList.toggle("is-active", isActive);
						button.setAttribute("aria-selected", isActive ? "true" : "false");
					});

					cards.forEach(function (card) {
						const matched = String(card.dataset.brandId || "") === brandId;
						card.classList.toggle("is-hidden", !matched);

						if (matched) {
							visibleCount++;
						}
					});

					if (productGrid) {
						productGrid.classList.remove("is-filtering");
						void productGrid.offsetWidth;
						productGrid.classList.add("is-filtering");
					}

					if (note) {
						note.innerHTML = "Đang hiển thị <b>" + visibleCount + "</b> sản phẩm của thương hiệu <b>" + brandName + "</b>.";
					}

					if (empty) {
						empty.classList.toggle("is-show", visibleCount === 0);
					}

					return false;
				};

				function initFeaturedBrandFilter() {
					const sections = document.querySelectorAll("[data-featured-brand-section]");

					sections.forEach(function (section) {
						const buttons = Array.from(section.querySelectorAll("[data-brand-filter]"));

						if (!buttons.length) {
							return;
						}

						buttons.forEach(function (button) {
							button.addEventListener("click", function (event) {
								window.myCosmeticFilterFeaturedBrand(button, event);
							});

							button.addEventListener("keydown", function (event) {
								if (event.key === "Enter" || event.key === " ") {
									window.myCosmeticFilterFeaturedBrand(button, event);
								}
							});
						});

						const defaultButton = buttons.find(function (button) {
							return getBrandProductCount(section, button.dataset.brandFilter || "") > 0;
						}) || buttons[0];

						window.myCosmeticFilterFeaturedBrand(defaultButton, null);
					});
				}

				document.addEventListener("click", function (event) {
					const brandControl = event.target.closest("[data-brand-filter]");

					if (!brandControl) {
						return;
					}

					window.myCosmeticFilterFeaturedBrand(brandControl, event);
				}, true);

				if (document.readyState === "loading") {
					document.addEventListener("DOMContentLoaded", initFeaturedBrandFilter);
				} else {
					initFeaturedBrandFilter();
				}
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

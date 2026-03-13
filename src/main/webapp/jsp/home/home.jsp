<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!-- ================= BANNER SLIDER ================= -->
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

<!-- ================= FEATURED PRODUCTS ================= -->
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

<!-- ================= SLIDER SCRIPT ================= -->
<script>
document.addEventListener("DOMContentLoaded", function () {

    const slides = document.querySelectorAll(".slide");
    const dots   = document.querySelectorAll(".dot");
    const prev   = document.querySelector(".prev");
    const next   = document.querySelector(".next");

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
</script>

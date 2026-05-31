<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isFlashSection" value="${homeSectionMode == 'flash'}" />

<c:set var="sectionViewAllText" value="${homeSectionViewAllText}" />
<c:if test="${empty sectionViewAllText}">
    <c:choose>
        <c:when test="${isFlashSection}">
            <c:set var="sectionViewAllText" value="XEM TẤT CẢ DEAL" />
        </c:when>
        <c:otherwise>
            <c:set var="sectionViewAllText" value="XEM TẤT CẢ" />
        </c:otherwise>
    </c:choose>
</c:if>

<c:if test="${not empty homeSectionProducts}">
    <section class="skin-product-section ${isFlashSection ? 'is-flash' : ''}">
        <div class="skin-container">

            <div class="skin-section-top ${isFlashSection ? 'flash-top' : ''}">
                <div>
                    <c:choose>
                        <c:when test="${isFlashSection}">
                            <div class="skin-flash-title">
                                <span class="flash-word">FLASH</span>
                                <span class="bolt">⚡</span>
                                <span class="deal-word">DEAL</span>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <span class="skin-eyebrow">MYCOSMETICSHOP</span>
                            <h2>${homeSectionTitle}</h2>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${not empty homeSectionDesc}">
                        <p>${homeSectionDesc}</p>
                    </c:if>
                </div>

                <div class="skin-section-actions">
                    <c:if test="${isFlashSection}">
                        <div class="skin-countdown" data-deal-countdown>
                            <span data-hh>00</span>
                            <b>:</b>
                            <span data-mm>00</span>
                            <b>:</b>
                            <span data-ss>00</span>
                        </div>
                    </c:if>

                    <c:if test="${not empty homeSectionLink}">
                        <a class="skin-view-all" href="${ctx}${homeSectionLink}">
                                ${sectionViewAllText}
                        </a>
                    </c:if>
                </div>
            </div>

            <c:if test="${isFlashSection}">
            <div class="skin-flash-carousel">
                <button type="button"
                        class="skin-flash-nav skin-flash-prev"
                        aria-label="Sản phẩm trước">
                    ‹
                </button>

                <div class="skin-flash-viewport">
                    </c:if>

                    <div class="skin-product-scroll ${isFlashSection ? 'flash-scroll skin-flash-track' : ''}">
                        <c:forEach var="product" items="${homeSectionProducts}">

                            <c:choose>
                                <c:when test="${not empty product.slug}">
                                    <c:set var="productUrl" value="${ctx}/product/${product.slug}" />
                                </c:when>
                                <c:otherwise>
                                    <c:set var="productUrl" value="${ctx}/product?id=${product.id}" />
                                </c:otherwise>
                            </c:choose>

                            <article class="skin-product-card ${isFlashSection ? 'flash-card' : ''}">

                                <a class="skin-product-image" href="${productUrl}">
                                    <c:if test="${product.discountPercent > 0}">
                                        <span class="skin-discount-bubble">-${product.discountPercent}%</span>
                                    </c:if>

                                    <c:if test="${isFlashSection}">
                                        <span class="skin-card-label">FREESHIP TQ</span>
                                    </c:if>

                                    <c:choose>
                                        <c:when test="${not empty product.imageUrl}">
                                            <c:choose>
                                                <c:when test="${fn:startsWith(product.imageUrl, 'http')}">
                                                    <img src="${product.imageUrl}" alt="${product.title}">
                                                </c:when>
                                                <c:when test="${fn:startsWith(product.imageUrl, '/')}">
                                                    <img src="${ctx}${product.imageUrl}" alt="${product.title}">
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="${ctx}/uploads/product/${product.imageUrl}" alt="${product.title}">
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="skin-no-image">No image</div>
                                        </c:otherwise>
                                    </c:choose>
                                </a>

                                <div class="skin-product-body">
                                    <c:if test="${isFlashSection}">
                                        <div class="skin-flash-card-tags">
                                            <span>FLASH DEAL</span>
                                        </div>
                                    </c:if>

                                    <a class="skin-product-title" href="${productUrl}">
                                            ${product.title}
                                    </a>

                                    <div class="skin-price-row">
                                        <c:choose>
                                            <c:when test="${product.discountPercent > 0}">
                                                <strong>
                                                    <fmt:formatNumber value="${product.finalPrice}" type="number" groupingUsed="true"/>đ
                                                </strong>
                                                <del>
                                                    <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>đ
                                                </del>
                                            </c:when>
                                            <c:otherwise>
                                                <strong>
                                                    <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>đ
                                                </strong>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="skin-product-meta">
                                        <c:if test="${homeSectionShowSold == true}">
                                            <span>${product.soldQuantity} đã bán</span>
                                        </c:if>

                                        <c:if test="${homeSectionShowViews == true}">
                                            <span>${product.viewCount} lượt xem</span>
                                        </c:if>

                                        <c:if test="${homeSectionShowDiscount == true && product.discountPercent > 0}">
                                            <span>Giảm ${product.discountPercent}%</span>
                                        </c:if>

                                        <c:if test="${product.reviewCount > 0}">
                                    <span>
                                        ★ <fmt:formatNumber value="${product.avgRating}" maxFractionDigits="1"/>
                                        (${product.reviewCount})
                                    </span>
                                        </c:if>
                                    </div>

                                    <c:choose>
                                        <c:when test="${isFlashSection}">
                                            <div class="skin-progress">
                                                <span style="width: ${product.saleProgressPercent}%;"></span>
                                            </div>
                                            <div class="skin-progress-text">
                                                ĐANG DIỄN RA ${product.saleProgressPercent}%
                                            </div>
                                        </c:when>

                                        <c:otherwise>
                                            <div class="skin-stock-line">
                                                <c:choose>
                                                    <c:when test="${product.stock == 0}">
                                                        <span class="out">Hết hàng</span>
                                                    </c:when>
                                                    <c:when test="${product.stock <= 5}">
                                                        <span class="low">Sắp hết hàng</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="ok">Còn hàng</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </article>
                        </c:forEach>
                    </div>

                    <c:if test="${isFlashSection}">
                </div>

                <button type="button"
                        class="skin-flash-nav skin-flash-next"
                        aria-label="Sản phẩm tiếp theo">
                    ›
                </button>
            </div>
            </c:if>

        </div>
    </section>
</c:if>

<c:if test="${isFlashSection}">
    <style>
        .skin-product-section.is-flash {
            padding-top: 34px;
            padding-bottom: 56px;
            overflow: hidden;
        }

        .skin-product-section.is-flash .flash-top {
            align-items: center;
            margin-bottom: 24px;
        }

        .skin-product-section.is-flash .skin-section-actions {
            align-items: center;
        }

        .skin-product-section.is-flash .skin-view-all {
            color: #111;
            font-size: 18px;
            font-weight: 950;
            text-decoration: underline;
            text-underline-offset: 5px;
            text-decoration-color: #9b001c;
            text-transform: uppercase;
            white-space: nowrap;
        }

        .skin-product-section.is-flash .skin-flash-title {
            display: flex;
            align-items: center;
            gap: 22px;
            color: #b30024;
            font-size: clamp(38px, 4vw, 62px);
            font-weight: 1000;
            letter-spacing: .14em;
            line-height: 1;
            text-transform: uppercase;
        }

        .skin-product-section.is-flash .skin-flash-title .bolt {
            color: #ff9b3d;
            letter-spacing: 0;
            filter: drop-shadow(0 8px 14px rgba(255, 142, 38, .22));
        }

        .skin-product-section.is-flash .skin-countdown {
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .skin-product-section.is-flash .skin-countdown span {
            min-width: 52px;
            height: 52px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            border-radius: 10px;
            background: #000;
            color: #fff;
            font-size: 27px;
            line-height: 1;
            font-weight: 950;
        }

        .skin-product-section.is-flash .skin-countdown b {
            color: #111;
            font-size: 28px;
            font-weight: 950;
        }

        .skin-flash-carousel {
            position: relative;
        }

        .skin-flash-viewport {
            width: 100%;
            overflow: hidden;
        }

        .skin-product-section.is-flash .skin-product-scroll.flash-scroll {
            display: flex !important;
            gap: 18px;
            overflow: visible !important;
            overflow-x: visible !important;
            scroll-snap-type: none !important;
            scrollbar-width: none;
            padding-bottom: 0 !important;
            transition: transform .4s ease;
            will-change: transform;
        }

        .skin-product-section.is-flash .skin-product-scroll.flash-scroll::-webkit-scrollbar {
            display: none;
        }

        .skin-product-section.is-flash .skin-product-card.flash-card {
            flex: 0 0 calc((100% - 72px) / 5);
            max-width: calc((100% - 72px) / 5);
            min-width: 0 !important;
            border-radius: 0;
            border: 1px solid #ececec;
            background: #fff;
            box-shadow: none;
            scroll-snap-align: unset;
            transition: transform .22s ease, box-shadow .22s ease, border-color .22s ease;
        }

        .skin-product-section.is-flash .skin-product-card.flash-card:hover {
            transform: translateY(-4px);
            border-color: #efc3cf;
            box-shadow: 0 18px 36px rgba(155, 0, 40, .12);
        }

        .skin-product-section.is-flash .skin-product-image {
            position: relative;
            aspect-ratio: 1 / 1;
            background: #f6fbff;
            overflow: hidden;
        }

        .skin-product-section.is-flash .skin-product-image img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
        }

        .skin-product-section.is-flash .skin-card-label {
            position: absolute;
            left: 12px;
            bottom: 12px;
            z-index: 2;
            min-height: 30px;
            padding: 7px 13px;
            border-radius: 999px;
            background: #06245f;
            color: #fff;
            font-size: 12px;
            line-height: 1;
            font-weight: 900;
            text-transform: uppercase;
            box-shadow: 0 8px 14px rgba(6, 36, 95, .18);
        }

        .skin-product-section.is-flash .skin-discount-bubble {
            position: absolute;
            right: 12px;
            bottom: 12px;
            z-index: 3;
            width: 48px;
            height: 48px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            background: #a8d83f;
            color: #fff;
            font-size: 13px;
            font-weight: 950;
        }

        .skin-product-section.is-flash .skin-product-body {
            padding: 14px 14px 16px;
        }

        .skin-product-section.is-flash .skin-flash-card-tags {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            margin-bottom: 8px;
        }

        .skin-product-section.is-flash .skin-flash-card-tags span {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-height: 28px;
            padding: 6px 11px;
            border-radius: 999px;
            background: #b0002f;
            color: #fff;
            font-size: 11px;
            line-height: 1;
            font-weight: 950;
            text-transform: uppercase;
        }

        .skin-product-section.is-flash .skin-product-title {
            min-height: 54px;
            display: -webkit-box;
            overflow: hidden;
            color: #1b1b1b;
            font-size: 16px;
            line-height: 1.42;
            font-weight: 850;
            text-decoration: none;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
        }

        .skin-product-section.is-flash .skin-product-title:hover {
            color: #b0002f;
        }

        .skin-product-section.is-flash .skin-price-row {
            margin-top: 9px;
            display: flex;
            flex-wrap: wrap;
            align-items: baseline;
            gap: 8px;
        }

        .skin-product-section.is-flash .skin-price-row strong {
            color: #a90027;
            font-size: 22px;
            line-height: 1.1;
            font-weight: 1000;
        }

        .skin-product-section.is-flash .skin-price-row del {
            color: #8d8d8d;
            font-size: 14px;
        }

        .skin-product-section.is-flash .skin-product-meta {
            margin-top: 8px;
            display: flex;
            flex-wrap: wrap;
            gap: 6px;
        }

        .skin-product-section.is-flash .skin-product-meta span {
            display: inline-flex;
            align-items: center;
            min-height: 24px;
            padding: 4px 8px;
            border-radius: 999px;
            background: #f5f5f5;
            color: #333;
            font-size: 12px;
            font-weight: 800;
        }

        .skin-product-section.is-flash .skin-progress {
            height: 7px;
            margin-top: 10px;
            overflow: hidden;
            border-radius: 999px;
            background: #f3dbe2;
        }

        .skin-product-section.is-flash .skin-progress span {
            display: block;
            height: 100%;
            border-radius: inherit;
            background: #970019;
        }

        .skin-product-section.is-flash .skin-progress-text {
            margin-top: 8px;
            color: #1f1f1f;
            font-size: 13px;
            font-weight: 900;
            text-transform: uppercase;
        }

        .skin-flash-nav {
            position: absolute;
            top: 41%;
            z-index: 10;
            width: 48px;
            height: 48px;
            border: 0;
            border-radius: 50%;
            background: rgba(255, 255, 255, .96);
            color: #555;
            box-shadow: 0 10px 26px rgba(0, 0, 0, .14);
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 36px;
            line-height: 1;
            font-weight: 400;
            transition: transform .2s ease, opacity .2s ease, background .2s ease;
        }

        .skin-flash-nav:hover {
            transform: translateY(-50%) scale(1.06);
            background: #fff;
            color: #9b001c;
        }

        .skin-flash-nav:disabled {
            opacity: .25;
            cursor: not-allowed;
            pointer-events: none;
        }

        .skin-flash-prev {
            left: -24px;
            transform: translateY(-50%);
        }

        .skin-flash-next {
            right: -24px;
            transform: translateY(-50%);
        }

        @media (max-width: 1200px) {
            .skin-product-section.is-flash .skin-product-card.flash-card {
                flex-basis: calc((100% - 54px) / 4);
                max-width: calc((100% - 54px) / 4);
            }
        }

        @media (max-width: 900px) {
            .skin-product-section.is-flash .skin-product-card.flash-card {
                flex-basis: calc((100% - 18px) / 2);
                max-width: calc((100% - 18px) / 2);
            }

            .skin-product-section.is-flash .flash-top {
                align-items: flex-start;
                flex-direction: column;
            }

            .skin-product-section.is-flash .skin-section-actions {
                align-items: flex-start;
            }

            .skin-flash-prev {
                left: 8px;
            }

            .skin-flash-next {
                right: 8px;
            }
        }

        @media (max-width: 600px) {
            .skin-product-section.is-flash .skin-product-card.flash-card {
                flex-basis: 100%;
                max-width: 100%;
            }

            .skin-product-section.is-flash .skin-flash-title {
                font-size: 34px;
                gap: 10px;
            }

            .skin-product-section.is-flash .skin-countdown span {
                min-width: 42px;
                height: 42px;
                font-size: 20px;
            }
        }
    </style>

    <script>
        (function () {
            if (window.__myCosmeticFlashHomeCarouselReady) {
                return;
            }

            window.__myCosmeticFlashHomeCarouselReady = true;

            document.addEventListener("DOMContentLoaded", function () {
                initMyCosmeticFlashCountdown();
                initMyCosmeticFlashCarousel();
            });

            function initMyCosmeticFlashCountdown() {
                const timers = document.querySelectorAll("[data-deal-countdown]");

                if (!timers.length) {
                    return;
                }

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

                    timers.forEach(function (box) {
                        const hh = box.querySelector("[data-hh]");
                        const mm = box.querySelector("[data-mm]");
                        const ss = box.querySelector("[data-ss]");

                        if (hh) {
                            hh.textContent = pad(hours);
                        }

                        if (mm) {
                            mm.textContent = pad(minutes);
                        }

                        if (ss) {
                            ss.textContent = pad(seconds);
                        }
                    });
                }

                render();
                setInterval(render, 1000);
            }

            function initMyCosmeticFlashCarousel() {
                const carousels = document.querySelectorAll(".skin-flash-carousel");

                carousels.forEach(function (carousel) {
                    const viewport = carousel.querySelector(".skin-flash-viewport");
                    const track = carousel.querySelector(".skin-flash-track");
                    const slides = carousel.querySelectorAll(".skin-product-card.flash-card");
                    const prevBtn = carousel.querySelector(".skin-flash-prev");
                    const nextBtn = carousel.querySelector(".skin-flash-next");

                    if (!viewport || !track || !slides.length || !prevBtn || !nextBtn) {
                        return;
                    }

                    let currentIndex = 0;

                    function getVisibleCount() {
                        const width = viewport.clientWidth;

                        if (width <= 600) {
                            return 1;
                        }

                        if (width <= 900) {
                            return 2;
                        }

                        if (width <= 1200) {
                            return 4;
                        }

                        return 5;
                    }

                    function getGap() {
                        const style = window.getComputedStyle(track);
                        const gapValue = style.columnGap || style.gap || "0";
                        return parseFloat(gapValue) || 0;
                    }

                    function getSlideStep() {
                        const slide = slides[0];
                        return slide.offsetWidth + getGap();
                    }

                    function getMaxIndex() {
                        return Math.max(0, slides.length - getVisibleCount());
                    }

                    function update() {
                        const maxIndex = getMaxIndex();

                        if (currentIndex > maxIndex) {
                            currentIndex = maxIndex;
                        }

                        if (currentIndex < 0) {
                            currentIndex = 0;
                        }

                        track.style.transform = "translateX(-" + (currentIndex * getSlideStep()) + "px)";

                        prevBtn.disabled = currentIndex <= 0;
                        nextBtn.disabled = currentIndex >= maxIndex;
                    }

                    nextBtn.addEventListener("click", function () {
                        currentIndex = Math.min(currentIndex + getVisibleCount(), getMaxIndex());
                        update();
                    });

                    prevBtn.addEventListener("click", function () {
                        currentIndex = Math.max(currentIndex - getVisibleCount(), 0);
                        update();
                    });

                    window.addEventListener("resize", update);
                    update();
                });
            }
        })();
    </script>
</c:if>
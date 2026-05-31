<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="selectedSlot" value="${empty param.slot ? '09:00' : param.slot}" />
<c:set var="selectedCategory" value="${empty param.category ? 'TOP_PICK' : param.category}" />

<c:set var="dealProducts" value="${flashSaleProducts}" />
<c:if test="${empty dealProducts && not empty deepDiscountProducts}">
  <c:set var="dealProducts" value="${deepDiscountProducts}" />
</c:if>
<c:if test="${empty dealProducts && not empty products}">
  <c:set var="dealProducts" value="${products}" />
</c:if>
<c:if test="${empty dealProducts && not empty featuredProducts}">
  <c:set var="dealProducts" value="${featuredProducts}" />
</c:if>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Flash Deal - MyCosmetic</title>
  <link rel="stylesheet" href="${ctx}/assets/css/flash-sale.css?v=20260531-final">
</head>
<body>

<jsp:include page="/jsp/common/header.jsp" />

<main class="flash-page">
  <div class="flash-container">

    <!-- HERO GIỐNG MẪU THAM KHẢO -->
    <section class="flash-hero">
      <div class="flash-hero__brand">MyCosmetic</div>

      <div class="flash-hero__content">
        <div class="flash-hero__title">FLASHDEAL</div>
        <div class="flash-hero__sub">KHUNG GIỜ VÀNG</div>
        <div class="flash-hero__discount">GIÁ GIẢM ĐẾN <strong>25%</strong></div>
      </div>

      <div class="flash-hero__schedule">
        <div class="flash-hero__schedule-left">
          <span class="flash-hero__clock">⏰</span>
          <span class="flash-hero__schedule-text">DUY NHẤT HÔM NAY</span>
          <span class="flash-hero__time-pill">09:00</span>
          <span class="flash-hero__time-pill">12:00</span>
          <span class="flash-hero__time-pill">18:00</span>
          <span class="flash-hero__time-pill">21:00</span>
        </div>

        <div class="flash-hero__schedule-right">
          <span class="flash-hero__clock">⏰</span>
          <span class="flash-hero__schedule-text">ĐỘC QUYỀN TẠI WEBSITE MYCOSMETIC</span>
          <span class="flash-hero__time-pill">12:00</span>
          <span class="flash-hero__time-pill">18:00</span>
        </div>
      </div>
    </section>

    <!-- COUNTDOWN -->
    <section class="flash-countdown-section">
      <div class="flash-countdown-label">KẾT THÚC TRONG</div>

      <div class="flash-countdown"
           id="flashCountdown"
           data-hours="${requestScope.countdownHours != null ? requestScope.countdownHours : 1}"
           data-minutes="${requestScope.countdownMinutes != null ? requestScope.countdownMinutes : 17}"
           data-seconds="${requestScope.countdownSeconds != null ? requestScope.countdownSeconds : 45}">
        <span id="cdHours">00</span>
        <b>:</b>
        <span id="cdMinutes">00</span>
        <b>:</b>
        <span id="cdSeconds">00</span>
      </div>
    </section>

    <!-- KHUNG GIỜ -->
    <section class="flash-time-slots">
      <a class="flash-time-slot ${selectedSlot eq '09:00' ? 'active' : ''}"
         href="${ctx}/flash-sale?slot=09:00&category=${selectedCategory}">
        <strong>09:00</strong>
        <span>${selectedSlot eq '09:00' ? 'Đang Diễn Ra' : 'Sắp Diễn Ra'}</span>
      </a>

      <a class="flash-time-slot ${selectedSlot eq '12:00' ? 'active' : ''}"
         href="${ctx}/flash-sale?slot=12:00&category=${selectedCategory}">
        <strong>12:00</strong>
        <span>${selectedSlot eq '12:00' ? 'Đang Diễn Ra' : 'Sắp Diễn Ra'}</span>
      </a>

      <a class="flash-time-slot ${selectedSlot eq '15:00' ? 'active' : ''}"
         href="${ctx}/flash-sale?slot=15:00&category=${selectedCategory}">
        <strong>15:00</strong>
        <span>${selectedSlot eq '15:00' ? 'Đang Diễn Ra' : 'Sắp Diễn Ra'}</span>
      </a>

      <a class="flash-time-slot ${selectedSlot eq '21:00' ? 'active' : ''}"
         href="${ctx}/flash-sale?slot=21:00&category=${selectedCategory}">
        <strong>21:00</strong>
        <span>${selectedSlot eq '21:00' ? 'Đang Diễn Ra' : 'Sắp Diễn Ra'}</span>
      </a>
    </section>

    <!-- TAB DANH MỤC -->
    <section class="flash-category-tabs">
      <a class="flash-category-tab ${selectedCategory eq 'TOP_PICK' ? 'active' : ''}"
         href="${ctx}/flash-sale?slot=${selectedSlot}&category=TOP_PICK">TOP PICK</a>

      <a class="flash-category-tab ${selectedCategory eq 'MUA_LA_CO_QUA' ? 'active' : ''}"
         href="${ctx}/flash-sale?slot=${selectedSlot}&category=MUA_LA_CO_QUA">MUA LÀ CÓ QUÀ</a>

      <a class="flash-category-tab ${selectedCategory eq 'SKINCARE' ? 'active' : ''}"
         href="${ctx}/flash-sale?slot=${selectedSlot}&category=SKINCARE">SKINCARE</a>

      <a class="flash-category-tab ${selectedCategory eq 'MAKEUP' ? 'active' : ''}"
         href="${ctx}/flash-sale?slot=${selectedSlot}&category=MAKEUP">MAKEUP</a>

      <a class="flash-category-tab ${selectedCategory eq 'QUA_TANG' ? 'active' : ''}"
         href="${ctx}/flash-sale?slot=${selectedSlot}&category=QUA_TANG">QUÀ TẶNG</a>
    </section>

    <!-- DANH SÁCH SẢN PHẨM -->
    <section class="flash-products-section">
      <c:choose>
        <c:when test="${not empty dealProducts}">
          <div class="flash-products-grid">
            <c:forEach var="product" items="${dealProducts}">
              <article class="flash-product-card">

                <c:choose>
                  <c:when test="${not empty product.slug}">
                    <c:set var="productUrl" value="${ctx}/products/${product.slug}" />
                  </c:when>
                  <c:otherwise>
                    <c:set var="productUrl" value="${ctx}/product?id=${product.id}" />
                  </c:otherwise>
                </c:choose>

                <a class="flash-product-image-link" href="${productUrl}">
                  <div class="flash-product-image-box">
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
                        <div class="flash-no-image">No image</div>
                      </c:otherwise>
                    </c:choose>
                  </div>
                </a>

                <div class="flash-badge-row">
                  <span class="flash-badge flash-badge-ship">FREESHIP TQ</span>
                  <span class="flash-badge flash-badge-deal">FLASH DEAL</span>

                  <c:if test="${product.discountPercent > 0}">
                    <span class="flash-discount-bubble">-${product.discountPercent}%</span>
                  </c:if>
                </div>

                <h3 class="flash-product-title">
                  <a href="${productUrl}">${product.title}</a>
                </h3>

                <div class="flash-price-row">
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

                <div class="flash-product-bottom">
                  <div class="flash-progress-wrap">
                    <div class="flash-progress">
                      <span style="width: ${empty product.saleProgressPercent ? 75 : product.saleProgressPercent}%;"></span>
                    </div>
                    <div class="flash-progress-text">
                      ĐANG DIỄN RA ${empty product.saleProgressPercent ? 75 : product.saleProgressPercent}%
                    </div>
                  </div>

                  <a class="flash-buy-btn" href="${productUrl}">
                    MUA<br>NGAY
                  </a>
                </div>
              </article>
            </c:forEach>
          </div>
        </c:when>

        <c:otherwise>
          <div class="flash-empty">
            <h3>Chưa có sản phẩm Flash Deal</h3>
            <p>Hiện chưa có chương trình Flash Deal đang hoạt động. Vui lòng quay lại sau.</p>
          </div>
        </c:otherwise>
      </c:choose>
    </section>

  </div>
</main>

<jsp:include page="/jsp/common/footer.jsp" />

<script>
  (function () {
    const countdown = document.getElementById("flashCountdown");
    if (!countdown) return;

    let hours = parseInt(countdown.getAttribute("data-hours") || "0", 10);
    let minutes = parseInt(countdown.getAttribute("data-minutes") || "0", 10);
    let seconds = parseInt(countdown.getAttribute("data-seconds") || "0", 10);

    const hEl = document.getElementById("cdHours");
    const mEl = document.getElementById("cdMinutes");
    const sEl = document.getElementById("cdSeconds");

    function pad(num) {
      return String(num).padStart(2, "0");
    }

    function render() {
      hEl.textContent = pad(hours);
      mEl.textContent = pad(minutes);
      sEl.textContent = pad(seconds);
    }

    function tick() {
      if (hours === 0 && minutes === 0 && seconds === 0) {
        render();
        return;
      }

      if (seconds > 0) {
        seconds--;
      } else {
        seconds = 59;

        if (minutes > 0) {
          minutes--;
        } else {
          minutes = 59;

          if (hours > 0) {
            hours--;
          } else {
            hours = 0;
            minutes = 0;
            seconds = 0;
          }
        }
      }

      render();
    }

    render();
    setInterval(tick, 1000);
  })();
</script>

</body>
</html>

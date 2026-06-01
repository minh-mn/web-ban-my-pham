<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="selectedSlot" value="${empty param.slot ? '09:00' : param.slot}" />
<c:set var="selectedCategory" value="${empty param.category ? 'TOP_PICK' : param.category}" />

<!--
Không include header/footer ở file này vì FlashSaleServlet đã render qua base.jsp.
Nếu include header/footer ở đây sẽ bị 2 thanh menu.
-->
<section class="flash-page">
  <div class="flash-container">

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

    <section class="flash-countdown-section">
      <div class="flash-countdown-label">KẾT THÚC TRONG</div>

      <div class="flash-countdown"
           id="flashCountdown"
           data-end-time="${not empty activeFlashSale ? activeFlashSale.endTime.time : 0}">
        <span id="cdHours">00</span>
        <b>:</b>
        <span id="cdMinutes">00</span>
        <b>:</b>
        <span id="cdSeconds">00</span>
      </div>
    </section>

    <div id="flash-control-panel" class="flash-control-panel">
      <section class="flash-time-slots">
        <a class="flash-time-slot ${selectedSlot eq '09:00' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=09:00&category=${selectedCategory}"
           data-flash-nav>
          <strong>09:00</strong>
          <span>${selectedSlot eq '09:00' ? 'Đang Diễn Ra' : 'Sắp Diễn Ra'}</span>
        </a>

        <a class="flash-time-slot ${selectedSlot eq '12:00' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=12:00&category=${selectedCategory}"
           data-flash-nav>
          <strong>12:00</strong>
          <span>${selectedSlot eq '12:00' ? 'Đang Diễn Ra' : 'Sắp Diễn Ra'}</span>
        </a>

        <a class="flash-time-slot ${selectedSlot eq '15:00' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=15:00&category=${selectedCategory}"
           data-flash-nav>
          <strong>15:00</strong>
          <span>${selectedSlot eq '15:00' ? 'Đang Diễn Ra' : 'Sắp Diễn Ra'}</span>
        </a>

        <a class="flash-time-slot ${selectedSlot eq '21:00' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=21:00&category=${selectedCategory}"
           data-flash-nav>
          <strong>21:00</strong>
          <span>${selectedSlot eq '21:00' ? 'Đang Diễn Ra' : 'Sắp Diễn Ra'}</span>
        </a>
      </section>

      <section class="flash-category-tabs">
        <a class="flash-category-tab ${selectedCategory eq 'TOP_PICK' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=${selectedSlot}&category=TOP_PICK"
           data-flash-nav>TOP PICK</a>

        <a class="flash-category-tab ${selectedCategory eq 'MUA_LA_CO_QUA' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=${selectedSlot}&category=MUA_LA_CO_QUA"
           data-flash-nav>MUA LÀ CÓ QUÀ</a>

        <a class="flash-category-tab ${selectedCategory eq 'SKINCARE' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=${selectedSlot}&category=SKINCARE"
           data-flash-nav>SKINCARE</a>

        <a class="flash-category-tab ${selectedCategory eq 'MAKEUP' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=${selectedSlot}&category=MAKEUP"
           data-flash-nav>MAKEUP</a>

        <a class="flash-category-tab ${selectedCategory eq 'QUA_TANG' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=${selectedSlot}&category=QUA_TANG"
           data-flash-nav>QUÀ TẶNG</a>
      </section>
    </div>

    <section class="flash-products-section">
      <c:choose>
        <c:when test="${not empty fsItems}">
          <div class="flash-products-grid">
            <c:forEach var="item" items="${fsItems}">
              <c:set var="product" value="${item.product}" />
              <c:set var="flashSoldPercent" value="${empty item.soldPercent ? 0 : item.soldPercent}" />
              <c:set var="flashLimit" value="${empty item.maxQuantityPerUser ? 2 : item.maxQuantityPerUser}" />
              <c:set var="flashSoldOut" value="${item.soldOut or item.remainQuantity <= 0 or flashSoldPercent >= 100}" />

              <article class="flash-product-card ${flashSoldOut ? 'is-sold-out' : ''}"
                       data-flash-limit="${flashLimit}"
                       data-sold-out="${flashSoldOut}">
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
                  <span class="flash-badge flash-badge-limit">
                    Giới hạn ${flashLimit}/khách
                  </span>

                  <c:if test="${product.discountPercent > 0}">
                    <span class="flash-discount-bubble">-${product.discountPercent}%</span>
                  </c:if>
                </div>

                <h3 class="flash-product-title">
                  <a href="${productUrl}">${product.title}</a>
                </h3>

                <div class="flash-price-row">
                  <strong>
                    <fmt:formatNumber value="${item.flashPrice}" type="number" groupingUsed="true"/>đ
                  </strong>

                  <c:if test="${product.price > item.flashPrice}">
                    <del>
                      <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>đ
                    </del>
                  </c:if>
                </div>

                <div class="flash-product-bottom">
                  <div class="flash-progress-wrap">
                    <div class="flash-progress flash-stock-progress"
                         role="progressbar"
                         aria-label="Tiến độ đã bán"
                         aria-valuemin="0"
                         aria-valuemax="100"
                         aria-valuenow="${flashSoldPercent}">
                      <span style="width: ${flashSoldPercent}%;"></span>
                    </div>

                    <div class="flash-progress-text flash-stock-progress-text"
                         data-progress-text
                         data-percent="${flashSoldPercent}">
                      <c:choose>
                        <c:when test="${flashSoldPercent >= 100}">
                          Đã bán hết
                        </c:when>
                        <c:when test="${flashSoldPercent <= 0}">
                          Vừa mở bán
                        </c:when>
                        <c:otherwise>
                          Đã bán ${flashSoldPercent}%
                        </c:otherwise>
                      </c:choose>
                    </div>

                    <div class="flash-purchase-limit">
                      Mỗi khách tối đa ${flashLimit} sản phẩm
                    </div>
                  </div>

                  <c:choose>
                    <c:when test="${flashSoldOut}">
                      <span class="flash-buy-btn is-disabled is-sold-out" aria-disabled="true">
                        ĐÃ<br>HẾT
                      </span>
                    </c:when>
                    <c:otherwise>
                      <a class="flash-buy-btn" href="${productUrl}">
                        MUA<br>NGAY
                      </a>
                    </c:otherwise>
                  </c:choose>
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
</section>

<script>
  (function () {
    const countdown = document.getElementById("flashCountdown");
    if (!countdown) return;

    const hEl = document.getElementById("cdHours");
    const mEl = document.getElementById("cdMinutes");
    const sEl = document.getElementById("cdSeconds");

    function pad(num) {
      return String(num).padStart(2, "0");
    }

    function render() {
      const rawEndTime = parseInt(countdown.getAttribute("data-end-time") || "0", 10);
      const fallbackEnd = new Date();
      fallbackEnd.setHours(23, 59, 59, 999);

      const endTime = rawEndTime > 0 ? rawEndTime : fallbackEnd.getTime();
      let distance = endTime - Date.now();

      if (distance < 0) {
        distance = 0;
      }

      const hours = Math.floor(distance / (1000 * 60 * 60));
      const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((distance % (1000 * 60)) / 1000);

      hEl.textContent = pad(hours);
      mEl.textContent = pad(minutes);
      sEl.textContent = pad(seconds);
    }

    render();
    setInterval(render, 1000);
  })();

  /*
      Fix giật màn hình:
      - Không dùng #hash.
      - Không scrollIntoView smooth sau reload.
      - Lưu vị trí hiện tại trước khi bấm tab/khung giờ.
      - Khi trang load lại, trả về đúng vị trí cũ ngay lập tức.
  */
  (function () {
    const STORAGE_KEY = "flashSaleScrollY";

    if ("scrollRestoration" in history) {
      history.scrollRestoration = "manual";
    }

    document.querySelectorAll("[data-flash-nav]").forEach(function (link) {
      link.addEventListener("click", function () {
        sessionStorage.setItem(STORAGE_KEY, String(window.scrollY || window.pageYOffset || 0));
      });
    });

    window.addEventListener("load", function () {
      const savedY = sessionStorage.getItem(STORAGE_KEY);
      if (savedY === null) return;

      sessionStorage.removeItem(STORAGE_KEY);

      const targetY = parseInt(savedY, 10);
      if (Number.isNaN(targetY)) return;

      requestAnimationFrame(function () {
        window.scrollTo({
          top: targetY,
          left: 0,
          behavior: "auto"
        });
      });
    });
  })();
</script>

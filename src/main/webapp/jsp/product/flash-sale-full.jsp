<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="selectedSlot" value="${empty param.slot ? '09:00' : param.slot}" />
<c:set var="selectedCategory" value="${empty param.category ? 'TOP_PICK' : param.category}" />

<!--
File này được FlashSaleServlet render qua base.jsp.
Không include header/footer ở đây để tránh bị trùng menu.
-->
<section class="flash-page" data-selected-slot="${selectedSlot}" data-selected-category="${selectedCategory}">
  <div class="flash-container">

    <section class="flash-hero">
      <div class="flash-hero__brand">MyCosmetic</div>

      <div class="flash-hero__content">
        <div class="flash-hero__title" aria-label="Flash Deal">
          <span>Flash</span>
          <b class="flash-title-bolt" aria-hidden="true">⚡</b>
          <span>Deal</span>
        </div>
        <div class="flash-hero__sub">KHUNG GIỜ VÀNG</div>
        <div class="flash-hero__discount">GIÁ GIẢM ĐẾN <strong>25%</strong></div>
      </div>

      <div class="flash-hero__schedule" aria-label="Khung giờ Flash Sale hôm nay">
        <div class="flash-hero__schedule-group flash-hero__schedule-group--main">
          <span class="flash-hero__clock" aria-hidden="true">⏰</span>
          <span class="flash-hero__schedule-text">DUY NHẤT HÔM NAY</span>

          <div class="flash-hero__time-list" aria-label="Các khung giờ duy nhất hôm nay">
            <a class="flash-hero__time-pill" href="${ctx}/flash-sale?slot=09:00&category=${selectedCategory}" data-flash-nav data-slot="09:00">09:00</a>
            <a class="flash-hero__time-pill" href="${ctx}/flash-sale?slot=12:00&category=${selectedCategory}" data-flash-nav data-slot="12:00">12:00</a>
            <a class="flash-hero__time-pill" href="${ctx}/flash-sale?slot=15:00&category=${selectedCategory}" data-flash-nav data-slot="15:00">15:00</a>
            <a class="flash-hero__time-pill" href="${ctx}/flash-sale?slot=21:00&category=${selectedCategory}" data-flash-nav data-slot="21:00">21:00</a>
          </div>
        </div>

        <div class="flash-hero__schedule-group flash-hero__schedule-group--right">
          <span class="flash-hero__clock" aria-hidden="true">⏰</span>
          <span class="flash-hero__schedule-text">ĐỘC QUYỀN TẠI WEBSITE MYCOSMETIC</span>

          <div class="flash-hero__time-list flash-hero__time-list--compact" aria-label="Khung giờ nổi bật độc quyền">
            <a class="flash-hero__time-pill compact" href="${ctx}/flash-sale?slot=12:00&category=${selectedCategory}" data-flash-nav data-slot="12:00">12:00</a>
            <a class="flash-hero__time-pill compact" href="${ctx}/flash-sale?slot=15:00&category=${selectedCategory}" data-flash-nav data-slot="15:00">15:00</a>
          </div>
        </div>
      </div>
    </section>

    <section class="flash-countdown-section">
      <div class="flash-countdown-label" id="flashCountdownLabel">KẾT THÚC TRONG</div>

      <div class="flash-countdown"
           id="flashCountdown"
           data-sale-end-time="${not empty activeFlashSale ? activeFlashSale.endTime.time : 0}">
        <span id="cdHours">00</span>
        <b>:</b>
        <span id="cdMinutes">00</span>
        <b>:</b>
        <span id="cdSeconds">00</span>
      </div>
    </section>

    <div id="flash-control-panel" class="flash-control-panel">
      <section class="flash-time-slots" aria-label="Chọn khung giờ Flash Sale">
        <a class="flash-time-slot ${selectedSlot eq '09:00' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=09:00&category=${selectedCategory}"
           data-flash-nav data-slot="09:00">
          <strong>09:00</strong>
          <span data-slot-status>Đang cập nhật</span>
        </a>

        <a class="flash-time-slot ${selectedSlot eq '12:00' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=12:00&category=${selectedCategory}"
           data-flash-nav data-slot="12:00">
          <strong>12:00</strong>
          <span data-slot-status>Đang cập nhật</span>
        </a>

        <a class="flash-time-slot ${selectedSlot eq '15:00' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=15:00&category=${selectedCategory}"
           data-flash-nav data-slot="15:00">
          <strong>15:00</strong>
          <span data-slot-status>Đang cập nhật</span>
        </a>

        <a class="flash-time-slot ${selectedSlot eq '21:00' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=21:00&category=${selectedCategory}"
           data-flash-nav data-slot="21:00">
          <strong>21:00</strong>
          <span data-slot-status>Đang cập nhật</span>
        </a>
      </section>

      <section class="flash-category-tabs" aria-label="Danh mục Flash Sale">
        <a class="flash-category-tab ${selectedCategory eq 'TOP_PICK' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=${selectedSlot}&category=TOP_PICK"
           data-flash-nav data-category="TOP_PICK">TOP PICK</a>

        <a class="flash-category-tab ${selectedCategory eq 'MUA_LA_CO_QUA' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=${selectedSlot}&category=MUA_LA_CO_QUA"
           data-flash-nav data-category="MUA_LA_CO_QUA">MUA LÀ CÓ QUÀ</a>

        <a class="flash-category-tab ${selectedCategory eq 'SKINCARE' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=${selectedSlot}&category=SKINCARE"
           data-flash-nav data-category="SKINCARE">SKINCARE</a>

        <a class="flash-category-tab ${selectedCategory eq 'MAKEUP' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=${selectedSlot}&category=MAKEUP"
           data-flash-nav data-category="MAKEUP">MAKEUP</a>

        <a class="flash-category-tab ${selectedCategory eq 'QUA_TANG' ? 'active' : ''}"
           href="${ctx}/flash-sale?slot=${selectedSlot}&category=QUA_TANG"
           data-flash-nav data-category="QUA_TANG">QUÀ TẶNG</a>
      </section>
    </div>

    <section class="flash-products-section" id="flashProductsSection">
      <c:choose>
        <c:when test="${not empty fsItems}">
          <div class="flash-products-grid" id="flashProductsGrid">
            <c:forEach var="item" items="${fsItems}">
              <c:set var="product" value="${item.product}" />

              <article class="flash-product-card" data-flash-card>
                <c:choose>
                  <c:when test="${not empty product.slug}">
                    <c:set var="productUrl" value="${ctx}/product/${product.slug}" />
                  </c:when>
                  <c:otherwise>
                    <c:set var="productUrl" value="${ctx}/product?id=${product.id}" />
                  </c:otherwise>
                </c:choose>

                <c:set var="resolvedImage" value="${not empty product.imageUrl ? product.imageUrl : product.image}" />

                <a class="flash-product-image-link" href="${productUrl}">
                  <div class="flash-product-image-box">
                    <c:choose>
                      <c:when test="${not empty resolvedImage}">
                        <c:choose>
                          <c:when test="${fn:startsWith(resolvedImage, 'http')}">
                            <img src="${resolvedImage}" alt="${product.title}" loading="lazy"
                                 onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                          </c:when>
                          <c:when test="${fn:startsWith(resolvedImage, '/')}">
                            <img src="${ctx}${resolvedImage}" alt="${product.title}" loading="lazy"
                                 onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                          </c:when>
                          <c:otherwise>
                            <img src="${ctx}/uploads/product/${resolvedImage}" alt="${product.title}" loading="lazy"
                                 onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                          </c:otherwise>
                        </c:choose>
                        <div class="flash-no-image fallback" style="display:none;">MyCosmetic</div>
                      </c:when>
                      <c:otherwise>
                        <div class="flash-no-image">MyCosmetic</div>
                      </c:otherwise>
                    </c:choose>
                  </div>
                </a>

                <div class="flash-badge-row">
                  <span class="flash-badge flash-badge-deal" data-card-badge>FLASH DEAL</span>

                  <c:if test="${product.discountPercent > 0}">
                    <span class="flash-discount-bubble">-${product.discountPercent}%</span>
                  </c:if>
                </div>

                <h3 class="flash-product-title">
                  <a href="${productUrl}">${product.title}</a>
                </h3>

                <div class="flash-price-row" data-price-row>
                  <div class="flash-price-meta">
                    <c:if test="${product.price > item.flashPrice}">
                      <del>
                        <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>đ
                      </del>
                    </c:if>
                    <span class="flash-price-status" data-price-status>Giá Flash Sale</span>
                  </div>

                  <strong class="flash-price-sale">
                    <fmt:formatNumber value="${item.flashPrice}" type="number" groupingUsed="true"/>đ
                  </strong>
                </div>

                <div class="flash-product-bottom">
                  <div class="flash-progress-wrap">
                    <div class="flash-progress">
                      <span style="width: ${item.soldPercent <= 0 ? 18 : item.soldPercent}%;"></span>
                    </div>
                    <div class="flash-progress-text" data-progress-text data-percent="${item.soldPercent <= 0 ? 18 : item.soldPercent}">
                      ĐANG DIỄN RA ${item.soldPercent <= 0 ? 18 : item.soldPercent}%
                    </div>
                  </div>

                  <a class="flash-buy-btn" href="${productUrl}" data-buy-url="${productUrl}" data-buy-btn>
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
</section>

<script>
  (function () {
    const SLOT_TIMES = ["09:00", "12:00", "15:00", "21:00"];
    const page = document.querySelector(".flash-page");
    if (!page) return;

    const countdown = document.getElementById("flashCountdown");
    const labelEl = document.getElementById("flashCountdownLabel");
    const hEl = document.getElementById("cdHours");
    const mEl = document.getElementById("cdMinutes");
    const sEl = document.getElementById("cdSeconds");
    const productGrid = document.getElementById("flashProductsGrid");

    function pad(num) {
      return String(Math.max(0, num)).padStart(2, "0");
    }

    function slotDate(slot) {
      const parts = slot.split(":");
      const d = new Date();
      d.setHours(parseInt(parts[0], 10), parseInt(parts[1], 10), 0, 0);
      return d;
    }

    function saleEndDate() {
      const raw = countdown ? parseInt(countdown.getAttribute("data-sale-end-time") || "0", 10) : 0;
      if (raw > 0) return new Date(raw);

      const fallback = new Date();
      fallback.setHours(23, 59, 59, 999);
      return fallback;
    }

    function getSlotState(slot) {
      const now = new Date();
      const idx = SLOT_TIMES.indexOf(slot);
      const start = slotDate(slot);
      let end;

      if (idx >= 0 && idx < SLOT_TIMES.length - 1) {
        end = slotDate(SLOT_TIMES[idx + 1]);
      } else {
        end = saleEndDate();
        if (end <= start) {
          end = new Date(start);
          end.setHours(23, 59, 59, 999);
        }
      }

      if (now < start) {
        return {name: "upcoming", text: "Sắp Diễn Ra", target: start, label: "BẮT ĐẦU TRONG"};
      }

      if (now >= start && now < end) {
        return {name: "running", text: "Đang Diễn Ra", target: end, label: "KẾT THÚC TRONG"};
      }

      return {name: "ended", text: "Đã Kết Thúc", target: now, label: "ĐÃ KẾT THÚC"};
    }

    function currentRealSlot() {
      for (let i = 0; i < SLOT_TIMES.length; i++) {
        const state = getSlotState(SLOT_TIMES[i]);
        if (state.name === "running") return SLOT_TIMES[i];
      }

      const now = new Date();
      for (let i = 0; i < SLOT_TIMES.length; i++) {
        if (now < slotDate(SLOT_TIMES[i])) return SLOT_TIMES[i];
      }

      return SLOT_TIMES[SLOT_TIMES.length - 1];
    }

    function selectedSlotFromUrl() {
      const params = new URLSearchParams(window.location.search);
      const slot = params.get("slot") || page.getAttribute("data-selected-slot") || "";
      return SLOT_TIMES.includes(slot) ? slot : currentRealSlot();
    }

    function selectedCategoryFromUrl() {
      const params = new URLSearchParams(window.location.search);
      return params.get("category") || page.getAttribute("data-selected-category") || "TOP_PICK";
    }

    function applyPageState(stateName) {
      page.classList.remove("is-slot-running", "is-slot-upcoming", "is-slot-ended");
      page.classList.add("is-slot-" + stateName);
      page.setAttribute("data-current-slot-state", stateName);
    }

    function applySlotUi(selectedSlot) {
      document.querySelectorAll("[data-slot]").forEach(function (el) {
        const slot = el.getAttribute("data-slot");
        const state = getSlotState(slot);

        el.classList.toggle("active", slot === selectedSlot);
        el.classList.toggle("running", state.name === "running");
        el.classList.toggle("upcoming", state.name === "upcoming");
        el.classList.toggle("ended", state.name === "ended");

        const statusEl = el.querySelector("[data-slot-status]");
        if (statusEl) statusEl.textContent = state.text;
      });
    }

    function applyCategoryUi(selectedCategory) {
      document.querySelectorAll("[data-category]").forEach(function (el) {
        el.classList.toggle("active", el.getAttribute("data-category") === selectedCategory);
      });
    }

    function applyProductCardState(selectedState) {
      document.querySelectorAll("[data-flash-card]").forEach(function (card) {
        card.classList.remove("is-running", "is-upcoming", "is-ended");
        card.classList.add("is-" + selectedState.name);

        const badge = card.querySelector("[data-card-badge]");
        const progressText = card.querySelector("[data-progress-text]");
        const priceStatus = card.querySelector("[data-price-status]");
        const buyBtn = card.querySelector("[data-buy-btn]");
        const percent = progressText ? (progressText.getAttribute("data-percent") || "0") : "0";

        if (selectedState.name === "running") {
          if (badge) badge.textContent = "FLASH DEAL";
          if (progressText) progressText.textContent = "ĐANG DIỄN RA " + percent + "%";
          if (priceStatus) priceStatus.textContent = "Giá Flash Sale";
          if (buyBtn) {
            buyBtn.innerHTML = "MUA<br>NGAY";
            buyBtn.classList.remove("is-disabled");
            buyBtn.setAttribute("href", buyBtn.getAttribute("data-buy-url") || "#");
            buyBtn.setAttribute("aria-disabled", "false");
          }
          return;
        }

        if (selectedState.name === "upcoming") {
          if (badge) badge.textContent = "SẮP MỞ";
          if (progressText) progressText.textContent = "SẮP DIỄN RA";
          if (priceStatus) priceStatus.textContent = "Giá mở bán";
          if (buyBtn) {
            buyBtn.innerHTML = "SẮP<br>MỞ";
            buyBtn.classList.add("is-disabled");
            buyBtn.removeAttribute("href");
            buyBtn.setAttribute("aria-disabled", "true");
          }
          return;
        }

        if (badge) badge.textContent = "ĐÃ KẾT THÚC";
        if (progressText) progressText.textContent = "ĐÃ KẾT THÚC";
        if (priceStatus) priceStatus.textContent = "Giá đã kết thúc";
        if (buyBtn) {
          buyBtn.innerHTML = "TẠM<br>HẾT";
          buyBtn.classList.add("is-disabled");
          buyBtn.removeAttribute("href");
          buyBtn.setAttribute("aria-disabled", "true");
        }
      });
    }

    function updateCountdown(selectedSlot) {
      if (!countdown || !hEl || !mEl || !sEl) return;

      const state = getSlotState(selectedSlot);
      if (labelEl) labelEl.textContent = state.label;

      let distance = state.target.getTime() - Date.now();
      if (state.name === "ended" || distance < 0) distance = 0;

      const hours = Math.floor(distance / (1000 * 60 * 60));
      const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((distance % (1000 * 60)) / 1000);

      hEl.textContent = pad(hours);
      mEl.textContent = pad(minutes);
      sEl.textContent = pad(seconds);

      applyPageState(state.name);
      applyProductCardState(state);
    }

    function updateLinks(selectedSlot, selectedCategory) {
      document.querySelectorAll("[data-flash-nav]").forEach(function (link) {
        const url = new URL(link.getAttribute("href"), window.location.origin);
        const slot = link.getAttribute("data-slot") || selectedSlot;
        const category = link.getAttribute("data-category") || selectedCategory;

        url.searchParams.set("slot", slot);
        url.searchParams.set("category", category);
        link.setAttribute("href", url.pathname + url.search);
      });
    }

    function setState(nextSlot, nextCategory, pushHistory) {
      const selectedSlot = SLOT_TIMES.includes(nextSlot) ? nextSlot : currentRealSlot();
      const selectedCategory = nextCategory || "TOP_PICK";

      page.setAttribute("data-selected-slot", selectedSlot);
      page.setAttribute("data-selected-category", selectedCategory);

      applySlotUi(selectedSlot);
      applyCategoryUi(selectedCategory);
      updateLinks(selectedSlot, selectedCategory);
      updateCountdown(selectedSlot);

      if (pushHistory) {
        const url = new URL(window.location.href);
        url.searchParams.set("slot", selectedSlot);
        url.searchParams.set("category", selectedCategory);
        history.pushState({slot: selectedSlot, category: selectedCategory}, "", url.pathname + url.search);
      }
    }

    function animateGrid() {
      if (!productGrid) return;
      productGrid.classList.remove("is-smooth-switch");
      void productGrid.offsetWidth;
      productGrid.classList.add("is-smooth-switch");
    }

    document.querySelectorAll("[data-flash-nav]").forEach(function (link) {
      link.addEventListener("click", function (event) {
        event.preventDefault();

        const currentSlot = page.getAttribute("data-selected-slot") || selectedSlotFromUrl();
        const currentCategory = page.getAttribute("data-selected-category") || selectedCategoryFromUrl();
        const nextSlot = link.getAttribute("data-slot") || currentSlot;
        const nextCategory = link.getAttribute("data-category") || currentCategory;

        animateGrid();
        setState(nextSlot, nextCategory, true);
      });
    });

    document.addEventListener("click", function (event) {
      const disabledBuy = event.target.closest(".flash-buy-btn.is-disabled");
      if (disabledBuy) {
        event.preventDefault();
      }
    });

    window.addEventListener("popstate", function () {
      setState(selectedSlotFromUrl(), selectedCategoryFromUrl(), false);
    });

    const hasSlotParam = new URLSearchParams(window.location.search).has("slot");
    setState(hasSlotParam ? selectedSlotFromUrl() : currentRealSlot(), selectedCategoryFromUrl(), false);

    setInterval(function () {
      setState(
              page.getAttribute("data-selected-slot") || selectedSlotFromUrl(),
              page.getAttribute("data-selected-category") || selectedCategoryFromUrl(),
              false
      );
    }, 1000);
  })();
</script>

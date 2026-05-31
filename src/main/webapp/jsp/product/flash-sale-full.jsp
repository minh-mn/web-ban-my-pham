<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<section class="mc-flash-page">
  <div class="mc-flash-container">

    <!-- Banner lớn giống trang Flash Sale tham khảo, dùng CSS thuần nên không cần ảnh ngoài -->
    <div class="mc-flash-hero">
      <div class="mc-flash-hero__brand">MyCosmetic</div>

      <div class="mc-flash-hero__center">
        <div class="mc-flash-ghost">FLASH DEAL</div>
        <h1>Khung giờ vàng</h1>
        <h2>Giá giảm đến <strong>25%</strong></h2>
      </div>

      <div class="mc-flash-hero__schedule">
        <span>Duy nhất hôm nay</span>
        <b>09:00</b>
        <b>12:00</b>
        <b>18:00</b>
        <b>21:00</b>
        <span>Độc quyền tại website MyCosmetic</span>
      </div>
    </div>

    <!-- Countdown -->
    <div class="mc-flash-countdown-wrap">
      <span class="mc-flash-countdown-label">KẾT THÚC TRONG</span>

      <c:choose>
        <c:when test="${not empty activeFlashSale}">
          <div class="mc-flash-countdown" id="flashCountdown" data-end-time="${activeFlashSale.endTime.time}">
            <span data-hh>00</span>
            <b>:</b>
            <span data-mm>00</span>
            <b>:</b>
            <span data-ss>00</span>
          </div>
        </c:when>
        <c:otherwise>
          <div class="mc-flash-countdown">
            <span>00</span>
            <b>:</b>
            <span>00</span>
            <b>:</b>
            <span>00</span>
          </div>
        </c:otherwise>
      </c:choose>
    </div>

    <!-- Khung giờ -->
    <div class="mc-flash-slots">
      <button type="button" class="mc-slot is-active">
        <strong>09:00</strong>
        <span>Đang diễn ra</span>
      </button>
      <button type="button" class="mc-slot">
        <strong>15:00</strong>
        <span>Sắp diễn ra</span>
      </button>
      <button type="button" class="mc-slot">
        <strong>21:00</strong>
        <span>Sắp diễn ra</span>
      </button>
    </div>

    <!-- Tab nhóm sản phẩm -->
    <div class="mc-flash-tabs">
      <button type="button" class="mc-flash-tab is-active" data-filter="all">TOP PICK</button>
      <button type="button" class="mc-flash-tab" data-filter="gift">MUA LÀ CÓ QUÀ</button>
      <button type="button" class="mc-flash-tab" data-filter="skincare">SKINCARE</button>
      <button type="button" class="mc-flash-tab" data-filter="makeup">MAKEUP</button>
      <button type="button" class="mc-flash-tab" data-filter="other">QUÀ TẶNG</button>
    </div>

    <c:choose>
      <c:when test="${empty fsItems}">
        <div class="mc-flash-empty">
          <h3>Chưa có sản phẩm Flash Deal</h3>
          <p>Hiện chưa có chương trình Flash Deal đang hoạt động. Vui lòng quay lại sau.</p>
        </div>
      </c:when>

      <c:otherwise>
        <div class="mc-flash-grid">
          <c:forEach var="item" items="${fsItems}">
            <c:set var="titleLower" value="${fn:toLowerCase(item.product.title)}" />
            <c:set var="categoryLower" value="${fn:toLowerCase(item.product.categoryName)}" />
            <c:set var="productType" value="other" />

            <c:if test="${fn:contains(titleLower, 'quà')
                                   or fn:contains(titleLower, 'gift')
                                   or fn:contains(titleLower, 'box')
                                   or fn:contains(titleLower, 'gương')
                                   or fn:contains(titleLower, 'cọ')
                                   or fn:contains(titleLower, 'phụ kiện')
                                   or fn:contains(categoryLower, 'phụ kiện')
                                   or fn:contains(categoryLower, 'gift')}">
              <c:set var="productType" value="gift" />
            </c:if>

            <c:if test="${fn:contains(titleLower, 'son')
                                   or fn:contains(titleLower, 'lip')
                                   or fn:contains(titleLower, 'phấn')
                                   or fn:contains(titleLower, 'cushion')
                                   or fn:contains(titleLower, 'mascara')
                                   or fn:contains(titleLower, 'eyeliner')
                                   or fn:contains(categoryLower, 'makeup')
                                   or fn:contains(categoryLower, 'trang điểm')
                                   or fn:contains(categoryLower, 'son')}">
              <c:set var="productType" value="makeup" />
            </c:if>

            <c:if test="${fn:contains(titleLower, 'serum')
                                   or fn:contains(titleLower, 'kem')
                                   or fn:contains(titleLower, 'toner')
                                   or fn:contains(titleLower, 'sữa rửa')
                                   or fn:contains(titleLower, 'mask')
                                   or fn:contains(titleLower, 'tẩy')
                                   or fn:contains(titleLower, 'dưỡng')
                                   or fn:contains(titleLower, 'essence')
                                   or fn:contains(categoryLower, 'skincare')
                                   or fn:contains(categoryLower, 'chăm sóc da')}">
              <c:set var="productType" value="skincare" />
            </c:if>

            <article class="mc-flash-card" data-category="${productType}">
              <a class="mc-flash-img" href="${ctx}/product/${item.product.slug}">
                <c:choose>
                  <c:when test="${not empty item.product.imageUrl}">
                    <c:choose>
                      <c:when test="${fn:startsWith(item.product.imageUrl, 'http')}">
                        <img src="${item.product.imageUrl}" alt="${item.product.title}">
                      </c:when>
                      <c:when test="${fn:startsWith(item.product.imageUrl, '/')}">
                        <img src="${ctx}${item.product.imageUrl}" alt="${item.product.title}">
                      </c:when>
                      <c:otherwise>
                        <img src="${ctx}/uploads/product/${item.product.imageUrl}" alt="${item.product.title}">
                      </c:otherwise>
                    </c:choose>
                  </c:when>
                  <c:otherwise>
                    <div class="mc-flash-no-image">No image</div>
                  </c:otherwise>
                </c:choose>

                <c:if test="${item.product.discountPercent > 0}">
                  <span class="mc-discount-badge">-${item.product.discountPercent}%</span>
                </c:if>
              </a>

              <div class="mc-flash-card-body">
                <div class="mc-card-badges">
                  <span>FREESHIP TQ</span>
                  <span>FLASH DEAL</span>
                </div>

                <a class="mc-flash-title" href="${ctx}/product/${item.product.slug}">
                    ${item.product.title}
                </a>

                <div class="mc-price-row">
                  <c:if test="${item.product.price > 0}">
                    <del>
                      <fmt:formatNumber value="${item.product.price}" type="number" groupingUsed="true"/>đ
                    </del>
                  </c:if>
                  <strong>
                    <fmt:formatNumber value="${item.flashPrice}" type="number" groupingUsed="true"/>đ
                  </strong>
                </div>

                <div class="mc-progress-wrap">
                  <div class="mc-progress">
                    <span style="width: ${item.soldPercent}%;"></span>
                  </div>
                  <small>ĐANG DIỄN RA ${item.soldPercent}%</small>
                </div>

                <div class="mc-card-footer">
                                    <span class="mc-remain">
                                        <c:choose>
                                          <c:when test="${item.remainQuantity <= 0}">
                                            TẠM HẾT
                                          </c:when>
                                          <c:otherwise>
                                            Còn ${item.remainQuantity} sản phẩm
                                          </c:otherwise>
                                        </c:choose>
                                    </span>

                  <a class="mc-buy-btn" href="${ctx}/product/${item.product.slug}">
                    MUA NGAY
                  </a>
                </div>
              </div>
            </article>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>

  </div>
</section>

<script>
  document.addEventListener("DOMContentLoaded", function () {
    const countdown = document.getElementById("flashCountdown");

    if (countdown) {
      const endTime = parseInt(countdown.getAttribute("data-end-time") || "0", 10);
      const hh = countdown.querySelector("[data-hh]");
      const mm = countdown.querySelector("[data-mm]");
      const ss = countdown.querySelector("[data-ss]");

      function renderCountdown() {
        const distance = endTime - Date.now();

        if (distance <= 0) {
          hh.textContent = "00";
          mm.textContent = "00";
          ss.textContent = "00";
          return;
        }

        const totalSeconds = Math.floor(distance / 1000);
        const hours = Math.floor(totalSeconds / 3600);
        const minutes = Math.floor((totalSeconds % 3600) / 60);
        const seconds = totalSeconds % 60;

        hh.textContent = String(hours).padStart(2, "0");
        mm.textContent = String(minutes).padStart(2, "0");
        ss.textContent = String(seconds).padStart(2, "0");
      }

      renderCountdown();
      setInterval(renderCountdown, 1000);
    }

    const tabs = document.querySelectorAll(".mc-flash-tab");
    const cards = document.querySelectorAll(".mc-flash-card");

    tabs.forEach(function (tab) {
      tab.addEventListener("click", function () {
        const filter = tab.getAttribute("data-filter");

        tabs.forEach(function (item) {
          item.classList.remove("is-active");
        });
        tab.classList.add("is-active");

        cards.forEach(function (card) {
          const category = card.getAttribute("data-category");
          card.style.display = (filter === "all" || filter === category) ? "" : "none";
        });
      });
    });
  });
</script>

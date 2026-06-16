<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<link rel="stylesheet" href="${ctx}/assets/css/brand-grid.css?v=20260616_brand_news_sync_2" />

<section class="brand-page" id="brandDirectory">
  <div class="brand-shell">
    <section class="brand-news-hero" aria-labelledby="brandHeroTitle">
      <span class="brand-news-kicker">DANH MỤC</span>
      <h1 id="brandHeroTitle">Thương hiệu chính hãng</h1>
      <p>
        Khám phá các thương hiệu mỹ phẩm, trang điểm và chăm sóc da được MyCosmetic chọn lọc,
        phân phối chính ngạch và đồng bộ sản phẩm liên quan cho từng thương hiệu.
      </p>

      <span class="brand-hero-shape brand-hero-shape--left" aria-hidden="true"></span>
      <span class="brand-hero-shape brand-hero-shape--right" aria-hidden="true"></span>
      <span class="brand-hero-dots" aria-hidden="true"></span>
    </section>

    <section class="brand-discovery-panel" aria-label="Khám phá và lọc thương hiệu">
      <div class="brand-toolbar" aria-label="Bộ lọc thương hiệu">
        <div class="brand-filter-chips" id="brandLetterFilters" aria-label="Lọc thương hiệu theo chữ cái">
          <button type="button" class="brand-filter-chip is-active" data-letter="ALL">Tất cả</button>
        </div>

        <div class="brand-search-box">
          <input id="brandSearchInput"
                 type="search"
                 autocomplete="off"
                 placeholder="Tìm thương hiệu..."
                 aria-label="Tìm thương hiệu" />
          <button id="brandSearchButton" type="button">Tìm</button>
        </div>
      </div>

      <div class="brand-section-head">
        <div class="brand-section-title-block">
          <span class="brand-section-label">Đối tác phân phối</span>
          <h2>Khám phá thương hiệu</h2>
          <p>Chọn chữ cái hoặc nhập tên thương hiệu để xem nhanh các sản phẩm liên quan.</p>
        </div>
        <span class="brand-section-count" id="brandVisibleCount">
          <c:out value="${fn:length(brands)}" /> thương hiệu
        </span>
      </div>
    </section>

    <div class="brand-grid" id="brandGrid">
      <c:forEach var="b" items="${brands}">
        <c:set var="brandName" value="${empty b.name ? 'Thương hiệu' : b.name}" />
        <c:set var="firstLetter" value="${fn:toUpperCase(fn:substring(brandName, 0, 1))}" />

        <a class="brand-card"
           data-letter="${firstLetter}"
           data-name="${fn:toLowerCase(brandName)}"
           href="${ctx}/products?brandId=${b.id}&brand=${fn:escapeXml(brandName)}"
           aria-label="Xem sản phẩm của thương hiệu ${brandName}">
          <div class="brand-card-media">
            <c:choose>
              <c:when test="${not empty b.image}">
                <img src="${ctx}${b.image}" alt="${brandName}" loading="lazy" />
              </c:when>
              <c:otherwise>
                <div class="brand-card-fallback">${firstLetter}</div>
              </c:otherwise>
            </c:choose>
          </div>

          <div class="brand-card-body">
            <span class="brand-card-letter">${firstLetter}</span>
            <h3 class="brand-card-name"><c:out value="${brandName}" /></h3>
            <span class="brand-card-link">Xem sản phẩm</span>
          </div>
        </a>
      </c:forEach>
    </div>

    <div class="brand-grid-empty" id="brandGridEmpty" hidden>
      <h3>Không tìm thấy thương hiệu</h3>
      <p>Hãy thử đổi chữ cái hoặc nhập từ khóa ngắn hơn để tìm thương hiệu phù hợp.</p>
      <button type="button" id="brandResetFilter">Xem tất cả thương hiệu</button>
    </div>
  </div>
</section>

<script>
  (function () {
    const grid = document.getElementById('brandGrid');
    const filterRoot = document.getElementById('brandLetterFilters');
    const searchInput = document.getElementById('brandSearchInput');
    const searchButton = document.getElementById('brandSearchButton');
    const resetButton = document.getElementById('brandResetFilter');
    const emptyState = document.getElementById('brandGridEmpty');
    const visibleCount = document.getElementById('brandVisibleCount');

    if (!grid || !filterRoot) return;

    const cards = Array.from(grid.querySelectorAll('.brand-card'));
    const letters = Array.from(new Set(cards.map(card => card.dataset.letter).filter(Boolean))).sort();
    let currentLetter = 'ALL';

    letters.forEach(function (letter) {
      const button = document.createElement('button');
      button.type = 'button';
      button.className = 'brand-filter-chip';
      button.dataset.letter = letter;
      button.textContent = letter;
      filterRoot.appendChild(button);
    });

    function normalizeText(value) {
      return (value || '')
              .toString()
              .toLowerCase()
              .normalize('NFD')
              .replace(/[\u0300-\u036f]/g, '')
              .replace(/đ/g, 'd')
              .trim();
    }

    function applyFilter() {
      const keyword = normalizeText(searchInput ? searchInput.value : '');
      let visible = 0;

      cards.forEach(function (card) {
        const letterMatched = currentLetter === 'ALL' || card.dataset.letter === currentLetter;
        const name = normalizeText(card.dataset.name || card.textContent);
        const keywordMatched = !keyword || name.indexOf(keyword) >= 0;
        const matched = letterMatched && keywordMatched;

        card.hidden = !matched;
        card.style.display = matched ? '' : 'none';

        if (matched) {
          visible += 1;
        }
      });

      if (emptyState) {
        emptyState.hidden = visible !== 0;
        emptyState.style.display = visible === 0 ? 'grid' : 'none';
      }

      if (visibleCount) {
        visibleCount.textContent = visible + ' thương hiệu';
      }
    }

    filterRoot.addEventListener('click', function (event) {
      const target = event.target.closest('.brand-filter-chip');
      if (!target) return;

      filterRoot.querySelectorAll('.brand-filter-chip').forEach(function (button) {
        button.classList.remove('is-active');
      });

      target.classList.add('is-active');
      currentLetter = target.dataset.letter || 'ALL';
      applyFilter();
    });

    if (searchInput) {
      searchInput.addEventListener('input', applyFilter);
      searchInput.addEventListener('keydown', function (event) {
        if (event.key === 'Enter') {
          event.preventDefault();
          applyFilter();
        }
      });
    }

    if (searchButton) {
      searchButton.addEventListener('click', applyFilter);
    }

    if (resetButton) {
      resetButton.addEventListener('click', function () {
        currentLetter = 'ALL';
        if (searchInput) searchInput.value = '';

        filterRoot.querySelectorAll('.brand-filter-chip').forEach(function (button) {
          button.classList.toggle('is-active', button.dataset.letter === 'ALL');
        });

        applyFilter();
      });
    }

    applyFilter();
  })();
</script>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${ctx}" />
<link rel="stylesheet" href="${ctx}/assets/css/brand-grid.css?v=20260615_brand_ui_fix_1" />


<section class="brand-hero-section">
  <div class="brand-hero-shell">
    <span class="brand-hero-badge">Đối tác phân phối chính hãng</span>
    <h2 class="brand-hero-title">Thương Hiệu Chính Hãng</h2>
    <p class="brand-hero-description">
      MyCosmetic là đối tác phân phối chiến lược của nhiều thương hiệu mỹ phẩm và chăm sóc da nổi tiếng toàn cầu.
      Chúng tôi cam kết 100% sản phẩm phân phối đạt chuẩn chính ngạch, an toàn và uy tín.
    </p>
  </div>
</section>

<section class="brand-directory-section" id="brandDirectory">
  <div class="brand-directory-shell">
    <div class="brand-directory-toolbar">
      <div class="brand-directory-heading">
        <h3>Khám phá thương hiệu</h3>
        <p>Chọn chữ cái hoặc nhấn trực tiếp vào thương hiệu để xem sản phẩm liên quan.</p>
      </div>

      <div class="brand-filter-wrap">
        <div class="brand-filter-chips" id="brandLetterFilters" aria-label="Bộ lọc chữ cái thương hiệu">
          <button type="button" class="brand-filter-chip is-active" data-letter="ALL">
            Tất cả
          </button>
        </div>
      </div>
    </div>

    <div class="brand-grid" id="brandGrid">
      <c:forEach var="b" items="${brands}">
        <c:set var="firstLetter" value="${fn:toUpperCase(fn:substring(b.name, 0, 1))}" />
        <a class="brand-card"
           data-letter="${firstLetter}"
           href="${ctx}/products?brandId=${b.id}&brand=${fn:escapeXml(b.name)}"
           aria-label="Xem sản phẩm của thương hiệu ${b.name}">
          <div class="brand-card-media">
            <c:choose>
              <c:when test="${not empty b.image}">
                <img src="${ctx}${b.image}"
                     alt="${b.name}"
                     loading="lazy" />
              </c:when>
              <c:otherwise>
                <div class="brand-card-fallback">${firstLetter}</div>
              </c:otherwise>
            </c:choose>
          </div>

          <div class="brand-card-body">
            <span class="brand-card-letter">${firstLetter}</span>
            <h4 class="brand-card-name"><c:out value="${b.name}" /></h4>
            <span class="brand-card-link">Xem sản phẩm</span>
          </div>
        </a>
      </c:forEach>
    </div>

    <div class="brand-grid-empty" id="brandGridEmpty" hidden>
      Không tìm thấy thương hiệu phù hợp với bộ lọc đã chọn.
    </div>
  </div>
</section>

<script>
  (function () {
    const grid = document.getElementById('brandGrid');
    const filterRoot = document.getElementById('brandLetterFilters');
    const emptyState = document.getElementById('brandGridEmpty');
    if (!grid || !filterRoot) return;

    const cards = Array.from(grid.querySelectorAll('.brand-card'));
    const letters = Array.from(new Set(cards.map(card => card.dataset.letter).filter(Boolean))).sort();

    letters.forEach(letter => {
      const button = document.createElement('button');
      button.type = 'button';
      button.className = 'brand-filter-chip';
      button.dataset.letter = letter;
      button.textContent = letter;
      filterRoot.appendChild(button);
    });

    const applyFilter = (letter) => {
      let visible = 0;
      cards.forEach(card => {
        const matched = letter === 'ALL' || card.dataset.letter === letter;
        card.hidden = !matched;
        card.style.display = matched ? '' : 'none';
        if (matched) visible++;
      });

      if (emptyState) {
        const showEmpty = visible === 0;
        emptyState.hidden = !showEmpty;
        emptyState.style.display = showEmpty ? 'grid' : 'none';
      }
    };

    filterRoot.addEventListener('click', function (event) {
      const target = event.target.closest('.brand-filter-chip');
      if (!target) return;

      filterRoot.querySelectorAll('.brand-filter-chip').forEach(button => {
        button.classList.remove('is-active');
      });
      target.classList.add('is-active');
      applyFilter(target.dataset.letter || 'ALL');
    });

    applyFilter('ALL');
  })();
</script>

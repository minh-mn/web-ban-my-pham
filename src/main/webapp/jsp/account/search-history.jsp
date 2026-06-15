<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="histories" value="${searchHistories}" />
<c:set var="historyCount" value="${empty searchHistoryCount ? 0 : searchHistoryCount}" />
<c:set var="searchTotal" value="${empty totalSearchCount ? 0 : totalSearchCount}" />
<c:set var="resultTotal" value="${empty totalResultCount ? 0 : totalResultCount}" />

<section class="sh-page" id="searchHistoryPage">
  <div class="sh-container">

    <section class="sh-header-compact">
      <div class="sh-header-compact__left">
        <span class="sh-section-tag">TÀI KHOẢN CỦA TÔI</span>
        <h1>Lịch sử tìm kiếm</h1>
        <p>
          Xem lại các từ khóa đã tìm, mở nhanh kết quả cũ hoặc xóa bớt những từ khóa không còn cần dùng.
        </p>
      </div>

      <div class="sh-header-compact__right">
        <c:if test="${not empty latestSearchHistory}">
          <c:url var="latestSearchUrl" value="/search">
            <c:param name="q" value="${latestSearchHistory.keyword}" />
          </c:url>

          <c:set var="latestItemUrl" value="${latestSearchUrl}" />
          <c:if test="${not empty latestSearchHistory.searchUrl && fn:startsWith(latestSearchHistory.searchUrl, '/')}">
            <c:set var="latestItemUrl" value="${ctx}${latestSearchHistory.searchUrl}" />
          </c:if>

          <div class="sh-last-search">
            <span class="sh-last-search__label">Tìm gần nhất</span>
            <strong><c:out value="${latestSearchHistory.keyword}" /></strong>
            <small>
              <c:out value="${latestSearchHistory.resultCount}" /> kết quả ·
              <c:out value="${latestSearchHistory.searchCount}" /> lượt tìm
            </small>
            <a href="${latestItemUrl}" class="sh-last-search__link">Tìm lại</a>
          </div>
        </c:if>
      </div>
    </section>

    <section class="sh-stats" aria-label="Thống kê lịch sử tìm kiếm">
      <article class="sh-stat-card">
        <span class="sh-stat-card__icon">⌕</span>
        <div>
          <strong><c:out value="${historyCount}" /></strong>
          <span>Từ khóa đã lưu</span>
        </div>
      </article>

      <article class="sh-stat-card">
        <span class="sh-stat-card__icon">↻</span>
        <div>
          <strong><c:out value="${searchTotal}" /></strong>
          <span>Tổng lượt tìm</span>
        </div>
      </article>

      <article class="sh-stat-card">
        <span class="sh-stat-card__icon">✦</span>
        <div>
          <strong><c:out value="${resultTotal}" /></strong>
          <span>Kết quả đã ghi nhận</span>
        </div>
      </article>
    </section>

    <c:if test="${param.deleteSuccess == '1'}">
      <div class="sh-alert sh-alert--success">Đã xóa một lịch sử tìm kiếm.</div>
    </c:if>
    <c:if test="${param.clearSuccess == '1'}">
      <div class="sh-alert sh-alert--success">Đã xóa toàn bộ lịch sử tìm kiếm.</div>
    </c:if>
    <c:if test="${param.clearEmpty == '1'}">
      <div class="sh-alert sh-alert--warning">Bạn chưa có lịch sử tìm kiếm để xóa.</div>
    </c:if>
    <c:if test="${param.deleteFailed == '1' || param.deleteInvalid == '1'}">
      <div class="sh-alert sh-alert--error">Không thể xóa lịch sử tìm kiếm này.</div>
    </c:if>
    <c:if test="${searchHistoryLoadError}">
      <div class="sh-alert sh-alert--error">
        Không tải được lịch sử tìm kiếm. Vui lòng kiểm tra bảng <strong>user_search_history</strong> trong database.
      </div>
    </c:if>

    <section class="sh-panel" id="shHistoryList">
      <div class="sh-panel__head">
        <div>
          <span class="sh-section-tag">DANH SÁCH</span>
          <h2>Toàn bộ lịch sử tìm kiếm</h2>
          <p>Danh sách được sắp xếp theo lần tìm gần nhất. Bạn có thể lọc nhanh bằng ô tìm kiếm bên dưới.</p>
        </div>

        <span class="sh-total-badge">
                    <c:out value="${historyCount}" /> mục
                </span>
      </div>

      <c:choose>
        <c:when test="${not empty histories}">
          <div class="sh-toolbar">
            <label class="sh-filter" for="historyFilterInput">
              <span class="sh-filter__icon">🔎</span>
              <input id="historyFilterInput"
                     type="search"
                     autocomplete="off"
                     placeholder="Tìm trong lịch sử...">
            </label>

            <div class="sh-sort-box">
              <span class="sh-sort-box__icon">↕</span>
              <label for="historySortSelect" class="sh-sort-box__label">Sắp xếp</label>
              <select id="historySortSelect">
                <option value="newest">Mới nhất</option>
                <option value="most-search">Tìm nhiều nhất</option>
                <option value="most-result">Nhiều kết quả nhất</option>
                <option value="az">Từ khóa A → Z</option>
              </select>
            </div>

            <div class="sh-toolbar__actions">
              <a class="sh-btn sh-btn--outline" href="${ctx}/products">Tiếp tục mua sắm</a>

              <form method="post"
                    action="${ctx}/search-history/clear"
                    onsubmit="return confirm('Bạn có chắc muốn xóa toàn bộ lịch sử tìm kiếm không?');">
                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                <button type="submit" class="sh-btn sh-btn--danger">Xóa tất cả</button>
              </form>
            </div>
          </div>

          <div class="sh-list" id="historyList">
            <c:forEach var="history" items="${histories}" varStatus="loop">
              <c:url var="searchAgainUrl" value="/search">
                <c:param name="q" value="${history.keyword}" />
              </c:url>

              <c:set var="itemUrl" value="${searchAgainUrl}" />
              <c:if test="${not empty history.searchUrl && fn:startsWith(history.searchUrl, '/')}">
                <c:set var="itemUrl" value="${ctx}${history.searchUrl}" />
              </c:if>

              <article class="sh-history-card"
                       data-history-item="true"
                       data-keyword="${fn:escapeXml(history.keyword)}"
                       data-newest-index="${loop.index}"
                       data-search-count="${history.searchCount}"
                       data-result-count="${history.resultCount}">

                <a class="sh-history-card__main" href="${itemUrl}">
                  <span class="sh-history-card__icon">↺</span>

                  <span class="sh-history-card__content">
                                        <strong><c:out value="${history.keyword}" /></strong>
                                        <small>Lần cuối: <c:out value="${history.displayLastSearchedAt}" /></small>
                                    </span>
                </a>

                <div class="sh-history-card__meta">
                  <span><c:out value="${history.resultCount}" /> kết quả</span>
                  <span><c:out value="${history.searchCount}" /> lượt tìm</span>
                </div>

                <div class="sh-history-card__actions">
                  <a class="sh-mini-btn sh-mini-btn--search" href="${itemUrl}">Tìm lại</a>

                  <form method="post"
                        action="${ctx}/search-history/delete"
                        onsubmit="return confirm('Xóa từ khóa này khỏi lịch sử tìm kiếm?');">
                    <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                    <input type="hidden" name="id" value="${history.id}">
                    <button type="submit" class="sh-mini-btn sh-mini-btn--delete">Xóa</button>
                  </form>
                </div>
              </article>
            </c:forEach>
          </div>

          <div class="sh-no-result" id="historyNoResult" hidden>
            <div class="sh-no-result__icon">🔎</div>
            <h3>Không tìm thấy từ khóa phù hợp</h3>
            <p>Hãy thử nhập từ khóa khác hoặc đổi cách sắp xếp.</p>
          </div>
        </c:when>

        <c:otherwise>
          <div class="sh-empty">
            <div class="sh-empty__icon">🔎</div>
            <h3>Chưa có lịch sử tìm kiếm</h3>
            <p>
              Khi bạn tìm sản phẩm, các từ khóa sẽ được lưu tại đây để bạn xem lại và mở nhanh ở những lần sau.
            </p>
            <a class="sh-btn sh-btn--primary" href="${ctx}/products">Khám phá sản phẩm</a>
          </div>
        </c:otherwise>
      </c:choose>
    </section>
  </div>
</section>

<script>
  (function () {
    const list = document.getElementById('historyList');
    const filterInput = document.getElementById('historyFilterInput');
    const sortSelect = document.getElementById('historySortSelect');
    const noResult = document.getElementById('historyNoResult');

    if (!list) return;

    const normalizeText = function (value) {
      return (value || '')
              .toString()
              .toLowerCase()
              .normalize('NFD')
              .replace(/[\u0300-\u036f]/g, '')
              .trim();
    };

    const getCards = function () {
      return Array.prototype.slice.call(list.querySelectorAll('[data-history-item="true"]'));
    };

    const applyFilter = function () {
      const keyword = normalizeText(filterInput ? filterInput.value : '');
      let visibleCount = 0;

      getCards().forEach(function (card) {
        const cardKeyword = normalizeText(card.getAttribute('data-keyword'));
        const matched = !keyword || cardKeyword.indexOf(keyword) !== -1;

        card.hidden = !matched;
        if (matched) visibleCount++;
      });

      if (noResult) {
        noResult.hidden = visibleCount !== 0;
      }
    };

    const applySort = function () {
      const mode = sortSelect ? sortSelect.value : 'newest';
      const cards = getCards();

      cards.sort(function (a, b) {
        if (mode === 'most-search') {
          return Number(b.dataset.searchCount || 0) - Number(a.dataset.searchCount || 0);
        }

        if (mode === 'most-result') {
          return Number(b.dataset.resultCount || 0) - Number(a.dataset.resultCount || 0);
        }

        if (mode === 'az') {
          return normalizeText(a.dataset.keyword).localeCompare(normalizeText(b.dataset.keyword));
        }

        return Number(a.dataset.newestIndex || 0) - Number(b.dataset.newestIndex || 0);
      });

      cards.forEach(function (card) {
        list.appendChild(card);
      });

      applyFilter();
    };

    if (filterInput) {
      filterInput.addEventListener('input', applyFilter);
    }

    if (sortSelect) {
      sortSelect.addEventListener('change', applySort);
    }
  })();
</script>

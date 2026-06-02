<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isLipPage" value="${lipCollectionPage == true}" />

<link rel="stylesheet" href="${ctx}/assets/css/base.css">
<link rel="stylesheet" href="${ctx}/assets/css/product-list.css">

<style>
  html.is-restoring-product-scroll {
    scroll-behavior: auto !important;
  }

  .mc-category-hero-wrap {
    padding: 0;
    background: #fff;
    border-bottom: 1px solid #f2eef0;
  }

  .mc-category-hero {
    max-width: 1480px;
    margin: 0 auto;
    padding: 26px 18px 18px;
    text-align: center;
  }

  .mc-category-hero__label {
    display: none;
  }

  .mc-category-hero__title {
    margin: 0;
    color: #d92c74;
    font-size: clamp(32px, 4vw, 54px);
    line-height: 1.08;
    font-weight: 950;
    letter-spacing: -0.04em;
  }

  .mc-category-hero__desc {
    max-width: 640px;
    margin: 12px auto 0;
    color: #59606d;
    font-size: 15px;
    line-height: 1.65;
  }

  .collection-body-section {
    padding: 0 0 70px;
    background: #fff;
  }

  .collection-container {
    max-width: 1480px;
  }

  .collection-layout {
    display: grid;
    grid-template-columns: 300px minmax(0, 1fr);
    gap: 42px;
    align-items: start;
  }

  .collection-filter-sidebar {
    position: sticky;
    top: 96px;
    max-height: calc(100vh - 112px);
    overflow-y: auto;
    padding: 28px 26px 34px 0;
    background: #fff;
    border-right: 7px solid #d78ca0;
    scrollbar-width: thin;
    scrollbar-color: #d78ca0 transparent;
  }

  .collection-filter-sidebar::-webkit-scrollbar {
    width: 6px;
  }

  .collection-filter-sidebar::-webkit-scrollbar-thumb {
    background: #d78ca0;
    border-radius: 999px;
  }

  .mc-filter-form {
    display: block;
  }

  .mc-filter-block {
    padding: 0 0 24px;
    margin-bottom: 24px;
    border-bottom: 1px solid #eeeeee;
  }

  .mc-filter-block:last-of-type {
    border-bottom: 0;
    margin-bottom: 0;
  }

  .mc-filter-title {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 0;
    border: 0;
    background: transparent;
    color: #2d2d2d;
    font-size: 18px;
    font-weight: 900;
    letter-spacing: .035em;
    text-transform: uppercase;
    cursor: pointer;
  }

  .mc-filter-title::after {
    content: "⌃";
    color: #222;
    font-size: 24px;
    line-height: 1;
    transform: translateY(4px);
  }

  .mc-filter-list {
    display: grid;
    gap: 15px;
    margin-top: 20px;
  }

  .mc-filter-list.is-scroll {
    max-height: 420px;
    overflow-y: auto;
    padding-right: 14px;
    scrollbar-width: thin;
    scrollbar-color: #444 transparent;
  }

  .mc-filter-list.is-scroll::-webkit-scrollbar {
    width: 4px;
  }

  .mc-filter-list.is-scroll::-webkit-scrollbar-thumb {
    background: #444;
    border-radius: 999px;
  }

  .mc-filter-option {
    display: flex;
    align-items: center;
    gap: 14px;
    min-height: 26px;
    color: #333;
    font-size: 18px;
    line-height: 1.35;
    cursor: pointer;
    user-select: none;
  }

  .mc-filter-option input {
    position: absolute;
    opacity: 0;
    pointer-events: none;
  }

  .mc-filter-box {
    flex: 0 0 auto;
    width: 23px;
    height: 23px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border: 1.5px solid #bdbdbd;
    background: #fff;
  }

  .mc-filter-option input:checked + .mc-filter-box {
    border-color: #bd0031;
    box-shadow: inset 0 0 0 4px #fff;
    background: #bd0031;
  }

  .mc-filter-option input:checked ~ .mc-filter-name {
    color: #bd0031;
    font-weight: 850;
  }

  .mc-filter-name {
    min-width: 0;
  }

  .mc-price-range-row {
    display: grid;
    grid-template-columns: 1fr 24px 1fr;
    gap: 14px;
    align-items: center;
    margin-top: 20px;
  }

  .mc-price-input {
    width: 100%;
    height: 62px;
    border: 1px solid #e5e5e5;
    background: #fff;
    color: #333;
    font-size: 19px;
    text-align: center;
    outline: none;
  }

  .mc-price-separator {
    color: #555;
    font-size: 25px;
    text-align: center;
  }

  .mc-filter-apply {
    width: 100%;
    min-height: 68px;
    margin-top: 18px;
    border: 0;
    background: linear-gradient(135deg, #ff4f97 0%, #d9154f 48%, #a4041b 100%);
    color: #fff;
    font-size: 19px;
    font-weight: 900;
    cursor: pointer;
    box-shadow: 0 14px 26px rgba(176, 18, 57, .18);
  }

  .mc-filter-reset {
    width: 100%;
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
    color: #9d001f;
    font-size: 17px;
    font-weight: 850;
    text-decoration: none;
  }

  .collection-main {
    min-width: 0;
    padding-top: 28px;
  }

  .collection-toolbar {
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto;
    gap: 22px;
    align-items: end;
    padding-bottom: 14px;
    margin-bottom: 18px;
    border-bottom: 1px solid #f0d2dc;
  }

  .collection-toolbar__sorts {
    display: grid;
    grid-template-columns: repeat(5, minmax(110px, 1fr));
    gap: 0;
  }

  .collection-sort-tab {
    min-height: 46px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #111;
    font-size: 17px;
    font-weight: 700;
    text-decoration: none;
    border-bottom: 2px solid transparent;
  }

  .collection-sort-tab.active,
  .collection-sort-tab:hover {
    color: #9f001f;
    border-bottom-color: #9f001f;
  }

  .collection-toolbar__count {
    color: #111;
    font-size: 14px;
    font-weight: 850;
    white-space: nowrap;
  }

  .collection-filter-tags {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
    min-height: 48px;
    padding: 10px 14px;
    margin-bottom: 18px;
    border: 1px solid #f0d2dc;
    background: #fff8fb;
  }

  .product-filter-tags__label {
    color: #333;
    font-size: 13px;
    font-weight: 900;
  }

  .product-filter-tag {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    min-height: 28px;
    padding: 6px 12px;
    border-radius: 999px;
    background: #ffe8f0;
    color: #b01239;
    font-size: 12px;
    font-weight: 850;
  }

  .product-filter-tag__remove {
    color: #b01239;
    font-size: 18px;
    line-height: 1;
    text-decoration: none;
  }

  .product-filter-clear {
    color: #b01239;
    font-size: 13px;
    font-weight: 850;
    text-decoration: none;
  }

  .collection-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 30px 28px;
    align-items: start;
  }

  .collection-card {
    position: relative;
    min-width: 0;
    background: #fff;
    border: 0;
    box-shadow: none;
    overflow: visible;
  }

  .collection-card__image-link {
    display: block;
    text-decoration: none;
  }

  .collection-card__image-box {
    position: relative;
    width: 100%;
    aspect-ratio: 1 / 1;
    overflow: hidden;
    background: #f5f8fb;
  }

  .collection-card__image-box img {
    width: 100%;
    height: 100%;
    display: block;
    object-fit: cover;
  }

  .collection-card__image-box.is-missing,
  .collection-card__image-placeholder {
    width: 100%;
    height: 100%;
    min-height: 220px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #b01239;
    font-weight: 950;
    background: linear-gradient(135deg, #fff1f6 0%, #f3f8ff 100%);
  }

  .collection-card__discount {
    position: absolute;
    right: 14px;
    top: 46%;
    z-index: 3;
    width: 48px;
    height: 48px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 999px;
    background: #a8d83f !important;
    color: #fff !important;
    font-size: 13px;
    font-weight: 950;
    box-shadow: none;
  }

  .collection-card__body {
    padding: 13px 0 0;
  }

  .collection-card__brand {
    margin-bottom: 8px;
    color: #111;
    font-size: 13px;
    font-weight: 950;
    letter-spacing: .06em;
    text-transform: uppercase;
  }

  .collection-card__title {
    min-height: 48px;
    margin: 0;
    display: -webkit-box;
    overflow: hidden;
    color: #161616;
    font-size: 18px;
    line-height: 1.35;
    font-weight: 500;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .product-title-link {
    color: inherit;
    text-decoration: none;
  }

  .collection-card__price-line {
    display: flex;
    flex-wrap: wrap;
    align-items: baseline;
    gap: 9px;
    margin-top: 12px;
  }

  .collection-card__sale-price {
    color: #a30624;
    font-size: 20px;
    font-weight: 950;
  }

  .collection-card__old-price {
    color: #8d8d8d;
    font-size: 15px;
    text-decoration: line-through;
  }

  .collection-card__actions {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
    margin-top: 14px;
  }

  .collection-card__view-btn,
  .collection-card__cart-btn {
    min-height: 42px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 10px 12px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 900;
    text-decoration: none;
    cursor: pointer;
  }

  .collection-card__view-btn {
    border: 1px solid #f0c4cf;
    color: #bd0031;
    background: #fff;
  }

  .collection-card__cart-btn {
    width: 100%;
    border: 0;
    color: #fff;
    background: linear-gradient(135deg, #ff4f97 0%, #d9154f 48%, #a4041b 100%);
    box-shadow: 0 10px 20px rgba(176, 18, 57, .16);
  }

  .collection-card__cart-btn:disabled {
    opacity: .5;
    cursor: not-allowed;
  }

  .collection-card__bottom {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-top: 12px;
  }

  .collection-card__meta {
    color: #222;
    font-size: 15px;
    font-weight: 700;
  }

  .collection-card__heart {
    color: #333;
    font-size: 28px;
    line-height: 1;
    text-decoration: none;
  }

  .pagination-wrap {
    display: flex;
    justify-content: center;
    margin-top: 40px;
  }

  @media (max-width: 1200px) {
    .collection-layout {
      grid-template-columns: 260px minmax(0, 1fr);
      gap: 26px;
    }

    .collection-grid {
      grid-template-columns: repeat(3, minmax(0, 1fr));
    }
  }

  @media (max-width: 900px) {
    .collection-layout {
      display: block;
    }

    .collection-filter-sidebar {
      position: relative;
      top: auto;
      max-height: none;
      border-right: 0;
      border-bottom: 4px solid #d78ca0;
      padding-right: 0;
      margin-bottom: 24px;
    }

    .collection-toolbar {
      grid-template-columns: 1fr;
    }

    .collection-toolbar__sorts {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .collection-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 560px) {
    .collection-grid {
      grid-template-columns: 1fr;
    }

    .collection-card__actions {
      grid-template-columns: 1fr;
    }
  }
</style>

<script>
  (function () {
    try {
      window.history.scrollRestoration = "manual";
      if (sessionStorage.getItem("mycosmetic_product_list_scroll_y")) {
        document.documentElement.classList.add("is-restoring-product-scroll");
      }
    } catch (e) {
      // ignore
    }
  })();
</script>

<c:url var="sortDefaultUrl" value="/products">
  <c:if test="${not empty param.q}">
    <c:param name="q" value="${param.q}" />
  </c:if>
  <c:if test="${not empty selectedCategoryList}">
    <c:forEach var="cid" items="${selectedCategoryList}">
      <c:param name="category" value="${cid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty selectedBrandList}">
    <c:forEach var="bid" items="${selectedBrandList}">
      <c:param name="brand" value="${bid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty priceRangeList}">
    <c:forEach var="pr" items="${priceRangeList}">
      <c:param name="priceRange" value="${pr}" />
    </c:forEach>
  </c:if>
  <c:if test="${minRating != null}">
    <c:param name="rating" value="${minRating}" />
  </c:if>
</c:url>

<c:url var="sortNewestUrl" value="/products">
  <c:if test="${not empty param.q}">
    <c:param name="q" value="${param.q}" />
  </c:if>
  <c:if test="${not empty selectedCategoryList}">
    <c:forEach var="cid" items="${selectedCategoryList}">
      <c:param name="category" value="${cid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty selectedBrandList}">
    <c:forEach var="bid" items="${selectedBrandList}">
      <c:param name="brand" value="${bid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty priceRangeList}">
    <c:forEach var="pr" items="${priceRangeList}">
      <c:param name="priceRange" value="${pr}" />
    </c:forEach>
  </c:if>
  <c:if test="${minRating != null}">
    <c:param name="rating" value="${minRating}" />
  </c:if>
  <c:param name="sort" value="created_desc" />
</c:url>

<c:url var="sortBestSellingUrl" value="/products">
  <c:if test="${not empty param.q}">
    <c:param name="q" value="${param.q}" />
  </c:if>
  <c:if test="${not empty selectedCategoryList}">
    <c:forEach var="cid" items="${selectedCategoryList}">
      <c:param name="category" value="${cid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty selectedBrandList}">
    <c:forEach var="bid" items="${selectedBrandList}">
      <c:param name="brand" value="${bid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty priceRangeList}">
    <c:forEach var="pr" items="${priceRangeList}">
      <c:param name="priceRange" value="${pr}" />
    </c:forEach>
  </c:if>
  <c:if test="${minRating != null}">
    <c:param name="rating" value="${minRating}" />
  </c:if>
  <c:param name="sort" value="best_selling" />
</c:url>

<c:url var="sortPriceAscUrl" value="/products">
  <c:if test="${not empty param.q}">
    <c:param name="q" value="${param.q}" />
  </c:if>
  <c:if test="${not empty selectedCategoryList}">
    <c:forEach var="cid" items="${selectedCategoryList}">
      <c:param name="category" value="${cid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty selectedBrandList}">
    <c:forEach var="bid" items="${selectedBrandList}">
      <c:param name="brand" value="${bid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty priceRangeList}">
    <c:forEach var="pr" items="${priceRangeList}">
      <c:param name="priceRange" value="${pr}" />
    </c:forEach>
  </c:if>
  <c:if test="${minRating != null}">
    <c:param name="rating" value="${minRating}" />
  </c:if>
  <c:param name="sort" value="price_asc" />
</c:url>

<c:url var="sortPriceDescUrl" value="/products">
  <c:if test="${not empty param.q}">
    <c:param name="q" value="${param.q}" />
  </c:if>
  <c:if test="${not empty selectedCategoryList}">
    <c:forEach var="cid" items="${selectedCategoryList}">
      <c:param name="category" value="${cid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty selectedBrandList}">
    <c:forEach var="bid" items="${selectedBrandList}">
      <c:param name="brand" value="${bid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty priceRangeList}">
    <c:forEach var="pr" items="${priceRangeList}">
      <c:param name="priceRange" value="${pr}" />
    </c:forEach>
  </c:if>
  <c:if test="${minRating != null}">
    <c:param name="rating" value="${minRating}" />
  </c:if>
  <c:param name="sort" value="price_desc" />
</c:url>

<c:choose>
  <c:when test="${not empty primaryCategory}">
    <c:url var="resetFilterUrl" value="/products">
      <c:param name="category" value="${primaryCategory.id}" />
    </c:url>
  </c:when>
  <c:otherwise>
    <c:url var="resetFilterUrl" value="/products" />
  </c:otherwise>
</c:choose>

<section class="mc-category-hero-wrap ${isLipPage ? 'is-lip-collection' : ''}">
  <div class="mc-category-hero">
    <div class="mc-category-hero__label">Danh mục</div>
    <h1 class="mc-category-hero__title">
      <c:out value="${empty collectionTitle ? 'Sản phẩm theo danh mục' : collectionTitle}" />
    </h1>
    <p class="mc-category-hero__desc">
      <c:out value="${empty collectionDesc
                ? 'Đang hiển thị sản phẩm thuộc danh mục đã chọn. Bạn có thể lọc thêm theo thương hiệu, mức giá và đánh giá.'
                : collectionDesc}" />
    </p>
  </div>
</section>

<section class="section collection-body-section">
  <div class="container collection-container">
    <div class="product-page collection-layout">

      <aside class="filter-sidebar collection-filter-sidebar">
        <form id="filterForm" class="mc-filter-form" method="get" action="${ctx}/products#collectionResults">
          <c:if test="${not empty param.q}">
            <input type="hidden" name="q" value="${fn:escapeXml(param.q)}">
          </c:if>
          <c:if test="${not empty param.sort}">
            <input type="hidden" name="sort" value="${fn:escapeXml(param.sort)}">
          </c:if>
          <c:if test="${not empty primaryCategory}">
            <input type="hidden" name="category" value="${primaryCategory.id}">
          </c:if>

          <c:if test="${not empty sidebarMainCategoryItems}">
            <div class="mc-filter-block">
              <button type="button" class="mc-filter-title">
                <span>Danh mục sản phẩm</span>
              </button>
              <div class="mc-filter-list">
                <c:forEach var="item" items="${sidebarMainCategoryItems}">
                  <label class="mc-filter-option">
                    <input type="checkbox" name="category" value="${item.id}" ${item.selected ? 'checked' : ''}>
                    <span class="mc-filter-box"></span>
                    <span class="mc-filter-name"><c:out value="${item.name}" /></span>
                  </label>
                </c:forEach>
              </div>
            </div>
          </c:if>

          <c:if test="${not empty sidebarTypeCategoryItems}">
            <div class="mc-filter-block">
              <button type="button" class="mc-filter-title">
                <span>Loại sản phẩm</span>
              </button>
              <div class="mc-filter-list">
                <c:forEach var="item" items="${sidebarTypeCategoryItems}">
                  <label class="mc-filter-option">
                    <input type="checkbox" name="category" value="${item.id}" ${item.selected ? 'checked' : ''}>
                    <span class="mc-filter-box"></span>
                    <span class="mc-filter-name"><c:out value="${item.name}" /></span>
                  </label>
                </c:forEach>
              </div>
            </div>
          </c:if>

          <div class="mc-filter-block">
            <button type="button" class="mc-filter-title">
              <span>Giá</span>
            </button>
            <div class="mc-price-range-row">
              <input class="mc-price-input" type="text" value="0" aria-label="Giá thấp nhất" readonly>
              <span class="mc-price-separator">–</span>
              <input class="mc-price-input" type="text" value="100,000,000" aria-label="Giá cao nhất" readonly>
            </div>
            <c:set var="priceLt500Selected" value="${false}" />
            <c:set var="price5001000Selected" value="${false}" />
            <c:set var="priceGt1000Selected" value="${false}" />
            <c:forEach var="selectedPrice" items="${priceRangeList}">
              <c:if test="${selectedPrice == 'lt500' || selectedPrice == '0_500' || selectedPrice == 'under_500'}">
                <c:set var="priceLt500Selected" value="${true}" />
              </c:if>
              <c:if test="${selectedPrice == '500_1000'}">
                <c:set var="price5001000Selected" value="${true}" />
              </c:if>
              <c:if test="${selectedPrice == 'gt1000' || selectedPrice == 'over_1000'}">
                <c:set var="priceGt1000Selected" value="${true}" />
              </c:if>
            </c:forEach>

            <div class="mc-filter-list">
              <label class="mc-filter-option">
                <input type="checkbox" name="priceRange" value="lt500" ${priceLt500Selected ? 'checked' : ''}>
                <span class="mc-filter-box"></span>
                <span class="mc-filter-name">Dưới 500.000đ</span>
              </label>
              <label class="mc-filter-option">
                <input type="checkbox" name="priceRange" value="500_1000" ${price5001000Selected ? 'checked' : ''}>
                <span class="mc-filter-box"></span>
                <span class="mc-filter-name">500.000đ - 1.000.000đ</span>
              </label>
              <label class="mc-filter-option">
                <input type="checkbox" name="priceRange" value="gt1000" ${priceGt1000Selected ? 'checked' : ''}>
                <span class="mc-filter-box"></span>
                <span class="mc-filter-name">Trên 1.000.000đ</span>
              </label>
            </div>
            <button type="submit" class="mc-filter-apply">Áp dụng</button>
          </div>

          <c:if test="${not empty brands}">
            <div class="mc-filter-block">
              <button type="button" class="mc-filter-title">
                <span>Thương hiệu</span>
              </button>
              <div class="mc-filter-list is-scroll">
                <c:forEach var="brand" items="${brands}">
                  <c:set var="brandChecked" value="${false}" />
                  <c:forEach var="selectedBrandId" items="${selectedBrandList}">
                    <c:if test="${selectedBrandId == brand.id}">
                      <c:set var="brandChecked" value="${true}" />
                    </c:if>
                  </c:forEach>

                  <label class="mc-filter-option">
                    <input type="checkbox" name="brand" value="${brand.id}" ${brandChecked ? 'checked' : ''}>
                    <span class="mc-filter-box"></span>
                    <span class="mc-filter-name"><c:out value="${brand.name}" /></span>
                  </label>
                </c:forEach>
              </div>
            </div>
          </c:if>

          <a class="mc-filter-reset" href="${resetFilterUrl}#collectionResults">Đặt lại</a>
        </form>
      </aside>

      <main class="product-main collection-main" id="collectionResults">

        <div class="collection-toolbar" id="collectionToolbar">
          <nav class="collection-toolbar__sorts" aria-label="Sắp xếp sản phẩm">
            <a href="${sortDefaultUrl}#collectionResults" class="collection-sort-tab ${empty param.sort ? 'active' : ''}">Phổ biến</a>
            <a href="${sortNewestUrl}#collectionResults" class="collection-sort-tab ${param.sort == 'created_desc' ? 'active' : ''}">Mới nhất</a>
            <a href="${sortBestSellingUrl}#collectionResults" class="collection-sort-tab ${param.sort == 'best_selling' ? 'active' : ''}">Bán chạy</a>
            <a href="${sortPriceAscUrl}#collectionResults" class="collection-sort-tab ${param.sort == 'price_asc' ? 'active' : ''}">Giá thấp</a>
            <a href="${sortPriceDescUrl}#collectionResults" class="collection-sort-tab ${param.sort == 'price_desc' ? 'active' : ''}">Giá cao</a>
          </nav>

          <div class="collection-toolbar__count">
            <c:choose>
              <c:when test="${total != null}">${total} sản phẩm</c:when>
              <c:otherwise>0 sản phẩm</c:otherwise>
            </c:choose>
          </div>
        </div>

        <div class="product-filter-tags collection-filter-tags">
          <span class="product-filter-tags__label">Đang lọc:</span>
          <c:choose>
            <c:when test="${not empty activeFilterTags}">
              <c:forEach var="filterTag" items="${activeFilterTags}">
                <span class="product-filter-tag product-filter-tag--removable">
                  <strong><c:out value="${filterTag.label}" /></strong>
                  <a class="product-filter-tag__remove"
                     href="${filterTag.removeUrl}"
                     title="Xóa bộ lọc này"
                     aria-label="Xóa bộ lọc này">×</a>
                </span>
              </c:forEach>
              <a class="product-filter-clear" href="${resetFilterUrl}#collectionResults">Xóa bộ lọc</a>
            </c:when>
            <c:otherwise>
              <span class="product-filter-tag product-filter-tag--muted"><strong>Tất cả sản phẩm</strong></span>
            </c:otherwise>
          </c:choose>
        </div>

        <div class="product-grid collection-grid">
          <c:choose>
            <c:when test="${not empty products}">
              <c:forEach var="product" items="${products}">
                <c:set var="rawImage" value="${product.imageUrl}" />

                <c:choose>
                  <c:when test="${not empty product.slug}">
                    <c:set var="productUrl" value="${ctx}/product/${product.slug}?id=${product.id}" />
                  </c:when>
                  <c:otherwise>
                    <c:set var="productUrl" value="${ctx}/product?id=${product.id}" />
                  </c:otherwise>
                </c:choose>

                <c:choose>
                  <c:when test="${not empty rawImage and fn:startsWith(rawImage, 'http')}">
                    <c:set var="productImageSrc" value="${rawImage}" />
                    <c:set var="productImageAlt" value="" />
                  </c:when>
                  <c:when test="${not empty rawImage and fn:startsWith(rawImage, '/')}">
                    <c:set var="productImageSrc" value="${ctx}${rawImage}" />
                    <c:set var="productImageAlt" value="" />
                  </c:when>
                  <c:when test="${not empty rawImage and fn:startsWith(rawImage, 'uploads/')}">
                    <c:set var="productImageSrc" value="${ctx}/${rawImage}" />
                    <c:set var="productImageAlt" value="" />
                  </c:when>
                  <c:when test="${not empty rawImage and fn:startsWith(rawImage, 'assets/')}">
                    <c:set var="productImageSrc" value="${ctx}/${rawImage}" />
                    <c:set var="productImageAlt" value="" />
                  </c:when>
                  <c:when test="${not empty rawImage and fn:startsWith(rawImage, 'products/')}">
                    <c:set var="imageFileName" value="${fn:substringAfter(rawImage, 'products/')}" />
                    <c:set var="productImageSrc" value="${ctx}/uploads/product/${imageFileName}" />
                    <c:set var="productImageAlt" value="${ctx}/${rawImage}" />
                  </c:when>
                  <c:when test="${not empty rawImage}">
                    <c:set var="productImageSrc" value="${ctx}/uploads/product/${rawImage}" />
                    <c:set var="productImageAlt" value="${ctx}/${rawImage}" />
                  </c:when>
                  <c:otherwise>
                    <c:set var="productImageSrc" value="" />
                    <c:set var="productImageAlt" value="" />
                  </c:otherwise>
                </c:choose>

                <article class="product-card collection-card">
                  <c:if test="${product.finalPrice lt product.price}">
                    <div class="badge-sale collection-card__discount">
                      <c:choose>
                        <c:when test="${product.discountPercent > 0}">-${product.discountPercent}%</c:when>
                        <c:otherwise>SALE</c:otherwise>
                      </c:choose>
                    </div>
                  </c:if>

                  <a class="product-img-link collection-card__image-link"
                     href="${productUrl}"
                     aria-label="Xem chi tiết ${fn:escapeXml(product.title)}">
                    <div class="product-img-box collection-card__image-box">
                      <c:choose>
                        <c:when test="${not empty productImageSrc}">
                          <img src="${productImageSrc}"
                               data-alt-src="${productImageAlt}"
                               alt="${fn:escapeXml(product.title)}"
                               onerror="if(this.dataset.altSrc){this.src=this.dataset.altSrc;this.dataset.altSrc='';}else{this.style.display='none';this.closest('.collection-card__image-box').classList.add('is-missing');}">
                        </c:when>
                        <c:otherwise>
                          <div class="collection-card__image-placeholder">MyCosmetic</div>
                        </c:otherwise>
                      </c:choose>
                    </div>
                  </a>

                  <div class="collection-card__body">
                    <div class="collection-card__brand">
                      <c:choose>
                        <c:when test="${not empty product.brandName}"><c:out value="${product.brandName}" /></c:when>
                        <c:otherwise>MYCOSMETIC</c:otherwise>
                      </c:choose>
                    </div>

                    <h3 class="collection-card__title">
                      <a class="product-title-link" href="${productUrl}"><c:out value="${product.title}" /></a>
                    </h3>

                    <div class="collection-card__price-wrap">
                      <c:choose>
                        <c:when test="${product.finalPrice lt product.price}">
                          <div class="collection-card__price-line">
                            <span class="collection-card__sale-price"><fmt:formatNumber value="${product.finalPrice}" type="number" groupingUsed="true"/>₫</span>
                            <span class="collection-card__old-price"><fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>₫</span>
                          </div>
                        </c:when>
                        <c:otherwise>
                          <div class="collection-card__price-line">
                            <span class="collection-card__sale-price"><fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>₫</span>
                          </div>
                        </c:otherwise>
                      </c:choose>
                    </div>

                    <div class="collection-card__actions">
                      <a class="collection-card__view-btn" href="${productUrl}">Xem sản phẩm</a>
                      <form method="post" action="${ctx}/cart/add" class="collection-card__cart-form">
                        <input type="hidden" name="productId" value="${product.id}">
                        <input type="hidden" name="quantity" value="1">
                        <input type="hidden" name="quickAdd" value="1">
                        <c:if test="${not empty csrfToken}">
                          <input type="hidden" name="csrfToken" value="${csrfToken}">
                        </c:if>
                        <button type="submit" class="collection-card__cart-btn" ${product.stock <= 0 ? 'disabled' : ''}>Thêm giỏ</button>
                      </form>
                    </div>

                    <div class="collection-card__bottom">
                      <div class="collection-card__meta">
                        <c:choose>
                          <c:when test="${product.soldQuantity > 0}">${product.soldQuantity} đã bán</c:when>
                          <c:otherwise>${product.reviewCount} đánh giá</c:otherwise>
                        </c:choose>
                      </div>
                      <a href="${productUrl}" class="collection-card__heart" aria-label="Xem chi tiết sản phẩm">♡</a>
                    </div>
                  </div>
                </article>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <div class="product-empty">
                <div class="product-empty__title">Không tìm thấy sản phẩm phù hợp</div>
                <div class="product-empty__text">Hãy thử chọn danh mục khác, bỏ bớt bộ lọc hoặc quay lại danh sách tất cả sản phẩm.</div>
                <a class="btn-outline" href="${ctx}/products#collectionResults">Xem tất cả sản phẩm</a>
              </div>
            </c:otherwise>
          </c:choose>
        </div>

        <c:if test="${totalPages != null && totalPages > 1}">
          <div class="pagination-wrap">
            <div class="pagination">
              <c:url var="prevPageUrl" value="/products">
                <c:param name="page" value="${page - 1}" />
                <c:if test="${not empty param.q}"><c:param name="q" value="${param.q}" /></c:if>
                <c:if test="${not empty param.sort}"><c:param name="sort" value="${param.sort}" /></c:if>
                <c:if test="${not empty priceRangeList}"><c:forEach var="pr" items="${priceRangeList}"><c:param name="priceRange" value="${pr}" /></c:forEach></c:if>
                <c:if test="${not empty selectedCategoryList}"><c:forEach var="cid" items="${selectedCategoryList}"><c:param name="category" value="${cid}" /></c:forEach></c:if>
                <c:if test="${not empty selectedBrandList}"><c:forEach var="bid" items="${selectedBrandList}"><c:param name="brand" value="${bid}" /></c:forEach></c:if>
                <c:if test="${minRating != null}"><c:param name="rating" value="${minRating}" /></c:if>
              </c:url>

              <c:url var="nextPageUrl" value="/products">
                <c:param name="page" value="${page + 1}" />
                <c:if test="${not empty param.q}"><c:param name="q" value="${param.q}" /></c:if>
                <c:if test="${not empty param.sort}"><c:param name="sort" value="${param.sort}" /></c:if>
                <c:if test="${not empty priceRangeList}"><c:forEach var="pr" items="${priceRangeList}"><c:param name="priceRange" value="${pr}" /></c:forEach></c:if>
                <c:if test="${not empty selectedCategoryList}"><c:forEach var="cid" items="${selectedCategoryList}"><c:param name="category" value="${cid}" /></c:forEach></c:if>
                <c:if test="${not empty selectedBrandList}"><c:forEach var="bid" items="${selectedBrandList}"><c:param name="brand" value="${bid}" /></c:forEach></c:if>
                <c:if test="${minRating != null}"><c:param name="rating" value="${minRating}" /></c:if>
              </c:url>

              <c:choose>
                <c:when test="${page != null && page > 1}"><a class="pg-btn" href="${prevPageUrl}#collectionResults">‹</a></c:when>
                <c:otherwise><span class="pg-btn disabled">‹</span></c:otherwise>
              </c:choose>

              <c:forEach begin="1" end="${totalPages}" var="p">
                <c:choose>
                  <c:when test="${p == page}"><span class="pg-num active">${p}</span></c:when>
                  <c:otherwise>
                    <c:url var="pageUrl" value="/products">
                      <c:param name="page" value="${p}" />
                      <c:if test="${not empty param.q}"><c:param name="q" value="${param.q}" /></c:if>
                      <c:if test="${not empty param.sort}"><c:param name="sort" value="${param.sort}" /></c:if>
                      <c:if test="${not empty priceRangeList}"><c:forEach var="pr" items="${priceRangeList}"><c:param name="priceRange" value="${pr}" /></c:forEach></c:if>
                      <c:if test="${not empty selectedCategoryList}"><c:forEach var="cid" items="${selectedCategoryList}"><c:param name="category" value="${cid}" /></c:forEach></c:if>
                      <c:if test="${not empty selectedBrandList}"><c:forEach var="bid" items="${selectedBrandList}"><c:param name="brand" value="${bid}" /></c:forEach></c:if>
                      <c:if test="${minRating != null}"><c:param name="rating" value="${minRating}" /></c:if>
                    </c:url>
                    <a class="pg-num" href="${pageUrl}#collectionResults">${p}</a>
                  </c:otherwise>
                </c:choose>
              </c:forEach>

              <c:choose>
                <c:when test="${page != null && page < totalPages}"><a class="pg-btn" href="${nextPageUrl}#collectionResults">›</a></c:when>
                <c:otherwise><span class="pg-btn disabled">›</span></c:otherwise>
              </c:choose>
            </div>
          </div>
        </c:if>
      </main>
    </div>
  </div>
</section>

<script>
  (function () {
    const SCROLL_KEY = "mycosmetic_product_list_scroll_y";

    try {
      window.history.scrollRestoration = "manual";
    } catch (e) {
      // ignore
    }

    function shouldRememberScroll(target) {
      if (!target) {
        return false;
      }

      return Boolean(
              target.closest(".collection-toolbar") ||
              target.closest(".collection-filter-sidebar") ||
              target.closest(".collection-filter-tags") ||
              target.closest(".pagination")
      );
    }

    function saveScrollPosition() {
      try {
        sessionStorage.setItem(SCROLL_KEY, String(window.scrollY || window.pageYOffset || 0));
      } catch (e) {
        // ignore
      }
    }

    document.addEventListener("click", function (event) {
      const link = event.target.closest("a");

      if (link && link.href && link.href.indexOf("/products") !== -1 && shouldRememberScroll(link)) {
        saveScrollPosition();
      }
    }, true);

    document.addEventListener("change", function (event) {
      if (event.target && event.target.closest("#filterForm")) {
        saveScrollPosition();
      }
    }, true);

    document.addEventListener("submit", function (event) {
      if (event.target && event.target.matches("#filterForm")) {
        saveScrollPosition();
      }
    }, true);

    function restoreScroll() {
      let savedY = null;

      try {
        savedY = sessionStorage.getItem(SCROLL_KEY);
        sessionStorage.removeItem(SCROLL_KEY);
      } catch (e) {
        savedY = null;
      }

      if (!savedY) {
        document.documentElement.classList.remove("is-restoring-product-scroll");
        return;
      }

      const y = parseInt(savedY, 10) || 0;

      requestAnimationFrame(function () {
        window.scrollTo(0, y);

        requestAnimationFrame(function () {
          window.scrollTo(0, y);
          document.documentElement.classList.remove("is-restoring-product-scroll");
        });
      });
    }

    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", restoreScroll);
    } else {
      restoreScroll();
    }
  })();
</script>

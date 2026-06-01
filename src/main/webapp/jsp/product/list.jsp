<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" href="${ctx}/assets/css/base.css">
<link rel="stylesheet" href="${ctx}/assets/css/product-list.css">

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

<section class="mc-category-hero-wrap">
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
        <jsp:include page="/jsp/product/category_filter.jsp" />
      </aside>

      <main class="product-main collection-main" id="collectionResults">

        <div class="collection-toolbar" id="collectionToolbar">
          <nav class="collection-toolbar__sorts" aria-label="Sắp xếp sản phẩm">
            <a href="${sortDefaultUrl}#collectionResults"
               class="collection-sort-tab ${empty param.sort ? 'active' : ''}">
              Phổ biến
            </a>

            <a href="${sortNewestUrl}#collectionResults"
               class="collection-sort-tab ${param.sort == 'created_desc' ? 'active' : ''}">
              Mới nhất
            </a>

            <a href="${sortBestSellingUrl}#collectionResults"
               class="collection-sort-tab ${param.sort == 'best_selling' ? 'active' : ''}">
              Bán chạy
            </a>

            <a href="${sortPriceAscUrl}#collectionResults"
               class="collection-sort-tab ${param.sort == 'price_asc' ? 'active' : ''}">
              Giá thấp
            </a>

            <a href="${sortPriceDescUrl}#collectionResults"
               class="collection-sort-tab ${param.sort == 'price_desc' ? 'active' : ''}">
              Giá cao
            </a>
          </nav>

          <div class="collection-toolbar__count">
            <c:choose>
              <c:when test="${total != null}">
                ${total} sản phẩm
              </c:when>
              <c:otherwise>
                0 sản phẩm
              </c:otherwise>
            </c:choose>
          </div>
        </div>

        <div class="product-filter-tags collection-filter-tags">
          <span class="product-filter-tags__label">Đang lọc:</span>

          <c:choose>
            <c:when test="${not empty activeFilterTags}">
              <c:forEach var="filterTag" items="${activeFilterTags}">
                                <span class="product-filter-tag product-filter-tag--removable">
                                    <strong>
                                        <c:out value="${filterTag.label}" />
                                    </strong>

                                    <a class="product-filter-tag__remove"
                                       href="${filterTag.removeUrl}"
                                       title="Xóa bộ lọc này"
                                       aria-label="Xóa bộ lọc này">
                                        ×
                                    </a>
                                </span>
              </c:forEach>

              <a class="product-filter-clear" href="${ctx}/products#collectionResults">
                Xóa bộ lọc
              </a>
            </c:when>

            <c:otherwise>
                            <span class="product-filter-tag product-filter-tag--muted">
                                <strong>Tất cả sản phẩm</strong>
                            </span>
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
                        <c:when test="${product.discountPercent > 0}">
                          -${product.discountPercent}%
                        </c:when>
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
                          <div class="collection-card__image-placeholder">
                            MyCosmetic
                          </div>
                        </c:otherwise>
                      </c:choose>
                    </div>
                  </a>

                  <div class="collection-card__body">
                    <div class="collection-card__brand">
                      <c:choose>
                        <c:when test="${not empty product.brandName}">
                          <c:out value="${product.brandName}" />
                        </c:when>
                        <c:otherwise>MYCOSMETIC</c:otherwise>
                      </c:choose>
                    </div>

                    <h3 class="collection-card__title">
                      <a class="product-title-link" href="${productUrl}">
                        <c:out value="${product.title}" />
                      </a>
                    </h3>

                    <div class="collection-card__price-wrap">
                      <c:choose>
                        <c:when test="${product.finalPrice lt product.price}">
                          <div class="collection-card__price-line">
                                                        <span class="collection-card__sale-price">
                                                            <fmt:formatNumber value="${product.finalPrice}" type="number" groupingUsed="true"/>₫
                                                        </span>

                            <span class="collection-card__old-price">
                                                            <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>₫
                                                        </span>
                          </div>
                        </c:when>

                        <c:otherwise>
                          <div class="collection-card__price-line">
                                                        <span class="collection-card__sale-price">
                                                            <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>₫
                                                        </span>
                          </div>
                        </c:otherwise>
                      </c:choose>
                    </div>

                    <div class="collection-card__actions">
                      <a class="collection-card__view-btn" href="${productUrl}">
                        Xem sản phẩm
                      </a>

                      <form method="post" action="${ctx}/cart/add" class="collection-card__cart-form">
                        <input type="hidden" name="productId" value="${product.id}">
                        <input type="hidden" name="quantity" value="1">
                        <input type="hidden" name="quickAdd" value="1">
                        <button type="submit" class="collection-card__cart-btn" ${product.stock <= 0 ? 'disabled' : ''}>
                          Thêm giỏ
                        </button>
                      </form>
                    </div>

                    <div class="collection-card__bottom">
                      <div class="collection-card__meta">
                        <c:choose>
                          <c:when test="${product.soldQuantity > 0}">
                            ${product.soldQuantity} đã bán
                          </c:when>
                          <c:otherwise>
                            ${product.reviewCount} đánh giá
                          </c:otherwise>
                        </c:choose>
                      </div>

                      <a href="${productUrl}"
                         class="collection-card__heart"
                         aria-label="Xem chi tiết sản phẩm">
                        ♡
                      </a>
                    </div>
                  </div>
                </article>
              </c:forEach>
            </c:when>

            <c:otherwise>
              <div class="product-empty">
                <div class="product-empty__title">Không tìm thấy sản phẩm phù hợp</div>

                <div class="product-empty__text">
                  Hãy thử chọn danh mục khác, bỏ bớt bộ lọc hoặc quay lại danh sách tất cả sản phẩm.
                </div>

                <a class="btn-outline" href="${ctx}/products#collectionResults">
                  Xem tất cả sản phẩm
                </a>
              </div>
            </c:otherwise>
          </c:choose>
        </div>

        <c:if test="${totalPages != null && totalPages > 1}">
          <div class="pagination-wrap">
            <div class="pagination">

              <c:url var="prevPageUrl" value="/products">
                <c:param name="page" value="${page - 1}" />

                <c:if test="${not empty param.q}">
                  <c:param name="q" value="${param.q}" />
                </c:if>

                <c:if test="${not empty param.sort}">
                  <c:param name="sort" value="${param.sort}" />
                </c:if>

                <c:if test="${not empty priceRangeList}">
                  <c:forEach var="pr" items="${priceRangeList}">
                    <c:param name="priceRange" value="${pr}" />
                  </c:forEach>
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

                <c:if test="${minRating != null}">
                  <c:param name="rating" value="${minRating}" />
                </c:if>
              </c:url>

              <c:url var="nextPageUrl" value="/products">
                <c:param name="page" value="${page + 1}" />

                <c:if test="${not empty param.q}">
                  <c:param name="q" value="${param.q}" />
                </c:if>

                <c:if test="${not empty param.sort}">
                  <c:param name="sort" value="${param.sort}" />
                </c:if>

                <c:if test="${not empty priceRangeList}">
                  <c:forEach var="pr" items="${priceRangeList}">
                    <c:param name="priceRange" value="${pr}" />
                  </c:forEach>
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

                <c:if test="${minRating != null}">
                  <c:param name="rating" value="${minRating}" />
                </c:if>
              </c:url>

              <c:choose>
                <c:when test="${page != null && page > 1}">
                  <a class="pg-btn" href="${prevPageUrl}#collectionResults">‹</a>
                </c:when>
                <c:otherwise>
                  <span class="pg-btn disabled">‹</span>
                </c:otherwise>
              </c:choose>

              <c:forEach begin="1" end="${totalPages}" var="p">
                <c:choose>
                  <c:when test="${p == page}">
                    <span class="pg-num active">${p}</span>
                  </c:when>

                  <c:otherwise>
                    <c:url var="pageUrl" value="/products">
                      <c:param name="page" value="${p}" />

                      <c:if test="${not empty param.q}">
                        <c:param name="q" value="${param.q}" />
                      </c:if>

                      <c:if test="${not empty param.sort}">
                        <c:param name="sort" value="${param.sort}" />
                      </c:if>

                      <c:if test="${not empty priceRangeList}">
                        <c:forEach var="pr" items="${priceRangeList}">
                          <c:param name="priceRange" value="${pr}" />
                        </c:forEach>
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

                      <c:if test="${minRating != null}">
                        <c:param name="rating" value="${minRating}" />
                      </c:if>
                    </c:url>

                    <a class="pg-num" href="${pageUrl}#collectionResults">${p}</a>
                  </c:otherwise>
                </c:choose>
              </c:forEach>

              <c:choose>
                <c:when test="${page != null && page < totalPages}">
                  <a class="pg-btn" href="${nextPageUrl}#collectionResults">›</a>
                </c:when>
                <c:otherwise>
                  <span class="pg-btn disabled">›</span>
                </c:otherwise>
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

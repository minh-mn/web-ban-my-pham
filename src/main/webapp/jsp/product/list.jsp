<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" href="${ctx}/assets/css/base.css">
<link rel="stylesheet" href="${ctx}/assets/css/product-list.css">

<!-- ================= PAGE HEADER ================= -->
<section class="section">
  <div class="container page-header" style="text-align:center; max-width:760px;">
    <h2 class="section-title">Tất cả sản phẩm</h2>

    <p style="color:#666; font-size:16px; line-height:1.7;">
      Tại đây, bạn có thể khám phá toàn bộ danh mục sản phẩm chăm sóc da chính hãng
      được hệ thống MyCosmetic tổng hợp và phân loại rõ ràng theo từng nhu cầu sử dụng.
      Các sản phẩm được lựa chọn dựa trên tiêu chí an toàn, nguồn gốc minh bạch và hiệu quả
      đã được người dùng đánh giá thực tế.
    </p>
  </div>
</section>

<!-- ================= PRODUCT LIST ================= -->
<section class="section">
  <div class="container">
    <div class="product-page">

      <!-- ================= SIDEBAR FILTER ================= -->
      <aside class="filter-sidebar">
        <jsp:include page="/jsp/product/category_filter.jsp" />
      </aside>

      <!-- ================= MAIN CONTENT ================= -->
      <div class="product-main">

        <!-- ================= FILTER SUMMARY ================= -->
        <div class="product-list-head">
          <div>
            <h3 class="product-list-title">Danh sách sản phẩm</h3>

            <p class="product-list-desc">
              Lọc sản phẩm theo danh mục, thương hiệu, mức giá và đánh giá để tìm sản phẩm phù hợp hơn.
            </p>
          </div>

          <div class="product-list-count">
            <c:choose>
              <c:when test="${not empty products}">
                ${fn:length(products)} sản phẩm
              </c:when>
              <c:otherwise>
                0 sản phẩm
              </c:otherwise>
            </c:choose>
          </div>
        </div>

        <!-- ================= ACTIVE FILTER TAGS ================= -->
        <c:if test="${not empty param.q
                    || not empty param.category
                    || not empty param.brand
                    || not empty param.priceRange
                    || not empty param.rating
                    || not empty param.sort}">
          <div class="product-filter-tags">

            <span class="product-filter-tags__label">Đang lọc:</span>

            <c:if test="${not empty param.q}">
              <span class="product-filter-tag">
                Từ khóa:
                <strong>
                  <c:out value="${param.q}" />
                </strong>
              </span>
            </c:if>

            <c:if test="${not empty param.category}">
              <span class="product-filter-tag">
                Danh mục:
                <strong>
                  <c:out value="${param.category}" />
                </strong>
              </span>
            </c:if>

            <c:if test="${not empty param.brand}">
              <span class="product-filter-tag">
                Thương hiệu:
                <strong>
                  <c:out value="${param.brand}" />
                </strong>
              </span>
            </c:if>

            <c:if test="${not empty param.priceRange}">
              <span class="product-filter-tag">
                Giá:
                <strong>
                  <c:choose>
                    <c:when test="${param.priceRange eq 'under-200'}">Dưới 200.000đ</c:when>
                    <c:when test="${param.priceRange eq '200-500'}">200.000đ - 500.000đ</c:when>
                    <c:when test="${param.priceRange eq '500-1000'}">500.000đ - 1.000.000đ</c:when>
                    <c:when test="${param.priceRange eq 'over-1000'}">Trên 1.000.000đ</c:when>
                    <c:otherwise><c:out value="${param.priceRange}" /></c:otherwise>
                  </c:choose>
                </strong>
              </span>
            </c:if>

            <c:if test="${not empty param.rating}">
              <span class="product-filter-tag">
                Đánh giá:
                <strong>
                  Từ <c:out value="${param.rating}" /> sao
                </strong>
              </span>
            </c:if>

            <c:if test="${not empty param.sort}">
              <span class="product-filter-tag">
                Sắp xếp:
                <strong>
                  <c:choose>
                    <c:when test="${param.sort eq 'price_asc'}">Giá tăng dần</c:when>
                    <c:when test="${param.sort eq 'price_desc'}">Giá giảm dần</c:when>
                    <c:when test="${param.sort eq 'rating_desc'}">Đánh giá cao</c:when>
                    <c:when test="${param.sort eq 'newest'}">Mới nhất</c:when>
                    <c:otherwise><c:out value="${param.sort}" /></c:otherwise>
                  </c:choose>
                </strong>
              </span>
            </c:if>

            <a class="product-filter-clear" href="${ctx}/products">
              Xóa bộ lọc
            </a>
          </div>
        </c:if>

        <!-- ================= PRODUCT GRID ================= -->
        <div class="product-grid">
          <c:choose>

            <c:when test="${not empty products}">
              <c:forEach var="product" items="${products}">
                <div class="product-card">

                  <!-- SALE BADGE -->
                  <c:if test="${product.finalPrice lt product.price}">
                    <div class="badge-sale">
                      <c:choose>
                        <c:when test="${product.discountPercent > 0}">
                          -${product.discountPercent}%
                        </c:when>
                        <c:otherwise>SALE</c:otherwise>
                      </c:choose>
                    </div>
                  </c:if>

                  <!-- IMAGE -->
                  <a class="product-img-link"
                     href="${ctx}/product/${product.slug}"
                     aria-label="Xem chi tiết ${fn:escapeXml(product.title)}">

                    <div class="product-img-box">
                      <c:choose>
                        <c:when test="${not empty product.imageUrl}">
                          <img
                                  src="${ctx}${product.imageUrl}"
                                  alt="${fn:escapeXml(product.title)}">
                        </c:when>

                        <c:otherwise>
                          <div class="no-image">No image</div>
                        </c:otherwise>
                      </c:choose>
                    </div>
                  </a>

                  <!-- TITLE -->
                  <h3>
                    <a class="product-title-link"
                       href="${ctx}/product/${product.slug}">
                      <c:out value="${product.title}" />
                    </a>
                  </h3>

                  <!-- RATING -->
                  <div class="rating-wrap">
                    <div class="rating-stars">
                      <c:forEach begin="1" end="5" var="i">
                        <c:choose>
                          <c:when test="${i <= product.avgRating}">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="#ffb400" aria-hidden="true">
                              <path d="M12 17.3l6.2 3.7-1.6-7 5.4-4.7-7.1-.6L12 2 9.1 8.7l-7.1.6 5.4 4.7-1.6 7z"/>
                            </svg>
                          </c:when>

                          <c:otherwise>
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="#e0e0e0" aria-hidden="true">
                              <path d="M12 17.3l6.2 3.7-1.6-7 5.4-4.7-7.1-.6L12 2 9.1 8.7l-7.1.6 5.4 4.7-1.6 7z"/>
                            </svg>
                          </c:otherwise>
                        </c:choose>
                      </c:forEach>
                    </div>

                    <span class="rating-count">
                      (<c:out value="${product.reviewCount}" /> đánh giá)
                    </span>
                  </div>

                  <!-- PRICE -->
                  <c:choose>
                    <c:when test="${product.finalPrice lt product.price}">
                      <p class="price">
                        <span class="old-price">
                          <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/> ₫
                        </span>

                        <span class="sale-price">
                          <fmt:formatNumber value="${product.finalPrice}" type="number" groupingUsed="true"/> ₫
                        </span>
                      </p>
                    </c:when>

                    <c:otherwise>
                      <p class="price">
                        <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/> ₫
                      </p>
                    </c:otherwise>
                  </c:choose>

                  <!-- STOCK -->
                  <c:choose>
                    <c:when test="${product.stock == 0}">
                      <div class="badge-out">Hết hàng</div>
                    </c:when>

                    <c:when test="${product.stock <= 5}">
                      <div class="badge-low">Sắp hết</div>
                    </c:when>

                    <c:otherwise>
                      <div class="stock-ok">Còn hàng</div>
                    </c:otherwise>
                  </c:choose>

                  <!-- DETAIL -->
                  <a href="${ctx}/product/${product.slug}" class="btn-outline">
                    Xem chi tiết
                  </a>

                </div>
              </c:forEach>
            </c:when>

            <c:otherwise>
              <div class="product-empty">
                <div class="product-empty__title">Không tìm thấy sản phẩm phù hợp</div>

                <div class="product-empty__text">
                  Hãy thử chọn danh mục khác, bỏ bớt bộ lọc hoặc quay lại danh sách tất cả sản phẩm.
                </div>

                <a class="btn-outline" href="${ctx}/products">
                  Xem tất cả sản phẩm
                </a>
              </div>
            </c:otherwise>

          </c:choose>
        </div>

        <!-- ================= PAGINATION ================= -->
        <c:if test="${totalPages != null && totalPages > 1}">
          <div class="pagination">

            <!-- Prev URL -->
            <c:url var="prevPageUrl" value="/products">
              <c:param name="page" value="${page - 1}" />
              <c:if test="${not empty param.q}">
                <c:param name="q" value="${param.q}" />
              </c:if>
              <c:if test="${not empty param.sort}">
                <c:param name="sort" value="${param.sort}" />
              </c:if>
              <c:if test="${not empty param.priceRange}">
                <c:param name="priceRange" value="${param.priceRange}" />
              </c:if>
              <c:if test="${not empty param.category}">
                <c:param name="category" value="${param.category}" />
              </c:if>
              <c:if test="${not empty param.brand}">
                <c:param name="brand" value="${param.brand}" />
              </c:if>
              <c:if test="${not empty param.rating}">
                <c:param name="rating" value="${param.rating}" />
              </c:if>
            </c:url>

            <!-- Next URL -->
            <c:url var="nextPageUrl" value="/products">
              <c:param name="page" value="${page + 1}" />
              <c:if test="${not empty param.q}">
                <c:param name="q" value="${param.q}" />
              </c:if>
              <c:if test="${not empty param.sort}">
                <c:param name="sort" value="${param.sort}" />
              </c:if>
              <c:if test="${not empty param.priceRange}">
                <c:param name="priceRange" value="${param.priceRange}" />
              </c:if>
              <c:if test="${not empty param.category}">
                <c:param name="category" value="${param.category}" />
              </c:if>
              <c:if test="${not empty param.brand}">
                <c:param name="brand" value="${param.brand}" />
              </c:if>
              <c:if test="${not empty param.rating}">
                <c:param name="rating" value="${param.rating}" />
              </c:if>
            </c:url>

            <!-- Prev -->
            <c:choose>
              <c:when test="${page != null && page > 1}">
                <a class="pg-btn" href="${prevPageUrl}">
                  ‹
                </a>
              </c:when>

              <c:otherwise>
                <span class="pg-btn disabled">‹</span>
              </c:otherwise>
            </c:choose>

            <!-- Pages -->
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
                    <c:if test="${not empty param.priceRange}">
                      <c:param name="priceRange" value="${param.priceRange}" />
                    </c:if>
                    <c:if test="${not empty param.category}">
                      <c:param name="category" value="${param.category}" />
                    </c:if>
                    <c:if test="${not empty param.brand}">
                      <c:param name="brand" value="${param.brand}" />
                    </c:if>
                    <c:if test="${not empty param.rating}">
                      <c:param name="rating" value="${param.rating}" />
                    </c:if>
                  </c:url>

                  <a class="pg-num" href="${pageUrl}">
                      ${p}
                  </a>
                </c:otherwise>
              </c:choose>
            </c:forEach>

            <!-- Next -->
            <c:choose>
              <c:when test="${page != null && page < totalPages}">
                <a class="pg-btn" href="${nextPageUrl}">
                  ›
                </a>
              </c:when>

              <c:otherwise>
                <span class="pg-btn disabled">›</span>
              </c:otherwise>
            </c:choose>

          </div>
        </c:if>

      </div>
    </div>
  </div>
</section>
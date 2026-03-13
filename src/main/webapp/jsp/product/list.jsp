<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

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

      <!-- ===== SIDEBAR ===== -->
      <aside class="filter-sidebar">
        <jsp:include page="/jsp/product/category_filter.jsp" />
      </aside>

      <!-- ===== MAIN ===== -->
      <div class="product-main">

        <!-- ===== GRID ===== -->
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
                  <div class="product-img-box">
                    <c:choose>
                      <c:when test="${not empty product.imageUrl}">
                        <img
                          src="${pageContext.request.contextPath}${product.imageUrl}"
                          alt="${fn:escapeXml(product.title)}">
                      </c:when>
                      <c:otherwise>
                        <div class="no-image">No image</div>
                      </c:otherwise>
                    </c:choose>
                  </div>

                  <!-- TITLE -->
                  <h3><c:out value="${product.title}"/></h3>

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

                    <span class="rating-count">(<c:out value="${product.reviewCount}"/> đánh giá)</span>
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
                  <a href="${pageContext.request.contextPath}/product/${product.slug}" class="btn-outline">
                    Xem chi tiết
                  </a>

                </div>
              </c:forEach>
            </c:when>

            <c:otherwise>
              <p style="text-align:center; color:#888;">Chưa có sản phẩm nào.</p>
            </c:otherwise>

          </c:choose>
        </div>

        <!-- ================= PAGINATION ================= -->
        <c:if test="${totalPages != null && totalPages > 1}">
          <div class="pagination">

            <!-- Prev -->
            <c:choose>
              <c:when test="${page != null && page > 1}">
                <a class="pg-btn"
                   href="${pageContext.request.contextPath}/products?page=${page-1}&q=${fn:escapeXml(param.q)}&sort=${param.sort}&priceRange=${param.priceRange}&category=${param.category}&brand=${param.brand}&rating=${param.rating}">
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
                  <a class="pg-num"
                     href="${pageContext.request.contextPath}/products?page=${p}&q=${fn:escapeXml(param.q)}&sort=${param.sort}&priceRange=${param.priceRange}&category=${param.category}&brand=${param.brand}&rating=${param.rating}">
                    ${p}
                  </a>
                </c:otherwise>
              </c:choose>
            </c:forEach>

            <!-- Next -->
            <c:choose>
              <c:when test="${page != null && page < totalPages}">
                <a class="pg-btn"
                   href="${pageContext.request.contextPath}/products?page=${page+1}&q=${fn:escapeXml(param.q)}&sort=${param.sort}&priceRange=${param.priceRange}&category=${param.category}&brand=${param.brand}&rating=${param.rating}">
                  ›
                </a>
              </c:when>
              <c:otherwise>
                <span class="pg-btn disabled">›</span>
              </c:otherwise>
            </c:choose>

          </div>
        </c:if>

      </div> <!-- /.product-main -->

    </div>
  </div>
</section>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="p" value="${product}" />
<c:set var="currentPrice" value="${not empty p.finalPrice ? p.finalPrice : p.price}" />
<c:set var="oldPrice" value="${p.price}" />
<c:set var="discountPercent" value="${p.discountPercent}" />
<c:set var="mainImage" value="${p.image}" />

<link rel="stylesheet" href="${ctx}/assets/css/product-detail.css?v=20260616_pd_ui_v8" />

<c:if test="${empty p}">
  <section class="pd-page pd-page-v5">
    <div class="pd-container">
      <div class="pd-empty-state">
        <div class="pd-empty-icon">!</div>
        <h1>Không tìm thấy sản phẩm</h1>
        <p>Sản phẩm có thể đã ngừng kinh doanh hoặc đường dẫn không còn chính xác.</p>
        <a href="${ctx}/products" class="pd-empty-btn">Quay lại sản phẩm</a>
      </div>
    </div>
  </section>
</c:if>

<c:if test="${not empty p}">
  <section class="pd-page">
    <div class="pd-container">
      <nav class="pd-breadcrumb" aria-label="breadcrumb">
        <a href="${ctx}/home">Trang chủ</a>
        <span>/</span>
        <a href="${ctx}/products">Sản phẩm</a>
        <c:if test="${not empty p.categoryName}">
          <span>/</span>
          <a href="${ctx}/products?category=${p.categoryId}"><c:out value="${p.categoryName}" /></a>
        </c:if>
        <span>/</span>
        <strong><c:out value="${p.title}" /></strong>
      </nav>

      <div class="pd-main-card">
        <div class="pd-main-grid">

          <aside class="pd-gallery-wrap">
            <div class="pd-gallery-shell">
              <div class="pd-gallery-head">
                <div>
                  <span class="pd-gallery-eyebrow">Hình ảnh sản phẩm</span>
                </div>
                <span class="pd-gallery-count">${1 + fn:length(p.images)} ảnh</span>
              </div>

              <div class="pd-main-image-box" id="pdMainImageBox">
                <c:choose>
                  <c:when test="${empty mainImage}">
                    <div class="pd-no-image">Không có ảnh sản phẩm</div>
                  </c:when>
                  <c:otherwise>
                    <c:choose>
                      <c:when test="${fn:startsWith(mainImage, 'http://') || fn:startsWith(mainImage, 'https://')}">
                        <img id="pdMainImage" src="${mainImage}" alt="${fn:escapeXml(p.title)}" />
                      </c:when>
                      <c:when test="${fn:startsWith(mainImage, '/')}">
                        <img id="pdMainImage" src="${ctx}${mainImage}" alt="${fn:escapeXml(p.title)}" />
                      </c:when>
                      <c:when test="${fn:startsWith(mainImage, 'assets/') || fn:startsWith(mainImage, 'uploads/')}">
                        <img id="pdMainImage" src="${ctx}/${mainImage}" alt="${fn:escapeXml(p.title)}" />
                      </c:when>
                      <c:otherwise>
                        <img id="pdMainImage" src="${ctx}/uploads/product/${mainImage}" alt="${fn:escapeXml(p.title)}" />
                      </c:otherwise>
                    </c:choose>
                  </c:otherwise>
                </c:choose>
              </div>

              <div class="pd-thumb-list" aria-label="Ảnh sản phẩm">
                <c:if test="${not empty mainImage}">
                  <button type="button" class="pd-thumb is-active" data-image-role="main" data-image="${fn:escapeXml(mainImage)}">
                    <c:choose>
                      <c:when test="${fn:startsWith(mainImage, 'http://') || fn:startsWith(mainImage, 'https://')}">
                        <img src="${mainImage}" alt="Ảnh chính" />
                      </c:when>
                      <c:when test="${fn:startsWith(mainImage, '/')}">
                        <img src="${ctx}${mainImage}" alt="Ảnh chính" />
                      </c:when>
                      <c:when test="${fn:startsWith(mainImage, 'assets/') || fn:startsWith(mainImage, 'uploads/')}">
                        <img src="${ctx}/${mainImage}" alt="Ảnh chính" />
                      </c:when>
                      <c:otherwise>
                        <img src="${ctx}/uploads/product/${mainImage}" alt="Ảnh chính" />
                      </c:otherwise>
                    </c:choose>
                  </button>
                </c:if>

                <c:forEach var="img" items="${p.images}" varStatus="st">
                  <c:if test="${not empty img.image}">
                    <button type="button" class="pd-thumb" data-image-role="gallery" data-image="${fn:escapeXml(img.image)}">
                      <c:choose>
                        <c:when test="${fn:startsWith(img.image, 'http://') || fn:startsWith(img.image, 'https://')}">
                          <img src="${img.image}" alt="Ảnh phụ ${st.index + 1}" />
                        </c:when>
                        <c:when test="${fn:startsWith(img.image, '/')}">
                          <img src="${ctx}${img.image}" alt="Ảnh phụ ${st.index + 1}" />
                        </c:when>
                        <c:when test="${fn:startsWith(img.image, 'assets/') || fn:startsWith(img.image, 'uploads/')}">
                          <img src="${ctx}/${img.image}" alt="Ảnh phụ ${st.index + 1}" />
                        </c:when>
                        <c:otherwise>
                          <img src="${ctx}/uploads/product/gallery/${img.image}" alt="Ảnh phụ ${st.index + 1}" />
                        </c:otherwise>
                      </c:choose>
                    </button>
                  </c:if>
                </c:forEach>
              </div>
            </div>
          </aside>

          <article class="pd-info-wrap">
            <div class="pd-badges-wrapper">
              <div class="pd-badge-list">
                <span class="pd-badge pd-badge-brand"><c:out value="${empty p.brandName ? 'MyCosmetic' : p.brandName}" /></span>
                <span class="pd-badge pd-badge-category"><c:out value="${empty p.categoryName ? 'Chính hãng' : p.categoryName}" /></span>
                <span class="pd-badge pd-badge-stock">Còn <c:out value="${p.stock}" /></span>
              </div>

              <form method="post" action="${ctx}/wishlist/toggle" class="wishlist-form">
                <input type="hidden" name="productId" value="${p.id}">
                <button type="submit" class="wishlist-btn ${isWishlisted ? 'active' : ''}">
                  <i class="${isWishlisted ? 'fa-solid' : 'fa-regular'} fa-heart"></i>
                </button>
              </form>
            </div>

            <h1 class="pd-title"><c:out value="${p.title}" /></h1>

            <div class="pd-rating-row">
              <span class="pd-stars">★★★★★</span>
              <span class="pd-rating-score">
                <fmt:formatNumber value="${empty p.avgRating ? 5 : p.avgRating}" minFractionDigits="1" maxFractionDigits="1" />
              </span>
              <span class="pd-rating-count">(<c:out value="${empty p.reviewCount ? 0 : p.reviewCount}" /> đánh giá)</span>
            </div>

            <div class="pd-price-panel">
              <div class="pd-price-line">
                <strong class="pd-current-price" id="price-display" data-base-price="${currentPrice}">
                  <fmt:formatNumber value="${currentPrice}" type="number" groupingUsed="true" />đ
                </strong>

                <c:if test="${discountPercent > 0}">
                  <span class="pd-old-price">
                    <fmt:formatNumber value="${oldPrice}" type="number" groupingUsed="true" />đ
                  </span>
                  <span class="pd-saving-badge">-${discountPercent}%</span>
                </c:if>
              </div>
              <div class="pd-vat-note">* Giá đã bao gồm thuế VAT</div>
            </div>

            <div class="pd-short-desc">
              <div class="pd-short-desc-label">Mô tả nhanh</div>
              <div class="pd-short-desc-content">
                <c:choose>
                  <c:when test="${not empty p.description}">
                    ${p.description}
                  </c:when>
                  <c:otherwise>
                    Sản phẩm chính hãng được chọn lọc tại MyCosmetic, cam kết chất lượng và an toàn cho làn da của bạn.
                  </c:otherwise>
                </c:choose>
              </div>
            </div>

            <div class="pd-product-meta-grid" aria-label="Thông tin nhanh sản phẩm">
              <div class="pd-product-meta-item">
                <span>Thương hiệu</span>
                <strong><c:out value="${empty p.brandName ? 'MyCosmetic' : p.brandName}" /></strong>
              </div>
              <div class="pd-product-meta-item">
                <span>Danh mục</span>
                <strong><c:out value="${empty p.categoryName ? 'Chính hãng' : p.categoryName}" /></strong>
              </div>
              <div class="pd-product-meta-item">
                <span>Tồn kho</span>
                <strong><c:out value="${p.stock}" /> sản phẩm</strong>
              </div>
            </div>

            <form class="pd-cart-form" action="${ctx}/cart/add" method="post">
              <input type="hidden" name="action" value="add" />
              <input type="hidden" name="quickAdd" value="1" />
              <input type="hidden" name="productId" value="${p.id}" />
              <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />

              <c:if test="${not empty variants}">
                <div class="pd-variants-wrapper">
                  <span class="pd-variant-label">Chọn phân loại:</span>
                  <div class="pd-variants-list">
                    <c:forEach var="v" items="${variants}" varStatus="st">
                      <label class="pd-variant-item">
                        <input type="radio" name="variantId" value="${v.id}" class="variant-option"
                               data-extra-price="${empty v.extraPrice ? 0 : v.extraPrice}"
                               onchange="updatePrice()" ${st.first ? 'checked' : ''} />
                        <div class="pd-variant-box">
                          <c:out value="${v.size}" />
                          <c:if test="${not empty v.size && not empty v.type}"> - </c:if>
                          <c:out value="${v.type}" />
                          <c:if test="${not empty v.extraPrice && v.extraPrice > 0}">
                            <span class="pd-variant-extra">
                              (+<fmt:formatNumber value="${v.extraPrice}" type="number" groupingUsed="true" />đ)
                            </span>
                          </c:if>
                        </div>
                      </label>
                    </c:forEach>
                  </div>
                </div>
              </c:if>

              <div class="pd-buy-row">
                <div class="pd-qty-control">
                  <button type="button" class="pd-qty-btn" data-qty-action="minus">−</button>
                  <input id="pdQuantity" name="quantity" type="number" min="1" max="${p.stock > 0 ? p.stock : 1}" value="1" readonly />
                  <button type="button" class="pd-qty-btn" data-qty-action="plus">+</button>
                </div>
                <button type="submit" class="pd-add-cart-btn" <c:if test="${p.stock <= 0}">disabled</c:if>>
                  <c:choose>
                    <c:when test="${p.stock <= 0}">Tạm Hết Hàng</c:when>
                    <c:otherwise>Thêm Vào Giỏ Hàng</c:otherwise>
                  </c:choose>
                </button>
              </div>
            </form>

            <div class="pd-service-grid" aria-label="Cam kết dịch vụ MyCosmetic">
              <div class="pd-service-item">
                <span>✓</span>
                <div><strong>Hàng chính hãng</strong><small>Cam kết nguồn gốc rõ ràng</small></div>
              </div>
              <div class="pd-service-item">
                <span>↩</span>
                <div><strong>Hỗ trợ đổi trả</strong><small>Theo chính sách cửa hàng</small></div>
              </div>
              <div class="pd-service-item">
                <span>🚚</span>
                <div><strong>Giao hàng nhanh</strong><small>Đóng gói an toàn</small></div>
              </div>
            </div>
          </article>
        </div>
      </div>

      <c:set var="comboProducts" value="${not empty boughtTogetherProducts ? boughtTogetherProducts : frequentlyBoughtProducts}" />
      <section class="pd-section pd-section-compact pd-detail-section pd-description-section" id="pd-description">
        <div class="pd-section-head pd-section-head--rich">
          <div>
            <span class="pd-section-eyebrow">Thông tin sản phẩm</span>
            <h2>Mô tả chi tiết</h2>
          </div>
        </div>

        <div class="pd-desc-layout">
          <aside class="pd-desc-side-card" aria-label="Tóm tắt sản phẩm">
            <div class="pd-desc-side-icon">✦</div>
            <h3>MyCosmetic gợi ý</h3>
            <p>Đọc kỹ phần mô tả trước khi chọn mua để nắm rõ công dụng, phân loại và cách sử dụng phù hợp.</p>
            <div class="pd-desc-fact-list">
              <div class="pd-desc-fact">
                <span>Thương hiệu</span>
                <strong><c:out value="${empty p.brandName ? 'MyCosmetic' : p.brandName}" /></strong>
              </div>
              <div class="pd-desc-fact">
                <span>Danh mục</span>
                <strong><c:out value="${empty p.categoryName ? 'Chính hãng' : p.categoryName}" /></strong>
              </div>
              <div class="pd-desc-fact">
                <span>Tình trạng</span>
                <strong><c:choose><c:when test="${p.stock > 0}">Còn hàng</c:when><c:otherwise>Tạm hết hàng</c:otherwise></c:choose></strong>
              </div>
            </div>
          </aside>

          <article class="pd-desc-main-card">
            <div class="pd-desc-content pd-desc-rich">
              <c:choose>
                <c:when test="${not empty p.description}">
                  ${p.description}
                </c:when>
                <c:otherwise>
                  <p>Thông tin chi tiết đang được cập nhật. Bạn có thể liên hệ tổng đài viên để được hỗ trợ thêm về sản phẩm này.</p>
                </c:otherwise>
              </c:choose>
            </div>
          </article>
        </div>
      </section>

      <c:if test="${not empty productMediaList}">
        <section class="pd-section pd-section-compact pd-media-section" id="pd-media">
          <div class="pd-section-head pd-section-head--rich">
            <div>
              <span class="pd-section-eyebrow">Thư viện mô tả</span>
              <h2>Ảnh & video chi tiết</h2>
            </div>
          </div>

          <div class="pd-media-grid">
            <c:forEach var="media" items="${productMediaList}" varStatus="ms">
              <c:set var="mediaUrl" value="${media.mediaUrl}" />
              <article class="pd-media-card ${media.video ? 'is-video' : 'is-image'}">
                <div class="pd-media-frame">
                  <c:choose>
                    <c:when test="${media.video}">
                      <c:choose>
                        <c:when test="${fn:startsWith(mediaUrl, 'http://') || fn:startsWith(mediaUrl, 'https://')}">
                          <video controls preload="metadata" src="${mediaUrl}"></video>
                        </c:when>
                        <c:when test="${fn:startsWith(mediaUrl, '/')}">
                          <video controls preload="metadata" src="${ctx}${mediaUrl}"></video>
                        </c:when>
                        <c:when test="${fn:startsWith(mediaUrl, 'assets/') || fn:startsWith(mediaUrl, 'uploads/')}">
                          <video controls preload="metadata" src="${ctx}/${mediaUrl}"></video>
                        </c:when>
                        <c:otherwise>
                          <video controls preload="metadata" src="${ctx}/uploads/product/media/${mediaUrl}"></video>
                        </c:otherwise>
                      </c:choose>
                    </c:when>
                    <c:otherwise>
                      <c:choose>
                        <c:when test="${fn:startsWith(mediaUrl, 'http://') || fn:startsWith(mediaUrl, 'https://')}">
                          <img src="${mediaUrl}" alt="Media sản phẩm ${ms.index + 1}" loading="lazy" />
                        </c:when>
                        <c:when test="${fn:startsWith(mediaUrl, '/')}">
                          <img src="${ctx}${mediaUrl}" alt="Media sản phẩm ${ms.index + 1}" loading="lazy" />
                        </c:when>
                        <c:when test="${fn:startsWith(mediaUrl, 'assets/') || fn:startsWith(mediaUrl, 'uploads/')}">
                          <img src="${ctx}/${mediaUrl}" alt="Media sản phẩm ${ms.index + 1}" loading="lazy" />
                        </c:when>
                        <c:otherwise>
                          <img src="${ctx}/uploads/product/media/${mediaUrl}" alt="Media sản phẩm ${ms.index + 1}" loading="lazy" />
                        </c:otherwise>
                      </c:choose>
                    </c:otherwise>
                  </c:choose>
                </div>
                <div class="pd-media-card-body">
                  <span>${media.video ? 'Video' : 'Hình ảnh'}</span>
                  <strong>Media mô tả #${ms.index + 1}</strong>
                </div>
              </article>
            </c:forEach>
          </div>
        </section>
      </c:if>

      <section class="pd-section pd-section-compact pd-reviews-section" id="pd-reviews">
        <div class="pd-section-head">
          <h2>Đánh giá từ khách hàng</h2>
          <p>Sản phẩm có tổng cộng <c:out value="${empty p.reviewCount ? 0 : p.reviewCount}" /> lượt đánh giá thực tế.</p>
        </div>

        <c:set var="reviewCount5" value="0" />
        <c:set var="reviewCount4" value="0" />
        <c:set var="reviewCount3" value="0" />
        <c:set var="reviewCount2" value="0" />
        <c:set var="reviewCount1" value="0" />
        <c:set var="reviewImageCount" value="0" />
        <c:set var="reviewVerifiedCount" value="0" />
        <c:forEach var="rv" items="${reviews}">
          <c:choose>
            <c:when test="${rv.rating == 5}"><c:set var="reviewCount5" value="${reviewCount5 + 1}" /></c:when>
            <c:when test="${rv.rating == 4}"><c:set var="reviewCount4" value="${reviewCount4 + 1}" /></c:when>
            <c:when test="${rv.rating == 3}"><c:set var="reviewCount3" value="${reviewCount3 + 1}" /></c:when>
            <c:when test="${rv.rating == 2}"><c:set var="reviewCount2" value="${reviewCount2 + 1}" /></c:when>
            <c:otherwise><c:set var="reviewCount1" value="${reviewCount1 + 1}" /></c:otherwise>
          </c:choose>
          <c:if test="${rv.hasImage && not empty rv.imageUrl}"><c:set var="reviewImageCount" value="${reviewImageCount + 1}" /></c:if>
          <c:if test="${not empty rv.orderId && rv.orderId > 0}"><c:set var="reviewVerifiedCount" value="${reviewVerifiedCount + 1}" /></c:if>
        </c:forEach>

        <div class="pd-review-toolbar">
          <div class="pd-review-summary-card">
            <div class="pd-review-summary-score">
              <fmt:formatNumber value="${empty p.avgRating ? 0 : p.avgRating}" minFractionDigits="1" maxFractionDigits="1" />
            </div>
            <div class="pd-review-summary-stars">★★★★★</div>
            <div class="pd-review-summary-note"><c:out value="${empty p.reviewCount ? 0 : p.reviewCount}" /> đánh giá</div>
          </div>
          <div class="pd-review-filters" id="pdReviewFilters">
            <button type="button" class="pd-review-filter is-active" data-filter="all">Tất cả (<c:out value="${empty p.reviewCount ? 0 : p.reviewCount}" />)</button>
            <button type="button" class="pd-review-filter" data-filter="5">5 sao (<c:out value="${reviewCount5}" />)</button>
            <button type="button" class="pd-review-filter" data-filter="4">4 sao (<c:out value="${reviewCount4}" />)</button>
            <button type="button" class="pd-review-filter" data-filter="3">3 sao (<c:out value="${reviewCount3}" />)</button>
            <button type="button" class="pd-review-filter" data-filter="image">Có ảnh (<c:out value="${reviewImageCount}" />)</button>
            <button type="button" class="pd-review-filter" data-filter="verified">Đã mua (<c:out value="${reviewVerifiedCount}" />)</button>
          </div>
        </div>
        <div class="pd-reviews-container">
          <c:choose>
            <c:when test="${empty reviews}">
              <div class="pd-no-reviews">
                <p>Chưa có đánh giá nào. Hãy là người đầu tiên trải nghiệm và nhận xét!</p>
              </div>
            </c:when>
            <c:otherwise>
              <div class="pd-review-empty-filter" id="pdReviewEmpty" hidden style="display:none;">Không có đánh giá phù hợp với bộ lọc đã chọn.</div>
              <div class="pd-reviews-list" id="pdReviewsList">
                <c:forEach var="rev" items="${reviews}">
                  <div class="pd-review-item" data-rating="${rev.rating}" data-has-image="${rev.hasImage && not empty rev.imageUrl}" data-verified="${not empty rev.orderId && rev.orderId > 0}">
                    <div class="pd-review-avatar">
                      <div class="pd-review-avatar-circle">
                        <c:out value="${fn:substring(not empty rev.authorName ? rev.authorName : 'K', 0, 1)}" />
                      </div>
                    </div>
                    <div class="pd-review-content">
                      <div class="pd-review-header">
                          <span class="pd-review-author">
                            <c:out value="${not empty rev.authorFullName ? rev.authorFullName : (not empty rev.authorName ? rev.authorName : 'Khách hàng')}" />
                          </span>
                        <c:if test="${not empty rev.orderId && rev.orderId > 0}">
                          <span class="pd-verified-badge">✓ Đã mua hàng</span>
                        </c:if>
                      </div>
                      <div class="pd-review-meta">
                          <span class="pd-review-stars">
                            <c:forEach begin="1" end="${rev.rating}">★</c:forEach><c:forEach begin="${rev.rating + 1}" end="5">☆</c:forEach>
                          </span>
                        <span class="pd-review-date">
                            <fmt:formatDate value="${rev.createdAtDate}" pattern="dd/MM/yyyy HH:mm" />
                          </span>
                      </div>
                      <div class="pd-purchased-product">
                        <span class="pd-review-product-label">Phân loại:</span> <c:out value="${not empty rev.productName ? rev.productName : p.title}" />
                      </div>
                      <div class="pd-review-body">
                        <c:out value="${rev.comment}" />
                      </div>
                      <c:if test="${rev.hasImage && not empty rev.imageUrl}">
                        <div class="pd-review-images">
                          <img src="${ctx}/${rev.imageUrl}" alt="Ảnh review" />
                        </div>
                      </c:if>
                    </div>
                  </div>
                </c:forEach>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </section>
      <c:if test="${not empty comboProducts}">
        <section class="pd-section pd-section-compact pd-combo-section" id="pd-combo">
          <div class="pd-section-head">
            <h2>Thường được mua kèm</h2>
            <p>Gợi ý các sản phẩm phù hợp để hoàn thiện quy trình chăm sóc của bạn.</p>
          </div>
          <div class="pd-product-grid">
            <c:forEach var="item" items="${comboProducts}" begin="0" end="3">
              <c:set var="itemPrice" value="${not empty item.finalPrice ? item.finalPrice : item.price}" />
              <c:set var="itemImg" value="${item.image}" />
              <article class="pd-mini-card">
                <a class="pd-mini-img" href="${ctx}/product/${item.slug}?id=${item.id}">
                  <c:choose>
                    <c:when test="${empty itemImg}"><div class="pd-mini-placeholder">No Image</div></c:when>
                    <c:when test="${fn:startsWith(itemImg, 'http://') || fn:startsWith(itemImg, 'https://')}"><img src="${itemImg}" alt="${fn:escapeXml(item.title)}" /></c:when>
                    <c:when test="${fn:startsWith(itemImg, '/')}"><img src="${ctx}${itemImg}" alt="${fn:escapeXml(item.title)}" /></c:when>
                    <c:otherwise><img src="${ctx}/${itemImg}" alt="${fn:escapeXml(item.title)}" /></c:otherwise>
                  </c:choose>
                </a>
                <div class="pd-mini-body">
                  <div class="pd-mini-brand"><c:out value="${empty item.brandName ? 'MyCosmetic' : item.brandName}" /></div>
                  <a class="pd-mini-title" href="${ctx}/product/${item.slug}?id=${item.id}"><c:out value="${item.title}" /></a>
                  <div class="pd-mini-price"><fmt:formatNumber value="${itemPrice}" type="number" groupingUsed="true" />đ</div>
                  <a class="pd-mini-link" href="${ctx}/product/${item.slug}?id=${item.id}">Xem sản phẩm</a>
                </div>
              </article>
            </c:forEach>
          </div>
        </section>
      </c:if>

      <c:if test="${not empty relatedProducts}">
        <section class="pd-section pd-section-compact pd-related-section" id="pd-related">
          <div class="pd-section-head">
            <h2>Sản phẩm tương tự</h2>
            <p>Các lựa chọn khác có thể bạn sẽ quan tâm.</p>
          </div>
          <div class="pd-product-grid">
            <c:forEach var="item" items="${relatedProducts}" begin="0" end="3">
              <c:set var="itemPrice" value="${not empty item.finalPrice ? item.finalPrice : item.price}" />
              <c:set var="itemImg" value="${item.image}" />
              <article class="pd-mini-card">
                <a class="pd-mini-img" href="${ctx}/product/${item.slug}?id=${item.id}">
                  <c:choose>
                    <c:when test="${empty itemImg}"><div class="pd-mini-placeholder">No Image</div></c:when>
                    <c:when test="${fn:startsWith(itemImg, 'http://') || fn:startsWith(itemImg, 'https://')}"><img src="${itemImg}" alt="${fn:escapeXml(item.title)}" /></c:when>
                    <c:when test="${fn:startsWith(itemImg, '/')}"><img src="${ctx}${itemImg}" alt="${fn:escapeXml(item.title)}" /></c:when>
                    <c:otherwise><img src="${ctx}/${itemImg}" alt="${fn:escapeXml(item.title)}" /></c:otherwise>
                  </c:choose>
                </a>
                <div class="pd-mini-body">
                  <div class="pd-mini-brand"><c:out value="${empty item.brandName ? 'MyCosmetic' : item.brandName}" /></div>
                  <a class="pd-mini-title" href="${ctx}/product/${item.slug}?id=${item.id}"><c:out value="${item.title}" /></a>
                  <div class="pd-mini-meta">
                    <span>★ <fmt:formatNumber value="${empty item.avgRating ? 5 : item.avgRating}" minFractionDigits="1" maxFractionDigits="1" /></span>
                    <span>(<c:out value="${empty item.reviewCount ? 0 : item.reviewCount}" />)</span>
                  </div>
                  <div class="pd-mini-price"><fmt:formatNumber value="${itemPrice}" type="number" groupingUsed="true" />đ</div>
                  <a class="pd-mini-link" href="${ctx}/product/${item.slug}?id=${item.id}">Xem sản phẩm</a>
                </div>
              </article>
            </c:forEach>
          </div>
        </section>

      </c:if>

    </div>
  </section>

  <div id="loginModal" class="modal hidden">
    <div class="modal-content">
      <div class="modal-icon"><i class="fa-solid fa-lock"></i></div>
      <p>Bạn cần đăng nhập để lưu sản phẩm vào danh sách yêu thích.</p>
      <div class="modal-actions">
        <button class="modal-btn-secondary" id="cancelLogin">Hủy bỏ</button>
        <button class="modal-btn-primary" id="confirmLogin">Đăng nhập ngay</button>
      </div>
    </div>
  </div>

  <script>
    (function () {
      const ctx = '${ctx}';
      const mainImage = document.getElementById('pdMainImage');
      const thumbs = document.querySelectorAll('.pd-thumb');

      function normalizeImageUrl(raw, role) {
        if (!raw) return '';
        if (raw.startsWith('http://') || raw.startsWith('https://')) return raw;
        if (raw.startsWith('/')) return ctx + raw;
        if (raw.startsWith('assets/') || raw.startsWith('uploads/')) return ctx + '/' + raw;
        if (role === 'main') return ctx + '/uploads/product/' + raw;
        return ctx + '/uploads/product/gallery/' + raw;
      }

      thumbs.forEach(function (btn) {
        btn.addEventListener('click', function () {
          const raw = btn.getAttribute('data-image');
          const role = btn.getAttribute('data-image-role') || 'gallery';
          if (mainImage && raw) {
            mainImage.src = normalizeImageUrl(raw, role);
          }
          thumbs.forEach(function (item) { item.classList.remove('is-active'); });
          btn.classList.add('is-active');
        });
      });

      const qtyInput = document.getElementById('pdQuantity');
      document.querySelectorAll('[data-qty-action]').forEach(function (btn) {
        btn.addEventListener('click', function () {
          if (!qtyInput) return;
          const min = parseInt(qtyInput.getAttribute('min') || '1', 10);
          const max = parseInt(qtyInput.getAttribute('max') || '999', 10);
          let value = parseInt(qtyInput.value || '1', 10);

          if (btn.getAttribute('data-qty-action') === 'plus') {
            value = Math.min(max, value + 1);
          } else {
            value = Math.max(min, value - 1);
          }
          qtyInput.value = value;
        });
      });


      const reviewFilterButtons = document.querySelectorAll('.pd-review-filter');
      const reviewItems = document.querySelectorAll('.pd-review-item');
      const reviewEmpty = document.getElementById('pdReviewEmpty');

      function applyReviewFilter(filter) {
        let visibleCount = 0;

        reviewItems.forEach(function (item) {
          const rating = item.getAttribute('data-rating');
          const hasImage = item.getAttribute('data-has-image') === 'true';
          const isVerified = item.getAttribute('data-verified') === 'true';
          let matched = false;

          if (filter === 'all') {
            matched = true;
          } else if (filter === 'image') {
            matched = hasImage;
          } else if (filter === 'verified') {
            matched = isVerified;
          } else {
            matched = rating === filter;
          }

          item.hidden = !matched;
          item.style.display = matched ? '' : 'none';

          if (matched) {
            visibleCount += 1;
          }
        });

        if (reviewEmpty) {
          const shouldShowEmpty = visibleCount === 0 && reviewItems.length > 0;
          reviewEmpty.hidden = !shouldShowEmpty;
          reviewEmpty.style.display = shouldShowEmpty ? 'grid' : 'none';
        }
      }

      reviewFilterButtons.forEach(function (button) {
        button.addEventListener('click', function () {
          reviewFilterButtons.forEach(function (btn) {
            btn.classList.remove('is-active');
          });

          button.classList.add('is-active');
          applyReviewFilter(button.getAttribute('data-filter') || 'all');
        });
      });

      applyReviewFilter('all');

    })();

    function updatePrice() {
      const priceDisplay = document.getElementById('price-display');
      if(!priceDisplay) return;
      const basePrice = parseFloat(priceDisplay.getAttribute('data-base-price'));
      const selectedVariant = document.querySelector('input[name="variantId"]:checked');
      const extraPrice = selectedVariant ? parseFloat(selectedVariant.getAttribute('data-extra-price')) : 0;
      const finalPrice = basePrice + extraPrice;
      const formatter = new Intl.NumberFormat('vi-VN');
      priceDisplay.textContent = formatter.format(finalPrice) + "đ";
    }

    window.onload = function() {
      updatePrice();
    };

    document.addEventListener("DOMContentLoaded", function () {
      const modal = document.getElementById("loginModal");
      const confirmBtn = document.getElementById("confirmLogin");
      const cancelBtn = document.getElementById("cancelLogin");
      const detailWishlistForm = document.querySelector(".wishlist-form");

      if (!detailWishlistForm) return;

      const formAction = detailWishlistForm.getAttribute("action") || "";
      const contextPath = formAction.substring(0, formAction.indexOf("/", 1)) || "";

      function openModal() { if (modal) modal.classList.remove("hidden"); }
      function closeModal() { if (modal) modal.classList.add("hidden"); }

      cancelBtn?.addEventListener("click", closeModal);
      modal?.addEventListener("click", (e) => {
        if (e.target === modal) closeModal();
      });

      confirmBtn?.addEventListener("click", () => {
        const currentUrl = window.location.href;
        window.location.href = contextPath + "/login?redirect=" + encodeURIComponent(currentUrl);
      });

      detailWishlistForm.addEventListener("submit", function (e) {
        e.preventDefault();
        const btn = this.querySelector(".wishlist-btn");
        const icon = btn?.querySelector("i");
        const formData = new URLSearchParams(new FormData(this));

        fetch(this.action, {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: formData
        })
                .then(response => {
                  if (response.status === 401) {
                    openModal();
                    throw new Error("LOGIN_REQUIRED");
                  }
                  return response.json();
                })
                .then(data => {
                  if (!btn) return;
                  if (data.wishlisted === true) {
                    if(icon) icon.className = "fa-solid fa-heart";
                    btn.classList.add("active");
                  } else {
                    if(icon) icon.className = "fa-regular fa-heart";
                    btn.classList.remove("active");
                  }
                })
                .catch(err => {
                  if (err.message !== "LOGIN_REQUIRED") console.error("Lỗi Wishlist:", err);
                });
      });
    });
  </script>
</c:if>

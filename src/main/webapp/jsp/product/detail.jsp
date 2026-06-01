<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="rawMainImg" value="${product.imageUrl}" />
<c:choose>
  <c:when test="${not empty rawMainImg and fn:startsWith(rawMainImg, 'http')}">
    <c:set var="mainImgSrc" value="${rawMainImg}" />
    <c:set var="mainImgAltSrc" value="" />
  </c:when>
  <c:when test="${not empty rawMainImg and fn:startsWith(rawMainImg, '/')}">
    <c:set var="mainImgSrc" value="${pageContext.request.contextPath}${rawMainImg}" />
    <c:set var="mainImgAltSrc" value="" />
  </c:when>
  <c:when test="${not empty rawMainImg and fn:startsWith(rawMainImg, 'assets/')}">
    <c:set var="mainImgSrc" value="${pageContext.request.contextPath}/${rawMainImg}" />
    <c:set var="mainImgAltSrc" value="" />
  </c:when>
  <c:when test="${not empty rawMainImg and fn:startsWith(rawMainImg, 'uploads/')}">
    <c:set var="mainImgSrc" value="${pageContext.request.contextPath}/${rawMainImg}" />
    <c:set var="mainImgAltSrc" value="" />
  </c:when>
  <c:when test="${not empty rawMainImg and fn:startsWith(rawMainImg, 'products/')}">
    <c:set var="mainImgFile" value="${fn:substringAfter(rawMainImg, 'products/')}" />
    <c:set var="mainImgSrc" value="${pageContext.request.contextPath}/uploads/product/${mainImgFile}" />
    <c:set var="mainImgAltSrc" value="${pageContext.request.contextPath}/${rawMainImg}" />
  </c:when>
  <c:when test="${not empty rawMainImg}">
    <c:set var="mainImgSrc" value="${pageContext.request.contextPath}/uploads/product/${rawMainImg}" />
    <c:set var="mainImgAltSrc" value="${pageContext.request.contextPath}/${rawMainImg}" />
  </c:when>
  <c:otherwise>
    <c:set var="mainImgSrc" value="" />
    <c:set var="mainImgAltSrc" value="" />
  </c:otherwise>
</c:choose>
<script src="${pageContext.request.contextPath}/assets/js/main.js"></script>

<section class="pd-page">
  <div class="pd-container">

    <nav class="pd-breadcrumb" aria-label="breadcrumb">
      <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
      <span>›</span>
      <a href="${pageContext.request.contextPath}/products">Sản phẩm</a>
      <span>›</span>
      <span><c:out value="${product.title}" /></span>
    </nav>

    <div class="pd-layout">

      <div class="pd-gallery-col">

        <div class="pd-thumbs">
          <button type="button" class="pd-thumb active" data-src="${mainImgSrc}">
            <img src="${mainImgSrc}" data-alt-src="${mainImgAltSrc}" alt="${fn:escapeXml(product.title)}" onerror="handleImgError(this)">
          </button>

          <c:if test="${not empty product.images}">
            <c:forEach var="img" items="${product.images}">
              <c:if test="${not empty img.imageUrl}">
                <c:set var="rawGalleryImg" value="${img.imageUrl}" />
                <c:choose>
                  <c:when test="${not empty rawGalleryImg and fn:startsWith(rawGalleryImg, 'http')}">
                    <c:set var="galleryImgSrc" value="${rawGalleryImg}" />
                    <c:set var="galleryImgAltSrc" value="" />
                  </c:when>
                  <c:when test="${not empty rawGalleryImg and fn:startsWith(rawGalleryImg, '/')}">
                    <c:set var="galleryImgSrc" value="${pageContext.request.contextPath}${rawGalleryImg}" />
                    <c:set var="galleryImgAltSrc" value="" />
                  </c:when>
                  <c:when test="${not empty rawGalleryImg and fn:startsWith(rawGalleryImg, 'assets/')}">
                    <c:set var="galleryImgSrc" value="${pageContext.request.contextPath}/${rawGalleryImg}" />
                    <c:set var="galleryImgAltSrc" value="" />
                  </c:when>
                  <c:when test="${not empty rawGalleryImg and fn:startsWith(rawGalleryImg, 'uploads/')}">
                    <c:set var="galleryImgSrc" value="${pageContext.request.contextPath}/${rawGalleryImg}" />
                    <c:set var="galleryImgAltSrc" value="" />
                  </c:when>
                  <c:otherwise>
                    <c:set var="galleryImgSrc" value="${pageContext.request.contextPath}/uploads/product/${rawGalleryImg}" />
                    <c:set var="galleryImgAltSrc" value="${pageContext.request.contextPath}/${rawGalleryImg}" />
                  </c:otherwise>
                </c:choose>
                <button type="button" class="pd-thumb" data-src="${galleryImgSrc}" data-alt-src="${galleryImgAltSrc}">
                  <img src="${galleryImgSrc}" data-alt-src="${galleryImgAltSrc}" alt="gallery" onerror="handleImgError(this)">
                </button>
              </c:if>
            </c:forEach>
          </c:if>
        </div>

        <div class="pd-main-image" id="mainImageBox">
          <img id="mainProductImage" src="${mainImgSrc}" data-alt-src="${mainImgAltSrc}" alt="${fn:escapeXml(product.title)}" onerror="handleMainImgError(this)">
        </div>

      </div>

      <div class="pd-info-col">

        <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 16px;">
          <h1 class="pd-title" style="flex: 1; margin: 0;">
            <c:out value="${product.title}" />
          </h1>

          <button type="button" class="wishlist-btn ${inWishlist ? 'active' : ''}" onclick="toggleWishlistDetail(${product.id}, this)"
                  style="color: ${inWishlist ? '#ff4757' : '#999'}; cursor: pointer; border: none; background: none; font-size: 24px; transition: 0.3s;">
            ❤
          </button>
        </div>

        <div class="pd-tags">
          <span class="pd-tag pink">MyCosmetic</span>
          <span class="pd-tag blue">Chính hãng</span>
          <span class="pd-tag yellow">Còn ${product.stock}</span>

          <c:if test="${not empty categoryTags}">
            <c:forEach var="tag" items="${categoryTags}">
              <span class="pd-tag category-tag"><c:out value="${tag.name}" /></span>
            </c:forEach>
          </c:if>
        </div>

        <div class="pd-rating-row">
          <div class="pd-stars">
            <c:forEach begin="1" end="5" var="i">
              <c:choose>
                <c:when test="${i <= product.avgRating}">★</c:when>
                <c:otherwise>☆</c:otherwise>
              </c:choose>
            </c:forEach>
          </div>
          <span class="pd-rating-badge"><fmt:formatNumber value="${product.avgRating}" maxFractionDigits="1" /></span>
          <span class="pd-review-count">(${product.reviewCount} đánh giá)</span>
        </div>

        <div class="pd-price-box">
          <c:set var="displayPrice" value="${basePrice}" />
          <span class="pd-sale-price" id="productPrice" data-base-price="${displayPrice}">
            <fmt:formatNumber value="${displayPrice}" type="number" groupingUsed="true"/>đ
          </span>

          <c:if test="${priceSaved gt 0}">
            <span class="pd-save-badge">
              Tiết kiệm <fmt:formatNumber value="${priceSaved}" type="number" groupingUsed="true"/>đ
            </span>
          </c:if>

          <c:if test="${product.finalPrice lt product.price}">
            <span class="pd-old-price">
              <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>đ
            </span>
          </c:if>
          <div class="pd-vat-note">*Giá đã bao gồm VAT</div>
        </div>

        <p class="pd-short-desc">
          <strong><c:out value="${product.title}" /></strong>
          <c:if test="${not empty product.description}">
            <br>Xem mô tả chi tiết sản phẩm ở phần bên dưới.
          </c:if>
        </p>

        <div class="pd-promo-strip">
          <div class="pd-promo-icon">%</div>
          <div>
            <strong>Ưu đãi độc quyền tại MyCosmetic</strong>
            <span>Giá đã bao gồm VAT, hỗ trợ chọn phân loại và thêm nhanh vào giỏ hàng.</span>
          </div>
        </div>

        <c:if test="${param.outOfStock == '1'}">
          <div class="pd-alert">Sản phẩm hiện đã hết hàng.</div>
        </c:if>
        <c:if test="${param.variantRequired == '1'}">
          <div class="pd-alert">Vui lòng chọn size/loại trước khi thêm vào giỏ hàng.</div>
        </c:if>
        <c:if test="${param.variantInvalid == '1'}">
          <div class="pd-alert">Biến thể không hợp lệ hoặc đã ngừng bán.</div>
        </c:if>
        <c:if test="${param.variantOutOfStock == '1'}">
          <div class="pd-alert">Biến thể đã hết hàng.</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/cart/add" class="pd-buy-form">
          <input type="hidden" name="productId" value="${product.id}">
          <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">

          <c:if test="${not empty variants}">
            <div class="pd-option-group">
              <div class="pd-option-title">Phân loại / Dung tích</div>

              <div class="pd-variant-list">
                <c:forEach var="v" items="${variants}">
                  <input type="radio" class="pd-variant-radio" name="variantId" id="variant_${v.id}" value="${v.id}"
                         data-extra-price="${v.extraPrice}" data-stock="${v.stock}" data-size="${v.size}" data-type="${v.type}" data-label="${v.displayLabel}"
                    ${v.stock <= 0 ? 'disabled' : ''} required>

                  <label class="pd-variant-card ${v.stock <= 0 ? 'disabled' : ''}" for="variant_${v.id}">
                    <span class="variant-name"><c:out value="${v.displayName}" /></span>
                    <c:if test="${v.extraPrice > 0}">
                      <span class="variant-extra">+<fmt:formatNumber value="${v.extraPrice}" type="number" groupingUsed="true" />đ</span>
                    </c:if>
                    <span class="variant-stock">Còn ${v.stock}</span>
                  </label>
                </c:forEach>
              </div>

              <div class="pd-variant-selected">
                <div>Đã chọn: <strong id="selectedVariant">Chưa chọn</strong></div>
                <div>Tồn kho: <strong id="variantStock">-</strong> sản phẩm</div>
              </div>
            </div>
          </c:if>

          <div class="pd-action-row">
            <div class="pd-quantity-row">
              <span class="pd-option-title">SỐ LƯỢNG:</span>
              <div class="pd-qty-control">
                <button type="button" class="qty-btn" id="qtyMinus">−</button>
                <input type="number" name="quantity" id="quantityInput" value="1" min="1" max="${product.stock}">
                <button type="button" class="qty-btn" id="qtyPlus">+</button>
              </div>
            </div>
            <button type="submit" class="pd-add-cart">Thêm vào giỏ hàng</button>
          </div>
        </form>
      </div>
    </div>

    <section class="pd-recommendations">
      <c:if test="${not empty boughtTogetherProducts}">
        <div class="recommend-section">
          <h2 class="pd-section-title">Thường được mua kèm</h2>
          <div class="recommend-grid">
            <c:forEach var="rp" items="${boughtTogetherProducts}">
              <a href="${pageContext.request.contextPath}/product/${rp.slug}" class="recommend-card">
                <img src="${pageContext.request.contextPath}${rp.imageUrl}" alt="${rp.title}">
                <h4>${rp.title}</h4>
                <span class="r-price"><fmt:formatNumber value="${rp.price}" type="number" groupingUsed="true"/>đ</span>
                <span class="bundle-badge">Ưu đãi mua kèm</span>
              </a>
            </c:forEach>
          </div>
        </div>
      </c:if>

      <c:if test="${not empty relatedProducts}">
        <div class="recommend-section">
          <h2 class="pd-section-title">Sản phẩm liên quan</h2>
          <div class="recommend-grid">
            <c:forEach var="rp" items="${relatedProducts}">
              <a href="${pageContext.request.contextPath}/product/${rp.slug}" class="recommend-card">
                <img src="${pageContext.request.contextPath}${rp.imageUrl}" alt="${rp.title}">
                <h4>${rp.title}</h4>
                <span class="r-price"><fmt:formatNumber value="${rp.price}" type="number" groupingUsed="true"/>đ</span>
              </a>
            </c:forEach>
          </div>
        </div>
      </c:if>
    </section>

    <section class="pd-detail-content-section">

      <section class="pd-description-section">
        <h2 class="pd-section-title">Thông số & Chi tiết sản phẩm</h2>

        <table class="pd-spec-table">
          <tbody>
          <tr>
            <td>Thương hiệu</td>
            <c:catch var="errBrand"><c:set var="bName" value="${product.brandName}" /></c:catch>
            <td><c:out value="${empty errBrand && not empty bName ? bName : 'Đang cập nhật'}" /></td>
          </tr>
          <tr>
            <td>Danh mục</td>
            <c:catch var="errCat"><c:set var="cName" value="${product.categoryName}" /></c:catch>
            <td><c:out value="${empty errCat && not empty cName ? cName : 'Mỹ phẩm'}" /></td>
          </tr>
          <tr>
            <td>Tình trạng</td>
            <td>Mới 100% - Chính hãng</td>
          </tr>
          </tbody>
        </table>

        <c:if test="${not empty product.description}">
          <div class="pd-rich-description">
              ${product.description}
          </div>
        </c:if>
      </section>

      <c:set var="displayProductMedia" value="${productMediaList}" />
      <section class="pd-product-media-section">
        <div class="pd-product-media-head">
          <div>
            <h2 class="pd-section-title">Hình ảnh và video thực tế</h2>
            <p class="pd-product-media-desc">Bộ sưu tập ảnh/video chi tiết giúp khách hàng xem rõ hơn về sản phẩm.</p>
          </div>
        </div>

        <c:choose>
          <c:when test="${not empty displayProductMedia}">
            <div class="pd-product-media-grid">
              <c:forEach var="media" items="${displayProductMedia}">
                <div class="pd-product-media-card">
                  <div class="pd-product-media-preview">
                    <span class="pd-product-media-badge"><c:out value="${media.displayTypeLabel}" /></span>
                    <c:choose>
                      <c:when test="${media.video}">
                        <video controls preload="metadata">
                          <source src="${pageContext.request.contextPath}${media.mediaUrl}">
                        </video>
                      </c:when>
                      <c:otherwise>
                        <img src="${pageContext.request.contextPath}${media.mediaUrl}" loading="lazy">
                      </c:otherwise>
                    </c:choose>
                  </div>
                  <div class="pd-product-media-body">
                    <div class="pd-product-media-type"><c:out value="${media.displayTypeLabel}" /></div>
                  </div>
                </div>
              </c:forEach>
            </div>
          </c:when>
          <c:otherwise>
            <div class="pd-product-media-empty">Chưa có ảnh/video chi tiết cho sản phẩm này.</div>
          </c:otherwise>
        </c:choose>
      </section>

    </section>

    <section class="pd-review-section" id="reviews">
      <h2 class="pd-section-title">Đánh giá từ khách hàng</h2>

      <c:if test="${param.success == 'review_pending'}">
        <div class="review-note" style="background:#ecfdf5;color:#166534;border:1px solid #bbf7d0;padding:12px 14px;border-radius:14px;margin-bottom:14px;">
          Đánh giá của bạn đã được gửi và đang chờ duyệt.
        </div>
      </c:if>

      <c:if test="${param.error == 'not_eligible'}">
        <div class="review-note" style="background:#fff1f2;color:#be123c;border:1px solid #fecdd3;padding:12px 14px;border-radius:14px;margin-bottom:14px;">
          Bạn chỉ có thể đánh giá sản phẩm đã mua và giao thành công.
        </div>
      </c:if>

      <c:choose>
        <c:when test="${not empty sessionScope.user && canReviewProduct}">
          <form method="post" action="${pageContext.request.contextPath}/review" class="review-form">
            <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
            <input type="hidden" name="productId" value="${product.id}">
            <input type="hidden" name="slug" value="${product.slug}">

            <c:catch var="errVarId">
              <input type="hidden" name="variantId" value="${purchasedVariantId}">
            </c:catch>

            <div class="review-product-info" style="margin-bottom: 16px; padding: 12px 16px; background: #fff; border: 1px solid #e2e8f0; border-radius: 10px;">
              <p style="margin: 0; font-weight: 600; color: #1e293b; font-size: 15px;">
                Đánh giá sản phẩm: <span style="color: #db2777;"><c:out value="${product.title}"/></span>
              </p>
              <c:catch var="errVarName">
                <c:if test="${not empty purchasedVariantName}">
                  <p style="margin: 6px 0 0 0; font-size: 14px; color: #475569;">
                    Phân loại bạn đã mua: <strong style="color: #0f172a; background: #f1f5f9; padding: 2px 6px; border-radius: 4px;"><c:out value="${purchasedVariantName}"/></strong>
                  </p>
                </c:if>
              </c:catch>
            </div>

            <textarea name="comment" placeholder="Chia sẻ cảm nhận của bạn về sản phẩm..." minlength="5" required></textarea>
            <div style="margin: 10px 0;">
              <label style="display:block; margin-bottom:5px; font-weight:500;">Tải ảnh từ máy tính (nếu có):</label>
              <input type="file"
                     name="imageFile"
                     accept="image/*"
                     class="review-media-input">
            </div>

            <div style="margin: 10px 0 20px 0;">
              <label style="display:block; margin-bottom:5px; font-weight:500;">Tải video từ máy tính (nếu có):</label>
              <input type="file"
                     name="videoFile"
                     accept="video/*"
                     class="review-media-input">
            </div>
            <button type="submit" class="btn-review">Gửi đánh giá chờ duyệt</button>
          </form>
        </c:when>
        <c:when test="${not empty sessionScope.user && !canReviewProduct}">
          <p class="review-note">Bạn có thể đánh giá sản phẩm này sau khi đơn hàng đã được giao thành công.</p>
        </c:when>
        <c:otherwise>
          <p class="review-note">Vui lòng <a href="${pageContext.request.contextPath}/login">đăng nhập</a> và mua hàng để đánh giá.</p>
        </c:otherwise>
      </c:choose>

      <form method="get" action="${pageContext.request.contextPath}/product/${product.slug}#reviews" class="review-filter-form">
        <select name="reviewSort">
          <option value="newest" ${reviewSort == 'newest' || empty reviewSort ? 'selected' : ''}>Gần nhất</option>
          <option value="oldest" ${reviewSort == 'oldest' ? 'selected' : ''}>Cũ nhất</option>
          <option value="highest" ${reviewSort == 'highest' ? 'selected' : ''}>Sao cao nhất</option>
          <option value="lowest" ${reviewSort == 'lowest' ? 'selected' : ''}>Sao thấp nhất</option>
          <option value="media" ${reviewSort == 'media' ? 'selected' : ''}>Có ảnh/video trước</option>
        </select>
        <select name="reviewRating">
          <option value="" ${empty reviewRating ? 'selected' : ''}>Tất cả số sao</option>
          <option value="5" ${reviewRating == 5 ? 'selected' : ''}>5 sao</option>
          <option value="4" ${reviewRating == 4 ? 'selected' : ''}>4 sao</option>
          <option value="3" ${reviewRating == 3 ? 'selected' : ''}>3 sao</option>
          <option value="2" ${reviewRating == 2 ? 'selected' : ''}>2 sao</option>
          <option value="1" ${reviewRating == 1 ? 'selected' : ''}>1 sao</option>
        </select>
        <select name="reviewMedia">
          <option value="" ${empty reviewMedia ? 'selected' : ''}>Tất cả đánh giá</option>
          <option value="IMAGE" ${reviewMedia == 'IMAGE' ? 'selected' : ''}>Có hình ảnh</option>
          <option value="VIDEO" ${reviewMedia == 'VIDEO' ? 'selected' : ''}>Có video</option>
          <option value="MEDIA" ${reviewMedia == 'MEDIA' ? 'selected' : ''}>Có ảnh hoặc video</option>
        </select>
        <button type="submit" class="btn-review-filter">Lọc</button>
        <a href="${pageContext.request.contextPath}/product/${product.slug}#reviews" class="btn-review-filter clear">Xóa lọc</a>
      </form>

      <c:choose>
        <c:when test="${not empty reviews}">
          <div class="review-list">
            <c:forEach var="r" items="${reviews}">

              <div class="review-item">
                <div class="review-user-info">
                  <c:set var="avatarUrl" value="${pageContext.request.contextPath}/assets/images/default-avatar.png" />
                  <c:catch var="errAvatar">
                    <c:if test="${not empty r.userAvatar}">
                      <c:set var="avatarUrl" value="${r.userAvatar}" />
                    </c:if>
                  </c:catch>
                  <img src="${avatarUrl}" class="review-avatar" alt="Avatar">

                  <div class="review-meta-info">
                    <strong><c:out value="${r.authorName}" /></strong>
                    <span class="review-time"><fmt:formatDate value="${r.createdAtDate}" pattern="dd/MM/yyyy HH:mm" /></span>
                  </div>
                </div>

                <div class="review-stars">
                  <c:forEach begin="1" end="5" var="i">
                    <c:choose><c:when test="${i <= r.rating}">★</c:when><c:otherwise>☆</c:otherwise></c:choose>
                  </c:forEach>
                </div>

                <c:catch var="errVariant">
                  <c:if test="${not empty r.variantName || not empty r.variantType}">
                    <div class="review-variant-bought">
                      Phân loại đã mua: <c:out value="${not empty r.variantName ? r.variantName : r.variantType}" />
                      <c:if test="${not empty r.variantSize}"> - Size: <c:out value="${r.variantSize}" /></c:if>
                    </div>
                  </c:if>
                </c:catch>

                <p><c:out value="${r.comment}" /></p>

                <c:if test="${r.hasImage || r.hasVideo}">
                  <div class="review-media-display">
                    <c:if test="${r.hasImage && not empty r.imageUrl}">
                      <img src="${r.imageUrl}" alt="review image" onclick="window.open('${r.imageUrl}', '_blank')">
                    </c:if>
                    <c:if test="${r.hasVideo && not empty r.videoUrl}">
                      <video src="${r.videoUrl}" controls></video>
                    </c:if>
                  </div>
                </c:if>
              </div>

            </c:forEach>
          </div>
        </c:when>
        <c:otherwise>
          <p class="no-review" style="text-align:center; color:#64748b; margin-top: 20px;">Chưa có đánh giá phù hợp với bộ lọc hiện tại.</p>
        </c:otherwise>
      </c:choose>
    </section>

  </div>
</section>

<script>
  function handleImgError(img) {
    const altSrc = img.getAttribute("data-alt-src");
    if (altSrc) {
      img.removeAttribute("data-alt-src");
      img.src = altSrc;
      return;
    }

    img.style.display = "none";
    const parent = img.closest(".pd-thumb");
    if (parent) { parent.classList.add("missing-img"); }
  }

  function handleMainImgError(img) {
    const altSrc = img.getAttribute("data-alt-src");
    if (altSrc) {
      img.removeAttribute("data-alt-src");
      img.src = altSrc;
      return;
    }

    img.style.display = "none";
    const box = document.getElementById("mainImageBox");
    if (box) { box.classList.add("missing-img"); }
  }

  document.addEventListener("DOMContentLoaded", function () {
    const mainImg = document.getElementById("mainProductImage");
    const mainBox = document.getElementById("mainImageBox");
    const thumbs = document.querySelectorAll(".pd-thumb");

    thumbs.forEach(function (thumb) {
      thumb.addEventListener("click", function () {
        thumbs.forEach(function (item) {
          item.classList.remove("active");
        });

        thumb.classList.add("active");
        const src = thumb.getAttribute("data-src");
        const altSrc = thumb.getAttribute("data-alt-src");

        if (mainImg && src) {
          mainImg.style.display = "block";
          if (altSrc) {
            mainImg.setAttribute("data-alt-src", altSrc);
          } else {
            mainImg.removeAttribute("data-alt-src");
          }
          mainImg.src = src;

          if (mainBox) {
            mainBox.classList.remove("missing-img");
          }
        }
      });
    });

    const minusBtn = document.getElementById("qtyMinus");
    const plusBtn = document.getElementById("qtyPlus");
    const qtyInput = document.getElementById("quantityInput");

    if (minusBtn && plusBtn && qtyInput) {
      minusBtn.addEventListener("click", function () {
        const current = Number(qtyInput.value || 1);
        qtyInput.value = Math.max(1, current - 1);
      });

      plusBtn.addEventListener("click", function () {
        const current = Number(qtyInput.value || 1);
        const max = Number(qtyInput.max || 9999);
        if (current < max) {
          qtyInput.value = current + 1;
        }
      });
    }

    const variantRadios = document.querySelectorAll(".pd-variant-radio");
    const priceElement = document.getElementById("productPrice");
    const selectedVariantText = document.getElementById("selectedVariant");
    const variantStockText = document.getElementById("variantStock");
    if (variantRadios.length > 0 && priceElement) {

      const basePrice = parseFloat(priceElement.dataset.basePrice);
      variantRadios.forEach(radio => {
        radio.addEventListener("change", function () {
          const extraPrice = parseFloat(this.dataset.extraPrice || 0);
          const stock = parseInt(this.dataset.stock || 0);
          const label = this.dataset.label;
          const finalPrice = basePrice + extraPrice;

          priceElement.innerText = finalPrice.toLocaleString("vi-VN") + "đ";

          if (selectedVariantText) {
            selectedVariantText.innerText = label;
          }
          if (variantStockText) {
            variantStockText.innerText = stock;
          }
          if (qtyInput) {
            qtyInput.max = stock;
            if (Number(qtyInput.value) > stock) {
              qtyInput.value = stock;
            }
          }
        });
      });
      const firstAvailable = document.querySelector(".pd-variant-radio:not(:disabled)");
      if (firstAvailable) {
        firstAvailable.checked = true;
        firstAvailable.dispatchEvent(new Event("change"));
      }
    }

    const stars = document.querySelectorAll(".star-input .star");
    const ratingInput = document.getElementById("ratingInput");
    let selectedRating = 5;

    function highlight(rating) {
      stars.forEach(function (star) {
        const value = Number(star.dataset.value);
        star.classList.toggle("active", value <= rating);
      });
    }

    stars.forEach(function (star) {
      const value = Number(star.dataset.value);
      star.addEventListener("mouseenter", function () { highlight(value); });
      star.addEventListener("click", function () {
        selectedRating = value;
        if (ratingInput) { ratingInput.value = value; }
        highlight(value);
      });
    });

    highlight(selectedRating);
  });

  function toggleWishlistDetail(productId, btn) {
    const formData = new URLSearchParams();
    formData.append("productId", productId);

    fetch("${pageContext.request.contextPath}/wishlist/toggle", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: formData
    })
            .then(response => {
              if (response.status === 401) {
                showLoginModal();
                throw new Error("LOGIN_REQUIRED");
              }
              return response.text();
            })
            .then(data => {
              if (data === "ADDED") {
                btn.style.color = "#ff4757";
                btn.style.transform = "scale(1.2)";
                setTimeout(() => btn.style.transform = "scale(1)", 200);
              } else if (data === "REMOVED") {
                btn.style.color = "#999";
              }
            })
            .catch(err => {
              if (err.message !== "LOGIN_REQUIRED") console.error("Lỗi Wishlist:", err);
            });
  }
</script>

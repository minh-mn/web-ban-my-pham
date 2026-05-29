<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="mainImg" value="${product.imageUrl}" />

<section class="pd-page">
  <div class="pd-container">

    <div class="pd-layout">

      <!-- LEFT: GALLERY -->
      <div class="pd-gallery-col">

        <div class="pd-thumbs">
          <button type="button"
                  class="pd-thumb active"
                  data-src="${pageContext.request.contextPath}${mainImg}">
            <img src="${pageContext.request.contextPath}${mainImg}"
                 alt="${fn:escapeXml(product.title)}"
                 onerror="handleImgError(this)">
          </button>

          <c:if test="${not empty product.images}">
            <c:forEach var="img" items="${product.images}">
              <c:if test="${not empty img.imageUrl}">
                <button type="button"
                        class="pd-thumb"
                        data-src="${pageContext.request.contextPath}${img.imageUrl}">
                  <img src="${pageContext.request.contextPath}${img.imageUrl}"
                       alt="gallery"
                       onerror="handleImgError(this)">
                </button>
              </c:if>
            </c:forEach>
          </c:if>
        </div>

        <div class="pd-main-image" id="mainImageBox">
          <img id="mainProductImage"
               src="${pageContext.request.contextPath}${mainImg}"
               alt="${fn:escapeXml(product.title)}"
               onerror="handleMainImgError(this)">
        </div>

      </div>

      <!-- RIGHT: PRODUCT INFO -->
      <div class="pd-info-col">

        <h1 class="pd-title">
          <c:out value="${product.title}" />
        </h1>

        <div class="pd-tags">
          <span class="pd-tag pink">MyCosmetic</span>
          <span class="pd-tag blue">Chính hãng</span>
          <span class="pd-tag yellow">Còn ${product.stock}</span>

          <c:if test="${not empty categoryTags}">
            <c:forEach var="tag" items="${categoryTags}">
              <span class="pd-tag category-tag">
                <c:out value="${tag.name}" />
              </span>
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

          <span class="pd-rating-badge">
            <fmt:formatNumber value="${product.avgRating}" maxFractionDigits="1" />
          </span>

          <span class="pd-review-count">
            (${product.reviewCount} đánh giá)
          </span>
        </div>

        <div class="pd-price-box">
          <c:choose>
            <c:when test="${product.finalPrice lt product.price}">
              <span class="pd-old-price">
                <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true" />đ
              </span>

              <span class="pd-sale-price">
                <fmt:formatNumber value="${product.finalPrice}" type="number" groupingUsed="true" />đ
              </span>

              <c:if test="${product.discountPercent > 0}">
                <span class="pd-discount-badge">
                  -${product.discountPercent}%
                </span>
              </c:if>
            </c:when>

            <c:otherwise>
              <span class="pd-sale-price">
                <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true" />đ
              </span>
            </c:otherwise>
          </c:choose>

          <div class="pd-vat-note">*Giá đã bao gồm VAT</div>
        </div>

        <p class="pd-short-desc">
          <strong><c:out value="${product.title}" /></strong>
          <c:if test="${not empty product.description}">
            <br>
            <c:out value="${product.description}" />
          </c:if>
        </p>

        <c:if test="${param.variantRequired == '1'}">
          <div class="pd-alert">Vui lòng chọn size/loại trước khi thêm vào giỏ hàng.</div>
        </c:if>

        <c:if test="${param.variantInvalid == '1'}">
          <div class="pd-alert">Biến thể không hợp lệ hoặc đã ngừng bán.</div>
        </c:if>

        <c:if test="${param.variantOutOfStock == '1'}">
          <div class="pd-alert">Biến thể đã hết hàng.</div>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/cart/add"
              class="pd-buy-form">

          <input type="hidden" name="productId" value="${product.id}">

          <c:if test="${not empty variants}">
            <div class="pd-option-group">
              <div class="pd-option-title">Phân loại / Dung tích</div>

              <div class="pd-variant-list">
                <c:forEach var="v" items="${variants}">
                  <input type="radio"
                         class="pd-variant-radio"
                         name="variantId"
                         id="variant_${v.id}"
                         value="${v.id}"
                    ${v.stock <= 0 ? 'disabled' : ''}
                         required>

                  <label class="pd-variant-card ${v.stock <= 0 ? 'disabled' : ''}"
                         for="variant_${v.id}">
                    <span class="variant-name">
                      <c:out value="${v.displayName}" />
                    </span>

                    <c:if test="${v.extraPrice > 0}">
                      <span class="variant-extra">
                        +<fmt:formatNumber value="${v.extraPrice}" type="number" groupingUsed="true" />đ
                      </span>
                    </c:if>

                    <span class="variant-stock">
                      Còn ${v.stock}
                    </span>
                  </label>
                </c:forEach>
              </div>
            </div>
          </c:if>

          <div class="pd-action-row">

            <div class="pd-quantity-row">
              <span class="pd-option-title">SỐ LƯỢNG:</span>

              <div class="pd-qty-control">
                <button type="button" class="qty-btn" id="qtyMinus">−</button>
                <input type="number" name="quantity" id="quantityInput" value="1" min="1">
                <button type="button" class="qty-btn" id="qtyPlus">+</button>
              </div>
            </div>

            <button type="submit" class="pd-add-cart">
              Thêm vào giỏ hàng
            </button>

          </div>

        </form>

      </div>
    </div>

    <section class="pd-review-section" id="reviews">
      <h2>Đánh giá từ khách hàng</h2>

      <c:if test="${param.success == 'review_pending'}">
        <div class="review-note" style="background:#ecfdf5;color:#166534;border:1px solid #bbf7d0;padding:12px 14px;border-radius:14px;margin-bottom:14px;">
          Đánh giá của bạn đã được gửi và đang chờ quản trị viên duyệt. Sau khi duyệt, voucher cảm ơn sẽ được lưu vào tài khoản.
        </div>
      </c:if>

      <c:if test="${param.error == 'not_eligible'}">
        <div class="review-note" style="background:#fff1f2;color:#be123c;border:1px solid #fecdd3;padding:12px 14px;border-radius:14px;margin-bottom:14px;">
          Bạn chỉ có thể đánh giá sản phẩm đã mua, đã thanh toán và đã giao thành công.
        </div>
      </c:if>

      <c:choose>
        <c:when test="${not empty sessionScope.user && canReviewProduct}">
          <form method="post"
                action="${pageContext.request.contextPath}/review"
                class="review-form">

            <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
            <input type="hidden" name="productId" value="${product.id}">
            <input type="hidden" name="slug" value="${product.slug}">

            <div class="star-input">
              <input type="hidden" name="rating" id="ratingInput" value="5">

              <c:forEach begin="1" end="5" var="i">
                <span class="star" data-value="${i}">★</span>
              </c:forEach>
            </div>

            <textarea name="comment"
                      placeholder="Chia sẻ cảm nhận của bạn về sản phẩm..."
                      minlength="5"
                      required></textarea>

            <input type="url"
                   name="imageUrl"
                   class="review-media-input"
                   placeholder="URL ảnh đánh giá nếu có">

            <input type="url"
                   name="videoUrl"
                   class="review-media-input"
                   placeholder="URL video đánh giá nếu có">

            <button type="submit" class="btn-review">Gửi đánh giá chờ duyệt</button>
          </form>
        </c:when>

        <c:when test="${not empty sessionScope.user && !canReviewProduct}">
          <p class="review-note">
            Bạn có thể đánh giá sản phẩm này sau khi đơn hàng đã được giao thành công.
          </p>
        </c:when>

        <c:otherwise>
          <p class="review-note">
            Vui lòng <a href="${pageContext.request.contextPath}/login">đăng nhập</a> để đánh giá.
          </p>
        </c:otherwise>
      </c:choose>

      <form method="get"
            action="${pageContext.request.contextPath}/product/${product.slug}#reviews"
            class="review-filter-form">

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

        <a href="${pageContext.request.contextPath}/product/${product.slug}#reviews"
           class="btn-review-filter clear">
          Xóa lọc
        </a>
      </form>

      <c:choose>
        <c:when test="${not empty reviews}">
          <div class="review-list">
            <c:forEach var="r" items="${reviews}">
              <div class="review-item">
                <div class="review-header">
                  <strong><c:out value="${r.authorName}" /></strong>

                  <div class="review-stars">
                    <c:forEach begin="1" end="5" var="i">
                      <c:choose>
                        <c:when test="${i <= r.rating}">★</c:when>
                        <c:otherwise>☆</c:otherwise>
                      </c:choose>
                    </c:forEach>
                  </div>
                </div>

                <p><c:out value="${r.comment}" /></p>

                <c:if test="${r.hasImage || r.hasVideo}">
                  <div class="review-media-preview">
                    <c:if test="${r.hasImage && not empty r.imageUrl}">
                      <a href="${r.imageUrl}" target="_blank" rel="noopener">Xem ảnh</a>
                    </c:if>

                    <c:if test="${r.hasVideo && not empty r.videoUrl}">
                      <a href="${r.videoUrl}" target="_blank" rel="noopener">Xem video</a>
                    </c:if>
                  </div>
                </c:if>

                <span>
                  <fmt:formatDate value="${r.createdAtDate}" pattern="dd/MM/yyyy" />
                </span>
              </div>
            </c:forEach>
          </div>
        </c:when>

        <c:otherwise>
          <p class="no-review">Chưa có đánh giá phù hợp với bộ lọc hiện tại.</p>
        </c:otherwise>
      </c:choose>
    </section>

  </div>
</section>

<script>
  function handleImgError(img) {
    img.style.display = "none";

    const parent = img.closest(".pd-thumb");

    if (parent) {
      parent.classList.add("missing-img");
    }
  }

  function handleMainImgError(img) {
    img.style.display = "none";

    const box = document.getElementById("mainImageBox");

    if (box) {
      box.classList.add("missing-img");
    }
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

        if (mainImg && src) {
          mainImg.style.display = "block";
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
        qtyInput.value = current + 1;
      });
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

      star.addEventListener("mouseenter", function () {
        highlight(value);
      });

      star.addEventListener("click", function () {
        selectedRating = value;

        if (ratingInput) {
          ratingInput.value = value;
        }

        highlight(value);
      });
    });

    highlight(selectedRating);
  });
</script>
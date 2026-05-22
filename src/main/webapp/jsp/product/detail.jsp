<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="defaultImg" value="${pageContext.request.contextPath}/assets/images/default-product.jpg"/>

<c:set var="mainImg" value="${defaultImg}"/>
<c:if test="${not empty product.imageUrl}">
  <c:choose>
    <c:when test="${fn:startsWith(product.imageUrl, 'http')}">
      <c:set var="mainImg" value="${product.imageUrl}"/>
    </c:when>
    <c:otherwise>
      <c:set var="mainImg" value="${pageContext.request.contextPath}${product.imageUrl}"/>
    </c:otherwise>
  </c:choose>
</c:if>

<section class="section">
  <div class="container">

    <div class="pd-layout">

      <!-- LEFT: PRODUCT IMAGES -->
      <div class="pd-gallery">

        <div class="pd-thumbs">
          <img class="thumb active"
               src="${mainImg}"
               data-src="${mainImg}"
               alt="${fn:escapeXml(product.title)}">

          <c:if test="${not empty product.images}">
            <c:forEach var="img" items="${product.images}">
              <c:if test="${not empty img.imageUrl}">
                <img class="thumb"
                     src="${pageContext.request.contextPath}${img.imageUrl}"
                     data-src="${pageContext.request.contextPath}${img.imageUrl}"
                     alt="gallery">
              </c:if>
            </c:forEach>
          </c:if>
        </div>

        <div class="pd-image-box">
          <img id="mainProductImage"
               src="${mainImg}"
               alt="${fn:escapeXml(product.title)}">
        </div>

      </div>

      <!-- RIGHT: PRODUCT INFO -->
      <div class="pd-info">

        <h1 class="product-title">
          <c:out value="${product.title}"/>
        </h1>

        <div class="rating">
          <c:forEach begin="1" end="5" var="i">
            <c:choose>
              <c:when test="${i <= product.avgRating}">★</c:when>
              <c:otherwise>☆</c:otherwise>
            </c:choose>
          </c:forEach>
          <span>(<c:out value="${product.reviewCount}"/> đánh giá)</span>
        </div>

        <div class="product-price">
          <c:choose>
            <c:when test="${product.finalPrice lt product.price}">
              <span class="old-price">
                <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/> ₫
              </span>

              <span class="sale-price">
                <fmt:formatNumber value="${product.finalPrice}" type="number" groupingUsed="true"/> ₫
              </span>

              <c:if test="${product.discountPercent > 0}">
                <div class="discount-info">Giảm ${product.discountPercent}%</div>
              </c:if>
            </c:when>

            <c:otherwise>
              <span class="sale-price">
                <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/> ₫
              </span>
            </c:otherwise>
          </c:choose>
        </div>

        <p class="product-desc">
          <c:out value="${product.description}"/>
        </p>

        <c:choose>
          <c:when test="${product.stock > 0}">
            <form method="post"
                  action="${pageContext.request.contextPath}/cart/add"
                  class="pd-cart-form">

              <input type="hidden" name="productId" value="${product.id}">

              <!-- VARIANT SELECTION -->
              <c:if test="${not empty variants}">
                <div class="variant-box">
                  <label for="variantId">Chọn biến thể</label>

                  <select id="variantId" name="variantId" class="variant-select" required>
                    <option value="">-- Chọn size / loại sản phẩm --</option>

                    <c:forEach var="v" items="${variants}">
                      <option value="${v.id}" ${v.stock <= 0 ? 'disabled' : ''}>
                        <c:out value="${v.displayName}"/>

                        <c:if test="${v.extraPrice > 0}">
                          - +<fmt:formatNumber value="${v.extraPrice}" type="number" groupingUsed="true"/> ₫
                        </c:if>

                        - Còn ${v.stock}
                      </option>
                    </c:forEach>
                  </select>
                </div>
              </c:if>

              <!-- VARIANT MESSAGES -->
              <c:if test="${param.variantRequired == '1'}">
                <div class="variant-message">
                  Vui lòng chọn size/loại trước khi thêm vào giỏ hàng.
                </div>
              </c:if>

              <c:if test="${param.variantInvalid == '1'}">
                <div class="variant-message">
                  Biến thể không hợp lệ hoặc đã ngừng bán.
                </div>
              </c:if>

              <c:if test="${param.variantOutOfStock == '1'}">
                <div class="variant-message">
                  Biến thể đã hết hàng.
                </div>
              </c:if>

              <button type="submit" class="pd-add-cart">
                Thêm vào giỏ hàng
              </button>

            </form>
          </c:when>

          <c:otherwise>
            <div class="out-of-stock">
              Sản phẩm hiện đã hết hàng
            </div>
          </c:otherwise>
        </c:choose>

      </div>
    </div>

    <!-- REVIEWS -->
    <section class="product-reviews">

      <h2 class="review-title">Đánh giá từ khách hàng</h2>

      <c:choose>
        <c:when test="${not empty sessionScope.user}">
          <form method="post"
                action="${pageContext.request.contextPath}/review"
                class="review-form">

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
                      required></textarea>

            <button type="submit" class="btn-review">
              Gửi đánh giá
            </button>
          </form>
        </c:when>

        <c:otherwise>
          <p class="review-note">
            Vui lòng
            <a href="${pageContext.request.contextPath}/login">đăng nhập</a>
            để đánh giá.
          </p>
        </c:otherwise>
      </c:choose>

      <c:choose>
        <c:when test="${not empty reviews}">
          <div class="review-list">
            <c:forEach var="r" items="${reviews}">
              <div class="review-item">

                <div class="review-header">
                  <strong><c:out value="${r.authorName}"/></strong>

                  <div class="review-stars">
                    <c:forEach begin="1" end="5" var="i">
                      <c:choose>
                        <c:when test="${i <= r.rating}">★</c:when>
                        <c:otherwise>☆</c:otherwise>
                      </c:choose>
                    </c:forEach>
                  </div>
                </div>

                <p class="review-comment">
                  <c:out value="${r.comment}"/>
                </p>

                <span class="review-date">
                  <fmt:formatDate value="${r.createdAtDate}" pattern="dd/MM/yyyy"/>
                </span>

              </div>
            </c:forEach>
          </div>
        </c:when>

        <c:otherwise>
          <p class="no-review">
            Chưa có đánh giá nào cho sản phẩm này.
          </p>
        </c:otherwise>
      </c:choose>

    </section>

  </div>
</section>

<script>
  document.addEventListener("DOMContentLoaded", function () {
    const mainImg = document.getElementById("mainProductImage");
    const thumbs = document.querySelectorAll(".pd-thumbs .thumb");

    thumbs.forEach(function (thumb) {
      thumb.addEventListener("click", function () {
        thumbs.forEach(function (item) {
          item.classList.remove("active");
        });

        thumb.classList.add("active");

        const src = thumb.getAttribute("data-src") || thumb.getAttribute("src");

        if (mainImg && src) {
          mainImg.src = src;
        }
      });
    });

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
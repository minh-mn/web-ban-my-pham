<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%-- ================= PREP MAIN IMAGE (đúng contextPath) ================= --%>
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

      <!-- ================= IMAGE / GALLERY ================= -->
      <div class="pd-gallery">

        <div class="pd-thumbs">
          <!-- Ảnh đại diện -->
          <img class="thumb active"
               src="${mainImg}"
               data-src="${mainImg}"
               alt="${fn:escapeXml(product.title)}">

          <!-- Ảnh con (gallery) nếu có -->
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

      <!-- ================= INFO ================= -->
      <div class="pd-info">

        <h1 class="product-title"><c:out value="${product.title}"/></h1>

        <!-- ⭐ Rating -->
        <div class="rating">
          <c:forEach begin="1" end="5" var="i">
            <c:choose>
              <c:when test="${i <= product.avgRating}">★</c:when>
              <c:otherwise>☆</c:otherwise>
            </c:choose>
          </c:forEach>
          <span>(<c:out value="${product.reviewCount}"/> đánh giá)</span>
        </div>

        <!-- ===== PRICE ===== -->
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

        <!-- DESCRIPTION -->
        <p class="product-desc"><c:out value="${product.description}"/></p>

        <!-- ADD TO CART -->
        <c:choose>
          <c:when test="${product.stock > 0}">
            <form method="post" action="${pageContext.request.contextPath}/cart/add">
              <input type="hidden" name="productId" value="${product.id}">
              <button class="pd-add-cart">Thêm vào giỏ hàng</button>
            </form>
          </c:when>
          <c:otherwise>
            <div class="out-of-stock">Sản phẩm hiện đã hết hàng</div>
          </c:otherwise>
        </c:choose>

      </div>
    </div>

    <!-- ================= REVIEWS ================= -->
    <section class="product-reviews">

      <h2 class="review-title">Đánh giá từ khách hàng</h2>

      <!-- ===== REVIEW FORM ===== -->
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
                      placeholder="Chia sẻ cảm nhận của bạn về sản phẩm..." required></textarea>

            <button type="submit" class="btn-review">Gửi đánh giá</button>
          </form>
        </c:when>

        <c:otherwise>
          <p class="review-note">
            ⚠️ Vui lòng <a href="${pageContext.request.contextPath}/login">đăng nhập</a> để đánh giá.
          </p>
        </c:otherwise>
      </c:choose>

      <!-- ===== REVIEW LIST ===== -->
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

                <p class="review-comment"><c:out value="${r.comment}"/></p>

                <span class="review-date">
                  <fmt:formatDate value="${r.createdAtDate}" pattern="dd/MM/yyyy"/>
                </span>

              </div>
            </c:forEach>
          </div>
        </c:when>

        <c:otherwise>
          <p class="no-review">Chưa có đánh giá nào cho sản phẩm này.</p>
        </c:otherwise>
      </c:choose>

    </section>

  </div>
</section>

<!-- ================= JS: đổi ảnh khi click thumbnail + rating ================= -->
<script>
document.addEventListener("DOMContentLoaded", function () {

  // ===== Gallery switch =====
  const mainImg = document.getElementById("mainProductImage");
  const thumbs = document.querySelectorAll(".pd-thumbs .thumb");

  thumbs.forEach(t => {
    t.addEventListener("click", () => {
      thumbs.forEach(x => x.classList.remove("active"));
      t.classList.add("active");

      const src = t.getAttribute("data-src") || t.getAttribute("src");
      if (mainImg && src) mainImg.src = src;
    });
  });

  // ===== Rating UI =====
  const stars = document.querySelectorAll(".star-input .star");
  const ratingInput = document.getElementById("ratingInput");
  let selectedRating = 5;

  function highlight(rating) {
    stars.forEach(s => {
      s.classList.toggle("active", Number(s.dataset.value) <= rating);
    });
  }

  stars.forEach(star => {
    const value = Number(star.dataset.value);

    star.addEventListener("mouseenter", () => highlight(value));
    star.addEventListener("click", () => {
      selectedRating = value;
      ratingInput.value = value;
      highlight(value);
    });
  });

  highlight(selectedRating);
});
</script>

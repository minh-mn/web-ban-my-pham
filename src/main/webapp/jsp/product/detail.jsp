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

<link rel="stylesheet" href="${ctx}/assets/css/product-detail.css" />

<c:if test="${empty p}">
  <section class="pd-page">
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
                  <button type="button" class="pd-thumb is-active" data-image="${fn:escapeXml(mainImage)}">
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
                    <button type="button" class="pd-thumb" data-image="${fn:escapeXml(img.image)}">
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
            <div class="pd-info-head">
              <div class="pd-badges">
                <span class="pd-badge pd-badge-brand"><c:out value="${empty p.brandName ? 'MyCosmetic' : p.brandName}" /></span>
                <span class="pd-badge pd-badge-category"><c:out value="${empty p.categoryName ? 'Chính hãng' : p.categoryName}" /></span>
                <span class="pd-badge pd-badge-stock">Còn <c:out value="${p.stock}" /></span>
              </div>

              <h1 class="pd-title"><c:out value="${p.title}" /></h1>

              <div class="pd-rating-row">
                <span class="pd-stars">★★★★★</span>
                <span class="pd-rating-score">
                                <fmt:formatNumber value="${empty p.avgRating ? 5 : p.avgRating}" minFractionDigits="1" maxFractionDigits="1" />
                            </span>
                <span class="pd-rating-count">(<c:out value="${empty p.reviewCount ? 0 : p.reviewCount}" /> đánh giá)</span>
              </div>
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
              <div class="pd-vat-note">* Giá đã bao gồm VAT</div>
            </div>

            <div class="pd-short-desc">
              <h2><c:out value="${p.title}" /></h2>
              <p>
                <c:choose>
                  <c:when test="${not empty p.description}">
                    <c:out value="${fn:substring(p.description, 0, fn:length(p.description) > 260 ? 260 : fn:length(p.description))}" />
                    <c:if test="${fn:length(p.description) > 260}">...</c:if>
                  </c:when>
                  <c:otherwise>Sản phẩm chính hãng được chọn lọc tại MyCosmetic, hỗ trợ mua nhanh và giao hàng tiện lợi.</c:otherwise>
                </c:choose>
              </p>
            </div>

            <form class="pd-cart-form" action="${ctx}/cart/add" method="post">
              <input type="hidden" name="action" value="add" />
              <input type="hidden" name="quickAdd" value="1" />
              <input type="hidden" name="productId" value="${p.id}" />
              <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />

              <c:if test="${not empty variants}">
                <div class="pd-variants-wrapper" style="margin: 15px 0 20px 0;">
                  <span class="pd-variant-label" style="font-weight: 600; display: block; margin-bottom: 10px; color: #334155; font-size: 0.9rem;">
                    Chọn phân loại:
                  </span>

                  <div class="pd-variants-list" style="display: flex; flex-wrap: wrap; gap: 10px;">
                    <c:forEach var="v" items="${variants}" varStatus="st">
                      <label class="pd-variant-item" style="cursor: pointer; position: relative;">
                        <input type="radio"
                               name="variantId"
                               value="${v.id}"
                               class="variant-option"
                               data-extra-price="${empty v.extraPrice ? 0 : v.extraPrice}"
                               onchange="updatePrice()"
                          ${st.first ? 'checked' : ''} />

                        <div class="pd-variant-box">
                          <c:out value="${v.size}" />
                          <c:if test="${not empty v.size && not empty v.type}"> - </c:if>
                          <c:out value="${v.type}" />

                            <%-- Nếu có giá cộng thêm thì hiển thị kèm theo --%>
                          <c:if test="${not empty v.extraPrice && v.extraPrice > 0}">
                            <span style="font-size: 0.8rem; margin-left: 4px; opacity: 0.8;">
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
                <label class="pd-qty-label" for="pdQuantity">Số lượng:</label>
                <div class="pd-qty-control">
                  <button type="button" class="pd-qty-btn" data-qty-action="minus">−</button>
                  <input id="pdQuantity" name="quantity" type="number" min="1" max="${p.stock > 0 ? p.stock : 1}" value="1" />
                  <button type="button" class="pd-qty-btn" data-qty-action="plus">+</button>
                </div>

                <button type="submit" class="pd-add-cart-btn" <c:if test="${p.stock <= 0}">disabled</c:if>>
                  <c:choose>
                    <c:when test="${p.stock <= 0}">Hết hàng</c:when>
                    <c:otherwise>Thêm vào giỏ hàng</c:otherwise>
                  </c:choose>
                </button>
              </div>
            </form>
          </article>
        </div>
      </div>

      <section class="pd-section pd-detail-section">
        <div class="pd-section-head">
          <span></span>
          <h2>Chi tiết sản phẩm</h2>
        </div>
        <div class="pd-desc-content">
          <c:choose>
            <c:when test="${not empty p.description}">
              <p><c:out value="${p.description}" /></p>
            </c:when>
            <c:otherwise>
              <p>Thông tin chi tiết đang được cập nhật. Bạn có thể liên hệ MyCosmetic để được tư vấn thêm về sản phẩm này.</p>
            </c:otherwise>
          </c:choose>
        </div>
      </section>

      <c:set var="comboProducts" value="${not empty boughtTogetherProducts ? boughtTogetherProducts : frequentlyBoughtProducts}" />
      <c:if test="${not empty comboProducts}">
        <section class="pd-section">
          <div class="pd-section-head">
            <span></span>
            <div>
              <h2>Thường được mua kèm</h2>
              <p>Gợi ý các sản phẩm phù hợp để hoàn thiện routine chăm sóc da.</p>
            </div>
          </div>

          <div class="pd-product-grid pd-product-grid-4">
            <c:forEach var="item" items="${comboProducts}" begin="0" end="3">
              <c:set var="itemPrice" value="${not empty item.finalPrice ? item.finalPrice : item.price}" />
              <c:set var="itemImg" value="${item.image}" />
              <article class="pd-mini-card">
                <a class="pd-mini-img" href="${ctx}/product/${item.slug}?id=${item.id}">
                  <c:choose>
                    <c:when test="${empty itemImg}"><div class="pd-mini-placeholder">MyCosmetic</div></c:when>
                    <c:when test="${fn:startsWith(itemImg, 'http://') || fn:startsWith(itemImg, 'https://')}"><img src="${itemImg}" alt="${fn:escapeXml(item.title)}" /></c:when>
                    <c:when test="${fn:startsWith(itemImg, '/')}"><img src="${ctx}${itemImg}" alt="${fn:escapeXml(item.title)}" /></c:when>
                    <c:otherwise><img src="${ctx}/${itemImg}" alt="${fn:escapeXml(item.title)}" /></c:otherwise>
                  </c:choose>
                </a>
                <div class="pd-mini-body">
                  <div class="pd-mini-brand"><c:out value="${empty item.brandName ? 'MyCosmetic' : item.brandName}" /></div>
                  <a class="pd-mini-title" href="${ctx}/product/${item.slug}?id=${item.id}"><c:out value="${item.title}" /></a>
                  <div class="pd-mini-price"><fmt:formatNumber value="${itemPrice}" type="number" groupingUsed="true" />đ</div>
                  <a class="pd-mini-link" href="${ctx}/product/${item.slug}?id=${item.id}">Xem chi tiết</a>
                </div>
              </article>
            </c:forEach>
          </div>
        </section>
      </c:if>

      <c:if test="${not empty relatedProducts}">
        <section class="pd-section">
          <div class="pd-section-head">
            <span></span>
            <div>
              <h2>Sản phẩm liên quan</h2>
              <p>Các sản phẩm cùng nhóm công dụng, dễ so sánh và lựa chọn hơn.</p>
            </div>
          </div>

          <div class="pd-product-grid pd-product-grid-4">
            <c:forEach var="item" items="${relatedProducts}" begin="0" end="7">
              <c:set var="itemPrice" value="${not empty item.finalPrice ? item.finalPrice : item.price}" />
              <c:set var="itemImg" value="${item.image}" />
              <article class="pd-mini-card">
                <a class="pd-mini-img" href="${ctx}/product/${item.slug}?id=${item.id}">
                  <c:choose>
                    <c:when test="${empty itemImg}"><div class="pd-mini-placeholder">MyCosmetic</div></c:when>
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
                    <span><c:out value="${empty item.reviewCount ? 0 : item.reviewCount}" /> đánh giá</span>
                  </div>
                  <div class="pd-mini-price"><fmt:formatNumber value="${itemPrice}" type="number" groupingUsed="true" />đ</div>
                  <a class="pd-mini-link" href="${ctx}/product/${item.slug}?id=${item.id}">Xem chi tiết</a>
                </div>
              </article>
            </c:forEach>
          </div>
        </section>

        <section class="pd-section pd-reviews-section">
          <div class="pd-section-head">
            <span></span>
            <div>
              <h2>Đánh giá từ khách hàng</h2>
              <p>Tổng số <c:out value="${empty p.reviewCount ? 0 : p.reviewCount}" /> lượt đánh giá dành cho sản phẩm này.</p>
            </div>
          </div>

          <div class="pd-reviews-container" style="margin-top: 25px;">
            <c:choose>
              <%-- Trường hợp chưa có bình luận nào --%>
              <c:when test="${empty reviews}">
                <div class="pd-no-reviews" style="padding: 40px; text-align: center; color: #777; background: #f9f9f9; border-radius: 8px;">
                  <p>Chưa có đánh giá nào cho sản phẩm này.</p>
                </div>
              </c:when>

              <%-- Trường hợp có bình luận --%>
              <c:otherwise>
                <div class="pd-reviews-list" style="display: flex; flex-direction: column; gap: 20px;">
                  <c:forEach var="rev" items="${reviews}">

                    <div class="pd-review-item" style="display: flex; gap: 15px; padding: 20px; border: 1px solid #f0f0f0; border-radius: 8px; background: #fff; box-shadow: 0 2px 4px rgba(0,0,0,0.02);">

                        <%-- 1. KHỐI ẢNH ĐẠI DIỆN (AVATAR TỰ ĐỘNG) --%>
                      <div class="pd-review-avatar" style="flex-shrink: 0;">
                        <div style="width: 45px; height: 45px; background-color: #e2e8f0; color: #4a5568; display: flex; align-items: center; justify-content: center; font-weight: bold; border-radius: 50%; font-size: 1.2rem; text-transform: uppercase; border: 1px solid #cbd5e1;">
                          <c:out value="${fn:substring(not empty rev.authorName ? rev.authorName : 'K', 0, 1)}" />
                        </div>
                      </div>

                        <%-- KHỐI NỘI DUNG ĐÁNH GIÁ --%>
                      <div class="pd-review-content" style="flex-grow: 1;">

                          <%-- Tên người dùng & Nhãn Xác thực mua hàng --%>
                        <div class="pd-review-header" style="display: flex; align-items: center; gap: 10px; margin-bottom: 4px; flex-wrap: wrap;">
                        <span class="pd-review-author" style="font-weight: 600; color: #1e293b; font-size: 1rem;">
                          <c:out value="${not empty rev.authorFullName ? rev.authorFullName : (not empty rev.authorName ? rev.authorName : 'Khách hàng ẩn danh')}" />
                        </span>

                            <%-- Thẻ Đã mua hàng (Nếu có orderId tức là đã mua sản phẩm) --%>
                          <c:if test="${not empty rev.orderId && rev.orderId > 0}">
                          <span class="pd-verified-badge" style="font-size: 0.75rem; color: #16a34a; background: #f0fdf4; padding: 2px 8px; border-radius: 4px; display: inline-flex; align-items: center; gap: 4px; font-weight: 500; border: 1px solid #bbf7d0;">
                            ✓ Đã mua hàng
                          </span>
                          </c:if>
                        </div>

                          <%-- Số sao & Ngày tháng bình luận --%>
                        <div class="pd-review-meta" style="display: flex; align-items: center; gap: 15px; margin-bottom: 6px; font-size: 0.85rem;">
                        <span class="pd-review-stars" style="color: #ffb400; font-size: 0.95rem;">
                          <c:forEach begin="1" end="${rev.rating}">★</c:forEach>
                          <c:forEach begin="${rev.rating + 1}" end="5">☆</c:forEach>
                        </span>
                          <span class="pd-review-date" style="color: #94a3b8;">
                          <fmt:formatDate value="${rev.createdAtDate}" pattern="dd/MM/yyyy HH:mm" />
                        </span>
                        </div>

                          <%-- 2. THÔNG TIN SẢN PHẨM ĐÃ MUA --%>
                        <div class="pd-purchased-product" style="font-size: 0.85rem; color: #64748b; margin-bottom: 10px; background: #f8fafc; padding: 6px 10px; border-radius: 4px; display: inline-block;">
                          <span style="font-weight: 500;">Sản phẩm:</span>
                          <c:out value="${not empty rev.productName ? rev.productName : p.title}" />
                        </div>

                          <%-- Nội dung văn bản bình luận --%>
                        <div class="pd-review-body" style="color: #334155; line-height: 1.6; font-size: 0.95rem; word-break: break-word;">
                          <c:out value="${rev.comment}" />
                        </div>

                          <%-- 3. HÌNH ẢNH ĐÍNH KÈM THỰC TẾ (NẾU CÓ) --%>
                        <c:if test="${rev.hasImage && not empty rev.imageUrl}">
                          <div class="pd-review-images" style="margin-top: 12px;">
                            <img src="${ctx}/${rev.imageUrl}" alt="Ảnh thực tế từ khách hàng" style="max-width: 100px; max-height: 100px; border-radius: 6px; object-fit: cover; border: 1px solid #e2e8f0; cursor: pointer;" />
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

      </c:if>
    </div>
  </section>

  <script>
    (function () {
      const ctx = '${ctx}';
      const mainImage = document.getElementById('pdMainImage');
      const thumbs = document.querySelectorAll('.pd-thumb');

      function normalizeImageUrl(raw) {
        if (!raw) return '';
        if (raw.startsWith('http://') || raw.startsWith('https://')) return raw;
        if (raw.startsWith('/')) return ctx + raw;
        if (raw.startsWith('assets/') || raw.startsWith('uploads/')) return ctx + '/' + raw;
        return ctx + '/uploads/product/gallery/' + raw;
      }

      thumbs.forEach(function (btn) {
        btn.addEventListener('click', function () {
          const raw = btn.getAttribute('data-image');
          if (mainImage && raw) {
            mainImage.src = normalizeImageUrl(raw);
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
    })();

    function updatePrice() {
      // 1. Lấy giá gốc từ thẻ hiển thị giá
      const priceDisplay = document.getElementById('price-display');
      const basePrice = parseFloat(priceDisplay.getAttribute('data-base-price'));

      // 2. Lấy biến thể đang được chọn
      const selectedVariant = document.querySelector('input[name="variantId"]:checked');

      // 3. Lấy giá trị cộng thêm (nếu có)
      const extraPrice = selectedVariant ? parseFloat(selectedVariant.getAttribute('data-extra-price')) : 0;

      // 4. Tính tổng
      const finalPrice = basePrice + extraPrice;

      // 5. Cập nhật lại giao diện
      // Sử dụng Intl.NumberFormat để định dạng lại dấu chấm ngăn cách hàng nghìn
      const formatter = new Intl.NumberFormat('vi-VN');
      priceDisplay.textContent = formatter.format(finalPrice) + "đ";
    }

    // Chạy hàm 1 lần khi load trang để hiển thị đúng giá của biến thể mặc định
    window.onload = function() {
      updatePrice();
    };
  </script>
</c:if>

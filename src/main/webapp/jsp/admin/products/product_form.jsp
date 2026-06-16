<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Product" scope="request"/>
<c:set var="activeMenu" value="products" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEditProduct" value="${not empty product}" />

<main class="admin-main">
  <div class="admin-container admin-product-form-page">

    <section class="admin-product-form-hero">
      <div class="admin-product-form-hero__content">
        <span class="admin-product-form-eyebrow">SẢN PHẨM &amp; DANH MỤC</span>
        <h1 class="admin-product-form-title">
          <c:choose>
            <c:when test="${empty product}">Thêm sản phẩm</c:when>
            <c:otherwise>Sửa sản phẩm #${product.id}</c:otherwise>
          </c:choose>
        </h1>
        <p class="admin-product-form-subtitle">
          Nhập thông tin sản phẩm, quản lý giá bán, tồn kho, biến thể, ảnh đại diện,
          gallery và media chi tiết. File upload được lưu trong
          <b>MyCosmeticShopUploads</b>, database lưu đường dẫn dạng <b>/uploads/product/...</b>.
        </p>
      </div>

      <div class="admin-product-form-hero__actions">
        <a class="admin-btn"
           href="${ctx}/admin/products">
          ← Quay lại danh sách
        </a>
      </div>
    </section>

    <c:if test="${not empty error}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${error}"/>
      </div>
    </c:if>

    <c:if test="${not empty success}">
      <div class="admin-alert admin-alert--success">
        <c:out value="${success}"/>
      </div>
    </c:if>

    <form method="post"
          action="${ctx}/admin/products"
          enctype="multipart/form-data"
          class="admin-form admin-product-form">
      <%@ include file="/jsp/common/csrf.jspf" %>

      <input type="hidden" name="action" value="${empty product ? 'create' : 'update'}"/>

      <c:if test="${not empty product}">
        <input type="hidden" name="id" value="${product.id}"/>
        <input type="hidden" name="existingImage" value="${product.image}"/>
      </c:if>

      <div class="admin-product-form-layout">

        <section class="admin-card admin-product-form-card">
          <div class="admin-card__body">
            <div class="admin-product-form-section-head">
              <div>
                <h2 class="admin-product-form-section-title">Thông tin cơ bản</h2>
                <p class="admin-product-form-section-desc">
                  Cấu hình tên, slug, danh mục, thương hiệu và trạng thái hiển thị.
                </p>
              </div>

              <c:choose>
                <c:when test="${empty product || product.active}">
                  <span class="admin-chip admin-chip--success">Đang hiển thị</span>
                </c:when>
                <c:otherwise>
                  <span class="admin-chip admin-chip--warning">Tạm ẩn</span>
                </c:otherwise>
              </c:choose>
            </div>

            <div class="admin-product-form-grid">
              <div class="admin-field admin-product-form-field--full">
                <label class="admin-label" for="productTitle">
                  Tên sản phẩm <span class="admin-required">*</span>
                </label>

                <input id="productTitle"
                       class="admin-input"
                       type="text"
                       name="title"
                       value="${not empty product ? fn:escapeXml(product.title) : ''}"
                       placeholder="Ví dụ: Kem dưỡng ẩm..."
                       maxlength="255"
                       required />
              </div>

              <div class="admin-field">
                <label class="admin-label" for="productSlug">
                  Slug <span class="admin-required">*</span>
                </label>

                <input id="productSlug"
                       class="admin-input"
                       type="text"
                       name="slug"
                       value="${not empty product ? fn:escapeXml(product.slug) : ''}"
                       placeholder="vi-du-kem-duong-am"
                       maxlength="255"
                       required />

                <div class="admin-help">
                  Không dấu, dùng dấu gạch ngang. Ví dụ: <b>kem-duong-am-cocoon</b>.
                </div>
              </div>

              <div class="admin-field admin-product-status-control">
                <label class="admin-label" for="productActive">
                  Trạng thái hiển thị
                </label>

                <div class="admin-product-status-select-wrap">
                  <select id="productActive" class="admin-select" name="active">
                    <option value="1" ${empty product || product.active ? "selected" : ""}>
                      Đang hiển thị
                    </option>
                    <option value="0" ${not empty product && !product.active ? "selected" : ""}>
                      Tạm ẩn
                    </option>
                  </select>

                  <c:choose>
                    <c:when test="${empty product || product.active}">
                      <span class="admin-product-status-badge admin-product-status-badge--active">
                        Đang bán
                      </span>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-product-status-badge admin-product-status-badge--hidden">
                        Đang ẩn
                      </span>
                    </c:otherwise>
                  </c:choose>
                </div>

                <div class="admin-product-status-note">
                  <strong>Đồng bộ với danh sách sản phẩm:</strong>
                  chọn <b>Tạm ẩn</b> để ẩn khỏi trang người dùng, chọn <b>Đang hiển thị</b> để mở khóa lại.
                  Thao tác này chỉ cập nhật trạng thái, không xóa ảnh, media, đánh giá hoặc biến thể.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="productCategory">
                  Danh mục <span class="admin-required">*</span>
                </label>

                <select id="productCategory" class="admin-select" name="categoryId" required>
                  <option value="">-- Chọn danh mục --</option>

                  <c:forEach var="cat" items="${categories}">
                    <option value="${cat.id}"
                            <c:if test="${not empty product && not empty product.category && product.category.id == cat.id}">
                              selected
                            </c:if>>
                      <c:if test="${cat.parentId != null}">↳ </c:if>
                      <c:out value="${cat.name}"/>
                      <c:if test="${cat.parentId == null}"> (Cha)</c:if>
                    </option>
                  </c:forEach>
                </select>

                <div class="admin-help">
                  Chọn danh mục cha hoặc danh mục con.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="productBrand">
                  Thương hiệu <span class="admin-required">*</span>
                </label>

                <select id="productBrand" class="admin-select" name="brandId" required>
                  <option value="">-- Chọn thương hiệu --</option>

                  <c:forEach var="b" items="${brands}">
                    <option value="${b.id}"
                            <c:if test="${not empty product && not empty product.brand && product.brand.id == b.id}">
                              selected
                            </c:if>>
                      <c:out value="${b.name}"/>
                    </option>
                  </c:forEach>
                </select>

                <div class="admin-help">
                  Chọn thương hiệu của sản phẩm.
                </div>
              </div>
            </div>
          </div>
        </section>

        <aside class="admin-card admin-product-guide-card">
          <div class="admin-card__body">
            <div class="admin-product-form-section-head">
              <div>
                <h2 class="admin-product-form-section-title">Tóm tắt cấu hình</h2>
                <p class="admin-product-form-section-desc">
                  Các nhóm thông tin cần hoàn tất trước khi lưu sản phẩm.
                </p>
              </div>
            </div>

            <div class="admin-product-guide-list">
              <div class="admin-product-guide-item">
                <span>1</span>
                <div>
                  <strong>Thông tin cơ bản</strong>
                  <small>Tên, slug, danh mục, thương hiệu và trạng thái.</small>
                </div>
              </div>

              <div class="admin-product-guide-item">
                <span>2</span>
                <div>
                  <strong>Giá bán &amp; tồn kho</strong>
                  <small>Giá gốc, giảm giá, tồn kho tổng và biến thể.</small>
                </div>
              </div>

              <div class="admin-product-guide-item">
                <span>3</span>
                <div>
                  <strong>Hình ảnh &amp; media</strong>
                  <small>Ảnh đại diện, gallery và ảnh/video chi tiết.</small>
                </div>
              </div>
            </div>

            <div class="admin-product-guide-note admin-product-guide-note--status">
              <strong>Lưu ý trạng thái</strong>
              <span>
                Sản phẩm tạm ẩn vẫn có thể sửa, nhập kho, quản lý ảnh và mở khóa lại từ danh sách hoặc form.
              </span>
            </div>
          </div>
        </aside>
      </div>

      <section class="admin-card admin-product-price-card">
        <div class="admin-card__body">
          <div class="admin-product-form-section-head">
            <div>
              <h2 class="admin-product-form-section-title">Giá bán và tồn kho</h2>
              <p class="admin-product-form-section-desc">
                Quản lý giá gốc, giảm giá phần trăm và tồn kho tổng của sản phẩm.
              </p>
            </div>
          </div>

          <div class="admin-product-price-grid">
            <div class="admin-field">
              <label class="admin-label" for="productPrice">
                Giá gốc (VND) <span class="admin-required">*</span>
              </label>

              <input id="productPrice"
                     class="admin-input"
                     type="number"
                     name="price"
                     value="${not empty product && not empty product.price ? product.price : ''}"
                     min="0"
                     step="1"
                     required />
            </div>

            <div class="admin-field">
              <label class="admin-label" for="productDiscount">
                Giảm giá (%)
              </label>

              <input id="productDiscount"
                     class="admin-input"
                     type="number"
                     name="discountPercent"
                     value="${not empty product ? product.discountPercent : 0}"
                     min="0"
                     max="100"
                     step="1" />

              <div class="admin-help">
                Nhập từ <b>0</b> đến <b>100</b>.
              </div>
            </div>

            <div class="admin-field">
              <label class="admin-label" for="productStock">
                Tồn kho <span class="admin-required">*</span>
              </label>

              <input id="productStock"
                     class="admin-input"
                     type="number"
                     name="stock"
                     value="${not empty product ? product.stock : 0}"
                     min="0"
                     step="1"
                     required />

              <div class="admin-help">
                Nếu sản phẩm có variant, tồn kho tổng nên đồng bộ với tổng tồn kho các variant.
              </div>
            </div>

            <div class="admin-product-rule-box">
              <span class="admin-product-rule-box__icon">📌</span>
              <div>
                <strong>Quy tắc hiển thị</strong>
                <small>Sản phẩm đang hiển thị và còn hàng sẽ được đưa ra trang người dùng.</small>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="admin-card admin-product-variant-card">
        <div class="admin-card__body">
          <div class="admin-product-form-section-head">
            <div>
              <h2 class="admin-product-form-section-title">Quản lý biến thể sản phẩm</h2>
              <p class="admin-product-form-section-desc">
                Nhập size, loại/màu, giá cộng thêm và tồn kho riêng cho từng biến thể.
              </p>
            </div>

            <button type="button"
                    class="admin-btn admin-product-add-variant-btn"
                    onclick="addRow()">
              + Thêm biến thể
            </button>
          </div>

          <div class="admin-product-variant-table-wrap">
            <table class="admin-table admin-product-variant-table" id="variantTable">
              <thead>
              <tr>
                <th>Kích thước (Size)</th>
                <th>Loại (Type)</th>
                <th>Giá cộng thêm (VNĐ)</th>
                <th>Tồn kho</th>
                <th class="admin-product-variant-action-col">Hành động</th>
              </tr>
              </thead>
              <tbody>
              <c:choose>
                <c:when test="${not empty variants}">
                  <c:forEach var="v" items="${variants}">
                    <tr class="admin-product-variant-row">
                      <td>
                        <input type="text"
                               name="v_size[]"
                               class="admin-input"
                               value="${fn:escapeXml(v.size)}"
                               placeholder="VD: XL">
                      </td>
                      <td>
                        <input type="text"
                               name="v_type[]"
                               class="admin-input"
                               value="${fn:escapeXml(v.type)}"
                               placeholder="VD: Đỏ">
                      </td>
                      <td>
                        <input type="number"
                               name="v_price[]"
                               class="admin-input"
                               value="${v.extraPrice}"
                               min="0">
                      </td>
                      <td>
                        <input type="number"
                               name="v_stock[]"
                               class="admin-input"
                               value="${v.stock}"
                               min="0">
                      </td>
                      <td class="admin-product-variant-action-cell">
                        <button type="button"
                                class="admin-btn admin-btn--danger admin-product-variant-remove"
                                onclick="removeRow(this)">
                          Xóa
                        </button>
                      </td>
                    </tr>
                  </c:forEach>
                </c:when>
                <c:otherwise>
                  <tr class="admin-product-variant-row">
                    <td><input type="text" name="v_size[]" class="admin-input" placeholder="VD: XL"></td>
                    <td><input type="text" name="v_type[]" class="admin-input" placeholder="VD: Đỏ"></td>
                    <td><input type="number" name="v_price[]" class="admin-input" value="0" min="0"></td>
                    <td><input type="number" name="v_stock[]" class="admin-input" value="0" min="0"></td>
                    <td class="admin-product-variant-action-cell">
                      <button type="button"
                              class="admin-btn admin-btn--danger admin-product-variant-remove"
                              onclick="removeRow(this)">
                        Xóa
                      </button>
                    </td>
                  </tr>
                </c:otherwise>
              </c:choose>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section class="admin-card admin-product-description-card">
        <div class="admin-card__body">
          <div class="admin-product-form-section-head">
            <div>
              <h2 class="admin-product-form-section-title">Mô tả chi tiết sản phẩm</h2>
              <p class="admin-product-form-section-desc">
                Có thể dùng HTML cơ bản để trình bày nội dung sản phẩm.
              </p>
            </div>
          </div>

          <div class="admin-field">
            <label class="admin-label" for="productDescription">
              Mô tả
            </label>

            <div class="admin-product-description-tools">
              <button type="button" class="admin-product-description-tool" data-insert="<h3>Tiêu đề</h3>">Tiêu đề</button>
              <button type="button" class="admin-product-description-tool" data-insert="<p>Nội dung đoạn văn...</p>">Đoạn văn</button>
              <button type="button" class="admin-product-description-tool" data-insert="<strong>Chữ đậm</strong>">Đậm</button>
              <button type="button" class="admin-product-description-tool" data-insert="<ul>&#10;  <li>Ý 1</li>&#10;  <li>Ý 2</li>&#10;</ul>">Danh sách</button>
              <button type="button" class="admin-product-description-tool" data-insert="<blockquote>Ghi chú nổi bật...</blockquote>">Ghi chú</button>
              <button type="button" class="admin-product-description-tool" data-insert="<img src=&quot;/uploads/product/media/ten-file.jpg&quot; alt=&quot;Mô tả ảnh&quot;>">Ảnh trong mô tả</button>
              <button type="button" class="admin-product-description-tool" data-insert="<video controls src=&quot;/uploads/product/media/ten-file.mp4&quot;></video>">Video trong mô tả</button>
            </div>

            <textarea id="productDescription"
                      class="admin-textarea admin-product-description-editor"
                      name="description"
                      rows="12"
                      placeholder="Nhập mô tả chi tiết. Có thể dùng HTML cơ bản như <h3>, <p>, <ul>, <strong>, <img>, <video>...">${not empty product ? fn:escapeXml(product.description) : ''}</textarea>

            <div class="admin-help">
              Có thể nhập mô tả dài hơn bằng HTML cơ bản:
              <b>&lt;h3&gt;</b>, <b>&lt;p&gt;</b>, <b>&lt;ul&gt;</b>,
              <b>&lt;strong&gt;</b>, <b>&lt;img&gt;</b>, <b>&lt;video&gt;</b>.
            </div>
          </div>
        </div>
      </section>

      <section class="admin-card admin-product-image-card">
        <div class="admin-card__body">
          <div class="admin-product-form-section-head">
            <div>
              <h2 class="admin-product-form-section-title">Ảnh sản phẩm</h2>
              <p class="admin-product-form-section-desc">
                Quản lý ảnh đại diện và gallery mô tả sản phẩm.
              </p>
            </div>
          </div>

          <div class="admin-product-upload-grid">
            <div class="admin-product-upload-box">
              <label class="admin-label" for="imageMain">
                Ảnh đại diện
              </label>

              <input id="imageMain"
                     class="admin-input"
                     type="file"
                     name="imageMain"
                     accept=".jpg,.jpeg,.png,.webp,.gif,image/jpeg,image/png,image/webp,image/gif">

              <div class="admin-help">
                Ảnh upload sẽ lưu tại <b>MyCosmeticShopUploads/product/</b>.
                Nếu sửa sản phẩm mà không chọn ảnh mới, hệ thống giữ ảnh cũ.
              </div>
            </div>

            <div class="admin-product-upload-box">
              <label class="admin-label" for="imageGallery">
                Ảnh mô tả / Gallery
              </label>

              <input id="imageGallery"
                     class="admin-input"
                     type="file"
                     name="imageGallery"
                     accept=".jpg,.jpeg,.png,.webp,.gif,image/jpeg,image/png,image/webp,image/gif"
                     multiple>

              <div class="admin-help">
                Ảnh gallery sẽ lưu tại <b>MyCosmeticShopUploads/product/gallery/</b>.
                Khi sửa sản phẩm, ảnh mới sẽ được <b>thêm vào gallery hiện có</b>.
              </div>
            </div>
          </div>

          <c:if test="${not empty product && not empty product.image}">
            <div class="admin-product-current-main">
              <div class="admin-product-current-main__preview">
                <img src="${ctx}${product.image}"
                     alt="${not empty product.title ? product.title : 'product'}">
              </div>

              <div class="admin-product-current-main__body">
                <strong>Ảnh đại diện hiện tại</strong>
                <code><c:out value="${product.image}"/></code>

                <c:choose>
                  <c:when test="${fn:startsWith(product.image, '/uploads/product/')}">
                    <span class="admin-pill admin-pill--ok">Đúng chuẩn upload</span>
                  </c:when>
                  <c:otherwise>
                    <span class="admin-pill admin-pill--warning">
                      Đường dẫn cũ, upload ảnh mới để chuyển sang /uploads/product/
                    </span>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
          </c:if>

          <c:set var="displayGallery" value="${not empty product ? product.images : null}" />

          <c:if test="${empty displayGallery && not empty productImages}">
            <c:set var="displayGallery" value="${productImages}" />
          </c:if>

          <c:if test="${empty displayGallery && not empty galleryImages}">
            <c:set var="displayGallery" value="${galleryImages}" />
          </c:if>

          <c:if test="${not empty displayGallery}">
            <div class="admin-product-gallery-current">
              <div class="admin-product-subsection-head">
                <strong>Gallery hiện tại</strong>
                <span>Tick ảnh cần xóa, sau đó bấm Lưu thay đổi.</span>
              </div>

              <div class="admin-product-gallery-grid">
                <c:forEach var="img" items="${displayGallery}">
                  <div class="admin-product-gallery-item">
                    <img src="${ctx}${img.image}"
                         alt="gallery">

                    <code class="admin-product-gallery-path">
                      <c:out value="${img.image}"/>
                    </code>

                    <c:choose>
                      <c:when test="${fn:startsWith(img.image, '/uploads/product/gallery/')}">
                        <span class="admin-pill admin-pill--ok">Đúng chuẩn upload</span>
                      </c:when>
                      <c:otherwise>
                        <span class="admin-pill admin-pill--warning">Path cũ</span>
                      </c:otherwise>
                    </c:choose>

                    <c:if test="${not empty product && not empty img.id}">
                      <label class="admin-product-delete-check">
                        <input type="checkbox"
                               name="deleteImageIds"
                               value="${img.id}">
                        Xóa ảnh này khi lưu
                      </label>
                    </c:if>
                  </div>
                </c:forEach>
              </div>

              <div class="admin-help admin-product-section-note">
                Hệ thống sẽ xóa cả dữ liệu SQL và file trong
                <b>MyCosmeticShopUploads/product/gallery</b>.
              </div>
            </div>
          </c:if>
        </div>
      </section>

      <section class="admin-card admin-product-media-card">
        <div class="admin-card__body">
          <div class="admin-product-form-section-head">
            <div>
              <h2 class="admin-product-form-section-title">Media chi tiết sản phẩm</h2>
              <p class="admin-product-form-section-desc">
                Upload nhiều ảnh hoặc video để hiển thị ở trang chi tiết sản phẩm.
              </p>
            </div>
          </div>

          <div class="admin-product-media-upload-box">
            <label class="admin-label" for="productMedia">
              Thêm nhiều ảnh/video
            </label>

            <input id="productMedia"
                   class="admin-input"
                   type="file"
                   name="productMedia"
                   accept=".jpg,.jpeg,.png,.webp,.gif,.mp4,.webm,.mov,.m4v,image/jpeg,image/png,image/webp,image/gif,video/mp4,video/webm,video/quicktime"
                   multiple>

            <div class="admin-help">
              <div><strong>Ảnh:</strong> png, jpg, jpeg, webp, gif.</div>
              <div><strong>Video:</strong> mp4, webm, mov, m4v.</div>
              <div>
                File sẽ lưu tại <b>MyCosmeticShopUploads/product/media/</b>
                và database lưu dạng <b>/uploads/product/media/tên-file</b>.
              </div>
            </div>
          </div>

          <c:set var="displayProductMedia" value="${productMediaList}" />

          <c:choose>
            <c:when test="${not empty displayProductMedia}">
              <div class="admin-product-media-current">
                <div class="admin-product-subsection-head">
                  <strong>Media hiện tại</strong>
                  <span>Tick media cần xóa, sau đó bấm Lưu thay đổi.</span>
                </div>

                <div class="admin-product-media-grid">
                  <c:forEach var="media" items="${displayProductMedia}">
                    <div class="admin-product-media-card-item">
                      <div class="admin-product-media-preview">
                        <span class="admin-product-media-badge">
                          <c:out value="${media.displayTypeLabel}"/>
                        </span>

                        <c:choose>
                          <c:when test="${media.video}">
                            <video controls muted preload="metadata">
                              <source src="${ctx}${media.mediaUrl}">
                              Trình duyệt không hỗ trợ video.
                            </video>
                          </c:when>

                          <c:otherwise>
                            <img src="${ctx}${media.mediaUrl}"
                                 alt="product media">
                          </c:otherwise>
                        </c:choose>
                      </div>

                      <div class="admin-product-media-card-body">
                        <strong>
                          <c:out value="${media.displayTypeLabel}"/>
                        </strong>

                        <code>
                          <c:out value="${media.mediaUrl}"/>
                        </code>

                        <label class="admin-product-delete-check">
                          <input type="checkbox"
                                 name="deleteMediaIds"
                                 value="${media.id}">
                          Xóa media này khi lưu
                        </label>
                      </div>
                    </div>
                  </c:forEach>
                </div>

                <div class="admin-help admin-product-section-note">
                  Hệ thống sẽ xóa cả dữ liệu SQL và file trong
                  <b>MyCosmeticShopUploads/product/media</b>.
                </div>
              </div>
            </c:when>

            <c:otherwise>
              <div class="admin-product-media-empty">
                <span>🎞️</span>
                <strong>Chưa có media chi tiết</strong>
                <small>Bạn có thể upload nhiều ảnh/video để hiển thị ở trang chi tiết sản phẩm.</small>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </section>

      <div class="admin-product-form-actions">
        <button type="submit" class="admin-btn admin-btn--primary">
          <c:choose>
            <c:when test="${empty product}">Tạo mới</c:when>
            <c:otherwise>Lưu thay đổi</c:otherwise>
          </c:choose>
        </button>

        <a class="admin-btn" href="${ctx}/admin/products">
          Hủy
        </a>
      </div>
    </form>

  </div>
</main>

<script>
  (function () {
    const textarea = document.getElementById("productDescription");
    const buttons = document.querySelectorAll(".admin-product-description-tool[data-insert]");

    if (!textarea || !buttons.length) {
      return;
    }

    buttons.forEach(function (button) {
      button.addEventListener("click", function () {
        const text = button.getAttribute("data-insert") || "";
        insertAtCursor(textarea, text);
      });
    });

    function insertAtCursor(field, text) {
      const start = field.selectionStart || 0;
      const end = field.selectionEnd || 0;
      const before = field.value.substring(0, start);
      const after = field.value.substring(end);

      const prefix = before && !before.endsWith("\n") ? "\n" : "";
      const suffix = after && !after.startsWith("\n") ? "\n" : "";

      field.value = before + prefix + text + suffix + after;
      field.focus();

      const cursor = (before + prefix + text).length;
      field.setSelectionRange(cursor, cursor);
    }
  })();

  function addRow() {
    const tableBody = document.querySelector("#variantTable tbody");

    if (!tableBody) {
      return;
    }

    const newRow = document.createElement("tr");
    newRow.className = "admin-product-variant-row";

    newRow.innerHTML = `
      <td><input type="text" name="v_size[]" class="admin-input" placeholder="VD: XL"></td>
      <td><input type="text" name="v_type[]" class="admin-input" placeholder="VD: Đỏ"></td>
      <td><input type="number" name="v_price[]" class="admin-input" value="0" min="0"></td>
      <td><input type="number" name="v_stock[]" class="admin-input" value="0" min="0"></td>
      <td class="admin-product-variant-action-cell">
        <button type="button" class="admin-btn admin-btn--danger admin-product-variant-remove" onclick="removeRow(this)">Xóa</button>
      </td>
    `;

    tableBody.appendChild(newRow);
  }

  function removeRow(btn) {
    const row = btn.closest("tr");

    if (row) {
      row.remove();
    }
  }
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

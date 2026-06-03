<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Product" scope="request"/>
<c:set var="activeMenu" value="products" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container product-form-page">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">
          <c:choose>
            <c:when test="${empty product}">Thêm sản phẩm</c:when>
            <c:otherwise>Sửa sản phẩm #${product.id}</c:otherwise>
          </c:choose>
        </h1>

        <p class="admin-subtext">
          Nhập thông tin sản phẩm. Ảnh/video upload sẽ lưu trong
          <b>MyCosmeticShopUploads</b> và database lưu đường dẫn dạng
          <b>/uploads/product/...</b>.
        </p>
      </div>

      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/products">
        Quay lại
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

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
              action="${pageContext.request.contextPath}/admin/products"
              enctype="multipart/form-data"
              class="admin-form product-form-layout">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="${empty product ? 'create' : 'update'}"/>

          <c:if test="${not empty product}">
            <input type="hidden" name="id" value="${product.id}"/>
            <input type="hidden" name="existingImage" value="${product.image}"/>
          </c:if>

          <!-- =====================================================
               THÔNG TIN CƠ BẢN
          ====================================================== -->
          <section class="admin-form-section product-form-section">
            <h2 class="admin-form-section__title">Thông tin cơ bản</h2>

            <div class="admin-grid-2">

              <div class="admin-field" style="grid-column: 1 / -1;">
                <div class="admin-label">
                  Tên sản phẩm <span class="admin-required">*</span>
                </div>

                <input class="admin-input"
                       type="text"
                       name="title"
                       value="${not empty product ? fn:escapeXml(product.title) : ''}"
                       placeholder="Ví dụ: Kem dưỡng ẩm..."
                       maxlength="255"
                       required />
              </div>

              <div class="admin-field">
                <div class="admin-label">
                  Slug <span class="admin-required">*</span>
                </div>

                <input class="admin-input"
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

              <div class="admin-field">
                <div class="admin-label">Trạng thái</div>

                <select class="admin-select" name="active">
                  <option value="1" ${empty product || product.active ? "selected" : ""}>
                    ACTIVE
                  </option>
                  <option value="0" ${not empty product && !product.active ? "selected" : ""}>
                    INACTIVE
                  </option>
                </select>

                <div class="admin-help">
                  INACTIVE: ẩn khỏi trang người dùng, admin vẫn xem được.
                </div>
              </div>

              <div class="admin-field">
                <div class="admin-label">
                  Danh mục <span class="admin-required">*</span>
                </div>

                <select class="admin-select" name="categoryId" required>
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
                <div class="admin-label">
                  Thương hiệu <span class="admin-required">*</span>
                </div>

                <select class="admin-select" name="brandId" required>
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
          </section>

          <!-- =====================================================
               GIÁ VÀ TỒN KHO
          ====================================================== -->
          <section class="admin-form-section product-form-section">
            <h2 class="admin-form-section__title">Giá bán và tồn kho</h2>

            <div class="admin-grid-2">
              <div class="admin-field">
                <div class="admin-label">
                  Giá gốc (VND) <span class="admin-required">*</span>
                </div>

                <input class="admin-input"
                       type="number"
                       name="price"
                       value="${not empty product && not empty product.price ? product.price : ''}"
                       min="0"
                       step="1"
                       required />
              </div>

              <div class="admin-field">
                <div class="admin-label">Giảm giá (%)</div>

                <input class="admin-input"
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
                <div class="admin-label">
                  Tồn kho <span class="admin-required">*</span>
                </div>

                <input class="admin-input"
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

              <div class="admin-field">
                <div class="admin-label">Ghi chú</div>

                <div class="admin-info-box">
                  <div class="admin-info-box__title">Quy tắc hiển thị</div>
                  <div class="admin-info-box__text">
                    Sản phẩm <strong>ACTIVE</strong> và còn hàng sẽ được hiển thị cho khách hàng.
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- =====================================================
               QUẢN LÝ BIỂN THỂ SẢN PHẨM
          ====================================================== -->
          <section class="admin-form-section product-form-section" style="margin-top: 14px;">
            <h2 class="admin-form-section__title">Quản lý Biến thể sản phẩm</h2>

            <div class="category-tag-table-wrap">
              <table class="category-tag-table" id="variantTable">
                <thead>
                <tr>
                  <th>Kích thước (Size)</th>
                  <th>Loại (Type)</th>
                  <th>Giá cộng thêm (VNĐ)</th>
                  <th>Tồn kho</th>
                  <th style="width: 90px; text-align: center;">Hành động</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                  <c:when test="${not empty variants}">
                    <c:forEach var="v" items="${variants}">
                      <tr class="category-tag-row">
                        <td><input type="text" name="v_size[]" class="admin-input" value="${fn:escapeXml(v.size)}" placeholder="VD: XL"></td>
                        <td><input type="text" name="v_type[]" class="admin-input" value="${fn:escapeXml(v.type)}" placeholder="VD: Đỏ"></td>
                        <td><input type="number" name="v_price[]" class="admin-input" value="${v.extraPrice}" min="0"></td>
                        <td><input type="number" name="v_stock[]" class="admin-input" value="${v.stock}" min="0"></td>
                        <td style="text-align: center;">
                          <button type="button" class="admin-btn admin-btn--danger category-tag-remove" onclick="removeRow(this)">Xóa</button>
                        </td>
                      </tr>
                    </c:forEach>
                  </c:when>
                  <c:otherwise>
                    <tr class="category-tag-row">
                      <td><input type="text" name="v_size[]" class="admin-input" placeholder="VD: XL"></td>
                      <td><input type="text" name="v_type[]" class="admin-input" placeholder="VD: Đỏ"></td>
                      <td><input type="number" name="v_price[]" class="admin-input" value="0" min="0"></td>
                      <td><input type="number" name="v_stock[]" class="admin-input" value="0" min="0"></td>
                      <td style="text-align: center;">
                        <button type="button" class="admin-btn admin-btn--danger category-tag-remove" onclick="removeRow(this)">Xóa</button>
                      </td>
                    </tr>
                  </c:otherwise>
                </c:choose>
                </tbody>
              </table>
            </div>

            <div style="margin-top: 14px; display: flex; justify-content: flex-end;">
              <button type="button" class="admin-btn" onclick="addRow()" style="background: #fff; border: 1px solid var(--admin-border); color: var(--admin-text); height: 38px;">
                + Thêm biến thể
              </button>
            </div>
          </section>


          <!-- =====================================================
               MÔ TẢ CHI TIẾT
          ====================================================== -->
          <section class="admin-form-section product-form-section">
            <h2 class="admin-form-section__title">Mô tả chi tiết sản phẩm</h2>

            <div class="admin-field">
              <div class="admin-label">Mô tả</div>

              <div class="product-description-tools">
                <button type="button" class="product-description-tool" data-insert="<h3>Tiêu đề</h3>">
                  Tiêu đề
                </button>

                <button type="button" class="product-description-tool" data-insert="<p>Nội dung đoạn văn...</p>">
                  Đoạn văn
                </button>

                <button type="button" class="product-description-tool" data-insert="<strong>Chữ đậm</strong>">
                  Đậm
                </button>

                <button type="button" class="product-description-tool" data-insert="<ul>&#10;  <li>Ý 1</li>&#10;  <li>Ý 2</li>&#10;</ul>">
                  Danh sách
                </button>

                <button type="button" class="product-description-tool" data-insert="<blockquote>Ghi chú nổi bật...</blockquote>">
                  Ghi chú
                </button>

                <button type="button" class="product-description-tool" data-insert="<img src=&quot;/uploads/product/media/ten-file.jpg&quot; alt=&quot;Mô tả ảnh&quot;>">
                  Ảnh trong mô tả
                </button>

                <button type="button" class="product-description-tool" data-insert="<video controls src=&quot;/uploads/product/media/ten-file.mp4&quot;></video>">
                  Video trong mô tả
                </button>
              </div>

              <textarea id="productDescription"
                        class="admin-textarea product-description-editor"
                        name="description"
                        rows="12"
                        placeholder="Nhập mô tả chi tiết. Có thể dùng HTML cơ bản như <h3>, <p>, <ul>, <strong>, <img>, <video>...">${not empty product ? fn:escapeXml(product.description) : ''}</textarea>

              <div class="admin-help">
                Có thể nhập mô tả dài hơn bằng HTML cơ bản:
                <b>&lt;h3&gt;</b>, <b>&lt;p&gt;</b>, <b>&lt;ul&gt;</b>,
                <b>&lt;strong&gt;</b>, <b>&lt;img&gt;</b>, <b>&lt;video&gt;</b>.
              </div>
            </div>
          </section>

          <!-- =====================================================
               ẢNH ĐẠI DIỆN + GALLERY
          ====================================================== -->
          <section class="admin-form-section product-form-section">
            <h2 class="admin-form-section__title">Ảnh sản phẩm</h2>

            <div class="admin-grid-2">
              <div class="admin-field">
                <div class="admin-label">Ảnh đại diện</div>

                <input class="admin-input"
                       type="file"
                       name="imageMain"
                       accept=".jpg,.jpeg,.png,.webp,.gif,image/jpeg,image/png,image/webp,image/gif">

                <div class="admin-help">
                  Ảnh upload sẽ lưu vật lý tại
                  <b>MyCosmeticShopUploads/product/</b>
                  và database lưu dạng
                  <b>/uploads/product/tên-file</b>.
                  Nếu sửa sản phẩm mà không chọn ảnh mới, hệ thống giữ ảnh cũ.
                  Nếu chọn ảnh mới, hệ thống sẽ xóa file ảnh đại diện cũ sau khi cập nhật SQL thành công.
                </div>
              </div>

              <div class="admin-field">
                <div class="admin-label">Ảnh mô tả / Gallery</div>

                <input class="admin-input"
                       type="file"
                       name="imageGallery"
                       accept=".jpg,.jpeg,.png,.webp,.gif,image/jpeg,image/png,image/webp,image/gif"
                       multiple>

                <div class="admin-help">
                  Ảnh gallery sẽ lưu vật lý tại
                  <b>MyCosmeticShopUploads/product/gallery/</b>
                  và database lưu dạng
                  <b>/uploads/product/gallery/tên-file</b>.
                  Khi sửa sản phẩm, ảnh mới sẽ được <b>thêm vào gallery hiện có</b>.
                </div>
              </div>
            </div>

            <c:if test="${not empty product && not empty product.image}">
              <hr class="admin-divider"/>

              <div class="admin-field">
                <div class="admin-label">Ảnh đại diện hiện tại</div>

                <div class="admin-preview">
                  <img class="admin-preview__img"
                       src="${pageContext.request.contextPath}${product.image}"
                       alt="${not empty product.title ? product.title : 'product'}">

                  <div class="admin-help admin-break">
                    Đường dẫn hiện tại:
                    <c:out value="${product.image}"/>
                  </div>

                  <c:choose>
                    <c:when test="${fn:startsWith(product.image, '/uploads/product/')}">
                      <div class="admin-help">
                        Ảnh này đã đúng chuẩn upload.
                      </div>
                    </c:when>
                    <c:otherwise>
                      <div class="admin-help" style="color:#b45309;">
                        Ảnh này có thể đang dùng đường dẫn cũ. Khi upload ảnh mới, hệ thống sẽ chuyển sang /uploads/product/.
                      </div>
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
              <hr class="admin-divider"/>

              <div class="product-gallery-current">
                <div class="admin-label">Gallery hiện tại</div>

                <div class="product-gallery-grid"
                     style="display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 14px;">
                  <c:forEach var="img" items="${displayGallery}">
                    <div class="product-gallery-item"
                         style="border: 1px solid #eee; border-radius: 14px; padding: 10px; background: #fff;">
                      <img src="${pageContext.request.contextPath}${img.image}"
                           alt="gallery"
                           style="width: 100%; height: 110px; object-fit: cover; border-radius: 10px;">

                      <div class="admin-help admin-break" style="margin-top: 8px;">
                        <c:out value="${img.image}"/>
                      </div>

                      <c:choose>
                        <c:when test="${fn:startsWith(img.image, '/uploads/product/gallery/')}">
                          <div class="admin-help">
                            Ảnh gallery đã đúng chuẩn upload.
                          </div>
                        </c:when>
                        <c:otherwise>
                          <div class="admin-help" style="color:#b45309;">
                            Path cũ
                          </div>
                        </c:otherwise>
                      </c:choose>

                      <c:if test="${not empty product && not empty img.id}">
                        <label style="display:flex; gap:8px; align-items:flex-start; margin-top:10px; padding:8px; border-radius:10px; background:#fff1f2; color:#be123c; font-size:13px; font-weight:800;">
                          <input type="checkbox"
                                 name="deleteImageIds"
                                 value="${img.id}">
                          Xóa ảnh này khi lưu
                        </label>
                      </c:if>
                    </div>
                  </c:forEach>
                </div>

                <div class="admin-help" style="margin-top: 10px;">
                  Tick ảnh gallery cần xóa, sau đó bấm <b>Lưu thay đổi</b>.
                  Hệ thống sẽ xóa cả dữ liệu SQL và file trong <b>MyCosmeticShopUploads/product/gallery</b>.
                </div>
              </div>
            </c:if>
          </section>

          <!-- =====================================================
               ISSUE 123: MEDIA CHI TIẾT ẢNH / VIDEO
          ====================================================== -->
          <section class="admin-form-section product-form-section">
            <h2 class="admin-form-section__title">Media chi tiết sản phẩm</h2>

            <div class="product-media-uploader">
              <div class="product-media-upload-box">
                <div class="admin-field" style="margin-bottom: 0;">
                  <div class="admin-label">Thêm nhiều ảnh/video</div>

                  <input class="admin-input"
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
              </div>

              <c:set var="displayProductMedia" value="${productMediaList}" />

              <c:choose>
                <c:when test="${not empty displayProductMedia}">
                  <div class="product-media-current" style="margin-top: 18px;">
                    <h3 class="admin-form-section__title" style="font-size: 18px; margin-bottom: 14px;">
                      Media hiện tại
                    </h3>

                    <div class="product-media-grid"
                         style="display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px;">
                      <c:forEach var="media" items="${displayProductMedia}">
                        <div class="product-media-card"
                             style="overflow: hidden; border: 1px solid #eee; border-radius: 16px; background: #fff;">
                          <div class="product-media-card__preview"
                               style="position: relative; width: 100%; aspect-ratio: 16 / 9; background: #000; overflow: hidden;">
                            <span class="product-media-card__badge"
                                  style="position: absolute; top: 8px; left: 8px; z-index: 2; padding: 4px 9px; border-radius: 999px; background: rgba(15,23,42,.78); color: #fff; font-size: 12px; font-weight: 800;">
                              <c:out value="${media.displayTypeLabel}"/>
                            </span>

                            <c:choose>
                              <c:when test="${media.video}">
                                <video controls muted preload="metadata"
                                       style="width: 100%; height: 100%; object-fit: contain; display: block;">
                                  <source src="${pageContext.request.contextPath}${media.mediaUrl}">
                                  Trình duyệt không hỗ trợ video.
                                </video>
                              </c:when>

                              <c:otherwise>
                                <img src="${pageContext.request.contextPath}${media.mediaUrl}"
                                     alt="product media"
                                     style="width: 100%; height: 100%; object-fit: cover; display: block;">
                              </c:otherwise>
                            </c:choose>
                          </div>

                          <div class="product-media-card__body" style="padding: 12px;">
                            <div style="color:#be185d; font-size:13px; font-weight:900;">
                              <c:out value="${media.displayTypeLabel}"/>
                            </div>

                            <div class="admin-help admin-break" style="margin-top: 6px;">
                              <c:out value="${media.mediaUrl}"/>
                            </div>

                            <label style="display:flex; gap:8px; align-items:flex-start; margin-top:10px; padding:8px; border-radius:10px; background:#fff1f2; color:#be123c; font-size:13px; font-weight:800;">
                              <input type="checkbox"
                                     name="deleteMediaIds"
                                     value="${media.id}">
                              Xóa media này khi lưu
                            </label>
                          </div>
                        </div>
                      </c:forEach>
                    </div>

                    <div class="admin-help" style="margin-top: 10px;">
                      Tick media cần xóa, sau đó bấm <b>Lưu thay đổi</b>.
                      Hệ thống sẽ xóa cả dữ liệu SQL và file trong <b>MyCosmeticShopUploads/product/media</b>.
                    </div>
                  </div>
                </c:when>

                <c:otherwise>
                  <div class="admin-help" style="margin-top: 12px; padding: 14px; border: 1px dashed #f1b6d4; border-radius: 14px; background: #fff8fb;">
                    Chưa có media chi tiết. Bạn có thể upload nhiều ảnh/video để hiển thị ở trang chi tiết sản phẩm.
                  </div>
                </c:otherwise>
              </c:choose>
            </div>
          </section>

          <hr class="admin-divider"/>

          <div class="admin-actions product-form-actions">
            <button type="submit" class="admin-btn admin-btn--primary">
              <c:choose>
                <c:when test="${empty product}">Tạo mới</c:when>
                <c:otherwise>Lưu thay đổi</c:otherwise>
              </c:choose>
            </button>

            <a class="admin-btn" href="${pageContext.request.contextPath}/admin/products">
              Hủy
            </a>
          </div>

        </form>

      </div>
    </div>

  </div>
</main>

<script>
  (function () {
    const textarea = document.getElementById("productDescription");
    const buttons = document.querySelectorAll(".product-description-tool[data-insert]");

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
    const newRow = document.createElement("tr");
    newRow.innerHTML = `
        <td><input type="text" name="v_size[]" class="admin-input"></td>
        <td><input type="text" name="v_type[]" class="admin-input"></td>
        <td><input type="number" name="v_price[]" class="admin-input" value="0"></td>
        <td><input type="number" name="v_stock[]" class="admin-input" value="0"></td>
        <td><button type="button" class="admin-btn admin-btn--danger" onclick="removeRow(this)">Xóa</button></td>
    `;
    tableBody.appendChild(newRow);
  }

  function removeRow(btn) {
    btn.closest("tr").remove();
  }

  function addRow() {
    const tableBody = document.querySelector("#variantTable tbody");
    const newRow = document.createElement("tr");

    // Áp dụng class chuẩn của Admin table cho row khi thêm mới
    newRow.className = "category-tag-row";

    newRow.innerHTML = `
        <td><input type="text" name="v_size[]" class="admin-input" placeholder="VD: XL"></td>
        <td><input type="text" name="v_type[]" class="admin-input" placeholder="VD: Đỏ"></td>
        <td><input type="number" name="v_price[]" class="admin-input" value="0" min="0"></td>
        <td><input type="number" name="v_stock[]" class="admin-input" value="0" min="0"></td>
        <td style="text-align: center;">
          <button type="button" class="admin-btn admin-btn--danger category-tag-remove" onclick="removeRow(this)">Xóa</button>
        </td>
    `;
    tableBody.appendChild(newRow);
  }
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Product" scope="request"/>
<c:set var="activeMenu" value="products" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">
          <c:choose>
            <c:when test="${empty product}">Thêm sản phẩm</c:when>
            <c:otherwise>Sửa sản phẩm #${product.id}</c:otherwise>
          </c:choose>
        </h1>

        <p class="admin-subtext">
          Nhập thông tin sản phẩm. Ảnh upload sẽ lưu trong MyCosmeticShopUploads và database lưu đường dẫn /uploads/product/.
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
              class="admin-form">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="${empty product ? 'create' : 'update'}"/>

          <c:if test="${not empty product}">
            <input type="hidden" name="id" value="${product.id}"/>

            <%--
              existingImage giữ lại ảnh cũ nếu admin không chọn ảnh đại diện mới.
              Servlet nên đọc field này khi update.
            --%>
            <input type="hidden" name="existingImage" value="${product.imageUrl}"/>
          </c:if>

          <div class="admin-grid-2">

            <div class="admin-field" style="grid-column: 1 / -1;">
              <div class="admin-label">Tên sản phẩm</div>
              <input class="admin-input"
                     type="text"
                     name="title"
                     value="${not empty product ? fn:escapeXml(product.title) : ''}"
                     placeholder="Ví dụ: Kem dưỡng ẩm..."
                     maxlength="255"
                     required />
            </div>

            <div class="admin-field">
              <div class="admin-label">Slug</div>
              <input class="admin-input"
                     type="text"
                     name="slug"
                     value="${not empty product ? fn:escapeXml(product.slug) : ''}"
                     placeholder="vi-du-kem-duong-am"
                     maxlength="255"
                     required />
              <div class="admin-help">
                Không dấu, dùng dấu gạch ngang. Ví dụ: kem-duong-am-cocoon.
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
              <div class="admin-label">Danh mục</div>
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
              <div class="admin-label">Thương hiệu</div>
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

            <div class="admin-field" style="grid-column: 1 / -1;">
              <div class="admin-label">Mô tả</div>
              <textarea class="admin-textarea"
                        name="description"
                        rows="5"
                        placeholder="Mô tả ngắn về sản phẩm...">${not empty product ? fn:escapeXml(product.description) : ''}</textarea>
            </div>

            <div class="admin-field">
              <div class="admin-label">Giá (VND)</div>
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
            </div>

            <div class="admin-field">
              <div class="admin-label">Tồn kho</div>
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

            <div class="admin-field"></div>

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
              </div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Ảnh mô tả (Gallery)</div>
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
              </div>
            </div>

          </div>

          <c:if test="${not empty product && not empty product.imageUrl}">
            <hr class="admin-divider"/>

            <div class="admin-field">
              <div class="admin-label">Ảnh đại diện hiện tại</div>

              <div class="admin-preview">
                <img class="admin-preview__img"
                     src="${pageContext.request.contextPath}${product.imageUrl}"
                     alt="${not empty product.title ? product.title : 'product'}">

                <div class="admin-help admin-break">
                  Đường dẫn hiện tại:
                  <c:out value="${product.imageUrl}"/>
                </div>

                <c:choose>
                  <c:when test="${fn:startsWith(product.imageUrl, '/uploads/product/')}">
                    <div class="admin-help">
                      Ảnh này đã đúng chuẩn upload.
                    </div>
                  </c:when>
                  <c:otherwise>
                    <div class="admin-help" style="color:#b45309;">
                      Ảnh này có thể đang dùng đường dẫn cũ. Khi lưu lại hoặc upload ảnh mới, hệ thống nên chuyển sang /uploads/product/.
                    </div>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
          </c:if>

          <%--
            Hiển thị gallery hiện tại nếu servlet có truyền:
            req.setAttribute("productImages", list)
            hoặc
            req.setAttribute("galleryImages", list)
          --%>
          <c:set var="displayGallery" value="${productImages}" />

          <c:if test="${empty displayGallery && not empty galleryImages}">
            <c:set var="displayGallery" value="${galleryImages}" />
          </c:if>

          <c:if test="${not empty displayGallery}">
            <hr class="admin-divider"/>

            <div class="admin-field" style="grid-column: 1 / -1;">
              <div class="admin-label">Gallery hiện tại</div>

              <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(120px, 1fr)); gap: 14px;">
                <c:forEach var="img" items="${displayGallery}">
                  <div style="border: 1px solid #eee; border-radius: 12px; padding: 8px; background: #fff;">
                    <img src="${pageContext.request.contextPath}${img.image}"
                         alt="gallery"
                         style="width: 100%; height: 90px; object-fit: cover; border-radius: 10px;">

                    <div class="admin-help admin-break" style="margin-top: 6px;">
                      <c:out value="${img.image}"/>
                    </div>

                    <c:if test="${not fn:startsWith(img.image, '/uploads/product/gallery/')}">
                      <div class="admin-help" style="color:#b45309;">
                        Path cũ
                      </div>
                    </c:if>
                  </div>
                </c:forEach>
              </div>
            </div>
          </c:if>

          <hr class="admin-divider"/>

          <div class="admin-actions">
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

<jsp:include page="/jsp/admin/layout/footer.jsp"/>
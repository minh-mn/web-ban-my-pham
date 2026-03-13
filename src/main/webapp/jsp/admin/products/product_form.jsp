<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

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
          Nhập thông tin cơ bản, giá và tồn kho. Ảnh đại diện và ảnh mô tả sẽ được upload.
        </p>
      </div>

      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/products">Quay lại</a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:if test="${not empty error}">
          <div class="admin-alert admin-alert--danger">
            <c:out value="${error}"/>
          </div>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/products"
              enctype="multipart/form-data"
              class="admin-form">

          <!-- ✅ CSRF (STATIC INCLUDE – KHÔNG VỠ UI) -->
          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="${empty product ? 'create' : 'update'}"/>
          <c:if test="${not empty product}">
            <input type="hidden" name="id" value="${product.id}"/>
            <input type="hidden" name="existingImage" value="${product.imageUrl}"/>
          </c:if>

          <div class="admin-grid-2">

            <div class="admin-field" style="grid-column: 1 / -1;">
              <div class="admin-label">Tên sản phẩm</div>
              <input class="admin-input" type="text" name="title"
                     value="${not empty product ? fn:escapeXml(product.title) : ''}"
                     placeholder="Ví dụ: Kem dưỡng ẩm..."
                     required />
            </div>

            <div class="admin-field">
              <div class="admin-label">Slug</div>
              <input class="admin-input" type="text" name="slug"
                     value="${not empty product ? fn:escapeXml(product.slug) : ''}"
                     placeholder="vi-du-kem-duong-am"
                     required />
              <div class="admin-help">Không dấu, dùng dấu gạch ngang.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Trạng thái</div>
              <select class="admin-select" name="active">
                <option value="1" ${empty product || product.active ? "selected" : ""}>ACTIVE</option>
                <option value="0" ${not empty product && !product.active ? "selected" : ""}>INACTIVE</option>
              </select>
              <div class="admin-help">
                INACTIVE: ẩn khỏi trang người dùng (admin vẫn thấy).
              </div>
            </div>

            <!-- CATEGORY -->
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
              <div class="admin-help">Chọn danh mục cha hoặc danh mục con.</div>
            </div>

            <!-- BRAND -->
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
              <div class="admin-help">Chọn thương hiệu của sản phẩm.</div>
            </div>

            <div class="admin-field" style="grid-column: 1 / -1;">
              <div class="admin-label">Mô tả</div>
              <textarea class="admin-textarea" name="description" rows="5"
                        placeholder="Mô tả ngắn về sản phẩm...">${not empty product ? fn:escapeXml(product.description) : ''}</textarea>
            </div>

            <div class="admin-field">
              <div class="admin-label">Giá (VND)</div>
              <input class="admin-input" type="number" name="price"
                     value="${not empty product && not empty product.price ? product.price : ''}"
                     min="0" step="1" required />
            </div>

            <div class="admin-field">
              <div class="admin-label">Giảm giá (%)</div>
              <input class="admin-input" type="number" name="discountPercent"
                     value="${not empty product ? product.discountPercent : 0}"
                     min="0" max="100" step="1" />
            </div>

            <div class="admin-field">
              <div class="admin-label">Tồn kho</div>
              <input class="admin-input" type="number" name="stock"
                     value="${not empty product ? product.stock : 0}"
                     min="0" step="1" required />
            </div>

            <div class="admin-field"></div>

            <!-- Upload ảnh -->
            <div class="admin-field">
              <div class="admin-label">Ảnh đại diện</div>
              <input class="admin-input" type="file" name="imageMain" accept="image/*">
            </div>

            <div class="admin-field">
              <div class="admin-label">Ảnh mô tả (Gallery)</div>
              <input class="admin-input" type="file" name="imageGallery" accept="image/*" multiple>
            </div>

          </div>

          <hr class="admin-divider"/>

          <div class="admin-actions">
            <button type="submit" class="admin-btn admin-btn--primary">
              <c:choose>
                <c:when test="${empty product}">Tạo mới</c:when>
                <c:otherwise>Lưu thay đổi</c:otherwise>
              </c:choose>
            </button>
            <a class="admin-btn" href="${pageContext.request.contextPath}/admin/products">Hủy</a>
          </div>

        </form>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

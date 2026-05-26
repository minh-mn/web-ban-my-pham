<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Form Thương hiệu" scope="request" />
<c:set var="activeMenu" value="brands" scope="request" />
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request" />

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">
          <c:choose>
            <c:when test="${mode == 'edit'}">Sửa thương hiệu</c:when>
            <c:otherwise>Thêm thương hiệu</c:otherwise>
          </c:choose>
        </h1>

        <p class="admin-subtext">
          Nhập tên thương hiệu và logo đại diện. Logo sẽ được lưu vào thư mục upload brand.
        </p>
      </div>

      <a class="admin-btn" href="${ctx}/admin/brands">
        Quay lại
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:if test="${not empty error}">
          <div class="admin-alert admin-alert--danger">
            <c:out value="${error}" />
          </div>
        </c:if>

        <form
                method="post"
                action="${ctx}/admin/brands"
                enctype="multipart/form-data"
                class="admin-form">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <input
                  type="hidden"
                  name="action"
                  value="${mode == 'edit' ? 'update' : 'create'}" />

          <c:if test="${mode == 'edit' && not empty brand}">
            <input type="hidden" name="id" value="${brand.id}" />
            <input type="hidden" name="existingImage" value="${brand.image}" />
          </c:if>

          <div class="admin-row">
            <div>
              <h2 class="admin-h2">Thông tin thương hiệu</h2>
              <p class="admin-subtext">
                Cập nhật tên thương hiệu và logo hiển thị trong hệ thống.
              </p>
            </div>

            <c:choose>
              <c:when test="${mode == 'edit'}">
                <span class="admin-chip">EDIT BRAND</span>
              </c:when>
              <c:otherwise>
                <span class="admin-chip">NEW BRAND</span>
              </c:otherwise>
            </c:choose>
          </div>

          <hr class="admin-divider" />

          <div class="admin-grid-2">

            <div class="admin-stack">

              <div class="admin-field">
                <label class="admin-label" for="brandName">Tên thương hiệu</label>

                <input
                        id="brandName"
                        class="admin-input"
                        type="text"
                        name="name"
                        value="${not empty brand ? brand.name : ''}"
                        placeholder="VD: L'Oréal, The Ordinary, Cocoon..."
                        maxlength="100"
                        required />

                <div class="admin-help">
                  Nhập tên thương hiệu ngắn gọn, dễ tìm kiếm và không vượt quá 100 ký tự.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="brandLogoFile">Logo thương hiệu</label>

                <input
                        id="brandLogoFile"
                        class="admin-input"
                        type="file"
                        name="imageFile"
                        accept=".jpg,.jpeg,.png,.webp,.gif,image/jpeg,image/png,image/webp,image/gif" />

                <div class="admin-help">
                  Hỗ trợ JPG, JPEG, PNG, WEBP, GIF.
                  Khi sửa thương hiệu, nếu không chọn logo mới thì hệ thống giữ logo cũ.
                </div>
              </div>

            </div>

            <div class="admin-preview">

              <div>
                <h2 class="admin-h2">Xem trước logo</h2>
                <p class="admin-subtext">
                  Khu vực này hiển thị logo hiện tại hoặc logo mới vừa chọn.
                </p>
              </div>

              <c:choose>
                <c:when test="${mode == 'edit' && not empty brand && not empty brand.image}">
                  <c:choose>
                    <c:when test="${fn:startsWith(brand.image, 'http://')
                                 || fn:startsWith(brand.image, 'https://')
                                 || fn:startsWith(brand.image, 'data:')}">
                      <img
                              id="brandPreviewImage"
                              class="admin-preview__img"
                              src="${brand.image}"
                              alt="${not empty brand.name ? brand.name : 'brand logo'}" />
                    </c:when>

                    <c:otherwise>
                      <img
                              id="brandPreviewImage"
                              class="admin-preview__img"
                              src="${ctx}${brand.image}"
                              alt="${not empty brand.name ? brand.name : 'brand logo'}" />
                    </c:otherwise>
                  </c:choose>

                  <div id="brandPreviewEmpty" class="admin-empty" style="display:none;">
                    Chưa có logo xem trước.
                  </div>

                  <div class="admin-help admin-break">
                    Đường dẫn hiện tại:
                    <c:out value="${brand.image}" />
                  </div>
                </c:when>

                <c:otherwise>
                  <img
                          id="brandPreviewImage"
                          class="admin-preview__img"
                          src=""
                          alt="Brand logo preview"
                          style="display:none;" />

                  <div id="brandPreviewEmpty" class="admin-empty">
                    Chưa có logo xem trước. Vui lòng chọn ảnh logo để kiểm tra trước khi lưu.
                  </div>
                </c:otherwise>
              </c:choose>

            </div>

          </div>

          <hr class="admin-divider" />

          <div class="admin-actions">
            <a class="admin-btn" href="${ctx}/admin/brands">
              Hủy
            </a>

            <button class="admin-btn admin-btn--primary" type="submit">
              Lưu thương hiệu
            </button>
          </div>

        </form>

      </div>
    </div>

  </div>
</main>

<script>
  (function () {
    const fileInput = document.getElementById('brandLogoFile');
    const previewImage = document.getElementById('brandPreviewImage');
    const previewEmpty = document.getElementById('brandPreviewEmpty');

    if (!fileInput || !previewImage) {
      return;
    }

    fileInput.addEventListener('change', function () {
      const file = this.files && this.files[0];

      if (!file) {
        return;
      }

      const allowedTypes = [
        'image/jpeg',
        'image/png',
        'image/webp',
        'image/gif'
      ];

      if (allowedTypes.indexOf(file.type) === -1) {
        alert('File không hợp lệ. Vui lòng chọn ảnh JPG, PNG, WEBP hoặc GIF.');
        this.value = '';
        return;
      }

      const previewUrl = URL.createObjectURL(file);

      previewImage.src = previewUrl;
      previewImage.style.display = 'block';

      if (previewEmpty) {
        previewEmpty.style.display = 'none';
      }
    });
  })();
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp" />
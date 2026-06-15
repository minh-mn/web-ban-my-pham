<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="${mode == 'edit' ? 'ADMIN | Sửa thương hiệu' : 'ADMIN | Thêm thương hiệu'}" scope="request" />
<c:set var="activeMenu" value="brands" scope="request" />
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request" />

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<main class="admin-main">
  <div class="admin-container admin-brand-form-page">

    <section class="admin-brand-form-hero">
      <div class="admin-brand-form-hero__content">
        <span class="admin-brand-form-eyebrow">SẢN PHẨM &amp; DANH MỤC</span>
        <h1 class="admin-brand-form-title">
          <c:choose>
            <c:when test="${mode == 'edit'}">Sửa thương hiệu</c:when>
            <c:otherwise>Thêm thương hiệu</c:otherwise>
          </c:choose>
        </h1>
        <p class="admin-brand-form-subtitle">
          Cập nhật tên thương hiệu và logo đại diện. Logo sẽ được lưu vào thư mục
          <strong>MyCosmeticShopUploads/brand/</strong> và dùng để hiển thị ở trang sản phẩm, bộ lọc và khu vực thương hiệu nổi bật.
        </p>
      </div>

      <div class="admin-brand-form-hero__actions">
        <a class="admin-btn" href="${ctx}/admin/brands">
          ← Quay lại danh sách
        </a>
      </div>
    </section>

    <c:if test="${not empty error}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${error}" />
      </div>
    </c:if>

    <c:if test="${not empty success}">
      <div class="admin-alert admin-alert--success">
        <c:out value="${success}" />
      </div>
    </c:if>

    <form
            method="post"
            action="${ctx}/admin/brands"
            enctype="multipart/form-data"
            class="admin-form admin-brand-form">

      <%@ include file="/jsp/common/csrf.jspf" %>

      <input
              type="hidden"
              name="action"
              value="${mode == 'edit' ? 'update' : 'create'}" />

      <c:if test="${mode == 'edit' && not empty brand}">
        <input type="hidden" name="id" value="${brand.id}" />
        <input type="hidden" name="existingImage" value="${brand.image}" />
      </c:if>

      <div class="admin-brand-form-layout">

        <section class="admin-card admin-brand-form-card">
          <div class="admin-card__body">
            <div class="admin-brand-form-section-head">
              <div>
                <h2 class="admin-brand-form-section-title">Thông tin thương hiệu</h2>
                <p class="admin-brand-form-section-desc">
                  Nhập tên thương hiệu ngắn gọn, dễ tìm kiếm và tải logo đại diện rõ nét.
                </p>
              </div>

              <c:choose>
                <c:when test="${mode == 'edit'}">
                  <span class="admin-chip admin-chip--brand">Đang chỉnh sửa</span>
                </c:when>
                <c:otherwise>
                  <span class="admin-chip admin-chip--success">Thương hiệu mới</span>
                </c:otherwise>
              </c:choose>
            </div>

            <div class="admin-brand-form-grid">
              <div class="admin-field">
                <label class="admin-label" for="brandName">
                  Tên thương hiệu <span class="admin-required">*</span>
                </label>

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
                  Tên thương hiệu nên ngắn gọn, đúng chính tả và không vượt quá 100 ký tự.
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

            <div class="admin-brand-form-note">
              <span class="admin-brand-form-note__icon">💡</span>
              <div>
                <strong>Gợi ý logo</strong>
                <span>Nên dùng ảnh nền trong suốt hoặc nền trắng, tỉ lệ vuông để logo không bị méo khi hiển thị ở danh sách thương hiệu.</span>
              </div>
            </div>
          </div>
        </section>

        <aside class="admin-card admin-brand-preview-card">
          <div class="admin-card__body">
            <div class="admin-brand-preview-head">
              <div>
                <h2 class="admin-brand-form-section-title">Xem trước logo</h2>
                <p class="admin-brand-form-section-desc">
                  Kiểm tra logo hiện tại hoặc logo mới vừa chọn trước khi lưu.
                </p>
              </div>
            </div>

            <div class="admin-brand-preview-box">
              <c:choose>
                <c:when test="${mode == 'edit' && not empty brand && not empty brand.image}">
                  <c:choose>
                    <c:when test="${fn:startsWith(brand.image, 'http://')
                                 || fn:startsWith(brand.image, 'https://')
                                 || fn:startsWith(brand.image, 'data:')}">
                      <img
                              id="brandPreviewImage"
                              class="admin-brand-preview-img"
                              src="${brand.image}"
                              alt="${not empty brand.name ? brand.name : 'brand logo'}" />
                    </c:when>

                    <c:otherwise>
                      <img
                              id="brandPreviewImage"
                              class="admin-brand-preview-img"
                              src="${ctx}${brand.image}"
                              alt="${not empty brand.name ? brand.name : 'brand logo'}" />
                    </c:otherwise>
                  </c:choose>

                  <div id="brandPreviewEmpty" class="admin-brand-preview-empty is-hidden">
                    Chưa có logo xem trước.
                  </div>

                  <div class="admin-help admin-break">
                    Đường dẫn hiện tại:
                    <strong><c:out value="${brand.image}" /></strong>
                  </div>
                </c:when>

                <c:otherwise>
                  <img
                          id="brandPreviewImage"
                          class="admin-brand-preview-img is-hidden"
                          src=""
                          alt="Brand logo preview" />

                  <div id="brandPreviewEmpty" class="admin-brand-preview-empty">
                    <span>🏷️</span>
                    <strong>Chưa có logo xem trước</strong>
                    <small>Vui lòng chọn ảnh logo để kiểm tra trước khi lưu.</small>
                  </div>
                </c:otherwise>
              </c:choose>
            </div>
          </div>
        </aside>

      </div>

      <div class="admin-brand-form-actions">
        <a class="admin-btn" href="${ctx}/admin/brands">
          Hủy
        </a>

        <button class="admin-btn admin-btn--primary" type="submit">
          Lưu thương hiệu
        </button>
      </div>

    </form>

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
      previewImage.classList.remove('is-hidden');

      if (previewEmpty) {
        previewEmpty.classList.add('is-hidden');
      }
    });
  })();
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp" />

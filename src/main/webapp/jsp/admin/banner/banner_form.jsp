<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="${not empty banner ? 'ADMIN | Sửa banner' : 'ADMIN | Thêm banner'}" scope="request" />
<c:set var="activeMenu" value="banners" scope="request" />
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request" />

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<main class="admin-main">
  <div class="admin-container admin-banner-form-page">

    <section class="admin-banner-form-hero">
      <div class="admin-banner-form-hero__content">
        <span class="admin-banner-form-eyebrow">TRANG CHỦ &amp; HIỂN THỊ</span>
        <h1 class="admin-banner-form-title">
          <c:choose>
            <c:when test="${not empty banner}">Sửa banner</c:when>
            <c:otherwise>Thêm banner</c:otherwise>
          </c:choose>
        </h1>
        <p class="admin-banner-form-subtitle">
          Quản lý thông tin banner trang chủ. Ảnh upload sẽ được lưu trong
          <strong>MyCosmeticShopUploads/banner/</strong> và database lưu đường dẫn
          <strong>/uploads/banner/</strong>.
        </p>
      </div>

      <div class="admin-banner-form-hero__actions">
        <a class="admin-btn" href="${ctx}/admin/banners">
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
            action="${ctx}/admin/banners"
            enctype="multipart/form-data"
            class="admin-form admin-banner-form">

      <%@ include file="/jsp/common/csrf.jspf" %>

      <input
              type="hidden"
              name="action"
              value="${not empty banner ? 'update' : 'create'}">

      <c:if test="${not empty banner}">
        <input type="hidden" name="id" value="${banner.id}">
        <input type="hidden" name="existingImage" value="${banner.imageUrl}">
      </c:if>

      <div class="admin-banner-form-layout">

        <section class="admin-card admin-banner-form-card">
          <div class="admin-card__body">
            <div class="admin-banner-form-section-head">
              <div>
                <h2 class="admin-banner-form-section-title">Thông tin banner</h2>
                <p class="admin-banner-form-section-desc">
                  Nhập tiêu đề, trạng thái, liên kết điều hướng và ảnh hiển thị trên trang chủ.
                </p>
              </div>

              <c:choose>
                <c:when test="${not empty banner && banner.active}">
                  <span class="admin-chip admin-chip--success">Đang hiển thị</span>
                </c:when>
                <c:when test="${not empty banner && !banner.active}">
                  <span class="admin-chip admin-chip--warning">Tạm ẩn</span>
                </c:when>
                <c:otherwise>
                  <span class="admin-chip admin-chip--brand">Banner mới</span>
                </c:otherwise>
              </c:choose>
            </div>

            <div class="admin-banner-form-grid">
              <div class="admin-field">
                <label class="admin-label" for="bannerTitle">
                  Tiêu đề banner <span class="admin-required">*</span>
                </label>
                <input
                        id="bannerTitle"
                        class="admin-input"
                        type="text"
                        name="title"
                        value="${not empty banner ? banner.title : ''}"
                        placeholder="VD: Bộ sưu tập mới, Sale 50%, Ưu đãi cuối tuần..."
                        maxlength="200"
                        required>

                <div class="admin-help">
                  Tiêu đề giúp admin dễ nhận biết banner trong danh sách quản lý.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="bannerLink">Link điều hướng</label>
                <input
                        id="bannerLink"
                        class="admin-input"
                        type="text"
                        name="link"
                        value="${not empty banner ? banner.link : ''}"
                        placeholder="VD: /products hoặc /products?category=skincare">

                <div class="admin-help">
                  Có thể để trống. Nếu nhập, nên dùng đường dẫn nội bộ trong website.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="bannerActive">Trạng thái hiển thị</label>
                <select id="bannerActive" class="admin-select" name="active">
                  <option value="1" ${empty banner || banner.active ? 'selected' : ''}>
                    Đang hiển thị trên trang chủ
                  </option>
                  <option value="0" ${not empty banner && !banner.active ? 'selected' : ''}>
                    Tạm ẩn khỏi trang chủ
                  </option>
                </select>

                <div class="admin-help">
                  Banner tạm ẩn sẽ được lưu trong admin nhưng không hiển thị ngoài trang chủ.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="bannerImageFile">Ảnh banner</label>
                <input
                        id="bannerImageFile"
                        class="admin-input"
                        type="file"
                        name="imageFile"
                        accept=".jpg,.jpeg,.png,.webp,.gif,image/jpeg,image/png,image/webp,image/gif">

                <div class="admin-help">
                  Hỗ trợ JPG, JPEG, PNG, WEBP, GIF. Khi sửa banner, nếu không chọn ảnh mới,
                  hệ thống sẽ giữ lại ảnh hiện tại.
                </div>
              </div>
            </div>

            <div class="admin-banner-form-note">
              <span class="admin-banner-form-note__icon">💡</span>
              <div>
                <strong>Gợi ý hình ảnh</strong>
                <span>
                  Nên dùng ảnh ngang, rõ chủ thể, dung lượng vừa phải để trang chủ tải nhanh và không bị méo layout.
                </span>
              </div>
            </div>

            <div class="admin-banner-form-actions">
              <a class="admin-btn" href="${ctx}/admin/banners">
                Hủy
              </a>

              <button class="admin-btn admin-btn--primary" type="submit">
                Lưu banner
              </button>
            </div>
          </div>
        </section>

        <aside class="admin-card admin-banner-preview-card">
          <div class="admin-card__body">
            <div class="admin-banner-form-section-head admin-banner-form-section-head--preview">
              <div>
                <h2 class="admin-banner-form-section-title">Xem trước ảnh</h2>
                <p class="admin-banner-form-section-desc">
                  Kiểm tra ảnh hiện tại hoặc ảnh mới vừa chọn trước khi lưu.
                </p>
              </div>
            </div>

            <div class="admin-banner-preview">
              <c:choose>
                <c:when test="${not empty banner && not empty banner.imageUrl}">
                  <img
                          id="bannerPreviewImage"
                          class="admin-banner-preview__img"
                          src="${ctx}${banner.imageUrl}"
                          alt="${not empty banner.title ? banner.title : 'banner'}">

                  <div id="bannerPreviewEmpty" class="admin-banner-preview__empty admin-banner-preview__empty--hidden">
                    Chưa có ảnh xem trước.
                  </div>

                  <div class="admin-help admin-break">
                    Đường dẫn hiện tại:
                    <strong><c:out value="${banner.imageUrl}" /></strong>
                  </div>

                  <c:choose>
                    <c:when test="${fn:startsWith(banner.imageUrl, '/uploads/banner/')}">
                      <div class="admin-alert admin-alert--success">
                        Ảnh hiện tại đang dùng đúng chuẩn đường dẫn upload.
                      </div>
                    </c:when>
                    <c:otherwise>
                      <div class="admin-alert admin-alert--warning">
                        Ảnh hiện tại có thể đang dùng đường dẫn cũ. Khi upload ảnh mới,
                        hệ thống nên lưu theo dạng /uploads/banner/.
                      </div>
                    </c:otherwise>
                  </c:choose>
                </c:when>

                <c:otherwise>
                  <img
                          id="bannerPreviewImage"
                          class="admin-banner-preview__img admin-banner-preview__img--hidden"
                          src=""
                          alt="Banner preview">

                  <div id="bannerPreviewEmpty" class="admin-banner-preview__empty">
                    <span>🖼️</span>
                    <strong>Chưa có ảnh xem trước</strong>
                    <small>Vui lòng chọn ảnh banner để kiểm tra trước khi lưu.</small>
                  </div>
                </c:otherwise>
              </c:choose>
            </div>
          </div>
        </aside>

      </div>
    </form>

  </div>
</main>

<script>
  (function () {
    const fileInput = document.getElementById('bannerImageFile');
    const previewImage = document.getElementById('bannerPreviewImage');
    const previewEmpty = document.getElementById('bannerPreviewEmpty');

    if (!fileInput || !previewImage) {
      return;
    }

    fileInput.addEventListener('change', function () {
      const file = this.files && this.files[0];

      if (!file) {
        return;
      }

      const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

      if (allowedTypes.indexOf(file.type) === -1) {
        alert('File không hợp lệ. Vui lòng chọn ảnh JPG, PNG, WEBP hoặc GIF.');
        this.value = '';
        return;
      }

      const previewUrl = URL.createObjectURL(file);

      previewImage.src = previewUrl;
      previewImage.classList.remove('admin-banner-preview__img--hidden');

      if (previewEmpty) {
        previewEmpty.classList.add('admin-banner-preview__empty--hidden');
      }
    });
  })();
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp" />

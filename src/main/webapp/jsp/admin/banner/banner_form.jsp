<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Form Banner" scope="request" />
<c:set var="activeMenu" value="banners" scope="request" />
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
            <c:when test="${not empty banner}">Sửa banner</c:when>
            <c:otherwise>Thêm banner</c:otherwise>
          </c:choose>
        </h1>

        <p class="admin-subtext">
          Quản lý thông tin banner trang chủ. Ảnh upload sẽ được lưu trong
          <strong>MyCosmeticShopUploads/banner/</strong> và database lưu đường dẫn
          <strong>/uploads/banner/</strong>.
        </p>
      </div>

      <a class="admin-btn" href="${ctx}/admin/banners">
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

        <c:if test="${not empty success}">
          <div class="admin-alert admin-alert--success">
            <c:out value="${success}" />
          </div>
        </c:if>

        <form
                method="post"
                action="${ctx}/admin/banners"
                enctype="multipart/form-data"
                class="admin-form">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <input
                  type="hidden"
                  name="action"
                  value="${not empty banner ? 'update' : 'create'}">

          <c:if test="${not empty banner}">
            <input type="hidden" name="id" value="${banner.id}">
            <input type="hidden" name="existingImage" value="${banner.imageUrl}">
          </c:if>

          <div class="admin-row">
            <div>
              <h2 class="admin-h2">Thông tin banner</h2>
              <p class="admin-subtext">
                Nhập tiêu đề, trạng thái, ảnh hiển thị và liên kết điều hướng nếu có.
              </p>
            </div>

            <c:choose>
              <c:when test="${not empty banner && banner.active}">
                <span class="admin-chip">ACTIVE</span>
              </c:when>
              <c:when test="${not empty banner && !banner.active}">
                <span class="admin-chip">INACTIVE</span>
              </c:when>
              <c:otherwise>
                <span class="admin-chip">NEW BANNER</span>
              </c:otherwise>
            </c:choose>
          </div>

          <hr class="admin-divider" />

          <div class="admin-grid-2">

            <div class="admin-stack">

              <div class="admin-field">
                <label class="admin-label" for="bannerTitle">Tiêu đề banner</label>
                <input
                        id="bannerTitle"
                        class="admin-input"
                        type="text"
                        name="title"
                        value="${not empty banner ? banner.title : ''}"
                        placeholder="VD: New Collection, Sale 50%, Bộ sưu tập mới..."
                        maxlength="200"
                        required>

                <div class="admin-help">
                  Tiêu đề giúp admin dễ nhận biết banner trong danh sách quản lý.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="bannerLink">Link tùy chọn</label>
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
                <label class="admin-label" for="bannerActive">Trạng thái</label>
                <select id="bannerActive" class="admin-select" name="active">
                  <option value="1" ${empty banner || banner.active ? 'selected' : ''}>
                    ACTIVE - Hiển thị trên trang chủ
                  </option>
                  <option value="0" ${not empty banner && !banner.active ? 'selected' : ''}>
                    INACTIVE - Tạm ẩn
                  </option>
                </select>

                <div class="admin-help">
                  Banner ở trạng thái INACTIVE sẽ không hiển thị ngoài trang chủ.
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

            <div class="admin-preview">

              <div>
                <h2 class="admin-h2">Xem trước ảnh</h2>
                <p class="admin-subtext">
                  Khu vực này hiển thị ảnh hiện tại hoặc ảnh mới vừa chọn.
                </p>
              </div>

              <c:choose>
                <c:when test="${not empty banner && not empty banner.imageUrl}">
                  <img
                          id="bannerPreviewImage"
                          class="admin-preview__img"
                          src="${ctx}${banner.imageUrl}"
                          alt="${not empty banner.title ? banner.title : 'banner'}">

                  <div id="bannerPreviewEmpty" class="admin-empty" style="display:none;">
                    Chưa có ảnh xem trước.
                  </div>

                  <div class="admin-help admin-break">
                    Đường dẫn hiện tại:
                    <c:out value="${banner.imageUrl}" />
                  </div>

                  <c:choose>
                    <c:when test="${fn:startsWith(banner.imageUrl, '/uploads/banner/')}">
                      <div class="admin-alert admin-alert--success">
                        Ảnh hiện tại đang dùng đúng chuẩn đường dẫn upload.
                      </div>
                    </c:when>
                    <c:otherwise>
                      <div class="admin-alert" style="color:#b45309;">
                        Ảnh hiện tại có thể đang dùng đường dẫn cũ. Khi upload ảnh mới,
                        hệ thống nên lưu theo dạng /uploads/banner/.
                      </div>
                    </c:otherwise>
                  </c:choose>
                </c:when>

                <c:otherwise>
                  <img
                          id="bannerPreviewImage"
                          class="admin-preview__img"
                          src=""
                          alt="Banner preview"
                          style="display:none;">

                  <div id="bannerPreviewEmpty" class="admin-empty">
                    Chưa có ảnh xem trước. Vui lòng chọn ảnh banner để kiểm tra trước khi lưu.
                  </div>
                </c:otherwise>
              </c:choose>

            </div>

          </div>

          <hr class="admin-divider" />

          <div class="admin-actions">
            <a class="admin-btn" href="${ctx}/admin/banners">
              Hủy
            </a>

            <button class="admin-btn admin-btn--primary" type="submit">
              Lưu banner
            </button>
          </div>

        </form>

      </div>
    </div>

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
      previewImage.style.display = 'block';

      if (previewEmpty) {
        previewEmpty.style.display = 'none';
      }
    });
  })();
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp" />
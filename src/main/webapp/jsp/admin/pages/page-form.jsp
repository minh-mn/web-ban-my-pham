<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | ${not empty page.id ? 'Sửa page' : 'Thêm page'}" scope="request"/>
<c:set var="activeMenu" value="pages" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEditPage" value="${not empty page.id}" />

<main class="admin-main">
  <div class="admin-container admin-page-form-page">

    <section class="admin-page-form-hero">
      <div class="admin-page-form-hero__content">
        <span class="admin-page-form-eyebrow">NỘI DUNG &amp; WEBSITE</span>
        <h1 class="admin-page-form-title">
          <c:choose>
            <c:when test="${isEditPage}">Sửa Page #${page.id}</c:when>
            <c:otherwise>Thêm Page Content</c:otherwise>
          </c:choose>
        </h1>
        <p class="admin-page-form-subtitle">
          Quản lý nội dung tĩnh của website như chính sách, giới thiệu và các trang hiển thị ở footer.
          Nội dung được soạn bằng CKEditor và có thể gắn thumbnail đại diện.
        </p>
      </div>

      <div class="admin-page-form-hero__actions">
        <a href="${ctx}/admin/pages"
           class="admin-btn">
          ← Quay lại danh sách
        </a>
      </div>
    </section>

    <form method="post"
          action="${ctx}/admin/pages/save?csrf_token=${sessionScope.CSRF_TOKEN}"
          enctype="multipart/form-data"
          class="admin-form admin-page-form">
      <%@ include file="/jsp/common/csrf.jspf" %>

      <c:if test="${isEditPage}">
        <input type="hidden" name="id" value="${page.id}">
      </c:if>

      <div class="admin-page-form-layout">

        <section class="admin-card admin-page-editor-card">
          <div class="admin-card__body">
            <div class="admin-page-form-section-head">
              <div>
                <h2 class="admin-page-form-section-title">Nội dung page</h2>
                <p class="admin-page-form-section-desc">
                  Nhập tiêu đề, slug và nội dung hiển thị cho khách hàng.
                </p>
              </div>

              <c:choose>
                <c:when test="${isEditPage}">
                  <span class="admin-chip admin-chip--brand">Đang chỉnh sửa</span>
                </c:when>
                <c:otherwise>
                  <span class="admin-chip admin-chip--success">Page mới</span>
                </c:otherwise>
              </c:choose>
            </div>

            <div class="admin-page-form-grid">
              <div class="admin-field">
                <label class="admin-label" for="pageTitleInput">
                  Tiêu đề Page <span class="admin-required">*</span>
                </label>
                <input id="pageTitleInput"
                       type="text"
                       class="admin-input"
                       name="title"
                       value="<c:out value='${page.title}'/>"
                       required
                       placeholder="Nhập tiêu đề...">
              </div>

              <div class="admin-field">
                <label class="admin-label" for="pageSlugInput">
                  Slug URL <span class="admin-required">*</span>
                </label>
                <input id="pageSlugInput"
                       type="text"
                       class="admin-input"
                       name="slug"
                       value="<c:out value='${page.slug}'/>"
                       required
                       placeholder="Ví dụ: chinh-sach-bao-mat">
                <small class="admin-help">
                  Không nhập dấu gạch chéo <b>/</b> ở đầu slug.
                </small>
              </div>

              <div class="admin-field admin-page-content-field">
                <label class="admin-label" for="content">
                  Nội dung <span class="admin-required">*</span>
                </label>
                <textarea class="admin-textarea"
                          id="content"
                          name="content">${page.content}</textarea>
                <small class="admin-help">
                  Nội dung này sẽ hiển thị khi người dùng click vào link ở footer hoặc trang tĩnh tương ứng.
                </small>
              </div>
            </div>
          </div>
        </section>

        <aside class="admin-page-editor-sidebar">

          <section class="admin-card admin-page-settings-card">
            <div class="admin-card__body">
              <div class="admin-page-form-section-head">
                <div>
                  <h2 class="admin-page-form-section-title">Thiết lập hiển thị</h2>
                  <p class="admin-page-form-section-desc">
                    Cấu hình trạng thái, loại page và thumbnail.
                  </p>
                </div>
              </div>

              <div class="admin-page-settings-grid">
                <div class="admin-field">
                  <label class="admin-label" for="pageStatusSelect">Trạng thái hiển thị</label>
                  <select id="pageStatusSelect" class="admin-input" name="status">
                    <option value="published">Published (Công khai)</option>
                    <option value="draft">Draft (Nháp)</option>
                  </select>
                </div>

                <div class="admin-field">
                  <label class="admin-label" for="pageTypeSelect">Loại cấu trúc</label>
                  <select id="pageTypeSelect" class="admin-input" name="type">
                    <option value="policy" ${page.type == 'policy' ? 'selected' : ''}>Policy (Chính sách)</option>
                    <option value="about" ${page.type == 'about' ? 'selected' : ''}>About (Giới thiệu)</option>
                  </select>
                </div>

                <div class="admin-field">
                  <label class="admin-label" for="thumbnailInput">Thumbnail</label>
                  <input type="file"
                         class="admin-input"
                         name="thumbnailFile"
                         accept="image/*"
                         id="thumbnailInput">
                  <input type="hidden" name="thumbnail" value="${page.thumbnail}">
                  <small class="admin-help">
                    Nên dùng ảnh ngang, rõ nội dung chính.
                  </small>
                </div>
              </div>

              <div class="admin-page-thumbnail-preview">
                <c:choose>
                  <c:when test="${not empty page.thumbnail}">
                    <img id="imgPreview"
                         class="admin-page-thumbnail-img"
                         src="${page.thumbnail}"
                         data-default-src="${ctx}/assets/images/pages/default-thumbnail.png"
                         alt="Thumbnail page">
                    <div id="emptyPreviewText"
                         class="admin-page-thumbnail-empty is-hidden">
                      Chưa có ảnh
                    </div>
                  </c:when>
                  <c:otherwise>
                    <img id="imgPreview"
                         class="admin-page-thumbnail-img is-hidden"
                         src=""
                         data-default-src="${ctx}/assets/images/pages/default-thumbnail.png"
                         alt="Thumbnail page">
                    <div id="emptyPreviewText"
                         class="admin-page-thumbnail-empty">
                      Chưa có ảnh
                    </div>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
          </section>

          <section class="admin-card admin-page-guide-card">
            <div class="admin-card__body">
              <div class="admin-page-guide">
                <span class="admin-page-guide__icon">📝</span>
                <div>
                  <strong>Gợi ý nội dung</strong>
                  <small>
                    Page chính sách nên có tiêu đề rõ, slug ngắn và nội dung chia đoạn để dễ đọc trên website.
                  </small>
                </div>
              </div>
            </div>
          </section>

          <section class="admin-card admin-page-submit-card">
            <div class="admin-card__body">
              <button class="admin-btn admin-btn--primary admin-page-submit-btn" type="submit">
                Lưu chỉnh sửa
              </button>
            </div>
          </section>

        </aside>
      </div>
    </form>
  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

<script src="https://cdn.ckeditor.com/4.16.2/standard/ckeditor.js"></script>
<script>
  CKEDITOR.replace('content', {
    height: 400,
    removePlugins: 'elementspath',
    resize_enabled: false
  });

  const thumbnailInput = document.getElementById('thumbnailInput');
  const imgPreview = document.getElementById('imgPreview');
  const emptyText = document.getElementById('emptyPreviewText');

  if (imgPreview) {
    imgPreview.addEventListener('error', function() {
      const defaultSrc = imgPreview.getAttribute('data-default-src');
      if (defaultSrc && imgPreview.src !== defaultSrc) {
        imgPreview.src = defaultSrc;
      }
      imgPreview.classList.remove('is-hidden');
      if (emptyText) {
        emptyText.classList.add('is-hidden');
      }
    });
  }

  if (thumbnailInput) {
    thumbnailInput.addEventListener('change', function(e) {
      const file = e.target.files[0];

      if (!file || !imgPreview) {
        return;
      }

      const reader = new FileReader();
      reader.onload = function(event) {
        imgPreview.src = event.target.result;
        imgPreview.classList.remove('is-hidden');

        if (emptyText) {
          emptyText.classList.add('is-hidden');
        }
      };
      reader.readAsDataURL(file);
    });
  }
</script>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Page Form" scope="request"/>
<c:set var="activeMenu" value="pages" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<style>
  .page-editor-layout {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 320px;
    gap: 20px;
    align-items: start;
  }

  .page-editor-sidebar {
    display: grid;
    gap: 18px;
  }

  .page-thumbnail-preview {
    width: 100%;
    height: 180px;
    border-radius: 14px;
    border: 1px dashed #d1d5db;
    overflow: hidden;
    background: #f9fafb;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 10px;
  }

  .page-thumbnail-preview img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .page-thumbnail-empty {
    color: #9ca3af;
    font-size: 14px;
  }
</style>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">
          <c:choose>
            <c:when test="${not empty page.id}">Sửa Page #${page.id}</c:when>
            <c:otherwise>Thêm mới Page Content</c:otherwise>
          </c:choose>
        </h1>
      </div>
    </div>

    <form method="post"
          action="${pageContext.request.contextPath}/admin/pages/save?csrf_token=${sessionScope.CSRF_TOKEN}"
          enctype="multipart/form-data"
          class="admin-form">

      <%@ include file="/jsp/common/csrf.jspf" %>

      <c:if test="${not empty page.id}">
        <input type="hidden" name="id" value="${page.id}">
      </c:if>

      <div class="page-editor-layout">

        <div class="page-editor-main-cards" style="display:grid; gap:20px;">
          <div class="admin-card">
            <div class="admin-card__body">

              <div class="admin-field">
                <label class="admin-label">Tiêu đề Page</label>
                <input type="text" class="admin-input" name="title" value="<c:out value='${page.title}'/>" required placeholder="Nhập tiêu đề...">
              </div>

              <div class="admin-field">
                <label class="admin-label">Slug URL (Ví dụ: chinh-sach-bao-mat)</label>
                <input type="text" class="admin-input" name="slug" value="<c:out value='${page.slug}'/>" required placeholder="Không nhập dấu gạch chéo / ở đầu...">
              </div>

              <div class="admin-field">
                <label class="admin-label">Nội dung</label>

                <textarea class="admin-textarea"
                          id="content"
                          name="content">${page.content}</textarea>

                <div class="admin-help">
                  Nội dung hiển thị khi user click footer link
                </div>
              </div>

            </div>
          </div>
        </div>

        <div class="page-editor-sidebar">

          <div class="admin-card">
            <div class="admin-card__body">

              <div class="admin-field">
                <label class="admin-label">Trạng thái hiển thị</label>
                <select class="admin-input" name="status">
                  <!-- ĐÃ SỬA: Loại bỏ các đoạn check page.status gây crash hệ thống -->
                  <option value="published">Published (Công khai)</option>
                  <option value="draft">Draft (Nháp)</option>
                </select>
              </div>

              <div class="admin-field">
                <label class="admin-label">Loại cấu trúc (Type)</label>
                <select class="admin-input" name="type">
                  <option value="policy" ${page.type == 'policy' ? 'selected' : ''}>Policy (Chính sách)</option>
                  <option value="about" ${page.type == 'about' ? 'selected' : ''}>About (Giới thiệu)</option>
                </select>
              </div>

              <div class="admin-field">
                <label class="admin-label">Thumbnail (Hình đại diện)</label>
                <input type="file" class="admin-input" name="thumbnailFile" accept="image/*" id="thumbnailInput">

                <input type="hidden" name="thumbnail" value="${page.thumbnail}">
              </div>

              <div class="page-thumbnail-preview">
                <c:choose>
                  <%-- Nếu trong DB có lưu đường dẫn ảnh --%>
                  <c:when test="${not empty page.thumbnail}">
                    <img id="imgPreview" src="${page.thumbnail}"
                         onerror="this.onerror=null; document.getElementById('emptyPreviewText').style.display='none'; this.src='${pageContext.request.contextPath}/assets/images/pages/default-thumbnail.png';"
                         alt="thumb">
                    <div id="emptyPreviewText" class="page-thumbnail-empty" style="display: none;">Chưa có ảnh</div>
                  </c:when>

                  <%-- Nếu DB hoàn toàn trống (Tạo trang mới tinh) --%>
                  <c:otherwise>
                    <img id="imgPreview" src="" alt="thumb" style="display: none; width: 100%; height: 100%; object-fit: cover;">
                    <div id="emptyPreviewText" class="page-thumbnail-empty">Chưa có ảnh</div>
                  </c:otherwise>
                </c:choose>
              </div>

            </div>
          </div>

          <div class="admin-card">
            <div class="admin-card__body">
              <button class="admin-btn admin-btn--primary" type="submit" style="width: 100%;">
                Lưu chỉnh sửa
              </button>
            </div>
          </div>

        </div>

      </div>
    </form>
  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

<script src="https://cdn.ckeditor.com/4.16.2/standard/ckeditor.js"></script>
<script>
  // Kích hoạt Trình soạn thảo văn bản CKEditor
  CKEDITOR.replace('content', {
    height: 400,
    removePlugins: 'elementspath',
    resize_enabled: false
  });

  // Xử lý sự kiện tải ảnh lên và render preview lập tức cho Admin nhìn thấy
  document.getElementById('thumbnailInput').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = function(event) {
        const imgPreview = document.getElementById('imgPreview');
        const emptyText = document.getElementById('emptyPreviewText');

        imgPreview.src = event.target.result;
        imgPreview.style.display = 'block';
        if (emptyText) {
          emptyText.style.display = 'none';
        }
      };
      reader.readAsDataURL(file);
    }
  });
</script>

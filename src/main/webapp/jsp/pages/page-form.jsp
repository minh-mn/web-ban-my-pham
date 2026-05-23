<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Page Form" scope="request"/>
<c:set var="activeMenu" value="pages" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<style>

  .page-editor-layout{
    display:grid;
    grid-template-columns: minmax(0, 1fr) 320px;
    gap:20px;
    align-items:start;
  }

  .page-editor-sidebar{
    display:grid;
    gap:18px;
  }

  .page-thumbnail-preview{
    width:100%;
    height:180px;
    border-radius:14px;
    border:1px dashed #d1d5db;
    overflow:hidden;
    background:#f9fafb;
    display:flex;
    align-items:center;
    justify-content:center;
  }

  .page-thumbnail-preview img{
    width:100%;
    height:100%;
    object-fit:cover;
  }

  .page-thumbnail-empty{
    color:#9ca3af;
    font-size:13px;
  }

  .ck-editor__editable {
    min-height: 420px;
  }

</style>

<main class="admin-main">

  <div class="admin-container">

    <!-- TOPBAR -->
    <div class="admin-topbar">

      <div>
        <h1 class="admin-h1">
          <c:choose>
            <c:when test="${not empty page}">
              Sửa CMS Page
            </c:when>
            <c:otherwise>
              Thêm CMS Page
            </c:otherwise>
          </c:choose>
        </h1>

        <p class="admin-subtext">
          Quản lý nội dung hiển thị Footer / CMS Website
        </p>
      </div>

      <a class="admin-btn"
         href="${pageContext.request.contextPath}/admin/pages">
        Quay lại
      </a>

    </div>

    <!-- FORM -->
    <form method="post"
          action="${pageContext.request.contextPath}/admin/pages/save"
          class="admin-form">

      <%@ include file="/jsp/common/csrf.jspf" %>

      <input type="hidden" name="id" value="${page.id}">

      <div class="page-editor-layout">

        <!-- LEFT CONTENT -->
        <div class="admin-card">

          <div class="admin-card__body">

            <div class="admin-field">
              <label class="admin-label">Tiêu đề Page</label>
              <input class="admin-input"
                     name="title"
                     value="${page.title}"
                     placeholder="VD: Chính sách bảo mật">
            </div>

            <div class="admin-field">
              <label class="admin-label">Slug URL</label>
              <input class="admin-input"
                     name="slug"
                     value="${page.slug}"
                     placeholder="privacy-policy">

              <div class="admin-help">
                URL: /page/{slug}
              </div>
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

        <!-- RIGHT SIDEBAR -->
        <div class="page-editor-sidebar">

          <!-- SETTINGS -->
          <div class="admin-card">
            <div class="admin-card__body">

              <div class="admin-field">
                <label class="admin-label">Loại Page</label>

                <select class="admin-select" name="type">
                  <option value="page" ${page.type == 'page' ? 'selected' : ''}>Page</option>
                  <option value="policy" ${page.type == 'policy' ? 'selected' : ''}>Policy</option>
                  <option value="about" ${page.type == 'about' ? 'selected' : ''}>About</option>
                </select>
              </div>

              <div class="admin-field">
                <label class="admin-label">Thumbnail</label>

                <input class="admin-input"
                       name="thumbnail"
                       value="${page.thumbnail}"
                       placeholder="https://...">
              </div>

              <div class="page-thumbnail-preview">

                <c:choose>
                  <c:when test="${not empty page.thumbnail}">
                    <img src="${page.thumbnail}" alt="thumb">
                  </c:when>

                  <c:otherwise>
                    <div class="page-thumbnail-empty">
                      No image
                    </div>
                  </c:otherwise>
                </c:choose>

              </div>

            </div>
          </div>

          <!-- ACTION -->
          <div class="admin-card">
            <div class="admin-card__body">

              <button class="admin-btn admin-btn--primary"
                      type="submit">
                Lưu Page
              </button>

            </div>
          </div>

        </div>

      </div>

    </form>

  </div>

</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

<script>

  CKEDITOR.replace('content', {
    height: 450
  });

</script>
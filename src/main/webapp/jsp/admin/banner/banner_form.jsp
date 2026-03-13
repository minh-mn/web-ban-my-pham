<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="Admin - Form Banner" scope="request"/>
<c:set var="activeMenu" value="banners" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

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
        <p class="admin-subtext">Nhập thông tin banner. Có thể chọn ảnh từ máy để upload.</p>
      </div>

      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/banners">Quay lại</a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <form method="post"
              action="${pageContext.request.contextPath}/admin/banners"
              enctype="multipart/form-data"
              class="admin-form">

          <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="${not empty banner ? 'update' : 'create'}">

          <c:if test="${not empty banner}">
            <input type="hidden" name="id" value="${banner.id}">
            <input type="hidden" name="existingImage" value="${banner.imageUrl}">
          </c:if>

          <div class="admin-grid-2">

            <div class="admin-field">
              <div class="admin-label">Title</div>
              <input class="admin-input" type="text" name="title"
                     value="${not empty banner ? banner.title : ''}"
                     placeholder="VD: New Collection, Sale 50%...">
              <div class="admin-help">Ví dụ: New Collection, Sale 50%, ...</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Trạng thái</div>
              <select class="admin-select" name="active">
                <option value="1" ${empty banner || banner.active ? 'selected' : ''}>ACTIVE</option>
                <option value="0" ${not empty banner && !banner.active ? 'selected' : ''}>INACTIVE</option>
              </select>
              <div class="admin-help">INACTIVE sẽ không hiển thị ở trang chủ.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Ảnh banner</div>
              <input class="admin-input" type="file" name="imageFile" accept="image/*">
              <div class="admin-help">
                Ảnh sẽ lưu vào <b>/assets/images/banner/</b>. Nếu không chọn ảnh mới khi sửa, hệ thống giữ ảnh cũ.
              </div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Link (tuỳ chọn)</div>
              <input class="admin-input" type="text" name="link"
                     value="${not empty banner ? banner.link : ''}"
                     placeholder="VD: /products?category=...">
              <div class="admin-help">Có thể để trống.</div>
            </div>

          </div>

          <c:if test="${not empty banner && not empty banner.imageUrl}">
            <hr class="admin-divider"/>
            <div class="admin-field">
              <div class="admin-label">Ảnh hiện tại</div>

              <div class="admin-preview">
                <img class="admin-preview__img"
                     src="${pageContext.request.contextPath}${banner.imageUrl}"
                     alt="banner">
                <div class="admin-help admin-break">
                  Đường dẫn: <c:out value="${banner.imageUrl}"/>
                </div>
              </div>
            </div>
          </c:if>

          <hr class="admin-divider"/>

          <div class="admin-actions">
            <a class="admin-btn" href="${pageContext.request.contextPath}/admin/banners">Hủy</a>
            <button class="admin-btn admin-btn--primary" type="submit">Lưu</button>
          </div>

        </form>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

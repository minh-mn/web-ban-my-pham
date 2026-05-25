<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Form Thương hiệu" scope="request"/>
<c:set var="activeMenu" value="brands" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

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
        <p class="admin-subtext">Nhập thông tin thương hiệu.</p>
      </div>

      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/brands">Quay lại</a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:if test="${not empty error}">
          <div class="admin-alert admin-alert--danger">
            <c:out value="${error}"/>
          </div>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/brands"
              enctype="multipart/form-data"
              class="admin-form admin-form--narrow">

          <%@ include file="/jsp/common/csrf.jspf" %>
          <input type="hidden" name="action" value="${mode == 'edit' ? 'update' : 'create'}" />

          <c:if test="${mode == 'edit'}">
            <input type="hidden" name="id" value="${brand.id}" />
          </c:if>

          <div class="admin-field">
            <div class="admin-label">Tên thương hiệu</div>
            <input class="admin-input" type="text" name="name"
                   value="${not empty brand ? brand.name : ''}"
                   placeholder="VD: L'Oréal, The Ordinary..."
                   required />
            <div class="admin-help">Nhập tên thương hiệu ngắn gọn, dễ tìm kiếm.</div>
          </div>

          <div class="admin-field">
            <div class="admin-label">Hình ảnh thương hiệu (Logo)</div>
            <input class="admin-input" type="file" name="image" accept="image/*" />

            <c:if test="${mode == 'edit' && not empty brand.image}">
              <div style="margin-top: 12px;">
                <div class="admin-help">Ảnh hiện tại:</div>
                <img src="${pageContext.request.contextPath}${brand.image}"
                     alt="${brand.name}"
                     style="width: 120px; height: 80px; object-fit: contain; border: 1px solid #e5e7eb; border-radius: 8px; padding: 4px; background: #fff;" />
              </div>
            </c:if>
            <div class="admin-help">Chọn tệp ảnh đại diện cho thương hiệu định dạng png, jpg, webp.</div>
          </div>

          <div class="admin-actions">
            <button class="admin-btn admin-btn--primary" type="submit">Lưu</button>
            <a class="admin-btn" href="${pageContext.request.contextPath}/admin/brands">Hủy</a>
          </div>

        </form>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp" />

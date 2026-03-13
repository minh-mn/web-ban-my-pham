<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="ADMIN | Form Danh mục" scope="request"/>
<c:set var="activeMenu" value="categories" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">
          <c:choose>
            <c:when test="${not empty category}">Sửa danh mục</c:when>
            <c:otherwise>Thêm danh mục</c:otherwise>
          </c:choose>
        </h1>
        <p class="admin-subtext">Nhập thông tin danh mục.</p>
      </div>

      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/categories">Quay lại</a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:if test="${not empty error}">
          <div class="admin-alert admin-alert--danger">
            <c:out value="${error}"/>
          </div>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/categories"
              class="admin-form admin-form--narrow">

          <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="${not empty category ? 'update' : 'create'}">
          <c:if test="${not empty category}">
            <input type="hidden" name="id" value="${category.id}">
          </c:if>

          <div class="admin-grid-2">

            <!-- Name -->
            <div class="admin-field">
              <div class="admin-label">Tên danh mục</div>
              <input class="admin-input"
                     type="text"
                     name="name"
                     value="${not empty category ? category.name : ''}"
                     placeholder="VD: Chăm sóc da, Trang điểm, Chống nắng..."
                     required>
              <div class="admin-help">Ví dụ: Chăm sóc da, Trang điểm, Chống nắng...</div>
            </div>

            <!-- Active -->
            <div class="admin-field">
              <div class="admin-label">Trạng thái</div>
              <select class="admin-select" name="active">
                <option value="1" ${empty category || category.active ? 'selected' : ''}>ACTIVE</option>
                <option value="0" ${not empty category && !category.active ? 'selected' : ''}>INACTIVE</option>
              </select>
              <div class="admin-help">Tắt danh mục sẽ ẩn khỏi giao diện khách hàng.</div>
            </div>

            <!-- Slug -->
            <div class="admin-field">
              <div class="admin-label">Slug</div>
              <input class="admin-input"
                     type="text"
                     name="slug"
                     value="${not empty category ? category.slug : ''}"
                     placeholder="VD: cham-soc-da">
              <div class="admin-help">Nếu để trống, hệ thống sẽ tự tạo từ tên.</div>
            </div>

            <!-- ✅ Parent category (CHA/CON) -->
            <div class="admin-field">
              <div class="admin-label">Danh mục cha (tuỳ chọn)</div>
              <select class="admin-select" name="parentId">
                <option value="">-- Không có (Danh mục cha) --</option>

                <c:forEach var="p" items="${parentCategories}">
                  <!-- ✅ không cho chọn chính nó làm cha khi edit -->
                  <c:if test="${empty category || p.id != category.id}">
                    <option value="${p.id}"
                      ${not empty category && category.parentId == p.id ? 'selected' : ''}>
                      <c:out value="${p.name}"/>
                      <c:if test="${not p.active}"> (INACTIVE)</c:if>
                    </option>
                  </c:if>
                </c:forEach>

              </select>
              <div class="admin-help">Chọn danh mục cha → danh mục này sẽ trở thành danh mục con.</div>
            </div>

          </div>

          <hr class="admin-divider"/>

          <div class="admin-actions">
            <a class="admin-btn" href="${pageContext.request.contextPath}/admin/categories">Hủy</a>
            <button class="admin-btn admin-btn--primary" type="submit">Lưu</button>
          </div>

        </form>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

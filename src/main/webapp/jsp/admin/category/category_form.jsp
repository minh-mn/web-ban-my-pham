<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Form Danh mục" scope="request"/>
<c:set var="activeMenu" value="categories" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${mode eq 'edit'}" />

<main class="admin-main">
  <div class="admin-container">

    <!-- ===================== TOP BAR ===================== -->
    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">
          <c:choose>
            <c:when test="${isEdit}">Sửa danh mục</c:when>
            <c:otherwise>Thêm danh mục</c:otherwise>
          </c:choose>
        </h1>

        <p class="admin-subtext">
          <c:choose>
            <c:when test="${isEdit}">
              Cập nhật thông tin danh mục, trạng thái hiển thị và quan hệ danh mục cha/con.
            </c:when>
            <c:otherwise>
              Tạo danh mục mới để phân loại sản phẩm và hiển thị ở trang sản phẩm.
            </c:otherwise>
          </c:choose>
        </p>
      </div>

      <a class="admin-btn" href="${ctx}/admin/categories">
        Quay lại
      </a>
    </div>

    <!-- ===================== FORM CARD ===================== -->
    <div class="admin-card">
      <div class="admin-card__head">
        <div>
          <h2 class="admin-card__title">
            <c:choose>
              <c:when test="${isEdit}">Thông tin danh mục</c:when>
              <c:otherwise>Thông tin danh mục mới</c:otherwise>
            </c:choose>
          </h2>

          <p class="admin-card__desc">
            Danh mục cha dùng để nhóm sản phẩm, danh mục con dùng để lọc và hiển thị chi tiết hơn.
          </p>
        </div>
      </div>

      <div class="admin-card__body">

        <!-- ===================== ERROR MESSAGE ===================== -->
        <c:if test="${not empty error}">
          <div class="admin-alert admin-alert--danger">
            <c:out value="${error}" />
          </div>
        </c:if>

        <form method="post"
              action="${ctx}/admin/categories"
              class="admin-form admin-form--narrow">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden"
                 name="action"
                 value="${isEdit ? 'update' : 'create'}">

          <c:if test="${isEdit}">
            <input type="hidden"
                   name="id"
                   value="${category.id}">
          </c:if>

          <div class="admin-grid-2">

            <!-- ===================== NAME ===================== -->
            <div class="admin-field">
              <label class="admin-label" for="categoryName">
                Tên danh mục <span class="admin-required">*</span>
              </label>

              <input class="admin-input"
                     id="categoryName"
                     type="text"
                     name="name"
                     value="${category.name}"
                     placeholder="VD: Chăm sóc da, Trang điểm, Chống nắng..."
                     maxlength="100"
                     required>

              <div class="admin-help">
                Tên danh mục sẽ được hiển thị ở trang quản trị và trang sản phẩm.
              </div>
            </div>

            <!-- ===================== STATUS ===================== -->
            <div class="admin-field">
              <label class="admin-label" for="categoryActive">
                Trạng thái
              </label>

              <select class="admin-select"
                      id="categoryActive"
                      name="active">
                <option value="1" ${not isEdit || category.active ? 'selected' : ''}>
                  ACTIVE - Đang hiển thị
                </option>

                <option value="0" ${isEdit && not category.active ? 'selected' : ''}>
                  INACTIVE - Tạm ẩn
                </option>
              </select>

              <div class="admin-help">
                Khi tắt trạng thái, danh mục sẽ bị ẩn khỏi giao diện khách hàng.
              </div>
            </div>

            <!-- ===================== SLUG ===================== -->
            <div class="admin-field">
              <label class="admin-label" for="categorySlug">
                Slug / Thẻ hiển thị
              </label>

              <input class="admin-input"
                     id="categorySlug"
                     type="text"
                     name="slug"
                     value="${category.slug}"
                     placeholder="VD: cham-soc-da"
                     maxlength="150"
                     pattern="[a-z0-9]+(-[a-z0-9]+)*">

              <div class="admin-help">
                Nếu để trống, hệ thống sẽ tự tạo slug từ tên danh mục. Ví dụ:
                <strong>Chăm sóc da</strong> → <strong>cham-soc-da</strong>.
              </div>
            </div>

            <!-- ===================== PARENT CATEGORY ===================== -->
            <div class="admin-field">
              <label class="admin-label" for="categoryParent">
                Danh mục cha
              </label>

              <select class="admin-select"
                      id="categoryParent"
                      name="parentId">

                <option value="">
                  -- Không có, đây là danh mục cha --
                </option>

                <c:forEach var="p" items="${parentCategories}">
                  <option value="${p.id}"
                    ${not empty category.parentId && category.parentId == p.id ? 'selected' : ''}>
                    <c:out value="${p.name}" />
                    <c:if test="${not p.active}"> - INACTIVE</c:if>
                  </option>
                </c:forEach>
              </select>

              <div class="admin-help">
                Nếu chọn danh mục cha, danh mục hiện tại sẽ trở thành danh mục con.
              </div>
            </div>

          </div>

          <!-- ===================== EDIT INFO ===================== -->
          <c:if test="${isEdit}">
            <div class="admin-info-box">
              <div class="admin-info-box__title">
                Thông tin hiện tại
              </div>

              <div class="admin-info-box__text">
                ID danh mục:
                <strong>#${category.id}</strong>

                <c:if test="${not empty category.productCount}">
                  · Số sản phẩm:
                  <strong>${category.productCount}</strong>
                </c:if>
              </div>
            </div>
          </c:if>

          <hr class="admin-divider"/>

          <!-- ===================== ACTIONS ===================== -->
          <div class="admin-actions">
            <a class="admin-btn"
               href="${ctx}/admin/categories">
              Hủy
            </a>

            <button class="admin-btn admin-btn--primary"
                    type="submit">
              <c:choose>
                <c:when test="${isEdit}">Cập nhật danh mục</c:when>
                <c:otherwise>Thêm danh mục</c:otherwise>
              </c:choose>
            </button>
          </div>

        </form>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>
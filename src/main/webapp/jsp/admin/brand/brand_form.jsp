<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

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
              class="admin-form admin-form--narrow">

          <!-- ✅ CSRF TOKEN -->
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

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%--
  Admin Layout Base
  Path: src/main/webapp/jsp/admin/layout/base.jsp

  Mục đích:
  - Gom layout admin về 1 file dùng chung.
  - Tự include header.jsp, sidebar.jsp, topbar.jsp, footer.jsp.
  - Servlet chỉ cần set pageContent rồi forward tới file này.

  Thuộc tính thường dùng từ servlet:
  - pageTitle: tiêu đề tab và topbar.
  - activeMenu: menu active trong sidebar.
  - pageCss: CSS riêng của trang, ví dụ /assets/css/admin/admin-list.css.
  - pageContent: JSP fragment/nội dung chính, ví dụ /jsp/admin/notification/notification-list.jsp.
  - adminContainerClass: class container nếu trang cần full width,
    ví dụ "admin-container admin-order-page".
  - hideAdminTopbar: true nếu trang muốn tự render topbar/hero riêng.
--%>

<c:set var="layoutContainerClass"
       value="${not empty adminContainerClass ? adminContainerClass : 'admin-container'}" />

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

<main class="admin-main">
  <div class="${layoutContainerClass}">

    <c:if test="${empty hideAdminTopbar or not hideAdminTopbar}">
      <jsp:include page="/jsp/admin/layout/topbar.jsp" />
    </c:if>

    <c:choose>
      <c:when test="${not empty pageContent}">
        <jsp:include page="${pageContent}" />
      </c:when>

      <c:when test="${not empty adminPageContent}">
        <jsp:include page="${adminPageContent}" />
      </c:when>

      <c:otherwise>
        <div class="admin-card">
          <div class="admin-card__body">
            <h2 class="admin-h2">Chưa cấu hình nội dung trang</h2>
            <p class="admin-subtext">
              Servlet cần set request attribute <strong>pageContent</strong>
              trước khi forward tới
              <strong>/jsp/admin/layout/base.jsp</strong>.
            </p>

            <hr class="admin-divider">

            <div class="admin-stack">
              <div class="admin-chip admin-chip--warning">
                Ví dụ: request.setAttribute("pageContent", "/jsp/admin/notification/notification-list.jsp");
              </div>

              <div class="admin-chip admin-chip--brand">
                Ví dụ: request.setAttribute("activeMenu", "notifications");
              </div>
            </div>
          </div>
        </div>
      </c:otherwise>
    </c:choose>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp" />

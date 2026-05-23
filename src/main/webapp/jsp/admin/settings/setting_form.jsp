<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 23/05/2026
  Time: 2:58 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Settings Form" scope="request"/>
<c:set var="activeMenu" value="settings" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <h1 class="admin-h1">Chỉnh sửa Website Settings</h1>
      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/settings">Quay lại</a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <form method="post"
              action="${pageContext.request.contextPath}/admin/settings/save"
              class="admin-form">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <div class="admin-grid-2">

            <div class="admin-field">
              <div class="admin-label">Hotline</div>
              <input class="admin-input" name="hotline"
                     value="${settings.hotline}">
            </div>

            <div class="admin-field">
              <div class="admin-label">Email Sales</div>
              <input class="admin-input" name="sales_email"
                     value="${settings.sales_email}">
            </div>

            <div class="admin-field">
              <div class="admin-label">Email HR</div>
              <input class="admin-input" name="hr_email"
                     value="${settings.hr_email}">
            </div>

            <div class="admin-field">
              <div class="admin-label">Địa chỉ</div>
              <input class="admin-input" name="address"
                     value="${settings.address}">
            </div>

            <div class="admin-field">
              <div class="admin-label">Facebook</div>
              <input class="admin-input" name="facebook"
                     value="${settings.facebook}">
            </div>

            <div class="admin-field">
              <div class="admin-label">Instagram</div>
              <input class="admin-input" name="instagram"
                     value="${settings.instagram}">
            </div>

            <div class="admin-field">
              <div class="admin-label">Tên công ty</div>
              <input class="admin-input" name="company_name"
                     value="${settings.company_name}">
            </div>

            <div class="admin-field">
              <div class="admin-label">MSDN</div>
              <input class="admin-input" name="business_code"
                     value="${settings.business_code}">
            </div>

            <div class="admin-field">
              <div class="admin-label">Ngày cấp</div>
              <input class="admin-input" name="business_date"
                     value="${settings.business_date}">
            </div>

            <div class="admin-field">
              <div class="admin-label">Tên mềm</div>
              <input class="admin-input" name="name_website"
                     value="${settings.name_website}">
            </div>

            <div class="admin-field">
              <div class="admin-label">Năm bản quyền</div>
              <input class="admin-input" name="copyright_year"
                     value="${settings.copyright_year}">
            </div>
            <div class="admin-actions" style="margin-top:24px;">

          </div>

          <div class="admin-actions">
            <button class="admin-btn admin-btn--primary" type="submit">
              Lưu thay đổi
            </button>
          </div>

        </form>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

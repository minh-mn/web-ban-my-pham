<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="ADMIN | Thương hiệu" scope="request"/>
<c:set var="activeMenu" value="brands" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Thương hiệu</h1>
        <p class="admin-subtext">Quản lý thương hiệu sản phẩm.</p>
      </div>

      <a class="admin-btn admin-btn--primary"
         href="${pageContext.request.contextPath}/admin/brands?action=new">
        + Thêm thương hiệu
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <!-- TOOLBAR -->
        <div class="admin-toolbar">
          <form method="get"
                action="${pageContext.request.contextPath}/admin/brands"
                class="admin-toolbar__form">
            <input class="admin-input" type="text" name="q" value="${param.q}" placeholder="Tìm theo tên...">
            <button class="admin-btn" type="submit">Tìm</button>

            <c:if test="${not empty param.q}">
              <a class="admin-btn" href="${pageContext.request.contextPath}/admin/brands">Xóa lọc</a>
            </c:if>
          </form>
        </div>

        <c:choose>
          <c:when test="${empty brands}">
            <div class="admin-empty">Chưa có thương hiệu.</div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
                <tr>
                  <th style="width:90px;">ID</th>
                  <th>Tên thương hiệu</th>
                  <th style="width:220px;">Thao tác</th>
                </tr>
              </thead>

              <tbody>
                <c:forEach var="b" items="${brands}">
                  <tr>
                    <td>#${b.id}</td>
                    <td><c:out value="${b.name}"/></td>

                    <td class="admin-actions">
                      <a class="admin-btn"
                         href="${pageContext.request.contextPath}/admin/brands?action=edit&id=${b.id}">
                        Sửa
                      </a>

                      <form method="post"
                            action="${pageContext.request.contextPath}/admin/brands"
                            class="admin-inline"
                            onsubmit="return confirm('Xóa thương hiệu này?')">

                        <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
                        <%@ include file="/jsp/common/csrf.jspf" %>

                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${b.id}">
                        <button class="admin-btn admin-btn--danger" type="submit">Xóa</button>
                      </form>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

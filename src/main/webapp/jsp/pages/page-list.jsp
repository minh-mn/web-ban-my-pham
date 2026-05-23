<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 23/05/2026
  Time: 7:02 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Pages CMS" scope="request"/>
<c:set var="activeMenu" value="pages" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <!-- TOP -->
    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Pages CMS</h1>
        <p class="admin-subtext">Quản lý nội dung website (policy, page, footer...)</p>
      </div>

      <a class="admin-btn admin-btn--primary"
         href="${pageContext.request.contextPath}/admin/pages?action=new">
        + Thêm page
      </a>
    </div>

    <!-- TABLE -->
    <div class="admin-card">
      <div class="admin-card__body">

        <c:choose>
          <c:when test="${empty pages}">
            <div class="admin-empty">Chưa có page nào.</div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
              <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Slug</th>
                <th>Type</th>
                <th>Actions</th>
              </tr>
              </thead>

              <tbody>
              <c:forEach var="p" items="${pages}">
                <tr>
                  <td>#${p.id}</td>

                  <td><b>${p.title}</b></td>

                  <td><c:out value="${p.slug}"/></td>

                  <td>
                    <span class="admin-chip">
                        ${p.type}
                    </span>
                  </td>

                  <td class="admin-actions">

                    <a class="admin-btn"
                       href="${pageContext.request.contextPath}/admin/pages?action=edit&id=${p.id}">
                      Sửa
                    </a>

                    <form method="post"
                          action="${pageContext.request.contextPath}/admin/pages"
                          class="admin-inline"
                          onsubmit="return confirm('Xóa page này?')">

                      <%@ include file="/jsp/common/csrf.jspf" %>

                      <input type="hidden" name="action" value="delete">
                      <input type="hidden" name="id" value="${p.id}">

                      <button class="admin-btn admin-btn--danger" type="submit">
                        Xóa
                      </button>

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

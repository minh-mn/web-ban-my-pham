<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="ADMIN | Danh mục" scope="request"/>
<c:set var="activeMenu" value="categories" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Danh mục</h1>
        <p class="admin-subtext">Quản lý danh mục sản phẩm (cha / con).</p>
      </div>

      <a class="admin-btn admin-btn--primary"
         href="${pageContext.request.contextPath}/admin/categories?action=new">
        + Thêm danh mục
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:choose>
          <c:when test="${empty parents}">
            <div class="admin-empty">Chưa có danh mục.</div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
                <tr>
                  <th style="width:90px;">ID</th>
                  <th>Tên danh mục</th>
                  <th style="width:160px;">Trạng thái</th>
                  <th style="width:260px;">Thao tác</th>
                </tr>
              </thead>

              <tbody>
                <c:forEach var="p" items="${parents}">
                  <tr>
                    <td>#${p.id}</td>
                    <td>
                      <strong><c:out value="${p.name}"/></strong>
                      <span class="admin-muted" style="margin-left:6px;">(Cha)</span>
                    </td>
                    <td class="admin-status-cell">
                      <c:choose>
                        <c:when test="${p.active}">
                          <span class="admin-pill admin-pill--ok">ACTIVE</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill">INACTIVE</span>
                        </c:otherwise>
                      </c:choose>
                    </td>
                    <td class="admin-actions">
                      <a class="admin-btn"
                         href="${pageContext.request.contextPath}/admin/categories?action=edit&id=${p.id}">
                        Sửa
                      </a>
                      <form method="post"
                            action="${pageContext.request.contextPath}/admin/categories"
                            class="admin-inline"
                            onsubmit="return confirm('Xóa danh mục này?')">

                        <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
                        <%@ include file="/jsp/common/csrf.jspf" %>

                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${p.id}">
                        <button class="admin-btn admin-btn--danger" type="submit">Xóa</button>
                      </form>
                    </td>
                  </tr>

                  <c:forEach var="ch" items="${childrenMap[p.id]}">
                    <tr>
                      <td>#${ch.id}</td>
                      <td>
                        <span style="display:inline-block; padding-left:18px;">↳</span>
                        <c:out value="${ch.name}"/>
                        <span class="admin-muted" style="margin-left:6px;">(Con)</span>
                      </td>
                      <td class="admin-status-cell">
                        <c:choose>
                          <c:when test="${ch.active}">
                            <span class="admin-pill admin-pill--ok">ACTIVE</span>
                          </c:when>
                          <c:otherwise>
                            <span class="admin-pill">INACTIVE</span>
                          </c:otherwise>
                        </c:choose>
                      </td>
                      <td class="admin-actions">
                        <a class="admin-btn"
                           href="${pageContext.request.contextPath}/admin/categories?action=edit&id=${ch.id}">
                          Sửa
                        </a>
                        <form method="post"
                              action="${pageContext.request.contextPath}/admin/categories"
                              class="admin-inline"
                              onsubmit="return confirm('Xóa danh mục này?')">

                          <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
                          <%@ include file="/jsp/common/csrf.jspf" %>

                          <input type="hidden" name="action" value="delete">
                          <input type="hidden" name="id" value="${ch.id}">
                          <button class="admin-btn admin-btn--danger" type="submit">Xóa</button>
                        </form>
                      </td>
                    </tr>
                  </c:forEach>

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

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Thương hiệu" scope="request" />
<c:set var="activeMenu" value="brands" scope="request" />
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request" />

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Quản lý thương hiệu</h1>
        <p class="admin-subtext">
          Quản lý danh sách thương hiệu sản phẩm, tên thương hiệu và logo hiển thị.
        </p>
      </div>

      <a class="admin-btn admin-btn--primary" href="${ctx}/admin/brands?action=new">
        + Thêm thương hiệu
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <div class="admin-row">
          <div>
            <h2 class="admin-h2">Danh sách thương hiệu</h2>
            <p class="admin-subtext">
              Tổng số:
              <strong>
                <c:choose>
                  <c:when test="${empty brands}">0</c:when>
                  <c:otherwise>${fn:length(brands)}</c:otherwise>
                </c:choose>
              </strong>
              thương hiệu
            </p>
          </div>

          <span class="admin-chip">Brand Management</span>
        </div>

        <hr class="admin-divider" />

        <c:choose>
          <c:when test="${empty brands}">
            <div class="admin-empty">
              Chưa có thương hiệu nào. Hãy thêm thương hiệu đầu tiên để quản lý sản phẩm tốt hơn.
            </div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
              <tr>
                <th style="width: 80px;">ID</th>
                <th>Thương hiệu</th>
                <th style="width: 150px;">Sản phẩm</th>
                <th style="width: 220px;">Thao tác</th>
              </tr>
              </thead>

              <tbody>
              <c:forEach var="b" items="${brands}">
                <tr>
                  <td>
                    <strong>#${b.id}</strong>
                  </td>

                  <td>
                    <div class="admin-media">
                      <c:choose>
                        <c:when test="${not empty b.image}">
                          <c:choose>
                            <c:when test="${fn:startsWith(b.image, 'http://')
                                           || fn:startsWith(b.image, 'https://')
                                           || fn:startsWith(b.image, 'data:')}">
                              <img
                                      class="admin-thumb admin-thumb--logo"
                                      src="${b.image}"
                                      alt="Brand logo">
                            </c:when>
                            <c:otherwise>
                              <img
                                      class="admin-thumb admin-thumb--logo"
                                      src="${ctx}${b.image}"
                                      alt="Brand logo">
                            </c:otherwise>
                          </c:choose>
                        </c:when>

                        <c:otherwise>
                          <div class="admin-thumb admin-thumb--logo admin-thumb--empty">
                            Logo
                          </div>
                        </c:otherwise>
                      </c:choose>

                      <div class="admin-media__meta">
                        <strong class="admin-media__title">
                          <c:out value="${b.name}" />
                        </strong>

                        <c:choose>
                          <c:when test="${not empty b.image}">
                            <div class="admin-path">
                              <c:out value="${b.image}" />
                            </div>
                          </c:when>
                          <c:otherwise>
                            <span class="admin-muted">Chưa có logo thương hiệu</span>
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </div>
                  </td>

                  <td class="admin-status-cell">
                      <span class="admin-pill">
                        ${b.productCount} sản phẩm
                      </span>
                  </td>

                  <td>
                    <div class="admin-actions">
                      <a
                              class="admin-btn"
                              href="${ctx}/admin/brands?action=edit&id=${b.id}">
                        Sửa
                      </a>

                      <form
                              method="post"
                              action="${ctx}/admin/brands"
                              class="admin-inline"
                              onsubmit="return confirm('Bạn có chắc muốn xóa thương hiệu này không?')">

                        <%@ include file="/jsp/common/csrf.jspf" %>

                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${b.id}">

                        <button class="admin-btn admin-btn--danger" type="submit">
                          Xóa
                        </button>
                      </form>
                    </div>
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

<jsp:include page="/jsp/admin/layout/footer.jsp" />
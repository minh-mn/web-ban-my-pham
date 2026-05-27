<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Danh mục" scope="request"/>
<c:set var="activeMenu" value="categories" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<main class="admin-main">
  <div class="admin-container">

    <!-- ===================== TOP BAR ===================== -->
    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Quản lý danh mục</h1>
        <p class="admin-subtext">
          Quản lý danh mục sản phẩm theo cấu trúc cha / con và thẻ hiển thị ở trang sản phẩm.
        </p>
      </div>

      <a class="admin-btn admin-btn--primary"
         href="${ctx}/admin/categories?action=new">
        + Thêm danh mục
      </a>
    </div>

    <!-- ===================== MESSAGE ===================== -->
    <c:if test="${not empty success}">
      <div class="admin-alert admin-alert--success">
        <c:out value="${success}" />
      </div>
    </c:if>

    <c:if test="${not empty error}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${error}" />
      </div>
    </c:if>

    <!-- ===================== LIST CARD ===================== -->
    <div class="admin-card">
      <div class="admin-card__head">
        <div>
          <h2 class="admin-card__title">Danh sách danh mục</h2>
          <p class="admin-card__desc">
            Danh mục cha dùng để nhóm sản phẩm, danh mục con dùng để lọc và hiển thị chi tiết hơn.
          </p>
        </div>
      </div>

      <div class="admin-card__body">

        <c:choose>
          <c:when test="${empty parents}">
            <div class="admin-empty">
              <div class="admin-empty__title">Chưa có danh mục</div>
              <div class="admin-empty__text">
                Hãy thêm danh mục đầu tiên để bắt đầu phân loại sản phẩm trong cửa hàng.
              </div>

              <a class="admin-btn admin-btn--primary"
                 href="${ctx}/admin/categories?action=new">
                + Thêm danh mục
              </a>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-table-wrap">
              <table class="admin-table">
                <thead>
                <tr>
                  <th style="width: 80px;">ID</th>
                  <th>Tên danh mục</th>
                  <th style="width: 220px;">Slug / Thẻ hiển thị</th>
                  <th style="width: 120px;">Loại</th>
                  <th style="width: 130px;">Sản phẩm</th>
                  <th style="width: 140px;">Trạng thái</th>
                  <th style="width: 220px;">Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="p" items="${parents}">
                  <!-- ===================== PARENT CATEGORY ===================== -->
                  <tr class="admin-category-row admin-category-row--parent">
                    <td>
                      <span class="admin-id">#${p.id}</span>
                    </td>

                    <td>
                      <div class="admin-category-name">
                        <strong>
                          <c:out value="${p.name}" />
                        </strong>
                      </div>
                    </td>

                    <td>
                        <span class="admin-tag">
                          <c:out value="${p.slug}" />
                        </span>
                    </td>

                    <td>
                      <span class="admin-pill admin-pill--info">Danh mục cha</span>
                    </td>

                    <td>
                        <span class="admin-count-pill">
                          ${p.productCount} sản phẩm
                        </span>
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
                         href="${ctx}/admin/categories?action=edit&id=${p.id}">
                        Sửa
                      </a>

                      <form method="post"
                            action="${ctx}/admin/categories"
                            class="admin-inline"
                            onsubmit="return confirm('Bạn có chắc muốn xóa danh mục này không? Nếu danh mục đang có sản phẩm hoặc danh mục con thì hệ thống sẽ không cho xóa.')">

                        <%@ include file="/jsp/common/csrf.jspf" %>

                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${p.id}">

                        <button class="admin-btn admin-btn--danger" type="submit">
                          Xóa
                        </button>
                      </form>
                    </td>
                  </tr>

                  <!-- ===================== CHILD CATEGORIES ===================== -->
                  <c:forEach var="ch" items="${childrenMap[p.id]}">
                    <tr class="admin-category-row admin-category-row--child">
                      <td>
                        <span class="admin-id">#${ch.id}</span>
                      </td>

                      <td>
                        <div class="admin-category-name admin-category-name--child">
                          <span class="admin-tree-symbol">↳</span>
                          <span>
                              <c:out value="${ch.name}" />
                            </span>
                        </div>
                      </td>

                      <td>
                          <span class="admin-tag">
                            <c:out value="${ch.slug}" />
                          </span>
                      </td>

                      <td>
                        <span class="admin-pill admin-pill--sub">Danh mục con</span>
                      </td>

                      <td>
                          <span class="admin-count-pill">
                            ${ch.productCount} sản phẩm
                          </span>
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
                           href="${ctx}/admin/categories?action=edit&id=${ch.id}">
                          Sửa
                        </a>

                        <form method="post"
                              action="${ctx}/admin/categories"
                              class="admin-inline"
                              onsubmit="return confirm('Bạn có chắc muốn xóa danh mục này không? Nếu danh mục đang có sản phẩm thì hệ thống sẽ không cho xóa.')">

                          <%@ include file="/jsp/common/csrf.jspf" %>

                          <input type="hidden" name="action" value="delete">
                          <input type="hidden" name="id" value="${ch.id}">

                          <button class="admin-btn admin-btn--danger" type="submit">
                            Xóa
                          </button>
                        </form>
                      </td>
                    </tr>
                  </c:forEach>
                </c:forEach>
                </tbody>
              </table>
            </div>
          </c:otherwise>
        </c:choose>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>
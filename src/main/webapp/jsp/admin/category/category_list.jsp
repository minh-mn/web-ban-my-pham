<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Danh mục" scope="request"/>
<c:set var="activeMenu" value="categories" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<main class="admin-main">
  <div class="admin-container admin-category-container">

    <!-- ===================== TOP BAR ===================== -->
    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Quản lý danh mục</h1>

        <p class="admin-subtext">
          Quản lý danh mục sản phẩm theo cấu trúc cha / con, số lượng sản phẩm
          và các thẻ hiển thị trên trang sản phẩm.
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
    <div class="admin-card admin-category-card">
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
            <div class="admin-table-wrap admin-category-table-wrap">
              <table class="admin-table admin-category-table">
                <thead>
                <tr>
                  <th class="admin-category-col-id">ID</th>
                  <th class="admin-category-col-name">Tên danh mục</th>
                  <th class="admin-category-col-slug">Slug</th>
                  <th class="admin-category-col-tags">Thẻ hiển thị</th>
                  <th class="admin-category-col-type">Loại</th>
                  <th class="admin-category-col-products">Sản phẩm</th>
                  <th class="admin-category-col-status">Trạng thái</th>
                  <th class="admin-category-col-actions">Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="p" items="${parents}">
                  <!-- ===================== PARENT CATEGORY ===================== -->
                  <tr class="admin-category-row admin-category-row--parent">
                    <!-- ID -->
                    <td class="admin-category-id-cell">
                      <span class="admin-id">#${p.id}</span>
                    </td>

                    <!-- NAME -->
                    <td>
                      <div class="admin-category-name">
                        <strong>
                          <c:out value="${p.name}" />
                        </strong>
                      </div>

                      <c:if test="${not empty childrenMap[p.id]}">
                        <div class="admin-category-subtext">
                          Có danh mục con
                        </div>
                      </c:if>
                    </td>

                    <!-- SLUG -->
                    <td>
                      <span class="admin-category-slug">
                        <c:out value="${p.slug}" />
                      </span>
                    </td>

                    <!-- TAG COUNT -->
                    <td>
                      <c:choose>
                        <c:when test="${p.tagCount > 0}">
                          <span class="admin-category-tag-count admin-category-tag-count--active">
                            ${p.tagCount} thẻ
                          </span>
                        </c:when>

                        <c:otherwise>
                          <span class="admin-category-tag-count">
                            Chưa có thẻ
                          </span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <!-- TYPE -->
                    <td>
                      <span class="admin-pill admin-pill--info">
                        Danh mục cha
                      </span>
                    </td>

                    <!-- PRODUCT COUNT -->
                    <td>
                      <span class="admin-count-pill">
                        ${p.productCount} sản phẩm
                      </span>
                    </td>

                    <!-- STATUS -->
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

                    <!-- ACTIONS -->
                    <td class="admin-category-action-cell">
                      <div class="admin-category-actions">
                        <a class="admin-btn admin-category-action-btn"
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

                          <button class="admin-btn admin-btn--danger admin-category-action-btn"
                                  type="submit">
                            Xóa
                          </button>
                        </form>
                      </div>
                    </td>
                  </tr>

                  <!-- ===================== CHILD CATEGORIES ===================== -->
                  <c:forEach var="ch" items="${childrenMap[p.id]}">
                    <tr class="admin-category-row admin-category-row--child">
                      <!-- ID -->
                      <td class="admin-category-id-cell">
                        <span class="admin-id">#${ch.id}</span>
                      </td>

                      <!-- NAME -->
                      <td>
                        <div class="admin-category-name admin-category-name--child">
                          <span class="admin-tree-symbol">↳</span>

                          <span>
                            <c:out value="${ch.name}" />
                          </span>
                        </div>

                        <div class="admin-category-subtext">
                          Thuộc:
                          <strong>
                            <c:out value="${p.name}" />
                          </strong>
                        </div>
                      </td>

                      <!-- SLUG -->
                      <td>
                        <span class="admin-category-slug">
                          <c:out value="${ch.slug}" />
                        </span>
                      </td>

                      <!-- TAG COUNT -->
                      <td>
                        <c:choose>
                          <c:when test="${ch.tagCount > 0}">
                            <span class="admin-category-tag-count admin-category-tag-count--active">
                              ${ch.tagCount} thẻ
                            </span>
                          </c:when>

                          <c:otherwise>
                            <span class="admin-category-tag-count">
                              Chưa có thẻ
                            </span>
                          </c:otherwise>
                        </c:choose>
                      </td>

                      <!-- TYPE -->
                      <td>
                        <span class="admin-pill admin-pill--sub">
                          Danh mục con
                        </span>
                      </td>

                      <!-- PRODUCT COUNT -->
                      <td>
                        <span class="admin-count-pill">
                          ${ch.productCount} sản phẩm
                        </span>
                      </td>

                      <!-- STATUS -->
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

                      <!-- ACTIONS -->
                      <td class="admin-category-action-cell">
                        <div class="admin-category-actions">
                          <a class="admin-btn admin-category-action-btn"
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

                            <button class="admin-btn admin-btn--danger admin-category-action-btn"
                                    type="submit">
                              Xóa
                            </button>
                          </form>
                        </div>
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
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Quản lý danh mục" scope="request"/>
<c:set var="activeMenu" value="categories" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="categoryParentTotal" value="0" />
<c:set var="categoryChildTotal" value="0" />
<c:set var="categoryTagTotal" value="0" />
<c:set var="categoryProductTotal" value="0" />
<c:set var="categoryInactiveTotal" value="0" />

<c:forEach var="pStat" items="${parents}">
  <c:set var="categoryParentTotal" value="${categoryParentTotal + 1}" />
  <c:set var="categoryTagTotal" value="${categoryTagTotal + (empty pStat.tagCount ? 0 : pStat.tagCount)}" />
  <c:set var="categoryProductTotal" value="${categoryProductTotal + (empty pStat.productCount ? 0 : pStat.productCount)}" />
  <c:if test="${not pStat.active}">
    <c:set var="categoryInactiveTotal" value="${categoryInactiveTotal + 1}" />
  </c:if>

  <c:forEach var="chStat" items="${childrenMap[pStat.id]}">
    <c:set var="categoryChildTotal" value="${categoryChildTotal + 1}" />
    <c:set var="categoryTagTotal" value="${categoryTagTotal + (empty chStat.tagCount ? 0 : chStat.tagCount)}" />
    <c:set var="categoryProductTotal" value="${categoryProductTotal + (empty chStat.productCount ? 0 : chStat.productCount)}" />
    <c:if test="${not chStat.active}">
      <c:set var="categoryInactiveTotal" value="${categoryInactiveTotal + 1}" />
    </c:if>
  </c:forEach>
</c:forEach>

<c:set var="categoryTotal" value="${categoryParentTotal + categoryChildTotal}" />

<main class="admin-main">
  <div class="admin-container admin-category-page">

    <section class="admin-category-hero">
      <div class="admin-category-hero__content">
        <span class="admin-category-eyebrow">SẢN PHẨM &amp; DANH MỤC</span>
        <h1 class="admin-category-title">Quản lý danh mục</h1>
        <p class="admin-category-subtitle">
          Quản lý danh mục sản phẩm theo cấu trúc cha / con, số lượng sản phẩm và các thẻ hiển thị trên trang sản phẩm.
          Cấu trúc danh mục rõ ràng giúp khách hàng lọc sản phẩm nhanh hơn.
        </p>
      </div>

      <div class="admin-category-hero__actions">
        <a class="admin-btn admin-btn--primary" href="${ctx}/admin/categories?action=new">
          + Thêm danh mục
        </a>
      </div>
    </section>

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

    <section class="admin-category-summary">
      <div class="admin-category-stat admin-category-stat--total">
        <span class="admin-category-stat__icon">🗂️</span>
        <span class="admin-category-stat__label">Tổng danh mục</span>
        <strong class="admin-category-stat__value">
          <c:out value="${categoryTotal}" />
        </strong>
        <span class="admin-category-stat__note">Bao gồm danh mục cha và con</span>
      </div>

      <div class="admin-category-stat admin-category-stat--parent">
        <span class="admin-category-stat__icon">🌳</span>
        <span class="admin-category-stat__label">Danh mục cha</span>
        <strong class="admin-category-stat__value">
          <c:out value="${categoryParentTotal}" />
        </strong>
        <span class="admin-category-stat__note">Nhóm phân loại cấp chính</span>
      </div>

      <div class="admin-category-stat admin-category-stat--tag">
        <span class="admin-category-stat__icon">🏷️</span>
        <span class="admin-category-stat__label">Thẻ hiển thị</span>
        <strong class="admin-category-stat__value">
          <c:out value="${categoryTagTotal}" />
        </strong>
        <span class="admin-category-stat__note">Tổng tag đang cấu hình</span>
      </div>

      <div class="admin-category-stat admin-category-stat--product">
        <span class="admin-category-stat__icon">🧴</span>
        <span class="admin-category-stat__label">Sản phẩm liên kết</span>
        <strong class="admin-category-stat__value">
          <c:out value="${categoryProductTotal}" />
        </strong>
        <span class="admin-category-stat__note">Tổng sản phẩm theo danh mục</span>
      </div>
    </section>

    <section class="admin-card admin-category-list-card">
      <div class="admin-card__body">
        <div class="admin-category-section-head">
          <div>
            <h2 class="admin-category-section-title">Danh sách danh mục</h2>
            <p class="admin-category-section-desc">
              Theo dõi cây danh mục, slug, số thẻ hiển thị, số sản phẩm và trạng thái của từng danh mục.
            </p>
          </div>

          <div class="admin-category-head-chips">
            <span class="admin-chip admin-chip--brand">
              <c:out value="${categoryTotal}" /> danh mục
            </span>
            <c:if test="${categoryInactiveTotal gt 0}">
              <span class="admin-chip admin-chip--warning">
                <c:out value="${categoryInactiveTotal}" /> đang ẩn
              </span>
            </c:if>
          </div>
        </div>

        <c:choose>
          <c:when test="${empty parents}">
            <div class="admin-category-empty">
              <div class="admin-category-empty__icon">🗂️</div>
              <div>
                <h3>Chưa có danh mục</h3>
                <p>Hãy thêm danh mục đầu tiên để bắt đầu phân loại sản phẩm trong cửa hàng.</p>
                <a class="admin-btn admin-btn--primary" href="${ctx}/admin/categories?action=new">
                  + Thêm danh mục
                </a>
              </div>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-category-table-wrap">
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
                  <tr class="admin-category-row admin-category-row--parent ${not p.active ? 'admin-category-row--inactive' : ''}">
                    <td class="admin-category-id-cell">
                      <span class="admin-id">#<c:out value="${p.id}" /></span>
                    </td>

                    <td>
                      <div class="admin-category-name">
                        <span class="admin-category-icon admin-category-icon--parent">🌳</span>
                        <span>
                          <strong><c:out value="${p.name}" /></strong>
                          <c:if test="${not empty childrenMap[p.id]}">
                            <small>Có danh mục con</small>
                          </c:if>
                        </span>
                      </div>
                    </td>

                    <td>
                      <span class="admin-category-slug"><c:out value="${p.slug}" /></span>
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${p.tagCount > 0}">
                          <span class="admin-category-tag-count admin-category-tag-count--active">
                            <c:out value="${p.tagCount}" /> thẻ
                          </span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-category-tag-count">Chưa có thẻ</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <span class="admin-pill admin-pill--info">Danh mục cha</span>
                    </td>

                    <td>
                      <span class="admin-count-pill">
                        <c:out value="${empty p.productCount ? 0 : p.productCount}" /> sản phẩm
                      </span>
                    </td>

                    <td class="admin-status-cell">
                      <c:choose>
                        <c:when test="${p.active}">
                          <span class="admin-pill admin-pill--ok">Đang hiển thị</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill admin-pill--warning">Tạm ẩn</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td class="admin-category-action-cell">
                      <div class="admin-category-actions">
                        <a class="admin-btn admin-category-action-btn" href="${ctx}/admin/categories?action=edit&id=${p.id}">
                          Sửa
                        </a>

                        <form method="post"
                              action="${ctx}/admin/categories"
                              class="admin-inline"
                              onsubmit="return confirm('Bạn có chắc muốn xóa danh mục này không? Nếu danh mục đang có sản phẩm hoặc danh mục con thì hệ thống sẽ không cho xóa.')">
                          <%@ include file="/jsp/common/csrf.jspf" %>
                          <input type="hidden" name="action" value="delete">
                          <input type="hidden" name="id" value="${p.id}">
                          <button class="admin-btn admin-btn--danger admin-category-action-btn" type="submit">
                            Xóa
                          </button>
                        </form>
                      </div>
                    </td>
                  </tr>

                  <c:forEach var="ch" items="${childrenMap[p.id]}">
                    <tr class="admin-category-row admin-category-row--child ${not ch.active ? 'admin-category-row--inactive' : ''}">
                      <td class="admin-category-id-cell">
                        <span class="admin-id">#<c:out value="${ch.id}" /></span>
                      </td>

                      <td>
                        <div class="admin-category-name admin-category-name--child">
                          <span class="admin-tree-symbol">↳</span>
                          <span class="admin-category-icon admin-category-icon--child">📁</span>
                          <span>
                            <strong><c:out value="${ch.name}" /></strong>
                            <small>Thuộc: <c:out value="${p.name}" /></small>
                          </span>
                        </div>
                      </td>

                      <td>
                        <span class="admin-category-slug"><c:out value="${ch.slug}" /></span>
                      </td>

                      <td>
                        <c:choose>
                          <c:when test="${ch.tagCount > 0}">
                            <span class="admin-category-tag-count admin-category-tag-count--active">
                              <c:out value="${ch.tagCount}" /> thẻ
                            </span>
                          </c:when>
                          <c:otherwise>
                            <span class="admin-category-tag-count">Chưa có thẻ</span>
                          </c:otherwise>
                        </c:choose>
                      </td>

                      <td>
                        <span class="admin-pill admin-pill--sub">Danh mục con</span>
                      </td>

                      <td>
                        <span class="admin-count-pill">
                          <c:out value="${empty ch.productCount ? 0 : ch.productCount}" /> sản phẩm
                        </span>
                      </td>

                      <td class="admin-status-cell">
                        <c:choose>
                          <c:when test="${ch.active}">
                            <span class="admin-pill admin-pill--ok">Đang hiển thị</span>
                          </c:when>
                          <c:otherwise>
                            <span class="admin-pill admin-pill--warning">Tạm ẩn</span>
                          </c:otherwise>
                        </c:choose>
                      </td>

                      <td class="admin-category-action-cell">
                        <div class="admin-category-actions">
                          <a class="admin-btn admin-category-action-btn" href="${ctx}/admin/categories?action=edit&id=${ch.id}">
                            Sửa
                          </a>

                          <form method="post"
                                action="${ctx}/admin/categories"
                                class="admin-inline"
                                onsubmit="return confirm('Bạn có chắc muốn xóa danh mục này không? Nếu danh mục đang có sản phẩm thì hệ thống sẽ không cho xóa.')">
                            <%@ include file="/jsp/common/csrf.jspf" %>
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="${ch.id}">
                            <button class="admin-btn admin-btn--danger admin-category-action-btn" type="submit">
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
    </section>
  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

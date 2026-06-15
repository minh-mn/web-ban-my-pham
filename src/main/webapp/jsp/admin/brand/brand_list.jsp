<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Quản lý thương hiệu" scope="request" />
<c:set var="activeMenu" value="brands" scope="request" />
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request" />

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="brandTotal" value="${empty brands ? 0 : fn:length(brands)}" />
<c:set var="brandLogoCount" value="0" />
<c:set var="brandNoLogoCount" value="0" />
<c:set var="brandProductTotal" value="0" />

<c:forEach var="brandStat" items="${brands}">
  <c:choose>
    <c:when test="${not empty brandStat.image}">
      <c:set var="brandLogoCount" value="${brandLogoCount + 1}" />
    </c:when>
    <c:otherwise>
      <c:set var="brandNoLogoCount" value="${brandNoLogoCount + 1}" />
    </c:otherwise>
  </c:choose>

  <c:set var="brandProductTotal" value="${brandProductTotal + (empty brandStat.productCount ? 0 : brandStat.productCount)}" />
</c:forEach>

<main class="admin-main">
  <div class="admin-container admin-brand-page">

    <section class="admin-brand-hero">
      <div class="admin-brand-hero__content">
        <span class="admin-brand-eyebrow">SẢN PHẨM &amp; DANH MỤC</span>
        <h1 class="admin-brand-title">Quản lý thương hiệu</h1>
        <p class="admin-brand-subtitle">
          Quản lý danh sách thương hiệu mỹ phẩm, logo đại diện và số lượng sản phẩm đang thuộc từng thương hiệu.
          Dữ liệu thương hiệu sẽ được dùng ở trang sản phẩm, bộ lọc và khu vực thương hiệu nổi bật.
        </p>
      </div>

      <div class="admin-brand-hero__actions">
        <a class="admin-btn admin-btn--primary" href="${ctx}/admin/brands?action=new">
          + Thêm thương hiệu
        </a>
      </div>
    </section>

    <section class="admin-brand-summary">
      <div class="admin-brand-stat admin-brand-stat--total">
        <span class="admin-brand-stat__icon">🏷️</span>
        <span class="admin-brand-stat__label">Tổng thương hiệu</span>
        <strong class="admin-brand-stat__value">
          <c:out value="${brandTotal}" />
        </strong>
        <span class="admin-brand-stat__note">Tất cả thương hiệu trong hệ thống</span>
      </div>

      <div class="admin-brand-stat admin-brand-stat--logo">
        <span class="admin-brand-stat__icon">🖼️</span>
        <span class="admin-brand-stat__label">Đã có logo</span>
        <strong class="admin-brand-stat__value">
          <c:out value="${brandLogoCount}" />
        </strong>
        <span class="admin-brand-stat__note">Thương hiệu có hình đại diện</span>
      </div>

      <div class="admin-brand-stat admin-brand-stat--missing">
        <span class="admin-brand-stat__icon">⚠️</span>
        <span class="admin-brand-stat__label">Thiếu logo</span>
        <strong class="admin-brand-stat__value">
          <c:out value="${brandNoLogoCount}" />
        </strong>
        <span class="admin-brand-stat__note">Nên bổ sung để hiển thị đẹp hơn</span>
      </div>

      <div class="admin-brand-stat admin-brand-stat--product">
        <span class="admin-brand-stat__icon">🧴</span>
        <span class="admin-brand-stat__label">Sản phẩm liên kết</span>
        <strong class="admin-brand-stat__value">
          <c:out value="${brandProductTotal}" />
        </strong>
        <span class="admin-brand-stat__note">Tổng sản phẩm theo thương hiệu</span>
      </div>
    </section>

    <section class="admin-card admin-brand-list-card">
      <div class="admin-card__body">
        <div class="admin-brand-section-head">
          <div>
            <h2 class="admin-brand-section-title">Danh sách thương hiệu</h2>
            <p class="admin-brand-section-desc">
              Kiểm tra logo, tên thương hiệu, số sản phẩm liên kết và thao tác chỉnh sửa/xóa.
            </p>
          </div>

          <span class="admin-chip admin-chip--brand">
            <c:out value="${brandTotal}" /> thương hiệu
          </span>
        </div>

        <c:choose>
          <c:when test="${empty brands}">
            <div class="admin-brand-empty">
              <div class="admin-brand-empty__icon">🏷️</div>
              <div>
                <h3>Chưa có thương hiệu nào</h3>
                <p>Hãy thêm thương hiệu đầu tiên để quản lý sản phẩm và bộ lọc thương hiệu tốt hơn.</p>
                <a class="admin-btn admin-btn--primary" href="${ctx}/admin/brands?action=new">
                  + Thêm thương hiệu
                </a>
              </div>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-brand-table-wrap">
              <table class="admin-table admin-brand-table">
                <thead>
                <tr>
                  <th class="admin-brand-col-id">ID</th>
                  <th class="admin-brand-col-info">Thương hiệu</th>
                  <th class="admin-brand-col-logo">Logo</th>
                  <th class="admin-brand-col-products">Sản phẩm</th>
                  <th class="admin-brand-col-actions">Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="b" items="${brands}">
                  <tr class="${empty b.image ? 'admin-brand-row--missing-logo' : ''}">
                    <td class="admin-brand-id-cell">
                      <strong>#<c:out value="${b.id}" /></strong>
                    </td>

                    <td>
                      <div class="admin-brand-info">
                        <strong class="admin-brand-name">
                          <c:out value="${b.name}" />
                        </strong>

                        <c:choose>
                          <c:when test="${not empty b.image}">
                            <span class="admin-pill admin-pill--ok">Đã có logo</span>
                          </c:when>
                          <c:otherwise>
                            <span class="admin-pill admin-pill--warning">Thiếu logo</span>
                          </c:otherwise>
                        </c:choose>

                        <c:if test="${not empty b.image}">
                          <div class="admin-path admin-brand-path">
                            <c:out value="${b.image}" />
                          </div>
                        </c:if>
                      </div>
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${not empty b.image}">
                          <c:choose>
                            <c:when test="${fn:startsWith(b.image, 'http://')
                                           || fn:startsWith(b.image, 'https://')
                                           || fn:startsWith(b.image, 'data:')}">
                              <img
                                      class="admin-brand-logo"
                                      src="${b.image}"
                                      alt="Logo thương hiệu">
                            </c:when>
                            <c:otherwise>
                              <img
                                      class="admin-brand-logo"
                                      src="${ctx}${b.image}"
                                      alt="Logo thương hiệu">
                            </c:otherwise>
                          </c:choose>
                        </c:when>

                        <c:otherwise>
                          <div class="admin-brand-logo admin-brand-logo--empty">
                            Logo
                          </div>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td class="admin-brand-product-cell">
                      <span class="admin-brand-product-pill">
                        <c:out value="${empty b.productCount ? 0 : b.productCount}" /> sản phẩm
                      </span>
                    </td>

                    <td class="admin-brand-action-cell">
                      <div class="admin-brand-actions">
                        <a
                                class="admin-btn admin-brand-action-btn"
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

                          <button class="admin-btn admin-btn--danger admin-brand-action-btn" type="submit">
                            Xóa
                          </button>
                        </form>
                      </div>
                    </td>
                  </tr>
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

<jsp:include page="/jsp/admin/layout/footer.jsp" />

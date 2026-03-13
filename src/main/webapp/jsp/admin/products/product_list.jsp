<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:set var="pageTitle" value="ADMIN | Products" scope="request" />
<c:set var="activeMenu" value="products" scope="request" />
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request" />

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Sản phẩm</h1>
        <p class="admin-subtext">Quản lý danh sách sản phẩm trong hệ thống.</p>
      </div>

      <a class="admin-btn admin-btn--primary"
         href="${pageContext.request.contextPath}/admin/products?action=new">
        + Thêm sản phẩm
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <!-- TOOLBAR -->
        <div class="admin-toolbar">
          <form method="get"
                action="${pageContext.request.contextPath}/admin/products"
                class="admin-toolbar__form">

            <input class="admin-input" type="text" name="keyword"
                   placeholder="Tìm theo tên/mô tả..." value="${param.keyword}">

            <select class="admin-select" name="sort">
              <option value="" ${empty param.sort ? "selected" : ""}>Mới nhất</option>
              <option value="price_asc"  ${param.sort == 'price_asc' ? 'selected' : ''}>Giá tăng dần</option>
              <option value="price_desc" ${param.sort == 'price_desc' ? 'selected' : ''}>Giá giảm dần</option>
            </select>

            <button class="admin-btn" type="submit">Lọc</button>

            <c:if test="${not empty param.keyword || not empty param.sort}">
              <a class="admin-btn"
                 href="${pageContext.request.contextPath}/admin/products">Xóa lọc</a>
            </c:if>

          </form>
        </div>

        <c:choose>
          <c:when test="${empty products}">
            <div class="admin-empty">Chưa có sản phẩm.</div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
              <tr>
                <th style="width: 90px;">ID</th>
                <th>Sản phẩm</th>
                <th style="width: 190px;">Giá</th>
                <th style="width: 140px;">Kho</th>
                <th style="width: 140px;">Đánh giá</th>
                <th style="width: 140px;">Trạng thái</th>
                <th style="width: 220px;">Thao tác</th>
              </tr>
              </thead>

              <tbody>
              <c:forEach var="p" items="${products}">
                <tr>
                  <td>#${p.id}</td>

                  <td>
                    <div class="admin-media">
                      <c:choose>
                        <c:when test="${not empty p.imageUrl}">
                          <img class="admin-thumb"
                               src="${pageContext.request.contextPath}${p.imageUrl}"
                               alt="${p.title}">
                        </c:when>
                        <c:otherwise>
                          <div class="admin-thumb"></div>
                        </c:otherwise>
                      </c:choose>

                      <div class="admin-media__meta">
                        <div style="font-weight: 850;">
                          <c:out value="${p.title}" />
                        </div>
                        <div class="admin-path">
                          <c:out value="${p.slug}" />
                        </div>
                      </div>
                    </div>
                  </td>

                  <td>
                    <c:choose>
                      <c:when test="${p.hasDiscount}">
                        <div style="font-weight: 850;">
                          <fmt:formatNumber value="${p.finalPrice}" type="number"
                                            groupingUsed="true" minFractionDigits="0"
                                            maxFractionDigits="0" /> ₫
                        </div>
                        <div class="admin-muted" style="font-size: 12px;">
                          <fmt:formatNumber value="${p.price}" type="number"
                                            groupingUsed="true" minFractionDigits="0"
                                            maxFractionDigits="0" />
                          ₫ (-${p.discountPercent}%)
                        </div>
                      </c:when>
                      <c:otherwise>
                        <strong>
                          <fmt:formatNumber value="${p.price}" type="number"
                                            groupingUsed="true" minFractionDigits="0"
                                            maxFractionDigits="0" /> ₫
                        </strong>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td>
                    <c:choose>
                      <c:when test="${p.outOfStock}">
                        <span class="admin-pill admin-pill--danger">Hết hàng</span>
                      </c:when>
                      <c:when test="${p.lowStock}">
                        <span class="admin-pill">Sắp hết (${p.stock})</span>
                      </c:when>
                      <c:otherwise>
                        <span class="admin-pill admin-pill--ok">Còn (${p.stock})</span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td>
                    <span style="font-weight: 800;">
                      <fmt:formatNumber value="${p.avgRating}" type="number"
                                        minFractionDigits="1" maxFractionDigits="1" />
                    </span>
                    <span class="admin-muted">★</span>
                    <span class="admin-muted">(${p.reviewCount})</span>
                  </td>

                  <td class="admin-status-cell">
                    <c:choose>
                      <c:when test="${p.active}">
                        <span class="admin-pill admin-pill--ok">ACTIVE</span>
                      </c:when>
                      <c:otherwise>
                        <span class="admin-pill admin-pill--danger">INACTIVE</span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td class="product-actions">
                    <div class="product-actions-inner">

                      <a class="admin-btn"
                         href="${pageContext.request.contextPath}/admin/products?action=edit&id=${p.id}">
                        Sửa
                      </a>

                      <!-- ✅ XÓA -->
                      <form method="post"
                            action="${pageContext.request.contextPath}/admin/products"
                            class="admin-inline"
                            style="display:inline;"
                            onsubmit="return confirm('Xóa sản phẩm #${p.id}? Sản phẩm sẽ chuyển sang INACTIVE.');">

                        <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
                        <%@ include file="/jsp/common/csrf.jspf" %>

                        <input type="hidden" name="action" value="delete"/>
                        <input type="hidden" name="id" value="${p.id}"/>
                        <button class="admin-btn admin-btn--danger" type="submit">Xóa</button>
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

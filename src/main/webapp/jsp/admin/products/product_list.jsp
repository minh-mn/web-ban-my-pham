<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Products" scope="request" />
<c:set var="activeMenu" value="products" scope="request" />
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request" />

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="productTotal" value="${empty products ? 0 : fn:length(products)}" />
<c:set var="productActiveCount" value="0" />
<c:set var="productInactiveCount" value="0" />
<c:set var="productLowStockCount" value="0" />
<c:set var="productOutOfStockCount" value="0" />

<c:forEach var="productStat" items="${products}">
  <c:choose>
    <c:when test="${productStat.active}">
      <c:set var="productActiveCount" value="${productActiveCount + 1}" />
    </c:when>
    <c:otherwise>
      <c:set var="productInactiveCount" value="${productInactiveCount + 1}" />
    </c:otherwise>
  </c:choose>

  <c:if test="${productStat.lowStock}">
    <c:set var="productLowStockCount" value="${productLowStockCount + 1}" />
  </c:if>

  <c:if test="${productStat.outOfStock}">
    <c:set var="productOutOfStockCount" value="${productOutOfStockCount + 1}" />
  </c:if>
</c:forEach>

<main class="admin-main">
  <div class="admin-container admin-product-page">

    <section class="admin-product-hero">
      <div class="admin-product-hero__content">
        <span class="admin-product-eyebrow">SẢN PHẨM &amp; DANH MỤC</span>
        <h1 class="admin-product-title">Quản lý sản phẩm</h1>
        <p class="admin-product-subtitle">
          Quản lý danh sách sản phẩm, giá bán, tồn kho, trạng thái hiển thị, ảnh đại diện và đánh giá.
          Các sản phẩm tạm ẩn vẫn được admin theo dõi nhưng không hiển thị cho khách hàng.
        </p>
      </div>

      <div class="admin-product-hero__actions">
        <a class="admin-btn admin-btn--primary"
           href="${ctx}/admin/products?action=new">
          + Thêm sản phẩm
        </a>
      </div>
    </section>

    <section class="admin-product-summary">
      <div class="admin-product-stat admin-product-stat--total">
        <span class="admin-product-stat__icon">🧴</span>
        <span class="admin-product-stat__label">Tổng sản phẩm</span>
        <strong class="admin-product-stat__value">
          <c:out value="${productTotal}" />
        </strong>
        <span class="admin-product-stat__note">Theo kết quả lọc hiện tại</span>
      </div>

      <div class="admin-product-stat admin-product-stat--active">
        <span class="admin-product-stat__icon">✅</span>
        <span class="admin-product-stat__label">Đang hiển thị</span>
        <strong class="admin-product-stat__value">
          <c:out value="${productActiveCount}" />
        </strong>
        <span class="admin-product-stat__note">Sản phẩm ACTIVE</span>
      </div>

      <div class="admin-product-stat admin-product-stat--inactive">
        <span class="admin-product-stat__icon">🙈</span>
        <span class="admin-product-stat__label">Tạm ẩn</span>
        <strong class="admin-product-stat__value">
          <c:out value="${productInactiveCount}" />
        </strong>
        <span class="admin-product-stat__note">Không hiển thị cho khách</span>
      </div>

      <div class="admin-product-stat admin-product-stat--stock">
        <span class="admin-product-stat__icon">📦</span>
        <span class="admin-product-stat__label">Cảnh báo kho</span>
        <strong class="admin-product-stat__value">
          <c:out value="${productLowStockCount + productOutOfStockCount}" />
        </strong>
        <span class="admin-product-stat__note">
          <c:out value="${productLowStockCount}" /> sắp hết, <c:out value="${productOutOfStockCount}" /> hết hàng
        </span>
      </div>
    </section>

    <section class="admin-card admin-product-filter-card admin-product-filter-card--full">
      <div class="admin-card__body">
        <div class="admin-product-section-head admin-product-section-head--filter">
          <div>
            <h2 class="admin-product-section-title">Bộ lọc sản phẩm</h2>
            <p class="admin-product-section-desc">
              Lọc đầy đủ theo từ khóa, danh mục, thương hiệu, trạng thái hiển thị, tồn kho và kiểu sắp xếp.
            </p>
          </div>

          <c:if test="${not empty param.keyword
                      || not empty param.categoryId
                      || not empty param.brandId
                      || not empty param.activeStatus
                      || not empty param.stockStatus
                      || not empty param.sort}">
            <div class="admin-product-active-filters">
              <c:if test="${not empty param.keyword}">
                <span class="admin-chip admin-chip--brand">
                  Từ khóa: <c:out value="${param.keyword}" />
                </span>
              </c:if>

              <c:if test="${not empty param.categoryId}">
                <span class="admin-chip admin-chip--brand">
                  Danh mục #<c:out value="${param.categoryId}" />
                </span>
              </c:if>

              <c:if test="${not empty param.brandId}">
                <span class="admin-chip admin-chip--brand">
                  Thương hiệu #<c:out value="${param.brandId}" />
                </span>
              </c:if>

              <c:if test="${not empty param.activeStatus}">
                <span class="admin-chip admin-chip--brand">
                  Hiển thị:
                  <c:choose>
                    <c:when test="${param.activeStatus == 'active'}">Đang hiển thị</c:when>
                    <c:when test="${param.activeStatus == 'hidden'}">Tạm ẩn</c:when>
                    <c:otherwise>Tất cả</c:otherwise>
                  </c:choose>
                </span>
              </c:if>

              <c:if test="${not empty param.stockStatus}">
                <span class="admin-chip admin-chip--brand">
                  Tồn kho:
                  <c:choose>
                    <c:when test="${param.stockStatus == 'in_stock'}">Còn hàng</c:when>
                    <c:when test="${param.stockStatus == 'low_stock'}">Sắp hết</c:when>
                    <c:when test="${param.stockStatus == 'out_stock'}">Hết hàng</c:when>
                    <c:otherwise>Tất cả</c:otherwise>
                  </c:choose>
                </span>
              </c:if>

              <c:if test="${not empty param.sort}">
                <span class="admin-chip admin-chip--brand">
                  Sắp xếp:
                  <c:choose>
                    <c:when test="${param.sort == 'price_asc'}">Giá tăng dần</c:when>
                    <c:when test="${param.sort == 'price_desc'}">Giá giảm dần</c:when>
                    <c:when test="${param.sort == 'stock_asc'}">Kho thấp trước</c:when>
                    <c:when test="${param.sort == 'stock_desc'}">Kho cao trước</c:when>
                    <c:when test="${param.sort == 'name_asc'}">Tên A-Z</c:when>
                    <c:when test="${param.sort == 'name_desc'}">Tên Z-A</c:when>
                    <c:otherwise>Mới nhất</c:otherwise>
                  </c:choose>
                </span>
              </c:if>
            </div>
          </c:if>
        </div>

        <form method="get"
              action="${ctx}/admin/products"
              class="admin-product-filter-form admin-product-filter-form--full">
          <label class="admin-product-filter-field admin-product-filter-field--keyword">
            <span>Từ khóa</span>
            <input class="admin-input"
                   type="text"
                   name="keyword"
                   placeholder="Tìm theo tên, slug hoặc mô tả..."
                   value="${param.keyword}">
          </label>

          <label class="admin-product-filter-field">
            <span>Danh mục</span>
            <select class="admin-select" name="categoryId">
              <option value="">Tất cả danh mục</option>
              <c:forEach var="cat" items="${categories}">
                <option value="${cat.id}" ${param.categoryId == cat.id ? "selected" : ""}>
                  <c:if test="${cat.parentId != null}">↳ </c:if>
                  <c:out value="${cat.name}" />
                  <c:if test="${cat.parentId == null}"> (Cha)</c:if>
                </option>
              </c:forEach>
            </select>
          </label>

          <label class="admin-product-filter-field">
            <span>Thương hiệu</span>
            <select class="admin-select" name="brandId">
              <option value="">Tất cả thương hiệu</option>
              <c:forEach var="b" items="${brands}">
                <option value="${b.id}" ${param.brandId == b.id ? "selected" : ""}>
                  <c:out value="${b.name}" />
                </option>
              </c:forEach>
            </select>
          </label>

          <label class="admin-product-filter-field">
            <span>Hiển thị</span>
            <select class="admin-select" name="activeStatus">
              <option value="" ${empty param.activeStatus ? "selected" : ""}>Tất cả trạng thái</option>
              <option value="active" ${param.activeStatus == 'active' ? 'selected' : ''}>Đang hiển thị</option>
              <option value="hidden" ${param.activeStatus == 'hidden' ? 'selected' : ''}>Tạm ẩn</option>
            </select>
          </label>

          <label class="admin-product-filter-field">
            <span>Tồn kho</span>
            <select class="admin-select" name="stockStatus">
              <option value="" ${empty param.stockStatus ? "selected" : ""}>Tất cả tồn kho</option>
              <option value="in_stock" ${param.stockStatus == 'in_stock' ? 'selected' : ''}>Còn hàng (&gt; 10)</option>
              <option value="low_stock" ${param.stockStatus == 'low_stock' ? 'selected' : ''}>Sắp hết (1 - 10)</option>
              <option value="out_stock" ${param.stockStatus == 'out_stock' ? 'selected' : ''}>Hết hàng</option>
            </select>
          </label>

          <label class="admin-product-filter-field">
            <span>Sắp xếp</span>
            <select class="admin-select" name="sort">
              <option value="" ${empty param.sort ? "selected" : ""}>Mới nhất</option>
              <option value="price_asc" ${param.sort == 'price_asc' ? 'selected' : ''}>Giá tăng dần</option>
              <option value="price_desc" ${param.sort == 'price_desc' ? 'selected' : ''}>Giá giảm dần</option>
              <option value="stock_asc" ${param.sort == 'stock_asc' ? 'selected' : ''}>Kho thấp trước</option>
              <option value="stock_desc" ${param.sort == 'stock_desc' ? 'selected' : ''}>Kho cao trước</option>
              <option value="name_asc" ${param.sort == 'name_asc' ? 'selected' : ''}>Tên A-Z</option>
              <option value="name_desc" ${param.sort == 'name_desc' ? 'selected' : ''}>Tên Z-A</option>
            </select>
          </label>

          <div class="admin-product-filter-actions">
            <button class="admin-btn admin-btn--primary admin-product-filter-btn" type="submit">
              Lọc sản phẩm
            </button>

            <a class="admin-btn admin-product-filter-btn admin-product-filter-reset"
               href="${ctx}/admin/products">
              Xóa lọc
            </a>
          </div>
        </form>
      </div>
    </section>

    <section class="admin-card admin-product-list-card">
      <div class="admin-card__body">
        <div class="admin-product-section-head admin-product-section-head--list">
          <div>
            <h2 class="admin-product-section-title">Danh sách sản phẩm</h2>
            <p class="admin-product-section-desc">
              Hiển thị thông tin sản phẩm, giá bán, tồn kho, đánh giá và trạng thái.
            </p>
          </div>

          <span class="admin-chip admin-chip--brand">
            <c:out value="${productTotal}" /> sản phẩm
          </span>
        </div>

        <c:choose>
          <c:when test="${empty products}">
            <div class="admin-product-empty">
              <div class="admin-product-empty__icon">🧴</div>
              <div>
                <h3>Chưa có sản phẩm</h3>
                <p>Thêm sản phẩm đầu tiên để bắt đầu quản lý danh mục mỹ phẩm của cửa hàng.</p>
                <a class="admin-btn admin-btn--primary"
                   href="${ctx}/admin/products?action=new">
                  + Thêm sản phẩm
                </a>
              </div>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-product-table-wrap">
              <table class="admin-table admin-product-table">
                <thead>
                <tr>
                  <th class="admin-product-col-id">ID</th>
                  <th class="admin-product-col-product">Sản phẩm</th>
                  <th class="admin-product-col-price">Giá</th>
                  <th class="admin-product-col-stock">Kho</th>
                  <th class="admin-product-col-review">Đánh giá</th>
                  <th class="admin-product-col-status">Trạng thái</th>
                  <th class="admin-product-col-actions">Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="p" items="${products}">
                  <tr class="${!p.active ? 'admin-product-row--inactive' : ''} ${p.outOfStock ? 'admin-product-row--out' : ''}">
                    <td class="admin-product-id-cell">
                      #<c:out value="${p.id}" />
                    </td>

                    <td>
                      <div class="admin-product-media">
                        <c:choose>
                          <c:when test="${not empty p.imageUrl}">
                            <img class="admin-product-thumb"
                                 src="${ctx}${p.imageUrl}"
                                 alt="${p.title}">
                          </c:when>
                          <c:otherwise>
                            <div class="admin-product-thumb admin-product-thumb--empty">
                              No image
                            </div>
                          </c:otherwise>
                        </c:choose>

                        <div class="admin-product-media__meta">
                          <strong class="admin-product-name">
                            <c:out value="${p.title}" />
                          </strong>
                          <code class="admin-product-slug">
                            <c:out value="${p.slug}" />
                          </code>
                        </div>
                      </div>
                    </td>

                    <td>
                      <div class="admin-product-price">
                        <c:choose>
                          <c:when test="${p.hasDiscount}">
                            <strong>
                              <fmt:formatNumber value="${p.finalPrice}"
                                                type="number"
                                                groupingUsed="true"
                                                minFractionDigits="0"
                                                maxFractionDigits="0" /> ₫
                            </strong>
                            <span>
                              <fmt:formatNumber value="${p.price}"
                                                type="number"
                                                groupingUsed="true"
                                                minFractionDigits="0"
                                                maxFractionDigits="0" />
                              ₫ (-<c:out value="${p.discountPercent}" />%)
                            </span>
                          </c:when>
                          <c:otherwise>
                            <strong>
                              <fmt:formatNumber value="${p.price}"
                                                type="number"
                                                groupingUsed="true"
                                                minFractionDigits="0"
                                                maxFractionDigits="0" /> ₫
                            </strong>
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${p.outOfStock}">
                          <span class="admin-pill admin-pill--danger">Hết hàng</span>
                        </c:when>
                        <c:when test="${p.lowStock}">
                          <span class="admin-pill admin-pill--warning">Sắp hết (<c:out value="${p.stock}" />)</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill admin-pill--ok">Còn (<c:out value="${p.stock}" />)</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <div class="admin-product-rating">
                        <strong>
                          <fmt:formatNumber value="${p.avgRating}"
                                            type="number"
                                            minFractionDigits="1"
                                            maxFractionDigits="1" />
                        </strong>
                        <span>★</span>
                        <small>(<c:out value="${p.reviewCount}" />)</small>
                      </div>
                    </td>

                    <td class="admin-status-cell">
                      <c:choose>
                        <c:when test="${p.active}">
                          <span class="admin-pill admin-pill--ok">Đang hiển thị</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill admin-pill--danger">Tạm ẩn</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td class="admin-product-action-cell">
                      <div class="admin-product-actions">
                        <a class="admin-btn admin-product-action-btn"
                           href="${ctx}/admin/products?action=edit&id=${p.id}">
                          Sửa
                        </a>

                        <c:choose>
                          <c:when test="${p.active}">
                            <form method="post"
                                  action="${ctx}/admin/products"
                                  class="admin-inline"
                                  onsubmit="return confirm('Ẩn sản phẩm #${p.id}? Sản phẩm sẽ không hiển thị cho khách hàng.');">
                              <%@ include file="/jsp/common/csrf.jspf" %>

                              <input type="hidden" name="action" value="hide"/>
                              <input type="hidden" name="id" value="${p.id}"/>
                              <button class="admin-btn admin-btn--danger admin-product-action-btn" type="submit">
                                Ẩn
                              </button>
                            </form>
                          </c:when>

                          <c:otherwise>
                            <form method="post"
                                  action="${ctx}/admin/products"
                                  class="admin-inline"
                                  onsubmit="return confirm('Mở khóa sản phẩm #${p.id} và hiển thị lại cho khách hàng?');">
                              <%@ include file="/jsp/common/csrf.jspf" %>

                              <input type="hidden" name="action" value="show"/>
                              <input type="hidden" name="id" value="${p.id}"/>
                              <button class="admin-btn admin-btn--primary admin-product-action-btn" type="submit">
                                Mở khóa
                              </button>
                            </form>
                          </c:otherwise>
                        </c:choose>
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

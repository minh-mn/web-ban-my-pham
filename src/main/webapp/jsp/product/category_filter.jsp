<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form method="get" action="${pageContext.request.contextPath}/products" class="sidebar-filter" id="filterForm">

  <h3 class="filter-title">Danh mục</h3>

  <c:forEach var="parent" items="${categories}">
    <div class="category-group ${not empty selectedCategoryList ? 'open' : ''}">

      <div class="category-parent"
           onclick="this.parentElement.classList.toggle('open')">
        <span>
          ${parent.name}
          <c:if test="${parent.productCount > 0}">
            <span class="cat-count-parent">(${parent.productCount})</span>
          </c:if>
        </span>
        <span class="arrow">▾</span>
      </div>

      <c:if test="${not empty parent.children}">
        <ul class="category-children">
          <c:forEach var="child" items="${parent.children}">
            <li>
              <label class="category-item" style="display: flex; align-items: center; gap: 8px; padding: 5px 0; cursor: pointer;">
                <input type="checkbox"
                       name="category"
                       value="${child.id}"
                       <c:if test="${fn:contains(selectedCategoryList, child.id)}">checked</c:if>>
                <span class="cat-name">${child.name}</span>
                <span class="cat-count">(${child.productCount})</span>
              </label>
            </li>
          </c:forEach>
        </ul>
      </c:if>

    </div>
  </c:forEach>

  <h3 class="filter-title">Khoảng giá</h3>
  <div class="price-pill-group">
    <label class="price-pill">
      <input type="checkbox" name="priceRange" value="lt500"
      ${fn:contains(priceRangeList, 'lt500') ? 'checked' : ''}>
      <span>0 – 500.000 ₫</span>
    </label>

    <label class="price-pill">
      <input type="checkbox" name="priceRange" value="500_1000"
      ${fn:contains(priceRangeList, '500_1000') ? 'checked' : ''}>
      <span>500.000 – 1.000.000 ₫</span>
    </label>

    <label class="price-pill">
      <input type="checkbox" name="priceRange" value="gt1000"
      ${fn:contains(priceRangeList, 'gt1000') ? 'checked' : ''}>
      <span>Trên 1.000.000 ₫</span>
    </label>
  </div>

  <h3 class="filter-title">Thương hiệu</h3>
  <c:forEach var="brand" items="${brands}">
    <label class="brand-item">
      <input type="checkbox"
             name="brand"
             value="${brand.id}"
             <c:if test="${fn:contains(selectedBrandList, brand.id)}">checked</c:if>>
        ${brand.name}
      <span class="count">(${brand.productCount})</span>
    </label>
  </c:forEach>

  <div class="filter-actions" style="margin-top: 20px;">
    <button type="submit" class="btn-apply-filter"
            style="width: 100%; padding: 10px; cursor: pointer;">
      Áp dụng bộ lọc
    </button>
  </div>

  <div class="filter-reset-wrap">
    <a href="${pageContext.request.contextPath}/products" class="btn-reset-filter">
      ⟲ Xóa tất cả bộ lọc
    </a>
  </div>
</form>

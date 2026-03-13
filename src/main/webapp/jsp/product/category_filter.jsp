<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<form method="get"
      action="${pageContext.request.contextPath}/products"
      class="sidebar-filter"
      id="filterForm">

  <!-- ================= CATEGORY ================= -->
  <h3 class="filter-title">Danh mục</h3>

  <c:forEach var="parent" items="${categories}">
    <div class="category-group ${not empty selectedCategory ? 'open' : ''}">

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
              <button type="submit"
                      name="category"
                      value="${child.id}"
                      class="category-link ${selectedCategory == child.id ? 'active' : ''}">
                <span class="cat-name">${child.name}</span>
                <span class="cat-count">(${child.productCount})</span>
              </button>
            </li>
          </c:forEach>
        </ul>
      </c:if>

    </div>
  </c:forEach>

  <!-- ================= PRICE RANGE ================= -->
  <h3 class="filter-title">Khoảng giá</h3>

  <div class="price-pill-group">

    <label class="price-pill">
      <input type="radio"
             name="priceRange"
             value="lt500"
             onchange="this.form.submit()"
             <c:if test="${priceRange == 'lt500'}">checked</c:if>>
      <span>0 – 500.000 ₫</span>
    </label>

    <label class="price-pill">
      <input type="radio"
             name="priceRange"
             value="500_1000"
             onchange="this.form.submit()"
             <c:if test="${priceRange == '500_1000'}">checked</c:if>>
      <span>500.000 – 1.000.000 ₫</span>
    </label>

    <label class="price-pill">
      <input type="radio"
             name="priceRange"
             value="gt1000"
             onchange="this.form.submit()"
             <c:if test="${priceRange == 'gt1000'}">checked</c:if>>
      <span>Trên 1.000.000 ₫</span>
    </label>

  </div>

  <!-- ================= BRAND ================= -->
  <h3 class="filter-title">Thương hiệu</h3>

  <c:forEach var="brand" items="${brands}">
    <label class="brand-item">
      <input type="radio"
             name="brand"
             value="${brand.id}"
             onchange="this.form.submit()"
             <c:if test="${selectedBrand == brand.id}">checked</c:if>>
      ${brand.name}
      <span class="count">(${brand.productCount})</span>
    </label>
  </c:forEach>

  <!-- ================= RESET ================= -->
  <div class="filter-reset-wrap">
    <a href="${pageContext.request.contextPath}/products" class="btn-reset-filter">
      ⟲ Xóa tất cả bộ lọc
    </a>
  </div>

</form>

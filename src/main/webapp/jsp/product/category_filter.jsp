<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<style>
  /* Scoped filter CSS: đặt trực tiếp trong JSP để tránh cache CSS cũ làm bung layout */
  .mc-left-filter,
  .mc-left-filter * {
    box-sizing: border-box;
  }

  .mc-left-filter {
    width: 100%;
    display: block !important;
    color: #2b2d32;
    font-family: inherit;
  }

  .mc-left-filter__section {
    display: block !important;
    padding: 0 0 24px;
    margin: 0 0 28px;
    border-bottom: 1px solid #ededed;
  }

  .mc-left-filter__title {
    width: 100%;
    border: 0;
    outline: 0;
    background: transparent;
    padding: 0;
    margin: 0 0 18px;
    display: flex !important;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    cursor: pointer;
    color: #27272a;
    text-align: left;
    font-size: 18px;
    line-height: 1.25;
    font-weight: 900;
    text-transform: uppercase;
    letter-spacing: .2px;
  }

  .mc-left-filter__chevron {
    width: 22px;
    height: 22px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex: 0 0 22px;
    color: #262626;
    font-size: 18px;
    line-height: 1;
    transform: rotate(0deg);
    transition: transform .15s ease;
  }

  .mc-left-filter__section.is-collapsed .mc-left-filter__chevron {
    transform: rotate(180deg);
  }

  .mc-left-filter__body {
    display: grid !important;
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .mc-left-filter__section.is-collapsed .mc-left-filter__body {
    display: none !important;
  }

  .mc-left-filter__group {
    margin: 0 0 4px;
    color: #a30624;
    font-size: 14px;
    font-weight: 900;
    text-transform: uppercase;
    letter-spacing: .7px;
  }

  .mc-left-filter__check {
    position: relative;
    min-height: 24px;
    display: flex !important;
    align-items: center;
    gap: 14px;
    cursor: pointer;
    color: #34363b;
    font-size: 17px;
    font-weight: 500;
    line-height: 1.35;
    text-align: left;
    white-space: normal;
  }

  .mc-left-filter__check:hover {
    color: #a30624;
  }

  .mc-left-filter__check input {
    position: absolute;
    opacity: 0;
    width: 1px;
    height: 1px;
    pointer-events: none;
  }

  .mc-left-filter__box {
    width: 22px;
    height: 22px;
    min-width: 22px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border: 1.4px solid #b7b7b7;
    background: #fff;
  }

  .mc-left-filter__box::after {
    content: "";
    width: 10px;
    height: 10px;
    display: block;
    background: linear-gradient(135deg, #f0528b, #970519);
    transform: scale(0);
    transition: transform .12s ease;
  }

  .mc-left-filter__check input:checked + .mc-left-filter__box {
    border-color: #a30624;
    box-shadow: 0 0 0 3px rgba(163, 6, 36, .10);
  }

  .mc-left-filter__check input:checked + .mc-left-filter__box::after {
    transform: scale(1);
  }

  .mc-left-filter__check input:checked ~ .mc-left-filter__name {
    color: #a30624;
    font-weight: 700;
  }

  .mc-left-filter__name {
    flex: 1 1 auto;
    min-width: 0;
  }

  .mc-left-filter__count {
    margin-left: auto;
    color: #9ca3af;
    font-size: 13px;
    font-weight: 700;
  }

  .mc-left-filter__price-row {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) 24px minmax(0, 1fr);
    align-items: center;
    gap: 14px;
    margin-bottom: 4px;
  }

  .mc-left-filter__price-input {
    width: 100%;
    height: 56px;
    border: 1px solid #e1e1e1;
    background: #fff;
    color: #27272a;
    font-size: 18px;
    font-weight: 500;
    text-align: center;
    outline: none;
    border-radius: 0;
  }

  .mc-left-filter__dash {
    height: 1px;
    background: #969696;
  }

  .mc-left-filter__price-options {
    display: grid !important;
    grid-template-columns: 1fr;
    gap: 10px;
    margin-top: 4px;
  }

  .mc-left-filter__price-options .mc-left-filter__check {
    font-size: 15px;
    color: #51535a;
  }

  .mc-left-filter__apply {
    width: 100%;
    height: 62px;
    border: 0;
    border-radius: 0;
    background: linear-gradient(135deg, #cf183d 0%, #970519 100%);
    color: #fff;
    cursor: pointer;
    font-size: 20px;
    font-weight: 850;
    margin-top: 12px;
  }

  .mc-left-filter__apply:hover {
    filter: brightness(1.04);
  }

  .mc-left-filter__brand-scroll {
    max-height: 620px;
    overflow-y: auto;
    padding-right: 10px;
    scrollbar-width: thin;
    scrollbar-color: rgba(151, 5, 25, .45) transparent;
  }

  .mc-left-filter__brand-scroll::-webkit-scrollbar {
    width: 5px;
  }

  .mc-left-filter__brand-scroll::-webkit-scrollbar-track {
    background: transparent;
  }

  .mc-left-filter__brand-scroll::-webkit-scrollbar-thumb {
    background: rgba(151, 5, 25, .45);
    border-radius: 999px;
  }

  .mc-left-filter__reset {
    color: #a30624;
    text-decoration: none;
    font-size: 15px;
    font-weight: 700;
  }
</style>

<form method="get"
      action="${ctx}/products#collectionResults"
      class="mc-left-filter"
      id="filterForm">

  <c:if test="${not empty param.q}">
    <input type="hidden" name="q" value="${fn:escapeXml(param.q)}">
  </c:if>

  <c:if test="${not empty param.sort}">
    <input type="hidden" name="sort" value="${fn:escapeXml(param.sort)}">
  </c:if>

  <c:set var="primarySelected" value="false" />
  <c:if test="${not empty primaryCategory}">
    <c:forEach var="selectedId" items="${selectedCategoryList}">
      <c:if test="${selectedId == primaryCategory.id}">
        <c:set var="primarySelected" value="true" />
      </c:if>
    </c:forEach>
  </c:if>

  <c:if test="${primarySelected}">
    <input type="hidden" name="category" value="${primaryCategory.id}">
  </c:if>

  <div class="mc-left-filter__section">
    <button type="button"
            class="mc-left-filter__title"
            onclick="this.closest('.mc-left-filter__section').classList.toggle('is-collapsed')">
      <span>Danh mục sản phẩm</span>
      <span class="mc-left-filter__chevron">⌃</span>
    </button>

    <div class="mc-left-filter__body">
      <c:choose>
        <c:when test="${not empty primaryCategory and not empty primaryCategory.children}">
          <c:forEach var="child" items="${primaryCategory.children}">
            <c:set var="checked" value="false" />
            <c:forEach var="selectedId" items="${selectedCategoryList}">
              <c:if test="${selectedId == child.id}">
                <c:set var="checked" value="true" />
              </c:if>
            </c:forEach>

            <label class="mc-left-filter__check">
              <input type="checkbox" name="category" value="${child.id}" <c:if test="${checked}">checked</c:if>>
              <span class="mc-left-filter__box"></span>
              <span class="mc-left-filter__name"><c:out value="${fn:toUpperCase(child.name)}" /></span>
            </label>
          </c:forEach>
        </c:when>

        <c:otherwise>
          <c:forEach var="parent" items="${categories}">
            <div class="mc-left-filter__group">
              <c:out value="${parent.name}" />
              <c:if test="${parent.productCount > 0}"> (${parent.productCount})</c:if>
            </div>

            <c:choose>
              <c:when test="${not empty parent.children}">
                <c:forEach var="child" items="${parent.children}">
                  <c:set var="checked" value="false" />
                  <c:forEach var="selectedId" items="${selectedCategoryList}">
                    <c:if test="${selectedId == child.id}">
                      <c:set var="checked" value="true" />
                    </c:if>
                  </c:forEach>

                  <label class="mc-left-filter__check">
                    <input type="checkbox" name="category" value="${child.id}" <c:if test="${checked}">checked</c:if>>
                    <span class="mc-left-filter__box"></span>
                    <span class="mc-left-filter__name"><c:out value="${child.name}" /></span>
                    <c:if test="${child.productCount > 0}">
                      <span class="mc-left-filter__count">${child.productCount}</span>
                    </c:if>
                  </label>
                </c:forEach>
              </c:when>
              <c:otherwise>
                <c:set var="checked" value="false" />
                <c:forEach var="selectedId" items="${selectedCategoryList}">
                  <c:if test="${selectedId == parent.id}">
                    <c:set var="checked" value="true" />
                  </c:if>
                </c:forEach>

                <label class="mc-left-filter__check">
                  <input type="checkbox" name="category" value="${parent.id}" <c:if test="${checked}">checked</c:if>>
                  <span class="mc-left-filter__box"></span>
                  <span class="mc-left-filter__name"><c:out value="${parent.name}" /></span>
                </label>
              </c:otherwise>
            </c:choose>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <div class="mc-left-filter__section">
    <button type="button"
            class="mc-left-filter__title"
            onclick="this.closest('.mc-left-filter__section').classList.toggle('is-collapsed')">
      <span>Loại sản phẩm</span>
      <span class="mc-left-filter__chevron">⌃</span>
    </button>

    <div class="mc-left-filter__body">
      <c:choose>
        <c:when test="${not empty primaryCategory and not empty primaryCategory.children}">
          <c:forEach var="child" items="${primaryCategory.children}">
            <c:set var="checked" value="false" />
            <c:forEach var="selectedId" items="${selectedCategoryList}">
              <c:if test="${selectedId == child.id}">
                <c:set var="checked" value="true" />
              </c:if>
            </c:forEach>

            <label class="mc-left-filter__check">
              <input type="checkbox" name="category" value="${child.id}" <c:if test="${checked}">checked</c:if>>
              <span class="mc-left-filter__box"></span>
              <span class="mc-left-filter__name"><c:out value="${child.name}" /></span>
            </label>
          </c:forEach>
        </c:when>
        <c:otherwise>
          <label class="mc-left-filter__check">
            <input type="checkbox" disabled>
            <span class="mc-left-filter__box"></span>
            <span class="mc-left-filter__name">Son thỏi</span>
          </label>
          <label class="mc-left-filter__check">
            <input type="checkbox" disabled>
            <span class="mc-left-filter__box"></span>
            <span class="mc-left-filter__name">Dưỡng môi</span>
          </label>
          <label class="mc-left-filter__check">
            <input type="checkbox" disabled>
            <span class="mc-left-filter__box"></span>
            <span class="mc-left-filter__name">Tẩy da chết môi</span>
          </label>
          <label class="mc-left-filter__check">
            <input type="checkbox" disabled>
            <span class="mc-left-filter__box"></span>
            <span class="mc-left-filter__name">Son kem</span>
          </label>
          <label class="mc-left-filter__check">
            <input type="checkbox" disabled>
            <span class="mc-left-filter__box"></span>
            <span class="mc-left-filter__name">Son tint</span>
          </label>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <div class="mc-left-filter__section">
    <button type="button"
            class="mc-left-filter__title"
            onclick="this.closest('.mc-left-filter__section').classList.toggle('is-collapsed')">
      <span>Giá</span>
      <span class="mc-left-filter__chevron">⌃</span>
    </button>

    <div class="mc-left-filter__body">
      <div class="mc-left-filter__price-row">
        <input class="mc-left-filter__price-input" type="text" value="0" readonly>
        <span class="mc-left-filter__dash"></span>
        <input class="mc-left-filter__price-input" type="text" value="100,000,000" readonly>
      </div>

      <div class="mc-left-filter__price-options">
        <label class="mc-left-filter__check">
          <input type="checkbox" name="priceRange" value="lt500" ${fn:contains(priceRangeList, 'lt500') ? 'checked' : ''}>
          <span class="mc-left-filter__box"></span>
          <span class="mc-left-filter__name">Dưới 500.000₫</span>
        </label>
        <label class="mc-left-filter__check">
          <input type="checkbox" name="priceRange" value="500_1000" ${fn:contains(priceRangeList, '500_1000') ? 'checked' : ''}>
          <span class="mc-left-filter__box"></span>
          <span class="mc-left-filter__name">500.000₫ - 1.000.000₫</span>
        </label>
        <label class="mc-left-filter__check">
          <input type="checkbox" name="priceRange" value="gt1000" ${fn:contains(priceRangeList, 'gt1000') ? 'checked' : ''}>
          <span class="mc-left-filter__box"></span>
          <span class="mc-left-filter__name">Trên 1.000.000₫</span>
        </label>
      </div>

      <button type="submit" class="mc-left-filter__apply">Áp dụng</button>
    </div>
  </div>

  <div class="mc-left-filter__section">
    <button type="button"
            class="mc-left-filter__title"
            onclick="this.closest('.mc-left-filter__section').classList.toggle('is-collapsed')">
      <span>Thương hiệu</span>
      <span class="mc-left-filter__chevron">⌃</span>
    </button>

    <div class="mc-left-filter__body mc-left-filter__brand-scroll">
      <c:forEach var="brand" items="${brands}">
        <c:set var="checked" value="false" />
        <c:forEach var="brandId" items="${selectedBrandList}">
          <c:if test="${brandId == brand.id}">
            <c:set var="checked" value="true" />
          </c:if>
        </c:forEach>

        <label class="mc-left-filter__check">
          <input type="checkbox" name="brand" value="${brand.id}" <c:if test="${checked}">checked</c:if>>
          <span class="mc-left-filter__box"></span>
          <span class="mc-left-filter__name"><c:out value="${brand.name}" /></span>
          <c:if test="${brand.productCount > 0}">
            <span class="mc-left-filter__count">${brand.productCount}</span>
          </c:if>
        </label>
      </c:forEach>
    </div>
  </div>

  <a href="${ctx}/products#collectionResults" class="mc-left-filter__reset">Đặt lại</a>
</form>

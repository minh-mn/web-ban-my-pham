<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<section class="skin-hot-categories" id="hot-categories">
  <div class="skin-container">
    <div class="skin-section-top skin-hot-category-head">
      <div class="hot-category-heading-wrap">
        <span class="hot-category-eyebrow">HOT CATEGORY</span>

        <h2 class="hot-category-heading" aria-label="Danh mục hot">
          <span class="hot-category-word hot-category-word-left">Danh mục</span>
          <span class="hot-category-icon" aria-hidden="true">✦</span>
          <span class="hot-category-word hot-category-word-right">Hot</span>
        </h2>
      </div>
    </div>

    <div class="skin-category-grid skin-category-grid--circle">
      <c:choose>
        <c:when test="${not empty hotCategories}">
          <c:forEach var="category" items="${hotCategories}">
            <c:choose>
              <c:when test="${category.id > 0}">
                <c:set var="categoryUrl" value="${ctx}/products?category=${category.id}" />
              </c:when>
              <c:otherwise>
                <c:set var="categoryUrl" value="${ctx}/products" />
              </c:otherwise>
            </c:choose>

            <a class="skin-category-card" href="${categoryUrl}" title="${category.name}">
              <div class="skin-category-thumb">
                <c:set var="categoryInitial" value="${fn:substring(category.name, 0, 1)}" />

                <c:choose>
                  <c:when test="${not empty category.hotImageUrl}">
                    <c:choose>
                      <c:when test="${fn:startsWith(category.hotImageUrl, 'http')}">
                        <img src="${category.hotImageUrl}" alt="${category.name}" loading="lazy"
                             onerror="this.style.display='none'; var fb=this.parentElement.querySelector('.skin-category-fallback'); if(fb){fb.classList.add('is-show');}">
                      </c:when>
                      <c:when test="${fn:startsWith(category.hotImageUrl, '/')}">
                        <img src="${ctx}${category.hotImageUrl}" alt="${category.name}" loading="lazy"
                             onerror="this.style.display='none'; var fb=this.parentElement.querySelector('.skin-category-fallback'); if(fb){fb.classList.add('is-show');}">
                      </c:when>
                      <c:otherwise>
                        <img src="${ctx}/uploads/product/${category.hotImageUrl}" alt="${category.name}" loading="lazy"
                             onerror="this.style.display='none'; var fb=this.parentElement.querySelector('.skin-category-fallback'); if(fb){fb.classList.add('is-show');}">
                      </c:otherwise>
                    </c:choose>

                    <span class="skin-category-fallback">${categoryInitial}</span>
                  </c:when>
                  <c:otherwise>
                    <span class="skin-category-fallback is-show">${categoryInitial}</span>
                  </c:otherwise>
                </c:choose>
              </div>

              <strong>${category.name}</strong>
            </a>
          </c:forEach>
        </c:when>

        <c:otherwise>
          <a class="skin-category-card" href="${ctx}/products">
            <div class="skin-category-thumb"><span class="skin-category-fallback is-show">M</span></div>
            <strong>Mỹ phẩm</strong>
          </a>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</section>

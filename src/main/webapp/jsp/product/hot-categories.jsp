<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<section class="skin-hot-categories" id="hot-categories">
  <div class="skin-container">
    <div class="skin-section-top">
      <div>
        <span class="skin-eyebrow">HOT CATEGORY</span>
        <h2>DANH MỤC HOT</h2>
        <p>Danh mục nổi bật được lấy theo số lượng sản phẩm đang hoạt động.</p>
      </div>
      <a class="skin-view-all" href="${ctx}/products">Xem sản phẩm</a>
    </div>

    <div class="skin-category-grid">
      <c:choose>
        <c:when test="${not empty hotCategories}">
          <c:forEach var="category" items="${hotCategories}">
            <a class="skin-category-card" href="${ctx}/products?categoryIds=${category.id}">
              <div class="skin-category-thumb">
                <c:choose>
                  <c:when test="${not empty category.hotImageUrl}">
                    <c:choose>
                      <c:when test="${fn:startsWith(category.hotImageUrl, 'http')}">
                        <img src="${category.hotImageUrl}" alt="${category.name}">
                      </c:when>
                      <c:when test="${fn:startsWith(category.hotImageUrl, '/')}">
                        <img src="${ctx}${category.hotImageUrl}" alt="${category.name}">
                      </c:when>
                      <c:otherwise>
                        <img src="${ctx}/uploads/product/${category.hotImageUrl}" alt="${category.name}">
                      </c:otherwise>
                    </c:choose>
                  </c:when>
                  <c:otherwise>
                    <span>${fn:substring(category.name, 0, 1)}</span>
                  </c:otherwise>
                </c:choose>
              </div>
              <strong>${category.name}</strong>
              <small>${category.productCount} sản phẩm</small>
            </a>
          </c:forEach>
        </c:when>
        <c:otherwise>
          <a class="skin-category-card" href="${ctx}/products">
            <div class="skin-category-thumb"><span>M</span></div>
            <strong>Mỹ phẩm</strong>
            <small>Đang cập nhật</small>
          </a>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</section>

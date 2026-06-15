<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="uri" value="${pageContext.request.requestURI}" />

<link rel="stylesheet" href="${ctx}/assets/css/base.css">
<link rel="stylesheet" href="${ctx}/assets/css/theme-red-buttons.css?v=20260613_8">

<nav class="category-nav-bar art-nav-bar" aria-label="Menu chính">
  <div class="container art-nav-container">
    <ul class="menu-horizontal-list art-menu-list">
      <li>
        <a href="${ctx}/home" class="menu-link-item art-menu-link ${fn:endsWith(uri, '/home') ? 'is-active' : ''}">
          <span class="art-menu-icon">⌂</span>
          <span>TRANG CHỦ</span>
        </a>
      </li>

      <li class="has-dropdown">
        <a href="${ctx}/products?category=all" class="menu-link-item art-menu-link ${fn:contains(uri, '/products') ? 'is-active' : ''}">
          <span class="art-menu-icon">◇</span>
          <span>SẢN PHẨM</span>
        </a>

        <c:if test="${not empty categories}">
          <ul class="dropdown-menu">
            <c:forEach var="parent" items="${categories}">
              <li>
                <a href="${ctx}/products?category=${parent.id}">${parent.name}</a>

                <c:if test="${not empty parent.children}">
                  <ul class="sub-dropdown-menu">
                    <c:forEach var="child" items="${parent.children}">
                      <li>
                        <a href="${ctx}/products?category=${child.id}">${child.name}</a>
                      </li>
                    </c:forEach>
                  </ul>
                </c:if>
              </li>
            </c:forEach>
          </ul>
        </c:if>
      </li>

      <li class="has-dropdown">
        <a href="${ctx}/brands" class="menu-link-item art-menu-link ${fn:contains(uri, '/brands') ? 'is-active' : ''}">
          <span class="art-menu-icon">☆</span>
          <span>THƯƠNG HIỆU</span>
        </a>

        <ul class="dropdown-menu">
          <c:forEach var="brand" items="${brands}">
            <li>
              <a href="${ctx}/products?brand=${brand.id}">
                  ${brand.name} (${brand.productCount})
              </a>
            </li>
          </c:forEach>
        </ul>
      </li>

      <c:if test="${isFlashSaleActive}">
        <li>
          <a href="${ctx}/flash-sale" class="menu-link-item art-menu-link hot-menu ${fn:contains(uri, '/flash-sale') ? 'is-active' : ''}">
            <span class="art-menu-icon">⚡</span>
            <span>FLASH SALE</span>
          </a>
        </li>
      </c:if>

      <li>
        <a href="${ctx}/blog" class="menu-link-item art-menu-link ${fn:contains(uri, '/blog') ? 'is-active' : ''}">
          <span class="art-menu-icon">▣</span>
          <span>TIN TỨC</span>
        </a>
      </li>

      <li>
        <a href="${ctx}/lien-he" class="menu-link-item art-menu-link ${fn:contains(uri, '/lien-he') ? 'is-active' : ''}">
          <span class="art-menu-icon">☏</span>
          <span>LIÊN HỆ</span>
        </a>
      </li>
    </ul>
  </div>
</nav>

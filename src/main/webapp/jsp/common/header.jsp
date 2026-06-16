<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="uri" value="${pageContext.request.requestURI}" />

<nav class="category-nav-bar art-nav-bar" aria-label="Menu chính">
  <div class="container art-nav-container">
    <ul class="menu-horizontal-list art-menu-list">
      <li>
        <a href="${ctx}/home" class="menu-link-item art-menu-link ${fn:endsWith(uri, '/home') || fn:endsWith(uri, '/') ? 'is-active' : ''}">
          <span class="art-menu-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" focusable="false">
              <path d="M3.5 10.7 12 3.7l8.5 7"></path>
              <path d="M5.6 9.8V20h12.8V9.8"></path>
              <path d="M9.5 20v-5.4h5V20"></path>
            </svg>
          </span>
          <span>TRANG CHỦ</span>
        </a>
      </li>

      <li class="has-dropdown">
        <a href="${ctx}/products?category=all" class="menu-link-item art-menu-link ${fn:contains(uri, '/products') ? 'is-active' : ''}">
          <span class="art-menu-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" focusable="false">
              <path d="M6.5 7.4h11l1.2 12.2H5.3L6.5 7.4z"></path>
              <path d="M9 7.4a3 3 0 0 1 6 0"></path>
              <path d="M9.2 11.3h5.6"></path>
            </svg>
          </span>
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
          <span class="art-menu-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" focusable="false">
              <path d="m12 3.2 2.65 5.37 5.93.86-4.29 4.18 1.01 5.9L12 16.72 6.7 19.51l1.01-5.9-4.29-4.18 5.93-.86L12 3.2z"></path>
            </svg>
          </span>
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
            <span class="art-menu-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" focusable="false">
                <path d="M13 2 4.8 13h6.1L10.6 22 19.2 10h-6.3L13 2z"></path>
              </svg>
            </span>
            <span>FLASH SALE</span>
          </a>
        </li>
      </c:if>

      <li>
        <a href="${ctx}/blog" class="menu-link-item art-menu-link ${fn:contains(uri, '/blog') ? 'is-active' : ''}">
          <span class="art-menu-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" focusable="false">
              <rect x="4.2" y="4.2" width="15.6" height="15.6" rx="2.4"></rect>
              <path d="M8 8.5h8"></path>
              <path d="M8 12h8"></path>
              <path d="M8 15.5h5.8"></path>
            </svg>
          </span>
          <span>TIN TỨC</span>
        </a>
      </li>

      <li>
        <a href="${ctx}/lien-he" class="menu-link-item art-menu-link ${fn:contains(uri, '/lien-he') ? 'is-active' : ''}">
          <span class="art-menu-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" focusable="false">
              <path d="M6.7 4.4 8.8 3a1.6 1.6 0 0 1 2.2.45l1.15 1.75a1.7 1.7 0 0 1-.26 2.2l-1.12 1.1a12.7 12.7 0 0 0 4.73 4.73l1.1-1.12a1.7 1.7 0 0 1 2.2-.26l1.75 1.15a1.6 1.6 0 0 1 .45 2.2l-1.4 2.1a2.2 2.2 0 0 1-2.35.92C9.7 16.45 5.55 12.3 4.78 6.75A2.2 2.2 0 0 1 6.7 4.4z"></path>
            </svg>
          </span>
          <span>LIÊN HỆ</span>
        </a>
      </li>

     <li>
        <a href="${ctx}/wishlist" class="menu-link-item art-menu-link ${fn:contains(uri, '/wishlist') ? 'is-active' : ''}">
          <span class="art-menu-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" focusable="false">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"></path>
            </svg>
          </span>
          <span>YÊU THÍCH</span>
        </a>
      </li>
    </ul>
  </div>
</nav>

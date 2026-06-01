<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">

<nav class="category-nav-bar">
  <div class="container">
    <ul class="menu-horizontal-list">
      <li><a href="${pageContext.request.contextPath}/home" class="menu-link-item">TRANG CHỦ</a></li>

      <li class="has-dropdown">
        <a href="${pageContext.request.contextPath}/products?category=all" class="menu-link-item">SẢN PHẨM</a>

        <c:if test="${not empty categories}">
          <ul class="dropdown-menu">
            <c:forEach var="parent" items="${categories}">
              <li>
                <a href="${pageContext.request.contextPath}/products?category=${parent.id}">${parent.name}</a>

                <c:if test="${not empty parent.children}">
                  <ul class="sub-dropdown-menu">
                    <c:forEach var="child" items="${parent.children}">
                      <li>
                        <a href="${pageContext.request.contextPath}/products?category=${child.id}">${child.name}</a>
                      </li>
                    </c:forEach>
                  </ul>
                </c:if>
              </li>
            </c:forEach>
          </ul>
        </c:if>
      </li>

      <!-- CATEGORY: BRAND -->
      <li class="has-dropdown">
        <a href="${pageContext.request.contextPath}/brands" class="menu-link-item">
          THƯƠNG HIỆU
        </a>

        <ul class="dropdown-menu">
          <c:forEach var="brand" items="${brands}">
            <li>
              <a href="${pageContext.request.contextPath}/products?brand=${brand.id}">
                  ${brand.name} (${brand.productCount})
              </a>
            </li>
          </c:forEach>
        </ul>
      </li>

      <li>
        <a href="${pageContext.request.contextPath}/flash-sale" class="menu-link-item hot-menu">
          FLASH SALE
        </a>
      </li>

       <c:if test="${isFlashSaleActive}">
        <li>
          <a href="${pageContext.request.contextPath}/flash-sale" class="menu-link-item hot-menu">
            FLASH SALE
          </a>
        </li>
      </c:if>

      <li>
        <a href="${pageContext.request.contextPath}/blog" class="menu-link-item">
          TIN TỨC
        </a>
      </li>

      <li>
        <a href="${pageContext.request.contextPath}/lien-he" class="menu-link-item">
          LIÊN HỆ
        </a>
      </li>

    </ul>
  </div>
</nav>

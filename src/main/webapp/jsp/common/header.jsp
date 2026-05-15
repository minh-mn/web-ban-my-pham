<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<nav class="category-nav-bar">
  <div class="container">
    <ul class="menu-horizontal-list">
      <li class="menu-item-root has-dropdown">
        <a href="javascript:void(0)" class="menu-link-item highlight">
          <span class="icon-menu">☰</span> DANH MỤC SẢN PHẨM
        </a>

        <ul class="dropdown-menu-list">
          <c:forEach var="parent" items="${menuCategories}">
            <li class="dropdown-item has-submenu">
              <a href="${pageContext.request.contextPath}/products?category=${parent.id}">
                  ${parent.name}
                <c:if test="${not empty parent.children}">
                  <span class="arrow-right">›</span> </c:if>
              </a>

              <c:if test="${not empty parent.children}">
                <ul class="sub-dropdown-list">
                  <c:forEach var="child" items="${parent.children}">
                    <li>
                      <a href="${pageContext.request.contextPath}/products?category=${child.id}">
                          ${child.name}
                      </a>
                    </li>
                  </c:forEach>
                </ul>
              </c:if>
            </li>
          </c:forEach>
        </ul>
      </li>

      <li><a href="#" class="menu-link-item">TRANG CHỦ</a></li>
      <li><a href="#" class="menu-link-item">KHUYẾN MÃI</a></li>
    </ul>
  </div>
</nav>
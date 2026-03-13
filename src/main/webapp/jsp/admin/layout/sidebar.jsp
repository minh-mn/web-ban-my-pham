<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<aside class="admin-sidebar">
  <div class="admin-sidebar__inner">

    <a class="admin-brand" href="${pageContext.request.contextPath}/admin">
      <span>MyCosmetic Admin</span>
    </a>

    <nav class="admin-nav">

      <div class="admin-nav__section">Tổng quan</div>

      <a class="${activeMenu == 'admin' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin">
        <span>Admin Center</span>
        <span class="admin-nav__meta">/admin</span>
      </a>

      <a class="${activeMenu == 'dashboard' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/dashboard">
        <span>Dashboard</span>
        <span class="admin-nav__meta">/admin/dashboard</span>
      </a>

      <div class="admin-nav__section">Catalog</div>

      <a class="${activeMenu == 'products' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/products">
        <span>Products</span>
        <span class="admin-nav__meta">/admin/products</span>
      </a>

      <a class="${activeMenu == 'categories' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/categories">
        <span>Categories</span>
        <span class="admin-nav__meta">/admin/categories</span>
      </a>

      <a class="${activeMenu == 'brands' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/brands">
        <span>Brands</span>
        <span class="admin-nav__meta">/admin/brands</span>
      </a>

      <a class="${activeMenu == 'banners' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/banners">
        <span>Banners</span>
        <span class="admin-nav__meta">/admin/banners</span>
      </a>

      <div class="admin-nav__section">Sales</div>

      <a class="${activeMenu == 'orders' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/orders">
        <span>Orders</span>
        <span class="admin-nav__meta">/admin/orders</span>
      </a>

      <a class="${activeMenu == 'coupons' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/coupons">
        <span>Coupons</span>
        <span class="admin-nav__meta">/admin/coupons</span>
      </a>

      <div class="admin-nav__section">Promotions</div>

      <a class="${activeMenu == 'promotionEvents' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/promotion-events">
        <span>Promotion Events</span>
        <span class="admin-nav__meta">/admin/promotion-events</span>
      </a>

      <a class="${activeMenu == 'brandDiscounts' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/brand-discounts">
        <span>Brand Discounts</span>
        <span class="admin-nav__meta">/admin/brand-discounts</span>
      </a>

      <a class="${activeMenu == 'orderDiscounts' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/order-discounts">
        <span>Order Discounts</span>
        <span class="admin-nav__meta">/admin/order-discounts</span>
      </a>

      <div class="admin-nav__section">System</div>

      <a class="${activeMenu == 'reviews' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/reviews">
        <span>Reviews</span>
        <span class="admin-nav__meta">/admin/reviews</span>
      </a>

      <a class="${activeMenu == 'users' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/users">
        <span>Users</span>
        <span class="admin-nav__meta">/admin/users</span>
      </a>

    </nav>

    <div class="admin-sidebar__footer">
      <hr class="admin-divider"/>
      <a class="admin-btn" href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
    </div>

  </div>
</aside>

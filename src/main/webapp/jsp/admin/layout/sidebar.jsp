<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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

      <a class="${activeMenu == 'inventory' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/inventory">
        <span>Quản lí tồn kho</span>
        <span class="admin-nav__meta">/admin/inventory</span>
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

      <a class="${activeMenu == 'returns' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/returns">
        <span>Return Requests</span>
        <span class="admin-nav__meta">/admin/returns</span>
      </a>

      <a class="${activeMenu == 'cancelRequests' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/cancel-requests">
        <span>Cancel Requests</span>
        <span class="admin-nav__meta">/admin/cancel-requests</span>
      </a>

      <div class="admin-nav__section">Promotions</div>

      <a class="${
            activeMenu == 'promotions'
            || activeMenu == 'coupons'
            || activeMenu == 'promotionEvents'
            || activeMenu == 'brandDiscounts'
            || activeMenu == 'orderDiscounts'
            ? 'is-active'
            : ''
          }"
         href="${pageContext.request.contextPath}/admin/promotions">
        <span>Khuyến mãi &amp; Mã giảm giá</span>
        <span class="admin-nav__meta">/admin/promotions</span>
      </a>

      <div class="admin-nav__section">Nội dung &amp; CMS</div>

      <a class="${activeMenu == 'reviews' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/reviews">
        <span>Quản lí bình luận</span>
        <span class="admin-nav__meta">/admin/reviews</span>
      </a>

      <a class="${activeMenu == 'settings' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/settings">
        <span>Footer Settings</span>
        <span class="admin-nav__meta">/admin/settings</span>
      </a>

      <a class="${activeMenu == 'pages' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/pages">
        <span>Pages CMS</span>
        <span class="admin-nav__meta">/admin/pages</span>
      </a>

      <a class="${activeMenu == 'contact' || activeMenu == 'contact-messages' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/contact-messages">
        <span>Contact Messages</span>
        <span class="admin-nav__meta">/admin/contact-messages</span>
      </a>

      <a class="${activeMenu == 'events' || activeMenu == 'blogs' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/events">
        <span>News / Blogs</span>
        <span class="admin-nav__meta">/admin/events</span>
      </a>

      <div class="admin-nav__section">System</div>

      <a class="${activeMenu == 'users' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/users">
        <span>Users</span>
        <span class="admin-nav__meta">/admin/users</span>
      </a>

      <a class="${activeMenu == 'notifications' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin/notifications">
        <span>Notifications</span>
        <span class="admin-nav__meta">/admin/notifications</span>
      </a>

    </nav>

    <div class="admin-sidebar__footer">
      <hr class="admin-divider"/>
      <a class="admin-btn" href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
    </div>

  </div>
</aside>

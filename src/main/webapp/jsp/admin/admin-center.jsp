<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="ADMIN | Tổng quan" scope="request"/>
<c:set var="activeMenu" value="admin" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-center.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Admin Center</h1>
        <p class="admin-subtext">Chọn phân hệ để quản trị.</p>
      </div>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">
        <div class="admin-grid admin-grid--3">

          <!-- ✅ HOME -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/">
            <div class="admin-menu-card__title">Trang chủ</div>
            <p class="admin-menu-card__desc">Quay về trang người dùng (Home).</p>
            <span class="admin-chip">/</span>
          </a>

          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/dashboard">
            <div class="admin-menu-card__title">Dashboard</div>
            <p class="admin-menu-card__desc">Tổng quan nhanh hệ thống (đơn hàng, doanh thu, KPI).</p>
            <span class="admin-chip">/admin/dashboard</span>
          </a>

          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/banners">
            <div class="admin-menu-card__title">Banners</div>
            <p class="admin-menu-card__desc">Quản lý banner slider trang chủ.</p>
            <span class="admin-chip">/admin/banners</span>
          </a>

          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/brands">
            <div class="admin-menu-card__title">Brands</div>
            <p class="admin-menu-card__desc">Quản lý thương hiệu sản phẩm.</p>
            <span class="admin-chip">/admin/brands</span>
          </a>

          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/brand-discounts">
            <div class="admin-menu-card__title">Brand Discounts</div>
            <p class="admin-menu-card__desc">Giảm giá theo thương hiệu (theo thời gian).</p>
            <span class="admin-chip">/admin/brand-discounts</span>
          </a>

          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/categories">
            <div class="admin-menu-card__title">Categories</div>
            <p class="admin-menu-card__desc">Quản lý danh mục (cây danh mục).</p>
            <span class="admin-chip">/admin/categories</span>
          </a>

          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/coupons">
            <div class="admin-menu-card__title">Coupons</div>
            <p class="admin-menu-card__desc">Quản lý coupon giảm giá theo %.</p>
            <span class="admin-chip">/admin/coupons</span>
          </a>

          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/orders">
            <div class="admin-menu-card__title">Orders</div>
            <p class="admin-menu-card__desc">Danh sách đơn hàng, xem chi tiết và trạng thái.</p>
            <span class="admin-chip">/admin/orders</span>
          </a>

          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/order-discounts">
            <div class="admin-menu-card__title">Order Discounts</div>
            <p class="admin-menu-card__desc">Giảm giá theo đơn hàng.</p>
            <span class="admin-chip">/admin/order-discounts</span>
          </a>

          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/products">
            <div class="admin-menu-card__title">Products</div>
            <p class="admin-menu-card__desc">Quản lý sản phẩm.</p>
            <span class="admin-chip">/admin/products</span>
          </a>

          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/promotion-events">
            <div class="admin-menu-card__title">Promotion Events</div>
            <p class="admin-menu-card__desc">Chương trình khuyến mãi.</p>
            <span class="admin-chip">/admin/promotion-events</span>
          </a>

          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/reviews">
            <div class="admin-menu-card__title">Reviews</div>
            <p class="admin-menu-card__desc">Quản lý đánh giá sản phẩm.</p>
            <span class="admin-chip">/admin/reviews</span>
          </a>

          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/users">
            <div class="admin-menu-card__title">Users</div>
            <p class="admin-menu-card__desc">Quản lý người dùng.</p>
            <span class="admin-chip">/admin/users</span>
          </a>

        </div>
      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

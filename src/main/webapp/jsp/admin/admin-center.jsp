<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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
        <p class="admin-subtext">Chọn phân hệ để quản trị hệ thống MyCosmeticShop.</p>
      </div>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">
        <div class="admin-grid admin-grid--3">

          <!-- HOME -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/">
            <div class="admin-menu-card__title">Trang chủ</div>
            <p class="admin-menu-card__desc">
              Quay về giao diện người dùng để kiểm tra hiển thị cửa hàng.
            </p>
            <span class="admin-chip">/</span>
          </a>

          <!-- DASHBOARD -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/dashboard">
            <div class="admin-menu-card__title">Dashboard</div>
            <p class="admin-menu-card__desc">
              Theo dõi tổng quan đơn hàng, doanh thu, KPI và tình hình hoạt động.
            </p>
            <span class="admin-chip">/admin/dashboard</span>
          </a>

          <!-- BANNERS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/banners">
            <div class="admin-menu-card__title">Banners</div>
            <p class="admin-menu-card__desc">
              Quản lý banner slider, hình ảnh quảng bá và trạng thái hiển thị.
            </p>
            <span class="admin-chip">/admin/banners</span>
          </a>

          <!-- PRODUCTS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/products">
            <div class="admin-menu-card__title">Products</div>
            <p class="admin-menu-card__desc">
              Quản lý sản phẩm, hình ảnh, giá bán, tồn kho và trạng thái kinh doanh.
            </p>
            <span class="admin-chip">/admin/products</span>
          </a>

          <!-- CATEGORIES -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/categories">
            <div class="admin-menu-card__title">Categories</div>
            <p class="admin-menu-card__desc">
              Quản lý danh mục sản phẩm, danh mục cha con và trạng thái hiển thị.
            </p>
            <span class="admin-chip">/admin/categories</span>
          </a>

          <!-- BRANDS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/brands">
            <div class="admin-menu-card__title">Brands</div>
            <p class="admin-menu-card__desc">
              Quản lý thương hiệu mỹ phẩm và thông tin liên quan đến sản phẩm.
            </p>
            <span class="admin-chip">/admin/brands</span>
          </a>

          <!-- PROMOTIONS - UNIFIED -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/promotions">
            <div class="admin-menu-card__title">Khuyến mãi &amp; Mã giảm giá</div>
            <p class="admin-menu-card__desc">
              Quản lý tập trung mã giảm giá, giảm giá thương hiệu, giảm theo giá trị đơn hàng
              và chương trình khuyến mãi cửa hàng.
            </p>
            <span class="admin-chip">/admin/promotions</span>
          </a>

          <!-- USER RANKS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/ranks">
            <div class="admin-menu-card__title">User Ranks</div>
            <p class="admin-menu-card__desc">
              Quản lý hạng khách hàng, mốc chi tiêu và ưu đãi theo từng cấp bậc.
            </p>
            <span class="admin-chip">/admin/ranks</span>
          </a>

          <!-- ORDERS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/orders">
            <div class="admin-menu-card__title">Orders</div>
            <p class="admin-menu-card__desc">
              Danh sách đơn hàng, xem chi tiết, cập nhật trạng thái và xử lý thanh toán.
            </p>
            <span class="admin-chip">/admin/orders</span>
          </a>

          <!-- RETURNS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/returns">
            <div class="admin-menu-card__title">Return Requests</div>
            <p class="admin-menu-card__desc">
              Quản lý yêu cầu hoàn hàng, duyệt hoàn tiền và theo dõi trạng thái xử lý.
            </p>
            <span class="admin-chip">/admin/returns</span>
          </a>

          <!-- REVIEWS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/reviews">
            <div class="admin-menu-card__title">Reviews</div>
            <p class="admin-menu-card__desc">
              Quản lý đánh giá sản phẩm, duyệt nội dung và phản hồi khách hàng.
            </p>
            <span class="admin-chip">/admin/reviews</span>
          </a>

          <!-- USERS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/users">
            <div class="admin-menu-card__title">Users</div>
            <p class="admin-menu-card__desc">
              Quản lý tài khoản người dùng, vai trò, trạng thái và thông tin hồ sơ.
            </p>
            <span class="admin-chip">/admin/users</span>
          </a>

        </div>
      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>
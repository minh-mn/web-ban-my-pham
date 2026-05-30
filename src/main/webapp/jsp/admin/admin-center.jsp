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
        <p class="admin-subtext">Chọn phân hệ để quản trị.</p>
      </div>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">
        <div class="admin-grid admin-grid--3">

          <!-- HOME -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/">
            <div class="admin-menu-card__title">Trang chủ</div>
            <p class="admin-menu-card__desc">Quay về trang người dùng.</p>
            <span class="admin-chip">/</span>
          </a>

          <!-- DASHBOARD -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/dashboard">
            <div class="admin-menu-card__title">Dashboard</div>
            <p class="admin-menu-card__desc">Tổng quan nhanh hệ thống: đơn hàng, doanh thu, KPI.</p>
            <span class="admin-chip">/admin/dashboard</span>
          </a>

          <!-- BANNERS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/banners">
            <div class="admin-menu-card__title">Banners</div>
            <p class="admin-menu-card__desc">Quản lý banner slider hiển thị ở trang chủ.</p>
            <span class="admin-chip">/admin/banners</span>
          </a>

          <!-- BRANDS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/brands">
            <div class="admin-menu-card__title">Brands</div>
            <p class="admin-menu-card__desc">Quản lý thương hiệu sản phẩm.</p>
            <span class="admin-chip">/admin/brands</span>
          </a>

          <!-- CATEGORIES -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/categories">
            <div class="admin-menu-card__title">Categories</div>
            <p class="admin-menu-card__desc">Quản lý danh mục sản phẩm và cây danh mục.</p>
            <span class="admin-chip">/admin/categories</span>
          </a>

          <!-- PRODUCTS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/products">
            <div class="admin-menu-card__title">Products</div>
            <p class="admin-menu-card__desc">Quản lý sản phẩm, hình ảnh, giá bán, tồn kho và trạng thái.</p>
            <span class="admin-chip">/admin/products</span>
          </a>

          <!-- INVENTORY -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/inventory">
            <div class="admin-menu-card__title">Quản lý tồn kho</div>
            <p class="admin-menu-card__desc">
              Theo dõi số lượng tồn, nhập thêm hàng, cảnh báo sắp hết hàng và thống kê xuất kho.
            </p>
            <span class="admin-chip">/admin/inventory</span>
          </a>

          <!-- UNIFIED PROMOTIONS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/promotions">
            <div class="admin-menu-card__title">Khuyến mãi &amp; Mã giảm giá</div>
            <p class="admin-menu-card__desc">
              Quản lý tập trung mã giảm giá, giảm giá thương hiệu, giảm theo đơn hàng
              và chương trình khuyến mãi cửa hàng.
            </p>
            <span class="admin-chip">/admin/promotions</span>
          </a>

          <!-- USER RANKS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/ranks">
            <div class="admin-menu-card__title">User Ranks</div>
            <p class="admin-menu-card__desc">
              Quản lý hạng khách hàng, mốc chi tiêu và ưu đãi theo rank.
            </p>
            <span class="admin-chip">/admin/ranks</span>
          </a>

          <!-- ORDERS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/orders">
            <div class="admin-menu-card__title">Orders</div>
            <p class="admin-menu-card__desc">Danh sách đơn hàng, xem chi tiết và cập nhật trạng thái.</p>
            <span class="admin-chip">/admin/orders</span>
          </a>

          <!-- REVIEWS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/reviews">
            <div class="admin-menu-card__title">Reviews</div>
            <p class="admin-menu-card__desc">Quản lý đánh giá và phản hồi của khách hàng.</p>
            <span class="admin-chip">/admin/reviews</span>
          </a>

          <!-- USERS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/users">
            <div class="admin-menu-card__title">Users</div>
            <p class="admin-menu-card__desc">Quản lý tài khoản người dùng, vai trò và trạng thái hoạt động.</p>
            <span class="admin-chip">/admin/users</span>
          </a>

          <!-- FLASH SALE -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/flash-sale">
            <div class="admin-menu-card__title">Flash Sale</div>
            <p class="admin-menu-card__desc">
              Thiết lập chương trình Flash Sale, quản lý thời gian, sản phẩm và giá giảm sốc.
            </p>
            <span class="admin-chip">/admin/flash-sale</span>
          </a>

          <!-- SETTINGS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/settings">
            <div class="admin-menu-card__title">Website Settings</div>
            <p class="admin-menu-card__desc">
              Quản lý hotline, email, địa chỉ, social, copyright và thông tin footer.
            </p>
            <span class="admin-chip">CMS / settings</span>
          </a>

          <!-- PAGES CMS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/pages">
            <div class="admin-menu-card__title">Pages CMS</div>
            <p class="admin-menu-card__desc">
              Quản lý chính sách, điều khoản và nội dung trang động.
            </p>
            <span class="admin-chip">CMS / pages</span>
          </a>

          <!-- CONTACT MESSAGES -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/contact-messages">
            <div class="admin-menu-card__title">Contact Messages</div>
            <p class="admin-menu-card__desc">
              Quản lý tin nhắn khách hàng gửi từ form liên hệ.
            </p>
            <span class="admin-chip">CMS / contact</span>
          </a>

          <!-- NEWS / BLOGS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/events">
            <div class="admin-menu-card__title">News / Blogs</div>
            <p class="admin-menu-card__desc">
              Quản lý bài viết tin tức, sự kiện và hình ảnh hiển thị trên trang chủ.
            </p>
            <span class="admin-chip">CMS / blogs</span>
          </a>

          <!-- NOTIFICATIONS -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/notifications">
            <div class="admin-menu-card__title">Hệ thống Thông báo</div>
            <p class="admin-menu-card__desc">
              Gửi thông báo hàng loạt sự kiện, voucher, ưu đãi giảm giá đến toàn bộ khách hàng.
            </p>
            <span class="admin-chip">Hệ thống / Broadcast</span>
          </a>

        </div>
      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

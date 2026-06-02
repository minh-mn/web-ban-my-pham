<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Tổng quan" scope="request"/>
<c:set var="activeMenu" value="admin" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-center.css" scope="request"/>


<c:set var="adminPermissions" value="${sessionScope.adminPermissions}"/>
<c:set var="isSuperAdmin" value="${sessionScope.isSuperAdmin}"/>
<c:set var="canRevenue" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('REVENUE_VIEW'))}"/>
<c:set var="canProducts" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('PRODUCT_MANAGE'))}"/>
<c:set var="canInventory" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('INVENTORY_MANAGE'))}"/>
<c:set var="canCategories" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('CATEGORY_MANAGE'))}"/>
<c:set var="canBrands" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('BRAND_MANAGE'))}"/>
<c:set var="canBanners" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('BANNER_MANAGE'))}"/>
<c:set var="canOrders" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('ORDER_MANAGE'))}"/>
<c:set var="canReturns" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('RETURN_MANAGE'))}"/>
<c:set var="canPromotions" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('PROMOTION_MANAGE'))}"/>
<c:set var="canFlashsale" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('FLASHSALE_MANAGE'))}"/>
<c:set var="canReviews" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('REVIEW_MANAGE'))}"/>
<c:set var="canSettings" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('SETTING_MANAGE'))}"/>
<c:set var="canCms" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('CMS_MANAGE'))}"/>
<c:set var="canContact" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('CONTACT_MANAGE'))}"/>
<c:set var="canUsers" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('USER_MANAGE'))}"/>
<c:set var="canRoles" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('ROLE_MANAGE'))}"/>
<c:set var="canNotifications" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('NOTIFICATION_MANAGE'))}"/>
<c:set var="canRanks" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('RANK_MANAGE'))}"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <jsp:include page="/jsp/admin/layout/topbar.jsp"/>

    <div class="admin-card">
      <div class="admin-card__body">
        <div class="admin-grid admin-grid--3">

          <!-- HOME -->
          <a class="admin-menu-card" href="${pageContext.request.contextPath}/">
            <div class="admin-menu-card__title">Trang chủ</div>
            <p class="admin-menu-card__desc">Quay về trang người dùng.</p>
            <span class="admin-chip">/</span>
          </a>

          <c:if test="${canRevenue}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/dashboard">
              <div class="admin-menu-card__title">Dashboard / Doanh thu</div>
              <p class="admin-menu-card__desc">Tổng quan nhanh hệ thống: đơn hàng, doanh thu, KPI.</p>
              <span class="admin-chip">/admin/dashboard</span>
            </a>
          </c:if>

          <c:if test="${canBanners}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/banners">
              <div class="admin-menu-card__title">Banners</div>
              <p class="admin-menu-card__desc">Quản lý banner slider hiển thị ở trang chủ.</p>
              <span class="admin-chip">/admin/banners</span>
            </a>
          </c:if>

          <c:if test="${canBrands}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/brands">
              <div class="admin-menu-card__title">Brands</div>
              <p class="admin-menu-card__desc">Quản lý thương hiệu sản phẩm.</p>
              <span class="admin-chip">/admin/brands</span>
            </a>
          </c:if>

          <c:if test="${canCategories}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/categories">
              <div class="admin-menu-card__title">Categories</div>
              <p class="admin-menu-card__desc">Quản lý danh mục sản phẩm và cây danh mục.</p>
              <span class="admin-chip">/admin/categories</span>
            </a>
          </c:if>

          <c:if test="${canProducts}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/products">
              <div class="admin-menu-card__title">Products</div>
              <p class="admin-menu-card__desc">Quản lý sản phẩm, hình ảnh, giá bán, tồn kho và trạng thái.</p>
              <span class="admin-chip">/admin/products</span>
            </a>
          </c:if>

          <c:if test="${canInventory}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/inventory">
              <div class="admin-menu-card__title">Quản lý tồn kho</div>
              <p class="admin-menu-card__desc">Theo dõi tồn kho, nhập thêm hàng, cảnh báo sắp hết hàng và thống kê nhập kho.</p>
              <span class="admin-chip">/admin/inventory</span>
            </a>
          </c:if>

          <c:if test="${canPromotions}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/promotions">
              <div class="admin-menu-card__title">Khuyến mãi &amp; Mã giảm giá</div>
              <p class="admin-menu-card__desc">Quản lý tập trung mã giảm giá, giảm giá thương hiệu, giảm theo đơn hàng và chương trình cửa hàng.</p>
              <span class="admin-chip">/admin/promotions</span>
            </a>
          </c:if>

          <c:if test="${canRanks}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/ranks">
              <div class="admin-menu-card__title">User Ranks</div>
              <p class="admin-menu-card__desc">Quản lý hạng khách hàng, mốc chi tiêu và ưu đãi theo rank.</p>
              <span class="admin-chip">/admin/ranks</span>
            </a>
          </c:if>

          <c:if test="${canOrders}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/orders">
              <div class="admin-menu-card__title">Orders</div>
              <p class="admin-menu-card__desc">Danh sách đơn hàng, xem chi tiết và cập nhật trạng thái.</p>
              <span class="admin-chip">/admin/orders</span>
            </a>
          </c:if>

          <c:if test="${canReturns}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/returns">
              <div class="admin-menu-card__title">Return Requests</div>
              <p class="admin-menu-card__desc">Duyệt yêu cầu trả hàng/hoàn hàng của khách.</p>
              <span class="admin-chip">/admin/returns</span>
            </a>
          </c:if>

          <c:if test="${canReviews}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/reviews">
              <div class="admin-menu-card__title">Reviews</div>
              <p class="admin-menu-card__desc">Quản lý đánh giá và phản hồi của khách hàng.</p>
              <span class="admin-chip">/admin/reviews</span>
            </a>
          </c:if>

          <c:if test="${canUsers}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/users">
              <div class="admin-menu-card__title">Quản lí user</div>
              <p class="admin-menu-card__desc">Xem tài khoản, role, rank, trạng thái và gán role cho từng user.</p>
              <span class="admin-chip">/admin/users</span>
            </a>
          </c:if>

          <c:if test="${canRoles}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/roles">
              <div class="admin-menu-card__title">Phân quyền role</div>
              <p class="admin-menu-card__desc">Tạo role mới và chọn quyền chi tiết cho từng nhóm nhân viên.</p>
              <span class="admin-chip">/admin/roles</span>
            </a>
          </c:if>

          <c:if test="${canFlashsale}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/flash-sale">
              <div class="admin-menu-card__title">Flash Sale</div>
              <p class="admin-menu-card__desc">Thiết lập chương trình Flash Sale, quản lý thời gian, sản phẩm và giá giảm sốc.</p>
              <span class="admin-chip">/admin/flash-sale</span>
            </a>
          </c:if>

          <c:if test="${canSettings}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/settings">
              <div class="admin-menu-card__title">Website Settings</div>
              <p class="admin-menu-card__desc">Quản lý hotline, email, địa chỉ, social, copyright và thông tin footer.</p>
              <span class="admin-chip">CMS / settings</span>
            </a>
          </c:if>

          <c:if test="${canCms}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/pages">
              <div class="admin-menu-card__title">Pages CMS</div>
              <p class="admin-menu-card__desc">Quản lý chính sách, điều khoản và nội dung trang động.</p>
              <span class="admin-chip">CMS / pages</span>
            </a>

            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/events">
              <div class="admin-menu-card__title">News / Blogs</div>
              <p class="admin-menu-card__desc">Quản lý bài viết tin tức, sự kiện và hình ảnh hiển thị trên trang chủ.</p>
              <span class="admin-chip">CMS / blogs</span>
            </a>
          </c:if>

          <c:if test="${canContact}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/contact-messages">
              <div class="admin-menu-card__title">Contact Messages</div>
              <p class="admin-menu-card__desc">Quản lý tin nhắn khách hàng gửi từ form liên hệ.</p>
              <span class="admin-chip">CMS / contact</span>
            </a>
          </c:if>

          <c:if test="${canNotifications}">
            <a class="admin-menu-card" href="${pageContext.request.contextPath}/admin/notifications">
              <div class="admin-menu-card__title">Hệ thống Thông báo</div>
              <p class="admin-menu-card__desc">Gửi thông báo sự kiện, voucher, ưu đãi giảm giá đến khách hàng.</p>
              <span class="admin-chip">Hệ thống / Broadcast</span>
            </a>
          </c:if>
        </div>
      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

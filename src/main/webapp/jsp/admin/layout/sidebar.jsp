<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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
<c:set var="canAuditLogs" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('AUDIT_LOG_VIEW'))}"/>

<aside class="admin-sidebar">
  <div class="admin-sidebar__inner">

    <a class="admin-brand" href="${pageContext.request.contextPath}/admin" aria-label="Bảng quản trị MyCosmetic">
      <span class="admin-brand__mark">MC</span>
      <span class="admin-brand__text">
        <strong>MyCosmetic</strong>
        <small>Bảng quản trị</small>
      </span>
    </a>

    <nav class="admin-nav">

      <div class="admin-nav__section">Tổng quan</div>

      <a class="${activeMenu == 'admin' ? 'is-active' : ''}"
         href="${pageContext.request.contextPath}/admin">
        <span>Trung tâm quản trị</span>
        <span class="admin-nav__meta">/admin</span>
      </a>

      <c:if test="${canRevenue}">
        <a class="${activeMenu == 'dashboard' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/dashboard">
          <span>Tổng quan doanh thu</span>
          <span class="admin-nav__meta">/admin/dashboard</span>
        </a>
      </c:if>

      <c:if test="${canProducts or canInventory or canCategories or canBrands or canBanners}">
        <div class="admin-nav__section">Sản phẩm &amp; danh mục</div>
      </c:if>

      <c:if test="${canProducts}">
        <a class="${activeMenu == 'products' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/products">
          <span>Sản phẩm</span>
          <span class="admin-nav__meta">/admin/products</span>
        </a>
      </c:if>

      <c:if test="${canInventory}">
        <a class="${activeMenu == 'inventory' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/inventory">
          <span>Quản lý tồn kho</span>
          <span class="admin-nav__meta">/admin/inventory</span>
        </a>
      </c:if>

      <c:if test="${canCategories}">
        <a class="${activeMenu == 'categories' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/categories">
          <span>Danh mục</span>
          <span class="admin-nav__meta">/admin/categories</span>
        </a>
      </c:if>

      <c:if test="${canBrands}">
        <a class="${activeMenu == 'brands' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/brands">
          <span>Thương hiệu</span>
          <span class="admin-nav__meta">/admin/brands</span>
        </a>
      </c:if>

      <c:if test="${canBanners}">
        <a class="${activeMenu == 'banners' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/banners">
          <span>Banner</span>
          <span class="admin-nav__meta">/admin/banners</span>
        </a>
      </c:if>

      <c:if test="${canOrders or canReturns}">
        <div class="admin-nav__section">Bán hàng</div>
      </c:if>

      <c:if test="${canOrders}">
        <a class="${activeMenu == 'orders' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/orders">
          <span>Đơn hàng</span>
          <span class="admin-nav__meta">/admin/orders</span>
        </a>
      </c:if>

      <c:if test="${canReturns}">
        <a class="${activeMenu == 'returns' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/returns">
          <span>Yêu cầu đổi trả</span>
          <span class="admin-nav__meta">/admin/returns</span>
        </a>

        <a class="${activeMenu == 'cancelRequests' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/cancel-requests">
          <span>Yêu cầu hủy đơn</span>
          <span class="admin-nav__meta">/admin/cancel-requests</span>
        </a>
      </c:if>

      <c:if test="${canPromotions or canFlashsale}">
        <div class="admin-nav__section">Khuyến mãi</div>

        <c:if test="${canPromotions}">
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
        </c:if>
      </c:if>

      <c:if test="${canFlashsale}">
        <a class="${activeMenu == 'flashSale' || activeMenu == 'flashsale' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/flash-sale">
          <span>Flash Sale</span>
          <span class="admin-nav__meta">/admin/flash-sale</span>
        </a>
      </c:if>

      <c:if test="${canReviews or canSettings or canCms or canContact or canNotifications}">
        <div class="admin-nav__section">Nội dung &amp; CMS</div>
      </c:if>
      <c:if test="${canReviews}">
        <a class="${activeMenu == 'reviews' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/reviews">
          <span>Quản lý bình luận</span>
          <span class="admin-nav__meta">/admin/reviews</span>
        </a>
      </c:if>

      <c:if test="${canSettings}">
        <a class="${activeMenu == 'settings' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/settings">
          <span>Cài đặt footer</span>
          <span class="admin-nav__meta">/admin/settings</span>
        </a>
      </c:if>

      <c:if test="${canCms}">
        <a class="${activeMenu == 'pages' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/pages">
          <span>Trang nội dung</span>
          <span class="admin-nav__meta">/admin/pages</span>
        </a>

        <a class="${activeMenu == 'events' || activeMenu == 'blogs' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/events">
          <span>Tin tức / Blog</span>
          <span class="admin-nav__meta">/admin/events</span>
        </a>
      </c:if>

      <c:if test="${canContact}">
        <a class="${activeMenu == 'contact' || activeMenu == 'contact-messages' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/contact-messages">
          <span>Tin nhắn liên hệ</span>
          <span class="admin-nav__meta">/admin/contact-messages</span>
        </a>
      </c:if>

      <c:if test="${canNotifications}">
        <a class="${activeMenu == 'notifications' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/notifications">
          <span>Thông báo</span>
          <span class="admin-nav__meta">/admin/notifications</span>
        </a>
      </c:if>

      <c:if test="${canUsers or canRoles or canRanks or canAuditLogs}">
        <div class="admin-nav__section">Hệ thống &amp; phân quyền</div>
      </c:if>

      <c:if test="${canUsers}">
        <a class="${activeMenu == 'users' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/users">
          <span>Quản lý người dùng</span>
          <span class="admin-nav__meta">/admin/users</span>
        </a>
      </c:if>

      <c:if test="${canRoles}">
        <a class="${activeMenu == 'roles' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/roles">
          <span>Vai trò &amp; phân quyền</span>
          <span class="admin-nav__meta">/admin/roles</span>
        </a>
      </c:if>

      <c:if test="${canRanks}">
        <a class="${activeMenu == 'ranks' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/ranks">
          <span>Hạng khách hàng</span>
          <span class="admin-nav__meta">/admin/ranks</span>
        </a>
      </c:if>

      <c:if test="${canAuditLogs}">
        <a class="${activeMenu == 'auditLogs' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/admin/audit-logs">
          <span>Nhật ký hệ thống</span>
          <span class="admin-nav__meta">/admin/audit-logs</span>
        </a>
      </c:if>

    </nav>

    <div class="admin-sidebar__footer">
      <hr class="admin-divider"/>
      <a class="admin-btn" href="${pageContext.request.contextPath}/logout">← Đăng xuất</a>
    </div>

  </div>
</aside>

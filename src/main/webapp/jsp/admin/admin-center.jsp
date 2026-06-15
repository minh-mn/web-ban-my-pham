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
<c:set var="canAuditLogs" value="${isSuperAdmin or (not empty adminPermissions and adminPermissions.contains('AUDIT_LOG_VIEW'))}"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">
    <jsp:include page="/jsp/admin/layout/topbar.jsp"/>

    <section class="admin-center-overview admin-card">
      <div class="admin-card__body">
        <div class="admin-row admin-center-overview__head">
          <div>
            <h2 class="admin-h2">Khu vực quản trị</h2>
            <p class="admin-subtext">Sắp xếp theo từng nhóm chức năng để thao tác nhanh, dễ nhìn và dễ phân biệt hơn.</p>
          </div>
          <span class="admin-chip admin-chip--brand">Nhóm chức năng</span>
        </div>

        <div class="admin-center-groups">

          <section class="admin-center-group admin-center-group--overview">
            <div class="admin-center-group__header">
              <div>                <h3 class="admin-center-group__title">Tổng quan</h3>
              </div>
              <p class="admin-center-group__desc">Các mục mở nhanh và theo dõi bức tranh chung của hệ thống.</p>
            </div>
            <div class="admin-grid admin-grid--3 admin-grid--group">
              <a class="admin-menu-card admin-menu-card--overview" href="${pageContext.request.contextPath}/">
                <span class="admin-menu-card__icon">🏠</span>
                <div class="admin-menu-card__title">Trang chủ</div>
                <p class="admin-menu-card__desc">Quay về giao diện người dùng để kiểm tra nhanh website.</p>
                <span class="admin-chip admin-menu-card__action">Xem trang chủ</span>
              </a>
              <c:if test="${canRevenue}">
                <a class="admin-menu-card admin-menu-card--overview" href="${pageContext.request.contextPath}/admin/dashboard">
                  <span class="admin-menu-card__icon">📊</span>
                  <div class="admin-menu-card__title">Tổng quan doanh thu</div>
                  <p class="admin-menu-card__desc">Theo dõi đơn hàng, doanh thu, KPI và hiệu quả kinh doanh.</p>
                  <span class="admin-chip admin-menu-card__action">Xem báo cáo</span>
                </a>
              </c:if>
            </div>
          </section>

          <c:if test="${canProducts or canInventory or canCategories or canBrands or canBanners}">
            <section class="admin-center-group admin-center-group--catalog">
              <div class="admin-center-group__header">
                <div>                  <h3 class="admin-center-group__title">Sản phẩm &amp; danh mục</h3>
                </div>
                <p class="admin-center-group__desc">Quản lý dữ liệu nền tảng của cửa hàng: sản phẩm, kho, thương hiệu, banner.</p>
              </div>
              <div class="admin-grid admin-grid--3 admin-grid--group">
                <c:if test="${canProducts}"><a class="admin-menu-card admin-menu-card--catalog" href="${pageContext.request.contextPath}/admin/products"><span class="admin-menu-card__icon">🧴</span><div class="admin-menu-card__title">Sản phẩm</div><p class="admin-menu-card__desc">Quản lý sản phẩm, hình ảnh, giá bán, tồn kho và trạng thái hiển thị.</p><span class="admin-chip admin-menu-card__action">Quản lý sản phẩm</span></a></c:if>
                <c:if test="${canInventory}"><a class="admin-menu-card admin-menu-card--catalog" href="${pageContext.request.contextPath}/admin/inventory"><span class="admin-menu-card__icon">📦</span><div class="admin-menu-card__title">Quản lý tồn kho</div><p class="admin-menu-card__desc">Theo dõi tồn kho, nhập hàng, cảnh báo sắp hết và thống kê nhập kho.</p><span class="admin-chip admin-menu-card__action">Kiểm tra tồn kho</span></a></c:if>
                <c:if test="${canCategories}"><a class="admin-menu-card admin-menu-card--catalog" href="${pageContext.request.contextPath}/admin/categories"><span class="admin-menu-card__icon">🗂️</span><div class="admin-menu-card__title">Danh mục</div><p class="admin-menu-card__desc">Quản lý danh mục sản phẩm, cây danh mục và cấu trúc phân loại.</p><span class="admin-chip admin-menu-card__action">Quản lý danh mục</span></a></c:if>
                <c:if test="${canBrands}"><a class="admin-menu-card admin-menu-card--catalog" href="${pageContext.request.contextPath}/admin/brands"><span class="admin-menu-card__icon">🏷️</span><div class="admin-menu-card__title">Thương hiệu</div><p class="admin-menu-card__desc">Quản lý thương hiệu sản phẩm và nội dung nhận diện thương hiệu.</p><span class="admin-chip admin-menu-card__action">Quản lý thương hiệu</span></a></c:if>
                <c:if test="${canBanners}"><a class="admin-menu-card admin-menu-card--catalog" href="${pageContext.request.contextPath}/admin/banners"><span class="admin-menu-card__icon">🖼️</span><div class="admin-menu-card__title">Banner</div><p class="admin-menu-card__desc">Quản lý banner slider và các hình ảnh nổi bật trên trang chủ.</p><span class="admin-chip admin-menu-card__action">Quản lý banner</span></a></c:if>
              </div>
            </section>
          </c:if>

          <c:if test="${canOrders or canReturns or canReviews}">
            <section class="admin-center-group admin-center-group--sales">
              <div class="admin-center-group__header">
                <div>                  <h3 class="admin-center-group__title">Bán hàng &amp; đơn hàng</h3>
                </div>
                <p class="admin-center-group__desc">Theo dõi vận hành bán hàng, xử lý đơn và chăm sóc sau mua.</p>
              </div>
              <div class="admin-grid admin-grid--3 admin-grid--group">
                <c:if test="${canOrders}"><a class="admin-menu-card admin-menu-card--sales" href="${pageContext.request.contextPath}/admin/orders"><span class="admin-menu-card__icon">🛒</span><div class="admin-menu-card__title">Đơn hàng</div><p class="admin-menu-card__desc">Xem danh sách đơn hàng, kiểm tra chi tiết và cập nhật trạng thái.</p><span class="admin-chip admin-menu-card__action">Xem đơn hàng</span></a></c:if>
                <c:if test="${canReturns}"><a class="admin-menu-card admin-menu-card--sales" href="${pageContext.request.contextPath}/admin/returns"><span class="admin-menu-card__icon">↩️</span><div class="admin-menu-card__title">Yêu cầu đổi trả</div><p class="admin-menu-card__desc">Duyệt và xử lý yêu cầu trả hàng, hoàn hàng của khách.</p><span class="admin-chip admin-menu-card__action">Duyệt yêu cầu</span></a></c:if>
                <c:if test="${canReviews}"><a class="admin-menu-card admin-menu-card--sales" href="${pageContext.request.contextPath}/admin/reviews"><span class="admin-menu-card__icon">⭐</span><div class="admin-menu-card__title">Đánh giá</div><p class="admin-menu-card__desc">Quản lý đánh giá và phản hồi khách hàng sau khi mua hàng.</p><span class="admin-chip admin-menu-card__action">Kiểm duyệt đánh giá</span></a></c:if>
              </div>
            </section>
          </c:if>

          <c:if test="${canPromotions or canFlashsale or canRanks or canNotifications}">
            <section class="admin-center-group admin-center-group--growth">
              <div class="admin-center-group__header">
                <div>                  <h3 class="admin-center-group__title">Khuyến mãi &amp; tăng trưởng</h3>
                </div>
                <p class="admin-center-group__desc">Các công cụ thúc đẩy doanh số, giữ chân khách hàng và truyền thông ưu đãi.</p>
              </div>
              <div class="admin-grid admin-grid--3 admin-grid--group">
                <c:if test="${canPromotions}"><a class="admin-menu-card admin-menu-card--growth" href="${pageContext.request.contextPath}/admin/promotions"><span class="admin-menu-card__icon">🎟️</span><div class="admin-menu-card__title">Khuyến mãi &amp; mã giảm giá</div><p class="admin-menu-card__desc">Quản lý tập trung mã giảm giá, ưu đãi theo thương hiệu và chương trình cửa hàng.</p><span class="admin-chip admin-menu-card__action">Quản lý khuyến mãi</span></a></c:if>
                <c:if test="${canFlashsale}"><a class="admin-menu-card admin-menu-card--growth" href="${pageContext.request.contextPath}/admin/flash-sale"><span class="admin-menu-card__icon">⚡</span><div class="admin-menu-card__title">Flash Sale</div><p class="admin-menu-card__desc">Thiết lập thời gian, sản phẩm và giá giảm sốc cho chương trình Flash Sale.</p><span class="admin-chip admin-menu-card__action">Quản lý Flash Sale</span></a></c:if>
                <c:if test="${canRanks}"><a class="admin-menu-card admin-menu-card--growth" href="${pageContext.request.contextPath}/admin/ranks"><span class="admin-menu-card__icon">👑</span><div class="admin-menu-card__title">Hạng khách hàng</div><p class="admin-menu-card__desc">Quản lý hạng thành viên, mốc chi tiêu và ưu đãi theo cấp độ.</p><span class="admin-chip admin-menu-card__action">Quản lý hạng</span></a></c:if>
                <c:if test="${canNotifications}"><a class="admin-menu-card admin-menu-card--growth" href="${pageContext.request.contextPath}/admin/notifications"><span class="admin-menu-card__icon">🔔</span><div class="admin-menu-card__title">Thông báo hệ thống</div><p class="admin-menu-card__desc">Gửi thông báo sự kiện, voucher và ưu đãi đến khách hàng.</p><span class="admin-chip admin-menu-card__action">Gửi thông báo</span></a></c:if>
              </div>
            </section>
          </c:if>

          <c:if test="${canCms or canContact or canSettings}">
            <section class="admin-center-group admin-center-group--content">
              <div class="admin-center-group__header">
                <div>                  <h3 class="admin-center-group__title">Nội dung &amp; website</h3>
                </div>
                <p class="admin-center-group__desc">Quản lý nội dung hiển thị trên website, liên hệ và cấu hình trang.</p>
              </div>
              <div class="admin-grid admin-grid--3 admin-grid--group">
                <c:if test="${canCms}"><a class="admin-menu-card admin-menu-card--content" href="${pageContext.request.contextPath}/admin/pages"><span class="admin-menu-card__icon">📄</span><div class="admin-menu-card__title">Trang nội dung CMS</div><p class="admin-menu-card__desc">Quản lý chính sách, điều khoản và các trang nội dung động.</p><span class="admin-chip admin-menu-card__action">Quản lý trang</span></a></c:if>
                <c:if test="${canCms}"><a class="admin-menu-card admin-menu-card--content" href="${pageContext.request.contextPath}/admin/events"><span class="admin-menu-card__icon">📝</span><div class="admin-menu-card__title">Tin tức / Blog</div><p class="admin-menu-card__desc">Quản lý bài viết, sự kiện và nội dung marketing trên trang chủ.</p><span class="admin-chip admin-menu-card__action">Quản lý bài viết</span></a></c:if>
                <c:if test="${canContact}"><a class="admin-menu-card admin-menu-card--content" href="${pageContext.request.contextPath}/admin/contact-messages"><span class="admin-menu-card__icon">✉️</span><div class="admin-menu-card__title">Tin nhắn liên hệ</div><p class="admin-menu-card__desc">Theo dõi và xử lý các tin nhắn khách hàng gửi từ form liên hệ.</p><span class="admin-chip admin-menu-card__action">Xem tin nhắn</span></a></c:if>
                <c:if test="${canSettings}"><a class="admin-menu-card admin-menu-card--content" href="${pageContext.request.contextPath}/admin/settings"><span class="admin-menu-card__icon">⚙️</span><div class="admin-menu-card__title">Cài đặt website</div><p class="admin-menu-card__desc">Cập nhật hotline, email, địa chỉ, social, footer và thông tin website.</p><span class="admin-chip admin-menu-card__action">Cập nhật thông tin</span></a></c:if>
              </div>
            </section>
          </c:if>

          <c:if test="${canUsers or canRoles or canAuditLogs}">
            <section class="admin-center-group admin-center-group--system">
              <div class="admin-center-group__header">
                <div>                  <h3 class="admin-center-group__title">Hệ thống &amp; phân quyền</h3>
                </div>
                <p class="admin-center-group__desc">Nhóm cấu hình người dùng nội bộ, phân quyền vận hành và theo dõi lịch sử thay đổi.</p>
              </div>
              <div class="admin-grid admin-grid--3 admin-grid--group">
                <c:if test="${canUsers}"><a class="admin-menu-card admin-menu-card--system" href="${pageContext.request.contextPath}/admin/users"><span class="admin-menu-card__icon">👥</span><div class="admin-menu-card__title">Quản lý người dùng</div><p class="admin-menu-card__desc">Quản lý tài khoản, trạng thái, vai trò và thông tin người dùng trong hệ thống.</p><span class="admin-chip admin-menu-card__action">Quản lý tài khoản</span></a></c:if>
                <c:if test="${canRoles}"><a class="admin-menu-card admin-menu-card--system" href="${pageContext.request.contextPath}/admin/roles"><span class="admin-menu-card__icon">🛡️</span><div class="admin-menu-card__title">Vai trò &amp; phân quyền</div><p class="admin-menu-card__desc">Tạo vai trò mới và cấu hình quyền chi tiết cho từng nhóm quản trị.</p><span class="admin-chip admin-menu-card__action">Cấu hình quyền</span></a></c:if>
                <c:if test="${canAuditLogs}"><a class="admin-menu-card admin-menu-card--system" href="${pageContext.request.contextPath}/admin/audit-logs"><span class="admin-menu-card__icon">🧾</span><div class="admin-menu-card__title">Nhật ký hệ thống</div><p class="admin-menu-card__desc">Truy vết lịch sử cập nhật dữ liệu, thay đổi trạng thái đơn hàng và thao tác admin.</p><span class="admin-chip admin-menu-card__action">Xem nhật ký</span></a></c:if>
              </div>
            </section>
          </c:if>

        </div>
      </div>
    </section>
  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

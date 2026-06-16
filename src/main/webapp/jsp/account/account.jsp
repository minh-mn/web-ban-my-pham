<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/account.css?v=20260616_voucher_home_v30" />

<fmt:setLocale value="vi_VN"/>

<section class="section account-dashboard">
    <div class="container order-page">

        <c:choose>
            <c:when test="${not empty sessionScope.user}">

                <c:set var="safeUsername" value="${empty sessionScope.user.username ? 'User' : sessionScope.user.username}" />

                <!-- ========================================================= -->
                <!-- ADMIN VIEW -->
                <!-- ========================================================= -->
                <c:if test="${sessionScope.user.admin}">

                    <!-- ADMIN ACCOUNT CENTER: gọn, tách phần quản trị khỏi hồ sơ cá nhân -->
                    <div class="mc-account-compact mc-admin-account">

                        <section class="mc-compact-hero mc-admin-hero">
                            <div class="mc-compact-user">
                                <div class="mc-compact-avatar mc-admin-avatar">
                                    <c:out value="${fn:toUpperCase(fn:substring(safeUsername, 0, 1))}" />
                                </div>

                                <div class="mc-compact-user-info">
                                    <div class="mc-compact-name-row">
                                        <h1>Tài khoản quản trị</h1>
                                        <span class="mc-compact-rank mc-admin-role-chip">ADMIN</span>

                                        <c:if test="${not empty rankLabel}">
                                            <span class="mc-compact-rank ${rankCss}">
                                                <c:out value="${rankLabel}" />
                                                <c:if test="${rankDiscount > 0}"> -<c:out value="${rankDiscount}" />%</c:if>
                                            </span>
                                        </c:if>
                                    </div>

                                    <p>
                                        Xin chào, <strong><c:out value="${safeUsername}" /></strong>.
                                        Trang này chỉ giữ các thông tin nhanh của tài khoản và lối tắt quản trị,
                                        không hiển thị toàn bộ dashboard để tránh trang tài khoản bị quá dài.
                                    </p>

                                    <div class="mc-compact-contact">
                                        <span>📧 <c:choose><c:when test="${not empty userEmail}"><c:out value="${userEmail}" /></c:when><c:otherwise>Chưa cập nhật email</c:otherwise></c:choose></span>
                                        <span>📱 <c:choose><c:when test="${not empty userPhone}"><c:out value="${userPhone}" /></c:when><c:otherwise>Chưa cập nhật SĐT</c:otherwise></c:choose></span>
                                    </div>
                                </div>
                            </div>

                            <div class="mc-compact-actions">
                                <a href="${pageContext.request.contextPath}/admin" class="mc-compact-btn is-primary">🛠 Vào Admin</a>
                                <button type="button" class="mc-compact-btn" data-account-target="profile">Sửa hồ sơ</button>
                                <a href="${pageContext.request.contextPath}/account/change-password" class="mc-compact-btn">Đổi mật khẩu</a>
                            </div>
                        </section>

                        <section class="mc-account-center mc-admin-account-center">
                            <aside class="mc-account-menu mc-admin-account-menu">
                                <div class="mc-account-menu-group">
                                    <div class="mc-account-menu-label">Quản trị</div>

                                    <button type="button" class="mc-account-menu-item is-active" data-account-target="overview">
                                        <span class="mc-menu-icon">📊</span>
                                        <span>
                                            <strong>Tổng quan nhanh</strong>
                                            <small>Doanh thu, đơn hàng, tồn kho</small>
                                        </span>
                                        <em>›</em>
                                    </button>

                                    <button type="button" class="mc-account-menu-item" data-account-target="report">
                                        <span class="mc-menu-icon">📈</span>
                                        <span>
                                            <strong>Báo cáo bán hàng</strong>
                                            <small>Biểu đồ và top sản phẩm</small>
                                        </span>
                                        <em>›</em>
                                    </button>
                                </div>

                                <div class="mc-account-menu-group">
                                    <div class="mc-account-menu-label">Tài khoản</div>

                                    <button type="button" class="mc-account-menu-item" data-account-target="profile">
                                        <span class="mc-menu-icon">👤</span>
                                        <span>
                                            <strong>Hồ sơ cá nhân</strong>
                                            <small>Cập nhật thông tin admin</small>
                                        </span>
                                        <em>›</em>
                                    </button>

                                    <button type="button" class="mc-account-menu-item" data-account-target="security">
                                        <span class="mc-menu-icon">🔐</span>
                                        <span>
                                            <strong>Bảo mật</strong>
                                            <small>Mật khẩu và quyền truy cập</small>
                                        </span>
                                        <em>›</em>
                                    </button>
                                </div>
                            </aside>

                            <div class="mc-account-content mc-admin-account-content">

                                <div class="mc-account-view is-active" data-account-view="overview">
                                    <div class="mc-view-head">
                                        <div>
                                            <h2>Tổng quan quản trị</h2>
                                            <p>Các chỉ số quan trọng nhất được rút gọn để xem nhanh trong trang tài khoản.</p>
                                        </div>
                                        <a class="mc-view-link" href="${pageContext.request.contextPath}/admin">Mở dashboard đầy đủ ›</a>
                                    </div>

                                    <div class="mc-admin-kpi-grid">
                                        <div class="mc-compact-stat mc-admin-stat">
                                            <span>💵</span>
                                            <strong>
                                                <c:choose>
                                                    <c:when test="${not empty admin_total_revenue_vnd}">
                                                        <fmt:formatNumber value="${admin_total_revenue_vnd}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                                    </c:when>
                                                    <c:otherwise>0 ₫</c:otherwise>
                                                </c:choose>
                                            </strong>
                                            <small>Tổng doanh thu hợp lệ</small>
                                        </div>

                                        <div class="mc-compact-stat mc-admin-stat">
                                            <span>🧾</span>
                                            <strong><c:out value="${empty admin_total_orders ? 0 : admin_total_orders}" /></strong>
                                            <small>Tổng số đơn hàng</small>
                                        </div>

                                        <div class="mc-compact-stat mc-admin-stat">
                                            <span>📊</span>
                                            <strong>
                                                <c:choose>
                                                    <c:when test="${not empty admin_aov_vnd}">
                                                        <fmt:formatNumber value="${admin_aov_vnd}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                                    </c:when>
                                                    <c:otherwise>0 ₫</c:otherwise>
                                                </c:choose>
                                            </strong>
                                            <small>Giá trị đơn trung bình</small>
                                        </div>

                                        <div class="mc-compact-stat mc-admin-stat">
                                            <span>
                                                <c:choose>
                                                    <c:when test="${empty revenue_diff_vnd or revenue_diff_vnd >= 0}">📈</c:when>
                                                    <c:otherwise>📉</c:otherwise>
                                                </c:choose>
                                            </span>
                                            <strong><c:out value="${empty revenue_percent ? 0 : revenue_percent}" />%</strong>
                                            <small>Tăng trưởng so với tháng trước</small>
                                        </div>
                                    </div>

                                    <div class="mc-mini-section mc-admin-module-section">
                                        <div class="mc-mini-section-head">
                                            <div>
                                                <h3>Truy cập nhanh</h3>
                                                <span>Các khu vực admin thường dùng</span>
                                            </div>
                                        </div>

                                        <div class="mc-admin-module-grid">
                                            <a href="${pageContext.request.contextPath}/admin/orders">
                                                <span>🧾</span>
                                                <strong>Đơn hàng</strong>
                                                <small>Xử lý và cập nhật trạng thái</small>
                                            </a>

                                            <a href="${pageContext.request.contextPath}/admin/products">
                                                <span>🧴</span>
                                                <strong>Sản phẩm</strong>
                                                <small>Quản lý sản phẩm và biến thể</small>
                                            </a>

                                            <a href="${pageContext.request.contextPath}/admin/users">
                                                <span>👥</span>
                                                <strong>Người dùng</strong>
                                                <small>Role, trạng thái, hạng thành viên</small>
                                            </a>

                                            <a href="${pageContext.request.contextPath}/admin/inventory">
                                                <span>📦</span>
                                                <strong>Kho hàng</strong>
                                                <small>Tồn kho và cảnh báo nhập hàng</small>
                                            </a>

                                            <a href="${pageContext.request.contextPath}/admin/promotions">
                                                <span>🏷️</span>
                                                <strong>Khuyến mãi</strong>
                                                <small>Voucher, flash sale, promotion</small>
                                            </a>

                                            <a href="${pageContext.request.contextPath}/admin/notifications">
                                                <span>🔔</span>
                                                <strong>Thông báo</strong>
                                                <small>Gửi và quản lý thông báo</small>
                                            </a>
                                        </div>
                                    </div>

                                    <div class="mc-admin-alert-grid">
                                        <div class="mc-admin-alert-card">
                                            <span>📉</span>
                                            <div>
                                                <strong><c:out value="${empty unsoldThisMonthCount ? 0 : unsoldThisMonthCount}" /></strong>
                                                <small>Không bán tháng này</small>
                                            </div>
                                        </div>

                                        <div class="mc-admin-alert-card">
                                            <span>🕒</span>
                                            <div>
                                                <strong><c:out value="${empty unsoldLast30DaysCount ? 0 : unsoldLast30DaysCount}" /></strong>
                                                <small>Không bán 30 ngày</small>
                                            </div>
                                        </div>

                                        <div class="mc-admin-alert-card is-danger">
                                            <span>🚫</span>
                                            <div>
                                                <strong><c:out value="${empty outOfStockCount ? 0 : outOfStockCount}" /></strong>
                                                <small>Hết hàng</small>
                                            </div>
                                        </div>

                                        <div class="mc-admin-alert-card is-warning">
                                            <span>⚠️</span>
                                            <div>
                                                <strong><c:out value="${empty lowStockCount ? 0 : lowStockCount}" /></strong>
                                                <small>Sắp hết hàng</small>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="mc-account-view" data-account-view="report">
                                    <div class="mc-view-head">
                                        <div>
                                            <h2>Báo cáo bán hàng</h2>
                                            <p>Rút gọn biểu đồ doanh thu và danh sách sản phẩm bán chạy để kiểm tra nhanh.</p>
                                        </div>
                                        <a class="mc-view-link" href="${pageContext.request.contextPath}/admin">Xem chi tiết ›</a>
                                    </div>

                                    <div class="mc-admin-report-grid">
                                        <section class="mc-mini-section mc-admin-report-card">
                                            <div class="mc-mini-section-head">
                                                <div>
                                                    <h3>🏬 Doanh thu toàn cửa hàng</h3>
                                                    <span>Biểu đồ doanh thu theo thời gian</span>
                                                </div>
                                            </div>

                                            <c:choose>
                                                <c:when test="${not empty admin_chart_labels and not empty admin_chart_values}">
                                                    <div class="mc-admin-chart-wrap">
                                                        <canvas id="storeRevenueChart" height="120"></canvas>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <p class="mc-empty-state"><span>📊</span> Chưa có dữ liệu doanh thu.</p>
                                                </c:otherwise>
                                            </c:choose>
                                        </section>

                                        <section class="mc-mini-section mc-admin-report-card">
                                            <div class="mc-mini-section-head">
                                                <div>
                                                    <h3>🔥 Top sản phẩm bán chạy</h3>
                                                    <span>Sản phẩm có doanh số tốt nhất</span>
                                                </div>
                                            </div>

                                            <div class="account-table-wrap mc-admin-table-wrap">
                                                <c:choose>
                                                    <c:when test="${not empty top_products}">
                                                        <table class="account-table mc-admin-mini-table">
                                                            <thead>
                                                            <tr>
                                                                <th>Sản phẩm</th>
                                                                <th class="account-text-right">Đã bán</th>
                                                                <th class="account-text-right">Doanh thu</th>
                                                            </tr>
                                                            </thead>

                                                            <tbody>
                                                            <c:forEach var="p" items="${top_products}">
                                                                <c:set var="pLen" value="${fn:length(p)}" />
                                                                <c:set var="isNewTopFormat" value="${pLen >= 4}" />

                                                                <tr>
                                                                    <td>
                                                                        <strong>
                                                                            <c:choose>
                                                                                <c:when test="${isNewTopFormat}">
                                                                                    <c:out value="${p[1]}" />
                                                                                </c:when>
                                                                                <c:otherwise>
                                                                                    <c:out value="${p[0]}" />
                                                                                </c:otherwise>
                                                                            </c:choose>
                                                                        </strong>
                                                                    </td>

                                                                    <td class="account-text-right">
                                                                        <c:choose>
                                                                            <c:when test="${isNewTopFormat}">
                                                                                <c:out value="${p[2]}" />
                                                                            </c:when>
                                                                            <c:otherwise>
                                                                                <c:out value="${p[1]}" />
                                                                            </c:otherwise>
                                                                        </c:choose>
                                                                    </td>

                                                                    <td class="account-text-right">
                                                                        <c:choose>
                                                                            <c:when test="${isNewTopFormat and not empty p[3]}">
                                                                                <fmt:formatNumber value="${p[3]}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                                                            </c:when>
                                                                            <c:when test="${not isNewTopFormat and not empty p[2]}">
                                                                                <fmt:formatNumber value="${p[2]}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                                                            </c:when>
                                                                            <c:otherwise>0 ₫</c:otherwise>
                                                                        </c:choose>
                                                                    </td>
                                                                </tr>
                                                            </c:forEach>
                                                            </tbody>
                                                        </table>
                                                    </c:when>

                                                    <c:otherwise>
                                                        <p class="mc-empty-state"><span>🧴</span> Chưa có dữ liệu bán hàng.</p>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </section>
                                    </div>
                                </div>

                                <div class="mc-account-view" data-account-view="profile">
                                    <div class="mc-view-head">
                                        <div>
                                            <h2>Hồ sơ quản trị</h2>
                                            <p>Cập nhật thông tin cá nhân, email, số điện thoại và địa chỉ giao hàng của tài khoản admin.</p>
                                        </div>
                                    </div>

                                    <div class="mc-profile-embed" data-profile-form-holder></div>
                                </div>

                                <div class="mc-account-view" data-account-view="security">
                                    <div class="mc-view-head">
                                        <div>
                                            <h2>Tài khoản & Bảo mật</h2>
                                            <p>Kiểm tra nhanh các thông tin đăng nhập và thao tác bảo mật của tài khoản quản trị.</p>
                                        </div>
                                    </div>

                                    <div class="mc-security-layout">
                                        <div class="mc-security-overview">
                                            <span class="mc-security-icon">🛡️</span>
                                            <div>
                                                <h3>Tài khoản có quyền quản trị</h3>
                                                <p>
                                                    Tài khoản admin có quyền truy cập các khu vực quản lý quan trọng.
                                                    Hãy đổi mật khẩu định kỳ và không chia sẻ OTP cho người khác.
                                                </p>
                                            </div>
                                        </div>

                                        <div class="mc-security-card-grid">
                                            <div class="mc-security-card">
                                                <span class="mc-security-card-icon">👤</span>
                                                <div>
                                                    <strong>Tên đăng nhập</strong>
                                                    <small><c:out value="${safeUsername}" /></small>
                                                    <p>Dùng để nhận diện tài khoản trong hệ thống.</p>
                                                </div>
                                            </div>

                                            <div class="mc-security-card">
                                                <span class="mc-security-card-icon">📧</span>
                                                <div>
                                                    <strong>Email</strong>
                                                    <small><c:choose><c:when test="${not empty userEmail}"><c:out value="${userEmail}" /></c:when><c:otherwise>Chưa cập nhật</c:otherwise></c:choose></small>
                                                    <p>Email dùng để nhận OTP và thông báo hệ thống.</p>
                                                </div>
                                            </div>
                                        </div>

                                        <div class="mc-security-list">
                                            <a href="${pageContext.request.contextPath}/account/change-password" class="mc-security-row is-danger">
                                                <span class="mc-security-row-icon">🔑</span>
                                                <div>
                                                    <strong>Đổi mật khẩu</strong>
                                                    <small>Nên thay đổi định kỳ để bảo vệ tài khoản quản trị.</small>
                                                </div>
                                                <em>Đổi ngay ›</em>
                                            </a>

                                            <a href="${pageContext.request.contextPath}/admin/users" class="mc-security-row">
                                                <span class="mc-security-row-icon">👥</span>
                                                <div>
                                                    <strong>Quản lý người dùng</strong>
                                                    <small>Kiểm tra role, trạng thái khóa/mở khóa và hạng thành viên của user.</small>
                                                </div>
                                                <em>Mở admin ›</em>
                                            </a>
                                        </div>
                                    </div>
                                </div>

                            </div>
                        </section>
                    </div>

                </c:if>

                <!-- ========================================================= -->
                <!-- USER VIEW -->
                <!-- ========================================================= -->
                <c:if test="${not sessionScope.user.admin}">

                    <c:choose>
                        <%-- CHẾ ĐỘ 1: GIAO DIỆN XEM TOÀN BỘ VÍ VOUCHER --%>
                        <c:when test="${param.view eq 'vouchers'}">

                            <div class="account-card account-section-space">
                                <div class="account-card-body">

                                    <!-- NÚT QUAY LẠI TRANG TÀI KHOẢN -->
                                    <div style="margin-bottom: 25px;">
                                        <a href="?" class="account-btn" style="background: #6b7280; color: #fff; padding: 8px 18px; border-radius: 20px; text-decoration: none; font-weight: bold; display: inline-flex; align-items: center; gap: 8px; border: none; transition: 0.3s;">
                                            ⬅ Quay lại trang tài khoản
                                        </a>
                                    </div>

                                    <div class="account-card-head account-card-head-start">
                                        <div>
                                            <h2 class="account-card-title" style="color: var(--pink-dark); font-size: 22px;">💼 Ví voucher của bạn (Tất cả mã)</h2>
                                            <p class="account-muted">
                                                Danh sách đầy đủ các mã giảm giá bạn đã thu thập thành công.
                                            </p>
                                        </div>
                                        <span class="account-chip user-chip" style="background-color: var(--pink-soft); color: var(--pink-main); font-weight: bold;">
                      🎰 Đã lưu: ${fn:length(savedCoupons)} mã
                    </span>
                                    </div>

                                    <c:choose>
                                        <c:when test="${not empty savedCoupons}">
                                            <!-- Hiển thị toàn bộ không giới hạn số lượng -->
                                            <div class="saved-voucher-grid saved-voucher-grid--full">
                                                <c:forEach var="savedCp" items="${savedCoupons}">
                                                    <c:set var="isRankVoucher" value="${not empty savedCp.type and savedCp.type eq 'RANK'}" />
                                                    <c:set var="isReviewVoucher" value="${not empty savedCp.type and savedCp.type eq 'REVIEW_REWARD'}" />
                                                    <c:set var="scope" value="${savedCp.applyScope}" />

                                                    <article class="saved-voucher-card">
                                                        <div class="saved-voucher-left">
                                                            <div class="saved-voucher-mark">
                                                                <span>MC</span>
                                                                <small>BEAUTY</small>
                                                            </div>
                                                        </div>

                                                        <div class="saved-voucher-divider"></div>

                                                        <div class="saved-voucher-content">
                                                            <div class="saved-voucher-top">
                                                                <div class="saved-voucher-tags">
                                                                    <span class="saved-voucher-tag saved-voucher-tag-hot">HOT</span>
                                                                    <c:choose>
                                                                        <c:when test="${isRankVoucher}">
                                                                            <span class="saved-voucher-tag saved-voucher-tag-soft">Hạng thành viên</span>
                                                                        </c:when>
                                                                        <c:when test="${isReviewVoucher}">
                                                                            <span class="saved-voucher-tag saved-voucher-tag-soft">Quà đánh giá</span>
                                                                        </c:when>
                                                                        <c:when test="${savedCp.type eq 'FREESHIP'}">
                                                                            <span class="saved-voucher-tag saved-voucher-tag-soft">Freeship</span>
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <span class="saved-voucher-tag saved-voucher-tag-soft">Ưu đãi</span>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </div>

                                                                <button type="button"
                                                                        class="saved-voucher-copy-btn"
                                                                        data-coupon-code="<c:out value='${savedCp.code}'/>"
                                                                        onclick="copyCouponCode(this.dataset.couponCode)">
                                                                    Copy
                                                                </button>
                                                            </div>

                                                            <div class="saved-voucher-code-row">
                                                                <strong><c:out value="${savedCp.code}" /></strong>
                                                            </div>

                                                            <div class="saved-voucher-discount">
                                                                <c:choose>
                                                                    <c:when test="${savedCp.type eq 'FREESHIP'}">
                                                                        Miễn phí vận chuyển
                                                                    </c:when>
                                                                    <c:when test="${savedCp.percentDiscount}">
                                                                        Giảm <b>${savedCp.discountPercent}%</b>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        Giảm <b><fmt:formatNumber value="${savedCp.discountValue}" type="number"/>đ</b>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </div>

                                                            <div class="saved-voucher-meta-list">
                                                                <div class="saved-voucher-meta-item">
                                                                    <span class="dot"></span>
                                                                    <span>
                                                                        <c:choose>
                                                                            <c:when test="${savedCp.minOrderAmount > 0}">
                                                                                Đơn hàng từ <b><fmt:formatNumber value="${savedCp.minOrderAmount}" type="number" groupingUsed="true" maxFractionDigits="0"/>đ</b>
                                                                            </c:when>
                                                                            <c:otherwise>Không giới hạn đơn hàng tối thiểu</c:otherwise>
                                                                        </c:choose>
                                                                    </span>
                                                                </div>

                                                                <c:if test="${not empty savedCp.maxDiscountAmount and savedCp.maxDiscountAmount > 0}">
                                                                    <div class="saved-voucher-meta-item">
                                                                        <span class="dot"></span>
                                                                        <span>Giảm tối đa <b><fmt:formatNumber value="${savedCp.maxDiscountAmount}" type="number" groupingUsed="true" maxFractionDigits="0"/>đ</b></span>
                                                                    </div>
                                                                </c:if>

                                                                <div class="saved-voucher-meta-item">
                                                                    <span class="dot"></span>
                                                                    <span>
                                                                        Áp dụng:
                                                                        <b>
                                                                            <c:choose>
                                                                                <c:when test="${empty scope or scope eq 'ALL'}">Tất cả sản phẩm</c:when>
                                                                                <c:when test="${scope eq 'BRAND'}">Theo thương hiệu</c:when>
                                                                                <c:when test="${scope eq 'PRODUCTS'}">Sản phẩm chỉ định</c:when>
                                                                                <c:otherwise><c:out value="${scope}" /></c:otherwise>
                                                                            </c:choose>
                                                                        </b>
                                                                    </span>
                                                                </div>

                                                                <c:if test="${not empty savedCp.minRankCode and savedCp.minRankCode ne 'MEMBER'}">
                                                                    <div class="saved-voucher-meta-item">
                                                                        <span class="dot"></span>
                                                                        <span>Hạng tối thiểu: <b><c:out value="${savedCp.minRankCode}" /></b></span>
                                                                    </div>
                                                                </c:if>
                                                            </div>

                                                            <c:if test="${not empty savedCp.description}">
                                                                <p class="saved-voucher-desc"><c:out value="${savedCp.description}" /></p>
                                                            </c:if>

                                                            <div class="saved-voucher-footer">
                                                                <span class="saved-voucher-ready">Sẵn sàng dùng tại Checkout</span>

                                                                <div class="saved-voucher-expire">
                                                                    HSD:
                                                                    <b>
                                                                        <c:choose>
                                                                            <c:when test="${not empty savedCp.endDate}">
                                                                                <c:out value="${savedCp.endDate}" />
                                                                            </c:when>
                                                                            <c:otherwise>Không giới hạn</c:otherwise>
                                                                        </c:choose>
                                                                    </b>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </article>
                                                </c:forEach>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div style="text-align: center; padding: 30px 10px; color: var(--text-muted);">
                                                <p style="font-size: 14px;">Bạn chưa lưu mã giảm giá nào.</p>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>

                                </div>
                            </div>
                        </c:when>




                        <%-- CHẾ ĐỘ 2: TRANG TỔNG QUAN TÀI KHOẢN (MẶC ĐỊNH) --%>
                        <c:otherwise>

                            <!-- COMPACT ACCOUNT CENTER -->
                            <div class="mc-account-compact">

                                <!-- Header gọn -->
                                <section class="mc-compact-hero">
                                    <div class="mc-compact-user">
                                        <div class="mc-compact-avatar">
                                            <c:out value="${fn:toUpperCase(fn:substring(safeUsername, 0, 1))}" />
                                        </div>

                                        <div class="mc-compact-user-info">
                                            <div class="mc-compact-name-row">
                                                <h1><c:out value="${safeUsername}" /></h1>
                                                <c:if test="${not empty rankLabel}">
                          <span class="mc-compact-rank ${rankCss}">
                            <c:out value="${rankLabel}" />
                            <c:if test="${rankDiscount > 0}"> -<c:out value="${rankDiscount}" />%</c:if>
                          </span>
                                                </c:if>
                                            </div>

                                            <p>Quản lý đơn hàng, voucher, tài khoản và hồ sơ giao hàng.</p>

                                            <div class="mc-compact-contact">
                                                <span>📧 <c:choose><c:when test="${not empty userEmail}"><c:out value="${userEmail}" /></c:when><c:otherwise>Chưa cập nhật email</c:otherwise></c:choose></span>
                                                <span>📱 <c:choose><c:when test="${not empty userPhone}"><c:out value="${userPhone}" /></c:when><c:otherwise>Chưa cập nhật SĐT</c:otherwise></c:choose></span>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Đã bỏ cụm nút nhanh: Đơn mua / Sửa hồ sơ / Đổi mật khẩu ở phần đầu tài khoản -->
                                </section>

                                <!-- Layout menu + nội dung -->
                                <section class="mc-account-center">
                                    <aside class="mc-account-menu">
                                        <div class="mc-account-menu-group">
                                            <div class="mc-account-menu-label">Tổng quan</div>

                                            <button type="button" class="mc-account-menu-item is-active" data-account-target="overview">
                                                <span class="mc-menu-icon">🏠</span>
                                                <span>
                          <strong>Tổng quan</strong>
                          <small>Thông tin nhanh</small>
                        </span>
                                                <em>›</em>
                                            </button>
                                        </div>

                                        <div class="mc-account-menu-group">
                                            <div class="mc-account-menu-label">Mua hàng</div>

                                            <button type="button" class="mc-account-menu-item" data-account-target="orders">
                                                <span class="mc-menu-icon">📦</span>
                                                <span>
                          <strong>Đơn mua</strong>
                          <small>Theo dõi đơn hàng</small>
                        </span>
                                                <em>›</em>
                                            </button>

                                            <button type="button" class="mc-account-menu-item" data-account-target="vouchers">
                                                <span class="mc-menu-icon">🎟️</span>
                                                <span>
                          <strong>Kho voucher</strong>
                          <small><c:out value="${empty savedCoupons ? 0 : fn:length(savedCoupons)}" /> mã đã lưu</small>
                        </span>
                                                <em>›</em>
                                            </button>
                                        </div>

                                        <div class="mc-account-menu-group">
                                            <div class="mc-account-menu-label">Tài khoản</div>

                                            <button type="button" class="mc-account-menu-item" data-account-target="profile">
                                                <span class="mc-menu-icon">👤</span>
                                                <span>
                          <strong>Hồ sơ cá nhân</strong>
                          <small>Sửa thông tin giao hàng</small>
                        </span>
                                                <em>›</em>
                                            </button>

                                            <button type="button" class="mc-account-menu-item" data-account-target="security">
                                                <span class="mc-menu-icon">🔐</span>
                                                <span>
                          <strong>Tài khoản & bảo mật</strong>
                          <small>Email, SĐT, mật khẩu</small>
                        </span>
                                                <em>›</em>
                                            </button>
                                        </div>

                                        <div class="mc-account-menu-group">
                                            <div class="mc-account-menu-label">Thành viên & hoạt động</div>

                                            <button type="button" class="mc-account-menu-item" data-account-target="rank">
                                                <span class="mc-menu-icon">🎖</span>
                                                <span>
                          <strong>Hạng thành viên</strong>
                          <small>Ưu đãi <c:out value="${empty rankDiscount ? 0 : rankDiscount}" />%</small>
                        </span>
                                                <em>›</em>
                                            </button>

                                            <button type="button" class="mc-account-menu-item" data-account-target="activity">
                                                <span class="mc-menu-icon">🔎</span>
                                                <span>
                          <strong>Hoạt động</strong>
                          <small>Tìm kiếm & chi tiêu</small>
                        </span>
                                                <em>›</em>
                                            </button>
                                        </div>
                                    </aside>

                                    <div class="mc-account-content">

                                        <!-- Tổng quan -->
                                        <div class="mc-account-view is-active" data-account-view="overview">
                                            <div class="mc-view-head">
                                                <div>
                                                    <h2>Tổng quan tài khoản</h2>
                                                    <p>Những thông tin quan trọng nhất của tài khoản.</p>
                                                </div>
                                            </div>

                                            <div class="mc-compact-stat-grid">
                                                <div class="mc-compact-stat">
                                                    <span>📦</span>
                                                    <strong><c:out value="${empty total_orders ? 0 : total_orders}" /></strong>
                                                    <small>Tổng đơn hàng</small>
                                                </div>

                                                <div class="mc-compact-stat">
                                                    <span>💰</span>
                                                    <strong>
                                                        <c:choose>
                                                            <c:when test="${not empty total_spent_vnd}">
                                                                <fmt:formatNumber value="${total_spent_vnd}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                                            </c:when>
                                                            <c:otherwise>0 ₫</c:otherwise>
                                                        </c:choose>
                                                    </strong>
                                                    <small>Tổng chi tiêu</small>
                                                </div>

                                                <div class="mc-compact-stat">
                                                    <span>🧾</span>
                                                    <strong>
                                                        <c:choose>
                                                            <c:when test="${not empty latest_order}">#<c:out value="${latest_order.id}" /></c:when>
                                                            <c:otherwise>--</c:otherwise>
                                                        </c:choose>
                                                    </strong>
                                                    <small>Đơn gần nhất</small>
                                                </div>

                                                <div class="mc-compact-stat">
                                                    <span>🎟️</span>
                                                    <strong><c:out value="${empty savedCoupons ? 0 : fn:length(savedCoupons)}" /></strong>
                                                    <small>Voucher đã lưu</small>
                                                </div>
                                            </div>

                                            <div class="mc-mini-section">
                                                <div class="mc-mini-section-head">
                                                    <h3>Tiện ích của tôi</h3>
                                                    <span>Chọn nhanh chức năng cần dùng</span>
                                                </div>

                                                <div class="mc-compact-utility-grid">
                                                    <button type="button" data-account-target="orders"><span>📦</span><strong>Đơn mua</strong><small>Theo dõi đơn</small></button>
                                                    <button type="button" data-account-target="vouchers"><span>🎟️</span><strong>Kho voucher</strong><small>Mã giảm giá</small></button>
                                                    <button type="button" data-account-target="rank"><span>🎖</span><strong>Hạng thành viên</strong><small>Ưu đãi</small></button>
                                                    <button type="button" data-account-target="activity"><span>🔎</span><strong>Hoạt động</strong><small>Lịch sử</small></button>
                                                    <button type="button" data-account-target="profile"><span>📍</span><strong>Địa chỉ</strong><small>Hồ sơ</small></button>
                                                    <button type="button" data-account-target="security"><span>🔐</span><strong>Bảo mật</strong><small>Tài khoản</small></button>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- Đơn mua -->
                                        <div class="mc-account-view" data-account-view="orders">
                                            <div class="mc-view-head">
                                                <div>
                                                    <h2>Đơn mua</h2>
                                                    <p>Theo dõi nhanh các trạng thái đơn hàng.</p>
                                                </div>
                                                <a href="${pageContext.request.contextPath}/orders" class="mc-view-link">Xem lịch sử mua hàng ›</a>
                                            </div>

                                            <div class="mc-order-shortcut-grid">
                                                <a href="${pageContext.request.contextPath}/orders?filter=processing">
                                                    <span>🕘</span>
                                                    <strong>Chờ xác nhận</strong>

                                                </a>
                                                <a href="${pageContext.request.contextPath}/orders?filter=confirmed">
                                                    <span>📦</span>
                                                    <strong>Chờ lấy hàng</strong>

                                                </a>
                                                <a href="${pageContext.request.contextPath}/orders?filter=shipping">
                                                    <span>🚚</span>
                                                    <strong>Đang giao</strong>

                                                </a>
                                                <a href="${pageContext.request.contextPath}/orders?filter=completed">
                                                    <span>⭐</span>
                                                    <strong>Đánh giá / Hoàn hàng</strong>

                                                </a>
                                            </div>
                                        </div>

                                        <!-- Voucher -->
                                        <div class="mc-account-view" data-account-view="vouchers">
                                            <div class="mc-view-head">
                                                <div>
                                                    <h2>Kho voucher</h2>
                                                    <p>Những mã giảm giá bạn đã lưu và có thể dùng khi checkout.</p>
                                                </div>
                                                <span class="mc-soft-count">Đã lưu: ${fn:length(savedCoupons)} mã</span>
                                            </div>

                                            <c:choose>
                                                <c:when test="${not empty savedCoupons}">
                                                    <div class="saved-voucher-grid saved-voucher-grid--compact">
                                                        <c:forEach var="savedCp" items="${savedCoupons}" end="5">
                                                            <c:set var="isRankVoucher" value="${not empty savedCp.type and savedCp.type eq 'RANK'}" />
                                                            <c:set var="isReviewVoucher" value="${not empty savedCp.type and savedCp.type eq 'REVIEW_REWARD'}" />
                                                            <c:set var="scope" value="${savedCp.applyScope}" />

                                                            <article class="saved-voucher-card saved-voucher-card--compact">
                                                                <div class="saved-voucher-left">
                                                                    <div class="saved-voucher-mark">
                                                                        <span>MC</span>
                                                                        <small>BEAUTY</small>
                                                                    </div>
                                                                </div>

                                                                <div class="saved-voucher-divider"></div>

                                                                <div class="saved-voucher-content">
                                                                    <div class="saved-voucher-top">
                                                                        <div class="saved-voucher-tags">
                                                                            <span class="saved-voucher-tag saved-voucher-tag-hot">HOT</span>
                                                                            <c:choose>
                                                                                <c:when test="${isRankVoucher}">
                                                                                    <span class="saved-voucher-tag saved-voucher-tag-soft">Hạng thành viên</span>
                                                                                </c:when>
                                                                                <c:when test="${isReviewVoucher}">
                                                                                    <span class="saved-voucher-tag saved-voucher-tag-soft">Quà đánh giá</span>
                                                                                </c:when>
                                                                                <c:when test="${savedCp.type eq 'FREESHIP'}">
                                                                                    <span class="saved-voucher-tag saved-voucher-tag-soft">Freeship</span>
                                                                                </c:when>
                                                                                <c:otherwise>
                                                                                    <span class="saved-voucher-tag saved-voucher-tag-soft">Ưu đãi</span>
                                                                                </c:otherwise>
                                                                            </c:choose>
                                                                        </div>

                                                                        <button type="button"
                                                                                class="saved-voucher-copy-btn"
                                                                                data-coupon-code="<c:out value='${savedCp.code}'/>"
                                                                                onclick="copyCouponCode(this.dataset.couponCode)">
                                                                            Copy
                                                                        </button>
                                                                    </div>

                                                                    <div class="saved-voucher-code-row">
                                                                        <strong><c:out value="${savedCp.code}" /></strong>
                                                                    </div>

                                                                    <div class="saved-voucher-discount">
                                                                        <c:choose>
                                                                            <c:when test="${savedCp.type eq 'FREESHIP'}">
                                                                                Miễn phí vận chuyển
                                                                            </c:when>
                                                                            <c:when test="${savedCp.percentDiscount}">
                                                                                Giảm <b>${savedCp.discountPercent}%</b>
                                                                            </c:when>
                                                                            <c:otherwise>
                                                                                Giảm <b><fmt:formatNumber value="${savedCp.discountValue}" type="number"/>đ</b>
                                                                            </c:otherwise>
                                                                        </c:choose>
                                                                    </div>

                                                                    <div class="saved-voucher-meta-list">
                                                                        <div class="saved-voucher-meta-item">
                                                                            <span class="dot"></span>
                                                                            <span>
                                                                                <c:choose>
                                                                                    <c:when test="${savedCp.minOrderAmount > 0}">
                                                                                        Đơn hàng từ <b><fmt:formatNumber value="${savedCp.minOrderAmount}" type="number" groupingUsed="true" maxFractionDigits="0"/>đ</b>
                                                                                    </c:when>
                                                                                    <c:otherwise>Không giới hạn đơn hàng tối thiểu</c:otherwise>
                                                                                </c:choose>
                                                                            </span>
                                                                        </div>

                                                                        <c:if test="${not empty savedCp.maxDiscountAmount and savedCp.maxDiscountAmount > 0}">
                                                                            <div class="saved-voucher-meta-item">
                                                                                <span class="dot"></span>
                                                                                <span>Giảm tối đa <b><fmt:formatNumber value="${savedCp.maxDiscountAmount}" type="number" groupingUsed="true" maxFractionDigits="0"/>đ</b></span>
                                                                            </div>
                                                                        </c:if>

                                                                        <div class="saved-voucher-meta-item">
                                                                            <span class="dot"></span>
                                                                            <span>
                                                                                Áp dụng:
                                                                                <b>
                                                                                    <c:choose>
                                                                                        <c:when test="${empty scope or scope eq 'ALL'}">Tất cả sản phẩm</c:when>
                                                                                        <c:when test="${scope eq 'BRAND'}">Theo thương hiệu</c:when>
                                                                                        <c:when test="${scope eq 'PRODUCTS'}">Sản phẩm chỉ định</c:when>
                                                                                        <c:otherwise><c:out value="${scope}" /></c:otherwise>
                                                                                    </c:choose>
                                                                                </b>
                                                                            </span>
                                                                        </div>
                                                                    </div>

                                                                    <c:if test="${not empty savedCp.description}">
                                                                        <p class="saved-voucher-desc"><c:out value="${savedCp.description}" /></p>
                                                                    </c:if>

                                                                    <div class="saved-voucher-footer">
                                                                        <span class="saved-voucher-ready">Sẵn sàng dùng tại Checkout</span>
                                                                    </div>
                                                                </div>
                                                            </article>
                                                        </c:forEach>
                                                    </div>

                                                    <c:if test="${fn:length(savedCoupons) > 6}">
                                                        <div class="mc-center-action">
                                                            <a href="?view=vouchers" class="mc-compact-btn is-primary">Xem tất cả mã giảm giá (${fn:length(savedCoupons)}) ›</a>
                                                        </div>
                                                    </c:if>
                                                </c:when>

                                                <c:otherwise>
                                                    <div class="mc-empty-state">
                                                        <span>🎟️</span>
                                                        <p>Bạn chưa lưu mã giảm giá nào.</p>
                                                        <a href="${pageContext.request.contextPath}/" class="mc-compact-btn is-primary">Tìm ưu đãi</a>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>

                                        <!-- Rank -->
                                        <div class="mc-account-view" data-account-view="rank">
                                            <div class="mc-view-head">
                                                <div>
                                                    <h2>Hạng thành viên</h2>
                                                    <p>Theo dõi hạng hiện tại, mốc cần đạt và quyền lợi của từng hạng để bạn dễ nâng hạng hơn.</p>
                                                </div>
                                                <span class="mc-compact-rank ${rankCss}">
                          <c:out value="${empty rankLabel ? 'Thành viên' : rankLabel}" />
                          <c:if test="${rankDiscount > 0}"> -<c:out value="${rankDiscount}" />%</c:if>
                        </span>
                                            </div>

                                            <div class="mc-rank-info-strip">
                                                <div class="mc-rank-info-item">
                                                    <strong>Hạng hiện tại</strong>
                                                    <span><c:out value="${empty rankLabel ? 'Thành viên' : rankLabel}" /></span>
                                                </div>
                                                <div class="mc-rank-info-item">
                                                    <strong>Ưu đãi đang áp dụng</strong>
                                                    <span><c:out value="${empty rankDiscount ? 0 : rankDiscount}" />%</span>
                                                </div>
                                                <div class="mc-rank-info-item">
                                                    <strong>Cách tính hạng</strong>
                                                    <span>Chỉ tính các đơn đã thanh toán thành công và cộng dồn theo tổng chi tiêu.</span>
                                                </div>
                                            </div>

                                            <div class="mc-rank-hero-grid">
                                                <div class="mc-rank-hero-card is-current">
                                                    <div class="mc-rank-hero-label">Hạng hiện tại của bạn</div>
                                                    <div class="mc-rank-hero-title-row">
                                                        <h3><c:out value="${empty rankLabel ? 'Thành viên' : rankLabel}" /></h3>
                                                        <span class="mc-rank-discount-chip">Ưu đãi <c:out value="${empty rankDiscount ? 0 : rankDiscount}" />%</span>
                                                    </div>
                                                    <p>
                                                        Bạn đã tích lũy <strong><fmt:formatNumber value="${empty rankTotalSpent ? 0 : rankTotalSpent}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</strong>
                                                        và có <strong><c:out value="${empty rankPaidOrderCount ? 0 : rankPaidOrderCount}" /></strong> đơn hợp lệ dùng để xét hạng.
                                                    </p>
                                                    <div class="mc-rank-hero-mini-grid">
                                                        <div class="mc-rank-mini-item">
                                                            <span>Tổng chi tiêu hiện tại</span>
                                                            <strong><fmt:formatNumber value="${empty rankTotalSpent ? 0 : rankTotalSpent}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</strong>
                                                        </div>
                                                        <div class="mc-rank-mini-item">
                                                            <span>Số đơn hợp lệ</span>
                                                            <strong><c:out value="${empty rankPaidOrderCount ? 0 : rankPaidOrderCount}" /></strong>
                                                        </div>
                                                    </div>
                                                </div>

                                                <div class="mc-rank-hero-card is-next">
                                                    <div class="mc-rank-hero-label">Mục tiêu tiếp theo</div>
                                                    <c:choose>
                                                        <c:when test="${maxRank}">
                                                            <div class="mc-rank-hero-title-row">
                                                                <h3>Bạn đã ở hạng cao nhất</h3>
                                                                <span class="mc-rank-status-badge is-top">Top rank</span>
                                                            </div>
                                                            <p>Bạn đã đạt mức ưu đãi cao nhất của hệ thống. Hãy tiếp tục mua sắm để duy trì hạng và tận hưởng toàn bộ quyền lợi hiện có.</p>
                                                            <div class="mc-rank-target-highlight is-top">
                                                                <strong>Chúc mừng!</strong>
                                                                <span>Hiện bạn đang ở hạng tối đa nên không còn mốc kế tiếp.</span>
                                                            </div>
                                                            <div class="mc-rank-hero-mini-grid">
                                                                <div class="mc-rank-mini-item">
                                                                    <span>Ưu đãi đang nhận</span>
                                                                    <strong><c:out value="${empty rankDiscount ? 0 : rankDiscount}" />%</strong>
                                                                </div>
                                                                <div class="mc-rank-mini-item">
                                                                    <span>Trạng thái</span>
                                                                    <strong>Đã tối đa</strong>
                                                                </div>
                                                            </div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <div class="mc-rank-hero-title-row">
                                                                <h3>Lên hạng <c:out value="${empty nextRankLabel ? 'tiếp theo' : nextRankLabel}" /></h3>
                                                                <span class="mc-rank-status-badge">Còn <fmt:formatNumber value="${empty amountToNextRank ? 0 : amountToNextRank}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</span>
                                                            </div>
                                                            <p>
                                                                Mốc cần đạt là <strong><fmt:formatNumber value="${empty nextRankMinSpent ? 0 : nextRankMinSpent}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</strong>.
                                                                Khi đạt mốc này, bạn sẽ được nâng lên hạng <strong><c:out value="${empty nextRankLabel ? 'tiếp theo' : nextRankLabel}" /></strong> với ưu đãi tốt hơn.
                                                            </p>
                                                            <div class="mc-rank-target-highlight">
                                                                <strong>Bạn cần chi thêm <fmt:formatNumber value="${empty amountToNextRank ? 0 : amountToNextRank}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</strong>
                                                                <span>để chạm mốc <fmt:formatNumber value="${empty nextRankMinSpent ? 0 : nextRankMinSpent}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫ và lên hạng <c:out value="${empty nextRankLabel ? 'tiếp theo' : nextRankLabel}" />.</span>
                                                            </div>
                                                            <div class="mc-rank-hero-mini-grid">
                                                                <div class="mc-rank-mini-item">
                                                                    <span>Mốc tiếp theo</span>
                                                                    <strong><fmt:formatNumber value="${empty nextRankMinSpent ? 0 : nextRankMinSpent}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</strong>
                                                                </div>
                                                                <div class="mc-rank-mini-item">
                                                                    <span>Cần thêm</span>
                                                                    <strong><fmt:formatNumber value="${empty amountToNextRank ? 0 : amountToNextRank}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</strong>
                                                                </div>
                                                            </div>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>

                                            <div class="mc-rank-summary">
                                                <div>
                                                    <span>Tổng chi tiêu xét hạng</span>
                                                    <strong><fmt:formatNumber value="${empty rankTotalSpent ? 0 : rankTotalSpent}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</strong>
                                                </div>
                                                <div>
                                                    <span>Ưu đãi hiện tại</span>
                                                    <strong><c:out value="${empty rankDiscount ? 0 : rankDiscount}" />%</strong>
                                                </div>
                                                <div>
                                                    <span>Đơn đã thanh toán</span>
                                                    <strong><c:out value="${empty rankPaidOrderCount ? 0 : rankPaidOrderCount}" /></strong>
                                                </div>
                                            </div>

                                            <div class="mc-rank-progress-panel">
                                                <div class="mc-rank-progress-head">
                                                    <div>
                                                        <h3>Tiến độ lên hạng</h3>
                                                        <p>Thanh tiến độ này cho biết bạn đã đi được bao xa so với mốc hạng kế tiếp.</p>
                                                    </div>
                                                    <span class="mc-rank-progress-value">
                            <c:choose>
                                <c:when test="${maxRank}">100%</c:when>
                                <c:otherwise><c:out value="${empty rankProgressPercent ? 0 : rankProgressPercent}" />%</c:otherwise>
                            </c:choose>
                          </span>
                                                </div>

                                                <div class="mc-rank-progress"><div class="mc-rank-progress-bar account-rank-progress__bar ${maxRank ? 'is-full' : ''}" data-progress="${empty rankProgressPercent ? 0 : rankProgressPercent}"></div></div>

                                                <div class="mc-rank-progress-meta">
                                                    <div>
                                                        <span>Hiện tại</span>
                                                        <strong><fmt:formatNumber value="${empty rankTotalSpent ? 0 : rankTotalSpent}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</strong>
                                                    </div>
                                                    <div>
                                                        <span>Mốc tiếp theo</span>
                                                        <strong>
                                                            <c:choose>
                                                                <c:when test="${maxRank}">Đã đạt hạng cao nhất</c:when>
                                                                <c:otherwise><fmt:formatNumber value="${empty nextRankMinSpent ? 0 : nextRankMinSpent}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</c:otherwise>
                                                            </c:choose>
                                                        </strong>
                                                    </div>
                                                    <div>
                                                        <span>Cần thêm</span>
                                                        <strong>
                                                            <c:choose>
                                                                <c:when test="${maxRank}">0 ₫</c:when>
                                                                <c:otherwise><fmt:formatNumber value="${empty amountToNextRank ? 0 : amountToNextRank}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</c:otherwise>
                                                            </c:choose>
                                                        </strong>
                                                    </div>
                                                </div>
                                            </div>

                                            <div class="mc-rank-rule-grid">
                                                <div class="mc-rank-rule-card">
                                                    <strong>1. Đơn nào được tính?</strong>
                                                    <span>Chỉ các đơn đã thanh toán thành công mới được cộng vào tổng chi tiêu xét hạng.</span>
                                                </div>
                                                <div class="mc-rank-rule-card">
                                                    <strong>2. Khi nào lên hạng?</strong>
                                                    <span>Khi tổng chi tiêu của bạn chạm đúng hoặc vượt mốc của hạng tiếp theo.</span>
                                                </div>
                                                <div class="mc-rank-rule-card">
                                                    <strong>3. Lợi ích khi lên hạng</strong>
                                                    <span>Bạn nhận mức ưu đãi cao hơn và được mở thêm voucher theo từng hạng.</span>
                                                </div>
                                            </div>

                                            <div class="mc-rank-tier-board">
                                                <div class="mc-section-title-row">
                                                    <div>
                                                        <h3>Các mốc hạng thành viên</h3>
                                                        <p>Bảng dưới đây cho biết rõ từng hạng cần bao nhiêu chi tiêu và nhận được ưu đãi nào.</p>
                                                    </div>
                                                </div>

                                                <div class="mc-rank-roadmap">
                                                    <div class="mc-rank-roadmap-line"></div>
                                                    <div class="mc-rank-roadmap-step ${currentRankCode == 'MEMBER' ? 'is-current' : 'is-complete'}">
                                                        <span class="mc-rank-roadmap-dot"></span>
                                                        <strong>Thành viên</strong>
                                                        <small>0 ₫</small>
                                                    </div>
                                                    <div class="mc-rank-roadmap-step ${currentRankCode == 'SILVER' ? 'is-current' : (currentRankCode == 'GOLD' || currentRankCode == 'DIAMOND' || currentRankCode == 'VIP' ? 'is-complete' : '')}">
                                                        <span class="mc-rank-roadmap-dot"></span>
                                                        <strong>Bạc</strong>
                                                        <small>1.000.000 ₫</small>
                                                    </div>
                                                    <div class="mc-rank-roadmap-step ${currentRankCode == 'GOLD' ? 'is-current' : (currentRankCode == 'DIAMOND' || currentRankCode == 'VIP' ? 'is-complete' : '')}">
                                                        <span class="mc-rank-roadmap-dot"></span>
                                                        <strong>Vàng</strong>
                                                        <small>3.000.000 ₫</small>
                                                    </div>
                                                    <div class="mc-rank-roadmap-step ${currentRankCode == 'DIAMOND' ? 'is-current' : (currentRankCode == 'VIP' ? 'is-complete' : '')}">
                                                        <span class="mc-rank-roadmap-dot"></span>
                                                        <strong>Kim cương</strong>
                                                        <small>7.000.000 ₫</small>
                                                    </div>
                                                    <div class="mc-rank-roadmap-step ${currentRankCode == 'VIP' ? 'is-current' : ''}">
                                                        <span class="mc-rank-roadmap-dot"></span>
                                                        <strong>VIP</strong>
                                                        <small>15.000.000 ₫</small>
                                                    </div>
                                                </div>

                                                <div class="mc-rank-compare-table-wrap">
                                                    <table class="mc-rank-compare-table">
                                                        <thead>
                                                        <tr>
                                                            <th>Hạng</th>
                                                            <th>Mốc chi tiêu</th>
                                                            <th>Ưu đãi</th>
                                                            <th>Quyền lợi chính</th>
                                                        </tr>
                                                        </thead>
                                                        <tbody>
                                                        <tr class="${currentRankCode == 'MEMBER' ? 'is-current' : ''}">
                                                            <td><strong>Thành viên</strong></td>
                                                            <td>Từ 0 ₫</td>
                                                            <td>0%</td>
                                                            <td>Dùng voucher cơ bản, bắt đầu tích lũy chi tiêu.</td>
                                                        </tr>
                                                        <tr class="${currentRankCode == 'SILVER' ? 'is-current' : ''}">
                                                            <td><strong>Bạc</strong></td>
                                                            <td>Từ 1.000.000 ₫</td>
                                                            <td>3%</td>
                                                            <td>Mở thêm voucher hạng Bạc.</td>
                                                        </tr>
                                                        <tr class="${currentRankCode == 'GOLD' ? 'is-current' : ''}">
                                                            <td><strong>Vàng</strong></td>
                                                            <td>Từ 3.000.000 ₫</td>
                                                            <td>5%</td>
                                                            <td>Ưu đãi tốt hơn và thêm voucher hạng Vàng.</td>
                                                        </tr>
                                                        <tr class="${currentRankCode == 'DIAMOND' ? 'is-current' : ''}">
                                                            <td><strong>Kim cương</strong></td>
                                                            <td>Từ 7.000.000 ₫</td>
                                                            <td>8%</td>
                                                            <td>Voucher hạng cao và quyền lợi nổi bật hơn.</td>
                                                        </tr>
                                                        <tr class="${currentRankCode == 'VIP' ? 'is-current' : ''}">
                                                            <td><strong>VIP</strong></td>
                                                            <td>Từ 15.000.000 ₫</td>
                                                            <td>10%</td>
                                                            <td>Mở toàn bộ voucher theo hạng và ưu đãi cao nhất.</td>
                                                        </tr>
                                                        </tbody>
                                                    </table>
                                                </div>

                                                <div class="mc-rank-tier-grid">
                                                    <div class="mc-rank-tier-card ${currentRankCode == 'MEMBER' ? 'is-current' : 'is-complete'}">
                                                        <div class="mc-rank-tier-top">
                                                            <span class="mc-rank-tier-badge">Member</span>
                                                            <span class="mc-rank-tier-state">${currentRankCode == 'MEMBER' ? 'Đang giữ' : 'Mặc định'}</span>
                                                        </div>
                                                        <h4>Thành viên</h4>
                                                        <p class="mc-rank-tier-sub">Bắt đầu khi tạo tài khoản và tích lũy từ đơn mua đầu tiên.</p>
                                                        <div class="mc-rank-tier-price">0 ₫</div>
                                                        <ul class="mc-rank-tier-list">
                                                            <li>Ưu đãi hạng: 0%</li>
                                                            <li>Dùng được voucher cơ bản</li>
                                                        </ul>
                                                    </div>

                                                    <div class="mc-rank-tier-card ${currentRankCode == 'SILVER' ? 'is-current' : (currentRankCode == 'GOLD' || currentRankCode == 'DIAMOND' || currentRankCode == 'VIP' ? 'is-complete' : '')}">
                                                        <div class="mc-rank-tier-top">
                                                            <span class="mc-rank-tier-badge">Silver</span>
                                                            <span class="mc-rank-tier-state">Từ 1.000.000 ₫</span>
                                                        </div>
                                                        <h4>Bạc</h4>
                                                        <p class="mc-rank-tier-sub">Mốc mở khóa đầu tiên cho khách hàng bắt đầu mua sắm thường xuyên.</p>
                                                        <div class="mc-rank-tier-price">1.000.000 ₫</div>
                                                        <ul class="mc-rank-tier-list">
                                                            <li>Ưu đãi hạng: 3%</li>
                                                            <li>Mở thêm voucher cho hạng Bạc</li>
                                                        </ul>
                                                    </div>

                                                    <div class="mc-rank-tier-card ${currentRankCode == 'GOLD' ? 'is-current' : (currentRankCode == 'DIAMOND' || currentRankCode == 'VIP' ? 'is-complete' : '')}">
                                                        <div class="mc-rank-tier-top">
                                                            <span class="mc-rank-tier-badge">Gold</span>
                                                            <span class="mc-rank-tier-state">Từ 3.000.000 ₫</span>
                                                        </div>
                                                        <h4>Vàng</h4>
                                                        <p class="mc-rank-tier-sub">Ưu đãi tốt hơn cho khách hàng thân thiết và có tần suất mua ổn định.</p>
                                                        <div class="mc-rank-tier-price">3.000.000 ₫</div>
                                                        <ul class="mc-rank-tier-list">
                                                            <li>Ưu đãi hạng: 5%</li>
                                                            <li>Nhận thêm voucher hạng Vàng</li>
                                                        </ul>
                                                    </div>

                                                    <div class="mc-rank-tier-card ${currentRankCode == 'DIAMOND' ? 'is-current' : (currentRankCode == 'VIP' ? 'is-complete' : '')}">
                                                        <div class="mc-rank-tier-top">
                                                            <span class="mc-rank-tier-badge">Diamond</span>
                                                            <span class="mc-rank-tier-state">Từ 7.000.000 ₫</span>
                                                        </div>
                                                        <h4>Kim cương</h4>
                                                        <p class="mc-rank-tier-sub">Dành cho khách hàng mua sắm thường xuyên và có giá trị đơn hàng cao.</p>
                                                        <div class="mc-rank-tier-price">7.000.000 ₫</div>
                                                        <ul class="mc-rank-tier-list">
                                                            <li>Ưu đãi hạng: 8%</li>
                                                            <li>Voucher hạng cao và quyền lợi tốt hơn</li>
                                                        </ul>
                                                    </div>

                                                    <div class="mc-rank-tier-card is-vip ${currentRankCode == 'VIP' ? 'is-current' : ''}">
                                                        <div class="mc-rank-tier-top">
                                                            <span class="mc-rank-tier-badge">VIP</span>
                                                            <span class="mc-rank-tier-state">Từ 15.000.000 ₫</span>
                                                        </div>
                                                        <h4>VIP</h4>
                                                        <p class="mc-rank-tier-sub">Hạng cao nhất của MyCosmetic, dành cho khách hàng thân thiết nhất.</p>
                                                        <div class="mc-rank-tier-price">15.000.000 ₫</div>
                                                        <ul class="mc-rank-tier-list">
                                                            <li>Ưu đãi hạng: 10%</li>
                                                            <li>Mở toàn bộ voucher theo hạng</li>
                                                        </ul>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>


                                        <!-- Activity -->
                                        <div class="mc-account-view" data-account-view="activity">
                                            <div class="mc-view-head">
                                                <div>
                                                    <h2>Hoạt động gần đây</h2>
                                                    <p>Theo dõi lịch sử tìm kiếm và xu hướng chi tiêu của bạn.</p>
                                                </div>
                                            </div>

                                            <div class="mc-activity-summary-grid">
                                                <div class="mc-activity-summary-card">
                                                    <span>🔎</span>
                                                    <div>
                                                        <strong><c:out value="${empty searchHistoryCount ? 0 : searchHistoryCount}" /></strong>
                                                        <p>Lượt tìm kiếm</p>
                                                    </div>
                                                </div>

                                                <div class="mc-activity-summary-card">
                                                    <span>💰</span>
                                                    <div>
                                                        <strong>
                                                            <c:choose>
                                                                <c:when test="${not empty total_spent_vnd}">
                                                                    <fmt:formatNumber value="${total_spent_vnd}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                                                </c:when>
                                                                <c:otherwise>0 ₫</c:otherwise>
                                                            </c:choose>
                                                        </strong>
                                                        <p>Tổng chi tiêu</p>
                                                    </div>
                                                </div>
                                            </div>

                                            <div class="mc-activity-stack">
                                                <div class="mc-mini-section mc-search-section">
                                                    <div class="mc-mini-section-head">
                                                        <div>
                                                            <h3>Lịch sử tìm kiếm</h3>
                                                            <span>Các từ khóa bạn đã tìm gần đây.</span>
                                                        </div>

                                                        <c:if test="${not empty searchHistories}">
                                                            <form method="post"
                                                                  action="${pageContext.request.contextPath}/account/search-history/clear"
                                                                  class="account-inline-form"
                                                                  onsubmit="return confirm('Bạn có chắc muốn xóa toàn bộ lịch sử tìm kiếm không?');">
                                                                <c:if test="${not empty csrfToken}">
                                                                    <input type="hidden" name="csrfToken" value="${fn:escapeXml(csrfToken)}" />
                                                                </c:if>
                                                                <c:if test="${empty csrfToken and not empty sessionScope.csrfToken}">
                                                                    <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.csrfToken)}" />
                                                                </c:if>
                                                                <button type="submit" class="mc-clear-history-btn">Xóa tất cả</button>
                                                            </form>
                                                        </c:if>
                                                    </div>

                                                    <c:choose>
                                                        <c:when test="${not empty searchHistories}">
                                                            <div class="mc-search-list mc-search-list-wide">
                                                                <c:forEach var="history" items="${searchHistories}">
                                                                    <c:choose>
                                                                        <c:when test="${not empty history.searchUrl}">
                                                                            <c:set var="historyHref" value="${pageContext.request.contextPath}${history.searchUrl}" />
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <c:url var="historyHref" value="/search">
                                                                                <c:param name="q" value="${history.keyword}" />
                                                                            </c:url>
                                                                        </c:otherwise>
                                                                    </c:choose>

                                                                    <div class="mc-search-item">
                                                                        <a href="${fn:escapeXml(historyHref)}">
                                                                            <span>🔍</span>
                                                                            <div>
                                                                                <strong><c:out value="${history.keyword}" /></strong>
                                                                                <small>
                                                                                    <c:out value="${empty history.resultCount ? 0 : history.resultCount}" /> kết quả
                                                                                    • Đã tìm <c:out value="${empty history.searchCount ? 1 : history.searchCount}" /> lần
                                                                                    • <c:out value="${history.displayLastSearchedAt}" />
                                                                                </small>
                                                                            </div>
                                                                        </a>

                                                                        <form method="post"
                                                                              action="${pageContext.request.contextPath}/account/search-history/delete"
                                                                              class="account-inline-form"
                                                                              onsubmit="return confirm('Xóa từ khóa tìm kiếm này?');">
                                                                            <c:if test="${not empty csrfToken}">
                                                                                <input type="hidden" name="csrfToken" value="${fn:escapeXml(csrfToken)}" />
                                                                            </c:if>
                                                                            <c:if test="${empty csrfToken and not empty sessionScope.csrfToken}">
                                                                                <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.csrfToken)}" />
                                                                            </c:if>
                                                                            <input type="hidden" name="id" value="${history.id}" />
                                                                            <button type="submit" class="mc-search-delete-btn" title="Xóa lịch sử">×</button>
                                                                        </form>
                                                                    </div>
                                                                </c:forEach>
                                                            </div>
                                                        </c:when>

                                                        <c:otherwise>
                                                            <div class="mc-empty-state">
                                                                <span>🕘</span>
                                                                <p>Bạn chưa có lịch sử tìm kiếm.</p>
                                                            </div>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>

                                                <div class="mc-mini-section mc-spending-section">
                                                    <div class="mc-mini-section-head">
                                                        <div>
                                                            <h3>Chi tiêu theo thời gian</h3>
                                                            <span>Biểu đồ tổng tiền mua hàng theo từng mốc thời gian.</span>
                                                        </div>
                                                        <span class="mc-soft-count">Spending</span>
                                                    </div>

                                                    <c:choose>
                                                        <c:when test="${not empty chart_labels and not empty chart_values}">
                                                            <div class="mc-chart-wrap mc-chart-wrap-wide">
                                                                <canvas id="spendingChart" height="120"></canvas>
                                                            </div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <div class="mc-empty-state">
                                                                <span>📊</span>
                                                                <p>Chưa có dữ liệu chi tiêu.</p>
                                                            </div>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- Profile -->
                                        <div class="mc-account-view" data-account-view="profile">
                                            <div class="mc-view-head">
                                                <div>
                                                    <h2>Hồ sơ cá nhân</h2>

                                                </div>
                                            </div>

                                            <div class="mc-profile-embed" data-profile-form-holder></div>
                                        </div>

                                        <!-- Security -->
                                        <div class="mc-account-view" data-account-view="security">
                                            <div class="mc-view-head">
                                                <div>
                                                    <h2>Tài khoản & Bảo mật</h2>
                                                    <p>Phần này dùng để quản lý đăng nhập, mật khẩu và các thông tin xác thực tài khoản.</p>
                                                </div>
                                            </div>

                                            <div class="mc-security-layout">
                                                <div class="mc-security-overview">
                                                    <div class="mc-security-icon">🔐</div>
                                                    <div>
                                                        <h3>Bảo vệ tài khoản của bạn</h3>
                                                        <p>Email, số điện thoại và mật khẩu là thông tin dùng để đăng nhập, nhận OTP và khôi phục tài khoản khi cần.</p>
                                                    </div>
                                                </div>

                                                <div class="mc-security-card-grid">
                                                    <div class="mc-security-card">
                                                        <span class="mc-security-card-icon">📧</span>
                                                        <div>
                                                            <strong>Email đăng nhập</strong>
                                                            <small>
                                                                <c:choose>
                                                                    <c:when test="${not empty userEmail}"><c:out value="${userEmail}" /></c:when>
                                                                    <c:otherwise>Chưa cập nhật email</c:otherwise>
                                                                </c:choose>
                                                            </small>
                                                            <p>Dùng để nhận OTP và thông báo quan trọng từ hệ thống.</p>
                                                        </div>
                                                        <button type="button" class="mc-security-action" data-account-target="profile">Cập nhật</button>
                                                    </div>

                                                    <div class="mc-security-card">
                                                        <span class="mc-security-card-icon">📱</span>
                                                        <div>
                                                            <strong>Số điện thoại xác thực</strong>
                                                            <small>
                                                                <c:choose>
                                                                    <c:when test="${not empty userPhone}"><c:out value="${userPhone}" /></c:when>
                                                                    <c:otherwise>Chưa cập nhật số điện thoại</c:otherwise>
                                                                </c:choose>
                                                            </small>
                                                            <p>Dùng để liên hệ giao hàng và hỗ trợ xác minh tài khoản.</p>
                                                        </div>
                                                        <button type="button" class="mc-security-action" data-account-target="profile">Cập nhật</button>
                                                    </div>
                                                </div>

                                                <div class="mc-security-list">
                                                    <a href="${pageContext.request.contextPath}/account/change-password" class="mc-security-row is-danger">
                                                        <span class="mc-security-row-icon">🔑</span>
                                                        <div>
                                                            <strong>Đổi mật khẩu</strong>
                                                            <small>Nên thay đổi định kỳ để bảo vệ tài khoản, đặc biệt sau khi đăng nhập trên thiết bị lạ.</small>
                                                        </div>
                                                        <em>Đổi ngay ›</em>
                                                    </a>

                                                    <button type="button" class="mc-security-row" data-account-target="profile">
                                                        <span class="mc-security-row-icon">✅</span>
                                                        <div>
                                                            <strong>Xác thực khi thay đổi thông tin</strong>
                                                            <small>Hồ sơ dùng cho đặt hàng và giao hàng. Riêng email khi thay đổi sẽ cần xác thực OTP trước khi lưu.</small>
                                                        </div>
                                                        <em>Xem hồ sơ ›</em>
                                                    </button>

                                                    <div class="mc-security-row is-muted">
                                                        <span class="mc-security-row-icon">🛡️</span>
                                                        <div>
                                                            <strong>Lưu ý bảo mật</strong>
                                                            <small>Không chia sẻ mật khẩu, mã OTP hoặc tài khoản cho người khác.</small>
                                                        </div>
                                                        <em>An toàn</em>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>

                                    </div>
                                </section>

                            </div>

                        </c:otherwise>
                    </c:choose>

                </c:if>


                <!-- ========================================================= -->
                <!-- PROFILE SETTINGS: USER AND ADMIN BOTH CAN UPDATE -->
                <!-- ========================================================= -->
                <div id="profile-settings" class="account-card account-section-space account-profile-settings-card mc-profile-source">
                    <div class="account-card-body">
                        <div class="account-card-head account-profile-settings-head">
                            <div>
                                <span class="account-section-kicker">Profile Settings</span>
                                <h2 class="account-card-title">Hồ sơ cá nhân</h2>
                                <p class="account-muted">Quản lý họ tên, số điện thoại, email và địa chỉ nhận hàng.</p>
                            </div>

                            <c:choose>
                                <c:when test="${sessionScope.user.admin}">
                                    <span class="account-chip">Admin Profile</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="account-chip user-chip">User Profile</span>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <c:if test="${param.update == 'success'}">
                            <p class="account-alert-success">Cập nhật thành công.</p>
                        </c:if>

                        <c:if test="${param.update == 'invalid_email'}">
                            <p class="account-alert-error">Email không hợp lệ.</p>
                        </c:if>

                        <c:if test="${param.update == 'invalid_phone'}">
                            <p class="account-alert-error">Số điện thoại không hợp lệ (9–11 chữ số).</p>
                        </c:if>

                        <c:if test="${param.update == 'email_used'}">
                            <p class="account-alert-error">Email đã được sử dụng.</p>
                        </c:if>

                        <form id="updateProfileForm" method="post" class="account-profile-form">
                            <c:if test="${not empty csrfToken}">
                                <input type="hidden" name="csrfToken" value="${fn:escapeXml(csrfToken)}" />
                            </c:if>
                            <c:if test="${empty csrfToken and not empty sessionScope.csrfToken}">
                                <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.csrfToken)}" />
                            </c:if>

                            <div class="mc-profile-form-sections">
                                <section class="mc-profile-form-section">
                                    <div class="mc-form-section-head">
                                        <span>01</span>
                                        <div>
                                            <h3>Thông tin cá nhân</h3>

                                        </div>
                                    </div>

                                    <div class="account-form-grid account-form-grid-profile mc-profile-personal-grid">
                                        <div class="form-group">
                                            <label for="nameInput">Họ tên <span>*</span></label>
                                            <input class="account-input" id="nameInput" name="fullName" value="${sessionScope.user.fullName}" required />
                                            <small id="name-error" class="account-field-error"></small>
                                        </div>

                                        <div class="form-group">
                                            <label for="birthDateInput">Ngày sinh</label>
                                            <input class="account-input" id="birthDateInput" type="date" name="birthDate" value="${sessionScope.user.birthDate}" />
                                        </div>

                                        <div class="form-group">
                                            <label for="genderInput">Giới tính</label>
                                            <select class="account-input" id="genderInput" name="gender">
                                                <option value="Male" ${sessionScope.user.gender == 'Male' ? 'selected' : ''}>Nam</option>
                                                <option value="Female" ${sessionScope.user.gender == 'Female' ? 'selected' : ''}>Nữ</option>
                                            </select>
                                        </div>
                                    </div>
                                </section>

                                <section class="mc-profile-form-section">
                                    <div class="mc-form-section-head">
                                        <span>02</span>
                                        <div>
                                            <h3>Thông tin liên hệ</h3>

                                        </div>
                                    </div>

                                    <div class="account-form-grid account-form-grid-profile">
                                        <div class="form-group">
                                            <label for="emailInput">Email <span>*</span></label>
                                            <input class="account-input" id="emailInput" name="email" value="${sessionScope.user.email}" required />
                                            <small id="email-error" class="account-field-error"></small>
                                        </div>

                                        <div class="form-group">
                                            <label for="phoneInput">Số điện thoại <span>*</span></label>
                                            <input class="account-input" id="phoneInput" name="phone" value="${sessionScope.user.phone}" required />
                                            <small id="phone-error" class="account-field-error"></small>
                                        </div>
                                    </div>
                                </section>

                                <section class="mc-profile-form-section">
                                    <div class="mc-form-section-head">
                                        <span>03</span>
                                        <div>
                                            <h3>Địa chỉ giao hàng</h3>

                                        </div>
                                    </div>

                                    <div class="form-group account-address-group">
                                        <label for="addressInput">Địa chỉ giao hàng</label>
                                        <div class="account-address-row">
                                            <input type="text" id="addressInput" name="address" value="${sessionScope.user.address}" class="account-input" placeholder="Nhập địa chỉ hoặc bấm lấy vị trí" />
                                            <button type="button" id="getLocationBtn" class="account-btn account-btn-secondary-soft account-location-btn" onclick="getLocation()">📍 Lấy vị trí</button>
                                        </div>
                                        <small id="locationStatus" class="account-location-status"></small>
                                    </div>
                                </section>
                            </div>

                            <div class="account-profile-form-actions">

                                <button type="submit" class="btn-save account-save-profile-btn" id="saveProfileBtn">Lưu thay đổi</button>
                            </div>
                        </form>
                    </div>
                </div>


                <script>
                    (function () {
                        function setAccountView(viewName) {
                            const target = viewName || "overview";
                            const views = document.querySelectorAll("[data-account-view]");
                            const triggers = document.querySelectorAll("[data-account-target]");

                            views.forEach(function (view) {
                                view.classList.remove("is-active");
                            });

                            views.forEach(function (view) {
                                if (view.dataset.accountView === target) {
                                    view.classList.add("is-active");
                                }
                            });

                            triggers.forEach(function (trigger) {
                                trigger.classList.toggle("is-active", trigger.dataset.accountTarget === target);
                            });

                            const content = document.querySelector(".mc-account-content");
                            if (content) {
                                content.scrollTop = 0;
                            }

                            if (window.history && window.history.replaceState) {
                                window.history.replaceState(null, "", "#account-" + target);
                            }
                        }

                        function moveProfileFormIntoPanel() {
                            const source = document.querySelector(".mc-profile-source");
                            const holder = document.querySelector("[data-profile-form-holder]");

                            if (!source || !holder || holder.contains(source)) return;

                            holder.appendChild(source);
                            source.classList.remove("account-section-space");
                            source.style.display = "block";
                        }

                        document.addEventListener("click", function (event) {
                            const trigger = event.target.closest("[data-account-target]");
                            if (!trigger) return;

                            const target = trigger.dataset.accountTarget;
                            if (!target) return;

                            event.preventDefault();
                            moveProfileFormIntoPanel();
                            setAccountView(target);

                        });

                        document.addEventListener("DOMContentLoaded", function () {
                            moveProfileFormIntoPanel();

                            const hash = (window.location.hash || "").replace("#account-", "");
                            const allowed = ["overview", "orders", "vouchers", "rank", "activity", "profile", "security", "report"];
                            const params = new URLSearchParams(window.location.search || "");
                            const rawTab = (params.get("tab") || "").trim();
                            const tabAlias = {
                                "search-history": "activity",
                                "history": "activity",
                                "activity": "activity",
                                "orders": "orders",
                                "vouchers": "vouchers",
                                "rank": "rank",
                                "profile": "profile",
                                "security": "security",
                                "overview": "overview"
                            };
                            const tabTarget = tabAlias[rawTab] || "";

                            if (allowed.indexOf(hash) >= 0) {
                                setAccountView(hash);
                            } else if (tabTarget && allowed.indexOf(tabTarget) >= 0) {
                                setAccountView(tabTarget);
                            } else if (params.has("update") || window.location.hash === "#profile-settings") {
                                setAccountView("profile");
                            } else {
                                setAccountView("overview");
                            }
                        });
                    })();
                </script>

                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

                <script>
                    function setLocationStatus(message, type) {
                        const statusEl = document.getElementById("locationStatus");
                        if (!statusEl) return;

                        statusEl.textContent = message || "";
                        statusEl.classList.remove("is-loading", "is-success", "is-error");

                        if (type) {
                            statusEl.classList.add("is-" + type);
                        }
                    }

                    function setLocationLoading(isLoading, message) {
                        const btn = document.getElementById("getLocationBtn");
                        const addressInput = document.getElementById("addressInput");

                        if (btn) {
                            btn.disabled = isLoading;
                            btn.classList.toggle("is-loading", isLoading);
                            btn.innerHTML = isLoading ? "⏳ Đang lấy..." : "📍 Lấy vị trí";
                        }

                        if (message) {
                            setLocationStatus(message, isLoading ? "loading" : "");
                        }

                        if (addressInput && isLoading) {
                            addressInput.classList.add("is-loading-location");
                        } else if (addressInput) {
                            addressInput.classList.remove("is-loading-location");
                        }
                    }

                    function getCurrentPositionPromise(options) {
                        return new Promise(function (resolve, reject) {
                            navigator.geolocation.getCurrentPosition(resolve, reject, options);
                        });
                    }

                    async function fetchJsonWithTimeout(url, timeoutMs) {
                        const controller = new AbortController();
                        const timer = setTimeout(function () {
                            controller.abort();
                        }, timeoutMs || 6500);

                        try {
                            const response = await fetch(url, {
                                signal: controller.signal,
                                cache: "no-store",
                                headers: {
                                    "Accept": "application/json"
                                }
                            });

                            if (!response.ok) {
                                throw new Error("HTTP " + response.status);
                            }

                            return await response.json();
                        } finally {
                            clearTimeout(timer);
                        }
                    }

                    function cleanAddressParts(parts) {
                        const seen = new Set();
                        return parts
                            .map(function (part) { return (part || "").toString().trim(); })
                            .filter(function (part) {
                                if (!part || seen.has(part.toLowerCase())) return false;
                                seen.add(part.toLowerCase());
                                return true;
                            })
                            .join(", ");
                    }

                    function buildBigDataCloudAddress(data) {
                        if (!data) return "";

                        return cleanAddressParts([
                            data.locality,
                            data.city,
                            data.principalSubdivision,
                            data.postcode,
                            data.countryName
                        ]);
                    }

                    function buildNominatimAddress(data) {
                        if (!data) return "";
                        return data.display_name || cleanAddressParts([
                            data.name,
                            data.address && data.address.road,
                            data.address && data.address.suburb,
                            data.address && data.address.city,
                            data.address && data.address.state,
                            data.address && data.address.postcode,
                            data.address && data.address.country
                        ]);
                    }

                    async function reverseGeocodeAddress(lat, lon) {
                        const encodedLat = encodeURIComponent(lat);
                        const encodedLon = encodeURIComponent(lon);

                        // Ưu tiên BigDataCloud vì thường phản hồi nhanh và ổn định hơn trên trình duyệt.
                        try {
                            const bigDataUrl = "https://api.bigdatacloud.net/data/reverse-geocode-client"
                                + "?latitude=" + encodedLat
                                + "&longitude=" + encodedLon
                                + "&localityLanguage=vi";

                            const bigData = await fetchJsonWithTimeout(bigDataUrl, 6500);
                            const address = buildBigDataCloudAddress(bigData);

                            if (address) {
                                return address;
                            }
                        } catch (error) {
                            console.warn("BigDataCloud reverse geocode failed:", error);
                        }

                        // Fallback sang OpenStreetMap Nominatim.
                        try {
                            const nominatimUrl = "https://nominatim.openstreetmap.org/reverse"
                                + "?format=jsonv2"
                                + "&lat=" + encodedLat
                                + "&lon=" + encodedLon
                                + "&accept-language=vi"
                                + "&addressdetails=1"
                                + "&zoom=18";

                            const nominatim = await fetchJsonWithTimeout(nominatimUrl, 7000);
                            const address = buildNominatimAddress(nominatim);

                            if (address) {
                                return address;
                            }
                        } catch (error) {
                            console.warn("Nominatim reverse geocode failed:", error);
                        }

                        return "";
                    }

                    function getLocationErrorMessage(error) {
                        if (!error) {
                            return "Không thể lấy vị trí. Vui lòng nhập địa chỉ thủ công.";
                        }

                        if (error.code === 1) {
                            return "Bạn chưa cấp quyền vị trí. Hãy bấm biểu tượng vị trí trên thanh địa chỉ trình duyệt và chọn Cho phép.";
                        }

                        if (error.code === 2) {
                            return "Trình duyệt không xác định được vị trí hiện tại. Hãy thử bật Wi-Fi/GPS hoặc nhập địa chỉ thủ công.";
                        }

                        if (error.code === 3) {
                            return "Lấy vị trí quá lâu. Hãy thử lại hoặc nhập địa chỉ thủ công.";
                        }

                        return "Không thể lấy vị trí. Vui lòng thử lại hoặc nhập địa chỉ thủ công.";
                    }

                    async function getLocation() {
                        const addressInput = document.getElementById("addressInput");

                        if (!navigator.geolocation) {
                            setLocationStatus("Trình duyệt không hỗ trợ lấy vị trí.", "error");
                            alert("Trình duyệt không hỗ trợ lấy vị trí.");
                            return;
                        }

                        const isLocalhost = ["localhost", "127.0.0.1", "::1"].includes(window.location.hostname);
                        const isHttps = window.location.protocol === "https:";

                        if (!isHttps && !isLocalhost) {
                            setLocationStatus("Trình duyệt chỉ cho phép lấy vị trí trên HTTPS hoặc localhost.", "error");
                            alert("Trình duyệt chỉ cho phép lấy vị trí trên HTTPS hoặc localhost.");
                            return;
                        }

                        const oldAddressValue = addressInput ? addressInput.value : "";

                        try {
                            setLocationLoading(true, "Đang xin quyền vị trí từ trình duyệt...");

                            if (addressInput) {
                                addressInput.value = "Đang lấy vị trí hiện tại...";
                            }

                            let position;

                            try {
                                position = await getCurrentPositionPromise({
                                    enableHighAccuracy: true,
                                    timeout: 18000,
                                    maximumAge: 0
                                });
                            } catch (firstError) {
                                setLocationStatus("Đang thử lại bằng chế độ lấy vị trí nhanh hơn...", "loading");

                                position = await getCurrentPositionPromise({
                                    enableHighAccuracy: false,
                                    timeout: 12000,
                                    maximumAge: 300000
                                });
                            }

                            const lat = Number(position.coords.latitude);
                            const lon = Number(position.coords.longitude);
                            const accuracy = Math.round(position.coords.accuracy || 0);

                            if (!Number.isFinite(lat) || !Number.isFinite(lon)) {
                                throw new Error("Invalid coordinates");
                            }

                            if (addressInput) {
                                addressInput.value = "Đã lấy tọa độ, đang tìm địa chỉ...";
                            }

                            setLocationStatus("Đã lấy tọa độ, đang chuyển thành địa chỉ...", "loading");

                            const address = await reverseGeocodeAddress(lat, lon);
                            const coordinateText = lat.toFixed(6) + ", " + lon.toFixed(6);

                            if (addressInput) {
                                addressInput.value = address ? address : coordinateText;
                            }

                            if (address) {
                                setLocationStatus("Đã lấy vị trí thành công" + (accuracy ? " · độ chính xác khoảng " + accuracy + "m" : "") + ".", "success");
                            } else {
                                setLocationStatus("Đã lấy được tọa độ, nhưng chưa lấy được địa chỉ chi tiết. Bạn có thể bổ sung số nhà/hẻm nếu cần.", "success");
                            }
                        } catch (error) {
                            console.error("Get location failed:", error);

                            if (addressInput) {
                                addressInput.value = oldAddressValue || "";
                            }

                            const message = getLocationErrorMessage(error);
                            setLocationStatus(message, "error");
                            alert(message);
                        } finally {
                            setLocationLoading(false);
                        }
                    }

                    function copyCouponCode(code) {
                        if (!code) {
                            return;
                        }

                        if (navigator.clipboard) {
                            navigator.clipboard.writeText(code)
                                .then(function () {
                                    alert("Đã copy mã: " + code);
                                })
                                .catch(function () {
                                    alert("Mã giảm giá: " + code);
                                });
                        } else {
                            alert("Mã giảm giá: " + code);
                        }
                    }

                    document.querySelectorAll(".account-rank-progress__bar[data-progress]").forEach(function (bar) {
                        const rawProgress = Number(bar.dataset.progress || 0);
                        const safeProgress = Math.max(0, Math.min(rawProgress, 100));
                        bar.style.width = safeProgress + "%";
                    });
                </script>


                <!-- USER SPENDING CHART -->
                <c:if test="${not sessionScope.user.admin and not empty chart_labels and not empty chart_values}">
                    <script>
                        const spendingLabels =
                            <c:out value="${chart_labels}" escapeXml="false"/>;

                        const spendingValues =
                            <c:out value="${chart_values}" escapeXml="false"/>;

                        const spendingChartEl = document.getElementById('spendingChart');

                        if (spendingChartEl) {
                            new Chart(spendingChartEl, {
                                type: 'line',
                                data: {
                                    labels: spendingLabels,
                                    datasets: [{
                                        label: 'Chi tiêu',
                                        data: spendingValues,
                                        borderColor: '#4f8cff',
                                        backgroundColor: 'rgba(79, 140, 255, 0.16)',
                                        fill: true,
                                        tension: 0.35,
                                        borderWidth: 2
                                    }]
                                },
                                options: {
                                    responsive: true,
                                    plugins: {
                                        legend: {
                                            display: false
                                        },
                                        tooltip: {
                                            callbacks: {
                                                label: function(context) {
                                                    return new Intl.NumberFormat('vi-VN').format(context.raw || 0) + ' ₫';
                                                }
                                            }
                                        }
                                    },
                                    scales: {
                                        y: {
                                            beginAtZero: true,
                                            ticks: {
                                                callback: function(value) {
                                                    return new Intl.NumberFormat('vi-VN').format(value) + ' ₫';
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    </script>
                </c:if>

                <!-- ADMIN REVENUE CHART -->
                <c:if test="${sessionScope.user.admin and not empty admin_chart_labels and not empty admin_chart_values}">
                    <script>
                        const adminRevenueLabels =
                            <c:out value="${admin_chart_labels}" escapeXml="false"/>;

                        const adminRevenueValues =
                            <c:out value="${admin_chart_values}" escapeXml="false"/>;

                        const storeRevenueChartEl = document.getElementById('storeRevenueChart');

                        if (storeRevenueChartEl) {
                            new Chart(storeRevenueChartEl, {
                                type: 'bar',
                                data: {
                                    labels: adminRevenueLabels,
                                    datasets: [{
                                        label: 'Doanh thu',
                                        data: adminRevenueValues,
                                        backgroundColor: 'rgba(255, 95, 162, 0.6)',
                                        borderColor: 'rgba(255, 95, 162, 1)',
                                        borderWidth: 1,
                                        borderRadius: 8
                                    }]
                                },
                                options: {
                                    responsive: true,
                                    plugins: {
                                        legend: {
                                            display: false
                                        },
                                        tooltip: {
                                            callbacks: {
                                                label: function(context) {
                                                    return new Intl.NumberFormat('vi-VN').format(context.raw || 0) + ' ₫';
                                                }
                                            }
                                        }
                                    },
                                    scales: {
                                        y: {
                                            beginAtZero: true,
                                            ticks: {
                                                callback: function(value) {
                                                    return new Intl.NumberFormat('vi-VN').format(value) + ' ₫';
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    </script>
                </c:if>

            </c:when>

            <c:otherwise>
                <div class="account-card">
                    <div class="account-card-body">
                        <p class="account-empty">Bạn cần đăng nhập để xem trang này.</p>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</section>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

<div id="otpModal" class="modal-overlay">
    <div class="modal-content">
        <h3>Xác thực OTP</h3>
        <p>Hệ thống đã gửi một mã OTP gồm 6 chữ số đến Email của bạn.</p>
        <input type="text" id="otp_input" placeholder="******" maxlength="6">
        <button type="button" onclick="xacThucOtp()" class="btn-save">Xác nhận</button>
        <button type="button" onclick="dongPopupOtp()" style="background:none; border:none; color:#777; margin-top:10px; cursor:pointer;">Hủy bỏ</button>
    </div>
</div>

<script>
    // ==========================================
    // 1. CÁC HÀM TIỆN ÍCH KIỂM TRA TRỰC TIẾP
    // ==========================================
    function showError(input, errorElement, message) {
        input.style.borderColor = "#e11d48";
        input.classList.add("is-invalid"); // Đánh dấu lỗi để chặn form
        errorElement.innerText = message;
        errorElement.style.display = "block";
    }

    function showSuccess(input, errorElement) {
        input.style.borderColor = "#10b981";
        input.classList.remove("is-invalid");
        errorElement.style.display = "none";
    }

    function resetState(input, errorElement) {
        input.style.borderColor = "#dbe3ef";
        input.classList.remove("is-invalid");
        errorElement.style.display = "none";
    }

    // ==========================================
    // 2. LẤY CÁC ELEMENT TỪ GIAO DIỆN
    // ==========================================
    const nameInput = document.getElementById("nameInput");
    const nameError = document.getElementById("name-error");
    const emailInput = document.getElementById("emailInput");
    const emailError = document.getElementById("email-error");
    const phoneInput = document.getElementById("phoneInput");
    const phoneError = document.getElementById("phone-error");

    // Bắt sự kiện gõ phím ở ô Họ Tên
    if (nameInput) {
        nameInput.addEventListener("input", () => {
            const value = nameInput.value.trim();
            resetState(nameInput, nameError);
            if (value.length < 4) {
                showError(nameInput, nameError, "Họ và tên tối thiểu 4 ký tự.");
                return;
            }
            showSuccess(nameInput, nameError);
        });
    }

    // Bắt sự kiện gõ phím ở ô Email
    if (emailInput) {
        emailInput.addEventListener("input", () => {
            const value = emailInput.value.trim();
            resetState(emailInput, emailError);
            const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!regex.test(value)) {
                showError(emailInput, emailError, "Định dạng email không hợp lệ (vd: abc@gmail.com).");
                return;
            }
            showSuccess(emailInput, emailError);
        });
    }

    // Bắt sự kiện gõ phím ở ô Số điện thoại
    if (phoneInput) {
        phoneInput.addEventListener("input", () => {
            const value = phoneInput.value.trim();
            resetState(phoneInput, phoneError);
            const regex = /^(03|05|07|08|09)\d{8}$/;
            if (!regex.test(value)) {
                showError(phoneInput, phoneError, "Số điện thoại gồm 10 số, bắt đầu 03/05/07/08/09.");
                return;
            }
            showSuccess(phoneInput, phoneError);
        });
    }

    // ==========================================
    // 3. XỬ LÝ KHI NHẤN NÚT "LƯU THAY ĐỔI"
    // ==========================================
    const updateForm = document.getElementById('updateProfileForm');
    if (updateForm) {
        updateForm.addEventListener('submit', function(e) {
            e.preventDefault(); // CHẶN LỖI 405 Ở ĐÂY (Ngăn form tự chuyển trang)

            // Kích hoạt kiểm tra lại toàn bộ form (phòng khi user chưa gõ gì mà bấm Lưu luôn)
            if (nameInput) nameInput.dispatchEvent(new Event('input'));
            if (emailInput) emailInput.dispatchEvent(new Event('input'));
            if (phoneInput) phoneInput.dispatchEvent(new Event('input'));

            // Kiểm tra xem có ô nào bị dính class "is-invalid" (bị lỗi báo đỏ) không
            const invalidInputs = updateForm.querySelectorAll('.is-invalid');
            if (invalidInputs.length > 0) {
                Swal.fire({
                    icon: 'error',
                    title: 'Thông tin chưa hợp lệ',
                    text: 'Vui lòng kiểm tra và chỉnh sửa lại các trường bị báo đỏ!'
                });
                return; // Dừng, không gửi dữ liệu lên server
            }

            // HIỂN THỊ POPUP ĐANG GỬI OTP
            Swal.fire({
                title: 'Đang xử lý...',
                text: 'Hệ thống đang gửi mã OTP đến email của bạn.',
                allowOutsideClick: false,
                didOpen: () => { Swal.showLoading(); }
            });

            const formData = new URLSearchParams(new FormData(this));

            // Gọi API cập nhật
            fetch('${pageContext.request.contextPath}/account/update-profile', {
                method: 'POST',
                body: formData
            })
                .then(res => res.json())
                .then(data => {
                    Swal.close(); // Tắt popup loading

                    if(data.status === 'success') {
                        // Gửi email xong, bật Modal nhập OTP
                        document.getElementById('otpModal').style.display = 'flex';
                        document.getElementById('otp_input').value = "";
                        document.getElementById('otp_input').focus();
                    } else {
                        Swal.fire('Lỗi', data.message || 'Lỗi gửi email xác thực!', 'error');
                    }
                })
                .catch(err => {
                    Swal.close();
                    Swal.fire('Lỗi mạng', 'Máy chủ không phản hồi, vui lòng thử lại', 'error');
                });
        });
    }

    // ==========================================
    // 4. XỬ LÝ NHẬP OTP (MODAL)
    // ==========================================
    function xacThucOtp() {
        const otpValue = document.getElementById('otp_input').value.trim();
        if (!otpValue || otpValue.length !== 6) {
            Swal.fire({ icon: 'warning', title: 'Cảnh báo', text: 'Vui lòng nhập đúng 6 số OTP!' });
            return;
        }

        const btnXacNhan = document.querySelector('#otpModal .btn-save');
        btnXacNhan.innerText = "Đang xác thực...";
        btnXacNhan.disabled = true;

        fetch('${pageContext.request.contextPath}/verify-update', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'otp_input=' + encodeURIComponent(otpValue)
        })
            .then(res => res.json())
            .then(data => {
                btnXacNhan.innerText = "Xác nhận";
                btnXacNhan.disabled = false;

                if(data.status === 'success') {
                    dongPopupOtp();
                    Swal.fire({
                        icon: 'success',
                        title: 'Thành công!',
                        text: 'Đã lưu thông tin thay đổi thành công.',
                        timer: 2000,
                        showConfirmButton: false
                    }).then(() => {
                        window.location.reload();
                    });
                } else {
                    Swal.fire('Lỗi xác thực', data.message || 'Sai mã OTP, vui lòng thử lại!', 'error');
                }
            })
            .catch(err => {
                btnXacNhan.innerText = "Xác nhận";
                btnXacNhan.disabled = false;
                Swal.fire('Lỗi mạng', 'Đã xảy ra lỗi đường truyền!', 'error');
            });
    }

    function dongPopupOtp() {
        document.getElementById('otpModal').style.display = 'none';
    }
</script>
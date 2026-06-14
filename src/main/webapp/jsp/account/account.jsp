<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/account.css?v=20260614_1" />

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

          <div class="account-hero admin-mode">
            <div class="account-profile">
              <div class="account-avatar">
                <c:out value="${fn:toUpperCase(fn:substring(safeUsername, 0, 1))}" />
              </div>

              <div>
                <h1 class="account-title">Admin Dashboard</h1>
                <p class="account-subtitle">
                  Xin chào, <strong><c:out value="${safeUsername}" /></strong>. Đây là khu vực tổng quan quản trị.
                </p>

                <c:if test="${not empty rankLabel}">
                  <div class="account-chip-row">
                    <span class="account-chip ${rankCss}">
                      🎖 <c:out value="${rankLabel}" />
                      <c:if test="${rankDiscount > 0}">
                        -<c:out value="${rankDiscount}" />%
                      </c:if>
                    </span>
                  </div>
                </c:if>
              </div>
            </div>

            <div class="account-actions">
              <a href="${pageContext.request.contextPath}/admin" class="account-btn primary">🛠 Vào trang Admin</a>
              <a href="${pageContext.request.contextPath}/account/change-password" class="account-btn">🔒 Đổi mật khẩu</a>
            </div>
          </div>

          <!-- ADMIN KPI -->
          <div class="account-grid account-grid-4">
            <div class="account-card">
              <div class="account-card-body account-kpi">
                <div>
                  <p class="account-kpi-label">Tổng doanh thu</p>
                  <div class="account-kpi-value">
                    <c:choose>
                      <c:when test="${not empty admin_total_revenue_vnd}">
                        <fmt:formatNumber value="${admin_total_revenue_vnd}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                      </c:when>
                      <c:otherwise>0 ₫</c:otherwise>
                    </c:choose>
                  </div>
                  <div class="account-kpi-note">Doanh thu từ đơn hợp lệ.</div>
                </div>
                <div class="account-kpi-icon">💵</div>
              </div>
            </div>

            <div class="account-card">
              <div class="account-card-body account-kpi">
                <div>
                  <p class="account-kpi-label">Tổng đơn</p>
                  <div class="account-kpi-value">
                    <c:out value="${empty admin_total_orders ? 0 : admin_total_orders}" />
                  </div>
                  <div class="account-kpi-note">Tổng số đơn đã ghi nhận.</div>
                </div>
                <div class="account-kpi-icon">🧾</div>
              </div>
            </div>

            <div class="account-card">
              <div class="account-card-body account-kpi">
                <div>
                  <p class="account-kpi-label">AOV</p>
                  <div class="account-kpi-value">
                    <c:choose>
                      <c:when test="${not empty admin_aov_vnd}">
                        <fmt:formatNumber value="${admin_aov_vnd}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                      </c:when>
                      <c:otherwise>0 ₫</c:otherwise>
                    </c:choose>
                  </div>
                  <div class="account-kpi-note">Giá trị đơn trung bình.</div>
                </div>
                <div class="account-kpi-icon">📊</div>
              </div>
            </div>

            <div class="account-card">
              <div class="account-card-body account-kpi">
                <div>
                  <p class="account-kpi-label">Tăng trưởng</p>
                  <div class="account-kpi-value">
                    <c:out value="${empty revenue_percent ? 0 : revenue_percent}" />%
                  </div>
                  <div class="account-kpi-note">So với tháng trước.</div>
                </div>
                <div class="account-kpi-icon">
                  <c:choose>
                    <c:when test="${empty revenue_diff_vnd or revenue_diff_vnd >= 0}">📈</c:when>
                    <c:otherwise>📉</c:otherwise>
                  </c:choose>
                </div>
              </div>
            </div>
          </div>

          <!-- ADMIN MONTH SUMMARY -->
          <div class="account-grid account-grid-3 account-section-space">
            <div class="account-card">
              <div class="account-card-body">
                <p class="account-kpi-label">Doanh thu tháng này</p>
                <div class="account-kpi-value">
                  <c:choose>
                    <c:when test="${not empty this_month_revenue}">
                      <fmt:formatNumber value="${this_month_revenue}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                    </c:when>
                    <c:otherwise>0 ₫</c:otherwise>
                  </c:choose>
                </div>
                <div class="account-kpi-note">Không dùng tổng doanh thu toàn hệ thống.</div>
              </div>
            </div>

            <div class="account-card">
              <div class="account-card-body">
                <p class="account-kpi-label">Doanh thu tháng trước</p>
                <div class="account-kpi-value">
                  <c:choose>
                    <c:when test="${not empty prev_month_revenue}">
                      <fmt:formatNumber value="${prev_month_revenue}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                    </c:when>
                    <c:when test="${not empty last_month_revenue}">
                      <fmt:formatNumber value="${last_month_revenue}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                    </c:when>
                    <c:otherwise>0 ₫</c:otherwise>
                  </c:choose>
                </div>
                <div class="account-kpi-note">Dữ liệu dùng để so sánh tăng trưởng.</div>
              </div>
            </div>

            <div class="account-card">
              <div class="account-card-body">
                <p class="account-kpi-label">Chênh lệch doanh thu</p>
                <div class="account-kpi-value">
                  <c:choose>
                    <c:when test="${not empty revenue_diff_vnd}">
                      <fmt:formatNumber value="${revenue_diff_vnd}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                    </c:when>
                    <c:otherwise>0 ₫</c:otherwise>
                  </c:choose>
                </div>
                <div class="account-kpi-note">
                  Tỷ lệ: <strong><c:out value="${empty revenue_percent ? 0 : revenue_percent}" />%</strong>
                </div>
              </div>
            </div>
          </div>

          <!-- ADMIN PRODUCT QUICK STATS -->
          <div class="account-grid account-grid-4 account-section-space">

            <div class="account-card">
              <div class="account-card-body account-kpi">
                <div>
                  <p class="account-kpi-label">Không bán tháng này</p>
                  <div class="account-kpi-value">
                    <c:out value="${empty unsoldThisMonthCount ? 0 : unsoldThisMonthCount}" />
                  </div>
                  <div class="account-kpi-note">Sản phẩm chưa phát sinh bán trong tháng.</div>
                </div>
                <div class="account-kpi-icon">📉</div>
              </div>
            </div>

            <div class="account-card">
              <div class="account-card-body account-kpi">
                <div>
                  <p class="account-kpi-label">Không bán 30 ngày</p>
                  <div class="account-kpi-value">
                    <c:out value="${empty unsoldLast30DaysCount ? 0 : unsoldLast30DaysCount}" />
                  </div>
                  <div class="account-kpi-note">Sản phẩm không bán trong 30 ngày gần nhất.</div>
                </div>
                <div class="account-kpi-icon">🕒</div>
              </div>
            </div>

            <div class="account-card">
              <div class="account-card-body account-kpi">
                <div>
                  <p class="account-kpi-label">Hết hàng</p>
                  <div class="account-kpi-value">
                    <c:out value="${empty outOfStockCount ? 0 : outOfStockCount}" />
                  </div>
                  <div class="account-kpi-note">Sản phẩm active nhưng tồn kho bằng 0.</div>
                </div>
                <div class="account-kpi-icon">🚫</div>
              </div>
            </div>

            <div class="account-card">
              <div class="account-card-body account-kpi">
                <div>
                  <p class="account-kpi-label">Sắp hết hàng</p>
                  <div class="account-kpi-value">
                    <c:out value="${empty lowStockCount ? 0 : lowStockCount}" />
                  </div>
                  <div class="account-kpi-note">Tồn kho còn từ 1 đến 10 sản phẩm.</div>
                </div>
                <div class="account-kpi-icon">⚠️</div>
              </div>
            </div>

          </div>

          <!-- ADMIN CHART + TOP PRODUCT -->
          <div class="account-grid account-grid-2 account-section-space">
            <div class="account-card">
              <div class="account-card-body">
                <div class="account-card-head account-card-head-center">
                  <div>
                    <h2 class="account-card-title">🏬 Doanh thu toàn cửa hàng</h2>
                    <p class="account-muted">Biểu đồ doanh thu theo thời gian.</p>
                  </div>
                  <span class="account-chip">Revenue</span>
                </div>

                <c:choose>
                  <c:when test="${not empty admin_chart_labels and not empty admin_chart_values}">
                    <canvas id="storeRevenueChart" height="130"></canvas>
                  </c:when>
                  <c:otherwise>
                    <p class="account-empty">Chưa có dữ liệu doanh thu.</p>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>

            <div class="account-card">
              <div class="account-card-body">
                <div class="account-card-head account-card-head-center">
                  <div>
                    <h2 class="account-card-title">🔥 Top sản phẩm bán chạy</h2>
                    <p class="account-muted">Sản phẩm có số lượng bán tốt nhất.</p>
                  </div>
                  <span class="account-chip">Top</span>
                </div>

                <div class="account-table-wrap">
                  <c:choose>
                    <c:when test="${not empty top_products}">
                      <table class="account-table">
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
                      <p class="account-empty">Chưa có dữ liệu bán hàng.</p>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>
            </div>
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
                      <div class="account-coupon-grid">
                        <c:forEach var="savedCp" items="${savedCoupons}">
                          <div class="account-coupon-card" style="border: 1px dashed var(--pink-main); background: var(--pink-soft);">
                            <div class="account-coupon-top">
                              <div class="account-coupon-code">
                                <span>🎁</span>
                                <span style="color: var(--pink-dark); font-weight: bold;"><c:out value="${savedCp.code}" /></span>
                              </div>
                              <button type="button" class="account-coupon-copy" style="background: var(--pink-main); color: #fff;" data-coupon-code="<c:out value='${savedCp.code}'/>" onclick="copyCouponCode(this.dataset.couponCode)">
                                Copy
                              </button>
                            </div>

                            <div class="account-coupon-discount" style="color: var(--text-main);">
                              <c:choose>
                                <c:when test="${savedCp.type eq 'FREESHIP'}">🚚 Freeship Vận Chuyển</c:when>
                                <c:otherwise>Giảm liền ${savedCp.discountPercent}%</c:otherwise>
                              </c:choose>
                            </div>

                            <div class="account-coupon-meta">
                              <div><strong>Mô tả:</strong> <c:out value="${not empty savedCp.description ? savedCp.description : 'Áp dụng giảm trừ trực tiếp vào hóa đơn khi thanh toán.'}" /></div>
                              <div>
                                <strong>Điều kiện:</strong>
                                <c:choose>
                                  <c:when test="${savedCp.minOrderAmount > 0}">
                                    Đơn hàng từ <fmt:formatNumber value="${savedCp.minOrderAmount}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                  </c:when>
                                  <c:otherwise>Không giới hạn đơn hàng tối thiểu</c:otherwise>
                                </c:choose>
                              </div>
                              <div>
                                <strong>Hạn dùng:</strong>
                                <c:choose>
                                  <c:when test="${not empty savedCp.endDate}">
                                    <span style="color: #d97706; font-weight: 500;"><c:out value="${savedCp.endDate}" /></span>
                                  </c:when>
                                  <c:otherwise>Không giới hạn thời gian</c:otherwise>
                                </c:choose>
                              </div>
                            </div>
                            <div class="account-coupon-rank" style="border-top: 1px solid rgba(255, 95, 162, 0.2); background: rgba(255, 95, 162, 0.05);">
                              ✔ Sẵn sàng sử dụng tại Checkout
                            </div>
                          </div>
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

                  <div class="mc-compact-actions">
                    <button type="button" class="mc-compact-btn is-primary" data-account-target="orders">Đơn mua</button>
                    <button type="button" class="mc-compact-btn" data-account-target="profile">Sửa hồ sơ</button>
                    <a href="${pageContext.request.contextPath}/account/change-password" class="mc-compact-btn">Đổi mật khẩu</a>
                  </div>
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
                        <a href="${pageContext.request.contextPath}/orders"><span>🕘</span><strong>Chờ xác nhận</strong><small>Đơn mới tạo</small></a>
                        <a href="${pageContext.request.contextPath}/orders"><span>📦</span><strong>Chờ lấy hàng</strong><small>Shop chuẩn bị</small></a>
                        <a href="${pageContext.request.contextPath}/orders"><span>🚚</span><strong>Đang giao</strong><small>Xem tracking</small></a>
                        <a href="${pageContext.request.contextPath}/orders"><span>⭐</span><strong>Đánh giá / Hoàn hàng</strong><small>Sau khi nhận</small></a>
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
                          <div class="mc-compact-voucher-grid">
                            <c:forEach var="savedCp" items="${savedCoupons}" end="5">
                              <div class="mc-compact-voucher-card">
                                <div class="mc-voucher-top">
                                  <span class="mc-voucher-code">🎁 <c:out value="${savedCp.code}" /></span>
                                  <button type="button"
                                          class="mc-copy-btn"
                                          data-coupon-code="<c:out value='${savedCp.code}'/>"
                                          onclick="copyCouponCode(this.dataset.couponCode)">
                                    Copy
                                  </button>
                                </div>

                                <h3>
                                  <c:choose>
                                    <c:when test="${savedCp.type eq 'FREESHIP'}">Freeship vận chuyển</c:when>
                                    <c:otherwise>Giảm liền ${savedCp.discountPercent}%</c:otherwise>
                                  </c:choose>
                                </h3>

                                <p><strong>Mô tả:</strong> <c:out value="${not empty savedCp.description ? savedCp.description : 'Áp dụng giảm trực tiếp vào hóa đơn khi thanh toán.'}" /></p>
                                <p>
                                  <strong>Điều kiện:</strong>
                                  <c:choose>
                                    <c:when test="${savedCp.minOrderAmount > 0}">
                                      Đơn hàng từ <fmt:formatNumber value="${savedCp.minOrderAmount}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                    </c:when>
                                    <c:otherwise>Không giới hạn đơn hàng tối thiểu</c:otherwise>
                                  </c:choose>
                                </p>

                                <span class="mc-voucher-ready">Sẵn sàng dùng tại Checkout</span>
                              </div>
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
                          <p>Hạng được tính từ các đơn đã thanh toán thành công.</p>
                        </div>
                        <span class="mc-compact-rank ${rankCss}">
                          <c:out value="${empty rankLabel ? 'Thành viên' : rankLabel}" />
                          <c:if test="${rankDiscount > 0}"> -<c:out value="${rankDiscount}" />%</c:if>
                        </span>
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

                      <c:choose>
                        <c:when test="${maxRank}">
                          <p class="mc-rank-note">Bạn đã đạt hạng cao nhất. Cảm ơn bạn đã đồng hành cùng MyCosmetic.</p>
                          <div class="mc-rank-progress"><div class="mc-rank-progress-bar is-full"></div></div>
                        </c:when>
                        <c:otherwise>
                          <p class="mc-rank-note">
                            Còn <strong><fmt:formatNumber value="${empty amountToNextRank ? 0 : amountToNextRank}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</strong>
                            để lên hạng <strong><c:out value="${empty nextRankLabel ? 'tiếp theo' : nextRankLabel}" /></strong>.
                          </p>
                          <div class="mc-rank-progress"><div class="mc-rank-progress-bar" data-progress="${empty rankProgressPercent ? 0 : rankProgressPercent}"></div></div>
                          <p class="mc-rank-note">Tiến độ: <strong><c:out value="${empty rankProgressPercent ? 0 : rankProgressPercent}" />%</strong></p>
                        </c:otherwise>
                      </c:choose>
                    </div>

                    <!-- Activity -->
                    <div class="mc-account-view" data-account-view="activity">
                      <div class="mc-view-head">
                        <div>
                          <h2>Hoạt động gần đây</h2>
                          <p>Lịch sử tìm kiếm và chi tiêu của bạn.</p>
                        </div>
                      </div>

                      <div class="mc-activity-split">
                        <div class="mc-mini-section">
                          <div class="mc-mini-section-head">
                            <h3>Lịch sử tìm kiếm</h3>
                            <span><c:out value="${empty searchHistoryCount ? 0 : searchHistoryCount}" /> lượt tìm</span>
                          </div>

                          <c:choose>
                            <c:when test="${not empty searchHistories}">
                              <div class="mc-search-list">
                                <c:forEach var="history" items="${searchHistories}">
                                  <c:choose>
                                    <c:when test="${not empty history.searchUrl}"><c:set var="historyHref" value="${pageContext.request.contextPath}${history.searchUrl}" /></c:when>
                                    <c:otherwise><c:url var="historyHref" value="/search"><c:param name="q" value="${history.keyword}" /></c:url></c:otherwise>
                                  </c:choose>

                                  <div class="mc-search-item">
                                    <a href="${fn:escapeXml(historyHref)}">
                                      <span>🔍</span>
                                      <div>
                                        <strong><c:out value="${history.keyword}" /></strong>
                                        <small><c:out value="${empty history.resultCount ? 0 : history.resultCount}" /> kết quả • Đã tìm <c:out value="${empty history.searchCount ? 1 : history.searchCount}" /> lần • <c:out value="${history.displayLastSearchedAt}" /></small>
                                      </div>
                                    </a>

                                    <form method="post" action="${pageContext.request.contextPath}/account/search-history/delete" class="account-inline-form" onsubmit="return confirm('Xóa từ khóa tìm kiếm này?');">
                                      <c:if test="${not empty csrfToken}"><input type="hidden" name="csrfToken" value="${fn:escapeXml(csrfToken)}" /></c:if>
                                      <c:if test="${empty csrfToken and not empty sessionScope.csrfToken}"><input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.csrfToken)}" /></c:if>
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

                        <div class="mc-mini-section">
                          <div class="mc-mini-section-head">
                            <h3>Chi tiêu theo thời gian</h3>
                            <span>Spending</span>
                          </div>

                          <c:choose>
                            <c:when test="${not empty chart_labels and not empty chart_values}">
                              <div class="mc-chart-wrap"><canvas id="spendingChart" height="150"></canvas></div>
                            </c:when>
                            <c:otherwise>
                              <div class="mc-empty-state"><span>📊</span><p>Chưa có dữ liệu chi tiêu.</p></div>
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
                          <p>Cập nhật thông tin dùng cho đơn hàng và giao hàng.</p>
                        </div>
                      </div>

                      <div class="mc-profile-embed" data-profile-form-holder></div>
                    </div>

                    <!-- Security -->
                    <div class="mc-account-view" data-account-view="security">
                      <div class="mc-view-head">
                        <div>
                          <h2>Tài khoản & Bảo mật</h2>
                          <p>Quản lý thông tin đăng nhập và bảo vệ tài khoản.</p>
                        </div>
                      </div>

                      <div class="mc-setting-list">
                        <button type="button" class="mc-setting-row" data-account-target="profile">
                          <div><strong>Hồ sơ cá nhân</strong><small>Họ tên, số điện thoại, email, địa chỉ</small></div>
                          <span>›</span>
                        </button>

                        <button type="button" class="mc-setting-row" data-account-target="profile">
                          <div><strong>Số điện thoại</strong><small><c:choose><c:when test="${not empty userPhone}"><c:out value="${userPhone}" /></c:when><c:otherwise>Chưa cập nhật</c:otherwise></c:choose></small></div>
                          <span>›</span>
                        </button>

                        <button type="button" class="mc-setting-row" data-account-target="profile">
                          <div><strong>Email</strong><small><c:choose><c:when test="${not empty userEmail}"><c:out value="${userEmail}" /></c:when><c:otherwise>Chưa cập nhật</c:otherwise></c:choose></small></div>
                          <span>›</span>
                        </button>

                        <a href="${pageContext.request.contextPath}/account/change-password" class="mc-setting-row">
                          <div><strong>Đổi mật khẩu</strong><small>Nên thay đổi định kỳ để bảo vệ tài khoản</small></div>
                          <span>›</span>
                        </a>
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

              <div class="account-form-grid account-form-grid-profile">
                <div class="form-group">
                  <label for="nameInput">Họ tên <span>*</span></label>
                  <input class="account-input" id="nameInput" name="fullName" value="${sessionScope.user.fullName}" required />
                  <small id="name-error" class="account-field-error"></small>
                </div>

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

                <div class="form-group account-address-group">
                  <label for="addressInput">Địa chỉ giao hàng</label>
                  <div class="account-address-row">
                    <input type="text" id="addressInput" name="address" value="${sessionScope.user.address}" class="account-input" placeholder="Nhập địa chỉ hoặc bấm lấy vị trí" />
                    <button type="button" class="account-btn account-btn-secondary-soft account-location-btn" onclick="getLocation()">📍 Lấy vị trí</button>
                  </div>
                </div>
              </div>

              <div class="account-profile-form-actions">
                <span class="account-profile-note">Khi đổi email, hệ thống sẽ gửi OTP để xác thực trước khi lưu.</span>
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
                view.classList.toggle("is-active", view.dataset.accountView === target);
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
              const allowed = ["overview", "orders", "vouchers", "rank", "activity", "profile", "security"];

              if (allowed.indexOf(hash) >= 0) {
                setAccountView(hash);
              } else if (window.location.hash === "#profile-settings") {
                setAccountView("profile");
              } else {
                setAccountView("overview");
              }
            });
          })();
        </script>

        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

        <script>
          function getLocation() {
            const addressInput = document.getElementById('addressInput');

            if (!navigator.geolocation) {
              alert("Trình duyệt không hỗ trợ lấy vị trí.");
              return;
            }

            if (addressInput) {
              addressInput.value = "Đang lấy vị trí hiện tại...";
            }

            navigator.geolocation.getCurrentPosition(async (pos) => {
              try {
                const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${pos.coords.latitude}&lon=${pos.coords.longitude}&accept-language=vi&addressdetails=1`;
                const res = await fetch(url).then(r => r.json());

                if (addressInput) {
                  addressInput.value = res.display_name || `${pos.coords.latitude}, ${pos.coords.longitude}`;
                }
              } catch (error) {
                if (addressInput) {
                  addressInput.value = `${pos.coords.latitude}, ${pos.coords.longitude}`;
                }
                alert("Không lấy được địa chỉ chi tiết, hệ thống đã điền tọa độ tạm thời.");
              }
            }, () => {
              if (addressInput) {
                addressInput.value = "";
              }
              alert("Không thể lấy vị trí. Vui lòng cấp quyền vị trí hoặc nhập địa chỉ thủ công.");
            }, {
              enableHighAccuracy: false,
              timeout: 8000,
              maximumAge: 60000
            });
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



<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<fmt:setLocale value="vi_VN"/>

<style>
  .account-dashboard {
    padding: 24px 0;
  }

  .account-hero {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 18px;
    padding: 22px;
    border-radius: 22px;
    background: linear-gradient(135deg, #fff0f6, #ffffff);
    border: 1px solid rgba(255, 95, 162, 0.18);
    box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
    margin-bottom: 18px;
  }

  .account-hero.admin-mode {
    background: linear-gradient(135deg, #fff0f6, #f8f4ff);
  }

  .account-hero.user-mode {
    background: linear-gradient(135deg, #f3f8ff, #ffffff);
  }

  .account-profile {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .account-avatar {
    width: 68px;
    height: 68px;
    border-radius: 22px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    font-weight: 900;
    color: #ffffff;
    background: linear-gradient(135deg, #ff5fa2, #b45cff);
    box-shadow: 0 10px 22px rgba(255, 95, 162, 0.25);
  }

  .account-hero.user-mode .account-avatar {
    background: linear-gradient(135deg, #4f8cff, #5cc8ff);
  }

  .account-title {
    margin: 0;
    font-size: 26px;
    font-weight: 900;
    color: #1f2937;
  }

  .account-subtitle {
    margin: 4px 0 0;
    color: #64748b;
    font-size: 14px;
  }

  .account-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .account-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 10px 14px;
    border-radius: 999px;
    text-decoration: none;
    font-weight: 800;
    border: 1px solid rgba(148, 163, 184, 0.35);
    background: #ffffff;
    color: #334155;
  }

  .account-btn.primary {
    background: linear-gradient(135deg, #ff5fa2, #b45cff);
    color: #ffffff;
    border: none;
  }

  .account-btn.user-primary {
    background: linear-gradient(135deg, #4f8cff, #5cc8ff);
    color: #ffffff;
    border: none;
  }

  .account-grid {
    display: grid;
    gap: 16px;
  }

  .account-grid-2 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .account-grid-3 {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .account-grid-4 {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .account-card {
    background: #ffffff;
    border: 1px solid rgba(226, 232, 240, 0.95);
    border-radius: 22px;
    box-shadow: 0 12px 28px rgba(15, 23, 42, 0.05);
    overflow: hidden;
  }

  .account-card-body {
    padding: 18px;
  }

  .account-card-title {
    margin: 0;
    font-size: 18px;
    font-weight: 900;
    color: #1f2937;
  }

  .account-muted {
    color: #64748b;
    font-size: 13px;
    margin-top: 4px;
  }

  .account-kpi {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
  }

  .account-kpi-icon {
    width: 42px;
    height: 42px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fff0f6;
    font-size: 20px;
  }

  .user-kpi-icon {
    background: #eef6ff;
  }

  .account-kpi-label {
    margin: 0;
    color: #64748b;
    font-size: 12px;
    font-weight: 900;
    text-transform: uppercase;
    letter-spacing: .04em;
  }

  .account-kpi-value {
    margin-top: 8px;
    font-size: 26px;
    font-weight: 950;
    color: #111827;
  }

  .account-kpi-note {
    margin-top: 6px;
    color: #64748b;
    font-size: 13px;
  }

  .account-form-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  .account-label {
    display: block;
    margin-bottom: 6px;
    font-weight: 800;
    color: #334155;
  }

  .account-input {
    width: 100%;
    min-height: 42px;
    border: 1px solid #dbe3ef;
    border-radius: 14px;
    padding: 10px 12px;
    outline: none;
    background: #ffffff;
  }

  .account-input:focus {
    border-color: #ff5fa2;
    box-shadow: 0 0 0 3px rgba(255, 95, 162, 0.12);
  }

  .account-submit {
    margin-top: 12px;
    border: none;
    border-radius: 999px;
    padding: 11px 18px;
    font-weight: 900;
    background: linear-gradient(135deg, #ff5fa2, #b45cff);
    color: #ffffff;
    cursor: pointer;
  }

  .user-mode-submit {
    background: linear-gradient(135deg, #4f8cff, #5cc8ff);
  }

  .account-chip {
    display: inline-flex;
    align-items: center;
    padding: 6px 10px;
    border-radius: 999px;
    background: #fff0f6;
    color: #be185d;
    font-size: 12px;
    font-weight: 900;
  }

  .account-chip.user-chip {
    background: #eef6ff;
    color: #2563eb;
  }

  .account-table-wrap {
    overflow-x: auto;
  }

  .account-table {
    width: 100%;
    border-collapse: collapse;
  }

  .account-table th,
  .account-table td {
    padding: 12px;
    border-bottom: 1px solid #edf2f7;
    text-align: left;
    vertical-align: middle;
  }

  .account-table th {
    color: #64748b;
    font-size: 12px;
    text-transform: uppercase;
    letter-spacing: .04em;
  }

  .account-empty {
    color: #64748b;
    padding: 12px 0;
  }

  .account-alert-success {
    color: #0a7a2f;
    font-weight: 700;
    margin: 8px 0;
  }

  .account-alert-error {
    color: #b00020;
    font-weight: 700;
    margin: 8px 0;
  }

  .account-section-space {
    margin-top: 16px;
  }

  @media (max-width: 980px) {
    .account-grid-4,
    .account-grid-3,
    .account-grid-2 {
      grid-template-columns: 1fr;
    }

    .account-hero {
      flex-direction: column;
      align-items: flex-start;
    }

    .account-form-grid {
      grid-template-columns: 1fr;
    }
  }
</style>

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
                  <div style="margin-top:8px;">
                    <span class="account-chip">
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
              <a href="${pageContext.request.contextPath}/admin/dashboard" class="account-btn">📊 Dashboard chi tiết</a>
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
                <div style="display:flex;justify-content:space-between;gap:12px;align-items:center;margin-bottom:12px;">
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
                <div style="display:flex;justify-content:space-between;gap:12px;align-items:center;margin-bottom:12px;">
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
                          <th style="text-align:right;">Đã bán</th>
                          <th style="text-align:right;">Doanh thu</th>
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

                            <td style="text-align:right;">
                              <c:choose>
                                <c:when test="${isNewTopFormat}">
                                  <c:out value="${p[2]}" />
                                </c:when>
                                <c:otherwise>
                                  <c:out value="${p[1]}" />
                                </c:otherwise>
                              </c:choose>
                            </td>

                            <td style="text-align:right;">
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

          <div class="account-hero user-mode">
            <div class="account-profile">
              <div class="account-avatar">
                <c:out value="${fn:toUpperCase(fn:substring(safeUsername, 0, 1))}" />
              </div>

              <div>
                <h1 class="account-title">Tài khoản của tôi</h1>
                <p class="account-subtitle">
                  Xin chào, <strong><c:out value="${safeUsername}" /></strong>. Theo dõi đơn hàng và chi tiêu cá nhân tại đây.
                </p>

                <div style="margin-top:8px;display:flex;gap:8px;flex-wrap:wrap;">
                  <span class="account-chip user-chip">
                    📧
                    <c:choose>
                      <c:when test="${not empty userEmail}">
                        <c:out value="${userEmail}" />
                      </c:when>
                      <c:otherwise>Chưa cập nhật email</c:otherwise>
                    </c:choose>
                  </span>

                  <span class="account-chip user-chip">
                    📱
                    <c:choose>
                      <c:when test="${not empty userPhone}">
                        <c:out value="${userPhone}" />
                      </c:when>
                      <c:otherwise>Chưa cập nhật SĐT</c:otherwise>
                    </c:choose>
                  </span>

                  <c:if test="${not empty rankLabel}">
                    <span class="account-chip user-chip">
                      🎖 <c:out value="${rankLabel}" />
                      <c:if test="${rankDiscount > 0}">
                        -<c:out value="${rankDiscount}" />%
                      </c:if>
                    </span>
                  </c:if>
                </div>
              </div>
            </div>

            <div class="account-actions">
              <a href="${pageContext.request.contextPath}/orders" class="account-btn user-primary">📦 Xem đơn hàng</a>
              <a href="${pageContext.request.contextPath}/account/change-password" class="account-btn">🔒 Đổi mật khẩu</a>
            </div>
          </div>

          <!-- USER KPI -->
          <div class="account-grid account-grid-3">
            <div class="account-card">
              <div class="account-card-body account-kpi">
                <div>
                  <p class="account-kpi-label">Tổng đơn hàng</p>
                  <div class="account-kpi-value">
                    <c:out value="${empty total_orders ? 0 : total_orders}" />
                  </div>
                  <div class="account-kpi-note">Tất cả đơn bạn đã tạo.</div>
                </div>
                <div class="account-kpi-icon user-kpi-icon">📦</div>
              </div>
            </div>

            <div class="account-card">
              <div class="account-card-body account-kpi">
                <div>
                  <p class="account-kpi-label">Tổng chi tiêu</p>
                  <div class="account-kpi-value">
                    <c:choose>
                      <c:when test="${not empty total_spent_vnd}">
                        <fmt:formatNumber value="${total_spent_vnd}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                      </c:when>
                      <c:otherwise>0 ₫</c:otherwise>
                    </c:choose>
                  </div>
                  <div class="account-kpi-note">Tổng tiền từ các đơn đã mua.</div>
                </div>
                <div class="account-kpi-icon user-kpi-icon">💰</div>
              </div>
            </div>

            <div class="account-card">
              <div class="account-card-body account-kpi">
                <div>
                  <p class="account-kpi-label">Đơn gần nhất</p>
                  <div class="account-kpi-value">
                    <c:choose>
                      <c:when test="${not empty latest_order}">
                        #<c:out value="${latest_order.id}" />
                      </c:when>
                      <c:otherwise>--</c:otherwise>
                    </c:choose>
                  </div>
                  <div class="account-kpi-note">Đơn hàng mới nhất của bạn.</div>
                </div>
                <div class="account-kpi-icon user-kpi-icon">🕒</div>
              </div>
            </div>
          </div>

          <!-- USER CHART -->
          <div class="account-card account-section-space">
            <div class="account-card-body">
              <div style="display:flex;justify-content:space-between;gap:12px;align-items:center;margin-bottom:12px;">
                <div>
                  <h2 class="account-card-title">📊 Chi tiêu theo thời gian</h2>
                  <p class="account-muted">Theo dõi xu hướng mua hàng của bạn.</p>
                </div>
                <span class="account-chip user-chip">Spending</span>
              </div>

              <c:choose>
                <c:when test="${not empty chart_labels and not empty chart_values}">
                  <canvas id="spendingChart" height="90"></canvas>
                </c:when>
                <c:otherwise>
                  <p class="account-empty">Chưa có dữ liệu chi tiêu.</p>
                </c:otherwise>
              </c:choose>
            </div>
          </div>

        </c:if>

        <!-- ========================================================= -->
        <!-- CONTACT FORM: USER AND ADMIN BOTH CAN UPDATE -->
        <!-- ========================================================= -->
        <div class="account-card account-section-space">
          <div class="account-card-body">
            <div style="display:flex;justify-content:space-between;gap:12px;align-items:center;margin-bottom:12px;">
              <div>
                <h2 class="account-card-title">📌 Cập nhật thông tin liên hệ</h2>
                <p class="account-muted">Thông tin này dùng cho đơn hàng và liên hệ giao hàng.</p>
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

            <form method="post" action="${pageContext.request.contextPath}/account/update-profile">

              <c:if test="${not empty csrfToken}">
                <input type="hidden" name="csrfToken" value="${fn:escapeXml(csrfToken)}" />
              </c:if>

              <c:if test="${empty csrfToken and not empty sessionScope.csrfToken}">
                <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.csrfToken)}" />
              </c:if>

              <div class="account-form-grid">
                <div>
                  <label class="account-label">Email</label>
                  <input class="account-input"
                         name="email"
                         type="email"
                         value="${not empty userEmail ? fn:escapeXml(userEmail) : ''}"
                         required />
                </div>

                <div>
                  <label class="account-label">Số điện thoại</label>
                  <input class="account-input"
                         name="phone"
                         value="${not empty userPhone ? fn:escapeXml(userPhone) : ''}"
                         required />
                </div>
              </div>

              <button class="account-submit ${sessionScope.user.admin ? '' : 'user-mode-submit'}" type="submit">
                Lưu thông tin
              </button>
            </form>
          </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

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
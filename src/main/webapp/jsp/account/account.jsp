<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

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

          <div class="account-hero user-mode">
            <div class="account-profile">
              <div class="account-avatar">
                <c:out value="${fn:toUpperCase(fn:substring(safeUsername, 0, 1))}" />
              </div>

              <div>
                <h1 class="account-title">Tài khoản của tôi</h1>
                <p class="account-subtitle">
                  Xin chào, <strong><c:out value="${safeUsername}" /></strong>. Theo dõi đơn hàng, chi tiêu và hạng khách hàng tại đây.
                </p>

                <div class="account-chip-row">
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
                    <span class="account-chip user-chip ${rankCss}">
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

          <!-- USER RANK DETAIL -->
          <div class="account-card account-section-space">
            <div class="account-card-body">
              <div class="account-card-head account-card-head-start">
                <div>
                  <h2 class="account-card-title">🎖 Hạng khách hàng</h2>
                  <p class="account-muted">Hạng được tính dựa trên tổng chi tiêu từ các đơn đã thanh toán thành công.</p>
                </div>

                <span class="account-chip user-chip ${rankCss}">
                  <c:out value="${empty rankLabel ? 'Thành viên' : rankLabel}" />
                  <c:if test="${rankDiscount > 0}">
                    -<c:out value="${rankDiscount}" />%
                  </c:if>
                </span>
              </div>

              <div class="account-grid account-grid-3">
                <div>
                  <p class="account-kpi-label">Tổng chi tiêu xét hạng</p>
                  <div class="account-kpi-value">
                    <fmt:formatNumber value="${empty rankTotalSpent ? 0 : rankTotalSpent}"
                                      type="number"
                                      groupingUsed="true"
                                      maxFractionDigits="0"/> ₫
                  </div>
                  <div class="account-kpi-note">Không tính đơn đã hủy.</div>
                </div>

                <div>
                  <p class="account-kpi-label">Ưu đãi hiện tại</p>
                  <div class="account-kpi-value">
                    <c:out value="${empty rankDiscount ? 0 : rankDiscount}" />%
                  </div>
                  <div class="account-kpi-note">Tự động áp dụng khi thanh toán.</div>
                </div>

                <div>
                  <p class="account-kpi-label">Số đơn đã thanh toán</p>
                  <div class="account-kpi-value">
                    <c:out value="${empty rankPaidOrderCount ? 0 : rankPaidOrderCount}" />
                  </div>
                  <div class="account-kpi-note">Chỉ tính đơn hợp lệ.</div>
                </div>
              </div>

              <div class="account-rank-next">
                <c:choose>
                  <c:when test="${maxRank}">
                    <p class="account-muted">
                      Bạn đã đạt hạng cao nhất. Cảm ơn bạn đã đồng hành cùng MyCosmetic.
                    </p>

                    <div class="account-rank-progress">
                      <div class="account-rank-progress__bar is-full"></div>
                    </div>
                  </c:when>

                  <c:otherwise>
                    <p class="account-muted">
                      Còn
                      <strong>
                        <fmt:formatNumber value="${empty amountToNextRank ? 0 : amountToNextRank}"
                                          type="number"
                                          groupingUsed="true"
                                          maxFractionDigits="0"/> ₫
                      </strong>
                      để lên hạng
                      <strong><c:out value="${empty nextRankLabel ? 'tiếp theo' : nextRankLabel}" /></strong>.
                    </p>

                    <div class="account-rank-progress">
                      <div class="account-rank-progress__bar"
                           data-progress="${empty rankProgressPercent ? 0 : rankProgressPercent}">
                      </div>
                    </div>

                    <p class="account-muted account-progress-note">
                      Tiến độ:
                      <strong><c:out value="${empty rankProgressPercent ? 0 : rankProgressPercent}" />%</strong>
                    </p>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
          </div>

          <!-- USER AVAILABLE COUPONS -->
          <div class="account-card account-section-space">
            <div class="account-card-body">
              <div class="account-card-head account-card-head-start">
                <div>
                  <h2 class="account-card-title" style="color: var(--pink-dark);">💼 Ví voucher của bạn</h2>
                  <p class="account-muted">
                    Danh sách các mã giảm giá bạn đã thu thập thành công từ trang chủ.
                  </p>
                </div>
                <span class="account-chip user-chip" style="background-color: var(--pink-soft); color: var(--pink-main); font-weight: bold;">
                  🎰 Đã lưu: ${fn:length(savedCoupons)} mã
                </span>
              </div>

              <c:choose>
                <c:when test="${not empty savedCoupons}">
                  <div class="account-coupon-grid">
                    <c:forEach var="savedCp" items="${savedCoupons}">
                      <div class="account-coupon-card" style="border: 1px dashed var(--pink-main); background: var(--pink-soft);">

                        <div class="account-coupon-top">
                          <div class="account-coupon-code">
                            <span>🎁</span>
                            <span style="color: var(--pink-dark); font-weight: bold;"><c:out value="${savedCp.code}" /></span>
                          </div>

                          <button type="button"
                                  class="account-coupon-copy"
                                  style="background: var(--pink-main); color: #fff;"
                                  data-coupon-code="<c:out value='${savedCp.code}'/>"
                                  onclick="copyCouponCode(this.dataset.couponCode)">
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
                          <div>
                            <strong>Mô tả:</strong>
                            <c:out value="${not empty savedCp.description ? savedCp.description : 'Áp dụng giảm trừ trực tiếp vào hóa đơn khi thanh toán.'}" />
                          </div>

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
                    <p style="font-size: 14px;">Bạn chưa lưu mã giảm giá nào từ trang chủ.</p>
                    <a href="${pageContext.request.contextPath}/" style="display: inline-block; margin-top: 12px; font-size: 13px; color: #fff; background: var(--pink-main); padding: 6px 16px; border-radius: 20px; font-weight: bold;">
                      Bấm vào đây để đi tìm mã uư đãi 🏃‍♂️
                    </a>
                  </div>
                </c:otherwise>
              </c:choose>
            </div>
          </div>

          <!-- USER KPI -->
          <div class="account-grid account-grid-3 account-section-space">
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


          <!-- USER SEARCH HISTORY -->
          <div class="account-card account-section-space account-search-history-card">
            <div class="account-card-body">
              <div class="account-card-head account-card-head-start">
                <div>
                  <h2 class="account-card-title">🔎 Lịch sử tìm kiếm</h2>
                  <p class="account-muted">
                    Lưu lại các từ khóa bạn đã tìm để có thể xem lại nhanh sản phẩm quan tâm.
                  </p>
                </div>

                <div class="account-search-history-actions">
                  <span class="account-chip user-chip">
                    <c:out value="${empty searchHistoryCount ? 0 : searchHistoryCount}" /> lượt tìm
                  </span>

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

                      <button type="submit" class="search-history-clear-btn">
                        Xóa tất cả
                      </button>
                    </form>
                  </c:if>
                </div>
              </div>

              <c:if test="${param.deleteSuccess == '1'}">
                <p class="account-alert-success">Đã xóa lịch sử tìm kiếm.</p>
              </c:if>
              <c:if test="${param.deleteFailed == '1'}">
                <p class="account-alert-error">Không thể xóa lịch sử tìm kiếm này.</p>
              </c:if>
              <c:if test="${param.clearSuccess == '1'}">
                <p class="account-alert-success">Đã xóa toàn bộ lịch sử tìm kiếm.</p>
              </c:if>
              <c:if test="${param.clearEmpty == '1'}">
                <p class="account-alert-success">Lịch sử tìm kiếm đang trống.</p>
              </c:if>

              <c:choose>
                <c:when test="${not empty searchHistories}">
                  <div class="search-history-list">
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

                      <div class="search-history-item">
                        <a class="search-history-main"
                           href="${fn:escapeXml(historyHref)}"
                           title="Tìm lại: ${fn:escapeXml(history.keyword)}">
                          <span class="search-history-icon">🔍</span>

                          <span class="search-history-content">
                            <strong class="search-history-keyword">
                              <c:out value="${history.keyword}" />
                            </strong>

                            <span class="search-history-meta">
                              <span>
                                <c:out value="${empty history.resultCount ? 0 : history.resultCount}" /> kết quả
                              </span>
                              <span>•</span>
                              <span>
                                Đã tìm <c:out value="${empty history.searchCount ? 1 : history.searchCount}" /> lần
                              </span>
                              <span>•</span>
                              <span>
                                <c:out value="${history.displayLastSearchedAt}" />
                              </span>
                            </span>
                          </span>
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

                          <button type="submit" class="search-history-delete-btn" title="Xóa lịch sử">
                            ×
                          </button>
                        </form>
                      </div>
                    </c:forEach>
                  </div>
                </c:when>

                <c:otherwise>
                  <div class="search-history-empty">
                    <div class="search-history-empty-icon">🕘</div>
                    <p class="account-empty">
                      Bạn chưa có lịch sử tìm kiếm. Hãy tìm kiếm sản phẩm để hệ thống lưu lại tại đây.
                    </p>
                    <a href="${pageContext.request.contextPath}/products" class="account-btn user-primary">
                      Khám phá sản phẩm
                    </a>
                  </div>
                </c:otherwise>
              </c:choose>
            </div>
          </div>

          <!-- USER CHART -->
          <div class="account-card account-section-space">
            <div class="account-card-body">
              <div class="account-card-head account-card-head-center">
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
            <div class="account-card-head account-card-head-center">
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

            <form id="updateProfileForm" method="post">
              <c:if test="${not empty csrfToken}">
                <input type="hidden" name="csrfToken" value="${fn:escapeXml(csrfToken)}" />
              </c:if>
              <c:if test="${empty csrfToken and not empty sessionScope.csrfToken}">
                <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.csrfToken)}" />
              </c:if>

              <div class="account-form-grid">
                <div class="form-group">
                  <label>Họ tên</label>
                  <input class="account-input" id="nameInput" name="fullName" value="${sessionScope.user.fullName}" required />
                  <small id="name-error" style="display:none; color: #e53935; font-size: 13px; margin-top: 5px; font-weight: 500;"></small>
                </div>

                <div class="form-group">
                  <label>Email</label>
                  <input class="account-input" id="emailInput" name="email" value="${sessionScope.user.email}" required />
                  <small id="email-error" style="display:none; color: #e53935; font-size: 13px; margin-top: 5px; font-weight: 500;"></small>
                </div>

                <div class="form-group">
                  <label>Số điện thoại</label>
                  <input class="account-input" id="phoneInput" name="phone" value="${sessionScope.user.phone}" required />
                  <small id="phone-error" style="display:none; color: #e53935; font-size: 13px; margin-top: 5px; font-weight: 500;"></small>
                </div>

                <div>
                  <label>Ngày sinh</label>
                  <input class="account-input" type="date" name="birthDate" value="${sessionScope.user.birthDate}" />
                </div>

                <div>
                  <label>Giới tính</label>
                  <select class="account-input" name="gender">
                    <option value="Male" ${sessionScope.user.gender == 'Male' ? 'selected' : ''}>Nam</option>
                    <option value="Female" ${sessionScope.user.gender == 'Female' ? 'selected' : ''}>Nữ</option>
                  </select>
                </div>
              </div> <div style="margin-top:15px">
              <label>Địa chỉ</label>
              <div style="display:flex; gap:10px">
                <input type="text" name="address" value="${sessionScope.user.address}" class="account-input">
                <button type="button" class="account-btn" onclick="getLocation()">Lấy vị trí</button>
              </div>
            </div>

              <button type="submit" class="btn-save" id="saveProfileBtn" style="margin-top: 20px;">Lưu thay đổi</button>
            </form>
          </div>
        </div>

        <div id="otpModal" class="modal-overlay">
          <div class="modal-content">
            <h3>Xác thực OTP</h3>
            <p>Vui lòng nhập mã OTP đã gửi về email của bạn</p>
            <input type="text" id="otp_input" placeholder="000000" maxlength="6">
            <button onclick="xacThucOtp()" class="btn-save">Xác nhận</button>
            <button onclick="dongPopupOtp()" style="background:none; border:none; color:#777; margin-top:10px; cursor:pointer;">Hủy bỏ</button>
          </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

        <script>
          function getLocation() {
            navigator.geolocation.getCurrentPosition(async (pos) => {
              const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${pos.coords.latitude}&lon=${pos.coords.longitude}`;
              const res = await fetch(url).then(r => r.json());
              document.getElementById('addressInput').value = res.display_name;
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
    input.style.borderColor = "#e53935"; // Viền đỏ
    input.classList.add("is-invalid"); // Đánh dấu lỗi để chặn form
    errorElement.innerText = message;
    errorElement.style.display = "block";
  }

  function showSuccess(input, errorElement) {
    input.style.borderColor = "#43a047"; // Viền xanh lá
    input.classList.remove("is-invalid");
    errorElement.style.display = "none";
  }

  function resetState(input, errorElement) {
    input.style.borderColor = "#ddd"; // Khôi phục viền gốc
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

<style>
  /* Nút Lưu thay đổi */
  .btn-save {
    background: #ff5fa2;
    color: white;
    padding: 12px 24px;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.3s ease;
    width: 100%;
  }

  .btn-save:hover {
    background: #e04a8a;
  }

  /* Modal Overlay */
  .modal-overlay {
    display: none; /* Mặc định ẩn */
    position: fixed;
    top: 0; left: 0;
    width: 100%; height: 100%;
    background: rgba(0,0,0,0.5);
    justify-content: center;
    align-items: center;
    z-index: 1000;
  }

  .modal-content {
    background: #fff;
    padding: 30px;
    border-radius: 16px;
    width: 90%;
    max-width: 400px;
    text-align: center;
    box-shadow: 0 10px 25px rgba(0,0,0,0.2);
  }

  .modal-content h3 { margin-bottom: 15px; color: #ff5fa2; }
  .modal-content input {
    width: 100%;
    padding: 10px;
    margin: 15px 0;
    border: 1px solid #ddd;
    border-radius: 6px;
    text-align: center;
    font-size: 18px;
    letter-spacing: 5px;
  }

</style>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<fmt:setLocale value="vi_VN"/>

<section class="section">
  <div class="container order-page">

    <c:choose>
      <c:when test="${not empty sessionScope.user}">
          <h3 class="order-section-title"></h3>

        <!-- ================= HEADER ================= -->
        <div class="order-header">

          <div class="avatar">
            <c:choose>
              <c:when test="${not empty sessionScope.user.username}">
                ${fn:toUpperCase(fn:substring(sessionScope.user.username, 0, 1))}
              </c:when>
              <c:otherwise>U</c:otherwise>
            </c:choose>
          </div>

          <div class="order-info">
            <div class="username-row">
              <h2 class="username">${sessionScope.user.username}</h2>

              <!-- ===== RANK (SAFE) ===== -->
              <c:if test="${not empty rankLabel}">
                <span class="user-rank-badge ${rankCss}">
                  🎖 ${rankLabel}
                  <c:if test="${rankDiscount > 0}">
                    -${rankDiscount}%
                  </c:if>
                </span>
              </c:if>
            </div>

            <!-- ===== EMAIL (SAFE) ===== -->
            <p class="email">
              <c:choose>
                <c:when test="${not empty userEmail}">
                  <c:out value="${userEmail}" />
                </c:when>
                <c:otherwise>Chưa cập nhật email</c:otherwise>
              </c:choose>
            </p>

            <!-- ===== PHONE (SAFE) ===== -->
            <p class="email">
              <c:choose>
                <c:when test="${not empty userPhone}">
                  <c:out value="${userPhone}" />
                </c:when>
                <c:otherwise>Chưa cập nhật số điện thoại</c:otherwise>
              </c:choose>
            </p>

            <div class="account-actions">
              <a href="${pageContext.request.contextPath}/account/change-password"
                 class="btn-auth btn-outline"> 🔒 Đổi mật khẩu </a>

              <!-- ADMIN BUTTON -->
              <c:if test="${sessionScope.user.admin}">
                <a href="${pageContext.request.contextPath}/admin"
                   class="btn-auth btn-outline"> 🛠 Vào Admin </a>
              </c:if>
            </div>
          </div>
        </div>

        <!-- ================= UPDATE CONTACT ================= -->
        <div class="order-table-wrap" style="margin-top:14px;">
          <h3 class="order-section-title">📌 Cập nhật thông tin liên hệ</h3>

          <c:if test="${param.update == 'success'}">
            <p class="empty-text" style="color:#0a7a2f;">Cập nhật thành công.</p>
          </c:if>
          <c:if test="${param.update == 'invalid_email'}">
            <p class="empty-text" style="color:#b00020;">Email không hợp lệ.</p>
          </c:if>
          <c:if test="${param.update == 'invalid_phone'}">
            <p class="empty-text" style="color:#b00020;">Số điện thoại không hợp lệ (9–11 chữ số).</p>
          </c:if>
          <c:if test="${param.update == 'email_used'}">
            <p class="empty-text" style="color:#b00020;">Email đã được sử dụng.</p>
          </c:if>

          <form method="post" action="${pageContext.request.contextPath}/account/update-profile">
            <div style="display:flex;gap:12px;flex-wrap:wrap;">
              <div style="flex:1;min-width:240px;">
                <label style="display:block;margin-bottom:6px;font-weight:700;">Email</label>
                <input class="auth-input" name="email" type="email"
                       value="<c:out value='${userEmail}'/>" required />
              </div>

              <div style="flex:1;min-width:240px;">
                <label style="display:block;margin-bottom:6px;font-weight:700;">Số điện thoại</label>
                <input class="auth-input" name="phone"
                       value="<c:out value='${userPhone}'/>" required />
              </div>
            </div>

            <div class="auth-actions" style="margin-top:10px;">
              <button class="auth-submit" type="submit">Lưu thông tin</button>
            </div>
          </form>
        </div>

        <!-- ================= ADMIN ================= -->
        <c:if test="${sessionScope.user.admin}">

          <div class="dashboard-stats">
            <div class="stat-card pink">
              <span class="stat-icon">💵</span>
              <div>
                <p>Tổng doanh thu</p>
                <strong>
                  <c:choose>
                    <c:when test="${not empty admin_total_revenue_vnd}">
                      <fmt:formatNumber value="${admin_total_revenue_vnd}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                    </c:when>
                    <c:otherwise>0 ₫</c:otherwise>
                  </c:choose>
                </strong>
              </div>
            </div>

            <div class="stat-card">
              <span class="stat-icon">🧾</span>
              <div>
                <p>Tổng đơn</p>
                <strong>${admin_total_orders}</strong>
              </div>
            </div>

            <div class="stat-card">
              <span class="stat-icon">📊</span>
              <div>
                <p>AOV</p>
                <strong>
                  <c:choose>
                    <c:when test="${not empty admin_aov_vnd}">
                      <fmt:formatNumber value="${admin_aov_vnd}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                    </c:when>
                    <c:otherwise>0 ₫</c:otherwise>
                  </c:choose>
                </strong>
              </div>
            </div>
          </div>

          <div class="dashboard-stats">
            <div class="stat-card">
              <span class="stat-icon">📅</span>
              <div>
                <p>Tháng này</p>
                <strong>
                  <c:choose>
                    <c:when test="${not empty admin_total_revenue_vnd}">
                      <fmt:formatNumber value="${admin_total_revenue_vnd}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                    </c:when>
                    <c:otherwise>0 ₫</c:otherwise>
                  </c:choose>
                </strong>
              </div>
            </div>

            <div class="stat-card">
              <span class="stat-icon">⏪</span>
              <div>
                <p>Tháng trước</p>
                <strong>
                  <c:choose>
                    <c:when test="${not empty last_month_revenue}">
                      <fmt:formatNumber value="${last_month_revenue}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                    </c:when>
                    <c:otherwise>0 ₫</c:otherwise>
                  </c:choose>
                </strong>
              </div>
            </div>

            <div class="stat-card ${revenue_diff >= 0 ? 'pink' : ''}">
              <span class="stat-icon">
                <c:choose>
                  <c:when test="${revenue_diff >= 0}">📈</c:when>
                  <c:otherwise>📉</c:otherwise>
                </c:choose>
              </span>
              <div>
                <p>So với tháng trước</p>
                <strong>
                  <c:choose>
                    <c:when test="${not empty revenue_diff_vnd}">
                      <fmt:formatNumber value="${revenue_diff_vnd}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                    </c:when>
                    <c:otherwise>0 ₫</c:otherwise>
                  </c:choose>
                  (<c:out value="${revenue_percent}" />%)
                </strong>
              </div>
            </div>
          </div>

          <div class="dashboard-chart">
            <h3 class="order-section-title">🏬 Doanh thu toàn cửa hàng</h3>

            <c:choose>
              <c:when test="${not empty admin_chart_labels}">
                <canvas id="storeRevenueChart"></canvas>
              </c:when>
              <c:otherwise>
                <p class="empty-text">Chưa có dữ liệu doanh thu</p>
              </c:otherwise>
            </c:choose>
          </div>

          <div class="order-table-wrap">
            <h3 class="order-section-title">🔥 Top sản phẩm bán chạy</h3>

            <c:choose>
              <c:when test="${not empty top_products}">
                <table class="order-table">
                  <thead>
                    <tr>
                      <th>Sản phẩm</th>
                      <th>Đã bán</th>
                      <th>Doanh thu</th>
                    </tr>
                  </thead>

                  <tbody>
                    <c:forEach var="p" items="${top_products}">
                      <tr>
                        <td><c:out value="${p[0]}" /></td>
                        <td><c:out value="${p[1]}" /></td>
                        <td class="price">
                          <c:choose>
                            <c:when test="${not empty p[2]}">
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
                <p class="empty-text">Chưa có dữ liệu bán hàng</p>
              </c:otherwise>
            </c:choose>
          </div>

        </c:if>
        <!-- =============== END ADMIN =============== -->

        <!-- ================= USER STATS ================= -->
        <div class="dashboard-stats">
          <div class="stat-card">
            <span class="stat-icon">📦</span>
            <div>
              <p>Tổng đơn hàng</p>
              <strong>${total_orders}</strong>
            </div>
          </div>

          <div class="stat-card pink">
            <span class="stat-icon">💰</span>
            <div>
              <p>Tổng chi tiêu</p>
              <strong>
                <c:choose>
                  <c:when test="${not empty total_spent_vnd}">
                    <fmt:formatNumber value="${total_spent_vnd}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                  </c:when>
                  <c:otherwise>0 ₫</c:otherwise>
                </c:choose>
              </strong>
            </div>
          </div>

          <c:if test="${not empty latest_order}">
            <div class="stat-card">
              <span class="stat-icon">🕒</span>
              <div>
                <p>Đơn gần nhất</p>
                <strong>#${latest_order.id}</strong>
              </div>
            </div>
          </c:if>
        </div>

        <div class="dashboard-chart">
          <h3 class="order-section-title">📊 Chi tiêu theo thời gian</h3>

          <c:choose>
            <c:when test="${not empty chart_labels}">
              <canvas id="spendingChart"></canvas>
            </c:when>
            <c:otherwise>
              <p class="empty-text">Chưa có dữ liệu chi tiêu</p>
            </c:otherwise>
          </c:choose>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

        <c:if test="${not empty chart_labels}">
          <script>
            new Chart(document.getElementById('spendingChart'), {
              type: 'line',
              data: {
                labels: ${chart_labels},
                datasets: [{
                  data: ${chart_values},
                  borderColor: '#ff5fa2',
                  backgroundColor: 'rgba(255,95,162,0.2)',
                  fill: true,
                  tension: 0.4
                }]
              },
              options: {
                plugins: { legend: { display: false } }
              }
            });
          </script>
        </c:if>

        <c:if test="${sessionScope.user.admin and not empty admin_chart_labels}">
          <script>
            new Chart(document.getElementById('storeRevenueChart'), {
              type: 'bar',
              data: {
                labels: ${admin_chart_labels},
                datasets: [{
                  data: ${admin_chart_values},
                  backgroundColor: 'rgba(255,95,162,0.6)',
                  borderRadius: 8
                }]
              },
              options: {
                plugins: { legend: { display: false } }
              }
            });
          </script>
        </c:if>

      </c:when>

      <c:otherwise>
        <p class="empty-text">Bạn cần đăng nhập để xem trang này.</p>
      </c:otherwise>
    </c:choose>

  </div>
</section>

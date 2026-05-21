<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Dashboard" scope="request"/>
<c:set var="activeMenu" value="dashboard" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-center.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Dashboard</h1>
        <p class="admin-subtext">Tổng quan nhanh hệ thống và tình hình kinh doanh.</p>
      </div>
    </div>

    <!-- KPI GRID -->
    <div class="admin-grid" style="grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px;">

      <div class="admin-card">
        <div class="admin-card__body admin-stack">
          <div class="admin-muted" style="font-weight:800; font-size:12px;">Tổng đơn đã thanh toán</div>
          <div class="admin-h1">
            <c:out value="${orderCount}"/>
          </div>
          <div class="admin-subtext">Số đơn có trạng thái thanh toán PAID.</div>
        </div>
      </div>

      <div class="admin-card">
        <div class="admin-card__body admin-stack">
          <div class="admin-muted" style="font-weight:800; font-size:12px;">Tổng doanh thu</div>
          <div class="admin-h1">
            <fmt:formatNumber value="${totalRevenue}" type="number" groupingUsed="true"/> ₫
          </div>
          <div class="admin-subtext">Tổng doanh thu từ các đơn hợp lệ.</div>
        </div>
      </div>

      <div class="admin-card">
        <div class="admin-card__body admin-stack">
          <div class="admin-muted" style="font-weight:800; font-size:12px;">Doanh thu hôm nay</div>
          <div class="admin-h1">
            <fmt:formatNumber value="${todayRevenue}" type="number" groupingUsed="true"/> ₫
          </div>
          <div class="admin-subtext">
            Số đơn hôm nay: <strong><c:out value="${todayOrderCount}"/></strong>
          </div>
        </div>
      </div>

      <div class="admin-card">
        <div class="admin-card__body admin-stack">
          <div class="admin-muted" style="font-weight:800; font-size:12px;">Giá trị đơn trung bình</div>
          <div class="admin-h1">
            <fmt:formatNumber value="${averageOrderValue}" type="number" groupingUsed="true"/> ₫
          </div>
          <div class="admin-subtext">AOV của các đơn đã thanh toán.</div>
        </div>
      </div>

    </div>

    <!-- SECOND KPI GRID -->
    <div class="admin-grid" style="grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; margin-top: 16px;">

      <div class="admin-card">
        <div class="admin-card__body admin-stack">
          <div class="admin-muted" style="font-weight:800; font-size:12px;">Khách hàng</div>
          <div class="admin-h1">
            <c:out value="${userCount}"/>
          </div>
          <div class="admin-subtext">Tổng tài khoản người dùng.</div>
        </div>
      </div>

      <div class="admin-card">
        <div class="admin-card__body admin-stack">
          <div class="admin-muted" style="font-weight:800; font-size:12px;">Sản phẩm đang bán</div>
          <div class="admin-h1">
            <c:out value="${productCount}"/>
          </div>
          <div class="admin-subtext">Sản phẩm có trạng thái đang hoạt động.</div>
        </div>
      </div>

      <div class="admin-card">
        <div class="admin-card__body admin-stack">
          <div class="admin-muted" style="font-weight:800; font-size:12px;">Đơn chờ xử lý</div>
          <div class="admin-h1">
            <c:out value="${pendingOrderCount}"/>
          </div>
          <div class="admin-subtext">Bao gồm đơn đang xử lý và đã xác nhận.</div>
        </div>
      </div>

      <div class="admin-card">
        <div class="admin-card__body admin-stack">
          <div class="admin-muted" style="font-weight:800; font-size:12px;">Tăng trưởng tháng</div>
          <div class="admin-h1">
            <c:out value="${monthGrowthPercent}"/>%
          </div>
          <div class="admin-subtext">
            So với doanh thu tháng trước.
          </div>
        </div>
      </div>

    </div>

    <!-- REVENUE SUMMARY -->
    <div class="admin-grid" style="grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; margin-top: 16px;">

      <div class="admin-card">
        <div class="admin-card__body admin-stack">
          <div class="admin-muted" style="font-weight:800; font-size:12px;">Doanh thu tháng này</div>
          <div class="admin-h1">
            <fmt:formatNumber value="${thisMonthRevenue}" type="number" groupingUsed="true"/> ₫
          </div>
          <div class="admin-subtext">Tổng doanh thu trong tháng hiện tại.</div>
        </div>
      </div>

      <div class="admin-card">
        <div class="admin-card__body admin-stack">
          <div class="admin-muted" style="font-weight:800; font-size:12px;">Doanh thu tháng trước</div>
          <div class="admin-h1">
            <fmt:formatNumber value="${prevMonthRevenue}" type="number" groupingUsed="true"/> ₫
          </div>
          <div class="admin-subtext">Dùng để so sánh tăng trưởng tháng.</div>
        </div>
      </div>

      <div class="admin-card">
        <div class="admin-card__body admin-stack">
          <div class="admin-muted" style="font-weight:800; font-size:12px;">Doanh thu 30 ngày gần nhất</div>
          <div class="admin-h1">
            <fmt:formatNumber value="${last30DaysRevenue}" type="number" groupingUsed="true"/> ₫
          </div>
          <div class="admin-subtext">
            Chênh lệch:
            <strong>
              <fmt:formatNumber value="${rollingDiffVnd}" type="number" groupingUsed="true"/> ₫
            </strong>
            /
            <strong><c:out value="${rollingGrowthPercent}"/>%</strong>
          </div>
        </div>
      </div>

    </div>

    <!-- CHARTS -->
    <div class="admin-grid" style="grid-template-columns: 2fr 1fr; gap: 16px; margin-top: 16px;">

      <div class="admin-card">
        <div class="admin-card__body">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
            <div>
              <h2 style="margin:0; font-size:18px;">Doanh thu 12 tháng gần nhất</h2>
              <p class="admin-subtext" style="margin:4px 0 0;">Biểu đồ doanh thu theo từng tháng.</p>
            </div>
            <span class="admin-chip">Revenue</span>
          </div>
          <canvas id="last12MonthRevenueChart" height="110"></canvas>
        </div>
      </div>

      <div class="admin-card">
        <div class="admin-card__body">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
            <div>
              <h2 style="margin:0; font-size:18px;">Trạng thái đơn hàng</h2>
              <p class="admin-subtext" style="margin:4px 0 0;">Tỷ lệ đơn theo trạng thái.</p>
            </div>
            <span class="admin-chip">Orders</span>
          </div>
          <canvas id="orderStatusChart" height="180"></canvas>
        </div>
      </div>

    </div>

    <div class="admin-card" style="margin-top:16px;">
      <div class="admin-card__body">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
          <div>
            <h2 style="margin:0; font-size:18px;">Doanh thu 7 ngày gần nhất</h2>
            <p class="admin-subtext" style="margin:4px 0 0;">Theo dõi xu hướng doanh thu ngắn hạn.</p>
          </div>
          <span class="admin-chip">Last 7 days</span>
        </div>
        <canvas id="last7DaysRevenueChart" height="85"></canvas>
      </div>
    </div>

    <!-- TABLES -->
    <div class="admin-grid" style="grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 16px;">

      <!-- TOP PRODUCTS -->
      <div class="admin-card">
        <div class="admin-card__body">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
            <div>
              <h2 style="margin:0; font-size:18px;">Top sản phẩm bán chạy</h2>
              <p class="admin-subtext" style="margin:4px 0 0;">Top 5 sản phẩm theo số lượng bán.</p>
            </div>
            <span class="admin-chip">Top 5</span>
          </div>

          <div class="admin-table-wrap">
            <table class="admin-table">
              <thead>
              <tr>
                <th>Sản phẩm</th>
                <th style="text-align:right;">Đã bán</th>
                <th style="text-align:right;">Doanh thu</th>
              </tr>
              </thead>
              <tbody>
              <c:choose>
                <c:when test="${not empty topProducts}">
                  <c:forEach var="product" items="${topProducts}">
                    <tr>
                      <td>
                        <strong><c:out value="${product[1]}"/></strong>
                      </td>
                      <td style="text-align:right;">
                        <c:out value="${product[2]}"/>
                      </td>
                      <td style="text-align:right;">
                        <fmt:formatNumber value="${product[3]}" type="number" groupingUsed="true"/> ₫
                      </td>
                    </tr>
                  </c:forEach>
                </c:when>
                <c:otherwise>
                  <tr>
                    <td colspan="3" class="admin-subtext">Chưa có dữ liệu sản phẩm bán chạy.</td>
                  </tr>
                </c:otherwise>
              </c:choose>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- RECENT ORDERS -->
      <div class="admin-card">
        <div class="admin-card__body">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
            <div>
              <h2 style="margin:0; font-size:18px;">Đơn hàng gần đây</h2>
              <p class="admin-subtext" style="margin:4px 0 0;">5 đơn hàng mới nhất trong hệ thống.</p>
            </div>
            <span class="admin-chip">Recent</span>
          </div>

          <div class="admin-table-wrap">
            <table class="admin-table">
              <thead>
              <tr>
                <th>Mã đơn</th>
                <th>Khách hàng</th>
                <th style="text-align:right;">Tổng tiền</th>
                <th>Trạng thái</th>
              </tr>
              </thead>
              <tbody>
              <c:choose>
                <c:when test="${not empty recentOrders}">
                  <c:forEach var="order" items="${recentOrders}">
                    <tr>
                      <td>#<c:out value="${order[0]}"/></td>
                      <td>
                        <c:out value="${order[1]}"/>
                        <div class="admin-subtext">
                          <fmt:formatDate value="${order[5]}" pattern="dd/MM/yyyy HH:mm"/>
                        </div>
                      </td>
                      <td style="text-align:right;">
                        <fmt:formatNumber value="${order[2]}" type="number" groupingUsed="true"/> ₫
                      </td>
                      <td>
                        <span class="admin-chip">
                          <c:out value="${order[3]}"/>
                        </span>
                      </td>
                    </tr>
                  </c:forEach>
                </c:when>
                <c:otherwise>
                  <tr>
                    <td colspan="4" class="admin-subtext">Chưa có đơn hàng gần đây.</td>
                  </tr>
                </c:otherwise>
              </c:choose>
              </tbody>
            </table>
          </div>
        </div>
      </div>

    </div>

  </div>
</main>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<script>
  const last12MonthLabels =
          <c:out value="${empty last12MonthLabelsJson ? '[]' : last12MonthLabelsJson}" escapeXml="false"/>;

  const last12MonthRevenueValues =
          <c:out value="${empty last12MonthRevenueValuesJson ? '[]' : last12MonthRevenueValuesJson}" escapeXml="false"/>;

  const last7DaysLabels =
          <c:out value="${empty last7DaysLabelsJson ? '[]' : last7DaysLabelsJson}" escapeXml="false"/>;

  const last7DaysRevenueValues =
          <c:out value="${empty last7DaysRevenueValuesJson ? '[]' : last7DaysRevenueValuesJson}" escapeXml="false"/>;

  const orderStatusLabels =
          <c:out value="${empty orderStatusLabelsJson ? '[]' : orderStatusLabelsJson}" escapeXml="false"/>;

  const orderStatusValues =
          <c:out value="${empty orderStatusValuesJson ? '[]' : orderStatusValuesJson}" escapeXml="false"/>;

  function formatVnd(value) {
    return new Intl.NumberFormat('vi-VN').format(value) + ' ₫';
  }

  const last12MonthRevenueChart = document.getElementById('last12MonthRevenueChart');

  if (last12MonthRevenueChart) {
    new Chart(last12MonthRevenueChart, {
      type: 'bar',
      data: {
        labels: last12MonthLabels,
        datasets: [{
          label: 'Doanh thu',
          data: last12MonthRevenueValues,
          borderWidth: 1
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
                return formatVnd(context.raw || 0);
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: function(value) {
                return formatVnd(value);
              }
            }
          }
        }
      }
    });
  }

  const last7DaysRevenueChart = document.getElementById('last7DaysRevenueChart');

  if (last7DaysRevenueChart) {
    new Chart(last7DaysRevenueChart, {
      type: 'line',
      data: {
        labels: last7DaysLabels,
        datasets: [{
          label: 'Doanh thu',
          data: last7DaysRevenueValues,
          tension: 0.35,
          fill: false,
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
                return formatVnd(context.raw || 0);
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: function(value) {
                return formatVnd(value);
              }
            }
          }
        }
      }
    });
  }

  const orderStatusChart = document.getElementById('orderStatusChart');

  if (orderStatusChart) {
    new Chart(orderStatusChart, {
      type: 'doughnut',
      data: {
        labels: orderStatusLabels,
        datasets: [{
          label: 'Số đơn',
          data: orderStatusValues,
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            position: 'bottom'
          }
        }
      }
    });
  }
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>
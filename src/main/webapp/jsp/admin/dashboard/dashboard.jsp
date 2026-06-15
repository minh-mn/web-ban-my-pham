<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Tổng quan doanh thu" scope="request"/>
<c:set var="activeMenu" value="dashboard" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-center.css" scope="request"/>

<c:set var="inventoryAlertCount" value="${outOfStockCount + lowStockCount}" />
<c:set var="inventoryNormalCount" value="${productCount - outOfStockCount - lowStockCount}" />
<c:if test="${inventoryNormalCount < 0}">
  <c:set var="inventoryNormalCount" value="0" />
</c:if>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container admin-dashboard-page">

    <section class="admin-dashboard-hero">
      <div class="admin-dashboard-hero__content">
        <span class="admin-dashboard-eyebrow">TỔNG QUAN &amp; DOANH THU</span>
        <h1 class="admin-dashboard-title">Dashboard quản trị</h1>
        <p class="admin-dashboard-subtitle">
          Theo dõi nhanh doanh thu, đơn hàng, hiệu suất sản phẩm và tình trạng tồn kho của hệ thống MyCosmetic.
          Các chỉ số giúp admin phát hiện vấn đề và chuyển nhanh đến khu vực cần xử lý.
        </p>
      </div>

      <div class="admin-dashboard-hero__actions">
        <a class="admin-btn admin-btn--primary" href="${pageContext.request.contextPath}/admin/orders">
          Xem đơn hàng
        </a>
        <a class="admin-btn" href="${pageContext.request.contextPath}/admin/inventory">
          Quản lý tồn kho
        </a>
      </div>
    </section>

    <section class="admin-dashboard-kpi-grid">
      <div class="admin-dashboard-kpi admin-dashboard-kpi--orders">
        <span class="admin-dashboard-kpi__icon">🛒</span>
        <span class="admin-dashboard-kpi__label">Tổng đơn đã thanh toán</span>
        <strong class="admin-dashboard-kpi__value">
          <c:out value="${orderCount}"/>
        </strong>
        <span class="admin-dashboard-kpi__note">Số đơn có trạng thái thanh toán PAID.</span>
      </div>

      <div class="admin-dashboard-kpi admin-dashboard-kpi--revenue">
        <span class="admin-dashboard-kpi__icon">💰</span>
        <span class="admin-dashboard-kpi__label">Tổng doanh thu</span>
        <strong class="admin-dashboard-kpi__value admin-dashboard-kpi__value--money">
          <fmt:formatNumber value="${totalRevenue}" type="number" groupingUsed="true"/> ₫
        </strong>
        <span class="admin-dashboard-kpi__note">Tổng doanh thu từ các đơn hợp lệ.</span>
      </div>

      <div class="admin-dashboard-kpi admin-dashboard-kpi--today">
        <span class="admin-dashboard-kpi__icon">📅</span>
        <span class="admin-dashboard-kpi__label">Doanh thu hôm nay</span>
        <strong class="admin-dashboard-kpi__value admin-dashboard-kpi__value--money">
          <fmt:formatNumber value="${todayRevenue}" type="number" groupingUsed="true"/> ₫
        </strong>
        <span class="admin-dashboard-kpi__note">
          Số đơn hôm nay: <strong><c:out value="${todayOrderCount}"/></strong>
        </span>
      </div>

      <div class="admin-dashboard-kpi admin-dashboard-kpi--aov">
        <span class="admin-dashboard-kpi__icon">🧾</span>
        <span class="admin-dashboard-kpi__label">Giá trị đơn trung bình</span>
        <strong class="admin-dashboard-kpi__value admin-dashboard-kpi__value--money">
          <fmt:formatNumber value="${averageOrderValue}" type="number" groupingUsed="true"/> ₫
        </strong>
        <span class="admin-dashboard-kpi__note">AOV của các đơn đã thanh toán.</span>
      </div>

      <div class="admin-dashboard-kpi admin-dashboard-kpi--users">
        <span class="admin-dashboard-kpi__icon">👥</span>
        <span class="admin-dashboard-kpi__label">Khách hàng</span>
        <strong class="admin-dashboard-kpi__value">
          <c:out value="${userCount}"/>
        </strong>
        <span class="admin-dashboard-kpi__note">Tổng tài khoản người dùng.</span>
      </div>

      <div class="admin-dashboard-kpi admin-dashboard-kpi--products">
        <span class="admin-dashboard-kpi__icon">🧴</span>
        <span class="admin-dashboard-kpi__label">Sản phẩm đang bán</span>
        <strong class="admin-dashboard-kpi__value">
          <c:out value="${productCount}"/>
        </strong>
        <span class="admin-dashboard-kpi__note">Sản phẩm có trạng thái đang hoạt động.</span>
      </div>

      <div class="admin-dashboard-kpi admin-dashboard-kpi--pending">
        <span class="admin-dashboard-kpi__icon">⏳</span>
        <span class="admin-dashboard-kpi__label">Đơn chờ xử lý</span>
        <strong class="admin-dashboard-kpi__value">
          <c:out value="${pendingOrderCount}"/>
        </strong>
        <span class="admin-dashboard-kpi__note">Bao gồm đơn đang xử lý và đã xác nhận.</span>
      </div>

      <div class="admin-dashboard-kpi admin-dashboard-kpi--growth">
        <span class="admin-dashboard-kpi__icon">📈</span>
        <span class="admin-dashboard-kpi__label">Tăng trưởng tháng</span>
        <strong class="admin-dashboard-kpi__value">
          <c:out value="${monthGrowthPercent}"/>%
        </strong>
        <span class="admin-dashboard-kpi__note">So với doanh thu tháng trước.</span>
      </div>
    </section>

    <section class="admin-dashboard-section">
      <div class="admin-dashboard-section__head">
        <div>
          <h2 class="admin-dashboard-section__title">Tổng quan doanh thu</h2>
          <p class="admin-dashboard-section__desc">Theo dõi doanh thu hiện tại, tháng trước và 30 ngày gần nhất.</p>
        </div>
        <span class="admin-chip admin-chip--brand">Doanh thu</span>
      </div>

      <div class="admin-dashboard-mini-grid admin-dashboard-mini-grid--3">
        <div class="admin-dashboard-mini-card">
          <span>Doanh thu tháng này</span>
          <strong><fmt:formatNumber value="${thisMonthRevenue}" type="number" groupingUsed="true"/> ₫</strong>
          <small>Tổng doanh thu trong tháng hiện tại.</small>
        </div>

        <div class="admin-dashboard-mini-card">
          <span>Doanh thu tháng trước</span>
          <strong><fmt:formatNumber value="${prevMonthRevenue}" type="number" groupingUsed="true"/> ₫</strong>
          <small>Dùng để so sánh tăng trưởng tháng.</small>
        </div>

        <div class="admin-dashboard-mini-card">
          <span>Doanh thu 30 ngày gần nhất</span>
          <strong><fmt:formatNumber value="${last30DaysRevenue}" type="number" groupingUsed="true"/> ₫</strong>
          <small>
            Chênh lệch:
            <b><fmt:formatNumber value="${rollingDiffVnd}" type="number" groupingUsed="true"/> ₫</b>
            /
            <b><c:out value="${rollingGrowthPercent}"/>%</b>
          </small>
        </div>
      </div>
    </section>

    <section class="admin-dashboard-section">
      <div class="admin-dashboard-section__head">
        <div>
          <h2 class="admin-dashboard-section__title">Hiệu suất sản phẩm &amp; tồn kho</h2>
          <p class="admin-dashboard-section__desc">Tóm tắt sản phẩm không bán, sắp hết hàng và các cảnh báo tồn kho.</p>
        </div>
        <a class="admin-btn" href="${pageContext.request.contextPath}/admin/inventory">
          Mở quản lý tồn kho
        </a>
      </div>

      <div class="admin-dashboard-mini-grid admin-dashboard-mini-grid--4">
        <div class="admin-dashboard-mini-card admin-dashboard-mini-card--warning">
          <span>Không bán tuần này</span>
          <strong><c:out value="${unsoldThisWeekCount}"/></strong>
          <small>Sản phẩm chưa phát sinh đơn trong tuần hiện tại.</small>
        </div>

        <div class="admin-dashboard-mini-card admin-dashboard-mini-card--warning">
          <span>Không bán tháng này</span>
          <strong><c:out value="${unsoldThisMonthCount}"/></strong>
          <small>Sản phẩm chưa bán được trong tháng hiện tại.</small>
        </div>

        <div class="admin-dashboard-mini-card admin-dashboard-mini-card--danger">
          <span>Sản phẩm hết hàng</span>
          <strong><c:out value="${outOfStockCount}"/></strong>
          <small>Sản phẩm đang active nhưng tồn kho bằng 0.</small>
        </div>

        <div class="admin-dashboard-mini-card admin-dashboard-mini-card--danger">
          <span>Sản phẩm sắp hết</span>
          <strong><c:out value="${lowStockCount}"/></strong>
          <small>Tồn kho còn từ 1 đến 9 sản phẩm (&lt; 10).</small>
        </div>
      </div>

      <div class="admin-dashboard-inventory-card">
        <div class="admin-dashboard-inventory-card__head">
          <div>
            <h3>Dashboard thống kê tồn kho</h3>
            <p>Theo dõi nhanh tình trạng tồn kho và chuyển sang trang quản lý tồn kho khi cần nhập thêm hàng.</p>
          </div>
          <span class="admin-chip admin-chip--brand">Tồn kho</span>
        </div>

        <div class="admin-dashboard-mini-grid admin-dashboard-mini-grid--4">
          <div class="admin-dashboard-stock-box">
            <span>Tổng sản phẩm active</span>
            <strong><c:out value="${productCount}"/></strong>
            <small>Sản phẩm đang kinh doanh.</small>
          </div>

          <div class="admin-dashboard-stock-box admin-dashboard-stock-box--ok">
            <span>Tồn kho ổn định</span>
            <strong><c:out value="${inventoryNormalCount}"/></strong>
            <small>Sản phẩm có tồn kho từ 10 trở lên.</small>
          </div>

          <div class="admin-dashboard-stock-box admin-dashboard-stock-box--warning">
            <span>Sắp hết hàng</span>
            <strong><c:out value="${lowStockCount}"/></strong>
            <small>Tồn kho còn từ 1 đến 9 sản phẩm.</small>
          </div>

          <div class="admin-dashboard-stock-box admin-dashboard-stock-box--danger">
            <span>Cần xử lý</span>
            <strong><c:out value="${inventoryAlertCount}"/></strong>
            <small>Tổng sản phẩm sắp hết và hết hàng.</small>
          </div>
        </div>

        <p class="admin-dashboard-note">
          Gợi ý: vào trang <strong>Quản lý tồn kho</strong> để nhập thêm hàng, xem lịch sử nhập/xuất kho và thống kê số lượng xuất theo ngày, tuần, tháng, năm.
        </p>
      </div>
    </section>

    <section class="admin-dashboard-chart-grid admin-dashboard-chart-grid--main">
      <div class="admin-card admin-dashboard-chart-card">
        <div class="admin-card__body">
          <div class="admin-dashboard-card-head">
            <div>
              <h2>Doanh thu 12 tháng gần nhất</h2>
              <p>Biểu đồ doanh thu theo từng tháng.</p>
            </div>
            <span class="admin-chip">Doanh thu</span>
          </div>
          <canvas id="last12MonthRevenueChart" height="110"></canvas>
        </div>
      </div>

      <div class="admin-card admin-dashboard-chart-card">
        <div class="admin-card__body">
          <div class="admin-dashboard-card-head">
            <div>
              <h2>Trạng thái đơn hàng</h2>
              <p>Tỷ lệ đơn theo trạng thái.</p>
            </div>
            <span class="admin-chip">Đơn hàng</span>
          </div>
          <canvas id="orderStatusChart" height="180"></canvas>
        </div>
      </div>
    </section>

    <section class="admin-card admin-dashboard-chart-card admin-dashboard-mt">
      <div class="admin-card__body">
        <div class="admin-dashboard-card-head">
          <div>
            <h2>Doanh thu 7 ngày gần nhất</h2>
            <p>Theo dõi xu hướng doanh thu ngắn hạn.</p>
          </div>
          <span class="admin-chip">7 ngày</span>
        </div>
        <canvas id="last7DaysRevenueChart" height="85"></canvas>
      </div>
    </section>

    <section class="admin-dashboard-chart-grid admin-dashboard-mt">
      <div class="admin-card admin-dashboard-chart-card">
        <div class="admin-card__body">
          <div class="admin-dashboard-card-head">
            <div>
              <h2>Hiệu suất sản phẩm tháng này</h2>
              <p>So sánh sản phẩm có bán và không bán trong tháng.</p>
            </div>
            <span class="admin-chip">Sản phẩm</span>
          </div>
          <canvas id="productPerformanceChart" height="120"></canvas>
        </div>
      </div>

      <div class="admin-card admin-dashboard-chart-card">
        <div class="admin-card__body">
          <div class="admin-dashboard-card-head">
            <div>
              <h2>Tình trạng tồn kho</h2>
              <p>Phân nhóm hết hàng, sắp hết và còn hàng.</p>
            </div>
            <span class="admin-chip">Tồn kho</span>
          </div>
          <canvas id="stockStatusChart" height="120"></canvas>
        </div>
      </div>
    </section>

    <section class="admin-card admin-dashboard-chart-card admin-dashboard-mt">
      <div class="admin-card__body">
        <div class="admin-dashboard-card-head">
          <div>
            <h2>Số lượng bán theo danh mục trong 30 ngày</h2>
            <p>Giúp nhận biết nhóm sản phẩm đang bán tốt.</p>
          </div>
          <span class="admin-chip">Danh mục</span>
        </div>
        <canvas id="categorySoldChart" height="90"></canvas>
      </div>
    </section>

    <section class="admin-dashboard-table-grid admin-dashboard-mt">
      <div class="admin-card admin-dashboard-table-card">
        <div class="admin-card__body">
          <div class="admin-dashboard-card-head">
            <div>
              <h2>Top sản phẩm bán chạy</h2>
              <p>Top 5 sản phẩm theo số lượng bán.</p>
            </div>
            <span class="admin-chip">Top 5</span>
          </div>

          <div class="admin-table-wrap admin-dashboard-table-wrap">
            <table class="admin-table admin-dashboard-table">
              <thead>
              <tr>
                <th>Sản phẩm</th>
                <th class="admin-dashboard-number-cell">Đã bán</th>
                <th class="admin-dashboard-number-cell">Doanh thu</th>
              </tr>
              </thead>
              <tbody>
              <c:choose>
                <c:when test="${not empty topProducts}">
                  <c:forEach var="product" items="${topProducts}">
                    <tr>
                      <td>
                        <strong><c:out value="${product[1]}"/></strong>
                        <div class="admin-subtext">ID: <c:out value="${product[0]}"/></div>
                      </td>
                      <td class="admin-dashboard-number-cell">
                        <c:out value="${product[2]}"/>
                      </td>
                      <td class="admin-dashboard-number-cell">
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

      <div class="admin-card admin-dashboard-table-card">
        <div class="admin-card__body">
          <div class="admin-dashboard-card-head">
            <div>
              <h2>Đơn hàng gần đây</h2>
              <p>5 đơn hàng mới nhất trong hệ thống.</p>
            </div>
            <span class="admin-chip">Gần đây</span>
          </div>

          <div class="admin-table-wrap admin-dashboard-table-wrap">
            <table class="admin-table admin-dashboard-table">
              <thead>
              <tr>
                <th>Mã đơn</th>
                <th>Khách hàng</th>
                <th class="admin-dashboard-number-cell">Tổng tiền</th>
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
                      <td class="admin-dashboard-number-cell">
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
    </section>

    <section class="admin-dashboard-table-grid admin-dashboard-mt">
      <div class="admin-card admin-dashboard-table-card">
        <div class="admin-card__body">
          <div class="admin-dashboard-card-head">
            <div>
              <h2>Sản phẩm không bán được tháng này</h2>
              <p>Top sản phẩm còn tồn nhưng không phát sinh bán trong tháng.</p>
            </div>
            <span class="admin-chip">Không bán</span>
          </div>

          <div class="admin-table-wrap admin-dashboard-table-wrap">
            <table class="admin-table admin-dashboard-table">
              <thead>
              <tr>
                <th>Sản phẩm</th>
                <th>Danh mục</th>
                <th class="admin-dashboard-number-cell">Tồn kho</th>
                <th class="admin-dashboard-number-cell">Giá</th>
              </tr>
              </thead>

              <tbody>
              <c:choose>
                <c:when test="${not empty unsoldProductsThisMonth}">
                  <c:forEach var="product" items="${unsoldProductsThisMonth}">
                    <tr>
                      <td>
                        <strong><c:out value="${product[1]}"/></strong>
                        <div class="admin-subtext">ID: <c:out value="${product[0]}"/></div>
                      </td>
                      <td><c:out value="${product[4]}"/></td>
                      <td class="admin-dashboard-number-cell"><c:out value="${product[2]}"/></td>
                      <td class="admin-dashboard-number-cell">
                        <fmt:formatNumber value="${product[3]}" type="number" groupingUsed="true"/> ₫
                      </td>
                    </tr>
                  </c:forEach>
                </c:when>

                <c:otherwise>
                  <tr>
                    <td colspan="4" class="admin-subtext">Không có sản phẩm không bán trong tháng này.</td>
                  </tr>
                </c:otherwise>
              </c:choose>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="admin-card admin-dashboard-table-card">
        <div class="admin-card__body">
          <div class="admin-dashboard-card-head">
            <div>
              <h2>Sản phẩm cần nhập thêm</h2>
              <p>Sản phẩm hết hàng hoặc tồn kho thấp.</p>
            </div>
            <span class="admin-chip">Tồn kho</span>
          </div>

          <div class="admin-table-wrap admin-dashboard-table-wrap">
            <table class="admin-table admin-dashboard-table">
              <thead>
              <tr>
                <th>Sản phẩm</th>
                <th>Danh mục</th>
                <th class="admin-dashboard-number-cell">Tồn kho</th>
                <th class="admin-dashboard-number-cell">Giá</th>
              </tr>
              </thead>

              <tbody>
              <c:choose>
                <c:when test="${not empty lowStockProducts}">
                  <c:forEach var="product" items="${lowStockProducts}">
                    <tr>
                      <td>
                        <strong><c:out value="${product[1]}"/></strong>
                        <div class="admin-subtext">ID: <c:out value="${product[0]}"/></div>
                      </td>
                      <td><c:out value="${product[4]}"/></td>
                      <td class="admin-dashboard-number-cell">
                        <c:choose>
                          <c:when test="${product[2] == 0}">
                            <span class="admin-chip">Hết hàng</span>
                          </c:when>
                          <c:otherwise>
                            <c:out value="${product[2]}"/>
                          </c:otherwise>
                        </c:choose>
                      </td>
                      <td class="admin-dashboard-number-cell">
                        <fmt:formatNumber value="${product[3]}" type="number" groupingUsed="true"/> ₫
                      </td>
                    </tr>
                  </c:forEach>
                </c:when>

                <c:otherwise>
                  <tr>
                    <td colspan="4" class="admin-subtext">Không có sản phẩm tồn kho thấp.</td>
                  </tr>
                </c:otherwise>
              </c:choose>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>

    <section class="admin-card admin-dashboard-table-card admin-dashboard-mt">
      <div class="admin-card__body">
        <div class="admin-dashboard-card-head">
          <div>
            <h2>Sản phẩm bán chậm trong 30 ngày</h2>
            <p>Sản phẩm bán từ 0 đến 2 sản phẩm trong 30 ngày gần nhất.</p>
          </div>
          <span class="admin-chip">Bán chậm</span>
        </div>

        <div class="admin-table-wrap admin-dashboard-table-wrap">
          <table class="admin-table admin-dashboard-table">
            <thead>
            <tr>
              <th>Sản phẩm</th>
              <th class="admin-dashboard-number-cell">Tồn kho</th>
              <th class="admin-dashboard-number-cell">Đã bán</th>
              <th class="admin-dashboard-number-cell">Doanh thu</th>
            </tr>
            </thead>

            <tbody>
            <c:choose>
              <c:when test="${not empty slowMovingProducts}">
                <c:forEach var="product" items="${slowMovingProducts}">
                  <tr>
                    <td>
                      <strong><c:out value="${product[1]}"/></strong>
                      <div class="admin-subtext">ID: <c:out value="${product[0]}"/></div>
                    </td>
                    <td class="admin-dashboard-number-cell"><c:out value="${product[2]}"/></td>
                    <td class="admin-dashboard-number-cell"><c:out value="${product[3]}"/></td>
                    <td class="admin-dashboard-number-cell">
                      <fmt:formatNumber value="${product[4]}" type="number" groupingUsed="true"/> ₫
                    </td>
                  </tr>
                </c:forEach>
              </c:when>

              <c:otherwise>
                <tr>
                  <td colspan="4" class="admin-subtext">Không có sản phẩm bán chậm.</td>
                </tr>
              </c:otherwise>
            </c:choose>
            </tbody>
          </table>
        </div>
      </div>
    </section>

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

  const productPerformanceLabels =
          <c:out value="${empty productPerformanceLabelsJson ? '[]' : productPerformanceLabelsJson}" escapeXml="false"/>;

  const productPerformanceValues =
          <c:out value="${empty productPerformanceValuesJson ? '[]' : productPerformanceValuesJson}" escapeXml="false"/>;

  const stockStatusLabels =
          <c:out value="${empty stockStatusLabelsJson ? '[]' : stockStatusLabelsJson}" escapeXml="false"/>;

  const stockStatusValues =
          <c:out value="${empty stockStatusValuesJson ? '[]' : stockStatusValuesJson}" escapeXml="false"/>;

  const categorySoldLabels =
          <c:out value="${empty categorySoldLabelsJson ? '[]' : categorySoldLabelsJson}" escapeXml="false"/>;

  const categorySoldValues =
          <c:out value="${empty categorySoldValuesJson ? '[]' : categorySoldValuesJson}" escapeXml="false"/>;

  const categoryRevenueValues =
          <c:out value="${empty categoryRevenueValuesJson ? '[]' : categoryRevenueValuesJson}" escapeXml="false"/>;

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

  const productPerformanceChart = document.getElementById('productPerformanceChart');

  if (productPerformanceChart) {
    new Chart(productPerformanceChart, {
      type: 'bar',
      data: {
        labels: productPerformanceLabels,
        datasets: [{
          label: 'Số sản phẩm',
          data: productPerformanceValues,
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            display: false
          }
        },
        scales: {
          y: {
            beginAtZero: true
          }
        }
      }
    });
  }

  const stockStatusChart = document.getElementById('stockStatusChart');

  if (stockStatusChart) {
    new Chart(stockStatusChart, {
      type: 'doughnut',
      data: {
        labels: stockStatusLabels,
        datasets: [{
          label: 'Sản phẩm',
          data: stockStatusValues,
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

  const categorySoldChart = document.getElementById('categorySoldChart');

  if (categorySoldChart) {
    new Chart(categorySoldChart, {
      type: 'bar',
      data: {
        labels: categorySoldLabels,
        datasets: [{
          label: 'Số lượng bán',
          data: categorySoldValues,
          borderWidth: 1
        }]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            callbacks: {
              afterLabel: function(context) {
                const revenue = categoryRevenueValues[context.dataIndex] || 0;
                return 'Doanh thu: ' + formatVnd(revenue);
              }
            }
          }
        },
        scales: {
          x: {
            beginAtZero: true
          }
        }
      }
    });
  }
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

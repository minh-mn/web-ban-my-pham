<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Admin - Quản lý tồn kho" scope="request"/>
<c:set var="activeMenu" value="inventory" scope="request"/>

<c:set var="csrfValue" value="${csrfToken}" />
<c:if test="${empty csrfValue}">
    <c:set var="csrfValue" value="${csrf_token}" />
</c:if>
<c:if test="${empty csrfValue}">
    <c:set var="csrfValue" value="${sessionScope.csrfToken}" />
</c:if>
<c:if test="${empty csrfValue}">
    <c:set var="csrfValue" value="${sessionScope.csrf_token}" />
</c:if>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<style>
    .inventory-page {
        padding: 24px;
        background: #f7f8fb;
        min-height: 100vh;
    }

    .inventory-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: 16px;
        margin-bottom: 22px;
    }

    .inventory-title h1 {
        margin: 0 0 6px;
        font-size: 28px;
        font-weight: 800;
        color: #1f2937;
    }

    .inventory-title p {
        margin: 0;
        color: #6b7280;
        font-size: 14px;
    }

    .inventory-badge {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        padding: 10px 14px;
        border-radius: 999px;
        background: #fff7ed;
        color: #c2410c;
        font-weight: 700;
        border: 1px solid #fed7aa;
        white-space: nowrap;
    }

    .inventory-alert {
        margin-bottom: 16px;
        padding: 13px 16px;
        border-radius: 14px;
        font-size: 14px;
        font-weight: 600;
    }

    .inventory-alert.success {
        background: #ecfdf5;
        color: #047857;
        border: 1px solid #a7f3d0;
    }

    .inventory-alert.error {
        background: #fef2f2;
        color: #b91c1c;
        border: 1px solid #fecaca;
    }

    .inventory-summary-grid {
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
        gap: 16px;
        margin-bottom: 20px;
    }

    .inventory-card {
        background: #ffffff;
        border-radius: 18px;
        padding: 18px;
        border: 1px solid #eef0f4;
        box-shadow: 0 10px 24px rgba(15, 23, 42, 0.05);
    }

    .inventory-card-label {
        color: #6b7280;
        font-size: 13px;
        margin-bottom: 8px;
        font-weight: 600;
    }

    .inventory-card-value {
        color: #111827;
        font-size: 28px;
        font-weight: 800;
        line-height: 1.1;
    }

    .inventory-card-note {
        margin-top: 8px;
        color: #9ca3af;
        font-size: 12px;
    }

    .inventory-card.danger .inventory-card-value {
        color: #dc2626;
    }

    .inventory-card.warning .inventory-card-value {
        color: #d97706;
    }

    .inventory-card.success .inventory-card-value {
        color: #059669;
    }

    .inventory-section-grid {
        display: grid;
        grid-template-columns: 1.35fr 0.65fr;
        gap: 16px;
        margin-bottom: 20px;
    }

    .inventory-panel {
        background: #ffffff;
        border-radius: 18px;
        border: 1px solid #eef0f4;
        box-shadow: 0 10px 24px rgba(15, 23, 42, 0.05);
        overflow: hidden;
    }

    .inventory-panel-header {
        padding: 18px 20px;
        border-bottom: 1px solid #eef0f4;
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 12px;
    }

    .inventory-panel-header h2 {
        margin: 0;
        font-size: 18px;
        color: #1f2937;
        font-weight: 800;
    }

    .inventory-panel-header span {
        color: #6b7280;
        font-size: 13px;
    }

    .inventory-panel-body {
        padding: 18px 20px;
    }

    .export-grid {
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
        gap: 14px;
    }

    .export-box {
        border-radius: 16px;
        padding: 16px;
        background: #f9fafb;
        border: 1px solid #eef0f4;
    }

    .export-box strong {
        display: block;
        font-size: 24px;
        color: #111827;
        margin-bottom: 5px;
    }

    .export-box span {
        font-size: 13px;
        color: #6b7280;
    }

    .chart-wrap {
        margin-top: 18px;
        height: 250px;
    }

    #inventoryExportChart {
        width: 100%;
        height: 250px;
    }

    .low-stock-list {
        display: flex;
        flex-direction: column;
        gap: 10px;
        max-height: 335px;
        overflow: auto;
    }

    .low-stock-item {
        padding: 12px;
        border-radius: 14px;
        background: #f9fafb;
        border: 1px solid #eef0f4;
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 12px;
    }

    .low-stock-item-title {
        font-size: 14px;
        color: #111827;
        font-weight: 700;
        margin-bottom: 4px;
    }

    .low-stock-item-meta {
        font-size: 12px;
        color: #6b7280;
    }

    .low-stock-number {
        min-width: 46px;
        text-align: center;
        border-radius: 12px;
        padding: 8px 10px;
        font-weight: 800;
        background: #fee2e2;
        color: #b91c1c;
    }

    .inventory-filter {
        display: flex;
        align-items: center;
        gap: 10px;
        flex-wrap: wrap;
    }

    .inventory-input,
    .inventory-select {
        height: 40px;
        border: 1px solid #d1d5db;
        border-radius: 12px;
        padding: 0 12px;
        outline: none;
        background: #ffffff;
        color: #111827;
    }

    .inventory-input {
        width: 260px;
    }

    .inventory-input:focus,
    .inventory-select:focus {
        border-color: #f472b6;
        box-shadow: 0 0 0 3px rgba(244, 114, 182, 0.15);
    }

    .inventory-btn {
        height: 40px;
        border: none;
        border-radius: 12px;
        padding: 0 14px;
        background: #ec4899;
        color: #ffffff;
        font-weight: 700;
        cursor: pointer;
        transition: 0.2s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
    }

    .inventory-btn:hover {
        background: #db2777;
        color: #ffffff;
        text-decoration: none;
    }

    .inventory-btn.secondary {
        background: #f3f4f6;
        color: #374151;
    }

    .inventory-btn.secondary:hover {
        background: #e5e7eb;
        color: #111827;
    }

    .inventory-table-wrap {
        width: 100%;
        overflow-x: auto;
    }

    .inventory-table {
        width: 100%;
        border-collapse: collapse;
        min-width: 1180px;
    }

    .inventory-table th {
        background: #f9fafb;
        color: #4b5563;
        text-align: left;
        font-size: 12px;
        text-transform: uppercase;
        letter-spacing: 0.04em;
        padding: 14px 16px;
        border-bottom: 1px solid #eef0f4;
        white-space: nowrap;
    }

    .inventory-table td {
        padding: 14px 16px;
        border-bottom: 1px solid #f1f5f9;
        color: #374151;
        vertical-align: middle;
        font-size: 14px;
    }

    .inventory-table tbody tr:hover {
        background: #fff7fb;
    }

    .product-name {
        font-weight: 800;
        color: #111827;
        margin-bottom: 4px;
    }

    .product-meta {
        color: #6b7280;
        font-size: 12px;
    }

    .stock-pill {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        border-radius: 999px;
        padding: 7px 12px;
        font-size: 12px;
        font-weight: 800;
        white-space: nowrap;
    }

    .stock-normal {
        background: #dcfce7;
        color: #047857;
    }

    .stock-low {
        background: #fef3c7;
        color: #b45309;
    }

    .stock-out {
        background: #fee2e2;
        color: #b91c1c;
    }

    .stock-number {
        font-size: 20px;
        font-weight: 900;
        color: #111827;
    }

    .add-stock-form {
        display: grid;
        grid-template-columns: 80px 150px 80px;
        gap: 8px;
        align-items: center;
    }

    .add-stock-form input {
        height: 36px;
        border: 1px solid #d1d5db;
        border-radius: 10px;
        padding: 0 10px;
        outline: none;
    }

    .add-stock-form input:focus {
        border-color: #f472b6;
        box-shadow: 0 0 0 3px rgba(244, 114, 182, 0.15);
    }

    .add-stock-form button {
        height: 36px;
        border: none;
        border-radius: 10px;
        background: #111827;
        color: #ffffff;
        font-weight: 700;
        cursor: pointer;
    }

    .add-stock-form button:hover {
        background: #374151;
    }

    .movement-list {
        display: flex;
        flex-direction: column;
        gap: 10px;
    }

    .movement-item {
        display: grid;
        grid-template-columns: 90px 1fr 90px 130px;
        gap: 12px;
        align-items: center;
        padding: 13px 0;
        border-bottom: 1px solid #f1f5f9;
    }

    .movement-item:last-child {
        border-bottom: none;
    }

    .movement-type {
        border-radius: 999px;
        padding: 7px 10px;
        font-size: 12px;
        font-weight: 800;
        text-align: center;
        white-space: nowrap;
    }

    .movement-in {
        background: #dcfce7;
        color: #047857;
    }

    .movement-out {
        background: #fee2e2;
        color: #b91c1c;
    }

    .movement-adjust {
        background: #e0f2fe;
        color: #0369a1;
    }

    .movement-other {
        background: #f3f4f6;
        color: #374151;
    }

    .movement-title {
        font-weight: 800;
        color: #111827;
        margin-bottom: 3px;
    }

    .movement-note {
        font-size: 12px;
        color: #6b7280;
    }

    .movement-qty {
        font-weight: 900;
        color: #111827;
        text-align: right;
    }

    .movement-time {
        color: #6b7280;
        font-size: 12px;
        text-align: right;
    }

    .empty-state {
        padding: 30px 20px;
        text-align: center;
        color: #6b7280;
    }

    @media (max-width: 1200px) {
        .inventory-summary-grid,
        .export-grid {
            grid-template-columns: repeat(2, minmax(0, 1fr));
        }

        .inventory-section-grid {
            grid-template-columns: 1fr;
        }
    }

    @media (max-width: 768px) {
        .inventory-page {
            padding: 16px;
        }

        .inventory-header {
            flex-direction: column;
        }

        .inventory-summary-grid,
        .export-grid {
            grid-template-columns: 1fr;
        }

        .inventory-input {
            width: 100%;
        }

        .inventory-filter {
            width: 100%;
        }

        .inventory-filter .inventory-btn,
        .inventory-filter .inventory-select {
            width: 100%;
        }

        .movement-item {
            grid-template-columns: 1fr;
        }

        .movement-qty,
        .movement-time {
            text-align: left;
        }
    }
</style>

<main class="admin-main">
    <div class="inventory-page">

        <div class="inventory-header">
            <div class="inventory-title">
                <h1>Quản lý tồn kho</h1>
                <p>Theo dõi tồn kho, cảnh báo sắp hết hàng và thống kê số lượng xuất theo ngày, tuần, tháng, năm.</p>
            </div>

            <div class="inventory-badge">
                Cảnh báo:
                <fmt:formatNumber value="${summary.alertCount}" type="number"/>
                sản phẩm
            </div>
        </div>

        <c:if test="${param.success == 'stock_added'}">
            <div class="inventory-alert success">
                Đã nhập thêm tồn kho thành công.
            </div>
        </c:if>

        <c:if test="${not empty param.error}">
            <div class="inventory-alert error">
                <c:out value="${param.error}"/>
            </div>
        </c:if>

        <div class="inventory-summary-grid">
            <div class="inventory-card">
                <div class="inventory-card-label">Tổng sản phẩm active</div>
                <div class="inventory-card-value">
                    <fmt:formatNumber value="${summary.productCount}" type="number"/>
                </div>
                <div class="inventory-card-note">Sản phẩm đang kinh doanh</div>
            </div>

            <div class="inventory-card success">
                <div class="inventory-card-label">Tổng tồn kho</div>
                <div class="inventory-card-value">
                    <fmt:formatNumber value="${summary.totalStock}" type="number"/>
                </div>
                <div class="inventory-card-note">Tổng số lượng còn lại</div>
            </div>

            <div class="inventory-card warning">
                <div class="inventory-card-label">Sắp hết hàng</div>
                <div class="inventory-card-value">
                    <fmt:formatNumber value="${summary.lowStockCount}" type="number"/>
                </div>
                <div class="inventory-card-note">Tồn kho lớn hơn 0 và nhỏ hơn 10</div>
            </div>

            <div class="inventory-card danger">
                <div class="inventory-card-label">Hết hàng</div>
                <div class="inventory-card-value">
                    <fmt:formatNumber value="${summary.outOfStockCount}" type="number"/>
                </div>
                <div class="inventory-card-note">Cần nhập hàng ngay</div>
            </div>
        </div>

        <div class="inventory-section-grid">
            <section class="inventory-panel">
                <div class="inventory-panel-header">
                    <div>
                        <h2>Dashboard thống kê xuất kho</h2>
                        <span>Số lượng sản phẩm đã xuất theo từng mốc thời gian</span>
                    </div>
                </div>

                <div class="inventory-panel-body">
                    <div class="export-grid">
                        <div class="export-box">
                            <strong>
                                <fmt:formatNumber value="${summary.exportedToday}" type="number"/>
                            </strong>
                            <span>Xuất hôm nay</span>
                        </div>

                        <div class="export-box">
                            <strong>
                                <fmt:formatNumber value="${summary.exportedThisWeek}" type="number"/>
                            </strong>
                            <span>Xuất tuần này</span>
                        </div>

                        <div class="export-box">
                            <strong>
                                <fmt:formatNumber value="${summary.exportedThisMonth}" type="number"/>
                            </strong>
                            <span>Xuất tháng này</span>
                        </div>

                        <div class="export-box">
                            <strong>
                                <fmt:formatNumber value="${summary.exportedThisYear}" type="number"/>
                            </strong>
                            <span>Xuất năm nay</span>
                        </div>
                    </div>

                    <div class="chart-wrap">
                        <canvas id="inventoryExportChart"></canvas>
                    </div>
                </div>
            </section>

            <section class="inventory-panel">
                <div class="inventory-panel-header">
                    <div>
                        <h2>Cảnh báo tồn kho</h2>
                        <span>Sản phẩm hết hàng hoặc gần hết hàng</span>
                    </div>
                </div>

                <div class="inventory-panel-body">
                    <c:choose>
                        <c:when test="${not empty lowStockAlerts}">
                            <div class="low-stock-list">
                                <c:forEach var="item" items="${lowStockAlerts}">
                                    <div class="low-stock-item">
                                        <div>
                                            <div class="low-stock-item-title">
                                                <c:out value="${item.displayTitle}"/>
                                            </div>
                                            <div class="low-stock-item-meta">
                                                <c:out value="${item.displayBrandName}"/>
                                                ·
                                                <c:out value="${item.stockStatusLabel}"/>
                                            </div>
                                        </div>
                                        <div class="low-stock-number">
                                            <fmt:formatNumber value="${item.stock}" type="number"/>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:when>

                        <c:otherwise>
                            <div class="empty-state">
                                Hiện chưa có sản phẩm nào sắp hết hàng hoặc hết hàng.
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>
        </div>

        <section class="inventory-panel">
            <div class="inventory-panel-header">
                <div>
                    <h2>Danh sách tồn kho sản phẩm</h2>
                    <span>Quản lý số lượng tồn và nhập thêm hàng</span>
                </div>

                <form class="inventory-filter" method="get" action="${pageContext.request.contextPath}/admin/inventory">
                    <input
                            class="inventory-input"
                            type="text"
                            name="keyword"
                            value="<c:out value='${keyword}'/>"
                            placeholder="Tìm sản phẩm, danh mục, thương hiệu">

                    <select class="inventory-select" name="status">
                        <option value="" ${empty status ? 'selected' : ''}>Tất cả trạng thái</option>
                        <option value="normal" ${status == 'normal' ? 'selected' : ''}>Còn hàng</option>
                        <option value="low" ${status == 'low' ? 'selected' : ''}>Sắp hết hàng</option>
                        <option value="out" ${status == 'out' ? 'selected' : ''}>Hết hàng</option>
                    </select>

                    <button class="inventory-btn" type="submit">Lọc</button>

                    <a class="inventory-btn secondary" href="${pageContext.request.contextPath}/admin/inventory">
                        Làm mới
                    </a>
                </form>
            </div>

            <div class="inventory-table-wrap">
                <table class="inventory-table">
                    <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th>Giá bán</th>
                        <th>Tồn kho</th>
                        <th>Trạng thái</th>
                        <th>Xuất ngày</th>
                        <th>Xuất tuần</th>
                        <th>Xuất tháng</th>
                        <th>Xuất năm</th>
                        <th>Nhập thêm</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:choose>
                        <c:when test="${not empty products}">
                            <c:forEach var="product" items="${products}">
                                <tr>
                                    <td>
                                        <div class="product-name">
                                            <c:out value="${product.displayTitle}"/>
                                        </div>
                                        <div class="product-meta">
                                            <c:out value="${product.displayCategoryName}"/>
                                            ·
                                            <c:out value="${product.displayBrandName}"/>
                                        </div>
                                    </td>

                                    <td>
                                        <c:out value="${product.formattedPrice}"/>
                                    </td>

                                    <td>
                                        <span class="stock-number">
                                            <fmt:formatNumber value="${product.stock}" type="number"/>
                                        </span>
                                    </td>

                                    <td>
                                        <span class="stock-pill ${product.stockStatusClass}">
                                            <c:out value="${product.stockStatusLabel}"/>
                                        </span>
                                    </td>

                                    <td>
                                        <fmt:formatNumber value="${product.exportedToday}" type="number"/>
                                    </td>

                                    <td>
                                        <fmt:formatNumber value="${product.exportedThisWeek}" type="number"/>
                                    </td>

                                    <td>
                                        <fmt:formatNumber value="${product.exportedThisMonth}" type="number"/>
                                    </td>

                                    <td>
                                        <fmt:formatNumber value="${product.exportedThisYear}" type="number"/>
                                    </td>

                                    <td>
                                        <form class="add-stock-form"
                                              method="post"
                                              action="${pageContext.request.contextPath}/admin/inventory">
                                            <input type="hidden" name="action" value="addStock">
                                            <input type="hidden" name="productId" value="${product.id}">
                                            <input type="hidden" name="csrf_token" value="${csrfValue}">
                                            <input type="hidden" name="csrfToken" value="${csrfValue}">

                                            <input type="number"
                                                   name="quantity"
                                                   min="1"
                                                   step="1"
                                                   placeholder="SL"
                                                   required>

                                            <input type="text"
                                                   name="note"
                                                   maxlength="255"
                                                   placeholder="Ghi chú">

                                            <button type="submit">Nhập</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>

                        <c:otherwise>
                            <tr>
                                <td colspan="9">
                                    <div class="empty-state">
                                        Không tìm thấy sản phẩm phù hợp với bộ lọc hiện tại.
                                    </div>
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="inventory-panel" style="margin-top: 20px;">
            <div class="inventory-panel-header">
                <div>
                    <h2>Lịch sử nhập/xuất kho gần đây</h2>
                    <span>Theo dõi hoạt động tồn kho mới nhất</span>
                </div>
            </div>

            <div class="inventory-panel-body">
                <c:choose>
                    <c:when test="${not empty recentActivities}">
                        <div class="movement-list">
                            <c:forEach var="movement" items="${recentActivities}">
                                <div class="movement-item">
                                    <div class="movement-type ${movement.movementTypeClass}">
                                        <c:out value="${movement.movementTypeLabel}"/>
                                    </div>

                                    <div>
                                        <div class="movement-title">
                                            <c:out value="${movement.displayProductTitle}"/>
                                        </div>
                                        <div class="movement-note">
                                            <c:out value="${movement.displayNote}"/>
                                            ·
                                            <c:out value="${movement.displayCreatedByName}"/>
                                        </div>
                                    </div>

                                    <div class="movement-qty">
                                        <c:out value="${movement.quantityText}"/>
                                    </div>

                                    <div class="movement-time">
                                        <div>
                                            <c:out value="${movement.formattedCreatedAt}"/>
                                        </div>
                                        <div>
                                            Tồn:
                                            <c:out value="${movement.stockChangeText}"/>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="empty-state">
                            Chưa có hoạt động nhập/xuất kho nào.
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

    </div>
</main>

<script>
    (function () {
        const labels = ${empty last7DaysExportLabelsJson ? "[]" : last7DaysExportLabelsJson};
        const values = ${empty last7DaysExportValuesJson ? "[]" : last7DaysExportValuesJson};

        const canvas = document.getElementById("inventoryExportChart");
        if (!canvas) {
            return;
        }

        const ctx = canvas.getContext("2d");
        const dpr = window.devicePixelRatio || 1;
        const rect = canvas.getBoundingClientRect();

        canvas.width = rect.width * dpr;
        canvas.height = rect.height * dpr;
        ctx.scale(dpr, dpr);

        const width = rect.width;
        const height = rect.height;
        const paddingLeft = 42;
        const paddingRight = 18;
        const paddingTop = 18;
        const paddingBottom = 42;
        const chartWidth = width - paddingLeft - paddingRight;
        const chartHeight = height - paddingTop - paddingBottom;

        ctx.clearRect(0, 0, width, height);

        const maxValue = Math.max.apply(null, values.concat([1]));
        const barGap = 12;
        const barWidth = values.length > 0
            ? Math.max(18, (chartWidth - barGap * (values.length - 1)) / values.length)
            : 18;

        ctx.font = "12px Arial";
        ctx.fillStyle = "#6b7280";
        ctx.strokeStyle = "#e5e7eb";
        ctx.lineWidth = 1;

        for (let i = 0; i <= 4; i++) {
            const y = paddingTop + chartHeight - (chartHeight * i / 4);
            ctx.beginPath();
            ctx.moveTo(paddingLeft, y);
            ctx.lineTo(width - paddingRight, y);
            ctx.stroke();

            const label = Math.round(maxValue * i / 4).toString();
            ctx.fillText(label, 8, y + 4);
        }

        values.forEach(function (value, index) {
            const barHeight = Math.round((value / maxValue) * chartHeight);
            const x = paddingLeft + index * (barWidth + barGap);
            const y = paddingTop + chartHeight - barHeight;

            const gradient = ctx.createLinearGradient(0, y, 0, paddingTop + chartHeight);
            gradient.addColorStop(0, "#ec4899");
            gradient.addColorStop(1, "#f9a8d4");

            ctx.fillStyle = gradient;
            roundRect(ctx, x, y, barWidth, barHeight, 8);
            ctx.fill();

            ctx.fillStyle = "#111827";
            ctx.font = "bold 12px Arial";
            ctx.textAlign = "center";
            ctx.fillText(value.toString(), x + barWidth / 2, y - 6);

            ctx.fillStyle = "#6b7280";
            ctx.font = "11px Arial";
            const label = labels[index] ? labels[index].substring(5) : "";
            ctx.fillText(label, x + barWidth / 2, height - 14);
        });

        function roundRect(ctx, x, y, width, height, radius) {
            const safeRadius = Math.min(radius, width / 2, height / 2);

            ctx.beginPath();
            ctx.moveTo(x + safeRadius, y);
            ctx.lineTo(x + width - safeRadius, y);
            ctx.quadraticCurveTo(x + width, y, x + width, y + safeRadius);
            ctx.lineTo(x + width, y + height);
            ctx.lineTo(x, y + height);
            ctx.lineTo(x, y + safeRadius);
            ctx.quadraticCurveTo(x, y, x + safeRadius, y);
            ctx.closePath();
        }
    })();
</script>
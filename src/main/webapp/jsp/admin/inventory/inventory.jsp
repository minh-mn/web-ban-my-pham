<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Admin - Quản lý tồn kho" scope="request"/>
<c:set var="activeMenu" value="inventory" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<c:set var="csrfValue" value="${csrfToken}" />
<c:if test="${empty csrfValue}">
    <c:set var="csrfValue" value="${csrf_token}" />
</c:if>
<c:if test="${empty csrfValue}">
    <c:set var="csrfValue" value="${requestScope.CSRF_TOKEN}" />
</c:if>
<c:if test="${empty csrfValue}">
    <c:set var="csrfValue" value="${sessionScope.CSRF_TOKEN}" />
</c:if>
<c:if test="${empty csrfValue}">
    <c:set var="csrfValue" value="${sessionScope.csrfToken}" />
</c:if>
<c:if test="${empty csrfValue}">
    <c:set var="csrfValue" value="${sessionScope.csrf_token}" />
</c:if>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
    <div class="admin-container inventory-page admin-inventory-page">

        <section class="admin-inventory-hero">
            <div class="admin-inventory-hero__content">
                <span class="admin-inventory-eyebrow">SẢN PHẨM &amp; KHO HÀNG</span>
                <h1 class="admin-inventory-title">Quản lý tồn kho</h1>
                <p class="admin-inventory-subtitle">
                    Theo dõi tồn kho sản phẩm, tồn kho biến thể/SKU, cảnh báo sắp hết hàng,
                    thống kê xuất kho và lịch sử nhập hàng theo tháng/năm.
                </p>
            </div>

            <div class="admin-inventory-hero__actions">
                <a class="admin-btn" href="#productInventory">Kho sản phẩm</a>
                <a class="admin-btn" href="#variantInventory">Kho SKU</a>
                <a class="admin-btn admin-btn--primary" href="#inventoryImportHistory">Lịch sử nhập hàng</a>
            </div>
        </section>

        <section class="admin-inventory-alert-strip">
            <span class="admin-inventory-alert-strip__icon">⚠️</span>
            <div>
                <strong>Cảnh báo tồn kho</strong>
                <span>
                    Có <span data-inventory-summary-value="alertCount"><fmt:formatNumber value="${summary.alertCount}" type="number"/></span> sản phẩm cần kiểm tra hoặc nhập thêm.
                </span>
            </div>
        </section>

        <c:if test="${param.success == 'stock_added'}">
            <div class="admin-alert admin-alert--success">
                Đã nhập thêm tồn kho thành công.
            </div>
        </c:if>

        <c:if test="${param.success == 'variant_stock_added'}">
            <div class="admin-alert admin-alert--success">
                Đã nhập thêm tồn kho cho biến thể thành công.
            </div>
        </c:if>

        <c:if test="${param.success == 'variant_min_stock_updated'}">
            <div class="admin-alert admin-alert--success">
                Đã cập nhật mức cảnh báo tồn kho cho biến thể.
            </div>
        </c:if>

        <c:if test="${not empty param.error}">
            <div class="admin-alert admin-alert--danger">
                <c:out value="${param.error}"/>
            </div>
        </c:if>

        <div id="inventoryAjaxMessage" class="admin-alert inventory-ajax-message" style="display:none"></div>

        <section class="admin-inventory-group">
            <div class="admin-inventory-group__head">
                <div>
                    <h2>Tổng quan tồn kho sản phẩm</h2>
                    <p>Theo dõi tổng số sản phẩm, lượng tồn kho và cảnh báo cần xử lý.</p>
                </div>
                <span class="admin-chip admin-chip--brand">Product Stock</span>
            </div>

            <div class="inventory-summary-grid">
            <div class="admin-card inventory-stat-card inventory-stat-card--product">
                <span class="inventory-card-icon">🧴</span>
                <div class="inventory-card-label">Tổng sản phẩm active</div>
                <div class="inventory-card-value" data-inventory-summary-value="productCount">
                    <fmt:formatNumber value="${summary.productCount}" type="number"/>
                </div>
                <div class="inventory-card-note">Sản phẩm đang kinh doanh</div>
            </div>

            <div class="admin-card inventory-stat-card inventory-stat-card--success">
                <span class="inventory-card-icon">📦</span>
                <div class="inventory-card-label">Tổng tồn kho</div>
                <div class="inventory-card-value" data-inventory-summary-value="totalStock">
                    <fmt:formatNumber value="${summary.totalStock}" type="number"/>
                </div>
                <div class="inventory-card-note">Tổng số lượng còn lại</div>
            </div>

            <div class="admin-card inventory-stat-card inventory-stat-card--warning">
                <span class="inventory-card-icon">⚠️</span>
                <div class="inventory-card-label">Sắp hết hàng</div>
                <div class="inventory-card-value" data-inventory-summary-value="lowStockCount">
                    <fmt:formatNumber value="${summary.lowStockCount}" type="number"/>
                </div>
                <div class="inventory-card-note">Tồn kho lớn hơn 0 và nhỏ hơn 10</div>
            </div>

            <div class="admin-card inventory-stat-card inventory-stat-card--danger">
                <span class="inventory-card-icon">⛔</span>
                <div class="inventory-card-label">Hết hàng</div>
                <div class="inventory-card-value" data-inventory-summary-value="outOfStockCount">
                    <fmt:formatNumber value="${summary.outOfStockCount}" type="number"/>
                </div>
                <div class="inventory-card-note">Cần nhập hàng ngay</div>
            </div>
            </div>
        </section>

        <section class="admin-inventory-group">
            <div class="admin-inventory-group__head">
                <div>
                    <h2>Tổng quan tồn kho biến thể / SKU</h2>
                    <p>Quản lý tồn kho theo size, màu sắc và mức cảnh báo tối thiểu riêng cho từng SKU.</p>
                </div>
                <span class="admin-chip admin-chip--brand">Variant Stock</span>
            </div>

            <div class="inventory-summary-grid inventory-variant-summary-grid">
            <div class="admin-card inventory-stat-card inventory-stat-card--variant">
                <span class="inventory-card-icon">🏷️</span>
                <div class="inventory-card-label">Tổng biến thể active</div>
                <div class="inventory-card-value">
                    <fmt:formatNumber value="${variantSummary.variantCount}" type="number"/>
                </div>
                <div class="inventory-card-note">SKU/Size/Màu đang kinh doanh</div>
            </div>

            <div class="admin-card inventory-stat-card inventory-stat-card--success">
                <span class="inventory-card-icon">📦</span>
                <div class="inventory-card-label">Tổng tồn biến thể</div>
                <div class="inventory-card-value">
                    <fmt:formatNumber value="${variantSummary.totalStock}" type="number"/>
                </div>
                <div class="inventory-card-note">Tổng số lượng theo từng SKU</div>
            </div>

            <div class="admin-card inventory-stat-card inventory-stat-card--warning">
                <span class="inventory-card-icon">⚠️</span>
                <div class="inventory-card-label">Biến thể sắp hết</div>
                <div class="inventory-card-value">
                    <fmt:formatNumber value="${variantSummary.lowStockCount}" type="number"/>
                </div>
                <div class="inventory-card-note">Tồn kho thấp hơn mức tối thiểu</div>
            </div>

            <div class="admin-card inventory-stat-card inventory-stat-card--danger">
                <span class="inventory-card-icon">⛔</span>
                <div class="inventory-card-label">Biến thể hết hàng</div>
                <div class="inventory-card-value">
                    <fmt:formatNumber value="${variantSummary.outOfStockCount}" type="number"/>
                </div>
                <div class="inventory-card-note">Cần nhập hàng theo SKU</div>
            </div>
            </div>
        </section>

        <div class="inventory-section-grid">
            <section class="admin-card inventory-panel">
                <div class="inventory-panel-header">
                    <div>
                        <h2>Dashboard thống kê xuất kho</h2>
                        <span>Số lượng sản phẩm đã xuất theo từng mốc thời gian</span>
                    </div>
                </div>

                <div class="inventory-panel-body">
                    <div class="export-grid">
                        <div class="export-box">
                            <span class="export-box__icon">📅</span>
                            <strong>
                                <fmt:formatNumber value="${summary.exportedToday}" type="number"/>
                            </strong>
                            <span>Xuất hôm nay</span>
                        </div>

                        <div class="export-box">
                            <span class="export-box__icon">🗓️</span>
                            <strong>
                                <fmt:formatNumber value="${summary.exportedThisWeek}" type="number"/>
                            </strong>
                            <span>Xuất tuần này</span>
                        </div>

                        <div class="export-box">
                            <span class="export-box__icon">📈</span>
                            <strong>
                                <fmt:formatNumber value="${summary.exportedThisMonth}" type="number"/>
                            </strong>
                            <span>Xuất tháng này</span>
                        </div>

                        <div class="export-box">
                            <span class="export-box__icon">📊</span>
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

            <section class="admin-card inventory-panel">
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
                            <div class="admin-empty inventory-empty">
                                Hiện chưa có sản phẩm nào sắp hết hàng hoặc hết hàng.
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div class="variant-alert-block">
                        <div class="variant-alert-title">Cảnh báo biến thể/SKU</div>
                        <c:choose>
                            <c:when test="${not empty lowStockVariantAlerts}">
                                <div class="low-stock-list low-stock-list--variant">
                                    <c:forEach var="variant" items="${lowStockVariantAlerts}">
                                        <div class="low-stock-item">
                                            <div>
                                                <div class="low-stock-item-title">
                                                    <c:out value="${variant.displayProductTitle}"/>
                                                </div>
                                                <div class="low-stock-item-meta">
                                                    SKU: <c:out value="${variant.displaySku}"/>
                                                    ·
                                                    <c:out value="${variant.variantName}"/>
                                                    ·
                                                    Tối thiểu: <fmt:formatNumber value="${variant.minStock}" type="number"/>
                                                </div>
                                            </div>
                                            <div class="low-stock-number">
                                                <fmt:formatNumber value="${variant.stock}" type="number"/>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="admin-empty inventory-empty">
                                    Chưa có biến thể nào dưới mức cảnh báo.
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </section>
        </div>

        <section class="admin-card inventory-panel inventory-mt-20" id="inventoryExcel">
            <div class="inventory-panel-header">
                <div>
                    <h2>Nhập hàng bằng Excel</h2>
                    <span>
                        Tích chọn sản phẩm ở bảng bên dưới, nhập số lượng cần nhập rồi xuất file Excel.
                        Sau khi nhập hàng thực tế, upload lại file Excel để hệ thống cộng tồn kho.
                    </span>
                </div>
            </div>

            <div class="inventory-panel-body">
                <div class="inventory-excel-grid">

                    <div class="inventory-excel-box">
                        <div class="inventory-excel-title">1. Xuất danh sách nhập hàng</div>
                        <div class="inventory-excel-desc">
                            Chọn sản phẩm trong bảng tồn kho, nhập số lượng cần nhập và ghi chú.
                            Sau đó bấm nút bên dưới để tải file Excel về máy.
                        </div>

                        <form id="restockExportForm"
                              method="post"
                              action="${pageContext.request.contextPath}/admin/inventory">
                            <input type="hidden" name="action" value="exportRestockExcel">
                            <input type="hidden" name="csrf_token" value="${csrfValue}">
                            <input type="hidden" name="csrfToken" value="${csrfValue}">

                            <button class="admin-btn admin-btn--primary" type="submit">
                                Xuất file Excel nhập hàng
                            </button>
                        </form>
                    </div>

                    <div class="inventory-excel-box">
                        <div class="inventory-excel-title">2. Nhập kho từ file Excel</div>
                        <div class="inventory-excel-desc">
                            Upload file Excel đã cập nhật số lượng nhập thực tế.
                            Sau khi xử lý, hệ thống sẽ cập nhật tồn kho ngay trên trang, không cần tải lại.
                        </div>

                        <form class="inventory-import-excel-form"
                              data-ajax-import="true"
                              method="post"
                              enctype="multipart/form-data"
                              action="${pageContext.request.contextPath}/admin/inventory">
                            <input type="hidden" name="action" value="importRestockExcel">
                            <input type="hidden" name="ajax" value="true">
                            <input type="hidden" name="csrf_token" value="${csrfValue}">
                            <input type="hidden" name="csrfToken" value="${csrfValue}">

                            <input class="admin-input"
                                   type="file"
                                   name="restockFile"
                                   accept=".xlsx"
                                   required>

                            <button class="admin-btn admin-btn--ok" type="submit">
                                Nhập kho từ Excel
                            </button>
                        </form>
                    </div>

                </div>

                <div class="admin-empty inventory-excel-note">
                    <strong>Lưu ý:</strong>
                    File Excel cần giữ đúng các cột:
                    Mã SP, Tên sản phẩm, Danh mục, Thương hiệu, Tồn hiện tại,
                    Số lượng cần nhập, Ghi chú. Hệ thống sẽ đọc cột
                    <strong>Mã SP</strong> và <strong>Số lượng cần nhập</strong>
                    để cộng tồn kho.
                </div>
            </div>
        </section>

        <section class="admin-card inventory-panel inventory-mt-20" id="productInventory">
            <div class="inventory-panel-header">
                <div>
                    <h2>Danh sách tồn kho sản phẩm</h2>
                    <span>
                        Quản lý số lượng tồn, nhập thủ công hoặc chọn sản phẩm để xuất file Excel nhập hàng.
                    </span>
                </div>

                <form class="inventory-filter" method="get" action="${pageContext.request.contextPath}/admin/inventory">
                    <input
                            class="admin-input inventory-search-input"
                            type="text"
                            name="keyword"
                            value="${fn:escapeXml(keyword)}"
                            placeholder="Tìm sản phẩm, danh mục, thương hiệu">

                    <select class="admin-select inventory-status-select" name="status">
                        <option value="" ${empty status ? 'selected' : ''}>Tất cả trạng thái</option>
                        <option value="normal" ${status == 'normal' ? 'selected' : ''}>Còn hàng</option>
                        <option value="low" ${status == 'low' ? 'selected' : ''}>Sắp hết hàng</option>
                        <option value="out" ${status == 'out' ? 'selected' : ''}>Hết hàng</option>
                    </select>

                    <button class="admin-btn admin-btn--primary" type="submit">Lọc</button>

                    <a class="admin-btn" href="${pageContext.request.contextPath}/admin/inventory">
                        Làm mới
                    </a>
                </form>
            </div>

            <div class="admin-table-wrap inventory-table-wrap">
                <table class="admin-table inventory-table">
                    <thead>
                    <tr>
                        <th>Chọn Excel</th>
                        <th>Sản phẩm</th>
                        <th>Giá bán</th>
                        <th>Tồn kho</th>
                        <th>Trạng thái</th>
                        <th>Xuất ngày</th>
                        <th>Xuất tuần</th>
                        <th>Xuất tháng</th>
                        <th>Xuất năm</th>
                        <th>SL cần nhập</th>
                        <th>Ghi chú Excel</th>
                        <th>Nhập thủ công</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:choose>
                        <c:when test="${not empty products}">
                            <c:forEach var="product" items="${products}">
                                <tr data-inventory-product-row="${product.id}">
                                    <td class="inventory-select-cell">
                                        <label class="inventory-check-label">
                                            <input
                                                    type="checkbox"
                                                    name="selectedProductIds"
                                                    value="${product.id}"
                                                    form="restockExportForm">
                                            <span>Chọn</span>
                                        </label>
                                    </td>

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
                                        <span class="stock-number" data-inventory-stock="${product.id}">
                                            <fmt:formatNumber value="${product.stock}" type="number"/>
                                        </span>
                                    </td>

                                    <td>
                                        <span class="admin-pill ${product.stockStatusClass}" data-inventory-status="${product.id}">
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
                                        <input class="admin-input inventory-excel-qty-input"
                                               type="number"
                                               name="importQuantities_${product.id}"
                                               form="restockExportForm"
                                               min="1"
                                               step="1"
                                               placeholder="SL">
                                    </td>

                                    <td>
                                        <input class="admin-input inventory-excel-note-input"
                                               type="text"
                                               name="importNotes_${product.id}"
                                               form="restockExportForm"
                                               maxlength="255"
                                               placeholder="Ghi chú">
                                    </td>

                                    <td>
                                        <form class="inventory-add-stock-form"
                                              method="post"
                                              action="${pageContext.request.contextPath}/admin/inventory">
                                            <input type="hidden" name="action" value="addStock">
                                            <input type="hidden" name="productId" value="${product.id}">
                                            <input type="hidden" name="csrf_token" value="${csrfValue}">
                                            <input type="hidden" name="csrfToken" value="${csrfValue}">

                                            <input class="admin-input"
                                                   type="number"
                                                   name="quantity"
                                                   min="1"
                                                   step="1"
                                                   placeholder="SL"
                                                   required>

                                            <input class="admin-input"
                                                   type="text"
                                                   name="note"
                                                   maxlength="255"
                                                   placeholder="Ghi chú">

                                            <button class="admin-btn admin-btn--primary" type="submit">
                                                Nhập
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>

                        <c:otherwise>
                            <tr>
                                <td colspan="12">
                                    <div class="admin-empty inventory-empty">
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

        <section class="admin-card inventory-panel inventory-mt-20" id="variantInventory">
            <div class="inventory-panel-header">
                <div>
                    <h2>Kho biến thể / SKU</h2>
                    <span>
                        Quản lý tồn kho theo từng biến thể Size/Màu sắc. Hệ thống cảnh báo khi tồn kho thấp hơn mức tối thiểu của từng SKU.
                    </span>
                </div>

                <form class="inventory-filter" method="get" action="${pageContext.request.contextPath}/admin/inventory#variantInventory">
                    <input type="hidden" name="keyword" value="${fn:escapeXml(keyword)}">
                    <input type="hidden" name="status" value="${fn:escapeXml(status)}">

                    <input
                            class="admin-input inventory-search-input"
                            type="text"
                            name="variantKeyword"
                            value="${fn:escapeXml(variantKeyword)}"
                            placeholder="Tìm SKU, sản phẩm, size, màu">

                    <select class="admin-select inventory-status-select" name="variantStatus">
                        <option value="" ${empty variantStatus ? 'selected' : ''}>Tất cả biến thể</option>
                        <option value="normal" ${variantStatus == 'normal' ? 'selected' : ''}>Còn hàng</option>
                        <option value="low" ${variantStatus == 'low' ? 'selected' : ''}>Sắp hết hàng</option>
                        <option value="out" ${variantStatus == 'out' ? 'selected' : ''}>Hết hàng</option>
                    </select>

                    <button class="admin-btn admin-btn--primary" type="submit">Lọc SKU</button>
                </form>
            </div>

            <div class="admin-table-wrap inventory-variant-table-wrap">
                <table class="admin-table inventory-variant-table">
                    <thead>
                    <tr>
                        <th>SKU</th>
                        <th>Sản phẩm</th>
                        <th>Size</th>
                        <th>Màu sắc / Loại</th>
                        <th>Giá biến thể</th>
                        <th>Tồn kho</th>
                        <th>Mức cảnh báo</th>
                        <th>Trạng thái</th>
                        <th>Xuất tháng</th>
                        <th>Nhập biến thể</th>
                        <th>Cập nhật cảnh báo</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:choose>
                        <c:when test="${not empty variantProducts}">
                            <c:forEach var="variant" items="${variantProducts}">
                                <tr>
                                    <td>
                                        <span class="inventory-sku-code">
                                            <c:out value="${variant.displaySku}"/>
                                        </span>
                                    </td>

                                    <td>
                                        <div class="product-name">
                                            <c:out value="${variant.displayProductTitle}"/>
                                        </div>
                                        <div class="product-meta">
                                            <c:out value="${variant.displayCategoryName}"/>
                                            ·
                                            <c:out value="${variant.displayBrandName}"/>
                                        </div>
                                    </td>

                                    <td>
                                        <c:out value="${variant.displaySize}"/>
                                    </td>

                                    <td>
                                        <c:out value="${variant.displayColor}"/>
                                    </td>

                                    <td>
                                        <c:out value="${variant.formattedFinalPrice}"/>
                                    </td>

                                    <td>
                                        <span class="stock-number">
                                            <fmt:formatNumber value="${variant.stock}" type="number"/>
                                        </span>
                                    </td>

                                    <td>
                                        <span class="inventory-min-stock">
                                            <fmt:formatNumber value="${variant.minStock}" type="number"/>
                                        </span>
                                    </td>

                                    <td>
                                        <span class="admin-pill ${variant.stockStatusClass}">
                                            <c:out value="${variant.stockStatusLabel}"/>
                                        </span>
                                        <div class="inventory-variant-alert-text">
                                            <c:out value="${variant.alertText}"/>
                                        </div>
                                    </td>

                                    <td>
                                        <fmt:formatNumber value="${variant.exportedThisMonth}" type="number"/>
                                    </td>

                                    <td>
                                        <form class="inventory-add-stock-form inventory-add-variant-stock-form"
                                              method="post"
                                              action="${pageContext.request.contextPath}/admin/inventory#variantInventory">
                                            <input type="hidden" name="action" value="addVariantStock">
                                            <input type="hidden" name="productId" value="${variant.productId}">
                                            <input type="hidden" name="variantId" value="${variant.variantId}">
                                            <input type="hidden" name="csrf_token" value="${csrfValue}">
                                            <input type="hidden" name="csrfToken" value="${csrfValue}">

                                            <input class="admin-input"
                                                   type="number"
                                                   name="quantity"
                                                   min="1"
                                                   step="1"
                                                   placeholder="SL"
                                                   required>

                                            <input class="admin-input"
                                                   type="text"
                                                   name="note"
                                                   maxlength="255"
                                                   placeholder="Ghi chú">

                                            <button class="admin-btn admin-btn--primary" type="submit">
                                                Nhập
                                            </button>
                                        </form>
                                    </td>

                                    <td>
                                        <form class="inventory-min-stock-form"
                                              method="post"
                                              action="${pageContext.request.contextPath}/admin/inventory#variantInventory">
                                            <input type="hidden" name="action" value="updateVariantMinStock">
                                            <input type="hidden" name="productId" value="${variant.productId}">
                                            <input type="hidden" name="variantId" value="${variant.variantId}">
                                            <input type="hidden" name="csrf_token" value="${csrfValue}">
                                            <input type="hidden" name="csrfToken" value="${csrfValue}">

                                            <input class="admin-input"
                                                   type="number"
                                                   name="minStock"
                                                   min="1"
                                                   step="1"
                                                   value="${variant.minStock}"
                                                   required>

                                            <button class="admin-btn" type="submit">
                                                Lưu
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>

                        <c:otherwise>
                            <tr>
                                <td colspan="11">
                                    <div class="admin-empty inventory-empty">
                                        Chưa có biến thể phù hợp. Hãy thêm biến thể trong form sản phẩm trước khi quản lý tồn kho theo SKU.
                                    </div>
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="admin-card inventory-panel inventory-mt-20" id="inventoryImportHistory">
            <div class="inventory-panel-header">
                <div>
                    <h2>Lịch sử nhập hàng</h2>
                    <span>
                        Hiển thị rõ số lượng nhập hàng, tồn trước/sau và thống kê nhập hàng theo tháng, năm.
                    </span>
                </div>

                <form class="inventory-import-filter"
                      method="get"
                      action="${pageContext.request.contextPath}/admin/inventory">
                    <input type="hidden" name="keyword" value="${fn:escapeXml(keyword)}">
                    <input type="hidden" name="status" value="${fn:escapeXml(status)}">

                    <select class="admin-select" name="importMonth">
                        <c:forEach var="m" begin="1" end="12">
                            <option value="${m}" ${selectedImportMonth == m ? 'selected' : ''}>
                                Tháng ${m}
                            </option>
                        </c:forEach>
                    </select>

                    <select class="admin-select" name="importYear">
                        <c:choose>
                            <c:when test="${not empty importYearOptions}">
                                <c:forEach var="year" items="${importYearOptions}">
                                    <option value="${year}" ${selectedImportYear == year ? 'selected' : ''}>
                                        Năm ${year}
                                    </option>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <option value="${selectedImportYear}" selected>
                                    Năm ${selectedImportYear}
                                </option>
                            </c:otherwise>
                        </c:choose>
                    </select>

                    <input class="admin-input inventory-import-search"
                           type="text"
                           name="importKeyword"
                           value="${fn:escapeXml(importKeyword)}"
                           placeholder="Tìm sản phẩm, thương hiệu, ghi chú">

                    <button class="admin-btn admin-btn--primary" type="submit">
                        Lọc lịch sử
                    </button>

                    <a class="admin-btn"
                       href="${pageContext.request.contextPath}/admin/inventory">
                        Làm mới
                    </a>
                </form>
            </div>

            <div class="inventory-panel-body">
                <div class="inventory-import-summary-grid">
                    <div class="export-box inventory-import-stat">
                        <span class="export-box__icon">📥</span>
                        <strong data-inventory-import-summary-value="monthlyImportQuantity">
                            <fmt:formatNumber value="${importSummary.monthlyImportQuantity}" type="number"/>
                        </strong>
                        <span>SL nhập tháng ${selectedImportMonth}/${selectedImportYear}</span>
                    </div>

                    <div class="export-box inventory-import-stat">
                        <span class="export-box__icon">🔁</span>
                        <strong data-inventory-import-summary-value="monthlyImportCount">
                            <fmt:formatNumber value="${importSummary.monthlyImportCount}" type="number"/>
                        </strong>
                        <span>Lượt nhập trong tháng</span>
                    </div>

                    <div class="export-box inventory-import-stat">
                        <span class="export-box__icon">🧴</span>
                        <strong data-inventory-import-summary-value="monthlyProductCount">
                            <fmt:formatNumber value="${importSummary.monthlyProductCount}" type="number"/>
                        </strong>
                        <span>Sản phẩm đã nhập trong tháng</span>
                    </div>

                    <div class="export-box inventory-import-stat">
                        <span class="export-box__icon">📊</span>
                        <strong data-inventory-import-summary-value="yearlyImportQuantity">
                            <fmt:formatNumber value="${importSummary.yearlyImportQuantity}" type="number"/>
                        </strong>
                        <span>SL nhập năm ${selectedImportYear}</span>
                    </div>
                </div>

                <div class="chart-wrap inventory-import-chart-wrap">
                    <canvas id="inventoryImportChart"></canvas>
                </div>
            </div>

            <div class="admin-table-wrap inventory-import-history-wrap">
                <table class="admin-table inventory-import-history-table">
                    <thead>
                    <tr>
                        <th>Ngày nhập</th>
                        <th>Sản phẩm</th>
                        <th>SL nhập</th>
                        <th>Tồn trước</th>
                        <th>Tồn sau</th>
                        <th>Biến động</th>
                        <th>Hình thức</th>
                        <th>Người nhập</th>
                        <th>Ghi chú</th>
                    </tr>
                    </thead>

                    <tbody id="inventoryImportHistoryBody">
                    <c:choose>
                        <c:when test="${not empty importHistory}">
                            <c:forEach var="item" items="${importHistory}">
                                <tr>
                                    <td>
                                        <c:out value="${item.formattedCreatedAt}"/>
                                    </td>

                                    <td>
                                        <div class="product-name">
                                            <c:out value="${item.displayProductTitle}"/>
                                        </div>
                                        <div class="product-meta">
                                            <c:out value="${item.displayCategoryName}"/>
                                            ·
                                            <c:out value="${item.displayBrandName}"/>
                                        </div>
                                    </td>

                                    <td>
                                        <span class="inventory-import-qty">
                                            <c:out value="${item.quantityText}"/>
                                        </span>
                                    </td>

                                    <td>
                                        <c:out value="${item.beforeStockText}"/>
                                    </td>

                                    <td>
                                        <c:out value="${item.afterStockText}"/>
                                    </td>

                                    <td>
                                        <span class="inventory-stock-change">
                                            <c:out value="${item.stockChangeText}"/>
                                        </span>
                                    </td>

                                    <td>
                                        <span class="admin-pill ${item.referenceTypeClass}">
                                            <c:out value="${item.referenceTypeLabel}"/>
                                        </span>
                                    </td>

                                    <td>
                                        <c:out value="${item.displayCreatedByName}"/>
                                    </td>

                                    <td>
                                        <div class="inventory-history-note">
                                            <c:out value="${item.displayNote}"/>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>

                        <c:otherwise>
                            <tr>
                                <td colspan="9">
                                    <div class="admin-empty inventory-empty">
                                        Chưa có lịch sử nhập hàng trong thời gian hoặc từ khóa đã chọn.
                                    </div>
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="admin-card inventory-panel inventory-mt-20">
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
                        <div class="admin-empty inventory-empty">
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
        const exportLabels = ${empty last7DaysExportLabelsJson ? "[]" : last7DaysExportLabelsJson};
        const exportValues = ${empty last7DaysExportValuesJson ? "[]" : last7DaysExportValuesJson};

        const importLabels = ${empty importMonthStatsLabelsJson ? "[]" : importMonthStatsLabelsJson};
        const importValues = ${empty importMonthStatsValuesJson ? "[]" : importMonthStatsValuesJson};

        drawInventoryBarChart("inventoryExportChart", exportLabels, exportValues);
        drawInventoryBarChart("inventoryImportChart", importLabels, importValues);
        bindInventoryExcelImport();

        function bindInventoryExcelImport() {
            const form = document.querySelector(".inventory-import-excel-form[data-ajax-import='true']");

            if (!form || !window.FormData || !window.fetch) {
                return;
            }

            form.addEventListener("submit", async function (event) {
                event.preventDefault();

                const fileInput = form.querySelector("input[type='file'][name='restockFile']");

                if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
                    showInventoryAjaxMessage("Vui lòng chọn file Excel nhập hàng.", "danger");
                    return;
                }

                const button = form.querySelector("button[type='submit']");
                const originalText = button ? button.textContent : "";

                if (button) {
                    button.disabled = true;
                    button.textContent = "Đang nhập kho...";
                }

                try {
                    const formData = new FormData(form);
                    formData.set("ajax", "true");

                    const response = await fetch(form.action, {
                        method: "POST",
                        body: formData,
                        headers: {
                            "X-Requested-With": "XMLHttpRequest",
                            "Accept": "application/json"
                        },
                        credentials: "same-origin"
                    });

                    const payload = await response.json();

                    if (!response.ok || !payload.ok) {
                        showInventoryAjaxMessage(payload.message || "Nhập kho từ Excel không thành công.", "danger");
                        return;
                    }

                    applyInventoryImportResult(payload);
                    form.reset();
                    showInventoryAjaxMessage(payload.message || "Đã nhập kho từ Excel thành công.", "success");

                } catch (error) {
                    showInventoryAjaxMessage("Không thể nhập kho từ Excel. Vui lòng kiểm tra kết nối hoặc file nhập.", "danger");
                } finally {
                    if (button) {
                        button.disabled = false;
                        button.textContent = originalText;
                    }
                }
            });
        }

        function applyInventoryImportResult(payload) {
            const updates = Array.isArray(payload.productUpdates) ? payload.productUpdates : [];

            updates.forEach(function (item) {
                updateInventoryProductRow(item);
                prependInventoryImportHistoryRow(item);
            });

            updateSummaryValues(payload.summary || {}, "data-inventory-summary-value");
            updateSummaryValues(payload.importSummary || {}, "data-inventory-import-summary-value");
        }

        function updateInventoryProductRow(item) {
            if (!item || !item.productId) {
                return;
            }

            const stockElement = document.querySelector("[data-inventory-stock='" + item.productId + "']");
            const statusElement = document.querySelector("[data-inventory-status='" + item.productId + "']");
            const row = document.querySelector("[data-inventory-product-row='" + item.productId + "']");

            if (stockElement) {
                stockElement.textContent = formatInventoryNumber(item.afterStock || 0);
            }

            if (statusElement) {
                statusElement.classList.remove("stock-normal", "stock-low", "stock-out");
                statusElement.classList.add(item.stockStatusClass || "stock-normal");
                statusElement.textContent = item.stockStatusLabel || "Còn hàng";
            }

            if (row) {
                row.classList.remove("inventory-row-updated");
                void row.offsetWidth;
                row.classList.add("inventory-row-updated");
            }
        }

        function updateSummaryValues(values, dataAttributeName) {
            Object.keys(values).forEach(function (key) {
                const selector = "[" + dataAttributeName + "='" + key + "']";
                document.querySelectorAll(selector).forEach(function (element) {
                    element.textContent = formatInventoryNumber(values[key] || 0);
                });
            });
        }

        function prependInventoryImportHistoryRow(item) {
            const tbody = document.getElementById("inventoryImportHistoryBody");

            if (!tbody || !item || !item.productId) {
                return;
            }

            const emptyRow = tbody.querySelector(".inventory-empty");

            if (emptyRow) {
                const parentRow = emptyRow.closest("tr");

                if (parentRow) {
                    parentRow.remove();
                }
            }

            const row = document.createElement("tr");
            row.className = "inventory-import-history-row inventory-import-history-row--new";

            row.innerHTML = ""
                + "<td>" + escapeInventoryHtml(formatInventoryDateTime(new Date())) + "</td>"
                + "<td>"
                + "<div class='product-name'>" + escapeInventoryHtml(item.title || ("Sản phẩm #" + item.productId)) + "</div>"
                + "<div class='product-meta'>Cập nhật tức thì từ Excel</div>"
                + "</td>"
                + "<td><span class='inventory-import-qty'>+" + formatInventoryNumber(item.quantity || 0) + "</span></td>"
                + "<td>" + formatInventoryNumber(item.beforeStock || 0) + "</td>"
                + "<td>" + formatInventoryNumber(item.afterStock || 0) + "</td>"
                + "<td><span class='inventory-stock-change'>" + formatInventoryNumber(item.beforeStock || 0) + " → " + formatInventoryNumber(item.afterStock || 0) + "</span></td>"
                + "<td><span class='admin-pill import-method-excel'>Nhập từ Excel</span></td>"
                + "<td>Admin</td>"
                + "<td><div class='inventory-history-note'>Vừa nhập kho, chưa cần tải lại trang.</div></td>";

            tbody.insertBefore(row, tbody.firstChild);
        }

        function showInventoryAjaxMessage(message, type) {
            const messageBox = document.getElementById("inventoryAjaxMessage");

            if (!messageBox) {
                return;
            }

            messageBox.className = "admin-alert inventory-ajax-message "
                + (type === "danger" ? "admin-alert--danger" : "admin-alert--success");
            messageBox.textContent = message || "Đã xử lý nhập kho từ Excel.";
            messageBox.style.display = "block";
            messageBox.scrollIntoView({behavior: "smooth", block: "nearest"});
        }

        function formatInventoryNumber(value) {
            const number = Number(value) || 0;
            return number.toLocaleString("vi-VN");
        }

        function formatInventoryDateTime(date) {
            return date.toLocaleString("vi-VN", {
                day: "2-digit",
                month: "2-digit",
                year: "numeric",
                hour: "2-digit",
                minute: "2-digit"
            });
        }

        function escapeInventoryHtml(value) {
            return String(value == null ? "" : value)
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#39;");
        }

        function drawInventoryBarChart(canvasId, labels, values) {
            const canvas = document.getElementById(canvasId);

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

            const safeValues = Array.isArray(values) ? values : [];
            const safeLabels = Array.isArray(labels) ? labels : [];
            const maxValue = Math.max.apply(null, safeValues.concat([1]));
            const barGap = safeValues.length > 8 ? 7 : 12;
            const barWidth = safeValues.length > 0
                ? Math.max(14, (chartWidth - barGap * (safeValues.length - 1)) / safeValues.length)
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

                const gridLabel = Math.round(maxValue * i / 4).toString();
                ctx.textAlign = "left";
                ctx.fillText(gridLabel, 8, y + 4);
            }

            safeValues.forEach(function (value, index) {
                const safeValue = Number(value) || 0;
                const barHeight = Math.round((safeValue / maxValue) * chartHeight);
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
                ctx.fillText(safeValue.toString(), x + barWidth / 2, y - 6);

                ctx.fillStyle = "#6b7280";
                ctx.font = "11px Arial";

                const rawLabel = safeLabels[index] ? String(safeLabels[index]) : "";
                const label = rawLabel.includes("-")
                    ? rawLabel.substring(5)
                    : rawLabel.substring(0, 5);

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
        }
    })();
</script>

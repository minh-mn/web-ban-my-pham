<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="MyCosmetic | Chi tiết đơn hàng" scope="request" />
<c:set var="pageCss" value="/order.css" scope="request" />

<style>
    .order-detail-page {
        padding: 32px 0;
        background: #fff7fb;
        min-height: 70vh;
    }

    .order-detail-container {
        max-width: 1180px;
        margin: 0 auto;
        padding: 0 16px;
    }

    .order-detail-header {
        display: flex;
        justify-content: space-between;
        gap: 16px;
        align-items: flex-start;
        margin-bottom: 22px;
    }

    .order-detail-title {
        margin: 0 0 10px;
        color: #1f2a44;
        font-size: 28px;
        line-height: 1.25;
        font-weight: 900;
    }

    .order-detail-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        align-items: stretch;
    }

    .status-summary-card {
        display: flex;
        align-items: center;
        gap: 10px;
        min-width: 230px;
        padding: 10px 12px;
        border: 1px solid #eef2f7;
        border-radius: 15px;
        background: #ffffff;
    }

    .status-summary-icon {
        width: 34px;
        height: 34px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        border-radius: 12px;
        background: #fff3f8;
        color: #d63384;
        font-weight: 950;
    }

    .status-summary-content span {
        display: block;
        color: #7b8794;
        font-size: 12px;
        font-weight: 850;
        text-transform: uppercase;
        letter-spacing: 0.04em;
    }

    .status-summary-content strong {
        display: block;
        margin-top: 3px;
        color: #1f2a44;
        font-size: 14px;
        font-weight: 950;
    }

    .order-back-btn {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        min-height: 42px;
        padding: 0 16px;
        border-radius: 12px;
        border: 1px solid #e5e7eb;
        color: #334155;
        text-decoration: none;
        background: #ffffff;
        font-weight: 800;
        transition: 0.18s ease;
        white-space: nowrap;
    }

    .order-back-btn:hover {
        border-color: #f3b8d2;
        color: #d63384;
        background: #fff3f8;
        transform: translateY(-1px);
    }

    .order-card {
        background: #ffffff;
        border: 1px solid #f0e8ee;
        border-radius: 22px;
        padding: 22px;
        margin-bottom: 22px;
        box-shadow: 0 10px 30px rgba(31, 42, 68, 0.06);
    }

    .order-card-title {
        margin: 0 0 16px;
        color: #1f2a44;
        font-size: 20px;
        line-height: 1.3;
        font-weight: 900;
    }

    .order-info-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 16px;
    }

    .order-info-item.full {
        grid-column: 1 / -1;
    }

    .order-info-label {
        margin-bottom: 5px;
        color: #7b8794;
        font-size: 13px;
        font-weight: 800;
    }

    .order-info-value {
        color: #1f2a44;
        font-size: 15px;
        font-weight: 800;
        line-height: 1.45;
    }

    .order-muted {
        color: #7b8794;
    }

    .order-pill {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        min-height: 30px;
        min-width: 128px;
        padding: 5px 13px;
        border-radius: 999px;
        font-size: 13px;
        line-height: 1;
        font-weight: 900;
        text-align: center;
        white-space: nowrap;
    }

    .order-pill.ok {
        background: #e8f7ef;
        color: #12804a;
        border: 1px solid #bdebd1;
    }

    .order-pill.danger {
        background: #fdecec;
        color: #c62828;
        border: 1px solid #facaca;
    }

    .order-pill.info {
        background: #eaf3ff;
        color: #1769aa;
        border: 1px solid #c7ddff;
    }

    .order-pill.warning {
        background: #fff6e5;
        color: #9a5b00;
        border: 1px solid #ffe4ad;
    }

    .order-pill.muted {
        background: #f8fafc;
        color: #475569;
        border: 1px solid #e2e8f0;
    }

    .shipping-summary-grid {
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
        gap: 14px;
        margin-bottom: 20px;
    }

    .shipping-summary-box {
        padding: 14px;
        border: 1px solid #eef2f7;
        border-radius: 15px;
        background: #f8fafc;
    }

    .shipping-summary-box span {
        display: block;
        margin-bottom: 6px;
        color: #7b8794;
        font-size: 12px;
        font-weight: 900;
        text-transform: uppercase;
        letter-spacing: 0.04em;
    }

    .shipping-summary-box strong {
        color: #1f2a44;
        font-size: 14px;
        font-weight: 950;
        line-height: 1.4;
    }

    .tracking-current-note {
        margin: 0 0 16px;
        padding: 12px 14px;
        border-radius: 15px;
        background: #fff8fb;
        color: #7b3a56;
        font-size: 14px;
        font-weight: 750;
        line-height: 1.45;
    }

    .tracking-steps {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 14px;
        margin-top: 18px;
    }

    .tracking-step {
        position: relative;
        min-height: 122px;
        padding: 17px;
        border: 1px solid #e5e7eb;
        border-radius: 20px;
        background: #f8fafc;
        color: #64748b;
    }

    .tracking-step::before {
        content: "";
        width: 34px;
        height: 34px;
        display: block;
        margin-bottom: 10px;
        border-radius: 50%;
        background: #e5e7eb;
    }

    .tracking-step::after {
        content: "";
        position: absolute;
        top: 33px;
        left: 54px;
        right: -21px;
        height: 4px;
        border-radius: 999px;
        background: #e5e7eb;
        z-index: 0;
    }

    .tracking-step:last-child::after {
        display: none;
    }

    .tracking-step strong {
        display: block;
        position: relative;
        z-index: 1;
        color: inherit;
        font-size: 15.5px;
        font-weight: 950;
        line-height: 1.35;
    }

    .tracking-step small {
        display: block;
        position: relative;
        z-index: 1;
        margin-top: 5px;
        color: inherit;
        font-size: 13px;
        line-height: 1.45;
        font-weight: 700;
    }

    .tracking-step.done {
        border-color: #bdebd1;
        background: #f0fdf4;
        color: #166534;
    }

    .tracking-step.done::before,
    .tracking-step.done::after {
        background: #22c55e;
    }

    .tracking-step.active {
        border-color: #c7ddff;
        background: #eff6ff;
        color: #1d4ed8;
    }

    .tracking-step.active::before {
        background: #3b82f6;
        box-shadow: 0 0 0 6px rgba(59, 130, 246, 0.13);
    }

    .tracking-step.failed {
        border-color: #facaca;
        background: #fff1f2;
        color: #b91c1c;
    }

    .tracking-step.failed::before {
        background: #ef4444;
        box-shadow: 0 0 0 6px rgba(239, 68, 68, 0.12);
    }

    .tracking-history {
        margin-top: 22px;
        padding-top: 18px;
        border-top: 1px solid #eef2f7;
    }

    .tracking-history-title {
        margin: 0 0 12px;
        color: #1f2a44;
        font-size: 16px;
        font-weight: 900;
    }

    .tracking-history-list {
        display: flex;
        flex-direction: column;
        gap: 10px;
    }

    .tracking-history-item {
        display: grid;
        grid-template-columns: 170px 1fr;
        gap: 14px;
        padding: 13px 14px;
        border: 1px solid #eef2f7;
        border-radius: 15px;
        background: #ffffff;
    }

    .tracking-history-time {
        color: #7b8794;
        font-size: 13px;
        font-weight: 850;
    }

    .tracking-history-content strong {
        display: block;
        margin-bottom: 4px;
        color: #1f2a44;
        font-size: 14px;
        font-weight: 900;
    }

    .tracking-history-content p {
        margin: 0;
        color: #475569;
        font-size: 13.5px;
        line-height: 1.45;
    }

    .order-empty-box {
        padding: 18px;
        border-radius: 14px;
        background: #f8fafc;
        color: #7b8794;
        font-weight: 700;
    }

    .order-items-table-wrap {
        overflow-x: auto;
    }

    .order-items-table {
        width: 100%;
        min-width: 760px;
        border-collapse: collapse;
    }

    .order-items-table th {
        padding: 12px 8px;
        border-bottom: 1px solid #eee;
        color: #475569;
        font-size: 13px;
        text-align: left;
        font-weight: 900;
    }

    .order-items-table td {
        padding: 14px 8px;
        border-bottom: 1px solid #f1f1f1;
        color: #334155;
        vertical-align: top;
    }

    .order-product-img {
        width: 58px;
        height: 58px;
        object-fit: cover;
        border-radius: 12px;
        border: 1px solid #eee;
        background: #f8fafc;
    }

    .order-product-placeholder {
        width: 58px;
        height: 58px;
        border-radius: 12px;
        background: #f1f5f9;
        color: #94a3b8;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .variant-badge {
        display: inline-block;
        margin-bottom: 8px;
        padding: 4px 10px;
        border-radius: 999px;
        background: #f4edf7;
        color: #8b4aa8;
        font-size: 12px;
        font-weight: 850;
    }

    .payment-summary {
        max-width: 480px;
        margin-left: auto;
    }

    .summary-line {
        display: flex;
        justify-content: space-between;
        gap: 16px;
        padding: 9px 0;
        color: #555;
        font-size: 15px;
    }

    .summary-line strong {
        color: #1f2a44;
    }

    .summary-total {
        margin-top: 6px;
        padding-top: 15px;
        border-top: 1px solid #eee;
        font-size: 20px;
        font-weight: 900;
    }

    .summary-total strong {
        color: #d63384;
        font-size: 22px;
    }

    @media (max-width: 900px) {
        .order-detail-header {
            flex-direction: column;
        }

        .order-info-grid,
        .shipping-summary-grid,
        .tracking-steps,
        .tracking-history-item {
            grid-template-columns: 1fr;
        }

        .tracking-step::after {
            display: none;
        }

        .payment-summary {
            max-width: none;
            margin-left: 0;
        }
    }
</style>

<section class="order-detail-page">
    <div class="order-detail-container">

        <div class="order-detail-header">
            <div>
                <h1 class="order-detail-title">
                    Chi tiết đơn hàng #${order.id}
                </h1>

                <div class="order-detail-meta">
                    <div class="status-summary-card">
                        <span class="status-summary-icon">📦</span>
                        <div class="status-summary-content">
                            <span>Trạng thái đơn</span>
                            <strong>
                                <c:choose>
                                    <c:when test="${order.status == 'processing'}">Chờ xác nhận</c:when>
                                    <c:when test="${order.status == 'confirmed'}">Đã xác nhận</c:when>
                                    <c:when test="${order.status == 'shipping'}">Đang giao</c:when>
                                    <c:when test="${order.status == 'completed'}">Hoàn thành</c:when>
                                    <c:when test="${order.status == 'cancelled' || order.status == 'canceled'}">Đã hủy</c:when>
                                    <c:otherwise>
                                        <c:out value="${empty order.statusLabel ? order.status : order.statusLabel}" />
                                    </c:otherwise>
                                </c:choose>
                            </strong>
                        </div>
                    </div>

                    <div class="status-summary-card">
                        <span class="status-summary-icon">🚚</span>
                        <div class="status-summary-content">
                            <span>Vận chuyển</span>
                            <strong>
                                <c:choose>
                                    <c:when test="${order.delivered}">Giao thành công</c:when>
                                    <c:when test="${order.deliveryFailed}">Giao thất bại</c:when>
                                    <c:when test="${order.shippingCanceled}">Đã hủy vận chuyển</c:when>
                                    <c:when test="${order.delivering}">Đang giao</c:when>
                                    <c:otherwise>Chờ lấy hàng</c:otherwise>
                                </c:choose>
                            </strong>
                        </div>
                    </div>
                </div>
            </div>

            <a href="${pageContext.request.contextPath}/account"
               class="order-back-btn">
                Quay lại tài khoản
            </a>
        </div>

        <div class="order-card">
            <h2 class="order-card-title">Thông tin nhận hàng</h2>

            <div class="order-info-grid">
                <div class="order-info-item">
                    <div class="order-info-label">Người nhận</div>
                    <div class="order-info-value">
                        <c:out value="${order.fullName}" />
                    </div>
                </div>

                <div class="order-info-item">
                    <div class="order-info-label">Số điện thoại</div>
                    <div class="order-info-value">
                        <c:out value="${order.phone}" />
                    </div>
                </div>

                <div class="order-info-item full">
                    <div class="order-info-label">Địa chỉ giao hàng</div>
                    <div class="order-info-value">
                        <c:out value="${order.address}" />
                    </div>
                </div>

                <div class="order-info-item">
                    <div class="order-info-label">Ngày đặt</div>
                    <div class="order-info-value">
                        <fmt:formatDate value="${order.createdAtDate}" pattern="dd/MM/yyyy HH:mm" />
                    </div>
                </div>

                <div class="order-info-item">
                    <div class="order-info-label">Thanh toán</div>
                    <div class="order-info-value">
                        <c:out value="${order.paymentMethod}" />
                        -
                        <c:choose>
                            <c:when test="${order.paymentStatus == 'PAID'}">
                                <span class="order-pill ok">Đã thanh toán</span>
                            </c:when>
                            <c:when test="${order.paymentStatus == 'CANCELED' || order.paymentStatus == 'FAILED'}">
                <span class="order-pill danger">
                  <c:out value="${order.paymentStatus}" />
                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="order-pill warning">Chờ thanh toán</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <c:if test="${not empty order.vnpTxnRef}">
                    <div class="order-info-item full">
                        <div class="order-info-label">Mã giao dịch VNPAY</div>
                        <div class="order-info-value">
                            <c:out value="${order.vnpTxnRef}" />
                        </div>
                    </div>
                </c:if>
            </div>
        </div>

        <div class="order-card" id="shipping-tracking">
            <h2 class="order-card-title">Theo dõi vận chuyển</h2>

            <div class="shipping-summary-grid">
                <div class="shipping-summary-box">
                    <span>Phương thức</span>
                    <strong>
                        <c:out value="${order.shippingMethodLabel}" />
                    </strong>
                </div>

                <div class="shipping-summary-box">
                    <span>Đơn vị giao</span>
                    <strong>
                        <c:out value="${order.shippingProviderLabel}" />
                    </strong>
                </div>

                <div class="shipping-summary-box">
                    <span>Mã vận đơn</span>
                    <strong>
                        <c:choose>
                            <c:when test="${not empty order.shippingCode}">
                                <c:out value="${order.shippingCode}" />
                            </c:when>
                            <c:otherwise>Đang cập nhật</c:otherwise>
                        </c:choose>
                    </strong>
                </div>

                <div class="shipping-summary-box">
                    <span>Phí vận chuyển</span>
                    <strong>
                        <fmt:formatNumber value="${empty order.shippingFee ? 0 : order.shippingFee}"
                                          type="number"
                                          groupingUsed="true"
                                          minFractionDigits="0"
                                          maxFractionDigits="0" /> ₫
                    </strong>
                </div>
            </div>

            <p class="tracking-current-note">
                <c:choose>
                    <c:when test="${order.delivered}">
                        Đơn hàng đã được giao thành công. Cảm ơn bạn đã mua hàng tại MyCosmetic.
                    </c:when>
                    <c:when test="${order.deliveryFailed}">
                        Giao hàng thất bại. Shop sẽ liên hệ lại để hỗ trợ giao lại hoặc xử lý đơn hàng.
                    </c:when>
                    <c:when test="${order.shippingCanceled}">
                        Vận chuyển của đơn hàng đã bị hủy. Vui lòng liên hệ shop nếu cần hỗ trợ.
                    </c:when>
                    <c:when test="${order.delivering}">
                        Đơn hàng đang được vận chuyển đến bạn.
                    </c:when>
                    <c:otherwise>
                        Đơn hàng đang chờ shop chuẩn bị và bàn giao cho đơn vị vận chuyển.
                    </c:otherwise>
                </c:choose>
            </p>

            <div class="tracking-steps">
                <div class="tracking-step ${order.pendingPickup ? 'active' : (order.delivering || order.delivered || order.deliveryFailed ? 'done' : '')}">
                    <strong>Chờ lấy hàng</strong>
                    <small>Shop chuẩn bị và chờ bàn giao đơn hàng.</small>
                </div>

                <div class="tracking-step ${order.delivering ? 'active' : (order.delivered || order.deliveryFailed ? 'done' : '')}">
                    <strong>Đang giao</strong>
                    <small>
                        <c:choose>
                            <c:when test="${not empty order.shippedAtDate}">
                                Bắt đầu giao:
                                <fmt:formatDate value="${order.shippedAtDate}" pattern="dd/MM/yyyy HH:mm" />
                            </c:when>
                            <c:otherwise>Đơn hàng sẽ được cập nhật khi bắt đầu giao.</c:otherwise>
                        </c:choose>
                    </small>
                </div>

                <c:choose>
                    <c:when test="${order.deliveryFailed}">
                        <div class="tracking-step failed">
                            <strong>Giao thất bại</strong>
                            <small>Shop sẽ liên hệ lại để hỗ trợ giao lại.</small>
                        </div>
                    </c:when>

                    <c:when test="${order.shippingCanceled}">
                        <div class="tracking-step failed">
                            <strong>Đã hủy vận chuyển</strong>
                            <small>Đơn vận chuyển đã bị hủy.</small>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="tracking-step ${order.delivered ? 'done' : ''}">
                            <strong>Giao thành công</strong>
                            <small>
                                <c:choose>
                                    <c:when test="${not empty order.deliveredAtDate}">
                                        Hoàn tất:
                                        <fmt:formatDate value="${order.deliveredAtDate}" pattern="dd/MM/yyyy HH:mm" />
                                    </c:when>
                                    <c:otherwise>Chờ đơn hàng được giao thành công.</c:otherwise>
                                </c:choose>
                            </small>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <c:if test="${not empty trackingList}">
                <div class="tracking-history">
                    <h3 class="tracking-history-title">Lịch sử vận chuyển</h3>

                    <div class="tracking-history-list">
                        <c:forEach var="tracking" items="${trackingList}">
                            <div class="tracking-history-item">
                                <div class="tracking-history-time">
                                    <c:choose>
                                        <c:when test="${not empty tracking.createdAt}">
                                            <c:out value="${tracking.createdAt}" />
                                        </c:when>
                                        <c:otherwise>Không rõ thời gian</c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="tracking-history-content">
                                    <strong>
                                        <c:out value="${tracking.shippingStatusLabel}" />
                                    </strong>

                                    <p>
                                        <c:out value="${tracking.note}" />
                                    </p>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </c:if>
        </div>

        <div class="order-card">
            <h2 class="order-card-title">Sản phẩm đã mua</h2>

            <c:set var="displayItems" value="${orderItems}" />

            <c:if test="${empty displayItems && not empty items}">
                <c:set var="displayItems" value="${items}" />
            </c:if>

            <c:choose>
                <c:when test="${empty displayItems}">
                    <div class="order-empty-box">
                        Đơn hàng chưa có sản phẩm hoặc chưa load được chi tiết sản phẩm.
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="order-items-table-wrap">
                        <table class="order-items-table">
                            <thead>
                            <tr>
                                <th style="width: 78px;">Ảnh</th>
                                <th>Sản phẩm</th>
                                <th style="width: 210px;">Biến thể</th>
                                <th style="width: 130px; text-align: right;">Đơn giá</th>
                                <th style="width: 80px; text-align: center;">SL</th>
                                <th style="width: 150px; text-align: right;">Thành tiền</th>
                            </tr>
                            </thead>

                            <tbody>
                            <c:forEach var="item" items="${displayItems}">
                                <tr>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty item.imageUrl}">
                                                <img src="${pageContext.request.contextPath}${item.imageUrl}"
                                                     alt="${item.productName}"
                                                     class="order-product-img" />
                                            </c:when>

                                            <c:otherwise>
                                                <div class="order-product-placeholder">—</div>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td>
                                        <div style="font-weight: 900; color: #1f2a44;">
                                            <c:out value="${item.productName}" />
                                        </div>

                                        <div class="order-muted" style="font-size: 13px; margin-top: 4px;">
                                            Mã sản phẩm:
                                            <c:out value="${item.productId}" />
                                        </div>
                                    </td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty item.variantId
                                  || not empty item.variantName
                                  || not empty item.variantSize
                                  || not empty item.variantType}">
                                                <div class="variant-badge">Có biến thể</div>

                                                <div style="font-size: 13px; color: #444; line-height: 1.7;">
                                                    <c:if test="${not empty item.variantName}">
                                                        <div>
                                                            <strong>Tên:</strong>
                                                            <c:out value="${item.variantName}" />
                                                        </div>
                                                    </c:if>

                                                    <c:if test="${not empty item.variantSize}">
                                                        <div>
                                                            <strong>Size:</strong>
                                                            <c:out value="${item.variantSize}" />
                                                        </div>
                                                    </c:if>

                                                    <c:if test="${not empty item.variantType}">
                                                        <div>
                                                            <strong>Loại:</strong>
                                                            <c:out value="${item.variantType}" />
                                                        </div>
                                                    </c:if>

                                                    <c:if test="${not empty item.variantId}">
                                                        <div class="order-muted">
                                                            Variant ID:
                                                            <c:out value="${item.variantId}" />
                                                        </div>
                                                    </c:if>
                                                </div>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="order-muted">Không có biến thể</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td style="text-align: right;">
                                        <fmt:formatNumber value="${item.price}"
                                                          type="number"
                                                          groupingUsed="true"
                                                          minFractionDigits="0"
                                                          maxFractionDigits="0" />
                                        ₫
                                    </td>

                                    <td style="text-align: center;">
                                        <c:out value="${item.quantity}" />
                                    </td>

                                    <td style="text-align: right; font-weight: 900;">
                                        <fmt:formatNumber value="${item.subtotal}"
                                                          type="number"
                                                          groupingUsed="true"
                                                          minFractionDigits="0"
                                                          maxFractionDigits="0" />
                                        ₫
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="order-card">
            <h2 class="order-card-title">Tổng kết thanh toán</h2>

            <div class="payment-summary">
                <div class="summary-line">
                    <span>Giảm giá coupon</span>
                    <strong>
                        -
                        <fmt:formatNumber value="${empty order.couponDiscount ? 0 : order.couponDiscount}"
                                          type="number"
                                          groupingUsed="true"
                                          minFractionDigits="0"
                                          maxFractionDigits="0" />
                        ₫
                    </strong>
                </div>

                <div class="summary-line">
                    <span>Phí vận chuyển</span>
                    <strong>
                        <fmt:formatNumber value="${empty order.shippingFee ? 0 : order.shippingFee}"
                                          type="number"
                                          groupingUsed="true"
                                          minFractionDigits="0"
                                          maxFractionDigits="0" />
                        ₫
                    </strong>
                </div>

                <div class="summary-line summary-total">
                    <span>Tổng tiền</span>
                    <strong>
                        <fmt:formatNumber value="${empty order.total ? 0 : order.total}"
                                          type="number"
                                          groupingUsed="true"
                                          minFractionDigits="0"
                                          maxFractionDigits="0" />
                        ₫
                    </strong>
                </div>
            </div>
        </div>

    </div>
</section>

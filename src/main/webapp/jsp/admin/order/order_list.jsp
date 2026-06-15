<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Quản lý đơn hàng" scope="request"/>
<c:set var="activeMenu" value="orders" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="orderPageTotal" value="${empty orders ? 0 : fn:length(orders)}" />
<c:set var="orderPendingCount" value="0" />
<c:set var="orderShippingCount" value="0" />
<c:set var="orderCompletedCount" value="0" />
<c:set var="orderCancelledCount" value="0" />

<c:forEach var="orderStat" items="${orders}">
  <c:set var="orderStatStatus" value="${fn:toLowerCase(orderStat.status)}" />
  <c:choose>
    <c:when test="${orderStatStatus eq 'processing' or orderStatStatus eq 'pending'}">
      <c:set var="orderPendingCount" value="${orderPendingCount + 1}" />
    </c:when>
    <c:when test="${orderStatStatus eq 'shipping'}">
      <c:set var="orderShippingCount" value="${orderShippingCount + 1}" />
    </c:when>
    <c:when test="${orderStatStatus eq 'completed'}">
      <c:set var="orderCompletedCount" value="${orderCompletedCount + 1}" />
    </c:when>
    <c:when test="${orderStatStatus eq 'cancelled' or orderStatStatus eq 'canceled'}">
      <c:set var="orderCancelledCount" value="${orderCancelledCount + 1}" />
    </c:when>
  </c:choose>
</c:forEach>


<main class="admin-main">
  <div class="admin-container admin-order-page">

    <div class="admin-order-hero">
      <div class="admin-order-hero__content">
        <span class="admin-order-eyebrow">BÁN HÀNG &amp; ĐƠN HÀNG</span>
        <h1 class="admin-order-title">Quản lý đơn hàng</h1>
        <p class="admin-subtext admin-order-subtitle">
          Lọc nhanh, tìm kiếm, phân trang và xử lý nhiều đơn cùng lúc theo luồng:
          <strong>Chờ xác nhận → Đã xác nhận → Đang giao → Giao thành công</strong>.
        </p>
      </div>
      <div class="admin-order-hero__actions">
        <a class="admin-btn" href="${ctx}/admin/orders">Làm mới</a>
      </div>
    </div>

    <c:if test="${not empty admin_order_success}">
      <div class="admin-alert admin-alert--success"><c:out value="${admin_order_success}"/></div>
    </c:if>
    <c:if test="${not empty admin_order_error}">
      <div class="admin-alert admin-alert--danger"><c:out value="${admin_order_error}"/></div>
    </c:if>


    <section class="admin-order-summary">
      <div class="admin-order-stat admin-order-stat--total">
        <span class="admin-order-stat__icon">📦</span>
        <span class="admin-order-stat__label">Tổng đơn phù hợp</span>
        <strong class="admin-order-stat__value">
          <fmt:formatNumber value="${totalRows}" type="number" groupingUsed="true"/>
        </strong>
        <span class="admin-order-stat__note">Theo bộ lọc hiện tại</span>
      </div>

      <div class="admin-order-stat admin-order-stat--pending">
        <span class="admin-order-stat__icon">⏳</span>
        <span class="admin-order-stat__label">Chờ xác nhận</span>
        <strong class="admin-order-stat__value">
          <c:out value="${orderPendingCount}" />
        </strong>
        <span class="admin-order-stat__note">Tính trên trang đang xem</span>
      </div>

      <div class="admin-order-stat admin-order-stat--shipping">
        <span class="admin-order-stat__icon">🚚</span>
        <span class="admin-order-stat__label">Đang giao</span>
        <strong class="admin-order-stat__value">
          <c:out value="${orderShippingCount}" />
        </strong>
        <span class="admin-order-stat__note">Cần theo dõi vận chuyển</span>
      </div>

      <div class="admin-order-stat admin-order-stat--completed">
        <span class="admin-order-stat__icon">✅</span>
        <span class="admin-order-stat__label">Hoàn tất</span>
        <strong class="admin-order-stat__value">
          <c:out value="${orderCompletedCount}" />
        </strong>
        <span class="admin-order-stat__note">Đã giao thành công</span>
      </div>

      <div class="admin-order-stat admin-order-stat--cancelled">
        <span class="admin-order-stat__icon">⛔</span>
        <span class="admin-order-stat__label">Đã hủy</span>
        <strong class="admin-order-stat__value">
          <c:out value="${orderCancelledCount}" />
        </strong>
        <span class="admin-order-stat__note">Tính trên trang đang xem</span>
      </div>
    </section>

    <div class="admin-order-filter-card admin-card">
      <div class="admin-card__body">
        <div class="admin-order-section-head admin-order-section-head--filter">
          <div>
            <h2 class="admin-order-section-title">Lọc và tìm kiếm</h2>
            <p class="admin-order-section-desc">
              Tìm theo mã đơn, khách hàng, SĐT, địa chỉ, mã vận đơn hoặc mã giao dịch VNPay.
            </p>
          </div>
          <div class="admin-order-result-count">
            <strong><fmt:formatNumber value="${totalRows}" type="number" groupingUsed="true"/></strong>
            <span>đơn phù hợp</span>
          </div>
        </div>

        <form class="admin-order-filter-form" method="get" action="${ctx}/admin/orders">
          <input type="hidden" name="action" value="list">

          <label class="admin-order-filter-field admin-order-filter-field--keyword">
            <span>Từ khóa</span>
            <input class="admin-input" type="text" name="keyword" value="${fn:escapeXml(filter.keyword)}"
                   placeholder="Mã đơn, tên khách, SĐT, mã vận đơn...">
          </label>

          <label class="admin-order-filter-field">
            <span>Trạng thái đơn</span>
            <select class="admin-select" name="orderStatus">
              <option value="">Tất cả</option>
              <option value="processing" <c:if test="${filter.orderStatus eq 'processing'}">selected</c:if>>Chờ xác nhận</option>
              <option value="confirmed" <c:if test="${filter.orderStatus eq 'confirmed'}">selected</c:if>>Đã xác nhận</option>
              <option value="shipping" <c:if test="${filter.orderStatus eq 'shipping'}">selected</c:if>>Đang giao</option>
              <option value="completed" <c:if test="${filter.orderStatus eq 'completed'}">selected</c:if>>Giao thành công</option>
              <option value="cancelled" <c:if test="${filter.orderStatus eq 'cancelled'}">selected</c:if>>Đã hủy</option>
            </select>
          </label>

          <label class="admin-order-filter-field">
            <span>Thanh toán</span>
            <select class="admin-select" name="paymentStatus">
              <option value="">Tất cả</option>
              <option value="PENDING" <c:if test="${filter.paymentStatus eq 'PENDING'}">selected</c:if>>Chờ thanh toán</option>
              <option value="PAID" <c:if test="${filter.paymentStatus eq 'PAID'}">selected</c:if>>Đã thanh toán</option>
              <option value="FAILED" <c:if test="${filter.paymentStatus eq 'FAILED'}">selected</c:if>>Thất bại</option>
              <option value="CANCELED" <c:if test="${filter.paymentStatus eq 'CANCELED'}">selected</c:if>>Đã hủy</option>
              <option value="REFUNDED" <c:if test="${filter.paymentStatus eq 'REFUNDED'}">selected</c:if>>Đã hoàn tiền</option>
            </select>
          </label>

          <label class="admin-order-filter-field">
            <span>Vận chuyển</span>
            <select class="admin-select" name="shippingStatus">
              <option value="">Tất cả</option>
              <option value="PENDING_PICKUP" <c:if test="${filter.shippingStatus eq 'PENDING_PICKUP'}">selected</c:if>>Chờ lấy hàng</option>
              <option value="DELIVERING" <c:if test="${filter.shippingStatus eq 'DELIVERING'}">selected</c:if>>Đang giao</option>
              <option value="DELIVERED" <c:if test="${filter.shippingStatus eq 'DELIVERED'}">selected</c:if>>Giao thành công</option>
              <option value="FAILED" <c:if test="${filter.shippingStatus eq 'FAILED'}">selected</c:if>>Giao thất bại</option>
              <option value="CANCELED" <c:if test="${filter.shippingStatus eq 'CANCELED'}">selected</c:if>>Đã hủy giao</option>
            </select>
          </label>

          <label class="admin-order-filter-field">
            <span>Đơn vị giao</span>
            <select class="admin-select" name="shippingProvider">
              <option value="">Tất cả</option>
              <option value="INTERNAL" <c:if test="${filter.shippingProvider eq 'INTERNAL'}">selected</c:if>>Vận chuyển nội bộ</option>
              <option value="GHTK" <c:if test="${filter.shippingProvider eq 'GHTK'}">selected</c:if>>GHTK</option>
              <option value="GHN" <c:if test="${filter.shippingProvider eq 'GHN'}">selected</c:if>>GHN</option>
              <option value="VIETTEL_POST" <c:if test="${filter.shippingProvider eq 'VIETTEL_POST'}">selected</c:if>>Viettel Post</option>
              <option value="OTHER" <c:if test="${filter.shippingProvider eq 'OTHER'}">selected</c:if>>Khác</option>
            </select>
          </label>

          <label class="admin-order-filter-field">
            <span>Từ ngày</span>
            <input class="admin-input" type="date" name="dateFrom" value="${filter.dateFrom}">
          </label>

          <label class="admin-order-filter-field">
            <span>Đến ngày</span>
            <input class="admin-input" type="date" name="dateTo" value="${filter.dateTo}">
          </label>

          <label class="admin-order-filter-field">
            <span>Số dòng</span>
            <select class="admin-select" name="pageSize">
              <option value="10" <c:if test="${pageSize eq 10}">selected</c:if>>10 / trang</option>
              <option value="20" <c:if test="${pageSize eq 20}">selected</c:if>>20 / trang</option>
              <option value="50" <c:if test="${pageSize eq 50}">selected</c:if>>50 / trang</option>
              <option value="100" <c:if test="${pageSize eq 100}">selected</c:if>>100 / trang</option>
            </select>
          </label>

          <div class="admin-order-filter-actions">
            <button class="admin-btn admin-btn--ok admin-order-filter-btn" type="submit">Lọc</button>
            <a class="admin-btn admin-order-filter-btn" href="${ctx}/admin/orders">Xóa lọc</a>
          </div>
        </form>
      </div>
    </div>

    <div class="admin-order-card admin-card">
      <div class="admin-card__body">
        <div class="admin-order-section-head">
          <div>
            <h2 class="admin-order-section-title">Danh sách đơn hàng</h2>
            <p class="admin-order-section-desc">
              Chọn nhiều đơn rồi dùng thao tác hàng loạt cho các đơn đang cùng trạng thái hợp lệ.
            </p>
          </div>
        </div>

        <form id="adminBulkOrderForm" class="admin-order-bulk-form" method="post" action="${ctx}/admin/orders">
          <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
          <input type="hidden" name="action" value="bulkWorkflow">
          <input type="hidden" name="returnUrl" value="/admin/orders?${filterQueryString}&page=${currentPage}">

          <div class="admin-order-bulk-left">
            <strong>Thao tác hàng loạt</strong>
            <span>Chỉ xử lý các đơn đúng điều kiện, đơn sai luồng sẽ tự bỏ qua.</span>
          </div>

          <div class="admin-order-bulk-controls">
            <select class="admin-select" name="bulkAction" required>
              <option value="">Chọn thao tác</option>
              <option value="confirmOrder">Xác nhận đơn đã chọn</option>
              <option value="startShipping">Chuyển sang đang giao</option>
              <option value="markDelivered">Đánh dấu giao thành công</option>
              <option value="markFailed">Đánh dấu giao thất bại</option>
              <option value="cancelOrder">Hủy đơn đã chọn</option>
            </select>
            <input class="admin-input" type="text" name="trackingNote" placeholder="Ghi chú tracking, có thể bỏ trống">
            <button class="admin-btn admin-btn--primary" type="submit"
                    onclick="return confirm('Xác nhận xử lý các đơn hàng đã chọn?');">
              Áp dụng
            </button>
          </div>
        </form>

        <c:choose>
          <c:when test="${empty orders}">
            <div class="admin-order-empty">
              <div class="admin-order-empty__icon">📦</div>
              <div>
                <h3>Không tìm thấy đơn hàng</h3>
                <p>Không có đơn hàng phù hợp với điều kiện lọc hiện tại.</p>
              </div>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-order-table-wrap">
              <table class="admin-table admin-order-table admin-order-table--bulk">
                <thead>
                <tr>
                  <th class="admin-order-col-check">
                    <label class="admin-order-check-label" title="Chọn tất cả trên trang hiện tại">
                      <input type="checkbox" data-order-select-all>
                    </label>
                  </th>
                  <th class="admin-order-col-id">Mã</th>
                  <th class="admin-order-col-customer">Khách hàng</th>
                  <th class="admin-order-col-total">Tổng tiền</th>
                  <th class="admin-order-col-payment">Thanh toán</th>
                  <th class="admin-order-col-status">Trạng thái</th>
                  <th class="admin-order-col-shipping">Vận chuyển</th>
                  <th class="admin-order-col-timeline">Quy trình</th>
                  <th class="admin-order-col-date">Thời gian</th>
                  <th class="admin-order-col-actions">Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="o" items="${orders}">
                  <c:set var="orderStatus" value="${fn:toLowerCase(o.status)}"/>
                  <c:set var="shippingStatus" value="${fn:toUpperCase(o.shippingStatus)}"/>
                  <c:set var="paymentStatus" value="${fn:toUpperCase(o.paymentStatus)}"/>
                  <c:set var="paymentMethod" value="${fn:toUpperCase(o.paymentMethod)}"/>
                  <c:set var="shippingProvider" value="${fn:toUpperCase(o.shippingProvider)}"/>
                  <c:set var="shippingMethod" value="${fn:toUpperCase(o.shippingMethod)}"/>

                  <c:set var="isProcessing" value="${orderStatus eq 'processing' or orderStatus eq 'pending'}"/>
                  <c:set var="isConfirmed" value="${orderStatus eq 'confirmed'}"/>
                  <c:set var="isOrderShipping" value="${orderStatus eq 'shipping'}"/>
                  <c:set var="isCompleted" value="${orderStatus eq 'completed'}"/>
                  <c:set var="isCancelled" value="${orderStatus eq 'cancelled' or orderStatus eq 'canceled'}"/>
                  <c:set var="isPendingPickup" value="${shippingStatus eq 'PENDING_PICKUP' or shippingStatus eq 'PENDING' or shippingStatus eq 'CREATED' or shippingStatus eq 'PICKING'}"/>
                  <c:set var="isDelivering" value="${shippingStatus eq 'DELIVERING' or shippingStatus eq 'SHIPPING' or shippingStatus eq 'IN_TRANSIT'}"/>
                  <c:set var="isDelivered" value="${shippingStatus eq 'DELIVERED' or shippingStatus eq 'SUCCESS' or shippingStatus eq 'COMPLETED'}"/>
                  <c:set var="isDeliveryFailed" value="${shippingStatus eq 'FAILED' or shippingStatus eq 'DELIVERY_FAILED' or shippingStatus eq 'RETURNED'}"/>
                  <c:set var="isShippingCanceled" value="${shippingStatus eq 'CANCELED' or shippingStatus eq 'CANCELLED'}"/>

                  <c:set var="step1" value="${not isCancelled}"/>
                  <c:set var="step2" value="${isConfirmed or isOrderShipping or isCompleted or isPendingPickup or isDelivering or isDelivered or isDeliveryFailed}"/>
                  <c:set var="step3" value="${isOrderShipping or isCompleted or isDelivering or isDelivered or isDeliveryFailed}"/>
                  <c:set var="step4" value="${isCompleted or isDelivered}"/>

                  <c:set var="orderCss" value="warning"/>
                  <c:if test="${isConfirmed}"><c:set var="orderCss" value="primary"/></c:if>
                  <c:if test="${isOrderShipping}"><c:set var="orderCss" value="info"/></c:if>
                  <c:if test="${isCompleted}"><c:set var="orderCss" value="ok"/></c:if>
                  <c:if test="${isCancelled}"><c:set var="orderCss" value="danger"/></c:if>

                  <c:set var="paymentCss" value="warning"/>
                  <c:if test="${paymentStatus eq 'PAID'}"><c:set var="paymentCss" value="ok"/></c:if>
                  <c:if test="${paymentStatus eq 'FAILED' or paymentStatus eq 'CANCELED' or paymentStatus eq 'CANCELLED'}"><c:set var="paymentCss" value="danger"/></c:if>
                  <c:if test="${paymentStatus eq 'REFUNDED'}"><c:set var="paymentCss" value="info"/></c:if>

                  <c:set var="shippingCss" value="muted"/>
                  <c:if test="${isPendingPickup}"><c:set var="shippingCss" value="warning"/></c:if>
                  <c:if test="${isDelivering}"><c:set var="shippingCss" value="info"/></c:if>
                  <c:if test="${isDelivered}"><c:set var="shippingCss" value="ok"/></c:if>
                  <c:if test="${isDeliveryFailed or isShippingCanceled}"><c:set var="shippingCss" value="danger"/></c:if>

                  <c:set var="rowClass" value=""/>
                  <c:if test="${isCancelled or isShippingCanceled}"><c:set var="rowClass" value="admin-order-row--cancelled"/></c:if>
                  <c:if test="${isDeliveryFailed}"><c:set var="rowClass" value="${rowClass} admin-order-row--failed"/></c:if>

                  <tr class="${rowClass}">
                    <td class="admin-order-check-cell">
                      <input class="admin-order-row-check" type="checkbox" name="selectedOrderIds" value="${o.id}"
                             form="adminBulkOrderForm" data-order-row-check>
                    </td>

                    <td class="admin-order-id-cell">
                      <div class="admin-order-id-content">
                        <strong>#${o.id}</strong>
                        <span>User ${o.userId}</span>
                      </div>
                    </td>

                    <td>
                      <div class="admin-order-customer">
                        <strong><c:out value="${o.fullName}"/></strong>
                        <c:if test="${not empty o.phone}"><span>📞 <c:out value="${o.phone}"/></span></c:if>
                        <c:if test="${not empty o.address}"><span class="admin-order-address">📍 <c:out value="${o.address}"/></span></c:if>
                      </div>
                    </td>

                    <td class="admin-order-total-cell">
                      <div class="admin-order-total-content">
                        <div class="admin-order-money">
                          <strong><fmt:formatNumber value="${o.total}" type="number" groupingUsed="true" maxFractionDigits="0"/></strong>
                          <span>₫</span>
                        </div>
                        <small>Ship: <fmt:formatNumber value="${o.shippingFee}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</small>
                      </div>
                    </td>

                    <td>
                      <div class="admin-order-payment">
                        <span class="admin-pill">
                          <c:choose>
                            <c:when test="${paymentMethod eq 'COD'}">Thanh toán khi nhận hàng</c:when>
                            <c:when test="${paymentMethod eq 'VNPAY'}">Thanh toán qua VNPAY</c:when>
                            <c:otherwise>Chưa xác định</c:otherwise>
                          </c:choose>
                        </span>
                        <span class="admin-pill admin-pill--${paymentCss}">
                          <c:choose>
                            <c:when test="${paymentStatus eq 'PAID'}">Đã thanh toán</c:when>
                            <c:when test="${paymentStatus eq 'FAILED'}">Thanh toán thất bại</c:when>
                            <c:when test="${paymentStatus eq 'CANCELED' or paymentStatus eq 'CANCELLED'}">Đã hủy thanh toán</c:when>
                            <c:when test="${paymentStatus eq 'REFUNDED'}">Đã hoàn tiền</c:when>
                            <c:otherwise>Chờ thanh toán</c:otherwise>
                          </c:choose>
                        </span>
                      </div>
                    </td>

                    <td class="admin-status-cell">
                      <div class="admin-order-status-stack">
                        <span class="admin-pill admin-pill--${orderCss}">
                          <c:choose>
                            <c:when test="${isProcessing}">Chờ xác nhận</c:when>
                            <c:when test="${isConfirmed}">Đã xác nhận</c:when>
                            <c:when test="${isOrderShipping}">Đang giao</c:when>
                            <c:when test="${isCompleted}">Giao thành công</c:when>
                            <c:when test="${isCancelled}">Đã hủy</c:when>
                            <c:otherwise><c:out value="${o.status}"/></c:otherwise>
                          </c:choose>
                        </span>
                        <c:if test="${isDeliveryFailed}"><span class="admin-pill admin-pill--danger">Giao thất bại</span></c:if>
                      </div>
                    </td>

                    <td>
                      <div class="admin-order-shipping">
                        <strong>
                          <c:choose>
                            <c:when test="${shippingProvider eq 'GHTK'}">Giao hàng tiết kiệm</c:when>
                            <c:when test="${shippingProvider eq 'GHN'}">Giao hàng nhanh</c:when>
                            <c:when test="${shippingProvider eq 'VIETTEL_POST'}">Viettel Post</c:when>
                            <c:when test="${shippingProvider eq 'OTHER'}">Đơn vị vận chuyển khác</c:when>
                            <c:otherwise>Vận chuyển nội bộ</c:otherwise>
                          </c:choose>
                        </strong>
                        <span>
                          <c:choose>
                            <c:when test="${shippingMethod eq 'FAST'}">Giao hàng nhanh</c:when>
                            <c:when test="${shippingMethod eq 'EXPRESS'}">Hỏa tốc</c:when>
                            <c:otherwise>Giao hàng tiết kiệm</c:otherwise>
                          </c:choose>
                        </span>
                        <code><c:out value="${empty o.shippingCode ? 'Chưa có mã vận đơn' : o.shippingCode}"/></code>
                        <span class="admin-pill admin-pill--${shippingCss}">
                          <c:choose>
                            <c:when test="${isPendingPickup}">Chờ lấy hàng</c:when>
                            <c:when test="${isDelivering}">Đang giao hàng</c:when>
                            <c:when test="${isDelivered}">Giao thành công</c:when>
                            <c:when test="${isDeliveryFailed}">Giao thất bại</c:when>
                            <c:when test="${isShippingCanceled}">Đã hủy giao hàng</c:when>
                            <c:otherwise>Chưa có trạng thái</c:otherwise>
                          </c:choose>
                        </span>
                      </div>
                    </td>

                    <td>
                      <div class="admin-order-flow ${isCancelled or isShippingCanceled ? 'is-cancelled' : ''} ${isDeliveryFailed ? 'is-failed' : ''}">
                        <span class="admin-order-flow__step ${step1 ? 'is-done' : ''}">1</span>
                        <span class="admin-order-flow__line ${step2 ? 'is-done' : ''}"></span>
                        <span class="admin-order-flow__step ${step2 ? 'is-done' : ''}">2</span>
                        <span class="admin-order-flow__line ${step3 ? 'is-done' : ''}"></span>
                        <span class="admin-order-flow__step ${step3 ? 'is-done' : ''}">3</span>
                        <span class="admin-order-flow__line ${step4 ? 'is-done' : ''}"></span>
                        <span class="admin-order-flow__step ${step4 ? 'is-done' : ''}">4</span>
                      </div>
                      <div class="admin-order-flow-labels">
                        <span>Chờ xác nhận</span>
                        <span>Đã xác nhận</span>
                        <span>Đang giao</span>
                        <span>Hoàn tất</span>
                      </div>
                    </td>

                    <td>
                      <div class="admin-order-time">
                        <span>Đặt:<strong><fmt:formatDate value="${o.createdAtDate}" pattern="dd/MM/yyyy HH:mm"/></strong></span>
                        <span>Gửi:<strong><c:choose><c:when test="${not empty o.shippedAtDate}"><fmt:formatDate value="${o.shippedAtDate}" pattern="dd/MM/yyyy HH:mm"/></c:when><c:otherwise>Chưa gửi</c:otherwise></c:choose></strong></span>
                        <span>Giao:<strong><c:choose><c:when test="${not empty o.deliveredAtDate}"><fmt:formatDate value="${o.deliveredAtDate}" pattern="dd/MM/yyyy HH:mm"/></c:when><c:otherwise>Chưa giao</c:otherwise></c:choose></strong></span>
                      </div>
                    </td>

                    <td class="admin-order-action-cell">
                      <div class="admin-order-actions">
                        <a class="admin-btn admin-order-action-btn admin-order-action-btn--view"
                           href="${ctx}/admin/orders?action=detail&id=${o.id}">Chi tiết</a>

                        <c:choose>
                          <c:when test="${isProcessing}">
                            <form method="post" action="${ctx}/admin/order/update-status">
                              <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                              <input type="hidden" name="orderId" value="${o.id}">
                              <input type="hidden" name="workflowAction" value="confirmOrder">
                              <input type="hidden" name="returnUrl" value="/admin/orders?${filterQueryString}&page=${currentPage}">
                              <button class="admin-btn admin-btn--ok admin-order-action-btn" type="submit">Xác nhận</button>
                            </form>
                            <form method="post" action="${ctx}/admin/order/update-status" onsubmit="return confirm('Bạn có chắc muốn hủy đơn hàng này?');">
                              <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                              <input type="hidden" name="orderId" value="${o.id}">
                              <input type="hidden" name="workflowAction" value="cancelOrder">
                              <input type="hidden" name="returnUrl" value="/admin/orders?${filterQueryString}&page=${currentPage}">
                              <button class="admin-btn admin-btn--danger admin-order-action-btn" type="submit">Hủy</button>
                            </form>
                          </c:when>

                          <c:when test="${isConfirmed}">
                            <form method="post" action="${ctx}/admin/order/update-status">
                              <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                              <input type="hidden" name="orderId" value="${o.id}">
                              <input type="hidden" name="workflowAction" value="startShipping">
                              <input type="hidden" name="returnUrl" value="/admin/orders?${filterQueryString}&page=${currentPage}">
                              <button class="admin-btn admin-btn--primary admin-order-action-btn" type="submit">Giao</button>
                            </form>
                            <form method="post" action="${ctx}/admin/order/update-status" onsubmit="return confirm('Bạn có chắc muốn hủy đơn hàng này?');">
                              <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                              <input type="hidden" name="orderId" value="${o.id}">
                              <input type="hidden" name="workflowAction" value="cancelOrder">
                              <input type="hidden" name="returnUrl" value="/admin/orders?${filterQueryString}&page=${currentPage}">
                              <button class="admin-btn admin-btn--danger admin-order-action-btn" type="submit">Hủy</button>
                            </form>
                          </c:when>

                          <c:when test="${isOrderShipping and (isDelivering or empty shippingStatus)}">
                            <form method="post" action="${ctx}/admin/order/update-status">
                              <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                              <input type="hidden" name="orderId" value="${o.id}">
                              <input type="hidden" name="workflowAction" value="markDelivered">
                              <input type="hidden" name="returnUrl" value="/admin/orders?${filterQueryString}&page=${currentPage}">
                              <button class="admin-btn admin-btn--ok admin-order-action-btn" type="submit">Đã giao</button>
                            </form>
                            <form method="post" action="${ctx}/admin/order/update-status" onsubmit="return confirm('Xác nhận giao hàng thất bại?');">
                              <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                              <input type="hidden" name="orderId" value="${o.id}">
                              <input type="hidden" name="workflowAction" value="markFailed">
                              <input type="hidden" name="returnUrl" value="/admin/orders?${filterQueryString}&page=${currentPage}">
                              <button class="admin-btn admin-btn--danger admin-order-action-btn" type="submit">Thất bại</button>
                            </form>
                          </c:when>

                          <c:when test="${isDeliveryFailed}">
                            <form method="post" action="${ctx}/admin/order/update-status">
                              <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                              <input type="hidden" name="orderId" value="${o.id}">
                              <input type="hidden" name="workflowAction" value="startShipping">
                              <input type="hidden" name="returnUrl" value="/admin/orders?${filterQueryString}&page=${currentPage}">
                              <button class="admin-btn admin-btn--primary admin-order-action-btn" type="submit">Giao lại</button>
                            </form>
                            <form method="post" action="${ctx}/admin/order/update-status" onsubmit="return confirm('Bạn có chắc muốn hủy đơn giao thất bại này?');">
                              <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                              <input type="hidden" name="orderId" value="${o.id}">
                              <input type="hidden" name="workflowAction" value="cancelOrder">
                              <input type="hidden" name="returnUrl" value="/admin/orders?${filterQueryString}&page=${currentPage}">
                              <button class="admin-btn admin-btn--danger admin-order-action-btn" type="submit">Hủy</button>
                            </form>
                          </c:when>

                          <c:otherwise>
                            <span class="admin-order-action-done">Đã kết thúc</span>
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </td>
                  </tr>
                </c:forEach>
                </tbody>
              </table>
            </div>

            <c:if test="${totalPages gt 1}">
              <c:set var="pageStart" value="${currentPage - 2}"/>
              <c:if test="${pageStart lt 1}"><c:set var="pageStart" value="1"/></c:if>
              <c:set var="pageEnd" value="${currentPage + 2}"/>
              <c:if test="${pageEnd gt totalPages}"><c:set var="pageEnd" value="${totalPages}"/></c:if>

              <div class="admin-order-pagination">
                <a class="admin-order-page-link ${currentPage le 1 ? 'is-disabled' : ''}"
                   href="${ctx}/admin/orders?${filterQueryString}&page=${currentPage - 1}">
                  Trước
                </a>

                <c:if test="${pageStart gt 1}">
                  <a class="admin-order-page-link" href="${ctx}/admin/orders?${filterQueryString}&page=1">1</a>
                  <span class="admin-order-page-dots">...</span>
                </c:if>

                <c:forEach var="p" begin="${pageStart}" end="${pageEnd}">
                  <a class="admin-order-page-link ${p eq currentPage ? 'is-active' : ''}"
                     href="${ctx}/admin/orders?${filterQueryString}&page=${p}">
                      ${p}
                  </a>
                </c:forEach>

                <c:if test="${pageEnd lt totalPages}">
                  <span class="admin-order-page-dots">...</span>
                  <a class="admin-order-page-link" href="${ctx}/admin/orders?${filterQueryString}&page=${totalPages}">${totalPages}</a>
                </c:if>

                <a class="admin-order-page-link ${currentPage ge totalPages ? 'is-disabled' : ''}"
                   href="${ctx}/admin/orders?${filterQueryString}&page=${currentPage + 1}">
                  Sau
                </a>
              </div>
            </c:if>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>
</main>

<script>
  (function () {
    const master = document.querySelector('[data-order-select-all]');
    const checks = Array.from(document.querySelectorAll('[data-order-row-check]'));

    if (!master || checks.length === 0) {
      return;
    }

    master.addEventListener('change', function () {
      checks.forEach(function (checkbox) {
        checkbox.checked = master.checked;
      });
    });

    checks.forEach(function (checkbox) {
      checkbox.addEventListener('change', function () {
        const checkedCount = checks.filter(function (item) {
          return item.checked;
        }).length;

        master.checked = checkedCount === checks.length;
        master.indeterminate = checkedCount > 0 && checkedCount < checks.length;
      });
    });
  })();
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

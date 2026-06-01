<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Chi tiết đơn hàng" scope="request" />
<c:set var="activeMenu" value="orders" scope="request" />
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request" />

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

<c:set var="orderStatus" value="${fn:toLowerCase(order.status)}" />
<c:set var="shippingStatus" value="${fn:toUpperCase(order.shippingStatus)}" />
<c:set var="paymentStatus" value="${fn:toUpperCase(order.paymentStatus)}" />
<c:set var="paymentMethod" value="${fn:toUpperCase(order.paymentMethod)}" />
<c:set var="shippingProvider" value="${fn:toUpperCase(order.shippingProvider)}" />
<c:set var="shippingMethod" value="${fn:toUpperCase(order.shippingMethod)}" />

<c:set var="isProcessing" value="${orderStatus eq 'processing' or orderStatus eq 'pending'}" />
<c:set var="isConfirmed" value="${orderStatus eq 'confirmed'}" />
<c:set var="isOrderShipping" value="${orderStatus eq 'shipping'}" />
<c:set var="isCompleted" value="${orderStatus eq 'completed'}" />
<c:set var="isCancelled" value="${orderStatus eq 'cancelled' or orderStatus eq 'canceled'}" />
<c:set var="isPendingPickup" value="${shippingStatus eq 'PENDING_PICKUP' or shippingStatus eq 'PENDING' or shippingStatus eq 'CREATED' or shippingStatus eq 'PICKING'}" />
<c:set var="isDelivering" value="${shippingStatus eq 'DELIVERING' or shippingStatus eq 'SHIPPING' or shippingStatus eq 'IN_TRANSIT'}" />
<c:set var="isDelivered" value="${shippingStatus eq 'DELIVERED' or shippingStatus eq 'SUCCESS' or shippingStatus eq 'COMPLETED'}" />
<c:set var="isDeliveryFailed" value="${shippingStatus eq 'FAILED' or shippingStatus eq 'DELIVERY_FAILED' or shippingStatus eq 'RETURNED'}" />
<c:set var="isShippingCanceled" value="${shippingStatus eq 'CANCELED' or shippingStatus eq 'CANCELLED'}" />

<c:set var="orderCss" value="warning"/>
<c:if test="${isConfirmed}"><c:set var="orderCss" value="primary"/></c:if>
<c:if test="${isOrderShipping}"><c:set var="orderCss" value="info"/></c:if>
<c:if test="${isCompleted}"><c:set var="orderCss" value="ok"/></c:if>
<c:if test="${isCancelled}"><c:set var="orderCss" value="danger"/></c:if>

<c:set var="shippingCss" value="muted"/>
<c:if test="${isPendingPickup}"><c:set var="shippingCss" value="warning"/></c:if>
<c:if test="${isDelivering}"><c:set var="shippingCss" value="info"/></c:if>
<c:if test="${isDelivered}"><c:set var="shippingCss" value="ok"/></c:if>
<c:if test="${isDeliveryFailed or isShippingCanceled}"><c:set var="shippingCss" value="danger"/></c:if>

<c:set var="paymentCss" value="warning"/>
<c:if test="${paymentStatus eq 'PAID'}"><c:set var="paymentCss" value="ok"/></c:if>
<c:if test="${paymentStatus eq 'FAILED' or paymentStatus eq 'CANCELED' or paymentStatus eq 'CANCELLED'}"><c:set var="paymentCss" value="danger"/></c:if>
<c:if test="${paymentStatus eq 'REFUNDED'}"><c:set var="paymentCss" value="info"/></c:if>

<main class="admin-main">
  <div class="admin-container admin-order-detail-page">

    <div class="admin-order-detail-hero">
      <div class="admin-order-detail-hero__content">
        <span class="admin-order-detail-eyebrow">ORDER DETAIL</span>
        <h1 class="admin-h1 admin-order-detail-title">Chi tiết đơn hàng #${order.id}</h1>
        <div class="admin-order-detail-status-row">
          <span class="admin-pill admin-pill--${orderCss}">
            <c:choose>
              <c:when test="${isProcessing}">Chờ xác nhận</c:when>
              <c:when test="${isConfirmed}">Đã xác nhận</c:when>
              <c:when test="${isOrderShipping}">Đang giao</c:when>
              <c:when test="${isCompleted}">Giao thành công</c:when>
              <c:when test="${isCancelled}">Đã hủy</c:when>
              <c:otherwise><c:out value="${order.status}" /></c:otherwise>
            </c:choose>
          </span>
          <span class="admin-pill admin-pill--${shippingCss}">
            <c:choose>
              <c:when test="${isPendingPickup}">Chờ lấy hàng</c:when>
              <c:when test="${isDelivering}">Đang giao hàng</c:when>
              <c:when test="${isDelivered}">Giao thành công</c:when>
              <c:when test="${isDeliveryFailed}">Giao thất bại</c:when>
              <c:when test="${isShippingCanceled}">Đã hủy giao hàng</c:when>
              <c:otherwise>Chưa có trạng thái vận chuyển</c:otherwise>
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
      </div>
      <div class="admin-order-detail-hero__actions">
        <a class="admin-btn" href="${pageContext.request.contextPath}/admin/orders">Quay lại</a>
      </div>
    </div>

    <c:if test="${not empty admin_order_success}">
      <div class="admin-alert admin-alert--success"><c:out value="${admin_order_success}" /></div>
    </c:if>
    <c:if test="${not empty admin_order_error}">
      <div class="admin-alert admin-alert--danger"><c:out value="${admin_order_error}" /></div>
    </c:if>

    <div class="admin-order-detail-layout">
      <section class="admin-order-detail-main">

        <div class="admin-order-detail-card">
          <div class="admin-order-detail-card__header">
            <div>
              <h2>Thông tin đơn hàng</h2>
              <p>Thông tin khách hàng, thanh toán và tổng tiền đơn hàng.</p>
            </div>
          </div>
          <div class="admin-order-detail-card__body">
            <div class="admin-order-detail-grid">
              <div class="admin-order-info-item"><span>Khách hàng</span><strong><c:out value="${order.fullName}" /></strong></div>
              <div class="admin-order-info-item"><span>Số điện thoại</span><strong><c:out value="${order.phone}" /></strong></div>
              <div class="admin-order-info-item admin-order-info-item--full"><span>Địa chỉ giao hàng</span><strong><c:out value="${order.address}" /></strong></div>
              <div class="admin-order-info-item"><span>Tổng tiền</span><strong><fmt:formatNumber value="${order.total}" type="number" groupingUsed="true" maxFractionDigits="0" /> ₫</strong></div>
              <div class="admin-order-info-item"><span>Giảm giá</span><strong><fmt:formatNumber value="${order.couponDiscount}" type="number" groupingUsed="true" maxFractionDigits="0" /> ₫</strong></div>
              <div class="admin-order-info-item"><span>Ngày tạo</span><strong><fmt:formatDate value="${order.createdAtDate}" pattern="dd/MM/yyyy HH:mm" /></strong></div>
              <div class="admin-order-info-item"><span>Thanh toán</span><strong><c:out value="${order.paymentMethod}" /> - <c:out value="${order.paymentStatus}" /></strong></div>
              <div class="admin-order-info-item"><span>VNPAY TxnRef</span><strong><c:out value="${empty order.vnpTxnRef ? 'Không có' : order.vnpTxnRef}" /></strong></div>
            </div>
          </div>
        </div>

        <div class="admin-order-detail-card">
          <div class="admin-order-detail-card__header">
            <div>
              <h2>Tracking vận chuyển</h2>
              <p>Theo dõi đơn vị vận chuyển, mã vận đơn, ngày gửi và ngày giao.</p>
            </div>
          </div>
          <div class="admin-order-detail-card__body">
            <div class="admin-order-shipping-summary">
              <div><span>Phương thức</span><strong><c:choose><c:when test="${shippingMethod eq 'FAST'}">Giao hàng nhanh</c:when><c:when test="${shippingMethod eq 'EXPRESS'}">Hỏa tốc</c:when><c:otherwise>Giao hàng tiết kiệm</c:otherwise></c:choose></strong></div>
              <div><span>Đơn vị giao</span><strong><c:choose><c:when test="${shippingProvider eq 'GHTK'}">Giao hàng tiết kiệm</c:when><c:when test="${shippingProvider eq 'GHN'}">Giao hàng nhanh</c:when><c:when test="${shippingProvider eq 'VIETTEL_POST'}">Viettel Post</c:when><c:when test="${shippingProvider eq 'OTHER'}">Đơn vị khác</c:when><c:otherwise>Vận chuyển nội bộ</c:otherwise></c:choose></strong></div>
              <div><span>Mã vận đơn</span><strong><c:out value="${empty order.shippingCode ? 'Chưa có' : order.shippingCode}" /></strong></div>
              <div><span>Phí ship</span><strong><fmt:formatNumber value="${order.shippingFee}" type="number" groupingUsed="true" maxFractionDigits="0" /> ₫</strong></div>
              <div><span>Ngày gửi</span><strong><c:choose><c:when test="${not empty order.shippedAtDate}"><fmt:formatDate value="${order.shippedAtDate}" pattern="dd/MM/yyyy HH:mm" /></c:when><c:otherwise>Chưa gửi</c:otherwise></c:choose></strong></div>
              <div><span>Ngày giao</span><strong><c:choose><c:when test="${not empty order.deliveredAtDate}"><fmt:formatDate value="${order.deliveredAtDate}" pattern="dd/MM/yyyy HH:mm" /></c:when><c:otherwise>Chưa giao</c:otherwise></c:choose></strong></div>
            </div>

            <div class="admin-order-tracking-steps">
              <div class="admin-order-tracking-step ${isPendingPickup ? 'is-active' : (isDelivering or isDelivered or isDeliveryFailed ? 'is-done' : '')}"><strong>Chờ lấy hàng</strong><small>Shop chuẩn bị và bàn giao cho đơn vị vận chuyển.</small></div>
              <div class="admin-order-tracking-step ${isDelivering ? 'is-active' : (isDelivered or isDeliveryFailed ? 'is-done' : '')}"><strong>Đang giao</strong><small>Đơn hàng đang trên đường giao đến khách.</small></div>
              <div class="admin-order-tracking-step ${isDelivered ? 'is-done' : (isDeliveryFailed ? 'is-failed' : '')}"><strong>${isDeliveryFailed ? 'Giao thất bại' : 'Giao thành công'}</strong><small>${isDeliveryFailed ? 'Shop cần liên hệ lại khách hàng để xử lý.' : 'Chờ đơn hàng được giao thành công.'}</small></div>
              <div class="admin-order-tracking-step ${isShippingCanceled ? 'is-failed' : ''}"><strong>Hủy vận chuyển</strong><small>Chỉ dùng khi vận chuyển đã bị hủy.</small></div>
            </div>

            <div class="admin-order-workflow-actions">
              <c:if test="${isProcessing}">
                <form method="post" action="${pageContext.request.contextPath}/admin/order/update-status">
                  <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                  <input type="hidden" name="orderId" value="${order.id}">
                  <input type="hidden" name="workflowAction" value="confirmOrder">
                  <input type="hidden" name="returnUrl" value="/admin/orders?action=detail&id=${order.id}">
                  <button type="submit" class="admin-btn admin-btn--ok">Xác nhận đơn</button>
                </form>
              </c:if>

              <c:if test="${isConfirmed or isDeliveryFailed}">
                <form method="post" action="${pageContext.request.contextPath}/admin/order/update-status">
                  <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                  <input type="hidden" name="orderId" value="${order.id}">
                  <input type="hidden" name="workflowAction" value="startShipping">
                  <input type="hidden" name="returnUrl" value="/admin/orders?action=detail&id=${order.id}">
                  <button type="submit" class="admin-btn admin-btn--primary">${isDeliveryFailed ? 'Giao lại' : 'Bắt đầu giao'}</button>
                </form>
              </c:if>

              <c:if test="${isOrderShipping and (isDelivering or empty shippingStatus)}">
                <form method="post" action="${pageContext.request.contextPath}/admin/order/update-status">
                  <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                  <input type="hidden" name="orderId" value="${order.id}">
                  <input type="hidden" name="workflowAction" value="markDelivered">
                  <input type="hidden" name="returnUrl" value="/admin/orders?action=detail&id=${order.id}">
                  <button type="submit" class="admin-btn admin-btn--ok">Giao thành công</button>
                </form>
                <form method="post" action="${pageContext.request.contextPath}/admin/order/update-status" onsubmit="return confirm('Xác nhận giao hàng thất bại?');">
                  <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                  <input type="hidden" name="orderId" value="${order.id}">
                  <input type="hidden" name="workflowAction" value="markFailed">
                  <input type="hidden" name="returnUrl" value="/admin/orders?action=detail&id=${order.id}">
                  <button type="submit" class="admin-btn admin-btn--danger">Giao thất bại</button>
                </form>
              </c:if>

              <c:if test="${isProcessing or isConfirmed or isDeliveryFailed}">
                <form method="post" action="${pageContext.request.contextPath}/admin/order/update-status" onsubmit="return confirm('Bạn có chắc muốn hủy đơn hàng này?');">
                  <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                  <input type="hidden" name="orderId" value="${order.id}">
                  <input type="hidden" name="workflowAction" value="cancelOrder">
                  <input type="hidden" name="returnUrl" value="/admin/orders?action=detail&id=${order.id}">
                  <button type="submit" class="admin-btn admin-btn--danger">Hủy đơn</button>
                </form>
              </c:if>
            </div>

            <hr class="admin-divider" />

            <h3 class="admin-order-subtitle-heading">Thông tin vận chuyển</h3>
            <form method="post" action="${pageContext.request.contextPath}/admin/orders" class="admin-form">
              <%@ include file="/jsp/common/csrf.jspf" %>
              <input type="hidden" name="action" value="updateShippingInfo" />
              <input type="hidden" name="id" value="${order.id}" />

              <div class="admin-form-grid admin-form-grid--2">
                <div class="admin-field">
                  <div class="admin-label">Đơn vị vận chuyển</div>
                  <select name="shippingProvider" class="admin-select">
                    <option value="INTERNAL" ${shippingProvider eq 'INTERNAL' ? 'selected' : ''}>Vận chuyển nội bộ</option>
                    <option value="GHTK" ${shippingProvider eq 'GHTK' ? 'selected' : ''}>Giao hàng tiết kiệm</option>
                    <option value="GHN" ${shippingProvider eq 'GHN' ? 'selected' : ''}>Giao hàng nhanh</option>
                    <option value="VIETTEL_POST" ${shippingProvider eq 'VIETTEL_POST' ? 'selected' : ''}>Viettel Post</option>
                    <option value="OTHER" ${shippingProvider eq 'OTHER' ? 'selected' : ''}>Khác</option>
                  </select>
                </div>
                <div class="admin-field">
                  <div class="admin-label">Phương thức giao hàng</div>
                  <select name="shippingMethod" class="admin-select">
                    <option value="ECONOMY" ${shippingMethod eq 'ECONOMY' ? 'selected' : ''}>Giao hàng tiết kiệm</option>
                    <option value="FAST" ${shippingMethod eq 'FAST' ? 'selected' : ''}>Giao hàng nhanh</option>
                    <option value="EXPRESS" ${shippingMethod eq 'EXPRESS' ? 'selected' : ''}>Hỏa tốc</option>
                  </select>
                </div>
                <div class="admin-field">
                  <div class="admin-label">Mã vận đơn</div>
                  <input type="text" name="shippingCode" class="admin-input" value="${order.shippingCode}" placeholder="Ví dụ: MC-SHIP-000123">
                </div>
                <div class="admin-field">
                  <div class="admin-label">Phí vận chuyển</div>
                  <input type="text" name="shippingFee" class="admin-input" value="${order.shippingFee}" placeholder="Ví dụ: 35000">
                </div>
              </div>

              <div class="admin-actions">
                <button type="submit" class="admin-btn admin-btn--primary">Lưu thông tin vận chuyển</button>
              </div>
            </form>

            <hr class="admin-divider" />

            <h3 class="admin-order-subtitle-heading">Lịch sử tracking</h3>
            <c:choose>
              <c:when test="${empty trackingList}">
                <div class="admin-empty">Chưa có lịch sử tracking cho đơn hàng này.</div>
              </c:when>
              <c:otherwise>
                <div class="admin-order-tracking-history">
                  <c:forEach var="tracking" items="${trackingList}">
                    <div class="admin-order-tracking-history-item">
                      <div class="admin-order-tracking-history-time">
                        <c:choose>
                          <c:when test="${not empty tracking.createdAtDate}"><fmt:formatDate value="${tracking.createdAtDate}" pattern="dd/MM/yyyy HH:mm" /></c:when>
                          <c:otherwise>Không rõ thời gian</c:otherwise>
                        </c:choose>
                      </div>
                      <div class="admin-order-tracking-history-content">
                        <strong><c:out value="${tracking.shippingStatusLabel}" /></strong>
                        <p><c:out value="${tracking.note}" /></p>
                      </div>
                    </div>
                  </c:forEach>
                </div>
              </c:otherwise>
            </c:choose>
          </div>
        </div>

        <div class="admin-order-detail-card">
          <div class="admin-order-detail-card__header"><div><h2>Sản phẩm trong đơn hàng</h2><p>Chi tiết sản phẩm, biến thể, số lượng và thành tiền.</p></div></div>
          <div class="admin-order-detail-card__body">
            <c:set var="displayItems" value="${orderItems}" />
            <c:if test="${empty displayItems && not empty items}"><c:set var="displayItems" value="${items}" /></c:if>

            <c:choose>
              <c:when test="${empty displayItems}">
                <div class="admin-empty">Đơn hàng chưa có sản phẩm hoặc chưa load được chi tiết sản phẩm.</div>
              </c:when>
              <c:otherwise>
                <div class="admin-order-items-wrap">
                  <table class="admin-table admin-order-items-table">
                    <thead>
                    <tr><th>Ảnh</th><th>Sản phẩm</th><th>Biến thể</th><th>Đơn giá</th><th>SL</th><th>Thành tiền</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach var="item" items="${displayItems}">
                      <tr>
                        <td>
                          <c:choose>
                            <c:when test="${not empty item.imageUrl}"><img class="admin-order-item-img" src="${pageContext.request.contextPath}${item.imageUrl}" alt="${item.productName}" /></c:when>
                            <c:otherwise><div class="admin-order-item-img admin-order-item-img--empty">—</div></c:otherwise>
                          </c:choose>
                        </td>
                        <td><strong><c:out value="${item.productName}" /></strong><div class="admin-muted">Product ID: <c:out value="${item.productId}" /></div></td>
                        <td>
                          <c:choose>
                            <c:when test="${not empty item.variantId || not empty item.variantName || not empty item.variantSize || not empty item.variantType}">
                              <span class="admin-pill">Variant</span>
                              <div class="admin-order-variant">
                                <c:if test="${not empty item.variantName}"><div><strong>Tên:</strong> <c:out value="${item.variantName}" /></div></c:if>
                                <c:if test="${not empty item.variantSize}"><div><strong>Size:</strong> <c:out value="${item.variantSize}" /></div></c:if>
                                <c:if test="${not empty item.variantType}"><div><strong>Loại:</strong> <c:out value="${item.variantType}" /></div></c:if>
                                <c:if test="${not empty item.variantId}"><div class="admin-muted">Variant ID: <c:out value="${item.variantId}" /></div></c:if>
                              </div>
                            </c:when>
                            <c:otherwise><span class="admin-muted">Không có biến thể</span></c:otherwise>
                          </c:choose>
                        </td>
                        <td><fmt:formatNumber value="${item.price}" type="number" groupingUsed="true" maxFractionDigits="0" /> ₫</td>
                        <td><c:out value="${item.quantity}" /></td>
                        <td><strong><fmt:formatNumber value="${item.subtotal}" type="number" groupingUsed="true" maxFractionDigits="0" /> ₫</strong></td>
                      </tr>
                    </c:forEach>
                    </tbody>
                  </table>
                </div>
              </c:otherwise>
            </c:choose>
          </div>
        </div>
      </section>

      <aside class="admin-order-detail-side">
        <div class="admin-order-detail-card admin-order-detail-card--side">
          <div class="admin-order-detail-card__header"><div><h2>Quy tắc xử lý</h2><p>Admin thao tác theo workflow để tránh lệch trạng thái.</p></div></div>
          <div class="admin-order-detail-card__body">
            <div class="admin-order-rule-list">
              <div class="admin-order-rule admin-order-rule--ok"><strong>1. Xác nhận đơn</strong><span>Chỉ áp dụng khi đơn đang chờ xử lý.</span></div>
              <div class="admin-order-rule admin-order-rule--info"><strong>2. Bắt đầu giao</strong><span>Chỉ áp dụng khi đơn đã xác nhận hoặc giao thất bại cần giao lại.</span></div>
              <div class="admin-order-rule admin-order-rule--ok"><strong>3. Giao thành công</strong><span>Tự chuyển đơn sang hoàn tất và COD sang đã thanh toán.</span></div>
              <div class="admin-order-rule admin-order-rule--danger"><strong>4. Hủy đơn</strong><span>Chỉ hủy khi đơn chưa giao hoặc đã giao thất bại.</span></div>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp" />

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<section class="section">
  <div class="container order-page">

    <div class="order-orders">
      <div class="order-section-head">
        <div>
          <h3 class="order-section-title">📄 Lịch sử đơn hàng</h3>
          <p class="order-section-subtitle">
            Theo dõi trạng thái đơn hàng, vận chuyển và xem chi tiết tracking.
          </p>
        </div>
      </div>

      <c:choose>
        <c:when test="${not empty orders}">
          <div class="order-table-wrap">

            <table class="order-table">
              <thead>
              <tr>
                <th class="order-col-id">Mã đơn</th>
                <th class="order-col-date">Ngày đặt</th>
                <th class="order-col-total">Tổng thanh toán</th>
                <th class="text-center order-col-status">Trạng thái đơn</th>
                <th class="text-center order-col-shipping">Vận chuyển</th>
                <th class="order-col-tracking">Tracking</th>
                <th class="text-center order-col-actions">Chi tiết</th>
              </tr>
              </thead>

              <tbody>
              <c:forEach var="order" items="${orders}">
                <c:set var="orderStatus" value="${empty order.status ? '' : fn:toLowerCase(order.status)}" />
                <c:set var="shippingStatus" value="${empty order.shippingStatus ? '' : fn:toUpperCase(order.shippingStatus)}" />
                <c:set var="paymentStatus" value="${empty order.paymentStatus ? 'PENDING' : fn:toUpperCase(order.paymentStatus)}" />
                <c:set var="paymentMethod" value="${empty order.paymentMethod ? 'COD' : fn:toUpperCase(order.paymentMethod)}" />
                <c:set var="isProcessing" value="${orderStatus eq 'processing' or orderStatus eq 'pending'}" />
                <c:set var="isConfirmed" value="${orderStatus eq 'confirmed'}" />
                <c:set var="isOrderShipping" value="${orderStatus eq 'shipping'}" />
                <c:set var="isCompleted" value="${orderStatus eq 'completed'}" />
                <c:set var="isCancelled" value="${orderStatus eq 'cancelled' or orderStatus eq 'canceled'}" />

                <c:set var="isPendingPickup" value="${shippingStatus eq 'PENDING_PICKUP' or shippingStatus eq 'PENDING' or shippingStatus eq 'CREATED' or shippingStatus eq 'PICKING' or empty shippingStatus}" />
                <c:set var="isDelivering" value="${shippingStatus eq 'DELIVERING' or shippingStatus eq 'SHIPPING' or shippingStatus eq 'IN_TRANSIT'}" />
                <c:set var="isDelivered" value="${shippingStatus eq 'DELIVERED' or shippingStatus eq 'SUCCESS' or shippingStatus eq 'COMPLETED'}" />
                <c:set var="isDeliveryFailed" value="${shippingStatus eq 'FAILED' or shippingStatus eq 'DELIVERY_FAILED' or shippingStatus eq 'RETURNED'}" />
                <c:set var="isShippingCanceled" value="${shippingStatus eq 'CANCELED' or shippingStatus eq 'CANCELLED'}" />

                <c:set var="canRetryPayment" value="${paymentMethod eq 'VNPAY' and paymentStatus ne 'PAID' and not isCancelled and not isCompleted}" />
                <c:set var="canCancel" value="${(isProcessing or isConfirmed) and isPendingPickup}" />
                <c:set var="canReturn" value="${isCompleted and isDelivered}" />

                <tr id="order-${order.id}"
                    class="<c:if test='${isCancelled or isShippingCanceled}'>order-row-cancelled</c:if><c:if test='${isDeliveryFailed}'> order-row-failed</c:if>">
                  <td>
                    <span class="order-id">#${order.id}</span>
                  </td>

                  <td>
                    <c:choose>
                      <c:when test="${not empty order.createdAtDate}">
                        <fmt:formatDate value="${order.createdAtDate}" pattern="dd/MM/yyyy HH:mm" />
                      </c:when>
                      <c:otherwise>
                        <span class="order-muted">--</span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td class="order-price">
                    <fmt:formatNumber value="${empty order.total ? 0 : order.total}"
                                      type="number"
                                      groupingUsed="true"
                                      minFractionDigits="0"
                                      maxFractionDigits="0" />
                    ₫
                  </td>

                  <td class="text-center order-status-td">
                    <div class="status-cell">
                      <c:choose>
                        <c:when test="${isCompleted}">
                          <span class="status-badge status-ok">Hoàn thành</span>
                        </c:when>
                        <c:when test="${isCancelled}">
                          <span class="status-badge status-danger">Đã hủy</span>
                        </c:when>
                        <c:when test="${isOrderShipping}">
                          <span class="status-badge status-info">Đang giao</span>
                        </c:when>
                        <c:when test="${isConfirmed}">
                          <span class="status-badge status-warning">Đã xác nhận</span>
                        </c:when>
                        <c:when test="${isProcessing}">
                          <span class="status-badge status-warning">Chờ xác nhận</span>
                        </c:when>
                        <c:otherwise>
                          <span class="status-badge status-muted">
                            <c:out value="${empty order.statusLabel ? order.status : order.statusLabel}" />
                          </span>
                        </c:otherwise>
                      </c:choose>

                      <span class="status-subtext">
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

                  <td class="text-center shipping-status-td">
                    <div class="status-cell">
                      <c:choose>
                        <c:when test="${isDelivered}">
                          <span class="status-badge status-ok">Giao thành công</span>
                        </c:when>
                        <c:when test="${isDeliveryFailed}">
                          <span class="status-badge status-danger">Giao thất bại</span>
                        </c:when>
                        <c:when test="${isShippingCanceled}">
                          <span class="status-badge status-danger">Đã hủy vận chuyển</span>
                        </c:when>
                        <c:when test="${isDelivering}">
                          <span class="status-badge status-info">Đang giao</span>
                        </c:when>
                        <c:otherwise>
                          <span class="status-badge status-warning">Chờ lấy hàng</span>
                        </c:otherwise>
                      </c:choose>

                      <span class="status-subtext shipping-code-line">
                        Mã vận đơn:
                        <c:choose>
                          <c:when test="${not empty order.shippingCode}">
                            <c:out value="${order.shippingCode}" />
                          </c:when>
                          <c:otherwise>Đang cập nhật</c:otherwise>
                        </c:choose>
                      </span>
                    </div>
                  </td>

                  <td>
                    <a href="${pageContext.request.contextPath}/orders/detail?id=${order.id}#shipping-tracking"
                       class="tracking-link"
                       title="Xem tracking đơn hàng #${order.id}">

                      <div class="tracking-progress">
                        <span class="tracking-bar ${isPendingPickup ? 'active' : (isDelivering or isDelivered or isDeliveryFailed ? 'done' : '')}"></span>
                        <span class="tracking-bar ${isDelivering ? 'active' : (isDelivered or isDeliveryFailed ? 'done' : '')}"></span>

                        <c:choose>
                          <c:when test="${isDeliveryFailed or isShippingCanceled}">
                            <span class="tracking-bar failed"></span>
                          </c:when>
                          <c:otherwise>
                            <span class="tracking-bar ${isDelivered ? 'done' : ''}"></span>
                          </c:otherwise>
                        </c:choose>
                      </div>

                      <div class="tracking-labels">
                        <span>Chờ lấy</span>
                        <span>Đang giao</span>
                        <c:choose>
                          <c:when test="${isDeliveryFailed}">
                            <span>Thất bại</span>
                          </c:when>
                          <c:when test="${isShippingCanceled}">
                            <span>Đã hủy</span>
                          </c:when>
                          <c:otherwise>
                            <span>Hoàn tất</span>
                          </c:otherwise>
                        </c:choose>
                      </div>

                      <span class="tracking-action">Xem tracking →</span>
                    </a>
                  </td>

                  <td class="detail-cell">
                    <div class="order-action-stack">
                      <c:if test="${canRetryPayment}">
                        <a href="${pageContext.request.contextPath}/vnpay/payment?orderId=${order.id}"
                           class="btn-retry-payment">
                          Thanh toán lại
                        </a>
                      </c:if>

                      <c:if test="${canCancel}">
                        <form method="post"
                              action="${pageContext.request.contextPath}/orders/cancel"
                              class="order-inline-form"
                              onsubmit="return confirmCancelOrder(this);">
                          <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                          <input type="hidden" name="orderId" value="${order.id}" />
                          <input type="hidden" name="reason" value="" />
                          <button type="submit" class="btn-cancel-order">Hủy đơn</button>
                        </form>
                      </c:if>

                      <c:if test="${canReturn}">
                        <form method="post"
                              action="${pageContext.request.contextPath}/orders/return"
                              class="order-inline-form"
                              onsubmit="return confirmReturnOrder(this);">
                          <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                          <input type="hidden" name="orderId" value="${order.id}" />
                          <input type="hidden" name="refundMethod" value="MANUAL" />
                          <input type="hidden" name="reason" value="" />
                          <button type="submit" class="btn-return-order">Hoàn hàng</button>
                        </form>
                      </c:if>

                      <a href="${pageContext.request.contextPath}/orders/detail?id=${order.id}"
                         class="btn-detail">
                        Xem chi tiết
                      </a>
                    </div>
                  </td>
                </tr>
              </c:forEach>
              </tbody>
            </table>

            <p class="order-note">
              💡 Tổng tiền đã bao gồm toàn bộ khuyến mãi, ưu đãi và phí vận chuyển nếu có.
            </p>

          </div>
        </c:when>

        <c:otherwise>
          <p class="empty-text">Bạn chưa có đơn hàng nào.</p>
        </c:otherwise>
      </c:choose>

    </div>

  </div>
</section>


<script>
  function confirmCancelOrder(form) {
    const reason = window.prompt("Nhập lý do hủy đơn hàng:");
    if (reason === null) return false;

    const trimmed = reason.trim();
    if (trimmed.length < 5) {
      alert("Lý do hủy đơn cần ít nhất 5 ký tự.");
      return false;
    }

    form.querySelector('input[name="reason"]').value = trimmed;
    return confirm("Bạn chắc chắn muốn hủy đơn hàng này?");
  }

  function confirmReturnOrder(form) {
    const reason = window.prompt("Nhập lý do hoàn hàng:");
    if (reason === null) return false;

    const trimmed = reason.trim();
    if (trimmed.length < 10) {
      alert("Lý do hoàn hàng cần ít nhất 10 ký tự.");
      return false;
    }

    form.querySelector('input[name="reason"]').value = trimmed;
    return confirm("Gửi yêu cầu hoàn hàng cho đơn này?");
  }
</script>

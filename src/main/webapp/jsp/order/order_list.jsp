<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<section class="section">
  <div class="container order-page">

    <div class="order-orders">
      <div class="order-section-head order-section-head-filter">
        <div>
          <h3 class="order-section-title">
            <c:choose>
              <c:when test="${not empty orderFilter and orderFilter ne 'all'}">
                📄 <c:out value="${orderFilterLabel}" />
              </c:when>
              <c:otherwise>
                📄 Lịch sử đơn hàng
              </c:otherwise>
            </c:choose>
          </h3>
          <p class="order-section-subtitle">
            <c:out value="${orderFilterDescription}" />
          </p>
        </div>

        <div class="order-filter-count">
          <strong><c:out value="${empty orderFilteredCount ? 0 : orderFilteredCount}" /></strong>
          <span>/ <c:out value="${empty orderTotalCount ? 0 : orderTotalCount}" /> đơn</span>
        </div>
      </div>

      <div class="order-filter-tabs">
        <a href="${pageContext.request.contextPath}/orders"
           class="order-filter-tab ${empty orderFilter or orderFilter eq 'all' ? 'active' : ''}">
          Tất cả
        </a>
        <a href="${pageContext.request.contextPath}/orders?filter=processing"
           class="order-filter-tab ${orderFilter eq 'processing' ? 'active' : ''}">
          Chờ xác nhận
        </a>
        <a href="${pageContext.request.contextPath}/orders?filter=confirmed"
           class="order-filter-tab ${orderFilter eq 'confirmed' ? 'active' : ''}">
          Chờ lấy hàng
        </a>
        <a href="${pageContext.request.contextPath}/orders?filter=shipping"
           class="order-filter-tab ${orderFilter eq 'shipping' ? 'active' : ''}">
          Đang giao
        </a>
        <a href="${pageContext.request.contextPath}/orders?filter=completed"
           class="order-filter-tab ${orderFilter eq 'completed' ? 'active' : ''}">
          Đánh giá / Hoàn hàng
        </a>
        <a href="${pageContext.request.contextPath}/orders?filter=cancelled"
           class="order-filter-tab ${orderFilter eq 'cancelled' ? 'active' : ''}">
          Đã hủy
        </a>
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
                <th class="text-center order-col-view">Xem đơn</th>
                <th class="text-center order-col-actions">Thao tác</th>
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

                  <td class="detail-cell detail-view-cell">
                    <a href="${pageContext.request.contextPath}/orders/detail?id=${order.id}"
                       class="btn-order-view">
                      Xem chi tiết
                    </a>
                  </td>

                  <td class="action-cell">
                    <div class="order-action-stack order-action-stack--ops">
                      <c:choose>
                        <c:when test="${canRetryPayment or canCancel or canReturn}">
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
                                  data-order-id="${order.id}">
                              <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                              <input type="hidden" name="orderId" value="${order.id}" />
                              <input type="hidden" name="reason" value="" />
                              <button type="button"
                                      class="btn-cancel-order"
                                      onclick="openCancelOrderModal(this.closest('form'))">
                                Hủy đơn
                              </button>
                            </form>
                          </c:if>

                          <c:if test="${canReturn}">
                            <form method="post"
                                  action="${pageContext.request.contextPath}/orders/return"
                                  class="order-inline-form"
                                  data-order-id="${order.id}">
                              <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                              <input type="hidden" name="orderId" value="${order.id}" />
                              <input type="hidden" name="refundMethod" value="MANUAL" />
                              <input type="hidden" name="reason" value="" />
                              <button type="button"
                                      class="btn-return-order"
                                      onclick="openReturnOrderModal(this.closest('form'))">
                                Hoàn hàng
                              </button>
                            </form>
                          </c:if>
                        </c:when>
                        <c:otherwise>
                          <span class="order-no-action">Không có thao tác</span>
                        </c:otherwise>
                      </c:choose>
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
          <div class="order-empty-filter">
            <p class="empty-text">
              <c:choose>
                <c:when test="${not empty orderFilter and orderFilter ne 'all'}">
                  Chưa có đơn nào trong mục <strong><c:out value="${orderFilterLabel}" /></strong>.
                </c:when>
                <c:otherwise>
                  Bạn chưa có đơn hàng nào.
                </c:otherwise>
              </c:choose>
            </p>
            <c:if test="${not empty orderFilter and orderFilter ne 'all'}">
              <a href="${pageContext.request.contextPath}/orders" class="order-filter-back">
                Xem tất cả đơn hàng
              </a>
            </c:if>
          </div>
        </c:otherwise>
      </c:choose>

    </div>

  </div>
</section>


<!-- Modal hủy đơn / hoàn hàng chuyên nghiệp -->
<div id="cancelOrderModal" class="order-modal-overlay" aria-hidden="true">
  <div class="order-modal-card order-modal-card-cancel" role="dialog" aria-modal="true" aria-labelledby="cancelOrderModalTitle">
    <div class="order-modal-accent"></div>

    <div class="order-modal-head">
      <div class="order-modal-title-wrap">
        <div class="order-modal-icon order-modal-icon-cancel">!</div>
        <div>
          <span class="order-modal-kicker">Yêu cầu hủy đơn</span>
          <h3 id="cancelOrderModalTitle">Hủy đơn hàng <span id="cancelOrderCode">#</span></h3>
          <p>Áp dụng cho đơn chưa xử lý hoặc chưa giao thành công.</p>
        </div>
      </div>
      <button type="button" class="order-modal-close" onclick="closeOrderActionModal('cancel')" aria-label="Đóng">×</button>
    </div>

    <div class="order-modal-summary">
      <div class="order-modal-summary-item">
        <span>Mã đơn</span>
        <strong id="cancelOrderCodeSummary">#</strong>
      </div>
      <div class="order-modal-summary-item">
        <span>Trạng thái yêu cầu</span>
        <strong>Chờ shop xác nhận</strong>
      </div>
    </div>

    <div class="order-modal-notice order-modal-notice-warning">
      <strong>Lưu ý:</strong> sau khi gửi yêu cầu hủy, shop có thể cần kiểm tra trạng thái xử lý trước khi xác nhận hủy đơn.
    </div>

    <div class="order-modal-grid">
      <div class="order-modal-field order-modal-field-full">
        <label for="cancelReasonType">Lý do hủy đơn <span>*</span></label>
        <select id="cancelReasonType" class="order-modal-control">
          <option value="">-- Chọn lý do hủy đơn --</option>
          <option value="Đặt nhầm sản phẩm">Đặt nhầm sản phẩm</option>
          <option value="Muốn thay đổi sản phẩm hoặc số lượng">Muốn thay đổi sản phẩm hoặc số lượng</option>
          <option value="Muốn thay đổi địa chỉ hoặc thông tin nhận hàng">Muốn thay đổi địa chỉ hoặc thông tin nhận hàng</option>
          <option value="Không còn nhu cầu mua hàng">Không còn nhu cầu mua hàng</option>
          <option value="Muốn đổi phương thức thanh toán">Muốn đổi phương thức thanh toán</option>
          <option value="Khác">Khác</option>
        </select>
      </div>

      <div class="order-modal-field order-modal-field-full">
        <div class="order-modal-label-row">
          <label for="cancelReasonNote">Mô tả thêm</label>
          <small><span id="cancelReasonCount">0</span>/300</small>
        </div>
        <textarea id="cancelReasonNote"
                  class="order-modal-control order-modal-textarea"
                  rows="4"
                  maxlength="300"
                  placeholder="Ví dụ: Em đặt nhầm màu/số lượng, muốn đặt lại đơn khác..."></textarea>
        <small>Ghi thêm nếu cần. Nếu chọn “Khác”, nội dung mô tả phải có ít nhất 5 ký tự.</small>
      </div>
    </div>

    <p id="cancelOrderError" class="order-modal-error" aria-live="polite"></p>

    <div class="order-modal-actions">
      <button type="button" class="order-modal-btn order-modal-btn-secondary" onclick="closeOrderActionModal('cancel')">
        Đóng
      </button>
      <button type="button" class="order-modal-btn order-modal-btn-cancel" onclick="submitCancelOrderModal()">
        Xác nhận hủy đơn
      </button>
    </div>
  </div>
</div>

<div id="returnOrderModal" class="order-modal-overlay" aria-hidden="true">
  <div class="order-modal-card order-modal-card-return" role="dialog" aria-modal="true" aria-labelledby="returnOrderModalTitle">
    <div class="order-modal-accent"></div>

    <div class="order-modal-head">
      <div class="order-modal-title-wrap">
        <div class="order-modal-icon order-modal-icon-return">↩</div>
        <div>
          <span class="order-modal-kicker">Yêu cầu hoàn hàng</span>
          <h3 id="returnOrderModalTitle">Hoàn hàng đơn <span id="returnOrderCode">#</span></h3>
          <p>Dành cho đơn đã giao/đã nhận nhưng sản phẩm có vấn đề.</p>
        </div>
      </div>
      <button type="button" class="order-modal-close" onclick="closeOrderActionModal('return')" aria-label="Đóng">×</button>
    </div>

    <div class="order-modal-summary">
      <div class="order-modal-summary-item">
        <span>Mã đơn</span>
        <strong id="returnOrderCodeSummary">#</strong>
      </div>
      <div class="order-modal-summary-item">
        <span>Quy trình</span>
        <strong>Shop kiểm tra → xử lý</strong>
      </div>
    </div>

    <div class="order-modal-notice order-modal-notice-info">
      <strong>Gợi ý:</strong> mô tả rõ lỗi sản phẩm và giữ lại hình ảnh/video để shop đối chiếu khi cần.
    </div>

    <div class="order-modal-grid">
      <div class="order-modal-field order-modal-field-full">
        <label for="returnReasonType">Lý do hoàn hàng <span>*</span></label>
        <select id="returnReasonType" class="order-modal-control">
          <option value="">-- Chọn lý do hoàn hàng --</option>
          <option value="Sản phẩm bị lỗi hoặc hư hỏng">Sản phẩm bị lỗi hoặc hư hỏng</option>
          <option value="Sản phẩm không đúng mô tả">Sản phẩm không đúng mô tả</option>
          <option value="Shop giao nhầm sản phẩm">Shop giao nhầm sản phẩm</option>
          <option value="Thiếu sản phẩm hoặc phụ kiện">Thiếu sản phẩm hoặc phụ kiện</option>
          <option value="Sản phẩm bị vỡ, đổ, rò rỉ khi nhận hàng">Sản phẩm bị vỡ, đổ, rò rỉ khi nhận hàng</option>
          <option value="Khác">Khác</option>
        </select>
      </div>

      <div class="order-modal-field order-modal-field-full">
        <div class="order-modal-label-row">
          <label for="returnReasonNote">Mô tả tình trạng <span>*</span></label>
          <small><span id="returnReasonCount">0</span>/500</small>
        </div>
        <textarea id="returnReasonNote"
                  class="order-modal-control order-modal-textarea"
                  rows="4"
                  maxlength="500"
                  placeholder="Ví dụ: Sản phẩm bị vỡ nắp, thiếu seal, giao sai màu, không đúng sản phẩm đã đặt..."></textarea>
        <small>Tối thiểu 10 ký tự để shop có đủ thông tin xử lý.</small>
      </div>
    </div>

    <label class="order-modal-check">
      <input type="checkbox" id="returnPolicyConfirm" />
      <span>Tôi xác nhận thông tin hoàn hàng là đúng và sản phẩm còn đủ để shop kiểm tra.</span>
    </label>

    <p id="returnOrderError" class="order-modal-error" aria-live="polite"></p>

    <div class="order-modal-actions">
      <button type="button" class="order-modal-btn order-modal-btn-secondary" onclick="closeOrderActionModal('return')">
        Đóng
      </button>
      <button type="button" class="order-modal-btn order-modal-btn-return" onclick="submitReturnOrderModal()">
        Gửi yêu cầu hoàn hàng
      </button>
    </div>
  </div>
</div>

<script>
  (function () {
    let activeCancelForm = null;
    let activeReturnForm = null;

    function byId(id) {
      return document.getElementById(id);
    }

    function normalizeText(value) {
      return (value || "").trim().replace(/\s+/g, " ");
    }

    function updateCount(textareaId, counterId) {
      const textarea = byId(textareaId);
      const counter = byId(counterId);

      if (!textarea || !counter) return;

      counter.textContent = String(textarea.value.length);
    }

    function resetModalScroll(modalId) {
      const card = byId(modalId)?.querySelector(".order-modal-card");
      if (card) {
        card.scrollTop = 0;
      }
    }

    function openModal(modalId) {
      const modal = byId(modalId);
      if (!modal) return;

      modal.classList.add("is-open");
      modal.setAttribute("aria-hidden", "false");
      document.body.classList.add("order-modal-open");
      resetModalScroll(modalId);

      const firstControl = modal.querySelector("select, textarea, button");
      if (firstControl) {
        window.setTimeout(function () {
          firstControl.focus();
        }, 80);
      }
    }

    function closeModal(modalId) {
      const modal = byId(modalId);
      if (!modal) return;

      modal.classList.remove("is-open");
      modal.setAttribute("aria-hidden", "true");
      document.body.classList.remove("order-modal-open");
    }

    function submitRealForm(form) {
      if (!form) return;
      HTMLFormElement.prototype.submit.call(form);
    }

    window.openCancelOrderModal = function (form) {
      if (!form) return;

      activeCancelForm = form;

      const orderId = form.dataset.orderId || form.querySelector('input[name="orderId"]')?.value || "";
      const orderCode = orderId ? "#" + orderId : "#";

      byId("cancelOrderCode").textContent = orderCode;
      byId("cancelOrderCodeSummary").textContent = orderCode;

      byId("cancelReasonType").value = "";
      byId("cancelReasonNote").value = "";
      byId("cancelOrderError").textContent = "";
      updateCount("cancelReasonNote", "cancelReasonCount");

      openModal("cancelOrderModal");
    };

    window.openReturnOrderModal = function (form) {
      if (!form) return;

      activeReturnForm = form;

      const orderId = form.dataset.orderId || form.querySelector('input[name="orderId"]')?.value || "";
      const orderCode = orderId ? "#" + orderId : "#";

      byId("returnOrderCode").textContent = orderCode;
      byId("returnOrderCodeSummary").textContent = orderCode;

      byId("returnReasonType").value = "";
      byId("returnReasonNote").value = "";
      byId("returnPolicyConfirm").checked = false;
      byId("returnOrderError").textContent = "";
      updateCount("returnReasonNote", "returnReasonCount");

      openModal("returnOrderModal");
    };

    window.closeOrderActionModal = function (type) {
      if (type === "cancel") {
        closeModal("cancelOrderModal");
        activeCancelForm = null;
        return;
      }

      if (type === "return") {
        closeModal("returnOrderModal");
        activeReturnForm = null;
      }
    };

    window.submitCancelOrderModal = function () {
      if (!activeCancelForm) return;

      const reasonType = normalizeText(byId("cancelReasonType").value);
      const reasonNote = normalizeText(byId("cancelReasonNote").value);
      const errorEl = byId("cancelOrderError");

      if (!reasonType) {
        errorEl.textContent = "Vui lòng chọn lý do hủy đơn.";
        byId("cancelReasonType").focus();
        return;
      }

      if (reasonType === "Khác" && reasonNote.length < 5) {
        errorEl.textContent = "Vui lòng nhập mô tả ít nhất 5 ký tự khi chọn lý do Khác.";
        byId("cancelReasonNote").focus();
        return;
      }

      const finalReason = reasonNote ? reasonType + " - " + reasonNote : reasonType;
      activeCancelForm.querySelector('input[name="reason"]').value = finalReason;

      closeModal("cancelOrderModal");
      submitRealForm(activeCancelForm);
    };

    window.submitReturnOrderModal = function () {
      if (!activeReturnForm) return;

      const reasonType = normalizeText(byId("returnReasonType").value);
      const reasonNote = normalizeText(byId("returnReasonNote").value);
      const checked = byId("returnPolicyConfirm").checked;
      const errorEl = byId("returnOrderError");

      if (!reasonType) {
        errorEl.textContent = "Vui lòng chọn lý do hoàn hàng.";
        byId("returnReasonType").focus();
        return;
      }

      if (reasonNote.length < 10) {
        errorEl.textContent = "Vui lòng mô tả tình trạng hoàn hàng ít nhất 10 ký tự.";
        byId("returnReasonNote").focus();
        return;
      }

      if (!checked) {
        errorEl.textContent = "Vui lòng xác nhận thông tin hoàn hàng trước khi gửi yêu cầu.";
        byId("returnPolicyConfirm").focus();
        return;
      }

      const finalReason = reasonType + " - " + reasonNote;
      activeReturnForm.querySelector('input[name="reason"]').value = finalReason;

      closeModal("returnOrderModal");
      submitRealForm(activeReturnForm);
    };

    document.addEventListener("input", function (event) {
      if (event.target && event.target.id === "cancelReasonNote") {
        updateCount("cancelReasonNote", "cancelReasonCount");
      }

      if (event.target && event.target.id === "returnReasonNote") {
        updateCount("returnReasonNote", "returnReasonCount");
      }
    });

    document.addEventListener("click", function (event) {
      const overlay = event.target.closest(".order-modal-overlay");
      if (overlay && event.target === overlay) {
        if (overlay.id === "cancelOrderModal") {
          window.closeOrderActionModal("cancel");
        }

        if (overlay.id === "returnOrderModal") {
          window.closeOrderActionModal("return");
        }
      }
    });

    document.addEventListener("keydown", function (event) {
      if (event.key !== "Escape") return;

      window.closeOrderActionModal("cancel");
      window.closeOrderActionModal("return");
    });
  })();
</script>

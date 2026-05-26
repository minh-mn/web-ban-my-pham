<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<style>
  .order-page {
    color: #1f2a44;
    width: min(1380px, calc(100vw - 32px));
    max-width: none !important;
    margin: 0 auto;
  }

  .order-orders {
    background: #ffffff;
    border: 1px solid #f0e8ee;
    border-radius: 22px;
    padding: 24px;
    box-shadow: 0 12px 34px rgba(31, 42, 68, 0.07);
  }

  .order-section-head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 14px;
    margin-bottom: 18px;
  }

  .order-section-title {
    margin: 0;
    color: #1f2a44;
    font-size: 22px;
    line-height: 1.35;
    font-weight: 900;
  }

  .order-section-subtitle {
    margin: 4px 0 0;
    color: #7b8794;
    font-size: 13.5px;
    font-weight: 650;
  }

  .order-table-wrap {
    overflow-x: auto;
    border: 1px solid #eef2f7;
    border-radius: 18px;
    background: #ffffff;
  }

  .order-table {
    width: 100%;
    min-width: 0;
    border-collapse: collapse;
    table-layout: fixed;
  }

  .order-table th {
    padding: 14px 12px;
    border-bottom: 1px solid #eef2f7;
    color: #475569;
    background: #f8fafc;
    font-size: 13px;
    text-align: left;
    font-weight: 900;
    white-space: nowrap;
  }

  .order-table td {
    padding: 15px 12px;
    border-bottom: 1px solid #f1f5f9;
    color: #334155;
    font-size: 14px;
    vertical-align: middle;
  }

  .order-table tbody tr:last-child td {
    border-bottom: none;
  }

  .order-table tr:hover td {
    background: #fff8fb;
  }

  .text-center {
    text-align: center !important;
  }

  .order-id {
    color: #d63384;
    font-weight: 950;
    white-space: nowrap;
    font-size: 15px;
  }

  .order-price {
    color: #1f2a44;
    font-weight: 950;
    white-space: nowrap;
  }

  .order-muted {
    color: #7b8794;
    font-size: 13px;
  }

  /*
   * FIX CĂN NGANG:
   * Hai cột "Trạng thái đơn" và "Vận chuyển" dùng cùng layout 2 hàng:
   * hàng 1 = badge, hàng 2 = mô tả/mã vận đơn.
   * Badge luôn nằm trên cùng một trục ngang dù dòng phụ có xuống hàng.
   */
  .order-status-td,
  .shipping-status-td {
    vertical-align: top !important;
    padding-top: 18px !important;
  }

  .status-cell {
    width: 100%;
    min-height: 82px;
    margin: 0 auto;
    display: grid;
    grid-template-rows: 34px 42px;
    row-gap: 8px;
    justify-items: center;
    align-items: start;
    align-content: start;
  }

  .status-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 152px;
    height: 34px;
    min-height: 34px;
    padding: 0 14px;
    border-radius: 999px;
    font-size: 13.5px;
    line-height: 1;
    font-weight: 900;
    text-align: center;
    white-space: nowrap;
    box-sizing: border-box;
  }

  .status-subtext {
    width: 100%;
    min-height: 38px;
    max-width: 172px;
    color: #64748b;
    font-size: 12.5px;
    font-weight: 750;
    line-height: 1.35;
    text-align: center;
    white-space: normal;
    overflow-wrap: anywhere;
    display: flex;
    align-items: flex-start;
    justify-content: center;
  }

  .shipping-code-line {
    margin-top: 0;
  }

  .status-badge.status-warning {
    background: #fff6e5;
    color: #9a5b00;
    border: 1px solid #ffe4ad;
  }

  .status-badge.status-info {
    background: #eaf3ff;
    color: #1769aa;
    border: 1px solid #c7ddff;
  }

  .status-badge.status-ok {
    background: #e8f7ef;
    color: #12804a;
    border: 1px solid #bdebd1;
  }

  .status-badge.status-danger {
    background: #fdecec;
    color: #c62828;
    border: 1px solid #facaca;
  }

  .status-badge.status-muted {
    background: #f8fafc;
    color: #475569;
    border: 1px solid #e2e8f0;
  }

  .tracking-link {
    display: block;
    width: 100%;
    padding: 12px 12px 11px;
    border: 1px solid #edf0f5;
    border-radius: 16px;
    background: #ffffff;
    color: inherit;
    text-decoration: none;
    transition: 0.18s ease;
  }

  .tracking-link:hover {
    border-color: #f3b8d2;
    background: #fff3f8;
    transform: translateY(-1px);
  }

  .tracking-progress {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 7px;
    margin-bottom: 8px;
  }

  .tracking-bar {
    height: 8px;
    border-radius: 999px;
    background: #e5e7eb;
  }

  .tracking-bar.done {
    background: #22c55e;
  }

  .tracking-bar.active {
    background: #3b82f6;
    box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.12);
  }

  .tracking-bar.failed {
    background: #ef4444;
    box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.12);
  }

  .tracking-labels {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 7px;
    color: #64748b;
    font-size: 11.5px;
    font-weight: 800;
    text-align: center;
  }

  .tracking-action {
    display: inline-flex;
    margin-top: 9px;
    color: #d63384;
    font-size: 12.5px;
    font-weight: 900;
  }

  .btn-outline.small {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-height: 34px;
    padding: 0 12px;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    color: #334155;
    background: #ffffff;
    text-decoration: none;
    font-size: 13px;
    font-weight: 850;
    cursor: pointer;
    transition: 0.18s ease;
    white-space: nowrap;
  }

  .btn-outline.small:hover {
    border-color: #f3b8d2;
    color: #d63384;
    background: #fff3f8;
    transform: translateY(-1px);
  }

  .btn-detail {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 132px;
    min-height: 44px;
    padding: 0 18px;
    border: none;
    border-radius: 999px;
    background: linear-gradient(180deg, #f45ea7 0%, #d63384 100%);
    color: #ffffff;
    text-decoration: none;
    font-size: 14px;
    font-weight: 900;
    box-shadow: 0 12px 24px rgba(214, 51, 132, 0.22);
    transition: 0.18s ease;
    white-space: nowrap;
  }

  .btn-detail:hover {
    transform: translateY(-1px);
    box-shadow: 0 14px 28px rgba(214, 51, 132, 0.28);
    color: #ffffff;
  }

  .detail-cell {
    text-align: center;
  }

  .order-action-stack {
    display: inline-flex;
    flex-direction: column;
    align-items: stretch;
    justify-content: center;
    gap: 8px;
  }

  .btn-retry-payment {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 132px;
    min-height: 40px;
    padding: 0 16px;
    border: none;
    border-radius: 999px;
    background: #1f2a44;
    color: #ffffff;
    text-decoration: none;
    font-size: 13px;
    font-weight: 900;
    box-shadow: 0 10px 20px rgba(31, 42, 68, 0.18);
    transition: 0.18s ease;
    white-space: nowrap;
  }

  .btn-retry-payment:hover {
    transform: translateY(-1px);
    color: #ffffff;
    box-shadow: 0 12px 24px rgba(31, 42, 68, 0.25);
  }

  .order-inline-form {
    margin: 0;
  }

  .btn-cancel-order,
  .btn-return-order {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 132px;
    min-height: 38px;
    padding: 0 14px;
    border-radius: 999px;
    border: none;
    text-decoration: none;
    font-size: 12.8px;
    font-weight: 900;
    cursor: pointer;
    transition: 0.18s ease;
    white-space: nowrap;
  }

  .btn-cancel-order {
    background: #fff1f2;
    color: #be123c;
    border: 1px solid #fecdd3;
  }

  .btn-return-order {
    background: #eff6ff;
    color: #1d4ed8;
    border: 1px solid #bfdbfe;
  }

  .btn-cancel-order:hover,
  .btn-return-order:hover {
    transform: translateY(-1px);
    filter: brightness(0.98);
  }

  .admin-status-form {
    min-height: 82px;
    display: flex;
    align-items: flex-start;
    justify-content: center;
    gap: 8px;
    padding-top: 0;
  }

  .order-status-select {
    min-height: 34px;
    padding: 0 10px;
    border: 1px solid #dbe3ef;
    border-radius: 10px;
    background: #ffffff;
    color: #334155;
    font-weight: 750;
  }

  .order-note {
    margin: 16px 0 0;
    padding: 12px 14px;
    border-radius: 14px;
    background: #fff8fb;
    color: #7b3a56;
    font-size: 14px;
    font-weight: 750;
  }

  .empty-text {
    margin: 0;
    padding: 22px;
    border-radius: 16px;
    background: #f8fafc;
    color: #7b8794;
    font-weight: 750;
    text-align: center;
  }

  @media (max-width: 1200px) {
    .order-page {
      width: min(1280px, calc(100vw - 24px));
    }
  }

  @media (max-width: 992px) {
    .order-table {
      min-width: 980px;
    }

    .order-table-wrap {
      overflow-x: auto;
    }
  }

  @media (max-width: 768px) {
    .order-page {
      width: calc(100vw - 16px);
    }

    .order-orders {
      padding: 16px;
    }

    .order-section-head {
      flex-direction: column;
    }

    .order-section-title {
      font-size: 19px;
    }

    .order-table {
      min-width: 940px;
    }
  }
</style>

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
                <th style="width: 84px;">Mã đơn</th>
                <th style="width: 138px;">Ngày đặt</th>
                <th style="width: 150px;">Tổng thanh toán</th>
                <th class="text-center" style="width: 170px;">Trạng thái đơn</th>
                <th class="text-center" style="width: 182px;">Vận chuyển</th>
                <th style="width: 230px;">Tracking</th>
                <th class="text-center" style="width: 144px;">Chi tiết</th>
              </tr>
              </thead>

              <tbody>
              <c:forEach var="order" items="${orders}">
                <tr id="order-${order.id}">
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
                    <c:choose>
                      <c:when test="${not empty sessionScope.user and sessionScope.user.admin}">
                        <form method="post"
                              action="${pageContext.request.contextPath}/admin/order/update-status"
                              class="admin-status-form">

                          <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                          <input type="hidden" name="orderId" value="${order.id}" />
                          <input type="hidden" name="returnUrl" value="/orders" />

                          <select name="status"
                                  class="order-status-select ${order.status}"
                                  <c:if test="${order.status eq 'completed'}">disabled="disabled"</c:if>>

                            <c:choose>
                              <c:when test="${not empty statusChoices}">
                                <c:forEach var="st" items="${statusChoices}">
                                  <option value="${st.key}"
                                          <c:if test="${st.key eq order.status}">selected="selected"</c:if>>
                                      ${st.label}
                                  </option>
                                </c:forEach>
                              </c:when>

                              <c:otherwise>
                                <option value="${order.status}" selected="selected">
                                  <c:choose>
                                    <c:when test="${order.status == 'processing'}">Chờ xác nhận</c:when>
                                    <c:when test="${order.status == 'confirmed'}">Đã xác nhận</c:when>
                                    <c:when test="${order.status == 'shipping'}">Đang giao</c:when>
                                    <c:when test="${order.status == 'completed'}">Hoàn thành</c:when>
                                    <c:when test="${order.status == 'cancelled' || order.status == 'canceled'}">Đã hủy</c:when>
                                    <c:when test="${not empty order.statusLabel}">
                                      ${order.statusLabel}
                                    </c:when>
                                    <c:otherwise>${order.status}</c:otherwise>
                                  </c:choose>
                                </option>
                              </c:otherwise>
                            </c:choose>
                          </select>

                          <c:if test="${order.status ne 'completed'}">
                            <button type="submit" class="btn-outline small">Lưu</button>
                          </c:if>
                        </form>
                      </c:when>

                      <c:otherwise>
                        <div class="status-cell">
                          <c:choose>
                            <c:when test="${order.status == 'completed'}">
                              <span class="status-badge status-ok">Hoàn thành</span>
                            </c:when>
                            <c:when test="${order.status == 'cancelled' || order.status == 'canceled'}">
                              <span class="status-badge status-danger">Đã hủy</span>
                            </c:when>
                            <c:when test="${order.status == 'shipping'}">
                              <span class="status-badge status-info">Đang giao</span>
                            </c:when>
                            <c:when test="${order.status == 'confirmed'}">
                              <span class="status-badge status-warning">Đã xác nhận</span>
                            </c:when>
                            <c:when test="${order.status == 'processing'}">
                              <span class="status-badge status-warning">Chờ xác nhận</span>
                            </c:when>
                            <c:otherwise>
                              <span class="status-badge status-muted">
                                <c:out value="${empty order.statusLabel ? order.status : order.statusLabel}" />
                              </span>
                            </c:otherwise>
                          </c:choose>

                          <span class="status-subtext">Trạng thái đơn hàng</span>
                        </div>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td class="text-center shipping-status-td">
                    <div class="status-cell">
                      <c:choose>
                        <c:when test="${order.delivered}">
                          <span class="status-badge status-ok">Giao thành công</span>
                        </c:when>
                        <c:when test="${order.deliveryFailed}">
                          <span class="status-badge status-danger">Giao thất bại</span>
                        </c:when>
                        <c:when test="${order.shippingCanceled}">
                          <span class="status-badge status-danger">Đã hủy vận chuyển</span>
                        </c:when>
                        <c:when test="${order.delivering}">
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
                        <span class="tracking-bar ${order.pendingPickup ? 'active' : (order.delivering || order.delivered || order.deliveryFailed ? 'done' : '')}"></span>
                        <span class="tracking-bar ${order.delivering ? 'active' : (order.delivered || order.deliveryFailed ? 'done' : '')}"></span>

                        <c:choose>
                          <c:when test="${order.deliveryFailed || order.shippingCanceled}">
                            <span class="tracking-bar failed"></span>
                          </c:when>
                          <c:otherwise>
                            <span class="tracking-bar ${order.delivered ? 'done' : ''}"></span>
                          </c:otherwise>
                        </c:choose>
                      </div>

                      <div class="tracking-labels">
                        <span>Chờ lấy</span>
                        <span>Đang giao</span>
                        <c:choose>
                          <c:when test="${order.deliveryFailed}">
                            <span>Thất bại</span>
                          </c:when>
                          <c:when test="${order.shippingCanceled}">
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
                      <c:if test="${order.retryPaymentAvailable}">
                        <a href="${pageContext.request.contextPath}/vnpay/payment?orderId=${order.id}"
                           class="btn-retry-payment">
                          Thanh toán lại
                        </a>
                      </c:if>

                      <c:if test="${order.cancelable}">
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

                      <c:if test="${order.returnable}">
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

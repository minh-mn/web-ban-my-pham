<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<style>
  .order-page {
    color: #1f2a44;
  }

  .order-orders {
    background: #ffffff;
    border: 1px solid #f0e8ee;
    border-radius: 20px;
    padding: 22px;
    box-shadow: 0 10px 30px rgba(31, 42, 68, 0.06);
  }

  .order-section-title {
    margin: 0 0 18px;
    color: #1f2a44;
    font-size: 22px;
    line-height: 1.35;
    font-weight: 850;
  }

  .order-table-wrap {
    overflow-x: auto;
  }

  .order-table {
    width: 100%;
    min-width: 960px;
    border-collapse: collapse;
  }

  .order-table th {
    padding: 13px 10px;
    border-bottom: 1px solid #eef2f7;
    color: #475569;
    background: #f8fafc;
    font-size: 13px;
    text-align: left;
    font-weight: 850;
    white-space: nowrap;
  }

  .order-table td {
    padding: 15px 10px;
    border-bottom: 1px solid #f1f5f9;
    color: #334155;
    font-size: 14px;
    vertical-align: middle;
  }

  .order-table tr:hover td {
    background: #fff8fb;
  }

  .order-id {
    color: #d63384;
    font-weight: 900;
  }

  .order-price {
    color: #1f2a44;
    font-weight: 900;
    white-space: nowrap;
  }

  .order-muted {
    color: #7b8794;
    font-size: 13px;
  }

  .order-status,
  .shipping-status {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-height: 28px;
    padding: 5px 12px;
    border-radius: 999px;
    font-size: 13px;
    line-height: 1;
    font-weight: 850;
    white-space: nowrap;
  }

  .order-status.processing,
  .order-status.confirmed {
    background: #fff6e5;
    color: #9a5b00;
    border: 1px solid #ffe4ad;
  }

  .order-status.shipping {
    background: #eaf3ff;
    color: #1769aa;
    border: 1px solid #c7ddff;
  }

  .order-status.completed {
    background: #e8f7ef;
    color: #12804a;
    border: 1px solid #bdebd1;
  }

  .order-status.cancelled,
  .order-status.canceled {
    background: #fdecec;
    color: #c62828;
    border: 1px solid #facaca;
  }

  .shipping-status.pending {
    background: #fff6e5;
    color: #9a5b00;
    border: 1px solid #ffe4ad;
  }

  .shipping-status.delivering {
    background: #eaf3ff;
    color: #1769aa;
    border: 1px solid #c7ddff;
  }

  .shipping-status.delivered {
    background: #e8f7ef;
    color: #12804a;
    border: 1px solid #bdebd1;
  }

  .shipping-status.failed,
  .shipping-status.canceled {
    background: #fdecec;
    color: #c62828;
    border: 1px solid #facaca;
  }

  .shipping-mini {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .shipping-code {
    color: #64748b;
    font-size: 12.5px;
    font-weight: 700;
  }

  .tracking-mini {
    min-width: 210px;
  }

  .tracking-line {
    position: relative;
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    margin-top: 8px;
  }

  .tracking-dot {
    position: relative;
    height: 6px;
    border-radius: 999px;
    background: #e5e7eb;
  }

  .tracking-dot.done {
    background: #22c55e;
  }

  .tracking-dot.active {
    background: #3b82f6;
    box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.12);
  }

  .tracking-dot.failed {
    background: #ef4444;
    box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.12);
  }

  .tracking-label {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    margin-top: 7px;
    color: #7b8794;
    font-size: 11.5px;
    font-weight: 750;
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
    font-weight: 800;
    cursor: pointer;
    transition: 0.18s ease;
  }

  .btn-outline.small:hover {
    border-color: #f3b8d2;
    color: #d63384;
    background: #fff3f8;
    transform: translateY(-1px);
  }

  .admin-status-form {
    display: flex;
    align-items: center;
    gap: 8px;
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
    font-weight: 700;
  }

  .empty-text {
    margin: 0;
    padding: 22px;
    border-radius: 16px;
    background: #f8fafc;
    color: #7b8794;
    font-weight: 700;
    text-align: center;
  }

  @media (max-width: 768px) {
    .order-orders {
      padding: 16px;
    }

    .order-section-title {
      font-size: 19px;
    }
  }
</style>

<section class="section">
  <div class="container order-page">

    <div class="order-orders">
      <h3 class="order-section-title">📄 Lịch sử đơn hàng</h3>

      <c:choose>
        <c:when test="${not empty orders}">
          <div class="order-table-wrap">

            <table class="order-table">
              <thead>
              <tr>
                <th>Mã đơn</th>
                <th>Ngày đặt</th>
                <th>Tổng thanh toán</th>
                <th>Trạng thái đơn</th>
                <th>Vận chuyển</th>
                <th>Tracking</th>
                <th>Chi tiết</th>
              </tr>
              </thead>

              <tbody>
              <c:forEach var="order" items="${orders}">
                <tr id="order-${order.id}">
                  <td>
                    <span class="order-id">#${order.id}</span>
                  </td>

                    <%-- DATE --%>
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

                    <%-- TOTAL --%>
                  <td class="order-price">
                    <fmt:formatNumber value="${empty order.total ? 0 : order.total}"
                                      type="number"
                                      groupingUsed="true"
                                      minFractionDigits="0"
                                      maxFractionDigits="0" />
                    ₫
                  </td>

                    <%-- ORDER STATUS --%>
                  <td>
                    <c:choose>

                      <%-- ADMIN: vẫn giữ khả năng đổi trạng thái nhanh nếu trang này được admin sử dụng --%>
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

                      <%-- USER: chỉ xem trạng thái --%>
                      <c:otherwise>
                        <span class="order-status ${order.status}">
                          <c:choose>
                            <c:when test="${not empty order.statusLabel}">
                              <c:out value="${order.statusLabel}" />
                            </c:when>
                            <c:otherwise>
                              <c:out value="${order.status}" />
                            </c:otherwise>
                          </c:choose>
                        </span>
                      </c:otherwise>

                    </c:choose>
                  </td>

                    <%-- SHIPPING STATUS --%>
                  <td>
                    <div class="shipping-mini">
                      <c:choose>
                        <c:when test="${order.delivered}">
                          <span class="shipping-status delivered">
                            <c:out value="${order.shippingStatusLabel}" />
                          </span>
                        </c:when>

                        <c:when test="${order.deliveryFailed}">
                          <span class="shipping-status failed">
                            <c:out value="${order.shippingStatusLabel}" />
                          </span>
                        </c:when>

                        <c:when test="${order.shippingCanceled}">
                          <span class="shipping-status canceled">
                            <c:out value="${order.shippingStatusLabel}" />
                          </span>
                        </c:when>

                        <c:when test="${order.delivering}">
                          <span class="shipping-status delivering">
                            <c:out value="${order.shippingStatusLabel}" />
                          </span>
                        </c:when>

                        <c:otherwise>
                          <span class="shipping-status pending">
                            <c:out value="${order.shippingStatusLabel}" />
                          </span>
                        </c:otherwise>
                      </c:choose>

                      <span class="shipping-code">
                        Mã vận đơn:
                        <c:choose>
                          <c:when test="${not empty order.shippingCode}">
                            <c:out value="${order.shippingCode}" />
                          </c:when>
                          <c:otherwise>Chưa có</c:otherwise>
                        </c:choose>
                      </span>
                    </div>
                  </td>

                    <%-- TRACKING MINI --%>
                  <td>
                    <div class="tracking-mini">
                      <div class="tracking-line">
                        <span class="tracking-dot ${order.pendingPickup ? 'active' : (order.delivering || order.delivered || order.deliveryFailed ? 'done' : '')}"></span>
                        <span class="tracking-dot ${order.delivering ? 'active' : (order.delivered || order.deliveryFailed ? 'done' : '')}"></span>

                        <c:choose>
                          <c:when test="${order.deliveryFailed || order.shippingCanceled}">
                            <span class="tracking-dot failed"></span>
                          </c:when>
                          <c:otherwise>
                            <span class="tracking-dot ${order.delivered ? 'done' : ''}"></span>
                          </c:otherwise>
                        </c:choose>
                      </div>

                      <div class="tracking-label">
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
                    </div>
                  </td>

                    <%-- DETAIL --%>
                  <td>
                    <a href="${pageContext.request.contextPath}/orders/detail?id=${order.id}"
                       class="btn-outline small">
                      Xem
                    </a>
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

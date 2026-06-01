<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Quản lý đơn hàng" scope="request"/>
<c:set var="activeMenu" value="orders" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container admin-order-page">

    <div class="admin-order-hero">
      <div class="admin-order-hero__content">
        <span class="admin-order-eyebrow">ORDER WORKFLOW</span>
        <h1 class="admin-h1 admin-order-title">Quản lý đơn hàng</h1>
        <p class="admin-subtext admin-order-subtitle">
          Theo dõi trạng thái đơn, thanh toán, đơn vị vận chuyển, mã vận đơn và xử lý đơn hàng theo đúng luồng:
          <strong>Chờ xác nhận → Đã xác nhận → Đang giao → Giao thành công</strong>.
        </p>
      </div>

      <div class="admin-order-hero__actions">
        <a class="admin-btn" href="${pageContext.request.contextPath}/admin/orders">
          Làm mới
        </a>
      </div>
    </div>

    <c:if test="${not empty admin_order_success}">
      <div class="admin-alert admin-alert--success">
        <c:out value="${admin_order_success}"/>
      </div>
    </c:if>

    <c:if test="${not empty admin_order_error}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${admin_order_error}"/>
      </div>
    </c:if>

    <div class="admin-order-card admin-card">
      <div class="admin-card__body">

        <div class="admin-order-section-head">
          <div>
            <h2 class="admin-order-section-title">Danh sách đơn hàng</h2>
            <p class="admin-order-section-desc">
              Hiển thị đầy đủ thông tin khách hàng, thanh toán, vận chuyển và ngày xử lý đơn.
            </p>
          </div>
        </div>

        <c:choose>
          <c:when test="${empty orders}">
            <div class="admin-order-empty">
              <div class="admin-order-empty__icon">📦</div>
              <div>
                <h3>Chưa có đơn hàng</h3>
                <p>Hiện tại hệ thống chưa ghi nhận đơn hàng nào.</p>
              </div>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-order-table-wrap">
              <table class="admin-table admin-order-table">
                <thead>
                  <tr>
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
                    <tr class="
                        <c:if test='${o.cancelled}'>admin-order-row--cancelled</c:if>
                        <c:if test='${o.deliveryFailed}'> admin-order-row--failed</c:if>
                    ">
                      <td class="admin-order-id-cell">
                        <strong>#${o.id}</strong>
                        <span>User ${o.userId}</span>
                      </td>

                      <td>
                        <div class="admin-order-customer">
                          <strong><c:out value="${o.fullName}"/></strong>

                          <c:if test="${not empty o.phone}">
                            <span>📞 <c:out value="${o.phone}"/></span>
                          </c:if>

                          <c:if test="${not empty o.address}">
                            <span class="admin-order-address">📍 <c:out value="${o.address}"/></span>
                          </c:if>
                        </div>
                      </td>

                      <td class="admin-order-total-cell">
                        <strong>
                          <c:choose>
                            <c:when test="${not empty o.totalVnd}">
                              <c:out value="${o.totalVnd}"/>
                            </c:when>
                            <c:otherwise>0</c:otherwise>
                          </c:choose>
                        </strong>
                        <span>₫</span>

                        <c:if test="${not empty o.shippingFeeVnd}">
                          <small>Ship: <c:out value="${o.shippingFeeVnd}"/> ₫</small>
                        </c:if>
                      </td>

                      <td>
                        <div class="admin-order-payment">
                          <span class="admin-pill">
                            <c:out value="${o.paymentMethodLabel}"/>
                          </span>

                          <span class="admin-pill admin-pill--${o.paymentStatusCssClass}">
                            <c:out value="${o.paymentStatusLabel}"/>
                          </span>
                        </div>
                      </td>

                      <td class="admin-status-cell">
                        <div class="admin-order-status-stack">
                          <span class="admin-pill admin-pill--${o.orderStatusCssClass}">
                            <c:out value="${o.statusLabel}"/>
                          </span>

                          <c:if test="${o.deliveryFailed}">
                            <span class="admin-pill admin-pill--danger">Giao thất bại</span>
                          </c:if>
                        </div>
                      </td>

                      <td>
                        <div class="admin-order-shipping">
                          <strong><c:out value="${o.shippingProviderLabel}"/></strong>
                          <span><c:out value="${o.shippingMethodLabel}"/></span>

                          <c:choose>
                            <c:when test="${not empty o.shippingCode}">
                              <code><c:out value="${o.shippingCode}"/></code>
                            </c:when>
                            <c:otherwise>
                              <code>Chưa có mã vận đơn</code>
                            </c:otherwise>
                          </c:choose>

                          <span class="admin-pill admin-pill--${o.shippingStatusCssClass}">
                            <c:out value="${o.shippingStatusLabel}"/>
                          </span>
                        </div>
                      </td>

                      <td>
                        <div class="
                          admin-order-flow
                          <c:if test='${o.cancelled}'>is-cancelled</c:if>
                          <c:if test='${o.deliveryFailed}'>is-failed</c:if>
                        ">
                          <span class="admin-order-flow__step <c:if test='${o.stepProcessingDone}'>is-done</c:if>">1</span>
                          <span class="admin-order-flow__line <c:if test='${o.stepConfirmedDone}'>is-done</c:if>"></span>
                          <span class="admin-order-flow__step <c:if test='${o.stepConfirmedDone}'>is-done</c:if>">2</span>
                          <span class="admin-order-flow__line <c:if test='${o.stepShippingDone}'>is-done</c:if>"></span>
                          <span class="admin-order-flow__step <c:if test='${o.stepShippingDone}'>is-done</c:if>">3</span>
                          <span class="admin-order-flow__line <c:if test='${o.stepCompletedDone}'>is-done</c:if>"></span>
                          <span class="admin-order-flow__step <c:if test='${o.stepCompletedDone}'>is-done</c:if>">4</span>
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
                          <span>
                            Đặt:
                            <strong>
                              <fmt:formatDate value="${o.createdAtDate}" pattern="dd/MM/yyyy HH:mm"/>
                            </strong>
                          </span>

                          <span>
                            Gửi:
                            <strong>
                              <c:choose>
                                <c:when test="${not empty o.shippedAtDate}">
                                  <fmt:formatDate value="${o.shippedAtDate}" pattern="dd/MM/yyyy HH:mm"/>
                                </c:when>
                                <c:otherwise>Chưa gửi</c:otherwise>
                              </c:choose>
                            </strong>
                          </span>

                          <span>
                            Giao:
                            <strong>
                              <c:choose>
                                <c:when test="${not empty o.deliveredAtDate}">
                                  <fmt:formatDate value="${o.deliveredAtDate}" pattern="dd/MM/yyyy HH:mm"/>
                                </c:when>
                                <c:otherwise>Chưa giao</c:otherwise>
                              </c:choose>
                            </strong>
                          </span>
                        </div>
                      </td>

                      <td class="admin-order-action-cell">
                        <div class="admin-order-actions">
                          <a class="admin-btn admin-order-action-btn admin-order-action-btn--view"
                             href="${pageContext.request.contextPath}/admin/orders?action=detail&id=${o.id}">
                            Chi tiết
                          </a>

                          <c:choose>
                            <c:when test="${o.processing}">
                              <form method="post" action="${pageContext.request.contextPath}/admin/order/update-status">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                <input type="hidden" name="orderId" value="${o.id}">
                                <input type="hidden" name="workflowAction" value="confirmOrder">
                                <input type="hidden" name="returnUrl" value="/admin/orders">
                                <button class="admin-btn admin-btn--ok admin-order-action-btn" type="submit">
                                  Xác nhận
                                </button>
                              </form>

                              <form method="post" action="${pageContext.request.contextPath}/admin/order/update-status"
                                    onsubmit="return confirm('Bạn có chắc muốn hủy đơn hàng này?');">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                <input type="hidden" name="orderId" value="${o.id}">
                                <input type="hidden" name="workflowAction" value="cancelOrder">
                                <input type="hidden" name="returnUrl" value="/admin/orders">
                                <button class="admin-btn admin-btn--danger admin-order-action-btn" type="submit">
                                  Hủy
                                </button>
                              </form>
                            </c:when>

                            <c:when test="${o.confirmed}">
                              <form method="post" action="${pageContext.request.contextPath}/admin/order/update-status">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                <input type="hidden" name="orderId" value="${o.id}">
                                <input type="hidden" name="workflowAction" value="startShipping">
                                <input type="hidden" name="returnUrl" value="/admin/orders">
                                <button class="admin-btn admin-btn--primary admin-order-action-btn" type="submit">
                                  Giao
                                </button>
                              </form>

                              <form method="post" action="${pageContext.request.contextPath}/admin/order/update-status"
                                    onsubmit="return confirm('Bạn có chắc muốn hủy đơn hàng này?');">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                <input type="hidden" name="orderId" value="${o.id}">
                                <input type="hidden" name="workflowAction" value="cancelOrder">
                                <input type="hidden" name="returnUrl" value="/admin/orders">
                                <button class="admin-btn admin-btn--danger admin-order-action-btn" type="submit">
                                  Hủy
                                </button>
                              </form>
                            </c:when>

                            <c:when test="${o.orderShipping and o.delivering}">
                              <form method="post" action="${pageContext.request.contextPath}/admin/order/update-status">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                <input type="hidden" name="orderId" value="${o.id}">
                                <input type="hidden" name="workflowAction" value="markDelivered">
                                <input type="hidden" name="returnUrl" value="/admin/orders">
                                <button class="admin-btn admin-btn--ok admin-order-action-btn" type="submit">
                                  Đã giao
                                </button>
                              </form>

                              <form method="post" action="${pageContext.request.contextPath}/admin/order/update-status"
                                    onsubmit="return confirm('Xác nhận giao hàng thất bại?');">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                <input type="hidden" name="orderId" value="${o.id}">
                                <input type="hidden" name="workflowAction" value="markFailed">
                                <input type="hidden" name="returnUrl" value="/admin/orders">
                                <button class="admin-btn admin-btn--danger admin-order-action-btn" type="submit">
                                  Thất bại
                                </button>
                              </form>
                            </c:when>

                            <c:when test="${o.deliveryFailed}">
                              <form method="post" action="${pageContext.request.contextPath}/admin/order/update-status">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                <input type="hidden" name="orderId" value="${o.id}">
                                <input type="hidden" name="workflowAction" value="startShipping">
                                <input type="hidden" name="returnUrl" value="/admin/orders">
                                <button class="admin-btn admin-btn--primary admin-order-action-btn" type="submit">
                                  Giao lại
                                </button>
                              </form>

                              <form method="post" action="${pageContext.request.contextPath}/admin/order/update-status"
                                    onsubmit="return confirm('Bạn có chắc muốn hủy đơn giao thất bại này?');">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                <input type="hidden" name="orderId" value="${o.id}">
                                <input type="hidden" name="workflowAction" value="cancelOrder">
                                <input type="hidden" name="returnUrl" value="/admin/orders">
                                <button class="admin-btn admin-btn--danger admin-order-action-btn" type="submit">
                                  Hủy
                                </button>
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
          </c:otherwise>
        </c:choose>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

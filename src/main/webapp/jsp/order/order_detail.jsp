<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="MyCosmetic | Chi tiết đơn hàng" scope="request" />
<c:set var="pageCss" value="/order-detail.css" scope="request" />

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

<c:set var="isPendingPickup" value="${shippingStatus eq 'PENDING_PICKUP' or shippingStatus eq 'PENDING' or shippingStatus eq 'CREATED' or shippingStatus eq 'PICKING' or empty shippingStatus}" />
<c:set var="isDelivering" value="${shippingStatus eq 'DELIVERING' or shippingStatus eq 'SHIPPING' or shippingStatus eq 'IN_TRANSIT'}" />
<c:set var="isDelivered" value="${shippingStatus eq 'DELIVERED' or shippingStatus eq 'SUCCESS' or isCompleted}" />
<c:set var="isDeliveryFailed" value="${shippingStatus eq 'FAILED' or shippingStatus eq 'DELIVERY_FAILED' or shippingStatus eq 'RETURNED'}" />
<c:set var="isShippingCanceled" value="${shippingStatus eq 'CANCELED' or shippingStatus eq 'CANCELLED'}" />

<c:set var="canRetryPayment" value="${paymentMethod eq 'VNPAY' and paymentStatus ne 'PAID' and not isCompleted and not isCancelled}" />
<c:set var="canCancelOrder" value="${(isProcessing or isConfirmed) and (isPendingPickup or empty shippingStatus)}" />
<c:set var="canReturnOrder" value="${isDelivered and paymentStatus eq 'PAID' and empty returnRequest}" />

<section class="order-detail-page">
    <div class="order-detail-container">

        <div class="order-breadcrumb">
            <a href="${pageContext.request.contextPath}/account#account-orders">← Quay lại tài khoản</a>
        </div>

        <header class="order-detail-hero">
            <div>
                <h1 class="order-detail-title">Chi tiết đơn hàng #${order.id}</h1>
                <p class="order-detail-subtitle">Theo dõi trạng thái đơn hàng, vận chuyển, thanh toán và các thao tác sau mua.</p>
            </div>

            <div class="order-detail-header-actions">
                <c:if test="${canRetryPayment}">
                    <a href="${pageContext.request.contextPath}/vnpay/payment?orderId=${order.id}"
                       class="order-soft-btn order-soft-btn-primary">
                        Thanh toán lại
                    </a>
                </c:if>

                <a href="${pageContext.request.contextPath}/orders" class="order-soft-btn order-soft-btn-light order-list-back-btn">
                    <span>📋</span>
                    Danh sách đơn hàng
                </a>
            </div>
        </header>

        <div class="order-status-strip">
            <div class="status-summary-card">
                <span class="status-summary-icon">📋</span>
                <div class="status-summary-content">
                    <span>Trạng thái đơn</span>
                    <strong>
                        <c:choose>
                            <c:when test="${isProcessing}">Chờ xác nhận</c:when>
                            <c:when test="${isConfirmed}">Đã xác nhận</c:when>
                            <c:when test="${isOrderShipping}">Đang giao</c:when>
                            <c:when test="${isCompleted}">Hoàn thành</c:when>
                            <c:when test="${isCancelled}">Đã hủy</c:when>
                            <c:otherwise><c:out value="${order.status}" /></c:otherwise>
                        </c:choose>
                        <i class="${isCancelled ? 'dot-danger' : 'dot-success'}"></i>
                    </strong>
                </div>
            </div>

            <div class="status-summary-card">
                <span class="status-summary-icon">🚚</span>
                <div class="status-summary-content">
                    <span>Vận chuyển</span>
                    <strong>
                        <c:choose>
                            <c:when test="${isDelivered}">Giao thành công</c:when>
                            <c:when test="${isDeliveryFailed}">Giao thất bại</c:when>
                            <c:when test="${isShippingCanceled}">Đã hủy vận chuyển</c:when>
                            <c:when test="${isDelivering}">Đang giao</c:when>
                            <c:otherwise>Chờ lấy hàng</c:otherwise>
                        </c:choose>
                        <i class="${isDeliveryFailed or isShippingCanceled ? 'dot-danger' : (isDelivered ? 'dot-success' : 'dot-warning')}"></i>
                    </strong>
                </div>
            </div>

            <div class="status-summary-card">
                <span class="status-summary-icon">💳</span>
                <div class="status-summary-content">
                    <span>Thanh toán</span>
                    <strong>
                        <c:choose>
                            <c:when test="${paymentMethod eq 'COD'}">COD</c:when>
                            <c:otherwise><c:out value="${order.paymentMethod}" /></c:otherwise>
                        </c:choose>
                        /
                        <c:choose>
                            <c:when test="${paymentStatus eq 'PAID'}">Đã thanh toán</c:when>
                            <c:when test="${paymentStatus eq 'FAILED'}">Thất bại</c:when>
                            <c:when test="${paymentStatus eq 'CANCELED' or paymentStatus eq 'CANCELLED'}">Đã hủy</c:when>
                            <c:otherwise>Chờ thanh toán</c:otherwise>
                        </c:choose>
                        <i class="${paymentStatus eq 'PAID' ? 'dot-success' : 'dot-warning'}"></i>
                    </strong>
                </div>
            </div>
        </div>

        <c:if test="${not empty success}">
            <div class="order-alert success"><c:out value="${success}" /></div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="order-alert error"><c:out value="${error}" /></div>
        </c:if>

        <section class="order-card order-action-panel">
            <div class="order-card-head">
                <h2 class="order-card-title">Hủy đơn / Hoàn hàng / Xác nhận nhận hàng</h2>
            </div>

            <div class="order-action-grid">
                <div class="order-action-box order-action-cancel ${canCancelOrder ? '' : 'is-disabled'}">
                    <div class="order-action-title-row">
                        <span class="order-action-icon">✕</span>
                        <h3>Hủy đơn hàng</h3>
                    </div>

                    <c:choose>
                        <c:when test="${not empty cancelRequest}">
                            <p>Yêu cầu hủy đơn của bạn đang được xử lý hoặc đã có kết quả.</p>
                            <div class="return-request-summary">
                                <span class="order-pill ${cancelRequest.statusCssClass}">
                                    <c:out value="${cancelRequest.statusLabel}" />
                                </span>
                                <p><strong>Lý do:</strong> <c:out value="${cancelRequest.reason}" /></p>
                                <p><strong>Số tiền hoàn dự kiến:</strong> <c:out value="${cancelRequest.refundAmountVnd}" /> ₫</p>
                                <c:if test="${not empty cancelRequest.adminNote}">
                                    <p><strong>Phản hồi shop:</strong> <c:out value="${cancelRequest.adminNote}" /></p>
                                </c:if>
                            </div>
                        </c:when>

                        <c:when test="${canCancelOrder}">
                            <p>Chỉ áp dụng khi đơn chưa được bàn giao cho đơn vị vận chuyển.</p>

                            <form method="post" action="${pageContext.request.contextPath}/orders/cancel">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                                <input type="hidden" name="orderId" value="${order.id}" />
                                <textarea name="reason" required minlength="5" maxlength="500"
                                          placeholder="Nhập lý do hủy đơn để shop xác nhận..."></textarea>
                                <button type="submit" class="order-action-btn cancel"
                                        onclick="return confirm('Gửi yêu cầu hủy đơn hàng này cho shop?');">
                                    Gửi yêu cầu hủy
                                </button>
                            </form>
                        </c:when>

                        <c:otherwise>
                            <p>Đơn hàng không thể hủy vì đã được giao, đang vận chuyển hoặc đã hoàn tất.</p>
                            <button type="button" class="order-action-btn disabled" disabled>Không thể hủy đơn</button>

                            <c:if test="${not empty order.cancelReason}">
                                <div class="return-request-summary">
                                    <p><strong>Lý do đã hủy:</strong> <c:out value="${order.cancelReason}" /></p>
                                </div>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="order-action-box order-action-return ${canReturnOrder ? 'is-highlight' : ''}">
                    <div class="order-action-title-row">
                        <span class="order-action-icon">↩</span>
                        <h3>Yêu cầu hoàn hàng</h3>
                    </div>

                    <p>Áp dụng cho đơn đã giao thành công và còn trong thời hạn 7 ngày kể từ lúc nhận hàng.</p>

                    <c:choose>
                        <c:when test="${canReturnOrder}">
                            <form method="post" action="${pageContext.request.contextPath}/orders/return">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                                <input type="hidden" name="orderId" value="${order.id}" />

                                <select name="refundMethod">
                                    <option value="MANUAL">Shop liên hệ xác nhận phương thức hoàn tiền</option>
                                    <option value="VNPAY">Hoàn về VNPay</option>
                                    <option value="BANK_TRANSFER">Chuyển khoản ngân hàng</option>
                                    <option value="STORE_CREDIT">Điểm / ví cửa hàng</option>
                                </select>

                                <textarea name="reason" required minlength="10" maxlength="1000"
                                          placeholder="Nhập lý do hoàn hàng, tình trạng sản phẩm..."></textarea>

                                <button type="submit" class="order-action-btn return"
                                        onclick="return confirm('Gửi yêu cầu hoàn hàng cho đơn này?');">
                                    Gửi yêu cầu hoàn hàng
                                </button>
                            </form>
                        </c:when>

                        <c:otherwise>
                            <div class="return-request-summary">
                                <p class="order-muted">Đơn hàng hiện không đủ điều kiện tạo yêu cầu hoàn hàng.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${not empty returnRequest}">
                        <div class="return-request-summary">
                            <span class="order-pill ${returnRequest.statusCssClass}">
                                <c:out value="${returnRequest.statusLabel}" />
                            </span>
                            <p><strong>Lý do:</strong> <c:out value="${returnRequest.reason}" /></p>
                            <p><strong>Số tiền dự kiến:</strong> <c:out value="${returnRequest.refundAmountVnd}" /> ₫</p>
                            <c:if test="${not empty returnRequest.adminNote}">
                                <p><strong>Phản hồi shop:</strong> <c:out value="${returnRequest.adminNote}" /></p>
                            </c:if>
                        </div>
                    </c:if>
                </div>

                <div class="order-action-box order-action-received ${customerReceivedConfirmed ? 'is-success' : ''}">
                    <div class="order-action-title-row">
                        <span class="order-action-icon">✓</span>
                        <h3>Xác nhận đã nhận hàng</h3>
                    </div>

                    <c:choose>
                        <c:when test="${customerReceivedConfirmed}">
                            <p>Bạn đã xác nhận nhận hàng thành công.</p>
                        </c:when>
                        <c:otherwise>
                            <p>Xác nhận khi bạn đã nhận đúng và đủ sản phẩm. Nếu sau 7 ngày kể từ khi giao thành công bạn chưa xác nhận, hệ thống sẽ tự động đánh dấu đã nhận.</p>
                        </c:otherwise>
                    </c:choose>

                    <div class="return-request-summary receive-summary">
                        <span class="order-pill ${customerReceivedConfirmed ? 'ok' : (isDelivered ? 'warning' : 'muted')}">
                            <c:out value="${receiveStatusLabel}" />
                        </span>

                        <c:if test="${not empty customerReceivedAtDate}">
                            <p><strong>Đã xác nhận:</strong>
                                <fmt:formatDate value="${customerReceivedAtDate}" pattern="dd/MM/yyyy HH:mm" />
                            </p>
                        </c:if>

                        <c:if test="${not empty receiveConfirmNote}">
                            <p><strong>Ghi chú:</strong> <c:out value="${receiveConfirmNote}" /></p>
                        </c:if>
                    </div>

                    <c:if test="${receiveConfirmable}">
                        <form method="post" action="${pageContext.request.contextPath}/orders/confirm-received" class="order-confirm-received-form">
                            <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                            <input type="hidden" name="orderId" value="${order.id}" />
                            <input type="text" name="note" maxlength="500"
                                   placeholder="Ghi chú nếu cần, ví dụ: đã nhận đủ hàng" />
                            <button type="submit" class="order-action-btn receive"
                                    onclick="return confirm('Xác nhận bạn đã nhận hàng thành công?');">
                                Tôi đã nhận hàng
                            </button>
                        </form>
                    </c:if>
                </div>
            </div>

            <div class="order-policy-panel">
                <div class="order-policy-head">
                    <div>
                        <h3>Chính sách hỗ trợ sau mua</h3>
                        <p>Kiểm tra nhanh điều kiện hủy đơn và hoàn hàng trước khi gửi yêu cầu.</p>
                    </div>
                    <span>Hỗ trợ khách hàng</span>
                </div>

                <div class="order-policy-grid">
                    <a href="${pageContext.request.contextPath}/policy/cancel" class="order-policy-card cancel-policy">
                        <div class="order-policy-icon">✕</div>
                        <div class="order-policy-content">
                            <strong>Chính sách hủy đơn</strong>
                            <p>Áp dụng khi đơn chưa bàn giao cho đơn vị vận chuyển. Một số yêu cầu cần shop xác nhận trước khi hoàn tất.</p>
                            <span>Xem điều kiện hủy đơn →</span>
                        </div>
                    </a>

                    <a href="${pageContext.request.contextPath}/policy/return" class="order-policy-card return-policy">
                        <div class="order-policy-icon">↩</div>
                        <div class="order-policy-content">
                            <strong>Chính sách hoàn hàng</strong>
                            <p>Áp dụng cho đơn đã giao thành công và còn trong thời hạn hỗ trợ theo quy định của MyCosmetic.</p>
                            <span>Xem điều kiện hoàn hàng →</span>
                        </div>
                    </a>
                </div>
            </div>
        </section>

        <section class="order-card">
            <div class="order-card-head">
                <h2 class="order-card-title">Thông tin nhận hàng</h2>
            </div>

            <div class="order-info-grid order-info-grid-modern">
                <div class="order-info-item">
                    <span class="order-info-icon">👤</span>
                    <div>
                        <div class="order-info-label">Người nhận</div>
                        <div class="order-info-value"><c:out value="${order.fullName}" /></div>
                    </div>
                </div>

                <div class="order-info-item">
                    <span class="order-info-icon">📞</span>
                    <div>
                        <div class="order-info-label">Số điện thoại</div>
                        <div class="order-info-value"><c:out value="${order.phone}" /></div>
                    </div>
                </div>

                <div class="order-info-item">
                    <span class="order-info-icon">📍</span>
                    <div>
                        <div class="order-info-label">Địa chỉ giao hàng</div>
                        <div class="order-info-value"><c:out value="${order.address}" /></div>
                    </div>
                </div>

                <div class="order-info-item">
                    <span class="order-info-icon">🗓</span>
                    <div>
                        <div class="order-info-label">Ngày đặt</div>
                        <div class="order-info-value">
                            <fmt:formatDate value="${order.createdAtDate}" pattern="dd/MM/yyyy HH:mm" />
                        </div>
                    </div>
                </div>

                <div class="order-info-item">
                    <span class="order-info-icon">💳</span>
                    <div>
                        <div class="order-info-label">Thanh toán</div>
                        <div class="order-info-value">
                            <c:choose>
                                <c:when test="${paymentMethod eq 'COD'}">COD</c:when>
                                <c:otherwise><c:out value="${order.paymentMethod}" /></c:otherwise>
                            </c:choose>
                            -
                            <c:choose>
                                <c:when test="${paymentStatus eq 'PAID'}">
                                    <span class="order-pill ok">Đã thanh toán</span>
                                </c:when>
                                <c:when test="${paymentStatus eq 'CANCELED' or paymentStatus eq 'CANCELLED' or paymentStatus eq 'FAILED'}">
                                    <span class="order-pill danger"><c:out value="${order.paymentStatus}" /></span>
                                </c:when>
                                <c:otherwise>
                                    <span class="order-pill warning">Chờ thanh toán</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>

                <c:if test="${not empty order.vnpTxnRef}">
                    <div class="order-info-item">
                        <span class="order-info-icon">#</span>
                        <div>
                            <div class="order-info-label">Mã giao dịch VNPAY</div>
                            <div class="order-info-value"><c:out value="${order.vnpTxnRef}" /></div>
                        </div>
                    </div>
                </c:if>
            </div>
        </section>

        <section class="order-card" id="shipping-tracking">
            <div class="order-card-head">
                <h2 class="order-card-title">Theo dõi vận chuyển</h2>
            </div>

            <div class="shipping-summary-grid">
                <div class="shipping-summary-box">
                    <span>Phương thức</span>
                    <strong>
                        <c:choose>
                            <c:when test="${shippingMethod eq 'FAST'}">Giao hàng nhanh</c:when>
                            <c:when test="${shippingMethod eq 'EXPRESS'}">Hỏa tốc</c:when>
                            <c:otherwise>Giao hàng tiết kiệm</c:otherwise>
                        </c:choose>
                    </strong>
                </div>

                <div class="shipping-summary-box">
                    <span>Đơn vị giao</span>
                    <strong>
                        <c:choose>
                            <c:when test="${shippingProvider eq 'GHTK'}">Giao hàng tiết kiệm</c:when>
                            <c:when test="${shippingProvider eq 'GHN'}">Giao hàng nhanh</c:when>
                            <c:when test="${shippingProvider eq 'VIETTEL_POST'}">Viettel Post</c:when>
                            <c:when test="${shippingProvider eq 'OTHER'}">Đơn vị vận chuyển khác</c:when>
                            <c:otherwise>Vận chuyển nội bộ</c:otherwise>
                        </c:choose>
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

                <div class="shipping-summary-box">
                    <span>Dự kiến nhận hàng</span>
                    <strong>
                        <c:choose>
                            <c:when test="${not empty estimatedDeliveryDate}">
                                <c:out value="${estimatedDeliveryDate}" />
                            </c:when>
                            <c:otherwise>3 - 5 ngày làm việc</c:otherwise>
                        </c:choose>
                    </strong>
                </div>
            </div>

            <p class="tracking-current-note">
                <c:choose>
                    <c:when test="${isDelivered}">
                        Đơn hàng đã được giao thành công. Cảm ơn bạn đã mua hàng tại MyCosmetic.
                    </c:when>
                    <c:when test="${isDeliveryFailed}">
                        Giao hàng thất bại. Shop sẽ liên hệ lại để hỗ trợ giao lại hoặc xử lý đơn hàng.
                    </c:when>
                    <c:when test="${isShippingCanceled}">
                        Vận chuyển của đơn hàng đã bị hủy. Vui lòng liên hệ shop nếu cần hỗ trợ.
                    </c:when>
                    <c:when test="${isDelivering}">
                        Đơn hàng đang được vận chuyển đến bạn.
                    </c:when>
                    <c:otherwise>
                        Đơn hàng đang chờ shop chuẩn bị và bàn giao cho đơn vị vận chuyển.
                    </c:otherwise>
                </c:choose>
            </p>

            <div class="tracking-steps">
                <div class="tracking-step ${isPendingPickup ? 'active' : (isDelivering or isDelivered or isDeliveryFailed ? 'done' : '')}">
                    <strong>Chờ lấy hàng</strong>
                    <small>Shop chuẩn bị và chờ bàn giao đơn hàng.</small>
                </div>

                <div class="tracking-step ${isDelivering ? 'active' : (isDelivered or isDeliveryFailed ? 'done' : '')}">
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
                    <c:when test="${isDeliveryFailed}">
                        <div class="tracking-step failed">
                            <strong>Giao thất bại</strong>
                            <small>Shop sẽ liên hệ lại để hỗ trợ giao lại.</small>
                        </div>
                    </c:when>

                    <c:when test="${isShippingCanceled}">
                        <div class="tracking-step failed">
                            <strong>Đã hủy vận chuyển</strong>
                            <small>Đơn vận chuyển đã bị hủy.</small>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="tracking-step ${isDelivered ? 'done' : ''}">
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
                                    <strong><c:out value="${tracking.shippingStatusLabel}" /></strong>
                                    <p><c:out value="${tracking.note}" /></p>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </c:if>
        </section>

        <section class="order-card">
            <div class="order-card-head">
                <h2 class="order-card-title">Sản phẩm đã mua</h2>
            </div>

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
                                <th>Sản phẩm</th>
                                <th class="order-item-col-code">Mã sản phẩm</th>
                                <th class="order-item-col-variant">Phân loại</th>
                                <th class="order-item-col-price text-right">Đơn giá</th>
                                <th class="order-item-col-qty text-center">SL</th>
                                <th class="order-item-col-subtotal text-right">Thành tiền</th>
                                <th class="order-item-col-status text-center">Trạng thái</th>
                            </tr>
                            </thead>

                            <tbody>
                            <c:forEach var="item" items="${displayItems}">
                                <tr>
                                    <td>
                                        <div class="order-product-cell">
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

                                            <div>
                                                <div class="order-product-name">
                                                    <c:out value="${item.productName}" />
                                                </div>
                                            </div>
                                        </div>
                                    </td>

                                    <td>
                                        <span class="order-code-pill"><c:out value="${item.productId}" /></span>
                                    </td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty item.variantId
                                  || not empty item.variantName
                                  || not empty item.variantSize
                                  || not empty item.variantType}">
                                                <div class="order-variant-detail">
                                                    <c:if test="${not empty item.variantName}">
                                                        <div><strong>Tên:</strong> <c:out value="${item.variantName}" /></div>
                                                    </c:if>

                                                    <c:if test="${not empty item.variantSize}">
                                                        <div><strong>Dung tích / kích thước:</strong> <c:out value="${item.variantSize}" /></div>
                                                    </c:if>

                                                    <c:if test="${not empty item.variantType}">
                                                        <div><strong>Loại:</strong> <c:out value="${item.variantType}" /></div>
                                                    </c:if>

                                                    <c:if test="${not empty item.variantId}">
                                                        <div class="order-muted">Mã phân loại: <c:out value="${item.variantId}" /></div>
                                                    </c:if>
                                                </div>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="order-muted">Không có phân loại</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td class="text-right">
                                        <fmt:formatNumber value="${item.price}"
                                                          type="number"
                                                          groupingUsed="true"
                                                          minFractionDigits="0"
                                                          maxFractionDigits="0" /> ₫
                                    </td>

                                    <td class="text-center">
                                        <c:out value="${item.quantity}" />
                                    </td>

                                    <td class="text-right order-subtotal-cell">
                                        <fmt:formatNumber value="${item.subtotal}"
                                                          type="number"
                                                          groupingUsed="true"
                                                          minFractionDigits="0"
                                                          maxFractionDigits="0" /> ₫
                                    </td>

                                    <td class="text-center">
                                        <c:if test="${isDelivered or (isCompleted and paymentStatus eq 'PAID')}">
                                            <c:choose>
                                                <c:when test="${reviewedOrderItemMap[item.id]}">
                                                    <span class="order-pill ok order-review-done-pill">Đã gửi đánh giá</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${pageContext.request.contextPath}/orders/review?orderId=${order.id}&productId=${item.productId}"
                                                       class="order-review-btn">
                                                        Đánh giá
                                                    </a>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <section class="order-card payment-card">
            <div class="order-card-head">
                <h2 class="order-card-title">Tổng kết thanh toán</h2>
            </div>

            <div class="payment-summary">
                <div class="summary-line">
                    <span>Giảm giá</span>
                    <strong>
                        -
                        <fmt:formatNumber value="${empty order.couponDiscount ? 0 : order.couponDiscount}"
                                          type="number"
                                          groupingUsed="true"
                                          minFractionDigits="0"
                                          maxFractionDigits="0" /> ₫
                    </strong>
                </div>

                <div class="summary-line">
                    <span>Phí vận chuyển</span>
                    <strong>
                        <fmt:formatNumber value="${empty order.shippingFee ? 0 : order.shippingFee}"
                                          type="number"
                                          groupingUsed="true"
                                          minFractionDigits="0"
                                          maxFractionDigits="0" /> ₫
                    </strong>
                </div>

                <div class="summary-line summary-total">
                    <span>Tổng tiền</span>
                    <strong>
                        <fmt:formatNumber value="${empty order.total ? 0 : order.total}"
                                          type="number"
                                          groupingUsed="true"
                                          minFractionDigits="0"
                                          maxFractionDigits="0" /> ₫
                    </strong>
                </div>
            </div>
        </section>

    </div>
</section>

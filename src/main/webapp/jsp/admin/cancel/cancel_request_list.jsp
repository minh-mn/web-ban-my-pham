<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Yêu cầu hủy đơn" scope="request"/>
<c:set var="activeMenu" value="cancelRequests" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-return.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="cancelTotal" value="0"/>
<c:set var="cancelPendingCount" value="0"/>
<c:set var="cancelApprovedCount" value="0"/>
<c:set var="cancelRejectedCount" value="0"/>

<c:forEach var="statReq" items="${cancelRequests}">
    <c:set var="cancelTotal" value="${cancelTotal + 1}"/>

    <c:choose>
        <c:when test="${statReq.status == 'REQUESTED'}">
            <c:set var="cancelPendingCount" value="${cancelPendingCount + 1}"/>
        </c:when>
        <c:when test="${statReq.status == 'APPROVED' || statReq.status == 'CANCELLED' || statReq.status == 'CANCELED'}">
            <c:set var="cancelApprovedCount" value="${cancelApprovedCount + 1}"/>
        </c:when>
        <c:when test="${statReq.status == 'REJECTED'}">
            <c:set var="cancelRejectedCount" value="${cancelRejectedCount + 1}"/>
        </c:when>
    </c:choose>
</c:forEach>

<main class="admin-main">
    <div class="admin-container admin-cancel-page">

        <section class="admin-cancel-hero">
            <div class="admin-cancel-hero__content">
                <span class="admin-cancel-eyebrow">BÁN HÀNG &amp; ĐƠN HÀNG</span>
                <h1 class="admin-cancel-title">Yêu cầu hủy đơn</h1>
                <p class="admin-cancel-subtitle">
                    Duyệt hoặc từ chối yêu cầu hủy đơn từ khách hàng. Khi duyệt, đơn sẽ chuyển sang trạng thái đã hủy
                    và thông tin hoàn tiền được ghi nhận theo phương thức xử lý đã chọn.
                </p>
            </div>

            <div class="admin-cancel-hero__actions">
                <span class="admin-chip admin-chip--brand">
                    <c:out value="${cancelTotal}"/> yêu cầu
                </span>
            </div>
        </section>

        <section class="admin-cancel-summary">
            <div class="admin-cancel-stat admin-cancel-stat--total">
                <span class="admin-cancel-stat__icon">❌</span>
                <span class="admin-cancel-stat__label">Tổng yêu cầu</span>
                <strong class="admin-cancel-stat__value">
                    <c:out value="${cancelTotal}"/>
                </strong>
                <span class="admin-cancel-stat__note">Tất cả yêu cầu hủy đơn</span>
            </div>

            <div class="admin-cancel-stat admin-cancel-stat--pending">
                <span class="admin-cancel-stat__icon">⏳</span>
                <span class="admin-cancel-stat__label">Chờ xử lý</span>
                <strong class="admin-cancel-stat__value">
                    <c:out value="${cancelPendingCount}"/>
                </strong>
                <span class="admin-cancel-stat__note">Cần admin phản hồi</span>
            </div>

            <div class="admin-cancel-stat admin-cancel-stat--approved">
                <span class="admin-cancel-stat__icon">✅</span>
                <span class="admin-cancel-stat__label">Đã duyệt</span>
                <strong class="admin-cancel-stat__value">
                    <c:out value="${cancelApprovedCount}"/>
                </strong>
                <span class="admin-cancel-stat__note">Đơn đã được hủy</span>
            </div>

            <div class="admin-cancel-stat admin-cancel-stat--rejected">
                <span class="admin-cancel-stat__icon">🚫</span>
                <span class="admin-cancel-stat__label">Đã từ chối</span>
                <strong class="admin-cancel-stat__value">
                    <c:out value="${cancelRejectedCount}"/>
                </strong>
                <span class="admin-cancel-stat__note">Yêu cầu không được duyệt</span>
            </div>
        </section>

        <c:if test="${not empty success}">
            <div class="return-alert success">
                <c:out value="${success}" />
            </div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="return-alert error">
                <c:out value="${error}" />
            </div>
        </c:if>

        <section class="admin-card admin-cancel-list-card">
            <div class="admin-card__body">
                <div class="admin-cancel-section-head">
                    <div>
                        <h2 class="admin-cancel-section-title">Danh sách yêu cầu hủy</h2>
                        <p class="admin-cancel-section-desc">
                            Kiểm tra đơn hàng, khách hàng, lý do hủy, trạng thái xử lý và thông tin hoàn tiền.
                        </p>
                    </div>

                    <span class="admin-chip admin-chip--brand">
                        <c:out value="${cancelTotal}"/> yêu cầu
                    </span>
                </div>

                <c:choose>
                    <c:when test="${empty cancelRequests}">
                        <div class="admin-cancel-empty">
                            <div class="admin-cancel-empty__icon">❌</div>
                            <div>
                                <h3>Chưa có yêu cầu hủy đơn</h3>
                                <p>
                                    Khi khách hàng gửi yêu cầu hủy đơn, danh sách sẽ hiển thị tại đây để admin duyệt hoặc từ chối.
                                </p>
                            </div>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="admin-cancel-table-wrap">
                            <table class="admin-table admin-cancel-table">
                                <thead>
                                <tr>
                                    <th class="admin-cancel-col-id">ID</th>
                                    <th class="admin-cancel-col-order">Đơn hàng</th>
                                    <th class="admin-cancel-col-customer">Khách hàng</th>
                                    <th class="admin-cancel-col-reason">Lý do hủy</th>
                                    <th class="admin-cancel-col-status">Trạng thái</th>
                                    <th class="admin-cancel-col-refund">Hoàn tiền</th>
                                    <th class="admin-cancel-col-date">Ngày gửi</th>
                                    <th class="admin-cancel-col-actions">Xử lý</th>
                                </tr>
                                </thead>

                                <tbody>
                                <c:forEach var="req" items="${cancelRequests}">
                                    <tr class="${req.status == 'REQUESTED' ? 'admin-cancel-row--pending' : ''}">
                                        <td class="admin-cancel-id-cell">
                                            <strong>#<c:out value="${req.id}" /></strong>
                                        </td>

                                        <td>
                                            <div class="admin-cancel-order">
                                                <a class="return-order-link"
                                                   href="${pageContext.request.contextPath}/admin/orders/detail?id=${req.orderId}">
                                                    Đơn #<c:out value="${req.orderId}" />
                                                </a>
                                                <span>Tổng: <strong><c:out value="${req.orderTotalVnd}" /> ₫</strong></span>
                                                <span>
                                                    Thanh toán:
                                                    <strong><c:out value="${req.paymentMethod}" /></strong>
                                                    /
                                                    <strong><c:out value="${req.paymentStatus}" /></strong>
                                                </span>
                                            </div>
                                        </td>

                                        <td>
                                            <div class="admin-cancel-customer">
                                                <span class="admin-cancel-customer__avatar">KH</span>
                                                <span class="admin-cancel-customer__body">
                                                    <strong><c:out value="${req.customerName}" /></strong>
                                                    <small><c:out value="${req.username}" /></small>
                                                </span>
                                            </div>
                                        </td>

                                        <td>
                                            <div class="return-reason admin-cancel-reason">
                                                <c:out value="${req.reason}" />
                                            </div>

                                            <c:if test="${not empty req.adminNote}">
                                                <div class="return-admin-note admin-cancel-admin-note">
                                                    <strong>Phản hồi:</strong>
                                                    <c:out value="${req.adminNote}" />
                                                </div>
                                            </c:if>
                                        </td>

                                        <td>
                                            <span class="return-pill ${req.statusCssClass}">
                                                <c:out value="${req.statusLabel}" />
                                            </span>
                                        </td>

                                        <td>
                                            <div class="admin-cancel-refund">
                                                <strong><c:out value="${req.refundAmountVnd}" /> ₫</strong>
                                                <span><c:out value="${req.refundMethodLabel}" /></span>
                                            </div>
                                        </td>

                                        <td>
                                            <div class="admin-cancel-time">
                                                <strong>
                                                    <fmt:formatDate value="${req.requestedAtDate}" pattern="dd/MM/yyyy" />
                                                </strong>
                                                <span>
                                                    <fmt:formatDate value="${req.requestedAtDate}" pattern="HH:mm" />
                                                </span>

                                                <c:if test="${not empty req.processedAtDate}">
                                                    <small>
                                                        Xử lý:
                                                        <fmt:formatDate value="${req.processedAtDate}" pattern="dd/MM/yyyy HH:mm" />
                                                    </small>
                                                </c:if>
                                            </div>
                                        </td>

                                        <td class="admin-cancel-action-cell">
                                            <c:choose>
                                                <c:when test="${req.status == 'REQUESTED'}">
                                                    <form class="return-action-form admin-cancel-action-form" method="post"
                                                          action="${pageContext.request.contextPath}/admin/cancel-requests">
                                                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                                                        <input type="hidden" name="id" value="${req.id}" />

                                                        <div class="return-form-grid admin-cancel-form-grid">
                                                            <input type="text" name="refundAmount"
                                                                   value="${req.refundAmountVnd}"
                                                                   placeholder="Số tiền hoàn" />
                                                            <select name="refundMethod">
                                                                <option value="MANUAL">Thủ công</option>
                                                                <option value="VNPAY" ${req.refundMethod == 'VNPAY' ? 'selected' : ''}>VNPay</option>
                                                                <option value="BANK_TRANSFER" ${req.refundMethod == 'BANK_TRANSFER' ? 'selected' : ''}>Chuyển khoản</option>
                                                                <option value="CASH" ${req.refundMethod == 'CASH' ? 'selected' : ''}>Tiền mặt</option>
                                                                <option value="STORE_CREDIT" ${req.refundMethod == 'STORE_CREDIT' ? 'selected' : ''}>Ví cửa hàng</option>
                                                            </select>
                                                        </div>

                                                        <textarea name="adminNote" maxlength="1000"
                                                                  placeholder="Ghi chú phản hồi cho khách..."></textarea>

                                                        <div class="return-actions admin-cancel-actions">
                                                            <button type="submit" name="action" value="approve"
                                                                    class="return-btn ok"
                                                                    onclick="return confirm('Duyệt hủy đơn này?');">
                                                                Duyệt hủy
                                                            </button>
                                                            <button type="submit" name="action" value="reject"
                                                                    class="return-btn danger"
                                                                    onclick="return confirm('Từ chối yêu cầu hủy này?');">
                                                                Từ chối
                                                            </button>
                                                        </div>
                                                    </form>
                                                </c:when>

                                                <c:otherwise>
                                                    <span class="admin-cancel-done">Đã xử lý</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

    </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

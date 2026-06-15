<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Yêu cầu hoàn hàng" scope="request"/>
<c:set var="activeMenu" value="returns" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="returnTotal" value="${empty returnRequests ? 0 : fn:length(returnRequests)}" />
<c:set var="returnPendingCount" value="0" />
<c:set var="returnProcessingCount" value="0" />
<c:set var="returnDoneCount" value="0" />
<c:set var="returnRejectedCount" value="0" />

<c:forEach var="returnStat" items="${returnRequests}">
    <c:choose>
        <c:when test="${returnStat.statusCssClass == 'warning'}">
            <c:set var="returnPendingCount" value="${returnPendingCount + 1}" />
        </c:when>
        <c:when test="${returnStat.statusCssClass == 'info'}">
            <c:set var="returnProcessingCount" value="${returnProcessingCount + 1}" />
        </c:when>
        <c:when test="${returnStat.statusCssClass == 'ok'}">
            <c:set var="returnDoneCount" value="${returnDoneCount + 1}" />
        </c:when>
        <c:when test="${returnStat.statusCssClass == 'danger'}">
            <c:set var="returnRejectedCount" value="${returnRejectedCount + 1}" />
        </c:when>
    </c:choose>
</c:forEach>

<main class="admin-main">
    <div class="admin-container admin-return-page">

        <section class="admin-return-hero">
            <div class="admin-return-hero__content">
                <span class="admin-return-eyebrow">BÁN HÀNG &amp; ĐƠN HÀNG</span>
                <h1 class="admin-return-title">Yêu cầu hoàn hàng</h1>
                <p class="admin-return-subtitle">
                    Quản lý yêu cầu hoàn hàng, kiểm tra lý do khách gửi, duyệt hoặc từ chối yêu cầu
                    và cập nhật trạng thái hoàn tiền sau khi xử lý.
                </p>
            </div>

            <div class="admin-return-hero__actions">
                <span class="admin-chip admin-chip--brand">
                    <c:out value="${returnTotal}" /> yêu cầu
                </span>
            </div>
        </section>

        <c:if test="${not empty success}">
            <div class="return-alert success"><c:out value="${success}" /></div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="return-alert error"><c:out value="${error}" /></div>
        </c:if>

        <section class="admin-return-summary">
            <div class="admin-return-stat admin-return-stat--total">
                <span class="admin-return-stat__icon">↩️</span>
                <span class="admin-return-stat__label">Tổng yêu cầu</span>
                <strong class="admin-return-stat__value">
                    <c:out value="${returnTotal}" />
                </strong>
                <span class="admin-return-stat__note">Tất cả yêu cầu hoàn hàng</span>
            </div>

            <div class="admin-return-stat admin-return-stat--pending">
                <span class="admin-return-stat__icon">⏳</span>
                <span class="admin-return-stat__label">Chờ xử lý</span>
                <strong class="admin-return-stat__value">
                    <c:out value="${returnPendingCount}" />
                </strong>
                <span class="admin-return-stat__note">Cần admin kiểm tra và phản hồi</span>
            </div>

            <div class="admin-return-stat admin-return-stat--processing">
                <span class="admin-return-stat__icon">📦</span>
                <span class="admin-return-stat__label">Đang xử lý</span>
                <strong class="admin-return-stat__value">
                    <c:out value="${returnProcessingCount}" />
                </strong>
                <span class="admin-return-stat__note">Đã duyệt hoặc đang chờ nhận hàng</span>
            </div>

            <div class="admin-return-stat admin-return-stat--done">
                <span class="admin-return-stat__icon">💳</span>
                <span class="admin-return-stat__label">Đã hoàn tất</span>
                <strong class="admin-return-stat__value">
                    <c:out value="${returnDoneCount}" />
                </strong>
                <span class="admin-return-stat__note">Đã nhận hàng hoặc đã hoàn tiền</span>
            </div>
        </section>

        <section class="admin-card admin-return-list-card">
            <div class="admin-card__body">
                <div class="admin-return-section-head">
                    <div>
                        <h2 class="admin-return-section-title">Danh sách yêu cầu hoàn hàng</h2>
                        <p class="admin-return-section-desc">
                            Kiểm tra đơn hàng, lý do hoàn hàng, số tiền hoàn và cập nhật trạng thái xử lý.
                        </p>
                    </div>

                    <div class="admin-return-head-chips">
                        <span class="admin-chip admin-chip--warning">
                            <c:out value="${returnPendingCount}" /> chờ xử lý
                        </span>
                        <span class="admin-chip admin-chip--success">
                            <c:out value="${returnDoneCount}" /> hoàn tất
                        </span>
                    </div>
                </div>

                <c:choose>
                    <c:when test="${empty returnRequests}">
                        <div class="admin-return-empty">
                            <div class="admin-return-empty__icon">↩️</div>
                            <div>
                                <h3>Chưa có yêu cầu hoàn hàng</h3>
                                <p>Khi khách gửi yêu cầu hoàn hàng, danh sách sẽ hiển thị tại đây để admin xử lý.</p>
                            </div>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="admin-return-table-wrap">
                            <table class="admin-table admin-return-table">
                                <thead>
                                <tr>
                                    <th class="admin-return-col-id">Mã</th>
                                    <th class="admin-return-col-order">Đơn hàng</th>
                                    <th class="admin-return-col-customer">Khách hàng</th>
                                    <th class="admin-return-col-reason">Lý do</th>
                                    <th class="admin-return-col-money">Số tiền</th>
                                    <th class="admin-return-col-status">Trạng thái</th>
                                    <th class="admin-return-col-date">Ngày gửi</th>
                                    <th class="admin-return-col-actions">Thao tác</th>
                                </tr>
                                </thead>

                                <tbody>
                                <c:forEach var="r" items="${returnRequests}">
                                    <tr class="admin-return-row admin-return-row--${r.statusCssClass}">
                                        <td class="admin-return-id-cell">
                                            #<c:out value="${r.id}" />
                                        </td>

                                        <td>
                                            <div class="admin-return-order">
                                                <a class="return-order-link" href="${ctx}/admin/orders?action=detail&id=${r.orderId}">
                                                    #<c:out value="${r.orderId}" />
                                                </a>
                                                <span>Chi tiết đơn hàng</span>
                                            </div>
                                        </td>

                                        <td>
                                            <div class="admin-return-customer">
                                                <span class="admin-return-customer__avatar">KH</span>
                                                <div class="admin-return-customer__body">
                                                    <strong>
                                                        <c:out value="${empty r.customerName ? r.username : r.customerName}" />
                                                    </strong>
                                                    <small><c:out value="${r.username}" /></small>
                                                </div>
                                            </div>
                                        </td>

                                        <td>
                                            <div class="return-reason admin-return-reason">
                                                <c:out value="${r.reason}" />
                                            </div>
                                            <c:if test="${not empty r.adminNote}">
                                                <div class="return-admin-note admin-return-admin-note">
                                                    Admin: <c:out value="${r.adminNote}" />
                                                </div>
                                            </c:if>
                                        </td>

                                        <td>
                                            <div class="admin-return-money">
                                                <strong><c:out value="${r.refundAmountVnd}" /> ₫</strong>
                                                <span>Số tiền hoàn</span>
                                            </div>
                                        </td>

                                        <td>
                                            <div class="admin-return-status">
                                                <span class="return-pill ${r.statusCssClass}">
                                                    <c:out value="${r.statusLabel}" />
                                                </span>
                                                <small><c:out value="${r.refundMethodLabel}" /></small>
                                            </div>
                                        </td>

                                        <td>
                                            <div class="admin-return-time">
                                                <strong>
                                                    <fmt:formatDate value="${r.requestedAtDate}" pattern="dd/MM/yyyy" />
                                                </strong>
                                                <span>
                                                    <fmt:formatDate value="${r.requestedAtDate}" pattern="HH:mm" />
                                                </span>
                                            </div>
                                        </td>

                                        <td class="admin-return-action-cell">
                                            <form method="post"
                                                  action="${ctx}/admin/returns"
                                                  class="return-action-form admin-return-action-form">
                                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                                                <input type="hidden" name="id" value="${r.id}" />

                                                <div class="return-form-grid admin-return-form-grid">
                                                    <select name="refundMethod" aria-label="Phương thức hoàn tiền">
                                                        <option value="MANUAL" <c:if test="${r.refundMethod == 'MANUAL' || empty r.refundMethod}">selected="selected"</c:if>>Thủ công</option>
                                                        <option value="VNPAY" <c:if test="${r.refundMethod == 'VNPAY'}">selected="selected"</c:if>>VNPay</option>
                                                        <option value="BANK_TRANSFER" <c:if test="${r.refundMethod == 'BANK_TRANSFER'}">selected="selected"</c:if>>Chuyển khoản</option>
                                                        <option value="CASH" <c:if test="${r.refundMethod == 'CASH'}">selected="selected"</c:if>>Tiền mặt</option>
                                                        <option value="STORE_CREDIT" <c:if test="${r.refundMethod == 'STORE_CREDIT'}">selected="selected"</c:if>>Ví cửa hàng</option>
                                                    </select>
                                                    <input type="text"
                                                           name="refundAmount"
                                                           value="${r.refundAmount}"
                                                           placeholder="Số tiền" />
                                                </div>

                                                <textarea name="adminNote"
                                                          placeholder="Ghi chú admin"><c:out value="${r.adminNote}" /></textarea>

                                                <div class="return-actions admin-return-actions">
                                                    <button name="action" value="approve" class="return-btn info" type="submit">Duyệt</button>
                                                    <button name="action" value="reject" class="return-btn danger" type="submit">Từ chối</button>
                                                    <button name="action" value="returned" class="return-btn warning" type="submit">Đã nhận hàng</button>
                                                    <button name="action" value="refunded" class="return-btn ok" type="submit">Đã hoàn tiền</button>
                                                </div>
                                            </form>
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

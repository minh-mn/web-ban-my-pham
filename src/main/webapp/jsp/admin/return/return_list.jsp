<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Yêu cầu hoàn hàng" scope="request"/>
<c:set var="activeMenu" value="returns" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-return.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
    <div class="admin-container">

        <div class="admin-topbar">
            <div>
                <h1 class="admin-h1">Yêu cầu hoàn hàng</h1>
                <p class="admin-subtext">Quản lý yêu cầu hoàn hàng, duyệt/từ chối và cập nhật trạng thái hoàn tiền.</p>
            </div>
        </div>

        <c:if test="${not empty success}">
            <div class="return-alert success"><c:out value="${success}" /></div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="return-alert error"><c:out value="${error}" /></div>
        </c:if>

        <div class="admin-card">
            <div class="admin-card__body">

                <c:choose>
                    <c:when test="${empty returnRequests}">
                        <div class="admin-empty">Chưa có yêu cầu hoàn hàng.</div>
                    </c:when>

                    <c:otherwise>
                        <div class="return-table-wrap">
                            <table class="admin-table return-table">
                                <thead>
                                <tr>
                                    <th style="width:80px;">Mã</th>
                                    <th style="width:92px;">Đơn</th>
                                    <th style="width:150px;">Khách hàng</th>
                                    <th>Lý do</th>
                                    <th style="width:145px;">Số tiền</th>
                                    <th style="width:150px;">Trạng thái</th>
                                    <th style="width:150px;">Ngày gửi</th>
                                    <th style="width:310px;">Thao tác</th>
                                </tr>
                                </thead>

                                <tbody>
                                <c:forEach var="r" items="${returnRequests}">
                                    <tr>
                                        <td>#${r.id}</td>
                                        <td>
                                            <a class="return-order-link" href="${pageContext.request.contextPath}/admin/orders?action=detail&id=${r.orderId}">
                                                #${r.orderId}
                                            </a>
                                        </td>
                                        <td>
                                            <strong><c:out value="${empty r.customerName ? r.username : r.customerName}" /></strong>
                                            <div class="return-muted"><c:out value="${r.username}" /></div>
                                        </td>
                                        <td>
                                            <div class="return-reason"><c:out value="${r.reason}" /></div>
                                            <c:if test="${not empty r.adminNote}">
                                                <div class="return-admin-note">Admin: <c:out value="${r.adminNote}" /></div>
                                            </c:if>
                                        </td>
                                        <td><strong><c:out value="${r.refundAmountVnd}" /> ₫</strong></td>
                                        <td>
                        <span class="return-pill ${r.statusCssClass}">
                          <c:out value="${r.statusLabel}" />
                        </span>
                                            <div class="return-muted"><c:out value="${r.refundMethodLabel}" /></div>
                                        </td>
                                        <td>
                                            <fmt:formatDate value="${r.requestedAtDate}" pattern="dd/MM/yyyy HH:mm" />
                                        </td>
                                        <td>
                                            <form method="post" action="${pageContext.request.contextPath}/admin/returns" class="return-action-form">
                                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                                                <input type="hidden" name="id" value="${r.id}" />

                                                <div class="return-form-grid">
                                                    <select name="refundMethod">
                                                        <option value="MANUAL" <c:if test="${r.refundMethod == 'MANUAL' || empty r.refundMethod}">selected="selected"</c:if>>Thủ công</option>
                                                        <option value="VNPAY" <c:if test="${r.refundMethod == 'VNPAY'}">selected="selected"</c:if>>VNPay</option>
                                                        <option value="BANK_TRANSFER" <c:if test="${r.refundMethod == 'BANK_TRANSFER'}">selected="selected"</c:if>>Chuyển khoản</option>
                                                        <option value="CASH" <c:if test="${r.refundMethod == 'CASH'}">selected="selected"</c:if>>Tiền mặt</option>
                                                        <option value="STORE_CREDIT" <c:if test="${r.refundMethod == 'STORE_CREDIT'}">selected="selected"</c:if>>Ví cửa hàng</option>
                                                    </select>
                                                    <input type="text" name="refundAmount" value="${r.refundAmount}" placeholder="Số tiền" />
                                                </div>

                                                <textarea name="adminNote" placeholder="Ghi chú admin"><c:out value="${r.adminNote}" /></textarea>

                                                <div class="return-actions">
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
        </div>

    </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

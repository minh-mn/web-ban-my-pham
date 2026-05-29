<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Admin - Yêu cầu hủy đơn" scope="request"/>
<c:set var="activeMenu" value="cancelRequests" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-return.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
    <div class="admin-container">

        <div class="admin-topbar">
            <div>
                <h1 class="admin-h1">Yêu cầu hủy đơn</h1>
                <p class="admin-subtitle">
                    Duyệt hoặc từ chối yêu cầu hủy đơn từ khách hàng. Khi duyệt, đơn sẽ chuyển sang trạng thái đã hủy.
                </p>
            </div>
        </div>

        <c:if test="${not empty success}">
            <div class="return-alert success"><c:out value="${success}" /></div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="return-alert error"><c:out value="${error}" /></div>
        </c:if>

        <div class="admin-card">
            <div class="return-table-wrap">
                <table class="admin-table return-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Đơn hàng</th>
                        <th>Khách hàng</th>
                        <th>Lý do hủy</th>
                        <th>Trạng thái</th>
                        <th>Hoàn tiền</th>
                        <th>Ngày gửi</th>
                        <th>Xử lý</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty cancelRequests}">
                            <tr>
                                <td colspan="8" class="admin-empty">Chưa có yêu cầu hủy đơn.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="req" items="${cancelRequests}">
                                <tr>
                                    <td>#${req.id}</td>
                                    <td>
                                        <a class="return-order-link"
                                           href="${pageContext.request.contextPath}/admin/orders/detail?id=${req.orderId}">
                                            Đơn #${req.orderId}
                                        </a>
                                        <div class="return-muted">
                                            Tổng: <c:out value="${req.orderTotalVnd}" /> ₫
                                        </div>
                                        <div class="return-muted">
                                            Thanh toán: <c:out value="${req.paymentMethod}" /> / <c:out value="${req.paymentStatus}" />
                                        </div>
                                    </td>
                                    <td>
                                        <strong><c:out value="${req.customerName}" /></strong>
                                        <div class="return-muted"><c:out value="${req.username}" /></div>
                                    </td>
                                    <td>
                                        <div class="return-reason"><c:out value="${req.reason}" /></div>
                                        <c:if test="${not empty req.adminNote}">
                                            <div class="return-admin-note">
                                                <strong>Phản hồi:</strong> <c:out value="${req.adminNote}" />
                                            </div>
                                        </c:if>
                                    </td>
                                    <td>
                                        <span class="return-pill ${req.statusCssClass}">
                                            <c:out value="${req.statusLabel}" />
                                        </span>
                                    </td>
                                    <td>
                                        <strong><c:out value="${req.refundAmountVnd}" /> ₫</strong>
                                        <div class="return-muted"><c:out value="${req.refundMethodLabel}" /></div>
                                    </td>
                                    <td>
                                        <fmt:formatDate value="${req.requestedAtDate}" pattern="dd/MM/yyyy HH:mm" />
                                        <c:if test="${not empty req.processedAtDate}">
                                            <div class="return-muted">
                                                Xử lý:
                                                <fmt:formatDate value="${req.processedAtDate}" pattern="dd/MM/yyyy HH:mm" />
                                            </div>
                                        </c:if>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${req.status == 'REQUESTED'}">
                                                <form class="return-action-form" method="post"
                                                      action="${pageContext.request.contextPath}/admin/cancel-requests">
                                                    <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                                                    <input type="hidden" name="id" value="${req.id}" />

                                                    <div class="return-form-grid">
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

                                                    <div class="return-actions">
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
                                                <span class="return-muted">Đã xử lý</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

    </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

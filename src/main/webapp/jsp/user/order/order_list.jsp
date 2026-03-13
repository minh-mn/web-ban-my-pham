<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

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
                  <th>Trạng thái</th>
                  <th>Chi tiết</th>
                </tr>
              </thead>

              <tbody>
                <c:forEach var="order" items="${orders}">
                  <tr id="order-${order.id}">
                    <td>#${order.id}</td>

                    <%-- ===== DATE ===== --%>
                    <td>
                      <c:choose>
                        <c:when test="${not empty order.createdAtDate}">
                          <fmt:formatDate value="${order.createdAtDate}" pattern="dd/MM/yyyy HH:mm" />
                        </c:when>
                        <c:otherwise>--</c:otherwise>
                      </c:choose>
                    </td>

                    <%-- ===== TOTAL ===== --%>
                    <td class="price">
                      <c:choose>
                        <c:when test="${not empty order.totalVnd}">
                          <c:out value="${order.totalVnd}" /> ₫
                        </c:when>
                        <c:otherwise>
                          <fmt:formatNumber value="${order.total}" type="number" groupingUsed="true" /> ₫
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <%-- ===== STATUS ===== --%>
                    <td>
                      <c:choose>

                        <%-- ADMIN: cho đổi trạng thái ngay tại /orders --%>
                        <c:when test="${not empty sessionScope.user and sessionScope.user.admin}">
                          <form method="post"
                                action="${pageContext.request.contextPath}/admin/order/update-status"
                                class="admin-status-form">

                            <%-- CSRF: match CsrfFilter (sessionKey=CSRF_TOKEN, param=csrf_token) --%>
                            <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />

                            <input type="hidden" name="orderId" value="${order.id}" />

                            <%-- ✅ Chỉ refresh về /orders --%>
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
                                  <option value="${order.status}" selected="selected">${order.status}</option>
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
                                ${order.statusLabel}
                              </c:when>
                              <c:otherwise>
                                ${order.status}
                              </c:otherwise>
                            </c:choose>
                          </span>
                        </c:otherwise>

                      </c:choose>
                    </td>

                    <%-- ===== DETAIL ===== --%>
                    <td>
                      <a href="${pageContext.request.contextPath}/orders/detail?id=${order.id}"
                         class="btn-outline small">Xem</a>
                    </td>

                  </tr>
                </c:forEach>
              </tbody>
            </table>

            <p class="order-note">💡 Tổng tiền đã bao gồm toàn bộ khuyến mãi và ưu đãi.</p>

          </div>
        </c:when>

        <c:otherwise>
          <p class="empty-text">Bạn chưa có đơn hàng nào.</p>
        </c:otherwise>
      </c:choose>

    </div>

  </div>
</section>

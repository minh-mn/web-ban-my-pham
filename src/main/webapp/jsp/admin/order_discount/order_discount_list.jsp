<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:set var="pageTitle" value="ADMIN | Order Discounts" scope="request"/>
<c:set var="activeMenu" value="order_discount" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Giảm giá đơn hàng</h1>
        <p class="admin-subtext">Quản lý chương trình giảm theo tổng tiền đơn và thời gian hiệu lực.</p>
      </div>

      <a class="admin-btn admin-btn--primary"
         href="${pageContext.request.contextPath}/admin/order-discounts?action=new">
        + Tạo mới
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:choose>
          <c:when test="${empty discounts}">
            <div class="admin-empty">
              Chưa có chương trình giảm giá. Hãy tạo mới để áp dụng cho đơn hàng.
            </div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
                <tr>
                  <th style="width:90px;">ID</th>
                  <th>Tên</th>
                  <th style="width:110px;">% giảm</th>
                  <th style="width:170px;">Đơn tối thiểu</th>
                  <th style="width:170px;">Giảm tối đa</th>
                  <th style="width:230px;">Hiệu lực</th>
                  <th style="width:140px;">Trạng thái</th>
                  <th style="width:240px;">Thao tác</th>
                </tr>
              </thead>

              <tbody>
                <c:forEach var="d" items="${discounts}">
                  <tr>
                    <td>#${d.id}</td>

                    <td>
                      <div style="font-weight:850;">
                        <c:out value="${d.name}"/>
                      </div>
                    </td>

                    <td><c:out value="${d.discountPercent}"/>%</td>

                    <td>
                      <strong>
                        <fmt:formatNumber value="${d.minOrderValue * 1000}"
                                          type="number" groupingUsed="true"
                                          minFractionDigits="0" maxFractionDigits="0"/>
                      </strong> ₫
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${not empty d.maxDiscountAmount}">
                          <strong>
                            <fmt:formatNumber value="${d.maxDiscountAmount * 1000}"
                                              type="number" groupingUsed="true"
                                              minFractionDigits="0" maxFractionDigits="0"/>
                          </strong> ₫
                        </c:when>
                        <c:otherwise>
                          <span class="admin-muted">Không giới hạn</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <div class="admin-muted">
                        Từ: <c:out value="${d.startDate}"/>
                      </div>
                      <div class="admin-muted">
                        Đến: <c:out value="${d.endDate}"/>
                      </div>
                    </td>

                    <td class="admin-status-cell">
                      <c:choose>
                        <c:when test="${d.active}">
                          <span class="admin-pill admin-pill--ok">ACTIVE</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill admin-pill--danger">INACTIVE</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td class="admin-actions">
                      <a class="admin-btn"
                         href="${pageContext.request.contextPath}/admin/order-discounts?action=edit&id=${d.id}">
                        Sửa
                      </a>

                      <form method="post"
                            action="${pageContext.request.contextPath}/admin/order-discounts"
                            class="admin-inline"
                            onsubmit="return confirm('Xóa chương trình giảm giá này?');">

                        <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
                        <%@ include file="/jsp/common/csrf.jspf" %>

                        <input type="hidden" name="action" value="delete"/>
                        <input type="hidden" name="id" value="${d.id}"/>
                        <button type="submit" class="admin-btn admin-btn--danger">Xóa</button>
                      </form>
                    </td>

                  </tr>
                </c:forEach>
              </tbody>

            </table>
          </c:otherwise>
        </c:choose>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

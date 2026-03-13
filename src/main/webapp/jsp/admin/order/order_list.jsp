<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="pageTitle" value="ADMIN | Orders" scope="request"/>
<c:set var="activeMenu" value="orders" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Đơn hàng</h1>
        <p class="admin-subtext">Danh sách đơn hàng mới nhất.</p>
      </div>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:choose>
          <c:when test="${empty orders}">
            <div class="admin-empty">Chưa có đơn hàng.</div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
                <tr>
                  <th style="width:90px;">Mã</th>
                  <th style="width:110px;">User</th>
                  <th>Khách hàng</th>
                  <th style="width:170px;">Tổng tiền</th>
                  <th style="width:160px;">Trạng thái</th>
                  <th style="width:200px;">Ngày tạo</th>
                  <th style="width:160px;">Thao tác</th>
                </tr>
              </thead>

              <tbody>
                <c:forEach var="o" items="${orders}">
                  <tr>
                    <td>#${o.id}</td>
                    <td>${o.userId}</td>

                    <td>
                      <div style="font-weight:850;">
                        <c:out value="${o.fullName}"/>
                      </div>
                    </td>

                    <td>
                      <strong>
                        <c:choose>
                          <c:when test="${not empty o.totalVnd}">
                            <fmt:formatNumber
                              value="${fn:replace(o.totalVnd,'.','')}"
                              type="number"
                              groupingUsed="true"
                              minFractionDigits="0"
                              maxFractionDigits="0"/>
                          </c:when>
                          <c:otherwise>0</c:otherwise>
                        </c:choose>
                      </strong> ₫
                    </td>

                    <td class="admin-status-cell">
                      <span class="admin-pill">
                        <c:out value="${o.statusLabel}"/>
                      </span>
                    </td>

                    <td>
                      <fmt:formatDate value="${o.createdAtDate}" pattern="dd/MM/yyyy HH:mm"/>
                    </td>

                    <td class="admin-actions">
                      <a class="admin-btn"
                         href="${pageContext.request.contextPath}/admin/orders?action=detail&id=${o.id}">
                        Chi tiết
                      </a>
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

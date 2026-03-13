<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:set var="pageTitle" value="ADMIN | Dashboard" scope="request"/>
<c:set var="activeMenu" value="dashboard" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-center.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Dashboard</h1>
        <p class="admin-subtext">Tổng quan nhanh hệ thống.</p>
      </div>
    </div>

    <!-- KPI GRID -->
    <div class="admin-grid admin-grid--3">

      <div class="admin-card">
        <div class="admin-card__body admin-stack">
          <div class="admin-muted" style="font-weight:800; font-size:12px;">Tổng đơn hàng</div>
          <div class="admin-h1">
            <c:out value="${orderCount}"/>
          </div>
          <div class="admin-subtext">Tất cả đơn đã tạo trong hệ thống.</div>
        </div>
      </div>

      <div class="admin-card">
        <div class="admin-card__body admin-stack">
          <div class="admin-muted" style="font-weight:800; font-size:12px;">Tổng doanh thu</div>
          <div class="admin-h1">
            <c:choose>
              <c:when test="${not empty totalRevenue}">
                <c:catch var="revErr">
                  <fmt:formatNumber value="${totalRevenue}" type="number" groupingUsed="true"/> ₫
                </c:catch>
                <c:if test="${not empty revErr}">
                  <c:out value="${totalRevenue}"/> ₫
                </c:if>
              </c:when>
              <c:otherwise>0 ₫</c:otherwise>
            </c:choose>
          </div>
          <div class="admin-subtext">Tổng doanh thu (VND) từ các đơn hợp lệ.</div>
        </div>
      </div>

      <div class="admin-card">
        <div class="admin-card__body admin-stack">
          <div class="admin-muted" style="font-weight:800; font-size:12px;">Gợi ý</div>
          <div class="admin-subtext">
            Bạn có thể bổ sung: đơn hàng mới nhất, top sản phẩm bán chạy, doanh thu theo ngày.
          </div>
          <span class="admin-chip">NEXT: Analytics</span>
        </div>
      </div>

    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

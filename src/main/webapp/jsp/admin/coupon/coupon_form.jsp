<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="pageTitle" value="ADMIN | Form Coupon" scope="request"/>
<c:set var="activeMenu" value="coupons" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">
          <c:choose>
            <c:when test="${mode == 'edit'}">Sửa coupon</c:when>
            <c:otherwise>Thêm coupon</c:otherwise>
          </c:choose>
        </h1>
        <p class="admin-subtext">Thiết lập mã và mức giảm (%).</p>
      </div>

      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/coupons">Quay lại</a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:if test="${not empty error}">
          <div class="admin-alert admin-alert--danger">
            <c:out value="${error}"/>
          </div>
        </c:if>

        <%-- ✅ LocalDate: dùng trực tiếp yyyy-MM-dd --%>
        <c:set var="startDateValue" value=""/>
        <c:set var="endDateValue" value=""/>

        <c:if test="${not empty coupon && not empty coupon.startDate}">
          <c:set var="startDateValue" value="${coupon.startDate}"/>
        </c:if>

        <c:if test="${not empty coupon && not empty coupon.endDate}">
          <c:set var="endDateValue" value="${coupon.endDate}"/>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/coupons"
              class="admin-form">

          <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
          <%@ include file="/jsp/common/csrf.jspf" %>

          <!-- ✅ FIX LỖI VALIDATION: maxUses phải >= 1 (không đổi giao diện) -->
          <input type="hidden" name="maxUses" value="1"/>

          <input type="hidden" name="action" value="${mode == 'edit' ? 'update' : 'create'}"/>

          <c:if test="${mode == 'edit'}">
            <input type="hidden" name="id" value="${coupon.id}"/>
          </c:if>

          <div class="admin-grid-2">

            <div class="admin-field">
              <div class="admin-label">CODE</div>
              <input class="admin-input" type="text" name="code"
                     value="${not empty coupon ? fn:escapeXml(coupon.code) : ''}"
                     maxlength="32" autocomplete="off" required />
              <div class="admin-help">Nên viết hoa, không dấu cách. Ví dụ: NEWYEAR10</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Trạng thái</div>
              <select class="admin-select" name="active">
                <option value="1" ${empty coupon || coupon.active ? "selected" : ""}>ACTIVE</option>
                <option value="0" ${not empty coupon && !coupon.active ? "selected" : ""}>INACTIVE</option>
              </select>
              <div class="admin-help">INACTIVE sẽ không áp dụng được ở checkout.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Giảm (%)</div>
              <input class="admin-input" type="number" name="discountPercent"
                     min="1" max="100" step="1"
                     value="${not empty coupon ? coupon.discountPercent : ''}"
                     required />
              <div class="admin-help">Nhập từ 1 đến 100.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Giảm tối đa (₫) (tuỳ chọn)</div>
              <input class="admin-input" type="number" name="maxDiscountAmount"
                     min="0" step="1"
                     value="${not empty coupon ? coupon.maxDiscountAmount : ''}"
                     placeholder="VD: 50000" />
              <div class="admin-help">Để trống nếu không giới hạn mức giảm tối đa.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Start Date</div>
              <input class="admin-input" type="date" name="startDate" value="${startDateValue}" />
              <div class="admin-help">Để trống nếu hiệu lực ngay.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">End Date</div>
              <input class="admin-input" type="date" name="endDate" value="${endDateValue}" />
              <div class="admin-help">Để trống nếu không giới hạn thời gian.</div>
            </div>

            <c:if test="${mode == 'edit'}">
              <div class="admin-field">
                <div class="admin-label">Đã dùng</div>
                <input class="admin-input" type="number" value="${coupon.usedCount}" disabled />
                <div class="admin-help">Tự tăng khi áp dụng coupon trong đơn hàng.</div>
              </div>
              <div class="admin-field"></div>
            </c:if>

          </div>

          <hr class="admin-divider"/>

          <div class="admin-actions">
            <button class="admin-btn admin-btn--primary" type="submit">Lưu</button>
            <a class="admin-btn" href="${pageContext.request.contextPath}/admin/coupons">Hủy</a>
          </div>

        </form>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

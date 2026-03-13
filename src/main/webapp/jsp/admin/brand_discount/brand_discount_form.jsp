<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="ADMIN | Form Brand Discount" scope="request"/>
<c:set var="activeMenu" value="brandDiscounts" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">
          <c:choose>
            <c:when test="${not empty discount}">Sửa Brand Discount</c:when>
            <c:otherwise>Thêm Brand Discount</c:otherwise>
          </c:choose>
        </h1>
        <p class="admin-subtext">Thiết lập giảm giá theo thương hiệu.</p>
      </div>

      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/brand-discounts">Quay lại</a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <!-- ✅ Convert enum -> String an toàn để so sánh trong JSP -->
        <c:set var="dt" value="${not empty discount ? discount.discountType.toString() : ''}"/>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/brand-discounts"
              class="admin-form">

          <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="${not empty discount ? 'update' : 'create'}">
          <c:if test="${not empty discount}">
            <input type="hidden" name="id" value="${discount.id}">
          </c:if>

          <div class="admin-grid-2">

            <!-- Brand -->
            <div class="admin-field">
              <div class="admin-label">Thương hiệu</div>
              <select class="admin-select" name="brandId" required>
                <option value="">-- Chọn thương hiệu --</option>
                <c:forEach var="b" items="${brands}">
                  <option value="${b.id}" ${not empty discount && discount.brandId == b.id ? 'selected' : ''}>
                    <c:out value="${b.name}"/>
                  </option>
                </c:forEach>
              </select>
              <div class="admin-help">Chọn thương hiệu áp dụng giảm giá.</div>
            </div>

            <!-- Active -->
            <div class="admin-field">
              <div class="admin-label">Trạng thái</div>
              <select class="admin-select" name="active">
                <option value="1" ${empty discount || discount.active ? 'selected' : ''}>ACTIVE</option>
                <option value="0" ${not empty discount && !discount.active ? 'selected' : ''}>INACTIVE</option>
              </select>
              <div class="admin-help">INACTIVE sẽ không áp dụng.</div>
            </div>

            <!-- Discount type -->
            <div class="admin-field">
              <div class="admin-label">Discount Type</div>
              <select class="admin-select" name="discountType" required>
                <option value="PERCENT" ${dt == 'PERCENT' ? 'selected' : ''}>PERCENT</option>
                <option value="AMOUNT" ${dt == 'AMOUNT' ? 'selected' : ''}>AMOUNT</option>
              </select>
              <div class="admin-help">PERCENT: giảm theo %; AMOUNT: giảm theo số tiền.</div>
            </div>

            <!-- Discount value -->
            <div class="admin-field">
              <div class="admin-label">Discount Value</div>
              <input class="admin-input"
                     type="number" step="0.01" min="0"
                     name="discountValue"
                     value="${not empty discount ? discount.discountValue : ''}"
                     required>
              <div class="admin-help">Ví dụ: 10 (10%) hoặc 50000 (giảm 50.000đ).</div>
            </div>

            <!-- Max discount (optional) -->
            <div class="admin-field">
              <div class="admin-label">Max Discount Amount (tuỳ chọn)</div>
              <input class="admin-input"
                     type="number" step="0.01" min="0"
                     name="maxDiscountAmount"
                     value="${not empty discount ? discount.maxDiscountAmount : ''}">
              <div class="admin-help">Dùng khi giảm theo % để giới hạn số tiền giảm tối đa.</div>
            </div>

            <!-- Start date -->
            <div class="admin-field">
              <div class="admin-label">Start Date</div>
              <input class="admin-input"
                     type="date" name="startDate"
                     value="${not empty discount ? discount.startDate : ''}"
                     required>
            </div>

            <!-- End date -->
            <div class="admin-field">
              <div class="admin-label">End Date</div>
              <input class="admin-input"
                     type="date" name="endDate"
                     value="${not empty discount ? discount.endDate : ''}"
                     required>
            </div>

          </div>

          <hr class="admin-divider"/>

          <div class="admin-actions">
            <a class="admin-btn" href="${pageContext.request.contextPath}/admin/brand-discounts">Hủy</a>
            <button class="admin-btn admin-btn--primary" type="submit">Lưu</button>
          </div>

        </form>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

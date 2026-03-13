<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="pageTitle" value="ADMIN | Order Discount" scope="request"/>
<c:set var="activeMenu" value="order_discount" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">
          <c:choose>
            <c:when test="${empty discount}">Tạo giảm giá đơn hàng</c:when>
            <c:otherwise>Chỉnh sửa giảm giá đơn hàng</c:otherwise>
          </c:choose>
        </h1>
        <p class="admin-subtext">Thiết lập điều kiện áp dụng và thời gian hiệu lực.</p>
      </div>

      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/order-discounts">Quay lại</a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:if test="${not empty error}">
          <div class="admin-alert admin-alert--danger">
            <c:out value="${error}"/>
          </div>
        </c:if>

        <%-- LocalDate-safe: input date cần yyyy-MM-dd; LocalDate toString() đáp ứng đúng --%>
        <c:set var="startVal" value=""/>
        <c:set var="endVal" value=""/>
        <c:if test="${not empty discount && not empty discount.startDate}">
          <c:set var="startVal" value="${discount.startDate}"/>
        </c:if>
        <c:if test="${not empty discount && not empty discount.endDate}">
          <c:set var="endVal" value="${discount.endDate}"/>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/order-discounts"
              class="admin-form">

          <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="${empty discount ? 'create' : 'update'}"/>
          <c:if test="${not empty discount}">
            <input type="hidden" name="id" value="${discount.id}"/>
          </c:if>

          <div class="admin-grid-2">

            <div class="admin-field">
              <div class="admin-label">Tên chương trình</div>
              <input class="admin-input" type="text" name="name"
                     value="${not empty discount ? fn:escapeXml(discount.name) : ''}"
                     placeholder="Ví dụ: SALE Tết - Giảm đơn"
                     required />
              <div class="admin-help">Bắt buộc (DB NOT NULL).</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Trạng thái</div>
              <select class="admin-select" name="active">
                <option value="1" ${empty discount || discount.active ? "selected" : ""}>ACTIVE</option>
                <option value="0" ${not empty discount && !discount.active ? "selected" : ""}>INACTIVE</option>
              </select>
              <div class="admin-help">INACTIVE sẽ không áp dụng dù còn thời gian.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Giá trị đơn tối thiểu (VND)</div>
              <input class="admin-input" type="number" name="minOrderValue"
                     value="${not empty discount ? discount.minOrderValue : ''}"
                     min="0" step="1" required />
              <div class="admin-help">Nhập số VND nguyên. (Nếu hệ bạn lưu theo nghìn, xử lý ở servlet/DAO.)</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Phần trăm giảm (%)</div>
              <input class="admin-input" type="number" name="discountPercent"
                     value="${not empty discount ? discount.discountPercent : ''}"
                     min="1" max="100" step="1" required />
              <div class="admin-help">Ví dụ: 10 = giảm 10%.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Giảm tối đa (VND) (tuỳ chọn)</div>
              <input class="admin-input" type="number" name="maxDiscountAmount"
                     value="${not empty discount ? discount.maxDiscountAmount : ''}"
                     min="0" step="1"
                     placeholder="Để trống nếu không giới hạn" />
              <div class="admin-help">Để trống nếu không giới hạn.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Ngày bắt đầu</div>
              <input class="admin-input" type="date" name="startDate" value="${startVal}" required />
            </div>

            <div class="admin-field">
              <div class="admin-label">Ngày kết thúc</div>
              <input class="admin-input" type="date" name="endDate" value="${endVal}" required />
            </div>

          </div>

          <hr class="admin-divider"/>

          <div class="admin-actions">
            <button type="submit" class="admin-btn admin-btn--primary">
              <c:choose>
                <c:when test="${empty discount}">Tạo mới</c:when>
                <c:otherwise>Lưu thay đổi</c:otherwise>
              </c:choose>
            </button>

            <a class="admin-btn" href="${pageContext.request.contextPath}/admin/order-discounts">Hủy</a>

            <c:if test="${not empty discount}">
              <button type="submit"
                      class="admin-btn admin-btn--danger"
                      formaction="${pageContext.request.contextPath}/admin/order-discounts"
                      formmethod="post"
                      onclick="return confirm('Xóa giảm giá này?');">
                Xóa
              </button>
              <input type="hidden" name="deleteAction" value="delete"/>
              <%-- LƯU Ý: nếu bạn muốn nút Xóa submit action=delete, nên tách thành form riêng (khuyến nghị) --%>
            </c:if>
          </div>

        </form>

        <%-- ✅ Khuyến nghị chuẩn: form XÓA tách riêng (để chắc chắn action=delete) --%>
        <c:if test="${not empty discount}">
          <form method="post"
                action="${pageContext.request.contextPath}/admin/order-discounts"
                class="admin-inline"
                onsubmit="return confirm('Xóa giảm giá này?');"
                style="margin-top:10px;">

            <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
            <%@ include file="/jsp/common/csrf.jspf" %>

            <input type="hidden" name="action" value="delete"/>
            <input type="hidden" name="id" value="${discount.id}"/>
          </form>
        </c:if>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

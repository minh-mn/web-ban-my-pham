<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="pageTitle" value="ADMIN | Form Khuyến mãi" scope="request"/>
<c:set var="activeMenu" value="promotion" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<%-- ✅ Enum-safe: lấy name() ra String để so sánh trong JSP --%>
<c:set var="sc" value="${not empty event && not empty event.scope ? event.scope.name() : ''}"/>
<c:set var="dt" value="${not empty event && not empty event.discountType ? event.discountType.name() : ''}"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">
          <c:choose>
            <c:when test="${mode == 'edit'}">Sửa Promotion Event</c:when>
            <c:otherwise>Tạo Promotion Event</c:otherwise>
          </c:choose>
        </h1>
        <p class="admin-subtext">Chọn scope và cấu hình giảm giá.</p>
      </div>

      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/promotion-events">Quay lại</a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:if test="${not empty error}">
          <div class="admin-alert admin-alert--danger">
            <c:out value="${error}"/>
          </div>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/promotion-events"
              class="admin-form">

          <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="${mode == 'edit' ? 'update' : 'create'}"/>
          <c:if test="${mode == 'edit'}">
            <input type="hidden" name="id" value="${event.id}"/>
          </c:if>

          <div class="admin-grid-2">

            <div class="admin-field" style="grid-column: 1 / -1;">
              <div class="admin-label">Tên chương trình</div>
              <input class="admin-input" type="text" name="name"
                     value="${not empty event ? fn:escapeXml(event.name) : ''}"
                     required maxlength="120">
            </div>

            <div class="admin-field">
              <div class="admin-label">Trạng thái</div>
              <select class="admin-select" name="active">
                <option value="1" ${empty event || event.active ? 'selected' : ''}>ACTIVE</option>
                <option value="0" ${not empty event && !event.active ? 'selected' : ''}>INACTIVE</option>
              </select>
              <div class="admin-help">INACTIVE sẽ không áp dụng.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Scope</div>
              <select class="admin-select" name="scope" id="scopeSelect" required>
                <option value="ALL"      ${sc == 'ALL' ? 'selected' : ''}>ALL</option>
                <option value="CATEGORY" ${sc == 'CATEGORY' ? 'selected' : ''}>CATEGORY</option>
                <option value="BRAND"    ${sc == 'BRAND' ? 'selected' : ''}>BRAND</option>
              </select>
              <div class="admin-help">ALL: áp dụng toàn bộ; CATEGORY/BRAND: theo danh mục/thương hiệu.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Kiểu giảm</div>
              <select class="admin-select" name="discountType" required>
                <option value="PERCENT" ${dt == 'PERCENT' ? 'selected' : ''}>PERCENT</option>
                <option value="AMOUNT"  ${dt == 'AMOUNT'  ? 'selected' : ''}>AMOUNT</option>
              </select>
              <div class="admin-help">PERCENT: giảm theo %; AMOUNT: giảm theo số tiền.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Giá trị giảm</div>
              <input class="admin-input" type="number" name="discountValue"
                     step="0.01" min="0"
                     value="${not empty event ? event.discountValue : ''}"
                     required>
              <div class="admin-help">Ví dụ: 10 (10%) hoặc 50000 (giảm 50.000đ).</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Max giảm (tuỳ chọn)</div>
              <input class="admin-input" type="number" name="maxDiscountAmount"
                     step="0.01" min="0"
                     value="${not empty event ? event.maxDiscountAmount : ''}">
              <div class="admin-help">Dùng khi giảm theo % để giới hạn số tiền giảm tối đa.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Start Date</div>
              <input class="admin-input" type="date" name="startDate"
                     value="${not empty event ? event.startDate : ''}" required>
            </div>

            <div class="admin-field">
              <div class="admin-label">End Date</div>
              <input class="admin-input" type="date" name="endDate"
                     value="${not empty event ? event.endDate : ''}" required>
            </div>

            <!-- CATEGORY -->
            <div class="admin-field" id="categoryRow">
              <div class="admin-label">Category (khi scope=CATEGORY)</div>
              <select class="admin-select" name="categoryId" id="categorySelect">
                <option value="">-- chọn --</option>

                <c:forEach var="parent" items="${categories}">
                  <option value="${parent.id}"
                          ${not empty event && event.categoryId==parent.id ? 'selected' : ''}>
                    <c:out value="${parent.name}"/>
                  </option>

                  <c:if test="${not empty parent.children}">
                    <c:forEach var="child" items="${parent.children}">
                      <option value="${child.id}"
                              ${not empty event && event.categoryId==child.id ? 'selected' : ''}>
                        └ <c:out value="${child.name}"/>
                      </option>
                    </c:forEach>
                  </c:if>
                </c:forEach>

              </select>
              <div class="admin-help">Chỉ chọn khi scope=CATEGORY.</div>
            </div>

            <!-- BRAND -->
            <div class="admin-field" id="brandRow">
              <div class="admin-label">Brand (khi scope=BRAND)</div>
              <select class="admin-select" name="brandId" id="brandSelect">
                <option value="">-- chọn --</option>
                <c:forEach var="b" items="${brands}">
                  <option value="${b.id}" ${not empty event && event.brandId==b.id ? 'selected' : ''}>
                    <c:out value="${b.name}"/>
                  </option>
                </c:forEach>
              </select>
              <div class="admin-help">Chỉ chọn khi scope=BRAND.</div>
            </div>

          </div>

          <hr class="admin-divider"/>

          <div class="admin-actions">
            <button class="admin-btn admin-btn--primary" type="submit">Lưu</button>
            <a class="admin-btn" href="${pageContext.request.contextPath}/admin/promotion-events">Hủy</a>
          </div>

        </form>

      </div>
    </div>

  </div>
</main>

<script>
(function () {
  const scopeSelect = document.getElementById("scopeSelect");
  const categoryRow = document.getElementById("categoryRow");
  const brandRow = document.getElementById("brandRow");
  const categorySelect = document.getElementById("categorySelect");
  const brandSelect = document.getElementById("brandSelect");

  function applyScopeUI() {
    const scope = scopeSelect ? scopeSelect.value : "ALL";

    if (scope === "CATEGORY") {
      if (categoryRow) categoryRow.style.display = "";
      if (brandRow) brandRow.style.display = "none";
      if (brandSelect) brandSelect.value = "";
    } else if (scope === "BRAND") {
      if (brandRow) brandRow.style.display = "";
      if (categoryRow) categoryRow.style.display = "none";
      if (categorySelect) categorySelect.value = "";
    } else {
      if (categoryRow) categoryRow.style.display = "none";
      if (brandRow) brandRow.style.display = "none";
      if (categorySelect) categorySelect.value = "";
      if (brandSelect) brandSelect.value = "";
    }
  }

  if (scopeSelect) {
    scopeSelect.addEventListener("change", applyScopeUI);
    applyScopeUI();
  }
})();
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

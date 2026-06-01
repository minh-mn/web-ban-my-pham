<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%-- 1. Cấu hình layout admin --%>
<c:set var="pageTitle" value="ADMIN | Chi tiết Flash Sale" scope="request"/>
<c:set var="activeMenu" value="flashsale" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/choices.js/public/assets/styles/choices.min.css"/>
<script src="https://cdn.jsdelivr.net/npm/choices.js/public/assets/scripts/choices.min.js"></script>

<main class="admin-main">
  <div class="admin-container flashsale-items-page">

    <div class="admin-topbar flashsale-items-topbar">
      <div>
        <h1 class="admin-h1">Chi tiết Flash Sale #${flashSaleId}</h1>
        <p class="admin-help">
          Quản lý sản phẩm trong khung Flash Sale, giá bán, số lượng phân bổ và giới hạn mua theo từng khách hàng.
        </p>
      </div>

      <a href="${pageContext.request.contextPath}/admin/flash-sale"
         class="admin-btn admin-btn--secondary">
        Quay lại
      </a>
    </div>

    <c:if test="${param.added == '1'}">
      <div class="admin-alert admin-alert--success">
        Đã thêm sản phẩm vào Flash Sale.
      </div>
    </c:if>

    <c:if test="${param.deleted == '1'}">
      <div class="admin-alert admin-alert--success">
        Đã xóa sản phẩm khỏi Flash Sale.
      </div>
    </c:if>

    <c:if test="${not empty param.error}">
      <div class="admin-alert admin-alert--danger">
        Không thể xử lý sản phẩm Flash Sale. Vui lòng kiểm tra lại sản phẩm, giá, số lượng và giới hạn mỗi khách.
      </div>
    </c:if>

    <%-- Form thêm sản phẩm --%>
    <div class="admin-card flashsale-item-form-card">
      <div class="admin-card__body">
        <div class="admin-form-section">
          <h2 class="admin-form-section__title">Thêm sản phẩm Flash Sale</h2>

          <form action="${pageContext.request.contextPath}/admin/flash-sale/items"
                method="POST"
                class="admin-form flashsale-item-form">
            <%@ include file="/jsp/common/csrf.jspf" %>

            <input type="hidden" name="action" value="add">
            <input type="hidden" name="flashSaleId" value="${flashSaleId}">

            <div class="admin-form-grid admin-form-grid--4 flashsale-item-form-grid">
              <div class="admin-field flashsale-product-field">
                <label class="admin-label" for="productSearchSelect">
                  Chọn sản phẩm <span class="admin-required">*</span>
                </label>
                <select name="productId" id="productSearchSelect" class="admin-select" required>
                  <option value="">Gõ tên sản phẩm để tìm kiếm...</option>
                </select>
                <small class="admin-help">Gõ ít nhất 3 ký tự để tìm sản phẩm.</small>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="flashPrice">
                  Giá Flash Sale <span class="admin-required">*</span>
                </label>
                <input type="number"
                       id="flashPrice"
                       name="flashPrice"
                       class="admin-input"
                       min="1"
                       step="1000"
                       required
                       placeholder="Ví dụ: 199000">
              </div>

              <div class="admin-field">
                <label class="admin-label" for="quantity">
                  Số lượng Flash Sale <span class="admin-required">*</span>
                </label>
                <input type="number"
                       id="quantity"
                       name="quantity"
                       class="admin-input"
                       min="1"
                       required
                       placeholder="Số lượng bán">
              </div>

              <div class="admin-field flashsale-limit-field">
                <label class="admin-label" for="maxQuantityPerUser">
                  Giới hạn / khách <span class="admin-required">*</span>
                </label>
                <input type="number"
                       id="maxQuantityPerUser"
                       name="maxQuantityPerUser"
                       class="admin-input"
                       min="1"
                       value="${empty defaultMaxQuantityPerUser ? 2 : defaultMaxQuantityPerUser}"
                       required
                       placeholder="Ví dụ: 2">
                <small class="admin-help">
                  Mỗi khách chỉ được mua tối đa X sản phẩm này trong khung giờ Flash Sale.
                </small>
              </div>
            </div>

            <div class="flashsale-limit-note">
              <div class="flashsale-limit-note__icon">!</div>
              <div>
                <strong>Issue 139 - Chặn mua quá số lượng Flash Sale</strong>
                <span>
                  Giá trị “Giới hạn / khách” sẽ được lưu vào
                  <code>flash_sale_items.max_quantity_per_user</code> và được kiểm tra ở giỏ hàng, checkout.
                </span>
              </div>
            </div>

            <div class="admin-actions">
              <button type="submit" class="admin-btn admin-btn--primary">
                Thêm sản phẩm
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <%-- Bảng danh sách sản phẩm Flash Sale --%>
    <div class="admin-card flashsale-items-card">
      <div class="admin-card__body">
        <div class="flashsale-items-head">
          <div>
            <h2 class="admin-form-section__title">Danh sách sản phẩm trong Flash Sale</h2>
            <p class="admin-help">
              Theo dõi số lượng phân bổ, đã bán và giới hạn mua của từng khách.
            </p>
          </div>
        </div>

        <c:choose>
          <c:when test="${empty items}">
            <div class="admin-empty">
              Chưa có sản phẩm nào trong Flash Sale này.
            </div>
          </c:when>

          <c:otherwise>
            <div class="flashsale-items-table-wrap">
              <table class="flashsale-items-table">
                <thead>
                <tr>
                  <th>Sản phẩm</th>
                  <th>Giá gốc</th>
                  <th>Giá Flash</th>
                  <th>Số lượng</th>
                  <th>Đã bán</th>
                  <th>Còn lại</th>
                  <th>Giới hạn / khách</th>
                  <th>Tiến độ</th>
                  <th>Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="item" items="${items}">
                  <tr>
                    <td class="flashsale-product-cell">
                      <strong>${item.product.title}</strong>
                      <span>ID: ${item.product.id}</span>
                    </td>

                    <td>
                      <fmt:formatNumber value="${item.product.price}" /> đ
                    </td>

                    <td class="flashsale-price-cell">
                      <fmt:formatNumber value="${item.flashPrice}" /> đ
                    </td>

                    <td>${item.quantity}</td>

                    <td>${item.soldQuantity}</td>

                    <td>
                      <c:choose>
                        <c:when test="${item.remainQuantity <= 0}">
                          <span class="admin-pill admin-pill--danger">Hết</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill admin-pill--ok">${item.remainQuantity}</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <span class="flashsale-limit-pill">
                        ${item.maxQuantityPerUser} sản phẩm / khách
                      </span>
                    </td>

                    <td class="flashsale-progress-cell">
                      <div class="flashsale-admin-progress"
                           role="progressbar"
                           aria-valuemin="0"
                           aria-valuemax="100"
                           aria-valuenow="${item.soldPercent}">
                        <span style="width: ${item.soldPercent}%;"></span>
                      </div>
                      <small>Đã bán ${item.soldPercent}%</small>
                    </td>

                    <td>
                      <form action="${pageContext.request.contextPath}/admin/flash-sale/items"
                            method="POST"
                            class="flashsale-delete-form"
                            onsubmit="return confirm('Xóa sản phẩm này khỏi Flash Sale?');">
                        <%@ include file="/jsp/common/csrf.jspf" %>
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="itemId" value="${item.id}">
                        <input type="hidden" name="flashSaleId" value="${flashSaleId}">
                        <button type="submit" class="admin-btn admin-btn--danger">
                          Xóa
                        </button>
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

<script>
  document.addEventListener('DOMContentLoaded', function() {
    const element = document.getElementById('productSearchSelect');

    if (!element || typeof Choices === 'undefined') {
      return;
    }

    const choices = new Choices(element, {
      searchEnabled: true,
      itemSelectText: 'Chọn',
      noResultsText: 'Không tìm thấy sản phẩm',
      placeholderValue: 'Gõ từ khóa...',
      shouldSort: false,
    });

    element.addEventListener('search', function(event) {
      const query = event.detail.value || '';

      if (query.length <= 2) {
        return;
      }

      fetch('${pageContext.request.contextPath}/ajax-search?q=' + encodeURIComponent(query))
        .then(response => response.json())
        .then(data => {
          if (data.results && data.results.length > 0) {
            const items = data.results.map(p => ({
              value: p.id,
              label: p.title + ' (Giá: ' + p.price + 'đ)'
            }));

            choices.setChoices(items, 'value', 'label', true);
          }
        })
        .catch(error => console.error('Error:', error));
    });
  });
</script>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Chi tiết Flash Sale" scope="request"/>
<c:set var="activeMenu" value="flashsale" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/choices.js/public/assets/styles/choices.min.css"/>
<script src="https://cdn.jsdelivr.net/npm/choices.js/public/assets/scripts/choices.min.js"></script>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="flashSaleItemTotal" value="${empty items ? 0 : fn:length(items)}" />
<c:set var="flashSaleQuantityTotal" value="0" />
<c:set var="flashSaleSoldTotal" value="0" />
<c:set var="flashSaleSoldOutTotal" value="0" />

<c:forEach var="itemStat" items="${items}">
  <c:set var="flashSaleQuantityTotal" value="${flashSaleQuantityTotal + (empty itemStat.quantity ? 0 : itemStat.quantity)}" />
  <c:set var="flashSaleSoldTotal" value="${flashSaleSoldTotal + (empty itemStat.soldQuantity ? 0 : itemStat.soldQuantity)}" />
  <c:if test="${itemStat.remainQuantity <= 0}">
    <c:set var="flashSaleSoldOutTotal" value="${flashSaleSoldOutTotal + 1}" />
  </c:if>
</c:forEach>

<main class="admin-main">
  <div class="admin-container admin-flashsale-items-page">

    <section class="admin-flashsale-items-hero">
      <div class="admin-flashsale-items-hero__content">
        <span class="admin-flashsale-items-eyebrow">FLASH SALE PRODUCTS</span>
        <h1 class="admin-flashsale-items-title">Chi tiết Flash Sale #${flashSaleId}</h1>
        <p class="admin-flashsale-items-subtitle">
          Quản lý sản phẩm trong khung Flash Sale, giá bán, số lượng phân bổ và giới hạn mua theo từng khách hàng.
          Mỗi sản phẩm có thể đặt giới hạn riêng để tránh khách mua vượt số lượng ưu đãi.
        </p>
      </div>

      <div class="admin-flashsale-items-hero__actions">
        <a href="${ctx}/admin/flash-sale"
           class="admin-btn">
          ← Quay lại danh sách
        </a>
      </div>
    </section>

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

    <section class="admin-flashsale-items-summary">
      <div class="admin-flashsale-items-stat admin-flashsale-items-stat--total">
        <span class="admin-flashsale-items-stat__icon">🧴</span>
        <span class="admin-flashsale-items-stat__label">Sản phẩm</span>
        <strong class="admin-flashsale-items-stat__value">
          <c:out value="${flashSaleItemTotal}" />
        </strong>
        <span class="admin-flashsale-items-stat__note">Đang nằm trong Flash Sale</span>
      </div>

      <div class="admin-flashsale-items-stat admin-flashsale-items-stat--qty">
        <span class="admin-flashsale-items-stat__icon">📦</span>
        <span class="admin-flashsale-items-stat__label">Số lượng phân bổ</span>
        <strong class="admin-flashsale-items-stat__value">
          <c:out value="${flashSaleQuantityTotal}" />
        </strong>
        <span class="admin-flashsale-items-stat__note">Tổng số lượng bán ưu đãi</span>
      </div>

      <div class="admin-flashsale-items-stat admin-flashsale-items-stat--sold">
        <span class="admin-flashsale-items-stat__icon">🔥</span>
        <span class="admin-flashsale-items-stat__label">Đã bán</span>
        <strong class="admin-flashsale-items-stat__value">
          <c:out value="${flashSaleSoldTotal}" />
        </strong>
        <span class="admin-flashsale-items-stat__note">Tổng số lượng đã mua</span>
      </div>

      <div class="admin-flashsale-items-stat admin-flashsale-items-stat--soldout">
        <span class="admin-flashsale-items-stat__icon">⛔</span>
        <span class="admin-flashsale-items-stat__label">Hết hàng Flash</span>
        <strong class="admin-flashsale-items-stat__value">
          <c:out value="${flashSaleSoldOutTotal}" />
        </strong>
        <span class="admin-flashsale-items-stat__note">Sản phẩm đã hết số lượng ưu đãi</span>
      </div>
    </section>

    <section class="admin-card admin-flashsale-item-form-card">
      <div class="admin-card__body">
        <div class="admin-flashsale-items-section-head">
          <div>
            <h2 class="admin-flashsale-items-section-title">Thêm sản phẩm Flash Sale</h2>
            <p class="admin-flashsale-items-section-desc">
              Tìm sản phẩm, nhập giá Flash Sale, số lượng phân bổ và giới hạn mua cho mỗi khách.
            </p>
          </div>
          <span class="admin-chip admin-chip--brand">Issue 139</span>
        </div>

        <form action="${ctx}/admin/flash-sale/items"
              method="POST"
              class="admin-form admin-flashsale-item-form">
          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="add">
          <input type="hidden" name="flashSaleId" value="${flashSaleId}">

          <div class="admin-flashsale-item-form-grid">
            <div class="admin-field admin-flashsale-product-field">
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

            <div class="admin-field admin-flashsale-limit-field">
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

          <div class="admin-flashsale-limit-note">
            <span class="admin-flashsale-limit-note__icon">⚠️</span>
            <div>
              <strong>Chặn mua quá số lượng Flash Sale</strong>
              <span>
                Giá trị “Giới hạn / khách” sẽ được lưu vào
                <code>flash_sale_items.max_quantity_per_user</code> và được kiểm tra ở giỏ hàng, checkout.
              </span>
            </div>
          </div>

          <div class="admin-flashsale-items-form-actions">
            <button type="submit" class="admin-btn admin-btn--primary">
              Thêm sản phẩm
            </button>
          </div>
        </form>
      </div>
    </section>

    <section class="admin-card admin-flashsale-items-card">
      <div class="admin-card__body">
        <div class="admin-flashsale-items-section-head admin-flashsale-items-section-head--list">
          <div>
            <h2 class="admin-flashsale-items-section-title">Danh sách sản phẩm trong Flash Sale</h2>
            <p class="admin-flashsale-items-section-desc">
              Theo dõi số lượng phân bổ, đã bán, còn lại và giới hạn mua của từng khách.
            </p>
          </div>
          <span class="admin-chip admin-chip--brand">
            <c:out value="${flashSaleItemTotal}" /> sản phẩm
          </span>
        </div>

        <c:choose>
          <c:when test="${empty items}">
            <div class="admin-flashsale-items-empty">
              <div class="admin-flashsale-items-empty__icon">🧴</div>
              <div>
                <h3>Chưa có sản phẩm nào trong Flash Sale này</h3>
                <p>Hãy thêm sản phẩm đầu tiên để bắt đầu cấu hình giá Flash Sale và giới hạn mua.</p>
              </div>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-flashsale-items-table-wrap">
              <table class="admin-table admin-flashsale-items-table">
                <thead>
                <tr>
                  <th class="admin-flashsale-items-col-product">Sản phẩm</th>
                  <th class="admin-flashsale-items-col-price">Giá gốc</th>
                  <th class="admin-flashsale-items-col-price">Giá Flash</th>
                  <th class="admin-flashsale-items-col-qty">Số lượng</th>
                  <th class="admin-flashsale-items-col-qty">Đã bán</th>
                  <th class="admin-flashsale-items-col-remain">Còn lại</th>
                  <th class="admin-flashsale-items-col-limit">Giới hạn / khách</th>
                  <th class="admin-flashsale-items-col-progress">Tiến độ</th>
                  <th class="admin-flashsale-items-col-actions">Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="item" items="${items}">
                  <tr class="${item.remainQuantity <= 0 ? 'admin-flashsale-items-row--soldout' : ''}">
                    <td class="admin-flashsale-product-cell">
                      <strong><c:out value="${item.product.title}" /></strong>
                      <span>ID: <c:out value="${item.product.id}" /></span>
                    </td>

                    <td>
                      <fmt:formatNumber value="${item.product.price}" /> đ
                    </td>

                    <td class="admin-flashsale-price-cell">
                      <fmt:formatNumber value="${item.flashPrice}" /> đ
                    </td>

                    <td><c:out value="${item.quantity}" /></td>

                    <td><c:out value="${item.soldQuantity}" /></td>

                    <td>
                      <c:choose>
                        <c:when test="${item.remainQuantity <= 0}">
                          <span class="admin-pill admin-pill--danger">Hết</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill admin-pill--ok">
                            <c:out value="${item.remainQuantity}" />
                          </span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <span class="admin-flashsale-limit-pill">
                        <c:out value="${item.maxQuantityPerUser}" /> sản phẩm / khách
                      </span>
                    </td>

                    <td class="admin-flashsale-progress-cell">
                      <progress class="admin-flashsale-admin-progress"
                                max="100"
                                value="${item.soldPercent}">
                        ${item.soldPercent}%
                      </progress>
                      <small>Đã bán <c:out value="${item.soldPercent}" />%</small>
                    </td>

                    <td class="admin-flashsale-items-action-cell">
                      <form action="${ctx}/admin/flash-sale/items"
                            method="POST"
                            class="admin-flashsale-delete-form"
                            onsubmit="return confirm('Xóa sản phẩm này khỏi Flash Sale?');">
                        <%@ include file="/jsp/common/csrf.jspf" %>
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="itemId" value="${item.id}">
                        <input type="hidden" name="flashSaleId" value="${flashSaleId}">
                        <button type="submit" class="admin-btn admin-btn--danger admin-flashsale-items-action-btn">
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
    </section>
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
      shouldSort: false
    });

    element.addEventListener('search', function(event) {
      const query = event.detail.value || '';

      if (query.length <= 2) {
        return;
      }

      fetch('${ctx}/ajax-search?q=' + encodeURIComponent(query))
        .then(response => response.json())
        .then(data => {
          if (data.results && data.results.length > 0) {
            const products = data.results.map(p => ({
              value: p.id,
              label: p.title + ' (Giá: ' + p.price + 'đ)'
            }));

            choices.setChoices(products, 'value', 'label', true);
          }
        })
        .catch(error => console.error('Error:', error));
    });
  });
</script>

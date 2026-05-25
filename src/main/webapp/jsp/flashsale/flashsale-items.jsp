<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%-- 1. Cấu hình layout admin --%>
<c:set var="pageTitle" value="ADMIN | Chi tiết Flash Sale" scope="request"/>
<c:set var="activeMenu" value="flashsale" scope="request"/>
<%-- Dùng admin-form.css để có style cho form và admin-list.css cho bảng --%>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/choices.js/public/assets/styles/choices.min.css"/>
<script src="https://cdn.jsdelivr.net/npm/choices.js/public/assets/scripts/choices.min.js"></script>

<main class="admin-main">
  <div class="admin-container">
    <h1 class="admin-h1">Chi tiết Flash Sale #${flashSaleId}</h1>

    <%-- Form thêm sản phẩm dùng class admin-card và admin-form --%>
    <div class="admin-card" style="margin-bottom: 20px;">
      <div class="admin-card__body">
        <form action="${pageContext.request.contextPath}/admin/flash-sale/items" method="POST" class="admin-form">
          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="add">
          <input type="hidden" name="flashSaleId" value="${flashSaleId}">

          <div class="admin-grid-3" style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px;">
            <div class="admin-field">
              <label class="admin-label">Chọn sản phẩm</label>
              <select name="productId" id="productSearchSelect" class="admin-input" required>
                <option value="">Gõ tên sản phẩm để tìm kiếm...</option>
              </select>
            </div>

            <div class="admin-field">
              <label class="admin-label">Giá Flash Sale</label>
              <input type="number" name="flashPrice" class="admin-input" required placeholder="Nhập giá...">
            </div>

            <div class="admin-field">
              <label class="admin-label">Số lượng</label>
              <input type="number" name="quantity" class="admin-input" required placeholder="Số lượng bán">
            </div>
          </div>

          <button type="submit" class="admin-btn admin-btn--primary">Thêm sản phẩm</button>
        </form>
      </div>
    </div>

    <%-- Bảng danh sách dùng class admin-table --%>
    <div class="admin-card">
      <div class="admin-card__body">
        <table class="admin-table">
          <thead>
          <tr>
            <th>Sản phẩm</th>
            <th>Giá gốc</th>
            <th>Giá Flash</th>
            <th>Số lượng</th>
            <th>Đã bán</th>
            <th>Thao tác</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="item" items="${items}">
            <tr>
              <td>${item.product.title}</td>
              <td><fmt:formatNumber value="${item.product.price}" /> đ</td>
              <td><fmt:formatNumber value="${item.flashPrice}" /> đ</td>
              <td>${item.quantity}</td>
              <td>${item.soldQuantity}</td>
              <td>
                <form action="${pageContext.request.contextPath}/admin/flash-sale/items" method="POST" style="margin: 0;">
                  <input type="hidden" name="action" value="delete">
                  <input type="hidden" name="itemId" value="${item.id}">
                  <input type="hidden" name="flashSaleId" value="${flashSaleId}">
                  <button type="submit" class="admin-btn admin-btn--danger">Xóa</button>
                </form>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</main>

<script>
  document.addEventListener('DOMContentLoaded', function() {
    const element = document.getElementById('productSearchSelect');
    const choices = new Choices(element, {
      searchEnabled: true,
      itemSelectText: 'Chọn',
      noResultsText: 'Không tìm thấy sản phẩm',
      placeholderValue: 'Gõ từ khóa...',
      shouldSort: false,
    });

    // Lắng nghe sự kiện người dùng gõ vào ô tìm kiếm
    element.addEventListener('search', function(event) {
      const query = event.detail.value;

      // Chỉ gọi API nếu gõ trên 2 ký tự
      if (query.length > 2) {
        fetch('${pageContext.request.contextPath}/ajax-search?q=' + encodeURIComponent(query))
                .then(response => response.json())
                .then(data => {
                  // data.results là mảng sản phẩm từ AjaxSearchServlet.java
                  if (data.results && data.results.length > 0) {
                    const items = data.results.map(p => ({
                      value: p.id,
                      label: p.title + ' (Giá: ' + p.price + 'đ)'
                    }));

                    // Cập nhật danh sách hiển thị
                    choices.setChoices(items, 'value', 'label', true);
                  }
                })
                .catch(error => console.error('Error:', error));
      }
    });
  });
</script>
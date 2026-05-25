<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | ${empty flashSale.id ? 'Thêm' : 'Sửa'} Flash Sale" scope="request"/>
<c:set var="activeMenu" value="flashsale" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">
    <h1 class="admin-h1">${empty flashSale.id ? 'Thêm' : 'Sửa'} Flash Sale</h1>

    <div class="admin-card">
      <div class="admin-card__body">
        <form method="post" action="${pageContext.request.contextPath}/admin/flash-sale/save" class="admin-form">
          <%@ include file="/jsp/common/csrf.jspf" %>
          <input type="hidden" name="id" value="${flashSale.id}">

          <div class="admin-field">
            <label class="admin-label">Tên Flash Sale</label>
            <input class="admin-input" name="title" value="${flashSale.title}" required>
          </div>

          <div class="admin-grid-2">
            <div class="admin-field">
              <label class="admin-label">Start Time</label>
              <input type="datetime-local" class="admin-input" name="startTime" value="${flashSale.startTime}" required>
            </div>
            <div class="admin-field">
              <label class="admin-label">End Time</label>
              <input type="datetime-local" class="admin-input" name="endTime" value="${flashSale.endTime}" required>
            </div>
          </div>

          <div class="admin-field">
            <label class="admin-label">Trạng thái</label>
            <select class="admin-input" name="active">
              <option value="true" ${flashSale.active ? 'selected' : ''}>Active</option>
              <option value="false" ${!flashSale.active ? 'selected' : ''}>Inactive</option>
            </select>
          </div>

          <div class="admin-actions">
            <a href="${pageContext.request.contextPath}/admin/flash-sale" class="admin-btn">Hủy</a>
            <button type="submit" class="admin-btn admin-btn--primary">Lưu Flash Sale</button>
          </div>
        </form>

        <c:if test="${not empty flashSale.id}">
          <hr>
          <h3>Sản phẩm trong Flash Sale</h3>
          <a href="${pageContext.request.contextPath}/admin/flash-sale/items?flashSaleId=${flashSale.id}"
             class="admin-btn admin-btn--primary">Quản lý sản phẩm chi tiết</a>

          <table class="admin-table" style="margin-top: 15px;">
            <thead>
            <tr>
              <th>Sản phẩm</th>
              <th>Giá Flash</th>
              <th>Số lượng</th>
              <th>Đã bán</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="item" items="${items}">
              <tr>
                <td>${item.product.title}</td>
                <td>${item.flashPrice}</td>
                <td>${item.quantity}</td>
                <td>${item.soldQuantity}</td>
              </tr>
            </c:forEach>
            </tbody>
          </table>
        </c:if>
      </div>
    </div>
  </div>
</main>
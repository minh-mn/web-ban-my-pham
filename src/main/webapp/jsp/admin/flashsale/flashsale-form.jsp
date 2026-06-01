<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | ${empty flashSale.id ? 'Thêm' : 'Sửa'} Flash Sale" scope="request"/>
<c:set var="activeMenu" value="flashsale" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container flashsale-form-page">

    <div class="admin-topbar flashsale-form-hero">
      <div class="flashsale-form-hero__content">
        <span class="flashsale-form-eyebrow">Flash Sale</span>
        <h1 class="admin-h1 flashsale-form-title">
          ${empty flashSale.id ? 'Thêm Flash Sale' : 'Sửa Flash Sale'}
        </h1>
        <p class="admin-help flashsale-form-subtitle">
          Cấu hình tên chương trình, thời gian diễn ra và trạng thái hoạt động.
          Giới hạn mua mỗi khách sẽ được cấu hình theo từng sản phẩm trong phần quản lý sản phẩm chi tiết.
        </p>
      </div>

      <div class="flashsale-form-hero__actions">
        <a href="${pageContext.request.contextPath}/admin/flash-sale"
           class="admin-btn admin-btn--secondary">
          Quay lại danh sách
        </a>

        <c:if test="${not empty flashSale.id}">
          <a href="${pageContext.request.contextPath}/admin/flash-sale/items?flashSaleId=${flashSale.id}"
             class="admin-btn admin-btn--primary">
            Quản lý sản phẩm
          </a>
        </c:if>
      </div>
    </div>

    <c:if test="${param.saved == '1'}">
      <div class="admin-alert admin-alert--success">
        Đã lưu thông tin Flash Sale.
      </div>
    </c:if>

    <c:if test="${not empty param.error}">
      <div class="admin-alert admin-alert--danger">
        Không thể lưu Flash Sale. Vui lòng kiểm tra tên chương trình, thời gian bắt đầu và thời gian kết thúc.
      </div>
    </c:if>

    <div class="flashsale-form-layout">
      <section class="admin-card flashsale-form-card">
        <div class="admin-card__body">
          <div class="admin-form-section">
            <h2 class="admin-form-section__title">Thông tin chương trình</h2>

            <form method="post"
                  action="${pageContext.request.contextPath}/admin/flash-sale/save"
                  class="admin-form flashsale-form">
              <%@ include file="/jsp/common/csrf.jspf" %>

              <input type="hidden" name="id" value="${flashSale.id}">

              <div class="admin-field">
                <label class="admin-label" for="flashSaleTitle">
                  Tên Flash Sale <span class="admin-required">*</span>
                </label>
                <input id="flashSaleTitle"
                       class="admin-input"
                       name="title"
                       value="${flashSale.title}"
                       maxlength="150"
                       required
                       placeholder="Ví dụ: Flash Sale cuối tuần">
              </div>

              <div class="admin-grid-2">
                <div class="admin-field">
                  <label class="admin-label" for="startTime">
                    Thời gian bắt đầu <span class="admin-required">*</span>
                  </label>
                  <input id="startTime"
                         type="datetime-local"
                         class="admin-input"
                         name="startTime"
                         value="${flashSale.startTime}"
                         required>
                </div>

                <div class="admin-field">
                  <label class="admin-label" for="endTime">
                    Thời gian kết thúc <span class="admin-required">*</span>
                  </label>
                  <input id="endTime"
                         type="datetime-local"
                         class="admin-input"
                         name="endTime"
                         value="${flashSale.endTime}"
                         required>
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="active">
                  Trạng thái
                </label>
                <select id="active" class="admin-select" name="active">
                  <option value="true" ${flashSale.active ? 'selected' : ''}>Đang hoạt động</option>
                  <option value="false" ${!flashSale.active ? 'selected' : ''}>Tạm tắt</option>
                </select>
                <small class="admin-help">
                  Chỉ Flash Sale đang hoạt động và nằm trong khung thời gian hợp lệ mới được áp dụng ở giỏ hàng/checkout.
                </small>
              </div>

              <div class="flashsale-form-note">
                <div class="flashsale-form-note__icon">139</div>
                <div>
                  <strong>Giới hạn mua Flash Sale theo từng sản phẩm</strong>
                  <span>
                    Sau khi lưu Flash Sale, vào “Quản lý sản phẩm” để nhập
                    <b>Giới hạn / khách</b> cho từng sản phẩm. Giá trị này dùng để chặn mua vượt số lượng ở giỏ hàng và checkout.
                  </span>
                </div>
              </div>

              <div class="admin-actions">
                <a href="${pageContext.request.contextPath}/admin/flash-sale"
                   class="admin-btn admin-btn--secondary">
                  Hủy
                </a>
                <button type="submit" class="admin-btn admin-btn--primary">
                  Lưu Flash Sale
                </button>
              </div>
            </form>
          </div>
        </div>
      </section>

      <aside class="admin-card flashsale-form-side">
        <div class="admin-card__body">
          <h2 class="admin-form-section__title">Luồng cấu hình</h2>

          <div class="flashsale-config-steps">
            <div class="flashsale-config-step is-done">
              <span>1</span>
              <div>
                <strong>Tạo khung Flash Sale</strong>
                <small>Nhập tên, thời gian bắt đầu/kết thúc và bật trạng thái.</small>
              </div>
            </div>

            <div class="flashsale-config-step ${empty flashSale.id ? '' : 'is-done'}">
              <span>2</span>
              <div>
                <strong>Thêm sản phẩm</strong>
                <small>Chọn sản phẩm, giá Flash Sale, số lượng phân bổ.</small>
              </div>
            </div>

            <div class="flashsale-config-step ${empty flashSale.id ? '' : 'is-done'}">
              <span>3</span>
              <div>
                <strong>Cấu hình giới hạn / khách</strong>
                <small>Mỗi khách chỉ được mua tối đa X sản phẩm trong khung giờ.</small>
              </div>
            </div>
          </div>

          <c:choose>
            <c:when test="${not empty flashSale.id}">
              <a href="${pageContext.request.contextPath}/admin/flash-sale/items?flashSaleId=${flashSale.id}"
                 class="admin-btn admin-btn--primary flashsale-side-btn">
                Quản lý sản phẩm Flash Sale
              </a>
            </c:when>
            <c:otherwise>
              <div class="admin-empty">
                Hãy lưu Flash Sale trước, sau đó mới thêm sản phẩm và cấu hình giới hạn mua mỗi khách.
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </aside>
    </div>

    <c:if test="${not empty flashSale.id}">
      <section class="admin-card flashsale-form-items-card">
        <div class="admin-card__body">
          <div class="flashsale-form-items-head">
            <div>
              <h2 class="admin-form-section__title">Sản phẩm trong Flash Sale</h2>
              <p class="admin-help">
                Danh sách tóm tắt. Để thêm/xóa sản phẩm hoặc chỉnh giới hạn mỗi khách, bấm “Quản lý sản phẩm”.
              </p>
            </div>

            <a href="${pageContext.request.contextPath}/admin/flash-sale/items?flashSaleId=${flashSale.id}"
               class="admin-btn admin-btn--primary">
              Quản lý sản phẩm chi tiết
            </a>
          </div>

          <c:choose>
            <c:when test="${empty items}">
              <div class="admin-empty">
                Flash Sale này chưa có sản phẩm nào.
              </div>
            </c:when>

            <c:otherwise>
              <div class="flashsale-form-table-wrap">
                <table class="flashsale-form-table">
                  <thead>
                  <tr>
                    <th>Sản phẩm</th>
                    <th>Giá Flash</th>
                    <th>Số lượng</th>
                    <th>Đã bán</th>
                    <th>Còn lại</th>
                    <th>Giới hạn / khách</th>
                    <th>Tiến độ</th>
                  </tr>
                  </thead>

                  <tbody>
                  <c:forEach var="item" items="${items}">
                    <tr>
                      <td class="flashsale-form-product-cell">
                        <strong>${item.product.title}</strong>
                        <span>ID: ${item.product.id}</span>
                      </td>

                      <td class="flashsale-form-price-cell">
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

                      <td class="flashsale-form-progress-cell">
                        <div class="flashsale-admin-progress"
                             role="progressbar"
                             aria-valuemin="0"
                             aria-valuemax="100"
                             aria-valuenow="${item.soldPercent}">
                          <span style="width: ${item.soldPercent}%;"></span>
                        </div>
                        <small>Đã bán ${item.soldPercent}%</small>
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
    </c:if>
  </div>
</main>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | ${empty flashSale.id ? 'Thêm' : 'Sửa'} Flash Sale" scope="request"/>
<c:set var="activeMenu" value="flashsale" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEditFlashSale" value="${not empty flashSale.id}" />

<main class="admin-main">
  <div class="admin-container admin-flashsale-form-page">

    <section class="admin-flashsale-form-hero">
      <div class="admin-flashsale-form-hero__content">
        <span class="admin-flashsale-form-eyebrow">KHUYẾN MÃI &amp; TĂNG TRƯỞNG</span>
        <h1 class="admin-flashsale-form-title">
          <c:choose>
            <c:when test="${isEditFlashSale}">Sửa Flash Sale</c:when>
            <c:otherwise>Thêm Flash Sale</c:otherwise>
          </c:choose>
        </h1>
        <p class="admin-flashsale-form-subtitle">
          Cấu hình tên chương trình, thời gian diễn ra và trạng thái hoạt động.
          Sau khi lưu chương trình, admin có thể thêm sản phẩm, nhập giá Flash Sale,
          số lượng phân bổ và giới hạn mua theo từng khách hàng.
        </p>
      </div>

      <div class="admin-flashsale-form-hero__actions">
        <a href="${ctx}/admin/flash-sale"
           class="admin-btn">
          ← Quay lại danh sách
        </a>

        <c:if test="${isEditFlashSale}">
          <a href="${ctx}/admin/flash-sale/items?flashSaleId=${flashSale.id}"
             class="admin-btn admin-btn--primary">
            Quản lý sản phẩm
          </a>
        </c:if>
      </div>
    </section>

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

    <form method="post"
          action="${ctx}/admin/flash-sale/save"
          class="admin-form admin-flashsale-form">
      <%@ include file="/jsp/common/csrf.jspf" %>

      <input type="hidden" name="id" value="${flashSale.id}">

      <div class="admin-flashsale-form-layout">

        <section class="admin-card admin-flashsale-form-card">
          <div class="admin-card__body">
            <div class="admin-flashsale-form-section-head">
              <div>
                <h2 class="admin-flashsale-form-section-title">Thông tin chương trình</h2>
                <p class="admin-flashsale-form-section-desc">
                  Nhập thông tin cơ bản để tạo khung Flash Sale.
                </p>
              </div>

              <c:choose>
                <c:when test="${isEditFlashSale && flashSale.active}">
                  <span class="admin-chip admin-chip--success">Đang bật</span>
                </c:when>
                <c:when test="${isEditFlashSale && !flashSale.active}">
                  <span class="admin-chip admin-chip--warning">Tạm tắt</span>
                </c:when>
                <c:otherwise>
                  <span class="admin-chip admin-chip--brand">Flash Sale mới</span>
                </c:otherwise>
              </c:choose>
            </div>

            <div class="admin-flashsale-form-grid">
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
                <small class="admin-help">
                  Tên chương trình nên ngắn gọn, dễ nhận biết trong danh sách quản trị.
                </small>
              </div>

              <div class="admin-flashsale-time-grid">
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
                  <option value="true" ${empty flashSale.id || flashSale.active ? 'selected' : ''}>Đang hoạt động</option>
                  <option value="false" ${not empty flashSale.id && !flashSale.active ? 'selected' : ''}>Tạm tắt</option>
                </select>
                <small class="admin-help">
                  Flash Sale chỉ được áp dụng khi đang bật và nằm trong thời gian hợp lệ.
                </small>
              </div>
            </div>

            <div class="admin-flashsale-form-note">
              <span class="admin-flashsale-form-note__icon">⚡</span>
              <div>
                <strong>Giới hạn mua Flash Sale theo từng sản phẩm</strong>
                <span>
                  Sau khi lưu chương trình, vào mục <b>Quản lý sản phẩm</b> để nhập giá Flash,
                  số lượng bán và giới hạn mua tối đa cho mỗi khách.
                </span>
              </div>
            </div>
          </div>
        </section>

        <aside class="admin-card admin-flashsale-config-card">
          <div class="admin-card__body">
            <div class="admin-flashsale-form-section-head">
              <div>
                <h2 class="admin-flashsale-form-section-title">Luồng cấu hình</h2>
                <p class="admin-flashsale-form-section-desc">
                  Các bước cần hoàn tất để Flash Sale hoạt động đúng.
                </p>
              </div>
            </div>

            <div class="admin-flashsale-config-steps">
              <div class="admin-flashsale-config-step is-done">
                <span>1</span>
                <div>
                  <strong>Tạo khung Flash Sale</strong>
                  <small>Nhập tên, thời gian bắt đầu/kết thúc và trạng thái.</small>
                </div>
              </div>

              <div class="admin-flashsale-config-step ${isEditFlashSale ? 'is-done' : ''}">
                <span>2</span>
                <div>
                  <strong>Thêm sản phẩm</strong>
                  <small>Chọn sản phẩm, giá Flash Sale và số lượng phân bổ.</small>
                </div>
              </div>

              <div class="admin-flashsale-config-step ${isEditFlashSale ? 'is-done' : ''}">
                <span>3</span>
                <div>
                  <strong>Cấu hình giới hạn / khách</strong>
                  <small>Chặn khách mua vượt quá số lượng được phép.</small>
                </div>
              </div>
            </div>

            <c:choose>
              <c:when test="${isEditFlashSale}">
                <a href="${ctx}/admin/flash-sale/items?flashSaleId=${flashSale.id}"
                   class="admin-btn admin-btn--primary admin-flashsale-side-btn">
                  Quản lý sản phẩm Flash Sale
                </a>
              </c:when>
              <c:otherwise>
                <div class="admin-flashsale-side-empty">
                  <span>💡</span>
                  <strong>Lưu chương trình trước</strong>
                  <small>Sau khi lưu, hệ thống mới cho phép thêm sản phẩm vào Flash Sale.</small>
                </div>
              </c:otherwise>
            </c:choose>
          </div>
        </aside>

      </div>

      <div class="admin-flashsale-form-actions">
        <a href="${ctx}/admin/flash-sale"
           class="admin-btn">
          Hủy
        </a>
        <button type="submit" class="admin-btn admin-btn--primary">
          Lưu Flash Sale
        </button>
      </div>
    </form>

    <c:if test="${isEditFlashSale}">
      <section class="admin-card admin-flashsale-form-items-card">
        <div class="admin-card__body">
          <div class="admin-flashsale-form-items-head">
            <div>
              <h2 class="admin-flashsale-form-section-title">Sản phẩm trong Flash Sale</h2>
              <p class="admin-flashsale-form-section-desc">
                Danh sách tóm tắt. Để thêm/xóa sản phẩm hoặc chỉnh giới hạn mỗi khách, bấm “Quản lý sản phẩm”.
              </p>
            </div>

            <a href="${ctx}/admin/flash-sale/items?flashSaleId=${flashSale.id}"
               class="admin-btn admin-btn--primary">
              Quản lý sản phẩm chi tiết
            </a>
          </div>

          <c:choose>
            <c:when test="${empty items}">
              <div class="admin-flashsale-form-empty">
                <span>🧴</span>
                <strong>Flash Sale này chưa có sản phẩm nào</strong>
                <small>Hãy thêm sản phẩm để cấu hình giá Flash, số lượng và giới hạn mua.</small>
              </div>
            </c:when>

            <c:otherwise>
              <div class="admin-flashsale-form-table-wrap">
                <table class="admin-table admin-flashsale-form-table">
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
                      <td class="admin-flashsale-form-product-cell">
                        <strong><c:out value="${item.product.title}" /></strong>
                        <span>ID: <c:out value="${item.product.id}" /></span>
                      </td>

                      <td class="admin-flashsale-form-price-cell">
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

                      <td class="admin-flashsale-form-progress-cell">
                        <progress class="admin-flashsale-admin-progress"
                                  max="100"
                                  value="${item.soldPercent}">
                          ${item.soldPercent}%
                        </progress>
                        <small>Đã bán <c:out value="${item.soldPercent}" />%</small>
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

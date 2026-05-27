<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Form khuyến mãi" scope="request"/>
<c:set var="activeMenu" value="promotions" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <!-- TOPBAR -->
    <div class="admin-topbar admin-promotion-topbar">
      <div>
        <h1 class="admin-h1">
          <c:choose>
            <c:when test="${mode == 'edit'}">Sửa khuyến mãi</c:when>
            <c:otherwise>Tạo khuyến mãi</c:otherwise>
          </c:choose>
        </h1>

        <p class="admin-subtext">
          Quản lý tập trung mã giảm giá, giảm giá thương hiệu, giảm theo giá trị đơn hàng
          và chương trình khuyến mãi cửa hàng.
        </p>
      </div>

      <div class="admin-promotion-create">
        <a class="admin-btn"
           href="${pageContext.request.contextPath}/admin/promotions?type=${promotionType}">
          ← Quay lại danh sách
        </a>
      </div>
    </div>

    <!-- TYPE TABS -->
    <div class="admin-card admin-promo-type-card">
      <div class="admin-card__body">
        <div class="admin-promo-tabs">
          <a class="admin-promo-tab ${promotionType == 'COUPON' ? 'is-active' : ''}"
             href="${pageContext.request.contextPath}/admin/promotions?action=new&type=COUPON">
            Mã giảm giá
          </a>

          <a class="admin-promo-tab ${promotionType == 'BRAND' ? 'is-active' : ''}"
             href="${pageContext.request.contextPath}/admin/promotions?action=new&type=BRAND">
            Giảm giá thương hiệu
          </a>

          <a class="admin-promo-tab ${promotionType == 'ORDER' ? 'is-active' : ''}"
             href="${pageContext.request.contextPath}/admin/promotions?action=new&type=ORDER">
            Giảm theo đơn hàng
          </a>

          <a class="admin-promo-tab ${promotionType == 'EVENT' ? 'is-active' : ''}"
             href="${pageContext.request.contextPath}/admin/promotions?action=new&type=EVENT">
            Chương trình khuyến mãi
          </a>
        </div>

        <p class="admin-subtext admin-promo-form-note">
          <c:choose>
            <c:when test="${promotionType == 'COUPON'}">
              Tạo mã giảm giá để khách hàng nhập khi thanh toán.
            </c:when>
            <c:when test="${promotionType == 'BRAND'}">
              Tạo giảm giá tự động cho sản phẩm thuộc một thương hiệu.
            </c:when>
            <c:when test="${promotionType == 'ORDER'}">
              Tạo giảm giá tự động khi đơn hàng đạt giá trị tối thiểu.
            </c:when>
            <c:when test="${promotionType == 'EVENT'}">
              Tạo chương trình khuyến mãi toàn cửa hàng, theo danh mục hoặc theo thương hiệu.
            </c:when>
            <c:otherwise>
              Chọn loại khuyến mãi cần tạo.
            </c:otherwise>
          </c:choose>
        </p>
      </div>
    </div>

    <!-- ERROR -->
    <c:if test="${not empty error}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${error}"/>
      </div>
    </c:if>

    <!-- FORM CARD -->
    <div class="admin-card">
      <div class="admin-card__body">

        <form method="post"
              action="${pageContext.request.contextPath}/admin/promotions"
              class="admin-form admin-promo-form">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="type" value="${promotionType}"/>

          <c:choose>
            <c:when test="${mode == 'edit'}">
              <input type="hidden" name="action" value="update"/>
            </c:when>
            <c:otherwise>
              <input type="hidden" name="action" value="create"/>
            </c:otherwise>
          </c:choose>

          <!-- =========================================================
               COUPON FORM
          ========================================================== -->
          <c:if test="${promotionType == 'COUPON'}">

            <input type="hidden" name="id" value="${coupon.id}"/>

            <div class="admin-form-section">
              <h2 class="admin-form-section__title">Thông tin mã giảm giá</h2>

              <div class="admin-form-grid admin-form-grid--2">
                <label class="admin-field">
                  <span>Mã coupon <b>*</b></span>
                  <input class="admin-input"
                         type="text"
                         name="code"
                         value="${coupon.code}"
                         placeholder="VD: SALE10, FREESHIP, VIP50"
                         required>
                </label>

                <label class="admin-field">
                  <span>Loại coupon <b>*</b></span>
                  <select class="admin-select" name="couponType" required>
                    <option value="DISCOUNT" ${coupon.type == 'DISCOUNT' ? 'selected' : ''}>
                      DISCOUNT - Giảm giá
                    </option>
                    <option value="FREESHIP" ${coupon.type == 'FREESHIP' ? 'selected' : ''}>
                      FREESHIP - Miễn phí vận chuyển
                    </option>
                    <option value="PRODUCT" ${coupon.type == 'PRODUCT' ? 'selected' : ''}>
                      PRODUCT - Theo sản phẩm
                    </option>
                    <option value="PERCENT" ${coupon.type == 'PERCENT' ? 'selected' : ''}>
                      PERCENT - Theo phần trăm
                    </option>
                  </select>
                </label>

                <label class="admin-field admin-field--full">
                  <span>Mô tả</span>
                  <textarea class="admin-textarea"
                            name="description"
                            rows="3"
                            placeholder="Mô tả ngắn về mã giảm giá"><c:out value="${coupon.description}"/></textarea>
                </label>

                <label class="admin-field">
                  <span>Phần trăm giảm <b>*</b></span>
                  <input class="admin-input"
                         type="number"
                         name="discountPercent"
                         min="1"
                         max="100"
                         value="${coupon.discountPercent}"
                         required>
                </label>

                <label class="admin-field">
                  <span>Số lượt dùng tối đa <b>*</b></span>
                  <input class="admin-input"
                         type="number"
                         name="maxUses"
                         min="1"
                         value="${coupon.maxUses}"
                         required>
                </label>

                <label class="admin-field">
                  <span>Đơn tối thiểu</span>
                  <input class="admin-input"
                         type="number"
                         name="minOrderAmount"
                         min="0"
                         step="1000"
                         value="${coupon.minOrderAmount}"
                         placeholder="VD: 300000">
                </label>

                <label class="admin-field">
                  <span>Giảm tối đa</span>
                  <input class="admin-input"
                         type="number"
                         name="maxDiscountAmount"
                         min="0"
                         step="1000"
                         value="${coupon.maxDiscountAmount}"
                         placeholder="Để trống nếu không giới hạn">
                </label>

                <label class="admin-field">
                  <span>Rank tối thiểu</span>
                  <select class="admin-select" name="minRankCode">
                    <option value="MEMBER" ${coupon.minRankCode == 'MEMBER' ? 'selected' : ''}>MEMBER</option>
                    <option value="SILVER" ${coupon.minRankCode == 'SILVER' ? 'selected' : ''}>SILVER</option>
                    <option value="GOLD" ${coupon.minRankCode == 'GOLD' ? 'selected' : ''}>GOLD</option>
                    <option value="DIAMOND" ${coupon.minRankCode == 'DIAMOND' ? 'selected' : ''}>DIAMOND</option>
                    <option value="VIP" ${coupon.minRankCode == 'VIP' ? 'selected' : ''}>VIP</option>
                  </select>
                </label>
              </div>
            </div>

            <div class="admin-form-section">
              <h2 class="admin-form-section__title">Thời gian & trạng thái</h2>

              <div class="admin-form-grid admin-form-grid--3">
                <label class="admin-field">
                  <span>Ngày bắt đầu</span>
                  <input class="admin-input"
                         type="date"
                         name="startDate"
                         value="${coupon.startDate}">
                </label>

                <label class="admin-field">
                  <span>Ngày kết thúc</span>
                  <input class="admin-input"
                         type="date"
                         name="endDate"
                         value="${coupon.endDate}">
                </label>

                <label class="admin-field admin-switch-field">
                  <span>Trạng thái</span>
                  <label class="admin-switch">
                    <input type="checkbox"
                           name="active"
                           value="1"
                      ${coupon.active ? 'checked' : ''}>
                    <span>Đang hoạt động</span>
                  </label>
                </label>
              </div>
            </div>

          </c:if>

          <!-- =========================================================
               BRAND DISCOUNT FORM
          ========================================================== -->
          <c:if test="${promotionType == 'BRAND'}">

            <input type="hidden" name="id" value="${discount.id}"/>

            <div class="admin-form-section">
              <h2 class="admin-form-section__title">Thông tin giảm giá thương hiệu</h2>

              <div class="admin-form-grid admin-form-grid--2">
                <label class="admin-field">
                  <span>Thương hiệu <b>*</b></span>
                  <select class="admin-select" name="brandId" required>
                    <option value="">-- Chọn thương hiệu --</option>
                    <c:forEach var="brand" items="${brands}">
                      <option value="${brand.id}" ${discount.brandId == brand.id ? 'selected' : ''}>
                        <c:out value="${brand.name}"/>
                      </option>
                    </c:forEach>
                  </select>
                </label>

                <label class="admin-field">
                  <span>Kiểu giảm <b>*</b></span>
                  <select class="admin-select" name="discountType" required>
                    <option value="PERCENT" ${discount.discountType == 'PERCENT' ? 'selected' : ''}>
                      PERCENT - Theo phần trăm
                    </option>
                    <option value="FIXED" ${discount.discountType == 'FIXED' ? 'selected' : ''}>
                      FIXED - Theo số tiền
                    </option>
                  </select>
                </label>

                <label class="admin-field">
                  <span>Giá trị giảm <b>*</b></span>
                  <input class="admin-input"
                         type="number"
                         name="discountValue"
                         min="1"
                         step="0.01"
                         value="${discount.discountValue}"
                         placeholder="VD: 10 hoặc 50000"
                         required>
                </label>

                <label class="admin-field">
                  <span>Giảm tối đa</span>
                  <input class="admin-input"
                         type="number"
                         name="maxDiscountAmount"
                         min="0"
                         step="1000"
                         value="${discount.maxDiscountAmount}"
                         placeholder="Để trống nếu không giới hạn">
                </label>
              </div>
            </div>

            <div class="admin-form-section">
              <h2 class="admin-form-section__title">Thời gian & trạng thái</h2>

              <div class="admin-form-grid admin-form-grid--3">
                <label class="admin-field">
                  <span>Ngày bắt đầu <b>*</b></span>
                  <input class="admin-input"
                         type="date"
                         name="startDate"
                         value="${discount.startDate}"
                         required>
                </label>

                <label class="admin-field">
                  <span>Ngày kết thúc <b>*</b></span>
                  <input class="admin-input"
                         type="date"
                         name="endDate"
                         value="${discount.endDate}"
                         required>
                </label>

                <label class="admin-field admin-switch-field">
                  <span>Trạng thái</span>
                  <label class="admin-switch">
                    <input type="checkbox"
                           name="active"
                           value="1"
                      ${discount.active ? 'checked' : ''}>
                    <span>Đang hoạt động</span>
                  </label>
                </label>
              </div>
            </div>

          </c:if>

          <!-- =========================================================
               ORDER DISCOUNT FORM
          ========================================================== -->
          <c:if test="${promotionType == 'ORDER'}">

            <input type="hidden" name="id" value="${orderDiscount.id}"/>

            <div class="admin-form-section">
              <h2 class="admin-form-section__title">Thông tin giảm theo giá trị đơn hàng</h2>

              <div class="admin-form-grid admin-form-grid--2">
                <label class="admin-field admin-field--full">
                  <span>Tên chương trình <b>*</b></span>
                  <input class="admin-input"
                         type="text"
                         name="name"
                         value="${orderDiscount.name}"
                         placeholder="VD: Giảm 10% cho đơn từ 500.000đ"
                         required>
                </label>

                <label class="admin-field">
                  <span>Giá trị đơn hàng tối thiểu <b>*</b></span>
                  <input class="admin-input"
                         type="number"
                         name="minOrderValue"
                         min="0"
                         step="1000"
                         value="${orderDiscount.minOrderValue}"
                         placeholder="VD: 500000"
                         required>
                </label>

                <label class="admin-field">
                  <span>Phần trăm giảm <b>*</b></span>
                  <input class="admin-input"
                         type="number"
                         name="discountPercent"
                         min="1"
                         max="100"
                         step="0.01"
                         value="${orderDiscount.discountPercent}"
                         placeholder="VD: 10"
                         required>
                </label>

                <label class="admin-field">
                  <span>Giảm tối đa</span>
                  <input class="admin-input"
                         type="number"
                         name="maxDiscountAmount"
                         min="0"
                         step="1000"
                         value="${orderDiscount.maxDiscountAmount}"
                         placeholder="Để trống nếu không giới hạn">
                </label>
              </div>
            </div>

            <div class="admin-form-section">
              <h2 class="admin-form-section__title">Thời gian & trạng thái</h2>

              <div class="admin-form-grid admin-form-grid--3">
                <label class="admin-field">
                  <span>Ngày bắt đầu <b>*</b></span>
                  <input class="admin-input"
                         type="date"
                         name="startDate"
                         value="${orderDiscount.startDate}"
                         required>
                </label>

                <label class="admin-field">
                  <span>Ngày kết thúc <b>*</b></span>
                  <input class="admin-input"
                         type="date"
                         name="endDate"
                         value="${orderDiscount.endDate}"
                         required>
                </label>

                <label class="admin-field admin-switch-field">
                  <span>Trạng thái</span>
                  <label class="admin-switch">
                    <input type="checkbox"
                           name="active"
                           value="1"
                      ${orderDiscount.active ? 'checked' : ''}>
                    <span>Đang hoạt động</span>
                  </label>
                </label>
              </div>
            </div>

          </c:if>

          <!-- =========================================================
               PROMOTION EVENT FORM
          ========================================================== -->
          <c:if test="${promotionType == 'EVENT'}">

            <input type="hidden" name="id" value="${event.id}"/>

            <div class="admin-form-section">
              <h2 class="admin-form-section__title">Thông tin chương trình khuyến mãi</h2>

              <div class="admin-form-grid admin-form-grid--2">
                <label class="admin-field admin-field--full">
                  <span>Tên chương trình <b>*</b></span>
                  <input class="admin-input"
                         type="text"
                         name="name"
                         value="${event.name}"
                         placeholder="VD: Sale hè toàn cửa hàng"
                         required>
                </label>

                <label class="admin-field">
                  <span>Phạm vi áp dụng <b>*</b></span>
                  <select class="admin-select" name="scope" id="promotionScope" required>
                    <option value="ALL" ${event.scope == 'ALL' ? 'selected' : ''}>
                      ALL - Toàn cửa hàng
                    </option>
                    <option value="CATEGORY" ${event.scope == 'CATEGORY' ? 'selected' : ''}>
                      CATEGORY - Theo danh mục
                    </option>
                    <option value="BRAND" ${event.scope == 'BRAND' ? 'selected' : ''}>
                      BRAND - Theo thương hiệu
                    </option>
                  </select>
                </label>

                <label class="admin-field">
                  <span>Kiểu giảm <b>*</b></span>
                  <select class="admin-select" name="discountType" required>
                    <option value="PERCENT" ${event.discountType == 'PERCENT' ? 'selected' : ''}>
                      PERCENT - Theo phần trăm
                    </option>
                    <option value="FIXED" ${event.discountType == 'FIXED' ? 'selected' : ''}>
                      FIXED - Theo số tiền
                    </option>
                  </select>
                </label>

                <label class="admin-field">
                  <span>Giá trị giảm <b>*</b></span>
                  <input class="admin-input"
                         type="number"
                         name="discountValue"
                         min="1"
                         step="0.01"
                         value="${event.discountValue}"
                         placeholder="VD: 15 hoặc 50000"
                         required>
                </label>

                <label class="admin-field">
                  <span>Giảm tối đa</span>
                  <input class="admin-input"
                         type="number"
                         name="maxDiscountAmount"
                         min="0"
                         step="1000"
                         value="${event.maxDiscountAmount}"
                         placeholder="Để trống nếu không giới hạn">
                </label>

                <label class="admin-field admin-promo-scope-field admin-promo-scope-category">
                  <span>Danh mục áp dụng</span>
                  <select class="admin-select" name="categoryId">
                    <option value="">-- Chọn danh mục --</option>
                    <c:forEach var="category" items="${categories}">
                      <option value="${category.id}" ${event.categoryId == category.id ? 'selected' : ''}>
                        <c:out value="${category.name}"/>
                      </option>
                    </c:forEach>
                  </select>
                </label>

                <label class="admin-field admin-promo-scope-field admin-promo-scope-brand">
                  <span>Thương hiệu áp dụng</span>
                  <select class="admin-select" name="brandId">
                    <option value="">-- Chọn thương hiệu --</option>
                    <c:forEach var="brand" items="${brands}">
                      <option value="${brand.id}" ${event.brandId == brand.id ? 'selected' : ''}>
                        <c:out value="${brand.name}"/>
                      </option>
                    </c:forEach>
                  </select>
                </label>
              </div>
            </div>

            <div class="admin-form-section">
              <h2 class="admin-form-section__title">Thời gian & trạng thái</h2>

              <div class="admin-form-grid admin-form-grid--3">
                <label class="admin-field">
                  <span>Ngày bắt đầu <b>*</b></span>
                  <input class="admin-input"
                         type="date"
                         name="startDate"
                         value="${event.startDate}"
                         required>
                </label>

                <label class="admin-field">
                  <span>Ngày kết thúc <b>*</b></span>
                  <input class="admin-input"
                         type="date"
                         name="endDate"
                         value="${event.endDate}"
                         required>
                </label>

                <label class="admin-field admin-switch-field">
                  <span>Trạng thái</span>
                  <label class="admin-switch">
                    <input type="checkbox"
                           name="active"
                           value="1"
                      ${event.active ? 'checked' : ''}>
                    <span>Đang hoạt động</span>
                  </label>
                </label>
              </div>
            </div>

          </c:if>

          <!-- SUBMIT -->
          <div class="admin-form-actions admin-promo-form-actions">
            <a class="admin-btn"
               href="${pageContext.request.contextPath}/admin/promotions?type=${promotionType}">
              Hủy
            </a>

            <button class="admin-btn admin-btn--primary" type="submit">
              <c:choose>
                <c:when test="${mode == 'edit'}">Cập nhật</c:when>
                <c:otherwise>Tạo mới</c:otherwise>
              </c:choose>
            </button>
          </div>

        </form>

      </div>
    </div>

  </div>
</main>

<script>
  (function () {
    const scopeSelect = document.getElementById("promotionScope");
    const categoryField = document.querySelector(".admin-promo-scope-category");
    const brandField = document.querySelector(".admin-promo-scope-brand");

    function updateScopeFields() {
      if (!scopeSelect || !categoryField || !brandField) {
        return;
      }

      const scope = scopeSelect.value;

      categoryField.style.display = scope === "CATEGORY" ? "" : "none";
      brandField.style.display = scope === "BRAND" ? "" : "none";
    }

    updateScopeFields();

    if (scopeSelect) {
      scopeSelect.addEventListener("change", updateScopeFields);
    }
  })();
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

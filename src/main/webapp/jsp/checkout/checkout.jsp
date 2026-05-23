<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/checkout.css?v=20260523_coupon_modal">

<style>
  /* ================= BUTTON STATE: NHẠT / ĐẬM ================= */

  .btn-apply-coupon,
  .btn-place-order {
    transition: background-color 0.25s ease, opacity 0.25s ease, transform 0.2s ease;
    border: none;
    outline: none;
  }

  .btn-apply-coupon:disabled,
  .btn-place-order:disabled {
    background: #e7a3b8 !important;
    color: #ffffff !important;
    cursor: not-allowed !important;
    opacity: 1 !important;
    transform: none !important;
  }

  .btn-apply-coupon:not(:disabled),
  .btn-place-order:not(:disabled) {
    background: #d63384 !important;
    color: #ffffff !important;
    cursor: pointer !important;
    opacity: 1 !important;
  }

  .btn-apply-coupon:not(:disabled):hover,
  .btn-place-order:not(:disabled):hover {
    background: #c72c79 !important;
    transform: translateY(-1px);
  }

  /* ================= DELIVERY OPTIONS + FREESHIP ================= */

  .delivery-empty {
    color: #777;
    font-size: 15px;
  }

  .delivery-options {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .delivery-options.hidden,
  .delivery-empty.hidden,
  .freeship-note.hidden {
    display: none !important;
  }

  .delivery-option {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 14px 16px;
    border: 1px solid #eee;
    border-radius: 16px;
    cursor: pointer;
    transition: all 0.2s ease;
    background: #fff;
  }

  .delivery-option:hover,
  .delivery-option:has(input:checked) {
    border-color: #d63384;
    background: #fff5fa;
  }

  .delivery-option input {
    accent-color: #d63384;
  }

  .delivery-info {
    display: flex;
    flex-direction: column;
    flex: 1;
  }

  .delivery-info strong {
    font-size: 15px;
    color: #111827;
  }

  .delivery-info small {
    margin-top: 4px;
    color: #6b7280;
    font-size: 13px;
  }

  .delivery-fee {
    font-weight: 700;
    color: #d63384;
    white-space: nowrap;
  }

  .freeship-note {
    margin-top: 12px;
    padding: 12px 14px;
    border-radius: 14px;
    background: #ecfdf5;
    color: #047857;
    font-weight: 700;
    font-size: 14px;
  }

  .delivery-option.is-disabled {
    opacity: 0.55;
    cursor: not-allowed !important;
    background: #f9fafb !important;
    border-color: #e5e7eb !important;
    box-shadow: none !important;
  }

  .delivery-option.is-disabled:hover {
    border-color: #e5e7eb !important;
    background: #f9fafb !important;
  }

  .delivery-option.is-disabled input {
    cursor: not-allowed !important;
  }

  .delivery-option.is-disabled .delivery-info strong,
  .delivery-option.is-disabled .delivery-info small,
  .delivery-option.is-disabled .delivery-fee {
    color: #9ca3af !important;
  }

  /* ================= COUPON MODAL ================= */

  .coupon-modal {
    position: fixed;
    inset: 0;
    z-index: 9999;
    display: none;
    align-items: center;
    justify-content: center;
  }

  .coupon-modal.show {
    display: flex;
  }

  .coupon-modal-backdrop {
    position: absolute;
    inset: 0;
    background: rgba(17, 24, 39, 0.45);
  }

  .coupon-modal-dialog {
    position: relative;
    width: min(560px, calc(100vw - 32px));
    max-height: 80vh;
    background: #ffffff;
    border-radius: 20px;
    box-shadow: 0 24px 70px rgba(0, 0, 0, 0.22);
    overflow: hidden;
    z-index: 1;
  }

  .coupon-modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 18px 22px;
    border-bottom: 1px solid #f1f1f1;
  }

  .coupon-modal-header h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 800;
    color: #111827;
  }

  .coupon-modal-close {
    width: 34px;
    height: 34px;
    border: none;
    border-radius: 999px;
    background: #f3f4f6;
    color: #374151;
    font-size: 24px;
    line-height: 1;
    cursor: pointer;
  }

  .coupon-modal-close:hover {
    background: #ffe6f0;
    color: #d63384;
  }

  .coupon-modal-body {
    padding: 18px 22px 22px;
    overflow-y: auto;
    max-height: calc(80vh - 72px);
  }

  .coupon-group-title {
    margin: 8px 0 12px;
    font-size: 14px;
    font-weight: 800;
    color: #374151;
  }

  .coupon-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 18px;
  }

  .coupon-item {
    width: 100%;
    display: flex;
    align-items: stretch;
    justify-content: space-between;
    gap: 12px;
    padding: 0;
    border: 1px solid #f2c4d7;
    border-radius: 16px;
    background: #fff7fb;
    overflow: hidden;
    cursor: pointer;
    text-align: left;
    transition: all 0.2s ease;
  }

  .coupon-item:hover {
    border-color: #d63384;
    transform: translateY(-1px);
    box-shadow: 0 10px 24px rgba(214, 51, 132, 0.14);
  }

  .coupon-ticket-left {
    flex: 1;
    padding: 14px 16px;
  }

  .coupon-code {
    font-size: 16px;
    font-weight: 900;
    color: #d63384;
    letter-spacing: 0.4px;
  }

  .coupon-desc {
    margin-top: 5px;
    font-size: 14px;
    color: #111827;
    font-weight: 600;
  }

  .coupon-condition {
    margin-top: 5px;
    font-size: 13px;
    color: #6b7280;
  }

  .coupon-ticket-right {
    width: 88px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #d63384;
    color: #ffffff;
    font-weight: 800;
  }

  .coupon-empty {
    padding: 26px 12px;
    text-align: center;
    color: #6b7280;
  }

  .coupon-empty-icon {
    font-size: 34px;
    margin-bottom: 8px;
  }

  body.coupon-modal-open {
    overflow: hidden;
  }
</style>

<c:set var="errors" value="${requestScope.errors}" />
<c:set var="checkoutCart" value="${not empty selectedCart ? selectedCart : cart}" />

<c:set var="orderSubtotal"
       value="${not empty subTotal ? subTotal : (not empty subtotal ? subtotal : 0)}" />

<c:set var="orderDiscount"
       value="${not empty discount ? discount : (not empty discountAmount ? discountAmount : 0)}" />

<c:set var="orderTotal"
       value="${not empty total ? total : (not empty totalAmount ? totalAmount : orderSubtotal - orderDiscount)}" />

<section class="checkout-page">
  <div class="checkout-container">

    <c:if test="${not empty errors.general}">
      <div class="checkout-alert-error">
        <c:out value="${errors.general}" />
      </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/checkout"
          method="post"
          class="checkout-grid"
          id="checkoutForm"
          novalidate>

      <input type="hidden"
             name="csrf_token"
             value="${sessionScope.CSRF_TOKEN}">

      <!-- ================= LEFT COLUMN ================= -->
      <div class="checkout-left">

        <!-- ACCOUNT -->
        <div class="checkout-card account-card">
          <div class="checkout-card-header">
            <h2>Tài khoản</h2>

            <a href="${pageContext.request.contextPath}/logout"
               class="checkout-logout">
              Đăng xuất
            </a>
          </div>

          <div class="account-box">
            <div class="account-avatar">
              <c:choose>
                <c:when test="${not empty sessionScope.authUser.username}">
                  ${fn:toUpperCase(fn:substring(sessionScope.authUser.username, 0, 1))}
                </c:when>
                <c:when test="${not empty sessionScope.user.username}">
                  ${fn:toUpperCase(fn:substring(sessionScope.user.username, 0, 1))}
                </c:when>
                <c:otherwise>U</c:otherwise>
              </c:choose>
            </div>

            <div class="account-info">
              <strong>
                <c:choose>
                  <c:when test="${not empty sessionScope.authUser.fullName}">
                    <c:out value="${sessionScope.authUser.fullName}" />
                  </c:when>
                  <c:when test="${not empty sessionScope.user.fullName}">
                    <c:out value="${sessionScope.user.fullName}" />
                  </c:when>
                  <c:when test="${not empty sessionScope.authUser.username}">
                    <c:out value="${sessionScope.authUser.username}" />
                  </c:when>
                  <c:when test="${not empty sessionScope.user.username}">
                    <c:out value="${sessionScope.user.username}" />
                  </c:when>
                  <c:otherwise>Khách hàng</c:otherwise>
                </c:choose>
              </strong>

              <span>
                <c:choose>
                  <c:when test="${not empty sessionScope.authUser.email}">
                    <c:out value="${sessionScope.authUser.email}" />
                  </c:when>
                  <c:when test="${not empty sessionScope.user.email}">
                    <c:out value="${sessionScope.user.email}" />
                  </c:when>
                  <c:otherwise>Chưa có email</c:otherwise>
                </c:choose>
              </span>
            </div>
          </div>
        </div>

        <!-- SHIPPING INFORMATION -->
        <div class="checkout-card shipping-card">
          <div class="checkout-card-header">
            <h2>Thông tin giao hàng</h2>
          </div>

          <!-- FULL NAME -->
          <div class="checkout-field">
            <label for="fullName">Họ và tên</label>

            <input type="text"
                   id="fullName"
                   name="fullName"
                   value="${not empty requestScope.formFullName ? requestScope.formFullName : (not empty param.fullName ? param.fullName : (not empty sessionScope.authUser.fullName ? sessionScope.authUser.fullName : sessionScope.user.fullName))}"
                   placeholder="Nhập họ và tên"
                   class="${not empty errors.fullName ? 'is-invalid' : ''}"
                   autocomplete="name">

            <div class="field-error" id="fullNameError">
              <c:if test="${not empty errors.fullName}">
                <c:out value="${errors.fullName}" />
              </c:if>
            </div>
          </div>

          <!-- PHONE -->
          <div class="checkout-field phone-field">
            <label for="phone">Số điện thoại</label>

            <div class="phone-input-wrap">
              <input type="text"
                     id="phone"
                     name="phone"
                     value="${not empty requestScope.formPhone ? requestScope.formPhone : (not empty param.phone ? param.phone : (not empty sessionScope.authUser.phone ? sessionScope.authUser.phone : sessionScope.user.phone))}"
                     placeholder="Nhập số điện thoại"
                     class="${not empty errors.phone ? 'is-invalid' : ''}"
                     inputmode="numeric"
                     autocomplete="tel">

              <span class="country-flag">★</span>
            </div>

            <div class="field-error" id="phoneError">
              <c:if test="${not empty errors.phone}">
                <c:out value="${errors.phone}" />
              </c:if>
            </div>
          </div>

          <!-- COUNTRY -->
          <div class="checkout-field">
            <label for="country">Quốc gia</label>

            <input type="text"
                   id="country"
                   name="country"
                   value="Vietnam"
                   readonly>
          </div>

          <!-- ADDRESS -->
          <div class="checkout-field">
            <label for="address">Địa chỉ, tên đường</label>

            <input type="text"
                   id="address"
                   name="address"
                   value="${not empty requestScope.formAddress ? requestScope.formAddress : param.address}"
                   placeholder="Ví dụ: 123 Nguyễn Văn A"
                   class="${not empty errors.address ? 'is-invalid' : ''}"
                   autocomplete="street-address">

            <div class="field-error" id="addressError">
              <c:if test="${not empty errors.address}">
                <c:out value="${errors.address}" />
              </c:if>
            </div>
          </div>

          <!-- LOCATION -->
          <div class="checkout-field no-margin location-field" id="locationField">
            <label for="locationInput">Tỉnh/TP, Phường/Xã</label>

            <input type="text"
                   id="locationInput"
                   name="locationText"
                   value="${not empty requestScope.formLocationText ? requestScope.formLocationText : param.locationText}"
                   placeholder="Tỉnh/TP, Phường/Xã"
                   autocomplete="off"
                   readonly
                   class="${not empty errors.location ? 'is-invalid' : ''}">

            <input type="hidden"
                   id="provinceInput"
                   name="province"
                   value="${not empty requestScope.formProvince ? requestScope.formProvince : param.province}">

            <input type="hidden"
                   id="provinceCodeInput"
                   name="provinceCode"
                   value="${not empty requestScope.formProvinceCode ? requestScope.formProvinceCode : param.provinceCode}">

            <input type="hidden"
                   id="wardInput"
                   name="wardName"
                   value="${not empty requestScope.formWardName ? requestScope.formWardName : param.wardName}">

            <input type="hidden"
                   id="wardCodeInput"
                   name="wardCode"
                   value="${not empty requestScope.formWardCode ? requestScope.formWardCode : param.wardCode}">

            <input type="hidden"
                   id="shippingAddressInput"
                   name="shippingAddress"
                   value="${not empty requestScope.formShippingAddress ? requestScope.formShippingAddress : param.shippingAddress}">

            <div class="location-dropdown" id="locationDropdown">
              <div class="location-tabs">
                <button type="button"
                        class="location-tab active"
                        id="provinceTab">
                  Tỉnh / TP
                </button>

                <button type="button"
                        class="location-tab disabled"
                        id="wardTab">
                  Phường / Xã
                </button>
              </div>

              <div class="location-list" id="provinceList">
                <div class="location-loading">
                  Đang tải danh sách Tỉnh/TP...
                </div>
              </div>

              <div class="location-list hidden" id="wardList">
                <div class="location-empty">
                  Vui lòng chọn Tỉnh/TP trước.
                </div>
              </div>
            </div>

            <div class="field-error" id="locationError">
              <c:if test="${not empty errors.location}">
                <c:out value="${errors.location}" />
              </c:if>
            </div>
          </div>
        </div>

        <!-- DELIVERY METHOD -->
        <div class="checkout-card delivery-card">
          <div class="checkout-card-header">
            <h2>Phương thức giao hàng</h2>
          </div>

          <div class="delivery-box" id="deliveryBox">
            <div class="delivery-empty" id="deliveryEmpty">
              Nhập địa chỉ để xem các phương thức giao hàng
            </div>

            <div class="delivery-options hidden" id="deliveryOptions">

              <label class="delivery-option">
                <input type="radio"
                       name="shippingMethod"
                       value="ECONOMY"
                       data-base-fee="20000"
                       checked>

                <span class="delivery-info">
                  <strong>Giao hàng tiết kiệm</strong>
                  <small>Thời gian dự kiến: 3 - 5 ngày</small>
                </span>

                <span class="delivery-fee">20.000đ</span>
              </label>

              <label class="delivery-option">
                <input type="radio"
                       name="shippingMethod"
                       value="FAST"
                       data-base-fee="35000">

                <span class="delivery-info">
                  <strong>Giao hàng nhanh</strong>
                  <small>Thời gian dự kiến: 1 - 3 ngày</small>
                </span>

                <span class="delivery-fee">35.000đ</span>
              </label>

              <label class="delivery-option">
                <input type="radio"
                       name="shippingMethod"
                       value="EXPRESS"
                       data-base-fee="50000">

                <span class="delivery-info">
                  <strong>Hỏa tốc</strong>
                  <small>Giao trong ngày, ưu tiên nội thành</small>
                </span>

                <span class="delivery-fee">50.000đ</span>
              </label>

            </div>

            <div class="freeship-note hidden" id="freeshipNote">
              🎉 Đơn hàng đã đạt điều kiện miễn phí vận chuyển.
            </div>

            <input type="hidden"
                   id="shippingFeeInput"
                   name="shippingFee"
                   value="0">
          </div>
        </div>

        <!-- PAYMENT METHOD -->
        <div class="checkout-card payment-card">
          <div class="checkout-card-header">
            <h2>Phương thức thanh toán</h2>
          </div>

          <div class="payment-list ${not empty errors.paymentMethod ? 'is-invalid' : ''}"
               id="paymentList">

            <label class="payment-item">
              <input type="radio"
                     name="paymentMethod"
                     value="COD"
              ${empty param.paymentMethod || param.paymentMethod == 'COD' ? 'checked' : ''}>

              <span class="payment-dot"></span>

              <span class="payment-icon">💵</span>

              <span class="payment-text">
                <strong>Thanh toán khi giao hàng (COD)</strong>
                <small>Thanh toán tiền mặt khi nhận hàng</small>
              </span>
            </label>

            <label class="payment-item">
              <input type="radio"
                     name="paymentMethod"
                     value="VNPAY"
              ${param.paymentMethod == 'VNPAY' ? 'checked' : ''}>

              <span class="payment-dot"></span>

              <span class="payment-icon">💳</span>

              <span class="payment-text">
                <strong>Thanh toán qua VNPAY</strong>
                <small>Chuyển sang cổng thanh toán VNPAY</small>
              </span>
            </label>
          </div>

          <div class="field-error payment-error" id="paymentMethodError">
            <c:if test="${not empty errors.paymentMethod}">
              <c:out value="${errors.paymentMethod}" />
            </c:if>
          </div>
        </div>

      </div>

      <!-- ================= RIGHT COLUMN ================= -->
      <div class="checkout-right">

        <!-- PRODUCTS -->
        <div class="checkout-card products-card">
          <div class="checkout-card-header">
            <h2>Hàng hóa</h2>
          </div>

          <div class="checkout-products">
            <c:choose>
              <c:when test="${empty checkoutCart}">
                <div class="checkout-empty-product">
                  Chưa có sản phẩm để thanh toán.
                </div>
              </c:when>

              <c:otherwise>
                <c:forEach var="entry" items="${checkoutCart}">
                  <c:set var="item" value="${entry.value}" />
                  <c:set var="cartKey" value="${entry.key}" />

                  <fmt:formatNumber var="itemSubtotalRaw"
                                    value="${item.subtotal}"
                                    pattern="0"
                                    groupingUsed="false" />

                  <div class="checkout-product-item">
                    <div class="checkout-product-main">

                      <div class="checkout-product-img">
                        <c:choose>
                          <c:when test="${not empty item.imageUrl}">
                            <img src="${pageContext.request.contextPath}${item.imageUrl}"
                                 alt="${fn:escapeXml(item.title)}"
                                 onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/assets/images/default-product.jpg';">
                          </c:when>

                          <c:otherwise>
                            <img src="${pageContext.request.contextPath}/assets/images/default-product.jpg"
                                 alt="default">
                          </c:otherwise>
                        </c:choose>
                      </div>

                      <div class="checkout-product-info">
                        <div class="checkout-product-title">
                          <c:out value="${item.title}" />
                        </div>

                        <div class="checkout-product-variant">
                          <c:out value="${empty item.variantDisplayName ? 'Mặc định' : item.variantDisplayName}" />
                          <span>›</span>
                        </div>

                        <div class="checkout-product-price">
                          <fmt:formatNumber value="${item.price}" type="number" groupingUsed="true" />đ
                        </div>
                      </div>

                      <a href="${pageContext.request.contextPath}/cart/remove?productId=${item.productId}&key=${cartKey}"
                         class="checkout-remove"
                         title="Xóa sản phẩm"
                         onclick="return confirm('Xóa sản phẩm này khỏi đơn thanh toán?');">
                        🗑
                      </a>
                    </div>

                    <div class="checkout-product-bottom">
                      <div class="checkout-qty"
                           data-product-id="${item.productId}"
                           data-cart-key="${cartKey}">

                        <button type="button"
                                class="checkout-qty-btn js-checkout-qty-btn"
                                data-action="decrease"
                                aria-label="Giảm số lượng">
                          −
                        </button>

                        <span class="checkout-qty-value">
                            ${item.quantity}
                        </span>

                        <button type="button"
                                class="checkout-qty-btn js-checkout-qty-btn"
                                data-action="increase"
                                aria-label="Tăng số lượng">
                          ＋
                        </button>
                      </div>

                      <strong class="checkout-product-subtotal js-item-subtotal"
                              data-raw="${itemSubtotalRaw}">
                        <fmt:formatNumber value="${item.subtotal}" type="number" groupingUsed="true" />đ
                      </strong>
                    </div>
                  </div>
                </c:forEach>
              </c:otherwise>
            </c:choose>
          </div>
        </div>

        <!-- COUPON -->
        <div class="checkout-card coupon-card">
          <div class="checkout-card-header">
            <h2>Mã khuyến mãi</h2>
          </div>

          <button type="button"
                  class="coupon-select-btn"
                  id="openCouponModal">
            <span>🎟 Chọn mã</span>
            <span class="coupon-arrow">›</span>
          </button>

          <div class="coupon-input-row">
            <input type="text"
                   id="couponCode"
                   name="couponCode"
                   value="${not empty couponCode ? couponCode : param.couponCode}"
                   placeholder="Mã khuyến mãi">

            <button type="button"
                    id="applyCouponBtn"
                    class="btn-apply-coupon"
                    disabled>
              Áp dụng
            </button>
          </div>

          <c:if test="${not empty couponLoadError}">
            <div class="coupon-message error">
              <c:out value="${couponLoadError}" />
            </div>
          </c:if>

          <div id="couponMessage" class="coupon-message"></div>
        </div>

        <!-- SUMMARY -->
        <div class="checkout-card checkout-summary-card">
          <div class="checkout-card-header">
            <h2>Tóm tắt đơn hàng</h2>
          </div>

          <div class="summary-line">
            <span>Tổng tiền hàng</span>
            <strong id="summarySubtotal">
              <fmt:formatNumber value="${orderSubtotal}" type="number" groupingUsed="true" />đ
            </strong>
          </div>

          <div class="summary-line">
            <span>Phí vận chuyển</span>
            <strong id="summaryShippingFee">-</strong>
          </div>

          <div class="summary-line">
            <span>Giảm giá</span>
            <strong id="summaryDiscount">
              <fmt:formatNumber value="${orderDiscount}" type="number" groupingUsed="true" />đ
            </strong>
          </div>

          <div class="summary-line summary-total">
            <span>Tổng thanh</span>
            <strong id="summaryTotal">
              <fmt:formatNumber value="${orderTotal}" type="number" groupingUsed="true" />đ
            </strong>
          </div>

          <button type="submit"
                  class="btn-place-order"
                  id="placeOrderBtn"
                  disabled>
            Đặt hàng
          </button>
        </div>

      </div>

    </form>
  </div>
</section>

<!-- ================= COUPON MODAL ================= -->
<div class="coupon-modal" id="couponModal">
  <div class="coupon-modal-backdrop" data-close-coupon-modal></div>

  <div class="coupon-modal-dialog">
    <div class="coupon-modal-header">
      <h3>Chọn mã khuyến mãi</h3>

      <button type="button"
              class="coupon-modal-close"
              data-close-coupon-modal>
        ×
      </button>
    </div>

    <div class="coupon-modal-body">

      <c:if test="${not empty savedCoupons}">
        <div class="coupon-group-title">Mã khuyến mãi đã lưu</div>

        <div class="coupon-list">
          <c:forEach var="coupon" items="${savedCoupons}">
            <button type="button"
                    class="coupon-item js-select-coupon"
                    data-code="${fn:escapeXml(coupon.code)}">

              <div class="coupon-ticket-left">
                <div class="coupon-code">
                  <c:out value="${coupon.code}" />
                </div>

                <div class="coupon-desc">
                  Giảm ${coupon.discountPercent}%

                  <c:if test="${not empty coupon.maxDiscountAmount}">
                    , tối đa
                    <fmt:formatNumber value="${coupon.maxDiscountAmount}"
                                      type="number"
                                      groupingUsed="true" />đ
                  </c:if>
                </div>

                <c:if test="${not empty coupon.minOrderAmount}">
                  <div class="coupon-condition">
                    Đơn từ
                    <fmt:formatNumber value="${coupon.minOrderAmount}"
                                      type="number"
                                      groupingUsed="true" />đ
                  </div>
                </c:if>
              </div>

              <div class="coupon-ticket-right">
                Chọn
              </div>
            </button>
          </c:forEach>
        </div>
      </c:if>

      <c:if test="${not empty availableCoupons}">
        <div class="coupon-group-title">Mã phù hợp với đơn hàng</div>

        <div class="coupon-list">
          <c:forEach var="coupon" items="${availableCoupons}">
            <button type="button"
                    class="coupon-item js-select-coupon"
                    data-code="${fn:escapeXml(coupon.code)}">

              <div class="coupon-ticket-left">
                <div class="coupon-code">
                  <c:out value="${coupon.code}" />
                </div>

                <div class="coupon-desc">
                  Giảm ${coupon.discountPercent}%

                  <c:if test="${not empty coupon.maxDiscountAmount}">
                    , tối đa
                    <fmt:formatNumber value="${coupon.maxDiscountAmount}"
                                      type="number"
                                      groupingUsed="true" />đ
                  </c:if>
                </div>

                <c:if test="${not empty coupon.minOrderAmount}">
                  <div class="coupon-condition">
                    Đơn từ
                    <fmt:formatNumber value="${coupon.minOrderAmount}"
                                      type="number"
                                      groupingUsed="true" />đ
                  </div>
                </c:if>
              </div>

              <div class="coupon-ticket-right">
                Chọn
              </div>
            </button>
          </c:forEach>
        </div>
      </c:if>

      <c:if test="${empty savedCoupons and empty availableCoupons}">
        <div class="coupon-empty">
          <div class="coupon-empty-icon">🏷️</div>
          <p>Không có mã khuyến mãi phù hợp</p>
        </div>
      </c:if>

    </div>
  </div>
</div>

<!-- ================= COUPON MODAL SCRIPT ================= -->
<script>
  (function () {
    const modal = document.getElementById("couponModal");
    const openBtn = document.getElementById("openCouponModal");
    const couponInput = document.getElementById("couponCode");

    function openModal() {
      if (!modal) return;

      modal.classList.add("show");
      document.body.classList.add("coupon-modal-open");
    }

    function closeModal() {
      if (!modal) return;

      modal.classList.remove("show");
      document.body.classList.remove("coupon-modal-open");
    }

    if (openBtn) {
      openBtn.addEventListener("click", openModal);
    }

    document.querySelectorAll("[data-close-coupon-modal]").forEach(function (el) {
      el.addEventListener("click", closeModal);
    });

    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape") {
        closeModal();
      }
    });

    document.querySelectorAll(".js-select-coupon").forEach(function (button) {
      button.addEventListener("click", function () {
        const code = button.dataset.code;

        if (couponInput && code) {
          couponInput.value = code;
          couponInput.dispatchEvent(new Event("input", { bubbles: true }));
        }

        if (window.updateCheckoutButtonsState) {
          window.updateCheckoutButtonsState();
        }

        closeModal();
      });
    });
  })();
</script>

<!-- ================= FRONTEND VALIDATION ================= -->
<script>
  (function () {
    const form = document.getElementById("checkoutForm");

    const fullNameInput = document.getElementById("fullName");
    const phoneInput = document.getElementById("phone");
    const addressInput = document.getElementById("address");
    const locationInput = document.getElementById("locationInput");

    const provinceInput = document.getElementById("provinceInput");
    const wardInput = document.getElementById("wardInput");

    const paymentList = document.getElementById("paymentList");
    const placeOrderBtn = document.getElementById("placeOrderBtn");

    const couponInput = document.getElementById("couponCode");
    const applyCouponBtn = document.getElementById("applyCouponBtn");

    function setFieldError(input, errorId, message) {
      const errorEl = document.getElementById(errorId);

      if (input) {
        input.classList.toggle("is-invalid", !!message);
      }

      if (errorEl) {
        errorEl.textContent = message || "";
      }
    }

    function validateFullName() {
      const value = fullNameInput ? fullNameInput.value.trim() : "";

      if (!value) {
        setFieldError(fullNameInput, "fullNameError", "Vui lòng nhập họ và tên người nhận.");
        return false;
      }

      if (value.length < 2 || value.length > 80) {
        setFieldError(fullNameInput, "fullNameError", "Họ và tên phải từ 2 đến 80 ký tự.");
        return false;
      }

      const nameRegex = /^[\p{L}\s'.-]+$/u;

      if (!nameRegex.test(value)) {
        setFieldError(fullNameInput, "fullNameError", "Họ và tên chỉ nên chứa chữ cái và khoảng trắng.");
        return false;
      }

      setFieldError(fullNameInput, "fullNameError", "");
      return true;
    }

    function validatePhone() {
      const value = phoneInput ? phoneInput.value.trim() : "";
      const phoneRegex = /^0(3|5|7|8|9)[0-9]{8}$/;

      if (!value) {
        setFieldError(phoneInput, "phoneError", "Vui lòng nhập số điện thoại.");
        return false;
      }

      if (!phoneRegex.test(value)) {
        setFieldError(phoneInput, "phoneError", "Số điện thoại không hợp lệ. Ví dụ: 0912345678.");
        return false;
      }

      setFieldError(phoneInput, "phoneError", "");
      return true;
    }

    function validateAddress() {
      const value = addressInput ? addressInput.value.trim() : "";

      if (!value) {
        setFieldError(addressInput, "addressError", "Vui lòng nhập địa chỉ giao hàng.");
        return false;
      }

      if (value.length < 5 || value.length > 160) {
        setFieldError(addressInput, "addressError", "Địa chỉ phải từ 5 đến 160 ký tự.");
        return false;
      }

      setFieldError(addressInput, "addressError", "");
      return true;
    }

    function validateLocation() {
      const province = provinceInput ? provinceInput.value.trim() : "";
      const ward = wardInput ? wardInput.value.trim() : "";

      if (!province) {
        setFieldError(locationInput, "locationError", "Vui lòng chọn Tỉnh/TP.");
        return false;
      }

      if (!ward) {
        setFieldError(locationInput, "locationError", "Vui lòng chọn Phường/Xã sau khi chọn Tỉnh/TP.");
        return false;
      }

      setFieldError(locationInput, "locationError", "");
      return true;
    }

    function validatePaymentMethod() {
      const checked = document.querySelector("input[name='paymentMethod']:checked");
      const errorEl = document.getElementById("paymentMethodError");

      if (!checked) {
        if (paymentList) {
          paymentList.classList.add("is-invalid");
        }

        if (errorEl) {
          errorEl.textContent = "Vui lòng chọn phương thức thanh toán.";
        }

        return false;
      }

      if (checked.value !== "COD" && checked.value !== "VNPAY") {
        if (paymentList) {
          paymentList.classList.add("is-invalid");
        }

        if (errorEl) {
          errorEl.textContent = "Phương thức thanh toán không hợp lệ.";
        }

        return false;
      }

      if (paymentList) {
        paymentList.classList.remove("is-invalid");
      }

      if (errorEl) {
        errorEl.textContent = "";
      }

      return true;
    }

    function hasValue(input) {
      return input && input.value && input.value.trim() !== "";
    }

    function updateApplyCouponButton() {
      if (!applyCouponBtn || !couponInput) {
        return;
      }

      applyCouponBtn.disabled = !hasValue(couponInput);
    }

    function isCheckoutFilled() {
      const checkedPayment = document.querySelector("input[name='paymentMethod']:checked");

      return hasValue(fullNameInput)
              && hasValue(phoneInput)
              && hasValue(addressInput)
              && hasValue(provinceInput)
              && hasValue(wardInput)
              && !!checkedPayment;
    }

    function updatePlaceOrderButton() {
      if (!placeOrderBtn) {
        return;
      }

      placeOrderBtn.disabled = !isCheckoutFilled();
    }

    window.updateCheckoutButtonsState = function () {
      updateApplyCouponButton();
      updatePlaceOrderButton();
    };

    if (fullNameInput) {
      fullNameInput.addEventListener("blur", validateFullName);
      fullNameInput.addEventListener("input", function () {
        setFieldError(fullNameInput, "fullNameError", "");
        updatePlaceOrderButton();
      });
    }

    if (phoneInput) {
      phoneInput.addEventListener("blur", validatePhone);
      phoneInput.addEventListener("input", function () {
        this.value = this.value.replace(/[^\d]/g, "");
        setFieldError(phoneInput, "phoneError", "");
        updatePlaceOrderButton();
      });
    }

    if (addressInput) {
      addressInput.addEventListener("blur", validateAddress);
      addressInput.addEventListener("input", function () {
        setFieldError(addressInput, "addressError", "");
        updatePlaceOrderButton();
      });
    }

    if (couponInput) {
      couponInput.addEventListener("input", updateApplyCouponButton);
      couponInput.addEventListener("blur", updateApplyCouponButton);
    }

    document.querySelectorAll("input[name='paymentMethod']").forEach(function (radio) {
      radio.addEventListener("change", function () {
        validatePaymentMethod();
        updatePlaceOrderButton();
      });
    });

    if (form) {
      form.addEventListener("submit", function (event) {
        const validFullName = validateFullName();
        const validPhone = validatePhone();
        const validAddress = validateAddress();
        const validLocation = validateLocation();
        const validPayment = validatePaymentMethod();

        if (!validFullName || !validPhone || !validAddress || !validLocation || !validPayment) {
          event.preventDefault();

          const firstInvalid = document.querySelector(".is-invalid");

          if (firstInvalid) {
            firstInvalid.scrollIntoView({
              behavior: "smooth",
              block: "center"
            });
          }

          updatePlaceOrderButton();
        }
      });
    }

    updateApplyCouponButton();
    updatePlaceOrderButton();
  })();
</script>

<!-- ================= APPLY COUPON AJAX ================= -->
<script>
  (function () {
    const applyBtn = document.getElementById("applyCouponBtn");
    const couponInput = document.getElementById("couponCode");
    const couponMessage = document.getElementById("couponMessage");

    const summarySubtotal = document.getElementById("summarySubtotal");
    const summaryDiscount = document.getElementById("summaryDiscount");
    const summaryTotal = document.getElementById("summaryTotal");

    function formatVnd(value) {
      return new Intl.NumberFormat("vi-VN").format(Math.round(Number(value || 0))) + "đ";
    }

    function setCouponMessage(message, isError) {
      if (!couponMessage) return;

      couponMessage.textContent = message || "";
      couponMessage.classList.toggle("error", !!isError);
      couponMessage.classList.toggle("success", !isError && !!message);
    }

    if (applyBtn && couponInput) {
      applyBtn.addEventListener("click", function () {
        const code = couponInput.value.trim();

        if (!code) {
          setCouponMessage("Vui lòng nhập mã khuyến mãi.", true);
          return;
        }

        fetch("${pageContext.request.contextPath}/ajax/apply-coupon?code=" + encodeURIComponent(code))
                .then(function (res) {
                  return res.json();
                })
                .then(function (data) {
                  if (data.error) {
                    setCouponMessage(data.error, true);
                    return;
                  }

                  if (summarySubtotal) summarySubtotal.textContent = formatVnd(data.subtotal);
                  if (summaryDiscount) summaryDiscount.textContent = formatVnd(data.discount);
                  if (summaryTotal) summaryTotal.textContent = formatVnd(data.total);

                  if (window.updateShippingFeeByLocation) {
                    window.updateShippingFeeByLocation();
                  }

                  setCouponMessage("Áp dụng mã giảm giá thành công.", false);
                })
                .catch(function () {
                  setCouponMessage("Không thể áp dụng mã giảm giá lúc này.", true);
                });
      });
    }
  })();
</script>

<!-- ================= CHECKOUT QUANTITY AJAX ================= -->
<script>
  (function () {
    const contextPath = "${pageContext.request.contextPath}";
    const csrfToken = "${sessionScope.CSRF_TOKEN}";

    const summarySubtotal = document.getElementById("summarySubtotal");
    const summaryDiscount = document.getElementById("summaryDiscount");
    const summaryTotal = document.getElementById("summaryTotal");

    const couponInput = document.getElementById("couponCode");
    const applyCouponBtn = document.getElementById("applyCouponBtn");

    function formatVnd(value) {
      return new Intl.NumberFormat("vi-VN").format(Math.round(Number(value || 0))) + "đ";
    }

    function parseMoney(value) {
      const number = Number(String(value || "0").trim());
      return Number.isFinite(number) ? number : 0;
    }

    function calculateCurrentSubtotal() {
      let subtotal = 0;

      document.querySelectorAll(".js-item-subtotal").forEach(function (el) {
        subtotal += parseMoney(el.dataset.raw);
      });

      return subtotal;
    }

    function updateSummaryWithoutCoupon() {
      const subtotal = calculateCurrentSubtotal();

      if (summarySubtotal) {
        summarySubtotal.textContent = formatVnd(subtotal);
      }

      if (summaryDiscount) {
        summaryDiscount.textContent = formatVnd(0);
      }

      if (summaryTotal) {
        summaryTotal.textContent = formatVnd(subtotal);
      }
    }

    function recalculateSummary() {
      updateSummaryWithoutCoupon();

      if (couponInput && couponInput.value.trim() && applyCouponBtn) {
        applyCouponBtn.click();
      }
    }

    document.querySelectorAll(".js-checkout-qty-btn").forEach(function (button) {
      button.addEventListener("click", function () {
        const qtyBox = button.closest(".checkout-qty");
        const productItem = button.closest(".checkout-product-item");

        if (!qtyBox || !productItem) {
          return;
        }

        const productId = qtyBox.dataset.productId;
        const cartKey = qtyBox.dataset.cartKey;
        const action = button.dataset.action;

        const params = new URLSearchParams();
        params.append("csrf_token", csrfToken);
        params.append("productId", productId);
        params.append("key", cartKey);
        params.append("action", action);

        button.disabled = true;

        fetch(contextPath + "/ajax/checkout-quantity", {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
            "Accept": "application/json"
          },
          body: params.toString()
        })
                .then(function (res) {
                  return res.json();
                })
                .then(function (data) {
                  if (!data.ok) {
                    alert(data.message || "Không thể cập nhật số lượng.");
                    return;
                  }

                  const qtyValue = qtyBox.querySelector(".checkout-qty-value");
                  const subtotalEl = productItem.querySelector(".js-item-subtotal");

                  if (qtyValue) {
                    qtyValue.textContent = data.quantity;
                  }

                  if (subtotalEl) {
                    subtotalEl.dataset.raw = data.itemSubtotal;
                    subtotalEl.textContent = formatVnd(data.itemSubtotal);
                  }

                  recalculateSummary();

                  if (window.updateShippingFeeByLocation) {
                    window.updateShippingFeeByLocation();
                  }
                })
                .catch(function () {
                  alert("Không thể cập nhật số lượng lúc này.");
                })
                .finally(function () {
                  button.disabled = false;
                });
      });
    });
  })();
</script>

<!-- ================= SHIPPING FEE / FREESHIP ================= -->
<script>
  (function () {
    const FREE_SHIP_THRESHOLD = 500000;

    const provinceInput = document.getElementById("provinceInput");
    const wardInput = document.getElementById("wardInput");

    const deliveryEmpty = document.getElementById("deliveryEmpty");
    const deliveryOptions = document.getElementById("deliveryOptions");
    const freeshipNote = document.getElementById("freeshipNote");

    const shippingFeeInput = document.getElementById("shippingFeeInput");
    const summaryShippingFee = document.getElementById("summaryShippingFee");

    const summarySubtotal = document.getElementById("summarySubtotal");
    const summaryDiscount = document.getElementById("summaryDiscount");
    const summaryTotal = document.getElementById("summaryTotal");

    function parseVndText(text) {
      if (!text) return 0;
      return Number(String(text).replace(/[^\d]/g, "")) || 0;
    }

    function formatVnd(value) {
      return new Intl.NumberFormat("vi-VN").format(Math.round(Number(value || 0))) + "đ";
    }

    function isHcmCity(provinceName) {
      const value = String(provinceName || "").toLowerCase();

      return value.includes("hồ chí minh")
              || value.includes("ho chi minh")
              || value.includes("tp. hcm")
              || value.includes("tphcm")
              || value.includes("thành phố hồ chí minh");
    }

    function hasValidLocation() {
      const province = provinceInput ? provinceInput.value.trim() : "";
      const ward = wardInput ? wardInput.value.trim() : "";

      return province !== "" && ward !== "";
    }

    function getOrderValueAfterVoucher() {
      const subtotal = parseVndText(summarySubtotal ? summarySubtotal.textContent : "0");
      const discount = parseVndText(summaryDiscount ? summaryDiscount.textContent : "0");

      return Math.max(subtotal - discount, 0);
    }

    function isFreeShipEligible() {
      return getOrderValueAfterVoucher() >= FREE_SHIP_THRESHOLD;
    }

    function getSelectedShippingInput() {
      return document.querySelector("input[name='shippingMethod']:checked");
    }

    function getSelectedShippingMethod() {
      const selected = getSelectedShippingInput();
      return selected ? selected.value : "ECONOMY";
    }

    function calculateShippingFeeByMethod(method, provinceName) {
      const hcm = isHcmCity(provinceName);

      if (method === "ECONOMY") {
        return hcm ? 20000 : 35000;
      }

      if (method === "FAST") {
        return hcm ? 35000 : 50000;
      }

      if (method === "EXPRESS") {
        // Hỏa tốc chỉ áp dụng nội thành / TP.HCM.
        return hcm ? 50000 : 0;
      }

      return hcm ? 20000 : 35000;
    }

    function updateShippingMethodLabels() {
      const province = provinceInput ? provinceInput.value.trim() : "";
      const hcm = isHcmCity(province);
      const validLocation = hasValidLocation();

      document.querySelectorAll("input[name='shippingMethod']").forEach(function (input) {
        const option = input.closest(".delivery-option");
        const feeEl = option ? option.querySelector(".delivery-fee") : null;
        const descEl = option ? option.querySelector(".delivery-info small") : null;

        if (!option || !feeEl) {
          return;
        }

        option.classList.remove("is-disabled");
        input.disabled = false;

        if (!validLocation) {
          return;
        }

        if (input.value === "ECONOMY") {
          feeEl.textContent = hcm ? "20.000đ" : "35.000đ";
          if (descEl) {
            descEl.textContent = hcm ? "Thời gian dự kiến: 3 - 5 ngày" : "Ngoại tỉnh: 3 - 5 ngày";
          }
        }

        if (input.value === "FAST") {
          feeEl.textContent = hcm ? "35.000đ" : "50.000đ";
          if (descEl) {
            descEl.textContent = hcm ? "Thời gian dự kiến: 1 - 3 ngày" : "Ngoại tỉnh: 1 - 3 ngày";
          }
        }

        if (input.value === "EXPRESS") {
          if (hcm) {
            feeEl.textContent = "50.000đ";
            if (descEl) {
              descEl.textContent = "Giao trong ngày, ưu tiên nội thành";
            }
          } else {
            feeEl.textContent = "Không hỗ trợ";
            if (descEl) {
              descEl.textContent = "Chỉ áp dụng cho khu vực TP.HCM";
            }

            input.disabled = true;
            option.classList.add("is-disabled");

            if (input.checked) {
              const economy = document.querySelector("input[name='shippingMethod'][value='ECONOMY']");
              if (economy) {
                economy.checked = true;
              }
            }
          }
        }
      });
    }

    function calculateShippingFee() {
      const province = provinceInput ? provinceInput.value.trim() : "";

      if (!hasValidLocation()) {
        return 0;
      }

      if (isFreeShipEligible()) {
        return 0;
      }

      return calculateShippingFeeByMethod(getSelectedShippingMethod(), province);
    }

    function updateCheckoutTotal() {
      const subtotal = parseVndText(summarySubtotal ? summarySubtotal.textContent : "0");
      const discount = parseVndText(summaryDiscount ? summaryDiscount.textContent : "0");
      const shippingFee = Number(shippingFeeInput ? shippingFeeInput.value : 0) || 0;

      const total = Math.max(subtotal - discount + shippingFee, 0);

      if (summaryTotal) {
        summaryTotal.textContent = formatVnd(total);
      }
    }

    function updateShippingDisplay() {
      updateShippingMethodLabels();

      if (!hasValidLocation()) {
        if (deliveryEmpty) {
          deliveryEmpty.classList.remove("hidden");
        }

        if (deliveryOptions) {
          deliveryOptions.classList.add("hidden");
        }

        if (freeshipNote) {
          freeshipNote.classList.add("hidden");
        }

        if (summaryShippingFee) {
          summaryShippingFee.textContent = "-";
        }

        if (shippingFeeInput) {
          shippingFeeInput.value = "0";
        }

        updateCheckoutTotal();
        return;
      }

      if (deliveryEmpty) {
        deliveryEmpty.classList.add("hidden");
      }

      if (deliveryOptions) {
        deliveryOptions.classList.remove("hidden");
      }

      const freeship = isFreeShipEligible();
      const fee = calculateShippingFee();

      if (summaryShippingFee) {
        summaryShippingFee.textContent = freeship ? "Miễn phí" : formatVnd(fee);
      }

      if (shippingFeeInput) {
        shippingFeeInput.value = String(fee);
      }

      if (freeshipNote) {
        freeshipNote.classList.toggle("hidden", !freeship);
      }

      updateCheckoutTotal();
    }

    document.querySelectorAll("input[name='shippingMethod']").forEach(function (radio) {
      radio.addEventListener("change", updateShippingDisplay);
    });

    window.updateShippingFeeByLocation = updateShippingDisplay;

    updateShippingDisplay();
  })();
</script>

<!-- ================= PROVINCE / WARD SELECTOR ================= -->
<script>
  (function () {
    const API_BASE_URL = "https://provinces.open-api.vn/api/v2";

    const checkoutForm = document.getElementById("checkoutForm");
    const addressInput = document.getElementById("address");
    const deliveryBox = document.getElementById("deliveryBox");

    const locationField = document.getElementById("locationField");
    const locationInput = document.getElementById("locationInput");

    const provinceInput = document.getElementById("provinceInput");
    const provinceCodeInput = document.getElementById("provinceCodeInput");
    const wardInput = document.getElementById("wardInput");
    const wardCodeInput = document.getElementById("wardCodeInput");
    const shippingAddressInput = document.getElementById("shippingAddressInput");

    const provinceTab = document.getElementById("provinceTab");
    const wardTab = document.getElementById("wardTab");
    const provinceList = document.getElementById("provinceList");
    const wardList = document.getElementById("wardList");

    if (!locationField || !locationInput || !provinceList || !wardList) {
      return;
    }

    let provinces = [];
    let selectedProvince = null;
    let selectedWard = null;
    let provinceLoaded = false;

    function notifyButtonState() {
      if (window.updateCheckoutButtonsState) {
        window.updateCheckoutButtonsState();
      }
    }

    function showDropdown() {
      locationField.classList.add("active");
    }

    function hideDropdown() {
      locationField.classList.remove("active");
    }

    function setActiveTab(tabName) {
      const isProvince = tabName === "province";

      provinceTab.classList.toggle("active", isProvince);
      wardTab.classList.toggle("active", !isProvince);

      provinceList.classList.toggle("hidden", !isProvince);
      wardList.classList.toggle("hidden", isProvince);
    }

    function setLoading(container, message) {
      container.innerHTML = "";

      const div = document.createElement("div");
      div.className = "location-loading";
      div.textContent = message;

      container.appendChild(div);
    }

    function setEmpty(container, message) {
      container.innerHTML = "";

      const div = document.createElement("div");
      div.className = "location-empty";
      div.textContent = message;

      container.appendChild(div);
    }

    function createOption(text, onClick) {
      const item = document.createElement("button");
      item.type = "button";
      item.className = "location-option";
      item.textContent = text;
      item.addEventListener("click", onClick);
      return item;
    }

    function normalizeList(data, key) {
      if (!data) return [];

      if (Array.isArray(data)) {
        return data;
      }

      if (Array.isArray(data[key])) {
        return data[key];
      }

      if (data.data && Array.isArray(data.data)) {
        return data.data;
      }

      if (data.data && Array.isArray(data.data[key])) {
        return data.data[key];
      }

      if (data.results && Array.isArray(data.results)) {
        return data.results;
      }

      return [];
    }

    function normalizeProvinceList(data) {
      return normalizeList(data, "provinces")
              .filter(function (province) {
                return province && province.name && province.code;
              });
    }

    function normalizeWardList(data) {
      let wards = normalizeList(data, "wards");

      if (!wards.length && data && Array.isArray(data.wards)) {
        wards = data.wards;
      }

      if (!wards.length && data && data.data && Array.isArray(data.data.wards)) {
        wards = data.data.wards;
      }

      return wards.filter(function (ward) {
        return ward && ward.name && ward.code;
      });
    }

    async function fetchJson(url) {
      const response = await fetch(url, {
        method: "GET",
        headers: {
          "Accept": "application/json"
        }
      });

      if (!response.ok) {
        throw new Error("HTTP " + response.status);
      }

      return response.json();
    }

    async function loadProvinces() {
      if (provinceLoaded) {
        renderProvinces();
        return;
      }

      setLoading(provinceList, "Đang tải danh sách Tỉnh/TP...");

      try {
        let data;

        try {
          data = await fetchJson(API_BASE_URL + "/");
        } catch (firstError) {
          data = await fetchJson(API_BASE_URL + "/p/");
        }

        provinces = normalizeProvinceList(data);

        provinces.sort(function (a, b) {
          return String(a.name || "").localeCompare(String(b.name || ""), "vi");
        });

        provinceLoaded = true;
        renderProvinces();
      } catch (error) {
        console.error("Load provinces failed:", error);
        setEmpty(provinceList, "Không tải được danh sách Tỉnh/TP. Vui lòng thử lại.");
      }
    }

    async function loadWardsByProvince(province) {
      selectedProvince = province;
      selectedWard = null;

      provinceInput.value = province.name || "";
      provinceCodeInput.value = province.code || "";

      wardInput.value = "";
      wardCodeInput.value = "";

      locationInput.value = province.name || "";

      notifyButtonState();

      wardTab.classList.remove("disabled");
      setActiveTab("ward");
      setLoading(wardList, "Đang tải danh sách Phường/Xã...");

      try {
        let data;
        let wards = [];

        try {
          data = await fetchJson(
                  API_BASE_URL + "/p/" + encodeURIComponent(province.code) + "?depth=2"
          );
          wards = normalizeWardList(data);
        } catch (firstError) {
          data = await fetchJson(
                  API_BASE_URL + "/w/?province_code=" + encodeURIComponent(province.code)
          );
          wards = normalizeWardList(data);
        }

        wards.sort(function (a, b) {
          return String(a.name || "").localeCompare(String(b.name || ""), "vi");
        });

        renderWards(wards);
      } catch (error) {
        console.error("Load wards failed:", error);
        setEmpty(wardList, "Không tải được danh sách Phường/Xã của tỉnh này.");
      }
    }

    function renderProvinces() {
      provinceList.innerHTML = "";

      if (!provinces.length) {
        setEmpty(provinceList, "Không có dữ liệu Tỉnh/TP.");
        return;
      }

      provinces.forEach(function (province) {
        const item = createOption(province.name, function () {
          loadWardsByProvince(province);
        });

        if (selectedProvince && selectedProvince.code === province.code) {
          item.classList.add("selected");
        }

        provinceList.appendChild(item);
      });
    }

    function renderWards(wards) {
      wardList.innerHTML = "";

      if (!wards.length) {
        setEmpty(wardList, "Tỉnh/TP này chưa có dữ liệu Phường/Xã.");
        return;
      }

      wards.forEach(function (ward) {
        const item = createOption(ward.name, function () {
          selectedWard = ward;

          wardInput.value = ward.name || "";
          wardCodeInput.value = ward.code || "";

          locationInput.value = ward.name + ", " + selectedProvince.name;

          const locationError = document.getElementById("locationError");
          locationInput.classList.remove("is-invalid");

          if (locationError) {
            locationError.textContent = "";
          }

          updateShippingAddress();
          updateDeliveryText();
          notifyButtonState();

          if (window.updateShippingFeeByLocation) {
            window.updateShippingFeeByLocation();
          }

          hideDropdown();
        });

        if (selectedWard && selectedWard.code === ward.code) {
          item.classList.add("selected");
        }

        wardList.appendChild(item);
      });
    }

    function updateShippingAddress() {
      const address = addressInput ? addressInput.value.trim() : "";
      const province = provinceInput ? provinceInput.value.trim() : "";
      const ward = wardInput ? wardInput.value.trim() : "";

      const parts = [];

      if (address) parts.push(address);
      if (ward) parts.push(ward);
      if (province) parts.push(province);

      if (shippingAddressInput) {
        shippingAddressInput.value = parts.join(", ");
      }
    }

    function updateDeliveryText() {
      if (!deliveryBox) return;

      if (addressInput && addressInput.value.trim() && selectedProvince && selectedWard) {
        deliveryBox.classList.add("available");
      } else {
        deliveryBox.classList.remove("available");
      }

      if (window.updateShippingFeeByLocation) {
        window.updateShippingFeeByLocation();
      }
    }

    locationInput.addEventListener("click", function () {
      showDropdown();

      if (!provinceLoaded) {
        setActiveTab("province");
        loadProvinces();
        return;
      }

      if (selectedProvince) {
        setActiveTab("ward");
      } else {
        setActiveTab("province");
      }
    });

    provinceTab.addEventListener("click", function () {
      showDropdown();
      setActiveTab("province");

      if (!provinceLoaded) {
        loadProvinces();
      }
    });

    wardTab.addEventListener("click", function () {
      showDropdown();

      if (!selectedProvince) {
        setActiveTab("ward");
        setEmpty(wardList, "Vui lòng chọn Tỉnh/TP trước.");
        return;
      }

      setActiveTab("ward");
    });

    if (addressInput) {
      addressInput.addEventListener("input", function () {
        updateShippingAddress();
        updateDeliveryText();
        notifyButtonState();

        if (window.updateShippingFeeByLocation) {
          window.updateShippingFeeByLocation();
        }
      });
    }

    if (checkoutForm) {
      checkoutForm.addEventListener("submit", function () {
        updateShippingAddress();
      });
    }

    document.addEventListener("click", function (event) {
      if (!locationField.contains(event.target)) {
        hideDropdown();
      }
    });

    loadProvinces();
    notifyButtonState();
  })();
</script>
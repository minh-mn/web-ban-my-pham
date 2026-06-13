<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/checkout.css?v=20260604_map_picker_full_fix">
<link rel="stylesheet"
      href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
      integrity="sha256-p4NxAoJBhIINfQh3Hh1q8CgFyuzL4P8rNQ3Drx0Kz5E="
      crossorigin="">

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

      <input type="hidden"
             id="checkoutActionInput"
             name="action"
             value="">

      <input type="hidden"
             id="checkoutRemoveCartKeyInput"
             name="removeCartKey"
             value="">

      <!-- ================= ELECTRONIC INVOICE HIDDEN FIELDS ================= -->
      <input type="hidden" id="needInvoiceInput" name="needInvoice" value="${not empty param.needInvoice ? param.needInvoice : 'false'}">
      <input type="hidden" id="invoiceTypeInput" name="invoiceType" value="${param.invoiceType}">
      <input type="hidden" id="invoiceNameInput" name="invoiceName" value="${param.invoiceName}">
      <input type="hidden" id="invoiceTaxCodeInput" name="invoiceTaxCode" value="${param.invoiceTaxCode}">
      <input type="hidden" id="invoiceBuyerNameInput" name="invoiceBuyerName" value="${param.invoiceBuyerName}">
      <input type="hidden" id="invoiceCitizenIdInput" name="invoiceCitizenId" value="${param.invoiceCitizenId}">
      <input type="hidden" id="invoicePassportInput" name="invoicePassport" value="${param.invoicePassport}">
      <input type="hidden" id="invoiceEmailInput" name="invoiceEmail" value="${param.invoiceEmail}">
      <input type="hidden" id="invoiceAddressInput" name="invoiceAddress" value="${param.invoiceAddress}">
      <input type="hidden" id="invoiceBudgetCodeInput" name="invoiceBudgetCode" value="${param.invoiceBudgetCode}">
      <input type="hidden" id="saveInvoiceInfoInput" name="saveInvoiceInfo" value="${not empty param.saveInvoiceInfo ? param.saveInvoiceInfo : 'false'}">


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
            <label for="address">Địa chỉ cụ thể</label>

            <input type="text"
                   id="address"
                   name="address"
                   value="${not empty requestScope.formAddress ? requestScope.formAddress : param.address}"
                   placeholder="Ví dụ: Số nhà, hẻm, tổ/khu phố/ấp, tên đường hoặc khu vực"
                   class="${not empty errors.address ? 'is-invalid' : ''}"
                   autocomplete="street-address">

            <button type="button"
                    id="useCurrentLocationBtn"
                    class="btn-use-location">
              <span class="btn-use-location__icon">📍</span>
              <span class="btn-use-location__text">Dùng vị trí hiện tại</span>
            </button>

            <div class="field-hint" id="addressHint">
              Nhập rõ số nhà, hẻm, tổ/khu phố/ấp/xã, tên đường hoặc khu vực giao hàng.
            </div>

            <div class="location-detected-hint" id="detectedAddressHint"></div>

            <input type="hidden"
                   id="latitudeInput"
                   name="latitude"
                   value="${not empty requestScope.formLatitude ? requestScope.formLatitude : param.latitude}">

            <input type="hidden"
                   id="longitudeInput"
                   name="longitude"
                   value="${not empty requestScope.formLongitude ? requestScope.formLongitude : param.longitude}">

            <input type="hidden"
                   id="detectedProvinceInput"
                   name="detectedProvince"
                   value="${not empty requestScope.formDetectedProvince ? requestScope.formDetectedProvince : param.detectedProvince}">

            <input type="hidden"
                   id="detectedAddressInput"
                   name="detectedAddress"
                   value="${not empty requestScope.formDetectedAddress ? requestScope.formDetectedAddress : param.detectedAddress}">

            <input type="hidden"
                   id="mapConfirmedInput"
                   name="mapConfirmed"
                   value="${not empty requestScope.formMapConfirmed ? requestScope.formMapConfirmed : param.mapConfirmed}">

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

          <div class="field-error" id="shippingMethodError">
            <c:if test="${not empty errors.shippingMethod}">
              <c:out value="${errors.shippingMethod}" />
            </c:if>
          </div>
        </div>

        <!-- PAYMENT METHOD -->
        <div class="checkout-card payment-card">
          <div class="checkout-card-header payment-card-header">
            <div>
              <h2>Phương thức thanh toán</h2>
              <p class="payment-support-note">Hỗ trợ thanh toán khi nhận hàng (COD) và cổng thanh toán trực tuyến VNPAY của website.</p>
            </div>
          </div>

          <div class="payment-supported-logos payment-supported-logos-supported" aria-label="Các phương thức thanh toán được hỗ trợ">
            <span class="payment-supported-logo payment-supported-logo-single" title="VNPAY">
              <img src="${pageContext.request.contextPath}/assets/images/payment/vnpay.svg" alt="VNPAY">
            </span>
            <span class="payment-supported-logo payment-supported-logo-cod" title="COD">
              <img src="${pageContext.request.contextPath}/assets/images/payment/cod.svg" alt="COD">
            </span>
          </div>

          <div class="payment-list ${not empty errors.paymentMethod ? 'is-invalid' : ''}"
               id="paymentList">

            <label class="payment-item">
              <input type="radio"
                     name="paymentMethod"
                     value="COD"
              ${empty param.paymentMethod || param.paymentMethod == 'COD' ? 'checked' : ''}>

              <span class="payment-dot"></span>

              <span class="payment-icon payment-icon-logo payment-icon-cod">
                <img src="${pageContext.request.contextPath}/assets/images/payment/cod.svg" alt="COD">
              </span>

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

              <span class="payment-icon payment-icon-logo payment-icon-vnpay">
                <img src="${pageContext.request.contextPath}/assets/images/payment/vnpay.svg" alt="VNPAY">
              </span>

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


        <!-- ELECTRONIC INVOICE REQUEST -->
        <div class="checkout-card e-invoice-card">
          <div class="e-invoice-header-line">
            <div class="e-invoice-info">
              <div class="e-invoice-icon">🧾</div>

              <div>
                <h2>Xuất hóa đơn điện tử</h2>
                <p>Yêu cầu xuất hóa đơn VAT bản PDF và gửi về email sau khi đặt hàng.</p>
              </div>
            </div>

            <button type="button"
                    class="e-invoice-open-btn"
                    id="openEInvoiceModalBtn">
              Xuất hóa đơn VAT
            </button>
          </div>

          <div class="e-invoice-selected-box" id="eInvoiceSelectedBox">
            <strong>Đã yêu cầu xuất hóa đơn.</strong>
            <span id="eInvoiceSelectedText"></span>
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

                      <button type="button"
                              class="checkout-remove js-checkout-remove"
                              title="Xóa sản phẩm"
                              data-cart-key="${fn:escapeXml(cartKey)}"
                              data-checkout-action="remove-item">
                        🗑
                      </button>
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
            <span>🎟 <span class="coupon-open-text">Chọn mã</span></span>
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

          <c:if test="${not empty coupon_success}">
            <div class="coupon-message success">
              <c:out value="${coupon_success}" />
            </div>
          </c:if>

          <c:if test="${not empty coupon_error}">
            <div class="coupon-message error">
              <c:out value="${coupon_error}" />
            </div>
          </c:if>
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

  <div class="coupon-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="couponModalTitle">
    <div class="coupon-modal-header">
      <h3 id="couponModalTitle">Chọn mã khuyến mãi</h3>

      <button type="button"
              class="coupon-modal-close"
              data-close-coupon-modal
              aria-label="Đóng cửa sổ chọn mã">
      </button>
    </div>

    <div class="coupon-modal-body">
      <div class="coupon-list-title">Mã giảm giá có thể áp dụng cho đơn hàng</div>

      <c:choose>
        <c:when test="${not empty couponOptions}">
          <c:set var="modalCoupons" value="${couponOptions}" />
        </c:when>
        <c:when test="${not empty checkoutCoupons}">
          <c:set var="modalCoupons" value="${checkoutCoupons}" />
        </c:when>
        <c:when test="${not empty allCoupons}">
          <c:set var="modalCoupons" value="${allCoupons}" />
        </c:when>
        <c:otherwise>
          <c:set var="modalCoupons" value="${availableCoupons}" />
        </c:otherwise>
      </c:choose>

      <div class="coupon-list" id="checkoutCouponList">

        <c:choose>
          <c:when test="${not empty modalCoupons}">
            <c:forEach var="coupon" items="${modalCoupons}">

              <c:set var="serverEstimatedDiscount"
                     value="${not empty couponEstimatedDiscountMap ? couponEstimatedDiscountMap[coupon.code] : 0}" />

              <c:set var="serverUsable"
                     value="${not empty couponUsableMap ? couponUsableMap[coupon.code] : true}" />

              <c:set var="serverRankEligible"
                     value="${not empty couponRankEligibleMap ? couponRankEligibleMap[coupon.code] : true}" />

              <c:set var="serverDisabledReason"
                     value="${not empty couponDisabledReasonMap ? couponDisabledReasonMap[coupon.code] : ''}" />

              <button type="button"
                      class="coupon-item js-select-coupon ${serverUsable ? 'is-usable' : 'is-disabled'} ${coupon.code == bestCouponCode ? 'is-best' : ''}"
                      data-code="${fn:escapeXml(coupon.code)}"
                      data-percent="${coupon.discountPercent}"
                      data-max-discount="${empty coupon.maxDiscountAmount ? 0 : coupon.maxDiscountAmount}"
                      data-min-order="${empty coupon.minOrderAmount ? 0 : coupon.minOrderAmount}"
                      data-min-rank="${fn:escapeXml(coupon.minRankCode)}"
                      data-active="${coupon.active}"
                      data-used-count="${coupon.usedCount}"
                      data-max-uses="${coupon.maxUses}"
                      data-end-date="${coupon.endDate}"
                      data-server-estimated-discount="${empty serverEstimatedDiscount ? 0 : serverEstimatedDiscount}"
                      data-server-usable="${serverUsable}"
                      data-rank-eligible="${serverRankEligible}"
                      data-disabled-reason="${fn:escapeXml(serverDisabledReason)}">

                <span class="coupon-best-badge">Tốt nhất</span>

                <div class="coupon-voucher-icon" aria-hidden="true">★</div>

                <div class="coupon-voucher-content">
                  <div class="coupon-discount-label">Giảm ${coupon.discountPercent}%</div>

                  <div class="coupon-title-line">
                    <c:choose>
                      <c:when test="${not empty coupon.maxDiscountAmount and coupon.maxDiscountAmount > 0}">
                        Giảm tối đa
                        <fmt:formatNumber value="${coupon.maxDiscountAmount}"
                                          type="number"
                                          groupingUsed="true" />đ
                      </c:when>
                      <c:otherwise>
                        Giảm ${coupon.discountPercent}% cho đơn hàng
                      </c:otherwise>
                    </c:choose>
                  </div>

                  <div class="coupon-condition">
                    <c:choose>
                      <c:when test="${not empty coupon.minOrderAmount and coupon.minOrderAmount > 0}">
                        Đơn tối thiểu
                        <fmt:formatNumber value="${coupon.minOrderAmount}"
                                          type="number"
                                          groupingUsed="true" />đ
                      </c:when>
                      <c:otherwise>
                        Áp dụng cho mọi đơn hàng đủ điều kiện
                      </c:otherwise>
                    </c:choose>
                  </div>

                  <div class="coupon-meta-line">
                    <span>Mã:</span>
                    <span class="coupon-meta-code"><c:out value="${coupon.code}" /></span>
                    <span>•</span>
                    <span class="coupon-detail-link">Điều kiện</span>
                  </div>

                  <div class="coupon-disabled-reason"></div>
                </div>

                <div class="coupon-ticket-right" aria-hidden="true"></div>
              </button>
            </c:forEach>
          </c:when>

          <c:otherwise>
            <div class="coupon-empty">
              <div class="coupon-empty-icon">🏷️</div>
              <p>Không có mã khuyến mãi phù hợp</p>
            </div>
          </c:otherwise>
        </c:choose>

      </div>
    </div>

    <div class="coupon-modal-footer">
      <div class="coupon-selected-count" id="couponSelectedCount">
        Chưa chọn mã giảm giá
      </div>

      <button type="button"
              class="coupon-confirm-btn"
              id="confirmCouponSelection">
        Đồng ý
      </button>
    </div>
  </div>
</div>



<!-- ================= ELECTRONIC INVOICE MODAL ================= -->
<div class="e-invoice-modal" id="eInvoiceModal" aria-hidden="true">
  <div class="e-invoice-backdrop" data-close-e-invoice></div>

  <div class="e-invoice-dialog" role="dialog" aria-modal="true" aria-labelledby="eInvoiceModalTitle">
    <div class="e-invoice-modal-header">
      <button type="button"
              class="e-invoice-close"
              data-close-e-invoice
              aria-label="Đóng cửa sổ xuất hóa đơn">
        <span class="e-invoice-close-icon" aria-hidden="true">×</span>
      </button>

      <h3 id="eInvoiceModalTitle">Xuất hóa đơn VAT</h3>
    </div>

    <div class="e-invoice-modal-body">
      <div class="e-invoice-type-row">
        <label class="e-invoice-radio">
          <input type="radio" name="modalInvoiceType" value="PERSONAL" checked>
          <span>Cá nhân</span>
        </label>

        <label class="e-invoice-radio">
          <input type="radio" name="modalInvoiceType" value="COMPANY">
          <span>Công ty</span>
        </label>
      </div>

      <div class="e-invoice-form" id="personalInvoiceForm">
        <input type="text"
               class="e-invoice-input"
               id="personalInvoiceName"
               placeholder="Họ và tên">

        <div class="e-invoice-grid-2">
          <input type="text"
                 class="e-invoice-input"
                 id="personalInvoiceCitizenId"
                 placeholder="CCCD (không bắt buộc)"
                 inputmode="numeric">

          <input type="text"
                 class="e-invoice-input"
                 id="personalInvoicePassport"
                 placeholder="Hộ chiếu (không bắt buộc)">
        </div>
      </div>

      <div class="e-invoice-form is-hidden" id="companyInvoiceForm">
        <div class="e-invoice-grid-2">
          <input type="text"
                 class="e-invoice-input"
                 id="companyInvoiceName"
                 placeholder="Nhập tên công ty">

          <input type="text"
                 class="e-invoice-input"
                 id="companyInvoiceTaxCode"
                 placeholder="MST/CCCD"
                 inputmode="numeric">
        </div>

        <div class="e-invoice-grid-2">
          <input type="text"
                 class="e-invoice-input"
                 id="companyInvoiceBuyerName"
                 placeholder="Tên người mua (không bắt buộc)">

          <input type="text"
                 class="e-invoice-input"
                 id="companyInvoiceBudgetCode"
                 placeholder="Mã ĐVQHNS (không bắt buộc)">
        </div>
      </div>

      <div class="e-invoice-form e-invoice-form-spacing">
        <input type="email"
               class="e-invoice-input"
               id="modalInvoiceEmail"
               placeholder="E-mail nhận hóa đơn">

        <input type="text"
               class="e-invoice-input"
               id="modalInvoiceAddress"
               placeholder="Địa chỉ xuất hóa đơn">

        <button type="button"
                class="e-invoice-use-address"
                id="useShippingAddressForInvoiceBtn">
          Dùng địa chỉ giao hàng
        </button>

        <label class="e-invoice-save-row">
          <input type="checkbox" id="modalSaveInvoiceInfo">
          <span>Lưu thông tin xuất hóa đơn làm mặc định</span>
        </label>

        <div class="e-invoice-note">
          <div class="e-invoice-note-icon">ⓘ</div>
          <div>
            <strong>Lưu ý</strong>
            <ul>
              <li>Nhà bán hàng hỗ trợ xuất hóa đơn một lần sau khi đơn hàng được xác nhận.</li>
              <li>MST/CCCD hợp lệ thường gồm 10 hoặc 12 chữ số.</li>
              <li>Hóa đơn VAT bản PDF sẽ được gửi về email sau khi đơn hàng được tạo thành công.</li>
            </ul>
          </div>
        </div>

        <div class="e-invoice-error" id="eInvoiceError"></div>
      </div>
    </div>

    <div class="e-invoice-modal-footer">
      <button type="button"
              class="e-invoice-submit"
              id="saveEInvoiceRequestBtn">
        Gửi yêu cầu
      </button>
    </div>
  </div>
</div>

<!-- ================= ELECTRONIC INVOICE SCRIPT ================= -->
<script>
  (function () {
    const modal = document.getElementById("eInvoiceModal");
    const openBtn = document.getElementById("openEInvoiceModalBtn");
    const saveBtn = document.getElementById("saveEInvoiceRequestBtn");
    const errorBox = document.getElementById("eInvoiceError");

    const personalForm = document.getElementById("personalInvoiceForm");
    const companyForm = document.getElementById("companyInvoiceForm");

    const selectedBox = document.getElementById("eInvoiceSelectedBox");
    const selectedText = document.getElementById("eInvoiceSelectedText");

    const needInvoiceInput = document.getElementById("needInvoiceInput");
    const invoiceTypeInput = document.getElementById("invoiceTypeInput");
    const invoiceNameInput = document.getElementById("invoiceNameInput");
    const invoiceTaxCodeInput = document.getElementById("invoiceTaxCodeInput");
    const invoiceBuyerNameInput = document.getElementById("invoiceBuyerNameInput");
    const invoiceCitizenIdInput = document.getElementById("invoiceCitizenIdInput");
    const invoicePassportInput = document.getElementById("invoicePassportInput");
    const invoiceEmailInput = document.getElementById("invoiceEmailInput");
    const invoiceAddressInput = document.getElementById("invoiceAddressInput");
    const invoiceBudgetCodeInput = document.getElementById("invoiceBudgetCodeInput");
    const saveInvoiceInfoInput = document.getElementById("saveInvoiceInfoInput");

    if (!modal || !openBtn || !saveBtn) {
      return;
    }

    function valueOf(id) {
      const el = document.getElementById(id);
      return el ? el.value.trim() : "";
    }

    function setValue(id, value) {
      const el = document.getElementById(id);
      if (el) {
        el.value = value || "";
      }
    }

    function setHiddenValue(input, value) {
      if (input) {
        input.value = value || "";
      }
    }

    function clearInvalidInputs() {
      modal.querySelectorAll(".e-invoice-input").forEach(function (input) {
        input.classList.remove("is-invalid");
      });
    }

    function showError(message, inputId) {
      if (errorBox) {
        errorBox.textContent = message || "";
        errorBox.classList.toggle("show", !!message);
      }

      clearInvalidInputs();

      if (inputId) {
        const input = document.getElementById(inputId);
        if (input) {
          input.classList.add("is-invalid");
          input.focus();
        }
      }
    }

    function getSelectedType() {
      const checked = document.querySelector("input[name='modalInvoiceType']:checked");
      return checked ? checked.value : "PERSONAL";
    }

    function syncTypeUI() {
      const type = getSelectedType();

      if (type === "COMPANY") {
        if (personalForm) personalForm.classList.add("is-hidden");
        if (companyForm) companyForm.classList.remove("is-hidden");
      } else {
        if (personalForm) personalForm.classList.remove("is-hidden");
        if (companyForm) companyForm.classList.add("is-hidden");
      }

      showError("");
    }

    function openModal() {
      modal.classList.add("show");
      modal.setAttribute("aria-hidden", "false");
      document.body.classList.add("e-invoice-modal-open");
      syncTypeUI();
    }

    function closeModal() {
      modal.classList.remove("show");
      modal.setAttribute("aria-hidden", "true");
      document.body.classList.remove("e-invoice-modal-open");
      showError("");
    }

    function getShippingAddress() {
      const shippingAddressInput = document.getElementById("shippingAddressInput");
      const addressInput = document.getElementById("address");
      const wardInput = document.getElementById("wardInput");
      const provinceInput = document.getElementById("provinceInput");

      if (shippingAddressInput && shippingAddressInput.value.trim()) {
        return shippingAddressInput.value.trim();
      }

      const parts = [];

      if (addressInput && addressInput.value.trim()) {
        parts.push(addressInput.value.trim());
      }

      if (wardInput && wardInput.value.trim()) {
        parts.push(wardInput.value.trim());
      }

      if (provinceInput && provinceInput.value.trim()) {
        parts.push(provinceInput.value.trim());
      }

      return parts.join(", ");
    }

    function getDefaultEmail() {
      const accountEmailText = document.querySelector(".account-info span");
      return accountEmailText ? accountEmailText.textContent.trim() : "";
    }

    function isValidEmail(email) {
      return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(email || "").trim());
    }

    function isValidTaxLikeCode(value) {
      const normalized = String(value || "").replace(/\s+/g, "");
      return /^\d{10}$/.test(normalized)
              || /^\d{12}$/.test(normalized)
              || /^\d{13}-\d$/.test(normalized);
    }

    function validate(type) {
      const email = valueOf("modalInvoiceEmail");
      const address = valueOf("modalInvoiceAddress");

      if (type === "COMPANY") {
        const companyName = valueOf("companyInvoiceName");
        const taxCode = valueOf("companyInvoiceTaxCode");

        if (!companyName) {
          showError("Vui lòng nhập tên công ty.", "companyInvoiceName");
          return false;
        }

        if (!taxCode) {
          showError("Vui lòng nhập mã số thuế hoặc CCCD.", "companyInvoiceTaxCode");
          return false;
        }

        if (!isValidTaxLikeCode(taxCode)) {
          showError("MST/CCCD phải gồm 10 hoặc 12 số. MST 14 ký tự có dạng 13 số và dấu '-'.", "companyInvoiceTaxCode");
          return false;
        }
      } else {
        const personalName = valueOf("personalInvoiceName");
        const citizenId = valueOf("personalInvoiceCitizenId");

        if (!personalName) {
          showError("Vui lòng nhập họ và tên.", "personalInvoiceName");
          return false;
        }

        if (citizenId && !/^\d{10,12}$/.test(citizenId)) {
          showError("CCCD cá nhân nếu nhập phải gồm 10 đến 12 chữ số.", "personalInvoiceCitizenId");
          return false;
        }
      }

      if (!email) {
        showError("Vui lòng nhập email nhận hóa đơn.", "modalInvoiceEmail");
        return false;
      }

      if (!isValidEmail(email)) {
        showError("Email nhận hóa đơn không hợp lệ.", "modalInvoiceEmail");
        return false;
      }

      if (!address) {
        showError("Vui lòng nhập địa chỉ xuất hóa đơn.", "modalInvoiceAddress");
        return false;
      }

      if (address.length < 6) {
        showError("Địa chỉ xuất hóa đơn quá ngắn.", "modalInvoiceAddress");
        return false;
      }

      showError("");
      return true;
    }

    function updateSelectedSummary(type, name, email) {
      if (selectedBox) {
        selectedBox.classList.add("show");
      }

      if (selectedText) {
        selectedText.textContent = " Loại: "
                + (type === "COMPANY" ? "Công ty" : "Cá nhân")
                + " • Tên: "
                + name
                + " • Email: "
                + email;
      }

      openBtn.textContent = "Sửa thông tin hóa đơn";
      openBtn.classList.add("is-selected");
    }

    function restoreModalFromHidden() {
      const type = invoiceTypeInput && invoiceTypeInput.value ? invoiceTypeInput.value : "PERSONAL";

      document.querySelectorAll("input[name='modalInvoiceType']").forEach(function (radio) {
        radio.checked = radio.value === type;
      });

      if (type === "COMPANY") {
        setValue("companyInvoiceName", invoiceNameInput ? invoiceNameInput.value : "");
        setValue("companyInvoiceTaxCode", invoiceTaxCodeInput ? invoiceTaxCodeInput.value : "");
        setValue("companyInvoiceBuyerName", invoiceBuyerNameInput ? invoiceBuyerNameInput.value : "");
        setValue("companyInvoiceBudgetCode", invoiceBudgetCodeInput ? invoiceBudgetCodeInput.value : "");
      } else {
        setValue("personalInvoiceName", invoiceNameInput ? invoiceNameInput.value : "");
        setValue("personalInvoiceCitizenId", invoiceCitizenIdInput ? invoiceCitizenIdInput.value : "");
        setValue("personalInvoicePassport", invoicePassportInput ? invoicePassportInput.value : "");
      }

      setValue("modalInvoiceEmail", invoiceEmailInput && invoiceEmailInput.value ? invoiceEmailInput.value : getDefaultEmail());
      setValue("modalInvoiceAddress", invoiceAddressInput ? invoiceAddressInput.value : "");

      const saveCheckbox = document.getElementById("modalSaveInvoiceInfo");
      if (saveCheckbox && saveInvoiceInfoInput) {
        saveCheckbox.checked = saveInvoiceInfoInput.value === "true";
      }

      syncTypeUI();
    }

    document.querySelectorAll("input[name='modalInvoiceType']").forEach(function (radio) {
      radio.addEventListener("change", syncTypeUI);
    });

    openBtn.addEventListener("click", function () {
      restoreModalFromHidden();
      openModal();
    });

    document.querySelectorAll("[data-close-e-invoice]").forEach(function (el) {
      el.addEventListener("click", closeModal);
    });

    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape" && modal.classList.contains("show")) {
        closeModal();
      }
    });

    const useAddressBtn = document.getElementById("useShippingAddressForInvoiceBtn");
    if (useAddressBtn) {
      useAddressBtn.addEventListener("click", function () {
        const address = getShippingAddress();

        if (!address) {
          showError("Chưa có địa chỉ giao hàng để sử dụng.", "modalInvoiceAddress");
          return;
        }

        setValue("modalInvoiceAddress", address);
        showError("");
      });
    }

    saveBtn.addEventListener("click", function () {
      const type = getSelectedType();

      if (!validate(type)) {
        return;
      }

      const email = valueOf("modalInvoiceEmail");
      const address = valueOf("modalInvoiceAddress");
      const saveCheckbox = document.getElementById("modalSaveInvoiceInfo");

      let invoiceName = "";
      let taxCode = "";
      let buyerName = "";
      let citizenId = "";
      let passport = "";
      let budgetCode = "";

      if (type === "COMPANY") {
        invoiceName = valueOf("companyInvoiceName");
        taxCode = valueOf("companyInvoiceTaxCode");
        buyerName = valueOf("companyInvoiceBuyerName");
        budgetCode = valueOf("companyInvoiceBudgetCode");
      } else {
        invoiceName = valueOf("personalInvoiceName");
        citizenId = valueOf("personalInvoiceCitizenId");
        passport = valueOf("personalInvoicePassport");
      }

      setHiddenValue(needInvoiceInput, "true");
      setHiddenValue(invoiceTypeInput, type);
      setHiddenValue(invoiceNameInput, invoiceName);
      setHiddenValue(invoiceTaxCodeInput, taxCode);
      setHiddenValue(invoiceBuyerNameInput, buyerName);
      setHiddenValue(invoiceCitizenIdInput, citizenId);
      setHiddenValue(invoicePassportInput, passport);
      setHiddenValue(invoiceEmailInput, email);
      setHiddenValue(invoiceAddressInput, address);
      setHiddenValue(invoiceBudgetCodeInput, budgetCode);
      setHiddenValue(saveInvoiceInfoInput, saveCheckbox && saveCheckbox.checked ? "true" : "false");

      updateSelectedSummary(type, invoiceName, email);
      closeModal();
    });

    if (needInvoiceInput && needInvoiceInput.value === "true" && invoiceNameInput && invoiceNameInput.value) {
      updateSelectedSummary(invoiceTypeInput ? invoiceTypeInput.value : "PERSONAL",
              invoiceNameInput.value,
              invoiceEmailInput ? invoiceEmailInput.value : "");
    }
  })();
</script>


<!-- ================= SYSTEM COUPON CODES FOR MANUAL INPUT ================= -->
<script>
  window.SYSTEM_COUPON_CODES = new Set();

  (function () {
    function addCouponCode(code) {
      const normalized = String(code || "").trim().toUpperCase();

      if (normalized) {
        window.SYSTEM_COUPON_CODES.add(normalized);
      }
    }

    <c:forEach var="coupon" items="${allCoupons}">
    addCouponCode("${fn:escapeXml(coupon.code)}");
    </c:forEach>

    <c:forEach var="coupon" items="${checkoutCoupons}">
    addCouponCode("${fn:escapeXml(coupon.code)}");
    </c:forEach>

    <c:forEach var="coupon" items="${couponOptions}">
    addCouponCode("${fn:escapeXml(coupon.code)}");
    </c:forEach>

    <c:forEach var="coupon" items="${savedCoupons}">
    addCouponCode("${fn:escapeXml(coupon.code)}");
    </c:forEach>

    <c:forEach var="coupon" items="${availableCoupons}">
    addCouponCode("${fn:escapeXml(coupon.code)}");
    </c:forEach>
  })();
</script>

<!-- ================= COUPON MODAL SCRIPT ================= -->
<script>
  (function () {
    const modal = document.getElementById("couponModal");
    const openBtn = document.getElementById("openCouponModal");
    const couponInput = document.getElementById("couponCode");
    const confirmBtn = document.getElementById("confirmCouponSelection");
    const selectedCountEl = document.getElementById("couponSelectedCount");
    const couponList = document.getElementById("checkoutCouponList");

    let selectedCode = couponInput && couponInput.value ? normalizeCode(couponInput.value) : "";
    let couponItems = [];

    function normalizeCode(code) {
      return String(code || "").trim().toUpperCase();
    }

    function parseNumber(value) {
      if (value === null || value === undefined || value === "") {
        return 0;
      }

      const normalized = String(value).replace(/[^0-9.-]/g, "");
      const number = Number(normalized);

      return Number.isFinite(number) ? number : 0;
    }

    function parseVndText(text) {
      if (!text) return 0;
      return Number(String(text).replace(/[^0-9]/g, "")) || 0;
    }

    function formatVnd(value) {
      return new Intl.NumberFormat("vi-VN").format(Math.round(Number(value || 0))) + "đ";
    }

    function getCurrentSubtotal() {
      const summarySubtotal = document.getElementById("summarySubtotal");

      if (summarySubtotal) {
        return parseVndText(summarySubtotal.textContent);
      }

      let subtotal = 0;

      document.querySelectorAll(".js-item-subtotal").forEach(function (el) {
        subtotal += parseNumber(el.dataset.raw);
      });

      return subtotal;
    }

    function isActiveValue(value) {
      const normalized = String(value || "").trim().toLowerCase();
      return normalized === "true" || normalized === "1" || normalized === "yes";
    }

    function isExpired(endDateRaw) {
      const value = String(endDateRaw || "").trim();

      if (!value || value === "null" || value === "undefined") {
        return false;
      }

      const date = new Date(value + "T23:59:59");

      if (Number.isNaN(date.getTime())) {
        return false;
      }

      return date.getTime() < Date.now();
    }

    function estimateDiscount(item, subtotal) {
      const percent = parseNumber(item.dataset.percent);
      const maxDiscount = parseNumber(item.dataset.maxDiscount);
      const minOrder = parseNumber(item.dataset.minOrder);

      if (subtotal < minOrder || percent <= 0) {
        return 0;
      }

      const rawDiscount = subtotal * percent / 100;

      if (maxDiscount > 0) {
        return Math.min(rawDiscount, maxDiscount);
      }

      return rawDiscount;
    }

    function getDisabledReason(item, subtotal) {
      const rankEligible = String(item.dataset.rankEligible || "true").toLowerCase() === "true";
      const serverReason = String(item.dataset.disabledReason || "").trim();
      const serverUsable = String(item.dataset.serverUsable || "true").toLowerCase() === "true";

      /*
       * Backend biết chính xác user hiện tại có đủ hạng hay không.
       * Nếu backend báo không đủ hạng thì frontend phải khóa mã luôn,
       * không được cho chọn và không được xét là "Tốt nhất".
       */
      if (!rankEligible) {
        return serverReason || "Hạng thành viên hiện tại chưa phù hợp với mã này.";
      }

      const minOrder = parseNumber(item.dataset.minOrder);
      const usedCount = parseNumber(item.dataset.usedCount);
      const maxUses = parseNumber(item.dataset.maxUses);

      if (!isActiveValue(item.dataset.active)) {
        return serverReason || "Mã hiện không còn hoạt động";
      }

      if (isExpired(item.dataset.endDate)) {
        return serverReason || "Mã đã hết hạn sử dụng";
      }

      if (maxUses > 0 && usedCount >= maxUses) {
        return serverReason || "Mã đã hết lượt sử dụng";
      }

      /*
       * Trường hợp serverUsable=false vì subtotal lúc render chưa đủ,
       * frontend vẫn được tính lại theo subtotal hiện tại khi user đổi số lượng.
       */
      if (!serverUsable && serverReason && !serverReason.toLowerCase().includes("tối thiểu")) {
        return serverReason;
      }

      if (subtotal < minOrder) {
        return "Cần mua thêm " + formatVnd(minOrder - subtotal) + " để dùng mã này";
      }

      return "";
    }

    function isUsable(item, subtotal) {
      return getDisabledReason(item, subtotal) === "";
    }

    function removeDuplicateCoupons() {
      if (!couponList) return;

      const seen = new Set();

      Array.from(couponList.querySelectorAll(".js-select-coupon")).forEach(function (item) {
        const code = normalizeCode(item.dataset.code);

        if (!code) {
          item.remove();
          return;
        }

        if (seen.has(code)) {
          item.remove();
          return;
        }

        seen.add(code);
      });
    }

    function getCouponItems() {
      return Array.from(document.querySelectorAll(".js-select-coupon"));
    }

    function clearBestBadges() {
      couponItems.forEach(function (item) {
        item.classList.remove("is-best");
      });
    }

    function markBestCoupon(usableItems, subtotal) {
      clearBestBadges();

      if (!usableItems.length) {
        return;
      }

      let bestItem = null;
      let bestDiscount = -1;
      let bestMinOrder = Number.MAX_SAFE_INTEGER;

      usableItems.forEach(function (item) {
        const discount = estimateDiscount(item, subtotal);
        const minOrder = parseNumber(item.dataset.minOrder);

        if (discount > bestDiscount || (discount === bestDiscount && minOrder < bestMinOrder)) {
          bestDiscount = discount;
          bestMinOrder = minOrder;
          bestItem = item;
        }
      });

      if (bestItem && bestDiscount > 0) {
        bestItem.classList.add("is-best");
      }
    }

    function sortCouponItemsByState(subtotal) {
      if (!couponList || !couponItems.length) {
        return;
      }

      couponItems.sort(function (a, b) {
        const usableA = isUsable(a, subtotal);
        const usableB = isUsable(b, subtotal);

        if (usableA !== usableB) {
          return usableA ? -1 : 1;
        }

        const discountA = usableA ? estimateDiscount(a, subtotal) : 0;
        const discountB = usableB ? estimateDiscount(b, subtotal) : 0;

        if (discountA !== discountB) {
          return discountB - discountA;
        }

        const minOrderA = parseNumber(a.dataset.minOrder);
        const minOrderB = parseNumber(b.dataset.minOrder);

        if (minOrderA !== minOrderB) {
          return minOrderA - minOrderB;
        }

        const codeA = normalizeCode(a.dataset.code);
        const codeB = normalizeCode(b.dataset.code);

        return codeA.localeCompare(codeB, "vi");
      });

      couponItems.forEach(function (item) {
        couponList.appendChild(item);
      });
    }

    function refreshCouponStates() {
      const subtotal = getCurrentSubtotal();
      const usableItems = [];
      let selectedStillValid = false;

      couponItems.forEach(function (item) {
        const code = normalizeCode(item.dataset.code);
        const reason = getDisabledReason(item, subtotal);
        const reasonEl = item.querySelector(".coupon-disabled-reason");
        const usable = reason === "";

        item.classList.remove("is-usable", "is-disabled", "is-selected", "is-best");

        if (usable) {
          item.classList.add("is-usable");
          item.disabled = false;
          usableItems.push(item);

          if (selectedCode && code === selectedCode) {
            selectedStillValid = true;
          }
        } else {
          item.classList.add("is-disabled");
          item.disabled = true;
        }

        if (reasonEl) {
          reasonEl.textContent = reason;
        }
      });

      if (selectedCode && !selectedStillValid) {
        selectedCode = "";
      }

      sortCouponItemsByState(subtotal);
      markBestCoupon(usableItems, subtotal);
      updateSelectedState();
    }

    function updateSelectedState() {
      let selectedCount = 0;

      couponItems.forEach(function (item) {
        const code = normalizeCode(item.dataset.code);
        const selected = selectedCode && code === selectedCode && item.classList.contains("is-usable");

        item.classList.toggle("is-selected", !!selected);

        if (selected) {
          selectedCount += 1;
        }
      });

      if (selectedCountEl) {
        if (selectedCode && selectedCount > 0) {
          selectedCountEl.innerHTML = "Đã chọn <strong>1</strong> mã giảm giá";
        } else {
          selectedCountEl.textContent = "Chưa chọn mã giảm giá";
        }
      }
    }

    let confirmedPickerCode = "";
    let settingCouponFromPicker = false;

    function findCouponItemByCode(code) {
      const normalizedCode = normalizeCode(code);

      if (!normalizedCode) {
        return null;
      }

      return couponItems.find(function (item) {
        return normalizeCode(item.dataset.code) === normalizedCode;
      }) || null;
    }

    function updateOpenButtonText() {
      if (!openBtn) return;

      const textEl = openBtn.querySelector(".coupon-open-text");
      const code = normalizeCode(confirmedPickerCode);

      openBtn.classList.toggle("has-selected", !!code);

      if (textEl) {
        textEl.textContent = code ? code : "Chọn mã";
      }
    }

    function setPickerAppliedCode(code) {
      confirmedPickerCode = normalizeCode(code);
      updateOpenButtonText();
    }

    window.setAppliedCouponCodeForPicker = setPickerAppliedCode;

    window.clearAppliedCouponPicker = function () {
      confirmedPickerCode = "";
      updateOpenButtonText();
    };

    window.refreshCheckoutCouponStates = function () {
      refreshCouponStates();
    };

    function closeModal() {
      if (!modal) return;

      modal.classList.remove("show");
      document.body.classList.remove("coupon-modal-open");
    }

    function openModal() {
      if (!modal) return;

      selectedCode = confirmedPickerCode ? normalizeCode(confirmedPickerCode) : "";
      refreshCouponStates();

      modal.classList.add("show");
      document.body.classList.add("coupon-modal-open");
    }

    function bindCouponEvents() {
      couponItems.forEach(function (item) {
        item.addEventListener("click", function () {
          if (item.classList.contains("is-disabled")) {
            return;
          }

          const code = normalizeCode(item.dataset.code);

          if (selectedCode === code) {
            selectedCode = "";
          } else {
            selectedCode = code;
          }

          updateSelectedState();
        });
      });
    }

    function confirmSelection() {
      if (!couponInput) return;

      confirmedPickerCode = selectedCode || "";
      settingCouponFromPicker = true;
      couponInput.value = confirmedPickerCode;
      couponInput.dispatchEvent(new Event("input", { bubbles: true }));
      settingCouponFromPicker = false;

      if (window.updateCheckoutButtonsState) {
        window.updateCheckoutButtonsState();
      }

      updateOpenButtonText();
      closeModal();
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

    if (confirmBtn) {
      confirmBtn.addEventListener("click", confirmSelection);
    }

    if (couponInput) {
      couponInput.addEventListener("input", function () {
        if (!settingCouponFromPicker) {
          confirmedPickerCode = "";
        }

        updateOpenButtonText();
      });
    }

    removeDuplicateCoupons();
    couponItems = getCouponItems();
    bindCouponEvents();
    refreshCouponStates();

    const initialInputCode = couponInput ? normalizeCode(couponInput.value) : "";
    const initialItem = findCouponItemByCode(initialInputCode);

    if (initialInputCode && initialItem) {
      confirmedPickerCode = initialInputCode;
    }

    updateOpenButtonText();
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
    const latitudeInput = document.getElementById("latitudeInput");
    const longitudeInput = document.getElementById("longitudeInput");
    const detectedProvinceInput = document.getElementById("detectedProvinceInput");
    const detectedAddressInput = document.getElementById("detectedAddressInput");
    const detectedAddressHint = document.getElementById("detectedAddressHint");

    const paymentList = document.getElementById("paymentList");
    const placeOrderBtn = document.getElementById("placeOrderBtn");

    const couponInput = document.getElementById("couponCode");
    const applyCouponBtn = document.getElementById("applyCouponBtn");
    const couponMessage = document.getElementById("couponMessage");

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

    function normalizeVietnameseText(value) {
      return String(value || "")
              .toLowerCase()
              .normalize("NFD")
              .replace(/[\u0300-\u036f]/g, "")
              .replace(/đ/g, "d")
              .replace(/[^a-z0-9\s./,-]/g, " ")
              .replace(/\s+/g, " ")
              .trim();
    }

    function containsAnyKeyword(value, keywords) {
      const normalizedValue = normalizeVietnameseText(value);

      return keywords.some(function (keyword) {
        const normalizedKeyword = normalizeVietnameseText(keyword);
        return normalizedKeyword && normalizedValue.includes(normalizedKeyword);
      });
    }

    function isHcmAddressKeyword(value) {
      return containsAnyKeyword(value, [
        "tphcm",
        "tp hcm",
        "tp. hcm",
        "ho chi minh",
        "thanh pho ho chi minh",
        "sai gon",
        "thu duc",
        "linh trung",
        "linh xuan"
      ]);
    }

    function isHcmProvince(value) {
      return containsAnyKeyword(value, [
        "tphcm",
        "tp hcm",
        "tp. hcm",
        "ho chi minh",
        "thanh pho ho chi minh"
      ]);
    }

    function isSameProvinceByName(selectedProvince, detectedProvince) {
      const selected = normalizeVietnameseText(selectedProvince);
      const detected = normalizeVietnameseText(detectedProvince);

      if (!selected || !detected) {
        return false;
      }

      if (isHcmProvince(selected) && isHcmProvince(detected)) {
        return true;
      }

      const aliasGroups = [
        ["ho chi minh", "tphcm", "tp hcm", "thanh pho ho chi minh", "sai gon", "thu duc"],
        ["can tho", "tp can tho", "thanh pho can tho"],
        ["ha noi", "tp ha noi", "thanh pho ha noi"],
        ["da nang", "tp da nang", "thanh pho da nang"],
        ["ba ria", "vung tau", "ba ria vung tau"],
        ["binh duong", "thu dau mot", "di an", "thuan an"],
        ["dong nai", "bien hoa"]
      ];

      for (const group of aliasGroups) {
        const selectedInGroup = group.some(function (keyword) {
          return selected.includes(normalizeVietnameseText(keyword));
        });

        const detectedInGroup = group.some(function (keyword) {
          return detected.includes(normalizeVietnameseText(keyword));
        });

        if (selectedInGroup && detectedInGroup) {
          return true;
        }
      }

      return selected.includes(detected) || detected.includes(selected);
    }

    function isValidCoordinateValue(value) {
      if (!value) {
        return false;
      }

      const number = Number(String(value).trim());
      return Number.isFinite(number) && number >= -180 && number <= 180;
    }

    function hasVerifiedCurrentLocation() {
      const latitude = latitudeInput ? latitudeInput.value.trim() : "";
      const longitude = longitudeInput ? longitudeInput.value.trim() : "";
      const detectedProvince = detectedProvinceInput ? detectedProvinceInput.value.trim() : "";
      const selectedProvince = provinceInput ? provinceInput.value.trim() : "";

      return isValidCoordinateValue(latitude)
              && isValidCoordinateValue(longitude)
              && detectedProvince
              && selectedProvince
              && isSameProvinceByName(selectedProvince, detectedProvince);
    }

    function isClearlyInvalidAddressText(value) {
      const normalized = normalizeVietnameseText(value);
      const compact = normalized.replace(/\s+/g, "");

      if (!normalized) {
        return true;
      }

      const invalidExactValues = [
        "abc",
        "abcd",
        "abcde",
        "test",
        "testing",
        "asdf",
        "aaa",
        "aaaa",
        "dia chi",
        "dia chi nha",
        "khong biet",
        "khong co",
        "chua co",
        "tam thoi",
        "none",
        "null"
      ];

      if (invalidExactValues.includes(normalized)) {
        return true;
      }

      /*
       * Chặn chuỗi nhập bừa kiểu aaaaaa, 111111, //////...
       * Địa chỉ ngắn vẫn có thể hợp lệ nếu đã có GPS xác minh,
       * nên không chặn mọi chuỗi toàn số.
       */
      if (compact.length >= 6 && /^(.)\1{5,}$/.test(compact)) {
        return true;
      }

      return !/[a-z0-9]/.test(normalized);
    }

    function isWeakManualAddress(value) {
      const normalized = normalizeVietnameseText(value);
      const compact = normalized.replace(/\s+/g, "");

      if (compact.length < 3) {
        return true;
      }

      const hasLetter = /[a-z]/.test(normalized);
      const hasDigit = /[0-9]/.test(normalized);

      /*
       * Nếu chưa xác minh GPS, chỉ nhập mỗi số nhà là quá mơ hồ.
       */
      if (!hasLetter) {
        return true;
      }

      const usefulKeywords = [
        "duong",
        "hem",
        "ngo",
        "so",
        "ap",
        "khu",
        "kp",
        "khu pho",
        "thon",
        "xom",
        "to",
        "block",
        "chung cu",
        "toa",
        "lau",
        "can ho",
        "xa",
        "phuong",
        "thi tran",
        "doi",
        "tan",
        "linh",
        "nguyen",
        "tran",
        "le",
        "pham",
        "hoang"
      ];

      const hasUsefulKeyword = usefulKeywords.some(function (keyword) {
        return normalized.includes(keyword);
      });

      const wordCount = normalized ? normalized.split(" ").length : 0;

      return !(hasUsefulKeyword || (hasDigit && normalized.length >= 4) || wordCount >= 2);
    }

    function isAddressProvinceConflict(address, province) {
      const normalizedAddress = normalizeVietnameseText(address);
      const normalizedProvince = normalizeVietnameseText(province);

      if (!normalizedAddress || !normalizedProvince) {
        return false;
      }

      if (isHcmAddressKeyword(address) && !isHcmProvince(province)) {
        return true;
      }

      const provinceGroups = [
        ["can tho", "tp can tho", "thanh pho can tho"],
        ["ho chi minh", "tphcm", "tp hcm", "sai gon", "thu duc"],
        ["ha noi", "tp ha noi", "thanh pho ha noi"],
        ["da nang", "tp da nang", "thanh pho da nang"],
        ["dong nai", "bien hoa"],
        ["binh duong", "thu dau mot", "di an", "thuan an"],
        ["long an", "tan an"],
        ["tien giang", "my tho"],
        ["ba ria", "vung tau", "ba ria vung tau"],
        ["tay ninh"],
        ["dong thap", "cao lanh", "sa dec"],
        ["vinh long"],
        ["ben tre"],
        ["an giang", "long xuyen", "chau doc"],
        ["kien giang", "rach gia", "phu quoc"],
        ["khanh hoa", "nha trang"],
        ["lam dong", "da lat"],
        ["binh thuan", "phan thiet"]
      ];

      return provinceGroups.some(function (group) {
        const addressMentionsProvince = group.some(function (keyword) {
          return normalizedAddress.includes(normalizeVietnameseText(keyword));
        });

        if (!addressMentionsProvince) {
          return false;
        }

        const selectedMatchesGroup = group.some(function (keyword) {
          return normalizedProvince.includes(normalizeVietnameseText(keyword));
        });

        return !selectedMatchesGroup;
      });
    }

    function isAddressWardConflict(address, wardName) {
      const normalizedAddress = normalizeVietnameseText(address);
      const normalizedWard = normalizeVietnameseText(wardName);

      if (!normalizedAddress || !normalizedWard) {
        return false;
      }

      const mentionsWardPrefix = normalizedAddress.includes("phuong ")
              || normalizedAddress.includes("xa ")
              || normalizedAddress.includes("thi tran ");

      if (!mentionsWardPrefix) {
        return false;
      }

      const wardShort = normalizedWard
              .replace("phuong ", "")
              .replace("xa ", "")
              .replace("thi tran ", "")
              .trim();

      return wardShort && !normalizedAddress.includes(wardShort);
    }

    function validateAddress() {
      const value = addressInput ? addressInput.value.trim() : "";
      const province = provinceInput ? provinceInput.value.trim() : "";
      const ward = wardInput ? wardInput.value.trim() : "";
      const verifiedLocation = hasVerifiedCurrentLocation();

      if (!value) {
        setFieldError(addressInput, "addressError", "Vui lòng nhập địa chỉ giao hàng.");
        return false;
      }

      if (value.length > 160) {
        setFieldError(addressInput, "addressError", "Địa chỉ không được vượt quá 160 ký tự.");
        return false;
      }

      if (isClearlyInvalidAddressText(value)) {
        setFieldError(addressInput, "addressError", "Địa chỉ không hợp lệ. Vui lòng nhập địa chỉ thật.");
        return false;
      }

      if (!verifiedLocation && isWeakManualAddress(value)) {
        setFieldError(
                addressInput,
                "addressError",
                "Vui lòng nhập rõ số nhà, hẻm, tổ/khu phố/ấp/xã, tên đường hoặc khu vực giao hàng."
        );
        return false;
      }

      if (isAddressProvinceConflict(value, province)) {
        setFieldError(addressInput, "addressError", "Địa chỉ cụ thể không khớp với Tỉnh/TP đã chọn.");
        return false;
      }

      if (isAddressWardConflict(value, ward)) {
        setFieldError(addressInput, "addressError", "Địa chỉ cụ thể có vẻ không khớp với Phường/Xã đã chọn.");
        return false;
      }

      setFieldError(addressInput, "addressError", "");
      return true;
    }

    function validateLocation() {
      const province = provinceInput ? provinceInput.value.trim() : "";
      const ward = wardInput ? wardInput.value.trim() : "";
      const detectedProvince = detectedProvinceInput ? detectedProvinceInput.value.trim() : "";

      if (!province) {
        setFieldError(locationInput, "locationError", "Vui lòng chọn Tỉnh/TP.");
        return false;
      }

      if (!ward) {
        setFieldError(locationInput, "locationError", "Vui lòng chọn Phường/Xã sau khi chọn Tỉnh/TP.");
        return false;
      }

      /*
       * Nếu user đã bấm Dùng vị trí hiện tại và hệ thống phát hiện được Tỉnh/TP,
       * Tỉnh/TP phát hiện phải khớp với Tỉnh/TP đang chọn.
       */
      if (detectedProvince && !isSameProvinceByName(province, detectedProvince)) {
        setFieldError(
                locationInput,
                "locationError",
                "Vị trí hiện tại không khớp với Tỉnh/TP đã chọn. Vị trí phát hiện: " + detectedProvince
        );
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

    function normalizeManualCouponCode(value) {
      return String(value || "")
              .trim()
              .toUpperCase()
              .replace(/[^A-Z0-9_-]/g, "");
    }

    function showManualCouponMessage(type, message) {
      if (!couponMessage) {
        return;
      }

      couponMessage.classList.remove("success", "error", "warning");

      if (type) {
        couponMessage.classList.add(type);
      }

      couponMessage.textContent = message || "";
    }

    function validateManualCouponCode(showMessage) {
      if (!applyCouponBtn || !couponInput) {
        return false;
      }

      const rawValue = couponInput.value;
      const normalizedValue = normalizeManualCouponCode(rawValue);

      if (rawValue !== normalizedValue) {
        couponInput.value = normalizedValue;
      }

      couponInput.classList.remove("is-valid", "is-invalid", "is-warning");

      if (!normalizedValue) {
        applyCouponBtn.disabled = true;

        if (showMessage) {
          showManualCouponMessage("warning", "Vui lòng nhập mã khuyến mãi.");
        } else {
          showManualCouponMessage("", "");
        }

        return false;
      }

      /*
       * Không báo lỗi khi người dùng đang gõ.
       * Khi bấm Áp dụng, backend/AJAX sẽ kiểm tra đầy đủ:
       * tồn tại, active, hạn dùng, lượt dùng, min_order_amount và min_rank_code.
       */
      applyCouponBtn.disabled = false;

      if (showMessage) {
        showManualCouponMessage("", "");
      }

      return true;
    }

    window.validateManualCouponCode = validateManualCouponCode;

    function updateApplyCouponButton() {
      validateManualCouponCode(false);
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
      couponInput.addEventListener("input", function () {
        validateManualCouponCode(false);
      });

      couponInput.addEventListener("blur", function () {
        validateManualCouponCode(true);
      });
    }

    document.querySelectorAll("input[name='paymentMethod']").forEach(function (radio) {
      radio.addEventListener("change", function () {
        validatePaymentMethod();
        updatePlaceOrderButton();
      });
    });

    if (form) {
      const checkoutActionInput = document.getElementById("checkoutActionInput");
      const checkoutRemoveCartKeyInput = document.getElementById("checkoutRemoveCartKeyInput");

      document.querySelectorAll(".js-checkout-remove").forEach(function (button) {
        button.addEventListener("click", function () {
          const ok = confirm("Xóa sản phẩm này khỏi đơn thanh toán?");

          if (!ok) {
            return;
          }

          if (checkoutActionInput) {
            checkoutActionInput.value = "remove-item";
          }

          if (checkoutRemoveCartKeyInput) {
            checkoutRemoveCartKeyInput.value = button.dataset.cartKey || "";
          }

          /*
           * Dùng submit() native để bỏ qua toàn bộ validate frontend.
           * Xóa hàng hóa chỉ là chỉnh danh sách sản phẩm, không phải đặt hàng.
           * Backend vẫn nhận action=remove-item và xử lý trước validate đơn hàng.
           */
          form.noValidate = true;
          HTMLFormElement.prototype.submit.call(form);
        });
      });

      form.addEventListener("submit", function (event) {
        const currentAction = checkoutActionInput ? checkoutActionInput.value : "";

        if (currentAction === "remove-item") {
          return;
        }

        if (checkoutActionInput) {
          checkoutActionInput.value = "";
        }

        if (checkoutRemoveCartKeyInput) {
          checkoutRemoveCartKeyInput.value = "";
        }

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
    const csrfTokenInput = document.querySelector("input[name='csrf_token']");

    function normalizeCode(value) {
      return String(value || "")
              .trim()
              .toUpperCase()
              .replace(/[^A-Z0-9_-]/g, "");
    }

    function formatVnd(value) {
      return new Intl.NumberFormat("vi-VN").format(Math.round(Number(value || 0))) + "đ";
    }

    function setCouponMessage(message, isError) {
      if (!couponMessage) return;

      couponMessage.textContent = message || "";
      couponMessage.classList.remove("warning");
      couponMessage.classList.toggle("error", !!isError);
      couponMessage.classList.toggle("success", !isError && !!message);
    }

    function updateSummary(data) {
      if (!data) return;

      if (summarySubtotal && data.subtotal !== undefined) {
        summarySubtotal.textContent = formatVnd(data.subtotal);
      }

      if (summaryDiscount && data.discount !== undefined) {
        summaryDiscount.textContent = formatVnd(data.discount);
      }

      if (summaryTotal && data.total !== undefined) {
        summaryTotal.textContent = formatVnd(data.total);
      }
    }

    function setLoadingState(loading) {
      if (!applyBtn) return;

      applyBtn.disabled = !!loading;
      applyBtn.textContent = loading ? "Đang áp dụng..." : "Áp dụng";
    }

    function applyCouponByAjax() {
      const code = normalizeCode(couponInput ? couponInput.value : "");

      if (!code) {
        setCouponMessage("Vui lòng nhập mã khuyến mãi.", true);
        return;
      }

      if (couponInput) {
        couponInput.value = code;
      }

      if (window.validateManualCouponCode && !window.validateManualCouponCode(true)) {
        return;
      }

      const params = new URLSearchParams();
      params.append("action", "apply");
      params.append("code", code);
      params.append("couponCode", code);

      if (csrfTokenInput && csrfTokenInput.value) {
        params.append("csrf_token", csrfTokenInput.value);
      }

      setLoadingState(true);
      setCouponMessage("", false);

      fetch("${pageContext.request.contextPath}/ajax/apply-coupon", {
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
                const discountValue = Number(data && data.discount ? data.discount : 0);

                if (!data || data.ok !== true || discountValue <= 0) {
                  const message = data && (data.error || data.message)
                          ? (data.error || data.message)
                          : "Mã khuyến mãi không tồn tại hoặc không đủ điều kiện áp dụng.";

                  setCouponMessage(message, true);

                  if (summaryDiscount) {
                    summaryDiscount.textContent = formatVnd(0);
                  }

                  if (window.clearAppliedCouponPicker) {
                    window.clearAppliedCouponPicker();
                  }

                  if (window.updateShippingFeeByLocation) {
                    window.updateShippingFeeByLocation();
                  }

                  if (window.refreshCheckoutCouponStates) {
                    window.refreshCheckoutCouponStates();
                  }

                  return;
                }

                updateSummary(data);

                if (window.updateShippingFeeByLocation) {
                  window.updateShippingFeeByLocation();
                }

                if (window.setAppliedCouponCodeForPicker) {
                  window.setAppliedCouponCodeForPicker(data.code || code);
                }

                if (window.refreshCheckoutCouponStates) {
                  window.refreshCheckoutCouponStates();
                }

                if (window.updateCheckoutButtonsState) {
                  window.updateCheckoutButtonsState();
                }

                setCouponMessage(data.message || "Áp dụng mã giảm giá thành công.", false);
              })
              .catch(function () {
                setCouponMessage("Không thể áp dụng mã giảm giá lúc này.", true);
              })
              .finally(function () {
                setLoadingState(false);

                if (window.validateManualCouponCode) {
                  window.validateManualCouponCode(false);
                }
              });
    }

    if (applyBtn && couponInput) {
      applyBtn.addEventListener("click", applyCouponByAjax);
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

      if (window.refreshCheckoutCouponStates) {
        window.refreshCheckoutCouponStates();
      }

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

    const SHIPPING_RULES = {
      HCM: {
        ECONOMY: {
          fee: 20000,
          label: "20.000đ",
          description: "Nội thành TP.HCM: 3 - 5 ngày",
          disabled: false
        },
        FAST: {
          fee: 35000,
          label: "35.000đ",
          description: "Nội thành TP.HCM: 1 - 3 ngày",
          disabled: false
        },
        EXPRESS: {
          fee: 50000,
          label: "50.000đ",
          description: "Hỏa tốc trong ngày, chỉ áp dụng TP.HCM",
          disabled: false
        }
      },
      OTHER: {
        ECONOMY: {
          fee: 35000,
          label: "35.000đ",
          description: "Ngoại tỉnh: 3 - 5 ngày",
          disabled: false
        },
        FAST: {
          fee: 50000,
          label: "50.000đ",
          description: "Ngoại tỉnh: 1 - 3 ngày",
          disabled: false
        },
        EXPRESS: {
          fee: 0,
          label: "Không hỗ trợ",
          description: "Hỏa tốc chỉ áp dụng cho khu vực TP.HCM",
          disabled: true
        }
      }
    };

    const provinceInput = document.getElementById("provinceInput");
    const wardInput = document.getElementById("wardInput");

    const deliveryBox = document.getElementById("deliveryBox");
    const deliveryEmpty = document.getElementById("deliveryEmpty");
    const deliveryOptions = document.getElementById("deliveryOptions");
    const freeshipNote = document.getElementById("freeshipNote");
    const shippingMethodError = document.getElementById("shippingMethodError");

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

    function normalizeText(value) {
      return String(value || "")
              .trim()
              .toLowerCase()
              .normalize("NFD")
              .replace(/[\u0300-\u036f]/g, "");
    }

    function isHcmCity(provinceName) {
      const value = normalizeText(provinceName);

      return value.includes("ho chi minh")
              || value.includes("tp. hcm")
              || value.includes("tp hcm")
              || value.includes("tphcm")
              || value.includes("thanh pho ho chi minh");
    }

    function getShippingArea() {
      const province = provinceInput ? provinceInput.value.trim() : "";
      return isHcmCity(province) ? "HCM" : "OTHER";
    }

    function hasValidLocation() {
      const province = provinceInput ? provinceInput.value.trim() : "";
      const ward = wardInput ? wardInput.value.trim() : "";
      return province !== "" && ward !== "";
    }

    function getSubtotal() {
      const subtotalFromSummary = parseVndText(summarySubtotal ? summarySubtotal.textContent : "0");

      if (subtotalFromSummary > 0) {
        return subtotalFromSummary;
      }

      let subtotalFromItems = 0;
      document.querySelectorAll(".js-item-subtotal").forEach(function (el) {
        subtotalFromItems += Number(el.dataset.raw || 0) || 0;
      });

      return subtotalFromItems;
    }

    function getDiscount() {
      return parseVndText(summaryDiscount ? summaryDiscount.textContent : "0");
    }

    function getOrderValueAfterVoucher() {
      return Math.max(getSubtotal() - getDiscount(), 0);
    }

    function isFreeShipEligible() {
      /*
       * Freeship áp dụng toàn quốc.
       * Không phân biệt TP.HCM hay tỉnh khác.
       */
      return getOrderValueAfterVoucher() >= FREE_SHIP_THRESHOLD;
    }

    function getSelectedShippingInput() {
      return document.querySelector("input[name='shippingMethod']:checked");
    }

    function getSelectedShippingMethod() {
      const selected = getSelectedShippingInput();
      return selected ? selected.value : "ECONOMY";
    }

    function getRule(method) {
      const area = getShippingArea();
      return (SHIPPING_RULES[area] && SHIPPING_RULES[area][method])
              ? SHIPPING_RULES[area][method]
              : SHIPPING_RULES.HCM.ECONOMY;
    }

    function clearShippingError() {
      if (shippingMethodError) {
        shippingMethodError.textContent = "";
      }

      if (deliveryBox) {
        deliveryBox.classList.remove("is-invalid");
      }
    }

    function setShippingError(message) {
      if (shippingMethodError) {
        shippingMethodError.textContent = message || "";
      }

      if (deliveryBox) {
        deliveryBox.classList.toggle("is-invalid", !!message);
      }
    }

    function ensureSupportedShippingMethod() {
      const selectedInput = getSelectedShippingInput();

      if (!selectedInput) {
        const economy = document.querySelector("input[name='shippingMethod'][value='ECONOMY']");
        if (economy) economy.checked = true;
        return false;
      }

      const selectedRule = getRule(selectedInput.value);

      if (!hasValidLocation() || !selectedRule.disabled) {
        return false;
      }

      const economy = document.querySelector("input[name='shippingMethod'][value='ECONOMY']");

      if (economy) {
        economy.checked = true;
      }

      return true;
    }

    function updateShippingMethodLabels() {
      const validLocation = hasValidLocation();
      const freeship = validLocation && isFreeShipEligible();

      document.querySelectorAll("input[name='shippingMethod']").forEach(function (input) {
        const option = input.closest(".delivery-option");
        const feeEl = option ? option.querySelector(".delivery-fee") : null;
        const descEl = option ? option.querySelector(".delivery-info small") : null;
        const rule = getRule(input.value);

        if (!option || !feeEl) {
          return;
        }

        input.disabled = false;
        option.classList.remove("is-disabled");

        if (!validLocation) {
          feeEl.textContent = rule.label;
          if (descEl) descEl.textContent = rule.description;
          return;
        }

        if (rule.disabled) {
          /*
           * Hỏa tốc ngoại tỉnh vẫn không hỗ trợ.
           * Freeship toàn quốc áp dụng cho các phương thức giao hàng được hỗ trợ.
           */
          feeEl.textContent = rule.label;
          if (descEl) descEl.textContent = rule.description;
          input.disabled = true;
          option.classList.add("is-disabled");
          return;
        }

        feeEl.textContent = freeship ? "Miễn phí" : rule.label;
        if (descEl) {
          descEl.textContent = freeship
                  ? "Đơn sau voucher đạt từ " + formatVnd(FREE_SHIP_THRESHOLD) + ", miễn phí vận chuyển toàn quốc"
                  : rule.description;
        }
      });
    }

    function calculateShippingFee() {
      if (!hasValidLocation()) {
        return 0;
      }

      /*
       * Quan trọng:
       * Kiểm tra freeship trước khi tính phí theo khu vực.
       * Điều kiện freeship áp dụng cho cả TP.HCM và tỉnh khác.
       */
      if (isFreeShipEligible()) {
        return 0;
      }

      const method = getSelectedShippingMethod();
      const rule = getRule(method);

      if (rule.disabled) {
        return SHIPPING_RULES.OTHER.ECONOMY.fee;
      }

      return Number(rule.fee || 0);
    }

    function updateCheckoutTotal() {
      const subtotal = getSubtotal();
      const discount = getDiscount();
      const shippingFee = Number(shippingFeeInput ? shippingFeeInput.value : 0) || 0;
      const total = Math.max(subtotal - discount + shippingFee, 0);

      if (summarySubtotal && parseVndText(summarySubtotal.textContent) === 0 && subtotal > 0) {
        summarySubtotal.textContent = formatVnd(subtotal);
      }

      if (summaryTotal) {
        summaryTotal.textContent = formatVnd(total);
      }
    }

    function updateShippingDisplay() {
      const validLocation = hasValidLocation();
      const changedUnsupportedMethod = ensureSupportedShippingMethod();

      updateShippingMethodLabels();

      if (!validLocation) {
        if (deliveryEmpty) deliveryEmpty.classList.remove("hidden");
        if (deliveryOptions) deliveryOptions.classList.add("hidden");
        if (freeshipNote) freeshipNote.classList.add("hidden");
        if (summaryShippingFee) summaryShippingFee.textContent = "-";
        if (shippingFeeInput) shippingFeeInput.value = "0";

        clearShippingError();
        updateCheckoutTotal();
        return;
      }

      if (deliveryEmpty) deliveryEmpty.classList.add("hidden");
      if (deliveryOptions) deliveryOptions.classList.remove("hidden");

      if (changedUnsupportedMethod) {
        setShippingError("Hỏa tốc chỉ hỗ trợ khu vực TP.HCM. Hệ thống đã chuyển về Giao hàng tiết kiệm.");
      } else {
        clearShippingError();
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
        if (freeship) {
          freeshipNote.textContent = "🎉 Đơn hàng sau voucher đạt từ "
                  + formatVnd(FREE_SHIP_THRESHOLD)
                  + ", miễn phí vận chuyển toàn quốc.";
        }
      }

      updateCheckoutTotal();
    }

    document.querySelectorAll("input[name='shippingMethod']").forEach(function (radio) {
      radio.addEventListener("change", updateShippingDisplay);
    });

    window.updateShippingFeeByLocation = updateShippingDisplay;
    window.updateCheckoutTotalWithShipping = updateCheckoutTotal;

    updateShippingDisplay();
  })();
</script>


<!-- ================= ADDRESS MAP PICKER MODAL ================= -->
<div class="address-map-modal" id="addressMapModal" aria-hidden="true">
  <div class="address-map-backdrop" data-close-address-map></div>

  <div class="address-map-dialog" role="dialog" aria-modal="true" aria-labelledby="addressMapTitle">
    <div class="address-map-header">
      <h3 id="addressMapTitle">Xác nhận vị trí giao hàng</h3>
      <p>Kéo ghim màu đỏ đến đúng vị trí giao hàng. Kiểm tra địa chỉ gợi ý, chọn dùng địa chỉ gợi ý nếu phù hợp, rồi bấm xác nhận vị trí.</p>
      <button type="button" class="address-map-close" data-close-address-map aria-label="Đóng">×</button>
    </div>

    <div class="address-map-body">
      <div class="address-map-status" id="addressMapStatus">
        Đang chuẩn bị bản đồ...
      </div>

      <div class="address-map-layout">
        <div class="address-map-left">
          <div class="address-map-canvas">
            <div id="addressMap" class="address-map-leaflet"></div>
          </div>
        </div>

        <div class="address-map-side">
          <div class="address-map-detected" id="addressMapDetected">
            <div class="address-map-detected-title">Địa chỉ gợi ý từ ghim</div>
            <div id="addressMapDetectedText">Chưa có địa chỉ phát hiện.</div>
            <div class="address-map-suggestion-actions">
              <button type="button"
                      class="address-map-btn suggestion"
                      id="useSuggestedAddressBtn"
                      disabled>
                Dùng địa chỉ gợi ý
              </button>
            </div>
          </div>

          <div class="address-map-note">
            <strong>Lưu ý:</strong> địa chỉ từ bản đồ chỉ là gợi ý gần đúng. Bạn nên kiểm tra lại số nhà, hẻm, tổ/khu phố/ấp trước khi đặt hàng.
          </div>
        </div>
      </div>
    </div>

    <div class="address-map-footer">
      <div class="address-map-footer-actions">
        <button type="button" class="address-map-btn" data-close-address-map>Hủy</button>
        <button type="button" class="address-map-btn primary" id="confirmMapLocationBtn" disabled>
          Xác nhận vị trí
        </button>
      </div>
    </div>
  </div>
</div>

<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
        integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo="
        crossorigin=""></script>

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

    function normalizeText(value) {
      return String(value || "")
              .trim()
              .toLowerCase()
              .normalize("NFD")
              .replace(/[\u0300-\u036f]/g, "")
              .replace(/đ/g, "d")
              .replace(/[^a-z0-9\s./,-]/g, " ")
              .replace(/\s+/g, " ")
              .trim();
    }

    function removeAdministrativePrefix(value) {
      return normalizeText(value)
              .replace(/^thanh pho\s+/, "")
              .replace(/^tp\.?\s+/, "")
              .replace(/^tinh\s+/, "")
              .replace(/^quan\s+/, "")
              .replace(/^huyen\s+/, "")
              .replace(/^thi xa\s+/, "")
              .replace(/^phuong\s+/, "")
              .replace(/^xa\s+/, "")
              .replace(/^thi tran\s+/, "")
              .trim();
    }

    function isHcmName(value) {
      const normalized = normalizeText(value);
      return normalized.includes("ho chi minh")
              || normalized.includes("thanh pho ho chi minh")
              || normalized.includes("tp hcm")
              || normalized.includes("tp. hcm")
              || normalized.includes("tphcm")
              || normalized.includes("sai gon")
              || normalized.includes("thu duc");
    }

    function isSameName(a, b) {
      const left = normalizeText(a);
      const right = normalizeText(b);

      if (!left || !right) {
        return false;
      }

      if (isHcmName(left) && isHcmName(right)) {
        return true;
      }

      const aliasGroups = [
        ["ho chi minh", "tphcm", "tp hcm", "thanh pho ho chi minh", "sai gon", "thu duc"],
        ["can tho", "tp can tho", "thanh pho can tho"],
        ["ha noi", "tp ha noi", "thanh pho ha noi"],
        ["da nang", "tp da nang", "thanh pho da nang"],
        ["ba ria", "vung tau", "ba ria vung tau"],
        ["binh duong", "thu dau mot", "di an", "thuan an"],
        ["dong nai", "bien hoa"]
      ];

      for (const group of aliasGroups) {
        const leftInGroup = group.some(function (keyword) {
          return left.includes(normalizeText(keyword));
        });

        const rightInGroup = group.some(function (keyword) {
          return right.includes(normalizeText(keyword));
        });

        if (leftInGroup && rightInGroup) {
          return true;
        }
      }

      const leftShort = removeAdministrativePrefix(left);
      const rightShort = removeAdministrativePrefix(right);

      return left.includes(right)
              || right.includes(left)
              || leftShort.includes(rightShort)
              || rightShort.includes(leftShort);
    }

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
        return provinces;
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
        return provinces;
      } catch (error) {
        console.error("Load provinces failed:", error);
        setEmpty(provinceList, "Không tải được danh sách Tỉnh/TP. Vui lòng thử lại.");
        return [];
      }
    }

    async function fetchWardsByProvince(province) {
      if (!province || !province.code) {
        return [];
      }

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

      return wards;
    }

    function setSelectedProvince(province) {
      selectedProvince = province;
      selectedWard = null;

      provinceInput.value = province && province.name ? province.name : "";
      provinceCodeInput.value = province && province.code ? province.code : "";

      wardInput.value = "";
      wardCodeInput.value = "";

      locationInput.value = province && province.name ? province.name : "";

      wardTab.classList.toggle("disabled", !province);
    }

    async function loadWardsByProvince(province) {
      setSelectedProvince(province);
      notifyButtonState();

      setActiveTab("ward");
      setLoading(wardList, "Đang tải danh sách Phường/Xã...");

      try {
        const wards = await fetchWardsByProvince(province);
        renderWards(wards);
        return wards;
      } catch (error) {
        console.error("Load wards failed:", error);
        setEmpty(wardList, "Không tải được danh sách Phường/Xã của tỉnh này.");
        return [];
      }
    }

    function selectWard(ward, shouldHideDropdown) {
      selectedWard = ward;

      wardInput.value = ward && ward.name ? ward.name : "";
      wardCodeInput.value = ward && ward.code ? ward.code : "";

      if (selectedProvince && selectedWard) {
        locationInput.value = selectedWard.name + ", " + selectedProvince.name;
      } else if (selectedProvince) {
        locationInput.value = selectedProvince.name;
      }

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

      if (shouldHideDropdown) {
        hideDropdown();
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
          selectWard(ward, true);
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

    function findBestProvince(candidates) {
      const candidateList = (candidates || [])
              .map(function (value) { return String(value || "").trim(); })
              .filter(Boolean);

      if (!candidateList.length || !provinces.length) {
        return null;
      }

      for (const candidate of candidateList) {
        const matched = provinces.find(function (province) {
          return isSameName(province.name, candidate);
        });

        if (matched) {
          return matched;
        }
      }

      return null;
    }

    function findBestWard(wards, candidates) {
      const candidateList = (candidates || [])
              .map(function (value) { return String(value || "").trim(); })
              .filter(Boolean);

      if (!candidateList.length || !wards.length) {
        return null;
      }

      for (const candidate of candidateList) {
        const matched = wards.find(function (ward) {
          return isSameName(ward.name, candidate);
        });

        if (matched) {
          return matched;
        }
      }

      return null;
    }

    async function autoFillFromCurrentLocation(payload) {
      const data = payload || {};
      const provinceCandidates = data.provinceCandidates || [];
      const wardCandidates = data.wardCandidates || [];
      const streetAddress = String(data.streetAddress || "").trim();

      const loadedProvinces = await loadProvinces();

      if (!loadedProvinces.length) {
        return {
          ok: false,
          reason: "Không tải được danh sách Tỉnh/TP để tự điền địa chỉ."
        };
      }

      const province = findBestProvince(provinceCandidates);

      if (!province) {
        return {
          ok: false,
          reason: "Đã lấy vị trí nhưng chưa ghép được Tỉnh/TP vào danh sách hệ thống. Vui lòng chọn thủ công.",
          detectedProvince: provinceCandidates.find(Boolean) || ""
        };
      }

      setSelectedProvince(province);
      setActiveTab("ward");
      setLoading(wardList, "Đang xác minh Phường/Xã theo vị trí hiện tại...");

      let wards = [];

      try {
        wards = await fetchWardsByProvince(province);
      } catch (error) {
        console.error("Auto-fill wards failed:", error);
      }

      renderWards(wards);

      const ward = findBestWard(wards, wardCandidates);

      if (ward) {
        selectWard(ward, true);
      } else {
        selectedWard = null;
        wardInput.value = "";
        wardCodeInput.value = "";
        locationInput.value = province.name;
        setEmpty(wardList, "Đã xác minh Tỉnh/TP. Vui lòng kiểm tra và chọn Phường/Xã thủ công.");
      }

      /*
       * Không tự ghi đè ô Địa chỉ cụ thể bằng kết quả reverse geocoding.
       * API bản đồ chỉ trả về địa chỉ gần đúng, có thể sai số nhà/đường/khu phố.
       * Vì vậy hệ thống chỉ tự điền Tỉnh/TP, Phường/Xã và lưu tọa độ;
       * địa chỉ giao hàng chính vẫn do người dùng nhập hoặc chỉnh lại.
       */

      updateShippingAddress();
      updateDeliveryText();
      notifyButtonState();

      return {
        ok: true,
        province: province.name,
        provinceCode: province.code,
        ward: ward ? ward.name : "",
        wardCode: ward ? ward.code : "",
        partial: !ward
      };
    }

    window.checkoutLocationSelector = {
      autoFillFromCurrentLocation: autoFillFromCurrentLocation,
      updateShippingAddress: updateShippingAddress,
      loadProvinces: loadProvinces
    };

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

<!-- ================= CURRENT LOCATION MAP PICKER ================= -->
<script>
  (function () {
    const useCurrentLocationBtn = document.getElementById("useCurrentLocationBtn");
    const addressInput = document.getElementById("address");
    const locationInput = document.getElementById("locationInput");

    const latitudeInput = document.getElementById("latitudeInput");
    const longitudeInput = document.getElementById("longitudeInput");
    const detectedProvinceInput = document.getElementById("detectedProvinceInput");
    const detectedAddressInput = document.getElementById("detectedAddressInput");
    const mapConfirmedInput = document.getElementById("mapConfirmedInput");
    const detectedAddressHint = document.getElementById("detectedAddressHint");

    const modal = document.getElementById("addressMapModal");
    const mapEl = document.getElementById("addressMap");
    const mapStatus = document.getElementById("addressMapStatus");
    const mapDetected = document.getElementById("addressMapDetected");
    const mapDetectedText = document.getElementById("addressMapDetectedText");
    const confirmBtn = document.getElementById("confirmMapLocationBtn");
    const useSuggestedAddressBtn = document.getElementById("useSuggestedAddressBtn");

    if (!useCurrentLocationBtn || !modal || !mapEl) {
      return;
    }

    let map = null;
    let marker = null;
    let selectedDot = null;

    let currentLat = null;
    let currentLon = null;
    let currentGeoData = null;
    let currentStreetAddress = "";
    let currentDetectedAddress = "";
    let currentDetectedProvince = "";

    let reverseGeocodeRequestId = 0;

    function normalizeText(value) {
      return String(value || "")
              .trim()
              .toLowerCase()
              .normalize("NFD")
              .replace(/[\u0300-\u036f]/g, "")
              .replace(/đ/g, "d")
              .replace(/[^a-z0-9\s./,-]/g, " ")
              .replace(/\s+/g, " ")
              .trim();
    }

    function wait(ms) {
      return new Promise(function (resolve) {
        window.setTimeout(resolve, ms);
      });
    }

    function setAddressError(message) {
      const errorEl = document.getElementById("addressError");

      if (addressInput) {
        addressInput.classList.toggle("is-invalid", !!message);
      }

      if (errorEl) {
        errorEl.textContent = message || "";
      }
    }

    function setLocationError(message) {
      const errorEl = document.getElementById("locationError");

      if (locationInput) {
        locationInput.classList.toggle("is-invalid", !!message);
      }

      if (errorEl) {
        errorEl.textContent = message || "";
      }
    }

    function escapeHtml(value) {
      return String(value || "")
              .replace(/&/g, "&amp;")
              .replace(/</g, "&lt;")
              .replace(/>/g, "&gt;")
              .replace(/"/g, "&quot;")
              .replace(/'/g, "&#039;");
    }

    function setMapStatus(message, type) {
      if (!mapStatus) return;

      mapStatus.textContent = message || "";
      mapStatus.classList.remove("is-error", "is-success");

      if (type === "error") {
        mapStatus.classList.add("is-error");
      }

      if (type === "success") {
        mapStatus.classList.add("is-success");
      }
    }

    function showDetectedAddressHint() {
      /*
       * Không hiển thị thêm dòng gợi ý ngoài form checkout.
       * Địa chỉ gợi ý chỉ nằm trong modal bản đồ để tránh rối giao diện.
       */
      if (!detectedAddressHint) {
        return;
      }

      detectedAddressHint.classList.remove("show");
      detectedAddressHint.textContent = "";
      detectedAddressHint.innerHTML = "";
    }

    function showMapDetected(streetAddress, detectedAddress) {
      const target = mapDetectedText || mapDetected;

      if (!target) {
        return;
      }

      const street = String(streetAddress || "").trim();
      const full = String(detectedAddress || "").trim();

      if (!street && !full) {
        target.innerHTML = "Chưa xác định được địa chỉ từ vị trí ghim.";

        if (useSuggestedAddressBtn) {
          useSuggestedAddressBtn.disabled = true;
        }

        return;
      }

      target.innerHTML =
              "<strong>" + escapeHtml(street || full) + "</strong>"
              + (street && full && street !== full ? "<small>Đầy đủ: " + escapeHtml(full) + "</small>" : "");

      if (useSuggestedAddressBtn) {
        useSuggestedAddressBtn.disabled = !(street || full);
      }
    }

    function applySuggestedAddressToInput() {
      const suggestion = String(currentStreetAddress || currentDetectedAddress || "").trim();

      if (!suggestion) {
        setMapStatus("Chưa có địa chỉ gợi ý để điền vào ô Địa chỉ cụ thể.", "error");
        return;
      }

      if (addressInput) {
        addressInput.value = suggestion;
        addressInput.classList.remove("is-invalid");
        addressInput.dispatchEvent(new Event("input", { bubbles: true }));
      }

      setAddressError("");
      showDetectedAddressHint();
      setMapStatus("Đã điền địa chỉ gợi ý vào ô Địa chỉ cụ thể. Bạn vẫn có thể sửa lại nếu cần.", "success");

      if (window.checkoutLocationSelector && window.checkoutLocationSelector.updateShippingAddress) {
        window.checkoutLocationSelector.updateShippingAddress();
      }

      if (window.updateShippingFeeByLocation) {
        window.updateShippingFeeByLocation();
      }

      if (window.updateCheckoutButtonsState) {
        window.updateCheckoutButtonsState();
      }
    }

    async function reverseGeocode(lat, lon) {
      const url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat="
              + encodeURIComponent(lat)
              + "&lon="
              + encodeURIComponent(lon)
              + "&accept-language=vi&addressdetails=1&zoom=18";

      const response = await fetch(url, {
        method: "GET",
        headers: {
          "Accept": "application/json"
        }
      });

      if (!response.ok) {
        throw new Error("Reverse geocoding failed: HTTP " + response.status);
      }

      return response.json();
    }

    function extractAddressObject(data) {
      return data && data.address ? data.address : {};
    }

    function compactParts(parts) {
      const seen = new Set();

      return (parts || [])
              .map(function (part) { return String(part || "").trim(); })
              .filter(Boolean)
              .filter(function (part) {
                const key = normalizeText(part);

                if (seen.has(key)) {
                  return false;
                }

                seen.add(key);
                return true;
              });
    }

    function addRoadPrefixIfNeeded(road) {
      const value = String(road || "").trim();

      if (!value) {
        return "";
      }

      const normalized = normalizeText(value);
      const alreadyHasRoadPrefix = normalized.includes("duong")
              || normalized.includes("street")
              || normalized.includes("road")
              || normalized.includes("ql")
              || normalized.includes("quoc lo")
              || normalized.includes("tinh lo")
              || normalized.includes("hem")
              || normalized.includes("ngo");

      return alreadyHasRoadPrefix ? value : "Đường " + value;
    }

    function buildStreetAddress(address, data) {
      const houseNumber = address.house_number || "";
      const road = addRoadPrefixIfNeeded(
              address.road || address.pedestrian || address.footway || address.path || ""
      );

      const roadLine = compactParts([houseNumber, road]).join(" ").trim();

      const areaLine = compactParts([
        address.hamlet,
        address.village,
        address.neighbourhood,
        address.quarter,
        address.suburb,
        address.residential,
        address.city_district,
        address.district
      ])[0] || "";

      const detailedLine = compactParts([roadLine, areaLine]).join(", ").trim();

      if (detailedLine) {
        return detailedLine;
      }

      const displayName = data && data.display_name ? String(data.display_name) : "";

      if (displayName) {
        const parts = displayName
                .split(",")
                .map(function (part) { return part.trim(); })
                .filter(Boolean)
                .filter(function (part) {
                  const normalized = normalizeText(part);

                  return normalized !== "viet nam"
                          && normalized !== "vietnam"
                          && !/^\d{5,6}$/.test(normalized)
                          && !normalized.includes("tinh ")
                          && !normalized.includes("thanh pho ");
                })
                .slice(0, 3);

        return compactParts(parts).join(", ");
      }

      return "";
    }

    function getProvinceCandidates(address) {
      return compactParts([
        address.state,
        address.province,
        address.city,
        address.town,
        address.county,
        address.municipality
      ]);
    }

    function getWardCandidates(address) {
      return compactParts([
        address.suburb,
        address.neighbourhood,
        address.quarter,
        address.city_district,
        address.district,
        address.village,
        address.hamlet,
        address.residential,
        address.municipality
      ]);
    }

    function getMapCanvas() {
      return mapEl.closest(".address-map-canvas") || mapEl.parentElement;
    }

    function forceMapElementSize() {
      const canvas = getMapCanvas();

      if (!canvas || !mapEl) {
        return false;
      }

      const rect = canvas.getBoundingClientRect();

      if (rect.width <= 0 || rect.height <= 0) {
        return false;
      }

      /*
       * Fix quan trọng:
       * Khi modal vừa mở, Leaflet đôi khi đọc sai kích thước vì khung trước đó bị ẩn.
       * Ép #addressMap theo kích thước thật của .address-map-canvas trước khi invalidateSize().
       */
      mapEl.style.width = Math.round(rect.width) + "px";
      mapEl.style.height = Math.round(rect.height) + "px";
      mapEl.style.minHeight = Math.round(rect.height) + "px";
      mapEl.style.position = "absolute";
      mapEl.style.left = "0";
      mapEl.style.top = "0";
      mapEl.style.right = "0";
      mapEl.style.bottom = "0";
      mapEl.style.display = "block";

      return true;
    }

    async function waitForVisibleMapFrame() {
      for (let i = 0; i < 14; i += 1) {
        forceMapElementSize();

        const rect = mapEl.getBoundingClientRect();

        if (rect.width > 120 && rect.height > 120) {
          return true;
        }

        await wait(i < 2 ? 60 : 100);
      }

      return forceMapElementSize();
    }

    function refreshMapSize(lat, lon) {
      if (!map) {
        return;
      }

      forceMapElementSize();

      const parsedLat = Number(lat);
      const parsedLon = Number(lon);

      const targetLat = Number.isFinite(parsedLat)
              ? parsedLat
              : (currentLat != null ? Number(currentLat) : map.getCenter().lat);

      const targetLon = Number.isFinite(parsedLon)
              ? parsedLon
              : (currentLon != null ? Number(currentLon) : map.getCenter().lng);

      function doRefresh() {
        if (!map) {
          return;
        }

        forceMapElementSize();

        map.invalidateSize(true);

        if (Number.isFinite(targetLat) && Number.isFinite(targetLon)) {
          map.setView([targetLat, targetLon], map.getZoom() || 17, {
            animate: false,
            pan: false
          });

          if (marker) {
            marker.setLatLng([targetLat, targetLon]);
          }

          updateSelectedDot(targetLat, targetLon);
        }
      }

      requestAnimationFrame(doRefresh);
      setTimeout(doRefresh, 80);
      setTimeout(doRefresh, 220);
      setTimeout(doRefresh, 480);
      setTimeout(doRefresh, 850);
    }

    async function openMapModal() {
      modal.classList.add("show");
      modal.setAttribute("aria-hidden", "false");
      document.body.classList.add("address-map-open");

      await waitForVisibleMapFrame();

      refreshMapSize(currentLat, currentLon);
    }

    function closeMapModal() {
      modal.classList.remove("show");
      modal.setAttribute("aria-hidden", "true");
      document.body.classList.remove("address-map-open");
    }

    function resetDetectedLocation() {
      if (latitudeInput) latitudeInput.value = "";
      if (longitudeInput) longitudeInput.value = "";
      if (detectedProvinceInput) detectedProvinceInput.value = "";
      if (detectedAddressInput) detectedAddressInput.value = "";
      if (mapConfirmedInput) mapConfirmedInput.value = "";

      showDetectedAddressHint();
      useCurrentLocationBtn.textContent = "📍 Dùng vị trí hiện tại";
    }

    function updateSelectedDot(lat, lon) {
      if (!map || !window.L) {
        return;
      }

      if (!selectedDot) {
        selectedDot = L.circleMarker([lat, lon], {
          radius: 9,
          color: "#ffffff",
          weight: 4,
          fillColor: "#dc2626",
          fillOpacity: 1,
          opacity: 1,
          interactive: false,
          className: "shipping-selected-dot"
        }).addTo(map);
      } else {
        selectedDot.setLatLng([lat, lon]);
      }
    }

    async function updateSelectedPin(lat, lon) {
      const requestId = ++reverseGeocodeRequestId;

      currentLat = Number(lat);
      currentLon = Number(lon);
      updateSelectedDot(currentLat, currentLon);

      if (confirmBtn) {
        confirmBtn.disabled = true;
      }

      setMapStatus("Đang xác định địa chỉ tại vị trí ghim...", "");

      try {
        const geoData = await reverseGeocode(currentLat, currentLon);

        if (requestId !== reverseGeocodeRequestId) {
          return;
        }

        const address = extractAddressObject(geoData);
        const streetAddress = buildStreetAddress(address, geoData);
        const detectedAddress = geoData && geoData.display_name ? geoData.display_name : "";
        const provinceCandidates = getProvinceCandidates(address);
        const detectedProvince = provinceCandidates[0] || "";

        currentGeoData = geoData;
        currentStreetAddress = streetAddress;
        currentDetectedAddress = detectedAddress;
        currentDetectedProvince = detectedProvince;

        showMapDetected(streetAddress, detectedAddress);

        if (!detectedProvince) {
          setMapStatus("Không xác định được Tỉnh/TP từ ghim này. Hãy kéo ghim sang vị trí rõ hơn.", "error");
          return;
        }

        setMapStatus("Đã xác định được vị trí. Hãy kiểm tra ghim và bấm xác nhận.", "success");

        if (confirmBtn) {
          confirmBtn.disabled = false;
        }
      } catch (error) {
        if (requestId !== reverseGeocodeRequestId) {
          return;
        }

        console.error(error);

        currentGeoData = null;
        currentStreetAddress = "";
        currentDetectedAddress = "";
        currentDetectedProvince = "";

        showMapDetected("", "");
        setMapStatus("Không thể lấy địa chỉ từ ghim hiện tại. Vui lòng thử kéo ghim hoặc thử lại sau.", "error");
      }
    }

    function resetMapContainerClasses() {
      if (!mapEl) {
        return;
      }

      mapEl.innerHTML = "";
      mapEl.className = "address-map-leaflet";
      mapEl.removeAttribute("tabindex");
      mapEl.removeAttribute("style");

      forceMapElementSize();
    }

    function destroyMap() {
      reverseGeocodeRequestId += 1;

      if (map) {
        try {
          map.off();
          map.remove();
        } catch (error) {
          console.warn("Cannot remove old map instance", error);
        }
      }

      map = null;
      marker = null;
      selectedDot = null;

      resetMapContainerClasses();
    }

    function ensureMap(lat, lon) {
      if (!window.L || !mapEl) {
        throw new Error("Leaflet library is not available");
      }

      const safeLat = Number(lat);
      const safeLon = Number(lon);

      if (!Number.isFinite(safeLat) || !Number.isFinite(safeLon)) {
        throw new Error("Invalid map coordinates");
      }

      forceMapElementSize();

      if (!map) {
        map = L.map(mapEl, {
          zoomControl: true,
          scrollWheelZoom: true,
          trackResize: true,
          fadeAnimation: false,
          zoomAnimation: false,
          markerZoomAnimation: false,
          preferCanvas: true
        }).setView([safeLat, safeLon], 17);

        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
          maxZoom: 19,
          tileSize: 256,
          zoomOffset: 0,
          updateWhenIdle: false,
          updateWhenZooming: false,
          keepBuffer: 6,
          crossOrigin: true,
          attribution: "&copy; OpenStreetMap contributors"
        }).addTo(map);

        const redPinIcon = L.divIcon({
          className: "custom-red-map-marker",
          html: '<div class="custom-red-map-pin"><span class="pin-pulse"></span></div>',
          iconSize: [44, 54],
          iconAnchor: [22, 53],
          popupAnchor: [0, -48]
        });

        marker = L.marker([safeLat, safeLon], {
          icon: redPinIcon,
          draggable: true,
          autoPan: true,
          riseOnHover: true,
          riseOffset: 700
        }).addTo(map);

        updateSelectedDot(safeLat, safeLon);

        marker.on("dragstart", function () {
          if (confirmBtn) {
            confirmBtn.disabled = true;
          }
        });

        marker.on("dragend", function () {
          const pos = marker.getLatLng();
          refreshMapSize(pos.lat, pos.lng);
          updateSelectedPin(pos.lat, pos.lng);
        });

        map.on("click", function (event) {
          marker.setLatLng(event.latlng);
          refreshMapSize(event.latlng.lat, event.latlng.lng);
          updateSelectedPin(event.latlng.lat, event.latlng.lng);
        });

        map.whenReady(function () {
          refreshMapSize(safeLat, safeLon);
        });
      } else {
        map.setView([safeLat, safeLon], 17, {
          animate: false,
          pan: false
        });

        if (marker) {
          marker.setLatLng([safeLat, safeLon]);
        }

        updateSelectedDot(safeLat, safeLon);
      }

      refreshMapSize(safeLat, safeLon);
    }

    async function buildFreshMap(lat, lon) {
      destroyMap();

      await waitForVisibleMapFrame();

      ensureMap(lat, lon);
      refreshMapSize(lat, lon);

      await wait(120);
      refreshMapSize(lat, lon);

      await wait(320);
      refreshMapSize(lat, lon);
    }

    async function confirmMapLocation() {
      if (!currentGeoData || currentLat == null || currentLon == null) {
        setMapStatus("Chưa có vị trí hợp lệ để xác nhận.", "error");
        return;
      }

      if (confirmBtn) {
        confirmBtn.disabled = true;
      }

      setMapStatus("Đang điền địa chỉ theo vị trí đã xác nhận...", "");

      const address = extractAddressObject(currentGeoData);
      const provinceCandidates = getProvinceCandidates(address);
      const wardCandidates = getWardCandidates(address);
      const detectedAddress = currentDetectedAddress || "";
      const streetAddress = currentStreetAddress || "";
      const detectedProvince = currentDetectedProvince || provinceCandidates[0] || "";

      if (latitudeInput) latitudeInput.value = String(currentLat);
      if (longitudeInput) longitudeInput.value = String(currentLon);
      if (detectedProvinceInput) detectedProvinceInput.value = detectedProvince;
      if (detectedAddressInput) detectedAddressInput.value = detectedAddress;
      if (mapConfirmedInput) mapConfirmedInput.value = "true";

      showDetectedAddressHint();

      /*
       * Không tự ghi đè ô Địa chỉ cụ thể khi xác nhận vị trí.
       * Nếu người dùng muốn dùng địa chỉ gợi ý, họ bấm nút "Dùng địa chỉ gợi ý" riêng.
       */

      if (!window.checkoutLocationSelector || !window.checkoutLocationSelector.autoFillFromCurrentLocation) {
        setMapStatus("Không tìm thấy bộ tự điền địa chỉ. Vui lòng tải lại trang và thử lại.", "error");

        if (confirmBtn) {
          confirmBtn.disabled = false;
        }

        return;
      }

      try {
        const result = await window.checkoutLocationSelector.autoFillFromCurrentLocation({
          provinceCandidates: provinceCandidates,
          wardCandidates: wardCandidates,
          streetAddress: streetAddress,
          detectedAddress: detectedAddress,
          latitude: currentLat,
          longitude: currentLon
        });

        if (!result || !result.ok) {
          setLocationError(result && result.reason ? result.reason : "Không thể khớp Tỉnh/TP từ vị trí đã chọn.");
          setMapStatus(result && result.reason ? result.reason : "Không thể khớp địa chỉ từ vị trí đã chọn.", "error");

          if (confirmBtn) {
            confirmBtn.disabled = false;
          }

          return;
        }

        setAddressError("");

        if (result.partial) {
          setLocationError("Đã xác minh Tỉnh/TP theo vị trí trên bản đồ. Vui lòng chọn lại Phường/Xã nếu hệ thống chưa nhận diện đúng.");
          useCurrentLocationBtn.textContent = "✅ Đã xác minh Tỉnh/TP";
        } else {
          setLocationError("");
          useCurrentLocationBtn.textContent = "✅ Đã xác minh vị trí";
        }

        if (window.checkoutLocationSelector.updateShippingAddress) {
          window.checkoutLocationSelector.updateShippingAddress();
        }

        if (window.updateShippingFeeByLocation) {
          window.updateShippingFeeByLocation();
        }

        if (window.updateCheckoutButtonsState) {
          window.updateCheckoutButtonsState();
        }

        closeMapModal();
      } catch (error) {
        console.error(error);
        setMapStatus("Không thể xác nhận vị trí. Vui lòng thử lại.", "error");
      } finally {
        if (confirmBtn) {
          confirmBtn.disabled = false;
        }
      }
    }

    if (addressInput) {
      addressInput.addEventListener("input", function () {
        if (useCurrentLocationBtn.textContent.includes("Đã xác minh")
                || useCurrentLocationBtn.textContent.includes("Vị trí khớp")) {
          useCurrentLocationBtn.textContent = "✅ Vị trí đã xác minh";
        }
      });
    }

    document.querySelectorAll("[data-close-address-map]").forEach(function (el) {
      el.addEventListener("click", closeMapModal);
    });

    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape" && modal.classList.contains("show")) {
        closeMapModal();
      }
    });

    window.addEventListener("resize", function () {
      if (modal.classList.contains("show")) {
        refreshMapSize(currentLat, currentLon);
      }
    });

    if (useSuggestedAddressBtn) {
      useSuggestedAddressBtn.addEventListener("click", applySuggestedAddressToInput);
    }

    if (confirmBtn) {
      confirmBtn.addEventListener("click", confirmMapLocation);
    }

    useCurrentLocationBtn.addEventListener("click", function () {
      if (!navigator.geolocation) {
        setAddressError("Trình duyệt không hỗ trợ lấy vị trí hiện tại.");
        return;
      }

      if (!window.L) {
        setAddressError("Không tải được thư viện bản đồ Leaflet. Vui lòng kiểm tra kết nối mạng và tải lại trang.");
        return;
      }

      useCurrentLocationBtn.disabled = true;
      useCurrentLocationBtn.textContent = "Đang lấy vị trí...";
      setAddressError("");
      setLocationError("");
      setMapStatus("Đang lấy vị trí hiện tại...", "");

      navigator.geolocation.getCurrentPosition(
              async function (position) {
                const lat = position.coords.latitude;
                const lon = position.coords.longitude;

                currentLat = lat;
                currentLon = lon;
                currentGeoData = null;
                currentStreetAddress = "";
                currentDetectedAddress = "";
                currentDetectedProvince = "";

                try {
                  await openMapModal();

                  setMapStatus("Đang mở bản đồ và xác định địa chỉ tại vị trí hiện tại...", "");

                  /*
                   * Fix triệt để Leaflet trong modal:
                   * 1. Mở modal trước.
                   * 2. Đợi khung bản đồ có width/height thật.
                   * 3. Xóa instance cũ.
                   * 4. Khởi tạo lại map.
                   * 5. invalidateSize nhiều lần sau khi tile bắt đầu render.
                   */
                  await buildFreshMap(lat, lon);
                  await updateSelectedPin(lat, lon);

                  refreshMapSize(lat, lon);

                  setTimeout(function () {
                    refreshMapSize(lat, lon);
                  }, 650);

                  useCurrentLocationBtn.textContent = "📍 Chọn lại vị trí";
                } catch (error) {
                  console.error(error);
                  setAddressError("Không thể mở bản đồ xác nhận vị trí. Vui lòng kiểm tra kết nối mạng hoặc thử lại sau.");
                  closeMapModal();
                  resetDetectedLocation();
                } finally {
                  useCurrentLocationBtn.disabled = false;
                }
              },
              function () {
                setAddressError("Không thể lấy vị trí hiện tại. Vui lòng cấp quyền vị trí cho trình duyệt.");
                resetDetectedLocation();
                useCurrentLocationBtn.disabled = false;
              },
              {
                enableHighAccuracy: true,
                timeout: 15000,
                maximumAge: 0
              }
      );
    });
  })();
</script>

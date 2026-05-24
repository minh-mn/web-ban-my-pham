<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/checkout.css?v=20260523_manual_coupon_validation">

<style>
  /* =========================================================
     CHECKOUT PAGE OVERRIDES
     Tone: hồng thương hiệu dịu, chữ xanh đen, nền sạch
     ========================================================= */

  :root {
    --checkout-brand: #d63384;
    --checkout-brand-dark: #b1125b;
    --checkout-brand-strong: #c21874;
    --checkout-brand-soft: #fff3f8;
    --checkout-brand-soft-2: #fff8fb;
    --checkout-brand-border: #f3b8d2;
    --checkout-good: #16a34a;
    --checkout-warning: #ff8a00;

    --checkout-text: #1f2a44;
    --checkout-text-2: #475569;
    --checkout-muted: #7b8794;
    --checkout-muted-2: #a0a8b4;
    --checkout-border: #e8edf3;
    --checkout-surface: #ffffff;
    --checkout-shadow: 0 22px 60px rgba(31, 42, 68, 0.18);
  }

  .checkout-page,
  .checkout-page *,
  .coupon-modal,
  .coupon-modal * {
    font-family: "Inter", "Segoe UI", Roboto, Arial, sans-serif;
  }

  /* ================= BUTTON STATE: NHẠT / ĐẬM ================= */

  .btn-apply-coupon,
  .btn-place-order {
    transition: background-color 0.25s ease, opacity 0.25s ease, transform 0.2s ease, box-shadow 0.2s ease;
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
    box-shadow: none !important;
  }

  .btn-apply-coupon:not(:disabled),
  .btn-place-order:not(:disabled) {
    background: linear-gradient(180deg, #df4b93 0%, var(--checkout-brand) 100%) !important;
    color: #ffffff !important;
    cursor: pointer !important;
    opacity: 1 !important;
    box-shadow: 0 10px 24px rgba(214, 51, 132, 0.2);
  }

  .btn-apply-coupon:not(:disabled):hover,
  .btn-place-order:not(:disabled):hover {
    background: linear-gradient(180deg, #cf3d84 0%, #bd256f 100%) !important;
    transform: translateY(-1px);
  }

  /* ================= DELIVERY OPTIONS + FREESHIP ================= */

  .delivery-empty {
    color: var(--checkout-muted);
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
    border: 1px solid var(--checkout-border);
    border-radius: 16px;
    cursor: pointer;
    transition: all 0.2s ease;
    background: #fff;
  }

  .delivery-option:hover,
  .delivery-option:has(input:checked) {
    border-color: var(--checkout-brand);
    background: var(--checkout-brand-soft);
  }

  .delivery-option input {
    accent-color: var(--checkout-brand);
  }

  .delivery-info {
    display: flex;
    flex-direction: column;
    flex: 1;
  }

  .delivery-info strong {
    font-size: 15px;
    color: var(--checkout-text);
  }

  .delivery-info small {
    margin-top: 4px;
    color: var(--checkout-muted);
    font-size: 13px;
  }

  .delivery-fee {
    font-weight: 800;
    color: var(--checkout-brand);
    white-space: nowrap;
  }

  .freeship-note {
    margin-top: 12px;
    padding: 12px 14px;
    border-radius: 14px;
    background: #ecfdf5;
    color: #047857;
    font-weight: 800;
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

  /* ================= COUPON BOX ================= */

  .coupon-select-btn {
    border-color: var(--checkout-border) !important;
    color: var(--checkout-text-2) !important;
    background: #ffffff !important;
    font-weight: 750 !important;
    letter-spacing: 0.01em;
    transition: all 0.2s ease;
  }

  .coupon-select-btn:hover {
    border-color: var(--checkout-brand-border) !important;
    background: var(--checkout-brand-soft-2) !important;
    color: var(--checkout-brand) !important;
    box-shadow: 0 8px 18px rgba(214, 51, 132, 0.08);
  }

  .coupon-select-btn.has-selected {
    border-color: var(--checkout-brand-border) !important;
    background: var(--checkout-brand-soft) !important;
    color: var(--checkout-text) !important;
  }

  .coupon-select-btn.has-selected .coupon-open-text {
    color: var(--checkout-brand);
    font-weight: 900;
    letter-spacing: 0.04em;
  }

  .coupon-arrow {
    color: #a1a8b3 !important;
  }

  .coupon-input-row input {
    color: var(--checkout-text) !important;
    border-color: var(--checkout-border) !important;
    font-weight: 600;
    text-transform: uppercase;
  }

  .coupon-input-row input::placeholder {
    color: #98a2b3;
    text-transform: none;
  }

  .coupon-input-row input:focus {
    border-color: var(--checkout-brand-border) !important;
    box-shadow: 0 0 0 4px rgba(214, 51, 132, 0.10) !important;
  }

  .coupon-message.success {
    color: #15803d !important;
  }

  .coupon-message.error {
    color: #dc2626 !important;
  }

  /* ================= COUPON MODAL - 1 LIST / BEST SUGGESTION ================= */

  .coupon-modal {
    position: fixed;
    inset: 0;
    z-index: 9999;
    display: none;
    align-items: center;
    justify-content: center;
    padding: 26px;
  }

  .coupon-modal.show {
    display: flex;
  }

  .coupon-modal-backdrop {
    position: absolute;
    inset: 0;
    background: rgba(15, 23, 42, 0.50);
    backdrop-filter: blur(2.5px);
  }

  .coupon-modal-dialog {
    position: relative;
    width: min(700px, calc(100vw - 32px));
    max-height: min(86vh, 780px);
    background: var(--checkout-surface);
    border-radius: 22px;
    box-shadow: var(--checkout-shadow);
    overflow: hidden;
    z-index: 1;
    border: 1px solid rgba(243, 184, 210, 0.62);
  }

  .coupon-modal-header {
    position: relative;
    min-height: 72px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px 74px;
    border-bottom: 1px solid #f0f2f5;
    background: linear-gradient(180deg, #ffffff 0%, #fff9fc 100%);
  }

  .coupon-modal-header h3 {
    margin: 0;
    color: var(--checkout-text);
    font-size: 22px;
    line-height: 1.25;
    font-weight: 900;
    text-align: center;
    letter-spacing: 0.08em;
    text-transform: uppercase;
  }

  .coupon-modal-close {
    position: absolute;
    right: 18px;
    top: 50%;
    width: 42px;
    height: 42px;
    transform: translateY(-50%);
    border: none;
    border-radius: 50%;
    background: transparent;
    color: #475569;
    cursor: pointer;
    transition: 0.18s ease;
    font-size: 0;
  }

  .coupon-modal-close::before,
  .coupon-modal-close::after {
    content: "";
    position: absolute;
    left: 12px;
    top: 20px;
    width: 18px;
    height: 2px;
    border-radius: 999px;
    background: currentColor;
  }

  .coupon-modal-close::before {
    transform: rotate(45deg);
  }

  .coupon-modal-close::after {
    transform: rotate(-45deg);
  }

  .coupon-modal-close:hover {
    background: #fff0f6;
    color: var(--checkout-brand);
  }

  .coupon-modal-body {
    max-height: calc(min(86vh, 780px) - 150px);
    overflow-y: auto;
    padding: 22px 24px 96px;
    background: #ffffff;
  }

  .coupon-modal-body::-webkit-scrollbar {
    width: 8px;
  }

  .coupon-modal-body::-webkit-scrollbar-track {
    background: transparent;
  }

  .coupon-modal-body::-webkit-scrollbar-thumb {
    background: #d6dae2;
    border-radius: 999px;
  }

  .coupon-list-title {
    margin: 4px 0 16px;
    color: var(--checkout-text);
    font-size: 15px;
    line-height: 1.35;
    font-weight: 850;
    letter-spacing: 0.01em;
  }

  .coupon-list {
    display: flex;
    flex-direction: column;
    gap: 14px;
    margin-bottom: 22px;
  }

  .coupon-item {
    width: 100%;
    min-height: 132px;
    position: relative;
    display: grid;
    grid-template-columns: 92px 1fr 54px;
    align-items: center;
    gap: 14px;
    padding: 18px 18px 18px 20px;
    border: 1.5px solid #f1c2d6;
    border-radius: 18px;
    background: linear-gradient(180deg, #fff8fb 0%, #fff3f8 100%);
    cursor: pointer;
    text-align: left;
    transition: 0.2s ease;
    color: var(--checkout-text);
    box-shadow: 0 8px 18px rgba(31, 42, 68, 0.04);
  }

  .coupon-item::before,
  .coupon-item::after {
    content: "";
    position: absolute;
    left: 92px;
    width: 14px;
    height: 14px;
    background: #ffffff;
    border: 1.5px solid #f1c2d6;
    border-radius: 50%;
    z-index: 2;
    transform: translateX(-50%);
  }

  .coupon-item::before {
    top: -8px;
  }

  .coupon-item::after {
    bottom: -8px;
  }

  .coupon-item.is-usable {
    border-color: #ee7cb2;
    background: linear-gradient(180deg, #fff8fc 0%, #fff 100%);
  }

  .coupon-item.is-usable:hover {
    border-color: var(--checkout-brand);
    box-shadow: 0 14px 30px rgba(214, 51, 132, 0.13);
    transform: translateY(-1px);
  }

  .coupon-item.is-selected {
    border-color: var(--checkout-brand);
    background: linear-gradient(180deg, #fff4f9 0%, #ffedf5 100%);
    box-shadow: 0 16px 34px rgba(214, 51, 132, 0.16);
  }

  .coupon-item.is-disabled {
    border-color: #e9edf3;
    background: #fcfcfd;
    color: #8a94a6;
    opacity: 0.58;
    cursor: not-allowed;
    box-shadow: none;
  }

  .coupon-item.is-disabled:hover {
    transform: none;
    box-shadow: none;
    border-color: #e9edf3;
  }

  .coupon-best-badge {
    position: absolute;
    top: 12px;
    right: 68px;
    left: auto;
    transform: none;
    display: none;
    align-items: center;
    justify-content: center;
    min-width: 62px;
    height: 20px;
    padding: 0 9px;
    border-radius: 999px;
    background: linear-gradient(135deg, #f4a9c8 0%, #d7659a 100%);
    color: #ffffff;
    font-size: 11px;
    line-height: 1;
    font-weight: 850;
    letter-spacing: 0.01em;
    box-shadow: 0 6px 14px rgba(215, 101, 154, 0.24);
    z-index: 4;
    pointer-events: none;
  }

  .coupon-item.is-best .coupon-best-badge {
    display: inline-flex;
  }

  .coupon-voucher-icon {
    width: 62px;
    height: 62px;
    border-radius: 50%;
    background: linear-gradient(180deg, #f8b7cc 0%, #ea91b0 100%);
    color: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 27px;
    box-shadow: inset 0 -5px 12px rgba(177, 18, 91, 0.08);
  }

  .coupon-item.is-disabled .coupon-voucher-icon {
    background: #e7eaf0;
    color: #ffffff;
  }

  .coupon-voucher-content {
    min-width: 0;
  }

  .coupon-discount-label {
    margin-bottom: 4px;
    color: var(--checkout-text-2);
    font-size: 14px;
    font-weight: 800;
  }

  .coupon-title-line {
    margin: 0;
    color: var(--checkout-text);
    font-size: 22px;
    line-height: 1.22;
    font-weight: 900;
  }

  .coupon-condition {
    margin-top: 5px;
    color: var(--checkout-muted);
    font-size: 13.5px;
    line-height: 1.45;
    font-weight: 650;
  }

  .coupon-meta-line {
    margin-top: 8px;
    display: flex;
    flex-wrap: wrap;
    gap: 6px 10px;
    align-items: center;
    color: #98a2b3;
    font-size: 13px;
    line-height: 1.4;
    font-weight: 650;
  }

  .coupon-meta-code {
    color: var(--checkout-text-2);
    font-weight: 900;
    letter-spacing: 0.08em;
    text-transform: uppercase;
  }

  .coupon-detail-link {
    border: none;
    background: transparent;
    color: #4b72d9;
    font-size: 13.5px;
    font-weight: 800;
    cursor: default;
    padding: 0;
  }

  .coupon-disabled-reason {
    display: none;
    margin-top: 7px;
    color: #b45309;
    font-size: 13px;
    line-height: 1.4;
    font-weight: 800;
  }

  .coupon-item.is-disabled .coupon-disabled-reason {
    display: block;
  }

  .coupon-ticket-right {
    width: 42px;
    height: 42px;
    justify-self: end;
    border-radius: 50%;
    border: 2px solid #efc2d8;
    background: #fff8fb;
    color: #c65b8c;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0;
    font-weight: 900;
    transition: 0.18s ease;
    box-shadow: 0 4px 10px rgba(214, 51, 132, 0.06);
  }

  .coupon-ticket-right::before {
    content: "+";
    font-size: 25px;
    line-height: 1;
    font-weight: 650;
  }

  .coupon-item.is-usable:hover .coupon-ticket-right {
    border-color: #d7659a;
    background: #fff0f6;
    color: #b1125b;
  }

  .coupon-item.is-selected .coupon-ticket-right {
    background: linear-gradient(180deg, #fde1ee 0%, #f7bfd7 100%);
    border-color: #d63384;
    color: #b1125b;
    box-shadow: 0 8px 18px rgba(214, 51, 132, 0.18);
  }

  .coupon-item.is-selected .coupon-ticket-right::before {
    content: "✓";
    font-size: 23px;
    font-weight: 950;
  }

  .coupon-item.is-disabled .coupon-ticket-right {
    background: #f6f7f9;
    border-color: #e5e8ee;
    color: #c1c7d0;
    box-shadow: none;
  }

  .coupon-empty {
    padding: 34px 20px;
    text-align: center;
    color: var(--checkout-muted);
    background: #fafafa;
    border: 1px dashed #d7dee8;
    border-radius: 18px;
  }

  .coupon-empty-icon {
    width: 58px;
    height: 58px;
    margin: 0 auto 10px;
    border-radius: 50%;
    background: #fff0f6;
    color: var(--checkout-brand);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
  }

  .coupon-modal-footer {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    min-height: 78px;
    padding: 14px 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 18px;
    background: rgba(255, 255, 255, 0.96);
    border-top: 1px solid #f0f2f5;
    box-shadow: 0 -10px 22px rgba(31, 42, 68, 0.06);
    backdrop-filter: blur(8px);
  }

  .coupon-selected-count {
    color: var(--checkout-text-2);
    font-size: 15px;
    font-weight: 750;
  }

  .coupon-selected-count strong {
    color: var(--checkout-brand);
    font-weight: 900;
  }

  .coupon-confirm-btn {
    min-width: 160px;
    height: 50px;
    border: none;
    border-radius: 12px;
    background: linear-gradient(180deg, var(--checkout-brand) 0%, var(--checkout-brand-dark) 100%);
    color: #ffffff;
    font-size: 15.5px;
    font-weight: 850;
    cursor: pointer;
    box-shadow: 0 12px 24px rgba(177, 18, 91, 0.22);
    transition: 0.18s ease;
  }

  .coupon-confirm-btn:hover {
    transform: translateY(-1px);
    box-shadow: 0 14px 28px rgba(177, 18, 91, 0.26);
  }

  body.coupon-modal-open {
    overflow: hidden;
  }

  @media (max-width: 768px) {
    .coupon-modal {
      padding: 14px;
    }

    .coupon-modal-dialog {
      width: 100%;
      border-radius: 18px;
    }

    .coupon-modal-header {
      min-height: 64px;
      padding: 17px 64px;
    }

    .coupon-modal-header h3 {
      font-size: 18px;
      letter-spacing: 0.05em;
    }

    .coupon-modal-body {
      padding: 18px 16px 92px;
    }

    .coupon-item {
      grid-template-columns: 64px 1fr 42px;
      min-height: 124px;
      gap: 12px;
      padding: 15px;
    }

    .coupon-item::before,
    .coupon-item::after {
      left: 70px;
    }

    .coupon-best-badge {
      top: 10px;
      right: 56px;
      left: auto;
      min-width: 56px;
      height: 19px;
      font-size: 10.5px;
      padding: 0 8px;
    }

    .coupon-voucher-icon {
      width: 48px;
      height: 48px;
      font-size: 23px;
    }

    .coupon-title-line {
      font-size: 18px;
    }

    .coupon-condition,
    .coupon-meta-line,
    .coupon-disabled-reason {
      font-size: 12.5px;
    }

    .coupon-modal-footer {
      padding: 12px 16px;
    }

    .coupon-confirm-btn {
      min-width: 128px;
    }
  }


  /* ================= MANUAL COUPON INPUT VALIDATION ================= */

  .coupon-input-row input.is-valid {
    border-color: #16a34a !important;
    background: #f0fdf4 !important;
    box-shadow: 0 0 0 4px rgba(22, 163, 74, 0.08) !important;
  }

  .coupon-input-row input.is-invalid {
    border-color: #ef4444 !important;
    background: #fff1f2 !important;
    box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.08) !important;
  }

  .coupon-input-row input.is-warning {
    border-color: #f59e0b !important;
    background: #fffbeb !important;
    box-shadow: 0 0 0 4px rgba(245, 158, 11, 0.08) !important;
  }

  .coupon-message.warning {
    color: #b45309 !important;
  }

  .coupon-message.success {
    color: #15803d !important;
  }

  .coupon-message.error {
    color: #dc2626 !important;
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

          <div class="field-error" id="shippingMethodError">
            <c:if test="${not empty errors.shippingMethod}">
              <c:out value="${errors.shippingMethod}" />
            </c:if>
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
        const code = couponInput.value.trim().toUpperCase();

        if (!code) {
          setCouponMessage("Vui lòng nhập mã khuyến mãi.", true);
          return;
        }

        if (window.validateManualCouponCode && !window.validateManualCouponCode(true)) {
          return;
        }

        fetch("${pageContext.request.contextPath}/ajax/apply-coupon?code=" + encodeURIComponent(code))
                .then(function (res) {
                  return res.json();
                })
                .then(function (data) {
                  const discountValue = Number(data && data.discount ? data.discount : 0);

                  if (!data || data.ok === false || data.error || discountValue <= 0) {
                    const message = data && data.error
                            ? data.error
                            : "Mã khuyến mãi không tồn tại hoặc không đủ điều kiện áp dụng.";

                    setCouponMessage(message, true);

                    if (window.clearAppliedCouponPicker) {
                      window.clearAppliedCouponPicker();
                    }

                    return;
                  }

                  if (summarySubtotal) summarySubtotal.textContent = formatVnd(data.subtotal);
                  if (summaryDiscount) summaryDiscount.textContent = formatVnd(data.discount);
                  if (summaryTotal) summaryTotal.textContent = formatVnd(data.total);

                  if (window.updateShippingFeeByLocation) {
                    window.updateShippingFeeByLocation();
                  }

                  if (window.setAppliedCouponCodeForPicker) {
                    window.setAppliedCouponCodeForPicker(data.code || code);
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

    const SHIPPING_RULES = {
      HCM: {
        ECONOMY: {
          fee: 20000,
          label: "20.000đ",
          description: "Nội thành TP.HCM: 3 - 5 ngày"
        },
        FAST: {
          fee: 35000,
          label: "35.000đ",
          description: "Nội thành TP.HCM: 1 - 3 ngày"
        },
        EXPRESS: {
          fee: 50000,
          label: "50.000đ",
          description: "Hỏa tốc trong ngày, chỉ áp dụng TP.HCM"
        }
      },
      OTHER: {
        ECONOMY: {
          fee: 35000,
          label: "35.000đ",
          description: "Ngoại tỉnh: 3 - 5 ngày"
        },
        FAST: {
          fee: 50000,
          label: "50.000đ",
          description: "Ngoại tỉnh: 1 - 3 ngày"
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
      return parseVndText(summarySubtotal ? summarySubtotal.textContent : "0");
    }

    function getDiscount() {
      return parseVndText(summaryDiscount ? summaryDiscount.textContent : "0");
    }

    function getOrderValueAfterVoucher() {
      return Math.max(getSubtotal() - getDiscount(), 0);
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

    function updateShippingMethodLabels() {
      const validLocation = hasValidLocation();
      const freeship = validLocation && isFreeShipEligible();
      let checkedInput = getSelectedShippingInput();

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
          if (descEl) {
            descEl.textContent = rule.description;
          }
          return;
        }

        if (rule.disabled) {
          feeEl.textContent = rule.label;
          if (descEl) {
            descEl.textContent = rule.description;
          }
          input.disabled = true;
          option.classList.add("is-disabled");

          if (input.checked) {
            checkedInput = document.querySelector("input[name='shippingMethod'][value='ECONOMY']");
          }
          return;
        }

        feeEl.textContent = freeship ? "Miễn phí" : rule.label;
        if (descEl) {
          descEl.textContent = freeship
                  ? "Đơn hàng đạt điều kiện miễn phí vận chuyển"
                  : rule.description;
        }
      });

      if (checkedInput && checkedInput.disabled) {
        const economy = document.querySelector("input[name='shippingMethod'][value='ECONOMY']");
        if (economy) {
          economy.checked = true;
        }
      }
    }

    function calculateShippingFee() {
      if (!hasValidLocation()) {
        return 0;
      }

      if (isFreeShipEligible()) {
        return 0;
      }

      const method = getSelectedShippingMethod();
      const rule = getRule(method);

      if (rule.disabled) {
        return 0;
      }

      return Number(rule.fee || 0);
    }

    function updateCheckoutTotal() {
      const subtotal = getSubtotal();
      const discount = getDiscount();
      const shippingFee = Number(shippingFeeInput ? shippingFeeInput.value : 0) || 0;
      const total = Math.max(subtotal - discount + shippingFee, 0);

      if (summaryTotal) {
        summaryTotal.textContent = formatVnd(total);
      }
    }

    function updateShippingDisplay() {
      const validLocation = hasValidLocation();

      updateShippingMethodLabels();

      if (!validLocation) {
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

        clearShippingError();
        updateCheckoutTotal();
        return;
      }

      if (deliveryEmpty) {
        deliveryEmpty.classList.add("hidden");
      }

      if (deliveryOptions) {
        deliveryOptions.classList.remove("hidden");
      }

      const method = getSelectedShippingMethod();
      const selectedRule = getRule(method);

      if (selectedRule.disabled) {
        const economy = document.querySelector("input[name='shippingMethod'][value='ECONOMY']");
        if (economy) {
          economy.checked = true;
        }
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
          freeshipNote.textContent = "🎉 Đơn hàng sau voucher đạt từ " + formatVnd(FREE_SHIP_THRESHOLD) + ", phí vận chuyển = 0đ.";
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
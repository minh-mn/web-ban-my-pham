<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<fmt:setLocale value="vi_VN"/>

<section class="section container">

  <h2 class="section-title">🧾 Thanh toán</h2>

  <div class="checkout-wrapper">

    <form method="post"
          action="${pageContext.request.contextPath}/checkout"
          class="checkout-card">

      <!-- ✅ CSRF (match CsrfFilter: sessionKey=CSRF_TOKEN, param=csrf_token) -->
      <input type="hidden" name="csrf_token"
             value="${sessionScope.CSRF_TOKEN}">

      <h3 class="checkout-subtitle">👤 Thông tin người nhận</h3>

      <div class="form-group">
        <label>Họ và tên</label>
        <input type="text" name="fullName" required>
      </div>

      <div class="form-group">
        <label>Số điện thoại</label>
        <input type="text" name="phone" required>
      </div>

      <div class="form-group">
        <label>Địa chỉ giao hàng</label>
        <textarea name="address" required></textarea>
      </div>

      <h3 class="checkout-subtitle">🎟 Mã khuyến mãi</h3>

      <div class="form-group">
        <input type="text"
               id="coupon-input"
               class="form-control"
               placeholder="Nhập mã giảm giá (nếu có)">
        <button type="button"
                id="apply-coupon"
                class="btn-auth"
                style="margin-top:10px">
          Áp dụng mã
        </button>
      </div>

      <div id="coupon-message" class="checkout-note"></div>

      <h3 class="checkout-subtitle">📦 Tổng kết đơn hàng</h3>

      <div class="order-summary">

        <div class="summary-row subtotal">
          <span>Tạm tính</span>
          <span id="subtotal">
            <fmt:formatNumber value="${subtotal != null ? subtotal : 0}"
                              type="number"
                              groupingUsed="true"/> ₫
          </span>
        </div>

        <div class="summary-row discount coupon"
             id="coupon-discount-row"
             style="display:none">
          <span>
            🎟 Mã khuyến mãi
            <small class="discount-percent"></small><br>
            <small class="discount-max"></small>
          </span>
          <span class="discount-value"></span>
        </div>

        <div class="summary-row discount rank">
          <span>
            🏅 Ưu đãi hạng <b>${rankLabel}</b>
            <small>(-${rankDiscountPercent}%)</small><br>
            <small>Giảm tối đa ${rankDiscountMax}</small>
          </span>
          <span class="discount-value">
            -<fmt:formatNumber value="${rankDiscount != null ? rankDiscount : 0}"
                               type="number"
                               groupingUsed="true"/> ₫
          </span>
        </div>

        <div class="summary-divider"></div>

        <div class="summary-total">
          <span>Tổng thanh toán</span>
          <strong id="total">
            <fmt:formatNumber value="${total != null ? total : 0}"
                              type="number"
                              groupingUsed="true"/> ₫
          </strong>
        </div>

      </div>

      <h3 class="checkout-subtitle">💳 Phương thức thanh toán</h3>

      <div class="payment-options">
        <label class="payment-option">
          <input type="radio" name="paymentMethod" value="COD" checked>
          <span class="custom-radio"></span>
          <div class="payment-text">
            <strong>Thanh toán khi nhận hàng (COD)</strong>
            <small>Thanh toán tiền mặt</small>
          </div>
        </label>

        <label class="payment-option">
          <input type="radio" name="paymentMethod" value="VNPAY">
          <span class="custom-radio"></span>
          <div class="payment-text">
            <strong>Thanh toán qua VNPay</strong>
            <small>Chuyển sang cổng VNPay</small>
          </div>
        </label>
      </div>

      <input type="hidden" name="couponCode" id="couponCode">

      <button type="submit" class="btn-auth">
        Xác nhận đặt hàng
      </button>

      <p class="checkout-note">🔒 Thông tin được bảo mật</p>
    </form>

  </div>
</section>

<script>
(function () {
  const btn = document.getElementById("apply-coupon");
  if (!btn) return;

  btn.addEventListener("click", function () {

    const code = document.getElementById("coupon-input").value.trim();
    const msg  = document.getElementById("coupon-message");

    if (!code) {
      msg.innerText = "Vui lòng nhập mã khuyến mãi";
      msg.style.color = "red";
      return;
    }

    fetch("${pageContext.request.contextPath}/ajax/apply-coupon?code=" + encodeURIComponent(code))
      .then(res => res.json())
      .then(data => {

        if (data.error) {
          msg.innerText = data.error;
          msg.style.color = "red";
          return;
        }

        msg.innerText = "Áp dụng mã thành công";
        msg.style.color = "green";

        document.getElementById("subtotal").innerText = data.subtotal + " ₫";
        document.getElementById("total").innerText = data.total + " ₫";
        document.getElementById("couponCode").value = code;

        const row = document.getElementById("coupon-discount-row");
        row.style.display = "flex";
        row.querySelector(".discount-value").innerText = "-" + data.discount + " ₫";
        row.querySelector(".discount-percent").innerText = "(-" + data.percent + "%)";
        row.querySelector(".discount-max").innerText = "Giảm tối đa " + data.max;
      })
      .catch(() => {
        msg.innerText = "Không thể áp dụng mã lúc này. Vui lòng thử lại.";
        msg.style.color = "red";
      });
  });
})();
</script>

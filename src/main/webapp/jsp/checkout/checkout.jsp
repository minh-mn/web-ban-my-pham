<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/checkout.css?v=20260522">

<c:set var="checkoutCart" value="${not empty selectedCart ? selectedCart : cart}" />
<c:set var="orderSubtotal" value="${not empty subTotal ? subTotal : 0}" />
<c:set var="orderDiscount" value="${not empty discount ? discount : 0}" />
<c:set var="orderTotal" value="${not empty total ? total : orderSubtotal - orderDiscount}" />

<section class="checkout-page">
  <div class="checkout-container">

    <form action="${pageContext.request.contextPath}/checkout"
          method="post"
          class="checkout-grid"
          id="checkoutForm">

      <!-- LEFT -->
      <div class="checkout-left">

        <div class="checkout-card account-card">
          <div class="checkout-card-header">
            <h2>Tài khoản</h2>

            <a href="${pageContext.request.contextPath}/logout" class="checkout-logout">
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

        <div class="checkout-card">
          <div class="checkout-card-header">
            <h2>Thông tin giao hàng</h2>
          </div>

          <div class="checkout-field">
            <input type="text"
                   name="fullName"
                   value="${not empty param.fullName ? param.fullName : sessionScope.authUser.fullName}"
                   placeholder="Họ và tên"
                   required>
          </div>

          <div class="checkout-field phone-field">
            <input type="text"
                   name="phone"
                   value="${not empty param.phone ? param.phone : sessionScope.authUser.phone}"
                   placeholder="Số điện thoại"
                   required>
            <span class="country-flag">★</span>
          </div>

          <div class="checkout-field">
            <input type="text"
                   name="country"
                   value="Vietnam"
                   placeholder="Quốc gia"
                   readonly>
          </div>

          <div class="checkout-field">
            <input type="text"
                   name="address"
                   value="${param.address}"
                   placeholder="Địa chỉ, tên đường"
                   required>
          </div>

          <div class="checkout-field no-margin">
            <input type="text"
                   name="ward"
                   value="${param.ward}"
                   placeholder="Tỉnh/TP, Phường/Xã"
                   required>
          </div>
        </div>

        <div class="checkout-card">
          <div class="checkout-card-header">
            <h2>Phương thức giao hàng</h2>
          </div>

          <div class="delivery-box">
            Nhập địa chỉ để xem các phương thức giao hàng
          </div>
        </div>

        <div class="checkout-card payment-card">
          <div class="checkout-card-header">
            <h2>Phương thức thanh toán</h2>
          </div>

          <div class="payment-list">
            <label class="payment-item">
              <input type="radio" name="paymentMethod" value="COD" checked>
              <span class="payment-dot"></span>
              <span class="payment-icon">💵</span>
              <span class="payment-text">
                                <strong>Thanh toán khi giao hàng (COD)</strong>
                                <small>Thanh toán tiền mặt khi nhận hàng</small>
                            </span>
            </label>

            <label class="payment-item">
              <input type="radio" name="paymentMethod" value="VNPAY">
              <span class="payment-dot"></span>
              <span class="payment-icon">💳</span>
              <span class="payment-text">
                                <strong>Thanh toán qua VNPAY</strong>
                                <small>Chuyển sang cổng thanh toán VNPAY</small>
                            </span>
            </label>
          </div>
        </div>

      </div>

      <!-- RIGHT -->
      <div class="checkout-right">

        <div class="checkout-card">
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
                      <div class="checkout-qty">
                        <a href="${pageContext.request.contextPath}/cart/decrease?productId=${item.productId}&key=${cartKey}">−</a>
                        <span>${item.quantity}</span>
                        <a href="${pageContext.request.contextPath}/cart/increase?productId=${item.productId}&key=${cartKey}">＋</a>
                      </div>

                      <strong class="checkout-product-subtotal">
                        <fmt:formatNumber value="${item.subtotal}" type="number" groupingUsed="true" />đ
                      </strong>
                    </div>
                  </div>
                </c:forEach>
              </c:otherwise>
            </c:choose>
          </div>
        </div>

        <div class="checkout-card coupon-card">
          <div class="checkout-card-header">
            <h2>Mã khuyến mãi</h2>
          </div>

          <button type="button" class="coupon-select-btn">
            <span>🎟 Chọn mã</span>
            <span>›</span>
          </button>

          <div class="coupon-input-row">
            <input type="text"
                   id="couponCode"
                   name="couponCode"
                   value="${param.couponCode}"
                   placeholder="Mã khuyến mãi">

            <button type="button" id="applyCouponBtn" class="btn-apply-coupon">
              Áp dụng
            </button>
          </div>

          <div id="couponMessage" class="coupon-message"></div>
        </div>

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
            <strong>-</strong>
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

          <button type="submit" class="btn-place-order">
            Đặt hàng
          </button>
        </div>

      </div>
    </form>
  </div>
</section>

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

                  setCouponMessage("Áp dụng mã giảm giá thành công.", false);
                })
                .catch(function () {
                  setCouponMessage("Không thể áp dụng mã giảm giá lúc này.", true);
                });
      });
    }
  })();
</script>
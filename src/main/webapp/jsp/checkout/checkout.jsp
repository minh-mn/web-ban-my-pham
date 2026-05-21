<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<fmt:setLocale value="vi_VN"/>

<section class="section container">

  <h2 class="section-title">🧾 Thanh toán</h2>

  <div class="checkout-wrapper">

    <form method="post"
          action="${pageContext.request.contextPath}/checkout"
          class="checkout-card">

      <!-- CSRF match CsrfFilter: sessionKey=CSRF_TOKEN, param=csrf_token -->
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

      <!-- ========================================================= -->
      <!-- COUPON SECTION - ISSUE 110 -->
      <!-- ========================================================= -->
      <h3 class="checkout-subtitle">🎟 Mã khuyến mãi</h3>

      <div class="form-group">
        <label>Mã giảm giá</label>
        <input type="text"
               id="coupon-input"
               class="form-control"
               placeholder="Nhập mã giảm giá hoặc chọn mã bên dưới">

        <button type="button"
                id="apply-coupon"
                class="btn-auth checkout-coupon-apply-btn">
          Áp dụng mã
        </button>
      </div>

      <div id="coupon-message" class="checkout-note"></div>

      <!-- AVAILABLE COUPONS BY USER RANK -->
      <div class="checkout-coupon-panel">

        <div class="checkout-coupon-panel__head">
          <div>
            <h4 class="checkout-coupon-title">Mã giảm giá dành cho bạn</h4>
            <p class="checkout-coupon-subtitle">
              Danh sách mã còn hiệu lực phù hợp với hạng khách hàng hiện tại.
            </p>
          </div>

          <c:if test="${not empty rankLabel}">
            <span class="checkout-rank-chip">
              Rank: <c:out value="${rankLabel}" />
            </span>
          </c:if>
        </div>

        <c:choose>
          <c:when test="${not empty availableCoupons}">
            <div class="checkout-coupon-list">
              <c:forEach var="coupon" items="${availableCoupons}">
                <div class="checkout-coupon-item">

                  <div class="checkout-coupon-item__top">
                    <div class="checkout-coupon-code">
                      🏷 <c:out value="${coupon.code}" />
                    </div>

                    <button type="button"
                            class="checkout-coupon-use-btn"
                            data-coupon-code="<c:out value='${coupon.code}'/>">
                      Dùng mã
                    </button>
                  </div>

                  <div class="checkout-coupon-discount">
                    Giảm <c:out value="${coupon.discountPercent}" />%
                  </div>

                  <div class="checkout-coupon-info">

                    <div>
                      <strong>Điều kiện:</strong>
                      <c:choose>
                        <c:when test="${coupon.minOrderAmount > 0}">
                          Đơn từ
                          <fmt:formatNumber value="${coupon.minOrderAmount}"
                                            type="number"
                                            groupingUsed="true"
                                            maxFractionDigits="0"/> ₫
                        </c:when>
                        <c:otherwise>
                          Không yêu cầu giá trị đơn tối thiểu
                        </c:otherwise>
                      </c:choose>
                    </div>

                    <div>
                      <strong>Giảm tối đa:</strong>
                      <c:choose>
                        <c:when test="${coupon.maxDiscountAmount > 0}">
                          <fmt:formatNumber value="${coupon.maxDiscountAmount}"
                                            type="number"
                                            groupingUsed="true"
                                            maxFractionDigits="0"/> ₫
                        </c:when>
                        <c:otherwise>
                          Không giới hạn
                        </c:otherwise>
                      </c:choose>
                    </div>

                    <div>
                      <strong>Hạn dùng:</strong>
                      <c:choose>
                        <c:when test="${not empty coupon.endDate}">
                          <fmt:formatDate value="${coupon.endDate}" pattern="dd/MM/yyyy HH:mm"/>
                        </c:when>
                        <c:otherwise>
                          Không giới hạn
                        </c:otherwise>
                      </c:choose>
                    </div>

                    <c:if test="${coupon.remainingUses >= 0}">
                      <div>
                        <strong>Còn lượt:</strong>
                        <c:out value="${coupon.remainingUses}" />
                      </div>
                    </c:if>

                  </div>

                  <div class="checkout-coupon-rank">
                    Dành cho: <c:out value="${coupon.minRankLabel}" /> trở lên
                  </div>

                </div>
              </c:forEach>
            </div>
          </c:when>

          <c:otherwise>
            <p class="checkout-note">
              Hiện chưa có mã giảm giá phù hợp với hạng khách hàng của bạn.
            </p>
          </c:otherwise>
        </c:choose>

      </div>

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
             hidden>
          <span>
            🎟 Mã khuyến mãi
            <small class="discount-percent"></small><br>
            <small class="discount-max"></small>
          </span>
          <span class="discount-value"></span>
        </div>

        <c:if test="${not empty rankDiscountPercent and rankDiscountPercent > 0}">
          <div class="summary-row discount rank">
            <span>
              🏅 Ưu đãi hạng <b><c:out value="${rankLabel}" /></b>
              <small>(-<c:out value="${rankDiscountPercent}" />%)</small><br>
              <small>
                Giảm tối đa
                <c:choose>
                  <c:when test="${not empty rankDiscountMax}">
                    <c:out value="${rankDiscountMax}" />
                  </c:when>
                  <c:otherwise>không giới hạn</c:otherwise>
                </c:choose>
              </small>
            </span>

            <span class="discount-value" id="rank-discount-value">
              -<fmt:formatNumber value="${rankDiscount != null ? rankDiscount : 0}"
                                 type="number"
                                 groupingUsed="true"/> ₫
            </span>
          </div>
        </c:if>

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
    const applyButton = document.getElementById("apply-coupon");
    const couponInput = document.getElementById("coupon-input");
    const couponMessage = document.getElementById("coupon-message");
    const couponCodeField = document.getElementById("couponCode");

    if (!applyButton || !couponInput || !couponMessage || !couponCodeField) {
      return;
    }

    function formatVnd(value) {
      if (value === null || value === undefined || value === "") {
        return "0 ₫";
      }

      const rawValue = String(value);

      if (rawValue.includes("₫")) {
        return rawValue;
      }

      const numericValue = Number(rawValue.replace(/[^\d.-]/g, ""));

      if (!Number.isFinite(numericValue)) {
        return rawValue + " ₫";
      }

      return new Intl.NumberFormat("vi-VN").format(numericValue) + " ₫";
    }

    function setMessage(text, type) {
      couponMessage.innerText = text;
      couponMessage.classList.remove("checkout-message-success", "checkout-message-error");

      if (type === "success") {
        couponMessage.classList.add("checkout-message-success");
      }

      if (type === "error") {
        couponMessage.classList.add("checkout-message-error");
      }
    }

    function applyCouponCode(rawCode) {
      const code = (rawCode || "").trim();

      if (!code) {
        setMessage("Vui lòng nhập mã khuyến mãi", "error");
        return;
      }

      fetch("${pageContext.request.contextPath}/ajax/apply-coupon?code=" + encodeURIComponent(code))
              .then(function (res) {
                return res.json();
              })
              .then(function (data) {

                if (data.error) {
                  setMessage(data.error, "error");
                  couponCodeField.value = "";
                  return;
                }

                setMessage("Áp dụng mã thành công", "success");

                couponInput.value = code;
                couponCodeField.value = code;

                if (data.subtotal !== undefined) {
                  document.getElementById("subtotal").innerText = formatVnd(data.subtotal);
                }

                if (data.total !== undefined) {
                  document.getElementById("total").innerText = formatVnd(data.total);
                }

                if (data.rankDiscount !== undefined) {
                  const rankDiscountValue = document.getElementById("rank-discount-value");

                  if (rankDiscountValue) {
                    rankDiscountValue.innerText = "-" + formatVnd(data.rankDiscount);
                  }
                }

                const couponRow = document.getElementById("coupon-discount-row");

                if (couponRow) {
                  couponRow.hidden = false;

                  const discountValue = couponRow.querySelector(".discount-value");
                  const discountPercent = couponRow.querySelector(".discount-percent");
                  const discountMax = couponRow.querySelector(".discount-max");

                  if (discountValue) {
                    discountValue.innerText = "-" + formatVnd(data.discount || 0);
                  }

                  if (discountPercent) {
                    discountPercent.innerText = data.percent !== undefined
                            ? "(-" + data.percent + "%)"
                            : "";
                  }

                  if (discountMax) {
                    discountMax.innerText = data.max
                            ? "Giảm tối đa " + formatVnd(data.max)
                            : "";
                  }
                }
              })
              .catch(function () {
                setMessage("Không thể áp dụng mã lúc này. Vui lòng thử lại.", "error");
              });
    }

    applyButton.addEventListener("click", function () {
      applyCouponCode(couponInput.value);
    });

    document.querySelectorAll(".checkout-coupon-use-btn").forEach(function (button) {
      button.addEventListener("click", function () {
        applyCouponCode(button.dataset.couponCode);
      });
    });
  })();
</script>
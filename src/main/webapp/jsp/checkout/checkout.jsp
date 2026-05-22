<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/checkout.css?v=20260522_3">

<c:set var="checkoutCart" value="${not empty selectedCart ? selectedCart : cart}" />

<c:set var="orderSubtotal"
       value="${not empty subTotal ? subTotal : (not empty subtotal ? subtotal : 0)}" />

<c:set var="orderDiscount"
       value="${not empty discount ? discount : (not empty discountAmount ? discountAmount : 0)}" />

<c:set var="orderTotal"
       value="${not empty total ? total : (not empty totalAmount ? totalAmount : orderSubtotal - orderDiscount)}" />

<section class="checkout-page">
  <div class="checkout-container">

    <form action="${pageContext.request.contextPath}/checkout"
          method="post"
          class="checkout-grid"
          id="checkoutForm">

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

          <div class="checkout-field">
            <label for="fullName">Họ và tên</label>
            <input type="text"
                   id="fullName"
                   name="fullName"
                   value="${not empty param.fullName ? param.fullName : sessionScope.authUser.fullName}"
                   placeholder="Nhập họ và tên"
                   required>
          </div>

          <div class="checkout-field phone-field">
            <label for="phone">Số điện thoại</label>

            <div class="phone-input-wrap">
              <input type="text"
                     id="phone"
                     name="phone"
                     value="${not empty param.phone ? param.phone : sessionScope.authUser.phone}"
                     placeholder="Nhập số điện thoại"
                     required>

              <span class="country-flag">★</span>
            </div>
          </div>

          <div class="checkout-field">
            <label for="country">Quốc gia</label>
            <input type="text"
                   id="country"
                   name="country"
                   value="Vietnam"
                   readonly>
          </div>

          <div class="checkout-field">
            <label for="address">Địa chỉ, tên đường</label>
            <input type="text"
                   id="address"
                   name="address"
                   value="${param.address}"
                   placeholder="Địa chỉ, tên đường"
                   required>
          </div>

          <!-- TỈNH/TP - PHƯỜNG/XÃ -->
          <div class="checkout-field no-margin location-field" id="locationField">
            <label for="locationInput">Tỉnh/TP, Phường/Xã</label>

            <input type="text"
                   id="locationInput"
                   name="locationText"
                   value="${param.locationText}"
                   placeholder="Tỉnh/TP, Phường/Xã"
                   autocomplete="off"
                   readonly
                   required>

            <input type="hidden" id="provinceInput" name="province" value="${param.province}">
            <input type="hidden" id="provinceCodeInput" name="provinceCode" value="${param.provinceCode}">
            <input type="hidden" id="wardInput" name="wardName" value="${param.wardName}">
            <input type="hidden" id="wardCodeInput" name="wardCode" value="${param.wardCode}">
            <input type="hidden" id="shippingAddressInput" name="shippingAddress" value="${param.shippingAddress}">

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
          </div>
        </div>

        <!-- DELIVERY METHOD -->
        <div class="checkout-card delivery-card">
          <div class="checkout-card-header">
            <h2>Phương thức giao hàng</h2>
          </div>

          <div class="delivery-box" id="deliveryBox">
            Nhập địa chỉ để xem các phương thức giao hàng
          </div>
        </div>

        <!-- PAYMENT METHOD -->
        <div class="checkout-card payment-card">
          <div class="checkout-card-header">
            <h2>Phương thức thanh toán</h2>
          </div>

          <div class="payment-list">
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

          <button type="button" class="coupon-select-btn">
            <span>🎟 Chọn mã</span>
            <span class="coupon-arrow">›</span>
          </button>

          <div class="coupon-input-row">
            <input type="text"
                   id="couponCode"
                   name="couponCode"
                   value="${param.couponCode}"
                   placeholder="Mã khuyến mãi">

            <button type="button"
                    id="applyCouponBtn"
                    class="btn-apply-coupon">
              Áp dụng
            </button>
          </div>

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

<script>
  (function () {
    const contextPath = "${pageContext.request.contextPath}";

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

          updateShippingAddress();
          updateDeliveryText();

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
        deliveryBox.textContent = "Giao hàng tiêu chuẩn";
        deliveryBox.classList.add("available");
      } else {
        deliveryBox.textContent = "Nhập địa chỉ để xem các phương thức giao hàng";
        deliveryBox.classList.remove("available");
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
  })();
</script>
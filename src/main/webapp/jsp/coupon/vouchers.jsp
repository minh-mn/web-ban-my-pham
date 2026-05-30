<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%--
  Trang này được render bởi VouchersController thông qua base.jsp.
  Controller truyền:
  - allVouchers: List<Coupon>
  - savedCodes: chuỗi dạng ,CODE1,CODE2,
--%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/voucher.css?v=20260530">

<section class="voucher-page">
  <div class="voucher-container">

    <div class="voucher-hero">
      <span class="voucher-hero-kicker">MyCosmetic Voucher</span>
      <h1>Tất cả ưu đãi dành cho bạn</h1>
      <p>
        Sưu tầm mã giảm giá, áp dụng cho đơn hàng mỹ phẩm yêu thích và tận hưởng ưu đãi
        với phong cách đỏ đậm kết hợp hồng pastel nhẹ nhàng.
      </p>
    </div>

    <div class="voucher-toolbar">
      <div>
        <span class="voucher-section-label">Ưu đãi đang hoạt động</span>
        <h2>Voucher nổi bật</h2>
      </div>
      <div class="voucher-toolbar-note">
        <span></span>
        Chọn mã phù hợp trước khi thanh toán
      </div>
    </div>

    <c:choose>
      <c:when test="${empty allVouchers}">
        <div class="voucher-empty">
          <div class="voucher-empty-icon">%</div>
          <h3>Chưa có voucher khả dụng</h3>
          <p>Hiện chưa có mã ưu đãi nào đang hoạt động. Vui lòng quay lại sau.</p>
        </div>
      </c:when>

      <c:otherwise>
        <div class="voucher-grid">
          <c:forEach var="voucher" items="${allVouchers}">
            <c:set var="isSaved" value="${not empty savedCodes and fn:contains(savedCodes, voucher.code)}" />
            <c:set var="isRankVoucher" value="${not empty voucher.type and voucher.type eq 'RANK'}" />
            <c:set var="isReviewVoucher" value="${not empty voucher.type and voucher.type eq 'REVIEW_REWARD'}" />
            <c:set var="scope" value="${voucher.applyScope}" />

            <article class="voucher-card">
              <div class="voucher-card-left">
                <div class="voucher-logo-mark">
                  <span>MC</span>
                  <small>BEAUTY</small>
                </div>
              </div>

              <div class="voucher-card-divider"></div>

              <div class="voucher-card-body">
                <div class="voucher-card-top">
                  <div class="voucher-tags">
                    <span class="voucher-tag voucher-tag-hot">HOT</span>
                    <c:choose>
                      <c:when test="${isRankVoucher}">
                        <span class="voucher-tag voucher-tag-soft">Hạng thành viên</span>
                      </c:when>
                      <c:when test="${isReviewVoucher}">
                        <span class="voucher-tag voucher-tag-soft">Quà đánh giá</span>
                      </c:when>
                      <c:otherwise>
                        <span class="voucher-tag voucher-tag-soft">Ưu đãi</span>
                      </c:otherwise>
                    </c:choose>
                  </div>

                  <button type="button"
                          class="voucher-condition-btn"
                          onclick="showVoucherDetailFromEl(this)"
                          data-code="<c:out value='${voucher.code}'/>"
                          data-desc="<c:out value='${not empty voucher.description ? voucher.description : "Không có mô tả"}'/>"
                          data-min="${voucher.minOrderAmount}"
                          data-end="${voucher.endDate}">
                    Điều kiện
                  </button>
                </div>

                <div class="voucher-code-row">
                  <h3><c:out value="${voucher.code}"/></h3>

                  <c:choose>
                    <c:when test="${isSaved}">
                      <button type="button"
                              class="voucher-save-btn saved"
                              data-code="<c:out value='${voucher.code}'/>"
                              data-loggedin="${not empty sessionScope.user}"
                              disabled>
                        Đã lưu
                      </button>
                    </c:when>
                    <c:otherwise>
                      <button type="button"
                              class="voucher-save-btn"
                              onclick="saveVoucher(this)"
                              data-code="<c:out value='${voucher.code}'/>"
                              data-loggedin="${not empty sessionScope.user}">
                        Lưu mã
                      </button>
                    </c:otherwise>
                  </c:choose>
                </div>

                <div class="voucher-discount-line">
                  <c:choose>
                    <c:when test="${voucher.type eq 'FREESHIP'}">
                      Miễn phí vận chuyển
                    </c:when>
                    <c:when test="${voucher.percentDiscount}">
                      Giảm <strong>${voucher.discountPercent}%</strong>
                    </c:when>
                    <c:otherwise>
                      Giảm <strong><fmt:formatNumber value="${voucher.discountValue}" type="number"/>đ</strong>
                    </c:otherwise>
                  </c:choose>
                </div>

                <div class="voucher-meta-list">
                  <div class="voucher-meta-item">
                    <span class="voucher-dot"></span>
                    <span>
                      Đơn hàng từ
                      <strong><fmt:formatNumber value="${voucher.minOrderAmount}" type="number"/>đ</strong>
                    </span>
                  </div>

                  <c:if test="${not empty voucher.maxDiscountAmount and voucher.maxDiscountAmount > 0}">
                    <div class="voucher-meta-item">
                      <span class="voucher-dot"></span>
                      <span>
                        Giảm tối đa
                        <strong><fmt:formatNumber value="${voucher.maxDiscountAmount}" type="number"/>đ</strong>
                      </span>
                    </div>
                  </c:if>

                  <div class="voucher-meta-item">
                    <span class="voucher-dot"></span>
                    <span>
                      Áp dụng:
                      <strong>
                        <c:choose>
                          <c:when test="${empty scope or scope eq 'ALL'}">Tất cả sản phẩm</c:when>
                          <c:when test="${scope eq 'BRAND'}">Theo thương hiệu</c:when>
                          <c:when test="${scope eq 'PRODUCTS'}">Sản phẩm chỉ định</c:when>
                          <c:otherwise><c:out value="${scope}"/></c:otherwise>
                        </c:choose>
                      </strong>
                    </span>
                  </div>

                  <c:if test="${not empty voucher.minRankCode and voucher.minRankCode ne 'MEMBER'}">
                    <div class="voucher-meta-item">
                      <span class="voucher-dot"></span>
                      <span>Hạng tối thiểu: <strong><c:out value="${voucher.minRankCode}"/></strong></span>
                    </div>
                  </c:if>
                </div>

                <c:if test="${not empty voucher.description}">
                  <p class="voucher-desc"><c:out value="${voucher.description}"/></p>
                </c:if>

                <div class="voucher-card-footer">
                  <div class="voucher-expire">
                    HSD: <strong><c:out value="${voucher.endDate}"/></strong>
                  </div>

                  <button type="button"
                          class="voucher-detail-btn"
                          onclick="showVoucherDetailFromEl(this)"
                          data-code="<c:out value='${voucher.code}'/>"
                          data-desc="<c:out value='${not empty voucher.description ? voucher.description : "Không có mô tả"}'/>"
                          data-min="${voucher.minOrderAmount}"
                          data-end="${voucher.endDate}">
                    Xem chi tiết
                  </button>
                </div>
              </div>
            </article>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</section>

<script>
  function showCustomAlert(title, message, isSuccess) {
    const oldModal = document.querySelector(".custom-alert-modal");
    if (oldModal) oldModal.remove();

    const modal = document.createElement("div");
    modal.className = "custom-alert-modal";
    const icon = isSuccess ? "🎉" : "⚠️";

    modal.innerHTML =
            '<div class="custom-alert-box">' +
            '<div class="custom-alert-icon">' + icon + '</div>' +
            '<h3>' + title + '</h3>' +
            '<p>' + message + '</p>' +
            '<button type="button" onclick="this.closest(\'.custom-alert-modal\').remove()">Đóng</button>' +
            '</div>';

    document.body.appendChild(modal);
  }

  function saveVoucher(btn) {
    const code = btn.getAttribute('data-code');
    const isLoggedIn = btn.getAttribute('data-loggedin') === 'true';

    if (!isLoggedIn) {
      showCustomAlert("Chưa đăng nhập", "Vui lòng đăng nhập để lưu mã!", false);
      return;
    }

    btn.innerText = "Đang lưu...";
    btn.disabled = true;

    fetch(window.APP_CTX + '/ajax/apply-coupon?code=' + encodeURIComponent(code) + '&action=save')
            .then(res => res.json())
            .then(data => {
              const msg = data.message ? data.message.toLowerCase() : "";

              if (data.success || msg.includes("đã sở hữu") || msg.includes("đã lưu")) {
                btn.innerText = "Đã lưu";
                btn.classList.add("saved");
                showCustomAlert("Thông báo", data.success ? "Lưu mã thành công!" : "Mã này đã có trong ví của bạn.", true);
              } else {
                btn.disabled = false;
                btn.innerText = "Lưu mã";
                showCustomAlert("Lưu thất bại", data.message || "Không thể lưu mã, vui lòng thử lại.", false);
              }
            })
            .catch(() => {
              btn.disabled = false;
              btn.innerText = "Lưu mã";
              showCustomAlert("Lỗi", "Có lỗi kết nối, vui lòng thử lại.", false);
            });
  }

  function showVoucherDetailFromEl(btn) {
    const code = btn.getAttribute('data-code') || "Không rõ";
    const desc = btn.getAttribute('data-desc') || "Không có mô tả cụ thể.";
    const min = btn.getAttribute('data-min');
    const end = btn.getAttribute('data-end');

    let minText = "0đ";
    if (min && Number(min) > 0) {
      minText = Number(min).toLocaleString('vi-VN') + "đ";
    }

    const endText = end && end.trim() !== "" ? end : "Không giới hạn";

    const oldModal = document.querySelector(".custom-alert-modal");
    if (oldModal) oldModal.remove();

    const modal = document.createElement("div");
    modal.className = "custom-alert-modal";

    modal.innerHTML =
            '<div class="custom-alert-box voucher-detail-modal">' +
            '<div class="custom-alert-icon">🎟️</div>' +
            '<h3>Chi tiết ưu đãi</h3>' +
            '<div class="voucher-modal-content">' +
            '<p><strong>Mã code:</strong> <span>' + code + '</span></p>' +
            '<p><strong>Mô tả:</strong> ' + desc + '</p>' +
            '<p><strong>Đơn tối thiểu:</strong> ' + minText + '</p>' +
            '<p><strong>Hạn sử dụng:</strong> ' + endText + '</p>' +
            '</div>' +
            '<button type="button" onclick="this.closest(\'.custom-alert-modal\').remove()">Đã hiểu</button>' +
            '</div>';

    document.body.appendChild(modal);
  }
</script>

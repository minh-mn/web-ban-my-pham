<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<section class="section" style="min-height: 60vh; padding-top: 40px; padding-bottom: 40px;">
  <div class="container">
    <h2 style="text-align: center; margin-bottom: 30px; font-weight: 800; color: #ff5fa2;">TẤT CẢ ƯU ĐÃI DÀNH CHO BẠN</h2>

    <div class="voucher-grid">
      <c:forEach var="voucher" items="${allVouchers}">
        <div class="voucher-card">
          <div class="voucher-left">
              ${voucher.type == 'FREESHIP' ? '🚚' : '🎟'}
          </div>

          <div class="voucher-right">
            <div>
              <span class="v-code">${voucher.code}</span>
              <span class="v-discount">
                           <c:choose>
                             <c:when test="${voucher.type == 'FREESHIP'}">Miễn phí Ship</c:when>
                             <c:otherwise>Giảm ${voucher.discountPercent}%</c:otherwise>
                           </c:choose>
                           </span>

              <div class="v-info-text">
                <div>Đơn tối thiểu: <b><fmt:formatNumber value="${voucher.minOrderAmount}" type="number"/>đ</b></div>
                <div>Áp dụng: <b>${not empty voucher.applicableProducts ? voucher.applicableProducts : 'Tất cả sản phẩm'}</b></div>
                <div>HSD: <b>${voucher.endDate}</b></div>
              </div>
            </div>

            <div class="btn-wrapper">
              <button type="button" class="btn-detail"
                      onclick="showVoucherDetailFromEl(this)"
                      data-code="${voucher.code}"
                      data-desc="${not empty voucher.description ? voucher.description : 'Không có mô tả'}"
                      data-min="${voucher.minOrderAmount}"
                      data-end="${voucher.endDate}">
                Xem chi tiết
              </button>
              <button class="btn-save" onclick="saveVoucher(this)" data-code="${voucher.code}" data-loggedin="${not empty sessionScope.user}">
                Lưu mã
              </button>
            </div>
          </div>
        </div>
      </c:forEach>
    </div>
  </div>
</section>

<script>
  function showCustomAlert(title, message, isSuccess) {
    const modal = document.createElement("div");
    modal.className = "custom-alert-modal";
    const icon = isSuccess ? "🎉" : "⚠️";
    const color = isSuccess ? "#ff5fa2" : "#e53935";
    modal.innerHTML =
            '<div class="custom-alert-box">' +
            '<div style="font-size: 40px; margin-bottom: 10px;">' + icon + '</div>' +
            '<h3 style="color:' + color + '; margin-bottom: 10px; font-size: 20px;">' + title + '</h3>' +
            '<p style="color: #555; margin-bottom: 20px; line-height: 1.5;">' + message + '</p>' +
            '<button onclick="this.closest(\'.custom-alert-modal\').remove()" ' +
            'style="background: ' + color + '; color: #fff; border: none; padding: 10px 24px; border-radius: 999px; cursor: pointer; font-weight: bold; width: 100%;">Đóng</button>' +
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

    // THÊM &action=save ĐỂ BÁO VỚI SERVER LÀ CHỈ LƯU VÀO VÍ
    fetch(window.APP_CTX + '/ajax/apply-coupon?code=' + encodeURIComponent(code) + '&action=save')
            .then(res => res.json())
            .then(data => {
              const msg = data.message ? data.message.toLowerCase() : "";

              // Nếu thành công HOẶC đã sở hữu từ trước -> Đổi trạng thái nút thành "Đã lưu"
              if (data.success || msg.includes("đã sở hữu") || msg.includes("đã lưu")) {
                btn.innerText = "Đã lưu";
                btn.classList.add("saved");
                btn.style.backgroundColor = "#ccc";
                btn.style.cursor = "not-allowed";
                showCustomAlert("Thông báo", data.success ? "Lưu mã thành công!" : "Mã này đã có trong ví của bạn.", true);
              } else {
                // Nếu lỗi khác (không phải lỗi trùng), trả lại trạng thái nút
                btn.disabled = false;
                btn.innerText = "Lưu mã";
                showCustomAlert("Lưu thất bại", data.message, false);
              }
            })
            .catch(err => {
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

    let minText = "0 ₫";
    if (min && parseInt(min) > 0) {
      minText = Number(min).toLocaleString('vi-VN') + " ₫";
    }

    let endText = "Không giới hạn";
    if (end && end.trim() !== "") {
      endText = end;
    }

    const modal = document.createElement("div");
    modal.className = "custom-alert-modal";

    modal.innerHTML =
            '<div class="custom-alert-box" style="text-align: left;">' +
            '<div style="font-size: 32px; text-align: center; margin-bottom: 10px;">🎟️</div>' +
            '<h3 style="color: #ff5fa2; margin-bottom: 15px; font-size: 20px; text-align: center;">Chi tiết Ưu đãi</h3>' +
            '<div style="color: #444; line-height: 1.6; font-size: 14px; margin-bottom: 24px; padding: 15px; background: #fff0f6; border-radius: 12px;">' +
            '<p style="margin: 0 0 8px 0;"><strong>Mã code:</strong> <span style="color: #d0021b; font-weight: bold; font-size: 16px;">' + code + '</span></p>' +
            '<p style="margin: 0 0 8px 0;"><strong>Mô tả:</strong> ' + desc + '</p>' +
            '<p style="margin: 0 0 8px 0;"><strong>Đơn tối thiểu:</strong> ' + minText + '</p>' +
            '<p style="margin: 0;"><strong>Hạn sử dụng:</strong> ' + endText + '</p>' +
            '</div>' +
            '<button onclick="this.closest(\'.custom-alert-modal\').remove()" ' +
            'style="background: linear-gradient(135deg, #ff5fa2, #ff85bc); color: #fff; border: none; padding: 12px 24px; border-radius: 999px; cursor: pointer; font-weight: bold; width: 100%; transition: 0.2s;">' +
            'Đã hiểu' +
            '</button>' +
            '</div>';

    document.body.appendChild(modal);
  }
</script>
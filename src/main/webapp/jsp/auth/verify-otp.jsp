<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Xác Thực Mã OTP - MyCosmetic</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/register.css?v=999">
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head>
<body>
<section class="auth-page">
  <div class="auth-card">
    <h2 class="auth-title">Xác Thực OTP</h2>
    <p class="auth-subtitle">Chúng tôi đã gửi mã OTP đến email: <br>
      <strong style="color: #ff5fa2;">${sessionScope.pendingUser.email}</strong>
    </p>

    <form id="otpForm" method="post" action="${pageContext.request.contextPath}/verify-registration" class="auth-form">
      <div class="form-group">
        <label for="otp_input">Nhập mã xác thực (6 số)</label>
        <div class="input-group">
          <input type="text" id="otp_input" name="otp_input" placeholder="••••••" maxlength="6" required autocomplete="off"
                 style="text-align: center; font-size: 24px; letter-spacing: 6px; font-weight: 800; color: #ff5fa2; width: 100%;">
        </div>
      </div>

      <!-- KHỐI ĐẾM NGƯỢC & NÚT GỬI LẠI MÃ OTP -->
      <div style="text-align: center; margin-top: 15px; margin-bottom: 5px;">
        <p id="otpCountdownText" style="color: #666666; font-size: 13px; margin: 0 0 8px 0;">
          Yêu cầu gửi lại mã sau: <span id="otpTimer" style="color: #ff5fa2; font-weight: bold;">30</span> giây
        </p>
        <button type="button" id="btnResendOtp" onclick="guiLaiOtp()" disabled
                style="background: #e0e0e0; color: #999999; border: none; padding: 8px 18px; border-radius: 20px; font-size: 13px; font-weight: bold; cursor: not-allowed; transition: all 0.3s; display: inline-block;">
          Gửi lại mã OTP 🔄
        </button>
      </div>

      <button type="submit" class="btn-auth" style="background-color: #ff5fa2; color: white; border: none; padding: 12px; border-radius: 50px; font-weight: bold; width: 100%; cursor: pointer; margin-top: 15px;">
        Hoàn Tất Đăng Ký
      </button>
    </form>
  </div>
</section>

<script>
  const contextPath = "${pageContext.request.contextPath}";
  let countdownInterval;

  // Hàm điều khiển bộ đếm ngược 30 giây
  function startOtpCountdown() {
    let timeLeft = 30;
    const timerSpan = document.getElementById("otpTimer");
    const btnResend = document.getElementById("btnResendOtp");
    const countdownText = document.getElementById("otpCountdownText");

    // Đưa nút về trạng thái khóa (disabled) khi bắt đầu đếm ngược
    btnResend.disabled = true;
    btnResend.style.background = "#e0e0e0";
    btnResend.style.color = "#999999";
    btnResend.style.border = "none";
    btnResend.style.cursor = "not-allowed";
    countdownText.style.display = "block";
    timerSpan.innerText = timeLeft;

    clearInterval(countdownInterval);
    countdownInterval = setInterval(() => {
      timeLeft--;
      timerSpan.innerText = timeLeft;

      if (timeLeft <= 0) {
        clearInterval(countdownInterval);
        countdownText.style.display = "none"; // Ẩn dòng chữ đếm giây

        // Mở khóa nút bấm để người dùng có thể click kích hoạt lại
        btnResend.disabled = false;
        btnResend.style.background = "#ffffff";
        btnResend.style.color = "#ff5fa2";
        btnResend.style.border = "1px solid #ff5fa2";
        btnResend.style.cursor = "pointer";
      }
    }, 1000);
  }

  // Tự động kích hoạt đếm ngược ngay khi người dùng vừa chuyển hướng đến trang này
  document.addEventListener("DOMContentLoaded", function() {
    startOtpCountdown();
  });

  // Hàm gọi API gửi lại mã OTP mới về Email
  function guiLaiOtp() {
    Swal.fire({
      title: 'Đang gửi lại mã...',
      allowOutsideClick: false,
      didOpen: () => { Swal.showLoading(); }
    });

    fetch(contextPath + '/resend-otp', {
      method: 'POST'
    })
            .then(res => res.json())
            .then(data => {
              Swal.close();
              if (data.status === 'success') {
                Swal.fire({
                  icon: 'success',
                  title: 'Thành công',
                  text: 'Mã OTP xác thực mới đã được gửi thành công vào email của bạn!',
                  confirmButtonColor: '#ff5fa2'
                });
                startOtpCountdown(); // Chạy lại một chu kỳ đếm ngược 30s mới
              } else {
                Swal.fire({
                  icon: 'error',
                  title: 'Thất bại',
                  text: data.message,
                  confirmButtonColor: '#ff5fa2'
                });
              }
            })
            .catch(err => {
              Swal.close();
              console.error("Error resending OTP:", err);
              Swal.fire({
                icon: 'error',
                title: 'Lỗi Kết Nối',
                text: 'Không thể kết nối với máy chủ để gửi lại mã!',
                confirmButtonColor: '#ff5fa2'
              });
            });
  }

  // Logic xác thực Form mã OTP sẵn có của bạn
  document.getElementById("otpForm").addEventListener("submit", function(e) {
    e.preventDefault();

    const otpValue = document.getElementById("otp_input").value.trim();

    Swal.fire({
      title: 'Đang xác thực...',
      didOpen: () => { Swal.showLoading(); },
      allowOutsideClick: false
    });

    fetch(contextPath + '/verify-registration', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ 'otp_input': otpValue })
    })
            .then(res => res.json())
            .then(data => {
              Swal.close();

              if (data.status === 'success') {
                Swal.fire({
                  icon: 'success',
                  title: 'Đăng Ký Thành Công!',
                  text: data.message,
                  confirmButtonColor: '#ff5fa2',
                  timer: 1800,
                  showConfirmButton: false
                }).then(() => {
                  window.location.href = contextPath + data.redirectUrl;
                });
              } else {
                Swal.fire({
                  icon: 'error',
                  title: 'Xác thực thất bại',
                  text: data.message,
                  confirmButtonColor: '#ff5fa2'
                });
              }
            })
            .catch(err => {
              Swal.close();
              console.error("Error:", err);
              Swal.fire({
                icon: 'error',
                title: 'Lỗi Kết Nối',
                text: 'Không thể truyền dữ liệu xác thực đến máy chủ!',
                confirmButtonColor: '#ff5fa2'
              });
            });
  });
</script>
</body>
</html>

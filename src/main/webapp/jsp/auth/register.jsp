<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/register.css?v=999">
<script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
<script src="https://accounts.google.com/gsi/client" async defer></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

<section class="auth-page">
  <div class="auth-card">
    <h2 class="auth-title">Tạo tài khoản</h2>
    <p class="auth-subtitle">Tham gia MyCosmetic để mua sắm và theo dõi đơn hàng</p>

    <form id="registerForm" method="post" action="${pageContext.request.contextPath}/register" class="auth-form">
      <input type="hidden" id="registerStatus" value="${status}">
      <input type="hidden" name="csrf_token" value="<c:out value='${sessionScope.CSRF_TOKEN}'/>">

      <div id="step-email">
        <div class="form-group">
          <label>Địa chỉ email của bạn là gì?</label>
          <div class="input-group">
            <span class="input-icon">📧</span>
            <input type="email" name="email" id="emailInput" placeholder="vd: example@gmail.com" required value="<c:out value='${param.email}'/>">
          </div>
          <small id="email-error" class="error-msg"></small>
        </div>

        <button type="button" class="btn-auth" id="btnNext" onclick="handleEmailCheck(event)">
          Tiếp tục
        </button>

        <div class="auth-separator">
          HOẶC
        </div>

        <div class="social-login-stack" style="display: flex; flex-direction: column; align-items: center; gap: 12px; margin-top: 10px;">
          <div id="googleRegisterBtn" style="margin-top:20px"></div>

          <a href="javascript:void(0)" id="btn-facebook" class="btn-facebook-custom">
            <img src="https://upload.wikimedia.org/wikipedia/commons/0/05/Facebook_Logo_%282019%29.png" alt="Facebook">
            <span>Tiếp tục bằng Facebook</span>
          </a>
        </div>
      </div>

      <div id="step-profile" style="display: none;">
        <div class="form-group">
          <label>Họ và tên của bạn</label>
          <div class="input-group">
            <span class="input-icon">👤</span>
            <input type="text" name="fullName" id="nameInput" placeholder="Nhập họ và tên đầy đủ" value="<c:out value='${param.fullName}'/>">
          </div>
          <small id="name-error" class="error-msg"></small>
        </div>

        <div class="form-group">
          <label>Số điện thoại</label>
          <div class="input-group">
            <span class="input-icon">📞</span>
            <input type="tel" name="phone" id="phoneInput" placeholder="Nhập số điện thoại của bạn" value="<c:out value='${param.phone}'/>">
          </div>
          <small id="phone-error" class="error-msg"></small>
        </div>

        <div class="form-group">
          <label>Ngày sinh</label>
          <div class="input-group">
            <span class="input-icon">📅</span>
            <input type="date" name="birthDate" id="birthDateInput" value="<c:out value='${param.birthDate}'/>">
          </div>
          <small id="birthdate-error" class="error-msg"></small>
        </div>

        <div class="form-group">
          <label>Giới tính</label>
          <div class="gender-wrapper">
            <div class="gender-option">
              <input type="radio" name="gender" id="gender-male" value="male">
              <label for="gender-male">Nam</label>
            </div>
            <div class="gender-option">
              <input type="radio" name="gender" id="gender-female" value="female">
              <label for="gender-female">Nữ</label>
            </div>
            <div class="gender-option">
              <input type="radio" name="gender" id="gender-other" value="other">
              <label for="gender-other">Khác</label>
            </div>
          </div>
        </div>

        <div class="form-group">
          <label>Mật khẩu</label>
          <div class="input-group" style="position: relative; display: flex; align-items: center;">
            <span class="input-icon">🔒</span>
            <input type="password" name="password" id="passInput" placeholder="Tối thiểu 8 ký tự" style="width: 100%; padding-right: 40px;">
            <span class="toggle-password" style="position: absolute; right: 15px; cursor: pointer; user-select: none; font-size: 18px; z-index: 10;">🙈</span>
          </div>
          <small id="pass-error" class="error-msg"></small>
        </div>

        <div class="form-group">
          <label>Xác nhận mật khẩu</label>
          <div class="input-group" style="position: relative; display: flex; align-items: center;">
            <span class="input-icon">🛡️</span>
            <input type="password" name="confirmPassword" id="confirmPassInput" placeholder="Nhập lại mật khẩu" style="width: 100%; padding-right: 40px;">
            <span class="toggle-password" style="position: absolute; right: 15px; cursor: pointer; user-select: none; font-size: 18px; z-index: 10;">🙈</span>
          </div>
          <small id="password-error" class="error-msg"></small>
        </div>

        <div class="form-group">
          <div class="checkbox">
            <label>
              <input type="checkbox" name="agreeTerms" id="agreeTerms" required>
              Tôi đã đọc và chấp nhận <a href="${pageContext.request.contextPath}/terms.jsp" target="_blank">Điều khoản sử dụng</a>
            </label>
          </div>
        </div>

        <button type="submit" class="btn-register" id="btnSubmit">
          Hoàn tất Đăng ký
        </button>
      </div>
    </form>

    <div class="auth-footer">
      Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
    </div>
  </div>
</section>

<div id="otpModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); z-index: 1000; justify-content: center; align-items: center; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
  <div style="background: #ffffff; padding: 35px; border-radius: 12px; width: 100%; max-width: 400px; text-align: center; box-shadow: 0 8px 30px rgba(0,0,0,0.3); animation: fadeInModal 0.3s ease;">

    <div id="otpSuccessBanner" style="background-color: #e8f5e9; color: #2e7d32; padding: 12px; border-radius: 8px; margin-bottom: 20px; font-size: 14px; border: 1px solid #c8e6c9; display: flex; align-items: center; justify-content: center; gap: 8px; font-weight: 500;">
      <span style="font-size: 16px;">✅</span> <span id="otpSuccessMessage">Mã OTP đã được gửi thành công!</span>
    </div>

    <div style="font-size: 50px; color: #d81b60; margin-bottom: 10px;">✉️</div>

    <h3 style="color: #333333; margin: 0 0 10px 0; font-size: 22px; font-weight: 600;">Xác Thực Mã OTP</h3>
    <p style="color: #666666; font-size: 14px; line-height: 1.5; margin: 0 0 20px 0;">
      Hệ thống đã gửi một mã OTP gồm 6 chữ số đến Email của bạn. Vui lòng kiểm tra và nhập vào dưới đây.
    </p>

    <input type="text" id="otp_input" placeholder="******" maxlength="6" autocomplete="off"
           style="width: 80%; padding: 12px; margin-bottom: 20px; border: 2px solid #e0e0e0; border-radius: 8px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 8px; color: #333; outline: none; transition: border-color 0.2s;">

    <div style="margin-bottom: 20px;">
      <p id="otpCountdownText" style="color: #666666; font-size: 13px; margin: 0 0 8px 0;">
        Gửi lại mã sau: <span id="otpTimer" style="color: #d81b60; font-weight: bold;">30</span> giây
      </p>
      <button type="button" id="btnResendOtp" onclick="guiLaiOtp()" disabled
              style="background: #e0e0e0; color: #999999; border: none; padding: 6px 15px; border-radius: 20px; font-size: 13px; font-weight: 500; cursor: not-allowed; transition: all 0.3s;">
        Gửi lại mã OTP 🔄
      </button>
    </div>

    <div style="display: flex; gap: 12px; justify-content: center;">
      <button type="button" onclick="dongPopupOtp()"
              style="flex: 1; background: #f5f5f5; color: #555555; border: 1px solid #cccccc; padding: 12px; border-radius: 6px; font-size: 15px; font-weight: 500; cursor: pointer; transition: background 0.2s;">
        Hủy Bỏ
      </button>
      <button type="button" onclick="xacThucOtp()"
              style="flex: 1; background: #d81b60; color: #ffffff; border: none; padding: 12px; border-radius: 6px; font-size: 15px; font-weight: 500; cursor: pointer; box-shadow: 0 4px 10px rgba(216,27,96,0.3); transition: background 0.2s;">
        Xác Nhận
      </button>
    </div>
  </div>
</div>

<style>
  @keyframes fadeInModal {
    from { opacity: 0; transform: scale(0.9); }
    to { opacity: 1; transform: scale(1); }
  }
  #otp_input:focus {
    border-color: #d81b60 !important;
  }
  .swal2-container {
    z-index: 99999 !important;
  }

  @keyframes fadeInModal {
    from { opacity: 0; transform: scale(0.9); }
    to { opacity: 1; transform: scale(1); }
  }
  #otp_input:focus {
    border-color: #d81b60 !important;
  }
  .swal2-container {
    z-index: 99999 !important;
  }
  /* BỔ SUNG VÀO ĐÂY */
  input::-ms-reveal,
  input::-ms-clear {
    display: none !important;
  }
</style>

<script>
  // =====================================================
  // 1. CÁC BIẾN & HÀM TIỆN ÍCH UI VALIDATION
  // =====================================================
  const emailInput = document.getElementById("emailInput");
  const nameInput = document.getElementById("nameInput");
  const phoneInput = document.getElementById("phoneInput");
  const birthDateInput = document.getElementById("birthDateInput");
  const passInput = document.getElementById("passInput");
  const confirmInput = document.getElementById("confirmPassInput");

  const emailError = document.getElementById("email-error");
  const nameError = document.getElementById("name-error");
  const phoneError = document.getElementById("phone-error");
  const birthdateError = document.getElementById("birthdate-error");
  const passError = document.getElementById("pass-error");
  const confirmError = document.getElementById("password-error");

  const btnSubmit = document.getElementById('btnSubmit');
  const checkbox = document.getElementById('agreeTerms');

  // Vô hiệu hóa nút Submit nếu chưa đồng ý điều khoản
  if (btnSubmit && checkbox) {
    btnSubmit.disabled = true;
    checkbox.addEventListener('change', function() {
      btnSubmit.disabled = !this.checked;
    });
  }

  function showError(input, errorElement, message) {
    input.classList.remove("input-success");
    input.classList.add("input-error");
    errorElement.innerText = message;
    errorElement.style.display = "block";
  }

  function showSuccess(input, errorElement) {
    input.classList.remove("input-error");
    input.classList.add("input-success");
    errorElement.innerText = "";
    errorElement.style.display = "none";
  }

  function resetState(input, errorElement) {
    input.classList.remove("input-error", "input-success");
    errorElement.style.display = "none";
  }

  // =====================================================
  // 2. LOGIC REAL-TIME TỪNG TRƯỜNG DỮ LIỆU
  // =====================================================
  if (emailInput) {
    emailInput.addEventListener("input", () => {
      const value = emailInput.value.trim();
      resetState(emailInput, emailError);
      const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!regex.test(value)) {
        showError(emailInput, emailError, "Định dạng email không hợp lệ.");
        return;
      }
      if (!value.endsWith("@gmail.com")) {
        showError(emailInput, emailError, "Vui lòng sử dụng địa chỉ email @gmail.com.");
        return;
      }
      showSuccess(emailInput, emailError);
    });
  }

  if (nameInput) {
    nameInput.addEventListener("input", () => {
      const value = nameInput.value.trim();
      resetState(nameInput, nameError);
      if (value.length < 4) {
        showError(nameInput, nameError, "Họ và tên tối thiểu 4 ký tự.");
        return;
      }
      showSuccess(nameInput, nameError);
    });
  }

  if (phoneInput) {
    phoneInput.addEventListener("input", () => {
      const value = phoneInput.value.trim();
      resetState(phoneInput, phoneError);
      const regex = /^(03|05|07|08|09)\d{8}$/;
      if (!regex.test(value)) {
        showError(phoneInput, phoneError, "Số điện thoại không hợp lệ (10 số, bắt đầu 03/05/07/08/09).");
        return;
      }
      showSuccess(phoneInput, phoneError);
    });
  }

  if (birthDateInput) {
    birthDateInput.addEventListener("input", () => {
      const value = birthDateInput.value;
      resetState(birthDateInput, birthdateError);
      if (!value) {
        showError(birthDateInput, birthdateError, "Vui lòng chọn ngày sinh của bạn.");
        return;
      }
      showSuccess(birthDateInput, birthdateError);
    });
  }

  if (passInput) {
    passInput.addEventListener("input", () => {
      const value = passInput.value;
      resetState(passInput, passError);
      if (value.length < 8) {
        showError(passInput, passError, "Mật khẩu tối thiểu 8 ký tự.");
        return;
      }
      if (!/[A-Z]/.test(value)) {
        showError(passInput, passError, "Cần ít nhất 1 chữ in hoa.");
        return;
      }
      if (!/[0-9]/.test(value)) {
        showError(passInput, passError, "Cần ít nhất 1 chữ số.");
        return;
      }
      if (!/[!@#$%^&*]/.test(value)) {
        showError(passInput, passError, "Cần ít nhất 1 ký tự đặc biệt (!@#$%^&*).");
        return;
      }
      showSuccess(passInput, passError);
      if(confirmInput && confirmInput.value) {
        confirmInput.dispatchEvent(new Event('input'));
      }
    });
  }

  if (confirmInput) {
    confirmInput.addEventListener("input", () => {
      resetState(confirmInput, confirmError);
      if (confirmInput.value === "" || confirmInput.value !== passInput.value) {
        showError(confirmInput, confirmError, "Mật khẩu xác nhận không khớp.");
        return;
      }
      showSuccess(confirmInput, confirmError);
    });
  }

  // =====================================================
  // 3. XỬ LÝ NÚT TIẾP TỤC (BƯỚC 1 - KIỂM TRA EMAIL)
  // =====================================================
  function handleEmailCheck(event) {
    if (event) event.preventDefault();
    emailInput.dispatchEvent(new Event('input'));
    if (emailInput.classList.contains("input-error")) {
      return;
    }
    const email = emailInput.value.trim().toLowerCase();
    fetch('${pageContext.request.contextPath}/check-email?email=' + encodeURIComponent(email))
  .then(res => res.json())
  .then(data => {
      if (data.exists) {
        showError(emailInput, emailError, "Email này đã được đăng ký.");
      } else {
        showSuccess(emailInput, emailError);
        document.getElementById('step-email').style.display = 'none';
        document.getElementById('step-profile').style.display = 'block';
      }
    })
  .catch(err => {
      console.error(err);
      showError(emailInput, emailError, "Lỗi hệ thống, vui lòng thử lại sau.");
    });
  }

  // =====================================================
  // 4. DUY NHẤT MỘT KHỐI XỬ LÝ SUBMIT FORM (SỬA LỖI GỬI 2 OTP)
  // =====================================================
  document.getElementById("registerForm").addEventListener("submit", function(e) {
    e.preventDefault();

    if (nameInput) nameInput.dispatchEvent(new Event('input'));
    if (phoneInput) phoneInput.dispatchEvent(new Event('input'));
    if (birthDateInput) birthDateInput.dispatchEvent(new Event('input'));
    if (passInput) passInput.dispatchEvent(new Event('input'));
    if (confirmInput) confirmInput.dispatchEvent(new Event('input'));

    const totalErrors = document.querySelectorAll(".input-error").length;
    if (totalErrors > 0) {
      Swal.fire({
      icon: 'error',
      title: 'Thông tin chưa hợp lệ',
      text: 'Vui lòng chỉnh sửa lại các trường thông tin đang báo đỏ!'
    });
      return;
    }

    const formData = new URLSearchParams(new FormData(this));
    Swal.fire({
    title: 'Đang gửi yêu cầu...',
    text: 'Hệ thống đang kiểm tra dữ liệu và gửi mã OTP về Email.',
    allowOutsideClick: false,
    didOpen: () => { Swal.showLoading(); }
  });

    fetch(this.action || '${pageContext.request.contextPath}/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: formData.toString()
  })
  .then(res => res.json())
  .then(data => {
      Swal.close();
      if (data.status === 'otp_required') {
        // GỌI HÀM MỞ VÀ TRUYỀN NỘI DUNG THÔNG BÁO Backend về giao diện
        moPopupOtp(data.message);
      } else if (data.status === 'error') {
        let noiDungLoi = "";
        if (data.errors) {
          for (const key in data.errors) {
            noiDungLoi += data.errors[key] + "\n";
            // Gắn trực tiếp text báo đỏ dưới từng ô nhập liệu tương ứng
            const errorSpan = document.getElementById("error-" + key) || document.getElementById(key + "-error");
            if (errorSpan) errorSpan.innerText = data.errors[key];
          }
        } else {
          noiDungLoi = data.message;
        }
        Swal.fire('Không thể đi tiếp', noiDungLoi, 'error');
      } else {
        Swal.fire('Thất bại', data.message || 'Đăng ký không thành công', 'error');
      }
    })
  .catch(err => {
      Swal.close();
      console.error(err);
      Swal.fire('Lỗi đường truyền', 'Máy chủ đang bận, vui lòng thử lại sau ít phút!', 'error');
    });
  });

  // Hàm hiển thị Popup OTP và gán nội dung câu chữ thông báo lên UI
  function moPopupOtp(messageText) {
    document.getElementById('otp_input').value = "";
    if (messageText) {
      document.getElementById('otpSuccessMessage').innerText = messageText;
    }
    document.getElementById('otpModal').style.display = 'flex';
    document.getElementById('otp_input').focus();
  }

  function dongPopupOtp() {
    document.getElementById('otpModal').style.display = 'none';
  }

  // Hàm gửi mã OTP từ trình duyệt lên VerifyRegistrationServlet để lưu tài khoản
  function xacThucOtp() {
    const otpValue = document.getElementById('otp_input').value.trim();

    // Kiểm tra tính hợp lệ sơ bộ ngoài client
    if (!otpValue || otpValue.length !== 6 || isNaN(otpValue)) {
      Swal.fire({
        icon: 'warning',
        title: 'Cảnh báo',
        text: 'Vui lòng nhập chính xác mã OTP gồm 6 chữ số!',
        confirmButtonColor: '#d81b60'
      });
      return;
    }

    // 1. Tìm nút Xác Nhận và khóa ngay lập tức để tránh bấm vô hiệu, chống lag dữ liệu
    const btnXacNhan = document.querySelector('#otpModal button[onclick="xacThucOtp()"]');
    let originalText = "Xác Nhận";

    if (btnXacNhan) {
      originalText = btnXacNhan.innerText;
      btnXacNhan.disabled = true;
      btnXacNhan.innerText = "Đang xử lý...";
      btnXacNhan.style.opacity = "0.7";
      btnXacNhan.style.cursor = "not-allowed";
    }

    // Gửi mã OTP qua phương thức POST lên VerifyRegistrationServlet
    fetch('${pageContext.request.contextPath}/verify-registration', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: 'otp_input=' + encodeURIComponent(otpValue)
    })
            .then(response => response.json())
            .then(data => {
              // Mở khóa lại nút bấm nếu tiến trình chạy xong hoặc có lỗi trả về
              if (btnXacNhan) {
                btnXacNhan.disabled = false;
                btnXacNhan.innerText = originalText;
                btnXacNhan.style.opacity = "1";
                btnXacNhan.style.cursor = "pointer";
              }

              if (data.status === 'success') {
                // 2. ẨN POPUP OTP NGAY LẬP TỨC để giải phóng giao diện
                dongPopupOtp();

                // 3. HIỂN THỊ THÔNG BÁO TỰ ĐỘNG ĐÓNG SAU 2 GIÂY (Không cần người dùng bấm OK)
                Swal.fire({
                  icon: 'success',
                  title: 'Thành công!',
                  text: data.message,
                  timer: 2000,               // Tự động đóng sau 2000ms (2 giây)
                  showConfirmButton: false,  // Ẩn hoàn toàn nút OK phiền phức
                  allowOutsideClick: false
                }).then(() => {
                  // 4. Chuyển hướng trang tự động sau khi thông báo biến mất
                  window.location.href = '${pageContext.request.contextPath}/login';
                });

              } else if (data.status === 'error') {
                // Sai OTP hoặc lỗi kết nối DB khi lưu, thông báo lỗi cho người dùng
                Swal.fire({
                  icon: 'error',
                  title: 'Lỗi xác thực',
                  text: data.message,
                  confirmButtonColor: '#d81b60'
                });
              }
            })
            .catch(error => {
              // Mở khóa lại nút bấm nếu lỗi đường truyền mạng
              if (btnXacNhan) {
                btnXacNhan.disabled = false;
                btnXacNhan.innerText = originalText;
                btnXacNhan.style.opacity = "1";
                btnXacNhan.style.cursor = "pointer";
              }
              console.error('Error:', error);
              Swal.fire({
                icon: 'error',
                title: 'Lỗi mạng',
                text: 'Đã xảy ra lỗi đường truyền trong quá trình xác thực OTP!',
                confirmButtonColor: '#d81b60'
              });
            });
  }

  // =====================================================
  // 5. TRẠNG THÁI STATUS CHUYỂN HƯỚNG TỪ BACKEND
  // =====================================================
  const status = document.getElementById('registerStatus').value;
  if (status === "success") {
    Swal.fire('Thành công!', 'Đang chuyển hướng đến đăng nhập...', 'success')
  .then(() => window.location.href = "${pageContext.request.contextPath}/login");
  } else if (status === "wrong_otp") {
    Swal.fire('Lỗi', 'Mã OTP không chính xác!', 'error');
  } else if (status === "fail") {
    Swal.fire('Lỗi', 'Có lỗi xảy ra trong quá trình đăng ký.', 'error');
  }

  // =====================================================
  // 6. TÍCH HỢP ĐĂNG NHẬP GOOGLE
  // =====================================================
  const GOOGLE_CLIENT_ID = "78979081819-fo21lsm5idv3pp22779bais8l1f5csnm.apps.googleusercontent.com";
  const contextPath = "${pageContext.request.contextPath}";

  window.onload = function () {
    google.accounts.id.initialize({
    client_id: GOOGLE_CLIENT_ID,
    callback: handleGoogleRegister
  });
    google.accounts.id.renderButton(
    document.getElementById("googleRegisterBtn"),
    { theme: "outline", size: "large", width: 350, shape: "pill" }
  );
  };

  function handleGoogleRegister(response) {
    // Hiển thị loading
    Swal.fire({
      title: 'Đang kiểm tra...',
      allowOutsideClick: false,
      didOpen: () => { Swal.showLoading(); }
    });

    fetch(contextPath + '/social-auth', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        provider: 'google',
        credential: response.credential,
        mode: 'register'
      })
    })
            .then(res => res.json())
            .then(data => {
              Swal.close(); // Tắt loading

              if (data.status === 'otp_required') {
                // Trường hợp đăng ký mới thành công (cần OTP)
                Swal.fire({
                  icon: 'success',
                  title: 'Đã gửi mã!',
                  text: 'Vui lòng kiểm tra Gmail để lấy mã OTP.'
                }).then(() => window.location.href = contextPath + data.redirectUrl);

              } else if (data.status === 'error') {
                let errorMsg = data.message;
                if (errorMsg.toLowerCase().includes("tồn tại") || errorMsg.toLowerCase().includes("exist") || errorMsg.toLowerCase().includes("trùng")) {
                  errorMsg = "Email đã tồn tại hoặc tài khoản này đã được đăng ký trước đó trên hệ thống!";
                }

                Swal.fire({
                  icon: 'error',
                  title: 'Đăng ký không thành công',
                  text: errorMsg
                });
              }
            })
            .catch(err => {
              Swal.close();
              Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Không thể kết nối server' });
            });
  }

  document.addEventListener("DOMContentLoaded", function () {

    var toggleBtns = document.getElementsByClassName("toggle-password");

    for (var i = 0; i < toggleBtns.length; i++) {
      toggleBtns[i].addEventListener("click", function () {
        var container = this.closest('.input-group');
        var inp = container.querySelector("input");

        if (!inp) return;


        if (inp.type === "password") {
          inp.type = "text";
          this.textContent = "👁️";
        } else {
          inp.type = "password";
          this.textContent = "🙈";
        }
      });
    }
  });

  // ================= CẤU HÌNH FACEBOOK REGISTER =================
  const FACEBOOK_APP_ID = "1891459851533615";

  window.fbAsyncInit = function () {
    FB.init({
      appId: FACEBOOK_APP_ID,
      cookie: true,
      xfbml: true,
      version: "v19.0"
    });
  };

  (function (d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)) return;
    js = d.createElement(s);
    js.id = id;
    js.src = "https://connect.facebook.net/vi_VN/sdk.js";
    fjs.parentNode.insertBefore(js, fjs);
  }(document, "script", "facebook-jssdk"));

  document.getElementById("btn-facebook").addEventListener("click", function () {
    FB.login(function (response) {
      if (response.authResponse) {
        const accessToken = response.authResponse.accessToken;

        Swal.fire({
          title: 'Đang xử lý...',
          allowOutsideClick: false,
          didOpen: () => { Swal.showLoading(); }
        });

        fetch(contextPath + "/social-auth", {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded"
          },
          body: new URLSearchParams({
            provider: "facebook",
            accessToken: accessToken,
            mode: "register"
          })
        })
                .then(res => res.json())
                .then(data => {
                  Swal.close();

                  if (data.status === 'success') {
                    // Nếu tài khoản mạng xã hội này đã từng liên kết
                    Swal.fire({ icon: 'success', title: 'Thành công', text: 'Đăng ký và đăng nhập thành công', timer: 1500, showConfirmButton: false })
                            .then(() => window.location.href = contextPath + data.redirectUrl);

                  } else if (data.status === 'otp_required') {moPopupOtp(data.message);

                  } else {
                    // Đoạn xử lý các lỗi còn lại (Ví dụ: trùng email)
                    let errorMsg = data.message;
                    if (errorMsg.toLowerCase().includes("tồn tại") || errorMsg.toLowerCase().includes("exist") || errorMsg.toLowerCase().includes("trùng")) {
                      errorMsg = "Email đã tồn tại hoặc tài khoản này đã được đăng ký trước đó trên hệ thống!";
                    }

                    Swal.fire({
                      icon: 'error',
                      title: 'Thông báo lỗi',
                      text: errorMsg
                    });
                  }
                }).catch(() => {
          Swal.fire("Lỗi", "Không thể kết nối server", "error");
        });
      } else {
        Swal.fire("Lỗi", "Bạn đã hủy đăng nhập Facebook", "error");
      }
    }, { scope: "public_profile,email" });
  });

  let countdownInterval;

  // Hàm kích hoạt đếm ngược 30 giây
  function startOtpCountdown() {
    let timeLeft = 30;
    const timerSpan = document.getElementById("otpTimer");
    const btnResend = document.getElementById("btnResendOtp");
    const countdownText = document.getElementById("otpCountdownText");

    // Reset trạng thái nút ban đầu
    btnResend.disabled = true;
    btnResend.style.background = "#e0e0e0";
    btnResend.style.color = "#999999";
    btnResend.style.cursor = "not-allowed";
    countdownText.style.display = "block";
    timerSpan.innerText = timeLeft;

    clearInterval(countdownInterval);
    countdownInterval = setInterval(() => {
      timeLeft--;
      timerSpan.innerText = timeLeft;

      if (timeLeft <= 0) {
        clearInterval(countdownInterval);
        countdownText.style.display = "none"; // Ẩn chữ đếm ngược khi hết giờ
        btnResend.disabled = false;
        btnResend.style.background = "#ffffff";
        btnResend.style.color = "#d81b60";
        btnResend.style.border = "1px solid #d81b60";
        btnResend.style.cursor = "pointer";
      }
    }, 1000);
  }

  // Sửa lại hàm moPopupOtp sẵn có để kích hoạt đếm ngược khi vừa mở popup
  function moPopupOtp(messageText) {
    document.getElementById('otp_input').value = "";
    if (messageText) {
      document.getElementById('otpSuccessMessage').innerText = messageText;
    }
    document.getElementById('otpModal').style.display = 'flex';
    document.getElementById('otp_input').focus();

    startOtpCountdown(); // Chạy đếm ngược ngay khi modal xuất hiện!
  }

  // Hàm gọi lên Servlet gửi lại mã OTP
  function guiLaiOtp() {
    Swal.fire({
      title: 'Đang gửi lại mã...',
      allowOutsideClick: false,
      didOpen: () => { Swal.showLoading(); }
    });

    fetch('${pageContext.request.contextPath}/resend-otp', {
      method: 'POST'
    })
            .then(res => res.json())
            .then(data => {
              Swal.close();
              if (data.status === 'success') {
                Swal.fire('Thành công', 'Mã OTP mới đã được gửi về email của bạn!', 'success');
                startOtpCountdown(); // Khởi động lại vòng lặp đếm ngược 30s mới
              } else {
                Swal.fire('Thất bại', data.message, 'error');
              }
            })
            .catch(err => {
              Swal.close();
              Swal.fire('Lỗi mạng', 'Không thể kết nối máy chủ để gửi lại mã!', 'error');
            });
  }
</script>

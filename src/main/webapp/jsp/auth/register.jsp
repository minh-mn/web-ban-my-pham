<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/register.css?v=999">
<script src="https://accounts.google.com/gsi/client" async defer></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

<section class="auth-page">
  <div class="auth-card">
    <h2 class="auth-title">Tạo tài khoản</h2>
    <p class="auth-subtitle">Tham gia MyCosmetic để mua sắm và theo dõi đơn hàng</p>

    <form method="post" action="${pageContext.request.contextPath}/register" class="auth-form" id="registerForm">
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

        <button type="button" class="btn-auth" id="btnNext" onclick="handleEmailCheck()">
          Tiếp theo
        </button>

        <div class="auth-divider">
          <span>HOẶC</span>
        </div>

        <div class="social-login-stack">
          <a href="javascript:void(0)" class="social-btn">
            <span class="social-icon google-icon"></span>
            <span>Đăng ký bằng Google</span>
          </a>

          <a href="javascript:void(0)" class="social-btn">
            <span class="social-icon facebook-icon"></span>
            <span>Đăng ký bằng Facebook</span>
          </a>
        </div>
      </div>

      <div id="step-details" style="display: none;">

        <div class="form-group" style="border: 1px dashed #ff5fa2; padding: 15px; border-radius: 4px; text-align: center;">
          <label style="color: #ff5fa2;">🔑 Mã xác thực OTP</label>
          <div class="input-group">
            <input type="text" name="otp_input" id="otpInput" placeholder="Nhập 6 số" required maxlength="6"
                   style="text-align: center; font-size: 20px; letter-spacing: 6px; padding: 0;">
          </div>
          <small style="color: #777; display: block; margin-top: 8px;">Kiểm tra email để lấy mã.</small>
        </div>

        <div class="form-group">
          <label>Tên đăng nhập</label>
          <div class="input-group">
            <span class="input-icon">👤</span>
            <input type="text" name="userName" id="userNameInput" required>
          </div>
          <small id="user-error" class="error-msg"></small>
        </div>

        <div class="form-group">
          <label>Mật khẩu</label>
          <div class="input-group">
            <span class="input-icon">🔒</span>
            <input type="password" name="password" id="passInput" required>
            <span class="toggle-password" onclick="togglePass('passInput', this)">👁️</span>
          </div>
          <small id="pass-error" class="error-msg"></small>
        </div>

        <div class="form-group">
          <label>Xác nhận mật khẩu</label>
          <div class="input-group">
            <span class="input-icon">🛡️</span>
            <input type="password" name="confirmPassword" id="confirmPassInput" required>
            <span class="toggle-password" onclick="togglePass('confirmPassInput', this)">👁️</span>
          </div>
          <small id="password-error" class="error-msg"></small>
        </div>

        <div class="form-group">
          <label>Họ và tên</label>
          <div class="input-group">
            <span class="input-icon">🪪</span>
            <input type="text" name="fullName" id="fullNameInput" value="${param.fullName}" required>
          </div>
          <small id="name-error" class="error-msg"></small>
        </div>

        <div class="form-group">
          <label>Ngày sinh</label>
          <div class="input-group">
            <span class="input-icon">🎂</span>
            <input type="date" name="birthday" required>
          </div>
        </div>

        <div class="form-group">
          <label>Giới tính</label>
          <div class="gender-group-inline">
            <label class="gender-pill"><input type="radio" name="gender" value="Male" checked> Nam</label>
            <label class="gender-pill"><input type="radio" name="gender" value="Female"> Nữ</label>
          </div>
        </div>

        <div class="form-group">
          <label>Số điện thoại</label>
          <div class="input-group">
            <span class="input-icon">📞</span>
            <input type="tel" name="phone" id="phoneInput" placeholder="vd: 0987654321" required>
          </div>
          <small id="phone-error" class="error-msg"></small>
        </div>

        <button type="submit" class="btn-auth">
          Hoàn tất Đăng ký
        </button>

        <div style="text-align: center; margin-top: 20px;">
          <a href="javascript:void(0)" onclick="goBack()" class="back-link">← Quay lại chỉnh sửa email</a>
        </div>
      </div>
    </form>

    <div class="auth-footer">
      Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
    </div>
  </div>
</section>

<script>
  function handleEmailCheck() {
    const emailInput = document.getElementById('emailInput');
    const emailError = document.getElementById('email-error');
    const stepEmail = document.getElementById('step-email');
    const stepDetails = document.getElementById('step-details');

    if (!emailInput.value.includes('@') || emailInput.value.length < 5) {
      emailError.innerText = "Vui lòng nhập địa chỉ email hợp lệ.";
      emailError.style.display = "block";
      return;
    }

    // Call API check email
    fetch('${pageContext.request.contextPath}/check-email?email=' + encodeURIComponent(emailInput.value))
            .then(response => response.json())
            .then(data => {
              if (data.exists) {
                emailError.innerText = "Email này đã được sử dụng. Vui lòng chọn email khác.";
                emailError.style.display = "block";
              } else {
                emailError.style.display = "none";
                stepEmail.style.display = 'none';
                stepDetails.style.display = 'block';
              }
            })
            .catch(() => {
              Swal.fire('Lỗi', 'Không thể kết nối máy chủ', 'error');
            });
  }

  function sendOTP(email) {
    fetch('${pageContext.request.contextPath}/send-otp', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: 'email=' + encodeURIComponent(email)
    })
            .then(res => res.text())
            .then(data => {
              if(data.trim() === "success") {
                Swal.fire('Thành công', 'Mã OTP đã gửi về Email của bạn!', 'success');
                document.getElementById('step-email').style.display = 'none';
                document.getElementById('step-details').style.display = 'block';
              } else {
                Swal.fire('Lỗi', 'Không thể gửi mã OTP. Vui lòng thử lại.', 'error');
              }
            })
            .finally(() => {
              document.getElementById('btnNext').innerText = "Tiếp theo";
              document.getElementById('btnNext').disabled = false;
            });
  }

  function goBack() {
    document.getElementById('step-email').style.display = 'block';
    document.getElementById('step-details').style.display = 'none';
  }

  function togglePass(id, el) {
    const input = document.getElementById(id);
    input.type = (input.type === "password") ? "text" : "password";
    el.innerText = (input.type === "password") ? "👁️" : "🙈";
  }

  document.addEventListener('DOMContentLoaded', function() {
    const validationRules = {
      fullNameInput: { regex: /^.{2,50}$/, errorId: 'name-error', message: 'Họ và tên phải từ 2 đến 50 ký tự.' },
      phoneInput: { regex: /^0\d{9}$/, errorId: 'phone-error', message: 'Số điện thoại phải 10 số, bắt đầu bằng 0.' },
      userNameInput: { regex: /^[a-zA-Z0-9._]{3,20}$/, errorId: 'user-error', message: 'Tên đăng nhập 3-20 ký tự, không dấu.' },
      passInput: { regex: /^(?=.*[A-Za-z])(?=.*\d).{8,}$/, errorId: 'pass-error', message: 'Mật khẩu từ 8 ký tự, gồm cả chữ và số.' }
    };

    function validateField(inputElement) {
      const rule = validationRules[inputElement.id];
      if(!rule) return;
      const errorElement = document.getElementById(rule.errorId);
      const isValid = rule.regex.test(inputElement.value);

      if (!isValid) {
        errorElement.style.display = "block";
        errorElement.innerText = rule.message;
        inputElement.classList.add('invalid');
      } else {
        errorElement.style.display = "none";
        inputElement.classList.remove('invalid');
        inputElement.classList.add('valid');
      }
    }

    Object.keys(validationRules).forEach(id => {
      const input = document.getElementById(id);
      if (input) input.addEventListener('input', () => validateField(input));
    });

    const passInput = document.getElementById('passInput');
    const confirmInput = document.getElementById('confirmPassInput');
    const confirmError = document.getElementById('password-error');

    confirmInput.addEventListener('input', function() {
      if (this.value !== passInput.value) {
        confirmError.innerText = "Mật khẩu xác nhận không khớp.";
        confirmError.style.display = "block";
        this.classList.add('invalid');
      } else {
        confirmError.style.display = "none";
        this.classList.remove('invalid');
        this.classList.add('valid');
      }
    });

    const status = document.getElementById('registerStatus').value;
    if (status === "success") {
      Swal.fire('Thành công!', 'Đang chuyển hướng đến đăng nhập...', 'success')
              .then(() => window.location.href = "${pageContext.request.contextPath}/login");
    } else if (status === "wrong_otp") {
      Swal.fire('Lỗi', 'Mã OTP không chính xác!', 'error');
    }
  });
</script>

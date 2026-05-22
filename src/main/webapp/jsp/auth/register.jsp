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
          <small id="email-error" class="error-msg" style="display: none; color: #ff4d4f; text-align: left; margin-top: 4px; font-size: 13px;"></small>
        </div>

        <button type="button" class="btn-auth" id="btnNext" onclick="handleEmailCheck(event)">
          Tiếp tục
        </button>

        <div class="auth-separator">
          <span>HOẶC</span>
        </div>

        <div class="social-login-stack">
          <a href="javascript:void(0)" id="btn-google" class="social-btn">
            <img src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg" alt="Google">
            <span>Tiếp tục bằng Google</span>
          </a>
          <a href="javascript:void(0)" id="btn-facebook" class="social-btn">
            <img src="https://upload.wikimedia.org/wikipedia/commons/b/b9/2023_Facebook_icon.svg" alt="Facebook">
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
          <small id="name-error" class="error-msg" style="display: none; color: #ff4d4f; text-align: left; margin-top: 4px; font-size: 13px;"></small>
        </div>

        <div class="form-group">
          <label>Số điện thoại</label>
          <div class="input-group">
            <span class="input-icon">📞</span>
            <input type="tel" name="phone" id="phoneInput" placeholder="Nhập số điện thoại của bạn" value="<c:out value='${param.phone}'/>">
          </div>
          <small id="phone-error" class="error-msg" style="display: none; color: #ff4d4f; text-align: left; margin-top: 4px; font-size: 13px;"></small>
        </div>

        <div class="form-group">
          <label>Ngày sinh</label>
          <div class="input-group">
            <span class="input-icon">📅</span>
            <input type="date" name="birthDate" id="birthDateInput" value="<c:out value='${param.birthDate}'/>">
          </div>
          <small id="birthdate-error" class="error-msg" style="display: none; color: #ff4d4f; text-align: left; margin-top: 4px; font-size: 13px;"></small>
        </div>

        <div class="form-group">
          <label>Giới tính</label>
          <div class="input-group" style="display: flex; gap: 24px; padding: 10px 5px; border: none; background: transparent;">
            <label style="font-weight: 500; cursor: pointer; display: flex; align-items: center; gap: 6px;">
              <input type="radio" name="gender" value="Male" checked> Nam
            </label>
            <label style="font-weight: 500; cursor: pointer; display: flex; align-items: center; gap: 6px;">
              <input type="radio" name="gender" value="Female"> Nữ
            </label>
            <label style="font-weight: 500; cursor: pointer; display: flex; align-items: center; gap: 6px;">
              <input type="radio" name="gender" value="Other"> Khác
            </label>
          </div>
        </div>

        <div class="form-group">
          <label>Mật khẩu</label>
          <div class="input-group">
            <span class="input-icon">🔒</span>
            <input type="password" name="password" id="passInput" placeholder="Tối thiểu 6 ký tự">
          </div>
          <small id="pass-error" class="error-msg" style="display: none; color: #ff4d4f; text-align: left; margin-top: 4px; font-size: 13px;"></small>
        </div>

        <div class="form-group">
          <label>Xác nhận mật khẩu</label>
          <div class="input-group">
            <span class="input-icon">🛡️</span>
            <input type="password" name="confirmPassword" id="confirmPassInput" placeholder="Nhập lại mật khẩu">
          </div>
          <small id="password-error" class="error-msg" style="display: none; color: #ff4d4f; text-align: left; margin-top: 4px; font-size: 13px;"></small>
        </div>

        <div class="form-group">
          <div class="checkbox">
            <label>
              <input type="checkbox" name="agreeTerms" id="agreeTerms" required>
              Tôi đã đọc và chấp nhận <a href="${pageContext.request.contextPath}/terms.jsp" target="_blank">Điều khoản sử dụng</a>
            </label>
          </div>
        </div>

        <button type="submit" class="btn-register">
          Hoàn tất Đăng ký
        </button>
      </div>
    </form>

    <div class="auth-footer">
      Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
    </div>
  </div>
</section>

<script>
  // Script đơn giản chặn nút đăng ký nếu chưa tích
  const checkbox = document.getElementById('agreeTerms');
  const btnSubmit = document.getElementById('btnSubmit'); // ID nút đăng ký của bạn

  btnSubmit.disabled = true; // Mặc định khóa nút

  checkbox.addEventListener('change', function() {
    btnSubmit.disabled = !this.checked;
  });

  // 1. Hàm Xử lý AJAX kiểm tra trùng email và chuyển tiếp bước mượt mà
  function handleEmailCheck(event) {
    if (event) event.preventDefault();
    const emailInput = document.getElementById('emailInput');
    const emailError = document.getElementById('email-error');
    // Chuyển email về chữ thường để kiểm tra đồng nhất
    const email = emailInput.value.trim().toLowerCase();

    // 1. Kiểm tra định dạng email cơ bản (Regex)
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      emailError.innerText = "Định dạng email không hợp lệ.";
      emailError.style.display = "block";
      emailInput.classList.add('invalid');
      return;
    }

    // 2. Kiểm tra bắt buộc phải là @gmail.com
    if (!email.endsWith("@gmail.com")) {
      emailError.innerText = "Vui lòng sử dụng địa chỉ email @gmail.com.";
      emailError.style.display = "block";
      emailInput.classList.add('invalid');
      return;
    }

    // Nếu vượt qua các bước kiểm tra, ẩn lỗi và tiếp tục
    emailError.style.display = "none";
    emailInput.classList.remove('invalid');

    // Gửi request kiểm tra email tồn tại
    fetch('${pageContext.request.contextPath}/check-email?email=' + encodeURIComponent(email))
            .then(res => res.json())
            .then(data => {
              if (data.exists) {
                emailError.innerText = "Email này đã được đăng ký.";
                emailError.style.display = "block";
                emailInput.classList.add('invalid');
              } else {
                emailError.style.display = "none";
                document.getElementById('step-email').style.display = 'none';
                document.getElementById('step-profile').style.display = 'block';
              }
            })
            .catch(err => {
              console.error(err);
              emailError.innerText = "Lỗi hệ thống, vui lòng thử lại sau.";
              emailError.style.display = "block";
            });
  }

  // 2. Logic Real-time Validation cho các ô nhập liệu còn lại
  const validationRules = {
    nameInput: { required: true, message: "Họ và tên không được để trống." },
    phoneInput: { required: true, pattern: /^[0-9]{10,11}$/, message: "Số điện thoại gồm 10-11 chữ số hợp lệ." },
    birthDateInput: { required: true, message: "Vui lòng chọn ngày sinh của bạn." },
    passInput: {
      required: true,
      pattern: /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{6,20}$/,
      message: "Mật khẩu dài 6-20 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường và 1 chữ số."
    }
  };

  function validateField(inputElement) {
    const rule = validationRules[inputElement.id];
    if (!rule) return;

    const errorId = inputElement.id === 'nameInput' ? 'name-error' :
            inputElement.id === 'phoneInput' ? 'phone-error' :
                    inputElement.id === 'birthDateInput' ? 'birthdate-error' : 'pass-error';
    const errorElement = document.getElementById(errorId);
    let isValid = true;

    if (rule.required && !inputElement.value.trim()) isValid = false;
    if (rule.minLength && inputElement.value.length < rule.minLength) isValid = false;
    if (rule.pattern && !rule.pattern.test(inputElement.value.trim())) isValid = false;

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

  // Kiểm tra xác nhận mật khẩu khớp nhau
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

  // Đọc Trạng thái phản hồi từ Server thông qua SweetAlert2
  const status = document.getElementById('registerStatus').value;
  if (status === "success") {
    Swal.fire('Thành công!', 'Đang chuyển hướng đến đăng nhập...', 'success')
            .then(() => window.location.href = "${pageContext.request.contextPath}/login");
  } else if (status === "wrong_otp") {
    Swal.fire('Lỗi', 'Mã OTP không chính xác!', 'error');
  } else if (status === "fail") {
    Swal.fire('Lỗi', 'Có lỗi xảy ra trong quá trình đăng ký.', 'error');
  }
</script>

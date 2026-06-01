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

      <button type="submit" class="btn-auth" style="background-color: #ff5fa2; color: white; border: none; padding: 12px; border-radius: 50px; font-weight: bold; width: 100%; cursor: pointer; margin-top: 15px;">
        Hoàn Tất Đăng Ký
      </button>
    </form>
  </div>
</section>

<script>
  const contextPath = "${pageContext.request.contextPath}";

  document.getElementById("otpForm").addEventListener("submit", function(e) {
    // Chặn đứng hành vi submit truyền thống của form để không bị reload sinh lỗi GET/405
    e.preventDefault();

    const otpValue = document.getElementById("otp_input").value.trim();

    Swal.fire({
      title: 'Đang xác thực...',
      didOpen: () => { Swal.showLoading(); },
      allowOutsideClick: false
    });

    // Gửi dữ liệu bằng POST một cách chuẩn xác qua Fetch API
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
                  // Nhảy thẳng vào trang chủ ("/") mà không cần thực hiện thêm bước đăng nhập nào
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

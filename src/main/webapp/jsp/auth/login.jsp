<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">
<script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
<script src="https://accounts.google.com/gsi/client" async defer></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

<section class="auth-page">
	<div class="auth-card">
		<h2 class="auth-title">Đăng nhập vào MyCosmetic</h2>

		<div class="social-login-stack" style="display: flex; flex-direction: column; gap: 12px; margin-top: 10px;">
			<a href="javascript:void(0)" id="btn-google" class="social-btn">

			<a href="javascript:void(0)" id="btn-facebook" class="social-btn" style="border: 1px solid #d9dadc; border-radius: 500px; padding: 12px 24px; text-decoration: none; color: #121212; font-weight: 700; display: flex; align-items: center;">
				<img src="https://upload.wikimedia.org/wikipedia/commons/0/05/Facebook_Logo_%282019%29.png" alt="Facebook" style="width: 20px; margin-right: 12px;">
				<span style="flex: 1; text-align: center;">Tiếp tục bằng Facebook</span>
			</a>
		</div>

		<div class="auth-divider"></div>

		<form method="post" action="${pageContext.request.contextPath}/login" class="auth-form">
			<input type="hidden" name="csrf_token" value="<c:out value='${sessionScope.CSRF_TOKEN}'/>">
			<div class="form-group">
				<label>Tên đăng nhập hoặc email</label>
				<input type="text" name="username" placeholder="Tên đăng nhập hoặc email" required style="width: 100%; height: 48px; padding: 0 15px; border-radius: 4px; border: 1px solid #727272;">
			</div>
			<div class="form-group">
				<label>Mật khẩu</label>
				<div class="input-group" style="position: relative; display: flex; align-items: center;">
					<input type="password" name="password" placeholder="Mật khẩu" required
					       style="width: 100%; height: 48px; padding: 0 40px 0 15px; border-radius: 4px; border: 1px solid #727272;">
					<span class="toggle-password" style="position: absolute; right: 15px; cursor: pointer; user-select: none; font-size: 18px; z-index: 10;">🙈</span>
				</div>
			</div>

			<div class="auth-footer" style="text-align: right; margin-top: 10px;">
				<a href="${pageContext.request.contextPath}/forgot-password" style="text-decoration: none; color: #555; font-size: 14px;">
					Quên mật khẩu?
				</a>
			</div>

			<button type="submit" class="btn-auth">Đăng nhập</button>
		</form>

		<div class="auth-footer">
			<p>Bạn chưa có tài khoản?</p>
			<a href="${pageContext.request.contextPath}/register" class="btn-outline-pill">
				ĐĂNG KÝ TÀI KHOẢN MYCOSMETIC
			</a>
		</div>
	</div>
</section>

<script>

	// 1. CẤU HÌNH GOOGLE LOGIN

	const GOOGLE_CLIENT_ID = "78979081819-fo21lsm5idv3pp22779bais8l1f5csnm.apps.googleusercontent.com";

	const contextPath = "${pageContext.request.contextPath}";

	window.onload = function () {

		google.accounts.id.initialize({
			client_id: GOOGLE_CLIENT_ID,

			callback: handleGoogleLogin
		});

		google.accounts.id.renderButton(
				document.getElementById("googleLoginBtn"),
				{
					theme: "outline",
					size: "large",
					width: 350,
					shape: "pill"
				}
		);
	};

	function handleGoogleLogin(response) {

		fetch(contextPath + '/social-auth', {
			method: 'POST',

			headers: {
				'Content-Type': 'application/x-www-form-urlencoded'
			},

			body: new URLSearchParams({
				provider: 'google',
				credential: response.credential,
				mode: 'login'
			})
		})
				.then(res => res.json())
				.then(data => {

					if (data.status === 'success') {

						Swal.fire({
							icon: 'success',
							title: 'Đăng nhập thành công'
						}).then(() => {
							window.location.href = contextPath + data.redirectUrl;
						});

					} else {

						Swal.fire({
							icon: 'error',
							title: 'Lỗi',
							text: data.message
						});
					}
				});
	}

	
	// 2. CẤU HÌNH FACEBOOK LOGIN
	
	window.fbAsyncInit = function() {
		FB.init({
			appId      : 'APP_ID_FACEBOOK_CUA_BAN', 
			cookie     : true,
			xfbml      : true,
			version    : 'v19.0'
		});
	};

	(function(d, s, id) {
		var js, fjs = d.getElementsByTagName(s)[0];
		if (d.getElementById(id)) return;
		js = d.createElement(s); js.id = id;
		js.src = "https://connect.facebook.net/vi_VN/sdk.js";
		fjs.parentNode.insertBefore(js, fjs);
	}(document, 'script', 'facebook-jssdk'));

	document.getElementById('btn-facebook').addEventListener('click', () => {
		FB.login(function(response) {
			if (response.authResponse) {
				sendTokenToServer('facebook', response.authResponse.accessToken);
			} else {
				Swal.fire('Lỗi', 'Bạn đã hủy đăng nhập Facebook', 'error');
			}
		}, {scope: 'public_profile,email'});
	});

	
	// 3. XỬ LÝ SỐ ĐIỆN THOẠI (Giả lập Frontend)
	
	document.getElementById('btn-phone').addEventListener('click', async () => {
		const { value: phone } = await Swal.fire({
			title: 'Nhập số điện thoại',
			input: 'tel',
			inputLabel: 'Số điện thoại của bạn',
			inputPlaceholder: 'VD: 0912345678',
			showCancelButton: true
		});

		if (phone) {
			// Thực tế bạn cần tích hợp Firebase Phone Auth ở bước này.
			// Dưới đây là bước giả lập UI nhập OTP
			const { value: otp } = await Swal.fire({
				title: 'Nhập mã OTP',
				text: `Mã OTP đã được gửi đến ${phone}`,
				input: 'text',
				showCancelButton: true
			});

			if (otp) {
				sendTokenToServer('phone', JSON.stringify({ phone: phone, otp: otp }));
			}
		}
	});

	
	// 4. HÀM GỬI TOKEN VỀ BACKEND (JAVA SERVLET)
	
	function sendTokenToServer(provider, token) {
		Swal.fire({
			title: 'Đang xử lý...',
			allowOutsideClick: false,
			didOpen: () => { Swal.showLoading(); }
		});

		// Đường dẫn này trỏ tới Servlet của bạn (VD: /social-auth)
		const contextPath = "${pageContext.request.contextPath}";

		fetch(contextPath + '/social-auth', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({
				'provider': provider,
				'token': token,
				'csrf_token': document.querySelector('input[name="csrf_token"]').value
			})
		})
				.then(response => response.json())
				.then(data => {
					if (data.status === 'success') {
						Swal.fire('Thành công!', 'Đăng nhập thành công.', 'success').then(() => {
							window.location.href = contextPath + data.redirectUrl;
						});
					} else {
						Swal.fire('Lỗi', data.message || 'Đăng nhập thất bại', 'error');
					}
				})
				.catch(error => {
					console.error('Error:', error);
					Swal.fire('Lỗi', 'Không thể kết nối đến máy chủ', 'error');
				});
	}
</script>

<script>
	// Hàm này chạy ngay khi trang login vừa tải xong
	window.addEventListener('load', function() {
		// Lấy thông số từ URL (ví dụ: login?status=success)
		const urlParams = new URLSearchParams(window.location.search);
		const status = urlParams.get('status');

		// Nếu status là 'success' thì hiện popup
		if (status === 'success') {
			Swal.fire({
				icon: 'success', // Biểu tượng thành công
				title: 'Đăng ký thành công!',
				text: 'Chào mừng bạn đến với MyCosmetic Shop. Vui lòng đăng nhập để bắt đầu mua sắm.',
				confirmButtonColor: '#3085d6',
				confirmButtonText: 'Đăng nhập ngay'
			});
		}
	});

const GOOGLE_CLIENT_ID = "78979081819-fo21lsm5idv3pp22779bais8l1f5csnm.apps.googleusercontent.com";
const contextPath = "${pageContext.request.contextPath}";

window.addEventListener("load", () => {
  google.accounts.id.initialize({
    client_id: GOOGLE_CLIENT_ID,
    callback: handleGoogleLogin
  });

  google.accounts.id.renderButton(
    document.getElementById("googleLoginBtn"),
    {
      theme: "outline",
      size: "large",
      shape: "pill",
      width: 320
    }
  );
});

function handleGoogleLogin(response) {
  fetch(contextPath + "/social-auth/google", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded"
    },
    body: new URLSearchParams({
      credential: response.credential
    })
  })
  .then(res => res.json())
  .then(data => {
    if (data.status === "success") {
      Swal.fire({
        icon: "success",
        title: "Đăng nhập thành công"
      }).then(() => {
        window.location.href = contextPath + data.redirectUrl;
      });
    } else {
      Swal.fire({
        icon: "error",
        title: "Lỗi",
        text: data.message
      });
    }
  })
  .catch(() => {
    Swal.fire("Lỗi", "Không thể kết nối server", "error");
  });
}
</script>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">
<script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
<script src="https://accounts.google.com/gsi/client" async defer></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

<section class="auth-page">
	<div class="auth-card">
		<h2 class="auth-title">Đăng nhập vào MyCosmetic</h2>

		<div class="social-login-stack" style="display: flex; flex-direction: column; align-items: center; gap: 12px; margin-top: 10px;">
			<div id="googleLoginBtn"></div>

			<a href="javascript:void(0)" id="btn-facebook" class="btn-facebook-custom">
				<img src="https://upload.wikimedia.org/wikipedia/commons/0/05/Facebook_Logo_%282019%29.png" alt="Facebook">
				<span>Đăng nhập bằng Facebook</span>
			</a>
		</div>

		<div class="auth-divider"></div>

		<form method="post" action="${pageContext.request.contextPath}/login" class="auth-form">
			<input type="hidden" name="csrf_token" value="<c:out value='${sessionScope.CSRF_TOKEN}'/>">
			<div class="form-group">
				<label>Tên đăng nhập hoặc email</label>
				<input type="text" name="username" id="usernameInput" placeholder="Tên đăng nhập hoặc email" required style="width: 100%; height: 48px; padding: 0 15px; border-radius: 4px; border: 1px solid #727272;">

				<span id="usernameError" style="color: red; font-size: 13px; display: none; margin-top: 5px;">
					Tên đăng nhập không được chứa chữ in hoa!
				</span>
			</div>

			<div class="form-group">
				<label class="auth-label">Mật khẩu</label>
				<div style="position: relative; display: flex; align-items: center;">
					<input type="password" name="password" class="auth-input" required placeholder="Nhập mật khẩu của bạn" style="padding-right: 40px; width: 100%;">
					<span class="toggle-btn" style="position: absolute; right: 15px; cursor: pointer; user-select: none;">🙈</span>
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
		fetch(contextPath + "/social-auth", {
			method: "POST",
			headers: {
				"Content-Type": "application/x-www-form-urlencoded"
			},
			body: new URLSearchParams({
				provider: "google",
				credential: response.credential,
				mode: "login"
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


	// 2. CẤU HÌNH FACEBOOK LOGIN

	window.fbAsyncInit = function() {
		FB.init({
			appId      : '1891459851533615',
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

	document.addEventListener("DOMContentLoaded", function () {

		// Logic Toggle Password
		var toggleBtns = document.getElementsByClassName("toggle-btn");

		for (var i = 0; i < toggleBtns.length; i++) {
			toggleBtns[i].addEventListener("click", function () {
				// Tìm container chứa input
				var container = this.closest('.form-group') || this.parentElement;
				var inp = container.querySelector("input");

				if (!inp) return;

				// Đảo trạng thái hiển thị
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

	

	document.addEventListener("DOMContentLoaded", function () {

		// 1. Khởi tạo SDK
		window.fbAsyncInit = function () {
			FB.init({
				appId: '1891459851533615',
				cookie: true,
				xfbml: true,
				version: 'v19.0'
			});
		};

		// 2. Tải SDK
		(function (d, s, id) {
			var js, fjs = d.getElementsByTagName(s)[0];
			if (d.getElementById(id)) return;
			js = d.createElement(s);
			js.id = id;
			js.src = "https://connect.facebook.net/vi_VN/sdk.js";
			fjs.parentNode.insertBefore(js, fjs);
		}(document, 'script', 'facebook-jssdk'));

		// 3. Xử lý sự kiện Click cho nút Facebook
		var btnFb = document.getElementById("btn-facebook");

		if (btnFb) {
			btnFb.addEventListener("click", function () {
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
							headers: { "Content-Type": "application/x-www-form-urlencoded" },
							body: new URLSearchParams({
								provider: "facebook",
								accessToken: accessToken,
								mode: "login"
							})
						})
								.then(res => res.json())
								.then(data => {
									if (data.status === "success") {
										Swal.fire({ icon: "success", title: "Thành công", timer: 1500, showConfirmButton: false })
												.then(() => { window.location.href = contextPath + data.redirectUrl; });
									} else {
										Swal.fire({ icon: "error", title: "Lỗi", text: data.message });
									}
								})
								.catch(err => {
									Swal.fire("Lỗi", "Không thể kết nối Server", "error");
								});
					} else {
						Swal.fire("Đã hủy", "Bạn chưa đăng nhập Facebook", "info");
					}
				}, { scope: 'public_profile,email' });
			});
		}
	});

	document.addEventListener("DOMContentLoaded", function () {
		const usernameInput = document.getElementById("usernameInput");
		const errorMsg = document.getElementById("usernameError");
		const loginForm = document.querySelector(".auth-form");

		// 1. Kiểm tra real-time khi người dùng đang nhập
		usernameInput.addEventListener("input", function() {
			if (/[A-Z]/.test(this.value)) {
				errorMsg.style.display = "block"; // Hiện lỗi
				this.style.borderColor = "red";   // Đổi viền đỏ
			} else {
				errorMsg.style.display = "none";  // Ẩn lỗi
				this.style.borderColor = "#727272"; // Trả về màu viền gốc
			}
		});

		// 2. Chặn submit truyền thống, chuyển sang AJAX fetch không tải lại trang
		loginForm.addEventListener("submit", function(e) {
			e.preventDefault(); // Tuyệt đối chặn việc tải lại trang mặc định

			if (/[A-Z]/.test(usernameInput.value)) {
				Swal.fire({
					icon: 'error',
					title: 'Lỗi định dạng',
					text: 'Tên đăng nhập không được phép chứa chữ in hoa. Vui lòng sửa lại!'
				});
				usernameInput.focus();
				return;
			}

			// Hiển thị hiệu ứng loading trong lúc chờ server phản hồi
			Swal.fire({
				title: 'Đang xử lý đăng nhập...',
				allowOutsideClick: false,
				didOpen: () => {
					Swal.showLoading();
				}
			});

			// Thu thập dữ liệu trong Form tự động (bao gồm cả csrf_token ẩn nếu có)
			const formData = new URLSearchParams(new FormData(loginForm));

			// Gửi AJAX request đến LoginServlet
			fetch(loginForm.action, {
				method: "POST",
				headers: {
					"Content-Type": "application/x-www-form-urlencoded"
				},
				body: formData
			})
					.then(res => {
						if (!res.ok) throw new Error("Mạng có sự cố hoặc lỗi Server");
						return res.json();
					})
					.then(data => {
						if (data.status === "success") {
							// Đăng nhập thành công hiển thị thông báo rồi chuyển trang
							Swal.fire({
								icon: "success",
								title: "Thành công!",
								text: data.message,
								timer: 1200,
								showConfirmButton: false
							}).then(() => {
								window.location.href = data.redirectUrl;
							});
						} else {
							// Thất bại hoặc bị khóa -> Hiện popup thông báo lỗi và Giữ nguyên trang để nhập lại
							Swal.fire({
								icon: "error",
								title: "Đăng nhập thất bại",
								text: data.message
							});
						}
					})
					.catch(err => {
						console.error("Login AJAX Error:", err);
						Swal.fire("Lỗi", "Không thể kết nối đến hệ thống máy chủ.", "error");
					});
		});
	});
</script>

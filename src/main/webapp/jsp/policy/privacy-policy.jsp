<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chính sách bảo mật | MyCosmetic</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link rel="stylesheet" href="${ctx}/assets/css/base.css">
    <link rel="stylesheet" href="${ctx}/assets/css/policy-pages.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
<jsp:include page="/jsp/common/header.jsp" />

<main class="policy-page-shell">
    <div class="policy-page-container">
        <nav class="policy-breadcrumb">
            <a href="${ctx}/home"><i class="fa-solid fa-house"></i> Trang chủ</a>
            <span>›</span>
            <span>Chính sách</span>
            <span>›</span>
            <span class="current">Chính sách bảo mật</span>
        </nav>

        <section class="policy-hero-simple">
            <span class="eyebrow">MYCOSMETIC POLICY</span>
            <h1>Chính sách bảo mật</h1>
            <p>Chính sách bảo mật giúp khách hàng hiểu rõ website thu thập những thông tin nào, sử dụng vào mục đích gì và cách MyCosmetic bảo vệ dữ liệu cá nhân trong quá trình mua sắm.</p>
            <span class="policy-tag">Bảo vệ dữ liệu cá nhân</span>
            <span class="policy-tag">Chỉ dùng cho mục đích cần thiết</span>
            <span class="policy-tag">Không bán thông tin khách hàng</span>
        </section>

        <section class="policy-layout-clear">
            <article class="policy-content-card">
                <div class="policy-content-intro">
                    <h2>Cách MyCosmetic bảo vệ thông tin khách hàng</h2>
                    <p>Chính sách bảo mật giúp khách hàng hiểu rõ website thu thập những thông tin nào, sử dụng vào mục đích gì và cách MyCosmetic bảo vệ dữ liệu cá nhân trong quá trình mua sắm.</p>
                </div>

                <section class="policy-content-section">
                    <h3 class="policy-section-title">
                        <span class="num">01</span>
                        <span>Thông tin được thu thập</span>
                    </h3>
                    <p>Shop chỉ thu thập các thông tin cần thiết để phục vụ mua hàng và chăm sóc khách hàng.</p>
                    <ul>
                        <li>Họ tên, số điện thoại, email và địa chỉ giao hàng.</li>
                        <li>Thông tin đơn hàng, lịch sử mua hàng, voucher và phương thức thanh toán.</li>
                        <li>Thông tin kỹ thuật cơ bản phục vụ bảo mật, đăng nhập và tối ưu trải nghiệm.</li>
                    </ul>
                </section>
                <section class="policy-content-section">
                    <h3 class="policy-section-title">
                        <span class="num">02</span>
                        <span>Mục đích sử dụng</span>
                    </h3>
                    <p>Thông tin được sử dụng đúng phạm vi nhằm xử lý giao dịch và hỗ trợ khách hàng.</p>
                    <ul>
                        <li>Xác nhận đơn hàng, giao hàng và gửi thông báo trạng thái đơn.</li>
                        <li>Hỗ trợ đổi trả, hoàn hàng, hủy đơn và chăm sóc sau mua.</li>
                        <li>Gửi thông tin khuyến mãi nếu khách hàng đồng ý nhận thông báo.</li>
                    </ul>
                </section>
                <section class="policy-content-section">
                    <h3 class="policy-section-title">
                        <span class="num">03</span>
                        <span>Chia sẻ thông tin</span>
                    </h3>
                    <p>MyCosmetic không bán dữ liệu khách hàng cho bên thứ ba.</p>
                    <ul>
                        <li>Thông tin chỉ được chia sẻ với đối tác vận chuyển hoặc thanh toán khi cần xử lý đơn hàng.</li>
                        <li>Có thể cung cấp thông tin khi có yêu cầu hợp lệ từ cơ quan có thẩm quyền.</li>
                        <li>Khách hàng có thể yêu cầu cập nhật thông tin cá nhân khi phát hiện sai lệch.</li>
                    </ul>
                    <div class="policy-highlight-note green">Bạn nên bảo mật tài khoản, không chia sẻ mật khẩu hoặc mã OTP cho người khác.</div>
                </section>

                <div class="policy-bottom-actions">
                    <a class="policy-btn primary" href="${ctx}/orders">Xem lịch sử đơn hàng</a>
                    <a class="policy-btn light" href="${ctx}/home">Quay lại trang chủ</a>
                </div>
            </article>

            <aside class="policy-sidebar">
                <section class="policy-sidebar-card">
                    <div class="policy-sidebar-title">
                        <span>Danh mục trang</span>
                        <i class="fa-solid fa-chevron-down"></i>
                    </div>
                    <div class="policy-side-menu">
                        <a class="" href="${ctx}/policy/cancel">
                            <span><i class="fa-solid fa-ban"></i>Chính sách hủy đơn</span>
                            <span>›</span>
                        </a>
                        <a class="" href="${ctx}/policy/return">
                            <span><i class="fa-solid fa-rotate-left"></i>Chính sách hoàn hàng</span>
                            <span>›</span>
                        </a>
                        <a class="active" href="${ctx}/policy/privacy">
                            <span><i class="fa-solid fa-shield-halved"></i>Chính sách bảo mật</span>
                            <span>›</span>
                        </a>
                        <a class="" href="${ctx}/policy/payment">
                            <span><i class="fa-solid fa-credit-card"></i>Chính sách thanh toán</span>
                            <span>›</span>
                        </a>
                        <a class="" href="${ctx}/policy/terms">
                            <span><i class="fa-solid fa-file-contract"></i>Điều khoản dịch vụ</span>
                            <span>›</span>
                        </a>
                        <a class="" href="${ctx}/policy/shopping-guide">
                            <span><i class="fa-solid fa-bag-shopping"></i>Hướng dẫn mua hàng</span>
                            <span>›</span>
                        </a>
                        <a class="" href="${ctx}/policy/vnpay-guide">
                            <span><i class="fa-solid fa-wallet"></i>Hướng dẫn VNPay</span>
                            <span>›</span>
                        </a>
                    </div>
                </section>

                <section class="policy-sidebar-card">
                    <div class="policy-summary-box">
                        <h3>Tóm tắt nhanh</h3>
                        <p>Những điểm chính bạn nên đọc trước khi thao tác.</p>
                        <div class="policy-summary-mini">
                            <div>
                                <strong>Thu thập</strong>
                                <span>Thông tin cơ bản để xử lý đơn.</span>
                            </div>
                            <div>
                                <strong>Sử dụng</strong>
                                <span>Giao hàng, thanh toán, hỗ trợ.</span>
                            </div>
                            <div>
                                <strong>Cam kết</strong>
                                <span>Không bán thông tin khách hàng.</span>
                            </div>
                        </div>
                    </div>
                </section>
            </aside>
        </section>
    </div>
</main>

<jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>

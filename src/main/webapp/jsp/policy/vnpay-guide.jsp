<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Hướng dẫn VNPay | MyCosmetic</title>
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
            <span class="current">Hướng dẫn VNPay</span>
        </nav>

        <section class="policy-hero-simple">
            <span class="eyebrow">MYCOSMETIC POLICY</span>
            <h1>Hướng dẫn VNPay</h1>
            <p>Trang này hướng dẫn khách hàng thanh toán online qua VNPay trên website MyCosmetic, bao gồm cách chọn phương thức, thực hiện giao dịch và kiểm tra kết quả sau khi thanh toán.</p>
            <span class="policy-tag">Chọn VNPay</span>
            <span class="policy-tag">Quét mã / chọn ngân hàng</span>
            <span class="policy-tag">Kiểm tra kết quả giao dịch</span>
        </section>

        <section class="policy-layout-clear">
            <article class="policy-content-card">
                <div class="policy-content-intro">
                    <h2>Cách thanh toán bằng VNPay</h2>
                    <p>Trang này hướng dẫn khách hàng thanh toán online qua VNPay trên website MyCosmetic, bao gồm cách chọn phương thức, thực hiện giao dịch và kiểm tra kết quả sau khi thanh toán.</p>
                </div>

                <section class="policy-content-section">
                    <h3 class="policy-section-title">
                        <span class="num">01</span>
                        <span>Quy trình thanh toán VNPay</span>
                    </h3>
                    <p>Thực hiện theo từng bước để tránh lỗi giao dịch.</p>
                    <div class="policy-step-list">
                        <div class="policy-step-item">
                            <div class="policy-step-index">1</div>
                            <div>
                                <strong>Chọn VNPay</strong>
                                <span>Tại trang thanh toán, chọn phương thức VNPay và kiểm tra lại tổng tiền.</span>
                            </div>
                        </div>
                        <div class="policy-step-item">
                            <div class="policy-step-index">2</div>
                            <div>
                                <strong>Chuyển sang cổng thanh toán</strong>
                                <span>Hệ thống điều hướng sang giao diện VNPay để bạn chọn ngân hàng hoặc quét mã QR.</span>
                            </div>
                        </div>
                        <div class="policy-step-item">
                            <div class="policy-step-index">3</div>
                            <div>
                                <strong>Hoàn tất giao dịch</strong>
                                <span>Làm theo hướng dẫn trên màn hình của VNPay hoặc ứng dụng ngân hàng.</span>
                            </div>
                        </div>
                        <div class="policy-step-item">
                            <div class="policy-step-index">4</div>
                            <div>
                                <strong>Nhận kết quả</strong>
                                <span>Sau khi thanh toán xong, hệ thống quay về MyCosmetic và cập nhật trạng thái đơn.</span>
                            </div>
                        </div>
                    </div>
                </section>
                <section class="policy-content-section">
                    <h3 class="policy-section-title">
                        <span class="num">02</span>
                        <span>Lưu ý quan trọng</span>
                    </h3>
                    <p>Các lưu ý giúp hạn chế lỗi khi thanh toán online.</p>
                    <ul>
                        <li>Không tắt trình duyệt trong khi giao dịch đang xử lý.</li>
                        <li>Đảm bảo tài khoản ngân hàng hoặc ví có đủ số dư.</li>
                        <li>Chỉ thanh toán trên cổng VNPay được điều hướng từ website MyCosmetic.</li>
                    </ul>
                    <div class="policy-highlight-note yellow">Nếu đã bị trừ tiền nhưng đơn chưa cập nhật, hãy lưu mã giao dịch và liên hệ shop để được kiểm tra.</div>
                </section>
                <section class="policy-content-section">
                    <h3 class="policy-section-title">
                        <span class="num">03</span>
                        <span>Khi giao dịch thất bại</span>
                    </h3>
                    <p>Nếu VNPay báo thất bại hoặc hết thời gian thanh toán, bạn có thể xử lý như sau.</p>
                    <ul>
                        <li>Quay lại trang đơn hàng để thanh toán lại nếu hệ thống cho phép.</li>
                        <li>Không thanh toán nhiều lần nếu chưa chắc giao dịch trước đã thất bại.</li>
                        <li>Liên hệ shop kèm mã đơn nếu cần kiểm tra trạng thái thanh toán.</li>
                    </ul>
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
                        <a class="" href="${ctx}/policy/privacy">
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
                        <a class="active" href="${ctx}/policy/vnpay-guide">
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
                                <strong>Bước đầu</strong>
                                <span>Chọn VNPay tại Checkout.</span>
                            </div>
                            <div>
                                <strong>Không tắt trang</strong>
                                <span>Chờ kết quả trả về website.</span>
                            </div>
                            <div>
                                <strong>Có lỗi</strong>
                                <span>Lưu mã giao dịch để đối chiếu.</span>
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

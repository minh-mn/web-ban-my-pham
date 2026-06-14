<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Hướng dẫn mua hàng | MyCosmetic</title>
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
            <span class="current">Hướng dẫn mua hàng</span>
        </nav>

        <section class="policy-hero-simple">
            <span class="eyebrow">MYCOSMETIC POLICY</span>
            <h1>Hướng dẫn mua hàng</h1>
            <p>Hướng dẫn mua hàng giúp khách mới thao tác nhanh hơn trên website: chọn sản phẩm, thêm vào giỏ, áp voucher, nhập thông tin nhận hàng và xác nhận thanh toán.</p>
            <span class="policy-tag">Chọn sản phẩm</span>
            <span class="policy-tag">Thêm vào giỏ hàng</span>
            <span class="policy-tag">Thanh toán đơn giản</span>
        </section>

        <section class="policy-layout-clear">
            <article class="policy-content-card">
                <div class="policy-content-intro">
                    <h2>Cách đặt hàng trên MyCosmetic</h2>
                    <p>Hướng dẫn mua hàng giúp khách mới thao tác nhanh hơn trên website: chọn sản phẩm, thêm vào giỏ, áp voucher, nhập thông tin nhận hàng và xác nhận thanh toán.</p>
                </div>

                <section class="policy-content-section">
                    <h3 class="policy-section-title">
                        <span class="num">01</span>
                        <span>Các bước đặt hàng</span>
                    </h3>
                    <p>Bạn có thể hoàn tất đơn hàng theo quy trình sau.</p>
                    <div class="policy-step-list">
                        <div class="policy-step-item">
                            <div class="policy-step-index">1</div>
                            <div>
                                <strong>Tìm sản phẩm</strong>
                                <span>Dùng thanh tìm kiếm, danh mục hoặc thương hiệu để chọn sản phẩm phù hợp.</span>
                            </div>
                        </div>
                        <div class="policy-step-item">
                            <div class="policy-step-index">2</div>
                            <div>
                                <strong>Thêm vào giỏ</strong>
                                <span>Chọn số lượng, phân loại nếu có rồi bấm Thêm vào giỏ hàng.</span>
                            </div>
                        </div>
                        <div class="policy-step-item">
                            <div class="policy-step-index">3</div>
                            <div>
                                <strong>Kiểm tra giỏ hàng</strong>
                                <span>Rà soát sản phẩm, số lượng, voucher, phí vận chuyển và tổng tiền.</span>
                            </div>
                        </div>
                        <div class="policy-step-item">
                            <div class="policy-step-index">4</div>
                            <div>
                                <strong>Nhập thông tin nhận hàng</strong>
                                <span>Điền họ tên, số điện thoại, địa chỉ và ghi chú nếu cần.</span>
                            </div>
                        </div>
                        <div class="policy-step-item">
                            <div class="policy-step-index">5</div>
                            <div>
                                <strong>Đặt hàng</strong>
                                <span>Chọn phương thức thanh toán rồi xác nhận đặt hàng.</span>
                            </div>
                        </div>
                    </div>
                </section>
                <section class="policy-content-section">
                    <h3 class="policy-section-title">
                        <span class="num">02</span>
                        <span>Theo dõi đơn hàng</span>
                    </h3>
                    <p>Sau khi đặt hàng, khách có thể theo dõi tiến trình xử lý trong tài khoản.</p>
                    <ul>
                        <li>Vào mục Đơn hàng để xem trạng thái xác nhận, lấy hàng, đang giao hoặc hoàn thành.</li>
                        <li>Bấm Xem chi tiết để xem sản phẩm đã mua, thông tin nhận hàng và lịch sử vận chuyển.</li>
                        <li>Nếu đơn đủ điều kiện, bạn có thể gửi yêu cầu hủy hoặc hoàn hàng từ chi tiết đơn.</li>
                    </ul>
                </section>
                <section class="policy-content-section">
                    <h3 class="policy-section-title">
                        <span class="num">03</span>
                        <span>Lưu ý khi mua hàng</span>
                    </h3>
                    <p>Một số điểm nên kiểm tra trước khi xác nhận đơn.</p>
                    <ul>
                        <li>Kiểm tra kỹ tên sản phẩm, số lượng, phân loại và giá tiền.</li>
                        <li>Nhập đúng địa chỉ, số điện thoại để tránh giao hàng thất bại.</li>
                        <li>Đọc chính sách hủy đơn, hoàn hàng và thanh toán trước khi đặt nếu cần.</li>
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
                        <a class="active" href="${ctx}/policy/shopping-guide">
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
                                <strong>Bắt đầu</strong>
                                <span>Tìm sản phẩm và thêm vào giỏ.</span>
                            </div>
                            <div>
                                <strong>Kiểm tra</strong>
                                <span>Số lượng, voucher, địa chỉ.</span>
                            </div>
                            <div>
                                <strong>Theo dõi</strong>
                                <span>Xem trạng thái trong Đơn hàng.</span>
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

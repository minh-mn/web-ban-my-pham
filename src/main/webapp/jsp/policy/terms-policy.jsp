<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Điều khoản dịch vụ | MyCosmetic</title>
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
            <span class="current">Điều khoản dịch vụ</span>
        </nav>

        <section class="policy-hero-simple">
            <span class="eyebrow">MYCOSMETIC POLICY</span>
            <h1>Điều khoản dịch vụ</h1>
            <p>Điều khoản dịch vụ giúp khách hàng hiểu rõ quyền lợi, trách nhiệm và phạm vi sử dụng khi truy cập, đăng ký tài khoản hoặc đặt hàng trên website MyCosmetic.</p>
            <span class="policy-tag">Quy định sử dụng website</span>
            <span class="policy-tag">Quyền và trách nhiệm</span>
            <span class="policy-tag">Áp dụng cho mọi khách hàng</span>
        </section>

        <section class="policy-layout-clear">
            <article class="policy-content-card">
                <div class="policy-content-intro">
                    <h2>Quy định sử dụng website MyCosmetic</h2>
                    <p>Điều khoản dịch vụ giúp khách hàng hiểu rõ quyền lợi, trách nhiệm và phạm vi sử dụng khi truy cập, đăng ký tài khoản hoặc đặt hàng trên website MyCosmetic.</p>
                </div>

                <section class="policy-content-section">
                    <h3 class="policy-section-title">
                        <span class="num">01</span>
                        <span>Phạm vi áp dụng</span>
                    </h3>
                    <p>Điều khoản áp dụng cho mọi khách hàng truy cập và sử dụng website.</p>
                    <ul>
                        <li>Khách hàng chịu trách nhiệm về thông tin đã cung cấp khi đặt hàng.</li>
                        <li>Không sử dụng website cho hành vi gian lận, phá hoại hoặc gây ảnh hưởng đến hệ thống.</li>
                        <li>Mọi giao dịch được thực hiện trên tinh thần minh bạch và thiện chí.</li>
                    </ul>
                </section>
                <section class="policy-content-section">
                    <h3 class="policy-section-title">
                        <span class="num">02</span>
                        <span>Quyền và trách nhiệm của MyCosmetic</span>
                    </h3>
                    <p>Shop có trách nhiệm vận hành website và hỗ trợ khách hàng trong phạm vi hợp lý.</p>
                    <ul>
                        <li>Cung cấp thông tin sản phẩm, giá bán, khuyến mãi và chính sách rõ ràng.</li>
                        <li>Tiếp nhận, xử lý đơn hàng và thông báo khi có phát sinh.</li>
                        <li>Có quyền từ chối hoặc hủy đơn nếu phát hiện gian lận, sai lệch thông tin hoặc lỗi hệ thống.</li>
                    </ul>
                </section>
                <section class="policy-content-section">
                    <h3 class="policy-section-title">
                        <span class="num">03</span>
                        <span>Giới hạn trách nhiệm</span>
                    </h3>
                    <p>Một số trường hợp ngoài phạm vi kiểm soát trực tiếp của shop.</p>
                    <ul>
                        <li>Chậm trễ do đơn vị vận chuyển, ngân hàng hoặc cổng thanh toán.</li>
                        <li>Gián đoạn website do bảo trì, lỗi hạ tầng hoặc sự kiện bất khả kháng.</li>
                        <li>Sai sót phát sinh do khách nhập sai thông tin đặt hàng hoặc thanh toán.</li>
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
                        <a class="active" href="${ctx}/policy/terms">
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
                                <strong>Áp dụng</strong>
                                <span>Khi truy cập và đặt hàng.</span>
                            </div>
                            <div>
                                <strong>Trách nhiệm</strong>
                                <span>Dùng thông tin chính xác.</span>
                            </div>
                            <div>
                                <strong>Cập nhật</strong>
                                <span>Điều khoản có thể thay đổi.</span>
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

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chính sách hủy đơn | MyCosmetic</title>
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
            <span class="current">Chính sách hủy đơn</span>
        </nav>

        <section class="policy-hero-simple">
            <span class="eyebrow">MYCOSMETIC POLICY</span>
            <h1>Chính sách hủy đơn</h1>
            <p>MyCosmetic hỗ trợ khách hàng hủy đơn ở giai đoạn phù hợp để hạn chế ảnh hưởng đến xử lý kho, đóng gói và vận chuyển. Chính sách này giúp bạn biết khi nào được hủy, khi nào cần shop kiểm tra thêm và cách hoàn tiền nếu đã thanh toán trước.</p>
            <span class="policy-tag">Đơn chưa giao vận chuyển dễ hủy hơn</span>
            <span class="policy-tag">Đơn đã thanh toán cần kiểm tra</span>
            <span class="policy-tag">Đang giao thì xem hoàn hàng</span>
        </section>

        <section class="policy-layout-clear">
            <article class="policy-content-card">
                <div class="policy-content-intro">
                    <h2>Điều kiện và quy trình hủy đơn</h2>
                    <p>MyCosmetic hỗ trợ khách hàng hủy đơn ở giai đoạn phù hợp để hạn chế ảnh hưởng đến xử lý kho, đóng gói và vận chuyển. Chính sách này giúp bạn biết khi nào được hủy, khi nào cần shop kiểm tra thêm và cách hoàn tiền nếu đã thanh toán trước.</p>
                </div>

                <section class="policy-content-section">
                    <h3 class="policy-section-title">
                        <span class="num">01</span>
                        <span>Khi nào được hủy đơn?</span>
                    </h3>
                    <p>Bạn có thể gửi yêu cầu hủy khi đơn chưa đi quá xa trong quy trình xử lý.</p>
                    <ul>
                        <li>Đơn hàng mới tạo, đang chờ xác nhận hoặc đã xác nhận nhưng chưa bàn giao cho đơn vị vận chuyển.</li>
                        <li>Đơn chưa chuyển sang trạng thái Đang giao, Giao thành công hoặc Hoàn thành.</li>
                        <li>Khách hàng cung cấp lý do hủy rõ ràng để shop kiểm tra và phản hồi nhanh hơn.</li>
                    </ul>
                    <div class="policy-highlight-note green">Nên gửi yêu cầu hủy càng sớm càng tốt nếu bạn đặt nhầm sản phẩm, sai số lượng hoặc muốn thay đổi địa chỉ.</div>
                </section>
                <section class="policy-content-section">
                    <h3 class="policy-section-title">
                        <span class="num">02</span>
                        <span>Khi nào không còn thể hủy?</span>
                    </h3>
                    <p>Một số đơn đã bước sang giai đoạn vận chuyển hoặc đã hoàn tất nên không thể xử lý như yêu cầu hủy thông thường.</p>
                    <ul>
                        <li>Đơn đã bàn giao cho đơn vị vận chuyển và đang trên đường giao.</li>
                        <li>Đơn đã giao thành công hoặc khách đã xác nhận nhận hàng.</li>
                        <li>Đơn đã hoàn tất xử lý hoặc đã có kết quả cuối cùng từ phía shop.</li>
                    </ul>
                    <div class="policy-highlight-note pink">Nếu đơn không còn đủ điều kiện hủy, bạn nên xem Chính sách hoàn hàng để được hỗ trợ đúng quy trình.</div>
                </section>
                <section class="policy-content-section">
                    <h3 class="policy-section-title">
                        <span class="num">03</span>
                        <span>Quy trình hủy đơn</span>
                    </h3>
                    <p>Quy trình hủy đơn được chia thành các bước ngắn gọn để khách dễ theo dõi.</p>
                    <div class="policy-step-list">
                        <div class="policy-step-item">
                            <div class="policy-step-index">1</div>
                            <div>
                                <strong>Tạo yêu cầu hủy</strong>
                                <span>Vào chi tiết đơn hàng, chọn Hủy đơn hàng và nhập lý do muốn hủy.</span>
                            </div>
                        </div>
                        <div class="policy-step-item">
                            <div class="policy-step-index">2</div>
                            <div>
                                <strong>Shop tiếp nhận</strong>
                                <span>Shop kiểm tra trạng thái xử lý, đóng gói, vận chuyển và thanh toán.</span>
                            </div>
                        </div>
                        <div class="policy-step-item">
                            <div class="policy-step-index">3</div>
                            <div>
                                <strong>Thông báo kết quả</strong>
                                <span>Khách nhận kết quả chấp nhận hoặc từ chối. Nếu có hoàn tiền, shop sẽ hướng dẫn phương thức phù hợp.</span>
                            </div>
                        </div>
                    </div>
                </section>
                <section class="policy-content-section">
                    <h3 class="policy-section-title">
                        <span class="num">04</span>
                        <span>Hoàn tiền khi hủy đơn</span>
                    </h3>
                    <p>Việc hoàn tiền phụ thuộc vào phương thức thanh toán và thời điểm yêu cầu hủy được duyệt.</p>
                    <ul>
                        <li>Đơn COD chưa thanh toán sẽ không phát sinh hoàn tiền.</li>
                        <li>Đơn thanh toán online hoặc chuyển khoản cần được shop đối chiếu giao dịch trước khi hoàn.</li>
                        <li>Thời gian hoàn tiền có thể phụ thuộc vào ngân hàng hoặc cổng thanh toán trung gian.</li>
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
                        <a class="active" href="${ctx}/policy/cancel">
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
                                <strong>Hủy dễ nhất khi</strong>
                                <span>Đơn còn mới tạo hoặc đang chờ xác nhận.</span>
                            </div>
                            <div>
                                <strong>Cần kiểm tra thêm</strong>
                                <span>Đơn đã thanh toán hoặc đang chuẩn bị giao.</span>
                            </div>
                            <div>
                                <strong>Không nên hủy</strong>
                                <span>Đơn đang giao nên chuyển sang chính sách hoàn hàng.</span>
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

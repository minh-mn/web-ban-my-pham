<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<style>
    :root {
        --pl-primary: #d94b79;
        --pl-primary-strong: #c53d6b;
        --pl-primary-soft: #fff5f8;
        --pl-primary-soft-2: #fff9fb;
        --pl-primary-border: #f3c7d6;
        --pl-primary-border-2: #efb4c8;
        --pl-navy: #18263d;
        --pl-text: #4f5d73;
        --pl-muted: #738198;
        --pl-line: #ebeff5;
        --pl-card: #ffffff;
        --pl-green: #219653;
        --pl-green-soft: #f2fcf6;
        --pl-green-border: #bfe8cd;
        --pl-yellow: #c08a14;
        --pl-yellow-soft: #fff9ea;
        --pl-yellow-border: #f3ddb0;
        --pl-red: #dc5c73;
        --pl-red-soft: #fff5f6;
        --pl-red-border: #f3c5ce;
        --pl-blue: #4f7bd9;
        --pl-blue-soft: #f4f7ff;
        --pl-blue-border: #cfdcfb;
        --pl-shadow: 0 14px 34px rgba(24, 38, 61, 0.06);
    }

    .policy-page {
        min-height: 80vh;
        padding: 28px 0 56px;
        background:
                radial-gradient(circle at left top, rgba(255, 233, 240, .9) 0 260px, transparent 360px),
                linear-gradient(180deg, #fff9fb 0%, #ffffff 58%, #fffafb 100%);
        color: var(--pl-text);
    }

    .policy-container {
        width: min(1120px, calc(100% - 32px));
        margin: 0 auto;
    }

    .policy-breadcrumb {
        margin-bottom: 14px;
    }

    .policy-breadcrumb a {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        text-decoration: none;
        color: var(--pl-primary);
        font-size: 14px;
        font-weight: 800;
    }

    .policy-breadcrumb a:hover {
        text-decoration: underline;
    }

    .policy-hero {
        display: grid;
        grid-template-columns: minmax(0, 1.45fr) minmax(280px, .75fr);
        gap: 18px;
        padding: 26px;
        margin-bottom: 18px;
        border-radius: 28px;
        border: 1px solid var(--pl-primary-border);
        background: linear-gradient(135deg, #fff3f7 0%, #ffeef4 100%);
        box-shadow: 0 12px 28px rgba(217, 75, 121, .08);
    }

    .policy-kicker {
        display: inline-flex;
        align-items: center;
        min-height: 30px;
        padding: 0 12px;
        border-radius: 999px;
        border: 1px solid var(--pl-primary-border);
        background: rgba(255,255,255,.82);
        color: var(--pl-primary);
        font-size: 12px;
        font-weight: 900;
    }

    .policy-title {
        margin: 12px 0 10px;
        color: var(--pl-navy);
        font-size: 38px;
        line-height: 1.12;
        letter-spacing: -.03em;
        font-weight: 900;
    }

    .policy-desc {
        margin: 0;
        color: var(--pl-muted);
        font-size: 15px;
        line-height: 1.7;
        font-weight: 650;
        max-width: 720px;
    }

    .policy-chip-row {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        margin-top: 16px;
    }

    .policy-chip {
        display: inline-flex;
        align-items: center;
        min-height: 34px;
        padding: 0 14px;
        border-radius: 999px;
        border: 1px solid var(--pl-primary-border);
        background: rgba(255,255,255,.85);
        color: var(--pl-primary-strong);
        font-size: 13px;
        font-weight: 800;
    }

    .policy-actions {
        display: flex;
        flex-direction: column;
        gap: 12px;
        padding: 18px;
        border-radius: 22px;
        border: 1px solid rgba(255,255,255,.9);
        background: rgba(255,255,255,.7);
    }

    .policy-actions-title {
        margin: 0;
        color: var(--pl-navy);
        font-size: 18px;
        font-weight: 900;
    }

    .policy-actions-desc {
        margin: -4px 0 4px;
        color: var(--pl-muted);
        font-size: 13px;
        line-height: 1.55;
        font-weight: 650;
    }

    .policy-btn-group {
        display: grid;
        gap: 10px;
    }

    .policy-btn {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        min-height: 44px;
        padding: 0 18px;
        border-radius: 14px;
        text-decoration: none;
        font-size: 14px;
        font-weight: 800;
        transition: .18s ease;
        white-space: nowrap;
    }

    .policy-btn:hover {
        transform: translateY(-1px);
    }

    .policy-btn-primary {
        color: #fff !important;
        background: linear-gradient(135deg, #e15a86 0%, #cf4673 100%);
        box-shadow: 0 10px 22px rgba(217, 75, 121, .14);
    }

    .policy-btn-secondary {
        color: var(--pl-primary-strong) !important;
        background: #fff;
        border: 1px solid var(--pl-primary-border-2);
    }

    .policy-btn-soft {
        color: var(--pl-navy) !important;
        background: #f8fbff;
        border: 1px solid var(--pl-line);
    }

    .policy-highlight-grid {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 14px;
        margin-bottom: 18px;
    }

    .policy-highlight-card {
        padding: 18px;
        border-radius: 22px;
        background: var(--pl-card);
        border: 1px solid #edf2f7;
        box-shadow: 0 8px 18px rgba(15, 23, 42, .03);
    }

    .policy-highlight-card .badge {
        display: inline-flex;
        padding: 6px 10px;
        border-radius: 999px;
        font-size: 12px;
        font-weight: 900;
        margin-bottom: 10px;
    }

    .policy-highlight-card h3 {
        margin: 0 0 8px;
        color: var(--pl-navy);
        font-size: 18px;
        line-height: 1.35;
        font-weight: 900;
    }

    .policy-highlight-card p {
        margin: 0;
        color: var(--pl-muted);
        font-size: 13px;
        line-height: 1.6;
        font-weight: 650;
    }

    .policy-highlight-card.green { border-color: var(--pl-green-border); background: #fcfffd; }
    .policy-highlight-card.green .badge { color: var(--pl-green); background: var(--pl-green-soft); }
    .policy-highlight-card.yellow { border-color: var(--pl-yellow-border); background: #fffdfa; }
    .policy-highlight-card.yellow .badge { color: var(--pl-yellow); background: var(--pl-yellow-soft); }
    .policy-highlight-card.red { border-color: var(--pl-red-border); background: #fffafb; }
    .policy-highlight-card.red .badge { color: var(--pl-red); background: var(--pl-red-soft); }
    .policy-highlight-card.blue { border-color: var(--pl-blue-border); background: #fbfcff; }
    .policy-highlight-card.blue .badge { color: var(--pl-blue); background: var(--pl-blue-soft); }

    .policy-body-grid {
        display: grid;
        grid-template-columns: minmax(0, 1.35fr) minmax(280px, .65fr);
        gap: 18px;
    }

    .policy-main,
    .policy-side {
        display: flex;
        flex-direction: column;
        gap: 18px;
    }

    .policy-section,
    .policy-side-card,
    .policy-cta {
        border-radius: 24px;
        border: 1px solid #edf2f7;
        background: #fff;
        box-shadow: 0 10px 24px rgba(15, 23, 42, .03);
    }

    .policy-section {
        padding: 22px 22px 20px;
    }

    .policy-section-top {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        margin-bottom: 10px;
    }

    .policy-number {
        flex: 0 0 auto;
        width: 30px;
        height: 30px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        border-radius: 999px;
        background: var(--pl-primary-soft);
        border: 1px solid var(--pl-primary-border);
        color: var(--pl-primary-strong);
        font-size: 14px;
        font-weight: 900;
    }

    .policy-section h2,
    .policy-side-card h3,
    .policy-cta h3 {
        margin: 0;
        color: var(--pl-navy);
        font-size: 20px;
        line-height: 1.3;
        font-weight: 900;
    }

    .policy-subtext {
        margin: 4px 0 0;
        color: var(--pl-muted);
        font-size: 13px;
        line-height: 1.6;
        font-weight: 650;
    }

    .policy-list {
        margin: 12px 0 0 0;
        padding-left: 20px;
    }

    .policy-list li {
        margin-bottom: 10px;
        color: var(--pl-text);
        font-size: 15px;
        line-height: 1.65;
        font-weight: 650;
    }

    .policy-note {
        margin-top: 14px;
        padding: 14px 16px;
        border-radius: 18px;
        font-size: 14px;
        line-height: 1.6;
        font-weight: 700;
    }

    .policy-note.info {
        color: var(--pl-blue);
        border: 1px solid var(--pl-blue-border);
        background: var(--pl-blue-soft);
    }

    .policy-note.warn {
        color: var(--pl-red);
        border: 1px solid var(--pl-red-border);
        background: var(--pl-red-soft);
    }

    .policy-steps {
        display: grid;
        gap: 12px;
        margin-top: 12px;
    }

    .policy-step {
        display: flex;
        gap: 14px;
        align-items: flex-start;
        padding: 16px;
        border-radius: 18px;
        background: #fcfdff;
        border: 1px solid var(--pl-line);
    }

    .policy-step-no {
        width: 34px;
        height: 34px;
        flex: 0 0 34px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        border-radius: 12px;
        background: var(--pl-primary-soft);
        color: var(--pl-primary-strong);
        font-size: 14px;
        font-weight: 900;
    }

    .policy-step h4 {
        margin: 0 0 4px;
        color: var(--pl-navy);
        font-size: 17px;
        font-weight: 850;
    }

    .policy-step p {
        margin: 0;
        color: var(--pl-muted);
        font-size: 14px;
        line-height: 1.6;
        font-weight: 650;
    }

    .policy-side-card {
        padding: 22px;
    }

    .policy-side-card + .policy-side-card {
        margin-top: 0;
    }

    .policy-side-card ul {
        list-style: none;
        margin: 14px 0 0;
        padding: 0;
    }

    .policy-side-card li {
        padding: 12px 0;
        border-bottom: 1px dashed var(--pl-line);
        color: var(--pl-text);
        font-size: 14px;
        line-height: 1.6;
        font-weight: 650;
    }

    .policy-side-card li:last-child {
        border-bottom: none;
        padding-bottom: 0;
    }

    .policy-side-card strong {
        display: block;
        margin-bottom: 4px;
        color: var(--pl-navy);
        font-weight: 850;
    }

    .policy-cta {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 18px;
        padding: 20px 22px;
        margin-top: 18px;
        background: linear-gradient(180deg, #fffefe 0%, #fff7fa 100%);
        border-color: var(--pl-primary-border);
    }

    .policy-cta p {
        margin: 6px 0 0;
        color: var(--pl-muted);
        font-size: 14px;
        line-height: 1.6;
        font-weight: 650;
    }

    @media (max-width: 992px) {
        .policy-hero,
        .policy-body-grid {
            grid-template-columns: 1fr;
        }

        .policy-highlight-grid {
            grid-template-columns: 1fr;
        }

        .policy-cta {
            flex-direction: column;
            align-items: stretch;
        }
    }

    @media (max-width: 640px) {
        .policy-page {
            padding: 20px 0 42px;
        }

        .policy-container {
            width: min(100% - 20px, 1120px);
        }

        .policy-hero,
        .policy-section,
        .policy-side-card,
        .policy-cta {
            padding: 18px;
            border-radius: 20px;
        }

        .policy-title {
            font-size: 30px;
        }

        .policy-btn-group {
            grid-template-columns: 1fr;
        }
    }

</style>

<section class="policy-page">
    <div class="policy-container">
        <div class="policy-breadcrumb">
            <a href="${pageContext.request.contextPath}/orders/detail?id=${param.id != null ? param.id : ''}">← Quay lại đơn hàng</a>
        </div>

        <div class="policy-hero">
            <div>
                <span class="policy-kicker">Chính sách mua hàng</span>
                <h1 class="policy-title">Chính sách hoàn hàng</h1>
                <p class="policy-desc">
                    MyCosmetic hỗ trợ hoàn hàng minh bạch để khách hàng yên tâm mua sắm. Nội dung dưới đây giúp bạn biết
                    khi nào được hỗ trợ, cần chuẩn bị gì và cách xử lý hoàn tiền để chủ động hơn trong quá trình làm việc với shop.
                </p>
                <div class="policy-chip-row">
                    <span class="policy-chip">Áp dụng sau khi giao thành công</span>
                    <span class="policy-chip">Gửi yêu cầu trong 7 ngày</span>
                    <span class="policy-chip">Shop kiểm tra trước khi hoàn tiền</span>
                </div>
            </div>

            <aside class="policy-actions">
                <h3 class="policy-actions-title">Thao tác liên quan</h3>
                <p class="policy-actions-desc">Dùng các nút bên dưới để chuyển nhanh tới trang phù hợp mà không bị nhầm giữa hủy đơn và hoàn hàng.</p>
                <div class="policy-btn-group">
                    <a class="policy-btn policy-btn-primary" href="${pageContext.request.contextPath}/orders">Xem lịch sử đơn hàng</a>
                    <a class="policy-btn policy-btn-secondary" href="${pageContext.request.contextPath}/policy/cancel">Chính sách hủy đơn</a>
                    <a class="policy-btn policy-btn-soft" href="${pageContext.request.contextPath}/orders">Đi tới trang đơn hàng</a>
                </div>
            </aside>
        </div>

        <div class="policy-highlight-grid">
            <article class="policy-highlight-card blue">
                <span class="badge">01 | Điều kiện</span>
                <h3>Đủ điều kiện tạo yêu cầu</h3>
                <p>Đơn đã giao thành công hoặc hoàn tất và khách gửi yêu cầu hỗ trợ trong thời hạn cho phép.</p>
            </article>
            <article class="policy-highlight-card green">
                <span class="badge">02 | Thời hạn</span>
                <h3>Hỗ trợ trong 7 ngày</h3>
                <p>Thời gian được tính từ lúc khách xác nhận đã nhận hàng hoặc hệ thống ghi nhận giao thành công.</p>
            </article>
            <article class="policy-highlight-card yellow">
                <span class="badge">03 | Kết quả</span>
                <h3>Hoàn tiền theo kiểm tra</h3>
                <p>Số tiền hoàn phụ thuộc vào tình trạng sản phẩm, lý do hoàn hàng và kết quả xác minh của shop.</p>
            </article>
        </div>

        <div class="policy-body-grid">
            <div class="policy-main">
                <section class="policy-section">
                    <div class="policy-section-top">
                        <span class="policy-number">1</span>
                        <div>
                            <h2>Khi nào được hoàn hàng?</h2>
                            <p class="policy-subtext">Bạn có thể gửi yêu cầu hoàn hàng khi đáp ứng đầy đủ các điều kiện sau.</p>
                        </div>
                    </div>
                    <ul class="policy-list">
                        <li>Đơn hàng đang ở trạng thái <strong>Giao thành công</strong> hoặc <strong>Hoàn thành</strong>.</li>
                        <li>Yêu cầu được gửi trong vòng <strong>7 ngày</strong> kể từ thời điểm nhận hàng thành công.</li>
                        <li>Sản phẩm còn đủ căn cứ để shop kiểm tra như ảnh/video, bao bì, tem niêm phong hoặc tình trạng thực tế.</li>
                    </ul>
                    <div class="policy-note info">Bạn nên chụp hình hoặc quay video ngay khi mở hàng để việc đối chiếu diễn ra nhanh và thuận tiện hơn.</div>
                </section>

                <section class="policy-section">
                    <div class="policy-section-top">
                        <span class="policy-number">2</span>
                        <div>
                            <h2>Các trường hợp được hỗ trợ</h2>
                            <p class="policy-subtext">MyCosmetic ưu tiên tiếp nhận các trường hợp phổ biến dưới đây.</p>
                        </div>
                    </div>
                    <ul class="policy-list">
                        <li>Sản phẩm bị lỗi, vỡ, rò rỉ hoặc hư hỏng trong quá trình vận chuyển.</li>
                        <li>Shop giao sai sản phẩm, sai màu, sai phân loại hoặc thiếu số lượng so với đơn đặt.</li>
                        <li>Sản phẩm có dấu hiệu cận hạn, hết hạn hoặc khác mô tả đã công bố trên website.</li>
                    </ul>
                </section>

                <section class="policy-section">
                    <div class="policy-section-top">
                        <span class="policy-number">3</span>
                        <div>
                            <h2>Các trường hợp có thể bị từ chối</h2>
                            <p class="policy-subtext">Để đảm bảo công bằng, một số yêu cầu sẽ không được chấp nhận.</p>
                        </div>
                    </div>
                    <ul class="policy-list">
                        <li>Sản phẩm đã qua sử dụng nhiều, không còn tem nhãn hoặc không còn đủ điều kiện kiểm tra.</li>
                        <li>Yêu cầu hoàn hàng vượt quá thời hạn chính sách.</li>
                        <li>Lỗi phát sinh do bảo quản sai hướng dẫn hoặc do tác động từ phía người dùng.</li>
                    </ul>
                    <div class="policy-note warn">Nếu yêu cầu không phù hợp với chính sách hoàn hàng, shop sẽ phản hồi rõ lý do để khách hàng dễ theo dõi.</div>
                </section>
            </div>

            <aside class="policy-side">
                <section class="policy-side-card">
                    <h3>Tóm tắt nhanh</h3>
                    <ul>
                        <li><strong>Trạng thái đơn phù hợp</strong> Đã giao thành công / Hoàn thành.</li>
                        <li><strong>Thời hạn gửi yêu cầu</strong> Trong vòng 7 ngày kể từ lúc nhận hàng.</li>
                        <li><strong>Cần chuẩn bị</strong> Lý do rõ ràng, mô tả tình trạng và bằng chứng nếu cần.</li>
                    </ul>
                </section>

                <section class="policy-side-card">
                    <h3>Hoàn tiền như thế nào?</h3>
                    <ul>
                        <li><strong>Đơn COD</strong> Hoàn tiền dựa trên kết quả kiểm tra và phương án xử lý được shop xác nhận.</li>
                        <li><strong>Đơn thanh toán online</strong> Hoàn qua phương thức phù hợp sau khi kiểm tra và chốt kết quả cuối cùng.</li>
                    </ul>
                </section>
            </aside>
        </div>

        <div class="policy-cta">
            <div>
                <h3>Sẵn sàng gửi yêu cầu?</h3>
                <p>Hãy mở chi tiết đơn hàng để kiểm tra trạng thái giao thành công và gửi yêu cầu hoàn hàng đúng quy trình.</p>
            </div>
            <a class="policy-btn policy-btn-primary" href="${pageContext.request.contextPath}/orders">Đi tới đơn hàng</a>
        </div>
    </div>
</section>

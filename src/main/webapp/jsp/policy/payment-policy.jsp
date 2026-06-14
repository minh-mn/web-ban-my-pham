<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Chính sách thanh toán | MyCosmetic</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">

  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

  <style>

    :root {
      --mc-primary: #e94073;
      --mc-primary-dark: #c91f56;
      --mc-primary-soft: #fff1f6;
      --mc-bg: #fff7fb;
      --mc-card: #ffffff;
      --mc-line: #f1d7e0;
      --mc-text: #17223b;
      --mc-muted: #66738b;
      --mc-green: #15985b;
      --mc-green-soft: #effbf4;
      --mc-yellow: #c28210;
      --mc-yellow-soft: #fff8e6;
      --mc-red: #d94b5f;
      --mc-red-soft: #fff2f4;
      --mc-blue: #2f72d6;
      --mc-blue-soft: #eef6ff;
      --mc-shadow: 0 18px 42px rgba(31, 41, 55, .08);
    }

    body { background: #ffffff; }

    .policy-page {
      min-height: 720px;
      padding: 24px 0 70px;
      background:
              radial-gradient(circle at 8% 6%, rgba(233, 64, 115, .10), transparent 310px),
              linear-gradient(180deg, #fff6fa 0%, #ffffff 34%, #fff8fb 100%);
      color: var(--mc-text);
    }

    .policy-wrap {
      width: min(1200px, calc(100% - 56px));
      margin: 0 auto;
    }

    .policy-breadcrumb {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 8px;
      margin: 0 0 18px;
      color: var(--mc-muted);
      font-size: 13px;
      font-weight: 700;
    }

    .policy-breadcrumb a {
      color: var(--mc-muted);
      text-decoration: none;
      transition: .18s ease;
    }

    .policy-breadcrumb a:hover { color: var(--mc-primary); }

    .policy-breadcrumb .current {
      color: var(--mc-primary);
      font-weight: 900;
    }

    .policy-hero {
      display: grid;
      grid-template-columns: minmax(0, 1fr) 320px;
      gap: 26px;
      align-items: stretch;
      margin-bottom: 22px;
      padding: 30px;
      border: 1px solid #f2c7d4;
      border-radius: 28px;
      background:
              radial-gradient(circle at 5% 0%, rgba(233, 64, 115, .17), transparent 330px),
              linear-gradient(135deg, #fff2f7 0%, #fff9fc 58%, #effaff 100%);
      box-shadow: var(--mc-shadow);
    }

    .policy-chip {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      width: fit-content;
      min-height: 32px;
      padding: 0 13px;
      margin-bottom: 13px;
      border-radius: 999px;
      border: 1px solid #f1c2d0;
      background: rgba(255, 255, 255, .82);
      color: var(--mc-primary);
      font-size: 12px;
      font-weight: 900;
      letter-spacing: .02em;
    }

    .policy-hero h1 {
      margin: 0 0 12px;
      color: var(--mc-text);
      font-size: clamp(30px, 3.2vw, 44px);
      font-weight: 950;
      line-height: 1.12;
    }

    .policy-hero p {
      margin: 0;
      max-width: 760px;
      color: var(--mc-muted);
      font-size: 15px;
      font-weight: 650;
      line-height: 1.8;
    }

    .policy-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      margin-top: 18px;
    }

    .policy-tags span {
      display: inline-flex;
      align-items: center;
      min-height: 34px;
      padding: 0 14px;
      border-radius: 999px;
      border: 1px solid #f2cad6;
      background: #fff;
      color: var(--mc-primary);
      font-size: 13px;
      font-weight: 850;
    }

    .policy-action-box {
      align-self: stretch;
      padding: 20px;
      border-radius: 22px;
      background: rgba(255, 255, 255, .74);
      border: 1px solid rgba(255, 255, 255, .88);
      box-shadow: 0 12px 30px rgba(233, 64, 115, .08);
    }

    .policy-action-box h2 {
      margin: 0 0 8px;
      color: var(--mc-text);
      font-size: 20px;
      font-weight: 950;
    }

    .policy-action-box p {
      margin: 0 0 14px;
      color: var(--mc-muted);
      font-size: 13px;
      line-height: 1.65;
    }

    .policy-action-stack { display: grid; gap: 10px; }

    .policy-action-btn {
      display: flex;
      align-items: center;
      justify-content: space-between;
      min-height: 46px;
      padding: 0 15px;
      border-radius: 14px;
      text-decoration: none;
      font-size: 13px;
      font-weight: 900;
      transition: .18s ease;
    }

    .policy-action-btn.primary {
      color: #fff;
      background: linear-gradient(135deg, var(--mc-primary), var(--mc-primary-dark));
      box-shadow: 0 12px 24px rgba(233, 64, 115, .20);
    }

    .policy-action-btn.blue {
      color: #235da8;
      background: var(--mc-blue-soft);
      border: 1px solid #cfe3ff;
    }

    .policy-action-btn.light {
      color: var(--mc-primary);
      background: #fff;
      border: 1px solid #f1c2d0;
    }

    .policy-action-btn:hover { transform: translateY(-2px); }

    .policy-state-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 16px;
      margin-bottom: 22px;
    }

    .policy-state-card {
      min-height: 150px;
      padding: 20px;
      border-radius: 22px;
      background: #fff;
      box-shadow: var(--mc-shadow);
    }

    .policy-state-card.ok {
      border: 1px solid #bee8cd;
      background: linear-gradient(180deg, #f5fff8, #ffffff);
    }

    .policy-state-card.check {
      border: 1px solid #f1d486;
      background: linear-gradient(180deg, #fffaf0, #ffffff);
    }

    .policy-state-card.no {
      border: 1px solid #f3c2cb;
      background: linear-gradient(180deg, #fff4f6, #ffffff);
    }

    .state-label {
      display: inline-flex;
      align-items: center;
      min-height: 28px;
      padding: 0 10px;
      margin-bottom: 12px;
      border-radius: 999px;
      font-size: 12px;
      font-weight: 950;
    }

    .ok .state-label { color: var(--mc-green); background: #e9fff2; }
    .check .state-label { color: var(--mc-yellow); background: #fff1c9; }
    .no .state-label { color: var(--mc-red); background: #ffe8ed; }

    .policy-state-card h3 {
      margin: 0 0 9px;
      color: var(--mc-text);
      font-size: 19px;
      font-weight: 950;
      line-height: 1.35;
    }

    .policy-state-card p {
      margin: 0;
      color: var(--mc-muted);
      font-size: 14px;
      font-weight: 650;
      line-height: 1.65;
    }

    .policy-main-layout {
      display: grid;
      grid-template-columns: minmax(0, 1fr) 330px;
      gap: 22px;
      align-items: start;
    }

    .policy-content-list { display: grid; gap: 18px; }

    .policy-section {
      padding: 24px;
      border: 1px solid #f0d9e1;
      border-radius: 22px;
      background: #fff;
      box-shadow: var(--mc-shadow);
    }

    .section-heading {
      display: flex;
      gap: 13px;
      align-items: flex-start;
      margin-bottom: 14px;
    }

    .section-no {
      width: 36px;
      height: 36px;
      flex: 0 0 36px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      border-radius: 13px;
      color: var(--mc-primary);
      background: var(--mc-primary-soft);
      border: 1px solid #f1c2d0;
      font-weight: 950;
    }

    .section-heading h2 {
      margin: 0;
      color: var(--mc-text);
      font-size: 23px;
      font-weight: 950;
      line-height: 1.3;
    }

    .policy-section .lead {
      margin: -4px 0 14px 49px;
      color: var(--mc-muted);
      font-size: 14px;
      font-weight: 650;
      line-height: 1.7;
    }

    .policy-section ul {
      margin: 0;
      padding: 0;
      list-style: none;
    }

    .policy-section li {
      position: relative;
      margin-bottom: 11px;
      padding-left: 25px;
      color: #30405c;
      font-size: 15px;
      line-height: 1.75;
    }

    .policy-section li:last-child { margin-bottom: 0; }

    .policy-section li::before {
      content: "";
      position: absolute;
      left: 5px;
      top: 12px;
      width: 7px;
      height: 7px;
      border-radius: 50%;
      background: var(--mc-primary);
      box-shadow: 0 0 0 4px rgba(233, 64, 115, .10);
    }

    .policy-note {
      margin-top: 16px;
      padding: 14px 16px;
      border-radius: 16px;
      border: 1px solid #cfe3ff;
      background: var(--mc-blue-soft);
      color: #2f5f9f;
      font-size: 14px;
      font-weight: 750;
      line-height: 1.7;
    }

    .policy-note.pink {
      border-color: #f1c2d0;
      background: var(--mc-primary-soft);
      color: #be2f60;
    }

    .policy-note.green {
      border-color: #bfe9cd;
      background: var(--mc-green-soft);
      color: #137649;
    }

    .policy-note.yellow {
      border-color: #f0d486;
      background: var(--mc-yellow-soft);
      color: #8d6211;
    }

    .process-list {
      display: grid;
      gap: 12px;
      margin-top: 6px;
    }

    .process-item {
      display: grid;
      grid-template-columns: 42px 1fr;
      gap: 13px;
      padding: 15px;
      border-radius: 17px;
      border: 1px solid #f0d9e1;
      background: #fffafd;
    }

    .process-item .index {
      width: 42px;
      height: 42px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      border-radius: 15px;
      color: var(--mc-primary);
      background: var(--mc-primary-soft);
      font-weight: 950;
    }

    .process-item strong {
      display: block;
      margin-bottom: 5px;
      color: var(--mc-text);
      font-size: 15px;
    }

    .process-item span {
      color: var(--mc-muted);
      font-size: 14px;
      line-height: 1.65;
    }

    .policy-sidebar {
      position: sticky;
      top: 96px;
      display: grid;
      gap: 18px;
    }

    .side-card {
      padding: 20px;
      border-radius: 22px;
      border: 1px solid #f0d9e1;
      background: #fff;
      box-shadow: var(--mc-shadow);
    }

    .side-card h3 {
      margin: 0 0 10px;
      color: var(--mc-text);
      font-size: 20px;
      font-weight: 950;
    }

    .side-card p {
      margin: 0 0 15px;
      color: var(--mc-muted);
      font-size: 13px;
      font-weight: 650;
      line-height: 1.65;
    }

    .summary-list { display: grid; gap: 12px; }

    .summary-item {
      padding: 13px 0;
      border-top: 1px dashed #e6d6dd;
    }

    .summary-item strong {
      display: block;
      margin-bottom: 5px;
      color: var(--mc-text);
      font-size: 14px;
    }

    .summary-item span {
      color: var(--mc-muted);
      font-size: 13px;
      line-height: 1.6;
    }

    .policy-menu { display: grid; gap: 8px; }

    .policy-menu a {
      display: flex;
      align-items: center;
      justify-content: space-between;
      min-height: 42px;
      padding: 0 12px;
      border-radius: 13px;
      color: #35435c;
      background: #fff;
      border: 1px solid #f0d9e1;
      text-decoration: none;
      font-size: 13px;
      font-weight: 850;
      transition: .18s ease;
    }

    .policy-menu a:hover,
    .policy-menu a.active {
      color: #fff;
      border-color: transparent;
      background: linear-gradient(135deg, var(--mc-primary), var(--mc-primary-dark));
    }

    .policy-menu i { margin-right: 8px; }

    .policy-bottom-cta {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 18px;
      margin-top: 22px;
      padding: 18px 20px;
      border: 1px solid #f0c8d4;
      border-radius: 20px;
      background: #fff8fb;
    }

    .policy-bottom-cta strong {
      color: var(--mc-text);
      font-size: 15px;
    }

    .policy-bottom-cta span {
      color: var(--mc-muted);
      font-size: 14px;
      font-weight: 650;
    }

    .policy-bottom-cta a {
      min-height: 44px;
      padding: 0 18px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      border-radius: 999px;
      color: #fff;
      text-decoration: none;
      background: linear-gradient(135deg, var(--mc-primary), var(--mc-primary-dark));
      font-size: 14px;
      font-weight: 900;
      white-space: nowrap;
      box-shadow: 0 12px 24px rgba(233, 64, 115, .18);
    }

    @media (max-width: 1100px) {
      .policy-hero,
      .policy-main-layout { grid-template-columns: 1fr; }
      .policy-sidebar { position: static; }
    }

    @media (max-width: 820px) {
      .policy-wrap { width: min(100% - 28px, 100%); }

      .policy-hero,
      .policy-section,
      .side-card {
        padding: 20px;
        border-radius: 20px;
      }

      .policy-state-grid { grid-template-columns: 1fr; }

      .policy-bottom-cta {
        align-items: flex-start;
        flex-direction: column;
      }

      .policy-bottom-cta a { width: 100%; }
    }

  </style>
</head>
<body>
<jsp:include page="/jsp/common/header.jsp" />

<main class="policy-page">
  <div class="policy-wrap">
    <nav class="policy-breadcrumb">
      <a href="${pageContext.request.contextPath}/home"><i class="fa-solid fa-house"></i> Trang chủ</a>
      <span>›</span>
      <span>Chính sách</span>
      <span>›</span>
      <span class="current">Chính sách thanh toán</span>
    </nav>

    <section class="policy-hero">
      <div>
                <span class="policy-chip">
                    <i class="fa-solid fa-file-shield"></i>
                    MYCOSMETIC POLICY
                </span>
        <h1>Chính sách thanh toán</h1>
        <p>Chính sách thanh toán trình bày rõ các phương thức đang hỗ trợ, thời điểm xác nhận thanh toán và những lưu ý giúp khách hàng tránh sai sót khi giao dịch.</p>

        <div class="policy-tags">
          <span>COD / Chuyển khoản / VNPay</span>
          <span>Xác minh minh bạch</span>
          <span>Lưu ý trước khi giao dịch</span>
        </div>
      </div>

      <aside class="policy-action-box">
        <h2>Thao tác liên quan</h2>
        <p>Kiểm tra phương thức thanh toán phù hợp trước khi đặt hàng hoặc thanh toán lại.</p>

        <div class="policy-action-stack">
          <a class="policy-action-btn primary" href="${pageContext.request.contextPath}/products">
            <span><i class="fa-solid fa-cart-shopping"></i> Tiếp tục mua hàng</span>
            <span>→</span>
          </a>
          <a class="policy-action-btn blue" href="${pageContext.request.contextPath}/policy/vnpay-guide">
            <span><i class="fa-solid fa-wallet"></i> Hướng dẫn VNPay</span>
            <span>→</span>
          </a>
          <a class="policy-action-btn light" href="${pageContext.request.contextPath}/orders">
            <span><i class="fa-solid fa-box"></i> Xem đơn hàng</span>
            <span>→</span>
          </a>
        </div>
      </aside>
    </section>

    <section class="policy-state-grid">
      <article class="policy-state-card ok">
        <span class="state-label">01 | COD</span>
        <h3>Thanh toán khi nhận hàng</h3>
        <p>Khách thanh toán trực tiếp khi đơn được giao thành công.</p>
      </article>
      <article class="policy-state-card check">
        <span class="state-label">02 | Online</span>
        <h3>VNPay hoặc chuyển khoản</h3>
        <p>Đơn thanh toán online cần chờ kết quả giao dịch hoặc shop đối chiếu.</p>
      </article>
      <article class="policy-state-card no">
        <span class="state-label">03 | An toàn</span>
        <h3>Không thanh toán ngoài hệ thống</h3>
        <p>Chỉ thanh toán qua các kênh được MyCosmetic công bố chính thức.</p>
      </article>
    </section>

    <section class="policy-main-layout">
      <div class="policy-content-list">
        <article class="policy-section">
          <div class="section-heading">
            <span class="section-no">1</span>
            <h2>Phương thức thanh toán</h2>
          </div>
          <p class="lead">MyCosmetic hỗ trợ nhiều phương thức để khách hàng lựa chọn.</p>
          <ul>
            <li>COD: thanh toán khi nhận hàng.</li>
            <li>Chuyển khoản ngân hàng theo thông tin shop cung cấp.</li>
            <li>Thanh toán online qua VNPay nếu đơn hàng hỗ trợ phương thức này.</li>
          </ul>
        </article>
        <article class="policy-section">
          <div class="section-heading">
            <span class="section-no">2</span>
            <h2>Xác nhận thanh toán</h2>
          </div>
          <p class="lead">Mỗi phương thức sẽ có cách xác nhận khác nhau để đảm bảo an toàn.</p>
          <ul>
            <li>Đơn COD được ghi nhận đã thanh toán khi khách thanh toán thành công lúc nhận hàng.</li>
            <li>Đơn chuyển khoản được xác nhận sau khi shop đối chiếu đúng số tiền và mã đơn.</li>
            <li>Đơn VNPay phụ thuộc kết quả giao dịch trả về từ cổng thanh toán.</li>
          </ul>
        </article>
        <article class="policy-section">
          <div class="section-heading">
            <span class="section-no">3</span>
            <h2>Lưu ý khi thanh toán</h2>
          </div>
          <p class="lead">Khách hàng nên kiểm tra kỹ thông tin trước khi hoàn tất giao dịch.</p>
          <ul>
            <li>Không thanh toán ngoài các kênh shop công bố chính thức.</li>
            <li>Kiểm tra tổng tiền, phí vận chuyển, mã giảm giá và thông tin nhận hàng trước khi đặt đơn.</li>
            <li>Lưu lại biên lai hoặc mã giao dịch nếu cần đối chiếu.</li>
          </ul>
          <div class="policy-note yellow">Nếu đã thanh toán nhưng đơn chưa cập nhật, hãy gửi mã đơn và ảnh giao dịch cho shop để được kiểm tra nhanh.</div>
        </article>

        <div class="policy-bottom-cta">
          <div>
            <strong>Cần thao tác ngay?</strong>
            <span> Vào trang đơn hàng để kiểm tra trạng thái thanh toán hoặc thanh toán lại nếu được hỗ trợ.</span>
          </div>
          <a href="${pageContext.request.contextPath}/orders">Đi tới trang phù hợp</a>
        </div>
      </div>

      <aside class="policy-sidebar">
        <section class="side-card">
          <h3>Tóm tắt nhanh</h3>
          <p>Đọc nhanh các điểm quan trọng trước khi thao tác.</p>

          <div class="summary-list">
            <div class="summary-item">
              <strong>Hỗ trợ</strong>
              <span>COD, chuyển khoản, VNPay.</span>
            </div>
            <div class="summary-item">
              <strong>Xác nhận</strong>
              <span>Theo từng phương thức thanh toán.</span>
            </div>
            <div class="summary-item">
              <strong>Nên lưu</strong>
              <span>Biên lai hoặc mã giao dịch.</span>
            </div>
          </div>
        </section>

        <section class="side-card">
          <h3>Danh mục chính sách</h3>
          <p>Chuyển nhanh sang các trang chính sách liên quan.</p>

          <div class="policy-menu">
            <a class="" href="${pageContext.request.contextPath}/policy/cancel">
              <span><i class="fa-solid fa-ban"></i>Chính sách hủy đơn</span>
              <span>›</span>
            </a>
            <a class="" href="${pageContext.request.contextPath}/policy/return">
              <span><i class="fa-solid fa-rotate-left"></i>Chính sách hoàn hàng</span>
              <span>›</span>
            </a>
            <a class="" href="${pageContext.request.contextPath}/policy/privacy">
              <span><i class="fa-solid fa-shield-halved"></i>Chính sách bảo mật</span>
              <span>›</span>
            </a>
            <a class="active" href="${pageContext.request.contextPath}/policy/payment">
              <span><i class="fa-solid fa-credit-card"></i>Chính sách thanh toán</span>
              <span>›</span>
            </a>
            <a class="" href="${pageContext.request.contextPath}/policy/terms">
              <span><i class="fa-solid fa-file-contract"></i>Điều khoản dịch vụ</span>
              <span>›</span>
            </a>
            <a class="" href="${pageContext.request.contextPath}/policy/shopping-guide">
              <span><i class="fa-solid fa-bag-shopping"></i>Hướng dẫn mua hàng</span>
              <span>›</span>
            </a>
            <a class="" href="${pageContext.request.contextPath}/policy/vnpay-guide">
              <span><i class="fa-solid fa-wallet"></i>Hướng dẫn VNPay</span>
              <span>›</span>
            </a>
          </div>
        </section>
      </aside>
    </section>
  </div>
</main>

<jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>

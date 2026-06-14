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


  /* =========================================================
     DISTINCT ACTION BUTTONS - CANCEL POLICY
  ========================================================= */

  .policy-btn-group-distinct {
    display: grid;
    gap: 12px;
  }

  .policy-btn-history,
  .policy-btn-return,
  .policy-btn-order-check {
    min-height: 48px;
    border-radius: 15px;
    font-weight: 850;
    letter-spacing: 0.1px;
    box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
    transition: all 0.2s ease;
  }

  .policy-btn-history:hover,
  .policy-btn-return:hover,
  .policy-btn-order-check:hover {
    transform: translateY(-2px);
  }

  /* Lịch sử đơn hàng - hồng chủ đạo, là hành động chính */
  .policy-btn-history {
    color: #ffffff !important;
    background: linear-gradient(135deg, #e85b88 0%, #cf3f6f 100%);
    border: 1px solid transparent;
    box-shadow: 0 12px 24px rgba(217, 75, 121, 0.18);
  }

  .policy-btn-history:hover {
    box-shadow: 0 14px 28px rgba(217, 75, 121, 0.22);
  }

  /* Chính sách hoàn hàng - cam vàng nhạt, phân biệt với nút chính */
  .policy-btn-return {
    color: #a55b08 !important;
    background: linear-gradient(135deg, #fff8eb 0%, #ffefd0 100%);
    border: 1px solid #f0d49a;
  }

  .policy-btn-return:hover {
    background: linear-gradient(135deg, #fff4df 0%, #ffe7b8 100%);
    box-shadow: 0 12px 24px rgba(214, 162, 52, 0.12);
  }

  /* Đi tới trang đơn hàng - xanh nhạt, dùng cho điều hướng / kiểm tra */
  .policy-btn-order-check {
    color: #295f9e !important;
    background: linear-gradient(135deg, #f4f8ff 0%, #eaf2ff 100%);
    border: 1px solid #c9daf8;
  }

  .policy-btn-order-check:hover {
    background: linear-gradient(135deg, #eef5ff 0%, #dfecff 100%);
    box-shadow: 0 12px 24px rgba(79, 123, 217, 0.12);
  }

  .policy-actions {
    gap: 14px;
    padding: 20px;
  }

  .policy-actions-desc {
    margin: 0 0 6px;
    line-height: 1.6;
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
        <h1 class="policy-title">Chính sách hủy đơn</h1>
        <p class="policy-desc">
          MyCosmetic hỗ trợ khách hủy đơn trong giai đoạn phù hợp để hạn chế ảnh hưởng đến quá trình xử lý,
          đóng gói và vận chuyển. Nội dung dưới đây giúp bạn biết khi nào có thể hủy đơn, khi nào cần shop
          kiểm tra thêm và khi nào cần chuyển sang chính sách hoàn hàng.
        </p>
        <div class="policy-chip-row">
          <span class="policy-chip">Ưu tiên khi đơn chưa giao vận chuyển</span>
          <span class="policy-chip">Đơn đã thanh toán có thể cần kiểm tra</span>
          <span class="policy-chip">Nếu đang giao, xem chính sách hoàn hàng</span>
        </div>
      </div>

      <aside class="policy-actions">
        <h3 class="policy-actions-title">Thao tác liên quan</h3>
        <p class="policy-actions-desc">Chọn đúng trang để kiểm tra trạng thái đơn hoặc xem thêm chính sách phù hợp.</p>
        <div class="policy-btn-group policy-btn-group-distinct">
          <a class="policy-btn policy-btn-history" href="${pageContext.request.contextPath}/orders">
            Xem lịch sử đơn hàng
          </a>

          <a class="policy-btn policy-btn-return" href="${pageContext.request.contextPath}/policy/return">
            Chính sách hoàn hàng
          </a>

          <a class="policy-btn policy-btn-order-check" href="${pageContext.request.contextPath}/orders">
            Đi tới trang đơn hàng
          </a>
        </div>
      </aside>
    </div>

    <div class="policy-highlight-grid">
      <article class="policy-highlight-card green">
        <span class="badge">01 | Nên hủy sớm</span>
        <h3>Đơn chưa bàn giao vận chuyển</h3>
        <p>Đơn mới tạo, chờ xác nhận hoặc đã xác nhận nhưng chưa giao cho đơn vị vận chuyển thường được xử lý nhanh hơn.</p>
      </article>
      <article class="policy-highlight-card yellow">
        <span class="badge">02 | Cần kiểm tra thêm</span>
        <h3>Đơn đã xác nhận / đã thanh toán</h3>
        <p>Một số đơn cần shop rà soát trạng thái xử lý và phương thức thanh toán trước khi chốt kết quả hủy.</p>
      </article>
      <article class="policy-highlight-card red">
        <span class="badge">03 | Không còn phù hợp</span>
        <h3>Đơn đang giao hoặc đã hoàn tất</h3>
        <p>Khi đơn đã chuyển sang vận chuyển hoặc đã giao thành công, bạn nên tham khảo chính sách hoàn hàng thay vì hủy đơn.</p>
      </article>
    </div>

    <div class="policy-body-grid">
      <div class="policy-main">
        <section class="policy-section">
          <div class="policy-section-top">
            <span class="policy-number">1</span>
            <div>
              <h2>Khi nào được hủy đơn?</h2>
              <p class="policy-subtext">Đây là những trường hợp khách hàng có thể gửi yêu cầu hủy trực tiếp từ trang chi tiết đơn hàng.</p>
            </div>
          </div>
          <ul class="policy-list">
            <li>Đơn hàng vẫn ở trạng thái <strong>Mới tạo</strong>, <strong>Chờ xác nhận</strong> hoặc <strong>Đã xác nhận nhưng chưa bàn giao cho đơn vị vận chuyển</strong>.</li>
            <li>Đơn chưa chuyển sang trạng thái <strong>Đang giao</strong>, <strong>Giao thành công</strong> hoặc <strong>Hoàn thành</strong>.</li>
            <li>Khách hàng cung cấp lý do hủy rõ ràng để shop kiểm tra và phản hồi nhanh hơn.</li>
          </ul>
          <div class="policy-note info">Mẹo: Nếu bạn đổi ý ngay sau khi đặt hàng, hãy gửi yêu cầu hủy càng sớm càng tốt để được hỗ trợ nhanh hơn.</div>
        </section>

        <section class="policy-section">
          <div class="policy-section-top">
            <span class="policy-number">2</span>
            <div>
              <h2>Khi nào không còn thể hủy?</h2>
              <p class="policy-subtext">Một số đơn đã đi xa trong quy trình nên sẽ không thể xử lý như yêu cầu hủy thông thường.</p>
            </div>
          </div>
          <ul class="policy-list">
            <li>Đơn đã bàn giao cho đơn vị vận chuyển và đang trên đường giao.</li>
            <li>Đơn đã giao thành công hoặc khách đã xác nhận nhận hàng.</li>
            <li>Đơn đã hoàn tất xử lý, hoàn tiền hoặc đã có kết quả cuối cùng từ phía shop.</li>
          </ul>
          <div class="policy-note warn">Nếu đơn không còn đủ điều kiện hủy, bạn có thể chuyển sang chính sách hoàn hàng để được hỗ trợ đúng quy trình.</div>
        </section>

        <section class="policy-section">
          <div class="policy-section-top">
            <span class="policy-number">3</span>
            <div>
              <h2>Quy trình hủy đơn</h2>
              <p class="policy-subtext">MyCosmetic giữ quy trình ngắn gọn, minh bạch để bạn dễ theo dõi từng bước.</p>
            </div>
          </div>
          <div class="policy-steps">
            <div class="policy-step">
              <span class="policy-step-no">1</span>
              <div>
                <h4>Tạo yêu cầu hủy</h4>
                <p>Vào chi tiết đơn hàng, chọn mục <strong>Hủy đơn hàng</strong> và nhập lý do mong muốn hủy.</p>
              </div>
            </div>
            <div class="policy-step">
              <span class="policy-step-no">2</span>
              <div>
                <h4>Shop tiếp nhận</h4>
                <p>Shop đối chiếu trạng thái đơn, tình trạng thanh toán và xác nhận khả năng hỗ trợ hủy.</p>
              </div>
            </div>
            <div class="policy-step">
              <span class="policy-step-no">3</span>
              <div>
                <h4>Thông báo kết quả</h4>
                <p>Khách nhận kết quả chấp nhận / từ chối và thông tin hoàn tiền nếu có.</p>
              </div>
            </div>
          </div>
        </section>
      </div>

      <aside class="policy-side">
        <section class="policy-side-card">
          <h3>Tóm tắt nhanh</h3>
          <ul>
            <li><strong>Hủy dễ nhất khi</strong> Đơn còn ở trạng thái mới tạo hoặc chờ xác nhận.</li>
            <li><strong>Cần lưu ý</strong> Đơn đã thanh toán có thể cần thêm thời gian kiểm tra hoàn tiền.</li>
            <li><strong>Nếu đơn đang giao</strong> Bạn nên theo chính sách hoàn hàng thay vì yêu cầu hủy đơn.</li>
          </ul>
        </section>

        <section class="policy-side-card">
          <h3>Hoàn tiền khi hủy đơn</h3>
          <ul>
            <li><strong>Đơn COD</strong> Nếu chưa thanh toán, hủy đơn thường không phát sinh hoàn tiền.</li>
            <li><strong>Đơn thanh toán online</strong> Shop xử lý theo trạng thái giao dịch và thông báo phương thức hoàn phù hợp.</li>
          </ul>
        </section>
      </aside>
    </div>

    <div class="policy-cta">
      <div>
        <h3>Cần thao tác ngay?</h3>
        <p>Vào chi tiết đơn hàng để kiểm tra trạng thái hiện tại và gửi yêu cầu hủy nếu đơn còn đủ điều kiện.</p>
      </div>
      <a class="policy-btn policy-btn-primary" href="${pageContext.request.contextPath}/orders">Đi tới đơn hàng</a>
    </div>
  </div>
</section>

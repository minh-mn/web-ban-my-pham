<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Chính sách hoàn hàng | MyCosmetic</title>
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
      <span class="current">Chính sách hoàn hàng</span>
    </nav>

    <section class="policy-hero-simple">
      <span class="eyebrow">MYCOSMETIC POLICY</span>
      <h1>Chính sách hoàn hàng</h1>
      <p>MyCosmetic hỗ trợ hoàn hàng minh bạch cho các đơn đã giao thành công hoặc hoàn thành trong thời hạn cho phép. Khách hàng nên chuẩn bị mô tả tình trạng sản phẩm và bằng chứng để shop kiểm tra nhanh hơn.</p>
      <span class="policy-tag">Áp dụng sau khi giao thành công</span>
      <span class="policy-tag">Gửi yêu cầu trong 7 ngày</span>
      <span class="policy-tag">Shop kiểm tra trước khi hoàn tiền</span>
    </section>

    <section class="policy-layout-clear">
      <article class="policy-content-card">
        <div class="policy-content-intro">
          <h2>Điều kiện hỗ trợ hoàn hàng</h2>
          <p>MyCosmetic hỗ trợ hoàn hàng minh bạch cho các đơn đã giao thành công hoặc hoàn thành trong thời hạn cho phép. Khách hàng nên chuẩn bị mô tả tình trạng sản phẩm và bằng chứng để shop kiểm tra nhanh hơn.</p>
        </div>

        <section class="policy-content-section">
          <h3 class="policy-section-title">
            <span class="num">01</span>
            <span>Khi nào được hoàn hàng?</span>
          </h3>
          <p>Yêu cầu hoàn hàng được tiếp nhận khi đáp ứng đủ các điều kiện dưới đây.</p>
          <ul>
            <li>Đơn hàng ở trạng thái Giao thành công hoặc Hoàn thành.</li>
            <li>Yêu cầu được gửi trong vòng 7 ngày kể từ thời điểm nhận hàng thành công.</li>
            <li>Sản phẩm còn đủ căn cứ kiểm tra như ảnh, video, bao bì, tem niêm phong hoặc tình trạng thực tế.</li>
          </ul>
          <div class="policy-highlight-note green">Bạn nên chụp hình hoặc quay video ngay khi mở hàng để việc đối chiếu diễn ra nhanh và thuận tiện hơn.</div>
        </section>
        <section class="policy-content-section">
          <h3 class="policy-section-title">
            <span class="num">02</span>
            <span>Các trường hợp được hỗ trợ</span>
          </h3>
          <p>Shop ưu tiên xử lý các trường hợp ảnh hưởng trực tiếp đến trải nghiệm sử dụng.</p>
          <ul>
            <li>Sản phẩm bị lỗi, rò rỉ, vỡ hoặc hư hỏng trong quá trình vận chuyển.</li>
            <li>Shop giao sai sản phẩm, sai phân loại, sai màu hoặc thiếu số lượng so với đơn đặt.</li>
            <li>Sản phẩm có dấu hiệu cận hạn, hết hạn hoặc khác mô tả đã công bố trên website.</li>
          </ul>
        </section>
        <section class="policy-content-section">
          <h3 class="policy-section-title">
            <span class="num">03</span>
            <span>Các trường hợp có thể bị từ chối</span>
          </h3>
          <p>Để đảm bảo công bằng, một số yêu cầu có thể không được chấp nhận.</p>
          <ul>
            <li>Sản phẩm đã qua sử dụng nhiều, không còn tem nhãn hoặc không còn đủ điều kiện kiểm tra.</li>
            <li>Yêu cầu gửi sau thời hạn hỗ trợ hoàn hàng.</li>
            <li>Lỗi phát sinh do bảo quản sai hướng dẫn hoặc sử dụng sai cách.</li>
          </ul>
          <div class="policy-highlight-note yellow">Nếu chưa chắc sản phẩm có đủ điều kiện hay không, hãy liên hệ shop trước khi gửi yêu cầu.</div>
        </section>
        <section class="policy-content-section">
          <h3 class="policy-section-title">
            <span class="num">04</span>
            <span>Quy trình hoàn hàng</span>
          </h3>
          <p>Khách hàng thực hiện hoàn hàng theo các bước sau.</p>
          <div class="policy-step-list">
            <div class="policy-step-item">
              <div class="policy-step-index">1</div>
              <div>
                <strong>Gửi yêu cầu</strong>
                <span>Vào chi tiết đơn hàng, chọn Yêu cầu hoàn hàng và mô tả tình trạng sản phẩm.</span>
              </div>
            </div>
            <div class="policy-step-item">
              <div class="policy-step-index">2</div>
              <div>
                <strong>Shop kiểm tra</strong>
                <span>Shop đối chiếu thông tin, trạng thái đơn hàng và bằng chứng khách cung cấp.</span>
              </div>
            </div>
            <div class="policy-step-item">
              <div class="policy-step-index">3</div>
              <div>
                <strong>Xử lý kết quả</strong>
                <span>Nếu được duyệt, shop cập nhật trạng thái và hướng dẫn hoàn tiền hoặc phương án xử lý phù hợp.</span>
              </div>
            </div>
          </div>
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
            <a class="active" href="${ctx}/policy/return">
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
                <strong>Thời hạn</strong>
                <span>Trong 7 ngày kể từ lúc nhận hàng.</span>
              </div>
              <div>
                <strong>Cần chuẩn bị</strong>
                <span>Lý do rõ ràng, ảnh/video nếu có.</span>
              </div>
              <div>
                <strong>Kết quả</strong>
                <span>Shop kiểm tra trước khi hoàn tiền.</span>
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

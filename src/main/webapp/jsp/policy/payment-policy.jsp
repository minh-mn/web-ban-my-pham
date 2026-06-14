<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Chính sách thanh toán | MyCosmetic</title>
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
      <span class="current">Chính sách thanh toán</span>
    </nav>

    <section class="policy-hero-simple">
      <span class="eyebrow">MYCOSMETIC POLICY</span>
      <h1>Chính sách thanh toán</h1>
      <p>Chính sách thanh toán trình bày rõ các phương thức đang hỗ trợ, thời điểm xác nhận thanh toán và những lưu ý giúp khách hàng tránh sai sót khi giao dịch.</p>
      <span class="policy-tag">COD / Chuyển khoản / VNPay</span>
      <span class="policy-tag">Xác minh minh bạch</span>
      <span class="policy-tag">Lưu ý trước khi giao dịch</span>
    </section>

    <section class="policy-layout-clear">
      <article class="policy-content-card">
        <div class="policy-content-intro">
          <h2>Phương thức và nguyên tắc thanh toán</h2>
          <p>Chính sách thanh toán trình bày rõ các phương thức đang hỗ trợ, thời điểm xác nhận thanh toán và những lưu ý giúp khách hàng tránh sai sót khi giao dịch.</p>
        </div>

        <section class="policy-content-section">
          <h3 class="policy-section-title">
            <span class="num">01</span>
            <span>Phương thức thanh toán</span>
          </h3>
          <p>MyCosmetic hỗ trợ nhiều phương thức để khách hàng lựa chọn.</p>
          <ul>
            <li>COD: thanh toán khi nhận hàng.</li>
            <li>Chuyển khoản ngân hàng theo thông tin shop cung cấp.</li>
            <li>Thanh toán online qua VNPay nếu đơn hàng hỗ trợ phương thức này.</li>
          </ul>
        </section>
        <section class="policy-content-section">
          <h3 class="policy-section-title">
            <span class="num">02</span>
            <span>Xác nhận thanh toán</span>
          </h3>
          <p>Mỗi phương thức sẽ có cách xác nhận khác nhau để đảm bảo an toàn.</p>
          <ul>
            <li>Đơn COD được ghi nhận đã thanh toán khi khách thanh toán thành công lúc nhận hàng.</li>
            <li>Đơn chuyển khoản được xác nhận sau khi shop đối chiếu đúng số tiền và mã đơn.</li>
            <li>Đơn VNPay phụ thuộc kết quả giao dịch trả về từ cổng thanh toán.</li>
          </ul>
        </section>
        <section class="policy-content-section">
          <h3 class="policy-section-title">
            <span class="num">03</span>
            <span>Lưu ý khi thanh toán</span>
          </h3>
          <p>Khách hàng nên kiểm tra kỹ thông tin trước khi hoàn tất giao dịch.</p>
          <ul>
            <li>Không thanh toán ngoài các kênh shop công bố chính thức.</li>
            <li>Kiểm tra tổng tiền, phí vận chuyển, mã giảm giá và thông tin nhận hàng trước khi đặt đơn.</li>
            <li>Lưu lại biên lai hoặc mã giao dịch nếu cần đối chiếu.</li>
          </ul>
          <div class="policy-highlight-note yellow">Nếu đã thanh toán nhưng đơn chưa cập nhật, hãy gửi mã đơn và ảnh giao dịch cho shop để được kiểm tra nhanh.</div>
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
            <a class="active" href="${ctx}/policy/payment">
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
                <strong>Hỗ trợ</strong>
                <span>COD, chuyển khoản, VNPay.</span>
              </div>
              <div>
                <strong>Xác nhận</strong>
                <span>Theo từng phương thức thanh toán.</span>
              </div>
              <div>
                <strong>Nên lưu</strong>
                <span>Biên lai hoặc mã giao dịch.</span>
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

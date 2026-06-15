<%--
  Contact page - MyCosmetic
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/contact.css">

<c:set var="companyName" value="${settings.company_name}" />
<c:if test="${empty companyName}">
  <c:set var="companyName" value="MyCosmeticShop" />
</c:if>

<c:set var="storeAddress" value="${settings.address}" />
<c:if test="${empty storeAddress}">
  <c:set var="storeAddress" value="TP. Hồ Chí Minh, Việt Nam" />
</c:if>

<c:set var="storeHotline" value="${settings.hotline}" />
<c:if test="${empty storeHotline}">
  <c:set var="storeHotline" value="1900 636 510" />
</c:if>

<c:set var="salesEmail" value="${settings.sales_email}" />
<c:if test="${empty salesEmail}">
  <c:set var="salesEmail" value="sales@mycosmetic.vn" />
</c:if>

<c:set var="hrEmail" value="${settings.hr_email}" />
<c:if test="${empty hrEmail}">
  <c:set var="hrEmail" value="hr@mycosmetic.vn" />
</c:if>

<c:set var="facebookUrl" value="${settings.facebook}" />
<c:if test="${empty facebookUrl}">
  <c:set var="facebookUrl" value="#" />
</c:if>

<c:set var="instagramUrl" value="${settings.instagram}" />
<c:if test="${empty instagramUrl}">
  <c:set var="instagramUrl" value="#" />
</c:if>

<main class="contact-page">

  <section class="contact-hero">
    <div class="contact-hero__glow contact-hero__glow--left"></div>
    <div class="contact-hero__glow contact-hero__glow--right"></div>

    <div class="contact-container contact-hero__inner">
      <div class="contact-kicker">
        <span></span>
        MyCosmetic Contact
      </div>

      <h1>Liên hệ với MyCosmetic</h1>
      <p>
        MyCosmetic luôn sẵn sàng hỗ trợ đơn hàng, tư vấn mỹ phẩm và tiếp nhận phản hồi của bạn.
        Hãy để lại thông tin, chúng tôi sẽ phản hồi trong thời gian sớm nhất.
      </p>

      <div class="contact-hero__stats">
        <div>
          <strong>08:00 - 21:00</strong>
          <span>Thời gian hỗ trợ</span>
        </div>
        <div>
          <strong>24h</strong>
          <span>Tiếp nhận tin nhắn</span>
        </div>
        <div>
          <strong>2 - 4h</strong>
          <span>Phản hồi trong ngày</span>
        </div>
      </div>
    </div>
  </section>

  <section class="contact-container contact-main">

    <aside class="contact-info-card">
      <div class="contact-card-head">
        <span class="contact-card-icon">
          <i class="fa-solid fa-store"></i>
        </span>
        <div>
          <span class="contact-card-label">Thông tin cửa hàng</span>
          <h2><c:out value="${companyName}" /></h2>
        </div>
      </div>

      <div class="contact-info-list">
        <div class="contact-info-item">
          <span><i class="fa-solid fa-building"></i></span>
          <div>
            <strong>Công ty</strong>
            <p><c:out value="${companyName}" /></p>
          </div>
        </div>

        <div class="contact-info-item">
          <span><i class="fa-solid fa-location-dot"></i></span>
          <div>
            <strong>Địa chỉ</strong>
            <p><c:out value="${storeAddress}" /></p>
          </div>
        </div>

        <div class="contact-info-item">
          <span><i class="fa-solid fa-phone-volume"></i></span>
          <div>
            <strong>Hotline</strong>
            <p><a href="tel:${storeHotline}"><c:out value="${storeHotline}" /></a></p>
          </div>
        </div>

        <div class="contact-info-item">
          <span><i class="fa-solid fa-envelope-open-text"></i></span>
          <div>
            <strong>Email CSKH</strong>
            <p><a href="mailto:${salesEmail}"><c:out value="${salesEmail}" /></a></p>
          </div>
        </div>

        <div class="contact-info-item">
          <span><i class="fa-solid fa-user-tie"></i></span>
          <div>
            <strong>Email nhân sự</strong>
            <p><a href="mailto:${hrEmail}"><c:out value="${hrEmail}" /></a></p>
          </div>
        </div>
      </div>

      <div class="contact-social-row">
        <a href="${facebookUrl}" aria-label="Facebook">
          <i class="fa-brands fa-facebook-f"></i>
        </a>
        <a href="${instagramUrl}" aria-label="Instagram">
          <i class="fa-brands fa-instagram"></i>
        </a>
        <a href="mailto:${salesEmail}" aria-label="Email">
          <i class="fa-solid fa-envelope"></i>
        </a>
        <a href="tel:${storeHotline}" aria-label="Hotline">
          <i class="fa-solid fa-phone"></i>
        </a>
      </div>
    </aside>

    <section class="contact-form-card">
      <div class="contact-form-head">
        <span class="contact-form-tag">Gửi phản hồi</span>
        <h2>Gửi lời nhắn cho MyCosmetic</h2>
        <p>
          Điền thông tin bên dưới để đội ngũ MyCosmetic hỗ trợ bạn về sản phẩm,
          đơn hàng, đổi trả hoặc các vấn đề cần tư vấn.
        </p>
      </div>

      <c:if test="${not empty messageSuccess}">
        <div class="contact-success">
          <i class="fa-solid fa-circle-check"></i>
          <span>${messageSuccess}</span>
        </div>
      </c:if>

      <form action="${pageContext.request.contextPath}/lien-he"
            method="POST"
            class="contact-form">

        <div class="contact-field-grid">
          <div class="contact-field">
            <label for="fullName">Họ và tên</label>
            <div class="contact-input-wrap">
              <i class="fa-solid fa-user"></i>
              <input id="fullName"
                     type="text"
                     name="fullName"
                     placeholder="Nhập họ và tên"
                     required>
            </div>
          </div>

          <div class="contact-field">
            <label for="phone">Số điện thoại</label>
            <div class="contact-input-wrap">
              <i class="fa-solid fa-phone"></i>
              <input id="phone"
                     type="text"
                     name="phone"
                     placeholder="Nhập số điện thoại"
                     required>
            </div>
          </div>
        </div>

        <div class="contact-field">
          <label for="email">Email</label>
          <div class="contact-input-wrap">
            <i class="fa-solid fa-envelope"></i>
            <input id="email"
                   type="email"
                   name="email"
                   placeholder="Nhập địa chỉ email"
                   required>
          </div>
        </div>

        <div class="contact-field">
          <label for="message">Nội dung lời nhắn</label>
          <div class="contact-input-wrap contact-input-wrap--textarea">
            <i class="fa-solid fa-message"></i>
            <textarea id="message"
                      name="message"
                      placeholder="Bạn cần MyCosmetic hỗ trợ vấn đề gì?"
                      required></textarea>
          </div>
        </div>

        <div class="contact-form-actions">
          <button type="submit" class="contact-submit">
            <span>Gửi lời nhắn</span>
            <i class="fa-solid fa-arrow-right"></i>
          </button>
        </div>

      </form>
    </section>

  </section>

  <section class="contact-container contact-support">
    <article>
      <i class="fa-solid fa-box-open"></i>
      <h3>Hỗ trợ đơn hàng</h3>
      <p>Tra cứu, xác nhận, đổi trả và cập nhật trạng thái đơn hàng.</p>
    </article>

    <article>
      <i class="fa-solid fa-wand-magic-sparkles"></i>
      <h3>Tư vấn sản phẩm</h3>
      <p>Gợi ý mỹ phẩm phù hợp theo nhu cầu chăm sóc da và trang điểm.</p>
    </article>

    <article>
      <i class="fa-solid fa-shield-heart"></i>
      <h3>Chính sách & bảo mật</h3>
      <p>Tiếp nhận phản hồi về chính sách thanh toán, bảo mật và dịch vụ.</p>
    </article>
  </section>

</main>

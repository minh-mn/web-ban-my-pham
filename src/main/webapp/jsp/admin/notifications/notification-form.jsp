<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Gửi thông báo hàng loạt" scope="request"/>
<c:set var="activeMenu" value="notifications" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container admin-notification-form-page">

    <jsp:include page="/jsp/admin/layout/topbar.jsp"/>

    <section class="admin-notification-form-hero">
      <div class="admin-notification-form-hero__content">
        <span class="admin-notification-form-eyebrow">BROADCAST</span>

        <h1 class="admin-h1 admin-notification-form-title">
          Tạo chiến dịch thông báo
        </h1>

        <p class="admin-subtext admin-notification-form-subtitle">
          Gửi thông báo hàng loạt tới khách hàng. Hệ thống sẽ tự động đưa thông báo vào trung tâm thông báo của từng tài khoản.
        </p>
      </div>

      <div class="admin-notification-form-hero__actions">
        <a href="${pageContext.request.contextPath}/admin/notifications"
           class="admin-btn">
          Quay lại danh sách
        </a>
      </div>
    </section>

    <c:if test="${not empty admin_notification_error}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${admin_notification_error}" />
      </div>
    </c:if>

    <form id="notificationForm"
          class="admin-notification-form"
          action="${pageContext.request.contextPath}/admin/notifications"
          method="post">

      <%@ include file="/jsp/common/csrf.jspf" %>
      <input type="hidden" name="action" value="sendBulk">

      <div class="admin-notification-form-layout">

        <div class="admin-notification-form-main">
          <div class="admin-card">
            <div class="admin-card__body">

              <div class="admin-form-section">
                <h2 class="admin-form-section__title">Nội dung thông báo</h2>

                <div class="admin-field">
                  <label class="admin-label" for="notificationTitle">
                    Tiêu đề thông báo <span class="admin-required">*</span>
                  </label>
                  <input id="notificationTitle"
                         class="admin-input"
                         name="title"
                         maxlength="180"
                         required
                         placeholder="VD: Siêu sale cuối tuần - Giảm giá toàn bộ cửa hàng!">
                  <span class="admin-help">
                    Tiêu đề nên ngắn gọn, dễ hiểu và thể hiện rõ lợi ích cho khách hàng.
                  </span>
                </div>

                <div class="admin-field">
                  <label class="admin-label" for="notificationMessage">
                    Nội dung chi tiết <span class="admin-required">*</span>
                  </label>
                  <textarea id="notificationMessage"
                            class="admin-textarea admin-notification-message-input"
                            name="message"
                            maxlength="1000"
                            required
                            placeholder="Nhập nội dung ngắn gọn để khách hàng hiểu thông báo này nói về điều gì..."></textarea>
                  <span class="admin-help">
                    Nội dung sẽ hiển thị trong dropdown thông báo và trang danh sách thông báo của khách hàng.
                  </span>
                </div>
              </div>

              <div class="admin-form-section">
                <h2 class="admin-form-section__title">Đường dẫn chuyển hướng</h2>

                <div class="admin-field">
                  <label class="admin-label" for="targetUrlInput">
                    Target URL <span class="admin-required">*</span>
                  </label>
                  <input id="targetUrlInput"
                         class="admin-input"
                         name="targetUrl"
                         required
                         value="/blog"
                         pattern="/.*"
                         placeholder="Ví dụ: /blog hoặc /vouchers">
                  <span class="admin-help">
                    Chỉ nhập đường dẫn bắt đầu bằng dấu <strong>/</strong>, ví dụ <strong>/vouchers</strong>.
                  </span>
                </div>

                <div class="admin-notification-url-shortcuts">
                  <button type="button"
                          class="admin-btn admin-notification-url-btn"
                          data-target-url="/vouchers">
                    🎟️ Trang voucher
                  </button>

                  <button type="button"
                          class="admin-btn admin-notification-url-btn"
                          data-target-url="/blog">
                    📢 Trang sự kiện
                  </button>

                  <button type="button"
                          class="admin-btn admin-notification-url-btn"
                          data-target-url="/">
                    🏠 Trang chủ
                  </button>
                </div>
              </div>

            </div>
          </div>
        </div>

        <aside class="admin-notification-form-side">
          <div class="admin-card">
            <div class="admin-card__body">

              <div class="admin-form-section admin-notification-type-section">
                <h2 class="admin-form-section__title">Cấu hình gửi</h2>

                <div class="admin-field">
                  <label class="admin-label" for="notifTypeSelect">
                    Loại thông báo
                  </label>
                  <select class="admin-select"
                          name="type"
                          id="notifTypeSelect">
                    <option value="EVENT" data-default-url="/blog">📢 Sự kiện</option>
                    <option value="VOUCHER" data-default-url="/vouchers">🎟️ Mã giảm giá</option>
                    <option value="SYSTEM" data-default-url="/">✨ Hệ thống</option>
                  </select>
                </div>

                <div class="admin-info-box">
                  <div class="admin-info-box__title">Phạm vi gửi</div>
                  <div class="admin-info-box__text">
                    Thông báo này sẽ được gửi tới toàn bộ tài khoản khách hàng đang có trong hệ thống.
                  </div>
                </div>
              </div>

              <div class="admin-notification-publish-box">
                <button type="button"
                        class="admin-btn admin-btn--primary admin-notification-publish-btn"
                        id="openConfirmModalBtn">
                  🚀 Phát hành ngay
                </button>

                <p>
                  Hãy kiểm tra kỹ tiêu đề, nội dung và đường dẫn trước khi phát hành.
                </p>
              </div>

            </div>
          </div>
        </aside>

      </div>
    </form>

  </div>
</main>

<div id="customConfirmModal"
     class="admin-notification-modal"
     aria-hidden="true">
  <div class="admin-notification-modal__dialog"
       role="dialog"
       aria-modal="true"
       aria-labelledby="notificationConfirmTitle">
    <div class="admin-notification-modal__icon">🚀</div>

    <h3 id="notificationConfirmTitle">
      Xác nhận phát hành
    </h3>

    <p>
      Bạn có chắc chắn muốn <strong>phát hành hàng loạt</strong> thông báo này tới toàn bộ người dùng trên hệ thống không?
      Hành động này không thể hoàn tác.
    </p>

    <div class="admin-notification-modal__actions">
      <button type="button"
              class="admin-btn"
              id="closeConfirmModalBtn">
        Đóng
      </button>

      <button type="button"
              class="admin-btn admin-btn--primary"
              id="submitNotificationFormBtn">
        Phát hành
      </button>
    </div>
  </div>
</div>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

<script>
  document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('notificationForm');
    const typeSelect = document.getElementById('notifTypeSelect');
    const targetUrlInput = document.getElementById('targetUrlInput');
    const modal = document.getElementById('customConfirmModal');

    const openBtn = document.getElementById('openConfirmModalBtn');
    const closeBtn = document.getElementById('closeConfirmModalBtn');
    const submitBtn = document.getElementById('submitNotificationFormBtn');

    function openModal() {
      if (!form.checkValidity()) {
        form.reportValidity();
        return;
      }

      modal.classList.add('is-open');
      modal.setAttribute('aria-hidden', 'false');
    }

    function closeModal() {
      modal.classList.remove('is-open');
      modal.setAttribute('aria-hidden', 'true');
    }

    typeSelect.addEventListener('change', function () {
      const selected = typeSelect.options[typeSelect.selectedIndex];
      const defaultUrl = selected.getAttribute('data-default-url');

      if (defaultUrl) {
        targetUrlInput.value = defaultUrl;
      }
    });

    document.querySelectorAll('.admin-notification-url-btn').forEach(function (button) {
      button.addEventListener('click', function () {
        targetUrlInput.value = button.getAttribute('data-target-url');
      });
    });

    openBtn.addEventListener('click', openModal);
    closeBtn.addEventListener('click', closeModal);

    submitBtn.addEventListener('click', function () {
      form.submit();
    });

    modal.addEventListener('click', function (event) {
      if (event.target === modal) {
        closeModal();
      }
    });

    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape' && modal.classList.contains('is-open')) {
        closeModal();
      }
    });
  });
</script>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Gửi Thông báo Hàng loạt" scope="request"/>
<c:set var="activeMenu" value="notifications" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Tạo Chiến dịch Thông báo</h1>
        <p class="admin-subtext">Hệ thống sẽ tự động chuyển đổi đường dẫn và gửi tới toàn bộ tài khoản người dùng.</p>
      </div>
      <a href="${pageContext.request.contextPath}/admin/notifications" class="admin-btn">Quay lại</a>
    </div>

    <form id="notificationForm" action="${pageContext.request.contextPath}/admin/notifications" method="post">

      <%@ include file="/jsp/common/csrf.jspf" %>
      <input type="hidden" name="action" value="sendBulk">

      <div class="page-editor-layout">

        <div class="page-editor-main">
          <div class="admin-card">
            <div class="admin-card__body">

              <div class="admin-field">
                <label class="admin-label">Tiêu đề thông báo</label>
                <input class="admin-input" name="title" required
                       placeholder="VD: Siêu sale 11/11 - Giảm giá toàn bộ cửa hàng!">
              </div>

              <div class="admin-field">
                <label class="admin-label">Nội dung chi tiết</label>
                <textarea class="admin-input" name="message" rows="4" required
                          placeholder="Nhập nội dung ngắn gọn để thu hút khách hàng..."></textarea>
              </div>

            </div>
          </div>
        </div>

        <div class="page-editor-sidebar">
          <div class="admin-card">
            <div class="admin-card__body">

              <div class="admin-field">
                <label class="admin-label">Loại thông báo</label>
                <select class="admin-input" name="type" id="notifTypeSelect" onchange="updateDefaultUrl()">
                  <option value="EVENT">📢 Sự kiện (Event)</option>
                  <option value="VOUCHER">🎟️ Mã giảm giá (Voucher)</option>
                  <option value="SYSTEM">✨ Hệ thống (System)</option>
                </select>
              </div>

              <div class="admin-field" style="margin-bottom: 15px;">
                <label class="admin-label" style="display: block; margin-bottom: 5px;">Đường dẫn đích (Target URL)</label>

                <input class="admin-input" name="targetUrl" id="targetUrlInput" required value="/blog"
                       placeholder="Ví dụ: /blog hoặc /vouchers">

                <small style="color: #888; display: block; margin-top: 4px;">
                  * Hệ thống tự động lấy gốc website. Chỉ cần nhập bắt đầu bằng dấu <code>/</code>
                </small>

                <div style="margin-top: 8px; display: flex; gap: 8px; flex-wrap: wrap;">
                  <button type="button" class="admin-btn" style="padding: 4px 10px; font-size: 12px; cursor: pointer;"
                          onclick="document.getElementById('targetUrlInput').value='/vouchers'">
                    🎟️ Trang Voucher
                  </button>
                  <button type="button" class="admin-btn" style="padding: 4px 10px; font-size: 12px; cursor: pointer;"
                          onclick="document.getElementById('targetUrlInput').value='/blog'">
                    📢 Trang Sự Kiện
                  </button>
                  <button type="button" class="admin-btn" style="padding: 4px 10px; font-size: 12px; cursor: pointer;"
                          onclick="document.getElementById('targetUrlInput').value='/'">
                    🏠 Trang Chủ
                  </button>
                </div>
              </div>

            </div>
          </div>

          <div class="admin-card">
            <div class="admin-card__body">
              <button type="button" class="admin-btn admin-btn--primary" style="width: 100%;"
                      onclick="showConfirmPopup()">
                🚀 Phát hành ngay
              </button>
            </div>
          </div>

        </div>

      </div>
    </form>

  </div>
</main>

<div id="customConfirmModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); z-index: 10000; align-items: center; justify-content: center; backdrop-filter: blur(3px);">
  <div style="background: white; padding: 30px; border-radius: 16px; width: 400px; max-width: 90%; box-shadow: 0 10px 30px rgba(0,0,0,0.25); text-align: center; animation: popIn 0.3s ease-out;">
    <div style="font-size: 45px; margin-bottom: 15px;">🚀</div>
    <h3 style="margin: 0 0 12px 0; color: #222; font-size: 20px; font-weight: bold;">Xác nhận phát hành</h3>
    <p style="color: #666; font-size: 14.5px; margin-bottom: 25px; line-height: 1.5;">
      Bạn có chắc chắn muốn <b>phát hành hàng loạt</b> thông báo này tới toàn bộ người dùng trên hệ thống không? Hành động này không thể hoàn tác.
    </p>
    <div style="display: flex; gap: 12px; justify-content: center;">
      <button type="button" onclick="closeConfirmPopup()" style="flex: 1; padding: 12px; border: 1px solid #ddd; background: #f9f9f9; color: #444; border-radius: 8px; cursor: pointer; font-weight: bold; transition: all 0.2s;">
        Đóng
      </button>
      <button type="button" onclick="submitNotificationForm()" style="flex: 1; padding: 12px; border: none; background: #ff5fa2; color: white; border-radius: 8px; cursor: pointer; font-weight: bold; box-shadow: 0 4px 12px rgba(255,95,162,0.3); transition: all 0.2s;">
        Phát hành
      </button>
    </div>
  </div>
</div>

<style>
  @keyframes popIn {
    from { transform: scale(0.9); opacity: 0; }
    to { transform: scale(1); opacity: 1; }
  }
</style>
<jsp:include page="/jsp/admin/layout/footer.jsp"/>

<script>
  // Thay đổi URL tự động khi đổi Loại thông báo
  function updateDefaultUrl() {
    const type = document.getElementById("notifTypeSelect").value;
    const urlInput = document.getElementById("targetUrlInput");
    if (type === "EVENT") {
      urlInput.value = "/blog";
    } else if (type === "VOUCHER") {
      urlInput.value = "/vouchers";
    } else {
      urlInput.value = "/";
    }
  }

  // Hiển thị Popup
  function showConfirmPopup() {
    const form = document.getElementById('notificationForm');
    // Kiểm tra xem các ô nhập liệu đã được điền đủ chưa (validate HTML5)
    if (form.checkValidity()) {
      document.getElementById('customConfirmModal').style.display = 'flex';
    } else {
      // Nếu chưa điền đủ, tự động báo lỗi đỏ ở ô đó
      form.reportValidity();
    }
  }

  // Đóng Popup
  function closeConfirmPopup() {
    document.getElementById('customConfirmModal').style.display = 'none';
  }

  // Gửi Form khi bấm Xác nhận trên Popup
  function submitNotificationForm() {
    document.getElementById('notificationForm').submit();
  }
</script>
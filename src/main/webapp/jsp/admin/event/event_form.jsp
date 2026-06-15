<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="${mode == 'edit' ? 'ADMIN | Sửa sự kiện' : 'ADMIN | Thêm sự kiện'}" scope="request"/>
<c:set var="activeMenu" value="events" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%-- Định dạng dữ liệu ngày tương thích chuẩn thẻ <input type="date"> (yyyy-MM-dd) --%>
<c:set var="eventDateValue" value=""/>
<c:if test="${not empty event && not empty event.eventDate}">
  <fmt:formatDate var="eventDateValue" value="${event.eventDate}" pattern="yyyy-MM-dd"/>
</c:if>

<main class="admin-main">
  <div class="admin-container admin-event-form-page">

    <section class="admin-event-form-hero">
      <div class="admin-event-form-hero__content">
        <span class="admin-event-form-eyebrow">NỘI DUNG &amp; WEBSITE</span>
        <h1 class="admin-event-form-title">
          <c:choose>
            <c:when test="${mode == 'edit'}">Sửa sự kiện</c:when>
            <c:otherwise>Thêm sự kiện</c:otherwise>
          </c:choose>
        </h1>
        <p class="admin-event-form-subtitle">
          Nhập thông tin sự kiện, workshop hoặc tin tức hiển thị trên trang chủ.
          Ảnh đại diện nên rõ nét, đúng tỉ lệ và phù hợp với tone màu của website.
        </p>
      </div>

      <div class="admin-event-form-hero__actions">
        <a class="admin-btn" href="${ctx}/admin/events">
          ← Quay lại danh sách
        </a>
      </div>
    </section>

    <c:if test="${not empty success}">
      <div class="admin-alert admin-alert--success">
        <c:out value="${success}" />
      </div>
    </c:if>

    <form method="post"
          action="${ctx}/admin/events"
          enctype="multipart/form-data"
          class="admin-form admin-event-form">

      <%@ include file="/jsp/common/csrf.jspf" %>

      <%-- Xác định chế độ Thêm (create) hoặc Sửa (update) --%>
      <input type="hidden" name="action" value="${mode == 'edit' ? 'update' : 'create'}"/>

      <c:if test="${mode == 'edit'}">
        <input type="hidden" name="id" value="${event.id}"/>
      </c:if>

      <div class="admin-event-form-layout">

        <section class="admin-card admin-event-form-card">
          <div class="admin-card__body">
            <div class="admin-event-form-section-head">
              <div>
                <h2 class="admin-event-form-section-title">Thông tin sự kiện</h2>
                <p class="admin-event-form-section-desc">
                  Cập nhật tiêu đề, nhãn dán, ngày diễn ra và mô tả ngắn của sự kiện.
                </p>
              </div>

              <c:choose>
                <c:when test="${mode == 'edit'}">
                  <span class="admin-chip admin-chip--brand">Đang chỉnh sửa</span>
                </c:when>
                <c:otherwise>
                  <span class="admin-chip admin-chip--success">Sự kiện mới</span>
                </c:otherwise>
              </c:choose>
            </div>

            <div class="admin-event-form-grid">
              <div class="admin-field admin-event-form-field--full">
                <label class="admin-label" for="eventTitle">
                  Tiêu đề sự kiện <span class="admin-required">*</span>
                </label>
                <input class="admin-input"
                       id="eventTitle"
                       type="text"
                       name="title"
                       value="${not empty event ? fn:escapeXml(event.title) : ''}"
                       maxlength="255"
                       autocomplete="off"
                       placeholder="VD: Workshop tự làm son môi handmade"
                       required />
                <div class="admin-help">
                  Tiêu đề nên ngắn gọn, rõ nội dung và thu hút người dùng.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="eventTag">
                  Nhãn dán <span class="admin-required">*</span>
                </label>
                <input class="admin-input"
                       id="eventTag"
                       type="text"
                       name="tag"
                       value="${not empty event ? fn:escapeXml(event.tag) : ''}"
                       maxlength="50"
                       placeholder="VD: Workshop, Khai trương, Tin tức"
                       required />
                <div class="admin-help">
                  Nhãn dán dùng để phân loại sự kiện trên giao diện.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="eventDate">
                  Ngày diễn ra <span class="admin-required">*</span>
                </label>
                <input class="admin-input"
                       id="eventDate"
                       type="date"
                       name="eventDate"
                       value="${eventDateValue}"
                       required />
                <div class="admin-help">
                  Hệ thống sẽ dùng ngày này để hiển thị lịch sự kiện.
                </div>
              </div>

              <div class="admin-field admin-event-form-field--full">
                <label class="admin-label" for="eventImageFile">
                  Hình ảnh đại diện <span class="admin-required">*</span>
                </label>
                <input class="admin-input"
                       id="eventImageFile"
                       type="file"
                       name="imageFile"
                       accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp"
                  ${mode == 'edit' ? '' : 'required'} />
                <div class="admin-help">
                  Hỗ trợ JPG, JPEG, PNG, WEBP. Khi sửa sự kiện, nếu không chọn ảnh mới thì hệ thống giữ ảnh cũ.
                </div>
              </div>

              <div class="admin-field admin-event-form-field--full">
                <label class="admin-label" for="eventSummary">Mô tả ngắn sự kiện</label>
                <textarea class="admin-textarea"
                          id="eventSummary"
                          name="summary"
                          maxlength="500"
                          placeholder="Nhập nội dung tóm tắt hiển thị ở trang chủ..."><c:out value="${event.summary}"/></textarea>
                <div class="admin-help">
                  Tóm tắt ngắn gọn nội dung sự kiện, tối đa 500 ký tự.
                </div>
              </div>
            </div>

            <div class="admin-event-form-note">
              <span class="admin-event-form-note__icon">💡</span>
              <div>
                <strong>Gợi ý hiển thị</strong>
                <span>Nên dùng ảnh ngang, rõ chủ đề sự kiện và tránh chữ quá nhỏ để khi hiển thị ở trang chủ không bị rối.</span>
              </div>
            </div>
          </div>
        </section>

        <aside class="admin-card admin-event-preview-card">
          <div class="admin-card__body">
            <div class="admin-event-preview-head">
              <div>
                <h2 class="admin-event-form-section-title">Xem trước ảnh</h2>
                <p class="admin-event-form-section-desc">
                  Kiểm tra ảnh hiện tại hoặc ảnh mới vừa chọn trước khi lưu.
                </p>
              </div>
            </div>

            <div class="admin-event-preview-box">
              <c:choose>
                <c:when test="${mode == 'edit' && not empty event.imageUrl}">
                  <img id="eventPreviewImage"
                       class="admin-event-preview-img"
                       src="${ctx}${event.imageUrl}"
                       alt="Event Preview" />

                  <div id="eventPreviewEmpty" class="admin-event-preview-empty is-hidden">
                    Chưa có ảnh xem trước.
                  </div>

                  <div class="admin-help admin-break">
                    Đường dẫn hiện tại:
                    <strong><c:out value="${event.imageUrl}" /></strong>
                  </div>
                </c:when>

                <c:otherwise>
                  <img id="eventPreviewImage"
                       class="admin-event-preview-img is-hidden"
                       src=""
                       alt="Event preview" />

                  <div id="eventPreviewEmpty" class="admin-event-preview-empty">
                    <span>📅</span>
                    <strong>Chưa có ảnh xem trước</strong>
                    <small>Vui lòng chọn ảnh đại diện để kiểm tra trước khi lưu.</small>
                  </div>
                </c:otherwise>
              </c:choose>
            </div>
          </div>
        </aside>

      </div>

      <div class="admin-event-form-actions">
        <a class="admin-btn" href="${ctx}/admin/events">
          Hủy
        </a>

        <button class="admin-btn admin-btn--primary" type="submit">
          Lưu sự kiện
        </button>
      </div>
    </form>

  </div>
</main>

<div id="errorPopup" class="custom-popup-overlay">
  <div class="custom-popup-box">
    <div class="custom-popup-icon danger-mode">✕</div>
    <div class="custom-popup-title">Thông báo hệ thống</div>
    <div class="custom-popup-msg" id="errorPopupMsg">
      <c:out value="${error}"/>
    </div>
    <button type="button" class="custom-popup-btn custom-popup-btn--confirm danger-brand" onclick="closeErrorPopup()">Đóng</button>
  </div>
</div>

<script>
  document.addEventListener("DOMContentLoaded", function() {
    const fileInput = document.getElementById("eventImageFile");
    const previewImage = document.getElementById("eventPreviewImage");
    const previewEmpty = document.getElementById("eventPreviewEmpty");

    if (fileInput && previewImage) {
      fileInput.addEventListener("change", function () {
        const file = this.files && this.files[0];

        if (!file) {
          return;
        }

        const allowedTypes = [
          "image/jpeg",
          "image/png",
          "image/webp"
        ];

        if (allowedTypes.indexOf(file.type) === -1) {
          alert("File không hợp lệ. Vui lòng chọn ảnh JPG, PNG hoặc WEBP.");
          this.value = "";
          return;
        }

        previewImage.src = URL.createObjectURL(file);
        previewImage.classList.remove("is-hidden");

        if (previewEmpty) {
          previewEmpty.classList.add("is-hidden");
        }
      });
    }

    var hasError = "${not empty error}";

    if (hasError === "true") {
      document.getElementById("errorPopup").classList.add("show");
    }
  });

  function closeErrorPopup() {
    document.getElementById("errorPopup").classList.remove("show");
  }
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 28/05/2026
  Time: 7:09 CH
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Form Sự kiện" scope="request"/>
<c:set var="activeMenu" value="events" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<style>
  @import url('https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@300;400;600;700;800;900&display=swap');

  /* Áp dụng phông chữ thống nhất cho toàn bộ khu vực quản trị và popup */
  .admin-main,
  .custom-popup-overlay {
    font-family: 'Be Vietnam Pro', sans-serif;
  }

  /* Lớp phủ mờ toàn màn hình của Popup */
  .custom-popup-overlay {
    position: fixed;
    top: 0; left: 0; width: 100%; height: 100%;
    background: rgba(17, 24, 39, 0.6);
    backdrop-filter: blur(4px);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
    opacity: 0; pointer-events: none;
    transition: all 0.3s ease;
  }
  .custom-popup-overlay.show {
    opacity: 1; pointer-events: auto;
  }

  /* Hộp nội dung cấu trúc Popup */
  .custom-popup-box {
    background: #ffffff;
    padding: 30px;
    border-radius: 12px;
    width: 90%;
    max-width: 420px;
    text-align: center;
    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
    transform: scale(0.8);
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  .custom-popup-overlay.show .custom-popup-box {
    transform: scale(1);
  }

  /* Biểu tượng Icon lỗi (Dấu X) */
  .custom-popup-icon {
    width: 60px; height: 60px;
    background: #fee2e2; color: #ef4444;
    font-size: 26px; font-weight: bold;
    display: flex; align-items: center; justify-content: center;
    margin: 0 auto 15px; border-radius: 50%;
  }

  /* Tiêu đề thông báo */
  .custom-popup-title {
    font-size: 18px; color: #111827;
    margin-bottom: 10px; font-weight: 700;
  }

  /* Chi tiết văn bản lỗi */
  .custom-popup-msg {
    font-size: 14px; color: #4b5563;
    line-height: 1.6; margin-bottom: 25px;
  }

  /* Nút tương tác đóng popup */
  .custom-popup-btn {
    background: #ef4444; color: white;
    border: none; padding: 12px 24px;
    font-size: 14px; font-weight: 600;
    border-radius: 6px; cursor: pointer;
    transition: background 0.2s;
    width: 100%;
  }
  .custom-popup-btn:hover {
    background: #dc2626;
  }
</style>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">
          <c:choose>
            <c:when test="${mode == 'edit'}">Sửa sự kiện</c:when>
            <c:otherwise>Thêm sự kiện</c:otherwise>
          </c:choose>
        </h1>
        <p class="admin-subtext">
          Nhập thông tin sự kiện và tải lên hình ảnh trực tiếp từ máy tính của bạn.
        </p>
      </div>
      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/events">Quay lại</a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <%-- Định dạng dữ liệu ngày tương thích chuẩn thẻ <input type="date"> (yyyy-MM-dd) --%>
        <c:set var="eventDateValue" value=""/>
        <c:if test="${not empty event && not empty event.eventDate}">
          <fmt:formatDate var="eventDateValue" value="${event.eventDate}" pattern="yyyy-MM-dd"/>
        </c:if>

        <%-- Biểu mẫu gửi thông tin --%>
        <form method="post"
              action="${pageContext.request.contextPath}/admin/events"
              enctype="multipart/form-data"
              class="admin-form">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <%-- Xác định chế độ Thêm (create) hoặc Sửa (update) --%>
          <input type="hidden" name="action" value="${mode == 'edit' ? 'update' : 'create'}"/>

          <c:if test="${mode == 'edit'}">
            <input type="hidden" name="id" value="${event.id}"/>
          </c:if>

          <div class="admin-grid-2">

            <%-- Trường dữ liệu: Tiêu đề sự kiện --%>
            <div class="admin-field admin-field--full">
              <div class="admin-label">Tiêu đề sự kiện *</div>
              <input class="admin-input"
                     type="text"
                     name="title"
                     value="${not empty event ? fn:escapeXml(event.title) : ''}"
                     maxlength="255"
                     autocomplete="off"
                     required />
              <div class="admin-help">Tiêu đề ngắn gọn, thu hút. Ví dụ: Workshop tự làm son môi handmade</div>
            </div>

            <%-- Trường dữ liệu: Nhãn dán --%>
            <div class="admin-field">
              <div class="admin-label">Nhãn dán (Tag) *</div>
              <input class="admin-input"
                     type="text"
                     name="tag"
                     value="${not empty event ? fn:escapeXml(event.tag) : ''}"
                     maxlength="50"
                     placeholder="VD: Workshop, Khai trương, Tin tức"
                     required />
              <div class="admin-help">Phân loại sự kiện hiển thị trên nhãn góc bài viết.</div>
            </div>

            <%-- Trường dữ liệu: Ngày diễn ra --%>
            <div class="admin-field">
              <div class="admin-label">Ngày diễn ra sự kiện *</div>
              <input class="admin-input"
                     type="date"
                     name="eventDate"
                     value="${eventDateValue}"
                     required />
              <div class="admin-help">Hệ thống sẽ tự bóc tách ngày và tháng để hiển thị theo UI trang chủ.</div>
            </div>

            <%-- Trường tải file: Hình ảnh đại diện --%>
            <div class="admin-field admin-field--full">
              <div class="admin-label">Hình ảnh đại diện *</div>

              <%-- Xem trước ảnh cũ nếu đang trong chế độ chỉnh sửa bài viết --%>
              <c:if test="${mode == 'edit' && not empty event.imageUrl}">
                <div style="margin-bottom: 12px;">
                  <p style="font-size: 12px; color: #6b7280; margin-bottom: 6px;">Ảnh hiện tại đang sử dụng:</p>
                  <img src="${pageContext.request.contextPath}${event.imageUrl}"
                       alt="Event Preview"
                       style="max-width: 200px; max-height: 120px; object-fit: cover; border-radius: 6px; border: 1px solid #e5e7eb;"/>
                </div>
              </c:if>

              <input class="admin-input"
                     type="file"
                     name="imageFile"
                     accept="image/*"
              ${mode == 'edit' ? '' : 'required'} />
              <div class="admin-help">Hỗ trợ định dạng: .jpg, .jpeg, .png. Chọn file trực tiếp từ máy tính của bạn.</div>
            </div>

            <%-- Trường dữ liệu: Mô tả tóm tắt --%>
            <div class="admin-field admin-field--full">
              <div class="admin-label">Mô tả ngắn sự kiện</div>
              <textarea class="admin-textarea"
                        name="summary"
                        maxlength="500"
                        placeholder="Nhập nội dung tóm tắt hiển thị ở trang chủ..."><c:out value="${event.summary}"/></textarea>
              <div class="admin-help">Tóm tắt ngắn gọn nội dung sự kiện (Tối đa 500 ký tự).</div>
            </div>

          </div>

          <hr class="admin-divider"/>

          <%-- Khu vực các nút bấm xác nhận hành động --%>
          <div class="admin-actions">
            <button class="admin-btn admin-btn--primary" type="submit">Lưu sự kiện</button>
            <a class="admin-btn" href="${pageContext.request.contextPath}/admin/events">Hủy</a>
          </div>

        </form>

      </div>
    </div>

  </div>
</main>

<div id="errorPopup" class="custom-popup-overlay">
  <div class="custom-popup-box">
    <div class="custom-popup-icon">✕</div>
    <div class="custom-popup-title">Thông báo hệ thống</div>
    <div class="custom-popup-msg" id="errorPopupMsg">
      <c:out value="${error}"/>
    </div>
    <button type="button" class="custom-popup-btn" onclick="closeErrorPopup()">Đóng</button>
  </div>
</div>

<script>
  document.addEventListener("DOMContentLoaded", function() {
    // Ép kiểu kiểm tra xem biến lỗi được truyền từ Servlet có dữ liệu hay không
    var hasError = "${not empty error}";

    if (hasError === "true") {
      // Kích hoạt hiển thị popup mượt mà bằng việc thêm class 'show'
      document.getElementById("errorPopup").classList.add("show");
    }
  });

  // Đóng chủ động popup đóng giao diện mờ
  function closeErrorPopup() {
    document.getElementById("errorPopup").classList.remove("show");
  }
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

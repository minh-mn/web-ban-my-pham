<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 28/05/2026
  Time: 7:09 CH
  To change this template use File | Settings | File Templates.
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

        <c:if test="${not empty error}">
          <div class="admin-alert admin-alert--danger">
            <c:out value="${error}"/>
          </div>
        </c:if>

        <c:set var="eventDateValue" value=""/>
        <c:if test="${not empty event && not empty event.eventDate}">
          <fmt:formatDate var="eventDateValue" value="${event.eventDate}" pattern="yyyy-MM-dd"/>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/events"
              enctype="multipart/form-data"
              class="admin-form">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="${mode == 'edit' ? 'update' : 'create'}"/>

          <c:if test="${mode == 'edit'}">
            <input type="hidden" name="id" value="${event.id}"/>
          </c:if>

          <div class="admin-grid-2">

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

            <div class="admin-field">
              <div class="admin-label">Ngày diễn ra sự kiện *</div>
              <input class="admin-input"
                     type="date"
                     name="eventDate"
                     value="${eventDateValue}"
                     required />
              <div class="admin-help">Hệ thống sẽ tự bóc tách ngày và tháng để hiển thị theo UI trang chủ.</div>
            </div>

            <div class="admin-field admin-field--full">
              <div class="admin-label">Hình ảnh đại diện *</div>

              <c:if test="${mode == 'edit' && not empty event.imageUrl}">
                <div style="margin-bottom: 10px;">
                  <p style="font-size: 12px; color: #666; margin-bottom: 4px;">Ảnh hiện tại:</p>
                  <img src="${pageContext.request.contextPath}${event.imageUrl}"
                       style="max-width: 200px; max-height: 120px; object-fit: cover; border-radius: 6px; border: 1px solid #ddd;"/>
                </div>
              </c:if>

              <input class="admin-input"
                     type="file"
                     name="imageFile"
                     accept="image/*"
              ${mode == 'edit' ? '' : 'required'} />
              <div class="admin-help">Hỗ trợ định dạng: .jpg, .jpeg, .png. Chọn file trực tiếp từ máy tính của bạn.</div>
            </div>

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

          <div class="admin-actions">
            <button class="admin-btn admin-btn--primary" type="submit">Lưu sự kiện</button>
            <a class="admin-btn" href="${pageContext.request.contextPath}/admin/events">Hủy</a>
          </div>

        </form>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

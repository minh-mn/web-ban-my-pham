<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 28/05/2026
  Time: 7:09 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Sự kiện cửa hàng" scope="request"/>
<c:set var="activeMenu" value="events" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<style>
  /* @import phông chữ bắt buộc nằm trên cùng của thẻ style */
  @import url('https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@300;400;600;700;800;900&display=swap');

  /* Đồng bộ phông chữ toàn cục */
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
    max-width: 400px;
    text-align: center;
    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
    transform: scale(0.8);
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  .custom-popup-overlay.show .custom-popup-box {
    transform: scale(1);
  }

  /* Biểu tượng dấu hỏi cảnh báo */
  .custom-popup-icon {
    width: 60px; height: 60px;
    background: #fef3c7; color: #d97706;
    font-size: 32px; font-weight: bold;
    display: flex; align-items: center; justify-content: center;
    margin: 0 auto 15px; border-radius: 50%;
  }

  /* Biến đổi màu biểu tượng nếu là tác vụ nguy hiểm (như Xóa) */
  .custom-popup-icon.danger-mode {
    background: #fee2e2; color: #ef4444;
  }

  /* Tiêu đề thông báo */
  .custom-popup-title {
    font-size: 18px; color: #111827;
    margin-bottom: 10px; font-weight: 700;
  }

  /* Chi tiết văn bản hỏi */
  .custom-popup-msg {
    font-size: 14px; color: #4b5563;
    line-height: 1.6; margin-bottom: 25px;
  }

  /* Khung chứa các nút bấm */
  .custom-popup-actions {
    display: flex;
    gap: 12px;
    justify-content: center;
  }

  /* Định dạng các nút bấm trong Popup */
  .custom-popup-btn {
    flex: 1;
    border: none;
    padding: 12px 20px;
    font-size: 14px;
    font-weight: 600;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  .custom-popup-btn--cancel {
    background: #f3f4f6; color: #374151;
  }
  .custom-popup-btn--cancel:hover {
    background: #e5e7eb;
  }
  .custom-popup-btn--confirm {
    background: #3b82f6; color: white;
  }
  .custom-popup-btn--confirm:hover {
    background: #2563eb;
  }
  .custom-popup-btn--confirm.danger-brand {
    background: #ef4444;
  }
  .custom-popup-btn--confirm.danger-brand:hover {
    background: #dc2626;
  }
</style>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Sự kiện cửa hàng</h1>
        <p class="admin-subtext">
          Quản lý bài viết sự kiện, workshop, tin tức và hình ảnh hiển thị tại trang chủ.
        </p>
      </div>

      <a class="admin-btn admin-btn--primary"
         href="${pageContext.request.contextPath}/admin/events?action=new">
        + Thêm sự kiện
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <div class="admin-toolbar">
          <form method="get"
                action="${pageContext.request.contextPath}/admin/events"
                class="admin-toolbar__form">

            <input type="hidden" name="action" value="list"/>

            <input class="admin-input"
                   type="text"
                   name="q"
                   value="${fn:escapeXml(param.q)}"
                   placeholder="Tìm theo tiêu đề sự kiện...">

            <button class="admin-btn" type="submit">Lọc</button>

            <c:if test="${not empty param.q}">
              <a class="admin-btn" href="${pageContext.request.contextPath}/admin/events">Xóa lọc</a>
            </c:if>
          </form>
        </div>

        <c:choose>
          <c:when test="${empty events}">
            <div class="admin-empty">Chưa có sự kiện nào được tạo.</div>
          </c:when>
          <c:otherwise>
            <table class="admin-table">
              <thead>
              <tr>
                <th style="width:70px;">ID</th>
                <th style="width:100px;">Hình ảnh</th>
                <th style="width:250px;">Tiêu đề</th>
                <th style="width:120px;">Nhãn dán (Tag)</th>
                <th style="width:350px;">Mô tả ngắn</th>
                <th style="width:130px;">Ngày diễn ra</th>
                <th style="width:200px; text-align: center;">Thao tác</th>
              </tr>
              </thead>

              <tbody>
              <c:forEach var="ev" items="${events}">
                <tr>
                  <td>#${ev.id}</td>
                  <td>
                    <c:choose>
                      <c:when test="${not empty ev.imageUrl}">
                        <img src="${pageContext.request.contextPath}${ev.imageUrl}"
                             style="width: 60px; height: 40px; object-fit: cover; border-radius: 4px; border: 1px solid #ddd;"
                             alt="Event Image"/>
                      </c:when>
                      <c:otherwise>
                        <span class="admin-muted">Không có ảnh</span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td>
                    <strong><c:out value="${ev.title}"/></strong>
                  </td>

                  <td>
                    <span class="badge badge-blue"><c:out value="${ev.tag}"/></span>
                  </td>

                  <td>
                    <div style="max-height: 40px; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; line-height: 1.4; color: #555;">
                      <c:out value="${ev.summary}"/>
                    </div>
                  </td>

                  <td>
                    <fmt:formatDate value="${ev.eventDate}" pattern="dd/MM/yyyy"/>
                  </td>

                  <td style="text-align: center;">
                      <%-- SỬA SỰ KIỆN: Kích hoạt thông qua Popup xác nhận --%>
                    <button type="button"
                            class="admin-btn"
                            onclick="triggerEdit('${pageContext.request.contextPath}/admin/events?action=edit&id=${ev.id}', '${fn:escapeXml(ev.title)}')">
                      Sửa
                    </button>

                      <%-- XÓA SỰ KIỆN: Gửi form sau khi xác nhận trên Popup --%>
                    <form method="post"
                          id="delete-form-${ev.id}"
                          action="${pageContext.request.contextPath}/admin/events"
                          class="admin-inline">

                      <%@ include file="/jsp/common/csrf.jspf" %>

                      <input type="hidden" name="action" value="delete">
                      <input type="hidden" name="id" value="${ev.id}">

                      <button class="admin-btn admin-btn--danger"
                              type="button"
                              onclick="triggerDelete('delete-form-${ev.id}', '${fn:escapeXml(ev.title)}')">
                        Xóa
                      </button>
                    </form>
                  </td>
                </tr>
              </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>

      </div>
    </div>

  </div>
</main>

<div id="confirmPopup" class="custom-popup-overlay">
  <div class="custom-popup-box">
    <div id="popupIcon" class="custom-popup-icon">?</div>
    <div id="popupTitle" class="custom-popup-title">Xác nhận tác vụ</div>
    <div id="popupMsg" class="custom-popup-msg">Bạn có chắc chắn muốn thực hiện hành động này không?</div>

    <div class="custom-popup-actions">
      <button type="button" class="custom-popup-btn custom-popup-btn--cancel" onclick="closeConfirmPopup()">Hủy bỏ</button>
      <button type="button" id="popupConfirmBtn" class="custom-popup-btn custom-popup-btn--confirm">Xác nhận</button>
    </div>
  </div>
</div>

<script>
  let pendingAction = null;

  function openConfirmPopup(title, message, isDanger, confirmCallback) {
    const popup = document.getElementById("confirmPopup");
    const icon = document.getElementById("popupIcon");
    const confirmBtn = document.getElementById("popupConfirmBtn");

    document.getElementById("popupTitle").innerText = title;
    document.getElementById("popupMsg").innerText = message;

    if (isDanger) {
      icon.innerText = "✕";
      icon.classList.add("danger-mode");
      confirmBtn.classList.add("danger-brand");
    } else {
      icon.innerText = "?";
      icon.classList.remove("danger-mode");
      confirmBtn.classList.remove("danger-brand");
    }

    pendingAction = confirmCallback;
    popup.classList.add("show");
  }

  function closeConfirmPopup() {
    document.getElementById("confirmPopup").classList.remove("show");
    pendingAction = null;
  }

  document.getElementById("popupConfirmBtn").addEventListener("click", function() {
    if (typeof pendingAction === "function") {
      pendingAction();
    }
    closeConfirmPopup();
  });

  function triggerDelete(formId, eventTitle) {
    const msg = "Bạn có chắc chắn muốn xóa sự kiện '" + eventTitle + "' không? Hành động này sẽ loại bỏ hoàn toàn dữ liệu khỏi hệ thống.";

    openConfirmPopup("Xác nhận xóa", msg, true, function() {
      document.getElementById(formId).submit();
    });
  }

  function triggerEdit(editUrl, eventTitle) {
    const msg = "Bạn có muốn mở trang chỉnh sửa thông tin cho sự kiện '" + eventTitle + "' không?";

    openConfirmPopup("Xác nhận chỉnh sửa", msg, false, function() {
      window.location.href = editUrl;
    });
  }
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

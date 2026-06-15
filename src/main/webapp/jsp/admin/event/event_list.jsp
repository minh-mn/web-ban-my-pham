<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 28/05/2026
  Time: 7:09 CH
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

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="eventTotal" value="${empty events ? 0 : fn:length(events)}" />
<c:set var="eventImageCount" value="0" />
<c:set var="eventNoImageCount" value="0" />
<c:set var="eventTaggedCount" value="0" />

<c:forEach var="evStat" items="${events}">
  <c:choose>
    <c:when test="${not empty evStat.imageUrl}">
      <c:set var="eventImageCount" value="${eventImageCount + 1}" />
    </c:when>
    <c:otherwise>
      <c:set var="eventNoImageCount" value="${eventNoImageCount + 1}" />
    </c:otherwise>
  </c:choose>

  <c:if test="${not empty evStat.tag}">
    <c:set var="eventTaggedCount" value="${eventTaggedCount + 1}" />
  </c:if>
</c:forEach>

<main class="admin-main">
  <div class="admin-container admin-event-page">

    <section class="admin-event-hero">
      <div class="admin-event-hero__content">
        <span class="admin-event-eyebrow">NỘI DUNG &amp; WEBSITE</span>
        <h1 class="admin-event-title">Sự kiện cửa hàng</h1>
        <p class="admin-event-subtitle">
          Quản lý bài viết sự kiện, workshop, tin tức và hình ảnh hiển thị tại trang chủ.
          Mỗi sự kiện nên có tiêu đề rõ, nhãn phân loại và ảnh đại diện đúng tỉ lệ.
        </p>
      </div>

      <div class="admin-event-hero__actions">
        <a class="admin-btn admin-btn--primary"
           href="${ctx}/admin/events?action=new">
          + Thêm sự kiện
        </a>
      </div>
    </section>

    <section class="admin-event-summary">
      <div class="admin-event-stat admin-event-stat--total">
        <span class="admin-event-stat__icon">📅</span>
        <span class="admin-event-stat__label">Tổng sự kiện</span>
        <strong class="admin-event-stat__value">
          <c:out value="${eventTotal}" />
        </strong>
        <span class="admin-event-stat__note">Tất cả sự kiện trong hệ thống</span>
      </div>

      <div class="admin-event-stat admin-event-stat--image">
        <span class="admin-event-stat__icon">🖼️</span>
        <span class="admin-event-stat__label">Đã có ảnh</span>
        <strong class="admin-event-stat__value">
          <c:out value="${eventImageCount}" />
        </strong>
        <span class="admin-event-stat__note">Sự kiện có ảnh đại diện</span>
      </div>

      <div class="admin-event-stat admin-event-stat--missing">
        <span class="admin-event-stat__icon">⚠️</span>
        <span class="admin-event-stat__label">Thiếu ảnh</span>
        <strong class="admin-event-stat__value">
          <c:out value="${eventNoImageCount}" />
        </strong>
        <span class="admin-event-stat__note">Nên bổ sung để hiển thị đẹp hơn</span>
      </div>

      <div class="admin-event-stat admin-event-stat--tag">
        <span class="admin-event-stat__icon">🏷️</span>
        <span class="admin-event-stat__label">Có nhãn dán</span>
        <strong class="admin-event-stat__value">
          <c:out value="${eventTaggedCount}" />
        </strong>
        <span class="admin-event-stat__note">Dùng để phân loại nội dung</span>
      </div>
    </section>

    <section class="admin-card admin-event-filter-card">
      <div class="admin-card__body">
        <div class="admin-event-section-head">
          <div>
            <h2 class="admin-event-section-title">Bộ lọc sự kiện</h2>
            <p class="admin-event-section-desc">
              Tìm nhanh sự kiện theo tiêu đề để chỉnh sửa hoặc xóa nội dung.
            </p>
          </div>

          <c:if test="${not empty param.q}">
            <span class="admin-chip admin-chip--brand">
              Đang lọc: <strong><c:out value="${param.q}" /></strong>
            </span>
          </c:if>
        </div>

        <form method="get"
              action="${ctx}/admin/events"
              class="admin-event-filter-form">

          <input type="hidden" name="action" value="list"/>

          <label class="admin-event-filter-field admin-event-filter-field--keyword">
            <span>Từ khóa</span>
            <input class="admin-input"
                   type="text"
                   name="q"
                   value="${fn:escapeXml(param.q)}"
                   placeholder="Tìm theo tiêu đề sự kiện...">
          </label>

          <div class="admin-event-filter-actions">
            <button class="admin-btn admin-btn--primary admin-event-filter-btn" type="submit">
              Lọc sự kiện
            </button>

            <c:if test="${not empty param.q}">
              <a class="admin-btn admin-event-filter-btn" href="${ctx}/admin/events">
                Xóa lọc
              </a>
            </c:if>
          </div>
        </form>
      </div>
    </section>

    <section class="admin-card admin-event-list-card">
      <div class="admin-card__body">
        <div class="admin-event-section-head admin-event-section-head--list">
          <div>
            <h2 class="admin-event-section-title">Danh sách sự kiện</h2>
            <p class="admin-event-section-desc">
              Kiểm tra ảnh, tiêu đề, nhãn dán, mô tả ngắn và ngày diễn ra của từng sự kiện.
            </p>
          </div>

          <span class="admin-chip admin-chip--brand">
            <c:out value="${eventTotal}" /> sự kiện
          </span>
        </div>

        <c:choose>
          <c:when test="${empty events}">
            <div class="admin-event-empty">
              <div class="admin-event-empty__icon">📅</div>
              <div>
                <h3>Chưa có sự kiện nào</h3>
                <p>Hãy thêm sự kiện đầu tiên để hiển thị nội dung nổi bật trên trang chủ.</p>
                <a class="admin-btn admin-btn--primary" href="${ctx}/admin/events?action=new">
                  + Thêm sự kiện
                </a>
              </div>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-event-table-wrap">
              <table class="admin-table admin-event-table">
                <thead>
                <tr>
                  <th class="admin-event-col-id">ID</th>
                  <th class="admin-event-col-image">Hình ảnh</th>
                  <th class="admin-event-col-title">Tiêu đề</th>
                  <th class="admin-event-col-tag">Nhãn dán</th>
                  <th class="admin-event-col-summary">Mô tả ngắn</th>
                  <th class="admin-event-col-date">Ngày diễn ra</th>
                  <th class="admin-event-col-actions">Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="ev" items="${events}">
                  <tr class="${empty ev.imageUrl ? 'admin-event-row--missing-image' : ''}">
                    <td class="admin-event-id-cell">
                      <strong>#<c:out value="${ev.id}" /></strong>
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${not empty ev.imageUrl}">
                          <img class="admin-event-thumb"
                               src="${ctx}${ev.imageUrl}"
                               alt="Event Image"/>
                        </c:when>
                        <c:otherwise>
                          <div class="admin-event-thumb admin-event-thumb--empty">
                            Ảnh
                          </div>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <div class="admin-event-title-cell">
                        <strong><c:out value="${ev.title}"/></strong>
                        <span>Sự kiện / Workshop / Tin tức</span>
                      </div>
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${not empty ev.tag}">
                          <span class="admin-event-tag">
                            <c:out value="${ev.tag}"/>
                          </span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill admin-pill--warning">Chưa có nhãn</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <div class="admin-event-summary-text">
                        <c:out value="${ev.summary}"/>
                      </div>
                    </td>

                    <td>
                      <div class="admin-event-date">
                        <span>Ngày diễn ra</span>
                        <strong>
                          <fmt:formatDate value="${ev.eventDate}" pattern="dd/MM/yyyy"/>
                        </strong>
                      </div>
                    </td>

                    <td class="admin-event-action-cell">
                      <div class="admin-event-actions">
                        <button type="button"
                                class="admin-btn admin-event-action-btn"
                                data-edit-url="${ctx}/admin/events?action=edit&id=${ev.id}"
                                data-event-title="${fn:escapeXml(ev.title)}"
                                onclick="triggerEdit(this.dataset.editUrl, this.dataset.eventTitle)">
                          Sửa
                        </button>

                        <form method="post"
                              id="delete-form-${ev.id}"
                              action="${ctx}/admin/events"
                              class="admin-inline">

                          <%@ include file="/jsp/common/csrf.jspf" %>

                          <input type="hidden" name="action" value="delete">
                          <input type="hidden" name="id" value="${ev.id}">

                          <button class="admin-btn admin-btn--danger admin-event-action-btn"
                                  type="button"
                                  data-form-id="delete-form-${ev.id}"
                                  data-event-title="${fn:escapeXml(ev.title)}"
                                  onclick="triggerDelete(this.dataset.formId, this.dataset.eventTitle)">
                            Xóa
                          </button>
                        </form>
                      </div>
                    </td>
                  </tr>
                </c:forEach>
                </tbody>
              </table>
            </div>
          </c:otherwise>
        </c:choose>

      </div>
    </section>

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

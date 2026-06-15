<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Pages CMS" scope="request"/>
<c:set var="activeMenu" value="pages" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTotal" value="${empty pages ? 0 : fn:length(pages)}" />
<c:set var="pagePolicyCount" value="0" />
<c:set var="pageAboutCount" value="0" />
<c:set var="pageThumbnailCount" value="0" />

<c:forEach var="pageStat" items="${pages}">
  <c:if test="${pageStat.type == 'policy'}">
    <c:set var="pagePolicyCount" value="${pagePolicyCount + 1}" />
  </c:if>
  <c:if test="${pageStat.type == 'about'}">
    <c:set var="pageAboutCount" value="${pageAboutCount + 1}" />
  </c:if>
  <c:if test="${not empty pageStat.thumbnail}">
    <c:set var="pageThumbnailCount" value="${pageThumbnailCount + 1}" />
  </c:if>
</c:forEach>

<main class="admin-main">
  <div class="admin-container admin-page-list-page">

    <section class="admin-page-list-hero">
      <div class="admin-page-list-hero__content">
        <span class="admin-page-list-eyebrow">NỘI DUNG &amp; WEBSITE</span>
        <h1 class="admin-page-list-title">Pages CMS</h1>
        <p class="admin-page-list-subtitle">
          Quản lý nội dung website như chính sách, giới thiệu, footer page và các trang tĩnh hiển thị cho khách hàng.
        </p>
      </div>

      <div class="admin-page-list-hero__actions">
        <a class="admin-btn admin-btn--primary"
           href="${ctx}/admin/pages?action=new">
          + Thêm page
        </a>
      </div>
    </section>

    <section class="admin-page-summary">
      <div class="admin-page-stat admin-page-stat--total">
        <span class="admin-page-stat__icon">📄</span>
        <span class="admin-page-stat__label">Tổng page</span>
        <strong class="admin-page-stat__value">
          <c:out value="${pageTotal}" />
        </strong>
        <span class="admin-page-stat__note">Tất cả nội dung CMS</span>
      </div>

      <div class="admin-page-stat admin-page-stat--policy">
        <span class="admin-page-stat__icon">📌</span>
        <span class="admin-page-stat__label">Chính sách</span>
        <strong class="admin-page-stat__value">
          <c:out value="${pagePolicyCount}" />
        </strong>
        <span class="admin-page-stat__note">Loại policy</span>
      </div>

      <div class="admin-page-stat admin-page-stat--about">
        <span class="admin-page-stat__icon">🏷️</span>
        <span class="admin-page-stat__label">Giới thiệu</span>
        <strong class="admin-page-stat__value">
          <c:out value="${pageAboutCount}" />
        </strong>
        <span class="admin-page-stat__note">Loại about</span>
      </div>

      <div class="admin-page-stat admin-page-stat--thumb">
        <span class="admin-page-stat__icon">🖼️</span>
        <span class="admin-page-stat__label">Có thumbnail</span>
        <strong class="admin-page-stat__value">
          <c:out value="${pageThumbnailCount}" />
        </strong>
        <span class="admin-page-stat__note">Page đã có ảnh đại diện</span>
      </div>
    </section>

    <section class="admin-card admin-page-list-card">
      <div class="admin-card__body">
        <div class="admin-page-section-head">
          <div>
            <h2 class="admin-page-section-title">Danh sách page</h2>
            <p class="admin-page-section-desc">
              Chỉnh sửa nội dung, thumbnail và loại page đang hiển thị trên website.
            </p>
          </div>

          <span class="admin-chip admin-chip--brand">
            <c:out value="${pageTotal}" /> page
          </span>
        </div>

        <c:choose>
          <c:when test="${empty pages}">
            <div class="admin-page-empty">
              <div class="admin-page-empty__icon">📄</div>
              <div>
                <h3>Chưa có page nào</h3>
                <p>Hãy tạo page đầu tiên để quản lý nội dung chính sách, giới thiệu hoặc footer.</p>
                <a class="admin-btn admin-btn--primary"
                   href="${ctx}/admin/pages?action=new">
                  + Thêm page
                </a>
              </div>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-page-table-wrap">
              <table class="admin-table admin-page-table">
                <thead>
                <tr>
                  <th class="admin-page-col-id">ID</th>
                  <th class="admin-page-col-image">Hình ảnh</th>
                  <th class="admin-page-col-title">Tiêu đề</th>
                  <th class="admin-page-col-slug">Slug</th>
                  <th class="admin-page-col-type">Loại</th>
                  <th class="admin-page-col-actions">Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="p" items="${pages}">
                  <tr class="${empty p.thumbnail ? 'admin-page-row--missing-thumb' : ''}">
                    <td class="admin-page-id-cell">
                      #<c:out value="${p.id}" />
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${not empty p.thumbnail}">
                          <img src="${p.thumbnail}"
                               data-default-src="${ctx}/assets/images/pages/default-thumbnail.png"
                               onerror="this.onerror=null; this.src=this.getAttribute('data-default-src');"
                               alt="Thumbnail page"
                               class="admin-page-thumb">
                        </c:when>
                        <c:otherwise>
                          <span class="admin-page-thumb admin-page-thumb--empty">No image</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <div class="admin-page-title-cell">
                        <strong><c:out value="${p.title}" /></strong>
                        <span>Nội dung website</span>
                      </div>
                    </td>

                    <td>
                      <span class="admin-page-slug">
                        <c:out value="${p.slug}" />
                      </span>
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${p.type == 'policy'}">
                          <span class="admin-chip admin-chip--brand">Policy</span>
                        </c:when>
                        <c:when test="${p.type == 'about'}">
                          <span class="admin-chip admin-chip--success">About</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-chip">
                            <c:out value="${p.type}" />
                          </span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td class="admin-page-action-cell">
                      <div class="admin-page-actions">
                        <button type="button"
                                class="admin-btn admin-page-action-btn js-page-edit"
                                data-edit-url="${ctx}/admin/pages?action=edit&amp;id=${p.id}"
                                data-page-title="${fn:escapeXml(p.title)}">
                          Sửa
                        </button>

                        <form id="delete-form-${p.id}"
                              action="${ctx}/admin/pages/delete"
                              method="POST"
                              class="admin-page-delete-form">
                          <%@ include file="/jsp/common/csrf.jspf" %>

                          <input type="hidden" name="id" value="${p.id}">

                          <button class="admin-btn admin-btn--danger admin-page-action-btn js-page-delete"
                                  type="button"
                                  data-form-id="delete-form-${p.id}"
                                  data-page-title="${fn:escapeXml(p.title)}">
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

  function triggerDelete(formId, pageTitle) {
    const msg = "Bạn có chắc chắn muốn xóa trang '" + pageTitle + "' không? Hành động này không thể hoàn tác!";

    openConfirmPopup("Xác nhận xóa trang", msg, true, function() {
      document.getElementById(formId).submit();
    });
  }

  function triggerEdit(editUrl, pageTitle) {
    const msg = "Bạn có muốn mở trang chỉnh sửa thông tin cho trang '" + pageTitle + "' không?";

    openConfirmPopup("Xác nhận chỉnh sửa", msg, false, function() {
      window.location.href = editUrl;
    });
  }

  document.querySelectorAll(".js-page-edit").forEach(function(button) {
    button.addEventListener("click", function() {
      triggerEdit(button.dataset.editUrl, button.dataset.pageTitle || "");
    });
  });

  document.querySelectorAll(".js-page-delete").forEach(function(button) {
    button.addEventListener("click", function() {
      triggerDelete(button.dataset.formId, button.dataset.pageTitle || "");
    });
  });
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

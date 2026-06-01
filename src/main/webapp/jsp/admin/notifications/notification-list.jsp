<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Quản lý Thông báo" scope="request"/>
<c:set var="activeMenu" value="notifications" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<c:set var="adminNotificationItems"
       value="${not empty adminNotificationList ? adminNotificationList : notificationList}" />
<c:set var="broadcastItems"
       value="${systemNotifications}" />
<c:set var="adminUnreadCountValue"
       value="${not empty adminUnreadNotificationCount ? adminUnreadNotificationCount : adminUnreadCount}" />

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container admin-notification-page">

    <jsp:include page="/jsp/admin/layout/topbar.jsp"/>

    <section class="admin-notification-hero">
      <div class="admin-notification-hero__content">
        <span class="admin-notification-eyebrow">ISSUE 114</span>

        <h1 class="admin-h1 admin-notification-title">
          Quản lý thông báo
        </h1>

        <p class="admin-subtext admin-notification-subtitle">
          Theo dõi thông báo hệ thống gửi cho admin và lịch sử các chiến dịch thông báo hàng loạt tới khách hàng.
        </p>
      </div>

      <div class="admin-notification-hero__actions">
        <a class="admin-btn admin-btn--primary"
           href="${pageContext.request.contextPath}/admin/notifications?action=new">
          + Gửi thông báo mới
        </a>

        <form action="${pageContext.request.contextPath}/admin/notifications"
              method="post"
              class="admin-inline">
          <%@ include file="/jsp/common/csrf.jspf" %>
          <input type="hidden" name="action" value="markAllRead">
          <button class="admin-btn" type="submit">
            Đánh dấu đã đọc
          </button>
        </form>
      </div>
    </section>

    <c:if test="${not empty admin_notification_success}">
      <div class="admin-alert admin-alert--success">
        <c:out value="${admin_notification_success}" />
      </div>
    </c:if>

    <c:if test="${not empty admin_notification_error}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${admin_notification_error}" />
      </div>
    </c:if>

    <section class="admin-notification-stats">
      <div class="admin-notification-stat">
        <span>Tổng thông báo admin</span>
        <strong>
          <c:out value="${empty totalNotifications ? 0 : totalNotifications}" />
        </strong>
        <small>Thông báo phát sinh từ đơn hàng, hủy/hoàn hàng và đánh giá.</small>
      </div>

      <div class="admin-notification-stat admin-notification-stat--unread">
        <span>Chưa đọc</span>
        <strong>
          <c:out value="${empty adminUnreadCountValue ? 0 : adminUnreadCountValue}" />
        </strong>
        <small>Cần kiểm tra để xử lý nghiệp vụ kịp thời.</small>
      </div>

      <div class="admin-notification-stat admin-notification-stat--campaign">
        <span>Chiến dịch đã gửi</span>
        <strong>
          <c:out value="${empty broadcastItems ? 0 : broadcastItems.size()}" />
        </strong>
        <small>Lịch sử thông báo hàng loạt tới khách hàng.</small>
      </div>
    </section>

    <section class="admin-card admin-notification-card">
      <div class="admin-card__body">
        <div class="admin-notification-section-head">
          <div>
            <h2 class="admin-notification-section-title">Thông báo dành cho admin</h2>
            <p class="admin-notification-section-desc">
              Danh sách thông báo mới nhất từ hệ thống. Bấm vào từng dòng để đánh dấu đã đọc và chuyển tới nội dung liên quan.
            </p>
          </div>
        </div>

        <c:choose>
          <c:when test="${not empty adminNotificationItems}">
            <div class="admin-notification-list">
              <c:forEach var="n" items="${adminNotificationItems}">
                <c:url var="adminNotificationReadUrl" value="/admin/notifications">
                  <c:param name="action" value="read" />
                  <c:param name="id" value="${n.id}" />
                  <c:param name="returnUrl" value="${not empty n.targetUrl ? n.targetUrl : '/admin/notifications'}" />
                </c:url>

                <a class="admin-notification-row ${n.read ? 'is-read' : 'is-unread'}"
                   href="${adminNotificationReadUrl}">
                  <span class="admin-notification-row__icon">
                    <c:choose>
                      <c:when test="${not empty n.icon}">
                        <c:out value="${n.icon}" />
                      </c:when>
                      <c:when test="${n.type == 'ORDER_CREATED'}">🛒</c:when>
                      <c:when test="${n.type == 'CANCEL_REQUEST_CREATED'}">❌</c:when>
                      <c:when test="${n.type == 'RETURN_REQUEST_CREATED'}">↩️</c:when>
                      <c:when test="${n.type == 'REVIEW_CREATED'}">⭐</c:when>
                      <c:otherwise>🔔</c:otherwise>
                    </c:choose>
                  </span>

                  <span class="admin-notification-row__main">
                    <span class="admin-notification-row__title">
                      <strong>
                        <c:out value="${n.title}" />
                      </strong>

                      <c:if test="${not n.read}">
                        <em>Mới</em>
                      </c:if>
                    </span>

                    <span class="admin-notification-row__message">
                      <c:out value="${n.message}" />
                    </span>

                    <span class="admin-notification-row__meta">
                      <c:choose>
                        <c:when test="${not empty n.type}">
                          <span class="admin-chip admin-chip--brand">
                            <c:out value="${n.type}" />
                          </span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-chip">SYSTEM</span>
                        </c:otherwise>
                      </c:choose>

                      <c:if test="${not empty n.createdAt}">
                        <span>
                          <fmt:formatDate value="${n.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                        </span>
                      </c:if>
                    </span>
                  </span>

                  <span class="admin-notification-row__arrow">→</span>
                </a>
              </c:forEach>
            </div>

            <c:if test="${not empty totalPages and totalPages gt 1}">
              <div class="admin-notification-pagination">
                <c:choose>
                  <c:when test="${currentPage gt 1}">
                    <a class="admin-notification-page-link"
                       href="${pageContext.request.contextPath}/admin/notifications?page=${currentPage - 1}&pageSize=${pageSize}">
                      ← Trước
                    </a>
                  </c:when>
                  <c:otherwise>
                    <span class="admin-notification-page-link is-disabled">← Trước</span>
                  </c:otherwise>
                </c:choose>

                <span class="admin-notification-page-current">
                  Trang <strong>${currentPage}</strong> / ${totalPages}
                </span>

                <c:choose>
                  <c:when test="${currentPage lt totalPages}">
                    <a class="admin-notification-page-link"
                       href="${pageContext.request.contextPath}/admin/notifications?page=${currentPage + 1}&pageSize=${pageSize}">
                      Sau →
                    </a>
                  </c:when>
                  <c:otherwise>
                    <span class="admin-notification-page-link is-disabled">Sau →</span>
                  </c:otherwise>
                </c:choose>
              </div>
            </c:if>
          </c:when>

          <c:otherwise>
            <div class="admin-notification-empty">
              <div class="admin-notification-empty__icon">🔕</div>
              <h3>Chưa có thông báo admin</h3>
              <p>Khi có đơn hàng mới, yêu cầu hủy/hoàn hàng hoặc đánh giá mới, thông báo sẽ xuất hiện tại đây.</p>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
    </section>

    <section class="admin-card admin-notification-card">
      <div class="admin-card__body">
        <div class="admin-notification-section-head">
          <div>
            <h2 class="admin-notification-section-title">Lịch sử thông báo hàng loạt</h2>
            <p class="admin-notification-section-desc">
              Các chiến dịch do admin gửi tới toàn bộ khách hàng.
            </p>
          </div>
        </div>

        <c:choose>
          <c:when test="${not empty broadcastItems}">
            <div class="admin-table-wrap admin-notification-campaign-wrap">
              <table class="admin-table admin-notification-campaign-table">
                <thead>
                <tr>
                  <th class="admin-notification-col-id">Mã lô</th>
                  <th>Tiêu đề</th>
                  <th class="admin-notification-col-type">Phân loại</th>
                  <th>Nội dung</th>
                  <th>Đường dẫn</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="item" items="${broadcastItems}">
                  <tr>
                    <td class="admin-id">#${item.id}</td>

                    <td>
                      <strong class="admin-notification-campaign-title">
                        <c:out value="${item.title}" />
                      </strong>
                    </td>

                    <td>
                      <span class="admin-chip admin-chip--brand">
                        <c:out value="${item.type}" />
                      </span>
                    </td>

                    <td>
                      <span class="admin-notification-campaign-message">
                        <c:out value="${item.message}" />
                      </span>
                    </td>

                    <td>
                      <code class="admin-notification-url">
                        <c:out value="${item.targetUrl}" />
                      </code>
                    </td>
                  </tr>
                </c:forEach>
                </tbody>
              </table>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-empty">
              <div class="admin-empty__title">Chưa có chiến dịch thông báo</div>
              <div class="admin-empty__text">
                Bạn có thể tạo chiến dịch mới để gửi thông báo sự kiện, voucher hoặc thông tin hệ thống tới khách hàng.
              </div>
              <a class="admin-btn admin-btn--primary"
                 href="${pageContext.request.contextPath}/admin/notifications?action=new">
                + Gửi thông báo mới
              </a>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
    </section>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

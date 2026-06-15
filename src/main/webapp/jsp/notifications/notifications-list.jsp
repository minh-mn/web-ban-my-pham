<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="notificationItems"
       value="${not empty notificationList ? notificationList : notifications}" />
<c:set var="unreadCountValue"
       value="${not empty unreadNotificationCount ? unreadNotificationCount : unreadCount}" />

<section class="notification-page">
  <div class="notification-page__container">

    <div class="notification-page__hero">
      <div class="notification-page__hero-content">
        <span class="notification-page__eyebrow">TRUNG TÂM THÔNG BÁO</span>

        <h1 class="notification-page__title">
          Tất cả thông báo của bạn
        </h1>

        <p class="notification-page__subtitle">
          Theo dõi trạng thái đơn hàng, yêu cầu hủy/hoàn, đánh giá sản phẩm và những cập nhật mới nhất từ MyCosmetic trong cùng một nơi.
        </p>
      </div>

      <div class="notification-page__hero-actions">
        <div class="notification-page__stat-grid">
          <div class="notification-page__stat">
            <strong>
              <c:out value="${empty totalNotifications ? 0 : totalNotifications}" />
            </strong>
            <span>Tổng thông báo</span>
          </div>

          <div class="notification-page__stat notification-page__stat--unread">
            <strong>
              <c:out value="${empty unreadCountValue ? 0 : unreadCountValue}" />
            </strong>
            <span>Chưa đọc</span>
          </div>
        </div>

        <form action="${pageContext.request.contextPath}/notifications"
              method="post"
              class="notification-page__mark-form">
          <input type="hidden" name="action" value="markAllRead">
          <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
          <button type="submit" class="notification-page__mark-btn">
            Đánh dấu tất cả đã đọc
          </button>
        </form>
      </div>
    </div>

    <div class="notification-list-card">
      <div class="notification-list-card__head">
        <div>
          <h2>Danh sách thông báo</h2>
          <p>
            Nhấn vào từng thông báo để đánh dấu đã đọc và chuyển nhanh tới nội dung liên quan.
          </p>
        </div>
      </div>

      <c:choose>
        <c:when test="${not empty notificationItems}">
          <div class="notification-list">
            <c:forEach var="notif" items="${notificationItems}">
              <c:url var="notificationReadUrl" value="/notifications">
                <c:param name="action" value="read" />
                <c:param name="id" value="${notif.id}" />
                <c:param name="returnUrl" value="${not empty notif.targetUrl ? notif.targetUrl : '/notifications'}" />
              </c:url>

              <a href="${notificationReadUrl}"
                 class="notification-list__item ${notif.read ? 'is-read' : 'is-unread'}">

                <span class="notification-list__icon" aria-hidden="true">
                  <c:choose>
                    <c:when test="${notif.type == 'ORDER_CREATED'}">ĐH</c:when>
                    <c:when test="${notif.type == 'ORDER_STATUS'}">VC</c:when>
                    <c:when test="${fn:startsWith(notif.type, 'CANCEL_REQUEST')}">HY</c:when>
                    <c:when test="${fn:startsWith(notif.type, 'RETURN_REQUEST')}">TH</c:when>
                    <c:when test="${fn:startsWith(notif.type, 'REVIEW_')}">DG</c:when>
                    <c:when test="${notif.type == 'VOUCHER'}">MG</c:when>
                    <c:when test="${notif.type == 'EVENT'}">SK</c:when>
                    <c:otherwise>TB</c:otherwise>
                  </c:choose>
                </span>

                <span class="notification-list__content">
                  <span class="notification-list__title-row">
                    <strong>
                      <c:out value="${notif.title}" />
                    </strong>

                    <c:if test="${not notif.read}">
                      <span class="notification-list__badge">
                        Mới
                      </span>
                    </c:if>
                  </span>

                  <span class="notification-list__type">
                    <c:choose>
                      <c:when test="${notif.type == 'ORDER_CREATED' || notif.type == 'ORDER_STATUS'}">Đơn hàng</c:when>
                      <c:when test="${fn:startsWith(notif.type, 'CANCEL_REQUEST')}">Yêu cầu hủy</c:when>
                      <c:when test="${fn:startsWith(notif.type, 'RETURN_REQUEST')}">Yêu cầu hoàn</c:when>
                      <c:when test="${fn:startsWith(notif.type, 'REVIEW_')}">Đánh giá</c:when>
                      <c:when test="${notif.type == 'VOUCHER'}">Voucher</c:when>
                      <c:when test="${notif.type == 'EVENT'}">Sự kiện</c:when>
                      <c:otherwise>Thông báo hệ thống</c:otherwise>
                    </c:choose>
                  </span>

                  <span class="notification-list__message">
                    <c:out value="${notif.message}" />
                  </span>

                  <span class="notification-list__meta-row">
                    <span class="notification-list__status ${notif.read ? 'is-read' : 'is-unread'}">
                        ${notif.read ? 'Đã đọc' : 'Chưa đọc'}
                    </span>

                    <span class="notification-list__meta">
                      <c:choose>
                        <c:when test="${not empty notif.createdAt}">
                          <c:out value="${notif.createdAt}" />
                        </c:when>
                        <c:otherwise>
                          Thông báo hệ thống
                        </c:otherwise>
                      </c:choose>
                    </span>
                  </span>
                </span>

                <span class="notification-list__arrow" aria-hidden="true">
                  →
                </span>
              </a>
            </c:forEach>
          </div>

          <c:if test="${not empty totalPages and totalPages gt 1}">
            <div class="notification-pagination">
              <c:choose>
                <c:when test="${currentPage gt 1}">
                  <a class="notification-pagination__btn"
                     href="${pageContext.request.contextPath}/notifications?page=${currentPage - 1}&pageSize=${pageSize}">
                    ← Trước
                  </a>
                </c:when>
                <c:otherwise>
                  <span class="notification-pagination__btn is-disabled">
                    ← Trước
                  </span>
                </c:otherwise>
              </c:choose>

              <span class="notification-pagination__current">
                Trang <strong>${currentPage}</strong> / ${totalPages}
              </span>

              <c:choose>
                <c:when test="${currentPage lt totalPages}">
                  <a class="notification-pagination__btn"
                     href="${pageContext.request.contextPath}/notifications?page=${currentPage + 1}&pageSize=${pageSize}">
                    Sau →
                  </a>
                </c:when>
                <c:otherwise>
                  <span class="notification-pagination__btn is-disabled">
                    Sau →
                  </span>
                </c:otherwise>
              </c:choose>
            </div>
          </c:if>
        </c:when>

        <c:otherwise>
          <div class="notification-empty-state">
            <div class="notification-empty-state__icon">TB</div>
            <h3>Bạn chưa có thông báo nào</h3>
            <p>
              Khi có đơn hàng mới, cập nhật vận chuyển, yêu cầu hủy/hoàn hàng hoặc đánh giá,
              thông báo sẽ xuất hiện tại đây.
            </p>
            <a href="${pageContext.request.contextPath}/home"
               class="notification-empty-state__btn">
              Tiếp tục mua sắm
            </a>
          </div>
        </c:otherwise>
      </c:choose>
    </div>

  </div>
</section>

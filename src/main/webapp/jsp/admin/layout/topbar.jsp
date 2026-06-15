<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="adminUser" value="${not empty sessionScope.user ? sessionScope.user : sessionScope.authUser}" />
<c:set var="adminUnreadCount" value="${not empty adminUnreadNotificationCount ? adminUnreadNotificationCount : adminUnreadCount}" />
<c:set var="adminNotificationsSource" value="${not empty adminLatestNotifications ? adminLatestNotifications : adminNotifications}" />

<header class="admin-topbar admin-notification-topbar">
  <div class="admin-topbar__left">
    <div class="admin-topbar__eyebrow">TRUNG TÂM QUẢN TRỊ</div>

    <h1 class="admin-topbar__title">
      <c:choose>
        <c:when test="${not empty pageTitle}">
          <c:out value="${pageTitle}" />
        </c:when>
        <c:otherwise>
          Quản trị hệ thống
        </c:otherwise>
      </c:choose>
    </h1>

    <p class="admin-topbar__subtitle">
      Quản lý đơn hàng, sản phẩm, khách hàng, đánh giá và thông báo hệ thống.
    </p>
  </div>

  <div class="admin-topbar__right">
    <a class="admin-topbar__site-link"
       href="${pageContext.request.contextPath}/home"
       target="_blank"
       rel="noopener">
      <span>↗</span>
      <span>Xem cửa hàng</span>
    </a>

    <div class="admin-notification">
      <button type="button"
              class="admin-notification__bell"
              aria-label="Thông báo admin"
              aria-haspopup="true"
              aria-expanded="false">
        <span class="admin-notification__icon">🔔</span>

        <c:if test="${adminUnreadCount gt 0}">
          <span class="admin-notification__badge">
            <c:out value="${adminUnreadCount}" />
          </span>
        </c:if>
      </button>

      <div class="admin-notification__dropdown">
        <div class="admin-notification__head">
          <div>
            <strong>Thông báo admin</strong>
            <small>
              <c:choose>
                <c:when test="${adminUnreadCount gt 0}">
                  <c:out value="${adminUnreadCount}" /> thông báo chưa đọc
                </c:when>
                <c:otherwise>
                  Không có thông báo chưa đọc
                </c:otherwise>
              </c:choose>
            </small>
          </div>

          <form action="${pageContext.request.contextPath}/admin/notifications"
                method="post"
                class="admin-notification__mark-form">
            <input type="hidden" name="action" value="markAllRead">
            <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
            <button type="submit" class="admin-notification__mark-btn">
              Đánh dấu đã đọc
            </button>
          </form>
        </div>

        <div class="admin-notification__body">
          <c:choose>
            <c:when test="${not empty adminNotificationsSource}">
              <c:forEach var="n" items="${adminNotificationsSource}">
                <c:url var="adminNotificationReadUrl" value="/admin/notifications">
                  <c:param name="action" value="read" />
                  <c:param name="id" value="${n.id}" />
                  <c:param name="returnUrl" value="${not empty n.targetUrl ? n.targetUrl : '/admin/notifications'}" />
                </c:url>

                <a href="${adminNotificationReadUrl}"
                   class="admin-notification__item ${n.read ? 'is-read' : 'is-unread'}">
                  <span class="admin-notification__item-icon">
                    <c:out value="${empty n.icon ? '🔔' : n.icon}" />
                  </span>

                  <span class="admin-notification__item-main">
                    <strong>
                      <c:out value="${n.title}" />
                    </strong>
                    <small>
                      <c:out value="${n.message}" />
                    </small>
                  </span>

                  <c:if test="${not n.read}">
                    <span class="admin-notification__dot"></span>
                  </c:if>
                </a>
              </c:forEach>
            </c:when>

            <c:otherwise>
              <div class="admin-notification__empty">
                <div>🔕</div>
                <p>Chưa có thông báo admin.</p>
              </div>
            </c:otherwise>
          </c:choose>
        </div>

        <a class="admin-notification__view-all"
           href="${pageContext.request.contextPath}/admin/notifications">
          Xem tất cả thông báo
        </a>
      </div>
    </div>

    <div class="admin-topbar__user">
      <div class="admin-topbar__avatar">
        <c:choose>
          <c:when test="${not empty adminUser.fullName}">
            <c:out value="${adminUser.fullName.substring(0,1)}" />
          </c:when>
          <c:when test="${not empty adminUser.username}">
            <c:out value="${adminUser.username.substring(0,1)}" />
          </c:when>
          <c:otherwise>A</c:otherwise>
        </c:choose>
      </div>

      <div class="admin-topbar__user-meta">
        <strong>
          <c:choose>
            <c:when test="${not empty adminUser.fullName}">
              <c:out value="${adminUser.fullName}" />
            </c:when>
            <c:when test="${not empty adminUser.username}">
              <c:out value="${adminUser.username}" />
            </c:when>
            <c:otherwise>Admin</c:otherwise>
          </c:choose>
        </strong>
        <small>Quản trị viên</small>
      </div>
    </div>

    <form action="${pageContext.request.contextPath}/logout"
          method="post"
          class="admin-topbar__logout-form">
      <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
      <button type="submit" class="admin-topbar__logout-btn">
        Đăng xuất
      </button>
    </form>
  </div>
</header>

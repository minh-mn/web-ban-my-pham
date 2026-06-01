<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">

<nav class="category-nav-bar">
  <div class="container">
    <ul class="menu-horizontal-list">
      <li><a href="${pageContext.request.contextPath}/home" class="menu-link-item">TRANG CHỦ</a></li>

      <li class="has-dropdown">
        <a href="${pageContext.request.contextPath}/products?category=all" class="menu-link-item">SẢN PHẨM</a>

        <c:if test="${not empty categories}">
          <ul class="dropdown-menu">
            <c:forEach var="parent" items="${categories}">
              <li>
                <a href="${pageContext.request.contextPath}/products?category=${parent.id}">${parent.name}</a>

                <c:if test="${not empty parent.children}">
                  <ul class="sub-dropdown-menu">
                    <c:forEach var="child" items="${parent.children}">
                      <li>
                        <a href="${pageContext.request.contextPath}/products?category=${child.id}">${child.name}</a>
                      </li>
                    </c:forEach>
                  </ul>
                </c:if>
              </li>
            </c:forEach>
          </ul>
        </c:if>
      </li>

      <!-- CATEGORY: BRAND -->
      <li class="has-dropdown">
        <a href="${pageContext.request.contextPath}/brands" class="menu-link-item">
          THƯƠNG HIỆU
        </a>

        <ul class="dropdown-menu">
          <c:forEach var="brand" items="${brands}">
            <li>
              <a href="${pageContext.request.contextPath}/products?brand=${brand.id}">
                  ${brand.name} (${brand.productCount})
              </a>
            </li>
          </c:forEach>
        </ul>
      </li>

      <li>
        <a href="${pageContext.request.contextPath}/flash-sale" class="menu-link-item hot-menu">
          FLASH SALE
        </a>
      </li>

      <li>
        <a href="${pageContext.request.contextPath}/promotions"
           class="menu-link-item">
          KHUYẾN MÃI
        </a>
      </li>

      <li>
        <a href="${pageContext.request.contextPath}/blog" class="menu-link-item">
          TIN TỨC
        </a>
      </li>

      <li>
        <a href="${pageContext.request.contextPath}/lien-he" class="menu-link-item">
          LIÊN HỆ
        </a>
      </li>

      <!-- ISSUE 114: USER NOTIFICATION CENTER -->
      <li class="header-notification-nav">
        <c:set var="headerUser" value="${not empty sessionScope.user ? sessionScope.user : sessionScope.authUser}" />
        <c:set var="headerUnreadCount" value="${not empty unreadNotificationCount ? unreadNotificationCount : unreadCount}" />
        <c:set var="headerNotifications" value="${not empty latestNotifications ? latestNotifications : notifications}" />

        <div class="notification-container header-notification-container">
          <button type="button"
                  class="notification-bell"
                  aria-label="Thông báo"
                  aria-haspopup="true"
                  aria-expanded="false">
            <span class="notification-bell__icon">🔔</span>

            <c:if test="${not empty headerUser and headerUnreadCount gt 0}">
              <span class="notif-badge">${headerUnreadCount}</span>
            </c:if>
          </button>

          <div class="notif-dropdown header-notification-dropdown">
            <c:choose>
              <c:when test="${not empty headerUser}">
                <div class="notif-dropdown__head">
                  <span>Thông báo</span>

                  <form class="notification-mark-all-form"
                        action="${pageContext.request.contextPath}/notifications"
                        method="post">
                    <input type="hidden" name="action" value="markAllRead">
                    <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                    <button type="submit" class="notification-mark-all-btn">
                      Đánh dấu đã đọc
                    </button>
                  </form>
                </div>

                <div class="notif-dropdown__body">
                  <c:choose>
                    <c:when test="${not empty headerNotifications}">
                      <c:forEach var="n" items="${headerNotifications}">
                        <c:url var="notificationReadUrl" value="/notifications">
                          <c:param name="action" value="read" />
                          <c:param name="id" value="${n.id}" />
                          <c:param name="returnUrl" value="${not empty n.targetUrl ? n.targetUrl : '/notifications'}" />
                        </c:url>

                        <a class="notification-item ${n.read ? 'is-read' : 'is-unread'}"
                           href="${notificationReadUrl}">
                          <span class="notification-item__icon">
                            <c:out value="${empty n.icon ? '🔔' : n.icon}" />
                          </span>

                          <span class="notification-item__content">
                            <strong>
                              <c:out value="${n.title}" />
                            </strong>
                            <small>
                              <c:out value="${n.message}" />
                            </small>
                          </span>

                          <c:if test="${not n.read}">
                            <span class="notification-item__dot"></span>
                          </c:if>
                        </a>
                      </c:forEach>
                    </c:when>

                    <c:otherwise>
                      <div class="notification-empty">
                        <div class="notification-empty__icon">🔕</div>
                        <p>Bạn chưa có thông báo mới.</p>
                      </div>
                    </c:otherwise>
                  </c:choose>
                </div>

                <a class="notification-view-all"
                   href="${pageContext.request.contextPath}/notifications">
                  Xem tất cả thông báo
                </a>
              </c:when>

              <c:otherwise>
                <div class="notification-guest">
                  <div class="notification-guest__icon">🔔</div>
                  <h4>Thông báo cá nhân</h4>
                  <p>Đăng nhập để theo dõi đơn hàng, hủy/hoàn hàng và đánh giá.</p>
                  <a class="notification-login-link"
                     href="${pageContext.request.contextPath}/login">
                    Đăng nhập
                  </a>
                </div>
              </c:otherwise>
            </c:choose>
          </div>
        </div>
      </li>


    </ul>
  </div>
</nav>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<section class="section store-events">
  <div class="container">
    <div class="section-header">
      <h2 class="section-title">SỰ KIỆN CỬA HÀNG</h2>
      <a href="${pageContext.request.contextPath}/blog" class="view-all">Xem tất cả ›</a>
    </div>

    <div class="event-grid">
      <c:choose>
        <c:when test="${not empty recentEvents}">
          <c:forEach var="event" items="${recentEvents}">
            <div class="event-card">
              <div class="event-img">
                  <c:choose>
                  <c:when test="${not empty event.imageUrl}">
                    <img src="${pageContext.request.contextPath}${event.imageUrl}" alt="${event.title}">
                  </c:when>
                  <c:otherwise>
                    <img src="${pageContext.request.contextPath}/assets/images/events/default.jpg" alt="Default Image">
                  </c:otherwise>
                </c:choose>

                <div class="event-date">
                  <span class="day"><fmt:formatDate value="${event.eventDate}" pattern="dd"/></span>
                  <span class="month">Th<fmt:formatDate value="${event.eventDate}" pattern="MM"/></span>
                </div>
              </div>

              <div class="event-content">
                <span class="event-tag">${event.tag}</span>
                <h3>${event.title}</h3>
                <p>${event.summary}</p>
                <a href="${pageContext.request.contextPath}/blog/detail?id=${event.id}" class="btn-event">Xem chi tiết</a>
              </div>
            </div>
          </c:forEach>
        </c:when>

        <c:otherwise>
          <div style="text-align: center; width: 100%; padding: 30px; color: #888; font-style: italic;">
            Hiện tại chưa có sự kiện nào được thiết lập.
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</section>

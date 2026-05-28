<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 28/05/2026
  Time: 7:53 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<section class="section store-events" style="padding: 40px 0;">
    <div class="container">
        <div class="section-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; border-bottom: 2px solid #f5f5f5; padding-bottom: 15px;">
            <h2 class="section-title" style="margin: 0; font-size: 24px; color: #333;">TIN TỨC & SỰ KIỆN</h2>

            <form action="${pageContext.request.contextPath}/blog" method="get" style="display: flex; gap: 8px;">
                <input type="text" name="search" value="<c:out value='${searchQuery}'/>"
                       placeholder="Tìm kiếm bài viết, sự kiện..."
                       style="padding: 8px 16px; border: 1px solid #ddd; border-radius: 20px; outline: none; width: 260px; font-size: 14px;">
                <button type="submit" style="padding: 8px 20px; background: #ff5fa2; color: #fff; border: none; border-radius: 20px; cursor: pointer; font-size: 14px; font-weight: 500;">Tìm kiếm</button>
            </form>
        </div>

        <div class="event-grid">
            <c:choose>
                <c:when test="${not empty events}">
                    <c:forEach var="event" items="${events}">
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
                    <div style="text-align: center; width: 100%; padding: 60px 0; color: #888; font-style: italic;">
                        <c:choose>
                            <c:when test="${not empty searchQuery}">
                                Không tìm thấy sự kiện hoặc bài viết nào khớp với từ khóa "${searchQuery}".
                            </c:when>
                            <c:otherwise>
                                Hiện tại hệ thống chưa cập nhật sự kiện nào mới.
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</section>

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

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/events-style.css">

<section class="store-events-section">
    <div class="events-container">

        <div class="events-header">
            <h2 class="events-title">TIN TỨC & SỰ KIỆN</h2>

            <form action="${pageContext.request.contextPath}/blog" method="get" class="events-search-form">
                <input type="text"
                       name="search"
                       value="<c:out value='${searchQuery}'/>"
                       class="events-search-input"
                       placeholder="Tìm kiếm bài viết, sự kiện...">
                <button type="submit" class="events-search-btn">Tìm kiếm</button>
            </form>
        </div>

        <div class="events-grid">
            <c:choose>
                <c:when test="${not empty events}">
                    <c:forEach var="event" items="${events}">
                        <div class="event-custom-card">

                            <div class="event-card-img-wrapper">
                                <c:choose>
                                    <c:when test="${not empty event.imageUrl}">
                                        <img src="${pageContext.request.contextPath}${event.imageUrl}" alt="${event.title}">
                                    </c:when>
                                    <c:otherwise>
                                        <img src="${pageContext.request.contextPath}/assets/images/events/default.jpg" alt="Default Image">
                                    </c:otherwise>
                                </c:choose>

                                <div class="event-card-date-badge">
                                    <span class="day"><fmt:formatDate value="${event.eventDate}" pattern="dd"/></span>
                                    <span class="month">Th<fmt:formatDate value="${event.eventDate}" pattern="MM"/></span>
                                </div>
                            </div>

                            <div class="event-card-body">
                                <span class="event-card-tag"><c:out value="${event.tag}"/></span>
                                <h3><c:out value="${event.title}"/></h3>
                                <p><c:out value="${event.summary}"/></p>
                                <a href="${pageContext.request.contextPath}/blog/detail?id=${event.id}" class="event-card-btn-action">
                                    Xem chi tiết
                                </a>
                            </div>

                        </div>
                    </c:forEach>
                </c:when>

                <c:otherwise>
                    <div class="event-empty-container">
                        <c:choose>
                            <c:when test="${not empty searchQuery}">
                                Không tìm thấy sự kiện hoặc bài viết nào khớp với từ khóa "<c:out value="${searchQuery}"/>".
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

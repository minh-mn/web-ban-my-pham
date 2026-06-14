<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 28/05/2026
  Time: 7:54 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/events-style.css">

<div class="detail-page-wrapper">
  <div class="detail-page-container">

    <div style="margin-bottom: 5px;">
      <a href="${pageContext.request.contextPath}/blog" class="back-to-list-link">
        ‹ Quay lại danh sách Tin tức & Sự kiện
      </a>
    </div>

    <article class="event-full-box">

      <div class="event-meta-info">
        <span class="event-meta-tag">
          <c:out value="${event.tag}"/>
        </span>
        <span class="event-meta-time">
          📅 Thời gian: <fmt:formatDate value="${event.eventDate}" pattern="dd/MM/yyyy HH:mm"/>
        </span>
      </div>

      <h1 class="event-main-title">
        <c:out value="${event.title}"/>
      </h1>

      <div class="event-banner-media">
        <c:choose>
          <c:when test="${not empty event.imageUrl}">
            <img src="${pageContext.request.contextPath}${event.imageUrl}" alt="${event.title}">
          </c:when>
          <c:otherwise>
            <img src="${pageContext.request.contextPath}/assets/images/events/default.jpg" alt="Default Image">
          </c:otherwise>
        </c:choose>
      </div>

      <div class="event-article-body"><c:out value="${event.summary}"/></div>

      <div class="event-detail-footer">
        <a href="${pageContext.request.contextPath}/home" class="btn-go-home">
          Quay về Trang chủ
        </a>
      </div>

    </article>

  </div>
</div>

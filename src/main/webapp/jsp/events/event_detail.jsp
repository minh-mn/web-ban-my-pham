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

<div class="container" style="padding: 40px 0; max-width: 850px; margin: 0 auto;">
  <div style="margin-bottom: 25px;">
    <a href="${pageContext.request.contextPath}/blog" style="color: #ff5fa2; text-decoration: none; font-size: 14px; font-weight: 500;">
      ‹ Quay lại danh sách Tin tức & Sự kiện
    </a>
  </div>

  <article class="event-detail-box">
    <div style="margin-bottom: 15px; display: flex; align-items: center; gap: 15px;">
      <span class="event-tag" style="background: #ff5fa2; color: white; padding: 4px 14px; border-radius: 20px; font-size: 12px; font-weight: bold; text-transform: uppercase; display: inline-block;">
        ${event.tag}
      </span>
      <span style="color: #777; font-size: 14px;">
        📅 Thời gian: <fmt:formatDate value="${event.eventDate}" pattern="dd/MM/yyyy HH:mm"/>
      </span>
    </div>

    <h1 style="font-size: 30px; color: #222; margin: 10px 0 25px 0; line-height: 1.4; font-weight: 700;">
      ${event.title}
    </h1>

    <div style="text-align: center; margin-bottom: 35px; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08);">
      <c:choose>
        <c:when test="${not empty event.imageUrl}">
          <img src="${pageContext.request.contextPath}${event.imageUrl}" alt="${event.title}" style="width: 100%; max-height: 480px; object-fit: cover;">
        </c:when>
        <c:otherwise>
          <img src="${pageContext.request.contextPath}/assets/images/events/default.jpg" alt="Default Image" style="width: 100%; max-height: 480px; object-fit: cover;">
        </c:otherwise>
      </c:choose>
    </div>

    <div style="font-size: 16px; color: #333; line-height: 1.8; white-space: pre-line; background: #fffcfd; padding: 30px; border-radius: 12px; border-left: 5px solid #ff5fa2; border-top: 1px solid #fdf0f4; border-right: 1px solid #fdf0f4; border-bottom: 1px solid #fdf0f4;">
      <c:out value="${event.summary}"/>
    </div>

    <div style="margin-top: 50px; border-top: 1px solid #eee; padding-top: 25px; text-align: center;">
      <a href="${pageContext.request.contextPath}/home" style="display: inline-block; padding: 10px 30px; background: #333; color: #fff; text-decoration: none; border-radius: 25px; font-size: 14px; transition: 0.3s;">
        Quay về Trang chủ
      </a>
    </div>
  </article>
</div>

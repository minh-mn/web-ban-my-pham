<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.webshop.app.model.Event" %>
<%@ page import="java.util.*" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/events-style.css?v=layout_topic_fix_v2">

<section class="mc-news-detail-page">
    <div class="mc-news-detail-shell">

        <c:choose>
            <c:when test="${not empty event}">
                <a href="${pageContext.request.contextPath}/blog/${event.topicKey}"
                   class="mc-news-back-link">
                    ← Quay lại chuyên mục <c:out value="${event.tag}"/>
                </a>

                <article class="mc-news-detail-card">
                    <div class="mc-news-detail-card__hero mc-topic-${event.topicKey}">
                        <div class="mc-news-detail-card__meta">
                            <span class="mc-news-detail-card__tag">
                                <c:out value="${event.tag}"/>
                            </span>

                            <span class="mc-news-detail-card__time">
                                <fmt:formatDate value="${event.eventDate}" pattern="dd/MM/yyyy"/>
                            </span>
                        </div>

                        <h1><c:out value="${event.title}"/></h1>
                        <p><c:out value="${event.summary}"/></p>
                    </div>

                    <div class="mc-news-detail-card__content">
                        <c:out value="${event.content}"/>
                    </div>

                    <div class="mc-news-detail-card__footer">
                        <a href="${pageContext.request.contextPath}${event.actionUrl}"
                           class="mc-news-detail-card__home-btn">
                            <c:out value="${event.actionText}"/>
                        </a>
                    </div>
                </article>
            </c:when>

            <c:otherwise>
                <div class="mc-news-empty">
                    <h3>Không tìm thấy bài viết</h3>
                    <p>Bài viết không tồn tại hoặc đường dẫn đã thay đổi.</p>
                    <a href="${pageContext.request.contextPath}/blog">Quay lại Tin tức</a>
                </div>
            </c:otherwise>
        </c:choose>

        <c:if test="${not empty relatedEvents}">
            <section class="mc-related-news">
                <div class="mc-news-section-head">
                    <div>
                        <span class="mc-news-section-label">Bài liên quan</span>
                        <h2>Cùng chuyên mục</h2>
                    </div>
                </div>

                <div class="mc-news-grid mc-news-grid--related">
                    <c:forEach var="related" items="${relatedEvents}">
                        <article class="mc-news-card">
                            <div class="mc-news-card__media mc-topic-${related.topicKey}">
                                <div class="mc-news-card__placeholder">
                                    <span class="mc-news-card__placeholder-tag">
                                        <c:out value="${related.tag}"/>
                                    </span>
                                </div>

                                <div class="mc-news-card__date">
                                    <span class="day">
                                        <fmt:formatDate value="${related.eventDate}" pattern="dd"/>
                                    </span>
                                    <span class="month">
                                        Th<fmt:formatDate value="${related.eventDate}" pattern="MM"/>
                                    </span>
                                </div>
                            </div>

                            <div class="mc-news-card__body">
                                <span class="mc-news-card__tag">
                                    <c:out value="${related.tag}"/>
                                </span>

                                <h3><c:out value="${related.title}"/></h3>
                                <p><c:out value="${related.summary}"/></p>

                                <a href="${pageContext.request.contextPath}/blog/bai-viet/${related.slug}"
                                   class="mc-news-card__action">
                                    Đọc bài viết
                                </a>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </section>
        </c:if>

    </div>
</section>

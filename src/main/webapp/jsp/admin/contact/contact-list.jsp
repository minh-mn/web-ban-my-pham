<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Tin nhắn liên hệ" scope="request" />
<c:set var="activeMenu" value="contact-messages" scope="request" />
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request" />

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

<c:set var="contactTotal" value="${empty list ? 0 : fn:length(list)}" />
<c:set var="contactEmailCount" value="0" />
<c:set var="contactPhoneCount" value="0" />
<c:set var="contactSubjectCount" value="0" />

<c:forEach var="contactStat" items="${list}">
  <c:if test="${not empty contactStat.email}">
    <c:set var="contactEmailCount" value="${contactEmailCount + 1}" />
  </c:if>
  <c:if test="${not empty contactStat.phone}">
    <c:set var="contactPhoneCount" value="${contactPhoneCount + 1}" />
  </c:if>
  <c:if test="${not empty contactStat.subject}">
    <c:set var="contactSubjectCount" value="${contactSubjectCount + 1}" />
  </c:if>
</c:forEach>

<main class="admin-main">
  <div class="admin-container admin-contact-page">

    <section class="admin-contact-hero">
      <div class="admin-contact-hero__content">
        <span class="admin-contact-eyebrow">NỘI DUNG &amp; WEBSITE</span>
        <h1 class="admin-contact-title">Tin nhắn liên hệ</h1>
        <p class="admin-contact-subtitle">
          Theo dõi các tin nhắn khách hàng gửi từ form liên hệ, gồm họ tên, email, số điện thoại,
          chủ đề, nội dung và thời gian gửi để admin phản hồi kịp thời.
        </p>
      </div>

      <div class="admin-contact-hero__actions">
        <span class="admin-chip admin-chip--brand">
          ✉️ <c:out value="${contactTotal}" /> tin nhắn
        </span>
      </div>
    </section>

    <section class="admin-contact-summary">
      <div class="admin-contact-stat admin-contact-stat--total">
        <span class="admin-contact-stat__icon">✉️</span>
        <span class="admin-contact-stat__label">Tổng tin nhắn</span>
        <strong class="admin-contact-stat__value">
          <c:out value="${contactTotal}" />
        </strong>
        <span class="admin-contact-stat__note">Tất cả liên hệ trong hệ thống</span>
      </div>

      <div class="admin-contact-stat admin-contact-stat--email">
        <span class="admin-contact-stat__icon">📧</span>
        <span class="admin-contact-stat__label">Có email</span>
        <strong class="admin-contact-stat__value">
          <c:out value="${contactEmailCount}" />
        </strong>
        <span class="admin-contact-stat__note">Có thể phản hồi qua email</span>
      </div>

      <div class="admin-contact-stat admin-contact-stat--phone">
        <span class="admin-contact-stat__icon">📞</span>
        <span class="admin-contact-stat__label">Có số điện thoại</span>
        <strong class="admin-contact-stat__value">
          <c:out value="${contactPhoneCount}" />
        </strong>
        <span class="admin-contact-stat__note">Có thể liên hệ trực tiếp</span>
      </div>

      <div class="admin-contact-stat admin-contact-stat--subject">
        <span class="admin-contact-stat__icon">📝</span>
        <span class="admin-contact-stat__label">Có chủ đề</span>
        <strong class="admin-contact-stat__value">
          <c:out value="${contactSubjectCount}" />
        </strong>
        <span class="admin-contact-stat__note">Tin nhắn có nội dung phân loại</span>
      </div>
    </section>

    <section class="admin-card admin-contact-list-card">
      <div class="admin-card__body">
        <div class="admin-contact-section-head">
          <div>
            <h2 class="admin-contact-section-title">Danh sách tin nhắn</h2>
            <p class="admin-contact-section-desc">
              Kiểm tra thông tin khách hàng, nội dung liên hệ và thời gian gửi tin nhắn.
            </p>
          </div>

          <span class="admin-chip admin-chip--brand">
            <c:out value="${contactTotal}" /> liên hệ
          </span>
        </div>

        <c:choose>
          <c:when test="${empty list}">
            <div class="admin-contact-empty">
              <div class="admin-contact-empty__icon">✉️</div>
              <div>
                <h3>Chưa có tin nhắn liên hệ</h3>
                <p>Khi khách hàng gửi form liên hệ, thông tin sẽ hiển thị tại đây để admin theo dõi và phản hồi.</p>
              </div>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-contact-table-wrap">
              <table class="admin-table admin-contact-table">
                <thead>
                <tr>
                  <th class="admin-contact-col-id">ID</th>
                  <th class="admin-contact-col-customer">Khách hàng</th>
                  <th class="admin-contact-col-contact">Liên hệ</th>
                  <th class="admin-contact-col-subject">Chủ đề</th>
                  <th class="admin-contact-col-message">Nội dung</th>
                  <th class="admin-contact-col-time">Thời gian</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="msg" items="${list}">
                  <tr class="admin-contact-row">
                    <td class="admin-contact-id-cell">
                      <strong>#<c:out value="${msg.id}" /></strong>
                    </td>

                    <td>
                      <div class="admin-contact-customer">
                        <span class="admin-contact-avatar">
                          <c:choose>
                            <c:when test="${not empty msg.fullName}">
                              <c:out value="${fn:substring(msg.fullName, 0, 1)}" />
                            </c:when>
                            <c:otherwise>KH</c:otherwise>
                          </c:choose>
                        </span>
                        <span class="admin-contact-customer__body">
                          <strong>
                            <c:out value="${empty msg.fullName ? 'Khách hàng' : msg.fullName}" />
                          </strong>
                          <small>Người gửi liên hệ</small>
                        </span>
                      </div>
                    </td>

                    <td>
                      <div class="admin-contact-methods">
                        <c:choose>
                          <c:when test="${not empty msg.email}">
                            <span class="admin-contact-method">📧 <c:out value="${msg.email}" /></span>
                          </c:when>
                          <c:otherwise>
                            <span class="admin-muted">Chưa có email</span>
                          </c:otherwise>
                        </c:choose>

                        <c:choose>
                          <c:when test="${not empty msg.phone}">
                            <span class="admin-contact-method">📞 <c:out value="${msg.phone}" /></span>
                          </c:when>
                          <c:otherwise>
                            <span class="admin-muted">Chưa có SĐT</span>
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </td>

                    <td>
                      <div class="admin-contact-subject">
                        <c:choose>
                          <c:when test="${not empty msg.subject}">
                            <c:out value="${msg.subject}" />
                          </c:when>
                          <c:otherwise>
                            <span class="admin-muted">Không có chủ đề</span>
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </td>

                    <td>
                      <div class="admin-contact-message">
                        <c:out value="${empty msg.message ? '-' : msg.message}" />
                      </div>
                    </td>

                    <td>
                      <div class="admin-contact-time">
                        <span><c:out value="${msg.createdAt}" /></span>
                      </div>
                    </td>
                  </tr>
                </c:forEach>
                </tbody>
              </table>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
    </section>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp" />

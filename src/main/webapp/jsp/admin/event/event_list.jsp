<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 28/05/2026
  Time: 7:09 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Sự kiện cửa hàng" scope="request"/>
<c:set var="activeMenu" value="events" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Sự kiện cửa hàng</h1>
        <p class="admin-subtext">
          Quản lý bài viết sự kiện, workshop, tin tức và hình ảnh hiển thị tại trang chủ.
        </p>
      </div>

      <a class="admin-btn admin-btn--primary"
         href="${pageContext.request.contextPath}/admin/events?action=new">
        + Thêm sự kiện
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <div class="admin-toolbar">
          <form method="get"
                action="${pageContext.request.contextPath}/admin/events"
                class="admin-toolbar__form">

            <input type="hidden" name="action" value="list"/>

            <input class="admin-input"
                   type="text"
                   name="q"
                   value="${fn:escapeXml(param.q)}"
                   placeholder="Tìm theo tiêu đề sự kiện...">

            <button class="admin-btn" type="submit">Lọc</button>

            <c:if test="${not empty param.q}">
              <a class="admin-btn" href="${pageContext.request.contextPath}/admin/events">Xóa lọc</a>
            </c:if>
          </form>
        </div>

        <c:choose>
          <c:when test="${empty events}">
            <div class="admin-empty">Chưa có sự kiện nào được tạo.</div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
              <tr>
                <th style="width:70px;">ID</th>
                <th style="width:100px;">Hình ảnh</th>
                <th style="width:250px;">Tiêu đề</th>
                <th style="width:120px;">Nhãn dán (Tag)</th>
                <th style="width:350px;">Mô tả ngắn</th>
                <th style="width:130px;">Ngày diễn ra</th>
                <th style="width:200px; text-align: center;">Thao tác</th>
              </tr>
              </thead>

              <tbody>
              <c:forEach var="ev" items="${events}">
                <tr>
                  <td>#${ev.id}</td>
                  <td>
                    <c:choose>
                      <c:when test="${not empty ev.imageUrl}">
                        <img src="${pageContext.request.contextPath}${ev.imageUrl}"
                             style="width: 60px; height: 40px; object-fit: cover; border-radius: 4px; border: 1px solid #ddd;"
                             alt="Event Image"/>
                      </c:when>
                      <c:otherwise>
                        <span class="admin-muted">Không có ảnh</span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td>
                    <strong><c:out value="${ev.title}"/></strong>
                  </td>

                  <td>
                    <span class="badge badge-blue"><c:out value="${ev.tag}"/></span>
                  </td>

                  <td>
                    <div style="max-height: 40px; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; line-height: 1.4; color: #555;">
                      <c:out value="${ev.summary}"/>
                    </div>
                  </td>

                  <td>
                    <fmt:formatDate value="${ev.eventDate}" pattern="dd/MM/yyyy"/>
                  </td>

                  <td style="text-align: center;">
                    <a href="${pageContext.request.contextPath}/admin/events?action=edit&id=${ev.id}"
                       class="admin-btn">Sửa</a>

                    <form method="post"
                          action="${pageContext.request.contextPath}/admin/events"
                          class="admin-inline"
                          onsubmit="return confirm('Xóa sự kiện này?')">

                      <%@ include file="/jsp/common/csrf.jspf" %>

                      <input type="hidden" name="action" value="delete">
                      <input type="hidden" name="id" value="${ev.id}">

                      <button class="admin-btn admin-btn--danger" type="submit">
                        Xóa
                      </button>
                    </form>
                  </td>
                </tr>
              </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>
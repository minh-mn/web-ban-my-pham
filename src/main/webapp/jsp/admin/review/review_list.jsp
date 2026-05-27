<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Duyệt đánh giá" scope="request"/>
<c:set var="activeMenu" value="reviews" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Duyệt đánh giá</h1>
        <p class="admin-subtext">Quản trị viên kiểm duyệt đánh giá trước khi hiển thị công khai.</p>
      </div>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <div class="admin-toolbar">
          <form method="get" action="${pageContext.request.contextPath}/admin/reviews" class="admin-toolbar__form">
            <input class="admin-input" type="number" name="rating" min="1" max="5" value="${param.rating}" placeholder="Số sao 1-5">
            <input class="admin-input" type="number" name="productId" value="${param.productId}" placeholder="Product ID">
            <input class="admin-input" type="number" name="authorId" value="${param.authorId}" placeholder="User ID">

            <select class="admin-input" name="status">
              <option value="" ${empty param.status ? 'selected' : ''}>Tất cả trạng thái</option>
              <option value="PENDING" ${param.status == 'PENDING' ? 'selected' : ''}>Chờ duyệt</option>
              <option value="APPROVED" ${param.status == 'APPROVED' ? 'selected' : ''}>Đã duyệt</option>
              <option value="REJECTED" ${param.status == 'REJECTED' ? 'selected' : ''}>Từ chối</option>
              <option value="HIDDEN" ${param.status == 'HIDDEN' ? 'selected' : ''}>Đang ẩn</option>
            </select>

            <select class="admin-input" name="media">
              <option value="" ${empty param.media ? 'selected' : ''}>Tất cả media</option>
              <option value="IMAGE" ${param.media == 'IMAGE' ? 'selected' : ''}>Có ảnh</option>
              <option value="VIDEO" ${param.media == 'VIDEO' ? 'selected' : ''}>Có video</option>
              <option value="MEDIA" ${param.media == 'MEDIA' ? 'selected' : ''}>Có ảnh/video</option>
            </select>

            <button class="admin-btn" type="submit">Lọc</button>
            <a class="admin-btn" href="${pageContext.request.contextPath}/admin/reviews">Xóa lọc</a>
          </form>
        </div>

        <c:choose>
          <c:when test="${empty reviews}">
            <div class="admin-empty">Chưa có đánh giá phù hợp.</div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
              <tr>
                <th style="width:80px;">ID</th>
                <th style="width:170px;">Trạng thái</th>
                <th style="width:90px;">Sao</th>
                <th>Sản phẩm</th>
                <th style="width:130px;">Người đánh giá</th>
                <th>Bình luận</th>
                <th style="width:150px;">Media</th>
                <th style="width:160px;">Thời gian</th>
                <th style="width:280px;">Thao tác</th>
              </tr>
              </thead>
              <tbody>
              <c:forEach var="r" items="${reviews}">
                <tr>
                  <td>#${r.id}</td>
                  <td>
                    <span class="admin-pill ${r.statusCssClass}"><c:out value="${r.statusLabel}" /></span>
                    <c:if test="${r.voucherAwarded}">
                      <div style="margin-top:6px;"><span class="admin-pill admin-pill--ok">Đã cấp voucher</span></div>
                    </c:if>
                  </td>
                  <td><strong>${r.rating}★</strong></td>
                  <td>
                    <strong><c:out value="${r.productName}" /></strong>
                    <div class="admin-muted">#${r.productId}</div>
                  </td>
                  <td>
                    <c:out value="${r.authorName}" />
                    <div class="admin-muted">#${r.authorId}</div>
                  </td>
                  <td>
                    <div class="admin-break"><c:out value="${r.comment}" /></div>
                    <c:if test="${not empty r.adminNote}">
                      <div class="admin-muted" style="margin-top:6px;">Ghi chú: <c:out value="${r.adminNote}" /></div>
                    </c:if>
                  </td>
                  <td>
                    <c:choose>
                      <c:when test="${r.hasImage || r.hasVideo}">
                        <c:if test="${r.hasImage}"><span class="admin-pill">Ảnh</span></c:if>
                        <c:if test="${r.hasVideo}"><span class="admin-pill">Video</span></c:if>
                      </c:when>
                      <c:otherwise><span class="admin-muted">Không có</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td class="admin-muted">
                    <fmt:formatDate value="${r.createdAtDate}" pattern="dd/MM/yyyy HH:mm"/>
                  </td>
                  <td class="product-actions">
                    <a class="admin-btn" href="${pageContext.request.contextPath}/admin/reviews?action=detail&id=${r.id}">Xem</a>

                    <form method="post" action="${pageContext.request.contextPath}/admin/reviews" class="admin-inline">
                      <%@ include file="/jsp/common/csrf.jspf" %>
                      <input type="hidden" name="action" value="approve"/>
                      <input type="hidden" name="id" value="${r.id}"/>
                      <button class="admin-btn admin-btn--ok" type="submit">Duyệt</button>
                    </form>

                    <form method="post" action="${pageContext.request.contextPath}/admin/reviews" class="admin-inline" onsubmit="return confirm('Từ chối đánh giá này?');">
                      <%@ include file="/jsp/common/csrf.jspf" %>
                      <input type="hidden" name="action" value="reject"/>
                      <input type="hidden" name="id" value="${r.id}"/>
                      <button class="admin-btn admin-btn--danger" type="submit">Từ chối</button>
                    </form>

                    <form method="post" action="${pageContext.request.contextPath}/admin/reviews" class="admin-inline" onsubmit="return confirm('Xóa vĩnh viễn đánh giá này?');">
                      <%@ include file="/jsp/common/csrf.jspf" %>
                      <input type="hidden" name="action" value="delete"/>
                      <input type="hidden" name="id" value="${r.id}"/>
                      <button class="admin-btn admin-btn--danger" type="submit">Xóa</button>
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

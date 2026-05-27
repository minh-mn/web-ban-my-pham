<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Chi tiết đánh giá" scope="request"/>
<c:set var="activeMenu" value="reviews" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Chi tiết đánh giá #${review.id}</h1>
        <p class="admin-subtext">Kiểm duyệt nội dung, ẩn/hiện đánh giá và cấp voucher sau khi duyệt.</p>
      </div>
      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/reviews">Quay lại</a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <div class="admin-form-grid">
          <div class="admin-field">
            <label>Trạng thái</label>
            <div><span class="admin-pill ${review.statusCssClass}"><c:out value="${review.statusLabel}" /></span></div>
          </div>

          <div class="admin-field">
            <label>Voucher</label>
            <div>
              <c:choose>
                <c:when test="${review.voucherAwarded}"><span class="admin-pill admin-pill--ok">Đã cấp voucher</span></c:when>
                <c:otherwise><span class="admin-pill admin-pill--warning">Chưa cấp</span></c:otherwise>
              </c:choose>
            </div>
          </div>

          <div class="admin-field">
            <label>Sản phẩm</label>
            <div><strong><c:out value="${review.productName}" /></strong> (#${review.productId})</div>
          </div>

          <div class="admin-field">
            <label>Người đánh giá</label>
            <div><c:out value="${review.authorName}" /> (#${review.authorId})</div>
          </div>

          <div class="admin-field">
            <label>Số sao</label>
            <div><strong>${review.rating}★</strong></div>
          </div>

          <div class="admin-field">
            <label>Thời gian gửi</label>
            <div><fmt:formatDate value="${review.createdAtDate}" pattern="dd/MM/yyyy HH:mm" /></div>
          </div>

          <div class="admin-field admin-field--full">
            <label>Bình luận</label>
            <div class="admin-help" style="white-space: pre-wrap;"><c:out value="${review.comment}" /></div>
          </div>

          <c:if test="${review.hasImage || review.hasVideo}">
            <div class="admin-field admin-field--full">
              <label>Hình ảnh / video</label>
              <div style="display:flex;gap:10px;flex-wrap:wrap;">
                <c:if test="${review.hasImage && not empty review.imageUrl}">
                  <a class="admin-btn" href="${review.imageUrl}" target="_blank" rel="noopener">Mở ảnh</a>
                </c:if>
                <c:if test="${review.hasVideo && not empty review.videoUrl}">
                  <a class="admin-btn" href="${review.videoUrl}" target="_blank" rel="noopener">Mở video</a>
                </c:if>
              </div>
            </div>
          </c:if>
        </div>

        <hr style="border:none;border-top:1px solid #eef2f7;margin:22px 0;">

        <form method="post" action="${pageContext.request.contextPath}/admin/reviews" class="admin-form">
          <%@ include file="/jsp/common/csrf.jspf" %>
          <input type="hidden" name="id" value="${review.id}" />

          <div class="admin-field">
            <label for="adminNote">Ghi chú kiểm duyệt</label>
            <textarea id="adminNote" name="adminNote" class="admin-textarea" rows="4" placeholder="Nhập lý do duyệt/từ chối nếu cần..."><c:out value="${review.adminNote}" /></textarea>
          </div>

          <div class="admin-actions">
            <button class="admin-btn admin-btn--ok" type="submit" name="action" value="approve">Duyệt và cấp voucher</button>
            <button class="admin-btn admin-btn--danger" type="submit" name="action" value="reject" onclick="return confirm('Từ chối đánh giá này?');">Từ chối</button>
            <c:choose>
              <c:when test="${review.hidden}">
                <button class="admin-btn" type="submit" name="action" value="unhide">Hiện lại</button>
              </c:when>
              <c:otherwise>
                <button class="admin-btn" type="submit" name="action" value="hide">Ẩn đánh giá</button>
              </c:otherwise>
            </c:choose>
            <button class="admin-btn admin-btn--danger" type="submit" name="action" value="delete" onclick="return confirm('Xóa vĩnh viễn đánh giá này?');">Xóa</button>
          </div>
        </form>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Quản lí bình luận" scope="request"/>
<c:set var="activeMenu" value="reviews" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Quản lí bình luận</h1>
        <p class="admin-subtext">
          Hiển thị nội dung bình luận, mã sản phẩm, ID user, thời gian, trạng thái duyệt và thông tin chi tiết người bình luận.
        </p>
      </div>
    </div>

    <c:if test="${not empty successMessage}">
      <div class="admin-alert admin-alert--success">
        <c:out value="${successMessage}" />
      </div>
    </c:if>

    <c:if test="${not empty errorMessage}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${errorMessage}" />
      </div>
    </c:if>

    <div class="admin-card">
      <div class="admin-card__body">

        <div class="admin-toolbar">
          <form method="get"
                action="${pageContext.request.contextPath}/admin/reviews"
                class="admin-toolbar__form">

            <input type="hidden" name="action" value="list"/>

            <input class="admin-input"
                   type="text"
                   name="keyword"
                   value="${keyword}"
                   placeholder="Tìm nội dung, tên, email, SĐT, ID...">

            <input class="admin-input"
                   type="number"
                   name="rating"
                   min="1"
                   max="5"
                   value="${rating}"
                   placeholder="Số sao 1-5">

            <input class="admin-input"
                   type="number"
                   name="productId"
                   value="${productId}"
                   placeholder="Mã sản phẩm / Product ID">

            <input class="admin-input"
                   type="number"
                   name="authorId"
                   value="${authorId}"
                   placeholder="ID user">

            <select class="admin-input" name="status">
              <option value="" ${empty status ? 'selected' : ''}>Tất cả trạng thái</option>
              <option value="PENDING" ${status == 'PENDING' ? 'selected' : ''}>Chờ duyệt</option>
              <option value="APPROVED" ${status == 'APPROVED' ? 'selected' : ''}>Đã duyệt</option>
              <option value="REJECTED" ${status == 'REJECTED' ? 'selected' : ''}>Từ chối</option>
              <option value="HIDDEN" ${status == 'HIDDEN' ? 'selected' : ''}>Đang ẩn</option>
            </select>

            <select class="admin-input" name="media">
              <option value="" ${empty media ? 'selected' : ''}>Tất cả media</option>
              <option value="IMAGE" ${media == 'IMAGE' ? 'selected' : ''}>Có ảnh</option>
              <option value="VIDEO" ${media == 'VIDEO' ? 'selected' : ''}>Có video</option>
              <option value="MEDIA" ${media == 'MEDIA' ? 'selected' : ''}>Có ảnh/video</option>
            </select>

            <button class="admin-btn" type="submit">Lọc</button>
            <a class="admin-btn" href="${pageContext.request.contextPath}/admin/reviews">Xóa lọc</a>
          </form>
        </div>

        <div style="margin-bottom: 14px;">
          <span class="admin-muted">
            Tổng số bình luận:
            <strong>${empty totalReviews ? 0 : totalReviews}</strong>
          </span>
        </div>

        <c:choose>
          <c:when test="${empty reviews}">
            <div class="admin-empty">Chưa có bình luận phù hợp.</div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
              <tr>
                <th style="width: 80px;">ID</th>
                <th style="width: 165px;">Trạng thái</th>
                <th style="width: 80px;">Sao</th>
                <th style="width: 230px;">Sản phẩm</th>
                <th style="width: 260px;">Người bình luận</th>
                <th>Nội dung bình luận</th>
                <th style="width: 135px;">Media</th>
                <th style="width: 155px;">Thời gian</th>
                <th style="width: 310px;">Thao tác</th>
              </tr>
              </thead>

              <tbody>
              <c:forEach var="r" items="${reviews}">
                <tr>
                  <td>
                    <strong>#${r.id}</strong>
                  </td>

                  <td>
                    <span class="admin-pill ${r.statusCssClass}">
                      <c:out value="${r.statusLabel}" />
                    </span>

                    <c:if test="${r.voucherAwarded}">
                      <div style="margin-top: 6px;">
                        <span class="admin-pill admin-pill--ok">Đã cấp voucher</span>
                      </div>
                    </c:if>
                  </td>

                  <td>
                    <strong>${r.rating}★</strong>
                  </td>

                  <td>
                    <strong>
                      <c:out value="${r.productName}" />
                    </strong>

                    <div class="admin-muted" style="margin-top: 4px;">
                      Mã SP:
                      <strong>
                        <c:out value="${r.productDisplayCode}" />
                      </strong>
                    </div>

                    <div class="admin-muted">
                      Product ID: #${r.productId}
                    </div>
                  </td>

                  <td>
                    <strong>
                      <c:out value="${r.authorDisplayName}" />
                    </strong>

                    <div class="admin-muted" style="margin-top: 4px;">
                      User ID: #${r.authorId}
                    </div>

                    <c:if test="${not empty r.authorEmail}">
                      <div class="admin-muted">
                        Email:
                        <c:out value="${r.authorEmail}" />
                      </div>
                    </c:if>

                    <c:if test="${not empty r.authorPhone}">
                      <div class="admin-muted">
                        SĐT:
                        <c:out value="${r.authorPhone}" />
                      </div>
                    </c:if>

                    <c:if test="${not empty r.authorRole}">
                      <div class="admin-muted">
                        Role:
                        <c:out value="${r.authorRole}" />
                      </div>
                    </c:if>

                    <div class="admin-muted">
                      Rank:
                      <c:out value="${r.authorRankDisplay}" />
                    </div>

                    <div style="margin-top: 6px;">
                      <span class="admin-pill ${r.authorStatusCssClass}">
                        <c:out value="${r.authorStatusLabel}" />
                      </span>
                    </div>
                  </td>

                  <td>
                    <div class="admin-break">
                      <c:choose>
                        <c:when test="${not empty r.comment}">
                          <c:out value="${r.comment}" />
                        </c:when>
                        <c:otherwise>
                          <span class="admin-muted">Không có nội dung.</span>
                        </c:otherwise>
                      </c:choose>
                    </div>

                    <c:if test="${r.hasEmoji}">
                      <div style="margin-top: 6px;">
                        <span class="admin-pill">Có emoji</span>
                      </div>
                    </c:if>

                    <div class="admin-muted" style="margin-top: 6px;">
                      Cảm xúc:
                      <c:out value="${r.sentimentLabel}" />
                    </div>

                    <c:if test="${not empty r.adminNote}">
                      <div class="admin-muted" style="margin-top: 6px;">
                        Ghi chú admin:
                        <c:out value="${r.adminNote}" />
                      </div>
                    </c:if>
                  </td>

                  <td>
                    <c:choose>
                      <c:when test="${r.hasImage or r.hasVideo}">
                        <c:if test="${r.hasImage}">
                          <span class="admin-pill">Ảnh</span>
                        </c:if>

                        <c:if test="${r.hasVideo}">
                          <span class="admin-pill">Video</span>
                        </c:if>

                        <div class="admin-muted" style="margin-top: 6px;">
                          ${r.mediaCount} file
                        </div>
                      </c:when>

                      <c:otherwise>
                        <span class="admin-muted">Không có</span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td class="admin-muted">
                    <fmt:formatDate value="${r.createdAtDate}" pattern="dd/MM/yyyy HH:mm"/>
                  </td>

                  <td class="product-actions">
                    <a class="admin-btn"
                       href="${pageContext.request.contextPath}/admin/reviews?action=detail&id=${r.id}">
                      Xem
                    </a>

                    <c:if test="${not r.approved}">
                      <form method="post"
                            action="${pageContext.request.contextPath}/admin/reviews"
                            class="admin-inline">
                        <%@ include file="/jsp/common/csrf.jspf" %>
                        <input type="hidden" name="action" value="approve"/>
                        <input type="hidden" name="id" value="${r.id}"/>
                        <button class="admin-btn admin-btn--ok" type="submit">
                          Duyệt
                        </button>
                      </form>
                    </c:if>

                    <c:if test="${not r.rejected}">
                      <form method="post"
                            action="${pageContext.request.contextPath}/admin/reviews"
                            class="admin-inline"
                            onsubmit="return confirm('Từ chối bình luận này?');">
                        <%@ include file="/jsp/common/csrf.jspf" %>
                        <input type="hidden" name="action" value="reject"/>
                        <input type="hidden" name="id" value="${r.id}"/>
                        <button class="admin-btn admin-btn--danger" type="submit">
                          Từ chối
                        </button>
                      </form>
                    </c:if>

                    <c:choose>
                      <c:when test="${r.hidden}">
                        <form method="post"
                              action="${pageContext.request.contextPath}/admin/reviews"
                              class="admin-inline">
                          <%@ include file="/jsp/common/csrf.jspf" %>
                          <input type="hidden" name="action" value="unhide"/>
                          <input type="hidden" name="id" value="${r.id}"/>
                          <button class="admin-btn" type="submit">
                            Hiện lại
                          </button>
                        </form>
                      </c:when>

                      <c:otherwise>
                        <form method="post"
                              action="${pageContext.request.contextPath}/admin/reviews"
                              class="admin-inline"
                              onsubmit="return confirm('Ẩn bình luận này khỏi trang khách hàng?');">
                          <%@ include file="/jsp/common/csrf.jspf" %>
                          <input type="hidden" name="action" value="hide"/>
                          <input type="hidden" name="id" value="${r.id}"/>
                          <button class="admin-btn" type="submit">
                            Ẩn
                          </button>
                        </form>
                      </c:otherwise>
                    </c:choose>

                    <form method="post"
                          action="${pageContext.request.contextPath}/admin/reviews"
                          class="admin-inline"
                          onsubmit="return confirm('Xóa vĩnh viễn bình luận này?');">
                      <%@ include file="/jsp/common/csrf.jspf" %>
                      <input type="hidden" name="action" value="delete"/>
                      <input type="hidden" name="id" value="${r.id}"/>
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

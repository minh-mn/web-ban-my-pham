<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Quản lí bình luận" scope="request"/>
<c:set var="activeMenu" value="reviews" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="pendingCount" value="0" />
<c:set var="approvedCount" value="0" />
<c:set var="rejectedCount" value="0" />
<c:set var="hiddenCount" value="0" />
<c:set var="mediaCount" value="0" />

<c:forEach var="r" items="${reviews}">
  <c:if test="${r.pending}">
    <c:set var="pendingCount" value="${pendingCount + 1}" />
  </c:if>
  <c:if test="${r.approved}">
    <c:set var="approvedCount" value="${approvedCount + 1}" />
  </c:if>
  <c:if test="${r.rejected}">
    <c:set var="rejectedCount" value="${rejectedCount + 1}" />
  </c:if>
  <c:if test="${r.hidden}">
    <c:set var="hiddenCount" value="${hiddenCount + 1}" />
  </c:if>
  <c:if test="${r.hasImage or r.hasVideo}">
    <c:set var="mediaCount" value="${mediaCount + 1}" />
  </c:if>
</c:forEach>

<main class="admin-main">
  <div class="admin-container admin-review-page">

    <div class="admin-review-hero">
      <div class="admin-review-hero__content">
        <h1 class="admin-h1 admin-review-title">Quản lí bình luận</h1>
        <p class="admin-subtext admin-review-subtitle">
          Kiểm duyệt bình luận sản phẩm, xem nội dung, mã sản phẩm, ID user, thời gian gửi,
          media đính kèm và thông tin chi tiết người bình luận.
        </p>
      </div>

      <div class="admin-review-hero__actions">
        <a class="admin-btn" href="${pageContext.request.contextPath}/admin/reviews">
          Làm mới
        </a>
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

    <section class="admin-review-stats" aria-label="Thống kê bình luận">
      <div class="admin-review-stat admin-review-stat--total">
        <span class="admin-review-stat__label">Tổng bình luận</span>
        <strong class="admin-review-stat__value">${empty totalReviews ? 0 : totalReviews}</strong>
        <span class="admin-review-stat__note">Kết quả theo bộ lọc hiện tại</span>
      </div>

      <div class="admin-review-stat admin-review-stat--pending">
        <span class="admin-review-stat__label">Chờ duyệt</span>
        <strong class="admin-review-stat__value">${pendingCount}</strong>
        <span class="admin-review-stat__note">Cần admin xử lý</span>
      </div>

      <div class="admin-review-stat admin-review-stat--approved">
        <span class="admin-review-stat__label">Đã duyệt</span>
        <strong class="admin-review-stat__value">${approvedCount}</strong>
        <span class="admin-review-stat__note">Có thể hiển thị với khách</span>
      </div>

      <div class="admin-review-stat admin-review-stat--rejected">
        <span class="admin-review-stat__label">Từ chối / Ẩn</span>
        <strong class="admin-review-stat__value">${rejectedCount + hiddenCount}</strong>
        <span class="admin-review-stat__note">Không hiển thị công khai</span>
      </div>

      <div class="admin-review-stat admin-review-stat--media">
        <span class="admin-review-stat__label">Có media</span>
        <strong class="admin-review-stat__value">${mediaCount}</strong>
        <span class="admin-review-stat__note">Ảnh hoặc video đánh giá</span>
      </div>
    </section>

    <section class="admin-card admin-review-filter-card">
      <div class="admin-card__body">
        <div class="admin-review-section-head">
          <div>
            <h2 class="admin-review-section-title">Bộ lọc bình luận</h2>
            <p class="admin-review-section-desc">
              Lọc nhanh theo nội dung, số sao, sản phẩm, user, trạng thái và media.
            </p>
          </div>
        </div>

        <form method="get"
              action="${pageContext.request.contextPath}/admin/reviews"
              class="admin-review-filter-form">

          <input type="hidden" name="action" value="list"/>

          <div class="admin-review-filter-main">
            <label class="admin-review-filter-field admin-review-filter-field--keyword">
              <span>Tìm kiếm</span>
              <input class="admin-input"
                     type="text"
                     name="keyword"
                     value="${keyword}"
                     placeholder="Nội dung, tên, email, SĐT, ID bình luận...">
            </label>

            <label class="admin-review-filter-field">
              <span>Số sao</span>
              <input class="admin-input"
                     type="number"
                     name="rating"
                     min="1"
                     max="5"
                     value="${rating}"
                     placeholder="1 - 5">
            </label>

            <label class="admin-review-filter-field">
              <span>Sản phẩm</span>
              <input class="admin-input"
                     type="number"
                     name="productId"
                     value="${productId}"
                     placeholder="Product ID">
            </label>

            <label class="admin-review-filter-field">
              <span>User</span>
              <input class="admin-input"
                     type="number"
                     name="authorId"
                     value="${authorId}"
                     placeholder="User ID">
            </label>

            <label class="admin-review-filter-field">
              <span>Trạng thái</span>
              <select class="admin-input" name="status">
                <option value="" ${empty status ? 'selected' : ''}>Tất cả</option>
                <option value="PENDING" ${status == 'PENDING' ? 'selected' : ''}>Chờ duyệt</option>
                <option value="APPROVED" ${status == 'APPROVED' ? 'selected' : ''}>Đã duyệt</option>
                <option value="REJECTED" ${status == 'REJECTED' ? 'selected' : ''}>Từ chối</option>
                <option value="HIDDEN" ${status == 'HIDDEN' ? 'selected' : ''}>Đang ẩn</option>
              </select>
            </label>

            <label class="admin-review-filter-field">
              <span>Media</span>
              <select class="admin-input" name="media">
                <option value="" ${empty media ? 'selected' : ''}>Tất cả</option>
                <option value="IMAGE" ${media == 'IMAGE' ? 'selected' : ''}>Có ảnh</option>
                <option value="VIDEO" ${media == 'VIDEO' ? 'selected' : ''}>Có video</option>
                <option value="MEDIA" ${media == 'MEDIA' ? 'selected' : ''}>Có ảnh/video</option>
              </select>
            </label>
          </div>

          <div class="admin-review-filter-actions">
            <button class="admin-btn admin-review-filter-btn" type="submit">
              Lọc bình luận
            </button>
            <a class="admin-btn admin-review-filter-btn" href="${pageContext.request.contextPath}/admin/reviews">
              Xóa lọc
            </a>
          </div>
        </form>
      </div>
    </section>

    <section class="admin-card admin-review-list-card">
      <div class="admin-card__body">
        <div class="admin-review-section-head admin-review-section-head--list">
          <div>
            <h2 class="admin-review-section-title">Danh sách bình luận</h2>
            <p class="admin-review-section-desc">
              Hiển thị <strong>${empty totalReviews ? 0 : totalReviews}</strong> bình luận phù hợp.
            </p>
          </div>

          <div class="admin-review-active-filters">
            <c:if test="${not empty keyword}">
              <span class="admin-chip">Từ khóa: <c:out value="${keyword}" /></span>
            </c:if>
            <c:if test="${not empty rating}">
              <span class="admin-chip">${rating} sao</span>
            </c:if>
            <c:if test="${not empty productId}">
              <span class="admin-chip">Product #${productId}</span>
            </c:if>
            <c:if test="${not empty authorId}">
              <span class="admin-chip">User #${authorId}</span>
            </c:if>
            <c:if test="${not empty status}">
              <span class="admin-chip">Trạng thái: <c:out value="${status}" /></span>
            </c:if>
            <c:if test="${not empty media}">
              <span class="admin-chip">Media: <c:out value="${media}" /></span>
            </c:if>
          </div>
        </div>

        <c:choose>
          <c:when test="${empty reviews}">
            <div class="admin-review-empty">
              <div class="admin-review-empty__icon">💬</div>
              <div>
                <h3>Không tìm thấy bình luận phù hợp</h3>
                <p>Hãy thử xóa bộ lọc hoặc tìm bằng từ khóa khác.</p>
                <a class="admin-btn" href="${pageContext.request.contextPath}/admin/reviews">Xóa lọc</a>
              </div>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-review-table-wrap">
              <table class="admin-table admin-review-table">
                <thead>
                <tr>
                  <th class="admin-review-col-id">ID</th>
                  <th class="admin-review-col-status">Trạng thái</th>
                  <th class="admin-review-col-rating">Sao</th>
                  <th class="admin-review-col-product">Sản phẩm</th>
                  <th class="admin-review-col-user">Người bình luận</th>
                  <th class="admin-review-col-comment">Nội dung</th>
                  <th class="admin-review-col-media">Media</th>
                  <th class="admin-review-col-time">Thời gian</th>
                  <th class="admin-review-col-actions">Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="r" items="${reviews}">
                  <tr class="admin-review-row ${r.pending ? 'admin-review-row--pending' : ''} ${r.hidden ? 'admin-review-row--hidden' : ''}">
                    <td class="admin-review-id-cell">
                      <strong>#${r.id}</strong>
                    </td>

                    <td>
                      <div class="admin-review-status-stack">
                        <span class="admin-pill ${r.statusCssClass}">
                          <c:out value="${r.statusLabel}" />
                        </span>

                        <c:if test="${r.voucherAwarded}">
                          <span class="admin-pill admin-pill--ok">Đã cấp voucher</span>
                        </c:if>

                        <c:if test="${r.hidden}">
                          <span class="admin-pill admin-pill--warning">Đang ẩn</span>
                        </c:if>
                      </div>
                    </td>

                    <td>
                      <div class="admin-review-rating">
                        <div class="admin-review-stars" aria-label="${r.rating} sao">
                          <c:forEach begin="1" end="5" var="star">
                            <span class="${star <= r.rating ? 'is-on' : ''}">★</span>
                          </c:forEach>
                        </div>
                        <strong>${r.rating}/5</strong>
                      </div>
                    </td>

                    <td>
                      <div class="admin-review-product">
                        <div class="admin-review-product__name">
                          <c:choose>
                            <c:when test="${not empty r.productName}">
                              <c:out value="${r.productName}" />
                            </c:when>
                            <c:otherwise>
                              Sản phẩm #${r.productId}
                            </c:otherwise>
                          </c:choose>
                        </div>

                        <div class="admin-review-meta-row">
                          <span>Mã SP: <strong><c:out value="${r.productDisplayCode}" /></strong></span>
                          <span>Product ID: <strong>#${r.productId}</strong></span>
                        </div>
                      </div>
                    </td>

                    <td>
                      <div class="admin-review-user">
                        <div class="admin-review-user__avatar">#${r.authorId}</div>
                        <div class="admin-review-user__body">
                          <div class="admin-review-user__name">
                            <c:out value="${r.authorDisplayName}" />
                          </div>

                          <div class="admin-review-meta-row admin-review-meta-row--user">
                            <span>User ID: <strong>#${r.authorId}</strong></span>
                            <c:if test="${not empty r.authorRole}">
                              <span>Role: <strong><c:out value="${r.authorRole}" /></strong></span>
                            </c:if>
                            <span>Rank: <strong><c:out value="${r.authorRankDisplay}" /></strong></span>
                          </div>

                          <c:if test="${not empty r.authorEmail or not empty r.authorPhone}">
                            <div class="admin-review-contact-line">
                              <c:if test="${not empty r.authorEmail}">
                                <span><c:out value="${r.authorEmail}" /></span>
                              </c:if>
                              <c:if test="${not empty r.authorPhone}">
                                <span><c:out value="${r.authorPhone}" /></span>
                              </c:if>
                            </div>
                          </c:if>

                          <span class="admin-pill ${r.authorStatusCssClass}">
                            <c:out value="${r.authorStatusLabel}" />
                          </span>
                        </div>
                      </div>
                    </td>

                    <td>
                      <div class="admin-review-comment-preview">
                        <c:choose>
                          <c:when test="${not empty r.comment}">
                            <c:out value="${r.comment}" />
                          </c:when>
                          <c:otherwise>
                            <span class="admin-muted">Không có nội dung.</span>
                          </c:otherwise>
                        </c:choose>
                      </div>

                      <div class="admin-review-comment-meta">
                        <span>Cảm xúc: <strong><c:out value="${r.sentimentLabel}" /></strong></span>
                        <c:if test="${r.hasEmoji}">
                          <span class="admin-chip">Có emoji</span>
                        </c:if>
                      </div>

                      <c:if test="${not empty r.adminNote}">
                        <div class="admin-review-note-preview">
                          Ghi chú admin: <c:out value="${r.adminNote}" />
                        </div>
                      </c:if>
                    </td>

                    <td>
                      <div class="admin-review-media-stack">
                        <c:choose>
                          <c:when test="${r.hasImage or r.hasVideo}">
                            <div class="admin-review-media-badges">
                              <c:if test="${r.hasImage}">
                                <span class="admin-pill">Ảnh</span>
                              </c:if>

                              <c:if test="${r.hasVideo}">
                                <span class="admin-pill">Video</span>
                              </c:if>
                            </div>

                            <span class="admin-review-media-count">${r.mediaCount} file</span>
                          </c:when>

                          <c:otherwise>
                            <span class="admin-review-no-media">Không có</span>
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </td>

                    <td>
                      <div class="admin-review-time">
                        <fmt:formatDate value="${r.createdAtDate}" pattern="dd/MM/yyyy"/>
                        <span><fmt:formatDate value="${r.createdAtDate}" pattern="HH:mm"/></span>
                      </div>
                    </td>

                    <td class="admin-review-action-cell">
                      <div class="admin-review-actions">
                        <a class="admin-btn admin-review-action-btn"
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
                            <button class="admin-btn admin-btn--ok admin-review-action-btn" type="submit">
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
                            <button class="admin-btn admin-btn--danger admin-review-action-btn" type="submit">
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
                              <button class="admin-btn admin-review-action-btn" type="submit">
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
                              <button class="admin-btn admin-review-action-btn" type="submit">
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
                          <button class="admin-btn admin-btn--danger admin-review-action-btn" type="submit">
                            Xóa
                          </button>
                        </form>
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

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

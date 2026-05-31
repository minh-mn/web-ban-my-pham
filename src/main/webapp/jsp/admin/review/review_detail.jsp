<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Chi tiết bình luận" scope="request"/>
<c:set var="activeMenu" value="reviews" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:if test="${not empty review}">
  <c:if test="${not empty review.productImage}">
    <c:set var="productImageUrl" value="${review.productImage}" />
    <c:if test="${fn:startsWith(review.productImage, '/')}">
      <c:set var="productImageUrl" value="${pageContext.request.contextPath}${review.productImage}" />
    </c:if>
    <c:if test="${not fn:startsWith(review.productImage, '/') and not fn:startsWith(review.productImage, 'http')}">
      <c:set var="productImageUrl" value="${pageContext.request.contextPath}/${review.productImage}" />
    </c:if>
  </c:if>

  <c:if test="${not empty review.imageUrl}">
    <c:set var="reviewImageUrl" value="${review.imageUrl}" />
    <c:if test="${fn:startsWith(review.imageUrl, '/')}">
      <c:set var="reviewImageUrl" value="${pageContext.request.contextPath}${review.imageUrl}" />
    </c:if>
    <c:if test="${not fn:startsWith(review.imageUrl, '/') and not fn:startsWith(review.imageUrl, 'http')}">
      <c:set var="reviewImageUrl" value="${pageContext.request.contextPath}/${review.imageUrl}" />
    </c:if>
  </c:if>

  <c:if test="${not empty review.videoUrl}">
    <c:set var="reviewVideoUrl" value="${review.videoUrl}" />
    <c:if test="${fn:startsWith(review.videoUrl, '/')}">
      <c:set var="reviewVideoUrl" value="${pageContext.request.contextPath}${review.videoUrl}" />
    </c:if>
    <c:if test="${not fn:startsWith(review.videoUrl, '/') and not fn:startsWith(review.videoUrl, 'http')}">
      <c:set var="reviewVideoUrl" value="${pageContext.request.contextPath}/${review.videoUrl}" />
    </c:if>
  </c:if>
</c:if>

<main class="admin-main">
  <div class="admin-container review-detail-page">

    <div class="review-topbar">
      <div class="review-title-block">
        <h1 class="review-title">
          Chi tiết bình luận
          <c:if test="${not empty review}">
            #${review.id}
          </c:if>
        </h1>
        <p class="review-subtitle">
          Theo dõi nội dung bình luận, thông tin sản phẩm, người bình luận và trạng thái kiểm duyệt.
        </p>
      </div>

      <div class="review-top-actions">
        <a class="admin-btn" href="${pageContext.request.contextPath}/admin/reviews">
          Quay lại danh sách
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

    <c:choose>
      <c:when test="${empty review}">
        <div class="review-empty-state">
          Không tìm thấy bình luận.
        </div>
      </c:when>

      <c:otherwise>

        <section class="review-summary">

          <div class="review-summary-card">
            <div class="review-summary-card__label">Bình luận</div>
            <div class="review-summary-card__value">
              #${review.id}
              <span class="admin-pill ${review.statusCssClass} review-status-pill">
                <c:out value="${review.statusLabel}" />
              </span>
            </div>
            <div class="review-summary-card__hint">
              <fmt:formatDate value="${review.createdAtDate}" pattern="dd/MM/yyyy HH:mm"/>
            </div>
          </div>

          <div class="review-summary-card">
            <div class="review-summary-card__label">Đánh giá</div>
            <div class="review-summary-card__value">
              <span class="review-rating">
                <c:forEach begin="1" end="5" var="star">
                  <span class="${star <= review.rating ? 'is-on' : ''}">★</span>
                </c:forEach>
              </span>
            </div>
            <div class="review-summary-card__hint">
              ${review.rating}/5 sao · <c:out value="${review.sentimentLabel}" />
            </div>
          </div>

          <div class="review-summary-card">
            <div class="review-summary-card__label">Sản phẩm</div>
            <div class="review-summary-card__value">
              Mã SP: <c:out value="${review.productDisplayCode}" />
            </div>
            <div class="review-summary-card__hint">
              Product ID: #${review.productId}
            </div>
          </div>

          <div class="review-summary-card">
            <div class="review-summary-card__label">Người bình luận</div>
            <div class="review-summary-card__value">
              User ID: #${review.authorId}
            </div>
            <div class="review-summary-card__hint">
              <c:out value="${review.authorDisplayName}" />
            </div>
          </div>

        </section>

        <div class="review-layout">

          <div class="review-main">

            <section class="review-card">
              <div class="review-card__header">
                <div>
                  <h2 class="review-card__title">Nội dung bình luận</h2>
                  <p class="review-card__desc">Nội dung khách hàng gửi kèm đánh giá sản phẩm.</p>
                </div>

                <div class="review-chip-row review-chip-row--compact">
                  <c:if test="${review.hasEmoji}">
                    <span class="admin-pill">Có emoji</span>
                  </c:if>

                  <c:if test="${review.hasImage}">
                    <span class="admin-pill">Ảnh</span>
                  </c:if>

                  <c:if test="${review.hasVideo}">
                    <span class="admin-pill">Video</span>
                  </c:if>
                </div>
              </div>

              <div class="review-card__body">
                <div class="review-comment-box">
                  <c:choose>
                    <c:when test="${not empty review.comment}">
                      <c:out value="${review.comment}" />
                    </c:when>
                    <c:otherwise>
                      <span class="review-muted">Không có nội dung bình luận.</span>
                    </c:otherwise>
                  </c:choose>
                </div>

                <c:if test="${review.hasImage or review.hasVideo}">
                  <div class="review-spacer"></div>

                  <div class="review-media-grid">
                    <c:if test="${review.hasImage}">
                      <div class="review-media-box">
                        <h3 class="review-media-box__title">Ảnh bình luận</h3>

                        <div class="review-media-preview">
                          <c:choose>
                            <c:when test="${not empty reviewImageUrl}">
                              <img src="${reviewImageUrl}" alt="Ảnh bình luận #${review.id}">
                            </c:when>
                            <c:otherwise>
                              <span class="review-muted">Có ảnh nhưng chưa có đường dẫn.</span>
                            </c:otherwise>
                          </c:choose>
                        </div>

                        <c:if test="${not empty reviewImageUrl}">
                          <a class="admin-btn" href="${reviewImageUrl}" target="_blank" rel="noopener">
                            Mở ảnh
                          </a>
                        </c:if>
                      </div>
                    </c:if>

                    <c:if test="${review.hasVideo}">
                      <div class="review-media-box">
                        <h3 class="review-media-box__title">Video bình luận</h3>

                        <div class="review-media-preview">
                          <c:choose>
                            <c:when test="${not empty reviewVideoUrl}">
                              <video controls preload="metadata">
                                <source src="${reviewVideoUrl}">
                                Trình duyệt không hỗ trợ xem video.
                              </video>
                            </c:when>
                            <c:otherwise>
                              <span class="review-muted">Có video nhưng chưa có đường dẫn.</span>
                            </c:otherwise>
                          </c:choose>
                        </div>

                        <c:if test="${not empty reviewVideoUrl}">
                          <a class="admin-btn" href="${reviewVideoUrl}" target="_blank" rel="noopener">
                            Mở video
                          </a>
                        </c:if>
                      </div>
                    </c:if>
                  </div>
                </c:if>
              </div>
            </section>

            <section class="review-card">
              <div class="review-card__header">
                <div>
                  <h2 class="review-card__title">Thông tin sản phẩm</h2>
                  <p class="review-card__desc">Sản phẩm liên quan đến bình luận này.</p>
                </div>
              </div>

              <div class="review-card__body">
                <div class="review-product-box">

                  <div class="review-product-thumb">
                    <c:choose>
                      <c:when test="${not empty productImageUrl}">
                        <img src="${productImageUrl}" alt="Ảnh sản phẩm">
                      </c:when>
                      <c:otherwise>
                        <span class="review-product-thumb__empty">Chưa có ảnh</span>
                      </c:otherwise>
                    </c:choose>
                  </div>

                  <div>
                    <h3 class="review-product-name">
                      <c:choose>
                        <c:when test="${not empty review.productName}">
                          <c:out value="${review.productName}" />
                        </c:when>
                        <c:otherwise>
                          Sản phẩm #${review.productId}
                        </c:otherwise>
                      </c:choose>
                    </h3>

                    <div class="review-info-grid">
                      <div class="review-info-item">
                        <span class="review-info-label">Mã sản phẩm</span>
                        <div class="review-info-value">
                          <c:out value="${review.productDisplayCode}" />
                        </div>
                      </div>

                      <div class="review-info-item">
                        <span class="review-info-label">Product ID</span>
                        <div class="review-info-value">#${review.productId}</div>
                      </div>

                      <div class="review-info-item">
                        <span class="review-info-label">Order ID</span>
                        <div class="review-info-value">
                          <c:choose>
                            <c:when test="${not empty review.orderId}">
                              #${review.orderId}
                            </c:when>
                            <c:otherwise>
                              <span class="review-muted">Không có</span>
                            </c:otherwise>
                          </c:choose>
                        </div>
                      </div>

                      <div class="review-info-item">
                        <span class="review-info-label">Order Item ID</span>
                        <div class="review-info-value">
                          <c:choose>
                            <c:when test="${not empty review.orderItemId}">
                              #${review.orderItemId}
                            </c:when>
                            <c:otherwise>
                              <span class="review-muted">Không có</span>
                            </c:otherwise>
                          </c:choose>
                        </div>
                      </div>

                      <c:if test="${not empty review.productSlug}">
                        <div class="review-info-item review-info-item--full">
                          <span class="review-info-label">Slug</span>
                          <div class="review-info-value">
                            <c:out value="${review.productSlug}" />
                          </div>
                        </div>
                      </c:if>
                    </div>
                  </div>

                </div>
              </div>
            </section>

            <section class="review-card">
              <div class="review-card__header">
                <div>
                  <h2 class="review-card__title">Thông tin người bình luận</h2>
                  <p class="review-card__desc">Thông tin tài khoản đã gửi bình luận.</p>
                </div>

                <span class="admin-pill ${review.authorStatusCssClass}">
                  <c:out value="${review.authorStatusLabel}" />
                </span>
              </div>

              <div class="review-card__body">
                <div class="review-author-card">

                  <div class="review-avatar">
                    #${review.authorId}
                  </div>

                  <div>
                    <h3 class="review-author-name">
                      <c:out value="${review.authorDisplayName}" />
                    </h3>

                    <div class="review-author-meta">
                      User ID: #${review.authorId}
                      <c:if test="${not empty review.authorName}">
                        · Username: <c:out value="${review.authorName}" />
                      </c:if>
                    </div>

                    <div class="review-chip-row">
                      <span class="admin-pill">
                        Rank: <c:out value="${review.authorRankDisplay}" />
                      </span>

                      <c:if test="${not empty review.authorRole}">
                        <span class="admin-pill">
                          Role: <c:out value="${review.authorRole}" />
                        </span>
                      </c:if>
                    </div>
                  </div>

                </div>

                <div class="review-spacer"></div>

                <div class="review-info-grid review-info-grid--3">
                  <div class="review-info-item">
                    <span class="review-info-label">Họ tên</span>
                    <div class="review-info-value">
                      <c:choose>
                        <c:when test="${not empty review.authorFullName}">
                          <c:out value="${review.authorFullName}" />
                        </c:when>
                        <c:otherwise>
                          <span class="review-muted">Chưa cập nhật</span>
                        </c:otherwise>
                      </c:choose>
                    </div>
                  </div>

                  <div class="review-info-item">
                    <span class="review-info-label">Email</span>
                    <div class="review-info-value">
                      <c:choose>
                        <c:when test="${not empty review.authorEmail}">
                          <c:out value="${review.authorEmail}" />
                        </c:when>
                        <c:otherwise>
                          <span class="review-muted">Chưa cập nhật</span>
                        </c:otherwise>
                      </c:choose>
                    </div>
                  </div>

                  <div class="review-info-item">
                    <span class="review-info-label">Số điện thoại</span>
                    <div class="review-info-value">
                      <c:choose>
                        <c:when test="${not empty review.authorPhone}">
                          <c:out value="${review.authorPhone}" />
                        </c:when>
                        <c:otherwise>
                          <span class="review-muted">Chưa cập nhật</span>
                        </c:otherwise>
                      </c:choose>
                    </div>
                  </div>

                  <div class="review-info-item">
                    <span class="review-info-label">Role</span>
                    <div class="review-info-value">
                      <c:choose>
                        <c:when test="${not empty review.authorRole}">
                          <c:out value="${review.authorRole}" />
                        </c:when>
                        <c:otherwise>
                          <span class="review-muted">Không rõ</span>
                        </c:otherwise>
                      </c:choose>
                    </div>
                  </div>

                  <div class="review-info-item">
                    <span class="review-info-label">Rank</span>
                    <div class="review-info-value">
                      <c:out value="${review.authorRankDisplay}" />
                    </div>
                  </div>

                  <div class="review-info-item">
                    <span class="review-info-label">Ngày tạo tài khoản</span>
                    <div class="review-info-value">
                      <c:choose>
                        <c:when test="${not empty review.authorCreatedAtDate}">
                          <fmt:formatDate value="${review.authorCreatedAtDate}" pattern="dd/MM/yyyy HH:mm"/>
                        </c:when>
                        <c:otherwise>
                          <span class="review-muted">Không rõ</span>
                        </c:otherwise>
                      </c:choose>
                    </div>
                  </div>
                </div>
              </div>
            </section>

          </div>

          <aside class="review-side">

            <section class="review-card">
              <div class="review-card__header">
                <div>
                  <h2 class="review-card__title">Kiểm duyệt</h2>
                  <p class="review-card__desc">Duyệt, từ chối, ẩn hoặc xóa bình luận.</p>
                </div>
              </div>

              <div class="review-card__body">
                <div class="review-mini-list">
                  <div class="review-mini-row">
                    <span>Trạng thái</span>
                    <span><c:out value="${review.statusLabel}" /></span>
                  </div>

                  <div class="review-mini-row">
                    <span>Hiển thị</span>
                    <span>
                      <c:choose>
                        <c:when test="${review.hidden}">Đang ẩn</c:when>
                        <c:otherwise>Đang hiển thị</c:otherwise>
                      </c:choose>
                    </span>
                  </div>

                  <div class="review-mini-row">
                    <span>Voucher</span>
                    <span>
                      <c:choose>
                        <c:when test="${review.voucherAwarded}">Đã cấp</c:when>
                        <c:otherwise>Chưa cấp</c:otherwise>
                      </c:choose>
                    </span>
                  </div>

                  <div class="review-mini-row">
                    <span>Media</span>
                    <span>${review.mediaCount} file</span>
                  </div>
                </div>

                <div class="review-spacer"></div>

                <form method="post"
                      action="${pageContext.request.contextPath}/admin/reviews"
                      class="review-action-form">

                  <%@ include file="/jsp/common/csrf.jspf" %>

                  <input type="hidden" name="id" value="${review.id}" />

                  <label for="adminNote" class="review-info-label">
                    Ghi chú kiểm duyệt
                  </label>

                  <textarea id="adminNote"
                            name="adminNote"
                            placeholder="Nhập lý do duyệt, từ chối hoặc ghi chú nội bộ..."><c:out value="${review.adminNote}" /></textarea>

                  <div class="review-action-grid">
                    <c:if test="${not review.approved}">
                      <button class="admin-btn admin-btn--ok"
                              type="submit"
                              name="action"
                              value="approve"
                              onclick="return confirm('Duyệt bình luận này và cấp voucher nếu đủ điều kiện?');">
                        Duyệt bình luận
                      </button>
                    </c:if>

                    <c:if test="${not review.rejected}">
                      <button class="admin-btn admin-btn--danger"
                              type="submit"
                              name="action"
                              value="reject"
                              onclick="return confirm('Từ chối bình luận này?');">
                        Từ chối bình luận
                      </button>
                    </c:if>

                    <c:choose>
                      <c:when test="${review.hidden}">
                        <button class="admin-btn"
                                type="submit"
                                name="action"
                                value="unhide">
                          Hiện lại bình luận
                        </button>
                      </c:when>

                      <c:otherwise>
                        <button class="admin-btn"
                                type="submit"
                                name="action"
                                value="hide"
                                onclick="return confirm('Ẩn bình luận này khỏi trang khách hàng?');">
                          Ẩn bình luận
                        </button>
                      </c:otherwise>
                    </c:choose>

                    <button class="admin-btn admin-btn--danger"
                            type="submit"
                            name="action"
                            value="delete"
                            onclick="return confirm('Xóa vĩnh viễn bình luận này? Thao tác này không thể hoàn tác.');">
                      Xóa vĩnh viễn
                    </button>

                    <a class="admin-btn"
                       href="${pageContext.request.contextPath}/admin/reviews">
                      Quay lại danh sách
                    </a>
                  </div>
                </form>
              </div>
            </section>

            <section class="review-card">
              <div class="review-card__header">
                <div>
                  <h2 class="review-card__title">Mốc thời gian</h2>
                  <p class="review-card__desc">Theo dõi thời điểm gửi và duyệt.</p>
                </div>
              </div>

              <div class="review-card__body">
                <div class="review-timeline">

                  <div class="review-timeline-item">
                    <span class="review-timeline-label">Khách gửi bình luận</span>
                    <fmt:formatDate value="${review.createdAtDate}" pattern="dd/MM/yyyy HH:mm"/>
                  </div>

                  <div class="review-timeline-item">
                    <span class="review-timeline-label">Admin duyệt</span>
                    <c:choose>
                      <c:when test="${not empty review.approvedAtDate}">
                        <fmt:formatDate value="${review.approvedAtDate}" pattern="dd/MM/yyyy HH:mm"/>
                        <br>
                        <span class="review-muted">
                          Admin ID:
                          <c:choose>
                            <c:when test="${not empty review.approvedBy}">
                              #${review.approvedBy}
                            </c:when>
                            <c:otherwise>
                              Không rõ
                            </c:otherwise>
                          </c:choose>
                        </span>
                      </c:when>
                      <c:otherwise>
                        <span class="review-muted">Chưa duyệt</span>
                      </c:otherwise>
                    </c:choose>
                  </div>

                  <div class="review-timeline-item">
                    <span class="review-timeline-label">Ghi chú hiện tại</span>
                    <c:choose>
                      <c:when test="${not empty review.adminNote}">
                        <c:out value="${review.adminNote}" />
                      </c:when>
                      <c:otherwise>
                        <span class="review-muted">Chưa có ghi chú.</span>
                      </c:otherwise>
                    </c:choose>
                  </div>

                </div>
              </div>
            </section>

          </aside>

        </div>

      </c:otherwise>
    </c:choose>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

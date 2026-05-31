<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Chi tiết bình luận" scope="request"/>
<c:set var="activeMenu" value="reviews" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Chi tiết bình luận #${review.id}</h1>
        <p class="admin-subtext">
          Xem nội dung bình luận, mã sản phẩm, ID user, thời gian, thông tin người bình luận và thao tác kiểm duyệt.
        </p>
      </div>

      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/reviews">
        Quay lại danh sách
      </a>
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
        <div class="admin-card">
          <div class="admin-card__body">
            <div class="admin-empty">Không tìm thấy bình luận.</div>
          </div>
        </div>
      </c:when>

      <c:otherwise>

        <div class="admin-card">
          <div class="admin-card__body">

            <h2 class="admin-section-title">Thông tin kiểm duyệt</h2>

            <div class="admin-form-grid">

              <div class="admin-field">
                <label>ID bình luận</label>
                <div>
                  <strong>#${review.id}</strong>
                </div>
              </div>

              <div class="admin-field">
                <label>Trạng thái</label>
                <div>
                  <span class="admin-pill ${review.statusCssClass}">
                    <c:out value="${review.statusLabel}" />
                  </span>
                </div>
              </div>

              <div class="admin-field">
                <label>Voucher đánh giá</label>
                <div>
                  <c:choose>
                    <c:when test="${review.voucherAwarded}">
                      <span class="admin-pill admin-pill--ok">Đã cấp voucher</span>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-pill admin-pill--warning">Chưa cấp</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field">
                <label>Ẩn khỏi khách hàng</label>
                <div>
                  <c:choose>
                    <c:when test="${review.hidden}">
                      <span class="admin-pill admin-pill--warning">Đang ẩn</span>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-pill admin-pill--ok">Đang hiển thị</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field">
                <label>Thời gian gửi</label>
                <div>
                  <fmt:formatDate value="${review.createdAtDate}" pattern="dd/MM/yyyy HH:mm"/>
                </div>
              </div>

              <div class="admin-field">
                <label>Thời gian duyệt</label>
                <div>
                  <c:choose>
                    <c:when test="${not empty review.approvedAtDate}">
                      <fmt:formatDate value="${review.approvedAtDate}" pattern="dd/MM/yyyy HH:mm"/>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Chưa duyệt</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field">
                <label>Admin duyệt</label>
                <div>
                  <c:choose>
                    <c:when test="${not empty review.approvedBy}">
                      Admin ID: #${review.approvedBy}
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Chưa có</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field">
                <label>Cảm xúc hệ thống</label>
                <div>
                  <c:out value="${review.sentimentLabel}" />
                  <c:if test="${review.hasEmoji}">
                    <span class="admin-pill" style="margin-left: 6px;">Có emoji</span>
                  </c:if>
                </div>
              </div>

            </div>

          </div>
        </div>

        <div class="admin-card">
          <div class="admin-card__body">

            <h2 class="admin-section-title">Thông tin sản phẩm</h2>

            <div class="admin-form-grid">

              <div class="admin-field">
                <label>Mã sản phẩm</label>
                <div>
                  <strong>
                    <c:out value="${review.productDisplayCode}" />
                  </strong>
                </div>
              </div>

              <div class="admin-field">
                <label>Product ID</label>
                <div>
                  #${review.productId}
                </div>
              </div>

              <div class="admin-field admin-field--full">
                <label>Tên sản phẩm</label>
                <div>
                  <strong>
                    <c:out value="${review.productName}" />
                  </strong>
                </div>
              </div>

              <c:if test="${not empty review.productSlug}">
                <div class="admin-field">
                  <label>Slug</label>
                  <div>
                    <c:out value="${review.productSlug}" />
                  </div>
                </div>
              </c:if>

              <c:if test="${not empty review.productImage}">
                <div class="admin-field">
                  <label>Ảnh sản phẩm</label>
                  <div>
                    <a class="admin-btn"
                       href="${review.productImage}"
                       target="_blank"
                       rel="noopener">
                      Mở ảnh sản phẩm
                    </a>
                  </div>
                </div>
              </c:if>

              <div class="admin-field">
                <label>Order ID</label>
                <div>
                  <c:choose>
                    <c:when test="${not empty review.orderId}">
                      #${review.orderId}
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Không có</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field">
                <label>Order Item ID</label>
                <div>
                  <c:choose>
                    <c:when test="${not empty review.orderItemId}">
                      #${review.orderItemId}
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Không có</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

            </div>

          </div>
        </div>

        <div class="admin-card">
          <div class="admin-card__body">

            <h2 class="admin-section-title">Thông tin người bình luận</h2>

            <div class="admin-form-grid">

              <div class="admin-field">
                <label>User ID</label>
                <div>
                  <strong>#${review.authorId}</strong>
                </div>
              </div>

              <div class="admin-field">
                <label>Username</label>
                <div>
                  <c:choose>
                    <c:when test="${not empty review.authorName}">
                      <c:out value="${review.authorName}" />
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Không có</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field">
                <label>Họ tên</label>
                <div>
                  <c:choose>
                    <c:when test="${not empty review.authorFullName}">
                      <c:out value="${review.authorFullName}" />
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Chưa cập nhật</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field">
                <label>Tên hiển thị</label>
                <div>
                  <strong>
                    <c:out value="${review.authorDisplayName}" />
                  </strong>
                </div>
              </div>

              <div class="admin-field">
                <label>Email</label>
                <div>
                  <c:choose>
                    <c:when test="${not empty review.authorEmail}">
                      <c:out value="${review.authorEmail}" />
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Chưa cập nhật</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field">
                <label>Số điện thoại</label>
                <div>
                  <c:choose>
                    <c:when test="${not empty review.authorPhone}">
                      <c:out value="${review.authorPhone}" />
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Chưa cập nhật</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field">
                <label>Role</label>
                <div>
                  <c:choose>
                    <c:when test="${not empty review.authorRole}">
                      <c:out value="${review.authorRole}" />
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Không rõ</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field">
                <label>Rank</label>
                <div>
                  <c:out value="${review.authorRankDisplay}" />
                </div>
              </div>

              <div class="admin-field">
                <label>Trạng thái tài khoản</label>
                <div>
                  <span class="admin-pill ${review.authorStatusCssClass}">
                    <c:out value="${review.authorStatusLabel}" />
                  </span>
                </div>
              </div>

              <div class="admin-field">
                <label>Ngày tạo tài khoản</label>
                <div>
                  <c:choose>
                    <c:when test="${not empty review.authorCreatedAtDate}">
                      <fmt:formatDate value="${review.authorCreatedAtDate}" pattern="dd/MM/yyyy HH:mm"/>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Không rõ</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

            </div>

          </div>
        </div>

        <div class="admin-card">
          <div class="admin-card__body">

            <h2 class="admin-section-title">Nội dung bình luận</h2>

            <div class="admin-form-grid">

              <div class="admin-field">
                <label>Số sao</label>
                <div>
                  <strong>${review.rating}★</strong>
                </div>
              </div>

              <div class="admin-field">
                <label>Media</label>
                <div>
                  <c:choose>
                    <c:when test="${review.hasImage or review.hasVideo}">
                      <c:if test="${review.hasImage}">
                        <span class="admin-pill">Ảnh</span>
                      </c:if>

                      <c:if test="${review.hasVideo}">
                        <span class="admin-pill">Video</span>
                      </c:if>

                      <span class="admin-muted" style="margin-left: 6px;">
                        ${review.mediaCount} file
                      </span>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Không có ảnh/video</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field admin-field--full">
                <label>Bình luận</label>
                <div class="admin-help" style="white-space: pre-wrap; line-height: 1.7;">
                  <c:choose>
                    <c:when test="${not empty review.comment}">
                      <c:out value="${review.comment}" />
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Không có nội dung.</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <c:if test="${review.hasImage or review.hasVideo}">
                <div class="admin-field admin-field--full">
                  <label>Hình ảnh / video đính kèm</label>

                  <div style="display:flex; gap:10px; flex-wrap:wrap;">
                    <c:if test="${review.hasImage and not empty review.imageUrl}">
                      <a class="admin-btn"
                         href="${review.imageUrl}"
                         target="_blank"
                         rel="noopener">
                        Mở ảnh bình luận
                      </a>
                    </c:if>

                    <c:if test="${review.hasVideo and not empty review.videoUrl}">
                      <a class="admin-btn"
                         href="${review.videoUrl}"
                         target="_blank"
                         rel="noopener">
                        Mở video bình luận
                      </a>
                    </c:if>
                  </div>
                </div>
              </c:if>

              <div class="admin-field admin-field--full">
                <label>Ghi chú kiểm duyệt hiện tại</label>
                <div class="admin-help" style="white-space: pre-wrap;">
                  <c:choose>
                    <c:when test="${not empty review.adminNote}">
                      <c:out value="${review.adminNote}" />
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Chưa có ghi chú.</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

            </div>

          </div>
        </div>

        <div class="admin-card">
          <div class="admin-card__body">

            <h2 class="admin-section-title">Thao tác kiểm duyệt</h2>

            <form method="post"
                  action="${pageContext.request.contextPath}/admin/reviews"
                  class="admin-form">

              <%@ include file="/jsp/common/csrf.jspf" %>

              <input type="hidden" name="id" value="${review.id}" />

              <div class="admin-field">
                <label for="adminNote">Ghi chú kiểm duyệt</label>
                <textarea id="adminNote"
                          name="adminNote"
                          class="admin-textarea"
                          rows="4"
                          placeholder="Nhập lý do duyệt hoặc từ chối nếu cần..."><c:out value="${review.adminNote}" /></textarea>
              </div>

              <div class="admin-actions">

                <c:if test="${not review.approved}">
                  <button class="admin-btn admin-btn--ok"
                          type="submit"
                          name="action"
                          value="approve"
                          onclick="return confirm('Duyệt bình luận này và cấp voucher nếu đủ điều kiện?');">
                    Duyệt
                  </button>
                </c:if>

                <c:if test="${not review.rejected}">
                  <button class="admin-btn admin-btn--danger"
                          type="submit"
                          name="action"
                          value="reject"
                          onclick="return confirm('Từ chối bình luận này?');">
                    Từ chối
                  </button>
                </c:if>

                <c:choose>
                  <c:when test="${review.hidden}">
                    <button class="admin-btn"
                            type="submit"
                            name="action"
                            value="unhide">
                      Hiện lại
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
                  Xóa
                </button>

                <a class="admin-btn"
                   href="${pageContext.request.contextPath}/admin/reviews">
                  Quay lại
                </a>

              </div>

            </form>

          </div>
        </div>

      </c:otherwise>
    </c:choose>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

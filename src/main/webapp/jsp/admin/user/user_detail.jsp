<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Chi tiết User" scope="request"/>
<c:set var="activeMenu" value="users" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Chi tiết User</h1>
        <p class="admin-subtext">
          Xem thông tin tài khoản, role, trạng thái, rank khách hàng và trạng thái mật khẩu.
        </p>
      </div>

      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/users">
        Quay lại
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:choose>
          <c:when test="${empty user}">
            <div class="admin-empty">Không tìm thấy user.</div>
          </c:when>

          <c:otherwise>

            <div class="admin-grid-2" style="max-width:820px;">

              <div class="admin-field">
                <div class="admin-label">ID</div>
                <div style="font-weight:850;">#${user.id}</div>
              </div>

              <div class="admin-field">
                <div class="admin-label">Username</div>
                <div style="font-weight:850;">
                  <c:out value="${user.username}"/>
                </div>
              </div>

              <div class="admin-field">
                <div class="admin-label">Họ tên</div>
                <div>
                  <c:choose>
                    <c:when test="${not empty user.fullName}">
                      <c:out value="${user.fullName}"/>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Chưa cập nhật</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field">
                <div class="admin-label">Email</div>
                <div>
                  <c:choose>
                    <c:when test="${not empty user.email}">
                      <c:out value="${user.email}"/>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Chưa cập nhật</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field">
                <div class="admin-label">Số điện thoại</div>
                <div>
                  <c:choose>
                    <c:when test="${not empty user.phone}">
                      <c:out value="${user.phone}"/>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Chưa cập nhật</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field">
                <div class="admin-label">Role</div>
                <div>
                  <c:choose>
                    <c:when test="${user.role == 'ADMIN'}">
                      <span class="admin-pill admin-pill--ok">ADMIN</span>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-pill">USER</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field">
                <div class="admin-label">Trạng thái tài khoản</div>
                <div>
                  <c:choose>
                    <c:when test="${user.active}">
                      <span class="admin-pill admin-pill--ok">ACTIVE</span>
                      <div class="admin-help">User có thể đăng nhập và sử dụng hệ thống.</div>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-pill admin-pill--danger">DISABLED</span>
                      <div class="admin-help">User đang bị vô hiệu hóa, không nên cho đăng nhập.</div>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <!-- ================= RANK INFO ================= -->
              <div class="admin-field">
                <div class="admin-label">Rank khách hàng</div>
                <div>
                  <c:choose>
                    <c:when test="${empty user.manualRankCode}">
                      <span class="admin-pill">AUTO</span>
                      <div class="admin-help">
                        Rank được hệ thống tự động tính theo tổng chi tiêu đã thanh toán.
                      </div>
                    </c:when>

                    <c:otherwise>
                      <span class="admin-pill admin-pill--ok">
                        <c:out value="${user.manualRankCode}"/>
                      </span>
                      <div class="admin-help">
                        Rank này do admin chỉ định trực tiếp cho tài khoản.
                      </div>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <!-- ================= PASSWORD INFO ================= -->
              <div class="admin-field">
                <div class="admin-label">Mật khẩu</div>
                <div>
                  <span class="admin-pill">
                    <c:out value="${user.passwordStatusLabel}"/>
                  </span>

                  <span class="admin-muted" style="margin-left:8px;">
                    <c:out value="${user.passwordMasked}"/>
                  </span>

                  <div class="admin-help">
                    Không thể xem mật khẩu gốc vì hệ thống lưu mật khẩu bằng BCrypt hash.
                    Admin chỉ có thể đặt lại mật khẩu mới trong trang sửa user.
                  </div>
                </div>
              </div>

              <div class="admin-field">
                <div class="admin-label">Google Login</div>
                <div>
                  <c:choose>
                    <c:when test="${not empty user.googleId}">
                      <span class="admin-pill admin-pill--ok">Đã liên kết</span>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-pill">Chưa liên kết</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field">
                <div class="admin-label">Facebook Login</div>
                <div>
                  <c:choose>
                    <c:when test="${not empty user.facebookId}">
                      <span class="admin-pill admin-pill--ok">Đã liên kết</span>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-pill">Chưa liên kết</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="admin-field" style="grid-column: 1 / -1;">
                <div class="admin-label">Ngày tạo</div>
                <div>
                  <c:choose>
                    <c:when test="${not empty user.createdAt}">
                      <fmt:formatDate value="${user.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                    </c:when>
                    <c:otherwise>
                      <span class="admin-muted">Không có dữ liệu</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

            </div>

            <hr class="admin-divider"/>

            <div class="admin-actions">
              <a class="admin-btn admin-btn--primary"
                 href="${pageContext.request.contextPath}/admin/users?action=edit&id=${user.id}">
                Sửa
              </a>

              <a class="admin-btn"
                 href="${pageContext.request.contextPath}/admin/users">
                Đóng
              </a>
            </div>

          </c:otherwise>
        </c:choose>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>
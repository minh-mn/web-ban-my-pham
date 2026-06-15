<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Hạng khách hàng" scope="request"/>
<c:set var="activeMenu" value="ranks" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="rankTotal" value="${empty ranks ? 0 : fn:length(ranks)}" />
<c:set var="rankActiveCount" value="0" />
<c:set var="rankInactiveCount" value="0" />
<c:set var="rankProtectedCount" value="0" />

<c:forEach var="rankStat" items="${ranks}">
  <c:choose>
    <c:when test="${rankStat.active}">
      <c:set var="rankActiveCount" value="${rankActiveCount + 1}" />
    </c:when>
    <c:otherwise>
      <c:set var="rankInactiveCount" value="${rankInactiveCount + 1}" />
    </c:otherwise>
  </c:choose>

  <c:if test="${rankStat.code == 'MEMBER'}">
    <c:set var="rankProtectedCount" value="${rankProtectedCount + 1}" />
  </c:if>
</c:forEach>

<main class="admin-main">
  <div class="admin-container admin-rank-page">

    <section class="admin-rank-hero">
      <div class="admin-rank-hero__content">
        <span class="admin-rank-eyebrow">KHÁCH HÀNG &amp; TĂNG TRƯỞNG</span>
        <h1 class="admin-rank-title">Hạng khách hàng</h1>
        <p class="admin-rank-subtitle">
          Quản lý hạng khách hàng, mốc chi tiêu tối thiểu và phần trăm ưu đãi theo rank.
          Các rank đang hoạt động sẽ được dùng để tự động phân hạng khách sau khi mua hàng.
        </p>
      </div>

      <div class="admin-rank-hero__actions">
        <form method="post"
              action="${ctx}/admin/ranks"
              class="admin-inline"
              onsubmit="return confirm('Khởi tạo dữ liệu rank mặc định nếu bảng đang trống?');">
          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="seed">
          <button class="admin-btn" type="submit">
            Khởi tạo mặc định
          </button>
        </form>

        <a class="admin-btn admin-btn--primary"
           href="${ctx}/admin/ranks?action=new">
          + Thêm rank
        </a>
      </div>
    </section>

    <c:if test="${not empty error}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${error}"/>
      </div>
    </c:if>

    <c:if test="${param.msg == 'saved'}">
      <div class="admin-alert admin-alert--ok">
        Đã lưu thông tin rank.
      </div>
    </c:if>

    <c:if test="${param.msg == 'updated'}">
      <div class="admin-alert admin-alert--ok">
        Đã cập nhật rank.
      </div>
    </c:if>

    <c:if test="${param.msg == 'seeded'}">
      <div class="admin-alert admin-alert--ok">
        Đã kiểm tra và khởi tạo dữ liệu rank mặc định.
      </div>
    </c:if>

    <section class="admin-rank-summary">
      <div class="admin-rank-stat admin-rank-stat--total">
        <span class="admin-rank-stat__icon">👑</span>
        <span class="admin-rank-stat__label">Tổng rank</span>
        <strong class="admin-rank-stat__value">
          <c:out value="${rankTotal}" />
        </strong>
        <span class="admin-rank-stat__note">Tất cả hạng khách hàng</span>
      </div>

      <div class="admin-rank-stat admin-rank-stat--active">
        <span class="admin-rank-stat__icon">✅</span>
        <span class="admin-rank-stat__label">Đang hoạt động</span>
        <strong class="admin-rank-stat__value">
          <c:out value="${rankActiveCount}" />
        </strong>
        <span class="admin-rank-stat__note">Được dùng để tính rank</span>
      </div>

      <div class="admin-rank-stat admin-rank-stat--inactive">
        <span class="admin-rank-stat__icon">⏸️</span>
        <span class="admin-rank-stat__label">Tạm tắt</span>
        <strong class="admin-rank-stat__value">
          <c:out value="${rankInactiveCount}" />
        </strong>
        <span class="admin-rank-stat__note">Không tham gia phân hạng</span>
      </div>

      <div class="admin-rank-stat admin-rank-stat--protected">
        <span class="admin-rank-stat__icon">🛡️</span>
        <span class="admin-rank-stat__label">Rank mặc định</span>
        <strong class="admin-rank-stat__value">
          <c:out value="${rankProtectedCount}" />
        </strong>
        <span class="admin-rank-stat__note">MEMBER được bảo vệ</span>
      </div>
    </section>

    <section class="admin-card admin-rank-list-card">
      <div class="admin-card__body">
        <div class="admin-rank-section-head">
          <div>
            <h2 class="admin-rank-section-title">Danh sách hạng khách hàng</h2>
            <p class="admin-rank-section-desc">
              Kiểm soát mã rank, mốc chi tiêu, ưu đãi, class hiển thị và trạng thái sử dụng.
            </p>
          </div>

          <span class="admin-chip admin-chip--brand">
            <c:out value="${rankTotal}" /> rank
          </span>
        </div>

        <c:choose>
          <c:when test="${empty ranks}">
            <div class="admin-rank-empty">
              <div class="admin-rank-empty__icon">👑</div>
              <div>
                <h3>Chưa có dữ liệu rank</h3>
                <p>Bấm <strong>Khởi tạo mặc định</strong> để tạo MEMBER, SILVER, GOLD, DIAMOND và VIP.</p>
              </div>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-rank-table-wrap">
              <table class="admin-table admin-rank-table">
                <thead>
                <tr>
                  <th class="admin-rank-col-id">ID</th>
                  <th class="admin-rank-col-code">Mã rank</th>
                  <th class="admin-rank-col-name">Tên hiển thị</th>
                  <th class="admin-rank-col-spent">Mốc chi tiêu</th>
                  <th class="admin-rank-col-discount">Ưu đãi</th>
                  <th class="admin-rank-col-css">CSS class</th>
                  <th class="admin-rank-col-status">Trạng thái</th>
                  <th class="admin-rank-col-date">Ngày tạo</th>
                  <th class="admin-rank-col-actions">Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="r" items="${ranks}">
                  <tr class="${r.active ? '' : 'admin-rank-row--inactive'} ${r.code == 'MEMBER' ? 'admin-rank-row--protected' : ''}">
                    <td class="admin-rank-id-cell">
                      #<c:out value="${r.id}" />
                    </td>

                    <td>
                      <div class="admin-rank-code-cell">
                        <strong><c:out value="${r.code}"/></strong>

                        <c:if test="${r.code == 'MEMBER'}">
                          <span class="admin-chip admin-chip--brand">Mặc định</span>
                        </c:if>
                      </div>
                    </td>

                    <td>
                      <div class="admin-rank-name-cell">
                        <span class="admin-rank-avatar ${not empty r.cssClass ? r.cssClass : 'rank-member'}">
                          <c:out value="${fn:substring(r.code, 0, 1)}" />
                        </span>
                        <div>
                          <strong><c:out value="${r.name}"/></strong>
                          <small>Hạng khách hàng</small>
                        </div>
                      </div>
                    </td>

                    <td class="admin-rank-money-cell">
                      <strong>
                        <fmt:formatNumber value="${r.minSpent}" type="number" groupingUsed="true"/> ₫
                      </strong>
                      <small>Chi tiêu tối thiểu</small>
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${r.discountPercent > 0}">
                          <span class="admin-pill admin-pill--ok">
                            <c:out value="${r.discountPercent}" />%
                          </span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-rank-no-discount">Không có ưu đãi</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <code class="admin-path admin-rank-css-code">
                        <c:out value="${r.cssClass}"/>
                      </code>
                    </td>

                    <td class="admin-status-cell">
                      <c:choose>
                        <c:when test="${r.active}">
                          <span class="admin-pill admin-pill--ok">Đang hoạt động</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill admin-pill--danger">Tạm tắt</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${not empty r.createdAt}">
                          <span class="admin-rank-date">
                            <fmt:formatDate value="${r.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                          </span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-muted">—</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td class="admin-rank-action-cell">
                      <div class="admin-rank-actions">
                        <a class="admin-btn admin-rank-action-btn"
                           href="${ctx}/admin/ranks?action=edit&id=${r.id}">
                          Sửa
                        </a>

                        <c:choose>
                          <c:when test="${r.code == 'MEMBER'}">
                            <button class="admin-btn admin-rank-action-btn admin-rank-action-btn--disabled"
                                    type="button"
                                    disabled>
                              Bảo vệ
                            </button>
                          </c:when>

                          <c:otherwise>
                            <form method="post"
                                  action="${ctx}/admin/ranks"
                                  class="admin-inline">
                              <%@ include file="/jsp/common/csrf.jspf" %>

                              <input type="hidden" name="action" value="toggle">
                              <input type="hidden" name="id" value="${r.id}">

                              <button class="admin-btn admin-rank-action-btn" type="submit">
                                <c:choose>
                                  <c:when test="${r.active}">Tắt</c:when>
                                  <c:otherwise>Bật</c:otherwise>
                                </c:choose>
                              </button>
                            </form>

                            <form method="post"
                                  action="${ctx}/admin/ranks"
                                  class="admin-inline"
                                  onsubmit="return confirm('Tắt rank ${r.code}? Rank này sẽ không còn được dùng để tính hạng khách hàng.');">
                              <%@ include file="/jsp/common/csrf.jspf" %>

                              <input type="hidden" name="action" value="delete">
                              <input type="hidden" name="id" value="${r.id}">

                              <button class="admin-btn admin-btn--danger admin-rank-action-btn" type="submit">
                                Xóa
                              </button>
                            </form>
                          </c:otherwise>
                        </c:choose>
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

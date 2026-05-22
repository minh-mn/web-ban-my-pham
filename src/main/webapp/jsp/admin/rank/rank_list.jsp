<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | User Ranks" scope="request"/>
<c:set var="activeMenu" value="ranks" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">User Ranks</h1>
        <p class="admin-subtext">
          Quản lý hạng khách hàng, mốc chi tiêu tối thiểu và phần trăm ưu đãi theo rank.
        </p>
      </div>

      <div class="admin-actions">
        <form method="post"
              action="${pageContext.request.contextPath}/admin/ranks"
              class="admin-inline"
              onsubmit="return confirm('Khởi tạo dữ liệu rank mặc định nếu bảng đang trống?');">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="seed">
          <button class="admin-btn" type="submit">
            Khởi tạo mặc định
          </button>
        </form>

        <a class="admin-btn admin-btn--primary"
           href="${pageContext.request.contextPath}/admin/ranks?action=new">
          + Thêm rank
        </a>
      </div>
    </div>

    <c:if test="${not empty error}">
      <div class="admin-alert admin-alert--danger" style="margin-bottom:12px;">
        <c:out value="${error}"/>
      </div>
    </c:if>

    <c:if test="${param.msg == 'saved'}">
      <div class="admin-alert admin-alert--ok" style="margin-bottom:12px;">
        Đã lưu thông tin rank.
      </div>
    </c:if>

    <c:if test="${param.msg == 'updated'}">
      <div class="admin-alert admin-alert--ok" style="margin-bottom:12px;">
        Đã cập nhật rank.
      </div>
    </c:if>

    <c:if test="${param.msg == 'seeded'}">
      <div class="admin-alert admin-alert--ok" style="margin-bottom:12px;">
        Đã kiểm tra và khởi tạo dữ liệu rank mặc định.
      </div>
    </c:if>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:choose>
          <c:when test="${empty ranks}">
            <div class="admin-empty">
              Chưa có dữ liệu rank. Bấm <strong>Khởi tạo mặc định</strong> để tạo MEMBER, SILVER, GOLD, DIAMOND và VIP.
            </div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
              <tr>
                <th style="width:80px;">ID</th>
                <th style="width:150px;">Mã rank</th>
                <th>Tên hiển thị</th>
                <th style="width:180px;">Mốc chi tiêu</th>
                <th style="width:140px;">Ưu đãi</th>
                <th style="width:180px;">CSS class</th>
                <th style="width:130px;">Trạng thái</th>
                <th style="width:170px;">Ngày tạo</th>
                <th style="width:260px;">Thao tác</th>
              </tr>
              </thead>

              <tbody>
              <c:forEach var="r" items="${ranks}">
                <tr>
                  <td>#${r.id}</td>

                  <td>
                    <strong>
                      <c:out value="${r.code}"/>
                    </strong>

                    <c:if test="${r.code == 'MEMBER'}">
                      <div style="margin-top:6px;">
                        <span class="admin-chip">Mặc định</span>
                      </div>
                    </c:if>
                  </td>

                  <td>
                    <c:out value="${r.name}"/>
                  </td>

                  <td>
                    <fmt:formatNumber value="${r.minSpent}" type="number" groupingUsed="true"/> ₫
                  </td>

                  <td>
                    <c:choose>
                      <c:when test="${r.discountPercent > 0}">
                          <span class="admin-pill admin-pill--ok">
                            ${r.discountPercent}%
                          </span>
                      </c:when>
                      <c:otherwise>
                        <span class="admin-muted">Không có ưu đãi</span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td>
                    <code class="admin-path">
                      <c:out value="${r.cssClass}"/>
                    </code>
                  </td>

                  <td class="admin-status-cell">
                    <c:choose>
                      <c:when test="${r.active}">
                        <span class="admin-pill admin-pill--ok">ACTIVE</span>
                      </c:when>
                      <c:otherwise>
                        <span class="admin-pill admin-pill--danger">INACTIVE</span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td>
                    <c:choose>
                      <c:when test="${not empty r.createdAt}">
                          <span class="admin-muted">
                            <fmt:formatDate value="${r.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                          </span>
                      </c:when>
                      <c:otherwise>
                        <span class="admin-muted">—</span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td class="admin-actions">
                    <a class="admin-btn"
                       href="${pageContext.request.contextPath}/admin/ranks?action=edit&id=${r.id}">
                      Sửa
                    </a>

                    <c:choose>
                      <c:when test="${r.code == 'MEMBER'}">
                        <button class="admin-btn"
                                type="button"
                                disabled
                                style="opacity:.55;cursor:not-allowed;">
                          Bảo vệ
                        </button>
                      </c:when>

                      <c:otherwise>
                        <form method="post"
                              action="${pageContext.request.contextPath}/admin/ranks"
                              class="admin-inline">

                          <%@ include file="/jsp/common/csrf.jspf" %>

                          <input type="hidden" name="action" value="toggle">
                          <input type="hidden" name="id" value="${r.id}">

                          <button class="admin-btn" type="submit">
                            <c:choose>
                              <c:when test="${r.active}">Tắt</c:when>
                              <c:otherwise>Bật</c:otherwise>
                            </c:choose>
                          </button>
                        </form>

                        <form method="post"
                              action="${pageContext.request.contextPath}/admin/ranks"
                              class="admin-inline"
                              onsubmit="return confirm('Tắt rank ${r.code}? Rank này sẽ không còn được dùng để tính hạng khách hàng.');">

                          <%@ include file="/jsp/common/csrf.jspf" %>

                          <input type="hidden" name="action" value="delete">
                          <input type="hidden" name="id" value="${r.id}">

                          <button class="admin-btn admin-btn--danger" type="submit">
                            Xóa
                          </button>
                        </form>
                      </c:otherwise>
                    </c:choose>
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
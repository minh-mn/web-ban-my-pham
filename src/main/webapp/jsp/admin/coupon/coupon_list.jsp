<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Mã giảm giá" scope="request"/>
<c:set var="activeMenu" value="coupons" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Mã giảm giá</h1>
        <p class="admin-subtext">
          Quản lý coupon, điều kiện đơn tối thiểu và rank khách hàng được áp dụng.
        </p>
      </div>

      <a class="admin-btn admin-btn--primary"
         href="${pageContext.request.contextPath}/admin/coupons?action=new">
        + Thêm coupon
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <div class="admin-toolbar">
          <form method="get"
                action="${pageContext.request.contextPath}/admin/coupons"
                class="admin-toolbar__form">

            <input type="hidden" name="action" value="list"/>

            <input class="admin-input"
                   type="text"
                   name="q"
                   value="${param.q}"
                   placeholder="Tìm theo mã coupon...">

            <select class="admin-select" name="status">
              <option value="" ${empty param.status ? 'selected' : ''}>Tất cả trạng thái</option>
              <option value="active" ${param.status == 'active' ? 'selected' : ''}>ACTIVE</option>
              <option value="inactive" ${param.status == 'inactive' ? 'selected' : ''}>INACTIVE</option>
            </select>

            <button class="admin-btn" type="submit">Lọc</button>

            <c:if test="${not empty param.q || not empty param.status}">
              <a class="admin-btn" href="${pageContext.request.contextPath}/admin/coupons">Xóa lọc</a>
            </c:if>
          </form>
        </div>

        <c:choose>
          <c:when test="${empty coupons}">
            <div class="admin-empty">Chưa có coupon.</div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
              <tr>
                <th style="width:70px;">ID</th>
                <th style="width:150px;">Mã</th>
                <th style="width:110px;">Giảm (%)</th>
                <th style="width:130px;">Loại</th>
                <th style="width:150px;">Giảm tối đa</th>
                <th style="width:170px;">Đơn tối thiểu</th>
                <th style="width:150px;">Rank áp dụng</th>
                <th style="width:100px;">Đã dùng</th>
                <th style="width:210px;">Hiệu lực</th>
                <th style="width:130px;">Trạng thái</th>
                <th style="width:260px;">Thao tác</th>
              </tr>
              </thead>

              <tbody>
              <c:forEach var="cp" items="${coupons}">
                <tr>
                  <td>#${cp.id}</td>

                  <td>
                    <strong><c:out value="${cp.code}"/></strong>
                  </td>

                  <td>${cp.discountPercent}%</td>

                  <td>
                    <c:choose>
                      <c:when test="${cp.type == 'DISCOUNT'}">
                        <span class="badge badge-blue">DISCOUNT</span>
                      </c:when>

                      <c:when test="${cp.type == 'PERCENT'}">
                        <span class="badge badge-blue">PERCENT</span>
                      </c:when>

                      <c:when test="${cp.type == 'FREESHIP'}">
                        <span class="badge badge-green">FREESHIP</span>
                      </c:when>

                      <c:when test="${cp.type == 'PRODUCT'}">
                        <span class="badge badge-gray">PRODUCT</span>
                      </c:when>

                      <c:otherwise>
                          <span class="badge badge-gray">
                            <c:out value="${empty cp.type ? 'UNKNOWN' : cp.type}"/>
                          </span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td>
                    <c:choose>
                      <c:when test="${not empty cp.maxDiscountAmount}">
                        <fmt:formatNumber value="${cp.maxDiscountAmount}"
                                          type="number"
                                          groupingUsed="true"/> ₫
                      </c:when>
                      <c:otherwise>
                        <span class="admin-muted">Không giới hạn</span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td>
                    <c:choose>
                      <c:when test="${not empty cp.minOrderAmount && cp.minOrderAmount > 0}">
                        <fmt:formatNumber value="${cp.minOrderAmount}"
                                          type="number"
                                          groupingUsed="true"/> ₫
                      </c:when>
                      <c:otherwise>
                        <span class="admin-muted">Không yêu cầu</span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td>
                    <c:choose>
                      <c:when test="${empty cp.minRankCode || cp.minRankCode == 'MEMBER'}">
                        <span class="badge badge-gray">MEMBER+</span>
                      </c:when>

                      <c:when test="${cp.minRankCode == 'SILVER'}">
                        <span class="badge badge-blue">SILVER+</span>
                      </c:when>

                      <c:when test="${cp.minRankCode == 'GOLD'}">
                        <span class="badge badge-green">GOLD+</span>
                      </c:when>

                      <c:when test="${cp.minRankCode == 'DIAMOND'}">
                        <span class="badge badge-blue">DIAMOND+</span>
                      </c:when>

                      <c:when test="${cp.minRankCode == 'VIP'}">
                        <span class="badge badge-green">VIP</span>
                      </c:when>

                      <c:otherwise>
                          <span class="badge badge-gray">
                            <c:out value="${cp.minRankCode}"/>
                          </span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td>
                      ${cp.usedCount}
                    <span class="admin-muted">/ ${cp.maxUses}</span>
                  </td>

                  <td>
                    <c:choose>
                      <c:when test="${not empty cp.startDate || not empty cp.endDate}">
                          <span class="admin-muted">
                            <c:out value="${cp.startDate != null ? cp.startDate : '—'}"/> →
                            <c:out value="${cp.endDate != null ? cp.endDate : '—'}"/>
                          </span>
                      </c:when>

                      <c:otherwise>
                        <span class="admin-muted">Không giới hạn</span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td class="admin-status-cell">
                    <c:choose>
                      <c:when test="${cp.active}">
                        <span class="admin-pill admin-pill--ok">ACTIVE</span>
                      </c:when>
                      <c:otherwise>
                        <span class="admin-pill admin-pill--danger">INACTIVE</span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <td class="admin-actions">
                    <a class="admin-btn"
                       href="${pageContext.request.contextPath}/admin/coupons?action=edit&id=${cp.id}">
                      Sửa
                    </a>

                    <form method="post"
                          action="${pageContext.request.contextPath}/admin/coupons"
                          class="admin-inline">

                      <%@ include file="/jsp/common/csrf.jspf" %>

                      <input type="hidden" name="action" value="toggle">
                      <input type="hidden" name="id" value="${cp.id}">

                      <button class="admin-btn" type="submit">
                        <c:choose>
                          <c:when test="${cp.active}">Tắt</c:when>
                          <c:otherwise>Bật</c:otherwise>
                        </c:choose>
                      </button>
                    </form>

                    <form method="post"
                          action="${pageContext.request.contextPath}/admin/coupons"
                          class="admin-inline"
                          onsubmit="return confirm('Xóa coupon này?')">

                      <%@ include file="/jsp/common/csrf.jspf" %>

                      <input type="hidden" name="action" value="delete">
                      <input type="hidden" name="id" value="${cp.id}">

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
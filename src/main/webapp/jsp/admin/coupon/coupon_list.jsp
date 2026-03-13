<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

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
        <p class="admin-subtext">Quản lý coupon giảm giá theo %.</p>
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
              <option value="" ${empty param.status ? "selected" : ""}>Tất cả trạng thái</option>
              <option value="active" ${param.status == "active" ? "selected" : ""}>ACTIVE</option>
              <option value="inactive" ${param.status == "inactive" ? "selected" : ""}>INACTIVE</option>
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
                  <th style="width:90px;">ID</th>
                  <th>Mã</th>
                  <th style="width:120px;">Giảm (%)</th>
                  <th style="width:170px;">Giảm tối đa</th>
                  <th style="width:110px;">Đã dùng</th>
                  <th style="width:220px;">Hiệu lực</th>
                  <th style="width:140px;">Trạng thái</th>
                  <th style="width:260px;">Thao tác</th>
                </tr>
              </thead>

              <tbody>
                <c:forEach var="cp" items="${coupons}">
                  <tr>
                    <td>#${cp.id}</td>

                    <td><strong><c:out value="${cp.code}"/></strong></td>

                    <td>${cp.discountPercent}%</td>

                    <td>
                      <c:choose>
                        <c:when test="${not empty cp.maxDiscountAmount}">
                          <fmt:formatNumber value="${cp.maxDiscountAmount}" type="number" groupingUsed="true"/> ₫
                        </c:when>
                        <c:otherwise>—</c:otherwise>
                      </c:choose>
                    </td>

                    <td>${cp.usedCount}</td>

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

                        <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
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

                        <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
                        <%@ include file="/jsp/common/csrf.jspf" %>

                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${cp.id}">
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

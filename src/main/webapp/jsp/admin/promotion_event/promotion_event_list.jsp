<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:set var="pageTitle" value="ADMIN | Khuyến mãi" scope="request"/>
<c:set var="activeMenu" value="promotion" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Promotion Events</h1>
        <p class="admin-subtext">Quản lý chương trình khuyến mãi (ALL / CATEGORY / BRAND).</p>
      </div>

      <a class="admin-btn admin-btn--primary"
         href="${pageContext.request.contextPath}/admin/promotion-events?action=new">
        + Tạo event
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:choose>
          <c:when test="${empty events}">
            <div class="admin-empty">Chưa có chương trình khuyến mãi.</div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
                <tr>
                  <th style="width:90px;">ID</th>
                  <th>Tên</th>
                  <th style="width:220px;">Phạm vi</th>
                  <th style="width:240px;">Giảm</th>
                  <th style="width:240px;">Thời gian</th>
                  <th style="width:140px;">Trạng thái</th>
                  <th style="width:260px;">Thao tác</th>
                </tr>
              </thead>

              <tbody>
                <c:forEach var="e" items="${events}">
                  <tr>
                    <td>#${e.id}</td>

                    <td>
                      <div style="font-weight:850;">
                        <c:out value="${e.name}"/>
                      </div>
                    </td>

                    <td>
                      <span class="admin-pill">
                        <c:out value="${e.scope}"/>
                      </span>

                      <c:choose>
                        <c:when test="${e.scope == 'BRAND' && not empty e.brandName}">
                          <div class="admin-muted" style="margin-top:6px;">
                            <c:out value="${e.brandName}"/>
                          </div>
                        </c:when>
                        <c:when test="${e.scope == 'CATEGORY' && not empty e.categoryName}">
                          <div class="admin-muted" style="margin-top:6px;">
                            <c:out value="${e.categoryName}"/>
                          </div>
                        </c:when>
                      </c:choose>
                    </td>

                    <td>
                      <div>
                        <span class="admin-muted"><c:out value="${e.discountType}"/>:</span>
                        <strong><c:out value="${e.discountValue}"/></strong>
                      </div>

                      <c:if test="${not empty e.maxDiscountAmount}">
                        <div class="admin-muted" style="margin-top:6px;">
                          Tối đa:
                          <fmt:formatNumber value="${e.maxDiscountAmount}" type="number" groupingUsed="true"
                                            minFractionDigits="0" maxFractionDigits="0"/> ₫
                        </div>
                      </c:if>
                    </td>

                    <td>
                      <div class="admin-muted">
                        <c:out value="${e.startDate}"/> → <c:out value="${e.endDate}"/>
                      </div>
                    </td>

                    <td class="admin-status-cell">
                      <c:choose>
                        <c:when test="${e.active}">
                          <span class="admin-pill admin-pill--ok">ACTIVE</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill admin-pill--danger">INACTIVE</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td class="admin-actions">
                      <a class="admin-btn"
                         href="${pageContext.request.contextPath}/admin/promotion-events?action=edit&id=${e.id}">
                        Sửa
                      </a>

                      <form method="post"
                            action="${pageContext.request.contextPath}/admin/promotion-events"
                            class="admin-inline">

                        <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
                        <%@ include file="/jsp/common/csrf.jspf" %>

                        <input type="hidden" name="action" value="toggle">
                        <input type="hidden" name="id" value="${e.id}">
                        <button class="admin-btn" type="submit">
                          <c:choose>
                            <c:when test="${e.active}">Tắt</c:when>
                            <c:otherwise>Bật</c:otherwise>
                          </c:choose>
                        </button>
                      </form>

                      <form method="post"
                            action="${pageContext.request.contextPath}/admin/promotion-events"
                            class="admin-inline"
                            onsubmit="return confirm('Xóa event này?');">

                        <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
                        <%@ include file="/jsp/common/csrf.jspf" %>

                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${e.id}">
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

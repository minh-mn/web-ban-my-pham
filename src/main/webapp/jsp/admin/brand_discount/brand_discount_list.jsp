<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="ADMIN | Brand Discounts" scope="request" />
<c:set var="activeMenu" value="brandDiscounts" scope="request" />
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request" />

<jsp:include page="/jsp/admin/layout/header.jsp" />
<jsp:include page="/jsp/admin/layout/sidebar.jsp" />

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Brand Discounts</h1>
        <p class="admin-subtext">Quản lý giảm giá theo thương hiệu.</p>
      </div>

      <a class="admin-btn admin-btn--primary"
         href="${pageContext.request.contextPath}/admin/brand-discounts?action=new">
        + Thêm
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:choose>
          <c:when test="${empty brandDiscounts}">
            <div class="admin-empty">Chưa có brand discount.</div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
                <tr>
                  <th style="width:90px;">ID</th>
                  <th>Brand</th>
                  <th style="width:130px;">Type</th>
                  <th style="width:140px;">Value</th>
                  <th style="width:170px;">Max</th>
                  <th style="width:260px;">Thời gian</th>
                  <th style="width:140px;">Trạng thái</th>
                  <th style="width:260px;">Thao tác</th>
                </tr>
              </thead>

              <tbody>
                <c:forEach var="d" items="${brandDiscounts}">
                  <tr>
                    <td>#${d.id}</td>

                    <td>
                      <c:choose>
                        <c:when test="${not empty d.brandName}">
                          <c:out value="${d.brandName}"/>
                        </c:when>
                        <c:otherwise>
                          Brand #${d.brandId}
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <span class="admin-pill">
                        <c:out value="${d.discountType}"/>
                      </span>
                    </td>

                    <td>
                      <strong><c:out value="${d.discountValue}"/></strong>
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${not empty d.maxDiscountAmount}">
                          <c:out value="${d.maxDiscountAmount}"/>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-muted">—</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td class="admin-break">
                      <c:out value="${d.startDate}"/> → <c:out value="${d.endDate}"/>
                    </td>

                    <td class="admin-status-cell">
                      <c:choose>
                        <c:when test="${d.active}">
                          <span class="admin-pill admin-pill--ok">ACTIVE</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill">INACTIVE</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td class="admin-actions">
                      <a class="admin-btn"
                         href="${pageContext.request.contextPath}/admin/brand-discounts?action=edit&id=${d.id}">
                        Sửa
                      </a>

                      <!-- ✅ TOGGLE (POST → CSRF) -->
                      <form method="post"
                            action="${pageContext.request.contextPath}/admin/brand-discounts"
                            class="admin-inline">

                        <%@ include file="/jsp/common/csrf.jspf" %>

                        <input type="hidden" name="action" value="toggle">
                        <input type="hidden" name="id" value="${d.id}">
                        <button class="admin-btn" type="submit">Bật/Tắt</button>
                      </form>

                      <!-- ✅ DELETE (POST → CSRF) -->
                      <form method="post"
                            action="${pageContext.request.contextPath}/admin/brand-discounts"
                            class="admin-inline"
                            onsubmit="return confirm('Xóa brand discount này?')">

                        <%@ include file="/jsp/common/csrf.jspf" %>

                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${d.id}">
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

<jsp:include page="/jsp/admin/layout/footer.jsp" />

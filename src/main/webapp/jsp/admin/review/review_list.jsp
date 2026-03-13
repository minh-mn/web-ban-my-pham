<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:set var="pageTitle" value="ADMIN | Đánh giá" scope="request"/>
<c:set var="activeMenu" value="reviews" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">Reviews</h1>
        <p class="admin-subtext">Danh sách đánh giá sản phẩm (rating/comment/AI).</p>
      </div>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <!-- TOOLBAR -->
        <div class="admin-toolbar">
          <form method="get"
                action="${pageContext.request.contextPath}/admin/reviews"
                class="admin-toolbar__form">

            <input class="admin-input" type="number" name="rating" min="1" max="5"
                   value="${param.rating}" placeholder="Rating (1-5)">

            <input class="admin-input" type="number" name="productId"
                   value="${param.productId}" placeholder="Product ID">

            <input class="admin-input" type="number" name="authorId"
                   value="${param.authorId}" placeholder="Author ID">

            <button class="admin-btn" type="submit">Lọc</button>

            <c:if test="${not empty param.rating || not empty param.productId || not empty param.authorId}">
              <a class="admin-btn" href="${pageContext.request.contextPath}/admin/reviews">Xóa lọc</a>
            </c:if>
          </form>
        </div>

        <c:choose>
          <c:when test="${empty reviews}">
            <div class="admin-empty">Chưa có đánh giá.</div>
          </c:when>

          <c:otherwise>
            <table class="admin-table">
              <thead>
                <tr>
                  <th style="width:90px;">ID</th>
                  <th style="width:110px;">Product</th>
                  <th style="width:110px;">Author</th>
                  <th style="width:120px;">Rating</th>
                  <th>Bình luận</th>
                  <th style="width:190px;">AI</th>
                  <th style="width:190px;">Thời gian</th>
                  <th style="width:240px;">Thao tác</th>
                </tr>
              </thead>

              <tbody>
                <c:forEach var="r" items="${reviews}">
                  <tr>
                    <td>#${r.id}</td>
                    <td>${r.productId}</td>
                    <td>${r.authorId}</td>
                    <td><strong>${r.rating}★</strong></td>

                    <td>
                      <div class="admin-break">
                        <c:out value="${r.comment}"/>
                      </div>
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${r.sentiment == 1}">
                          <span class="admin-pill admin-pill--ok">POS</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill admin-pill--danger">NEG</span>
                        </c:otherwise>
                      </c:choose>

                      <c:if test="${r.hasEmoji}">
                        <span class="admin-pill" style="margin-left:6px;">EMOJI</span>
                      </c:if>
                    </td>

                    <td class="admin-muted">
                      <fmt:formatDate value="${r.createdAtDate}" pattern="dd/MM/yyyy HH:mm"/>
                    </td>

                    <td class="product-actions">
                      <a class="admin-btn"
                         href="${pageContext.request.contextPath}/admin/reviews?action=detail&id=${r.id}">
                        Xem
                      </a>

                      <form method="post"
                            action="${pageContext.request.contextPath}/admin/reviews"
                            class="admin-inline"
                            onsubmit="return confirm('Xóa review này?');">

                        <!-- ✅ CSRF (STATIC INCLUDE - KHÔNG VỠ UI) -->
                        <%@ include file="/jsp/common/csrf.jspf" %>

                        <input type="hidden" name="action" value="delete"/>
                        <input type="hidden" name="id" value="${r.id}"/>
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

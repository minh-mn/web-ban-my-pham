<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="ADMIN | Phân quyền role" scope="request"/>
<c:set var="activeMenu" value="roles" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <section class="admin-card">
      <div class="admin-card__body">
        <div class="admin-list-head">
          <div>
            <h1 class="admin-h1">Phân quyền role</h1>
            <p class="admin-subtext">
              Tạo role mới và gán quyền theo từng chức năng quản trị: kho, sale, doanh thu, user, khuyến mãi, CMS...
            </p>
          </div>

          <a class="admin-btn admin-btn--primary" href="${pageContext.request.contextPath}/admin/roles?action=create">
            + Tạo role mới
          </a>
        </div>

        <c:if test="${not empty success}">
          <div class="admin-alert admin-alert--success"><c:out value="${success}"/></div>
        </c:if>

        <c:if test="${not empty error}">
          <div class="admin-alert admin-alert--danger"><c:out value="${error}"/></div>
        </c:if>

        <div class="admin-table-wrap">
          <table class="admin-table">
            <thead>
            <tr>
              <th>Mã role</th>
              <th>Tên role</th>
              <th>Trạng thái</th>
              <th>Loại</th>
              <th>Quyền được cấp</th>
              <th>Cập nhật</th>
              <th>Thao tác</th>
            </tr>
            </thead>

            <tbody>
            <c:choose>
              <c:when test="${empty roles}">
                <tr>
                  <td colspan="7">
                    <div class="admin-empty">Chưa có role nào.</div>
                  </td>
                </tr>
              </c:when>

              <c:otherwise>
                <c:forEach var="r" items="${roles}">
                  <tr>
                    <td>
                      <strong><c:out value="${r.code}"/></strong>
                      <c:if test="${not empty r.description}">
                        <div class="admin-muted"><c:out value="${r.description}"/></div>
                      </c:if>
                    </td>

                    <td><c:out value="${r.name}"/></td>

                    <td>
                      <span class="admin-pill ${r.active ? 'admin-pill--ok' : 'admin-pill--danger'}">
                        <c:choose>
                          <c:when test="${r.active}">Đang bật</c:when>
                          <c:otherwise>Đã tắt</c:otherwise>
                        </c:choose>
                      </span>
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${r.systemRole}">
                          <span class="admin-pill admin-pill--warning">Hệ thống</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill admin-pill--info">Tự tạo</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <div class="admin-chip-row">
                        <c:choose>
                          <c:when test="${empty r.permissionCodes}">
                            <span class="admin-muted">Chưa cấp quyền admin</span>
                          </c:when>
                          <c:otherwise>
                            <c:forEach var="p" items="${r.permissionCodes}">
                              <span class="admin-chip"><c:out value="${p}"/></span>
                            </c:forEach>
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${not empty r.updatedAt}">
                          <fmt:formatDate value="${r.updatedAt}" pattern="dd/MM/yyyy HH:mm"/>
                        </c:when>
                        <c:otherwise>Không rõ</c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <div class="admin-actions">
                        <a class="admin-btn" href="${pageContext.request.contextPath}/admin/roles?action=edit&code=${r.code}">
                          Sửa quyền
                        </a>

                        <form method="post"
                              action="${pageContext.request.contextPath}/admin/roles"
                              class="admin-inline"
                              onsubmit="return confirm('Tắt role này? User đang dùng role này sẽ không còn quyền admin cho tới khi đổi role khác.');">
                          <%@ include file="/jsp/common/csrf.jspf" %>
                          <input type="hidden" name="action" value="deactivate"/>
                          <input type="hidden" name="code" value="${r.code}"/>

                          <button class="admin-btn admin-btn--danger"
                                  type="submit"
                            ${r.systemRole ? 'disabled' : ''}>
                            Tắt
                          </button>
                        </form>
                      </div>
                    </td>
                  </tr>
                </c:forEach>
              </c:otherwise>
            </c:choose>
            </tbody>
          </table>
        </div>
      </div>
    </section>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

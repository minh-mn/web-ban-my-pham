<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Phân quyền role" scope="request"/>
<c:set var="activeMenu" value="roles" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="roleTotal" value="${empty roles ? 0 : fn:length(roles)}" />
<c:set var="roleActiveCount" value="0" />
<c:set var="roleInactiveCount" value="0" />
<c:set var="roleSystemCount" value="0" />

<c:forEach var="roleStat" items="${roles}">
  <c:choose>
    <c:when test="${roleStat.active}">
      <c:set var="roleActiveCount" value="${roleActiveCount + 1}" />
    </c:when>
    <c:otherwise>
      <c:set var="roleInactiveCount" value="${roleInactiveCount + 1}" />
    </c:otherwise>
  </c:choose>

  <c:if test="${roleStat.systemRole}">
    <c:set var="roleSystemCount" value="${roleSystemCount + 1}" />
  </c:if>
</c:forEach>

<main class="admin-main">
  <div class="admin-container admin-role-page">

    <section class="admin-role-hero">
      <div class="admin-role-hero__content">
        <span class="admin-role-eyebrow">HỆ THỐNG &amp; PHÂN QUYỀN</span>
        <h1 class="admin-role-title">Phân quyền role</h1>
        <p class="admin-role-subtitle">
          Tạo role mới và gán quyền theo từng chức năng quản trị: kho, sale, doanh thu, user,
          khuyến mãi, CMS và các module vận hành khác trong Admin Center.
        </p>
      </div>

      <div class="admin-role-hero__actions">
        <a class="admin-btn admin-btn--primary"
           href="${ctx}/admin/roles?action=create">
          + Tạo role mới
        </a>
      </div>
    </section>

    <c:if test="${not empty success}">
      <div class="admin-alert admin-alert--success">
        <c:out value="${success}"/>
      </div>
    </c:if>

    <c:if test="${not empty error}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${error}"/>
      </div>
    </c:if>

    <section class="admin-role-summary">
      <div class="admin-role-stat admin-role-stat--total">
        <span class="admin-role-stat__icon">🛡️</span>
        <span class="admin-role-stat__label">Tổng role</span>
        <strong class="admin-role-stat__value">
          <c:out value="${roleTotal}" />
        </strong>
        <span class="admin-role-stat__note">Tất cả role trong hệ thống</span>
      </div>

      <div class="admin-role-stat admin-role-stat--active">
        <span class="admin-role-stat__icon">✅</span>
        <span class="admin-role-stat__label">Đang bật</span>
        <strong class="admin-role-stat__value">
          <c:out value="${roleActiveCount}" />
        </strong>
        <span class="admin-role-stat__note">Có thể gán cho tài khoản</span>
      </div>

      <div class="admin-role-stat admin-role-stat--inactive">
        <span class="admin-role-stat__icon">⏸️</span>
        <span class="admin-role-stat__label">Đã tắt</span>
        <strong class="admin-role-stat__value">
          <c:out value="${roleInactiveCount}" />
        </strong>
        <span class="admin-role-stat__note">Không còn cấp quyền admin</span>
      </div>

      <div class="admin-role-stat admin-role-stat--system">
        <span class="admin-role-stat__icon">🔒</span>
        <span class="admin-role-stat__label">Role hệ thống</span>
        <strong class="admin-role-stat__value">
          <c:out value="${roleSystemCount}" />
        </strong>
        <span class="admin-role-stat__note">Không cho tắt để đảm bảo an toàn</span>
      </div>
    </section>

    <section class="admin-card admin-role-list-card">
      <div class="admin-card__body">
        <div class="admin-role-section-head">
          <div>
            <h2 class="admin-role-section-title">Danh sách role</h2>
            <p class="admin-role-section-desc">
              Quản lý trạng thái role và xem nhanh các quyền đang được cấp cho từng nhóm người dùng admin.
            </p>
          </div>

          <span class="admin-chip admin-chip--brand">
            <c:out value="${roleTotal}" /> role
          </span>
        </div>

        <c:choose>
          <c:when test="${empty roles}">
            <div class="admin-role-empty">
              <div class="admin-role-empty__icon">🛡️</div>
              <div>
                <h3>Chưa có role nào</h3>
                <p>Tạo role đầu tiên để phân quyền quản trị theo từng nhóm chức năng.</p>
                <a class="admin-btn admin-btn--primary"
                   href="${ctx}/admin/roles?action=create">
                  + Tạo role mới
                </a>
              </div>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-role-table-wrap">
              <table class="admin-table admin-role-table">
                <thead>
                <tr>
                  <th class="admin-role-col-code">Mã role</th>
                  <th class="admin-role-col-name">Tên role</th>
                  <th class="admin-role-col-status">Trạng thái</th>
                  <th class="admin-role-col-type">Loại</th>
                  <th class="admin-role-col-permissions">Quyền được cấp</th>
                  <th class="admin-role-col-updated">Cập nhật</th>
                  <th class="admin-role-col-actions">Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="r" items="${roles}">
                  <tr class="${!r.active ? 'admin-role-row--inactive' : ''} ${r.systemRole ? 'admin-role-row--system' : ''}">
                    <td>
                      <div class="admin-role-code-cell">
                        <strong><c:out value="${r.code}"/></strong>
                        <c:if test="${not empty r.description}">
                          <span><c:out value="${r.description}"/></span>
                        </c:if>
                      </div>
                    </td>

                    <td>
                      <div class="admin-role-name-cell">
                        <strong><c:out value="${r.name}"/></strong>
                        <c:choose>
                          <c:when test="${empty r.permissionCodes}">
                            <small>Chưa cấp quyền admin</small>
                          </c:when>
                          <c:otherwise>
                            <small><c:out value="${fn:length(r.permissionCodes)}" /> quyền</small>
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${r.active}">
                          <span class="admin-pill admin-pill--ok">Đang bật</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill admin-pill--danger">Đã tắt</span>
                        </c:otherwise>
                      </c:choose>
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
                      <div class="admin-role-chip-row">
                        <c:choose>
                          <c:when test="${empty r.permissionCodes}">
                            <span class="admin-role-muted-chip">Chưa cấp quyền</span>
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
                      <div class="admin-role-time-cell">
                        <c:choose>
                          <c:when test="${not empty r.updatedAt}">
                            <fmt:formatDate value="${r.updatedAt}" pattern="dd/MM/yyyy"/>
                            <span><fmt:formatDate value="${r.updatedAt}" pattern="HH:mm"/></span>
                          </c:when>
                          <c:otherwise>
                            <span>Không rõ</span>
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </td>

                    <td class="admin-role-action-cell">
                      <div class="admin-role-actions">
                        <a class="admin-btn admin-role-action-btn"
                           href="${ctx}/admin/roles?action=edit&code=${r.code}">
                          Sửa quyền
                        </a>

                        <form method="post"
                              action="${ctx}/admin/roles"
                              class="admin-inline"
                              onsubmit="return confirm('Tắt role này? User đang dùng role này sẽ không còn quyền admin cho tới khi đổi role khác.');">
                          <%@ include file="/jsp/common/csrf.jspf" %>
                          <input type="hidden" name="action" value="deactivate"/>
                          <input type="hidden" name="code" value="${r.code}"/>

                          <button class="admin-btn admin-btn--danger admin-role-action-btn"
                                  type="submit"
                            ${r.systemRole ? 'disabled' : ''}>
                            Tắt
                          </button>
                        </form>
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

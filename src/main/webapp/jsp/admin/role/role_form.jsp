<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Form role" scope="request"/>
<c:set var="activeMenu" value="roles" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="permissionTotal" value="${empty permissions ? 0 : fn:length(permissions)}" />
<c:set var="selectedPermissionTotal" value="0" />
<c:forEach var="permissionStat" items="${permissions}">
  <c:if test="${role.hasPermission(permissionStat.code)}">
    <c:set var="selectedPermissionTotal" value="${selectedPermissionTotal + 1}" />
  </c:if>
</c:forEach>

<main class="admin-main">
  <div class="admin-container admin-role-form-page">

    <section class="admin-role-form-hero">
      <div class="admin-role-form-hero__content">
        <span class="admin-role-form-eyebrow">HỆ THỐNG &amp; PHÂN QUYỀN</span>
        <h1 class="admin-role-form-title">
          <c:choose>
            <c:when test="${mode == 'create'}">Tạo role mới</c:when>
            <c:otherwise>Sửa phân quyền role</c:otherwise>
          </c:choose>
        </h1>
        <p class="admin-role-form-subtitle">
          Chọn quyền nào thì role đó chỉ được nhìn thấy và truy cập đúng chức năng tương ứng trong Admin Center.
          Role hệ thống sẽ được bảo vệ để tránh mất quyền quản trị quan trọng.
        </p>
      </div>

      <div class="admin-role-form-hero__actions">
        <a class="admin-btn"
           href="${ctx}/admin/roles">
          ← Quay lại danh sách
        </a>
      </div>
    </section>

    <c:if test="${not empty error}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${error}"/>
      </div>
    </c:if>

    <form method="post"
          action="${ctx}/admin/roles"
          class="admin-form admin-role-form">
      <%@ include file="/jsp/common/csrf.jspf" %>

      <input type="hidden" name="action" value="${mode == 'create' ? 'create' : 'update'}"/>

      <div class="admin-role-form-layout">

        <section class="admin-card admin-role-form-card">
          <div class="admin-card__body">
            <div class="admin-role-form-section-head">
              <div>
                <h2 class="admin-role-form-section-title">Thông tin role</h2>
                <p class="admin-role-form-section-desc">
                  Thiết lập mã role, tên hiển thị, mô tả và trạng thái sử dụng.
                </p>
              </div>

              <c:choose>
                <c:when test="${mode == 'create'}">
                  <span class="admin-chip admin-chip--brand">Role mới</span>
                </c:when>
                <c:when test="${role.systemRole}">
                  <span class="admin-chip admin-chip--warning">Role hệ thống</span>
                </c:when>
                <c:when test="${role.active}">
                  <span class="admin-chip admin-chip--success">Đang bật</span>
                </c:when>
                <c:otherwise>
                  <span class="admin-chip admin-chip--danger">Đã tắt</span>
                </c:otherwise>
              </c:choose>
            </div>

            <div class="admin-role-form-grid">
              <label class="admin-field">
                <span class="admin-label">Mã role</span>
                <input class="admin-input"
                       type="text"
                       name="code"
                       value="${role.code}"
                       placeholder="VD: INVENTORY_STAFF"
                       pattern="[A-Za-z0-9_]+"
                       maxlength="50"
                  ${mode == 'edit' ? 'readonly' : ''}
                       required/>
                <span class="admin-help">
                  Chỉ dùng chữ, số và dấu gạch dưới. Hệ thống sẽ tự chuyển thành chữ in hoa.
                </span>
              </label>

              <label class="admin-field">
                <span class="admin-label">Tên role</span>
                <input class="admin-input"
                       type="text"
                       name="name"
                       value="${role.name}"
                       placeholder="VD: Nhân viên kho"
                       maxlength="100"
                       required/>
                <span class="admin-help">
                  Tên role nên thể hiện đúng phạm vi công việc của nhóm tài khoản.
                </span>
              </label>

              <label class="admin-field admin-field--full">
                <span class="admin-label">Mô tả</span>
                <textarea class="admin-textarea"
                          name="description"
                          rows="3"
                          maxlength="255"
                          placeholder="Mô tả phạm vi công việc của role..."><c:out value="${role.description}"/></textarea>
              </label>

              <label class="admin-field">
                <span class="admin-label">Trạng thái</span>
                <select class="admin-select" name="active" ${role.systemRole ? 'disabled' : ''}>
                  <option value="1" ${role.active ? 'selected' : ''}>Đang bật</option>
                  <option value="0" ${!role.active ? 'selected' : ''}>Đã tắt</option>
                </select>
                <c:if test="${role.systemRole}">
                  <input type="hidden" name="active" value="1"/>
                </c:if>
                <span class="admin-help">
                  Role hệ thống như ADMIN/USER luôn được giữ an toàn.
                </span>
              </label>
            </div>
          </div>
        </section>

        <aside class="admin-card admin-role-guide-card">
          <div class="admin-card__body">
            <div class="admin-role-form-section-head">
              <div>
                <h2 class="admin-role-form-section-title">Tóm tắt phân quyền</h2>
                <p class="admin-role-form-section-desc">
                  Kiểm tra nhanh số quyền đang chọn trước khi lưu role.
                </p>
              </div>
            </div>

            <div class="admin-role-preview">
              <div class="admin-role-preview__avatar">
                <c:choose>
                  <c:when test="${mode == 'create'}">NEW</c:when>
                  <c:otherwise><c:out value="${role.code}" /></c:otherwise>
                </c:choose>
              </div>
              <div class="admin-role-preview__body">
                <strong>
                  <c:choose>
                    <c:when test="${not empty role.name}">
                      <c:out value="${role.name}" />
                    </c:when>
                    <c:otherwise>Role chưa đặt tên</c:otherwise>
                  </c:choose>
                </strong>
                <span>
                  <c:out value="${selectedPermissionTotal}" /> / <c:out value="${permissionTotal}" /> quyền được chọn
                </span>
              </div>
            </div>

            <div class="admin-role-guide-list">
              <div>
                <span>Kho</span>
                <strong>INVENTORY_MANAGE</strong>
              </div>
              <div>
                <span>Sale / đơn hàng</span>
                <strong>ORDER_MANAGE, RETURN_MANAGE</strong>
              </div>
              <div>
                <span>Doanh thu</span>
                <strong>REVENUE_VIEW</strong>
              </div>
            </div>

            <c:if test="${role.systemRole}">
              <div class="admin-role-system-note">
                <span>🔒</span>
                <div>
                  <strong>Role hệ thống</strong>
                  <small>Role này được bảo vệ, không nên tắt hoặc thu hẹp quyền quan trọng.</small>
                </div>
              </div>
            </c:if>
          </div>
        </aside>

      </div>

      <section class="admin-card admin-role-permission-card">
        <div class="admin-card__body">
          <div class="admin-role-form-section-head">
            <div>
              <h2 class="admin-role-form-section-title">Danh sách quyền</h2>
              <p class="admin-role-form-section-desc">
                Với role nhân viên kho: chọn <strong>INVENTORY_MANAGE</strong>.
                Với nhân viên sale: chọn <strong>ORDER_MANAGE</strong>, <strong>RETURN_MANAGE</strong>.
                Với admin xem doanh thu: chọn <strong>REVENUE_VIEW</strong>.
              </p>
            </div>
            <span class="admin-chip admin-chip--brand">
              <c:out value="${permissionTotal}" /> quyền
            </span>
          </div>

          <c:choose>
            <c:when test="${empty permissions}">
              <div class="admin-role-form-empty">
                <span>🧩</span>
                <strong>Chưa có quyền nào</strong>
                <small>Hãy seed hoặc cấu hình danh sách permission trước khi tạo role.</small>
              </div>
            </c:when>

            <c:otherwise>
              <div class="admin-role-permission-grid">
                <c:forEach var="p" items="${permissions}">
                  <label class="admin-role-check-card">
                    <input type="checkbox"
                           name="permissions"
                           value="${p.code}"
                      ${role.hasPermission(p.code) ? 'checked' : ''}/>
                    <span class="admin-role-check-card__box"></span>
                    <span class="admin-role-check-card__content">
                      <strong><c:out value="${p.code}"/></strong>
                      <small>
                        <c:out value="${p.name}"/>
                        <c:if test="${not empty p.module}"> · <c:out value="${p.module}"/></c:if>
                      </small>
                      <c:if test="${not empty p.description}">
                        <em><c:out value="${p.description}"/></em>
                      </c:if>
                    </span>
                  </label>
                </c:forEach>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </section>

      <div class="admin-role-form-actions">
        <a class="admin-btn" href="${ctx}/admin/roles">Hủy</a>
        <button class="admin-btn admin-btn--primary" type="submit">
          Lưu role
        </button>
      </div>
    </form>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

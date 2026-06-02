<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Form role" scope="request"/>
<c:set var="activeMenu" value="roles" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
    <div class="admin-container">

        <section class="admin-card">
            <div class="admin-card__body">
                <div class="admin-form-head">
                    <div>
                        <h1 class="admin-h1">
                            <c:choose>
                                <c:when test="${mode == 'create'}">Tạo role mới</c:when>
                                <c:otherwise>Sửa phân quyền role</c:otherwise>
                            </c:choose>
                        </h1>
                        <p class="admin-subtext">
                            Chọn quyền nào thì role đó chỉ được nhìn thấy và truy cập đúng chức năng tương ứng trong Admin Center.
                        </p>
                    </div>

                    <a class="admin-btn" href="${pageContext.request.contextPath}/admin/roles">
                        Quay lại
                    </a>
                </div>

                <c:if test="${not empty error}">
                    <div class="admin-alert admin-alert--danger"><c:out value="${error}"/></div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/admin/roles" class="admin-form">
                    <%@ include file="/jsp/common/csrf.jspf" %>

                    <input type="hidden" name="action" value="${mode == 'create' ? 'create' : 'update'}"/>

                    <div class="admin-form-grid">
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
                            <span class="admin-help">Chỉ dùng chữ, số và dấu gạch dưới. Hệ thống sẽ tự chuyển thành chữ in hoa.</span>
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
                                <option value="1" ${role.active ? 'selected' : ''}>Bật</option>
                                <option value="0" ${!role.active ? 'selected' : ''}>Tắt</option>
                            </select>
                            <c:if test="${role.systemRole}">
                                <input type="hidden" name="active" value="1"/>
                            </c:if>
                            <span class="admin-help">Role hệ thống như ADMIN/USER luôn được giữ an toàn.</span>
                        </label>
                    </div>

                    <section class="admin-card admin-card--soft">
                        <div class="admin-card__body">
                            <h2 class="admin-h2">Danh sách quyền</h2>
                            <p class="admin-subtext">
                                Với role nhân viên kho: chọn <strong>INVENTORY_MANAGE</strong>. Với nhân viên sale: chọn <strong>ORDER_MANAGE</strong>, <strong>RETURN_MANAGE</strong>. Với admin xem doanh thu: chọn <strong>REVENUE_VIEW</strong>.
                            </p>

                            <div class="admin-form-grid">
                                <c:forEach var="p" items="${permissions}">
                                    <label class="admin-check-card">
                                        <input type="checkbox"
                                               name="permissions"
                                               value="${p.code}"
                                            ${role.hasPermission(p.code) ? 'checked' : ''}/>
                                        <span>
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
                        </div>
                    </section>

                    <div class="admin-form-actions">
                        <a class="admin-btn" href="${pageContext.request.contextPath}/admin/roles">Hủy</a>
                        <button class="admin-btn admin-btn--primary" type="submit">
                            Lưu role
                        </button>
                    </div>
                </form>
            </div>
        </section>

    </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

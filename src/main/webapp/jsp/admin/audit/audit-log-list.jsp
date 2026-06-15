<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Nhật ký hệ thống" scope="request"/>
<c:set var="activeMenu" value="auditLogs" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
    <div class="admin-container admin-audit-page">

        <jsp:include page="/jsp/admin/layout/topbar.jsp"/>

        <section class="admin-audit-hero">
            <div class="admin-audit-hero__content">
                <span class="admin-audit-eyebrow">HỆ THỐNG &amp; PHÂN QUYỀN</span>
                <h1 class="admin-audit-title">Nhật ký hệ thống</h1>
                <p class="admin-audit-subtitle">
                    Theo dõi ai đã sửa sản phẩm, đổi trạng thái đơn hàng, nhập kho hoặc thay đổi dữ liệu nhạy cảm.
                    Trang này giúp admin truy vết thao tác và kiểm soát thay đổi trong hệ thống.
                </p>
            </div>

            <div class="admin-audit-hero__actions">
                <span class="admin-chip admin-chip--brand">
                    🧾 <c:out value="${empty totalRows ? 0 : totalRows}"/> log
                </span>
            </div>
        </section>

        <section class="admin-audit-summary">
            <div class="admin-audit-stat admin-audit-stat--total">
                <span class="admin-audit-stat__icon">🧾</span>
                <span class="admin-audit-stat__label">Tổng nhật ký</span>
                <strong class="admin-audit-stat__value">
                    <c:out value="${empty totalRows ? 0 : totalRows}"/>
                </strong>
                <span class="admin-audit-stat__note">Theo bộ lọc hiện tại</span>
            </div>

            <div class="admin-audit-stat admin-audit-stat--module">
                <span class="admin-audit-stat__icon">📦</span>
                <span class="admin-audit-stat__label">Module</span>
                <strong class="admin-audit-stat__value admin-audit-stat__value--text">
                    <c:out value="${empty filter.module ? 'Tất cả' : filter.module}"/>
                </strong>
                <span class="admin-audit-stat__note">Khu vực đang theo dõi</span>
            </div>

            <div class="admin-audit-stat admin-audit-stat--action">
                <span class="admin-audit-stat__icon">⚙️</span>
                <span class="admin-audit-stat__label">Thao tác</span>
                <strong class="admin-audit-stat__value admin-audit-stat__value--text">
                    <c:out value="${empty filter.actionType ? 'Tất cả' : filter.actionType}"/>
                </strong>
                <span class="admin-audit-stat__note">Loại hành động</span>
            </div>

            <div class="admin-audit-stat admin-audit-stat--page">
                <span class="admin-audit-stat__icon">📄</span>
                <span class="admin-audit-stat__label">Hiển thị</span>
                <strong class="admin-audit-stat__value">
                    <c:out value="${empty pageSize ? 20 : pageSize}"/>
                </strong>
                <span class="admin-audit-stat__note">Dòng mỗi trang</span>
            </div>
        </section>

        <section class="admin-card admin-audit-filter-card">
            <div class="admin-card__body">
                <div class="admin-audit-section-head">
                    <div>
                        <h2 class="admin-audit-section-title">Bộ lọc nhật ký</h2>
                        <p class="admin-audit-section-desc">
                            Lọc theo từ khóa, người thao tác, module, loại thao tác và khoảng thời gian.
                        </p>
                    </div>

                    <div class="admin-audit-active-filters">
                        <c:if test="${not empty filter.keyword}">
                            <span class="admin-chip">Từ khóa: <strong><c:out value="${filter.keyword}"/></strong></span>
                        </c:if>
                        <c:if test="${not empty filter.actor}">
                            <span class="admin-chip">Admin: <strong><c:out value="${filter.actor}"/></strong></span>
                        </c:if>
                        <c:if test="${not empty filter.module}">
                            <span class="admin-chip">Module: <strong><c:out value="${filter.module}"/></strong></span>
                        </c:if>
                        <c:if test="${not empty filter.actionType}">
                            <span class="admin-chip">Thao tác: <strong><c:out value="${filter.actionType}"/></strong></span>
                        </c:if>
                    </div>
                </div>

                <form class="admin-audit-filter-form" method="get" action="${pageContext.request.contextPath}/admin/audit-logs">
                    <div class="admin-audit-filter-grid">
                        <label class="admin-audit-filter-field admin-audit-filter-field--keyword">
                            <span>Từ khóa</span>
                            <input class="admin-input" type="text" name="keyword" value="${fn:escapeXml(filter.keyword)}"
                                   placeholder="VD: giá, đơn hàng, tên admin, mã sản phẩm...">
                        </label>

                        <label class="admin-audit-filter-field">
                            <span>Người thao tác</span>
                            <input class="admin-input" type="text" name="actor" value="${fn:escapeXml(filter.actor)}"
                                   placeholder="Username hoặc họ tên admin">
                        </label>

                        <label class="admin-audit-filter-field">
                            <span>Module</span>
                            <select class="admin-select" name="module">
                                <option value="">Tất cả module</option>
                                <c:forEach var="m" items="${modules}">
                                    <option value="${fn:escapeXml(m)}" ${filter.module == m ? 'selected' : ''}>
                                        <c:out value="${m}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </label>

                        <label class="admin-audit-filter-field">
                            <span>Thao tác</span>
                            <select class="admin-select" name="actionType">
                                <option value="">Tất cả thao tác</option>
                                <c:forEach var="a" items="${actionTypes}">
                                    <option value="${fn:escapeXml(a)}" ${filter.actionType == a ? 'selected' : ''}>
                                        <c:out value="${a}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </label>

                        <label class="admin-audit-filter-field">
                            <span>Từ ngày</span>
                            <input class="admin-input" type="date" name="dateFrom" value="${filter.dateFrom}">
                        </label>

                        <label class="admin-audit-filter-field">
                            <span>Đến ngày</span>
                            <input class="admin-input" type="date" name="dateTo" value="${filter.dateTo}">
                        </label>

                        <label class="admin-audit-filter-field">
                            <span>Số dòng</span>
                            <select class="admin-select" name="pageSize">
                                <option value="10" ${pageSize == 10 ? 'selected' : ''}>10</option>
                                <option value="20" ${pageSize == 20 ? 'selected' : ''}>20</option>
                                <option value="50" ${pageSize == 50 ? 'selected' : ''}>50</option>
                                <option value="100" ${pageSize == 100 ? 'selected' : ''}>100</option>
                            </select>
                        </label>
                    </div>

                    <div class="admin-audit-filter-actions">
                        <button type="submit" class="admin-btn admin-btn--primary">Lọc nhật ký</button>
                        <a class="admin-btn admin-btn--ghost" href="${pageContext.request.contextPath}/admin/audit-logs">
                            Xóa lọc
                        </a>
                    </div>
                </form>
            </div>
        </section>

        <section class="admin-card admin-audit-list-card">
            <div class="admin-card__body">
                <div class="admin-audit-section-head admin-audit-section-head--list">
                    <div>
                        <h2 class="admin-audit-section-title">Danh sách nhật ký</h2>
                        <p class="admin-audit-section-desc">
                            Hiển thị chi tiết thời gian, admin thao tác, module, loại thao tác và giá trị thay đổi.
                        </p>
                    </div>
                    <span class="admin-chip admin-chip--brand">
                        <c:out value="${empty totalRows ? 0 : totalRows}"/> kết quả
                    </span>
                </div>

                <c:choose>
                    <c:when test="${not empty auditLogs}">
                        <div class="admin-audit-table-wrap">
                            <table class="admin-table admin-audit-table">
                                <thead>
                                <tr>
                                    <th class="admin-audit-col-time">Thời gian</th>
                                    <th class="admin-audit-col-admin">Admin</th>
                                    <th class="admin-audit-col-module">Module</th>
                                    <th class="admin-audit-col-action">Thao tác</th>
                                    <th class="admin-audit-col-target">Đối tượng</th>
                                    <th class="admin-audit-col-desc">Nội dung</th>
                                    <th class="admin-audit-col-value">Giá trị cũ</th>
                                    <th class="admin-audit-col-value">Giá trị mới</th>
                                    <th class="admin-audit-col-ip">IP</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="log" items="${auditLogs}">
                                    <tr class="admin-audit-row">
                                        <td class="admin-audit-time">
                                            <strong>#<c:out value="${log.id}"/></strong>
                                            <span><c:out value="${log.createdAtDisplay}"/></span>
                                        </td>

                                        <td>
                                            <div class="admin-audit-actor">
                                                <span class="admin-audit-actor__avatar">A</span>
                                                <span class="admin-audit-actor__body">
                                                    <strong><c:out value="${log.actorDisplayName}"/></strong>
                                                    <small><c:out value="${empty log.actorUsername ? '-' : log.actorUsername}"/></small>
                                                    <c:if test="${not empty log.actorRole}">
                                                        <span class="admin-chip"><c:out value="${log.actorRole}"/></span>
                                                    </c:if>
                                                </span>
                                            </div>
                                        </td>

                                        <td>
                                            <span class="admin-chip admin-chip--brand">
                                                <c:out value="${log.moduleLabel}"/>
                                            </span>
                                        </td>

                                        <td>
                                            <span class="admin-pill ${log.actionCssClass}">
                                                <c:out value="${log.actionLabel}"/>
                                            </span>
                                        </td>

                                        <td class="admin-audit-target">
                                            <c:out value="${log.entityDisplay}"/>
                                        </td>

                                        <td class="admin-audit-description">
                                            <c:out value="${log.description}"/>
                                            <c:if test="${not empty log.requestUri}">
                                                <div class="admin-path admin-audit-uri">
                                                    <c:out value="${log.requestMethod}"/> <c:out value="${log.requestUri}"/>
                                                </div>
                                            </c:if>
                                        </td>

                                        <td class="admin-audit-value">
                                            <c:out value="${empty log.oldValue ? '-' : log.oldValue}"/>
                                        </td>

                                        <td class="admin-audit-value">
                                            <c:out value="${empty log.newValue ? '-' : log.newValue}"/>
                                        </td>

                                        <td class="admin-path admin-audit-ip">
                                            <c:out value="${empty log.ipAddress ? '-' : log.ipAddress}"/>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <c:if test="${totalPages gt 1}">
                            <div class="admin-pagination admin-audit-pagination">
                                <c:choose>
                                    <c:when test="${currentPage gt 1}">
                                        <a class="admin-btn admin-btn--ghost"
                                           href="${pageContext.request.contextPath}/admin/audit-logs?page=${currentPage - 1}&${filterQueryString}">
                                            ← Trước
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="admin-btn admin-btn--ghost is-disabled">← Trước</span>
                                    </c:otherwise>
                                </c:choose>

                                <span class="admin-chip">
                                    Trang <strong>${currentPage}</strong> / ${totalPages}
                                </span>

                                <c:choose>
                                    <c:when test="${currentPage lt totalPages}">
                                        <a class="admin-btn admin-btn--ghost"
                                           href="${pageContext.request.contextPath}/admin/audit-logs?page=${currentPage + 1}&${filterQueryString}">
                                            Sau →
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="admin-btn admin-btn--ghost is-disabled">Sau →</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:if>
                    </c:when>

                    <c:otherwise>
                        <div class="admin-audit-empty">
                            <div class="admin-audit-empty__icon">🧾</div>
                            <div>
                                <h3>Chưa có nhật ký phù hợp</h3>
                                <p>
                                    Hãy thử xóa bộ lọc hoặc thực hiện một thao tác quản trị như sửa giá sản phẩm,
                                    cập nhật đơn hàng, nhập kho.
                                </p>
                                <a class="admin-btn admin-btn--primary" href="${pageContext.request.contextPath}/admin/audit-logs">
                                    Xóa bộ lọc
                                </a>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

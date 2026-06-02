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

        <section class="admin-card admin-mb-3">
            <div class="admin-card__body">
                <div class="admin-toolbar">
                    <div>
                        <h1 class="admin-h1">Nhật ký hệ thống</h1>
                        <p class="admin-subtext">
                            Theo dõi ai đã sửa sản phẩm, đổi trạng thái đơn hàng, nhập kho hoặc thay đổi dữ liệu nhạy cảm.
                        </p>
                    </div>
                    <div class="admin-toolbar__actions">
            <span class="admin-chip admin-chip--brand">
              <c:out value="${empty totalRows ? 0 : totalRows}"/> log
            </span>
                    </div>
                </div>

                <form class="admin-filter" method="get" action="${pageContext.request.contextPath}/admin/audit-logs">
                    <div class="admin-filter__grid admin-filter__grid--wide">
                        <label class="admin-field">
                            <span>Từ khóa</span>
                            <input type="text" name="keyword" value="${fn:escapeXml(filter.keyword)}"
                                   placeholder="VD: giá, đơn hàng, tên admin, mã sản phẩm...">
                        </label>

                        <label class="admin-field">
                            <span>Người thao tác</span>
                            <input type="text" name="actor" value="${fn:escapeXml(filter.actor)}"
                                   placeholder="Username hoặc họ tên admin">
                        </label>

                        <label class="admin-field">
                            <span>Module</span>
                            <select name="module">
                                <option value="">Tất cả module</option>
                                <c:forEach var="m" items="${modules}">
                                    <option value="${fn:escapeXml(m)}" ${filter.module == m ? 'selected' : ''}>
                                        <c:out value="${m}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </label>

                        <label class="admin-field">
                            <span>Thao tác</span>
                            <select name="actionType">
                                <option value="">Tất cả thao tác</option>
                                <c:forEach var="a" items="${actionTypes}">
                                    <option value="${fn:escapeXml(a)}" ${filter.actionType == a ? 'selected' : ''}>
                                        <c:out value="${a}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </label>

                        <label class="admin-field">
                            <span>Từ ngày</span>
                            <input type="date" name="dateFrom" value="${filter.dateFrom}">
                        </label>

                        <label class="admin-field">
                            <span>Đến ngày</span>
                            <input type="date" name="dateTo" value="${filter.dateTo}">
                        </label>

                        <label class="admin-field">
                            <span>Số dòng</span>
                            <select name="pageSize">
                                <option value="10" ${pageSize == 10 ? 'selected' : ''}>10</option>
                                <option value="20" ${pageSize == 20 ? 'selected' : ''}>20</option>
                                <option value="50" ${pageSize == 50 ? 'selected' : ''}>50</option>
                                <option value="100" ${pageSize == 100 ? 'selected' : ''}>100</option>
                            </select>
                        </label>
                    </div>

                    <div class="admin-filter__actions">
                        <button type="submit" class="admin-btn admin-btn--primary">Lọc nhật ký</button>
                        <a class="admin-btn admin-btn--ghost" href="${pageContext.request.contextPath}/admin/audit-logs">
                            Xóa lọc
                        </a>
                    </div>
                </form>
            </div>
        </section>

        <section class="admin-card">
            <div class="admin-card__body">
                <c:choose>
                    <c:when test="${not empty auditLogs}">
                        <div class="admin-table-wrap">
                            <table class="admin-table admin-table--audit">
                                <thead>
                                <tr>
                                    <th>Thời gian</th>
                                    <th>Admin</th>
                                    <th>Module</th>
                                    <th>Thao tác</th>
                                    <th>Đối tượng</th>
                                    <th>Nội dung</th>
                                    <th>Giá trị cũ</th>
                                    <th>Giá trị mới</th>
                                    <th>IP</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="log" items="${auditLogs}">
                                    <tr>
                                        <td class="admin-nowrap">
                                            <div class="admin-id">#<c:out value="${log.id}"/></div>
                                            <div><c:out value="${log.createdAtDisplay}"/></div>
                                        </td>

                                        <td>
                                            <div class="admin-media__title"><c:out value="${log.actorDisplayName}"/></div>
                                            <div class="admin-muted">
                                                <c:out value="${empty log.actorUsername ? '-' : log.actorUsername}"/>
                                            </div>
                                            <c:if test="${not empty log.actorRole}">
                                                <span class="admin-chip"><c:out value="${log.actorRole}"/></span>
                                            </c:if>
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

                                        <td class="admin-break">
                                            <c:out value="${log.entityDisplay}"/>
                                        </td>

                                        <td class="admin-break admin-audit-description">
                                            <c:out value="${log.description}"/>
                                            <c:if test="${not empty log.requestUri}">
                                                <div class="admin-path admin-audit-uri">
                                                    <c:out value="${log.requestMethod}"/> <c:out value="${log.requestUri}"/>
                                                </div>
                                            </c:if>
                                        </td>

                                        <td class="admin-break admin-audit-value">
                                            <c:out value="${empty log.oldValue ? '-' : log.oldValue}"/>
                                        </td>

                                        <td class="admin-break admin-audit-value">
                                            <c:out value="${empty log.newValue ? '-' : log.newValue}"/>
                                        </td>

                                        <td class="admin-path">
                                            <c:out value="${empty log.ipAddress ? '-' : log.ipAddress}"/>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <c:if test="${totalPages gt 1}">
                            <div class="admin-pagination">
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
                        <div class="admin-empty">
                            <div class="admin-empty__title">Chưa có nhật ký phù hợp</div>
                            <div class="admin-empty__text">
                                Hãy thử xóa bộ lọc hoặc thực hiện một thao tác quản trị như sửa giá sản phẩm, cập nhật đơn hàng, nhập kho.
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Khuyến mãi & Mã giảm giá" scope="request"/>
<c:set var="activeMenu" value="promotions" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main admin-promo-page">
    <div class="admin-container admin-promo-container">

        <!-- TOPBAR -->
        <div class="admin-topbar admin-promotion-topbar">
            <div class="admin-promotion-heading">
                <h1 class="admin-h1">Khuyến mãi &amp; Mã giảm giá</h1>
                <p class="admin-subtext">
                    Quản lý tập trung mã giảm giá, giảm giá thương hiệu, giảm theo giá trị đơn hàng
                    và chương trình khuyến mãi cửa hàng.
                </p>
            </div>

            <div class="admin-promotion-create">
                <a class="admin-btn admin-btn--primary admin-promo-create-btn"
                   href="${pageContext.request.contextPath}/admin/promotions?action=new&type=COUPON">
                    + Tạo mã giảm giá
                </a>

                <a class="admin-btn admin-promo-create-btn"
                   href="${pageContext.request.contextPath}/admin/promotions?action=new&type=BRAND">
                    + Giảm giá thương hiệu
                </a>

                <a class="admin-btn admin-promo-create-btn"
                   href="${pageContext.request.contextPath}/admin/promotions?action=new&type=ORDER">
                    + Giảm theo đơn hàng
                </a>

                <a class="admin-btn admin-promo-create-btn"
                   href="${pageContext.request.contextPath}/admin/promotions?action=new&type=EVENT">
                    + Chương trình khuyến mãi
                </a>
            </div>
        </div>

        <!-- STATS -->
        <div class="admin-promo-stats">
            <div class="admin-promo-stat">
                <div class="admin-promo-stat__label">Tổng chương trình</div>
                <div class="admin-promo-stat__value">${stats.total}</div>
            </div>

            <div class="admin-promo-stat">
                <div class="admin-promo-stat__label">Đang hoạt động</div>
                <div class="admin-promo-stat__value">${stats.active}</div>
            </div>

            <div class="admin-promo-stat">
                <div class="admin-promo-stat__label">Tạm tắt</div>
                <div class="admin-promo-stat__value">${stats.inactive}</div>
            </div>

            <div class="admin-promo-stat">
                <div class="admin-promo-stat__label">Sắp diễn ra</div>
                <div class="admin-promo-stat__value">${stats.upcoming}</div>
            </div>

            <div class="admin-promo-stat">
                <div class="admin-promo-stat__label">Đã hết hạn</div>
                <div class="admin-promo-stat__value">${stats.expired}</div>
            </div>
        </div>

        <!-- MAIN CARD -->
        <div class="admin-card admin-promo-list-card">
            <div class="admin-card__body">

                <!-- TYPE TABS -->
                <div class="admin-promo-tabs">
                    <a class="admin-promo-tab ${promotionType == 'ALL' ? 'is-active' : ''}"
                       href="${pageContext.request.contextPath}/admin/promotions?type=ALL">
                        Tất cả
                    </a>

                    <a class="admin-promo-tab ${promotionType == 'COUPON' ? 'is-active' : ''}"
                       href="${pageContext.request.contextPath}/admin/promotions?type=COUPON">
                        Mã giảm giá
                    </a>

                    <a class="admin-promo-tab ${promotionType == 'BRAND' ? 'is-active' : ''}"
                       href="${pageContext.request.contextPath}/admin/promotions?type=BRAND">
                        Giảm giá thương hiệu
                    </a>

                    <a class="admin-promo-tab ${promotionType == 'ORDER' ? 'is-active' : ''}"
                       href="${pageContext.request.contextPath}/admin/promotions?type=ORDER">
                        Giảm theo đơn hàng
                    </a>

                    <a class="admin-promo-tab ${promotionType == 'EVENT' ? 'is-active' : ''}"
                       href="${pageContext.request.contextPath}/admin/promotions?type=EVENT">
                        Chương trình khuyến mãi
                    </a>
                </div>

                <!-- FILTER -->
                <div class="admin-toolbar admin-promo-toolbar">
                    <form method="get"
                          action="${pageContext.request.contextPath}/admin/promotions"
                          class="admin-toolbar__form admin-promo-filter-form">

                        <input type="hidden" name="action" value="list"/>
                        <input type="hidden" name="type" value="${promotionType}"/>

                        <input class="admin-input admin-promo-search"
                               type="text"
                               name="q"
                               value="${q}"
                               placeholder="Tìm theo mã, tên, thương hiệu, điều kiện...">

                        <select class="admin-select admin-promo-status-filter" name="status">
                            <option value="" ${empty status ? 'selected' : ''}>Tất cả trạng thái</option>
                            <option value="active" ${status == 'active' ? 'selected' : ''}>ACTIVE</option>
                            <option value="inactive" ${status == 'inactive' ? 'selected' : ''}>INACTIVE</option>
                            <option value="upcoming" ${status == 'upcoming' ? 'selected' : ''}>UPCOMING</option>
                            <option value="expired" ${status == 'expired' ? 'selected' : ''}>EXPIRED</option>
                        </select>

                        <button class="admin-btn admin-promo-filter-btn" type="submit">Lọc</button>

                        <c:if test="${not empty q || not empty status}">
                            <a class="admin-btn admin-promo-filter-btn"
                               href="${pageContext.request.contextPath}/admin/promotions?type=${promotionType}">
                                Xóa lọc
                            </a>
                        </c:if>
                    </form>
                </div>

                <!-- TABLE -->
                <c:choose>
                    <c:when test="${empty promotions}">
                        <div class="admin-empty admin-promo-empty">
                            Chưa có khuyến mãi phù hợp với bộ lọc hiện tại.
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="admin-table-wrap admin-promo-table-wrap">
                            <table class="admin-table admin-promo-table">
                                <thead>
                                <tr>
                                    <th class="admin-promo-col-id">ID</th>
                                    <th class="admin-promo-col-type">Loại</th>
                                    <th class="admin-promo-col-title">Tên / Mã</th>
                                    <th class="admin-promo-col-scope">Phạm vi áp dụng</th>
                                    <th class="admin-promo-col-discount-type">Kiểu giảm</th>
                                    <th class="admin-promo-col-value">Giá trị</th>
                                    <th class="admin-promo-col-condition">Điều kiện</th>
                                    <th class="admin-promo-col-period">Thời gian</th>
                                    <th class="admin-promo-col-status">Trạng thái</th>
                                    <th class="admin-promo-col-actions">Thao tác</th>
                                </tr>
                                </thead>

                                <tbody>
                                <c:forEach var="row" items="${promotions}">
                                    <tr>
                                        <!-- ID -->
                                        <td class="admin-promo-id-cell">
                                            <span class="admin-muted">#${row.id}</span>
                                        </td>

                                        <!-- TYPE -->
                                        <td>
                                            <c:choose>
                                                <c:when test="${row.type == 'COUPON'}">
                          <span class="admin-promo-type admin-promo-type--coupon">
                                  ${row.typeLabel}
                          </span>
                                                </c:when>

                                                <c:when test="${row.type == 'BRAND'}">
                          <span class="admin-promo-type admin-promo-type--brand">
                                  ${row.typeLabel}
                          </span>
                                                </c:when>

                                                <c:when test="${row.type == 'ORDER'}">
                          <span class="admin-promo-type admin-promo-type--order">
                                  ${row.typeLabel}
                          </span>
                                                </c:when>

                                                <c:when test="${row.type == 'EVENT'}">
                          <span class="admin-promo-type admin-promo-type--event">
                                  ${row.typeLabel}
                          </span>
                                                </c:when>

                                                <c:otherwise>
                          <span class="admin-promo-type">
                            <c:out value="${row.typeLabel}"/>
                          </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <!-- TITLE / CODE -->
                                        <td>
                                            <div class="admin-promo-title">
                                                <c:out value="${row.title}"/>
                                            </div>

                                            <c:if test="${not empty row.code}">
                                                <div class="admin-promo-code">
                                                    CODE: <strong><c:out value="${row.code}"/></strong>
                                                </div>
                                            </c:if>
                                        </td>

                                        <!-- SCOPE -->
                                        <td>
                                            <div class="admin-promo-scope">
                                                <c:out value="${row.scopeLabel}"/>
                                            </div>
                                        </td>

                                        <!-- DISCOUNT TYPE -->
                                        <td>
                                            <c:choose>
                                                <c:when test="${row.discountType == 'PERCENT'}">
                                                    <span class="admin-pill admin-pill--ok">PERCENT</span>
                                                </c:when>

                                                <c:when test="${row.discountType == 'FIXED'}">
                                                    <span class="admin-pill">FIXED</span>
                                                </c:when>

                                                <c:otherwise>
                          <span class="admin-muted">
                            <c:out value="${empty row.discountType ? '—' : row.discountType}"/>
                          </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <!-- DISCOUNT VALUE -->
                                        <td class="admin-promo-value-cell">
                                            <strong>
                                                <c:out value="${row.discountValueLabel}"/>
                                            </strong>
                                        </td>

                                        <!-- CONDITION -->
                                        <td>
                                            <div class="admin-promo-condition">
                                                <c:out value="${row.conditionLabel}"/>
                                            </div>
                                        </td>

                                        <!-- PERIOD -->
                                        <td class="admin-promo-period-cell">
                                            <div class="admin-promo-period">
                                                <div class="admin-promo-period__row">
                                                    <span class="admin-promo-period__label">Từ</span>
                                                    <span class="admin-promo-period__date">
                            <c:choose>
                                <c:when test="${empty row.startDate}">Không giới hạn</c:when>
                                <c:otherwise><c:out value="${row.startDate}"/></c:otherwise>
                            </c:choose>
                          </span>
                                                </div>

                                                <div class="admin-promo-period__row">
                                                    <span class="admin-promo-period__label">Đến</span>
                                                    <span class="admin-promo-period__date">
                            <c:choose>
                                <c:when test="${empty row.endDate}">Không giới hạn</c:when>
                                <c:otherwise><c:out value="${row.endDate}"/></c:otherwise>
                            </c:choose>
                          </span>
                                                </div>
                                            </div>
                                        </td>

                                        <!-- STATUS -->
                                        <td class="admin-status-cell">
                                            <c:choose>
                                                <c:when test="${row.statusLabel == 'ACTIVE'}">
                                                    <span class="admin-pill admin-pill--ok">ACTIVE</span>
                                                </c:when>

                                                <c:when test="${row.statusLabel == 'INACTIVE'}">
                                                    <span class="admin-pill admin-pill--danger">INACTIVE</span>
                                                </c:when>

                                                <c:when test="${row.statusLabel == 'EXPIRED'}">
                                                    <span class="admin-pill admin-pill--danger">EXPIRED</span>
                                                </c:when>

                                                <c:when test="${row.statusLabel == 'UPCOMING'}">
                                                    <span class="admin-pill admin-pill--warning">UPCOMING</span>
                                                </c:when>

                                                <c:otherwise>
                          <span class="admin-pill">
                            <c:out value="${row.statusLabel}"/>
                          </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <!-- ACTIONS -->
                                        <td class="admin-promo-action-cell">
                                            <div class="admin-promo-actions">
                                                <a class="admin-btn admin-promo-action-btn"
                                                   href="${pageContext.request.contextPath}/admin/promotions?action=edit&type=${row.type}&id=${row.id}">
                                                    Sửa
                                                </a>

                                                <form method="post"
                                                      action="${pageContext.request.contextPath}/admin/promotions"
                                                      class="admin-inline">

                                                    <%@ include file="/jsp/common/csrf.jspf" %>

                                                    <input type="hidden" name="action" value="toggle"/>
                                                    <input type="hidden" name="type" value="${row.type}"/>
                                                    <input type="hidden" name="id" value="${row.id}"/>

                                                    <button class="admin-btn admin-promo-action-btn" type="submit">
                                                        <c:choose>
                                                            <c:when test="${row.active}">Tắt</c:when>
                                                            <c:otherwise>Bật</c:otherwise>
                                                        </c:choose>
                                                    </button>
                                                </form>

                                                <form method="post"
                                                      action="${pageContext.request.contextPath}/admin/promotions"
                                                      class="admin-inline"
                                                      onsubmit="return confirm('Bạn có chắc muốn xóa hoặc vô hiệu hóa khuyến mãi này?');">

                                                    <%@ include file="/jsp/common/csrf.jspf" %>

                                                    <input type="hidden" name="action" value="delete"/>
                                                    <input type="hidden" name="type" value="${row.type}"/>
                                                    <input type="hidden" name="id" value="${row.id}"/>

                                                    <button class="admin-btn admin-btn--danger admin-promo-action-btn" type="submit">
                                                        Xóa
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
        </div>

    </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>
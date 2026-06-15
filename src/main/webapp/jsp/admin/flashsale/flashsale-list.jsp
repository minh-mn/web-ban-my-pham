<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Flash Sale" scope="request"/>
<c:set var="activeMenu" value="flashsale" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="flashSaleTotal" value="${empty flashSales ? 0 : fn:length(flashSales)}" />
<c:set var="flashSaleActiveCount" value="0" />
<c:set var="flashSaleInactiveCount" value="0" />

<c:forEach var="flashSaleStat" items="${flashSales}">
  <c:choose>
    <c:when test="${flashSaleStat.active}">
      <c:set var="flashSaleActiveCount" value="${flashSaleActiveCount + 1}" />
    </c:when>
    <c:otherwise>
      <c:set var="flashSaleInactiveCount" value="${flashSaleInactiveCount + 1}" />
    </c:otherwise>
  </c:choose>
</c:forEach>

<main class="admin-main">
  <div class="admin-container admin-flashsale-page">

    <section class="admin-flashsale-hero">
      <div class="admin-flashsale-hero__content">
        <span class="admin-flashsale-eyebrow">KHUYẾN MÃI &amp; TĂNG TRƯỞNG</span>
        <h1 class="admin-flashsale-title">Quản lý Flash Sale</h1>
        <p class="admin-flashsale-subtitle">
          Tạo khung giờ Flash Sale, bật/tắt chương trình và quản lý sản phẩm chi tiết.
          Giới hạn mua mỗi khách được cấu hình trong mục sản phẩm của từng Flash Sale.
        </p>
      </div>

      <div class="admin-flashsale-hero__actions">
        <a class="admin-btn admin-btn--primary"
           href="${ctx}/admin/flash-sale?action=new">
          + Thêm Flash Sale
        </a>
      </div>
    </section>

    <c:if test="${param.saved == '1'}">
      <div class="admin-alert admin-alert--success">
        Đã lưu chương trình Flash Sale.
      </div>
    </c:if>

    <c:if test="${param.deleted == '1'}">
      <div class="admin-alert admin-alert--success">
        Đã xóa chương trình Flash Sale.
      </div>
    </c:if>

    <c:if test="${not empty param.error}">
      <div class="admin-alert admin-alert--danger">
        Không thể xử lý Flash Sale. Vui lòng kiểm tra lại dữ liệu hoặc chương trình đang có sản phẩm liên kết.
      </div>
    </c:if>

    <section class="admin-flashsale-stats">
      <div class="admin-flashsale-stat admin-flashsale-stat--total">
        <span class="admin-flashsale-stat__icon">⚡</span>
        <span class="admin-flashsale-stat__label">Tổng chương trình</span>
        <strong class="admin-flashsale-stat__value">
          <c:out value="${flashSaleTotal}" />
        </strong>
        <small class="admin-flashsale-stat__note">Tất cả Flash Sale đã tạo</small>
      </div>

      <div class="admin-flashsale-stat admin-flashsale-stat--active">
        <span class="admin-flashsale-stat__icon">✅</span>
        <span class="admin-flashsale-stat__label">Đang bật</span>
        <strong class="admin-flashsale-stat__value">
          <c:out value="${flashSaleActiveCount}" />
        </strong>
        <small class="admin-flashsale-stat__note">Có thể áp dụng nếu đúng thời gian</small>
      </div>

      <div class="admin-flashsale-stat admin-flashsale-stat--inactive">
        <span class="admin-flashsale-stat__icon">⏸️</span>
        <span class="admin-flashsale-stat__label">Tạm tắt</span>
        <strong class="admin-flashsale-stat__value">
          <c:out value="${flashSaleInactiveCount}" />
        </strong>
        <small class="admin-flashsale-stat__note">Chưa hiển thị cho khách</small>
      </div>

      <div class="admin-flashsale-stat admin-flashsale-stat--limit">
        <span class="admin-flashsale-stat__icon">🛒</span>
        <span class="admin-flashsale-stat__label">Giới hạn mua</span>
        <strong class="admin-flashsale-stat__value admin-flashsale-stat__value--text">Theo sản phẩm</strong>
        <small class="admin-flashsale-stat__note">Cấu hình max quantity / khách</small>
      </div>
    </section>

    <section class="admin-card admin-flashsale-list-card">
      <div class="admin-card__body">
        <div class="admin-flashsale-section-head">
          <div>
            <h2 class="admin-flashsale-section-title">Danh sách chương trình</h2>
            <p class="admin-flashsale-section-desc">
              Bấm <strong>Sản phẩm</strong> để thêm sản phẩm Flash Sale, nhập giá Flash,
              số lượng và giới hạn mua mỗi khách.
            </p>
          </div>

          <span class="admin-chip admin-chip--brand">
            <c:out value="${flashSaleTotal}" /> chương trình
          </span>
        </div>

        <c:choose>
          <c:when test="${empty flashSales}">
            <div class="admin-flashsale-empty">
              <div class="admin-flashsale-empty__icon">⚡</div>
              <div>
                <h3>Chưa có Flash Sale nào</h3>
                <p>Tạo chương trình Flash Sale đầu tiên, sau đó thêm sản phẩm và cấu hình giới hạn mua mỗi khách.</p>
                <a class="admin-btn admin-btn--primary"
                   href="${ctx}/admin/flash-sale?action=new">
                  + Thêm Flash Sale
                </a>
              </div>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-flashsale-table-wrap">
              <table class="admin-table admin-flashsale-table">
                <thead>
                <tr>
                  <th class="admin-flashsale-col-id">ID</th>
                  <th class="admin-flashsale-col-title">Chương trình</th>
                  <th class="admin-flashsale-col-period">Bắt đầu</th>
                  <th class="admin-flashsale-col-period">Kết thúc</th>
                  <th class="admin-flashsale-col-status">Trạng thái</th>
                  <th class="admin-flashsale-col-limit">Giới hạn mua</th>
                  <th class="admin-flashsale-col-actions">Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="f" items="${flashSales}">
                  <tr class="${f.active ? 'admin-flashsale-row--active' : 'admin-flashsale-row--inactive'}">
                    <td class="admin-flashsale-id-cell">
                      #<c:out value="${f.id}" />
                    </td>

                    <td>
                      <div class="admin-flashsale-program">
                        <strong><c:out value="${f.title}" /></strong>
                        <span>
                          Quản lý sản phẩm để cấu hình giá Flash, số lượng bán và giới hạn / khách.
                        </span>
                      </div>
                    </td>

                    <td>
                      <div class="admin-flashsale-period">
                        <span>Bắt đầu</span>
                        <strong><c:out value="${f.startTime}" /></strong>
                      </div>
                    </td>

                    <td>
                      <div class="admin-flashsale-period">
                        <span>Kết thúc</span>
                        <strong><c:out value="${f.endTime}" /></strong>
                      </div>
                    </td>

                    <td class="admin-status-cell">
                      <c:choose>
                        <c:when test="${f.active}">
                          <span class="admin-pill admin-pill--ok">Đang bật</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill admin-pill--danger">Tạm tắt</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <div class="admin-flashsale-limit-summary">
                        <span class="admin-flashsale-limit-chip">max_quantity_per_user</span>
                        <small>Giới hạn mua nằm trong từng sản phẩm Flash Sale</small>
                      </div>
                    </td>

                    <td class="admin-flashsale-action-cell">
                      <div class="admin-flashsale-actions">
                        <a class="admin-btn admin-flashsale-action-btn"
                           href="${ctx}/admin/flash-sale?action=edit&id=${f.id}">
                          Sửa
                        </a>

                        <a class="admin-btn admin-btn--primary admin-flashsale-action-btn"
                           href="${ctx}/admin/flash-sale/items?flashSaleId=${f.id}">
                          Sản phẩm
                        </a>
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

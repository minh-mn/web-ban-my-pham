<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | ${mode == 'edit' ? 'Sửa hạng khách hàng' : 'Thêm hạng khách hàng'}" scope="request"/>
<c:set var="activeMenu" value="ranks" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEditRank" value="${mode == 'edit'}" />
<c:set var="isMemberRank" value="${isEditRank && rank.code == 'MEMBER'}" />

<main class="admin-main">
  <div class="admin-container admin-rank-form-page">

    <section class="admin-rank-form-hero">
      <div class="admin-rank-form-hero__content">
        <span class="admin-rank-form-eyebrow">KHÁCH HÀNG &amp; TĂNG TRƯỞNG</span>
        <h1 class="admin-rank-form-title">
          <c:choose>
            <c:when test="${isEditRank}">Sửa hạng khách hàng</c:when>
            <c:otherwise>Thêm hạng khách hàng</c:otherwise>
          </c:choose>
        </h1>
        <p class="admin-rank-form-subtitle">
          Cấu hình mã rank, tên hiển thị, mốc chi tiêu tối thiểu và phần trăm ưu đãi
          để hệ thống tự động phân hạng khách hàng theo doanh số đã thanh toán.
        </p>
      </div>

      <div class="admin-rank-form-hero__actions">
        <a class="admin-btn" href="${ctx}/admin/ranks">
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
          action="${ctx}/admin/ranks"
          class="admin-form admin-rank-form">
      <%@ include file="/jsp/common/csrf.jspf" %>

      <input type="hidden"
             name="action"
             value="${isEditRank ? 'update' : 'create'}"/>

      <c:if test="${isEditRank}">
        <input type="hidden" name="id" value="${rank.id}"/>
      </c:if>

      <div class="admin-rank-form-layout">

        <section class="admin-card admin-rank-form-card">
          <div class="admin-card__body">
            <div class="admin-rank-form-section-head">
              <div>
                <h2 class="admin-rank-form-section-title">Thông tin hạng khách hàng</h2>
                <p class="admin-rank-form-section-desc">
                  Các trường này quyết định thứ tự rank, tên hiển thị và ưu đãi tự động.
                </p>
              </div>

              <c:choose>
                <c:when test="${isMemberRank}">
                  <span class="admin-chip admin-chip--brand">Rank mặc định</span>
                </c:when>
                <c:when test="${isEditRank && rank.active}">
                  <span class="admin-chip admin-chip--success">Đang hoạt động</span>
                </c:when>
                <c:when test="${isEditRank && !rank.active}">
                  <span class="admin-chip admin-chip--warning">Tạm tắt</span>
                </c:when>
                <c:otherwise>
                  <span class="admin-chip admin-chip--brand">Rank mới</span>
                </c:otherwise>
              </c:choose>
            </div>

            <div class="admin-rank-form-grid">
              <div class="admin-field">
                <label class="admin-label" for="rankCode">
                  Mã rank <span class="admin-required">*</span>
                </label>

                <input id="rankCode"
                       class="admin-input"
                       type="text"
                       name="code"
                       value="${not empty rank ? fn:escapeXml(rank.code) : ''}"
                       maxlength="50"
                       pattern="[A-Z0-9_-]{2,50}"
                       placeholder="VD: MEMBER, SILVER, GOLD"
                       autocomplete="off"
                       required />

                <div class="admin-help">
                  Chỉ dùng chữ in hoa, số, dấu gạch dưới hoặc gạch ngang. Ví dụ: MEMBER, SILVER, GOLD, VIP.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="rankName">
                  Tên hiển thị <span class="admin-required">*</span>
                </label>

                <input id="rankName"
                       class="admin-input"
                       type="text"
                       name="name"
                       value="${not empty rank ? fn:escapeXml(rank.name) : ''}"
                       maxlength="100"
                       placeholder="VD: Thành viên, Bạc, Vàng, Kim cương"
                       autocomplete="off"
                       required />

                <div class="admin-help">
                  Tên này sẽ được hiển thị ở trang tài khoản khách hàng.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="minSpent">
                  Mốc chi tiêu tối thiểu <span class="admin-required">*</span>
                </label>

                <input id="minSpent"
                       class="admin-input"
                       type="number"
                       name="minSpent"
                       min="0"
                       step="1000"
                       value="${not empty rank ? rank.minSpent : 0}"
                       placeholder="VD: 1000000"
                       required />

                <div class="admin-help">
                  Tổng tiền đơn hàng đã thanh toán cần đạt để lên rank này.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="discountPercent">
                  Ưu đãi theo rank (%) <span class="admin-required">*</span>
                </label>

                <input id="discountPercent"
                       class="admin-input"
                       type="number"
                       name="discountPercent"
                       min="0"
                       max="100"
                       step="1"
                       value="${not empty rank ? rank.discountPercent : 0}"
                       placeholder="VD: 5"
                       required />

                <div class="admin-help">
                  Nhập từ 0 đến 100. Nếu không có ưu đãi thì nhập 0.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="cssClass">
                  CSS class <span class="admin-required">*</span>
                </label>

                <input id="cssClass"
                       class="admin-input"
                       type="text"
                       name="cssClass"
                       value="${not empty rank ? fn:escapeXml(rank.cssClass) : 'rank-member'}"
                       maxlength="100"
                       list="rankClassSuggestions"
                       placeholder="VD: rank-gold"
                       autocomplete="off"
                       required />

                <datalist id="rankClassSuggestions">
                  <option value="rank-member"></option>
                  <option value="rank-silver"></option>
                  <option value="rank-gold"></option>
                  <option value="rank-diamond"></option>
                  <option value="rank-vip"></option>
                </datalist>

                <div class="admin-help">
                  Dùng để gắn màu hoặc badge rank ở giao diện user. Ví dụ: rank-member, rank-gold, rank-vip.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="active">
                  Trạng thái
                </label>

                <select id="active" class="admin-select" name="active">
                  <option value="1" ${empty rank || rank.active ? "selected" : ""}>
                    Đang hoạt động
                  </option>
                  <option value="0" ${not empty rank && !rank.active ? "selected" : ""}>
                    Tạm tắt
                  </option>
                </select>

                <div class="admin-help">
                  Rank tạm tắt sẽ không được dùng để tính hạng khách hàng.
                </div>
              </div>
            </div>

            <c:if test="${isMemberRank}">
              <div class="admin-alert admin-alert--warning admin-rank-member-warning">
                Rank MEMBER là rank mặc định của hệ thống. Không nên đổi mã hoặc tắt trạng thái của rank này.
              </div>
            </c:if>
          </div>
        </section>

        <aside class="admin-card admin-rank-guide-card">
          <div class="admin-card__body">
            <div class="admin-rank-form-section-head">
              <div>
                <h2 class="admin-rank-form-section-title">Gợi ý cấu hình</h2>
                <p class="admin-rank-form-section-desc">
                  Nên sắp xếp rank theo mốc chi tiêu tăng dần để khách dễ theo dõi.
                </p>
              </div>
            </div>

            <div class="admin-rank-guide-list">
              <div class="admin-rank-guide-item">
                <span>1</span>
                <div>
                  <strong>MEMBER</strong>
                  <small>Rank mặc định, mốc chi tiêu 0đ, ưu đãi có thể là 0%.</small>
                </div>
              </div>

              <div class="admin-rank-guide-item">
                <span>2</span>
                <div>
                  <strong>SILVER / GOLD</strong>
                  <small>Dùng cho khách mua thường xuyên, có thể ưu đãi theo phần trăm nhỏ.</small>
                </div>
              </div>

              <div class="admin-rank-guide-item">
                <span>3</span>
                <div>
                  <strong>DIAMOND / VIP</strong>
                  <small>Dành cho khách chi tiêu cao, ưu đãi và mã giảm giá có thể mở rộng hơn.</small>
                </div>
              </div>
            </div>

            <div class="admin-rank-preview-box">
              <span class="admin-rank-preview-badge ${not empty rank.cssClass ? rank.cssClass : 'rank-member'}">
                <c:choose>
                  <c:when test="${not empty rank.name}">
                    <c:out value="${rank.name}" />
                  </c:when>
                  <c:otherwise>Rank preview</c:otherwise>
                </c:choose>
              </span>
              <small>
                Preview dùng class CSS bạn nhập để dễ kiểm tra tên badge trước khi lưu.
              </small>
            </div>
          </div>
        </aside>

      </div>

      <div class="admin-rank-form-actions">
        <a class="admin-btn" href="${ctx}/admin/ranks">
          Hủy
        </a>

        <button class="admin-btn admin-btn--primary" type="submit">
          Lưu hạng khách hàng
        </button>
      </div>
    </form>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

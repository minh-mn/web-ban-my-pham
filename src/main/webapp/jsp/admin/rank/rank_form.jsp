<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Form User Rank" scope="request"/>
<c:set var="activeMenu" value="ranks" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">
          <c:choose>
            <c:when test="${mode == 'edit'}">Sửa hạng khách hàng</c:when>
            <c:otherwise>Thêm hạng khách hàng</c:otherwise>
          </c:choose>
        </h1>

        <p class="admin-subtext">
          Cấu hình mã rank, mốc chi tiêu tối thiểu và phần trăm ưu đãi cho khách hàng.
        </p>
      </div>

      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/ranks">
        Quay lại
      </a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:if test="${not empty error}">
          <div class="admin-alert admin-alert--danger">
            <c:out value="${error}"/>
          </div>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/ranks"
              class="admin-form">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden"
                 name="action"
                 value="${mode == 'edit' ? 'update' : 'create'}"/>

          <c:if test="${mode == 'edit'}">
            <input type="hidden" name="id" value="${rank.id}"/>
          </c:if>

          <div class="admin-grid-2">

            <div class="admin-field">
              <div class="admin-label">Mã rank</div>

              <input class="admin-input"
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
              <div class="admin-label">Tên hiển thị</div>

              <input class="admin-input"
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
              <div class="admin-label">Mốc chi tiêu tối thiểu</div>

              <input class="admin-input"
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
              <div class="admin-label">Ưu đãi theo rank (%)</div>

              <input class="admin-input"
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
              <div class="admin-label">CSS class</div>

              <input class="admin-input"
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
              <div class="admin-label">Trạng thái</div>

              <select class="admin-select" name="active">
                <option value="1" ${empty rank || rank.active ? "selected" : ""}>
                  ACTIVE
                </option>
                <option value="0" ${not empty rank && !rank.active ? "selected" : ""}>
                  INACTIVE
                </option>
              </select>

              <div class="admin-help">
                INACTIVE sẽ không được dùng để tính hạng khách hàng.
              </div>
            </div>

          </div>

          <c:if test="${mode == 'edit' && rank.code == 'MEMBER'}">
            <div class="admin-alert admin-alert--warning" style="margin-top:16px;">
              Rank MEMBER là rank mặc định của hệ thống. Không nên đổi mã hoặc tắt trạng thái của rank này.
            </div>
          </c:if>

          <hr class="admin-divider"/>

          <div class="admin-actions">
            <button class="admin-btn admin-btn--primary" type="submit">
              Lưu
            </button>

            <a class="admin-btn" href="${pageContext.request.contextPath}/admin/ranks">
              Hủy
            </a>
          </div>

        </form>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>
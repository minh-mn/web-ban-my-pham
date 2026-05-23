<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Form Coupon" scope="request"/>
<c:set var="activeMenu" value="coupons" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<main class="admin-main">
  <div class="admin-container">

    <div class="admin-topbar">
      <div>
        <h1 class="admin-h1">
          <c:choose>
            <c:when test="${mode == 'edit'}">Sửa coupon</c:when>
            <c:otherwise>Thêm coupon</c:otherwise>
          </c:choose>
        </h1>
        <p class="admin-subtext">
          Thiết lập mã giảm giá, đơn tối thiểu và rank khách hàng được áp dụng.
        </p>
      </div>

      <a class="admin-btn" href="${pageContext.request.contextPath}/admin/coupons">Quay lại</a>
    </div>

    <div class="admin-card">
      <div class="admin-card__body">

        <c:if test="${not empty error}">
          <div class="admin-alert admin-alert--danger">
            <c:out value="${error}"/>
          </div>
        </c:if>

        <%-- LocalDate: dùng trực tiếp định dạng yyyy-MM-dd --%>
        <c:set var="startDateValue" value=""/>
        <c:set var="endDateValue" value=""/>

        <c:if test="${not empty coupon && not empty coupon.startDate}">
          <c:set var="startDateValue" value="${coupon.startDate}"/>
        </c:if>

        <c:if test="${not empty coupon && not empty coupon.endDate}">
          <c:set var="endDateValue" value="${coupon.endDate}"/>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/coupons"
              class="admin-form">

          <%@ include file="/jsp/common/csrf.jspf" %>

          <input type="hidden" name="action" value="${mode == 'edit' ? 'update' : 'create'}"/>

          <c:if test="${mode == 'edit'}">
            <input type="hidden" name="id" value="${coupon.id}"/>
          </c:if>

          <div class="admin-grid-2">

            <div class="admin-field">
              <div class="admin-label">CODE</div>
              <input class="admin-input"
                     type="text"
                     name="code"
                     value="${not empty coupon ? fn:escapeXml(coupon.code) : ''}"
                     maxlength="32"
                     autocomplete="off"
                     required />
              <div class="admin-help">Nên viết hoa, không dấu cách. Ví dụ: NEWYEAR10</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Trạng thái</div>
              <select class="admin-select" name="active">
                <option value="1" ${empty coupon || coupon.active ? 'selected' : ''}>ACTIVE</option>
                <option value="0" ${not empty coupon && !coupon.active ? 'selected' : ''}>INACTIVE</option>
              </select>
              <div class="admin-help">INACTIVE sẽ không áp dụng được ở checkout.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Loại voucher</div>
              <select name="type" class="admin-select" required>
                <option value="DISCOUNT"
                ${empty coupon || empty coupon.type || coupon.type == 'DISCOUNT' ? 'selected' : ''}>
                  Giảm giá (%)
                </option>

                <option value="FREESHIP"
                ${not empty coupon && coupon.type == 'FREESHIP' ? 'selected' : ''}>
                  Miễn phí vận chuyển
                </option>

                <option value="PRODUCT"
                ${not empty coupon && coupon.type == 'PRODUCT' ? 'selected' : ''}>
                  Sản phẩm
                </option>
              </select>
              <div class="admin-help">Loại voucher dùng để phân loại nghiệp vụ giảm giá.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Số lượt sử dụng tối đa</div>
              <input class="admin-input"
                     type="number"
                     name="maxUses"
                     min="1"
                     required
                     value="${not empty coupon ? coupon.maxUses : 1}" />
              <div class="admin-help">Coupon sẽ ngừng dùng khi đạt số lượt này.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Giảm (%)</div>
              <input class="admin-input"
                     type="number"
                     name="discountPercent"
                     min="1"
                     max="100"
                     step="1"
                     value="${not empty coupon ? coupon.discountPercent : ''}"
                     required />
              <div class="admin-help">Nhập từ 1 đến 100.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Giảm tối đa (₫) (tuỳ chọn)</div>
              <input class="admin-input"
                     type="number"
                     name="maxDiscountAmount"
                     min="0"
                     step="1"
                     value="${not empty coupon ? coupon.maxDiscountAmount : ''}"
                     placeholder="VD: 50000" />
              <div class="admin-help">Để trống nếu không giới hạn mức giảm tối đa.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Đơn hàng tối thiểu</div>
              <input class="admin-input"
                     type="number"
                     name="minOrderAmount"
                     min="0"
                     step="1000"
                     value="${not empty coupon ? coupon.minOrderAmount : 0}"
                     placeholder="VD: 200000" />
              <div class="admin-help">Đơn hàng phải đạt mức này mới dùng được mã.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Rank tối thiểu</div>
              <select class="admin-select" name="minRankCode" required>
                <option value="MEMBER"
                ${empty coupon || empty coupon.minRankCode || coupon.minRankCode == 'MEMBER' ? 'selected' : ''}>
                  Thành viên trở lên
                </option>

                <option value="SILVER"
                ${not empty coupon && coupon.minRankCode == 'SILVER' ? 'selected' : ''}>
                  Bạc trở lên
                </option>

                <option value="GOLD"
                ${not empty coupon && coupon.minRankCode == 'GOLD' ? 'selected' : ''}>
                  Vàng trở lên
                </option>

                <option value="DIAMOND"
                ${not empty coupon && coupon.minRankCode == 'DIAMOND' ? 'selected' : ''}>
                  Kim cương trở lên
                </option>

                <option value="VIP"
                ${not empty coupon && coupon.minRankCode == 'VIP' ? 'selected' : ''}>
                  VIP
                </option>
              </select>
              <div class="admin-help">Rank thấp hơn sẽ không thấy hoặc không dùng được mã này.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">Start Date</div>
              <input class="admin-input"
                     type="date"
                     name="startDate"
                     value="${startDateValue}" />
              <div class="admin-help">Để trống nếu hiệu lực ngay.</div>
            </div>

            <div class="admin-field">
              <div class="admin-label">End Date</div>
              <input class="admin-input"
                     type="date"
                     name="endDate"
                     value="${endDateValue}" />
              <div class="admin-help">Để trống nếu không giới hạn thời gian.</div>
            </div>

            <div class="admin-field admin-field--full">
              <div class="admin-label">Mô tả</div>
              <textarea class="admin-textarea"
                        name="description"
                        placeholder="VD: Mã giảm 10% cho khách hàng GOLD trở lên, đơn từ 500.000đ."><c:out value="${coupon.description}"/></textarea>
              <div class="admin-help">Mô tả ngắn điều kiện hoặc mục đích của coupon.</div>
            </div>

            <c:if test="${mode == 'edit'}">
              <div class="admin-field">
                <div class="admin-label">Đã dùng</div>
                <input class="admin-input"
                       type="number"
                       value="${coupon.usedCount}"
                       disabled />
                <div class="admin-help">Tự tăng khi áp dụng coupon trong đơn hàng.</div>
              </div>

              <div class="admin-field"></div>
            </c:if>

          </div>

          <hr class="admin-divider"/>

          <div class="admin-actions">
            <button class="admin-btn admin-btn--primary" type="submit">Lưu</button>
            <a class="admin-btn" href="${pageContext.request.contextPath}/admin/coupons">Hủy</a>
          </div>

        </form>

      </div>
    </div>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>
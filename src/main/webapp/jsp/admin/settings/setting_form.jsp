<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="ADMIN | Settings Form" scope="request"/>
<c:set var="activeMenu" value="settings" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<main class="admin-main">
  <div class="admin-container admin-setting-form-page">

    <section class="admin-setting-form-hero">
      <div class="admin-setting-form-hero__content">
        <span class="admin-setting-form-eyebrow">NỘI DUNG &amp; WEBSITE</span>
        <h1 class="admin-setting-form-title">Chỉnh sửa Website Settings</h1>
        <p class="admin-setting-form-subtitle">
          Cập nhật thông tin liên hệ, mạng xã hội, doanh nghiệp và tên website.
          Các giá trị này thường được dùng ở footer, trang liên hệ và các phần thông tin hệ thống.
        </p>
      </div>

      <div class="admin-setting-form-hero__actions">
        <a class="admin-btn"
           href="${ctx}/admin/settings">
          ← Quay lại danh sách
        </a>
      </div>
    </section>

    <form method="post"
          action="${ctx}/admin/settings/save"
          class="admin-form admin-setting-form">
      <%@ include file="/jsp/common/csrf.jspf" %>

      <div class="admin-setting-form-layout">

        <section class="admin-card admin-setting-form-card">
          <div class="admin-card__body">
            <div class="admin-setting-form-section-head">
              <div>
                <h2 class="admin-setting-form-section-title">Thông tin liên hệ</h2>
                <p class="admin-setting-form-section-desc">
                  Các thông tin này thường hiển thị ở footer và trang liên hệ.
                </p>
              </div>
              <span class="admin-chip admin-chip--brand">Contact</span>
            </div>

            <div class="admin-setting-form-grid">
              <label class="admin-field">
                <span class="admin-label">Hotline</span>
                <input class="admin-input"
                       name="hotline"
                       value="${settings.hotline}"
                       placeholder="VD: 0900 000 000">
              </label>

              <label class="admin-field">
                <span class="admin-label">Email Sales</span>
                <input class="admin-input"
                       type="email"
                       name="sales_email"
                       value="${settings.sales_email}"
                       placeholder="sales@example.com">
              </label>

              <label class="admin-field">
                <span class="admin-label">Email HR</span>
                <input class="admin-input"
                       type="email"
                       name="hr_email"
                       value="${settings.hr_email}"
                       placeholder="hr@example.com">
              </label>

              <label class="admin-field admin-field--full">
                <span class="admin-label">Địa chỉ</span>
                <input class="admin-input"
                       name="address"
                       value="${settings.address}"
                       placeholder="Địa chỉ công ty / cửa hàng">
              </label>
            </div>
          </div>
        </section>

        <aside class="admin-card admin-setting-preview-card">
          <div class="admin-card__body">
            <div class="admin-setting-form-section-head">
              <div>
                <h2 class="admin-setting-form-section-title">Preview nhanh</h2>
                <p class="admin-setting-form-section-desc">
                  Kiểm tra các thông tin quan trọng trước khi lưu.
                </p>
              </div>
            </div>

            <div class="admin-setting-preview">
              <div class="admin-setting-preview__icon">🏪</div>
              <div class="admin-setting-preview__body">
                <strong>
                  <c:choose>
                    <c:when test="${not empty settings.name_website}">
                      <c:out value="${settings.name_website}" />
                    </c:when>
                    <c:otherwise>MyCosmetic</c:otherwise>
                  </c:choose>
                </strong>
                <span>
                  <c:choose>
                    <c:when test="${not empty settings.company_name}">
                      <c:out value="${settings.company_name}" />
                    </c:when>
                    <c:otherwise>Chưa cấu hình tên công ty</c:otherwise>
                  </c:choose>
                </span>
              </div>
            </div>

            <div class="admin-setting-preview-list">
              <div>
                <span>Hotline</span>
                <strong>
                  <c:choose>
                    <c:when test="${not empty settings.hotline}">
                      <c:out value="${settings.hotline}" />
                    </c:when>
                    <c:otherwise>Chưa cấu hình</c:otherwise>
                  </c:choose>
                </strong>
              </div>

              <div>
                <span>Email Sales</span>
                <strong>
                  <c:choose>
                    <c:when test="${not empty settings.sales_email}">
                      <c:out value="${settings.sales_email}" />
                    </c:when>
                    <c:otherwise>Chưa cấu hình</c:otherwise>
                  </c:choose>
                </strong>
              </div>

              <div>
                <span>Năm bản quyền</span>
                <strong>
                  <c:choose>
                    <c:when test="${not empty settings.copyright_year}">
                      <c:out value="${settings.copyright_year}" />
                    </c:when>
                    <c:otherwise>Chưa cấu hình</c:otherwise>
                  </c:choose>
                </strong>
              </div>
            </div>
          </div>
        </aside>

      </div>

      <section class="admin-card admin-setting-form-card">
        <div class="admin-card__body">
          <div class="admin-setting-form-section-head">
            <div>
              <h2 class="admin-setting-form-section-title">Mạng xã hội</h2>
              <p class="admin-setting-form-section-desc">
                Nhập đường dẫn fanpage hoặc trang mạng xã hội đang sử dụng.
              </p>
            </div>
            <span class="admin-chip admin-chip--success">Social</span>
          </div>

          <div class="admin-setting-form-grid">
            <label class="admin-field">
              <span class="admin-label">Facebook</span>
              <input class="admin-input"
                     name="facebook"
                     value="${settings.facebook}"
                     placeholder="https://facebook.com/...">
            </label>

            <label class="admin-field">
              <span class="admin-label">Instagram</span>
              <input class="admin-input"
                     name="instagram"
                     value="${settings.instagram}"
                     placeholder="https://instagram.com/...">
            </label>
          </div>
        </div>
      </section>

      <section class="admin-card admin-setting-form-card">
        <div class="admin-card__body">
          <div class="admin-setting-form-section-head">
            <div>
              <h2 class="admin-setting-form-section-title">Thông tin doanh nghiệp</h2>
              <p class="admin-setting-form-section-desc">
                Thông tin pháp lý hiển thị trong footer hoặc khu vực giới thiệu công ty.
              </p>
            </div>
            <span class="admin-chip admin-chip--warning">Business</span>
          </div>

          <div class="admin-setting-form-grid">
            <label class="admin-field">
              <span class="admin-label">Tên công ty</span>
              <input class="admin-input"
                     name="company_name"
                     value="${settings.company_name}"
                     placeholder="Tên công ty">
            </label>

            <label class="admin-field">
              <span class="admin-label">MSDN</span>
              <input class="admin-input"
                     name="business_code"
                     value="${settings.business_code}"
                     placeholder="Mã số doanh nghiệp">
            </label>

            <label class="admin-field">
              <span class="admin-label">Ngày cấp</span>
              <input class="admin-input"
                     name="business_date"
                     value="${settings.business_date}"
                     placeholder="VD: 01/01/2026">
            </label>
          </div>
        </div>
      </section>

      <section class="admin-card admin-setting-form-card">
        <div class="admin-card__body">
          <div class="admin-setting-form-section-head">
            <div>
              <h2 class="admin-setting-form-section-title">Thiết lập website</h2>
              <p class="admin-setting-form-section-desc">
                Cấu hình tên website và năm bản quyền hiển thị ngoài giao diện.
              </p>
            </div>
            <span class="admin-chip admin-chip--brand">Website</span>
          </div>

          <div class="admin-setting-form-grid">
            <label class="admin-field">
              <span class="admin-label">Tên web</span>
              <input class="admin-input"
                     name="name_website"
                     value="${settings.name_website}"
                     placeholder="VD: MyCosmetic">
            </label>

            <label class="admin-field">
              <span class="admin-label">Năm bản quyền</span>
              <input class="admin-input"
                     name="copyright_year"
                     value="${settings.copyright_year}"
                     placeholder="VD: 2026">
            </label>
          </div>
        </div>
      </section>

      <div class="admin-setting-form-actions">
        <a class="admin-btn"
           href="${ctx}/admin/settings">
          Hủy
        </a>
        <button class="admin-btn admin-btn--primary"
                type="submit">
          Lưu thay đổi
        </button>
      </div>
    </form>

  </div>
</main>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

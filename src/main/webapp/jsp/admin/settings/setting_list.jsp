<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="ADMIN | Website Settings" scope="request"/>
<c:set var="activeMenu" value="settings" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-list.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="settingTotal" value="0" />
<c:set var="settingContactCount" value="0" />
<c:set var="settingSocialCount" value="0" />
<c:set var="settingBusinessCount" value="0" />

<c:forEach var="entryStat" items="${settings}">
  <c:set var="settingTotal" value="${settingTotal + 1}" />
  <c:set var="settingKey" value="${fn:toLowerCase(entryStat.key)}" />

  <c:if test="${settingKey == 'hotline' or settingKey == 'sales_email' or settingKey == 'hr_email' or settingKey == 'address'}">
    <c:set var="settingContactCount" value="${settingContactCount + 1}" />
  </c:if>

  <c:if test="${settingKey == 'facebook' or settingKey == 'instagram'}">
    <c:set var="settingSocialCount" value="${settingSocialCount + 1}" />
  </c:if>

  <c:if test="${settingKey == 'company_name' or settingKey == 'business_code' or settingKey == 'business_date'}">
    <c:set var="settingBusinessCount" value="${settingBusinessCount + 1}" />
  </c:if>
</c:forEach>

<main class="admin-main">
  <div class="admin-container admin-setting-page">

    <section class="admin-setting-hero">
      <div class="admin-setting-hero__content">
        <span class="admin-setting-eyebrow">NỘI DUNG &amp; WEBSITE</span>
        <h1 class="admin-setting-title">Website Settings</h1>
        <p class="admin-setting-subtitle">
          Quản lý thông tin hiển thị ở footer, liên hệ, mạng xã hội, thông tin pháp lý và tên website.
          Các giá trị này được dùng chung cho nhiều khu vực ngoài giao diện khách hàng.
        </p>
      </div>

      <div class="admin-setting-hero__actions">
        <a class="admin-btn admin-btn--primary"
           href="${ctx}/admin/settings?action=edit">
          Chỉnh sửa cấu hình
        </a>
      </div>
    </section>

    <section class="admin-setting-summary">
      <div class="admin-setting-stat admin-setting-stat--total">
        <span class="admin-setting-stat__icon">⚙️</span>
        <span class="admin-setting-stat__label">Tổng cấu hình</span>
        <strong class="admin-setting-stat__value">
          <c:out value="${settingTotal}" />
        </strong>
        <span class="admin-setting-stat__note">Tất cả key đang được quản lý</span>
      </div>

      <div class="admin-setting-stat admin-setting-stat--contact">
        <span class="admin-setting-stat__icon">☎️</span>
        <span class="admin-setting-stat__label">Thông tin liên hệ</span>
        <strong class="admin-setting-stat__value">
          <c:out value="${settingContactCount}" />
        </strong>
        <span class="admin-setting-stat__note">Hotline, email và địa chỉ</span>
      </div>

      <div class="admin-setting-stat admin-setting-stat--social">
        <span class="admin-setting-stat__icon">🌐</span>
        <span class="admin-setting-stat__label">Mạng xã hội</span>
        <strong class="admin-setting-stat__value">
          <c:out value="${settingSocialCount}" />
        </strong>
        <span class="admin-setting-stat__note">Facebook, Instagram</span>
      </div>

      <div class="admin-setting-stat admin-setting-stat--business">
        <span class="admin-setting-stat__icon">🏢</span>
        <span class="admin-setting-stat__label">Doanh nghiệp</span>
        <strong class="admin-setting-stat__value">
          <c:out value="${settingBusinessCount}" />
        </strong>
        <span class="admin-setting-stat__note">Tên công ty, MSDN và ngày cấp</span>
      </div>
    </section>

    <section class="admin-card admin-setting-list-card">
      <div class="admin-card__body">
        <div class="admin-setting-section-head">
          <div>
            <h2 class="admin-setting-section-title">Danh sách cấu hình</h2>
            <p class="admin-setting-section-desc">
              Kiểm tra nhanh các key đang được lưu trong hệ thống. Bấm chỉnh sửa để cập nhật toàn bộ thông tin website.
            </p>
          </div>

          <span class="admin-chip admin-chip--brand">
            <c:out value="${settingTotal}" /> key
          </span>
        </div>

        <c:choose>
          <c:when test="${empty settings}">
            <div class="admin-setting-empty">
              <div class="admin-setting-empty__icon">⚙️</div>
              <div>
                <h3>Chưa có cấu hình website</h3>
                <p>Hãy tạo hoặc seed dữ liệu settings trước khi hiển thị thông tin ngoài website.</p>
                <a class="admin-btn admin-btn--primary"
                   href="${ctx}/admin/settings?action=edit">
                  Chỉnh sửa cấu hình
                </a>
              </div>
            </div>
          </c:when>

          <c:otherwise>
            <div class="admin-setting-table-wrap">
              <table class="admin-table admin-setting-table">
                <thead>
                <tr>
                  <th class="admin-setting-col-group">Nhóm</th>
                  <th class="admin-setting-col-key">Key</th>
                  <th class="admin-setting-col-value">Giá trị</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="entry" items="${settings}">
                  <c:set var="entryKey" value="${fn:toLowerCase(entry.key)}" />

                  <tr>
                    <td>
                      <c:choose>
                        <c:when test="${entryKey == 'hotline' or entryKey == 'sales_email' or entryKey == 'hr_email' or entryKey == 'address'}">
                          <span class="admin-pill admin-pill--info">Liên hệ</span>
                        </c:when>
                        <c:when test="${entryKey == 'facebook' or entryKey == 'instagram'}">
                          <span class="admin-pill admin-pill--primary">Mạng xã hội</span>
                        </c:when>
                        <c:when test="${entryKey == 'company_name' or entryKey == 'business_code' or entryKey == 'business_date'}">
                          <span class="admin-pill admin-pill--warning">Doanh nghiệp</span>
                        </c:when>
                        <c:otherwise>
                          <span class="admin-pill admin-pill--ok">Website</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <div class="admin-setting-key-cell">
                        <strong><c:out value="${entry.key}"/></strong>
                      </div>
                    </td>

                    <td>
                      <c:choose>
                        <c:when test="${empty entry.value}">
                          <span class="admin-setting-empty-value">Chưa cấu hình</span>
                        </c:when>
                        <c:otherwise>
                          <div class="admin-setting-value-cell">
                            <c:out value="${entry.value}"/>
                          </div>
                        </c:otherwise>
                      </c:choose>
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

<jsp:include page="/jsp/admin/layout/footer.jsp"/>

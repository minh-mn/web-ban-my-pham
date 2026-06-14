<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 25/05/2026
  Time: 3:34 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<section style="padding: 50px 0; background: linear-gradient(135deg, var(--primary-soft) 0%, var(--primary-soft) 100%); text-align: center;">
  <div style="max-width:760px; margin: 0 auto; padding: 0 15px;">
    <h2 style="font-size: 36px; font-weight: 900; color: var(--primary); margin-bottom: 12px; letter-spacing: -0.5px;">
      Thương Hiệu Chính Hãng
    </h2>
    <p style="color: var(--text-muted); font-size: 16px; line-height: 1.7; margin: 0;">
      MyCosmetic là đối tác phân phối chiến lược của nhiều thương hiệu mỹ phẩm và chăm sóc da nổi tiếng toàn cầu.
      Chúng tôi cam kết 100% sản phẩm phân phối đạt chuẩn chính ngạch, an toàn và uy tín.
    </p>
  </div>
</section>

<section style="padding: 40px 0;">
  <div style="max-width: 1200px; margin: 0 auto; padding: 0 20px;">
    <%-- Danh sách thương hiệu --%>
    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 20px;">
      <c:forEach var="b" items="${brands}">
        <div style="padding: 20px; border: 1px solid var(--border-color); border-radius: var(--radius-md); background: var(--surface); transition: 0.3s;">
          <div style="width: 100%; height: 90px; display: flex; align-items: center; justify-content: center; margin-bottom: 20px; background: var(--surface);">
            <c:choose>
              <c:when test="${not empty b.image}">
                <img src="${pageContext.request.contextPath}${b.image}"
                     alt="${b.name}"
                     style="max-width: 100%; max-height: 100%; object-fit: contain; transition: all 0.3s;" />
              </c:when>
              <c:otherwise>
                <div style="width: 65px; height: 65px; border-radius: 50%; background: var(--primary-soft); color: var(--primary); display: flex; align-items: center; justify-content: center; font-weight: 800; font-size: 24px; border: 1px dashed var(--primary);">
                    ${fn:substring(b.name, 0, 1)}
                </div>
              </c:otherwise>
            </c:choose>
          </div>

          <h3 style="margin: 0 0 8px 0; font-size: 19px; font-weight: 700; color: var(--text-main); text-align: center; font-family: sans-serif;">
            <c:out value="${b.name}"/>
          </h3>
        </div>
      </c:forEach>
    </div>
  </div>
</section>

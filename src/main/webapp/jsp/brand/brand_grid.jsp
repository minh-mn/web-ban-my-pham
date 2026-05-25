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

<section style="padding: 50px 0; background: linear-gradient(135deg, #fff0f6 0%, #ffe3ec 100%); text-align: center;">
  <div style="max-width:760px; margin: 0 auto; padding: 0 15px;">
    <h2 style="font-size: 36px; font-weight: 900; color: #ff5fa2; margin-bottom: 12px; letter-spacing: -0.5px;">
      Thương Hiệu Chính Hãng
    </h2>
    <p style="color:#666; font-size:16px; line-height:1.7; margin: 0;">
      MyCosmetic là đối tác phân phối chiến lược của nhiều thương hiệu mỹ phẩm và chăm sóc da nổi tiếng toàn cầu.
      Chúng tôi cam kết 100% sản phẩm phân phối đạt chuẩn chính ngạch, an toàn và uy tín.
    </p>
  </div>
</section>

<section style="padding: 60px 0; background: #fff;">
  <div style="max-width: 1200px; margin: 0 auto; padding: 0 15px;">

    <c:choose>
      <c:when test="${empty brands}">
        <div style="text-align: center; color: #9ca3af; font-size: 16px; padding: 50px; font-style: italic;">
          Hệ thống đang cập nhật danh mục đối tác thương hiệu...
        </div>
      </c:when>

      <c:otherwise>
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 30px;">

          <c:forEach var="b" items="${brands}">
            <a href="${pageContext.request.contextPath}/products?brand=${b.id}"
               style="display: flex; flex-direction: column; align-items: center; justify-content: center; background: #fff; border: 1px solid #ffe3ec; border-radius: 20px; padding: 30px 20px; text-decoration: none; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); box-shadow: 0 4px 15px rgba(255, 95, 162, 0.03);"
               onmouseover="this.style.transform='translateY(-6px)'; this.style.borderColor='#ff5fa2'; this.style.boxShadow='0 12px 24px rgba(255, 95, 162, 0.15)';"
               onmouseout="this.style.transform='translateY(0)'; this.style.borderColor='#ffe3ec'; this.style.boxShadow='0 4px 15px rgba(255, 95, 162, 0.03)';">

              <div style="width: 100%; height: 90px; display: flex; align-items: center; justify-content: center; margin-bottom: 20px; background: #fff;">
                <c:choose>
                  <c:when test="${not empty b.image}">
                    <img src="${pageContext.request.contextPath}${b.image}"
                         alt="${b.name}"
                         style="max-width: 100%; max-height: 100%; object-fit: contain; transition: all 0.3s;" />
                  </c:when>
                  <c:otherwise>
                    <div style="width: 65px; height: 65px; border-radius: 50%; background: #fff0f6; color: #ff5fa2; display: flex; align-items: center; justify-content: center; font-weight: 800; font-size: 24px; border: 1px dashed #ff85bc;">
                        ${fn:substring(b.name, 0, 1)}
                    </div>
                  </c:otherwise>
                </c:choose>
              </div>

              <h3 style="margin: 0 0 8px 0; font-size: 19px; font-weight: 700; color: #333; text-align: center; font-family: sans-serif;">
                <c:out value="${b.name}"/>
              </h3>

              <span style="font-size: 13px; color: #ff5fa2; background: #fff0f6; padding: 4px 14px; border-radius: 30px; font-weight: 600; border: 1px solid #ffd1e3;">
                ${b.productCount} sản phẩm
              </span>

            </a>
          </c:forEach>

        </div>
      </c:otherwise>
    </c:choose>

  </div>
</section>

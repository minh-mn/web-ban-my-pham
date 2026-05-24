<%--
  Policy detail page
  Hiển thị file chính sách từ thư mục MyCosmeticShopUploads thông qua UploadServlet.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>${policyTitle} - MyCosmetic</title>

  <style>
    .policy-container {
      max-width: 1000px;
      margin: 40px auto;
      padding: 20px;
      font-family: Arial, sans-serif;
    }

    .policy-header {
      border-bottom: 2px solid #ff5fa2;
      margin-bottom: 25px;
      padding-bottom: 10px;
    }

    .policy-header h1 {
      color: #333;
      font-size: 24px;
      text-transform: uppercase;
      margin: 0;
    }

    .policy-wrapper {
      position: relative;
      background: #f9f9f9;
      border: 1px solid #ddd;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .policy-frame {
      width: 100%;
      height: 650px;
      border: 0;
      display: block;
      background: #fff;
    }

    .policy-image {
      max-width: 100%;
      width: auto;
      height: auto;
      display: block;
      margin: 0 auto;
      background: #fff;
    }

    .btn-back {
      display: inline-block;
      margin-bottom: 20px;
      color: #ff5fa2;
      text-decoration: none;
      font-weight: bold;
    }

    .btn-back:hover {
      text-decoration: underline;
    }

    .policy-note {
      margin-top: 20px;
      color: #666;
      font-style: italic;
    }

    .policy-empty {
      padding: 30px;
      text-align: center;
      color: #777;
      background: #fff;
    }
  </style>
</head>

<body>
<div class="policy-container">
  <a href="${pageContext.request.contextPath}/home" class="btn-back">⬅ Quay lại trang chủ</a>

  <div class="policy-header">
    <h1>${policyTitle}</h1>
  </div>

  <c:set var="publicFileUrl" value="${pageContext.request.contextPath}${fileUrl}" />
  <c:set var="lowerFileName" value="${fn:toLowerCase(fileName)}" />

  <div class="policy-wrapper">
    <c:choose>
      <c:when test="${empty fileUrl}">
        <div class="policy-empty">
          Không tìm thấy file chính sách.
        </div>
      </c:when>

      <c:when test="${fn:endsWith(lowerFileName, '.pdf')}">
        <iframe
                class="policy-frame"
                src="${publicFileUrl}"
                title="${policyTitle}">
        </iframe>
      </c:when>

      <c:otherwise>
        <img
                class="policy-image"
                src="${publicFileUrl}"
                alt="${policyTitle}">
      </c:otherwise>
    </c:choose>
  </div>

  <p class="policy-note">
    * Nếu không xem được nội dung, vui lòng làm mới trang hoặc liên hệ bộ phận hỗ trợ.
  </p>
</div>
</body>
</html>
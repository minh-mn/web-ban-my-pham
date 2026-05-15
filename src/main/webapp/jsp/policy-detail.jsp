<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 27/04/2026
  Time: 7:21 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
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
    }
    .ppt-wrapper {
      position: relative;
      background: #f9f9f9;
      border: 1px solid #ddd;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }
    .btn-back {
      display: inline-block;
      margin-bottom: 20px;
      color: #ff5fa2;
      text-decoration: none;
      font-weight: bold;
    }
  </style>
</head>
<body>
<div class="policy-container">
  <a href="${pageContext.request.contextPath}/home" class="btn-back"> ⬅ Quay lại trang chủ</a>

  <div class="policy-header">
    <h1>${policyTitle}</h1>
  </div>

  <div class="ppt-wrapper">
    <iframe
            src="https://docs.google.com/gview?url=https://yourdomain.com/uploads/policy/${fileName}&embedded=true"
            style="width: 100%; height: 650px;"
            frameborder="0">
    </iframe>
  </div>

  <p style="margin-top: 20px; color: #666; font-style: italic;">
    * Nếu không xem được nội dung, vui lòng làm mới trang hoặc liên lạc với bộ phận hỗ trợ.
  </p>
</div>
</body>
</html>

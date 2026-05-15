<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 25/04/2026
  Time: 6:20 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<section class="auth-page">
  <div class="auth-card">
    <h2 class="auth-title">Xác thực Email</h2>
    <p class="auth-subtitle">Chúng tôi đã gửi mã OTP đến email: <strong>${sessionScope.pendingUser.email}</strong></p>

    <form method="post" action="${pageContext.request.contextPath}/verify-registration">
      <div class="form-group">
        <label>Nhập mã xác thực (6 số)</label>
        <div class="input-group">
          <input type="text" name="otp_input" required maxlength="6"
                style="text-align: center; font-size: 24px; letter-spacing: 8px">
        </div>
        <c:if test="${not empty error}">
          <small class="error-msg" style="display: block">${error}</small>
        </c:if>
      </div>
      <button type="submit" class="btn-auth">Xác nhận hoàn tất</button>
    </form>
  </div>
</section>

</body>
</html>

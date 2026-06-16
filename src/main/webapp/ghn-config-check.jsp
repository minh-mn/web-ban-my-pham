<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.webshop.app.utils.GHNConfig" %>
<%@ page import="com.webshop.app.service.GHNShippingService" %>
<%@ page import="com.webshop.app.service.GHNShippingService.FeeResult" %>
<%@ page import="java.math.BigDecimal" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>GHN Config Check</title>
  <style>
    body { font-family: Arial, sans-serif; padding: 28px; line-height: 1.7; background: #fff7fb; color: #111827; }
    .box { max-width: 900px; margin: 0 auto; background: white; border: 1px solid #ffd6df; border-radius: 18px; padding: 22px; }
    code { background: #f8fafc; padding: 3px 7px; border-radius: 8px; }
    .ok { color: #15803d; font-weight: 800; }
    .bad { color: #be123c; font-weight: 800; }
  </style>
</head>
<body>
<div class="box">
  <h1>GHN Config Check</h1>

  <p><b>Configured:</b>
    <span class="<%= GHNConfig.isConfigured() ? "ok" : "bad" %>">
      <%= GHNConfig.isConfigured() %>
    </span>
  </p>

  <p><b>Base URL:</b> <code><%= GHNConfig.GHN_BASE_URL %></code></p>
  <p><b>Shop ID:</b> <code><%= GHNConfig.GHN_SHOP_ID %></code></p>
  <p><b>Token:</b> <code><%= GHNConfig.maskedToken() %></code></p>

  <hr>

  <p><b>From district ID:</b> <code><%= GHNConfig.FROM_DISTRICT_ID %></code></p>
  <p><b>From ward code:</b> <code><%= GHNConfig.FROM_WARD_CODE %></code></p>
  <p><b>Demo to district ID:</b> <code><%= GHNConfig.DEMO_TO_DISTRICT_ID %></code></p>
  <p><b>Demo to ward code:</b> <code><%= GHNConfig.DEMO_TO_WARD_CODE %></code></p>

  <hr>

  <%
    GHNShippingService service = new GHNShippingService();
    FeeResult feeResult = service.calculateDemoFee(new BigDecimal("100000"));
  %>

  <p><b>Test calculate fee:</b>
    <% if (feeResult.isAvailable()) { %>
    <span class="ok">OK - <%= feeResult.getTotalFee() %>đ, service_id=<%= feeResult.getServiceId() %></span>
    <% } else { %>
    <span class="bad">Chưa gọi được API: <%= feeResult.getMessage() %></span>
    <% } %>
  </p>

  <p>
    Nếu Configured=false hoặc test fee lỗi, kiểm tra lại
    <code>src/main/resources/ghn.properties</code> rồi rebuild artifact.
  </p>
</div>
</body>
</html>

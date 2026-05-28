<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 23/05/2026
  Time: 4:00 CH
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<style>
  /* Khung bao bọc tạo nền xám nhạt toàn trang */
  .policy-page-wrapper {
    background-color: #f8f9fa;
    padding: 40px 0;
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  }

  /* Khung chứa nội dung chính dạng hộp trắng nổi bật */
  .policy-card {
    max-width: 960px;
    margin: 0 auto;
    background: #ffffff;
    padding: 45px 50px;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  }

  /* Thanh điều hướng nhỏ đầu trang (Breadcrumb) */
  .policy-breadcrumb {
    margin-bottom: 25px;
    font-size: 14px;
    color: #777777;
  }
  .policy-breadcrumb a {
    color: #ff5fa2;
    text-decoration: none;
    font-weight: 500;
  }
  .policy-breadcrumb span {
    margin: 0 8px;
    color: #cccccc;
  }

  /* Thiết lập lại tiêu đề chính */
  .policy-main-title {
    font-size: 30px;
    color: #222222;
    font-weight: 700;
    margin-top: 0;
    margin-bottom: 15px;
    line-height: 1.4;
    padding-bottom: 20px;
    border-bottom: 2px solid #f1f1f1;
  }

  /* Dòng thông tin phụ dưới tiêu đề */
  .policy-sub-meta {
    font-size: 13px;
    color: #999999;
    margin-bottom: 30px;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  /* VÙNG CHỨA NỘI DUNG CHÍNH (Từ Admin nhập vào) */
  .policy-article-content {
    font-size: 16px;
    color: #444444;
    line-height: 1.8;
    text-align: justify;
    /* QUAN TRỌNG: Giữ nguyên định dạng xuống dòng và khoảng cách từ Admin gõ */
    white-space: pre-line;
  }

  /* Định dạng các phần tiêu đề số (1., 2., 3.) tự động đậm lên nếu admin viết */
  .policy-article-content strong,
  .policy-article-content b {
    color: #111111;
    font-size: 17px;
    display: inline-block;
    margin-top: 15px;
  }

  /* Nút quay lại mua sắm ở cuối bài */
  .policy-action-bottom {
    margin-top: 40px;
    border-top: 1px solid #eeeeee;
    padding-top: 25px;
    text-align: center;
  }
  .btn-policy-back {
    display: inline-block;
    padding: 10px 28px;
    background-color: #333333;
    color: #ffffff;
    text-decoration: none;
    border-radius: 25px;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.3s ease;
  }
  .btn-policy-back:hover {
    background-color: #ff5fa2;
    transform: translateY(-2px);
    color: #ffffff;
  }
</style>

<div class="policy-page-wrapper">
  <div class="policy-card">

    <div class="policy-breadcrumb">
      <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
      <span>/</span>
      <span>Chính sách cửa hàng</span>
    </div>

    <h1 class="policy-main-title">
      <c:out value="${page.title}" />
    </h1>

    <div class="policy-sub-meta">
      <span>📅 Điều khoản áp dụng chính thức | Hệ thống MyCosmetic Shop</span>
    </div>

    <div class="policy-article-content">
      <c:out value="${page.content}" />
    </div>

    <div class="policy-action-bottom">
      <a href="${pageContext.request.contextPath}/home" class="btn-policy-back">
        ‹ Quay lại trang chủ
      </a>
    </div>

  </div>
</div>
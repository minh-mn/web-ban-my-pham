<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<style>
    .site-footer-v2 {
        background-color: #1a1a1a;
        color: #ffffff;
        padding: 50px 0 0;
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        line-height: 1.6;
        margin-top: 40px;
    }

    .footer-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 20px;
    }

    .footer-grid {
        display: grid;
        grid-template-columns: 2fr 1fr 1.5fr 2fr; 
        gap: 30px;
        padding-bottom: 40px;
    }

    .footer-col h4 {
        color: #ffffff;
        font-size: 14px;
        font-weight: bold;
        margin-bottom: 20px;
        text-transform: uppercase;
        letter-spacing: 0.5px;
    }

    .footer-col ul {
        list-style: none;
        padding: 0;
        margin: 0;
    }

    .footer-col ul li {
        margin-bottom: 8px;
        font-size: 13px;
        color: #bbbbbb;
    }

    .footer-col ul li a {
        color: #bbbbbb;
        text-decoration: none;
        transition: 0.3s;
    }

    .footer-col ul li a:hover {
        color: #ff4d94;
        padding-left: 5px;
    }

    .company-info {
        font-size: 12px;
        color: #888;
        margin-top: 15px;
        border-top: 1px solid #333;
        padding-top: 15px;
    }

    /* Newsletter */
    .footer-newsletter {
        display: flex;
        border-bottom: 1px solid #444;
        padding: 5px 0;
        margin-bottom: 20px;
        align-items: center;
    }

    .footer-newsletter input {
        background: transparent;
        border: none;
        color: #fff;
        flex: 1;
        outline: none;
        font-size: 13px;
    }

    .footer-newsletter button {
        background: transparent;
        border: none;
        color: #fff;
        cursor: pointer;
        padding: 0 5px;
    }

    /* Social Icons */
    .footer-social {
        display: flex;
        gap: 12px;
        margin-bottom: 25px;
    }

    .social-icon {
        width: 35px;
        height: 35px;
        background: #333;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        color: #fff;
        text-decoration: none;
        font-size: 16px;
        transition: 0.3s ease;
    }

    .social-icon:hover {
        background: #ff4d94;
        transform: translateY(-3px);
    }

    /* Badges & Trust */
    .footer-trust {
        display: flex;
        flex-direction: column;
        gap: 15px;
    }

    .bct-badge {
        width: 150px;
        height: auto;
        display: block;
        transition: 0.3s;
    }
    
    .bct-badge:hover { opacity: 0.8; }

    .store-system {
        font-size: 13px;
        font-weight: bold;
        color: #fff;
        text-transform: uppercase;
        border: 1px solid #444;
        padding: 8px 12px;
        display: inline-block;
        border-radius: 4px;
        text-decoration: none;
        text-align: center;
    }

    .store-system:hover {
        border-color: #ff4d94;
        color: #ff4d94;
    }

    .footer-bottom {
        background-color: #111111;
        padding: 20px 0;
        text-align: center;
        border-top: 1px solid #222;
        color: #666;
        font-size: 12px;
    }
</style>

<footer class="site-footer-v2">
    <div class="footer-container">
        <div class="footer-grid">
            
            <div class="footer-col">
                <h4>Thông tin liên hệ</h4>
                <ul>
                    <li><i class="fa-solid fa-phone" style="margin-right: 8px;"></i> Hotline: 1900 636 510 (8:00 - 21:00)</li>
                    <li><i class="fa-solid fa-envelope" style="margin-right: 8px;"></i> sales@mycosmetic.com</li>
                    <li><i class="fa-solid fa-briefcase" style="margin-right: 8px;"></i> hr@mycosmetic.com</li>
                </ul>
                <div class="company-info">
                    <strong>CÔNG TY TNHH MYCOSMETIC SHOP</strong><br>
                    MSDN: 0316037655 - Cấp ngày: 21/03/2026<br>
                    <i class="fa-solid fa-location-dot"></i> Khu Công Nghệ Phần Mềm ĐHQG, Dĩ An, Bình Dương.
                </div>
            </div>

            <div class="footer-col">
                <h4>Danh mục</h4>
                <ul>
                    <li><a href="${pageContext.request.contextPath}/">Trang chủ</a></li>
                    <li><a href="#">Sản phẩm mới</a></li>
                    <li><a href="#">Combo khuyến mãi</a></li>
                    <li><a href="#">Chăm sóc da</a></li>
                    <li><a href="#">Trang điểm</a></li>
                </ul>
            </div>

            <div class="footer-col">
                <h4>Chính sách</h4>
                <ul>
                    <li><a href="#">Giới thiệu</a></li>
                    <li><a href="#">Chính sách đổi trả</a></li>
                    <li><a href="#">Chính sách bảo mật</a></li>
                    <li><a href="#">Điều khoản sử dụng</a></li>
                    <li><a href="#">Yêu cầu xuất hóa đơn (VAT)</a></li>
                </ul>
            </div>

            <div class="footer-col">
                <h4>Kết nối với chúng tôi</h4>
                <div class="footer-newsletter">
                    <input type="email" placeholder="Nhập email của bạn...">
                    <button type="button"><i class="fa-solid fa-paper-plane"></i></button>
                </div>
                
                <div class="footer-social">
                    <a href="#" class="social-icon"><i class="fa-brands fa-facebook-f"></i></a>
                    <a href="#" class="social-icon"><i class="fa-brands fa-instagram"></i></a>
                    <a href="#" class="social-icon"><i class="fa-brands fa-tiktok"></i></a>
                    <a href="#" class="social-icon"><i class="fa-solid fa-comment-dots"></i></a> </div>

                <div class="footer-trust">
                    <a href="http://online.gov.vn/">
                        <img src="https://thegioiskinfood.com/static/media/logoSaleNoti.6229020e.png" alt="Bộ Công Thương" class="bct-badge">
                    </a>
                    <a href="#" class="store-system">
                        <i class="fa-solid fa-shop"></i> Hệ thống cửa hàng
                    </a>
                </div>
            </div>

        </div>
    </div>

    <div class="footer-bottom">
        <div class="footer-container">
            Copyright © 2026 MyCosmetic Shop — All rights reserved [cite: 1, 25]
        </div>
    </div>
</footer>
</body>
</html>
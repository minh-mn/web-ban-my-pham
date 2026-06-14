<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

<style>
    .site-footer-v2 {
        margin-top: 70px;
        padding: 58px 0 0;
        color: #f8fafc;
        background:
                radial-gradient(circle at 18% 0%, rgba(240, 98, 146, 0.16) 0 240px, transparent 410px),
                radial-gradient(circle at 92% 18%, rgba(79, 123, 217, 0.14) 0 230px, transparent 430px),
                linear-gradient(180deg, #111827 0%, #060913 100%);
        border-top: 1px solid rgba(255,255,255,.10);
    }
    .footer-container { max-width: 1240px; margin: 0 auto; padding: 0 24px; }
    .footer-grid { display: grid; grid-template-columns: 1.45fr 1fr 1.35fr 1.35fr; gap: 34px; padding-bottom: 42px; }
    .footer-col h4 { position: relative; margin: 0 0 18px; padding-bottom: 12px; color: #fff; font-size: 13px; font-weight: 900; text-transform: uppercase; letter-spacing: .08em; }
    .footer-col h4::after { content:""; position:absolute; left:0; bottom:0; width:42px; height:2px; background: linear-gradient(90deg,#f06292,transparent); border-radius:99px; }
    .footer-col ul { list-style: none; padding: 0; margin: 0; }
    .footer-col li { margin-bottom: 11px; color: #a8b3c5; font-size: 13px; line-height: 1.65; display: flex; align-items: flex-start; gap: 9px; }
    .footer-col li i { width: 16px; margin-top: 4px; color: #f06292; }
    .footer-col a { color: #a8b3c5; text-decoration: none; transition: .2s ease; }
    .footer-col a:hover { color: #fff; }
    .company-info { margin-top: 18px; padding-top: 18px; border-top: 1px solid rgba(255,255,255,.10); color: #a8b3c5; font-size: 12px; line-height: 1.75; }
    .company-info strong { display: inline-block; color:#fff; margin-bottom:4px; }
    .footer-policy-list { display:grid; gap:7px; }
    .footer-policy-list li { margin-bottom:0; }
    .footer-policy-link { width:100%; min-height:34px; padding:7px 10px; border-radius:11px; display:flex; align-items:center; gap:9px; color:#a8b3c5!important; border:1px solid transparent; }
    .footer-policy-link:hover { color:#fff!important; background:rgba(255,255,255,.075); border-color:rgba(255,255,255,.12); }
    .footer-policy-link i { margin-top:0!important; font-size:12px; }
    .footer-newsletter { display:flex; align-items:center; gap:8px; min-height:48px; padding:6px 7px 6px 16px; margin-bottom:18px; border-radius:999px; border:1px solid rgba(255,255,255,.16); background:rgba(255,255,255,.06); }
    .footer-newsletter input { flex:1; min-width:0; border:none; outline:none; background:transparent; color:#fff; font-size:13px; }
    .footer-newsletter button { border:none; width:36px; height:36px; border-radius:50%; background:linear-gradient(135deg,#f06292,#e11d48); color:#fff; cursor:pointer; }
    .footer-social { display:flex; flex-wrap:wrap; gap:10px; margin-bottom:18px; }
    .social-icon { width:38px; height:38px; border-radius:50%; display:inline-flex; align-items:center; justify-content:center; color:#a8b3c5; background:rgba(255,255,255,.06); border:1px solid rgba(255,255,255,.16); }
    .store-system { display:inline-flex; align-items:center; gap:8px; padding:9px 13px; border-radius:12px; color:#fff!important; background:rgba(240,98,146,.13); border:1px solid rgba(240,98,146,.36); font-size:12px; font-weight:800; }
    .footer-bottom { padding:18px 0; border-top:1px solid rgba(255,255,255,.10); background:rgba(0,0,0,.18); color:#7f8aa0; text-align:center; font-size:12px; }
    @media(max-width:1080px){.footer-grid{grid-template-columns:1fr 1fr}}
    @media(max-width:640px){.footer-grid{grid-template-columns:1fr}.site-footer-v2{padding-top:42px}.footer-container{padding:0 18px}}
</style>

<footer class="site-footer-v2">
    <div class="footer-container">
        <div class="footer-grid">
            <div class="footer-col">
                <h4>Thông tin liên hệ</h4>
                <ul>
                    <li><i class="fa-solid fa-phone"></i><span>Hotline: ${settings.hotline}</span></li>
                    <li><i class="fa-solid fa-envelope"></i><span>Email: ${settings.sales_email}</span></li>
                    <li><i class="fa-solid fa-user-tie"></i><span>HR: ${settings.hr_email}</span></li>
                </ul>
                <div class="company-info">
                    <strong>${settings.company_name}</strong><br>
                    MSDN: ${settings.business_code} - Cấp ngày: ${settings.business_date}<br>
                    <i class="fa-solid fa-location-dot"></i> ${settings.address}
                </div>
            </div>

            <div class="footer-col">
                <h4>Danh mục</h4>
                <ul>
                    <li><a href="${pageContext.request.contextPath}/home">TRANG CHỦ</a></li>
                    <li><a href="${pageContext.request.contextPath}/products?category=all">SẢN PHẨM</a></li>
                    <li><a href="${pageContext.request.contextPath}/products?category=brand">THƯƠNG HIỆU</a></li>
                    <li><a href="${pageContext.request.contextPath}/flash-sale">FLASH SALE</a></li>
                    <li><a href="${pageContext.request.contextPath}/promotions">KHUYẾN MÃI</a></li>
                    <li><a href="${pageContext.request.contextPath}/blog">TIN TỨC</a></li>
                    <li><a href="${pageContext.request.contextPath}/lien-he">LIÊN HỆ</a></li>
                </ul>
            </div>

            <div class="footer-col">
                <h4>Chính sách</h4>
                <ul class="footer-policy-list">
                    <li><a class="footer-policy-link" href="${pageContext.request.contextPath}/policy/cancel"><i class="fa-solid fa-ban"></i>Chính sách hủy đơn</a></li>
                    <li><a class="footer-policy-link" href="${pageContext.request.contextPath}/policy/return"><i class="fa-solid fa-rotate-left"></i>Chính sách hoàn hàng</a></li>
                    <li><a class="footer-policy-link" href="${pageContext.request.contextPath}/policy/privacy"><i class="fa-solid fa-shield-halved"></i>Chính sách bảo mật</a></li>
                    <li><a class="footer-policy-link" href="${pageContext.request.contextPath}/policy/payment"><i class="fa-solid fa-credit-card"></i>Chính sách thanh toán</a></li>
                    <li><a class="footer-policy-link" href="${pageContext.request.contextPath}/policy/terms"><i class="fa-solid fa-file-contract"></i>Điều khoản dịch vụ</a></li>
                    <li><a class="footer-policy-link" href="${pageContext.request.contextPath}/policy/shopping-guide"><i class="fa-solid fa-bag-shopping"></i>Hướng dẫn mua hàng</a></li>
                    <li><a class="footer-policy-link" href="${pageContext.request.contextPath}/policy/vnpay-guide"><i class="fa-solid fa-wallet"></i>Hướng dẫn VNPay</a></li>
                </ul>
            </div>

            <div class="footer-col">
                <h4>Kết nối với chúng tôi</h4>
                <form action="${pageContext.request.contextPath}/contact" method="post" class="footer-newsletter">
                    <input type="email" name="email" placeholder="Nhập email của bạn..." required>
                    <button type="submit" aria-label="Gửi email"><i class="fa-solid fa-paper-plane"></i></button>
                </form>
                <div class="footer-social">
                    <a href="${settings.facebook}" class="social-icon"><i class="fa-brands fa-facebook-f"></i></a>
                    <a href="${settings.instagram}" class="social-icon"><i class="fa-brands fa-instagram"></i></a>
                    <a href="${settings.tiktok}" class="social-icon"><i class="fa-brands fa-tiktok"></i></a>
                    <a href="${pageContext.request.contextPath}/lien-he" class="social-icon"><i class="fa-solid fa-comment-dots"></i></a>
                </div>
                <a href="#" class="store-system"><i class="fa-solid fa-shop"></i> Hệ thống cửa hàng</a>
            </div>
        </div>
    </div>

    <div class="footer-bottom">
        <div class="footer-container">
            Copyright © ${settings.name_website} ${settings.copyright_year} – All rights reserved
        </div>
    </div>
</footer>

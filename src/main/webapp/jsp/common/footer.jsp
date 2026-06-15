<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

<style>
    /* =========================================================
       FOOTER - GIU BO CUC CU, DOI NEN DEN + KY HIEU NOI BAT
    ========================================================= */
    .site-footer-v2 {
        margin-top: 70px;
        padding: 54px 0 0;
        color: #f8f8fb;
        background:
                linear-gradient(180deg, rgba(255,255,255,0.02), rgba(255,255,255,0.0)),
                linear-gradient(135deg, #171717 0%, #0e0e0f 48%, #050505 100%);
        border-top: 1px solid rgba(255,255,255,.07);
    }

    .footer-container {
        max-width: 1260px;
        margin: 0 auto;
        padding: 0 24px;
    }

    .footer-grid {
        display: grid;
        grid-template-columns: 1.45fr 1fr 1.35fr 1.35fr;
        gap: 38px;
        padding-bottom: 44px;
        align-items: flex-start;
    }

    .footer-col h4 {
        position: relative;
        margin: 0 0 22px;
        padding-bottom: 13px;
        color: #ffffff;
        font-size: 14px;
        font-weight: 950;
        text-transform: uppercase;
        letter-spacing: .08em;
    }

    .footer-col h4::after {
        content: "";
        position: absolute;
        left: 0;
        bottom: 0;
        width: 46px;
        height: 2px;
        border-radius: 999px;
        background: linear-gradient(90deg, #ff2d55 0%, rgba(255,45,85,0) 100%);
    }

    .footer-col ul {
        list-style: none;
        padding: 0;
        margin: 0;
    }

    .footer-col li {
        margin-bottom: 13px;
        color: #c8cbd4;
        font-size: 14px;
        line-height: 1.65;
        display: flex;
        align-items: flex-start;
        gap: 10px;
    }

    .footer-col li i {
        width: 17px;
        margin-top: 4px;
        color: #ff3b5c;
    }

    .footer-col a {
        color: #d0d4dc;
        text-decoration: none;
        transition: .22s ease;
    }

    .footer-col a:hover {
        color: #ffffff;
        transform: translateX(2px);
    }

    .footer-contact-label {
        min-width: 62px;
        color: #f0f2f7;
        font-weight: 800;
    }

    .footer-contact-value {
        color: #ffffff;
        font-weight: 650;
        word-break: break-word;
    }

    .company-info {
        margin-top: 22px;
        padding-top: 20px;
        border-top: 1px solid rgba(255,255,255,.10);
        color: #c3c8d2;
        font-size: 13px;
        line-height: 1.8;
    }

    .company-info strong {
        display: inline-block;
        color: #ffffff;
        margin-bottom: 6px;
        font-size: 14px;
        font-weight: 900;
    }

    .company-info .company-line {
        display: flex;
        gap: 9px;
        align-items: flex-start;
        margin-top: 6px;
    }

    .company-info .company-line i {
        width: 16px;
        margin-top: 4px;
        color: #ff3b5c;
    }

    .footer-policy-list {
        display: grid;
        gap: 8px;
    }

    .footer-policy-list li {
        margin-bottom: 0;
    }

    .footer-policy-link {
        width: 100%;
        min-height: 36px;
        padding: 8px 10px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        gap: 10px;
        color: #cdd2dc !important;
        border: 1px solid transparent;
    }

    .footer-policy-link:hover {
        color: #ffffff !important;
        background: rgba(255,255,255,.055);
        border-color: rgba(255,59,92,.18);
    }

    .footer-policy-link i {
        margin-top: 0 !important;
        font-size: 12px;
        color: #ff3b5c;
    }

    .footer-newsletter {
        display: flex;
        align-items: center;
        gap: 8px;
        min-height: 48px;
        padding: 6px 7px 6px 17px;
        margin-bottom: 20px;
        border-radius: 999px;
        border: 1px solid rgba(255,255,255,.14);
        background: rgba(255,255,255,.03);
        box-shadow: inset 0 1px 0 rgba(255,255,255,.03);
    }

    .footer-newsletter input {
        flex: 1;
        min-width: 0;
        border: none;
        outline: none;
        background: transparent;
        color: #ffffff;
        font-size: 13px;
    }

    .footer-newsletter input::placeholder {
        color: #8c909a;
    }

    .footer-newsletter button {
        border: none;
        width: 36px;
        height: 36px;
        border-radius: 50%;
        background: linear-gradient(135deg, #ff4d6d 0%, #ff2d55 55%, #db0938 100%);
        color: #ffffff;
        cursor: pointer;
        box-shadow: 0 10px 22px rgba(255, 45, 85, .28);
        transition: .22s ease;
    }

    .footer-newsletter button:hover {
        transform: translateY(-1px);
        box-shadow: 0 14px 28px rgba(255, 45, 85, .34);
    }

    .footer-social {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        margin-bottom: 20px;
    }

    .social-icon {
        width: 38px;
        height: 38px;
        border-radius: 50%;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        color: #ff3b5c;
        background: rgba(255,255,255,.035);
        border: 1px solid rgba(255,255,255,.12);
        transition: .22s ease;
    }

    .social-icon:hover {
        color: #ffffff;
        background: rgba(255, 59, 92, .18);
        border-color: rgba(255, 59, 92, .40);
        transform: translateY(-2px);
        box-shadow: 0 8px 18px rgba(255, 45, 85, .16);
    }

    .store-system {
        display: inline-flex;
        align-items: center;
        gap: 9px;
        padding: 10px 14px;
        border-radius: 13px;
        color: #ffffff !important;
        background: rgba(255, 59, 92, .10);
        border: 1px solid rgba(255, 59, 92, .32);
        font-size: 12px;
        font-weight: 850;
    }

    .store-system i { color: #ff4d6d !important; }

    .store-system:hover {
        background: rgba(255, 59, 92, .18);
        transform: none !important;
    }

    .footer-extra-info {
        margin-top: 16px;
        display: grid;
        gap: 10px;
        color: #c7cbd4;
        font-size: 13px;
        line-height: 1.6;
    }

    .footer-extra-info div {
        display: flex;
        gap: 9px;
        align-items: flex-start;
    }

    .footer-extra-info i {
        margin-top: 4px;
        color: #ff3b5c;
    }

    .footer-bottom {
        padding: 18px 0;
        border-top: 1px solid rgba(255,255,255,.08);
        background: #050505;
        color: #9aa1ad;
        text-align: center;
        font-size: 12px;
    }

    .footer-bottom-inner {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 18px;
        flex-wrap: wrap;
    }

    .footer-bottom-links {
        display: inline-flex;
        align-items: center;
        gap: 14px;
        flex-wrap: wrap;
    }

    .footer-bottom-links a {
        color: #b2b8c3;
        text-decoration: none;
    }

    .footer-bottom-links a:hover {
        color: #ffffff;
    }

    @media(max-width:1080px) {
        .footer-grid {
            grid-template-columns: 1fr 1fr;
            gap: 30px;
        }
    }

    @media(max-width:640px) {
        .site-footer-v2 {
            padding-top: 42px;
        }

        .footer-container {
            padding: 0 18px;
        }

        .footer-grid {
            grid-template-columns: 1fr;
            gap: 26px;
        }

        .footer-bottom-inner {
            flex-direction: column;
            gap: 10px;
        }
    }

    /* FOOTER WHITE BOLD ICONS FINAL */
    .footer-col li i,
    .company-info .company-line i,
    .footer-policy-link i,
    .footer-extra-info i,
    .footer-mini-info i,
    .store-system i,
    .footer-social i,
    .social-icon i {
        color: #ffffff !important;
        font-weight: 900 !important;
        text-shadow: 0 0 10px rgba(255, 255, 255, .22) !important;
    }

    .footer-contact-item i {
        color: #ffffff !important;
        background: rgba(255, 255, 255, .10) !important;
        border: 1px solid rgba(255, 255, 255, .22) !important;
        box-shadow: 0 8px 18px rgba(255, 255, 255, .08) !important;
    }

    .social-icon {
        color: #ffffff !important;
        background: rgba(255, 255, 255, .055) !important;
        border-color: rgba(255, 255, 255, .18) !important;
    }

    .social-icon:hover {
        color: #ffffff !important;
        background: rgba(255, 255, 255, .12) !important;
        border-color: rgba(255, 255, 255, .32) !important;
        box-shadow: 0 8px 18px rgba(255, 255, 255, .10) !important;
    }

    .store-system {
        border-color: rgba(255, 255, 255, .24) !important;
    }

</style>

<footer class="site-footer-v2">
    <div class="footer-container">
        <div class="footer-grid">
            <div class="footer-col">
                <h4>Thông tin liên hệ</h4>
                <ul>
                    <li>
                        <i class="fa-solid fa-phone"></i>
                        <span>
                            <span class="footer-contact-label">Hotline:</span>
                            <span class="footer-contact-value">
                                <c:choose>
                                    <c:when test="${not empty settings.hotline}">${settings.hotline}</c:when>
                                    <c:otherwise>0909 123 456</c:otherwise>
                                </c:choose>
                            </span>
                        </span>
                    </li>
                    <li>
                        <i class="fa-solid fa-envelope"></i>
                        <span>
                            <span class="footer-contact-label">Email:</span>
                            <span class="footer-contact-value">
                                <c:choose>
                                    <c:when test="${not empty settings.sales_email}">${settings.sales_email}</c:when>
                                    <c:otherwise>support@mycosmetic.vn</c:otherwise>
                                </c:choose>
                            </span>
                        </span>
                    </li>
                    <li>
                        <i class="fa-solid fa-user-tie"></i>
                        <span>
                            <span class="footer-contact-label">HR:</span>
                            <span class="footer-contact-value">
                                <c:choose>
                                    <c:when test="${not empty settings.hr_email}">${settings.hr_email}</c:when>
                                    <c:otherwise>hr@mycosmetic.vn</c:otherwise>
                                </c:choose>
                            </span>
                        </span>
                    </li>
                </ul>

                <div class="company-info">
                    <strong>
                        <c:choose>
                            <c:when test="${not empty settings.company_name}">${settings.company_name}</c:when>
                            <c:otherwise>Công ty TNHH MyCosmetic Việt Nam</c:otherwise>
                        </c:choose>
                    </strong><br>

                    MSDN:
                    <c:choose>
                        <c:when test="${not empty settings.business_code}">${settings.business_code}</c:when>
                        <c:otherwise>0312345678</c:otherwise>
                    </c:choose>
                    - Cấp ngày:
                    <c:choose>
                        <c:when test="${not empty settings.business_date}">${settings.business_date}</c:when>
                        <c:otherwise>15/06/2026</c:otherwise>
                    </c:choose>

                    <div class="company-line">
                        <i class="fa-solid fa-location-dot"></i>
                        <span>
                            <c:choose>
                                <c:when test="${not empty settings.address}">${settings.address}</c:when>
                                <c:otherwise>TP. Hồ Chí Minh, Việt Nam</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
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
                    <li><a href="${pageContext.request.contextPath}/vouchers">VOUCHER</a></li>
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
                    <li><a class="footer-policy-link" href="${pageContext.request.contextPath}/policy/shipping"><i class="fa-solid fa-truck-fast"></i>Chính sách vận chuyển</a></li>
                </ul>
            </div>

            <div class="footer-col">
                <h4>Kết nối với chúng tôi</h4>
                <form action="${pageContext.request.contextPath}/contact" method="post" class="footer-newsletter">
                    <input type="email" name="email" placeholder="Nhập email của bạn..." required>
                    <button type="submit" aria-label="Gửi email"><i class="fa-solid fa-paper-plane"></i></button>
                </form>

                <div class="footer-social">
                    <a href="${settings.facebook}" class="social-icon" aria-label="Facebook"><i class="fa-brands fa-facebook-f"></i></a>
                    <a href="${settings.instagram}" class="social-icon" aria-label="Instagram"><i class="fa-brands fa-instagram"></i></a>
                    <a href="${settings.tiktok}" class="social-icon" aria-label="TikTok"><i class="fa-brands fa-tiktok"></i></a>
                    <a href="${pageContext.request.contextPath}/lien-he" class="social-icon" aria-label="Liên hệ"><i class="fa-solid fa-comment-dots"></i></a>
                    <a href="mailto:support@mycosmetic.vn" class="social-icon" aria-label="Email"><i class="fa-solid fa-envelope-open-text"></i></a>
                </div>

                <a href="${pageContext.request.contextPath}/stores" class="store-system"><i class="fa-solid fa-shop"></i> Hệ thống cửa hàng</a>

                <div class="footer-extra-info">
                    <div><i class="fa-solid fa-clock"></i><span>Hỗ trợ khách hàng: 08:00 - 22:00 hằng ngày.</span></div>
                    <div><i class="fa-solid fa-gift"></i><span>Theo dõi MyCosmetic để nhận voucher và thông tin khuyến mãi mới.</span></div>
                </div>
            </div>
        </div>
    </div>

    <div class="footer-bottom">
        <div class="footer-container">
            <div class="footer-bottom-inner">
                <span>Copyright ©
                    <c:choose>
                        <c:when test="${not empty settings.name_website}">${settings.name_website}</c:when>
                        <c:otherwise>MyCosmeticShop</c:otherwise>
                    </c:choose>
                    <c:choose>
                        <c:when test="${not empty settings.copyright_year}">${settings.copyright_year}</c:when>
                        <c:otherwise>2026</c:otherwise>
                    </c:choose>
                    – All rights reserved
                </span>
                <span class="footer-bottom-links">
                    <a href="${pageContext.request.contextPath}/policy/privacy">Bảo mật</a>
                    <a href="${pageContext.request.contextPath}/policy/terms">Điều khoản</a>
                    <a href="${pageContext.request.contextPath}/lien-he">Hỗ trợ</a>
                </span>
            </div>
        </div>
    </div>
</footer>

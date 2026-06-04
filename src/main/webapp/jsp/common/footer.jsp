<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>

<link rel="stylesheet"
      href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

<style>
    :root {
        --primary: #e11d48;
        --primary-hover: #be123c;
        --primary-soft: #fff1f5;

        --surface: #ffffff;
        --surface-soft: #f8fafc;
        --background: #f5f7fb;

        --text-main: #111827;
        --text-muted: #6b7280;

        --border-color: #dbe3ee;

        --shadow-sm: 0 1px 2px rgba(15,23,42,0.05);
        --shadow-md: 0 8px 24px rgba(15,23,42,0.12);
        --shadow-lg: 0 18px 40px rgba(15,23,42,0.18);
    }

    /* FOOTER BASE */
    .site-footer-v2 {
        background: linear-gradient(180deg, var(--surface) 0%, var(--surface-soft) 100%);
        color: var(--text-main);
        padding: 60px 0 0;
        margin-top: 60px;
        border-top: 1px solid var(--border-color);
    }

    /* CONTAINER */
    .footer-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 20px;
    }

    /* GRID */
    .footer-grid {
        display: grid;
        grid-template-columns: 2fr 1fr 1.5fr 2fr;
        gap: 32px;
        padding-bottom: 40px;
    }

    /* TITLE */
    .footer-col h4 {
        font-size: 13px;
        font-weight: 900;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        margin-bottom: 18px;
        color: var(--text-main);
    }

    /* LIST */
    .footer-col ul {
        list-style: none;
        padding: 0;
        margin: 0;
    }

    .footer-col ul li {
        margin-bottom: 10px;
        font-size: 13px;
        color: var(--text-muted);
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .footer-col ul li i {
        color: var(--primary);
    }

    /* LINKS */
    .footer-col ul li a {
        color: var(--text-muted);
        text-decoration: none;
        transition: 0.2s ease;
        position: relative;
    }

    .footer-col ul li a:hover {
        color: var(--primary);
        transform: translateX(4px);
    }

    /* COMPANY INFO */
    .company-info {
        font-size: 12px;
        color: var(--text-muted);
        margin-top: 16px;
        border-top: 1px solid var(--border-color);
        padding-top: 16px;
        line-height: 1.6;
    }

    .company-info strong {
        color: var(--text-main);
    }

    /* NEWSLETTER */
    .footer-newsletter {
        display: flex;
        align-items: center;
        border: 1px solid var(--border-color);
        border-radius: 999px;
        overflow: hidden;
        padding: 6px 10px;
        margin-bottom: 20px;
        background: var(--surface);
        box-shadow: var(--shadow-sm);
    }

    .footer-newsletter input {
        flex: 1;
        border: none;
        outline: none;
        font-size: 13px;
        background: transparent;
        color: var(--text-main);
    }

    .footer-newsletter button {
        border: none;
        background: var(--primary);
        color: #fff;
        width: 34px;
        height: 34px;
        border-radius: 50%;
        cursor: pointer;
        transition: 0.2s ease;
    }

    .footer-newsletter button:hover {
        background: var(--primary-hover);
        transform: scale(1.05);
    }

    /* SOCIAL */
    .footer-social {
        display: flex;
        gap: 12px;
        margin-bottom: 20px;
    }

    .social-icon {
        width: 38px;
        height: 38px;
        background: var(--surface);
        border: 1px solid var(--border-color);
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        color: var(--text-muted);
        transition: 0.25s ease;
        box-shadow: var(--shadow-sm);
    }

    .social-icon:hover {
        background: var(--primary);
        color: #fff;
        border-color: var(--primary);
        transform: translateY(-3px);
        box-shadow: var(--shadow-md);
    }

    /* TRUST */
    .store-system {
        font-size: 12px;
        font-weight: 800;
        color: var(--primary);
        border: 1px solid var(--primary);
        padding: 8px 12px;
        border-radius: 10px;
        display: inline-block;
        text-decoration: none;
        transition: 0.2s ease;
        background: var(--primary-soft);
    }

    .store-system:hover {
        background: var(--primary);
        color: #fff;
    }

    /* BOTTOM */
    .footer-bottom {
        background: var(--surface);
        border-top: 1px solid var(--border-color);
        padding: 18px 0;
        text-align: center;
        font-size: 12px;
        color: var(--text-muted);
    }

    /* RESPONSIVE */
    @media (max-width: 992px) {
        .footer-grid {
            grid-template-columns: 1fr 1fr;
            gap: 24px;
        }
    }

    @media (max-width: 600px) {
        .footer-grid {
            grid-template-columns: 1fr;
        }
    }
</style>

<footer class="site-footer-v2">

    <div class="footer-container">
        <div class="footer-grid">

            <!-- CONTACT -->
            <div class="footer-col">
                <h4>Thông tin liên hệ</h4>

                <ul>
                    <li>
                        <i class="fa-solid fa-phone"></i>
                        Hotline: ${settings.hotline}
                    </li>

                    <li>
                        <i class="fa-solid fa-envelope"></i>
                        Email: ${settings.sales_email}
                    </li>

                    <li>
                        <i class="fa-solid fa-envelope"></i>
                        HR: ${settings.hr_email}
                    </li>
                </ul>

                <div class="company-info">

                    <strong>${settings.company_name}</strong><br>

                    MSDN: ${settings.business_code} - Cấp ngày: ${settings.business_date}<br>

                    <i class="fa-solid fa-location-dot"></i>
                    ${settings.address}

                </div>
            </div>

            <!--  CATEGORY  -->
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

            <!--  POLICY -->
            <div class="footer-col">
                <h4>Chính sách</h4>

                <ul>
                    <c:forEach var="p" items="${policyList}">
                        <li>
                            <a href="${pageContext.request.contextPath}/page/${p.slug}">
                                    ${p.title}
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </div>

            <!--  SOCIAL -->
            <div class="footer-col">
                <h4>Kết nối với chúng tôi</h4>

                <form action="${pageContext.request.contextPath}/contact"
                      method="post"
                      class="footer-newsletter">

                    <input type="email"
                           name="email"
                           placeholder="Nhập email của bạn..."
                           required>

                    <button type="submit">
                        <i class="fa-solid fa-paper-plane"></i>
                    </button>

                </form>

                <div class="footer-social">

                    <a href="${settings.facebook}" class="social-icon">
                        <i class="fa-brands fa-facebook-f"></i>
                    </a>

                    <a href="${settings.instagram}" class="social-icon">
                        <i class="fa-brands fa-instagram"></i>
                    </a>

                    <a href="${settings.tiktok}" class="social-icon">
                        <i class="fa-brands fa-tiktok"></i>
                    </a>

                    <a href="${pageContext.request.contextPath}/lien-he" class="social-icon">
                        <i class="fa-solid fa-comment-dots"></i>
                    </a>

                </div>

                <div class="footer-trust">
                    <a href="http://online.gov.vn/">

                    </a>

                    <a href="#" class="store-system">
                        <i class="fa-solid fa-shop"></i> Hệ thống cửa hàng
                    </a>
                </div>
            </div>

        </div>
    </div>

    <!--  BOTTOM  -->
    <div class="footer-bottom">
        <div class="footer-container">
            Copyright © ${settings.name_website} ${settings.copyright_year} – All rights reserved
        </div>
    </div>

</footer>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Đánh giá sản phẩm" scope="request"/>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="csrfTokenValue" value="${sessionScope['CSRF_TOKEN']}" />

<c:choose>
    <c:when test="${not empty order.id}">
        <c:set var="orderIdValue" value="${order.id}" />
    </c:when>
    <c:otherwise>
        <c:set var="orderIdValue" value="${param.orderId}" />
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${not empty product.id}">
        <c:set var="productIdValue" value="${product.id}" />
    </c:when>
    <c:when test="${not empty param.productId}">
        <c:set var="productIdValue" value="${param.productId}" />
    </c:when>
    <c:otherwise>
        <c:set var="productIdValue" value="${param.product_id}" />
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${not empty product.name}">
        <c:set var="productDisplayName" value="${product.name}" />
    </c:when>
    <c:when test="${not empty product.title}">
        <c:set var="productDisplayName" value="${product.title}" />
    </c:when>
    <c:when test="${not empty param.productName}">
        <c:set var="productDisplayName" value="${param.productName}" />
    </c:when>
    <c:otherwise>
        <c:set var="productDisplayName" value="Sản phẩm #${productIdValue}" />
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${not empty product.image}">
        <c:set var="rawProductImage" value="${product.image}" />
    </c:when>
    <c:when test="${not empty param.productImage}">
        <c:set var="rawProductImage" value="${param.productImage}" />
    </c:when>
    <c:otherwise>
        <c:set var="rawProductImage" value="/assets/images/no-image.png" />
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${fn:startsWith(rawProductImage, 'http://') || fn:startsWith(rawProductImage, 'https://')}">
        <c:set var="productImageUrl" value="${rawProductImage}" />
    </c:when>
    <c:when test="${fn:startsWith(rawProductImage, '/')}">
        <c:set var="productImageUrl" value="${ctx}${rawProductImage}" />
    </c:when>
    <c:otherwise>
        <c:set var="productImageUrl" value="${ctx}/${rawProductImage}" />
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${not empty orderItem}">
        <c:set var="orderItemIdValue" value="${orderItem.id}" />
    </c:when>
    <c:when test="${not empty param.orderItemId}">
        <c:set var="orderItemIdValue" value="${param.orderItemId}" />
    </c:when>
    <c:otherwise>
        <c:set var="orderItemIdValue" value="${param.order_item_id}" />
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${not empty param.variantId}">
        <c:set var="variantIdValue" value="${param.variantId}" />
    </c:when>
    <c:otherwise>
        <c:set var="variantIdValue" value="" />
    </c:otherwise>
</c:choose>

<style>
    :root {
        --mc-primary-gradient: linear-gradient(135deg, #ff6aa9 0%, #ef4f93 50%, #ff7043 100%);
        --mc-primary-gradient-hover: linear-gradient(135deg, #ff5d9f 0%, #e94388 50%, #ff6233 100%);
        --mc-primary-shadow: 0 12px 26px rgba(239, 79, 147, 0.28);
        --mc-primary-shadow-hover: 0 16px 32px rgba(239, 79, 147, 0.36);
        --mc-pink: #ef4f93;
        --mc-pink-soft: #fff0f7;
        --mc-orange: #ff7043;
        --mc-yellow: #ffc107;
        --mc-text: #20243a;
        --mc-muted: #7d8398;
        --mc-border: #eef0f6;
    }

    .mc-review-page,
    .mc-review-page * {
        box-sizing: border-box;
    }

    .mc-review-page {
        min-height: calc(100vh - 120px);
        padding: 28px 14px 80px;
        background:
                radial-gradient(circle at top left, rgba(255, 216, 235, 0.75), transparent 30%),
                linear-gradient(180deg, #fff7fb 0%, #ffffff 58%, #fff8fc 100%);
        font-family: Arial, Helvetica, sans-serif;
        color: var(--mc-text);
    }

    .mc-review-wrap {
        max-width: 920px;
        margin: 0 auto;
    }

    .mc-review-top {
        display: flex;
        align-items: center;
        gap: 14px;
        margin-bottom: 18px;
    }

    .mc-review-back {
        width: 42px;
        height: 42px;
        border-radius: 50%;
        border: 1px solid #ffd3e6;
        background: #fff;
        color: var(--mc-pink);
        display: inline-flex;
        align-items: center;
        justify-content: center;
        text-decoration: none;
        font-size: 24px;
        font-weight: 800;
        box-shadow: 0 8px 20px rgba(239, 79, 147, 0.12);
        transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
    }

    .mc-review-back:hover {
        transform: translateY(-1px);
        background: #fff7fb;
        box-shadow: 0 12px 26px rgba(239, 79, 147, 0.18);
    }

    .mc-review-title {
        margin: 0;
        font-size: 32px;
        font-weight: 900;
        color: var(--mc-text);
        letter-spacing: -0.4px;
    }

    .mc-guide-trigger {
        width: 100%;
        border: 1px solid #f3d48b;
        background: #fff7df;
        border-radius: 18px;
        padding: 16px 18px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16px;
        cursor: pointer;
        transition: all 0.2s ease;
        box-shadow: 0 8px 18px rgba(255, 193, 7, 0.08);
        text-align: left;
        margin-bottom: 18px;
    }

    .mc-guide-trigger:hover {
        transform: translateY(-1px);
        background: #fff3cf;
        box-shadow: 0 12px 24px rgba(255, 193, 7, 0.12);
    }

    .mc-guide-trigger-left {
        display: flex;
        align-items: center;
        gap: 12px;
        min-width: 0;
    }

    .mc-guide-coin {
        width: 34px;
        height: 34px;
        border-radius: 50%;
        background: linear-gradient(135deg, #ffdb60, #ffb300);
        color: #fff;
        font-size: 18px;
        font-weight: 900;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        flex: 0 0 34px;
        box-shadow: 0 8px 16px rgba(255, 179, 0, 0.25);
    }

    .mc-guide-trigger-text {
        color: #6b4d00;
        font-size: 15px;
        font-weight: 750;
        line-height: 1.5;
    }

    .mc-guide-trigger-text strong {
        color: #f28c00;
        font-weight: 900;
    }

    .mc-guide-trigger-arrow {
        color: #7f6500;
        font-size: 24px;
        line-height: 1;
        font-weight: 900;
        flex: 0 0 auto;
    }

    .mc-review-card {
        overflow: hidden;
        background: #fff;
        border-radius: 26px;
        border: 1px solid #f1dce7;
        box-shadow:
                0 22px 60px rgba(217, 59, 135, 0.12),
                0 8px 24px rgba(30, 35, 60, 0.04);
    }

    .mc-card-body {
        padding: 26px;
    }

    .mc-alert {
        margin-bottom: 16px;
        padding: 14px 16px;
        border-radius: 14px;
        font-size: 14px;
        font-weight: 700;
        line-height: 1.55;
    }

    .mc-alert-success {
        background: #eefaf3;
        border: 1px solid #bde8cc;
        color: #17663d;
    }

    .mc-alert-error {
        background: #fff1f2;
        border: 1px solid #fecdd3;
        color: #b42318;
    }

    .mc-product-row {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 16px;
        border-radius: 18px;
        background: #fff;
        border: 1px solid var(--mc-border);
    }

    .mc-product-thumb {
        width: 78px;
        height: 78px;
        flex: 0 0 78px;
        overflow: hidden;
        border-radius: 14px;
        background: #f6f7fb;
        border: 1px solid #eceef5;
    }

    .mc-product-thumb img {
        width: 100%;
        height: 100%;
        display: block;
        object-fit: cover;
    }

    .mc-product-info {
        min-width: 0;
    }

    .mc-product-name {
        margin: 0 0 7px;
        font-size: 17px;
        font-weight: 900;
        color: var(--mc-text);
        line-height: 1.35;
    }

    .mc-product-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        color: #7b8298;
        font-size: 13px;
        font-weight: 700;
    }

    .mc-divider {
        height: 1px;
        background: #f0f1f6;
        margin: 22px 0;
    }

    .mc-section {
        margin-bottom: 24px;
    }

    .mc-section-head {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 12px;
        margin-bottom: 13px;
    }

    .mc-section-title {
        margin: 0;
        font-size: 18px;
        font-weight: 900;
        color: var(--mc-text);
    }

    .mc-section-sub {
        margin: 4px 0 0;
        font-size: 13px;
        color: #7e8499;
        font-weight: 650;
        line-height: 1.55;
    }

    .mc-point {
        color: #ff9800;
        font-size: 15px;
        font-weight: 900;
        white-space: nowrap;
    }

    .mc-star-area {
        display: flex;
        align-items: center;
        gap: 18px;
        flex-wrap: wrap;
    }

    .mc-stars {
        display: flex;
        gap: 10px;
    }

    .mc-star {
        width: 54px;
        height: 54px;
        border: 0;
        background: transparent;
        color: #dddfe8;
        cursor: pointer;
        font-size: 46px;
        line-height: 1;
        padding: 0;
        transition: transform 0.16s ease, color 0.16s ease, filter 0.16s ease;
    }

    .mc-star:hover,
    .mc-star.hovering,
    .mc-star.active {
        color: var(--mc-yellow);
        filter: drop-shadow(0 8px 10px rgba(255, 193, 7, 0.24));
    }

    .mc-star:hover {
        transform: translateY(-2px) scale(1.03);
    }

    .mc-rating-text {
        display: flex;
        flex-direction: column;
        gap: 3px;
    }

    .mc-rating-text strong {
        color: #cf8600;
        font-size: 16px;
        font-weight: 900;
    }

    .mc-rating-text span {
        color: var(--mc-muted);
        font-size: 13px;
        font-weight: 700;
    }

    .mc-media-box {
        border: 2px dashed #ffd166;
        border-radius: 18px;
        padding: 22px;
        background: #fffdf5;
        text-align: center;
        transition: border-color 0.18s ease, background 0.18s ease;
    }

    .mc-media-box:hover {
        border-color: var(--mc-yellow);
        background: #fff8dd;
    }

    .mc-media-icon {
        width: 54px;
        height: 54px;
        margin: 0 auto 10px;
        border-radius: 50%;
        background: #fff7db;
        color: #f0a500;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
    }

    .mc-media-title {
        margin: 0 0 5px;
        color: #32364d;
        font-size: 15px;
        font-weight: 850;
    }

    .mc-media-hint {
        margin: 0 0 16px;
        color: #8a91a5;
        font-size: 13px;
        font-weight: 650;
        line-height: 1.5;
    }

    .mc-media-upload-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 14px;
    }

    .mc-upload-box {
        min-height: 132px;
        border: 1px solid #ffe0a3;
        border-radius: 16px;
        background: #fff;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 7px;
        cursor: pointer;
        transition: all 0.18s ease;
        text-align: center;
        padding: 16px;
    }

    .mc-upload-box:hover {
        background: #fffaf0;
        border-color: var(--mc-yellow);
        transform: translateY(-1px);
    }

    .mc-upload-box input {
        display: none;
    }

    .mc-upload-icon {
        width: 42px;
        height: 42px;
        border-radius: 50%;
        background: #fff1b8;
        color: #f0a500;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 22px;
    }

    .mc-upload-box strong {
        color: #33384d;
        font-size: 14px;
        font-weight: 900;
    }

    .mc-upload-box span {
        color: #848b9f;
        font-size: 12px;
        font-weight: 650;
        line-height: 1.45;
    }

    .mc-media-preview {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(118px, 1fr));
        gap: 12px;
        margin-top: 14px;
    }

    .mc-preview-item {
        border: 1px solid #eceef5;
        border-radius: 14px;
        background: #fff;
        overflow: hidden;
    }

    .mc-preview-item img,
    .mc-preview-item video {
        width: 100%;
        height: 100px;
        object-fit: cover;
        display: block;
        background: #f7f8fb;
    }

    .mc-preview-item span {
        display: block;
        padding: 8px;
        color: #6f768a;
        font-size: 12px;
        font-weight: 700;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .mc-field {
        display: flex;
        flex-direction: column;
        gap: 8px;
    }

    .mc-field label {
        color: #30354d;
        font-size: 14px;
        font-weight: 850;
    }

    .mc-field textarea {
        width: 100%;
        min-height: 210px;
        padding: 16px;
        resize: vertical;
        border: 1px solid #e1e5ee;
        border-radius: 15px;
        background: #fff;
        color: #29304a;
        font-size: 15px;
        font-weight: 600;
        line-height: 1.6;
        outline: none;
        transition: border-color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
    }

    .mc-field textarea:focus {
        border-color: var(--mc-pink);
        background: #fffafd;
        box-shadow: 0 0 0 4px rgba(239, 79, 147, 0.12);
    }

    .mc-textarea-wrap {
        position: relative;
    }

    .mc-char-count {
        position: absolute;
        right: 14px;
        bottom: 12px;
        color: #a0a6b6;
        font-size: 13px;
        font-weight: 700;
    }

    .mc-checkbox-row {
        display: flex;
        align-items: flex-start;
        gap: 10px;
        margin-top: 14px;
        color: #50566d;
        font-size: 15px;
        font-weight: 700;
    }

    .mc-checkbox-row input {
        width: 22px;
        height: 22px;
        accent-color: var(--mc-pink);
        margin-top: 2px;
    }

    .mc-checkbox-row small {
        display: block;
        margin-top: 3px;
        color: #8a91a5;
        font-size: 12px;
        font-weight: 650;
        line-height: 1.4;
    }

    .mc-service-card {
        margin-top: 20px;
        padding: 22px;
        border-radius: 22px;
        background: #fff;
        border: 1px solid var(--mc-border);
        box-shadow: 0 10px 24px rgba(30, 35, 60, 0.035);
    }

    .mc-service-row {
        display: grid;
        grid-template-columns: 210px 1fr;
        align-items: center;
        gap: 16px;
        margin-bottom: 16px;
    }

    .mc-service-label {
        color: var(--mc-text);
        font-size: 16px;
        font-weight: 850;
    }

    .mc-service-stars {
        display: flex;
        gap: 7px;
    }

    .mc-service-star {
        border: 0;
        background: transparent;
        padding: 0;
        color: #dddfe8;
        font-size: 34px;
        line-height: 1;
        cursor: pointer;
        transition: color 0.16s ease, transform 0.16s ease;
    }

    .mc-service-star.active,
    .mc-service-star.hovering,
    .mc-service-star:hover {
        color: var(--mc-yellow);
        transform: translateY(-1px);
    }

    .mc-chip-group {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        margin: 16px 0;
    }

    .mc-chip {
        position: relative;
    }

    .mc-chip input {
        position: absolute;
        opacity: 0;
        pointer-events: none;
    }

    .mc-chip span {
        display: inline-flex;
        align-items: center;
        min-height: 38px;
        padding: 8px 14px;
        border-radius: 10px;
        background: #f7f8fb;
        color: #41475e;
        font-size: 14px;
        font-weight: 750;
        cursor: pointer;
        border: 1px solid transparent;
        transition: all 0.16s ease;
    }

    .mc-chip input:checked + span {
        background: var(--mc-pink-soft);
        color: #d63384;
        border-color: #ffc2dc;
    }

    .mc-actions {
        position: sticky;
        bottom: 0;
        display: flex;
        align-items: center;
        justify-content: flex-end;
        gap: 14px;
        margin-top: 24px;
        padding: 18px 0 0;
        background: linear-gradient(180deg, rgba(255,255,255,0), #fff 28%);
    }

    .mc-btn {
        min-width: 160px;
        min-height: 52px;
        border-radius: 14px;
        border: 0;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 0 22px;
        text-decoration: none;
        font-size: 15px;
        font-weight: 900;
        cursor: pointer;
        transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease, filter 0.18s ease;
    }

    .mc-btn-secondary {
        background: #fff;
        color: #2d344c;
        border: 1px solid #dfe4ee;
    }

    .mc-btn-secondary:hover {
        transform: translateY(-1px);
        background: #fff7fb;
        border-color: #ffc2dc;
        color: #d63384;
    }

    .mc-btn-primary {
        color: #fff;
        background: var(--mc-primary-gradient);
        box-shadow: var(--mc-primary-shadow);
    }

    .mc-btn-primary:hover {
        transform: translateY(-1px);
        background: var(--mc-primary-gradient-hover);
        box-shadow: var(--mc-primary-shadow-hover);
        filter: brightness(1.03);
    }

    .mc-policy-modal {
        position: fixed;
        inset: 0;
        z-index: 9999;
        display: none;
    }

    .mc-policy-modal.show {
        display: block;
    }

    .mc-policy-backdrop {
        position: absolute;
        inset: 0;
        background: rgba(20, 22, 35, 0.45);
        backdrop-filter: blur(2px);
    }

    .mc-policy-dialog {
        position: relative;
        width: min(720px, calc(100% - 24px));
        margin: 40px auto;
        background: #fff;
        border-radius: 26px;
        overflow: hidden;
        box-shadow: 0 30px 80px rgba(23, 24, 37, 0.24);
    }

    .mc-policy-header {
        padding: 24px 24px 18px;
        text-align: center;
        border-bottom: 1px solid #f0f1f5;
    }

    .mc-policy-header h3 {
        margin: 0;
        font-size: 22px;
        font-weight: 900;
        color: var(--mc-text);
    }

    .mc-policy-body {
        max-height: 65vh;
        overflow-y: auto;
        padding: 18px 20px 10px;
        background: #fafafa;
    }

    .mc-policy-card-block,
    .mc-policy-note-block {
        background: #fff;
        border-radius: 20px;
        overflow: hidden;
        border: 1px solid #f0f1f5;
        margin-bottom: 18px;
    }

    .mc-policy-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 14px;
        padding: 18px;
        border-bottom: 1px solid #f2f3f7;
    }

    .mc-policy-row:last-child {
        border-bottom: none;
    }

    .mc-policy-row-left {
        display: flex;
        align-items: flex-start;
        gap: 12px;
    }

    .mc-policy-icon {
        font-size: 24px;
    }

    .mc-policy-row-title {
        color: #1f2438;
        font-size: 16px;
        font-weight: 850;
    }

    .mc-policy-row-desc {
        color: #72798f;
        font-size: 14px;
        font-weight: 650;
        margin-top: 4px;
    }

    .mc-policy-reward {
        min-width: 88px;
        text-align: center;
        padding: 8px 14px;
        border-radius: 999px;
        background: #fff4db;
        color: #f08a00;
        font-size: 15px;
        font-weight: 900;
    }

    .mc-policy-note-block {
        padding: 18px 18px 8px;
    }

    .mc-policy-note-list {
        margin: 0;
        padding-left: 18px;
        color: #5b6278;
    }

    .mc-policy-note-list li {
        margin-bottom: 12px;
        font-size: 15px;
        line-height: 1.65;
        font-weight: 600;
    }

    .mc-policy-footer {
        padding: 18px 20px 22px;
        background: #fff;
        border-top: 1px solid #f0f1f5;
    }

    .mc-policy-confirm {
        width: 100%;
        height: 54px;
        border: none;
        border-radius: 16px;
        background: var(--mc-primary-gradient);
        color: #fff;
        font-size: 22px;
        font-weight: 900;
        cursor: pointer;
        box-shadow: var(--mc-primary-shadow);
        transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease, filter 0.18s ease;
    }

    .mc-policy-confirm:hover {
        transform: translateY(-1px);
        background: var(--mc-primary-gradient-hover);
        box-shadow: var(--mc-primary-shadow-hover);
        filter: brightness(1.03);
    }

    @media (max-width: 768px) {
        .mc-card-body {
            padding: 18px;
        }

        .mc-review-title {
            font-size: 26px;
        }

        .mc-star {
            width: 44px;
            height: 44px;
            font-size: 38px;
        }

        .mc-media-upload-grid {
            grid-template-columns: 1fr;
        }

        .mc-service-row {
            grid-template-columns: 1fr;
        }

        .mc-actions {
            flex-direction: column-reverse;
            align-items: stretch;
        }

        .mc-btn {
            width: 100%;
        }

        .mc-policy-dialog {
            width: calc(100% - 16px);
            margin: 20px auto;
            border-radius: 20px;
        }
    }
</style>

<main class="mc-review-page">
    <div class="mc-review-wrap">

        <div class="mc-review-top">
            <a class="mc-review-back" href="${ctx}/orders/detail?id=${orderIdValue}">‹</a>
            <h1 class="mc-review-title">Đánh giá sản phẩm</h1>
        </div>

        <button type="button" class="mc-guide-trigger" id="openRewardPolicyBtn">
            <span class="mc-guide-trigger-left">
                <span class="mc-guide-coin">S</span>
                <span class="mc-guide-trigger-text">
                    Đánh giá chuẩn để nhận xu cho lần mua sau.
                    <strong>Tối đa 600 xu!</strong>
                </span>
            </span>
            <span class="mc-guide-trigger-arrow">›</span>
        </button>

        <section class="mc-review-card">
            <div class="mc-card-body">

                <c:if test="${not empty successMessage}">
                    <div class="mc-alert mc-alert-success">${successMessage}</div>
                </c:if>

                <c:if test="${not empty errorMessage}">
                    <div class="mc-alert mc-alert-error">${errorMessage}</div>
                </c:if>

                <div class="mc-product-row">
                    <div class="mc-product-thumb">
                        <img src="${productImageUrl}" alt="${productDisplayName}">
                    </div>

                    <div class="mc-product-info">
                        <h2 class="mc-product-name">${productDisplayName}</h2>

                        <div class="mc-product-meta">
                            <span>Đơn hàng #${orderIdValue}</span>
                            <span>Sản phẩm #${productIdValue}</span>

                            <c:if test="${not empty orderItemIdValue}">
                                <span>Chi tiết đơn #${orderItemIdValue}</span>
                            </c:if>

                            <c:if test="${not empty order.createdAtDate}">
                                <span>
                                    Ngày mua:
                                    <fmt:formatDate value="${order.createdAtDate}" pattern="dd/MM/yyyy"/>
                                </span>
                            </c:if>
                        </div>
                    </div>
                </div>

                <div class="mc-divider"></div>

                <form method="post"
                      action="${ctx}/orders/review/submit?csrf_token=${csrfTokenValue}"
                      enctype="multipart/form-data"
                      id="reviewForm">

                    <input type="hidden" name="csrf_token" value="${csrfTokenValue}">
                    <input type="hidden" id="orderIdInput" name="orderId" value="${orderIdValue}">
                    <input type="hidden" id="productIdInput" name="productId" value="${productIdValue}">
                    <input type="hidden" name="variantId" value="${variantIdValue}">
                    <input type="hidden" name="orderItemId" value="${orderItemIdValue}">
                    <input type="hidden" id="ratingValue" name="rating" value="${not empty param.rating ? param.rating : 5}">
                    <input type="hidden" id="sellerServiceRating" name="sellerServiceRating" value="5">
                    <input type="hidden" id="deliverySpeedRating" name="deliverySpeedRating" value="5">
                    <input type="hidden" id="shipperRating" name="shipperRating" value="5">

                    <div class="mc-section">
                        <div class="mc-section-head">
                            <div>
                                <h3 class="mc-section-title">Đánh giá sản phẩm</h3>
                                <p class="mc-section-sub">Chọn số sao phù hợp với trải nghiệm thực tế của bạn.</p>
                            </div>
                        </div>

                        <div class="mc-star-area">
                            <div class="mc-stars" data-target="ratingValue">
                                <button type="button" class="mc-star active" data-value="1">★</button>
                                <button type="button" class="mc-star active" data-value="2">★</button>
                                <button type="button" class="mc-star active" data-value="3">★</button>
                                <button type="button" class="mc-star active" data-value="4">★</button>
                                <button type="button" class="mc-star active" data-value="5">★</button>
                            </div>

                            <div class="mc-rating-text">
                                <strong id="ratingText">Rất hài lòng</strong>
                                <span id="ratingHint">Sản phẩm đáp ứng tốt kỳ vọng của bạn.</span>
                            </div>
                        </div>
                    </div>

                    <div class="mc-section">
                        <div class="mc-section-head">
                            <div>
                                <h3 class="mc-section-title">Thêm ảnh/video về sản phẩm</h3>
                                <p class="mc-section-sub">Upload trực tiếp ảnh hoặc video minh họa.</p>
                            </div>
                            <span class="mc-point">+200 xu</span>
                        </div>

                        <div class="mc-media-box">
                            <div class="mc-media-icon">📷</div>
                            <p class="mc-media-title">Hình ảnh / Video minh họa</p>
                            <p class="mc-media-hint">
                                Ảnh: tối đa 5 ảnh, mỗi ảnh không quá 5MB. Video: tối đa 1 video, không quá 50MB.
                            </p>

                            <div class="mc-media-upload-grid">
                                <label class="mc-upload-box">
                                    <input type="file"
                                           id="reviewImages"
                                           name="reviewImages"
                                           accept="image/jpeg,image/jpg,image/png,image/webp"
                                           multiple>
                                    <span class="mc-upload-icon">📷</span>
                                    <strong>Thêm hình ảnh</strong>
                                    <span>JPG, PNG, WEBP. Tối đa 5 ảnh.</span>
                                </label>

                                <label class="mc-upload-box">
                                    <input type="file"
                                           id="reviewVideo"
                                           name="reviewVideo"
                                           accept="video/mp4,video/webm">
                                    <span class="mc-upload-icon">🎬</span>
                                    <strong>Thêm video</strong>
                                    <span>MP4, WEBM. Tối đa 50MB.</span>
                                </label>
                            </div>

                            <div id="mediaPreview" class="mc-media-preview"></div>
                        </div>
                    </div>

                    <div class="mc-section">
                        <div class="mc-section-head">
                            <div>
                                <h3 class="mc-section-title">Viết đánh giá từ 50 ký tự</h3>
                                <p class="mc-section-sub">Chia sẻ chất lượng, thiết kế, đóng gói và cảm nhận sau khi sử dụng.</p>
                            </div>
                        </div>

                        <div class="mc-field mc-textarea-wrap">
                            <label for="comment">Nội dung đánh giá</label>
                            <textarea id="comment"
                                      name="comment"
                                      maxlength="1000"
                                      required
                                      placeholder="Chất lượng: Sản phẩm dùng khá tốt, đóng gói chắc chắn.
Thiết kế: Bao bì đẹp, dễ sử dụng.
Cảm nhận: Mùi dễ chịu, phù hợp với nhu cầu của mình.

Hãy chia sẻ nhận xét cho sản phẩm này nhé!">${param.comment}</textarea>
                            <span class="mc-char-count"><span id="commentCount">0</span> ký tự</span>
                        </div>

                        <label class="mc-checkbox-row">
                            <input type="checkbox" name="anonymous" value="1">
                            <span>
                                Đánh giá ẩn danh
                                <small>Tên của bạn sẽ được hiển thị dạng rút gọn, ví dụ: us***01.</small>
                            </span>
                        </label>
                    </div>

                    <div class="mc-service-card">
                        <div class="mc-section-head">
                            <div>
                                <h3 class="mc-section-title">Dịch vụ của người bán</h3>
                                <p class="mc-section-sub">Đánh giá trải nghiệm giao hàng và chăm sóc khách hàng.</p>
                            </div>
                        </div>

                        <div class="mc-service-row">
                            <div class="mc-service-label">Dịch vụ của người bán</div>
                            <div class="mc-service-stars" data-target="sellerServiceRating">
                                <button type="button" class="mc-service-star active" data-value="1">★</button>
                                <button type="button" class="mc-service-star active" data-value="2">★</button>
                                <button type="button" class="mc-service-star active" data-value="3">★</button>
                                <button type="button" class="mc-service-star active" data-value="4">★</button>
                                <button type="button" class="mc-service-star active" data-value="5">★</button>
                            </div>
                        </div>

                        <div class="mc-service-row">
                            <div class="mc-service-label">Tốc độ giao hàng</div>
                            <div class="mc-service-stars" data-target="deliverySpeedRating">
                                <button type="button" class="mc-service-star active" data-value="1">★</button>
                                <button type="button" class="mc-service-star active" data-value="2">★</button>
                                <button type="button" class="mc-service-star active" data-value="3">★</button>
                                <button type="button" class="mc-service-star active" data-value="4">★</button>
                                <button type="button" class="mc-service-star active" data-value="5">★</button>
                            </div>
                        </div>

                        <div class="mc-service-row">
                            <div class="mc-service-label">Đơn vị giao hàng</div>
                            <div class="mc-service-stars" data-target="shipperRating">
                                <button type="button" class="mc-service-star active" data-value="1">★</button>
                                <button type="button" class="mc-service-star active" data-value="2">★</button>
                                <button type="button" class="mc-service-star active" data-value="3">★</button>
                                <button type="button" class="mc-service-star active" data-value="4">★</button>
                                <button type="button" class="mc-service-star active" data-value="5">★</button>
                            </div>
                        </div>

                        <div class="mc-chip-group">
                            <label class="mc-chip">
                                <input type="checkbox" name="serviceTags" value="Chuyên nghiệp, chu đáo">
                                <span>Chuyên nghiệp, chu đáo</span>
                            </label>

                            <label class="mc-chip">
                                <input type="checkbox" name="serviceTags" value="Cập nhật trạng thái thường xuyên">
                                <span>Cập nhật trạng thái thường xuyên</span>
                            </label>

                            <label class="mc-chip">
                                <input type="checkbox" name="serviceTags" value="Giao hàng đúng hẹn">
                                <span>Giao hàng đúng hẹn</span>
                            </label>

                            <label class="mc-chip">
                                <input type="checkbox" name="serviceTags" value="Đóng gói gọn gàng">
                                <span>Đóng gói gọn gàng</span>
                            </label>

                            <label class="mc-chip">
                                <input type="checkbox" name="serviceTags" value="Đáng tin cậy">
                                <span>Đáng tin cậy</span>
                            </label>

                            <label class="mc-chip">
                                <input type="checkbox" name="serviceTags" value="Hỗ trợ khách hàng nhiệt tình">
                                <span>Hỗ trợ khách hàng nhiệt tình</span>
                            </label>
                        </div>

                        <div class="mc-field">
                            <label for="serviceComment">Đánh giá thêm về dịch vụ</label>
                            <textarea id="serviceComment"
                                      name="serviceComment"
                                      maxlength="500"
                                      placeholder="Thêm đánh giá chi tiết tại đây..."></textarea>
                        </div>
                    </div>

                    <div class="mc-actions">
                        <a class="mc-btn mc-btn-secondary" href="${ctx}/orders/detail?id=${orderIdValue}">
                            Quay lại đơn hàng
                        </a>

                        <button type="submit" class="mc-btn mc-btn-primary">
                            Gửi đánh giá
                        </button>
                    </div>
                </form>
            </div>
        </section>
    </div>
</main>

<div class="mc-policy-modal" id="rewardPolicyModal" aria-hidden="true">
    <div class="mc-policy-backdrop" id="rewardPolicyBackdrop"></div>

    <div class="mc-policy-dialog" role="dialog" aria-modal="true" aria-labelledby="rewardPolicyTitle">
        <div class="mc-policy-header">
            <h3 id="rewardPolicyTitle">Quy định đánh giá</h3>
        </div>

        <div class="mc-policy-body">
            <div class="mc-policy-card-block">
                <div class="mc-policy-row">
                    <div class="mc-policy-row-left">
                        <span class="mc-policy-icon">📷</span>
                        <div>
                            <div class="mc-policy-row-title">Đăng ảnh hoặc video minh họa</div>
                            <div class="mc-policy-row-desc">Viết đánh giá từ 50 ký tự trở lên</div>
                        </div>
                    </div>
                    <div class="mc-policy-reward">200 xu</div>
                </div>

                <div class="mc-policy-row">
                    <div class="mc-policy-row-left">
                        <span class="mc-policy-icon">✅</span>
                        <div>
                            <div class="mc-policy-row-title">Đánh giá được quản trị viên duyệt</div>
                            <div class="mc-policy-row-desc">Chỉ đánh giá hợp lệ mới được cộng xu</div>
                        </div>
                    </div>
                    <div class="mc-policy-reward">200 xu</div>
                </div>

                <div class="mc-policy-row">
                    <div class="mc-policy-row-left">
                        <span class="mc-policy-icon">🎁</span>
                        <div>
                            <div class="mc-policy-row-title">Hoàn tất đánh giá đúng quy định</div>
                            <div class="mc-policy-row-desc">Có thể nhận tối đa 600 xu cho mỗi sản phẩm</div>
                        </div>
                    </div>
                    <div class="mc-policy-reward">Tối đa 600</div>
                </div>
            </div>

            <div class="mc-policy-note-block">
                <ul class="mc-policy-note-list">
                    <li>Trong 1 đơn hàng có nhiều sản phẩm, bạn có thể nhận xu trên từng sản phẩm nếu đánh giá đủ điều kiện.</li>
                    <li>Nội dung đánh giá phải liên quan đến sản phẩm, rõ ràng, lành mạnh và không vi phạm tiêu chuẩn cộng đồng.</li>
                    <li>Ảnh/video phải đúng sản phẩm thực tế, rõ nội dung và không chứa nội dung không phù hợp.</li>
                    <li>Đánh giá bị từ chối hoặc bị ẩn sẽ không được cộng xu.</li>
                    <li>Xu chỉ được cộng sau khi đánh giá được duyệt hợp lệ.</li>
                    <li>100 xu = 1.000đ, số xu dùng để giảm giá tối đa 20% giá trị đơn hàng.</li>
                    <li>Xu có thời hạn sử dụng 90 ngày kể từ ngày được cộng và không quy đổi thành tiền mặt.</li>
                    <li>Mỗi sản phẩm trong một đơn hàng chỉ được nhận thưởng xu đánh giá một lần.</li>
                    <li>Quyết định duyệt đánh giá và cộng xu của hệ thống/quản trị viên là quyết định cuối cùng.</li>
                </ul>
            </div>
        </div>

        <div class="mc-policy-footer">
            <button type="button" class="mc-policy-confirm" id="closeRewardPolicyBtn">
                ĐỒNG Ý
            </button>
        </div>
    </div>
</div>

<script>
    (function () {
        const labels = {
            1: {
                title: 'Rất không hài lòng',
                hint: 'Sản phẩm chưa đáp ứng kỳ vọng của bạn.'
            },
            2: {
                title: 'Không hài lòng',
                hint: 'Sản phẩm còn nhiều điểm cần cải thiện.'
            },
            3: {
                title: 'Bình thường',
                hint: 'Trải nghiệm ở mức chấp nhận được.'
            },
            4: {
                title: 'Hài lòng',
                hint: 'Sản phẩm khá tốt và đáng cân nhắc.'
            },
            5: {
                title: 'Rất hài lòng',
                hint: 'Sản phẩm đáp ứng tốt kỳ vọng của bạn.'
            }
        };

        function setupStars(groupSelector, starSelector, activeClass, callback) {
            document.querySelectorAll(groupSelector).forEach(function (group) {
                const targetId = group.dataset.target;
                const targetInput = document.getElementById(targetId);
                const stars = group.querySelectorAll(starSelector);

                function update(value) {
                    stars.forEach(function (star) {
                        const starValue = parseInt(star.dataset.value);
                        star.classList.toggle(activeClass, starValue <= value);
                    });

                    if (targetInput) {
                        targetInput.value = value;
                    }

                    if (callback) {
                        callback(value, targetId);
                    }
                }

                stars.forEach(function (star) {
                    star.addEventListener('click', function () {
                        update(parseInt(this.dataset.value));
                    });

                    star.addEventListener('mouseenter', function () {
                        const hoverValue = parseInt(this.dataset.value);

                        stars.forEach(function (item) {
                            const starValue = parseInt(item.dataset.value);
                            item.classList.toggle('hovering', starValue <= hoverValue);
                        });
                    });

                    star.addEventListener('mouseleave', function () {
                        stars.forEach(function (item) {
                            item.classList.remove('hovering');
                        });
                    });
                });

                let initial = targetInput ? parseInt(targetInput.value || '5') : 5;

                if (initial < 1 || initial > 5) {
                    initial = 5;
                }

                update(initial);
            });
        }

        setupStars('.mc-stars', '.mc-star', 'active', function (value) {
            const ratingText = document.getElementById('ratingText');
            const ratingHint = document.getElementById('ratingHint');

            if (ratingText && ratingHint) {
                ratingText.textContent = labels[value].title;
                ratingHint.textContent = labels[value].hint;
            }
        });

        setupStars('.mc-service-stars', '.mc-service-star', 'active');

        const comment = document.getElementById('comment');
        const commentCount = document.getElementById('commentCount');
        const form = document.getElementById('reviewForm');
        const orderIdInput = document.getElementById('orderIdInput');
        const productIdInput = document.getElementById('productIdInput');

        function updateCount() {
            if (comment && commentCount) {
                commentCount.textContent = comment.value.trim().length;
            }
        }

        if (comment) {
            comment.addEventListener('input', updateCount);
            updateCount();
        }

        const imageInput = document.getElementById('reviewImages');
        const videoInput = document.getElementById('reviewVideo');
        const preview = document.getElementById('mediaPreview');

        const MAX_IMAGE_SIZE = 5 * 1024 * 1024;
        const MAX_VIDEO_SIZE = 50 * 1024 * 1024;
        const MAX_IMAGES = 5;

        function formatSize(bytes) {
            return (bytes / 1024 / 1024).toFixed(1) + 'MB';
        }

        function renderPreview() {
            if (!preview) return;

            preview.innerHTML = '';

            const images = imageInput ? Array.from(imageInput.files || []) : [];
            const video = videoInput && videoInput.files ? videoInput.files[0] : null;

            images.forEach(function (file) {
                const item = document.createElement('div');
                item.className = 'mc-preview-item';

                const img = document.createElement('img');
                img.src = URL.createObjectURL(file);

                const name = document.createElement('span');
                name.textContent = file.name;

                item.appendChild(img);
                item.appendChild(name);
                preview.appendChild(item);
            });

            if (video) {
                const item = document.createElement('div');
                item.className = 'mc-preview-item';

                const videoEl = document.createElement('video');
                videoEl.src = URL.createObjectURL(video);
                videoEl.controls = true;

                const name = document.createElement('span');
                name.textContent = video.name;

                item.appendChild(videoEl);
                item.appendChild(name);
                preview.appendChild(item);
            }
        }

        function validateImages() {
            if (!imageInput) return true;

            const files = Array.from(imageInput.files || []);

            if (files.length > MAX_IMAGES) {
                alert('Bạn chỉ được upload tối đa 5 ảnh.');
                imageInput.value = '';
                renderPreview();
                return false;
            }

            for (const file of files) {
                const allowed = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];

                if (!allowed.includes(file.type)) {
                    alert('Ảnh chỉ hỗ trợ JPG, PNG hoặc WEBP.');
                    imageInput.value = '';
                    renderPreview();
                    return false;
                }

                if (file.size > MAX_IMAGE_SIZE) {
                    alert('Ảnh "' + file.name + '" vượt quá 5MB. Dung lượng hiện tại: ' + formatSize(file.size));
                    imageInput.value = '';
                    renderPreview();
                    return false;
                }
            }

            renderPreview();
            return true;
        }

        function validateVideo() {
            if (!videoInput) return true;

            const file = videoInput.files && videoInput.files[0];

            if (!file) {
                renderPreview();
                return true;
            }

            const allowed = ['video/mp4', 'video/webm'];

            if (!allowed.includes(file.type)) {
                alert('Video chỉ hỗ trợ MP4 hoặc WEBM.');
                videoInput.value = '';
                renderPreview();
                return false;
            }

            if (file.size > MAX_VIDEO_SIZE) {
                alert('Video vượt quá 50MB. Dung lượng hiện tại: ' + formatSize(file.size));
                videoInput.value = '';
                renderPreview();
                return false;
            }

            renderPreview();
            return true;
        }

        if (imageInput) {
            imageInput.addEventListener('change', validateImages);
        }

        if (videoInput) {
            videoInput.addEventListener('change', validateVideo);
        }

        if (form) {
            form.addEventListener('submit', function (event) {
                const orderId = orderIdInput ? orderIdInput.value.trim() : '';
                const productId = productIdInput ? productIdInput.value.trim() : '';
                const length = comment ? comment.value.trim().length : 0;

                if (!orderId || !productId) {
                    event.preventDefault();
                    alert('Thiếu mã đơn hàng hoặc mã sản phẩm. Vui lòng quay lại chi tiết đơn hàng rồi bấm đánh giá lại.');
                    return;
                }

                if (length < 10) {
                    event.preventDefault();
                    alert('Vui lòng nhập nội dung đánh giá ít nhất 10 ký tự.');
                    if (comment) comment.focus();
                    return;
                }

                if (!validateImages() || !validateVideo()) {
                    event.preventDefault();
                }
            });
        }

        const openBtn = document.getElementById('openRewardPolicyBtn');
        const closeBtn = document.getElementById('closeRewardPolicyBtn');
        const modal = document.getElementById('rewardPolicyModal');
        const backdrop = document.getElementById('rewardPolicyBackdrop');

        function openModal() {
            if (!modal) return;
            modal.classList.add('show');
            modal.setAttribute('aria-hidden', 'false');
            document.body.style.overflow = 'hidden';
        }

        function closeModal() {
            if (!modal) return;
            modal.classList.remove('show');
            modal.setAttribute('aria-hidden', 'true');
            document.body.style.overflow = '';
        }

        if (openBtn) openBtn.addEventListener('click', openModal);
        if (closeBtn) closeBtn.addEventListener('click', closeModal);
        if (backdrop) backdrop.addEventListener('click', closeModal);

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closeModal();
            }
        });
    })();
</script>
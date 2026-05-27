<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<c:choose>
    <c:when test="${not empty product.name}">
        <c:set var="productDisplayName" value="${product.name}" />
    </c:when>
    <c:when test="${not empty product.title}">
        <c:set var="productDisplayName" value="${product.title}" />
    </c:when>
    <c:otherwise>
        <c:set var="productDisplayName" value="Sản phẩm" />
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${not empty product.image}">
        <c:set var="rawProductImage" value="${product.image}" />
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
    <c:otherwise>
        <c:set var="orderItemIdValue" value="${param.orderItemId}" />
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
        color: #22243a;
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
        color: #ef4f93;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        text-decoration: none;
        font-size: 24px;
        font-weight: 800;
        box-shadow: 0 8px 20px rgba(239, 79, 147, 0.12);
    }

    .mc-review-title {
        margin: 0;
        font-size: 32px;
        font-weight: 900;
        color: #20243a;
        letter-spacing: -0.4px;
    }

    .mc-guide-bar {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 14px;
        margin-bottom: 18px;
        padding: 15px 18px;
        border-radius: 18px;
        background: #fff9e9;
        border: 1px solid #ffe1a8;
        color: #60420a;
        box-shadow: 0 10px 24px rgba(255, 193, 7, 0.12);
    }

    .mc-guide-left {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 15px;
        font-weight: 750;
        line-height: 1.5;
    }

    .mc-coin {
        width: 34px;
        height: 34px;
        border-radius: 50%;
        background: linear-gradient(135deg, #ffdb60, #ffb300);
        color: #fff;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-weight: 900;
        box-shadow: 0 8px 16px rgba(255, 179, 0, 0.25);
    }

    .mc-guide-highlight {
        color: #f08a00;
        font-weight: 900;
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
        border: 1px solid #eef0f6;
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
        color: #20243a;
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
        color: #20243a;
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
        color: #ffc107;
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
        color: #7d8398;
        font-size: 13px;
        font-weight: 700;
    }

    .mc-media-box {
        border: 2px dashed #d7dbe6;
        border-radius: 18px;
        padding: 22px;
        background: #fff;
        text-align: center;
        transition: border-color 0.18s ease, background 0.18s ease;
    }

    .mc-media-box:hover {
        border-color: #ffc107;
        background: #fffdf5;
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

    .mc-media-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 14px;
        text-align: left;
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

    .mc-field input,
    .mc-field textarea {
        width: 100%;
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

    .mc-field input {
        height: 48px;
        padding: 0 14px;
    }

    .mc-field textarea {
        min-height: 210px;
        padding: 16px;
        resize: vertical;
    }

    .mc-field input:focus,
    .mc-field textarea:focus {
        border-color: #ef4f93;
        background: #fffafd;
        box-shadow: 0 0 0 4px rgba(239, 79, 147, 0.12);
    }

    .mc-field input::placeholder,
    .mc-field textarea::placeholder {
        color: #a7adbc;
        font-weight: 550;
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
        align-items: center;
        gap: 10px;
        margin-top: 14px;
        color: #50566d;
        font-size: 15px;
        font-weight: 700;
    }

    .mc-checkbox-row input {
        width: 22px;
        height: 22px;
        accent-color: #ef4f93;
    }

    .mc-service-card {
        margin-top: 20px;
        padding: 22px;
        border-radius: 22px;
        background: #fff;
        border: 1px solid #eef0f6;
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
        color: #20243a;
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
        color: #ffc107;
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
        background: #fff0f7;
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
        transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
    }

    .mc-btn-secondary {
        background: #fff;
        color: #2d344c;
        border: 1px solid #dfe4ee;
    }

    .mc-btn-secondary:hover {
        background: #f8f9fc;
        transform: translateY(-1px);
    }

    .mc-btn-primary {
        color: #fff;
        background: linear-gradient(135deg, #ff6aa9 0%, #ef4f93 50%, #ff7043 100%);
        box-shadow: 0 12px 26px rgba(239, 79, 147, 0.28);
    }

    .mc-btn-primary:hover {
        transform: translateY(-2px);
        box-shadow: 0 16px 34px rgba(239, 79, 147, 0.34);
    }

    @media (max-width: 768px) {
        .mc-review-page {
            padding: 18px 10px 70px;
        }

        .mc-review-title {
            font-size: 26px;
        }

        .mc-card-body {
            padding: 18px;
        }

        .mc-review-card {
            border-radius: 20px;
        }

        .mc-guide-bar {
            align-items: flex-start;
        }

        .mc-product-row {
            align-items: flex-start;
        }

        .mc-product-thumb {
            width: 64px;
            height: 64px;
            flex-basis: 64px;
        }

        .mc-product-name {
            font-size: 15px;
        }

        .mc-star {
            width: 44px;
            height: 44px;
            font-size: 38px;
        }

        .mc-media-grid {
            grid-template-columns: 1fr;
        }

        .mc-service-row {
            grid-template-columns: 1fr;
            gap: 8px;
        }

        .mc-actions {
            flex-direction: column-reverse;
            align-items: stretch;
        }

        .mc-btn {
            width: 100%;
        }
    }
</style>

<main class="mc-review-page">
    <div class="mc-review-wrap">

        <div class="mc-review-top">
            <a class="mc-review-back" href="${ctx}/orders/detail?id=${order.id}">‹</a>
            <h1 class="mc-review-title">Đánh giá sản phẩm</h1>
        </div>

        <div class="mc-guide-bar">
            <div class="mc-guide-left">
                <span class="mc-coin">S</span>
                <span>
                    Đánh giá chuẩn để nhận voucher cho lần mua sau.
                    <span class="mc-guide-highlight">Nhận tối đa 600 xu!</span>
                </span>
            </div>
            <span>›</span>
        </div>

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
                            <span>Đơn hàng #${order.id}</span>
                            <span>Sản phẩm #${product.id}</span>

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

                <form method="post" action="${ctx}/orders/review/submit" id="reviewForm">
                    <input type="hidden" name="orderId" value="${order.id}">
                    <input type="hidden" name="productId" value="${product.id}">
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
                                <p class="mc-section-sub">Thêm ít nhất 1 hình ảnh hoặc video để đánh giá sinh động hơn.</p>
                            </div>
                            <span class="mc-point">+200 xu</span>
                        </div>

                        <div class="mc-media-box">
                            <div class="mc-media-icon">📷</div>
                            <p class="mc-media-title">Hình ảnh / Video minh họa</p>
                            <p class="mc-media-hint">
                                Hiện tại nhập URL ảnh/video. Sau này có thể nâng cấp thành upload file trực tiếp.
                            </p>

                            <div class="mc-media-grid">
                                <div class="mc-field">
                                    <label for="reviewImage">Ảnh minh họa URL</label>
                                    <input type="text"
                                           id="reviewImage"
                                           name="reviewImage"
                                           value="${param.reviewImage}"
                                           placeholder="https://.../anh-danh-gia.jpg">
                                </div>

                                <div class="mc-field">
                                    <label for="reviewVideo">Video minh họa URL</label>
                                    <input type="text"
                                           id="reviewVideo"
                                           name="reviewVideo"
                                           value="${param.reviewVideo}"
                                           placeholder="https://.../video-danh-gia.mp4">
                                </div>
                            </div>
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
                            <span>Đánh giá ẩn danh</span>
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
                        <a class="mc-btn mc-btn-secondary" href="${ctx}/orders/detail?id=${order.id}">
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

            ratingText.textContent = labels[value].title;
            ratingHint.textContent = labels[value].hint;
        });

        setupStars('.mc-service-stars', '.mc-service-star', 'active');

        const comment = document.getElementById('comment');
        const commentCount = document.getElementById('commentCount');
        const form = document.getElementById('reviewForm');

        function updateCount() {
            commentCount.textContent = comment.value.trim().length;
        }

        if (comment) {
            comment.addEventListener('input', updateCount);
            updateCount();
        }

        if (form) {
            form.addEventListener('submit', function (event) {
                const length = comment.value.trim().length;

                if (length < 10) {
                    event.preventDefault();
                    alert('Vui lòng nhập nội dung đánh giá ít nhất 10 ký tự.');
                    comment.focus();
                }
            });
        }
    })();
</script>
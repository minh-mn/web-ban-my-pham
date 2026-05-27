<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<style>
    .review-write-page {
        padding: 34px 0;
        background: #fff7fb;
        min-height: 70vh;
    }
    .review-write-container {
        width: min(900px, calc(100vw - 32px));
        margin: 0 auto;
    }
    .review-write-card {
        background: #fff;
        border: 1px solid #f0e8ee;
        border-radius: 24px;
        padding: 26px;
        box-shadow: 0 14px 34px rgba(31, 42, 68, 0.08);
    }
    .review-write-title {
        margin: 0 0 8px;
        color: #1f2a44;
        font-size: 26px;
        font-weight: 950;
    }
    .review-write-subtitle {
        margin: 0 0 22px;
        color: #64748b;
        font-size: 14px;
        font-weight: 700;
        line-height: 1.5;
    }
    .review-product-box {
        display: flex;
        gap: 16px;
        align-items: center;
        padding: 16px;
        border: 1px solid #eef2f7;
        border-radius: 18px;
        background: #f8fafc;
        margin-bottom: 20px;
    }
    .review-product-box img {
        width: 76px;
        height: 76px;
        border-radius: 16px;
        object-fit: cover;
        background: #fff;
        border: 1px solid #eef2f7;
    }
    .review-product-box strong {
        display: block;
        color: #1f2a44;
        font-size: 16px;
        font-weight: 950;
        line-height: 1.35;
    }
    .review-product-box span {
        display: block;
        margin-top: 5px;
        color: #64748b;
        font-size: 13px;
        font-weight: 750;
    }
    .review-form-grid {
        display: grid;
        gap: 16px;
    }
    .review-field label {
        display: block;
        margin-bottom: 8px;
        color: #334155;
        font-weight: 900;
        font-size: 14px;
    }
    .review-rating-input {
        display: flex;
        gap: 8px;
        align-items: center;
    }
    .review-star-choice {
        width: 44px;
        height: 44px;
        border-radius: 14px;
        border: 1px solid #f7b8d3;
        background: #fff3f8;
        color: #d63384;
        font-size: 22px;
        font-weight: 950;
        cursor: pointer;
        transition: 0.15s ease;
    }
    .review-star-choice.active,
    .review-star-choice:hover {
        background: #d63384;
        color: #fff;
        transform: translateY(-1px);
    }
    .review-textarea,
    .review-input {
        width: 100%;
        border: 1px solid #dbe3ef;
        border-radius: 16px;
        padding: 13px 14px;
        color: #1f2a44;
        background: #fff;
        font-size: 14px;
        font-weight: 700;
        box-sizing: border-box;
    }
    .review-textarea {
        min-height: 150px;
        resize: vertical;
        line-height: 1.55;
    }
    .review-help {
        margin-top: 6px;
        color: #94a3b8;
        font-size: 12.5px;
        font-weight: 700;
    }
    .review-alert {
        padding: 12px 14px;
        border-radius: 14px;
        margin-bottom: 16px;
        background: #fff1f2;
        color: #be123c;
        border: 1px solid #fecdd3;
        font-weight: 800;
    }
    .review-actions {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 12px;
        margin-top: 8px;
    }
    .review-back,
    .review-submit {
        min-height: 44px;
        padding: 0 18px;
        border-radius: 14px;
        font-weight: 900;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        justify-content: center;
    }
    .review-back {
        color: #334155;
        border: 1px solid #e5e7eb;
        background: #fff;
    }
    .review-submit {
        border: none;
        color: #fff;
        background: linear-gradient(180deg, #f45ea7 0%, #d63384 100%);
        box-shadow: 0 12px 24px rgba(214, 51, 132, 0.22);
        cursor: pointer;
    }
</style>

<section class="review-write-page">
    <div class="review-write-container">
        <div class="review-write-card">
            <h1 class="review-write-title">Đánh giá sản phẩm</h1>
            <p class="review-write-subtitle">
                Đánh giá sẽ được gửi đến quản trị viên duyệt trước khi hiển thị công khai. Sau khi được duyệt, bạn sẽ nhận voucher giảm giá cho lần mua tiếp theo.
            </p>

            <c:if test="${not empty error}">
                <div class="review-alert">
                    <c:choose>
                        <c:when test="${error == 'not_eligible'}">Bạn chỉ có thể đánh giá sản phẩm đã mua và đã giao thành công.</c:when>
                        <c:when test="${error == 'comment_required'}">Nội dung đánh giá cần ít nhất 5 ký tự.</c:when>
                        <c:otherwise>Không thể gửi đánh giá. Vui lòng thử lại.</c:otherwise>
                    </c:choose>
                </div>
            </c:if>

            <div class="review-product-box">
                <c:if test="${not empty reviewable.productImage}">
                    <img src="${pageContext.request.contextPath}${reviewable.productImage}" alt="${fn:escapeXml(reviewable.productName)}">
                </c:if>
                <div>
                    <strong><c:out value="${reviewable.productName}" /></strong>
                    <span>Đơn hàng #${reviewable.orderId} · Sản phẩm #${reviewable.productId}</span>
                </div>
            </div>

            <form method="post" action="${pageContext.request.contextPath}/orders/review/submit" class="review-form-grid">
                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                <input type="hidden" name="orderId" value="${reviewable.orderId}" />
                <input type="hidden" name="orderItemId" value="${reviewable.orderItemId}" />
                <input type="hidden" name="productId" value="${reviewable.productId}" />
                <input type="hidden" name="slug" value="${reviewable.productSlug}" />
                <input type="hidden" name="rating" id="ratingInput" value="5" />

                <div class="review-field">
                    <label>Chọn số sao</label>
                    <div class="review-rating-input" id="reviewStarGroup">
                        <button type="button" class="review-star-choice active" data-value="1">★</button>
                        <button type="button" class="review-star-choice active" data-value="2">★</button>
                        <button type="button" class="review-star-choice active" data-value="3">★</button>
                        <button type="button" class="review-star-choice active" data-value="4">★</button>
                        <button type="button" class="review-star-choice active" data-value="5">★</button>
                    </div>
                </div>

                <div class="review-field">
                    <label>Nội dung đánh giá</label>
                    <textarea class="review-textarea" name="comment" required minlength="5"
                              placeholder="Sản phẩm dùng như thế nào? Giao hàng, đóng gói, chất lượng ra sao?"></textarea>
                </div>

                <div class="review-field">
                    <label>Ảnh minh họa URL</label>
                    <input class="review-input" type="url" name="imageUrl" placeholder="https://.../anh-danh-gia.jpg">
                    <div class="review-help">Tạm thời nhập URL ảnh. Sau này có thể nâng cấp thành upload file trực tiếp.</div>
                </div>

                <div class="review-field">
                    <label>Video minh họa URL</label>
                    <input class="review-input" type="url" name="videoUrl" placeholder="https://.../video-danh-gia.mp4">
                    <div class="review-help">Dùng để hỗ trợ bộ lọc đánh giá có hình ảnh/video.</div>
                </div>

                <div class="review-actions">
                    <a class="review-back" href="${pageContext.request.contextPath}/orders/detail?id=${reviewable.orderId}">Quay lại đơn hàng</a>
                    <button class="review-submit" type="submit">Gửi đánh giá chờ duyệt</button>
                </div>
            </form>
        </div>
    </div>
</section>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const ratingInput = document.getElementById("ratingInput");
        const stars = document.querySelectorAll(".review-star-choice");
        let rating = 5;

        function render(value) {
            stars.forEach(function (star) {
                const v = Number(star.dataset.value);
                star.classList.toggle("active", v <= value);
            });
        }

        stars.forEach(function (star) {
            star.addEventListener("click", function () {
                rating = Number(star.dataset.value);
                if (ratingInput) ratingInput.value = rating;
                render(rating);
            });
        });

        render(rating);
    });
</script>

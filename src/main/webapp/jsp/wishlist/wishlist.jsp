<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" href="${ctx}/assets/css/product-list.css">
<link rel="stylesheet" href="${ctx}/assets/css/wishlist.css">

<main class="wishlist-page">

    <section class="contact-hero">
        <div class="contact-hero__glow contact-hero__glow--left"></div>
        <div class="contact-hero__glow contact-hero__glow--right"></div>

        <div class="contact-container contact-hero__inner">
            <div class="contact-kicker">
                <span></span>
                Wishlist MyCosmetic
            </div>

            <h1>Danh sách yêu thích</h1>
            <p>
                Những sản phẩm bạn đã lưu lại để mua sau. Bạn có thể xoá hoặc thêm lại bất kỳ lúc nào.
            </p>

            <div class="contact-hero__stats">
                <div>
                    <strong>${fn:length(products)}</strong>
                    <span>Sản phẩm đã lưu</span>
                </div>
                <div>
                    <strong>1 chạm</strong>
                    <span>Xoá / thêm lại</span>
                </div>
                <div>
                    <strong>Theo dõi</strong>
                    <span>Danh sách mua sắm riêng của bạn</span>
                </div>
            </div>
        </div>
    </section>

    <section class="contact-container contact-main wishlist-main">

        <section class="contact-form-card wishlist-panel">
            <div class="contact-form-head">
                <span class="contact-form-tag">Sản phẩm đã lưu</span>
                <h2>Wishlist của bạn</h2>
                <p>
                    Danh sách sản phẩm bạn đã đánh dấu yêu thích để theo dõi hoặc mua sau.
                </p>
            </div>

            <div class="wishlist-title-bar">
                <div class="wishlist-title-bar__left">
                    <i class="fa-solid fa-heart"></i>
                    <span>Danh sách sản phẩm</span>
                </div>

                <div class="wishlist-title-bar__right">
                    <span>${fn:length(products)} sản phẩm</span>
                </div>
            </div>

            <div class="product-grid collection-grid wishlist-grid">
                <c:choose>
                    <c:when test="${not empty products}">
                        <c:forEach var="p" items="${products}">
                            <article class="product-card collection-card" id="wishlist-item-${p.id}">
                                <div class="card-image-wrap">
                                    <img src="${ctx}${p.image}"
                                         alt="${p.title}"
                                         class="card-img"
                                         onerror="this.onerror=null; this.style.display='none'; this.parentElement.querySelector('.no-image-placeholder').style.display='flex';">

                                    <div class="no-image-placeholder">
                                        <span>Chưa có ảnh</span>
                                    </div>
                                </div>

                                <div class="card-content">
                                    <h3 class="product-title">
                                        <a href="${ctx}/product/detail?id=${p.id}">${p.title}</a>
                                    </h3>
                                    <p class="product-price">
                                        <fmt:formatNumber value="${not empty p.finalPrice ? p.finalPrice : p.price}" type="number" pattern="#,###"/>₫
                                    </p>
                                </div>

                                <form method="post" action="${ctx}/wishlist/toggle" class="wishlist-form">
                                    <input type="hidden" name="productId" value="${p.id}">
                                    <button type="submit" class="wishlist-btn" title="Yêu thích">
                                        <i class="fa-solid fa-heart heart-icon active"></i>
                                    </button>
                                </form>
                            </article>
                        </c:forEach>
                    </c:when>

                    <c:otherwise>
                        <div class="wishlist-empty">
                            <div class="wishlist-empty-icon">
                                <i class="fa-regular fa-heart"></i>
                            </div>
                            <h3 class="wishlist-empty-title">Danh sách trống</h3>
                            <p>Bạn chưa lưu sản phẩm nào vào danh mục yêu thích.</p>
                            <a href="${ctx}/products" class="contact-submit btn-continue-shopping">
                                <span>Tiếp tục mua sắm</span>
                                <i class="fa-solid fa-arrow-right"></i>
                            </a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

    </section>
</main>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        document.querySelectorAll(".wishlist-form").forEach(form => {
            form.addEventListener("submit", function (e) {
                e.preventDefault();

                const btn = this.querySelector(".wishlist-btn");
                const icon = this.querySelector("i");
                const formData = new URLSearchParams(new FormData(this));
                const productId = formData.get("productId");

                fetch(this.action, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    body: formData
                })
                    .then(async response => {
                        const text = await response.text();
                        try {
                            return JSON.parse(text);
                        } catch (e) {
                            return { status: text };
                        }
                    })
                    .then(data => {
                        const wishlisted = data.wishlisted === true || data.status === "ADDED";

                        if (!wishlisted) {
                            const card = document.getElementById("wishlist-item-" + productId);
                            if (card) {
                                card.style.transition = "all 0.35s cubic-bezier(0.4,0,0.2,1)";
                                card.style.opacity = "0";
                                card.style.transform = "scale(0.9) translateY(10px)";

                                setTimeout(() => {
                                    card.remove();

                                    const remain = document.querySelectorAll(".collection-card");
                                    if (remain.length === 0) {
                                        window.location.reload();
                                    }
                                }, 350);
                            }
                        } else {
                            btn?.classList.add("active");
                            if (icon) {
                                icon.className = "fa-solid fa-heart heart-icon active";
                            }
                        }
                    })
                    .catch(err => {
                        console.error("Lỗi Wishlist:", err);
                    });
            });
        });
    });
</script>

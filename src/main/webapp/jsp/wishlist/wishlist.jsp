<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" href="${ctx}/assets/css/product-list.css">
<link rel="stylesheet" href="${ctx}/assets/css/wishlist.css">


<section class="section wishlist-section">
    <div class="container wishlist-page-header" style="text-align:center; max-width:760px; margin-bottom: 40px; margin-top: 10px;">
        <h2>Danh sách yêu thích của bạn</h2>
        <p>Những sản phẩm bạn đã lưu lại để mua sau.</p>
    </div>

    <div class="container collection-container">
        <div class="product-grid collection-grid">
            <c:choose>
                <c:when test="${not empty products}">
                    <c:forEach var="p" items="${products}">
                        <article class="product-card collection-card" id="wishlist-item-${p.id}">

                                <%-- 1. Hình ảnh --%>
                            <div class="card-image-wrap">
                                <img src="${ctx}${p.image}" alt="${p.title}" class="card-img"
                                     onerror="this.onerror=null; this.style.display='none'; this.parentElement.querySelector('.no-image-placeholder').style.display='flex';">

                                    <%-- Khối hiển thị khi không có ảnh gốc hoặc ảnh bị lỗi mạng --%>
                                <div class="no-image-placeholder">
                                    <span>Chưa có ảnh</span>
                                </div>
                            </div>

                                <%-- 2. Thông tin chữ --%>
                            <div class="card-content">
                                <h3 class="product-title">
                                    <a href="${ctx}/product/detail?id=${p.id}">${p.title}</a>
                                </h3>
                                <p class="product-price">
                                    <fmt:formatNumber value="${not empty p.finalPrice ? p.finalPrice : p.price}" type="number" pattern="#,###"/>₫
                                </p>
                            </div>

                                <%-- 3. Nút xóa dạng Trái Tim Nổi độc lập --%>
                                    <form method="post" action="${ctx}/wishlist/toggle" class="wishlist-form">
                                        <input type="hidden" name="productId" value="${p.id}">
                                        <button type="submit" class="wishlist-btn" title="Yêu thích">
                                            <i class="fa-solid fa-heart heart-icon active"></i>                                        </button>
                                    </form>
                        </article>
                    </c:forEach>
                </c:when>

                <%-- TRẠNG THÁI DANH SÁCH TRỐNG --%>
                <c:otherwise>
                    <div class="wishlist-empty">
                        <div class="wishlist-empty-content" style="text-align: center;">
                            <div class="wishlist-empty-icon"><i class="fa-regular fa-heart"></i></div>
                            <h3 class="wishlist-empty-title">Danh sách trống</h3>
                            <p>Bạn chưa lưu sản phẩm nào vào danh mục yêu thích.</p>
                            <a href="${ctx}/products" class="btn-continue-shopping">
                                Tiếp tục mua sắm
                            </a>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</section>

<%-- JS xử lý bất đồng bộ JSON để đổi màu / xóa trực tiếp card sản phẩm --%>
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

                        const wishlisted =
                            data.wishlisted === true ||
                            data.status === "ADDED";

                        /* Nếu đã xóa khỏi wishlist */
                        if (!wishlisted) {

                            const card = document.getElementById(
                                "wishlist-item-" + productId
                            );

                            if (card) {
                                card.style.transition =
                                    "all 0.35s cubic-bezier(0.4,0,0.2,1)";
                                card.style.opacity = "0";
                                card.style.transform =
                                    "scale(0.9) translateY(10px)";

                                setTimeout(() => {
                                    card.remove();

                                    /* Nếu không còn sản phẩm nào */
                                    const remain =
                                        document.querySelectorAll(
                                            ".collection-card"
                                        );

                                    if (remain.length === 0) {
                                        window.location.reload();
                                    }
                                }, 350);
                            }

                        } else {
                            /* Trường hợp thêm lại */
                            btn?.classList.add("active");

                            if (icon) {
                                icon.className =
                                    "fa-solid fa-heart heart-icon active";
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

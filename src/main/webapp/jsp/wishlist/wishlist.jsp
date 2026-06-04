<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" href="${ctx}/assets/css/product-list.css">

<section class="section">
    <div class="container page-header" style="text-align:center; max-width:760px; margin-bottom: 40px; margin-top: 30px;">
        <h2 class="section-title" style="font-size: 28px; font-weight: 800; color: #222; margin-bottom: 8px;">Danh sách yêu thích của bạn</h2>
        <p style="color:#666; font-size:16px;">Những sản phẩm bạn đã lưu lại để mua sau.</p>
    </div>

    <div class="container collection-container">
        <div class="product-grid collection-grid">

            <c:choose>
                <c:when test="${not empty products}">
                    <%-- CHỈ DÙNG 1 VÒNG LẶP DUY NHẤT --%>
                    <c:forEach var="p" items="${products}">
                        <article class="product-card collection-card" id="wishlist-item-${p.id}" style="border: 1px solid #eee; position: relative;">

                                <%-- 1. Hình ảnh --%>
                                    <div class="card-image-wrap">
                                        <img src="${ctx}${p.image}" alt="${p.title}" class="card-img"
                                             onerror="this.onerror=null; this.style.display='none'; this.parentElement.querySelector('.no-image-placeholder').style.display='flex';">
                                            <%-- Khối hiển thị khi không có ảnh --%>
                                        <div class="no-image-placeholder">
                                            <span>Chưa có ảnh</span>
                                        </div>
                                    </div>

                                <%-- 2. Thông tin --%>
                            <div class="card-content" style="padding: 10px;">
                                <h3 class="product-title" style="font-size: 16px; margin-bottom: 5px;">
                                    <a href="${ctx}/product/detail?id=${p.id}">${p.title}</a>
                                </h3>
                                <p class="product-price" style="color: #d92c74; font-weight: 600;">
                                    <fmt:formatNumber value="${not empty p.finalPrice ? p.finalPrice : p.price}" type="number" pattern="#,###"/>₫
                                </p>
                            </div>

                                <%-- 3. Nút xóa --%>
                            <form method="post" action="${ctx}/wishlist/toggle" class="wishlist-form" style="position: absolute; top: 10px; right: 10px;">
                                <input type="hidden" name="productId" value="${p.id}">
                                <button type="submit" class="wishlist-btn active" style="background:none; border:none; cursor:pointer;">
                                    <i class="fa-solid fa-heart" style="color: #ff5fa2; font-size: 20px;"></i>
                                </button>
                            </form>
                        </article>
                    </c:forEach>
                </c:when>

                <%-- KHI DANH SÁCH TRỐNG --%>
                <c:otherwise>
                    <div class="wishlist-empty" style="grid-column: 1 / -1; display: flex; align-items: center; justify-content: center; min-height: 350px;">
                        <div class="wishlist-empty-content" style="text-align: center;">
                            <div class="wishlist-empty-icon" style="color: #ff5fa2; font-size: 56px; margin-bottom: 16px;"><i class="fa-regular fa-heart"></i></div>
                            <h3 class="wishlist-empty-title" style="color: #222; font-size: 22px; margin-bottom: 8px;">Danh sách trống</h3>
                            <p style="color: #666; margin-bottom: 24px;">Bạn chưa lưu sản phẩm nào vào danh mục yêu thích.</p>
                            <a href="${ctx}/products" class="collection-card__cart-btn" style="display: inline-block; padding: 12px 30px; text-decoration: none; border-radius: 999px; background: #eee; color: #333;">
                                Tiếp tục mua sắm
                            </a>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</section>

<script>
    document.addEventListener("DOMContentLoaded", function() {
        const wishlistForms = document.querySelectorAll(".wishlist-form");

        wishlistForms.forEach(form => {
            form.addEventListener("submit", function(e) {
                e.preventDefault();
                const formData = new URLSearchParams(new FormData(this));
                const productId = formData.get("productId");

                fetch(this.action, {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: formData
                })
                    .then(response => response.text()) // Lấy text trả về
                    .then(data => {
                        if (data === "REMOVED") {
                            const card = document.getElementById("wishlist-item-" + productId);
                            if(card) {
                                card.style.opacity = '0';
                                card.style.transition = 'all 0.3s ease';
                                setTimeout(() => {
                                    card.remove();
                                    // Nếu xóa hết thì reload để hiện thông báo danh sách trống
                                    const remaining = document.querySelectorAll(".collection-card");
                                    if(remaining.length === 0) window.location.reload();
                                }, 300);
                            }
                        }
                    })
                    .catch(err => console.error("Lỗi Wishlist:", err));
            });
        });
    });
</script>

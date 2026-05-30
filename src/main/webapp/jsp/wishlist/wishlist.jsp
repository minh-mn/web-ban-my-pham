<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 30/05/2026
  Time: 12:26 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" href="${ctx}/assets/css/product-list.css">

<section class="section">
    <div class="container page-header" style="text-align:center; max-width:760px; margin-bottom: 40px;">
        <h2 class="section-title">Danh sách yêu thích của bạn</h2>
        <p style="color:#666; font-size:16px;">Những sản phẩm bạn đã lưu lại để mua sau.</p>
    </div>

    <div class="container">
        <div class="product-grid">
            <c:choose>
                <c:when test="${not empty products}">
                    <c:forEach var="product" items="${products}">
                        <div class="product-card" id="wishlist-item-${product.id}" style="position: relative;">

                            <form method="post" action="${ctx}/wishlist/toggle" class="wishlist-form" style="position: absolute; top: 10px; right: 10px; z-index: 10;">
                                <input type="hidden" name="productId" value="${product.id}" />
                                <button type="submit" class="wishlist-btn" title="Bỏ yêu thích"
                                        style="background: transparent; border: none; font-size: 24px; cursor: pointer; color: red;">
                                    ❤
                                </button>
                            </form>

                            <a href="${ctx}/product/${product.slug}" class="product-img-link">
                                <div class="product-img-box">
                                    <c:choose>
                                        <c:when test="${not empty product.imageUrl}">
                                            <img src="${ctx}${product.imageUrl}" alt="${product.title}">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="no-image">No image</div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </a>

                            <h3>
                                <a href="${ctx}/product/${product.slug}" class="product-title-link">
                                    <c:out value="${product.title}" />
                                </a>
                            </h3>

                            <a href="${ctx}/product/${product.slug}" class="btn-outline">
                                Xem chi tiết
                            </a>
                        </div>
                    </c:forEach>
                </c:when>

                <c:otherwise>
                    <div style="text-align: center; width: 100%; padding: 50px 0;">
                        <h3>Bạn chưa có sản phẩm yêu thích nào.</h3>
                        <a href="${ctx}/products" class="btn-outline" style="margin-top: 20px; display: inline-block;">Tiếp tục mua sắm</a>
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
                    .then(response => {
                        if(response.ok) return response.text();
                    })
                    .then(data => {
                        if (data === "REMOVED") {
                            // Hiệu ứng ẩn sản phẩm khỏi grid khi xóa
                            const card = document.getElementById("wishlist-item-" + productId);
                            if(card) {
                                card.style.display = 'none';
                            }
                        }
                    });
            });
        });
    });
</script>

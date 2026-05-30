<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isFlashSection" value="${homeSectionMode == 'flash'}" />

<c:if test="${not empty homeSectionProducts}">
    <section class="skin-product-section ${isFlashSection ? 'is-flash' : ''}">
        <div class="skin-container">

            <div class="skin-section-top ${isFlashSection ? 'flash-top' : ''}">
                <div>
                    <c:choose>
                        <c:when test="${isFlashSection}">
                            <div class="skin-flash-title">
                                <span class="flash-word">FLASH</span>
                                <span class="bolt">⚡</span>
                                <span class="deal-word">DEAL</span>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <span class="skin-eyebrow">MYCOSMETICSHOP</span>
                            <h2>${homeSectionTitle}</h2>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${not empty homeSectionDesc}">
                        <p>${homeSectionDesc}</p>
                    </c:if>
                </div>

                <div class="skin-section-actions">
                    <c:if test="${isFlashSection}">
                        <div class="skin-countdown" data-deal-countdown>
                            <span data-hh>00</span>
                            <b>:</b>
                            <span data-mm>00</span>
                            <b>:</b>
                            <span data-ss>00</span>
                        </div>
                    </c:if>

                    <c:if test="${not empty homeSectionLink}">
                        <a class="skin-view-all" href="${ctx}${homeSectionLink}">
                            XEM TẤT CẢ
                        </a>
                    </c:if>
                </div>
            </div>

            <div class="skin-product-scroll ${isFlashSection ? 'flash-scroll' : ''}">
                <c:forEach var="product" items="${homeSectionProducts}">
                    <article class="skin-product-card ${isFlashSection ? 'flash-card' : ''}">

                        <a class="skin-product-image" href="${ctx}/product/${product.slug}">
                            <c:if test="${product.discountPercent > 0}">
                                <span class="skin-discount-bubble">-${product.discountPercent}%</span>
                            </c:if>

                            <c:if test="${isFlashSection}">
                                <span class="skin-card-label">FREESHIP TQ</span>
                            </c:if>

                            <c:choose>
                                <c:when test="${not empty product.imageUrl}">
                                    <c:choose>
                                        <c:when test="${fn:startsWith(product.imageUrl, 'http')}">
                                            <img src="${product.imageUrl}" alt="${product.title}">
                                        </c:when>
                                        <c:when test="${fn:startsWith(product.imageUrl, '/')}">
                                            <img src="${ctx}${product.imageUrl}" alt="${product.title}">
                                        </c:when>
                                        <c:otherwise>
                                            <img src="${ctx}/uploads/product/${product.imageUrl}" alt="${product.title}">
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <div class="skin-no-image">No image</div>
                                </c:otherwise>
                            </c:choose>
                        </a>

                        <div class="skin-product-body">
                            <a class="skin-product-title" href="${ctx}/product/${product.slug}">
                                    ${product.title}
                            </a>

                            <div class="skin-price-row">
                                <c:choose>
                                    <c:when test="${product.discountPercent > 0}">
                                        <strong>
                                            <fmt:formatNumber value="${product.finalPrice}" type="number" groupingUsed="true"/>đ
                                        </strong>
                                        <del>
                                            <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>đ
                                        </del>
                                    </c:when>
                                    <c:otherwise>
                                        <strong>
                                            <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>đ
                                        </strong>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <div class="skin-product-meta">
                                <c:if test="${homeSectionShowSold == true}">
                                    <span>${product.soldQuantity} đã bán</span>
                                </c:if>
                                <c:if test="${homeSectionShowViews == true}">
                                    <span>${product.viewCount} lượt xem</span>
                                </c:if>
                                <c:if test="${homeSectionShowDiscount == true && product.discountPercent > 0}">
                                    <span>Giảm ${product.discountPercent}%</span>
                                </c:if>
                                <c:if test="${product.reviewCount > 0}">
                                    <span>★ <fmt:formatNumber value="${product.avgRating}" maxFractionDigits="1"/> (${product.reviewCount})</span>
                                </c:if>
                            </div>

                            <c:choose>
                                <c:when test="${isFlashSection}">
                                    <div class="skin-progress">
                                        <span style="width: ${product.saleProgressPercent}%;"></span>
                                    </div>
                                    <div class="skin-progress-text">ĐANG DIỄN RA ${product.saleProgressPercent}%</div>
                                </c:when>
                                <c:otherwise>
                                    <div class="skin-stock-line">
                                        <c:choose>
                                            <c:when test="${product.stock == 0}">
                                                <span class="out">Hết hàng</span>
                                            </c:when>
                                            <c:when test="${product.stock <= 5}">
                                                <span class="low">Sắp hết hàng</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="ok">Còn hàng</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </div>
    </section>
</c:if>

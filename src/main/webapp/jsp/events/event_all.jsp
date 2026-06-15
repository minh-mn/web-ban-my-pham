<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/events-style.css">

<section class="store-events-section">
    <div class="events-container">

        <section class="events-hero">
            <div class="events-hero__content">
        <span class="events-kicker">
          <i class="fa-solid fa-sparkles"></i>
          MyCosmetic Newsroom
        </span>

                <h1>Tin tức, sự kiện và cẩm nang làm đẹp</h1>

                <p>
                    Cập nhật ưu đãi mới, sự kiện thành viên, xu hướng trang điểm và kiến thức chăm sóc da
                    được MyCosmetic chọn lọc dành riêng cho bạn.
                </p>

                <div class="events-hero__stats">
                    <div>
                        <strong>Deal hot</strong>
                        <span>Săn ưu đãi mỗi tuần</span>
                    </div>
                    <div>
                        <strong>Beauty tips</strong>
                        <span>Cẩm nang chăm da</span>
                    </div>
                    <div>
                        <strong>Member day</strong>
                        <span>Sự kiện thành viên</span>
                    </div>
                </div>
            </div>
        </section>

        <div class="events-toolbar">
            <div>
                <span class="events-section-label">Khám phá nội dung</span>
                <h2 class="events-title">Tin tức & sự kiện</h2>
            </div>

            <form action="${pageContext.request.contextPath}/blog" method="get" class="events-search-form">
                <i class="fa-solid fa-magnifying-glass"></i>
                <input type="text"
                       name="search"
                       value="<c:out value='${searchQuery}'/>"
                       class="events-search-input"
                       placeholder="Tìm kiếm bài viết, sự kiện...">
                <button type="submit" class="events-search-btn">Tìm kiếm</button>
            </form>
        </div>

        <div class="events-topic-row">
            <a href="${pageContext.request.contextPath}/blog" class="events-topic-chip is-active">Tất cả</a>
            <a href="${pageContext.request.contextPath}/blog?search=Flash%20sale" class="events-topic-chip">Flash sale</a>
            <a href="${pageContext.request.contextPath}/blog?search=Ch%C4%83m%20s%C3%B3c%20da" class="events-topic-chip">Chăm sóc da</a>
            <a href="${pageContext.request.contextPath}/blog?search=Trang%20%C4%91i%E1%BB%83m" class="events-topic-chip">Trang điểm</a>
            <a href="${pageContext.request.contextPath}/blog?search=Voucher" class="events-topic-chip">Voucher</a>
            <a href="${pageContext.request.contextPath}/blog?search=Th%C3%A0nh%20vi%C3%AAn" class="events-topic-chip">Thành viên</a>
        </div>

        <c:if test="${empty events}">
            <div class="events-note">
                <i class="fa-solid fa-circle-info"></i>
                <div>
                    <strong>
                        <c:choose>
                            <c:when test="${not empty searchQuery}">
                                Chưa tìm thấy bài viết phù hợp với từ khóa “<c:out value="${searchQuery}"/>”.
                            </c:when>
                            <c:otherwise>
                                Chưa có bài viết từ hệ thống quản trị.
                            </c:otherwise>
                        </c:choose>
                    </strong>
                    <span>MyCosmetic hiển thị các thông tin gợi ý bên dưới để trang tin tức không bị trống.</span>
                </div>
            </div>
        </c:if>

        <section class="events-featured">
            <article class="events-featured-card events-featured-card--pink">
                <div>
                    <span class="event-card-tag">Sự kiện</span>
                    <h3>Ngày hội thành viên MyCosmetic</h3>
                    <p>
                        Tích điểm, nhận voucher và mở khóa ưu đãi dành riêng cho khách hàng thân thiết.
                        Theo dõi lịch sự kiện để không bỏ lỡ quyền lợi thành viên.
                    </p>
                    <a href="${pageContext.request.contextPath}/vouchers">Khám phá ưu đãi</a>
                </div>
            </article>

            <article class="events-featured-card events-featured-card--dark">
                <div>
                    <span class="event-card-tag">Cẩm nang</span>
                    <h3>Chăm sóc da đúng bước, mua sắm đúng nhu cầu</h3>
                    <p>
                        Gợi ý cách chọn sản phẩm theo tình trạng da, ngân sách và mục tiêu chăm sóc cá nhân.
                    </p>
                    <a href="${pageContext.request.contextPath}/products?category=all">Xem sản phẩm</a>
                </div>
            </article>
        </section>

        <div class="events-grid">
            <c:choose>
                <c:when test="${not empty events}">
                    <c:forEach var="event" items="${events}">
                        <article class="event-custom-card">
                            <div class="event-card-img-wrapper">
                                <c:choose>
                                    <c:when test="${not empty event.imageUrl}">
                                        <img src="${pageContext.request.contextPath}${event.imageUrl}" alt="${event.title}">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="event-card-placeholder">
                                            <i class="fa-solid fa-newspaper"></i>
                                            <span>MyCosmetic</span>
                                        </div>
                                    </c:otherwise>
                                </c:choose>

                                <div class="event-card-date-badge">
                                    <span class="day"><fmt:formatDate value="${event.eventDate}" pattern="dd"/></span>
                                    <span class="month">Th<fmt:formatDate value="${event.eventDate}" pattern="MM"/></span>
                                </div>
                            </div>

                            <div class="event-card-body">
                                <span class="event-card-tag"><c:out value="${event.tag}"/></span>
                                <h3><c:out value="${event.title}"/></h3>
                                <p><c:out value="${event.summary}"/></p>

                                <a href="${pageContext.request.contextPath}/blog/detail?id=${event.id}" class="event-card-btn-action">
                                    Xem chi tiết
                                    <i class="fa-solid fa-arrow-right"></i>
                                </a>
                            </div>
                        </article>
                    </c:forEach>
                </c:when>

                <c:otherwise>
                    <article class="event-custom-card">
                        <div class="event-card-img-wrapper event-card-img-wrapper--gradient event-gradient-sale">
                            <div class="event-card-date-badge">
                                <span class="day">15</span>
                                <span class="month">Th06</span>
                            </div>
                            <div class="event-card-placeholder">
                                <i class="fa-solid fa-tags"></i>
                                <span>Flash Deal</span>
                            </div>
                        </div>

                        <div class="event-card-body">
                            <span class="event-card-tag">Flash sale</span>
                            <h3>Lịch săn deal hồng cuối tuần tại MyCosmetic</h3>
                            <p>Gợi ý các khung giờ mua mỹ phẩm giá tốt, ưu tiên voucher và sản phẩm bán chạy.</p>
                            <a href="${pageContext.request.contextPath}/vouchers" class="event-card-btn-action">
                                Xem ưu đãi
                                <i class="fa-solid fa-arrow-right"></i>
                            </a>
                        </div>
                    </article>

                    <article class="event-custom-card">
                        <div class="event-card-img-wrapper event-card-img-wrapper--gradient event-gradient-skin">
                            <div class="event-card-date-badge">
                                <span class="day">18</span>
                                <span class="month">Th06</span>
                            </div>
                            <div class="event-card-placeholder">
                                <i class="fa-solid fa-droplet"></i>
                                <span>Skin care</span>
                            </div>
                        </div>

                        <div class="event-card-body">
                            <span class="event-card-tag">Chăm sóc da</span>
                            <h3>Quy trình chăm sóc da buổi sáng cho da dầu</h3>
                            <p>Cách chọn sữa rửa mặt, toner, tinh chất và kem chống nắng để da thông thoáng hơn.</p>
                            <a href="${pageContext.request.contextPath}/products?category=all" class="event-card-btn-action">
                                Xem sản phẩm
                                <i class="fa-solid fa-arrow-right"></i>
                            </a>
                        </div>
                    </article>

                    <article class="event-custom-card">
                        <div class="event-card-img-wrapper event-card-img-wrapper--gradient event-gradient-member">
                            <div class="event-card-date-badge">
                                <span class="day">20</span>
                                <span class="month">Th06</span>
                            </div>
                            <div class="event-card-placeholder">
                                <i class="fa-solid fa-crown"></i>
                                <span>Member</span>
                            </div>
                        </div>

                        <div class="event-card-body">
                            <span class="event-card-tag">Thành viên</span>
                            <h3>Ưu đãi hạng thành viên: mua nhiều, nhận nhiều</h3>
                            <p>Tìm hiểu quyền lợi tích điểm, giảm giá theo hạng và voucher riêng cho khách hàng thân thiết.</p>
                            <a href="${pageContext.request.contextPath}/account" class="event-card-btn-action">
                                Xem tài khoản
                                <i class="fa-solid fa-arrow-right"></i>
                            </a>
                        </div>
                    </article>

                    <article class="event-custom-card">
                        <div class="event-card-img-wrapper event-card-img-wrapper--gradient event-gradient-makeup">
                            <div class="event-card-date-badge">
                                <span class="day">22</span>
                                <span class="month">Th06</span>
                            </div>
                            <div class="event-card-placeholder">
                                <i class="fa-solid fa-wand-magic-sparkles"></i>
                                <span>Makeup</span>
                            </div>
                        </div>

                        <div class="event-card-body">
                            <span class="event-card-tag">Trang điểm</span>
                            <h3>Xu hướng má hồng và son tint nhẹ nhàng mùa hè</h3>
                            <p>Gợi ý layout trang điểm trong trẻo, dễ dùng hằng ngày với tone hồng pastel.</p>
                            <a href="${pageContext.request.contextPath}/products?category=all" class="event-card-btn-action">
                                Khám phá ngay
                                <i class="fa-solid fa-arrow-right"></i>
                            </a>
                        </div>
                    </article>

                    <article class="event-custom-card">
                        <div class="event-card-img-wrapper event-card-img-wrapper--gradient event-gradient-voucher">
                            <div class="event-card-date-badge">
                                <span class="day">25</span>
                                <span class="month">Th06</span>
                            </div>
                            <div class="event-card-placeholder">
                                <i class="fa-solid fa-ticket"></i>
                                <span>Voucher</span>
                            </div>
                        </div>

                        <div class="event-card-body">
                            <span class="event-card-tag">Voucher</span>
                            <h3>Cách chọn mã giảm giá phù hợp trước khi thanh toán</h3>
                            <p>Kiểm tra điều kiện đơn tối thiểu, hạng thành viên và hạn sử dụng để áp mã hiệu quả.</p>
                            <a href="${pageContext.request.contextPath}/vouchers" class="event-card-btn-action">
                                Xem voucher
                                <i class="fa-solid fa-arrow-right"></i>
                            </a>
                        </div>
                    </article>

                    <article class="event-custom-card">
                        <div class="event-card-img-wrapper event-card-img-wrapper--gradient event-gradient-guide">
                            <div class="event-card-date-badge">
                                <span class="day">28</span>
                                <span class="month">Th06</span>
                            </div>
                            <div class="event-card-placeholder">
                                <i class="fa-solid fa-shield-heart"></i>
                                <span>Beauty guide</span>
                            </div>
                        </div>

                        <div class="event-card-body">
                            <span class="event-card-tag">Cẩm nang</span>
                            <h3>Checklist phục hồi da sau khi makeup nhiều ngày</h3>
                            <p>Làm sạch đúng cách, cấp ẩm vừa đủ và chọn sản phẩm phục hồi để da cân bằng lại.</p>
                            <a href="${pageContext.request.contextPath}/products?category=all" class="event-card-btn-action">
                                Xem gợi ý
                                <i class="fa-solid fa-arrow-right"></i>
                            </a>
                        </div>
                    </article>
                </c:otherwise>
            </c:choose>
        </div>

        <section class="events-info-strip">
            <div>
                <i class="fa-solid fa-calendar-check"></i>
                <h3>Lịch cập nhật nội dung</h3>
                <p>MyCosmetic ưu tiên cập nhật deal, bài tư vấn và thông tin thành viên vào mỗi tuần.</p>
            </div>

            <div>
                <i class="fa-solid fa-circle-question"></i>
                <h3>Cần tư vấn nhanh?</h3>
                <p>Liên hệ đội ngũ hỗ trợ để được gợi ý sản phẩm phù hợp với nhu cầu của bạn.</p>
            </div>

            <a href="${pageContext.request.contextPath}/lien-he">
                Liên hệ ngay
                <i class="fa-solid fa-arrow-right"></i>
            </a>
        </section>

    </div>
</section>

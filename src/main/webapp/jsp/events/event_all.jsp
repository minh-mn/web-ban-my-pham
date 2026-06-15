<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/events-style.css?v=20260615_blog_topic_fix_v3">

<c:set var="viewTopic" value="${not empty activeTopic ? activeTopic : param.topic}" />
<c:if test="${empty viewTopic}">
    <c:set var="viewTopic" value="all" />
</c:if>

<c:choose>
    <c:when test="${viewTopic eq 'flash-sale'}"><c:set var="viewTopicLabel" value="Flash sale" /></c:when>
    <c:when test="${viewTopic eq 'cham-soc-da'}"><c:set var="viewTopicLabel" value="Chăm sóc da" /></c:when>
    <c:when test="${viewTopic eq 'trang-diem'}"><c:set var="viewTopicLabel" value="Trang điểm" /></c:when>
    <c:when test="${viewTopic eq 'voucher'}"><c:set var="viewTopicLabel" value="Voucher" /></c:when>
    <c:when test="${viewTopic eq 'thanh-vien'}"><c:set var="viewTopicLabel" value="Thành viên" /></c:when>
    <c:when test="${viewTopic eq 'cam-nang'}"><c:set var="viewTopicLabel" value="Cẩm nang" /></c:when>
    <c:otherwise><c:set var="viewTopicLabel" value="Tất cả" /></c:otherwise>
</c:choose>

<c:set var="viewSearch" value="${not empty searchQuery ? searchQuery : param.search}" />
<c:if test="${empty viewSearch}">
    <c:set var="viewSearch" value="" />
</c:if>

<section class="mc-news-page" data-active-topic="${viewTopic}" data-search="${fn:escapeXml(viewSearch)}">
    <div class="mc-news-shell">

        <section class="mc-news-hero">
            <div class="mc-news-hero__content">
                <span class="mc-news-kicker">MyCosmetic News</span>

                <c:choose>
                    <c:when test="${viewTopic eq 'voucher'}">
                        <h1>Voucher và mã ưu đãi MyCosmetic</h1>
                        <p>Cập nhật mã giảm giá, freeship và điều kiện áp dụng để bạn chọn voucher phù hợp trước khi thanh toán.</p>
                    </c:when>
                    <c:when test="${viewTopic eq 'flash-sale'}">
                        <h1>Flash sale và deal đang chạy</h1>
                        <p>Theo dõi các khung giờ săn deal, sản phẩm giảm sâu và ưu đãi nổi bật trong tuần tại MyCosmetic.</p>
                    </c:when>
                    <c:when test="${viewTopic eq 'cham-soc-da'}">
                        <h1>Cẩm nang chăm sóc da</h1>
                        <p>Gợi ý quy trình, sản phẩm và mẹo chăm sóc da theo nhu cầu để bạn mua sắm chính xác hơn.</p>
                    </c:when>
                    <c:when test="${viewTopic eq 'trang-diem'}">
                        <h1>Xu hướng trang điểm</h1>
                        <p>Khám phá các gợi ý son môi, má hồng, phấn mắt và layout makeup dễ ứng dụng hằng ngày.</p>
                    </c:when>
                    <c:when test="${viewTopic eq 'thanh-vien'}">
                        <h1>Sự kiện và quyền lợi thành viên</h1>
                        <p>Cập nhật chương trình tích điểm, nâng hạng và ưu đãi riêng cho khách hàng thân thiết của MyCosmetic.</p>
                    </c:when>
                    <c:when test="${viewTopic eq 'cam-nang'}">
                        <h1>Cẩm nang mua sắm mỹ phẩm</h1>
                        <p>Tổng hợp kinh nghiệm chọn sản phẩm, đọc thông tin, kiểm tra ưu đãi và chăm sóc da sau khi trang điểm.</p>
                    </c:when>
                    <c:otherwise>
                        <h1>Tin tức, sự kiện và ưu đãi nổi bật</h1>
                        <p>Theo dõi sự kiện thành viên, khuyến mãi, voucher và cẩm nang làm đẹp để mua sắm tiện hơn mỗi ngày.</p>
                    </c:otherwise>
                </c:choose>

                <div class="mc-news-hero__meta">
                    <span><strong id="mcNewsCountTop"><c:out value="${not empty eventCount ? eventCount : 0}" /></strong> nội dung hiển thị</span>
                    <span><strong id="mcNewsTopicTop"><c:out value="${viewTopicLabel}" /></strong> đang được chọn</span>
                    <c:if test="${not empty viewSearch}">
                        <span>Từ khóa: <strong>"<c:out value="${viewSearch}"/>"</strong></span>
                    </c:if>
                </div>
            </div>
        </section>

        <section class="mc-news-toolbar">
            <form action="${pageContext.request.contextPath}/blog" method="get" class="mc-news-search">
                <input id="mcNewsTopicInput" type="hidden" name="topic" value="${viewTopic}">

                <div class="mc-news-search__box">
                    <span class="mc-news-search__icon">⌕</span>
                    <input type="text"
                           name="search"
                           value="<c:out value='${viewSearch}'/>"
                           placeholder="Tìm bài viết, voucher, thành viên, chăm sóc da...">
                </div>

                <button type="submit">Tìm kiếm</button>
            </form>
        </section>

        <section class="mc-news-topics" aria-label="Lọc nội dung tin tức">
            <a href="${pageContext.request.contextPath}/blog" data-topic="all"
               class="mc-news-chip ${viewTopic eq 'all' ? 'is-active' : ''}">Tất cả</a>
            <a href="${pageContext.request.contextPath}/blog?topic=flash-sale" data-topic="flash-sale"
               class="mc-news-chip ${viewTopic eq 'flash-sale' ? 'is-active' : ''}">Flash sale</a>
            <a href="${pageContext.request.contextPath}/blog?topic=cham-soc-da" data-topic="cham-soc-da"
               class="mc-news-chip ${viewTopic eq 'cham-soc-da' ? 'is-active' : ''}">Chăm sóc da</a>
            <a href="${pageContext.request.contextPath}/blog?topic=trang-diem" data-topic="trang-diem"
               class="mc-news-chip ${viewTopic eq 'trang-diem' ? 'is-active' : ''}">Trang điểm</a>
            <a href="${pageContext.request.contextPath}/blog?topic=voucher" data-topic="voucher"
               class="mc-news-chip ${viewTopic eq 'voucher' ? 'is-active' : ''}">Voucher</a>
            <a href="${pageContext.request.contextPath}/blog?topic=thanh-vien" data-topic="thanh-vien"
               class="mc-news-chip ${viewTopic eq 'thanh-vien' ? 'is-active' : ''}">Thành viên</a>
            <a href="${pageContext.request.contextPath}/blog?topic=cam-nang" data-topic="cam-nang"
               class="mc-news-chip ${viewTopic eq 'cam-nang' ? 'is-active' : ''}">Cẩm nang</a>
        </section>

        <c:if test="${usingFallback}">
            <div class="mc-news-alert">
                <strong>Đang hiển thị nội dung gợi ý của MyCosmetic.</strong>
                <span>Khi admin thêm bài viết thật trong hệ thống quản trị, trang này sẽ ưu tiên hiển thị dữ liệu từ database.</span>
            </div>
        </c:if>

        <c:if test="${viewTopic eq 'all'}">
            <section class="mc-news-featured">
                <article class="mc-news-featured-card mc-news-featured-card--primary">
                    <span class="mc-news-badge">Sự kiện</span>
                    <h2>Ngày hội thành viên MyCosmetic</h2>
                    <p>Tích điểm, nhận voucher và mở khóa ưu đãi dành riêng cho khách hàng thân thiết.</p>
                    <a href="${pageContext.request.contextPath}/blog?topic=thanh-vien">Xem mục thành viên</a>
                </article>

                <article class="mc-news-featured-card mc-news-featured-card--secondary">
                    <span class="mc-news-badge">Cẩm nang</span>
                    <h2>Chăm sóc da đúng bước, mua sắm đúng nhu cầu</h2>
                    <p>Gợi ý cách chọn sản phẩm theo tình trạng da, ngân sách và mục tiêu chăm sóc.</p>
                    <a href="${pageContext.request.contextPath}/blog?topic=cam-nang">Xem cẩm nang</a>
                </article>
            </section>
        </c:if>

        <div class="mc-news-section-head">
            <div>
                <span class="mc-news-section-label">Nội dung đang hiển thị</span>
                <h2 id="mcNewsTopicTitle"><c:out value="${viewTopicLabel}" /></h2>
            </div>
            <div class="mc-news-section-count"><span id="mcNewsCountInline"><c:out value="${not empty eventCount ? eventCount : 0}" /></span> mục</div>
        </div>

        <div class="mc-news-grid" id="mcNewsGrid">
            <c:choose>
                <c:when test="${not empty events}">
                    <c:forEach var="event" items="${events}">
                        <article class="mc-news-card" data-topic="${event.topicKey}" data-search="${fn:toLowerCase(event.title)} ${fn:toLowerCase(event.summary)} ${fn:toLowerCase(event.tag)}">
                            <div class="mc-news-card__media mc-topic-${event.topicKey}">
                                <c:choose>
                                    <c:when test="${not empty event.imageUrl}">
                                        <img src="${pageContext.request.contextPath}${event.imageUrl}" alt="${event.title}">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="mc-news-card__placeholder">
                                            <span class="mc-news-card__placeholder-tag"><c:out value="${event.tag}"/></span>
                                        </div>
                                    </c:otherwise>
                                </c:choose>

                                <div class="mc-news-card__date">
                                    <span class="day"><fmt:formatDate value="${event.eventDate}" pattern="dd"/></span>
                                    <span class="month">Th<fmt:formatDate value="${event.eventDate}" pattern="MM"/></span>
                                </div>
                            </div>

                            <div class="mc-news-card__body">
                                <span class="mc-news-card__tag"><c:out value="${event.tag}"/></span>
                                <h3><c:out value="${event.title}"/></h3>
                                <p><c:out value="${event.summary}"/></p>

                                <c:choose>
                                    <c:when test="${event.id gt 0}">
                                        <a href="${pageContext.request.contextPath}/blog/detail?id=${event.id}" class="mc-news-card__action">Xem chi tiết</a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="${pageContext.request.contextPath}${event.actionUrl}" class="mc-news-card__action"><c:out value="${event.actionText}"/></a>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </article>
                    </c:forEach>
                </c:when>

                <c:otherwise>
                    <%-- Fallback trực tiếp trong JSP để vẫn có nội dung đúng từng mục kể cả khi servlet chưa rebuild --%>
                    <article class="mc-news-card" data-topic="flash-sale" data-search="flash sale deal son môi má hồng cuối tuần mỹ phẩm giảm giá săn deal">
                        <div class="mc-news-card__media mc-topic-flash-sale"><div class="mc-news-card__placeholder"><span class="mc-news-card__placeholder-tag">Flash sale</span></div><div class="mc-news-card__date"><span class="day">15</span><span class="month">Th06</span></div></div>
                        <div class="mc-news-card__body"><span class="mc-news-card__tag">Flash sale</span><h3>Lịch săn deal hồng cuối tuần tại MyCosmetic</h3><p>Cập nhật khung giờ flash sale, sản phẩm đang giảm sâu và cách kết hợp voucher để mua mỹ phẩm tiết kiệm hơn.</p><a href="${pageContext.request.contextPath}/vouchers" class="mc-news-card__action">Xem ưu đãi</a></div>
                    </article>

                    <article class="mc-news-card" data-topic="flash-sale" data-search="deal bán chạy son môi má hồng sản phẩm giảm sâu">
                        <div class="mc-news-card__media mc-topic-flash-sale"><div class="mc-news-card__placeholder"><span class="mc-news-card__placeholder-tag">Deal hot</span></div><div class="mc-news-card__date"><span class="day">16</span><span class="month">Th06</span></div></div>
                        <div class="mc-news-card__body"><span class="mc-news-card__tag">Flash sale</span><h3>Deal son môi và má hồng bán chạy trong tuần</h3><p>Tổng hợp các sản phẩm trang điểm có lượt mua cao, giá tốt và phù hợp để dùng hằng ngày.</p><a href="${pageContext.request.contextPath}/products?category=all" class="mc-news-card__action">Săn deal ngay</a></div>
                    </article>

                    <article class="mc-news-card" data-topic="cham-soc-da" data-search="chăm sóc da da dầu sữa rửa mặt toner serum kem chống nắng">
                        <div class="mc-news-card__media mc-topic-cham-soc-da"><div class="mc-news-card__placeholder"><span class="mc-news-card__placeholder-tag">Skin care</span></div><div class="mc-news-card__date"><span class="day">18</span><span class="month">Th06</span></div></div>
                        <div class="mc-news-card__body"><span class="mc-news-card__tag">Chăm sóc da</span><h3>Quy trình chăm sóc da buổi sáng cho da dầu</h3><p>Gợi ý thứ tự dùng sữa rửa mặt, toner, serum, kem dưỡng và chống nắng để da thông thoáng hơn.</p><a href="${pageContext.request.contextPath}/products?category=all" class="mc-news-card__action">Xem sản phẩm</a></div>
                    </article>

                    <article class="mc-news-card" data-topic="cham-soc-da" data-search="chống nắng kem chống nắng xịt chống nắng mùa nắng nóng bảo vệ da">
                        <div class="mc-news-card__media mc-topic-cham-soc-da"><div class="mc-news-card__placeholder"><span class="mc-news-card__placeholder-tag">Chống nắng</span></div><div class="mc-news-card__date"><span class="day">19</span><span class="month">Th06</span></div></div>
                        <div class="mc-news-card__body"><span class="mc-news-card__tag">Chăm sóc da</span><h3>Top sản phẩm chống nắng nên có trong mùa nắng nóng</h3><p>Danh sách kem chống nắng, xịt chống nắng và mẹo bôi lại để bảo vệ da khi đi học, đi làm hoặc đi chơi.</p><a href="${pageContext.request.contextPath}/products?category=all" class="mc-news-card__action">Xem chống nắng</a></div>
                    </article>

                    <article class="mc-news-card" data-topic="trang-diem" data-search="trang điểm makeup má hồng son tint mùa hè layout trong trẻo">
                        <div class="mc-news-card__media mc-topic-trang-diem"><div class="mc-news-card__placeholder"><span class="mc-news-card__placeholder-tag">Makeup</span></div><div class="mc-news-card__date"><span class="day">20</span><span class="month">Th06</span></div></div>
                        <div class="mc-news-card__body"><span class="mc-news-card__tag">Trang điểm</span><h3>Xu hướng má hồng và son tint nhẹ nhàng mùa hè</h3><p>Gợi ý layout makeup trong trẻo với son tint, má hồng và lớp nền mỏng nhẹ hợp phong cách hằng ngày.</p><a href="${pageContext.request.contextPath}/products?category=all" class="mc-news-card__action">Khám phá ngay</a></div>
                    </article>

                    <article class="mc-news-card" data-topic="trang-diem" data-search="phấn mắt người mới bắt đầu bảng màu trung tính nhũ sáng">
                        <div class="mc-news-card__media mc-topic-trang-diem"><div class="mc-news-card__placeholder"><span class="mc-news-card__placeholder-tag">Phấn mắt</span></div><div class="mc-news-card__date"><span class="day">21</span><span class="month">Th06</span></div></div>
                        <div class="mc-news-card__body"><span class="mc-news-card__tag">Trang điểm</span><h3>Cách chọn phấn mắt dễ dùng cho người mới bắt đầu</h3><p>Ưu tiên bảng màu trung tính, chất phấn dễ tán và phối cùng nhũ sáng để đôi mắt nổi bật hơn.</p><a href="${pageContext.request.contextPath}/products?category=all" class="mc-news-card__action">Xem trang điểm</a></div>
                    </article>

                    <article class="mc-news-card" data-topic="voucher" data-search="voucher mã giảm giá điều kiện thanh toán áp mã đơn tối thiểu">
                        <div class="mc-news-card__media mc-topic-voucher"><div class="mc-news-card__placeholder"><span class="mc-news-card__placeholder-tag">Voucher</span></div><div class="mc-news-card__date"><span class="day">22</span><span class="month">Th06</span></div></div>
                        <div class="mc-news-card__body"><span class="mc-news-card__tag">Voucher</span><h3>Cách chọn mã giảm giá phù hợp trước khi thanh toán</h3><p>Kiểm tra điều kiện đơn tối thiểu, hạn sử dụng, hạng thành viên và giá trị giảm tối đa trước khi áp mã.</p><a href="${pageContext.request.contextPath}/vouchers" class="mc-news-card__action">Xem voucher</a></div>
                    </article>

                    <article class="mc-news-card" data-topic="voucher" data-search="voucher cuối tháng khách hàng mới freeship giảm đơn đầu">
                        <div class="mc-news-card__media mc-topic-voucher"><div class="mc-news-card__placeholder"><span class="mc-news-card__placeholder-tag">Ưu đãi</span></div><div class="mc-news-card__date"><span class="day">23</span><span class="month">Th06</span></div></div>
                        <div class="mc-news-card__body"><span class="mc-news-card__tag">Voucher</span><h3>Ngày hội voucher cuối tháng cho khách hàng mới</h3><p>Tổng hợp mã freeship, mã giảm đơn đầu và các lưu ý để không bỏ lỡ ưu đãi hấp dẫn.</p><a href="${pageContext.request.contextPath}/vouchers" class="mc-news-card__action">Lấy mã ngay</a></div>
                    </article>

                    <article class="mc-news-card" data-topic="thanh-vien" data-search="thành viên tích điểm nâng hạng voucher khách hàng thân thiết">
                        <div class="mc-news-card__media mc-topic-thanh-vien"><div class="mc-news-card__placeholder"><span class="mc-news-card__placeholder-tag">Member</span></div><div class="mc-news-card__date"><span class="day">24</span><span class="month">Th06</span></div></div>
                        <div class="mc-news-card__body"><span class="mc-news-card__tag">Thành viên</span><h3>Ưu đãi hạng thành viên: mua nhiều, nhận nhiều</h3><p>Tìm hiểu quyền lợi tích điểm, giảm giá theo hạng và voucher riêng cho khách hàng thân thiết.</p><a href="${pageContext.request.contextPath}/account" class="mc-news-card__action">Xem tài khoản</a></div>
                    </article>

                    <article class="mc-news-card" data-topic="thanh-vien" data-search="nâng hạng thành viên tích điểm lịch sử mua sắm ưu đãi vip">
                        <div class="mc-news-card__media mc-topic-thanh-vien"><div class="mc-news-card__placeholder"><span class="mc-news-card__placeholder-tag">Tích điểm</span></div><div class="mc-news-card__date"><span class="day">25</span><span class="month">Th06</span></div></div>
                        <div class="mc-news-card__body"><span class="mc-news-card__tag">Thành viên</span><h3>Cách nâng hạng thành viên nhanh hơn tại MyCosmetic</h3><p>Theo dõi giá trị đơn hàng, lịch sử mua sắm và các chương trình tích điểm để mở khóa ưu đãi tốt hơn.</p><a href="${pageContext.request.contextPath}/account" class="mc-news-card__action">Xem hạng thành viên</a></div>
                    </article>

                    <article class="mc-news-card" data-topic="cam-nang" data-search="cẩm nang checklist phục hồi da sau makeup làm sạch cấp ẩm cân bằng da">
                        <div class="mc-news-card__media mc-topic-cam-nang"><div class="mc-news-card__placeholder"><span class="mc-news-card__placeholder-tag">Cẩm nang</span></div><div class="mc-news-card__date"><span class="day">26</span><span class="month">Th06</span></div></div>
                        <div class="mc-news-card__body"><span class="mc-news-card__tag">Cẩm nang</span><h3>Checklist phục hồi da sau khi makeup nhiều ngày</h3><p>Làm sạch đúng cách, cấp ẩm vừa đủ và chọn sản phẩm phục hồi để da cân bằng lại nhanh hơn.</p><a href="${pageContext.request.contextPath}/products?category=all" class="mc-news-card__action">Xem gợi ý</a></div>
                    </article>

                    <article class="mc-news-card" data-topic="cam-nang" data-search="cẩm nang mua mỹ phẩm online an toàn kiểm tra thương hiệu đánh giá đổi trả">
                        <div class="mc-news-card__media mc-topic-cam-nang"><div class="mc-news-card__placeholder"><span class="mc-news-card__placeholder-tag">Hướng dẫn</span></div><div class="mc-news-card__date"><span class="day">28</span><span class="month">Th06</span></div></div>
                        <div class="mc-news-card__body"><span class="mc-news-card__tag">Cẩm nang</span><h3>5 bước mua mỹ phẩm online an toàn hơn</h3><p>Kiểm tra thương hiệu, ảnh sản phẩm, mô tả, đánh giá và chính sách đổi trả trước khi đặt hàng.</p><a href="${pageContext.request.contextPath}/products?category=all" class="mc-news-card__action">Đọc cẩm nang</a></div>
                    </article>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="mc-news-empty" id="mcNewsEmpty" hidden>
            <h3>Không tìm thấy nội dung phù hợp</h3>
            <p>Hãy thử đổi từ khóa tìm kiếm hoặc chọn lại mục nội dung khác như Voucher, Chăm sóc da, Trang điểm hoặc Thành viên.</p>
            <a href="${pageContext.request.contextPath}/blog">Quay lại tất cả nội dung</a>
        </div>

        <section class="mc-news-bottom-strip">
            <div class="mc-news-bottom-strip__item">
                <span class="icon">📅</span>
                <div>
                    <h3>Lịch cập nhật nội dung</h3>
                    <p>MyCosmetic ưu tiên cập nhật deal, bài tư vấn và thông tin thành viên vào mỗi tuần.</p>
                </div>
            </div>
            <div class="mc-news-bottom-strip__item">
                <span class="icon">❔</span>
                <div>
                    <h3>Cần tư vấn nhanh?</h3>
                    <p>Liên hệ đội ngũ hỗ trợ để được gợi ý sản phẩm phù hợp với nhu cầu của bạn.</p>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/lien-he" class="mc-news-bottom-strip__cta">Liên hệ ngay</a>
        </section>

    </div>
</section>

<script>
    (function () {
        const root = document.querySelector('.mc-news-page');
        if (!root) return;

        const params = new URLSearchParams(window.location.search);
        const topic = params.get('topic') || root.dataset.activeTopic || 'all';
        const search = (params.get('search') || root.dataset.search || '').trim().toLowerCase();
        const labels = {
            'all': 'Tất cả',
            'flash-sale': 'Flash sale',
            'cham-soc-da': 'Chăm sóc da',
            'trang-diem': 'Trang điểm',
            'voucher': 'Voucher',
            'thanh-vien': 'Thành viên',
            'cam-nang': 'Cẩm nang'
        };

        document.querySelectorAll('.mc-news-chip').forEach(chip => {
            chip.classList.toggle('is-active', (chip.dataset.topic || 'all') === topic);
        });

        const topicInput = document.getElementById('mcNewsTopicInput');
        if (topicInput) topicInput.value = topic;

        let visibleCount = 0;
        document.querySelectorAll('.mc-news-card[data-topic]').forEach(card => {
            const cardTopic = card.dataset.topic || 'all';
            const text = (card.dataset.search || card.textContent || '').toLowerCase();
            const matchTopic = topic === 'all' || cardTopic === topic;
            const matchSearch = !search || text.includes(search);
            const visible = matchTopic && matchSearch;

            card.hidden = !visible;
            card.classList.toggle('is-hidden', !visible);
            if (visible) visibleCount++;
        });

        const title = document.getElementById('mcNewsTopicTitle');
        if (title) title.textContent = labels[topic] || 'Tất cả';

        const countInline = document.getElementById('mcNewsCountInline');
        const countTop = document.getElementById('mcNewsCountTop');
        if (countInline) countInline.textContent = visibleCount;
        if (countTop) countTop.textContent = visibleCount;

        const topTopic = document.getElementById('mcNewsTopicTop');
        if (topTopic) topTopic.textContent = labels[topic] || 'Tất cả';

        const empty = document.getElementById('mcNewsEmpty');
        if (empty) empty.hidden = visibleCount > 0;
    })();
</script>

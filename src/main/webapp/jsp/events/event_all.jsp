<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.webshop.app.model.Event" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.Normalizer" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%!
    private static final String TOPIC_ALL = "all";
    private static final String TOPIC_FLASH_SALE = "flash-sale";
    private static final String TOPIC_SKINCARE = "cham-soc-da";
    private static final String TOPIC_MAKEUP = "trang-diem";
    private static final String TOPIC_VOUCHER = "voucher";
    private static final String TOPIC_MEMBER = "thanh-vien";
    private static final String TOPIC_GUIDE = "cam-nang";

    private LinkedHashMap<String, String> topicLabels() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(TOPIC_ALL, "Tất cả");
        map.put(TOPIC_FLASH_SALE, "Flash sale");
        map.put(TOPIC_SKINCARE, "Chăm sóc da");
        map.put(TOPIC_MAKEUP, "Trang điểm");
        map.put(TOPIC_VOUCHER, "Voucher");
        map.put(TOPIC_MEMBER, "Thành viên");
        map.put(TOPIC_GUIDE, "Cẩm nang");
        return map;
    }

    private LinkedHashMap<String, String> topicDescriptions() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(TOPIC_ALL, "Tổng hợp tin tức, sự kiện, ưu đãi, voucher và cẩm nang làm đẹp mới nhất từ MyCosmetic.");
        map.put(TOPIC_FLASH_SALE, "Trang riêng cho lịch săn deal, khung giờ giảm sâu và sản phẩm đang có ưu đãi nổi bật.");
        map.put(TOPIC_SKINCARE, "Trang riêng cho routine chăm sóc da, cách chọn sản phẩm và mẹo chăm da đúng bước.");
        map.put(TOPIC_MAKEUP, "Trang riêng cho xu hướng trang điểm, son môi, má hồng, phấn mắt và layout makeup dễ áp dụng.");
        map.put(TOPIC_VOUCHER, "Trang riêng cho mã giảm giá, điều kiện áp mã, cách lưu voucher và tối ưu tổng tiền thanh toán.");
        map.put(TOPIC_MEMBER, "Trang riêng cho quyền lợi thành viên, tích điểm, nâng hạng và ưu đãi khách hàng thân thiết.");
        map.put(TOPIC_GUIDE, "Trang riêng cho cẩm nang mua mỹ phẩm an toàn, chọn sản phẩm đúng nhu cầu và dùng mỹ phẩm đúng cách.");
        return map;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        return normalized.replace('đ', 'd');
    }

    private String extractTopic(HttpServletRequest request) {
        LinkedHashMap<String, String> labels = topicLabels();
        String queryTopic = safe(request.getParameter("topic"));
        if (!queryTopic.isEmpty() && labels.containsKey(queryTopic)) {
            return queryTopic;
        }

        String uri = request.getRequestURI();
        String marker = "/blog/";
        int index = uri.indexOf(marker);
        if (index >= 0) {
            String tail = uri.substring(index + marker.length());
            int slash = tail.indexOf('/');
            if (slash >= 0) {
                tail = tail.substring(0, slash);
            }
            if (labels.containsKey(tail)) {
                return tail;
            }
        }

        return TOPIC_ALL;
    }

    private List<Event> fallbackEvents() {
        List<Event> events = new ArrayList<>();

        add(events, "Lịch săn deal hồng cuối tuần tại MyCosmetic",
                "Cập nhật khung giờ flash sale, sản phẩm đang giảm sâu và cách kết hợp voucher để mua mỹ phẩm tiết kiệm hơn.",
                "Flash sale", TOPIC_FLASH_SALE, "lich-san-deal-hong-cuoi-tuan", 15, "/vouchers", "Xem ưu đãi",
                "Flash sale cuối tuần là thời điểm phù hợp để mua các sản phẩm đang có nhu cầu dùng thường xuyên như son môi, má hồng, sữa rửa mặt và kem chống nắng.");

        add(events, "Deal son môi và má hồng bán chạy trong tuần",
                "Tổng hợp các sản phẩm trang điểm có lượt mua cao, giá tốt và phù hợp để dùng hằng ngày.",
                "Deal hot", TOPIC_FLASH_SALE, "deal-son-moi-va-ma-hong-ban-chay", 16, "/products?category=all", "Săn deal ngay",
                "Son môi và má hồng là hai nhóm sản phẩm dễ mua trong các đợt deal vì có nhiều tone màu, nhiều mức giá và phù hợp nhiều phong cách trang điểm.");

        add(events, "Top khung giờ giảm sâu nên canh mua",
                "Gợi ý cách theo dõi khung giờ ưu đãi để không bỏ lỡ sản phẩm có giá tốt.",
                "Săn sale", TOPIC_FLASH_SALE, "top-khung-gio-giam-sau-nen-canh-mua", 17, "/vouchers", "Xem lịch sale",
                "Các khung giờ giảm sâu thường có số lượng sản phẩm giới hạn, vì vậy bạn nên thêm sản phẩm vào giỏ trước và kiểm tra lại voucher sẵn có.");

        add(events, "Quy trình chăm sóc da buổi sáng cho da dầu",
                "Gợi ý thứ tự dùng sữa rửa mặt, toner, serum, kem dưỡng và chống nắng để da thông thoáng hơn.",
                "Chăm sóc da", TOPIC_SKINCARE, "quy-trinh-cham-soc-da-buoi-sang-cho-da-dau", 18, "/products?category=all", "Xem sản phẩm",
                "Với da dầu, routine buổi sáng nên tập trung vào làm sạch nhẹ, cân bằng da, dưỡng ẩm vừa đủ và bảo vệ da bằng kem chống nắng.");

        add(events, "Top sản phẩm chống nắng nên có trong mùa nắng nóng",
                "Danh sách kem chống nắng, xịt chống nắng và mẹo bôi lại để bảo vệ da khi đi học, đi làm hoặc đi chơi.",
                "Chống nắng", TOPIC_SKINCARE, "top-san-pham-chong-nang-nen-co", 19, "/products?category=all", "Xem chống nắng",
                "Kem chống nắng là bước quan trọng trong routine ban ngày, đặc biệt khi bạn thường xuyên ra ngoài hoặc ngồi gần cửa sổ.");

        add(events, "Checklist phục hồi da sau khi makeup nhiều ngày",
                "Làm sạch đúng cách, cấp ẩm vừa đủ và chọn sản phẩm phục hồi để da cân bằng lại nhanh hơn.",
                "Phục hồi da", TOPIC_SKINCARE, "checklist-phuc-hoi-da-sau-khi-makeup-nhieu-ngay", 26, "/products?category=all", "Xem gợi ý",
                "Sau nhiều ngày trang điểm, da có thể bị khô, bí hoặc dễ nổi mụn nếu làm sạch chưa kỹ và dưỡng phục hồi chưa đủ.");

        add(events, "Xu hướng má hồng và son tint nhẹ nhàng mùa hè",
                "Gợi ý layout makeup trong trẻo với son tint, má hồng và lớp nền mỏng nhẹ hợp phong cách hằng ngày.",
                "Trang điểm", TOPIC_MAKEUP, "xu-huong-ma-hong-va-son-tint-nhe-nhang", 20, "/products?category=all", "Khám phá ngay",
                "Xu hướng trang điểm mùa hè ưu tiên lớp nền mỏng, má hồng tự nhiên và son tint có độ bám ổn để gương mặt tươi tắn hơn.");

        add(events, "Cách chọn phấn mắt dễ dùng cho người mới bắt đầu",
                "Ưu tiên bảng màu trung tính, chất phấn dễ tán và phối cùng nhũ sáng để đôi mắt nổi bật hơn.",
                "Phấn mắt", TOPIC_MAKEUP, "cach-chon-phan-mat-de-dung-cho-nguoi-moi", 21, "/products?category=all", "Xem trang điểm",
                "Người mới bắt đầu nên chọn bảng phấn mắt có màu nền trung tính, màu chuyển tiếp dễ tán và một đến hai màu nhũ sáng.");

        add(events, "Layout trang điểm đi học nhẹ nhàng, tươi tắn",
                "Gợi ý cách phối nền mỏng, má hồng nhẹ và son MLBB để gương mặt rạng rỡ mà vẫn tự nhiên.",
                "Makeup", TOPIC_MAKEUP, "layout-trang-diem-di-hoc-nhe-nhang", 22, "/products?category=all", "Xem layout",
                "Layout đi học nên ưu tiên sự tự nhiên, nhanh gọn và không quá dày nền.");

        add(events, "Cách chọn mã giảm giá phù hợp trước khi thanh toán",
                "Kiểm tra điều kiện đơn tối thiểu, hạn sử dụng, hạng thành viên và giá trị giảm tối đa trước khi áp mã.",
                "Voucher", TOPIC_VOUCHER, "cach-chon-ma-giam-gia-phu-hop-truoc-khi-thanh-toan", 22, "/vouchers", "Xem voucher",
                "Trước khi áp voucher, bạn cần kiểm tra bốn yếu tố chính: giá trị đơn tối thiểu, thời hạn sử dụng, nhóm sản phẩm áp dụng và mức giảm tối đa.");

        add(events, "Ngày hội voucher cuối tháng cho khách hàng mới",
                "Tổng hợp mã freeship, mã giảm đơn đầu và các lưu ý để không bỏ lỡ ưu đãi hấp dẫn.",
                "Ưu đãi", TOPIC_VOUCHER, "ngay-hoi-voucher-cuoi-thang-cho-khach-hang-moi", 23, "/vouchers", "Lấy mã ngay",
                "Khách hàng mới thường có nhiều mã ưu đãi như giảm đơn đầu, freeship hoặc voucher theo nhóm sản phẩm.");

        add(events, "Cách kết hợp mã freeship và mã giảm đơn hiệu quả",
                "Hướng dẫn chọn voucher vận chuyển và voucher giảm giá để tối ưu tổng tiền thanh toán.",
                "Voucher", TOPIC_VOUCHER, "cach-ket-hop-ma-freeship-va-ma-giam-don", 24, "/vouchers", "Xem mã phù hợp",
                "Để tối ưu tổng tiền, bạn nên kiểm tra xem hệ thống cho phép áp đồng thời mã freeship và mã giảm đơn hay không.");

        add(events, "Ngày hội thành viên MyCosmetic",
                "Tích điểm, nhận voucher và mở khóa ưu đãi dành riêng cho khách hàng thân thiết.",
                "Sự kiện", TOPIC_MEMBER, "ngay-hoi-thanh-vien-mycosmetic", 24, "/account", "Xem mục thành viên",
                "Ngày hội thành viên là dịp MyCosmetic dành ưu đãi riêng cho khách hàng đã mua sắm và tích điểm thường xuyên.");

        add(events, "Ưu đãi hạng thành viên: mua nhiều, nhận nhiều",
                "Tìm hiểu quyền lợi tích điểm, giảm giá theo hạng và voucher riêng cho khách hàng thân thiết.",
                "Thành viên", TOPIC_MEMBER, "uu-dai-hang-thanh-vien-mua-nhieu-nhan-nhieu", 25, "/account", "Xem tài khoản",
                "Hạng thành viên giúp khách hàng nhận nhiều quyền lợi hơn khi mua sắm thường xuyên tại MyCosmetic.");

        add(events, "Cách nâng hạng thành viên nhanh hơn tại MyCosmetic",
                "Theo dõi giá trị đơn hàng, lịch sử mua sắm và chương trình tích điểm để mở khóa ưu đãi tốt hơn.",
                "Tích điểm", TOPIC_MEMBER, "cach-nang-hang-thanh-vien-nhanh-hon", 26, "/account", "Xem hạng thành viên",
                "Để nâng hạng thành viên, bạn nên theo dõi tổng giá trị mua sắm, số đơn hoàn tất và các chương trình tích điểm đang diễn ra.");

        add(events, "Chăm sóc da đúng bước, mua sắm đúng nhu cầu",
                "Gợi ý cách chọn sản phẩm theo tình trạng da, ngân sách và mục tiêu chăm sóc để tránh mua dư hoặc dùng sai bước.",
                "Cẩm nang", TOPIC_GUIDE, "cham-soc-da-dung-buoc-mua-sam-dung-nhu-cau", 26, "/products?category=all", "Đọc cẩm nang",
                "Mua mỹ phẩm hiệu quả không chỉ là chọn sản phẩm nổi tiếng mà còn cần hiểu tình trạng da và mục tiêu chăm sóc của bản thân.");

        add(events, "5 bước mua mỹ phẩm online an toàn hơn",
                "Kiểm tra thương hiệu, ảnh sản phẩm, mô tả, đánh giá và chính sách đổi trả trước khi đặt hàng.",
                "Hướng dẫn", TOPIC_GUIDE, "5-buoc-mua-my-pham-online-an-toan-hon", 28, "/products?category=all", "Đọc cẩm nang",
                "Khi mua mỹ phẩm online, bạn nên kiểm tra thông tin thương hiệu, hình ảnh sản phẩm, dung tích, hạn sử dụng và mô tả chi tiết.");

        add(events, "Những lưu ý khi chọn mỹ phẩm cho người mới bắt đầu",
                "Ưu tiên sản phẩm cơ bản, dễ dùng, bảng thành phần rõ ràng và phù hợp với ngân sách.",
                "Cẩm nang", TOPIC_GUIDE, "nhung-luu-y-khi-chon-my-pham-cho-nguoi-moi", 29, "/products?category=all", "Xem hướng dẫn",
                "Người mới bắt đầu không cần mua quá nhiều sản phẩm cùng lúc. Hãy ưu tiên những món cơ bản, dễ dùng và phù hợp với nhu cầu hiện tại.");

        return events;
    }

    private void add(List<Event> events, String title, String summary, String tag, String topicKey,
                     String slug, int day, String actionUrl, String actionText, String content) {
        Event event = new Event();
        event.setTitle(title);
        event.setSummary(summary);
        event.setTag(tag);
        event.setTopicKey(topicKey);
        event.setSlug(slug);
        event.setEventDate(new Date(126, 5, day));
        event.setActionUrl(actionUrl);
        event.setActionText(actionText);
        event.setContent(content);
        events.add(event);
    }

    private boolean matchSearch(Event event, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }

        String key = normalizeText(keyword);
        String source = normalizeText(event.getTitle()) + " "
                + normalizeText(event.getSummary()) + " "
                + normalizeText(event.getTag()) + " "
                + normalizeText(event.getContent());

        return source.contains(key);
    }

    private Event findBySlug(List<Event> events, String slug) {
        for (Event event : events) {
            if (event.getSlug().equals(slug)) {
                return event;
            }
        }
        return null;
    }
%>

<%
    LinkedHashMap<String, String> labels = topicLabels();
    LinkedHashMap<String, String> descriptions = topicDescriptions();

    String activeTopic = extractTopic(request);
    String searchQuery = safe(request.getParameter("search"));

    Object eventAttr = request.getAttribute("events");
    List<Event> sourceEvents;

    if (eventAttr instanceof List && request.getAttribute("activeTopicLabel") != null) {
        sourceEvents = (List<Event>) eventAttr;
    } else {
        sourceEvents = fallbackEvents();
    }

    List<Event> visibleEvents = new ArrayList<>();
    for (Event item : sourceEvents) {
        boolean matchTopic = TOPIC_ALL.equals(activeTopic) || activeTopic.equals(item.getTopicKey());
        if (matchTopic && matchSearch(item, searchQuery)) {
            visibleEvents.add(item);
        }
    }

    Event featuredPrimary = null;
    Event featuredSecondary = null;

    if (TOPIC_ALL.equals(activeTopic)) {
        featuredPrimary = findBySlug(visibleEvents, "ngay-hoi-thanh-vien-mycosmetic");
        featuredSecondary = findBySlug(visibleEvents, "cham-soc-da-dung-buoc-mua-sam-dung-nhu-cau");
    }

    if (featuredPrimary == null && !visibleEvents.isEmpty()) {
        featuredPrimary = visibleEvents.get(0);
    }

    if (featuredSecondary == null) {
        featuredSecondary = visibleEvents.size() > 1 ? visibleEvents.get(1) : featuredPrimary;
    }

    String activeTopicUrl = TOPIC_ALL.equals(activeTopic) ? "/blog" : "/blog/" + activeTopic;

    request.setAttribute("events", visibleEvents);
    request.setAttribute("featuredPrimary", featuredPrimary);
    request.setAttribute("featuredSecondary", featuredSecondary);
    request.setAttribute("activeTopic", activeTopic);
    request.setAttribute("activeTopicLabel", labels.get(activeTopic));
    request.setAttribute("activeTopicDescription", descriptions.get(activeTopic));
    request.setAttribute("searchQuery", searchQuery);
    request.setAttribute("eventCount", visibleEvents.size());
    request.setAttribute("activeTopicUrl", activeTopicUrl);
%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/events-style.css?v=layout_topic_fix_v2">

<section class="mc-news-page">
    <div class="mc-news-shell">

        <section class="mc-news-hero mc-topic-${activeTopic}">
            <span class="mc-news-hero__shape mc-news-hero__shape--left"></span>
            <span class="mc-news-hero__shape mc-news-hero__shape--right"></span>
            <span class="mc-news-hero__glow mc-news-hero__glow--left"></span>
            <span class="mc-news-hero__glow mc-news-hero__glow--right"></span>

            <div class="mc-news-hero__inner">
                <span class="mc-news-panel-kicker">
                    <c:choose>
                        <c:when test="${activeTopic eq 'all'}">Danh mục</c:when>
                        <c:otherwise>Chuyên mục</c:otherwise>
                    </c:choose>
                </span>

                <h1>
                    <c:choose>
                        <c:when test="${activeTopic eq 'all'}">Tin tức, sự kiện và ưu đãi nổi bật</c:when>
                        <c:otherwise>${activeTopicLabel}</c:otherwise>
                    </c:choose>
                </h1>

                <p><c:out value="${activeTopicDescription}"/></p>

                <div class="mc-news-hero__dots" aria-hidden="true">
                    <span></span>
                    <span></span>
                    <span></span>
                </div>
            </div>
        </section>

        <section class="mc-news-toolbar">
            <div class="mc-news-toolbar__topics">
                <a href="${pageContext.request.contextPath}/blog"
                   class="mc-news-chip ${activeTopic eq 'all' ? 'is-active' : ''}">
                    Tất cả
                </a>

                <a href="${pageContext.request.contextPath}/blog/flash-sale"
                   class="mc-news-chip ${activeTopic eq 'flash-sale' ? 'is-active' : ''}">
                    Flash sale
                </a>

                <a href="${pageContext.request.contextPath}/blog/cham-soc-da"
                   class="mc-news-chip ${activeTopic eq 'cham-soc-da' ? 'is-active' : ''}">
                    Chăm sóc da
                </a>

                <a href="${pageContext.request.contextPath}/blog/trang-diem"
                   class="mc-news-chip ${activeTopic eq 'trang-diem' ? 'is-active' : ''}">
                    Trang điểm
                </a>

                <a href="${pageContext.request.contextPath}/blog/voucher"
                   class="mc-news-chip ${activeTopic eq 'voucher' ? 'is-active' : ''}">
                    Voucher
                </a>

                <a href="${pageContext.request.contextPath}/blog/thanh-vien"
                   class="mc-news-chip ${activeTopic eq 'thanh-vien' ? 'is-active' : ''}">
                    Thành viên
                </a>

                <a href="${pageContext.request.contextPath}/blog/cam-nang"
                   class="mc-news-chip ${activeTopic eq 'cam-nang' ? 'is-active' : ''}">
                    Cẩm nang
                </a>
            </div>

            <form action="${pageContext.request.contextPath}${activeTopicUrl}" method="get" class="mc-news-search mc-news-search--toolbar">
                <input type="text"
                       name="search"
                       value="<c:out value='${searchQuery}'/>"
                       placeholder="Tìm trong ${activeTopicLabel}...">
                <button type="submit">Tìm</button>
            </form>
        </section>

        <c:if test="${not empty featuredPrimary and not empty featuredSecondary}">
            <section class="mc-news-featured">
                <article class="mc-news-featured-card mc-topic-${featuredPrimary.topicKey}">
                    <span class="mc-news-badge"><c:out value="${featuredPrimary.tag}"/></span>
                    <h2><c:out value="${featuredPrimary.title}"/></h2>
                    <p><c:out value="${featuredPrimary.summary}"/></p>
                    <a href="${pageContext.request.contextPath}/blog/bai-viet/${featuredPrimary.slug}">
                        Đọc bài viết
                    </a>
                </article>

                <article class="mc-news-featured-card mc-topic-${featuredSecondary.topicKey}">
                    <span class="mc-news-badge"><c:out value="${featuredSecondary.tag}"/></span>
                    <h2><c:out value="${featuredSecondary.title}"/></h2>
                    <p><c:out value="${featuredSecondary.summary}"/></p>
                    <a href="${pageContext.request.contextPath}/blog/bai-viet/${featuredSecondary.slug}">
                        Đọc bài viết
                    </a>
                </article>
            </section>
        </c:if>

        <div class="mc-news-section-head">
            <div>
                <span class="mc-news-section-label">Nội dung đang hiển thị</span>
                <h2>${activeTopicLabel}</h2>
            </div>

            <div class="mc-news-section-count">${eventCount} mục</div>
        </div>

        <div class="mc-news-grid">
            <c:choose>
                <c:when test="${not empty events}">
                    <c:forEach var="event" items="${events}">
                        <article class="mc-news-card">
                            <div class="mc-news-card__media mc-topic-${event.topicKey}">
                                <div class="mc-news-card__placeholder">
                                    <span class="mc-news-card__placeholder-tag">
                                        <c:out value="${event.tag}"/>
                                    </span>
                                </div>

                                <div class="mc-news-card__date">
                                    <span class="day">
                                        <fmt:formatDate value="${event.eventDate}" pattern="dd"/>
                                    </span>
                                    <span class="month">
                                        Th<fmt:formatDate value="${event.eventDate}" pattern="MM"/>
                                    </span>
                                </div>
                            </div>

                            <div class="mc-news-card__body">
                                <span class="mc-news-card__tag">
                                    <c:out value="${event.tag}"/>
                                </span>

                                <h3><c:out value="${event.title}"/></h3>
                                <p><c:out value="${event.summary}"/></p>

                                <a href="${pageContext.request.contextPath}/blog/bai-viet/${event.slug}"
                                   class="mc-news-card__action">
                                    Đọc bài viết
                                </a>
                            </div>
                        </article>
                    </c:forEach>
                </c:when>

                <c:otherwise>
                    <div class="mc-news-empty">
                        <h3>Không tìm thấy nội dung phù hợp</h3>
                        <p>Không có bài viết khớp với từ khóa hiện tại. Hãy xóa từ khóa hoặc chọn lại chuyên mục khác.</p>
                        <a href="${pageContext.request.contextPath}${activeTopicUrl}">
                            Xóa tìm kiếm
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>
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
                <span class="icon">?</span>
                <div>
                    <h3>Cần tư vấn nhanh?</h3>
                    <p>Liên hệ đội ngũ hỗ trợ để được gợi ý sản phẩm phù hợp với nhu cầu của bạn.</p>
                </div>
            </div>

            <a href="${pageContext.request.contextPath}/lien-he"
               class="mc-news-bottom-strip__cta">
                Liên hệ ngay
            </a>
        </section>

    </div>
</section>

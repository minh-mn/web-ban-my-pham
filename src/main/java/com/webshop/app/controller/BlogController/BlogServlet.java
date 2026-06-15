package com.webshop.app.controller.BlogController;

import com.webshop.app.dao.EventDAO;
import com.webshop.app.model.Event;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

@WebServlet(urlPatterns = {"/blog", "/blog/detail"})
public class BlogServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final EventDAO eventDAO = new EventDAO();

    private static final String TOPIC_ALL = "all";
    private static final String TOPIC_FLASH_SALE = "flash-sale";
    private static final String TOPIC_SKINCARE = "cham-soc-da";
    private static final String TOPIC_MAKEUP = "trang-diem";
    private static final String TOPIC_VOUCHER = "voucher";
    private static final String TOPIC_MEMBER = "thanh-vien";
    private static final String TOPIC_GUIDE = "cam-nang";

    private static final LinkedHashMap<String, String> TOPIC_LABELS = new LinkedHashMap<>();

    static {
        TOPIC_LABELS.put(TOPIC_ALL, "Tất cả");
        TOPIC_LABELS.put(TOPIC_FLASH_SALE, "Flash sale");
        TOPIC_LABELS.put(TOPIC_SKINCARE, "Chăm sóc da");
        TOPIC_LABELS.put(TOPIC_MAKEUP, "Trang điểm");
        TOPIC_LABELS.put(TOPIC_VOUCHER, "Voucher");
        TOPIC_LABELS.put(TOPIC_MEMBER, "Thành viên");
        TOPIC_LABELS.put(TOPIC_GUIDE, "Cẩm nang");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/blog/detail".equals(path)) {
            handleEventDetail(req, resp);
            return;
        }

        handleEventList(req, resp);
    }

    private void handleEventList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String searchQuery = normalizeQuery(req.getParameter("search"));
        String activeTopic = normalizeTopic(req.getParameter("topic"));

        List<Event> dbEvents = loadDatabaseEvents(searchQuery, activeTopic);
        boolean usingFallback = dbEvents.isEmpty();

        List<Event> displayEvents = usingFallback
                ? buildFallbackEvents(searchQuery, activeTopic)
                : decorateEvents(dbEvents);

        req.setAttribute("events", displayEvents);
        req.setAttribute("usingFallback", usingFallback);
        req.setAttribute("searchQuery", searchQuery);
        req.setAttribute("activeTopic", activeTopic);
        req.setAttribute("activeTopicLabel", TOPIC_LABELS.get(activeTopic));
        req.setAttribute("eventCount", displayEvents.size());

        req.setAttribute("pageTitle", "MyCosmetic | Tin tức & Sự kiện");
        req.setAttribute("pageCss", "/assets/css/events-style.css");
        req.setAttribute("pageContent", "/jsp/events/event_all.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    private void handleEventDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String idParam = req.getParameter("id");
            if (idParam != null && !idParam.isBlank()) {
                int id = Integer.parseInt(idParam);
                Event event = eventDAO.getEventById(id);

                if (event != null) {
                    decorateEvent(event);
                    req.setAttribute("event", event);
                    req.setAttribute("pageTitle", event.getTitle());
                    req.setAttribute("pageCss", "/assets/css/events-style.css");
                    req.setAttribute("pageContent", "/jsp/events/event_detail.jsp");
                    req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
                    return;
                }
            }
        } catch (NumberFormatException ignored) {
        }

        resp.sendRedirect(req.getContextPath() + "/blog");
    }

    private List<Event> loadDatabaseEvents(String searchQuery, String activeTopic) {
        List<Event> source = (searchQuery == null || searchQuery.isBlank())
                ? eventDAO.getAllEvents()
                : eventDAO.searchEvents(searchQuery);

        List<Event> filtered = new ArrayList<>();
        for (Event event : source) {
            decorateEvent(event);
            if (matchesTopic(event, activeTopic) && matchesKeyword(event, searchQuery)) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    private List<Event> decorateEvents(List<Event> events) {
        for (Event event : events) {
            decorateEvent(event);
        }
        return events;
    }

    private void decorateEvent(Event event) {
        String topicKey = resolveTopicKey(event);
        event.setTopicKey(topicKey);

        if (event.getActionUrl() == null || event.getActionUrl().isBlank()) {
            event.setActionUrl(buildDefaultActionUrl(topicKey));
        }
        if (event.getActionText() == null || event.getActionText().isBlank()) {
            event.setActionText(buildDefaultActionText(topicKey, event.getId() > 0));
        }
        if (event.getEventDate() == null) {
            event.setEventDate(new Date());
        }
    }

    private List<Event> buildFallbackEvents(String searchQuery, String activeTopic) {
        List<Event> fallback = new ArrayList<>();

        fallback.add(createFallbackEvent(
                "Lịch săn deal hồng cuối tuần tại MyCosmetic",
                "Cập nhật khung giờ flash sale, sản phẩm đang giảm sâu và cách kết hợp voucher để mua mỹ phẩm tiết kiệm hơn.",
                "Flash sale", TOPIC_FLASH_SALE, daysAgo(1), "/vouchers", "Xem ưu đãi"
        ));
        fallback.add(createFallbackEvent(
                "Deal son môi và má hồng bán chạy trong tuần",
                "Tổng hợp các sản phẩm trang điểm có lượt mua cao, giá tốt và phù hợp để dùng hằng ngày.",
                "Flash sale", TOPIC_FLASH_SALE, daysAgo(2), "/products?category=all", "Săn deal ngay"
        ));
        fallback.add(createFallbackEvent(
                "Quy trình chăm sóc da buổi sáng cho da dầu",
                "Gợi ý thứ tự dùng sữa rửa mặt, toner, serum, kem dưỡng và chống nắng để da thông thoáng hơn.",
                "Chăm sóc da", TOPIC_SKINCARE, daysAgo(3), "/products?category=all", "Xem sản phẩm"
        ));
        fallback.add(createFallbackEvent(
                "Top sản phẩm chống nắng nên có trong mùa nắng nóng",
                "Danh sách kem chống nắng, xịt chống nắng và mẹo bôi lại để bảo vệ da khi đi học, đi làm hoặc đi chơi.",
                "Chăm sóc da", TOPIC_SKINCARE, daysAgo(4), "/products?category=all", "Xem chống nắng"
        ));
        fallback.add(createFallbackEvent(
                "Xu hướng má hồng và son tint nhẹ nhàng mùa hè",
                "Gợi ý layout makeup trong trẻo với son tint, má hồng và lớp nền mỏng nhẹ hợp phong cách hằng ngày.",
                "Trang điểm", TOPIC_MAKEUP, daysAgo(5), "/products?category=all", "Khám phá ngay"
        ));
        fallback.add(createFallbackEvent(
                "Cách chọn phấn mắt dễ dùng cho người mới bắt đầu",
                "Ưu tiên bảng màu trung tính, chất phấn dễ tán và phối cùng nhũ sáng để đôi mắt nổi bật hơn.",
                "Trang điểm", TOPIC_MAKEUP, daysAgo(6), "/products?category=all", "Xem trang điểm"
        ));
        fallback.add(createFallbackEvent(
                "Cách chọn mã giảm giá phù hợp trước khi thanh toán",
                "Kiểm tra điều kiện đơn tối thiểu, hạn sử dụng, hạng thành viên và giá trị giảm tối đa trước khi áp mã.",
                "Voucher", TOPIC_VOUCHER, daysAgo(7), "/vouchers", "Xem voucher"
        ));
        fallback.add(createFallbackEvent(
                "Ngày hội voucher cuối tháng cho khách hàng mới",
                "Tổng hợp mã freeship, mã giảm đơn đầu và các lưu ý để không bỏ lỡ ưu đãi hấp dẫn.",
                "Voucher", TOPIC_VOUCHER, daysAgo(8), "/vouchers", "Lấy mã ngay"
        ));
        fallback.add(createFallbackEvent(
                "Ưu đãi hạng thành viên: mua nhiều, nhận nhiều",
                "Tìm hiểu quyền lợi tích điểm, giảm giá theo hạng và voucher riêng cho khách hàng thân thiết.",
                "Thành viên", TOPIC_MEMBER, daysAgo(9), "/account", "Xem tài khoản"
        ));
        fallback.add(createFallbackEvent(
                "Cách nâng hạng thành viên nhanh hơn tại MyCosmetic",
                "Theo dõi giá trị đơn hàng, lịch sử mua sắm và các chương trình tích điểm để mở khóa ưu đãi tốt hơn.",
                "Thành viên", TOPIC_MEMBER, daysAgo(10), "/account", "Xem hạng thành viên"
        ));
        fallback.add(createFallbackEvent(
                "Checklist phục hồi da sau khi makeup nhiều ngày",
                "Làm sạch đúng cách, cấp ẩm vừa đủ và chọn sản phẩm phục hồi để da cân bằng lại nhanh hơn.",
                "Cẩm nang", TOPIC_GUIDE, daysAgo(11), "/products?category=all", "Xem gợi ý"
        ));
        fallback.add(createFallbackEvent(
                "5 bước mua mỹ phẩm online an toàn hơn",
                "Kiểm tra thương hiệu, ảnh sản phẩm, mô tả, đánh giá và chính sách đổi trả trước khi đặt hàng.",
                "Cẩm nang", TOPIC_GUIDE, daysAgo(12), "/products?category=all", "Đọc cẩm nang"
        ));

        List<Event> filtered = new ArrayList<>();
        for (Event event : fallback) {
            if (matchesTopic(event, activeTopic) && matchesKeyword(event, searchQuery)) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    private Event createFallbackEvent(String title,
                                      String summary,
                                      String tag,
                                      String topicKey,
                                      Date eventDate,
                                      String actionUrl,
                                      String actionText) {
        Event event = new Event();
        event.setTitle(title);
        event.setSummary(summary);
        event.setTag(tag);
        event.setTopicKey(topicKey);
        event.setEventDate(eventDate);
        event.setActionUrl(actionUrl);
        event.setActionText(actionText);
        return event;
    }

    private Date daysAgo(int days) {
        return new Date(System.currentTimeMillis() - days * 24L * 60L * 60L * 1000L);
    }

    private String normalizeTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            return TOPIC_ALL;
        }
        String normalized = topic.trim().toLowerCase(Locale.ROOT);
        return TOPIC_LABELS.containsKey(normalized) ? normalized : TOPIC_ALL;
    }

    private String normalizeQuery(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean matchesTopic(Event event, String activeTopic) {
        return TOPIC_ALL.equals(activeTopic) || activeTopic.equals(resolveTopicKey(event));
    }

    private boolean matchesKeyword(Event event, String searchQuery) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return true;
        }

        String haystack = normalizeText(event.getTitle()) + " "
                + normalizeText(event.getSummary()) + " "
                + normalizeText(event.getTag());
        return haystack.contains(normalizeText(searchQuery));
    }

    private String resolveTopicKey(Event event) {
        if (event.getTopicKey() != null && !event.getTopicKey().isBlank()) {
            return event.getTopicKey();
        }

        String content = normalizeText(event.getTag()) + " "
                + normalizeText(event.getTitle()) + " "
                + normalizeText(event.getSummary());

        if (containsAny(content, "voucher", "giam gia", "khuyen mai", "ma giam", "freeship")) {
            return TOPIC_VOUCHER;
        }
        if (containsAny(content, "thanh vien", "member", "tich diem", "vip", "hang")) {
            return TOPIC_MEMBER;
        }
        if (containsAny(content, "trang diem", "makeup", "son", "ma hong", "phan mat", "phan nuoc")) {
            return TOPIC_MAKEUP;
        }
        if (containsAny(content, "cham soc da", "skin care", "serum", "kem chong nang", "toner", "duong am", "sua rua mat")) {
            return TOPIC_SKINCARE;
        }
        if (containsAny(content, "cam nang", "huong dan", "checklist", "tu van", "bi kip", "online")) {
            return TOPIC_GUIDE;
        }
        if (containsAny(content, "flash", "sale", "deal", "uu dai", "san deal")) {
            return TOPIC_FLASH_SALE;
        }
        return TOPIC_GUIDE;
    }

    private String buildDefaultActionUrl(String topicKey) {
        switch (topicKey) {
            case TOPIC_VOUCHER:
            case TOPIC_FLASH_SALE:
                return "/vouchers";
            case TOPIC_MEMBER:
                return "/account";
            default:
                return "/products?category=all";
        }
    }

    private String buildDefaultActionText(String topicKey, boolean hasDetail) {
        if (hasDetail) {
            return "Xem chi tiết";
        }
        switch (topicKey) {
            case TOPIC_VOUCHER:
                return "Xem voucher";
            case TOPIC_MEMBER:
                return "Xem tài khoản";
            case TOPIC_FLASH_SALE:
                return "Xem ưu đãi";
            default:
                return "Khám phá ngay";
        }
    }

    private boolean containsAny(String source, String... keywords) {
        return Arrays.stream(keywords).anyMatch(source::contains);
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
}

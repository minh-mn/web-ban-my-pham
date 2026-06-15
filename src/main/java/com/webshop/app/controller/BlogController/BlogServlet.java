package com.webshop.app.controller.BlogController;

import com.webshop.app.model.Event;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

@WebServlet(urlPatterns = {"/blog", "/blog/*"})
public class BlogServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String TOPIC_ALL = "all";
    private static final String TOPIC_FLASH_SALE = "flash-sale";
    private static final String TOPIC_SKINCARE = "cham-soc-da";
    private static final String TOPIC_MAKEUP = "trang-diem";
    private static final String TOPIC_VOUCHER = "voucher";
    private static final String TOPIC_MEMBER = "thanh-vien";
    private static final String TOPIC_GUIDE = "cam-nang";

    private static final LinkedHashMap<String, String> TOPIC_LABELS = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> TOPIC_DESCRIPTIONS = new LinkedHashMap<>();

    static {
        TOPIC_LABELS.put(TOPIC_ALL, "Tất cả");
        TOPIC_LABELS.put(TOPIC_FLASH_SALE, "Flash sale");
        TOPIC_LABELS.put(TOPIC_SKINCARE, "Chăm sóc da");
        TOPIC_LABELS.put(TOPIC_MAKEUP, "Trang điểm");
        TOPIC_LABELS.put(TOPIC_VOUCHER, "Voucher");
        TOPIC_LABELS.put(TOPIC_MEMBER, "Thành viên");
        TOPIC_LABELS.put(TOPIC_GUIDE, "Cẩm nang");

        TOPIC_DESCRIPTIONS.put(TOPIC_ALL,
                "Tổng hợp tin tức, sự kiện, ưu đãi, voucher và cẩm nang làm đẹp mới nhất từ MyCosmetic.");
        TOPIC_DESCRIPTIONS.put(TOPIC_FLASH_SALE,
                "Trang riêng cho lịch săn deal, khung giờ giảm sâu và sản phẩm đang có ưu đãi nổi bật.");
        TOPIC_DESCRIPTIONS.put(TOPIC_SKINCARE,
                "Trang riêng cho routine chăm sóc da, cách chọn sản phẩm và mẹo chăm da đúng bước.");
        TOPIC_DESCRIPTIONS.put(TOPIC_MAKEUP,
                "Trang riêng cho xu hướng trang điểm, son môi, má hồng, phấn mắt và layout makeup dễ áp dụng.");
        TOPIC_DESCRIPTIONS.put(TOPIC_VOUCHER,
                "Trang riêng cho mã giảm giá, điều kiện áp mã, cách lưu voucher và tối ưu tổng tiền thanh toán.");
        TOPIC_DESCRIPTIONS.put(TOPIC_MEMBER,
                "Trang riêng cho quyền lợi thành viên, tích điểm, nâng hạng và ưu đãi khách hàng thân thiết.");
        TOPIC_DESCRIPTIONS.put(TOPIC_GUIDE,
                "Trang riêng cho cẩm nang mua mỹ phẩm an toàn, chọn sản phẩm đúng nhu cầu và dùng mỹ phẩm đúng cách.");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = safeTrim(req.getPathInfo());

        /*
         * Hỗ trợ cả link cũ và link mới:
         * /blog
         * /blog?topic=cam-nang
         * /blog/cam-nang
         * /blog?slug=...
         * /blog/bai-viet/{slug}
         */
        String oldSlug = safeTrim(req.getParameter("slug"));
        if (!oldSlug.isEmpty()) {
            showDetail(req, resp, oldSlug);
            return;
        }

        if (pathInfo.startsWith("/bai-viet/")) {
            String slug = pathInfo.substring("/bai-viet/".length()).trim();
            showDetail(req, resp, slug);
            return;
        }

        String topicFromPath = extractTopicFromPath(pathInfo);
        String topicFromQuery = safeTrim(req.getParameter("topic"));
        String activeTopic = normalizeTopic(!topicFromPath.isEmpty() ? topicFromPath : topicFromQuery);

        showTopic(req, resp, activeTopic);
    }

    private void showTopic(HttpServletRequest req, HttpServletResponse resp, String activeTopic)
            throws ServletException, IOException {

        String searchQuery = safeTrim(req.getParameter("search"));
        List<Event> events = findEvents(activeTopic, searchQuery);

        req.setAttribute("events", events);
        req.setAttribute("featuredPrimary", pickFeatured(events, activeTopic, 0));
        req.setAttribute("featuredSecondary", pickFeatured(events, activeTopic, 1));

        req.setAttribute("activeTopic", activeTopic);
        req.setAttribute("activeTopicLabel", TOPIC_LABELS.get(activeTopic));
        req.setAttribute("activeTopicDescription", TOPIC_DESCRIPTIONS.get(activeTopic));
        req.setAttribute("topicLabels", TOPIC_LABELS);
        req.setAttribute("searchQuery", searchQuery);
        req.setAttribute("eventCount", events.size());
        req.setAttribute("activeTopicUrl", getTopicUrl(activeTopic));

        req.setAttribute("pageTitle", TOPIC_ALL.equals(activeTopic)
                ? "MyCosmetic | Tin tức & Sự kiện"
                : "MyCosmetic | " + TOPIC_LABELS.get(activeTopic));
        req.setAttribute("pageCss", "/assets/css/events-style.css");
        req.setAttribute("pageContent", "/jsp/events/event_all.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp, String slug)
            throws ServletException, IOException {

        Event event = findBySlug(slug);

        if (event == null) {
            resp.sendRedirect(req.getContextPath() + "/blog");
            return;
        }

        req.setAttribute("event", event);
        req.setAttribute("relatedEvents", relatedEvents(event));
        req.setAttribute("topicLabels", TOPIC_LABELS);

        req.setAttribute("pageTitle", event.getTitle());
        req.setAttribute("pageCss", "/assets/css/events-style.css");
        req.setAttribute("pageContent", "/jsp/events/event_detail.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    private Event pickFeatured(List<Event> events, String activeTopic, int index) {
        if (events.isEmpty()) {
            return null;
        }

        if (!TOPIC_ALL.equals(activeTopic)) {
            return events.size() > index ? events.get(index) : events.get(0);
        }

        /*
         * Trang Tất cả giữ đúng bố cục ảnh người dùng gửi:
         * card lớn bên trái là Thành viên, card bên phải là Cẩm nang.
         */
        String targetSlug = index == 0
                ? "ngay-hoi-thanh-vien-mycosmetic"
                : "cham-soc-da-dung-buoc-mua-sam-dung-nhu-cau";

        for (Event event : events) {
            if (targetSlug.equals(event.getSlug())) {
                return event;
            }
        }

        return events.size() > index ? events.get(index) : events.get(0);
    }

    private List<Event> findEvents(String topic, String keyword) {
        List<Event> result = new ArrayList<>();

        for (Event event : buildEvents()) {
            boolean matchTopic = TOPIC_ALL.equals(topic) || topic.equals(event.getTopicKey());
            boolean matchKeyword = keyword.isBlank() || containsKeyword(event, keyword);

            if (matchTopic && matchKeyword) {
                result.add(event);
            }
        }

        return result;
    }

    private Event findBySlug(String slug) {
        for (Event event : buildEvents()) {
            if (event.getSlug().equals(slug)) {
                return event;
            }
        }

        return null;
    }

    private List<Event> relatedEvents(Event current) {
        List<Event> related = new ArrayList<>();

        for (Event event : buildEvents()) {
            if (event.getTopicKey().equals(current.getTopicKey())
                    && !event.getSlug().equals(current.getSlug())) {
                related.add(event);
            }

            if (related.size() >= 3) {
                break;
            }
        }

        return related;
    }

    private boolean containsKeyword(Event event, String keyword) {
        String searchText = normalizeText(keyword);
        String source = normalizeText(event.getTitle()) + " "
                + normalizeText(event.getSummary()) + " "
                + normalizeText(event.getTag()) + " "
                + normalizeText(event.getContent());

        return source.contains(searchText);
    }

    private String extractTopicFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return "";
        }

        String value = pathInfo.replaceFirst("^/", "");
        int slashIndex = value.indexOf('/');

        if (slashIndex >= 0) {
            value = value.substring(0, slashIndex);
        }

        return value.trim();
    }

    private String normalizeTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            return TOPIC_ALL;
        }

        String normalized = topic.trim().toLowerCase(Locale.ROOT);
        return TOPIC_LABELS.containsKey(normalized) ? normalized : TOPIC_ALL;
    }

    private String getTopicUrl(String topic) {
        return TOPIC_ALL.equals(topic) ? "/blog" : "/blog/" + topic;
    }

    private String safeTrim(String value) {
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

    private List<Event> buildEvents() {
        List<Event> events = new ArrayList<>();

        add(events, "Lịch săn deal hồng cuối tuần tại MyCosmetic",
                "Cập nhật khung giờ flash sale, sản phẩm đang giảm sâu và cách kết hợp voucher để mua mỹ phẩm tiết kiệm hơn.",
                "Flash sale", TOPIC_FLASH_SALE, "lich-san-deal-hong-cuoi-tuan",
                15, "/vouchers", "Xem ưu đãi",
                "Flash sale cuối tuần là thời điểm phù hợp để mua các sản phẩm đang có nhu cầu dùng thường xuyên như son môi, má hồng, sữa rửa mặt và kem chống nắng.\n\nĐể săn deal hiệu quả, bạn nên chuẩn bị trước danh sách sản phẩm cần mua, kiểm tra tồn kho, xem điều kiện voucher và đăng nhập tài khoản trước khi khung giờ sale bắt đầu.\n\nMyCosmetic khuyến khích khách hàng ưu tiên sản phẩm chính hãng, đọc kỹ mô tả, hạn sử dụng và chính sách đổi trả trước khi thanh toán.");

        add(events, "Deal son môi và má hồng bán chạy trong tuần",
                "Tổng hợp các sản phẩm trang điểm có lượt mua cao, giá tốt và phù hợp để dùng hằng ngày.",
                "Deal hot", TOPIC_FLASH_SALE, "deal-son-moi-va-ma-hong-ban-chay",
                16, "/products?category=all", "Săn deal ngay",
                "Son môi và má hồng là hai nhóm sản phẩm dễ mua trong các đợt deal vì có nhiều tone màu, nhiều mức giá và phù hợp nhiều phong cách trang điểm.\n\nBạn nên chọn sản phẩm dựa trên undertone da, độ bám màu, chất son hoặc chất phấn, đồng thời kiểm tra đánh giá của khách hàng trước đó.\n\nKhi mua theo combo, hãy ưu tiên các màu dễ dùng như hồng đất, cam đào, đỏ nâu hoặc hồng nude để sử dụng được trong nhiều hoàn cảnh.");

        add(events, "Top khung giờ giảm sâu nên canh mua",
                "Gợi ý cách theo dõi khung giờ ưu đãi để không bỏ lỡ sản phẩm có giá tốt.",
                "Săn sale", TOPIC_FLASH_SALE, "top-khung-gio-giam-sau-nen-canh-mua",
                17, "/vouchers", "Xem lịch sale",
                "Các khung giờ giảm sâu thường có số lượng sản phẩm giới hạn, vì vậy bạn nên thêm sản phẩm vào giỏ trước và kiểm tra lại voucher sẵn có.\n\nNếu sản phẩm có nhiều phân loại, hãy chọn sẵn màu hoặc dung tích để tránh mất thời gian khi thanh toán.\n\nSau khi đặt hàng, hãy theo dõi trạng thái đơn để kịp thời cập nhật thanh toán hoặc thông tin giao hàng nếu cần.");

        add(events, "Quy trình chăm sóc da buổi sáng cho da dầu",
                "Gợi ý thứ tự dùng sữa rửa mặt, toner, serum, kem dưỡng và chống nắng để da thông thoáng hơn.",
                "Chăm sóc da", TOPIC_SKINCARE, "quy-trinh-cham-soc-da-buoi-sang-cho-da-dau",
                18, "/products?category=all", "Xem sản phẩm",
                "Với da dầu, routine buổi sáng nên tập trung vào làm sạch nhẹ, cân bằng da, dưỡng ẩm vừa đủ và bảo vệ da bằng kem chống nắng.\n\nThứ tự gợi ý: sữa rửa mặt dịu nhẹ, toner hoặc lotion cấp ẩm, serum mỏng nhẹ, kem dưỡng dạng gel và kem chống nắng có kết cấu ráo.\n\nKhông nên dùng quá nhiều lớp đặc vào buổi sáng vì dễ gây bí da. Nếu da đang kích ứng, hãy ưu tiên phục hồi hàng rào bảo vệ da trước khi dùng treatment mạnh.");

        add(events, "Top sản phẩm chống nắng nên có trong mùa nắng nóng",
                "Danh sách kem chống nắng, xịt chống nắng và mẹo bôi lại để bảo vệ da khi đi học, đi làm hoặc đi chơi.",
                "Chống nắng", TOPIC_SKINCARE, "top-san-pham-chong-nang-nen-co",
                19, "/products?category=all", "Xem chống nắng",
                "Kem chống nắng là bước quan trọng trong routine ban ngày, đặc biệt khi bạn thường xuyên ra ngoài hoặc ngồi gần cửa sổ.\n\nBạn nên chọn sản phẩm có chỉ số bảo vệ phù hợp, kết cấu hợp loại da và khả năng dùng lại thuận tiện. Da dầu có thể chọn dạng gel hoặc fluid, da khô có thể ưu tiên dạng kem cấp ẩm.\n\nĐể hiệu quả tốt hơn, hãy bôi đủ lượng và bôi lại sau vài giờ nếu đổ mồ hôi hoặc tiếp xúc ánh nắng lâu.");

        add(events, "Checklist phục hồi da sau khi makeup nhiều ngày",
                "Làm sạch đúng cách, cấp ẩm vừa đủ và chọn sản phẩm phục hồi để da cân bằng lại nhanh hơn.",
                "Phục hồi da", TOPIC_SKINCARE, "checklist-phuc-hoi-da-sau-khi-makeup-nhieu-ngay",
                26, "/products?category=all", "Xem gợi ý",
                "Sau nhiều ngày trang điểm, da có thể bị khô, bí hoặc dễ nổi mụn nếu làm sạch chưa kỹ và dưỡng phục hồi chưa đủ.\n\nChecklist cơ bản gồm: tẩy trang kỹ, rửa mặt dịu nhẹ, dùng toner cấp ẩm, bổ sung serum phục hồi và khóa ẩm bằng kem dưỡng phù hợp.\n\nTrong giai đoạn da yếu, nên hạn chế tẩy da chết mạnh hoặc phối quá nhiều treatment cùng lúc.");

        add(events, "Xu hướng má hồng và son tint nhẹ nhàng mùa hè",
                "Gợi ý layout makeup trong trẻo với son tint, má hồng và lớp nền mỏng nhẹ hợp phong cách hằng ngày.",
                "Trang điểm", TOPIC_MAKEUP, "xu-huong-ma-hong-va-son-tint-nhe-nhang",
                20, "/products?category=all", "Khám phá ngay",
                "Xu hướng trang điểm mùa hè ưu tiên lớp nền mỏng, má hồng tự nhiên và son tint có độ bám ổn để gương mặt tươi tắn hơn.\n\nCác tone dễ dùng gồm hồng đào, cam sữa, đỏ đất nhạt và hồng nude. Bạn có thể dùng cùng một màu cho môi và má để tổng thể hài hòa.\n\nKhi chọn sản phẩm, hãy chú ý chất son, khả năng stain màu và độ dễ tán của má hồng để tránh bị loang.");

        add(events, "Cách chọn phấn mắt dễ dùng cho người mới bắt đầu",
                "Ưu tiên bảng màu trung tính, chất phấn dễ tán và phối cùng nhũ sáng để đôi mắt nổi bật hơn.",
                "Phấn mắt", TOPIC_MAKEUP, "cach-chon-phan-mat-de-dung-cho-nguoi-moi",
                21, "/products?category=all", "Xem trang điểm",
                "Người mới bắt đầu nên chọn bảng phấn mắt có màu nền trung tính, màu chuyển tiếp dễ tán và một đến hai màu nhũ sáng.\n\nKhông cần mua bảng quá nhiều ô màu nếu bạn chưa dùng thường xuyên. Một bảng nhỏ gồm nâu nhạt, hồng đất, cam be và nhũ champagne đã đủ cho nhiều layout hằng ngày.\n\nKhi đánh mắt, hãy bắt đầu với lượng phấn ít, tán từng lớp mỏng để màu lên tự nhiên hơn.");

        add(events, "Layout trang điểm đi học nhẹ nhàng, tươi tắn",
                "Gợi ý cách phối nền mỏng, má hồng nhẹ và son MLBB để gương mặt rạng rỡ mà vẫn tự nhiên.",
                "Makeup", TOPIC_MAKEUP, "layout-trang-diem-di-hoc-nhe-nhang",
                22, "/products?category=all", "Xem layout",
                "Layout đi học nên ưu tiên sự tự nhiên, nhanh gọn và không quá dày nền. Bạn có thể dùng cushion mỏng, che khuyết điểm nhẹ, má hồng kem và son MLBB.\n\nĐiểm quan trọng là giữ da sạch, lớp nền ráo và màu môi hài hòa với tổng thể. Nếu cần, có thể thêm mascara nhẹ để mắt có chiều sâu hơn.\n\nKhông nên dùng quá nhiều nhũ hoặc màu quá đậm trong môi trường học tập hằng ngày.");

        add(events, "Cách chọn mã giảm giá phù hợp trước khi thanh toán",
                "Kiểm tra điều kiện đơn tối thiểu, hạn sử dụng, hạng thành viên và giá trị giảm tối đa trước khi áp mã.",
                "Voucher", TOPIC_VOUCHER, "cach-chon-ma-giam-gia-phu-hop-truoc-khi-thanh-toan",
                22, "/vouchers", "Xem voucher",
                "Trước khi áp voucher, bạn cần kiểm tra bốn yếu tố chính: giá trị đơn tối thiểu, thời hạn sử dụng, nhóm sản phẩm áp dụng và mức giảm tối đa.\n\nMột số mã chỉ dành cho hạng thành viên nhất định hoặc chỉ áp dụng cho sản phẩm không nằm trong chương trình giảm giá khác.\n\nNếu có nhiều mã, hãy so sánh số tiền giảm thực tế sau khi áp để chọn mã có lợi nhất.");

        add(events, "Ngày hội voucher cuối tháng cho khách hàng mới",
                "Tổng hợp mã freeship, mã giảm đơn đầu và các lưu ý để không bỏ lỡ ưu đãi hấp dẫn.",
                "Ưu đãi", TOPIC_VOUCHER, "ngay-hoi-voucher-cuoi-thang-cho-khach-hang-moi",
                23, "/vouchers", "Lấy mã ngay",
                "Khách hàng mới thường có nhiều mã ưu đãi như giảm đơn đầu, freeship hoặc voucher theo nhóm sản phẩm.\n\nBạn nên đăng nhập tài khoản trước khi lưu mã, kiểm tra điều kiện và chọn sản phẩm đủ giá trị đơn tối thiểu.\n\nNếu mã không áp được, hãy xem lại hạn sử dụng, hạng thành viên hoặc sản phẩm có thuộc phạm vi áp dụng không.");

        add(events, "Cách kết hợp mã freeship và mã giảm đơn hiệu quả",
                "Hướng dẫn chọn voucher vận chuyển và voucher giảm giá để tối ưu tổng tiền thanh toán.",
                "Voucher", TOPIC_VOUCHER, "cach-ket-hop-ma-freeship-va-ma-giam-don",
                24, "/vouchers", "Xem mã phù hợp",
                "Để tối ưu tổng tiền, bạn nên kiểm tra xem hệ thống cho phép áp đồng thời mã freeship và mã giảm đơn hay không.\n\nNếu chỉ được chọn một mã, hãy so sánh giá trị giảm thực tế. Với đơn nhỏ, freeship có thể lợi hơn; với đơn lớn, mã giảm theo phần trăm thường hiệu quả hơn.\n\nLuôn kiểm tra tổng tiền cuối cùng trước khi xác nhận thanh toán.");

        add(events, "Ngày hội thành viên MyCosmetic",
                "Tích điểm, nhận voucher và mở khóa ưu đãi dành riêng cho khách hàng thân thiết.",
                "Sự kiện", TOPIC_MEMBER, "ngay-hoi-thanh-vien-mycosmetic",
                24, "/account", "Xem mục thành viên",
                "Ngày hội thành viên là dịp MyCosmetic dành ưu đãi riêng cho khách hàng đã mua sắm và tích điểm thường xuyên.\n\nTùy theo hạng thành viên, bạn có thể nhận voucher, ưu đãi giảm giá hoặc quyền lợi riêng trong các chương trình đặc biệt.\n\nHãy kiểm tra tài khoản để biết hạng hiện tại, điểm tích lũy và các mã ưu đãi đang có thể sử dụng.");

        add(events, "Ưu đãi hạng thành viên: mua nhiều, nhận nhiều",
                "Tìm hiểu quyền lợi tích điểm, giảm giá theo hạng và voucher riêng cho khách hàng thân thiết.",
                "Thành viên", TOPIC_MEMBER, "uu-dai-hang-thanh-vien-mua-nhieu-nhan-nhieu",
                25, "/account", "Xem tài khoản",
                "Hạng thành viên giúp khách hàng nhận nhiều quyền lợi hơn khi mua sắm thường xuyên tại MyCosmetic.\n\nCác quyền lợi có thể gồm tích điểm, voucher theo hạng, ưu đãi sinh nhật hoặc chương trình dành riêng cho khách hàng thân thiết.\n\nĐể nâng hạng nhanh hơn, hãy theo dõi đơn hàng, giữ lịch sử mua sắm ổn định và tận dụng các dịp tích điểm cao.");

        add(events, "Cách nâng hạng thành viên nhanh hơn tại MyCosmetic",
                "Theo dõi giá trị đơn hàng, lịch sử mua sắm và chương trình tích điểm để mở khóa ưu đãi tốt hơn.",
                "Tích điểm", TOPIC_MEMBER, "cach-nang-hang-thanh-vien-nhanh-hon",
                26, "/account", "Xem hạng thành viên",
                "Để nâng hạng thành viên, bạn nên theo dõi tổng giá trị mua sắm, số đơn hoàn tất và các chương trình tích điểm đang diễn ra.\n\nKhi mua sản phẩm cần dùng định kỳ, hãy đặt chung trong một đơn hợp lý để đạt ngưỡng ưu đãi hoặc freeship.\n\nĐừng quên lưu voucher theo hạng để dùng đúng thời điểm, tránh hết hạn.");

        add(events, "Chăm sóc da đúng bước, mua sắm đúng nhu cầu",
                "Gợi ý cách chọn sản phẩm theo tình trạng da, ngân sách và mục tiêu chăm sóc để tránh mua dư hoặc dùng sai bước.",
                "Cẩm nang", TOPIC_GUIDE, "cham-soc-da-dung-buoc-mua-sam-dung-nhu-cau",
                26, "/products?category=all", "Đọc cẩm nang",
                "Mua mỹ phẩm hiệu quả không chỉ là chọn sản phẩm nổi tiếng mà còn cần hiểu tình trạng da và mục tiêu chăm sóc của bản thân.\n\nBạn nên bắt đầu từ các bước cơ bản: làm sạch, dưỡng ẩm và chống nắng. Sau đó mới bổ sung serum hoặc treatment theo nhu cầu như mụn, thâm, khô da hoặc lão hóa.\n\nTrước khi mua, hãy đọc kỹ mô tả sản phẩm, thành phần chính, cách dùng và đánh giá từ người dùng có loại da tương tự.");

        add(events, "5 bước mua mỹ phẩm online an toàn hơn",
                "Kiểm tra thương hiệu, ảnh sản phẩm, mô tả, đánh giá và chính sách đổi trả trước khi đặt hàng.",
                "Hướng dẫn", TOPIC_GUIDE, "5-buoc-mua-my-pham-online-an-toan-hon",
                28, "/products?category=all", "Đọc cẩm nang",
                "Khi mua mỹ phẩm online, bạn nên kiểm tra thông tin thương hiệu, hình ảnh sản phẩm, dung tích, hạn sử dụng và mô tả chi tiết.\n\nĐánh giá của người mua trước là nguồn tham khảo quan trọng, nhưng vẫn cần đối chiếu với nhu cầu thực tế của bạn.\n\nCuối cùng, hãy xem chính sách đổi trả, thanh toán và giao hàng để đảm bảo đơn hàng được xử lý an toàn.");

        add(events, "Những lưu ý khi chọn mỹ phẩm cho người mới bắt đầu",
                "Ưu tiên sản phẩm cơ bản, dễ dùng, bảng thành phần rõ ràng và phù hợp với ngân sách.",
                "Cẩm nang", TOPIC_GUIDE, "nhung-luu-y-khi-chon-my-pham-cho-nguoi-moi",
                29, "/products?category=all", "Xem hướng dẫn",
                "Người mới bắt đầu không cần mua quá nhiều sản phẩm cùng lúc. Hãy ưu tiên những món cơ bản, dễ dùng và phù hợp với nhu cầu hiện tại.\n\nNếu chăm sóc da, hãy bắt đầu với sữa rửa mặt, kem dưỡng và chống nắng. Nếu trang điểm, có thể bắt đầu với cushion, son môi và má hồng.\n\nKhi da chưa quen sản phẩm, nên thử từng món một để dễ nhận biết sản phẩm nào phù hợp hoặc gây kích ứng.");

        return events;
    }

    private void add(List<Event> events,
                     String title,
                     String summary,
                     String tag,
                     String topicKey,
                     String slug,
                     int day,
                     String actionUrl,
                     String actionText,
                     String content) {
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
}

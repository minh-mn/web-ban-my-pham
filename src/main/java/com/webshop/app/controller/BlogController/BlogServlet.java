package com.webshop.app.controller.BlogController;

import com.webshop.app.dao.EventDAO;
import com.webshop.app.model.Event;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/blog", "/blog/detail"})
public class BlogServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final EventDAO eventDAO = new EventDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/blog/detail".equals(path)) {
            handleEventDetail(req, resp);
        } else {
            handleEventList(req, resp);
        }
    }

    // 1. Xử lý hiển thị toàn bộ sự kiện (Xem tất cả) + Tìm kiếm sự kiện
    private void handleEventList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String searchQuery = req.getParameter("search");
        List<Event> events;

        // Nếu có từ khóa tìm kiếm thì gọi hàm searchEvents mới tạo
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            events = eventDAO.searchEvents(searchQuery.trim());
            req.setAttribute("searchQuery", searchQuery.trim());
        } else {
            // Ngược lại lấy toàn bộ sự kiện sắp xếp mới nhất
            events = eventDAO.getAllEvents();
        }

        req.setAttribute("events", events);

        // Cấu hình Layout theo mẫu chuẩn của dự án giống HomeServlet
        req.setAttribute("pageTitle", "MyCosmetic | Tin tức & Sự kiện");
        req.setAttribute("pageCss", "home.css"); // Kế thừa CSS home để lấy style card sự kiện có sẵn
        req.setAttribute("pageContent", "/jsp/events/event_all.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    // 2. Xử lý hiển thị chi tiết một sự kiện khi nhấn "Xem chi tiết"
    private void handleEventDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String idParam = req.getParameter("id");
            if (idParam != null) {
                int id = Integer.parseInt(idParam);
                Event event = eventDAO.getEventById(id);

                if (event != null) {
                    req.setAttribute("event", event);
                    req.setAttribute("pageTitle", event.getTitle());
                    req.setAttribute("pageCss", "home.css");
                    req.setAttribute("pageContent", "/jsp/events/event_detail.jsp");

                    req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
                    return;
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        // Nếu có lỗi hoặc không tìm thấy bài viết, chuyển hướng về trang danh sách chung
        resp.sendRedirect(req.getContextPath() + "/blog");
    }
}
